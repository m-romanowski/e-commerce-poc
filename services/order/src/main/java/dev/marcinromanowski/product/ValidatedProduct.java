package dev.marcinromanowski.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ValidatedProduct(UUID id, String name, BigDecimal price) {

}
