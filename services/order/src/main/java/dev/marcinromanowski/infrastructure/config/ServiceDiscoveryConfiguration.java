package dev.marcinromanowski.infrastructure.config;

import dev.marcinromanowski.common.Profiles;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableEurekaClient
@EnableDiscoveryClient
@Profile(value = Profiles.EXCEPT_INTEGRATION)
class ServiceDiscoveryConfiguration {

    @Bean
    @LoadBalanced
    WebClient.Builder webclientBuilder() {
        return WebClient.builder();
    }

}
