package com.vivo.mediaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Metadata;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.vivo.mediaplayer.IMediaPlayer.OnBufferingUpdateListener;
import com.vivo.mediaplayer.IMediaPlayer.OnCompletionListener;
import com.vivo.mediaplayer.IMediaPlayer.OnErrorListener;
import com.vivo.mediaplayer.IMediaPlayer.OnInfoListener;
import com.vivo.mediaplayer.IMediaPlayer.OnPreparedListener;
import com.vivo.mediaplayer.IMediaPlayer.OnSeekCompleteListener;
import com.vivo.mediaplayer.IMediaPlayer.OnTimedTextListener;
import com.vivo.mediaplayer.IMediaPlayer.OnVideoSizeChangedListener;
import com.vivo.mediaplayer.IMediaPlayer.TrackInfo;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

class MarsMediaPlayer implements IMediaPlayer {
    private static final String TAG = "MarsMediaPlayer";
    private NaiveMediaPlayer mMediaPlayer = null;

    public static native byte[] getBufferFromParcel(Parcel parcel);

    public static native byte[] getBufferFromParcelUseCrop(Parcel parcel);

    private MarsMediaPlayer() {
    }

    public MarsMediaPlayer(NaiveMediaPlayer mp) {
        Log.d(TAG, "===================================== new MarsMediaPlayer");
        this.mMediaPlayer = mp;
    }

    public Bitmap captureCurrentFrame() {
        return this.mMediaPlayer.captureCurrentFrame();
    }

    public int turnOnHqv(int percent) {
        return this.mMediaPlayer.turnOnHqv(percent);
    }

    public boolean isHardwareDecoder() {
        return this.mMediaPlayer.isHardwareDecoder();
    }

    public Parcel newRequest() {
        return this.mMediaPlayer.newRequest();
    }

    public void invoke(Parcel request, Parcel reply) {
        this.mMediaPlayer.invoke(request, reply);
    }

    @Deprecated
    public void setDisplay(SurfaceView view) {
        this.mMediaPlayer.setDisplay(view.getHolder());
    }

    public void setDisplay(SurfaceHolder sh) {
        this.mMediaPlayer.setDisplay(sh);
    }

    public void setSurface(Surface surface) {
        this.mMediaPlayer.setSurface(surface);
    }

    public void setVideoScalingMode(int mode) {
        this.mMediaPlayer.setVideoScalingMode(mode);
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(context, uri);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(context, uri, (Map) headers);
    }

    public void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(path);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(fd);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.setDataSource(fd, offset, length);
    }

    public void prepare() throws IOException, IllegalStateException {
        this.mMediaPlayer.prepare();
    }

    public void prepareAsync() throws IllegalStateException {
        this.mMediaPlayer.prepareAsync();
    }

    public void start() throws IllegalStateException {
        this.mMediaPlayer.start();
    }

    public void stop() throws IllegalStateException {
        this.mMediaPlayer.stop();
    }

    public void pause() throws IllegalStateException {
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
        this.mMediaPlayer.seekTo(msec);
    }

    public int getCurrentPosition() {
        return this.mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return this.mMediaPlayer.getDuration();
    }

    public void setNextMediaPlayer(IMediaPlayer next) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void release() {
        this.mMediaPlayer.release();
    }

    public void reset() {
        this.mMediaPlayer.reset();
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
        return this.mMediaPlayer.getTrackInfo();
    }

    public void addTimedTextSource(String path, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.getMediaPlayer().addTimedTextSource(path, mimeType);
    }

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IllegalArgumentException, IllegalStateException, IOException {
        this.mMediaPlayer.getMediaPlayer().addTimedTextSource(context, uri, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) {
        this.mMediaPlayer.getMediaPlayer().addTimedTextSource(fd, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mimeType) {
        this.mMediaPlayer.getMediaPlayer().addTimedTextSource(fd, offset, length, mimeType);
    }

    public void selectTrack(int index) throws IllegalStateException {
        this.mMediaPlayer.selectTrack(index);
    }

    public void deselectTrack(int index) throws IllegalStateException {
        this.mMediaPlayer.deselectTrack(index);
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mMediaPlayer.setOnPreparedListener(listener);
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mMediaPlayer.setOnCompletionListener(listener);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mMediaPlayer.setOnBufferingUpdateListener(listener);
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mMediaPlayer.setOnSeekCompleteListener(listener);
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mMediaPlayer.setOnVideoSizeChangedListener(listener);
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mMediaPlayer.setOnTimedTextListener(listener);
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mMediaPlayer.setOnErrorListener(listener);
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mMediaPlayer.setOnInfoListener(listener);
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        return this.mMediaPlayer.getMetadata(update_only, apply_filter);
    }
}
