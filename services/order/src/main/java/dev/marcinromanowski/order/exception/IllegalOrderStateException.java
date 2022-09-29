package dev.marcinromanowski.order.exception;

public class IllegalOrderStateException extends OrderException {

    public IllegalOrderStateException() {
        super("Illegal order state");
    }

}
