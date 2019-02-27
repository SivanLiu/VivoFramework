package com.vivo.services.vivo4dgamevibrator;

import android.content.Context;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Slog;
import com.vivo.framework.vivo4dgamevibrator.IVivo4DGameVibratorService.Stub;

public class Vivo4DGameVibratorService extends Stub {
    private static final long MAX_VIB_TIME = 1000;
    private static final String TAG = "gamevibrator";
    private Vibrator mVibrator = null;

    private void startVib(long vibMillis) {
        Slog.d(TAG, "startVib time: " + SystemClock.elapsedRealtime());
        this.mVibrator.vibrate(VibrationEffect.createOneShot(vibMillis, -1), null);
    }

    public Vivo4DGameVibratorService(Context context) {
        this.mVibrator = (Vibrator) context.getSystemService(Vibrator.class);
        Slog.d(TAG, "Vivo4DGameVibratorService constructor method called,className: " + Vivo4DGameVibratorService.class.getName());
    }

    public void vibrate(int mod, long callTimeMillis, long vibMillis) {
        Slog.d(TAG, "vibrate method called,mod: " + mod + ",callTimeMillis: " + callTimeMillis + ",call cost: " + (SystemClock.elapsedRealtime() - callTimeMillis) + "ms,vibMillis: " + vibMillis);
        if (0 < vibMillis && 1000 > vibMillis) {
            startVib(vibMillis);
        }
    }
}
