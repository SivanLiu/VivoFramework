package com.vivo.services.motion;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public interface MotionRecognitionListener {
    Context onMotionActionTriger(int i);
}
