package com.vivo.mediaplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaFile;
import android.media.Metadata;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import com.vivo.mediaplayer.lib.EventHandler;
import com.vivo.mediaplayer.lib.IVideoPlayer;
import com.vivo.mediaplayer.lib.LibVLC;
import com.vivo.mediaplayer.lib.LibVlcException;
import com.vivo.mediaplayer.lib.WeakHandler;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

class VlcMediaPlayer implements IMediaPlayer, IVideoPlayer {
    private static final String TAG = "VMediaPlayer";
    private String mAudioCodec = "NONE";
    private int mAudioTrackIndex = -1;
    private long mAverageBitrate = 0;
    private int mCurrentPos = 0;
    private int mDuration = 0;
    private VlcEventHandler mEventHandler;
    private int mHeight = 0;
    private boolean mIsEosReached = false;
    private boolean mIsOnVideoSizeChangedCalled = false;
    private boolean mIsPaused = true;
    private boolean mIsReleased = false;
    private boolean mIsSeekable = true;
    private LibVLC mLibVLC = null;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private String mPath = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    private SurfaceHolder mSurfaceHolder;
    private ArrayList<TrackInfo> mTracks = new ArrayList();
    private String mVideoCodec = "NONE";
    private String mVideoFilePath = null;
    private final Handler mVlcEventHandler = new VideoPlayerEventHandler(this);
    private WakeLock mWakeLock = null;
    private int mWidth = 0;

    private static class HandlerMessage {
        private static final int VLC_DO_PREPARE_ASYNC = 1000;
        private static final int VLC_DO_SEEK = 1001;
        private static final int VLC_ERROR_BITRATE_TOO_HIGH = 2000;
        private static final int VLC_ON_VIDEO_SIZE_CHANGED = 3000;

        private HandlerMessage() {
        }
    }

    private class VideoPlayerEventHandler extends WeakHandler<VlcMediaPlayer> {
        public VideoPlayerEventHandler(VlcMediaPlayer owner) {
            super(owner);
        }

        public void handleMessage(Message msg) {
            VlcMediaPlayer activity = (VlcMediaPlayer) getOwner();
            if (activity != null) {
                switch (msg.getData().getInt("event")) {
                    case EventHandler.MediaPlayerPlaying /*260*/:
                        if (VlcMediaPlayer.this.mAudioTrackIndex >= 0) {
                            Log.v(VlcMediaPlayer.TAG, "selected audio track " + VlcMediaPlayer.this.mAudioTrackIndex);
                            VlcMediaPlayer.this.mLibVLC.setAudioTrack(VlcMediaPlayer.this.mAudioTrackIndex);
                            VlcMediaPlayer.this.mAudioTrackIndex = -1;
                            break;
                        }
                        break;
                    case EventHandler.MediaPlayerPaused /*261*/:
                    case EventHandler.MediaPlayerPositionChanged /*268*/:
                    case EventHandler.MediaPlayerVout /*274*/:
                    case EventHandler.MediaListItemAdded /*512*/:
                        break;
                    case EventHandler.MediaPlayerStopped /*262*/:
                        Log.d(VlcMediaPlayer.TAG, "Event MediaPlayerStopped");
                        break;
                    case EventHandler.MediaPlayerEndReached /*265*/:
                        Log.d(VlcMediaPlayer.TAG, "onCompletion");
                        VlcMediaPlayer.this.mIsEosReached = true;
                        if (VlcMediaPlayer.this.mOnCompletionListener != null) {
                            VlcMediaPlayer.this.mOnCompletionListener.onCompletion(activity);
                        }
                        VlcMediaPlayer.this.stayAwake(false);
                        break;
                    case EventHandler.MediaPlayerEncounteredError /*266*/:
                        Log.d(VlcMediaPlayer.TAG, "onError");
                        if (VlcMediaPlayer.this.mOnErrorListener != null) {
                            VlcMediaPlayer.this.mOnErrorListener.onError(activity, 1, 0);
                            break;
                        }
                        break;
                    default:
                        Log.d(VlcMediaPlayer.TAG, String.format("Event not handled (0x%x)", new Object[]{Integer.valueOf(msg.getData().getInt("event"))}));
                        break;
                }
            }
        }
    }

