package com.bartz24.externaltweaker.app.ui.slots;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;

public class InputSlot extends AbstractItemSlot {

    public InputSlot(AppFrame appFrame) {
        super(appFrame);
    }

    @Override
    public boolean isItemValid(String id) {
        if (id == null || id.equals("null") || id.isEmpty())
            return true; // Empty is valid (clearing)

        String raw = Utils.unformatItemId(id);
        // Does not allow OreDict or Fluid
        if (raw.startsWith("ore:") || raw.startsWith("liquid:"))
            return false;

        return true;
    }
}
