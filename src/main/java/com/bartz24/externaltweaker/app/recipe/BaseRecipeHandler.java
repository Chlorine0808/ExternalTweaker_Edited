package com.bartz24.externaltweaker.app.recipe;

import java.util.regex.Pattern;

import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;

public abstract class BaseRecipeHandler implements RecipeHandler {

    /**
     * Parses a 2D array string representation (e.g. [[item1, item2], [item3,
     * item4]])
     * into a String[][] array.
     * 
     * @param data The string data to parse.
     * @return A 2D array of strings.
     */
    protected String[][] parseGridData(String data) {
        if (data == null || data.isEmpty() || data.equals("null"))
            return new String[0][0];

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

                String[] items = row.split(", ");
                result[i] = items;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[3][3];
        }
    }

    /**
     * Formats a 2D grid of items into a string representation for saving.
     * 
     * @param panel The crafting panel to read from.
     * @return The formatted string.
     */
    protected String formatGridData(PanelCraftingRecipe panel) {
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
        return gridBuilder.toString();
    }
}
