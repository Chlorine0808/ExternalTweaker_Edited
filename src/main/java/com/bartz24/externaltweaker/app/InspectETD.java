package com.bartz24.externaltweaker.app;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InspectETD {
    public static void main(String[] args) {
        String[] files = { "etVer4.etd", "ExtData.etd", "externalTweaker.etd" };

        for (String filename : files) {
            File f = new File(filename);
            if (!f.exists()) {
                System.out.println("File not found: " + filename);
                continue;
            }

            System.out.println("==========================================");
            System.out.println("Inspecting: " + filename);
            System.out.println("==========================================");

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                // Order: itemMappings, fluidMappings, oreDictMappings, recipeData

                Object obj1 = ois.readObject();
                printMapping("Item Mappings", obj1);

                Object obj2 = ois.readObject();
                printMapping("Fluid Mappings", obj2);

                Object obj3 = ois.readObject();
                printMapping("OreDict Mappings", obj3);

                Object obj4 = ois.readObject();
                System.out.println(
                        "Recipe Data: " + (obj4 instanceof List ? ((List) obj4).size() + " entries" : "Unknown type"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("\n");
        }
    }

    private static void printMapping(String label, Object obj) {
        if (obj instanceof Object[][]) {
            Object[][] map = (Object[][]) obj;
            System.out.println(label + ": " + map.length + " entries");
            if (map.length > 0) {
                System.out.println("  Sample (first 5):");
                for (int i = 0; i < Math.min(5, map.length); i++) {
                    System.out.println("    " + Arrays.toString(map[i]));
                }
            }
        } else {
            System.out.println(label + ": Unknown type " + obj.getClass().getName());
        }
    }
}
