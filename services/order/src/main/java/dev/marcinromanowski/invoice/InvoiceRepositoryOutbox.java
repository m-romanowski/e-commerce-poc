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
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

interface InvoiceRepositoryOutbox {
    Mono<InvoiceEvent> save(InvoiceEvent command);
}

interface InvoiceCrudRepositoryOutbox extends ReactiveCrudRepository<InvoiceOutboxEntity, UUID> {

}

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class JpaInvoiceRepositoryOutbox implements InvoiceRepositoryOutbox {

    ObjectMapper objectMapper;
    InvoiceCrudRepositoryOutbox invoiceCrudRepositoryOutbox;

    @Override
    public Mono<InvoiceEvent> save(InvoiceEvent command) {
        try {
            val serializedCommand = objectMapper.writeValueAsString(command);
            val outboxEntity = new InvoiceOutboxEntity(command.getId(), serializedCommand, command.getType());
            return invoiceCrudRepositoryOutbox.save(outboxEntity)
                .then(Mono.just(command));
        } catch (JsonProcessingException e) {
            throw new CannotProcessInvoiceException(e);
        }
    }

}

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_invoice_outbox")
class InvoiceOutboxEntity {

    @Id
    UUID id;
    String payload;
    String type;

}
