package android.view.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.AttributeSet;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoAccelerateDecelerateInterpolator extends BaseInterpolator {
    private static final boolean DEBUG = false;
    private static final String TAG = "VivoAccelerateDecelerateInterpolator";
    private static PathInterpolator sInterpolator;

    public VivoAccelerateDecelerateInterpolator() {
        init();
    }

    public VivoAccelerateDecelerateInterpolator(Context context, AttributeSet Attr) {
        init();
    }

    private void init() {
        if (sInterpolator == null) {
            sInterpolator = new PathInterpolator(0.23f, 0.0f, 0.23f, 1.0f);
        }
    }

    public float getInterpolation(float input) {
        return sInterpolator.getInterpolation(input);
    }
}
