package dev.marcinromanowski.order.events;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderSucceeded implements OrderEvent {

    public static final String TYPE_NAME = "OrderSucceeded";
    public static final String VERSION = "1.0";

    @NonNull
    UUID id;
    @NonNull
    String paymentId;

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
