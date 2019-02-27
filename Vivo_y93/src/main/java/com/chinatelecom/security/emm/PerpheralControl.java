package com.chinatelecom.security.emm;

import android.content.ComponentName;
import android.os.Bundle;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;

public interface PerpheralControl {
    public static final String TAG = "PerpheralControl";

    Bundle getMobileSettings(ComponentName componentName, String str, String str2) throws SecurityException;

    void setMobileSettings(ComponentName componentName, String str, Bundle bundle) throws SecurityException, IllegalParamaterException;
}
