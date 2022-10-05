package dev.marcinromanowski.communicationservice.mail;

public interface MailSender {
    MailResponse send(MailRequest request);
}
