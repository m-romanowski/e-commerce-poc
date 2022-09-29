package dev.marcinromanowski.invoice;

interface InvoiceCommandListener {
    void process(String serializedCommand);
}
