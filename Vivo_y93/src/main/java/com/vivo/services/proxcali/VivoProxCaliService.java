package com.vivo.services.proxcali;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.VivoCollectData;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.proximity.ProximityExceptionCollect;
import com.vivo.services.DeviceParaProvideService;
import com.vivo.services.epm.util.MessageCenterHelper;
import com.vivo.services.rms.ProcessList;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vivo.app.proxcali.IVivoProxCali.Stub;

public class VivoProxCaliService extends Stub {
    private static String ALS_PARA_INDEX_PATH = "/sys/bus/platform/drivers/als_ps/als_para_index";
    private static final int APDS9960_PROXIMITY_RAW_MARGIN = 10;
    private static final String BASE_THRESHOLD_SENSOR = "persist.sys.base_threshold_prox";
    private static final String BASE_THRESHOLD_SENSOR_SECOND = "persist.sys.ps_cali_data_short";
    private static String BOARD_VERSION_PATH = "/sys/networktype/networktype";
    private static final String COLLECT_DATA_EVENTID = "1032";
    private static final String COLLECT_DATA_LABLE_FAILED = "10322";
    private static final String COLLECT_DATA_LABLE_SUCCESS = "10321";
    private static final int GP2AP052A_PROXIMITY_RAW_MARGIN = 400;
    private static final int LIGHT_TIMES_LIMIT = 12;
    private static final int MAX_CALI_RETRY_TIMES = 5;
    private static final String NEED_CHANGE_PROXIMITY_PULSE = "persist.sys.need_change_pulse";
    private static String PD1216_TOUCHPANEL_ID_PATH = "/sys/touchscreen/firmware_module_id";
    private static String PD1225_HARDWARE_VER_A = "1";
    private static String PD1225_HARDWARE_VER_B = "2";
    private static String PD1225_HARDWARE_VER_C = "3";
    private static String PD1225_HARDWARE_VER_D = "4";
    private static String PD1225_HARDWARE_VER_E = "5";
    private static String PD1225_HARDWARE_VER_F = "6";
    private static final String PROP_DIRECT_CALL_TIME = "debug.direct.call.stamp";
    private static final String PROP_SENSOR_DUMP_KEY = "debug.sensor.dump";
    public static final int PROXIMITY_BOOTUP_CALI = 0;
    public static final int PROXIMITY_PHONE_CALI = 2;
    public static final int PROXIMITY_POWERKEY_CALI = 1;
    private static final int PROXIMITY_TIMES_LIMIT = 12;
    public static final int PROXIMITY_UNKNOWN_CALI = -1;
    private static final String PS_CALI_FLAG = "persist.sys.ps_cali_flag";
    private static final String PS_CALI_OFFSET_DATA = "persist.sys.ps_cali_offset_data";
    private static final String PS_CALI_OFFSET_FLAG = "persist.sys.ps_offset";
    private static final int PS_DRIVER_TEMP_CALI = 527;
    private static String PS_PARA_INDEX_PATH = "/sys/bus/platform/drivers/als_ps/ps_para_index";
    private static String PS_PULSE_VALUE_PATH = "/sys/bus/platform/drivers/als_ps/pulse";
    private static final int PS_SET_CALI_OFFSET_DATA = 524;
    private static final int PS_SET_ENG_CALI_DATA = 520;
    private static final int SENSOR_COMMAND_SET_PS_CALI_DATA = 22;
    private static final int SENSOR_COMMAND_SET_PS_CALI_OFFSET_DATA = 24;
    private static final int SENSOR_COMMAND_SET_PS_DRIVER_TEMP_CALI = 25;
    private static final int SENSOR_COMMAND_SET_PS_PARA_INDEX = 9;
    private static final String SENSOR_DUMP_DISABLED = "disabled";
    private static final String SENSOR_DUMP_ENABLED = "enabled";
    private static final int STATE_PHONE_CALLING = 32;
    private static final int STATE_PHONE_UNDER_FLAT = 64;
    private static final int STATE_PROXIMITY_SCREEN_OFF_WAKE_LOCK = 16;
    private static final String TAG = "VivoProxCaliService";
    private static final int TMD2772_PROXIMITY_RAW_MARGIN = 50;
    private static final String TMP_BASE_THRESHOLD_SENSOR = "persist.sys.tmp_base_thres_prox";
    private static int mCaliStartBy = -1;
    private static boolean mIsCalibrationing = false;
    private static boolean mIsDriverProxTempCali = AblConfig.isDriverProxTempCali();
    private static boolean mIsUseInstantCali = AblConfig.isUseInstantCali();
    private static boolean mIsUseVST = true;
    private static boolean mIsVerifying = false;
    private static boolean mNeedStopCali = false;
    private static boolean mNotNeedDoProxCali = false;
    private static final String mOpEntry = SystemProperties.get("ro.vivo.op.entry", "no");
    private static final String mProductId = SystemProperties.get("ro.product.model.bbk");
    private static final String[] mUseUltrasound = new String[]{"TD1703"};
    private final IntentFilter mBootCompleteFilter;
    private final BroadcastReceiver mBootPsCaliReceiver;
    private Context mContext;
    private DeviceParaProvideService mDeviceParaProvideService;
    private long mDirectCallTime;
    private boolean mIsCrystalAnim;
    private float mLightCaliValue;
    private int mLightCounts;
    private Sensor mLightSensor;
    private SensorEventListener mLightSensorListener;
    private float mLightThreshould;
    private HandlerThread mProxCaliThread;
    private ProximityExceptionCollect mProximityExceptionCollect;
    private Sensor mProximitySensor;
    private SensorEventListener mProximitySensorListenerVST;
    private final IntentFilter mScreenOffFilter;
    private final IntentFilter mScreenOnFilter;
    private SensorDump mSensorDump;
    private SensorManager mSensorManager;
    private Runnable mStartCaliRunnable;
    private Handler mStartHandler;
    private Runnable mStopCaliRunnable;
    private Handler mStopHandler;
    private int[] mTestArg;
    private SensorTestResult mTestResult;
    public VivoCollectData mVivoCollectData;
    private VivoSensorTest mVivoSensorTest;
    private WakeLock mWakeLock;
    private PowerManager pm;

