
package com.bartz24.externaltweaker.app.controller;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Strings;
import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.data.ETRecipeData;
import com.bartz24.externaltweaker.app.data.ETScript;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

public class AppController {
    private AppFrame view;

    public AppController() {
    }

    public void setView(AppFrame view) {
        this.view = view;
    }

    public void help() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Bartz24/ExternalTweaker/wiki"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void about() {
        JOptionPane.showMessageDialog(view,
                "External Tweaker by Bartz24\n\n" + "A tool to easily create scripts for Minetweaker/Crafttweaker.\n"
                        + "Supports adding/removing recipes, and Ore Dictionary entries.\n\n"
                        + "Version: 0.4.1 (Modified)",
                "About External Tweaker", JOptionPane.INFORMATION_MESSAGE);
    }

    public void download() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Bartz24/ExternalTweaker/releases"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void newScript() {
        String fileName = JOptionPane.showInputDialog(view, "New script file name", "New Script.zs");
        if ((fileName == null || fileName.isEmpty()))
            return;
        if (!fileName.trim().endsWith(".zs"))
            fileName = fileName.trim() + ".zs";
        view.scripts.add(new ETScript("", fileName));
        view.updateScriptsList(false);
        view.comboScripts.setSelectedIndex(view.scripts.size() - 1);
        view.comboRecipes.setSelectedIndex(-1);
        view.updateRecipesList(false);
        view.comboRecipes.setSelectedIndex(-1);
        view.updateCurrentRecipe();
        view.btnNewRecipe
                .setEnabled(view.comboScripts.getSelectedIndex() >= 0 && view.listMethods.getModel().getSize() > 0);
    }

    public void deleteScript() {
        if (view.scripts.size() > 0 && view.comboScripts.getSelectedIndex() >= 0) {
            int dialogResult = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to delete this script? \n \n This will also delete the actual file!",
                    "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                if (!(view.getCurrentScript().filePath == null || view.getCurrentScript().filePath.isEmpty())) {
                    new File(view.getCurrentScript().filePath + File.separator + view.getCurrentScript().fileName)
                            .delete();
                }
                view.scripts.remove(view.comboScripts.getSelectedIndex());
                view.comboScripts.setSelectedIndex(-1);
                view.updateScriptsList(false);
                view.comboScripts.setSelectedIndex(-1);
                view.comboRecipes.setSelectedIndex(-1);
                view.updateRecipesList(false);
                view.comboRecipes.setSelectedIndex(-1);
                view.updateCurrentRecipe();
            }
        }
    }

    public void renameFile() {
        ETScript script = view.getCurrentScript();
        if (script != null) {
            String fileName = JOptionPane.showInputDialog(view, "Rename script file name", script.fileName);
            if (fileName == null)
                return;
            if (!fileName.trim().endsWith(".zs"))
                fileName = fileName.trim() + ".zs";
            if (!Strings.isNullOrEmpty(script.filePath)) {
                File file = new File(script.filePath + File.separator + script.fileName);
                if (!file.renameTo(new File(script.filePath + File.separator + fileName)))
                    JOptionPane.showOptionDialog(view, "Failed to rename script!", "Error!", JOptionPane.OK_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
            }
            script.fileName = fileName;
            view.updateScriptsList(false);
        }
    }

    public void saveScript(ETScript script) {
        if ((script.filePath == null || script.filePath.isEmpty())) {
            java.awt.FileDialog fd = new java.awt.FileDialog(view, "Save Script", java.awt.FileDialog.SAVE);
            fd.setDirectory(System.getProperty("user.dir"));
            fd.setFile(script.fileName);
            fd.setVisible(true);

            if (fd.getFile() != null) {
                script.filePath = fd.getDirectory();
                script.fileName = fd.getFile();
                if (!script.fileName.endsWith(".zs"))
                    script.fileName += ".zs";
            } else {
                return;
            }
        }

        BufferedWriter writer = null;
        try {

            writer = new BufferedWriter(new FileWriter(new File(script.filePath + File.separator + script.fileName)));

            writer.write("# CREATED USING EXTERNAL TWEAKER\n");

            for (ETActualRecipe r : script.recipes) {
                int index = view.indexOfRecipeFormat(r.getRecipeFormat());
                if (index >= 0) {
                    if (!view.recipeData.get(index).isAddRecipe()) {
                        writer.write(r.recipeToString(view.recipeData.get(index)) + "\n");
                    }
                }
            }

            writer.write("\n");

            for (ETActualRecipe r : script.recipes) {
                int index = view.indexOfRecipeFormat(r.getRecipeFormat());
                if (index >= 0) {
                    if (view.recipeData.get(index).isAddRecipe()) {
                        writer.write(r.recipeToString(view.recipeData.get(index)) + "\n");
                    }
                } else
                    writer.write(r.recipeToString(view.recipeData.get(index)) + "\n");

            }

            writer.close();

            JOptionPane.showOptionDialog(view, "Script saved successfully!", "Saved", JOptionPane.OK_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null, new Object[] { "OK" }, "OK");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showOptionDialog(view, e.getLocalizedMessage(), "Error! Report this issue if you can!",
                    JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
        }

    }

    public void openScripts() {
        File[] files = null;
        java.awt.FileDialog fd = new java.awt.FileDialog(view, "Open Scripts", java.awt.FileDialog.LOAD);
        fd.setMultipleMode(true);
        fd.setDirectory(System.getProperty("user.dir"));
        fd.setVisible(true);
        files = fd.getFiles();

        if (files == null || files.length == 0)
            return;

        List<File> allScripts = new ArrayList<File>();
        for (File f : files) {
            allScripts.addAll(f.isDirectory() ? getScripts(f) : Collections.singletonList(f));
        }
        boolean ignoreErrors = false;
        for (File f : allScripts) {
            ETScript script = new ETScript(
                    f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator)),
                    f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separator) + 1));

            List<String> lines = new ArrayList<String>();
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showOptionDialog(view, e.getLocalizedMessage(), "Error! Report this issue if you can!",
                        JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
            }
            boolean skippingScript = false;
            int lineNum = 0;
            String lastLine = "";
            HashMap<String, String> variables = new HashMap<String, String>();
            for (String string : lines) {
                try {
                    lineNum++;
                    if ((string != null && !string.isEmpty()) && !string.startsWith("#") && !string.startsWith("//")
                            && !string.startsWith("/*") && !string.endsWith("*/")) {
                        if (!(lastLine + string).endsWith(";")) {
                            lastLine += string;
                            continue;
                        }
                        String s = lastLine + string;
                        lastLine = "";
                        if (s.startsWith("val ") && s.contains("=")) {
                            String[] variable = s.trim().substring(4, s.trim().length() - 1).split("=");
                            variables.put(variable[0].trim(), variable[1].trim());
                            continue;
                        }
                        ETRecipeData d = view.findRecipeDataForRecipe(s);
                        if (d != null) {
                            String params = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")"));
                            HashMap<Integer, String> arrayIndexes = new HashMap<Integer, String>();
                            List<String> paramList = new ArrayList<String>();
                            boolean varGoThroughAgain = true;
                            while (varGoThroughAgain) {
                                varGoThroughAgain = false;
                                boolean changedSomething = true;
                                while (changedSomething) {
                                    changedSomething = false;
                                    while (params.contains(".withTag(") && params.contains(")")
                                            && params.indexOf(".withTag(") < params.indexOf(")")
                                            && !params.substring(params.indexOf(".withTag("), params.indexOf(")") + 1)
                                                    .contains(".onlyWithTag(")) {
                                        String arg = params.substring(params.indexOf(".withTag("),
                                                params.indexOf(")") + 1);
                                        params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                        arrayIndexes.put(arrayIndexes.size(), arg);
                                        changedSomething = true;
                                    }
                                    if (!changedSomething)
                                        while (params.contains("\"")
                                                && params.substring(params.indexOf("\"") + 1).contains("\"")) {
                                            String arg = params.substring(params.indexOf("\""),
                                                    params.indexOf("\"", params.indexOf("\"") + 1) + 1);
                                            params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                            arrayIndexes.put(arrayIndexes.size(), arg);
                                            changedSomething = true;
                                        }
                                    if (!changedSomething)
                                        while (params.contains(".onlyWithTag(") && params.contains(")")
                                                && params.indexOf(".onlyWithTag(") < params.indexOf(")")) {
                                            String arg = params.substring(params.indexOf(".onlyWithTag("),
                                                    params.indexOf(")") + 1);
                                            params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                            arrayIndexes.put(arrayIndexes.size(), arg);
                                            changedSomething = true;
                                        }
                                    if (!changedSomething)
                                        while (params.contains("<") && params.contains(">")
                                                && params.indexOf("<") < params.indexOf(">")) {
                                            String arg = params.substring(params.indexOf("<"), params.indexOf(">") + 1);
                                            params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                            arrayIndexes.put(arrayIndexes.size(), arg);
                                            changedSomething = true;
                                        }
                                    if (!changedSomething)
                                        for (String var : variables.keySet()) {
                                            while (params.contains(var)) {
                                                varGoThroughAgain = true;
                                                for (String var2 : variables.keySet()) {
                                                    String[] splitVar = variables.get(var).split("\\.");
                                                    if (!var.equals(var2) && splitVar.length > 1
                                                            && splitVar[0].equals(var2)) {
                                                        splitVar[0] = variables.get(var2);
                                                        String arg = "";
                                                        for (int i = 0; i < splitVar.length; i++) {
                                                            arg += splitVar[i];
                                                            if (i < splitVar.length - 1)
                                                                arg += ".";
                                                        }
                                                        params = params.replace(var, arg);
                                                        changedSomething = true;
                                                    }
                                                }
                                                if (changedSomething)
                                                    break;
                                                String arg = variables.get(var);
                                                params = params.replace(var, "~" + arrayIndexes.size() + "~");
                                                arrayIndexes.put(arrayIndexes.size(), arg);
                                                changedSomething = true;
                                            }
                                            if (changedSomething)
                                                break;
                                        }
                                    if (!changedSomething)
                                        while (params.contains("[[") && params.contains("]]")
                                                && params.indexOf("[[") < params.indexOf("]]")) {
                                            String arg = params.substring(params.indexOf("[["),
                                                    params.indexOf("]]") + 2);
                                            params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                            arrayIndexes.put(arrayIndexes.size(), arg);
                                            changedSomething = true;

                                        }
                                    if (!changedSomething)
                                        while (params.contains("[") && params.contains("]")
                                                && params.indexOf("[") < params.indexOf("]")) {
                                            String arg = params.substring(params.indexOf("["), params.indexOf("]") + 1);
                                            params = params.replace(arg, "~" + arrayIndexes.size() + "~");
                                            arrayIndexes.put(arrayIndexes.size(), arg);
                                            changedSomething = true;
                                        }
                                }

                                paramList = new ArrayList<String>(Arrays.asList(params.split(",")));
                                for (int i = 0; i < paramList.size(); i++) {
                                    paramList.set(i, paramList.get(i).trim());
                                    boolean changed = true;
                                    String p = paramList.get(i).substring(0, paramList.get(i).length());
                                    while (changed) {
                                        changed = false;
                                        for (int i2 = 0; i2 < arrayIndexes.size(); i2++) {
                                            if (p.contains("~" + i2 + "~")) {
                                                p = p.replace("~" + i2 + "~", arrayIndexes.get(i2));
                                                changed = true;
                                            }
                                        }
                                    }
                                    paramList.set(i, p);
                                }
                            }

                            while (paramList.size() < d.getParameterCount()) {
                                paramList.add("~");
                            }
                            script.recipes.add(new ETActualRecipe(view.findRecipeDataForRecipe(s).getRecipeFormat(),
                                    paramList.toArray(new String[paramList.size()])));
                        } else {
                            if (!ignoreErrors) {
                                int result = JOptionPane.showOptionDialog(view,
                                        "The line: " + s + " in " + script.fileName + " at line " + lineNum
                                                + " \n could not be loaded into External Tweaker. Make sure to import all the recipes you need first.\n Any recipes not loaded will be lost if you DO save over the script. \n Make a backup of the script if you have parts you want to keep.",
                                        "Script Loading Error", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                                        new Object[] { "Cancel Loading Of Scripts", "Skip loading this Script",
                                                "Ignore Once and Continue", "Ignore Everything and Continue" },
                                        "Ignore Once and Continue");
                                if (result == 0)
                                    return;
                                else if (result == 1) {
                                    skippingScript = true;
                                    break;
                                } else if (result == 3) {
                                    ignoreErrors = true;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showOptionDialog(view,
                            e.getLocalizedMessage() + " when loading " + script.fileName + " at line " + lineNum,
                            "Error! Report this issue if you can!", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, new Object[] { "OK" }, "OK");
                    return;
                }
            }
            if (skippingScript)
                continue;
            boolean replaced = false;
            for (int i = 0; i < view.scripts.size(); i++) {
                if (view.scripts.get(i).fileName.equals(script.fileName)
                        && view.scripts.get(i).filePath.equals(script.filePath)) {
                    view.scripts.set(i, script);
                    replaced = true;
                }
            }
            if (!replaced)
                view.scripts.add(script);
            view.updateScriptsList(false);
            view.comboScripts.setSelectedIndex(0);
            view.comboRecipes.setSelectedIndex(-1);
            view.updateRecipesList(false);
            view.comboRecipes.setSelectedIndex(-1);
            view.updateCurrentRecipe();
            view.btnNewRecipe
                    .setEnabled(view.comboScripts.getSelectedIndex() >= 0 && view.listMethods.getModel().getSize() > 0);

        }
    }

    public List<File> getScripts(File... files) {
        List<File> scriptFiles = new ArrayList();
        for (File file : files) {
            if (file.isDirectory()) {
                scriptFiles.addAll(getScripts(file.listFiles()));
            } else if (file.getAbsolutePath().endsWith(".zs")) {
                scriptFiles.add(file);
            }
        }
        return scriptFiles;
    }

    public void saveAllScripts() {
        for (ETScript s : view.scripts)
            saveScript(s);
    }
}
