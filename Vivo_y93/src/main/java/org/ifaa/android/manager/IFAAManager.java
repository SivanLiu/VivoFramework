package org.ifaa.android.manager;

import android.content.Context;

public abstract class IFAAManager {
    public abstract String getDeviceModel();

    public abstract int getSupportBIOTypes(Context context);

    public abstract int getVersion();

    public abstract int startBIOManager(Context context, int i);
}
