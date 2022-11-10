package dev.marcinromanowski.security.registration.exception;

public class UserAlreadyRegisteredException extends RegistrationException {

    public UserAlreadyRegisteredException(String email) {
        super("A user with %s email address already exists".formatted(email));
    }

}
