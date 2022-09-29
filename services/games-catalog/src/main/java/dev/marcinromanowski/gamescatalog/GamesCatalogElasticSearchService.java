package dev.marcinromanowski.gamescatalog;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
class GamesCatalogElasticSearchService implements GamesCatalogSearchService {

    private static final String TITLE_QUERY_FIELD_NAME = "liveDraft.title";

    private final FullTextEntityManager fullTextEntityManager;

    @SuppressWarnings("unchecked")
    @Override
    public List<GameDetails> findAllContainingTitle(String title, Pageable pageable) {
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(GameEntity.class)
                .get();
        Query titleQuery = queryBuilder.keyword().onField(TITLE_QUERY_FIELD_NAME).matching(title).createQuery();
        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(titleQuery, GameEntity.class);
        fullTextQuery.setFirstResult(pageable.getPageNumber());
        fullTextQuery.setMaxResults(pageable.getPageSize());
        return ((List<GameEntity>) fullTextQuery.getResultList())
                .stream()
                .map(GameEntity::toGameDetails)
                .toList();
    }

}
