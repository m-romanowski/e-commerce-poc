package dev.marcinromanowski.order.dto;

import java.util.Set;
import java.util.UUID;

public record OrderDto(String userId, Set<ProductDto> products) {

    public record ProductDto(UUID id, int amount) {

    }

}
