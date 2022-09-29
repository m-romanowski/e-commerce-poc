package dev.marcinromanowski.gamescatalog

import dev.marcinromanowski.base.ClockFixture
import dev.marcinromanowski.common.Price
import dev.marcinromanowski.gamescatalog.dto.GameDetailsDto
import dev.marcinromanowski.gamescatalog.exception.CannotFindGameDraftException
import dev.marcinromanowski.gamescatalog.exception.GamesCatalogInconsistencyStateException
import org.hibernate.search.jpa.FullTextEntityManager
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class GamesCatalogFacadeSpec extends Specification implements ClockFixture {

    private GamesCatalogFacade gamesCatalogFacade

    def setup() {
        def gamesCatalogDraftsRepository = new InMemoryGamesCatalogDraftsRepository()
        def gamesCatalogRepository = new InMemoryGamesCatalogRepository()
        def configuration = new GamesCatalogConfiguration()
        gamesCatalogFacade = configuration.gamesCatalogFacade(clock(), Mock(FullTextEntityManager), gamesCatalogDraftsRepository, gamesCatalogRepository)
    }

    def "New game (not existing) should be marked as a draft"() {
        given: "prepared by user game details"
            def addGameDraftCommand = addGameToCatalogCommand("The Witcher", new Price(2.0, "EUR"))

        when: "user tries to add a new draft"
            gamesCatalogFacade.createNewGameDraft(addGameDraftCommand)
        then: "draft is successfully saved"
            verifyAll(gamesCatalogFacade.getAllDrafts()) {
                it.size() == 1
                it[0].id() != null
                it[0].title() == addGameDraftCommand.title()
                it[0].producer() == addGameDraftCommand.producer()
//                it[0].publishedAt() != null
//                it[0].modifiedAt() != null
//                it[0].createdBy() == addGameDraftCommand.createdBy()
//                it[0].prices().size() == 1
//                it[0].prices()[0].amount() == addGameDraftCommand.prices()[0].amount()
//                it[0].prices()[0].currencyCode() == addGameDraftCommand.prices()[0].currencyCode()
            }
        and: "game is not published to catalog"
            gamesCatalogFacade
                    .getGamesCatalog(Pageable.unpaged())
                    .isEmpty()
    }

    def "It should be possible to create a new draft from existing game in catalog"() {
        given: "existing game in catalog"
            def addGameDraftCommand = addGameToCatalogCommand("Cyberpunk 2077", new Price(50.0, "EUR"))
            def gameDetails = createDraftAndPublishGameInCatalog(addGameDraftCommand)

        when: "user requests for a new draft"
            def draft = gamesCatalogFacade.createGameDraftFromCatalog(gameDetails.id())
        then: "draft is initialized with game catalog details"
            verifyAll(draft) {
                it.id() != null
                it.title() == gameDetails.title()
                it.producer() == gameDetails.producer()
            }
    }

    def "It should be possible to create many drafts for the same product"() {
        when: "user add two drafts"
            def firstAddGameDraftCommand = addGameToCatalogCommand("The Witcher", new Price(2.0, "EUR"))
            def secondAddGameDraftCommand = addGameToCatalogCommand("The Witcher", [new Price(2.0, "EUR"), new Price(1.0, "PLN")] as Set)
            gamesCatalogFacade.createNewGameDraft(firstAddGameDraftCommand)
            gamesCatalogFacade.createNewGameDraft(secondAddGameDraftCommand)
        then: "drafts exists in repository"
            gamesCatalogFacade.getAllDrafts().size() == 2
    }

    def "Published game should be accessible to preview for users"() {
        given: "prepared by user game draft already exists"
            def addGameDraftCommand = addGameToCatalogCommand("Cyberpunk 2077", new Price(50.0, "EUR"))
            gamesCatalogFacade.createNewGameDraft(addGameDraftCommand)
            def drafts = gamesCatalogFacade.getAllDrafts()
            assert drafts.size() == 1

        when: "user publishes his draft"
            gamesCatalogFacade.publishGameToCatalog(drafts[0].id())
        then: "published game is visible in catalog"
            verifyAll(gamesCatalogFacade.getGamesCatalog(Pageable.unpaged())) {
                it.size() == 1
                it[0].id() != null
                it[0].title() == addGameDraftCommand.title()
                it[0].producer() == addGameDraftCommand.producer()
                it[0].publishedAt() != null
//                it[0].price().amount() == 50.0
//                it[0].price().currencyCode() == "EUR"
            }
        and: "draft is not removed from repository"
            gamesCatalogFacade.getAllDrafts().size() == 1
    }

    def "It should be possible to delete live product from catalog"() {
        given: "published game in catalog"
            def addGameDraftCommand = addGameToCatalogCommand("Cyberpunk 2077", new Price(50.0, "EUR"))
            def gameDetails = createDraftAndPublishGameInCatalog(addGameDraftCommand)

        expect: "there is only one game in catalog"
            gamesCatalogFacade
                    .getGamesCatalog(Pageable.unpaged())
                    .size() == 1

        when: "user tries to delete game from catalog"
            gamesCatalogFacade.deleteGameFromCatalog(gameDetails.id())
        then: "game is not accessible from catalog"
            gamesCatalogFacade
                    .getGamesCatalog(Pageable.unpaged())
                    .isEmpty()
        and: "draft still exists"
            gamesCatalogFacade.getAllDrafts().size() == 1
    }

    def "An exception should be thrown if user try to remove live draft"() {
        given: "published game in catalog"
            def addGameDraftCommand = addGameToCatalogCommand("Cyberpunk 2077", new Price(50.0, "EUR"))
            createDraftAndPublishGameInCatalog(addGameDraftCommand)

        expect: "there is one game in catalog"
            def drafts = gamesCatalogFacade.getAllDrafts()
            drafts.size() == 1
            gamesCatalogFacade.getGamesCatalog(Pageable.unpaged()).size() == 1

        when: "user tries remove live draft"
            gamesCatalogFacade.deleteGameDraft(drafts[0].id())
        then: "exception is thrown"
            def e = thrown(GamesCatalogInconsistencyStateException)
            e.message.contains("Games catalog is in inconsistency state")
    }

    def "An exception should be thrown if we try to publish non existing draft"() {
        expect: "there is no any drafts"
            gamesCatalogFacade.getAllDrafts().isEmpty()

        when: "user tries to publish not existing draft"
            def notExistingDraftId = UUID.randomUUID()
            gamesCatalogFacade.publishGameToCatalog(notExistingDraftId)
        then: "exception is thrown"
            def e = thrown(CannotFindGameDraftException)
            e.message.contains("Cannot find game draft, id: $notExistingDraftId")
    }

    def "An exception should be thrown if we try to modify non existing draft"() {
        expect: "there is no any drafts"
            gamesCatalogFacade.getAllDrafts().isEmpty()

        when: "user tries to modify not existing draft"
            def notExistingDraftId = UUID.randomUUID()
            def modificationCommand = new GamesCatalogFacade.ModifyGameFromCatalogCommand(notExistingDraftId, "Terraria", "Re-Logic")
            gamesCatalogFacade.modifyGameDraft(modificationCommand)
        then: "exception is thrown"
            def e = thrown(CannotFindGameDraftException)
            e.message.contains("Cannot find game draft, id: $notExistingDraftId")
    }

    private GamesCatalogFacade.AddGameToCatalogCommand addGameToCatalogCommand(String title, Price price) {
        return addGameToCatalogCommand(title, [price] as Set)
    }

    private GamesCatalogFacade.AddGameToCatalogCommand addGameToCatalogCommand(String title, Set<Price> prices) {
        return new GamesCatalogFacade.AddGameToCatalogCommand(title, "Game producer")
    }

    private GameDetailsDto createDraftAndPublishGameInCatalog(GamesCatalogFacade.AddGameToCatalogCommand command) {
        def draft = gamesCatalogFacade.createNewGameDraft(command)
        def gameId = gamesCatalogFacade.publishGameToCatalog(draft.id())
        def gameDetails = gamesCatalogFacade
                .getGamesCatalog(Pageable.unpaged())
                .find {
                    it.id() == gameId
                }
        assert gameDetails != null
        return gameDetails
    }

    private final class InMemoryGamesCatalogDraftsRepository implements GamesCatalogDraftsRepository {

        private final Map<UUID, GameDraftDetails> gameDraftDetailsCollector = new ConcurrentHashMap<>()

        @Override
        GameDraftDetails save(GameDraftDetails gameDetails) {
            def gameDraftDetailsWithId = GameDraftDetails.restoreFrom(
                    UUID.randomUUID(),
                    gameDetails.basedOnGameId,
                    gameDetails.title,
                    gameDetails.producer
            )
            gameDraftDetailsCollector.put(gameDraftDetailsWithId.id, gameDraftDetailsWithId)
            return gameDraftDetailsWithId
        }

        @Override
        Optional<GameDraftDetails> findById(UUID id) {
            return Optional.ofNullable(gameDraftDetailsCollector.find { it.key == id })
                    .map { it.value }
        }

        @Override
        List<GameDraftDetails> findAll() {
            return gameDraftDetailsCollector.values()
                    .collect()
        }

        @Override
        void deleteById(UUID draftId) {
            gameDraftDetailsCollector.remove(draftId)
        }

    }

    private final class InMemoryGamesCatalogRepository implements GamesCatalogRepository {

        private final Map<UUID, GameDetails> gameDetailsCollector = new ConcurrentHashMap<>()

        @Override
        GameDetails save(GameDetails gameDetails) {
            def gameDetailsWIthId = GameDetails.restoreFrom(
                    UUID.randomUUID(),
                    gameDetails.draftDetails,
                    gameDetails.publishedAt
            )
            gameDetailsCollector.put(gameDetailsWIthId.id, gameDetailsWIthId)
            return gameDetailsWIthId
        }

        @Override
        Optional<GameDetails> findById(UUID id) {
            return Optional.ofNullable(gameDetailsCollector.find { it.key == id })
                    .map { it.value }
        }

        @Override
        List<GameDetails> findAll(Pageable pageable) {
            return gameDetailsCollector.values()
                    .collect()
        }

        @Override
        void deleteById(UUID gameId) {
            gameDetailsCollector.remove(gameId)
        }

        @Override
        Optional<GameDetails> findByDraftId(UUID draftId) {
            return Optional.ofNullable(gameDetailsCollector.find { it.value.draftDetails.id == draftId })
                    .map { it.value }
        }
    }

}
