package com.bartz24.externaltweaker.app.ui.slots;

import com.bartz24.externaltweaker.app.AppFrame;

public class InputOreDictStackSlot extends InputOreDictSlot implements IStackable {

    public InputOreDictStackSlot(AppFrame appFrame) {
        super(appFrame);
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
