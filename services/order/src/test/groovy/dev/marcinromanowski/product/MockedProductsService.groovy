package dev.marcinromanowski.product

import dev.marcinromanowski.base.MockWebServerSupplier
import dev.marcinromanowski.base.RandomizerFixture
import okhttp3.mockwebserver.MockResponse

import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

trait MockedProductsService implements MockWebServerSupplier {

    private static String productAsJson(UUID productId) {
        return """
            {
                "id": "$productId,
                "name": "${UUID.randomUUID()}",
                "price": "${RandomizerFixture.randomBigDecimal()}"
            }
        """
    }

    private static MockResponse successResponse(Set<UUID> products) {
        return new MockResponse()
                .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(
                        """
                        [
                            ${products.each { productAsJson(it) }.join(",")}
                        ]
                        """
                )
    }

    def validateProducts(Set<UUID> ids) {
        return mockWebServer.setDispatcher {
            if (it.path.matches("/product") && it.method == GET.name()) {
                return successResponse(ids)
            }

            throw new RuntimeException("No matches found for $ids")
        }
    }

}
