package com.bartz24.externaltweaker.app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.bartz24.externaltweaker.app.data.ETRecipeData;
import com.bartz24.externaltweaker.app.model.FluidRegistry;
import com.bartz24.externaltweaker.app.model.ItemRegistry;
import com.bartz24.externaltweaker.app.model.OreDictRegistry;
import com.bartz24.externaltweaker.app.model.RecipeRegistry;

public class DataService {

    public void loadData(File etdFile, File oredictCsv) throws Exception {
        if (etdFile == null || !etdFile.exists()) {
            throw new Exception("Data file not found: " + etdFile);
        }

        try (FileInputStream fis = new FileInputStream(etdFile);
                ObjectInputStream ois = new ObjectInputStream(fis)) {

            // Read objects in order: Items, Fluids, OreDicts, Recipes
            Object[][] itemMappings = (Object[][]) ois.readObject();
            Object[][] fluidMappings = (Object[][]) ois.readObject();
            Object[][] oreDictMappings = (Object[][]) ois.readObject();
            List<ETRecipeData> recipeData = (ArrayList<ETRecipeData>) ois.readObject();

            // Populate Registries
            ItemRegistry.getInstance().loadFromLegacyArray(itemMappings);
            FluidRegistry.getInstance().loadFromLegacyArray(fluidMappings);

            // OreDicts: Load from legacy array first (for display data), then CSV if
            // available
            OreDictRegistry.getInstance().loadFromLegacyArray(oreDictMappings);
            if (oredictCsv != null && oredictCsv.exists()) {
                OreDictRegistry.getInstance().loadFromCsv(oredictCsv);
            }

            RecipeRegistry.getInstance().setRecipes(recipeData);

        } catch (Exception e) {
            throw e; // Re-throw to let caller handle UI error display
        }
    }
}
