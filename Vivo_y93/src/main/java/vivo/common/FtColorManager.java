package vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.Application;
import android.content.Context;
import com.qti.snapdragon.sdk.display.ColorManager;
import com.qti.snapdragon.sdk.display.ColorManager.ColorManagerListener;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public abstract class FtColorManager {
    public static final int COLOR_MODE_BACKLIGHT_HIGH = 3;
    public static final int COLOR_MODE_BACKLIGHT_LOW = 2;
    public static final int COLOR_MODE_COLORTEMP = 4;
    public static final int COLOR_MODE_EYE_CARE = 1;
    public static final int COLOR_MODE_NORMAL = 0;
    public static final int COLOR_MODE_UNKNOWN = 16;
    public static final int RET_FAILED = -1;
    public static final int RET_SUCCESS = 0;
    public static final int RET_UNSUPPORT = -2;
    private static final String TAG = "FtColorManager";
    private static FtColorManager sFtColorManager = null;
    private static int sRefCount = 0;

    public interface CallBack {
        void onCallBack(FtColorManager ftColorManager);
    }

    public abstract int getActiveMode();

    public abstract int getInterfaceState();

    public abstract int getModeFactor();

    public abstract int isModeExist(int i);

    public abstract int notifyFingerAuthState(boolean z, int i);

    public abstract int setActiveMode(int i);

    public abstract int setActiveModeWithAm(int i);

    public abstract int setDefaultMode(int i);

    public abstract int setInterfaceState(int i);

    public abstract int setModeFactor(int i);

    public abstract int setUserDefaultMode();

    public static int getInstanceWithCallBack(final Context context, final Application app, final CallBack callback) {
        if (sFtColorManager != null) {
            callback.onCallBack(sFtColorManager);
            sRefCount++;
            return 0;
        }
        int r = ColorManager.connect(context, new ColorManagerListener() {
            public void onConnected() {
                if (FtColorManager.sFtColorManager == null) {
                    FtColorManager.sFtColorManager = new FtColorManagerImpl__QCOM(context, app);
                }
                if (((FtColorManagerImpl__QCOM) FtColorManager.sFtColorManager).isError()) {
                    FtColorManager.sFtColorManager = null;
                }
                if (FtColorManager.sFtColorManager != null) {
                    FtColorManager.sRefCount = FtColorManager.sRefCount + 1;
                    callback.onCallBack(FtColorManager.sFtColorManager);
                }
            }
        });
        if (r == 0) {
            return 0;
        }
        FtColorManagerImpl__QCOM.Log("connect  " + FtColorManagerImpl__QCOM.getErrorString(r));
        if (r == -901) {
            return -2;
        }
        return -1;
    }

    public static void releaseInstance(FtColorManager colorManager) {
        sRefCount--;
        if (sRefCount == 0) {
            sFtColorManager = null;
            colorManager.onRelease();
        }
        if (sRefCount < 0) {
            sRefCount = 0;
        }
    }

    protected void onRelease() {
    }
}
