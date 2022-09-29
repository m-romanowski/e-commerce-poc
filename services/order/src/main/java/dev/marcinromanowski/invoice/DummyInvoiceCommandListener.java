package dev.marcinromanowski.invoice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.marcinromanowski.invoice.commands.InvoiceCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class DummyInvoiceCommandListener implements InvoiceCommandListener {

    ObjectMapper objectMapper;
    InvoiceService invoiceService;

    @Override
    public void process(String serializedCommand) {
        deserializeInvoiceCommand(serializedCommand)
                .ifPresent(invoiceService::onInvoiceCommand);
    }

    private Optional<InvoiceCommand> deserializeInvoiceCommand(String serializedCommand) {
        try {
            return Optional.of(objectMapper.readValue(serializedCommand, InvoiceCommand.class));
        } catch (JsonProcessingException e) {
            log.info("Cannot deserialize invoice command: {}", serializedCommand);
            return Optional.empty();
        }
    }

}
