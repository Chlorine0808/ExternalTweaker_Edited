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
import com.bartz24.externaltweaker.app.ui.slots.AbstractSlot;

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

            if (!(btn instanceof AbstractSlot)) {
                return false;
            }

            AbstractSlot slot = (AbstractSlot) btn;

            // Check if item is valid for this slot
            if (!slot.isItemValid(data)) {
                return false;
            }

            if (btn == panel.getOutputButton()) {
                // Output slot logic
                // If it's an OreDict, we might need to resolve it to a representative item if
                // the slot doesn't support OreDict
                // But OutputItemStackSlot doesn't support OreDict, so isItemValid should handle
                // it.
                // However, if the user drags an OreDict here, we might want to try to resolve
                // it?
                // The previous logic did:
                /*
                 * String rawData = Utils.unformatItemId(data);
                 * if (rawData.startsWith("ore:")) {
                 * String rep = Utils.getOreDictRepresentativeItem(rawData);
                 * if (rep != null) {
                 * data = rep;
                 * }
                 * }
                 */
                // Let's keep this convenience for output if it's not valid as is
                if (!slot.isItemValid(data)) {
                    String rawData = Utils.unformatItemId(data);
                    if (rawData.startsWith("ore:")) {
                        String rep = Utils.getOreDictRepresentativeItem(rawData);
                        if (rep != null && slot.isItemValid(rep)) {
                            data = rep;
                        }
                    }
                }

                if (slot.isItemValid(data)) {
                    panel.setOutputItem(data);
                } else {
                    return false;
                }
            } else {
                // Grid slot logic
                // Find which grid button this is
                AbstractSlot[][] gridButtons = panel.getGridButtons();
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (gridButtons[y][x] == btn) {
                            // Check for swap
                            if (draggedButton != null && draggedButton != btn
                                    && draggedButton instanceof AbstractSlot) {
                                AbstractSlot sourceBtn = (AbstractSlot) draggedButton;
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
                                    // Check if swap is valid
                                    if (sourceBtn.isItemValid(currentTargetItem) && slot.isItemValid(data)) {
                                        panel.setGridItem(sourceX, sourceY, currentTargetItem);
                                        // Reset draggedButton to indicate swap was handled
                                        draggedButton = null;
                                    } else {
                                        // Swap not valid
                                        return false;
                                    }
                                }
                            }

                            // OreDict conversion logic if enabled
                            if (panel.isUseOreDict()) {
                                // Only convert if the slot accepts OreDict (which InputOreDictSlot does)
                                // And if it's not already an OreDict
                                if (!Utils.unformatItemId(data).startsWith("ore:")) {
                                    List<String> ores = OreDictRegistry.getInstance().getOreDictsForItem(data);
                                    if (!ores.isEmpty()) {
                                        String oreData = Utils.formatItemId("ore:" + ores.get(0));
                                        if (slot.isItemValid(oreData)) {
                                            data = oreData;
                                        }
                                    }
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
            AbstractSlot[][] gridButtons = panel.getGridButtons();
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
                    AbstractSlot[][] gridButtons = panel.getGridButtons();
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
            draggedButton = null;
        }
    }
}
