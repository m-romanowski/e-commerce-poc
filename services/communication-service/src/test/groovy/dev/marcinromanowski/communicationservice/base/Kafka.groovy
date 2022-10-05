package dev.marcinromanowski.communicationservice.base

import groovy.transform.CompileStatic
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

@CompileStatic
class Kafka {

    static void startContainer() {
        def container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
        container.start()

        System.setProperty("spring.kafka.bootstrap-servers", container.bootstrapServers)
    }

}
