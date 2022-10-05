package dev.marcinromanowski.communicationservice.base

import dev.marcinromanowski.CommunicationServiceApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Clock

@Import(IntegrationSpecConfiguration)
@SpringBootTest(classes = [CommunicationServiceApplication])
abstract class IntegrationSpec extends Specification implements ClockFixture {

    @Autowired
    MutableClock testClock

    static {
        Kafka.startContainer()
        setupMailProperties()
    }

    static void setupMailProperties() {
        System.setProperty("mail.email.reply-to", "contact@example.com")
        System.setProperty("mail.kafka.command-topic", "commands-topic-${UUID.randomUUID()}")
    }

    def setup() {
        testClock.setInstant(clock().instant())
    }

    def cleanup() {
        testClock.setInstant(clock().instant())
    }

    private static class IntegrationSpecConfiguration implements ClockFixture {

        @Bean
        @Primary
        Clock testClock() {
            return new MutableClock(clock().instant())
        }

    }

}
