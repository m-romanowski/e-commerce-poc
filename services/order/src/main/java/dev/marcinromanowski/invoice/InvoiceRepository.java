package dev.marcinromanowski.invoice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
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
@Table(name = "invoice")
class InvoiceEntity {

    @Id
    private UUID id;
    @NotNull
    @Column(value = "order_id")
    private UUID orderId;
    @NotNull
    @Column(value = "created_at")
    private Instant createdAt;
    @Version
    private Long version;

    static InvoiceEntity from(Invoice invoice) {
        return new InvoiceEntity(invoice.getId(), invoice.getOrderId(), invoice.getCreatedAt(), invoice.getVersion());
    }

}
