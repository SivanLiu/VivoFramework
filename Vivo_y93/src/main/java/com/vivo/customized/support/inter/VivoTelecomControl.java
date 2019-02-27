package com.vivo.customized.support.inter;

import java.util.List;

public interface VivoTelecomControl {
    void addPhoneBlackList(List<String> list);

    void addPhoneBlackListInfo(String str, int i, int i2);

    void addPhoneWhiteList(List<String> list);

    void addPhoneWhiteListInfo(String str, int i, int i2);

    void addSmsBlackList(List<String> list);

    void addSmsWhiteList(List<String> list);

    void deletePhoneBlackList(List<String> list);

    void deletePhoneWhiteList(List<String> list);

    void deleteSmsBlackList(List<String> list);

    void deleteSmsWhiteList(List<String> list);

    List<String> getPhoneBlackList();

    List<String> getPhoneBlackListInfo();

    int getPhoneRestrictPattern();

    List<String> getPhoneWhiteList();

    List<String> getPhoneWhiteListInfo();

    List<String> getSmsBlackList();

    int getSmsRestrictPattern();

    List<String> getSmsWhiteList();

    String getTelephonyMmsState();

    String getTelephonyPhoneState();

    int getTelephonySlotState();

    String getTelephonySmsState();

    void setPhoneRestrictPattern(int i);

    void setSmsRestrictPattern(int i);

    void setTelephonyMmsState(int i, int i2, int i3);

    void setTelephonyPhoneState(int i, int i2, int i3);

    boolean setTelephonySlotState(int i);

    void setTelephonySmsState(int i, int i2, int i3);
}
