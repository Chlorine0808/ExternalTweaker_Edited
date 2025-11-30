package com.bartz24.externaltweaker.app.data;

import java.util.List;

public class ImportedData {
    public Object[][] itemMappings;
    public Object[][] fluidMappings;
    public Object[][] oreDictMappings;
    public List<ETRecipeData> recipeData;

    public ImportedData(Object[][] itemMappings, Object[][] fluidMappings, Object[][] oreDictMappings,
            List<ETRecipeData> recipeData) {
        this.itemMappings = itemMappings;
        this.fluidMappings = fluidMappings;
        this.oreDictMappings = oreDictMappings;
        this.recipeData = recipeData;
    }
}
