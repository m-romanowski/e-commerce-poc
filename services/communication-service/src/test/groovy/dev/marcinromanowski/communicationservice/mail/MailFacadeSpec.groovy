package dev.marcinromanowski.communicationservice.mail

import dev.marcinromanowski.communicationservice.mail.command.EmailMetadata
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand
import org.springframework.context.MessageSource
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.IContext
import spock.lang.Specification

class MailFacadeSpec extends Specification {

    private MessageSource messageSource
    private ITemplateEngine iTemplateEngine
    private MailSender mailSender
    private MailFacade mailFacade

    def setup() {
        def configuration = new MailConfiguration()
        def mailProperties = new MailProperties().tap {
            it.email = new MailProperties.Email().tap {
                it.replyTo = "reply@example.com"
                it.defaultLanguage = "en"
            }
        }
        messageSource = Mock(MessageSource)
        iTemplateEngine = Mock(ITemplateEngine)
        mailSender = Mock(MailSender)
        mailFacade = configuration.mailFacade(mailProperties, messageSource, iTemplateEngine, mailSender)
    }

    def "Invoice mail should be send to the user"() {
        given: "email command"
            messageSource.getMessage("email.invoice.title", null, Locale.forLanguageTag("pl")) >> "Invoice subject"
            iTemplateEngine.process("invoice-email", _ as IContext) >> "Invoice body"

            def invoiceMailCommand = new SendInvoiceToUserMailCommand(
                    UUID.randomUUID(),
                    null,
                    new EmailMetadata("user@example.com", "pl"),
                    "https://example.com?download=${UUID.randomUUID()}",
                    new SendInvoiceToUserMailCommand.Order(1.0, [new SendInvoiceToUserMailCommand.Product("Product name", 1, 1.0)] as Set)
            )

        when: "command come"
            mailFacade.sendMail(invoiceMailCommand)
        then: "email was sent"
            1 * mailSender.send({ MailRequest request ->
                request.id() == invoiceMailCommand.id.toString()
                        && request.to().value() == "user@example.com"
                        && request.to().language() == "pl"
                        && request.from().value() == "reply@example.com"
                        && request.from().language() == "en"
                        && request.subject() == "Invoice subject"
                        && request.body() == "Invoice body"
            })
    }

}
