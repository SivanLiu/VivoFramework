package android.app.ai.beeservice.capture;

import android.app.Activity;
import android.app.ai.beeservice.capture.types.profile.ActivityInfoCapture;
import android.app.ai.mobileagent.capture.ICaptureInfo;
import android.app.mobileagent.util.LogUtil;
import org.json.JSONObject;

public class InfoCapture implements ICaptureInfo<Activity> {
    public static final String TAG = "InfoCapture";

    public JSONObject captureViewInfo(Activity activity) {
        int i = 0;
        while (i < 3) {
            i++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                LogUtil.e(TAG, "InterruptedException ex: " + ex);
            }
            ActivityInfoCapture captureView = new ActivityInfoCapture(activity);
            if (captureView != null) {
                captureView.captureView();
                if (captureView.isSuccess) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex2) {
                    LogUtil.e(TAG, "InterruptedException ex2: " + ex2);
                }
            }
        }
        return null;
    }
}
