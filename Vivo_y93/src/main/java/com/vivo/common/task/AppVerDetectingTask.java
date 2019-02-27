package com.vivo.common.task;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.vivo.common.provider.Calendar.Calendars;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather.AutoUpdate;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public abstract class AppVerDetectingTask extends AsyncTask<Void, Integer, Object> {
    private static final String ACTION_SYSTEM_UPDATE = "com.bbk.action.SYSTEM_UPDATE";
    private static final String CHECK_URL = "http://sysupgrade.bbk.com/checkapp/query";
    private static final String METHOD_ENCODE = "UTF-8";
    private static final String PROP_BRANCH_VERSION = "ro.hardware.bbk";
    private static final String PROP_CUSTOMIZE = "ro.product.customize.bbk";
    private static final String PROP_CUSTOMIZE_NEW = "persist.sys.vivo.product.cust";
    private static final String PROP_MODEL = "ro.product.model.bbk";
    private static final String PROP_NETWORK_STANDARD_TYPE = "ro.vivo.op.entry";
    private static final String PROP_RADIO_TYPE = "persist.vivo.radio.type.abbr";
    private static final String PROP_VERSION = "ro.build.version.bbk";
    private static final String PROTOCAL_VERSION = "1";
    private static final String TAG = "AppVerDetectingTask";
    private static final int TIMEOUT = 60000;
    private String appName = null;
    private Activity context = null;
    private ProgressDialog dialog = null;
    private boolean showDialog = false;
    private int verCode = 0;
    private String verName = null;

    public static class AppUpdateInfo {
        private String appName = null;
        private String instruction = null;
        private int stateCode = 0;
        private String url = null;
        private int verCode = 0;
        private String verName = null;

        public AppUpdateInfo(String info) {
            if (!AppVerDetectingTask.isStringEmpty(info)) {
                try {
                    JSONObject json = new JSONObject(info);
                    setStateCode(getInt("stat", json));
                    setAppName(getRawString("appName", json));
                    setVerName(getRawString("verName", json));
                    setVerCode(getInt("verCode", json));
                    setInstruction(getRawString("instruction", json));
                    setUrl(getRawString(Calendars.URL, json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getRawString(String name, JSONObject json) {
            try {
                if (json.isNull(name)) {
                    return null;
                }
                return json.getString(name);
            } catch (JSONException e) {
                return null;
            }
        }

        public int getInt(String name, JSONObject json) {
            return getInt(getRawString(name, json));
        }

        public int getInt(String str) {
            if (str == null || Events.DEFAULT_SORT_ORDER.equals(str) || "null".equals(str)) {
                return 0;
            }
            try {
                return Integer.valueOf(str).intValue();
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public String getAppName() {
            return this.appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getVerName() {
            return this.verName;
        }

        public void setVerName(String verName) {
            this.verName = verName;
        }

        public int getVerCode() {
            return this.verCode;
        }

        public void setVerCode(int verCode) {
            this.verCode = verCode;
        }

        public String getInstruction() {
            return this.instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getStateCode() {
            return this.stateCode;
        }

        public void setStateCode(int stateCode) {
            this.stateCode = stateCode;
        }
    }

    final class ExceptionCode {
        public static final int FORCE_UPDATE = 201;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int MSG_LATEAST_VERSION = 200;
        public static final int NET_UNCONNECTED = 1001;
        public static final int PARAM_ERROR = 400;
        public static final int SELF_UPDATE = 210;
        public static final int SUGGEST_UPDATE = 202;

        ExceptionCode() {
        }
    }

    protected abstract void onCancel();

    protected abstract void onExit();

    protected abstract void onUpdate();

    public AppVerDetectingTask(Activity context, String appName, String verName, int verCode, boolean showDialog) {
        this.context = context;
        this.appName = appName;
        this.verName = verName;
        this.verCode = verCode;
        this.showDialog = showDialog;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (isShowDialog()) {
            this.dialog = ProgressDialog.show(this.context, null, this.context.getString(51249167));
            this.dialog.setCancelable(true);
            this.dialog.setOwnerActivity(this.context);
            this.dialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    AppVerDetectingTask.this.cancel(true);
                    dialog.dismiss();
                }
            });
        }
    }

    private String getProName() {
        return SystemProperties.get("ro.product.model.bbk", Build.MODEL);
    }

    private String getNetworkStandardType() {
        return SystemProperties.get(PROP_NETWORK_STANDARD_TYPE, Events.DEFAULT_SORT_ORDER);
    }

    private String getRadioType() {
        return SystemProperties.get(PROP_RADIO_TYPE, Events.DEFAULT_SORT_ORDER);
    }

    private String getPublicProName() {
        return Build.MODEL;
    }

    private String getHardwareVersion() {
        return SystemProperties.get(PROP_BRANCH_VERSION, "A");
    }

    private boolean isDigit(char c) {
        if (c < '0' || c > '9') {
            return false;
        }
        return true;
    }

    private String format(String version) {
        char[] array = version.trim().toCharArray();
        int len = array.length;
        int index = 0;
        while (index < len && !isDigit(array[index])) {
            index++;
        }
        return version.substring(index);
    }

    private String getSoftVersion() {
        String[] list = SystemProperties.get(PROP_VERSION).split("\\_");
        if (list == null) {
            return Events.DEFAULT_SORT_ORDER;
        }
        if (list.length <= 3) {
            return format(list[list.length - 1]);
        }
        return format(list[2]);
    }

    private String getCustomize() {
        String cust = SystemProperties.get(PROP_CUSTOMIZE_NEW);
        if (cust == null || cust.length() <= 0) {
            return SystemProperties.get(PROP_CUSTOMIZE, "N");
        }
        return cust;
    }

    private String getSysVersion() {
        String sysVersion = (((getProName() + "_") + getCustomize() + "_") + getHardwareVersion() + "_") + getSoftVersion();
        Log.d(TAG, "sysVersion:" + sysVersion);
        return sysVersion;
    }

    private String getImei() {
        String imei = ((TelephonyManager) this.context.getSystemService("phone")).getImei(0);
        return TextUtils.isEmpty(imei) ? "unknow" : imei;
    }

    private String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private String getElapsedRealtime() {
        return String.valueOf(SystemClock.elapsedRealtime());
    }

    private String getCheckUrl() {
        String urlStr = null;
        Map<String, String> localMap = new HashMap();
        localMap.put("appName", this.appName);
        localMap.put("verCode", String.valueOf(this.verCode));
        localMap.put("verName", this.verName);
        localMap.put("proName", getProName());
        localMap.put("public_model", getPublicProName());
        localMap.put("sysVersion", getSysVersion());
        localMap.put(AutoUpdate.IMEI, getImei());
        localMap.put("mtype", getNetworkStandardType());
        localMap.put("language", getLanguage());
        localMap.put("elapsedtime", getElapsedRealtime());
        localMap.put("pflag", PROTOCAL_VERSION);
        localMap.put("radiotype", getRadioType());
        try {
            return appendQueryParameters(CHECK_URL, localMap, METHOD_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            return urlStr;
        }
    }

    protected Object doInBackground(Void... params) {
        HttpClient client = new DefaultHttpClient();
        try {
            String urlStr = getCheckUrl();
            Log.d(TAG, "CheckAppUrl:" + urlStr);
            HttpGet request = new HttpGet(URI.create(urlStr));
            request.addHeader("Cache-Control", "no-cache");
            client.getParams().setParameter("http.connection.timeout", Integer.valueOf(TIMEOUT));
            client.getParams().setParameter("http.socket.timeout", Integer.valueOf(TIMEOUT));
            try {
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() != 200) {
                    publishProgress(new Integer[]{Integer.valueOf(ExceptionCode.NET_UNCONNECTED)});
                    Log.d(TAG, "network can't connect");
                    return null;
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    publishProgress(new Integer[]{Integer.valueOf(ExceptionCode.NET_UNCONNECTED)});
                    return null;
                }
                AppUpdateInfo ret = null;
                try {
                    BufferedReader lineReader = new BufferedReader(new InputStreamReader(entity.getContent()), 2048);
                    StringBuffer buf = new StringBuffer();
                    while (true) {
                        String line = lineReader.readLine();
                        if (line == null) {
                            break;
                        }
                        buf.append(line);
                    }
                    lineReader.close();
                    String buffer = buf.toString();
                    Log.d(TAG, "response:" + buffer);
                    if (buffer.equalsIgnoreCase("no_record")) {
                        publishProgress(new Integer[]{Integer.valueOf(200)});
                    } else {
                        ret = new AppUpdateInfo(buffer);
                    }
                    return ret;
                } catch (Exception e) {
                    publishProgress(new Integer[]{Integer.valueOf(500)});
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e2) {
                publishProgress(new Integer[]{Integer.valueOf(ExceptionCode.NET_UNCONNECTED)});
                e2.printStackTrace();
                return null;
            }
        } catch (Exception e22) {
            publishProgress(new Integer[]{Integer.valueOf(ExceptionCode.NET_UNCONNECTED)});
            e22.printStackTrace();
            return null;
        }
    }

    private String getCodeString(int code) {
        String ret = Events.DEFAULT_SORT_ORDER;
        switch (code) {
            case 200:
                return this.context.getString(51249173);
            case 400:
            case 500:
                return this.context.getString(51249174);
            case ExceptionCode.NET_UNCONNECTED /*1001*/:
                return this.context.getString(51249175);
            default:
                return this.context.getString(51249173);
        }
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (isShowDialog() && values != null && values[0] != null) {
            Toast.makeText(this.context, getCodeString(values[0].intValue()), 1).show();
        }
    }

    private boolean isShowDialog() {
        return this.showDialog;
    }

    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (this.context != null && !this.context.isFinishing()) {
            if (isShowDialog()) {
                if (this.dialog != null && (this.dialog.isShowing() ^ 1) == 0) {
                    try {
                        this.dialog.dismiss();
                        this.dialog = null;
                    } catch (Exception e) {
                        Log.d(TAG, "Exception:onPostExecute():mDialog.dismiss()");
                    }
                } else {
                    return;
                }
            }
            if (result != null && ((result instanceof AppUpdateInfo) ^ 1) == 0) {
                AppUpdateInfo info = (AppUpdateInfo) result;
                int stateCode = info.getStateCode();
                if (stateCode == ExceptionCode.FORCE_UPDATE || stateCode == ExceptionCode.SUGGEST_UPDATE || stateCode == ExceptionCode.SELF_UPDATE) {
                    showUpdateDialog(info);
                } else {
                    onProgressUpdate(Integer.valueOf(stateCode));
                }
            }
        }
    }

    protected void onShow() {
    }

    protected void onDismiss() {
    }

    protected void onUpdate(AppUpdateInfo result) {
    }

    private <T> String appendQueryParameters(String url, Map<String, T> paramMap, String encode) throws Exception {
        StringBuilder localStringBuilder = new StringBuilder();
        if (url == null || paramMap == null || paramMap.size() == 0) {
            return localStringBuilder.toString();
        }
        if (isStringEmpty(encode)) {
            encode = METHOD_ENCODE;
        }
        for (Entry<String, T> localEntry : paramMap.entrySet()) {
            if (localStringBuilder.length() > 0) {
                localStringBuilder.append("&");
            }
            localStringBuilder.append(URLEncoder.encode((String) localEntry.getKey(), encode));
            localStringBuilder.append("=");
            localStringBuilder.append(URLEncoder.encode(String.valueOf(localEntry.getValue()), encode));
        }
        if (url.indexOf("?") < 0) {
            localStringBuilder.insert(0, "?");
        } else {
            localStringBuilder.insert(0, "&");
        }
        localStringBuilder.insert(0, url);
        return localStringBuilder.toString();
    }

    private static boolean isStringEmpty(String paramString) {
        if (paramString == null || paramString.trim().length() == 0) {
            return true;
        }
        return false;
    }

    private void keepDialog(DialogInterface dialog, boolean isKeep) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, Boolean.valueOf(isKeep ^ 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(final AppUpdateInfo info) {
        AppUpdateInfo result = info;
        final int stateCode = info.getStateCode();
        final String url = info.getUrl();
        String title = this.context.getString(51249168);
        String msg = info.getInstruction();
        String posBtn = this.context.getString(51249169);
        String negBtn = this.context.getString(51249170);
        if (stateCode == ExceptionCode.FORCE_UPDATE) {
            negBtn = this.context.getString(51249171);
        }
        onShow();
        Dialog dialog = new Builder(this.context).setTitle(title).setMessage(msg).setPositiveButton(posBtn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (stateCode == ExceptionCode.FORCE_UPDATE) {
                    AppVerDetectingTask.this.keepDialog(dialog, true);
                }
                AppVerDetectingTask.this.onUpdate();
                AppVerDetectingTask.this.onUpdate(info);
                if (AppVerDetectingTask.isStringEmpty(url)) {
                    AppVerDetectingTask.this.context.sendBroadcast(new Intent(AppVerDetectingTask.ACTION_SYSTEM_UPDATE));
                    Log.d(AppVerDetectingTask.TAG, "sendBroadcast:SNSConstant.ACTION_SYSTEM_UPDATE");
                }
            }
        }).setNegativeButton(negBtn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (stateCode == ExceptionCode.FORCE_UPDATE) {
                    AppVerDetectingTask.this.onExit();
                } else {
                    AppVerDetectingTask.this.onCancel();
                }
            }
        }).create();
        if (stateCode == ExceptionCode.FORCE_UPDATE) {
            dialog.setCancelable(false);
        } else {
            dialog.setCancelable(true);
        }
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                AppVerDetectingTask.this.onDismiss();
            }
        });
        dialog.show();
    }
}
