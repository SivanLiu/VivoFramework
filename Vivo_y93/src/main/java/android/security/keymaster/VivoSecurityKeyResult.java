package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class VivoSecurityKeyResult implements Parcelable {
    public static final Creator<VivoSecurityKeyResult> CREATOR = new Creator<VivoSecurityKeyResult>() {
        public VivoSecurityKeyResult createFromParcel(Parcel in) {
            return new VivoSecurityKeyResult(in);
        }

        public VivoSecurityKeyResult[] newArray(int length) {
            return new VivoSecurityKeyResult[length];
        }
    };
    public final int keyVersion;
    public final int needUpdateKey;
    public final byte[] operateData;
    public final byte[] publicKeyHash;
    public final int resultCode;
    public final String uniqueId;

    public VivoSecurityKeyResult(int errorCode) {
        this.resultCode = errorCode;
        this.operateData = null;
        this.keyVersion = -1;
        this.uniqueId = null;
        this.publicKeyHash = null;
        this.needUpdateKey = -1;
    }

    protected VivoSecurityKeyResult(Parcel in) {
        this.resultCode = in.readInt();
        this.operateData = in.createByteArray();
        this.keyVersion = in.readInt();
        this.uniqueId = in.readString();
        this.publicKeyHash = in.createByteArray();
        this.needUpdateKey = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (out != null) {
            out.writeInt(this.resultCode);
            out.writeByteArray(this.operateData);
            out.writeInt(this.keyVersion);
            out.writeString(this.uniqueId);
            out.writeByteArray(this.publicKeyHash);
            out.writeInt(this.needUpdateKey);
        }
    }

    public byte[] getOperateData() {
        return this.operateData;
    }

    public int getKeyVersion() {
        return this.keyVersion;
    }

    public String getUniqueID() {
        return this.uniqueId;
    }
}
