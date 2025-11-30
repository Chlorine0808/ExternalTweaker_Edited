package com.bartz24.externaltweaker.app.ui.slots;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;

public class OutputItemStackSlot extends AbstractItemSlot implements IStackable {

    public OutputItemStackSlot(AppFrame appFrame) {
        super(appFrame);
    }

    @Override
    public boolean isItemValid(String id) {
        if (id == null || id.equals("null") || id.isEmpty())
            return true;

        String raw = Utils.unformatItemId(id);
        // Output cannot be OreDict or Fluid
        if (raw.startsWith("ore:") || raw.startsWith("liquid:"))
            return false;

        return true;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
