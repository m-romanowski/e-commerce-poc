package dev.marcinromanowski.communicationservice.infrastructure.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.communicationservice.common.CommunicationServiceCommand;
import dev.marcinromanowski.communicationservice.mail.MailFacade;
import dev.marcinromanowski.communicationservice.mail.command.MailCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunicationServiceCommandListener {

    MailFacade mailFacade;
    ObjectMapper objectMapper;

    @KafkaListener(topics = "${mail.kafka.command-topic}", groupId = "${mail.kafka.consumer-group-id}", containerFactory = "kafkaListenerContainerFactory")
    void onCommand(ConsumerRecord<String, String> consumerRecord) {
        log.debug("Processing {}", consumerRecord);
        parseCommand(consumerRecord.value())
            .ifPresent(command -> {
                if (command instanceof MailCommand mailCommand) {
                    mailFacade.sendMail(mailCommand);
                } else {
                    log.error("Got unsupported command type: {}", command.getType());
                }
            });
    }

    private Optional<CommunicationServiceCommand> parseCommand(String serializedCommand) {
        try {
            return Optional.of(objectMapper.readValue(serializedCommand, CommunicationServiceCommand.class));
        } catch (JsonProcessingException e) {
            log.error("Cannot deserialize command: {}", serializedCommand, e);
            return Optional.empty();
        }
    }

}