    private final class BootcompleteReceiverForPsCali extends BroadcastReceiver {
        /* synthetic */ BootcompleteReceiverForPsCali(VivoProxCaliService this$0, BootcompleteReceiverForPsCali -this1) {
            this();
        }

        private BootcompleteReceiverForPsCali() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String ProxSensorName = "Null";
                if (VivoProxCaliService.this.mProximitySensor != null) {
                    ProxSensorName = VivoProxCaliService.this.mProximitySensor.getName();
                }
                String prop;
                SensorTestResult mTempRes;
                int[] mTempTestArg;
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    Log.d(VivoProxCaliService.TAG, "startCalibration(0) when bootcompleted...");
                    prop = SystemProperties.get("ro.product.model.bbk", null);
                    int psCaliFlag = SystemProperties.getInt(VivoProxCaliService.PS_CALI_FLAG, 0);
                    Log.d(VivoProxCaliService.TAG, "psCaliFlag = " + psCaliFlag + ", prop = " + prop);
                    int longBaseValue;
                    int shortBaseValue;
                    int offsetFlag;
                    String caliOffsetDataStr;
                    int offsetNS;
                    int offsetWE;
                    String[] strs;
                    int BaseValue;
                    if (prop != null && prop.toLowerCase().startsWith("pd1635")) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        longBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR, 4000);
                        shortBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR_SECOND, 8000);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        mTempTestArg[1] = longBaseValue;
                        mTempTestArg[2] = shortBaseValue;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                        }
                    } else if (psCaliFlag == 1 && prop != null && prop.toLowerCase().startsWith("pd1709")) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        offsetFlag = SystemProperties.getInt(VivoProxCaliService.PS_CALI_OFFSET_FLAG, 0);
                        if (offsetFlag == 1) {
                            caliOffsetDataStr = SystemProperties.get(VivoProxCaliService.PS_CALI_OFFSET_DATA, "unknown");
                            offsetNS = 0;
                            offsetWE = 0;
                            if (!caliOffsetDataStr.equals("unknown")) {
                                strs = caliOffsetDataStr.split(",");
                                if (strs.length == 2) {
                                    offsetNS = Integer.parseInt(strs[0].trim());
                                    offsetWE = Integer.parseInt(strs[1].trim());
                                }
                            }
                            mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_OFFSET_DATA;
                            mTempTestArg[1] = offsetNS;
                            mTempTestArg[2] = offsetWE;
                            if (VivoProxCaliService.this.mVivoSensorTest != null) {
                                VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_CALI_OFFSET_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                                Log.d(VivoProxCaliService.TAG, "proximity cali_off_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                            }
                        }
                        shortBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR_SECOND, MessageCenterHelper.REBIND_SERVICE_TIME_INTERVAL);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        mTempTestArg[1] = offsetFlag;
                        mTempTestArg[2] = shortBaseValue;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                        }
                    } else if (psCaliFlag == 1 && prop != null && (prop.toLowerCase().startsWith("pd1710") || prop.toLowerCase().startsWith("pd1721"))) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        offsetFlag = SystemProperties.getInt(VivoProxCaliService.PS_CALI_OFFSET_FLAG, 0);
                        caliOffsetDataStr = SystemProperties.get(VivoProxCaliService.PS_CALI_OFFSET_DATA, "unknown");
                        offsetNS = 0;
                        offsetWE = 0;
                        if (!caliOffsetDataStr.equals("unknown")) {
                            strs = caliOffsetDataStr.split(",");
                            if (strs.length == 2) {
                                offsetNS = Integer.parseInt(strs[0].trim());
                                offsetWE = Integer.parseInt(strs[1].trim());
                            }
                        }
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_OFFSET_DATA;
                        mTempTestArg[1] = offsetNS;
                        mTempTestArg[2] = offsetWE;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_CALI_OFFSET_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_off_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                        }
                        shortBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR_SECOND, MessageCenterHelper.REBIND_SERVICE_TIME_INTERVAL);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        Log.d(VivoProxCaliService.TAG, "set_ps_cali_data: offsetFlag " + offsetFlag + " psCaliFlag " + psCaliFlag);
                        mTempTestArg[1] = ((offsetFlag << 4) & 240) | (psCaliFlag & 15);
                        mTempTestArg[2] = shortBaseValue;
                        Log.d(VivoProxCaliService.TAG, "set_ps_cali_data: data[0]" + mTempTestArg[0] + " data[1] " + mTempTestArg[1] + " data[2] " + mTempTestArg[2]);
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                        }
                    } else if (psCaliFlag == 1 && (ProxSensorName.toLowerCase().startsWith("apds9151") || ((prop != null && (prop.toLowerCase().startsWith("td1705") || prop.toLowerCase().startsWith("pd1801"))) || ProxSensorName.toLowerCase().startsWith("apds-9160") || prop.toLowerCase().startsWith("pd1731")))) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        if (prop.toLowerCase().startsWith("pd1728")) {
                            offsetFlag = SystemProperties.getInt(VivoProxCaliService.PS_CALI_OFFSET_FLAG, 0);
                            if (offsetFlag == 1) {
                                mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_OFFSET_DATA;
                                mTempTestArg[1] = offsetFlag;
                                mTempTestArg[2] = 0;
                                if (VivoProxCaliService.this.mVivoSensorTest != null) {
                                    VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_CALI_OFFSET_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                                    Log.d(VivoProxCaliService.TAG, "proximity cali_off_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                                }
                            } else {
                                Log.d(VivoProxCaliService.TAG, "proximity no need to do offset");
                            }
                        }
                        longBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR, ProcessList.SERVICE_ADJ);
                        shortBaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR_SECOND, 8000);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        mTempTestArg[1] = longBaseValue;
                        mTempTestArg[2] = shortBaseValue;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " long " + mTempTestArg[1] + " short " + mTempTestArg[2]);
                        }
                    } else if (prop != null && (prop.toLowerCase().startsWith("pd1803") || prop.toLowerCase().startsWith("pd1732") || prop.toLowerCase().startsWith("pd1814") || prop.toLowerCase().startsWith("pd1816"))) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        if (prop.toLowerCase().startsWith("pd1814")) {
                            offsetFlag = SystemProperties.getInt(VivoProxCaliService.PS_CALI_OFFSET_FLAG, 0);
                            if (offsetFlag == 1) {
                                mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_OFFSET_DATA;
                                mTempTestArg[1] = offsetFlag;
                                mTempTestArg[2] = 0;
                                if (VivoProxCaliService.this.mVivoSensorTest != null) {
                                    VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_CALI_OFFSET_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                                    Log.d(VivoProxCaliService.TAG, "proximity cali_off_data: data[0]" + mTempTestArg[0] + " offsetFlag " + mTempTestArg[1]);
                                }
                            } else {
                                Log.d(VivoProxCaliService.TAG, "proximity no need to send cali type to driver");
                            }
                        }
                        BaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR, ProcessList.BACKUP_APP_ADJ);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        mTempTestArg[1] = BaseValue;
                        mTempTestArg[2] = 0;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " data[1] " + mTempTestArg[1] + " data[2] " + mTempTestArg[2]);
                        }
                    } else if (psCaliFlag == 1) {
                        mTempRes = new SensorTestResult();
                        mTempTestArg = new int[3];
                        BaseValue = SystemProperties.getInt(VivoProxCaliService.BASE_THRESHOLD_SENSOR, ProcessList.BACKUP_APP_ADJ);
                        mTempTestArg[0] = VivoProxCaliService.SENSOR_COMMAND_SET_PS_CALI_DATA;
                        mTempTestArg[1] = BaseValue;
                        mTempTestArg[2] = 0;
                        if (VivoProxCaliService.this.mVivoSensorTest != null) {
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(VivoProxCaliService.PS_SET_ENG_CALI_DATA, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "proximity cali_data: data[0]" + mTempTestArg[0] + " data[1] " + mTempTestArg[1] + " data[2] " + mTempTestArg[2]);
                        }
                    }
                    VivoProxCaliService.this.startCalibration(0);
                    if (prop != null && (prop.equals("PD1222T") || prop.equals("PD1222") || prop.equals("PD1222W") || prop.equals("PD1222TG3"))) {
                        Log.d("akmd", "startup copy pdc.txt for 1222\n");
                        SystemProperties.set("ctl.start", "bbk_pdc_copy");
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    if (VivoProxCaliService.this.pm.isUseProximitySensorLocked() || VivoProxCaliService.this.mIsCrystalAnim) {
                        Log.d(VivoProxCaliService.TAG, "use proximity lock or cystal animation in screen on,return");
                        return;
                    }
                    SensorTestResult result = new SensorTestResult();
                    VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(513, result, "", 0);
                    if (result.mSuccess == 1) {
                        Log.d(VivoProxCaliService.TAG, "startCalibration when screen on...");
                        VivoProxCaliService.this.startCalibration(1);
                    } else {
                        Log.d(VivoProxCaliService.TAG, "not configured ps tolerance,not do calibration when screen on...");
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF") || VivoProxCaliService.this.mIsCrystalAnim) {
                    prop = SystemProperties.get("ro.product.model.bbk", null);
                    if (prop != null && (prop.toLowerCase().startsWith("pd1710") || prop.toLowerCase().startsWith("pd1709"))) {
                        TelephonyManager tpManager = (TelephonyManager) VivoProxCaliService.this.mContext.getSystemService("phone");
                        if (tpManager != null && tpManager.isIdle()) {
                            mTempRes = new SensorTestResult();
                            mTempTestArg = new int[]{9, 0, 112};
                            VivoProxCaliService.this.mVivoSensorTest.vivoSensorTest(45, mTempRes, mTempTestArg, mTempTestArg.length);
                            Log.d(VivoProxCaliService.TAG, "ACTION_SCREEN_OFF param:" + mTempTestArg[2]);
                        }
                    }
                    if (VivoProxCaliService.this.pm.isUseProximitySensorLocked()) {
                        Log.d(VivoProxCaliService.TAG, "use proximity lock or cystal animation in screen off,return");
                    } else if (!VivoProxCaliService.mNeedStopCali) {
                        Log.d(VivoProxCaliService.TAG, "Stop calibration coz screen off broadcast");
                        VivoProxCaliService.this.mStopHandler.removeCallbacks(VivoProxCaliService.this.mStopCaliRunnable);
                        VivoProxCaliService.this.mStopHandler.post(VivoProxCaliService.this.mStopCaliRunnable);
                    }
                }
            }
        }
    }

    private static boolean isOpEntry() {
        return !mOpEntry.equals("CMCC") ? mOpEntry.contains("_RW") : true;
    }

    private static boolean IsUseUltrasound() {
        if (mUseUltrasound.length > 0) {
            for (String startsWith : mUseUltrasound) {
                if (mProductId.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doPsCaliUseVST(SensorTestResult mRes) {
        if (this.mVivoSensorTest != null) {
            this.mVivoSensorTest.vivoSensorTest(36, mRes, this.mTestArg, 0);
            if (this.mLightCaliValue > 5000.0f) {
                mRes.mSuccess = 0;
                Log.d(TAG, "mLightCaliValue =" + this.mLightCaliValue);
            }
            if (mRes.mSuccess == 1) {
                String strVal = Integer.toString(Math.round(mRes.mTestVal[0]));
                int defBase = Math.round(mRes.mDefBase[0]);
                try {
                    SystemProperties.set(TMP_BASE_THRESHOLD_SENSOR, strVal);
                } catch (Exception e) {
                    Log.d(TAG, "setprop persist.sys.tmp_base_thres_prox failed");
                }
                Log.d(TAG, "setprop persist.sys.tmp_base_thres_prox " + strVal);
                Log.d(TAG, "doPsCaliUseVST success: " + mRes.dumpString());
                this.mTestArg[0] = Math.min(255, SystemProperties.getInt(BASE_THRESHOLD_SENSOR, 0));
                this.mTestArg[1] = 0;
                if (SystemProperties.getInt(BASE_THRESHOLD_SENSOR, defBase) >= 65535) {
                    this.mTestArg[2] = Math.round(mRes.mTestVal[0]);
                } else {
                    this.mTestArg[2] = Math.max(Math.round(mRes.mTestVal[0]), SystemProperties.getInt(BASE_THRESHOLD_SENSOR, defBase));
                }
                this.mVivoSensorTest.vivoSensorTest(34, mRes, this.mTestArg, this.mTestArg.length);
                if (this.mVivoCollectData != null) {
                    HashMap<String, String> params = new HashMap();
                    params.put("base", String.valueOf(SystemProperties.getInt(BASE_THRESHOLD_SENSOR, defBase)));
                    params.put("temp", String.valueOf(this.mTestArg[2]));
                    this.mVivoCollectData.writeData(COLLECT_DATA_EVENTID, COLLECT_DATA_LABLE_SUCCESS, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
                    Log.d(TAG, "doPsCaliUseVST success, params:" + params.toString());
                    return;
                }
                return;
            }
            DeviceParaProvideService dpService = (DeviceParaProvideService) this.mContext.getSystemService("device_para_provide_service");
            this.mTestArg[0] = Math.min(255, SystemProperties.getInt(BASE_THRESHOLD_SENSOR, 0));
            if (mCaliStartBy == 2) {
                this.mTestArg[1] = 1;
            } else {
                this.mTestArg[1] = 0;
            }
            if (dpService != null) {
                this.mTestArg[2] = Math.max(SystemProperties.getInt(TMP_BASE_THRESHOLD_SENSOR, dpService.getPsBaseValue()), SystemProperties.getInt(BASE_THRESHOLD_SENSOR, dpService.getPsBaseValue()));
            } else {
                this.mTestArg[2] = Math.max(SystemProperties.getInt(TMP_BASE_THRESHOLD_SENSOR, ProcessList.SERVICE_ADJ), SystemProperties.getInt(BASE_THRESHOLD_SENSOR, ProcessList.SERVICE_ADJ));
            }
            this.mVivoSensorTest.vivoSensorTest(34, mRes, this.mTestArg, this.mTestArg.length);
            Log.d(TAG, "doPsCaliUseVST fail: " + mRes.dumpString() + " set caldata as" + this.mTestArg[2]);
        }
    }

    private void enabledrivercali(boolean enable) {
        if (mIsCalibrationing && enable) {
            Log.d(TAG, "enabledrivercali,return,as mIsCalibrationing=" + mIsCalibrationing + " enable=" + enable);
            return;
        }
        if (enable) {
            SensorTestResult mTempRes = new SensorTestResult();
            int[] mTempTestArg = new int[3];
            int mBaseValue = SystemProperties.getInt(BASE_THRESHOLD_SENSOR, -1);
            mIsCalibrationing = true;
            mNeedStopCali = false;
            mTempTestArg[0] = SENSOR_COMMAND_SET_PS_DRIVER_TEMP_CALI;
            mTempTestArg[1] = mBaseValue;
            if (this.mVivoSensorTest != null) {
                this.mVivoSensorTest.vivoSensorTest(PS_DRIVER_TEMP_CALI, mTempRes, mTempTestArg, mTempTestArg.length);
                Log.d(TAG, "enabledrivercali cali_data: data[0]" + mTempTestArg[0] + " cali_data " + mTempTestArg[1]);
            }
            mIsCalibrationing = false;
            mNeedStopCali = false;
        } else if (!mNeedStopCali) {
            mNeedStopCali = true;
        }
    }

    private void enableCalibrationUseVST(boolean enable) {
        if (mIsCalibrationing && enable) {
            Log.d(TAG, "WIRED,return,as mIsCalibrationing=" + mIsCalibrationing + " enable=" + enable);
            return;
        }
        if (enable) {
            mIsCalibrationing = true;
            mNeedStopCali = false;
            this.mSensorManager.registerListener(this.mProximitySensorListenerVST, this.mProximitySensor, 1);
            this.mLightCounts = 0;
            this.mLightCaliValue = 0.0f;
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, 1);
            doPsCaliUseVST(this.mTestResult);
            if (this.mTestResult.mSuccess == 1 || mCaliStartBy != 2) {
                if (this.mTestResult.mSuccess != 1 && (mCaliStartBy == 0 || mCaliStartBy == 1)) {
                    for (int i = 0; i < 5 && (mNeedStopCali ^ 1) != 0; i++) {
                        doPsCaliUseVST(this.mTestResult);
                        if (this.mTestResult.mSuccess == 1) {
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "finish prox temp cali");
                }
            } else {
                TelephonyManager tpManager = (TelephonyManager) this.mContext.getSystemService("phone");
                if (tpManager != null) {
                    while (this.mTestResult.mSuccess != 1 && tpManager.isRinging() && (mNeedStopCali ^ 1) != 0) {
                        doPsCaliUseVST(this.mTestResult);
                        if (this.mTestResult.mSuccess == 1) {
                            break;
                        }
                    }
                }
            }
            SensorTestResult mTempRes = new SensorTestResult();
            int[] mTempTestArg = new int[3];
            this.mSensorManager.unregisterListener(this.mProximitySensorListenerVST);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            mIsCalibrationing = false;
            mNeedStopCali = false;
        } else {
            if (!mNeedStopCali) {
                mNeedStopCali = true;
            }
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
        }
    }

    public static String sss(String myString) {
        Matcher m = Pattern.compile("(\r\n|\r|\n|\n\r)").matcher(myString);
        if (m.find()) {
            return m.replaceAll("");
        }
        return null;
    }

    public VivoProxCaliService() {
        this.mLightCounts = 0;
        this.mLightCaliValue = 0.0f;
        this.mLightThreshould = 500.0f;
        this.mProximityExceptionCollect = null;
        this.mProximitySensor = null;
        this.mLightSensor = null;
        this.pm = null;
        this.mWakeLock = null;
        this.mStartHandler = null;
        this.mStopHandler = null;
        this.mProximitySensorListenerVST = null;
        this.mLightSensorListener = null;
        this.mStartCaliRunnable = null;
        this.mStopCaliRunnable = null;
        this.mDeviceParaProvideService = null;
        this.mBootCompleteFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        this.mBootPsCaliReceiver = new BootcompleteReceiverForPsCali(this, null);
        this.mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        this.mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        this.mVivoSensorTest = VivoSensorTest.getInstance();
        this.mTestResult = new SensorTestResult();
        this.mTestArg = new int[3];
        this.mVivoCollectData = null;
        this.mSensorDump = null;
        this.mIsCrystalAnim = false;
        this.mDirectCallTime = 0;
    }

    public VivoProxCaliService(Context ctx) {
        this.mLightCounts = 0;
        this.mLightCaliValue = 0.0f;
        this.mLightThreshould = 500.0f;
        this.mProximityExceptionCollect = null;
        this.mProximitySensor = null;
        this.mLightSensor = null;
        this.pm = null;
        this.mWakeLock = null;
        this.mStartHandler = null;
        this.mStopHandler = null;
        this.mProximitySensorListenerVST = null;
        this.mLightSensorListener = null;
        this.mStartCaliRunnable = null;
        this.mStopCaliRunnable = null;
        this.mDeviceParaProvideService = null;
        this.mBootCompleteFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        this.mBootPsCaliReceiver = new BootcompleteReceiverForPsCali(this, null);
        this.mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        this.mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        this.mVivoSensorTest = VivoSensorTest.getInstance();
        this.mTestResult = new SensorTestResult();
        this.mTestArg = new int[3];
        this.mVivoCollectData = null;
        this.mSensorDump = null;
        this.mIsCrystalAnim = false;
        this.mDirectCallTime = 0;
        String ProxSensorName = "Null";
        String prop = SystemProperties.get("ro.product.model.bbk", null);
        this.mContext = ctx;
        this.mProxCaliThread = new HandlerThread("ProxCaliThread");
        this.mProxCaliThread.start();
        Log.d(TAG, "call VivoProxCaliService constructor.");
        this.mStartHandler = new Handler(this.mProxCaliThread.getLooper());
        this.mStopHandler = new Handler(this.mProxCaliThread.getLooper());
        this.pm = (PowerManager) this.mContext.getSystemService("power");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            ProxSensorName = this.mProximitySensor.getName();
        }
        if (ProxSensorName.toLowerCase().startsWith("stk3x1x") && prop.toLowerCase().startsWith("pd1731")) {
            mIsUseInstantCali = false;
            Log.d(TAG, "Force mIsUseInstantCali false");
        }
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mWakeLock = this.pm.newWakeLock(1, TAG);
        this.mProximityExceptionCollect = ProximityExceptionCollect.getInstance(this.mContext);
        mNotNeedDoProxCali = IsUseUltrasound();
        this.mVivoCollectData = new VivoCollectData(this.mContext);
        this.mSensorDump = new SensorDump(this.mContext);
        this.mProximitySensorListenerVST = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Log.d(VivoProxCaliService.TAG, "mProximitySensorListenerVST get ps data" + event.values[0]);
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mLightSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (VivoProxCaliService.this.mLightCounts < 12) {
                    VivoProxCaliService vivoProxCaliService = VivoProxCaliService.this;
                    vivoProxCaliService.mLightCounts = vivoProxCaliService.mLightCounts + 1;
                    float lightValue = (event.values[0] * 500.0f) / VivoProxCaliService.this.mLightThreshould;
                    vivoProxCaliService = VivoProxCaliService.this;
                    vivoProxCaliService.mLightCaliValue = vivoProxCaliService.mLightCaliValue + (lightValue / 12.0f);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mStartCaliRunnable = new Runnable() {
            public void run() {
                Log.d(VivoProxCaliService.TAG, "Start calibration");
                if (VivoProxCaliService.mIsDriverProxTempCali) {
                    VivoProxCaliService.this.enabledrivercali(true);
                } else {
                    VivoProxCaliService.this.enableCalibrationUseVST(true);
                }
            }
        };
        this.mStopCaliRunnable = new Runnable() {
            public void run() {
                Log.d(VivoProxCaliService.TAG, "Stop calibration");
                if (VivoProxCaliService.mIsDriverProxTempCali) {
                    VivoProxCaliService.this.enabledrivercali(false);
                } else {
                    VivoProxCaliService.this.enableCalibrationUseVST(false);
                }
            }
        };
        this.mContext.registerReceiver(this.mBootPsCaliReceiver, this.mBootCompleteFilter);
        this.mContext.registerReceiver(this.mBootPsCaliReceiver, this.mScreenOnFilter);
        this.mContext.registerReceiver(this.mBootPsCaliReceiver, this.mScreenOffFilter);
        this.mVivoSensorTest.vivoSensorTest(47, this.mTestResult, this.mTestArg, this.mTestArg.length);
        Log.d(TAG, "get temp para " + this.mTestResult.dumpString());
    }

    public void startCalibration(int type) {
        if (mNotNeedDoProxCali) {
            Log.d(TAG, "Ultrasound do not need to do calibration");
        } else if (mIsCalibrationing) {
            Log.d(TAG, "Being Calibrationing, return");
        } else if (this.mProximitySensor == null) {
            Log.d(TAG, "Proximity sensor is null,return");
        } else {
            long now = System.currentTimeMillis();
            if (type != 2 || now - this.mDirectCallTime >= 2000) {
                Log.d(TAG, "Start Calibration...., type: " + type);
                mCaliStartBy = type;
                this.mStartHandler.removeCallbacks(this.mStartCaliRunnable);
                this.mStartHandler.post(this.mStartCaliRunnable);
                this.mStopHandler.removeCallbacks(this.mStopCaliRunnable);
                this.mStopHandler.postDelayed(this.mStopCaliRunnable, 10000);
                return;
            }
            Log.d(TAG, "direct calling, not cali. now:" + now + " call:" + this.mDirectCallTime);
        }
    }

    public void changeProximityParam(boolean change, int state) {
        SensorTestResult mTempRes = new SensorTestResult();
        int[] mTempTestArg = new int[3];
        String prop = SystemProperties.get("ro.product.model.bbk", null);
        if (prop == null) {
            return;
        }
        if (prop.toLowerCase().startsWith("pd1710") || prop.toLowerCase().startsWith("pd1709") || prop.toLowerCase().startsWith("pd1718") || prop.toLowerCase().startsWith("pd1708") || prop.toLowerCase().startsWith("pd1616") || prop.toLowerCase().startsWith("pd1619") || prop.toLowerCase().startsWith("pd1635") || prop.toLowerCase().startsWith("pd1610") || prop.toLowerCase().startsWith("pd1624")) {
            mTempTestArg[0] = 9;
            if (change) {
                mTempTestArg[1] = 1;
            } else {
                mTempTestArg[1] = 0;
            }
            mTempTestArg[2] = state;
            if (this.mVivoSensorTest != null) {
                Log.d(TAG, "proximity status = " + change + " ps_para: data[0]" + mTempTestArg[0] + " data[1]=" + mTempTestArg[1] + "data[2]=" + mTempTestArg[2]);
                this.mVivoSensorTest.vivoSensorTest(45, mTempRes, mTempTestArg, mTempTestArg.length);
            }
            this.mProximityExceptionCollect.notifyChangeProximityParam(0, change);
            Log.d(TAG, "0ops change param in vivoproxcaliservice");
        }
    }

    public void onDirectCall(long timestamp) {
        this.mDirectCallTime = timestamp;
    }

    public void setCrystalAnimStatus(boolean isCrystalAnim) {
        Log.d(TAG, "set crystal animation : " + isCrystalAnim);
        this.mIsCrystalAnim = isCrystalAnim;
    }

    private int getPhoneCallState() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            return 0;
        }
        Log.d(TAG, "getCallState: " + tm.getCallState());
        return tm.getCallState();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (!SENSOR_DUMP_ENABLED.contentEquals(SystemProperties.get(PROP_SENSOR_DUMP_KEY, SENSOR_DUMP_DISABLED))) {
            pw.print("Permission Denial, android.permission.DUMP\r\n");
        } else if (this.mSensorDump != null) {
            this.mSensorDump.handleCommand(fd, pw, args);
        } else {
            pw.println("Error__NULL");
        }
    }
}
