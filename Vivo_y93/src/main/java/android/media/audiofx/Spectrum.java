package android.media.audiofx;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.lang.ref.WeakReference;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class Spectrum {
    public static final int ALREADY_EXISTS = -2;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -4;
    public static final int ERROR_DEAD_OBJECT = -7;
    public static final int ERROR_INVALID_OPERATION = -5;
    public static final int ERROR_NO_INIT = -3;
    public static final int ERROR_NO_MEMORY = -6;
    private static final int NATIVE_EVENT_FFT_CAPTURE = 1;
    private static final int NATIVE_EVENT_PCM_CAPTURE = 0;
    private static final int NATIVE_EVENT_SERVER_DIED = 2;
    public static final int SCALING_MODE_AS_PLAYED = 1;
    public static final int SCALING_MODE_NORMALIZED = 0;
    public static final int STATE_ENABLED = 2;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "Spectrum-JAVA";
    private OnDataCaptureListener mCaptureListener = null;
    private int mId;
    private int mJniData;
    private final Object mListenerLock = new Object();
    private NativeEventHandler mNativeEventHandler = null;
    private int mNativeSpectrum;
    private OnServerDiedListener mServerDiedListener = null;
    private int mState = 0;
    private final Object mStateLock = new Object();

    private class NativeEventHandler extends Handler {
        private Spectrum mSpectrum;

        public NativeEventHandler(Spectrum v, Looper looper) {
            super(looper);
            this.mSpectrum = v;
        }

        private void handleCaptureMessage(Message msg) {
            OnDataCaptureListener l;
            synchronized (Spectrum.this.mListenerLock) {
                l = this.mSpectrum.mCaptureListener;
            }
            if (l != null) {
                byte[] data = msg.obj;
                int samplingRate = msg.arg1;
                switch (msg.what) {
                    case 0:
                        l.onWaveFormDataCapture(this.mSpectrum, data, samplingRate);
                        return;
                    case 1:
                        l.onFftDataCapture(this.mSpectrum, data, samplingRate);
                        return;
                    default:
                        Log.e(Spectrum.TAG, "Unknown native event in handleCaptureMessge: " + msg.what);
                        return;
                }
            }
        }

        private void handleServerDiedMessage(Message msg) {
            OnServerDiedListener l;
            synchronized (Spectrum.this.mListenerLock) {
                l = this.mSpectrum.mServerDiedListener;
            }
            if (l != null) {
                l.onServerDied();
            }
        }

        public void handleMessage(Message msg) {
            if (this.mSpectrum != null) {
                switch (msg.what) {
                    case 0:
                    case 1:
                        handleCaptureMessage(msg);
                        break;
                    case 2:
                        handleServerDiedMessage(msg);
                        break;
                    default:
                        Log.e(Spectrum.TAG, "Unknown native event: " + msg.what);
                        break;
                }
            }
        }
    }

    public interface OnDataCaptureListener {
        void onFftDataCapture(Spectrum spectrum, byte[] bArr, int i);

        void onWaveFormDataCapture(Spectrum spectrum, byte[] bArr, int i);
    }

    public interface OnServerDiedListener {
        void onServerDied();
    }

    public static native int[] getCaptureSizeRange();

    public static native int getMaxCaptureRate();

    private final native void native_finalize();

    private final native int native_getCaptureSize();

    private final native boolean native_getEnabled();

    private final native int native_getFft(byte[] bArr);

    private final native int native_getSamplingRate();

    private final native int native_getScalingMode();

    private final native int native_getWaveForm(byte[] bArr);

    private static final native void native_init();

    private final native void native_release();

    private final native int native_setCaptureSize(int i);

    private final native int native_setEnabled(boolean z);

    private final native int native_setPeriodicCapture(int i, boolean z, boolean z2);

    private final native int native_setScalingMode(int i);

    private final native int native_setup(Object obj, int i, int[] iArr);

    static {
        System.loadLibrary("Spectrum_jni");
        native_init();
    }

    public Spectrum(int audioSession) throws UnsupportedOperationException, RuntimeException {
        int[] id = new int[1];
        synchronized (this.mStateLock) {
            this.mState = 0;
            int result = native_setup(new WeakReference(this), audioSession, id);
            if (result == 0 || result == -2) {
                this.mId = id[0];
                if (native_getEnabled()) {
                    this.mState = 2;
                } else {
                    this.mState = 1;
                }
            } else {
                Log.e(TAG, "Error code " + result + " when initializing Spectrum.");
                switch (result) {
                    case -5:
                        throw new UnsupportedOperationException("Effect library not loaded");
                    default:
                        throw new RuntimeException("Cannot initialize Spectrum engine, error: " + result);
                }
            }
        }
    }

    public void release() {
        synchronized (this.mStateLock) {
            native_release();
            this.mState = 0;
        }
    }

    protected void finalize() {
        native_finalize();
    }

    /* JADX WARNING: Missing block: B:12:0x0011, code:
            if (r5.mState == 1) goto L_0x0013;
     */
    /* JADX WARNING: Missing block: B:13:0x0013, code:
            r0 = native_setEnabled(r6);
     */
    /* JADX WARNING: Missing block: B:14:0x0017, code:
            if (r0 != 0) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:15:0x0019, code:
            if (r6 == false) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:16:0x001b, code:
            r5.mState = r1;
     */
    /* JADX WARNING: Missing block: B:18:0x001e, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:22:0x0023, code:
            if (r5.mState == 2) goto L_0x0013;
     */
    /* JADX WARNING: Missing block: B:23:0x0026, code:
            r1 = 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int setEnabled(boolean enabled) throws IllegalStateException {
        int i = 2;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return -3;
            }
            int status = 0;
            if (enabled) {
            }
            if (!enabled) {
            }
        }
    }

    public boolean getEnabled() {
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return false;
            }
            boolean native_getEnabled = native_getEnabled();
            return native_getEnabled;
        }
    }

    public int setCaptureSize(int size) throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState != 1) {
                return -5;
            }
            int native_setCaptureSize = native_setCaptureSize(size);
            return native_setCaptureSize;
        }
    }

    public int getCaptureSize() throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return -3;
            }
            int native_getCaptureSize = native_getCaptureSize();
            return native_getCaptureSize;
        }
    }

    public int setScalingMode(int mode) throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return -3;
            }
            int native_setScalingMode = native_setScalingMode(mode);
            return native_setScalingMode;
        }
    }

    public int getScalingMode() throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return -3;
            }
            int native_getScalingMode = native_getScalingMode();
            return native_getScalingMode;
        }
    }

    public int getSamplingRate() throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                return -3;
            }
            int native_getSamplingRate = native_getSamplingRate();
            return native_getSamplingRate;
        }
    }

    public int getWaveForm(byte[] waveform) throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState != 2) {
                return -5;
            }
            int native_getWaveForm = native_getWaveForm(waveform);
            return native_getWaveForm;
        }
    }

    public int getFft(byte[] fft) throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState != 2) {
                return -5;
            }
            int native_getFft = native_getFft(fft);
            return native_getFft;
        }
    }

    public int setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft) {
        synchronized (this.mListenerLock) {
            this.mCaptureListener = listener;
        }
        if (listener == null) {
            waveform = false;
            fft = false;
        }
        int status = native_setPeriodicCapture(rate, waveform, fft);
        if (status != 0 || listener == null || this.mNativeEventHandler != null) {
            return status;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, looper);
            return status;
        }
        looper = Looper.getMainLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, looper);
            return status;
        }
        this.mNativeEventHandler = null;
        return -3;
    }

    public int setServerDiedListener(OnServerDiedListener listener) {
        synchronized (this.mListenerLock) {
            this.mServerDiedListener = listener;
        }
        return 0;
    }

    private static void postEventFromNative(Object effect_ref, int what, int arg1, int arg2, Object obj) {
        Spectrum visu = (Spectrum) ((WeakReference) effect_ref).get();
        if (!(visu == null || visu.mNativeEventHandler == null)) {
            visu.mNativeEventHandler.sendMessage(visu.mNativeEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }
}
