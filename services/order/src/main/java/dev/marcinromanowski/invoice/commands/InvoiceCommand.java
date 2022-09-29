package dev.marcinromanowski.invoice.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GenerateInvoiceCommand.class, name = GenerateInvoiceCommand.TYPE_NAME)
})
public sealed interface InvoiceCommand permits GenerateInvoiceCommand {
    UUID getId();
    String getType();
    String getVersion();
}
