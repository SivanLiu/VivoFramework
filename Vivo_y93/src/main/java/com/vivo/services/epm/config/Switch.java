package com.vivo.services.epm.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class Switch implements Parcelable {
    public static final Creator<Switch> CREATOR = new Creator<Switch>() {
        public Switch createFromParcel(Parcel in) {
            return new Switch(in);
        }

        public Switch[] newArray(int size) {
            return new Switch[size];
        }
    };
    public static final String ROOT_TAG = "switchs";
    public static final String SWITCH_ATTR_NAME = "name";
    public static final String SWITCH_ATTR_VALUE = "value";
    public static final String SWITCH_ATTR_VALUE_OFF = "off";
    public static final String SWITCH_ATTR_VALUE_ON = "on";
    public static final String SWITCH_ITEM = "switch";
    private String mConfigFilePath;
    private String name;
    private boolean on;
    private boolean uninitialized;

    public Switch(String name, String path, boolean uninitialized) {
        this(name, false, path, uninitialized);
    }

    public Switch(String name, boolean on, String path, boolean uninitialized) {
        this.name = name;
        this.on = on;
        this.mConfigFilePath = path;
        this.uninitialized = uninitialized;
    }

    protected Switch(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.name = in.readString();
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.on = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.uninitialized = z2;
        this.mConfigFilePath = in.readString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOn() {
        return this.on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getConfigFilePath() {
        return this.mConfigFilePath;
    }

    public void setConfigFilePath(String path) {
        this.mConfigFilePath = path;
    }

    public boolean isInvalidSwitch() {
        return false;
    }

    public void setUninitialized(boolean uninitialized) {
        this.uninitialized = uninitialized;
    }

    public boolean isUninitialized() {
        return this.uninitialized;
    }

    public String toString() {
        return "Switch{name='" + this.name + '\'' + ", on=" + this.on + ", uninitialized=" + this.uninitialized + ", path=" + this.mConfigFilePath + '}';
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof Switch)) {
            return false;
        }
        Switch aSwitch = (Switch) o;
        if (isOn() == aSwitch.isOn() && Objects.equals(getName(), aSwitch.getName()) && isUninitialized() == aSwitch.isUninitialized()) {
            z = Objects.equals(getConfigFilePath(), aSwitch.getConfigFilePath());
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2;
        int i3 = 1;
        parcel.writeString(this.name);
        if (this.on) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        parcel.writeByte((byte) i2);
        if (!this.uninitialized) {
            i3 = 0;
        }
        parcel.writeByte((byte) i3);
        parcel.writeString(this.mConfigFilePath);
    }
}
