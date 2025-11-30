package com.bartz24.externaltweaker.app.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;

import com.bartz24.externaltweaker.app.IconLoader;
import com.bartz24.externaltweaker.app.Utils;
import com.bartz24.externaltweaker.app.ui.renderer.IconRenderer;

public class TableController {

    public void loadTable(JTable table, String type, String filter, Object[][] itemMappings, Object[][] fluidMappings,
            Object[][] oreDictMappings, List<String> blacklist, IconLoader iconLoader) {
        List<RowSorter.SortKey> keys = table.getRowSorter() != null
                ? ((DefaultRowSorter) table.getRowSorter()).getSortKeys()
                : new ArrayList<RowSorter.SortKey>();
        Object[][] array = type.equals("Items") ? itemMappings
                : type.equals("Fluids") ? fluidMappings : type.equals("Ore Dict") ? oreDictMappings : null;
        if (array == null)
            return;

        List<Integer> indexesValid = new ArrayList<Integer>();
        for (int i = 0; i < array.length; i++) {
            String cleanName = Utils.stripFormatting(array[i][1].toString());

            boolean blacklisted = false;
            for (String s : blacklist) {
                if (array[i][0].toString().toLowerCase().contains(s) || cleanName.toLowerCase().contains(s)) {
                    blacklisted = true;
                    break;
                }
            }
            if (blacklisted)
                continue;

            if (!(filter == null || filter.isEmpty())) {
                if (array[i][0].toString().toLowerCase().contains(filter) || cleanName.toLowerCase().contains(filter))
                    indexesValid.add(i);
            } else {
                indexesValid.add(i);
            }
        }
        Object[][] newArray = new Object[indexesValid.size()][2];
        for (int i = 0; i < indexesValid.size(); i++) {
            newArray[i][0] = array[indexesValid.get(i)][0];
            newArray[i][1] = Utils.stripFormatting(array[indexesValid.get(i)][1].toString());
        }
        array = newArray;

        Arrays.sort(array, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                String id1 = (String) o1[0];
                String id2 = (String) o2[0];

                String mod1 = "", name1 = "";
                int meta1 = 0;
                try {
                    String clean1 = id1.replace("<", "").replace(">", "");
                    String[] parts1 = clean1.split(":");
                    if (parts1.length > 0)
                        mod1 = parts1[0];
                    if (parts1.length > 1)
                        name1 = parts1[1];
                    if (parts1.length > 2 && !parts1[2].equals("*"))
                        meta1 = Integer.parseInt(parts1[2]);
                } catch (Exception e) {
                }

                String mod2 = "", name2 = "";
                int meta2 = 0;
                try {
                    String clean2 = id2.replace("<", "").replace(">", "");
                    String[] parts2 = clean2.split(":");
                    if (parts2.length > 0)
                        mod2 = parts2[0];
                    if (parts2.length > 1)
                        name2 = parts2[1];
                    if (parts2.length > 2 && !parts2[2].equals("*"))
                        meta2 = Integer.parseInt(parts2[2]);
                } catch (Exception e) {
                }

                int modCompare = mod1.compareToIgnoreCase(mod2);
                if (modCompare != 0)
                    return modCompare;

                int nameCompare = name1.compareToIgnoreCase(name2);
                if (nameCompare != 0)
                    return nameCompare;

                return Integer.compare(meta1, meta2);
            }
        });

        table.setModel(new DefaultTableModel(array, new String[] { "ID", "Name" }));
        if (table.getColumnCount() > 2) {
            table.getColumnModel().removeColumn(table.getColumnModel().getColumn(2));
        }
        if (table.getRowSorter() != null) {
            DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
            sorter.setSortKeys(keys);
            sorter.sort();
        }

        // table.clearSelection();

        if (iconLoader != null) {
            table.getColumnModel().getColumn(1).setCellRenderer(new IconRenderer(iconLoader));
            table.setRowHeight(36); // Make rows taller for icons
        }
    }
}
