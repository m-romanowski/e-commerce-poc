package dev.marcinromanowski.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.invoice.InvoiceFacade;
import dev.marcinromanowski.product.ProductFacade;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
class OrderConfiguration {

    @Bean
    OrderRepository orderRepository(OrderCrudRepository orderCrudRepository, OrderProductsCrudRepository orderProductsCrudRepository) {
        return new R2DBCOrderRepository(orderCrudRepository, orderProductsCrudRepository);
    }

    @Bean
    OrderOutboxRepository orderRepositoryOutbox(ObjectMapper objectMapper, OrderCrudOutboxRepository orderCrudOutboxRepository) {
        return new R2DBCOrderOutboxRepository(objectMapper, orderCrudOutboxRepository);
    }

    @Bean
    PaymentService paymentService() {
        return new DummyPaymentService();
    }

    @Bean
    Supplier<UUID> idSupplier() {
        return UUID::randomUUID;
    }

    @Bean
    OrderFacade orderFacade(Supplier<UUID> idSupplier,
                            ProductFacade productFacade,
                            OrderRepository orderRepository,
                            OrderOutboxRepository orderOutboxRepository,
                            PaymentService paymentService,
                            InvoiceFacade invoiceFacade) {
        val orderService = new OrderService(idSupplier, productFacade, orderRepository, orderOutboxRepository, paymentService, invoiceFacade);
        return new OrderFacade(orderService);
    }

}
