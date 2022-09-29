package dev.marcinromanowski.invoice.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record InvoiceOrderDto(UUID id, String userId, Set<ProductDto> products) {

    public record ProductDto(UUID id, String name, int amount, BigDecimal price) {

    }

}
