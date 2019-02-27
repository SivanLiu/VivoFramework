package com.vivo.mediaplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.vivo.media.FeatureService;
import com.vivo.mediaplayer.IMediaPlayer.OnBufferingUpdateListener;
import com.vivo.mediaplayer.IMediaPlayer.OnCompletionListener;
import com.vivo.mediaplayer.IMediaPlayer.OnErrorListener;
import com.vivo.mediaplayer.IMediaPlayer.OnInfoListener;
import com.vivo.mediaplayer.IMediaPlayer.OnPreparedListener;
import com.vivo.mediaplayer.IMediaPlayer.OnSeekCompleteListener;
import com.vivo.mediaplayer.IMediaPlayer.OnTimedTextListener;
import com.vivo.mediaplayer.IMediaPlayer.OnVideoSizeChangedListener;
import com.vivo.mediaplayer.IMediaPlayer.TrackInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

class NaiveMediaPlayer implements IMediaPlayer {
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_CAPTURE_CURRENT_FRAME = 17;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_IS_HW_DECODER = 19;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int INVOKE_ID_TURN_ON_HQV = 18;
    private static final String TAG = "NaiveMediaPlayer";
    private static boolean mVivoSubtitleSupport = SystemProperties.getBoolean("ro.vivo.media.subtitle_support", false);
    private Pattern mAssDrawingPattern;
    private Pattern mAssTagPattern;
    private boolean mHasAssSubtitle;
    private MediaPlayer mMediaPlayer;
    private SubtitleState mSubtitleState;

    public static class FrameInfo implements Parcelable {
        private byte[] mBuffer;
        private int mColorFormat;
        private int mHeight;
        private int mRotation;
        private int mSize;
        private int mSliceHeight;
        private int mStride;
        private int mWidth;

        public FrameInfo(Parcel in) {
            this.mWidth = in.readInt();
            this.mHeight = in.readInt();
            this.mStride = in.readInt();
            this.mSliceHeight = in.readInt();
            this.mSize = in.readInt();
            this.mColorFormat = in.readInt();
            this.mRotation = in.readInt();
            this.mBuffer = NaiveMediaPlayer.getBufferFromParcel(in);
            if (this.mBuffer == null) {
                Log.e(NaiveMediaPlayer.TAG, "cannot read buffer");
            }
        }

        public static Bitmap rotateBitmap(Bitmap source, float angle) {
            if (angle == 0.0f || angle == 360.0f) {
                return source;
            }
            if (angle == 90.0f || angle == 270.0f || angle == 180.0f) {
                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                Bitmap bmp = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                Log.v(NaiveMediaPlayer.TAG, "Rotate bitmap for " + angle + " degrees, width " + bmp.getWidth() + ", height " + bmp.getHeight());
                return bmp;
            }
            throw new IllegalArgumentException("Illegal rotation angle " + angle);
        }

