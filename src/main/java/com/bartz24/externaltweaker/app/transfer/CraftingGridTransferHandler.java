package com.bartz24.externaltweaker.app.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.bartz24.externaltweaker.app.Utils;
import com.bartz24.externaltweaker.app.model.OreDictRegistry;
import com.bartz24.externaltweaker.app.panels.PanelCraftingRecipe;

public class CraftingGridTransferHandler extends TransferHandler {
    private PanelCraftingRecipe panel;
    private JButton draggedButton;

    public CraftingGridTransferHandler(PanelCraftingRecipe panel) {
        this.panel = panel;
    }

    public void setDraggedButton(JButton btn) {
        this.draggedButton = btn;
    }

    public JButton getDraggedButton() {
        return draggedButton;
    }

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

            if (btn == panel.getOutputButton()) {
                String rawData = Utils.unformatItemId(data);
                if (rawData.startsWith("ore:")) {
                    String rep = Utils.getOreDictRepresentativeItem(rawData);
                    if (rep != null) {
                        data = rep;
                    }
                }
                panel.setOutputItem(data);
            } else {
                // Find which grid button this is
                JButton[][] gridButtons = panel.getGridButtons();
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
                                    String currentTargetItem = panel.getGridItem(x, y);
                                    panel.setGridItem(sourceX, sourceY, currentTargetItem);
                                    // Reset draggedButton to indicate swap was handled
                                    draggedButton = null;
                                }
                            }

                            if (panel.isUseOreDict()) {
                                System.out.println("Checking OreDict for: " + data);
                                List<String> ores = OreDictRegistry.getInstance().getOreDictsForItem(data);
                                if (!ores.isEmpty()) {
                                    data = Utils.formatItemId("ore:" + ores.get(0));
                                    System.out.println("Converted to OreDict: " + data);
                                }
                            }
                            panel.setGridItem(x, y, data);
                            panel.setSelectedSlot(x, y);
                            panel.saveToRecipe();
                            return true;
                        }
                    }
                }
            }
            panel.saveToRecipe();
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
        if (btn == panel.getOutputButton()) {
            data = panel.getOutputItem();
        } else {
            JButton[][] gridButtons = panel.getGridButtons();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (gridButtons[y][x] == btn) {
                        data = panel.getGridItem(x, y);
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
                if (btn == panel.getOutputButton()) {
                    panel.setOutputItem("null");
                } else {
                    JButton[][] gridButtons = panel.getGridButtons();
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 3; x++) {
                            if (gridButtons[y][x] == btn) {
                                panel.setGridItem(x, y, "null");
                                break;
                            }
                        }
                    }
                }
            }
            panel.saveToRecipe();
            draggedButton = null; // Cleanup
        }
    }
}
