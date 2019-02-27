package com.vivo.services.cipher.utils;

import android.content.Context;
import android.security.KeyStore;
import android.text.TextUtils;
import com.android.internal.telephony.SmsConstants;

public class SecurityKeyConfigure {
    private static final int MINI_PACKAGE_NAME_MAX = 16;
    private String mAppSignHash;
    private int mCipherMode;
    private Context mContext;
    private int mKeyVersion;
    private KeyStore mKeystore;
    private long mLastExecTime;
    private long mLastTryUpdateKeyTime;
    private String mPackageName;
    private int mRealCipherMode = 2;
    private String mShortPackageName;
    private String mToken;
    private String mUniqueId;
    private int mUpdateKeyFailCause;

    public KeyStore getKeystore() {
        return this.mKeystore;
    }

    public void setKeystore(KeyStore keystore) {
        this.mKeystore = keystore;
    }

    public String getUniqueId() {
        return this.mUniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.mUniqueId = uniqueId;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public int getRealCipherMode() {
        return this.mRealCipherMode;
    }

    public void setRealCipherMode(int mode) {
        this.mRealCipherMode = mode;
    }

    public String getAppSignHash() {
        return this.mAppSignHash;
    }

    public void setAppSignHash(String appSignHash) {
        this.mAppSignHash = appSignHash;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getShortPackageName() {
        return this.mShortPackageName;
    }

    private void setShortPackageName(String packageName) {
        this.mShortPackageName = packageName;
        if (packageName == null || (TextUtils.isEmpty(packageName) ^ 1) == 0) {
            this.mShortPackageName = SmsConstants.FORMAT_UNKNOWN;
        } else {
            int lastIndexOfPoint = packageName.lastIndexOf(46);
            if (lastIndexOfPoint != -1) {
                this.mShortPackageName = packageName.substring(lastIndexOfPoint + 1);
            }
        }
        if (this.mShortPackageName.length() > 16) {
            this.mShortPackageName = this.mShortPackageName.substring(0, 15);
        }
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
        setShortPackageName(packageName);
    }

    public int getKeyVersion() {
        return this.mKeyVersion;
    }

    public int getKeyVersion(int type) {
        switch (type) {
            case 1:
                return this.mKeyVersion & 255;
            case 2:
                return (this.mKeyVersion >> 8) & 255;
            case 4:
                return (this.mKeyVersion >> 16) & 255;
            default:
                return 0;
        }
    }

    public void setKeyVersion(int keyVersion) {
        this.mKeyVersion = keyVersion;
    }

    public String getToken() {
        return this.mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public void setCipherMode(int mode) {
        this.mCipherMode = mode;
    }

    public int getCipherMode() {
        return this.mCipherMode;
    }

    public void setUpdateKeyFailCause(int cause) {
        this.mUpdateKeyFailCause = cause;
    }

    public int getUpdateKeyFailCause() {
        return this.mUpdateKeyFailCause;
    }

    public void setLastTryUpdateKeyTime(long updateKeytime) {
        this.mLastTryUpdateKeyTime = updateKeytime;
    }

    public long getLastTryUpdateKeyTime() {
        return this.mLastTryUpdateKeyTime;
    }

    public void setLastExecTime(long execTime) {
        this.mLastExecTime = execTime;
    }

    public long getLastExecTime() {
        return this.mLastExecTime;
    }
}
