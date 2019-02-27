package com.vivo.services.popupcamera;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import com.android.internal.util.DumpUtils;
import com.android.server.UiThread;
import com.vivo.services.epm.config.BaseList;
import com.vivo.services.popupcamera.ApplicationProcessStateHelper.ApplicationProcessStatus;
import com.vivo.services.popupcamera.PopupFrontCameraPermissionHelper.PopupFrontCameraPermissionState;
import com.vivo.services.rms.ProcessList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import vivo.app.popupcamera.IPopupCameraManager.Stub;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public class PopupCameraManagerService extends Stub {
    private static ArrayList<String> BACKGROUND_USE_FRONT_CAMERA_PACKAGE_WHITE_LIST = new ArrayList<String>() {
        {
            add("com.sohu.inputmethod.sogou.vivo");
            add("org.codeaurora.ims");
            add("android.camera.cts");
            add("android.camera.cts.api25test");
            add("com.vivo.findphone");
            add("android");
        }
    };
    private static final int BACK_CAMERA_CLOSED = 4;
    private static final int BACK_CAMERA_OPENED = 2;
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    private static final int CHECK_DELAY_TIME_AFTER_HOME_KEY = 3000;
    private static final boolean DBG = true;
    private static final boolean EMULATE_HOME_KEY_ENALBE = true;
    private static final String FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE = "/data/bbkcore/forbidden_background_popup_front_camera.xml";
    private static final String FRONT_CAMERA_AUDIO_NOTIFICATION_BASE_DIR = "/system/media/audio/ui/";
    private static final int FRONT_CAMERA_CLOSED = 1;
    private static final boolean FRONT_CAMERA_NOTIFICATION_ENABLE = true;
    private static final int FRONT_CAMERA_OPENED = 0;
    private static final String FRONT_CAMERA_PRESSED_ACTION = "vivo.intent.action.FRONT_CAMERA_PRESSED";
    private static final String FRONT_CAMERA_PRESSED_EXTRA_TIME = "press-time";
    private static final int FRONT_CAMERA_TEMPERATURE_OVERHEAD_WARNING_TIMES_IN_MILLIS = 5000;
    private static final int MAX_FRONT_CAMERA_USE_TIMES_FOR_30_SECONDS = 10;
    static final int MSG_BACK_CAMERA_CLOSED = 10;
    static final int MSG_BACK_CAMERA_OPENED = 9;
    static final int MSG_CAMERASERVER_DIED = 14;
    static final int MSG_CHECK_FRONT_CAMERA_IS_CLOSED = 15;
    static final int MSG_DELAY_TAKE_BACK_FRONT_CAMERA_AGAIN = 16;
    static final int MSG_FRONT_CAMERA_CLOSED = 7;
    static final int MSG_FRONT_CAMERA_CLOSED_POLL = 11;
    static final int MSG_FRONT_CAMERA_OPENED = 8;
    static final int MSG_HANDLE_DATA_COLLECT = 17;
    static final int MSG_POLL_STATUS_CANCELED = 0;
    static final int MSG_POLL_STATUS_INVALID = -1;
    static final int MSG_POLL_STATUS_POPUP_JAMMED = 4;
    static final int MSG_POLL_STATUS_POPUP_OK = 2;
    static final int MSG_POLL_STATUS_PRESSED = 5;
    static final int MSG_POLL_STATUS_PUSH_JAMMED = 3;
    static final int MSG_POLL_STATUS_PUSH_OK = 1;
    static final int MSG_POP_UP_FRONT_CAMERA = 13;
    static final int MSG_SCREEN_OFF = 20;
    static final int MSG_SCREEN_ON = 19;
    static final int MSG_TAKE_BACK_FRONT_CAMERA = 12;
    static final int MSG_TAKE_BACK_FRONT_CAMERA_FOR_DROP = 18;
    static final int MSG_UI_FRONT_CAMERA_POPUP_JAMMED = 0;
    static final int MSG_UI_FRONT_CAMERA_PRESSED = 3;
    static final int MSG_UI_FRONT_CAMERA_PUSH_JAMMED = 1;
    static final int MSG_UI_FRONT_CAMERA_TOO_FREQUENT = 2;
    private static ArrayList<String> SHORT_TAKEBACK_PACKAGE_LIST = new ArrayList<String>() {
        {
            add("com.tencent.mm");
            add("com.tencent.mobileqq");
            add("com.alibaba.android.rimet");
            add("im.yixin");
            add("jp.naver.line.android");
            add("com.skype.raider");
            add("com.whatsapp");
            add("com.facebook.katana");
            add("com.kugou.fanxing");
            add("com.duowan.kiwi");
            add("air.tv.douyu.android");
            add("com.meelive.ingkee");
            add("com.kascend.chushou");
            add("tv.xiaoka.live");
            add("com.huajiao");
            add("com.panda.videoliveplatform");
            add("com.tencent.now");
            add("com.smile.gifmaker");
            add("com.ss.android.ugc.aweme");
            add("com.meitu.meiyancamera");
            add("com.mt.mtxx.mtxx");
            add("com.meitu.meipaimv");
            add("com.meitu.wheecam");
        }
    };
    private static final int SHORT_TAKE_BACK_FRONT_CAMERA_TIMEOUT_IN_MILLIS = 2000;
    private static ArrayList<String> SYSTEM_CAMERA_APP_PACKAGE_LIST = new ArrayList<String>() {
        {
            add("com.android.camera");
        }
    };
    static final String TAG = "PopupCameraManagerService";
    private static final int TAKE_BACK_FRONT_CAMERA_AGAIN_AFTER_JAMMED_INTERVALS = 20000;
    private static final int TAKE_BACK_FRONT_CAMERA_MAX_RETRY_TIMES = 5;
    private static final int TAKE_BACK_FRONT_CAMERA_TIMEOUT_IN_MILLIS = 5000;
    private static final int TEMPERATURE_MONITOR_INTERVAL_IN_MILLIS = 30000;
    private static ArrayList<String> WARN_POPUP_CAMERA_PACKAGE_LIST = new ArrayList<String>() {
        {
            add("cn.net.cyberway");
        }
    };
    private static final HashMap<Integer, AudioNotification> mAudioNotificationMap = new HashMap();
    private static PopupCameraManagerService sInstance;
    private LimitQueue<CameraPopupTakebackRecord> cameraPopupAndTakebackRecords = new LimitQueue(10);
    private volatile boolean isCheckingPopupCameraPermission = false;
    private volatile boolean isFrontCameraOpened = false;
    private volatile boolean isFrontCameraPopup = false;
    private boolean isHavingPendingTakeBackTask = false;
    private volatile boolean isLastInGalleryActivity = false;
    private volatile boolean isLastPupupJammed = false;
    private volatile boolean isLastTakebackJammed = false;
    private volatile boolean isScreenOn = true;
    private volatile boolean isSystemShutdowning = false;
    private volatile boolean isTodoFirstClosedPoll = false;
    private CameraStatus mBackCameraStatus;
    private IBinder mCameraServiceBinder;
    private Object mCameraServiceBinderLock = new Object();
    private CameraServerDeathRecipient mCameraServiceDeatchRecipient;
    private Runnable mConfigFileObserveRunnable = new Runnable() {
        public void run() {
            Log.d(PopupCameraManagerService.TAG, "mConfigFileObserveRunnable");
            PopupCameraManagerService.this.parseConfigFile(PopupCameraManagerService.FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE);
            PopupCameraManagerService.this.observeConfigFileChange();
        }
    };
    private Context mContext;
    private int mCurrentFrontCameraStatus = 1;
    private volatile int mCurrentRetryTimes = 0;
    private DataAnalysisHelper mDataAnalysisHelper = null;
    private MySensorEventListener mDropDetectSensorEventListener;
    private int mDropDetectSensorType = 66548;
    private FileObserver mFileObserver;
    private ArrayList<ForbiddenItem> mForbiddenList = new ArrayList();
    private Object mForbiddenListLock = new Object();
    private volatile boolean mForceDelayTakebackFrontCamera;
    private int mFrontCameraPopupStreamId;
    private int mFrontCameraPushStreamId;
    private CameraStatus mFrontCameraStatus;
    private Object mFrontCameraStatusLock = new Object();
    private WakeLock mHallWakeLock;
    private volatile boolean mHavaPendingOpenVibStepTask = false;
    private volatile boolean mHavePendingCloseVibStepTask = false;
    private volatile boolean mHavePendingPopupTask = false;
    private int mLastCloseVibHallCookie = 100000;
    private int mLastFrontCameraStatus = -1;
    private int mLastOpenVibHallCookie = 0;
    private Handler mMainHandler;
    private HandlerThread mMainHandlerThread;
    private CameraStatus mPendingFrontCameraCloseStatus;
    private PendingPopupTask mPendingPopupTask = null;
    private CameraPopupTakebackRecord mPendingRecordAddToQueue;
    private Object mPermissionCheckDialogLock = new Object();
    private FrontCameraErrorDialog mPopupJammedDialog;
    private FrontCameraPressedDialog mPressedDialog;
    private FrontCameraErrorDialog mPushJammedDialog;
    private ScreenStatusReceiver mScreenStatusReceiver;
    private SensorManager mSensorManager;
    private SoundPool mSoundPool;
    private long mTakeBackTaskStartTime = 0;
    private FrontCameraTemperatureProtectDialog mTemperatureProtectDialog;
    private Handler mUIHandler;
    private CameraPopupPermissionCheckDialog permissionCheckDialog;

    private static class AudioNotification {
        public String popupAudioFile;
        public int popupStreamId;
        public String pushedAudioFile;
        public int pushedStreamId;

        public AudioNotification(String popupFile, String pushFile, SoundPool soundPool) {
            this.popupAudioFile = popupFile;
            this.pushedAudioFile = pushFile;
            if (this.popupAudioFile == null || soundPool == null) {
                this.popupStreamId = -1;
            } else {
                this.popupStreamId = soundPool.load(this.popupAudioFile, 1);
            }
            if (this.pushedAudioFile == null || soundPool == null) {
                this.pushedStreamId = -1;
            } else {
                this.pushedStreamId = soundPool.load(this.pushedAudioFile, 1);
            }
        }
    }

    private static class CameraPopupTakebackRecord {
        long popupTimeInMillis;
        long takebackTimeInMillis;

        /* synthetic */ CameraPopupTakebackRecord(CameraPopupTakebackRecord -this0) {
            this();
        }

        private CameraPopupTakebackRecord() {
        }

        public String toString() {
            return "front camera record{ popupTimeInMillis=" + timeMillisToString(this.popupTimeInMillis) + " takebackTimeInMillis=" + timeMillisToString(this.takebackTimeInMillis) + " used_time_millis=" + (this.takebackTimeInMillis - this.popupTimeInMillis) + " }";
        }

        public boolean isValid() {
            return this.popupTimeInMillis > 0 && this.takebackTimeInMillis > 0 && this.takebackTimeInMillis - this.popupTimeInMillis > 0;
        }

        private String timeMillisToString(long timeMillis) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeMillis));
        }
    }

    private final class CameraServerDeathRecipient implements DeathRecipient {
        /* synthetic */ CameraServerDeathRecipient(PopupCameraManagerService this$0, CameraServerDeathRecipient -this1) {
            this();
        }

        private CameraServerDeathRecipient() {
        }

        public void binderDied() {
            Log.d(PopupCameraManagerService.TAG, "we got cameraserver death recipient");
            synchronized (PopupCameraManagerService.this.mCameraServiceBinderLock) {
                PopupCameraManagerService.this.mCameraServiceBinder = null;
            }
            if (PopupCameraManagerService.this.mMainHandler != null) {
                PopupCameraManagerService.this.mMainHandler.sendEmptyMessage(PopupCameraManagerService.MSG_CAMERASERVER_DIED);
            }
        }
    }

    private final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:85:0x051d, code:
            if ((r20 ^ 1) == 0) goto L_0x051f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.d(PopupCameraManagerService.TAG, "MSG_POLL_STATUS_CANCELED");
                    break;
                case 1:
                    Log.d(PopupCameraManagerService.TAG, "front camera pushed ok");
                    PopupCameraManagerService.this.cancelPendingCloseOrOpenVibStebTask(msg.what, msg.arg2);
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                    break;
                case 2:
                    Log.d(PopupCameraManagerService.TAG, "front camera popup ok");
                    PopupCameraManagerService.this.cancelPendingCloseOrOpenVibStebTask(msg.what, msg.arg2);
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                    break;
                case 3:
                    Log.d(PopupCameraManagerService.TAG, "front camera push jammed & add MSG_DELAY_TAKE_BACK_FRONT_CAMERA_AGAIN for try again");
                    PopupCameraManagerService.this.cancelPendingCloseOrOpenVibStebTask(msg.what, msg.arg2);
                    if (hasMessages(16)) {
                        removeMessages(16);
                    }
                    if (PopupCameraManagerService.this.mCurrentRetryTimes < 5) {
                        sendEmptyMessageDelayed(16, 20000);
                    }
                    PopupCameraManagerService.this.mUIHandler.sendMessage(PopupCameraManagerService.this.mUIHandler.obtainMessage(1));
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                    break;
                case 4:
                    Log.d(PopupCameraManagerService.TAG, "front camera popup jammed");
                    PopupCameraManagerService.this.cancelPendingCloseOrOpenVibStebTask(msg.what, msg.arg2);
                    PopupCameraManagerService.this.mUIHandler.sendMessage(PopupCameraManagerService.this.mUIHandler.obtainMessage(0));
                    Log.d(PopupCameraManagerService.TAG, "because popup front camera jammed, we take back front camera immediately");
                    PopupCameraManagerService.this.takeupFrontCameraInternal(true, false);
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                    break;
                case 5:
                    Log.d(PopupCameraManagerService.TAG, "front camera pressed, take back front camera immediately");
                    PopupCameraManagerService.this.cancelPendingCloseOrOpenVibStebTask(msg.what, msg.arg2);
                    PopupCameraManagerService.this.takeupFrontCameraInternal(true, false);
                    PopupCameraManagerService.this.emulatePressHomeKey();
                    if (PopupCameraManagerService.this.isFrontCameraOpened) {
                        sendEmptyMessageDelayed(15, 3000);
                    }
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                    break;
                case 7:
                    Log.d(PopupCameraManagerService.TAG, "MSG_FRONT_CAMERA_CLOSED");
                    PopupCameraManagerService.this.acquireWakeLock(6000);
                    PopupCameraManagerService.this.resetPendingPopupTask();
                    if (hasMessages(15)) {
                        removeMessages(15);
                        Log.d(PopupCameraManagerService.TAG, "front camera is close, we remove MSG_CHECK_FRONT_CAMERA_IS_CLOSED");
                    }
                    if (!(PopupCameraManagerService.this.mPressedDialog == null || PopupCameraManagerService.this.mUIHandler == null)) {
                        PopupCameraManagerService.this.mUIHandler.post(new Runnable() {
                            public void run() {
                                if (PopupCameraManagerService.this.mPressedDialog.isShowing()) {
                                    PopupCameraManagerService.this.mPressedDialog.dismiss();
                                }
                            }
                        });
                    }
                    CameraStatus tmp = msg.obj;
                    boolean isAppForground = ApplicationProcessStateHelper.isApplicationProcessForeground(tmp.currentStatusPackageName).isAppForeground;
                    if (PopupCameraManagerService.this.isFrontCameraPopup) {
                        PopupCameraManagerService.this.mTakeBackTaskStartTime = System.currentTimeMillis();
                        PopupCameraManagerService.this.mPendingFrontCameraCloseStatus = tmp;
                        PopupCameraManagerService.this.isHavingPendingTakeBackTask = true;
                        boolean isInKeyguardMode = PopupCameraManagerService.this.isInKeyguardRestrictedInputMode();
                        boolean isShortTakeback = PopupCameraManagerService.this.isShouldTakebackImmediately(tmp);
                        Log.d(PopupCameraManagerService.TAG, "the app isInShortTakebackList=" + isShortTakeback);
                        if ((PopupCameraManagerService.this.isSystemCameraApp(tmp) || (isAppForground ^ 1) == 0) && !isInKeyguardMode && !isShortTakeback) {
                            Message tmpMsg = obtainMessage(11);
                            PopupCameraManagerService.this.isTodoFirstClosedPoll = true;
                            PopupCameraManagerService.this.isLastInGalleryActivity = false;
                            tmpMsg.obj = tmp;
                            sendMessage(tmpMsg);
                            break;
                        }
                        Log.d(PopupCameraManagerService.TAG, "the third parth app is in background,take back front camera isInKeyguardMode=" + isInKeyguardMode);
                        PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                        break;
                    }
                    break;
                case 8:
                    if (hasMessages(11)) {
                        removeMessages(11);
                        Log.d(PopupCameraManagerService.TAG, "MSG_FRONT_CAMERA_OPENED, we remove MSG_FRONT_CAMERA_CLOSED_POLL");
                    }
                    if (!PopupCameraManagerService.this.isFrontCameraPopup) {
                        CameraStatus tmp3 = msg.obj;
                        boolean isAppForground3 = ApplicationProcessStateHelper.isApplicationProcessForeground(tmp3.currentStatusPackageName).isAppForeground;
                        Log.d(PopupCameraManagerService.TAG, "MSG_FRONT_CAMERA_OPENED isAppForground=" + isAppForground3 + " isScreenOn=" + PopupCameraManagerService.this.isScreenOn);
                        PopupFrontCameraPermissionState ps = PopupFrontCameraPermissionHelper.getFrontCameraPermissionStateFromSettings(PopupCameraManagerService.this.mContext, tmp3.currentStatusPackageName);
                        Log.d(PopupCameraManagerService.TAG, "getFrontCameraPermissionStateFromSettings" + ps);
                        if (!PopupCameraManagerService.this.isShouldForbiddenPopupFrontCamera(tmp3.currentStatusPackageName, ps)) {
                            boolean isBackgroundUseExempted = PopupCameraManagerService.this.isInBackgroundUseExemptionList(tmp3);
                            if (!(isAppForground3 || (isBackgroundUseExempted ^ 1) == 0)) {
                                int i = 1;
                                while (i <= 5) {
                                    try {
                                        Thread.sleep(200);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    isAppForground3 = ApplicationProcessStateHelper.isApplicationProcessForeground(tmp3.currentStatusPackageName).isAppForeground;
                                    Log.d(PopupCameraManagerService.TAG, "check again after " + (i * ProcessList.PERCEPTIBLE_APP_ADJ) + "ms isAppForground3=" + isAppForground3);
                                    if (!isAppForground3) {
                                        i++;
                                    }
                                }
                            }
                            Log.d(PopupCameraManagerService.TAG, "after finally check , isAppForground3=" + isAppForground3 + " isFrontCameraOpened=" + PopupCameraManagerService.this.isFrontCameraOpened);
                            if (PopupCameraManagerService.this.isFrontCameraOpened) {
                                if (!isAppForground3) {
                                    if (isBackgroundUseExempted) {
                                        if (!PopupCameraManagerService.this.isScreenOn) {
                                            Log.d(PopupCameraManagerService.TAG, "screen is off, don't popup front camera now, we will popup when screen on");
                                            PopupCameraManagerService.this.setPendingPopupTask(false, false, tmp3, ps);
                                            break;
                                        }
                                        Log.d(PopupCameraManagerService.TAG, "screen is on, just popup front camera now");
                                        PopupCameraManagerService.this.resetPendingPopupTask();
                                        PopupCameraManagerService.this.popupFrontCameraInternal();
                                        PopupCameraManagerService.this.resetCloseFrontCameraStatus();
                                        break;
                                    }
                                    PopupCameraManagerService.this.popupPermissionConfirmDialog(tmp3, ps, false);
                                    break;
                                }
                                boolean isShouldPopupPermissionDialog = PopupCameraManagerService.this.isShouldWarnUserPopupCameraPermissionDialog(tmp3, ps);
                                if (PopupCameraManagerService.this.isScreenOn) {
                                    if (!isShouldPopupPermissionDialog) {
                                        Log.d(PopupCameraManagerService.TAG, "screen is on, just popup front camera now");
                                        PopupCameraManagerService.this.resetPendingPopupTask();
                                        PopupCameraManagerService.this.popupFrontCameraInternal();
                                        PopupCameraManagerService.this.resetCloseFrontCameraStatus();
                                        break;
                                    }
                                    Log.d(PopupCameraManagerService.TAG, tmp3.currentStatusPackageName + " is in warn list for popup camera");
                                    PopupCameraManagerService.this.popupPermissionConfirmDialog(tmp3, ps, true);
                                    break;
                                }
                                Log.d(PopupCameraManagerService.TAG, "screen is off, don't popup front camera now, we will popup when screen on");
                                PopupCameraManagerService.this.setPendingPopupTask(isShouldPopupPermissionDialog, true, tmp3, ps);
                                break;
                            }
                            Log.d(PopupCameraManagerService.TAG, "after finally check , the front camera is closed, we just return...");
                            return;
                        }
                        Log.d(PopupCameraManagerService.TAG, "the app " + tmp3.currentStatusPackageName + "is not allowed to popup front camera, ignore the popup");
                        break;
                    }
                    Log.d(PopupCameraManagerService.TAG, "front camera is popuped, we just return ,need do nothing for MSG_FRONT_CAMERA_OPENED");
                    return;
                case 9:
                    if (PopupCameraManagerService.this.mPendingFrontCameraCloseStatus != null) {
                        if (hasMessages(7)) {
                            removeMessages(7);
                        }
                        if (hasMessages(11)) {
                            removeMessages(11);
                        }
                        Log.d(PopupCameraManagerService.TAG, "becuase back camera opened , we take back front camera immediately");
                        PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                    }
                    if (PopupCameraManagerService.this.isLastTakebackJammed) {
                        Log.d(PopupCameraManagerService.TAG, "the back camera is opened and the last takeback front camera is jammed, we try take back again");
                        PopupCameraManagerService.this.takeupFrontCameraInternal(true, true);
                        break;
                    }
                    break;
                case 11:
                    Log.d(PopupCameraManagerService.TAG, "MSG_FRONT_CAMERA_CLOSED_POLL again");
                    if (PopupCameraManagerService.this.isFrontCameraPopup) {
                        CameraStatus tmp2 = msg.obj;
                        ApplicationProcessStatus aps = ApplicationProcessStateHelper.isApplicationProcessForeground(tmp2.currentStatusPackageName);
                        boolean isAppForground2 = aps.isAppForeground;
                        boolean isInSystemGallery = aps.isInGalleryActivity;
                        int isStillInGallery = (isInSystemGallery && PopupCameraManagerService.this.isLastInGalleryActivity) ? true : (!isInSystemGallery || (PopupCameraManagerService.this.isLastInGalleryActivity ^ 1) == 0) ? false : PopupCameraManagerService.this.isTodoFirstClosedPoll;
                        PopupCameraManagerService.this.isLastInGalleryActivity = isInSystemGallery;
                        PopupCameraManagerService.this.isTodoFirstClosedPoll = false;
                        boolean isInKeyguardMode2 = PopupCameraManagerService.this.isInKeyguardRestrictedInputMode();
                        if (!isAppForground2) {
                            if (!PopupCameraManagerService.this.isSystemCameraApp(tmp2)) {
                                isStillInGallery = 0;
                            }
                            break;
                        }
                        if (!isInKeyguardMode2) {
                            if (System.currentTimeMillis() - PopupCameraManagerService.this.mTakeBackTaskStartTime <= 5000) {
                                Message pollAgainMsg = obtainMessage(11);
                                pollAgainMsg.obj = tmp2;
                                sendMessageDelayed(pollAgainMsg, 500);
                                break;
                            }
                            Log.d(PopupCameraManagerService.TAG, "we have poll more than  5000ms , we take back front camera immediately");
                            PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                            break;
                        }
                        Log.d(PopupCameraManagerService.TAG, "the app is in background,take back front camera immediately isInKeyguardMode=" + isInKeyguardMode2);
                        PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                        break;
                    }
                    break;
                case 12:
                    PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                    break;
                case 13:
                    PopupCameraManagerService.this.popupFrontCameraInternal();
                    break;
                case PopupCameraManagerService.MSG_CAMERASERVER_DIED /*14*/:
                    if (PopupCameraManagerService.this.mPendingFrontCameraCloseStatus == null) {
                        if (PopupCameraManagerService.this.isFrontCameraPopup) {
                            Log.d(PopupCameraManagerService.TAG, "take back front camera even through the front camera is opened");
                            PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                            break;
                        }
                    }
                    if (hasMessages(7)) {
                        removeMessages(7);
                    }
                    if (hasMessages(11)) {
                        removeMessages(11);
                    }
                    Log.d(PopupCameraManagerService.TAG, "have pending close task,becuase cameraserver died , we take back front camera immediately");
                    PopupCameraManagerService.this.takeupFrontCameraInternal(false, false);
                    break;
                    break;
                case 15:
                    if (!PopupCameraManagerService.this.isFrontCameraOpened) {
                        Log.d(PopupCameraManagerService.TAG, "after emulate home key , the front camera is closed, do nothing");
                        break;
                    }
                    Log.d(PopupCameraManagerService.TAG, "after emulate home key , the front camera is opened, notify user");
                    if (PopupCameraManagerService.this.mUIHandler != null) {
                        PopupCameraManagerService.this.mUIHandler.sendMessage(PopupCameraManagerService.this.mUIHandler.obtainMessage(3));
                        break;
                    }
                    break;
                case 16:
                    Log.d(PopupCameraManagerService.TAG, "after 20s, try takeback front camera again time " + (PopupCameraManagerService.this.mCurrentRetryTimes + 1));
                    if (!PopupCameraManagerService.this.isFrontCameraOpened && PopupCameraManagerService.this.isLastTakebackJammed) {
                        PopupCameraManagerService popupCameraManagerService = PopupCameraManagerService.this;
                        popupCameraManagerService.mCurrentRetryTimes = popupCameraManagerService.mCurrentRetryTimes + 1;
                        PopupCameraManagerService.this.takeupFrontCameraInternal(true, true);
                        break;
                    }
                case PopupCameraManagerService.MSG_HANDLE_DATA_COLLECT /*17*/:
                    if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.handleDataCollect(msg.obj);
                        break;
                    }
                    break;
                case PopupCameraManagerService.MSG_TAKE_BACK_FRONT_CAMERA_FOR_DROP /*18*/:
                    Log.d(PopupCameraManagerService.TAG, "MSG_TAKE_BACK_FRONT_CAMERA_FOR_DROP");
                    PopupCameraManagerService.this.takeupFrontCameraInternalAfterFalling();
                    if (!(PopupCameraManagerService.this.isFrontCameraPopup || PopupCameraManagerService.this.mDataAnalysisHelper == null)) {
                        PopupCameraManagerService.this.mDataAnalysisHelper.gatherCounts(msg.what);
                        break;
                    }
                case PopupCameraManagerService.MSG_SCREEN_ON /*19*/:
                    Log.d(PopupCameraManagerService.TAG, "MSG_SCREEN_ON");
                    if (PopupCameraManagerService.this.isFrontCameraOpened && (PopupCameraManagerService.this.isFrontCameraPopup ^ 1) != 0 && PopupCameraManagerService.this.mHavePendingPopupTask && PopupCameraManagerService.this.mPendingPopupTask != null) {
                        if (!PopupCameraManagerService.this.mPendingPopupTask.mPendingPopupTaskNeedPopupPermissionDialog) {
                            Log.d(PopupCameraManagerService.TAG, "have pending popup task, no need check permission ,just popup now");
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            PopupCameraManagerService.this.popupFrontCameraInternal();
                            PopupCameraManagerService.this.resetCloseFrontCameraStatus();
                            break;
                        }
                        Log.d(PopupCameraManagerService.TAG, "have pending popup task,need check permission");
                        PopupCameraManagerService.this.popupPermissionConfirmDialog(PopupCameraManagerService.this.mPendingPopupTask.mPendingPopupCameraStatus, PopupCameraManagerService.this.mPendingPopupTask.mPermissionState, PopupCameraManagerService.this.mPendingPopupTask.isShowCheckbox);
                        break;
                    }
            }
        }
    }

    private final class MySensorEventListener implements SensorEventListener {
        private static final int MIN_DROP_INTERNAL = 2000;
        private long lastDropEventTime;

        /* synthetic */ MySensorEventListener(PopupCameraManagerService this$0, MySensorEventListener -this1) {
            this();
        }

        private MySensorEventListener() {
            this.lastDropEventTime = 0;
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            int type = sensorEvent.sensor.getType();
            Log.d(PopupCameraManagerService.TAG, "onSensorChanged type=" + type);
            if (type == PopupCameraManagerService.this.mDropDetectSensorType && PopupCameraManagerService.this.mMainHandler != null) {
                PopupCameraManagerService.this.mMainHandler.sendEmptyMessage(PopupCameraManagerService.MSG_TAKE_BACK_FRONT_CAMERA_FOR_DROP);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.d(PopupCameraManagerService.TAG, "onAccuracyChanged");
        }
    }

    private static class PendingPopupTask {
        public boolean isShowCheckbox;
        public CameraStatus mPendingPopupCameraStatus;
        public boolean mPendingPopupTaskNeedPopupPermissionDialog;
        public PopupFrontCameraPermissionState mPermissionState;

        public PendingPopupTask(boolean showDialog, boolean showCheckBox, CameraStatus status, PopupFrontCameraPermissionState state) {
            this.mPendingPopupTaskNeedPopupPermissionDialog = showDialog;
            this.isShowCheckbox = showCheckBox;
            this.mPendingPopupCameraStatus = status;
            this.mPermissionState = state;
        }
    }

    private final class ScreenStatusReceiver extends BroadcastReceiver {
        private static final String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
        private static final String SCREEN_ON = "android.intent.action.SCREEN_ON";

        /* synthetic */ ScreenStatusReceiver(PopupCameraManagerService this$0, ScreenStatusReceiver -this1) {
            this();
        }

        private ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!PopupCameraManagerService.this.isFrontCameraOpened && PopupCameraManagerService.this.isCheckingPopupCameraPermission && PopupCameraManagerService.this.permissionCheckDialog != null && PopupCameraManagerService.this.permissionCheckDialog.isShowing()) {
                Log.d(PopupCameraManagerService.TAG, "dismiss the last permission check dialog");
                PopupCameraManagerService.this.permissionCheckDialog.dismiss();
            }
            if (SCREEN_ON.equals(intent.getAction())) {
                Log.d(PopupCameraManagerService.TAG, "receive screen on broadcast isFrontCameraOpened=" + PopupCameraManagerService.this.isFrontCameraOpened + " isFrontCameraPopup=" + PopupCameraManagerService.this.isFrontCameraPopup + " mHavePendingPopupTask=" + PopupCameraManagerService.this.mHavePendingPopupTask);
                PopupCameraManagerService.this.isScreenOn = true;
                if (PopupCameraManagerService.this.mMainHandler != null) {
                    PopupCameraManagerService.this.mMainHandler.sendEmptyMessage(PopupCameraManagerService.MSG_SCREEN_ON);
                }
            } else if (SCREEN_OFF.equals(intent.getAction())) {
                Log.d(PopupCameraManagerService.TAG, "receive screen off broadcast");
                PopupCameraManagerService.this.isScreenOn = false;
            }
        }
    }

    private final class ShutDownBootReceiver extends BroadcastReceiver {
        /* synthetic */ ShutDownBootReceiver(PopupCameraManagerService this$0, ShutDownBootReceiver -this1) {
            this();
        }

        private ShutDownBootReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                Log.d(PopupCameraManagerService.TAG, "receive shutdown broadcast");
                PopupCameraManagerService.this.isSystemShutdowning = true;
                if (PopupCameraManagerService.this.mPendingFrontCameraCloseStatus != null) {
                    if (PopupCameraManagerService.this.mMainHandler.hasMessages(7)) {
                        PopupCameraManagerService.this.mMainHandler.removeMessages(7);
                    }
                    if (PopupCameraManagerService.this.mMainHandler.hasMessages(11)) {
                        PopupCameraManagerService.this.mMainHandler.removeMessages(11);
                    }
                    if (PopupCameraManagerService.this.mMainHandler.hasMessages(16)) {
                        PopupCameraManagerService.this.mMainHandler.removeMessages(16);
                    }
                    Log.d(PopupCameraManagerService.TAG, "becuase system shutdown, we take back front camera immediately");
                    PopupCameraManagerService.this.takeupFrontCameraInternal(true, false);
                } else if (PopupCameraManagerService.this.isFrontCameraPopup) {
                    Log.d(PopupCameraManagerService.TAG, "becuase system shutdown, we take back front camera immediately even if the front camera is opened");
                    PopupCameraManagerService.this.takeupFrontCameraInternal(true, false);
                }
                if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                    PopupCameraManagerService.this.mDataAnalysisHelper.notifyShutdownBroadcast();
                }
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                Log.d(PopupCameraManagerService.TAG, "receive bootcomplected broadcast");
                if (PopupCameraManagerService.this.mDataAnalysisHelper != null) {
                    PopupCameraManagerService.this.mDataAnalysisHelper.notifyBootCompletedBroadcast();
                }
            }
        }
    }

    private final class UIHandler extends Handler {
        public UIHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (PopupCameraManagerService.this.mPopupJammedDialog == null || !(PopupCameraManagerService.this.mPopupJammedDialog == null || (PopupCameraManagerService.this.mPopupJammedDialog.isShowing() ^ 1) == 0)) {
                        PopupCameraManagerService.this.mPopupJammedDialog = new FrontCameraErrorDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249633));
                        PopupCameraManagerService.this.mPopupJammedDialog.show();
                        return;
                    }
                    return;
                case 1:
                    if (PopupCameraManagerService.this.mPushJammedDialog == null || !(PopupCameraManagerService.this.mPushJammedDialog == null || (PopupCameraManagerService.this.mPushJammedDialog.isShowing() ^ 1) == 0)) {
                        PopupCameraManagerService.this.mPushJammedDialog = new FrontCameraErrorDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249635));
                        PopupCameraManagerService.this.mPushJammedDialog.show();
                        return;
                    }
                    return;
                case 2:
                    if (PopupCameraManagerService.this.mTemperatureProtectDialog == null || !(PopupCameraManagerService.this.mTemperatureProtectDialog == null || (PopupCameraManagerService.this.mTemperatureProtectDialog.isShowing() ^ 1) == 0)) {
                        PopupCameraManagerService.this.mTemperatureProtectDialog = new FrontCameraTemperatureProtectDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249632), PopupCameraManagerService.this.mContext.getString(51249638));
                        PopupCameraManagerService.this.mTemperatureProtectDialog.show();
                        return;
                    }
                    return;
                case 3:
                    if (PopupCameraManagerService.this.mPressedDialog == null || !(PopupCameraManagerService.this.mPressedDialog == null || (PopupCameraManagerService.this.mPressedDialog.isShowing() ^ 1) == 0)) {
                        PopupCameraManagerService.this.mPressedDialog = new FrontCameraPressedDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249634), PopupCameraManagerService.this);
                        PopupCameraManagerService.this.mPressedDialog.show();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void resetPendingPopupTask() {
        this.mHavePendingPopupTask = false;
        this.mPendingPopupTask = null;
    }

    private void setPendingPopupTask(boolean showDialog, boolean showCheckBox, CameraStatus status, PopupFrontCameraPermissionState state) {
        this.mHavePendingPopupTask = true;
        this.mPendingPopupTask = new PendingPopupTask(showDialog, showCheckBox, status, state);
    }

    private PopupCameraManagerService(Context context) {
        Log.d(TAG, "PopupCameraManagerService construct");
        this.mContext = context;
        this.mFrontCameraStatus = new CameraStatus(1);
        this.mBackCameraStatus = new CameraStatus(0);
        this.mMainHandlerThread = new HandlerThread(TAG);
        this.mMainHandlerThread.start();
        this.mMainHandler = new MainHandler(this.mMainHandlerThread.getLooper());
        this.mUIHandler = new UIHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(new ShutDownBootReceiver(this, null), filter, null, this.mMainHandler);
        registSreenStatusReceiver();
        this.mCameraServiceDeatchRecipient = new CameraServerDeathRecipient(this, null);
        connectCameraServiceLocked();
        VibHallWrapper.initVibHallWrapper(this.mMainHandler);
        initAudioNotificationMap();
        if (this.mUIHandler != null) {
            this.mUIHandler.post(new Runnable() {
                public void run() {
                    PopupCameraManagerService.this.mPopupJammedDialog = new FrontCameraErrorDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249633));
                    PopupCameraManagerService.this.mPushJammedDialog = new FrontCameraErrorDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249635));
                    PopupCameraManagerService.this.mTemperatureProtectDialog = new FrontCameraTemperatureProtectDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249632), PopupCameraManagerService.this.mContext.getString(51249638));
                    PopupCameraManagerService.this.mPressedDialog = new FrontCameraPressedDialog(PopupCameraManagerService.this.mContext, true, PopupCameraManagerService.this.mContext.getString(51249634), PopupCameraManagerService.this);
                }
            });
        }
        this.mHallWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "PopupCameraManagerService-Hall-WakeLock");
        this.mHallWakeLock.setReferenceCounted(false);
        readForbiddenBackgroundPopupFrontCameraConfig();
        this.mDropDetectSensorType = getDropDetectSensorTypeByReflect();
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mDropDetectSensorEventListener = new MySensorEventListener(this, null);
        if (!(this.mDropDetectSensorType == -1 || this.mSensorManager == null)) {
            Sensor dropDetectSensor = this.mSensorManager.getDefaultSensor(this.mDropDetectSensorType);
            if (dropDetectSensor != null) {
                Log.d(TAG, "registerListener for drop detect");
                this.mSensorManager.registerListener(this.mDropDetectSensorEventListener, dropDetectSensor, 500000);
            }
        }
        this.mDataAnalysisHelper = DataAnalysisHelper.getInstance(this.mContext, this.mMainHandler);
    }

    private int getDropDetectSensorTypeByReflect() {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField("TYPE_DROP_DET").getInt(sensorClass);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void registSreenStatusReceiver() {
        this.mScreenStatusReceiver = new ScreenStatusReceiver(this, null);
        IntentFilter screenStatusIF = new IntentFilter();
        screenStatusIF.addAction("android.intent.action.SCREEN_ON");
        screenStatusIF.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenStatusReceiver, screenStatusIF, null, this.mUIHandler);
    }

    private void readForbiddenBackgroundPopupFrontCameraConfig() {
        parseConfigFile(FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE);
        observeConfigFileChange();
    }

    private ArrayList<ForbiddenItem> parseConfigFromXml(InputStream is) {
        ArrayList<ForbiddenItem> tmp = new ArrayList();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            try {
                parser.setInput(new InputStreamReader(is));
                while (parser.getEventType() != 1) {
                    try {
                        if (parser.getEventType() == 2 && BaseList.STANDARD_LIST_ITEM_TAG.equalsIgnoreCase(parser.getName()) && "package".equalsIgnoreCase(parser.getAttributeName(0)) && "mode".equalsIgnoreCase(parser.getAttributeName(1))) {
                            tmp.add(new ForbiddenItem(parser.getAttributeValue(0), Integer.parseInt(parser.getAttributeValue(1))));
                        }
                        parser.next();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return tmp;
                    }
                }
                return tmp;
            } catch (Exception e2) {
                e2.printStackTrace();
                return tmp;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            return tmp;
        }
    }

    private void parseConfigFile(String filePath) {
        try {
            String result = FileUtils.readTextFile(new File(filePath), 0, null);
            if (result != null) {
                Log.d(TAG, "result = " + result);
                synchronized (this.mForbiddenListLock) {
                    this.mForbiddenList = parseConfigFromXml(new ByteArrayInputStream(result.getBytes()));
                }
            }
        } catch (Exception e) {
            this.mForbiddenList = null;
            Log.e(TAG, "parseConfigFile error! " + e.fillInStackTrace());
        }
    }

    private boolean isShouldForbiddenPopupFrontCamera(String packageName, PopupFrontCameraPermissionState ps) {
        return (ps == null || (ps.isPopupFrontCameraPermissionGranted() ^ 1) == 0 || !ps.isAlwaysDeny()) ? false : ps.isPermissionStateValid();
    }

    private boolean isShouldTakebackImmediately(CameraStatus status) {
        if (status == null || status.currentStatusPackageName == null) {
            return false;
        }
        if (isInShortTakebackPackageList(status)) {
            return true;
        }
        synchronized (this.mForbiddenListLock) {
            if (this.mForbiddenList != null) {
                for (ForbiddenItem item : this.mForbiddenList) {
                    if (item != null && item.packageName != null && item.packageName.equalsIgnoreCase(status.currentStatusPackageName) && item.mode == 4) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isShouldWarnUserPopupCameraPermissionDialog(CameraStatus status, PopupFrontCameraPermissionState ps) {
        if (status == null || status.currentStatusPackageName == null || ps == null || !ps.isPermissionStateValid() || ps.isPopupFrontCameraPermissionGranted() || ps.isAlwaysDeny()) {
            return false;
        }
        return true;
    }

    private boolean isInBackgroundUseExemptionList(CameraStatus status) {
        if (status == null || status.currentStatusPackageName == null) {
            return false;
        }
        if (isSystemApp(status.currentStatusPackageName)) {
            Log.d(TAG, "system app " + status.currentStatusPackageName + " is in background exemption list");
            return true;
        } else if (BACKGROUND_USE_FRONT_CAMERA_PACKAGE_WHITE_LIST.contains(status.currentStatusPackageName)) {
            return true;
        } else {
            synchronized (this.mForbiddenListLock) {
                if (this.mForbiddenList != null) {
                    for (ForbiddenItem item : this.mForbiddenList) {
                        if (item != null && item.packageName != null && item.packageName.equalsIgnoreCase(status.currentStatusPackageName) && item.mode == 5) {
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            }
        }
    }

    private void observeConfigFileChange() {
        Log.d(TAG, "observeConfigFileChange");
        if (this.mFileObserver != null) {
            this.mFileObserver.stopWatching();
        }
        File file = new File(FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE);
        try {
            if (!file.exists()) {
                Log.d(TAG, "/data/bbkcore/forbidden_background_popup_front_camera.xml file not exist ,create new one");
                file.createNewFile();
            }
        } catch (Exception e) {
            Log.e(TAG, "observeConfigFileChange create file error");
        }
        this.mFileObserver = new FileObserver(FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE, 1544) {
            public void onEvent(int event, String path) {
                Log.d(PopupCameraManagerService.TAG, "onEvent=" + event + " path=" + path);
                if (8 == event) {
                    Log.d(PopupCameraManagerService.TAG, "get CLOSE_WRITE event, parse config again");
                    PopupCameraManagerService.this.parseConfigFile(PopupCameraManagerService.FORBIDDEN_BACKGROUND_POPUP_FRONT_CAMERA_CONFIG_FILE);
                }
                if (event == 1024 || event == ProcessStates.PAUSING) {
                    Log.d(PopupCameraManagerService.TAG, "get DELETE event, delay parse config & watch file again");
                    PopupCameraManagerService.this.mMainHandler.removeCallbacks(PopupCameraManagerService.this.mConfigFileObserveRunnable);
                    PopupCameraManagerService.this.mMainHandler.postDelayed(PopupCameraManagerService.this.mConfigFileObserveRunnable, 5000);
                }
            }
        };
        this.mFileObserver.startWatching();
    }

    private void initAudioNotificationMap() {
        this.mSoundPool = new SoundPool(10, 7, 0);
        mAudioNotificationMap.put(Integer.valueOf(0), new AudioNotification(null, null, this.mSoundPool));
        mAudioNotificationMap.put(Integer.valueOf(1), new AudioNotification("/system/media/audio/ui/front_camera_sciencefiction_popuped.ogg", "/system/media/audio/ui/front_camera_sciencefiction_pushed.ogg", this.mSoundPool));
        mAudioNotificationMap.put(Integer.valueOf(2), new AudioNotification("/system/media/audio/ui/front_camera_mechanical_popuped.ogg", "/system/media/audio/ui/front_camera_mechanical_pushed.ogg", this.mSoundPool));
        mAudioNotificationMap.put(Integer.valueOf(3), new AudioNotification("/system/media/audio/ui/front_camera_rhythm_popuped.ogg", "/system/media/audio/ui/front_camera_rhythm_pushed.ogg", this.mSoundPool));
    }

    private int getFrontCameraNotificationModeFromSettings() {
        int mode = System.getInt(this.mContext.getContentResolver(), "telescopic_camera_sound", 1);
        Log.d(TAG, "getFrontCameraNotificationModeFromSettings mode = " + mode);
        return mode;
    }

    private void playFrontCameraPopupAudio() {
        AudioNotification notify = (AudioNotification) mAudioNotificationMap.get(Integer.valueOf(getFrontCameraNotificationModeFromSettings()));
        if (this.mSoundPool != null && notify != null && notify.popupStreamId > 0) {
            this.mSoundPool.play(notify.popupStreamId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void playFrontCameraPushAudio() {
        AudioNotification notify = (AudioNotification) mAudioNotificationMap.get(Integer.valueOf(getFrontCameraNotificationModeFromSettings()));
        if (this.mSoundPool != null && notify != null && notify.pushedStreamId > 0) {
            this.mSoundPool.play(notify.pushedStreamId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void connectCameraServiceLocked() {
        synchronized (this.mCameraServiceBinderLock) {
            if (this.mCameraServiceBinder == null) {
                this.mCameraServiceBinder = ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME);
                if (this.mCameraServiceBinder == null) {
                    Log.d(TAG, "mCameraServiceBinder is null");
                } else {
                    try {
                        this.mCameraServiceBinder.linkToDeath(this.mCameraServiceDeatchRecipient, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.d(TAG, "we can not linkToDeath to cameraserver");
                    }
                }
            }
        }
        return;
    }

    public static synchronized PopupCameraManagerService getInstance(Context context) {
        PopupCameraManagerService popupCameraManagerService;
        synchronized (PopupCameraManagerService.class) {
            if (sInstance == null) {
                sInstance = new PopupCameraManagerService(context);
            }
            popupCameraManagerService = sInstance;
        }
        return popupCameraManagerService;
    }

    public boolean popupFrontCamera() {
        Log.d(TAG, "popupFrontCamera");
        if (this.mMainHandler == null) {
            return false;
        }
        this.mMainHandler.sendEmptyMessage(13);
        return true;
    }

    public boolean takeupFrontCamera() {
        Log.d(TAG, "takeupFrontCamera");
        if (this.mMainHandler == null) {
            return false;
        }
        this.mMainHandler.sendEmptyMessage(12);
        return true;
    }

    private int calculateOpenCookie() {
        int i = this.mLastOpenVibHallCookie + 1;
        this.mLastOpenVibHallCookie = i;
        return i;
    }

    private int calculateCloseCookie() {
        int i = this.mLastCloseVibHallCookie + 1;
        this.mLastCloseVibHallCookie = i;
        return i;
    }

    private void recordPopupFrontCamera() {
        long openingTime = SystemClock.elapsedRealtime();
        if (this.cameraPopupAndTakebackRecords == null || this.cameraPopupAndTakebackRecords.size() != 10 || openingTime - ((CameraPopupTakebackRecord) this.cameraPopupAndTakebackRecords.getFirst()).popupTimeInMillis >= 30000) {
            Log.d(TAG, "current popup does not trigger temperature monitor");
        } else {
            Log.d(TAG, "current popup trigger temperature monitor");
            this.mForceDelayTakebackFrontCamera = true;
            this.mUIHandler.sendEmptyMessage(2);
        }
        if (this.mPendingRecordAddToQueue != null) {
            Log.d(TAG, "there is maybe something wrong, mPendingRecordAddToQueue is non-null, but open the front camera again,we just skip the last");
            this.mPendingRecordAddToQueue = null;
        }
        Log.d(TAG, "record front camera opened time");
        this.mPendingRecordAddToQueue = new CameraPopupTakebackRecord();
        this.mPendingRecordAddToQueue.popupTimeInMillis = openingTime;
    }

    private void recordTakebackFrontCamera() {
        if (this.mPendingRecordAddToQueue != null) {
            this.mPendingRecordAddToQueue.takebackTimeInMillis = SystemClock.elapsedRealtime();
            if (this.mPendingRecordAddToQueue.isValid()) {
                Log.d(TAG, "put CameraPopupTakebackRecord " + this.mPendingRecordAddToQueue + "to queue");
                this.cameraPopupAndTakebackRecords.offer(this.mPendingRecordAddToQueue);
                this.mPendingRecordAddToQueue = null;
                return;
            }
            return;
        }
        Log.d(TAG, "there is maybe something wrong, we get takeback camera event , but mPendingRecordAddToQueue is null ,don't record this close event");
    }

    public boolean popupFrontCameraInternal() {
        boolean z = true;
        Log.d(TAG, "popupFrontCameraInternal isFrontCameraPopup=" + this.isFrontCameraPopup);
        if (this.isFrontCameraPopup) {
            return false;
        }
        this.isFrontCameraPopup = true;
        playFrontCameraPopupAudio();
        this.mHavaPendingOpenVibStepTask = true;
        this.isLastPupupJammed = false;
        this.isLastTakebackJammed = false;
        this.mCurrentRetryTimes = 0;
        recordPopupFrontCamera();
        if (VibHallWrapper.openStepVibrator(calculateOpenCookie()) == -1) {
            z = false;
        }
        return z;
    }

    public boolean takeupFrontCameraInternal(boolean isForce, boolean silent) {
        boolean z = true;
        Log.d(TAG, "takeupFrontCameraInternal isFrontCameraPopup=" + this.isFrontCameraPopup);
        resetCloseFrontCameraStatus();
        if (!this.isFrontCameraPopup && !isForce) {
            return false;
        }
        acquireWakeLock(5000);
        this.isFrontCameraPopup = false;
        if (!silent) {
            playFrontCameraPushAudio();
        }
        this.mHavePendingCloseVibStepTask = true;
        this.isLastTakebackJammed = false;
        this.isLastPupupJammed = false;
        recordTakebackFrontCamera();
        if (VibHallWrapper.closeStepVibrator(calculateCloseCookie()) == -1) {
            z = false;
        }
        return z;
    }

    public boolean takeupFrontCameraInternalAfterFalling() {
        boolean z = false;
        Log.d(TAG, "takeupFrontCameraInternalAfterFalling isFrontCameraPopup=" + this.isFrontCameraPopup);
        if (this.isFrontCameraPopup) {
            return false;
        }
        acquireWakeLock(5000);
        if (VibHallWrapper.closeStepVibratorAfterFalling() != -1) {
            z = true;
        }
        return z;
    }

    public int getFrontCameraStatus() {
        Log.d(TAG, "getFrontCameraStatus");
        if (this.mFrontCameraStatus != null) {
            return this.mFrontCameraStatus.getCameraStatus();
        }
        return -1;
    }

    private void resetCloseFrontCameraStatus() {
        this.mPendingFrontCameraCloseStatus = null;
        this.mTakeBackTaskStartTime = 0;
        this.isHavingPendingTakeBackTask = false;
    }

    private void handleFrontCameraOpened(CameraStatus status) {
        Log.d(TAG, "handleFrontCameraOpened");
        if (this.mMainHandler.hasMessages(7)) {
            Log.d(TAG, "becuaseof open again, we remove message MSG_FRONT_CAMERA_CLOSED");
            this.mMainHandler.removeMessages(7);
        }
        if (this.mMainHandler.hasMessages(11)) {
            Log.d(TAG, "becuaseof open again, we remove message MSG_FRONT_CAMERA_CLOSED_POLL");
            this.mMainHandler.removeMessages(11);
        }
        if (this.mMainHandler.hasMessages(16)) {
            Log.d(TAG, "becuaseof open again, we remove message MSG_DELAY_TAKE_BACK_FRONT_CAMERA_AGAIN");
            this.mMainHandler.removeMessages(16);
        }
        Message msg = this.mMainHandler.obtainMessage(8);
        msg.obj = status;
        this.mMainHandler.sendMessage(msg);
    }

    private void handleFrontCameraClosed(CameraStatus status) {
        Log.d(TAG, "handleFrontCameraClosed");
        if (this.isCheckingPopupCameraPermission && this.permissionCheckDialog != null) {
            Log.d(TAG, "get front camera closed event, but the last open event for check popup permission is doing,we will cancel the check task");
            this.permissionCheckDialog.cancelPermissionCheck();
        }
        Message msg = this.mMainHandler.obtainMessage(7);
        msg.obj = status;
        this.mMainHandler.sendMessage(msg);
    }

    private void handleBackCameraOpened(CameraStatus status) {
        Log.d(TAG, "handleBackCameraOpened");
        Message msg = this.mMainHandler.obtainMessage(9);
        msg.obj = status;
        this.mMainHandler.sendMessage(msg);
    }

    private void handleBackCameraClosed(CameraStatus status) {
        Log.d(TAG, "handleBackCameraClosed");
    }

    private String cameraStatsFromIntToString(int status) {
        switch (status) {
            case 0:
                return "front-camera-opened";
            case 1:
                return "front-camera-closed";
            case 2:
                return "back-camera-opened";
            case 4:
                return "back-camera-closed";
            default:
                return "invalid";
        }
    }

    private boolean isFrontCameraFromStatus(int status) {
        return status == 1 || status == 0;
    }

    private boolean isBackCameraFromStatus(int status) {
        return status == 4 || status == 2;
    }

    private boolean isCameraOpendFromStatus(int status) {
        return status == 0 || status == 2;
    }

    private boolean isCameraClosedFromStatus(int status) {
        return status == 1 || status == 4;
    }

    public boolean notifyCameraStatus(int cameraId, int status, String packageName) {
        Log.d(TAG, "notifyCameraStatus cameraId=" + cameraId + " packageName=" + packageName + " status=" + cameraStatsFromIntToString(status));
        if (this.isSystemShutdowning) {
            Log.d(TAG, "because system is shutdown, we ignore any notifyCameraStatus!!!");
            return true;
        }
        connectCameraServiceLocked();
        synchronized (this.mFrontCameraStatusLock) {
            CameraStatus tmpFrontStatus = new CameraStatus();
            CameraStatus tmpBackStatus = new CameraStatus();
            if (isFrontCameraFromStatus(status)) {
                CameraStatus.updateCameraStatus(cameraId, isCameraOpendFromStatus(status), packageName, this.mFrontCameraStatus);
                CameraStatus.updateCameraStatus(cameraId, isCameraOpendFromStatus(status), packageName, tmpFrontStatus);
            }
            if (isBackCameraFromStatus(status)) {
                CameraStatus.updateCameraStatus(cameraId, isCameraOpendFromStatus(status), packageName, this.mBackCameraStatus);
                CameraStatus.updateCameraStatus(cameraId, isCameraOpendFromStatus(status), packageName, tmpBackStatus);
            }
            if (isFrontCameraFromStatus(status)) {
                if (status == 1) {
                    this.isFrontCameraOpened = false;
                    handleFrontCameraClosed(tmpFrontStatus);
                } else if (status == 0) {
                    this.isFrontCameraOpened = true;
                    handleFrontCameraOpened(tmpFrontStatus);
                }
            } else if (isBackCameraFromStatus(status)) {
                if (status == 4) {
                    handleBackCameraClosed(tmpBackStatus);
                } else if (status == 2) {
                    handleBackCameraOpened(tmpBackStatus);
                }
            }
        }
        return true;
    }

    private boolean isInWarnPopupCameraPackageList(CameraStatus status) {
        if (status == null) {
            return false;
        }
        return WARN_POPUP_CAMERA_PACKAGE_LIST.contains(status.currentStatusPackageName);
    }

    private boolean isSystemCameraApp(CameraStatus status) {
        if (status == null) {
            return false;
        }
        return SYSTEM_CAMERA_APP_PACKAGE_LIST.contains(status.currentStatusPackageName);
    }

    private boolean isInShortTakebackPackageList(CameraStatus status) {
        if (status == null) {
            return false;
        }
        return SHORT_TAKEBACK_PACKAGE_LIST.contains(status.currentStatusPackageName);
    }

    private String getAppName(String packageName) {
        try {
            PackageInfo pi = this.mContext.getPackageManager().getPackageInfo(packageName, 64);
            if (!(pi == null || pi.applicationInfo == null)) {
                packageName = pi.applicationInfo.loadLabel(this.mContext.getPackageManager()).toString();
            }
            return packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return packageName;
        }
    }

    private boolean isSystemApp(String packageName) {
        boolean z = true;
        if (packageName == null) {
            return false;
        }
        try {
            PackageInfo pi = this.mContext.getPackageManager().getPackageInfo(packageName, 64);
            if (pi == null || pi.applicationInfo == null || ((pi.applicationInfo.flags & 1) == 0 && (pi.applicationInfo.flags & ProcessStates.HASSERVICE) == 0)) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendFrontCameraPressedBroadcast() {
        Intent intent = new Intent(FRONT_CAMERA_PRESSED_ACTION);
        Bundle extras = new Bundle();
        extras.putLong(FRONT_CAMERA_PRESSED_EXTRA_TIME, System.currentTimeMillis());
        intent.putExtras(extras);
        intent.setPackage("com.android.camera");
        Log.d(TAG, " broadcast vivo.intent.action.FRONT_CAMERA_PRESSED");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isStatusJammed(int status) {
        return status == 3 || status == 4;
    }

    private boolean isStatusPressed(int status) {
        return status == 5;
    }

    private void cancelPendingCloseOrOpenVibStebTask(int status, int msgCookie) {
        String str;
        StringBuilder append;
        String str2;
        if (msgCookie == this.mLastCloseVibHallCookie) {
            str = TAG;
            append = new StringBuilder().append("closeVibStep with cookie ").append(msgCookie).append(" get response ");
            str2 = isStatusJammed(status) ? "by jammed status" : isStatusPressed(status) ? "by pressed" : "by push-ok";
            Log.d(str, append.append(str2).toString());
            this.mHavePendingCloseVibStepTask = false;
            if (isStatusJammed(status)) {
                this.isLastTakebackJammed = true;
            }
        }
        if (msgCookie == this.mLastOpenVibHallCookie) {
            str = TAG;
            append = new StringBuilder().append("openVibStep with cookie ").append(msgCookie).append(" get response ");
            str2 = isStatusJammed(status) ? "by jammed status" : isStatusPressed(status) ? "by pressed" : "by popup-ok";
            Log.d(str, append.append(str2).toString());
            this.mHavaPendingOpenVibStepTask = false;
            if (isStatusJammed(status)) {
                this.isLastPupupJammed = true;
            }
        }
    }

    private boolean isInKeyguardRestrictedInputMode() {
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (km != null) {
            return km.inKeyguardRestrictedInputMode();
        }
        return false;
    }

    private void sendEvent(int keyCode, int action) {
        InputManager.getInstance().injectInputEvent(new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, keyCode, 0, 0, -1, 0, 72, 257), 0);
    }

    private void emulatePressHomeKey() {
        Log.d(TAG, "emulatePressHomeKey");
        sendEvent(3, 0);
        sendEvent(3, 1);
    }

    boolean isCurrentFrontCameraOpened() {
        return this.isFrontCameraOpened;
    }

    private void acquireWakeLock(long timeout) {
        if (this.mHallWakeLock == null || (this.mHallWakeLock.isHeld() ^ 1) == 0) {
            Log.d(TAG, "the wakelock is held, don't need acquire it again");
            return;
        }
        Log.d(TAG, "wakelock is not held, acquire it again");
        this.mHallWakeLock.acquire(timeout);
    }

    private void releaseWakeLock() {
        if (this.mHallWakeLock == null || !this.mHallWakeLock.isHeld()) {
            Log.d(TAG, "wakelock is not held, no need to release it");
            return;
        }
        Log.d(TAG, "wakelock is held, release it");
        this.mHallWakeLock.release();
    }

    private void popupPermissionConfirmDialog(final CameraStatus tmp3, final PopupFrontCameraPermissionState ps, final boolean isShowCheckbox) {
        this.isCheckingPopupCameraPermission = true;
        this.permissionCheckDialog = null;
        if (this.mUIHandler != null) {
            this.mUIHandler.post(new Runnable() {
                public void run() {
                    PopupCameraManagerService.this.permissionCheckDialog = new CameraPopupPermissionCheckDialog(new ContextThemeWrapper(PopupCameraManagerService.this.mContext, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT)), true, "\"" + PopupCameraManagerService.this.getAppName(tmp3.currentStatusPackageName) + "\" " + PopupCameraManagerService.this.mContext.getString(51249630), isShowCheckbox ? PopupCameraManagerService.this.mContext.getString(51249592) : PopupCameraManagerService.this.mContext.getString(51249202), PopupCameraManagerService.this.mContext.getString(51249203), isShowCheckbox, ps);
                    PopupCameraManagerService.this.permissionCheckDialog.show();
                    synchronized (PopupCameraManagerService.this.mPermissionCheckDialogLock) {
                        PopupCameraManagerService.this.mPermissionCheckDialogLock.notifyAll();
                    }
                }
            });
        }
        synchronized (this.mPermissionCheckDialogLock) {
            while (this.permissionCheckDialog == null) {
                Log.d(TAG, "wait for create CameraPopupPermissionCheckDialog");
                try {
                    this.mPermissionCheckDialogLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (this.permissionCheckDialog) {
            boolean isAllowToPopup;
            if (this.permissionCheckDialog.isPermissionConfirmed()) {
                isAllowToPopup = this.permissionCheckDialog.isPermissionGranted();
            } else {
                Log.d(TAG, "wait 20s for user confim popup permission");
                try {
                    this.permissionCheckDialog.wait(25000);
                    this.isCheckingPopupCameraPermission = false;
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                    this.isCheckingPopupCameraPermission = false;
                } catch (Throwable th) {
                    this.isCheckingPopupCameraPermission = false;
                }
                if (!this.permissionCheckDialog.isPermissionConfirmed() && (this.permissionCheckDialog.isPermissionCheckCanceled() ^ 1) != 0) {
                    Log.d(TAG, "after 25 seconds, the user doesn't confirm, we just deny it");
                    isAllowToPopup = false;
                } else if (this.permissionCheckDialog.isPermissionCheckCanceled()) {
                    Log.d(TAG, "popup front camera is canceled after check, we don't popup");
                    isAllowToPopup = false;
                } else {
                    isAllowToPopup = this.permissionCheckDialog.isPermissionGranted();
                    Log.d(TAG, "user confirm popucamera isAllowToPopup=" + isAllowToPopup);
                }
            }
            if (!isAllowToPopup) {
                Log.d(TAG, "popup front camera is denied by user!!!, not popup front camera");
            } else if (this.isScreenOn && this.isFrontCameraOpened) {
                Log.d(TAG, "screen is on & front camera is opened, just popup front camera after user choose");
                resetPendingPopupTask();
                popupFrontCameraInternal();
                resetCloseFrontCameraStatus();
            } else if (!this.isScreenOn && this.isFrontCameraOpened) {
                Log.d(TAG, "screen is off , but front camera is close after user choose, will popup when screen on");
                setPendingPopupTask(false, false, tmp3, ps);
            }
        }
        return;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("PopupCameraManagerService Status:");
            pw.println("********************************************************");
            pw.println("mFrontCameraStatus=" + this.mFrontCameraStatus);
            pw.println("mBackCameraStatus=" + this.mBackCameraStatus);
            pw.println("isFrontCameraPopup=" + this.isFrontCameraPopup);
            pw.println("isHavingPendingTakeBackTask=" + this.isHavingPendingTakeBackTask);
            pw.println("mTakeBackTaskStartTime=" + this.mTakeBackTaskStartTime);
            pw.println("isSystemShutdowning=" + this.isSystemShutdowning);
            pw.println("mLastOpenVibHallCookie=" + this.mLastOpenVibHallCookie);
            pw.println("mLastCloseVibHallCookie=" + this.mLastCloseVibHallCookie);
            pw.println("mDropDetectSensorType=" + this.mDropDetectSensorType);
            pw.println("recent front camera popup-takeback record");
            for (CameraPopupTakebackRecord r : this.cameraPopupAndTakebackRecords.getQueue()) {
                pw.println(r.toString());
            }
            pw.println("popup camera policy");
            for (ForbiddenItem item : this.mForbiddenList) {
                pw.println(item);
            }
            pw.println("********************************************************");
            int opti = 0;
            boolean openHall = false;
            boolean closeHall = false;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("--pop".equals(opt)) {
                    openHall = true;
                } else if ("--push".equals(opt)) {
                    closeHall = true;
                }
            }
            if (openHall) {
                popupFrontCamera();
            }
            if (closeHall) {
                takeupFrontCamera();
            }
        }
    }
}
