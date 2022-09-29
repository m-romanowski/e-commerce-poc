package dev.marcinromanowski.gamescatalog;

import dev.marcinromanowski.gamescatalog.exception.CannotFindGameDraftException;
import dev.marcinromanowski.gamescatalog.exception.GamesCatalogInconsistencyStateException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class GamesCatalogService {

    Clock clock;
    GamesCatalogDraftsRepository gamesCatalogDraftsRepository;
    GamesCatalogRepository gamesCatalogRepository;

    GameDraftDetails createNewGameDraft(GameDraftDetails gameDetails) {
        return gamesCatalogDraftsRepository.save(gameDetails);
    }

    GameDraftDetails createGameDraftBasedOnCatalog(UUID gameId) {
        return gamesCatalogRepository.findById(gameId)
                .map(GameDetails::createNewDraft)
                .map(gamesCatalogDraftsRepository::save)
                .orElseThrow(() -> new CannotFindGameDraftException(gameId));
    }

    List<GameDraftDetails> getAllDrafts() {
        return gamesCatalogDraftsRepository.findAll();
    }

    List<GameDetails> getGamesCatalog(Pageable pageable) {
        return gamesCatalogRepository.findAll(pageable);
    }

    UUID publishGameToCatalog(UUID draftId) {
        return gamesCatalogDraftsRepository.findById(draftId)
                .map(gameDraftDetails -> GameDetails.from(gameDraftDetails.getBasedOnGameId(), gameDraftDetails, clock.instant()))
                .map(gamesCatalogRepository::save)
                .map(GameDetails::getId)
                .orElseThrow(() -> new CannotFindGameDraftException(draftId));
    }

    void deleteGameDraft(UUID draftId) {
        gamesCatalogRepository.findByDraftId(draftId)
                .ifPresentOrElse(
                        ignoredDetails -> {
                            throw new GamesCatalogInconsistencyStateException();
                        },
                        () -> gamesCatalogDraftsRepository.deleteById(draftId)
                );
    }

    void modifyGameDraft(UUID draftId, String title, String producer) {
        gamesCatalogDraftsRepository.findById(draftId)
                .ifPresentOrElse(
                        oldDetails -> {
                            val newDetails = oldDetails
                                    .withTitle(title)
                                    .withProducer(producer);
                            gamesCatalogDraftsRepository.save(newDetails);
                        },
                        () -> {
                            throw new CannotFindGameDraftException(draftId);
                        }
                );
    }

    void deleteGameFromCatalog(UUID gameId) {
        gamesCatalogRepository.deleteById(gameId);
    }

}
