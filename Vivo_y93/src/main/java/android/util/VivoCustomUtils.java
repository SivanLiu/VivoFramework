package android.util;

import android.database.sqlite.SQLiteStatement;
import android.os.SystemProperties;
import com.vivo.services.cust.VivoCustomManager;
import java.util.List;

public class VivoCustomUtils {
    private static final String TAG = "VivoCustomUtils";
    private static final String VIVO_CUSTOM_SUPPORT = SystemProperties.get("ro.build.gn.support", "0");
    private static VivoCustomManager mVivoCustomManager;

    public static boolean isVivoCustomized() {
        return "0".equals(VIVO_CUSTOM_SUPPORT) ^ 1;
    }

    public static List<String> getCustomizedApps(int type) {
        if (mVivoCustomManager == null) {
            mVivoCustomManager = new VivoCustomManager();
        }
        return mVivoCustomManager.getCustomizedApps(type);
    }

    public static List<String> getByPassPermissions() {
        if (mVivoCustomManager == null) {
            mVivoCustomManager = new VivoCustomManager();
        }
        return mVivoCustomManager.getByPassPermissions();
    }

    public static List<String> getByPassOps() {
        if (mVivoCustomManager == null) {
            mVivoCustomManager = new VivoCustomManager();
        }
        return mVivoCustomManager.getByPassOps();
    }

    public static void loadSctSecureSettings(SQLiteStatement stmt) {
        Slog.i(TAG, "loadSctSecureSettings : ");
    }

    private static void loadSetting(SQLiteStatement stmt, String key, Object value) {
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }
}
