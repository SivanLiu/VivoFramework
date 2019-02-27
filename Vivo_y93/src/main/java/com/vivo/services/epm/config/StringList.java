package com.vivo.services.epm.config;

import android.content.pm.ParceledListSlice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class StringList extends BaseList implements Parcelable {
    public static final Creator<StringList> CREATOR = new Creator<StringList>() {
        public StringList createFromParcel(Parcel in) {
            return new StringList(in);
        }

        public StringList[] newArray(int size) {
            return new StringList[size];
        }
    };
    public static final String LIST_TAG = "list";
    public static final String ROOT_TAG = "lists";
    private List<ParcelableString> values = new ArrayList();

    private static class ParcelableString implements Parcelable {
        public static final Creator<ParcelableString> CREATOR = new Creator<ParcelableString>() {
            public ParcelableString createFromParcel(Parcel in) {
                return new ParcelableString(in);
            }

            public ParcelableString[] newArray(int size) {
                return new ParcelableString[size];
            }
        };
        public String content;

        public ParcelableString(String content) {
            this.content = content;
        }

        protected ParcelableString(Parcel in) {
            this.content = in.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.content);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return this.content;
        }
    }

    public StringList(boolean uninitialized) {
        this.uninitialized = uninitialized;
    }

    public StringList(String name, String path, boolean uninitialized) {
        this.name = name;
        this.mConfigFilePath = path;
        this.uninitialized = uninitialized;
    }

    protected StringList(Parcel in) {
        boolean z = false;
        this.name = in.readString();
        this.mConfigFilePath = in.readString();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.uninitialized = z;
        ParceledListSlice<ParcelableString> pls = (ParceledListSlice) in.readParcelable(null);
        if (pls != null) {
            this.values = pls.getList();
        }
    }

    public StringList makeMiniCopy() {
        return new StringList(this.name, this.mConfigFilePath, this.uninitialized);
    }

    public List<String> getValues() {
        List<String> ret = new ArrayList();
        if (this.values == null) {
            return ret;
        }
        for (ParcelableString ps : this.values) {
            ret.add(ps.content);
        }
        return ret;
    }

    public void addItem(String t) {
        this.uninitialized = false;
        this.values.add(new ParcelableString(t));
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public String toString() {
        return "StringList{name='" + this.name + '\'' + ", size=" + (this.values != null ? this.values.size() : 0) + ", values=" + getValues() + ", uninitialized=" + this.uninitialized + '}';
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
