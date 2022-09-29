package dev.marcinromanowski.gamescatalog;

import dev.marcinromanowski.gamescatalog.dto.GameDraftDetailsDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
@RequiredArgsConstructor(staticName = "restoreFrom")
class GameDraftDetails {

    UUID id;
    UUID basedOnGameId;
    String title;
    String producer;

    static GameDraftDetails createNew(String title, String producer) {
        return GameDraftDetails.builder()
                .title(title)
                .producer(producer)
                .build();
    }

    static GameDraftDetails createNewBasedOn(UUID gameId, String title, String producer) {
        return GameDraftDetails.builder()
                .basedOnGameId(gameId)
                .title(title)
                .producer(producer)
                .build();
    }

    GameDraftDetails withTitle(String title) {
        return this.toBuilder()
                .title(title)
                .build();
    }

    GameDraftDetails withProducer(String producer) {
        return this.toBuilder()
                .producer(producer)
                .build();
    }

    GameDraftDetailsDto toDto() {
        return new GameDraftDetailsDto(id, title, producer);
    }

}
