package com.vivo.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class MotorModeWhiteInfoServer {
    public static final String MotorModeIdentifiers = "MotorModeWhitelist";
    public static final String MotorModeModuleName = "MotorMode";
    public static final String MotorModeType = "1";
    public static final String MotorModeUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final String MotorModeVersion = "1.0";
    private static final String ORIGINAL_CURRENT_REPOSITORY = "/data/audio/";
    private static final String ORIGINAL_LOCAL_REPOSITORY = "/system/etc/";
    private static final String TAG = "MotorModeWhiteInfoServer";
    private static final String WHITE_FROM_SECURE_LIST_FILE = "MotorModeWhitelist_FromSecure.xml";
    private static final String WHITE_LIST_FILE = "MotorModeWhitelist.xml";
    private Context mContext;
    public ArrayList<AppList> mCurrentMotorModeAppList;

    public class AppList {
        private String mAppName;

        public void addAppName(String AppName) {
            this.mAppName = AppName;
        }

        public String GetAppName() {
            return this.mAppName;
        }
    }

    public class WhiteList implements Serializable {
        private ArrayList<AppList> mMotorModeLists = new ArrayList();
        private int version = 1;

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public ArrayList<AppList> getMotorModeList() {
            return this.mMotorModeLists;
        }

        public void addMotorModeList(AppList motorModeList) {
            this.mMotorModeLists.add(motorModeList);
        }
    }

    public class WhiteParseHandler extends DefaultHandler {
        private AppList appList;
        private StringBuilder content;
        private WhiteList mWhiteList;

        public WhiteList getWhiteLists() {
            return this.mWhiteList;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            Log.v(MotorModeWhiteInfoServer.TAG, "startElement() localNmae is :" + localName);
            if (localName.equalsIgnoreCase("content")) {
                this.mWhiteList = new WhiteList();
                Log.d(MotorModeWhiteInfoServer.TAG, localName + " " + this.content.toString());
            } else if (localName.equalsIgnoreCase("application")) {
                this.appList = new AppList();
                this.appList.addAppName(attributes.getValue("name"));
                Log.d(MotorModeWhiteInfoServer.TAG, localName + " " + attributes.getValue("name"));
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                if (this.mWhiteList != null) {
                    this.mWhiteList.setVersion(Integer.valueOf(this.content.toString()).intValue());
                }
            } else if (localName.equalsIgnoreCase("application")) {
                this.mWhiteList.addMotorModeList(this.appList);
                this.appList = null;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    public MotorModeWhiteInfoServer(Context context) {
        this.mContext = context;
    }

    public void loadCurrentMotorModeList() {
        WhiteList whiteList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, WHITE_LIST_FILE);
        if (whiteList != null) {
            Log.v(TAG, "getwhiteList() version: " + whiteList.getVersion());
            this.mCurrentMotorModeAppList = whiteList.getMotorModeList();
            return;
        }
        Log.v(TAG, "getwhiteList current list doesn't exist load local list");
        whiteList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, WHITE_LIST_FILE);
        if (whiteList != null) {
            Log.v(TAG, "getwhiteList load orrginal list sucess version: " + whiteList.getVersion());
            this.mCurrentMotorModeAppList = whiteList.getMotorModeList();
        }
    }

    public ArrayList<AppList> getMotorModeList() {
        return this.mCurrentMotorModeAppList;
    }

    public boolean checkUpdateWhiteList() {
        boolean result = false;
        if (!isCurrentConfigAvailable()) {
            Log.d(TAG, "checkUpdateWhiteList copy list file from Original to current directory");
            copyOriginalFileToCurrentFile(this.mContext);
        } else if (isLocalFileUpdate()) {
            Log.d(TAG, "checkUpdateWhiteList copy list file from Original to current directory since local one is newer");
            copyOriginalFileToCurrentFile(this.mContext);
        }
        if (isSecureConfigAvailable()) {
            Log.d(TAG, "isSecureConfigAvailable");
            WhiteList secureList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, WHITE_FROM_SECURE_LIST_FILE);
            WhiteList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, WHITE_LIST_FILE);
            if (!(secureList == null || currentList == null)) {
                Log.d(TAG, "secureList.version-->" + secureList.getVersion() + " currentList.version-->" + currentList.getVersion());
                if (secureList.getVersion() > currentList.getVersion()) {
                    copySecureFileToCurrentFile(this.mContext);
                    result = true;
                }
            }
        }
        Log.v(TAG, "checkUpdateWhiteList: " + result);
        return result;
    }

    private boolean isLocalFileUpdate() {
        Log.d(TAG, "isLocalFileUpdate");
        WhiteList localList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, WHITE_LIST_FILE);
        WhiteList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, WHITE_LIST_FILE);
        if (localList == null || currentList != null) {
            if (!(localList == null || currentList == null)) {
                Log.d(TAG, "localList.version-->" + localList.getVersion() + " currentList.version-->" + currentList.getVersion());
                if (localList.getVersion() > currentList.getVersion()) {
                    return true;
                }
            }
            return false;
        }
        Log.d(TAG, "isLocalFileUpdate return true since currentList is null");
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a6 A:{SYNTHETIC, Splitter: B:31:0x00a6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private WhiteList parseFileToGetList(Context context, String direname, String filename) {
        FileNotFoundException e;
        Throwable th;
        Log.v(TAG, "parseFileToGetList: " + filename);
        WhiteList whiteList = null;
        InputStream inputStream = null;
        try {
            File fileCurrent = new File(direname + filename);
            if (fileCurrent.exists()) {
                InputStream inputStream2 = new FileInputStream(fileCurrent);
                try {
                    whiteList = parseInputStreamToWhiteList(inputStream2);
                    if (whiteList == null || whiteList.getMotorModeList() == null) {
                        Log.w(TAG, filename + "can't be parsed, delete!");
                        fileCurrent.delete();
                        whiteList = null;
                        inputStream = inputStream2;
                    } else {
                        inputStream = inputStream2;
                    }
                } catch (FileNotFoundException e2) {
                    e = e2;
                    inputStream = inputStream2;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return whiteList;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            }
            Log.d(TAG, "current dir doesn't have  " + direname + filename);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (FileNotFoundException e4) {
            e = e4;
        }
        return whiteList;
    }

    private WhiteList parseInputStreamToWhiteList(InputStream inputStream) {
        Log.v(TAG, "parseInputStreamToWhiteList()");
        WhiteList whiteList = null;
        try {
            InputSource inputSource = new InputSource(new InputStreamReader(inputStream, "UTF-8"));
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            WhiteParseHandler whiteParseHandler = new WhiteParseHandler();
            xmlReader.setContentHandler(whiteParseHandler);
            xmlReader.parse(inputSource);
            Log.d(TAG, "parseInputStreamToWhiteList Parse xml");
            return whiteParseHandler.getWhiteLists();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return whiteList;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return whiteList;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return whiteList;
        } catch (IOException e4) {
            e4.printStackTrace();
            return whiteList;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0037 A:{SYNTHETIC, Splitter: B:16:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0061 A:{SYNTHETIC, Splitter: B:40:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0066 A:{SYNTHETIC, Splitter: B:43:0x0066} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0037 A:{SYNTHETIC, Splitter: B:16:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0061 A:{SYNTHETIC, Splitter: B:40:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0066 A:{SYNTHETIC, Splitter: B:43:0x0066} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0081 A:{SYNTHETIC, Splitter: B:55:0x0081} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0086 A:{SYNTHETIC, Splitter: B:58:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0081 A:{SYNTHETIC, Splitter: B:55:0x0081} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0086 A:{SYNTHETIC, Splitter: B:58:0x0086} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean copyOriginalFileToCurrentFile(Context context) {
        IOException e;
        Throwable th;
        Log.e(TAG, "copyOriginalFileToCurrentFile");
        boolean flag = false;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2;
            FileInputStream fileInputStream2 = new FileInputStream("/system/etc/MotorModeWhitelist.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/MotorModeWhitelist.xml"));
            } catch (FileNotFoundException e2) {
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                return flag;
            } catch (IOException e3) {
                e = e3;
                fileInputStream = fileInputStream2;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    return flag;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                throw th;
            }
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int count = fileInputStream2.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    fileOutputStream2.write(buffer, 0, count);
                }
                fileOutputStream2.flush();
                flag = true;
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4222) {
                        e4222.printStackTrace();
                    }
                }
                return flag;
            } catch (IOException e6) {
                e4222 = e6;
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                e4222.printStackTrace();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e42222) {
                        e42222.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e422222) {
                        e422222.printStackTrace();
                    }
                }
                return flag;
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4222222) {
                        e4222222.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e42222222) {
                        e42222222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            flag = false;
            if (fileInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            return flag;
        } catch (IOException e8) {
            e42222222 = e8;
            e42222222.printStackTrace();
            if (fileInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            return flag;
        }
        return flag;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0037 A:{SYNTHETIC, Splitter: B:16:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x006a A:{SYNTHETIC, Splitter: B:39:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006f A:{SYNTHETIC, Splitter: B:42:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x008a A:{SYNTHETIC, Splitter: B:54:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x008f A:{SYNTHETIC, Splitter: B:57:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x008a A:{SYNTHETIC, Splitter: B:54:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x008f A:{SYNTHETIC, Splitter: B:57:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0037 A:{SYNTHETIC, Splitter: B:16:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x006a A:{SYNTHETIC, Splitter: B:39:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006f A:{SYNTHETIC, Splitter: B:42:0x006f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean copySecureFileToCurrentFile(Context context) {
        IOException e;
        Throwable th;
        Log.d(TAG, "copySecureFileToCurrentFile");
        boolean flag = false;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2;
            FileInputStream fileInputStream2 = new FileInputStream("/data/audio/MotorModeWhitelist_FromSecure.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/MotorModeWhitelist.xml"));
            } catch (FileNotFoundException e2) {
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return flag;
            } catch (IOException e4) {
                e32 = e4;
                fileInputStream = fileInputStream2;
                try {
                    e32.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    return flag;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
                throw th;
            }
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int count = fileInputStream2.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    fileOutputStream2.write(buffer, 0, count);
                }
                fileOutputStream2.flush();
                flag = true;
                Log.d(TAG, "copySecureFileToCurrentFile sucess");
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e3222222) {
                        e3222222.printStackTrace();
                    }
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e32222222) {
                        e32222222.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                return flag;
            } catch (IOException e6) {
                e32222222 = e6;
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                e32222222.printStackTrace();
                if (fileInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                return flag;
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            flag = false;
            if (fileInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            return flag;
        } catch (IOException e8) {
            e32222222 = e8;
            e32222222.printStackTrace();
            if (fileInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            return flag;
        }
        return flag;
    }

    private boolean isCurrentConfigAvailable() {
        File file = new File("/data/audio/MotorModeWhitelist.xml");
        try {
            Process exec = Runtime.getRuntime().exec("chmod 644 /data/audio/MotorModeWhitelist.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    private boolean isSecureConfigAvailable() {
        File file = new File("/data/audio/MotorModeWhitelist_FromSecure.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b6 A:{SYNTHETIC, Splitter: B:34:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getSecureConfigToWhiteListXml() {
        Exception e;
        Throwable th;
        boolean result = false;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{MotorModeModuleName, "1", "1.0", MotorModeIdentifiers};
        Cursor cursor = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File("/data/audio/MotorModeWhitelist_FromSecure.xml"));
            try {
                cursor = resolver.query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, selectionArgs, null);
                if (cursor != null) {
                    Log.d(TAG, "get MotorModeServer uri Config!");
                    String targetIdentifier = "";
                    String fileVersion = "";
                    cursor.moveToFirst();
                    if (cursor.getCount() > 0) {
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getInt(0);
                            targetIdentifier = cursor.getString(1);
                            fileVersion = cursor.getString(2);
                            fileOutputStream2.write(cursor.getBlob(3));
                            cursor.moveToNext();
                        }
                        result = true;
                    } else {
                        Log.d(TAG, "no data!");
                    }
                } else {
                    Log.d(TAG, "cursor is null, lock failed, continue checking for update!");
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e3) {
                e = e3;
                fileOutputStream = fileOutputStream2;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fileOutputStream2;
                if (fileOutputStream != null) {
                }
                if (cursor != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            try {
                Log.d(TAG, "open database error!");
                e.printStackTrace();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        return result;
    }
}
