package dev.marcinromanowski.communicationservice.mail

import ch.qos.logback.classic.spi.ILoggingEvent
import dev.marcinromanowski.communicationservice.base.IntegrationSpec
import dev.marcinromanowski.communicationservice.base.LogAppender
import dev.marcinromanowski.communicationservice.infrastructure.mail.CommunicationServiceCommandListener
import dev.marcinromanowski.communicationservice.infrastructure.mail.CoutMailSender
import dev.marcinromanowski.communicationservice.mail.command.EmailMetadata
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import spock.lang.AutoCleanup

import static dev.marcinromanowski.communicationservice.base.PredefinedPollingConditions.WAIT

@Import(MailIntegrationSpecConfiguration)
class MailIntegrationSpec extends IntegrationSpec {

    @AutoCleanup
    private LogAppender.ThreadSafeListAppender<ILoggingEvent> listenerLogAppender
    @AutoCleanup
    private LogAppender.ThreadSafeListAppender<ILoggingEvent> senderLogAppender
    @Autowired
    private MailCommandsKafkaPublisher mailCommandsKafkaPublisher

    def setup() {
        listenerLogAppender = LogAppender.getListAppenderForClass(CommunicationServiceCommandListener)
        senderLogAppender = LogAppender.getListAppenderForClass(CoutMailSender)
    }

    def "Mail command should be eventually processed"() {
        when: "invalid command come"
            mailCommandsKafkaPublisher.sendInvalidMessage()
        then: "error log is printed"
            WAIT.eventually {
                verifyAll(listenerLogAppender.eventsContainer) {
                    it.size() == 1
                    it[0].message.containsIgnoreCase("Cannot deserialize command")
                }
            }

        when: "unknown command come"
            mailCommandsKafkaPublisher.sendUnknownCommand()
        then: "error log is printed"
            WAIT.eventually {
                verifyAll(listenerLogAppender.eventsContainer) {
                    it.size() == 2
                    it[0].message.containsIgnoreCase("Cannot deserialize command")
                }
            }

        when: "valid invoice mail command come"
            def invoiceMailCommand = new SendInvoiceToUserMailCommand(
                    UUID.randomUUID(),
                    "user",
                    new EmailMetadata("user@example.com", "en"),
                    "https://example.com?download=${UUID.randomUUID()}",
                    new SendInvoiceToUserMailCommand.Order(1.0, [new SendInvoiceToUserMailCommand.Product("Product name", 1, 1.0)] as Set)
            )
            mailCommandsKafkaPublisher.sendInvoiceMailCommand(invoiceMailCommand)
        then: "command is eventually processed"
        and: "email is sent"
            WAIT.eventually {
                verifyAll(senderLogAppender.eventsContainer) {
                    it.size() == 1
                    it[0].message.containsIgnoreCase("Email sent")
                }
            }
    }

}
