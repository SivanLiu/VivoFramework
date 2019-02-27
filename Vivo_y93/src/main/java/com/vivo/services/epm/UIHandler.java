package com.vivo.services.epm;

import android.os.Handler;
import com.android.server.UiThread;

public class UIHandler extends Handler {
    public UIHandler() {
        super(UiThread.get().getLooper(), null, true);
    }
}
