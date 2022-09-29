package dev.marcinromanowski.infrastructure.web;

import dev.marcinromanowski.gamescatalog.GamesCatalogFacade;
import dev.marcinromanowski.gamescatalog.dto.GameDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/game-catalog")
public class GamesCatalogController {

    private final GamesCatalogFacade gamesCatalogFacade;

    @GetMapping
    List<GameDetailsDto> getGamesCatalog(@RequestParam(required = false) String search, Pageable pageable) {
        val query = new GamesCatalogFacade.GetGamesCatalogQuery(search, pageable);
        return gamesCatalogFacade.getGamesCatalog(query);
    }

}
