package dev.marcinromanowski.invoice

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcinromanowski.base.ClockFixture
import dev.marcinromanowski.invoice.dto.OrderDetailsDto
import dev.marcinromanowski.invoice.events.InvoiceCreated
import dev.marcinromanowski.invoice.events.InvoiceEvent
import dev.marcinromanowski.invoice.exception.CannotProcessInvoiceException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class InvoiceFacadeSpec extends Specification implements ClockFixture {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(10)

    private InvoiceFacade invoiceFacade
    private InvoiceRepository invoiceRepository
    private InvoiceOutboxRepository invoiceRepositoryOutbox

    def setup() {
        def configuration = new InvoiceConfiguration()
        invoiceRepository = Mock(InvoiceRepository)
        invoiceRepositoryOutbox = Mock(InvoiceOutboxRepository)
        invoiceFacade = configuration.invoiceFacade(clock(), invoiceRepository, invoiceRepositoryOutbox)
    }

    def "Invoice should be generated on request"() {
        given:
            def orderDetails = new OrderDetailsDto(UUID.randomUUID(), "user")
        and:
            1 * invoiceRepository.save({ Invoice invoice ->
                invoice.id != null
                        && invoice.userId == orderDetails.userId()
                        && invoice.orderId == orderDetails.id()
                        && invoice.createdAt == clock().instant()
            }) >> Mono.just(Invoice.create(orderDetails.id(), orderDetails.userId(), clock().instant()))
        and:
            1 * invoiceRepositoryOutbox.save({ InvoiceCreated event ->
                event.id != null
                        && event.orderId == orderDetails.id()
                        && event.createdAt == clock().instant()
            }) >> Mono.just(new InvoiceCreated(UUID.randomUUID(), orderDetails.id(), clock().instant()))

        expect:
            StepVerifier.create(invoiceFacade.createInvoiceFor(orderDetails))
                    .expectSubscription()
                    .expectComplete()
                    .verify(BLOCK_TIMEOUT)
    }

    @SuppressWarnings('GroovyAccessibility')
    def "An exception should be thrown on unexpected serialization error"() {
        given:
            def configuration = new InvoiceConfiguration()
            def invoiceRepository = Mock(InvoiceRepository) {
                save(_ as Invoice) >> Mono.just(Invoice.create(UUID.randomUUID(), "user", clock().instant()))
            }
            def objectMapper = Mock(ObjectMapper) {
                writeValueAsString(_ as InvoiceEvent) >> { throw new JsonProcessingException("Mocked exception") }
            }
            def invoiceCrudRepositoryOutbox = Mock(InvoiceCrudOutboxRepository)
            def invoiceRepositoryOutbox = new R2DBCInvoiceOutboxRepository(objectMapper, invoiceCrudRepositoryOutbox)
            def invoiceFacade = configuration.invoiceFacade(clock(), invoiceRepository, invoiceRepositoryOutbox)
        and:
            def orderDetails = new OrderDetailsDto(UUID.randomUUID(), "user")

        when:
            def verifier = StepVerifier.create(invoiceFacade.createInvoiceFor(orderDetails))
        then:
            verifier
                    .expectSubscription()
                    .expectError(CannotProcessInvoiceException)
                    .verify(BLOCK_TIMEOUT)
    }

}
