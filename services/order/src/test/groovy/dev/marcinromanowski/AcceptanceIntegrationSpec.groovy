package dev.marcinromanowski

import com.jayway.jsonpath.JsonPath
import dev.marcinromanowski.base.IntegrationSpec
import dev.marcinromanowski.base.MockConsumer
import dev.marcinromanowski.base.MockConsumers
import dev.marcinromanowski.base.MockedProductsService
import dev.marcinromanowski.base.RandomizerFixture
import dev.marcinromanowski.order.dto.OrderDto
import org.apache.http.client.utils.URIBuilder
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import spock.lang.Ignore

import static dev.marcinromanowski.base.PredefinedPollingConditions.WAIT
import static org.mockito.ArgumentMatchers.argThat
import static org.mockito.Mockito.verify

class AcceptanceIntegrationSpec extends IntegrationSpec implements MockedProductsService {

    private String userId
    private MockConsumer orderEventsConsumer, invoiceEventsConsumer

    def setup() {
        userId = RandomizerFixture.randomUserId("user")
        orderEventsConsumer = MockConsumers.registerMockConsumer(ORDER_EVENTS_TOPIC_NAME)
        invoiceEventsConsumer = MockConsumers.registerMockConsumer(INVOICE_EVENTS_TOPIC_NAME)
    }

    // TODO: Test payment cancellation happy path
    @Ignore(value = "FIXME: It's weird endpoint error. It always returns 404 status code")
    def "Orders acceptance test"() {
        given: "mocked products validation response"
            def firstProduct = new OrderDto.ProductDto(UUID.randomUUID(), 1)
            def secondProduct = new OrderDto.ProductDto(UUID.randomUUID(), 2)
            validateProducts([firstProduct.id(), secondProduct.id()] as Set)

        when: "user creates a new order"
            def newOrder = new OrderDto(userId, [firstProduct, secondProduct] as Set) // TODO: userId should be from principals (for logged user). For non-registered users this variable will be nullable
            def response = createNewOrder(newOrder)
        then: "payment redirection url is returned"
            def responseBody = response
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath('$.value').isNotEmpty()
                    .returnResult()
                    .responseBody
            def paymentApprovalLink = JsonPath.parse(new String(responseBody)).read('$.value', String)
            def paymentId = getRequestParamFrom(paymentApprovalLink, "paymentId")
            paymentId != null
        and: "order event is produced"
            WAIT.eventually {
                verify(orderEventsConsumer).consumed(argThat({ String event ->
                    def parsedEvent = JsonPath.parse(event)
                    return parsedEvent.read('$.type') == "OrderCreated"
                            && parsedEvent.read('$.id') != null
                            && parsedEvent.read('$.userId') == userId
                            && parsedEvent.read('$.total') == 3.0
                            && parsedEvent.read('$.products.length()') == 2
                            && parsedEvent.read('$.products', List).containsAll(firstProduct.id().toString(), secondProduct.id().toString())
                }))
            }

        when: "successfully payment event come from external system"
            def paymentSucceededResponse = paymentSucceeded(paymentId)
        then: "order ends with 'SUCCESS' status"
            paymentSucceededResponse
                    .expectStatus().isOk()
        and: "order event is produced"
            WAIT.eventually {
                verify(orderEventsConsumer).consumed(argThat({ String event ->
                    def parsedEvent = JsonPath.parse(event)
                    return parsedEvent.read('$.type') == "OrderSucceeded"
                            && parsedEvent.read('$.paymentId') != null
                }))
            }

        and: "user's invoice is generated"
        and: "user's invoice details are sent as event"
            WAIT.eventually {
                verify(orderEventsConsumer).consumed(argThat({ String event ->
                    def parsedEvent = JsonPath.parse(event)
                    return parsedEvent.read('$.type') == "InvoiceCreated"
                            && parsedEvent.read('$.id') != null
                            && parsedEvent.read('$.orderId') != null
                            && parsedEvent.read('$.createdAt') != null
                }))
            }
    }

    private static String getRequestParamFrom(String uri, String requestParam) {
        return new URIBuilder(uri).getQueryParams().find { it.value == requestParam }
    }

    private static String productAsJson(OrderDto.ProductDto product) {
        return """
            {
                "id": "${product.id()}",
                "amount": ${product.amount()}
            }
        """
    }

    private static String newOrderAsJson(OrderDto order) {
        return """
            {
                "userId": "${order.userId()}",
                "products": [${order.products().collect { productAsJson(it) }.join(",")}]
            }
        """
    }

    private static String paymentDetailsAsJson(String paymentId) {
        return """
            {
                "paymentId": "$paymentId"
            }
        """
    }

    private WebTestClient.ResponseSpec createNewOrder(OrderDto order) {
        return webTestClient
                .post()
                .uri("/api/v1/checkout")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newOrderAsJson(order)))
                .exchange()
    }

    private WebTestClient.ResponseSpec paymentSucceeded(String paymentId) {
        return webTestClient
                .post()
                .uri("/api/v1/checkout/success")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentDetailsAsJson(paymentId)))
                .exchange()
    }

    private WebTestClient.ResponseSpec paymentCancelled(String paymentId) {
        return webTestClient
                .post()
                .uri("/api/v1/checkout/cancel")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentDetailsAsJson(paymentId)))
                .exchange()
    }

}
