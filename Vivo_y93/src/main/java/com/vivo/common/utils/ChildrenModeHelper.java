package com.vivo.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import com.vivo.internal.R;
import com.vivo.services.cipher.utils.Contants;
import com.vivo.services.security.client.VivoPermissionManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ChildrenModeHelper extends BroadcastReceiver {
    private static final String ACTION_CHILDREN_MODE_FULL_SCREEN_ASSERT = "com.vivo.childrenmode_action_full_screen_assert";
    private static final String ACTION_CHILDREN_MODE_START = "com.vivo.childrenmode_action_app_start";
    private static final int AP_INDICATE_ADDED = 2;
    private static final int AP_INDICATE_SYSTEM_APP_WHITE = 1;
    private static final int AP_INDICATE_THIRD_APP_BLACK = 0;
    private static final String CHILDRENMODE_AP_INDICATE = "ap_indicate";
    private static final String CHILDRENMODE_CLASS_NAME = "main_class";
    private static final String CHILDRENMODE_PACKAGE_NAME = "pack_name";
    private static final String CHILDRENMODE_TITLE_NAME = "ap_name";
    private static final Uri CHILDREN_MODE_URI = Uri.parse("content://com.vivo.mod.child/app");
    private static final String EXTRA_IS_BAD_POSTURE = "com.vivo.childrenmode_bad_posture";
    private static final String EXTRA_IS_START = "com.vivo.childrenmode_start";
    private static final String EXTRA_IS_TIME_OUT = "com.vivo.childrenmode_time_out";
    private static final String FLAG_ALL_CLASS = ".*";
    private static final String FLAG_CHILDREN_MODE_ENABLE = "vivo_children_mode_enable";
    private static final String PATH_CHILDREN_MODE_WHITE_LIST_TEMP = "/data/bbkcore/com.vivo.childrenmode_children_mode_white_list_temp.xml";
    private static final String PATH_DEFAULT_CONFIG = "/data/bbkcore/com.vivo.childrenmode_default_config_list.xml";
    private static final String TAG = "ChildrenModeHelper";
    private static final String TAG_BLACK_ITEM = "black_item";
    private static final String TAG_CHILDREN_MODE_WHITE_LIST_ITEM = "children_mode_white_item";
    private static final String TAG_WHITE_ITEM = "white_item";
    public static final int TYPE_NEED_INFO = 0;
    public static final int TYPE_NOT_NEED_INFO = 1;
    private static ChildrenModeHelper sInstance = null;
    private static boolean sLogEnable;
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final HandlerThread sWorkerThread = new HandlerThread("children-mode-loader", 10);
    private boolean mChildrenModeBadPosture = false;
    private boolean mChildrenModeEnable = false;
    private ContentObserver mChildrenModeObserver = new ContentObserver(sWorker) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ChildrenModeHelper.this.checkChildrenMode(ChildrenModeHelper.this.mContext);
        }
    };
    private boolean mChildrenModeStart = false;
    private boolean mChildrenModeTimeout = false;
    private ArrayList<String> mChildrenModeWhiteList = new ArrayList();
    private ChildrenModeContentObserver mContentObserver = new ChildrenModeContentObserver(sWorker);
    private Context mContext;
    private ArrayList<String> mDefaultBlackList = new ArrayList();
    private String[] mDefaultBlackListConfig = new String[]{"com.alipay.sdk.app.H5PayActivity", "com.alipay.android.app.flybird.ui.window.FlyBirdWindowActivity", "com.alipay.android.app.ui.quickpay.window.MiniPayActivity", "com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI", "cooperation.qwallet.plugin.QWalletPluginProxyActivity", "com.bbk.payment.PaymentActivity", "com.bbk.payment.PaymentRechargeActivity", "com.bbkmobile.iqoo.payment.PaymentActivity", "com.android.dialer.TwelveKeyDialer", "com.android.dialer.BBKTwelveKeyDialer", "com.sogou.sledog.message.prompt.SmsPromptActivity"};
    private DefaultConfigObserver mDefaultConfigObserver = new DefaultConfigObserver(PATH_DEFAULT_CONFIG);
    private String[] mDefaultWhiteListConfig = new String[]{"com.bbk.calendar/.alerts.AlertActivity", "com.android.BBKClock/.AlertClock.AlarmAlertFullScreen", "com.android.BBKClock/.Timers.TimerAlert", "com.android.BBKClock/.Timers.FullScreenAlert", "com.android.bluetoothsettings/.bluetooth.BluetoothPairingDialog", "com.android.bluetooth/.opp.BluetoothOppIncomingFileConfirmActivity", "com.android.incallui/.BaseCallActivity", "com.android.incallui/.InCallActivity", "com.android.incallui/.HolsterCallActivity", "com.android.incallui/.ToCallScreenActivity", "android/com.android.internal.app.ChooserActivity", "android/com.android.internal.app.ShutdownActivity", "android/com.android.internal.app.ResolverActivity", "android/com.android.internal.app.DoubleAppResolverActivity", "com.vivo.gallery/.*", "com.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity", "com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity", "com.android.settings/com.android.settings.Settings$AppWriteSettingsActivity", "com.android.settings/.ChooseLockGeneric", "com.android.settings/.ChooseLockPassword", "com.android.settings/com.android.settings.password.ChooseLockPassword", "com.android.settings/com.android.settings.password.ChooseLockGeneric", "com.android.settings/.ConfirmTipProblem", "com.android.BBKCrontab/com.android.BBKCrontab.schpwronoff.AlarmAlert", "com.vivo.numbermark/com.vivo.numbermark.NumberTagOnlineActivity", "com.android.stk/com.android.stk.StkDialogActivity", "com.android.systemui/com.android.systemui.vivo.common.keyguard.security.VivoConfirmActivity", "com.android.systemui/com.android.keyguard.vivo.VivoConfirmActivity", "com.vivo.securedaemonservice/com.iqoo.secure.safeguard.PasswordActivity", "com.android.settings/com.vivo.settings.secret.PasswordActivity", "com.android.settings/com.vivo.settings.secret.PasswordActivityUD", "com.vivo.sdkplugin/.*", "com.vivo.childrenmode/.*", "com.android.incallui/.EmergencyDialer", "com.android.dialer/.EmergencyDialer", "com.android.server.telecom/com.android.server.telecom.PrivilegedCallActivity", "com.vivo.magazine/com.vivo.magazine.pager.view.PreviewActivity", "com.android.contacts/com.vivo.contacts.contactspicker.MultiContactsPickerActivity", "com.android.server.telecom/.CallActivity", "com.android.server.telecom/.ErrorDialogActivity", "com.android.incallui/com.android.incallui.PasswordManagerActivity", "com.vivo.space/com.vivo.space.ui.ActiviationActivity", "com.vivo.space/com.vivo.space.ui.feedback.FeedBackMainActivity", "android/android.accounts.ChooseTypeAndAccountActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.common.UnpackingRedirectActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.addaccount.AccountIntroActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.addaccount.WrapperControlledActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.addaccount.ErrorActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.addaccount.PreAddAccountActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.minutemaid.MinuteMaidActivity", "com.google.android.gms/com.google.android.gms.auth.uiflows.addaccount.AddAccountActivity", "com.android.settings/com.android.settings.password.ConfirmDeviceCredentialActivity", "com.android.settings/com.android.settings.password.ConfirmLockPattern", "com.google.android.gms/com.google.android.gms.setupservices.GoogleServicesActivity"};
    private HashMap<String, ArrayList<String>> mDefaultWhiteMap = new HashMap();
    private Object mLock = new Object();
    protected WeakReference<Toast> mToastRef = new WeakReference(null);
    private int mType = 0;

    class ChildrenModeContentObserver extends ContentObserver {
        public ChildrenModeContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ChildrenModeHelper.this.loadChildrenModeWhiteList(ChildrenModeHelper.this.mContext);
        }
    }

    class DefaultConfigObserver extends FileObserver {
        public DefaultConfigObserver(String path) {
            super(path, 264);
        }

        public void onEvent(int event, String path) {
            if (path != null) {
                switch (event) {
                    case 8:
                    case 256:
                        ChildrenModeHelper.sWorker.post(new Runnable() {
                            public void run() {
                                ChildrenModeHelper.this.loadDefaultConfigList(ChildrenModeHelper.this.mContext);
                            }
                        });
                        break;
                }
            }
        }
    }

    class IllegalConfigException extends RuntimeException {
        public IllegalConfigException(String detailMessage) {
            super(detailMessage);
        }

        public IllegalConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalConfigException(ChildrenModeHelper this$0, Throwable cause) {
            String str = null;
            ChildrenModeHelper.this = this$0;
            if (cause != null) {
                str = cause.toString();
            }
            super(str, cause);
        }
    }

    static {
        boolean z;
        sWorkerThread.start();
        if (SystemProperties.get(VivoPermissionManager.KEY_VIVO_LOG_CTRL, "no").equals("yes")) {
            z = true;
        } else {
            z = Build.TYPE.equals("eng");
        }
        sLogEnable = z;
    }

    private ChildrenModeHelper(Context context, int type) {
        if (sLogEnable) {
            Log.d(TAG, "init type = " + type);
        }
        this.mContext = context;
        this.mType = type;
        checkChildrenMode(context);
        context.getContentResolver().registerContentObserver(System.getUriFor(FLAG_CHILDREN_MODE_ENABLE), true, this.mChildrenModeObserver);
        if (type == 0) {
            this.mDefaultConfigObserver.startWatching();
            context.getContentResolver().registerContentObserver(CHILDREN_MODE_URI, true, this.mContentObserver);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CHILDREN_MODE_START);
        intentFilter.addAction(ACTION_CHILDREN_MODE_FULL_SCREEN_ASSERT);
        context.registerReceiver(this, intentFilter);
        if (type == 0) {
            loadData(context);
        }
    }

    private void checkChildrenMode(Context context) {
        this.mChildrenModeEnable = Boolean.parseBoolean(System.getString(context.getContentResolver(), FLAG_CHILDREN_MODE_ENABLE));
        if (!this.mChildrenModeEnable) {
            this.mChildrenModeStart = false;
            this.mChildrenModeTimeout = false;
            this.mChildrenModeBadPosture = false;
        }
        Log.i(TAG, "childrenModeEnable (checkChildrenMode) : " + this.mChildrenModeEnable);
    }

    public static ChildrenModeHelper getInstance(Context context, int type) {
        if (sInstance == null) {
            sInstance = new ChildrenModeHelper(context, type);
        }
        return sInstance;
    }

    private void loadData(final Context context) {
        sWorker.post(new Runnable() {
            public void run() {
                ChildrenModeHelper.this.loadDefaultConfigList(context);
                ChildrenModeHelper.this.loadChildrenModeWhiteListFromFile(context);
            }
        });
    }

    public boolean isChildrenModeEnable() {
        if (this.mType == 1) {
            return this.mChildrenModeEnable;
        }
        return this.mChildrenModeEnable ? this.mChildrenModeStart : false;
    }

    public void makeToast() {
        if (!this.mChildrenModeTimeout) {
            sWorker.post(new Runnable() {
                public void run() {
                    if (ChildrenModeHelper.sLogEnable) {
                        Log.i(ChildrenModeHelper.TAG, "Tost show : fun disabled");
                    }
                    if (ChildrenModeHelper.this.mToastRef.get() != null) {
                        ((Toast) ChildrenModeHelper.this.mToastRef.get()).cancel();
                    }
                    Toast toast = Toast.makeText(ChildrenModeHelper.this.mContext, (int) R.string.vivo_children_mode_fun_unavailable, 0);
                    toast.show();
                    ChildrenModeHelper.this.mToastRef = new WeakReference(toast);
                }
            });
        }
    }

    public boolean filter(String packageName, String className, String callingPackage) {
        long startTime = System.currentTimeMillis();
        if (!isChildrenModeEnable()) {
            return true;
        }
        if (packageName == null || className == null) {
            Log.w(TAG, "packageName or className is null");
            return true;
        }
        boolean result;
        if (sLogEnable) {
            Log.d(TAG, "filter : pkgName = " + packageName + "; clsName = " + className + "; callPkg = " + callingPackage);
        }
        synchronized (this.mLock) {
            if (TextUtils.equals(className, "com.android.camera.CameraActivity") && TextUtils.equals(callingPackage, "com.vivo.childrenmode")) {
                result = true;
            } else if (TextUtils.equals(className, "com.android.server.telecom.CallActivity") && (TextUtils.equals(callingPackage, "com.vivo.childrenmode") ^ 1) != 0) {
                result = false;
            } else if (this.mDefaultBlackList.contains(className)) {
                result = false;
            } else if (this.mChildrenModeWhiteList.contains(packageName)) {
                if (this.mChildrenModeTimeout || this.mChildrenModeBadPosture) {
                    if (sLogEnable) {
                        Log.d(TAG, "mChildrenModeTimeout = " + this.mChildrenModeTimeout);
                        Log.d(TAG, "mChildrenModeBadPosture = " + this.mChildrenModeBadPosture);
                    }
                    result = false;
                } else {
                    result = true;
                }
            } else if (this.mDefaultWhiteMap.containsKey(packageName)) {
                ArrayList<String> clsNames = (ArrayList) this.mDefaultWhiteMap.get(packageName);
                if (clsNames == null) {
                }
                if (clsNames.contains(packageName + FLAG_ALL_CLASS)) {
                    result = true;
                } else if (clsNames.contains(className)) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
            if (!result) {
                if (sLogEnable) {
                    Log.d(TAG, "fun : " + packageName + "/" + className + " is disabled under children mode");
                }
                makeToast();
            }
            if (sLogEnable) {
                Log.d(TAG, "ChildrenMode : filter cost time : " + (System.currentTimeMillis() - startTime));
            }
        }
        return result;
    }

    private void loadDefaultConfigList(Context context) {
        synchronized (this.mLock) {
            InputStream input = null;
            try {
                File file = new File(PATH_DEFAULT_CONFIG);
                if (file != null && file.exists()) {
                    input = new FileInputStream(file);
                }
            } catch (Exception e) {
                Log.w(TAG, "can not get default_white_list file from system!!");
                input = null;
            }
            if (input == null) {
                loadFactoryDefaultConifg();
            } else {
                try {
                    this.mDefaultWhiteMap.clear();
                    this.mDefaultBlackList.clear();
                    XmlPullParser pullParser = Xml.newPullParser();
                    pullParser.setInput(input, Contants.ENCODE_MODE);
                    for (int eventType = pullParser.getEventType(); eventType != 1; eventType = pullParser.next()) {
                        String name = pullParser.getName();
                        String value;
                        if (TAG_WHITE_ITEM.equals(name)) {
                            value = pullParser.nextText();
                            if (!(value == null || (value.isEmpty() ^ 1) == 0)) {
                                parserWhiteStr(value);
                            }
                        } else if (TAG_BLACK_ITEM.equals(name)) {
                            value = pullParser.nextText();
                            if (!(value == null || (value.isEmpty() ^ 1) == 0)) {
                                parserBlackStr(value);
                            }
                        }
                    }
                    try {
                        input.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException : " + e2);
                    }
                    if (null != null) {
                        loadFactoryDefaultConifg();
                    }
                } catch (XmlPullParserException ex) {
                    Log.e(TAG, "XmlPullParserException : " + ex);
                    try {
                        input.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "IOException : " + e22);
                    }
                    if (true) {
                        loadFactoryDefaultConifg();
                    }
                } catch (IOException e222) {
                    Log.e(TAG, "IOException : " + e222);
                    try {
                        input.close();
                    } catch (IOException e2222) {
                        Log.e(TAG, "IOException : " + e2222);
                    }
                    if (true) {
                        loadFactoryDefaultConifg();
                    }
                } catch (IllegalConfigException ex2) {
                    Log.e(TAG, "IllegalConfigException : " + ex2);
                    try {
                        input.close();
                    } catch (IOException e22222) {
                        Log.e(TAG, "IOException : " + e22222);
                    }
                    if (true) {
                        loadFactoryDefaultConifg();
                    }
                } catch (Throwable th) {
                    try {
                        input.close();
                    } catch (IOException e222222) {
                        Log.e(TAG, "IOException : " + e222222);
                    }
                    if (null != null) {
                        loadFactoryDefaultConifg();
                    }
                }
            }
        }
        dumpDefaultConfig();
        checkDefalutConfig();
    }

    private void loadFactoryDefaultConifg() {
        this.mDefaultWhiteMap.clear();
        this.mDefaultBlackList.clear();
        for (String parserWhiteStr : this.mDefaultWhiteListConfig) {
            try {
                parserWhiteStr(parserWhiteStr);
            } catch (IllegalConfigException ex) {
                Log.e(TAG, "loadFactoryDefaultConifg Error : " + ex);
            }
        }
        for (String parserWhiteStr2 : this.mDefaultBlackListConfig) {
            parserBlackStr(parserWhiteStr2);
        }
    }

    private void parserWhiteStr(String str) throws IllegalConfigException {
        int sep = str.indexOf(47);
        if (sep < 0 || sep + 1 >= str.length()) {
            throw new IllegalConfigException("config item is : " + str);
        }
        String pkg = str.substring(0, sep);
        String cls = str.substring(sep + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        ArrayList<String> clsNames;
        if (pkg == null || cls == null) {
            throw new IllegalConfigException("config item is : " + str);
        } else if (this.mDefaultWhiteMap.containsKey(pkg)) {
            clsNames = (ArrayList) this.mDefaultWhiteMap.get(pkg);
            if (clsNames == null) {
                clsNames = new ArrayList();
                this.mDefaultWhiteMap.put(pkg, clsNames);
            }
            clsNames.add(cls);
        } else {
            clsNames = new ArrayList();
            clsNames.add(cls);
            this.mDefaultWhiteMap.put(pkg, clsNames);
        }
    }

    private void parserBlackStr(String str) {
        this.mDefaultBlackList.add(str);
    }

    private void dumpDefaultConfig() {
        if (sLogEnable) {
            synchronized (this.mLock) {
                Log.d(TAG, "dump default white list start ---------- ");
                for (Entry entry : this.mDefaultWhiteMap.entrySet()) {
                    ArrayList<String> clsNames = (ArrayList) entry.getValue();
                    Log.d(TAG, "packageName : " + ((String) entry.getKey()));
                    for (String s : clsNames) {
                        Log.d(TAG, "    className : " + s);
                    }
                }
                Log.d(TAG, "dump default white list end ---------- ");
                Log.d(TAG, "dump default black list start ---------- ");
                for (String blackItem : this.mDefaultBlackList) {
                    Log.d(TAG, "black item : " + blackItem);
                }
                Log.d(TAG, "dump default black list end ---------- ");
            }
        }
    }

    private void checkDefalutConfig() {
        synchronized (this.mLock) {
            try {
                for (Entry entry : this.mDefaultWhiteMap.entrySet()) {
                    String pkg = (String) entry.getKey();
                    ArrayList<String> clsNames = (ArrayList) entry.getValue();
                    if (clsNames == null) {
                        throw new IllegalConfigException("exist illegal config item");
                    } else if (clsNames != null) {
                        if (clsNames.size() > 1 && clsNames.contains(pkg + FLAG_ALL_CLASS)) {
                            throw new IllegalConfigException("exist illegal config item");
                        }
                    }
                }
            } catch (IllegalConfigException ex) {
                Log.e(TAG, "load Config error when check default config : ", ex);
                loadFactoryDefaultConifg();
            }
        }
        return;
    }

    private void loadChildrenModeWhiteListFromFile(Context context) {
        InputStream inputStream = null;
        ArrayList<String> childrenModeWhiteListTemp = new ArrayList();
        try {
            File file = new File(PATH_CHILDREN_MODE_WHITE_LIST_TEMP);
            if (file != null && file.exists()) {
                inputStream = new FileInputStream(file);
            }
        } catch (Exception ex) {
            Log.w(TAG, "can not get com.vivo.childrenmode_children_mode_white_list_temp.xml file from system!!", ex);
            inputStream = null;
        }
        if (inputStream != null) {
            try {
                childrenModeWhiteListTemp.clear();
                XmlPullParser pullParser = Xml.newPullParser();
                pullParser.setInput(inputStream, Contants.ENCODE_MODE);
                for (int eventType = pullParser.getEventType(); eventType != 1; eventType = pullParser.next()) {
                    if (TAG_CHILDREN_MODE_WHITE_LIST_ITEM.equals(pullParser.getName())) {
                        String value = pullParser.nextText();
                        if (!(value == null || (value.isEmpty() ^ 1) == 0)) {
                            childrenModeWhiteListTemp.add(value);
                        }
                    }
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException : " + e);
                }
                if (null == null) {
                    synchronized (this.mLock) {
                        this.mChildrenModeWhiteList.clear();
                        this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                    }
                    dumpChildrenModeList();
                    return;
                }
                loadChildrenModeWhiteList(context);
            } catch (XmlPullParserException ex2) {
                Log.e(TAG, "XmlPullParserException : " + ex2);
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    Log.e(TAG, "IOException : " + e2);
                }
                if (true) {
                    loadChildrenModeWhiteList(context);
                    return;
                }
                synchronized (this.mLock) {
                    this.mChildrenModeWhiteList.clear();
                    this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                    dumpChildrenModeList();
                }
            } catch (IOException e22) {
                Log.e(TAG, "IOException : " + e22);
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    Log.e(TAG, "IOException : " + e222);
                }
                if (true) {
                    loadChildrenModeWhiteList(context);
                    return;
                }
                synchronized (this.mLock) {
                    this.mChildrenModeWhiteList.clear();
                    this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                    dumpChildrenModeList();
                }
            } catch (IllegalConfigException ex3) {
                Log.e(TAG, "IllegalConfigException : " + ex3);
                try {
                    inputStream.close();
                } catch (IOException e2222) {
                    Log.e(TAG, "IOException : " + e2222);
                }
                if (true) {
                    loadChildrenModeWhiteList(context);
                    return;
                }
                synchronized (this.mLock) {
                    this.mChildrenModeWhiteList.clear();
                    this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                    dumpChildrenModeList();
                }
            } catch (Throwable th) {
                try {
                    inputStream.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException : " + e22222);
                }
                if (null == null) {
                    synchronized (this.mLock) {
                        this.mChildrenModeWhiteList.clear();
                        this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                        dumpChildrenModeList();
                    }
                } else {
                    loadChildrenModeWhiteList(context);
                }
            }
        } else {
            loadChildrenModeWhiteList(context);
        }
    }

    private void writeChildrenModeWhiteListToFile(ArrayList<String> whiteList) {
        Throwable th;
        StringBuilder contant = new StringBuilder();
        contant.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        if (whiteList != null && whiteList.size() > 0) {
            for (String item : whiteList) {
                contant.append("<");
                contant.append(TAG_CHILDREN_MODE_WHITE_LIST_ITEM);
                contant.append(">");
                contant.append(item);
                contant.append("</");
                contant.append(TAG_CHILDREN_MODE_WHITE_LIST_ITEM);
                contant.append(">\n");
            }
        }
        File file = new File(PATH_CHILDREN_MODE_WHITE_LIST_TEMP);
        FileWriter fw = null;
        try {
            if (sLogEnable) {
                Log.i(TAG, "write children mode white list to file : \n" + contant);
            }
            if (file == null || (file.exists() ^ 1) != 0) {
                file.createNewFile();
            }
            FileWriter fw2 = new FileWriter(file, false);
            try {
                fw2.write(contant.toString());
                try {
                    fw2.close();
                } catch (IOException e) {
                }
                fw = fw2;
            } catch (IOException e2) {
                fw = fw2;
                try {
                    Log.w(TAG, "write children mode white item fail!!");
                    file.delete();
                    try {
                        fw.close();
                    } catch (IOException e3) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        fw.close();
                    } catch (IOException e4) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fw = fw2;
                fw.close();
                throw th;
            }
        } catch (IOException e5) {
            Log.w(TAG, "write children mode white item fail!!");
            if (file != null && file.exists()) {
                file.delete();
            }
            fw.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x004f A:{SYNTHETIC, Splitter: B:12:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0055 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadChildrenModeWhiteList(Context context) {
        ArrayList<String> childrenModeWhiteListTemp = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CHILDREN_MODE_URI, new String[]{CHILDRENMODE_PACKAGE_NAME}, "ap_indicate=?", new String[]{String.valueOf(2)}, null);
            if (cursor != null) {
                int packageNameIndex = cursor.getColumnIndexOrThrow(CHILDRENMODE_PACKAGE_NAME);
                childrenModeWhiteListTemp.clear();
                if (cursor == null || cursor.getCount() <= 0) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            Log.e(TAG, "close couser error : " + e);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mChildrenModeWhiteList.clear();
                        this.mChildrenModeWhiteList.addAll(childrenModeWhiteListTemp);
                    }
                    writeChildrenModeWhiteListToFile(childrenModeWhiteListTemp);
                    dumpChildrenModeList();
                    return;
                }
                cursor.moveToFirst();
                do {
                    childrenModeWhiteListTemp.add(cursor.getString(packageNameIndex));
                } while (cursor.moveToNext());
                if (cursor != null) {
                }
                synchronized (this.mLock) {
                }
                writeChildrenModeWhiteListToFile(childrenModeWhiteListTemp);
                dumpChildrenModeList();
                return;
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    Log.e(TAG, "close couser error : " + e2);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "query children mode list error : " + ex);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e22) {
                    Log.e(TAG, "close couser error : " + e22);
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e222) {
                    Log.e(TAG, "close couser error : " + e222);
                }
            }
        }
    }

    private void dumpChildrenModeList() {
        if (sLogEnable) {
            synchronized (this.mLock) {
                if (this.mChildrenModeWhiteList != null) {
                    Log.d(TAG, "dump children mode list start ---------- ");
                    for (String pkgName : this.mChildrenModeWhiteList) {
                        Log.d(TAG, "pkgName = " + pkgName);
                    }
                    Log.d(TAG, "dump children mode list end ---------- ");
                }
            }
        }
    }

    public void destory() {
        if (this.mDefaultConfigObserver != null) {
            this.mDefaultConfigObserver.stopWatching();
        }
        if (!(this.mContentObserver == null || this.mContext == null)) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        }
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this);
        }
        synchronized (this.mLock) {
            this.mDefaultWhiteMap.clear();
            this.mDefaultBlackList.clear();
            this.mChildrenModeWhiteList.clear();
        }
    }

    public void onReceive(Context context, final Intent intent) {
        final String action = intent.getAction();
        sWorker.post(new Runnable() {
            public void run() {
                if (ChildrenModeHelper.ACTION_CHILDREN_MODE_START.equals(action)) {
                    ChildrenModeHelper.this.mChildrenModeStart = intent.getBooleanExtra(ChildrenModeHelper.EXTRA_IS_START, false);
                    Log.i(ChildrenModeHelper.TAG, "mChildrenModeStart = " + ChildrenModeHelper.this.mChildrenModeStart);
                } else if (ChildrenModeHelper.ACTION_CHILDREN_MODE_FULL_SCREEN_ASSERT.equals(action)) {
                    ChildrenModeHelper.this.mChildrenModeTimeout = intent.getBooleanExtra(ChildrenModeHelper.EXTRA_IS_TIME_OUT, false);
                    ChildrenModeHelper.this.mChildrenModeBadPosture = intent.getBooleanExtra(ChildrenModeHelper.EXTRA_IS_BAD_POSTURE, false);
                    Log.i(ChildrenModeHelper.TAG, "mChildrenModeTimeout = " + ChildrenModeHelper.this.mChildrenModeTimeout);
                    Log.i(ChildrenModeHelper.TAG, "mChildrenModeBadPosture = " + ChildrenModeHelper.this.mChildrenModeBadPosture);
                }
            }
        });
    }
}
