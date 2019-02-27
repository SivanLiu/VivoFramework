package com.vivo.services.userprofiling;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.security.keymaster.SecurityKeyException;
import android.util.Base64;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.vivo.services.cipher.SecurityKeyCipher;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.rms.sdk.RMNative;
import com.vivo.services.userprofiling.entity.PageInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import vivo.app.userprofiling.IVivoUserProfiling.Stub;

public class VivoUserProfilingService extends Stub {
    private static final int BASE = 100;
    private static final String CONFIG_KEY = "dE80aWhsZzVWa1Zyc0c0N0FvR2FBdz37";
    private static final String CONFIG_PATH = "/data/bbkcore/";
    private static final String DATE = "time";
    private static final long DELAY_TIME = 60000;
    private static final long DETECT_INTERNAL = 3600000;
    private static final String EVENT_ID = "30010";
    private static final String LABEL = "label";
    private static final int LOAD_CONFIG = 101;
    private static final String MODULE_NAME = "com.vivo.services.userprofiling.IUserProfilingService";
    private static final long ONE_HOUR = 3600000;
    private static final String PAGE_CONFIG_FILENAME = "acpgcf_config.xml";
    private static final String PKG = "pkgName";
    private static final String REPORT_ERROR_CODE = "-1";
    private static final int SEND_INFO = 103;
    private static final String SPLIT_COLON = ":";
    private static final String SPLIT_STR = "#";
    private static final String TABLE = "tableName";
    private static final String TAG = "VivoUserProfilingService";
    private static final String TYPE = "type";
    private static final int UPDATE_SWITCH = 102;
    private static final String URISTR = "content://com.vivo.assistant.CollectorProvider/";
    private static final String USER_EXPERIENCE_IMPROVE_PLAN = "user_experience_improve_plan";
    private static final String VALUE = "value";
    private static boolean isCaptureOn = false;
    private static boolean isEventEnable = false;
    private static boolean isUEIPOpen = false;
    private static Map<String, Object> mApplicationConfig;
    private static List mTableNames;
    private String mAllowListStr = "";
    private Context mContext;
    private boolean mDebug = false;
    private String mDisAllowListStr = "";
    private boolean mEnable = true;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private long mLastCheck = 0;
    private Looper mLooper;
    private String mValueKey = null;
    private String mWeChatWebViewValue = null;

