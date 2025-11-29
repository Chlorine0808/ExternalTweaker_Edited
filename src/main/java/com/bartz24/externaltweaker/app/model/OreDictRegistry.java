package com.bartz24.externaltweaker.app.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bartz24.externaltweaker.app.Utils;

public class OreDictRegistry {
    private static OreDictRegistry instance;

    private Map<String, OreDictData> oreDictMap = new HashMap<>(); // Name -> Data
    private Map<String, List<String>> itemToOreDict = new HashMap<>(); // ItemID -> List<OreName>

    private OreDictRegistry() {
    }

    public static OreDictRegistry getInstance() {
        if (instance == null) {
            instance = new OreDictRegistry();
        }
        return instance;
    }

    public void clear() {
        oreDictMap.clear();
        itemToOreDict.clear();
    }

    public void addOreDict(OreDictData data) {
        oreDictMap.put(data.getName(), data);
        for (String itemId : data.getItems()) {
            addItemMapping(itemId, data.getName());
        }
    }

    private void addItemMapping(String itemId, String oreName) {
        if (!itemToOreDict.containsKey(itemId)) {
            itemToOreDict.put(itemId, new ArrayList<>());
        }
        List<String> list = itemToOreDict.get(itemId);
        if (!list.contains(oreName)) {
            list.add(oreName);
        }
    }

    public OreDictData getOreDict(String name) {
        return oreDictMap.get(name);
    }

    public List<String> getOreDictsForItem(String itemId) {
        return itemToOreDict.getOrDefault(itemId, Collections.emptyList());
    }

    public List<OreDictData> getAllOreDicts() {
        return new ArrayList<>(oreDictMap.values());
    }

    public void loadFromLegacyArray(Object[][] data) {
        // Only loads display info, cannot reconstruct item lists
        if (data == null)
            return;

        for (Object[] row : data) {
            if (row.length >= 3) {
                String key = (String) row[0]; // ore:name
                String name = (String) row[1]; // name
                String repItem = (String) row[2];

                if (!oreDictMap.containsKey(name)) {
                    oreDictMap.put(name, new OreDictData(name, repItem));
                }
            }
        }
    }

    public void loadFromCsv(File file) {
        if (file == null || !file.exists())
            return;

        clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                // Ore Name,ItemStack,Item ID,Display Name,Wildcard
                String[] parts = line.split(",");
                if (parts.length < 4)
                    continue;

                String oreNameFull = parts[0].trim(); // ore:logWood
                String itemId = Utils.formatItemId(parts[2].trim()); // minecraft:log

                String oreName = oreNameFull.replace("ore:", "").replace(">", "");

                OreDictData data = oreDictMap.get(oreName);
                if (data == null) {
                    // Use the first item found as representative initially
                    data = new OreDictData(oreName, itemId);
                    oreDictMap.put(oreName, data);
                }

                data.addItem(itemId);
                addItemMapping(itemId, oreName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
