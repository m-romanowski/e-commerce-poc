package dev.marcinromanowski.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

interface OrderRepository {
    Mono<Order> save(Order order);
    Mono<Order> findByPaymentId(String paymentId);
}

interface OrderCrudRepository extends ReactiveCrudRepository<OrderEntity, UUID> {
    Mono<OrderEntity> findByPaymentId(String id);
}

interface OrderProductsCrudRepository extends ReactiveCrudRepository<ProductEntity, UUID> {
    Flux<ProductEntity> findAllByOrderId(UUID orderId);
}

// TODO: R2DBC doesnt support relationships (https://github.com/spring-projects/spring-data-r2dbc/issues/356), its a workaround to saving "many"
//  objects and then the "one". Relations are stored in another table. Another solution might be use JPA with custom thread poll - I need to test
//  which solution is better of performance.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class R2DBCOrderRepository implements OrderRepository {

    OrderCrudRepository orderCrudRepository;
    OrderProductsCrudRepository orderProductsCrudRepository;

    @Override
    @Transactional
    public Mono<Order> save(Order order) {
        return Mono.defer(() ->
            Flux.fromIterable(order.getProducts())
                .flatMap(product -> orderProductsCrudRepository.save(ProductEntity.from(order.getId(), product)))
                .collectList()
                .flatMap(orderProducts -> orderCrudRepository.save(OrderEntity.from(order))
                    .map(orderEntity -> {
                        orderEntity.setProducts(orderProducts.stream().collect(Collectors.toUnmodifiableSet()));
                        return orderEntity;
                    }))
                .map(OrderEntity::toOrder)
        );
    }

    @Override
    public Mono<Order> findByPaymentId(String paymentId) {
        return Mono.defer(() ->
            orderCrudRepository.findByPaymentId(paymentId)
                .flatMap(orderEntity ->
                    orderProductsCrudRepository.findAllByOrderId(orderEntity.getId())
                        .collectList()
                        .flatMap(orderProducts -> {
                            orderEntity.setProducts(orderProducts.stream().collect(Collectors.toUnmodifiableSet()));
                            return Mono.just(orderEntity);
                        })
                )
                .map(OrderEntity::toOrder)
        );
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
class ProductEntity {

    @Id
    private UUID id;
    @NotNull
    @Column(value = "order_id")
    private UUID orderId;
    @Version
    private Long version;

    static ProductEntity from(UUID orderId, Product product) {
        return new ProductEntity(product.id(), orderId, product.version());
    }

    Product toProduct() {
        return new Product(id, version);
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"order\"")
class OrderEntity {

    @Id
    private UUID id;
    @Column(value = "user_id")
    private String userId;
    @NotNull
    private BigDecimal total;
    @NotNull
    private String status;
    @Column(value = "payment_id")
    private String paymentId;
    @Version
    private Long version;
    @Transient
    private Set<ProductEntity> products;

    private static OrderStatus getStatusFor(Order order) {
        return switch (order) {
            case PendingOrder ignored -> OrderStatus.PENDING;
            case SucceededOrder ignored -> OrderStatus.SUCCEEDED;
            case FailedOrder ignored -> OrderStatus.FAILED;
        };
    }

    static OrderEntity from(Order order) {
        return new OrderEntity(
            order.getId(),
            order.getUserId(),
            order.getTotal(),
            getStatusFor(order).name(),
            order.getPaymentId(),
            order.getVersion(),
            Set.of()
        );
    }

    Order toOrder() {
        val productsCollection = products.stream()
            .map(ProductEntity::toProduct)
            .collect(Collectors.toUnmodifiableSet());
        val pendingOrder = PendingOrder.from(id, userId, paymentId, total, productsCollection, version);
        return switch (OrderStatus.valueOf(status)) {
            case PENDING -> pendingOrder;
            case SUCCEEDED -> new SucceededOrder(pendingOrder);
            case FAILED -> new FailedOrder(pendingOrder);
        };
    }

    enum OrderStatus {
        PENDING,
        SUCCEEDED,
        FAILED
    }

}