    final class UserProfilingServiceHandler extends Handler {
        public UserProfilingServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VivoUserProfilingService.LOAD_CONFIG /*101*/:
                    VivoUserProfilingService.this.LoadConfig();
                    return;
                case VivoUserProfilingService.UPDATE_SWITCH /*102*/:
                    if (VivoUserProfilingService.this.mContext != null) {
                        VivoUserProfilingService.isUEIPOpen = VivoUserProfilingService.this.isUEIPOpen(VivoUserProfilingService.this.mContext);
                        VivoUserProfilingService.isEventEnable = VivoUserProfilingService.this.isEventEnable(VivoUserProfilingService.EVENT_ID, VivoUserProfilingService.this.mContext);
                    }
                    if (VivoUserProfilingService.this.mHandler != null && (VivoUserProfilingService.this.mHandler.hasMessages(VivoUserProfilingService.UPDATE_SWITCH) ^ 1) != 0) {
                        Message msg_up = Message.obtain();
                        msg_up.what = VivoUserProfilingService.UPDATE_SWITCH;
                        VivoUserProfilingService.this.mHandler.sendMessageDelayed(msg_up, 3600000);
                        return;
                    }
                    return;
                case VivoUserProfilingService.SEND_INFO /*103*/:
                    Bundle bundle = new Bundle();
                    bundle = msg.getData();
                    String tableName = bundle.getString(VivoUserProfilingService.TABLE);
                    Slog.d(VivoUserProfilingService.TAG, "reportDataToBee " + VivoUserProfilingService.this.subReportDataToBee(Uri.parse(VivoUserProfilingService.URISTR + tableName), bundle.getString("value"), bundle.getString(VivoUserProfilingService.PKG), bundle.getString(VivoUserProfilingService.TYPE)));
                    return;
                default:
                    return;
            }
        }
    }

    private static native String doCommonJobByNative(String str);

    static {
        mApplicationConfig = null;
        mTableNames = null;
        mApplicationConfig = new HashMap();
        mTableNames = new ArrayList();
        mTableNames.add("info/userinfo");
        initWeChatViewId();
    }

    public VivoUserProfilingService(Context context) {
        Message msg;
        Slog.i(TAG, "Vivo UserProfiling Service");
        this.mContext = context;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new UserProfilingServiceHandler(thread.getLooper());
        if (!(this.mHandler == null || (this.mHandler.hasMessages(LOAD_CONFIG) ^ 1) == 0)) {
            msg = Message.obtain();
            msg.what = LOAD_CONFIG;
            this.mHandler.sendMessageDelayed(msg, DELAY_TIME);
        }
        if (this.mHandler != null && (this.mHandler.hasMessages(UPDATE_SWITCH) ^ 1) != 0) {
            msg = Message.obtain();
            msg.what = UPDATE_SWITCH;
            this.mHandler.sendMessageDelayed(msg, DELAY_TIME);
        }
    }

    public String doCommonJob(String msg) {
        Slog.d(TAG, "doCommonJob msg = " + msg);
        return doCommonJobByNative(msg);
    }

    private void LoadConfig() {
        synchronized (mApplicationConfig) {
            clearProperties();
            Log.d(TAG, "Config");
            File file = new File("/data/bbkcore/acpgcf_config.xml");
            readXmlFileFromUnifiedConfig("VivoAssistant", "1", RMNative.VERSION, "VivoAssistant_acpgcf", file);
            if (file == null || !file.exists()) {
                Slog.d(TAG, "file is not exist ");
            } else {
                try {
                    String result = readByBufferedReader(file);
                    if (result != null) {
                        String xml_config = decrypt(CONFIG_KEY, result);
                        if (xml_config != null) {
                            parsePageConfigXml(new ByteArrayInputStream(xml_config.getBytes()));
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "decode data error! " + e.fillInStackTrace());
                }
            }
            if (mApplicationConfig != null && mApplicationConfig.size() == 0) {
                initWeChatViewId();
            }
        }
        return;
    }

    private void parsePageConfigXml(InputStream inStream) {
        try {
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(inStream, "UTF-8");
            ArrayList arrPageInfo = null;
            PageInfo pageInfo = null;
            Object pkgName = null;
            for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
                switch (event) {
                    case 2:
                        String name = pullParser.getName();
                        if ("CaptureSwitch".equals(name)) {
                            String isOn = pullParser.getAttributeValue(null, "onOff");
                            if ("1".equals(isOn)) {
                                isCaptureOn = true;
                            } else if ("0".equals(isOn)) {
                                isCaptureOn = false;
                            }
                        }
                        if ("ValueKey".equals(name)) {
                            String key = pullParser.getAttributeValue(null, "key");
                            if (key != null && key.length() > 0) {
                                this.mValueKey = key;
                            }
                        }
                        if ("Package".equals(name)) {
                            pkgName = pullParser.getAttributeValue(null, "name");
                            arrPageInfo = new ArrayList();
                        }
                        if ("Page".equals(name)) {
                            String pageName = pullParser.getAttributeValue(null, "name");
                            String pageType = pullParser.getAttributeValue(null, "pagetype");
                            String infoType = pullParser.getAttributeValue(null, "infotype");
                            if (!(pageName == null && pageType == null)) {
                                pageInfo = new PageInfo(pageName, pageType, infoType);
                            }
                        }
                        if (!LABEL.equals(name)) {
                            break;
                        }
                        String label = pullParser.getAttributeValue(null, "name");
                        String value = pullParser.getAttributeValue(null, "value");
                        if (!(pageInfo == null || label == null || label.length() <= 0 || value == null)) {
                            pageInfo.addPageProperties(label, value);
                            break;
                        }
                    case 3:
                        String end = pullParser.getName();
                        if (!(!"Page".equals(end) || arrPageInfo == null || pageInfo == null)) {
                            if (isUEIPOpen) {
                                arrPageInfo.add(pageInfo);
                            } else if (!isUEIPOpen && "com.tencent.mm".equals(pkgName) && "WebViewUI".equals(pageInfo.getPageName())) {
                                arrPageInfo.add(pageInfo);
                            }
                        }
                        if (!(!"Package".equals(end) || mApplicationConfig == null || pkgName == null || arrPageInfo == null)) {
                            mApplicationConfig.put(pkgName, arrPageInfo);
                            arrPageInfo = null;
                            pageInfo = null;
                            break;
                        }
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Exception : " + e);
        }
    }

    private boolean checkAccessPermision(String s) {
        boolean flag = false;
        if (s != null) {
            flag = mTableNames.contains(s.trim());
        }
        Slog.d(TAG, "checkAccessPermision -> [suffix: " + " # result: " + flag + "]");
        return flag;
    }

    private void clearProperties() {
        if (mApplicationConfig != null) {
            mApplicationConfig.clear();
        }
    }

    public String getApplicationConfig() {
        synchronized (mApplicationConfig) {
            if (mApplicationConfig == null || mApplicationConfig.size() < 1) {
                return null;
            }
            JSONObject json = new JSONObject();
            for (Entry<String, Object> entry : mApplicationConfig.entrySet()) {
                String pkg = (String) entry.getKey();
                ArrayList<PageInfo> pkgArrPageInfo = (ArrayList) entry.getValue();
                if (!(pkg == null || pkgArrPageInfo == null)) {
                    if (pkgArrPageInfo.size() >= 1) {
                        Iterator iterator = pkgArrPageInfo.iterator();
                        while (iterator.hasNext()) {
                            PageInfo pageInfo = (PageInfo) iterator.next();
                            if (pageInfo == null) {
                                return null;
                            }
                            String pageName = pageInfo.getPageName();
                            String pageType = pageInfo.getPageType();
                            String infoType = pageInfo.getInfoType();
                            Map<String, String> properties = pageInfo.getPageProperties();
                            int propertiesSize = pageInfo.getPagePropertiesSize();
                            if (pageName == null || pageType == null || infoType == null || properties == null || propertiesSize < 1) {
                                return null;
                            }
                            StringBuilder title = new StringBuilder();
                            title.append(pkg).append(SPLIT_STR).append(pageName).append(SPLIT_STR).append(pageType).append(SPLIT_STR).append(infoType);
                            StringBuilder pageProperties = new StringBuilder();
                            for (Entry<String, String> entry2 : properties.entrySet()) {
                                pageProperties.append((String) entry2.getKey()).append(SPLIT_COLON).append((String) entry2.getValue()).append(SPLIT_STR);
                            }
                            if (title != null) {
                                if (title.length() >= 1 && pageProperties != null) {
                                    if (pageProperties.length() >= 1) {
                                        try {
                                            json.put(title.toString(), pageProperties.toString());
                                        } catch (JSONException e2) {
                                            Slog.e(TAG, "get Config e2:" + e2);
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                    }
                }
                return null;
            }
            Slog.d(TAG, "get Config");
            String jSONObject = json.toString();
            return jSONObject;
        }
    }

    private boolean isStrInList(String str, List<String> list) {
        if (list == null || str == null) {
            return false;
        }
        for (String strEle : list) {
            if (str.equals(strEle)) {
                return true;
            }
        }
        return false;
    }

    private void getControlStr(Context context) {
        if (context != null) {
            Slog.d(TAG, "getControlStr");
            String URI = "content://com.vivo.assistant.InformationProvider/server_control";
            Cursor cursor = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                if (resolver != null) {
                    boolean z;
                    cursor = resolver.query(Uri.parse(URI), null, null, null, null);
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("cursor != null ");
                    if (cursor != null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Slog.d(str, append.append(z).toString());
                    if (cursor != null) {
                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            while (!cursor.isAfterLast()) {
                                this.mAllowListStr = cursor.getString(cursor.getColumnIndex("allow"));
                                this.mDisAllowListStr = cursor.getString(cursor.getColumnIndex("disallow"));
                                cursor.moveToNext();
                            }
                        } else {
                            Slog.d(TAG, "no data!");
                        }
                    } else {
                        Slog.d(TAG, "cursor is null, lock failed, continue checking for update!");
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                Slog.e(TAG, "open database error!");
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private List<String> strToList(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        List<String> targetList = new ArrayList();
        String[] strs = str.split(",");
        if (strs != null) {
            for (String subStr : strs) {
                if (!isStrInList(subStr, targetList)) {
                    targetList.add(subStr);
                }
            }
        }
        return targetList;
    }

    private boolean isEventEnable(String eventID, Context context) {
        long current = System.currentTimeMillis();
        if (this.mLastCheck == 0 || current - this.mLastCheck > 3600000) {
            try {
                this.mLastCheck = current;
                getControlStr(context);
                List<String> allowList = strToList(this.mAllowListStr);
                List<String> disallowList = strToList(this.mDisAllowListStr);
                if (isStrInList("_all", disallowList) || isStrInList(eventID, disallowList)) {
                    this.mEnable = false;
                    return this.mEnable;
                }
                if (isStrInList("_all", allowList) || isStrInList(eventID, allowList)) {
                    this.mEnable = true;
                }
                return this.mEnable;
            } catch (Exception e) {
                Slog.d(TAG, "mEnable:" + e);
                this.mEnable = false;
            }
        } else {
            Slog.d(TAG, "mEnable:" + this.mEnable);
            return this.mEnable;
        }
    }

    private boolean isUEIPOpen(Context context) {
        return System.getInt(context.getContentResolver(), USER_EXPERIENCE_IMPROVE_PLAN, 0) == 1;
    }

    public boolean isCaptureOn() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        Slog.d(TAG, isUEIPOpen + " " + isEventEnable + " " + isCaptureOn);
        if (isEventEnable) {
            z = isCaptureOn;
        }
        return z;
    }

    private String unicode2String(String unicode) {
        if (unicode == null) {
            return null;
        }
        StringBuffer string = new StringBuffer();
        try {
            String[] hex = unicode.split("\\\\u");
            for (int i = 1; i < hex.length; i++) {
                string.append((char) Integer.parseInt(hex[i], 16));
            }
        } catch (Exception e) {
            Slog.e(TAG, "Exception :" + e);
        }
        return string.toString();
    }

    private int subReportDataToBee(Uri uri, String values, String pkg, String type) {
        try {
            if (this.mValueKey == null || this.mValueKey.length() < 1 || this.mContext == null) {
                Log.d(TAG, "key is null");
                return -1;
            }
            JSONObject json = new JSONObject(values);
            long date = System.currentTimeMillis();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = json.get(key).toString();
                String value_encrypt = null;
                if (value != null && value.getBytes("UTF-8").length < 204800) {
                    SecurityKeyCipher cipher = SecurityKeyCipher.getInstance(this.mContext, MODULE_NAME);
                    if (cipher == null) {
                        Log.d(TAG, "cipher is null");
                        return -1;
                    }
                    try {
                        byte[] encryptData = cipher.aesEncrypt(value.getBytes("UTF-8"));
                        if (encryptData != null) {
                            value_encrypt = Base64.encodeToString(encryptData, 11);
                            Log.d(TAG, "input");
                        }
                    } catch (SecurityKeyException e) {
                        Log.e(TAG, "VivoCipher aesDecrypt Exception: " + e.getErrorCode());
                        if (!(e.getErrorCode() == UPDATE_SWITCH || e.getErrorCode() == 120)) {
                            cipher.setCipherMode(3);
                            try {
                                byte[] encryptData1 = cipher.aesEncrypt(value.getBytes("UTF-8"));
                                if (encryptData1 != null) {
                                    value_encrypt = Base64.encodeToString(encryptData1, 11);
                                    Log.d(TAG, "input1");
                                }
                            } catch (SecurityKeyException e1) {
                                Log.e(TAG, "VivoCipher aesDecrypt Exception1: " + e1.getErrorCode());
                                return -1;
                            } catch (UnsupportedEncodingException e12) {
                                Log.e(TAG, "UnsupportedEncodingException1:" + e12);
                                e12.printStackTrace();
                                return -1;
                            } catch (Exception e13) {
                                Log.e(TAG, "Exception1:" + e13);
                                e13.printStackTrace();
                                return -1;
                            }
                        }
                    } catch (UnsupportedEncodingException e2) {
                        Log.e(TAG, "UnsupportedEncodingException:" + e2);
                        e2.printStackTrace();
                        return -1;
                    } catch (Exception e3) {
                        Log.e(TAG, "Exception:" + e3);
                        e3.printStackTrace();
                        return -1;
                    }
                    if (value_encrypt != null) {
                        if (value_encrypt.length() > 0) {
                            ContentValues contentvalues = new ContentValues();
                            contentvalues.put(DATE, Long.valueOf(date));
                            contentvalues.put(TYPE, type);
                            contentvalues.put(PKG, pkg);
                            contentvalues.put(LABEL, key);
                            contentvalues.put("value", value_encrypt);
                            this.mContext.getContentResolver().insert(uri, contentvalues);
                            Log.d(TAG, "Report successfully");
                        }
                    }
                }
            }
            return 0;
        } catch (Exception e32) {
            Slog.e(TAG, "Failed to insert value " + e32);
            return -1;
        }
    }

    public int reportDataToBee(String tableName, String values, String pkgName, String infoType) {
        int i = -1;
        if (checkAccessPermision(tableName) && this.mHandler != null) {
            Bundle data = new Bundle();
            data.putString(TABLE, tableName);
            data.putString("value", values);
            data.putString(PKG, pkgName);
            data.putString(TYPE, infoType);
            Message msg = Message.obtain();
            msg.what = SEND_INFO;
            msg.setData(data);
            this.mHandler.sendMessage(msg);
            i = 1;
        }
        setWeChatWebViewValue(values);
        return i;
    }

    public void updateConfig() {
        if (this.mHandler != null && (this.mHandler.hasMessages(LOAD_CONFIG) ^ 1) != 0) {
            this.mHandler.sendEmptyMessage(LOAD_CONFIG);
        }
    }

    private String decrypt(String seed, String encrypted) throws Exception {
        return new String(decrypt(getRawKey(seed.getBytes()), toByte(encrypted)));
    }

    private byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(ProcessStates.HASSERVICE, sr);
        return kgen.generateKey().getEncoded();
    }

    private byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(2, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(encrypted);
    }

    private byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0058 A:{SYNTHETIC, Splitter: B:24:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0071 A:{SYNTHETIC, Splitter: B:38:0x0071} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readByBufferedReader(File file) {
        Exception e;
        Throwable th;
        String str = null;
        if (file == null || (file.exists() ^ 1) != 0) {
            return null;
        }
        BufferedReader bReader = null;
        StringBuffer buffer = null;
        try {
            BufferedReader bReader2 = new BufferedReader(new FileReader(file));
            if (bReader2 != null) {
                while (true) {
                    StringBuffer buffer2 = buffer;
                    try {
                        String line = bReader2.readLine();
                        if (line == null) {
                            buffer = buffer2;
                            break;
                        }
                        if (buffer2 == null) {
                            buffer = new StringBuffer();
                        } else {
                            buffer = buffer2;
                        }
                        if (buffer != null) {
                            try {
                                buffer.append(line).append("\n");
                            } catch (Exception e2) {
                                e = e2;
                                bReader = bReader2;
                            } catch (Throwable th2) {
                                th = th2;
                                bReader = bReader2;
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        buffer = buffer2;
                        bReader = bReader2;
                        try {
                            Log.e(TAG, "Buffered Reader failed! " + e.fillInStackTrace());
                            if (bReader != null) {
                                try {
                                    bReader.close();
                                } catch (Exception e4) {
                                }
                            }
                            if (buffer != null) {
                            }
                            return str;
                        } catch (Throwable th3) {
                            th = th3;
                            if (bReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        bReader = bReader2;
                        if (bReader != null) {
                            try {
                                bReader.close();
                            } catch (Exception e5) {
                            }
                        }
                        throw th;
                    }
                }
            }
            if (bReader2 != null) {
                try {
                    bReader2.close();
                } catch (Exception e6) {
                }
            }
        } catch (Exception e7) {
            e = e7;
            Log.e(TAG, "Buffered Reader failed! " + e.fillInStackTrace());
            if (bReader != null) {
            }
            if (buffer != null) {
            }
            return str;
        }
        if (buffer != null) {
            str = buffer.toString();
        }
        return str;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x006f A:{SYNTHETIC, Splitter: B:28:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0066 A:{SYNTHETIC, Splitter: B:23:0x0066} */
    /* JADX WARNING: Missing block: B:6:0x000c, code:
            if ((r8.exists() ^ 1) != 0) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeByBufferedWriter(String string, File desFile) {
        Exception e;
        Throwable th;
        if (string != null) {
            BufferedWriter bWriter = null;
            if (desFile != null) {
                try {
                } catch (Exception e2) {
                    e = e2;
                    try {
                        Log.e(TAG, "Buffered write failed! " + e.fillInStackTrace());
                        if (bWriter != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bWriter != null) {
                        }
                        throw th;
                    }
                }
            }
            desFile.createNewFile();
            BufferedWriter bWriter2 = new BufferedWriter(new FileWriter(desFile));
            try {
                bWriter2.write(string);
                if (this.mDebug) {
                    Log.d(TAG, "writeByBufferedWriter: " + string);
                }
                if (bWriter2 != null) {
                    try {
                        bWriter2.close();
                    } catch (Exception e3) {
                    }
                }
                bWriter = bWriter2;
            } catch (Exception e4) {
                e = e4;
                bWriter = bWriter2;
                Log.e(TAG, "Buffered write failed! " + e.fillInStackTrace());
                if (bWriter != null) {
                    try {
                        bWriter.close();
                    } catch (Exception e5) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                bWriter = bWriter2;
                if (bWriter != null) {
                    try {
                        bWriter.close();
                    } catch (Exception e6) {
                    }
                }
                throw th;
            }
        }
    }

    private void readXmlFileFromUnifiedConfig(String module, String type, String version, String identifier, File file) {
        Cursor cursor = null;
        byte[] filecontent = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, new String[]{module, type, version, identifier}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        filecontent = cursor.getBlob(cursor.getColumnIndex("filecontent"));
                        cursor.moveToNext();
                        Log.d(TAG, "UnifiedConfig content");
                    }
                } else {
                    Log.d(TAG, "UnifiedConfig no data!");
                }
            } else {
                Log.d(TAG, "null!");
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "open database error! " + e2.fillInStackTrace());
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                }
            }
        }
        if (filecontent != null) {
            writeByBufferedWriter(new String(filecontent), file);
        }
    }

    public void ping(String msg) {
        Slog.d(TAG, "ping msg = " + msg);
    }

    private static void initWeChatViewId() {
        ArrayList<PageInfo> arrPageInfo = new ArrayList();
        PageInfo pageInfo = new PageInfo("WebViewUI", "2", "8");
        pageInfo.addPageProperties("1000", "text1");
        arrPageInfo.add(pageInfo);
        mApplicationConfig.put("com.tencent.mm", arrPageInfo);
    }

    public void setWeChatWebViewValue(String webViewValue) {
        this.mWeChatWebViewValue = webViewValue;
    }

    public String getWeChatWebViewValue() {
        return this.mWeChatWebViewValue;
    }

    public void updateWeChatViewId(String textViewId) {
        ArrayList<PageInfo> arrPageInfo = (ArrayList) mApplicationConfig.get("com.tencent.mm");
        if (arrPageInfo == null) {
            initWeChatViewId();
            return;
        }
        for (PageInfo pageInfo : arrPageInfo) {
            if (pageInfo != null && "WebViewUI".equals(pageInfo.getPageName())) {
                pageInfo.clearPageProperties();
                pageInfo.addPageProperties("1000", textViewId);
                return;
            }
        }
    }
}
