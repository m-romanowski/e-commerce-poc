package dev.marcinromanowski.invoice;

import dev.marcinromanowski.invoice.commands.GenerateInvoiceCommand;
import dev.marcinromanowski.invoice.commands.InvoiceCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class InvoiceService {

    InvoiceRepositoryOutbox invoiceRepositoryOutbox;

    @Transactional
    public Mono<Void> generateInvoice(GenerateInvoiceCommand command) {
        return invoiceRepositoryOutbox.save(command)
                .then();
    }

    void onInvoiceCommand(InvoiceCommand command) {
        // TODO:
        //  1. Generate invoice
        //  2. Save invoice data
        //  3. Send command with prepared data to another service (e.g. communication service) which send email, etc
    }

}
