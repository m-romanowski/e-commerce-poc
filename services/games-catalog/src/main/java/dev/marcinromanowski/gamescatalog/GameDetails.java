package dev.marcinromanowski.gamescatalog;

import dev.marcinromanowski.gamescatalog.dto.GameDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@RequiredArgsConstructor(staticName = "restoreFrom")
class GameDetails {

    UUID id;
    GameDraftDetails draftDetails;
    Instant publishedAt;

    static GameDetails from(UUID gameId, GameDraftDetails gameDraftDetails, Instant publishedAt) {
        return new GameDetails(gameId, gameDraftDetails, publishedAt);
    }

    GameDraftDetails createNewDraft() {
        return GameDraftDetails.createNewBasedOn(id, draftDetails.getTitle(), draftDetails.getProducer());
    }

    GameDetailsDto toDto() {
        return new GameDetailsDto(
                id,
                draftDetails.getTitle(),
                draftDetails.getProducer(),
                publishedAt
        );
    }

}
