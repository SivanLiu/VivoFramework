package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoNetworkControl;
import java.util.List;

class NetworkManager extends BaseManager implements VivoNetworkControl {
    NetworkManager() {
    }

    public void setAppDataNetworkPattern(int pattern) {
        this.custManager.setAppDataNetworkPattern(pattern);
    }

    public int getAppDataNetworkPattern() {
        return this.custManager.getAppDataNetworkPattern();
    }

    public void addAppDataNetworkBlackList(List<String> packageNames) {
        this.custManager.addAppDataNetworkBlackList(packageNames);
    }

    public void deleteAppDataNetworkBlackList(List<String> packageNames) {
        this.custManager.deleteAppDataNetworkBlackList(packageNames);
    }

    public void clearAppDataNetworkBlackList() {
        this.custManager.clearPackageState(6);
    }

    public List<String> getAppDataNetworkBlackList() {
        return this.custManager.getAppDataNetworkBlackList();
    }

    public void addAppDataNetworkWhiteList(List<String> packageNames) {
        this.custManager.addAppDataNetworkWhiteList(packageNames);
    }

    public void deleteAppDataNetworkWhiteList(List<String> packageNames) {
        this.custManager.deleteAppDataNetworkWhiteList(packageNames);
    }

    public void clearAppDataNetworkWhiteList() {
        this.custManager.clearPackageState(7);
    }

    public List<String> getAppDataNetworkWhiteList() {
        return this.custManager.getAppDataNetworkWhiteList();
    }

    public void setAppWifiNetworkPattern(int pattern) {
        this.custManager.setAppWifiNetworkPattern(pattern);
    }

    public int getAppWifiNetworkPattern() {
        return this.custManager.getAppWifiNetworkPattern();
    }

    public void addAppWifiNetworkBlackList(List<String> packageNames) {
        this.custManager.addAppWifiNetworkBlackList(packageNames);
    }

    public void deleteAppWifiNetworkBlackList(List<String> packageNames) {
        this.custManager.deleteAppWifiNetworkBlackList(packageNames);
    }

    public void clearAppWifiNetworkBlackList() {
        this.custManager.clearPackageState(8);
    }

    public List<String> getAppWifiNetworkBlackList() {
        return this.custManager.getAppWifiNetworkBlackList();
    }

    public void addAppWifiNetworkWhiteList(List<String> packageNames) {
        this.custManager.addAppWifiNetworkWhiteList(packageNames);
    }

    public void deleteAppWifiNetworkWhiteList(List<String> packageNames) {
        this.custManager.deleteAppWifiNetworkWhiteList(packageNames);
    }

    public void clearAppWifiNetworkWhiteList() {
        this.custManager.clearPackageState(9);
    }

    public List<String> getAppWifiNetworkWhiteList() {
        return this.custManager.getAppWifiNetworkWhiteList();
    }

    public void setDomainNamePattern(int pattern) {
        this.custManager.setDomainNamePattern(pattern);
    }

    public int getDomainNamePattern() {
        return this.custManager.getDomainNamePattern();
    }

    public void addDomainNameBlackList(List<String> urls) {
        this.custManager.setDomainNameBlackList(urls, true);
    }

    public void deleteDomainNameBlackList(List<String> urls) {
        this.custManager.setDomainNameBlackList(urls, false);
    }

    public void clearDomainNameBlackList() {
        this.custManager.clearDomainNameBlackList();
    }

    public List<String> getDomainNameBlackList() {
        return this.custManager.getDomainNameBlackList();
    }

    public void addDomainNameWhiteList(List<String> urls) {
        this.custManager.setDomainNameWhiteList(urls, true);
    }

    public void deleteDomainNameWhiteList(List<String> urls) {
        this.custManager.setDomainNameWhiteList(urls, false);
    }

    public void clearDomainNameWhiteList() {
        this.custManager.clearDomainNameWhiteList();
    }

    public List<String> getDomainNameWhiteList() {
        return this.custManager.getDomainNameWhiteList();
    }

    public void setIpAddrPattern(int pattern) {
        this.custManager.setIpAddrPattern(pattern);
    }

    public int getIpAddrPattern() {
        return this.custManager.getIpAddrPattern();
    }

    public void addIpAddrBlackList(List<String> ips) {
        this.custManager.setIpAddrBlackList(ips, true);
    }

    public void deleteIpAddrBlackList(List<String> ips) {
        this.custManager.setIpAddrBlackList(ips, false);
    }

    public void clearIpAddrBlackList() {
        this.custManager.clearIpAddrBlackList();
    }

    public List<String> getIpAddrBlackList() {
        return this.custManager.getIpAddrBlackList();
    }

    public void addIpAddrWhiteList(List<String> ips) {
        this.custManager.setIpAddrWhiteList(ips, true);
    }

    public void deleteIpAddrWhiteList(List<String> ips) {
        this.custManager.setIpAddrWhiteList(ips, false);
    }

    public void clearIpAddrWhiteList() {
        this.custManager.clearIpAddrWhiteList();
    }

    public List<String> getIpAddrWhiteList() {
        return this.custManager.getIpAddrWhiteList();
    }

    public boolean setDataNetworkState(int state) {
        return this.custManager.setDataNetworkState(state);
    }

    public int getDataNetworkState() {
        return this.custManager.getDataNetworkState();
    }

    public void setTelephonyDataState(int simId, int dataState) {
        this.custManager.setTelephonyDataState(simId, dataState);
    }

    public String getTelephonyDataState() {
        return this.custManager.getTelephonyDataState();
    }

    public void setWlanRestrictPattern(int pattern) {
        this.custManager.setWlanRestrictPattern(pattern);
    }

    public int getWlanRestrictPattern() {
        return this.custManager.getWlanRestrictPattern();
    }

    public void addWlanBlackList(List<String> ssids) {
        this.custManager.addWlanBlackList(ssids);
    }

    public void deleteWlanBlackList(List<String> ssids) {
        this.custManager.deleteWlanBlackList(ssids);
    }

    public List<String> getWlanBlackList() {
        return this.custManager.getWlanBlackList();
    }

    public void addWlanWhiteList(List<String> ssids) {
        this.custManager.addWlanWhiteList(ssids);
    }

    public void deleteWlanWhiteList(List<String> ssids) {
        this.custManager.deleteWlanWhiteList(ssids);
    }

    public List<String> getWlanWhiteList() {
        return this.custManager.getWlanWhiteList();
    }
}
