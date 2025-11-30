package com.bartz24.externaltweaker.app.ui.slots;

import com.bartz24.externaltweaker.app.AppFrame;
import com.bartz24.externaltweaker.app.Utils;

public class InputItemSlot extends AbstractItemSlot {

    public InputItemSlot(AppFrame appFrame) {
        super(appFrame);
    }

    @Override
    public boolean isItemValid(String id) {
        if (id == null || id.equals("null") || id.isEmpty())
            return true;

        String raw = Utils.unformatItemId(id);
        if (raw.startsWith("ore:") || raw.startsWith("liquid:"))
            return false;

        return true;
    }

}
