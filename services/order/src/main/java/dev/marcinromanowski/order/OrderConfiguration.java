package dev.marcinromanowski.order;

import dev.marcinromanowski.invoice.InvoiceFacade;
import dev.marcinromanowski.product.ProductFacade;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OrderConfiguration {

    @Bean
    OrderRepository orderRepository(OrderCrudRepository orderCrudRepository) {
        return new JpaOrderRepository(orderCrudRepository);
    }

    @Bean
    PaymentService paymentService() {
        return new DummyPaymentService();
    }

    @Bean
    OrderFacade orderFacade(ProductFacade productFacade,
                            OrderRepository orderRepository,
                            PaymentService paymentService,
                            InvoiceFacade invoiceFacade) {
        val orderService = new OrderService(productFacade, orderRepository, paymentService, invoiceFacade);
        return new OrderFacade(orderService);
    }

}
