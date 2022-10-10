package dev.marcinromanowski.invoice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.invoice.events.InvoiceEvent;
import dev.marcinromanowski.invoice.exception.CannotProcessInvoiceException;
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

interface InvoiceOutboxRepository {
    Mono<InvoiceEvent> save(InvoiceEvent event);
}

interface InvoiceCrudOutboxRepository extends ReactiveCrudRepository<InvoiceOutboxEntity, UUID> {

}

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class R2DBCInvoiceOutboxRepository implements InvoiceOutboxRepository {

    ObjectMapper objectMapper;
    InvoiceCrudOutboxRepository invoiceCrudOutboxRepository;

    @Override
    public Mono<InvoiceEvent> save(InvoiceEvent event) {
        try {
            val serializedCommand = objectMapper.writeValueAsString(event);
            val outboxEntity = new InvoiceOutboxEntity(null, event.getId(), serializedCommand, event.getType());
            return invoiceCrudOutboxRepository.save(outboxEntity)
                .then(Mono.just(event));
        } catch (JsonProcessingException e) {
            throw new CannotProcessInvoiceException(e);
        }
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoice_outbox")
class InvoiceOutboxEntity {

    @Id
    private String id;
    @NotNull
    private UUID key;
    @NotNull
    private String payload;
    @NotNull
    private String type;

}
