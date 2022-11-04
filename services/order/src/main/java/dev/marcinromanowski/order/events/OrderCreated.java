package dev.marcinromanowski.order.events;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Value
public class OrderCreated implements OrderEvent {

    public static final String TYPE_NAME = "OrderCreated";
    public static final String VERSION = "1.0";

    @NonNull
    UUID id;
    @NonNull
    String userId;
    @NonNull
    BigDecimal total;
    @NonNull
    Set<UUID> products;

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
