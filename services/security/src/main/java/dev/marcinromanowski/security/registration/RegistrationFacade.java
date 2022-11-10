package dev.marcinromanowski.security.registration;

public class RegistrationFacade {

    public void createAccount(RegisterUserCommand command) {

    }

    public record RegisterUserCommand(String email, char[] password) {

    }

}
