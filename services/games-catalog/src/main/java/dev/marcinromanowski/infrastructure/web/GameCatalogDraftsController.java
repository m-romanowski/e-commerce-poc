package dev.marcinromanowski.infrastructure.web;

import dev.marcinromanowski.gamescatalog.GamesCatalogFacade;
import dev.marcinromanowski.gamescatalog.dto.GameDraftDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// TODO: add exceptions handlers
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/game-catalog/draft")
public class GameCatalogDraftsController {

    private final GamesCatalogFacade gamesCatalogFacade;

    @GetMapping
    public List<GameDraftDetailsDto> getGameCatalogDrafts() {
        return gamesCatalogFacade.getAllDrafts();
    }

    @PostMapping(value = "/new")
    public GameDraftDetailsDto createNewGameDraft(@RequestBody AddGameDraftFormRequest request) {
        val command = new GamesCatalogFacade.AddGameToCatalogCommand(request.title(), request.producer());
        return gamesCatalogFacade.createNewGameDraft(command);
    }

    @PostMapping(value = "/copy")
    public GameDraftDetailsDto createNewGameDraftBasedOnCatalog(@RequestParam UUID id) {
        return gamesCatalogFacade.createGameDraftFromCatalog(id);
    }

    @PutMapping(value = "/modify")
    public void modifyGameDraft(@RequestBody ModifyGameDraftFormRequest request) {
        val command = new GamesCatalogFacade.ModifyGameFromCatalogCommand(
                request.id(),
                request.title(),
                request.producer()
        );
        gamesCatalogFacade.modifyGameDraft(command);
    }

    @PostMapping(value = "/{draftId}/publish")
    public UUID publishGameDraft(@PathVariable UUID draftId) {
        return gamesCatalogFacade.publishGameToCatalog(draftId);
    }

    public record AddGameDraftFormRequest(String title, String producer) {

    }

    public record ModifyGameDraftFormRequest(UUID id, String title, String producer) {

    }

}
