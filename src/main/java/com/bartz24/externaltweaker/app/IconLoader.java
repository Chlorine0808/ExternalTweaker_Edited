package com.bartz24.externaltweaker.app;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconLoader {
    private File iconDir;
    private Map<String, ImageIcon> iconCache = new HashMap<>();

    public IconLoader(File iconDir) {
        this.iconDir = iconDir;
    }

    public ImageIcon loadIcon(String id, String name, int size) {
        if (iconDir == null)
            return null;

        // Cache key includes size to allow different sizes for same ID
        String cacheKey = id + "@" + size;

        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }

        List<String> candidates = new ArrayList<>();

        // Priority 1: Exact Name match (e.g. "Apple.png")
        if (name != null && !name.isEmpty()) {
            candidates.add(name + ".png");
            // Handle cases where name has special chars that might be replaced in filename
            candidates.add(name.replace(":", "_") + ".png");
        }

        String cleanId = Utils.unformatItemId(id);
        String[] parts = cleanId.split(":");
        if (parts.length >= 2) {
            String modid = parts[0];
            String nameFromId = parts[1];
            String meta = parts.length > 2 ? parts[2] : null;

            candidates.add(modid + "_" + nameFromId + ".png");
            candidates.add(nameFromId + ".png");
            if (meta != null && !meta.equals("*")) {
                candidates.add(modid + "_" + nameFromId + "_" + meta + ".png");
                candidates.add(nameFromId + "_" + meta + ".png");
            }
        }

        ImageIcon icon = null;
        for (String filename : candidates) {
            File f = new File(iconDir, filename);
            // System.out.println("Checking icon: " + f.getAbsolutePath());
            if (f.exists()) {
                try {
                    java.awt.image.BufferedImage img = ImageIO.read(f);
                    Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                    System.out.println("Icon loaded: " + f.getName() + " for " + id);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (icon == null) {
            System.out.println("Icon NOT found for: " + id + " (Clean: " + cleanId + ")");
        }

        iconCache.put(cacheKey, icon);
        return icon;
    }
}
