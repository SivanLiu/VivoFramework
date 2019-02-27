package android.media;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.media.IAudioFeatureCallback.Stub;
import android.media.PlayerBase.PlayerIdCard;
import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class AudioFeatures {
    protected static final String COMP_RINGTONE = "RINGTONE";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static boolean IsRomMatch_Vivo = false;
    public static final String KEY_COMPONENT = "component";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_LOOPING = "looping";
    public static final String KEY_RETURN = "return";
    public static final String KEY_STATE = "state";
    public static final String KEY_STREAM = "stream";
    public static final String KEY_VOLUME = "volume";
    private static final String TAG = "AudioFeatures";
    public static final String TAG_DMIC = "DMIC";
    public static final String TAG_DMIC_VIVOICE = "DMIC.VIVOICE";
    public static final String TAG_FACE_DETECT = "FACEDT";
    public static final String TAG_FD_START = "start";
    public static final String TAG_FD_STOP = "stop";
    public static final String TAG_HIFI = "HIFI";
    public static final String TAG_HISING_DEVICE = "HISING_DEVICE";
    public static final String TAG_HISING_ENABLE = "HISING_ENABLE";
    public static final String TAG_HISING_MODE = "HISING_MODE";
    public static final String TAG_IMUSIC = "IMUS";
    public static final String TAG_INTF = "INTF";
    public static final String TAG_MAXV = "MAXV";
    public static final String TAG_MUTEKEY = "MUTK";
    private static String TAG_NONE = "NONE";
    public static final String TAG_SPKR_MODE = "SPKR";
    public static final String TAG_SRSVIPPLUS = "SRSVIPPLUS";
    public static final String TAG_VIPPLUSPARA = "VIPPLUSPARA";
    public static final String TAG_VIVOVIDEO = "IVIVOVIDEO";
    public static final String VALUE_ERROR = "ERROR";
    public static final String VALUE_OK = "OK";
    private static IAudioService sService;
    private final String FEATURE_MANAGER_CLASS_NAME = "com.vivo.media.FeatureManager";
    private String mComponent;
    private Context mContext;
    private final IBinder mICallBack = new Binder();
    private Object mObj;
    private String mTag;

    public static class AudioFeatureCallback {
        private IAudioFeatureCallback mCb;
        private String mComponent;
        private Context mContext;
        private Object mObj;
        private String mTag;

        public AudioFeatureCallback(Context context, String arg0, Object obj) {
            this.mContext = context;
            TagParameters tp = new TagParameters(arg0);
            this.mTag = tp.tag();
            if (this.mTag == null) {
                this.mTag = AudioFeatures.TAG_NONE;
            }
            this.mObj = obj;
            this.mComponent = tp.get("component");
            if (this.mComponent != null && this.mObj == null) {
                this.mComponent = null;
                Log.w(AudioFeatures.TAG, "set component without obj is useless");
            }
            if (this.mComponent == null) {
                this.mCb = new Stub() {
                    public String onCallback(String arg0) {
                        TagParameters tp = new TagParameters(arg0);
                        if (AudioFeatureCallback.this.mTag.equals(tp.tag())) {
                            return AudioFeatureCallback.this.onCallback(arg0, AudioFeatureCallback.this.mObj);
                        }
                        tp.put(AudioFeatures.KEY_RETURN, AudioFeatures.VALUE_ERROR);
                        return tp.toString();
                    }
                };
            }
        }

        protected IAudioFeatureCallback getCallback() {
            return this.mCb;
        }

        public String onCallback(String arg0, Object obj) {
            return null;
        }
    }

    public static class TagParameters {
        private static final boolean DEBUG = false;
        private static final String TAG = "TagParameters";
        private final HashMap<String, String> mKeyValues = new HashMap();
        private String mTag;

        public TagParameters(String arg0) {
            if (arg0 == null) {
                this.mTag = null;
                return;
            }
            StringTokenizer st = new StringTokenizer(arg0, ":=;");
            int tokens = st.countTokens();
            if (tokens < 1 || tokens % 2 == 0) {
                Log.e(TAG, "malformated string " + arg0);
                this.mTag = null;
                return;
            }
            this.mTag = st.nextToken();
            while (st.hasMoreTokens()) {
                this.mKeyValues.put(st.nextToken(), st.nextToken());
            }
        }

        public String tag() {
            return this.mTag;
        }

        public boolean contains(String key) {
            return this.mKeyValues.containsKey(key);
        }

        public String get(String key) {
            if (this.mKeyValues.containsKey(key)) {
                return (String) this.mKeyValues.get(key);
            }
            return null;
        }

        public short getShort(String key, short def) {
            String value = get(key);
            if (value != null) {
                return Short.parseShort(value);
            }
            return def;
        }

        public int getInt(String key, int def) {
            String value = get(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
            return def;
        }

        public float getFloat(String key, float def) {
            String value = get(key);
            if (value != null) {
                return Float.parseFloat(value);
            }
            return def;
        }

        public double getDouble(String key, double def) {
            String value = get(key);
            if (value != null) {
                return Double.parseDouble(value);
            }
            return def;
        }

        public boolean getBoolean(String key, boolean def) {
            String value = get(key);
            if (value != null) {
                return Boolean.parseBoolean(value);
            }
            return def;
        }

        public void put(String key, String value) {
            if (key != null && value != null) {
                this.mKeyValues.put(key, value);
            }
        }

        public void put(String key, short value) {
            put(key, Short.toString(value));
        }

        public void put(String key, int value) {
            put(key, Integer.toString(value));
        }

        public void put(String key, float value) {
            put(key, Float.toString(value));
        }

        public void put(String key, boolean value) {
            put(key, Boolean.toString(value));
        }

        public void put(String key, double value) {
            put(key, Double.toString(value));
        }

        public String toString() {
            String str = this.mTag + ":";
            for (Entry entry : this.mKeyValues.entrySet()) {
                str = str + entry.getKey() + "=" + entry.getValue() + ";";
            }
            return str;
        }
    }

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    static {
        IsRomMatch_Vivo = false;
        try {
            String[] parts = SystemProperties.get("ro.vivo.rom", null).split("_");
            String part1 = parts[0];
            float romVerion = Float.parseFloat(parts[1]);
            Log.d(TAG, "Detect the rom verion rom:" + part1 + " Verion:" + romVerion);
            if (((double) romVerion) >= 3.5d) {
                IsRomMatch_Vivo = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public AudioFeatures(Context context, String arg0, Object obj) {
        this.mContext = context;
        TagParameters tp = new TagParameters(arg0);
        this.mTag = tp.tag();
        this.mObj = obj;
        this.mComponent = tp.get("component");
        if (this.mComponent != null && this.mObj == null) {
            Log.w(TAG, "DEFINE component without obj is danger");
            this.mComponent = null;
        }
    }

    public String setAudioFeature(String feature, Object obj) {
        IAudioService service = getService();
        if (service != null) {
            try {
                return (String) service.getClass().getMethod("setAudioFeature", new Class[]{String.class, IBinder.class}).invoke(service, new Object[]{feature, this.mICallBack});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return null;
    }

    public boolean setMicPhoneEnable(String feature, Object obj) {
        setAudioFeature(feature, obj);
        return true;
    }

    public String getAudioFeature(String feature, Object obj) {
        IAudioService service = getService();
        if (service != null) {
            try {
                return (String) service.getClass().getMethod("getAudioFeature", new Class[]{String.class}).invoke(service, new Object[]{feature});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return null;
    }

    public void setDeltaStreamVolume(int streamType, int index, int flags) {
        if (IsRomMatch_Vivo) {
            IAudioService service = getService();
            if (service != null) {
                try {
                    service.getClass().getMethod("setStreamVolumeDelta", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class}).invoke(service, new Object[]{Integer.valueOf(streamType), Integer.valueOf(index), Integer.valueOf(flags), this.mContext.getOpPackageName()});
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                } catch (InvocationTargetException e3) {
                    e3.printStackTrace();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            return;
        }
        Log.e(TAG, "This platform do not supprt DeltaVolume, please check application flow");
    }

    public int getDeltaStreamVolume(int streamType) {
        if (IsRomMatch_Vivo) {
            IAudioService service = getService();
            if (service != null) {
                try {
                    return ((Integer) service.getClass().getMethod("getStreamVolumeDelta", new Class[]{Integer.TYPE}).invoke(service, new Object[]{Integer.valueOf(streamType)})).intValue();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                } catch (InvocationTargetException e3) {
                    e3.printStackTrace();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            return 0;
        }
        Log.e(TAG, "This platform do not supprt DeltaVolume, please check application flow");
        return 0;
    }

    public int getDeltaStreamVolumeMax(int streamType) {
        if (IsRomMatch_Vivo) {
            IAudioService service = getService();
            if (service != null) {
                try {
                    return ((Integer) service.getClass().getMethod("getStreamVolumeMaxDelta", new Class[]{Integer.TYPE}).invoke(service, new Object[]{Integer.valueOf(streamType)})).intValue();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                } catch (InvocationTargetException e3) {
                    e3.printStackTrace();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            return 1;
        }
        Log.e(TAG, "This platform do not supprt DeltaVolume, please check application flow");
        return 1;
    }

    public boolean isSupportDeltaStreamVolume() {
        return IsRomMatch_Vivo;
    }

    public boolean SetZenModeUtils(String keys, FileDescriptor fd, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        IAudioService service = getService();
        try {
            if (AdjustZenModeUtils(this.mContext.getOpPackageName())) {
                if (SetZenModegetFilePath(fd, Binder.getCallingPid()).equals("")) {
                    Log.e(TAG, "SetZenModeUtils cannot get the file path");
                    return false;
                }
                if (service != null) {
                    return ((Boolean) service.getClass().getMethod("isZenmodSurpported", new Class[]{String.class, String.class, String.class, Long.TYPE}).invoke(service, new Object[]{keys, this.mContext.getOpPackageName(), filePath, Long.valueOf(length)})).booleanValue();
                }
                return false;
            }
            Log.e(TAG, "Application cannot support or Zenmode close");
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004c A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassNotFoundException (e java.lang.ClassNotFoundException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x004c A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassNotFoundException (e java.lang.ClassNotFoundException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:14:0x004d, code:
            android.util.Log.e(TAG, "Unable to find class com.vivo.media.FeatureManager");
     */
    /* JADX WARNING: Missing block: B:16:0x0058, code:
            android.util.Log.e(TAG, "Unknown exception while invoke getFilePath");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String SetZenModegetFilePath(FileDescriptor fd, int pid) {
        try {
            return (String) Class.forName("com.vivo.media.FeatureManager").getMethod("getFilePath", new Class[]{FileDescriptor.class, Integer.TYPE}).invoke(null, new Object[]{fd, Integer.valueOf(pid)});
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getFilePath method not found in class com.vivo.media.FeatureManager");
        } catch (Exception ex) {
            Log.e(TAG, "Unknown exception hit while invoke getFilePath");
            ex.printStackTrace();
        } catch (ClassNotFoundException e2) {
        }
        return "";
    }

    public boolean AdjustZenModeUtils(String PkgName) throws IOException, IllegalArgumentException, IllegalStateException {
        IAudioService service = getService();
        if (service != null) {
            try {
                return ((Boolean) service.getClass().getMethod("AdjustZenModeUtils", new Class[]{String.class}).invoke(service, new Object[]{PkgName})).booleanValue();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return false;
    }

    public boolean isMotorModeSupportPlayback() {
        IAudioService service = getService();
        if (service != null) {
            try {
                return ((Boolean) service.getClass().getMethod("isMotorModeSupportPlayback", new Class[]{String.class}).invoke(service, new Object[]{this.mContext.getOpPackageName()})).booleanValue();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return true;
    }

    public void PlaybackDetectionCallBack(String tag, String state, String packageName, int usage, PlayerBase mPlayerBase) {
        IAudioService service = getService();
        try {
            Field f = mPlayerBase.getClass().getField("mPlayerIdCard");
            if (f != null && service != null) {
                PlayerIdCard temp_mPlayerIdCard = (PlayerIdCard) f.get(mPlayerBase);
                Log.d(TAG, "PlaybackDetectionCallBack start:" + ((IPlayer) temp_mPlayerIdCard.getClass().getField("mIPlayer").get(temp_mPlayerIdCard)).toString());
                service.getClass().getMethod("PlaybackDetectionCallBack", new Class[]{String.class, String.class, String.class, Integer.TYPE, String.class, PlayerIdCard.class}).invoke(service, new Object[]{tag, state, packageName, Integer.valueOf(usage), mPlayerAddr, temp_mPlayerIdCard});
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    public boolean isSmartRingModeEnable() {
        IAudioService service = getService();
        if (service != null) {
            try {
                return ((Boolean) service.getClass().getMethod("isSmartRingModeEnable", new Class[0]).invoke(service, new Object[0])).booleanValue();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return false;
    }

    public String registerAudioFeatureCallback(AudioFeatureCallback callback, String arg0, Object obj) {
        IAudioService service = getService();
        if (service != null) {
            try {
                return (String) service.getClass().getMethod("registerAudioFeatureCallback", new Class[]{IAudioFeatureCallback.class, String.class, IBinder.class}).invoke(service, new Object[]{callback.getCallback(), arg0, this.mICallBack});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return null;
    }

    public String unregisterAudioFeatureCallback(AudioFeatureCallback callback, String arg0, Object obj) {
        IAudioService service = getService();
        if (service != null) {
            try {
                return (String) service.getClass().getMethod("unregisterAudioFeatureCallback", new Class[]{IAudioFeatureCallback.class, String.class}).invoke(service, new Object[]{callback.getCallback(), arg0});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return null;
    }
}
