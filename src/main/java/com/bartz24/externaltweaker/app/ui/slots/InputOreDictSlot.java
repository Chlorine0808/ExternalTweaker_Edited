package com.bartz24.externaltweaker.app.ui.slots;

import java.util.List;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;
import com.bartz24.externaltweaker.app.model.OreDictRegistry;

public class InputOreDictSlot extends AbstractItemSlot {

    public InputOreDictSlot(AppFrame appFrame) {
        super(appFrame);
    }

    private String originalItem;

    @Override
    public void setContent(String id) {
        super.setContent(id);
        if (id != null && !id.equals("null") && !id.isEmpty()) {
            String raw = Utils.unformatItemId(id);
            if (!raw.startsWith("ore:")) {
                this.originalItem = Utils.formatItemId(id);
            } else if (this.originalItem == null) {
                // If we set content to an ore: ID and don't have an original item,
                // try to find a representative item to be the "original"
                String rep = Utils.getOreDictRepresentativeItem(id);
                if (rep != null) {
                    this.originalItem = Utils.formatItemId(rep);
                }
            }
        }
    }

    @Override
    public boolean isItemValid(String id) {
        if (id == null || id.equals("null") || id.isEmpty())
            return true;

        String raw = Utils.unformatItemId(id);
        // Does not allow Fluid
        if (raw.startsWith("liquid:"))
            return false;

        return true; // Accepts everything else including OreDict
    }

    public void cycleItem(boolean next) {
        String current = getContent();
        if (current == null || current.equals("null") || current.isEmpty())
            return;

        // 1. Determine the base item ID
        String baseItem = current;
        String rawCurrent = Utils.unformatItemId(current);

        if (rawCurrent.startsWith("ore:")) {
            // Try to restore original item if it matches this OreDict
            boolean restored = false;
            if (originalItem != null && !originalItem.equals("null")) {
                List<String> ores = OreDictRegistry.getInstance().getOreDictsForItem(originalItem);
                for (String ore : ores) {
                    if (Utils.formatItemId("ore:" + ore).equals(Utils.formatItemId(current))) {
                        baseItem = originalItem;
                        restored = true;
                        break;
                    }
                }
            }

            if (!restored) {
                // If we can't restore from originalItem, try to find a representative item
                String rep = Utils.getOreDictRepresentativeItem(current);
                if (rep != null) {
                    baseItem = rep;
                } else {
                    // If we still can't find a base item, we can't cycle
                    return;
                }
            }
        }

        // 2. Get all OreDicts for the base item
        List<String> ores = OreDictRegistry.getInstance().getOreDictsForItem(baseItem);

        // 3. Create options list: [BaseItem, <ore:Dict1>, <ore:Dict2>, ...]
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add(Utils.formatItemId(baseItem));
        for (String ore : ores) {
            // Check if this OreDict has any items registered
            // If the list of items for this OreDict is empty, don't add it to options
            // We can check this by getting items for the OreDict name
            List<String> items = OreDictRegistry.getInstance().getItemsForOreDict(ore);
            if (items != null && !items.isEmpty()) {
                options.add(Utils.formatItemId("ore:" + ore));
            }
        }

        if (options.size() <= 1)
            return;

        // 4. Find current index and cycle
        int index = options.indexOf(current);
        if (index == -1) {
            // If current is not in options (e.g. it was an OreDict that we filtered out or
            // logic mismatch),
            // default to 0 (Base Item)
            index = 0;
        }

        if (next) {
            index++;
            if (index >= options.size())
                index = 0;
        } else {
            index--;
            if (index < 0)
                index = options.size() - 1;
        }

        // We don't want to overwrite originalItem when cycling,
        // because setContent logic might try to reset it if we pass an ore: ID.
        // But our setContent logic only sets originalItem if it's NOT an ore: ID,
        // OR if originalItem is null. Since we have a baseItem here, originalItem
        // should be set/preserved.
        // However, if we cycle to an ore: ID, setContent will see it starts with ore:
        // and won't overwrite originalItem
        // unless originalItem is null.
        setContent(options.get(index));
    }

}
