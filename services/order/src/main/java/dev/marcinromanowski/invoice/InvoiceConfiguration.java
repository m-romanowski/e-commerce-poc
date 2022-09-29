package dev.marcinromanowski.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class InvoiceConfiguration {

    @Bean
    InvoiceRepositoryOutbox invoiceRepositoryOutbox(ObjectMapper objectMapper, InvoiceCrudRepositoryOutbox invoiceCrudRepositoryOutbox) {
        return new JpaInvoiceRepositoryOutbox(objectMapper, invoiceCrudRepositoryOutbox);
    }

    @Bean
    InvoiceService invoiceService(InvoiceRepositoryOutbox invoiceRepositoryOutbox) {
        return new InvoiceService(invoiceRepositoryOutbox);
    }

    @Bean
    InvoiceCommandListener invoiceCommandListener(ObjectMapper objectMapper, InvoiceService invoiceService) {
        return new DummyInvoiceCommandListener(objectMapper, invoiceService);
    }

    @Bean
    InvoiceFacade invoiceFacade(InvoiceService invoiceService) {
        return new InvoiceFacade(invoiceService);
    }

}
