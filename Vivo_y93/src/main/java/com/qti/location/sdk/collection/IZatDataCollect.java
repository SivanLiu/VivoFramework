package com.qti.location.sdk.collection;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.qti.debugreport.IZatDebugConstants;
import com.qti.debugreport.IZatGpsTimeDebugReport;
import com.qti.debugreport.IZatLocationReport;
import com.qti.debugreport.IZatLocationReport.IzatLocationSource;
import com.qti.debugreport.IZatPDRDebugReport;
import com.qti.debugreport.IZatRfStateDebugReport;
import com.qti.debugreport.IZatUtcSpec;
import com.qti.debugreport.IZatXTRADebugReport;
import com.qti.location.sdk.IZatDebugReportingService;
import com.qti.location.sdk.IZatDebugReportingService.IZatDebugReportCallback;
import com.qti.location.sdk.IZatManager;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class IZatDataCollect {
    private static boolean DEBUG = SystemProperties.getBoolean(IZatGpsLocData.PROP_DEBUG, false);
    private static final int DELAY_LOST_LOCATION = 5000;
    private static final int DELAY_NAVIGATING_TIME_LIMIT = 5000;
    private static final int LOCATION_LOST_NO = 0;
    private static final int LOCATION_LOST_PENDING = 1;
    private static final int LOCATION_LOST_YES = 2;
    private static final float MAX_SPEED = 41.67f;
    private static final int MSG_CN0_LOW = 7;
    private static final int MSG_GET_NETWORK_STATE = 6;
    private static final int MSG_LOCATION_SUCCESS = 1;
    private static final int MSG_LOST_LOCATION = 5;
    private static final int MSG_LOST_RESUME = 9;
    private static final int MSG_OUT5S_FAILED = 2;
    private static final int MSG_SPEED_ABNORMAL = 8;
    private static final int MSG_STOP_NAVIGATING = 4;
    private static final String TAG = "IZatDataCollect";
    private static IZatDataCollect sInstance;
    private HandlerThread mCollectThread = null;
    private ConnectivityManager mConnMgr;
    private final Context mContext;
    private MyHandler mDataCollectHandler = null;
    private IZatDebugReportingService mDebugReportingService;
    private DebugReportCallback mDebugreportCb;
    private EZ mEz = new EZ();
    private long mFixRequestTime = -1;
    private IZatGpsLocData mGpsLocData = new IZatGpsLocData();
    private IZatTrialCollect mIZatTrialCollect = null;
    private IZatManager mIzatMgr;
    private int mLocationLost = LOCATION_LOST_NO;
    private long mLostResume = 0;
    private long mLostStart = 0;
    private int mReportCount = LOCATION_LOST_NO;
    private IzatLocationSource mReportInjectionSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
    private int mReportTimeUncertainity = -1;
    private boolean mReportXtraValid = false;
    private IZatSensorInfo mSensorInfo = null;
    private long mTimeToFirstFix = -1;
    private VivoCollectData mVivoCollectData = null;

    private class DebugReportCallback implements IZatDebugReportCallback {
        public void onDebugReportAvailable(Bundle reportObj) {
            if (reportObj != null) {
                IZatDataCollect iZatDataCollect = IZatDataCollect.this;
                iZatDataCollect.mReportCount = iZatDataCollect.mReportCount + 1;
                try {
                    reportObj.setClassLoader(IZatLocationReport.class.getClassLoader());
                    IZatLocationReport epiReport = (IZatLocationReport) reportObj.getParcelable(IZatDebugConstants.IZAT_DEBUG_EXTERNAL_POSITION_INJECTION_KEY);
                    if (epiReport != null) {
                        IZatUtcSpec timespec = epiReport.getUTCTimestamp();
                        if (IZatDataCollect.this.mReportInjectionSource == IzatLocationSource.POSITION_SOURCE_UNKNOWN && IZatDataCollect.this.mReportCount == 3 && epiReport.hasHorizontalFix()) {
                            IZatDataCollect.this.mReportInjectionSource = epiReport.getSource();
                            IZatDataCollect.this.mGpsLocData.onNlpInjectLocation(IZatDataCollect.this.mReportInjectionSource, (timespec.getSeconds() * 1000) + (timespec.getNanoSeconds() / 100000), epiReport.getLongitude(), epiReport.getLatitude());
                            if (IZatDataCollect.DEBUG) {
                                Log.d(IZatDataCollect.TAG, "mReportInjectionSource: yes " + IZatDataCollect.this.mReportInjectionSource + ", timespec:" + timespec.getSeconds() + ", " + timespec.getNanoSeconds());
                            }
                        } else if (IZatDataCollect.DEBUG) {
                            Log.d(IZatDataCollect.TAG, "mReportInjectionSource:" + IZatDataCollect.this.mReportInjectionSource + ", timespec:" + timespec.getSeconds() + ", " + timespec.getNanoSeconds());
                        }
                    }
                    reportObj.setClassLoader(IZatGpsTimeDebugReport.class.getClassLoader());
                    IZatGpsTimeDebugReport gpsTimeReport = (IZatGpsTimeDebugReport) reportObj.getParcelable(IZatDebugConstants.IZAT_DEBUG_GPS_TIME_KEY);
                    if (gpsTimeReport != null && IZatDataCollect.this.mReportTimeUncertainity == -1) {
                        String str;
                        IZatGpsLocData -get3 = IZatDataCollect.this.mGpsLocData;
                        if (gpsTimeReport.getTimeUncertainity() < 500) {
                            str = IZatGpsLocData.BOOT_TYPE_HOT;
                        } else {
                            str = IZatGpsLocData.BOOT_TYPE_WARM;
                        }
                        -get3.setBootType(str);
                        IZatDataCollect.this.mReportTimeUncertainity = gpsTimeReport.getTimeUncertainity();
                        if (IZatDataCollect.DEBUG) {
                            Log.d(IZatDataCollect.TAG, "mReportTimeUncertainity = " + IZatDataCollect.this.mReportTimeUncertainity);
                        }
                        if (!gpsTimeReport.IsTimeValid()) {
                            GpsLog.w(IZatDataCollect.TAG, "GPS TIME INVALID.");
                        }
                        if (IZatDataCollect.this.mReportTimeUncertainity >= 3000000) {
                            GpsLog.w(IZatDataCollect.TAG, "GPS TIME UNCERTAINITY " + IZatDataCollect.this.mReportTimeUncertainity);
                        }
                    }
                    reportObj.setClassLoader(IZatXTRADebugReport.class.getClassLoader());
                    IZatXTRADebugReport xtraReport = (IZatXTRADebugReport) reportObj.getParcelable(IZatDebugConstants.IZAT_DEBUG_XTRA_STATUS_KEY);
                    if (xtraReport != null) {
                        IZatDataCollect.this.mReportXtraValid = xtraReport.hasGpsXtraInfo();
                        IZatDataCollect.this.mGpsLocData.setXtraValid(IZatDataCollect.this.mReportXtraValid);
                        if (!IZatDataCollect.this.mReportXtraValid) {
                            GpsLog.w(IZatDataCollect.TAG, "GPS XTAR INVALID.");
                        }
                    }
                    reportObj.setClassLoader(IZatRfStateDebugReport.class.getClassLoader());
                    IZatRfStateDebugReport rfStateReport = (IZatRfStateDebugReport) reportObj.getParcelable(IZatDebugConstants.IZAT_DEBUG_RF_STATE_KEY);
                    int pgaGain = IZatDataCollect.LOCATION_LOST_NO;
                    long gpsBPAmpI = 0;
                    long pgsBPAmpQ = 0;
                    if (rfStateReport != null) {
                        pgaGain = rfStateReport.getPGAGain();
                        gpsBPAmpI = rfStateReport.getGPSBPAmpI();
                        pgsBPAmpQ = rfStateReport.getADCAmplitudeQ();
                        IZatDataCollect.this.mGpsLocData.setJammer(pgaGain, gpsBPAmpI, pgsBPAmpQ);
                    }
                    reportObj.setClassLoader(IZatPDRDebugReport.class.getClassLoader());
                    IZatPDRDebugReport pdrReport = (IZatPDRDebugReport) reportObj.getParcelable(IZatDebugConstants.IZAT_DEBUG_PDR_INFO_KEY);
                    boolean insEngaged = false;
                    if (pdrReport != null) {
                        insEngaged = pdrReport.isINSFilterEngaged();
                        IZatDataCollect.this.mGpsLocData.setSap(insEngaged);
                    }
                    if (IZatDataCollect.DEBUG) {
                        Log.d(IZatDataCollect.TAG, "onDebugReportAvailable, mReportInjectionSource:" + IZatDataCollect.this.mReportInjectionSource + ", mReportTimeUncertainity:" + IZatDataCollect.this.mReportTimeUncertainity + "us, mReportXtraValid:" + IZatDataCollect.this.mReportXtraValid + ", pgaGain:" + pgaGain + ", gpsBPAmpI:" + gpsBPAmpI + ", pgsBPAmpQ:" + pgsBPAmpQ + ", insEngaged:" + insEngaged);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                JSONObject obj = null;
                if (IZatDataCollect.DEBUG) {
                    Log.d(IZatDataCollect.TAG, "handleMessage " + IZatDataCollect.this.msgToString(msg.what));
                }
                switch (msg.what) {
                    case 1:
                    case 4:
                    case IZatDataCollect.MSG_CN0_LOW /*7*/:
                    case 8:
                        if (msg.obj != null) {
                            obj = msg.obj;
                            break;
                        }
                        break;
                    case 2:
                        obj = IZatDataCollect.this.mGpsLocData.getJson(IZatGpsLocData.TYPE_OVER5S);
                        GpsLog.w(IZatDataCollect.TAG, "TTFF OVER 5S");
                        break;
                    case 5:
                        IZatDataCollect.this.mLostStart = System.currentTimeMillis();
                        IZatDataCollect.this.mGpsLocData.setLostResumeStart(IZatDataCollect.this.mLostStart);
                        IZatDataCollect.this.mLocationLost = 2;
                        obj = IZatDataCollect.this.mGpsLocData.getJson(IZatGpsLocData.TYPE_LOST);
                        GpsLog.w(IZatDataCollect.TAG, "LOST STAR");
                        break;
                    case IZatDataCollect.MSG_GET_NETWORK_STATE /*6*/:
                        if (IZatDataCollect.this.mConnMgr != null) {
                            boolean wifi = IZatDataCollect.this.mConnMgr.getNetworkInfo(1).isConnected();
                            boolean internet = IZatDataCollect.this.mConnMgr.getNetworkInfo(IZatDataCollect.LOCATION_LOST_NO).isConnected();
                            IZatDataCollect.this.mGpsLocData.setNetwork(!wifi ? internet : true);
                            if (IZatDataCollect.DEBUG) {
                                String str = IZatDataCollect.TAG;
                                StringBuilder append = new StringBuilder().append("MSG_GET_NETWORK_STATE ");
                                if (wifi) {
                                    internet = true;
                                }
                                Log.d(str, append.append(internet).toString());
                                break;
                            }
                        }
                        break;
                    case IZatDataCollect.MSG_LOST_RESUME /*9*/:
                        obj = IZatDataCollect.this.mGpsLocData.getJson(IZatGpsLocData.TYPE_LOSTRESUME);
                        IZatDataCollect.this.mLocationLost = IZatDataCollect.LOCATION_LOST_NO;
                        GpsLog.w(IZatDataCollect.TAG, "LOST RESUME");
                        break;
                }
                if (obj == null) {
                    Log.d(IZatDataCollect.TAG, "handleMessage msg:" + msg.what + " obj=null.");
                    return;
                }
                IZatDataCollect.this.mSensorInfo.enableSensor();
                try {
                    obj.put(IZatGpsLocData.KEY_ACCELEMETER_INFO, IZatDataCollect.this.mSensorInfo.getJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                IZatDataCollect.this.mSensorInfo.disableSensor();
                HashMap<String, String> params = new HashMap(1);
                String info = obj.toString();
                String eInfo = IZatDataCollect.this.mEz.encrypt(info);
                params.put("info", eInfo);
                boolean control = false;
                if (IZatDataCollect.this.mVivoCollectData.getControlInfo("203")) {
                    IZatDataCollect.this.mVivoCollectData.writeData("203", "2032", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    control = true;
                }
                if (IZatDataCollect.DEBUG) {
                    Log.d(IZatDataCollect.TAG, "handleMessage control=" + control);
                    Log.d(IZatDataCollect.TAG, "handleMessage info=" + info);
                    Log.d(IZatDataCollect.TAG, "handleMessage eInfo=" + eInfo);
                    Log.d(IZatDataCollect.TAG, "handleMessage dInfo=" + IZatDataCollect.this.mEz.decrypt(eInfo));
                }
            }
        }
    }

    public static synchronized IZatDataCollect getInstance(Context context) {
        synchronized (IZatDataCollect.class) {
            if (context == null) {
                Log.w(TAG, "getInstance, context is null");
                return null;
            }
            if (sInstance == null) {
                sInstance = new IZatDataCollect(context);
            }
            IZatDataCollect iZatDataCollect = sInstance;
            return iZatDataCollect;
        }
    }

    private String msgToString(int msg) {
        switch (msg) {
            case 1:
                return "MSG_LOCATION_SUCCESS";
            case 2:
                return "MSG_OUT5S_FAILED";
            case 4:
                return "MSG_STOP_NAVIGATING";
            case 5:
                return "MSG_LOST_LOCATION";
            case MSG_GET_NETWORK_STATE /*6*/:
                return "MSG_GET_NETWORK_STATE";
            case MSG_CN0_LOW /*7*/:
                return "MSG_CN0_LOW";
            case 8:
                return "MSG_SPEED_ABNORMAL";
            case MSG_LOST_RESUME /*9*/:
                return "MSG_LOST_RESUME";
            default:
                return "MSG_UNKOWN";
        }
    }

    private IZatDataCollect(Context context) {
        this.mContext = context;
        this.mConnMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mCollectThread = new HandlerThread("izatdc");
        this.mCollectThread.start();
        this.mDataCollectHandler = new MyHandler(this.mCollectThread.getLooper());
        this.mSensorInfo = new IZatSensorInfo(context);
        this.mVivoCollectData = new VivoCollectData(context);
        this.mIZatTrialCollect = new IZatTrialCollect(context, this.mCollectThread.getLooper());
    }

    public void onSetRequest(String request, String source) {
        this.mGpsLocData.onSetRequest(request, source);
        this.mIZatTrialCollect.onSetRequest(request, source);
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
        IZatGpsLocData.setDebug(debug);
        IZatTrialCollect.setDebug(debug);
    }

    public void startNavigating(long fixRequestTime) {
        if (DEBUG) {
            Log.d(TAG, "startNavigating at " + fixRequestTime);
        }
        this.mIZatTrialCollect.onStartNavigating(fixRequestTime);
        this.mReportInjectionSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
        this.mFixRequestTime = fixRequestTime;
        this.mReportTimeUncertainity = -1;
        this.mLocationLost = LOCATION_LOST_NO;
        this.mReportCount = LOCATION_LOST_NO;
        this.mGpsLocData.startNavigating(fixRequestTime);
        this.mDataCollectHandler.sendEmptyMessageDelayed(2, 5000);
        this.mDataCollectHandler.sendEmptyMessageDelayed(MSG_GET_NETWORK_STATE, 1000);
        try {
            if (this.mDebugreportCb == null) {
                this.mDebugreportCb = new DebugReportCallback();
            }
            if (this.mIzatMgr == null || this.mDebugReportingService == null) {
                this.mIzatMgr = IZatManager.getInstance(this.mContext);
                this.mDebugReportingService = this.mIzatMgr.connectDebugReportingService();
            }
            if (this.mDebugReportingService != null) {
                this.mDebugReportingService.registerForDebugReports(this.mDebugreportCb);
                Log.d(TAG, "mDebugReportingService registerForDebugReports finished.");
                return;
            }
            Log.d(TAG, "mDebugReportingService null");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNlpPassiveLocation(long time, double longitude, double latitude) {
        long now = System.currentTimeMillis();
        this.mGpsLocData.onNlpPassiveLocation(now, longitude, latitude);
        if (DEBUG) {
            Log.d(TAG, "onNlpPassiveLocation time:" + now + " lon:" + longitude + " lat:" + latitude);
        }
    }

    public void stopNavigating(long now) {
        if (DEBUG) {
            Log.d(TAG, "stopNavigating");
        }
        this.mIZatTrialCollect.onStopNavigating(now);
        if (now - this.mFixRequestTime < 5000) {
            this.mDataCollectHandler.removeMessages(2);
        } else {
            if (this.mLocationLost == 2) {
                this.mGpsLocData.setLostResumeTime(-1);
                this.mDataCollectHandler.sendEmptyMessage(MSG_LOST_RESUME);
            }
            Message msg = this.mDataCollectHandler.obtainMessage(4);
            msg.obj = this.mGpsLocData.getJson(IZatGpsLocData.TYPE_STOP);
            this.mDataCollectHandler.sendMessage(msg);
        }
        if (!(this.mDebugreportCb == null || this.mDebugReportingService == null)) {
            try {
                this.mDebugReportingService.deregisterForDebugReports(this.mDebugreportCb);
                this.mIzatMgr.disconnectDebugReportingService(this.mDebugReportingService);
                this.mDebugReportingService = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (DEBUG) {
            Log.d(TAG, "stopNavigating finished.");
        }
    }

    public void updateSvStatus(int count, int[] svmSvidWithFlagss, float[] cn0) {
        if (DEBUG) {
            Log.d(TAG, "updateSvStatus");
        }
        this.mIZatTrialCollect.onUpdateSvStatus(count, svmSvidWithFlagss, cn0);
        getAverageSnrs(count, svmSvidWithFlagss, cn0);
    }

    public void onReportLocation(Location location) {
        if (location == null) {
            if (DEBUG) {
                Log.d(TAG, "onReportLocation location=null");
            }
            return;
        }
        this.mIZatTrialCollect.onReportLocation(location);
        onReportLocation(true, location.getLatitude(), location.getLongitude(), location.getSpeed());
    }

    public void onReportLocation(boolean hasLatLong, double latitude, double longitude, float speed) {
        if (DEBUG) {
            Log.d(TAG, "onReportLocation");
        }
        long now = System.currentTimeMillis();
        if (hasLatLong) {
            JSONObject obj;
            Message msg;
            this.mGpsLocData.onReportLocation(hasLatLong, now, latitude, longitude, speed);
            if (this.mTimeToFirstFix == -1) {
                this.mDataCollectHandler.removeMessages(2);
                this.mTimeToFirstFix = now - this.mFixRequestTime;
                if (DEBUG) {
                    Log.v(TAG, "mTimeToFirstFix:" + this.mTimeToFirstFix);
                }
                if (this.mTimeToFirstFix < 5000) {
                    obj = this.mGpsLocData.getJson(IZatGpsLocData.TYPE_IN5S);
                } else {
                    obj = this.mGpsLocData.getJson(IZatGpsLocData.TYPE_OVER5S);
                }
                msg = this.mDataCollectHandler.obtainMessage(1);
                msg.obj = obj;
                this.mDataCollectHandler.sendMessage(msg);
            }
            if (speed > MAX_SPEED) {
                obj = this.mGpsLocData.getJson(IZatGpsLocData.TYPE_SPEEDAB);
                msg = this.mDataCollectHandler.obtainMessage(8);
                msg.obj = obj;
                this.mDataCollectHandler.sendMessage(msg);
            }
            if (this.mLocationLost == 2) {
                this.mLocationLost = LOCATION_LOST_NO;
                this.mLostResume = System.currentTimeMillis() - this.mLostStart;
                this.mGpsLocData.setLostResumeTime(this.mLostResume);
                this.mDataCollectHandler.sendEmptyMessage(MSG_LOST_RESUME);
            }
            if (this.mLocationLost == 1) {
                this.mLocationLost = LOCATION_LOST_NO;
                this.mGpsLocData.setLostResumeTime(-1);
                this.mDataCollectHandler.removeMessages(5);
            }
        }
    }

    public void lostLocation() {
        if (DEBUG) {
            Log.d(TAG, "lostLocation = " + this.mLocationLost);
        }
        this.mIZatTrialCollect.onLost();
        if (this.mLocationLost == 0) {
            this.mLocationLost = 1;
            this.mDataCollectHandler.sendEmptyMessageDelayed(5, 5000);
        }
    }

    public void getAverageSnrs(int count, int[] mSvidWithFlags, float[] cn0) {
        this.mGpsLocData.getAverageSnrs(count, mSvidWithFlags, cn0);
        if (this.mGpsLocData.decideCn0Low()) {
            JSONObject obj = this.mGpsLocData.getJson(IZatGpsLocData.TYPE_CN0LOW);
            Message msg = this.mDataCollectHandler.obtainMessage(MSG_CN0_LOW);
            msg.obj = obj;
            this.mDataCollectHandler.sendMessage(msg);
        }
    }

    public void setNetwork(boolean data) {
    }
}
