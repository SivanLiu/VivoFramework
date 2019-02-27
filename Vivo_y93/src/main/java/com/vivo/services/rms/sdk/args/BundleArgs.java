package com.vivo.services.rms.sdk.args;

import android.os.Bundle;
import android.os.Parcel;

public class BundleArgs extends Args {
    public Bundle mBundle;

    public void writeToParcel(Parcel dest) {
        this.mBundle.writeToParcel(dest, 0);
    }

    public void readFromParcel(Parcel data) {
        this.mBundle = new Bundle();
        this.mBundle.readFromParcel(data);
    }

    public void recycle() {
        this.mBundle = null;
    }
}
