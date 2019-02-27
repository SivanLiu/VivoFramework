package com.vivo.customized.support;

import android.content.ComponentName;
import android.graphics.Bitmap;
import com.vivo.customized.support.inter.VivoOperationControl;
import com.vivo.services.cust.VivoCustomManager;

class OperationManager extends BaseManager implements VivoOperationControl {
    OperationManager() {
    }

    public boolean shutDown() {
        return this.custManager.shutDown();
    }

    public boolean reBoot() {
        return this.custManager.reBoot();
    }

    public boolean formatSDCard() {
        return this.custManager.formatSDCard();
    }

    public boolean setDevicePolicyManager(ComponentName componentName, boolean isActive) {
        return this.custManager.setDevicePolicyManager(componentName, isActive);
    }

    public boolean isDevicePolicyManagerEnable(ComponentName componentName) {
        return this.custManager.isDevicePolicyManagerEnable(componentName);
    }

    public boolean setAccessibilityServcie(ComponentName componentName, boolean isActive) {
        return this.custManager.setAccessibilityServcie(componentName, isActive);
    }

    public boolean isAccessibilityServcieEnable(ComponentName componentName) {
        return this.custManager.isAccessibilityServcieEnable(componentName);
    }

    public boolean killProcess(String procName) {
        return this.custManager.killProcess(procName);
    }

    public boolean clearAppData(String packageName) {
        return this.custManager.clearAppData(packageName);
    }

    public boolean setDefaultLauncher(ComponentName componentName, int state) {
        return this.custManager.setDefaultLauncher(componentName, state);
    }

    public boolean setDefaultBrowser(ComponentName componentName, int state) {
        return this.custManager.setDefaultBrowser(componentName, state);
    }

    public boolean setDefaultEmail(ComponentName componentName, int state) {
        return this.custManager.setDefaultEmail(componentName, state);
    }

    public boolean setFlightModeState(int state) {
        return this.custManager.setFlightModeState(state);
    }

    public int getFlightModeState() {
        return this.custManager.getFlightModeState();
    }

    public boolean setStatusBarState(int state) {
        boolean z = true;
        VivoCustomManager vivoCustomManager = this.custManager;
        if (state != 1) {
            z = false;
        }
        return vivoCustomManager.setStatusBarState(z);
    }

    public int getStatusBarState() {
        return this.custManager.getStatusBarState() ? 1 : 0;
    }

    public void setHomeKeyEventState(int state) {
        this.custManager.setHomeKeyEventState(state);
    }

    public int getHomeKeyEventState() {
        return this.custManager.getHomeKeyEventState();
    }

    public void setMenuKeyEventState(int state) {
        this.custManager.setMenuKeyEventState(state);
    }

    public int getMenuKeyEventState() {
        return this.custManager.getMenuKeyEventState();
    }

    public void setBackKeyEventState(int state) {
        this.custManager.setBackKeyEventState(state);
    }

    public int getBackKeyEventState() {
        return this.custManager.getBackKeyEventState();
    }

    public void setSafeModeState(int state) {
        this.custManager.setSafeModeState(state);
    }

    public int getSafeModeState() {
        return this.custManager.getSafeModeState();
    }

    public Bitmap captureScreen() {
        return this.custManager.captureScreen(null);
    }

    public void endCall() {
        this.custManager.endCall();
    }
}
