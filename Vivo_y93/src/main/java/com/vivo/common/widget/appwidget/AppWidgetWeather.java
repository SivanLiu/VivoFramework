package com.vivo.common.widget.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.Toast;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.util.HashMap;

public class AppWidgetWeather {
    private String mAddCityString = null;
    private WeatherUpdateCallback mCallback;
    private Context mContext = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AppWidgetWeather.this.mCallback.onWeatherUpdate(AppWidgetWeather.this.mWeatherInfo);
        }
    };
    private String mNetworkErrorString = null;
    private String mNoDataString = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Weather.ACTION_DATA_CHANGE.equals(intent.getAction())) {
                AppWidgetWeather.this.removeMsg();
                AppWidgetWeather.this.mWorkHandler.post(AppWidgetWeather.this.updateRunnable);
            }
        }
    };
    private Weather mWeather = null;
    private WeatherInfo mWeatherInfo = null;
    private Handler mWorkHandler = null;
    private HandlerThread mWorkThread = null;
    private Runnable updateRunnable = new Runnable() {
        public void run() {
            try {
                int i;
                AppWidgetWeather.this.mWeatherInfo.isLbs = AppWidgetWeather.this.mWeather.isLBS();
                int citynum = AppWidgetWeather.this.mWeather.getCityNum(AppWidgetWeather.this.mWeatherInfo.isLbs);
                Cursor dataCursor = null;
                Time time = new Time();
                time.setToNow();
                int j = AppWidgetWeather.this.mWeatherInfo.futherWeather.length;
                for (i = 0; i < j; i++) {
                    time.monthDay++;
                    AppWidgetWeather.this.mWeatherInfo.futherWeather[i].week = DateUtils.formatDateTime(AppWidgetWeather.this.mContext, time.normalize(true), 32770);
                    AppWidgetWeather.this.mWeatherInfo.futherWeather[i].icon = 3;
                    AppWidgetWeather.this.mWeatherInfo.futherWeather[i].low = "N";
                    AppWidgetWeather.this.mWeatherInfo.futherWeather[i].high = "N";
                }
                if (citynum != 0) {
                    dataCursor = AppWidgetWeather.this.mWeather.queryCurrentWeather(AppWidgetWeather.this.mWeatherInfo.isLbs, AppWidgetWeather.this.mWeather.getCurrentCityPosition(AppWidgetWeather.this.mWeatherInfo.isLbs), 5);
                }
                if (dataCursor == null || dataCursor.getCount() <= 0) {
                    if (dataCursor != null) {
                        dataCursor.close();
                    }
                    AppWidgetWeather.this.mWeatherInfo.currentIcon = 3;
                    if (citynum != 0) {
                        AppWidgetWeather.this.mWeatherInfo.currentCity = AppWidgetWeather.this.mWeather.getCurrentCity(AppWidgetWeather.this.mWeatherInfo.isLbs);
                        AppWidgetWeather.this.mWeatherInfo.currentCondition = AppWidgetWeather.this.mNoDataString;
                    } else {
                        AppWidgetWeather.this.mWeatherInfo.currentCity = null;
                        AppWidgetWeather.this.mWeatherInfo.currentCondition = null;
                    }
                    AppWidgetWeather.this.mWeatherInfo.currentTemp = null;
                    AppWidgetWeather.this.mWeatherInfo.currentTempHigh = null;
                    AppWidgetWeather.this.mWeatherInfo.currentTempLow = null;
                } else {
                    dataCursor.moveToFirst();
                    AppWidgetWeather.this.mWeatherInfo.currentCity = dataCursor.getString(1);
                    time.setToNow();
                    long flagday = AppWidgetWeather.this.compareCurrentDate(dataCursor.getString(2), time.toString().substring(0, 8));
                    boolean pre = flagday > 0;
                    if (flagday > 0) {
                        AppWidgetWeather.this.mWeatherInfo.currentTemp = null;
                        AppWidgetWeather.this.mWeatherInfo.currentTempHigh = null;
                        AppWidgetWeather.this.mWeatherInfo.currentTempLow = null;
                        AppWidgetWeather.this.mWeatherInfo.currentCondition = null;
                        AppWidgetWeather.this.mWeatherInfo.currentIcon = 3;
                        flagday--;
                    } else {
                        AppWidgetWeather.this.mWeatherInfo.currentCondition = dataCursor.getString(4);
                        AppWidgetWeather.this.mWeatherInfo.currentTemp = dataCursor.getString(9);
                        AppWidgetWeather.this.mWeatherInfo.currentTempLow = dataCursor.getString(7);
                        AppWidgetWeather.this.mWeatherInfo.currentTempHigh = dataCursor.getString(8);
                        AppWidgetWeather.this.mWeatherInfo.currentIcon = ((Integer) AppWidgetWeather.this.mWeather.getWeatherState(AppWidgetWeather.this.mWeatherInfo.currentCondition).get("live")).intValue();
                    }
                    j = AppWidgetWeather.this.mWeatherInfo.futherWeather.length;
                    for (i = 0; i < j; i++) {
                        if (flagday > 0) {
                            flagday--;
                        } else if (!dataCursor.isLast()) {
                            if (pre) {
                                AppWidgetWeather.this.setFutureWeather(dataCursor, i);
                                dataCursor.moveToNext();
                            } else {
                                dataCursor.moveToNext();
                                AppWidgetWeather.this.setFutureWeather(dataCursor, i);
                            }
                        }
                    }
                    dataCursor.close();
                }
                if (AppWidgetWeather.this.mWeatherInfo.currentCity == null || Events.DEFAULT_SORT_ORDER.equals(AppWidgetWeather.this.mWeatherInfo.currentCity)) {
                    AppWidgetWeather.this.mWeatherInfo.currentCity = AppWidgetWeather.this.mAddCityString;
                }
                if (AppWidgetWeather.this.mWeatherInfo.currentTemp == null) {
                    AppWidgetWeather.this.mWeatherInfo.currentTemp = "N";
                }
                if (AppWidgetWeather.this.mWeatherInfo.currentTempHigh == null) {
                    AppWidgetWeather.this.mWeatherInfo.currentTempHigh = "N";
                }
                if (AppWidgetWeather.this.mWeatherInfo.currentTempLow == null) {
                    AppWidgetWeather.this.mWeatherInfo.currentTempLow = "N";
                }
                if (AppWidgetWeather.this.mWeatherInfo.currentCondition == null) {
                    AppWidgetWeather.this.mWeatherInfo.currentCondition = AppWidgetWeather.this.mNoDataString;
                }
                AppWidgetWeather.this.mHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public class WeatherInfo {
        private String currentCity;
        private String currentCondition;
        private int currentIcon;
        private String currentTemp;
        private String currentTempHigh;
        private String currentTempLow;
        private FutherWeather[] futherWeather;
        private boolean isLbs;

        public class FutherWeather {
            String high = "N";
            int icon = 3;
            String low = "N";
            String week = Events.DEFAULT_SORT_ORDER;

            public int getIcon() {
                return this.icon;
            }

            public String getWeek() {
                return this.week;
            }

            public String getHigh() {
                return this.high;
            }

            public String getLow() {
                return this.low;
            }
        }

        WeatherInfo(int day) {
            this.futherWeather = new FutherWeather[day];
            for (int i = 0; i < day; i++) {
                this.futherWeather[i] = new FutherWeather();
            }
        }

        public int getCurrentIcon() {
            return this.currentIcon;
        }

        public String getCurrentCity() {
            return this.currentCity;
        }

        public String getCurrentCondition() {
            return this.currentCondition;
        }

        public String getCurrentTemp() {
            return this.currentTemp;
        }

        public String getCurrentTempHigh() {
            return this.currentTempHigh;
        }

        public String getCurrentTempLow() {
            return this.currentTempLow;
        }

        public boolean getIsLbs() {
            return this.isLbs;
        }

        public FutherWeather[] getFutherWeather() {
            return this.futherWeather;
        }
    }

    public interface WeatherUpdateCallback {
        void onWeatherUpdate(WeatherInfo weatherInfo);
    }

    public AppWidgetWeather(Context context, int day, WeatherUpdateCallback callback, String networkErrorString, String addCityString, String noDataString) {
        this.mContext = context;
        this.mCallback = callback;
        this.mNoDataString = noDataString;
        this.mAddCityString = addCityString;
        this.mNetworkErrorString = networkErrorString;
        this.mWeather = new Weather(this.mContext);
        this.mWeatherInfo = new WeatherInfo(day);
        if (this.mWorkThread == null) {
            this.mWorkThread = new HandlerThread("AppWidgetWeather#Backgroundwork");
            this.mWorkThread.start();
            this.mWorkHandler = new Handler(this.mWorkThread.getLooper());
        }
        this.mWorkHandler.post(this.updateRunnable);
    }

    public void nextCity() {
        this.mWeather.toNextCity();
    }

    public void prevCity() {
        this.mWeather.toPreCity();
    }

    public void update() {
        if (getConnectionType() == 0) {
            Toast.makeText(this.mContext, this.mNetworkErrorString, 0).show();
            return;
        }
        this.mContext.sendBroadcast(new Intent(Weather.ACTION_UPDATE_ALARM));
    }

    public void location() {
        this.mContext.sendBroadcast(new Intent("com.vivo.weather.startlocation"));
    }

    public void startWeatherApp() {
        Intent startapp = new Intent("android.intent.action.MAIN");
        startapp.setClassName("com.vivo.weather", "com.vivo.weather.WeatherMain");
        startapp.setFlags(270532608);
        try {
            this.mContext.startActivity(startapp);
        } catch (Exception e) {
        }
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Weather.ACTION_DATA_CHANGE);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public void unregister() {
        try {
            this.mContext.unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
        }
    }

    private int getConnectionType() {
        NetworkInfo networkinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkinfo == null || networkinfo.getState() != State.CONNECTED) {
            return 0;
        }
        if (networkinfo.getType() == 1) {
            return 1;
        }
        if (networkinfo.getType() == 0) {
            return 2;
        }
        return 0;
    }

    private void removeMsg() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mWorkHandler.removeCallbacksAndMessages(null);
    }

    private long compareCurrentDate(String serverDateStr, String nowStr) {
        if (serverDateStr == null) {
            return 5;
        }
        int i;
        int i2 = 12;
        int[] leapYear = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        i2 = 12;
        int[] common = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int yearS = Integer.parseInt(serverDateStr.substring(0, 4));
        int monthS = Integer.parseInt(serverDateStr.substring(4, 6));
        int dayS = Integer.parseInt(serverDateStr.substring(6, 8));
        boolean leapS = (yearS % 4 == 0 && yearS % 100 != 0) || yearS % 400 == 0;
        long server = (long) ((leapS ? yearS * 366 : yearS * 365) + dayS);
        for (i = 1; i < monthS; i++) {
            server += (long) ((leapS ? leapYear[i - 1] : common[i - 1]) * i);
        }
        int yearN = Integer.parseInt(nowStr.substring(0, 4));
        int monthN = Integer.parseInt(nowStr.substring(4, 6));
        int dayN = Integer.parseInt(nowStr.substring(6, 8));
        boolean leapN = (yearN % 4 == 0 && yearN % 100 != 0) || yearN % 400 == 0;
        long now = (long) ((leapN ? yearN * 366 : yearN * 365) + dayN);
        for (i = 1; i < monthN; i++) {
            now += (long) ((leapN ? leapYear[i - 1] : common[i - 1]) * i);
        }
        return server - now;
    }

    private void setFutureWeather(Cursor cursor, int i) {
        HashMap<String, Integer> stateMaps = this.mWeather.getWeatherState(cursor.getString(4));
        this.mWeatherInfo.futherWeather[i].icon = ((Integer) stateMaps.get("live")).intValue();
        this.mWeatherInfo.futherWeather[i].low = cursor.getString(7);
        this.mWeatherInfo.futherWeather[i].high = cursor.getString(8);
        if (this.mWeatherInfo.futherWeather[i].high == null) {
            this.mWeatherInfo.futherWeather[i].high = "N";
        }
        if (this.mWeatherInfo.futherWeather[i].low == null) {
            this.mWeatherInfo.futherWeather[i].low = "N";
        }
    }
}
