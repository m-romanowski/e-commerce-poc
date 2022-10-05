package dev.marcinromanowski.communicationservice.mail;

import dev.marcinromanowski.communicationservice.mail.command.MailCommand;
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MailFacade {

    private final MailService mailService;

    public MailResponse sendMail(MailCommand command) {
        return switch (command) {
            case SendInvoiceToUserMailCommand mailCommand -> mailService.sendInvoice(mailCommand);
        };
    }

}
