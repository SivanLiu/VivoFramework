package com.vivo.customized.support.inter;

import java.util.List;

public interface VivoDeviceInfoControl {
    List<String> getPhoneIccids();

    List<String> getPhoneImeis();

    List<String> getPhoneNumbers();

    long getTrafficBytes(int i, String str);

    boolean isDeviceRoot();
}
