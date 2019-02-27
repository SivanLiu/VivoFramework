package com.vivo.services.epm;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.util.DumpUtils;
import com.vivo.services.epm.config.ConfigurationManagerImpl;
import com.vivo.services.epm.config.ConfigurationObserver;
import com.vivo.services.epm.config.DefaultConfigurationManager;
import com.vivo.services.epm.config.Switch;
import com.vivo.services.epm.util.LogSystemHelper;
import com.vivo.services.epm.util.MessageCenterHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import vivo.app.epm.IExceptionPolicyManager.Stub;

public class ExceptionPolicyManagerService extends Stub {
    private static final String ALL_EXCEPTION_TOAST_WARN_PROP = "debug.epm.allexception.toast";
    private static final boolean DBG = true;
    private static final String EPM_DISABLE_SWITCH_NAME = "epm_disable";
    private static final int MAX_EXCEPTION_HANDLE_TIMEOUT_MILLIS = 5000;
    private static final int MSG_REPORT_EVENT = 0;
    static final String TAG = "EPM";
    private static final int UI_MSG_SHOW_EXCEPTION_TOAST = 0;
    private static ExceptionPolicyManagerService sInstance;
    private boolean isEpmSystemDisable = false;
    private Context mContext;
    private DefaultConfigurationManager mDefaultConfigurationManager;
    private Switch mEpmDisableSwitch;
    private ConfigurationObserver mEpmDisableSwitchObserver = new ConfigurationObserver() {
        public void onConfigChange(String file, String name) {
            Log.d(ExceptionPolicyManagerService.TAG, "file=" + file + " name=" + name);
            Switch w = ExceptionPolicyManagerService.this.mDefaultConfigurationManager.getSwitch(ExceptionPolicyManagerService.EPM_DISABLE_SWITCH_NAME);
            ExceptionPolicyManagerService exceptionPolicyManagerService = ExceptionPolicyManagerService.this;
            boolean isOn = (w == null || (w.isUninitialized() ^ 1) == 0) ? false : w.isOn();
            exceptionPolicyManagerService.isEpmSystemDisable = isOn;
        }
    };
    private Handler mMainHandler;
    private HandlerThread mMainHandlerThread;
    private Handler mUIHandler;

    private final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        EventData data = msg.obj;
                        if (data != null) {
                            BaseExceptionPolicyHandler handler = ExceptionPolicyHandlerRegister.getExceptionPolicyHandler(data.getType());
                            if (handler != null) {
                                handler.recordExceptionEvent(data);
                                long begin = System.currentTimeMillis();
                                handler.handleExceptionEvent(data);
                                long totalTime = System.currentTimeMillis() - begin;
                                if (totalTime > 5000) {
                                    Log.e(ExceptionPolicyManagerService.TAG, "handler " + handler + "costs " + (totalTime / 1000) + " second to handle the event");
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private final class WarningUIHandler extends UIHandler {
        /* synthetic */ WarningUIHandler(ExceptionPolicyManagerService this$0, WarningUIHandler -this1) {
            this();
        }

        private WarningUIHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(ExceptionPolicyManagerService.this.mContext, "exception:" + ((EventData) msg.obj), 1).show();
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized ExceptionPolicyManagerService getInstance(Context context) {
        ExceptionPolicyManagerService exceptionPolicyManagerService;
        synchronized (ExceptionPolicyManagerService.class) {
            if (sInstance == null) {
                sInstance = new ExceptionPolicyManagerService(context);
            }
            exceptionPolicyManagerService = sInstance;
        }
        return exceptionPolicyManagerService;
    }

    private ExceptionPolicyManagerService(Context context) {
        boolean z = false;
        Log.d(TAG, "ExceptionPolicyManagerService construct");
        this.mContext = context;
        this.mUIHandler = new WarningUIHandler(this, null);
        this.mMainHandlerThread = new HandlerThread(TAG);
        this.mMainHandlerThread.start();
        this.mMainHandler = new MainHandler(this.mMainHandlerThread.getLooper());
        try {
            LogSystemHelper.init(context).startFlushData();
            ConfigurationManagerImpl.initConfigurationManagerImpl(this.mContext);
            MessageCenterHelper.initMessageCenterHelper(this.mContext);
            ExceptionPolicyHandlerRegister.registAllPolicyHandlerDefault(this.mContext);
            this.mDefaultConfigurationManager = DefaultConfigurationManager.getInstance();
            this.mEpmDisableSwitch = this.mDefaultConfigurationManager.getSwitch(EPM_DISABLE_SWITCH_NAME);
            if (!(this.mEpmDisableSwitch == null || (this.mEpmDisableSwitch.isUninitialized() ^ 1) == 0)) {
                z = this.mEpmDisableSwitch.isOn();
            }
            this.isEpmSystemDisable = z;
            this.mDefaultConfigurationManager.registerSwitchObserver(this.mEpmDisableSwitch, this.mEpmDisableSwitchObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAllExceptionToast(EventData data) {
        if (SystemProperties.getBoolean(ALL_EXCEPTION_TOAST_WARN_PROP, false)) {
            Message.obtain(this.mUIHandler, 0, data).sendToTarget();
        }
    }

    public void reportEvent(int eventType, long timestamp, String message) {
        if (!this.isEpmSystemDisable) {
            Log.d(TAG, "reportEvent eventType=" + eventType + " timestamp=" + timestamp + "message=" + message);
            EventData data = new EventData(eventType, timestamp, message);
            Message.obtain(this.mMainHandler, 0, data).sendToTarget();
            showAllExceptionToast(data);
        }
    }

    public void reportEventWithMap(int eventType, long timestamp, ContentValues content) {
        if (!this.isEpmSystemDisable) {
            Log.d(TAG, "reportEventWithMap eventType=" + eventType + " timestamp=" + timestamp + "content=" + content);
            EventData data = new EventData(eventType, timestamp, content);
            Message.obtain(this.mMainHandler, 0, data).sendToTarget();
            showAllExceptionToast(data);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("registed exception policy handler");
            pw.println("isEpmSystemDisable=" + this.isEpmSystemDisable);
            ExceptionPolicyHandlerRegister.dump(pw);
            LogSystemHelper.getInstance().dump(pw);
            ConfigurationManagerImpl.getInstance().dump(pw);
        }
    }
}
