package android.app.mobileagent.ext;

import android.app.Activity;
import java.util.concurrent.ExecutorService;

public interface ActivityExtInterface {
    void handleOnResume(Activity activity, ExecutorService executorService);
}
