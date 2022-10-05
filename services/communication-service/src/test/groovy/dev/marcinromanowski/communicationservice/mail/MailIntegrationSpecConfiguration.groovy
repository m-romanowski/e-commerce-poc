package dev.marcinromanowski.communicationservice.mail

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate

import static groovy.transform.PackageScopeTarget.CLASS
import static groovy.transform.PackageScopeTarget.CONSTRUCTORS
import static groovy.transform.PackageScopeTarget.FIELDS
import static groovy.transform.PackageScopeTarget.METHODS

@CompileStatic
@PackageScope(value = [CLASS, CONSTRUCTORS, FIELDS, METHODS])
class MailIntegrationSpecConfiguration {

    @Bean
    MailCommandsKafkaPublisher mailCommandsKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, MailProperties mailProperties) {
        return new MailCommandsKafkaPublisher(kafkaTemplate, mailProperties)
    }

}
