package dev.marcinromanowski.gamescatalog;

import dev.marcinromanowski.gamescatalog.dto.GameDetailsDto;
import dev.marcinromanowski.gamescatalog.dto.GameDraftDetailsDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamesCatalogFacade {

    GamesCatalogService gamesCatalogService;
    GamesCatalogSearchService gamesCatalogSearchService;

    public GameDraftDetailsDto createNewGameDraft(AddGameToCatalogCommand command) {
        val newGameDetails = GameDraftDetails.createNew(command.title(), command.producer());
        return gamesCatalogService.createNewGameDraft(newGameDetails)
                .toDto();
    }

    public GameDraftDetailsDto createGameDraftFromCatalog(UUID gameId) {
        return gamesCatalogService.createGameDraftBasedOnCatalog(gameId)
                .toDto();
    }

    public void modifyGameDraft(ModifyGameFromCatalogCommand modifyGameFromCatalogCommand) {
        gamesCatalogService.modifyGameDraft(
                modifyGameFromCatalogCommand.draftId(),
                modifyGameFromCatalogCommand.title(),
                modifyGameFromCatalogCommand.producer()
        );
    }

    public void deleteGameDraft(UUID draftId) {
        gamesCatalogService.deleteGameDraft(draftId);
    }

    public UUID publishGameToCatalog(UUID draftId) {
        return gamesCatalogService.publishGameToCatalog(draftId);
    }

    public void deleteGameFromCatalog(UUID gameId) {
        gamesCatalogService.deleteGameFromCatalog(gameId);
    }

    public List<GameDraftDetailsDto> getAllDrafts() {
        return gamesCatalogService.getAllDrafts().stream()
                .map(GameDraftDetails::toDto)
                .toList();
    }

    public List<GameDetailsDto> getGamesCatalog(Pageable pageable) {
        return gamesCatalogService.getGamesCatalog(pageable).stream()
                .map(GameDetails::toDto)
                .toList();
    }

    public List<GameDetailsDto> findGamesCatalogBy(FindGamesCatalogQuery query) {
        return gamesCatalogSearchService.findAllContainingTitle(query.titlePattern(), query.pageable()).stream()
                .map(GameDetails::toDto)
                .toList();
    }

    public record AddGameToCatalogCommand(String title, String producer) {

    }

    public record ModifyGameFromCatalogCommand(UUID draftId, String title, String producer) {

    }

    public record FindGamesCatalogQuery(String titlePattern, Pageable pageable) {

    }

}
