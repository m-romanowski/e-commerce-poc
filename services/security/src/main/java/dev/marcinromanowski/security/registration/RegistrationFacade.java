package dev.marcinromanowski.security.registration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistrationFacade {

    private final RegistrationService registrationService;

    public void createAccount(RegisterUserCommand command) {
        registrationService.createUserAccount(command.email(), command.password());
    }

    public record RegisterUserCommand(String email, char[] password) {

    }

}
