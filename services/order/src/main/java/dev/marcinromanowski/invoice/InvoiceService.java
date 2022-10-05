package dev.marcinromanowski.invoice;

import dev.marcinromanowski.invoice.dto.OrderDetailsDto;
import dev.marcinromanowski.invoice.events.InvoiceCreated;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Clock;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class InvoiceService {

    Clock clock;
    InvoiceRepository invoiceRepository;
    InvoiceOutboxRepository invoiceOutboxRepository;

    @Transactional
    public Mono<Void> createInvoice(OrderDetailsDto orderDetails) {
        return invoiceRepository.save(Invoice.create(orderDetails.id(), orderDetails.userId(), clock.instant()))
            .flatMap(invoice -> Mono.just(new InvoiceCreated(invoice.getId(), orderDetails.id(), clock.instant())))
            .flatMap(invoiceOutboxRepository::save)
            .then();
    }

}
