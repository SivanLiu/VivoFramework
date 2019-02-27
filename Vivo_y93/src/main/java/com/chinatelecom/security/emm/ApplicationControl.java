package com.chinatelecom.security.emm;

import android.content.ComponentName;
import android.os.Bundle;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import java.util.List;

public interface ApplicationControl {
    public static final String TAG = "ApplicationControl";

    void addTrustedAppStore(ComponentName componentName, String str) throws SecurityException, IllegalParamaterException;

    void deleteTrustedAppStore(ComponentName componentName, String str) throws SecurityException, IllegalParamaterException;

    void enableTrustedAppStore(ComponentName componentName, boolean z) throws SecurityException;

    Bundle getApplicationSettings(ComponentName componentName, String str, String str2) throws SecurityException, IllegalParamaterException;

    List<String> getTrustedAppStore(ComponentName componentName) throws SecurityException;

    void installPackage(ComponentName componentName, String str, String str2, boolean z) throws SecurityException, IllegalParamaterException;

    boolean isTrustedAppStoreEnabled(ComponentName componentName) throws SecurityException;

    void setApplicationSettings(ComponentName componentName, String str, Bundle bundle) throws SecurityException, IllegalParamaterException;

    void uninstallPackage(ComponentName componentName, String str, boolean z) throws SecurityException, IllegalParamaterException;
}
