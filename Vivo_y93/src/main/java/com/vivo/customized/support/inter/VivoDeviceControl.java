package com.vivo.customized.support.inter;

public interface VivoDeviceControl {
    int getAPNState();

    int getBluetoothApState();

    int getBluetoothState();

    int getCameraState();

    int getFactoryResetState();

    int getGpsLocationState();

    int getMicrophoneState();

    int getNFCState();

    int getNetworkLocationState();

    int getOTGState();

    int getRestoreState();

    int getSDCardState();

    int getScreenshotState();

    int getTimeState();

    int getUsbApState();

    int getUsbDebugState();

    int getUsbTransferState();

    int getVPNState();

    int getWifiApState();

    int getWifiState();

    boolean setAPNState(int i);

    boolean setBluetoothApState(int i);

    boolean setBluetoothState(int i);

    boolean setCameraState(int i);

    boolean setFactoryResetState(int i);

    boolean setGpsLocationState(int i);

    boolean setMicrophoneState(int i);

    boolean setNFCState(int i);

    boolean setNetworkLocationState(int i);

    boolean setOTGState(int i);

    boolean setRestoreState(int i);

    boolean setSDCardState(int i);

    boolean setScreenshotState(int i);

    boolean setTimeState(int i);

    boolean setUsbApState(int i);

    boolean setUsbDebugState(int i);

    boolean setUsbTransferState(int i);

    boolean setVPNState(int i);

    boolean setWifiApState(int i);

    boolean setWifiState(int i);
}
