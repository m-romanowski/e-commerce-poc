package dev.marcinromanowski.gamescatalog;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class GamesCatalogConfiguration {

    @Bean
    GamesCatalogDraftsRepository gamesCatalogDraftsRepository(GamesCatalogDraftsCrudRepository gamesCatalogDraftsCrudRepository) {
        return new JpaGamesCatalogDraftsRepository(gamesCatalogDraftsCrudRepository);
    }

    @Bean
    GamesCatalogRepository gamesCatalogRepository(GamesCatalogCrudRepository gamesCatalogCrudRepository) {
        return new JpaGamesCatalogRepository(gamesCatalogCrudRepository);
    }

    @Bean
    GamesCatalogFacade gamesCatalogFacade(
            Clock clock,
            GamesCatalogDraftsRepository gamesCatalogDraftsRepository,
            GamesCatalogRepository gamesCatalogRepository) {
        val gamesCatalogService = new GamesCatalogService(clock, gamesCatalogDraftsRepository, gamesCatalogRepository);
        return new GamesCatalogFacade(gamesCatalogService);
    }

}
