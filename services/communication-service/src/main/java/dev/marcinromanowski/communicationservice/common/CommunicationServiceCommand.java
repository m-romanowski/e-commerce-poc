package dev.marcinromanowski.communicationservice.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SendInvoiceToUserMailCommand.class, name = SendInvoiceToUserMailCommand.TYPE_NAME)
})
public interface CommunicationServiceCommand extends IntegrationMessage {

}
