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
        return new JpaInvoiceRepository(invoiceCrudRepository);
    }

    @Bean
    InvoiceRepositoryOutbox invoiceRepositoryOutbox(ObjectMapper objectMapper, InvoiceCrudRepositoryOutbox invoiceCrudRepositoryOutbox) {
        return new JpaInvoiceRepositoryOutbox(objectMapper, invoiceCrudRepositoryOutbox);
    }

    @Bean
    InvoiceFacade invoiceFacade(Clock clock, InvoiceRepository invoiceRepository, InvoiceRepositoryOutbox invoiceRepositoryOutbox) {
        val invoiceService = new InvoiceService(clock, invoiceRepository, invoiceRepositoryOutbox);
        return new InvoiceFacade(invoiceService);
    }

}
