package dev.marcinromanowski.security.registration

import dev.marcinromanowski.security.registration.exception.RegistrationValidationException
import spock.lang.Specification

class RegistrationSpec extends Specification {

    private static final String VALID_EMAIL = "abc@example.com"
    private static final String VALID_PASSWORD = "very_StrongP4\$sword"

    def "An 'registration' should be successfully created for valid email"() {
        when:
            def emailValidation = new EmailValidation(email)
            def registration = Registration.create(email, VALID_PASSWORD.toCharArray(), new ValidationRules().with(emailValidation))
        then:
            verifyAll(registration) {
                registration.email == email
                registration.password == VALID_PASSWORD.toCharArray()
            }

        where:
            email << [
                    "abc@example.com",
                    "abc-d@example.com",
                    "abc.def@example.com",
                    "abc_def@example.com",
                    "user+subaddress@example.com"
            ]
    }

    def "An error should be thrown for invalid email format"() {
        when:
            def emailValidation = new EmailValidation(email)
            Registration.create(email, VALID_PASSWORD.toCharArray(), new ValidationRules().with(emailValidation))
        then:
            def e = thrown(RegistrationValidationException)
            e.message.containsIgnoreCase("registration data doesn't pass validation rules")

        where:
            email << [
                    null,
                    "",
                    "abc..def@example.com",
                    ".abc@example.com",
                    "abc#def@example.com",
                    "Abc.example.com",
                    "A@b@c@example.com",
                    """a"b(c)d,e:f;g<h>i[j\\k]l@example.com""",
                    """just"not"right@example.com""",
                    "i_like_underscore@but_its_not_allowed_in_this_part.example.com"
            ]
    }

    def "An 'registration' should be successfully created for valid password"() {
        when:
            def passwordValidation = new PasswordValidation(VALID_PASSWORD.toCharArray())
            def registration = Registration.create(VALID_EMAIL, VALID_PASSWORD.toCharArray(), new ValidationRules().with(passwordValidation))
        then:
            verifyAll(registration) {
                registration.email == VALID_EMAIL
                registration.password == VALID_PASSWORD.toCharArray()
            }
    }

    def "An error should be thrown for invalid password format"() {
        when:
            def passwordArr = password?.toCharArray() ?: null
            def passwordValidation = new PasswordValidation(passwordArr)
            Registration.create(VALID_EMAIL, passwordArr, new ValidationRules().with(passwordValidation))
        then:
            def e = thrown(RegistrationValidationException)
            e.message.containsIgnoreCase("registration data doesn't pass validation rules")

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