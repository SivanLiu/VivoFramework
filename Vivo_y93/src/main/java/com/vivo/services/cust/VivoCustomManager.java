package com.vivo.services.cust;

import android.app.PackageDeleteObserver;
import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.vivo.services.cust.spec.IVivoCustomSpecService;
import com.vivo.services.cust.spec.IVivoCustomSpecService.Stub;
import java.util.List;

public class VivoCustomManager {
    private static final String TAG = "VCS";
    public static final String VIVO_CT_SERVICE = "vivo_customized";
    public static final String VIVO_CT_SPEC_SERVICE = "vivo_spec_customized";
    private IVivoCustomSpecService mSpecVcts = getDefaultSpecVCTS();
    private final Singleton<IVivoCustomSpecService> sDefaultSpecVCTS = new Singleton<IVivoCustomSpecService>() {
        protected IVivoCustomSpecService create() {
            return Stub.asInterface(ServiceManager.getService(VivoCustomManager.VIVO_CT_SPEC_SERVICE));
        }
    };
    private final Singleton<IVivoCustomService> sDefaultVCTS = new Singleton<IVivoCustomService>() {
        protected IVivoCustomService create() {
            return IVivoCustomService.Stub.asInterface(ServiceManager.getService(VivoCustomManager.VIVO_CT_SERVICE));
        }
    };
    private IVivoCustomService vcts = getDefaultVCTS();

    public interface NetworkListChangeListener {
        void updateBlackList(int i);

        void updateWhiteList(int i);
    }

    private static class NetworkListChangeListenerDelegate extends INetworkListDelegate.Stub {
        final NetworkListChangeListener Callback;

        public NetworkListChangeListenerDelegate(NetworkListChangeListener callback) {
            this.Callback = callback;
        }

        public void updateBlackList(int type) {
            this.Callback.updateBlackList(type);
        }

        public void updateWhiteList(int type) {
            this.Callback.updateWhiteList(type);
        }
    }

    public IVivoCustomService getDefaultVCTS() {
        return (IVivoCustomService) this.sDefaultVCTS.get();
    }

    public IVivoCustomSpecService getDefaultSpecVCTS() {
        return (IVivoCustomSpecService) this.sDefaultSpecVCTS.get();
    }

