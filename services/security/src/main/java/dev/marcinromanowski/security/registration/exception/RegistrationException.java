package dev.marcinromanowski.security.registration.exception;

public abstract class RegistrationException extends RuntimeException {

    public RegistrationException(String message) {
        super(message);
    }

}
