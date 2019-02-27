package org.ifaa.android.manager;

import android.content.Context;
import android.util.Log;

public class IFAAManagerFactory {
    private static final String TAG = "IFAAManagerFactory";

    public static IFAAManager getIFAAManager(Context context, int authType) {
        Log.d(TAG, "getIFAAManager enter");
        return new AlipayIFAAManager(context);
    }
}
