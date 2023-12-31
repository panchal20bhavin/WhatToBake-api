package com.whattobake.api.Service;

import com.whattobake.api.Dto.InsertDto.RatingInsertRequest;
import com.whattobake.api.Exception.NodeNotFound;
import com.whattobake.api.Model.Recipe;
import com.whattobake.api.Repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    public Mono<Recipe> addRating(RatingInsertRequest rating, Long recipeId, String pbUid) {
        return ratingRepository.addRating(rating, recipeId, pbUid)
                .switchIfEmpty(Mono.error(new NodeNotFound("Recipe with given id: "+recipeId+" does not exist")));
    }

    public Mono<Void> deleteRating(Long recipeId, String pbUid) {
        return ratingRepository.deleteRating(recipeId, pbUid).flatMap(result ->{
            if(!result) return Mono.error(new NodeNotFound("Recipe with given id: "+recipeId+" does not exist"));
            else return Mono.empty();
        });
    }

    public Flux<Recipe> getRated(Long page, String pbUid) {
        return ratingRepository.ratedRecipes(page, pbUid);
    }

    public Mono<Map<Long, Long>> getRatedShort(String pbId) {
        return ratingRepository.ratedRecipesShort(pbId);
    }
}
