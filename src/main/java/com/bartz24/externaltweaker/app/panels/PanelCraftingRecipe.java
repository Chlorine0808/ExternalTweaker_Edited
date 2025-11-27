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
                    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
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
                btn.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        TransferHandler handler = c.getTransferHandler();
                        handler.exportAsDrag(c, e, TransferHandler.MOVE);
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
        outputButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.MOVE);
            }
        });

        JLabel arrowLabel = new JLabel("->");
        arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        arrowLabel.setFont(arrowLabel.getFont().deriveFont(32.0f));

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
                    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                    JButton btn = (JButton) support.getComponent();

                    if (btn == outputButton) {
                        setOutputItem(data);
                    } else {
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
                    saveToRecipe();
                }
            }
        };

        // Re-apply handler
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
                        .addComponent(gridPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.CENTER)
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
                                        .addComponent(gridPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                        .addComponent(arrowLabel)
                                                        .addComponent(outputButton, GroupLayout.PREFERRED_SIZE, 120,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGap(20)
                                                .addComponent(trashPanel, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
    }

    public void setGridItem(int x, int y, String item) {
        gridData[y][x] = item;
        updateButtonDisplay(gridButtons[y][x], item, 64);
    }

    public String getGridItem(int x, int y) {
        return gridData[y][x];
    }

    public void setOutputItem(String item) {
        outputData = item;
        updateButtonDisplay(outputButton, item, 80);
    }

    public String getOutputItem() {
        return outputData;
    }

    private void updateButtonDisplay(JButton btn, String item, int iconSize) {
        if (item == null || item.equals("null") || item.isEmpty()) {
            btn.setText("");
            btn.setIcon(null);
            btn.setToolTipText(null);
            return;
        }

        String name = mainFrame.getNameFromId(item);
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
            ImageIcon icon = mainFrame.iconLoader.loadIcon(item, name, iconSize);
            btn.setIcon(icon);
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
