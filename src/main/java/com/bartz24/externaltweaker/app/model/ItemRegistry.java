package com.bartz24.externaltweaker.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRegistry {
    private static ItemRegistry instance;

    private List<ItemData> items = new ArrayList<>();
    private Map<String, ItemData> itemMap = new HashMap<>();

    private ItemRegistry() {
    }

    public static ItemRegistry getInstance() {
        if (instance == null) {
            instance = new ItemRegistry();
        }
        return instance;
    }

    public void addItem(ItemData item) {
        items.add(item);
        itemMap.put(item.getId(), item);
    }

    public void clear() {
        items.clear();
        itemMap.clear();
    }

    public ItemData getItem(String id) {
        return itemMap.get(id);
    }

    public List<ItemData> getAllItems() {
        return new ArrayList<>(items);
    }

    // Helper for fuzzy search or unformatted ID lookup if needed
    public ItemData findItem(String id) {
        if (itemMap.containsKey(id)) {
            return itemMap.get(id);
        }
        // Add logic for unformatted ID lookup if necessary
        return null;
    }

    public void loadFromLegacyArray(Object[][] data) {
        clear();
        if (data == null)
            return;

        for (Object[] row : data) {
            if (row.length >= 2) {
                String id = (String) row[0];
                String name = (String) row[1];
                addItem(new ItemData(id, name));
            }
        }
    }

    public Object[][] toLegacyArray() {
        Object[][] data = new Object[items.size()][2];
        for (int i = 0; i < items.size(); i++) {
            ItemData item = items.get(i);
            data[i][0] = item.getId();
            data[i][1] = item.getName();
        }
        return data;
    }
}
