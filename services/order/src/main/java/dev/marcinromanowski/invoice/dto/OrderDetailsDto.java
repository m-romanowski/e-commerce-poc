package dev.marcinromanowski.invoice.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record OrderDetailsDto(UUID id, String userId, Set<ProductDetailsDto> products) {

    public record ProductDetailsDto(UUID id, String name, int amount, BigDecimal price) {

    }

}
