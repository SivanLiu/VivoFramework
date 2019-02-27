package com.vivo.mediaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Metadata;
import android.media.TimedText;
import android.net.Uri;
import android.os.Parcel;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public interface IMediaPlayer {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final String MEDIA_MIMETYPE_TEXT_ASS = "text/x-ass";
    public static final String MEDIA_MIMETYPE_TEXT_SUBASS = "application/x-subtitle-ass";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_SUBSSA = "application/x-subtitle-ssa";
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i);
    }

    public interface OnCompletionListener {
        void onCompletion(IMediaPlayer iMediaPlayer);
    }

    public interface OnErrorListener {
        boolean onError(IMediaPlayer iMediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i2);
    }

    public interface OnPreparedListener {
        void onPrepared(IMediaPlayer iMediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer iMediaPlayer);
    }

    public interface OnTimedTextListener {
        void onTimedText(IMediaPlayer iMediaPlayer, TimedText timedText);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i2);
    }

    public static class TrackInfo {
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        private static final String[] TRACK_TYPE_TABLE = new String[]{"UNKNOWN", "VIDEO", "AUDIO", "TEXT"};
        final String mLanguage;
        final int mTrackType;

        public TrackInfo(int trackType, String language) {
            if (trackType > 3 || trackType < 0) {
                trackType = 0;
            }
            this.mTrackType = trackType;
            this.mLanguage = language;
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            return this.mLanguage;
        }

        private static String getReadableTrackType(int type) {
            return TRACK_TYPE_TABLE[type];
        }

        public String toString() {
            return "TrackInfo {type <" + getReadableTrackType(this.mTrackType) + ">, language <" + this.mLanguage + ">}";
        }
    }

    void addTimedTextSource(Context context, Uri uri, String str) throws IllegalArgumentException, IllegalStateException, IOException;

    void addTimedTextSource(FileDescriptor fileDescriptor, long j, long j2, String str);

    void addTimedTextSource(FileDescriptor fileDescriptor, String str);

    void addTimedTextSource(String str, String str2) throws IllegalArgumentException, IllegalStateException, IOException;

    void attachAuxEffect(int i);

    Bitmap captureCurrentFrame();

    void deselectTrack(int i) throws IllegalStateException;

    int getAudioSessionId();

    int getCurrentPosition();

    int getDuration();

    Metadata getMetadata(boolean z, boolean z2);

    TrackInfo[] getTrackInfo() throws IllegalStateException;

    int getVideoHeight();

    int getVideoWidth();

    void invoke(Parcel parcel, Parcel parcel2);

    boolean isHardwareDecoder();

    boolean isLooping();

    boolean isPlaying();

    Parcel newRequest();

    void pause() throws IllegalStateException;

    void prepare() throws IOException, IllegalStateException;

    void prepareAsync() throws IllegalStateException;

    void release();

    void reset();

    void seekTo(int i) throws IllegalStateException;

    void selectTrack(int i) throws IllegalStateException;

    void setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    void setAudioStreamType(int i);

    void setAuxEffectSendLevel(float f);

    void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(Context context, Uri uri, Map<String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(FileDescriptor fileDescriptor) throws IllegalArgumentException, IllegalStateException, IOException;

    void setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    void setDataSource(String str) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDisplay(SurfaceHolder surfaceHolder);

    @Deprecated
    void setDisplay(SurfaceView surfaceView);

    void setLooping(boolean z);

    void setNextMediaPlayer(IMediaPlayer iMediaPlayer);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener);

    void setOnCompletionListener(OnCompletionListener onCompletionListener);

    void setOnErrorListener(OnErrorListener onErrorListener);

    void setOnInfoListener(OnInfoListener onInfoListener);

    void setOnPreparedListener(OnPreparedListener onPreparedListener);

    void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener);

    void setOnTimedTextListener(OnTimedTextListener onTimedTextListener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener);

    void setScreenOnWhilePlaying(boolean z);

    void setSurface(Surface surface);

    void setVideoScalingMode(int i);

    void setVolume(float f, float f2);

    void setWakeMode(Context context, int i);

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;

    int turnOnHqv(int i);
}
