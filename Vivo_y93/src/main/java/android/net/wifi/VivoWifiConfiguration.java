package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.HashMap;

public class VivoWifiConfiguration implements Parcelable {
    public static final int CONNECT_REASON_DHCP_FAILURE = 4;
    public static final int CONNECT_REASON_FULL_ERROR = 1;
    public static final int CONNECT_REASON_MAX = 6;
    public static final int CONNECT_REASON_NO_ERROR = 0;
    public static final int CONNECT_REASON_NO_RSP = 5;
    public static final int CONNECT_REASON_PASSWORD_ERROR = 3;
    public static final int CONNECT_REASON_REJECT_ERROR = 2;
    public static final String[] CONNECT_REASON_STRING = new String[]{"NO_ERROR", "FULL_ERROR", "REJECT_ERROR ", "PASSWORD_ERROR", "DHCP_FAILURE", "NO_RSP", "REASON_MAX"};
    public static final Creator<VivoWifiConfiguration> CREATOR = new Creator<VivoWifiConfiguration>() {
        public VivoWifiConfiguration createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            VivoWifiConfiguration config = new VivoWifiConfiguration();
            config.vivoConnectedCount = in.readInt();
            config.vivoConfigInitTime = in.readLong();
            config.vivoLastConnectedTime = in.readLong();
            config.vivoLastAutoEnabledTime = in.readLong();
            config.vivoNoInternetAccessTime = in.readInt();
            config.vivoLatitude = in.readDouble();
            config.vivoLongitude = in.readDouble();
            config.vivoIsInRange = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.vivoIsLastEnableAP = z;
            config.vivoNoInternetState = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.vivoNeedAutoJoinScan = z;
            config.vivoPoorRssiState = in.readInt();
            config.vivoAutoLogin = in.readInt();
            config.vivoConnectTipsTimes = in.readInt();
            config.vivoStatusCodeCounter = in.createIntArray();
            config.rssi2MobileThreshold = in.readDouble();
            config.netspeed2MobileThreshold = in.readDouble();
            config.linkSpeed2MobileThreshold = in.readDouble();
            config.rssiThresholdSampleCount = in.readInt();
            config.netSpeedThresholdSampleCount = in.readInt();
            if (in.readInt() == 0) {
                z2 = false;
            }
            config.isOneTouchConnectWifi = z2;
            config.vivoPortalState = in.readInt();
            return config;
        }

