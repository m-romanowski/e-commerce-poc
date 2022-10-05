package dev.marcinromanowski.invoice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

interface InvoiceRepository {
    Mono<Invoice> save(Invoice invoice);
}

interface InvoiceCrudRepository extends ReactiveCrudRepository<InvoiceEntity, UUID> {

}

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class R2DBCInvoiceRepository implements InvoiceRepository {

    InvoiceCrudRepository invoiceCrudRepository;

    @Override
    public Mono<Invoice> save(Invoice invoice) {
        return invoiceCrudRepository.save(InvoiceEntity.from(invoice))
            .then(Mono.just(invoice));
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_invoice")
class InvoiceEntity {

    @Id
    UUID id;
    UUID orderId;
    Instant createdAt;

    static InvoiceEntity from(Invoice invoice) {
        return new InvoiceEntity(invoice.getId(), invoice.getOrderId(), invoice.getCreatedAt());
    }

}
