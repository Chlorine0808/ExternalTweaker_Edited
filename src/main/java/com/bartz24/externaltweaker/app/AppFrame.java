package com.bartz24.externaltweaker.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import com.bartz24.externaltweaker.app.service.FileExportService;
import com.bartz24.externaltweaker.app.ui.renderer.IconRenderer;
import com.bartz24.externaltweaker.app.controller.AppController;
import com.bartz24.externaltweaker.app.controller.TableController;
import com.bartz24.externaltweaker.app.panels.PanelParameterEdit;
import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.data.ETRecipeData;
import com.bartz24.externaltweaker.app.data.ETScript;
import com.bartz24.externaltweaker.app.recipe.RecipeHandler;
import com.bartz24.externaltweaker.app.recipe.ShapedCraftingHandler;
import com.bartz24.externaltweaker.app.service.DataService;
import com.bartz24.externaltweaker.app.panels.PanelImportExportDialog;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;
import com.bartz24.externaltweaker.app.data.ImportedData;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AppFrame extends JFrame {
	Object[][] itemMappings;
	Object[][] fluidMappings;
	Object[][] oreDictMappings;

	public JTable table;
	public List<String> blacklist = new ArrayList<>();
	public JScrollPane tableScroll;
	public JList listMethods;
	public JScrollPane scrollMethods;
	public final ButtonGroup buttonGroup = new ButtonGroup();
	public List<PanelParameterEdit> paramPanels = new ArrayList();
	public List<ETRecipeData> recipeData = new ArrayList();
	public List<ETScript> scripts = new ArrayList();
	public List<RecipeHandler> recipeHandlers = new ArrayList();
	public JPanel pnlRecipeEdit;
	public JLabel labelRecipe;
	public JComboBox comboRecipes;
	public JRadioButton rdbtnItems;
	public JRadioButton rdbtnFluids;
	public JRadioButton rdbtnOreDict;
	public JPlaceholderTextField txtSearchTable;
	public JButton btnDeleteRecipe;
	public JButton btnDupeRecipe;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	public JRadioButton btnRecipeRemove;
	public JRadioButton btnRecipeAdd;
	private JTextField recipeDisplay;
	public boolean updatingParameters;
	public String copyData;
	public String copyType;
	public JLabel lblCopying;
	public JComboBox comboScripts;
	private JMenuItem mntmDeleteScript;
	private JMenuItem mntmSaveAllScripts;
	private JMenu menuHelp;
	private JMenuItem mntmHelp;
	private JMenuItem mntmDownload;
	public JButton btnNewRecipe;
	private JMenuItem mntmAbout;
	private JMenu mnOther;
	private JMenuItem mntmExportRecipesTo;
	private JMenuItem mntmExportCurrentTable;
	private JMenuItem mntmRenameCurrentScript;
	public JCheckBox chkVisualEditor;
	public IconLoader iconLoader;
	public AppController controller;
	public DataService dataService = new DataService();
	public TableController tableController = new TableController();
	public FileExportService fileExportService = new FileExportService();

	public File iconDir;

	public AppFrame(Object[][] itemMappings, Object[][] fluidMappings, Object[][] oreDictMappings,
			List<String> methods, File iconDir, File oredictCsv) {
		this.iconDir = iconDir;
		this.iconLoader = new IconLoader(iconDir);
		this.controller = new AppController();
		this.controller.setView(this);
		setTitle("External Tweaker");
		// setIconImage(Toolkit.getDefaultToolkit().getImage(AppFrame.class.getResource("/book_writable.png")));
		this.itemMappings = itemMappings;
		this.fluidMappings = fluidMappings;
		this.oreDictMappings = oreDictMappings;

		// Populate Registries for Utils usage
		com.bartz24.externaltweaker.app.model.ItemRegistry.getInstance().loadFromLegacyArray(itemMappings);
		com.bartz24.externaltweaker.app.model.FluidRegistry.getInstance().loadFromLegacyArray(fluidMappings);
		com.bartz24.externaltweaker.app.model.OreDictRegistry.getInstance().loadFromLegacyArray(oreDictMappings);

		this.setPreferredSize(new Dimension(1200, 800));

		this.blacklist = dataService.loadBlacklist();
		loadOreDictCsv(oredictCsv);

		recipeHandlers.add(new ShapedCraftingHandler());

		for (String s : methods) {
			boolean exists = false;
			for (ETRecipeData data : recipeData) {
				if (data.getRecipeFormat().equals(s)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				recipeData.add(new ETRecipeData(s, new String[0], true));
			}
		}
		DefaultListModel model = new DefaultListModel();
		for (String s : methodDisplays()) {
			model.addElement(s);
		}
		listMethods = new JList(model);
		listMethods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMethods.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listMethods.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				btnNewRecipe.setEnabled(comboScripts.getSelectedIndex() >= 0 && listMethods.getModel().getSize() > 0);
			}
		});
		scrollMethods = new JScrollPane(listMethods);

		comboRecipes = new JComboBox();
		comboRecipes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && !updatingParameters) {
					updateCurrentRecipe();
				}
			}
		});

		labelRecipe = new JLabel("Current Recipe");
		labelRecipe.setHorizontalAlignment(SwingConstants.TRAILING);

		table = new JTable(new Object[0][0], new String[] { "ID", "Name" }) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			// Use custom renderer for the Name column (index 1) to show icon
			public Class getColumnClass(int column) {
				return column == 1 ? String.class : Object.class;
			}
		};

		if (iconDir != null) {
			table.getColumnModel().getColumn(1).setCellRenderer(new IconRenderer(iconLoader));
			table.setRowHeight(36); // Make rows taller for icons
		}

		table.setBackground(Color.decode("#FFFFFF"));
		tableScroll = new JScrollPane(table);
		tableScroll.getViewport().setBackground(Color.decode("#FFFFFF"));

		// Enable Drag support
		table.setDragEnabled(true);
		table.setTransferHandler(new javax.swing.TransferHandler() {
			@Override
			public int getSourceActions(javax.swing.JComponent c) {
				return COPY;
			}

			@Override
			protected java.awt.datatransfer.Transferable createTransferable(javax.swing.JComponent c) {
				JTable table = (JTable) c;
				int row = table.getSelectedRow();
				if (row < 0)
					return null;
				// Get the ID (column 0)
				String text = (String) table.getValueAt(row, 0);
				return new java.awt.datatransfer.StringSelection(text);
			}

			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor);
			}

			@Override
			public boolean importData(TransferSupport support) {
				// We don't actually import data into the table, but we return true
				// so that the source (if it's a MOVE action) knows to clear itself.
				return true;
			}
		});

		tableController.loadTable(table, "Items", "", itemMappings, fluidMappings, oreDictMappings, blacklist,
				iconLoader);

		table.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (table.getSelectedRow() >= 0 && table.getSelectedColumn() >= 0)
					table.setToolTipText("Currently Selected: " + table.getValueAt(table.getSelectedRow(), 0) + " ("
							+ table.getValueAt(table.getSelectedRow(), 1) + ")");
				else
					table.setToolTipText("Currently Selected: None");

				updateParameters();
			}
		});

		ActionListener tableSelectListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aBtn = (AbstractButton) actionEvent.getSource();
				tableController.loadTable(table, aBtn.getText(), txtSearchTable.getText(), itemMappings, fluidMappings,
						oreDictMappings, blacklist, iconLoader);
			}
		};

		rdbtnItems = new JRadioButton("Items");
		rdbtnItems.setSelected(true);
		rdbtnItems.addActionListener(tableSelectListener);
		buttonGroup.add(rdbtnItems);

		rdbtnFluids = new JRadioButton("Fluids");
		rdbtnFluids.addActionListener(tableSelectListener);
		buttonGroup.add(rdbtnFluids);

		rdbtnOreDict = new JRadioButton("Ore Dict");
		rdbtnOreDict.addActionListener(tableSelectListener);
		buttonGroup.add(rdbtnOreDict);

		btnNewRecipe = new JButton("Add New Recipe Using Selected Type");
		btnNewRecipe.setEnabled(false);
		btnNewRecipe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (listMethods.getSelectedIndex() >= 0 && getCurrentScript() != null) {
					ETRecipeData data = recipeData.get(listMethods.getSelectedIndex());
					getCurrentScript().recipes
							.add(new ETActualRecipe(data.getRecipeFormat(), new String[data.getParameterCount()]));
					updateRecipesList(false);
					comboRecipes.setSelectedIndex(getCurrentScript().recipes.size() - 1);
					updateCurrentRecipe();
				}
			}
		});

		pnlRecipeEdit = new JPanel();

		JScrollPane scrollPane = new JScrollPane(pnlRecipeEdit);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		txtSearchTable = new JPlaceholderTextField("Search");
		txtSearchTable.setDisabledTextColor(UIManager.getColor("Button.disabledText"));

		javax.swing.Timer searchTimer = new javax.swing.Timer(300, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search();
			}

			public void search() {
				tableController.loadTable(table,
						rdbtnItems.isSelected() ? "Items" : rdbtnFluids.isSelected() ? "Fluids" : "Ore Dict",
						txtSearchTable.getText().trim().toLowerCase(), itemMappings, fluidMappings, oreDictMappings,
						blacklist, iconLoader);
			}
		});
		searchTimer.setRepeats(false);

		txtSearchTable.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				restartTimer();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				restartTimer();
			}

			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				restartTimer();
			}

			public void restartTimer() {
				searchTimer.restart();
			}
		});

		JButton btnSearchTable = new JButton("Search Table");
		btnSearchTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableController.loadTable(table,
						rdbtnItems.isSelected() ? "Items" : rdbtnFluids.isSelected() ? "Fluids" : "Ore Dict",
						txtSearchTable.getText().trim().toLowerCase(), itemMappings, fluidMappings, oreDictMappings,
						blacklist, iconLoader);
			}
		});

		ActionListener recipeTypeSelect = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (comboRecipes.getSelectedIndex() >= 0) {
					int index = indexOfRecipeFormat(
							getCurrentScript().recipes.get(comboRecipes.getSelectedIndex()).getRecipeFormat());

					if (index >= 0) {
						recipeData.get(index).setAddRecipe(btnRecipeAdd.isSelected());
					}
				}
			}
		};

		btnRecipeRemove = new JRadioButton("Remove");
		btnRecipeRemove.addActionListener(recipeTypeSelect);
		buttonGroup_1.add(btnRecipeRemove);

		btnRecipeAdd = new JRadioButton("Add/Other");
		btnRecipeAdd.addActionListener(recipeTypeSelect);
		buttonGroup_1.add(btnRecipeAdd);

		// ... (rest of constructor)

		JPanel panel = new JPanel();

		recipeDisplay = new JTextField();
		recipeDisplay.setEditable(false);
		recipeDisplay.setColumns(10);

		lblCopying = new JLabel("Currently Copying: ");

		comboScripts = new JComboBox();
		comboScripts.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				btnNewRecipe.setEnabled(comboScripts.getSelectedIndex() >= 0 && listMethods.getModel().getSize() > 0);
				if (e.getStateChange() == ItemEvent.SELECTED && !updatingParameters) {
					comboRecipes.setSelectedIndex(-1);
					updateRecipesList(false);
					comboRecipes.setSelectedIndex(-1);
					updateCurrentRecipe();
				}
			}
		});

		JLabel lblCurrentScript = new JLabel("Current Script");
		lblCurrentScript.setHorizontalAlignment(SwingConstants.TRAILING);

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout
								.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
										.createSequentialGroup().addGap(6).addGroup(groupLayout.createParallelGroup(
												Alignment.LEADING, false)
												.addComponent(tableScroll, GroupLayout.PREFERRED_SIZE, 332,
														GroupLayout.PREFERRED_SIZE)
												.addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
														.createParallelGroup(Alignment.LEADING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(rdbtnItems, GroupLayout.PREFERRED_SIZE,
																		63, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(rdbtnFluids, GroupLayout.PREFERRED_SIZE,
																		63, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(rdbtnOreDict, GroupLayout.PREFERRED_SIZE,
																		79, GroupLayout.PREFERRED_SIZE))
														.addComponent(txtSearchTable, GroupLayout.DEFAULT_SIZE, 217,
																Short.MAX_VALUE))
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(btnSearchTable)))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 563,
																Short.MAX_VALUE)
														.addGap(6))
												.addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
														.createParallelGroup(Alignment.LEADING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(btnRecipeRemove)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(btnRecipeAdd)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(recipeDisplay, GroupLayout.DEFAULT_SIZE,
																		398, Short.MAX_VALUE))
														.addGroup(groupLayout.createSequentialGroup()
																.addGroup(groupLayout
																		.createParallelGroup(Alignment.TRAILING)
																		.addComponent(lblCurrentScript,
																				GroupLayout.PREFERRED_SIZE, 95,
																				GroupLayout.PREFERRED_SIZE)
																		.addComponent(labelRecipe,
																				GroupLayout.PREFERRED_SIZE, 95,
																				GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(ComponentPlacement.RELATED)
																.addGroup(groupLayout
																		.createParallelGroup(Alignment.LEADING)
																		.addComponent(comboScripts, Alignment.TRAILING,
																				0, 446, Short.MAX_VALUE)
																		.addComponent(comboRecipes, 0, 446,
																				Short.MAX_VALUE))))
														.addGap(16))))
								.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblCopying)
										.addPreferredGap(ComponentPlacement.RELATED)))
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(btnNewRecipe, GroupLayout.DEFAULT_SIZE, 254,
														Short.MAX_VALUE)
												.addComponent(scrollMethods, Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
										.addGap(11))
								.addGroup(groupLayout.createSequentialGroup().addGap(0)
										.addComponent(panel, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
										.addContainerGap()))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
				Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addGroup(groupLayout.createSequentialGroup().addComponent(lblCopying)
														.addGap(28))
												.addComponent(panel, GroupLayout.PREFERRED_SIZE, 38,
														GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnNewRecipe))
						.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(txtSearchTable, GroupLayout.PREFERRED_SIZE, 19,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnSearchTable))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(comboScripts, GroupLayout.PREFERRED_SIZE, 18,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblCurrentScript).addComponent(rdbtnItems)
										.addComponent(rdbtnFluids).addComponent(rdbtnOreDict))))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollMethods, GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(tableScroll, GroupLayout.DEFAULT_SIZE, 639,
												Short.MAX_VALUE)
										.addGroup(groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addComponent(labelRecipe).addComponent(comboRecipes,
																GroupLayout.PREFERRED_SIZE, 18,
																GroupLayout.PREFERRED_SIZE))
												.addGap(6)
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(btnRecipeRemove).addComponent(btnRecipeAdd)
														.addComponent(recipeDisplay, GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGap(9).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 582,
														Short.MAX_VALUE))))
						.addGap(0)));

		btnDeleteRecipe = new JButton("Delete Recipe");
		btnDeleteRecipe.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnDeleteRecipe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboRecipes.getSelectedIndex() >= 0) {
					getCurrentScript().recipes.remove(comboRecipes.getSelectedIndex());
					comboRecipes.setSelectedIndex(-1);
					updateRecipesList(false);
					comboRecipes.setSelectedIndex(-1);
					updateCurrentRecipe();
				}
			}
		});

		btnDupeRecipe = new JButton("Duplicate Recipe");
		btnDupeRecipe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboRecipes.getSelectedIndex() >= 0) {
					getCurrentScript().recipes
							.add(getCurrentScript().recipes.get(comboRecipes.getSelectedIndex()).clone());
					updateRecipesList(false);
					comboRecipes.setSelectedIndex(getCurrentScript().recipes.size() - 1);
					updateCurrentRecipe();
				}
			}
		});
		btnDupeRecipe.setAlignmentX(Component.CENTER_ALIGNMENT);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addComponent(btnDeleteRecipe, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btnDupeRecipe, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE).addGap(0)));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup().addGap(6)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(btnDeleteRecipe)
								.addComponent(btnDupeRecipe))));
		panel.setLayout(gl_panel);
		pnlRecipeEdit.setLayout(new BoxLayout(pnlRecipeEdit, BoxLayout.Y_AXIS));
		tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().setLayout(groupLayout);
		this.pack();

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		chkVisualEditor = new JCheckBox("Use Visual Editor");
		chkVisualEditor.setSelected(true);
		chkVisualEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCurrentRecipe();
			}
		});
		menuBar.add(chkVisualEditor);

		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		JMenuItem menuItemNew = new JMenuItem("New Script");
		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menuFile.add(menuItemNew);

		menuItemNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.newScript();
			}
		});

		JMenuItem menuItemOpen = new JMenuItem("Open Scripts");
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		menuFile.add(menuItemOpen);

		menuItemOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.openScripts();
			}
		});

		JMenuItem menuItemSave = new JMenuItem("Save Current Script");
		menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menuFile.add(menuItemSave);

		menuItemSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.saveScript(getCurrentScript());
			}
		});

		JMenuItem menuItemSaveAs = new JMenuItem("Save Current Script As");
		menuItemSaveAs
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		menuFile.add(menuItemSaveAs);

		menuItemSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				saveCurScriptAs();
			}
		});

		mntmSaveAllScripts = new JMenuItem("Save All Scripts");
		mntmSaveAllScripts
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuFile.add(mntmSaveAllScripts);

		mntmSaveAllScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.saveAllScripts();
			}
		});

		mntmDeleteScript = new JMenuItem("Delete Current Script");
		mntmDeleteScript.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		menuFile.add(mntmDeleteScript);

		mntmDeleteScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.deleteScript();
			}
		});

		JMenuItem mntmImportData = new JMenuItem("Import Obj Data");
		mntmImportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		menuFile.add(mntmImportData);

		mntmImportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				importData();
			}
		});

		JMenuItem mntmExportData = new JMenuItem("Export Obj Data");
		mntmExportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		menuFile.add(mntmExportData);

		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				exportData();
			}
		});

		mnOther = new JMenu("Other");
		menuBar.add(mnOther);

		mntmRenameCurrentScript = new JMenuItem("Rename Current Script");
		mnOther.add(mntmRenameCurrentScript);
		mntmRenameCurrentScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.renameFile();
			}
		});

		mntmExportRecipesTo = new JMenuItem("Create Text File Of Recipes");
		mnOther.add(mntmExportRecipesTo);
		mntmExportRecipesTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (recipeData.size() > 0)
					sendRecipesToTextFile();
				else
					JOptionPane.showOptionDialog(AppFrame.this, "No Recipes!", "Message", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK" }, "OK");
			}
		});

		mntmExportCurrentTable = new JMenuItem("Create Text File Of Current Table");
		mnOther.add(mntmExportCurrentTable);
		mntmExportCurrentTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (recipeData.size() > 0)
					sendTableToTextFile();
				else
					JOptionPane.showOptionDialog(AppFrame.this, "Table is Empty!", "Message", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK" }, "OK");
			}
		});

		menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);

		mntmHelp = new JMenuItem("Open Help Wiki");
		menuHelp.add(mntmHelp);

		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.help();
			}
		});

		mntmAbout = new JMenuItem("About External Tweaker");
		menuHelp.add(mntmAbout);

		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.about();
			}
		});

		mntmDownload = new JMenuItem("Download External Tweaker");
		menuHelp.add(mntmDownload);

		mntmDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				controller.download();
			}
		});

		this.setVisible(true);
		updateParameters();
		updateCurrentRecipe();
	}

	public void updateParameters() {
		// Check if a handler is active
		if (pnlRecipeEdit.getComponentCount() > 0 && pnlRecipeEdit.getComponent(0) instanceof PanelCraftingRecipe) {
			// Handler handles saving automatically on change, but we can force a save here
			// if needed
			// or just do nothing as the panel handles it.
			return;
		}

		for (int i = 0; i < paramPanels.size(); i++) {
			paramPanels.get(i).editPanel.update();
			paramPanels.get(i).btnPaste.setEnabled(paramPanels.get(i).paramType.equals(copyType));
			if (comboRecipes.getSelectedIndex() >= 0 && getCurrentScript().recipes.size() > 0)
				getCurrentScript().recipes.get(comboRecipes.getSelectedIndex()).setParameterData(i,
						paramPanels.get(i).exportData());
		}
	}

	public void updateRecipesList(boolean updating) {
		int comboIndex = -1;
		if (updating) {
			comboIndex = comboRecipes.getSelectedIndex();
			updatingParameters = true;
		}
		String[] displays = new String[getCurrentScript() == null ? 0 : getCurrentScript().recipes.size()];
		for (int i = 0; i < (getCurrentScript() == null ? 0 : getCurrentScript().recipes.size()); i++) {
			int index = indexOfRecipeFormat(getCurrentScript().recipes.get(i).getRecipeFormat());
			if (index >= 0) {

				displays[i] = getCurrentScript().recipes.get(i).recipeToString(recipeData.get(index));
				String[] split = displays[i].substring(0, displays[i].indexOf("(")).split("\\.");
				displays[i] = "#" + Integer.toString(i + 1) + "  " + split[split.length - 2] + "."
						+ split[split.length - 1]
						+ displays[i].substring(displays[i].indexOf("("), displays[i].length());

				List<Object[]> mappings = new ArrayList<Object[]>();
				mappings.addAll(new ArrayList<Object[]>(Arrays.asList(itemMappings)));
				mappings.addAll(new ArrayList<Object[]>(Arrays.asList(fluidMappings)));
				mappings.addAll(new ArrayList<Object[]>(Arrays.asList(oreDictMappings)));
				for (int x = 0; x < mappings.size(); x++) {
					if (index >= 0) {
						String newVal = "";
						while (!newVal.equals(displays[i])) {
							if (!newVal.equals(""))
								displays[i] = newVal;
							newVal = displays[i].replace(mappings.get(x)[0].toString(),
									mappings.get(x)[1].toString());
						}
					}
				}

			} else
				displays[i] = "#" + Integer.toString(i + 1) + "  "
						+ getCurrentScript().recipes.get(i).getRecipeFormat()
						+ " ERROR: DID NOT FIND RECIPE FORMAT";
		}

		if (comboRecipes.getSelectedIndex() >= 0) {
			int index = indexOfRecipeFormat(
					getCurrentScript().recipes.get(comboRecipes.getSelectedIndex()).getRecipeFormat());

			if (index >= 0) {
				recipeDisplay.setText(getCurrentScript().recipes.get(comboRecipes.getSelectedIndex())
						.recipeToString(recipeData.get(index)));
			} else
				recipeDisplay.setText("");
		} else
			recipeDisplay.setText("");

		comboRecipes.setModel(new DefaultComboBoxModel<String>(displays));
		if (updating) {
			comboRecipes.setSelectedIndex(comboIndex);
			updatingParameters = false;
		}
	}

	public void updateScriptsList(boolean updating) {
		int comboIndex = 0;
		if (updating) {
			comboIndex = comboScripts.getSelectedIndex();
			updatingParameters = true;
		}
		String[] displays = new String[scripts.size()];
		for (int i = 0; i < scripts.size(); i++) {
			displays[i] = "#" + Integer.toString(i + 1) + "  " + scripts.get(i).filePath
					+ (Strings.isNullOrEmpty(scripts.get(i).filePath) ? "" : File.separator) + scripts.get(i).fileName;
		}

		comboScripts.setModel(new DefaultComboBoxModel<String>(displays));
		if (updating) {
			comboScripts.setSelectedIndex(comboIndex);
			updatingParameters = false;
		}
	}

	public void updateCurrentRecipe() {

		pnlRecipeEdit.removeAll();
		paramPanels.clear();
		if (comboRecipes.getSelectedIndex() >= 0) {
			ETActualRecipe currentRecipe = getCurrentScript().recipes.get(comboRecipes.getSelectedIndex());
			String recipeFormat = currentRecipe.getRecipeFormat();

			// Check handlers
			RecipeHandler activeHandler = null;
			if (chkVisualEditor.isSelected()) {
				for (RecipeHandler h : recipeHandlers) {
					if (h.matches(recipeFormat)) {
						activeHandler = h;
						break;
					}
				}
			}

			if (activeHandler != null) {
				PanelCraftingRecipe panel = new PanelCraftingRecipe(this, activeHandler, currentRecipe);
				pnlRecipeEdit.add(panel);

				int index = indexOfRecipeFormat(recipeFormat);
				if (index >= 0) {
					btnRecipeAdd.setSelected(recipeData.get(index).isAddRecipe());
					btnRecipeRemove.setSelected(!recipeData.get(index).isAddRecipe());
					recipeDisplay.setText(currentRecipe.recipeToString(recipeData.get(index)));
				}
			} else {
				// Default behavior
				int index = indexOfRecipeFormat(recipeFormat);

				if (index >= 0) {
					for (String s : recipeData.get(index).getParameterTypes())
						addParameter(s);

					for (int i = 0; i < paramPanels.size(); i++) {
						btnRecipeAdd.setSelected(recipeData.get(index).isAddRecipe());
						btnRecipeRemove.setSelected(!recipeData.get(index).isAddRecipe());
						paramPanels.get(i).txtName.setText(recipeData.get(index).getParamName(i));
						paramPanels.get(i).importData(
								getCurrentScript().recipes.get(comboRecipes.getSelectedIndex()).getParameterData(i));
					}
					updateParameters();
					recipeDisplay.setText(getCurrentScript().recipes.get(comboRecipes.getSelectedIndex())
							.recipeToString(recipeData.get(index)));

				} else
					recipeDisplay.setText("");
			}
		}
		btnDeleteRecipe.setEnabled(comboRecipes.getSelectedIndex() >= 0);
		btnDupeRecipe.setEnabled(comboRecipes.getSelectedIndex() >= 0);
		btnRecipeAdd.setEnabled(comboRecipes.getSelectedIndex() >= 0);
		btnRecipeRemove.setEnabled(comboRecipes.getSelectedIndex() >= 0);
		pnlRecipeEdit.revalidate();
		pnlRecipeEdit.repaint();
	}

	private List<String> methodDisplays() {
		List<String> displays = new ArrayList<String>();
		List<ETRecipeData> recipeDataNew = new ArrayList<ETRecipeData>();
		for (ETRecipeData data : recipeData) {
			displays.add(data.getRecipeDisplay());
		}
		Collections.sort(displays);
		for (String s : displays) {
			for (ETRecipeData data : recipeData) {
				if (data.getRecipeDisplay().equals(s))
					recipeDataNew.add(data);
			}
		}
		recipeData = recipeDataNew;
		return displays;
	}

	public int indexOfRecipeFormat(String format) {
		for (int i = 0; i < recipeData.size(); i++) {
			if (recipeData.get(i).getRecipeFormat().equals(format))
				return i;
		}
		return -1;
	}

	private void addParameter(String type) {
		type = type.trim();
		PanelParameterEdit p = new PanelParameterEdit(paramPanels.size() + 1, type, this);
		p.setListeners();
		pnlRecipeEdit.add(p);
		paramPanels.add(p);
	}

	public ETScript getCurrentScript() {
		if (comboScripts.getSelectedIndex() >= 0)
			return scripts.get(comboScripts.getSelectedIndex());
		return null;
	}

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				AppFrame frame = new AppFrame(new Object[0][0], new Object[0][0], new Object[0][0], new ArrayList(),
						null, null);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}

	private void exportData() {
		PanelImportExportDialog dataPanel = new PanelImportExportDialog(false);
		int input = JOptionPane.showOptionDialog(this, dataPanel, "Exporting Data", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Export", "Cancel" }, "Export");

		boolean[] settings = dataPanel.getSettings();
		if (input == 0 && ((dataPanel.txtPath.getText().trim() == null || dataPanel.txtPath.getText().trim().isEmpty())
				|| !dataPanel.txtPath.getText().trim().endsWith(".etd")
				|| (!settings[0] && !settings[1] && !settings[2] && !settings[3]))) {
			JOptionPane.showOptionDialog(this, "Invalid file path", "Error", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
			return;
		}
		if (input != 0)
			return;

		try {
			File file = new File(dataPanel.txtPath.getText().trim());
			if (settings[4]) {
				dataService.saveData(file, recipeData, settings);
			} else {
				dataService.mergeAndSaveData(file, recipeData, settings);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			JOptionPane.showOptionDialog(this, exc.getLocalizedMessage(),
					"Error! Try exporting from the game again! Report this issue if that fails!", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
		}
		JOptionPane.showOptionDialog(this, "Export Finished!", "Done", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, new Object[] { "OK" }, "OK");
	}

	private void importData() {
		PanelImportExportDialog dataPanel = new PanelImportExportDialog(true);
		int input = JOptionPane.showOptionDialog(this, dataPanel, "Importing Data", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Import", "Cancel" }, "Import");

		boolean[] settings = dataPanel.getSettings();
		if (input == 0 && ((dataPanel.txtPath.getText().trim() == null || dataPanel.txtPath.getText().trim().isEmpty())
				|| !dataPanel.txtPath.getText().trim().endsWith(".etd")
				|| (!settings[0] && !settings[1] && !settings[2] && !settings[3]))) {
			JOptionPane.showOptionDialog(this, "Invalid file path", "Error", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
			return;
		}
		if (input != 0)
			return;
		try {
			File file = new File(dataPanel.txtPath.getText().trim());
			ImportedData currentData = new ImportedData(itemMappings, fluidMappings, oreDictMappings, recipeData);
			ImportedData newData = dataService.importData(file, settings, currentData);

			this.itemMappings = newData.itemMappings;
			this.fluidMappings = newData.fluidMappings;
			this.oreDictMappings = newData.oreDictMappings;
			this.recipeData = (ArrayList<ETRecipeData>) newData.recipeData;

			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String s : methodDisplays()) {
				model.addElement(s);
			}
			listMethods.setModel(model);
			txtSearchTable.setText("");
			tableController.loadTable(table,
					rdbtnItems.isSelected() ? "Items" : rdbtnFluids.isSelected() ? "Fluids" : "Ore Dict", "",
					itemMappings, fluidMappings, oreDictMappings, blacklist, iconLoader);

		} catch (Exception exc) {
			exc.printStackTrace();
			JOptionPane.showOptionDialog(this, exc.getLocalizedMessage(),
					"Error! Try importing from the game again! Especially if you updated this program!\nReport this issue if this fails!",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
		}
		JOptionPane.showOptionDialog(this, "Import Finished!", "Done", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, new Object[] { "OK" }, "OK");
	}

	public ETRecipeData findRecipeDataForRecipe(String recipe) {
		if (!recipe.contains("("))
			return null;
		String begin = recipe.substring(0, recipe.indexOf("("));
		String params = recipe.substring(recipe.indexOf("(") + 1, recipe.lastIndexOf(")"));
		while (params.contains("[[") && params.contains("]]")) {
			params = params.replace(params.substring(params.indexOf("[["), params.indexOf("]]") + 2), " ");
		}
		while (params.contains("[") && params.contains("]")) {
			params = params.replace(params.substring(params.indexOf("["), params.indexOf("]") + 1), " ");
		}
		int numParam = params.split(",").length;
		for (ETRecipeData d : recipeData) {
			if (d.getRecipeFormat().startsWith(begin) && numParam <= d.getParameterCount()
					&& numParam >= d.getParameterCountOptMin()) {
				return d;
			}
		}
		return null;
	}

	private void saveCurScriptAs() {
		ETScript script = getCurrentScript().clone();
		java.awt.FileDialog fd = new java.awt.FileDialog(this, "Save Script As", java.awt.FileDialog.SAVE);
		fd.setDirectory(!(script.filePath == null || script.filePath.isEmpty())
				? (script.filePath + File.separator + script.fileName)
				: (System.getProperty("user.dir") + File.separator + script.filePath));
		if (!Strings.isNullOrEmpty(script.fileName))
			fd.setFile(script.fileName);
		fd.setVisible(true);

		if (fd.getFile() != null) {
			File selected = new File(fd.getDirectory(), fd.getFile());
			script.filePath = selected.getParent();
			script.fileName = selected.getName();
			if (!script.fileName.endsWith(".zs"))
				script.fileName += ".zs";

			controller.saveScript(script);
		}
	}

	private void sendTableToTextFile() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir") + File.separator + "tableList.txt"));
		fc.setSelectedFile(new File("tableList.txt"));
		fc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));

		String filePath = "";
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			filePath = fc.getSelectedFile().getAbsolutePath();
			if (!filePath.endsWith(".txt"))
				filePath += ".txt";
		} else
			return;

		try {
			fileExportService.writeTableToFile(table, new File(filePath),
					rdbtnItems.isSelected() ? "Items" : rdbtnFluids.isSelected() ? "Fluids" : "Ore Dict");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showOptionDialog(this, e.getLocalizedMessage(), "Error! Report this issue if you can!",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
		}
	}

	private void sendRecipesToTextFile() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir") + File.separator + "recipeList.txt"));
		fc.setSelectedFile(new File("recipeList.txt"));
		fc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));

		String filePath = "";
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			filePath = fc.getSelectedFile().getAbsolutePath();
			if (!filePath.endsWith(".txt"))
				filePath += ".txt";
		} else
			return;

		try {
			fileExportService.writeRecipesToFile(recipeData, new File(filePath));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showOptionDialog(this, e.getLocalizedMessage(), "Error! Report this issue if you can!",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { "OK" }, "OK");
		}
	}

	public void loadOreDictCsv(File file) {
		if (file == null || !file.exists()) {
			System.out.println("OreDict CSV not found or null: " + file);
			return;
		}

		System.out.println("Loading OreDict CSV: " + file.getAbsolutePath());
		com.bartz24.externaltweaker.app.model.OreDictRegistry.getInstance().loadFromCsv(file);
		this.oreDictMappings = com.bartz24.externaltweaker.app.model.OreDictRegistry.getInstance().toLegacyArray();

		System.out.println("Loaded OreDict entries via Registry.");
	}

}
