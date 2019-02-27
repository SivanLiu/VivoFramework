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

public class ZenModeBlackWhiteInfoServer {
    private static final String BLACK_WHITE_FROM_SECURE_LIST_FILE = "ZenModeBlackWhitelist_FromSecure.xml";
    private static final String BLACK_WHITE_LIST_FILE = "ZenModeBlackWhitelist.xml";
    public static final String MediaZenModeIdentifiers = "ZenModeBlackWhitelist";
    private static final String ORIGINAL_CURRENT_REPOSITORY = "/data/audio/";
    private static final String ORIGINAL_LOCAL_REPOSITORY = "/system/etc/";
    private static final String TAG = "ZenModeBlackWhiteInfoServer";
    public static final String secureModuleName = "AudioserverZenMode";
    public static final String secureType = "1";
    public static final String secureUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final String secureVersion = "1.0";
    public ArrayList<PathList> mAppPathList;
    private Context mContext;
    public boolean mmAppPathListLoaded = false;

    public class BlackWhiteList implements Serializable {
        private ArrayList<PathList> mPathLists = new ArrayList();
        private int version = 1;

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public ArrayList<PathList> getPathList() {
            return this.mPathLists;
        }

        public void addPathList(PathList pathList) {
            this.mPathLists.add(pathList);
        }
    }

    public class BlackWhiteParseHandler extends DefaultHandler {
        private StringBuilder content;
        private BlackWhiteList mBlackWhiteList;
        private PathList mPathList;

        public BlackWhiteList getBlackWhiteLists() {
            return this.mBlackWhiteList;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            if (localName.equalsIgnoreCase("content")) {
                this.mBlackWhiteList = new BlackWhiteList();
            } else if (localName.equalsIgnoreCase("application")) {
                this.mPathList = new PathList();
                this.mPathList.setPathListName(attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("path")) {
                this.mPathList.addPaths(attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("datasize")) {
                this.mPathList.addDataSizePaths(attributes.getValue("name"));
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                if (this.mBlackWhiteList != null) {
                    this.mBlackWhiteList.setVersion(Integer.valueOf(this.content.toString()).intValue());
                }
            } else if (localName.equalsIgnoreCase("application")) {
                this.mBlackWhiteList.addPathList(this.mPathList);
                this.mPathList = null;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    public class PathList {
        private ArrayList<String> mDataSize = new ArrayList();
        private ArrayList<String> mPath = new ArrayList();
        private String mPathListName;

        public void setPathListName(String listName) {
            this.mPathListName = listName;
        }

        public String getPathListName() {
            return this.mPathListName;
        }

        public void addPaths(String path) {
            this.mPath.add(path);
        }

        public void addDataSizePaths(String path) {
            this.mDataSize.add(path);
        }

        public ArrayList<String> getPaths() {
            return this.mPath;
        }

        public ArrayList<String> getDataSizePaths() {
            return this.mDataSize;
        }
    }

    public ZenModeBlackWhiteInfoServer(Context context) {
        this.mContext = context;
    }

    public void getBlackWhiteList() {
        Log.v(TAG, "getBlackWhiteList()");
        BlackWhiteList blackWhiteList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BLACK_WHITE_LIST_FILE);
        if (blackWhiteList != null) {
            Log.d(TAG, "getblackWhiteList() version: " + blackWhiteList.getVersion());
            getFinalLists(blackWhiteList);
            return;
        }
        Log.d(TAG, "getblackWhiteList current list doesn't exist load local list");
        blackWhiteList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, BLACK_WHITE_LIST_FILE);
        if (blackWhiteList != null) {
            Log.d(TAG, "getblackWhiteList load orrginal list sucess version: " + blackWhiteList.getVersion());
            getFinalLists(blackWhiteList);
        }
    }

    public boolean checkUpdateBlackWhiteList() {
        boolean result = false;
        if (!isCurrentConfigAvailable()) {
            Log.d(TAG, "checkUpdateBlackWhiteList copy list file from Original to current directory");
            copyOriginalFileToCurrentFile(this.mContext);
        } else if (isLocalFileUpdate()) {
            Log.d(TAG, "checkUpdateBlackWhiteList copy list file from Original to current directory since local one is newer");
            copyOriginalFileToCurrentFile(this.mContext);
        }
        if (isSecureConfigAvailable()) {
            Log.d(TAG, "isSecureConfigAvailable");
            BlackWhiteList secureList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BLACK_WHITE_FROM_SECURE_LIST_FILE);
            BlackWhiteList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BLACK_WHITE_LIST_FILE);
            if (!(secureList == null || currentList == null)) {
                Log.d(TAG, "secureList.version-->" + secureList.getVersion() + " currentList.version-->" + currentList.getVersion());
                if (secureList.getVersion() > currentList.getVersion()) {
                    copySecureFileToCurrentFile(this.mContext);
                    result = true;
                }
            }
        }
        Log.v(TAG, "checkUpdateBlackWhiteList: " + result);
        return result;
    }

