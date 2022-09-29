package dev.marcinromanowski.order;

import dev.marcinromanowski.invoice.InvoiceFacade;
import dev.marcinromanowski.invoice.dto.InvoiceOrderDto;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class OrderService {

    ProductFacade productFacade;
    OrderRepository orderRepository;
    PaymentService paymentService;
    InvoiceFacade invoiceFacade;

    @Transactional
    public Mono<String> processOrder(OrderDto orderDto) {
        return fetchProductsDetails(orderDto.products())
                .collectList()
                .flatMap(products -> {
                    val pendingOrder = PendingOrder.from(orderDto.userId(), products);
                    val paymentResponse = paymentService.getApprovalLink(pendingOrder);
                    return orderRepository.save(pendingOrder.paymentInitialized(paymentResponse.id()))
                            .then(Mono.just(paymentResponse.approvalLink()));
                });
    }

    private Flux<Product> fetchProductsDetails(Set<OrderDto.ProductDto> products) {
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
                            .map(validatedProduct -> new Product(
                                            validatedProduct.id(),
                                            validatedProduct.name(),
                                            groupedProducts.get(validatedProduct.id()).amount(),
                                            validatedProduct.price()
                                    )
                            );
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
                .flatMap(orderRepository::save)
                .map(this::toInvoiceOrderFrom)
                .flatMap(invoiceFacade::generateInvoiceForOrder);
    }

    Mono<Void> paymentCancel(String paymentId) {
        return orderRepository.findByPaymentId(paymentId)
                .flatMap(order -> {
                    if (order instanceof PendingOrder pendingOrder) {
                        return Mono.just(pendingOrder.failed());
                    }

                    return Mono.error(new OrderInconsistencyStateException(order.getId()));
                })
                .flatMap(orderRepository::save)
                .then();
    }

    private InvoiceOrderDto toInvoiceOrderFrom(Order order) {
        val products = order.getProducts().stream()
                .map(product -> new InvoiceOrderDto.ProductDto(product.getId(), product.getName(), product.getAmount(), product.getPrice()))
                .collect(Collectors.toUnmodifiableSet());
        return new InvoiceOrderDto(order.getId(), order.getUserId(), products);
    }

}
