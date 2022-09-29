package dev.marcinromanowski.order;

import lombok.*;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.persistence.*;
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

@RequiredArgsConstructor
class JpaOrderRepository implements OrderRepository {

    private final OrderCrudRepository orderCrudRepository;

    @Override
    public Mono<Order> save(Order order) {
        return orderCrudRepository.save(OrderEntity.from(order))
                .then(Mono.just(order));
    }

    @Override
    public Mono<Order> findByPaymentId(String paymentId) {
        return orderCrudRepository.findByPaymentId(paymentId)
                .map(OrderEntity::toOrder);
    }

}

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_product")
class ProductEntity {

    @Id
    UUID id;
    String name;
    int amount;
    BigDecimal price;

    static ProductEntity from(Product product) {
        return new ProductEntity(product.getId(), product.getName(), product.getAmount(), product.getPrice());
    }

    Product toProduct() {
        return new Product(id, name, amount, price);
    }

}

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order")
class OrderEntity {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "user_id")
    private String userId;
    private BigDecimal total;
    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;
    @Column(name = "payment_id")
    private String paymentId;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @Column(name = "product_id")
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
        val productsEntities = order.getProducts()
                .stream()
                .map(ProductEntity::from)
                .collect(Collectors.toUnmodifiableSet());
        return new OrderEntity(
                order.getId(),
                order.getUserId(),
                order.getTotal(),
                getStatusFor(order),
                getPaymentIdFor(order),
                productsEntities
        );
    }

    Order toOrder() {
        val productsCollection = products.stream()
                .map(ProductEntity::toProduct)
                .collect(Collectors.toUnmodifiableSet());
        val pendingOrder = new PendingOrder(id, userId, paymentId, total, productsCollection);
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
