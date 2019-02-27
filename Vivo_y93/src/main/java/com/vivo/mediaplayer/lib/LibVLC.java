package com.vivo.mediaplayer.lib;

import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import com.vivo.mediaplayer.MediaPlayerUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LibVLC {
    public static final int AOUT_AUDIOTRACK = 1;
    public static final int AOUT_AUDIOTRACK_JAVA = 0;
    public static final int AOUT_OPENSLES = 2;
    private static final String TAG = "VMediaPlayer/lib";
    private static LibVLC sInstance;
    private int aout = 0;
    private String chroma = "RV32";
    private boolean iomx = false;
    private Aout mAout = new Aout();
    private StringBuffer mDebugLogBuffer;
    private long mInternalMediaPlayerInstance = 0;
    private boolean mIsBufferingLog = false;
    private boolean mIsInitialized = false;
    private long mLibVlcInstance = 0;
    private int mStreamType = 3;
    private String subtitlesEncoding = "UTF8";
    private boolean timeStretching = false;
    private boolean verboseMode = MediaPlayerUtils.DEBUG;

    private native void detachEventHandler();

    private native long getLengthFromLocation(long j, String str);

    private native byte[] getThumbnail(long j, String str, int i, int i2, int i3);

    private native boolean hasVideoTrack(long j, String str);

    private native void nativeDestroy();

    private native void nativeInit() throws LibVlcException;

    public static native boolean nativeIsPathDirectory(String str);

    public static native void nativeReadDirectory(String str, ArrayList<String> arrayList);

    public static native String nativeToURI(String str);

    private native void playIndex(long j, int i);

    private native int readMedia(long j, String str, boolean z);

    private native String[] readMediaMeta(long j, String str);

    private native TrackInfo[] readTracksInfo(long j, String str);

    private native void setEventHandler(EventHandler eventHandler);

    public native void attachSurface(Surface surface, IVideoPlayer iVideoPlayer, int i, int i2);

    public native String changeset();

    public native String compiler();

    public native void detachSurface();

    public native int getAudioTrack();

    public native Map<Integer, String> getAudioTrackDescription();

    public native int getAudioTracksCount();

    public native long getLength();

    public native void getMediaListItems(ArrayList<String> arrayList);

    public native float getPosition();

    public native float getRate();

    public native int getSpuTrack();

    public native Map<Integer, String> getSpuTrackDescription();

    public native int getSpuTracksCount();

    public native long getTime();

    public native int getVideoTracksCount();

    public native int getVolume();

    public native boolean hasMediaPlayer();

    public native boolean isPlaying();

    public native boolean isSeekable();

    public native void next();

    public native void pause();

    public native void play();

    public native void previous();

    public native TrackInfo[] readTracksInfoPosition(int i);

    public native int setAudioTrack(int i);

    public native void setPosition(float f);

    public native void setRate(float f);

    public native int setSpuTrack(int i);

    public native void setSurface(Surface surface);

    public native long setTime(long j);

    public native int setVolume(int i);

    public native void startDebugBuffer();

    public native void stop();

    public native void stopDebugBuffer();

    public native String version();

    public void attachSurface(Surface surface, IVideoPlayer player) {
        attachSurface(surface, player, 0, 0);
    }

    static {
        try {
            System.loadLibrary("iomx_vivo");
        } catch (Throwable t) {
            Log.w(TAG, "Unable to load the iomx library: " + t);
        }
        try {
            System.loadLibrary("vlcjni_vivo");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load jni library: " + ule);
            System.exit(1);
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading vlcjni library: " + se);
            System.exit(1);
        }
    }

    public static LibVLC getInstance() throws LibVlcException {
        synchronized (LibVLC.class) {
            if (sInstance == null) {
                sInstance = new LibVLC();
                sInstance.init();
            }
        }
        return sInstance;
    }

    public static LibVLC getPlayerInstance() throws LibVlcException {
        LibVLC player = new LibVLC();
        player.init();
        return player;
    }

    public static LibVLC getExistingInstance() {
        LibVLC libVLC;
        synchronized (LibVLC.class) {
            libVLC = sInstance;
        }
        return libVLC;
    }

    private LibVLC() {
    }

    public void finalize() {
        if (this.mLibVlcInstance != 0) {
            Log.d(TAG, "LibVLC is was destroyed yet before finalize()");
            destroy();
        }
    }

    public boolean useIOMX() {
        String model = SystemProperties.get("ro.product.model.bbk", "UNKNOWN");
        if (!"MTK".equals(MediaPlayerUtils.SOLUTION) || ("PD1227T".equals(MediaPlayerUtils.MODEL) ^ 1) == 0 || ("PD1216T".equals(MediaPlayerUtils.MODEL) ^ 1) == 0) {
            this.iomx = true;
        } else {
            Log.d(TAG, "disabled hw decoder for MTK platform except MT6589T");
            this.iomx = false;
        }
        return this.iomx;
    }

    public void setIomx(boolean iomx) {
        this.iomx = iomx;
    }

    public String getSubtitlesEncoding() {
        return this.subtitlesEncoding;
    }

    public void setSubtitlesEncoding(String subtitlesEncoding) {
        this.subtitlesEncoding = subtitlesEncoding;
    }

    public int getAout() {
        return this.aout;
    }

    public void setAout(int aout) {
        Log.d(TAG, "setAout " + aout);
        this.aout = aout;
    }

    public boolean timeStretchingEnabled() {
        return this.timeStretching;
    }

    public void setTimeStretching(boolean timeStretching) {
        this.timeStretching = timeStretching;
    }

    public String getChroma() {
        int drMode = "".equals(MediaPlayerUtils.DR_MODE) ? 0 : Integer.parseInt(MediaPlayerUtils.DR_MODE);
        if (drMode == 1) {
            Log.i(TAG, "In force direct rendering mode");
            this.chroma = null;
        } else if (drMode == 2) {
            Log.i(TAG, "In force NO direct rendering mode");
            this.chroma = "RV32";
        }
        return this.chroma;
    }

    public void setChroma(String chroma) {
        this.chroma = chroma;
    }

    public boolean isVerboseMode() {
        return this.verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
    }

    public void init() throws LibVlcException {
        Log.v(TAG, "Initializing VMediaPlayer/lib");
        this.mDebugLogBuffer = new StringBuffer();
        if (!this.mIsInitialized) {
            nativeInit();
            setEventHandler(EventHandler.getInstance());
            this.mIsInitialized = true;
        }
    }

    public void destroy() {
        Log.v(TAG, "Destroying VMediaPlayer/lib instance");
        nativeDestroy();
        detachEventHandler();
        this.mIsInitialized = false;
    }

    public void setAudioStreamType(int streamtype) {
        this.mStreamType = streamtype;
    }

    public int getAudioSessionId() {
        return this.mAout.getAudioSessionId();
    }

    public void initAout(int sampleRateInHz, int channels, int samples) {
        Log.d(TAG, "Opening the java audio output");
        this.mAout.init(sampleRateInHz, channels, samples, this.mStreamType);
    }

    public void playAudio(byte[] audioData, int bufferSize) {
        this.mAout.playBuffer(audioData, bufferSize);
    }

    public void pauseAout() {
        Log.d(TAG, "Pausing the java audio output");
        this.mAout.pause();
    }

    public void closeAout() {
        Log.d(TAG, "Closing the java audio output");
        this.mAout.release();
    }

    public void readMedia(String mrl) {
        readMedia(this.mLibVlcInstance, mrl, false);
    }

    public int readMedia(String mrl, boolean novideo) {
        return readMedia(this.mLibVlcInstance, mrl, novideo);
    }

    public void playIndex(int position) {
        playIndex(this.mLibVlcInstance, position);
    }

    public String[] readMediaMeta(String mrl) {
        return readMediaMeta(this.mLibVlcInstance, mrl);
    }

    public TrackInfo[] readTracksInfo(String mrl) {
        return readTracksInfo(this.mLibVlcInstance, mrl);
    }

    public byte[] getThumbnail(String mrl, int i_width, int i_height) {
        return getThumbnail(this.mLibVlcInstance, mrl, i_width, i_height, 0);
    }

    public boolean hasVideoTrack(String mrl) throws IOException {
        return hasVideoTrack(this.mLibVlcInstance, mrl);
    }

    public long getLengthFromLocation(String mrl) {
        return getLengthFromLocation(this.mLibVlcInstance, mrl);
    }

    public String getBufferContent() {
        return this.mDebugLogBuffer.toString();
    }

    public void clearBuffer() {
        this.mDebugLogBuffer.setLength(0);
    }

    public boolean isDebugBuffering() {
        return this.mIsBufferingLog;
    }

    public static String PathToURI(String path) {
        if (path != null) {
            return nativeToURI(path);
        }
        throw new NullPointerException("Cannot convert null path!");
    }
}