        public Bitmap getBitmap() {
            if (this.mBuffer == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(this.mBuffer);
            Bitmap bitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            return rotateBitmap(bitmap, (float) this.mRotation);
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0035 A:{SYNTHETIC, Splitter: B:15:0x0035} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0041 A:{SYNTHETIC, Splitter: B:21:0x0041} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void saveToFile(Bitmap bitmap) {
            Exception e;
            Throwable th;
            Log.d(NaiveMediaPlayer.TAG, "2saveToFile");
            BufferedOutputStream out = null;
            try {
                BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(new File("/storage/sdcard0/aa.jpg")));
                try {
                    bitmap.compress(CompressFormat.JPEG, 80, out2);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (Exception e3) {
                    e = e3;
                    out = out2;
                    try {
                        e.printStackTrace();
                        if (out == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (out == null) {
                    try {
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0036 A:{SYNTHETIC, Splitter: B:15:0x0036} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0042 A:{SYNTHETIC, Splitter: B:21:0x0042} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void dumpToFile() {
            Exception e;
            Throwable th;
            Log.d(NaiveMediaPlayer.TAG, "dumpToFile");
            BufferedOutputStream out = null;
            try {
                BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(new File("/storage/sdcard0/aa.rgb")));
                try {
                    out2.write(this.mBuffer, 0, this.mSize);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (Exception e3) {
                    e = e3;
                    out = out2;
                    try {
                        e.printStackTrace();
                        if (out == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (out == null) {
                    try {
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mWidth);
            dest.writeInt(this.mHeight);
            dest.writeInt(this.mSize);
        }
    }

    public static class FrameInfoUseCrop implements Parcelable {
        private byte[] mBuffer;
        private int mColorFormat;
        private int mCrop_H;
        private int mCrop_L;
        private int mCrop_T;
        private int mCrop_W;
        private int mHeight;
        private int mRotation;
        private int mSize;
        private int mSliceHeight;
        private int mStride;
        private int mWidth;

        public FrameInfoUseCrop(Parcel in) {
            this.mWidth = in.readInt();
            this.mHeight = in.readInt();
            this.mStride = in.readInt();
            this.mSliceHeight = in.readInt();
            this.mSize = in.readInt();
            this.mColorFormat = in.readInt();
            this.mRotation = in.readInt();
            this.mCrop_L = in.readInt();
            this.mCrop_T = in.readInt();
            this.mCrop_W = in.readInt();
            this.mCrop_H = in.readInt();
            this.mBuffer = NaiveMediaPlayer.getBufferFromParcelUseCrop(in);
            if (this.mBuffer == null) {
                Log.e(NaiveMediaPlayer.TAG, "cannot read buffer");
            }
        }

        public static Bitmap rotateBitmap(Bitmap source, float angle) {
            if (angle == 0.0f || angle == 360.0f) {
                return source;
            }
            if (angle == 90.0f || angle == 270.0f || angle == 180.0f) {
                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                Bitmap bmp = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                Log.d(NaiveMediaPlayer.TAG, "Rotate bitmap for " + angle + " degrees, width " + bmp.getWidth() + ", height " + bmp.getHeight());
                return bmp;
            }
            throw new IllegalArgumentException("Illegal rotation angle " + angle);
        }

        public Bitmap getBitmap() {
            if (this.mBuffer == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(this.mBuffer);
            int actualW = this.mCrop_W + this.mCrop_L;
            int actualH = this.mCrop_H + this.mCrop_T;
            Log.d(NaiveMediaPlayer.TAG, "getBitmap actual W: " + actualW + "  actual H: " + actualH);
            Bitmap bitmap = Bitmap.createBitmap(actualW, actualH, Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            return rotateBitmap(bitmap, (float) this.mRotation);
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0035 A:{SYNTHETIC, Splitter: B:15:0x0035} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0041 A:{SYNTHETIC, Splitter: B:21:0x0041} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void saveToFile(Bitmap bitmap) {
            Exception e;
            Throwable th;
            Log.d(NaiveMediaPlayer.TAG, "2saveToFile");
            BufferedOutputStream out = null;
            try {
                BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(new File("/storage/sdcard0/aa.jpg")));
                try {
                    bitmap.compress(CompressFormat.JPEG, 80, out2);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (Exception e3) {
                    e = e3;
                    out = out2;
                    try {
                        e.printStackTrace();
                        if (out == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (out == null) {
                    try {
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0036 A:{SYNTHETIC, Splitter: B:15:0x0036} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0042 A:{SYNTHETIC, Splitter: B:21:0x0042} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void dumpToFile() {
            Exception e;
            Throwable th;
            Log.d(NaiveMediaPlayer.TAG, "dumpToFile");
            BufferedOutputStream out = null;
            try {
                BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(new File("/storage/sdcard0/aa.rgb")));
                try {
                    out2.write(this.mBuffer, 0, this.mSize);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (Exception e3) {
                    e = e3;
                    out = out2;
                    try {
                        e.printStackTrace();
                        if (out == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (out == null) {
                    try {
                        out.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mWidth);
            dest.writeInt(this.mHeight);
            dest.writeInt(this.mSize);
        }
    }

    private class SubtitleState {
        private final int SUBTITLE_NULL = 1;
        private final int SUBTITLE_TEXT = 0;
        private LinkedList mState = new LinkedList();

        public void push(int sub) {
            if (sub == 0) {
                this.mState.push(Integer.valueOf(0));
            } else if (sub != 1) {
                throw new IllegalArgumentException();
            } else if (this.mState.size() > 0) {
                this.mState.pop();
            } else {
                Log.i(NaiveMediaPlayer.TAG, "SubtitleState not inited");
            }
        }

        public void push(TimedText t) {
            push(t == null ? 1 : 0);
        }

        public boolean ok() {
            return this.mState.size() == 0;
        }

        public int size() {
            return this.mState.size();
        }

        public void clear() {
            this.mState.clear();
        }
    }

    public static native byte[] getBufferFromParcel(Parcel parcel);

    public static native byte[] getBufferFromParcelUseCrop(Parcel parcel);

    static {
        try {
            System.loadLibrary("videocapture_vivo");
        } catch (Exception e) {
            Log.e(TAG, "Unable to load library");
        }
    }

    public NaiveMediaPlayer() {
        this.mMediaPlayer = null;
        this.mAssTagPattern = null;
        this.mAssDrawingPattern = null;
        this.mHasAssSubtitle = false;
        this.mSubtitleState = new SubtitleState();
        this.mMediaPlayer = new MediaPlayer();
        Log.d(TAG, "ro.vivo.media.subtitle_support = " + mVivoSubtitleSupport);
    }

    public Bitmap captureCurrentFrame() {
        Log.v(TAG, "============= captureCurrentFrame");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(INVOKE_ID_CAPTURE_CURRENT_FRAME);
            invoke(request, reply);
            Log.v(TAG, "begin get frame");
            String Board = SystemProperties.get("ro.product.board", null);
            if (Board.equals("sdm660")) {
                Log.d(TAG, "Board " + Board + " Use new FrameInfoUseCrop");
                FrameInfoUseCrop newinfo = new FrameInfoUseCrop(reply);
                Log.v(TAG, "begin get frame 2");
                Bitmap newbmp = newinfo.getBitmap();
                Log.v(TAG, "begin get frame 3");
                return newbmp;
            }
            FrameInfo info = new FrameInfo(reply);
            Log.v(TAG, "begin get frame 2");
            Bitmap bmp = info.getBitmap();
            Log.v(TAG, "begin get frame 3");
            request.recycle();
            reply.recycle();
            return bmp;
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to captureCurrentFrame ", e);
            return null;
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public int turnOnHqv(int percent) {
        Log.v(TAG, "turnOnHqv,percent " + percent);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(INVOKE_ID_TURN_ON_HQV);
            request.writeInt(percent);
            invoke(request, reply);
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to turnOnHqv ", e);
        } finally {
            request.recycle();
            reply.recycle();
        }
        return 0;
    }

    public boolean isHardwareDecoder() {
        boolean z = true;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(INVOKE_ID_IS_HW_DECODER);
            invoke(request, reply);
            if (reply.readInt() != 1) {
                z = false;
            }
            request.recycle();
            reply.recycle();
            return z;
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to isHardwareDecoder ", e);
            request.recycle();
            reply.recycle();
            return false;
        } catch (Throwable th) {
            request.recycle();
            reply.recycle();
            throw th;
        }
    }

    public Parcel newRequest() {
        return this.mMediaPlayer.newRequest();
    }

    public void invoke(Parcel request, Parcel reply) {
        this.mMediaPlayer.invoke(request, reply);
    }

    @Deprecated
    public void setDisplay(SurfaceView view) {
        Log.d(TAG, "setDisplay SurfaceView");
        this.mMediaPlayer.setDisplay(view.getHolder());
    }

    public void setDisplay(SurfaceHolder sh) {
        Log.d(TAG, "setDisplay SurfaceHolder");
        this.mMediaPlayer.setDisplay(sh);
    }

    public void setSurface(Surface surface) {
        Log.d(TAG, "setSurface Surface");
        this.mMediaPlayer.setSurface(surface);
    }

    public void setVideoScalingMode(int mode) {
        this.mMediaPlayer.setVideoScalingMode(mode);
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        Log.v(TAG, "setDataSource");
        this.mMediaPlayer.setDataSource(context, uri);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        Log.v(TAG, "setDataSource2");
        this.mMediaPlayer.setDataSource(context, uri, headers);
    }

    public void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        Log.v(TAG, "setDataSource " + path);
        this.mMediaPlayer.setDataSource(path);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(fd);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(fd, offset, length);
    }

    public void prepare() throws IOException, IllegalStateException {
        Log.v(TAG, "prepare");
        this.mMediaPlayer.prepare();
    }

    public void prepareAsync() throws IllegalStateException {
        Log.v(TAG, "prepareAsync");
        this.mMediaPlayer.prepareAsync();
    }

    public void start() throws IllegalStateException {
        Log.v(TAG, FeatureService.TAG_FACE_START);
        this.mMediaPlayer.start();
    }

    public void stop() throws IllegalStateException {
        Log.v(TAG, FeatureService.TAG_FACE_STOP);
        this.mMediaPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        Log.v(TAG, "pause");
        this.mMediaPlayer.pause();
    }

    public void setWakeMode(Context context, int mode) {
        this.mMediaPlayer.setWakeMode(context, mode);
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        this.mMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    public int getVideoWidth() {
        return this.mMediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return this.mMediaPlayer.getVideoHeight();
    }

    public boolean isPlaying() {
        return this.mMediaPlayer.isPlaying();
    }

    public void seekTo(int msec) throws IllegalStateException {
        Log.d(TAG, "seekTo " + msec + "ms");
        this.mMediaPlayer.seekTo(msec);
    }

    public int getCurrentPosition() {
        int pos = this.mMediaPlayer.getCurrentPosition();
        Log.v(TAG, "getCurrentPosition " + pos + "ms");
        return pos;
    }

    public int getDuration() {
        return this.mMediaPlayer.getDuration();
    }

    public MediaPlayer getMediaPlayer() {
        return this.mMediaPlayer;
    }

    public void setNextMediaPlayer(IMediaPlayer next) {
        if (next instanceof NaiveMediaPlayer) {
            this.mMediaPlayer.setNextMediaPlayer(((NaiveMediaPlayer) next).getMediaPlayer());
            return;
        }
        throw new IllegalArgumentException("next must be a NaiveMediaPlayer instance");
    }

    public void release() {
        Log.v(TAG, "release");
        this.mMediaPlayer.release();
        this.mSubtitleState.clear();
    }

    public void reset() {
        Log.v(TAG, "reset");
        this.mMediaPlayer.reset();
        this.mSubtitleState.clear();
    }

    public void setAudioStreamType(int streamtype) {
        this.mMediaPlayer.setAudioStreamType(streamtype);
    }

    public void setLooping(boolean looping) {
        this.mMediaPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return this.mMediaPlayer.isLooping();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        this.mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {
        this.mMediaPlayer.setAudioSessionId(sessionId);
    }

    public int getAudioSessionId() {
        return this.mMediaPlayer.getAudioSessionId();
    }

    public void attachAuxEffect(int effectId) {
        this.mMediaPlayer.attachAuxEffect(effectId);
    }

    public void setAuxEffectSendLevel(float level) {
        this.mMediaPlayer.setAuxEffectSendLevel(level);
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        MediaPlayer.TrackInfo[] infos = null;
        ArrayList<TrackInfo> newInfos = new ArrayList();
        if (VERSION.SDK_INT < 23 || (mVivoSubtitleSupport ^ 1) == 0) {
            try {
                infos = this.mMediaPlayer.getTrackInfo();
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (RuntimeException ex2) {
                Log.e(TAG, "getTrackInfo failed, catch RuntimeException ", ex2);
                return (TrackInfo[]) newInfos.toArray(new TrackInfo[0]);
            }
        }
        try {
            infos = (MediaPlayer.TrackInfo[]) this.mMediaPlayer.getClass().getMethod("getTrackInfoHook", new Class[0]).invoke(this.mMediaPlayer, new Object[0]);
            Log.e(TAG, " After Android6.0 use getTrackInfoHook");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        for (MediaPlayer.TrackInfo i : infos) {
            newInfos.add(new TrackInfo(i.getTrackType(), i.getLanguage()));
        }
        Log.d(TAG, "getTrackInfo " + newInfos);
        return (TrackInfo[]) newInfos.toArray(new TrackInfo[5]);
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if (mVivoSubtitleSupport) {
            if (mimeType == IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP || IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBASS.equals(mimeType) || IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBSSA.equals(mimeType)) {
                return true;
            }
        } else if (mimeType == IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP || IMediaPlayer.MEDIA_MIMETYPE_TEXT_ASS.equals(mimeType)) {
            return true;
        }
        return false;
    }

    public void addTimedTextSource(String path, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        Log.v(TAG, "addTimedTextSource " + path + ", mime " + mimeType);
        if (mVivoSubtitleSupport) {
            boolean z;
            if (IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBASS.equals(mimeType)) {
                z = true;
            } else {
                z = IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBSSA.equals(mimeType);
            }
            this.mHasAssSubtitle = z;
        } else {
            this.mHasAssSubtitle = IMediaPlayer.MEDIA_MIMETYPE_TEXT_ASS.equals(mimeType);
        }
        if (availableMimeTypeForExternalSource(mimeType)) {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                addTimedTextSource(is.getFD(), mimeType);
                is.close();
                return;
            }
            throw new IOException(path);
        }
        throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
    }

    /* JADX WARNING: Missing block: B:21:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            addTimedTextSource(assetFileDescriptor.getFileDescriptor(), mimeType);
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException e) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) {
        addTimedTextSource(fd, 0, 576460752303423487L, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mimeType) {
        if (mVivoSubtitleSupport) {
            boolean z;
            if (IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBASS.equals(mimeType)) {
                z = true;
            } else {
                z = IMediaPlayer.MEDIA_MIMETYPE_TEXT_SUBSSA.equals(mimeType);
            }
            this.mHasAssSubtitle = z;
        } else {
            this.mHasAssSubtitle = IMediaPlayer.MEDIA_MIMETYPE_TEXT_ASS.equals(mimeType);
        }
        if (!availableMimeTypeForExternalSource(mimeType)) {
            throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
        } else if (mVivoSubtitleSupport) {
            try {
                this.mMediaPlayer.addTimedTextSource(fd, offset, length, mimeType);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                throw e;
            }
        } else {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInterfaceToken(IMEDIA_PLAYER);
                request.writeInt(3);
                request.writeFileDescriptor(fd);
                request.writeLong(offset);
                request.writeLong(length);
                request.writeString(mimeType);
                this.mMediaPlayer.invoke(request, reply);
            } finally {
                request.recycle();
                reply.recycle();
            }
        }
    }

    public void selectTrack(int index) throws IllegalStateException {
        Log.v(TAG, "selectTrack index " + index);
        try {
            this.mMediaPlayer.selectTrack(index);
        } catch (RuntimeException e) {
            Log.e(TAG, "selectTrack failed ", e);
        }
    }

    public void deselectTrack(int index) throws IllegalStateException {
        Log.v(TAG, "deselectTrack index " + index);
        this.mMediaPlayer.deselectTrack(index);
    }

    public void setOnPreparedListener(final OnPreparedListener listener) {
        this.mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                listener.onPrepared(this);
            }
        });
    }

    public void setOnCompletionListener(final OnCompletionListener listener) {
        this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                listener.onCompletion(this);
            }
        });
    }

    public void setOnBufferingUpdateListener(final OnBufferingUpdateListener listener) {
        this.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                listener.onBufferingUpdate(this, percent);
            }
        });
    }

    public void setOnSeekCompleteListener(final OnSeekCompleteListener listener) {
        this.mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            public void onSeekComplete(MediaPlayer arg0) {
                listener.onSeekComplete(this);
            }
        });
    }

    public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
        this.mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
                listener.onVideoSizeChanged(this, arg1, arg2);
            }
        });
    }

    public void setOnTimedTextListener(final OnTimedTextListener listener) {
        this.mMediaPlayer.setOnTimedTextListener(new MediaPlayer.OnTimedTextListener() {
            public void onTimedText(MediaPlayer arg0, TimedText arg1) {
                TimedText t = arg1;
                if (NaiveMediaPlayer.this.mHasAssSubtitle && arg1 != null) {
                    if (NaiveMediaPlayer.this.mAssTagPattern == null || NaiveMediaPlayer.this.mAssDrawingPattern == null) {
                        Log.v(NaiveMediaPlayer.TAG, "new regex pattern");
                        NaiveMediaPlayer.this.mAssTagPattern = Pattern.compile("\\{.+?\\}");
                        NaiveMediaPlayer.this.mAssDrawingPattern = Pattern.compile("\\{.*\\\\p[1-9]\\\\");
                    }
                    String text = arg1.getText();
                    if (text != null) {
                        if (NaiveMediaPlayer.this.mAssDrawingPattern.matcher(text).find()) {
                            Log.v(NaiveMediaPlayer.TAG, "ignore ASS graphic drawing commands");
                            return;
                        }
                        text = NaiveMediaPlayer.this.mAssTagPattern.matcher(text).replaceAll("").replace("\\n", "\n").replace("\\N", "\n").replace("\\h", " ");
                        Parcel p = Parcel.obtain();
                        p.writeInt(102);
                        p.writeInt(7);
                        p.writeInt(0);
                        p.writeInt(16);
                        p.writeInt(text.length());
                        p.writeByteArray(text.getBytes());
                        t = new TimedText(p);
                    }
                }
                if (!NaiveMediaPlayer.mVivoSubtitleSupport) {
                    NaiveMediaPlayer.this.mSubtitleState.push(t);
                    if (t == null && (NaiveMediaPlayer.this.mSubtitleState.ok() ^ 1) != 0) {
                        Log.v(NaiveMediaPlayer.TAG, "ignore this null subtitle " + NaiveMediaPlayer.this.mSubtitleState.size());
                        return;
                    }
                }
                listener.onTimedText(this, t);
            }
        });
    }

    public void setOnErrorListener(final OnErrorListener listener) {
        this.mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                return listener.onError(this, arg1, arg2);
            }
        });
    }

    public void setOnInfoListener(final OnInfoListener listener) {
        this.mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
                return listener.onInfo(this, arg1, arg2);
            }
        });
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        return this.mMediaPlayer.getMetadata(update_only, apply_filter);
    }
}
