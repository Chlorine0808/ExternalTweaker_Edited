package com.bartz24.externaltweaker.app.model;

import java.io.Serializable;

public class FluidData implements Serializable {
    private String name; // Internal name (e.g. "water")
    private String displayName; // Display name (e.g. "Water")

    public FluidData(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName + " (" + name + ")";
    }
}
