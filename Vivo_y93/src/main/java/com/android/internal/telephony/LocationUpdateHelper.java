package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkState;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimeUtils;
import com.android.internal.telephony.vivo.syncdomain.DomainHelper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LocationUpdateHelper extends Handler {
    private static final String ACTION_COUNTRY_CHANGED = "vivo.intent.action.USER_COUNTRY_CHANGE";
    private static final String COORD_TYPE = "coord_type";
    private static final String DOMAIN_KEY = "phone_timezone_key";
    private static final int FLAG_SHOULD_NOT_RETRY = 0;
    private static final int FLAG_SHOULD_RETRY = 1;
    private static final String HOST_OTHERS = "https://asia-tzapi.vivoglobal.com/timezone/v1";
    private static final String HOST_RU = "https://ru-tzapi.vivoglobal.com/timezone/v1";
    private static final String LOCATION = "location";
    private static final String LOCATION_UPDATE_ACTION = "com.android.LocationUpdate.alarm";
    private static final String LOG_TAG = "LocationUpdateHelper";
    private static final String LOG_TAG_TEST = "LocationUpdateHelper";
    public static final int MSG_LAC_CHANGED = 1001;
    public static final int MSG_LOCATEFAILED_WHAT = 1002;
    public static final int MSG_LOCATE_RETRY = 1004;
    public static final int MSG_PROCESS_UP = 1003;
    public static final int MSG_RETRY_QUERY_TIME_ZONE = 1005;
    private static final long REPEAT_TIME = 2100000;
    private static final String SELF_CITY = "&city=";
    private static final String SELF_COUNTRY = "&iso=";
    private static final String SELF_IMEI = "&i=";
    private static final String SELF_MODEL = "&model=";
    private static final String SELF_PRODUCT = "&product=";
    private static final String SELF_PROVINCE = "&prov=";
    private static final String SELF_VERSION = "&sysVer=";
    private static final String TIMESTAMP = "timestamp";
    private static final String WEATHER_BROADCAST = "com.vivo.weather.ACTION_SEND_LOCAL_INFO";
    private static LocationUpdateHelper mLocationUpdateHelper;
    private final PendingIntent mAlarmIntent;
    private final AlarmManager mAlarmManager;
    private ContentObserver mAutoTimeObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange) {
            if (!LocationUpdateHelper.this.getAutoTimeZone()) {
                LocationUpdateHelper.this.clearAllTimeZoneFromSp();
            } else if (!LocationUpdateHelper.this.checkIfSingleClockOrIndian()) {
                LocationUpdateHelper.this.mFindCityChangeAndRequireAdapt = true;
                LocationUpdateHelper.this.startLocate();
            }
        }
    };
    private String mCityLocation;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private String mCountryCode = "";
    private State mCurWifiState;
    private long mCurrentLocateSuccessTime;
    private boolean mFindCityChangeAndRequireAdapt;
    private boolean mHasNitzWaitToRecord;
    private final Intent mIntent;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("LocationUpdateHelper", "onreceive " + action);
            if (action == null) {
                return;
            }
            if (LocationUpdateHelper.LOCATION_UPDATE_ACTION.equals(action)) {
                LocationUpdateHelper.this.onAlarmUp();
            } else if (LocationUpdateHelper.WEATHER_BROADCAST.equals(action)) {
                LocationUpdateHelper.this.onWeatherBroadCastReceive(intent);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                if (LocationUpdateHelper.this.isNetworkConnected()) {
                    Log.v("LocationUpdateHelper", "isNeworkConnected mCurrentTime=" + LocationUpdateHelper.this.mCurrentLocateSuccessTime);
                    if (LocationUpdateHelper.this.mCurrentLocateSuccessTime == 0 || SystemClock.elapsedRealtime() - LocationUpdateHelper.this.mCurrentLocateSuccessTime >= LocationUpdateHelper.REPEAT_TIME) {
                        LocationUpdateHelper.this.resetAlarmManager();
                        LocationUpdateHelper.this.networkConnectAndLocate();
                    }
                }
            } else if (LocationUpdateHelper.ACTION_COUNTRY_CHANGED.equals(action)) {
                LocationUpdateHelper.this.startAdapterTimeZone(LocationUpdateHelper.this.mLatitude, LocationUpdateHelper.this.mLongitude, false);
            }
        }
    };
    private boolean mLacOrTacChangedRequireLocate;
    private String mLatitude = "";
    private final LocationListener mLocationListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onLocationChanged(Location location) {
            boolean isSuccess = true;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String countryCode = "";
            String countryName = "";
            String province = "";
            String subAdminArea = "";
            String locality = "";
            String subLocality = "";
            try {
                List<Address> addList = new Geocoder(LocationUpdateHelper.this.mContext.getApplicationContext(), Locale.getDefault()).getFromLocation(latitude, longitude, 1);
                if (addList != null && addList.size() > 0) {
                    Address address = (Address) addList.get(0);
                    countryCode = address.getCountryCode();
                    countryName = address.getCountryName();
                    province = address.getAdminArea();
                    locality = address.getLocality();
                    subAdminArea = address.getSubAdminArea();
                    subLocality = address.getSubLocality();
                }
            } catch (Exception e) {
                isSuccess = false;
                Log.v("LocationUpdateHelper", e.toString());
            }
            String city = locality;
            LocationUpdateHelper.this.mLatitude = "" + latitude;
            LocationUpdateHelper.this.mLongitude = "" + longitude;
            String spTimeZone = LocationUpdateHelper.this.getTimeZoneFromSpWithCity(city);
            if (!TextUtils.isEmpty(spTimeZone)) {
                if (!TextUtils.equals(spTimeZone, SystemProperties.get("persist.sys.timezone", ""))) {
                    LocationUpdateHelper.this.clearTimeZoneFromSpWithCurrentCity();
                    LocationUpdateHelper.this.mFindCityChangeAndRequireAdapt = true;
                }
            } else if (!TextUtils.equals(LocationUpdateHelper.this.mCityLocation, city)) {
                LocationUpdateHelper.this.mFindCityChangeAndRequireAdapt = true;
            }
            LocationUpdateHelper.this.mCountryCode = countryCode;
            LocationUpdateHelper.this.mProvince = province;
            LocationUpdateHelper.this.mCityLocation = city;
            LocationUpdateHelper.this.onLocateFinished(isSuccess, LocationUpdateHelper.this.mLatitude, LocationUpdateHelper.this.mLongitude);
        }
    };
    private LocationManager mLocationManager;
    private String mLongitude = "";
    private ArrayList<String> mPlmnsHaveNitz = new ArrayList();
    private String mProvince = "";

    public boolean checkNitzWaitingForRecord() {
        return this.mHasNitzWaitToRecord;
    }

    public void setHasNitzWaitingForRecord(boolean value) {
        this.mHasNitzWaitToRecord = value;
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (activeInfo == null || !activeInfo.isConnected()) {
            Log.v("LocationUpdateHelper", "isNeworkConnected false");
            return false;
        }
        Log.v("LocationUpdateHelper", "isNeworkConnected true");
        return true;
    }

    public static void makeDefaultUpdater(Context context, Looper looper) {
        if (mLocationUpdateHelper == null) {
            mLocationUpdateHelper = new LocationUpdateHelper(context, looper);
        }
    }

    public static LocationUpdateHelper getInstance() {
        return mLocationUpdateHelper;
    }

    public LocationUpdateHelper(Context context, Looper looper) {
        super(looper);
        this.mContext = context.getApplicationContext();
        DomainHelper.getInstance().init(this.mContext, true);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(LOCATION);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mIntent = new Intent(LOCATION_UPDATE_ACTION);
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, this.mIntent, 268435456);
        sendEmptyMessageDelayed(1003, 20000);
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOCATION_UPDATE_ACTION);
        filter.addAction(WEATHER_BROADCAST);
        filter.addAction(ACTION_COUNTRY_CHANGED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mIntentReceiver, filter, null, this);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("auto_time_zone"), true, this.mAutoTimeObserver);
    }

    public void handleMessage(Message msg) {
        if (msg != null) {
            Log.v("LocationUpdateHelper", "handleMessage what = " + msg.what);
            switch (msg.what) {
                case 1001:
                    onLacOrTacChanged();
                    return;
                case 1002:
                    Log.v("LocationUpdateHelper", "needRetry=" + msg.arg1);
                    onLocateFinished(false, "", "");
                    if (msg.arg1 == 1) {
                        if (hasMessages(1004)) {
                            removeMessages(1004);
                        }
                        sendEmptyMessageDelayed(1004, 120000);
                        return;
                    }
                    return;
                case 1003:
                    onProcessUp();
                    return;
                case 1004:
                    getLocateResult(false);
                    return;
                case 1005:
                    retryQueryTimeZone();
                    return;
                default:
                    return;
            }
        }
    }

    private void retryQueryTimeZone() {
        Log.v("LocationUpdateHelper", "retryQuery");
        if (!TextUtils.isEmpty(this.mLatitude) && (TextUtils.isEmpty(this.mLongitude) ^ 1) != 0 && getAutoTimeZone() && (checkIfSingleClockOrIndian() ^ 1) != 0 && checkShouldAdaptTimeZone()) {
            queryTimeZone(this.mLatitude, this.mLongitude, false);
        }
    }

    private void onWeatherBroadCastReceive(Intent intent) {
        try {
            HashMap<String, String> info = (HashMap) intent.getSerializableExtra("local_info");
            Log.v("LocationUpdateHelper", "onWeatherBroadCastReceive weather " + info);
            if (!checkIfSingleClockOrIndian() && info != null && info.size() > 0) {
                resetAlarmManager();
                this.mLatitude = (String) info.get("latitude");
                this.mLongitude = (String) info.get("longitude");
                String cityLocation = (String) info.get("locality");
                this.mCountryCode = (String) info.get("countryCode");
                this.mProvince = (String) info.get("province");
                String spTimeZone = getTimeZoneFromSpWithCity(cityLocation);
                if (checkIfCityChanged(cityLocation)) {
                    if (TextUtils.isEmpty(spTimeZone)) {
                        this.mCityLocation = cityLocation;
                        this.mFindCityChangeAndRequireAdapt = true;
                    } else if (!TextUtils.equals(spTimeZone, SystemProperties.get("persist.sys.timezone", ""))) {
                        this.mCityLocation = cityLocation;
                        clearTimeZoneFromSpWithCurrentCity();
                        this.mFindCityChangeAndRequireAdapt = true;
                    }
                }
                onLocateFinished(true, this.mLatitude, this.mLongitude);
                Log.v("LocationUpdateHelper", "onWeatherBroadCastReceive " + this.mCountryCode + " " + this.mProvince + " " + cityLocation + " " + this.mLatitude + " " + this.mLongitude + " mCurrentTime=" + this.mCurrentLocateSuccessTime);
            }
        } catch (Exception e) {
            Log.v("LocationUpdateHelper", "getWeather data error " + e.toString());
        }
    }

    public boolean checkShouldAdaptTimeZone() {
        Log.v("LocationUpdateHelper", "checkShouldAdapt = " + this.mFindCityChangeAndRequireAdapt);
        return this.mFindCityChangeAndRequireAdapt;
    }

    private boolean checkShouldLocate() {
        Log.v("LocationUpdateHelper", "checkShouldLoc = " + this.mLacOrTacChangedRequireLocate);
        return this.mLacOrTacChangedRequireLocate;
    }

    public void onLacOrTacChanged() {
        Log.v("LocationUpdateHelper", "onLacOrTacChanged");
        this.mLacOrTacChangedRequireLocate = true;
    }

    public void onTimeZoneAdaptFinished() {
        Log.v("LocationUpdateHelper", "onAdaptFinished");
        this.mFindCityChangeAndRequireAdapt = false;
    }

    private boolean checkIfCityChanged(String cityLocation) {
        if (cityLocation == null || (cityLocation.equals(this.mCityLocation) ^ 1) == 0) {
            Log.v("LocationUpdateHelper", "checkIfCChanged = false");
            return false;
        }
        Log.v("LocationUpdateHelper", "checkIfCChanged = true");
        return true;
    }

    private void notifyCityChanged() {
        Log.v("LocationUpdateHelper", "notifyCChanged");
    }

    private void onProcessUp() {
        if (!checkIfSingleClockOrIndian()) {
            startFirstLocate();
        }
        startAlarmManager();
    }

    private void startFirstLocate() {
        if (!checkIfSingleClockOrIndian()) {
            startLocate();
        }
    }

    private void startLocate() {
        Log.v("LocationUpdateHelper", "startLC");
        getLocateResult(true);
    }

    private void clearAllTimeZoneFromSp() {
        Editor editor = this.mContext.getSharedPreferences("com.android.phone_preferences_timezone", 0).edit();
        editor.clear();
        editor.commit();
    }

    private void clearTimeZoneFromSpWithCurrentCity() {
        String iso = SystemProperties.get("gsm.vivo.countrycode", "");
        if (!TextUtils.isEmpty(iso) && (TextUtils.isEmpty(this.mCityLocation) ^ 1) != 0) {
            Editor editor = this.mContext.getSharedPreferences("com.android.phone_preferences_timezone", 0).edit();
            editor.remove(iso + this.mCityLocation);
            editor.commit();
        }
    }

    private String getTimeZoneFromSpWithCity(String city) {
        String timezone = "";
        String iso = SystemProperties.get("gsm.vivo.countrycode", "");
        if (TextUtils.isEmpty(iso) || (TextUtils.isEmpty(city) ^ 1) == 0) {
            return timezone;
        }
        return this.mContext.getSharedPreferences("com.android.phone_preferences_timezone", 0).getString(iso + city, "");
    }

    private void saveTimeZoneToSpWithCity(String city, String timeZone) {
        String iso = SystemProperties.get("gsm.vivo.countrycode", "");
        SharedPreferences sp = this.mContext.getSharedPreferences("com.android.phone_preferences_timezone", 0);
        if (sp.getAll().size() > 20) {
            sp.edit().clear().commit();
            Log.v("LocationUpdateHelper", "save too many data and we clear");
        }
        if (!TextUtils.isEmpty(iso) && (TextUtils.isEmpty(city) ^ 1) != 0 && (TextUtils.isEmpty(timeZone) ^ 1) != 0) {
            String spTimeZone = sp.getString(iso + city, "");
            Editor editor;
            if (TextUtils.isEmpty(spTimeZone)) {
                editor = sp.edit();
                editor.putString(iso + city, timeZone);
                editor.commit();
            } else if (!TextUtils.equals(spTimeZone, timeZone)) {
                editor = sp.edit();
                editor.remove(iso + city);
                editor.commit();
            }
        }
    }

    private void getLocateResult(boolean needRetry) {
        this.mLocationManager.removeUpdates(this.mLocationListener);
        this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this.mLocationListener);
        if (hasMessages(1002)) {
            removeMessages(1002);
        }
        if (hasMessages(1004)) {
            removeMessages(1004);
        }
        Message msg = obtainMessage(1002);
        msg.arg1 = needRetry ? 1 : 0;
        sendMessageDelayed(msg, 30000);
    }

    private void onLocateFinished(boolean isSuccess, String latitude, String longitude) {
        if (isSuccess) {
            if (hasMessages(1002)) {
                removeMessages(1002);
            }
            if (hasMessages(1004)) {
                removeMessages(1004);
            }
            this.mCurrentLocateSuccessTime = SystemClock.elapsedRealtime();
            this.mLacOrTacChangedRequireLocate = false;
            startAdapterTimeZone(latitude, longitude, false);
        }
        this.mLocationManager.removeUpdates(this.mLocationListener);
        Log.v("LocationUpdateHelper", "onLFinished isSuccess=" + isSuccess + " mCurrentLSuccessTime=" + this.mCurrentLocateSuccessTime);
    }

    private void startAdapterTimeZone(String latitude, String longitude, boolean ignoreCityChanged) {
        Log.v("LocationUpdateHelper", "startAdapter " + latitude + " " + longitude + " ignore=" + ignoreCityChanged);
        if (ignoreCityChanged) {
            if (!TextUtils.isEmpty(latitude) && (TextUtils.isEmpty(longitude) ^ 1) != 0 && getAutoTimeZone() && (checkIfSingleClockOrIndian() ^ 1) != 0) {
                queryTimeZone(latitude, longitude, true);
            }
        } else if (!TextUtils.isEmpty(latitude) && (TextUtils.isEmpty(longitude) ^ 1) != 0 && getAutoTimeZone() && (checkIfSingleClockOrIndian() ^ 1) != 0 && checkShouldAdaptTimeZone()) {
            queryTimeZone(latitude, longitude, true);
        }
    }

    private boolean getAutoTimeZone() {
        boolean z = false;
        try {
            if (Global.getInt(this.mContext.getContentResolver(), "auto_time_zone") > 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException snfe) {
            Log.v("LocationUpdateHelper", snfe.toString());
            return false;
        }
    }

    private void startAlarmManager() {
        this.mAlarmManager.setRepeating(3, SystemClock.elapsedRealtime() + REPEAT_TIME, REPEAT_TIME, this.mAlarmIntent);
    }

    private void resetAlarmManager() {
        this.mAlarmManager.cancel(this.mAlarmIntent);
        startAlarmManager();
    }

    private void networkConnectAndLocate() {
        Log.v("LocationUpdateHelper", "networkConnect");
        if (!checkIfSingleClockOrIndian()) {
            if (checkShouldLocate()) {
                startLocate();
            } else {
                startAdapterTimeZone(this.mLatitude, this.mLongitude, false);
            }
        }
    }

    private void onAlarmUp() {
        Log.v("LocationUpdateHelper", "onAlarmUp");
        if (!checkIfSingleClockOrIndian()) {
            if (checkShouldLocate()) {
                startLocate();
            } else {
                startAdapterTimeZone(this.mLatitude, this.mLongitude, false);
            }
        }
    }

    public boolean checkIfSingleClockOrIndian() {
        String preIsoCountryCode = SystemProperties.get("gsm.vivo.countrycode", "");
        if ("id".equals(preIsoCountryCode) || "cn".equals(preIsoCountryCode) || "in".equals(preIsoCountryCode) || TimeUtils.getTimeZoneIdsWithUniqueOffsets(preIsoCountryCode).size() <= 1 || TextUtils.isEmpty(preIsoCountryCode)) {
            Log.v("LocationUpdateHelper", "checkL true");
            return true;
        }
        Log.v("LocationUpdateHelper", "checkL false");
        return false;
    }

    private String encode(String s) {
        String result = "";
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            Log.e("LocationUpdateHelper", "encode error:" + s);
            return result;
        }
    }

    public void queryTimeZone(String latitude, String longitude, boolean shouldRetry) {
        Log.v("LocationUpdateHelper", "query " + latitude + " " + longitude + " shouldRetry=" + shouldRetry);
        boolean isSuccess = false;
        InputStream inputStream = null;
        try {
            String sellIso;
            GsmCdmaPhone phone = (GsmCdmaPhone) PhoneFactory.getPhone(0);
            Network network = getAnyConnectedDefaultNetwork();
            String product = SystemProperties.get("ro.product.name", "");
            String version = SystemProperties.get("ro.build.version.bbk", "");
            String model = SystemProperties.get("ro.product.model", "");
            if (VERSION.SDK_INT >= 26) {
                sellIso = SystemProperties.get("ro.product.country.region", "N");
            } else {
                sellIso = SystemProperties.get("ro.product.customize.bbk", "N");
            }
            String selfTimeZoneQueryURL = "";
            if ("RU".equals(sellIso)) {
                selfTimeZoneQueryURL = DomainHelper.getInstance().getDomain(DOMAIN_KEY, HOST_RU);
            } else {
                selfTimeZoneQueryURL = DomainHelper.getInstance().getDomain(DOMAIN_KEY, HOST_OTHERS);
            }
            Log.e("LocationUpdateHelper", "sellIso : " + sellIso);
            if (network != null) {
                URLConnection connection = network.openConnection(new URL(selfTimeZoneQueryURL + "?" + COORD_TYPE + "=" + "wgs84ll" + "&" + LOCATION + "=" + encode(latitude + "," + longitude) + SELF_IMEI + encode(md5(phone.getImei())) + SELF_PRODUCT + encode(product) + SELF_VERSION + encode(version) + SELF_MODEL + encode(model) + SELF_COUNTRY + encode(this.mCountryCode) + SELF_PROVINCE + encode(this.mProvince) + SELF_CITY + encode(this.mCityLocation)));
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection conn = (HttpURLConnection) connection;
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    int code = conn.getResponseCode();
                    Log.e("LocationUpdateHelper", "code : " + code);
                    if (code == 200) {
                        isSuccess = true;
                        inputStream = conn.getInputStream();
                        String result = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                        Log.e("LocationUpdateHelper", "get result");
                        startFrameworkAdapt(result);
                        onTimeZoneAdaptFinished();
                    }
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
            Log.v("LocationUpdateHelper", e2.toString());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                }
            }
        }
        if (hasMessages(1005)) {
            removeMessages(1005);
        }
        if (!isSuccess && shouldRetry) {
            sendEmptyMessageDelayed(1005, 300000);
        }
    }

    private void startFrameworkAdapt(String result) {
        Log.v("LocationUpdateHelper", "startFAda");
        try {
            String resultSuccess = result.split(",")[0].replaceAll("\\{\"code\":", "");
            Log.v("LocationUpdateHelper", "resultSuccess " + resultSuccess);
            if ("0".equals(resultSuccess)) {
                String resultTimeZone = result.split(",")[1].split(":")[2].replaceAll("\"", "");
                Log.v("LocationUpdateHelper", "resultTz success");
                setAndBroadcastNetworkSetTimeZone(resultTimeZone);
            }
        } catch (Exception e) {
            Log.v("LocationUpdateHelper", "startFAda error: " + e.toString());
        }
    }

    private boolean checkTimeZoneIdAvailable(String zoneId) {
        boolean isAvailable = false;
        String[] ids = TimeZone.getAvailableIDs();
        if (ids != null && ids.length > 0) {
            for (String s : ids) {
                if (TextUtils.equals(s, zoneId)) {
                    isAvailable = true;
                }
            }
        }
        Log.v("LocationUpdateHelper", "checkTzAvailable return " + isAvailable);
        return isAvailable;
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        if (!TextUtils.isEmpty(zoneId) && (checkTimeZoneIdAvailable(zoneId) ^ 1) == 0) {
            Log.v("LocationUpdateHelper", "setNetworkSet ");
            GsmCdmaPhone phone0 = (GsmCdmaPhone) PhoneFactory.getPhone(0);
            GsmCdmaPhone phone1 = (GsmCdmaPhone) PhoneFactory.getPhone(1);
            if (!TextUtils.isEmpty(phone0.mSST.getNitzTimeZone())) {
                phone0.mSST.saveNitzTimeZone(zoneId);
            }
            if (!TextUtils.isEmpty(phone1.mSST.getNitzTimeZone())) {
                phone1.mSST.saveNitzTimeZone(zoneId);
            }
            saveTimeZoneToSpWithCity(this.mCityLocation, zoneId);
            ((AlarmManager) this.mContext.getSystemService("alarm")).setTimeZone(zoneId);
            if (zoneId != null && zoneId.length() <= 91) {
                SystemProperties.set("persist.radio.vivo.zone", zoneId);
            }
            Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
            intent.addFlags(536870912);
            intent.putExtra("time-zone", zoneId);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        try {
            String result = "";
            for (byte b : MessageDigest.getInstance("MD5").digest(string.getBytes())) {
                String temp = Integer.toHexString(b & 255);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result = result + temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private Network getAnyConnectedDefaultNetwork() {
        IConnectivityManager mCMService = null;
        try {
            mCMService = Stub.asInterface(ServiceManager.getServiceOrThrow("connectivity"));
        } catch (Exception e) {
            Log.v("LocationUpdateHelper", e.toString());
        }
        if (mCMService != null) {
            try {
                NetworkState[] networkStates = mCMService.getAllNetworkState();
                if (networkStates == null || networkStates.length == 0) {
                    return null;
                }
                for (NetworkState networkState : networkStates) {
                    if (!(networkState == null || networkState.networkCapabilities == null || networkState.networkInfo == null)) {
                        Log.e("LocationUpdateHelper", "network : " + networkState.network + " networkcapability : " + networkState.networkCapabilities + " isConnected = " + networkState.networkInfo.isConnected());
                        if (networkState.networkCapabilities.hasCapability(12) && networkState.networkCapabilities.hasCapability(15) && networkState.networkInfo.isConnected()) {
                            return networkState.network;
                        }
                    }
                }
            } catch (Exception e2) {
                Log.v("LocationUpdateHelper", e2.toString());
            }
        }
        return null;
    }

    private void testSetErrorCountryCode(String countryCode, String province, String city, String lat, String longitude) {
        this.mCountryCode = countryCode;
        this.mProvince = province;
        this.mCityLocation = city;
        this.mLatitude = lat;
        this.mLongitude = longitude;
    }

    public String testGetmLatitude() {
        return this.mLatitude;
    }

    public String testGetmLongitude() {
        return this.mLongitude;
    }

    public String testGetmCityLocation() {
        return this.mCityLocation;
    }

    public void testSetmFindCityChangeAndRequireAdapt(boolean value) {
        this.mFindCityChangeAndRequireAdapt = value;
    }

    public void testSetmLacOrTacChangedRequireLocate(boolean value) {
        this.mLacOrTacChangedRequireLocate = value;
    }

    public void testqueryTimeZone(String a, String b) {
        queryTimeZone(a, b, true);
    }

    public void teststartAdapterTimeZone(String a, String b) {
        startAdapterTimeZone(a, b, true);
    }

    public void teststartLocate() {
        startLocate();
    }

    public void testLocationChanged(Location location) {
        this.mLocationListener.onLocationChanged(location);
    }
}
