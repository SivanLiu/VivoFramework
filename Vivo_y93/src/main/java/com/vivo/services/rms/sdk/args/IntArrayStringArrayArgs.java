package com.vivo.services.rms.sdk.args;

import android.os.Parcel;
import com.vivo.services.rms.sdk.IntArrayFactory;

public class IntArrayStringArrayArgs extends Args {
    public int[] mIntArray0;
    public String[] mStringArray0;

    public void writeToParcel(Parcel dest) {
        writeIntArray(dest, this.mIntArray0);
        dest.writeStringArray(this.mStringArray0);
    }

    public void readFromParcel(Parcel data) {
        this.mIntArray0 = readIntArray(data);
        this.mStringArray0 = data.createStringArray();
    }

    public void recycle() {
        IntArrayFactory.recycle(this.mIntArray0);
        this.mStringArray0 = null;
    }
}
