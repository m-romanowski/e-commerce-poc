package dev.marcinromanowski

import com.jayway.jsonpath.JsonPath
import dev.marcinromanowski.base.AuthFixture
import dev.marcinromanowski.base.IntegrationSpec
import dev.marcinromanowski.base.RandomizerFixture
import org.hamcrest.Matchers
import org.springframework.test.web.servlet.ResultActions

import static dev.marcinromanowski.infrastructure.web.GameCatalogDraftsController.AddGameDraftFormRequest
import static dev.marcinromanowski.infrastructure.web.GameCatalogDraftsController.ModifyGameDraftFormRequest
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AcceptanceIntegrationSpec extends IntegrationSpec implements AuthFixture {

    private String user1, user2

    def setup() {
        user1 = RandomizerFixture.randomUserId("user1")
        user2 = RandomizerFixture.randomUserId("user2")
    }

    // TODO: removal endpoints
    def "Acceptance test"() {
        given: "users access tokens"
            def user1AccessToken = generateAdminToken(user1)
            def user2AccessToken = generateUserToken(user2)

        expect: "games catalog is empty"
            fetchGamesCatalog()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$').isEmpty())
        and: "there is no drafts"
            fetchGameDraftsCatalog(user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$').isEmpty())

        when: "administrator creates a new game draft"
            def newGameDraft = new AddGameDraftFormRequest("The Witcher", "CD Project RED")
            def draftCreationResult = createNewGameDraft(newGameDraft, user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.id').isNotEmpty())
                    .andExpect(jsonPath('$.title').value("The Witcher"))
                    .andExpect(jsonPath('$.producer').value("CD Project RED"))
                    .andReturn()
            def draftId = JsonPath.parse(draftCreationResult.getResponse().getContentAsString())
                    .read('$.id', String)

        then: "game draft is available"
            fetchGameDraftsCatalog(user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.[0].id').value(draftId))

        when: "administrator tries to modify game draft"
            def modificationRequest = new ModifyGameDraftFormRequest(UUID.fromString(draftId), "The Witcher 2", "CD Project RED")
            modifyGameDraft(modificationRequest, user1AccessToken)
                    .andExpect(status().isOk())
        then: "game draft is changed"
            fetchGameDraftsCatalog(user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.length()').value(1))
                    .andExpect(jsonPath('$.[0].id').value(draftId))
                    .andExpect(jsonPath('$.[0].title').value(modificationRequest.title()))
                    .andExpect(jsonPath('$.[0].producer').value(modificationRequest.producer()))

        when: "administrator publishes changes"
            def publicationResult = publishGameDraft(draftId, user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$').isNotEmpty())
                    .andReturn()
            def gameId = JsonPath.parse(publicationResult.getResponse().getContentAsString())
                    .read('$', String)
        then: "game draft goes live to game catalog"
            fetchGamesCatalog()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.length()').value(1))
                    .andExpect(jsonPath('$.[0].id').value(gameId))
                    .andExpect(jsonPath('$.[0].title').value(modificationRequest.title()))
                    .andExpect(jsonPath('$.[0].producer').value(modificationRequest.producer()))

        when: "administrator creates a new draft from existing game in catalog"
            def draftBasedOnCatalog = createNewGameDraftBasedOnCatalog(gameId, user1AccessToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.id').value(Matchers.not(draftId)))
                    .andExpect(jsonPath('$.title').value(modificationRequest.title()))
                    .andExpect(jsonPath('$.producer').value(modificationRequest.producer()))
                    .andReturn()
            def newDraftId = JsonPath.parse(draftBasedOnCatalog.getResponse().getContentAsString())
                    .read('$.id', String)
        and: "modifying data"
            def newModificationRequest = new ModifyGameDraftFormRequest(UUID.fromString(newDraftId), "The Witcher 3", "Unknown")
            modifyGameDraft(newModificationRequest, user1AccessToken)
                    .andExpect(status().isOk())
        and: "publishes changes"
            publishGameDraft(newDraftId, user1AccessToken)
                    .andExpect(status().isOk())
        then: "live game is changed in catalog"
            fetchGamesCatalog()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.length()').value(1))
                    .andExpect(jsonPath('$.[0].id').isNotEmpty())
                    .andExpect(jsonPath('$.[0].title').value(newModificationRequest.title()))
                    .andExpect(jsonPath('$.[0].producer').value(newModificationRequest.producer()))

        when: "administrator publishes another game to catalog"
            def secondProductDraft = createNewGameDraft(new AddGameDraftFormRequest("Assassin's creed", "Ubisoft"), user1AccessToken)
                    .andExpect(status().isOk())
                    .andReturn()
            def secondProductDraftId = JsonPath.parse(secondProductDraft.getResponse().getContentAsString())
                    .read('$.id', String)
            publishGameDraft(secondProductDraftId, user1AccessToken)
        then: "user fetch two games in catalog"
            fetchGamesCatalog()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.length()').value(2))

        when: "user tries to search game by title"
            def searchingResult = searchInGamesCatalog("witcher")
        then: "user got game from game catalog"
            searchingResult
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.length()').value(1))
                    .andExpect(jsonPath('$.[0].title').value("The Witcher 3"))

        when: "user2 tries to access to private endpoint"
            def result = fetchGameDraftsCatalog(user2AccessToken)
        then: "403 FORBIDDEN status code is returned"
            result.andExpect(status().isForbidden())
    }

    private static String valueAsStringOrNull(Object value) {
        return value == null ? null : /"$value"/
    }

    private static String addGameDraftFormAsJson(AddGameDraftFormRequest request) {
        return """
            {
                "title": "${request.title()}",
                "producer": "${request.producer()}"
            }
        """
    }

    private static String modifyGameDraftFormAsJson(ModifyGameDraftFormRequest request) {
        return """
            {
                "id": "${request.id()}",
                "title": "${request.title()}",
                "producer": "${request.producer()}"
            }
        """
    }

    private ResultActions fetchGamesCatalog() {
        return mockMvc.perform(get("/api/v1/game-catalog")
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions searchInGamesCatalog(String title) {
        return mockMvc.perform(get("/api/v1/game-catalog")
                .param("search", title)
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions fetchGameDraftsCatalog(String accessToken) {
        return mockMvc.perform(get("/api/v1/game-catalog/draft")
                .header("Authorization", "Bearer $accessToken")
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions createNewGameDraft(AddGameDraftFormRequest request, String accessToken) {
        return mockMvc.perform(post("/api/v1/game-catalog/draft/new")
                .header("Authorization", "Bearer $accessToken")
                .content(addGameDraftFormAsJson(request))
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions createNewGameDraftBasedOnCatalog(String gameId, String accessToken) {
        return mockMvc.perform(post("/api/v1/game-catalog/draft/copy")
                .header("Authorization", "Bearer $accessToken")
                .param("id", gameId)
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions modifyGameDraft(ModifyGameDraftFormRequest request, String accessToken) {
        return mockMvc.perform(put("/api/v1/game-catalog/draft/modify")
                .header("Authorization", "Bearer $accessToken")
                .content(modifyGameDraftFormAsJson(request))
                .contentType(APPLICATION_JSON_VALUE))
    }

    private ResultActions publishGameDraft(String draftId, String accessToken) {
        return mockMvc.perform(post("/api/v1/game-catalog/draft/{draftId}/publish", draftId)
                .header("Authorization", "Bearer $accessToken"))
    }

}
