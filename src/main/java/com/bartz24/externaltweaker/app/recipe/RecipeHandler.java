package com.bartz24.externaltweaker.app.recipe;

import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;

public interface RecipeHandler {
    boolean matches(String recipeMethod);

    void load(ETActualRecipe recipe, PanelCraftingRecipe panel);

    void save(PanelCraftingRecipe panel, ETActualRecipe recipe);
}
