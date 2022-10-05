package dev.marcinromanowski.order.exception;

public class CannotProcessOrderException extends OrderException {

    public CannotProcessOrderException(Throwable cause) {
        super("Cannot process order", cause);
    }

}
