package com.bartz24.externaltweaker.app.ui.slots;

import com.bartz24.externaltweaker.app.AppFrame;

public class OutputSlot extends AbstractItemSlot {

    public OutputSlot(AppFrame appFrame) {
        super(appFrame);
    }

    @Override
    public boolean isItemValid(String id) {
        return true; // Output can generally be anything
    }
}
