package dev.marcinromanowski.communicationservice.mail;

import dev.marcinromanowski.communicationservice.mail.command.EmailMetadata;

public record MailRequest(String id, EmailMetadata from, EmailMetadata to, String subject, String body) {

}
