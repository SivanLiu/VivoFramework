package android.util;

import android.content.Context;
import android.graphics.Point;
import android.provider.Settings.Secure;
import com.vivo.internal.R;

public class FtDeviceInfo {
    public static final String NAVIGATION_GESTURE_ON = "navigation_gesture_on";
    public static final int VALUE_GESTURE_ON_HOME_INDICATOR = 3;

    public static int getUpRoundRaidus(Context context) {
        return (int) context.getResources().getDimension(R.dimen.round_phone_up_radius);
    }

    public static int getDnRoundRaidus(Context context) {
        return (int) context.getResources().getDimension(R.dimen.round_phone_down_radius);
    }

    public static int getEarUpWidth(Context context) {
        return (int) context.getResources().getDimension(R.dimen.ear_up_width);
    }

    public static int getEarDnWidth(Context context) {
        return (int) context.getResources().getDimension(R.dimen.ear_down_width);
    }

    public static int getEarHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.ear_height);
    }

    public static Point getPortraitEarPosition(Context context) {
        return new Point((int) context.getResources().getDimension(R.dimen.ear_position_x), (int) context.getResources().getDimension(R.dimen.ear_position_y));
    }

    public static int getGestureBarHeight(Context context) {
        if (Secure.getInt(context.getContentResolver(), NAVIGATION_GESTURE_ON, 3) == 3) {
            return (int) context.getResources().getDimension(R.dimen.gesture_bar_height);
        }
        return 0;
    }
}
