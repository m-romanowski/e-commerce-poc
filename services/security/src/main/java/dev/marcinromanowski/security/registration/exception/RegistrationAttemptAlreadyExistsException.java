package dev.marcinromanowski.security.registration.exception;

public class RegistrationAttemptAlreadyExistsException extends RegistrationException {

    public RegistrationAttemptAlreadyExistsException(String email) {
        super("An registration attempt with %s email address already exists".formatted(email));
    }

}
