package dev.marcinromanowski.invoice.exception;

public class InvoiceException extends RuntimeException {

    public InvoiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
