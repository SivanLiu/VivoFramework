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

public class VivoVideoBlackWhiteInfoServer {
    private static final String BLACK_WHITE_FROM_SECURE_LIST_FILE = "VivoVideoBlackWhitelist_FromSecure.xml";
    private static final String BLACK_WHITE_LIST_FILE = "VivoVideoBlackWhitelist.xml";
    public static final String MediaVivoVideoIdentifiers = "VivoVideoBlackWhitelist";
    private static final String ORIGINAL_CURRENT_REPOSITORY = "/data/audio/";
    private static final String ORIGINAL_LOCAL_REPOSITORY = "/system/etc/";
    private static final String TAG = "VivoVideoBlackWhiteInfoServer";
    public static final String secureModuleName = "VideoBlackWhiteServer";
    public static final String secureType = "1";
    public static final String secureUri = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final String secureVersion = "1.0";
    private Context mContext;
    public ArrayList<VideoList> mVideoAppList;
    public boolean mmAppListLoaded = false;

    public class BlackWhiteList implements Serializable {
        private ArrayList<VideoList> mVideoLists = new ArrayList();
        private int version = 1;

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public ArrayList<VideoList> getVideoList() {
            return this.mVideoLists;
        }

        public void addVideoList(VideoList videoList) {
            this.mVideoLists.add(videoList);
        }
    }

    public class BlackWhiteParseHandler extends DefaultHandler {
        private StringBuilder content;
        private BlackWhiteList mBlackWhiteList;
        private VideoList mVideoList;

        public BlackWhiteList getBlackWhiteLists() {
            return this.mBlackWhiteList;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            if (localName.equalsIgnoreCase("content")) {
                this.mBlackWhiteList = new BlackWhiteList();
                Log.d(VivoVideoBlackWhiteInfoServer.TAG, localName + " " + this.content.toString());
            } else if (localName.equalsIgnoreCase("application")) {
                this.mVideoList = new VideoList();
                this.mVideoList.addAppName(attributes.getValue("name"));
                Log.d(VivoVideoBlackWhiteInfoServer.TAG, localName + " " + attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("feature")) {
                this.mVideoList.addFeature(attributes.getValue("name"));
                Log.d(VivoVideoBlackWhiteInfoServer.TAG, localName + " " + attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("resolution")) {
                this.mVideoList.addResolution(attributes.getValue("name"));
                Log.d(VivoVideoBlackWhiteInfoServer.TAG, localName + " " + attributes.getValue("name"));
            } else if (localName.equalsIgnoreCase("values")) {
                this.mVideoList.addParaValue(attributes.getValue("name"));
                Log.d(VivoVideoBlackWhiteInfoServer.TAG, localName + " " + attributes.getValue("name"));
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                if (this.mBlackWhiteList != null) {
                    this.mBlackWhiteList.setVersion(Integer.valueOf(this.content.toString()).intValue());
                }
            } else if (localName.equalsIgnoreCase("application")) {
                this.mBlackWhiteList.addVideoList(this.mVideoList);
                this.mVideoList = null;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    public class VideoList {
        private String mAppName;
        private ArrayList<String> mFeature = new ArrayList();
        private ArrayList<String> mParaValue = new ArrayList();
        private ArrayList<String> mResolution = new ArrayList();

        public void addAppName(String AppName) {
            this.mAppName = AppName;
        }

        public String GetAppName() {
            return this.mAppName;
        }

        public void addFeature(String FeatureValue) {
            this.mFeature.add(FeatureValue);
        }

        public ArrayList<String> GetFeature() {
            return this.mFeature;
        }

        public void addResolution(String ResolutionValue) {
            this.mResolution.add(ResolutionValue);
        }

        public ArrayList<String> GetResolution() {
            return this.mResolution;
        }

        public void addParaValue(String ParaValue) {
            this.mParaValue.add(ParaValue);
        }

        public ArrayList<String> GetParaValue() {
            return this.mParaValue;
        }
    }

    public VivoVideoBlackWhiteInfoServer(Context context) {
        this.mContext = context;
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
                    if (blackWhiteList == null || blackWhiteList.getVideoList() == null) {
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
            Log.d(TAG, "parseInputStreamToBlackWhiteList Parse xml");
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
            FileInputStream fileInputStream2 = new FileInputStream("/system/etc/VivoVideoBlackWhitelist.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/VivoVideoBlackWhitelist.xml"));
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
            FileInputStream fileInputStream2 = new FileInputStream("/data/audio/VivoVideoBlackWhitelist_FromSecure.xml");
            try {
                fileOutputStream2 = new FileOutputStream(new File("/data/audio/VivoVideoBlackWhitelist.xml"));
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
        File file = new File("/data/audio/VivoVideoBlackWhitelist.xml");
        try {
            Process exec = Runtime.getRuntime().exec("chmod 644 /data/audio/VivoVideoBlackWhitelist.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
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

    private boolean isSecureConfigAvailable() {
        File file = new File("/data/audio/VivoVideoBlackWhitelist_FromSecure.xml");
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00bf A:{SYNTHETIC, Splitter: B:34:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getSecureConfigToWhiteListXml() {
        Exception e;
        Throwable th;
        Log.d(TAG, "get VivoVideoBlackWhitelist Config!");
        boolean result = false;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] selectionArgs = new String[]{secureModuleName, "1", "1.0", MediaVivoVideoIdentifiers};
        Cursor cursor = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File("/data/audio/VivoVideoBlackWhitelist_FromSecure.xml"));
            try {
                cursor = resolver.query(Uri.parse("content://com.vivo.daemonservice.unifiedconfigprovider/configs"), null, null, selectionArgs, null);
                if (cursor != null) {
                    Log.d(TAG, "get VivoServer uri Config!");
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
