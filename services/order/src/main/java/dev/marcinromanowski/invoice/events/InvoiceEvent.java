package dev.marcinromanowski.invoice.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.marcinromanowski.common.IntegrationMessage;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InvoiceCreated.class, name = InvoiceCreated.TYPE_NAME)
})
public sealed interface InvoiceEvent extends IntegrationMessage permits InvoiceCreated {
    UUID getId();
}
