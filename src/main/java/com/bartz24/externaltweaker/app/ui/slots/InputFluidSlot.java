package com.bartz24.externaltweaker.app.ui.slots;

import javax.swing.ImageIcon;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;

public class InputFluidSlot extends AbstractSlot implements IStackable {
    protected AppFrame appFrame;
    protected String currentFluid;
    protected int amount;

    public InputFluidSlot(AppFrame appFrame) {
        super();
        this.appFrame = appFrame;
    }

    @Override
    public boolean isItemValid(String id) {
        if (id == null || id.isEmpty())
            return true;

        String raw = Utils.unformatItemId(id);
        return raw.startsWith("liquid:");
    }

    @Override
    public void setContent(String id) {
        this.currentFluid = id;
        updateIcon(id);
        setToolTipText(id);
    }

    @Override
    public String getContent() {
        return currentFluid;
    }

    @Override
    public void updateIcon(String id) {
        if (id == null || id.isEmpty()) {
            setIcon(null);
            setText("");
            return;
        }

        setText(id);

        if (appFrame.iconLoader != null) {
            ImageIcon icon = appFrame.iconLoader.loadIcon(id, id, 64);
            setIcon(icon);
        }
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
