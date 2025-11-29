package com.bartz24.externaltweaker.app.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;
import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.recipe.RecipeHandler;

public class PanelCraftingRecipe extends JPanel {
    private AppFrame mainFrame;
    private RecipeHandler handler;
    private ETActualRecipe currentRecipe;

    private JButton[][] gridButtons = new JButton[3][3];
    private JButton outputButton;
    private JPanel trashPanel;
    private String[][] gridData = new String[3][3];
    private String outputData = "null";

    private javax.swing.JCheckBox chkUseOreDict;
    private JPanel selectedItemPanel;
    private JLabel lblSelectedItem;
    private JButton btnPrevItem;
    private JButton btnNextItem;
    private int selectedX = -1;
    private int selectedY = -1;

    // Static field to track the source button of a drag operation
    private static JButton draggedButton = null;

    public PanelCraftingRecipe(AppFrame frame, RecipeHandler recipeHandler, ETActualRecipe recipe) {
        this.mainFrame = frame;
        this.handler = recipeHandler;
        this.currentRecipe = recipe;

        initComponents();
        loadFromRecipe();
    }

    private void initComponents() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3, 2, 2));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        TransferHandler dropHandler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    String data = Utils.formatItemId((String) t.getTransferData(DataFlavor.stringFlavor));
                    JButton btn = (JButton) support.getComponent();

                    if (btn == outputButton) {
                        setOutputItem(data);
                    } else {
                        // Find which grid button this is
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 3; x++) {
                                if (gridButtons[y][x] == btn) {
                                    setGridItem(x, y, data);
                                    return true;
                                }
                            }
                        }
                    }
                    saveToRecipe();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                JButton btn = (JButton) c;
                String data = null;
                if (btn == outputButton) {
                    data = outputData;
                } else {
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 3; x++) {
                            if (gridButtons[y][x] == btn) {
                                data = gridData[y][x];
                                break;
                            }
                        }
                    }
                }
                if (data == null || data.equals("null") || data.isEmpty())
                    return null;
                return new StringSelection(data);
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                if (action == MOVE) {
                    // If moved to trash (or elsewhere), we might want to clear source?
                    // But standard D&D usually clears on move.
                    // Let's handle clearing in the Trash's import logic or here if we detect it was
                    // trash.
                    // Actually, for grid re-arrangement, we might not want to clear immediately if
                    // we are just swapping.
                    // But user asked for "drag away to remove", implying dropping on trash clears
                    // it.
                    // If we drop on another slot, it copies (or moves).
                    // Let's rely on the target to decide. If target is trash, it clears.
                    // If target is another slot, it overwrites.
                    // Wait, if it's MOVE, we should clear source.
                    // But if we drag from list, it's COPY.
                    // If we drag from grid, it should be MOVE.
                }
            }
        };

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                JButton btn = new JButton("");
                btn.setPreferredSize(new Dimension(100, 100));
                btn.setBackground(Color.decode("#8B8B8B"));
                btn.setTransferHandler(dropHandler);
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);

                // Enable dragging from button
                btn.addMouseMotionListener(new MouseAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        // Update selection
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 3; x++) {
                                if (gridButtons[y][x] == c) {
                                    selectedX = x;
                                    selectedY = y;
                                    updateSelectionDisplay();
                                    break;
                                }
                            }
                        }

                        TransferHandler handler = c.getTransferHandler();
                        draggedButton = (JButton) c; // Track source
                        handler.exportAsDrag(c, e, TransferHandler.MOVE);
                    }
                });
                btn.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        // Update selection on click too
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 3; x++) {
                                if (gridButtons[y][x] == c) {
                                    selectedX = x;
                                    selectedY = y;
                                    updateSelectionDisplay();
                                    break;
                                }
                            }
                        }
                    }
                });

                gridButtons[y][x] = btn;
                gridPanel.add(btn);
            }
        }

        outputButton = new JButton("");
        outputButton.setPreferredSize(new Dimension(120, 120));
        outputButton.setBackground(Color.decode("#8B8B8B"));
        outputButton.setTransferHandler(dropHandler);
        outputButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        outputButton.setHorizontalTextPosition(SwingConstants.CENTER);
        outputButton.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                draggedButton = (JButton) c; // Track source
                handler.exportAsDrag(c, e, TransferHandler.MOVE);
            }
        });

        JLabel arrowLabel = new JLabel("->");
        arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        arrowLabel.setFont(arrowLabel.getFont().deriveFont(32.0f));

        chkUseOreDict = new javax.swing.JCheckBox("Use OreDict");
        chkUseOreDict.setSelected(true);
        chkUseOreDict.setHorizontalAlignment(SwingConstants.CENTER);

        // Selected Item Panel
        selectedItemPanel = new JPanel(new BorderLayout());
        selectedItemPanel.setBorder(BorderFactory.createTitledBorder("Selected Item"));
        lblSelectedItem = new JLabel("None");
        lblSelectedItem.setHorizontalAlignment(SwingConstants.CENTER);
        selectedItemPanel.add(lblSelectedItem, BorderLayout.CENTER);

        btnPrevItem = new JButton("<");
        btnNextItem = new JButton(">");
        JPanel btnPanel = new JPanel(new GridLayout(1, 2));
        btnPanel.add(btnPrevItem);
        btnPanel.add(btnNextItem);
        selectedItemPanel.add(btnPanel, BorderLayout.SOUTH);

        java.awt.event.ActionListener arrowListener = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (selectedX != -1 && selectedY != -1) {
                    cycleItem(selectedX, selectedY, e.getSource() == btnNextItem);
                }
            }
        };
        btnPrevItem.addActionListener(arrowListener);
        btnNextItem.addActionListener(arrowListener);

        // Trash Panel
        trashPanel = new JPanel(new BorderLayout());
        trashPanel.setBackground(Color.LIGHT_GRAY);
        trashPanel.setBorder(BorderFactory.createTitledBorder("Trash"));
        trashPanel.setPreferredSize(new Dimension(100, 100));
        JLabel trashLabel = new JLabel("<html><center>Drop here<br>to remove</center></html>");
        trashLabel.setHorizontalAlignment(SwingConstants.CENTER);
        trashPanel.add(trashLabel, BorderLayout.CENTER);

        trashPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                // When dropped here, we don't do anything with the data,
                // but we return true to indicate success.
                // The exportDone of the source should handle clearing if it was a MOVE.
                // BUT, exportDone doesn't know where it was dropped easily.
                // So we need a way to callback or we just handle it here if we can identify
                // source.
                // Actually, standard Swing D&D MOVE action implies source removes it.
                // Let's implement exportDone in the buttons to clear if action was MOVE.
                return true;
            }
        });

        // Update dropHandler to handle exportDone for clearing
        TransferHandler gridTransferHandler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    JButton btn = (JButton) support.getComponent();

                    if (t instanceof ButtonTransferable && ((ButtonTransferable) t).getSource() == btn) {
                        return false;
                    }

                    String data = Utils.formatItemId((String) t.getTransferData(DataFlavor.stringFlavor));

                    if (btn == outputButton) {
                        String rawData = Utils.unformatItemId(data);
                        if (rawData.startsWith("ore:")) {
                            String rep = Utils.getOreDictRepresentativeItem(rawData);
                            if (rep != null) {
                                data = rep;
                            }
                        }
                        setOutputItem(data);
                    } else {
                        // Find which grid button this is
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 3; x++) {
                                if (gridButtons[y][x] == btn) {
                                    // Check for swap
                                    if (draggedButton != null && draggedButton != btn) {
                                        JButton sourceBtn = draggedButton;
                                        // Find source coordinates
                                        int sourceX = -1;
                                        int sourceY = -1;
                                        for (int sy = 0; sy < 3; sy++) {
                                            for (int sx = 0; sx < 3; sx++) {
                                                if (gridButtons[sy][sx] == sourceBtn) {
                                                    sourceX = sx;
                                                    sourceY = sy;
                                                    break;
                                                }
                                            }
                                        }

                                        if (sourceX != -1) {
                                            // Perform swap
                                            String currentTargetItem = gridData[y][x];
                                            setGridItem(sourceX, sourceY, currentTargetItem);
                                            // Reset draggedButton to indicate swap was handled
                                            draggedButton = null;
                                        }
                                    }

                                    if (chkUseOreDict.isSelected()) {
                                        System.out.println("Checking OreDict for: " + data);
                                        java.util.List<String> ores = com.bartz24.externaltweaker.app.model.OreDictRegistry
                                                .getInstance().getOreDictsForItem(data);
                                        if (!ores.isEmpty()) {
                                            data = Utils.formatItemId("ore:" + ores.get(0));
                                            System.out.println("Converted to OreDict: " + data);
                                        }
                                    }
                                    setGridItem(x, y, data);
                                    selectedX = x;
                                    selectedY = y;
                                    updateSelectionDisplay();
                                    saveToRecipe();
                                    return true;
                                }
                            }
                        }
                    }
                    saveToRecipe();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                JButton btn = (JButton) c;
                String data = null;
                if (btn == outputButton) {
                    data = outputData;
                } else {
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 3; x++) {
                            if (gridButtons[y][x] == btn) {
                                data = gridData[y][x];
                                break;
                            }
                        }
                    }
                }
                if (data == null || data.equals("null") || data.isEmpty())
                    return null;
                return new ButtonTransferable(btn, data);
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                if (action == MOVE) {
                    if (draggedButton == null) {
                        // Swapped, do nothing
                    } else {
                        JButton btn = (JButton) source;
                        if (btn == outputButton) {
                            setOutputItem("null");
                        } else {
                            for (int y = 0; y < 3; y++) {
                                for (int x = 0; x < 3; x++) {
                                    if (gridButtons[y][x] == btn) {
                                        setGridItem(x, y, "null");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    saveToRecipe();
                    draggedButton = null; // Cleanup
                }
            }
        };

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                gridButtons[y][x].setTransferHandler(gridTransferHandler);
            }
        }
        outputButton.setTransferHandler(gridTransferHandler);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                .addComponent(gridPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(selectedItemPanel, GroupLayout.PREFERRED_SIZE, 150,
                                        GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                .addComponent(chkUseOreDict)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(arrowLabel, GroupLayout.PREFERRED_SIZE, 40,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(outputButton, GroupLayout.PREFERRED_SIZE, 120,
                                                GroupLayout.PREFERRED_SIZE))
                                .addComponent(trashPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.CENTER)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(gridPanel, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(10)
                                                .addComponent(selectedItemPanel, GroupLayout.PREFERRED_SIZE, 150,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(chkUseOreDict)
                                                .addGap(10)
                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                        .addComponent(arrowLabel)
                                                        .addComponent(outputButton, GroupLayout.PREFERRED_SIZE, 120,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGap(20)
                                                .addComponent(trashPanel, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
    }

    private String[][] originalGridItems = new String[3][3];

    private void cycleItem(int x, int y, boolean next) {
        String current = gridData[y][x];
        if (current == null || current.equals("null"))
            return;

        // 1. Determine the base item ID
        String baseItem = current;
        String rawCurrent = Utils.unformatItemId(current);
        if (rawCurrent.startsWith("ore:")) {
            // Try to restore original item if it matches this OreDict
            String original = originalGridItems[y][x];
            boolean restored = false;
            if (original != null && !original.equals("null")) {
                java.util.List<String> ores = com.bartz24.externaltweaker.app.model.OreDictRegistry
                        .getInstance().getOreDictsForItem(original);
                for (String ore : ores) {
                    if (Utils.formatItemId("ore:" + ore).equals(Utils.formatItemId(current))) {
                        baseItem = original;
                        restored = true;
                        break;
                    }
                }
            }

            if (!restored) {
                String rep = Utils.getOreDictRepresentativeItem(current);
                if (rep != null) {
                    baseItem = rep;
                }
            }
        }

        System.out.println("Cycling item. Current: " + current + ", Base: " + baseItem);

        // 2. Get all OreDicts for the base item
        java.util.List<String> ores = com.bartz24.externaltweaker.app.model.OreDictRegistry.getInstance()
                .getOreDictsForItem(baseItem);

        // 3. Create options list: [BaseItem, <ore:Dict1>, <ore:Dict2>, ...]
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add(Utils.formatItemId(baseItem));
        for (String ore : ores) {
            options.add(Utils.formatItemId("ore:" + ore));
        }

        if (options.size() <= 1) {
            System.out.println("No alternatives to cycle.");
            return;
        }

        // 4. Find current index and cycle
        int index = options.indexOf(current);
        // If current is not in options (maybe it was an OreDict but we switched base
        // item logic?),
        // default to 0 (Base Item)
        if (index == -1) {
            System.out.println("Current item not in options. Resetting to base.");
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

        String newItem = options.get(index);
        System.out.println("Cycled to: " + newItem);
        // Pass baseItem as icon override to keep the icon constant
        setGridItem(x, y, newItem, baseItem);
        updateSelectionDisplay();
        saveToRecipe();
    }

    private class ButtonTransferable implements Transferable {
        private JButton btn;
        private String data;

        public ButtonTransferable(JButton btn, String data) {
            this.btn = btn;
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.stringFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return data;
            }
            throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
        }

        public JButton getSource() {
            return btn;
        }
    }

    public void setGridItem(int x, int y, String item) {
        setGridItem(x, y, item, null);
    }

    public void setGridItem(int x, int y, String item, String iconOverride) {
        String formatted = Utils.formatItemId(item);
        gridData[y][x] = formatted;

        if (item != null && !item.equals("null") && !Utils.unformatItemId(item).startsWith("ore:")) {
            originalGridItems[y][x] = formatted;
        }

        updateButtonDisplay(gridButtons[y][x], item, 64, iconOverride);
    }

    public String getGridItem(int x, int y) {
        return gridData[y][x];
    }

    public void setOutputItem(String item) {
        outputData = Utils.formatItemId(item);
        updateButtonDisplay(outputButton, item, 80, null);
    }

    public String getOutputItem() {
        return outputData;
    }

    private void updateButtonDisplay(JButton btn, String item, int iconSize) {
        updateButtonDisplay(btn, item, iconSize, null);
    }

    private void updateButtonDisplay(JButton btn, String item, int iconSize, String iconOverride) {
        if (item == null || item.equals("null") || item.isEmpty()) {
            btn.setText("");
            btn.setIcon(null);
            btn.setToolTipText(null);
            return;
        }

        String name = Utils.getNameFromId(item);
        System.out.println("updateButtonDisplay: item=" + item + ", name=" + name);
        if (name == null || name.isEmpty()) {
            name = item;
            if (item.contains(":")) {
                String[] parts = item.split(":");
                if (parts.length > 1)
                    name = parts[1];
            }
        }

        btn.setText(name);
        btn.setToolTipText(item);

        if (mainFrame.iconLoader != null) {
            String iconItem = item;
            if (iconOverride != null) {
                iconItem = iconOverride;
            } else if (Utils.unformatItemId(item).startsWith("ore:")) {
                String rep = Utils.getOreDictRepresentativeItem(item);
                if (rep != null)
                    iconItem = rep;
            }
            System.out.println("updateButtonDisplay: item=" + item + ", iconOverride=" + iconOverride
                    + ", finalIconItem=" + iconItem);

            // Use the name of the iconItem for lookup, not the display name of the button
            // (which might be the OreDict name)
            String iconName = name;
            if (!iconItem.equals(item)) {
                String resolvedName = Utils.getNameFromId(iconItem);
                if (resolvedName != null && !resolvedName.isEmpty()) {
                    iconName = resolvedName;
                }
            }

            ImageIcon icon = mainFrame.iconLoader.loadIcon(iconItem, iconName, iconSize);
            btn.setIcon(icon);
        }
    }

    private void updateSelectionDisplay() {
        if (selectedX != -1 && selectedY != -1) {
            String item = gridData[selectedY][selectedX];
            lblSelectedItem.setText(item == null || item.equals("null") ? "None" : item);
            btnPrevItem.setEnabled(item != null && !item.equals("null"));
            btnNextItem.setEnabled(item != null && !item.equals("null"));
        } else {
            lblSelectedItem.setText("None");
            btnPrevItem.setEnabled(false);
            btnNextItem.setEnabled(false);
        }
    }

    public void loadFromRecipe() {
        handler.load(currentRecipe, this);
    }

    public void saveToRecipe() {
        handler.save(this, currentRecipe);
        mainFrame.updateRecipesList(true);
    }
}
