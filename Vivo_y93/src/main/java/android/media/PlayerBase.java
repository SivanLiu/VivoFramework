package android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioFeatures.TagParameters;
import android.media.VolumeShaper.Configuration;
import android.media.VolumeShaper.Operation;
import android.media.VolumeShaper.State;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsCallback.Stub;
import com.android.internal.app.IAppOpsService;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class PlayerBase {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_APP_OPS = false;
    private static final String TAG = "PlayerBase";
    private static IAudioService sService;
    private IAppOpsService mAppOps;
    private IAppOpsCallback mAppOpsCallback;
    protected AudioAttributes mAttributes;
    AudioFeatures mAudioFeatures;
    protected float mAuxEffectSendLevel = 0.0f;
    Context mContext;
    private boolean mHasAppOpsPlayAudio = true;
    private final int mImplType;
    protected float mLeftVolume = 1.0f;
    private final Object mLock = new Object();
    private boolean mMotorModeFlag = false;
    private float mPanMultiplierL = 1.0f;
    private float mPanMultiplierR = 1.0f;
    private int mPlayerIId;
    public PlayerIdCard mPlayerIdCard;
    protected float mRightVolume = 1.0f;
    private int mStartCallBackState;
    private int mStartDelayMs = 0;
    private int mState;

    private static class IAppOpsCallbackWrapper extends Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IAppOpsCallbackWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference(pb);
        }

        public void opChanged(int op, int uid, String packageName) {
            if (op == 28) {
                PlayerBase pb = (PlayerBase) this.mWeakPB.get();
                if (pb != null) {
                    pb.updateAppOpsPlayAudio();
                }
            }
        }
    }

    private static class IPlayerWrapper extends IPlayer.Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IPlayerWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference(pb);
        }

        public void start() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerStart();
            }
        }

        public void pause() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerPause();
            }
        }

        public void stop() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerStop();
            }
        }

        public void setVolume(float vol) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetVolume(vol, vol);
            }
        }

        public void setPan(float pan) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetPan(pan);
            }
        }

        public void setStartDelayMs(int delayMs) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetStartDelayMs(delayMs);
            }
        }

        public void applyVolumeShaper(Configuration configuration, Operation operation) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerApplyVolumeShaper(configuration, operation);
            }
        }
    }

    public static class PlayerIdCard implements Parcelable {
        public static final int AUDIO_ATTRIBUTES_DEFINED = 1;
        public static final int AUDIO_ATTRIBUTES_NONE = 0;
        public static final Creator<PlayerIdCard> CREATOR = new Creator<PlayerIdCard>() {
            public PlayerIdCard createFromParcel(Parcel p) {
                return new PlayerIdCard(p, null);
            }

            public PlayerIdCard[] newArray(int size) {
                return new PlayerIdCard[size];
            }
        };
        public final AudioAttributes mAttributes;
        public final IPlayer mIPlayer;
        public final int mPlayerType;

        /* synthetic */ PlayerIdCard(Parcel in, PlayerIdCard -this1) {
            this(in);
        }

        PlayerIdCard(int type, AudioAttributes attr, IPlayer iplayer) {
            this.mPlayerType = type;
            this.mAttributes = attr;
            this.mIPlayer = iplayer;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.mPlayerType)});
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            IBinder iBinder = null;
            dest.writeInt(this.mPlayerType);
            this.mAttributes.writeToParcel(dest, 0);
            if (this.mIPlayer != null) {
                iBinder = this.mIPlayer.asBinder();
            }
            dest.writeStrongBinder(iBinder);
        }

        private PlayerIdCard(Parcel in) {
            this.mPlayerType = in.readInt();
            this.mAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(in);
            IBinder b = in.readStrongBinder();
            this.mIPlayer = b == null ? null : IPlayer.Stub.asInterface(b);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (this == o) {
                return true;
            }
            if (o == null || ((o instanceof PlayerIdCard) ^ 1) != 0) {
                return false;
            }
            PlayerIdCard that = (PlayerIdCard) o;
            if (this.mPlayerType == that.mPlayerType) {
                z = this.mAttributes.equals(that.mAttributes);
            }
            return z;
        }
    }

    abstract int playerApplyVolumeShaper(Configuration configuration, Operation operation);

    abstract State playerGetVolumeShaperState(int i);

    abstract void playerPause();

    abstract int playerSetAuxEffectSendLevel(boolean z, float f);

    abstract void playerSetVolume(boolean z, float f, float f2);

    abstract void playerStart();

    abstract void playerStop();

    PlayerBase(AudioAttributes attr, int implType) {
        if (attr == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        this.mAttributes = attr;
        this.mImplType = implType;
        this.mState = 1;
    }

    protected void baseRegisterPlayer() {
        int newPiid = -1;
        this.mAppOps = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        updateAppOpsPlayAudio();
        this.mAppOpsCallback = new IAppOpsCallbackWrapper(this);
        try {
            this.mAppOps.startWatchingMode(28, ActivityThread.currentPackageName(), this.mAppOpsCallback);
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = false;
        }
        try {
            this.mPlayerIdCard = new PlayerIdCard(this.mImplType, this.mAttributes, new IPlayerWrapper(this));
            newPiid = getService().trackPlayer(this.mPlayerIdCard);
            this.mContext = ActivityThread.currentApplication();
            if (this.mContext != null) {
                this.mAudioFeatures = new AudioFeatures(this.mContext, null, null);
            } else {
                this.mAudioFeatures = null;
            }
            this.mStartCallBackState = 1;
        } catch (RemoteException e2) {
            Log.e(TAG, "Error talking to audio service, player will not be tracked", e2);
        }
        this.mPlayerIId = newPiid;
    }

    void baseUpdateAudioAttributes(AudioAttributes attr) {
        if (attr == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        try {
            getService().playerAttributes(this.mPlayerIId, attr);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, STARTED state will not be tracked", e);
        }
        synchronized (this.mLock) {
            boolean attributesChanged = this.mAttributes != attr;
            this.mAttributes = attr;
            updateAppOpsPlayAudio_sync(attributesChanged);
        }
    }

    void baseStart() {
        Log.v(TAG, "baseStart() piid=" + this.mPlayerIId);
        try {
            synchronized (this.mLock) {
                this.mState = 2;
                if (this.mStartCallBackState != 2) {
                    PlaybackDetectionCallBack(AudioFeatures.TAG_FD_START, this.mState, -1);
                }
                this.mStartCallBackState = 1;
                getService().playerEvent(this.mPlayerIId, this.mState);
                if (!(this.mAudioFeatures == null || (this.mAudioFeatures.isMotorModeSupportPlayback() ^ 1) == 0)) {
                    Log.d(TAG, "motor mode on, mute this stream");
                    playerSetVolume(true, 0.0f, 0.0f);
                    this.mMotorModeFlag = true;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, STARTED state will not be tracked", e);
        }
        synchronized (this.mLock) {
            if (isRestricted_sync()) {
                playerSetVolume(true, 0.0f, 0.0f);
            }
        }
    }

    void baseSetStartDelayMs(int delayMs) {
        synchronized (this.mLock) {
            this.mStartDelayMs = Math.max(delayMs, 0);
        }
    }

    protected int getStartDelayMs() {
        int i;
        synchronized (this.mLock) {
            i = this.mStartDelayMs;
        }
        return i;
    }

    void basePause() {
        Log.v(TAG, "basePause() piid=" + this.mPlayerIId);
        try {
            synchronized (this.mLock) {
                this.mState = 3;
                getService().playerEvent(this.mPlayerIId, this.mState);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, PAUSED state will not be tracked", e);
        }
    }

    void baseStop() {
        Log.v(TAG, "baseStop() piid=" + this.mPlayerIId);
        try {
            synchronized (this.mLock) {
                PlaybackDetectionCallBack(AudioFeatures.TAG_FD_STOP, this.mState, -1);
                this.mState = 4;
                getService().playerEvent(this.mPlayerIId, this.mState);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, STOPPED state will not be tracked", e);
        }
    }

    void baseSetPan(float pan) {
        float p = Math.min(Math.max(-1.0f, pan), 1.0f);
        synchronized (this.mLock) {
            if (p >= 0.0f) {
                this.mPanMultiplierL = 1.0f - p;
                this.mPanMultiplierR = 1.0f;
            } else {
                this.mPanMultiplierL = 1.0f;
                this.mPanMultiplierR = 1.0f + p;
            }
        }
        baseSetVolume(this.mLeftVolume, this.mRightVolume);
    }

    /* JADX WARNING: Missing block: B:19:0x003a, code:
            playerSetVolume(r0, r4.mPanMultiplierL * r5, r4.mPanMultiplierR * r6);
     */
    /* JADX WARNING: Missing block: B:20:0x0043, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void baseSetVolume(float leftVolume, float rightVolume) {
        synchronized (this.mLock) {
            if (!this.mMotorModeFlag || this.mAudioFeatures == null || (this.mAudioFeatures.isMotorModeSupportPlayback() ^ 1) == 0) {
                this.mLeftVolume = leftVolume;
                this.mRightVolume = rightVolume;
                boolean isRestricted = isRestricted_sync();
                if (this.mAudioFeatures == null || (this.mAudioFeatures.isMotorModeSupportPlayback() ^ 1) == 0) {
                    this.mMotorModeFlag = false;
                } else {
                    this.mMotorModeFlag = true;
                }
            } else {
                Log.d(TAG, "motor mode on, do nothing!");
            }
        }
    }

    int baseSetAuxEffectSendLevel(float level) {
        synchronized (this.mLock) {
            this.mAuxEffectSendLevel = level;
            if (isRestricted_sync()) {
                return 0;
            }
            return playerSetAuxEffectSendLevel(false, level);
        }
    }

    void baseRelease() {
        Log.v(TAG, "baseRelease() piid=" + this.mPlayerIId + " state=" + this.mState);
        try {
            synchronized (this.mLock) {
                if (this.mState != 0) {
                    getService().releasePlayer(this.mPlayerIId);
                    PlaybackDetectionCallBack(AudioFeatures.TAG_FD_STOP, this.mState, -1);
                    this.mState = 0;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, the player will still be tracked", e);
        }
        try {
            if (this.mAppOps != null) {
                this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
            }
        } catch (Exception e2) {
        }
    }

    void vivobaseStart(int duration) {
        Log.v(TAG, "vivobaseStart() piid=" + this.mPlayerIId);
        synchronized (this.mLock) {
            PlaybackDetectionCallBack(AudioFeatures.TAG_FD_START, this.mState, duration);
            this.mStartCallBackState = 2;
        }
    }

    void vivobaseStop() {
        Log.v(TAG, "vivobaseStop() piid=" + this.mPlayerIId);
        synchronized (this.mLock) {
            PlaybackDetectionCallBack(AudioFeatures.TAG_FD_STOP, this.mState, -1);
        }
    }

    private void PlaybackDetectionCallBack(String state, int mState, int duration) {
        if (this.mAudioFeatures != null) {
            if (state.equals(AudioFeatures.TAG_FD_STOP) && (mState == 0 || mState == -1 || mState == 1 || mState == 4)) {
                Log.w(TAG, "baseStop state not match:" + mState);
                return;
            }
            TagParameters tp = new TagParameters(AudioFeatures.TAG_FACE_DETECT);
            tp.put(AudioFeatures.KEY_VOLUME, this.mLeftVolume);
            tp.put(AudioFeatures.KEY_DURATION, duration);
            this.mAudioFeatures.PlaybackDetectionCallBack(tp.toString(), state, this.mContext.getOpPackageName(), this.mAttributes.getUsage(), this);
        }
    }

    private void updateAppOpsPlayAudio() {
        synchronized (this.mLock) {
            updateAppOpsPlayAudio_sync(false);
        }
    }

    void updateAppOpsPlayAudio_sync(boolean attributesChanged) {
        boolean z = true;
        boolean oldHasAppOpsPlayAudio = this.mHasAppOpsPlayAudio;
        int mode = 1;
        try {
            if (this.mAppOps != null) {
                mode = this.mAppOps.checkAudioOperation(28, this.mAttributes.getUsage(), Process.myUid(), ActivityThread.currentPackageName());
            }
            if (mode != 0) {
                z = false;
            }
            this.mHasAppOpsPlayAudio = z;
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = false;
        }
        try {
            if (oldHasAppOpsPlayAudio != this.mHasAppOpsPlayAudio || attributesChanged) {
                getService().playerHasOpPlayAudio(this.mPlayerIId, this.mHasAppOpsPlayAudio);
                if (isRestricted_sync()) {
                    playerSetVolume(true, 0.0f, 0.0f);
                    playerSetAuxEffectSendLevel(true, 0.0f);
                    return;
                }
                playerSetVolume(false, this.mLeftVolume * this.mPanMultiplierL, this.mRightVolume * this.mPanMultiplierR);
                playerSetAuxEffectSendLevel(false, this.mAuxEffectSendLevel);
            }
        } catch (Exception e2) {
        }
    }

    boolean isRestricted_sync() {
        if (this.mHasAppOpsPlayAudio || (this.mAttributes.getAllFlags() & 64) != 0) {
            return false;
        }
        if ((this.mAttributes.getAllFlags() & 1) != 0 && this.mAttributes.getUsage() == 13) {
            boolean cameraSoundForced = false;
            try {
                cameraSoundForced = getService().isCameraSoundForced();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot access AudioService in isRestricted_sync()");
            } catch (NullPointerException e2) {
                Log.e(TAG, "Null AudioService in isRestricted_sync()");
            }
            if (cameraSoundForced) {
                return false;
            }
        }
        return true;
    }

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    public void setStartDelayMs(int delayMs) {
        baseSetStartDelayMs(delayMs);
    }

    public static void deprecateStreamTypeForPlayback(int streamType, String className, String opName) throws IllegalArgumentException {
        if (streamType == 10) {
            throw new IllegalArgumentException("Use of STREAM_ACCESSIBILITY is reserved for volume control");
        }
        Log.w(className, "Use of stream types is deprecated for operations other than volume control");
        Log.w(className, "See the documentation of " + opName + " for what to use instead with " + "android.media.AudioAttributes to qualify your playback use case");
    }
}
