package dev.marcinromanowski.communicationservice.mail

import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand.Product
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.kafka.core.KafkaTemplate

import java.util.concurrent.TimeUnit

import static groovy.transform.PackageScopeTarget.CLASS
import static groovy.transform.PackageScopeTarget.CONSTRUCTORS
import static groovy.transform.PackageScopeTarget.FIELDS
import static groovy.transform.PackageScopeTarget.METHODS

@CompileStatic
@PackageScope(value = [CLASS, CONSTRUCTORS, FIELDS, METHODS])
class MailCommandsKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate
    private final MailProperties mailProperties

    MailCommandsKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, MailProperties mailProperties) {
        this.kafkaTemplate = kafkaTemplate
        this.mailProperties = mailProperties
    }

    private static String valueAsStringOrNull(Object value) {
        return value == null ? null : /"$value"/
    }

    private static String invoiceOrderProductAsJson(Product product) {
        return """
            {
                "name": "${product.name()}",
                "amount": ${product.amount()},
                "price": "${product.price()}"
            }
        """
    }

    private static String invoiceCommandAsJson(SendInvoiceToUserMailCommand command) {
        return """
            {
                "id": "$command.id",
                "userId": ${valueAsStringOrNull(command.userId)},
                "to": {
                    "value": "${command.to.value()}",
                    "language": "${command.to.language()}"
                },
                "downloadUrl": "$command.downloadUrl",
                "order": {
                    "totalPrice": "${command.order.totalPrice()}",
                    "products": [${command.order.products().collect { invoiceOrderProductAsJson(it) }.join(",")}]
                },
                "type": "SendInvoiceToUserMailCommand",
                "version": "1.0"
            }
        """
    }

    private static String unknownCommandAsJson() {
        return """
            {
                "type": "Unknown",
                "version": "1.0"
            }
        """
    }

    void sendInvalidMessage() {
        sendTo(mailProperties.kafka.commandTopic, UUID.randomUUID().toString(), "{notValidJson")
    }

    void sendUnknownCommand() {
        sendTo(mailProperties.kafka.commandTopic, UUID.randomUUID().toString(), unknownCommandAsJson())
    }

    void sendInvoiceMailCommand(SendInvoiceToUserMailCommand command) {
        sendTo(mailProperties.kafka.commandTopic, command.id.toString(), invoiceCommandAsJson(command))
    }

    private void sendTo(String topic, String key, String value) {
        kafkaTemplate.send(topic, key, value).get(5, TimeUnit.SECONDS)
    }

}
