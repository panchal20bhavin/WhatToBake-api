package com.whattobake.api.Controller;

import com.whattobake.api.Dto.FilterDto.RecipeFilters;
import com.whattobake.api.Dto.InfoDto.RecipeInfo;
import com.whattobake.api.Dto.InsertDto.RecipeInsertRequest;
import com.whattobake.api.Dto.UpdateDto.RecipeUpdateRequest;
import com.whattobake.api.Exception.RecipeNotFoundException;
import com.whattobake.api.Model.Recipe;
import com.whattobake.api.Service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/info")
    public Mono<RecipeInfo> info(@RequestBody Optional<RecipeFilters> recipeFilters){
        return recipeService.info(recipeFilters.orElse(new RecipeFilters()).fillDefaults());
    }

    @GetMapping("/")
    public Flux<Recipe> getAllRecipes(@RequestBody Optional<RecipeFilters> recipeFilters){
        return recipeService.getAllRecipes(recipeFilters.orElse(new RecipeFilters()).fillDefaults());
    }

    @GetMapping("/{id}")
    public Mono<Recipe> getOneById(@PathVariable("id") Long id){
        return recipeService.getOneById(id)
                .switchIfEmpty(Mono.error(new RecipeNotFoundException("Recipe with given id: "+id+" does not exist")));
    }

    @PostMapping("/")
    public Mono<Recipe> newRecipe(@RequestBody RecipeInsertRequest recipeInsertRequest){
        return recipeService.newRecipe(recipeInsertRequest);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteRecipe(@PathVariable("id") Long id){
        return recipeService.deleteRecipe(id);
    }

    @PutMapping("/{id}")
    public Mono<Recipe> updateRecipe(@PathVariable("id") Long id, @RequestBody RecipeInsertRequest recipeInsertRequest){
        return recipeService.updateRecipe(RecipeUpdateRequest.builder()
                        .id(id)
                        .title(recipeInsertRequest.getTitle())
                        .image(recipeInsertRequest.getImage())
                        .link(recipeInsertRequest.getLink())
                        .products(recipeInsertRequest.getProducts())
                .build())
                .switchIfEmpty(Mono.error(new RecipeNotFoundException("Recipe with given id: "+id+" does not exist")));
    }
}
