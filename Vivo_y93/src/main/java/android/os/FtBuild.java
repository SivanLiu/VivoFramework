package android.os;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Slog;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtBuild {
    public static final boolean IS_MULTIWINDOWVALID = SystemProperties.get("persist.vivo.multiwindow", "false").contains("true");
    public static final boolean IS_VIVO_FREEFORM_VALID = SystemProperties.get("persist.vivo.freeform", "false").contains("true");
    private static final String PLATFORM_INFO = SystemProperties.get("ro.vivo.product.solution", "unknown");
    public static final String PROP_ROM_VERSION = SystemProperties.get("ro.vivo.rom.version", "unknown");
    private static final String ROM_AB_VERSION = SystemProperties.get("ro.vivo.rom.abversion", "unknown");
    private static final String TAG = "FtBuild";
    private static final String UNKNOWN = "unknown";
    private static final String mCustiomizeBbk = SystemProperties.get("ro.product.customize.bbk", "unknown");
    private static final String mDisplayId = SystemProperties.get("ro.vivo.os.build.display.id", "unknown");
    private static final String mModel = SystemProperties.get("ro.product.model", "unknown");
    private static final String mNetEntry = SystemProperties.get("ro.vivo.net.entry", "unknown");
    private static final String mOpEntry = SystemProperties.get("ro.vivo.op.entry", "unknown");
    private static final String mOsName = SystemProperties.get("ro.vivo.os.name", "unknown");
    private static final String mOsVersion = SystemProperties.get("ro.vivo.os.version", "unknown");
    private static final String mOverSeas = SystemProperties.get("ro.vivo.product.overseas", "unknown");
    private static final String mProductName = SystemProperties.get("ro.vivo.product.model", "unknown");
    private static final String mProductVersion = SystemProperties.get("ro.vivo.product.version", "unknown");
    private static final String mReleaseBrand = SystemProperties.get("ro.vivo.product.release.brand", "unknown");
    private static final String mReleaseModel = SystemProperties.get("ro.vivo.product.release.model", "unknown");
    private static final String mReleaseName = SystemProperties.get("ro.vivo.product.release.name", "unknown");
    private static Float mRomVer = null;

    private static float getFuntouchSDKVersion() {
        return Float.valueOf(21.1f).floatValue();
    }

    public static String getFuntouchOsVersion() {
        String funtouchOsVersion;
        if (mDisplayId.equals("unknown")) {
            funtouchOsVersion = mOsName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + mOsVersion;
        } else {
            funtouchOsVersion = mDisplayId;
        }
        printfInfo("getFuntouchOsVersion:" + funtouchOsVersion);
        return funtouchOsVersion;
    }

    public static float getRomVersion() {
        if (mRomVer != null) {
            return mRomVer.floatValue();
        }
        String romVer = PROP_ROM_VERSION;
        if (!(romVer == null || (romVer.equals("unknown") ^ 1) == 0)) {
            String[] romVerSplit = romVer.split("_");
            if (romVerSplit != null) {
                try {
                    if (romVerSplit.length >= 2) {
                        mRomVer = Float.valueOf(romVerSplit[1]);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            mRomVer = Float.valueOf(romVer);
        }
        if (mRomVer == null) {
            mRomVer = Float.valueOf(1.0f);
        }
        printfInfo("getRomVersion mRomVer=" + mRomVer);
        return mRomVer.floatValue();
    }

    public static String getProductVersion() {
        String version = mProductVersion;
        if (isNetEntry()) {
            return SystemProperties.get("ro.build.netaccess.version", "unknown");
        }
        if (isCmccOpEntry()) {
            return SystemProperties.get("ro.vivo.op.entry.version", "unknown");
        }
        if (mCustiomizeBbk.equals("CN-YD")) {
            if ("PD1421".equals(getProductName())) {
                return version.replaceFirst("PD1421D", "PD1421L");
            }
            return version.replaceFirst("_", "-YD_");
        } else if (mCustiomizeBbk.equals("CN-DX")) {
            return version.replaceFirst("_", "-DX_");
        } else {
            return version;
        }
    }

    public static String getProductName() {
        return mProductName;
    }

    public static boolean isNetEntry() {
        return "yes".equals(mNetEntry);
    }

    public static boolean isCmccOpEntry() {
        if (mOpEntry.contains("CMCC_RW") || mOpEntry.equals("CMCC")) {
            return true;
        }
        return false;
    }

    public static boolean isOverSeas() {
        return "yes".equals(mOverSeas);
    }

    public static boolean isMTKPlatform() {
        return PLATFORM_INFO.equals("MTK");
    }

    public static boolean isQCOMPlatform() {
        return PLATFORM_INFO.equals("QCOM");
    }

    private static boolean isFlagshipMachine() {
        return mReleaseName.equals(mReleaseModel) ^ 1;
    }

    public static String getProductReleaseName() {
        if (!isFlagshipMachine()) {
            return mModel;
        }
        if (mReleaseBrand.equals("unknown")) {
            return mReleaseName;
        }
        return mReleaseBrand + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + mReleaseName;
    }

    public static String getProductReleaseModel() {
        if (mReleaseBrand.equals("unknown")) {
            return mReleaseModel;
        }
        return mReleaseBrand + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + mReleaseModel;
    }

    public static String getRomABVersion() {
        String abVersion = SystemProperties.get(ROM_AB_VERSION, "A");
        if (abVersion.equals("A") || abVersion.equals("B")) {
            return abVersion;
        }
        return "A";
    }

    private static void printfInfo(String msg) {
        Slog.i(TAG, msg);
    }
}
