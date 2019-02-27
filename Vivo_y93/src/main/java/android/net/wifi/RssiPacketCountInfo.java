package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RssiPacketCountInfo implements Parcelable {
    public static final Creator<RssiPacketCountInfo> CREATOR = new Creator<RssiPacketCountInfo>() {
        public RssiPacketCountInfo createFromParcel(Parcel in) {
            return new RssiPacketCountInfo(in, null);
        }

        public RssiPacketCountInfo[] newArray(int size) {
            return new RssiPacketCountInfo[size];
        }
    };
    public int mLinkspeed;
    public long rFailedCount;
    public long rMultipleRetryCount;
    public long rRetryCount;
    public int rssi;
    public int rxgood;
    public int txbad;
    public int txgood;

    /* synthetic */ RssiPacketCountInfo(Parcel in, RssiPacketCountInfo -this1) {
        this(in);
    }

    public RssiPacketCountInfo() {
        this.rxgood = 0;
        this.txbad = 0;
        this.txgood = 0;
        this.rssi = 0;
        this.mLinkspeed = 0;
        this.rMultipleRetryCount = 0;
        this.rRetryCount = 0;
        this.rFailedCount = 0;
    }

    private RssiPacketCountInfo(Parcel in) {
        this.rssi = in.readInt();
        this.txgood = in.readInt();
        this.txbad = in.readInt();
        this.rxgood = in.readInt();
        this.rFailedCount = in.readLong();
        this.rRetryCount = in.readLong();
        this.rMultipleRetryCount = in.readLong();
        this.mLinkspeed = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.rssi);
        out.writeInt(this.txgood);
        out.writeInt(this.txbad);
        out.writeInt(this.rxgood);
        out.writeLong(this.rFailedCount);
        out.writeLong(this.rRetryCount);
        out.writeLong(this.rMultipleRetryCount);
        out.writeInt(this.mLinkspeed);
    }

    public int describeContents() {
        return 0;
    }
}
