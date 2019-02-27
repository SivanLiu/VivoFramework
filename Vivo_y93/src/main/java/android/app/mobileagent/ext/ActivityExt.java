package android.app.mobileagent.ext;

import android.app.Activity;
import android.app.ai.mobileagent.ext.ActivityCaptureEntry;
import android.app.mobileagent.util.LogUtil;
import java.util.concurrent.ExecutorService;

public class ActivityExt implements ActivityExtInterface {
    private static final String TAG = "ActivityExt";
    private static ActivityExtInterface iCaptureInfo = null;
    private ExecutorService mExecutorService = null;

    private static ActivityExtInterface captureView(Activity activity) {
        if (iCaptureInfo != null) {
            return iCaptureInfo;
        }
        try {
            iCaptureInfo = new ActivityCaptureEntry();
            LogUtil.d(TAG, "iCaptureInfo:" + iCaptureInfo);
            return iCaptureInfo;
        } catch (Exception ex) {
            LogUtil.e(TAG, "iCaptureInfo:" + ex.fillInStackTrace());
            return iCaptureInfo;
        }
    }

    public void handleOnResume(final Activity activity, ExecutorService executorService) {
        this.mExecutorService = executorService;
        if (this.mExecutorService != null) {
            this.mExecutorService.execute(new Runnable() {
                public void run() {
                    ActivityExtInterface capture = ActivityExt.captureView(activity);
                    if (capture != null) {
                        capture.handleOnResume(activity, ActivityExt.this.mExecutorService);
                    }
                }
            });
        }
    }
}
