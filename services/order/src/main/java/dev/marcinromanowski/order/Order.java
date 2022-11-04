package dev.marcinromanowski.order;

import dev.marcinromanowski.order.events.OrderCreated;
import dev.marcinromanowski.order.events.OrderEvent;
import dev.marcinromanowski.order.events.OrderFailed;
import dev.marcinromanowski.order.events.OrderSucceeded;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

sealed interface Order permits PendingOrder, SucceededOrder, FailedOrder {
    UUID getId();
    String getUserId();
    BigDecimal getTotal();
    String getPaymentId();
    Long getVersion();
    Set<Product> getProducts();
    OrderEvent toEvent();
}

record Product(UUID id, Long version) {

}

@Value
@RequiredArgsConstructor(staticName = "from")
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
class PendingOrder implements Order {

    UUID id;
    String userId;
    String paymentId;
    BigDecimal total;
    Set<Product> products;
    Long version;

    static PendingOrder create(Supplier<UUID> idSupplier, String userId, List<OrderProductDetails> products) {
        val totalPrice = products.stream()
            .map(OrderProductDetails::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return PendingOrder.builder()
            .id(idSupplier.get())
            .userId(userId)
            .total(totalPrice)
            .products(products.stream().map(productDetails -> new Product(productDetails.getId(), null)).collect(Collectors.toUnmodifiableSet()))
            .build();
    }

    PendingOrder withPendingPayment(String paymentId) {
        return this.toBuilder()
            .paymentId(paymentId)
            .build();
    }

    SucceededOrder succeeded() {
        return new SucceededOrder(this);
    }

    FailedOrder failed() {
        return new FailedOrder(this);
    }

    @Override
    public OrderEvent toEvent() {
        return new OrderCreated(id, userId, total, products.stream().map(Product::id).collect(Collectors.toUnmodifiableSet()));
    }

}

@Value
class SucceededOrder implements Order {

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
    public String getPaymentId() {
        return pendingOrder.getPaymentId();
    }

    @Override
    public Long getVersion() {
        return pendingOrder.getVersion();
    }

    @Override
    public Set<Product> getProducts() {
        return pendingOrder.getProducts();
    }

    @Override
    public OrderEvent toEvent() {
        return new OrderSucceeded(getId(), getPaymentId());
    }

}

@Value
class FailedOrder implements Order {

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
    public String getPaymentId() {
        return pendingOrder.getPaymentId();
    }

    @Override
    public Long getVersion() {
        return pendingOrder.getVersion();
    }

    @Override
    public Set<Product> getProducts() {
        return pendingOrder.getProducts();
    }

    @Override
    public OrderEvent toEvent() {
        return new OrderFailed(getId());
    }

}
