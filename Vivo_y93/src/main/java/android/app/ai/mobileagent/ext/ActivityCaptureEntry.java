package android.app.ai.mobileagent.ext;

import android.app.Activity;
import android.app.ai.mobileagent.capture.ICaptureInfo;
import android.app.mobileagent.ext.ActivityExtInterface;
import android.app.mobileagent.util.LogUtil;
import java.util.concurrent.ExecutorService;
import org.json.JSONObject;

public class ActivityCaptureEntry implements ActivityExtInterface {
    private static final String TAG = "ActivityCaptureEntry";

    private void handleCaptureView(Activity activity) {
        captureView(activity);
    }

    private <T extends Activity> JSONObject captureView(T classType) {
        if (classType == null) {
            return null;
        }
        try {
            ICaptureInfo captureInfo = (ICaptureInfo) Class.forName("android.app.ai.beeservice.capture.InfoCapture").newInstance();
            LogUtil.d(TAG, "captureInfo: " + captureInfo);
            if (captureInfo == null) {
                return null;
            }
            LogUtil.d(TAG, "capture");
            return captureInfo.captureViewInfo(classType);
        } catch (ClassNotFoundException ex2) {
            LogUtil.e(TAG, "ClassNotFoundException ex2:" + ex2);
            return null;
        } catch (IllegalAccessException ex3) {
            LogUtil.e(TAG, "IllegalAccessException ex3:" + ex3);
            return null;
        } catch (InstantiationException ex4) {
            LogUtil.e(TAG, "InstantiationException ex4:" + ex4);
            return null;
        } catch (Exception ex) {
            LogUtil.e(TAG, "Exception ex:" + ex);
            return null;
        }
    }

    public void handleOnResume(Activity activity, ExecutorService executorService) {
        handleCaptureView(activity);
    }
}
