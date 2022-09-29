package dev.marcinromanowski.product;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class ProductConfiguration {

    @Bean
    ProductRepository productRepository(WebClient.Builder builder, ProductProperties productProperties) {
        val detailService = productProperties.getDetailsService();
        return new HttpProductRepository(builder, detailService.getBaseUrl(), detailService.getRequestTimeout());
    }

    @Bean
    ProductFacade productFacade(ProductRepository productRepository) {
        return new ProductFacade(productRepository);
    }

}
