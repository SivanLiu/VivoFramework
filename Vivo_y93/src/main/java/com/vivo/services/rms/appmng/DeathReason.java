package com.vivo.services.rms.appmng;

import android.os.UserHandle;
import android.util.EventLog;
import com.vivo.services.rms.ProcessInfo;
import com.vivo.services.rms.util.JniTool;
import java.io.File;

public class DeathReason {
    private static final String FILE_PATH = "/proc/vivo_rsc/check_lmk";
    private static boolean LMK_READY = isLMKReady();
    private static final String REASON_BY_LMK = "by lmk";

    public static void fillReason(ProcessInfo app) {
        if (LMK_READY && app.mKillReason == null && isByLmk(app.mPid)) {
            app.mKillReason = REASON_BY_LMK;
            EventLog.writeEvent(30023, new Object[]{Integer.valueOf(UserHandle.getUserId(app.mUid)), Integer.valueOf(app.mPid), app.mProcName, Integer.valueOf(app.mAdj), REASON_BY_LMK});
        }
    }

    private static boolean isByLmk(int pid) {
        return JniTool.writeFile(FILE_PATH, new StringBuilder().append(pid).append(" 1").toString()) >= 0;
    }

    private static boolean isLMKReady() {
        File file = new File(FILE_PATH);
        return file.exists() ? file.isFile() : false;
    }
}
