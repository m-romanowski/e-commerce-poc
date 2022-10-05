package dev.marcinromanowski.invoice.exception;

public class CannotProcessInvoiceException extends InvoiceException {

    public CannotProcessInvoiceException(Throwable cause) {
        super("Cannot process invoice", cause);
    }

}
