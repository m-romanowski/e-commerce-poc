package dev.marcinromanowski.invoice;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@RequiredArgsConstructor(staticName = "from")
class Invoice {

    @NonNull
    UUID id;
    @NonNull
    UUID orderId;
    @NonNull
    String userId;
    @NonNull
    Instant createdAt;

    static Invoice create(UUID orderId, String userId, Instant currentTime) {
        return new Invoice(UUID.randomUUID(), orderId, userId, currentTime);
    }

}
