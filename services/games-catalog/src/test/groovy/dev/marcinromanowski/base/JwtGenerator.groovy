package dev.marcinromanowski.base

import com.auth0.jwt.algorithms.Algorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import dev.marcinromanowski.infrastructure.security.RSAFixture
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.apache.commons.codec.binary.Base64

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

@CompileStatic
class JwtGenerator {

    private KeyStore keyStore
    private Supplier<Instant> issueTimeFactory

    JwtGenerator(String publicKey, String privateKey, Supplier<Instant> timeSupplier) {
        this.keyStore = keyStore(publicKey, privateKey)
        this.issueTimeFactory = timeSupplier
    }

    private static KeyStore keyStore(String publicKey, String privateKey) {
        def RSAPublicKey = RSAFixture.getPublicKeyFromString(publicKey)
                .orElseThrow(() -> new RuntimeException())
        def RSAPrivateKey = RSAFixture.getPrivateKeyFromString(privateKey)
                .orElseThrow(() -> new RuntimeException())
        return new KeyPairKeystore(Set.of(RSAPublicKey), RSAPrivateKey, RSAPublicKey)
    }

    String generateToken(Integer expirationDelayMinutes, String userName, String authorities, UUID sessionId) {
        return generateToken(
                expirationDelayMinutes,
                [
                        (JwtClaims.SCOPE.getClaim())     : authorities,
                        (JwtClaims.SESSION_ID.getClaim()): sessionId.toString(),
                        (JWTClaimNames.SUBJECT)          : userName
                ] as Map<String, Object>
        )
    }

    String generateToken(Integer expirationDelayMinutes, Map<String, Object> claims) {
        JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder()
        claims.forEach((key, value) -> jwtBuilder.claim(key, value))
        Instant currentTime = issueTimeFactory.get()
        jwtBuilder = jwtBuilder.issueTime(Date.from(currentTime))
        if (expirationDelayMinutes != 0) {
            Date expiration = Date.from(currentTime.plus(expirationDelayMinutes, ChronoUnit.MINUTES))
            jwtBuilder = jwtBuilder.expirationTime(expiration)
        }

        JWTClaimsSet jwtClaimsSet = jwtBuilder.build()
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256)
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet)
        return getSignedJwtToken(jwt)
    }

    private String getSignedJwtToken(SignedJWT jwt) {
        String header = jwt.getHeader().toBase64URL().toString()
        String payload = jwt.getPayload().toBase64URL().toString()
        String signature = keyStore.sign(header, payload)
        return "$header.$payload.$signature"
    }

    private enum JwtClaims {

        SESSION_ID("session_id"),
        SCOPE("scope")

        final String claim

        JwtClaims(String claim) {
            this.claim = claim
        }

    }

    private static interface KeyStore {
        Set<RSAPublicKey> getPublicKeys()
        String sign(String header, String payload)
    }

    @TupleConstructor(includeFields = true)
    private static class KeyPairKeystore implements KeyStore {

        private final Set<RSAPublicKey> validatorPublicKeys
        private final RSAPrivateKey generatorPrivateKey
        private final RSAPublicKey generatorPublicKey

        @Override
        Set<RSAPublicKey> getPublicKeys() {
            return validatorPublicKeys
        }

        @Override
        String sign(String header, String payload) {
            Algorithm algorithm = Algorithm.RSA256(generatorPublicKey, generatorPrivateKey)
            byte[] signature = algorithm.sign(header.getBytes(), payload.getBytes())
            return Base64.encodeBase64URLSafeString(signature)
        }

    }

}
