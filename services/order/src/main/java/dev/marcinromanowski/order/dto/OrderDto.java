package dev.marcinromanowski.order.dto;

import java.util.Set;
import java.util.UUID;

// TODO: We need additional user's information like email, etc - especially for not registered users
public record OrderDto(String userId, Set<ProductDto> products) {

    public record ProductDto(UUID id, int amount) {

    }

}
