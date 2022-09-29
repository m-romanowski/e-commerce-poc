package dev.marcinromanowski.invoice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.invoice.commands.InvoiceCommand;
import dev.marcinromanowski.invoice.exception.CannotProcessInvoiceCommandException;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

interface InvoiceRepositoryOutbox {
    Mono<InvoiceCommand> save(InvoiceCommand command);
}

interface InvoiceCrudRepositoryOutbox extends ReactiveCrudRepository<InvoiceOutboxEntity, UUID> {

}

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class JpaInvoiceRepositoryOutbox implements InvoiceRepositoryOutbox {

    ObjectMapper objectMapper;
    InvoiceCrudRepositoryOutbox invoiceCrudRepositoryOutbox;

    @Override
    public Mono<InvoiceCommand> save(InvoiceCommand command) {
        try {
            val serializedCommand = objectMapper.writeValueAsString(command);
            val outboxEntity = new InvoiceOutboxEntity(command.getId(), serializedCommand, command.getType());
            return invoiceCrudRepositoryOutbox.save(outboxEntity)
                    .then(Mono.just(command));
        } catch (JsonProcessingException e) {
            throw new CannotProcessInvoiceCommandException(e);
        }
    }

}

// TODO: It might be processed by e.g. Kafka Connect to generate command based on outbox payload
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoice_outbox")
class InvoiceOutboxEntity {

    @Id
    UUID id;
    String payload;
    String type;

}
