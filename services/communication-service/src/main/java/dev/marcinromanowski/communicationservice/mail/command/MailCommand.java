package dev.marcinromanowski.communicationservice.mail.command;

import dev.marcinromanowski.communicationservice.common.CommunicationServiceCommand;

public sealed interface MailCommand extends CommunicationServiceCommand permits SendInvoiceToUserMailCommand {
    EmailMetadata getTo();
}
