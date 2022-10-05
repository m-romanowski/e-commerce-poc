package dev.marcinromanowski.invoice.events;

import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class InvoiceCreated implements InvoiceEvent {

    public static final String TYPE_NAME = "InvoiceCreated";
    public static final String VERSION = "1.0";

    @NonNull
    UUID id;
    @NonNull
    UUID orderId;
    @NonNull
    Instant createdAt;

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
