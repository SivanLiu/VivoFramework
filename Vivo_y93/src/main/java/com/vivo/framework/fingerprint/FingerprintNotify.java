package com.vivo.framework.fingerprint;

import android.util.Slog;

public class FingerprintNotify {
    private static final String TAG = "FingerprintNotify";
    private static FingerprintNotify mFingerprintNotify;
    private static Object mLock = new Object();
    private FingerprintNotifyMessage mFingerprintNotifyMessage;

    public interface FingerprintNotifyMessage {
        void notifyMessage(int i);

        void notifyOtherMessage(String str, byte[] bArr, int i, String str2);
    }

    private FingerprintNotify() {
        Slog.d(TAG, "FingerprintNotify instance created");
    }

    public static FingerprintNotify getInstance() {
        if (mFingerprintNotify == null) {
            synchronized (mLock) {
                if (mFingerprintNotify == null) {
                    mFingerprintNotify = new FingerprintNotify();
                }
            }
        }
        return mFingerprintNotify;
    }

    public void registerCallBack(FingerprintNotifyMessage listener) {
        this.mFingerprintNotifyMessage = listener;
    }

    public void notifyMessage(int message) {
        if (this.mFingerprintNotifyMessage != null) {
            this.mFingerprintNotifyMessage.notifyMessage(message);
        } else {
            Slog.w(TAG, "notifyMessage failed");
        }
    }

    public void notifyOtherMessage(String message, byte[] array, int param, String extra) {
        if (this.mFingerprintNotifyMessage != null) {
            this.mFingerprintNotifyMessage.notifyOtherMessage(message, array, param, extra);
        } else {
            Slog.w(TAG, "notifyOtherMessage failed");
        }
    }
}
