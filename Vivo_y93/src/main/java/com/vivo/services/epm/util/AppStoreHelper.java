package com.vivo.services.epm.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.rms.ProcessList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class AppStoreHelper {
    private static final String ANDROID_SDK_VERSION_ = SystemProperties.get("ro.build.version.sdk");
    private static final String ANDROID_VERSION = SystemProperties.get("ro.build.version.release");
    private static final int APPSTORE_CHANNEL_NUM = 9;
    private static final String APPSTORE_URL = "http://az.appstore.vivo.com.cn/third-service/appinfo";
    private static final String APP_STORE_PACKAGE_NAME = "com.bbk.appstore";
    private static final String IMEI = SystemProperties.get("persist.sys.updater.imei");
    private static final String PRODUCT_MODEL = SystemProperties.get("ro.product.model");
    private static final String TAG = "AppStoreHelper";
    private static int sVersionCode = -1;

    public interface IAppVersionCheckCallback {
        public static final int INVALID_VERSION = -1;

        void appVersionCallback(String str, int i);
    }

    public static void gotoAppStoreDetailActivity(Context context, String packageName) {
        Uri uri = new Builder().scheme("market").authority("details").appendQueryParameter("id", packageName).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setPackage(APP_STORE_PACKAGE_NAME);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void checkAppVersionAync(final String packageName, final IAppVersionCheckCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection connection = null;
                int versionCode = -1;
                try {
                    connection = (HttpURLConnection) new URL("http://az.appstore.vivo.com.cn/third-service/appinfo?pkgs=" + packageName + "&channel=" + 9 + "&imei=" + AppStoreHelper.IMEI + "&model=" + AppStoreHelper.PRODUCT_MODEL + "&an=" + AppStoreHelper.ANDROID_VERSION + "&av=" + AppStoreHelper.ANDROID_SDK_VERSION_).openConnection();
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);
                    connection.connect();
                    if (connection.getResponseCode() == ProcessList.PERCEPTIBLE_APP_ADJ) {
                        Log.i(AppStoreHelper.TAG, "connection success");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            response.append(line);
                        }
                        if (!TextUtils.isEmpty(response)) {
                            versionCode = AppStoreHelper.parseServerData(response.toString());
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    callback.appVersionCallback(packageName, versionCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (connection != null) {
                        connection.disconnect();
                    }
                    callback.appVersionCallback(packageName, -1);
                } catch (Throwable th) {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    callback.appVersionCallback(packageName, -1);
                    throw th;
                }
            }
        }).start();
    }

    public static synchronized int getAppVersionCode(String packageName) {
        int i;
        synchronized (AppStoreHelper.class) {
            final Object lock = new Object();
            checkAppVersionAync(packageName, new IAppVersionCheckCallback() {
                public void appVersionCallback(String packageName, int versionCode) {
                    AppStoreHelper.sVersionCode = versionCode;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i = sVersionCode;
        }
        return i;
    }

    private static int parseServerData(String data) {
        int versionCode = -1;
        try {
            JSONArray jArray = new JSONObject(data).getJSONArray("value");
            for (int i = 0; i < jArray.length(); i++) {
                versionCode = Integer.parseInt(jArray.getJSONObject(i).getString("versionCode"));
            }
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
