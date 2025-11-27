package com.bartz24.externaltweaker.app.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;

public class ShapedCraftingHandler implements RecipeHandler {

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

        StringBuilder gridBuilder = new StringBuilder("[");
        for (int y = 0; y < 3; y++) {
            gridBuilder.append("[");
            for (int x = 0; x < 3; x++) {
                String item = panel.getGridItem(x, y);
                gridBuilder.append(item != null && !item.isEmpty() ? item : "null");
                if (x < 2)
                    gridBuilder.append(", ");
            }
            gridBuilder.append("]");
            if (y < 2)
                gridBuilder.append(", ");
        }
        gridBuilder.append("]");
        recipe.setParameterData(1, gridBuilder.toString());
    }

    private String[][] parseGridData(String data) {
        if (data == null || data.isEmpty() || data.equals("null"))
            return new String[0][0];

        // Simple parsing for [[item, item, item], [item, item, item], [item, item,
        // item]]
        // This is a simplified parser and might need to be more robust like
        // PanelArrayParam's parser
        // reusing logic from PanelArrayParam would be ideal but it's private.
        // For now, let's try a basic approach assuming standard formatting.

        try {
            String content = data.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
            }

            String[] rows = content.split(Pattern.quote("], ["));
            String[][] result = new String[rows.length][];

            for (int i = 0; i < rows.length; i++) {
                String row = rows[i].trim();
                if (row.startsWith("["))
                    row = row.substring(1);
                if (row.endsWith("]"))
                    row = row.substring(0, row.length() - 1);

                // Split by comma, but need to be careful about commas inside functions/brackets
                // For this MVP, we'll assume basic items.
                // TODO: Use a proper parser if complex ingredients are used.
                String[] items = row.split(", ");
                result[i] = items;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[3][3];
        }
    }
}
