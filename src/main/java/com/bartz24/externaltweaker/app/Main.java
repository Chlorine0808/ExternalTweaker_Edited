package com.bartz24.externaltweaker.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bartz24.externaltweaker.app.data.ETRecipeData;

public class Main {

	private static final String CONFIG_FILE = "config.properties";
	private static final String KEY_ETD_PATH = "etdPath";
	private static final String KEY_ICON_PATH = "iconPath";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Properties props = new Properties();
		File etdFile = null;
		File iconDir = null;

		// Load config
		try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
			props.load(in);
			String etdPath = props.getProperty(KEY_ETD_PATH);
			String iconPath = props.getProperty(KEY_ICON_PATH);

			if (etdPath != null) {
				File f = new File(etdPath);
				if (f.exists() && f.isFile()) {
					etdFile = f;
				}
			}
			if (iconPath != null) {
				File f = new File(iconPath);
				if (f.exists() && f.isDirectory()) {
					iconDir = f;
				}
			}
		} catch (Exception e) {
			// Ignore
		}

		if (etdFile == null) {
			java.awt.FileDialog fileDialog = new java.awt.FileDialog((java.awt.Frame) null, "Select .etd Data File",
					java.awt.FileDialog.LOAD);
			fileDialog.setDirectory(System.getProperty("user.dir"));
			fileDialog.setVisible(true);

			if (fileDialog.getFile() != null) {
				etdFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
			} else {
				System.exit(0);
			}
		}

		if (iconDir == null) {
			int result = JOptionPane.showConfirmDialog(null,
					"Do you want to select an Icon Directory?\n(Select 'No' to skip icon loading)",
					"Select Icon Directory", JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null,
						"Please select any image file inside the Icon Directory (minecraft/dumps/itempanel_icons).\nThe parent folder will be used as the icon source.",
						"Select Icon Directory", JOptionPane.INFORMATION_MESSAGE);

				java.awt.FileDialog iconDialog = new java.awt.FileDialog((java.awt.Frame) null,
						"Select any image in Icon Directory", java.awt.FileDialog.LOAD);
				iconDialog.setFile("*.png");
				iconDialog.setVisible(true);

				if (iconDialog.getFile() != null) {
					iconDir = new File(iconDialog.getDirectory());
				}
			}
		}

		// Save config
		if (etdFile != null) {
			props.setProperty(KEY_ETD_PATH, etdFile.getAbsolutePath());
			if (iconDir != null) {
				props.setProperty(KEY_ICON_PATH, iconDir.getAbsolutePath());
			}
			try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
				props.store(out, "ExternalTweaker Configuration");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			FileInputStream fis = new FileInputStream(etdFile);
			ObjectInputStream ois = new ObjectInputStream(fis);

			Object[][] itemMappings = (Object[][]) ois.readObject();
			Object[][] fluidMappings = (Object[][]) ois.readObject();
			Object[][] oreDictMappings = (Object[][]) ois.readObject();
			List<ETRecipeData> recipeData = (ArrayList) ois.readObject();

			List<String> methodList = new ArrayList<>();
			for (ETRecipeData data : recipeData) {
				methodList.add(data.getRecipeFormat());
			}

			ois.close();
			fis.close();

			AppFrame frame = new AppFrame(itemMappings, fluidMappings, oreDictMappings, methodList, iconDir);
			frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Error loading file: " + (e.getMessage() != null ? e.getMessage() : e.toString()), "Error",
					JOptionPane.ERROR_MESSAGE);
			// If error, maybe clear config?
			new File(CONFIG_FILE).delete();
		}
	}
}
