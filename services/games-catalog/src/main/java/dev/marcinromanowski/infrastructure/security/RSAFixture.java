package dev.marcinromanowski.infrastructure.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSAFixture {

    public static Optional<RSAPublicKey> getPublicKeyFromString(String key) {
        try {
            String publicKeyPEM = key;
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
            publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
            publicKeyPEM = publicKeyPEM.replaceAll("\\s+", "");
            byte[] encoded = Base64.decodeBase64(publicKeyPEM);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return Optional.of((RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded)));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public static Optional<RSAPrivateKey> getPrivateKeyFromString(String key) {
        try {
            String privateKeyPEM = key;
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replaceAll("\\s+", "");
            byte[] encoded = Base64.decodeBase64(privateKeyPEM);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return Optional.of((RSAPrivateKey) kf.generatePrivate(keySpec));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

}
