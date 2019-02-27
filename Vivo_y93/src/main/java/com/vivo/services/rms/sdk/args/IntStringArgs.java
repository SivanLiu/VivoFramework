package com.vivo.services.rms.sdk.args;

import android.os.Parcel;

public class IntStringArgs extends Args {
    public int mInt0;
    public String mString0;

    public void writeToParcel(Parcel dest) {
        dest.writeInt(this.mInt0);
        dest.writeString(this.mString0);
    }

    public void readFromParcel(Parcel data) {
        this.mInt0 = data.readInt();
        this.mString0 = data.readString();
    }

    public void recycle() {
        this.mString0 = null;
    }
}
