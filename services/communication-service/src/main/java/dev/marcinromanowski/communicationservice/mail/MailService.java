package dev.marcinromanowski.communicationservice.mail;

import dev.marcinromanowski.communicationservice.mail.command.EmailMetadata;
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.context.MessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class MailService {

    private static final String BODY_VAR_NAME = "data";

    MailProperties mailProperties;
    MessageSource messageSource;
    ITemplateEngine templateEngine;
    MailSender mailSender;

    private MailResponse sendEmail(String id, EmailMetadata to, MailTemplate mailTemplate) {
        val replyEmailProperties = mailProperties.getEmail();
        val receiverEmailLanguage = Optional.ofNullable(to.language())
            .orElseGet(replyEmailProperties::getDefaultLanguage);
        val locale = Locale.forLanguageTag(receiverEmailLanguage);
        val context = new Context(locale);
        context.setVariable(BODY_VAR_NAME, mailTemplate);

        val mailRequest = new MailRequest(
            id,
            new EmailMetadata(replyEmailProperties.getReplyTo(), replyEmailProperties.getDefaultLanguage()),
            to,
            messageSource.getMessage(mailTemplate.getSubjectId(), null, locale),
            templateEngine.process(mailTemplate.getTemplateId(), context)
        );

        return mailSender.send(mailRequest);
    }

    MailResponse sendInvoice(SendInvoiceToUserMailCommand command) {
        return sendEmail(command.getId().toString(), command.getTo(), InvoiceMailTemplate.from(command));
    }

}
