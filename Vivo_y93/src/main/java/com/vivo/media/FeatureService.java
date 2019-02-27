package com.vivo.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioFeatures.TagParameters;
import android.media.AudioSystem;
import android.media.IAudioFeatureCallback;
import android.media.PlayerBase.PlayerIdCard;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;
import com.vivo.media.AudioNativeBlackWhiteInfoServer.AudioNativeList;
import com.vivo.media.BveCallModuleInfoServer.FeatureList;
import com.vivo.media.FeatureManager.NotifyCallback;
import com.vivo.media.FeatureProject.projectFeature;
import com.vivo.media.MotorModeWhiteInfoServer.AppList;
import com.vivo.media.VAPIBlackWhiteInfoServer.AppBlackWhiteList;
import com.vivo.media.VAPIBlackWhiteInfoServer.VAPIList;
import com.vivo.media.VAPIBlackWhiteInfoServer.VAPITable;
import com.vivo.media.ZenModeBlackWhiteInfoServer.PathList;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureService {
    private static final String ACTION_KTV_STATE_CHANGED = "com.vivo.audio.ktv_state.changed";
    private static final String ACTION_KTV_SWITCH_CHANGED = "intent.action.karaoke_settings_off";
    public static final String AUDIONATIVE_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_AudioserverAudioNative";
    public static int AUDIO_FEATURE_BVE_DEFAULT_VALUE = 1;
    public static final String AUDIO_FEATURE_BVE_NAME = "bve";
    public static boolean AUDIO_FEATURE_BVM = false;
    public static final String AUDIO_FEATURE_BVM_NAME = "BVM";
    public static final String AUDIO_FEATURE_CT_PERIPHERAL_MICROPHONE = "ct_peripheral_microphone";
    public static final String AUDIO_FEATURE_DMIC_EXIST = "dualmic_exist";
    public static boolean AUDIO_FEATURE_DSDHW = false;
    public static int AUDIO_FEATURE_DSDHW_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_DSDHW_EXIST = "DSDHw_exist";
    public static final String AUDIO_FEATURE_DSDHW_NAME = "DSDHw";
    public static boolean AUDIO_FEATURE_DUALMIC = false;
    public static int AUDIO_FEATURE_DUALMIC_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_DUALMIC_NAME = "dualmic";
    public static boolean AUDIO_FEATURE_DUALSPKR = false;
    public static final String AUDIO_FEATURE_DUALSPKR_EXIST = "dualspeaker_exist";
    public static final String AUDIO_FEATURE_DUALSPKR_NAME = "dualspeaker";
    public static int AUDIO_FEATURE_DUALSPK_DEFAULT_VALUE = 0;
    public static boolean AUDIO_FEATURE_HANDSETSPKMODE = false;
    public static final String AUDIO_FEATURE_HANDSETSPKMODE_NAME = "HandsetSpkMode";
    public static boolean AUDIO_FEATURE_HIFI = false;
    public static int AUDIO_FEATURE_HIFI_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_HIFI_NAME = "hifi";
    public static boolean AUDIO_FEATURE_KTV = false;
    public static int AUDIO_FEATURE_KTV_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_KTV_EXIST = "ktv_exist";
    public static final String AUDIO_FEATURE_KTV_NAME = "ktv";
    public static boolean AUDIO_FEATURE_MAXA = false;
    public static int AUDIO_FEATURE_MAXA_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_MAXA_EXIST = "maxxaudio_exist";
    public static final String AUDIO_FEATURE_MAXA_NAME = "maxxaudio";
    public static boolean AUDIO_FEATURE_MCVS = false;
    public static int AUDIO_FEATURE_MCVS_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_MCVS_EXIST = "mcvs_exist";
    public static final String AUDIO_FEATURE_MCVS_NAME = "MCVS";
    public static boolean AUDIO_FEATURE_MICROPHONE = false;
    public static final String AUDIO_FEATURE_MODE_POM = "ModePOM";
    public static final String AUDIO_FEATURE_MODE_POM_EXIST = "ModePOM_exist";
    public static boolean AUDIO_FEATURE_POM = false;
    public static int AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE = 0;
    public static final String AUDIO_FEATURE_SAFE_VOLUME_INDEX_NAME = "SafeVolumeIndex";
    public static boolean AUDIO_FEATURE_SPBOOST = false;
    public static int AUDIO_FEATURE_SPBOOST_DEFAULT_VALUE = 0;
    public static final String AUDIO_FEATURE_SPBOOST_EXIST = "spkboost_exist";
    public static final String AUDIO_FEATURE_SPBOOST_NAME = "spkboost";
    private static final int AUDIO_FEATURE_STATUS_OK = 1;
    public static boolean AUDIO_FEATURE_VAPI = false;
    public static int AUDIO_FEATURE_VAPI_DEFAULT_VALUE = 1;
    public static boolean AUDIO_FEATURE_VIVO_INCALL = true;
    public static final String AUDIO_FEATURE_VIVO_INCALL_EXIST = "vivo_incall_exist";
    public static boolean AUDIO_FEATURE_ZENMOD = false;
    public static int AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE = 80;
    public static final String AUDIO_SET_DAILPAD_VOL_NAME = "DailpadVol";
    public static final String BVE_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_BVE_Call_Module";
    private static final String[] FACEDETECT_ALLOW_PACKGAGES = new String[]{"com.android.systemui", "com.android.BBKClock"};
    private static final String[] FACEDETECT_FORBID_PACKGAGES = new String[]{"com.android.settings"};
    public static int FLAG_SHOW_SPBOOST = Integer.MIN_VALUE;
    private static boolean IS_OVERSEA_VERSION = false;
    public static final String KEY_AUDIO_NATIVE_LIST = "audio_native_list";
    public static final String KEY_BVE = "BVE";
    public static final String KEY_COMPONENT = "component";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_HIFI = "hifi";
    public static final String KEY_LOOPING = "looping";
    public static final String KEY_MOTORMODE = "MOTORMODE";
    public static final String KEY_RETURN = "return";
    public static final String KEY_STATE = "state";
    public static final String KEY_STREAM = "stream";
    public static final String KEY_USAGE = "usage";
    public static final String KEY_VAPI = "VAPI";
    public static final String KEY_VIDEOBWLIST = "VIDEOBW";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_ZENMOD = "ZENMOD";
    private static final String[] KTV_SUPPORTED_PACKGAGES = new String[]{"com.changba", "com.tencent.karaoke", "com.app.hero.ui", "cn.banshenggua.aichang", "com.meelive.ingkee", "com.meitu.meipaimv", "tv.xiaoka.live", "com.huajiao", "com.ccvideo", "com.hunantv.mglive", "com.netease.cc", "com.duowan.mobile", "air.tv.douyu.android"};
    private static final String[] KTV_SUPPORTED_PACKGAGES_OVERSEAS = new String[]{"com.smule.singandroid.test"};
    public static final String MOTORMODE_WHITELIST_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_MotorModeWhiteServer";
    private static final int MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE = 124;
    private static final int MSG_CHECK_BVE_STATE = 115;
    private static final int MSG_CHECK_MOTORMODE_WHITELIST_SECURE = 121;
    private static final int MSG_CHECK_VAPI_STATE = 119;
    private static final int MSG_CHECK_VIDEO_BLACKWHITE_SECURE = 117;
    private static final int MSG_NOTIFY_MEDIASERVER_DIED = 106;
    private static final int MSG_NOTIFY_PLAY_CALLBACK = 105;
    private static final int MSG_SET_DSDHW_HEADSET_PLUG = 111;
    private static final int MSG_SET_DSDHW_MODE_SETTINGS = 110;
    private static final int MSG_SET_KTV_EXT_SP_MODE = 109;
    private static final int MSG_SET_KTV_MODE_SETTING = 107;
    private static final int MSG_SET_KTV_REC_NOTIFY = 108;
    private static final int MSG_SET_MCVS_MODE_SETTINGS = 123;
    private static final int MSG_SET_REC_NOTIFY = 126;
    private static final int MSG_SET_SPKBOOST_SETTINGS = 103;
    private static final int MSG_SET_SPK_MODE_SETTINGS = 101;
    private static final int MSG_SHOW_DSD_STATE = 112;
    private static final int MSG_SHOW_HIFI_STATE = 102;
    private static final int MSG_SHOW_SPBOOST_STATE = 104;
    private static final int MSG_UPDATE_AUDIONATIVE_BLACKWHITE_SECURE = 125;
    private static final int MSG_UPDATE_BVE_STATE = 116;
    private static final int MSG_UPDATE_MOTORMODE_WHITELIST_SECURE = 122;
    private static final int MSG_UPDATE_VAPI_STATE = 120;
    private static final int MSG_UPDATE_VIDEO_BLACKWHITE_SECURE = 118;
    private static final int MSG_WHITELIST_CHECK_ZENMODE_SECURE = 113;
    private static final int MSG_WHITELIST_UPDATE_ZENMODE_SECURE = 114;
    private static final String[] NEED_CUSTOMIZE_PACKGAGES = new String[]{"com.vivo.vivokaraoke", "com.audiocn.kalaok", "com.vivo.mediatune"};
    private static final String[] NEED_CUSTOMIZE_PACKGAGES_OVERSEAS = new String[]{"com.vivo.vivokaraoke", "com.vivo.mediatune", "com.tencent.wesing"};
    private static final int NUM_STREAM_TYPES = AudioSystem.getNumStreamTypes();
    private static final int PERSIST_DELAY = 500;
    private static final int PERSIST_DELAY_MIN = 100;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String TAG = "FeatureService";
    public static final String TAG_BVE_MODE = "BVE";
    public static final String TAG_DAILPAD_VOL = "DAILPAD_VOL";
    public static final String TAG_DMIC = "DMIC";
    public static final String TAG_DSDHW = "DSDHW";
    public static final String TAG_FACE_DETECT = "FACEDT";
    public static final String TAG_FACE_START = "start";
    public static final String TAG_FACE_STOP = "stop";
    public static final String TAG_HEANDSETSPK = "handetspk_support";
    public static final String TAG_HIFI = "HIFI";
    public static final String TAG_IMUSIC = "IMUS";
    private static final String TAG_KTV_EXT_SP = "vivo_ktv_ext_speaker";
    private static final String TAG_KTV_MIC = "KTV Mic";
    private static final String TAG_KTV_MODE = "vivo_ktv_mode";
    public static final String TAG_MCVS = "MCVS";
    private static final String TAG_MCVS_ENABLE = "vivo_multichnvs_enable";
    public static final String TAG_MICROPHONE = "MICROPHONE";
    public static final String TAG_SPBOOST = "SPBOOST";
    public static final String TAG_SPKR_MODE = "SPKR";
    public static final String TAG_VAPI = "VAPI";
    public static final String TAG_VIVOVIDEO = "IVIVOVIDEO";
    public static final String TAG_VIVO_INCALL = "VIVO_INCALL";
    private static final String TAG_ZEN_MODE = "vivo_zen_mode";
    public static final String VALUE_ERROR = "ERROR";
    public static final String VALUE_OK = "OK";
    public static final String VAPI_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_AudioserverVAPI";
    public static boolean VAPI_DEBUG = true;
    public static final String VIDEO_BWLIST_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_VideoBlackWhiteServer";
    public static final String ZENMODE_SECURE_BROCAST = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_AudioserverZenMode";
    private static final Object mKTVModeLock = new Object();
    public static int mZenMode = 0;
    private final String ACTION_HIFI_STATE_CHANGED = "com.vivo.action.HIFI_STATE_CHANGED";
    public final Uri DRIVE_MODE = System.getUriFor("drive_mode_enabled");
    private final String DSD_DISPLAY_INTENT = "com.vivo.media.dsd.display";
    private final String ENABLE_BVE_MODE_COMMAND = "bve_mode";
    private final String ENABLE_DULA_MIC_COMMAND = "dmic_enable";
    private final String ENABLE_MIC_PHONE_COMMAND = "mic_phone_enable";
    private final String ENABLE_SPK_MODE_COMMAND = "speaker_mode";
    public final Uri GAME_4D_SHOCK_ENABLED = System.getUriFor("game_4d_shock_enabled");
    public final Uri GAME_DO_NOT_DISTURB = System.getUriFor("game_do_not_disturb");
    private final String HIFI_DISPLAY_INTENT = "com.bbk.audiofx.hifi.display";
    public final Uri MOTOR_MODE = System.getUriFor("motor_mode_enabled");
    private final String NOTIFY_CALL_INTENT = "com.vivo.audiofx.maxxaudio.effect";
    public final Uri SHIELD_NOTIFICATION = System.getUriFor("shield_notification_reminder_enabled");
    private VivoVideoBlackWhiteInfoServer VideoBlackWhiteList;
    private ZenModeBlackWhiteInfoServer blackWhiteList;
    private byte[] bytesInt;
    private final String keySPBoost = "spboost_mode";
    public ArrayList<PathList> mAppBlackPathList;
    final HashMap<String, PlayerIdCard> mAudioFaceDetectVolumeControl = new HashMap();
    final HashMap<String, RemoteCallbackList<IAudioFeatureCallback>> mAudioFeatureCallbacks = new HashMap();
    private ArrayList<AudioNativeList> mAudioNativeList;
    private AudioNativeBlackWhiteInfoServer mAudioNativeserver;
    final HashMap<String, Playback> mAudioPlaybackList = new HashMap();
    private BveCallModuleInfoServer mBveCallModule;
    public ArrayList<FeatureList> mBveCallModuleFeatureList;
    private final ArrayMap<Integer, String> mClientMap = new ArrayMap();
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final int[] mDefaultStreamVolume = new int[NUM_STREAM_TYPES];
    private boolean mEnableBveMode = true;
    private boolean mEnableDSDHwMode = false;
    private boolean mEnableDualMic = false;
    private boolean mEnableFeatureSPBoost;
    private int mEnableFeatureSPBoostStreams;
    private boolean mEnableMCVSMode = false;
    private boolean mEnableMicPhone = true;
    private boolean mEnableSpeakerMode = false;
    private final FeatureEventHandler mEventHandler;
    private final NotifyCallback mFeatureNotifyCallback = new NotifyCallback() {
        public void onNotifyCallback(int msg0, int msg1, int[] msg2) {
            switch (msg0) {
                case 200:
                    Log.d(FeatureService.TAG, "FEATURE_NOTIFY_HIFI_CALLBACK msg1: " + msg1);
                    if (msg1 == 1) {
                        FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SHOW_HIFI_STATE, 0, msg1, msg2[0], null, 100);
                        break;
                    }
                    Log.d(FeatureService.TAG, "NotifyCallback msg error return ");
                    return;
                case FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK /*201*/:
                    Log.d(FeatureService.TAG, "FEATURE_NOTIFY_PLAY_CALLBACK, msg1: " + msg1);
                    if (msg1 == 3) {
                        FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_NOTIFY_PLAY_CALLBACK, 2, 0, 0, new AudioEffectCallback(msg2[0], msg2[1], msg2[2]), 0);
                        break;
                    }
                    Log.d(FeatureService.TAG, "msg1 param eror : " + msg1);
                    return;
                case FeatureManager.FEATURE_NOTIFY_MEDIASERVER_DIED /*202*/:
                    Log.d(FeatureService.TAG, "FEATURE_NOTIFY_MEDIASERVER_DIED ");
                    FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SHOW_HIFI_STATE, 0, 0, 0, null, 100);
                    FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SHOW_DSD_STATE, 0, 0, 0, null, 100);
                    FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_NOTIFY_MEDIASERVER_DIED, 1, 0, 0, Integer.valueOf(0), 100);
                    break;
                case FeatureManager.FEATURE_NOTIFY_KTV_REC_CALLBACK /*203*/:
                    Log.d(FeatureService.TAG, "FEATURE_NOTIFY_KTV_REC_CALLBACK msg1: " + msg1);
                    if (msg1 == 3) {
                        KTVRecordInfo ktvrecinfo = new KTVRecordInfo(msg2[0], msg2[1], msg2[2]);
                        FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SET_KTV_REC_NOTIFY, 0, 0, 0, ktvrecinfo, 0);
                        if (msg2[2] != 2) {
                            FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SET_KTV_REC_NOTIFY, 0, 0, 0, ktvrecinfo, 0);
                            break;
                        } else {
                            FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SET_REC_NOTIFY, 2, 0, 0, ktvrecinfo, 0);
                            break;
                        }
                    }
                    Log.d(FeatureService.TAG, "NotifyCallback msg error return ");
                    return;
                case FeatureManager.FEATURE_NOTIFY_DSD_CALLBACK /*204*/:
                    Log.d(FeatureService.TAG, "FEATURE_NOTIFY_DSD_CALLBACK msg1: " + msg1);
                    if (msg1 == 1) {
                        if (msg2[0] == 1) {
                            FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SHOW_HIFI_STATE, 0, 0, 0, null, 0);
                        }
                        FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SHOW_DSD_STATE, 0, msg1, msg2[0], null, 100);
                        break;
                    }
                    Log.d(FeatureService.TAG, "NotifyCallback msg error return ");
                    return;
            }
        }
    };
    private int mGetSecureConfigTimesBve = 0;
    private int mGetSecureConfigTimesMotorMode = 0;
    private int mGetSecureConfigTimesNativeList = 0;
    private int mGetSecureConfigTimesVAPI = 0;
    private int mGetSecureConfigTimesVBW = 0;
    private int mGetSecureConfigTimesZen = 0;
    private int mIsKTVMode = 0;
    private boolean mIsKTVRecording = false;
    private IBinder mKTVClientCb;
    private int mKTVExtSpMode = 0;
    private KTVModeDeathHandler mKTVModeDeathHandler;
    private String mKTVRecordingPkgName = "";
    private boolean mLowPower = false;
    private final int[] mMaxStreamVolume = new int[NUM_STREAM_TYPES];
    private MotorModeWhiteInfoServer mMotorMode;
    private boolean mMotorModeEnabled = false;
    private ArrayList<AppList> mMotorModeWhiteList;
    private boolean mNotInterruptDuringDrive = false;
    private ArrayList<AudioEffectCallback> mNotifyCallbackArray = new ArrayList();
    private final BroadcastReceiver mReceiver = new FeatureServiceBroadcastReceiver(this, null);
    private int mRingerMode = 2;
    private boolean[] mSPBShowState;
    private SettingsObserver mSettingsObserver;
    private final int[] mStreamVolumeAlias = new int[NUM_STREAM_TYPES];
    private StreamVolumeInfo[] mStreamVolumeInfo;
    private boolean mStreamVolumeInit = false;
    private boolean mSystemReadyCond = false;
    private Toast mToast;
    private ArrayList<VAPIList> mVAPILists;
    public VAPITable mVAPITable;
    private VAPIBlackWhiteInfoServer mVAPIserver;

    public class AudioEffectCallback {
        public int mAction;
        public int mCount = 0;
        public int mPid;
        public int mSessionid;

        public AudioEffectCallback(int action, int pid, int sessionid) {
            this.mPid = pid;
            this.mSessionid = sessionid;
            this.mAction = action;
        }
    }

    private class FeatureEventHandler extends Handler {
        FeatureEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FeatureService.MSG_SET_SPK_MODE_SETTINGS /*101*/:
                    FeatureService.this.onSetSpkrModeSettings(msg.arg1);
                    return;
                case FeatureService.MSG_SHOW_HIFI_STATE /*102*/:
                    FeatureService.this.onShowHifiStateChanged(msg.arg2);
                    return;
                case FeatureService.MSG_SET_SPKBOOST_SETTINGS /*103*/:
                    FeatureService.this.onSetSpkrBoostSettings(msg.arg1);
                    return;
                case FeatureService.MSG_SHOW_SPBOOST_STATE /*104*/:
                    FeatureService.this.onShowSPBoostState(msg.arg1, msg.arg2);
                    return;
                case FeatureService.MSG_NOTIFY_PLAY_CALLBACK /*105*/:
                    FeatureService.this.onAudioEffectCallback((AudioEffectCallback) msg.obj);
                    return;
                case FeatureService.MSG_NOTIFY_MEDIASERVER_DIED /*106*/:
                    if (FeatureService.this.onMediaServerDied() != 1) {
                        FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_NOTIFY_MEDIASERVER_DIED, 1, 0, 0, Integer.valueOf(0), 500);
                        return;
                    }
                    return;
                case FeatureService.MSG_SET_KTV_MODE_SETTING /*107*/:
                    FeatureService.this.onKTVModeSettings(msg.arg1);
                    return;
                case FeatureService.MSG_SET_KTV_REC_NOTIFY /*108*/:
                    FeatureService.this.notifyKTVRecordState((KTVRecordInfo) msg.obj);
                    return;
                case FeatureService.MSG_SET_KTV_EXT_SP_MODE /*109*/:
                    FeatureService.this.onKTVExtSPModeUse(msg.arg1);
                    return;
                case FeatureService.MSG_SET_DSDHW_MODE_SETTINGS /*110*/:
                    FeatureService.this.onSetDSDHwModeSettings(msg.arg1);
                    return;
                case FeatureService.MSG_SET_DSDHW_HEADSET_PLUG /*111*/:
                    FeatureService.this.setDSDHwHeadsetPlug(msg.arg1);
                    return;
                case FeatureService.MSG_SHOW_DSD_STATE /*112*/:
                    FeatureService.this.onSendShowDSDIntent(msg.arg2);
                    return;
                case FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE /*113*/:
                    FeatureService.this.getSecureConfigXml(FeatureService.KEY_ZENMOD);
                    return;
                case FeatureService.MSG_WHITELIST_UPDATE_ZENMODE_SECURE /*114*/:
                    FeatureService.this.updateSecureConfigXml(FeatureService.KEY_ZENMOD);
                    return;
                case FeatureService.MSG_CHECK_BVE_STATE /*115*/:
                    FeatureService.this.getSecureConfigXml("BVE");
                    return;
                case FeatureService.MSG_UPDATE_BVE_STATE /*116*/:
                    FeatureService.this.updateSecureConfigXml("BVE");
                    return;
                case FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE /*117*/:
                    FeatureService.this.getSecureConfigXml(FeatureService.KEY_VIDEOBWLIST);
                    return;
                case FeatureService.MSG_UPDATE_VIDEO_BLACKWHITE_SECURE /*118*/:
                    Log.d(FeatureService.TAG, "receive MSG_UPDATE_VIDEO_BLACKWHITE_SECURE");
                    FeatureService.this.updateSecureConfigXml(FeatureService.KEY_VIDEOBWLIST);
                    return;
                case FeatureService.MSG_CHECK_VAPI_STATE /*119*/:
                    FeatureService.this.getSecureConfigXml("VAPI");
                    return;
                case FeatureService.MSG_UPDATE_VAPI_STATE /*120*/:
                    FeatureService.this.updateSecureConfigXml("VAPI");
                    return;
                case FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE /*121*/:
                    FeatureService.this.getSecureConfigXml(FeatureService.KEY_MOTORMODE);
                    return;
                case FeatureService.MSG_UPDATE_MOTORMODE_WHITELIST_SECURE /*122*/:
                    FeatureService.this.updateSecureConfigXml(FeatureService.KEY_MOTORMODE);
                    return;
                case FeatureService.MSG_SET_MCVS_MODE_SETTINGS /*123*/:
                    FeatureService.this.onSetMCVSModeSettings(msg.arg1);
                    return;
                case FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE /*124*/:
                    FeatureService.this.getSecureConfigXml(FeatureService.KEY_AUDIO_NATIVE_LIST);
                    return;
                case FeatureService.MSG_UPDATE_AUDIONATIVE_BLACKWHITE_SECURE /*125*/:
                    FeatureService.this.updateSecureConfigXml(FeatureService.KEY_AUDIO_NATIVE_LIST);
                    return;
                case FeatureService.MSG_SET_REC_NOTIFY /*126*/:
                    Log.d(FeatureService.TAG, "start record.....");
                    FeatureService.this.JudgeSettingAndShowMessage();
                    return;
                default:
                    return;
            }
        }
    }

    private class FeatureServiceBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ FeatureServiceBroadcastReceiver(FeatureService this$0, FeatureServiceBroadcastReceiver -this1) {
            this();
        }

        private FeatureServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(FeatureService.TAG, "onReceive action " + action);
            int state;
            if (action.equals("com.vivo.action.HIFI_STATE_CHANGED")) {
                FeatureManager.setAudioFeatures("hifi_config_changed=1");
            } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                boolean low_power = !(intent.getIntExtra("plugged", 0) > 0) && intent.getIntExtra("level", 100) <= 10;
                if (low_power && (FeatureService.this.mLowPower ^ 1) != 0) {
                    FeatureManager.setAudioFeatures("low_power=1");
                } else if (!low_power && FeatureService.this.mLowPower) {
                    FeatureManager.setAudioFeatures("low_power=0");
                }
                FeatureService.this.mLowPower = low_power;
            } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                state = intent.getIntExtra(FeatureService.KEY_STATE, 0);
                if (FeatureService.AUDIO_FEATURE_KTV && FeatureService.this.mIsKTVRecording) {
                    if (state == 1) {
                        FeatureService.this.updateKTVModeState(FeatureService.this.mKTVRecordingPkgName);
                    } else {
                        FeatureService.this.closeKTVMode(true);
                    }
                }
                if (FeatureService.AUDIO_FEATURE_DSDHW) {
                    FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_SET_DSDHW_HEADSET_PLUG, 0, state, 0, null, 100);
                }
            } else if (action.equals(FeatureService.ACTION_KTV_SWITCH_CHANGED)) {
                if (FeatureService.AUDIO_FEATURE_KTV) {
                    state = intent.getIntExtra(FeatureService.KEY_STATE, 0);
                    String packageName = intent.getStringExtra("pkgname");
                    if (packageName.equals(FeatureService.this.mKTVRecordingPkgName)) {
                        Log.v(FeatureService.TAG, "onReceive karaoke_settings: state=" + state + " pkgname=" + packageName);
                        FeatureService.this.updateKTVModeState(packageName);
                    }
                }
            } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                FeatureService.this.mSystemReadyCond = true;
            }
        }
    }

    protected class KTVModeDeathHandler implements DeathRecipient {
        private IBinder mCb;

        KTVModeDeathHandler(IBinder cb) {
            if (cb != null) {
                Log.v(FeatureService.TAG, "KTVModeDeathHandler: link binder death");
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Log.w(FeatureService.TAG, "KTVModeDeathHandler could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (FeatureService.mKTVModeLock) {
                if (FeatureService.this.mKTVModeDeathHandler != this) {
                    Log.w(FeatureService.TAG, "unregistered ktv client died");
                } else {
                    FeatureService.this.mIsKTVRecording = false;
                    if (FeatureService.this.mIsKTVMode == 1) {
                        Log.d(FeatureService.TAG, "KTV client died! recover ktv mode");
                        FeatureService.this.mKTVModeDeathHandler = null;
                        FeatureService.this.closeKTVMode(true);
                    }
                }
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public void release() {
            if (this.mCb != null) {
                Log.v(FeatureService.TAG, "KTVModeDeathHandler: unlink binder death");
                try {
                    this.mCb.unlinkToDeath(this, 0);
                } catch (NoSuchElementException e) {
                    Log.e(FeatureService.TAG, "KTVModeDeathHandler error unlinking to death", e);
                }
                this.mCb = null;
            }
        }
    }

    public class KTVRecordInfo {
        public int mPid;
        public int mSessionid;
        public int mState;

        public KTVRecordInfo(int pid, int sessionid, int state) {
            this.mPid = pid;
            this.mSessionid = sessionid;
            this.mState = state;
        }
    }

    class Playback {
        private String mPackage;
        private PlayerIdCard mPic;
        private float mVolume;

        public Playback(String pkg, PlayerIdCard pic, float volume) {
            this.mPackage = pkg;
            this.mPic = pic;
            this.mVolume = volume;
        }

        public String getPackageName() {
            return this.mPackage;
        }

        public PlayerIdCard getPlayerIdCard() {
            return this.mPic;
        }

        public float getVolume() {
            return this.mVolume;
        }
    }

    class SecureBroadcastReceiver extends BroadcastReceiver {
        SecureBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(FeatureService.TAG, "unifiedconfig update identifiers: " + Arrays.toString((String[]) intent.getExtras().get("identifiers")));
            String action = intent.getAction();
            if (action.equals(FeatureService.ZENMODE_SECURE_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE, 200);
            } else if (action.equals(FeatureService.BVE_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_BVE_STATE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_BVE_STATE, 500);
            } else if (action.equals(FeatureService.VIDEO_BWLIST_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE, 500);
            } else if (action.equals(FeatureService.VAPI_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VAPI_STATE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_VAPI_STATE, 500);
            } else if (action.equals(FeatureService.MOTORMODE_WHITELIST_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE, 500);
            } else if (action.equals(FeatureService.AUDIONATIVE_BROCAST)) {
                FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE);
                FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE, 500);
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d(FeatureService.TAG, "SettingsObserver onChange:" + selfChange + " uri: " + uri);
            if (FeatureService.this.DRIVE_MODE.equals(uri) || FeatureService.this.SHIELD_NOTIFICATION.equals(uri)) {
                FeatureService.this.mNotInterruptDuringDrive = FeatureService.this.computeDriveMode();
                Log.d(FeatureService.TAG, "SettingsObserver onChange:" + selfChange + " mNotInterruptDuringDrive: " + FeatureService.this.mNotInterruptDuringDrive);
            } else if (FeatureService.this.GAME_4D_SHOCK_ENABLED.equals(uri) || FeatureService.this.GAME_DO_NOT_DISTURB.equals(uri)) {
                int game_4d_shock_enabled = System.getInt(FeatureService.this.mContext.getContentResolver(), "game_4d_shock_enabled", 0);
                int game_do_not_disturb = System.getInt(FeatureService.this.mContext.getContentResolver(), "game_do_not_disturb", 0);
                if (game_4d_shock_enabled <= 0 || game_do_not_disturb <= 0) {
                    FeatureManager.setAudioFeatures("game_vibrate=off");
                } else {
                    FeatureManager.setAudioFeatures("game_vibrate=on");
                }
            } else if (FeatureService.this.MOTOR_MODE.equals(uri)) {
                FeatureService.this.mMotorModeEnabled = System.getInt(FeatureService.this.mContext.getContentResolver(), "motor_mode_enabled", 0) == 1;
                HashMap<String, Playback> needChangePlayback = new HashMap();
                synchronized (FeatureService.this.mAudioPlaybackList) {
                    for (String key : FeatureService.this.mAudioPlaybackList.keySet()) {
                        boolean found = false;
                        for (int i = 0; i < FeatureService.this.mMotorModeWhiteList.size(); i++) {
                            if (((AppList) FeatureService.this.mMotorModeWhiteList.get(i)).GetAppName().equals(((Playback) FeatureService.this.mAudioPlaybackList.get(key)).getPackageName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Log.d(FeatureService.TAG, "evan add key is " + ((Playback) FeatureService.this.mAudioPlaybackList.get(key)).getPackageName() + "motomodeenabled ? " + FeatureService.this.mMotorModeEnabled);
                            needChangePlayback.put(key, (Playback) FeatureService.this.mAudioPlaybackList.get(key));
                        }
                    }
                }
                for (String key2 : needChangePlayback.keySet()) {
                    try {
                        ((Playback) needChangePlayback.get(key2)).getPlayerIdCard().mIPlayer.setVolume(FeatureService.this.mMotorModeEnabled ? 0.0f : ((Playback) needChangePlayback.get(key2)).getVolume());
                    } catch (Exception e) {
                        e.printStackTrace();
                        synchronized (FeatureService.this.mAudioPlaybackList) {
                            FeatureService.this.mAudioPlaybackList.remove(key2);
                        }
                    }
                }
            } else {
                FeatureService.mZenMode = Global.getInt(FeatureService.this.mContext.getContentResolver(), "zen_mode", 0);
                Log.d(FeatureService.TAG, "SettingsObserver onChange:" + selfChange + " mZenMode: " + FeatureService.mZenMode);
                FeatureService.this.mGetSecureConfigTimesZen = 0;
                FeatureService.sendMsg(FeatureService.this.mEventHandler, FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE, 0, 0, 0, null, 100);
                if (FeatureService.mZenMode != 0) {
                    FeatureService.AUDIO_FEATURE_ZENMOD = true;
                } else {
                    FeatureService.AUDIO_FEATURE_ZENMOD = false;
                }
            }
        }
    }

    class StreamVolumeInfo {
        private ConcurrentHashMap<Integer, Integer> mIndex = new ConcurrentHashMap(8, 0.75f, 4);
        public int mMaxIndex;
        private int mMuteCount;
        public int mStreamType;
        private String mVolumeIndexName;

        StreamVolumeInfo(String settingName, int streamType) {
            this.mVolumeIndexName = settingName;
            this.mStreamType = streamType;
            this.mMaxIndex = FeatureService.this.mMaxStreamVolume[streamType];
            this.mMaxIndex *= 10;
            readSettings();
            this.mMuteCount = 0;
        }

        public String getNameForDevice(int device) {
            String name = this.mVolumeIndexName;
            String suffix = AudioSystem.getOutputDeviceName(device);
            if (suffix.isEmpty()) {
                return name;
            }
            return name + "_" + suffix;
        }

        public void readSettings() {
            synchronized (StreamVolumeInfo.class) {
                if (this.mStreamType == 1 || this.mStreamType == 7) {
                    this.mIndex.put(Integer.valueOf(1073741824), Integer.valueOf(FeatureService.this.mDefaultStreamVolume[this.mStreamType] * 10));
                    return;
                }
                int remainingDevices = 1207959551;
                int i = 0;
                while (remainingDevices != 0) {
                    int device = 1 << i;
                    if ((device & remainingDevices) != 0) {
                        remainingDevices &= ~device;
                        int index = System.getIntForUser(FeatureService.this.mContentResolver, getNameForDevice(device), device == 1073741824 ? FeatureService.this.mDefaultStreamVolume[this.mStreamType] : -1, -2);
                        if (index != -1) {
                            this.mIndex.put(Integer.valueOf(device), Integer.valueOf(getValidIndex(index * 10)));
                        }
                    }
                    i++;
                }
            }
        }

        public boolean setIndex(int index, int device) {
            synchronized (StreamVolumeInfo.class) {
                int oldIndex = getIndex(device);
                index = getValidIndex(index);
                this.mIndex.put(Integer.valueOf(device), Integer.valueOf(index));
                if (oldIndex != index) {
                    boolean currentDevice = device == FeatureService.this.getDeviceForStream(this.mStreamType);
                    int streamType = FeatureService.NUM_STREAM_TYPES - 1;
                    while (streamType >= 0) {
                        if (streamType != this.mStreamType && FeatureService.this.mStreamVolumeAlias[streamType] == this.mStreamType) {
                            int scaledIndex = rescaleIndex(index, this.mStreamType, streamType);
                            FeatureService.this.mStreamVolumeInfo[streamType].setIndex(scaledIndex, device);
                            if (currentDevice) {
                                FeatureService.this.mStreamVolumeInfo[streamType].setIndex(scaledIndex, FeatureService.this.getDeviceForStream(streamType));
                            }
                        }
                        streamType--;
                    }
                    return true;
                }
                return false;
            }
        }

        public int getIndex(int device) {
            int intValue;
            synchronized (StreamVolumeInfo.class) {
                Integer index = (Integer) this.mIndex.get(Integer.valueOf(device));
                if (index == null) {
                    index = (Integer) this.mIndex.get(Integer.valueOf(1073741824));
                }
                intValue = index.intValue();
            }
            return intValue;
        }

        public int getMaxIndex() {
            return this.mMaxIndex;
        }

        public int getStreamType() {
            return this.mStreamType;
        }

        public int getValidIndex(int index) {
            if (index < 0) {
                return 0;
            }
            if (index > this.mMaxIndex) {
                return this.mMaxIndex;
            }
            return index;
        }

        public int rescaleIndex(int index, int srcStream, int dstStream) {
            return ((FeatureService.this.mStreamVolumeInfo[dstStream].getMaxIndex() * index) + (FeatureService.this.mStreamVolumeInfo[srcStream].getMaxIndex() / 2)) / FeatureService.this.mStreamVolumeInfo[srcStream].getMaxIndex();
        }

        public void mute(boolean state) {
            synchronized (StreamVolumeInfo.class) {
                if (state) {
                    if (this.mMuteCount != 0) {
                        Log.w(FeatureService.TAG, "stream: " + this.mStreamType + " was already muted");
                    }
                    this.mMuteCount++;
                } else if (this.mMuteCount == 0) {
                    Log.w(FeatureService.TAG, "unexpected unmute for stream: " + this.mStreamType);
                } else {
                    this.mMuteCount--;
                }
            }
        }

        public boolean isMuted() {
            boolean z = false;
            synchronized (StreamVolumeInfo.class) {
                if (this.mMuteCount != 0) {
                    z = true;
                }
            }
            return z;
        }

        public int isSPBoostOn(int device) {
            int i = 1;
            if (!FeatureService.this.mEnableFeatureSPBoost || (FeatureService.this.mEnableFeatureSPBoostStreams & (1 << this.mStreamType)) == 0 || device != 2 || isMuted()) {
                return 0;
            }
            if (getIndex(device) != this.mMaxIndex) {
                i = 0;
            }
            return i;
        }
    }

    public FeatureService(Looper looper, Context cntxt) {
        Log.d(TAG, "FeatureService construtor ++ ");
        this.mEventHandler = new FeatureEventHandler(looper);
        this.mContext = cntxt;
        this.mContentResolver = this.mContext.getContentResolver();
        FeatureManager.native_init();
        FeatureManager.setNotifyCallback(this.mFeatureNotifyCallback);
        readFeatureConfig();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("com.vivo.action.HIFI_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction(ACTION_KTV_SWITCH_CHANGED);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        if (AUDIO_FEATURE_DUALSPKR) {
            this.mEnableSpeakerMode = System.getInt(this.mContentResolver, "vivo_feature_audio_spkrmode", AUDIO_FEATURE_DUALSPK_DEFAULT_VALUE) != 0;
            setSpeakerModeSettings(this.mEnableSpeakerMode);
        }
        if (AUDIO_FEATURE_SPBOOST) {
            this.mEnableFeatureSPBoost = (System.getInt(this.mContentResolver, "vivo_features_for_spboost", AUDIO_FEATURE_SPBOOST_DEFAULT_VALUE) & 1) != 0;
            this.mEnableFeatureSPBoostStreams = System.getInt(this.mContentResolver, "vivo_spboost_streams", 12);
        }
        if (AUDIO_FEATURE_DSDHW) {
            this.mEnableDSDHwMode = System.getInt(this.mContentResolver, "vivo_feature_dsd_hw", AUDIO_FEATURE_DSDHW_DEFAULT_VALUE) != 0;
            if (this.mEnableDSDHwMode) {
                setDSDHwSwitch(500, 1);
            } else {
                setDSDHwSwitch(500, 0);
            }
        }
        if (AUDIO_FEATURE_MCVS) {
            setMCVSModeSettings(System.getInt(this.mContentResolver, "vivo_feature_multichnvs", AUDIO_FEATURE_MCVS_DEFAULT_VALUE) == 1);
        }
        this.mSettingsObserver = new SettingsObserver(this.mEventHandler);
        this.mContentResolver.registerContentObserver(Global.getUriFor("zen_mode"), false, this.mSettingsObserver);
        mZenMode = Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        Log.d(TAG, "construtor SettingsObserver mZenMode: " + mZenMode);
        if (mZenMode != 0) {
            AUDIO_FEATURE_ZENMOD = true;
        } else {
            AUDIO_FEATURE_ZENMOD = false;
        }
        this.mContentResolver.registerContentObserver(this.DRIVE_MODE, false, this.mSettingsObserver);
        this.mContentResolver.registerContentObserver(this.MOTOR_MODE, false, this.mSettingsObserver);
        this.mContentResolver.registerContentObserver(this.SHIELD_NOTIFICATION, false, this.mSettingsObserver);
        this.mNotInterruptDuringDrive = computeDriveMode();
        this.mMotorModeEnabled = System.getInt(this.mContext.getContentResolver(), "motor_mode_enabled", 0) == 1;
        Log.d(TAG, "construtor SettingsObserver mNotInterruptDuringDrive: " + this.mNotInterruptDuringDrive + "motormode is " + this.mMotorModeEnabled);
        this.mContentResolver.registerContentObserver(this.GAME_4D_SHOCK_ENABLED, false, this.mSettingsObserver);
        this.mContentResolver.registerContentObserver(this.GAME_DO_NOT_DISTURB, false, this.mSettingsObserver);
        this.blackWhiteList = new ZenModeBlackWhiteInfoServer(this.mContext);
        this.blackWhiteList.checkUpdateBlackWhiteList();
        loadZenmodeBlackWhiteListAndUpdate();
        this.mBveCallModule = new BveCallModuleInfoServer(this.mContext);
        this.mBveCallModule.checkUpdateBveCallModuleList();
        updateCurrentBveCallModuleListAndSet();
        this.VideoBlackWhiteList = new VivoVideoBlackWhiteInfoServer(this.mContext);
        this.VideoBlackWhiteList.checkUpdateBlackWhiteList();
        this.mVAPIserver = new VAPIBlackWhiteInfoServer(this.mContext);
        this.mVAPIserver.checkUpdateVAPITable();
        updateCurrentVAPITableAndSet();
        this.mMotorMode = new MotorModeWhiteInfoServer(this.mContext);
        this.mMotorMode.checkUpdateWhiteList();
        updateCurrentMotorModeListAndSet();
        this.mAudioNativeserver = new AudioNativeBlackWhiteInfoServer(this.mContext);
        this.mAudioNativeserver.checkUpdateAudioNativeTable();
        sendAudioNativelistUpdate(FeatureManager.FEATURE_TO_PLAYER_AUDIO_NATIVE_LIST_UPDATE, 1);
        this.mAudioNativeserver.loadCurrentAudioNativeTable();
        Log.d(TAG, "use SecureMethod ");
        SecureBroadcastReceiver mSecureBroadcastReceiver = new SecureBroadcastReceiver();
        IntentFilter receieveDateBaseUpadteFilter = new IntentFilter();
        receieveDateBaseUpadteFilter.addAction(ZENMODE_SECURE_BROCAST);
        receieveDateBaseUpadteFilter.addAction(BVE_BROCAST);
        receieveDateBaseUpadteFilter.addAction(VIDEO_BWLIST_BROCAST);
        receieveDateBaseUpadteFilter.addAction(MOTORMODE_WHITELIST_BROCAST);
        receieveDateBaseUpadteFilter.addAction(VAPI_BROCAST);
        receieveDateBaseUpadteFilter.addAction(AUDIONATIVE_BROCAST);
        this.mContext.registerReceiver(mSecureBroadcastReceiver, receieveDateBaseUpadteFilter);
        int game_4d_shock_enabled = System.getInt(this.mContext.getContentResolver(), "game_4d_shock_enabled", 0);
        int game_do_not_disturb = System.getInt(this.mContext.getContentResolver(), "game_do_not_disturb", 0);
        if (game_4d_shock_enabled <= 0 || game_do_not_disturb <= 0) {
            FeatureManager.setAudioFeatures("game_vibrate=off");
        } else {
            FeatureManager.setAudioFeatures("game_vibrate=on");
        }
        this.mEnableMicPhone = Secure.getIntForUser(this.mContentResolver, AUDIO_FEATURE_CT_PERIPHERAL_MICROPHONE, 1, getCurrentUserId()) == 1;
        setMicrPhoneSettings(this.mEnableMicPhone);
        Log.d(TAG, "FeatureService construtor -- ");
    }

    private int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManager.getService().getCurrentUser().id;
            return i;
        } catch (RemoteException e) {
            return 0;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean updateCurrentVAPITableAndSet() {
        this.mVAPIserver.loadCurrentVAPITable();
        VAPITable tmp = this.mVAPIserver.getVAPITables();
        if (tmp != null) {
            this.mVAPILists = tmp.getVAPILists();
        }
        this.mEventHandler.removeMessages(MSG_CHECK_VAPI_STATE);
        this.mEventHandler.sendEmptyMessageDelayed(MSG_CHECK_VAPI_STATE, 600);
        if (this.mVAPILists == null || this.mVAPILists.size() <= 0) {
            return false;
        }
        Log.d(TAG, "updateCurrentVAPITableAndSet success!!");
        return true;
    }

    private boolean updateCurrentBveCallModuleListAndSet() {
        this.mBveCallModule.loadCurrentBveCallModuleList();
        this.mBveCallModuleFeatureList = this.mBveCallModule.getBveCallModuleList();
        this.mEventHandler.removeMessages(MSG_CHECK_BVE_STATE);
        this.mEventHandler.sendEmptyMessageDelayed(MSG_CHECK_BVE_STATE, 600);
        if (this.mBveCallModuleFeatureList == null || this.mBveCallModuleFeatureList.size() <= 0) {
            return false;
        }
        Log.d(TAG, "updateCurrentBveCallModuleListAndSet success!!");
        setBveCallModule();
        return true;
    }

    private boolean updateCurrentMotorModeListAndSet() {
        this.mMotorMode.loadCurrentMotorModeList();
        this.mMotorModeWhiteList = this.mMotorMode.getMotorModeList();
        if (this.mMotorModeWhiteList == null || this.mMotorModeWhiteList.size() <= 0) {
            return false;
        }
        Log.d(TAG, "updateCurrentMotorModeListAndSet success!!");
        return true;
    }

    private boolean updateCurrentAudioServerListAndSet() {
        this.mAudioNativeserver.loadCurrentAudioNativeTable();
        this.mAudioNativeList = this.mAudioNativeserver.getAudioNativeTable();
        if (this.mAudioNativeList == null || this.mAudioNativeList.size() <= 0) {
            return false;
        }
        Log.d(TAG, "updateCurrentAudioServerListAndSet success!!");
        return true;
    }

    private void setBveCallModule() {
        for (int i = 0; i < this.mBveCallModuleFeatureList.size(); i++) {
            if (AUDIO_FEATURE_BVE_NAME.equals(((FeatureList) this.mBveCallModuleFeatureList.get(i)).getFeatureName())) {
                AUDIO_FEATURE_BVE_DEFAULT_VALUE = ((FeatureList) this.mBveCallModuleFeatureList.get(i)).getFeatureValue();
                Log.d(TAG, "setBveCallModule bve config,  value:" + AUDIO_FEATURE_BVE_DEFAULT_VALUE);
                setBveModeSettings(AUDIO_FEATURE_BVE_DEFAULT_VALUE);
            } else {
                Log.d(TAG, "FeatureService don't support " + ((FeatureList) this.mBveCallModuleFeatureList.get(i)).getFeatureName());
            }
        }
    }

    private boolean loadZenmodeBlackWhiteListAndUpdate() {
        this.blackWhiteList.getBlackWhiteList();
        this.mAppBlackPathList = this.blackWhiteList.getApplicationPathList();
        this.mEventHandler.removeMessages(MSG_WHITELIST_CHECK_ZENMODE_SECURE);
        this.mEventHandler.sendEmptyMessageDelayed(MSG_WHITELIST_CHECK_ZENMODE_SECURE, 200);
        if (this.mAppBlackPathList == null || this.mAppBlackPathList.size() <= 0) {
            return false;
        }
        Log.d(TAG, "loadBlackWhiteListAndUpdate success!!");
        return true;
    }

    private void getSecureConfigXml(String key) {
        if (key.equals(KEY_ZENMOD)) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.blackWhiteList.getSecureConfigToWhiteListXml()) {
                        Log.d(FeatureService.TAG, "ZenMode getSecureConfigToWhiteListXml finished!");
                        FeatureService.this.mGetSecureConfigTimesZen = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_WHITELIST_UPDATE_ZENMODE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_WHITELIST_UPDATE_ZENMODE_SECURE, 500);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesZen = FeatureService.this.mGetSecureConfigTimesZen + 1;
                    Log.w(FeatureService.TAG, "ZenMode getSecureConfigToWhiteListXml failed! times:" + FeatureService.this.mGetSecureConfigTimesZen);
                    if (FeatureService.this.mGetSecureConfigTimesZen < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE, 1000);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_WHITELIST_CHECK_ZENMODE_SECURE);
                    FeatureService.this.mGetSecureConfigTimesZen = 0;
                }
            }.start();
        } else if (key.equals("BVE")) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.mBveCallModule.getBveConfig()) {
                        Log.d(FeatureService.TAG, "getBveConfig finished!");
                        FeatureService.this.mGetSecureConfigTimesBve = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_UPDATE_BVE_STATE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_UPDATE_BVE_STATE, 800);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesBve = FeatureService.this.mGetSecureConfigTimesBve + 1;
                    Log.w(FeatureService.TAG, "getBveConfig failed! times:" + FeatureService.this.mGetSecureConfigTimesBve);
                    if (FeatureService.this.mGetSecureConfigTimesBve < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_BVE_STATE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_BVE_STATE, 1500);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_BVE_STATE);
                    FeatureService.this.mGetSecureConfigTimesBve = 0;
                }
            }.start();
        } else if (key.equals(KEY_VIDEOBWLIST)) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.VideoBlackWhiteList.getSecureConfigToWhiteListXml()) {
                        Log.d(FeatureService.TAG, "get VideoBlackWhiteList SecureConfig finished!");
                        FeatureService.this.mGetSecureConfigTimesVBW = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_UPDATE_VIDEO_BLACKWHITE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_UPDATE_VIDEO_BLACKWHITE_SECURE, 800);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesVBW = FeatureService.this.mGetSecureConfigTimesVBW + 1;
                    Log.w(FeatureService.TAG, "get VideoBlackWhiteList failed! times:" + FeatureService.this.mGetSecureConfigTimesVBW);
                    if (FeatureService.this.mGetSecureConfigTimesVBW < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE, 1500);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VIDEO_BLACKWHITE_SECURE);
                    FeatureService.this.mGetSecureConfigTimesVBW = 0;
                }
            }.start();
        } else if (key.equals("VAPI")) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.mVAPIserver.getSecureConfigToVAPITable()) {
                        Log.d(FeatureService.TAG, "VAPI getSecureConfigToVAPITable finished!");
                        FeatureService.this.mGetSecureConfigTimesVAPI = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_UPDATE_VAPI_STATE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_UPDATE_VAPI_STATE, 500);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesVAPI = FeatureService.this.mGetSecureConfigTimesVAPI + 1;
                    Log.w(FeatureService.TAG, "VAPI getSecureConfigToVAPITable failed! times:" + FeatureService.this.mGetSecureConfigTimesVAPI);
                    if (FeatureService.this.mGetSecureConfigTimesVAPI < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VAPI_STATE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_VAPI_STATE, 1000);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_VAPI_STATE);
                    FeatureService.this.mGetSecureConfigTimesVAPI = 0;
                }
            }.start();
        } else if (key.equals(KEY_MOTORMODE)) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.mMotorMode.getSecureConfigToWhiteListXml()) {
                        Log.d(FeatureService.TAG, "VAPI getSecureConfigToVAPITable finished!");
                        FeatureService.this.mGetSecureConfigTimesMotorMode = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_UPDATE_MOTORMODE_WHITELIST_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_UPDATE_MOTORMODE_WHITELIST_SECURE, 500);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesMotorMode = FeatureService.this.mGetSecureConfigTimesMotorMode + 1;
                    Log.w(FeatureService.TAG, "get MotorModeWhiteList failed! times:" + FeatureService.this.mGetSecureConfigTimesMotorMode);
                    if (FeatureService.this.mGetSecureConfigTimesMotorMode < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE, 1000);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_MOTORMODE_WHITELIST_SECURE);
                    FeatureService.this.mGetSecureConfigTimesMotorMode = 0;
                }
            }.start();
        } else if (key.equals(KEY_AUDIO_NATIVE_LIST)) {
            new Thread() {
                public void run() {
                    if (FeatureService.this.mAudioNativeserver.getSecureConfigToAudioNativeTable()) {
                        Log.d(FeatureService.TAG, "KEY_AUDIO_NATIVE_LIST getSecureConfigToVAPITable finished!");
                        FeatureService.this.mGetSecureConfigTimesNativeList = 0;
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_UPDATE_AUDIONATIVE_BLACKWHITE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_UPDATE_AUDIONATIVE_BLACKWHITE_SECURE, 500);
                        return;
                    }
                    FeatureService.this.mGetSecureConfigTimesNativeList = FeatureService.this.mGetSecureConfigTimesNativeList + 1;
                    Log.w(FeatureService.TAG, "get KEY_AUDIO_NATIVE_LIST failed! times:" + FeatureService.this.mGetSecureConfigTimesNativeList);
                    if (FeatureService.this.mGetSecureConfigTimesNativeList < 15) {
                        FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE);
                        FeatureService.this.mEventHandler.sendEmptyMessageDelayed(FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE, 1000);
                        return;
                    }
                    FeatureService.this.mEventHandler.removeMessages(FeatureService.MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE);
                    FeatureService.this.mGetSecureConfigTimesNativeList = 0;
                }
            }.start();
        }
    }

    private void updateSecureConfigXml(String key) {
        if (key.equals(KEY_ZENMOD)) {
            if (this.blackWhiteList.checkUpdateBlackWhiteList()) {
                Log.d(TAG, "updateSecureConfigXml ZENMOD");
                loadZenmodeBlackWhiteListAndUpdate();
            }
        } else if (key.equals("BVE")) {
            if (this.mBveCallModule.checkUpdateBveCallModuleList()) {
                updateCurrentBveCallModuleListAndSet();
                Log.d(TAG, "updateSecureConfigXml BVE sucess");
                return;
            }
            Log.d(TAG, "updateSecureConfigXml BVE fail!!");
        } else if (key.equals(KEY_VIDEOBWLIST)) {
            if (this.VideoBlackWhiteList.checkUpdateBlackWhiteList()) {
                this.mEventHandler.removeMessages(MSG_CHECK_VIDEO_BLACKWHITE_SECURE);
                this.mEventHandler.sendEmptyMessageDelayed(MSG_CHECK_VIDEO_BLACKWHITE_SECURE, 600);
                Log.d(TAG, "updateSecureConfigXml VideoBWList sucess");
                sendVideolistUpdate(FeatureManager.FEATURE_TO_PLAYER_VIDEOLIST_UPDATE, 1);
                return;
            }
            Log.d(TAG, "updateSecureConfigXml VideoBWList fail!!");
        } else if (key.equals("VAPI")) {
            if (this.mVAPIserver.checkUpdateVAPITable()) {
                updateCurrentVAPITableAndSet();
                Log.d(TAG, "updateCurrentVAPITableAndSet VAPI sucess");
                return;
            }
            Log.d(TAG, "updateCurrentVAPITableAndSet VAPI fail!!");
        } else if (key.equals(KEY_MOTORMODE)) {
            if (this.mMotorMode.checkUpdateWhiteList()) {
                updateCurrentMotorModeListAndSet();
                Log.d(TAG, "updateCurrentMotorModeListAndSet sucess");
                return;
            }
            Log.d(TAG, "updateCurrentMotorModeListAndSet fail!!");
        } else if (!key.equals(KEY_AUDIO_NATIVE_LIST)) {
        } else {
            if (this.mAudioNativeserver.checkUpdateAudioNativeTable()) {
                updateCurrentAudioServerListAndSet();
                this.mEventHandler.removeMessages(MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE);
                this.mEventHandler.sendEmptyMessageDelayed(MSG_CHECK_AUDIONATIVE_BLACKWHITE_SECURE, 600);
                Log.d(TAG, "updateSecureConfigXml AudioNativeBWList sucess");
                sendAudioNativelistUpdate(FeatureManager.FEATURE_TO_PLAYER_AUDIO_NATIVE_LIST_UPDATE, 1);
                return;
            }
            Log.d(TAG, "updateSecureConfigXml AudioNativeBWList fail!!");
        }
    }

    private void readFeatureConfig() {
        ArrayList<FeatureProject> mProjectLists = new FeatureXmlReader().getFeatureProjects();
        if (mProjectLists == null) {
            Log.w(TAG, "readFeatureConfig read project config from xml fail!!!");
            return;
        }
        String prop = SystemProperties.get("ro.product.model.bbk", null);
        String propWithVer = prop + "-";
        Log.d(TAG, "ro.product.model.bbk is " + prop);
        if (prop != null) {
            String ardVer = SystemProperties.get("ro.build.version.release", null);
            Log.d(TAG, "android version is " + ardVer);
            if (ardVer != null) {
                propWithVer = propWithVer + ardVer;
                Log.d(TAG, "propWithVer is " + propWithVer);
            }
        }
        if (SystemProperties.get("ro.vivo.product.overseas", null).equals("yes")) {
            IS_OVERSEA_VERSION = true;
        }
        int idxWithNoVer = -1;
        int defconfig = -1;
        int found = 0;
        while (found < mProjectLists.size()) {
            if (((FeatureProject) mProjectLists.get(found)).getProjectName().equals("default")) {
                defconfig = found;
                Log.d(TAG, "default config : " + defconfig);
            }
            if (prop.equals(((FeatureProject) mProjectLists.get(found)).getProjectName())) {
                Log.d(TAG, "project " + prop + " has found :" + found);
                idxWithNoVer = found;
            }
            if (propWithVer.equals(((FeatureProject) mProjectLists.get(found)).getProjectName())) {
                Log.d(TAG, "project " + prop + " has found :" + found);
                break;
            }
            found++;
        }
        if (found >= mProjectLists.size()) {
            Log.d(TAG, "readFeatureConfig cann't find project: " + propWithVer);
            if (idxWithNoVer != -1) {
                Log.d(TAG, "readFeatureConfig use project: " + prop);
                found = idxWithNoVer;
            } else if (defconfig == -1) {
                Log.d(TAG, "readFeatureConfig cann't find default config");
                return;
            } else {
                found = defconfig;
            }
        }
        Log.d(TAG, "readFeatureConfig project:" + ((FeatureProject) mProjectLists.get(found)).getProjectName());
        ArrayList<projectFeature> projectfeature = ((FeatureProject) mProjectLists.get(found)).getProjects();
        for (int i = 0; i < projectfeature.size(); i++) {
            String featureName = ((projectFeature) projectfeature.get(i)).getFeatureName();
            if (AUDIO_FEATURE_DUALMIC_NAME.equals(featureName)) {
                AUDIO_FEATURE_DUALMIC = true;
                AUDIO_FEATURE_DUALMIC_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                this.mEnableDualMic = AUDIO_FEATURE_DUALMIC_DEFAULT_VALUE != 0;
                setDualMicSettings(this.mEnableDualMic);
                Log.d(TAG, "dualmic config, default value:" + AUDIO_FEATURE_DUALMIC_DEFAULT_VALUE + "mEnableDualMic:" + this.mEnableDualMic);
            } else if (AUDIO_FEATURE_DUALSPKR_NAME.equals(featureName)) {
                AUDIO_FEATURE_DUALSPKR = true;
                AUDIO_FEATURE_DUALSPK_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "dualspeaker config, default value:" + AUDIO_FEATURE_DUALSPK_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_KTV_NAME.equals(featureName)) {
                AUDIO_FEATURE_KTV = true;
                AUDIO_FEATURE_KTV_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "ktv config, default value:" + AUDIO_FEATURE_KTV_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_SPBOOST_NAME.equals(featureName)) {
                AUDIO_FEATURE_SPBOOST = true;
                AUDIO_FEATURE_SPBOOST_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "spkboost config, default value:" + AUDIO_FEATURE_SPBOOST_DEFAULT_VALUE);
            } else if ("hifi".equals(featureName)) {
                AUDIO_FEATURE_HIFI = true;
                AUDIO_FEATURE_HIFI_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "hifi config, default value:" + AUDIO_FEATURE_HIFI_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_MAXA_NAME.equals(featureName)) {
                AUDIO_FEATURE_MAXA = true;
                AUDIO_FEATURE_MAXA_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "maxa config, default value:" + AUDIO_FEATURE_MAXA_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_MODE_POM.equals(featureName)) {
                AUDIO_FEATURE_POM = true;
                Log.d(TAG, "mode pom config, ok " + AUDIO_FEATURE_POM);
            } else if (AUDIO_SET_DAILPAD_VOL_NAME.equals(featureName)) {
                AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "dailpad volume, default value:" + AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_DSDHW_NAME.equals(featureName)) {
                AUDIO_FEATURE_DSDHW = true;
                Log.d(TAG, "dsdhw config, ok " + AUDIO_FEATURE_DSDHW);
            } else if (AUDIO_FEATURE_BVM_NAME.equals(featureName)) {
                AUDIO_FEATURE_BVM = true;
                Log.d(TAG, "big_volume_mode config, ok " + AUDIO_FEATURE_BVM);
            } else if (AUDIO_FEATURE_SAFE_VOLUME_INDEX_NAME.equals(featureName)) {
                AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "safe volume index set to " + AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE);
            } else if ("MCVS".equals(featureName)) {
                AUDIO_FEATURE_MCVS = true;
                AUDIO_FEATURE_MCVS_DEFAULT_VALUE = ((projectFeature) projectfeature.get(i)).getFeatureValue();
                Log.d(TAG, "mcvs config, ok default value:" + AUDIO_FEATURE_MCVS_DEFAULT_VALUE);
            } else if (AUDIO_FEATURE_HANDSETSPKMODE_NAME.equals(featureName)) {
                AUDIO_FEATURE_HANDSETSPKMODE = true;
                Log.d(TAG, "handset_speaker_mode config, ok " + AUDIO_FEATURE_HANDSETSPKMODE);
            } else {
                Log.d(TAG, "FeatureService don't support " + ((projectFeature) projectfeature.get(i)).getFeatureName());
            }
        }
    }

    private void JudgeSettingAndShowMessage() {
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        Log.d(TAG, "JudgeSettingAndShowMessage   mEnableMicPhone:" + this.mEnableMicPhone);
        if (!this.mEnableMicPhone) {
            RecordShowToast(context);
            Log.d(TAG, "ShowToast");
        }
    }

    private void RecordShowToast(Context context) {
        Toast.makeText(context, context.getString(51249706), 0).show();
    }

    public String setAudioFeature(String feature, IBinder cb) {
        Log.v(TAG, "setAudioFeature " + feature + " calling from " + Binder.getCallingPid());
        TagParameters tagParameters = new TagParameters(feature);
        String tag = tagParameters.tag();
        if (tag == null) {
            Log.e(TAG, "malformated feature " + feature);
            return null;
        }
        TagParameters rt = new TagParameters(tag);
        rt.put(KEY_RETURN, VALUE_OK);
        boolean enable = tagParameters.getBoolean(KEY_STATE, false);
        if (tag.equals(TAG_SPKR_MODE) && AUDIO_FEATURE_DUALSPKR) {
            setSpeakerModeSettings(enable);
            sendMsg(this.mEventHandler, MSG_SET_SPK_MODE_SETTINGS, 0, enable ? 1 : 0, 0, null, 500);
        } else {
            if (tag.equals(TAG_DSDHW) && AUDIO_FEATURE_DSDHW) {
                sendMsg(this.mEventHandler, MSG_SET_DSDHW_MODE_SETTINGS, 0, enable ? 1 : 0, 0, null, 500);
            } else {
                if (tag.equals("MCVS") && AUDIO_FEATURE_MCVS) {
                    sendMsg(this.mEventHandler, MSG_SET_MCVS_MODE_SETTINGS, 0, enable ? 1 : 0, 0, null, 500);
                } else {
                    if (tag.equals(TAG_SPBOOST) && AUDIO_FEATURE_SPBOOST) {
                        int stream = tagParameters.getInt(KEY_STREAM, 0);
                        int i;
                        int device;
                        if (stream <= 0) {
                            Log.v(TAG, "set global setttings");
                            this.mEnableFeatureSPBoost = enable;
                            for (i = 0; i < NUM_STREAM_TYPES; i++) {
                                if ((this.mEnableFeatureSPBoostStreams & (1 << i)) != 0) {
                                    device = getDeviceForStream(i);
                                    setSPBoostSettings(i, device, this.mStreamVolumeInfo[i].isSPBoostOn(device));
                                }
                            }
                        } else {
                            Log.v(TAG, "set stream settings");
                            int activeStream = -1;
                            for (i = 0; i < NUM_STREAM_TYPES; i++) {
                                if ((1 << i) == stream) {
                                    activeStream = i;
                                }
                            }
                            if (activeStream < 0 || activeStream >= NUM_STREAM_TYPES) {
                                Log.e(TAG, "wrong stream " + stream);
                                rt.put(KEY_RETURN, VALUE_ERROR);
                                return rt.toString();
                            }
                            Log.v(TAG, "activeStream = " + activeStream);
                            device = getDeviceForStream(activeStream);
                            int on = this.mStreamVolumeInfo[activeStream].isSPBoostOn(device);
                            if (enable) {
                                this.mEnableFeatureSPBoostStreams |= stream;
                            } else {
                                this.mEnableFeatureSPBoostStreams &= ~stream;
                            }
                            setSPBoostSettings(activeStream, device, on);
                        }
                        Log.v(TAG, "done with SPBoost settings");
                        sendMsg(this.mEventHandler, MSG_SET_SPKBOOST_SETTINGS, 0, 0, 0, null, 500);
                    } else {
                        if (tag.equals(TAG_FACE_DETECT)) {
                            Log.d(TAG, "setAudioFeature PlaybackSetVolume call");
                            PlaybackSetVolume(tagParameters.get(KEY_STREAM), tagParameters.getFloat(KEY_VOLUME, 1.0f));
                        } else {
                            if (tag.equals(TAG_DAILPAD_VOL)) {
                                int vol = tagParameters.getInt(KEY_VOLUME, 0);
                                AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE = vol;
                                Log.v(TAG, "set dailpad vol: " + vol);
                            } else {
                                if (tag.equals(TAG_MICROPHONE)) {
                                    Log.d(TAG, "setAudioFeature setMicrPhoneSettings:" + enable);
                                    setMicrPhoneSettings(enable);
                                } else {
                                    Log.w(TAG, "unkown/read-only feature " + feature);
                                    rt.put(KEY_RETURN, VALUE_ERROR);
                                }
                            }
                        }
                    }
                }
            }
        }
        return rt.toString();
    }

    public String getAudioFeature(String feature) {
        Log.v(TAG, "getAudioFeature " + feature + " calling from " + Binder.getCallingPid());
        TagParameters tp = new TagParameters(feature);
        String tag = tp.tag();
        if (tag == null) {
            Log.e(TAG, "malformated feature " + feature);
            return null;
        }
        Log.v(TAG, "tp.get(KEY_VOLUME): " + tp.get(KEY_VOLUME));
        if (tp.get(KEY_VOLUME) != null) {
            int vol = 0;
            if (tag.equals(TAG_DAILPAD_VOL)) {
                vol = AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE;
            }
            tp.put(KEY_RETURN, VALUE_OK);
            tp.put(KEY_VOLUME, vol);
            return tp.toString();
        } else if (tp.get("hifi") != null) {
            int config = 0;
            Log.v(TAG, "tp.get(KEY_HIFI): " + tp.get("hifi"));
            if (AUDIO_FEATURE_HIFI) {
                config = AUDIO_FEATURE_HIFI_DEFAULT_VALUE;
            }
            tp.put(KEY_RETURN, VALUE_OK);
            tp.put("hifi", config);
            return tp.toString();
        } else if (tp.get(KEY_STATE) == null) {
            Log.e(TAG, "only support query state now");
            tp.put(KEY_RETURN, VALUE_ERROR);
            return tp.toString();
        } else {
            tp.put(KEY_RETURN, VALUE_OK);
            boolean enabled = false;
            if (tag.equals(AUDIO_FEATURE_DMIC_EXIST)) {
                enabled = AUDIO_FEATURE_DUALMIC;
            } else if (tag.equals(AUDIO_FEATURE_DUALSPKR_EXIST)) {
                enabled = AUDIO_FEATURE_DUALSPKR;
            } else if (tag.equals(AUDIO_FEATURE_SPBOOST_EXIST)) {
                enabled = AUDIO_FEATURE_SPBOOST;
            } else if (tag.equals(AUDIO_FEATURE_MAXA_EXIST)) {
                enabled = AUDIO_FEATURE_MAXA;
            } else if (tag.equals(AUDIO_FEATURE_KTV_EXIST)) {
                enabled = AUDIO_FEATURE_KTV;
            } else if (tag.equals(AUDIO_FEATURE_VIVO_INCALL_EXIST)) {
                enabled = AUDIO_FEATURE_VIVO_INCALL;
            } else if (tag.equals(AUDIO_FEATURE_DSDHW_EXIST)) {
                enabled = AUDIO_FEATURE_DSDHW;
            } else if (tag.equals(AUDIO_FEATURE_MCVS_EXIST)) {
                enabled = AUDIO_FEATURE_MCVS;
            } else if (tag.equals(TAG_VIVO_INCALL)) {
                enabled = getPhoneState_POM() > 0;
            } else if (tag.equals(TAG_DMIC)) {
                enabled = getDualMicSettings();
            } else if (tag.equals(TAG_SPKR_MODE)) {
                enabled = getSpeakerModeSettings();
            } else if (tag.equals(TAG_DSDHW)) {
                enabled = getDSDHwModeSettings();
            } else if (tag.equals("MCVS")) {
                enabled = getMCVSModeSettings();
            } else if (tag.equals(TAG_SPBOOST)) {
                enabled = getSPBoostSettings();
            } else if (tag.equals(TAG_IMUSIC) || tag.equals(TAG_VIVOVIDEO)) {
                synchronized (this.mAudioFeatureCallbacks) {
                    if (this.mAudioFeatureCallbacks.containsKey(tag)) {
                        int N = ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).beginBroadcast();
                        if (N > 0) {
                            int pos = N;
                            while (pos > 0) {
                                pos--;
                                IAudioFeatureCallback callback = (IAudioFeatureCallback) ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).getBroadcastItem(pos);
                                try {
                                    enabled = new TagParameters(callback.onCallback(feature)).getBoolean(KEY_STATE, false);
                                    if (enabled) {
                                    }
                                } catch (RemoteException e) {
                                    Log.w(TAG, "unregistered callback: " + callback + " " + e);
                                    ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).unregister(callback);
                                    N--;
                                }
                            }
                        } else {
                            Log.v(TAG, "NO remote callback for " + tag);
                        }
                        ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).finishBroadcast();
                        if (N <= 0) {
                            Log.d(TAG, "empty map entry for " + tag);
                            this.mAudioFeatureCallbacks.remove(tag);
                        }
                    } else {
                        enabled = false;
                    }
                }
            } else if (tag.equals(TAG_HEANDSETSPK)) {
                enabled = isHandsetSpeakerSupport() ? getFeatureSupport("HandsetSpk") : false;
            }
            tp.put(KEY_STATE, enabled);
            Log.v(TAG, "getAudioFeature return " + tp.toString());
            return tp.toString();
        }
    }

    public String registerAudioFeatureCallback(IAudioFeatureCallback callback, String arg0, IBinder cb) {
        Log.v(TAG, "registerAudioFeatureCallback " + arg0 + " calling from " + Binder.getCallingPid());
        String tag = new TagParameters(arg0).tag();
        if (tag == null) {
            Log.e(TAG, "malformated arg0" + arg0);
            return null;
        }
        TagParameters rt = new TagParameters(tag);
        rt.put(KEY_RETURN, VALUE_OK);
        synchronized (this.mAudioFeatureCallbacks) {
            if (!this.mAudioFeatureCallbacks.containsKey(tag)) {
                this.mAudioFeatureCallbacks.put(tag, new RemoteCallbackList());
            }
            ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).register(callback);
        }
        return rt.toString();
    }

    public String unregisterAudioFeatureCallback(IAudioFeatureCallback callback, String arg0) {
        Log.v(TAG, "unregisterAudioFeatureCallback " + arg0 + " calling from " + Binder.getCallingPid());
        String tag = new TagParameters(arg0).tag();
        if (tag == null) {
            Log.e(TAG, "malformated arg0" + arg0);
            return null;
        }
        TagParameters rt = new TagParameters(tag);
        rt.put(KEY_RETURN, VALUE_OK);
        synchronized (this.mAudioFeatureCallbacks) {
            if (this.mAudioFeatureCallbacks.containsKey(tag)) {
                ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).unregister(callback);
                int N = ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).beginBroadcast();
                ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).finishBroadcast();
                if (N <= 0) {
                    this.mAudioFeatureCallbacks.remove(tag);
                }
            } else {
                Log.w(TAG, "not registered callback");
            }
        }
        return rt.toString();
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delay);
    }

    private void sendStickyBroadcastToAll(Intent intent) {
        if (this.mSystemReadyCond) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "sendStickyBroadcastToAll mSystemReady is not ready");
        }
    }

    private int getDeviceForStream(int stream) {
        int device = AudioSystem.getDevicesForStream(stream);
        if (((device - 1) & device) == 0) {
            return device;
        }
        if ((device & 4) != 0) {
            return 4;
        }
        if ((device & 8) != 0) {
            return 8;
        }
        if ((device & 2) != 0) {
            return 2;
        }
        return device & 896;
    }

    public int onMediaServerDied() {
        Log.d(TAG, "onMediaServerDied ");
        setMicrPhoneSettings(this.mEnableMicPhone);
        Log.d(TAG, "onMediaServerDied  setMicrPhoneSettings:" + this.mEnableMicPhone);
        int state = FeatureManager.native_init();
        if (state == 1) {
            updateCurrentBveCallModuleListAndSet();
            if (getDualMicSettings()) {
                setDualMicSettings(this.mEnableDualMic);
            }
            if (getMCVSModeSettings()) {
                AudioSystem.setParameters("vivo_multichnvs_enable=1");
            }
            if (getSpeakerModeSettings()) {
                setSpeakerModeSettings(this.mEnableSpeakerMode);
            }
            if (this.mLowPower) {
                FeatureManager.setAudioFeatures("low_power=1");
            }
            int game_4d_shock_enabled = System.getInt(this.mContext.getContentResolver(), "game_4d_shock_enabled", 0);
            int game_do_not_disturb = System.getInt(this.mContext.getContentResolver(), "game_do_not_disturb", 0);
            if (game_4d_shock_enabled <= 0 || game_do_not_disturb <= 0) {
                FeatureManager.setAudioFeatures("game_vibrate=off");
            } else {
                FeatureManager.setAudioFeatures("game_vibrate=on");
            }
        }
        return state;
    }

    public void onAudioEffectCallback(AudioEffectCallback audioEffectCallback) {
        int action = audioEffectCallback.mAction;
        int pid = audioEffectCallback.mPid;
        int sessionid = audioEffectCallback.mSessionid;
        int index = -1;
        boolean bSendIntent = false;
        boolean bFind = false;
        boolean bRemove = false;
        Log.d(TAG, "onAudioEffectCallback action: " + audioEffectCallback.mAction + " pid: " + audioEffectCallback.mPid + " sessionid: " + audioEffectCallback.mSessionid);
        if (this.mNotifyCallbackArray.size() > 0) {
            Iterator<AudioEffectCallback> Iterator = this.mNotifyCallbackArray.iterator();
            while (Iterator.hasNext()) {
                index++;
                AudioEffectCallback notifyCallbackInfo = (AudioEffectCallback) Iterator.next();
                if (notifyCallbackInfo.mPid == pid && notifyCallbackInfo.mSessionid == sessionid) {
                    bFind = true;
                    if (notifyCallbackInfo.mAction == action && action == 0) {
                        notifyCallbackInfo.mCount++;
                    } else if (action == 1) {
                        notifyCallbackInfo.mCount--;
                    }
                    if (notifyCallbackInfo.mCount == 0) {
                        bRemove = true;
                    }
                }
            }
        }
        if (bRemove) {
            this.mNotifyCallbackArray.remove(index);
            bSendIntent = true;
            Log.d(TAG, "NotifyCallback Remove, pid : " + pid + " sessionid " + sessionid);
        }
        if (!bFind) {
            audioEffectCallback.mCount = 1;
            this.mNotifyCallbackArray.add(audioEffectCallback);
            bSendIntent = true;
            Log.d(TAG, "NotifyCallback, Add, pid: " + pid + "sessionid " + sessionid);
        }
        if (bSendIntent) {
            Intent intent = new Intent("com.vivo.audiofx.maxxaudio.effect");
            intent.setPackage("com.vivo.audiofx");
            intent.putExtra("action", action);
            intent.putExtra("pid", pid);
            intent.putExtra("sessionid", sessionid);
            sendStickyBroadcastToAll(intent);
            Log.d(TAG, "SendIntent:com.vivo.audiofx.maxxaudio.effect action : " + action + " pid " + pid + " sessionid " + sessionid);
        }
    }

    private void onShowHifiStateChanged(int state) {
        boolean z = false;
        Log.d(TAG, "onHifiStateChanged " + state);
        Intent intent = new Intent("com.bbk.audiofx.hifi.display");
        String str = KEY_STATE;
        if (state > 0) {
            z = true;
        }
        intent.putExtra(str, z);
        sendStickyBroadcastToAll(intent);
    }

    private void onSendShowDSDIntent(int state) {
        boolean z = false;
        Log.d(TAG, "onSendShowDSDIntent " + state);
        Intent intent = new Intent("com.vivo.media.dsd.display");
        String str = KEY_STATE;
        if (state > 0) {
            z = true;
        }
        intent.putExtra(str, z);
        sendStickyBroadcastToAll(intent);
    }

    private void setMicrPhoneSettings(boolean enable) {
        Log.d(TAG, "setMicrPhoneSettings enable:" + enable);
        this.mEnableMicPhone = enable;
        FeatureManager.setAudioFeatures("mic_phone_enable=" + (enable ? 1 : 0));
    }

    private void setDualMicSettings(boolean enable) {
        if (AUDIO_FEATURE_DUALMIC) {
            this.mEnableDualMic = enable;
            FeatureManager.setAudioFeatures("dmic_enable=" + (this.mEnableDualMic ? 1 : 0));
        }
    }

    private boolean getDualMicSettings() {
        return this.mEnableDualMic;
    }

    private void setSpeakerModeSettings(boolean enable) {
        if (AUDIO_FEATURE_DUALSPKR) {
            this.mEnableSpeakerMode = enable;
            FeatureManager.setAudioFeatures("speaker_mode=" + (this.mEnableSpeakerMode ? 1 : 0));
        }
    }

    private void setBveModeSettings(int enable) {
        boolean z;
        int i = 1;
        if (enable != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mEnableBveMode = z;
        StringBuilder append = new StringBuilder().append("bve_mode=");
        if (!this.mEnableBveMode) {
            i = 0;
        }
        FeatureManager.setAudioFeatures(append.append(i).toString());
    }

    private boolean getSpeakerModeSettings() {
        return this.mEnableSpeakerMode;
    }

    private void onSetSpkrModeSettings(int feature) {
        System.putInt(this.mContentResolver, "vivo_feature_audio_spkrmode", feature);
    }

    public byte[] intToByteArray(int a) {
        return new byte[]{(byte) ((a >> 24) & 255), (byte) ((a >> 16) & 255), (byte) ((a >> 8) & 255), (byte) (a & 255)};
    }

    private void sendAudioNativelistUpdate(int msgCode, int data) {
        this.bytesInt = intToByteArray(data);
        Log.d(TAG, "sendAudioNativelistUpdate, msgCode: " + msgCode + " data " + data);
        FeatureManager.setPlayerFeatures(msgCode, this.bytesInt, data);
    }

    private void sendVideolistUpdate(int msgCode, int data) {
        this.bytesInt = intToByteArray(data);
        Log.d(TAG, "sendVideolistUpdate, msgCode: " + msgCode + " data " + data);
        FeatureManager.setPlayerFeatures(msgCode, this.bytesInt, data);
    }

    private void setDSDHwSwitch(int msgCode, int data) {
        this.bytesInt = intToByteArray(data);
        Log.d(TAG, "setDSDHWSwitch, msgCode: " + msgCode + " data " + data);
        FeatureManager.setPlayerFeatures(msgCode, this.bytesInt, data);
    }

    private void setDSDHwModeSettings(boolean enable) {
        if (AUDIO_FEATURE_DSDHW && this.mEnableDSDHwMode != enable) {
            this.mEnableDSDHwMode = enable;
            if (this.mEnableDSDHwMode) {
                setDSDHwSwitch(500, 1);
            } else {
                setDSDHwSwitch(500, 0);
            }
        }
    }

    private void setMCVSModeSettings(boolean enable) {
        if (AUDIO_FEATURE_MCVS && this.mEnableMCVSMode != enable) {
            this.mEnableMCVSMode = enable;
            if (this.mEnableMCVSMode) {
                AudioSystem.setParameters("vivo_multichnvs_enable=1");
            } else {
                AudioSystem.setParameters("vivo_multichnvs_enable=0");
            }
        }
    }

    private void setDSDHwHeadsetPlug(int plug) {
        if (AUDIO_FEATURE_DSDHW) {
            if (plug > 0) {
                setDSDHwSwitch(FeatureManager.FEATURE_TO_PLAYER_HEADSET_PULG, 1);
            } else {
                setDSDHwSwitch(FeatureManager.FEATURE_TO_PLAYER_HEADSET_PULG, 0);
                sendMsg(this.mEventHandler, MSG_SHOW_DSD_STATE, 0, 0, 0, null, 100);
            }
        }
    }

    private boolean getDSDHwModeSettings() {
        return this.mEnableDSDHwMode;
    }

    private void onSetDSDHwModeSettings(int feature) {
        System.putInt(this.mContentResolver, "vivo_feature_dsd_hw", feature);
        if (feature == 1) {
            setDSDHwModeSettings(true);
        } else {
            setDSDHwModeSettings(false);
        }
    }

    private boolean getMCVSModeSettings() {
        return this.mEnableMCVSMode;
    }

    private void onSetMCVSModeSettings(int feature) {
        boolean z = true;
        System.putInt(this.mContentResolver, "vivo_feature_multichnvs", feature);
        if (feature != 1) {
            z = false;
        }
        setMCVSModeSettings(z);
    }

    private boolean getFeatureSupport(String feature) {
        if (feature == null || feature.equals("")) {
            return false;
        }
        boolean result = false;
        this.mAudioNativeList = this.mAudioNativeserver.getAudioNativeTable();
        if (this.mAudioNativeList != null) {
            for (int i = 0; i < this.mAudioNativeList.size(); i++) {
                String mFeatureName = ((AudioNativeList) this.mAudioNativeList.get(i)).get_list_name();
                if (mFeatureName != null && mFeatureName.equals(feature)) {
                    result = true;
                    Log.d(TAG, "getFeatureSupport:" + feature);
                    break;
                }
            }
        }
        return result;
    }

    public void setSPBoostSettings(int streamType, int device, int on) {
        Log.d(TAG, "setSPBoostSettings " + device + " " + streamType + " " + on);
        FeatureManager.setAudioFeatures("spboost_mode=" + (((device << 16) | (streamType << 8)) | on));
    }

    public boolean getSPBoostSettings() {
        return this.mEnableFeatureSPBoost;
    }

    private void onSetSpkrBoostSettings(int feature) {
        System.putInt(this.mContentResolver, "vivo_features_for_spboost", feature);
    }

    private int processSPBoost(int streamType, int device, int flags) {
        int on = this.mStreamVolumeInfo[streamType].isSPBoostOn(device);
        if (on > 0) {
            flags |= FLAG_SHOW_SPBOOST;
        }
        sendMsg(this.mEventHandler, MSG_SHOW_SPBOOST_STATE, 0, streamType, on, null, 100);
        setSPBoostSettings(streamType, device, on);
        return flags;
    }

    private void onShowSPBoostState(int streamType, int state) {
        StreamVolumeInfo streamVolume = this.mStreamVolumeInfo[streamType];
        boolean isMax = streamVolume.getIndex(getDeviceForStream(streamType)) == streamVolume.getMaxIndex();
        Log.d(TAG, "showSPBoostState: " + streamType + " " + isMax + " " + this.mSPBShowState[streamType] + " show: " + state);
        if ((this.mEnableFeatureSPBoostStreams & (1 << streamType)) == 0 || getDeviceForStream(streamType) != 2 || isMax == this.mSPBShowState[streamType]) {
            Log.d(TAG, "onShowSPBoostState, return ");
            return;
        }
        this.mSPBShowState[streamType] = isMax;
        if (state > 0 && this.mEnableFeatureSPBoost) {
            int resid = 51249468;
            if (isMax) {
                resid = 51249469;
            }
            if (this.mToast == null) {
                this.mToast = Toast.makeText(this.mContext, resid, 0);
            } else {
                this.mToast.setText(resid);
            }
            this.mToast.show();
        }
    }

    public void updateParameters(String para, IBinder cb) {
        if (AUDIO_FEATURE_KTV && para != null) {
            StringTokenizer st = new StringTokenizer(para, "=");
            if (st.countTokens() != 2) {
                Log.e(TAG, "updateParameters: malformated string " + para);
                return;
            }
            String tag = st.nextToken();
            Log.v(TAG, "updateParameters: tag = " + tag);
            String value;
            if (tag.equals(TAG_KTV_MODE)) {
                value = st.nextToken();
                Log.v(TAG, "vivo_ktv_mode: value = " + value + " pid = " + Binder.getCallingPid());
                this.mIsKTVMode = Integer.parseInt(value);
                synchronized (mKTVModeLock) {
                    setKTVModeDeathHandler(this.mIsKTVMode, cb);
                }
            } else if (tag.equals(TAG_KTV_EXT_SP)) {
                value = st.nextToken();
                Log.v(TAG, "vivo_ktv_ext_speaker: value = " + value + " pid = " + Binder.getCallingPid());
                this.mKTVExtSpMode = Integer.parseInt(value);
                sendMsg(this.mEventHandler, MSG_SET_KTV_EXT_SP_MODE, 0, this.mKTVExtSpMode, 0, null, 300);
            }
        }
    }

    public static String[] getKTVSupportedPackages() {
        if (IS_OVERSEA_VERSION) {
            return KTV_SUPPORTED_PACKGAGES_OVERSEAS;
        }
        return KTV_SUPPORTED_PACKGAGES;
    }

    private static String getKTVSupportedPackageName(int order) {
        if (IS_OVERSEA_VERSION) {
            return KTV_SUPPORTED_PACKGAGES_OVERSEAS[order];
        }
        return KTV_SUPPORTED_PACKGAGES[order];
    }

    public static String[] getKTVCustomPackages() {
        if (IS_OVERSEA_VERSION) {
            return NEED_CUSTOMIZE_PACKGAGES_OVERSEAS;
        }
        return NEED_CUSTOMIZE_PACKGAGES;
    }

    private static String getKTVCustomPackageName(int order) {
        if (IS_OVERSEA_VERSION) {
            return NEED_CUSTOMIZE_PACKGAGES_OVERSEAS[order];
        }
        return NEED_CUSTOMIZE_PACKGAGES[order];
    }

    private static String getKTVRealName(String pkgname) {
        if (pkgname.equals("cn.banshenggua.aichang:aichang") || pkgname.equals("cn.banshenggua.aichang:aichangliveroom")) {
            return "cn.banshenggua.aichang";
        }
        return pkgname;
    }

    public boolean isKTVSurpported(String keys, String pkgname) {
        if (AUDIO_FEATURE_KTV && keys.equals(TAG_KTV_MODE)) {
            boolean flag = false;
            for (int i = 0; i < getKTVCustomPackages().length; i++) {
                if (pkgname.equals(getKTVCustomPackageName(i))) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return true;
            }
        }
        return false;
    }

    public void notifyKTVRecordState(boolean state, String pkgname, IBinder cb) {
        if (AUDIO_FEATURE_KTV && cb != null && isPkgKTVSupported(pkgname)) {
            this.mIsKTVRecording = state;
            this.mKTVRecordingPkgName = pkgname;
            this.mKTVClientCb = cb;
            Log.v(TAG, "notifyKTVRecordState: " + state + " " + pkgname + ":" + isPkgKTVSwitchON(pkgname) + " " + Binder.getCallingPid());
            updateKTVModeState(this.mKTVRecordingPkgName);
        }
    }

    public void notifyKTVRecordState(KTVRecordInfo info) {
        boolean z = true;
        if (AUDIO_FEATURE_KTV) {
            String pkgname = getKTVRealName(getKTVAppName(info.mPid));
            Integer keyPid = new Integer(info.mPid);
            if (info.mState == 1) {
                if (!this.mClientMap.containsKey(keyPid)) {
                    this.mClientMap.put(keyPid, pkgname);
                }
            } else if (this.mClientMap.containsKey(keyPid)) {
                if (pkgname.equals("")) {
                    pkgname = (String) this.mClientMap.get(keyPid);
                    Log.d(TAG, "null package found, package may killed already, use cache name:" + pkgname);
                }
                this.mClientMap.remove(keyPid);
            }
            Log.d(TAG, "notifyKTVRecordState pkgname=" + pkgname);
            if (isPkgKTVSupported(pkgname)) {
                if (info.mState != 1) {
                    z = false;
                }
                this.mIsKTVRecording = z;
                this.mKTVRecordingPkgName = pkgname;
                this.mKTVClientCb = null;
                Log.v(TAG, "notifyKTVRecordState: " + info.mState + " " + info.mPid + " " + pkgname + " : " + isPkgKTVSwitchON(pkgname));
                updateKTVModeState(this.mKTVRecordingPkgName);
            }
        }
    }

    private String getKTVAppName(int pID) {
        String processName = "";
        for (RunningAppProcessInfo pinfo : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            try {
                if (pinfo.pid == pID) {
                    return pinfo.processName;
                }
            } catch (Exception e) {
            }
        }
        return processName;
    }

    private void updateKTVModeState(String pkgname) {
        if (AudioSystem.getDeviceConnectionState(4, "") == 1 || AudioSystem.getDeviceConnectionState(8, "") == 1) {
            if (this.mIsKTVRecording && isPkgKTVSwitchON(pkgname)) {
                this.mIsKTVMode = 1;
                sendMsg(this.mEventHandler, MSG_SET_KTV_MODE_SETTING, 0, 1, 0, null, 0);
            } else {
                closeKTVMode(false);
            }
            boolean flag = AudioSystem.isStreamActive(3, 0);
            Intent i = new Intent(ACTION_KTV_STATE_CHANGED);
            i.putExtra(KEY_STATE, this.mIsKTVMode);
            i.putExtra("pkgname", pkgname);
            i.putExtra("needplay", flag ^ 1);
            sendStickyBroadcastToAll(i);
            Log.v(TAG, "updateKTVModeState: state=" + this.mIsKTVMode + " recording=" + this.mIsKTVRecording + " music=" + flag + " " + pkgname);
            setKTVModeDeathHandler(this.mIsKTVMode, this.mKTVClientCb);
        } else if (this.mIsKTVMode == 1) {
            closeKTVMode(true);
        }
    }

    private boolean isPkgKTVSupported(String pkgname) {
        for (int i = 0; i < getKTVSupportedPackages().length; i++) {
            if (pkgname.equals(getKTVSupportedPackageName(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPkgKTVSwitchON(String pkgname) {
        int i = 0;
        while (i < getKTVSupportedPackages().length) {
            if (pkgname.equals(getKTVSupportedPackageName(i)) && System.getInt(this.mContentResolver, getKTVSupportedPackageName(i), 0) == 0) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void onKTVModeSettings(int enable) {
        Log.v(TAG, "onKTVModeSettings: enable=" + enable);
        AudioSystem.setParameters("vivo_ktv_mode=" + enable);
        if (enable == 1 && this.mKTVExtSpMode == 2 && AudioSystem.getDeviceConnectionState(4, "") == 1) {
            sendMsg(this.mEventHandler, MSG_SET_KTV_EXT_SP_MODE, 0, this.mKTVExtSpMode, 0, null, 300);
        } else {
            sendMsg(this.mEventHandler, MSG_SET_KTV_EXT_SP_MODE, 0, 0, 0, null, 300);
        }
    }

    private void onKTVExtSPModeUse(int mode) {
        if (mode == 2 && AudioSystem.getDeviceConnectionState(4, "") == 1) {
            Log.v(TAG, "enable headset+speaker");
            AudioSystem.setForceUse(1, 1);
        } else if (AudioSystem.getForceUse(1) == 1) {
            Log.v(TAG, "disable headset+speaker");
            AudioSystem.setForceUse(1, 0);
        }
    }

    private void closeKTVMode(boolean notify) {
        Log.v(TAG, "closeKTVMode: notify=" + notify);
        this.mIsKTVMode = 0;
        sendMsg(this.mEventHandler, MSG_SET_KTV_MODE_SETTING, 0, 0, 0, null, 0);
        if (notify) {
            Intent i = new Intent(ACTION_KTV_STATE_CHANGED);
            i.putExtra(KEY_STATE, 0);
            i.putExtra("pkgname", this.mKTVRecordingPkgName);
            i.putExtra("needplay", false);
            sendStickyBroadcastToAll(i);
        }
    }

    private void setKTVModeDeathHandler(int mode, IBinder cb) {
        if (cb == null) {
            Log.w(TAG, "setKTVModeDeathHandler() called with null binder");
            return;
        }
        if (mode == 1) {
            if (this.mKTVModeDeathHandler == null) {
                this.mKTVModeDeathHandler = new KTVModeDeathHandler(cb);
            }
        } else if (this.mKTVModeDeathHandler != null) {
            this.mKTVModeDeathHandler.release();
            this.mKTVModeDeathHandler = null;
        }
    }

    public void AudioServiceInitHook(int[] maxIndex, int[] alias, int[] defVolume) {
        int i;
        for (i = 0; i < NUM_STREAM_TYPES; i++) {
            this.mMaxStreamVolume[i] = maxIndex[i];
            this.mDefaultStreamVolume[i] = defVolume[i];
            this.mStreamVolumeAlias[i] = alias[i];
        }
        this.mStreamVolumeInfo = new StreamVolumeInfo[NUM_STREAM_TYPES];
        for (i = 0; i < NUM_STREAM_TYPES; i++) {
            this.mStreamVolumeInfo[i] = new StreamVolumeInfo(System.VOLUME_SETTINGS_INT[this.mStreamVolumeAlias[i]], i);
        }
        this.mStreamVolumeInit = true;
        if (AUDIO_FEATURE_SPBOOST) {
            this.mSPBShowState = new boolean[NUM_STREAM_TYPES];
            for (i = 0; i < NUM_STREAM_TYPES; i++) {
                boolean z;
                int on = this.mStreamVolumeInfo[i].isSPBoostOn(2);
                if (on > 0) {
                    setSPBoostSettings(i, 2, on);
                }
                boolean[] zArr = this.mSPBShowState;
                if (this.mStreamVolumeInfo[i].getIndex(2) == this.mStreamVolumeInfo[i].getMaxIndex()) {
                    z = true;
                } else {
                    z = false;
                }
                zArr[i] = z;
            }
        }
    }

    public int adjustStreamVolumeHook(int streamType, int index, int device, int flags) {
        int result = flags;
        if (!this.mStreamVolumeInit) {
            return flags;
        }
        if (streamType < 0 || streamType >= NUM_STREAM_TYPES) {
            Log.d(TAG, "adjustStreamVolumeHook, streamType: " + streamType + " error, return ");
            return flags;
        }
        Log.d(TAG, "adjustStreamVolumeHook, streamType: " + streamType + " index " + index + " device " + device);
        this.mStreamVolumeInfo[streamType].setIndex(index, device);
        if (AUDIO_FEATURE_SPBOOST) {
            result = processSPBoost(streamType, device, flags);
        }
        return result;
    }

    public int setStreamVolumeHook(int streamType, int index, int device, int flags) {
        int result = flags;
        if (!this.mStreamVolumeInit) {
            return flags;
        }
        if (streamType < 0 || streamType >= NUM_STREAM_TYPES) {
            Log.d(TAG, "setStreamVolumeHook, streamType: " + streamType + " error, return ");
            return flags;
        }
        Log.d(TAG, "setStreamVolumeHook streamType " + streamType + " index " + index + " device " + device + " by:" + Binder.getCallingPid());
        this.mStreamVolumeInfo[streamType].setIndex(index, device);
        if (AUDIO_FEATURE_SPBOOST) {
            result = processSPBoost(streamType, device, flags);
        }
        return result;
    }

    public int setStreamMuteHook(int streamType, boolean state) {
        int result = 0;
        if (!this.mStreamVolumeInit) {
            return 0;
        }
        if (streamType < 0 || streamType >= NUM_STREAM_TYPES) {
            Log.d(TAG, "setStreamMuteHook, streamType: " + streamType + " error, return ");
            return 0;
        }
        int device = getDeviceForStream(streamType);
        Log.d(TAG, "setStreamMuteHook streamType " + streamType + " state " + state);
        this.mStreamVolumeInfo[streamType].mute(state);
        if (AUDIO_FEATURE_SPBOOST) {
            result = processSPBoost(streamType, device, 1);
        }
        return result;
    }

    public void setRingerModeIntHook(int ringerMode) {
        this.mRingerMode = ringerMode;
        Log.d(TAG, "setRingerModeIntHook, mRingerMode: " + this.mRingerMode);
    }

    public boolean onSetWiredDeviceConnectionStateHook(int device, int state, String name) {
        if (AUDIO_FEATURE_KTV && name.equals(TAG_KTV_MIC)) {
            Log.v(TAG, "ktv mic: setMasterMute " + (state == 0 ? "true" : "false"));
            if (state == 0) {
                AudioSystem.setMasterMute(true);
            } else {
                AudioSystem.setMasterMute(false);
            }
        }
        return false;
    }

    public boolean handleDeviceConnectionHook(boolean connected, int device, String params) {
        if (AUDIO_FEATURE_SPBOOST) {
            for (int i = 0; i < NUM_STREAM_TYPES; i++) {
                if ((this.mEnableFeatureSPBoostStreams & (1 << i)) != 0) {
                    setSPBoostSettings(i, device, this.mStreamVolumeInfo[i].isSPBoostOn(getDeviceForStream(i)));
                }
            }
        }
        return false;
    }

    public boolean sendDeviceConnectionIntentHook(int device, int state, String name) {
        if (!AUDIO_FEATURE_KTV || !name.equals(TAG_KTV_MIC)) {
            return false;
        }
        Log.v(TAG, "sendDeviceConnectionIntent: ktv mic won't send");
        return true;
    }

    public int setPhoneState_POM(int state, int POM) {
        return FeatureManager.setPhoneState_POM(state, POM);
    }

    public int getPhoneState_POM() {
        return FeatureManager.getPhoneState_POM();
    }

    public boolean isPOMSupport() {
        return AUDIO_FEATURE_POM;
    }

    public boolean isBVMSupport() {
        Log.d(TAG, "evan add isBVMSupport" + AUDIO_FEATURE_BVM);
        return AUDIO_FEATURE_BVM;
    }

    public boolean isSafeVolumeSupport() {
        boolean z;
        String str = TAG;
        StringBuilder append = new StringBuilder().append("isSafeVolumeSupport ");
        if (AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE > 0) {
            z = true;
        } else {
            z = false;
        }
        Log.d(str, append.append(z).toString());
        return AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE > 0;
    }

    public int getSafeVolumeIndex() {
        Log.d(TAG, "getSafeVolumeIndex " + AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE);
        return AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE;
    }

    public boolean isHandsetSpeakerSupport() {
        Log.d(TAG, "isHandsetSpeakerSupport:" + AUDIO_FEATURE_HANDSETSPKMODE);
        return AUDIO_FEATURE_HANDSETSPKMODE;
    }

    public void makeA2dpDeviceAvailableHOOK(String address, String name) {
        setDSDHwSwitch(FeatureManager.FEATURE_TO_PLAYER_A2DP_AVAILABLE, 1);
    }

    public void makeA2dpDeviceUnavailableNowHOOK(String address) {
        setDSDHwSwitch(FeatureManager.FEATURE_TO_PLAYER_A2DP_AVAILABLE, 0);
    }

    public int setStreamVolumeDeltaIndexHook(int streamType, int index, int device) {
        return FeatureManager.setStreamVolumeDeltaIndex(streamType, index, device);
    }

    public boolean isApplicationForeground(String pkgname) {
        Field field = null;
        try {
            field = RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {
        }
        for (RunningAppProcessInfo appProcess : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            if (appProcess.importance == 100 && appProcess.importanceReasonCode == 0) {
                Integer state = null;
                if (field != null) {
                    try {
                        state = Integer.valueOf(field.getInt(appProcess));
                    } catch (Exception e2) {
                    }
                }
                if (state != null && state.intValue() == 2) {
                    Log.d(TAG, "the player App is Foreground :" + appProcess.processName);
                    if (appProcess.processName.contains(pkgname)) {
                        Log.d(TAG, "We have found the right Application");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isZenmodSurpported(String keys, String pkgname, String FilePath, long length) {
        boolean ZenModeFlag = false;
        PathList pathListR = null;
        if ((AUDIO_FEATURE_ZENMOD && keys.equals(TAG_ZEN_MODE)) || this.mNotInterruptDuringDrive) {
            int i;
            for (i = 0; i < this.mAppBlackPathList.size(); i++) {
                PathList pathList = (PathList) this.mAppBlackPathList.get(i);
                if (pathList != null && pkgname.contains(pathList.getPathListName())) {
                    ZenModeFlag = true;
                    pathListR = pathList;
                    break;
                }
            }
            if (ZenModeFlag && pathListR != null && (isApplicationForeground(pkgname) ^ 1) != 0 && pathListR.getPaths().size() > 0) {
                Log.d(TAG, "getFilePath  length:" + length + " start pid:" + Binder.getCallingPid());
                for (i = 0; i < pathListR.getPaths().size(); i++) {
                    if (FilePath.contains((CharSequence) pathListR.getPaths().get(i))) {
                        Log.d(TAG, "getFilePath : " + ((String) pathListR.getPaths().get(i)));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean computeDriveMode() {
        boolean driveModeEnabled = System.getInt(this.mContext.getContentResolver(), "drive_mode_enabled", 0) == 1;
        boolean shieldNotificationEnabled = System.getInt(this.mContext.getContentResolver(), "shield_notification_reminder_enabled", 0) == 1;
        if (driveModeEnabled) {
            return shieldNotificationEnabled;
        }
        return false;
    }

    public boolean AdjustZenModeUtils(String pkgname) {
        boolean result = false;
        if (AUDIO_FEATURE_ZENMOD || this.mNotInterruptDuringDrive) {
            for (int i = 0; i < this.mAppBlackPathList.size(); i++) {
                PathList pathList = (PathList) this.mAppBlackPathList.get(i);
                if (pathList != null && pkgname.contains(pathList.getPathListName())) {
                    Log.d(TAG, "AdjustZenModeUtils : " + pathList.getPathListName());
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isZenModeOn() {
        return AUDIO_FEATURE_ZENMOD;
    }

    public boolean isDriveModeOn() {
        return this.mNotInterruptDuringDrive;
    }

    public boolean isMotorModeSupportPlayback(String pkgname) {
        if (this.mMotorModeWhiteList == null) {
            Log.e(TAG, "pkgname is" + pkgname + " motormodewhitelist has not been initialized!!");
            return true;
        }
        boolean result = true;
        if (this.mMotorModeEnabled) {
            result = false;
            for (int i = 0; i < this.mMotorModeWhiteList.size(); i++) {
                if (pkgname.equals(((AppList) this.mMotorModeWhiteList.get(i)).GetAppName())) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean is_in_VAPI_List(int pid, String api_name) {
        if (pid <= 0 || api_name == null || api_name.length() <= 0) {
            Log.w(TAG, "is_in_VAPI_List: pid:" + pid + " api_name:" + api_name);
            return false;
        } else if (this.mVAPILists == null) {
            Log.e(TAG, "is_in_VAPI_List: pid:" + pid + " api_name:" + api_name + "->is_in_VAPI_List:reutrn false since mVAPILists has  not be initialized");
            return false;
        } else {
            for (int i = 0; i < this.mVAPILists.size(); i++) {
                VAPIList tmp_api_list = (VAPIList) this.mVAPILists.get(i);
                if (VAPI_DEBUG) {
                    Log.d(TAG, "is_in_VAPI_List : " + tmp_api_list.get_api_name());
                }
                if (tmp_api_list != null && api_name.equals(tmp_api_list.get_api_name())) {
                    String pkg_name = getAppNameFromPid(pid);
                    if (VAPI_DEBUG) {
                        Log.d(TAG, "is_in_VAPI_List:api_name : " + tmp_api_list.get_api_name() + " calling pkg_name:" + pkg_name);
                    }
                    ArrayList<AppBlackWhiteList> applists = tmp_api_list.getAppLists();
                    int j = 0;
                    while (j < applists.size()) {
                        AppBlackWhiteList tmp_app_info = (AppBlackWhiteList) applists.get(j);
                        if (tmp_app_info == null || pkg_name == null || !pkg_name.equals(tmp_app_info.getAppName())) {
                            j++;
                        } else {
                            Log.d(TAG, "is_in_VAPI_List:return true. api_name : " + tmp_api_list.get_api_name() + " calling pkg_name:" + pkg_name);
                            return true;
                        }
                    }
                    continue;
                }
            }
            Log.w(TAG, "is_in_VAPI_List:return false. pid: " + pid + " api_name:" + api_name + " mVAPILists.size:" + this.mVAPILists.size());
            return false;
        }
    }

    public String getAppNameFromPid(int pID) {
        String processName = "";
        for (RunningAppProcessInfo pinfo : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            try {
                if (pinfo.pid == pID) {
                    processName = pinfo.processName;
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (processName == null || processName.length() <= 0) {
            Log.e(TAG, "getAppNameFromPid: " + pID + " find name fail!");
        }
        return processName;
    }

    public void PlaybackDetectionCallBack(String tag, String state, String packageName, int usage, String mPlayerAddr, PlayerIdCard pic) {
        if (tag != null) {
            if (!(tag.equals("") || state == null)) {
                if (!state.equals("")) {
                    TagParameters tpTemp = new TagParameters(tag);
                    tag = tpTemp.tag();
                    int duration = tpTemp.getInt(KEY_DURATION, -1);
                    if (tag.equals(TAG_FACE_DETECT)) {
                        float volume = tpTemp.getFloat(KEY_VOLUME, 1.0f);
                        synchronized (this.mAudioPlaybackList) {
                            if (state.equals(TAG_FACE_START)) {
                                this.mAudioPlaybackList.put(mPlayerAddr, new Playback(packageName, pic, volume));
                            } else {
                                if (state.equals(TAG_FACE_STOP)) {
                                    this.mAudioPlaybackList.remove(mPlayerAddr);
                                }
                            }
                        }
                        synchronized (this.mAudioFeatureCallbacks) {
                            if (this.mAudioFeatureCallbacks.containsKey(tag)) {
                                if (state.equals(TAG_FACE_STOP) || isSmartRingModeEnable()) {
                                    if (isUsageSupport(usage, packageName, state)) {
                                        int N = ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).beginBroadcast();
                                        if (N > 0) {
                                            int pos = N;
                                            while (pos > 0) {
                                                pos--;
                                                IAudioFeatureCallback callback = (IAudioFeatureCallback) ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).getBroadcastItem(pos);
                                                try {
                                                    TagParameters tp = new TagParameters(tag);
                                                    tp.put(KEY_STATE, state);
                                                    tp.put(KEY_STREAM, mPlayerAddr);
                                                    tp.put(KEY_COMPONENT, packageName);
                                                    tp.put(KEY_USAGE, usageToStream(usage));
                                                    tp.put(KEY_DURATION, duration);
                                                    synchronized (this.mAudioFaceDetectVolumeControl) {
                                                        if (state.equals(TAG_FACE_START)) {
                                                            this.mAudioFaceDetectVolumeControl.put(mPlayerAddr, pic);
                                                        } else {
                                                            if (state.equals(TAG_FACE_STOP)) {
                                                                this.mAudioFaceDetectVolumeControl.remove(mPlayerAddr);
                                                            }
                                                        }
                                                    }
                                                    Log.d(TAG, "PlaybackDetectionCallBack start#####");
                                                    callback.onCallback(tp.toString());
                                                } catch (RemoteException e) {
                                                    Log.w(TAG, "unregistered callback: " + callback + " " + e);
                                                    ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).unregister(callback);
                                                    N--;
                                                }
                                            }
                                        } else {
                                            Log.v(TAG, "NO remote callback for " + tag);
                                        }
                                        ((RemoteCallbackList) this.mAudioFeatureCallbacks.get(tag)).finishBroadcast();
                                        if (N <= 0) {
                                            Log.d(TAG, "empty map entry for " + tag);
                                            this.mAudioFeatureCallbacks.remove(tag);
                                        }
                                    } else {
                                        return;
                                    }
                                }
                            }
                            Log.w(TAG, "unkown tag " + tag);
                        }
                    }
                }
            }
        }
    }

    private boolean isUsageSupport(int mUsageForVM, String packageName, String state) {
        boolean ret = false;
        int stream = -1;
        if (mUsageForVM == 4 || mUsageForVM == 6) {
            if (isFaceDTSurpported(false, packageName)) {
                return false;
            }
            switch (mUsageForVM) {
                case 4:
                    stream = 4;
                    break;
                case 6:
                    stream = 2;
                    break;
            }
            int index = this.mStreamVolumeInfo[stream].getIndex(AudioSystem.getDevicesForStream(stream));
            if ((state.equals(TAG_FACE_START) && index != 0) || state.equals(TAG_FACE_STOP)) {
                ret = true;
            }
        }
        return ret;
    }

    public boolean isFaceDTSurpported(boolean state, String pkgname) {
        Log.d(TAG, "isFaceDTSurpported pkgname:" + pkgname);
        boolean flag = false;
        if (pkgname == null || pkgname.equals("")) {
            return false;
        }
        if (state) {
            for (Object equals : FACEDETECT_ALLOW_PACKGAGES) {
                if (pkgname.equals(equals)) {
                    flag = true;
                    break;
                }
            }
        } else {
            for (Object equals2 : FACEDETECT_FORBID_PACKGAGES) {
                if (pkgname.equals(equals2)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    public void PlaybackSetVolume(String mPlayerAddr, float volume) {
        PlayerIdCard pic;
        Log.d(TAG, "PlaybackSetVolume mPlayerAddr:" + mPlayerAddr + " volume:" + volume);
        synchronized (this.mAudioFaceDetectVolumeControl) {
            pic = (PlayerIdCard) this.mAudioFaceDetectVolumeControl.get(mPlayerAddr);
        }
        if (pic == null || (isSmartRingModeEnable() ^ 1) != 0) {
            Log.e(TAG, "PlaybackSetVolume can not SetVolume");
            return;
        }
        try {
            pic.mIPlayer.setVolume(volume);
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (this.mAudioFaceDetectVolumeControl) {
                this.mAudioFaceDetectVolumeControl.remove(mPlayerAddr);
            }
        }
    }

    public int usageToStream(int usage) {
        switch (usage) {
            case 4:
                return 4;
            case 6:
                return 2;
            default:
                return -1;
        }
    }

    public boolean isSmartRingModeEnable() {
        boolean SmartRingModeEnabled = System.getInt(this.mContext.getContentResolver(), "vivo_smart_ring_enable_setting", 0) == 1;
        boolean isSmartRingEnable = SystemProperties.get("persist.vivo.smartring.enable", "false").equals("true");
        boolean isChildMode = isChildMode(this.mContext);
        Log.d(TAG, "SmartRingModeEnabled:" + SmartRingModeEnabled + " isSmartRingEnable:" + isSmartRingEnable + " isChildMode:" + isChildMode);
        if (SmartRingModeEnabled && isSmartRingEnable) {
            return isChildMode ^ 1;
        }
        return false;
    }

    public boolean isChildMode(Context context) {
        return "true".equals(System.getString(context.getContentResolver(), "vivo_children_mode_enable"));
    }

    public void dumpFeaturesHook(PrintWriter pw) {
        pw.println("\nfeatures:");
        pw.print("dualmic:");
        pw.print(AUDIO_FEATURE_DUALMIC);
        pw.println(":" + AUDIO_FEATURE_DUALMIC_DEFAULT_VALUE);
        pw.print("dualspeaker:");
        pw.print(AUDIO_FEATURE_DUALSPKR);
        pw.println(":" + AUDIO_FEATURE_DUALSPK_DEFAULT_VALUE);
        pw.print("ktv:");
        pw.print(AUDIO_FEATURE_KTV);
        pw.println(":" + AUDIO_FEATURE_KTV_DEFAULT_VALUE);
        pw.print("spkboost:");
        pw.print(AUDIO_FEATURE_SPBOOST);
        pw.println(":" + AUDIO_FEATURE_SPBOOST_DEFAULT_VALUE);
        pw.print("hifi:");
        pw.print(AUDIO_FEATURE_HIFI);
        pw.println(":" + AUDIO_FEATURE_HIFI_DEFAULT_VALUE);
        pw.print("maxxaudio:");
        pw.print(AUDIO_FEATURE_MAXA);
        pw.println(":" + AUDIO_FEATURE_MAXA_DEFAULT_VALUE);
        pw.print("ModePOM:");
        pw.print(AUDIO_FEATURE_POM);
        pw.println(":" + AUDIO_FEATURE_MAXA_DEFAULT_VALUE);
        pw.print("bve:");
        pw.println(":" + AUDIO_FEATURE_BVE_DEFAULT_VALUE);
        pw.println("DailpadVol:" + AUDIO_SET_DAILPAD_VOL_DEFAULT_VALUE);
        pw.print("DSDHw:");
        pw.println(":" + AUDIO_FEATURE_DSDHW_DEFAULT_VALUE);
        pw.print("SafeVolumeIndex:");
        pw.println(":" + AUDIO_FEATURE_SAFEVOLUME_INDEX_VALUE);
        pw.print("MCVS:");
        pw.print(AUDIO_FEATURE_MCVS);
        pw.println(":" + AUDIO_FEATURE_MCVS_DEFAULT_VALUE);
        pw.print("HandsetSpkMode:");
        pw.println(":" + AUDIO_FEATURE_HANDSETSPKMODE);
        pw.println("MIC mute state:" + AudioSystem.isMicrophoneMuted());
    }
}
