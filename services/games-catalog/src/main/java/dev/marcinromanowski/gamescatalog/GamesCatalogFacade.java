package dev.marcinromanowski.gamescatalog;

import dev.marcinromanowski.gamescatalog.dto.GameDetailsDto;
import dev.marcinromanowski.gamescatalog.dto.GameDraftDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class GamesCatalogFacade {

    private final GamesCatalogService gamesCatalogService;

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

    public void deleteGameFromCatalog(UUID gameId) {
        gamesCatalogService.deleteGameFromCatalog(gameId);
    }

    public List<GameDraftDetailsDto> getAllDrafts() {
        return gamesCatalogService.getAllDrafts().stream()
                .map(GameDraftDetails::toDto)
                .toList();
    }

    public List<GameDetailsDto> getGamesCatalog(GetGamesCatalogQuery query) {
        List<GameDetails> foundCatalog = query.getTitlePattern().isEmpty()
                ? gamesCatalogService.getGamesCatalog(query.pageable())
                : gamesCatalogService.searchGamesByTitleCatalog(query.titlePattern(), query.pageable());
        return foundCatalog.stream()
                .map(GameDetails::toDto)
                .toList();
    }

    public UUID publishGameToCatalog(UUID draftId) {
        return gamesCatalogService.publishGameToCatalog(draftId);
    }

    public record AddGameToCatalogCommand(
            String title,
            String producer) {

    }

    public record ModifyGameFromCatalogCommand(
            UUID draftId,
            String title,
            String producer) {

    }

    public record GetGamesCatalogQuery(String titlePattern, Pageable pageable) {

        public GetGamesCatalogQuery(Pageable pageable) {
            this(null, pageable);
        }

        Optional<String> getTitlePattern() {
            return Optional.ofNullable(titlePattern);
        }

    }

}
