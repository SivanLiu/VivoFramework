package android.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.FtBuild;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtCpuInfo {
    private static final int DEFAULT_INVALID_VALUE_INT = 0;
    private static final long DEFAULT_INVALID_VALUE_LONG = 0;
    private static final String TAG = "FtCpuInfo";
    private static int mCpuNumber = 0;
    private static AbsCpuInfo mCpuinfoImpl;
    private static long mMaxCpuRate = 0;

    private static void createConcreteCpuinfoImpl() {
        if (mCpuinfoImpl != null) {
            return;
        }
        if (FtBuild.isMTKPlatform()) {
            mCpuinfoImpl = new MTKCpuInfo();
        } else if (FtBuild.isQCOMPlatform()) {
            mCpuinfoImpl = new QcomCpuInfo();
        }
    }

    public static String getCpuModel(Context context) {
        createConcreteCpuinfoImpl();
        if (mCpuinfoImpl != null) {
            return mCpuinfoImpl.getCpuModel(context);
        }
        return null;
    }

    public static long getMaxCpuRate() {
        if (0 == mMaxCpuRate) {
            createConcreteCpuinfoImpl();
            if (mCpuinfoImpl != null) {
                mMaxCpuRate = mCpuinfoImpl.getMaxCpuRate();
            }
        }
        return mMaxCpuRate;
    }

    public static int getCPUCoreNumber() {
        if (mCpuNumber == 0) {
            createConcreteCpuinfoImpl();
            if (mCpuinfoImpl != null) {
                mCpuNumber = mCpuinfoImpl.getCPUCoreNumber();
            }
        }
        return mCpuNumber;
    }
}
