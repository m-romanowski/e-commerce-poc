package dev.marcinromanowski.order

import dev.marcinromanowski.base.RandomizerFixture
import dev.marcinromanowski.invoice.InvoiceFacade
import dev.marcinromanowski.invoice.dto.OrderDetailsDto
import dev.marcinromanowski.order.dto.OrderDto
import dev.marcinromanowski.order.dto.PaymentDto
import dev.marcinromanowski.order.events.OrderCreated
import dev.marcinromanowski.order.events.OrderFailed
import dev.marcinromanowski.order.events.OrderSucceeded
import dev.marcinromanowski.order.exception.IllegalOrderStateException
import dev.marcinromanowski.order.exception.OrderInconsistencyStateException
import dev.marcinromanowski.product.ProductFacade
import dev.marcinromanowski.product.ValidatedProduct
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class OrderFacadeSpec extends Specification {

    private static final Duration BLOCK_TIME = Duration.ofSeconds(10)
    private static final UUID ORDER_ID = UUID.randomUUID()
    private static final String PAYMENT_ID = UUID.randomUUID().toString()
    private static final String APPROVAL_LINK = UUID.randomUUID().toString()
    private static final Long VERSION = 1L

    private OrderFacade orderFacade
    private OrderRepository orderRepository
    private OrderOutboxRepository orderOutboxRepository
    private ProductFacade productFacade
    private InvoiceFacade invoiceFacade
    private String userId

    def setup() {
        def configuration = new OrderConfiguration()
        def paymentService = Mock(PaymentService) {
            getApprovalLink(_ as Order) >> new PaymentResponse(PAYMENT_ID, APPROVAL_LINK)
        }
        orderRepository = Mock(OrderRepository)
        orderOutboxRepository = Mock(OrderOutboxRepository)
        productFacade = Mock(ProductFacade)
        invoiceFacade = Mock(InvoiceFacade)
        orderFacade = configuration.orderFacade(() -> ORDER_ID, productFacade, orderRepository, orderOutboxRepository, paymentService, invoiceFacade)
        userId = RandomizerFixture.randomUserId("user")
    }

    def "A new order should be registered as 'PENDING' status"() {
        given:
            def productId = UUID.randomUUID()
            def newOrder = new OrderDto(userId, [new OrderDto.ProductDto(productId, 2)] as Set)

        when:
            1 * productFacade.validateProducts([productId] as Set) >> Flux.just(new ValidatedProduct(productId, "Product name", 2.0))
            1 * orderRepository.save({ PendingOrder pendingOrder ->
                pendingOrder.id == ORDER_ID
                        && pendingOrder.paymentId == PAYMENT_ID
                        && pendingOrder.total == 4.0
                        && pendingOrder.products.size() == 1
                        && pendingOrder.products[0].id() == productId
            }) >> Mono.just(pendingOrderFor(ORDER_ID, userId, PAYMENT_ID, 4.0, [productFor(productId)] as Set))
            1 * orderOutboxRepository.save({ OrderCreated orderCreated ->
                orderCreated.id == ORDER_ID
                        && orderCreated.userId == userId
                        && orderCreated.total == 4.0
                        && orderCreated.products.size() == 1
                        && orderCreated.products[0] == productId
            }) >> Mono.just(new OrderCreated(ORDER_ID, userId, 4.0, [productId] as Set))
            def verifier = StepVerifier.create(orderFacade.newOrder(newOrder))
        then:
            verifier
                    .expectSubscription()
                    .assertNext {
                        assert it.value() == APPROVAL_LINK
                    }
                    .verifyComplete()
    }

    def "The order status should be changed after successfully payment"() {
        when:
            def pendingOrder = pendingOrderFor(ORDER_ID, userId, PAYMENT_ID, 4.0, [productFor(UUID.randomUUID())] as Set)
            1 * orderRepository.findByPaymentId(PAYMENT_ID) >> Mono.just(pendingOrder)
            1 * orderRepository.save(_ as SucceededOrder) >> Mono.just(new SucceededOrder(pendingOrder))
            1 * orderOutboxRepository.save({ OrderSucceeded event -> event.id && event.paymentId == PAYMENT_ID }) >> Mono.just(new OrderSucceeded(ORDER_ID, PAYMENT_ID))
            1 * invoiceFacade.createInvoiceFor({ OrderDetailsDto orderDetails ->
                orderDetails.id() == ORDER_ID
                        && orderDetails.userId() == userId
            }) >> Mono.empty()
        then:
            StepVerifier.create(orderFacade.paymentSucceeded(new PaymentDto(PAYMENT_ID)))
                    .expectSubscription()
                    .verifyComplete()
    }

    def "The order status should be changed after payment failure or cancellation"() {
        when:
            def pendingOrder = pendingOrderFor(ORDER_ID, userId, PAYMENT_ID, 4.0, [productFor(UUID.randomUUID())] as Set)
            1 * orderRepository.findByPaymentId(PAYMENT_ID) >> Mono.just(pendingOrder)
            1 * orderRepository.save({ Order order -> order instanceof FailedOrder }) >> Mono.just(new FailedOrder(pendingOrder))
            1 * orderOutboxRepository.save({ OrderFailed event -> event.id }) >> Mono.just(new OrderFailed(ORDER_ID))
            0 * invoiceFacade.createInvoiceFor(_ as OrderDetailsDto) >> Mono.empty()
        then:
            StepVerifier.create(orderFacade.paymentCanceled(new PaymentDto(PAYMENT_ID)))
                    .expectSubscription()
                    .verifyComplete()
    }

    def "An exception should be thrown if we try mark order as succeeded twice"() {
        when:
            1 * orderRepository.findByPaymentId(PAYMENT_ID) >> Mono.just(orderState)
        then:
            StepVerifier.create(orderFacade.paymentSucceeded(new PaymentDto(PAYMENT_ID)))
                    .expectSubscription()
                    .expectError(OrderInconsistencyStateException)
                    .verify(BLOCK_TIME)

        where:
            orderState << [
                    new SucceededOrder(pendingOrderFor(ORDER_ID, "user", PAYMENT_ID, 1.0, [productFor(UUID.randomUUID())] as Set)),
                    new FailedOrder(pendingOrderFor(ORDER_ID, "user", PAYMENT_ID, 1.0, [productFor(UUID.randomUUID())] as Set))
            ]
    }

    def "An exception should be thrown if we try mark order as cancelled twice"() {
        when:
            1 * orderRepository.findByPaymentId(PAYMENT_ID) >> Mono.just(orderState)
        then:
            StepVerifier.create(orderFacade.paymentCanceled(new PaymentDto(PAYMENT_ID)))
                    .expectSubscription()
                    .expectError(OrderInconsistencyStateException)
                    .verify(BLOCK_TIME)

        where:
            orderState << [
                    new SucceededOrder(pendingOrderFor(ORDER_ID, "user", PAYMENT_ID, 1.0, [productFor(UUID.randomUUID())] as Set)),
                    new FailedOrder(pendingOrderFor(ORDER_ID, "user", PAYMENT_ID, 1.0, [productFor(UUID.randomUUID())] as Set))
            ]
    }

    def "An exception should be thrown if order is empty"() {
        expect:
            StepVerifier.create(orderFacade.newOrder(new OrderDto(userId, [] as Set)))
                    .expectSubscription()
                    .expectError(IllegalOrderStateException)
                    .verify(BLOCK_TIME)
    }

    def "An exception should be thrown if cannot fetch products details"() {
        given:
            def productId = UUID.randomUUID()
            1 * productFacade.validateProducts([productId] as Set) >> Mono.error(new RuntimeException("Mocked exception"))

        expect:
            StepVerifier.create(orderFacade.newOrder(new OrderDto(userId, [new OrderDto.ProductDto(productId, 2)] as Set)))
                    .expectSubscription()
                    .expectError(IllegalOrderStateException)
                    .verify(BLOCK_TIME)
    }

    private static Product productFor(UUID id) {
        return new Product(id, VERSION)
    }

    private static PendingOrder pendingOrderFor(UUID orderId, String userId, String paymentId, BigDecimal total, Set<Product> products) {
        return PendingOrder.from(orderId, userId, paymentId, total, products, VERSION)
    }

}
