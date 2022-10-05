package dev.marcinromanowski.order;

import dev.marcinromanowski.product.ValidatedProduct;

import java.math.BigDecimal;
import java.util.UUID;

record OrderProductDetails(ValidatedProduct validatedProduct, int amount) {

    UUID getId() {
        return validatedProduct.id();
    }

    BigDecimal getTotal() {
        return new BigDecimal(amount).multiply(validatedProduct.price());
    }

}