    public boolean isDeviceRoot() {
        try {
            return this.vcts.isDeviceRoot();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean shutDown() {
        try {
            return this.vcts.shutDown();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean reBoot() {
        try {
            return this.vcts.reBoot();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean formatSDCard() {
        try {
            return this.vcts.formatSDCard();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setWifiState(int state) {
        try {
            return this.vcts.setWifiState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getWifiState() {
        try {
            return this.vcts.getWifiState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setWifiApState(int state) {
        try {
            return this.vcts.setWifiApState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getWifiApState() {
        try {
            return this.vcts.getWifiApState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setNFCState(int state) {
        try {
            return this.vcts.setNFCState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getNFCState() {
        try {
            return this.vcts.getNFCState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setBluetoothState(int state) {
        try {
            return this.vcts.setBluetoothState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getBluetoothState() {
        try {
            return this.vcts.getBluetoothState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setGpsLocationState(int state) {
        try {
            return this.vcts.setGpsLocationState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getGpsLocationState() {
        try {
            return this.vcts.getGpsLocationState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setNetworkLocationState(int state) {
        try {
            return this.vcts.setNetworkLocationState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getNetworkLocationState() {
        try {
            return this.vcts.getNetworkLocationState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setBluetoothApState(int state) {
        try {
            return this.vcts.setBluetoothApState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getBluetoothApState() {
        try {
            return this.vcts.getBluetoothApState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setUsbTransferState(int state) {
        try {
            return this.vcts.setUsbTransferState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getUsbTransferState() {
        try {
            return this.vcts.getUsbTransferState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setUsbApState(int state) {
        try {
            return this.vcts.setUsbApState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getUsbApState() {
        try {
            return this.vcts.getUsbApState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setCameraState(int state) {
        try {
            return this.vcts.setCameraState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getCameraState() {
        try {
            return this.vcts.getCameraState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setMicrophoneState(int state) {
        try {
            return this.vcts.setMicrophoneState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getMicrophoneState() {
        try {
            return this.vcts.getMicrophoneState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setScreenshotState(int state) {
        try {
            return this.vcts.setScreenshotState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getScreenshotState() {
        try {
            return this.vcts.getScreenshotState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setSDCardState(int state) {
        try {
            return this.vcts.setSDCardState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getSDCardState() {
        try {
            return this.vcts.getSDCardState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setOTGState(int state) {
        try {
            return this.vcts.setOTGState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getOTGState() {
        try {
            return this.vcts.getOTGState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setUsbDebugState(int state) {
        try {
            return this.vcts.setUsbDebugState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getUsbDebugState() {
        try {
            return this.vcts.getUsbDebugState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setAPNState(int state) {
        try {
            return this.vcts.setAPNState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getAPNState() {
        try {
            return this.vcts.getAPNState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setVPNState(int state) {
        try {
            return this.vcts.setVPNState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getVPNState() {
        try {
            return this.vcts.getVPNState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setTimeState(int state) {
        try {
            return this.vcts.setTimeState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getTimeState() {
        try {
            return this.vcts.getTimeState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setSystemTime(long when) {
        try {
            return this.vcts.setSystemTime(when);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setTimeAutoState(int state) {
        try {
            return this.vcts.setTimeAutoState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getTimeAutoState() {
        try {
            return this.vcts.getTimeAutoState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setNetworkDataSimState(int state) {
        try {
            return this.vcts.setNetworkDataSimState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getNetworkDataSimState() {
        try {
            return this.vcts.getNetworkDataSimState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setVivoEmailPara(ContentValues values) {
        try {
            return this.vcts.setVivoEmailPara(values);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setRestoreState(int state) {
        try {
            return this.vcts.setRestoreState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getRestoreState() {
        try {
            return this.vcts.getRestoreState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setFactoryResetState(int state) {
        try {
            return this.vcts.setFactoryResetState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getFactoryResetState() {
        try {
            return this.vcts.getFactoryResetState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setTelephonySlotState(int state) {
        try {
            return this.vcts.setTelephonySlotState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getTelephonySlotState() {
        try {
            return this.vcts.getTelephonySlotState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDataNetworkState(int state) {
        try {
            return this.vcts.setDataNetworkState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getDataNetworkState() {
        try {
            return this.vcts.getDataNetworkState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setFlightModeState(int state) {
        try {
            return this.vcts.setFlightModeState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getFlightModeState() {
        try {
            return this.vcts.getFlightModeState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void installPackage(String path, int flags, String installerPackage) {
        try {
            this.vcts.installPackage(path, flags, installerPackage);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void installPackageWithObserver(String path, int flags, String installerPackage, PackageInstallObserver observer) {
        try {
            this.vcts.installPackageWithObserver(path, flags, installerPackage, observer.getBinder());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deletePackage(String packageName, int flags) {
        try {
            this.vcts.deletePackage(packageName, flags);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deletePackageWithObserver(String packageName, int flags, PackageDeleteObserver observer) {
        try {
            this.vcts.deletePackageWithObserver(packageName, flags, observer.getBinder());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void disablePackage(String packageName, int flags) {
        try {
            this.vcts.disablePackage(packageName, flags);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setInstallPattern(int pattern) {
        try {
            this.vcts.setInstallPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getInstallPattern() {
        try {
            return this.vcts.getInstallPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addInstallBlackList(List<String> packageNames) {
        try {
            this.vcts.addInstallBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteInstallBlackList(List<String> packageNames) {
        try {
            this.vcts.deleteInstallBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getInstallBlackList() {
        try {
            return this.vcts.getInstallBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addInstallWhiteList(List<String> packageNames) {
        try {
            this.vcts.addInstallWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteInstallWhiteList(List<String> packageNames) {
        try {
            this.vcts.deleteInstallWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getInstallWhiteList() {
        try {
            return this.vcts.getInstallWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setUninstallPattern(int pattern) {
        try {
            this.vcts.setUninstallPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getUninstallPattern() {
        try {
            return this.vcts.getUninstallPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addUninstallBlackList(List<String> packageNames) {
        try {
            this.vcts.addUninstallBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteUninstallBlackList(List<String> packageNames) {
        try {
            this.vcts.deleteUninstallBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getUninstallBlackList() {
        try {
            return this.vcts.getUninstallBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addUninstallWhiteList(List<String> packageNames) {
        try {
            this.vcts.addUninstallWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteUninstallWhiteList(List<String> packageNames) {
        try {
            this.vcts.deleteUninstallWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getUninstallWhiteList() {
        try {
            return this.vcts.getUninstallWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addPersistApps(List<String> packageNames) {
        try {
            this.vcts.addPersistApps(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void removePersistApps(List<String> packageNames) {
        try {
            this.vcts.removePersistApps(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPersistApps() {
        try {
            return this.vcts.getPersistApps();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addDisabledApps(List<String> packageNames) {
        try {
            this.vcts.addDisabledApps(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void removeDisableApps(List<String> packageNames) {
        try {
            this.vcts.removeDisableApps(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getDisableApps() {
        try {
            return this.vcts.getDisableApps();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setAppDataNetworkPattern(int pattern) {
        try {
            this.vcts.setAppDataNetworkPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getAppDataNetworkPattern() {
        try {
            return this.vcts.getAppDataNetworkPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addAppDataNetworkBlackList(List<String> packageNames) {
        try {
            this.vcts.addAppDataNetworkBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteAppDataNetworkBlackList(List<String> packageNames) {
        try {
            this.vcts.deleteAppDataNetworkBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getAppDataNetworkBlackList() {
        try {
            return this.vcts.getAppDataNetworkBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addAppDataNetworkWhiteList(List<String> packageNames) {
        try {
            this.vcts.addAppDataNetworkWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteAppDataNetworkWhiteList(List<String> packageNames) {
        try {
            this.vcts.deleteAppDataNetworkWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getAppDataNetworkWhiteList() {
        try {
            return this.vcts.getAppDataNetworkWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isAppDataNetworkWhiteListNotNull() {
        try {
            return this.vcts.isAppDataNetworkWhiteListNotNull();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setAppWifiNetworkPattern(int pattern) {
        try {
            this.vcts.setAppWifiNetworkPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getAppWifiNetworkPattern() {
        try {
            return this.vcts.getAppWifiNetworkPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addAppWifiNetworkBlackList(List<String> packageNames) {
        try {
            this.vcts.addAppWifiNetworkBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteAppWifiNetworkBlackList(List<String> packageNames) {
        try {
            this.vcts.deleteAppWifiNetworkBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getAppWifiNetworkBlackList() {
        try {
            return this.vcts.getAppWifiNetworkBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addAppWifiNetworkWhiteList(List<String> packageNames) {
        try {
            this.vcts.addAppWifiNetworkWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteAppWifiNetworkWhiteList(List<String> packageNames) {
        try {
            this.vcts.deleteAppWifiNetworkWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getAppWifiNetworkWhiteList() {
        try {
            return this.vcts.getAppWifiNetworkWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isAppWifiNetworkWhiteListNotNull() {
        try {
            return this.vcts.isAppWifiNetworkWhiteListNotNull();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearPackageState(int state) {
        try {
            this.vcts.clearPackageState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDomainNamePattern(int pattern) {
        try {
            this.vcts.setDomainNamePattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getDomainNamePattern() {
        try {
            return this.vcts.getDomainNamePattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDomainNameBlackList(List<String> urls, boolean isBlackList) {
        try {
            this.vcts.setDomainNameBlackList(urls, isBlackList);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearDomainNameBlackList() {
        try {
            this.vcts.clearDomainNameBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getDomainNameBlackList() {
        try {
            return this.vcts.getDomainNameBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDomainNameWhiteList(List<String> urls, boolean isWhiteList) {
        try {
            this.vcts.setDomainNameWhiteList(urls, isWhiteList);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearDomainNameWhiteList() {
        try {
            this.vcts.clearDomainNameWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getDomainNameWhiteList() {
        try {
            return this.vcts.getDomainNameWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setIpAddrPattern(int pattern) {
        try {
            this.vcts.setIpAddrPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getIpAddrPattern() {
        try {
            return this.vcts.getIpAddrPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setIpAddrBlackList(List<String> ips, boolean isBlackList) {
        try {
            this.vcts.setIpAddrBlackList(ips, isBlackList);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearIpAddrBlackList() {
        try {
            this.vcts.clearIpAddrBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getIpAddrBlackList() {
        try {
            return this.vcts.getIpAddrBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setIpAddrWhiteList(List<String> ips, boolean isWhiteList) {
        try {
            this.vcts.setIpAddrWhiteList(ips, isWhiteList);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearIpAddrWhiteList() {
        try {
            this.vcts.clearIpAddrWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getIpAddrWhiteList() {
        try {
            return this.vcts.getIpAddrWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setPhoneRestrictPattern(int pattern) {
        try {
            this.vcts.setPhoneRestrictPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getPhoneRestrictPattern() {
        try {
            return this.vcts.getPhoneRestrictPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addPhoneBlackList(List<String> numbers) {
        try {
            this.vcts.addPhoneBlackList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addPhoneBlackListInfo(String number, int inOutMode, int simID) {
        try {
            this.vcts.addPhoneBlackListInfo(number, inOutMode, simID);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deletePhoneBlackList(List<String> numbers) {
        try {
            this.vcts.deletePhoneBlackList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneBlackList() {
        try {
            return this.vcts.getPhoneBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneBlackListInfo() {
        try {
            return this.vcts.getPhoneBlackListInfo();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addPhoneWhiteList(List<String> numbers) {
        try {
            this.vcts.addPhoneWhiteList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addPhoneWhiteListInfo(String number, int inOutMode, int simID) {
        try {
            this.vcts.addPhoneWhiteListInfo(number, inOutMode, simID);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deletePhoneWhiteList(List<String> numbers) {
        try {
            this.vcts.deletePhoneWhiteList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneWhiteList() {
        try {
            return this.vcts.getPhoneWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneWhiteListInfo() {
        try {
            return this.vcts.getPhoneWhiteListInfo();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setTelephonyMmsState(int simId, int receiveState, int sendState) {
        try {
            this.vcts.setTelephonyMmsState(simId, receiveState, sendState);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getTelephonyMmsState() {
        try {
            return this.vcts.getTelephonyMmsState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setTelephonySmsState(int simId, int receiveState, int sendState) {
        try {
            this.vcts.setTelephonySmsState(simId, receiveState, sendState);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getTelephonySmsState() {
        try {
            return this.vcts.getTelephonySmsState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setSmsRestrictPattern(int pattern) {
        try {
            this.vcts.setSmsRestrictPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getSmsRestrictPattern() {
        try {
            return this.vcts.getSmsRestrictPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addSmsBlackList(List<String> numbers) {
        try {
            this.vcts.addSmsBlackList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteSmsBlackList(List<String> numbers) {
        try {
            this.vcts.deleteSmsBlackList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getSmsBlackList() {
        try {
            return this.vcts.getSmsBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addSmsWhiteList(List<String> numbers) {
        try {
            this.vcts.addSmsWhiteList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteSmsWhiteList(List<String> numbers) {
        try {
            this.vcts.deleteSmsWhiteList(numbers);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getSmsWhiteList() {
        try {
            return this.vcts.getSmsWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setTelephonyDataState(int simId, int dataState) {
        try {
            this.vcts.setTelephonyDataState(simId, dataState);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getTelephonyDataState() {
        try {
            return this.vcts.getTelephonyDataState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setWlanRestrictPattern(int pattern) {
        try {
            this.vcts.setWlanRestrictPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getWlanRestrictPattern() {
        try {
            return this.vcts.getWlanRestrictPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addWlanBlackList(List<String> iccids) {
        try {
            this.vcts.addWlanBlackList(iccids);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteWlanBlackList(List<String> iccids) {
        try {
            this.vcts.deleteWlanBlackList(iccids);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getWlanBlackList() {
        try {
            return this.vcts.getWlanBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addWlanWhiteList(List<String> iccids) {
        try {
            this.vcts.addWlanWhiteList(iccids);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteWlanWhiteList(List<String> iccids) {
        try {
            this.vcts.deleteWlanWhiteList(iccids);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getWlanWhiteList() {
        try {
            return this.vcts.getWlanWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void registerNetworkListChangeCallback(NetworkListChangeListener listener) {
        Log.d(TAG, "register NetworkList callback");
        try {
            this.vcts.registerNetworkListChangeCallback(new NetworkListChangeListenerDelegate(listener));
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void registerPhoneListChangeCallback(IPhoneListListener listener) {
        Log.d(TAG, "register PhoneListChange callback");
        try {
            this.vcts.registerPhoneListChangeCallback(listener);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void registerWlanListChangeCallback(IWlanListListener listener) {
        Log.d(TAG, "register WlanListChange callback");
        try {
            this.vcts.registerWlanListChangeCallback(listener);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setBluetoothRestrictPattern(int pattern) {
        try {
            this.vcts.setBluetoothRestrictPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getBluetoothRestrictPattern() {
        try {
            return this.vcts.getBluetoothRestrictPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addBluetoothBlackList(List<String> macs) {
        try {
            this.vcts.addBluetoothBlackList(macs);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteBluetoothBlackList(List<String> macs) {
        try {
            this.vcts.deleteBluetoothBlackList(macs);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getBluetoothBlackList() {
        try {
            return this.vcts.getBluetoothBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addBluetoothWhiteList(List<String> macs) {
        try {
            this.vcts.addBluetoothWhiteList(macs);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteBluetoothWhiteList(List<String> macs) {
        try {
            this.vcts.deleteBluetoothWhiteList(macs);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getBluetoothWhiteList() {
        try {
            return this.vcts.getBluetoothWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void registerSensitiveMmsListenerCallback(ISensitiveMmsDelegate listener) {
        Log.d(TAG, "register Sensitive Message callback");
        try {
            this.vcts.registerSensitiveMmsListenerCallback(listener);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setMmsKeywords(String str) {
        try {
            this.vcts.setMmsKeywords(str);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void transmitSensitiveMms(String str) {
        Log.d(TAG, "transmit Sensitive Message callback");
        try {
            this.vcts.transmitSensitiveMms(str);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setTelephonyPhoneState(int simId, int callinState, int calloutState) {
        try {
            this.vcts.setTelephonyPhoneState(simId, callinState, calloutState);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getTelephonyPhoneState() {
        try {
            return this.vcts.getTelephonyPhoneState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDevicePolicyManager(ComponentName componentName, boolean isActive) {
        try {
            return this.vcts.setDevicePolicyManager(componentName, isActive);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isDevicePolicyManagerEnable(ComponentName componentName) {
        try {
            return this.vcts.isDevicePolicyManagerEnable(componentName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setAccessibilityServcie(ComponentName componentName, boolean isActive) {
        try {
            return this.vcts.setAccessibilityServcie(componentName, isActive);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isAccessibilityServcieEnable(ComponentName componentName) {
        try {
            return this.vcts.isAccessibilityServcieEnable(componentName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean killProcess(String procName) {
        try {
            return this.vcts.killProcess(procName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean clearAppData(String packageName) {
        try {
            return this.vcts.clearAppData(packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean forceStopPackage(String packageName) {
        try {
            return this.vcts.forceStopPackage(packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void endCall() {
        try {
            this.vcts.endCall();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public Bundle captureScreen() {
        try {
            return this.vcts.captureScreen();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public Bitmap captureScreen(ComponentName admin) {
        try {
            return (Bitmap) this.vcts.captureScreen().getParcelable("CAPTURE_SCREEN");
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDefaultLauncher(ComponentName componentName, int state) {
        try {
            return this.vcts.setDefaultLauncher(componentName, state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDefaultBrowser(ComponentName componentName, int state) {
        try {
            return this.vcts.setDefaultBrowser(componentName, state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDefaultEmail(ComponentName componentName, int state) {
        try {
            return this.vcts.setDefaultEmail(componentName, state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDefaultMessage(ComponentName componentName, int state) {
        try {
            return this.vcts.setDefaultMessage(componentName, state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setHomeKeyEventState(int state) {
        try {
            this.vcts.setHomeKeyEventState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getHomeKeyEventState() {
        try {
            return this.vcts.getHomeKeyEventState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setMenuKeyEventState(int state) {
        try {
            this.vcts.setMenuKeyEventState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getMenuKeyEventState() {
        try {
            return this.vcts.getMenuKeyEventState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setBackKeyEventState(int state) {
        try {
            this.vcts.setBackKeyEventState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getBackKeyEventState() {
        try {
            return this.vcts.getBackKeyEventState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setStatusBarState(boolean enable) {
        try {
            return this.vcts.setStatusBarState(enable);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean getStatusBarState() {
        try {
            return this.vcts.getStatusBarState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setSafeModeState(int state) {
        try {
            this.vcts.setSafeModeState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getSafeModeState() {
        try {
            return this.vcts.getSafeModeState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public long getTrafficBytes(int mode, String packageName) {
        try {
            return this.vcts.getTrafficBytes(mode, packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneNumbers() {
        try {
            return this.vcts.getPhoneNumbers();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneIccids() {
        try {
            return this.vcts.getPhoneIccids();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getPhoneImeis() {
        try {
            return this.vcts.getPhoneImeis();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean addTrustedAppStore(String pkgs) {
        try {
            return this.vcts.addTrustedAppStore(pkgs);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean addTrustedAppStoreList(List<String> packageNames) {
        try {
            return this.vcts.addTrustedAppStoreList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean deleteTrustedAppStore(String packageName) {
        try {
            return this.vcts.deleteTrustedAppStore(packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getTrustedAppStore() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getTrustedAppStore();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getTrustedAppStore failed", e2);
            return null;
        }
    }

    public boolean setTrustedAppStoreState(int state) {
        try {
            return this.vcts.setTrustedAppStoreState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getTrustedAppStoreState() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getTrustedAppStoreState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getTrustedAppStoreState failed", e2);
            return 0;
        }
    }

    public boolean ClearDnsCache() {
        try {
            return this.vcts.ClearDnsCache();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean startCallRecordPolicy(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension) {
        try {
            return this.vcts.startCallRecordPolicy(filePath, fileNameMode, samplingRate, fileFormat, fileExtension);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean startCallRecordPolicyEx(String filePath, int fileNameMode, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) {
        try {
            return this.vcts.startCallRecordPolicyEx(filePath, fileNameMode, samplingRate, fileFormat, fileExtension, disableToast, hide);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean stopCallRecordPolicy() {
        try {
            return this.vcts.stopCallRecordPolicy();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean startCallRecord(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension) {
        try {
            return this.vcts.startCallRecord(filePath, fileName, fileNameMode, maxTime, samplingRate, fileFormat, fileExtension);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean startCallRecordEx(String filePath, String fileName, int fileNameMode, int maxTime, int samplingRate, int fileFormat, String fileExtension, boolean disableToast, boolean hide) {
        try {
            return this.vcts.startCallRecordEx(filePath, fileName, fileNameMode, maxTime, samplingRate, fileFormat, fileExtension, disableToast, hide);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean stopCallRecord() {
        try {
            return this.vcts.stopCallRecord();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getCustomizedApps(int type) {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getCustomizedApps(type);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getCustomizedApps failed", e2);
            return null;
        }
    }

    public String getDefaultDeviceOwner() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getDefaultDeviceOwner();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getDefaultDeviceOwner failed", e2);
            return null;
        }
    }

    public List<String> getByPassPermissions() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getByPassPermissions();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getByPassPermissions failed", e2);
            return null;
        }
    }

    public List<String> getByPassOps() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getByPassOps();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getByPassOps failed", e2);
            return null;
        }
    }

    public List<String> getDefaultSettingString() {
        try {
            if (this.vcts == null) {
                this.vcts = getDefaultVCTS();
                this.mSpecVcts = getDefaultSpecVCTS();
            }
            return this.vcts.getDefaultSettingString();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.w(TAG, "getDefaultSettingString failed", e2);
            return null;
        }
    }

    public void setNotificationRestrictPattern(int pattern) {
        try {
            this.vcts.setNotificationRestrictPattern(pattern);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getNotificationRestrictPattern() {
        try {
            return this.vcts.getNotificationRestrictPattern();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addNotificationBlackList(List<String> packageNames) {
        try {
            this.vcts.addNotificationBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteNotificationBlackList(List<String> packageNames) {
        try {
            this.vcts.deleteNotificationBlackList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getNotificationBlackList() {
        try {
            return this.vcts.getNotificationBlackList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void addNotificationWhiteList(List<String> packageNames) {
        try {
            this.vcts.addNotificationWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void deleteNotificationWhiteList(List<String> packageNames) {
        try {
            this.vcts.deleteNotificationWhiteList(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public List<String> getNotificationWhiteList() {
        try {
            return this.vcts.getNotificationWhiteList();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setClipBoardState(int state) {
        try {
            return this.vcts.setClipBoardState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getClipBoardState() {
        try {
            return this.vcts.getClipBoardState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean setDeviceOwner(ComponentName who) {
        try {
            return this.mSpecVcts.setDeviceOwner(who);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getAPIVersion() {
        try {
            return this.mSpecVcts.getAPIVersion();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getRomVersion() {
        try {
            return this.mSpecVcts.getRomVersion();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDevicePolicyManagerUIState(int state) {
        try {
            this.mSpecVcts.setDevicePolicyManagerUIState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getDevicePolicyManagerUIState() {
        try {
            return this.mSpecVcts.getDevicePolicyManagerUIState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setAccessibilityServcieUIState(int state) {
        try {
            this.mSpecVcts.setAccessibilityServcieUIState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getAccessibilityServcieUIState() {
        try {
            return this.mSpecVcts.getAccessibilityServcieUIState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void clearDeviceOwner(String packageName) {
        try {
            this.mSpecVcts.clearDeviceOwner(packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isTrustedAppStoreEnabled() {
        try {
            return this.mSpecVcts.isTrustedAppStoreEnabled();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setFlightModeStateNormal(int state) {
        try {
            this.mSpecVcts.setFlightModeStateNormal(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getFlightModeStateNormal() {
        try {
            return this.mSpecVcts.getFlightModeStateNormal();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setFaceWakeState(int state) {
        try {
            this.mSpecVcts.setFaceWakeState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getFaceWakeState() {
        try {
            return this.mSpecVcts.getFaceWakeState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setSmartlockState(int state) {
        try {
            this.mSpecVcts.setSmartlockState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getSmartlockState() {
        try {
            return this.mSpecVcts.getSmartlockState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setFingerprintState(int state) {
        try {
            this.mSpecVcts.setFingerprintState(state);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int getFingerprintState() {
        try {
            return this.mSpecVcts.getFingerprintState();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDataEnabled(boolean value) {
        try {
            this.mSpecVcts.setDataEnabled(value);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean getDataEnabled() {
        try {
            return this.mSpecVcts.getDataEnabled();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setMobileSettings(ComponentName admin, String busi, Bundle settings) {
        try {
            this.mSpecVcts.setMobileSettings(admin, busi, settings);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public Bundle getMobileSettings(ComponentName admin, String busi, String setting) {
        try {
            return this.mSpecVcts.getMobileSettings(admin, busi, setting);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void disablePackage(ComponentName admin, String packageName) {
        try {
            this.mSpecVcts.disablePackage(admin, packageName);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        try {
            this.mSpecVcts.setLanguageChangeDisabled(disabled);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        try {
            this.mSpecVcts.setDeveloperOptionsDisabled(disabled);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isDeveloperOptionsDisabled() {
        try {
            return this.mSpecVcts.isDeveloperOptionsDisabled();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }
}
