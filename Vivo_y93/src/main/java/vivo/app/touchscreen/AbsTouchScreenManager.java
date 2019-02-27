package vivo.app.touchscreen;

public abstract class AbsTouchScreenManager {
    public abstract void TouchscreenLcdBacklightStateSet(boolean z);

    public abstract int TouchscreenSetFingerGestureSwitch(int i);

    public abstract int touchScreenCallingSwitch(int i);

    public abstract int touchScreenGlovesModeSwitch(int i);

    public abstract void touchscreenAccStateSet(int i);
}
