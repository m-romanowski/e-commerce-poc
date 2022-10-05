package dev.marcinromanowski.communicationservice.mail;

import java.time.Instant;

public record MailResponse(String id, Instant sentAt) {

}
