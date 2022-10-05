package dev.marcinromanowski.order;

import dev.marcinromanowski.invoice.InvoiceFacade;
import dev.marcinromanowski.invoice.dto.OrderDetailsDto;
import dev.marcinromanowski.order.dto.OrderDto;
import dev.marcinromanowski.order.exception.IllegalOrderStateException;
import dev.marcinromanowski.order.exception.OrderInconsistencyStateException;
import dev.marcinromanowski.product.ProductFacade;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class OrderService {

    Supplier<UUID> idSupplier;
    ProductFacade productFacade;
    OrderRepository orderRepository;
    OrderOutboxRepository orderOutboxRepository;
    PaymentService paymentService;
    InvoiceFacade invoiceFacade;

    @Transactional
    public Mono<String> processOrder(OrderDto orderDto) {
        return fetchProductsDetails(orderDto.products())
            .collectList()
            .flatMap(products -> {
                val pendingOrder = PendingOrder.create(idSupplier, orderDto.userId(), products);
                val paymentResponse = paymentService.getApprovalLink(pendingOrder);
                return orderRepository.save(pendingOrder.withPendingPayment(paymentResponse.id()))
                    .flatMap(order -> orderOutboxRepository.save(order.toEvent()))
                    .flatMap(ignored -> Mono.just(paymentResponse.approvalLink()));
            });
    }

    private Flux<OrderProductDetails> fetchProductsDetails(Set<OrderDto.ProductDto> products) {
        if (products.isEmpty()) {
            return Flux.error(new IllegalOrderStateException());
        }

        return Flux.fromIterable(products)
            .collectList()
            .flatMapMany(productsToValidation -> {
                val groupedProducts = productsToValidation
                    .stream()
                    .collect(Collectors.toMap(OrderDto.ProductDto::id, Function.identity()));
                return productFacade.validateProducts(groupedProducts.keySet())
                    .map(validatedProduct -> new OrderProductDetails(validatedProduct, groupedProducts.get(validatedProduct.id()).amount()));
            })
            .onErrorMap(e -> new IllegalOrderStateException());
    }

    @Transactional
    public Mono<Void> paymentSucceeded(String paymentId) {
        return orderRepository.findByPaymentId(paymentId)
            .flatMap(order -> {
                if (order instanceof PendingOrder pendingOrder) {
                    return Mono.just(pendingOrder.succeeded(paymentId));
                }

                return Mono.error(new OrderInconsistencyStateException(order.getId()));
            })
            .flatMap(order ->
                orderRepository.save(order)
                    .flatMap(savedOrder -> orderOutboxRepository.save(savedOrder.toEvent()).then(Mono.just(savedOrder)))
            )
            .map(order -> new OrderDetailsDto(order.getId(), order.getUserId()))
            .flatMap(invoiceFacade::createInvoiceFor);
    }

    @Transactional
    public Mono<Void> paymentCanceled(String paymentId) {
        return orderRepository.findByPaymentId(paymentId)
            .flatMap(order -> {
                if (order instanceof PendingOrder pendingOrder) {
                    return Mono.just(pendingOrder.failed());
                }

                return Mono.error(new OrderInconsistencyStateException(order.getId()));
            })
            .flatMap(orderRepository::save)
            .flatMap(order -> orderOutboxRepository.save(order.toEvent()))
            .then();
    }

}
