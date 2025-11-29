package com.bartz24.externaltweaker.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OreDictData implements Serializable {
    private String name; // e.g. "logWood" (without ore: prefix)
    private String representativeItem; // Item ID to use as icon
    private List<String> items = new ArrayList<>(); // List of Item IDs in this OreDict

    public OreDictData(String name, String representativeItem) {
        this.name = name;
        this.representativeItem = representativeItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepresentativeItem() {
        return representativeItem;
    }

    public void setRepresentativeItem(String representativeItem) {
        this.representativeItem = representativeItem;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public void addItem(String itemId) {
        if (!items.contains(itemId)) {
            items.add(itemId);
        }
    }

    @Override
    public String toString() {
        return "ore:" + name;
    }
}
