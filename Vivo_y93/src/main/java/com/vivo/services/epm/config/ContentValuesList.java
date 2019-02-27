package com.vivo.services.epm.config;

import android.content.ContentValues;
import android.content.pm.ParceledListSlice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ContentValuesList extends BaseList implements Parcelable {
    public static final Creator<ContentValuesList> CREATOR = new Creator<ContentValuesList>() {
        public ContentValuesList createFromParcel(Parcel in) {
            return new ContentValuesList(in);
        }

        public ContentValuesList[] newArray(int size) {
            return new ContentValuesList[size];
        }
    };
    public static final String LIST_TAG = "clist";
    public static final String ROOT_TAG = "customlists";
    private List<ContentValues> values = new ArrayList();

    public ContentValuesList(boolean uninitialized) {
        this.uninitialized = uninitialized;
    }

    public ContentValuesList(String name, String path, boolean uninitialized) {
        this.name = name;
        this.mConfigFilePath = path;
        this.uninitialized = uninitialized;
    }

    protected ContentValuesList(Parcel in) {
        boolean z = false;
        this.name = in.readString();
        this.mConfigFilePath = in.readString();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.uninitialized = z;
        ParceledListSlice<ContentValues> pls = (ParceledListSlice) in.readParcelable(null);
        if (pls != null) {
            this.values = pls.getList();
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ContentValues> getValues() {
        return this.values;
    }

    public void addItem(ContentValues t) {
        this.values.add(t);
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public ContentValuesList makeMiniCopy() {
        return new ContentValuesList(this.name, this.mConfigFilePath, this.uninitialized);
    }

    public String toString() {
        return "ContentValuesList{name='" + this.name + '\'' + ", size=" + (this.values != null ? this.values.size() : 0) + ", values=" + this.values + ", uninitialized=" + this.uninitialized + '}';
    }

    public boolean isInvalidList() {
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2;
        parcel.writeString(this.name);
        parcel.writeString(this.mConfigFilePath);
        if (this.uninitialized) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        parcel.writeByte((byte) i2);
        parcel.writeParcelable(new ParceledListSlice(this.values), 0);
    }
}
