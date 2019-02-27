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

public class BveCallModuleInfoServer {
    private static final String BVE_FROM_SECURE_LIST_FILE = "BveCallModulelist_FromSecure.xml";
    private static final String BVE_WHITE_LIST_FILE = "BVE_Call_Module.xml";
    public static final String BveIdentifiers = "BVE_Switch_ID";
    public static final String BveModuleName = "BVE_Call_Module";
    public static final String BveType = "1";
    public static final String BveUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final String BveVersion = "V1.0";
    private static final String ORIGINAL_CURRENT_REPOSITORY = "/data/audio/";
    private static final String ORIGINAL_LOCAL_REPOSITORY = "/system/etc/";
    private static final String TAG = "BveCallModuleInfoServer";
    private Context mContext;
    public ArrayList<FeatureList> mCurrentBveCallModuleFeatureList = null;

    public class BveCallModuleList implements Serializable {
        private ArrayList<FeatureList> mFeatureList = new ArrayList();
        private int version = 1;

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public ArrayList<FeatureList> getFeatureList() {
            return this.mFeatureList;
        }

        public void addPathList(FeatureList featureList) {
            this.mFeatureList.add(featureList);
        }
    }

    public class BveCallModuleParseHandler extends DefaultHandler {
        private StringBuilder content;
        private BveCallModuleList mBveCallModuleList;
        private FeatureList mFeature = null;

        public BveCallModuleList getBveCallModuleLists() {
            return this.mBveCallModuleList;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            if (localName.equalsIgnoreCase("content")) {
                this.mBveCallModuleList = new BveCallModuleList();
            } else if (localName.equalsIgnoreCase("feature")) {
                this.mFeature = new FeatureList();
                this.mFeature.setFeatureName(attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("value")) {
                this.mFeature.setFeatureValue(Integer.valueOf(attributes.getValue("name")).intValue());
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                if (this.mBveCallModuleList != null) {
                    this.mBveCallModuleList.setVersion(Integer.valueOf(this.content.toString()).intValue());
                }
            } else if (localName.equalsIgnoreCase("feature")) {
                this.mBveCallModuleList.addPathList(this.mFeature);
                this.mFeature = null;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    public class FeatureList {
        private String featureName;
        private int featureValue;

        public String getFeatureName() {
            return this.featureName;
        }

        public void setFeatureName(String name) {
            this.featureName = name;
        }

        public int getFeatureValue() {
            return this.featureValue;
        }

        public void setFeatureValue(int value) {
            this.featureValue = value;
        }
    }

    public BveCallModuleInfoServer(Context context) {
        this.mContext = context;
    }

    public void loadCurrentBveCallModuleList() {
        Log.v(TAG, "loadCurrentBveCallModuleList()");
        BveCallModuleList mBveCallModuleList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BVE_WHITE_LIST_FILE);
        if (mBveCallModuleList != null) {
            Log.d(TAG, "loadCurrentBveCallModuleList() sucess version: " + mBveCallModuleList.getVersion());
            this.mCurrentBveCallModuleFeatureList = mBveCallModuleList.getFeatureList();
            return;
        }
        Log.d(TAG, "loadCurrentBveCallModuleList() current list doesn't exist load local list");
        mBveCallModuleList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, BVE_WHITE_LIST_FILE);
        if (mBveCallModuleList != null) {
            Log.d(TAG, "loadCurrentBveCallModuleList() load local list sucess version: " + mBveCallModuleList.getVersion());
            this.mCurrentBveCallModuleFeatureList = mBveCallModuleList.getFeatureList();
        }
    }

    public boolean checkUpdateBveCallModuleList() {
        boolean result = false;
        if (!isCurrentConfigAvailable()) {
            Log.d(TAG, "checkUpdateBveCallModuleList copy list file from Original to current directory");
            copyOriginalFileToCurrentFile(this.mContext);
        }
        if (isLocalFileUpdate()) {
            Log.d(TAG, "checkUpdateBveCallModuleList copy list file from Original to current directory since local one is newer");
            copyOriginalFileToCurrentFile(this.mContext);
        }
        if (isSecureConfigAvailable()) {
            Log.d(TAG, "isSecureConfigAvailable");
            BveCallModuleList secureList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BVE_FROM_SECURE_LIST_FILE);
            BveCallModuleList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BVE_WHITE_LIST_FILE);
            if (!(secureList == null || currentList == null)) {
                Log.d(TAG, "secureList.version-->" + secureList.getVersion() + " currentList.version-->" + currentList.getVersion());
                if (secureList.getVersion() > currentList.getVersion()) {
                    copySecureFileToCurrentFile(this.mContext);
                    result = true;
                }
            }
        }
        Log.v(TAG, "checkUpdateBveCallModuleList: " + result);
        return result;
    }

