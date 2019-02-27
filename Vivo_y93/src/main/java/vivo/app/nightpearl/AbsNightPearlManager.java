package vivo.app.nightpearl;

import android.os.IBinder.DeathRecipient;

public abstract class AbsNightPearlManager {
    public static final int NIGHT_PEARL_DISABLED = 0;
    public static final int NIGHT_PEARL_ENABLED = 1;
    public static final String NIGHT_PEARL_KEY = "night_pearl_enabled";
    public static final String NIGHT_REMIND_SWITCH = "night_remind";
    public static final String SCREEN_OFF_REMIND_SWITCH = "screen_off_remind";

    public abstract boolean isNightPearlShowing();

    public abstract void linkToDeath(DeathRecipient deathRecipient, int i);

    public abstract void onBacklightStateChanged(int i, int i2);

    public abstract void onDrawFinished();

    public abstract void onShowOff(int i);

    public abstract boolean unlinkToDeath(DeathRecipient deathRecipient, int i);
}
