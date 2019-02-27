package android.view.animation;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

public class TranslateAnimationEvade extends TranslateAnimation {
    private static final boolean DBG = Build.TYPE.equals("eng");
    private static final boolean DEBUG = false;
    private static final String TAG = "TranslateAnimationEvade";
    private static String[] mEvadAppPackages = new String[]{"jp.naver.line.android", "com.whatsapp"};

    public TranslateAnimationEvade(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public TranslateAnimationEvade(Context context, AttributeSet attrs, String targetPackageName) {
        super(context, attrs);
        if (shouldBackupAnim(targetPackageName)) {
            this.mFromXValue = 0.0f;
            this.mToXValue = 0.0f;
            if (DBG) {
                Log.i(TAG, "BackupAnim appPackage=" + targetPackageName);
            }
        }
    }

    private boolean shouldBackupAnim(String packageName) {
        for (String pkg : mEvadAppPackages) {
            if (pkg.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