    private class VlcEventHandler extends Handler {
        private VlcMediaPlayer mMediaPlayer;

        public VlcEventHandler(VlcMediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    if (VlcMediaPlayer.this.mLibVLC != null) {
                        VlcMediaPlayer.this.doPrepare();
                        if (VlcMediaPlayer.this.canPlay()) {
                            VlcMediaPlayer.this.stayAwake(true);
                            if (VlcMediaPlayer.this.mPath != null) {
                                VlcMediaPlayer.this.mLibVLC.readMedia(VlcMediaPlayer.this.mPath, false);
                                VlcMediaPlayer.this.mIsPaused = false;
                            }
                        } else {
                            if (VlcMediaPlayer.this.mEventHandler != null) {
                                VlcMediaPlayer.this.mEventHandler.sendMessage(VlcMediaPlayer.this.mEventHandler.obtainMessage(2000));
                            }
                            return;
                        }
                    }
                    if (VlcMediaPlayer.this.mOnVideoSizeChangedListener != null) {
                        VlcMediaPlayer.this.mOnVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, VlcMediaPlayer.this.mWidth, VlcMediaPlayer.this.mHeight);
                    }
                    if (VlcMediaPlayer.this.mOnPreparedListener != null) {
                        VlcMediaPlayer.this.mOnPreparedListener.onPrepared(this.mMediaPlayer);
                        break;
                    }
                    break;
                case 1001:
                    if (VlcMediaPlayer.this.mOnSeekCompleteListener != null) {
                        VlcMediaPlayer.this.mOnSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                        break;
                    }
                    break;
                case 2000:
                    if (VlcMediaPlayer.this.mOnErrorListener != null) {
                        VlcMediaPlayer.this.mOnErrorListener.onError(this.mMediaPlayer, IMediaPlayer.MEDIA_ERROR_UNSUPPORTED, 0);
                        break;
                    }
                    break;
                case 3000:
                    Log.v(VlcMediaPlayer.TAG, "onVideoSizeChanged");
                    if (VlcMediaPlayer.this.canPlay() || VlcMediaPlayer.this.mOnErrorListener == null) {
                        if (VlcMediaPlayer.this.mOnVideoSizeChangedListener != null) {
                            VlcMediaPlayer.this.mOnVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                            break;
                        }
                    }
                    VlcMediaPlayer.this.mOnErrorListener.onError(this.mMediaPlayer, IMediaPlayer.MEDIA_ERROR_UNSUPPORTED, 0);
                    VlcMediaPlayer.this.stop();
                    return;
                    break;
            }
        }
    }

    public VlcMediaPlayer() {
        Log.v(TAG, "=========== new VMediaPlayer");
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new VlcEventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new VlcEventHandler(this, looper);
            } else {
                Log.w(TAG, "no EventHandler is created");
                this.mEventHandler = null;
            }
        }
        try {
            this.mLibVLC = LibVLC.getPlayerInstance();
            EventHandler.getInstance().addHandler(this.mVlcEventHandler);
        } catch (LibVlcException e) {
            Log.e(TAG, "Lib initialisation failed");
        }
    }

    public Parcel newRequest() {
        Log.w(TAG, "method not implemented");
        return null;
    }

    public void invoke(Parcel request, Parcel reply) {
        Log.w(TAG, "method not implemented");
    }

    public Bitmap captureCurrentFrame() {
        Log.v(TAG, "method not implemented");
        return null;
    }

    public int turnOnHqv(int percent) {
        Log.v(TAG, "turnOnHqv method not implemented");
        return 0;
    }

    public boolean isHardwareDecoder() {
        return false;
    }

    @Deprecated
    public void setDisplay(SurfaceView view) {
        this.mSurfaceHolder = view.getHolder();
        if (this.mSurfaceHolder != null) {
            this.mLibVLC.attachSurface(this.mSurfaceHolder.getSurface(), this);
        }
        updateSurfaceScreenOn();
    }

    public void setDisplay(SurfaceHolder sh) {
        this.mSurfaceHolder = sh;
        if (this.mSurfaceHolder != null) {
            this.mLibVLC.attachSurface(this.mSurfaceHolder.getSurface(), this);
        }
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        SurfaceHolder holder = this.mSurfaceHolder;
        if (holder != null) {
            holder.setKeepScreenOn(this.mScreenOnWhilePlaying ? this.mStayAwake : false);
        }
    }

    public void setSurface(Surface surface) {
        if (this.mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        this.mSurfaceHolder = null;
        this.mLibVLC.attachSurface(surface, this);
    }

    public void setVideoScalingMode(int mode) {
        throw new UnsupportedOperationException("method not implemented");
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        Log.v(TAG, "setDataSource, uri = " + uri);
        String scheme = uri.getScheme();
        String path;
        if ("content".equals(scheme)) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(0);
                    if (path == null) {
                        throw new IllegalArgumentException("can not get file path from uri " + uri);
                    }
                    this.mVideoFilePath = path;
                    this.mPath = LibVLC.PathToURI(path);
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (this.mVideoFilePath != null) {
                    return;
                }
                if (this.mOnErrorListener != null) {
                    Log.e(TAG, "failed to get file path from uri " + uri);
                    this.mOnErrorListener.onError(this, IMediaPlayer.MEDIA_ERROR_IO, 0);
                    return;
                }
                throw new IllegalArgumentException("can not get file path from uri " + uri);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                if (this.mVideoFilePath == null) {
                    if (this.mOnErrorListener != null) {
                        Log.e(TAG, "failed to get file path from uri " + uri);
                        this.mOnErrorListener.onError(this, IMediaPlayer.MEDIA_ERROR_IO, 0);
                    } else {
                        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("can not get file path from uri " + uri);
                    }
                }
            }
        } else if ("file".equals(scheme)) {
            path = uri.getPath();
            if (MediaFile.isVideoFileType(MediaFile.getFileTypeForMimeType(MediaFile.getMimeTypeForFile(path))) || (MediaFile.isAudioFileType(MediaFile.getFileTypeForMimeType(MediaFile.getMimeTypeForFile(path))) ^ 1) == 0) {
                setDataSource(path);
                return;
            }
            throw new IllegalArgumentException("this file is neither video file nor audio file");
        } else if ("http".equals(scheme) || "rtsp".equals(scheme) || "mms".equals(scheme)) {
            this.mPath = uri.toString();
        }
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> map) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        setDataSource(context, uri);
    }

    public void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        Log.v(TAG, "setDataSource, path = " + path);
        if (path == null) {
            throw new IllegalArgumentException("path can not be null");
        }
        this.mVideoFilePath = path;
        this.mPath = LibVLC.PathToURI(path);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException, IllegalStateException, IOException {
        throw new UnsupportedOperationException("method not implemented");
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IllegalArgumentException, IllegalStateException, IOException {
        throw new UnsupportedOperationException("method not implemented");
    }

    public void prepare() throws IOException, IllegalStateException {
        Log.v(TAG, "prepare");
        doPrepare();
        if (canPlay()) {
            stayAwake(true);
            if (this.mPath != null) {
                this.mLibVLC.readMedia(this.mPath, false);
                this.mIsPaused = false;
            }
            if (this.mOnVideoSizeChangedListener != null) {
                this.mOnVideoSizeChangedListener.onVideoSizeChanged(this, this.mWidth, this.mHeight);
            }
            return;
        }
        if (this.mEventHandler != null) {
            this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(2000));
        }
    }

    public void prepareAsync() throws IllegalStateException {
        Log.v(TAG, "prepareAsync");
        if (this.mEventHandler != null) {
            this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1000));
        }
    }

    public void setSurfaceSize(int videoWidth, int videoHeight, int sourceAspectRatioNumber, int sourceAspectRatioDensity) {
        double visualWidth;
        Log.v(TAG, "setSurfaceSize from native res " + videoWidth + "X" + videoHeight + ", sarNum " + sourceAspectRatioNumber + ", sarDen " + sourceAspectRatioDensity);
        double density = ((double) sourceAspectRatioNumber) / ((double) sourceAspectRatioDensity);
        double displayAspectRatio;
        if (density == 1.0d) {
            visualWidth = (double) videoWidth;
            displayAspectRatio = ((double) videoWidth) / ((double) videoHeight);
        } else {
            visualWidth = ((double) videoWidth) * density;
            displayAspectRatio = visualWidth / ((double) videoHeight);
        }
        if (this.mWidth != videoWidth || this.mHeight != videoHeight) {
            this.mWidth = videoWidth;
            this.mHeight = videoHeight;
            if (!this.mIsOnVideoSizeChangedCalled && this.mEventHandler != null) {
                this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(3000, (int) visualWidth, videoHeight));
                this.mIsOnVideoSizeChangedCalled = true;
            }
        }
    }

    public void start() throws IllegalStateException {
        Log.v(TAG, FeatureService.TAG_FACE_START);
        stayAwake(true);
        if (this.mIsEosReached) {
            Log.i(TAG, "EOS has been reached, reinit player");
            this.mIsEosReached = false;
            this.mLibVLC.readMedia(this.mPath, false);
        }
        this.mLibVLC.play();
        this.mIsPaused = false;
    }

    public void stop() throws IllegalStateException {
        Log.v(TAG, FeatureService.TAG_FACE_STOP);
        if (this.mIsReleased) {
            Log.w(TAG, "Player is already released");
            return;
        }
        stayAwake(false);
        this.mLibVLC.stop();
        this.mIsPaused = true;
    }

    public void pause() throws IllegalStateException {
        Log.v(TAG, "pause");
        stayAwake(false);
        if (!this.mLibVLC.isPlaying() || (this.mIsPaused ^ 1) == 0) {
            Log.v(TAG, "---------------- already pused, ignore");
            return;
        }
        this.mLibVLC.pause();
        this.mIsPaused = true;
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (this.mWakeLock != null) {
            if (this.mWakeLock.isHeld()) {
                washeld = true;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(536870912 | mode, VlcMediaPlayer.class.getName());
        this.mWakeLock.setReferenceCounted(false);
        if (washeld) {
            this.mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        SurfaceHolder holder = this.mSurfaceHolder;
        if (this.mScreenOnWhilePlaying != screenOn) {
            if (screenOn && holder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        if (this.mWakeLock != null) {
            if (awake && (this.mWakeLock.isHeld() ^ 1) != 0) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    public int getVideoWidth() {
        return this.mWidth;
    }

    public int getVideoHeight() {
        return this.mHeight;
    }

    public boolean isPlaying() {
        return this.mLibVLC.isPlaying();
    }

    public void seekTo(int msec) throws IllegalStateException {
        Log.v(TAG, "seek to " + msec + " ms");
        if (this.mLibVLC == null || this.mDuration <= 0 || !this.mIsSeekable) {
            Log.i(TAG, "this file does not support seek");
        } else {
            this.mCurrentPos = msec;
            this.mLibVLC.setTime((long) msec);
        }
        if (this.mEventHandler != null) {
            this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1001, msec, 0));
        }
    }

    public int getCurrentPosition() {
        float pos = (float) this.mLibVLC.getTime();
        if (pos == -1.0f) {
            Log.e(TAG, "getCurrentPosision err, can not get pos");
        } else if (pos == 0.0f) {
            Log.i(TAG, "getCurrentPosision maybe not ready");
            return this.mCurrentPos;
        }
        if (this.mDuration > 0 && pos > ((float) this.mDuration)) {
            pos = (float) this.mDuration;
            Log.w(TAG, "getCurrentPosision err, pos larger than duration");
        }
        this.mCurrentPos = Float.valueOf(pos).intValue();
        return this.mCurrentPos;
    }

    public int getDuration() {
        if (this.mDuration != 0) {
            return this.mDuration;
        }
        long duration = this.mLibVLC.getLength();
        if (duration <= 0) {
            Log.e(TAG, "getCurrentPosision err");
        }
        Log.v(TAG, "getDuration is " + duration);
        this.mDuration = (int) duration;
        return this.mDuration;
    }

    public void setNextMediaPlayer(IMediaPlayer next) {
        throw new UnsupportedOperationException("setNextMediaPlayer method not implemented");
    }

    public void release() {
        Log.v(TAG, "release");
        this.mIsReleased = true;
        stayAwake(false);
        updateSurfaceScreenOn();
        this.mLibVLC.stop();
        this.mLibVLC.detachSurface();
        this.mLibVLC.destroy();
        this.mLibVLC = null;
        this.mEventHandler.removeCallbacksAndMessages(null);
        this.mVlcEventHandler.removeCallbacksAndMessages(null);
        EventHandler.getInstance().removeHandler(this.mVlcEventHandler);
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnTimedTextListener = null;
        this.mCurrentPos = 0;
        this.mDuration = 0;
        this.mHeight = 0;
        this.mWidth = 0;
        this.mPath = null;
        this.mVideoFilePath = null;
        this.mWakeLock = null;
        this.mVideoCodec = "NONE";
        this.mAudioCodec = "NONE";
        this.mAverageBitrate = 0;
        this.mIsOnVideoSizeChangedCalled = false;
        this.mIsPaused = true;
        this.mIsSeekable = true;
        this.mTracks.clear();
    }

    public void reset() {
        Log.v(TAG, "reset");
        stayAwake(false);
        this.mLibVLC.stop();
        this.mEventHandler.removeCallbacksAndMessages(null);
        this.mVlcEventHandler.removeCallbacksAndMessages(null);
        this.mCurrentPos = 0;
        this.mDuration = 0;
        this.mHeight = 0;
        this.mWidth = 0;
        this.mPath = null;
        this.mVideoFilePath = null;
        this.mVideoCodec = "NONE";
        this.mAudioCodec = "NONE";
        this.mAverageBitrate = 0;
        this.mIsOnVideoSizeChangedCalled = false;
        this.mIsPaused = true;
        this.mIsSeekable = true;
        this.mIsReleased = false;
        this.mTracks.clear();
    }

    public void setAudioStreamType(int streamtype) {
        this.mLibVLC.setAudioStreamType(streamtype);
    }

    public void setLooping(boolean looping) {
        throw new UnsupportedOperationException("setLooping method not implemented");
    }

    public boolean isLooping() {
        throw new UnsupportedOperationException("isLooping method not implemented");
    }

    public void setVolume(float leftVolume, float rightVolume) {
        throw new UnsupportedOperationException("setVolume method not implemented");
    }

    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException("setAudioSessionId method not implemented");
    }

    public int getAudioSessionId() {
        return this.mLibVLC.getAudioSessionId();
    }

    public void attachAuxEffect(int effectId) {
        throw new UnsupportedOperationException("attachAuxEffect method not implemented");
    }

    public void setAuxEffectSendLevel(float level) {
        throw new UnsupportedOperationException("setAuxEffectSendLevel method not implemented");
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        Log.d(TAG, "getTrackInfo " + this.mTracks);
        return (TrackInfo[]) this.mTracks.toArray(new TrackInfo[5]);
    }

    public void addTimedTextSource(String path, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        Log.e(TAG, "addTimedTextSource1 method not implemented");
    }

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        Log.e(TAG, "addTimedTextSource2 method not implemented");
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) {
        Log.e(TAG, "addTimedTextSource3 method not implemented");
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mimeType) {
        Log.e(TAG, "addTimedTextSource4 method not implemented");
    }

    public void selectTrack(int index) throws IllegalStateException {
        if (index > this.mTracks.size() - 1) {
            Log.e(TAG, "track index out of range, index " + index + ", track cout " + this.mTracks.size());
            throw new IllegalStateException("index out of range");
        }
        TrackInfo track = (TrackInfo) this.mTracks.get(index);
        Log.d(TAG, "selectTrack index = " + index + ", track " + track);
        if (track.getTrackType() == 2 && isPlaying()) {
            this.mLibVLC.setAudioTrack(index);
            return;
        }
        Log.i(TAG, "play not started, select audio track later");
        this.mAudioTrackIndex = index;
    }

    public void deselectTrack(int index) throws IllegalStateException {
        Log.e(TAG, "deselectTrack method not implemented");
    }

    private void doPrepare() {
        if (this.mPath != null && !this.mPath.endsWith(".ts")) {
            this.mTracks.clear();
            for (com.vivo.mediaplayer.lib.TrackInfo i : this.mLibVLC.readTracksInfo(this.mPath)) {
                if (i.Type == 1) {
                    this.mWidth = i.Width;
                    this.mHeight = i.Height;
                    this.mVideoCodec = i.Codec;
                    this.mTracks.add(new TrackInfo(convertTrackType(i.Type), "English".equals(i.Language) ? "eng" : i.Language));
                } else if (i.Type == 3) {
                    this.mDuration = (int) i.Length;
                } else if (i.Type == 0) {
                    this.mAudioCodec = i.Codec;
                    this.mTracks.add(new TrackInfo(convertTrackType(i.Type), "English".equals(i.Language) ? "eng" : i.Language));
                }
            }
            if (this.mDuration > 0) {
                this.mAverageBitrate = (new File(this.mVideoFilePath).length() * 8) / ((long) this.mDuration);
            }
            dumpVideoInfo();
        }
    }

    private static int convertTrackType(int type) {
        switch (type) {
            case 0:
                return 2;
            case 1:
                return 1;
            case 2:
                return 3;
            default:
                return 0;
        }
    }

    private void dumpVideoInfo() {
        Log.v(TAG, "=================== Parsed Video Info ====================");
        Log.v(TAG, "* Video : " + this.mVideoFilePath);
        Log.v(TAG, "    * resolution: " + this.mWidth + "X" + this.mHeight);
        Log.v(TAG, "    * video codec: " + this.mVideoCodec);
        Log.v(TAG, "    * audio codec: " + this.mAudioCodec);
        Log.v(TAG, "    * duration " + this.mDuration + "ms");
        Log.v(TAG, "    * average bitrate " + this.mAverageBitrate + "kbps");
        Log.v(TAG, "=========================================================");
    }

    private boolean canPlay() {
        if (("MTK".equals(MediaPlayerUtils.SOLUTION) || "QCOM8916".equals(MediaPlayerUtils.PLATFORM) || "QCOM8939".equals(MediaPlayerUtils.PLATFORM)) && (this.mWidth > 1280 || this.mHeight > 720)) {
            Log.v(TAG, "not support play video files that mWidth > 1280 or mHeight > 720 in low tier devices");
            return false;
        }
        int mode = "".equals(MediaPlayerUtils.PLAYER_MODE) ? 0 : Integer.parseInt(MediaPlayerUtils.PLAYER_MODE);
        if (this.mPath == null || !this.mPath.toLowerCase().endsWith(".mp4") || !this.mVideoCodec.startsWith("H264") || !this.mAudioCodec.startsWith("MPEG AAC") || mode == 1) {
            return true;
        }
        Log.w(TAG, "do not play mp4 video files with H264 and AAC codecs");
        return false;
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        return null;
    }
}
