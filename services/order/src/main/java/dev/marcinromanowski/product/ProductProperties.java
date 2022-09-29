package dev.marcinromanowski.product;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "product")
class ProductProperties {

    @NotNull
    private DetailsService detailsService;

    @Data
    static class DetailsService {

        @NotBlank
        private String baseUrl;
        @NotNull
        private Duration requestTimeout;

    }

}
