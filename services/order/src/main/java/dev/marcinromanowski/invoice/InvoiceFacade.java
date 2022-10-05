package dev.marcinromanowski.invoice;

import dev.marcinromanowski.invoice.dto.OrderDetailsDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class InvoiceFacade {

    private final InvoiceService invoiceService;

    public Mono<Void> createInvoiceFor(OrderDetailsDto orderDetails) {
        return invoiceService.createInvoice(orderDetails);
    }

}
