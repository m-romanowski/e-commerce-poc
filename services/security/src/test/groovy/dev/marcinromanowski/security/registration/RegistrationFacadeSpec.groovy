package dev.marcinromanowski.security.registration

import dev.marcinromanowski.security.registration.exception.RegistrationValidationException
import dev.marcinromanowski.security.registration.exception.UserAlreadyRegisteredException
import dev.marcinromanowski.security.user.UserFacade
import dev.marcinromanowski.security.user.dto.UserDetailsDto
import spock.lang.PendingFeature
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class RegistrationFacadeSpec extends Specification {

    private UserFacade userFacade
    private RegistrationFacade registrationFacade
    private RegistrationAttemptsRepository registrationAttemptsRepository

    def setup() {
        def configuration = new RegistrationConfiguration()
        userFacade = Mock()
        registrationAttemptsRepository = new InMemoryRegistrationAttemptsRepository()
        registrationFacade = configuration.registrationFacade(userFacade, registrationAttemptsRepository)
    }

    def "User should be successfully registered for valid credentials"() {
        given: "user doesn't exists"
            def email = "user@example.com"
            1 * userFacade.findUser(email) >> Optional.empty()
        and: "valid user credentials"
            def password = "very_StrongP4\$sword"
            def registerCommand = new RegistrationFacade.RegisterUserCommand(email, password.toCharArray())

        when: "user tries to create a new account"
            registrationFacade.createAccount(registerCommand)
        then: "there no exception thrown"
            noExceptionThrown()
        and: "user registration attempt exists"
            verifyAll(registrationAttemptsRepository.findByEmail(email)) {
                it.isPresent()
                it.get().email == email
                it.get().password == password.toCharArray()
            }
    }

    def "Registration attempt should be rejected on invalid credentials, reason: #reason"() {
        given: "user doesn't exists"
            0 * userFacade.findUser(registrationCommand.email()) >> Optional.empty()

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
            def userEmailAddress = "email@example.com"
            1 * userFacade.findUser(userEmailAddress) >> Optional.of(new UserDetailsDto(userEmailAddress))

        when: "user tries to register twice with the same email"
            registrationFacade.createAccount(new RegistrationFacade.RegisterUserCommand(userEmailAddress, "very_StrongP4\$sword".toCharArray()))
        then: "exception is thrown"
            def e = thrown(UserAlreadyRegisteredException)
            e.message.containsIgnoreCase("A user with $userEmailAddress email address already exists")
    }

    // TODO:
    //   1. After 1-st stage registration user need to activate his account via activation code sent to the email address
    @PendingFeature
    def "An account should be available to log in after activation"() {

    }

    private static class InMemoryRegistrationAttemptsRepository implements RegistrationAttemptsRepository {

        private final Map<String, Registration> registrationAttempts = new ConcurrentHashMap<>()

        @Override
        Registration save(Registration registration) {
            registrationAttempts.put(registration.email, registration)
            return registration
        }

        @Override
        Optional<Registration> findByEmail(String email) {
            def foundRegistrationAttempt = registrationAttempts.values().find { it.email == email }
            return Optional.ofNullable(foundRegistrationAttempt)
        }

    }

}
