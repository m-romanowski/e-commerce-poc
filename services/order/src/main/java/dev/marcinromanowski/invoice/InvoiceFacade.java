package dev.marcinromanowski.invoice;

import dev.marcinromanowski.invoice.commands.GenerateInvoiceCommand;
import dev.marcinromanowski.invoice.dto.InvoiceOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class InvoiceFacade {

    private final InvoiceService invoiceService;

    public Mono<Void> generateInvoiceForOrder(InvoiceOrderDto invoiceOrderDto) {
        val command = new GenerateInvoiceCommand(invoiceOrderDto.id());
        return invoiceService.generateInvoice(command);
    }

}