        public VivoWifiConfiguration[] newArray(int size) {
            return new VivoWifiConfiguration[size];
        }
    };
    private static final String TAG = "VivoWifiConfiguration";
    private int bssidNum;
    private boolean isOneTouchConnectWifi;
    private double linkSpeed2MobileThreshold;
    private int netSpeedThresholdSampleCount;
    private double netspeed2MobileThreshold;
    private double rssi2MobileThreshold;
    private int rssiThresholdSampleCount;
    private HashMap<String, Integer> vivoAccessPointContact;
    private int vivoAutoLogin;
    private long vivoConfigInitTime;
    public HashMap<String, Integer> vivoConnectChoices;
    private int vivoConnectTipsTimes;
    private int vivoConnectedCount;
    private long vivoDhcpHistoryLeaseTime;
    private boolean vivoIsInRange;
    private boolean vivoIsLastEnableAP;
    private long vivoLastAutoEnabledTime;
    private long vivoLastConnectedTime;
    private double vivoLatitude;
    private double vivoLongitude;
    private boolean vivoNeedAutoJoinScan;
    private int vivoNoInternetAccessTime;
    private int vivoNoInternetState;
    private int vivoPoorRssiState;
    private long vivoPoorRssiTimeMillis;
    private int vivoPoorRssiValue;
    private int vivoPortalState;
    private int[] vivoStatusCodeCounter;
    private boolean vivoValid;
    private double vivoWifiBeaconTimeoutRssiValue;
    private int vivoWifiDisableRssiValue;

    public VivoWifiConfiguration() {
        this.vivoAutoLogin = 2;
        this.vivoConnectTipsTimes = 0;
        this.vivoStatusCodeCounter = new int[6];
        this.rssi2MobileThreshold = (double) WifiConfiguration.INVALID_RSSI;
        this.netspeed2MobileThreshold = -1.0d;
        this.linkSpeed2MobileThreshold = 0.0d;
        this.rssiThresholdSampleCount = 0;
        this.netSpeedThresholdSampleCount = 0;
        this.vivoPortalState = 0;
        this.vivoConnectedCount = 0;
        this.vivoConfigInitTime = 0;
        this.vivoLastConnectedTime = 0;
        this.vivoLastAutoEnabledTime = 0;
        this.vivoNoInternetAccessTime = 0;
        this.vivoLatitude = 0.0d;
        this.vivoLongitude = 0.0d;
        this.vivoIsInRange = true;
        this.vivoIsLastEnableAP = false;
        this.vivoPoorRssiState = 0;
        this.vivoPoorRssiValue = -127;
        this.vivoWifiBeaconTimeoutRssiValue = -127.0d;
        this.vivoPoorRssiTimeMillis = 0;
        this.vivoNoInternetState = 0;
        this.vivoNeedAutoJoinScan = false;
        this.vivoAccessPointContact = new HashMap();
        this.vivoDhcpHistoryLeaseTime = 0;
        this.vivoValid = false;
        this.vivoConnectChoices = new HashMap();
        this.vivoWifiDisableRssiValue = -127;
        this.bssidNum = 0;
        this.vivoAutoLogin = 2;
        this.vivoConnectTipsTimes = 0;
        this.vivoStatusCodeCounter = new int[6];
        this.rssi2MobileThreshold = (double) WifiConfiguration.INVALID_RSSI;
        this.netspeed2MobileThreshold = -1.0d;
        this.linkSpeed2MobileThreshold = 0.0d;
        this.rssiThresholdSampleCount = 0;
        this.netSpeedThresholdSampleCount = 0;
        this.isOneTouchConnectWifi = false;
        this.vivoPortalState = 0;
    }

    public VivoWifiConfiguration(VivoWifiConfiguration source) {
        this();
        if (source != null) {
            this.vivoConnectedCount = source.vivoConnectedCount;
            this.vivoConfigInitTime = source.vivoConfigInitTime;
            this.vivoLastConnectedTime = source.vivoLastConnectedTime;
            this.vivoLastAutoEnabledTime = source.vivoLastAutoEnabledTime;
            this.vivoNoInternetAccessTime = source.vivoNoInternetAccessTime;
            this.vivoLatitude = source.vivoLatitude;
            this.vivoLongitude = source.vivoLongitude;
            this.vivoIsInRange = source.vivoIsInRange;
            this.vivoIsLastEnableAP = source.vivoIsLastEnableAP;
            this.vivoNoInternetState = source.vivoNoInternetState;
            this.vivoNeedAutoJoinScan = source.vivoNeedAutoJoinScan;
            if (source.vivoAccessPointContact != null && source.vivoAccessPointContact.size() > 0) {
                this.vivoAccessPointContact = new HashMap();
                this.vivoAccessPointContact.putAll(source.vivoAccessPointContact);
            }
            this.vivoDhcpHistoryLeaseTime = 0;
            this.vivoValid = source.vivoValid;
            this.vivoPoorRssiState = source.vivoPoorRssiState;
            if (source.vivoConnectChoices != null && source.vivoConnectChoices.size() > 0) {
                this.vivoConnectChoices = new HashMap();
                this.vivoConnectChoices.putAll(source.vivoConnectChoices);
            }
            this.vivoWifiDisableRssiValue = source.vivoWifiDisableRssiValue;
            this.bssidNum = source.bssidNum;
            this.vivoAutoLogin = source.vivoAutoLogin;
            this.vivoConnectTipsTimes = source.vivoConnectTipsTimes;
            if (source.vivoStatusCodeCounter != null) {
                for (int i = 0; i < 6; i++) {
                    this.vivoStatusCodeCounter[i] = source.vivoStatusCodeCounter[i];
                }
            }
            this.rssi2MobileThreshold = source.rssi2MobileThreshold;
            this.netspeed2MobileThreshold = source.netspeed2MobileThreshold;
            this.linkSpeed2MobileThreshold = source.linkSpeed2MobileThreshold;
            this.rssiThresholdSampleCount = source.rssiThresholdSampleCount;
            this.netSpeedThresholdSampleCount = source.netSpeedThresholdSampleCount;
            this.isOneTouchConnectWifi = source.isOneTouchConnectWifi;
            this.vivoPortalState = source.vivoPortalState;
        }
    }

    public String toString() {
        Integer choice;
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("VivoWifiConfiguration: \n");
        sbuf.append("\n");
        sbuf.append(" vivoConnectedCount: ").append(this.vivoConnectedCount).append(" vivoConfigInitTime: ").append(this.vivoConfigInitTime).append(10);
        sbuf.append(" vivoLastConnectedTime: ").append(this.vivoLastConnectedTime).append(" vivoLastAutoEnabledTime: ").append(this.vivoLastAutoEnabledTime).append(10);
        sbuf.append(" vivoNoInternetAccessTime: ").append(this.vivoNoInternetAccessTime).append(" vivoLatitude: ").append(this.vivoLatitude).append(10);
        sbuf.append(" vivoLongitude: ").append(this.vivoLongitude).append(" vivoIsInRange: ").append(this.vivoIsInRange).append(10);
        sbuf.append(" vivoIsLastEnableAP: ").append(this.vivoIsLastEnableAP).append(10);
        sbuf.append(" vivoNoInternetState: ").append(this.vivoNoInternetState).append(" vivoNeedAutoJoinScan: ").append(this.vivoNeedAutoJoinScan).append(10);
        sbuf.append(" vivoPoorRssiState: ").append(this.vivoPoorRssiState).append(10);
        if (this.vivoAccessPointContact != null) {
            for (String key : this.vivoAccessPointContact.keySet()) {
                choice = (Integer) this.vivoAccessPointContact.get(key);
                if (choice != null) {
                    sbuf.append(" apContact: ").append(key);
                    sbuf.append(" = ").append(choice);
                    sbuf.append(10);
                }
            }
        }
        sbuf.append(" vivoValid: ").append(this.vivoValid).append(10);
        if (this.vivoConnectChoices != null) {
            for (String key2 : this.vivoConnectChoices.keySet()) {
                choice = (Integer) this.vivoConnectChoices.get(key2);
                if (choice != null) {
                    sbuf.append(" choice: ").append(key2);
                    sbuf.append(" = ").append(choice);
                    sbuf.append(10);
                }
            }
        }
        sbuf.append(" vivoWifiDisableRssiValue: ").append(this.vivoWifiDisableRssiValue).append(10);
        sbuf.append(" bssidNum: ").append(this.bssidNum).append(10);
        sbuf.append(" vivoAutoLogin: ").append(this.vivoAutoLogin).append(10);
        sbuf.append(" vivoConnectTipsTimes: ").append(this.vivoConnectTipsTimes).append(10);
        if (this.vivoStatusCodeCounter != null) {
            for (int i = 0; i < 6; i++) {
                sbuf.append(CONNECT_REASON_STRING[i]).append(" :").append(this.vivoStatusCodeCounter[i]).append(10);
            }
        }
        sbuf.append(" rssi2MobileThreshold: ").append(this.rssi2MobileThreshold).append(10);
        sbuf.append(" netspeed2MobileThreshold: ").append(this.netspeed2MobileThreshold).append(10);
        sbuf.append(" linkSpeed2MobileThreshold: ").append(this.linkSpeed2MobileThreshold).append(10);
        sbuf.append(" rssiThresholdSampleCount: ").append(this.rssiThresholdSampleCount).append(10);
        sbuf.append(" netSpeedThresholdSampleCount: ").append(this.netSpeedThresholdSampleCount).append(10);
        sbuf.append("isOneTouchConnectWifi: ").append(this.isOneTouchConnectWifi).append(10);
        sbuf.append(" vivoPortalState: ").append(this.vivoPortalState).append(10);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.vivoConnectedCount);
        dest.writeLong(this.vivoConfigInitTime);
        dest.writeLong(this.vivoLastConnectedTime);
        dest.writeLong(this.vivoLastAutoEnabledTime);
        dest.writeInt(this.vivoNoInternetAccessTime);
        dest.writeDouble(this.vivoLatitude);
        dest.writeDouble(this.vivoLongitude);
        dest.writeInt(this.vivoIsInRange ? 1 : 0);
        if (this.vivoIsLastEnableAP) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.vivoNoInternetState);
        if (this.vivoNeedAutoJoinScan) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.vivoPoorRssiState);
        dest.writeInt(this.vivoAutoLogin);
        dest.writeInt(this.vivoConnectTipsTimes);
        dest.writeIntArray(this.vivoStatusCodeCounter);
        dest.writeDouble(this.rssi2MobileThreshold);
        dest.writeDouble(this.netspeed2MobileThreshold);
        dest.writeDouble(this.linkSpeed2MobileThreshold);
        dest.writeInt(this.rssiThresholdSampleCount);
        dest.writeInt(this.netSpeedThresholdSampleCount);
        if (!this.isOneTouchConnectWifi) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.vivoPortalState);
    }

    public void setVivoConnectedCount(int count) {
        this.vivoConnectedCount = count;
    }

    public int getVivoConnectedCount() {
        return this.vivoConnectedCount;
    }

    public void setVivoConfigInitTime(long time) {
        this.vivoConfigInitTime = time;
    }

    public long getVivoConfigInitTime() {
        return this.vivoConfigInitTime;
    }

    public void setVivoLastConnectedTime(long time) {
        this.vivoLastConnectedTime = time;
    }

    public long getVivoLastConnectedTime() {
        return this.vivoLastConnectedTime;
    }

    public void setVivoLastAutoEnabledTime(long time) {
        this.vivoLastAutoEnabledTime = time;
    }

    public long getVivoLastAutoEnabledTime() {
        return this.vivoLastAutoEnabledTime;
    }

    public void setVivoNoInternetAccessTime(int time) {
        this.vivoNoInternetAccessTime = time;
    }

    public int getVivoNoInternetAccessTime() {
        return this.vivoNoInternetAccessTime;
    }

    public void setVivoNoInternetState(int state) {
        this.vivoNoInternetState = state;
    }

    public int getVivoNoInternetState() {
        return this.vivoNoInternetState;
    }

    public void setVivoLatitude(double latitude) {
        this.vivoLatitude = latitude;
    }

    public double getVivoLatitude() {
        return this.vivoLatitude;
    }

    public void setVivoLongitude(double longitude) {
        this.vivoLongitude = longitude;
    }

    public double getVivoLongitude() {
        return this.vivoLongitude;
    }

    public void setVivoIsInRange(boolean inRange) {
        this.vivoIsInRange = inRange;
    }

    public boolean getVivoIsInRange() {
        return this.vivoIsInRange;
    }

    public void setVivoIsLastEnableAP(boolean isLastEnableAP) {
        this.vivoIsLastEnableAP = isLastEnableAP;
    }

    public boolean getVivoIsLastEnableAP() {
        return this.vivoIsLastEnableAP;
    }

    public void setVivoPoorRssiState(int poorRssiState) {
        this.vivoPoorRssiState = poorRssiState;
    }

    public int getVivoPoorRssiState() {
        return this.vivoPoorRssiState;
    }

    public void setVivoPoorRssiValue(int count) {
        this.vivoPoorRssiValue = count;
    }

    public int getVivoPoorRssiValue() {
        return this.vivoPoorRssiValue;
    }

    public void setVivoWifiBeaconTimeoutRssiValue(double count) {
        this.vivoWifiBeaconTimeoutRssiValue = count;
    }

    public double getVivoWifiBeaconTimeoutRssiValue() {
        return this.vivoWifiBeaconTimeoutRssiValue;
    }

    public void setVivoPoorRssiTimeMillis(long timeMillis) {
        this.vivoPoorRssiTimeMillis = timeMillis;
    }

    public long getVivoPoorRssiTimeMillis() {
        return this.vivoPoorRssiTimeMillis;
    }

    public void setVivoNeedAutoJoinScan(boolean needAutoJoinScan) {
        this.vivoNeedAutoJoinScan = needAutoJoinScan;
    }

    public boolean getVivoNeedAutoJoinScan() {
        return this.vivoNeedAutoJoinScan;
    }

    public void setVivoAccessPointContact(HashMap<String, Integer> hashMap) {
        this.vivoAccessPointContact = hashMap;
    }

    public void addVivoAccessPointContact(String key, int value) {
        this.vivoAccessPointContact.put(key, Integer.valueOf(value));
    }

    public void removeVivoAccessPointContact(String key) {
        this.vivoAccessPointContact.remove(key);
    }

    public int getVivoAccessPointContactSize() {
        return this.vivoAccessPointContact.size();
    }

    public Integer getVivoAccessPointContact(String key) {
        return (Integer) this.vivoAccessPointContact.get(key);
    }

    public HashMap<String, Integer> getVivoAccessPointContact() {
        return this.vivoAccessPointContact;
    }

    public void setVivoDhcpHistoryLeaseTime(long timeMillis) {
        this.vivoDhcpHistoryLeaseTime = timeMillis;
    }

    public long getVivoDhcpHistoryLeaseTime() {
        return this.vivoDhcpHistoryLeaseTime;
    }

    public void setVivoValid(boolean valid) {
        this.vivoValid = valid;
    }

    public boolean isValid() {
        return this.vivoValid;
    }

    public void setVivoWifiDisableRssiValue(int rssi) {
        this.vivoWifiDisableRssiValue = rssi;
    }

    public int getVivoWifiDisableRssiValue() {
        return this.vivoWifiDisableRssiValue;
    }

    public void setBssidNum(int num) {
        this.bssidNum = num;
    }

    public int getBssidNum() {
        return this.bssidNum;
    }

    public int getVivoAutoLogin() {
        Log.d(TAG, "getvivoAutoLogin " + this.vivoAutoLogin);
        return this.vivoAutoLogin;
    }

    public boolean setVivoAutoLogin(int autoLogin) {
        Log.d(TAG, "setvivoAutoLogin " + autoLogin);
        this.vivoAutoLogin = autoLogin;
        return true;
    }

    public void incrementVivoStatusCodeCounter(int reason) {
        Log.d(TAG, "incrementVivoStatusCodeCounter reason:" + reason);
        if (reason == 0) {
            clearVivoStatusCodeCounter();
        } else if (reason > 0 && reason < 6) {
            int[] iArr = this.vivoStatusCodeCounter;
            iArr[reason] = iArr[reason] + 1;
        }
    }

    public int getVivoStatusCodeCounter(int reason) {
        Log.d(TAG, "getVivoStatusCodeCounter reason:" + reason);
        int ret = -1;
        if (reason >= 0 && reason < 6) {
            ret = this.vivoStatusCodeCounter[reason];
        }
        Log.d(TAG, "getVivoStatusCodeCounter reason:" + reason + ", ret:" + ret);
        return ret;
    }

    public void clearVivoStatusCodeCounter(int reason) {
        Log.d(TAG, "clearVivoStatusCodeCounter reason:" + reason);
        if (reason >= 0 && reason < 6) {
            this.vivoStatusCodeCounter[reason] = 0;
        }
    }

    public void clearVivoStatusCodeCounter() {
        Log.d(TAG, "clearVivoStatusCodeCounter");
        for (int i = 0; i < 6; i++) {
            this.vivoStatusCodeCounter[i] = 0;
        }
    }

    public int getVivoConnectTipsTimes() {
        Log.d(TAG, "getVivoConnectTipsTimes vivoConnectTipsTimes:" + this.vivoConnectTipsTimes);
        return this.vivoConnectTipsTimes;
    }

    public void setVivoConnectTipsTimes(int times) {
        Log.d(TAG, "setVivoConnectTipsTimes times " + times);
        this.vivoConnectTipsTimes = times;
    }

    public void incrementVivoConnectTipsTimes() {
        this.vivoConnectTipsTimes++;
        Log.d(TAG, "incrementVivoConnectTipsTimes vivoConnectTipsTimes " + this.vivoConnectTipsTimes);
    }

    public void clearVivoConnectTipsTimes() {
        Log.d(TAG, "clearVivoConnectTipsTimes");
        this.vivoConnectTipsTimes = 0;
    }

    public void setRssi2MobileThreshold(double rssi) {
        this.rssi2MobileThreshold = rssi;
    }

    public double getRssi2MobileThreshold() {
        return this.rssi2MobileThreshold;
    }

    public void setNetSpeed2MobileThreshold(double threshold) {
        this.netspeed2MobileThreshold = threshold;
    }

    public double getNetSpeed2MobileThreshold() {
        return this.netspeed2MobileThreshold;
    }

    public void setLinkSpeed2MobileThreshold(double threshold) {
        this.linkSpeed2MobileThreshold = threshold;
    }

    public double getLinkSpeed2MobileThreshold() {
        return this.linkSpeed2MobileThreshold;
    }

    public void setRssiThresholdSampleCount(int count) {
        this.rssiThresholdSampleCount = count;
    }

    public void incrementRssiThresholdSampleCount() {
        this.rssiThresholdSampleCount++;
    }

    public int getRssiThresholdSampleCount() {
        return this.rssiThresholdSampleCount;
    }

    public void setNetSpeedThresholdSampleCount(int count) {
        this.netSpeedThresholdSampleCount = count;
    }

    public void incrementNetSpeedThresholdSampleCount() {
        this.netSpeedThresholdSampleCount++;
    }

    public int getNetSpeedThresholdSampleCount() {
        return this.rssiThresholdSampleCount;
    }

    public void setIsOneTouchConnectWifi(boolean isOneTouchConnect) {
        this.isOneTouchConnectWifi = isOneTouchConnect;
    }

    public boolean getIsOneTouchConnectWifi() {
        return this.isOneTouchConnectWifi;
    }

    public void setVivoPortalState(int state) {
        this.vivoPortalState = state;
    }

    public int getVivoPortalState() {
        return this.vivoPortalState;
    }
}
