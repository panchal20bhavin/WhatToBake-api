package com.whattobake.api.Repository.Implementations;

import com.whattobake.api.Dto.InsertDto.RatingInsertRequest;
import com.whattobake.api.Mapers.RecipeMapper;
import com.whattobake.api.Model.Recipe;
import com.whattobake.api.Repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RatingRepositoryImpl implements RatingRepository{

    private final RecipeMapper recipeMapper;

    private final ReactiveNeo4jClient client;

    @Override
    public Mono<Recipe> addRating(RatingInsertRequest rating, Long recipeId, String pbUid) {
        String q = """
            MATCH (recipe:RECIPE) WHERE ID(recipe) = $rid
            MERGE (user:USER{pbId:$pbId})
            WITH user, recipe
            MERGE (user)-[rate:RATING]->(recipe)
            ON CREATE
            SET rate.date = datetime()
            SET rate.stars = $stars
            WITH recipe
            RETURN""";
        return recipeMapper.resultAsMono(recipeMapper.getMapperQuery(q), Map.of(
                "pbId",pbUid,
                "rid", recipeId,
                "stars", rating.getStars()));
    }

    @Override
    public Mono<Boolean> deleteRating(Long recipeId, String pbUid) {
        String q = """
            MATCH (user:USER{pbId: $pbId})
            MATCH (recipe:RECIPE) WHERE ID(recipe) = $rid
            MATCH (user)-[rate:RATING]->(recipe)
            DELETE rate;
        """;
        return client.query(q).bindAll(Map.of("pbId",pbUid,"rid", recipeId)).run()
                .map(resultSummary -> resultSummary.counters().relationshipsDeleted() != 0);
    }

    @Override
    public Flux<Recipe> ratedRecipes(Long page, String pbUid) {
        String q = """
            MATCH (user:USER{pbId:$pbId})-[r:RATING]->(recipe:RECIPE)
            RETURN""" + RecipeMapper.RETURN + """
            ORDER BY r.date DESC
            """;
        return recipeMapper.resultAsFlux(recipeMapper.getMapperQueryNoAddon(q), Map.of("pbId",pbUid));
    }

    @Override
    public Mono<Map<Long, Long>> ratedRecipesShort(String pbId) {
        String q = """
            MATCH (user:USER{pbId:$pbId})-[r:RATING]->(recipe:RECIPE)
            RETURN ID(recipe) as key, r.stars as value
            """;
        return client.query(q)
                .bind(pbId).to("pbId")
                .fetch()
                .all()
                .collectMap(
                        record -> (Long) record.get("key"),
                        record -> (Long) record.get("value")
                );
    }
}
