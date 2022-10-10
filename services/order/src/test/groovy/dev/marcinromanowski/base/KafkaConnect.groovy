package dev.marcinromanowski.base

import dev.marcinromanowski.testcontainers.KafkaConnectContainer
import groovy.transform.CompileStatic
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

@CompileStatic
class KafkaConnect {

    static final short DEFAULT_PORT = 8083
    static final String DEFAULT_SERVICE_NAME = "tests-kafka-connect"

    static void startContainer(Network network, String bootstrapServers, List<KafkaConnectContainer.ConnectorConfiguration> connectors) {
        def container = new KafkaConnectContainer(DockerImageName.parse("confluentinc/cp-kafka-connect:6.2.1"), bootstrapServers)
                .withPlugin("confluentinc-kafka-connect-jdbc-10.5.3")
                .withNetwork(network)
                .withNetworkAliases(DEFAULT_SERVICE_NAME)
        container.start()
        connectors.forEach { container.withConnector(it) }
    }

    static String getNetworkBootstrapServer() {
        return "$DEFAULT_SERVICE_NAME:$DEFAULT_PORT"
    }

}
