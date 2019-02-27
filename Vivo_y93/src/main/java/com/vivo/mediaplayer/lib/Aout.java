package com.vivo.mediaplayer.lib;

import android.media.AudioTrack;
import android.util.Log;

public class Aout {
    private static int AUDIO_SESSION_ID = 9999;
    private static final String TAG = "VMediaPlayer/aout";
    private AudioTrack mAudioTrack;

    public void init(int sampleRateInHz, int channels, int samples, int streamtype) {
        Log.d(TAG, sampleRateInHz + ", " + channels + ", " + samples + "=>" + (channels * samples));
        this.mAudioTrack = new AudioTrack(streamtype, sampleRateInHz, 12, 2, Math.max(AudioTrack.getMinBufferSize(sampleRateInHz, 12, 2), (channels * samples) * 2), 1, AUDIO_SESSION_ID);
    }

    public void release() {
        if (this.mAudioTrack != null) {
            this.mAudioTrack.release();
        }
        this.mAudioTrack = null;
    }

    public int getAudioSessionId() {
        if (this.mAudioTrack != null) {
            return this.mAudioTrack.getAudioSessionId();
        }
        return AUDIO_SESSION_ID;
    }

    public void playBuffer(byte[] audioData, int bufferSize) {
        if (this.mAudioTrack.getState() != 0) {
            if (this.mAudioTrack.write(audioData, 0, bufferSize) != bufferSize) {
                Log.w(TAG, "Could not write all the samples to the audio device");
            }
            this.mAudioTrack.play();
        }
    }

    public void pause() {
        this.mAudioTrack.pause();
    }
}
