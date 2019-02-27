package com.vivo.services.rms.sdk.args;

import android.os.Parcel;
import java.util.ArrayList;

public class LongStringListArgs extends Args {
    public long mLong0;
    public ArrayList<String> mStringList0;

    public void writeToParcel(Parcel dest) {
        dest.writeLong(this.mLong0);
        dest.writeStringList(this.mStringList0);
    }

    public void readFromParcel(Parcel data) {
        this.mLong0 = data.readLong();
        this.mStringList0 = data.createStringArrayList();
    }

    public void recycle() {
        this.mStringList0 = null;
    }
}
