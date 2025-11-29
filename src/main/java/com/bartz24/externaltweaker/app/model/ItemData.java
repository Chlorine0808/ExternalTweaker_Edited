package com.bartz24.externaltweaker.app.model;

import java.io.Serializable;

public class ItemData implements Serializable {
    private String id;
    private String name;
    private String modId; // Optional, derived from ID

    public ItemData(String id, String name) {
        this.id = id;
        this.name = name;
        this.modId = parseModId(id);
    }

    private String parseModId(String id) {
        if (id != null && id.contains(":")) {
            return id.split(":")[0];
        }
        return "minecraft"; // Default or unknown
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModId() {
        return modId;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
