package com.vivo.services.epm.policy.jank;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import com.vivo.core.interfaces.messagecore.IDisplayCallback.Stub;
import com.vivo.services.epm.BaseExceptionPolicyHandler;
import com.vivo.services.epm.EventData;
import com.vivo.services.epm.ExceptionType.PerformanceException;
import com.vivo.services.epm.util.AppStoreHelper;
import com.vivo.services.epm.util.AppStoreHelper.IAppVersionCheckCallback;
import com.vivo.services.epm.util.MessageCenterHelper;
import com.vivo.services.epm.util.NotificationMessage;
import com.vivo.services.epm.util.NotificationMessage.DisplayID;
import com.vivo.services.epm.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JankExceptionPolicyHandler extends BaseExceptionPolicyHandler {
    private static final int CLICK_LEFT = 3;
    private static final int CLICK_NOTIFICATION = 1;
    private static final int CLICK_RIGHT = 4;
    private static boolean DEBUG = true;
    public static final String TAG = "JEPH";
    private static final int TYPE_APP_SWITCH = 3;
    private static final int TYPE_DOCKED = 4;
    private static final int TYPE_JANK_APP = 1;
    private static final int TYPE_JANK_GAME = 2;
    private static final int TYPE_NONE = 0;
    private static final int TYPE_PIP = 5;
    private static final int TYPE_RESTART = 1;
    private static final int TYPE_UPDATE = 0;
    private AppRecord mAnotherApp = null;
    private Map<Integer, List<JankData>> mAppJankRecords = new HashMap();
    private Map<Integer, JankData> mAppWarmRecords = new HashMap();
    private List<JankData> mCurrentAppJankDatas = new ArrayList();
    private final Stub mDisplayCallback = new Stub() {
        public void onShow(int messageId, Bundle bundle) throws RemoteException {
            Slog.d(JankExceptionPolicyHandler.TAG, "messageId:" + messageId);
            JankExceptionPolicyHandler.this.mPreMessageId = messageId;
        }

        public void onClick(int messageId, Bundle bundle) throws RemoteException {
            int clickID = bundle.getInt("click");
            Slog.d(JankExceptionPolicyHandler.TAG, "onClick:" + clickID + ",mNotifyType=" + JankExceptionPolicyHandler.this.mNotifyType + ",mNotifyPackage=" + JankExceptionPolicyHandler.this.mNotifyPackage);
            if (JankExceptionPolicyHandler.this.mNotifyPackage != null && JankExceptionPolicyHandler.this.mNotifyPackage.pkgName != null) {
                switch (clickID) {
                    case 1:
                    case 3:
                        Utils.collapsePanels(JankExceptionPolicyHandler.this.mContext);
                        if (JankExceptionPolicyHandler.this.mNotifyType != 0) {
                            if (JankExceptionPolicyHandler.this.mNotifyType == 1) {
                                final String pkgName = JankExceptionPolicyHandler.this.mNotifyPackage.pkgName;
                                ((ActivityManager) JankExceptionPolicyHandler.this.mContext.getSystemService("activity")).forceStopPackage(pkgName);
                                JankExceptionPolicyHandler.this.mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        JankExceptionPolicyHandler.this.mContext.startActivity(JankExceptionPolicyHandler.this.mContext.getPackageManager().getLaunchIntentForPackage(pkgName));
                                    }
                                }, 1000);
                                break;
                            }
                        }
                        AppStoreHelper.gotoAppStoreDetailActivity(JankExceptionPolicyHandler.this.mContext, JankExceptionPolicyHandler.this.mNotifyPackage.pkgName);
                        break;
                        break;
                    case 4:
                        Utils.collapsePanels(JankExceptionPolicyHandler.this.mContext);
                        JankExceptionPolicyHandler.this.mNotWarmUids.add(Integer.valueOf(JankExceptionPolicyHandler.this.mNotifyPackage.uid));
                        break;
                    default:
                        Utils.collapsePanels(JankExceptionPolicyHandler.this.mContext);
                        break;
                }
            }
        }

        public void onCancel(int messageId, Bundle bundle) throws RemoteException {
        }

        public void onError(int errCode, String errMsg, Bundle bundle) throws RemoteException {
        }
    };
    private volatile boolean mDockedExist = false;
    private AppRecord mFocusApp = new AppRecord(1000, "com.bbk.launcher2");
    private Handler mHandler;
    private JankConfiguration mJankConfiguration;
    private List<Integer> mNotWarmUids = new ArrayList();
    private AppRecord mNotifyPackage;
    private int mNotifyType;
    private volatile boolean mPipExist = false;
    private int mPreMessageId = -1;

    final class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    JankData jankData = JankExceptionPolicyHandler.this.createJankData(msg.obj);
                    if (jankData != null) {
                        JankExceptionPolicyHandler.this.handleJank(jankData);
                        return;
                    }
                    return;
                case 3:
                    JankExceptionPolicyHandler.this.handleAppSwitch((EventData) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public JankExceptionPolicyHandler(Context context) {
        super(context);
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        this.mHandler = new H(ht.getLooper());
        this.mJankConfiguration = new JankConfiguration(this.mHandler);
    }

    public void handleExceptionEvent(EventData data) {
        boolean z = true;
        if (!this.mJankConfiguration.isEnable()) {
            Slog.d(TAG, "disable..");
        } else if (data != null && data.getContent() != null) {
            if (DEBUG) {
                Slog.d(TAG, "handleExceptionEvent: data = " + data);
            }
            try {
                if (data.getType() == PerformanceException.EXCEPTION_TYPE_JANK && SystemClock.elapsedRealtime() - data.getTimestamp() <= 10000) {
                    int type = getInt(data.getContent(), "jank_type", 0);
                    if (type == 1 || type == 2) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(type, data));
                    }
                    if (type == 3) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(type, data));
                    } else if (type == 4) {
                        if (getInt(data.getContent(), "exist", 0) != 1) {
                            z = false;
                        }
                        this.mDockedExist = z;
                        if (!this.mDockedExist) {
                            this.mAnotherApp = null;
                        }
                    } else if (type == 5) {
                        if (getInt(data.getContent(), "exist", 0) != 1) {
                            z = false;
                        }
                        this.mPipExist = z;
                        if (!this.mPipExist) {
                            this.mAnotherApp = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAppSwitch(EventData data) {
        ContentValues cv = data.getContent();
        int uid = getInt(cv, "uid", -1);
        int stack = getInt(cv, "stack", -1);
        int cool = getInt(cv, "cool", 1);
        String activity = getString(cv, "activity", null);
        String pkgName = getString(cv, "package", null);
        if (uid >= 0 && pkgName != null && activity != null) {
            AppRecord prePackage = this.mFocusApp;
            if (!pkgName.equals(prePackage.pkgName) || uid != this.mFocusApp.uid) {
                this.mFocusApp = new AppRecord(uid, pkgName);
                this.mFocusApp.init(this.mContext);
                if (this.mDockedExist || this.mPipExist) {
                    this.mAnotherApp = prePackage;
                    return;
                }
                synchronized (this.mCurrentAppJankDatas) {
                    this.mCurrentAppJankDatas.clear();
                }
            }
        }
    }

    private void handleJank(JankData data) {
        if (SystemCheckUtils.isSystemApp(data.uid)) {
            if (DEBUG) {
                Slog.d(TAG, "ignore system jank :" + data.uid);
            }
        } else if (data.uid != this.mFocusApp.uid) {
            if (DEBUG) {
                Slog.d(TAG, "ignore jank because is not same uid:" + data.uid);
            }
        } else if (this.mNotWarmUids.contains(Integer.valueOf(data.uid))) {
            if (DEBUG) {
                Slog.d(TAG, "ignore jank because not warm by user cancel:" + data.uid);
            }
        } else {
            data.application = this.mFocusApp;
            if (this.mJankConfiguration.isInBlackList(data.application.pkgName)) {
                if (DEBUG) {
                    Slog.d(TAG, "isInBlackList:" + data);
                }
                return;
            }
            if (DEBUG) {
                Slog.d(TAG, "handleJank:" + data.uid);
            }
            SystemCheckUtils.collectCpuInfo(data);
            SystemCheckUtils.collectMemInfo(data);
            handleAppJank(data);
            data.reportToServer();
        }
    }

    /* JADX WARNING: Missing block: B:23:0x00ac, code:
            return;
     */
    /* JADX WARNING: Missing block: B:31:0x00e3, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleAppJank(JankData data) {
        if (!data.isCpuOK(this.mJankConfiguration.getJankCpuThreshold())) {
            if (DEBUG) {
                Slog.d(TAG, "ignore app jank because cpu not ok !" + data.uid);
            }
        } else if (data.isMemOK(this.mJankConfiguration.getJankMemoryThreshold())) {
            synchronized (this.mCurrentAppJankDatas) {
                if (this.mAppWarmRecords.containsKey(Integer.valueOf(data.uid))) {
                    if (SystemClock.elapsedRealtime() - ((JankData) this.mAppWarmRecords.get(Integer.valueOf(data.uid))).elapsedRealtime < this.mJankConfiguration.getJankWarmInterval()) {
                        if (DEBUG) {
                            Slog.d(TAG, "ignore app jank because interval !" + data.uid);
                        }
                    }
                }
                List<JankData> list;
                if (this.mCurrentAppJankDatas.size() < this.mJankConfiguration.getJankCount()) {
                    this.mCurrentAppJankDatas.add(data);
                    if (DEBUG) {
                        Slog.d(TAG, "add app jank !" + data.toString());
                    }
                } else if (this.mAppJankRecords.containsKey(Integer.valueOf(data.uid))) {
                    list = (List) this.mAppJankRecords.get(Integer.valueOf(data.uid));
                    if (list.size() <= 0) {
                        list.add(data);
                    } else if (list.size() < this.mJankConfiguration.getJankWarmCount() && ((JankData) list.get(list.size() - 1)).application.startTime != data.application.startTime) {
                        list.add(data);
                        if (DEBUG) {
                            Slog.d(TAG, "add to 2jank count!" + list.size());
                        }
                    }
                    if (list.size() >= this.mJankConfiguration.getJankWarmCount()) {
                        sendMessage(data);
                        this.mAppWarmRecords.put(Integer.valueOf(data.uid), data);
                        this.mAppJankRecords.remove(Integer.valueOf(data.uid));
                    }
                } else {
                    list = new ArrayList();
                    list.add(data);
                    this.mAppJankRecords.put(Integer.valueOf(data.uid), list);
                    if (DEBUG) {
                        Slog.d(TAG, "add to 1jank count!" + list.size());
                    }
                }
            }
        } else {
            if (DEBUG) {
                Slog.d(TAG, "ignore app jank because memory not ok !" + data.uid);
            }
        }
    }

    private void sendMessage(final JankData jankData) {
        if (this.mPreMessageId >= 0) {
            Slog.i(TAG, "mPreMessgeId:" + this.mPreMessageId);
            MessageCenterHelper.getInstance().cancel(DisplayID.DISPLAY_ID_1, this.mPreMessageId, null);
        }
        if (DEBUG) {
            Slog.d(TAG, "sendMessage : " + jankData);
        }
        AppStoreHelper.checkAppVersionAync(jankData.application.pkgName, new IAppVersionCheckCallback() {
            public void appVersionCallback(String packageName, int versionCode) {
                JankExceptionPolicyHandler.this.sendNotify(jankData, jankData.application.versionCode < versionCode);
            }
        });
    }

    private void sendNotify(JankData jankData, boolean hasNewVersion) {
        MessageCenterHelper messageCenterHelper = MessageCenterHelper.getInstance();
        if (messageCenterHelper != null) {
            String content;
            String leftButtonText;
            NotificationMessage msg = new NotificationMessage();
            msg.setPattern(2);
            msg.setOnGoing(0);
            msg.setSound(1);
            msg.setFloating(1);
            if (hasNewVersion) {
                content = String.format(this.mContext.getString(51249702), new Object[]{jankData.application.label});
                leftButtonText = this.mContext.getString(51249698);
                this.mNotifyType = 0;
            } else {
                content = String.format(this.mContext.getString(51249703), new Object[]{jankData.application.label, jankData.application.label});
                leftButtonText = this.mContext.getString(51249704);
                this.mNotifyType = 1;
            }
            msg.setContent(content);
            msg.setTitle(this.mContext.getString(51249693));
            msg.setBtn2(this.mContext.getString(51249700));
            msg.setBtn1(leftButtonText);
            this.mNotifyPackage = jankData.application;
            if (DEBUG) {
                Slog.d(TAG, "sendMessage : hasNewVersion=" + hasNewVersion + ",msg=" + msg);
            }
            messageCenterHelper.send(DisplayID.DISPLAY_ID_1, msg, this.mDisplayCallback);
        }
    }

    private JankData createJankData(EventData data) {
        ContentValues cv = data.getContent();
        int uid = getInt(cv, "uid", -1);
        int jankType = getInt(cv, "jank_type", -1);
        int dropFrames = getInt(cv, "drop_frames", -1);
        long dropTime = getLong(cv, "drop_time", -1);
        if (uid < 0 || jankType < 0) {
            if (DEBUG) {
                Slog.d(TAG, "createJankData : failed!");
            }
            return null;
        }
        JankData jankData = new JankData();
        jankData.uid = uid;
        jankData.type = jankType;
        jankData.dropFrames = dropFrames;
        jankData.dropTime = dropTime;
        return jankData;
    }

    private int getInt(ContentValues cv, String key, int defaultValue) {
        Integer value = cv.getAsInteger(key);
        if (value != null) {
            return value.intValue();
        }
        return defaultValue;
    }

    private long getLong(ContentValues cv, String key, long defaultValue) {
        Long value = cv.getAsLong(key);
        if (value != null) {
            return value.longValue();
        }
        return defaultValue;
    }

    private String getString(ContentValues cv, String key, String defaultValue) {
        String value = cv.getAsString(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}
