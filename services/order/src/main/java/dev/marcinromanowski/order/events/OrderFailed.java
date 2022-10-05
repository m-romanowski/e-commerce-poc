package dev.marcinromanowski.order.events;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderFailed implements OrderEvent {

    public static final String TYPE_NAME = "OrderFailed";
    public static final String VERSION = "1.0";

    @NonNull
    UUID id;

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
