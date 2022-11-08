package dev.marcinromanowski.security.registration

import dev.marcinromanowski.security.registration.exception.RegistrationValidationException
import spock.lang.Specification

class RegistrationSpec extends Specification {

    private static final String VALID_EMAIL = ""
    private static final String VALID_PASSWORD = ""

    def "An 'registration' should be successfully created for valid email"() {
        when:
            def registration = Registration.create(email, VALID_PASSWORD.toCharArray())
        then:
            verifyAll(registration) {
                registration.email == VALID_EMAIL
                registration.password == VALID_PASSWORD.toCharArray()
            }

        where:
            email << [
                    "abc@example.com",
                    "abc-d@example.com",
                    "abc.def@example.com",
                    "abc_def@example.com",
                    "test/test@example.com",
                    "admin@example",
                    """"><script>alert(1);</script>"@example.com""",
                    "user+subaddress@example.com",
                    "user@[IPv6:2001:db8::1]",
                    """" "@example.com"""
            ]
    }

    def "An error should be thrown for invalid email format"() {
        when:
            Registration.create(email, VALID_PASSWORD.toCharArray())
        then:
            def e = thrown(RegistrationValidationException)
            e.message.containsIgnoreCase("registration doesn't pass validation rules")

        where:
            email << [
                    null,
                    "",
                    "abc-@example.com",
                    "abc..def@example.com",
                    ".abc@example.com",
                    "abc#def@example.com",
                    "Abc.example.com",
                    "A@b@c@example.com",
                    """a"b(c)d,e:f;g<h>i[j\\k]l@example.com""",
                    """just"not"right@example.com""",
                    "1234567890123456789012345678901234567890123456789012345678901234+x@example.com",
                    "i_like_underscore@but_its_not_allowed_in_this_part.example.com"
            ]
    }

    def "An 'registration' should be successfully created for valid password"() {
        when:
            def registration = Registration.create(VALID_EMAIL, "very_StrongP4\$sword".toCharArray())
        then:
            verifyAll(registration) {
                registration.email == VALID_EMAIL
                registration.password == VALID_PASSWORD.toCharArray()
            }
    }

    def "An error should be thrown for invalid password format"() {
        when:
            Registration.create(VALID_EMAIL, password?.toCharArray())
        then:
            def e = thrown(RegistrationValidationException)
            e.message.containsIgnoreCase("registration doesn't pass validation rules")

        where:
            password << [
                    null,
                    "",
                    "toShort",
                    "toEasyPassword",
                    "123abc",
            ]
    }

}