    private boolean isLocalFileUpdate() {
        Log.d(TAG, "isLocalFileUpdate");
        BlackWhiteList localList = parseFileToGetList(this.mContext, ORIGINAL_LOCAL_REPOSITORY, BLACK_WHITE_LIST_FILE);
        BlackWhiteList currentList = parseFileToGetList(this.mContext, ORIGINAL_CURRENT_REPOSITORY, BLACK_WHITE_LIST_FILE);
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

    private void getFinalLists(BlackWhiteList ZenmodeBlackList) {
        ArrayList<PathList> pathLists = ZenmodeBlackList.getPathList();
        if (pathLists != null) {
            for (int i = 0; i < pathLists.size(); i++) {
                PathList pathList = (PathList) pathLists.get(i);
                if (pathList != null) {
                    String listName = pathList.getPathListName();
                    if (listName != null) {
                        Log.v(TAG, "getFinalLists:" + listName);
                        this.mAppPathList = pathLists;
                        this.mmAppPathListLoaded = true;
                        return;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a6 A:{SYNTHETIC, Splitter: B:31:0x00a6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BlackWhiteList parseFileToGetList(Context context, String direname, String filename) {
        FileNotFoundException e;
        Throwable th;
        Log.v(TAG, "parseFileToGetList: " + filename);
        BlackWhiteList blackWhiteList = null;
        InputStream inputStream = null;
        try {
            File fileCurrent = new File(direname + filename);
            if (fileCurrent.exists()) {
                InputStream inputStream2 = new FileInputStream(fileCurrent);
                try {
                    blackWhiteList = parseInputStreamToBlackWhiteList(inputStream2);
                    if (blackWhiteList == null || blackWhiteList.getPathList() == null) {
                        Log.w(TAG, filename + "can't be parsed, delete!");
                        fileCurrent.delete();
                        blackWhiteList = null;
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
                        return blackWhiteList;
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
        return blackWhiteList;
    }

    private BlackWhiteList parseInputStreamToBlackWhiteList(InputStream inputStream) {
        Log.v(TAG, "parseInputStreamToBlackWhiteList()");
        BlackWhiteList blackWhiteList = null;
        try {
            InputSource inputSource = new InputSource(new InputStreamReader(inputStream, "UTF-8"));
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            BlackWhiteParseHandler blackWhiteParseHandler = new BlackWhiteParseHandler();
            xmlReader.setContentHandler(blackWhiteParseHandler);
            xmlReader.parse(inputSource);
            return blackWhiteParseHandler.getBlackWhiteLists();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return blackWhiteList;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return blackWhiteList;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return blackWhiteList;
        } catch (IOException e4) {
            e4.printStackTrace();
            return blackWhiteList;
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
            FileInputStream fileInputStream2 = new FileInputStream("/system/etc/ZenModeBlackWhitelist.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/ZenModeBlackWhitelist.xml"));
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
            FileInputStream fileInputStream2 = new FileInputStream("/data/audio/ZenModeBlackWhitelist_FromSecure.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/ZenModeBlackWhitelist.xml"));
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
        File file = new File("/data/audio/ZenModeBlackWhitelist.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    private boolean isSecureConfigAvailable() {
        File file = new File("/data/audio/ZenModeBlackWhitelist_FromSecure.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    public ArrayList<PathList> getApplicationPathList() {
        return this.mAppPathList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b6 A:{SYNTHETIC, Splitter: B:34:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getSecureConfigToWhiteListXml() {
        Exception e;
        Throwable th;
        Log.d(TAG, "get Secure Config!");
        boolean result = false;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{secureModuleName, "1", "1.0", MediaZenModeIdentifiers};
        Cursor cursor = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File("/data/audio/ZenModeBlackWhitelist_FromSecure.xml"));
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
