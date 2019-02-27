package com.vivo.services.facedetect;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.qualcomm.qcrilhook.EmbmsOemHook;
import com.vivo.common.VivoCollectData;
import com.vivo.framework.facedetect.AdjusterParams;
import com.vivo.framework.facedetect.FaceEnrollResult;
import com.vivo.framework.facedetect.IFaceDetectClient;
import com.vivo.framework.facedetect.IFaceDetectDozeService;
import com.vivo.framework.facedetect.IFaceDetectIRClient;
import com.vivo.framework.facedetect.IFaceDetectManager.Stub;
import com.vivo.framework.facedetect.PhoneWindowNotifyFace;
import com.vivo.framework.facedetect.PhoneWindowNotifyFace.FaceDetectNotify;
import com.vivo.framework.fingerprint.FingerprintNotify;
import com.vivo.services.daemon.VivoDmServiceProxy;
import com.vivo.services.facedetect.FaceSensorManager.SensorCallback;
import com.vivo.services.facedetect.analyze.CameraAdjuster;
import com.vivo.services.facedetect.camera.FaceCameraManager;
import com.vivo.services.facedetect.camera.FaceCameraManager.CameraOpenCallback;
import com.vivo.services.facedetect.camera.FaceCameraManager.CameraPreviewCallback;
import com.vivo.services.facedetect.camera.FaceClientApp;
import com.vivo.services.rms.ProcessList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import vivo.app.nightpearl.AbsNightPearlManager;

