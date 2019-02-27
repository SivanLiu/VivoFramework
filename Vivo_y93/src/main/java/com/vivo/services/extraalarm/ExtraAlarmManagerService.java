package com.vivo.services.extraalarm;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.FtBuild;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.VivoCustomUtils;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ExtraAlarmManagerService {
    private static boolean IS_LOG_CTRL_OPEN = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
    private static final String TAG = "ExtraAlarmManager";
    private static final int[] UID_WHITE_LIST = new int[]{Weather.WEATHERVERSION_ROM_2_0, ExceptionCode.NET_UNCONNECTED, 1002};
    private static final boolean isUnifiedConfig = SystemProperties.get("persist.vivo.unifiedconfig.sec", "no").equals("yes");
    private String[] ACTION_NAME_BLACK_LIST;
    private String[] ACTION_NAME_WHITE_LIST;
    private String DECRYPT_KEY = "vivo@szbg2014666";
    private String[] PACKAGE_NAME_BLACK_LIST;
    private String[] PACKAGE_NAME_WHITE_LIST;
    private final Context mContext;
    private ArrayList<String> mPackageWhiteList = new ArrayList();
    private String pkgPath = "/data/bbkcore/package_white_black_list.xml";

    public ExtraAlarmManagerService(Context context) {
        int i;
        Log.i(TAG, "Init the ExtraAlarmManagerService");
        this.mContext = context;
        this.PACKAGE_NAME_WHITE_LIST = this.mContext.getResources().getStringArray(50923283);
        if (this.PACKAGE_NAME_WHITE_LIST != null) {
            for (Object add : this.PACKAGE_NAME_WHITE_LIST) {
                this.mPackageWhiteList.add(add);
            }
        }
        if (!SystemProperties.get("ro.build.gn.support", "0").equals("0")) {
            List<String> mCustomList = VivoCustomUtils.getCustomizedApps(0);
            if (mCustomList != null && mCustomList.size() > 0) {
                for (i = 0; i < mCustomList.size(); i++) {
                    if (!this.mPackageWhiteList.contains(mCustomList.get(i))) {
                        this.mPackageWhiteList.add((String) mCustomList.get(i));
                    }
                }
            }
        }
        this.PACKAGE_NAME_WHITE_LIST = (String[]) this.mPackageWhiteList.toArray(this.PACKAGE_NAME_WHITE_LIST);
        IS_LOG_CTRL_OPEN = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
        updatePackageList();
    }

    public void updatePackageList() {
        if (!FtBuild.isOverSeas()) {
            Log.i(TAG, "update List ...");
            if (!isUnifiedConfig) {
                try {
                    File file = new File(this.pkgPath);
                    if (file.exists()) {
                        ReadXmlbyPullServeice(new FileInputStream(file));
                    } else {
                        Log.i(TAG, "Use the default package list");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (SystemProperties.get("sys.boot_completed", "0").equals("1")) {
                readAlarmFileFromUC();
            }
        }
    }

    public void updatePackageList(PendingIntent operation) {
        if (isUnifiedConfig) {
            Intent intent = operation.getIntent();
            if ("com.vivo.daemonService.uc.read.alarm".equals(intent != null ? intent.getAction() : null)) {
                updatePackageList();
                return;
            }
            return;
        }
        updatePackageList();
    }

    private void ReadXmlbyPullServeice(InputStream inputStream) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(inputStream, "utf-8");
            ArrayList tmpPkg = null;
            String whitePkg = null;
            String blackPkg = null;
            String whiteAction = null;
            String blackAction = null;
            for (int eventCode = parser.getEventType(); eventCode != 1; eventCode = parser.next()) {
                switch (eventCode) {
                    case 0:
                        tmpPkg = new ArrayList();
                        break;
                    case 2:
                        String name = parser.getName();
                        if (!"resource".equals(name)) {
                            if (!"string-array".equals(name) || !"white_list".equals(parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name"))) {
                                if (!"string-array".equals(name) || !"black_list".equals(parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name"))) {
                                    if (!"string-array".equals(name) || !"white_list_action".equals(parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name"))) {
                                        if (!"string-array".equals(name) || !"black_list_action".equals(parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name"))) {
                                            if (!"item".equals(name)) {
                                                break;
                                            }
                                            tmpPkg.add(parser.nextText());
                                            break;
                                        }
                                        blackAction = parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name");
                                        break;
                                    }
                                    whiteAction = parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name");
                                    break;
                                }
                                blackPkg = parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name");
                                break;
                            }
                            whitePkg = parser.getAttributeValue(Events.DEFAULT_SORT_ORDER, "name");
                            break;
                        }
                        String tmp = parser.nextText();
                        break;
                        break;
                    case 3:
                        if (whitePkg != null) {
                            String[] whiteArray = (String[]) tmpPkg.toArray(new String[tmpPkg.size()]);
                            for (int i = 0; i < whiteArray.length; i++) {
                                if (!this.mPackageWhiteList.contains(whiteArray[i])) {
                                    this.mPackageWhiteList.add(whiteArray[i]);
                                }
                            }
                            this.PACKAGE_NAME_WHITE_LIST = (String[]) this.mPackageWhiteList.toArray(this.PACKAGE_NAME_WHITE_LIST);
                            whitePkg = null;
                            tmpPkg.clear();
                        }
                        if (blackPkg != null) {
                            this.PACKAGE_NAME_BLACK_LIST = new String[tmpPkg.size()];
                            this.PACKAGE_NAME_BLACK_LIST = (String[]) tmpPkg.toArray(this.PACKAGE_NAME_BLACK_LIST);
                            blackPkg = null;
                            tmpPkg.clear();
                        }
                        if (whiteAction != null) {
                            this.ACTION_NAME_WHITE_LIST = new String[tmpPkg.size()];
                            this.ACTION_NAME_WHITE_LIST = (String[]) tmpPkg.toArray(this.ACTION_NAME_WHITE_LIST);
                            whiteAction = null;
                            tmpPkg.clear();
                        }
                        if (blackAction == null) {
                            break;
                        }
                        this.ACTION_NAME_BLACK_LIST = new String[tmpPkg.size()];
                        this.ACTION_NAME_BLACK_LIST = (String[]) tmpPkg.toArray(this.ACTION_NAME_BLACK_LIST);
                        blackAction = null;
                        tmpPkg.clear();
                        break;
                    default:
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void readAlarmFileFromUC() {
        byte[] filecontent = readDataFromUC();
        if (filecontent != null) {
            byte[] data = null;
            try {
                data = decrypt(filecontent, this.DECRYPT_KEY.getBytes());
            } catch (Exception e) {
                Log.d(TAG, "decrypt Exception: " + e);
                e.printStackTrace();
            }
            if (data != null) {
                ReadXmlbyPullServeice(new ByteArrayInputStream(data));
                this.mContext.sendBroadcast(new Intent("com.vivo.daemonService.uc.read.alarm.ok"));
                Log.d(TAG, "read config sucess");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b1 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0090 A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readDataFromUC() {
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{"AlarmManager", "1", "1.0", "alarmwake"};
        Cursor cursor = null;
        String uri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
        String fileId = Events.DEFAULT_SORT_ORDER;
        String targetIdentifier = Events.DEFAULT_SORT_ORDER;
        String fileVersion = Events.DEFAULT_SORT_ORDER;
        byte[] filecontent = null;
        try {
            cursor = resolver.query(Uri.parse(uri), null, null, selectionArgs, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        fileId = cursor.getString(cursor.getColumnIndex("id"));
                        targetIdentifier = cursor.getString(cursor.getColumnIndex("identifier"));
                        fileVersion = cursor.getString(cursor.getColumnIndex("fileversion"));
                        filecontent = cursor.getBlob(cursor.getColumnIndex("filecontent"));
                        cursor.moveToNext();
                    }
                } else {
                    Log.d(TAG, "no data!");
                }
            } else {
                Log.d(TAG, "cursor is null, lock failed, continue checking for update!");
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "open database error!");
            e.printStackTrace();
            if (filecontent == null) {
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (filecontent == null) {
            return filecontent;
        }
        return null;
    }

    private byte[] decrypt(byte[] data, byte[] key) throws Exception {
        Key k = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(2, k);
        return cipher.doFinal(data);
    }

    public int convertType(int type, long triggerAtTime, long interval, PendingIntent operation) {
        int retType = type;
        if (FtBuild.isOverSeas()) {
            return type;
        }
        if ((type == 0 || type == 2) && operation != null) {
            int j = Binder.getCallingUid();
            String packageName = operation.getTargetPackage();
            Log.i(TAG, "Uid : " + j + ", Package name is : " + packageName);
            Intent intent = null;
            long identity = Binder.clearCallingIdentity();
            try {
                intent = operation.getIntent();
                String actionName = intent != null ? intent.getAction() : null;
                Object compName = intent != null ? intent.getComponent() : null;
                if (!inActionNameWhiteList(actionName)) {
                    if (inActionNameBlackList(actionName)) {
                        if (type == 2) {
                            retType = 3;
                        } else if (type == 0) {
                            retType = 1;
                        }
                    } else if (!(inUidWhiteList(j) || (inPackageNameWhiteList(packageName) ^ 1) == 0)) {
                        if (type == 2) {
                            retType = 3;
                        } else if (type == 0) {
                            retType = 1;
                        }
                    }
                }
                if (IS_LOG_CTRL_OPEN) {
                    String triggerAtDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(triggerAtTime));
                    if (type == 2) {
                        triggerAtDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((System.currentTimeMillis() + triggerAtTime) - SystemClock.elapsedRealtime()));
                    }
                    Log.i(TAG, "triggerAtTime = " + triggerAtDate + " type = " + type + " Type = " + retType + " package = " + packageName + " action = " + actionName + " ComponentName = " + compName);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        Log.i(TAG, "type: " + type + " " + "Type: " + retType);
        return retType;
    }

    private boolean inUidWhiteList(int i) {
        for (int i2 : UID_WHITE_LIST) {
            if (i == i2) {
                Log.d(TAG, "UID : " + i + " is the uid");
                return true;
            }
        }
        return false;
    }

    private boolean inPackageNameWhiteList(String packageName) {
        if (!(packageName == null || this.PACKAGE_NAME_WHITE_LIST == null)) {
            if (packageName.toLowerCase().contains("clock") || packageName.equals("com.android.systemui")) {
                Log.d(TAG, packageName + ": is package");
                return true;
            }
            for (Object equals : this.PACKAGE_NAME_WHITE_LIST) {
                if (packageName.equals(equals)) {
                    Log.d(TAG, packageName + ": is package");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inActionNameWhiteList(String actionName) {
        if (!(this.ACTION_NAME_WHITE_LIST == null || actionName == null)) {
            for (Object equals : this.ACTION_NAME_WHITE_LIST) {
                if (actionName.equals(equals)) {
                    Log.d(TAG, actionName + ": is the action");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inActionNameBlackList(String actionName) {
        if (!(this.ACTION_NAME_BLACK_LIST == null || actionName == null)) {
            for (Object equals : this.ACTION_NAME_BLACK_LIST) {
                if (actionName.equals(equals)) {
                    Log.d(TAG, actionName + ": is in the action");
                    return true;
                }
            }
        }
        return false;
    }
}
