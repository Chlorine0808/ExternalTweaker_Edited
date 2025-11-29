package com.bartz24.externaltweaker.app.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;

public class ShapedCraftingHandler extends BaseRecipeHandler {

    @Override
    public boolean matches(String recipeMethod) {
        return recipeMethod.startsWith("recipes.addShaped");
    }

    @Override
    public void load(ETActualRecipe recipe, PanelCraftingRecipe panel) {
        // Param 0: Output (IItemStack)
        panel.setOutputItem(recipe.getParameterData(0));

        // Param 1: Inputs (IIngredient[][])
        String inputData = recipe.getParameterData(1);
        String[][] gridData = parseGridData(inputData);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (y < gridData.length && x < gridData[y].length) {
                    panel.setGridItem(x, y, gridData[y][x]);
                } else {
                    panel.setGridItem(x, y, "null");
                }
            }
        }
    }

    @Override
    public void save(PanelCraftingRecipe panel, ETActualRecipe recipe) {
        recipe.setParameterData(0, panel.getOutputItem());
        recipe.setParameterData(1, formatGridData(panel));
    }
}
