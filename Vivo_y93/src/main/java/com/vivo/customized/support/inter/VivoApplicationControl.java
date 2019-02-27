package com.vivo.customized.support.inter;

import com.vivo.customized.support.utils.CustPackageDeleteObserver;
import com.vivo.customized.support.utils.CustPackageInstallObserver;
import java.util.List;

public interface VivoApplicationControl {
    void addDisabledApps(List<String> list);

    void addInstallBlackList(List<String> list);

    void addInstallWhiteList(List<String> list);

    void addNotificationBlackList(List<String> list);

    void addNotificationWhiteList(List<String> list);

    void addPersistApps(List<String> list);

    boolean addTrustedAppStore(List<String> list);

    void addUninstallBlackList(List<String> list);

    void clearDisableApps();

    void clearInstallBlackList();

    void clearInstallWhiteList();

    void clearPersistApps();

    void clearUninstallBlackList();

    void deleteInstallBlackList(List<String> list);

    void deleteInstallWhiteList(List<String> list);

    void deleteNotificationBlackList(List<String> list);

    void deleteNotificationWhiteList(List<String> list);

    void deletePackage(String str, int i);

    void deletePackageWithObserver(String str, int i, CustPackageDeleteObserver custPackageDeleteObserver);

    boolean deleteTrustedAppStore(String str);

    void deleteUninstallBlackList(List<String> list);

    void disablePackage(String str, int i);

    List<String> getDisableApps();

    List<String> getInstallBlackList();

    int getInstallPattern();

    List<String> getInstallWhiteList();

    List<String> getNotificationBlackList();

    int getNotificationRestrictPattern();

    List<String> getNotificationWhiteList();

    List<String> getPersistApps();

    List<String> getTrustedAppStore();

    int getTrustedAppStoreState();

    List<String> getUninstallBlackList();

    int getUninstallPattern();

    void installPackage(String str, int i, String str2);

    void installPackageWithObserver(String str, int i, String str2, CustPackageInstallObserver custPackageInstallObserver);

    void removeDisableApps(List<String> list);

    void removePersistApps(List<String> list);

    void setInstallPattern(int i);

    void setNotificationRestrictPattern(int i);

    boolean setTrustedAppStoreState(int i);

    void setUninstallPattern(int i);
}
