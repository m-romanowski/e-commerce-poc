package dev.marcinromanowski.communicationservice.mail.command;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Value
public class SendInvoiceToUserMailCommand implements MailCommand {

    public static final String TYPE_NAME = "SendInvoiceToUserMailCommand";
    public static final String VERSION = "1.0";

    @NonNull
    UUID id;
    String userId;
    @NonNull
    EmailMetadata to;
    @NonNull
    String downloadUrl;
    @NonNull
    Order order;

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    public record Product(String name, int amount, BigDecimal price) {

    }

    public record Order(BigDecimal totalPrice, Set<Product> products) {

    }

}
