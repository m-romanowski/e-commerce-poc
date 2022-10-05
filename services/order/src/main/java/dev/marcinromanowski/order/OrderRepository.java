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
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

interface OrderProductsCrudRepository extends ReactiveCrudRepository<OrderProductsEntity, UUID> {
    Flux<OrderProductsEntity> findAllByOrderId(UUID orderId);
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
    public Mono<Order> save(Order order) {
        return Mono.defer(() ->
            Flux.fromIterable(order.getProducts())
                .flatMap(product -> orderProductsCrudRepository.save(new OrderProductsEntity(UUID.randomUUID(), order.getId(), product.id())))
                .collectList()
                .flatMap(ignored -> orderCrudRepository.save(OrderEntity.from(order)))
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
                            val productsIds = orderProducts.stream()
                                .map(ProductEntity::from)
                                .collect(Collectors.toUnmodifiableSet());
                            orderEntity.setProducts(productsIds);
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
    UUID id;

    static ProductEntity from(OrderProductsEntity orderProducts) {
        return new ProductEntity(orderProducts.getProductId());
    }

    Product toProduct() {
        return new Product(id);
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order")
class OrderEntity {

    @Id
    private UUID id;
    private String userId;
    private BigDecimal total;
    private OrderStatus status;
    private String paymentId;
    @Transient
    private Set<ProductEntity> products;

    private static OrderStatus getStatusFor(Order order) {
        return switch (order) {
            case PendingOrder ignored -> OrderStatus.PENDING;
            case SucceededOrder ignored -> OrderStatus.SUCCEEDED;
            case FailedOrder ignored -> OrderStatus.FAILED;
        };
    }

    private static String getPaymentIdFor(Order order) {
        if (order instanceof SucceededOrder succeededOrder) {
            return succeededOrder.getPaymentId();
        }

        return null;
    }

    static OrderEntity from(Order order) {
        return new OrderEntity(
            order.getId(),
            order.getUserId(),
            order.getTotal(),
            getStatusFor(order),
            getPaymentIdFor(order),
            Set.of()
        );
    }

    Order toOrder() {
        val productsCollection = products.stream()
            .map(ProductEntity::toProduct)
            .collect(Collectors.toUnmodifiableSet());
        val pendingOrder = PendingOrder.from(id, userId, paymentId, total, productsCollection);
        return switch (status) {
            case PENDING -> pendingOrder;
            case SUCCEEDED -> new SucceededOrder(pendingOrder, paymentId);
            case FAILED -> new FailedOrder(pendingOrder);
        };
    }

    enum OrderStatus {
        PENDING,
        SUCCEEDED,
        FAILED
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_product")
class OrderProductsEntity {

    @Id
    UUID id;
    UUID orderId;
    UUID productId;

}
