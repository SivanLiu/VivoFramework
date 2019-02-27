package com.vivo.services.rms.sdk.args;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.vivo.services.rms.sdk.IntArrayFactory;

public abstract class Args implements Parcelable {
    public static final Creator<Args> CREATOR = new Creator<Args>() {
        public Args createFromParcel(Parcel source) {
            Args data = ArgsFactory.create(source.readString());
            if (data != null) {
                data.readFromParcel(source);
            }
            return data;
        }

        public Args[] newArray(int size) {
            return new Args[size];
        }
    };
    private final String mClassName = getClass().getSimpleName();

    public abstract void readFromParcel(Parcel parcel);

    public abstract void recycle();

    public abstract void writeToParcel(Parcel parcel);

    public int describeContents() {
        return 0;
    }

    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mClassName);
        writeToParcel(dest);
    }

    public String getClassName() {
        return this.mClassName;
    }

    public void writeIntArray(Parcel dest, int[] val) {
        if (val != null) {
            dest.writeInt(N);
            for (int writeInt : val) {
                dest.writeInt(writeInt);
            }
            return;
        }
        dest.writeInt(-1);
    }

    public int[] readIntArray(Parcel data) {
        int N = data.readInt();
        int[] val = null;
        if (N >= 0) {
            val = IntArrayFactory.create(N);
            for (int i = 0; i < N; i++) {
                val[i] = data.readInt();
            }
        }
        return val;
    }
}