    private boolean isLocalFileUpdate() {
        Log.d(TAG, "isLocalFileUpdate");
        BveCallModuleList localList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, BVE_WHITE_LIST_FILE);
        BveCallModuleList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BVE_WHITE_LIST_FILE);
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
    private BveCallModuleList parseFileToGetList(Context context, String direname, String filename) {
        FileNotFoundException e;
        Throwable th;
        Log.v(TAG, "parseFileToGetList: " + filename);
        BveCallModuleList mBveCallModuleList = null;
        InputStream inputStream = null;
        try {
            File fileCurrent = new File(direname + filename);
            if (fileCurrent.exists()) {
                InputStream inputStream2 = new FileInputStream(fileCurrent);
                try {
                    mBveCallModuleList = parseInputStreamToBveCallModuleList(inputStream2);
                    if (mBveCallModuleList == null || mBveCallModuleList.getFeatureList() == null) {
                        Log.w(TAG, filename + "can't be parsed, delete!");
                        fileCurrent.delete();
                        mBveCallModuleList = null;
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
                        return mBveCallModuleList;
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
            mBveCallModuleList = null;
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
            return mBveCallModuleList;
        }
        return mBveCallModuleList;
    }

    private BveCallModuleList parseInputStreamToBveCallModuleList(InputStream inputStream) {
        Log.v(TAG, "parseInputStreamToBveCallModuleList()");
        BveCallModuleList mBveCallModuleList = null;
        try {
            InputSource inputSource = new InputSource(new InputStreamReader(inputStream, "UTF-8"));
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            BveCallModuleParseHandler mBveCallModuleParseHandler = new BveCallModuleParseHandler();
            xmlReader.setContentHandler(mBveCallModuleParseHandler);
            xmlReader.parse(inputSource);
            return mBveCallModuleParseHandler.getBveCallModuleLists();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return mBveCallModuleList;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return mBveCallModuleList;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return mBveCallModuleList;
        } catch (IOException e4) {
            e4.printStackTrace();
            return mBveCallModuleList;
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
            FileInputStream fileInputStream2 = new FileInputStream("/system/etc/BVE_Call_Module.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/BVE_Call_Module.xml"));
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
            FileInputStream fileInputStream2 = new FileInputStream("/data/audio/BveCallModulelist_FromSecure.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/BVE_Call_Module.xml"));
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

    private boolean isCurrentConfigAvailable() {
        File file = new File("/data/audio/BVE_Call_Module.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    private boolean isSecureConfigAvailable() {
        File file = new File("/data/audio/BveCallModulelist_FromSecure.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    public ArrayList<FeatureList> getBveCallModuleList() {
        return this.mCurrentBveCallModuleFeatureList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b6 A:{SYNTHETIC, Splitter: B:34:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getBveConfig() {
        Exception e;
        Throwable th;
        Log.d(TAG, "get Bve Config!");
        boolean result = false;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{BveModuleName, "1", BveVersion, BveIdentifiers};
        Cursor cursor = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File("/data/audio/BveCallModulelist_FromSecure.xml"));
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
}
