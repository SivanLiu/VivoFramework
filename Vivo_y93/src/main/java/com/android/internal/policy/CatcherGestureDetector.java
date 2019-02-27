package com.android.internal.policy;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

public class CatcherGestureDetector {
    private static final int MSG_HANDLE_LONGPRESS = 0;
    private static final String TAG = "CatcherGestureDetector";
    private boolean isFilter = false;
    private Context mContext;
    private Context mContextActivity;
    private FrameLayout mDecorView;
    private PointF mFirstDown = null;
    private LongPressHandler mLongPressHandler;
    private int mLongPressTimeout;
    private float mScrollWander;

    class LongPressHandler extends Handler {
        LongPressHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CatcherGestureDetector.this.startContentCatcher(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public CatcherGestureDetector(Context context, FrameLayout decorView, Context contextActivity) {
        this.mContext = context;
        this.mContextActivity = contextActivity;
        this.mDecorView = decorView;
        this.mLongPressHandler = new LongPressHandler();
        this.mLongPressTimeout = SystemProperties.getInt("persist.sys.content.long.press.time", 1000);
        this.mScrollWander = (float) ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void onAttached(int windowType) {
        Log.i(TAG, "DecorView onAttached");
        if (windowType >= 1000) {
            this.isFilter = true;
            Log.i(TAG, "window type filtered");
        }
    }

    public void onDetached() {
        cancelLongPressTask();
        Log.i(TAG, "DecorView onDetached");
    }

    public void dispatchTouchEvent(MotionEvent event) {
        if (!this.isFilter && this.mDecorView.getParent() == this.mDecorView.getViewRootImpl()) {
            switch (event.getActionMasked()) {
                case 0:
                    PointF point = new PointF(event.getRawX(), event.getRawY());
                    this.mFirstDown = point;
                    scheduleLongPressTask(point);
                    break;
                case 1:
                    cancelLongPressTask();
                    break;
                case 2:
                    if (this.mFirstDown != null) {
                        float dist_x = Math.abs(event.getRawX() - this.mFirstDown.x);
                        float dist_y = Math.abs(event.getRawY() - this.mFirstDown.y);
                        if (Math.sqrt((double) ((dist_x * dist_x) + (dist_y * dist_y))) > ((double) this.mScrollWander)) {
                            cancelLongPressTask();
                            break;
                        }
                    }
                    break;
                case 3:
                    cancelLongPressTask();
                    break;
                case 5:
                    cancelLongPressTask();
                    break;
                default:
                    return;
            }
        }
    }

    private void scheduleLongPressTask(PointF point) {
        this.mLongPressHandler.sendMessageDelayed(this.mLongPressHandler.obtainMessage(0, point), (long) this.mLongPressTimeout);
    }

    private void cancelLongPressTask() {
        this.mLongPressHandler.removeMessages(0);
    }

    private void startContentCatcher(PointF point) {
        Log.i(TAG, "start ContentCatcher");
        Intent intent = new Intent();
        intent.setAction("com.vivo.contentcatcher.action.TRIGGER");
        intent.setPackage("com.vivo.contentcatcher");
        intent.putExtra("pointX", point.x);
        intent.putExtra("pointY", point.y);
        this.mContextActivity.startService(intent);
    }
}
