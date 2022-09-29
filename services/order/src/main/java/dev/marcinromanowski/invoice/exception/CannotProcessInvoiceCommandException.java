package dev.marcinromanowski.invoice.exception;

public class CannotProcessInvoiceCommandException extends InvoiceException {

    public CannotProcessInvoiceCommandException(Throwable cause) {
        super("Cannot process invoice command", cause);
    }

}
