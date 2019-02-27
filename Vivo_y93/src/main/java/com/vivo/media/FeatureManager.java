package com.vivo.media;

import android.util.Log;
import java.io.FileDescriptor;

public class FeatureManager {
    public static final int FEATURE_NOTIFY_DSD_CALLBACK = 204;
    public static final int FEATURE_NOTIFY_HIFI_CALLBACK = 200;
    public static final int FEATURE_NOTIFY_KTV_REC_CALLBACK = 203;
    public static final int FEATURE_NOTIFY_MEDIASERVER_DIED = 202;
    public static final int FEATURE_NOTIFY_PLAY_CALLBACK = 201;
    public static final int FEATURE_TO_PLAYER_A2DP_AVAILABLE = 502;
    public static final int FEATURE_TO_PLAYER_AUDIO_NATIVE_LIST_UPDATE = 504;
    public static final int FEATURE_TO_PLAYER_DSD_ENABLE = 500;
    public static final int FEATURE_TO_PLAYER_HEADSET_PULG = 501;
    public static final int FEATURE_TO_PLAYER_VIDEOLIST_UPDATE = 503;
    static final String TAG = "FeatureManager";
    private static NotifyCallback mNotifyCallback;

    public interface NotifyCallback {
        void onNotifyCallback(int i, int i2, int[] iArr);
    }

    public static native String getAudioFeatures(String str);

    public static native String getFilePath(FileDescriptor fileDescriptor, int i);

    public static native int getPhoneState_POM();

    public static native int getPlayerFeatures(int i, byte[] bArr, int i2);

    public static native String getPolicyFeatures(String str);

    public static native int native_init();

    public static native int setAudioFeatures(String str);

    public static native int setPhoneState_POM(int i, int i2);

    public static native int setPlayerFeatures(int i, byte[] bArr, int i2);

    public static native int setPolicyFeatures(String str);

    public static native int setStreamVolumeDeltaIndex(int i, int i2, int i3);

    static {
        Log.v(TAG, "static initilize of FeatureManager");
        System.loadLibrary("featuremanager_jni");
    }

    public static void setNotifyCallback(NotifyCallback cb) {
        synchronized (FeatureManager.class) {
            mNotifyCallback = cb;
        }
    }

    private static void notifyCallbackFromNative(int msg0, int msg1, int[] msg2) {
        NotifyCallback notifyCallback = null;
        synchronized (FeatureManager.class) {
            if (mNotifyCallback != null) {
                notifyCallback = mNotifyCallback;
            }
        }
        if (notifyCallback != null) {
            notifyCallback.onNotifyCallback(msg0, msg1, msg2);
        }
    }

    FeatureManager() {
        native_init();
    }
}
