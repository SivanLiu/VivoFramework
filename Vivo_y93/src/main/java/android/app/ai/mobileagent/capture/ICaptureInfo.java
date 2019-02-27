package android.app.ai.mobileagent.capture;

import android.app.Activity;
import org.json.JSONObject;

public interface ICaptureInfo<T extends Activity> {
    JSONObject captureViewInfo(T t);
}
