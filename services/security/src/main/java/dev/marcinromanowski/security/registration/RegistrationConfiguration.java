package dev.marcinromanowski.security.registration;

import dev.marcinromanowski.security.user.UserFacade;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RegistrationConfiguration {

    @Bean
    RegistrationFacade registrationFacade(UserFacade userFacade, RegistrationAttemptsRepository registrationAttemptsRepository) {
        val registrationService = new RegistrationService(userFacade, registrationAttemptsRepository);
        return new RegistrationFacade(registrationService);
    }

}
