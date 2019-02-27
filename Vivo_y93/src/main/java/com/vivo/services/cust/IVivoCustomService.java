package com.vivo.services.cust;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IVivoCustomService extends IInterface {

    public static abstract class Stub extends Binder implements IVivoCustomService {
        private static final String DESCRIPTOR = "com.vivo.services.cust.IVivoCustomService";
        static final int TRANSACTION_ClearDnsCache = 211;
        static final int TRANSACTION_addAppDataNetworkBlackList = 119;
        static final int TRANSACTION_addAppDataNetworkWhiteList = 122;
        static final int TRANSACTION_addAppWifiNetworkBlackList = 129;
        static final int TRANSACTION_addAppWifiNetworkWhiteList = 132;
        static final int TRANSACTION_addBluetoothBlackList = 165;
        static final int TRANSACTION_addBluetoothWhiteList = 168;
        static final int TRANSACTION_addDisabledApps = 26;
        static final int TRANSACTION_addInstallBlackList = 8;
        static final int TRANSACTION_addInstallWhiteList = 11;
        static final int TRANSACTION_addNotificationBlackList = 38;
        static final int TRANSACTION_addNotificationWhiteList = 41;
        static final int TRANSACTION_addPersistApps = 23;
        static final int TRANSACTION_addPhoneBlackList = 175;
        static final int TRANSACTION_addPhoneBlackListInfo = 176;
        static final int TRANSACTION_addPhoneWhiteList = 180;
        static final int TRANSACTION_addPhoneWhiteListInfo = 181;
        static final int TRANSACTION_addSmsBlackList = 192;
        static final int TRANSACTION_addSmsWhiteList = 195;
        static final int TRANSACTION_addTrustedAppStore = 29;
        static final int TRANSACTION_addTrustedAppStoreList = 30;
        static final int TRANSACTION_addUninstallBlackList = 16;
        static final int TRANSACTION_addUninstallWhiteList = 19;
        static final int TRANSACTION_addWlanBlackList = 156;
        static final int TRANSACTION_addWlanWhiteList = 159;
        static final int TRANSACTION_captureScreen = 75;
        static final int TRANSACTION_clearAppData = 57;
        static final int TRANSACTION_clearDomainNameBlackList = 139;
        static final int TRANSACTION_clearDomainNameWhiteList = 142;
        static final int TRANSACTION_clearIpAddrBlackList = 147;
        static final int TRANSACTION_clearIpAddrWhiteList = 150;
        static final int TRANSACTION_clearPackageState = 35;
        static final int TRANSACTION_deleteAppDataNetworkBlackList = 120;
        static final int TRANSACTION_deleteAppDataNetworkWhiteList = 123;
        static final int TRANSACTION_deleteAppWifiNetworkBlackList = 130;
        static final int TRANSACTION_deleteAppWifiNetworkWhiteList = 133;
        static final int TRANSACTION_deleteBluetoothBlackList = 166;
        static final int TRANSACTION_deleteBluetoothWhiteList = 169;
        static final int TRANSACTION_deleteInstallBlackList = 9;
        static final int TRANSACTION_deleteInstallWhiteList = 12;
        static final int TRANSACTION_deleteNotificationBlackList = 39;
        static final int TRANSACTION_deleteNotificationWhiteList = 42;
        static final int TRANSACTION_deletePackage = 3;
        static final int TRANSACTION_deletePackageWithObserver = 4;
        static final int TRANSACTION_deletePhoneBlackList = 177;
        static final int TRANSACTION_deletePhoneWhiteList = 182;
        static final int TRANSACTION_deleteSmsBlackList = 193;
        static final int TRANSACTION_deleteSmsWhiteList = 196;
        static final int TRANSACTION_deleteTrustedAppStore = 31;
        static final int TRANSACTION_deleteUninstallBlackList = 17;
        static final int TRANSACTION_deleteUninstallWhiteList = 20;
        static final int TRANSACTION_deleteWlanBlackList = 157;
        static final int TRANSACTION_deleteWlanWhiteList = 160;
        static final int TRANSACTION_disablePackage = 5;
        static final int TRANSACTION_endCall = 76;
        static final int TRANSACTION_forceStopPackage = 58;
        static final int TRANSACTION_formatSDCard = 51;
        static final int TRANSACTION_getAPNState = 108;
        static final int TRANSACTION_getAppDataNetworkBlackList = 121;
        static final int TRANSACTION_getAppDataNetworkPattern = 118;
        static final int TRANSACTION_getAppDataNetworkWhiteList = 124;
        static final int TRANSACTION_getAppWifiNetworkBlackList = 131;
        static final int TRANSACTION_getAppWifiNetworkPattern = 128;
        static final int TRANSACTION_getAppWifiNetworkWhiteList = 134;
        static final int TRANSACTION_getBackKeyEventState = 72;
        static final int TRANSACTION_getBluetoothApState = 86;
        static final int TRANSACTION_getBluetoothBlackList = 167;
        static final int TRANSACTION_getBluetoothRestrictPattern = 164;
        static final int TRANSACTION_getBluetoothState = 84;
        static final int TRANSACTION_getBluetoothWhiteList = 170;
        static final int TRANSACTION_getByPassOps = 221;
        static final int TRANSACTION_getByPassPermissions = 220;
        static final int TRANSACTION_getCameraState = 96;
        static final int TRANSACTION_getClipBoardState = 202;
        static final int TRANSACTION_getCustomizedApps = 218;
        static final int TRANSACTION_getDataNetworkState = 210;
        static final int TRANSACTION_getDefaultDeviceOwner = 219;
        static final int TRANSACTION_getDefaultSettingString = 222;
        static final int TRANSACTION_getDisableApps = 28;
        static final int TRANSACTION_getDomainNameBlackList = 140;
        static final int TRANSACTION_getDomainNamePattern = 137;
        static final int TRANSACTION_getDomainNameWhiteList = 143;
        static final int TRANSACTION_getFactoryResetState = 116;
        static final int TRANSACTION_getFlightModeState = 64;
        static final int TRANSACTION_getGpsLocationState = 88;
        static final int TRANSACTION_getHomeKeyEventState = 68;
        static final int TRANSACTION_getInstallBlackList = 10;
        static final int TRANSACTION_getInstallPattern = 7;
        static final int TRANSACTION_getInstallWhiteList = 13;
        static final int TRANSACTION_getIpAddrBlackList = 148;
        static final int TRANSACTION_getIpAddrPattern = 145;
        static final int TRANSACTION_getIpAddrWhiteList = 151;
        static final int TRANSACTION_getMenuKeyEventState = 70;
        static final int TRANSACTION_getMicrophoneState = 98;
        static final int TRANSACTION_getNFCState = 82;
        static final int TRANSACTION_getNetworkDataSimState = 207;
        static final int TRANSACTION_getNetworkLocationState = 90;
        static final int TRANSACTION_getNotificationBlackList = 40;
        static final int TRANSACTION_getNotificationRestrictPattern = 37;
        static final int TRANSACTION_getNotificationWhiteList = 43;
        static final int TRANSACTION_getOTGState = 104;
        static final int TRANSACTION_getPersistApps = 25;
        static final int TRANSACTION_getPhoneBlackList = 178;
        static final int TRANSACTION_getPhoneBlackListInfo = 179;
        static final int TRANSACTION_getPhoneIccids = 46;
        static final int TRANSACTION_getPhoneImeis = 47;
        static final int TRANSACTION_getPhoneNumbers = 45;
        static final int TRANSACTION_getPhoneRestrictPattern = 174;
        static final int TRANSACTION_getPhoneWhiteList = 183;
        static final int TRANSACTION_getPhoneWhiteListInfo = 184;
        static final int TRANSACTION_getRestoreState = 114;
        static final int TRANSACTION_getSDCardState = 102;
        static final int TRANSACTION_getSafeModeState = 74;
        static final int TRANSACTION_getScreenshotState = 100;
        static final int TRANSACTION_getSmsBlackList = 194;
        static final int TRANSACTION_getSmsRestrictPattern = 191;
        static final int TRANSACTION_getSmsWhiteList = 197;
        static final int TRANSACTION_getStatusBarState = 66;
        static final int TRANSACTION_getTelephonyDataState = 153;
        static final int TRANSACTION_getTelephonyMmsState = 187;
        static final int TRANSACTION_getTelephonyPhoneState = 172;
        static final int TRANSACTION_getTelephonySlotState = 200;
        static final int TRANSACTION_getTelephonySmsState = 189;
        static final int TRANSACTION_getTimeAutoState = 205;
        static final int TRANSACTION_getTimeState = 112;
        static final int TRANSACTION_getTrafficBytes = 48;
        static final int TRANSACTION_getTrustedAppStore = 32;
        static final int TRANSACTION_getTrustedAppStoreState = 34;
        static final int TRANSACTION_getUninstallBlackList = 18;
        static final int TRANSACTION_getUninstallPattern = 15;
        static final int TRANSACTION_getUninstallWhiteList = 21;
        static final int TRANSACTION_getUsbApState = 94;
        static final int TRANSACTION_getUsbDebugState = 106;
        static final int TRANSACTION_getUsbTransferState = 92;
        static final int TRANSACTION_getVPNState = 110;
        static final int TRANSACTION_getWifiApState = 80;
        static final int TRANSACTION_getWifiState = 78;
        static final int TRANSACTION_getWlanBlackList = 158;
        static final int TRANSACTION_getWlanRestrictPattern = 155;
        static final int TRANSACTION_getWlanWhiteList = 161;
        static final int TRANSACTION_installPackage = 1;
        static final int TRANSACTION_installPackageWithObserver = 2;
        static final int TRANSACTION_isAccessibilityServcieEnable = 55;
        static final int TRANSACTION_isAppDataNetworkWhiteListNotNull = 125;
        static final int TRANSACTION_isAppWifiNetworkWhiteListNotNull = 135;
        static final int TRANSACTION_isDevicePolicyManagerEnable = 53;
        static final int TRANSACTION_isDeviceRoot = 44;
        static final int TRANSACTION_killProcess = 56;
        static final int TRANSACTION_reBoot = 50;
        static final int TRANSACTION_registerNetworkListChangeCallback = 126;
        static final int TRANSACTION_registerPhoneListChangeCallback = 185;
        static final int TRANSACTION_registerSensitiveMmsListenerCallback = 224;
        static final int TRANSACTION_registerSmsListChangeCallback = 198;
        static final int TRANSACTION_registerUninstallListChangeCallback = 22;
        static final int TRANSACTION_registerWlanListChangeCallback = 162;
        static final int TRANSACTION_removeDisableApps = 27;
        static final int TRANSACTION_removePersistApps = 24;
        static final int TRANSACTION_setAPNState = 107;
        static final int TRANSACTION_setAccessibilityServcie = 54;
        static final int TRANSACTION_setAppDataNetworkPattern = 117;
        static final int TRANSACTION_setAppWifiNetworkPattern = 127;
        static final int TRANSACTION_setBackKeyEventState = 71;
        static final int TRANSACTION_setBluetoothApState = 85;
        static final int TRANSACTION_setBluetoothRestrictPattern = 163;
        static final int TRANSACTION_setBluetoothState = 83;
        static final int TRANSACTION_setCameraState = 95;
        static final int TRANSACTION_setClipBoardState = 201;
        static final int TRANSACTION_setDataNetworkState = 209;
        static final int TRANSACTION_setDefaultBrowser = 60;
        static final int TRANSACTION_setDefaultEmail = 61;
        static final int TRANSACTION_setDefaultLauncher = 59;
        static final int TRANSACTION_setDefaultMessage = 62;
        static final int TRANSACTION_setDevicePolicyManager = 52;
        static final int TRANSACTION_setDomainNameBlackList = 138;
        static final int TRANSACTION_setDomainNamePattern = 136;
        static final int TRANSACTION_setDomainNameWhiteList = 141;
        static final int TRANSACTION_setFactoryResetState = 115;
        static final int TRANSACTION_setFlightModeState = 63;
        static final int TRANSACTION_setGpsLocationState = 87;
        static final int TRANSACTION_setHomeKeyEventState = 67;
        static final int TRANSACTION_setInstallPattern = 6;
        static final int TRANSACTION_setIpAddrBlackList = 146;
        static final int TRANSACTION_setIpAddrPattern = 144;
        static final int TRANSACTION_setIpAddrWhiteList = 149;
        static final int TRANSACTION_setMenuKeyEventState = 69;
        static final int TRANSACTION_setMicrophoneState = 97;
        static final int TRANSACTION_setMmsKeywords = 225;
        static final int TRANSACTION_setNFCState = 81;
        static final int TRANSACTION_setNetworkDataSimState = 206;
        static final int TRANSACTION_setNetworkLocationState = 89;
        static final int TRANSACTION_setNotificationRestrictPattern = 36;
        static final int TRANSACTION_setOTGState = 103;
        static final int TRANSACTION_setPhoneRestrictPattern = 173;
        static final int TRANSACTION_setRestoreState = 113;
        static final int TRANSACTION_setSDCardState = 101;
        static final int TRANSACTION_setSafeModeState = 73;
        static final int TRANSACTION_setScreenshotState = 99;
        static final int TRANSACTION_setSmsRestrictPattern = 190;
        static final int TRANSACTION_setStatusBarState = 65;
        static final int TRANSACTION_setSystemTime = 203;
        static final int TRANSACTION_setTelephonyDataState = 152;
        static final int TRANSACTION_setTelephonyMmsState = 186;
        static final int TRANSACTION_setTelephonyPhoneState = 171;
        static final int TRANSACTION_setTelephonySlotState = 199;
        static final int TRANSACTION_setTelephonySmsState = 188;
        static final int TRANSACTION_setTimeAutoState = 204;
        static final int TRANSACTION_setTimeState = 111;
        static final int TRANSACTION_setTrustedAppStoreState = 33;
        static final int TRANSACTION_setUninstallPattern = 14;
        static final int TRANSACTION_setUsbApState = 93;
        static final int TRANSACTION_setUsbDebugState = 105;
        static final int TRANSACTION_setUsbTransferState = 91;
        static final int TRANSACTION_setVPNState = 109;
        static final int TRANSACTION_setVivoEmailPara = 208;
        static final int TRANSACTION_setWifiApState = 79;
        static final int TRANSACTION_setWifiState = 77;
        static final int TRANSACTION_setWlanRestrictPattern = 154;
        static final int TRANSACTION_shutDown = 49;
        static final int TRANSACTION_startCallRecord = 215;
        static final int TRANSACTION_startCallRecordEx = 216;
        static final int TRANSACTION_startCallRecordPolicy = 212;
        static final int TRANSACTION_startCallRecordPolicyEx = 213;
        static final int TRANSACTION_stopCallRecord = 217;
        static final int TRANSACTION_stopCallRecordPolicy = 214;
        static final int TRANSACTION_transmitSensitiveMms = 223;

        private static class Proxy implements IVivoCustomService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void installPackage(String path, int flags, String installerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeInt(flags);
                    _data.writeString(installerPackage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void installPackageWithObserver(String path, int flags, String installerPackage, IPackageInstallObserver2 observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeInt(flags);
                    _data.writeString(installerPackage);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackage(String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackageWithObserver(String packageName, int flags, IPackageDeleteObserver2 observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disablePackage(String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInstallPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInstallPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addInstallBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteInstallBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getInstallBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addInstallWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteInstallWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getInstallWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUninstallPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUninstallPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUninstallBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteUninstallBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getUninstallBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUninstallWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteUninstallWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getUninstallWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerUninstallListChangeCallback(IUninstallListListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPersistApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePersistApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPersistApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addDisabledApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeDisableApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDisableApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addTrustedAppStore(String pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgs);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addTrustedAppStoreList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deleteTrustedAppStore(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getTrustedAppStore() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTrustedAppStoreState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTrustedAppStoreState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPackageState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationRestrictPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNotificationRestrictPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNotificationBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteNotificationBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getNotificationBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNotificationWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteNotificationWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getNotificationWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDeviceRoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneNumbers() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneIccids() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneImeis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getTrafficBytes(int mode, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeString(packageName);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shutDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean reBoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean formatSDCard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDevicePolicyManager(ComponentName componentName, boolean isActive) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isActive) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDevicePolicyManagerEnable(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAccessibilityServcie(ComponentName componentName, boolean isActive) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isActive) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAccessibilityServcieEnable(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killProcess(String procName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(procName);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearAppData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean forceStopPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDefaultLauncher(ComponentName componentName, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDefaultBrowser(ComponentName componentName, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDefaultEmail(ComponentName componentName, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDefaultMessage(ComponentName componentName, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setFlightModeState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFlightModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setStatusBarState(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getStatusBarState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHomeKeyEventState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHomeKeyEventState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMenuKeyEventState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMenuKeyEventState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackKeyEventState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBackKeyEventState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSafeModeState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSafeModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle captureScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWifiState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiApState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWifiApState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNFCState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNFCState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBluetoothState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBluetoothState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBluetoothApState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBluetoothApState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setGpsLocationState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getGpsLocationState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetworkLocationState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkLocationState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setUsbTransferState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUsbTransferState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setUsbApState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUsbApState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setCameraState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCameraState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setMicrophoneState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMicrophoneState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setScreenshotState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getScreenshotState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSDCardState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSDCardState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setOTGState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(103, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getOTGState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(104, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setUsbDebugState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUsbDebugState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAPNState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAPNState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVPNState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVPNState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTimeState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTimeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRestoreState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(113, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRestoreState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setFactoryResetState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFactoryResetState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppDataNetworkPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(117, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppDataNetworkPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(118, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppDataNetworkBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAppDataNetworkBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(120, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAppDataNetworkBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(121, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppDataNetworkWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAppDataNetworkWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(123, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAppDataNetworkWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppDataNetworkWhiteListNotNull() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(125, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerNetworkListChangeCallback(INetworkListDelegate callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(126, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppWifiNetworkPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(127, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppWifiNetworkPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(128, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppWifiNetworkBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(129, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAppWifiNetworkBlackList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(130, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAppWifiNetworkBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(131, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppWifiNetworkWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(132, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteAppWifiNetworkWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(133, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAppWifiNetworkWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(134, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppWifiNetworkWhiteListNotNull() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(135, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDomainNamePattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(136, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDomainNamePattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(137, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDomainNameBlackList(List<String> urls, boolean isBlackList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(urls);
                    if (isBlackList) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(138, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDomainNameBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(139, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDomainNameBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(140, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDomainNameWhiteList(List<String> urls, boolean isWhiteList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(urls);
                    if (isWhiteList) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(141, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDomainNameWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(142, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDomainNameWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(143, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIpAddrPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(144, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIpAddrPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(145, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIpAddrBlackList(List<String> ips, boolean isBlackList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(ips);
                    if (isBlackList) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(146, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearIpAddrBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(147, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getIpAddrBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(148, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIpAddrWhiteList(List<String> ips, boolean isWhiteList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(ips);
                    if (isWhiteList) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(149, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearIpAddrWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(150, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getIpAddrWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(151, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTelephonyDataState(int simId, int dataState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simId);
                    _data.writeInt(dataState);
                    this.mRemote.transact(152, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTelephonyDataState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(153, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWlanRestrictPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(154, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWlanRestrictPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(155, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addWlanBlackList(List<String> iccids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(iccids);
                    this.mRemote.transact(156, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteWlanBlackList(List<String> iccids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(iccids);
                    this.mRemote.transact(157, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getWlanBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(158, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addWlanWhiteList(List<String> iccids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(iccids);
                    this.mRemote.transact(159, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteWlanWhiteList(List<String> iccids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(iccids);
                    this.mRemote.transact(160, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getWlanWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(161, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerWlanListChangeCallback(IWlanListListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(162, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBluetoothRestrictPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(163, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBluetoothRestrictPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(164, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addBluetoothBlackList(List<String> macs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(macs);
                    this.mRemote.transact(165, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteBluetoothBlackList(List<String> macs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(macs);
                    this.mRemote.transact(166, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getBluetoothBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(167, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addBluetoothWhiteList(List<String> macs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(macs);
                    this.mRemote.transact(168, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteBluetoothWhiteList(List<String> macs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(macs);
                    this.mRemote.transact(169, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getBluetoothWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(170, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTelephonyPhoneState(int simId, int callinState, int calloutState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simId);
                    _data.writeInt(callinState);
                    _data.writeInt(calloutState);
                    this.mRemote.transact(171, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTelephonyPhoneState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(172, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPhoneRestrictPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(173, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPhoneRestrictPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(174, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPhoneBlackList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(175, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPhoneBlackListInfo(String number, int inOutMode, int simID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeInt(inOutMode);
                    _data.writeInt(simID);
                    this.mRemote.transact(176, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePhoneBlackList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(177, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(178, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneBlackListInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(179, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPhoneWhiteList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(180, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPhoneWhiteListInfo(String number, int inOutMode, int simID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeInt(inOutMode);
                    _data.writeInt(simID);
                    this.mRemote.transact(181, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePhoneWhiteList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(182, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(183, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneWhiteListInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(184, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerPhoneListChangeCallback(IPhoneListListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(185, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTelephonyMmsState(int simId, int receiveState, int sendState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simId);
                    _data.writeInt(receiveState);
                    _data.writeInt(sendState);
                    this.mRemote.transact(186, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTelephonyMmsState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(187, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTelephonySmsState(int simId, int receiveState, int sendState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simId);
                    _data.writeInt(receiveState);
                    _data.writeInt(sendState);
                    this.mRemote.transact(188, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTelephonySmsState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(189, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSmsRestrictPattern(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(190, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSmsRestrictPattern() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(191, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSmsBlackList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(192, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteSmsBlackList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(193, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getSmsBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(194, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSmsWhiteList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(195, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteSmsWhiteList(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(196, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getSmsWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(197, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerSmsListChangeCallback(ISmsListListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(198, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTelephonySlotState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(199, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTelephonySlotState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(200, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setClipBoardState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(201, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getClipBoardState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(202, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSystemTime(long when) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(when);
                    this.mRemote.transact(203, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTimeAutoState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(204, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTimeAutoState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(205, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetworkDataSimState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(206, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkDataSimState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(207, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVivoEmailPara(ContentValues values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(208, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDataNetworkState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(209, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataNetworkState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(210, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean ClearDnsCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(211, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startCallRecordPolicy(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeInt(fileNameMode);
                    _data.writeInt(samplingRate);
                    _data.writeInt(fileFormat);
                    _data.writeString(fileExtension);
                    this.mRemote.transact(212, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startCallRecordPolicyEx(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeInt(fileNameMode);
                    _data.writeInt(samplingRate);
                    _data.writeInt(fileFormat);
                    _data.writeString(fileExtension);
                    if (disableToast) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!hide) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(213, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopCallRecordPolicy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(214, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startCallRecord(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeString(fileName);
                    _data.writeInt(fileNameMode);
                    _data.writeInt(maxTime);
                    _data.writeInt(samplingRate);
                    _data.writeInt(fileFormat);
                    _data.writeString(fileExtension);
                    this.mRemote.transact(215, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startCallRecordEx(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeString(fileName);
                    _data.writeInt(fileNameMode);
                    _data.writeInt(maxTime);
                    _data.writeInt(samplingRate);
                    _data.writeInt(fileFormat);
                    _data.writeString(fileExtension);
                    if (disableToast) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!hide) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(216, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopCallRecord() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(217, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getCustomizedApps(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(218, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultDeviceOwner() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(219, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getByPassPermissions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(220, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getByPassOps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(221, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDefaultSettingString() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(222, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void transmitSensitiveMms(String str) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(str);
                    this.mRemote.transact(223, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerSensitiveMmsListenerCallback(ISensitiveMmsDelegate callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(224, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMmsKeywords(String str) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(str);
                    this.mRemote.transact(225, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVivoCustomService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVivoCustomService)) {
                return new Proxy(obj);
            }
            return (IVivoCustomService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            List<String> _result2;
            boolean _result3;
            ComponentName _arg0;
            String _result4;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    installPackage(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    installPackageWithObserver(data.readString(), data.readInt(), data.readString(), android.content.pm.IPackageInstallObserver2.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    deletePackage(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    deletePackageWithObserver(data.readString(), data.readInt(), android.content.pm.IPackageDeleteObserver2.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    disablePackage(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setInstallPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getInstallPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    addInstallBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    deleteInstallBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInstallBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    addInstallWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    deleteInstallWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInstallWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    setUninstallPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUninstallPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    addUninstallBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    deleteUninstallBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUninstallBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    addUninstallWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    deleteUninstallWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUninstallWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    registerUninstallListChangeCallback(com.vivo.services.cust.IUninstallListListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    addPersistApps(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    removePersistApps(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPersistApps();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    addDisabledApps(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    removeDisableApps(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDisableApps();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = addTrustedAppStore(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = addTrustedAppStoreList(data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = deleteTrustedAppStore(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getTrustedAppStore();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setTrustedAppStoreState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTrustedAppStoreState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    clearPackageState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    setNotificationRestrictPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 37:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNotificationRestrictPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    addNotificationBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    deleteNotificationBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNotificationBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    addNotificationWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    deleteNotificationWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 43:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNotificationWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 44:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isDeviceRoot();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 45:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneNumbers();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 46:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneIccids();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 47:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneImeis();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 48:
                    data.enforceInterface(DESCRIPTOR);
                    long _result5 = getTrafficBytes(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case 49:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = shutDown();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 50:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = reBoot();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 51:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = formatSDCard();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 52:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setDevicePolicyManager(_arg0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 53:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = isDevicePolicyManagerEnable(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 54:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setAccessibilityServcie(_arg0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 55:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = isAccessibilityServcieEnable(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 56:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = killProcess(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 57:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = clearAppData(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 58:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = forceStopPackage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 59:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setDefaultLauncher(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 60:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setDefaultBrowser(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 61:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setDefaultEmail(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 62:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result3 = setDefaultMessage(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 63:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setFlightModeState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 64:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFlightModeState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 65:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setStatusBarState(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 66:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getStatusBarState();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 67:
                    data.enforceInterface(DESCRIPTOR);
                    setHomeKeyEventState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 68:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getHomeKeyEventState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 69:
                    data.enforceInterface(DESCRIPTOR);
                    setMenuKeyEventState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 70:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMenuKeyEventState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 71:
                    data.enforceInterface(DESCRIPTOR);
                    setBackKeyEventState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 72:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBackKeyEventState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 73:
                    data.enforceInterface(DESCRIPTOR);
                    setSafeModeState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 74:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSafeModeState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 75:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result6 = captureScreen();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 76:
                    data.enforceInterface(DESCRIPTOR);
                    endCall();
                    reply.writeNoException();
                    return true;
                case 77:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setWifiState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 78:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWifiState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 79:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setWifiApState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 80:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWifiApState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 81:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setNFCState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 82:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNFCState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 83:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setBluetoothState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 84:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBluetoothState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 85:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setBluetoothApState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 86:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBluetoothApState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 87:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setGpsLocationState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 88:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getGpsLocationState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 89:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setNetworkLocationState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 90:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNetworkLocationState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 91:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setUsbTransferState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 92:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUsbTransferState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 93:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setUsbApState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 94:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUsbApState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 95:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setCameraState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 96:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCameraState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 97:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setMicrophoneState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 98:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMicrophoneState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 99:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setScreenshotState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 100:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getScreenshotState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 101:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSDCardState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 102:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSDCardState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 103:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setOTGState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 104:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getOTGState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 105:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setUsbDebugState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 106:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUsbDebugState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 107:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setAPNState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 108:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAPNState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 109:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setVPNState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 110:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVPNState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 111:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setTimeState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 112:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTimeState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 113:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setRestoreState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 114:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getRestoreState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 115:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setFactoryResetState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 116:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFactoryResetState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 117:
                    data.enforceInterface(DESCRIPTOR);
                    setAppDataNetworkPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 118:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAppDataNetworkPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 119:
                    data.enforceInterface(DESCRIPTOR);
                    addAppDataNetworkBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 120:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAppDataNetworkBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 121:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppDataNetworkBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 122:
                    data.enforceInterface(DESCRIPTOR);
                    addAppDataNetworkWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 123:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAppDataNetworkWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 124:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppDataNetworkWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 125:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isAppDataNetworkWhiteListNotNull();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 126:
                    data.enforceInterface(DESCRIPTOR);
                    registerNetworkListChangeCallback(com.vivo.services.cust.INetworkListDelegate.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 127:
                    data.enforceInterface(DESCRIPTOR);
                    setAppWifiNetworkPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 128:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAppWifiNetworkPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 129:
                    data.enforceInterface(DESCRIPTOR);
                    addAppWifiNetworkBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 130:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAppWifiNetworkBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 131:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppWifiNetworkBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 132:
                    data.enforceInterface(DESCRIPTOR);
                    addAppWifiNetworkWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 133:
                    data.enforceInterface(DESCRIPTOR);
                    deleteAppWifiNetworkWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 134:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppWifiNetworkWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 135:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isAppWifiNetworkWhiteListNotNull();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 136:
                    data.enforceInterface(DESCRIPTOR);
                    setDomainNamePattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 137:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDomainNamePattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 138:
                    data.enforceInterface(DESCRIPTOR);
                    setDomainNameBlackList(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 139:
                    data.enforceInterface(DESCRIPTOR);
                    clearDomainNameBlackList();
                    reply.writeNoException();
                    return true;
                case 140:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDomainNameBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 141:
                    data.enforceInterface(DESCRIPTOR);
                    setDomainNameWhiteList(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 142:
                    data.enforceInterface(DESCRIPTOR);
                    clearDomainNameWhiteList();
                    reply.writeNoException();
                    return true;
                case 143:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDomainNameWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 144:
                    data.enforceInterface(DESCRIPTOR);
                    setIpAddrPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 145:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getIpAddrPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 146:
                    data.enforceInterface(DESCRIPTOR);
                    setIpAddrBlackList(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 147:
                    data.enforceInterface(DESCRIPTOR);
                    clearIpAddrBlackList();
                    reply.writeNoException();
                    return true;
                case 148:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getIpAddrBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 149:
                    data.enforceInterface(DESCRIPTOR);
                    setIpAddrWhiteList(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 150:
                    data.enforceInterface(DESCRIPTOR);
                    clearIpAddrWhiteList();
                    reply.writeNoException();
                    return true;
                case 151:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getIpAddrWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 152:
                    data.enforceInterface(DESCRIPTOR);
                    setTelephonyDataState(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 153:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getTelephonyDataState();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 154:
                    data.enforceInterface(DESCRIPTOR);
                    setWlanRestrictPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 155:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWlanRestrictPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 156:
                    data.enforceInterface(DESCRIPTOR);
                    addWlanBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 157:
                    data.enforceInterface(DESCRIPTOR);
                    deleteWlanBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 158:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWlanBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 159:
                    data.enforceInterface(DESCRIPTOR);
                    addWlanWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 160:
                    data.enforceInterface(DESCRIPTOR);
                    deleteWlanWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 161:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWlanWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 162:
                    data.enforceInterface(DESCRIPTOR);
                    registerWlanListChangeCallback(com.vivo.services.cust.IWlanListListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 163:
                    data.enforceInterface(DESCRIPTOR);
                    setBluetoothRestrictPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 164:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBluetoothRestrictPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 165:
                    data.enforceInterface(DESCRIPTOR);
                    addBluetoothBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 166:
                    data.enforceInterface(DESCRIPTOR);
                    deleteBluetoothBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 167:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getBluetoothBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 168:
                    data.enforceInterface(DESCRIPTOR);
                    addBluetoothWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 169:
                    data.enforceInterface(DESCRIPTOR);
                    deleteBluetoothWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 170:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getBluetoothWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 171:
                    data.enforceInterface(DESCRIPTOR);
                    setTelephonyPhoneState(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 172:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getTelephonyPhoneState();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 173:
                    data.enforceInterface(DESCRIPTOR);
                    setPhoneRestrictPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 174:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPhoneRestrictPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 175:
                    data.enforceInterface(DESCRIPTOR);
                    addPhoneBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 176:
                    data.enforceInterface(DESCRIPTOR);
                    addPhoneBlackListInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 177:
                    data.enforceInterface(DESCRIPTOR);
                    deletePhoneBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 178:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 179:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneBlackListInfo();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 180:
                    data.enforceInterface(DESCRIPTOR);
                    addPhoneWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 181:
                    data.enforceInterface(DESCRIPTOR);
                    addPhoneWhiteListInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 182:
                    data.enforceInterface(DESCRIPTOR);
                    deletePhoneWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 183:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 184:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneWhiteListInfo();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 185:
                    data.enforceInterface(DESCRIPTOR);
                    registerPhoneListChangeCallback(com.vivo.services.cust.IPhoneListListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 186:
                    data.enforceInterface(DESCRIPTOR);
                    setTelephonyMmsState(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 187:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getTelephonyMmsState();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 188:
                    data.enforceInterface(DESCRIPTOR);
                    setTelephonySmsState(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 189:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getTelephonySmsState();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 190:
                    data.enforceInterface(DESCRIPTOR);
                    setSmsRestrictPattern(data.readInt());
                    reply.writeNoException();
                    return true;
                case 191:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSmsRestrictPattern();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 192:
                    data.enforceInterface(DESCRIPTOR);
                    addSmsBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 193:
                    data.enforceInterface(DESCRIPTOR);
                    deleteSmsBlackList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 194:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSmsBlackList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 195:
                    data.enforceInterface(DESCRIPTOR);
                    addSmsWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 196:
                    data.enforceInterface(DESCRIPTOR);
                    deleteSmsWhiteList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 197:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSmsWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 198:
                    data.enforceInterface(DESCRIPTOR);
                    registerSmsListChangeCallback(com.vivo.services.cust.ISmsListListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 199:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setTelephonySlotState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 200:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTelephonySlotState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 201:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setClipBoardState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 202:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getClipBoardState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 203:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSystemTime(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 204:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setTimeAutoState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 205:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTimeAutoState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 206:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setNetworkDataSimState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 207:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNetworkDataSimState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 208:
                    ContentValues _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result3 = setVivoEmailPara(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 209:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setDataNetworkState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 210:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDataNetworkState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 211:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = ClearDnsCache();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 212:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startCallRecordPolicy(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 213:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startCallRecordPolicyEx(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 214:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = stopCallRecordPolicy();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 215:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startCallRecord(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 216:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = startCallRecordEx(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 217:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = stopCallRecord();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 218:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCustomizedApps(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 219:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getDefaultDeviceOwner();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 220:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getByPassPermissions();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 221:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getByPassOps();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 222:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultSettingString();
                    reply.writeNoException();
                    reply.writeStringList(_result2);
                    return true;
                case 223:
                    data.enforceInterface(DESCRIPTOR);
                    transmitSensitiveMms(data.readString());
                    reply.writeNoException();
                    return true;
                case 224:
                    data.enforceInterface(DESCRIPTOR);
                    registerSensitiveMmsListenerCallback(com.vivo.services.cust.ISensitiveMmsDelegate.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 225:
                    data.enforceInterface(DESCRIPTOR);
                    setMmsKeywords(data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean ClearDnsCache() throws RemoteException;

    void addAppDataNetworkBlackList(List<String> list) throws RemoteException;

    void addAppDataNetworkWhiteList(List<String> list) throws RemoteException;

    void addAppWifiNetworkBlackList(List<String> list) throws RemoteException;

    void addAppWifiNetworkWhiteList(List<String> list) throws RemoteException;

    void addBluetoothBlackList(List<String> list) throws RemoteException;

    void addBluetoothWhiteList(List<String> list) throws RemoteException;

    void addDisabledApps(List<String> list) throws RemoteException;

    void addInstallBlackList(List<String> list) throws RemoteException;

    void addInstallWhiteList(List<String> list) throws RemoteException;

    void addNotificationBlackList(List<String> list) throws RemoteException;

    void addNotificationWhiteList(List<String> list) throws RemoteException;

    void addPersistApps(List<String> list) throws RemoteException;

    void addPhoneBlackList(List<String> list) throws RemoteException;

    void addPhoneBlackListInfo(String str, int i, int i2) throws RemoteException;

    void addPhoneWhiteList(List<String> list) throws RemoteException;

    void addPhoneWhiteListInfo(String str, int i, int i2) throws RemoteException;

    void addSmsBlackList(List<String> list) throws RemoteException;

    void addSmsWhiteList(List<String> list) throws RemoteException;

    boolean addTrustedAppStore(String str) throws RemoteException;

    boolean addTrustedAppStoreList(List<String> list) throws RemoteException;

    void addUninstallBlackList(List<String> list) throws RemoteException;

    void addUninstallWhiteList(List<String> list) throws RemoteException;

    void addWlanBlackList(List<String> list) throws RemoteException;

    void addWlanWhiteList(List<String> list) throws RemoteException;

    Bundle captureScreen() throws RemoteException;

    boolean clearAppData(String str) throws RemoteException;

    void clearDomainNameBlackList() throws RemoteException;

    void clearDomainNameWhiteList() throws RemoteException;

    void clearIpAddrBlackList() throws RemoteException;

    void clearIpAddrWhiteList() throws RemoteException;

    void clearPackageState(int i) throws RemoteException;

    void deleteAppDataNetworkBlackList(List<String> list) throws RemoteException;

    void deleteAppDataNetworkWhiteList(List<String> list) throws RemoteException;

    void deleteAppWifiNetworkBlackList(List<String> list) throws RemoteException;

    void deleteAppWifiNetworkWhiteList(List<String> list) throws RemoteException;

    void deleteBluetoothBlackList(List<String> list) throws RemoteException;

    void deleteBluetoothWhiteList(List<String> list) throws RemoteException;

    void deleteInstallBlackList(List<String> list) throws RemoteException;

    void deleteInstallWhiteList(List<String> list) throws RemoteException;

    void deleteNotificationBlackList(List<String> list) throws RemoteException;

    void deleteNotificationWhiteList(List<String> list) throws RemoteException;

    void deletePackage(String str, int i) throws RemoteException;

    void deletePackageWithObserver(String str, int i, IPackageDeleteObserver2 iPackageDeleteObserver2) throws RemoteException;

    void deletePhoneBlackList(List<String> list) throws RemoteException;

    void deletePhoneWhiteList(List<String> list) throws RemoteException;

    void deleteSmsBlackList(List<String> list) throws RemoteException;

    void deleteSmsWhiteList(List<String> list) throws RemoteException;

    boolean deleteTrustedAppStore(String str) throws RemoteException;

    void deleteUninstallBlackList(List<String> list) throws RemoteException;

    void deleteUninstallWhiteList(List<String> list) throws RemoteException;

    void deleteWlanBlackList(List<String> list) throws RemoteException;

    void deleteWlanWhiteList(List<String> list) throws RemoteException;

    void disablePackage(String str, int i) throws RemoteException;

    void endCall() throws RemoteException;

    boolean forceStopPackage(String str) throws RemoteException;

    boolean formatSDCard() throws RemoteException;

    int getAPNState() throws RemoteException;

    List<String> getAppDataNetworkBlackList() throws RemoteException;

    int getAppDataNetworkPattern() throws RemoteException;

    List<String> getAppDataNetworkWhiteList() throws RemoteException;

    List<String> getAppWifiNetworkBlackList() throws RemoteException;

    int getAppWifiNetworkPattern() throws RemoteException;

    List<String> getAppWifiNetworkWhiteList() throws RemoteException;

    int getBackKeyEventState() throws RemoteException;

    int getBluetoothApState() throws RemoteException;

    List<String> getBluetoothBlackList() throws RemoteException;

    int getBluetoothRestrictPattern() throws RemoteException;

    int getBluetoothState() throws RemoteException;

    List<String> getBluetoothWhiteList() throws RemoteException;

    List<String> getByPassOps() throws RemoteException;

    List<String> getByPassPermissions() throws RemoteException;

    int getCameraState() throws RemoteException;

    int getClipBoardState() throws RemoteException;

    List<String> getCustomizedApps(int i) throws RemoteException;

    int getDataNetworkState() throws RemoteException;

    String getDefaultDeviceOwner() throws RemoteException;

    List<String> getDefaultSettingString() throws RemoteException;

    List<String> getDisableApps() throws RemoteException;

    List<String> getDomainNameBlackList() throws RemoteException;

    int getDomainNamePattern() throws RemoteException;

    List<String> getDomainNameWhiteList() throws RemoteException;

    int getFactoryResetState() throws RemoteException;

    int getFlightModeState() throws RemoteException;

    int getGpsLocationState() throws RemoteException;

    int getHomeKeyEventState() throws RemoteException;

    List<String> getInstallBlackList() throws RemoteException;

    int getInstallPattern() throws RemoteException;

    List<String> getInstallWhiteList() throws RemoteException;

    List<String> getIpAddrBlackList() throws RemoteException;

    int getIpAddrPattern() throws RemoteException;

    List<String> getIpAddrWhiteList() throws RemoteException;

    int getMenuKeyEventState() throws RemoteException;

    int getMicrophoneState() throws RemoteException;

    int getNFCState() throws RemoteException;

    int getNetworkDataSimState() throws RemoteException;

    int getNetworkLocationState() throws RemoteException;

    List<String> getNotificationBlackList() throws RemoteException;

    int getNotificationRestrictPattern() throws RemoteException;

    List<String> getNotificationWhiteList() throws RemoteException;

    int getOTGState() throws RemoteException;

    List<String> getPersistApps() throws RemoteException;

    List<String> getPhoneBlackList() throws RemoteException;

    List<String> getPhoneBlackListInfo() throws RemoteException;

    List<String> getPhoneIccids() throws RemoteException;

    List<String> getPhoneImeis() throws RemoteException;

    List<String> getPhoneNumbers() throws RemoteException;

    int getPhoneRestrictPattern() throws RemoteException;

    List<String> getPhoneWhiteList() throws RemoteException;

    List<String> getPhoneWhiteListInfo() throws RemoteException;

    int getRestoreState() throws RemoteException;

    int getSDCardState() throws RemoteException;

    int getSafeModeState() throws RemoteException;

    int getScreenshotState() throws RemoteException;

    List<String> getSmsBlackList() throws RemoteException;

    int getSmsRestrictPattern() throws RemoteException;

    List<String> getSmsWhiteList() throws RemoteException;

    boolean getStatusBarState() throws RemoteException;

    String getTelephonyDataState() throws RemoteException;

    String getTelephonyMmsState() throws RemoteException;

    String getTelephonyPhoneState() throws RemoteException;

    int getTelephonySlotState() throws RemoteException;

    String getTelephonySmsState() throws RemoteException;

    int getTimeAutoState() throws RemoteException;

    int getTimeState() throws RemoteException;

    long getTrafficBytes(int i, String str) throws RemoteException;

    List<String> getTrustedAppStore() throws RemoteException;

    int getTrustedAppStoreState() throws RemoteException;

    List<String> getUninstallBlackList() throws RemoteException;

    int getUninstallPattern() throws RemoteException;

    List<String> getUninstallWhiteList() throws RemoteException;

    int getUsbApState() throws RemoteException;

    int getUsbDebugState() throws RemoteException;

    int getUsbTransferState() throws RemoteException;

    int getVPNState() throws RemoteException;

    int getWifiApState() throws RemoteException;

    int getWifiState() throws RemoteException;

    List<String> getWlanBlackList() throws RemoteException;

    int getWlanRestrictPattern() throws RemoteException;

    List<String> getWlanWhiteList() throws RemoteException;

    void installPackage(String str, int i, String str2) throws RemoteException;

    void installPackageWithObserver(String str, int i, String str2, IPackageInstallObserver2 iPackageInstallObserver2) throws RemoteException;

    boolean isAccessibilityServcieEnable(ComponentName componentName) throws RemoteException;

    boolean isAppDataNetworkWhiteListNotNull() throws RemoteException;

    boolean isAppWifiNetworkWhiteListNotNull() throws RemoteException;

    boolean isDevicePolicyManagerEnable(ComponentName componentName) throws RemoteException;

    boolean isDeviceRoot() throws RemoteException;

    boolean killProcess(String str) throws RemoteException;

    boolean reBoot() throws RemoteException;

    void registerNetworkListChangeCallback(INetworkListDelegate iNetworkListDelegate) throws RemoteException;

    void registerPhoneListChangeCallback(IPhoneListListener iPhoneListListener) throws RemoteException;

    void registerSensitiveMmsListenerCallback(ISensitiveMmsDelegate iSensitiveMmsDelegate) throws RemoteException;

    void registerSmsListChangeCallback(ISmsListListener iSmsListListener) throws RemoteException;

    void registerUninstallListChangeCallback(IUninstallListListener iUninstallListListener) throws RemoteException;

    void registerWlanListChangeCallback(IWlanListListener iWlanListListener) throws RemoteException;

    void removeDisableApps(List<String> list) throws RemoteException;

    void removePersistApps(List<String> list) throws RemoteException;

    boolean setAPNState(int i) throws RemoteException;

    boolean setAccessibilityServcie(ComponentName componentName, boolean z) throws RemoteException;

    void setAppDataNetworkPattern(int i) throws RemoteException;

    void setAppWifiNetworkPattern(int i) throws RemoteException;

    void setBackKeyEventState(int i) throws RemoteException;

    boolean setBluetoothApState(int i) throws RemoteException;

    void setBluetoothRestrictPattern(int i) throws RemoteException;

    boolean setBluetoothState(int i) throws RemoteException;

    boolean setCameraState(int i) throws RemoteException;

    boolean setClipBoardState(int i) throws RemoteException;

    boolean setDataNetworkState(int i) throws RemoteException;

    boolean setDefaultBrowser(ComponentName componentName, int i) throws RemoteException;

    boolean setDefaultEmail(ComponentName componentName, int i) throws RemoteException;

    boolean setDefaultLauncher(ComponentName componentName, int i) throws RemoteException;

    boolean setDefaultMessage(ComponentName componentName, int i) throws RemoteException;

    boolean setDevicePolicyManager(ComponentName componentName, boolean z) throws RemoteException;

    void setDomainNameBlackList(List<String> list, boolean z) throws RemoteException;

    void setDomainNamePattern(int i) throws RemoteException;

    void setDomainNameWhiteList(List<String> list, boolean z) throws RemoteException;

    boolean setFactoryResetState(int i) throws RemoteException;

    boolean setFlightModeState(int i) throws RemoteException;

    boolean setGpsLocationState(int i) throws RemoteException;

    void setHomeKeyEventState(int i) throws RemoteException;

    void setInstallPattern(int i) throws RemoteException;

    void setIpAddrBlackList(List<String> list, boolean z) throws RemoteException;

    void setIpAddrPattern(int i) throws RemoteException;

    void setIpAddrWhiteList(List<String> list, boolean z) throws RemoteException;

    void setMenuKeyEventState(int i) throws RemoteException;

    boolean setMicrophoneState(int i) throws RemoteException;

    void setMmsKeywords(String str) throws RemoteException;

    boolean setNFCState(int i) throws RemoteException;

    boolean setNetworkDataSimState(int i) throws RemoteException;

    boolean setNetworkLocationState(int i) throws RemoteException;

    void setNotificationRestrictPattern(int i) throws RemoteException;

    boolean setOTGState(int i) throws RemoteException;

    void setPhoneRestrictPattern(int i) throws RemoteException;

    boolean setRestoreState(int i) throws RemoteException;

    boolean setSDCardState(int i) throws RemoteException;

    void setSafeModeState(int i) throws RemoteException;

    boolean setScreenshotState(int i) throws RemoteException;

    void setSmsRestrictPattern(int i) throws RemoteException;

    boolean setStatusBarState(boolean z) throws RemoteException;

    boolean setSystemTime(long j) throws RemoteException;

    void setTelephonyDataState(int i, int i2) throws RemoteException;

    void setTelephonyMmsState(int i, int i2, int i3) throws RemoteException;

    void setTelephonyPhoneState(int i, int i2, int i3) throws RemoteException;

    boolean setTelephonySlotState(int i) throws RemoteException;

    void setTelephonySmsState(int i, int i2, int i3) throws RemoteException;

    boolean setTimeAutoState(int i) throws RemoteException;

    boolean setTimeState(int i) throws RemoteException;

    boolean setTrustedAppStoreState(int i) throws RemoteException;

    void setUninstallPattern(int i) throws RemoteException;

    boolean setUsbApState(int i) throws RemoteException;

    boolean setUsbDebugState(int i) throws RemoteException;

    boolean setUsbTransferState(int i) throws RemoteException;

    boolean setVPNState(int i) throws RemoteException;

    boolean setVivoEmailPara(ContentValues contentValues) throws RemoteException;

    boolean setWifiApState(int i) throws RemoteException;

    boolean setWifiState(int i) throws RemoteException;

    void setWlanRestrictPattern(int i) throws RemoteException;

    boolean shutDown() throws RemoteException;

    boolean startCallRecord(String str, String str2, int i, int i2, int i3, int i4, String str3) throws RemoteException;

    boolean startCallRecordEx(String str, String str2, int i, int i2, int i3, int i4, String str3, boolean z, boolean z2) throws RemoteException;

    boolean startCallRecordPolicy(String str, int i, int i2, int i3, String str2) throws RemoteException;

    boolean startCallRecordPolicyEx(String str, int i, int i2, int i3, String str2, boolean z, boolean z2) throws RemoteException;

    boolean stopCallRecord() throws RemoteException;

    boolean stopCallRecordPolicy() throws RemoteException;

    void transmitSensitiveMms(String str) throws RemoteException;
}
