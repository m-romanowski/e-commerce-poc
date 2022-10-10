package dev.marcinromanowski.base

import groovy.transform.CompileStatic
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

@CompileStatic
class Kafka {

    static final short DEFAULT_PORT = 9092
    static final String DEFAULT_SERVICE_NAME = "tests-kafka"

    static void startContainer(Network network) {
        def container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withNetwork(network)
                .withNetworkAliases(DEFAULT_SERVICE_NAME)
        container.start()

        System.setProperty("spring.kafka.bootstrap-servers", container.bootstrapServers)
    }

    static String getNetworkBootstrapServers() {
        return "$DEFAULT_SERVICE_NAME:$DEFAULT_PORT"
    }

}
