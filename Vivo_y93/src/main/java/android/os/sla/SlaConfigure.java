package android.os.sla;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SlaConfigure implements Parcelable {
    public static final Creator<SlaConfigure> CREATOR = new Creator<SlaConfigure>() {
        public SlaConfigure createFromParcel(Parcel p) {
            return new SlaConfigure(p, null);
        }

        public SlaConfigure[] newArray(int size) {
            return new SlaConfigure[size];
        }
    };
    public boolean slaEnabled;
    public int[] slaUids;

    /* synthetic */ SlaConfigure(Parcel p, SlaConfigure -this1) {
        this(p);
    }

    public SlaConfigure() {
        this.slaUids = new int[0];
    }

    private SlaConfigure(Parcel p) {
        this.slaUids = new int[0];
        this.slaEnabled = p.readBoolean();
        int numSlaUids = p.readInt();
        this.slaUids = new int[numSlaUids];
        for (int i = 0; i < numSlaUids; i++) {
            this.slaUids[i] = p.readInt();
        }
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeBoolean(this.slaEnabled);
        p.writeInt(this.slaUids.length);
        for (int slaUid : this.slaUids) {
            p.writeInt(slaUid);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("slaEnabled: ").append(this.slaEnabled).append(' ').append("slaUids:");
        for (int append : this.slaUids) {
            sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(append);
        }
        return sbuf.toString();
    }
}
