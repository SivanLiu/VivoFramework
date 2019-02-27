package com.vivo.services.rms.sdk;

import android.os.Bundle;

public interface IProcessEvent {
    void add(int i, String str, int i2, int i3, String str2, String str3);

    void addDepPkg(int i, String str);

    void addPkg(int i, String str);

    void remove(int i, String str);

    void setAdj(int[] iArr, int[] iArr2);

    void setConfig(Bundle bundle);

    void setOom(int i, int i2);

    void setSchedGroup(int[] iArr, int[] iArr2);

    void setStates(int[] iArr, int[] iArr2, int[] iArr3);

    void startActivity(String str, String str2, int i, int i2, int i3);
}
