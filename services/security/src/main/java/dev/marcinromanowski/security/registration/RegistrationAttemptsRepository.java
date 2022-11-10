package dev.marcinromanowski.security.registration;

import java.util.Optional;

public interface RegistrationAttemptsRepository {
    Registration save(Registration registration);
    Optional<Registration> findByEmail(String email);
}
