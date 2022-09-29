package dev.marcinromanowski.infrastructure.security

import spock.lang.Specification

class RSAFixtureSpec extends Specification {

    private static final String PUBLIC_RSA_KEY = """
        -----BEGIN PUBLIC KEY-----
        MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJrtIpra+AESwvbi2An/TjYG7hPIylzX
        meR1RcL3MXOZlFnFqejcK6imsCXSKz9tkS3N9MDuxHYwM6FgH57BMG0CAwEAAQ==
        -----END PUBLIC KEY-----
    """

    private static final String PRIVATE_RSA_KEY = """
        -----BEGIN PRIVATE KEY-----
        MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmu0imtr4ARLC9uLY
        Cf9ONgbuE8jKXNeZ5HVFwvcxc5mUWcWp6NwrqKawJdIrP22RLc30wO7EdjAzoWAf
        nsEwbQIDAQABAkBQ8RPjVAO+i87QrjOxBXIXSAeazozrdLkyYxLPidaMTiDaztSH
        CazXaa3hIkgtYw40d1EhtuTm0xhfJAl8LGgBAiEAx3XBogOJv8VrppEwF9ZG0VwY
        qZoo0Og0DwNCQIqfLhUCIQDG17JUxYPMTFVQzATRQow5qXj1ylyhghUIAqEoDI1m
        +QIgcJvC3j0xH9vNkxSVGmAXS0u7gMVQFeGCwpcMaHPauqECIGOGpSEfN8fzUSS+
        6Y5gU9WsyPmlz1WbybEXt9hW6BPpAiEAsiOgkHvMD0r4kOdMGYJHh9FHsxpU8Doe
        Yu2v8w4xlgo=
        -----END PRIVATE KEY-----
    """

    def "RSA keys should be successfully loaded"() {
        when:
            def publicKey = RSAFixture.getPublicKeyFromString(PUBLIC_RSA_KEY)
            def privateKey = RSAFixture.getPrivateKeyFromString(PRIVATE_RSA_KEY)
        then:
            publicKey.isPresent()
            privateKey.isPresent()
    }

    def "RSA keys shouldn't be loaded for invalid data"() {
        when:
            def publicKey = RSAFixture.getPublicKeyFromString("INVALID_PUBLIC_KEY")
            def privateKey = RSAFixture.getPrivateKeyFromString("INVALID_PRIVATE_KEY")
        then:
            publicKey.isEmpty()
            privateKey.isEmpty()
    }

}
