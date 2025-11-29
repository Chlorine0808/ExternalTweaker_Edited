package com.bartz24.externaltweaker.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidRegistry {
    private static FluidRegistry instance;

    private List<FluidData> fluids = new ArrayList<>();
    private Map<String, FluidData> fluidMap = new HashMap<>();

    private FluidRegistry() {
    }

    public static FluidRegistry getInstance() {
        if (instance == null) {
            instance = new FluidRegistry();
        }
        return instance;
    }

    public void addFluid(FluidData fluid) {
        fluids.add(fluid);
        fluidMap.put(fluid.getName(), fluid);
    }

    public void clear() {
        fluids.clear();
        fluidMap.clear();
    }

    public FluidData getFluid(String name) {
        return fluidMap.get(name);
    }

    public List<FluidData> getAllFluids() {
        return new ArrayList<>(fluids);
    }

    public void loadFromLegacyArray(Object[][] data) {
        clear();
        if (data == null)
            return;

        for (Object[] row : data) {
            if (row.length >= 2) {
                String name = (String) row[0];
                String displayName = (String) row[1];
                addFluid(new FluidData(name, displayName));
            }
        }
    }

    public Object[][] toLegacyArray() {
        Object[][] data = new Object[fluids.size()][2];
        for (int i = 0; i < fluids.size(); i++) {
            FluidData fluid = fluids.get(i);
            data[i][0] = fluid.getName();
            data[i][1] = fluid.getDisplayName();
        }
        return data;
    }
}
