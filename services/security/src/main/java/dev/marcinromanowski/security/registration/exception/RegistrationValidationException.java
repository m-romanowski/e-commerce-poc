package dev.marcinromanowski.security.registration.exception;

public class RegistrationValidationException extends RegistrationException {

    public RegistrationValidationException() {
        super("Registration doesn't pass validation rules");
    }

}
