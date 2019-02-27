package vivo.app.popupcamera;

public abstract class AbsPopupCameraManager {
    public static final int CAMERA_STATUS_CLOSED = 1;
    public static final int CAMERA_STATUS_INVALID = -1;
    public static final int CAMERA_STATUS_OPENED = 0;
    public static final int FRONT_CAMERA_STATUS_INVALID = -1;
    public static final int FRONT_CAMERA_STATUS_POPUP_INVALID_POSITION = 5;
    public static final int FRONT_CAMERA_STATUS_POPUP_JAMMED = 4;
    public static final int FRONT_CAMERA_STATUS_POPUP_OK = 1;
    public static final int FRONT_CAMERA_STATUS_PRESSED = 6;
    public static final int FRONT_CAMERA_STATUS_PUSH_INVALID_POSITION = 3;
    public static final int FRONT_CAMERA_STATUS_PUSH_JAMMED = 2;
    public static final int FRONT_CAMERA_STATUS_PUSH_OK = 0;

    public abstract int getFrontCameraStatus();

    public abstract boolean notifyCameraStatus(int i, int i2, String str);

    public abstract boolean popupFrontCamera();

    public abstract boolean takeupFrontCamera();
}
