package android.preference;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public interface VivoPreferenceBackground {
    int getBackgroundRes();
}
