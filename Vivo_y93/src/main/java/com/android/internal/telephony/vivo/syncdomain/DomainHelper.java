package com.android.internal.telephony.vivo.syncdomain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.radio.V1_0.RadioAccessFamily;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainHelper {
    private static final String ACTION_DOMAIN_CHANGE = "com.vivo.domainsync.action.DOMAINS_UPDATE";
    private static final String DEFAULT_DOMAIN_PATH = "oem/etc/domains/";
    private static final String DEFAULT_HOST = "";
    private static final String KEY_KEY = "key";
    private static final String KEY_METADATA = "metadatas";
    private static final String KEY_VALUE = "value";
    private static final String SP_KEY_CRC_PREF = "sp_key_crc_";
    private static final String SP_VIVO_DAMONS_DOMAIN_CACHE = "sp_vivo_damons_domain_cache";
    private static final String TAG = DomainHelper.class.getSimpleName();
    private static final String VERSION_NAME = "3.0.0";
    private static final String VIVO_DAMONS_DOMAIN_PATH = "data/bbkcore/domains/";
    private static DomainHelper mDomainHelper = null;
    private static boolean sInSystemServer = false;
    private Context mContext;
    private DomainChangeReceiver mDomainChangeReceiver;
    private Map<String, List<DomainRepo>> mDomainRepos;

    private interface DomainRepo {
        String getDomain(String str);

        boolean isValid();
    }

    private abstract class BaseDomainRepo implements DomainRepo {
        String mPackageName;

        public BaseDomainRepo(String packageName) {
            this.mPackageName = packageName;
        }
    }

    class DomainChangeReceiver extends BroadcastReceiver {
        DomainChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(DomainHelper.TAG, "receive domain change broadcast");
            DomainHelper.this.clearData();
        }
    }

    private class LocalDefaultRepo extends BaseDomainRepo implements DomainRepo {
        private Map<String, String> localDefaultMap;

        public LocalDefaultRepo(String packageName) {
            super(packageName);
        }

        public boolean isValid() {
            if (this.localDefaultMap != null || new File(DomainHelper.DEFAULT_DOMAIN_PATH + this.mPackageName).exists()) {
                return true;
            }
            return false;
        }

        private void confirmLocalDefaultMap() {
            if (this.localDefaultMap == null) {
                this.localDefaultMap = new HashMap();
                byte[] buff = DomainHelper.this.readFile(new File(DomainHelper.DEFAULT_DOMAIN_PATH + this.mPackageName));
                if (buff == null) {
                    Log.e(DomainHelper.TAG, "read oem default error");
                    return;
                }
                try {
                    Map<String, String> content = DomainHelper.this.parseContent(buff);
                    if (content != null) {
                        this.localDefaultMap.putAll(content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public String getDomain(String key) {
            confirmLocalDefaultMap();
            String value = (String) this.localDefaultMap.get(key);
            if (TextUtils.isEmpty(value)) {
                return DomainHelper.DEFAULT_HOST;
            }
            return value;
        }
    }

    private class LocalVivoDamonsRepo extends BaseDomainRepo implements DomainRepo {
        private boolean isReadFromVivoDamons = false;
        private Map<String, ?> localVivoDamonsMap;

        public LocalVivoDamonsRepo(String packageName) {
            super(packageName);
        }

        public boolean isValid() {
            confirmReadVivoDamonFile();
            return true;
        }

        public String getDomain(String key) {
            SharedPreferences sp = DomainHelper.this.mContext.getSharedPreferences(DomainHelper.SP_VIVO_DAMONS_DOMAIN_CACHE, 0);
            if (this.localVivoDamonsMap == null) {
                this.localVivoDamonsMap = sp.getAll();
            }
            Object value = this.localVivoDamonsMap.get(key);
            if (value == null) {
                return DomainHelper.DEFAULT_HOST;
            }
            if (value instanceof String) {
                return (String) value;
            }
            return DomainHelper.DEFAULT_HOST;
        }

        private void confirmReadVivoDamonFile() {
            if (!this.isReadFromVivoDamons) {
                this.isReadFromVivoDamons = true;
                File dir = new File(DomainHelper.VIVO_DAMONS_DOMAIN_PATH);
                File vivoDomainFile = null;
                if (dir.exists()) {
                    File[] files = dir.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            boolean z = false;
                            if (pathname == null) {
                                return false;
                            }
                            String path = pathname.getPath();
                            if (!TextUtils.isEmpty(path)) {
                                z = path.endsWith(LocalVivoDamonsRepo.this.mPackageName);
                            }
                            return z;
                        }
                    });
                    if (files != null && files.length > 0) {
                        vivoDomainFile = files[0];
                    }
                }
                if (vivoDomainFile != null && vivoDomainFile.exists()) {
                    String fileName = vivoDomainFile.getName();
                    int indexOfPackage = fileName.lastIndexOf(this.mPackageName);
                    if (indexOfPackage > 0) {
                        try {
                            SharedPreferences sp = DomainHelper.this.mContext.getSharedPreferences(DomainHelper.SP_VIVO_DAMONS_DOMAIN_CACHE, 0);
                            String fileNameCrc = fileName.substring(0, indexOfPackage);
                            if (sp.getString(DomainHelper.SP_KEY_CRC_PREF + this.mPackageName, DomainHelper.DEFAULT_HOST).equals(fileNameCrc)) {
                                Log.i(DomainHelper.TAG, "skip read vivo damons file");
                                return;
                            }
                            byte[] buff = DomainHelper.this.readFile(vivoDomainFile);
                            if (buff == null) {
                                Log.e(DomainHelper.TAG, "read vivoDomainFile error");
                                return;
                            }
                            new CRC32().update(buff);
                            String fileContentCrc = String.format("%08x", new Object[]{Long.valueOf(crc32.getValue())});
                            if (fileNameCrc.equals(fileContentCrc)) {
                                Map<String, String> keyValues = DomainHelper.this.parseContent(buff);
                                if (keyValues != null && keyValues.size() > 0) {
                                    Editor editor = sp.edit();
                                    editor.putString(DomainHelper.SP_KEY_CRC_PREF + this.mPackageName, fileContentCrc);
                                    for (Entry<String, String> entry : keyValues.entrySet()) {
                                        editor.putString((String) entry.getKey(), (String) entry.getValue());
                                    }
                                    if (!editor.commit()) {
                                        Log.e(DomainHelper.TAG, "commit failed!");
                                    }
                                }
                            } else {
                                Log.e(DomainHelper.TAG, "skip read vivo damons file");
                            }
                        } catch (Exception e) {
                            Log.e(DomainHelper.TAG, "read or parse error", e);
                        }
                    }
                }
            }
        }
    }

    private class LocalVivoDamonsRepoForSystemServer extends BaseDomainRepo implements DomainRepo {
        private Map<String, String> localVivoDamonsForSSMap;

        public LocalVivoDamonsRepoForSystemServer(String packageName) {
            super(packageName);
        }

        public boolean isValid() {
            confirmReadVivoDamonFile();
            return true;
        }

        public String getDomain(String key) {
            Object value = this.localVivoDamonsForSSMap.get(key);
            if (value == null) {
                return DomainHelper.DEFAULT_HOST;
            }
            if (value instanceof String) {
                return (String) value;
            }
            return DomainHelper.DEFAULT_HOST;
        }

        private void confirmReadVivoDamonFile() {
            if (this.localVivoDamonsForSSMap == null) {
                this.localVivoDamonsForSSMap = new HashMap();
                File dir = new File(DomainHelper.VIVO_DAMONS_DOMAIN_PATH);
                File vivoDomainFile = null;
                if (dir.exists()) {
                    File[] files = dir.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            boolean z = false;
                            if (pathname == null) {
                                return false;
                            }
                            String path = pathname.getPath();
                            if (!TextUtils.isEmpty(path)) {
                                z = path.endsWith(LocalVivoDamonsRepoForSystemServer.this.mPackageName);
                            }
                            return z;
                        }
                    });
                    if (files != null && files.length > 0) {
                        vivoDomainFile = files[0];
                    }
                }
                if (vivoDomainFile != null && vivoDomainFile.exists()) {
                    String fileName = vivoDomainFile.getName();
                    int indexOfPackage = fileName.lastIndexOf(this.mPackageName);
                    if (indexOfPackage > 0) {
                        try {
                            byte[] buff = DomainHelper.this.readFile(vivoDomainFile);
                            if (buff == null) {
                                Log.e(DomainHelper.TAG, "read vivoDomainFile error");
                                return;
                            }
                            new CRC32().update(buff);
                            if (fileName.substring(0, indexOfPackage).equals(String.format("%08x", new Object[]{Long.valueOf(crc32.getValue())}))) {
                                Map<String, String> keyValues = DomainHelper.this.parseContent(buff);
                                if (keyValues != null) {
                                    this.localVivoDamonsForSSMap.putAll(keyValues);
                                }
                            } else {
                                Log.e(DomainHelper.TAG, "skip read vivo damons file");
                            }
                        } catch (Exception e) {
                            Log.e(DomainHelper.TAG, "read or parse error", e);
                        }
                    }
                }
            }
        }
    }

    public static DomainHelper getInstance() {
        if (mDomainHelper == null) {
            synchronized (DomainHelper.class) {
                if (mDomainHelper == null) {
                    mDomainHelper = new DomainHelper();
                }
            }
        }
        return mDomainHelper;
    }

    private DomainHelper() {
    }

    public void init(Context ctx) {
        init(ctx, false, false);
    }

    public void init(Context ctx, boolean daemon) {
        init(ctx, daemon, false);
    }

    public void init(Context ctx, boolean daemon, boolean inSystemServer) {
        sInSystemServer = inSystemServer;
        if (ctx == null) {
            Log.e(TAG, "ctx is null when init");
            return;
        }
        if (VERSION.SDK_INT >= 24) {
            this.mContext = ctx.getApplicationContext().createDeviceProtectedStorageContext();
        } else {
            this.mContext = ctx.getApplicationContext();
        }
        if (daemon) {
            if (this.mDomainChangeReceiver == null) {
                this.mDomainChangeReceiver = new DomainChangeReceiver();
            } else {
                try {
                    this.mContext.unregisterReceiver(this.mDomainChangeReceiver);
                } catch (Exception e) {
                    Log.e(TAG, "unregisterReceiver fatal! " + e.toString());
                }
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DOMAIN_CHANGE);
            this.mContext.registerReceiver(this.mDomainChangeReceiver, filter);
        }
    }

    public String getDomain(String key, String defaultHost) {
        return getDomain(key, defaultHost, null);
    }

    /* JADX WARNING: Missing block: B:31:0x0093, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String getDomain(String key, String defaultHost, String packageName) {
        if (this.mContext == null) {
            Log.e(TAG, "ctx is null when getDomain");
            return defaultHost;
        } else if (TextUtils.isEmpty(key)) {
            Log.e(TAG, "key is empty");
            return defaultHost;
        } else {
            if (packageName == null) {
                packageName = this.mContext.getPackageName();
            }
            if (this.mDomainRepos == null) {
                this.mDomainRepos = new HashMap();
            }
            List<DomainRepo> mDomainRepoList = (List) this.mDomainRepos.get(packageName);
            if (mDomainRepoList == null) {
                Log.d(TAG, "Create Repo List By packageName:" + packageName + ",key:" + key);
                mDomainRepoList = new ArrayList();
                if (sInSystemServer) {
                    mDomainRepoList.add(new LocalVivoDamonsRepoForSystemServer(packageName));
                } else {
                    mDomainRepoList.add(new LocalVivoDamonsRepo(packageName));
                }
                mDomainRepoList.add(new LocalDefaultRepo(packageName));
                this.mDomainRepos.put(packageName, mDomainRepoList);
            }
            String host = takeFirstDomain(key, mDomainRepoList);
            if (TextUtils.isEmpty(host) || DEFAULT_HOST.equals(host)) {
                host = defaultHost;
            }
        }
    }

    private synchronized void clearData() {
        Log.d(TAG, "domain repos clear");
        if (this.mDomainRepos != null) {
            this.mDomainRepos.clear();
        }
    }

    private String takeFirstDomain(String key, List<DomainRepo> mDomainRepoList) {
        String domain = null;
        for (DomainRepo repo : mDomainRepoList) {
            if (repo.isValid()) {
                domain = repo.getDomain(key);
                if ((TextUtils.isEmpty(domain) ^ 1) != 0) {
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(domain)) {
            return DEFAULT_HOST;
        }
        return domain;
    }

    private Map<String, String> parseContent(byte[] buff) throws UnsupportedEncodingException, JSONException {
        JSONObject jo = new JSONObject(new String(buff, "UTF-8").trim());
        if (!jo.has(KEY_METADATA)) {
            return null;
        }
        JSONArray ja = jo.getJSONArray(KEY_METADATA);
        Map<String, String> keyValues = new HashMap();
        for (int i = 0; i < ja.length(); i++) {
            jo = ja.getJSONObject(i);
            String key = jo.getString(KEY_KEY);
            String value = jo.getString(KEY_VALUE);
            if (!(TextUtils.isEmpty(key) || (TextUtils.isEmpty(value) ^ 1) == 0)) {
                keyValues.put(key, value);
            }
        }
        return keyValues;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x005b A:{SYNTHETIC, Splitter: B:41:0x005b} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0060 A:{SYNTHETIC, Splitter: B:44:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005b A:{SYNTHETIC, Splitter: B:41:0x005b} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0060 A:{SYNTHETIC, Splitter: B:44:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002c A:{SYNTHETIC, Splitter: B:16:0x002c} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0031 A:{SYNTHETIC, Splitter: B:19:0x0031} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readFile(File file) {
        Exception e;
        Throwable th;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                byte[] buff = new byte[RadioAccessFamily.HSUPA];
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream(RadioAccessFamily.HSUPA);
                while (true) {
                    try {
                        int count = fis2.read(buff);
                        if (count == -1) {
                            break;
                        }
                        bos2.write(buff, 0, count);
                    } catch (Exception e2) {
                        e = e2;
                        bos = bos2;
                        fis = fis2;
                    } catch (Throwable th2) {
                        th = th2;
                        bos = bos2;
                        fis = fis2;
                        if (fis != null) {
                        }
                        if (bos != null) {
                        }
                        throw th;
                    }
                }
                byte[] toByteArray = bos2.toByteArray();
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (bos2 != null) {
                    try {
                        bos2.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return toByteArray;
            } catch (Exception e4) {
                e = e4;
                fis = fis2;
                try {
                    Log.e(TAG, "read or parse error", e);
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (fis != null) {
                    }
                    if (bos != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                fis = fis2;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            Log.e(TAG, "read or parse error", e);
            if (fis != null) {
            }
            if (bos != null) {
            }
            return null;
        }
    }
}
