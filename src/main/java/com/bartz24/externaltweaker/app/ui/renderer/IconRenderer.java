package com.bartz24.externaltweaker.app.ui.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.bartz24.externaltweaker.app.IconLoader;
import com.bartz24.externaltweaker.app.Utils;

public class IconRenderer extends DefaultTableCellRenderer {
    private IconLoader iconLoader;

    public IconRenderer(IconLoader iconLoader) {
        this.iconLoader = iconLoader;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Object idObj = table.getValueAt(row, 0);
        Object nameObj = table.getValueAt(row, 1);
        if (idObj instanceof String) {
            String id = (String) idObj;
            String name = (nameObj instanceof String) ? (String) nameObj : "";
            String iconItem = id;

            // OreDict handling
            String rawId = Utils.unformatItemId(id);
            if (rawId.startsWith("ore:")) {
                String rep = null;
                // Try getting from model first
                int modelRow = table.convertRowIndexToModel(row);
                if (table.getModel().getColumnCount() > 2) {
                    Object val = table.getModel().getValueAt(modelRow, 2);
                    if (val instanceof String)
                        rep = (String) val;
                }

                if (rep == null)
                    rep = Utils.getOreDictRepresentativeItem(rawId);

                if (rep != null) {
                    iconItem = rep;
                }
                // System.out.println("Table Renderer: ID=" + id + ", Raw=" + rawId + ", Rep=" +
                // rep
                // + ", IconItem=" + iconItem);
            }

            // Use the name of the iconItem for lookup, not the display name of the row
            // (which might be the OreDict name)
            String iconName = name;
            if (!iconItem.equals(id)) {
                String resolvedName = Utils.getNameFromId(iconItem);
                if (resolvedName != null && !resolvedName.isEmpty()) {
                    iconName = resolvedName;
                }
            }

            if (iconLoader != null) {
                ImageIcon icon = iconLoader.loadIcon(iconItem, iconName, 32);
                label.setIcon(icon);
            }
        }
        return label;
    }
}
