package com.vivo.customized.support;

import com.vivo.customized.support.inter.VivoTelecomControl;
import java.util.List;

class TelecomManager extends BaseManager implements VivoTelecomControl {
    TelecomManager() {
    }

    public void setTelephonyPhoneState(int simId, int callinState, int calloutState) {
        this.custManager.setTelephonyPhoneState(simId, callinState, calloutState);
    }

    public String getTelephonyPhoneState() {
        return this.custManager.getTelephonyPhoneState();
    }

    public void setPhoneRestrictPattern(int pattern) {
        this.custManager.setPhoneRestrictPattern(pattern);
    }

    public int getPhoneRestrictPattern() {
        return this.custManager.getPhoneRestrictPattern();
    }

    public void addPhoneBlackList(List<String> numbers) {
        this.custManager.addPhoneBlackList(numbers);
    }

    public void addPhoneBlackListInfo(String number, int inOutMode, int simID) {
        this.custManager.addPhoneBlackListInfo(number, inOutMode, simID);
    }

    public void deletePhoneBlackList(List<String> numbers) {
        this.custManager.deletePhoneBlackList(numbers);
    }

    public List<String> getPhoneBlackList() {
        return this.custManager.getPhoneBlackList();
    }

    public List<String> getPhoneBlackListInfo() {
        return this.custManager.getPhoneBlackListInfo();
    }

    public void addPhoneWhiteList(List<String> numbers) {
        this.custManager.addPhoneWhiteList(numbers);
    }

    public void addPhoneWhiteListInfo(String number, int inOutMode, int simID) {
        this.custManager.addPhoneWhiteListInfo(number, inOutMode, simID);
    }

    public void deletePhoneWhiteList(List<String> numbers) {
        this.custManager.deletePhoneWhiteList(numbers);
    }

    public List<String> getPhoneWhiteList() {
        return this.custManager.getPhoneWhiteList();
    }

    public List<String> getPhoneWhiteListInfo() {
        return this.custManager.getPhoneWhiteListInfo();
    }

    public void setTelephonySmsState(int simId, int receiveState, int sendState) {
        this.custManager.setTelephonySmsState(simId, receiveState, sendState);
    }

    public String getTelephonySmsState() {
        return this.custManager.getTelephonySmsState();
    }

    public void setTelephonyMmsState(int simId, int receiveState, int sendState) {
        this.custManager.setTelephonyMmsState(simId, receiveState, sendState);
    }

    public String getTelephonyMmsState() {
        return this.custManager.getTelephonyMmsState();
    }

    public void setSmsRestrictPattern(int pattern) {
        this.custManager.setSmsRestrictPattern(pattern);
    }

    public int getSmsRestrictPattern() {
        return this.custManager.getSmsRestrictPattern();
    }

    public void addSmsBlackList(List<String> numbers) {
        this.custManager.addSmsBlackList(numbers);
    }

    public void deleteSmsBlackList(List<String> numbers) {
        this.custManager.deleteSmsBlackList(numbers);
    }

    public List<String> getSmsBlackList() {
        return this.custManager.getSmsBlackList();
    }

    public void addSmsWhiteList(List<String> numbers) {
        this.custManager.addSmsWhiteList(numbers);
    }

    public void deleteSmsWhiteList(List<String> numbers) {
        this.custManager.deleteSmsWhiteList(numbers);
    }

    public List<String> getSmsWhiteList() {
        return this.custManager.getSmsWhiteList();
    }

    public boolean setTelephonySlotState(int state) {
        return this.custManager.setTelephonySlotState(state);
    }

    public int getTelephonySlotState() {
        return this.custManager.getTelephonySlotState();
    }
}
