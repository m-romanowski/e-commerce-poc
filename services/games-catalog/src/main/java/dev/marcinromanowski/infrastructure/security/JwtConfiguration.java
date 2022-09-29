package dev.marcinromanowski.infrastructure.security;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Clock;
import java.util.List;

@Configuration
class JwtConfiguration {

    @Bean
    JwtDecoder jwtDecoder(Clock clock, SecurityProperties securityProperties) {
        val publicKey = RSAFixture.getPublicKeyFromString(securityProperties.getJwtToken().getPublicKey())
                .orElseThrow(CannotLoadRSAKeys::new);
        val decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        val timestampValidator = new JwtTimestampValidator();
        timestampValidator.setClock(clock);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(timestampValidator)));
        return decoder;
    }

    static class SecurityConfigurationException extends RuntimeException {

    }

    static class CannotLoadRSAKeys extends SecurityConfigurationException {

    }

}
