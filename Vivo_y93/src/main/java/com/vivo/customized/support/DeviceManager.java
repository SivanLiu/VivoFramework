package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoDeviceControl;

class DeviceManager extends BaseManager implements VivoDeviceControl {
    DeviceManager() {
    }

    public boolean setWifiState(int state) {
        return this.custManager.setWifiState(state);
    }

    public int getWifiState() {
        return this.custManager.getWifiState();
    }

    public boolean setWifiApState(int state) {
        return this.custManager.setWifiApState(state);
    }

    public int getWifiApState() {
        return this.custManager.getWifiApState();
    }

    public boolean setNFCState(int state) {
        return this.custManager.setNFCState(state);
    }

    public int getNFCState() {
        return this.custManager.getNFCState();
    }

    public boolean setBluetoothState(int state) {
        return this.custManager.setBluetoothState(state);
    }

    public int getBluetoothState() {
        return this.custManager.getBluetoothState();
    }

    public boolean setBluetoothApState(int state) {
        return this.custManager.setBluetoothApState(state);
    }

    public int getBluetoothApState() {
        return this.custManager.getBluetoothApState();
    }

    public boolean setGpsLocationState(int state) {
        return this.custManager.setGpsLocationState(state);
    }

    public int getGpsLocationState() {
        return this.custManager.getGpsLocationState();
    }

    public boolean setNetworkLocationState(int state) {
        return this.custManager.setNetworkLocationState(state);
    }

    public int getNetworkLocationState() {
        return this.custManager.getNetworkLocationState();
    }

    public boolean setUsbTransferState(int state) {
        return this.custManager.setUsbTransferState(state);
    }

    public int getUsbTransferState() {
        return this.custManager.getUsbTransferState();
    }

    public boolean setUsbApState(int state) {
        return this.custManager.setUsbApState(state);
    }

    public int getUsbApState() {
        return this.custManager.getUsbApState();
    }

    public boolean setCameraState(int state) {
        return this.custManager.setCameraState(state);
    }

    public int getCameraState() {
        return this.custManager.getCameraState();
    }

    public boolean setMicrophoneState(int state) {
        return this.custManager.setMicrophoneState(state);
    }

    public int getMicrophoneState() {
        return this.custManager.getMicrophoneState();
    }

    public boolean setScreenshotState(int state) {
        return this.custManager.setScreenshotState(state);
    }

    public int getScreenshotState() {
        return this.custManager.getScreenshotState();
    }

    public boolean setSDCardState(int state) {
        return this.custManager.setSDCardState(state);
    }

    public int getSDCardState() {
        return this.custManager.getSDCardState();
    }

    public boolean setOTGState(int state) {
        return this.custManager.setOTGState(state);
    }

    public int getOTGState() {
        return this.custManager.getOTGState();
    }

    public boolean setUsbDebugState(int state) {
        return this.custManager.setUsbDebugState(state);
    }

    public int getUsbDebugState() {
        return this.custManager.getUsbDebugState();
    }

    public boolean setAPNState(int state) {
        return this.custManager.setAPNState(state);
    }

    public int getAPNState() {
        return this.custManager.getAPNState();
    }

    public boolean setVPNState(int state) {
        return this.custManager.setVPNState(state);
    }

    public int getVPNState() {
        return this.custManager.getVPNState();
    }

    public boolean setTimeState(int state) {
        return this.custManager.setTimeState(state);
    }

    public int getTimeState() {
        return this.custManager.getTimeState();
    }

    public boolean setRestoreState(int state) {
        return this.custManager.setRestoreState(state);
    }

    public int getRestoreState() {
        return this.custManager.getRestoreState();
    }

    public boolean setFactoryResetState(int state) {
        return this.custManager.setFactoryResetState(state);
    }

    public int getFactoryResetState() {
        return this.custManager.getFactoryResetState();
    }
}
