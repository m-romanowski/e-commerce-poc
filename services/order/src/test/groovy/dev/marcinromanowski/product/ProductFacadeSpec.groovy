package dev.marcinromanowski.product

import dev.marcinromanowski.product.exception.CannotValidateProductsException
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class ProductFacadeSpec extends Specification {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(10)

    private ProductFacade productFacade
    private ProductRepository productRepository

    def setup() {
        productRepository = Mock(ProductRepository)
        productFacade = new ProductFacade(productRepository)
    }

    def "Products details should be returned for available data"() {
        given: "existing product details in repository"
            def validatedProduct = new ValidatedProduct(UUID.randomUUID(), "Product name", 1.0)
            productRepository.getProductDetailsByIds(_ as Set<UUID>) >> Flux.just(validatedProduct)

        when: "we try to validate product id"
            def verifier = StepVerifier.create(productFacade.validateProducts([validatedProduct.id()] as Set<UUID>))
        then: "no exception is thrown"
        and: "result is returned"
            verifier
                    .expectSubscription()
                    .assertNext {
                        assert it.id() == validatedProduct.id()
                                && it.name() == validatedProduct.name()
                                && it.price() == validatedProduct.price()
                    }
                    .verifyComplete()
    }

    def "An exception should be thrown for empty ids collection"() {
        when: "we try to validate empty collection with ids"
            def verifier = StepVerifier.create(productFacade.validateProducts([] as Set<UUID>))
        then: "exception is thrown"
            verifier
                    .expectSubscription()
                    .expectError(CannotValidateProductsException)
                    .verify(BLOCK_TIMEOUT)
    }

}
