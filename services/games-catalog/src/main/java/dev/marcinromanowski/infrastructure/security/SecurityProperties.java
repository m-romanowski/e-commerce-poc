package dev.marcinromanowski.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Component
@ConfigurationProperties(value = "security")
public class SecurityProperties {

    @NotNull
    private JwtToken jwtToken;

    @Data
    public static class JwtToken {
        @NotBlank
        private String privateKey;
        @NotBlank
        private String publicKey;
    }

}
