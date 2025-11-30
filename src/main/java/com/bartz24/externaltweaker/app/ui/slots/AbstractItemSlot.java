package com.bartz24.externaltweaker.app.ui.slots;

import javax.swing.ImageIcon;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;

public abstract class AbstractItemSlot extends AbstractSlot {
    protected AppFrame appFrame;
    protected String currentId;

    public AbstractItemSlot(AppFrame appFrame) {
        super();
        this.appFrame = appFrame;
    }

    @Override
    public void setContent(String id) {
        this.currentId = Utils.formatItemId(id);
        updateIcon(this.currentId);
        updateTooltip(this.currentId);
    }

    @Override
    public String getContent() {
        return currentId;
    }

    @Override
    public void updateIcon(String id) {
        if (id == null || id.equals("null") || id.isEmpty()) {
            setIcon(null);
            setText("");
            return;
        }

        String name = Utils.getNameFromId(id);
        if (name == null || name.isEmpty()) {
            name = id;
            if (id.contains(":")) {
                String[] parts = id.split(":");
                if (parts.length > 1)
                    name = parts[1];
            }
        }

        setText(name);

        if (appFrame.iconLoader != null) {
            String iconItem = id;
            if (Utils.unformatItemId(id).startsWith("ore:")) {
                String rep = Utils.getOreDictRepresentativeItem(id);
                if (rep != null)
                    iconItem = rep;
            }

            String iconName = name;
            if (!iconItem.equals(id)) {
                String resolvedName = Utils.getNameFromId(iconItem);
                if (resolvedName != null && !resolvedName.isEmpty()) {
                    iconName = resolvedName;
                }
            }

            ImageIcon icon = appFrame.iconLoader.loadIcon(iconItem, iconName, 64); // Default size 64
            setIcon(icon);
        }
    }

    protected void updateTooltip(String id) {
        setToolTipText(id);
    }
}
