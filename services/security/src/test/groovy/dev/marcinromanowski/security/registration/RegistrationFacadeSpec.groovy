package dev.marcinromanowski.security.registration

import dev.marcinromanowski.security.registration.exception.RegistrationAttemptAlreadyExistsException
import dev.marcinromanowski.security.registration.exception.RegistrationValidationException
import spock.lang.PendingFeature
import spock.lang.Specification

class RegistrationFacadeSpec extends Specification {

    private RegistrationFacade registrationFacade
    private RegistrationAttemptsRepository registrationAttemptsRepository

    def setup() {

    }

    def "User should be successfully registered for valid credentials"() {
        given: "valid user credentials"
            def email = "user@example.com"
            def password = "very_StrongP4\$sword"
            def registerCommand = new RegistrationFacade.RegisterUserCommand(email, password.toCharArray())

        when: "user tries to create a new account"
            registrationFacade.createAccount(registerCommand)
        then: "there no exception thrown"
            noExceptionThrown()
        and: "user registration attempt exists"
            verifyAll(registrationAttemptsRepository.findByEmail(email)) {
                it.email == email
                it.password == password.toCharArray()
            }
    }

    def "Registration attempt should be rejected on invalid credentials"() {
        when: "user tries to register with invalid credentials"
            registrationFacade.createAccount(registrationCommand)
        then: "an exception is thrown"
            def e = thrown(RegistrationValidationException)
            e.message.containsIgnoreCase("Registration data doesn't pass validation rules")

        where:
            registrationCommand                                                                           || reason
            new RegistrationFacade.RegisterUserCommand("email", "very_StrongP4\$sword".toCharArray())     || "invalid email"
            new RegistrationFacade.RegisterUserCommand("email@example.com", "weakPassword".toCharArray()) || "weak password"
    }

    def "Registration should be rejected if user already exists"() {
        given: "user already exists"
            def registration = Registration.create("email@example.com", "password".toCharArray(), new ValidationRules())
            registrationAttemptsRepository.save(registration)

        when: "user tries to register twice with the same email"
            registrationFacade.createAccount(new RegistrationFacade.RegisterUserCommand("email@example.com", "very_StrongP4\$sword".toCharArray()))
        then: "exception is thrown"
            def e = thrown(RegistrationAttemptAlreadyExistsException)
            e.message.containsIgnoreCase("An registration attempt with $registration.email email address already exists")
    }

    // TODO:
    //   1. After 1-st stage registration user need to activate his account via activation code sent to the email address
    @PendingFeature
    def "An account should be available to log in after activation"() {

    }

}
