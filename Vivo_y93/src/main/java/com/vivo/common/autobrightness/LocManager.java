package com.vivo.common.autobrightness;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import java.util.List;
import java.util.Locale;

public class LocManager implements LocationListener {
    private static final long HIGHEST_UPDATE_RATE = 3600000;
    private static final long LISTENER_DELAY_TIME = 60000;
    private static final int MSG_INIT = 1;
    private static final int MSG_REQUEST_LISTENER = 2;
    private static final int RETRY_TIME = 2;
    private static final String TAG = TextTool.makeTag("LocManager");
    private static LocManager sInstance;
    private final Uri URI_MTMP = Uri.parse("content://com.vivo.common.autobrightness/mtmp");
    private Context mContext;
    private BackgroundHandler mHandler;
    private long mLastUpdateTime;
    private String mLocation = "unknown";
    private LocationManager mLocationManager;
    private Looper mLooper;
    private int mRetryTime;
    private long mUpdateRate = HIGHEST_UPDATE_RATE;

    private class BackgroundHandler extends Handler {
        private BackgroundHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    LocManager.this.requestListener();
                    return;
                default:
                    return;
            }
        }
    }

    private LocManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        Slog.e(TAG, "not config, no need to do job");
    }

    public static LocManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LocManager.class) {
                if (sInstance == null) {
                    sInstance = new LocManager(context);
                }
            }
        }
        return sInstance;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("onStatusChanged, status = " + status);
    }

    public void onProviderEnabled(String provider) {
        log("onProviderEnabled");
    }

    public void onProviderDisabled(String provider) {
        log("onProviderDisabled");
    }

    public void onLocationChanged(Location location) {
        log("onLocationChanged has data back.");
        this.mLocationManager.removeUpdates(this);
        if (location != null) {
            String split = "&&";
            StringBuilder sb = new StringBuilder();
            double latitude = location.getLatitude();
            sb.append(latitude).append("&&");
            double longitude = location.getLongitude();
            sb.append(longitude).append("&&");
            List addList = null;
            try {
                addList = new Geocoder(this.mContext, Locale.ENGLISH).getFromLocation(latitude, longitude, 1);
            } catch (Exception e) {
                Slog.e(TAG, "Get as failed." + e);
            }
            if (addList == null || addList.size() <= 0 || addList.get(0) == null) {
                Slog.e(TAG, "Get the as is null.");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1").append("&&");
                sb.append("-1");
            } else {
                Address address = (Address) addList.get(0);
                String countryName = address.getCountryName();
                if (TextTool.isNull(countryName)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(countryName).append("&&");
                }
                String adminArea = address.getAdminArea();
                if (TextTool.isNull(adminArea)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(adminArea).append("&&");
                }
                String locality = address.getLocality();
                if (TextTool.isNull(locality)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(locality).append("&&");
                }
                String subAdminArea = address.getSubAdminArea();
                if (TextTool.isNull(subAdminArea)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(subAdminArea).append("&&");
                }
                String subLocality = address.getSubLocality();
                if (TextTool.isNull(subLocality)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(subLocality).append("&&");
                }
                String thoroughfare = address.getThoroughfare();
                if (TextTool.isNull(thoroughfare)) {
                    sb.append("-1").append("&&");
                } else {
                    sb.append(thoroughfare).append("&&");
                }
                String subThoroughfare = address.getSubThoroughfare();
                if (TextTool.isNull(subThoroughfare)) {
                    sb.append("-1");
                } else {
                    sb.append(subThoroughfare);
                }
            }
            log("Update loc info:" + sb.toString());
            this.mLocation = sb.toString();
        }
    }

    public void createJobber() {
        log("create job, mHandler is null: " + (this.mHandler == null));
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
            long delay = CommonUtil.getTime(4, 0, 0, 0) - System.currentTimeMillis();
            log("delay : " + delay);
            if (delay <= LISTENER_DELAY_TIME) {
                this.mHandler.sendEmptyMessageDelayed(2, LISTENER_DELAY_TIME);
            } else {
                this.mHandler.sendEmptyMessageDelayed(2, delay);
            }
        }
    }

    public void cancelJobber() {
        if (this.mLooper != null) {
            this.mLooper.quit();
        }
    }

    private void requestListener() {
        try {
            this.mHandler.removeMessages(2);
            long leadTime = (this.mLastUpdateTime + this.mUpdateRate) - System.currentTimeMillis();
            if (leadTime > 0) {
                Slog.e(TAG, "Lc has been updated 1 hours ago, waiting for the next update.");
                this.mHandler.sendEmptyMessageDelayed(2, leadTime);
                return;
            }
            this.mHandler.sendEmptyMessageDelayed(2, this.mUpdateRate);
            this.mLocationManager.removeUpdates(this);
            Slog.e(TAG, "Request listener start.");
            this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this);
        } catch (Exception e) {
            Slog.e(TAG, "exp:" + e);
            if (this.mRetryTime < 2) {
                this.mRetryTime++;
                Slog.e(TAG, "When exp retry.");
                try {
                    this.mLocationManager.removeUpdates(this);
                } catch (Exception e1) {
                    Slog.e(TAG, "requestListener: " + e1);
                }
                this.mHandler.removeMessages(2);
                this.mHandler.sendEmptyMessageDelayed(2, ((long) (this.mRetryTime * (this.mRetryTime + 1))) * LISTENER_DELAY_TIME);
            }
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initUpdateRate() {
        String[] projection = new String[]{"analysis_date"};
        String selection = "m_key=?";
        String[] selectionArgs = new String[]{"update_loc_info_rate"};
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(this.URI_MTMP, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                long updateRate = cursor.getLong(0);
                if (updateRate > HIGHEST_UPDATE_RATE) {
                    this.mUpdateRate = updateRate;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Slog.e(TAG, "exp:" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void setLocation(String value) {
        this.mLastUpdateTime = System.currentTimeMillis();
        ContentResolver cr = this.mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("btloc", value);
        try {
            cr.insert(this.URI_MTMP, cv);
        } catch (Exception e) {
            Slog.e(TAG, "exp:" + e);
        }
    }

    public String getLocation() {
        return this.mLocation;
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }
}
