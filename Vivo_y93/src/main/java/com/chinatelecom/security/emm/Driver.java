package com.chinatelecom.security.emm;

import android.content.Context;

public interface Driver {
    APNControl getAPNControl(Context context);

    ApplicationControl getApplicationControl(Context context);

    CommControl getCommControl(Context context);

    DeviceControl getDeviceControl(Context context);

    PerpheralControl getPerpheralControl(Context context);

    SecurityControl getSecurityControl(Context context);
}
