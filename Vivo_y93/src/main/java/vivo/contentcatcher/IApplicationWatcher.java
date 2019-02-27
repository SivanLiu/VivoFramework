package vivo.contentcatcher;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public interface IApplicationWatcher {
    void onLoad(Activity activity);

    void onTouchEvent(View view, MotionEvent motionEvent);
}
