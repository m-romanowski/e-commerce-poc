package dev.marcinromanowski.gamescatalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GamesCatalogRepository {
    GameDetails save(GameDetails gameDetails);
    Optional<GameDetails> findById(UUID id);
    List<GameDetails> findAll(Pageable pageable);
    void deleteById(UUID gameId);
    Optional<GameDetails> findByDraftId(UUID draftId);
}

interface GamesCatalogCrudRepository extends PagingAndSortingRepository<GameEntity, UUID> {

}

@RequiredArgsConstructor
class JpaGamesCatalogRepository implements GamesCatalogRepository {

    private final GamesCatalogCrudRepository gamesCatalogCrudRepository;

    @Override
    public GameDetails save(GameDetails gameDetails) {
        return gamesCatalogCrudRepository.save(GameEntity.from(gameDetails))
                .toGameDetails();
    }

    @Override
    public Optional<GameDetails> findById(UUID id) {
        return gamesCatalogCrudRepository.findById(id)
                .map(GameEntity::toGameDetails);
    }

    @Override
    public List<GameDetails> findAll(Pageable pageable) {
        return gamesCatalogCrudRepository.findAll(pageable).stream()
                .map(GameEntity::toGameDetails)
                .toList();
    }

    @Override
    public void deleteById(UUID gameId) {
        gamesCatalogCrudRepository.deleteById(gameId);
    }

    @Override
    public Optional<GameDetails> findByDraftId(UUID draftId) {
        return gamesCatalogCrudRepository.findById(draftId)
                .map(GameEntity::toGameDetails);
    }

}

@Data
@Entity
@Indexed
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "game_catalog")
class GameEntity {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "published_at")
    private Instant publishedAt;
    @OneToOne
    @JoinColumn(name = "live_draft_id", referencedColumnName = "id")
    @IndexedEmbedded(includePaths = {"title"})
    private GameDraftEntity liveDraft;

    static GameEntity from(GameDetails gameDetails) {
        return new GameEntity(
                gameDetails.getId(),
                gameDetails.getPublishedAt(),
                GameDraftEntity.from(gameDetails.getDraftDetails())
        );
    }

    GameDetails toGameDetails() {
        return GameDetails.restoreFrom(id, liveDraft.toGameDetails(), publishedAt);
    }

}
