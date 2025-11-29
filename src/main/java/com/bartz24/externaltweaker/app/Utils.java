package com.bartz24.externaltweaker.app;

public class Utils {

    /**
     * Ensures the given item ID is wrapped in angle brackets <>.
     * If the ID is null, returns null.
     * If the ID is "null" (string), returns "null".
     * 
     * @param id The item ID to format.
     * @return The formatted item ID with angle brackets.
     */
    public static String formatItemId(String id) {
        if (id == null) {
            return null;
        }
        if (id.equals("null")) {
            return id;
        }
        String formatted = id.trim();
        if (formatted.startsWith("<")) {
            return formatted;
        }
        formatted = "<" + formatted;
        if (!formatted.endsWith(">")) {
            formatted = formatted + ">";
        }
        return formatted;
    }

    /**
     * Removes angle brackets from the given item ID.
     * 
     * @param id The item ID to unformat.
     * @return The item ID without angle brackets.
     */
    public static String unformatItemId(String id) {
        if (id == null) {
            return null;
        }
        String unformatted = id.trim();
        if (unformatted.startsWith("<") && unformatted.endsWith(">")) {
            return unformatted.substring(1, unformatted.length() - 1);
        }
        return unformatted;
    }

    public static String stripFormatting(String input) {
        if (input == null)
            return null;
        return input.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "");
    }

    public static String getNameFromId(String id) {
        String name = null;
        String formattedId = formatItemId(id);
        String unformattedId = unformatItemId(id);

        // Check Items
        com.bartz24.externaltweaker.app.model.ItemData item = com.bartz24.externaltweaker.app.model.ItemRegistry
                .getInstance().getItem(formattedId);
        if (item != null) {
            name = item.getName();
        }

        // Check Fluids
        if (name == null) {
            com.bartz24.externaltweaker.app.model.FluidData fluid = com.bartz24.externaltweaker.app.model.FluidRegistry
                    .getInstance().getFluid(formattedId);
            if (fluid != null) {
                name = fluid.getDisplayName();
            }
        }

        // Check OreDict
        if (name == null) {
            // OreDictRegistry stores by name (e.g. "logWood"), but ID might be
            // "ore:logWood"
            String oreName = unformattedId;
            if (oreName.startsWith("ore:")) {
                oreName = oreName.substring(4);
            }
            com.bartz24.externaltweaker.app.model.OreDictData od = com.bartz24.externaltweaker.app.model.OreDictRegistry
                    .getInstance().getOreDict(oreName);
            if (od != null) {
                name = od.getName();
            }
        }

        return stripFormatting(name);
    }

    public static String getOreDictRepresentativeItem(String oreName) {
        if (oreName == null)
            return null;

        String name = oreName;
        if (name.startsWith("<") && name.endsWith(">")) {
            name = name.substring(1, name.length() - 1);
        }
        if (name.startsWith("ore:")) {
            name = name.substring(4);
        }

        com.bartz24.externaltweaker.app.model.OreDictData data = com.bartz24.externaltweaker.app.model.OreDictRegistry
                .getInstance().getOreDict(name);
        if (data != null) {
            return data.getRepresentativeItem();
        }
        return null;
    }
}
