package com.vivo.services.engineerutile;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import java.util.List;

public class PCBAFloatView {
    private static final int MSG_HILE = 0;
    private static final int MSG_SHOW = 1;
    private static final String TAG = "PCBAFloatView";
    private Context mContext = null;
    private Handler mHandler = null;
    private boolean mShow = false;
    private TextView mTextView = null;
    private WindowManager mWindowManager = null;
    private LayoutParams wmParams = null;

    private class LocaleChangeReceiver extends BroadcastReceiver {
        /* synthetic */ LocaleChangeReceiver(PCBAFloatView this$0, LocaleChangeReceiver -this1) {
            this();
        }

        private LocaleChangeReceiver() {
        }

        /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && PCBAFloatView.this.mTextView != null && intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                String mLock = SystemProperties.get("ro.pcba.control", "1");
                if ("0".equals(mLock)) {
                    PCBAFloatView.this.mTextView.setText(51249535);
                } else if ("2".equals(mLock)) {
                    PCBAFloatView.this.mTextView.setText(51249536);
                }
            }
        }
    }

    private class ViewHandler extends Handler {
        /* synthetic */ ViewHandler(PCBAFloatView this$0, ViewHandler -this1) {
            this();
        }

        private ViewHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (PCBAFloatView.this.mShow) {
                    PCBAFloatView.this.mShow = false;
                    PCBAFloatView.this.mWindowManager.removeView(PCBAFloatView.this.mTextView);
                }
            } else if (msg.what == 1 && !PCBAFloatView.this.mShow) {
                PCBAFloatView.this.mShow = true;
                PCBAFloatView.this.mWindowManager.addView(PCBAFloatView.this.mTextView, PCBAFloatView.this.wmParams);
            }
        }
    }

    private class ViewThread extends Thread {
        /* synthetic */ ViewThread(PCBAFloatView this$0, ViewThread -this1) {
            this();
        }

        private ViewThread() {
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    PCBAFloatView.this.mHandler.obtainMessage(PCBAFloatView.this.isHome(PCBAFloatView.this.mContext) ? 1 : 0).sendToTarget();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public PCBAFloatView(Context context) {
        Log.d(TAG, TAG);
        this.mContext = context;
        createFloatView();
        registerLocaleChange();
        this.mHandler = new ViewHandler(this, null);
        new ViewThread(this, null).start();
    }

    private void createFloatView() {
        this.mTextView = new TextView(this.mContext);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        DisplayMetrics metric = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metric);
        this.wmParams = new LayoutParams(-2, -2, 2002, 40, -2);
        this.wmParams.gravity = 51;
        this.wmParams.x = metric.widthPixels;
        this.wmParams.y = getStatusBarHeight();
        String mLock = SystemProperties.get("ro.pcba.control", "1");
        if ("0".equals(mLock)) {
            this.mTextView.setText(51249535);
        } else if ("2".equals(mLock)) {
            this.mTextView.setText(51249536);
        }
        this.mTextView.setTextColor(-65536);
        this.mTextView.setTypeface(Typeface.defaultFromStyle(1));
        this.mWindowManager.addView(this.mTextView, this.wmParams);
        this.mShow = true;
    }

    private int getStatusBarHeight() {
        int resourceId = this.mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return this.mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return 50;
    }

    private void registerLocaleChange() {
        this.mContext.registerReceiver(new LocaleChangeReceiver(this, null), new IntentFilter("android.intent.action.LOCALE_CHANGED"));
    }

    private boolean isHome(Context mContext) {
        try {
            ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService("activity");
            List<RunningAppProcessInfo> processInfos = mActivityManager.getRunningAppProcesses();
            if (processInfos == null || (processInfos.isEmpty() ^ 1) == 0) {
                Log.d("isHome", "processInfos is invalid");
                return false;
            }
            PackageManager packageManager = mContext.getPackageManager();
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, 65536);
            for (ResolveInfo ri : resolveInfo) {
                if (ri.activityInfo.processName.equals(((RunningAppProcessInfo) processInfos.get(0)).processName)) {
                    return true;
                }
            }
            List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
            for (ResolveInfo ri2 : resolveInfo) {
                if (ri2.activityInfo.packageName.equals(((RunningTaskInfo) rti.get(0)).topActivity.getPackageName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
