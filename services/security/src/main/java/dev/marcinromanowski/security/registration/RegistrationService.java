package dev.marcinromanowski.security.registration;

import dev.marcinromanowski.security.registration.exception.UserAlreadyRegisteredException;
import dev.marcinromanowski.security.user.UserFacade;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class RegistrationService {

    UserFacade userFacade;
    RegistrationAttemptsRepository registrationAttemptsRepository;

    void createUserAccount(String email, char[] password) {
        val validationRules = new ValidationRules()
            .with(new EmailValidation(email))
            .with(new PasswordValidation(password));
        val registration = Registration.create(email, password, validationRules);
        userFacade.findUser(email)
            .ifPresentOrElse(ignoredDetails -> {
                throw new UserAlreadyRegisteredException(email);
            }, () -> registrationAttemptsRepository.save(registration));
    }

}
