package dev.marcinromanowski.infrastructure.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class JwtAuthenticationProvider implements AuthenticationProvider {

    JwtDecoder jwtDecoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            String tokenValue = authentication.getCredentials().toString();
            Jwt token = jwtDecoder.decode(tokenValue);
            List<GrantedAuthority> authorityList = tokenAuthorities(token).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
            return new UsernamePasswordAuthenticationToken(token.getSubject(), null, authorityList);
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private List<String> tokenAuthorities(Jwt token) {
        return Arrays.asList(token.<String>getClaim("scope").split(","));
    }

}
