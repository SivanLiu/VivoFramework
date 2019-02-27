package com.vivo.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class VAPIBlackWhiteInfoServer {
    public static final String MediaVAPIIdentifiers = "VAPIBlackWhitelist";
    private static final String ORIGINAL_CURRENT_REPOSITORY = "/data/audio/";
    private static final String ORIGINAL_LOCAL_REPOSITORY = "/system/etc/";
    private static final String TAG = "VAPIBlackWhiteInfoServer";
    private static final String VAPI_Table_FILE = "VAPIBlackWhitelist.xml";
    private static final String VAPI_Table_FROM_SECURE_LIST_FILE = "VAPIBlackWhitelist_FromSecure.xml";
    public static final String secureModuleName = "AudioserverVAPI";
    public static final String secureType = "1";
    public static final String secureUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final String secureVersion = "1.0";
    private final Context mContext;
    public ArrayList<VAPIList> mCurrentVAPILists = null;
    public VAPITable mVAPITable = null;

    public class AppBlackWhiteList {
        private String app_pkg_name;
        private String app_ver;

        public String getAppName() {
            return this.app_pkg_name;
        }

        public void setAppName(String name) {
            this.app_pkg_name = name;
        }

        public String getAppVer() {
            return this.app_ver;
        }

        public void setAppVer(String ver) {
            this.app_ver = ver;
        }
    }

    public class VAPIList {
        private String api_name;
        private ArrayList<AppBlackWhiteList> mAppList = new ArrayList();

        public String get_api_name() {
            return this.api_name;
        }

        public void set_api_name(String name) {
            this.api_name = name;
        }

        public ArrayList<AppBlackWhiteList> getAppLists() {
            return this.mAppList;
        }

        public void addAppList(AppBlackWhiteList List) {
            this.mAppList.add(List);
        }
    }

    public class VAPIParseHandler extends DefaultHandler {
        private StringBuilder content;
        private AppBlackWhiteList mAppList;
        private VAPIList mVAPIList;
        private VAPITable mVAPITable;

        public VAPITable getVAPITables() {
            return this.mVAPITable;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            if (localName.equalsIgnoreCase("content")) {
                this.mVAPITable = new VAPITable();
            } else if (localName.equalsIgnoreCase("api_name")) {
                this.mVAPIList = new VAPIList();
                this.mVAPIList.set_api_name(attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("app_pkg_info")) {
                this.mAppList = new AppBlackWhiteList();
                this.mAppList.setAppName(attributes.getValue("pkg_name"));
                this.mAppList.setAppVer(attributes.getValue("app_ver"));
                this.mVAPIList.addAppList(this.mAppList);
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                if (this.mVAPITable != null) {
                    this.mVAPITable.setVersion(Integer.valueOf(this.content.toString()).intValue());
                }
            } else if (localName.equalsIgnoreCase("api_name")) {
                if (this.mVAPITable != null) {
                    this.mVAPITable.addVAPIList(this.mVAPIList);
                }
                this.mVAPIList = null;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    public class VAPITable {
        private ArrayList<VAPIList> mVAPILists = new ArrayList();
        private int mVAPITable_ver;

        public int getVersion() {
            return this.mVAPITable_ver;
        }

        public void setVersion(int ver) {
            this.mVAPITable_ver = ver;
        }

        public ArrayList<VAPIList> getVAPILists() {
            return this.mVAPILists;
        }

        public void addVAPIList(VAPIList List) {
            this.mVAPILists.add(List);
        }
    }

    public VAPIBlackWhiteInfoServer(Context context) {
        this.mContext = context;
    }

    public VAPITable getVAPITables() {
        return this.mVAPITable;
    }

    public void loadCurrentVAPITable() {
        Log.v(TAG, "loadCurrentVAPITable()");
        this.mVAPITable = parseFileToGetTable(this.mContext, ORIGINAL_CURRENT_REPOSITORY, VAPI_Table_FILE);
        if (this.mVAPITable != null) {
            Log.d(TAG, "loadCurrentVAPITable() sucess version: " + this.mVAPITable.getVersion());
            this.mCurrentVAPILists = this.mVAPITable.getVAPILists();
            return;
        }
        Log.d(TAG, "loadCurrentVAPITable() current list doesn't exist load local list");
        this.mVAPITable = parseFileToGetTable(this.mContext, ORIGINAL_LOCAL_REPOSITORY, VAPI_Table_FILE);
        if (this.mVAPITable != null) {
            Log.d(TAG, "loadCurrentVAPITable() load local list sucess version: " + this.mVAPITable.getVersion());
            this.mCurrentVAPILists = this.mVAPITable.getVAPILists();
        }
    }

    public boolean checkUpdateVAPITable() {
        boolean result = false;
        if (!isCurrentConfigAvailable()) {
            Log.d(TAG, "checkUpdateVAPITable copy list file from Original to current directory");
            copyOriginalFileToCurrentFile(this.mContext);
        } else if (isLocalFileUpdate()) {
            Log.d(TAG, "checkUpdateVAPITable copy list file from Original to current directory since local one is newer");
            copyOriginalFileToCurrentFile(this.mContext);
        }
        if (isSecureConfigAvailable()) {
            Log.d(TAG, "isSecureConfigAvailable");
            VAPITable secureList = parseFileToGetTable(this.mContext, ORIGINAL_CURRENT_REPOSITORY, VAPI_Table_FROM_SECURE_LIST_FILE);
            VAPITable currentList = parseFileToGetTable(this.mContext, ORIGINAL_CURRENT_REPOSITORY, VAPI_Table_FILE);
            if (!(secureList == null || currentList == null)) {
                Log.d(TAG, "secureList.version-->" + secureList.getVersion() + " currentList.version-->" + currentList.getVersion());
                if (secureList.getVersion() > currentList.getVersion()) {
                    copySecureFileToCurrentFile(this.mContext);
                    result = true;
                }
            }
        }
        Log.v(TAG, "checkUpdateVAPITable: " + result);
        return result;
    }

    private boolean isLocalFileUpdate() {
        Log.d(TAG, "isLocalFileUpdate");
        VAPITable localList = parseFileToGetTable(this.mContext, ORIGINAL_LOCAL_REPOSITORY, VAPI_Table_FILE);
        VAPITable currentList = parseFileToGetTable(this.mContext, ORIGINAL_CURRENT_REPOSITORY, VAPI_Table_FILE);
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

    /* JADX WARNING: Removed duplicated region for block: B:26:0x009b A:{SYNTHETIC, Splitter: B:26:0x009b} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a7 A:{SYNTHETIC, Splitter: B:32:0x00a7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private VAPITable parseFileToGetTable(Context context, String direname, String filename) {
        FileNotFoundException e;
        Throwable th;
        Log.v(TAG, "parseFileToGetTable: " + filename);
        VAPITable mVAPITable = null;
        InputStream inputStream = null;
        try {
            File fileCurrent = new File(direname + filename);
            if (fileCurrent.exists()) {
                InputStream inputStream2 = new FileInputStream(fileCurrent);
                try {
                    mVAPITable = parseInputStreamToVAPITable(inputStream2);
                    if (mVAPITable == null || mVAPITable.getVAPILists() == null) {
                        Log.w(TAG, filename + "can't be parsed, delete!");
                        fileCurrent.delete();
                        mVAPITable = null;
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
                        }
                        return mVAPITable;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
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
            mVAPITable = null;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            return mVAPITable;
        }
        return mVAPITable;
    }

    private VAPITable parseInputStreamToVAPITable(InputStream inputStream) {
        Log.v(TAG, "parseInputStreamToVAPITable()");
        VAPITable mVAPITable = null;
        try {
            InputSource inputSource = new InputSource(new InputStreamReader(inputStream, "UTF-8"));
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            VAPIParseHandler mVAPIParseHandler = new VAPIParseHandler();
            xmlReader.setContentHandler(mVAPIParseHandler);
            xmlReader.parse(inputSource);
            return mVAPIParseHandler.getVAPITables();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return mVAPITable;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return mVAPITable;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return mVAPITable;
        } catch (IOException e4) {
            e4.printStackTrace();
            return mVAPITable;
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
        Log.v(TAG, "copyOriginalFileToCurrentFile");
        boolean flag = false;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2;
            FileInputStream fileInputStream2 = new FileInputStream("/system/etc/VAPIBlackWhitelist.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/VAPIBlackWhitelist.xml"));
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
    private boolean copySecureFileToCurrentFile(Context context) {
        IOException e;
        Throwable th;
        Log.d(TAG, "copySecureFileToCurrentFile");
        boolean flag = false;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2;
            FileInputStream fileInputStream2 = new FileInputStream("/data/audio/VAPIBlackWhitelist_FromSecure.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/VAPIBlackWhitelist.xml"));
            } catch (FileNotFoundException e2) {
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileOutputStream != null) {
                }
                if (fileInputStream != null) {
                }
                return flag;
            } catch (IOException e3) {
                e = e3;
                fileInputStream = fileInputStream2;
                try {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    return flag;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileOutputStream != null) {
                }
                if (fileInputStream != null) {
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
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                flag = false;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
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
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e42222) {
                        e42222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e422222) {
                        e422222.printStackTrace();
                    }
                }
                return flag;
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = fileOutputStream2;
                fileInputStream = fileInputStream2;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4222222) {
                        e4222222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e42222222) {
                        e42222222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            flag = false;
            if (fileOutputStream != null) {
            }
            if (fileInputStream != null) {
            }
            return flag;
        } catch (IOException e8) {
            e42222222 = e8;
            e42222222.printStackTrace();
            if (fileOutputStream != null) {
            }
            if (fileInputStream != null) {
            }
            return flag;
        }
        return flag;
    }

    private boolean isCurrentConfigAvailable() {
        File file = new File("/data/audio/VAPIBlackWhitelist.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    private boolean isSecureConfigAvailable() {
        File file = new File("/data/audio/VAPIBlackWhitelist_FromSecure.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    public ArrayList<VAPIList> getVAPITable() {
        return this.mCurrentVAPILists;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b6 A:{SYNTHETIC, Splitter: B:34:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getSecureConfigToVAPITable() {
        Exception e;
        Throwable th;
        Log.d(TAG, "get Secure Config!");
        boolean result = false;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{secureModuleName, "1", "1.0", MediaVAPIIdentifiers};
        Cursor cursor = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File("/data/audio/VAPIBlackWhitelist_FromSecure.xml"));
            try {
                cursor = resolver.query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, selectionArgs, null);
                if (cursor != null) {
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

    public boolean checkConfig() {
        String unSecure = SystemProperties.get("persist.vivo.unifiedconfig.sec", "yes");
        Log.d(TAG, "unSecure = " + unSecure);
        if (unSecure == null || ("".equals(unSecure) ^ 1) == 0) {
            return false;
        }
        if (unSecure.equals("yes")) {
            Log.d(TAG, "use Secure new method!");
            return true;
        }
        Log.d(TAG, "use Secure old method!");
        return false;
    }
}
