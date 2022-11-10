package dev.marcinromanowski.security.registration;

public interface RegistrationAttemptsRepository {
    Registration save(Registration registration);
    Registration findByEmail(String email);
}
