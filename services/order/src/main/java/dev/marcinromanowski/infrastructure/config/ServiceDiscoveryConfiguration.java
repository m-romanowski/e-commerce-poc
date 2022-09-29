package dev.marcinromanowski.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@EnableEurekaServer
@EnableEurekaClient
class ServiceDiscoveryConfiguration {

    @Bean
    @LoadBalanced
    WebClient.Builder webclientBuilder() {
        return WebClient.builder();
    }

}
