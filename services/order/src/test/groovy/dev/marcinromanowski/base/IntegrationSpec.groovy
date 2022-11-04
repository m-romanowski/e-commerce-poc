package dev.marcinromanowski.base

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcinromanowski.OrderApplication
import dev.marcinromanowski.common.Profiles
import dev.marcinromanowski.testcontainers.KafkaConnectContainer
import okhttp3.mockwebserver.MockWebServer
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.Network
import spock.lang.Shared
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Clock

@Import(IntegrationConfiguration)
@ActiveProfiles([Profiles.INTEGRATION])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [OrderApplication])
abstract class IntegrationSpec extends Specification implements ClockFixture, MockWebServerSupplier {

    static final String ORDER_EVENTS_TOPIC_NAME = "order-events"
    static final String INVOICE_EVENTS_TOPIC_NAME = "invoice-events"

    static {
        Network.newNetwork().with {
            PostgreSQL.startContainer(it)
            Kafka.startContainer(it)
            KafkaConnect.startContainer(
                    it,
                    Kafka.getNetworkBootstrapServers(),
                    getConnectorsConfiguration(PostgreSQL.getNetworkJdbcUrl(), PostgreSQL.DEFAULT_USERNAME, PostgreSQL.DEFAULT_PASSWORD)
            )
        }
    }

    private static List<KafkaConnectContainer.ConnectorConfiguration> getConnectorsConfiguration(String jdbcConnectionUrl, String dbUser, String dbPassword) {
        // FIXME: incrementing column cannot be UUID field - https://github.com/confluentinc/kafka-connect-jdbc/issues/469
        return [
                new KafkaConnectContainer.ConnectorConfiguration(
                        "order-events-connector",
                        [
                                "name"                          : "order-events-connector",
                                "connector.class"               : "io.confluent.connect.jdbc.JdbcSourceConnector",
                                "tasks.max"                     : "1",
                                "poll.interval.ms"              : 100,
                                "connection.url"                : jdbcConnectionUrl,
                                "connection.user"               : dbUser,
                                "connection.password"           : dbPassword,
                                "topics"                        : ORDER_EVENTS_TOPIC_NAME,
                                "topic.prefix"                  : ORDER_EVENTS_TOPIC_NAME,
                                "mode"                          : "incrementing",
                                "numeric.mapping"               : "best_fit",
                                "incrementing.column.name"      : "id",
                                "query"                         : "SELECT id, key, payload FROM public.order_outbox",
                                "dialect.name"                  : "PostgreSqlDatabaseDialect",
                                "transforms"                    : "createKey,extractKey,extractValue",
                                "transforms.createKey.type"     : "org.apache.kafka.connect.transforms.ValueToKey",
                                "transforms.createKey.fields"   : "key",
                                "transforms.extractKey.type"    : "org.apache.kafka.connect.transforms.ExtractField\$Key",
                                "transforms.extractKey.field"   : "key",
                                "transforms.extractValue.type"  : "org.apache.kafka.connect.transforms.ExtractField\$Value",
                                "transforms.extractValue.field" : "payload",
                                "key.converter.schemas.enable"  : "false",
                                "key.converter"                 : "org.apache.kafka.connect.storage.StringConverter",
                                "value.converter.schemas.enable": "false",
                                "value.converter"               : "org.apache.kafka.connect.storage.StringConverter"
                        ]
                ),
                new KafkaConnectContainer.ConnectorConfiguration(
                        "invoice-events-connector",
                        [
                                "name"                          : "invoice-events-connector",
                                "connector.class"               : "io.confluent.connect.jdbc.JdbcSourceConnector",
                                "tasks.max"                     : "1",
                                "poll.interval.ms"              : 100,
                                "connection.url"                : jdbcConnectionUrl,
                                "connection.user"               : dbUser,
                                "connection.password"           : dbPassword,
                                "topics"                        : INVOICE_EVENTS_TOPIC_NAME,
                                "topic.prefix"                  : INVOICE_EVENTS_TOPIC_NAME,
                                "mode"                          : "incrementing",
                                "numeric.mapping"               : "best_fit",
                                "incrementing.column.name"      : "id",
                                "query"                         : "SELECT id, key, payload FROM public.invoice_outbox",
                                "dialect.name"                  : "PostgreSqlDatabaseDialect",
                                "transforms"                    : "createKey,extractKey,extractValue",
                                "transforms.createKey.type"     : "org.apache.kafka.connect.transforms.ValueToKey",
                                "transforms.createKey.fields"   : "key",
                                "transforms.extractKey.type"    : "org.apache.kafka.connect.transforms.ExtractField\$Key",
                                "transforms.extractKey.field"   : "key",
                                "transforms.extractValue.type"  : "org.apache.kafka.connect.transforms.ExtractField\$Value",
                                "transforms.extractValue.field" : "payload",
                                "key.converter.schemas.enable"  : "false",
                                "key.converter"                 : "org.apache.kafka.connect.storage.StringConverter",
                                "value.converter.schemas.enable": "false",
                                "value.converter"               : "org.apache.kafka.connect.storage.StringConverter"
                        ]
                )
        ]
    }

    private static void setupProductProperties(int productServicePort) {
        System.setProperty("product.details-service.base-url", "http://localhost:$productServicePort/product")
    }

    @Shared
    MockWebServer mockWebServer
    @Autowired
    MutableClock testClock
    @Autowired
    WebTestClient webTestClient

    def setupSpec() {
        mockWebServer = new MockWebServer()
        mockWebServer.start()
        setupProductProperties(mockWebServer.port)
    }

    void setup() {
        testClock.setInstant(clock().instant())
    }

    void cleanup() {
        testClock.setInstant(clock().instant())
        MockConsumers.unregisterMockConsumers()
    }

    void cleanupSpec() {
        mockWebServer.shutdown()
    }

    @TestConfiguration
    static class IntegrationConfiguration {

        @Bean
        @Primary
        Clock testClock() {
            return new MutableClock()
        }

        @Bean
        @Primary
        WebClient.Builder testWebclientBuilder(ObjectMapper objectMapper) {
            return WebClient.builder()
                    .codecs(configurer -> {
                        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
                        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
                    })
        }

        @Bean
        TestKafkaConsumers testKafkaConsumers(@Value(value = '${spring.kafka.bootstrap-servers}') String bootstrapServers) {
            TestKafkaConsumers testKafkaConsumers = new TestKafkaConsumers([ORDER_EVENTS_TOPIC_NAME, INVOICE_EVENTS_TOPIC_NAME] as Set, bootstrapServers)
            testKafkaConsumers.run()
            return testKafkaConsumers
        }

        @Bean
        NewTopic orderEventsTopic() {
            return new NewTopic(ORDER_EVENTS_TOPIC_NAME, 1, (short) 1)
        }

        @Bean
        NewTopic invoiceEventsTopic() {
            return new NewTopic(INVOICE_EVENTS_TOPIC_NAME, 1, (short) 1)
        }

    }

}
