package dev.marcinromanowski.gamescatalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GamesCatalogDraftsRepository {
    GameDraftDetails save(GameDraftDetails gameDetails);
    Optional<GameDraftDetails> findById(UUID id);
    List<GameDraftDetails> findAll();
    void deleteById(UUID draftId);
}

interface GamesCatalogDraftsCrudRepository extends JpaRepository<GameDraftEntity, UUID> {

}

@RequiredArgsConstructor
class JpaGamesCatalogDraftsRepository implements GamesCatalogDraftsRepository {

    private final GamesCatalogDraftsCrudRepository gamesCatalogDraftsCrudRepository;

    @Override
    public GameDraftDetails save(GameDraftDetails gameDetails) {
        return gamesCatalogDraftsCrudRepository.save(GameDraftEntity.from(gameDetails))
                .toGameDetails();
    }

    @Override
    public Optional<GameDraftDetails> findById(UUID id) {
        return gamesCatalogDraftsCrudRepository.findById(id)
                .map(GameDraftEntity::toGameDetails);
    }

    @Override
    public List<GameDraftDetails> findAll() {
        return gamesCatalogDraftsCrudRepository.findAll().stream()
                .map(GameDraftEntity::toGameDetails)
                .toList();
    }

    @Override
    public void deleteById(UUID draftId) {
        gamesCatalogDraftsCrudRepository.deleteById(draftId);
    }

}

@Data
@Entity
@NoArgsConstructor
@Table(name = "currency")
class CurrencyEntity {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

}

@Data
@Entity
@NoArgsConstructor
@Table(name = "country")
class CountryEntity {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "country_code", length = 2)
    private String countryCode;
    @OneToOne(cascade = CascadeType.ALL) // TODO: Countries can have many available currencies?
    @JoinColumn(name = "currency_id", referencedColumnName = "id", nullable = false)
    private CurrencyEntity currency;

}

@Data
@Entity
@NoArgsConstructor
@Table(name = "game_catalog_price")
class GamePriceEntity {

    @Id
    @GeneratedValue
    private UUID id;
    private BigDecimal amount;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "id", nullable = false)
    private CountryEntity country;
    @ManyToOne
    @JoinColumn(name = "draft_id", referencedColumnName = "id", nullable = false)
    private GameDraftEntity gameDraft;

}

// TODO: add more information (tags, opinions, etc)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "game_draft_catalog")
class GameDraftEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
    private String title;
    private String producer;
    @Column(name = "based_on_game_id")
    private UUID basedOnGameId;
    // TODO: include price
//    @OneToMany(mappedBy = "gameDraft")
//    private Set<GamePriceEntity> prices;

    static GameDraftEntity from(GameDraftDetails gameDetails) {
        return new GameDraftEntity(
                gameDetails.getId(),
                gameDetails.getTitle(),
                gameDetails.getProducer(),
                gameDetails.getBasedOnGameId()
        );
    }

    GameDraftDetails toGameDetails() {
        return GameDraftDetails.restoreFrom(id, basedOnGameId, title, producer);
    }

}