public class FaceDetectService extends Stub {
    private static final String ACTION_SUPER_POWER_MODE = "intent.action.super_power_save_send";
    private static final String ACTION_UPSLIDE = "vivo.intent.action.UPSLIDE_PANEL_STATE_CHANGED";
    private static final String BROADCAST_KEY = "sps_action";
    private static final String BROADCAST_KEY_UPSLIDE = "panel_state";
    private static final String BROADCAST_KEY_VALUE_ENTER = "entered";
    private static final String BROADCAST_KEY_VALUE_EXIT = "exited";
    private static final int CAMERA_OPEN_METHOD_NONE = 1;
    private static final int CAMERA_OPEN_METHOD_RAISEUP = 2;
    private static final int CAMERA_OPEN_METHOD_SCREENOFF = 5;
    private static final int CAMERA_OPEN_METHOD_SCREENON = 4;
    private static final int CAMERA_OPEN_METHOD_STARTAUTH = 3;
    private static final int CAMERA_OPEN_METHOD_UDFINGERKEY = 6;
    private static final int CAMERA_OPEN_STATUS_OPENED = 2;
    private static final int CAMERA_OPEN_STATUS_RAISEUP = 3;
    private static final int CAMERA_OPEN_STATUS_SCREENOFF = 6;
    private static final int CAMERA_OPEN_STATUS_SCREENON = 5;
    private static final int CAMERA_OPEN_STATUS_STARTAUTH = 4;
    private static final int CAMERA_OPEN_STATUS_UNOPEN = 1;
    private static final int CAMERA_RELEASE = 2;
    private static final int CAMERA_TIME_OUT = 2000;
    private static final String[] CLASSNAME = new String[]{"com.tencent.av.ui", "com.imo.android.imoim.av.ui", "com.skype.m2.views.CallScreen", "jp.naver.line.android.freecall.FreeCallActivity", "com.bbm.ui.voice.activities", "com.whatsapp.voipcalling", "zm.voip.ui.incall", "com.tencent.mm.plugin.voip.ui.VideoActivity"};
    private static final String CT_SECURITY_FACE_WARKE = "ct_security_facewake";
    private static final String EVENT_ID = "1090";
    private static final String EVENT_LABEL_FACEFP_COMBINE_ENABLE = "109016";
    private static final String EVENT_LABEL_FACE_FP_FACE_FAILED_TIME = "109013";
    private static final String EVENT_LABEL_FACE_SECURE_ENABLE = "109014";
    private static final String EVENT_LABEL_FACE_UNLOCK = "10901";
    private static final String EVENT_LABEL_FACE_UNLOCK_ENABLE = "10905";
    private static final String EVENT_LABEL_FACE_UNLOCK_KEEP_KEYGUARD = "109011";
    private static final String EVENT_LABEL_FACE_UNLOCK_METHOD = "109015";
    private static final String EVENT_LABEL_FACE_UNLOCK_UNLOCK_METHOD = "109012";
    private static final String EVENT_LABEL_FACE_UNLOCK_WHEN_SCREEN_ON = "109010";
    private static final String EVENT_LABEL_FAST_UNLOCK = "10902";
    private static final String EVENT_LABEL_RAISE_UP = "10904";
    private static final String EVENT_LABEL_SCREEN_FILL_LIGHT_ENABLE = "10907";
    private static final String EVENT_LABEL_UPDATE_MODLE_RESULT = "10909";
    private static final float EVENT_PREDICE_RAISE_UP = 2.0f;
    private static final float EVENT_RAISE_UP = 1.0f;
    private static final String FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS = "faceunlock_adjust_screen_brightness";
    private static final String FACEUNLOCK_CAMERA_BEAUTY_ENABLE = "faceunlock_camera_beauty";
    private static final String FACEUNLOCK_ENABLED = "faceunlock_enabled";
    private static final String FACEUNLOCK_KEYGUARD_KEEP = "faceunlock_keyguard_keep";
    private static final String FACEUNLOCK_LIGHT_FRAME_NUMBERS = "vivo_smartmulitwindow_current_app_mode";
    private static final String FACEUNLOCK_SECURE_ENABLE = "faceunlock_secure_open";
    private static final String FACEUNLOCK_START_WHEN_SCREENON = "faceunlock_start_when_screenon";
    private static final int FACE_AUTH_FAIL = 5;
    private static final int FACE_CAMERA_OCCUPIED = -1025;
    private static final int FACE_DETECT_BUSY = -3;
    private static final int FACE_DETECT_DISABLED = -7;
    private static final int FACE_DETECT_ERROR_COMPARE_FAILURE = -34;
    private static final int FACE_DETECT_ERROR_GET_SHARE_MEMORY = -3;
    private static final int FACE_DETECT_ERROR_KEYSTORE_ERROR = -100;
    private static final int FACE_DETECT_ERROR_LIVENESS_FAILED = -51;
    private static final int FACE_DETECT_ERROR_LIVENESS_FAILURE = -35;
    private static final int FACE_DETECT_ERROR_LIVENESS_WARNING = -36;
    private static final int FACE_DETECT_ERROR_OCULAR_DETECT = -24;
    private static final int FACE_DETECT_ERROR_VERIYF_FAILED = -50;
    private static final int FACE_DETECT_FAILED = -1;
    private static final int FACE_DETECT_MSG_AUTHEN_NEXT = 203;
    private static final int FACE_DETECT_MSG_BIND_LOCKSETTING = 300;
    private static final int FACE_DETECT_MSG_BIND_SERVICE_STATUS_CHANGED = 225;
    private static final int FACE_DETECT_MSG_BOOT_INIT = 209;
    private static final int FACE_DETECT_MSG_CAMERA_OCCUPIED = 121;
    private static final int FACE_DETECT_MSG_CAMERA_RETRY_OPEN = 202;
    private static final int FACE_DETECT_MSG_CAMERA_TIME_OUT = 111;
    private static final int FACE_DETECT_MSG_CLEAR_SHARED_MEMORY = 119;
    private static final int FACE_DETECT_MSG_DIRECTION_START = 200;
    private static final int FACE_DETECT_MSG_DIRECTION_STOP = 201;
    private static final int FACE_DETECT_MSG_FACEKEY_BIND_SERVICE = 211;
    private static final int FACE_DETECT_MSG_FACEKEY_BOOT_FINISHED = 218;
    private static final int FACE_DETECT_MSG_FACEKEY_FACEFP_RESULT = 220;
    private static final int FACE_DETECT_MSG_FACEKEY_HIDE_FACE_ICON = 217;
    private static final int FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT = 221;
    private static final int FACE_DETECT_MSG_FACEKEY_POWER_ON = 219;
    private static final int FACE_DETECT_MSG_FACEKEY_SHOW_FACE_ICON = 212;
    private static final int FACE_DETECT_MSG_FACEKEY_SHOW_RESULT = 216;
    private static final int FACE_DETECT_MSG_FACEKEY_SHOW_RESULT_ERROR = 215;
    private static final int FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM = 213;
    private static final int FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM = 214;
    private static final int FACE_DETECT_MSG_FINGERPRINT_LOCKED = 105;
    private static final int FACE_DETECT_MSG_FINISH_OPERATION = 205;
    private static final int FACE_DETECT_MSG_FP_VERIFY_TIMEOUT = 123;
    private static final int FACE_DETECT_MSG_HANDLE_MSG = 227;
    private static final int FACE_DETECT_MSG_INFRARED_PROXIMITY = 222;
    private static final int FACE_DETECT_MSG_KEYCODE_SMARTWAKE = 234;
    private static final int FACE_DETECT_MSG_KEYGUARD_CHANGE_FOCUSED = 224;
    private static final int FACE_DETECT_MSG_KEYGUARD_EXIT = 103;
    private static final int FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH = 130;
    private static final int FACE_DETECT_MSG_KEYGUARD_HIDE = 230;
    private static final int FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT = 210;
    private static final int FACE_DETECT_MSG_KEYGUARD_PW_CHANGED = 208;
    private static final int FACE_DETECT_MSG_KEYGUARD_SHOW = 231;
    private static final int FACE_DETECT_MSG_PREVIEW = 102;
    private static final int FACE_DETECT_MSG_PROCESS_MSG_QUEUE = 226;
    private static final int FACE_DETECT_MSG_RELEASE_CAMERA = 109;
    private static final int FACE_DETECT_MSG_SAVE_IMAGE = 106;
    private static final int FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED = 122;
    private static final int FACE_DETECT_MSG_SCREEN_OFF = 108;
    private static final int FACE_DETECT_MSG_SCREEN_ON = 107;
    private static final int FACE_DETECT_MSG_SCREEN_ON_FINISHED = 114;
    private static final int FACE_DETECT_MSG_SCREEN_STATUS_CHANGED = 229;
    private static final int FACE_DETECT_MSG_SENSOR_LIGHT = 223;
    private static final int FACE_DETECT_MSG_SMART_KEY_OPENED = 112;
    private static final int FACE_DETECT_MSG_SOFT_KEYBOARD_HIDE = 233;
    private static final int FACE_DETECT_MSG_SOFT_KEYBOARD_SHOW = 232;
    private static final int FACE_DETECT_MSG_START_CAMERA = 110;
    private static final int FACE_DETECT_MSG_START_FACE_DETECT = 228;
    private static final int FACE_DETECT_MSG_START_OPERATION = 204;
    private static final int FACE_DETECT_MSG_SUCCESS = 100;
    private static final int FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_OFF = 140;
    private static final int FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_ON = 141;
    private static final int FACE_DETECT_MSG_SYSTEM_REBOOT = 104;
    private static final int FACE_DETECT_MSG_THREAD_FINISHED = 206;
    private static final int FACE_DETECT_MSG_TIME_OUT = 101;
    private static final int FACE_DETECT_MSG_VERIFY_FAILED = 120;
    private static final int FACE_DETECT_MSG_VERIFY_INIT = 115;
    private static final int FACE_DETECT_MSG_VERIFY_RELEASE = 116;
    private static final int FACE_DETECT_MSG_VERIFY_RELEASE_ALL = 117;
    private static final int FACE_DETECT_MSG_VERIFY_RELEASE_MEMORY = 118;
    private static final int FACE_DETECT_MSG_VERIFY_TIMEOUT = 207;
    private static final int FACE_DETECT_MSG_WRITE_SHARED_MEMORY = 113;
    private static final int FACE_DETECT_NO_FACE = -2;
    private static final int FACE_DETECT_OK = 0;
    private static final int FACE_DETECT_SUCEESS = 0;
    private static final int FACE_DETECT_THREAD_WAIT_TIME = 50;
    private static final int FACE_ENTER_SECURITY_LOCK = 7;
    private static final int FACE_EROLL = 0;
    private static final int FACE_EXIT_SECURITY_LOCK = 8;
    private static final int FACE_FAIL_FIVE_TIMES = -8;
    private static final int FACE_FAIL_TIMEOUT = -10;
    private static final int FACE_FORBID_LOCK = 1;
    private static final int FACE_ID = 305419896;
    private static final int FACE_KEY_FP_TIMEOUT = 3000;
    private static final int FACE_KEY_MIN_TIME = 100;
    private static final int FACE_OTHER_FAIL = -9;
    private static final int FACE_SCREEN_ON_AUTH = 6;
    private static final int FACE_SUCCESS_COMBINE = 3;
    private static final int FACE_SUCCESS_HIDE_ICON = 4;
    private static final int FACE_SUCCESS_SINGLE_FACE = 2;
    private static final int FACE_UNLOCK_BOTH_FAIL_FIVE_TIMES = -213;
    private static final int FACE_UNLOCK_FACE_FAIL_FIVE_TIMES = -212;
    private static final int FACE_UNLOCK_FP_BOTH_FAIL_TRD = -210;
    private static final int FACE_UNLOCK_FP_COM_LESS_TRD = -206;
    private static final int FACE_UNLOCK_FP_COVER_ALL = -205;
    private static final int FACE_UNLOCK_FP_FACE_FAIL_TRD = -209;
    private static final int FACE_UNLOCK_FP_FAIL = -203;
    private static final int FACE_UNLOCK_FP_FAIL_FACE_FAIL = -200;
    private static final int FACE_UNLOCK_FP_FAIL_FIVETIES = -207;
    private static final int FACE_UNLOCK_FP_FAIL_FIVE_TIMES = -211;
    private static final int FACE_UNLOCK_FP_FAIL_NOFACE = -201;
    private static final int FACE_UNLOCK_FP_START_VERIFY = -208;
    private static final int FACE_UNLOCK_FP_STAY_LONG = -204;
    private static final int FACE_UNLOCK_FP_TINY = -202;
    private static final int FACE_UNLOCK_FREEMEMORY_DELAY = 1000;
    private static final int FACE_UNLOCK_FREEMEMORY_TIME = 60000;
    private static final int FACE_VERIFY = 1;
    private static final int FACE_WHEN_FINGER_FAIL_FIVE_TIMES = -4;
    private static final int FACE_WHEN_PASSWORD_COUNTING = -5;
    private static final int FACE_WHEN_REBOOT = -6;
    private static final int FAIL_LIGHT_ON = 112;
    private static final int FAIL_SCREEN_OFF = 111;
    private static final String FINGER_FACE_COMBINE = "finger_face_combine";
    public static final String FINGER_MOVE_WAKE = "udfp_move_wake";
    private static final String FINGER_SIMPINPUK = "finger_simpinpuk";
    private static final String FINGER_UNLOCK_OPEN = "finger_unlock_open";
    private static final int FP_RESULT_FAIL = -1;
    private static final int FP_RESULT_FAIL_BASE = 100;
    private static final int FP_RESULT_FAIL_COVERAGE = 104;
    private static final int FP_RESULT_FAIL_DEADLINE = 110;
    private static final int FP_RESULT_FAIL_DRY_TOUCH = 105;
    private static final int FP_RESULT_FAIL_FIVE = 102;
    private static final int FP_RESULT_FAIL_PASS = 107;
    private static final int FP_RESULT_FAIL_QUALITY = 109;
    private static final int FP_RESULT_FAIL_REBOOT = 101;
    private static final int FP_RESULT_FAIL_SIX = 103;
    private static final int FP_RESULT_FAIL_TOAST = 108;
    private static final int FP_RESULT_FAIL_TOO_FAST = 106;
    private static final int FP_RESULT_SUCCESS = 0;
    private static final int FP_START_AUTHEN = 11;
    private static final int FRAME_GAP_TIME = 50;
    private static final int ICON_FACEKEY_HIDE_FACE_ICON = 6;
    private static final int ICON_FACEKEY_KEYGUARD_EXIT = 10;
    private static final int ICON_FACEKEY_NONE = 0;
    private static final int ICON_FACEKEY_POWER_ON = 9;
    private static final int ICON_FACEKEY_SECURE_LOCK_STATE = 11;
    private static final int ICON_FACEKEY_SHOW_FACE_ICON = 1;
    private static final int ICON_FACEKEY_SHOW_RESULT = 4;
    private static final int ICON_FACEKEY_SHOW_VERIFY_ANIM = 2;
    private static final int ICON_FACEKEY_SMARTWAKE = 12;
    private static final int ICON_FACEKEY_STOP_RESULT_ANIM = 5;
    private static final int ICON_FACEKEY_STOP_VERIFY_ANIM = 3;
    private static final int ICON_FACEKEY_UPDATE_BOOT_FINISHED = 8;
    private static final int ICON_FACEKEY_UPDATE_DISPLAY_STATUS = 7;
    private static final int IR_OPEN = 1;
    public static final String[] KEYGUARD_HIDE_PKGNAME = new String[]{"com.android.camera", "com.android.BBKClock", "com.android.incallui", "com.android.systemui", "com.vivo.magazine", "com.tencent.mm", "com.vivo.gallery", "com.vivo.agent", "com.facebook.orca", "com.viber.voip", "com.facebook.mlite", "com.google.android.apps.tachyon", "org.telegram.messenger", "com.kakao.talk", "com.azarlive.android", "com.google.android.talk", "ru.mail", "com.skype.raider", "com.icq.mobile.client", "com.google.android.apps.maps", "com.android.bbkcalculator"};
    private static final String KEY_FLASHLIGHT_STATE = "FlashState";
    private static final int KEY_GUARD_ENTER_SECURE_LOCK = 1;
    private static final int KEY_GUARD_EXIT_SECURE_LOCK = 2;
    private static final String KEY_PROPERTY_OPEN_CAMERA_WHEN_SCREEN_OFF = "persist.vivo.face.screenoff";
    private static final int MAX_FACEKEY_VERIFYTIME = 50;
    private static final int MAX_LIMIT_MEMORY = 307200;
    private static final int MAX_OPEN_FAILED_COUNT = 3;
    private static final int MAX_PROCESS_VERIFY_TIMEOUT = 10000;
    private static final int MAX_RETRY = 5;
    private static final int MIN_FACEKEY_ERROR_TIMEOUT = 2000;
    public static final int MIXED_DATA_LENGTH = 6557184;
    private static final int MOTION_STATE_MOVE = 2;
    private static final int MOTION_STATE_STILL = 1;
    private static final int MOTION_STATE_UNKNOWN = 0;
    private static final int MSG_RAISE_UP_OPEN_CAMERA = 666;
    private static final boolean NIGHT_PEARL_SUPPORT = "nightpearl_support".equals(SystemProperties.get("ro.build.nightpearl.support", "nightpearl_not_support"));
    private static final int NORMAL_MEMORY_MAX_SIZE = 463800;
    public static final String[] NOT_PLAY_ANIM_PKGNAME = new String[0];
    private static final String[] PKGNAME_CLASSNAME = new String[]{"jp.naver.line.android", "com.tencent.mobileqq", "com.imo.android.imov", "com.imo.android.imoim", "com.bbm", "com.whatsapp", "com.zing.zalo", "com.skype.m2", "com.tencent.mm"};
    private static final String PRODUCT_MODEL = SystemProperties.get("ro.vivo.product.model", "unknown");
    private static final String PRODUCT_VERSION = SystemProperties.get("ro.vivo.product.version", "");
    private static final String PROP_FINGERPRINT_TYPE = "persist.sys.fptype";
    private static final String PROP_VALUE_PREFIX_UDFP = "udfp_";
    public static final String[] SCREEN_ON_PKGNAME = new String[]{"com.android.incallui", "com.android.BBKClock", "com.tencent.mm", "com.android.systemui", "com.android.camera", "com.vivo.gallery", "com.facebook.orca", "com.viber.voip", "com.facebook.mlite", "com.google.android.apps.tachyon", "org.telegram.messenger", "com.kakao.talk", "com.azarlive.android", "com.google.android.talk", "ru.mail", "com.skype.raider", "com.icq.mobile.client", "com.google.android.apps.maps"};
    private static final String SETTING_RAISE_UP_ENABLED = "bbk_raiseup_wake_enable_setting";
    private static final String SHARE_MEMORY_NAME = "xxxxxx";
    private static final String SIM_STATE_DISABLE = "disable";
    private static final String SIM_STATE_ENABLE = "enable";
    private static final int SKIP_FRAME_COUNT;
    private static final int STAGE_INIT = 0;
    private static final int STAGE_PREDICT_RAISE_UP = 1;
    private static final int STAGE_RAISE_UP = 2;
    private static final String STOP_SERVICE_PROP = "sys.face.detect";
    private static final String TAG = FaceDetectService.class.getSimpleName();
    private static final int TYPE_RAISEUP_DETECT = getTypeRaiseupDetectValue();
    private static final int UNLOCK_FAIL_TIME_SCRREN_DOZE = 1000;
    private static final int UNLOCK_IR_TIME_OUT = 2500;
    private static final int UNLOCK_MINI_TIME_OUT = 3000;
    private static final int UNLOCK_TIME_OUT = 4000;
    private static final int VERIFY_RESULT_DETECT_FAIL = -13;
    private static final int VERIFY_RESULT_NO_FACE = -14;
    private static final int VERIFY_STOPED_BY_USER = -52;
    private static final boolean isSupportFaceUnlockKey = "1".equals(SystemProperties.get("persist.facedetect.doze.key", "0"));
    private static final String mDAEMONVERSION = "3.1.0";
    private static final boolean mFreeMemory = true;
    private static final boolean mFreeMemoryAll = true;
    private static final Object mLock = new Object();
    private static final boolean mRemoveSecureLock = "0".equals(SystemProperties.get("ro.vivo.face.softwarelock", "1"));
    private static final boolean mScreenBrightDefaultOn = false;
    private static final Object mShareMemoryLock = new Object();
    private static final boolean mStopServiceEnable = true;
    private static final boolean mUdFingerSupport = SystemProperties.get(PROP_FINGERPRINT_TYPE, "unknown").startsWith(PROP_VALUE_PREFIX_UDFP);
    private static FaceDetectService sInstance;
    private final int IR_TEST_FACE_DETECT_FAILED = -1;
    private final int IR_TEST_FACE_DETECT_IR_CANNOT_USE = FACE_WHEN_FINGER_FAIL_FIVE_TIMES;
    private final int IR_TEST_FACE_DETECT_IR_ERROR = FACE_WHEN_PASSWORD_COUNTING;
    private final int IR_TEST_FACE_DETECT_IR_LIGHT_LOW = -3;
    private final int IR_TEST_FACE_DETECT_NO_FACE = FACE_DETECT_NO_FACE;
    private final int IR_TEST_FACE_DETECT_SUCEESS = 0;
    private final float IR_TEST_LIGHT_THRESHOLD = 21.0f;
    private final float IR_TEST_SCORE_THRESHOLD = 22.0f;
    private final int MAX_IR_TEST_TIME_OUT = 6000;
    private final int MSG_TEST_IR_FACE_OVER = 1002;
    private final int MSG_TEST_IR_SHARE_MEMORY = ProcessList.UNKNOWN_ADJ;
    private int MSG_TURNON_SCREEN_MIN_TIMEOUT = ProcessList.SERVICE_ADJ;
    private String NAME_SERVICE = "com.vivo.faceunlock.FaceDetectDozeService";
    private final int PD_RAWDATA_LENGTH = 6096384;
    private final int PD_RAW_OFFSET = 0;
    private String PKG_SERVICE = "com.vivo.faceunlock";
    private final int PREVIEW_DATA_LENGTH = 460800;
    private final int PREVIEW_OFFSET = 6096384;
    private int SHARE_MEMORY_MAX_SIZE;
    boolean bInitiated = false;
    private long cameraTimeEnd;
    private FaceEnrollResult erollResult;
    private int handleOpenCameraTimeOut = 0;
    private int handleOpenCameraopenMethod = 0;
    private boolean isCompareDataReady = false;
    private boolean isIRTestTimeOut;
    private boolean isWriteMemory = false;
    private final boolean m2PdAlgEnable = "1".equals(SystemProperties.get("persist.facedetect.twopd", "0"));
    private ActivityManager mActivityManager;
    private CameraAdjuster mAdjuster;
    private boolean mAppUsing = false;
    private boolean mBeautyCameraEnable = "1".equals(SystemProperties.get("persist.facedetect.camera", "0"));
    private int mBindServiceErrorCount = 0;
    private boolean mBootFinished = true;
    private HashMap<String, IFaceDetectClient> mCallBackClient = new HashMap();
    private int mCallingUid = 0;
    private int mCamerOpenMethod = 1;
    private int mCameraDataInternalTime = 0;
    private int mCameraErrorTimes = 0;
    private Handler mCameraHandler;
    private int mCameraOpenStatus = 1;
    private HandlerThread mCameraThread;
    private long mCameraTimeStart;
    private Object mCameraTimerLock = new Object();
    private CameraTimerTask mCameraTimerTask;
    private Timer mCameratime;
    private ArrayList<String> mClientModelList = new ArrayList();
    private int mCompareCount;
    private ContentResolver mContentRv = null;
    private Context mContext;
    private int mContinuousNoFaceTime = 0;
    private int mCurrentBackLight = 0;
    private int mCurrentDisplayState = 0;
    private Object mCurrentRunClientLock = new Object();
    private String mCurrentRunModel = null;
    private boolean mDataReadyRemove = false;
    private Handler mDirectionHandler;
    private HandlerThread mDirectionThread;
    private int mDirectoryVerifyResult = -1;
    private int mDozeFaceKeyTimes = 0;
    private byte[] mEmptyData = null;
    private FaceCameraManager mFaceCameraManager;
    private FaceClientApp mFaceClientApp;
    private ServiceConnection mFaceDetectDozeConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.d(FaceDetectService.TAG, "service connected");
            FaceDetectService.this.mIFaceDetectDozeService = IFaceDetectDozeService.Stub.asInterface(service);
            FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_BIND_SERVICE_STATUS_CHANGED, 1, 0, 0);
            if (FaceDetectService.isSupportFaceUnlockKey && FaceDetectService.this.mBootFinished) {
                FaceDetectService.this.notifyBindServiceSystemBooted();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Slog.d(FaceDetectService.TAG, "service disconnected");
            FaceDetectService.this.mIFaceDetectDozeService = null;
            FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_BIND_SERVICE_STATUS_CHANGED, 0, 0, 0);
        }
    };
    private Handler mFaceDetectHandler;
    private FaceDetectNative mFaceDetectNative;
    private FaceDetectStatus mFaceDetectStatus;
    private HandlerThread mFaceDetectThread;
    private boolean mFaceDetectWorking = false;
    private long mFaceErrorTime = 0;
    private long mFaceKeyDownTime = 0;
    private int mFaceKeyErrorTimes = 0;
    private UnlockNotify mFaceNotify;
    private FaceOrientation mFaceOrientation;
    private BroadcastReceiver mFaceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (FaceDetectService.ACTION_SUPER_POWER_MODE.equals(action)) {
                    String superState = intent.getStringExtra(FaceDetectService.BROADCAST_KEY);
                    Slog.i(FaceDetectService.TAG, "onReceive: superState: " + superState);
                    if (FaceDetectService.BROADCAST_KEY_VALUE_ENTER.equals(superState)) {
                        FaceDetectService.this.mSuperPowerModeOpen = true;
                    } else if (FaceDetectService.BROADCAST_KEY_VALUE_EXIT.equals(superState)) {
                        FaceDetectService.this.mSuperPowerModeOpen = false;
                    }
                } else if (FaceDetectService.ACTION_UPSLIDE.equals(action)) {
                    String reason = intent.getStringExtra(FaceDetectService.BROADCAST_KEY_UPSLIDE);
                    Slog.i(FaceDetectService.TAG, "onReceive: reason: " + reason);
                    if (reason.equals("starting_expand") || reason.equals("expand")) {
                        FaceDetectService.this.mUpslideExpand = true;
                    } else if (reason.equals("collapsed") || reason.equals("closed")) {
                        FaceDetectService.this.mUpslideExpand = false;
                    }
                }
            }
        }
    };
    private boolean mFaceResultReply = false;
    private FaceSensorManager mFaceSensorManager;
    private boolean mFaceUnlockKeyEnable = false;
    private boolean mFaceUnlockOtherWay = false;
    private boolean mFaceUnlockStartWhenScreenOn = false;
    private boolean mFaceUnlockWhenScreenOn = false;
    private boolean mFaceVerified = false;
    private int mFaceVerifyCode = -1;
    private long mFingerErrorTime = 0;
    private boolean mFingerFaceCombine = false;
    private boolean mFingerLocked = false;
    private boolean mFingerMoveWakeEnabled = true;
    private int mFingerVerifyCode = -1;
    private boolean mFingerVerifyReply = false;
    private int mFingerVerifyResult = -1;
    private boolean mFingerprintEnabled = false;
    private boolean mFirstKeyguardExit = false;
    private int mFrameCount = 0;
    private int mFrameOrder = 0;
    private boolean mHasRemove = false;
    private IActivityManager mIActivityManager;
    private IFaceDetectDozeService mIFaceDetectDozeService;
    private IFaceDetectIRClient mIFaceDetectIRClient;
    private int mIRFrameCount;
    private TestHandler mIRMemoryHandler;
    private Object mIRShareMemoryLock = new Object();
    private boolean mIRTestVerifyNoFace = false;
    private boolean mInfraredNear = false;
    private boolean mIqooSecEnable = false;
    private int mIrTestFrameCount = 0;
    private boolean mIs2PDAlg = false;
    private boolean mIsFaceUnlockEnabled = false;
    private boolean mIsGlobalActions = false;
    private boolean mIsIconShow = false;
    private boolean mIsKeyDown;
    private boolean mIsNv21DataReady = false;
    private boolean mIsOpenFlashLight = false;
    private boolean mIsPhoneState = false;
    private boolean mIsRaiseUpEnabled;
    private boolean mIsScreenDoze = false;
    private boolean mIsScreenFillLightEnabled = true;
    private boolean mIsSecurityFaceWake = false;
    private boolean mKeepMemory = false;
    private IFaceDetectClient mKeyguardClient;
    private boolean mKeyguardExitByCombin = false;
    private boolean mKeyguardExitByFace = false;
    private boolean mKeyguardExitByFinger = false;
    private boolean mKeyguardExited = false;
    private boolean mKeyguardFocused = false;
    private boolean mKeyguardHide = true;
    private boolean mKeyguardHideGlobal = true;
    private int mKeyguardLocationStatus = 0;
    private KeyguardManager mKeyguardManager = null;
    private int mKeyguardStatus = 1;
    private boolean mKeyguardWouldExit = false;
    private boolean mKeystoreVPassword = false;
    private boolean mKeystorehasGet = false;
    private int mLastFaceDetectFailReason = 0;
    private long mLastFaceKeyErrorTime = 0;
    private float mLightMax;
    private Object mListLock = new Object();
    private boolean mListenerRegistered;
    private ILockSettings mLockSettingsService;
    private final int mMaxBindServiceErrorCount = 5;
    private int mMaxFrameCount = 3;
    private Handler mMemoryHandler;
    private LowMemoryListener mMemoryListener = null;
    private TestHandler mMessageIRHandler;
    private int mMotionState = 0;
    private ArrayList<FaceKeyEventNode> mMsgQueueList = new ArrayList();
    private boolean mMsgQueueProcessed = false;
    private boolean mNeedAdjustCamera = false;
    private AbsNightPearlManager mNightPearlManager;
    private HashMap<String, Boolean> mNotifiedClient = new HashMap();
    private boolean mOnlyFaceUnlock = true;
    private int mOpenCameraFailCount = 0;
    private boolean mOpenCameraWhenScreenOff = SystemProperties.getBoolean(KEY_PROPERTY_OPEN_CAMERA_WHEN_SCREEN_OFF, false);
    private Object mPPDataCallback = null;
    private int mPPDataFrameGap = 0;
    private String mPackageName;
    WakeLock mPartialWakeLock = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            Slog.i(FaceDetectService.TAG, "onCallStateChanged: state = " + state);
            switch (state) {
                case 0:
                    Slog.i(FaceDetectService.TAG, "CALL_STATE_IDLE");
                    FaceDetectService.this.mIsPhoneState = false;
                    return;
                case 1:
                    Slog.i(FaceDetectService.TAG, "CALL_STATE_RINGING");
                    FaceDetectService.this.mIsPhoneState = true;
                    return;
                case 2:
                    Slog.i(FaceDetectService.TAG, "CALL_STATE_OFFHOOK");
                    FaceDetectService.this.mIsPhoneState = true;
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mPreviewCallbackCalled = false;
    private long mPreviewCallbackTime;
    private byte[] mPreviewData;
    private int mPreviewDataLen = 0;
    private Object mPreviewDataLock = new Object();
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Slog.i(FaceDetectService.TAG, "onForegroundActivitiesChanged pid=" + pid + " uid=" + uid + " foreground=" + foregroundActivities);
            if (!FaceDetectService.this.mSystemScreenOn || !FaceDetectService.this.isKeyguardShowing()) {
                return;
            }
            if (FaceDetectService.this.isTopActivity(FaceDetectService.SCREEN_ON_PKGNAME)) {
                FaceDetectService.this.mSkipFingerDownByProcess = true;
            } else if (FaceDetectService.this.isTopActivity(FaceDetectService.KEYGUARD_HIDE_PKGNAME)) {
                FaceDetectService.this.mSkipFingerDownByProcess = true;
            } else {
                FaceDetectService.this.mSkipFingerDownByProcess = false;
            }
        }

        public void onProcessDied(int pid, int uid) {
        }
    };
    private boolean mProcessed = false;
    private boolean mRegisteredProcessObserver = false;
    private int mRetryTimes = 5;
    private float mScore;
    private int mScreenDozeVerifyTime = 3;
    private int mScreenState;
    private boolean mSendVerifyTimeOut = false;
    private SensorCallback mSensorCallback = new SensorCallback() {
        public void onFaceTimer(int type, int state) {
            Slog.i(FaceDetectService.TAG, "mSensorCallback : type:" + type + ":state:" + state + ":screen:" + FaceDetectService.this.mSystemScreenOn);
            if (FaceDetectService.this.mFaceSensorManager == null) {
                Slog.w(FaceDetectService.TAG, "mFaceSensorManager  is null");
            } else if (2 == type && FaceDetectService.this.mCurrentBackLight > 0) {
                if (1 == state) {
                    FaceDetectService.this.mInfraredNear = true;
                } else if (2 == state) {
                    FaceDetectService.this.mInfraredNear = false;
                }
            }
        }
    };
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != FaceDetectService.TYPE_RAISEUP_DETECT) {
                return;
            }
            if (event.values[0] == FaceDetectService.EVENT_PREDICE_RAISE_UP) {
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(FaceDetectService.TAG, "onSensorChanged stage one mStage:" + FaceDetectService.this.mStage);
                }
                if (FaceDetectService.this.mStage == 0) {
                    FaceDetectService.this.mStage = 1;
                }
            } else if (event.values[0] == FaceDetectService.EVENT_RAISE_UP) {
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(FaceDetectService.TAG, "onSensorChanged trigger stage two mStage:" + FaceDetectService.this.mStage + ", mRetryTimes = " + FaceDetectService.this.mRetryTimes);
                }
                if (FaceDetectService.this.mIsFaceUnlockEnabled && FaceDetectService.this.mFaceUnlockWhenScreenOn && FaceDetectService.this.mRetryTimes > 0 && (FaceDetectService.this.mFingerLocked ^ 1) != 0) {
                    if (FaceDetectService.this.mSkipKeyguardEnable && FaceDetectService.this.mKeyguardManager != null && FaceDetectService.this.mKeyguardManager.isKeyguardLocked()) {
                        FaceDetectService.this.removeMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                        FaceDetectService.this.removeMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT);
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT, 1, 0, 0);
                    }
                    if (FaceDetectService.this.mPartialWakeLock != null) {
                        FaceDetectService.this.mPartialWakeLock.acquire(500);
                    }
                    FaceDetectService.this.removeMyMessage(FaceDetectService.this.mCameraHandler, 109);
                    FaceDetectService.this.removeMyMessage(FaceDetectService.this.mCameraHandler, 110);
                    FaceDetectService.this.sendMyMessage(FaceDetectService.this.mCameraHandler, 110, 1, 2, 0);
                    FaceDetectService.this.removeMyMessage(FaceDetectService.this.mCameraHandler, 111);
                    FaceDetectService.this.sendMyMessage(FaceDetectService.this.mCameraHandler, 111, 2000);
                }
                FaceDetectService.this.mStage = 2;
                if (FaceDetectService.this.mIsRaiseUpEnabled && FaceDetectService.this.mIsFaceUnlockEnabled) {
                    if (FaceDetectService.this.mSensorManager == null) {
                        FaceDetectService.this.mSensorManager = (SensorManager) FaceDetectService.this.mContext.getSystemService("sensor");
                    }
                    if (FaceDetectService.this.mSensorManager != null && FaceDetectService.this.mListenerRegistered) {
                        FaceDetectService.this.mSensorManager.unregisterListener(FaceDetectService.this.mSensorEventListener);
                        FaceDetectService.this.mListenerRegistered = false;
                    }
                    if (FaceDebugConfig.DEBUG) {
                        Slog.d(FaceDetectService.TAG, "onSensorChanged registered:" + FaceDetectService.this.mListenerRegistered);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Handler mSensorHandler;
    private SensorManager mSensorManager;
    private HandlerThread mSensorThread;
    private boolean mServiceConnected = false;
    private FaceDetectShareMemory mShareMemory;
    private boolean mSimPinEnable = true;
    private boolean mSkipFingerDownByProcess = false;
    private final boolean mSkipKeyguardEnable = "1".equals(SystemProperties.get("persist.facedetect.skip.guard", "0"));
    private boolean mSmartKeyOpened = false;
    private boolean mSoftKeyboardShown = false;
    private int mStage = 0;
    private boolean mStartByRaseUp = false;
    private long mStartOpenTime = 0;
    private boolean mSuperPowerModeOpen = false;
    private boolean mSystemReady = false;
    private boolean mSystemScreenOn = false;
    private boolean mThreadWork = false;
    private boolean mTimeOut = true;
    private boolean mTimeTest = false;
    private MyVerifyThread mTrackThread;
    private boolean mUnlockKeyguardKeep = false;
    private ContentObserver mUnlockObserver;
    private Timer mUnlockTimer;
    private Object mUnlockTimerLock = new Object();
    private UnlockTimerTask mUnlockTimerTask;
    private boolean mUpslideExpand = false;
    private boolean mUserReboot = true;
    private HashMap<Integer, Integer> mVerifyErrorCodes;
    private int mVerifyFailedCount = 0;
    private boolean mVerifyFinished = true;
    private boolean mVerifyNoFace = true;
    private int mVerifyResult = 0;
    private VivoCollectData mVivoCollectData;
    private VivoDmServiceProxy mVivoProxy = null;
    private WindowManagerPolicy mWindowManagerPolicy;
    private HandlerThread mWriteSharedMemoryThread;
    private boolean mWritingMemory = false;
    private ArrayList<Float> mverifyScores;
    private PowerManager pm;

    private class CameraCallBack implements CameraPreviewCallback {
        /* synthetic */ CameraCallBack(FaceDetectService this$0, CameraCallBack -this1) {
            this();
        }

        private CameraCallBack() {
        }

        public void onPreviewFrame(byte[] data) {
            FaceDetectService faceDetectService = FaceDetectService.this;
            faceDetectService.mFrameOrder = faceDetectService.mFrameOrder + 1;
            if (FaceDebugConfig.DEBUG) {
                Slog.i(FaceDetectService.TAG, "onPreviewFrame mFrameOrder = " + FaceDetectService.this.mFrameOrder);
            }
            if (!FaceDetectService.this.mPreviewCallbackCalled) {
                FaceDetectService.this.mPreviewCallbackTime = System.currentTimeMillis() - FaceDetectService.this.mCameraTimeStart;
                FaceDetectService.this.mPreviewCallbackCalled = true;
            }
            if (!FaceDetectService.this.checkIfSkipFrame()) {
                if (!FaceDetectService.this.mTimeTest) {
                    FaceDetectService.this.sendMessage(102);
                }
                synchronized (FaceDetectService.this.mPreviewDataLock) {
                    FaceDetectService.this.mPreviewData = data;
                }
                FaceDetectService.this.mMemoryHandler.sendEmptyMessage(FaceDetectService.FACE_DETECT_MSG_WRITE_SHARED_MEMORY);
            }
        }
    }

    private class CameraHandler extends Handler {
        /* synthetic */ CameraHandler(FaceDetectService this$0, Looper looper, CameraHandler -this2) {
            this(looper);
        }

        private CameraHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Slog.i(FaceDetectService.TAG, "CameraHandler handleMessage what = " + msg.what);
            switch (msg.what) {
                case 109:
                    FaceDetectService.this.handleReleaseCamera();
                    return;
                case 110:
                    FaceDetectService.this.handleOpenCamera(msg.arg1, msg.arg2);
                    return;
                case 111:
                    FaceDetectService.this.handleReleaseCamera();
                    return;
                case FaceDetectService.FACE_DETECT_MSG_CAMERA_OCCUPIED /*121*/:
                    FaceDetectService.this.handleCameraOccupied((String) msg.obj);
                    return;
                case FaceDetectService.FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_OFF /*140*/:
                    FaceDetectService.this.handleSyncCameraPreviewOff();
                    return;
                case FaceDetectService.FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_ON /*141*/:
                    FaceDetectService.this.handleSyncCameraPreviewOn();
                    return;
                default:
                    return;
            }
        }
    }

    private class CameraTimerTask extends TimerTask {
        /* synthetic */ CameraTimerTask(FaceDetectService this$0, CameraTimerTask -this1) {
            this();
        }

        private CameraTimerTask() {
        }

        public void run() {
            FaceDetectService.this.sendMyMessage(FaceDetectService.this.mCameraHandler, 111, 0);
        }
    }

    private class CameraoOpenStatusCallBack implements CameraOpenCallback {
        /* synthetic */ CameraoOpenStatusCallBack(FaceDetectService this$0, CameraoOpenStatusCallBack -this1) {
            this();
        }

        private CameraoOpenStatusCallBack() {
        }

        public void onCameraCallback(int result) {
            if (result == 0) {
                FaceDetectService.this.openCameraSuccess(FaceDetectService.this.handleOpenCameraopenMethod, FaceDetectService.this.handleOpenCameraTimeOut);
            } else {
                FaceDetectService.this.openCameraError(FaceDetectService.this.handleOpenCameraTimeOut, result, FaceDetectService.this.handleOpenCameraopenMethod);
            }
        }
    }

    private class DirectionHandler extends Handler {
        /* synthetic */ DirectionHandler(FaceDetectService this$0, Looper looper, DirectionHandler -this2) {
            this(looper);
        }

        private DirectionHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (FaceDetectService.this.mIsFaceUnlockEnabled || (FaceDetectService.this.mIqooSecEnable ^ 1) == 0) {
                switch (msg.what) {
                    case FaceDetectService.FACE_DETECT_MSG_FP_VERIFY_TIMEOUT /*123*/:
                        if (FaceDetectService.this.needViber(-1, FaceDetectService.this.mBootFinished ? -1 : FaceDetectService.FACE_WHEN_REBOOT)) {
                            FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, FaceDetectService.this.mFaceVerifyCode, -1023, 0);
                        }
                        FaceDetectService.this.handleNotifyFingerResult(-1, -1);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH /*130*/:
                        if (FaceDetectService.this.mSkipKeyguardEnable) {
                            if (!((FaceDetectService.this.mUnlockKeyguardKeep && (FaceDetectService.this.mFaceUnlockOtherWay ^ 1) == 0) || (FaceDetectService.this.mKeyguardExited ^ 1) == 0)) {
                                FaceDetectService.this.forceKeyguardShow();
                                try {
                                    Thread.sleep(80);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (msg.arg1 == 1) {
                                String model = msg.obj;
                                if (model == null) {
                                    PhoneWindowNotifyFace.getInstance().notifyFaceUnlockStatus(0);
                                    break;
                                }
                                IFaceDetectClient client = (IFaceDetectClient) FaceDetectService.this.mCallBackClient.get(model);
                                if (client != null) {
                                    try {
                                        client.onAuthenticationResult(model, -1024, 1);
                                    } catch (RemoteException e2) {
                                        Slog.w(FaceDetectService.TAG, "Remote exception while face authenticating: ", e2);
                                    }
                                }
                                FaceDetectService.this.mTimeOut = true;
                            }
                            PhoneWindowNotifyFace.getInstance().notifyFaceUnlockStatus(0);
                            break;
                        }
                        break;
                    case 200:
                        if (FaceDetectService.this.mContext != null) {
                            Slog.d(FaceDetectService.TAG, "Accelerometer start");
                            AccelerometerManager.start(FaceDetectService.this.mContext);
                            break;
                        }
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_DIRECTION_STOP /*201*/:
                        Slog.d(FaceDetectService.TAG, "Accelerometer stop");
                        AccelerometerManager.stop();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT /*210*/:
                        if (FaceDetectService.this.mSkipKeyguardEnable) {
                            if (msg.arg1 != 0) {
                                FaceDetectService.this.handleKeepBacklightWakeUp();
                                break;
                            } else {
                                FaceDetectService.this.handleKeepBacklightKey();
                                break;
                            }
                        }
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_BIND_SERVICE /*211*/:
                        FaceDetectService.this.handleBindService(msg.arg1);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_SHOW_FACE_ICON /*212*/:
                        FaceDetectService.this.handleBinderIconStatus(1);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM /*213*/:
                        FaceDetectService.this.handleBinderIconStatus(2);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM /*214*/:
                        FaceDetectService.this.handleBinderIconStatus(3);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_SHOW_RESULT /*216*/:
                        FaceDetectService.this.handleBinderIconStatus(4, msg.arg1, msg.arg2);
                        FaceDetectService.this.saveErrorTime();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_HIDE_FACE_ICON /*217*/:
                        FaceDetectService.this.handleBinderIconStatus(6);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_BOOT_FINISHED /*218*/:
                        FaceDetectService.this.handleBinderIconStatus(8, msg.arg1, msg.arg2);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT /*221*/:
                        FaceDetectService.this.handleBinderIconStatus(10, msg.arg1, msg.arg2);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_CHANGE_FOCUSED /*224*/:
                        FaceDetectService.this.handleKeyguardFocusedChanged();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_BIND_SERVICE_STATUS_CHANGED /*225*/:
                        FaceDetectService.this.handleBindServiceStatusChanged(msg.arg1);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_PROCESS_MSG_QUEUE /*226*/:
                        FaceDetectService.this.handleProcessMsgQueue();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_HANDLE_MSG /*227*/:
                        FaceDetectService.this.handleMsgQueue((FaceKeyEventNode) msg.obj);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_SCREEN_STATUS_CHANGED /*229*/:
                        FaceDetectService.this.handleBacklightStateChanged(msg.arg1, msg.arg2);
                        break;
                    case 300:
                        if (FaceDebugConfig.DEBUG) {
                            Slog.i(FaceDetectService.TAG, "bind l setting");
                        }
                        FaceDetectService.this.getLockSettings();
                        break;
                }
            }
        }
    }

    private class FaceKeyEventNode {
        public int mErrorCode;
        public int mEventId;
        public int mExtraInfo;

        public FaceKeyEventNode(int eventid, int errorcode, int extra) {
            this.mEventId = eventid;
            this.mErrorCode = errorcode;
            this.mExtraInfo = extra;
        }

        public String toString() {
            if (FaceDebugConfig.DEBUG) {
                return "FaceKeyEventNode{event: " + FaceDetectService.this.msgQueueToString(this.mEventId) + ", errorcode: " + this.mErrorCode + ", extrainfo: " + this.mExtraInfo + " }";
            }
            return "";
        }
    }

    private class IRVerifyTestThread {
        private Thread mVerifyThread;

        /* synthetic */ IRVerifyTestThread(FaceDetectService this$0, IRVerifyTestThread -this1) {
            this();
        }

        private IRVerifyTestThread() {
        }

        public void start() {
            this.mVerifyThread = new Thread() {
                public void run() {
                    Process.setThreadPriority(FaceDetectService.FACE_DETECT_NO_FACE);
                    FaceDetectService.this.handleTestCompare();
                }
            };
            this.mVerifyThread.start();
        }
    }

    private class LowMemoryListener implements ComponentCallbacks2 {
        private LowMemoryListener() {
        }

        public void onTrimMemory(int level) {
            if (level >= 15) {
                FaceDetectService.this.handleLowMemory();
            }
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onLowMemory() {
        }
    }

    private class MyCameraVerifyThread {
        private boolean livenesscheck;
        private Thread mCameraVerifyThread;
        private int mPreviewDatalen;
        private int mPreviewHeight;
        private int mPreviewWidth;
        private int mVerifyResult = -1;
        private String model;
        private int orientation;
        private int pixelFormat;

        public MyCameraVerifyThread(String model, int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation, boolean livenesscheck) {
            this.model = model;
            this.mPreviewDatalen = mPreviewDatalen;
            this.pixelFormat = pixelFormat;
            this.mPreviewWidth = mPreviewWidth;
            this.mPreviewHeight = mPreviewHeight;
            this.orientation = orientation;
            this.livenesscheck = livenesscheck;
        }

        public void start() {
            this.mCameraVerifyThread = new Thread() {
                public void run() {
                    long processVerifyTime = System.currentTimeMillis();
                    if (FaceDetectService.this.mFaceDetectNative != null) {
                        FaceDetectService.this.mFaceDetectNative.prepareVerifyThread();
                        Vector result = FaceDetectService.this.mFaceDetectNative.processVerify(MyCameraVerifyThread.this.mPreviewDatalen, MyCameraVerifyThread.this.pixelFormat, MyCameraVerifyThread.this.mPreviewWidth, MyCameraVerifyThread.this.mPreviewHeight, MyCameraVerifyThread.this.orientation, MyCameraVerifyThread.this.livenesscheck, FaceDetectService.this.mFaceCameraManager.getIsDarkEnvironment(), FaceDetectService.this.mFaceCameraManager.isIrLedOpened());
                        if (result != null) {
                            MyCameraVerifyThread.this.mVerifyResult = ((Integer) result.get(0)).intValue();
                        } else {
                            MyCameraVerifyThread.this.mVerifyResult = -1;
                        }
                        FaceDetectService.this.mFaceDetectNative.resetVerifyThread();
                    }
                    processVerifyTime = System.currentTimeMillis() - processVerifyTime;
                    if (FaceDebugConfig.DEBUG_TIME) {
                        Slog.d(FaceDetectService.TAG, "verify costTime = " + processVerifyTime);
                        Slog.d(FaceDetectService.TAG, "mVerifyResult = " + MyCameraVerifyThread.this.mVerifyResult);
                    }
                    FaceDetectService.this.notifyVerifyResult(MyCameraVerifyThread.this.model, MyCameraVerifyThread.this.mVerifyResult, 1);
                }
            };
            this.mCameraVerifyThread.start();
        }
    }

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(FaceDetectService this$0, Looper looper, MyHandler -this2) {
            this(looper);
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Slog.i(FaceDetectService.TAG, "MyHandler handleMessage what = " + msg.what);
            if (FaceDetectService.this.mIsFaceUnlockEnabled || (FaceDetectService.this.mIqooSecEnable ^ 1) == 0 || (FaceDetectService.this.mBeautyCameraEnable ^ 1) == 0 || msg.what == 104 || msg.what == FaceDetectService.FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH || msg.what == FaceDetectService.FACE_DETECT_MSG_BOOT_INIT) {
                switch (msg.what) {
                    case ProcessList.VISIBLE_APP_ADJ /*100*/:
                        FaceDetectService.this.handleSuccess((String) msg.obj);
                        break;
                    case 101:
                        FaceDetectService.this.handleTimeOut((String) msg.obj);
                        break;
                    case 102:
                        FaceDetectService.this.handlePreviewTest();
                        break;
                    case 103:
                        FaceDetectService.this.handleKeyguardExit();
                        break;
                    case 104:
                        FaceDetectService.this.handleSystemReboot();
                        break;
                    case 105:
                        FaceDetectService.this.handleFingerLocked();
                        break;
                    case 107:
                        FaceDetectService.this.handleScreenOn();
                        break;
                    case 108:
                    case 109:
                        FaceDetectService.this.handleScreenOff();
                        break;
                    case 111:
                        FaceDetectService.this.handleCameraTimeOut();
                        break;
                    case 112:
                        FaceDetectService.this.handleSmartKeyOpened();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_SCREEN_ON_FINISHED /*114*/:
                        FaceDetectService.this.handleScreenOnFinished();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_VERIFY_FAILED /*120*/:
                        FaceDetectService.this.handleVerifyFailed((String) msg.obj);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED /*122*/:
                        FaceDetectService.this.handleFailScreenDoze();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_AUTHEN_NEXT /*203*/:
                        FaceDetectService.this.retryNextAuthenticate();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_START_OPERATION /*204*/:
                        FaceDetectService.this.processUnFinishedEvent();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FINISH_OPERATION /*205*/:
                        FaceDetectService.this.handleReleaseResources();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_THREAD_FINISHED /*206*/:
                        FaceDetectService.this.handleThreadFinished();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_VERIFY_TIMEOUT /*207*/:
                        FaceDetectService.this.handleThreadVerifyTimeout((String) msg.obj);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_PW_CHANGED /*208*/:
                        FaceDetectService.this.handleKeyguardSecureChanged();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_BOOT_INIT /*209*/:
                        FaceDetectService.this.handleBootInit();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_FACEKEY_FACEFP_RESULT /*220*/:
                        FaceDetectService.this.handleFaceFpResult();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_START_FACE_DETECT /*228*/:
                        FaceDetectService.this.handleStartFaceDetectByFaceKey();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_HIDE /*230*/:
                        FaceDetectService.this.handleKeyguardHide();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYGUARD_SHOW /*231*/:
                        FaceDetectService.this.handleKeyguardShow();
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_SOFT_KEYBOARD_SHOW /*232*/:
                        FaceDetectService.this.handleSoftKeyboardState(true);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_SOFT_KEYBOARD_HIDE /*233*/:
                        FaceDetectService.this.handleSoftKeyboardState(false);
                        break;
                    case FaceDetectService.FACE_DETECT_MSG_KEYCODE_SMARTWAKE /*234*/:
                        FaceDetectService.this.handleKeycodeSmartWake(msg.arg1);
                        break;
                }
            }
        }
    }

    private class MyInterfaceTestCallback implements InvocationHandler {
        /* synthetic */ MyInterfaceTestCallback(FaceDetectService this$0, MyInterfaceTestCallback -this1) {
            this();
        }

        private MyInterfaceTestCallback() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(FaceDetectService.TAG, "startAuthenticateIRTest: onPPDateFrame");
            }
            FaceDetectService faceDetectService = FaceDetectService.this;
            faceDetectService.mIrTestFrameCount = faceDetectService.mIrTestFrameCount + 1;
            if (FaceDetectService.this.mIrTestFrameCount <= 3) {
                return null;
            }
            if (!(args == null || args[0] == null || FaceDetectService.this.isIRTestTimeOut)) {
                FaceDetectShareMemory faceDetectShareMemory = null;
                try {
                    faceDetectShareMemory = new FaceDetectShareMemory(args[0], FaceDetectService.MIXED_DATA_LENGTH, true);
                } catch (IOException e) {
                    Slog.e(FaceDetectService.TAG, "startAuthenticateIRTest: error ", e);
                }
                Object data = null;
                if (faceDetectShareMemory != null && faceDetectShareMemory.getSize() == FaceDetectService.MIXED_DATA_LENGTH) {
                    data = faceDetectShareMemory.readData(FaceDetectService.MIXED_DATA_LENGTH);
                }
                if (faceDetectShareMemory != null) {
                    faceDetectShareMemory.releaseShareMemory();
                }
                if (data != null) {
                    Slog.d(FaceDetectService.TAG, "startAuthenticateIRTest: data length: " + data.length);
                    float light = FaceDetectService.this.ChooseTheIRFrame(data);
                    Slog.d(FaceDetectService.TAG, "startAuthenticateIRTest: light: " + light);
                    FaceDetectService.this.mLightMax = Math.max(FaceDetectService.this.mLightMax, light);
                    if (light >= 21.0f) {
                        faceDetectService = FaceDetectService.this;
                        faceDetectService.mIRFrameCount = faceDetectService.mIRFrameCount + 1;
                        Message message = FaceDetectService.this.mIRMemoryHandler.obtainMessage(ProcessList.UNKNOWN_ADJ, data);
                        FaceDetectService.this.mIRMemoryHandler.removeMessages(ProcessList.UNKNOWN_ADJ);
                        FaceDetectService.this.mIRMemoryHandler.sendMessage(message);
                    }
                }
            }
            return null;
        }
    }

    private class MyVerifyThread {
        private Thread mVerifyThread;
        private String model;

        public MyVerifyThread(String model) {
            this.model = model;
        }

        public void start() {
            this.mVerifyThread = new Thread() {
                public void run() {
                    Process.setThreadPriority(FaceDetectService.FACE_DETECT_NO_FACE);
                    FaceDetectService.this.processFaceVerify(MyVerifyThread.this.model);
                }
            };
            this.mVerifyThread.start();
        }
    }

    private enum PiXFormat {
        PIX_FMT_GRAY8,
        PIX_FMT_YUV420P,
        PIX_FMT_NV12,
        PIX_FMT_NV21,
        PIX_FMT_BGRA8888,
        PIX_FMT_BGR888
    }

    private class TestHandler extends Handler {
        /* synthetic */ TestHandler(FaceDetectService this$0, Looper looper, TestHandler -this2) {
            this(looper);
        }

        private TestHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ProcessList.UNKNOWN_ADJ /*1001*/:
                    FaceDetectService.this.handleIRTestWriteMemory(msg);
                    return;
                case 1002:
                    FaceDetectService.this.handleIRTestOver();
                    return;
                default:
                    return;
            }
        }
    }

    private class UnlockNotify implements FaceDetectNotify {
        /* synthetic */ UnlockNotify(FaceDetectService this$0, UnlockNotify -this1) {
            this();
        }

        private UnlockNotify() {
        }

        public void notifyFaceDetectKeyHandler(int action) {
            Slog.i(FaceDetectService.TAG, "notifyFaceDetectKeyHandler action: " + action);
            switch (action) {
                case 1:
                    FaceDetectService.this.sendMessage(107);
                    return;
                case 2:
                    if (!FaceDetectService.this.setupWizardHasRun()) {
                        Slog.i(FaceDetectService.TAG, "notifyFaceDetectKeyHandler action: " + action + ", setup wizard false");
                        FaceDetectService.this.mSystemReady = true;
                    }
                    FaceDetectService.this.sendMessage(108);
                    HashMap params = new HashMap();
                    params.put("sw", FaceDetectService.this.mIsFaceUnlockEnabled ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_FACE_UNLOCK_ENABLE, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    params = new HashMap();
                    params.put("sw", FaceDetectService.this.mIsScreenFillLightEnabled ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_SCREEN_FILL_LIGHT_ENABLE, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    params = new HashMap();
                    params.put("sw", FaceDetectService.this.mFaceUnlockWhenScreenOn ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_FACE_UNLOCK_WHEN_SCREEN_ON, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    params.put("sw", FaceDetectService.this.mUnlockKeyguardKeep ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_FACE_UNLOCK_KEEP_KEYGUARD, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    params.put("sw", FaceDetectService.this.mIqooSecEnable ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_FACE_SECURE_ENABLE, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    params.put("sw", FaceDetectService.this.mFingerFaceCombine ? "1" : "0");
                    params.put("version", FaceDetectService.PRODUCT_VERSION);
                    FaceDetectService.this.mVivoCollectData.writeData(FaceDetectService.EVENT_ID, FaceDetectService.EVENT_LABEL_FACEFP_COMBINE_ENABLE, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    return;
                case 3:
                    FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_KEYGUARD_HIDE);
                    return;
                case 4:
                    FaceDetectService.this.mSystemReady = true;
                    FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_KEYGUARD_SHOW);
                    return;
                case 5:
                    FaceDetectService.this.mUserReboot = PhoneWindowNotifyFace.getInstance().detectRebootReason();
                    FaceDetectService.this.sendMessage(104);
                    return;
                case 6:
                    FaceDetectService.this.handleNotifyScreenOn();
                    FaceDetectService.this.sendMessage(103);
                    return;
                case 7:
                    if (FaceDetectService.this.mIsFaceUnlockEnabled) {
                        FaceDetectService.this.mFingerLocked = true;
                        return;
                    }
                    return;
                case 8:
                    FaceDetectService.this.sendMessage(112);
                    return;
                case 9:
                    FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_SCREEN_ON_FINISHED);
                    return;
                case 10:
                    if (FaceDetectService.this.mBootFinished) {
                        FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_KEYGUARD_PW_CHANGED);
                        return;
                    }
                    return;
                case EmbmsOemHook.UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL /*11*/:
                    FaceDetectService.this.handleFaceKeyDown();
                    return;
                case 12:
                    FaceDetectService.this.handleFaceKeyUp();
                    return;
                case ProcessStates.WORKING /*16*/:
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        FaceDetectService.this.mKeyguardFocused = true;
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_CHANGE_FOCUSED, 0);
                        return;
                    }
                    return;
                case 17:
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        FaceDetectService.this.mKeyguardFocused = false;
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_CHANGE_FOCUSED, 0);
                        return;
                    }
                    return;
                case 18:
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_SOFT_KEYBOARD_HIDE);
                        return;
                    }
                    return;
                case 19:
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        FaceDetectService.this.sendMessage(FaceDetectService.FACE_DETECT_MSG_SOFT_KEYBOARD_SHOW);
                        return;
                    }
                    return;
                case 20:
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        Slog.i(FaceDetectService.TAG, "WINDOW_EVENT_SMART_WAKE ScreenOn: " + FaceDetectService.this.mSystemScreenOn);
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mFaceDetectHandler, FaceDetectService.FACE_DETECT_MSG_KEYCODE_SMARTWAKE, 1, 0, 0);
                        return;
                    }
                    return;
                case 21:
                    if (FaceDetectService.this.mCurrentBackLight <= 0) {
                        FaceDetectService.this.mMotionState = 1;
                        return;
                    }
                    return;
                case 22:
                    if (FaceDetectService.this.mCurrentBackLight <= 0) {
                        FaceDetectService.this.mMotionState = 2;
                        return;
                    }
                    return;
                case 23:
                    FaceDetectService.this.mIsGlobalActions = true;
                    return;
                case 24:
                    FaceDetectService.this.mIsGlobalActions = false;
                    return;
                default:
                    return;
            }
        }

        public boolean isFaceOrFingerLocked() {
            return (FaceDetectService.this.mFingerLocked || FaceDetectService.this.mRetryTimes <= 0) ? true : FaceDetectService.this.mBootFinished ^ 1;
        }

        public void notifyFaceUnlockStart() {
            if (FaceDetectService.this.mSkipKeyguardEnable && FaceDetectService.this.mKeyguardManager != null && FaceDetectService.this.mKeyguardManager.isKeyguardLocked()) {
                PhoneWindowNotifyFace.getInstance().notifyFaceUnlockStatus(1);
                FaceDetectService.this.mTimeOut = false;
                FaceDetectService.this.mHasRemove = false;
                FaceDetectService.this.mDataReadyRemove = false;
                FaceDetectService.this.mKeyguardWouldExit = false;
                if (FaceDetectService.isSupportFaceUnlockKey) {
                    FaceDetectService.this.mDozeFaceKeyTimes = 0;
                    FaceDetectService.this.mLastFaceKeyErrorTime = 0;
                }
                FaceDetectService.this.removeMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                FaceDetectService.this.removeMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT);
                FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT, 0, 0, 0);
                if (!FaceDetectService.this.mUnlockKeyguardKeep) {
                    FaceDetectService.this.forceKeyguardHide();
                }
            }
        }

        public void onBacklightStateChanged(int state, int backlight) {
            boolean z;
            if (FaceDebugConfig.DEBUG) {
                Slog.i(FaceDetectService.TAG, "backlightState: " + FaceDetectService.this.mCurrentDisplayState + " state: " + state + " mCurrentBackLight: " + FaceDetectService.this.mCurrentBackLight + " backlight: " + backlight);
            }
            FaceCameraManager -get17 = FaceDetectService.this.mFaceCameraManager;
            if (backlight > 0) {
                z = true;
            } else {
                z = false;
            }
            -get17.onScreenState(z);
            if (FaceDetectService.isSupportFaceUnlockKey) {
                if (FaceDetectService.this.mCurrentBackLight > 0 && backlight <= 0) {
                    FaceDetectService.this.mInfraredNear = false;
                    FaceDetectService.this.mMotionState = 0;
                    FaceDetectService.this.notifyScreenState(false);
                } else if (FaceDetectService.this.mCurrentBackLight <= 0 && backlight > 0) {
                    FaceDetectService.this.mInfraredNear = false;
                    FaceDetectService.this.mMotionState = 0;
                    FaceDetectService.this.notifyScreenState(true);
                }
                FaceDetectService.this.mCurrentDisplayState = state;
                FaceDetectService.this.mCurrentBackLight = backlight;
                FaceDetectService.this.sendMyMessage(FaceDetectService.this.mDirectionHandler, FaceDetectService.FACE_DETECT_MSG_SCREEN_STATUS_CHANGED, state, backlight, 0);
            }
        }

        public void notifyFingerResult(int result, int errorcode) {
            FaceDetectService.this.handleNotifyFingerResult(result, errorcode);
        }
    }

    private class UnlockTimerTask extends TimerTask {
        private String model;

        public UnlockTimerTask(String model) {
            this.model = model;
            if (FaceDebugConfig.DEBUG) {
                Slog.i(FaceDetectService.TAG, "UnlockTimerTask construct model = " + model);
            }
        }

        public void run() {
            FaceDetectService.this.sendMessage(this.model, 101);
        }
    }

    private class WriteSharedMemoryHandler extends Handler {
        /* synthetic */ WriteSharedMemoryHandler(FaceDetectService this$0, Looper looper, WriteSharedMemoryHandler -this2) {
            this(looper);
        }

        private WriteSharedMemoryHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (FaceDebugConfig.DEBUG) {
                Slog.i(FaceDetectService.TAG, "WriteSharedMemoryHandler handleMessage what = " + msg.what);
            }
            switch (msg.what) {
                case FaceDetectService.FACE_DETECT_MSG_WRITE_SHARED_MEMORY /*113*/:
                    FaceDetectService.this.writeSharedMemroy();
                    return;
                case FaceDetectService.FACE_DETECT_MSG_VERIFY_INIT /*115*/:
                    FaceDetectService.this.handleFaceVerifyInit();
                    return;
                case FaceDetectService.FACE_DETECT_MSG_VERIFY_RELEASE /*116*/:
                    FaceDetectService.this.handleFaceVerifyRelease(false);
                    return;
                case FaceDetectService.FACE_DETECT_MSG_VERIFY_RELEASE_ALL /*117*/:
                    FaceDetectService.this.handleFaceVerifyRelease(true);
                    return;
                case FaceDetectService.FACE_DETECT_MSG_CLEAR_SHARED_MEMORY /*119*/:
                    FaceDetectService.this.handleClearSharedMemory();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        if ("PD1809".equals(PRODUCT_MODEL) || "PD1809F_EX".equals(PRODUCT_MODEL) || "PD1813B".equals(PRODUCT_MODEL) || "PD1813D".equals(PRODUCT_MODEL)) {
            SKIP_FRAME_COUNT = 2;
        } else {
            SKIP_FRAME_COUNT = 3;
        }
    }

    private boolean getSecurityFaceWake() {
        return Secure.getInt(this.mContentRv, CT_SECURITY_FACE_WARKE, 1) == 0;
    }

    private static int getTypeRaiseupDetectValue() {
        try {
            Class<?> sensorClass = Class.forName("android.hardware.Sensor");
            return sensorClass.getDeclaredField("TYPE_RAISEUP_DETECT").getInt(sensorClass);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public FaceDetectService(Context context) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "TYPE_RAISEUP_DETECT = " + TYPE_RAISEUP_DETECT);
        }
        this.mContext = context;
        this.mContentRv = this.mContext.getContentResolver();
        this.mSensorThread = new HandlerThread("FaceSensor");
        this.mSensorThread.start();
        this.mSensorHandler = new Handler(this.mSensorThread.getLooper());
        this.mFaceCameraManager = new FaceCameraManager(context, this.mSensorHandler);
        this.mAdjuster = new CameraAdjuster();
        if (this.m2PdAlgEnable) {
            this.mIs2PDAlg = true;
            this.SHARE_MEMORY_MAX_SIZE = MIXED_DATA_LENGTH;
            this.mFaceCameraManager.set2PdEnable(true);
        } else {
            this.mIs2PDAlg = false;
            this.SHARE_MEMORY_MAX_SIZE = NORMAL_MEMORY_MAX_SIZE;
            this.mFaceCameraManager.set2PdEnable(false);
        }
        this.mFaceClientApp = new FaceClientApp(context);
        this.mFaceCameraManager.setCameraPreviewCallback(new CameraCallBack(this, null));
        this.mFaceCameraManager.setCameraOpenCallback(new CameraoOpenStatusCallBack(this, null));
        this.mEmptyData = new byte[this.SHARE_MEMORY_MAX_SIZE];
        this.mFaceDetectStatus = FaceDetectStatus.getInstance();
        this.mFaceNotify = new UnlockNotify(this, null);
        PhoneWindowNotifyFace.getInstance().setFaceNotifyListener(this.mFaceNotify);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mFaceDetectThread = new HandlerThread("VivoFaceDetect");
        this.mFaceDetectThread.start();
        this.mFaceDetectHandler = new MyHandler(this, this.mFaceDetectThread.getLooper(), null);
        this.mWriteSharedMemoryThread = new HandlerThread("WriteSharedMemory");
        this.mWriteSharedMemoryThread.start();
        this.mMemoryHandler = new WriteSharedMemoryHandler(this, this.mWriteSharedMemoryThread.getLooper(), null);
        this.mCameraThread = new HandlerThread("FaceDetectCamera");
        this.mCameraThread.start();
        this.mCameraHandler = new CameraHandler(this, this.mCameraThread.getLooper(), null);
        this.mDirectionThread = new HandlerThread("FaceDirection");
        this.mDirectionThread.start();
        this.mDirectionHandler = new DirectionHandler(this, this.mDirectionThread.getLooper(), null);
        FaceDebugConfig.listenDebugProp(this.mContext, this.mFaceDetectHandler);
        this.mIsSecurityFaceWake = getSecurityFaceWake();
        if (this.mIsSecurityFaceWake) {
            setFaceUnlockEnable(false);
        }
        this.mVivoCollectData = new VivoCollectData(this.mContext);
        this.mVerifyErrorCodes = new HashMap();
        this.mverifyScores = new ArrayList();
        this.mIsFaceUnlockEnabled = isFaceUnlockEnable();
        this.mIsScreenFillLightEnabled = isScreenFillLightEnable();
        this.mIsRaiseUpEnabled = isRaiseUpEnabled();
        this.mFaceUnlockWhenScreenOn = isStartWhenScreenOnEnable();
        this.mMaxFrameCount = BoostConfig.getCurrentModelSkipFrameConfig();
        initKeepMemoryEnable();
        this.mUnlockObserver = new ContentObserver(this.mFaceDetectHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                boolean z = true;
                if (System.getUriFor(FaceDetectService.SETTING_RAISE_UP_ENABLED).equals(uri)) {
                    FaceDetectService.this.mIsRaiseUpEnabled = FaceDetectService.this.isRaiseUpEnabled();
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_ENABLED).equals(uri)) {
                    FaceDetectService.this.mIsFaceUnlockEnabled = FaceDetectService.this.isFaceUnlockEnable();
                    FaceDetectService.this.enableProximitySensor();
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_START_WHEN_SCREENON).equals(uri)) {
                    FaceDetectService.this.mFaceUnlockWhenScreenOn = FaceDetectService.this.isStartWhenScreenOnEnable();
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS).equals(uri)) {
                    FaceDetectService.this.mIsScreenFillLightEnabled = FaceDetectService.this.isScreenFillLightEnable();
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_CAMERA_BEAUTY_ENABLE).equals(uri)) {
                    FaceDetectService.this.mBeautyCameraEnable = FaceDetectService.this.isBeautyCameraEnable();
                    FaceDetectService.this.updateKeepMemory();
                    if (FaceDetectService.this.mKeepMemory) {
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mMemoryHandler, FaceDetectService.FACE_DETECT_MSG_VERIFY_INIT, 0);
                    }
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_SECURE_ENABLE).equals(uri)) {
                    FaceDetectService.this.mIqooSecEnable = FaceDetectService.this.isIqoosecEnable();
                    FaceDetectService.this.enableProximitySensor();
                    FaceDetectService.this.updateKeepMemory();
                    if (FaceDetectService.this.mKeepMemory) {
                        FaceDetectService.this.sendMyMessage(FaceDetectService.this.mMemoryHandler, FaceDetectService.FACE_DETECT_MSG_VERIFY_INIT, 0);
                    }
                } else if (System.getUriFor(FaceDetectService.FINGER_UNLOCK_OPEN).equals(uri)) {
                    FaceDetectService.this.updateUdFingerEnable();
                    if (FaceDetectService.isSupportFaceUnlockKey) {
                        FaceDetectService.this.updateShowFaceIconEnable();
                    }
                } else if (System.getUriFor(FaceDetectService.FACEUNLOCK_KEYGUARD_KEEP).equals(uri)) {
                    FaceDetectService.this.updateKeyguardKeep();
                } else if (FaceDetectService.isSupportFaceUnlockKey && System.getUriFor(FaceDetectService.FINGER_FACE_COMBINE).equals(uri)) {
                    FaceDetectService.this.updateFingerFaceCombine();
                } else if (FaceDetectService.isSupportFaceUnlockKey && System.getUriFor(FaceDetectService.FINGER_SIMPINPUK).equals(uri)) {
                    FaceDetectService.this.updateSimPinState();
                } else if (System.getUriFor(FaceDetectService.KEY_FLASHLIGHT_STATE).equals(uri)) {
                    FaceDetectService faceDetectService = FaceDetectService.this;
                    if (System.getInt(FaceDetectService.this.mContentRv, FaceDetectService.KEY_FLASHLIGHT_STATE, 0) != 1) {
                        z = false;
                    }
                    faceDetectService.mIsOpenFlashLight = z;
                    Slog.i(FaceDetectService.TAG, "open Flash Light state is:" + FaceDetectService.this.mIsOpenFlashLight);
                } else if (System.getUriFor(FaceDetectService.FINGER_MOVE_WAKE).equals(uri)) {
                    FaceDetectService.this.updateFingerMoveWake();
                } else if (Secure.getUriFor(FaceDetectService.CT_SECURITY_FACE_WARKE).equals(uri)) {
                    FaceDetectService.this.mIsSecurityFaceWake = FaceDetectService.this.getSecurityFaceWake();
                    if (FaceDetectService.this.mIsSecurityFaceWake) {
                        FaceDetectService.this.setFaceUnlockEnable(false);
                    }
                }
            }
        };
        if (this.mContentRv != null) {
            this.mContentRv.registerContentObserver(System.getUriFor(SETTING_RAISE_UP_ENABLED), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_ENABLED), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_START_WHEN_SCREENON), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(KEY_FLASHLIGHT_STATE), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(Secure.getUriFor(CT_SECURITY_FACE_WARKE), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_CAMERA_BEAUTY_ENABLE), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_SECURE_ENABLE), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FINGER_UNLOCK_OPEN), true, this.mUnlockObserver);
            this.mContentRv.registerContentObserver(System.getUriFor(FACEUNLOCK_KEYGUARD_KEEP), true, this.mUnlockObserver);
            if (isSupportFaceUnlockKey) {
                this.mContentRv.registerContentObserver(System.getUriFor(FINGER_FACE_COMBINE), true, this.mUnlockObserver);
                this.mContentRv.registerContentObserver(System.getUriFor(FINGER_SIMPINPUK), true, this.mUnlockObserver);
                this.mContentRv.registerContentObserver(System.getUriFor(FINGER_MOVE_WAKE), true, this.mUnlockObserver);
            }
        }
        this.mPartialWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
        startNativeServiceBooted();
        updateScreenBrightDeafult();
        if (this.mKeepMemory) {
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 200);
        }
        this.mVivoProxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        this.mCameraDataInternalTime = BoostConfig.getCurrentCameraDataInternal();
        this.mPPDataFrameGap = this.mCameraDataInternalTime;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
        enableProximitySensor();
        sendMyMessage(this.mDirectionHandler, 300, 2000);
        updateUdFingerEnable();
        updateFingerMoveWake();
        updateKeyguardKeep();
        if (isSupportFaceUnlockKey) {
            this.mFaceSensorManager = FaceSensorManager.getInstance(this.mContext);
            this.mFaceSensorManager.setSensorCallback(this.mSensorCallback);
            updateShowFaceIconEnable();
            updateFingerFaceCombine();
            updateSimPinState();
            registerReceiver();
        }
    }

    private void enableProximitySensor() {
        this.mFaceCameraManager.enableProximitySensor(!this.mIsFaceUnlockEnabled ? this.mIqooSecEnable : true);
    }

    public static synchronized FaceDetectService getInstance(Context context) {
        FaceDetectService faceDetectService;
        synchronized (FaceDetectService.class) {
            if (sInstance == null) {
                sInstance = new FaceDetectService(context);
            }
            faceDetectService = sInstance;
        }
        return faceDetectService;
    }

    public boolean isFaceUnlockEnable() {
        boolean z = true;
        if (this.mContext == null || this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, FACEUNLOCK_ENABLED, 0) != 1) {
            z = false;
        }
        return z;
    }

    public boolean isScreenFillLightEnable() {
        boolean z = true;
        if (this.mContext == null || this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS, 1) != 1) {
            z = false;
        }
        return z;
    }

    public void setFaceUnlockEnable(boolean enabled) {
        if (this.mContentRv == null) {
            return;
        }
        if (enabled) {
            System.putInt(this.mContentRv, FACEUNLOCK_ENABLED, 1);
        } else {
            System.putInt(this.mContentRv, FACEUNLOCK_ENABLED, 0);
        }
    }

    private boolean isRaiseUpEnabled() {
        boolean z = true;
        if (this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, SETTING_RAISE_UP_ENABLED, 0) != 1) {
            z = false;
        }
        return z;
    }

    private boolean isStartWhenScreenOnEnable() {
        boolean z = true;
        if (this.mContext == null || this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, FACEUNLOCK_START_WHEN_SCREENON, 1) != 1) {
            z = false;
        }
        return z;
    }

    private int getFrameToScreenOn() {
        if (this.mContext == null || this.mContentRv == null) {
            return 3;
        }
        return System.getInt(this.mContentRv, FACEUNLOCK_LIGHT_FRAME_NUMBERS, 3);
    }

    private boolean isSystemUid(int callingUid) {
        String[] packages = null;
        long ident = Binder.clearCallingIdentity();
        PackageManager manager = this.mContext.getPackageManager();
        if (manager == null) {
            return false;
        }
        try {
            packages = manager.getPackagesForUid(callingUid);
            if (packages != null) {
                for (String name : packages) {
                    try {
                        PackageInfo packageInfo = manager.getPackageInfo(name, 0);
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 1) == 0)) {
                            return true;
                        }
                    } catch (NameNotFoundException e) {
                        Slog.w(TAG, String.format("Could not find package [%s]", new Object[]{name}), e);
                    }
                }
            } else {
                Slog.w(TAG, "No known packages with uid " + callingUid);
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isRunningForeground(String packageName) {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        try {
            ComponentName componentName = ((RunningTaskInfo) this.mActivityManager.getRunningTasks(1).get(0)).topActivity;
            if (componentName != null) {
                String currentPackageName = componentName.getPackageName();
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "currentPackageName = " + currentPackageName);
                }
                return !TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isActivityOnTop(String className) {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        if (this.mActivityManager != null) {
            ComponentName componentName = ((RunningTaskInfo) this.mActivityManager.getRunningTasks(1).get(0)).topActivity;
            if (componentName != null) {
                String topActivityName = componentName.getClassName();
                Slog.i(TAG, "isActivityOnTop: topActivityName = " + topActivityName + ", className = " + className);
                return TextUtils.equals(className, topActivityName);
            }
        }
        return false;
    }

    private boolean checkCallingPermission() {
        boolean result = true;
        int callingUid = Binder.getCallingUid();
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "callingUid = " + callingUid);
            Slog.d(TAG, "mCallingUid = " + this.mCallingUid);
        }
        if (this.mCallingUid != callingUid) {
            result = isSystemUid(callingUid);
            if (result) {
                this.mCallingUid = callingUid;
            }
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "result = " + result);
        }
        return result;
    }

    private boolean checkSystemCaller() {
        return Binder.getCallingPid() == Process.myPid();
    }

    private void cancelUnlockTimer() {
        synchronized (this.mUnlockTimerLock) {
            if (this.mUnlockTimer != null && this.mUnlockTimerTask != null) {
                this.mUnlockTimer.cancel();
                this.mUnlockTimer.purge();
                this.mUnlockTimerTask.cancel();
                this.mUnlockTimerTask = null;
                this.mUnlockTimer = null;
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG, "cancelUnlockTimer success");
                }
            } else if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "cancelUnlockTimer==null");
            }
        }
    }

    private void cancelCameraTimer() {
        synchronized (this.mCameraTimerLock) {
            if (this.mCameratime != null && this.mCameraTimerTask != null) {
                this.mCameratime.cancel();
                this.mCameratime.purge();
                this.mCameraTimerTask.cancel();
                this.mCameraTimerTask = null;
                this.mCameratime = null;
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG, "cancelCameraTimer success");
                }
            } else if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "cancelCameraTimer==null");
            }
        }
    }

    private boolean checkIfSkipFrame() {
        Slog.i(TAG, "IS_RGB_IR_SCHEME = " + FaceCameraManager.IS_RGB_IR_SCHEME);
        if (FaceCameraManager.IS_RGB_IR_SCHEME && this.mFaceCameraManager.isIrLedOpened() && this.mFrameOrder < SKIP_FRAME_COUNT) {
            return true;
        }
        return false;
    }

    private boolean setupWizardHasRun() {
        try {
            if (this.mContext.getPackageManager().getComponentEnabledSetting(new ComponentName("com.vivo.setupwizard", "com.vivo.setupwizard.LaunchActivity")) == 2) {
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG, "setupwizard component disabled");
                }
                return true;
            }
            if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "setupwizard component enabled");
            }
            return false;
        } catch (IllegalArgumentException ex) {
            Slog.w(TAG, ex.getMessage());
            return true;
        }
    }

    private void sendMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        this.mFaceDetectHandler.sendMessage(msg);
    }

    private void removeMyMessage(Handler handler, int what) {
        handler.removeMessages(what);
    }

    private void sendMyMessage(Handler handler, int what, int delay) {
        Message msg = Message.obtain();
        msg.what = what;
        if (delay <= 0) {
            handler.sendMessage(msg);
        } else {
            handler.sendMessageDelayed(msg, (long) delay);
        }
    }

    private void sendMyMessage(Handler handler, Object obj, int what, int arg1, int arg2, int delay) {
        Message msg = Message.obtain();
        if (obj != null) {
            msg.obj = obj;
        }
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (delay <= 0) {
            handler.sendMessage(msg);
        } else {
            handler.sendMessageDelayed(msg, (long) delay);
        }
    }

    private void sendMyMessage(Handler handler, int what, int arg1, int arg2, int delay) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (delay <= 0) {
            handler.sendMessage(msg);
        } else {
            handler.sendMessageDelayed(msg, (long) delay);
        }
    }

    private void handleClearSharedMemory() {
        if (this.mShareMemory != null) {
            synchronized (mShareMemoryLock) {
                this.mShareMemory.clearData();
            }
        }
    }

    private void handleTimeOut(String model) {
        resetPPDateFrameGap();
        this.mSendVerifyTimeOut = false;
        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_VERIFY_TIMEOUT);
        if (isSupportFaceUnlockKey) {
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
        }
        if (model == null) {
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            return;
        }
        boolean iskeyguard = model.equals("keyguard");
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleTimeOut model = " + model + ", mVerifyResult = " + this.mVerifyResult);
        }
        cancelUnlockTimer();
        if (isRejectFaceUnlock(model)) {
            this.mFaceUnlockOtherWay = false;
            this.mFaceResultReply = true;
            if (isSupportFaceUnlockKey) {
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
            }
            sendFaceMessageToFingerprint(5);
            this.mVerifyFinished = true;
            handleThreadFinished();
            return;
        }
        this.mVerifyFinished = true;
        notifyVerifyResult(model, this.mVerifyNoFace ? FACE_DETECT_NO_FACE : FACE_FAIL_TIMEOUT, this.mRetryTimes);
        handleThreadFinished();
    }

    private void handleSuccess(String model) {
        this.mSendVerifyTimeOut = false;
        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_VERIFY_TIMEOUT);
        if (isSupportFaceUnlockKey) {
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
        }
        if (model == null) {
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            return;
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleSucess model = " + model);
        }
        cancelUnlockTimer();
        if (isRejectFaceUnlock(model)) {
            this.mVerifyFinished = true;
            handleThreadFinished();
            return;
        }
        this.mRetryTimes = 5;
        boolean iskeyguard = model.equals("keyguard");
        Boolean obj = (Boolean) this.mNotifiedClient.get(model);
        if ((!this.mVerifyFinished && (obj == null || (obj.booleanValue() ^ 1) != 0)) || (!this.mVerifyFinished && this.mIsScreenDoze)) {
            notifyVerifyResult(model, 0, this.mRetryTimes);
            this.mNotifiedClient.put(model, Boolean.valueOf(true));
            if (iskeyguard) {
                clearLockCountDown();
            }
        }
        this.mVerifyFinished = true;
        handleThreadFinished();
    }

    private void handlePreviewTest() {
        this.cameraTimeEnd = System.currentTimeMillis() - this.mCameraTimeStart;
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "costTime cameraTimeEnd = " + this.cameraTimeEnd);
        }
        this.mTimeTest = true;
    }

    private void handleKeyguardHide() {
        Slog.d(TAG, "handleKeyguardHide: " + this.mKeyguardHide + " mSystemScreenOn: " + this.mSystemScreenOn);
        if (this.mSystemScreenOn && isKeyguardShowing() && isTopActivity(KEYGUARD_HIDE_PKGNAME)) {
            this.mKeyguardHide = true;
        }
        this.mKeyguardHideGlobal = true;
    }

    private void handleKeyguardShow() {
        Slog.d(TAG, "handleKeygurdShow: " + this.mKeyguardHide);
        this.mKeyguardHide = false;
        this.mKeyguardHideGlobal = false;
        registerProcessObserver(true);
    }

    private void handleSoftKeyboardState(boolean show) {
        Slog.d(TAG, "handleSoftKeyboardState: " + show + " mKeyguardHideGlobal: " + this.mKeyguardHideGlobal);
        if (!this.mSystemScreenOn || !this.mKeyguardHideGlobal || (isTopActivity(KEYGUARD_HIDE_PKGNAME) ^ 1) == 0) {
            return;
        }
        if (show) {
            this.mSoftKeyboardShown = true;
        } else {
            this.mSoftKeyboardShown = false;
        }
    }

    private void handleKeyguardExit() {
        int i = 1;
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleKeyguardExit");
        }
        this.mKeyguardExited = true;
        this.mIsGlobalActions = false;
        if (this.mBootFinished) {
            saveFaceUnlockData();
        }
        handleThreadFinished();
        this.mIsOpenFlashLight = false;
        this.mFaceCameraManager.keyGuardExit();
        sendMyMessage(this.mCameraHandler, 109, 0);
        registerProcessObserver(false);
        this.mKeyguardHide = true;
        this.mSkipFingerDownByProcess = false;
        this.mKeyguardHideGlobal = true;
        this.mSoftKeyboardShown = false;
        this.mKeyguardExitByFinger = false;
        this.mKeyguardExitByFace = false;
        this.mKeyguardExitByCombin = false;
        this.mKeystorehasGet = true;
        this.mKeystoreVPassword = true;
        this.mRetryTimes = 5;
        if (!this.mSmartKeyOpened) {
            this.mBootFinished = true;
            this.mFingerLocked = false;
            this.mFirstKeyguardExit = true;
        }
        this.mStage = 0;
        removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
        removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
        sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, FACE_UNLOCK_FREEMEMORY_TIME);
        this.mTimeOut = true;
        if (isSupportFaceUnlockKey) {
            this.mIsScreenDoze = false;
            this.mFingerVerifyReply = false;
            this.mFaceResultReply = false;
            this.mFingerVerifyResult = -1;
            this.mOnlyFaceUnlock = true;
            this.mFingerVerifyCode = 0;
            this.mBindServiceErrorCount = 0;
            this.mFaceKeyErrorTimes = 0;
            this.mDozeFaceKeyTimes = 0;
            this.mLastFaceKeyErrorTime = 0;
            this.mKeyguardFocused = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
            this.mKeyguardWouldExit = true;
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT, 0);
            Handler handler = this.mDirectionHandler;
            if (!this.mBootFinished) {
                i = 0;
            }
            sendMyMessage(handler, FACE_DETECT_MSG_FACEKEY_BOOT_FINISHED, i, 0, 0);
        }
    }

    private void handleNotifyScreenOn() {
        if (this.mSkipKeyguardEnable) {
            this.mFrameCount = 0;
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 0);
        }
    }

    private void handleSystemReboot() {
        boolean userReboot = this.mUserReboot;
        LockPatternUtils lockPatternUtils = new LockPatternUtils(this.mContext);
        boolean usertrigger = "trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt", "0"));
        Slog.i(TAG, "detectRebootReason userReboot = " + userReboot);
        Slog.d(TAG, "lockPatternUtils.isSecure(0) = " + lockPatternUtils.isSecure(0));
        if (!usertrigger && (lockPatternUtils.isSecure(0) ^ 1) != 0) {
            removeEnrolledFace();
            setFaceUnlockEnable(false);
            this.mIqooSecEnable = false;
            updateFaceUnlockSecureEnable(false);
        } else if (this.mIsFaceUnlockEnabled || this.mIqooSecEnable) {
            if (setupWizardHasRun()) {
                this.mBootFinished = !userReboot;
            } else if (this.mIsFaceUnlockEnabled && hasEnrolledFace()) {
                this.mBootFinished = userReboot ^ 1;
            } else {
                this.mBootFinished = true;
            }
        }
        if (hasEnrolledFace() && this.mFaceDetectNative != null) {
            Vector updateResult = this.mFaceDetectNative.processCheckIfModelUpdate();
            Slog.d(TAG, "updateResult = " + updateResult);
            if (updateResult != null) {
                int result = ((Integer) updateResult.get(0)).intValue();
                int errorImageCount = ((Integer) updateResult.get(1)).intValue();
                HashMap params = new HashMap();
                params.put("ur", Integer.toString(result));
                params.put("eic", Integer.toString(errorImageCount));
                params.put("version", PRODUCT_VERSION);
                this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_UPDATE_MODLE_RESULT, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
            }
        }
        if (isSupportFaceUnlockKey) {
            notifyBindServiceSystemBooted();
        }
    }

    private void handleFingerLocked() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleFingerLocked");
        }
        this.mFingerLocked = true;
    }

    private void handleOpenCamera(int timeout, int openmethod) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleOpenCamera mCameraOpenStatus = " + this.mCameraOpenStatus + ", timeout = " + timeout + ", mVerifyFinished = " + this.mVerifyFinished + ", mFrameOrder = " + this.mFrameOrder);
        }
        boolean isKeyguardLocked = this.mKeyguardManager != null ? this.mKeyguardManager.isKeyguardLocked() : false;
        Slog.i(TAG, "handleOpenCamera: isKeyguardLocked = " + isKeyguardLocked);
        Slog.i(TAG, "handleOpenCamera: mIqooSecEnable = " + this.mIqooSecEnable);
        if (this.mIqooSecEnable && (isKeyguardLocked ^ 1) != 0) {
            boolean isIqooSecOnTop;
            if (isActivityOnTop("com.vivo.settings.secret.PasswordActivityUD") || isActivityOnTop("com.vivo.settings.secret.PasswordActivity") || isActivityOnTop("com.vivo.settings.secret.ConfirmSecretPatternNoTitle") || isActivityOnTop("com.vivo.settings.secret.ConfirmSecretPinNoTitle")) {
                isIqooSecOnTop = true;
            } else {
                isIqooSecOnTop = isActivityOnTop("com.vivo.biometricdetect.MainActivity");
            }
            Slog.i(TAG, "handleOpenCamera: isIqooSecOnTop = " + isIqooSecOnTop);
            if (!isIqooSecOnTop) {
                return;
            }
        }
        this.handleOpenCameraTimeOut = timeout;
        this.handleOpenCameraopenMethod = openmethod;
        synchronized (mLock) {
            if (this.mCameraOpenStatus != 2) {
                sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 0);
                this.mPreviewCallbackCalled = false;
                this.mCameraTimeStart = System.currentTimeMillis();
                int openCameraValue = this.mFaceCameraManager.restartCamera();
                if (!this.mFaceCameraManager.getUseCamera2()) {
                    if (openCameraValue == 0) {
                        openCameraSuccess(openmethod, timeout);
                    } else {
                        openCameraError(timeout, openCameraValue, openmethod);
                    }
                }
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG, "handleOpenCamera end mCameraOpenStatus = " + this.mCameraOpenStatus);
                }
            }
        }
    }

    private void openCameraSuccess(int openmethod, int timeout) {
        this.mFrameOrder = 0;
        this.mOpenCameraFailCount = 0;
        this.mCameraOpenStatus = 2;
        sendMyMessage(this.mDirectionHandler, 200, 0);
        if (timeout == 1 && this.mIsFaceUnlockEnabled && this.mFaceUnlockWhenScreenOn && this.mVerifyFinished) {
            removeMyMessage(this.mCameraHandler, 111);
            sendMyMessage(this.mCameraHandler, 111, 2000);
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "open success method = " + openmethod);
        }
        this.mCamerOpenMethod = openmethod;
    }

    private void openCameraError(int timeout, int openCameraValue, int openmethod) {
        Slog.i(TAG, "openCameraError openCameraValue is: " + openCameraValue);
        if (timeout == 1 && this.mIsFaceUnlockEnabled && this.mFaceUnlockWhenScreenOn && this.mVerifyFinished) {
            removeMyMessage(this.mCameraHandler, 111);
            sendMyMessage(this.mCameraHandler, 111, 2000);
        }
        if (openCameraValue == 1) {
            this.mOpenCameraFailCount++;
            Slog.d(TAG, "handleOccupied handleStartCamera: " + this.mOpenCameraFailCount);
        }
        if (this.mOpenCameraFailCount < 3 || this.mCurrentRunModel == null) {
            removeMyMessage(this.mCameraHandler, 110);
            sendMyMessage(this.mCameraHandler, 110, timeout, openmethod, 200);
            return;
        }
        this.mOpenCameraFailCount = 0;
        removeMyMessage(this.mCameraHandler, 110);
        removeMyMessage(this.mCameraHandler, FACE_DETECT_MSG_CAMERA_OCCUPIED);
        sendMyMessage(this.mCameraHandler, this.mCurrentRunModel, FACE_DETECT_MSG_CAMERA_OCCUPIED, 0, 0, 0);
    }

    private void handleReleaseCamera() {
        Slog.i(TAG, "handleReleaseCamera: mCameraOpenStatus = " + this.mCameraOpenStatus);
        this.mPreviewCallbackCalled = false;
        removeMyMessage(this.mCameraHandler, FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_ON);
        removeMyMessage(this.mCameraHandler, 110);
        this.mNeedAdjustCamera = false;
        this.mAdjuster.reset();
        synchronized (mLock) {
            if (this.mCameraOpenStatus != 1) {
                this.mFaceCameraManager.releaseCamera();
                releaseAnimation();
                this.mCameraOpenStatus = 1;
            }
            if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "handleReleaseCamera end mCameraOpenStatus = " + this.mCameraOpenStatus);
            }
            this.mCameraTimeStart = 0;
        }
        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_DIRECTION_STOP);
        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_DIRECTION_STOP, 0);
        if (isSupportFaceUnlockKey && this.mIsScreenDoze && this.mVerifyFinished) {
            this.mIsScreenDoze = false;
            if (this.mIsIconShow) {
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
            }
        }
    }

    private void handleScreenOn() {
        Slog.i(TAG, "handleScreenOn: mKeyguardHideGlobal: " + this.mKeyguardHideGlobal);
        this.mSystemScreenOn = true;
        if (isSupportFaceUnlockKey && this.mIsScreenDoze) {
            Slog.i(TAG, "is screen doze");
        } else if (this.mIsOpenFlashLight) {
            Slog.i(TAG, "handleScreenOn fail FlashLight is open");
        } else {
            if (!isKeyguardShowing()) {
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "handleScreenOn keyguard not showing");
                }
                this.mFaceDetectWorking = false;
                if (this.mBootFinished) {
                    removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
                    removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
                    sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, FACE_UNLOCK_FREEMEMORY_TIME);
                }
            } else if (this.mKeyguardHideGlobal && isTopActivity(KEYGUARD_HIDE_PKGNAME)) {
                this.mKeyguardHide = true;
            }
            if (this.mIsRaiseUpEnabled && this.mIsFaceUnlockEnabled) {
                if (this.mSensorManager == null) {
                    this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
                }
                if (this.mSensorManager != null && this.mListenerRegistered) {
                    this.mSensorManager.unregisterListener(this.mSensorEventListener);
                    this.mListenerRegistered = false;
                }
            }
            Slog.i(TAG, "handleScreenOn: mRetryTimes = " + this.mRetryTimes);
            if (this.mIsFaceUnlockEnabled && this.mFaceUnlockWhenScreenOn && this.mBootFinished && this.mFirstKeyguardExit && this.mRetryTimes > 0) {
                removeMyMessage(this.mCameraHandler, 109);
                removeMyMessage(this.mCameraHandler, 110);
                sendMyMessage(this.mCameraHandler, 110, 0, 4, 0);
                removeMyMessage(this.mCameraHandler, 111);
                sendMyMessage(this.mCameraHandler, 111, 2000);
            }
        }
    }

    private void handleScreenOff() {
        Slog.d(TAG, "handleScreenOff");
        this.mSoftKeyboardShown = false;
        this.mFaceUnlockOtherWay = false;
        this.mSkipFingerDownByProcess = false;
        this.mSystemScreenOn = false;
        this.mKeyguardExited = false;
        this.mKeyguardExitByFace = false;
        this.mKeyguardExitByFinger = false;
        this.mKeyguardExitByCombin = false;
        this.mKeyguardWouldExit = false;
        if (isSupportFaceUnlockKey) {
            this.mIsScreenDoze = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
        }
        this.mKeyguardExited = false;
        long screenOffTime = System.currentTimeMillis();
        if (screenOffTime - this.mCameraTimeStart < 10000 && this.mStartByRaseUp) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "writeData screen on and off last " + (screenOffTime - this.mCameraTimeStart) + "ms");
            }
            this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_RAISE_UP, this.mCameraTimeStart, screenOffTime, screenOffTime - this.mCameraTimeStart, 1, null);
        }
        if (this.mSkipKeyguardEnable) {
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 0, 0, 100);
        }
        if (this.mIsRaiseUpEnabled && this.mIsFaceUnlockEnabled) {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            }
            if (this.mSensorManager == null) {
                this.mListenerRegistered = false;
            } else if (!this.mListenerRegistered) {
                this.mListenerRegistered = this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensorManager.getDefaultSensor(TYPE_RAISEUP_DETECT), 500000, null);
            }
        }
        this.mStartByRaseUp = false;
        this.mPreviewCallbackCalled = false;
        this.mStage = 0;
        this.mNeedAdjustCamera = false;
        this.mAdjuster.reset();
        if (this.mIsFaceUnlockEnabled || this.mIqooSecEnable || this.mBeautyCameraEnable) {
            this.mFaceDetectWorking = true;
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 0);
        } else {
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, 0);
        }
        this.mVerifyFinished = true;
        removeMyMessage(this.mCameraHandler, 109);
        removeMyMessage(this.mCameraHandler, 110);
        sendMyMessage(this.mCameraHandler, 109, 0);
        sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_CLEAR_SHARED_MEMORY, 0);
        if (this.mOpenCameraWhenScreenOff) {
            synchronized (mLock) {
                if (this.mFaceCameraManager.openCamera(1)) {
                    this.mCameraOpenStatus = 6;
                } else {
                    Slog.e(TAG, "handleScreenOff openCamera failed");
                }
            }
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "handleScreenOff mCameraOpenStatus = " + this.mCameraOpenStatus);
        }
        if (isSupportFaceUnlockKey) {
            if (this.mBootFinished) {
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_BIND_SERVICE, 0);
            } else {
                notifyBindServiceSystemBooted();
            }
        }
        handleKeycodeSmartWake(0);
        Slog.i(TAG, "mKeyguardLocationStatus:" + this.mKeyguardLocationStatus);
        if (isSupportFaceUnlockKey && this.mKeyguardLocationStatus == 1) {
            handleBinderIconStatus(11, this.mKeyguardLocationStatus, 0);
            sendFaceMessageToFingerprint(8);
        }
    }

    private void handleCameraTimeOut() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleCameraTimeOut");
        }
        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN);
        if (this.mSkipKeyguardEnable) {
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_KEEP_BACKLIGHT);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 0);
        }
        this.mNeedAdjustCamera = false;
        this.mAdjuster.reset();
        cancelCameraTimer();
        removeMyMessage(this.mCameraHandler, 109);
        removeMyMessage(this.mCameraHandler, 110);
        sendMyMessage(this.mCameraHandler, 109, 0);
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "handleCameraTimeOut mCameraOpenStatus = " + this.mCameraOpenStatus);
        }
    }

    private void handleSmartKeyOpened() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleSmartKeyOpened");
        }
        this.mSmartKeyOpened = true;
    }

    private void handleScreenOnFinished() {
        if (isSupportFaceUnlockKey) {
            this.mSystemScreenOn = true;
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleScreenOnFinished");
        }
        if (this.mKeyguardHideGlobal && isKeyguardShowing() && isTopActivity(KEYGUARD_HIDE_PKGNAME)) {
            this.mKeyguardHide = true;
        }
        if (this.mSmartKeyOpened && (isRunningForeground("com.android.camera") ^ 1) != 0) {
            Slog.i(TAG, "start camera in smart key.");
            Slog.i(TAG, "handleScreenOnFinished: mRetryTimes = " + this.mRetryTimes);
            if (this.mIsFaceUnlockEnabled && this.mFaceUnlockWhenScreenOn && this.mBootFinished && this.mFirstKeyguardExit && this.mRetryTimes > 0) {
                removeMyMessage(this.mCameraHandler, 109);
                removeMyMessage(this.mCameraHandler, 110);
                sendMyMessage(this.mCameraHandler, 110, 0, 4, 0);
                removeMyMessage(this.mCameraHandler, 111);
                sendMyMessage(this.mCameraHandler, 111, 2000);
            }
        }
        this.mSmartKeyOpened = false;
        if (isSupportFaceUnlockKey && this.mKeyguardLocationStatus == 1) {
            handleBinderIconStatus(11, this.mKeyguardLocationStatus, 0);
        }
    }

    private void handleFaceVerifyInit() {
        long startTime = System.currentTimeMillis();
        Slog.d(TAG, "handleFaceVerifyInit");
        if (this.mFaceDetectNative != null) {
            this.mFaceDetectNative.processFaceVerifyInit();
        }
        long endTime = System.currentTimeMillis() - startTime;
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "face verify init costTime = " + endTime);
        }
        Slog.d(TAG, "handleFaceVerifyInit end");
    }

    private void handleFaceVerifyRelease(boolean releaseall) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleFaceVerifyRelease releaseall = " + releaseall);
        }
        if (!this.mKeepMemory || (isMaxMemory() ^ 1) == 0) {
            stopNativeService();
            startNativeService();
            if (this.mKeepMemory) {
                sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 200);
            }
        }
    }

    private void handleFaceVerifyMemory(boolean enable) {
        if (!enable) {
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, 0);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSharedMemroy() {
        if (!this.mWritingMemory) {
            synchronized (mShareMemoryLock) {
                if (this.mWritingMemory || this.mIsNv21DataReady) {
                } else {
                    this.mWritingMemory = true;
                    byte[] bArr = null;
                    synchronized (this.mPreviewDataLock) {
                        if (this.mPreviewData != null) {
                            bArr = (byte[]) this.mPreviewData.clone();
                        }
                    }
                    if (FaceDebugConfig.DEBUG) {
                        Slog.d(TAG, "writeSharedMemroy  data.length = " + bArr.length);
                        Slog.d(TAG, "writeSharedMemroy  mShareMemory = " + this.mShareMemory);
                    }
                    if (this.mShareMemory != null && this.mShareMemory.writeData(bArr) && this.mPreviewData.length <= this.SHARE_MEMORY_MAX_SIZE) {
                        this.mIsNv21DataReady = true;
                        if (bArr != null) {
                            this.mPreviewDataLen = bArr.length;
                        } else {
                            this.mPreviewDataLen = 0;
                        }
                        mShareMemoryLock.notify();
                    }
                    this.mWritingMemory = false;
                }
            }
        }
    }

    private void faceVerifyRelease() {
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "faceVerifyRelease() faceVerifyRelease = " + this.mPreviewDataLen);
        }
        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN);
        removeMyMessage(this.mCameraHandler, 109);
        removeMyMessage(this.mCameraHandler, 110);
        sendMyMessage(this.mCameraHandler, 109, 0);
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "faceVerifyRelease mCameraOpenStatus = " + this.mCameraOpenStatus);
        }
        this.mSmartKeyOpened = false;
        this.mStage = 0;
        if (this.mPreviewDataLen > 0 && this.mShareMemory != null) {
            this.mShareMemory.writeData(this.mEmptyData);
        }
        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_DIRECTION_STOP, 0);
    }

    private void notifyVerifyResult(int errorCode, int retryTimes) {
        notifyVerifyResult("keyguard", errorCode, retryTimes);
    }

    private void notifyVerifyResult(String model, int errorCode, int retryTimes) {
        Slog.i(TAG, "notifyVerifyResult: with errorCode " + errorCode + ", client = " + model + ",mFaceUnlockOtherWay:" + this.mFaceUnlockOtherWay + ",mUnlockKeyguardKeep:" + this.mUnlockKeyguardKeep + ", mSkipKeyguardEnable:" + this.mSkipKeyguardEnable);
        if (model != null && model.length() > 0) {
            if (this.mCurrentRunModel != null && this.mCurrentRunModel.equals("keyguard") && this.mCurrentRunModel.equals(model)) {
                Slog.i(TAG, "notifyVerifyResult: reset current run model");
                this.mCurrentRunModel = null;
            }
            boolean iskeyguard = model.equals("keyguard");
            long verifyEndTime = System.currentTimeMillis();
            if (FaceDebugConfig.DEBUG_TIME) {
                Slog.d(TAG, "writeData verifyCostTime = " + (verifyEndTime - this.mCameraTimeStart));
            }
            int unlock_screenon = this.mFaceUnlockStartWhenScreenOn ? 1 : 0;
            if (this.mFaceUnlockOtherWay) {
            }
            this.mFaceResultReply = true;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            if (errorCode == 0 && iskeyguard) {
                this.mKeyguardExitByFace = true;
                this.mKeyguardExitByFinger = false;
                this.mKeyguardExitByCombin = isUdFingerPrintEnabled() ? this.mFaceUnlockOtherWay ^ 1 : false;
                if (this.mUnlockKeyguardKeep && this.mFaceUnlockOtherWay) {
                    if (FaceDebugConfig.DEBUG) {
                        Slog.d(TAG, "notifyVerifyResult keep then notify face hide");
                    }
                    if (isSupportFaceUnlockKey) {
                        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, errorCode, 0, 0);
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT, 0);
                    }
                    this.mKeyguardWouldExit = true;
                    sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT, 0);
                    sendFaceMessageToFingerprint(4);
                } else if (isUdFingerPrintEnabled()) {
                    Slog.d(TAG, "fp send successed to unlock:mFaceUnlockOtherWay:" + this.mFaceUnlockOtherWay + ",mSystemScreenOn:" + this.mSystemScreenOn);
                    if (isSupportFaceUnlockKey) {
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, errorCode, 0, 0);
                    }
                    if (!isFingerFaceCombine()) {
                        sendFaceMessageToFingerprint(2);
                    } else if (this.mFaceUnlockOtherWay && this.mSystemScreenOn) {
                        sendFaceMessageToFingerprint(2);
                    } else {
                        sendFaceMessageToFingerprint(3);
                    }
                }
            } else if (iskeyguard && errorCode != 0 && this.mFaceUnlockKeyEnable && isSupportFaceUnlockKey) {
                removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, errorCode, 0, 0);
            }
            HashMap<String, String> params = new HashMap();
            boolean isDark;
            if (errorCode == 0 && iskeyguard) {
                params.put("t1", Long.toString(this.mPreviewCallbackTime));
                params.put("status", "1");
                params.put("startby", Integer.toString(this.mCamerOpenMethod));
                params.put("err", Integer.toString(0));
                params.put("verifyerrorcodes", getErrorString());
                params.put("scr", Integer.toString(unlock_screenon));
                params.put("FingerFaceCombine", this.mFingerFaceCombine ? "1" : "0");
                params.put("fpsw", this.mFingerprintEnabled ? "1" : "0");
                params.put("keepsw", this.mUnlockKeyguardKeep ? "1" : "0");
                if (FaceCameraManager.IS_RGB_IR_SCHEME) {
                    isDark = this.mFaceCameraManager.getIsDarkEnvironment();
                    params.put("isDark", isDark ? "1" : "0");
                    params.put("lux", this.mFaceCameraManager.getRectifiedLuxStr());
                    params.put("isScreenOn", this.mFaceCameraManager.getIsScreenOnWhenGetLux() ? "1" : "0");
                    if (isDark) {
                        params.put("openIr", this.mFaceCameraManager.isIrLedOpened() ? "1" : "0");
                    }
                }
                params.put("version", PRODUCT_VERSION);
                this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_UNLOCK, this.mCameraTimeStart, verifyEndTime, verifyEndTime - this.mCameraTimeStart, 1, params);
            } else if (iskeyguard) {
                params.put("t1", Long.toString(this.mPreviewCallbackTime));
                params.put("status", "0");
                params.put("startby", Integer.toString(this.mCamerOpenMethod));
                params.put("err", Integer.toString(errorCode));
                params.put("lastfailerror", Integer.toString(this.mLastFaceDetectFailReason));
                params.put("verifyerrorcodes", getErrorString());
                params.put("scr", Integer.toString(unlock_screenon));
                params.put("FingerFaceCombine", this.mFingerFaceCombine ? "1" : "0");
                params.put("fpsw", this.mFingerprintEnabled ? "1" : "0");
                params.put("keepsw", this.mUnlockKeyguardKeep ? "1" : "0");
                if (FaceCameraManager.IS_RGB_IR_SCHEME) {
                    isDark = this.mFaceCameraManager.getIsDarkEnvironment();
                    params.put("isDark", isDark ? "1" : "0");
                    params.put("lux", this.mFaceCameraManager.getRectifiedLuxStr());
                    params.put("isScreenOn", this.mFaceCameraManager.getIsScreenOnWhenGetLux() ? "1" : "0");
                    if (isDark) {
                        params.put("openIr", this.mFaceCameraManager.isIrLedOpened() ? "1" : "0");
                    }
                }
                params.put("version", PRODUCT_VERSION);
                this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_UNLOCK, this.mCameraTimeStart, verifyEndTime, verifyEndTime - this.mCameraTimeStart, 1, params);
                this.mFaceErrorTime = verifyEndTime;
            }
            if (errorCode == FACE_FAIL_TIMEOUT) {
                errorCode = -1;
            }
            this.mLastFaceDetectFailReason = 0;
            this.mFaceVerifyCode = errorCode;
            if (isSupportFaceUnlockKey && iskeyguard) {
                removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
                if (errorCode != 0) {
                    sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
                    sendFaceMessageToFingerprint(5);
                    if ((this.mFingerVerifyReply && this.mFingerVerifyResult != 0) || (isUdFingerPrintEnabled() ^ 1) != 0) {
                        if (FaceDebugConfig.DEBUG) {
                            Slog.d(TAG, "notifyVerifyResult mRetryTimes = " + this.mRetryTimes);
                        }
                        if (needViber(this.mFingerVerifyCode, this.mFaceVerifyCode)) {
                            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, errorCode, -1023, 0);
                        }
                    } else if (!(this.mFaceUnlockOtherWay || !isUdFingerPrintEnabled() || (this.mFingerVerifyReply ^ 1) == 0)) {
                        if (FaceDebugConfig.DEBUG) {
                            Slog.d(TAG, "face has result, wait for fp ....");
                        }
                        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FP_VERIFY_TIMEOUT);
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FP_VERIFY_TIMEOUT, 3000);
                        return;
                    }
                }
            }
            IFaceDetectClient client;
            if ((iskeyguard && this.mSkipKeyguardEnable && (this.mTimeOut ^ 1) != 0 && errorCode == 0) || (errorCode == 0 && iskeyguard && (isFaceUnlockKeyShowEnabled() || this.mFaceUnlockOtherWay))) {
                if (!(this.mFaceUnlockOtherWay && (this.mUnlockKeyguardKeep ^ 1) == 0)) {
                    SystemProperties.set("sys.fingerprint.keguard", "1");
                    forcekeyguardExit();
                }
                if (BoostConfig.getCurrentModelNotifyConfig() == 0) {
                    removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                    sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 0);
                }
                Slog.d(TAG, "mIsScreenDoze = " + this.mIsScreenDoze + ", pm != null " + (this.pm != null));
                if (isSupportFaceUnlockKey && this.mIsScreenDoze) {
                    FaceSensorManager faceSensorManager = this.mFaceSensorManager;
                    PhoneWindowNotifyFace.getInstance().notifyFaceUnlockWakeUp();
                }
                if (this.mFaceUnlockOtherWay && this.mUnlockKeyguardKeep) {
                    Slog.i(TAG, "face unlock success other way");
                    client = (IFaceDetectClient) this.mCallBackClient.get(model);
                    if (client == null) {
                        client = this.mKeyguardClient;
                    }
                    if (client != null) {
                        try {
                            client.onAuthenticationResult(model, errorCode, retryTimes);
                        } catch (Throwable e) {
                            Slog.w(TAG, "Remote exception while face authenticating: ", e);
                        }
                    }
                }
                if (!(!this.mFaceUnlockOtherWay || (this.mUnlockKeyguardKeep ^ 1) == 0 || (this.mSkipKeyguardEnable ^ 1) == 0)) {
                    Slog.i(TAG, "face unlock success otherway not  support skipkeyguard");
                    client = (IFaceDetectClient) this.mCallBackClient.get(model);
                    if (client == null) {
                        client = this.mKeyguardClient;
                    }
                    if (client != null) {
                        try {
                            client.onAuthenticationResult(model, errorCode, retryTimes);
                        } catch (Throwable e2) {
                            Slog.w(TAG, "Remote exception while face authenticating: ", e2);
                        }
                    }
                }
            } else if (errorCode != 0 && (this.mFaceUnlockOtherWay ^ 1) != 0 && isUdFingerPrintEnabled() && iskeyguard && isSupportFaceUnlockKey) {
                handleFaceFpResult();
            } else if (errorCode != 100 && this.mFaceUnlockKeyEnable && (this.mFaceUnlockOtherWay ^ 1) != 0 && iskeyguard && isSupportFaceUnlockKey) {
                handleFacekeyResult();
            } else {
                client = (IFaceDetectClient) this.mCallBackClient.get(model);
                if (isSupportFaceUnlockKey && client == null && iskeyguard) {
                    client = this.mKeyguardClient;
                }
                if (client != null) {
                    try {
                        client.onAuthenticationResult(model, errorCode, retryTimes);
                    } catch (Throwable e3) {
                        Slog.w(TAG, "Remote exception while face authenticating: ", e3);
                    }
                }
            }
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "notify empty = " + model);
            }
            if (errorCode == 0 && iskeyguard) {
                sendFaceSuccessToSecure();
            }
            this.mStartOpenTime = 0;
            if (isSupportFaceUnlockKey) {
                this.mIsScreenDoze = false;
            }
        }
    }

    private boolean getLockDeadline() {
        long deadline = new LockPatternUtils(this.mContext).getLockoutAttemptDeadline(0);
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "getLockDeadline: " + deadline);
        }
        if (deadline > 0) {
            return true;
        }
        return false;
    }

    private boolean isRejectFaceUnlock(String model) {
        if (model == null) {
            return true;
        }
        boolean iskeyguard = model.equals("keyguard");
        if (checkCallingPermission()) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "mIsFaceUnlockEnabled = " + this.mIsFaceUnlockEnabled);
            }
            if (!this.mIsFaceUnlockEnabled && iskeyguard) {
                notifyVerifyResult(model, FACE_DETECT_DISABLED, this.mRetryTimes);
                return true;
            } else if (this.mRetryTimes == 0 && iskeyguard) {
                Slog.i(TAG, "mRetryTimes is 0.");
                notifyVerifyResult(model, FACE_FAIL_FIVE_TIMES, this.mRetryTimes);
                return true;
            } else if (getLockDeadline() && iskeyguard) {
                Slog.i(TAG, "getLockDeadline true.");
                notifyVerifyResult(model, FACE_WHEN_PASSWORD_COUNTING, this.mRetryTimes);
                return true;
            } else if (!this.mBootFinished) {
                Slog.i(TAG, "mBootFinished false.");
                notifyVerifyResult(model, FACE_WHEN_REBOOT, this.mRetryTimes);
                return true;
            } else if (!this.mFingerLocked || !iskeyguard) {
                return false;
            } else {
                Slog.i(TAG, "mFingerLocked true.");
                notifyVerifyResult(model, FACE_WHEN_FINGER_FAIL_FIVE_TIMES, this.mRetryTimes);
                return true;
            }
        }
        Slog.w(TAG, "permission denied.");
        notifyVerifyResult(model, FACE_OTHER_FAIL, this.mRetryTimes);
        return true;
    }

    private ILockSettings getLockSettings() {
        if (this.mLockSettingsService == null) {
            this.mLockSettingsService = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
        }
        return this.mLockSettingsService;
    }

    private void clearLockCountDown() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    FaceDetectService.this.getLockSettings().clearLockCountDown();
                } catch (RemoteException e) {
                }
            }
        }).start();
    }

    private static String getFormatTime(long milliseconds) {
        return new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date(milliseconds));
    }

    public void startAuthenticate(IFaceDetectClient mFaceDetectClient) {
        startAuthenticateModel(mFaceDetectClient, "keyguard");
        if (mFaceDetectClient != null) {
            this.mKeyguardClient = mFaceDetectClient;
        }
    }

    public void startAuthenticateModel(IFaceDetectClient mFaceDetectClient, String model) {
        Slog.i(TAG, "startAuthenticate:calling pid = " + Binder.getCallingPid() + ", model = " + model + ", cur = " + this.mCurrentRunModel + " mKeyguardExitByFace: " + this.mKeyguardExitByFace);
        if (model == null || model.length() <= 0) {
            Slog.e(TAG, "face detection denied/invalid model");
        } else if (this.mCurrentRunModel != null && this.mCurrentRunModel.equals("keyguard") && this.mCurrentRunModel.equals(model)) {
            Slog.w(TAG, "face detection denied/same client");
        } else if (this.mKeyguardExitByFace) {
            Slog.w(TAG, "face detection denied/face detection is already success");
        } else if (this.mUpslideExpand && isKeyguardShowing()) {
            Slog.w(TAG, "face detection denied/upslide is expanded while device is locked");
        } else {
            if (model.equals("keyguard")) {
                this.mKeyguardExited = false;
                this.mKeyguardExitByFace = false;
                if (mFaceDetectClient != null) {
                    this.mKeyguardClient = mFaceDetectClient;
                }
            }
            this.mCallBackClient.put(model, mFaceDetectClient);
            if (isRejectFaceUnlock(model)) {
                if (this.mBootFinished) {
                    removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
                    removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
                    sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, FACE_UNLOCK_FREEMEMORY_TIME);
                }
                removeMyMessage(this.mCameraHandler, 110);
                sendMyMessage(this.mCameraHandler, 109, 0);
                this.mFaceDetectWorking = false;
                return;
            }
            this.mNotifiedClient.put(model, Boolean.valueOf(false));
            if (!this.mClientModelList.contains(model)) {
                this.mClientModelList.add(model);
            }
            if (!this.mIsScreenDoze && checkSystemCaller()) {
                this.mTimeOut = true;
            }
            if (model.equals("keyguard")) {
                Slog.i(TAG, "start verify.mVerifyFinished: " + this.mVerifyFinished + ", mSystemScreenOn:" + this.mSystemScreenOn + ",mKeyguardHide:" + this.mKeyguardHide + ",mIsScreenDoze:" + this.mIsScreenDoze);
                if (!this.mIsScreenDoze) {
                    this.mFaceUnlockOtherWay = true;
                    this.mDozeFaceKeyTimes = 0;
                    this.mLastFaceKeyErrorTime = 0;
                    this.mKeyguardWouldExit = false;
                    this.mFaceUnlockStartWhenScreenOn = this.mSystemScreenOn;
                    if (this.mVerifyFinished && isSupportFaceUnlockKey && mUdFingerSupport) {
                        if (((this.mSystemScreenOn ? isTopActivity(NOT_PLAY_ANIM_PKGNAME) : 0) ^ 1) != 0) {
                            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM, 0);
                        }
                    }
                }
            }
            Slog.d(TAG, "mKeyguardLocationStatus:" + this.mKeyguardLocationStatus + " , mFaceUnlockOtherWay = " + this.mFaceUnlockOtherWay);
            if (this.mKeyguardLocationStatus != 1 && this.mFaceUnlockOtherWay && this.mFingerFaceCombine && isSupportFaceUnlockKey) {
                Slog.d(TAG, "start face otherway in keyguard page(not secure lock page)");
                sendFaceMessageToFingerprint(6);
            }
            if (!this.mProcessed) {
                sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            }
        }
    }

    private void startVerify(String model) {
        Slog.i(TAG, "start verify curr = " + model);
        if (model != null) {
            this.mCurrentRunModel = model;
            boolean iskeyguard = model.equals("keyguard");
            if (this.mVerifyFinished) {
                sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 0);
                this.mVerifyNoFace = true;
                this.mVerifyFinished = false;
                this.mIsNv21DataReady = false;
                this.mNeedAdjustCamera = false;
                if (this.mFaceDetectNative == null) {
                    this.mFaceDetectNative = FaceDetectNative.getInstance();
                }
                if (this.mShareMemory == null) {
                    try {
                        this.mShareMemory = getShareMemory();
                    } catch (Throwable e) {
                        Slog.e(TAG, "get memory failed", e);
                        this.mShareMemory = null;
                    }
                }
                if (this.mFaceDetectNative == null || this.mShareMemory == null) {
                    Slog.e(TAG, "para is null.");
                    notifyVerifyResult(model, FACE_OTHER_FAIL, this.mRetryTimes);
                    return;
                }
                resetPPDateFrameGap();
                this.mFaceVerified = false;
                this.mFrameCount = 0;
                removeMyMessage(this.mCameraHandler, 111);
                removeMyMessage(this.mCameraHandler, 109);
                removeMyMessage(this.mCameraHandler, 110);
                if (this.mIsScreenDoze) {
                    sendMyMessage(this.mCameraHandler, 110, 0, 6, 0);
                } else {
                    sendMyMessage(this.mCameraHandler, 110, 0, 3, 0);
                }
                cancelUnlockTimer();
                cancelCameraTimer();
                synchronized (this.mUnlockTimerLock) {
                    if (this.mUnlockTimer == null && this.mUnlockTimerTask == null) {
                        long delay;
                        this.mUnlockTimer = new Timer();
                        this.mUnlockTimerTask = new UnlockTimerTask(model);
                        if (!iskeyguard) {
                            delay = 3000;
                        } else if (FaceCameraManager.IS_RGB_IR_SCHEME) {
                            if (this.mIsPhoneState && this.mFaceCameraManager.getIsDarkEnvironment()) {
                                delay = 2500;
                            } else if (!this.mIsScreenDoze || (isUdFingerPrintEnabled() ^ 1) == 0) {
                                delay = 4000;
                            } else {
                                delay = 3000;
                            }
                        } else if (!this.mIsScreenDoze || (isUdFingerPrintEnabled() ^ 1) == 0) {
                            delay = 4000;
                        } else {
                            delay = 3000;
                        }
                        this.mUnlockTimer.schedule(this.mUnlockTimerTask, delay);
                    }
                    if (FaceDebugConfig.DEBUG) {
                        Slog.d(TAG, "startAuthenticate start unlock timer success");
                    }
                }
                this.mTimeTest = false;
                this.mVerifyErrorCodes.clear();
                this.mverifyScores.clear();
                HashMap params = new HashMap();
                params.put("sw", getFastUnlockEnable() ? "1" : "0");
                params.put("version", PRODUCT_VERSION);
                this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FAST_UNLOCK, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG, "new thread cur = " + model);
                }
                this.mThreadWork = true;
                this.mTrackThread = new MyVerifyThread(model);
                this.mTrackThread.start();
                return;
            }
            Slog.i(TAG, "face detect is busy.");
            notifyVerifyResult(model, -3, this.mRetryTimes);
        }
    }

    public void stopAuthenticate() {
        stopAuthenticateModel("keyguard");
    }

    public void stopAuthenticateModel(String model) {
        Slog.d(TAG, "stopAuthenticate() calling pid = " + Binder.getCallingPid());
        boolean needStopVerifyAnim = false;
        if (!this.mKeyguardWouldExit) {
            needStopVerifyAnim = true;
        }
        if (model == null) {
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
        } else if (model.equals("camera_beauty")) {
            this.mCallBackClient.remove(model);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
        } else {
            if (this.mCurrentRunModel != null && this.mCurrentRunModel.equals("keyguard") && this.mCurrentRunModel.equals(model)) {
                Slog.i(TAG, "stopAuthenticate: reset current run model");
                this.mCurrentRunModel = null;
            }
            boolean iskeyguard = model.equals("keyguard");
            if (!(this.mVerifyFinished || this.mVerifyNoFace)) {
                if (iskeyguard && this.mRetryTimes > 0) {
                    this.mRetryTimes--;
                    if (this.mRetryTimes <= 0) {
                        sendFaceMessageToFingerprint(1);
                    }
                }
                this.mVerifyNoFace = true;
            }
            if (!this.mVerifyFinished && iskeyguard) {
                this.mVerifyFinished = true;
                long verifyEndTime = System.currentTimeMillis();
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "writeData verifyCostTime = " + (verifyEndTime - this.mCameraTimeStart));
                }
                HashMap<String, String> params = new HashMap();
                params.put("t1", Long.toString(this.mPreviewCallbackTime));
                params.put("status", "0");
                params.put("startby", Integer.toString(this.mCamerOpenMethod));
                params.put("err", Integer.toString(VERIFY_STOPED_BY_USER));
                params.put("verifyerrorcodes", getErrorString());
                params.put("FingerFaceCombine", this.mFingerFaceCombine ? "1" : "0");
                params.put("fpsw", this.mFingerprintEnabled ? "1" : "0");
                if (FaceCameraManager.IS_RGB_IR_SCHEME) {
                    boolean isDark = this.mFaceCameraManager.getIsDarkEnvironment();
                    params.put("isDark", isDark ? "1" : "0");
                    params.put("lux", this.mFaceCameraManager.getRectifiedLuxStr());
                    params.put("isScreenOn", this.mFaceCameraManager.getIsScreenOnWhenGetLux() ? "1" : "0");
                    if (isDark) {
                        params.put("openIr", this.mFaceCameraManager.isIrLedOpened() ? "1" : "0");
                    }
                }
                params.put("version", PRODUCT_VERSION);
                this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_UNLOCK, this.mCameraTimeStart, verifyEndTime, verifyEndTime - this.mCameraTimeStart, 1, params);
            }
            this.mVerifyFinished = true;
            this.mSmartKeyOpened = false;
            this.mStage = 0;
            cancelUnlockTimer();
            this.mCallBackClient.remove(model);
            this.mNotifiedClient.remove(model);
            if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "stopAuthenticateModel model = " + model + ", mThreadWork = " + this.mThreadWork + ", mProcessed = " + this.mProcessed + ",needStopVerifyAnim:" + needStopVerifyAnim);
            }
            if (!this.mThreadWork && this.mProcessed) {
                this.mProcessed = false;
            }
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            if (needStopVerifyAnim && isSupportFaceUnlockKey) {
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
            }
            removeMyMessage(this.mCameraHandler, 110);
            sendMyMessage(this.mCameraHandler, 109, 0);
        }
    }

    /* JADX WARNING: Missing block: B:24:0x008e, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] enrollFaceWithImage(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        if (!checkCallingPermission()) {
            return null;
        }
        synchronized (mShareMemoryLock) {
            if (this.mFaceDetectNative != null) {
                this.erollResult = this.mFaceDetectNative.processEnroll(mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation);
            }
            if (this.erollResult != null) {
                Slog.d(TAG, "erollResult.mEnrollFinished = " + this.erollResult.mEnrollFinished);
                if (this.erollResult.mEnrollFinished && this.mFaceDetectStatus != null) {
                    this.mFaceDetectStatus.setEnrolledFaceID(FACE_ID);
                    this.mFaceDetectStatus.setEnrolledFaceStatus(true);
                }
                byte[] result = new byte[5];
                result[0] = (byte) this.erollResult.mErroCode;
                result[1] = (byte) (this.erollResult.mEnrollFinished ? 1 : 0);
                result[2] = (byte) this.erollResult.mCurrentEnrollDirect;
                result[3] = (byte) this.erollResult.mEnrollFaceStatus;
                result[4] = (byte) this.erollResult.mEnrolledDirect;
                if (this.erollResult.mErroCode == -3) {
                    transferPreviewBufferToNative();
                }
            } else {
                return null;
            }
        }
    }

    public void FaceDetectInit() {
        if (this.bInitiated) {
            Slog.d(TAG, "already initiated.");
            return;
        }
        transferPreviewBufferToNative();
        int result = this.mFaceDetectNative != null ? this.mFaceDetectNative.processFaceDetectInit() : -1;
        this.bInitiated = true;
        Slog.d(TAG, "init result = " + result);
    }

    public boolean hasEnrolledFace() {
        boolean result = this.mFaceDetectStatus.getEnrolledFaceStatus();
        Slog.i(TAG, "hasEnrolledFace result is:" + result + ",mFaceDetectNative status is:" + (this.mFaceDetectNative != null));
        if (result || this.mFaceDetectNative == null) {
            return result;
        }
        this.mFaceDetectNative.processFaceDetectInit();
        return this.mFaceDetectStatus.getEnrolledFaceStatus();
    }

    public void setEnrollPreview(FileDescriptor fd, int previewLen) {
    }

    public boolean isSupportFaceDetect() {
        return true;
    }

    public boolean getFastUnlockEnable() {
        return this.mFaceDetectStatus.getFastUnlockStatus();
    }

    public void setFastUnlockEnable(boolean enable) {
        if (checkCallingPermission()) {
            if (this.mFaceDetectNative == null) {
                this.mFaceDetectNative = FaceDetectNative.getInstance();
            }
            if ((this.mFaceDetectNative != null ? this.mFaceDetectNative.processFaceSetFastUnlock(enable) : -1) == 0 && this.mFaceDetectStatus != null) {
                this.mFaceDetectStatus.setFastUnlockStatus(enable);
            }
            return;
        }
        Slog.d(TAG, "setFastUnlockEnable permission denied.");
    }

    public int getFaceID() {
        return this.mFaceDetectStatus != null ? this.mFaceDetectStatus.getEnrolledFaceID() : 0;
    }

    public void removeEnrolledFace() {
        if (checkCallingPermission()) {
            Slog.e(TAG, "faceID removed by :" + this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()));
            if (this.mFaceDetectNative == null) {
                this.mFaceDetectNative = FaceDetectNative.getInstance();
            }
            if ((this.mFaceDetectNative != null ? this.mFaceDetectNative.processFaceDetectRemove() : -1) == 0 && this.mFaceDetectStatus != null) {
                this.mFaceDetectStatus.setEnrolledFaceID(-1);
                this.mFaceDetectStatus.setEnrolledFaceStatus(false);
                this.mFaceDetectStatus.setFaceName(null);
            }
            return;
        }
        Slog.e(TAG, "removeEnrolledFace permission denied.");
    }

    public void faceRename(byte[] faceName) {
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
        if (faceName != null && faceName.length != 0) {
            if (checkCallingPermission()) {
                if ((this.mFaceDetectNative != null ? this.mFaceDetectNative.processFaceRename(faceName) : -1) == 0) {
                    this.mFaceDetectStatus.setFaceName(faceName);
                }
                return;
            }
            Slog.e(TAG, "faceRename permission denied.");
        }
    }

    public byte[] getFaceRename() {
        byte[] faceName = this.mFaceDetectStatus.getFaceName();
        if (faceName == null || faceName.length <= 0) {
            Slog.e(TAG, "faceName is null 11");
        } else {
            String str = new String(faceName);
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "faceName = " + str);
            }
        }
        return faceName;
    }

    public long preEnroll() {
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
        long result = this.mFaceDetectNative != null ? this.mFaceDetectNative.processPreEnroll() : -1;
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "preEnroll result = " + result);
            Slog.d(TAG, "preEnroll result = " + Long.toHexString(result));
        }
        return result;
    }

    public int getRetryCount() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "getRetryCount mRetryTimes = " + this.mRetryTimes);
        }
        return this.mRetryTimes;
    }

    public boolean isFaceUnlockRunning() {
        boolean z;
        synchronized (mLock) {
            z = this.mVerifyFinished ^ 1;
        }
        return z;
    }

    public void releaseHandle() {
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
        if (this.mFaceDetectNative != null) {
            this.mFaceDetectNative.processFaceDetectRelease();
        }
    }

    public void setVerifyTestPreview(FileDescriptor fd, int previewLen) {
    }

    public int startAuthenticateTest(int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation) {
        return -1;
    }

    public void startAuthenticateIRTest(String packageName, IFaceDetectIRClient faceDetectIRClient) {
        Slog.d(TAG, "startAuthenticateIRTest: enter");
        if (this.mFaceDetectNative == null || this.mFaceCameraManager == null) {
            Slog.d(TAG, "startAuthenticateIRTest: end mFaceDetectNative " + this.mFaceDetectNative);
            try {
                faceDetectIRClient.onAuthenticationResult(FACE_WHEN_PASSWORD_COUNTING, 0.0f, 0.0f);
            } catch (RemoteException e) {
                Slog.w(TAG, "startAuthenticateIRTest Remote exception while face authenticating: ", e);
            }
        } else if (hasEnrolledFace()) {
            Slog.d(TAG, "startAuthenticateIRTest: end hasEnrolledFace");
            try {
                faceDetectIRClient.onAuthenticationResult(FACE_WHEN_FINGER_FAIL_FIVE_TIMES, 0.0f, 0.0f);
            } catch (RemoteException e2) {
                Slog.w(TAG, "startAuthenticateIRTest Remote exception while face authenticating: ", e2);
            }
        } else {
            Slog.d(TAG, "startAuthenticateIRTest: PackageName = " + packageName);
            if (packageName.equals("com.vivo.engineermode")) {
                this.mPackageName = packageName;
                this.mIFaceDetectIRClient = faceDetectIRClient;
                initIRTestData();
                HandlerThread testHandlerThread = new HandlerThread("WriteSharedMemoryIR");
                testHandlerThread.start();
                this.mIRMemoryHandler = new TestHandler(this, testHandlerThread.getLooper(), null);
                Slog.d(TAG, "startAuthenticateIRTest: processFaceVerifyInit");
                if (this.mFaceDetectNative != null) {
                    this.mFaceDetectNative.processFaceVerifyInit();
                }
                Slog.d(TAG, "startAuthenticateIRTest: restartCamera " + this.mFaceCameraManager.restartCamera());
                Slog.d(TAG, "startAuthenticateIRTest: updateCamera " + this.mFaceCameraManager.updateCamera());
                new IRVerifyTestThread(this, null).start();
                HandlerThread messageIRThread = new HandlerThread("MessageIR");
                messageIRThread.start();
                this.mMessageIRHandler = new TestHandler(this, messageIRThread.getLooper(), null);
                this.mMessageIRHandler.sendEmptyMessageDelayed(1002, 6000);
            }
        }
    }

    private void initIRTestData() {
        if (this.mShareMemory == null) {
            try {
                this.mShareMemory = getShareMemory();
            } catch (Exception e) {
                Slog.e(TAG, "get memory failed", e);
                this.mShareMemory = null;
                return;
            }
        }
        this.mShareMemory.clearData();
        this.mCompareCount = 0;
        this.mScore = 0.0f;
        this.mLightMax = 0.0f;
        this.mIRFrameCount = 0;
        this.mIrTestFrameCount = 0;
        this.isCompareDataReady = false;
        this.isIRTestTimeOut = false;
        this.mIRTestVerifyNoFace = false;
        this.mFaceCameraManager.stopPreview();
        this.mFaceCameraManager.releaseCamera();
        initPdValuesTest();
    }

    private void releaseIRTest() {
        this.isCompareDataReady = false;
        this.mIRTestVerifyNoFace = false;
        if (this.mIRMemoryHandler != null) {
            this.mIRMemoryHandler.removeMessages(ProcessList.UNKNOWN_ADJ);
        }
        if (this.mShareMemory != null) {
            this.mShareMemory.clearData();
        }
        if (this.mFaceCameraManager != null) {
            this.mFaceCameraManager.stopPreview();
            this.mFaceCameraManager.releaseCamera();
            if (this.mPPDataCallback != null) {
                this.mFaceCameraManager.setPPDataCallback(this.mPPDataCallback);
            } else {
                this.mFaceCameraManager.setPPDataCallback(null);
            }
        }
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
    }

    private void handleIRTestOver() {
        if (!this.isIRTestTimeOut) {
            this.isIRTestTimeOut = true;
            Slog.d(TAG, "startAuthenticateIRTest end");
            releaseIRTest();
            try {
                if (this.mIRTestVerifyNoFace) {
                    Slog.d(TAG, "startAuthenticateIRTest no face");
                    this.mIFaceDetectIRClient.onAuthenticationResult(FACE_DETECT_NO_FACE, this.mLightMax, 0.0f);
                } else {
                    Slog.d(TAG, "startAuthenticateIRTest mIRFrameCount = " + this.mIRFrameCount);
                    if (this.mIRFrameCount < 8) {
                        this.mIFaceDetectIRClient.onAuthenticationResult(-3, this.mLightMax, 0.0f);
                    } else {
                        Slog.d(TAG, "startAuthenticateIRTest mCompareCount = " + this.mCompareCount);
                        if (this.mCompareCount != 0) {
                            float dScore = this.mScore / ((float) this.mCompareCount);
                            Slog.d(TAG, "startAuthenticateIRTest D-Score = " + dScore);
                            if (dScore >= 22.0f) {
                                this.mIFaceDetectIRClient.onAuthenticationResult(0, this.mLightMax, dScore);
                            } else {
                                this.mIFaceDetectIRClient.onAuthenticationResult(-1, this.mLightMax, dScore);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "startAuthenticateIRTest Remote exception while face authenticating: ", e);
            }
        }
    }

    private void handleIRTestWriteMemory(Message msg) {
        Slog.d(TAG, "startAuthenticateIRTest: isWriteMemory = " + this.isWriteMemory);
        if (!this.isWriteMemory) {
            synchronized (this.mIRShareMemoryLock) {
                if (this.isWriteMemory || this.isCompareDataReady) {
                    Slog.d(TAG, "startAuthenticateIRTest: isCompareDataReady = " + this.isCompareDataReady);
                } else {
                    this.isWriteMemory = true;
                    this.mShareMemory.writeData((byte[]) ((byte[]) msg.obj).clone());
                    this.isCompareDataReady = true;
                    this.mIRShareMemoryLock.notify();
                    this.isWriteMemory = false;
                }
            }
        }
    }

    private void handleTestCompare() {
        while (!this.isIRTestTimeOut) {
            Slog.d(TAG, "startAuthenticateIRTest: handleTestCompare: ");
            synchronized (this.mIRShareMemoryLock) {
                if (!this.isCompareDataReady || this.isIRTestTimeOut) {
                    Slog.d(TAG, "startAuthenticateIRTest: return");
                    try {
                        this.mIRShareMemoryLock.wait(50);
                    } catch (InterruptedException e) {
                        Slog.d(TAG, "startAuthenticateIRTest: InterruptedException: ");
                    }
                } else {
                    byte[] data = this.mShareMemory.readData(MIXED_DATA_LENGTH);
                    long processVerifyTime = System.currentTimeMillis();
                    Vector result = null;
                    if (this.mFaceDetectNative != null) {
                        result = this.mFaceDetectNative.processVerify(MIXED_DATA_LENGTH, PiXFormat.PIX_FMT_NV21.ordinal(), 640, 480, FaceOrientation.RIGHT.getValue(), true, true, true, this.mPackageName);
                    }
                    float score = 0.0f;
                    if (result != null) {
                        this.mDirectoryVerifyResult = ((Integer) result.get(0)).intValue();
                        score = ((Float) result.get(1)).floatValue();
                    } else {
                        this.mDirectoryVerifyResult = -1;
                    }
                    Slog.d(TAG, "startAuthenticateIRTest: verify costTime = " + (System.currentTimeMillis() - processVerifyTime));
                    Slog.d(TAG, "startAuthenticateIRTest: mVerifyResult = " + this.mDirectoryVerifyResult);
                    Slog.d(TAG, "startAuthenticateIRTest: score = " + score);
                    if (this.mDirectoryVerifyResult == FACE_DETECT_ERROR_VERIYF_FAILED || this.mDirectoryVerifyResult == 0) {
                        this.mCompareCount++;
                        this.mScore += score;
                    } else if (this.mDirectoryVerifyResult == VERIFY_RESULT_NO_FACE) {
                        this.mIRTestVerifyNoFace = true;
                        Slog.d(TAG, "startAuthenticateIRTest: no face");
                        this.mMessageIRHandler.removeMessages(1002);
                        this.mMessageIRHandler.sendEmptyMessage(1002);
                    }
                    this.isCompareDataReady = false;
                }
            }
        }
    }

    private float ChooseTheIRFrame(byte[] data) {
        int sum = 0;
        int i = 0;
        while (i < 640) {
            int j = 0;
            while (j < 480) {
                if (((float) i) < (EVENT_RAISE_UP - 0.25f) * 640.0f && ((float) i) >= 320.0f / EVENT_PREDICE_RAISE_UP && ((float) j) < (EVENT_RAISE_UP - 0.25f) * 480.0f && ((float) j) >= 240.0f / EVENT_PREDICE_RAISE_UP) {
                    sum += data[((i * 480) + 6096384) + j] > (byte) 0 ? data[((i * 480) + 6096384) + j] : -data[((i * 480) + 6096384) + j];
                }
                j++;
            }
            i++;
        }
        return ((float) sum) / ((float) (((int) 320.0f) * ((int) 1131413504)));
    }

    private void initPdValuesTest() {
        MyInterfaceTestCallback handler = new MyInterfaceTestCallback(this, null);
        try {
            this.mFaceCameraManager.setPPDataCallback(Proxy.newProxyInstance(Class.forName("android.hardware.Camera$PPDataCallback").getClassLoader(), new Class[]{pdCallbackClass}, handler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int startAuthenticateWithBuffer(IFaceDetectClient mFaceDetectClient, String model, int mPreviewDatalen, int pixelFormat, int mPreviewWidth, int mPreviewHeight, int orientation, boolean livenesscheck) {
        this.mDirectoryVerifyResult = -1;
        if (model == null || (model.equals("camera_beauty") ^ 1) != 0 || mPreviewWidth <= 0 || mPreviewHeight <= 0) {
            return this.mDirectoryVerifyResult;
        }
        if (!checkCallingPermission()) {
            return this.mDirectoryVerifyResult;
        }
        if (!hasEnrolledFace()) {
            return this.mDirectoryVerifyResult;
        }
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
        if (this.mFaceDetectNative == null) {
            return this.mDirectoryVerifyResult;
        }
        if (mFaceDetectClient == null) {
            long processVerifyTime = System.currentTimeMillis();
            if (this.mFaceDetectNative != null) {
                this.mFaceDetectNative.prepareVerifyThread();
                Vector result = this.mFaceDetectNative.processVerify(mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation, livenesscheck, this.mFaceCameraManager.getIsDarkEnvironment(), this.mFaceCameraManager.isIrLedOpened());
                if (result != null) {
                    this.mDirectoryVerifyResult = ((Integer) result.get(0)).intValue();
                } else {
                    this.mDirectoryVerifyResult = -1;
                }
            }
            processVerifyTime = System.currentTimeMillis() - processVerifyTime;
            if (FaceDebugConfig.DEBUG_TIME) {
                Slog.d(TAG, "verify costTime = " + processVerifyTime);
                Slog.d(TAG, "mVerifyResult = " + this.mDirectoryVerifyResult);
            }
            if (this.mFaceDetectNative != null) {
                this.mFaceDetectNative.resetVerifyThread();
            }
        } else {
            this.mCallBackClient.put(model, mFaceDetectClient);
            new MyCameraVerifyThread(model, mPreviewDatalen, pixelFormat, mPreviewWidth, mPreviewHeight, orientation, livenesscheck).start();
            this.mDirectoryVerifyResult = 0;
        }
        return this.mDirectoryVerifyResult;
    }

    public AdjusterParams getAdjusterParams() {
        if (this.mAdjuster == null) {
            return null;
        }
        return this.mAdjuster.getParams();
    }

    public void setAdjusterParams(AdjusterParams params) {
        if (this.mAdjuster != null) {
            this.mAdjuster.setParams(params);
        }
    }

    private String getErrorString() {
        StringBuilder buffer = new StringBuilder();
        Set<Entry<Integer, Integer>> entrySet = this.mVerifyErrorCodes.entrySet();
        if (!entrySet.isEmpty()) {
            buffer.append("{");
            for (Entry<Integer, Integer> entry : entrySet) {
                buffer.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
            buffer.append("}");
        }
        if (!this.mverifyScores.isEmpty()) {
            buffer.append("{");
            for (int i = 0; i < this.mverifyScores.size(); i++) {
                buffer.append(this.mverifyScores.get(i)).append(",");
            }
            buffer.append("}");
        }
        return buffer.toString();
    }

    private void handleLowMemory() {
        Slog.i(TAG, "lowMemory working = " + this.mFaceDetectWorking + ", fingerlock = " + this.mFaceDetectWorking + ", times = " + this.mRetryTimes + ", mSystemReady = " + this.mSystemReady);
        if ((this.mSystemReady ^ 1) == 0) {
            if (!this.mFaceDetectWorking || (this.mFingerLocked ^ 1) == 0 || this.mRetryTimes <= 0) {
                removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT);
                removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
                sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, 0);
            }
        }
    }

    private void stopNativeService() {
        try {
            if (this.mFaceDetectNative != null) {
                this.mFaceDetectNative.releaseTa();
            }
            SystemProperties.set(STOP_SERVICE_PROP, "0");
            if (this.mFaceDetectNative != null) {
                FaceDetectNative faceDetectNative = this.mFaceDetectNative;
                FaceDetectNative.resetBind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startNativeService() {
        try {
            SystemProperties.set(STOP_SERVICE_PROP, "1");
            Thread.sleep(1000);
            transferPreviewBufferToNative();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startNativeServiceBooted() {
        try {
            SystemProperties.set(STOP_SERVICE_PROP, "1");
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_BOOT_INIT, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBootInit() {
        Slog.i(TAG, "handleBootInit");
        FaceDetectInit();
    }

    private void transferPreviewBufferToNative() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "transferPreviewBufferToNative");
        }
        if (this.mFaceDetectNative == null) {
            this.mFaceDetectNative = FaceDetectNative.getInstance();
        }
        FaceDetectShareMemory memory = getShareMemory();
        if (memory != null && this.mFaceDetectNative != null) {
            this.mFaceDetectNative.processFaceSetPreviewBuffer(memory.getFileDescriptor(), this.SHARE_MEMORY_MAX_SIZE, 1, this.mIs2PDAlg);
        }
    }

    public void notifyFaceUnlockEnable(boolean enable) {
        Slog.d(TAG, "notify from app face unlock enable = " + enable + ", mAppUsing = " + this.mAppUsing);
    }

    public void notifyFaceUnlockKillEnable(boolean killenable) {
        Slog.d(TAG, "notify from app face kill enable = " + killenable);
        this.mAppUsing = killenable ^ 1;
        if (this.mAppUsing) {
            removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 0);
            return;
        }
        removeMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL);
        sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_RELEASE_ALL, FACE_UNLOCK_FREEMEMORY_TIME);
    }

    private void handleRetryOpenCamera(int status, int timeout) {
        synchronized (mLock) {
            this.mPreviewCallbackCalled = false;
            this.mCameraTimeStart = System.currentTimeMillis();
            sendMyMessage(this.mDirectionHandler, 200, 0);
            int openCameraValue = this.mFaceCameraManager.restartCamera();
            if (openCameraValue == 0) {
                this.mCameraOpenStatus = status;
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "handle reopen camera status :" + this.mCameraOpenStatus);
                }
                if (timeout == 1 && this.mIsFaceUnlockEnabled && this.mFaceUnlockWhenScreenOn && this.mVerifyFinished && (this.mCameraOpenStatus == 6 || this.mCameraOpenStatus == 5)) {
                    synchronized (this.mCameraTimerLock) {
                        if (this.mCameratime == null && this.mCameraTimerTask == null) {
                            this.mCameratime = new Timer();
                            this.mCameraTimerTask = new CameraTimerTask(this, null);
                            if (FaceDebugConfig.DEBUG) {
                                Slog.i(TAG, "CameraTimerTask construct");
                            }
                            this.mCameratime.schedule(this.mCameraTimerTask, 2000);
                        }
                    }
                }
            } else if (openCameraValue != 1) {
                removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN);
                sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN, status, timeout, 200);
            } else if (this.mOpenCameraFailCount >= 3) {
                sendMessage(FACE_DETECT_MSG_CAMERA_OCCUPIED);
                this.mOpenCameraFailCount = 0;
                Slog.d(TAG, "handleOccupied handleRetryOpenCamera1: " + this.mOpenCameraFailCount);
            } else {
                this.mOpenCameraFailCount++;
                removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN);
                sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_CAMERA_RETRY_OPEN, status, timeout, 200);
                Slog.d(TAG, "handleOccupied handleRetryOpenCamera2: " + this.mOpenCameraFailCount);
            }
        }
    }

    public FileDescriptor getSharedMemoryFD(int length) {
        if (!checkCallingPermission()) {
            throw new SecurityException("Permission denied");
        } else if (length > this.SHARE_MEMORY_MAX_SIZE) {
            throw new IllegalArgumentException("length is greater than MAX SIZE: " + length);
        } else {
            FaceDetectShareMemory memory = getShareMemory();
            if (memory == null) {
                return null;
            }
            FileDescriptor fd = memory.getFileDescriptor();
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "getSharedMemoryFD " + fd);
            }
            return fd;
        }
    }

    private FaceDetectShareMemory getShareMemory() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "getShareMemory");
        }
        if (this.mShareMemory == null) {
            synchronized (mShareMemoryLock) {
                if (this.mShareMemory == null) {
                    try {
                        this.mShareMemory = new FaceDetectShareMemory(SHARE_MEMORY_NAME, this.SHARE_MEMORY_MAX_SIZE);
                    } catch (IOException e) {
                        Slog.w(TAG, "getShareMemory error", e);
                    }
                }
            }
        }
        return this.mShareMemory;
    }

    public void initEnroll() {
        if (!checkCallingPermission()) {
            throw new SecurityException("Permission denied");
        } else if (this.mFaceDetectNative != null) {
            this.mFaceDetectNative.processFaceEnrollInit();
        }
    }

    private void updateScreenBrightDeafult() {
        if (this.mContentRv != null) {
            int settings;
            try {
                settings = System.getInt(this.mContentRv, FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS, -1);
            } catch (Exception e) {
                settings = -1;
                e.printStackTrace();
            }
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "get default settings = " + settings);
            }
            if (settings == -1) {
                setFaceunlockAdjustScreenBrighteness(false);
            }
        }
    }

    private void setFaceunlockAdjustScreenBrighteness(boolean enabled) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "update enable = " + enabled);
        }
        if (this.mContentRv == null) {
            return;
        }
        if (enabled) {
            try {
                System.putInt(this.mContentRv, FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS, 1);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        System.putInt(this.mContentRv, FACEUNLOCK_ADJUST_SCREEN_BRIGHTNESS, 0);
    }

    private void processFaceVerify(String model) {
        if (model == null || model.length() <= 0) {
            this.mThreadWork = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_THREAD_FINISHED);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_THREAD_FINISHED, 0);
            return;
        }
        boolean iskeyguardunlock = model != null ? model.equals("keyguard") : false;
        long mVerifyTime = System.currentTimeMillis();
        float score = 0.0f;
        this.mVerifyFailedCount = 0;
        startAnimation();
        while (!this.mVerifyFinished && (Thread.interrupted() ^ 1) != 0) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "mIsNv21DataReady == " + this.mIsNv21DataReady + ", frame = " + this.mMaxFrameCount + ", model = " + model + ", mVerifyFinished = " + this.mVerifyFinished);
            }
            if (this.mSkipKeyguardEnable && iskeyguardunlock && !this.mTimeOut && (this.mHasRemove ^ 1) != 0 && this.mMaxFrameCount <= 50) {
                this.mHasRemove = true;
                this.mDataReadyRemove = false;
                removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 1, 0, 550);
            }
            synchronized (mShareMemoryLock) {
                if (this.mVerifyFinished || Thread.interrupted()) {
                    Slog.d(TAG, "processFaceVerify exit because face detection already finished");
                } else {
                    if (this.mIsNv21DataReady) {
                        if (this.mFaceDetectNative != null) {
                            this.mFaceDetectNative.prepareVerifyThread();
                        }
                        if (this.mHasRemove && (this.mDataReadyRemove ^ 1) != 0) {
                            this.mDataReadyRemove = true;
                            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 1, 0, ProcessList.HEAVY_WEIGHT_APP_ADJ);
                        }
                        this.mFaceOrientation = AccelerometerManager.getFaceOrientation(true);
                        long processVerifyTime = System.currentTimeMillis();
                        if (FaceDebugConfig.DEBUG_TIME) {
                            Trace.traceBegin(8, "FaceVerify");
                        }
                        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_VERIFY_TIMEOUT);
                        sendMyMessage(this.mFaceDetectHandler, model, FACE_DETECT_MSG_VERIFY_TIMEOUT, 0, 0, 10000);
                        if (!FaceCameraManager.IS_RGB_IR_SCHEME) {
                            removeMyMessage(this.mCameraHandler, FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_OFF);
                            sendMyMessage(this.mCameraHandler, FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_OFF, 0);
                        }
                        Vector result = null;
                        if (this.mVerifyFinished || Thread.interrupted()) {
                            Slog.d(TAG, "processFaceVerify exit because face detection already finished");
                        } else {
                            if (this.mFaceDetectNative != null) {
                                result = this.mFaceDetectNative.processVerify(this.mPreviewDataLen, PiXFormat.PIX_FMT_NV21.ordinal(), 640, 480, this.mFaceOrientation.getValue(), true, this.mFaceCameraManager.getIsDarkEnvironment(), this.mFaceCameraManager.isIrLedOpened());
                            }
                            if (this.mVerifyFinished || Thread.interrupted()) {
                                Slog.d(TAG, "processFaceVerify exit because face detection already finished");
                            } else {
                                removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_VERIFY_TIMEOUT);
                                if (result != null) {
                                    this.mVerifyResult = ((Integer) result.get(0)).intValue();
                                    score = ((Float) result.get(1)).floatValue();
                                } else {
                                    this.mVerifyResult = -1;
                                }
                                if (FaceDebugConfig.DEBUG_TIME) {
                                    Trace.traceEnd(8);
                                }
                                processVerifyTime = System.currentTimeMillis() - processVerifyTime;
                                if (FaceDebugConfig.DEBUG_TIME) {
                                    Slog.d(TAG, "verify costTime = " + processVerifyTime);
                                }
                                if (FaceDebugConfig.DEBUG) {
                                    Slog.d(TAG, "mVerifyResult = " + this.mVerifyResult);
                                }
                                if (this.mVerifyResult == VERIFY_RESULT_NO_FACE) {
                                    this.mContinuousNoFaceTime++;
                                    changePPDateFrameGap();
                                } else {
                                    resetPPDateFrameGap();
                                }
                                if (this.mVerifyResult != VERIFY_RESULT_NO_FACE) {
                                    this.mLastFaceDetectFailReason = this.mVerifyResult;
                                }
                                this.mVerifyErrorCodes.put(Integer.valueOf(this.mVerifyResult), Integer.valueOf((this.mVerifyErrorCodes.get(Integer.valueOf(this.mVerifyResult)) == null ? 0 : ((Integer) this.mVerifyErrorCodes.get(Integer.valueOf(this.mVerifyResult))).intValue()) + 1));
                                if (this.mVerifyResult == 0 || this.mVerifyResult == FACE_DETECT_ERROR_VERIYF_FAILED || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_FAILED || this.mVerifyResult == FACE_DETECT_ERROR_COMPARE_FAILURE || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_FAILURE || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_WARNING) {
                                    this.mverifyScores.add(Float.valueOf(score));
                                }
                                if (this.mVerifyResult == FACE_DETECT_ERROR_VERIYF_FAILED || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_FAILED || this.mVerifyResult == FACE_DETECT_ERROR_COMPARE_FAILURE || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_FAILURE || this.mVerifyResult == FACE_DETECT_ERROR_LIVENESS_WARNING) {
                                    this.mVerifyFailedCount++;
                                    if (FaceDebugConfig.DEBUG) {
                                        Slog.d(TAG, "mVerifyFailedCount = " + this.mVerifyFailedCount);
                                    }
                                    if (this.mVerifyFailedCount == 5) {
                                        if (iskeyguardunlock && this.mRetryTimes > 0) {
                                            this.mRetryTimes--;
                                            if (this.mRetryTimes <= 0) {
                                                sendFaceMessageToFingerprint(1);
                                            }
                                            if (FaceDebugConfig.DEBUG) {
                                                Slog.d(TAG, "verify mRetryTimes = " + this.mRetryTimes);
                                            }
                                        }
                                        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
                                        sendMessage(model, FACE_DETECT_MSG_VERIFY_FAILED);
                                    }
                                }
                                if (this.mVerifyResult == 0) {
                                    removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
                                    if (this.mFaceUnlockKeyEnable && iskeyguardunlock) {
                                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_KEYGUARD_EXIT, 0, 0, 0);
                                    }
                                    this.mKeyguardWouldExit = true;
                                    if (iskeyguardunlock && (this.mVerifyFinished ^ 1) != 0) {
                                        this.mKeyguardExited = true;
                                        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
                                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 0);
                                    }
                                    if (this.mFaceCameraManager != null) {
                                        this.mFaceCameraManager.updateCameraParametersControlPreviewData(0);
                                    }
                                    sendMessage(model, 100);
                                } else {
                                    if (this.mSkipKeyguardEnable && iskeyguardunlock) {
                                        this.mFrameCount++;
                                        if (this.mMaxFrameCount <= 50 && this.mFrameCount >= this.mMaxFrameCount && (this.mFaceVerified ^ 1) != 0) {
                                            this.mFaceVerified = true;
                                            sendMyMessage(this.mDirectionHandler, this.mCurrentRunModel, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 1, 0, 0);
                                        }
                                    }
                                    if (this.mVerifyResult != VERIFY_RESULT_NO_FACE) {
                                        this.mVerifyNoFace = false;
                                    } else {
                                        this.mVerifyNoFace = true;
                                    }
                                    if (this.mVerifyResult == -3) {
                                        transferPreviewBufferToNative();
                                    }
                                }
                            }
                        }
                    }
                    if (!(this.mVerifyFinished || (this.mNeedAdjustCamera ^ 1) == 0 || System.currentTimeMillis() - mVerifyTime <= 500)) {
                        this.mNeedAdjustCamera = true;
                    }
                    this.mIsNv21DataReady = false;
                    try {
                        mShareMemoryLock.wait(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if (this.mFaceDetectNative != null) {
            this.mFaceDetectNative.resetVerifyThread();
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "verify thread mVerifyFinished = " + this.mVerifyFinished);
        }
        this.mThreadWork = false;
        removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_THREAD_FINISHED);
        sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_THREAD_FINISHED, 0);
        releaseAnimation();
        return;
    }

    private void sendMessage(String model, int what) {
        Message msg = Message.obtain();
        msg.obj = model;
        msg.what = what;
        this.mFaceDetectHandler.sendMessage(msg);
    }

    private boolean isMaxMemory() {
        boolean z = true;
        if (this.mVivoProxy == null) {
            this.mVivoProxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        }
        if (this.mVivoProxy == null || getFaceDetectServicePID(runPSFaceDetectServiceShellCommand()) == -1) {
            return false;
        }
        MemoryInfo[] memoryInfos = null;
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        if (am != null) {
            try {
                memoryInfos = am.getProcessMemoryInfo(new int[]{PID});
            } catch (Exception e) {
                memoryInfos = null;
            }
        }
        if (memoryInfos == null || memoryInfos.length <= 0) {
            return false;
        }
        MemoryInfo item = memoryInfos[0];
        if (item == null) {
            return false;
        }
        int totalPss = item.getTotalPss();
        Slog.d(TAG, "current usage:" + totalPss);
        if (totalPss < MAX_LIMIT_MEMORY) {
            z = false;
        }
        return z;
    }

    private String runPSFaceDetectServiceShellCommand() {
        if (this.mVivoProxy == null) {
            this.mVivoProxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        }
        if (this.mVivoProxy == null) {
            return null;
        }
        String ret = "";
        try {
            String cmd = "";
            if (mDAEMONVERSION.equals(BoostConfig.getCurrentModelDaemonVersion())) {
                cmd = "C3hRlYPOEvgPzZrz1SmVTJHSWB944sL0Nu/oHYw2mGVavmWkUi9Lh7CyVetSzfPUd04GFq7uN650JgRo6PXrFGNcNlBn42m3eZEjbg96KAH1lQjx2WTi6qwXhmQceJOS/9OrW01kJaCvCrHcANSXFbkUqVSQvj5GYThf0uE3aOuGP7ZqQJnohKl+7mGYX9R6l6NU88Qylk4ujqkUdIo5Xz37csLZfUI/B5Fjf0GSLOzpd6kmGQrve4WvfqpeLpKsVyTLTWhcBLJuwu3I5ULPVa8iaqm4ZlnHTE/d+39C95Voa6QfDhuW4YmgbLGlM8RJbc2WvwMYGeLNndHt+nugsg==?face_detect";
            } else {
                cmd = "ps -A | grep face_detect";
            }
            Slog.w(TAG, "runPSFaceDetectServiceShellCommand cmd : " + cmd);
            ret = this.mVivoProxy.runShellWithResult(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    private int getFaceDetectServicePID(String msg) {
        int i = -1;
        if (msg == null || msg.length() <= 0) {
            return -1;
        }
        if (this.mVivoProxy == null) {
            this.mVivoProxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        }
        if (this.mVivoProxy == null) {
            return -1;
        }
        String[] strings = msg.split(" ");
        if (strings == null || strings.length == 0) {
            return -1;
        }
        boolean flag = false;
        int res = -1;
        for (int j = 0; j < strings.length; j++) {
            char[] array = strings[j].toCharArray();
            if (array != null && array.length != 0 && array[0] <= '9' && array[0] >= '0') {
                res = j;
                flag = true;
                break;
            }
        }
        if (!flag || res < 0 || res >= strings.length) {
            return -1;
        }
        Integer pid;
        try {
            pid = Integer.valueOf(Integer.parseInt(strings[res]));
        } catch (Exception e) {
            pid = null;
        }
        if (pid != null) {
            i = pid.intValue();
        }
        return i;
    }

    private int getMemoryNumber(String info) {
        int i = 0;
        String ret1 = null;
        if (info == null || info.length() <= 0) {
            return 0;
        }
        String[] array = info.split(" ");
        if (array == null) {
            return 0;
        }
        int len = array.length;
        boolean find = false;
        int i2 = 0;
        while (i2 < len) {
            String ret = array[i2];
            if (array[i2] != null && ret.equals("TOTAL:") && i2 + 1 < len) {
                ret1 = array[i2 + 1];
                if (ret1 != null && ret1.length() > 0) {
                    char chr = ret1.charAt(0);
                    if (chr >= '0' && chr <= '9') {
                        find = true;
                        break;
                    }
                }
            }
            i2++;
        }
        if (!find || ret1 == null) {
            return 0;
        }
        Integer mem;
        try {
            mem = Integer.valueOf(Integer.parseInt(ret1));
        } catch (Exception e) {
            mem = null;
        }
        if (mem != null) {
            i = mem.intValue();
        }
        return i;
    }

    private void initKeepMemoryEnable() {
        if (mRemoveSecureLock) {
            this.mIqooSecEnable = false;
            updateFaceUnlockSecureEnable(false);
        } else {
            this.mIqooSecEnable = isIqoosecEnable();
        }
        updateKeepMemory();
    }

    private boolean isBeautyCameraEnable() {
        boolean z = true;
        if (this.mContext == null || this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, FACEUNLOCK_CAMERA_BEAUTY_ENABLE, 0) != 1) {
            z = false;
        }
        return z;
    }

    private boolean isIqoosecEnable() {
        boolean z = true;
        if (this.mContext == null || this.mContentRv == null) {
            return false;
        }
        if (System.getInt(this.mContentRv, FACEUNLOCK_SECURE_ENABLE, 0) != 1) {
            z = false;
        }
        return z;
    }

    private void updateFaceUnlockSecureEnable(boolean enabled) {
        if (this.mContext != null && this.mContentRv != null) {
            System.putInt(this.mContentRv, FACEUNLOCK_SECURE_ENABLE, enabled ? 1 : 0);
        }
    }

    private void updateKeepMemory() {
        this.mKeepMemory = !this.mBeautyCameraEnable ? this.mIqooSecEnable : true;
    }

    private void sendFaceSuccessToSecure() {
        if (this.mIqooSecEnable) {
            Slog.i(TAG, "notify secure unlock");
            try {
                this.mContext.sendBroadcast(new Intent("com.vivo.facedetect.action_unlock"));
            } catch (Exception e) {
                Slog.i(TAG, "notify unlock exception e: " + e);
            }
        }
    }

    private void retryNextAuthenticate() {
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "retryNextAuthenticate currun = " + this.mCurrentRunModel);
        }
        if (this.mCurrentRunModel != null) {
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_AUTHEN_NEXT);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_AUTHEN_NEXT, 100);
            return;
        }
        boolean find = false;
        String model = null;
        for (Entry entry : this.mCallBackClient.entrySet()) {
            String key = entry.getKey();
            if (FaceDebugConfig.DEBUG) {
                Slog.i(TAG, "retryNextAuthenticate key = " + key);
            }
            if (key != null) {
                model = key;
                if (!model.equals("camera_beauty")) {
                    find = true;
                    break;
                }
            }
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.i(TAG, "retryNextAuthenticate next = " + model);
        }
        if (find) {
            this.mCurrentRunModel = model;
            Slog.i(TAG, "startAuthenticate new model = " + this.mCurrentRunModel);
            startVerify(model);
        }
    }

    private void processUnFinishedEvent() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "process unfinished event processing " + this.mProcessed + ", size = " + this.mClientModelList.size());
        }
        if (this.mProcessed) {
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 100);
            return;
        }
        this.mProcessed = true;
        if (this.mClientModelList.size() <= 0) {
            this.mProcessed = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_FINISH_OPERATION);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_FINISH_OPERATION, 0);
            return;
        }
        String model = (String) this.mClientModelList.get(0);
        if (model == null || model.length() <= 0) {
            this.mProcessed = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_FINISH_OPERATION);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_FINISH_OPERATION, 0);
            return;
        }
        this.mClientModelList.remove(0);
        startVerify(model);
    }

    private void handleReleaseResources() {
        resetPPDateFrameGap();
        removeMyMessage(this.mCameraHandler, 111);
        removeMyMessage(this.mCameraHandler, 110);
        sendMyMessage(this.mCameraHandler, 109, 0);
        synchronized (mShareMemoryLock) {
            mShareMemoryLock.notify();
        }
        if (this.mPreviewDataLen > 0 && this.mShareMemory != null) {
            this.mShareMemory.writeData(this.mEmptyData);
        }
        if (isSupportFaceUnlockKey) {
            this.mIsScreenDoze = false;
        }
    }

    private void handleThreadFinished() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle thread finished " + this.mVerifyFinished + ", mProcessed = " + this.mProcessed);
        }
        if (this.mVerifyFinished) {
            this.mProcessed = false;
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION);
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
        }
        releaseAnimation();
    }

    private void handleThreadVerifyTimeout(String model) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle thread verify timeout finished =  " + this.mVerifyFinished + ", model = " + model);
        }
        stopNativeService();
        startNativeService();
        if (this.mKeepMemory) {
            sendMyMessage(this.mMemoryHandler, FACE_DETECT_MSG_VERIFY_INIT, 200);
        }
    }

    private void handleKeyguardSecureChanged() {
        if (!new LockPatternUtils(this.mContext).isSecure(0) && hasEnrolledFace()) {
            removeEnrolledFace();
            setFaceUnlockEnable(false);
            this.mIsFaceUnlockEnabled = false;
            this.mIqooSecEnable = false;
            updateFaceUnlockSecureEnable(false);
        }
    }

    private void handleCameraOccupied(String model) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleCameraOccupied model = " + model);
        }
        if (model == null) {
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            return;
        }
        cancelUnlockTimer();
        if (isRejectFaceUnlock(model)) {
            this.mVerifyFinished = true;
            return;
        }
        this.mVerifyFinished = true;
        synchronized (mShareMemoryLock) {
            mShareMemoryLock.notify();
        }
        notifyVerifyResult(model, FACE_CAMERA_OCCUPIED, this.mRetryTimes);
        sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
    }

    private void handleVerifyFailed(String model) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleVerifyFailed model = " + model);
        }
        this.mSendVerifyTimeOut = false;
        cancelUnlockTimer();
        if (isRejectFaceUnlock(model)) {
            this.mVerifyFinished = true;
            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_OPERATION, 0);
            return;
        }
        this.mVerifyNoFace = true;
        this.mVerifyFinished = true;
        notifyVerifyResult(model, -1, this.mRetryTimes);
        handleThreadFinished();
    }

    private void handleKeycodeSmartWake(int enableStatus) {
        Slog.i(TAG, "handleKeycodeSmartWake:ScreenOn:" + this.mSystemScreenOn + ",enable:" + enableStatus);
        if (this.mFingerprintEnabled && (this.mFingerFaceCombine ^ 1) == 0) {
            if (!this.mSystemScreenOn) {
                handleBinderIconStatus(12, enableStatus, 0);
            }
            return;
        }
        Slog.i(TAG, "FingerFaceCombine is close");
    }

    private void sendFaceMessageToFingerprint(int state) {
        FingerprintNotify.getInstance().notifyMessage(state);
    }

    private void handleSyncCameraPreviewOff() {
        if (this.mFaceCameraManager != null) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "onPPDataFrame sync camera preview data switch off times = " + this.mPPDataFrameGap);
            }
            this.mFaceCameraManager.updateCameraParametersControlPreviewData(0);
            removeMyMessage(this.mCameraHandler, FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_ON);
            sendMyMessage(this.mCameraHandler, FACE_DETECT_MSG_SYNC_CAMERA_PREVIEW_ON, this.mPPDataFrameGap);
        }
    }

    private void handleSyncCameraPreviewOn() {
        if (this.mFaceCameraManager != null) {
            Slog.d(TAG, "onPPDataFrame sync camera preview data switch on");
            this.mFaceCameraManager.updateCameraParametersControlPreviewData(1);
        }
    }

    private void changePPDateFrameGap() {
        if (this.mContinuousNoFaceTime > 2) {
            this.mPPDataFrameGap += 50;
            if (this.mPPDataFrameGap > ProcessList.SERVICE_ADJ) {
                this.mPPDataFrameGap = ProcessList.SERVICE_ADJ;
            }
        }
    }

    private void resetPPDateFrameGap() {
        this.mPPDataFrameGap = this.mCameraDataInternalTime;
        this.mContinuousNoFaceTime = 0;
    }

    public boolean isDarkEnvironment() {
        Slog.i(TAG, "isDarkEnvironment: ");
        boolean isDarkEnvironment = this.mFaceCameraManager.getIsDarkEnvironment();
        Slog.i(TAG, "isDarkEnvironment: " + isDarkEnvironment);
        return isDarkEnvironment;
    }

    public boolean isIRLedAvailable() {
        Slog.i(TAG, "isIRLedAvailable: ");
        boolean isIrLedAvailable = this.mFaceCameraManager.getIsIrLedAvailable();
        Slog.i(TAG, "isIRLedAvailable: " + isIrLedAvailable);
        return isIrLedAvailable;
    }

    public void setParam(boolean isDarkEnvironment) {
        Slog.i(TAG, "setParam: isDarkEnvironment = " + isDarkEnvironment);
        this.mFaceCameraManager.setParam(isDarkEnvironment);
    }

    public void notifyBrightnessChange(boolean changed) {
        Slog.i(TAG, "notifyBrightnessChange: " + changed);
        PhoneWindowNotifyFace.getInstance().notifyBrightnessChange(changed);
    }

    private void forceKeyguardHide() {
        this.mKeyguardStatus = 2;
        updateKeyguardStatus(2);
    }

    private void forceKeyguardShow() {
        this.mKeyguardStatus = 1;
        updateKeyguardStatus(1);
    }

    private void forcekeyguardExit() {
        this.mKeyguardStatus = 3;
        updateKeyguardStatus(3);
    }

    private void updateKeyguardStatus(int status) {
        PhoneWindowNotifyFace.getInstance().notifyKeyguardStatus(status);
    }

    private void notifyClientResult(String model, int errorCode, int retryTimes) {
        if (model != null) {
            IFaceDetectClient client = (IFaceDetectClient) this.mCallBackClient.get(model);
            if (client != null) {
                try {
                    client.onAuthenticationResult(model, errorCode, retryTimes);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Remote exception while face authenticating: ", e);
                }
            }
        }
    }

    private void handleKeepBacklightKey() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handleKeepBacklightKey hold backlight");
        }
        this.mStartOpenTime = System.currentTimeMillis();
        PhoneWindowNotifyFace.getInstance().notifyFaceUnlockStatus(1);
        this.mTimeOut = false;
        this.mHasRemove = false;
        this.mDataReadyRemove = false;
        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH);
        if (this.mMaxFrameCount > 50) {
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, this.mMaxFrameCount);
        } else {
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 1, 0, this.MSG_TURNON_SCREEN_MIN_TIMEOUT);
        }
        if (!this.mUnlockKeyguardKeep) {
            forceKeyguardHide();
        }
    }

    private void handleKeepBacklightWakeUp() {
        this.mStartOpenTime = System.currentTimeMillis();
        PhoneWindowNotifyFace.getInstance().notifyFaceUnlockStatus(1);
        this.mTimeOut = false;
        this.mHasRemove = false;
        this.mDataReadyRemove = false;
        if (this.mMaxFrameCount > 50) {
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, this.mMaxFrameCount);
        } else {
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_KEYGUARD_EXIT_FINISH, 1, 0, this.MSG_TURNON_SCREEN_MIN_TIMEOUT);
        }
        if (!this.mUnlockKeyguardKeep) {
            forceKeyguardHide();
        }
    }

    private void handleBacklightStateChanged(int state, int backlight) {
        Slog.d(TAG, "onBacklightStateChanged state: " + state + ", backlight = " + backlight + ", mScreenState = " + this.mScreenState);
        if (isSupportFaceUnlockKey) {
            bindService();
            Slog.d(TAG, "onBacklightStateChanged state: " + state + ", backlight = " + backlight + ", mScreenState = " + this.mScreenState);
            putMsgIdInQueue(7, state, backlight, 0);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 0);
        }
    }

    private boolean bindService() {
        if (this.mIFaceDetectDozeService != null) {
            return false;
        }
        Intent intent = new Intent("com.vivo.framework.facedetect.IFaceDetectDozeService");
        intent.setComponent(new ComponentName(this.PKG_SERVICE, this.NAME_SERVICE));
        if (this.mContext.bindServiceAsUser(intent, this.mFaceDetectDozeConnection, 1, UserHandle.SYSTEM)) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "bindService succeed");
            }
        } else if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "bindService failed");
        }
        return true;
    }

    public void unbindService() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "faceDetectScreenDoze unbindService");
        }
        new Intent("com.vivo.framework.facedetect.IFaceDetectDozeService").setComponent(new ComponentName(this.PKG_SERVICE, this.NAME_SERVICE));
        this.mContext.unbindService(this.mFaceDetectDozeConnection);
    }

    public void handleFailScreenDoze() {
        Slog.i(TAG, "handleFailScreenDoze: " + this.mFingerVerifyReply + " mVerifyFailedCount: " + this.mVerifyFailedCount + " mRetryTimes: " + this.mRetryTimes);
        if (this.mFingerVerifyReply) {
            if (this.mVerifyFailedCount > 0 && this.mRetryTimes > 0) {
                this.mRetryTimes--;
                if (this.mRetryTimes <= 0) {
                    sendFaceMessageToFingerprint(1);
                }
            }
            handleTimeOut("keyguard");
            return;
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle face 1s time out not has fp result");
        }
    }

    private void handleBindService(int status) {
        bindService();
    }

    private void handleBinderIconStatus(int status) {
        if ((2 == status || 3 == status) && !(this.mFingerprintEnabled && (this.mFingerFaceCombine ^ 1) == 0)) {
            Slog.w(TAG, "skip finger down/biometric authentication disabled");
        } else {
            handleBinderIconStatus(status, 0, 0);
        }
    }

    private void handleBinderIconStatus(int status, int errorcode, int retrytime) {
        Slog.d(TAG, "handleBinderIconStatus state = " + status);
        trimMsgIdInQueue(status);
        putMsgIdInQueue(status, errorcode, retrytime, 0);
        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 0);
    }

    private boolean notifyServiceMessage(int status, int errorcode, int retrytime) {
        Slog.d(TAG, "notifyServiceMessage state = " + status + ", code = " + errorcode);
        bindService();
        try {
            if (this.mIFaceDetectDozeService != null) {
                this.mIFaceDetectDozeService.onFaceUnlockIconStatus(status, errorcode, retrytime);
                return true;
            }
            Slog.d(TAG, "bind service is null ");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isUdFingerPrintEnabled() {
        return mUdFingerSupport ? this.mFingerprintEnabled : false;
    }

    private void updateUdFingerEnable() {
        boolean z = false;
        if (this.mContentRv != null) {
            int settings;
            try {
                settings = System.getInt(this.mContentRv, FINGER_UNLOCK_OPEN, 0);
            } catch (Exception e) {
                settings = 0;
                e.printStackTrace();
            }
            if (settings > 0) {
                z = true;
            }
            this.mFingerprintEnabled = z;
        }
    }

    private boolean isKeyguardKeep() {
        return this.mUnlockKeyguardKeep;
    }

    private void updateKeyguardKeep() {
        boolean z = false;
        if (this.mContentRv != null) {
            int settings;
            try {
                settings = System.getInt(this.mContentRv, FACEUNLOCK_KEYGUARD_KEEP, 0);
            } catch (Exception e) {
                settings = 0;
                e.printStackTrace();
            }
            if (settings > 0) {
                z = true;
            }
            this.mUnlockKeyguardKeep = z;
        }
    }

    private void updateShowFaceIconEnable() {
        int i = 0;
        this.mFaceUnlockKeyEnable = false;
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "update show facekey ud support = " + mUdFingerSupport + ", finger enabled = " + this.mFingerprintEnabled);
        }
        if (mUdFingerSupport && (this.mFingerprintEnabled ^ 1) == 0) {
            if (mUdFingerSupport) {
                i = this.mFingerprintEnabled;
            }
            this.mFaceUnlockKeyEnable = i ^ 1;
            return;
        }
        this.mFaceUnlockKeyEnable = isSupportFaceUnlockKey;
    }

    private boolean isFaceUnlockKeyShowEnabled() {
        return this.mFaceUnlockKeyEnable;
    }

    private void updateFingerFaceCombine() {
        boolean z = false;
        if (this.mContentRv != null) {
            int settings;
            try {
                settings = System.getInt(this.mContentRv, FINGER_FACE_COMBINE, 0);
            } catch (Exception e) {
                settings = 0;
                e.printStackTrace();
            }
            if (settings > 0) {
                z = true;
            }
            this.mFingerFaceCombine = z;
            if (this.mFaceCameraManager != null) {
                this.mFaceCameraManager.setFingerFaceCombine(this.mFingerFaceCombine);
            }
        }
    }

    private void updateFingerMoveWake() {
        boolean z = true;
        if (this.mContentRv != null) {
            int settings;
            try {
                settings = System.getInt(this.mContentRv, FINGER_MOVE_WAKE, 1);
            } catch (Exception e) {
                settings = 1;
                e.printStackTrace();
            }
            Slog.d(TAG, "updateFingerMoveWake: " + settings);
            if (settings <= 0) {
                z = false;
            }
            this.mFingerMoveWakeEnabled = z;
        }
    }

    private void updateSimPinState() {
        if (this.mContentRv != null) {
            String pinState = System.getString(this.mContentRv, FINGER_SIMPINPUK);
            Slog.d(TAG, "updateSimPinState: " + pinState);
            this.mSimPinEnable = SIM_STATE_DISABLE.equals(pinState) ^ 1;
        }
    }

    private void handleFaceFpResult() {
        removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
        IFaceDetectClient client = this.mKeyguardClient;
        Slog.i(TAG, "fp result = " + this.mFingerVerifyResult + ", face result = " + this.mFaceVerifyCode + ", code = " + this.mFingerVerifyCode + ",mFaceUnlockOtherWay=" + this.mFaceUnlockOtherWay);
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "fp reply = " + this.mFingerVerifyReply + ", face reply = " + this.mFaceResultReply + ", client = " + client + ", mCurrentDisplayState = " + this.mCurrentDisplayState + ", mCurrentBackLight = " + this.mCurrentBackLight);
        }
        if (!this.mFingerVerifyReply || (this.mFaceResultReply ^ 1) != 0 || client == null || this.mFingerVerifyResult == 0) {
            Slog.i(TAG, "Not need to send result");
            return;
        }
        int finalerrorCode;
        if (this.mRetryTimes <= 2 && this.mRetryTimes > 0) {
        }
        if (!this.mFaceUnlockOtherWay) {
            switch (this.mFingerVerifyResult) {
                case -1:
                    switch (this.mFaceVerifyCode) {
                        case FACE_DETECT_ERROR_KEYSTORE_ERROR /*-100*/:
                        case FACE_DETECT_DISABLED /*-7*/:
                        case FACE_WHEN_REBOOT /*-6*/:
                        case FACE_WHEN_PASSWORD_COUNTING /*-5*/:
                        case FACE_WHEN_FINGER_FAIL_FIVE_TIMES /*-4*/:
                            finalerrorCode = this.mFaceVerifyCode;
                            break;
                        case VERIFY_RESULT_NO_FACE /*-14*/:
                        case FACE_DETECT_NO_FACE /*-2*/:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL_NOFACE;
                            break;
                        case -1:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL_FACE_FAIL;
                            break;
                        default:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL;
                            break;
                    }
                    Slog.d(TAG, "mFingerVerifyCode = " + this.mFingerVerifyCode);
                    switch (this.mFingerVerifyCode) {
                        case 101:
                            finalerrorCode = FACE_WHEN_REBOOT;
                            break;
                        case 102:
                            if (this.mRetryTimes > 0) {
                                finalerrorCode = FACE_UNLOCK_FP_FAIL_FIVE_TIMES;
                                break;
                            } else {
                                finalerrorCode = FACE_UNLOCK_BOTH_FAIL_FIVE_TIMES;
                                break;
                            }
                        case 103:
                            this.mFingerLocked = true;
                            if (this.mRetryTimes > 0) {
                                finalerrorCode = FACE_UNLOCK_FP_FAIL_FIVE_TIMES;
                                break;
                            } else {
                                finalerrorCode = FACE_UNLOCK_BOTH_FAIL_FIVE_TIMES;
                                break;
                            }
                        case 104:
                            finalerrorCode = FACE_UNLOCK_FP_COVER_ALL;
                            break;
                        case 105:
                            finalerrorCode = FACE_UNLOCK_FP_TINY;
                            break;
                        case 106:
                            finalerrorCode = FACE_UNLOCK_FP_STAY_LONG;
                            break;
                        case 107:
                        case 112:
                            if (this.mFaceVerifyCode != -1 || this.mRetryTimes > 2 || this.mRetryTimes <= 0) {
                                if (this.mKeyguardLocationStatus != 1) {
                                    finalerrorCode = FACE_UNLOCK_FP_COM_LESS_TRD;
                                    break;
                                }
                            }
                            finalerrorCode = FACE_UNLOCK_FP_BOTH_FAIL_TRD;
                            break;
                            break;
                        case 108:
                            if (this.mFaceVerifyCode == -1 && this.mRetryTimes <= 2 && this.mRetryTimes > 0) {
                                finalerrorCode = FACE_UNLOCK_FP_FACE_FAIL_TRD;
                                break;
                            }
                        case 110:
                            finalerrorCode = FACE_WHEN_PASSWORD_COUNTING;
                            break;
                    }
                    Slog.d(TAG, "mFingerVerifyCode = " + this.mFingerVerifyCode);
                    break;
                default:
                    switch (this.mFaceVerifyCode) {
                        case FACE_DETECT_ERROR_KEYSTORE_ERROR /*-100*/:
                        case FACE_DETECT_DISABLED /*-7*/:
                        case FACE_WHEN_REBOOT /*-6*/:
                        case FACE_WHEN_PASSWORD_COUNTING /*-5*/:
                        case FACE_WHEN_FINGER_FAIL_FIVE_TIMES /*-4*/:
                            finalerrorCode = this.mFaceVerifyCode;
                            break;
                        case VERIFY_RESULT_NO_FACE /*-14*/:
                        case FACE_DETECT_NO_FACE /*-2*/:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL_NOFACE;
                            break;
                        case -1:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL_FACE_FAIL;
                            break;
                        default:
                            finalerrorCode = FACE_UNLOCK_FP_FAIL;
                            break;
                    }
            }
        }
        finalerrorCode = FACE_UNLOCK_FP_FAIL;
        this.mFaceVerifyCode = -3;
        this.mFaceResultReply = false;
        if (this.mBootFinished && this.mRetryTimes <= 0 && this.mFingerVerifyCode != 102) {
            finalerrorCode = FACE_UNLOCK_FACE_FAIL_FIVE_TIMES;
        }
        if (102 == this.mFingerVerifyCode) {
            finalerrorCode = FACE_UNLOCK_FP_FAIL_FIVE_TIMES;
        }
        if (this.mFingerLocked && this.mRetryTimes <= 0) {
            finalerrorCode = FACE_UNLOCK_BOTH_FAIL_FIVE_TIMES;
        } else if (this.mFingerLocked) {
            finalerrorCode = FACE_UNLOCK_FP_FAIL_FIVE_TIMES;
        } else if (this.mRetryTimes <= 0) {
            finalerrorCode = FACE_UNLOCK_FACE_FAIL_FIVE_TIMES;
        }
        boolean needturnon = false;
        if (!this.mBootFinished && (this.mCurrentDisplayState != 2 || this.mCurrentBackLight <= 0)) {
            needturnon = true;
        }
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "fp result = " + this.mFingerVerifyCode + ", face result = " + this.mFaceVerifyCode + ", client code = " + finalerrorCode + " mRetryTimes: " + this.mRetryTimes);
        }
        try {
            if (getLockDeadline()) {
                finalerrorCode = FACE_WHEN_PASSWORD_COUNTING;
            }
            client.onAuthenticationResult("keyguard", finalerrorCode, this.mRetryTimes);
        } catch (Exception e) {
            Slog.w(TAG, "Remote exception while face authenticating: ", e);
        }
        if (needturnon) {
            wakeupTiny();
        }
    }

    private void saveUnlockMethod(int method) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "unlock method = " + method);
        }
        HashMap params = new HashMap();
        params.put("lm", method > 0 ? "1" : "0");
        params.put("version", PRODUCT_VERSION);
        this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_UNLOCK_UNLOCK_METHOD, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
    }

    private void saveErrorTime() {
        if (FaceDebugConfig.DEBUG_TIME) {
            Slog.d(TAG, "err fp time = " + this.mFingerErrorTime + ", face time = " + this.mFaceErrorTime);
        }
        if (this.mFingerVerifyCode != 0 && this.mFaceVerifyCode != 0) {
            HashMap params = new HashMap();
            params.put("fp_c", Integer.toString(this.mFingerVerifyCode));
            params.put("fp", getFormatTime(this.mFingerErrorTime));
            params.put("ft_vc", Integer.toString(this.mVerifyResult));
            params.put("ft_c", Integer.toString(this.mFaceVerifyCode));
            params.put("ft", getFormatTime(this.mFaceErrorTime));
            params.put("version", PRODUCT_VERSION);
            this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_FP_FACE_FAILED_TIME, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:9:0x0014, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean needViber(int fpcode, int facecode) {
        if (this.mFingerLocked || this.mRetryTimes <= 0 || !isUdFingerPrintEnabled() || this.mFaceUnlockOtherWay || facecode == FACE_DETECT_DISABLED || facecode == FACE_WHEN_PASSWORD_COUNTING || facecode == FACE_WHEN_REBOOT || facecode == FACE_DETECT_ERROR_KEYSTORE_ERROR || fpcode == 101 || fpcode == 110) {
            return false;
        }
        return true;
    }

    private void handleNotifyFingerResult(int result, int errorcode) {
        if (isSupportFaceUnlockKey && !this.mKeyguardExited) {
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FP_VERIFY_TIMEOUT);
            if (isUdFingerPrintEnabled()) {
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "handleNotifyFingerResult result = " + result + ", code = " + errorcode + ", mFaceResultReply = " + this.mFaceResultReply);
                }
                this.mFingerVerifyReply = true;
                this.mFingerVerifyResult = result;
                this.mFingerVerifyCode = errorcode;
                if (result != 0) {
                    this.mFingerErrorTime = System.currentTimeMillis();
                    long fingertime = this.mFingerErrorTime - this.mFaceKeyDownTime;
                    if (fingertime < 1000) {
                        if (FaceDebugConfig.DEBUG) {
                            Slog.d(TAG, "handleNotifyFingerResult finger authentication failed within 1 second");
                        }
                        return;
                    } else if (this.mFaceResultReply) {
                        if (FaceDebugConfig.DEBUG) {
                            Slog.d(TAG, "handleNotifyFingerResult finger time = " + fingertime + ", mRetryTimes = " + this.mRetryTimes);
                        }
                        if (needViber(this.mFingerVerifyCode, this.mFaceVerifyCode)) {
                            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, this.mFaceVerifyCode, -1023, 0);
                        }
                        sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_FACEKEY_FACEFP_RESULT, 0);
                    } else {
                        if (FaceDebugConfig.DEBUG) {
                            Slog.d(TAG, "handleNotifyFingerResult finger time = " + fingertime);
                        }
                        this.mVerifyFinished = true;
                        if (isSupportFaceUnlockKey) {
                            Slog.d(TAG, "finger has result exceed 1S, then face failed immediately.");
                            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
                            sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED, 0);
                        }
                    }
                } else if (this.mRetryTimes > 0) {
                    this.mVerifyFinished = true;
                    this.mRetryTimes = 5;
                    this.mKeyguardExitByCombin = true;
                    this.mKeyguardExitByFinger = true;
                    if (isSupportFaceUnlockKey) {
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_RESULT, result, this.mFingerVerifyCode, 0);
                    }
                } else {
                    return;
                }
                this.mIsScreenDoze = false;
            }
        }
    }

    private boolean isreJectFaceKeyUnlock() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "mBootFinished = " + this.mBootFinished + "  mFingerLocked = " + this.mFingerLocked + "  mFaceUnlockKeyEnable: " + this.mFaceUnlockKeyEnable);
        }
        if (getLockDeadline()) {
            Slog.d(TAG, "biometric authentication disabled/lock dealine mode");
            return true;
        } else if (!this.mBootFinished) {
            this.mFingerVerifyReply = true;
            this.mFingerVerifyResult = -1;
            this.mFingerVerifyCode = FACE_WHEN_REBOOT;
            this.mFaceResultReply = true;
            this.mFaceVerifyCode = FACE_WHEN_REBOOT;
            this.mVerifyResult = FACE_WHEN_REBOOT;
            if (this.mFaceUnlockKeyEnable) {
                handleFacekeyResult();
            } else {
                handleFaceFpResult();
            }
            return true;
        } else if (this.mFingerLocked) {
            return true;
        } else {
            if (this.mRetryTimes <= 0) {
                this.mFingerVerifyReply = true;
                this.mFingerVerifyResult = -1;
                this.mFingerVerifyCode = 102;
                this.mFaceResultReply = true;
                this.mFaceVerifyCode = FACE_WHEN_FINGER_FAIL_FIVE_TIMES;
                this.mVerifyResult = FACE_WHEN_FINGER_FAIL_FIVE_TIMES;
                handleFaceFpResult();
                return true;
            } else if (this.mFingerVerifyCode == 103) {
                Slog.d(TAG, "finger verify fail five times");
                return true;
            } else {
                Slog.d(TAG, "mIsScreenDoze = " + this.mIsScreenDoze + "  mIsIconShow = " + this.mIsIconShow);
                return this.mIsScreenDoze || (this.mIsIconShow ^ 1) != 0;
            }
        }
    }

    private void handleStartFaceDetectByFaceKey() {
        Slog.d(TAG, "start face detection by face key");
        notifyKeyguardStartAuthenticate();
    }

    private void handleFaceKeyDown() {
        Slog.d(TAG, "handleFaceKeyDown: mVerifyFinished: " + this.mVerifyFinished + " mSystemScreenOn: " + this.mSystemScreenOn + " mCurrentBackLight: " + this.mCurrentBackLight);
        if (!isSupportFaceUnlockKey || (this.mVerifyFinished ^ 1) != 0 || (mUdFingerSupport ^ 1) != 0) {
            return;
        }
        if (!this.mSystemScreenOn || (isKeyguardShowing() ^ 1) == 0) {
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            if (!this.mIsFaceUnlockEnabled) {
                Slog.w(TAG, "skip finger down/face detection disabled");
                return;
            } else if (!this.mFingerprintEnabled || (this.mFingerFaceCombine ^ 1) != 0) {
                Slog.w(TAG, "skip finger down/biometric authentication disabled");
                return;
            } else if (this.mUnlockKeyguardKeep && this.mFaceVerifyCode == 0 && this.mSystemScreenOn) {
                Slog.w(TAG, "skip finger down/device is unlocked by face detection");
                return;
            } else if (this.mSystemScreenOn && this.mKeyguardHide) {
                Slog.w(TAG, "skip finger down/keyguard is not showing");
                return;
            } else if (!this.mSimPinEnable) {
                Slog.w(TAG, "skip finger down/sim pin disabled");
                return;
            } else if (this.mSuperPowerModeOpen) {
                Slog.w(TAG, "skip finger down/enter super power mode");
                return;
            } else if (this.mSoftKeyboardShown) {
                Slog.w(TAG, "skip finger down/soft keyboard shown");
                return;
            } else if (this.mSystemScreenOn && this.mSkipFingerDownByProcess) {
                Slog.w(TAG, "skip finger down/keyguard is covered");
                return;
            } else if (this.mMotionState == 1 && this.mCurrentBackLight <= 0) {
                Slog.w(TAG, "skip finger down/motion is still");
                return;
            } else if (this.mInfraredNear && this.mCurrentBackLight <= 0) {
                Slog.w(TAG, "skip finger down/infrared is near");
                return;
            } else if (!this.mFingerMoveWakeEnabled && this.mCurrentBackLight <= 0) {
                Slog.w(TAG, "skip finger down/backlight is off and fp move wake is disabled");
                return;
            } else if (this.mUpslideExpand && isKeyguardShowing()) {
                Slog.w(TAG, "skip finger down/upslide is expanded while device is locked");
                return;
            } else if (!isreJectFaceKeyUnlock()) {
                if (this.mIsGlobalActions) {
                    Slog.w(TAG, "skip finger down/show GlobalActions");
                    return;
                } else if (isFaceIconHide()) {
                    this.mIsIconShow = false;
                    sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_HIDE_FACE_ICON, 0);
                    return;
                } else {
                    if (this.mFaceUnlockOtherWay) {
                        this.mVerifyFinished = true;
                    }
                    this.mFaceKeyDownTime = System.currentTimeMillis();
                    this.mFaceUnlockOtherWay = false;
                    this.mIsKeyDown = true;
                    this.mIsScreenDoze = true;
                    this.mTimeOut = true;
                    this.mFingerVerifyReply = false;
                    this.mFingerVerifyResult = -1;
                    this.mFingerVerifyCode = 0;
                    this.mFaceResultReply = false;
                    this.mKeyguardWouldExit = false;
                    this.mFaceUnlockStartWhenScreenOn = this.mSystemScreenOn;
                    Slog.i(TAG, "startAuthenticate mSystemScreenOn = " + this.mSystemScreenOn);
                    removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FP_VERIFY_TIMEOUT);
                    sendMyMessage(this.mCameraHandler, 110, 1, 6, 0);
                    sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_FACE_DETECT, 100);
                    sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM, 100);
                    removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
                    if (isUdFingerPrintEnabled()) {
                        sendMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED, 1000);
                    }
                    Slog.i(TAG, "handleFaceKeyDown:mFaceUnlockOtherWay:" + this.mFaceUnlockOtherWay);
                    return;
                }
            } else {
                return;
            }
        }
        Slog.w(TAG, "skip finger down/keyguard is unlocked");
    }

    private void handleFaceKeyUp() {
        Slog.i(TAG, "handleFaceKeyUp: mCurrentBackLight: " + this.mCurrentBackLight);
        if (!this.mBootFinished || (this.mIsScreenDoze ^ 1) != 0 || (isSupportFaceUnlockKey ^ 1) != 0) {
            return;
        }
        if (!this.mIsFaceUnlockEnabled) {
            Slog.w(TAG, "skip finger up/face detection disabled");
        } else if (!this.mFingerprintEnabled || (this.mFingerFaceCombine ^ 1) != 0) {
            Slog.w(TAG, "skip finger up/biometric authentication disabled");
        } else if (this.mSystemScreenOn && this.mKeyguardHide) {
            Slog.w(TAG, "skip finger up/keyguard is not showing");
        } else if (!this.mSimPinEnable) {
            Slog.w(TAG, "skip finger up/sim pin disabled");
        } else if (this.mSuperPowerModeOpen) {
            Slog.w(TAG, "skip finger up/enter super power mode");
        } else if (this.mSoftKeyboardShown) {
            Slog.w(TAG, "skip finger up/soft keyboard shown");
        } else if (this.mSystemScreenOn && this.mSkipFingerDownByProcess) {
            Slog.w(TAG, "skip finger up/keyguard is covered");
        } else if (System.currentTimeMillis() - this.mFaceKeyDownTime < 100) {
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "touch time less than wake time");
            }
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_START_FACE_DETECT);
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_VERIFY_ANIM);
            removeMyMessage(this.mFaceDetectHandler, FACE_DETECT_MSG_SCREEN_DOZE_VERIFY_FAILED);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_STOP_VERIFY_ANIM, 0);
            if (isUdFingerPrintEnabled()) {
                sendFaceMessageToFingerprint(5);
            }
            this.mIsScreenDoze = false;
            stopAuthenticate();
        }
    }

    private void wakeupTiny() {
        PhoneWindowNotifyFace.getInstance().notifyFaceUnlockWakeUp();
    }

    private boolean isFaceIconHide() {
        if (!isKeyguardShowing() && (this.mSystemScreenOn ^ 1) != 0) {
            return false;
        }
        String runningCmp = getCurrentRunningTopPackage();
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "keyguard f = " + this.mKeyguardFocused + ", running = " + runningCmp);
        }
        if (runningCmp == null || this.mKeyguardFocused) {
            Slog.i(TAG, "mKeyguardFocused is false");
            return false;
        } else if (!"com.android.camera".equals(runningCmp) && !"com.android.dialer".equals(runningCmp)) {
            return false;
        } else {
            Slog.i(TAG, "Top package is disable");
            return true;
        }
    }

    private boolean isKeyguardShowing() {
        boolean isKeyguardShowing = ((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked();
        Slog.d(TAG, "isKeyguardShowing: " + isKeyguardShowing);
        return isKeyguardShowing;
    }

    private void notifyKeyguardStartAuthenticate() {
        String model = "keyguard";
        IFaceDetectClient client = (IFaceDetectClient) this.mCallBackClient.get(model);
        if (isSupportFaceUnlockKey && client == null) {
            client = this.mKeyguardClient;
        }
        Slog.d(TAG, "notifyKeyguardStartAuthenticate");
        if (client != null) {
            try {
                client.onAuthenticationResult(model, FACE_UNLOCK_FP_START_VERIFY, this.mRetryTimes);
            } catch (Exception e) {
                Slog.w(TAG, "Remote exception while face authenticating: ", e);
            }
        }
    }

    private void handleFacekeyResult() {
        int finalerrorCode = this.mFaceVerifyCode;
        IFaceDetectClient client = this.mKeyguardClient;
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "face result = " + this.mFaceVerifyCode + ", dtimes = " + this.mDozeFaceKeyTimes + ", mCurrentDisplayState = " + this.mCurrentDisplayState + ", mCurrentBackLight = " + this.mCurrentBackLight);
        }
        if (client != null) {
            boolean needturnon = false;
            if (this.mIsScreenDoze) {
                if (this.mFaceVerifyCode == -1) {
                    this.mFaceKeyErrorTimes++;
                    this.mDozeFaceKeyTimes++;
                    long times = System.currentTimeMillis();
                    if (this.mDozeFaceKeyTimes >= 2) {
                        times = this.mFaceKeyDownTime - this.mLastFaceKeyErrorTime;
                        if (times <= 2000 && (this.mCurrentDisplayState != 2 || this.mCurrentBackLight <= 0)) {
                            needturnon = true;
                            finalerrorCode = FACE_UNLOCK_FP_FACE_FAIL_TRD;
                        }
                    }
                    this.mLastFaceKeyErrorTime = times;
                } else if (FACE_DETECT_NO_FACE == this.mFaceVerifyCode) {
                    this.mFaceKeyErrorTimes++;
                }
                if (this.mFaceKeyErrorTimes >= 50) {
                    this.mRetryTimes = 0;
                    sendFaceMessageToFingerprint(1);
                    finalerrorCode = -1;
                }
            } else if (!this.mBootFinished) {
                notifyBindServiceSystemBooted();
                if (this.mCurrentDisplayState != 2 || this.mCurrentBackLight <= 0) {
                    needturnon = true;
                }
            }
            try {
                client.onAuthenticationResult("keyguard", finalerrorCode, this.mRetryTimes);
            } catch (Exception e) {
                Slog.w(TAG, "Remote exception while face authenticating: ", e);
            }
            if (needturnon) {
                FaceSensorManager faceSensorManager = this.mFaceSensorManager;
                wakeupTiny();
            }
        }
    }

    private String getCurrentRunningTopPackage() {
        ComponentName componentName = null;
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        try {
            componentName = ((RunningTaskInfo) this.mActivityManager.getRunningTasks(1).get(0)).topActivity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (componentName != null) {
            return componentName.getPackageName();
        }
        return null;
    }

    private void handleKeyguardFocusedChanged() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle keyguard focused changed mIsIconShow = " + this.mIsIconShow);
        }
        if ((isSupportFaceUnlockKey && !this.mKeyguardExited && !this.mKeyguardWouldExit) || !this.mSystemScreenOn) {
            if (isKeyguardShowing() || !this.mSystemScreenOn) {
                boolean hide = isFaceIconHide();
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "handle keyguard focused changed need hide = " + hide);
                }
                if (this.mIsIconShow != (hide ^ 1)) {
                    if (hide) {
                        this.mIsIconShow = hide ^ 1;
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_HIDE_FACE_ICON, 0);
                    } else if (this.mVerifyFinished) {
                        this.mIsIconShow = hide ^ 1;
                        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_FACEKEY_SHOW_FACE_ICON, 0);
                    }
                    return;
                }
                return;
            }
            Slog.w(TAG, "handle keyguard focused changed keygaurd is unlocked");
        }
    }

    public void notifyFaceKeyguardStatus(int status) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "notifyFaceKeyguardStatus: " + status + ",mSystemScreenOn:" + this.mSystemScreenOn + ",code:" + this.mFingerVerifyCode);
        }
        this.mKeyguardLocationStatus = status;
        if (status != 1) {
            handleBinderIconStatus(11, status, 0);
            sendFaceMessageToFingerprint(8);
        } else if (this.mSystemScreenOn) {
            handleBinderIconStatus(11, status, 0);
            sendFaceMessageToFingerprint(7);
        }
    }

    public void notifyOtherMessage(String message, int param, int extra) {
        Slog.d(TAG, "notifyOtherMessage: " + message + " param: " + param + " extra: " + extra);
        if (!"sensor_move_wake".equals(message)) {
            return;
        }
        if (isFingerFaceCombine() && this.mFingerMoveWakeEnabled) {
            this.mMotionState = param;
        } else {
            Slog.w(TAG, "notifyOtherMessage: sensor message failed");
        }
    }

    private void handleBindServiceStatusChanged(int connected) {
        boolean z = true;
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "bind service connected: " + connected);
        }
        if (connected != 1) {
            z = false;
        }
        this.mServiceConnected = z;
        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 0);
    }

    private void notifyBindServiceSystemBooted() {
        int i;
        trimMsgIdInQueue(8);
        if (this.mBootFinished) {
            i = 1;
        } else {
            i = 0;
        }
        putMsgIdInQueue(8, i, 0, 0);
        sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 0);
    }

    private void handleProcessMsgQueue() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle msg queue processed = " + this.mMsgQueueProcessed + ", service status = " + this.mServiceConnected);
        }
        if (!isSupportFaceUnlockKey) {
            return;
        }
        if (!this.mServiceConnected || this.mIFaceDetectDozeService == null) {
            bindService();
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 1000);
        } else if (this.mMsgQueueProcessed) {
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 100);
        } else {
            this.mMsgQueueProcessed = true;
            if (this.mMsgQueueList.size() <= 0) {
                this.mMsgQueueProcessed = false;
                if (FaceDebugConfig.DEBUG) {
                    Slog.d(TAG, "handle msg queue all finished");
                }
                return;
            }
            FaceKeyEventNode node = (FaceKeyEventNode) this.mMsgQueueList.get(0);
            synchronized (this.mListLock) {
                this.mMsgQueueList.remove(0);
            }
            if (node == null) {
                this.mMsgQueueProcessed = false;
                removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE);
                sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 0);
                Slog.w(TAG, "handleProcessMsgQueue: node id null. size=" + this.mMsgQueueList.size());
                return;
            }
            if (FaceDebugConfig.DEBUG) {
                Slog.d(TAG, "handle msg queue unfinished event = " + node);
            }
            sendMyMessage(this.mDirectionHandler, node, FACE_DETECT_MSG_HANDLE_MSG, 0, 0, 0);
        }
    }

    private void handleMsgQueue(FaceKeyEventNode msg) {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "handle event msg: " + msg + ", connetct = " + this.mServiceConnected + ", service = " + this.mIFaceDetectDozeService);
        }
        if (!this.mServiceConnected || this.mIFaceDetectDozeService == null) {
            trimMsgIdInQueue(msg.mEventId);
            putMsgIdInQueue(msg, 0);
            this.mMsgQueueProcessed = false;
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 1000);
        } else if (notifyServiceMessage(msg.mEventId, msg.mErrorCode, msg.mExtraInfo)) {
            this.mMsgQueueProcessed = false;
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 100);
        } else {
            trimMsgIdInQueue(msg.mEventId);
            putMsgIdInQueue(msg, 0);
            this.mMsgQueueProcessed = false;
            removeMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE);
            sendMyMessage(this.mDirectionHandler, FACE_DETECT_MSG_PROCESS_MSG_QUEUE, 1000);
        }
    }

    private void trimMsgIdInQueue(int eventid) {
        if (eventid == 6) {
            removeMsgIdInQueue(1);
            removeMsgIdInQueue(2);
            removeMsgIdInQueue(3);
            removeMsgIdInQueue(4);
            removeMsgIdInQueue(5);
        } else if (eventid == 1) {
            removeMsgIdInQueue(3);
            removeMsgIdInQueue(5);
        } else if (eventid == 2) {
            removeMsgIdInQueue(3);
            removeMsgIdInQueue(5);
        } else if (eventid == 3) {
            removeMsgIdInQueue(1);
        } else if (eventid == 10) {
            removeMsgIdInQueue(1);
            removeMsgIdInQueue(2);
            removeMsgIdInQueue(3);
            removeMsgIdInQueue(4);
            removeMsgIdInQueue(5);
        }
        removeMsgIdInQueue(eventid);
    }

    private void removeMsgIdInQueue(int eventid) {
        synchronized (this.mListLock) {
            int q_size = this.mMsgQueueList.size();
            if (q_size <= 0) {
                return;
            }
            for (int i = q_size - 1; i >= 0; i--) {
                FaceKeyEventNode node = (FaceKeyEventNode) this.mMsgQueueList.get(i);
                if (node != null && node.mEventId == eventid) {
                    this.mMsgQueueList.remove(node);
                }
            }
        }
    }

    private void putMsgIdInQueue(FaceKeyEventNode node, int index) {
        if (node != null) {
            synchronized (this.mListLock) {
                if (index < 0) {
                    this.mMsgQueueList.add(node);
                } else {
                    this.mMsgQueueList.add(index, node);
                }
            }
        }
    }

    private void putMsgIdInQueue(int eventid, int errorcode, int extra, int index) {
        synchronized (this.mListLock) {
            if (index == -1) {
                this.mMsgQueueList.add(new FaceKeyEventNode(eventid, errorcode, extra));
            } else {
                this.mMsgQueueList.add(index, new FaceKeyEventNode(eventid, errorcode, extra));
            }
        }
    }

    private String msgQueueToString(int code) {
        switch (code) {
            case 0:
                return "ICON_FACEKEY_NONE";
            case 1:
                return "ICON_FACEKEY_SHOW_FACE_ICON";
            case 2:
                return "ICON_FACEKEY_SHOW_VERIFY_ANIM";
            case 3:
                return "ICON_FACEKEY_STOP_VERIFY_ANIM";
            case 4:
                return "ICON_FACEKEY_SHOW_RESULT";
            case 5:
                return "ICON_FACEKEY_STOP_RESULT_ANIM";
            case 6:
                return "ICON_FACEKEY_HIDE_FACE_ICON";
            case 7:
                return "ICON_FACEKEY_UPDATE_DISPLAY_STATUS";
            case 8:
                return "ICON_FACEKEY_UPDATE_BOOT_FINISHED";
            case 9:
                return "ICON_FACEKEY_POWER_ON";
            case 10:
                return "ICON_FACEKEY_KEYGUARD_EXIT";
            default:
                return code + "";
        }
    }

    private void saveFaceUnlockData() {
        if (FaceDebugConfig.DEBUG) {
            Slog.d(TAG, "save unlock method data fa: " + this.mKeyguardExitByFace + ", fp: " + this.mKeyguardExitByFinger + ", combine: " + this.mKeyguardExitByCombin);
        }
        int unlock_screenon = this.mFaceUnlockStartWhenScreenOn ? 1 : 0;
        int unlock_method = this.mFaceUnlockOtherWay ? 1 : 0;
        HashMap params = new HashMap();
        params.put("fpsw", this.mFingerprintEnabled ? "1" : "0");
        params.put("fu", this.mKeyguardExitByFace ? "1" : "0");
        String str = "fp";
        Object valueOf = (this.mKeyguardExitByCombin && this.mKeyguardExitByFinger) ? "1" : Integer.valueOf(0);
        params.put(str, valueOf);
        params.put("co", this.mKeyguardExitByCombin ? "1" : "0");
        params.put("FingerFaceCombine", this.mFingerFaceCombine ? "1" : "0");
        params.put("scr", Integer.toString(unlock_screenon));
        params.put("me", Integer.toString(unlock_method));
        params.put("version", PRODUCT_VERSION);
        this.mVivoCollectData.writeData(EVENT_ID, EVENT_LABEL_FACE_UNLOCK_METHOD, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
    }

    private boolean isTopActivity(String[] pkgNames) {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            String cmpNameTemp = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getPackageName().toString();
            String activityClassName = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getClassName().toString();
            Slog.d(TAG, "package name is:" + cmpNameTemp + "  class name is: " + activityClassName);
            for (String pkg : pkgNames) {
                if (pkg.equals(cmpNameTemp)) {
                    Slog.d(TAG, "top activity is true.");
                    return true;
                }
            }
            for (String pkg2 : PKGNAME_CLASSNAME) {
                if (pkg2.equals(cmpNameTemp) && isClassNameVideo(activityClassName)) {
                    Slog.d(TAG, "top class activity is true.");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isClassNameVideo(String activityClassName) {
        for (String className : CLASSNAME) {
            if (activityClassName.indexOf(className) != -1) {
                return true;
            }
        }
        return false;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SUPER_POWER_MODE);
        intentFilter.addAction(ACTION_UPSLIDE);
        this.mContext.registerReceiver(this.mFaceReceiver, intentFilter);
    }

    private void registerProcessObserver(boolean register) {
        if (this.mIActivityManager == null) {
            this.mIActivityManager = ActivityManagerNative.getDefault();
        }
        if (!(this.mIActivityManager == null || this.mRegisteredProcessObserver == register)) {
            if (register) {
                try {
                    this.mIActivityManager.registerProcessObserver(this.mProcessObserver);
                } catch (RemoteException e) {
                    Slog.e(TAG, "registerProcessObserver failed.");
                }
            } else {
                this.mIActivityManager.unregisterProcessObserver(this.mProcessObserver);
            }
            this.mRegisteredProcessObserver = register;
        }
    }

    private void startAnimation() {
        if (FaceCameraManager.IS_SUPPORT_FACEWINDOW) {
            this.mFaceClientApp.bindFaceWindowService();
            this.mFaceClientApp.sendMessageToFaceWindowService(1, 7, 540);
        }
    }

    private void releaseAnimation() {
        if (FaceCameraManager.IS_SUPPORT_FACEWINDOW) {
            this.mFaceClientApp.sendMessageToFaceWindowService(2, 0, 0);
        }
    }

    private boolean isFingerFaceCombine() {
        Slog.i(TAG, "finger&face: " + this.mFingerFaceCombine + ":" + this.mFingerprintEnabled + ":" + this.mIsFaceUnlockEnabled);
        return (this.mFingerFaceCombine && this.mFingerprintEnabled) ? this.mIsFaceUnlockEnabled : false;
    }

    /* JADX WARNING: Missing block: B:4:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyScreenState(boolean screenOn) {
        if (isFingerFaceCombine() && (this.mFingerMoveWakeEnabled ^ 1) == 0 && this.mFaceSensorManager != null) {
            this.mFaceSensorManager.notifyScreenState(screenOn);
        }
    }
}
