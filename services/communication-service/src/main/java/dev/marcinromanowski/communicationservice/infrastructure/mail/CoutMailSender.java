package dev.marcinromanowski.communicationservice.infrastructure.mail;

import dev.marcinromanowski.communicationservice.mail.MailRequest;
import dev.marcinromanowski.communicationservice.mail.MailResponse;
import dev.marcinromanowski.communicationservice.mail.MailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Clock;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CoutMailSender implements MailSender {

    private final Clock clock;

    @Override
    public MailResponse send(MailRequest request) {
        val response = new MailResponse(UUID.randomUUID().toString(), clock.instant());
        log.info("Email sent: {} -> {}", request, response);
        return response;
    }

}
