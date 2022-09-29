package dev.marcinromanowski.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.val;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

sealed interface Order permits PendingOrder, SucceededOrder, FailedOrder {
    UUID getId();
    String getUserId();
    BigDecimal getTotal();
    Set<Product> getProducts();
}

@Value
class Product {

    UUID id;
    String name;
    int amount;
    BigDecimal price;

    BigDecimal getTotal() {
        return price.multiply(new BigDecimal(amount));
    }

}

@Value
@NonFinal
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
non-sealed class PendingOrder implements Order {

    UUID id;
    String userId;
    String paymentId;
    BigDecimal total;
    Set<Product> products;

    static PendingOrder from(String userId, List<Product> products) {
        val totalPrice = products.stream()
                .map(Product::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return PendingOrder.builder()
                .userId(userId)
                .total(totalPrice)
                .products(products.stream().collect(Collectors.toUnmodifiableSet()))
                .build();
    }

    PendingOrder paymentInitialized(String paymentId) {
        return this.toBuilder()
                .paymentId(paymentId)
                .build();
    }

    SucceededOrder succeeded(String paymentId) {
        return new SucceededOrder(this, paymentId);
    }

    FailedOrder failed() {
        return new FailedOrder(this);
    }

}

@Value
@NonFinal
non-sealed class SucceededOrder implements Order {

    PendingOrder pendingOrder;
    String paymentId;

    @Override
    public UUID getId() {
        return pendingOrder.getId();
    }

    @Override
    public String getUserId() {
        return pendingOrder.getUserId();
    }

    @Override
    public BigDecimal getTotal() {
        return pendingOrder.getTotal();
    }

    @Override
    public Set<Product> getProducts() {
        return pendingOrder.getProducts();
    }

}

@Value
@NonFinal
non-sealed class FailedOrder implements Order {

    PendingOrder pendingOrder;

    @Override
    public UUID getId() {
        return pendingOrder.getId();
    }

    @Override
    public String getUserId() {
        return pendingOrder.getUserId();
    }

    @Override
    public BigDecimal getTotal() {
        return pendingOrder.getTotal();
    }

    @Override
    public Set<Product> getProducts() {
        return pendingOrder.getProducts();
    }

}
