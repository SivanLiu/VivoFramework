package com.vivo.customized.support.inter;

import java.util.List;

public interface VivoNetworkControl {
    void addAppDataNetworkBlackList(List<String> list);

    void addAppDataNetworkWhiteList(List<String> list);

    void addAppWifiNetworkBlackList(List<String> list);

    void addAppWifiNetworkWhiteList(List<String> list);

    void addDomainNameBlackList(List<String> list);

    void addDomainNameWhiteList(List<String> list);

    void addIpAddrBlackList(List<String> list);

    void addIpAddrWhiteList(List<String> list);

    void addWlanBlackList(List<String> list);

    void addWlanWhiteList(List<String> list);

    void clearAppDataNetworkBlackList();

    void clearAppDataNetworkWhiteList();

    void clearAppWifiNetworkBlackList();

    void clearAppWifiNetworkWhiteList();

    void clearDomainNameBlackList();

    void clearDomainNameWhiteList();

    void clearIpAddrBlackList();

    void clearIpAddrWhiteList();

    void deleteAppDataNetworkBlackList(List<String> list);

    void deleteAppDataNetworkWhiteList(List<String> list);

    void deleteAppWifiNetworkBlackList(List<String> list);

    void deleteAppWifiNetworkWhiteList(List<String> list);

    void deleteDomainNameBlackList(List<String> list);

    void deleteDomainNameWhiteList(List<String> list);

    void deleteIpAddrBlackList(List<String> list);

    void deleteIpAddrWhiteList(List<String> list);

    void deleteWlanBlackList(List<String> list);

    void deleteWlanWhiteList(List<String> list);

    List<String> getAppDataNetworkBlackList();

    int getAppDataNetworkPattern();

    List<String> getAppDataNetworkWhiteList();

    List<String> getAppWifiNetworkBlackList();

    int getAppWifiNetworkPattern();

    List<String> getAppWifiNetworkWhiteList();

    int getDataNetworkState();

    List<String> getDomainNameBlackList();

    int getDomainNamePattern();

    List<String> getDomainNameWhiteList();

    List<String> getIpAddrBlackList();

    int getIpAddrPattern();

    List<String> getIpAddrWhiteList();

    String getTelephonyDataState();

    List<String> getWlanBlackList();

    int getWlanRestrictPattern();

    List<String> getWlanWhiteList();

    void setAppDataNetworkPattern(int i);

    void setAppWifiNetworkPattern(int i);

    boolean setDataNetworkState(int i);

    void setDomainNamePattern(int i);

    void setIpAddrPattern(int i);

    void setTelephonyDataState(int i, int i2);

    void setWlanRestrictPattern(int i);
}
