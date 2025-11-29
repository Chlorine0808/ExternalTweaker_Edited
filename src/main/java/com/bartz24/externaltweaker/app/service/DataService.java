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

    public void saveData(File etdFile, List<ETRecipeData> recipes, boolean[] settings) throws Exception {
        if (etdFile == null) {
            throw new Exception("Save file cannot be null");
        }

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(etdFile);
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos)) {

            // Write objects in order: Items, Fluids, OreDicts, Recipes
            if (settings[1])
                oos.writeObject(ItemRegistry.getInstance().toLegacyArray());
            else
                oos.writeObject(new Object[0][0]);

            if (settings[2])
                oos.writeObject(FluidRegistry.getInstance().toLegacyArray());
            else
                oos.writeObject(new Object[0][0]);

            if (settings[3])
                oos.writeObject(OreDictRegistry.getInstance().toLegacyArray());
            else
                oos.writeObject(new Object[0][0]);

            if (settings[0])
                oos.writeObject(new ArrayList<>(recipes));
            else
                oos.writeObject(new ArrayList<>());

        } catch (Exception e) {
            throw new Exception("Failed to save data: " + e.getMessage(), e);
        }
    }

    public void mergeAndSaveData(File etdFile, List<ETRecipeData> recipes, boolean[] settings) throws Exception {
        if (etdFile == null || !etdFile.exists()) {
            saveData(etdFile, recipes, settings);
            return;
        }

        // Read existing data
        List<Object[]> iMap;
        List<Object[]> fMap;
        List<Object[]> oMap;
        List<ETRecipeData> rList;

        try (FileInputStream fis = new FileInputStream(etdFile);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            iMap = new ArrayList<>(java.util.Arrays.asList((Object[][]) ois.readObject()));
            fMap = new ArrayList<>(java.util.Arrays.asList((Object[][]) ois.readObject()));
            oMap = new ArrayList<>(java.util.Arrays.asList((Object[][]) ois.readObject()));
            rList = (ArrayList<ETRecipeData>) ois.readObject();
        }

        // Merge current data
        if (settings[1]) {
            Object[][] currentItems = ItemRegistry.getInstance().toLegacyArray();
            for (Object[] row : currentItems) {
                boolean contains = false;
                for (Object[] existing : iMap) {
                    if (row[0].equals(existing[0])) {
                        contains = true;
                        break;
                    }
                }
                if (!contains)
                    iMap.add(row);
            }
        }

        if (settings[2]) {
            Object[][] currentFluids = FluidRegistry.getInstance().toLegacyArray();
            for (Object[] row : currentFluids) {
                boolean contains = false;
                for (Object[] existing : fMap) {
                    if (row[0].equals(existing[0])) {
                        contains = true;
                        break;
                    }
                }
                if (!contains)
                    fMap.add(row);
            }
        }

        if (settings[3]) {
            Object[][] currentOreDicts = OreDictRegistry.getInstance().toLegacyArray();
            for (Object[] row : currentOreDicts) {
                boolean contains = false;
                for (Object[] existing : oMap) {
                    if (row[0].equals(existing[0])) {
                        contains = true;
                        break;
                    }
                }
                if (!contains)
                    oMap.add(row);
            }
        }

        if (settings[0]) {
            for (ETRecipeData recipe : recipes) {
                boolean contains = false;
                for (ETRecipeData existing : rList) {
                    if (existing.getRecipeFormat().equals(recipe.getRecipeFormat())) {
                        contains = true;
                        // Update param names if missing
                        for (int x = 0; x < existing.getParameterTypes().length; x++) {
                            if ((existing.getParamName(x) == null || existing.getParamName(x).isEmpty()))
                                existing.setParamName(x, recipe.getParamName(x));
                        }
                        break;
                    }
                }
                if (!contains)
                    rList.add(recipe);
            }
        }

        // Write back merged data
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(etdFile);
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos)) {
            oos.writeObject(iMap.toArray(new Object[iMap.size()][2]));
            oos.writeObject(fMap.toArray(new Object[fMap.size()][2]));
            oos.writeObject(oMap.toArray(new Object[oMap.size()][2]));
            oos.writeObject(rList);
        } catch (Exception e) {
            throw new Exception("Failed to save merged data: " + e.getMessage(), e);
        }
    }
}
