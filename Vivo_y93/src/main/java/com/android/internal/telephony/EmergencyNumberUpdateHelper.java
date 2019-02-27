package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class EmergencyNumberUpdateHelper extends Handler {
    private static final int EVENT_ACQUIRE_EMERGENCY_NUMBERS = 0;
    private static final int EVENT_UPDATE_EMERGENCY_PROPERTY = 1;
    private static final int EVENT_UPDATE_EMERGENCY_PROPERTY_BY_PHONEID = 2;
    private static final String PHONE_DISPLAY_ONLY_NUM = "ril.display.vivo.list";
    private static final String PHONE_EMCC_NUM = "ril.ecc.vivo.list";
    private static final String TAG = "EmergencyNumberUpdateHelper";
    private static EmergencyNumberUpdateHelper mEmergencyNumberUpdateHelper;
    private Context mContext;
    private HashMap<String, HashMap<String, EmergencyNum>> mEmerMccList = new HashMap();
    private HashMap<String, HashMap<String, EmergencyNum>> mEmerMccMncList = new HashMap();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_TelephonyEmergencyNumber".equals(action)) {
                    EmergencyNumberUpdateHelper.this.triggleAcquireEmergencyNumber();
                } else if ("vivo.intent.action.USER_COUNTRY_CHANGE".equals(action)) {
                    EmergencyNumberUpdateHelper.this.handleMccChanged();
                }
            }
        }
    };

    private class EmergencyNum {
        String mDisplayRules;
        String mEmerRules;

        public EmergencyNum(String emerRules, String displayRules) {
            this.mEmerRules = emerRules;
            this.mDisplayRules = displayRules;
        }
    }

    public static void makeDefaultUpdater(Context context, Looper looper) {
        if (mEmergencyNumberUpdateHelper == null) {
            mEmergencyNumberUpdateHelper = new EmergencyNumberUpdateHelper(context, looper);
        }
    }

    public static EmergencyNumberUpdateHelper getInstance() {
        return mEmergencyNumberUpdateHelper;
    }

    public EmergencyNumberUpdateHelper(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.vivo.daemonService.unifiedconfig.update_finish_broadcast_TelephonyEmergencyNumber");
        filter.addAction("vivo.intent.action.USER_COUNTRY_CHANGE");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        triggleAcquireEmergencyNumber();
    }

    public void triggleAcquireEmergencyNumber() {
        sendEmptyMessage(0);
    }

    public void updateEmergencyProperties() {
        if (!hasMessages(1)) {
            sendEmptyMessageDelayed(1, 5000);
        }
    }

    public void updateEmergencyPropertyByPhoneId(int phoneId) {
        sendMessage(obtainMessage(2, Integer.valueOf(phoneId)));
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                onLoadEmergencyNumber();
                return;
            case 1:
                try {
                    onUpdateEmergencyProperties();
                    return;
                } catch (Exception e) {
                    log("onUpdateEmergencyProperties error!! ");
                    e.printStackTrace();
                    return;
                }
            case 2:
                try {
                    Integer phoneId = msg.obj;
                    log("updateEmergencyProperties by phoneId: " + phoneId);
                    updatePropertiesForPhone(phoneId.intValue());
                    return;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return;
                }
            default:
                return;
        }
    }

    public void handleMccChanged() {
        updateEmergencyProperties();
    }

    private void updatePropertiesForPhone(int phoneId) {
        HashMap<String, EmergencyNum> map;
        String key;
        EmergencyNum val;
        String emerRule;
        String displayRule;
        String propertiesEmerNum = "";
        String propertiesDisplay = "";
        GsmCdmaPhone phone = (GsmCdmaPhone) PhoneFactory.getPhone(phoneId);
        boolean isSimInsert = phone.getIccCard().hasIccCard();
        boolean isSimServiceFull = phone.mSST.mSS.getState() == 0;
        boolean isSimServiceLimited = isSimServiceFull ^ 1;
        log("updatePropertiesForPhone " + phoneId + " " + isSimServiceFull + " " + isSimServiceLimited);
        String mcc = SystemProperties.get("persist.radio.vivo.mcc", "");
        String mccMnc = "";
        if (isSimInsert) {
            String operator = phone.getOperatorNumeric();
            if (!TextUtils.isEmpty(operator) && operator.length() >= 3) {
                mcc = operator.substring(0, 3);
                mccMnc = operator;
            }
        }
        int emerFlag = -1;
        if (isSimInsert && isSimServiceFull) {
            emerFlag = 0;
        } else if (isSimInsert && isSimServiceLimited) {
            emerFlag = 1;
        } else if (!isSimInsert && isSimServiceFull) {
            emerFlag = 2;
        } else if (!isSimInsert && isSimServiceLimited) {
            emerFlag = 3;
        }
        int displayFlag = -1;
        if (isSimInsert) {
            if (isSimServiceFull) {
                displayFlag = 0;
            } else if (isSimServiceLimited) {
                displayFlag = 1;
            }
        }
        log("updatePropertiesForPhone mcc=" + mcc + " mccMnc=" + mccMnc + " emerFlag=" + emerFlag + " displayFlag=" + displayFlag);
        if (!TextUtils.isEmpty(mcc)) {
            map = (HashMap) this.mEmerMccList.get(mcc);
            if (!(map == null || map.entrySet() == null)) {
                for (Entry entry : map.entrySet()) {
                    key = (String) entry.getKey();
                    val = (EmergencyNum) entry.getValue();
                    emerRule = val.mEmerRules;
                    displayRule = val.mDisplayRules;
                    log("updatePropertiesForPhone emerRule=" + emerRule + " displayRule=" + displayRule);
                    if (emerFlag != -1 && '1' == emerRule.charAt(emerFlag)) {
                        if (TextUtils.isEmpty(propertiesEmerNum)) {
                            propertiesEmerNum = key;
                        } else {
                            propertiesEmerNum = propertiesEmerNum + "," + key;
                        }
                    }
                    if (displayFlag != -1 && '1' == displayRule.charAt(displayFlag)) {
                        if (TextUtils.isEmpty(propertiesDisplay)) {
                            propertiesDisplay = key;
                        } else {
                            propertiesDisplay = propertiesDisplay + "," + key;
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(mccMnc)) {
            map = (HashMap) this.mEmerMccMncList.get(mccMnc);
            if (!(map == null || map.entrySet() == null)) {
                for (Entry entry2 : map.entrySet()) {
                    key = (String) entry2.getKey();
                    val = (EmergencyNum) entry2.getValue();
                    emerRule = val.mEmerRules;
                    displayRule = val.mDisplayRules;
                    log("updatePropertiesForPhone mcc mnc emerRule=" + emerRule);
                    log("updatePropertiesForPhone mcc mnc displayRule=" + displayRule);
                    if (emerFlag != -1 && '1' == emerRule.charAt(emerFlag)) {
                        if (TextUtils.isEmpty(propertiesEmerNum)) {
                            propertiesEmerNum = key;
                        } else {
                            propertiesEmerNum = propertiesEmerNum + "," + key;
                        }
                    }
                    if (displayFlag != -1 && '1' == displayRule.charAt(displayFlag)) {
                        if (TextUtils.isEmpty(propertiesDisplay)) {
                            propertiesDisplay = key;
                        } else {
                            propertiesDisplay = propertiesDisplay + "," + key;
                        }
                    }
                }
            }
        }
        log("phoneId=" + phoneId + " propertiesEmerNum=" + propertiesEmerNum + " propertiesDisplay" + propertiesDisplay);
        if (phoneId == 0) {
            SystemProperties.set(PHONE_EMCC_NUM, propertiesEmerNum);
            SystemProperties.set(PHONE_DISPLAY_ONLY_NUM, propertiesDisplay);
            return;
        }
        SystemProperties.set(PHONE_EMCC_NUM + phoneId, propertiesEmerNum);
        SystemProperties.set(PHONE_DISPLAY_ONLY_NUM + phoneId, propertiesDisplay);
    }

    private void onUpdateEmergencyProperties() {
        updatePropertiesForPhone(0);
        updatePropertiesForPhone(1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0158 A:{SYNTHETIC, Splitter: B:48:0x0158} */
    /* JADX WARNING: Removed duplicated region for block: B:60:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0147 A:{SYNTHETIC, Splitter: B:42:0x0147} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onLoadEmergencyNumber() {
        XmlPullParser configParser;
        Exception ex;
        Throwable th;
        log("onLoadEmergencyNumber");
        Cursor cursor = null;
        FileReader confreader = null;
        boolean needReadLocalFile = false;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, new String[]{"TelephonyEmergencyNumber", "1", "1.0"}, null);
            if (cursor == null || cursor.getCount() <= 0) {
                log("get cursor failed");
                needReadLocalFile = true;
            } else {
                this.mEmerMccList.clear();
                this.mEmerMccMncList.clear();
                cursor.moveToFirst();
                int id = cursor.getInt(0);
                String targetIdentifer = cursor.getString(1);
                String fileVersion = cursor.getString(2);
                byte[] fileContent = cursor.getBlob(3);
                log("id=" + id + " targetIdentifer:" + targetIdentifer + " fileVersion:" + fileVersion);
                if (fileContent != null && fileContent.length > 0) {
                    String str = new String(fileContent);
                    configParser = XmlPullParserFactory.newInstance().newPullParser();
                    configParser.setInput(new StringReader(str));
                    XmlUtils.beginDocument(configParser, "emergencys");
                    loadEmerNum(configParser);
                }
            }
            updateEmergencyProperties();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            needReadLocalFile = true;
            log("query database error");
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (needReadLocalFile) {
            try {
                this.mEmerMccList.clear();
                this.mEmerMccMncList.clear();
                FileReader confreader2 = new FileReader(new File(Environment.getRootDirectory(), "etc/TelephonyEmergencyNumber.xml"));
                try {
                    configParser = Xml.newPullParser();
                    configParser.setInput(confreader2);
                    XmlUtils.beginDocument(configParser, "emergencys");
                    loadEmerNum(configParser);
                    updateEmergencyProperties();
                    confreader = confreader2;
                } catch (Exception e2) {
                    ex = e2;
                    confreader = confreader2;
                    try {
                        log("read from local xml failed");
                        ex.printStackTrace();
                        if (confreader == null) {
                            try {
                                confreader.close();
                                return;
                            } catch (Exception e3) {
                                log("confreader.close error ");
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        if (confreader != null) {
                            try {
                                confreader.close();
                            } catch (Exception e4) {
                                log("confreader.close error ");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    confreader = confreader2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                ex = e5;
                log("read from local xml failed");
                ex.printStackTrace();
                if (confreader == null) {
                }
            }
        }
        if (confreader != null) {
            try {
                confreader.close();
            } catch (Exception e6) {
                log("confreader.close error ");
            }
        }
    }

    private void loadEmerNum(XmlPullParser configParser) {
        try {
            String mcc = "";
            String mccMnc = "";
            int emergencyTable = 0;
            HashMap<String, EmergencyNum> emergencyNum = new HashMap();
            while (configParser.getEventType() != 1) {
                String parserName = configParser.getName();
                log("parserName=" + parserName);
                if ("emergency1".equals(parserName)) {
                    emergencyTable = 1;
                    XmlUtils.nextElement(configParser);
                } else if ("emergency2".equals(parserName)) {
                    emergencyTable = 2;
                    XmlUtils.nextElement(configParser);
                } else if ("mcc".equals(parserName)) {
                    mcc = configParser.getAttributeValue(null, "id");
                    XmlUtils.nextElement(configParser);
                } else if ("mccmnc".equals(parserName)) {
                    mccMnc = configParser.getAttributeValue(null, "id");
                    XmlUtils.nextElement(configParser);
                } else if (IccProvider.STR_NUMBER.equals(parserName)) {
                    String number = configParser.getAttributeValue(null, "num");
                    String emerRules = configParser.getAttributeValue(null, "emer_rule");
                    String displayRules = configParser.getAttributeValue(null, "display_rule");
                    log("number=" + number);
                    log("emerRules=" + emerRules);
                    log("displayRules=" + displayRules);
                    emergencyNum.put(number, new EmergencyNum(emerRules, displayRules));
                    XmlUtils.nextElement(configParser);
                    if (!IccProvider.STR_NUMBER.equals(configParser.getName()) || configParser.getEventType() == 1) {
                        if (emergencyTable == 1) {
                            log(" mEmerMccList.put " + mcc + " " + emergencyNum);
                            this.mEmerMccList.put(mcc, emergencyNum);
                        } else if (emergencyTable == 2) {
                            log("mEmerMccMncList.put " + mccMnc + " " + emergencyNum);
                            this.mEmerMccMncList.put(mccMnc, emergencyNum);
                        }
                        emergencyNum = new HashMap();
                    }
                } else {
                    XmlUtils.nextElement(configParser);
                }
            }
        } catch (Exception e) {
            log("loadEmerNum error ");
            e.printStackTrace();
        }
    }

    public void dump() {
        for (Entry entry : this.mEmerMccList.entrySet()) {
            log("key= " + ((String) entry.getKey()));
            for (Entry entry2 : ((HashMap) entry.getValue()).entrySet()) {
                EmergencyNum val2 = (EmergencyNum) entry2.getValue();
                log("key2= " + ((String) entry2.getKey()));
                log("mDisplayRules= " + val2.mDisplayRules);
                log("mEmerRules= " + val2.mEmerRules);
            }
        }
        dump2();
    }

    public void dump2() {
        for (Entry entry : this.mEmerMccMncList.entrySet()) {
            log("key= " + ((String) entry.getKey()));
            for (Entry entry2 : ((HashMap) entry.getValue()).entrySet()) {
                EmergencyNum val2 = (EmergencyNum) entry2.getValue();
                log("key2= " + ((String) entry2.getKey()));
                log("mDisplayRules= " + val2.mDisplayRules);
                log("mEmerRules= " + val2.mEmerRules);
            }
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }
}
