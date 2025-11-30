package com.bartz24.externaltweaker.app.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JTable;

import com.bartz24.externaltweaker.app.data.ETRecipeData;

public class FileExportService {

    public void writeTableToFile(JTable table, File file, String type) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("TABLE DATA FROM EXTERNAL TWEAKER (" + type + ")\n");

            for (int i = 0; i < table.getRowCount(); i++) {
                writer.write(table.getValueAt(i, 0) + ": " + table.getValueAt(i, 1) + "\n");
            }
        }
    }

    public void writeRecipesToFile(List<ETRecipeData> recipes, File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("RECIPE DATA FROM EXTERNAL TWEAKER\n");

            for (int i = 0; i < recipes.size(); i++) {
                writer.write(recipes.get(i).getRecipeFormat() + "\n");
            }
        }
    }
}
