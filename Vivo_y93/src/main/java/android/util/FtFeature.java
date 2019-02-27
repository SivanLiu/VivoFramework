package android.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.SystemProperties;
import com.android.internal.telephony.SmsConstants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtFeature {
    private static final String CPU_MODE = SystemProperties.get("ro.board.platform", SmsConstants.FORMAT_UNKNOWN);
    public static final int FEATURE_COLOR_MANAGER_MASK = 1;
    public static final int FEATURE_COLOR_TEMPERATURE_MASK = 128;
    public static final int FEATURE_CURVED_SCREEN_MASK = 2;
    public static final int FEATURE_EAR_PHONE_MASK = 32;
    public static final int FEATURE_EXCEPTION_POLICY_MANAGER_MASK = 256;
    public static final int FEATURE_POPUP_FRONT_CAMERA_MASK = 64;
    public static final int FEATURE_ROUND_PHONE_MASK = 8;
    public static final int FEATURE_VIRTUAL_KEY_MASK = 4;
    public static final int FEATURE_VOLTE_ENABLE_MASK = 16;
    private static String OTG_MODE_STATE_FILE_PATH = null;
    private static final String OTG_MODE_STATE_FILE_PATH_8939 = "/sys/bus/platform/drivers/msm_otg/78d9000.usb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_8953 = "/sys/bus/platform/drivers/msm-dwc3/7000000.ssusb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_8976 = "/sys/bus/platform/drivers/msm_otg/78db000.usb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_8996 = "/sys/bus/platform/drivers/msm-dwc3/6a00000.ssusb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6750 = "/sys/devices/soc/11270000.usb3/musb-hdrc/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6752 = "/sys/bus/platform/drivers/mt_usb/mt_usb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6761 = "/sys/devices/platform/mt_usb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6763 = "/sys/devices/platform/otg_iddig/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6765 = "/sys/devices/platform/mt_usb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_MTK_6771 = "/sys/devices/platform/11200000.usb3/musb-hdrc/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_SDM660 = "/sys/bus/platform/drivers/msm-dwc3/a800000.ssusb/otg_mode";
    private static final String OTG_MODE_STATE_FILE_PATH_SDM710 = "/sys/devices/platform/soc/a600000.ssusb/otg_mode";
    private static final String PATH_OTG_8939 = "/sys/bus/platform/drivers/msm_otg/78d9000.usb/host_mode";
    private static final String PATH_OTG_8953 = "/sys/bus/platform/drivers/msm-dwc3/7000000.ssusb/host_mode";
    private static final String PATH_OTG_8976 = "/sys/bus/platform/drivers/msm_otg/78db000.usb/host_mode";
    private static final String PATH_OTG_8996 = "/sys/bus/platform/drivers/msm-dwc3/6a00000.ssusb/host_mode";
    private static String PATH_OTG_MODE = null;
    private static final String PATH_OTG_MTK_6750 = "/sys/devices/soc/11270000.usb3/musb-hdrc/host_mode";
    private static final String PATH_OTG_MTK_6752 = "/sys/bus/platform/drivers/mt_usb/mt_usb/host_mode";
    private static final String PATH_OTG_MTK_6761 = "/sys/devices/platform/mt_usb/host_mode";
    private static final String PATH_OTG_MTK_6763 = "/sys/devices/platform/otg_iddig/host_mode";
    private static final String PATH_OTG_MTK_6765 = "/sys/devices/platform/mt_usb/host_mode";
    private static final String PATH_OTG_MTK_6771 = "/sys/devices/platform/11200000.usb3/musb-hdrc/host_mode";
    private static final String PATH_OTG_SDM660 = "/sys/bus/platform/drivers/msm-dwc3/a800000.ssusb/host_mode";
    private static final String PATH_OTG_SDM710 = "/sys/devices/platform/soc/a600000.ssusb/host_mode";
    private static final String TAG = "FtFeature";
    private static final String VIGOUR_FEATURE_PRO = "ro.vigour.feature";
    private static final String sVigourFeatureBit = SystemProperties.get(VIGOUR_FEATURE_PRO, "0x00000000");

    private static void getOtgSwitchPath() {
        if (PATH_OTG_MODE != null) {
            return;
        }
        if (CPU_MODE.contains("8953")) {
            PATH_OTG_MODE = PATH_OTG_8953;
        } else if (CPU_MODE.contains("8976") || CPU_MODE.contains("8937") || CPU_MODE.contains("8917") || CPU_MODE.contains("8952")) {
            PATH_OTG_MODE = PATH_OTG_8976;
        } else if (CPU_MODE.contains("8939")) {
            PATH_OTG_MODE = PATH_OTG_8939;
        } else if (CPU_MODE.contains("8996")) {
            PATH_OTG_MODE = PATH_OTG_8996;
        } else if (CPU_MODE.contains("sdm660")) {
            PATH_OTG_MODE = PATH_OTG_SDM660;
        } else if (CPU_MODE.contains("sdm710") || CPU_MODE.contains("sdm845")) {
            PATH_OTG_MODE = PATH_OTG_SDM710;
        } else if (CPU_MODE.contains("6752")) {
            PATH_OTG_MODE = PATH_OTG_MTK_6752;
        } else if (CPU_MODE.contains("6765")) {
            PATH_OTG_MODE = "/sys/devices/platform/mt_usb/host_mode";
        } else if (CPU_MODE.contains("6750")) {
            PATH_OTG_MODE = PATH_OTG_MTK_6750;
        } else if (CPU_MODE.contains("6763")) {
            PATH_OTG_MODE = PATH_OTG_MTK_6763;
        } else if (CPU_MODE.contains("6771")) {
            PATH_OTG_MODE = PATH_OTG_MTK_6771;
        } else if (CPU_MODE.contains("6761")) {
            PATH_OTG_MODE = "/sys/devices/platform/mt_usb/host_mode";
        } else {
            PATH_OTG_MODE = PATH_OTG_8939;
        }
    }

    private static void getOtgModePath() {
        if (OTG_MODE_STATE_FILE_PATH != null) {
            return;
        }
        if (CPU_MODE.contains("8953")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_8953;
        } else if (CPU_MODE.contains("8976") || CPU_MODE.contains("8937") || CPU_MODE.contains("8917")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_8976;
        } else if (CPU_MODE.contains("8939")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_8939;
        } else if (CPU_MODE.contains("8996")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_8996;
        } else if (CPU_MODE.contains("sdm660")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_SDM660;
        } else if (CPU_MODE.contains("sdm710") || CPU_MODE.contains("sdm845")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_SDM710;
        } else if (CPU_MODE.contains("6752")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_MTK_6752;
        } else if (CPU_MODE.contains("6765")) {
            OTG_MODE_STATE_FILE_PATH = "/sys/devices/platform/mt_usb/otg_mode";
        } else if (CPU_MODE.contains("6750")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_MTK_6750;
        } else if (CPU_MODE.contains("6763")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_MTK_6763;
        } else if (CPU_MODE.contains("6771")) {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_MTK_6771;
        } else if (CPU_MODE.contains("6761")) {
            OTG_MODE_STATE_FILE_PATH = "/sys/devices/platform/mt_usb/otg_mode";
        } else {
            OTG_MODE_STATE_FILE_PATH = OTG_MODE_STATE_FILE_PATH_8939;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0067 A:{SYNTHETIC, Splitter: B:25:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0073 A:{SYNTHETIC, Splitter: B:31:0x0073} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isOtgEnable() {
        IOException e;
        boolean state;
        Throwable th;
        getOtgSwitchPath();
        File file = new File(PATH_OTG_MODE);
        if (file.exists()) {
            String line = null;
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new FileReader(file));
                try {
                    line = br2.readLine();
                    br2.close();
                    if (br2 != null) {
                        try {
                            br2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    br = br2;
                } catch (IOException e3) {
                    e2 = e3;
                    br = br2;
                    try {
                        e2.printStackTrace();
                        if (br != null) {
                        }
                        state = false;
                        if (line != null) {
                        }
                        return state;
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (br != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e22 = e4;
                e22.printStackTrace();
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                state = false;
                if (line != null) {
                }
                return state;
            }
            state = false;
            if (line != null) {
                if ("enabled".equals(line)) {
                    state = true;
                } else {
                    state = false;
                }
            }
            return state;
        }
        Log.e(TAG, "path:" + PATH_OTG_MODE + " is not exist");
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x006f A:{SYNTHETIC, Splitter: B:30:0x006f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setOtgEnable(boolean enable) {
        IOException e;
        Throwable th;
        getOtgSwitchPath();
        File file = new File(PATH_OTG_MODE);
        if (file.exists()) {
            BufferedWriter bw = null;
            try {
                String str;
                BufferedWriter bw2 = new BufferedWriter(new FileWriter(file));
                if (enable) {
                    try {
                        str = "enabled";
                    } catch (IOException e2) {
                        e = e2;
                        bw = bw2;
                    } catch (Throwable th2) {
                        th = th2;
                        bw = bw2;
                        if (bw != null) {
                        }
                        throw th;
                    }
                }
                str = "disabled";
                bw2.write(str);
                bw2.flush();
                bw2.close();
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                bw = bw2;
            } catch (IOException e4) {
                e3 = e4;
                try {
                    e3.printStackTrace();
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            return;
        }
        Log.e(TAG, "path:" + PATH_OTG_MODE + " is not exist");
    }

    public static String getOtgModeSwitchFilePath() {
        getOtgSwitchPath();
        return PATH_OTG_MODE;
    }

    public static String getOtgModeStateFilePath() {
        getOtgModePath();
        return OTG_MODE_STATE_FILE_PATH;
    }

    private static boolean isVigourFeatureValid() {
        return sVigourFeatureBit.length() > 2 && sVigourFeatureBit.charAt(0) == '0' && sVigourFeatureBit.charAt(1) == StateProperty.TARGET_X;
    }

    public static boolean isFeatureSupport(int mask) {
        boolean z = true;
        if (mask == 4) {
            return true;
        }
        if (isVigourFeatureValid()) {
            try {
                if ((Integer.valueOf(sVigourFeatureBit.substring(2), 16).intValue() & mask) <= 0) {
                    z = false;
                }
                return z;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isColorManagerSupport() {
        return isFeatureSupport(1);
    }
}
