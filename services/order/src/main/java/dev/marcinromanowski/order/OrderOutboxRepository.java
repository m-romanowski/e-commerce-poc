package dev.marcinromanowski.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.order.events.OrderEvent;
import dev.marcinromanowski.order.exception.CannotProcessOrderException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.UUID;

interface OrderOutboxRepository {
    Mono<OrderEvent> save(OrderEvent event);
}

interface OrderCrudOutboxRepository extends ReactiveCrudRepository<OrderOutboxEntity, UUID> {

}

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class R2DBCOrderOutboxRepository implements OrderOutboxRepository {

    ObjectMapper objectMapper;
    OrderCrudOutboxRepository invoiceCrudRepositoryOutbox;

    @Override
    public Mono<OrderEvent> save(OrderEvent event) {
        try {
            val serializedCommand = objectMapper.writeValueAsString(event);
            val outboxEntity = new OrderOutboxEntity(null, event.getId(), serializedCommand, event.getType());
            return invoiceCrudRepositoryOutbox.save(outboxEntity)
                .then(Mono.just(event));
        } catch (JsonProcessingException e) {
            throw new CannotProcessOrderException(e);
        }
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_outbox")
class OrderOutboxEntity {

    @Id
    private String id;
    @NotNull
    private UUID key;
    @NotNull
    private String payload;
    @NotNull
    private String type;

}
