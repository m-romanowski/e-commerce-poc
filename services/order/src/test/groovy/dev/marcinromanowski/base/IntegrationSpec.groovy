package dev.marcinromanowski.base

import dev.marcinromanowski.OrderApplication
import dev.marcinromanowski.common.Profiles
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Shared
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Clock

@Import(IntegrationConfiguration)
@AutoConfigureMockMvc
@ActiveProfiles([Profiles.INTEGRATION])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [OrderApplication])
abstract class IntegrationSpec extends Specification implements ClockFixture, MockWebServerSupplier {

    static {
        PostgreSQL.startContainer()
    }

    @Shared
    MockWebServer mockWebServer
    @Autowired
    MutableClock testClock
    @Autowired
    MockMvc mockMvc

    def setupSpec() {
        mockWebServer = new MockWebServer()
        mockWebServer.start(9090)
    }

    void setup() {
        testClock.setInstant(clock().instant())
    }

    void cleanup() {
        testClock.setInstant(clock().instant())
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

    }

}
