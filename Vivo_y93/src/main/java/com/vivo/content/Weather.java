package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public final class Weather {
    public static final String ACTION_DATA_CHANGE = "com.vivo.weather.data.change";
    public static final String ACTION_UPDATE_ALARM = "com.vivo.weather.update.ALARM_ALERT";
    public static final String AUTHORITY = "com.vivo.weather.provider";
    public static final String AUTHORITY_CITY = "com.vivo.weather.provider.city";
    public static final int CITYAQI_INDEX_AQI = 2;
    public static final int CITYAQI_INDEX_CITY = 1;
    public static final int CITYAQI_INDEX_CO = 7;
    public static final int CITYAQI_INDEX_ID = 0;
    public static final int CITYAQI_INDEX_LEVEL = 9;
    public static final int CITYAQI_INDEX_NO2 = 4;
    public static final int CITYAQI_INDEX_O3 = 6;
    public static final int CITYAQI_INDEX_PM10 = 8;
    public static final int CITYAQI_INDEX_PM25 = 3;
    public static final int CITYAQI_INDEX_PUBLISHER = 11;
    public static final int CITYAQI_INDEX_PUBLISHTIME = 10;
    public static final int CITYAQI_INDEX_SO2 = 5;
    public static String[] CITYAQI_PROJECTION = new String[]{"_id", "city", CityAQI.AQI, CityAQI.PM25, CityAQI.NO2, CityAQI.SO2, CityAQI.O3, CityAQI.CO, CityAQI.PM10, "level", CityAQI.PUBLISH_TIME, "publisher"};
    public static final int CITYORDER_INDEX_ADD = 6;
    public static final int CITYORDER_INDEX_CITY = 1;
    public static final int CITYORDER_INDEX_CONDITION_REAL = 7;
    public static final int CITYORDER_INDEX_CURRENTDATE = 12;
    public static final int CITYORDER_INDEX_HIGH = 9;
    public static final int CITYORDER_INDEX_ID = 0;
    public static final int CITYORDER_INDEX_INTERVAL = 11;
    public static final int CITYORDER_INDEX_LOCAL = 5;
    public static final int CITYORDER_INDEX_LOW = 8;
    public static final int CITYORDER_INDEX_ORDERID = 4;
    public static final int CITYORDER_INDEX_RELEASETIME = 3;
    public static final int CITYORDER_INDEX_TEMP = 10;
    public static final int CITYORDER_INDEX_UPDATETIME = 2;
    public static String[] CITYORDER_NEW_PROJECTION = new String[]{"_id", "city", "updatetime", "releasetime", "orderid", "local", CityOrderNew.ADD, CityOrderNew.CONDITION_REAL, "low", "high", "temp", CityOrderNew.INTERVAL, CityOrderNew.CURRENTDATE};
    public static String[] CITYORDER_PROJECTION = new String[]{"_id", "city", "updatetime", "releasetime", "orderid"};
    public static final int CITY_INDEX_CITY = 6;
    public static final int CITY_INDEX_CITYID = 4;
    public static final int CITY_INDEX_CITYNAME = 5;
    public static final int CITY_INDEX_ID = 0;
    public static final int CITY_INDEX_PROVINCE = 3;
    public static final int CITY_INDEX_PROVINCEID = 1;
    public static final int CITY_INDEX_PROVINCENAME = 2;
    public static String[] CITY_PROJECTION = new String[]{"_id", City.PROVINCEID, City.PROVINCENAME, "province", City.CITYID, City.CITYNAME, "city"};
    public static final int CLOUDY_STATE_LIVE = 3;
    public static final int CONDITION_INDEX_CLOUDY = 21;
    public static final int CONDITION_INDEX_FOG = 23;
    public static final int CONDITION_INDEX_FREEZING = 1;
    public static final int CONDITION_INDEX_ICE = 2;
    public static final int CONDITION_INDEX_ID = 0;
    public static final int CONDITION_INDEX_RAIN = 4;
    public static final int CONDITION_INDEX_RAINBIG = 9;
    public static final int CONDITION_INDEX_RAINMEDIUM = 10;
    public static final int CONDITION_INDEX_RAINSMALL = 12;
    public static final int CONDITION_INDEX_RAINSNOW = 3;
    public static final int CONDITION_INDEX_RAINSTORMBIG = 6;
    public static final int CONDITION_INDEX_RAINSTORMMEDIUM = 7;
    public static final int CONDITION_INDEX_RAINSTORMSMALL = 8;
    public static final int CONDITION_INDEX_RAINTHUNDER = 11;
    public static final int CONDITION_INDEX_SANDMEDIUM = 18;
    public static final int CONDITION_INDEX_SANDSMALL = 19;
    public static final int CONDITION_INDEX_SANDSTORM = 17;
    public static final int CONDITION_INDEX_SNOW = 5;
    public static final int CONDITION_INDEX_SNOWBIG = 14;
    public static final int CONDITION_INDEX_SNOWMEDIUM = 15;
    public static final int CONDITION_INDEX_SNOWSMALL = 16;
    public static final int CONDITION_INDEX_SNOWSTORE = 13;
    public static final int CONDITION_INDEX_SUN = 22;
    public static final int CONDITION_INDEX_YIN = 20;
    public static String[] CONDITION_PROJECTION = new String[]{"_id", Condition.FREEZING, Condition.ICE, Condition.RAINSNOW, Condition.RAIN, Condition.SNOW, Condition.RAINSTORMBIG, Condition.RAINSTORMMEDIUM, Condition.RAINSTORMSMALL, Condition.RAINBIG, Condition.RAINMEDIUM, Condition.RAINTHUNDER, Condition.RAINSMALL, Condition.SNOWSTORE, Condition.SNOWBIG, Condition.SNOWMEDIUM, Condition.SNOWSMALL, Condition.SANDSTORM, Condition.SANDMEDIUM, Condition.SANDSMALL, Condition.YIN, Condition.CLOUDY, Condition.SUN, Condition.FOG};
    public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider");
    public static final Uri CONTENT_URI_CITY = Uri.parse("content://com.vivo.weather.provider.city");
    public static final int CURRENTCITY_INDEX_ID = 0;
    public static final int CURRENTCITY_INDEX_POSITION = 1;
    public static final int CURRENTCITY_INDEX_POSITION2 = 2;
    public static String[] CURRENTCITY_PROJECTION = new String[]{"_id", "position", CurrentCity.POSITION2};
    public static final int DEFAULT_UPDATEFREQUENCY = 3;
    public static final int FOG_STATE_LIVE = 5;
    public static final int HOURDATA_COUNT = 24;
    public static final int HOURDATA_INDEX_CITY = 1;
    public static final int HOURDATA_INDEX_COUNT = 5;
    public static final int HOURDATA_INDEX_ID = 0;
    public static final int HOURDATA_INDEX_PIC = 4;
    public static final int HOURDATA_INDEX_TEMP = 3;
    public static final int HOURDATA_INDEX_TIME = 2;
    public static String[] HOURDATA_PROJECTION = new String[]{"_id", "city", HourData.TIME, "temp", HourData.PIC, HourData.COUNT};
    public static final String KEY_AUTOUPDATE = "weather_update";
    public static final String KEY_AUTOUPDATETIME = "weather_auto_update_time";
    public static final String KEY_FIRSTBOOT = "weather_firstboot";
    public static final String KEY_LOCATEFAIL_COUNT = "weather_local_fail_count";
    public static final String KEY_UPDATEFREQUENCY = "weather_updatefrequency";
    public static final int LAUNCHERWEATHER_INDEX_ID = 0;
    public static final int LAUNCHERWEATHER_INDEX_STATE = 1;
    public static String[] LAUNCHERWEATHER_PROJECTION = new String[]{"_id", "state"};
    public static final int LOCALWEATHER_INDEX_ID = 0;
    public static final int LOCALWEATHER_INDEX_STATE = 1;
    public static String[] LOCALWEATHER_PROJECTION = new String[]{"_id", Localweather.LBSSTATE, Localweather.DOUBLECLOCK};
    public static final int LOCATION_INDEX_CITY = 2;
    public static final int LOCATION_INDEX_ID = 0;
    public static final int LOCATION_INDEX_LAC = 1;
    public static String[] LOCATION_PROJECTION = new String[]{"_id", "lac", "city"};
    public static final int RAINTHUNDER_STATE_LIVE = 0;
    public static final int RAIN_STATE_LIVE = 1;
    public static final int SNOW_STATE_LIVE = 2;
    public static final int SUN_STATE_LIVE = 4;
    private static final String TAG = "BBKWeather-frm";
    public static long TIMEGAP_DEFAULT = TIMEGAP_FORMAL;
    private static final long TIMEGAP_FORMAL = 7200;
    private static final long TIMEGAP_TEST = 600;
    private static int UPDATE_STATE_DEFAULT = 3;
    public static final int UPDATE_STATE_FORMAL = 3;
    public static final int UPDATE_STATE_TEST = 2;
    public static int URL_STATE_DEFAULT = 1;
    public static final int URL_STATE_FORMAL = 1;
    public static final int URL_STATE_TEST = 0;
    public static final String URL_WEATHER_DEFAULT = "http://weather.bbk.com:15000/getvivoweather/";
    private static final String URL_WEATHER_FORMAL = "http://weather.bbk.com:15000/getvivoweather/";
    public static final int WEATHERINDEX_INDEX_CARWASH = 3;
    public static final int WEATHERINDEX_INDEX_CITY = 1;
    public static final int WEATHERINDEX_INDEX_DATE = 2;
    public static final int WEATHERINDEX_INDEX_DRESS = 4;
    public static final int WEATHERINDEX_INDEX_ID = 0;
    public static final int WEATHERINDEX_INDEX_RAYS = 7;
    public static final int WEATHERINDEX_INDEX_SICK = 5;
    public static final int WEATHERINDEX_INDEX_SPORTS = 6;
    public static String[] WEATHERINDEX_PROJECTION = new String[]{"_id", "city", "date", "carwash", "dress", WeatherIndex.SICK, WeatherIndex.SPORTS, "rays"};
    public static final int WEATHERVERSION_ROM_1_0 = 1;
    public static final int WEATHERVERSION_ROM_1_5 = 2;
    public static final int WEATHERVERSION_ROM_2_0 = 1000;
    public static final int WEATHERVERSION_ROM_2_5_1 = 2000;
    public static final int WEATHERVERSION_ROM_3_0 = 3000;
    public static final int WEATHERVERSION_ROM_3_5 = 4000;
    public static final int WEATHERVERSION_ROM_4_0 = 5000;
    public static final int WEATHERVERSION_ROM_4_5 = 6000;
    public static final int WEATHERVERSION_ROM_5_0 = 7000;
    public static final int WEATHER_INDEX_BODYTEMP = 9;
    public static final int WEATHER_INDEX_CITY = 1;
    public static final int WEATHER_INDEX_CONDITION = 4;
    public static final int WEATHER_INDEX_DATE = 2;
    public static final int WEATHER_INDEX_DIRECTION = 5;
    public static final int WEATHER_INDEX_FEELSLIKE = 12;
    public static final int WEATHER_INDEX_FORECAST = 10;
    public static final int WEATHER_INDEX_HIGH = 8;
    public static final int WEATHER_INDEX_HUMIDITY = 11;
    public static final int WEATHER_INDEX_ID = 0;
    public static final int WEATHER_INDEX_LOW = 7;
    public static final int WEATHER_INDEX_WEEK = 3;
    public static final int WEATHER_INDEX_WIND = 6;
    public static String[] WEATHER_NEW_PROJECTION = new String[]{"_id", "city", "date", "week", "condition", "direction", "wind", "low", "high", "bodytemp", WeatherMessageNew.FORECAST, "humidity", WeatherMessageNew.FEELSLIKE};
    public static String[] WEATHER_PROJECTION = new String[]{"_id", "city", "date", "week", "condition", "direction", "wind", "low", "high", "bodytemp"};
    private Context mContext = null;
    private boolean mIsRom1_5 = false;
    private boolean mIsRom2_0 = false;
    private boolean mIsRom3_0 = false;
    private ContentResolver mResolver = null;
    private int mWeatherVersionCode = 2;
    private String str_CLOUDY = "";
    private String str_FOG = "";
    private String str_FREEZING = "";
    private String str_ICE = "";
    private String str_RAIN = "";
    private String str_RAINBIG = "";
    private String str_RAINMEDIUM = "";
    private String str_RAINSMALL = "";
    private String str_RAINSNOW = "";
    private String str_RAINSTORMBIG = "";
    private String str_RAINSTORMMEDIUM = "";
    private String str_RAINSTORMSMALL = "";
    private String str_RAINTHUNDER = "";
    private String str_SANDMEDIUM = "";
    private String str_SANDSMALL = "";
    private String str_SANDSTORM = "";
    private String str_SNOW = "";
    private String str_SNOWBIG = "";
    private String str_SNOWMEDIUM = "";
    private String str_SNOWSMALL = "";
    private String str_SNOWSTORE = "";
    private String str_SUN = "";
    private String str_YIN = "";

    public interface WeathersColumns {
    }

    public static final class AutoUpdate implements BaseColumns, WeathersColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/autoupdate");
        public static final String FREQUENCY = "frequency";
        public static final String IMEI = "imei";
        public static final String STATE = "state";
        public static final String TABLENAME = "autoupdate";
        public static final String UPGRADE = "upgrade";
        public static final String _ID = "_id";
    }

    public static final class City implements BaseColumns, WeathersColumns {
        public static final String AREA_ID = "area_id";
        public static final String CITY = "city";
        public static final String CITYID = "cityid";
        public static final String CITYNAME = "cityname";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider.city/city");
        public static final String PROVINCE = "province";
        public static final String PROVINCEID = "provinceid";
        public static final String PROVINCENAME = "provincename";
        public static final String TABLENAME = "city";
        public static final String _ID = "_id";
    }

    public static final class CityAQI implements BaseColumns, WeathersColumns {
        public static final String AQI = "aqi";
        public static final String AREA_ID = "area_id";
        public static final String CITY = "city";
        public static final String CO = "co";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/cityAQI");
        public static final String LEVEL = "level";
        public static final String NO2 = "no2";
        public static final String O3 = "o3";
        public static final String PM10 = "pm10";
        public static final String PM25 = "pm25";
        public static final String PUBLISHER = "publisher";
        public static final String PUBLISH_TIME = "publish_time";
        public static final String SO2 = "so2";
        public static final String TABLENAME = "cityAQI";
        public static final String _ID = "_id";
    }

    public static final class CityOrder implements BaseColumns, WeathersColumns {
        public static final String AREA_ID = "area_id";
        public static final String CITY = "city";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/cityorder");
        public static final String LOCAL = "local";
        public static final String ORDERID = "orderid";
        public static final String RELEASETIME = "releasetime";
        public static final String TABLENAME = "cityorder";
        public static final String UPDATETIME = "updatetime";
        public static final String _ID = "_id";
    }

    public static final class CityOrderNew implements BaseColumns, WeathersColumns {
        public static final String ADD = "added";
        public static final String AREA_ID = "area_id";
        public static final String BACKGROUND = "background";
        public static final String CITY = "city";
        public static final String CONDITION_REAL = "condition_real";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/cityorder");
        public static final String CURRENTDATE = "currentdate";
        public static final String HIGH = "high";
        public static final String ICON = "icon";
        public static final String INTERVAL = "interval";
        public static final String INVALID = "invalid";
        public static final String LOCAL = "local";
        public static final String LOW = "low";
        public static final String ORDERID = "orderid";
        public static final String RECOMMEND = "recommend";
        public static final String RELEASETIME = "releasetime";
        public static final String TABLENAME = "cityorder";
        public static final String TEMP = "temp";
        public static final String TIMEZONE = "timezone";
        public static final String UPDATETIME = "updatetime";
        public static final String _ID = "_id";
    }

    public class CityWeatherEntry {
        private String entryAreaId = "";
        private int entryBackground = 0;
        private String entryCity = "";
        private String entryCondition = "";
        private String entryCurrentTemp = "";
        private String entryHighTemp = "";
        private int entryIcon = 0;
        private String entryLowTemp = "";
        private String entryRealTime = "";
        private String entrySunrise = "";
        private String entrySunset = "";
        private String entryWindDir = "";
        private String entryWindPow = "";

        public void setCity(String city) {
            this.entryCity = city;
        }

        public void setAreaId(String areaId) {
            this.entryAreaId = areaId;
        }

        public void setCondition(String condition) {
            this.entryCondition = condition;
        }

        public void setCurrentTemp(String temp) {
            this.entryCurrentTemp = temp;
        }

        public void setHighTemp(String high) {
            this.entryHighTemp = high;
        }

        public void setLowTemp(String low) {
            this.entryLowTemp = low;
        }

        public void setWindPow(String windPower) {
            this.entryWindPow = windPower;
        }

        public void setWindDir(String windDir) {
            this.entryWindDir = windDir;
        }

        public void setSunrise(String sunrise) {
            this.entrySunrise = sunrise;
        }

        public void setSunset(String sunset) {
            this.entrySunset = sunset;
        }

        public void setRealTime(String time) {
            this.entryRealTime = time;
        }

        public void setBackground(int bg) {
            this.entryBackground = bg;
        }

        public void setIcon(int icon) {
            this.entryIcon = icon;
        }

        public String getCity() {
            return this.entryCity;
        }

        public String getAreaId() {
            return this.entryAreaId;
        }

        public String getCondition() {
            return this.entryCondition;
        }

        public String getCurrentTemp() {
            return this.entryCurrentTemp;
        }

        public String getHighTemp() {
            return this.entryHighTemp;
        }

        public String getLowTemp() {
            return this.entryLowTemp;
        }

        public String getWindPow() {
            return this.entryWindPow;
        }

        public String getWindDir() {
            return this.entryWindDir;
        }

        public String getSunrise() {
            return this.entrySunrise;
        }

        public String getSunset() {
            return this.entrySunset;
        }

        public String getRealTime() {
            return this.entryRealTime;
        }

        public int getBackground() {
            return this.entryBackground;
        }

        public int getIcon() {
            return this.entryIcon;
        }
    }

    public static class Condition implements BaseColumns, WeathersColumns {
        public static final String CLOUDY = "cloudy";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/condition");
        public static final String FOG = "fog";
        public static final String FREEZING = "freezing";
        public static final String ICE = "ice";
        public static final String RAIN = "rain";
        public static final String RAINBIG = "rainbig";
        public static final String RAINMEDIUM = "rainmedium";
        public static final String RAINSMALL = "rainsmall";
        public static final String RAINSNOW = "rainsnow";
        public static final String RAINSTORMBIG = "rainstormbig";
        public static final String RAINSTORMMEDIUM = "rainstormmedium";
        public static final String RAINSTORMSMALL = "rainstormsmall";
        public static final String RAINTHUNDER = "rainthunder";
        public static final String SANDMEDIUM = "sandmedium";
        public static final String SANDSMALL = "sandsmall";
        public static final String SANDSTORM = "sandstorm";
        public static final String SNOW = "snow";
        public static final String SNOWBIG = "snowbig";
        public static final String SNOWMEDIUM = "snowmedium";
        public static final String SNOWSMALL = "snowsmall";
        public static final String SNOWSTORE = "snowstore";
        public static final String SUN = "sun";
        public static final String TABLENAME = "condition";
        public static final String YIN = "yin";
        public static final String _ID = "_id";
    }

    public static final class CurrentCity implements BaseColumns, WeathersColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/currentcity");
        public static final String POSITION = "position";
        public static final String POSITION2 = "position2";
        public static final String TABLENAME = "currentcity";
        public static final String _ID = "_id";
    }

    public static final class HourData implements BaseColumns, WeathersColumns {
        public static final String AREA_ID = "area_id";
        public static final String CITY = "city";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/hourdata");
        public static final String COUNT = "count";
        public static final String PIC = "pic";
        public static final String TABLENAME = "hourdata";
        public static final String TEMP = "temp";
        public static final String TIME = "time";
        public static final String _ID = "_id";
    }

    public static final class Launcherweather implements BaseColumns, WeathersColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/launcherweather");
        public static final String STATE = "state";
        public static final String TABLENAME = "launcherweather";
        public static final String _ID = "_id";
    }

    public static final class Localweather implements BaseColumns, WeathersColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/localweather");
        public static final String DOUBLECLOCK = "doubleclock";
        public static final String LBSSTATE = "lbsstate";
        public static final String TABLENAME = "localweather";
        public static final String _ID = "_id";
    }

    public static final class Location implements BaseColumns, WeathersColumns {
        public static final String CITY = "city";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/location");
        public static final String LAC = "lac";
        public static final String TABLENAME = "location";
        public static final String _ID = "_id";
    }

    public static final class UsualCity implements BaseColumns, WeathersColumns {
        public static final String AREAID = "area_id";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/usualcity");
        public static final String LOCALITY = "locality";
        public static final String MARKDATE = "mark_date";
        public static final String MARKTIME = "mark_time";
        public static final String ORIGIN = "origin";
        public static final String PROVINCE = "province";
        public static final String SUBLOCALITY = "sublocality";
        public static final String TABLENAME = "usualcity";
        public static final String _ID = "_id";
    }

    public static final class WeatherAlert implements BaseColumns, WeathersColumns {
        public static final String AREA_ID = "area_id";
        public static final String CITY = "city";
        public static final String CONTENT = "content";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/alert");
        public static final String LEVEL = "level";
        public static final String PUBLISHER = "publisher";
        public static final String TABLENAME = "alert";
        public static final String TYPE = "type";
        public static final String _ID = "_id";
    }

    public static final class WeatherIndex implements BaseColumns, WeathersColumns {
        public static final String CARWASH = "carwash";
        public static final String CITY = "city";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/weatherindex");
        public static final String DATE = "date";
        public static final String DRESS = "dress";
        public static final String RAYS = "rays";
        public static final String SICK = "sick";
        public static final String SPORTS = "sports";
        public static final String TABLENAME = "weatherindex";
        public static final String _ID = "_id";
    }

    public static class WeatherMessage implements BaseColumns, WeathersColumns {
        public static final String AIR = "air";
        public static final String AIRMSG = "airmsg";
        public static final String AREA_ID = "area_id";
        public static final String BODYTEMP = "bodytemp";
        public static final String CARWASH = "carwash";
        public static final String CARWASHMSG = "carwashmsg";
        public static final String CITY = "city";
        public static final String CONDITION = "condition";
        public static final String CONTAMINATION = "contamination";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/weathermessage");
        public static final String CONTMSG = "contmsg";
        public static final String DATE = "date";
        public static final String DIRECTION = "direction";
        public static final String DRESS = "dress";
        public static final String DRESSMSG = "dressmsg";
        public static final String HIGH = "high";
        public static final String HUMIDITY = "humidity";
        public static final String LOCAL = "local";
        public static final String LOW = "low";
        public static final String RAYS = "rays";
        public static final String RAYSMSG = "raysmsg";
        public static final String TABLENAME = "weathermessage";
        public static final String WEEK = "week";
        public static final String WIND = "wind";
        public static final String _ID = "_id";
    }

    public static class WeatherMessageNew implements BaseColumns, WeathersColumns {
        public static final String AREA_ID = "area_id";
        public static final String BACKGROUND = "background";
        public static final String CITY = "city";
        public static final String CONDITION = "condition";
        public static final Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/weathermessage");
        public static final String DATE = "date";
        public static final String DIRECTION = "direction";
        public static final String FEELSLIKE = "feelslike";
        public static final String FORECAST = "forecast";
        public static final String HIGH = "high";
        public static final String HUMIDITY = "humidity";
        public static final String ICON = "icon";
        public static final String LOW = "low";
        public static final String SUNRISE = "sunrise";
        public static final String SUNSET = "sunset";
        public static final String TABLENAME = "weathermessage";
        public static final String TEMP = "bodytemp";
        public static final String WEEK = "week";
        public static final String WIND = "wind";
        public static final String _ID = "_id";
    }

    public Weather(Context context) {
        this.mContext = context;
        this.mResolver = this.mContext.getContentResolver();
        init();
    }

    private void init() {
        try {
            PackageInfo pkgInfo = this.mContext.getPackageManager().getPackageInfo("com.vivo.weather", 0);
            if (pkgInfo != null) {
                this.mWeatherVersionCode = pkgInfo.versionCode;
                if (this.mWeatherVersionCode == 2) {
                    this.mIsRom1_5 = true;
                } else if (this.mWeatherVersionCode > 2 && this.mWeatherVersionCode <= 1000) {
                    this.mIsRom2_0 = true;
                } else if (this.mWeatherVersionCode > 2000) {
                    this.mIsRom3_0 = true;
                }
            }
        } catch (NameNotFoundException e) {
            Log.v(TAG, "com.vivo.weather.provider not found " + e.getMessage());
        }
        if (TextUtils.isEmpty(this.str_FREEZING)) {
            Cursor cursor = null;
            try {
                cursor = this.mResolver.query(Condition.CONTENT_URI, CONDITION_PROJECTION, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    this.str_FREEZING = cursor.getString(1);
                    this.str_ICE = cursor.getString(2);
                    this.str_RAINSNOW = cursor.getString(3);
                    this.str_RAIN = cursor.getString(4);
                    this.str_SNOW = cursor.getString(5);
                    this.str_RAINSTORMBIG = cursor.getString(6);
                    this.str_RAINSTORMMEDIUM = cursor.getString(7);
                    this.str_RAINSTORMSMALL = cursor.getString(8);
                    this.str_RAINBIG = cursor.getString(9);
                    this.str_RAINMEDIUM = cursor.getString(10);
                    this.str_RAINTHUNDER = cursor.getString(11);
                    this.str_RAINSMALL = cursor.getString(12);
                    this.str_SNOWSTORE = cursor.getString(13);
                    this.str_SNOWBIG = cursor.getString(14);
                    this.str_SNOWMEDIUM = cursor.getString(15);
                    this.str_SNOWSMALL = cursor.getString(16);
                    this.str_SANDSTORM = cursor.getString(17);
                    this.str_SANDMEDIUM = cursor.getString(18);
                    this.str_SANDSMALL = cursor.getString(19);
                    this.str_YIN = cursor.getString(20);
                    this.str_CLOUDY = cursor.getString(21);
                    this.str_SUN = cursor.getString(22);
                    this.str_FOG = cursor.getString(23);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public int getWeatherVersion() {
        return this.mWeatherVersionCode;
    }

    public String getURL() {
        return "http://weather.bbk.com:15000/getvivoweather/";
    }

    public boolean isLBS() {
        boolean state = false;
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(Localweather.CONTENT_URI, null, "_id=1", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                state = cursor.getInt(cursor.getColumnIndex(Localweather.LBSSTATE)) == 1;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.v(TAG, "isLBS exception " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return state;
    }

    public int getCityNum() {
        return getCityNum(isLBS());
    }

    public int getCityNum(boolean lbsState) {
        int num = 0;
        Cursor cursor = null;
        try {
            cursor = queryOrderCity(lbsState, null);
            if (cursor == null || cursor.getCount() <= 0) {
                num = 0;
            } else {
                num = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.v(TAG, "getCityNum exception " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return num;
    }

    public Cursor queryOrderCity(boolean lbsState, String sortOrder) {
        String str = null;
        try {
            if (!this.mIsRom1_5) {
                ContentResolver contentResolver = this.mResolver;
                Uri uri = CityOrderNew.CONTENT_URI;
                String[] strArr = CITYORDER_NEW_PROJECTION;
                if (!lbsState) {
                    str = "added=1";
                }
                return contentResolver.query(uri, strArr, str, null, sortOrder);
            } else if (lbsState) {
                return this.mResolver.query(CityOrder.CONTENT_URI, CITYORDER_PROJECTION, null, null, sortOrder);
            } else {
                return this.mResolver.query(CityOrder.CONTENT_URI, CITYORDER_PROJECTION, "local=?", new String[]{"null"}, sortOrder);
            }
        } catch (Exception e) {
            Log.v(TAG, "queryOrderCity exception " + e.getMessage());
            return null;
        }
    }

    public int getCurrentCityPosition() {
        return getCurrentCityPosition(isLBS());
    }

    public int getCurrentCityPosition(boolean lbsState) {
        int pos = 0;
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(CurrentCity.CONTENT_URI, CURRENTCITY_PROJECTION, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                pos = this.mIsRom1_5 ? lbsState ? cursor.getInt(2) : cursor.getInt(1) : cursor.getInt(1);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.v(TAG, "getCurrentCityPosition exception " + e.getMessage());
            pos = 0;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pos;
    }

    public String getCurrentCity() {
        return getCurrentCity(isLBS());
    }

    public String getCurrentCity(boolean lbsState) {
        String city = "";
        Cursor cursor = null;
        try {
            cursor = queryOrderCity(lbsState, "orderid");
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return city;
            }
            int citynum = cursor.getCount();
            int position = getCurrentCityPosition(lbsState);
            if (position < 0) {
                position = 0;
            }
            if (position >= citynum) {
                position = 0;
            }
            if (cursor != null && cursor.moveToPosition(position)) {
                city = cursor.getString(cursor.getColumnIndex("city"));
            }
            if (cursor != null) {
                cursor.close();
            }
            return city;
        } catch (Exception e) {
            Log.v(TAG, "getCurrentCity exception " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void toPreCity() {
        Log.v(TAG, "toPreCity.");
        boolean lbsstate = isLBS();
        int position = getCurrentCityPosition(lbsstate);
        int citynum = getCityNum(lbsstate);
        if (citynum == 0) {
            position = 0;
        } else {
            position = ((position + citynum) - 1) % citynum;
        }
        sendBroadcast(lbsstate, position);
    }

    public void toNextCity() {
        Log.v(TAG, "toNextCity.");
        boolean lbsstate = isLBS();
        int position = getCurrentCityPosition(lbsstate);
        int citynum = getCityNum(lbsstate);
        if (citynum == 0) {
            position = 0;
        } else {
            position = ((position + citynum) + 1) % citynum;
        }
        sendBroadcast(lbsstate, position);
    }

    private void sendBroadcast(boolean lbsState, int position) {
        try {
            ContentValues values = new ContentValues();
            if (!this.mIsRom1_5) {
                values.put("position", Integer.valueOf(position));
            } else if (lbsState) {
                values.put("position", Integer.valueOf(position - 1));
                values.put(CurrentCity.POSITION2, Integer.valueOf(position));
            } else {
                values.put("position", Integer.valueOf(position));
                values.put(CurrentCity.POSITION2, Integer.valueOf(position + 1));
            }
            this.mResolver.update(CurrentCity.CONTENT_URI, values, "_id=1", null);
        } catch (Exception e) {
        }
        Intent intent = new Intent(ACTION_DATA_CHANGE);
        intent.putExtra("widgetupdatestate", 1);
        this.mContext.sendBroadcast(intent);
    }

    public Cursor queryCurrentWeather() {
        return queryCurrentWeather(isLBS(), getCurrentCityPosition(), 1);
    }

    public Cursor queryCurrentWeather(int position) {
        return queryCurrentWeather(isLBS(), position, 1);
    }

    public Cursor queryCurrentWeather(boolean lbsState, int position) {
        return queryCurrentWeather(lbsState, position, 1);
    }

    public Cursor queryCurrentWeather(boolean lbsState, int position, int Needfivedays) {
        if (this.mIsRom1_5) {
            return queryCurrentWeatherOld(lbsState, position, Needfivedays);
        }
        if (this.mIsRom2_0) {
            return queryCurrentWeatherNew(lbsState, position, Needfivedays);
        }
        return queryCurrentWeatherNewSdk(lbsState, position, Needfivedays);
    }

    /* JADX WARNING: Missing block: B:9:0x002d, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Cursor queryCurrentWeatherOld(boolean lbsState, int position, int needfivedays) {
        if (needfivedays <= 0 || needfivedays > 5) {
            Log.v(TAG, "queryCurrentWeatherOld failed days " + needfivedays);
            return null;
        }
        int count = getCityNum(lbsState);
        if (position < 0 || count == 0 || getCurrentCity() == null) {
            return null;
        }
        Cursor cursor;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Time time = new Time("Asia/Shanghai");
        time.setToNow();
        String dataBegin = format.format(Long.valueOf(time.normalize(true)));
        time.monthDay = (time.monthDay + needfivedays) - 1;
        String dataEnd = format.format(Long.valueOf(time.normalize(true)));
        if (lbsState && position == 0) {
            try {
                cursor = this.mResolver.query(WeatherMessage.CONTENT_URI, WEATHER_PROJECTION, "local!=? AND city=? AND date>=? AND date<=?", new String[]{"null", currentCityName, dataBegin, dataEnd}, null);
            } catch (Exception e) {
                Log.v(TAG, "queryCurrentWeatherOld exception " + e.getMessage());
                cursor = null;
            }
        } else {
            cursor = this.mResolver.query(WeatherMessage.CONTENT_URI, WEATHER_PROJECTION, "local=? AND city=? AND date>=? AND date<=?", new String[]{"null", currentCityName, dataBegin, dataEnd}, null);
        }
        return cursor;
    }

    /* JADX WARNING: Missing block: B:9:0x002d, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Cursor queryCurrentWeatherNew(boolean lbsState, int position, int needfivedays) {
        if (needfivedays <= 0 || needfivedays > 6) {
            Log.v(TAG, "queryCurrentWeatherNew failed days " + needfivedays);
            return null;
        }
        int count = getCityNum(lbsState);
        if (position < 0 || count == 0 || getCurrentCity() == null) {
            return null;
        }
        Cursor cursor;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Time time = new Time("Asia/Shanghai");
        time.setToNow();
        time.monthDay -= needfivedays == 6 ? 1 : 0;
        String dataBegin = format.format(Long.valueOf(time.normalize(true)));
        time.monthDay = (needfivedays == 6 ? needfivedays - 2 : needfivedays - 1) + time.monthDay;
        String dataEnd = format.format(Long.valueOf(time.normalize(true)));
        try {
            cursor = this.mResolver.query(WeatherMessageNew.CONTENT_URI, WEATHER_NEW_PROJECTION, "city=? AND date>=? AND date<=?", new String[]{currentCityName, dataBegin, dataEnd}, null);
        } catch (Exception e) {
            Log.v(TAG, "queryCurrentWeatherNew exception " + e.getMessage());
            cursor = null;
        }
        return cursor;
    }

    /* JADX WARNING: Missing block: B:7:0x0026, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Cursor queryCurrentWeatherNewSdk(boolean lbsState, int position, int needfivedays) {
        Cursor cursor = null;
        if (needfivedays <= 0) {
            Log.v(TAG, "queryCurrentWeatherNew failed days " + needfivedays);
            return null;
        }
        int count = getCityNum(lbsState);
        if (position < 0 || count == 0 || getCurrentCity() == null) {
            return null;
        }
        Cursor cursor2 = null;
        try {
            cursor2 = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "city=?", new String[]{getCurrentCity()}, null);
            if (cursor2 != null && cursor2.moveToFirst()) {
                if (!"".equals(getDayBegin(cursor2))) {
                    cursor = this.mResolver.query(WeatherMessageNew.CONTENT_URI, WEATHER_NEW_PROJECTION, "city=? AND date>=?", new String[]{currentCityName, getDayBegin(cursor2)}, null);
                }
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Exception e) {
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
        }
        return cursor;
    }

    public CityWeatherEntry getWidgetWeatherInfo(boolean local, boolean isDoubleClock) {
        CityWeatherEntry cityWeatherEntry = new CityWeatherEntry();
        Cursor cursor = null;
        Cursor weatherMessageCursor = null;
        long reaTimeMilles = 0;
        String[] cityInfo = new String[2];
        String city = "";
        String areaId = "";
        cityInfo = getWidgetCityInfo(local, isDoubleClock);
        if (cityInfo != null && cityInfo.length == 2) {
            city = cityInfo[0];
            areaId = cityInfo[1];
        }
        try {
            cursor = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "city = ? AND area_id = ?", new String[]{city, areaId}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int conditionIndex = cursor.getColumnIndex(CityOrderNew.CONDITION_REAL);
                int currentTempIndex = cursor.getColumnIndex("temp");
                int highTempIndex = cursor.getColumnIndex("high");
                int lowTempIndex = cursor.getColumnIndex("low");
                int backgroundIndex = cursor.getColumnIndex("background");
                int iconIndex = cursor.getColumnIndex("icon");
                int updateTimeIndex = cursor.getColumnIndex("updatetime");
                int currentDateIndex = cursor.getColumnIndex(CityOrderNew.CURRENTDATE);
                int invalidIndex = cursor.getColumnIndex(CityOrderNew.INVALID);
                String condition = cursor.getString(conditionIndex);
                String currentTemp = cursor.getString(currentTempIndex);
                String highTemp = cursor.getString(highTempIndex);
                String lowTemp = cursor.getString(lowTempIndex);
                int background = cursor.getInt(backgroundIndex);
                int icon = cursor.getInt(iconIndex);
                String updateTime = cursor.getString(updateTimeIndex);
                String currentDate = cursor.getString(currentDateIndex);
                int invalid = cursor.getInt(invalidIndex);
                cityWeatherEntry.setCity(city);
                cityWeatherEntry.setAreaId(areaId);
                cityWeatherEntry.setCondition(condition);
                cityWeatherEntry.setCurrentTemp(currentTemp);
                cityWeatherEntry.setHighTemp(highTemp);
                cityWeatherEntry.setLowTemp(lowTemp);
                cityWeatherEntry.setBackground(background);
                cityWeatherEntry.setIcon(icon);
                reaTimeMilles = getRealTime(updateTime, currentDate, invalid);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        try {
            String realTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Long.valueOf(reaTimeMilles));
            Log.v(TAG, "getWidgetWeatherInfo realTimeStr = " + realTimeStr);
            String[] realTime = realTimeStr.split(" ");
            if (realTime != null && realTime.length == 2) {
                String realDateStr = realTime[0].replace(NativeLibraryHelper.CLEAR_ABI_OVERRIDE, "");
                weatherMessageCursor = this.mResolver.query(WeatherMessageNew.CONTENT_URI, null, "city = ? AND area_id = ? AND date = ?", new String[]{city, areaId, realDateStr}, null);
                if (weatherMessageCursor != null && weatherMessageCursor.moveToFirst()) {
                    int windPowIndex = weatherMessageCursor.getColumnIndex("wind");
                    int windDirIndex = weatherMessageCursor.getColumnIndex("direction");
                    int sunriseIndex = weatherMessageCursor.getColumnIndex(WeatherMessageNew.SUNRISE);
                    int sunsetIndex = weatherMessageCursor.getColumnIndex(WeatherMessageNew.SUNSET);
                    String windPow = weatherMessageCursor.getString(windPowIndex);
                    String windDir = weatherMessageCursor.getString(windDirIndex);
                    String sunrise = weatherMessageCursor.getString(sunriseIndex);
                    String sunset = weatherMessageCursor.getString(sunsetIndex);
                    cityWeatherEntry.setWindPow(windPow);
                    cityWeatherEntry.setWindDir(windDir);
                    cityWeatherEntry.setSunrise(sunrise);
                    cityWeatherEntry.setSunset(sunset);
                    cityWeatherEntry.setRealTime(realTimeStr);
                }
            }
            if (weatherMessageCursor != null) {
                weatherMessageCursor.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (weatherMessageCursor != null) {
                weatherMessageCursor.close();
            }
        } catch (Throwable th2) {
            if (weatherMessageCursor != null) {
                weatherMessageCursor.close();
            }
        }
        return cityWeatherEntry;
    }

    private String[] getWidgetCityInfo(boolean local, boolean isDoubleClock) {
        String[] cityInfo = new String[2];
        String city = "";
        String areaId = "";
        Cursor cursor = null;
        int cityIndex;
        int areaIdIndex;
        if (local) {
            try {
                cursor = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "local = ?", new String[]{"local"}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cityIndex = cursor.getColumnIndex("city");
                    areaIdIndex = cursor.getColumnIndex("area_id");
                    city = cursor.getString(cityIndex);
                    areaId = cursor.getString(areaIdIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (isDoubleClock) {
            cursor = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "added = ? AND recommend = ?", new String[]{"1", "recommend_manual"}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                Cursor cursor2 = null;
                try {
                    cursor2 = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "added = ? AND recommend = ?", new String[]{"1", "recommend_auto"}, null);
                    if (cursor2 != null && cursor2.moveToFirst()) {
                        cityIndex = cursor2.getColumnIndex("city");
                        areaIdIndex = cursor2.getColumnIndex("area_id");
                        city = cursor2.getString(cityIndex);
                        areaId = cursor2.getString(areaIdIndex);
                        Log.v(TAG, "getWidgetCityInfo recommend_auto city = " + city + ",areaId = " + areaId);
                    }
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                } catch (Throwable th2) {
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                }
            } else {
                cityIndex = cursor.getColumnIndex("city");
                areaIdIndex = cursor.getColumnIndex("area_id");
                city = cursor.getString(cityIndex);
                areaId = cursor.getString(areaIdIndex);
                Log.v(TAG, "getWidgetCityInfo recommend_manual city = " + city + ",areaId = " + areaId);
            }
        } else {
            cursor = this.mResolver.query(CityOrderNew.CONTENT_URI, null, "added = ?", new String[]{"1"}, "orderid");
            if (cursor != null && cursor.moveToFirst()) {
                cityIndex = cursor.getColumnIndex("city");
                areaIdIndex = cursor.getColumnIndex("area_id");
                city = cursor.getString(cityIndex);
                areaId = cursor.getString(areaIdIndex);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        cityInfo[0] = city;
        cityInfo[1] = areaId;
        return cityInfo;
    }

    public String getDayBegin(Cursor cursor) {
        String dayBegin = "";
        String updateTimeStr = "";
        String currentDateStr = "";
        Date validDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        if (cursor == null) {
            return dayBegin;
        }
        try {
            int invalidIndex = cursor.getColumnIndex(CityOrderNew.INVALID);
            int updateTimeIndex = cursor.getColumnIndex("updatetime");
            int currentDateIndex = cursor.getColumnIndex(CityOrderNew.CURRENTDATE);
            updateTimeStr = cursor.getString(updateTimeIndex);
            currentDateStr = cursor.getString(currentDateIndex);
            int invalid = cursor.getInt(invalidIndex);
            if (updateTimeStr.equals("") || currentDateStr.equals("") || invalid == 1) {
                currentDateStr = "";
                return dayBegin;
            }
            int dayMargin;
            long now = System.currentTimeMillis();
            Date updateTimeDate = simpleDateFormat.parse(updateTimeStr);
            Date currentDate = simpleDateFormat.parse(currentDateStr);
            long localDiff = now - updateTimeDate.getTime();
            validDate.setTime(currentDate.getTime() + localDiff);
            if (localDiff <= 0) {
                dayMargin = 0;
            } else {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH", Locale.ENGLISH);
                Date currentDateHour = simpleDateFormat.parse(simpleDateFormat.format(currentDate));
                dayMargin = (currentDateHour.getHours() + ((int) ((simpleDateFormat.parse(simpleDateFormat.format(validDate)).getTime() - currentDateHour.getTime()) / DateUtils.HOUR_IN_MILLIS))) / 24;
            }
            return dateFormat.format(new Date(currentDate.getTime() + (((long) dayMargin) * DateUtils.DAY_IN_MILLIS)));
        } catch (Exception e) {
            e.printStackTrace();
            return dayBegin;
        }
    }

    private long getRealTime(String localTime, String internetTime, int invalid) {
        long realTimeMilles = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        if ("".equals(localTime) || "".equals(internetTime) || invalid == 1) {
            Log.v(TAG, "getRealTime err localTime = " + localTime + ",internetTime = " + internetTime + ",invalid = " + invalid);
            return 0;
        }
        long now = System.currentTimeMillis();
        try {
            Date localTimeDate = format.parse(localTime);
            realTimeMilles = format.parse(internetTime).getTime() + (now - localTimeDate.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realTimeMilles;
    }

    public HashMap<String, Integer> getWeatherState(String condition) {
        if (condition == null) {
            return null;
        }
        int staticState;
        int liveState;
        if (condition.contains(this.str_RAINSNOW) || (condition.contains(this.str_RAIN) && condition.contains(this.str_SNOW))) {
            staticState = 1;
            liveState = 1;
        } else if (condition.contains(this.str_FREEZING) || condition.contains(this.str_ICE)) {
            staticState = 3;
            liveState = 1;
        } else if (condition.contains(this.str_RAINSTORMBIG)) {
            staticState = 6;
            liveState = 1;
        } else if (condition.contains(this.str_RAINSTORMMEDIUM)) {
            staticState = 7;
            liveState = 1;
        } else if (condition.contains(this.str_RAINSTORMSMALL)) {
            staticState = 8;
            liveState = 1;
        } else if (condition.contains(this.str_RAINBIG)) {
            staticState = 9;
            liveState = 1;
        } else if (condition.contains(this.str_RAINMEDIUM)) {
            staticState = 10;
            liveState = 1;
        } else if (condition.contains(this.str_RAINTHUNDER)) {
            staticState = 11;
            liveState = 0;
        } else if (condition.contains(this.str_RAINSMALL) || condition.contains(this.str_RAIN)) {
            staticState = 12;
            liveState = 1;
        } else if (condition.contains(this.str_SNOWBIG) || condition.contains(this.str_SNOWSTORE)) {
            staticState = 14;
            liveState = 2;
        } else if (condition.contains(this.str_SNOWMEDIUM)) {
            staticState = 15;
            liveState = 2;
        } else if (condition.contains(this.str_SNOWSMALL) || condition.contains(this.str_SNOW)) {
            staticState = 16;
            liveState = 2;
        } else if (condition.contains(this.str_SANDSTORM)) {
            staticState = 17;
            liveState = 3;
        } else if (condition.contains(this.str_SANDSMALL) || condition.contains(this.str_SANDMEDIUM)) {
            staticState = 19;
            liveState = 3;
        } else if (condition.contains(this.str_FOG)) {
            staticState = 23;
            liveState = 3;
        } else if (condition.contains(this.str_YIN) || condition.contains(this.str_CLOUDY)) {
            staticState = 21;
            liveState = 3;
        } else if (condition.contains(this.str_SUN)) {
            staticState = 22;
            liveState = 4;
        } else {
            staticState = 21;
            liveState = 3;
        }
        HashMap<String, Integer> map = new HashMap();
        map.put("static", Integer.valueOf(staticState - 1));
        map.put("live", Integer.valueOf(liveState));
        return map;
    }

    public int getCurrentState() {
        int state = 3;
        boolean lbsstate = isLBS();
        Cursor cursor = null;
        try {
            cursor = queryCurrentWeather(lbsstate, getCurrentCityPosition(lbsstate));
            if (cursor != null && cursor.moveToFirst()) {
                state = ((Integer) getWeatherState(cursor.getString(cursor.getColumnIndex(CityOrderNew.CONDITION_REAL))).get("live")).intValue();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return state;
    }

    public void setURLState(int state) {
        URL_STATE_DEFAULT = state;
    }

    public void setUpdateState(int state) {
        UPDATE_STATE_DEFAULT = state;
    }

    public long getUpdateTime() {
        if (UPDATE_STATE_DEFAULT == 2) {
            TIMEGAP_DEFAULT = TIMEGAP_TEST;
        } else {
            TIMEGAP_DEFAULT = TIMEGAP_FORMAL;
        }
        return TIMEGAP_DEFAULT;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0039  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<String> backup() {
        ArrayList<String> cityList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = queryOrderCity(false, "orderid");
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return cityList;
            }
            do {
                String city = cursor.getString(cursor.getColumnIndex("city"));
                if (!(city == null || (city.equals("") ^ 1) == 0)) {
                    cityList.add(city);
                }
            } while (cursor.moveToNext());
            if (cursor != null) {
            }
            return cityList;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int add(String city) {
        int ret = -1;
        boolean add = false;
        Cursor cursor = null;
        try {
            cursor = queryOrderCity(isLBS(), "orderid");
            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getCount();
                if (count >= 9) {
                    add = true;
                    ret = -1;
                } else {
                    while (!cursor.getString(cursor.getColumnIndex("city")).equals(city)) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                    }
                    if (!"".equals(cursor.getString(cursor.getColumnIndex("local")))) {
                        ContentValues values = new ContentValues();
                        values.put(CityOrderNew.ADD, Integer.valueOf(1));
                        this.mContext.getContentResolver().update(CityOrderNew.CONTENT_URI, values, "city=?", new String[]{city});
                    }
                    add = true;
                    ret = 0;
                }
            }
            if (!add) {
                int i;
                ContentValues cityOrderValues = new ContentValues();
                cityOrderValues.put("city", city);
                cityOrderValues.put(CityOrderNew.ADD, Integer.valueOf(1));
                cityOrderValues.put("orderid", Integer.valueOf(count + 1));
                this.mContext.getContentResolver().insert(CityOrderNew.CONTENT_URI, cityOrderValues);
                for (i = 0; i < 5; i++) {
                    ContentValues weatherMsgValues = new ContentValues();
                    weatherMsgValues.put("city", city);
                    this.mContext.getContentResolver().insert(WeatherMessageNew.CONTENT_URI, weatherMsgValues);
                }
                ContentValues indexValues = new ContentValues();
                indexValues.put("city", city);
                this.mContext.getContentResolver().insert(WeatherIndex.CONTENT_URI, indexValues);
                ContentValues aqiValues = new ContentValues();
                aqiValues.put("city", city);
                this.mContext.getContentResolver().insert(CityAQI.CONTENT_URI, aqiValues);
                for (i = 0; i < 24; i++) {
                    ContentValues hourdataValues = new ContentValues();
                    hourdataValues.put("city", city);
                    this.mContext.getContentResolver().insert(HourData.CONTENT_URI, hourdataValues);
                }
                ret = 0;
                this.mContext.sendBroadcast(new Intent("com.vivo.weather.ACTION_ADDCITY"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    public int del(String city) {
        try {
            int ret = this.mContext.getContentResolver().delete(CityOrderNew.CONTENT_URI, "city=?", new String[]{city});
            if (ret == -1) {
                return ret;
            }
            this.mContext.getContentResolver().delete(WeatherMessageNew.CONTENT_URI, "city=?", new String[]{city});
            this.mContext.getContentResolver().delete(HourData.CONTENT_URI, "city=?", new String[]{city});
            this.mContext.getContentResolver().delete(CityAQI.CONTENT_URI, "city=?", new String[]{city});
            this.mContext.getContentResolver().delete(WeatherIndex.CONTENT_URI, "city=?", new String[]{city});
            this.mContext.getContentResolver().delete(Uri.parse("content://com.vivo.weather.provider/alert"), "city=?", new String[]{city});
            ContentValues curPosValues = new ContentValues();
            curPosValues.put("position", Integer.valueOf(0));
            this.mContext.getContentResolver().update(CurrentCity.CONTENT_URI, curPosValues, "_id=1", null);
            this.mContext.sendBroadcast(new Intent("com.vivo.weather.ACTION_DELCITY"));
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
