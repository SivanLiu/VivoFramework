package com.vivo.services.rms.sdk;

import android.os.Bundle;
import java.util.ArrayList;

public interface ISystemEvent {
    void setAppList(String str, ArrayList<String> arrayList);

    void setBundle(Bundle bundle);
}
