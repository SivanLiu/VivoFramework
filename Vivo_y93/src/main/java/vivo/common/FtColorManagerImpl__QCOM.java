package vivo.common;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
import com.qti.snapdragon.sdk.display.ColorManager;
import com.qti.snapdragon.sdk.display.ColorManager.DCM_DISPLAY_TYPE;
import com.qti.snapdragon.sdk.display.ColorManager.MODE_TYPE;
import com.qti.snapdragon.sdk.display.ModeInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FtColorManagerImpl__QCOM extends FtColorManager {
    private static final String ACTION_NIGHT_MODE_CHANGE = "com.vivo.night.mode.change.intent";
    private static final String[] BACKLIGHT_DIR = new String[]{"/sys/class/backlight/panel0-backlight/brightness", "/sys/class/backlight/panel1-backlight/brightness", "/sys/class/leds/lcd-backlight/brightness"};
    public static final String COLOR_REGULATE_LIMIT_STATE = "color_regulate_limit_state";
    private static final int DIMMING_STEPS = 1;
    private static final String[] MODE_NAME_LIST = new String[]{"Mode_default", "Mode_EyeCare", "Mode_backlightlow", "Mode_backlighthigh", "Mode_EyeCare_user"};
    private static final int NONVIVO_MODULE = 2;
    private static final int ONE_STEP = 0;
    private static final String TAG = "FtColorManager";
    private static final int USER_INTERFACE_COLORTEMP = 4;
    private static final int USER_INTERFACE_EYECARE = 1;
    private static final int USER_INTERFACE_NORMAL = 0;
    private static final int VIVO_MODULE = 1;
    private static boolean mAnimating = false;
    private boolean colormanager_release = false;
    private int colormode = 0;
    private boolean isEyeCare;
    private boolean isEyeCare2;
    private int mAnimatedValue;
    private final Runnable mAnimationCallback = new Runnable() {
        public void run() {
            if (FtColorManagerImpl__QCOM.this.user_move_seek_bar) {
                FtColorManagerImpl__QCOM.this.user_move_seek_bar = false;
                FtColorManagerImpl__QCOM.this.cancelAnimationCallback();
                return;
            }
            if (FtColorManagerImpl__QCOM.this.mTargetValue > FtColorManagerImpl__QCOM.this.mAnimatedValue) {
                FtColorManagerImpl__QCOM.this.mAnimatedValue = Math.min(FtColorManagerImpl__QCOM.this.mAnimatedValue + 1, FtColorManagerImpl__QCOM.this.mTargetValue);
            } else {
                FtColorManagerImpl__QCOM.this.mAnimatedValue = Math.max(FtColorManagerImpl__QCOM.this.mAnimatedValue - 1, FtColorManagerImpl__QCOM.this.mTargetValue);
            }
            if (FtColorManagerImpl__QCOM.this.mAnimatedValue == 0) {
                FtColorManagerImpl__QCOM.this.isEyeCare = false;
            } else {
                FtColorManagerImpl__QCOM.this.isEyeCare = true;
            }
            if (FtColorManagerImpl__QCOM.this.readScreenBrightness() == 0) {
                FtColorManagerImpl__QCOM.this.setDimmingFactor(FtColorManagerImpl__QCOM.this.mTargetValue);
            } else if (FtColorManagerImpl__QCOM.this.mTargetValue == FtColorManagerImpl__QCOM.this.mAnimatedValue || !FtColorManagerImpl__QCOM.mAnimating) {
                FtColorManagerImpl__QCOM.this.setDimmingFactor(FtColorManagerImpl__QCOM.this.mAnimatedValue);
                FtColorManagerImpl__QCOM.this.cancelAnimationCallback();
                if (FtColorManagerImpl__QCOM.this.colormanager_release) {
                    FtColorManagerImpl__QCOM.this.mColorManager.release();
                    FtColorManagerImpl__QCOM.this.colormanager_release = false;
                }
                FtColorManagerImpl__QCOM.mAnimating = false;
            } else {
                FtColorManagerImpl__QCOM.this.setDimmingFactor(FtColorManagerImpl__QCOM.this.mAnimatedValue);
                FtColorManagerImpl__QCOM.this.postAnimationCallback();
            }
        }
    };
    private ColorManager mColorManager;
    private Context mContext;
    private boolean mError = false;
    private ModeInfo[] mModeList;
    private int mTargetValue;
    private HandlerThread mVivoEyesThread = null;
    private Handler mVivoEyescareHandler;
    private int min_color_b = 153;
    private int min_color_g = 214;
    private int min_color_r = 248;
    private int min_colortemp_b = 236;
    private int min_colortemp_g = 251;
    private int min_colortemp_r = 255;
    private String newModeName = "Mode_EyeCare_user";
    private final Runnable setUserDefaultModeCallback = new Runnable() {
        public void run() {
            Slog.d(FtColorManagerImpl__QCOM.TAG, "getActiveMode mode" + FtColorManagerImpl__QCOM.getEnableBlueLightModevivo());
            Slog.d(FtColorManagerImpl__QCOM.TAG, "getActiveMode mode" + FtColorManagerImpl__QCOM.getEnableColorTempModevivo());
            Slog.d(FtColorManagerImpl__QCOM.TAG, "ActiveMode == " + (FtColorManagerImpl__QCOM.getEnableBlueLightModevivo() | FtColorManagerImpl__QCOM.getEnableColorTempModevivo()));
            if (1 == FtColorManagerImpl__QCOM.this.getInterfaceState()) {
                FtColorManagerImpl__QCOM.setBlueLightIndexvivo(FtColorManagerImpl__QCOM.this.setUserfactor);
                FtColorManagerImpl__QCOM.Log("qxx setBlueLight UserDefaultModeCallback!");
            } else if (4 == FtColorManagerImpl__QCOM.this.getInterfaceState()) {
                FtColorManagerImpl__QCOM.setColorTempIndexvivo(FtColorManagerImpl__QCOM.this.setUserfactor);
                FtColorManagerImpl__QCOM.Log("qxx setColorTemp UserDefaultModeCallback!");
            } else {
                FtColorManagerImpl__QCOM.setBlueLightIndexvivo(FtColorManagerImpl__QCOM.this.setUserfactor);
                FtColorManagerImpl__QCOM.Log("qxx Unspecific or Normal mode, use the default bluelight setUserDefaultMode!");
            }
        }
    };
    private int setUserfactor;
    private int systembrightness;
    private int userModeId = -1;
    private boolean user_move_seek_bar;

    protected FtColorManagerImpl__QCOM(Context context, Application app) {
        Log("qxx FtColorManagerImpl__QCOM  construct!");
        this.mContext = context;
        this.mColorManager = ColorManager.getInstance(app, context, DCM_DISPLAY_TYPE.DISP_PRIMARY);
        if (this.mColorManager == null) {
            Log("ColorManager.getInstance  return null");
            this.mError = true;
            return;
        }
        this.mModeList = this.mColorManager.getModes(MODE_TYPE.MODE_ALL);
        if (this.mModeList == null) {
            Log("ColorManager.getModes return null");
            this.mError = true;
        }
        this.isEyeCare2 = getBlueLightSupportvivo().booleanValue();
        if (this.isEyeCare2) {
            this.mVivoEyesThread = new HandlerThread("VivoEyesThread");
            this.mVivoEyesThread.start();
            this.mVivoEyescareHandler = new Handler(this.mVivoEyesThread.getLooper());
            this.min_color_r = getBlueLightMinRedvivo();
            this.min_color_g = getBlueLightMinGreenvivo();
            this.min_color_b = getBlueLightMinBluevivo();
        }
    }

    public void animateTo_open() {
        mAnimating = true;
        if (getActiveMode() == 4) {
            this.mTargetValue = getColorTempIndexvivo();
        } else {
            this.mTargetValue = getBlueLightIndexvivo();
        }
        this.mAnimatedValue = getUserColorBalance();
        Slog.d(TAG, "Runnable animateTo_open ------- " + mAnimating);
        postAnimationCallback();
    }

    public void animateTo_close() {
        mAnimating = true;
        this.mTargetValue = 0;
        this.mAnimatedValue = getUserColorBalance();
        Slog.d(TAG, "Runnable animateTo_close ------- + " + mAnimating);
        postAnimationCallback();
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a9 A:{SYNTHETIC, Splitter: B:26:0x00a9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readFileByline(String fileName) {
        FileNotFoundException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                reader2.close();
            } catch (Exception e2) {
                try {
                    Slog.d(TAG, "reader.readLine():" + e2.getMessage());
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Slog.d(TAG, "the readFileByline is:" + e1.getMessage());
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e4) {
            e = e4;
            try {
                Slog.d(TAG, "the readFileByline is:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.d(TAG, "the readFileByline is:" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG, "the readFileByline is:" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    private int readScreenBrightness() {
        String brightnessString1 = readFileByline(getBacklightDirectoryvivo());
        return brightnessString1 != null ? Integer.parseInt(brightnessString1) : 0;
    }

    private void setUserDefaultimpl() {
        Slog.d(TAG, "setUserDefault from animate begin  " + this.userModeId);
        this.userModeId = getBlueLightModeIDvivo();
        if (this.userModeId <= 0) {
            this.userModeId = this.mColorManager.createNewMode(this.newModeName);
            if (this.userModeId < 0) {
                Log("createNewMode  fail  " + this.userModeId);
                setBlueLightModeIDvivo(2000);
                this.userModeId = 2000;
            } else {
                setBlueLightModeIDvivo(this.userModeId);
            }
        } else if (this.mColorManager.modifyMode(this.userModeId, this.newModeName) < 0) {
            Log("modifyMode change fail  " + this.userModeId);
            setBlueLightModeIDvivo(-1);
        }
        Slog.d(TAG, "setUserDefaultMode from animate end  " + this.userModeId);
    }

    private void postAnimationCallback() {
        this.mVivoEyescareHandler.post(this.mAnimationCallback);
    }

    private void cancelAnimationCallback() {
        this.mVivoEyescareHandler.removeCallbacks(this.mAnimationCallback);
    }

    private boolean isuserModeExist(int mode) {
        for (ModeInfo name : this.mModeList) {
            if (MODE_NAME_LIST[mode].equals(name.getName())) {
                Slog.d(TAG, "isuserModeExist mode " + mode);
                return true;
            }
        }
        return false;
    }

    protected boolean isError() {
        return this.mError;
    }

    private void setColorTempModeDisableState(int Displaymode) {
        if (Displaymode == 1) {
            Global.putInt(this.mContext.getContentResolver(), COLOR_REGULATE_LIMIT_STATE, 1);
        } else {
            Global.putInt(this.mContext.getContentResolver(), COLOR_REGULATE_LIMIT_STATE, 0);
        }
    }

    public int setActiveMode(int mode) {
        setDisplayModeState(mode);
        if (getFingerAuthState()) {
            Slog.w(TAG, "setActiveMode: eye mode change forbid " + mode);
            return -1;
        }
        if (this.isEyeCare2) {
            Slog.d(TAG, "qxx setActiveModeWithAm mode " + mode);
            if (1 == mode) {
                setEnableBlueLightModevivo(mode);
                setEnableColorTempModevivo(0);
                Slog.d(TAG, "qxx setActiveMode enableBlueLight=1,");
                animateTo_open();
                setColorTempModeDisableState(1);
                return 0;
            } else if (4 == mode) {
                setEnableColorTempModevivo(mode);
                setEnableBlueLightModevivo(0);
                Slog.d(TAG, "qxx setActiveMode enableColorTemp=1,");
                animateTo_open();
                setColorTempModeDisableState(0);
                return 0;
            } else if (mode == 0 || 2 == mode || 3 == mode) {
                setEnableBlueLightModevivo(mode);
                setEnableColorTempModevivo(mode);
                Slog.d(TAG, "qxx setActiveMode enableBlueLightvivo= close");
                animateTo_close();
                setColorTempModeDisableState(0);
                return 0;
            }
        }
        for (int i = 0; i < this.mModeList.length; i++) {
            if (MODE_NAME_LIST[mode].equals(this.mModeList[i].getName())) {
                int r = this.mColorManager.setActiveMode(this.mModeList[i].getId());
                if (r == 0) {
                    return 0;
                }
                Log("setActiveMode  " + getErrorString(r));
                if (r == -901) {
                    return -2;
                }
                return -1;
            }
        }
        return -1;
    }

    public int setActiveModeWithAm(int mode) {
        setDisplayModeState(mode);
        if (getFingerAuthState()) {
            Slog.w(TAG, "setActiveModeWithAm: eye mode change forbid " + mode);
            return -1;
        }
        if (this.isEyeCare2) {
            Slog.d(TAG, "qxx setActiveModeWithAm mode " + mode);
            if (1 == mode) {
                setEnableBlueLightModevivo(mode);
                setEnableColorTempModevivo(0);
                Slog.d(TAG, "qxx setActiveMode enableBlueLight=1,");
                animateTo_open();
                setColorTempModeDisableState(1);
                return 0;
            } else if (4 == mode) {
                setEnableColorTempModevivo(mode);
                setEnableBlueLightModevivo(0);
                Slog.d(TAG, "qxx setActiveMode enableColorTemp=1,");
                animateTo_open();
                setColorTempModeDisableState(0);
                return 0;
            } else if (mode == 0 || 2 == mode || 3 == mode) {
                if (getActiveMode() == 4) {
                    setEnableBlueLightModevivo(mode);
                    setEnableColorTempModevivo(mode);
                    animateTo_close();
                    setColorTempModeDisableState(0);
                    Slog.d(TAG, "qxx setActiveMode enableColorTempvivo= close");
                } else {
                    setEnableBlueLightModevivo(mode);
                    setEnableColorTempModevivo(mode);
                    animateTo_close();
                    setColorTempModeDisableState(0);
                    Slog.d(TAG, "qxx setActiveMode enableBlueLightvivo= close");
                }
                return 0;
            }
        }
        for (int i = 0; i < this.mModeList.length; i++) {
            if (MODE_NAME_LIST[mode].equals(this.mModeList[i].getName())) {
                int r = this.mColorManager.setActiveMode(this.mModeList[i].getId());
                if (r == 0) {
                    return 0;
                }
                Log("setActiveMode  " + getErrorString(r));
                if (r == -901) {
                    return -2;
                }
                return -1;
            }
        }
        return -1;
    }

    private static int getDisplayModeState() {
        int Displaymode = SystemProperties.getInt("persist.sys.displaymode.state", 0);
        Slog.d(TAG, "Displaymode: " + Displaymode);
        return Displaymode;
    }

    private static void setDisplayModeState(int Displaymode) {
        SystemProperties.set("persist.sys.displaymode.state", String.valueOf(Displaymode));
    }

    private static boolean getFingerAuthState() {
        return SystemProperties.getBoolean("sys.eye.finger.auth", false);
    }

    private static void setFingerAuthState(boolean fingerAuth) {
        SystemProperties.set("sys.eye.finger.auth", String.valueOf(fingerAuth));
    }

    public int notifyFingerAuthState(boolean fingerAuth, int type) {
        Slog.i(TAG, "notifyFingerAuthState: " + fingerAuth + ":" + type);
        setFingerAuthState(fingerAuth);
        if (fingerAuth) {
            if (1 == getDisplayModeState() || 4 == getDisplayModeState()) {
                return setActiveModeWithAmForUD(0, 1);
            }
        } else if (1 == getDisplayModeState() || 4 == getDisplayModeState()) {
            if (2 == type) {
                return setActiveModeWithAmForUD(getDisplayModeState(), 1);
            }
            if (1 == type) {
                return setActiveModeWithAmForUD(getDisplayModeState(), 0);
            }
        }
        return -1;
    }

    private void notifyNightModeChange() {
        Intent intent = new Intent(ACTION_NIGHT_MODE_CHANGE);
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent);
        } else {
            Slog.w(TAG, "notifyNightModeChange: broadcast sent failed");
        }
    }

    private int setActiveModeWithAmForUD(int Displaymode, int Dimmingmode) {
        if (this.isEyeCare2) {
            Slog.d(TAG, "UD setActiveModeWithAm Displaymode " + Displaymode);
            if (1 == Displaymode) {
                setEnableBlueLightModevivo(Displaymode);
                setEnableColorTempModevivo(0);
                Slog.d(TAG, "UD setActiveMode enableBlueLight=1,");
                if (Dimmingmode == 0) {
                    this.mTargetValue = getBlueLightIndexvivo();
                    setDimmingFactor(this.mTargetValue);
                } else {
                    animateTo_open();
                }
                setColorTempModeDisableState(1);
                notifyNightModeChange();
                return 0;
            } else if (4 == Displaymode) {
                setEnableColorTempModevivo(Displaymode);
                setEnableBlueLightModevivo(0);
                Slog.d(TAG, "UD setActiveMode enableColorTemp=4,");
                if (Dimmingmode == 0) {
                    this.mTargetValue = getColorTempIndexvivo();
                    setDimmingFactor(this.mTargetValue);
                } else {
                    animateTo_open();
                }
                setColorTempModeDisableState(0);
                notifyNightModeChange();
                return 0;
            } else if (Displaymode == 0 || 2 == Displaymode || 3 == Displaymode) {
                Slog.d(TAG, "UD setActiveMode enableBlueLightvivo= close");
                setEnableBlueLightModevivo(Displaymode);
                setEnableColorTempModevivo(Displaymode);
                if (Dimmingmode == 0) {
                    setDimmingFactor(0);
                } else {
                    animateTo_close();
                }
                setColorTempModeDisableState(0);
                notifyNightModeChange();
                return 0;
            }
        }
        for (int i = 0; i < this.mModeList.length; i++) {
            if (MODE_NAME_LIST[Displaymode].equals(this.mModeList[i].getName())) {
                int r = this.mColorManager.setActiveMode(this.mModeList[i].getId());
                if (r == 0) {
                    return 0;
                }
                Log("UD setActiveMode  " + getErrorString(r));
                if (r == -901) {
                    return -2;
                }
                return -1;
            }
        }
        return -1;
    }

    public int isModeExist(int mode) {
        for (ModeInfo name : this.mModeList) {
            if (MODE_NAME_LIST[mode].equals(name.getName())) {
                return 0;
            }
        }
        return -2;
    }

    public int getActiveMode() {
        if (this.isEyeCare2) {
            Slog.d(TAG, "GetActiveMode : qxx getBlueLightActiveMode mode" + getEnableBlueLightModevivo());
            Slog.d(TAG, "GetActiveMode : qxx getColorTempActiveMode mode" + getEnableColorTempModevivo());
            Slog.d(TAG, "GetActiveMode : qxx get ActiveMode == " + (getEnableBlueLightModevivo() | getEnableColorTempModevivo()));
            return getEnableBlueLightModevivo() | getEnableColorTempModevivo();
        }
        int[] r = this.mColorManager.getActiveMode();
        if (r[0] < 0) {
            Log("getActiveMode  " + getErrorString(r[0]));
            if (r[0] == -901) {
                return -2;
            }
            return -1;
        }
        for (int i = 0; i < this.mModeList.length; i++) {
            if (this.mModeList[i].getId() == r[0]) {
                for (int j = 0; j < MODE_NAME_LIST.length; j++) {
                    if (MODE_NAME_LIST[j].equals(this.mModeList[i].getName())) {
                        return j;
                    }
                }
                return 16;
            }
        }
        return -1;
    }

    public int getUserColorBalance() {
        double factor;
        double factor_g = this.mColorManager.getUserColorBalance()[2];
        Slog.d(TAG, "qxx getUserColorBalance: factor_g == " + factor_g);
        double temp_g;
        if (4 == getInterfaceState()) {
            if (getActiveMode() == 4) {
                temp_g = (double) this.min_colortemp_g;
                if (factor_g <= 0.985d || factor_g >= 0.989d) {
                    factor = ((temp_g - (255.0d * factor_g)) / ((temp_g - 241.0d) / 127.0d)) + 128.0d;
                } else {
                    factor = (252.0d - (255.0d * factor_g)) / ((252.0d - temp_g) / 127.0d);
                }
            } else {
                temp_g = (double) this.min_color_g;
                factor = 255.0d - ((((255.0d * factor_g) - temp_g) * 255.0d) / (255.0d - temp_g));
            }
        } else if (getActiveMode() == 4) {
            factor = 382.0d - (((255.0d * factor_g) - 214.0d) / (27.0d / 127.0d));
            Slog.d(TAG, "qxx test SP getUserColorBalance: factor == " + ((int) factor));
        } else {
            temp_g = (double) this.min_color_g;
            factor = 255.0d - ((((255.0d * factor_g) - temp_g) * 255.0d) / (255.0d - temp_g));
        }
        Slog.d(TAG, "qxx getUserColorBalance: factor == " + ((int) factor));
        return (int) factor;
    }

    public int getModeFactor() {
        Slog.d(TAG, "GetModeFactor : qxx getActiveMode mode" + getEnableBlueLightModevivo());
        Slog.d(TAG, "GetModeFactor : qxx getActiveMode mode" + getEnableColorTempModevivo());
        Slog.d(TAG, "GetModeFactor : qxx get ActiveMode == " + (getEnableBlueLightModevivo() | getEnableColorTempModevivo()));
        if (1 == getInterfaceState()) {
            Log("GetModeFactor: qxx getModeFactor  " + getBlueLightIndexvivo());
            return getBlueLightIndexvivo();
        } else if (4 == getInterfaceState()) {
            Log("GetModeFactor: qxx getColorTempModeFactor  " + getColorTempIndexvivo());
            return getColorTempIndexvivo();
        } else {
            Log("GetModeFactor: qxx Unspecific or Normal mode, return the bluelight ModeFactor!");
            return getBlueLightIndexvivo();
        }
    }

    protected static void Log(String str) {
        Log.d(TAG, str);
    }

    protected static String getErrorString(int errorCode) {
        switch (errorCode) {
            case -999:
                return "RET_FAILURE";
            case -905:
                return "RET_FEATURE_DISABLED";
            case -904:
                return "RET_ILLEGAL_ARGUMENT";
            case -903:
                return "RET_PERMISSION_DENIED";
            case -902:
                return "RET_VALUE_OUT_OF_RANGE";
            case -901:
                return "RET_NOT_SUPPORTED";
            case 0:
                return "RET_SUCCESS";
            default:
                return "UNKNOWN " + errorCode;
        }
    }

    public int factor_map(int factor) {
        return 255 - factor;
    }

    public int setModeFactor(int factor) {
        double factor_r;
        double factor_g;
        double factor_b;
        this.user_move_seek_bar = true;
        this.setUserfactor = factor;
        Log("qxx setModeFactor  factor " + factor);
        double temp_r;
        double temp_g;
        double temp_b;
        if (getInterfaceState() == 1) {
            factor = factor_map(factor);
            temp_r = (double) this.min_color_r;
            temp_g = (double) this.min_color_g;
            temp_b = (double) this.min_color_b;
            if (factor == 255) {
                factor_r = 255.0d;
            } else {
                factor_r = temp_r;
            }
            factor_g = temp_g + (((255.0d - temp_g) * ((double) factor)) / 255.0d);
            factor_b = temp_b + (((255.0d - temp_b) * ((double) factor)) / 255.0d);
            Log("set_bluelight_ModeFactor  factor_r " + factor_r + " factor_g " + factor_g + " factor_b " + factor_b);
        } else {
            temp_r = (double) this.min_colortemp_r;
            temp_g = (double) this.min_colortemp_g;
            temp_b = (double) this.min_colortemp_b;
            if (factor >= 0 && factor <= 127) {
                factor_r = 250.0d + (((temp_r - 250.0d) / 127.0d) * ((double) factor));
                factor_g = 252.0d - (((252.0d - temp_g) / 127.0d) * ((double) factor));
                factor_b = 255.0d - (((255.0d - temp_b) / 127.0d) * ((double) factor));
                Log("set_colortemp_ModeFactor  factor_r " + factor_r + " factor_g " + factor_g + " factor_b " + factor_b);
            } else if (factor <= 127 || factor > 255) {
                factor_r = 255.0d;
                factor_g = 214.0d + ((27.0d / 127.0d) * ((double) (382 - factor)));
                factor_b = 153.0d + ((45.0d / 127.0d) * ((double) (382 - factor)));
                Log("set_colortemp_ModeFactor SP factor_r " + 255.0d + " factor_g " + factor_g + " factor_b " + factor_b);
            } else {
                factor_r = 255.0d;
                factor_g = temp_g - (((temp_g - 241.0d) / 127.0d) * ((double) (factor - 128)));
                factor_b = temp_b - (((temp_b - 198.0d) / 127.0d) * ((double) (factor - 128)));
                Log("set_colortemp_ModeFactor  factor_r " + 255.0d + " factor_g " + factor_g + " factor_b " + factor_b);
            }
        }
        double red = factor_r / 255.0d;
        double green = factor_g / 255.0d;
        double blue = factor_b / 255.0d;
        int r = this.mColorManager.setUserColorBalance(red, green, blue);
        this.mVivoEyescareHandler.post(this.setUserDefaultModeCallback);
        Log("setModeFactor  red " + red + " green " + green + "blue " + blue);
        this.user_move_seek_bar = false;
        if (r == 0) {
            return 0;
        }
        Log("setModeFactor  " + getErrorString(r));
        if (r == -901) {
            return -2;
        }
        return -1;
    }

    public int setDimmingFactor(int factor) {
        double factor_r;
        double factor_g;
        double factor_b;
        Log("qxx setDimmingFactor  factor " + factor);
        double temp_r;
        double temp_g;
        double temp_b;
        if (getActiveMode() == 4 || (getInterfaceState() == 4 && getActiveMode() == 0)) {
            temp_r = (double) this.min_colortemp_r;
            temp_g = (double) this.min_colortemp_g;
            temp_b = (double) this.min_colortemp_b;
            if (factor >= 0 && factor <= 127) {
                factor_r = 250.0d + (((temp_r - 250.0d) / 127.0d) * ((double) factor));
                factor_g = 252.0d - (((252.0d - temp_g) / 127.0d) * ((double) factor));
                factor_b = 255.0d - (((255.0d - temp_b) / 127.0d) * ((double) factor));
                Log("set_colortemp_DimmingFactor  factor_r " + factor_r + " factor_g " + factor_g + " factor_b " + factor_b);
            } else if (factor <= 127 || factor > 255) {
                factor_r = 255.0d;
                factor_g = 214.0d + ((27.0d / 127.0d) * ((double) (382 - factor)));
                factor_b = 153.0d + ((45.0d / 127.0d) * ((double) (382 - factor)));
                Log("set_colortemp_DimmingFactor SP factor_r " + 255.0d + " factor_g " + factor_g + " factor_b " + factor_b);
            } else {
                factor_r = 255.0d;
                factor_g = temp_g - (((temp_g - 241.0d) / 127.0d) * ((double) (factor - 128)));
                factor_b = temp_b - (((temp_b - 198.0d) / 127.0d) * ((double) (factor - 128)));
                Log("set_colortemp_DimmingFactor  factor_r " + 255.0d + " factor_g " + factor_g + " factor_b " + factor_b);
            }
        } else {
            factor = factor_map(factor);
            Log("qxx setDimmingFactor2  factor " + factor);
            temp_r = (double) this.min_color_r;
            temp_g = (double) this.min_color_g;
            temp_b = (double) this.min_color_b;
            if (factor == 255) {
                factor_r = 255.0d;
            } else {
                factor_r = temp_r;
            }
            factor_g = temp_g + (((255.0d - temp_g) * ((double) factor)) / 255.0d);
            factor_b = temp_b + (((255.0d - temp_b) * ((double) factor)) / 255.0d);
            Log("set_bluelight_DimmingFactor  factor_r " + factor_r + " factor_g " + factor_g + " factor_b " + factor_b);
        }
        int r = this.mColorManager.setUserColorBalance(factor_r / 255.0d, factor_g / 255.0d, factor_b / 255.0d);
        if (r == 0) {
            return 0;
        }
        Log("setDimmingFactor  " + getErrorString(r));
        if (r == -901) {
            return -2;
        }
        return -1;
    }

    public int setDefaultMode(int mode) {
        for (int i = 0; i < this.mModeList.length; i++) {
            if (MODE_NAME_LIST[mode].equals(this.mModeList[i].getName())) {
                int r = this.mColorManager.setDefaultMode(this.mModeList[i].getId());
                if (r == 0) {
                    return 0;
                }
                Log("setDefalutMode  " + getErrorString(r));
                if (r == -901) {
                    return -2;
                }
                return -1;
            }
        }
        return -1;
    }

    public int getInterfaceState() {
        return getUserInterface();
    }

    public int setInterfaceState(int interfaceState) {
        setUserInterface(interfaceState);
        return 0;
    }

    public int setUserDefaultMode() {
        Slog.d(TAG, "setUserDefaultMode begin  " + this.userModeId);
        this.userModeId = getBlueLightModeIDvivo();
        Slog.d(TAG, "setUserDefaultModeCallback begin  " + this.userModeId);
        if (this.userModeId <= 0) {
            this.userModeId = this.mColorManager.createNewMode(this.newModeName);
            if (this.userModeId < 0) {
                Log("createNewMode  fail  " + this.userModeId);
                setBlueLightModeIDvivo(2000);
                this.userModeId = 2000;
            } else {
                setBlueLightModeIDvivo(this.userModeId);
            }
        }
        this.mColorManager.setDefaultMode(this.userModeId);
        Slog.d(TAG, "setUserDefaultModeCallback end  " + this.userModeId);
        Slog.d(TAG, "setUserDefaultMode end  " + this.userModeId);
        return 0;
    }

    protected void onRelease() {
        Slog.d(TAG, "onRelease and isEyeCare2 is " + this.isEyeCare2);
        if (!this.isEyeCare2) {
            this.mColorManager.release();
        } else if (!mAnimating) {
            Slog.d(TAG, "onRelease and mAnimating is " + mAnimating);
            this.mColorManager.release();
            if (this.mVivoEyesThread != null) {
                this.mVivoEyesThread.getLooper().quit();
            }
        } else if (this.mTargetValue == this.mAnimatedValue || !mAnimating) {
            this.colormanager_release = true;
        } else {
            this.colormanager_release = false;
        }
    }

    public static void setEnableBlueLightModevivo(int mode) {
        SystemProperties.set("persist.sys.bluelight.en.vivo", Integer.toString(mode));
    }

    public static int getEnableBlueLightModevivo() {
        return SystemProperties.getInt("persist.sys.bluelight.en.vivo", -1);
    }

    public static void setBlueLightIndexvivo(int index) {
        Slog.d(TAG, "setBlueLightIndexvivo ------=" + index);
        SystemProperties.set("persist.sys.bluelight.ui.vivo", Integer.toString(index));
    }

    public static int getBlueLightIndexvivo() {
        int index = SystemProperties.getInt("persist.sys.bluelight.ui.vivo", -1);
        Slog.d(TAG, "getBlueLightIndexvivo ------=" + index);
        return index;
    }

    public static void setBlueLightModeIDvivo(int modeid) {
        SystemProperties.set("persist.sys.bluelight.mode", Integer.toString(modeid));
    }

    public static int getBlueLightModeIDvivo() {
        return SystemProperties.getInt("persist.sys.bluelight.mode", -10);
    }

    public static Boolean getBlueLightSupportvivo() {
        return Boolean.valueOf(SystemProperties.getBoolean("ro.build.eyecare.support", false));
    }

    public static void setBlueLightstrengthvivo(int strength) {
        SystemProperties.set("persist.sys.bluelight.strength", Integer.toString(strength));
    }

    public static int getBlueLightstrengthvivo() {
        return SystemProperties.getInt("persist.sys.bluelight.strength", 0);
    }

    public static void setEnableColorTempModevivo(int mode) {
        SystemProperties.set("persist.sys.colortemp.en.vivo", Integer.toString(mode));
    }

    public static int getEnableColorTempModevivo() {
        return SystemProperties.getInt("persist.sys.colortemp.en.vivo", -1);
    }

    public static void setColorTempIndexvivo(int index) {
        Slog.d(TAG, "setColorTempIndexvivo ------=" + index);
        SystemProperties.set("persist.sys.colortemp.ui.vivo", Integer.toString(index));
    }

    public static int getColorTempIndexvivo() {
        int index = SystemProperties.getInt("persist.sys.colortemp.ui.vivo", -1);
        Slog.d(TAG, "getColorTempIndexvivo ------=" + index);
        return index;
    }

    public static void setColorTempModeIDvivo(int modeid) {
        SystemProperties.set("persist.sys.colortemp.mode", Integer.toString(modeid));
    }

    public static int getColorTempModeIDvivo() {
        return SystemProperties.getInt("persist.sys.colortemp.mode", -10);
    }

    public static Boolean getColorTempSupportvivo() {
        return Boolean.valueOf(SystemProperties.getBoolean("persist.sys.colortemp.support", false));
    }

    private static int getUserInterface() {
        return SystemProperties.getInt("persist.sys.user.interface", -10);
    }

    private static void setUserInterface(int interfaceState) {
        SystemProperties.set("persist.sys.user.interface", String.valueOf(interfaceState));
    }

    public static int getBlueLightMinRedvivo() {
        return SystemProperties.getInt("persist.sys.bluelight.minred", 248);
    }

    public static int getBlueLightMinGreenvivo() {
        return SystemProperties.getInt("persist.sys.bluelight.mingreen", 214);
    }

    public static int getBlueLightMinBluevivo() {
        return SystemProperties.getInt("persist.sys.bluelight.minblue", 153);
    }

    public static String getBacklightDirectoryvivo() {
        return BACKLIGHT_DIR[SystemProperties.getInt("persist.sys.backlight.dir", 0)];
    }
}
