package com.bartz24.externaltweaker.app.model;

import java.util.ArrayList;
import java.util.List;

import com.bartz24.externaltweaker.app.data.ETRecipeData;

public class RecipeRegistry {
    private static RecipeRegistry instance;

    private List<ETRecipeData> recipes = new ArrayList<>();

    private RecipeRegistry() {
    }

    public static RecipeRegistry getInstance() {
        if (instance == null) {
            instance = new RecipeRegistry();
        }
        return instance;
    }

    public void setRecipes(List<ETRecipeData> recipes) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
    }

    public List<ETRecipeData> getRecipes() {
        return recipes;
    }

    public void addRecipe(ETRecipeData recipe) {
        recipes.add(recipe);
    }

    public void clear() {
        recipes.clear();
    }
}
