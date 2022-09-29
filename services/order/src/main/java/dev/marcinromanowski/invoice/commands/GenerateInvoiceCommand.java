package dev.marcinromanowski.invoice.commands;

import lombok.Value;

import java.util.UUID;

@Value
public class GenerateInvoiceCommand implements InvoiceCommand {

    static final String TYPE_NAME = "GenerateInvoice";
    static final String VERSION = "1.0";

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
