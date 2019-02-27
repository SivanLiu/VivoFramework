package com.vivo.api.ctgn;

import android.content.ComponentName;
import android.os.Bundle;
import com.chinatelecom.security.emm.PerpheralControl;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import com.vivo.services.cust.VivoCustomManager;

public class PerpheralManager implements PerpheralControl {
    private static final int BUSI_NETWORK = 1;
    private static final int BUSI_PERIPHERAL = 2;
    private static final int BUSI_SECURITY = 3;
    private VivoCustomManager custManager;

    public PerpheralManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public void setMobileSettings(ComponentName admin, String busi, Bundle setting) throws SecurityException, IllegalParamaterException {
        int i = 0;
        int i2 = 1;
        try {
            VivoCustomManager vivoCustomManager;
            VivoCustomManager vivoCustomManager2;
            int i3;
            switch (translate(busi)) {
                case 1:
                    if (setting.containsKey("WIFI")) {
                        this.custManager.setWifiState(setting.getInt("WIFI", 1));
                        return;
                    } else if (setting.containsKey("VPN")) {
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("VPN", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setVPNState(i2);
                        return;
                    } else if (setting.containsKey("TETHER")) {
                        vivoCustomManager2 = this.custManager;
                        if (setting.getBoolean("TETHER", true)) {
                            i3 = 1;
                        } else {
                            i3 = 0;
                        }
                        vivoCustomManager2.setWifiApState(i3);
                        vivoCustomManager2 = this.custManager;
                        if (setting.getBoolean("TETHER", true)) {
                            i3 = 1;
                        } else {
                            i3 = 0;
                        }
                        vivoCustomManager2.setBluetoothApState(i3);
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("TETHER", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setUsbApState(i2);
                        return;
                    } else if (setting.containsKey("BLUETOOTH")) {
                        this.custManager.setBluetoothState(setting.getInt("BLUETOOTH", 1));
                        return;
                    } else {
                        return;
                    }
                case 2:
                    if (setting.containsKey("LOCATION")) {
                        this.custManager.setGpsLocationState(setting.getInt("LOCATION", 1));
                        return;
                    } else if (setting.containsKey("CAMRA")) {
                        this.custManager.setCameraState(setting.getInt("CAMRA", 1));
                        return;
                    } else if (setting.containsKey("MICROPHONE")) {
                        this.custManager.setMicrophoneState(setting.getInt("MICROPHONE", 1));
                        return;
                    } else if (setting.containsKey("SCREEN")) {
                        this.custManager.setScreenshotState(setting.getInt("SCREEN", 1));
                        return;
                    } else if (setting.containsKey("SDCARD")) {
                        this.custManager.setSDCardState(setting.getInt("SDCARD", 1));
                        return;
                    } else if (setting.containsKey("USBTRANSFER")) {
                        this.custManager.setUsbTransferState(setting.getInt("USBTRANSFER", 1));
                        return;
                    } else if (!setting.containsKey("NFC") && setting.containsKey("OTG")) {
                        this.custManager.setOTGState(setting.getInt("OTG", 1));
                        return;
                    } else {
                        return;
                    }
                case 3:
                    if (setting.containsKey("FINGERPRINT")) {
                        vivoCustomManager2 = this.custManager;
                        if (setting.getBoolean("FINGERPRINT", true)) {
                            i3 = 1;
                        } else {
                            i3 = 0;
                        }
                        vivoCustomManager2.setFingerprintState(i3);
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("FINGERPRINT", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setFaceWakeState(i2);
                        return;
                    } else if (setting.containsKey("USBDEBUG")) {
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("USBDEBUG", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setUsbDebugState(i2);
                        return;
                    } else if (setting.containsKey("FACTORYRESET")) {
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("FACTORYRESET", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setFactoryResetState(i2);
                        return;
                    } else if (setting.containsKey("RESTORE")) {
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("RESTORE", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setRestoreState(i2);
                        return;
                    } else if (setting.containsKey("TIME")) {
                        vivoCustomManager = this.custManager;
                        if (!setting.getBoolean("TIME", true)) {
                            i2 = 0;
                        }
                        vivoCustomManager.setTimeState(i2);
                        return;
                    } else if (setting.containsKey("FLIGHT")) {
                        int value = setting.getInt("FLIGHT", 2);
                        if (2 == value || 3 == value) {
                            vivoCustomManager = this.custManager;
                            if (value != 2) {
                                i = 1;
                            }
                            vivoCustomManager.setFlightModeStateNormal(i);
                            return;
                        }
                        this.custManager.setFlightModeState(value);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setMobileSettings:IllegalParamaterException occur!");
        }
        throw new IllegalParamaterException("setMobileSettings:IllegalParamaterException occur!");
    }

    public Bundle getMobileSettings(ComponentName admin, String busi, String setting) throws SecurityException {
        boolean z = true;
        Bundle mBundle = new Bundle();
        switch (translate(busi)) {
            case 1:
                if (!setting.equals("WIFI")) {
                    if (!setting.equals("VPN")) {
                        if (!setting.equals("TETHER")) {
                            if (setting.equals("BLUETOOTH")) {
                                mBundle.putInt(setting, this.custManager.getBluetoothState());
                                break;
                            }
                        }
                        if (((this.custManager.getUsbApState() & this.custManager.getBluetoothApState()) & this.custManager.getWifiApState()) != 1) {
                            z = false;
                        }
                        mBundle.putBoolean(setting, z);
                        break;
                    }
                    if (this.custManager.getVPNState() != 1) {
                        z = false;
                    }
                    mBundle.putBoolean(setting, z);
                    break;
                }
                mBundle.putInt(setting, this.custManager.getWifiState());
                break;
                break;
            case 2:
                if (!setting.equals("LOCATION")) {
                    if (!setting.equals("CAMRA")) {
                        if (!setting.equals("MICROPHONE")) {
                            if (!setting.equals("SCREEN")) {
                                if (!setting.equals("SDCARD")) {
                                    if (!setting.equals("USBTRANSFER")) {
                                        if (!setting.equals("NFC")) {
                                            if (setting.equals("OTG")) {
                                                mBundle.putInt(setting, this.custManager.getOTGState());
                                                break;
                                            }
                                        }
                                        mBundle.putInt(setting, -1);
                                        break;
                                    }
                                    mBundle.putInt(setting, this.custManager.getUsbTransferState());
                                    break;
                                }
                                mBundle.putInt(setting, this.custManager.getSDCardState());
                                break;
                            }
                            mBundle.putInt(setting, this.custManager.getScreenshotState());
                            break;
                        }
                        mBundle.putInt(setting, this.custManager.getMicrophoneState());
                        break;
                    }
                    mBundle.putInt(setting, this.custManager.getCameraState());
                    break;
                }
                mBundle.putInt(setting, this.custManager.getGpsLocationState());
                break;
                break;
            case 3:
                if (!setting.equals("FINGERPRINT")) {
                    if (!setting.equals("USBDEBUG")) {
                        if (!setting.equals("FACTORYRESET")) {
                            if (!setting.equals("RESTORE")) {
                                if (!setting.equals("TIME")) {
                                    if (setting.equals("FLIGHT")) {
                                        int value = this.custManager.getFlightModeState();
                                        if (value == 1) {
                                            this.custManager.getFlightModeStateNormal();
                                        }
                                        mBundle.putInt(setting, value);
                                        break;
                                    }
                                }
                                if (this.custManager.getTimeState() != 1) {
                                    z = false;
                                }
                                mBundle.putBoolean(setting, z);
                                break;
                            }
                            if (this.custManager.getRestoreState() != 1) {
                                z = false;
                            }
                            mBundle.putBoolean(setting, z);
                            break;
                        }
                        if (this.custManager.getFactoryResetState() != 1) {
                            z = false;
                        }
                        mBundle.putBoolean(setting, z);
                        break;
                    }
                    if (this.custManager.getUsbDebugState() != 1) {
                        z = false;
                    }
                    mBundle.putBoolean(setting, z);
                    break;
                }
                if (this.custManager.getFingerprintState() != 1) {
                    z = false;
                }
                mBundle.putBoolean(setting, z);
                break;
                break;
        }
        return mBundle;
    }

    private int translate(String busi) {
        if (busi.equals("NETWORK")) {
            return 1;
        }
        if (busi.equals("PERIPHERAL")) {
            return 2;
        }
        if (busi.equals("SECURITY")) {
            return 3;
        }
        return -1;
    }
}
