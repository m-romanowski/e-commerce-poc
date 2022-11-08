package dev.marcinromanowski.security.registration;

import dev.marcinromanowski.security.registration.exception.RegistrationValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class Registration {

    String email;
    char[] password;

    static Registration create(String email, char[] password, ValidationRules validationRules) {
        if (validationRules.isInvalid()) {
            throw new RegistrationValidationException();
        }

        return new Registration(email, password);
    }

}
