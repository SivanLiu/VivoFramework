package com.vivo.services.cipher.utils;

import android.os.SystemProperties;
import android.security.keymaster.SecurityKeyException;
import android.text.TextUtils;
import com.android.internal.telephony.SmsConstants;
import com.vivo.content.Weather;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

public class SecurityServerHandler implements Callable<SecurityServerResult> {
    private static final String INTERNET_PERMISSION = "android.permission.INTERNET";
    private static final int SUCCESS_CODE = 200;
    private SecurityKeyConfigure mConfigure = null;
    private int mKeyType = 0;

    public SecurityServerHandler(SecurityKeyConfigure configure, int keyType) {
        this.mConfigure = configure;
        this.mKeyType = keyType;
    }

    public SecurityServerResult call() throws Exception {
        VLog.i(Contants.TAG, Thread.currentThread().getName() + " start UpdateKeyTask!");
        try {
            return new SecurityServerResult(true, getSecurityKeysFromServer(this.mKeyType), null);
        } catch (SecurityKeyException e) {
            VLog.e(Contants.TAG, this.mConfigure, "Get security keys fail:" + e.getMessage());
            return new SecurityServerResult(false, null, e);
        } catch (Exception e2) {
            VLog.e(Contants.TAG, this.mConfigure, "Get security keys fail:" + e2.getMessage());
            return new SecurityServerResult(false, null, new SecurityKeyException("update key fail", 1000));
        }
    }

    private String getSecurityServerUrl() {
        String serverUrl_IN = "https://in-vmd.vivoglobal.com/api/tkdist";
        String serverUrl_RU = "https://ru-vmd.vivoglobal.com/api/tkdist";
        String serverUrl_ASIA = "https://asia-vmd.vivoglobal.com/api/tkdist";
        String serverUrl_CN = "https://vmd.vivo.com.cn/api/tkdist";
        String countryCode = SystemProperties.get("ro.product.country.region", SmsConstants.FORMAT_UNKNOWN);
        if (SmsConstants.FORMAT_UNKNOWN.equals(countryCode) || "N".equals(countryCode)) {
            countryCode = SystemProperties.get("ro.product.customize.bbk", SmsConstants.FORMAT_UNKNOWN);
        }
        VLog.d(Contants.TAG, this.mConfigure, "countryCode:" + countryCode);
        if (SmsConstants.FORMAT_UNKNOWN.equals(countryCode) || "N".equals(countryCode) || countryCode.startsWith("CN")) {
            return "https://vmd.vivo.com.cn/api/tkdist";
        }
        if ("IN".equals(countryCode)) {
            return "https://in-vmd.vivoglobal.com/api/tkdist";
        }
        if ("RU".equals(countryCode)) {
            return "https://ru-vmd.vivoglobal.com/api/tkdist";
        }
        return "https://asia-vmd.vivoglobal.com/api/tkdist";
    }

    private boolean checkInternetPermission() {
        if (this.mConfigure.getContext().checkCallingOrSelfPermission(INTERNET_PERMISSION) == 0) {
            return true;
        }
        VLog.e(Contants.TAG, this.mConfigure, "no permission of internet");
        return false;
    }

