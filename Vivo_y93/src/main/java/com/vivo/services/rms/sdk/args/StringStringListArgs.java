package com.vivo.services.rms.sdk.args;

import android.os.Parcel;
import java.util.ArrayList;

public class StringStringListArgs extends Args {
    public String mString0;
    public ArrayList<String> mStringList0;

    public void writeToParcel(Parcel dest) {
        dest.writeString(this.mString0);
        dest.writeStringList(this.mStringList0);
    }

    public void readFromParcel(Parcel data) {
        this.mString0 = data.readString();
        this.mStringList0 = data.createStringArrayList();
    }

    public void recycle() {
        this.mString0 = null;
        this.mStringList0 = null;
    }
}
