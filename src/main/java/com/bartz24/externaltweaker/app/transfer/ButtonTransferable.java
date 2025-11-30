package com.bartz24.externaltweaker.app.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JButton;

public class ButtonTransferable implements Transferable {
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
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public JButton getSource() {
        return btn;
    }
}
