package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoApplicationControl;
import com.vivo.customized.support.utils.CustPackageDeleteObserver;
import com.vivo.customized.support.utils.CustPackageInstallObserver;
import java.util.List;

class ApplicationManager extends BaseManager implements VivoApplicationControl {
    ApplicationManager() {
    }

    public void installPackage(String path, int flags, String installerPackage) {
        this.custManager.installPackage(path, flags, installerPackage);
    }

    public void installPackageWithObserver(String path, int flags, String installerPackage, CustPackageInstallObserver observer) {
        this.custManager.installPackageWithObserver(path, flags, installerPackage, observer);
    }

    public void deletePackage(String packageName, int flags) {
        this.custManager.deletePackage(packageName, flags);
    }

    public void deletePackageWithObserver(String packageName, int flags, CustPackageDeleteObserver observer) {
        this.custManager.deletePackageWithObserver(packageName, flags, observer);
    }

    public void disablePackage(String packageName, int flags) {
        this.custManager.disablePackage(packageName, flags);
    }

    public void setInstallPattern(int pattern) {
        this.custManager.setInstallPattern(pattern);
    }

    public int getInstallPattern() {
        return this.custManager.getInstallPattern();
    }

    public void addInstallBlackList(List<String> packageNames) {
        this.custManager.addInstallBlackList(packageNames);
    }

    public void deleteInstallBlackList(List<String> packageNames) {
        this.custManager.deleteInstallBlackList(packageNames);
    }

    public void clearInstallBlackList() {
        this.custManager.clearPackageState(4);
    }

    public List<String> getInstallBlackList() {
        return this.custManager.getInstallBlackList();
    }

    public void addInstallWhiteList(List<String> packageNames) {
        this.custManager.addInstallWhiteList(packageNames);
    }

    public void deleteInstallWhiteList(List<String> packageNames) {
        this.custManager.deleteInstallWhiteList(packageNames);
    }

    public void clearInstallWhiteList() {
        this.custManager.clearPackageState(5);
    }

    public List<String> getInstallWhiteList() {
        return this.custManager.getInstallWhiteList();
    }

    public void setUninstallPattern(int pattern) {
        this.custManager.setUninstallPattern(pattern);
    }

    public int getUninstallPattern() {
        return this.custManager.getUninstallPattern();
    }

    public void addUninstallBlackList(List<String> packageNames) {
        this.custManager.addUninstallBlackList(packageNames);
    }

    public void deleteUninstallBlackList(List<String> packageNames) {
        this.custManager.deleteUninstallBlackList(packageNames);
    }

    public void clearUninstallBlackList() {
        this.custManager.clearPackageState(2);
    }

    public List<String> getUninstallBlackList() {
        return this.custManager.getUninstallBlackList();
    }

    public void addPersistApps(List<String> packageNames) {
        this.custManager.addPersistApps(packageNames);
    }

    public void removePersistApps(List<String> packageNames) {
        this.custManager.removePersistApps(packageNames);
    }

    public void clearPersistApps() {
        this.custManager.clearPackageState(0);
    }

    public List<String> getPersistApps() {
        return this.custManager.getPersistApps();
    }

    public void addDisabledApps(List<String> packageNames) {
        this.custManager.addDisabledApps(packageNames);
    }

    public void removeDisableApps(List<String> packageNames) {
        this.custManager.removeDisableApps(packageNames);
    }

    public void clearDisableApps() {
        this.custManager.clearPackageState(1);
    }

    public List<String> getDisableApps() {
        return this.custManager.getDisableApps();
    }

    public boolean setTrustedAppStoreState(int state) {
        return this.custManager.setTrustedAppStoreState(state);
    }

    public int getTrustedAppStoreState() {
        return this.custManager.getTrustedAppStoreState();
    }

    public boolean addTrustedAppStore(List<String> pkgs) {
        return this.custManager.addTrustedAppStoreList(pkgs);
    }

    public boolean deleteTrustedAppStore(String packageName) {
        return this.custManager.deleteTrustedAppStore(packageName);
    }

    public List<String> getTrustedAppStore() {
        return this.custManager.getTrustedAppStore();
    }

    public void setNotificationRestrictPattern(int pattern) {
        this.custManager.setNotificationRestrictPattern(pattern);
    }

    public int getNotificationRestrictPattern() {
        return this.custManager.getNotificationRestrictPattern();
    }

    public void addNotificationBlackList(List<String> packageNames) {
        this.custManager.addNotificationBlackList(packageNames);
    }

    public void deleteNotificationBlackList(List<String> packageNames) {
        this.custManager.deleteNotificationBlackList(packageNames);
    }

    public List<String> getNotificationBlackList() {
        return this.custManager.getNotificationBlackList();
    }

    public void addNotificationWhiteList(List<String> packageNames) {
        this.custManager.addNotificationWhiteList(packageNames);
    }

    public void deleteNotificationWhiteList(List<String> packageNames) {
        this.custManager.deleteNotificationWhiteList(packageNames);
    }

    public List<String> getNotificationWhiteList() {
        return this.custManager.getNotificationWhiteList();
    }
}
