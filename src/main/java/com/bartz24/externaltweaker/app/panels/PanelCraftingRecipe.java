package com.bartz24.externaltweaker.app.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import com.bartz24.externaltweaker.app.AppFrame;

import com.bartz24.externaltweaker.app.data.ETActualRecipe;
import com.bartz24.externaltweaker.app.recipe.RecipeHandler;
import com.bartz24.externaltweaker.app.transfer.CraftingGridTransferHandler;
import com.bartz24.externaltweaker.app.ui.slots.AbstractSlot;
import com.bartz24.externaltweaker.app.ui.slots.InputOreDictSlot;
import com.bartz24.externaltweaker.app.ui.slots.OutputItemStackSlot;

public class PanelCraftingRecipe extends JPanel {
    private AppFrame mainFrame;
    private RecipeHandler handler;
    private ETActualRecipe currentRecipe;

    private InputOreDictSlot[][] gridButtons = new InputOreDictSlot[3][3];
    private OutputItemStackSlot outputButton;
    private JPanel trashPanel;
    // gridData is now managed by slots, but we keep it for compatibility with
    // handler if needed,
    // or we can remove it and use slots directly.
    // For now, let's try to rely on slots.
    // private String[][] gridData = new String[3][3];
    // private String outputData = "null";

    private javax.swing.JCheckBox chkUseOreDict;
    private JPanel selectedItemPanel;
    private JLabel lblSelectedItem;
    private JButton btnPrevItem;
    private JButton btnNextItem;
    private int selectedX = -1;
    private int selectedY = -1;

    public PanelCraftingRecipe(AppFrame frame, RecipeHandler recipeHandler, ETActualRecipe recipe) {
        this.mainFrame = frame;
        this.handler = recipeHandler;
        this.currentRecipe = recipe;

        initComponents();
        loadFromRecipe();
    }

    private void initComponents() {
        TransferHandler gridTransferHandler = new CraftingGridTransferHandler(this);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3, 2, 2));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                InputOreDictSlot btn = new InputOreDictSlot(mainFrame);
                btn.setPreferredSize(new Dimension(100, 100));
                btn.setBackground(Color.decode("#8B8B8B"));
                btn.setTransferHandler(gridTransferHandler);
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
                        if (handler instanceof CraftingGridTransferHandler) {
                            ((CraftingGridTransferHandler) handler).setDraggedButton((JButton) c);
                        }
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

        outputButton = new OutputItemStackSlot(mainFrame);
        outputButton.setPreferredSize(new Dimension(120, 120));
        outputButton.setBackground(Color.decode("#8B8B8B"));
        outputButton.setTransferHandler(gridTransferHandler);
        outputButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        outputButton.setHorizontalTextPosition(SwingConstants.CENTER);
        outputButton.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                if (handler instanceof CraftingGridTransferHandler) {
                    ((CraftingGridTransferHandler) handler).setDraggedButton((JButton) c);
                }
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
                return true;
            }
        });

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

    private void cycleItem(int x, int y, boolean next) {
        InputOreDictSlot slot = gridButtons[y][x];
        slot.cycleItem(next);
        updateSelectionDisplay();
        saveToRecipe();
    }

    public AbstractSlot[][] getGridButtons() {
        return gridButtons;
    }

    public AbstractSlot getOutputButton() {
        return outputButton;
    }

    public boolean isUseOreDict() {
        return chkUseOreDict.isSelected();
    }

    public void setSelectedSlot(int x, int y) {
        this.selectedX = x;
        this.selectedY = y;
        updateSelectionDisplay();
    }

    public void setGridItem(int x, int y, String item) {
        gridButtons[y][x].setContent(item);
    }

    public String getGridItem(int x, int y) {
        return gridButtons[y][x].getContent();
    }

    public void setOutputItem(String item) {
        outputButton.setContent(item);
    }

    public String getOutputItem() {
        return outputButton.getContent();
    }

    private void updateSelectionDisplay() {
        if (selectedX != -1 && selectedY != -1) {
            String item = getGridItem(selectedX, selectedY);
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
