package dev.marcinromanowski.order.exception;

import java.util.UUID;

public class OrderInconsistencyStateException extends OrderException {

    public OrderInconsistencyStateException(UUID orderId) {
        super("Order is in inconsistency state, id: %s".formatted(orderId));
    }

}
