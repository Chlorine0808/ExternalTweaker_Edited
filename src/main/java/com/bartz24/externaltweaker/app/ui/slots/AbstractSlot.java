package com.bartz24.externaltweaker.app.ui.slots;

import javax.swing.JButton;

public abstract class AbstractSlot extends JButton implements IIconDrawable {

    protected int amount = 1;

    public AbstractSlot() {
        super();
    }

    public abstract boolean isItemValid(String id);

    public abstract void setContent(String id);

    public abstract String getContent();

    public int getAmount() {
        return amount;
    }
}
