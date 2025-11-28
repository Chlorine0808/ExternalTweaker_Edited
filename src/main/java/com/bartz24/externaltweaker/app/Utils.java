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
}
