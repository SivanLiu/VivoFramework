package com.vivo.services.rms.sdk.args;

import android.os.Parcel;

public class Int3Args extends Args {
    public int mInt0;
    public int mInt1;
    public int mInt2;

    public void writeToParcel(Parcel dest) {
        dest.writeInt(this.mInt0);
        dest.writeInt(this.mInt1);
        dest.writeInt(this.mInt2);
    }

    public void readFromParcel(Parcel data) {
        this.mInt0 = data.readInt();
        this.mInt1 = data.readInt();
        this.mInt2 = data.readInt();
    }

    public void recycle() {
    }
}
