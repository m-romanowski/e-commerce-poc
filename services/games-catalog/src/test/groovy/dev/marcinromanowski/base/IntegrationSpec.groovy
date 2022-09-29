package dev.marcinromanowski.base

import dev.marcinromanowski.ProductsCatalogApplication
import dev.marcinromanowski.common.Profiles
import dev.marcinromanowski.infrastructure.security.SecurityProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Clock

@Import(IntegrationConfiguration)
@AutoConfigureMockMvc
@ActiveProfiles([Profiles.INTEGRATION])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ProductsCatalogApplication])
abstract class IntegrationSpec extends Specification implements ClockFixture {

    static {
        PostgreSQL.startContainer()
        ElasticSearch.startContainer()
    }

    @Autowired
    MutableClock testClock
    @Autowired
    MockMvc mockMvc

    void setup() {
        testClock.setInstant(clock().instant())
    }

    void cleanup() {
        testClock.setInstant(clock().instant())
    }

    @TestConfiguration
    static class IntegrationConfiguration {

        @Bean
        @Primary
        Clock testClock() {
            return new MutableClock()
        }

        @Bean
        JwtGenerator jwtGenerator(SecurityProperties securityProperties, Clock clock) {
            def accessTokenProperties = securityProperties.jwtToken
            return new JwtGenerator(accessTokenProperties.publicKey, accessTokenProperties.privateKey, () -> clock.instant())
        }

    }

}
