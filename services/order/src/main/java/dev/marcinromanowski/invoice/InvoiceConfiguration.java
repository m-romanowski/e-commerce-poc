package dev.marcinromanowski.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class InvoiceConfiguration {

    @Bean
    InvoiceRepository invoiceRepository(InvoiceCrudRepository invoiceCrudRepository) {
        return new R2DBCInvoiceRepository(invoiceCrudRepository);
    }

    @Bean
    InvoiceOutboxRepository invoiceRepositoryOutbox(ObjectMapper objectMapper, InvoiceCrudOutboxRepository invoiceCrudOutboxRepository) {
        return new R2DBCInvoiceOutboxRepository(objectMapper, invoiceCrudOutboxRepository);
    }

    @Bean
    InvoiceFacade invoiceFacade(Clock clock, InvoiceRepository invoiceRepository, InvoiceOutboxRepository invoiceOutboxRepository) {
        val invoiceService = new InvoiceService(clock, invoiceRepository, invoiceOutboxRepository);
        return new InvoiceFacade(invoiceService);
    }

}
