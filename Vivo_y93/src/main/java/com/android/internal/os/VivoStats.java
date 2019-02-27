package com.android.internal.os;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.SubscriptionPlan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.DisplayInfo;
import com.android.internal.content.NativeLibraryHelper;
import java.util.HashMap;
import vivo.app.epm.ExceptionPolicyManager;

public final class VivoStats {
    private static final int ARGS_DISCONNECT_END = 1653;
    private static final int ARGS_GET_ALL_STATE = -123;
    private static final int ARGS_GET_CONFIG = 1654;
    private static final int ARGS_NOTE_ALL_STATE = 1655;
    private static final int ARGS_RECONNECT = 1652;
    private static final int ARGS_SET_VERSION = 1651;
    private static final int CONFIG_DEBUG = 49;
    private static boolean DEBUG = true;
    private static final int MSG_ACTIVITY = 14;
    private static final int MSG_ACTIVITY_PAUSED = 5;
    private static final int MSG_ACTIVITY_RESUMED = 4;
    private static final int MSG_ADD_WIN = 31;
    private static final int MSG_AP_STATE = 1;
    private static final int MSG_AUDIO_STATE = 12;
    private static final int MSG_BATTERY_LEVEL = 10;
    private static final int MSG_BATTERY_PLUG = 9;
    private static final int MSG_BRIGHTNESS = 2;
    private static final int MSG_DOCKED = 40;
    private static final int MSG_DOWNLOAD_STATE = 13;
    private static final int MSG_DROP_FRAME_APP = 10006;
    private static final int MSG_DROP_FRAME_GAME = 10004;
    private static final int MSG_GPS_STATE = 11;
    private static final int MSG_HAS_CONNECT = 10000;
    private static final int MSG_INPUT = 17;
    public static final int MSG_IS_VIEWDRAW = 10005;
    private static final int MSG_MAX = 50;
    private static final int MSG_MEDIA = 15;
    private static final int MSG_NOTE_GET_LIST = 19;
    private static final int MSG_PIP = 45;
    private static final int MSG_RECORD_STATE = 16;
    private static final int MSG_REMOTE_REINIT = 10001;
    private static final int MSG_REMOTE_STATE_INIT = 10002;
    private static final int MSG_REMOVE_UID = 3;
    private static final int MSG_REMOVE_WIN = 32;
    private static final int MSG_RESET = 0;
    private static final int MSG_SCREEN_STATE = 8;
    private static final int MSG_SET_GET = 10003;
    private static final int MSG_SHUTDOWN = 7;
    private static final int MSG_WIN_HIDE = 34;
    private static final int MSG_WIN_SHOW = 33;
    private static final int MSG_WRITE_DATA = 6;
    private static final String SERVICE_NAME = "vivostats";
    private static final String TAG = "VivoStats";
    private static final int TRANSACTION_GET_DISPLAY = 4;
    private static final int TRANSACTION_GET_LIST = 7;
    private static final int TRAN_CONFIG = 3;
    private static final int TRAN_NOTE = 1;
    private static final int TRAN_NOTE_ACTIVITY = 2;
    private static final int TRAN_NOTE_ACTIVITY_V5 = 8;
    private static final int TRAN_NOTE_WHICH = 9;
    private static final int TRAN_NOTE_WHICH_THREE = 11;
    private static final int TRAN_NOTE_WHICH_THREEINT = 12;
    private static final int TRAN_NOTE_WHICH_THREEINT_TWO = 13;
    private static final int TRAN_NOTE_WHICH_TWO = 10;
    private static final int WHICH_ACTIVITY = 48;
    private static final int WHICH_CLEAR_DDC_WHITELIST = 0;
    private static final int WHICH_CLEAR_VIEWDRAW_WHITELIST = 2;
    private static final int WHICH_GET_DDC_WHITELIST = 1;
    private static final int WHICH_GET_VIEWDRAW_WHITELIST = 112;
    private static final int WHICH_PID_ADD = 41;
    private static final int WHICH_PID_DEL = 42;
    private static VivoStats _instance;
    private static float curDensity = 640.0f;
    private static int densityVer = 0;
    private static boolean isNoScaled = false;
    private static String mActivity_name = "unknow";
    private static int mApState = -1;
    private static int mBrightness = -1;
    private static Context mContext;
    private static int mCool = -1;
    private static boolean mDocked = false;
    public static long mFocusNote = 0;
    private static NoteHandler mHandler = null;
    private static long mLastGameNoteMs;
    public static int mLastHashCore = 0;
    private static int mLastUid = -1;
    private static int mLevel = -1;
    private static boolean mPIP = false;
    private static String mPkgName = null;
    private static int mPlugType = -1;
    private static IBinder mRemote = null;
    private static SparseArray<UidCot> mResList = new SparseArray();
    private static boolean mReset = false;
    private static boolean mRestart = false;
    private static final HashMap<Integer, PidMsg> mScaledPids = new HashMap();
    private static final HashMap<String, PidMsg> mScaledProcessNames = new HashMap();
    private static int mStackId = 0;
    private static BatteryStatsImpl mStats;
    private static boolean mTState = false;
    private static int mTryConnect = 10;
    private static int mVStatsVersion = 10000;
    private static int mVersionCode = 0;
    private static SparseArray<ViewDrawConfig> mViewList = new SparseArray();
    private static final boolean[] run = new boolean[50];
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(VivoStats.TAG, "onServiceConnected name = " + name.toString());
            VivoStats.this.getConfig(service);
            VivoStats.mHandler.sendMessage(VivoStats.mHandler.obtainMessage(10000, service));
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(VivoStats.TAG, "onServiceDisconnected name = " + name.toString());
            VivoStats.mRemote = null;
            VivoStats.mTState = true;
            VivoStats.mHandler.sendEmptyMessageDelayed(10001, 2000);
        }
    };
    private final SparseArray<AppInfo> mAppInfo = new SparseArray();

    public final class AppInfo {
        public static final int A = 1;
        public static final int FA = 5;
        public static final int FS = 6;
        public static final int G = 2;
        public static final int M = 3;
        public static final int MAX = 7;
        public static final int RE = 4;
        public static final int U = 0;
        public int[] ss = new int[7];
    }

    final class NoteHandler extends Handler {
        public NoteHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            long now;
            ContentValues content;
            switch (msg.what) {
                case 14:
                    VivoStats.this.notePEM(msg.arg1, msg.arg2, (String) msg.obj);
                    return;
                case 19:
                    if (msg.arg1 == 0) {
                        if (VivoStats.mResList != null) {
                            VivoStats.mResList.clear();
                        }
                        VivoStats.densityVer = VivoStats.densityVer + 1;
                        Log.v(VivoStats.TAG, "stop update densityVer = " + VivoStats.densityVer);
                        return;
                    } else if (msg.arg1 == 1) {
                        VivoStats.this.getListRemote(1);
                        return;
                    } else if (msg.arg1 == 2) {
                        if (VivoStats.mViewList != null) {
                            VivoStats.mViewList.clear();
                            return;
                        }
                        return;
                    } else if (msg.arg1 == 112) {
                        VivoStats.this.getListRemote(112);
                        return;
                    } else {
                        return;
                    }
                case 10000:
                    VivoStats.mRemote = (IBinder) msg.obj;
                    if (VivoStats.mTState) {
                        VivoStats.this.noteTState();
                    }
                    VivoStats.this.noteAllState();
                    VivoStats.mTState = false;
                    return;
                case 10001:
                    VivoStats.this.connectPEM();
                    return;
                case VivoStats.MSG_SET_GET /*10003*/:
                    if (msg.arg1 == VivoStats.ARGS_RECONNECT) {
                        if (VivoStats.mRemote == null) {
                            VivoStats.mHandler.sendEmptyMessageDelayed(10001, 1000);
                            return;
                        }
                        VivoStats.this.getAllState();
                        VivoStats.mTState = true;
                        VivoStats.this.notePEM((int) VivoStats.MSG_SET_GET, (int) VivoStats.ARGS_RECONNECT, msg.arg2);
                        VivoStats.this.disconnectPEM();
                        return;
                    } else if (msg.arg1 == VivoStats.ARGS_GET_CONFIG) {
                        VivoStats.this.getConfig(VivoStats.mRemote);
                        return;
                    } else if (msg.arg1 == VivoStats.ARGS_NOTE_ALL_STATE) {
                        VivoStats.this.noteAllState();
                        return;
                    } else {
                        return;
                    }
                case VivoStats.MSG_DROP_FRAME_GAME /*10004*/:
                    now = SystemClock.elapsedRealtime();
                    long dropTime = now - VivoStats.mLastGameNoteMs;
                    if (VivoStats.mLastGameNoteMs != 0 && dropTime <= 10000) {
                        content = new ContentValues();
                        content.put("jank_type", Integer.valueOf(2));
                        content.put("uid", Integer.valueOf(msg.arg1));
                        content.put("drop_frames", Integer.valueOf(msg.arg2));
                        content.put("drop_time", Long.valueOf(dropTime));
                        ExceptionPolicyManager.getInstance().reportEvent(2001, now, content);
                    }
                    VivoStats.mLastGameNoteMs = now;
                    return;
                case VivoStats.MSG_DROP_FRAME_APP /*10006*/:
                    now = SystemClock.elapsedRealtime();
                    content = new ContentValues();
                    content.put("uid", Integer.valueOf(msg.arg1));
                    content.put("jank_type", Integer.valueOf(1));
                    content.put("drop_frames", Integer.valueOf(msg.arg2));
                    ExceptionPolicyManager.getInstance().reportEvent(2001, now, content);
                    return;
                default:
                    VivoStats.this.notePEM(msg.what, msg.arg1, msg.arg2);
                    if (VivoStats.mTState) {
                        VivoStats.this.toTState(msg.what, msg.arg1, msg.arg2);
                        return;
                    }
                    return;
            }
        }
    }

    public static final class PidMsg {
        public float scale = 1.0f;
        public UidCot uidcot = null;
    }

    public static final class UidCot {
        public int density;
        public String[] pkg = null;
        public int uid;
    }

    public static class ViewDrawConfig {
        public int uid;
        public int viewdraw = -1;
    }

    private static final class WhichMsgRun implements Runnable {
        public final int arg1;
        public final int arg2;
        public final String name;
        public final int which;

        public WhichMsgRun(int _which, int _arg1, int _arg2, String _name) {
            this.which = _which;
            this.arg1 = _arg1;
            this.arg2 = _arg2;
            this.name = _name;
        }

        public void run() {
            if (VivoStats.mRemote != null) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.vivo.pem.IPemr");
                    data.writeInt(this.which);
                    data.writeInt(this.arg1);
                    data.writeInt(this.arg2);
                    data.writeString(this.name);
                    VivoStats.mRemote.transact(9, data, null, 1);
                } catch (Exception e) {
                    Log.e(VivoStats.TAG, "notePEM:", e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    private static final class WhichMsgRunThree implements Runnable {
        public final int arg1;
        public final int arg2;
        public final int arg3;
        public final int which;

        public WhichMsgRunThree(int _which, int _arg1, int _arg2, int _arg3) {
            this.which = _which;
            this.arg1 = _arg1;
            this.arg2 = _arg2;
            this.arg3 = _arg3;
        }

        public void run() {
            if (VivoStats.mRemote != null) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.vivo.pem.IPemr");
                    data.writeInt(this.which);
                    data.writeInt(this.arg1);
                    data.writeInt(this.arg2);
                    data.writeInt(this.arg3);
                    VivoStats.mRemote.transact(11, data, null, 1);
                } catch (Exception e) {
                    Log.e(VivoStats.TAG, "notePEM:", e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    private static final class WhichMsgRunThreeInt implements Runnable {
        public final int arg1;
        public final int arg2;
        public final int arg3;
        public final String name;
        public final int which;

        public WhichMsgRunThreeInt(int _which, int _arg1, int _arg2, int _arg3, String _name) {
            this.which = _which;
            this.arg1 = _arg1;
            this.arg2 = _arg2;
            this.arg3 = _arg3;
            this.name = _name;
        }

        public void run() {
            if (VivoStats.mRemote != null) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.vivo.pem.IPemr");
                    data.writeInt(this.which);
                    data.writeInt(this.arg1);
                    data.writeInt(this.arg2);
                    data.writeInt(this.arg3);
                    data.writeString(this.name);
                    VivoStats.mRemote.transact(12, data, null, 1);
                } catch (Exception e) {
                    Log.e(VivoStats.TAG, "notePEM:", e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    private static final class WhichMsgRunThreeIntTwo implements Runnable {
        public final int arg1;
        public final int arg2;
        public final int arg3;
        public final String str1;
        public final String str2;
        public final int which;

        public WhichMsgRunThreeIntTwo(int _which, int _arg1, int _arg2, int _arg3, String _str1, String _str2) {
            this.which = _which;
            this.arg1 = _arg1;
            this.arg2 = _arg2;
            this.arg3 = _arg3;
            this.str1 = _str1;
            this.str2 = _str2;
        }

        public void run() {
            if (VivoStats.mRemote != null) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.vivo.pem.IPemr");
                    data.writeInt(this.which);
                    data.writeInt(this.arg1);
                    data.writeInt(this.arg2);
                    data.writeInt(this.arg3);
                    data.writeString(this.str1);
                    data.writeString(this.str2);
                    VivoStats.mRemote.transact(13, data, null, 1);
                } catch (Exception e) {
                    Log.e(VivoStats.TAG, "notePEM:", e);
                } finally {
                    data.recycle();
                }
                ContentValues content;
                if (this.which == 48) {
                    content = new ContentValues();
                    content.put("jank_type", Integer.valueOf(3));
                    content.put("uid", Integer.valueOf(this.arg1));
                    content.put("stack", Integer.valueOf(this.arg2));
                    content.put("cool", Integer.valueOf(this.arg3));
                    content.put("activity", this.str1);
                    content.put("package", this.str2);
                    ExceptionPolicyManager.getInstance().reportEvent(2001, SystemClock.elapsedRealtime(), content);
                } else if (this.which == 40) {
                    content = new ContentValues();
                    content.put("jank_type", Integer.valueOf(4));
                    content.put("exist", Integer.valueOf(this.arg1));
                    ExceptionPolicyManager.getInstance().reportEvent(2001, SystemClock.elapsedRealtime(), content);
                } else if (this.which == 45) {
                    content = new ContentValues();
                    content.put("jank_type", Integer.valueOf(5));
                    content.put("exist", Integer.valueOf(this.arg1));
                    ExceptionPolicyManager.getInstance().reportEvent(2001, SystemClock.elapsedRealtime(), content);
                }
            }
        }
    }

    private static final class WhichMsgRunTwo implements Runnable {
        public final int arg1;
        public final int arg2;
        public final String str1;
        public final String str2;
        public final int which;

        public WhichMsgRunTwo(int _which, int _arg1, int _arg2, String _str1, String _str2) {
            this.which = _which;
            this.arg1 = _arg1;
            this.arg2 = _arg2;
            this.str1 = _str1;
            this.str2 = _str2;
        }

        public void run() {
            if (VivoStats.mRemote != null) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.vivo.pem.IPemr");
                    data.writeInt(this.which);
                    data.writeInt(this.arg1);
                    data.writeInt(this.arg2);
                    data.writeString(this.str1);
                    data.writeString(this.str2);
                    VivoStats.mRemote.transact(10, data, null, 1);
                } catch (Exception e) {
                    Log.e(VivoStats.TAG, "notePEM:", e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    public static VivoStats getInstance() {
        if (_instance == null) {
            _instance = new VivoStats();
        }
        return _instance;
    }

    private VivoStats() {
    }

    public void publish(Context context, BatteryStatsImpl stats) {
        if (DEBUG) {
            Log.v(TAG, "VivoStats publish!");
        }
        mStats = stats;
        mContext = context;
        HandlerThread thread = new HandlerThread("vivo_stats");
        thread.start();
        boolean[] zArr = run;
        run[15] = true;
        run[11] = true;
        run[16] = true;
        zArr[12] = true;
        mHandler = new NoteHandler(thread.getLooper());
        curDensity = (float) DisplayMetrics.DENSITY_DEVICE;
    }

    public void noteAPState(int state) {
        if (DEBUG) {
            Log.v(TAG, "noteAPState state = " + state);
        }
        if (run[1]) {
            mHandler.sendMessage(mHandler.obtainMessage(1, state, 0));
        }
        mApState = state;
    }

    public static int note(int what, int arg1, int arg2) {
        if (DEBUG) {
            Log.v(TAG, "note what = " + what + ", arg1 = " + arg1 + ", arg2 = " + arg2);
        }
        if (what < 50) {
            if (run[what]) {
                mHandler.sendMessage(mHandler.obtainMessage(what, arg1, arg2));
            }
            return 0;
        } else if (what == MSG_IS_VIEWDRAW) {
            int uid = arg2;
            if (isViewDrawWhitelist(arg2)) {
                return 1;
            }
            return 0;
        } else if (what == 10001) {
            mHandler.sendEmptyMessage(10001);
            return mVStatsVersion;
        } else {
            if (what == MSG_SET_GET) {
                if (arg1 == ARGS_SET_VERSION) {
                    int versionCode = mVersionCode;
                    mVersionCode = arg2;
                    if (mRemote == null) {
                        versionCode = -1;
                    }
                    return versionCode;
                } else if (arg1 == ARGS_RECONNECT || arg1 == ARGS_GET_CONFIG || arg1 == ARGS_NOTE_ALL_STATE) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_GET, arg1, arg2));
                } else if (arg1 == ARGS_DISCONNECT_END) {
                    mHandler.sendEmptyMessageDelayed(10001, 10000);
                }
            } else if (what == MSG_DROP_FRAME_APP || what == MSG_DROP_FRAME_GAME) {
                mHandler.sendMessage(mHandler.obtainMessage(what, arg1, arg2));
            }
            return 123;
        }
    }

    public void noteVideo(int uid, int state) {
        if (DEBUG) {
            Log.v(TAG, "noteVideo state = " + state + ", uid = " + uid);
        }
        if (run[15]) {
            mHandler.sendMessage(mHandler.obtainMessage(15, state, uid));
        }
    }

    public void noteAudio(int uid, int state) {
        if (DEBUG) {
            Log.v(TAG, "noteAudio state = " + state + ", uid = " + uid);
        }
        if (run[12]) {
            mHandler.sendMessage(mHandler.obtainMessage(12, state, uid));
        }
    }

    public void noteResetVideo() {
        if (DEBUG) {
            Log.v(TAG, "noteResetVideo");
        }
        if (run[15]) {
            mHandler.sendMessage(mHandler.obtainMessage(15, 1000, 0));
        }
    }

    public void noteResetAudio() {
        if (DEBUG) {
            Log.v(TAG, "noteResetAudio");
        }
        if (run[12]) {
            mHandler.sendMessage(mHandler.obtainMessage(12, 1000, 0));
        }
    }

    public void noteScreenState(int state, int oldState) {
        if (DEBUG) {
            Log.v(TAG, "noteScreenState state = " + state + ", oldState = " + oldState);
        }
        if (run[8]) {
            mHandler.sendMessage(mHandler.obtainMessage(8, state, oldState));
        }
    }

    public void noteScreenBrightness(int brightness) {
        if (DEBUG) {
            Log.v(TAG, "noteScreenBrightness brightness = " + brightness);
        }
        if (run[2]) {
            mHandler.sendMessage(mHandler.obtainMessage(2, brightness, 0));
        }
        mBrightness = brightness;
    }

    public void noteRemoveUid(int uid) {
        if (DEBUG) {
            Log.v(TAG, "noteRemoveUid uid = " + uid);
        }
        if (run[3]) {
            mHandler.sendMessage(mHandler.obtainMessage(3, uid, 0));
        }
    }

    public void noteGpsState(int start, int uid) {
        if (DEBUG) {
            Log.v(TAG, "noteGpsState start = " + start + ", uid = " + uid);
        }
        if (run[11]) {
            mHandler.sendMessage(mHandler.obtainMessage(11, start, uid));
        }
    }

    public void noteBatteryState(int plugType, int level) {
        if (mPlugType != plugType) {
            mPlugType = plugType;
            if (run[9]) {
                mHandler.sendMessage(mHandler.obtainMessage(9, plugType, level));
            }
            if (DEBUG) {
                Log.v(TAG, "noteBatteryState plugType = " + plugType + ", level = " + level);
            }
        }
        if (mLevel != level) {
            mLevel = level;
            if (run[10]) {
                mHandler.sendMessage(mHandler.obtainMessage(10, plugType, level));
            }
            if (DEBUG) {
                Log.v(TAG, "noteBatteryState plugType = " + plugType + ", level = " + level);
            }
        }
    }

    public void noteShutdown() {
        if (DEBUG) {
            Log.v(TAG, "noteShutdown");
        }
        if (run[7]) {
            mHandler.sendMessage(mHandler.obtainMessage(7, 0, 0));
        }
    }

    boolean noteReset(boolean detachIfReset) {
        if (DEBUG) {
            Log.v(TAG, "reset detachIfReset = " + detachIfReset);
        }
        if (run[0]) {
            int i;
            NoteHandler noteHandler = mHandler;
            NoteHandler noteHandler2 = mHandler;
            if (detachIfReset) {
                i = 1;
            } else {
                i = 0;
            }
            noteHandler.sendMessage(noteHandler2.obtainMessage(0, i, 0));
        }
        if (mRemote == null) {
            mReset = true;
        }
        return true;
    }

    public static void noteActivityResumedLocked(int uid, int cool, String activityName) {
        mActivity_name = activityName;
        mCool = cool;
        if (mLastUid != uid) {
            if (DEBUG) {
                Log.v(TAG, "noteActivityResumedLocked uid = " + uid + ", mLastUid = " + mLastUid);
            }
            mLastUid = uid;
            if (run[4]) {
                mHandler.sendMessage(mHandler.obtainMessage(4, uid, 0));
            }
        }
        if (run[14]) {
            mHandler.sendMessage(mHandler.obtainMessage(14, uid, cool, activityName));
        }
    }

    public static void noteActivityPausedLocked(int uid) {
        if (DEBUG) {
            Log.v(TAG, "noteActivityPausedLocked uid = " + uid);
        }
        if (run[5]) {
            mHandler.sendMessage(mHandler.obtainMessage(5, uid, 0));
        }
    }

    void noteWriteSummary() {
        if (mHandler != null && run[6]) {
            mHandler.sendMessage(mHandler.obtainMessage(6, 0, 0));
        }
    }

    void noteReadSummary() {
        mRestart = true;
    }

    private void disconnectPEM() {
        Log.d(TAG, "disconnectPEM");
        try {
            mContext.unbindService(this.conn);
            mRemote = null;
            mTryConnect = 10;
        } catch (Exception e) {
            Log.d(TAG, "unbindService:", e);
        }
    }

    private void connectPEM() {
        Log.d(TAG, "connectPEM, mTryConnect = " + mTryConnect);
        mTryConnect--;
        if (mTryConnect >= 0) {
            try {
                Intent intent = new Intent("com.vivo.pem.PemService");
                intent.setPackage("com.vivo.pem");
                boolean result = mContext.bindService(intent, this.conn, 1);
                Log.d(TAG, "result = " + result);
                if (!result) {
                    mHandler.sendEmptyMessageDelayed(10001, 3000);
                }
            } catch (Exception e) {
                Log.d(TAG, "bindService:", e);
                mHandler.sendEmptyMessageDelayed(10001, 3000);
            }
        }
    }

    private void noteTState() {
        for (int i = 0; i < this.mAppInfo.size(); i++) {
            AppInfo app = (AppInfo) this.mAppInfo.valueAt(i);
            if (app.ss[1] > 0) {
                notePEM(12, app.ss[1], app.ss[0]);
            }
            if (app.ss[4] > 0) {
                notePEM(16, app.ss[4], app.ss[0]);
            }
            if (app.ss[3] > 0) {
                notePEM(15, app.ss[3], app.ss[0]);
            }
            if (app.ss[2] > 0) {
                notePEM(11, app.ss[2], app.ss[0]);
            }
            if (app.ss[5] > 0) {
                notePEM(31, app.ss[5], app.ss[0]);
            }
            if (app.ss[6] > 0) {
                notePEM(33, app.ss[6], app.ss[0]);
            }
        }
        this.mAppInfo.clear();
    }

    private void noteAllState() {
        if (mPlugType >= 0) {
            notePEM(10002, 9, mPlugType);
        }
        if (mLevel >= 0) {
            notePEM(10002, 10, mLevel);
        }
        notePEM(10002, 8, mStats.isScreenOn(mStats.getScreenState()) ? 1 : 0);
        if (mBrightness > 0) {
            notePEM(10002, 2, mBrightness);
        }
        if (mReset) {
            notePEM(10002, 0, 0);
            mReset = false;
        }
        if (mApState >= 0) {
            notePEM(10002, 1, mApState);
        }
        if (mPkgName != null) {
            new WhichMsgRunTwo(48, mLastUid, mStackId, mActivity_name, mPkgName).run();
            if (mPIP) {
                new WhichMsgRunThreeIntTwo(45, 1, mStackId, mLastUid, mActivity_name, mPkgName).run();
            }
            if (mDocked) {
                new WhichMsgRunThreeIntTwo(40, 1, mStackId, mLastUid, mActivity_name, mPkgName).run();
                return;
            }
            return;
        }
        if (mLastUid >= 0) {
            notePEM(mLastUid, mCool, mActivity_name);
        }
        if (mCool > 1 && mLastUid >= 0) {
            for (int i = 0; i < mCool; i++) {
                notePEM(mLastUid, mCool, mActivity_name);
            }
        }
        if (mPIP) {
            note(45, 1, mLastUid);
        }
        if (mDocked) {
            note(40, 1, mLastUid);
        }
    }

    private boolean getConfig(IBinder remote) {
        boolean z = true;
        int[] args = new int[50];
        boolean result = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.vivo.pem.IPemr");
            if (args == null) {
                data.writeInt(-1);
            } else {
                data.writeInt(args.length);
            }
            remote.transact(3, data, reply, 0);
            reply.readException();
            result = reply.readInt() != 0;
            if (result) {
                reply.readIntArray(args);
                for (int i = 0; i < args.length; i++) {
                    boolean z2;
                    boolean[] zArr = run;
                    if (args[i] > 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    zArr[i] = z2;
                    Log.d(TAG, "run[" + i + "] = " + run[i]);
                }
                if (args[49] <= 0) {
                    z = false;
                }
                DEBUG = z;
            }
            reply.recycle();
            data.recycle();
        } catch (Exception e) {
            Log.e(TAG, "getConfig:", e);
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        return result;
    }

    private void toTStateSg(int arg1, int arg2, int idx) {
        if (arg2 > 1000) {
            AppInfo app = (AppInfo) this.mAppInfo.get(arg2);
            int[] iArr;
            if (arg1 > 0) {
                if (app == null) {
                    app = new AppInfo();
                    app.ss[0] = arg2;
                    this.mAppInfo.put(arg2, app);
                }
                iArr = app.ss;
                iArr[idx] = iArr[idx] + 1;
            } else if (app != null && app.ss[idx] > 0) {
                iArr = app.ss;
                iArr[idx] = iArr[idx] - 1;
            }
        }
    }

    private void toTState(int what, int arg1, int arg2) {
        if (what == 16) {
            toTStateSg(arg1, arg2, 4);
        }
        if (what == 15) {
            toTStateSg(arg1, arg2, 3);
        }
        if (what == 11) {
            toTStateSg(arg1, arg2, 2);
        }
        if (what == 31) {
            toTStateSg(arg1, arg2, 5);
        }
        if (what == 33) {
            toTStateSg(arg1, arg2, 6);
        }
        if (what == 12) {
            if (arg1 > 100) {
                for (int i = 0; i < this.mAppInfo.size(); i++) {
                    AppInfo app = (AppInfo) this.mAppInfo.valueAt(i);
                    int[] iArr = app.ss;
                    app.ss[4] = 0;
                    app.ss[1] = 0;
                    iArr[3] = 0;
                }
                return;
            }
            toTStateSg(arg1, arg2, 1);
        }
    }

    private void getAllState() {
        int[] aUid = new int[]{36};
        long[] gpuT = new long[aUid[0]];
        long[] screenT = new long[aUid[0]];
        double[] gpuP = new double[aUid[0]];
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("com.vivo.pem.IPemr");
            _data.writeInt(ARGS_GET_ALL_STATE);
            _data.writeIntArray(aUid);
            _data.writeInt(gpuT.length);
            _data.writeInt(screenT.length);
            _data.writeInt(gpuP.length);
            _data.writeInt(-1);
            mRemote.transact(4, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
            }
            _reply.readLongArray(gpuT);
            _reply.readLongArray(screenT);
            _reply.readDoubleArray(gpuP);
            this.mAppInfo.clear();
            int i = 0;
            while (i < aUid[0] && gpuT[i] > 0) {
                AppInfo app = new AppInfo();
                app.ss[0] = (int) gpuT[i];
                app.ss[1] = ((int) screenT[i]) & 255;
                screenT[i] = screenT[i] / 256;
                app.ss[4] = ((int) screenT[i]) & 255;
                screenT[i] = screenT[i] / 256;
                app.ss[2] = ((int) screenT[i]) & 255;
                screenT[i] = screenT[i] / 256;
                app.ss[3] = ((int) screenT[i]) & 255;
                screenT[i] = screenT[i] / 256;
                int tmp = (int) gpuP[i];
                app.ss[5] = tmp & 255;
                screenT[i] = screenT[i] / 256;
                app.ss[6] = tmp & 255;
                this.mAppInfo.put(app.ss[0], app);
                if (DEBUG) {
                    Log.v(TAG, "getAllState, uid = " + app.ss[0] + ", audio = " + app.ss[1] + ", record = " + app.ss[4] + ", gps = " + app.ss[2] + ", media = " + app.ss[3]);
                }
                i++;
            }
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            Log.e(TAG, "getAllState:", e);
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    private void notePEM(int what, int arg1, int arg2) {
        if (mRemote != null) {
            Parcel data = Parcel.obtain();
            try {
                data.writeInterfaceToken("com.vivo.pem.IPemr");
                data.writeInt(what);
                data.writeInt(arg1);
                data.writeInt(arg2);
                mRemote.transact(1, data, null, 1);
            } catch (Exception e) {
                Log.e(TAG, "notePEM:", e);
            } finally {
                data.recycle();
            }
        }
    }

    private void notePEM(int uid, int cool, String name) {
        if (mRemote != null) {
            Parcel data = Parcel.obtain();
            try {
                data.writeInterfaceToken("com.vivo.pem.IPemr");
                data.writeInt(uid);
                data.writeInt(cool);
                data.writeString(name);
                mRemote.transact(8, data, null, 1);
            } catch (Exception e) {
                Log.e(TAG, "notePEM:", e);
            } finally {
                data.recycle();
            }
        }
    }

    public static void noteWhich(int which, int arg1, int arg2, String name) {
        if (DEBUG) {
            Log.v(TAG, "noteWhich " + which + ", arg1 = " + arg1 + ", arg2 = " + arg2 + ", name = " + name);
        }
        if (run[which]) {
            mHandler.post(new WhichMsgRun(which, arg1, arg2, name));
        }
    }

    public static void noteWhich(int which, int arg1, int arg2, String str1, String str2) {
        if (DEBUG) {
            Log.v(TAG, "noteWhich " + which + ", arg1 = " + arg1 + ", arg2 = " + arg2 + ", str1 = " + str1 + ", str2 = " + str2);
        }
        if (run[which]) {
            mHandler.post(new WhichMsgRunTwo(which, arg1, arg2, str1, str2));
        }
    }

    public static int noteWhich(int which, int arg1, int arg2, int arg3) {
        if (DEBUG) {
            Log.v(TAG, "noteWhich " + which + ", arg1 = " + arg1 + ", arg2 = " + arg2 + ", arg3 = " + arg3);
        }
        if (run[which]) {
            mHandler.post(new WhichMsgRunThree(which, arg1, arg2, arg3));
        }
        return 123;
    }

    public static void noteWhich(int which, int arg1, int arg2, int arg3, String name) {
        if (DEBUG) {
            Log.v(TAG, "noteWhich " + which + ", arg1 = " + arg1 + ", arg2 = " + arg2 + ", arg3 = " + arg3 + ", name = " + name);
        }
        if (run[which]) {
            mHandler.post(new WhichMsgRunThreeInt(which, arg1, arg2, arg3, name));
        }
    }

    public static void noteWhich(int which, int arg1, int arg2, int arg3, String str1, String str2) {
        if (DEBUG) {
            Log.v(TAG, "noteWhich " + which + ", arg1 = " + arg1 + ", arg2 = " + arg2 + ", arg3 = " + arg3 + ", str1 = " + str1 + ", str2 = " + str2);
        }
        if (run[which]) {
            mHandler.post(new WhichMsgRunThreeIntTwo(which, arg1, arg2, arg3, str1, str2));
        }
    }

    public static void noteWhichActivity(int uid, String activity, String pkg, String action, String type, int stackId, boolean cool) {
        if (action != null && action.startsWith("APAP-")) {
            String[] praseAction = action.split(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            if (praseAction.length > 2) {
                stackId += 10000;
                Log.v(TAG, "for pem: action " + action);
                pkg = praseAction[1] + "-pem-" + praseAction[2];
            }
        }
        if (type != null && stackId < 10000) {
            String[] praseType = type.split("/");
            if (praseType.length > 1) {
                stackId += 10000;
                Log.v(TAG, "for pem: type " + type);
                pkg = praseType[0] + "-pem-" + praseType[1];
            }
        }
        if (run[48]) {
            mHandler.post(new WhichMsgRunThreeIntTwo(48, uid, stackId, cool ? 1 : 0, activity, pkg));
        }
        if (DEBUG) {
            Log.v(TAG, "noteWhichActivity uid = " + uid + ", stackId = " + stackId + ", activity = " + activity + ", pkg = " + pkg);
        }
        mLastUid = uid;
        mPkgName = pkg;
        mStackId = stackId;
        mActivity_name = activity;
    }

    public static boolean checkIsReset(int level, boolean reset) {
        if ("rom_2.5".compareTo(SystemProperties.get("ro.vivo.rom.version", "rom_2.0")) <= 0) {
            reset = false;
            if (level >= 100 || (mRestart && level >= 99)) {
                reset = true;
            }
            mRestart = false;
        }
        return reset;
    }

    private void doGetList(Parcel reply) {
        SparseArray<UidCot> resList = new SparseArray();
        try {
            int NU = reply.readInt();
            for (int i = 0; i < NU; i++) {
                UidCot u = new UidCot();
                u.uid = reply.readInt();
                u.density = reply.readInt();
                u.pkg = reply.createStringArray();
                resList.put(u.uid, u);
            }
            SparseArray<UidCot> tmp = mResList;
            mResList = resList;
            densityVer++;
            tmp.clear();
            Log.v(TAG, "doGetList update densityVer = " + densityVer);
        } catch (Exception e) {
            Log.e(TAG, "doGetList error:", e);
        }
    }

    private void doGetViewList(Parcel reply) {
        SparseArray<ViewDrawConfig> resList = new SparseArray();
        try {
            int NU = reply.readInt();
            for (int i = 0; i < NU; i++) {
                ViewDrawConfig u = new ViewDrawConfig();
                u.uid = reply.readInt();
                u.viewdraw = reply.readInt();
                resList.put(u.uid, u);
            }
            SparseArray<ViewDrawConfig> tmp = mViewList;
            mViewList = resList;
            tmp.clear();
            Log.v(TAG, "doGetViewList update");
        } catch (Exception e) {
            Log.e(TAG, "doGetViewList error:", e);
        }
    }

    public void getListRemote(int which) {
        if (mRemote == null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(19, which, which), 3000);
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("com.vivo.pem.IPemr");
            _data.writeInt(which);
            mRemote.transact(7, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                if (which == 1) {
                    doGetList(_reply);
                } else if (which == 112) {
                    doGetViewList(_reply);
                }
            }
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            Log.e(TAG, "getListRemote error:", e);
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    public static float obtainScaleByProcessName(String processName, int uid, int pid, String pkg) {
        if (isNoScaled) {
            return 1.0f;
        }
        try {
            UidCot u = (UidCot) mResList.get(uid);
            if (u != null) {
                if (u.pkg != null) {
                    if (pkg == null) {
                        return 1.0f;
                    }
                    int len = u.pkg.length - 1;
                    int i = 0;
                    while (i <= len && !pkg.equals(u.pkg[i])) {
                        if (i == len) {
                            return 1.0f;
                        }
                        i++;
                    }
                }
                PidMsg pmsg = new PidMsg();
                pmsg.scale = ((float) u.density) / curDensity;
                pmsg.uidcot = u;
                mScaledPids.put(Integer.valueOf(pid), pmsg);
                mScaledProcessNames.put(processName, pmsg);
                Log.v(TAG, processName + " is will be scale to " + pmsg.scale + ", uid = " + uid + ", pid = " + pid + ", pkg = " + pkg);
                return pmsg.scale;
            }
        } catch (Exception e) {
            Log.e(TAG, "isPackageExist error on " + processName + ", uid = " + uid + ", pid = " + pid + ", pkg = " + pkg, e);
        }
        return 1.0f;
    }

    public static float getScaleByPidAndName(Integer pid, CharSequence title) {
        PidMsg pmsg = (PidMsg) mScaledPids.get(pid);
        if (pmsg != null) {
            return pmsg.scale;
        }
        return 1.0f;
    }

    public static float getScaleByPid(Integer pid) {
        PidMsg pmsg = (PidMsg) mScaledPids.get(pid);
        if (pmsg != null) {
            return pmsg.scale;
        }
        return 1.0f;
    }

    public static float getScaleByName(String processName) {
        PidMsg pmsg = (PidMsg) mScaledProcessNames.get(processName);
        if (pmsg != null) {
            return pmsg.scale;
        }
        return 1.0f;
    }

    public static boolean isScaled(int pid) {
        return mScaledPids.containsKey(Integer.valueOf(pid));
    }

    public static boolean isScaled(String processName) {
        return mScaledProcessNames.containsKey(processName);
    }

    public static void setDisableScaled(boolean disable) {
        isNoScaled = disable;
    }

    public static void removeScaledProcess(String processName, int pid) {
        if (processName != null && mScaledProcessNames.containsKey(processName)) {
            try {
                mScaledPids.remove(Integer.valueOf(pid));
                mScaledProcessNames.remove(processName);
                Log.v(TAG, "removeScaledPid pid = " + pid + ", name = " + processName);
            } catch (Exception e) {
                Log.e(TAG, "removeScaledPid error on " + processName + ", pid = " + pid, e);
            }
        }
    }

    public static void updateConfigIfScaled(Configuration cfg, String name) {
        if (name != null && cfg != null) {
            PidMsg pmsg = (PidMsg) mScaledProcessNames.get(name);
            if (pmsg != null) {
                cfg.densityDpi = (int) ((pmsg.scale * ((float) cfg.densityDpi)) + 0.5f);
                if (cfg.appBounds != null) {
                    cfg.appBounds.scale(pmsg.scale);
                }
            }
        }
    }

    public static Configuration obtainNewConfigIfScaled(Configuration config, String name) {
        if (name == null || config == null) {
            return config;
        }
        PidMsg pmsg = (PidMsg) mScaledProcessNames.get(name);
        if (pmsg == null) {
            return config;
        }
        Configuration newConfig = new Configuration(config);
        newConfig.densityDpi = (int) ((pmsg.scale * ((float) newConfig.densityDpi)) + 0.5f);
        if (newConfig.appBounds != null) {
            newConfig.appBounds.scale(pmsg.scale);
        }
        return newConfig;
    }

    public static DisplayInfo obtainNewDisplayInfoIfScaled(DisplayInfo info, int pid) {
        PidMsg pmsg = (PidMsg) mScaledPids.get(Integer.valueOf(pid));
        if (pmsg == null) {
            return info;
        }
        DisplayInfo newInfo = new DisplayInfo(info);
        newInfo.logicalDensityDpi = (int) ((pmsg.scale * ((float) newInfo.logicalDensityDpi)) + 0.5f);
        newInfo.physicalXDpi = (float) ((int) ((pmsg.scale * newInfo.physicalXDpi) + 0.5f));
        newInfo.physicalYDpi = (float) ((int) ((pmsg.scale * newInfo.physicalYDpi) + 0.5f));
        newInfo.logicalHeight = (int) ((pmsg.scale * ((float) newInfo.logicalHeight)) + 0.5f);
        newInfo.logicalWidth = (int) ((pmsg.scale * ((float) newInfo.logicalWidth)) + 0.5f);
        newInfo.appHeight = (int) ((pmsg.scale * ((float) newInfo.appHeight)) + 0.5f);
        newInfo.appWidth = (int) ((pmsg.scale * ((float) newInfo.appWidth)) + 0.5f);
        newInfo.largestNominalAppHeight = (int) ((pmsg.scale * ((float) newInfo.largestNominalAppHeight)) + 0.5f);
        newInfo.largestNominalAppWidth = (int) ((pmsg.scale * ((float) newInfo.largestNominalAppWidth)) + 0.5f);
        newInfo.smallestNominalAppWidth = (int) ((pmsg.scale * ((float) newInfo.smallestNominalAppWidth)) + 0.5f);
        newInfo.smallestNominalAppHeight = (int) ((pmsg.scale * ((float) newInfo.smallestNominalAppHeight)) + 0.5f);
        return newInfo;
    }

    public static void setDocked(boolean exist) {
        if (mDocked != exist) {
            Log.v(TAG, "noteDockedState exist = " + exist + ", mLastUid = " + mLastUid);
            note(40, exist ? 1 : 0, mLastUid);
            mDocked = exist;
        }
    }

    public static void setPIP(boolean exist) {
        if (mPIP != exist) {
            Log.v(TAG, "notePIPState exist = " + exist + ", mLastUid = " + mLastUid);
            note(45, exist ? 1 : 0, mLastUid);
            mPIP = exist;
        }
    }

    public static void setWhichDocked(boolean exist) {
        if (mDocked != exist) {
            Log.v(TAG, "noteWhichDockedState exist = " + exist + ", mStackId = " + mStackId + ", mLastUid = " + mLastUid + ", pkg = " + mPkgName + ", activity = " + mActivity_name);
            noteWhich(40, exist ? 1 : 0, mStackId, mLastUid, mActivity_name, mPkgName);
            mDocked = exist;
            mFocusNote = exist ? SubscriptionPlan.BYTES_UNLIMITED : 1;
        }
    }

    public static void setWhichPIP(boolean exist) {
        if (mPIP != exist) {
            Log.v(TAG, "noteWhichPIPState exist = " + exist + ", mStackId = " + mStackId + ", mLastUid = " + mLastUid + ", pkg = " + mPkgName + ", activity = " + mActivity_name);
            noteWhich(45, exist ? 1 : 0, mStackId, mLastUid, mActivity_name, mPkgName);
            mPIP = exist;
            mFocusNote = exist ? SubscriptionPlan.BYTES_UNLIMITED : 1;
        }
    }

    public static boolean isViewDrawWhitelist(int uid) {
        if (mViewList == null || ((ViewDrawConfig) mViewList.get(uid)) == null) {
            return false;
        }
        return true;
    }
}
