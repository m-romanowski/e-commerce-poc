package dev.marcinromanowski.gamescatalog;

import org.springframework.data.domain.Pageable;

import java.util.List;

interface GamesCatalogSearchService {
    List<GameDetails> findAllContainingTitle(String title, Pageable pageable);
}
