package com.vivo.customized.support.inter;

import android.content.ComponentName;
import android.graphics.Bitmap;

public interface VivoOperationControl {
    Bitmap captureScreen();

    boolean clearAppData(String str);

    void endCall();

    boolean formatSDCard();

    int getBackKeyEventState();

    int getFlightModeState();

    int getHomeKeyEventState();

    int getMenuKeyEventState();

    int getSafeModeState();

    int getStatusBarState();

    boolean isAccessibilityServcieEnable(ComponentName componentName);

    boolean isDevicePolicyManagerEnable(ComponentName componentName);

    boolean killProcess(String str);

    boolean reBoot();

    boolean setAccessibilityServcie(ComponentName componentName, boolean z);

    void setBackKeyEventState(int i);

    boolean setDefaultBrowser(ComponentName componentName, int i);

    boolean setDefaultEmail(ComponentName componentName, int i);

    boolean setDefaultLauncher(ComponentName componentName, int i);

    boolean setDevicePolicyManager(ComponentName componentName, boolean z);

    boolean setFlightModeState(int i);

    void setHomeKeyEventState(int i);

    void setMenuKeyEventState(int i);

    void setSafeModeState(int i);

    boolean setStatusBarState(int i);

    boolean shutDown();
}
