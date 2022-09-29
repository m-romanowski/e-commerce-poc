package dev.marcinromanowski.gamescatalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GamesCatalogRepository {
    GameDetails save(GameDetails gameDetails);
    Optional<GameDetails> findById(UUID id);
    List<GameDetails> findAll(Pageable pageable);
    List<GameDetails> findAllContainingTitle(String title, Pageable pageable);
    void deleteById(UUID gameId);
    Optional<GameDetails> findByDraftId(UUID draftId);
}


interface GamesCatalogCrudRepository extends PagingAndSortingRepository<GameEntity, UUID> {
    @Query(value = "SELECT gc.id, gc.published_at, gc.live_draft_id FROM game_catalog gc INNER JOIN game_draft_catalog gdc ON gc.live_draft_id = gdc.id WHERE UPPER(gdc.title) LIKE UPPER(CONCAT('%', :title, '%'))", nativeQuery = true)
    List<GameEntity> findAllContainingTitle(@Param("title") String title);
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
    public List<GameDetails> findAllContainingTitle(String title, Pageable pageable) {
        return gamesCatalogCrudRepository.findAllContainingTitle(title).stream()
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
