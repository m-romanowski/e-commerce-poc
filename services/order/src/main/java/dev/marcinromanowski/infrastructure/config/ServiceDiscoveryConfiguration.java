package dev.marcinromanowski.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.common.Profiles;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableEurekaClient
@EnableDiscoveryClient
@Profile(value = Profiles.EXCEPT_INTEGRATION)
class ServiceDiscoveryConfiguration {

    @Bean
    @LoadBalanced
    WebClient.Builder webclientBuilder(ObjectMapper objectMapper) {
        return WebClient.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
            });
    }

}
