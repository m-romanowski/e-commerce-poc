package dev.marcinromanowski.security.registration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class Registration {

    String email;
    char[] password;

    static Registration create(String email, char[] password) {
        return new Registration(email, password);
    }

}