    private byte[] buildRequestData(int keyType) {
        if (TextUtils.isEmpty(this.mConfigure.getUniqueId()) || TextUtils.isEmpty(this.mConfigure.getToken()) || (checkKeyTypeValid(keyType) ^ 1) != 0 || TextUtils.isEmpty(this.mConfigure.getAppSignHash())) {
            VLog.e(Contants.TAG, this.mConfigure, "Request(update key) params: uniqueId=" + this.mConfigure.getUniqueId() + ";packageName=" + this.mConfigure.getToken() + ";keyType=" + keyType + ";appSignHash=" + this.mConfigure.getAppSignHash());
            return null;
        }
        HashMap<String, String> params = new HashMap();
        params.put("uid", this.mConfigure.getUniqueId());
        params.put("kt", this.mConfigure.getToken());
        params.put("ktp", String.valueOf(keyType));
        params.put("pkh", this.mConfigure.getAppSignHash());
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append((String) entry.getKey()).append(Contants.QSTRING_EQUAL).append(URLEncoder.encode((String) entry.getValue(), Contants.ENCODE_MODE)).append(Contants.QSTRING_SPLIT);
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            return stringBuffer.toString().getBytes();
        } catch (Exception e) {
            VLog.e(Contants.TAG, this.mConfigure, "Build request data error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkKeyTypeValid(int type) {
        if (type <= 0 || type > 7) {
            return false;
        }
        return true;
    }

    private byte[] getKeysFromResponse(String response) throws SecurityKeyException {
        try {
            JSONObject responseObj = new JSONObject(response);
            if (responseObj == null) {
                return null;
            }
            if (responseObj.has("status")) {
                int status = responseObj.getInt("status");
                if (status != 200) {
                    VLog.e(Contants.TAG, this.mConfigure, "security server error: " + status);
                    throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_SERVER_STA_EXP + status, status);
                } else if (responseObj.has("data")) {
                    String data = responseObj.getString("data");
                    if (!TextUtils.isEmpty(data)) {
                        return Base64Utils.decode(data);
                    }
                    VLog.e(Contants.TAG, this.mConfigure, "security server return data is empty,response:" + response);
                    throw new SecurityKeyException("update key fail", 162);
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "security server return has no data item,response:" + response);
                    throw new SecurityKeyException("update key fail", 163);
                }
            }
            VLog.e(Contants.TAG, this.mConfigure, "security server response no status info:" + response);
            throw new SecurityKeyException("update key fail", 156);
        } catch (JSONException e) {
            VLog.e(Contants.TAG, this.mConfigure, "security server response not json format:" + response);
            throw new SecurityKeyException("update key fail", 156);
        }
    }

    private String readResponseStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        while (true) {
            try {
                int len = inputStream.read(data);
                if (len == -1) {
                    break;
                }
                byteArrayOutputStream.write(data, 0, len);
            } catch (IOException e) {
                VLog.e(Contants.TAG, this.mConfigure, "Read response steam error:" + e.getMessage());
                e.printStackTrace();
                if (byteArrayOutputStream == null) {
                    return null;
                }
                try {
                    byteArrayOutputStream.close();
                    return null;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return null;
                }
            } catch (Throwable th) {
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            }
        }
        String response = new String(byteArrayOutputStream.toByteArray());
        if (byteArrayOutputStream != null) {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e222) {
                e222.printStackTrace();
            }
        }
        return response;
    }

    private String sendRequest(String url, byte[] requestData) throws SecurityKeyException {
        HttpsURLConnection conn = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(Weather.WEATHERVERSION_ROM_3_0);
            conn.setReadTimeout(Weather.WEATHERVERSION_ROM_3_0);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            outputStream = conn.getOutputStream();
            if (outputStream == null) {
                VLog.e(Contants.TAG, this.mConfigure, "conn outputStream is null");
                throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_CONN_OSTREAM_NULL, 166);
            }
            outputStream.write(requestData);
            outputStream.flush();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
                if (inputStream == null) {
                    VLog.e(Contants.TAG, this.mConfigure, "conn inputStream is null");
                    throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_CONN_ISTREAM_NULL, 167);
                }
                String response = readResponseStream(inputStream);
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
                return response;
            }
            VLog.e(Contants.TAG, this.mConfigure, "HttpURLConnection response code is:" + responseCode);
            throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_CONN_RESP_CODE + responseCode, 164);
        } catch (ProtocolException e3) {
            e3.printStackTrace();
            throw new SecurityKeyException("" + e3.getMessage(), 168);
        } catch (MalformedURLException e4) {
            e4.printStackTrace();
            throw new SecurityKeyException("" + e4.getMessage(), 169);
        } catch (IOException e22) {
            e22.printStackTrace();
            throw new SecurityKeyException("" + e22.getMessage(), 170);
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public byte[] getSecurityKeysFromServer(int keyType) throws SecurityKeyException {
        if (!checkInternetPermission()) {
            VLog.e(Contants.TAG, this.mConfigure, "Update key Internet permission deny");
            throw new SecurityKeyException(Contants.ERROR_NET_ACCESS_DENY, 158);
        } else if (checkKeyTypeValid(keyType)) {
            byte[] requestData = buildRequestData(keyType);
            if (requestData == null) {
                VLog.e(Contants.TAG, this.mConfigure, "Update key build request data fail");
                throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_DEVICE_FAIL, 160);
            }
            String response = sendRequest(getSecurityServerUrl(), requestData);
            if (response != null) {
                return getKeysFromResponse(response);
            }
            VLog.e(Contants.TAG, this.mConfigure, "Update key server has no response");
            throw new SecurityKeyException("update key fail", 165);
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "Update key keytype " + keyType + " unsupported");
            throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_TYPE_UNKNOWN, 159);
        }
    }
}
