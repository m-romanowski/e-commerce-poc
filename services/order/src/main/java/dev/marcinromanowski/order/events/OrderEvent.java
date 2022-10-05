package dev.marcinromanowski.order.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.marcinromanowski.common.IntegrationMessage;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderCreated.class, name = OrderCreated.TYPE_NAME),
    @JsonSubTypes.Type(value = OrderSucceeded.class, name = OrderSucceeded.TYPE_NAME),
    @JsonSubTypes.Type(value = OrderFailed.class, name = OrderFailed.TYPE_NAME)
})
public sealed interface OrderEvent extends IntegrationMessage permits OrderCreated, OrderSucceeded, OrderFailed {
    UUID getId();
}
