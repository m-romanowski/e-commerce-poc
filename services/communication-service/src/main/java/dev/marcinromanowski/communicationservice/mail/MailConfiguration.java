package dev.marcinromanowski.communicationservice.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.communicationservice.infrastructure.mail.CommunicationServiceCommandListener;
import dev.marcinromanowski.communicationservice.infrastructure.mail.CoutMailSender;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.ITemplateEngine;

import java.time.Clock;

@Configuration
class MailConfiguration {

    @Bean
    MailSender mailSender(Clock clock) {
        return new CoutMailSender(clock);
    }

    @Bean
    MailFacade mailFacade(MailProperties mailProperties,
                          MessageSource messageSource,
                          ITemplateEngine iTemplateEngine,
                          MailSender mailSender) {
        val mailService = new MailService(mailProperties, messageSource, iTemplateEngine, mailSender);
        return new MailFacade(mailService);
    }

    @Bean
    CommunicationServiceCommandListener communicationServiceCommandListener(MailFacade mailFacade, ObjectMapper objectMapper) {
        return new CommunicationServiceCommandListener(mailFacade, objectMapper);
    }

}
