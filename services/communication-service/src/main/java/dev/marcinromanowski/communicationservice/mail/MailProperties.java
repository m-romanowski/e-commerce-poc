package dev.marcinromanowski.communicationservice.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "mail")
class MailProperties {

    @NotNull
    private Email email;
    @NotNull
    private Kafka kafka;

    @Data
    static class Email {

        @NotBlank
        private String replyTo;
        @NotBlank
        private String defaultLanguage;

    }

    @Data
    static class Kafka {

        @NotBlank
        private String consumerGroupId;
        @NotBlank
        private String commandTopic;

    }

}
