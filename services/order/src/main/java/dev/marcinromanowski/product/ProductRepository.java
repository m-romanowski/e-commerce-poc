package dev.marcinromanowski.product;

import dev.marcinromanowski.product.exception.CannotValidateProductsException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

interface ProductRepository {
    Flux<ValidatedProduct> getProductDetailsByIds(Set<UUID> ids);
}

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class HttpProductRepository implements ProductRepository {

    private static final String QUERY_PARAM_NAME = "id";

    WebClient webClient;
    Duration timeout;

    HttpProductRepository(WebClient.Builder builder, String baseUrl, Duration timeout) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeout = timeout;
    }

    @Override
    public Flux<ValidatedProduct> getProductDetailsByIds(Set<UUID> ids) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam(QUERY_PARAM_NAME, ids)
                .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux(clientResponse -> {
                val statusCode = clientResponse.statusCode();
                if (statusCode != HttpStatus.OK) {
                    log.error("Cannot validate products: {}. Got {} status code", ids, statusCode);
                    return Flux.error(new CannotValidateProductsException(ids));
                }

                return clientResponse.bodyToMono(new ParameterizedTypeReference<List<ProductDetailsResponse>>() {})
                    .flatMapMany(Flux::fromIterable);
            })
            .timeout(timeout)
            .onErrorMap(Exception.class, e -> new CannotValidateProductsException(ids, e))
            .map(ProductDetailsResponse::toProduct);
    }

    private record ProductDetailsResponse(UUID id, String name, BigDecimal price) {

        ValidatedProduct toProduct() {
            return new ValidatedProduct(id, name, price);
        }

    }

}
