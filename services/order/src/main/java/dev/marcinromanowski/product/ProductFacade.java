package dev.marcinromanowski.product;

import dev.marcinromanowski.product.exception.CannotValidateProductsException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductFacade {

    private final ProductRepository productRepository;

    public Flux<ValidatedProduct> validateProducts(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Flux.error(new CannotValidateProductsException(ids));
        }

        return productRepository.getProductDetailsByIds(ids);
    }

}
