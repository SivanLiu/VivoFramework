package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.Rlog;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class AudioStateInvalidHelper extends Handler {
    private static final int EVENT_CHECK_AUDIO_STATE_AFTER_MODEM_RESET = 1;
    private static final int EVENT_CHECK_AUDIO_STATE_VALID = 0;
    private static final int EVENT_RESTART_MODEM_INTERVAL_ONE_HOUR = 2;
    private static final int INVALID_AUDIO_STATE_CHECK_INTERVAL_TIME = 30000;
    private static final int MODEM_RESTART_INTERVAL_TIME = 3600000;
    public static final int QCRIL_QMI_AUDIO_STATE_DOWN = 2;
    public static final int QCRIL_QMI_AUDIO_STATE_UNKNOWN = 0;
    public static final int QCRIL_QMI_AUDIO_STATE_UP = 1;
    private static final String TAG = "AudioStateInvalidHelper";
    private static long mLastModemResetElapsedRealtime;
    private static int mModemRestartNum;
    private int mAudioState;
    private long mAudioStateInvalidRecover = 0;
    private long mAudioStateInvalidStart = 0;
    private Context mContext;
    private VivoCollectData mVivoCollectData;

    public AudioStateInvalidHelper(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        mLastModemResetElapsedRealtime = 0;
        mModemRestartNum = 0;
        this.mAudioState = -1;
    }

    public void handleMessage(Message msg) {
        long time;
        switch (msg.what) {
            case 0:
                if (mModemRestartNum == 0) {
                    restartModem();
                    return;
                } else if (mModemRestartNum < 5) {
                    time = SystemClock.elapsedRealtime() - mLastModemResetElapsedRealtime;
                    if (time > 3600000) {
                        restartModem();
                        return;
                    } else {
                        sendMessageDelayed(obtainMessage(2), 3600000 - time);
                        return;
                    }
                } else {
                    return;
                }
            case 1:
                collectData();
                return;
            case 2:
                if (!isValidState(this.mAudioState)) {
                    time = SystemClock.elapsedRealtime() - mLastModemResetElapsedRealtime;
                    if (time > 3600000) {
                        restartModem();
                        return;
                    } else {
                        sendMessageDelayed(obtainMessage(2), 3600000 - time);
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    public void setAudioState(int audioState) {
        log("setAudioState audioState = " + audioState);
        if (this.mAudioState == -1 || isValidState(audioState) != isValidState(this.mAudioState)) {
            this.mAudioState = audioState;
            if (isValidState(audioState)) {
                if (hasMessages(0)) {
                    removeMessages(0);
                }
                if (hasMessages(1)) {
                    removeMessages(1);
                    this.mAudioStateInvalidRecover = SystemClock.elapsedRealtime();
                    collectData();
                }
                if (hasMessages(2)) {
                    removeMessages(2);
                }
            } else {
                this.mAudioStateInvalidStart = SystemClock.elapsedRealtime();
                sendMessageDelayed(obtainMessage(0), 30000);
            }
            return;
        }
        this.mAudioState = audioState;
    }

    private void collectData() {
        log("collectData mAudioStateInvalidStart = " + this.mAudioStateInvalidStart + " mAudioStateInvalidRecover = " + this.mAudioStateInvalidRecover + " mLastModemResetElapsedRealtime = " + mLastModemResetElapsedRealtime + " mModemRestartNum = " + mModemRestartNum + " mAudioState = " + this.mAudioState);
        if (this.mVivoCollectData == null) {
            this.mVivoCollectData = new VivoCollectData(this.mContext);
        }
        HashMap<String, String> params = new HashMap();
        params.put("code", String.valueOf(this.mAudioState));
        this.mVivoCollectData.writeData("106", "1063", this.mAudioStateInvalidStart, this.mAudioStateInvalidRecover, mLastModemResetElapsedRealtime, mModemRestartNum, params);
        reset();
    }

    private void restartModem() {
        mModemRestartNum++;
        log("restartModem mModemRestartNum = " + mModemRestartNum);
        Intent intent = new Intent("vivo.intent.action.vivoresetmodem");
        intent.putExtra("flag", 1641);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        mLastModemResetElapsedRealtime = SystemClock.elapsedRealtime();
        sendMessageDelayed(obtainMessage(1), 30000);
    }

    private void reset() {
        this.mAudioStateInvalidStart = 0;
        this.mAudioStateInvalidRecover = 0;
    }

    private boolean isValidState(int state) {
        if (state == 1) {
            return true;
        }
        return false;
    }

    private void log(String str) {
        Rlog.e(TAG, str);
    }
}
