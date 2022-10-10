package dev.marcinromanowski.base

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Slf4j
@CompileStatic
class TestKafkaConsumers implements Closeable, Runnable {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(100)
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1)

    private final Set<String> topics
    private final AtomicBoolean isRunning
    private final ScheduledExecutorService scheduledExecutorService
    private final KafkaConsumer<String, String> kafkaConsumer

    TestKafkaConsumers(Set<String> topics, String bootstrapServers) {
        this.topics = topics
        this.isRunning = new AtomicBoolean()
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        this.kafkaConsumer = new KafkaConsumer<>(getKafkaConsumerProperties(bootstrapServers))
    }

    private static Map<String, Object> getKafkaConsumerProperties(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-kafka-consumers-group-id")
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        props.put(ConsumerConfig.DEFAULT_ISOLATION_LEVEL, "read_committed")
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
        return props
    }

    @Override
    void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        kafkaConsumer.subscribe(topics)
        scheduledExecutorService.schedule(() -> {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(POLL_TIMEOUT)
            records.each { ConsumerRecord<String, String> record ->
                log.info("Consumed record. Key: {}, Value:", record.key(), JsonOutput.prettyPrint(record.value()))
                MockConsumers.getMockConsumers(record.topic()).each {
                    it.consumed(record.key(), record.value())
                }
            }
        }, POLL_INTERVAL.toMillis(), TimeUnit.MILLISECONDS)
    }

    @Override
    void close() throws IOException {
        isRunning.compareAndSet(true, false)
        kafkaConsumer.close()
        scheduledExecutorService.shutdown()
    }

}
