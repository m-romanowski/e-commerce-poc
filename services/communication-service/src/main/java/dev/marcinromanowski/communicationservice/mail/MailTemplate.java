package dev.marcinromanowski.communicationservice.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.marcinromanowski.communicationservice.mail.command.SendInvoiceToUserMailCommand;
import lombok.val;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

interface MailTemplate {
    String getSubjectId();
    String getTemplateId();
}

record InvoiceMailTemplate(String userId, String downloadUrl, Order order) implements MailTemplate {

    private static final String DEFAULT_USER = "User";

    static InvoiceMailTemplate from(SendInvoiceToUserMailCommand command) {
        val products = command.getOrder().products()
            .stream()
            .map(product -> new Product(product.name(), product.amount(), product.price()))
            .collect(Collectors.toUnmodifiableSet());
        return new InvoiceMailTemplate(
            Optional.ofNullable(command.getUserId()).orElse(DEFAULT_USER),
            command.getDownloadUrl(),
            new Order(command.getOrder().totalPrice(), products)
        );
    }

    @Override
    @JsonIgnore
    public String getSubjectId() {
        return "email.invoice.title";
    }

    @Override
    @JsonIgnore
    public String getTemplateId() {
        return "invoice-email";
    }

    record Product(String name, int amount, BigDecimal price) {

    }

    record Order(BigDecimal totalPrice, Set<Product> products) {

    }

}
