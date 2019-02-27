package android.preference;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.VolumePreference.VolumeStore;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.internal.annotations.GuardedBy;

public class SeekBarVolumizer implements OnSeekBarChangeListener, Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String TAG = "SeekBarVolumizer";
    private boolean mAffectedByRingerMode;
    private final AudioManager mAudioManager;
    private final Callback mCallback;
    private final Context mContext;
    private final Uri mDefaultUri;
    private Handler mHandler;
    private int mLastAudibleStreamVolume;
    private int mLastProgress = -1;
    private final int mMaxStreamVolume;
    private boolean mMuted;
    private final NotificationManager mNotificationManager;
    private boolean mNotificationOrRing;
    private int mOriginalStreamVolume;
    private final Receiver mReceiver = new Receiver(this, null);
    private int mRingerMode;
    @GuardedBy("this")
    private Ringtone mRingtone;
    private SeekBar mSeekBar;
    private final int mStreamType;
    private final H mUiHandler = new H(this, null);
    private int mVolumeBeforeMute = -1;
    private Observer mVolumeObserver;
    private int mZenMode;

    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mStreamType = streamType;
        this.mAffectedByRingerMode = this.mAudioManager.isStreamAffectedByRingerMode(this.mStreamType);
        this.mNotificationOrRing = isNotificationOrRing(this.mStreamType);
        if (this.mNotificationOrRing) {
            this.mRingerMode = this.mAudioManager.getRingerModeInternal();
        }
        this.mZenMode = this.mNotificationManager.getZenMode();
        this.mMaxStreamVolume = this.mAudioManager.getStreamMaxVolume(this.mStreamType);
        this.mCallback = callback;
        this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
        this.mLastAudibleStreamVolume = this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType);
        this.mMuted = this.mAudioManager.isStreamMute(this.mStreamType);
        if (this.mCallback != null) {
            this.mCallback.onMuted(this.mMuted, isZenMuted());
        }
        if (defaultUri == null) {
            if (this.mStreamType == 2) {
                defaultUri = System.DEFAULT_RINGTONE_URI;
            } else if (this.mStreamType == 5) {
                defaultUri = System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
    }

    private static boolean isNotificationOrRing(int stream) {
        return stream == 2 || stream == 5;
    }

    public void setSeekBar(SeekBar seekBar) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = seekBar;
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mSeekBar.setMax(this.mMaxStreamVolume);
        updateSeekBar();
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    private boolean isZenMuted() {
        if ((this.mNotificationOrRing && this.mZenMode == 3) || this.mZenMode == 2) {
            return true;
        }
        return false;
    }

    protected void updateSeekBar() {
        boolean zenMuted = isZenMuted();
        this.mSeekBar.setEnabled(zenMuted ^ 1);
        if (zenMuted) {
            this.mSeekBar.setProgress(this.mLastAudibleStreamVolume, true);
        } else if (this.mNotificationOrRing && this.mRingerMode == 1) {
            this.mSeekBar.setProgress(0, true);
        } else if (this.mMuted) {
            this.mSeekBar.setProgress(0, true);
        } else {
            this.mSeekBar.setProgress(this.mLastProgress > -1 ? this.mLastProgress : this.mOriginalStreamVolume, true);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (this.mMuted && this.mLastProgress > 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, 100, 0);
                } else if (!this.mMuted && this.mLastProgress == 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, -100, 0);
                }
                this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 1024);
                break;
            case 1:
                onStartSample();
                break;
            case 2:
                onStopSample();
                break;
            case 3:
                onInitSample();
                break;
            default:
                Log.e(TAG, "invalid SeekBarVolumizer message: " + msg.what);
                break;
        }
        return true;
    }

    private void onInitSample() {
        synchronized (this) {
            this.mRingtone = RingtoneManager.getRingtone(this.mContext, this.mDefaultUri);
            if (this.mRingtone != null) {
                this.mRingtone.setStreamType(this.mStreamType);
            }
        }
    }

    private void postStartSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), (long) (isSamplePlaying() ? 1000 : 0));
        }
    }

    private void onStartSample() {
        if (!isSamplePlaying()) {
            if (this.mCallback != null) {
                this.mCallback.onSampleStarting(this);
            }
            synchronized (this) {
                if (this.mRingtone != null) {
                    try {
                        this.mRingtone.setAudioAttributes(new Builder(this.mRingtone.getAudioAttributes()).setFlags(128).build());
                        this.mRingtone.play();
                    } catch (Throwable e) {
                        Log.w(TAG, "Error playing ringtone, stream " + this.mStreamType, e);
                    }
                }
            }
            return;
        }
        return;
    }

    private void postStopSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        }
    }

    private void onStopSample() {
        synchronized (this) {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
        }
    }

    public void stop() {
        if (this.mHandler != null) {
            postStopSample();
            this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
            this.mReceiver.setListening(false);
            this.mSeekBar.setOnSeekBarChangeListener(null);
            this.mHandler.getLooper().quitSafely();
            this.mHandler = null;
            this.mVolumeObserver = null;
        }
    }

    public void start() {
        if (this.mHandler == null) {
            HandlerThread thread = new HandlerThread("SeekBarVolumizer.CallbackHandler");
            thread.start();
            this.mHandler = new Handler(thread.getLooper(), this);
            this.mHandler.sendEmptyMessage(3);
            this.mVolumeObserver = new Observer(this, this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
            this.mReceiver.setListening(true);
        }
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            postSetVolume(progress);
        }
        if (this.mCallback != null) {
            this.mCallback.onProgressChanged(seekBar, progress, fromTouch);
        }
    }

    private void postSetVolume(int progress) {
        if (this.mHandler != null) {
            this.mLastProgress = progress;
            this.mHandler.removeMessages(0);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        postStartSample();
    }

    public boolean isSamplePlaying() {
        boolean isPlaying;
        synchronized (this) {
            isPlaying = this.mRingtone != null ? this.mRingtone.isPlaying() : false;
        }
        return isPlaying;
    }

    public void startSample() {
        postStartSample();
    }

    public void stopSample() {
        postStopSample();
    }

    public SeekBar getSeekBar() {
        return this.mSeekBar;
    }

    public void changeVolumeBy(int amount) {
        this.mSeekBar.incrementProgressBy(amount);
        postSetVolume(this.mSeekBar.getProgress());
        postStartSample();
        this.mVolumeBeforeMute = -1;
    }

    public void muteVolume() {
        if (this.mVolumeBeforeMute != -1) {
            this.mSeekBar.setProgress(this.mVolumeBeforeMute, true);
            postSetVolume(this.mVolumeBeforeMute);
            postStartSample();
            this.mVolumeBeforeMute = -1;
            return;
        }
        this.mVolumeBeforeMute = this.mSeekBar.getProgress();
        this.mSeekBar.setProgress(0, true);
        postStopSample();
        postSetVolume(0);
    }

    public void onSaveInstanceState(VolumeStore volumeStore) {
        if (this.mLastProgress >= 0) {
            volumeStore.volume = this.mLastProgress;
            volumeStore.originalVolume = this.mOriginalStreamVolume;
        }
    }

    public void onRestoreInstanceState(VolumeStore volumeStore) {
        if (volumeStore.volume != -1) {
            this.mOriginalStreamVolume = volumeStore.originalVolume;
            this.mLastProgress = volumeStore.volume;
            postSetVolume(this.mLastProgress);
        }
    }

    private void updateSlider() {
        if (this.mSeekBar != null && this.mAudioManager != null) {
            this.mUiHandler.postUpdateSlider(this.mAudioManager.getStreamVolume(this.mStreamType), this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType), this.mAudioManager.isStreamMute(this.mStreamType));
        }
    }
}
