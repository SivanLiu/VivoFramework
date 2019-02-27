package com.vivo.services.facedetect.camera;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import android.view.OrientationEventListener;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.sensoroperate.VivoSensorOperationResult;
import com.sensoroperate.VivoSensorOperationUtils;
import com.vivo.services.daemon.VivoDmServiceProxy;
import com.vivo.services.epm.config.Switch;
import com.vivo.services.facedetect.BoostConfig;
import com.vivo.services.facedetect.FaceDebugConfig;
import com.vivo.services.rms.ProcessList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

public class FaceCamera {
    private static final String CAMERA_CAPTURE_FRAME = "persist.debug.set.fixedfps";
    private static final int DARK_ENVIRONMENT_LUX = 2;
    private static int DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = 0;
    private static int DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 0;
    private static int DURATION_TIME_PER_CYCLE = 0;
    private static final int LIGHT_THRESHOLD = SystemProperties.getInt("persist.sys.light_threshold", ProcessList.SERVICE_ADJ);
    private static int MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 0;
    private static int MAX_FLASH_CYCLES_OF_IR_LED = 0;
    private static final int MAX_PREVIEW_FPS_TIMES_1000 = 400000;
    private static final String MODEL = SystemProperties.get("ro.vivo.product.model", "");
    private static final int PREFERRED_PREVIEW_FPS_TIMES_1000 = 30000;
    public static final int PREVIEW_HEIGHT = 480;
    public static final int PREVIEW_WIDTH = 640;
    private static final String PROP_MTK_FACEIR = "debug.appcontrol.name";
    private static final String PROP_RGB_IR = "persist.sys.facedetect.rgb_ir";
    private static final int SCALE_TYPE_16_9 = 2;
    private static final int SCALE_TYPE_4_3 = 1;
    private static final String SHOW_TIPS = "persist.facedetect.showtips";
    private static final String SOLUTION = SystemProperties.get("ro.vivo.product.solution", "");
    private static final String TAG = "FaceCamera";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static boolean USE_BINNING_IN_DARK_ENVIRONMENT = false;
    private static int aBoostTimeOut = 1000;
    private static final String mDAEMONVERSION = "3.1.0";
    private static Object mPowerHalService = null;
    private static Object mPowerHallock = new Object();
    private static int mPowerHandle = -1;
    private int M_DEFAULT_CAMERA_CAPTURE_FRAME = 24;
    private int OPEN_CAMERA_FAILED = 2;
    private int OPEN_CAMERA_OCCUPIED = 1;
    private int OPEN_CAMERA_SUCCESS = 0;
    private boolean m2PdEnable = false;
    private AlertDialog mAlertDialog;
    private int mAvailableCyclesOfIrLed = MAX_FLASH_CYCLES_OF_IR_LED;
    public byte[] mBuffer;
    private Camera mCamera;
    private int mCameraCaptureFrameNumber = 0;
    private int mCameraFacing;
    private boolean mCameraOccupied = false;
    private Object mCameraPerf = null;
    private Class<?> mClassObj = null;
    private Context mContext;
    public int mDegrees;
    private int mDirection = 0;
    private final Runnable mGetLuxRunnable = new Runnable() {
        public void run() {
            FaceCamera.this.getIsDarkEnvironment();
        }
    };
    private long mGetLuxTime = 0;
    private boolean mHasGetLux = false;
    private boolean mHasJudgeIrLedAvailable = false;
    private long mIrLedOffTime = 0;
    private long mIrLedOnTime = 0;
    private boolean mIrLedOpened = false;
    private final Object mIrLock = new Object();
    private boolean mIsDarkEnvironment = false;
    private boolean mIsDarkEnvironmentAndNoProximity = false;
    private boolean mIsIrLedAvailable = true;
    private Handler mLightHandler;
    private final Object mLock = new Object();
    private Handler mMainThreadHandler;
    private long mMaxCostTime = Long.MIN_VALUE;
    private Method mMethod = null;
    private long mMinCostTime = Long.MAX_VALUE;
    private final Runnable mNotifyOffRunnable = new Runnable() {
        public void run() {
            FaceCamera.this.notifyProximitySensor(false);
        }
    };
    private final Runnable mNotifyOnRunnable = new Runnable() {
        public void run() {
            FaceCamera.this.notifyProximitySensor(true);
        }
    };
    private volatile boolean mOpenFailed;
    private OrientationEventListener mOrientationListener;
    private Object mPPDataCallback = null;
    private Class<?> mPdCallbackClassObj = null;
    private Method mPdCallbackMethod = null;
    private PreviewCallback mPreviewCallback;
    private float mPreviewScale;
    private Size mPreviewSize;
    private boolean mProximity = false;
    private boolean mProximityEnabled = false;
    private Sensor mProximitySensor;
    private float mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
    private float mRectifiedLux = 0.0f;
    private int mResolution;
    private int mScenario = -1;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float distance = event.values[0];
            boolean proximity = distance >= 0.0f && distance < FaceCamera.this.mProximityThreshold;
            FaceCamera.this.mProximity = proximity;
            Log.i(FaceCamera.TAG, "onSensorChanged: proximity = " + proximity);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Handler mSensorHandler;
    private SensorManager mSensorManager;
    private VivoSensorOperationUtils mVivoSensorOperationUtils;
    private float previewScaleX;
    private float previewScaleY;
    private Method scnConfigMethod = null;
    private Method scnEnableMethod = null;

    static {
        MAX_FLASH_CYCLES_OF_IR_LED = 40;
        MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 10;
        DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 90;
        DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = 330;
        DURATION_TIME_PER_CYCLE = DURATION_TIME_OF_IR_LED_ON_PER_CYCLE + DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE;
        USE_BINNING_IN_DARK_ENVIRONMENT = false;
        if ("PD1728".equals(MODEL) || "PD1730C".equals(MODEL) || "PD1730CF_EX".equals(MODEL) || "PD1813C".equals(MODEL)) {
            USE_BINNING_IN_DARK_ENVIRONMENT = true;
        }
        if ("PD1813".equals(MODEL)) {
            MAX_FLASH_CYCLES_OF_IR_LED = 40;
            MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 8;
            DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 100;
            DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = 390;
            DURATION_TIME_PER_CYCLE = DURATION_TIME_OF_IR_LED_ON_PER_CYCLE + DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE;
        }
    }

    public FaceCamera(Context context, Handler handler) {
        init(context, handler);
    }

    private void init(Context context, Handler handler) {
        this.mContext = context;
        this.mSensorHandler = handler;
        HandlerThread handlerThread = new HandlerThread("FaceLight");
        handlerThread.start();
        this.mLightHandler = new Handler(handlerThread.getLooper());
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mSensorManager != null) {
            this.mProximitySensor = this.mSensorManager.getDefaultSensor(8, true);
            if (this.mProximitySensor == null) {
                Log.e(TAG, "mProximitySensor is null!");
                this.mProximityThreshold = Math.min(1.0f, TYPICAL_PROXIMITY_THRESHOLD);
            } else {
                this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
            }
        }
        this.mCameraFacing = 1;
        this.mResolution = 480;
        this.mPreviewScale = getPreviewScale(0);
        this.M_DEFAULT_CAMERA_CAPTURE_FRAME = BoostConfig.getCurrentCameraFps();
        getCameraCaptureFrame();
        this.mVivoSensorOperationUtils = VivoSensorOperationUtils.getInstance();
        initAlertDialog();
    }

    private void initAlertDialog() {
        this.mAlertDialog = new Builder(this.mContext).create();
        this.mAlertDialog.setTitle("tips");
        this.mAlertDialog.setButton(-2, "sure", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mAlertDialog.setCanceledOnTouchOutside(false);
        this.mAlertDialog.getWindow().addFlags(IQcRilHook.QCRILHOOK_BASE);
        this.mAlertDialog.getWindow().setType(2009);
    }

    public boolean openCamera(int mCameraFacing) {
        if (BoostConfig.isMtkProductModel()) {
            if (this.mCameraPerf == null) {
                newInstanceClassForMtk();
            }
            if (!(this.mCameraPerf == null || this.mMethod == null)) {
                performFunctionForMtk();
            }
        } else if (BoostConfig.isMtkNewBoostFramework()) {
            if (mPowerHalService == null) {
                newInstanceClassForMtkNewPlatform();
            }
            if (FaceDebugConfig.DEBUG_TIME) {
                Log.i(TAG, "openCamera conf = " + this.scnConfigMethod + ", scm = " + this.scnEnableMethod + ", pref = " + mPowerHalService + ", ph = " + mPowerHandle);
            }
            if (!(mPowerHalService == null || mPowerHandle == -1 || this.scnConfigMethod == null || this.scnEnableMethod == null)) {
                performFunctionForMtkNewPlatform();
            }
        } else {
            if (this.mCameraPerf == null) {
                newInstanceClass("android.util.BoostFramework");
            }
            if (!(this.mCameraPerf == null || this.mMethod == null)) {
                performFunction();
            }
        }
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "openCamera enter");
        }
        CameraInfo info = new CameraInfo();
        this.mOpenFailed = true;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraFacing) {
                boolean openCameraInner;
                synchronized (this.mLock) {
                    openCameraInner = openCameraInner(i);
                }
                return openCameraInner;
            }
        }
        Log.e(TAG, "openCamera leave failed");
        return false;
    }

    private void newInstanceClass(String cls) {
        try {
            Class<?> objclass = Class.forName(cls);
            this.mCameraPerf = objclass.getConstructor(new Class[0]).newInstance(new Object[0]);
            this.mMethod = objclass.getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performFunction() {
        if (this.mMethod != null && BoostConfig.getCurrentModelBoostConfig() != null) {
            try {
                this.mMethod.invoke(this.mCameraPerf, new Object[]{Integer.valueOf(aBoostTimeOut), BoostConfig.getCurrentModelBoostConfig()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getScenarioForMtk() {
        try {
            Class<?> objclass = Class.forName("com.mediatek.perfservice.IPerfServiceWrapper");
            if (objclass == null) {
                return -1;
            }
            Field field = objclass.getDeclaredField("SCN_APP_LAUNCH");
            if (field != null) {
                return field.getInt(null);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newInstanceClassForMtk() {
        try {
            Class<?> objclass = Class.forName("com.mediatek.perfservice.PerfServiceWrapper");
            this.mCameraPerf = objclass.getConstructor(new Class[0]).newInstance(new Object[0]);
            this.mMethod = objclass.getMethod("boostEnableTimeoutMs", new Class[]{Integer.TYPE, Integer.TYPE});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performFunctionForMtk() {
        Log.d(TAG, "performFunctionForMtk enter");
        try {
            if (this.mScenario == -1) {
                this.mScenario = getScenarioForMtk();
            }
            Log.d(TAG, "mScenario = " + this.mScenario + ",aBoostTimeOut = " + aBoostTimeOut);
            if (this.mCameraPerf != null && this.mMethod != null && this.mScenario != -1) {
                this.mMethod.invoke(this.mCameraPerf, new Object[]{Integer.valueOf(this.mScenario), Integer.valueOf(aBoostTimeOut)});
                Log.d(TAG, "performFunctionForMtk boostEnableTimeoutMs success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean openCameraInner(int index) {
        this.mCameraOccupied = false;
        if (index < 0) {
            return false;
        }
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "openCameraInner enter");
        }
        try {
            if (FaceCameraManager.IS_RGB_IR_SCHEME || USE_BINNING_IN_DARK_ENVIRONMENT) {
                this.mSensorHandler.removeCallbacks(this.mGetLuxRunnable);
                this.mSensorHandler.post(this.mGetLuxRunnable);
            }
            if (FaceDebugConfig.DEBUG_TIME) {
                Trace.traceBegin(8, "openCameraInner");
            }
            long start = System.currentTimeMillis();
            this.mCamera = Camera.open(index);
            long end = System.currentTimeMillis();
            Log.i(TAG, "Camera.open cost = " + (end - start) + " ms");
            if (FaceCameraManager.IS_RGB_IR_SCHEME || USE_BINNING_IN_DARK_ENVIRONMENT) {
                synchronized (this.mIrLock) {
                    if (end - this.mGetLuxTime < 0) {
                        Log.i(TAG, "Camera is waiting for lux: " + (this.mGetLuxTime - end) + " ms");
                    }
                }
            }
            if (FaceDebugConfig.DEBUG_TIME) {
                Trace.traceEnd(8);
            }
            initParameters();
            if (this.mCamera != null) {
                this.mCamera.setPreviewDisplay(null);
                this.mOpenFailed = false;
                if (FaceDebugConfig.DEBUG_TIME) {
                    Log.d(TAG, "openCameraInner leave success");
                }
                return true;
            }
        } catch (RuntimeException e1) {
            Log.d(TAG, "openCamera occupied start");
            String[] error = printStackTraceToString(e1).split(":");
            if (error != null && error.length > 1 && error[1].trim().contains("Fail to connect to camera service")) {
                this.mCameraOccupied = true;
                Log.d(TAG, "openCamera occupied end");
            }
        } catch (Exception e) {
            Log.e(TAG, "openCameraInner setPreviewDisplay fail");
            e.printStackTrace();
        }
        Log.e(TAG, "openCameraInner leave failed");
        return false;
    }

    private boolean openCameraLocked(int mCameraFacing) {
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "openCameraLocked enter");
        }
        CameraInfo info = new CameraInfo();
        this.mOpenFailed = true;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraFacing) {
                return openCameraInner(i);
            }
        }
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "openCameraLocked leave failed");
        }
        return false;
    }

    private int[] getPhotoPreviewFpsRange(List<int[]> frameRates) {
        if (frameRates == null || frameRates.size() == 0) {
            Log.e(TAG, "No suppoted frame rates returned!");
            return null;
        }
        int minFps;
        int[] rate;
        int lowestMinRate = MAX_PREVIEW_FPS_TIMES_1000;
        for (int[] rate2 : frameRates) {
            minFps = rate2[0];
            if (rate2[1] >= PREFERRED_PREVIEW_FPS_TIMES_1000 && minFps <= PREFERRED_PREVIEW_FPS_TIMES_1000 && minFps < lowestMinRate) {
                lowestMinRate = minFps;
            }
        }
        int resultIndex = -1;
        int highestMaxRate = 0;
        for (int i = 0; i < frameRates.size(); i++) {
            rate2 = (int[]) frameRates.get(i);
            minFps = rate2[0];
            int maxFps = rate2[1];
            if (minFps == lowestMinRate && highestMaxRate < maxFps) {
                highestMaxRate = maxFps;
                resultIndex = i;
            }
        }
        if (resultIndex >= 0) {
            return (int[]) frameRates.get(resultIndex);
        }
        Log.e(TAG, "Can't find an appropiate frame rate range!");
        return null;
    }

    private void initParameters() {
        if (this.mCamera != null) {
            try {
                Parameters parameters = this.mCamera.getParameters();
                parameters.setPreviewFormat(17);
                int[] fpsRange = getPhotoPreviewFpsRange(parameters.getSupportedPreviewFpsRange());
                if (fpsRange != null && fpsRange.length > 0) {
                    int mMinFps = fpsRange[0];
                    int mMaxFps = fpsRange[1];
                    if (FaceDebugConfig.DEBUG_TIME) {
                        Log.d(TAG, "face got params " + mMinFps + ", " + mMaxFps);
                    }
                    parameters.setPreviewFpsRange(mMinFps, this.M_DEFAULT_CAMERA_CAPTURE_FRAME * 1000);
                }
                if (FaceCameraManager.IS_RGB_IR_SCHEME || USE_BINNING_IN_DARK_ENVIRONMENT) {
                    this.mIrLedOpened = false;
                    initParametersForIr(parameters);
                } else {
                    List<String> mlist = parameters.getSupportedSceneModes();
                    if (mlist != null && mlist.contains("face-detection")) {
                        parameters.set("scene-mode", "face-detection");
                    }
                }
                parameters.setPreviewSize(640, 480);
                parameters.set("no-display-mode", 1);
                Log.e(TAG, "no-display-mode :" + parameters.get("no-display-mode"));
                if (this.m2PdEnable) {
                    parameters.set("facepdcallbackflag", 1);
                }
                this.mCamera.setParameters(parameters);
                this.mCamera.setPreviewCallback(this.mPreviewCallback);
                setPPDataCallback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initParametersForIr(Parameters parameters) {
        synchronized (this.mIrLock) {
            if (this.mIsDarkEnvironmentAndNoProximity) {
                if (getIsIrLedAvailable()) {
                    this.mIrLedOpened = true;
                    this.mSensorHandler.removeCallbacks(this.mNotifyOffRunnable);
                    this.mSensorHandler.removeCallbacks(this.mNotifyOnRunnable);
                    this.mSensorHandler.post(this.mNotifyOnRunnable);
                    if ("MTK".equals(SOLUTION)) {
                        System.putInt(this.mContext.getContentResolver(), "back_flashlight_state", 0);
                    }
                    parameters.set("ir-flash-enable", Switch.SWITCH_ATTR_VALUE_ON);
                    this.mIrLedOnTime = System.currentTimeMillis();
                    showTips(true);
                } else {
                    showTips(false);
                }
                parameters.set("scene-mode", "face-detection-binning");
            } else {
                parameters.set("scene-mode", "face-detection");
            }
        }
    }

    public void updateCameraParametersControlPreviewData(int flag) {
        if (this.m2PdEnable) {
            synchronized (this.mLock) {
                if (this.mCamera != null) {
                    try {
                        Parameters parameters = this.mCamera.getParameters();
                        if (parameters != null) {
                            parameters.set("facepdcallbackflag", flag);
                            this.mCamera.setParameters(parameters);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        return;
    }

    public boolean updateCamera() {
        if (this.mCamera == null) {
            return false;
        }
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "updateCamera enter");
        }
        synchronized (this.mLock) {
            try {
                this.mCamera.startPreview();
                if (FaceDebugConfig.DEBUG_TIME) {
                    Log.d(TAG, "updateCamera leave successful");
                }
            } catch (Exception e) {
                Log.e(TAG, "updateCamera leave failed");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean updateCameraLocked() {
        if (this.mCamera == null) {
            return false;
        }
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.d(TAG, "updateCameraLocked enter");
        }
        try {
            this.mCamera.startPreview();
            if (FaceDebugConfig.DEBUG_TIME) {
                Log.d(TAG, "updateCameraLocked leave successful");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "updateCameraLocked leave failed");
            e.printStackTrace();
            return false;
        }
    }

    private Size getFitPreviewSize(Parameters parameters) {
        List<Size> previewSizes = parameters.getSupportedPreviewSizes();
        int minDelta = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < previewSizes.size(); i++) {
            Size previewSize = (Size) previewSizes.get(i);
            if (((float) previewSize.width) * this.mPreviewScale == ((float) previewSize.height)) {
                int delta = Math.abs(this.mResolution - previewSize.height);
                if (delta == 0) {
                    return previewSize;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return (Size) previewSizes.get(index);
    }

    public boolean cameraOpenFailed() {
        boolean z;
        synchronized (this.mLock) {
            Log.e(TAG, "mOpenFailed : " + this.mOpenFailed);
            z = this.mOpenFailed;
        }
        return z;
    }

    public void releaseCamera() {
        Log.d(TAG, "releaseCamera enter");
        if (this.mCamera != null) {
            long cameraTimeStart = System.currentTimeMillis();
            synchronized (this.mLock) {
                releaseCameraLocked();
                Log.i(TAG, "releaseCamera: ");
            }
            long cameraTimeEnd = System.currentTimeMillis() - cameraTimeStart;
            if (FaceDebugConfig.DEBUG_TIME) {
                Log.v(TAG, "releaseCamera() CostTime " + cameraTimeEnd);
            }
        }
    }

    private void releaseCameraLocked() {
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.v(TAG, "releaseCameraLocked()");
        }
        if (this.mCamera != null) {
            try {
                this.mCamera.setPreviewCallback(null);
                try {
                    Parameters parameters = this.mCamera.getParameters();
                    if (this.m2PdEnable) {
                        parameters.set("facepdcallbackflag", 0);
                    }
                    if (FaceCameraManager.IS_RGB_IR_SCHEME || USE_BINNING_IN_DARK_ENVIRONMENT) {
                        releaseCameraForIrLocked(parameters);
                    }
                    this.mCamera.setParameters(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mCamera.stopPreview();
                this.mCamera.release();
                this.mCamera = null;
                this.mPdCallbackClassObj = null;
            } catch (Exception e2) {
                Log.w(TAG, "releaseCamera failed");
                runKillCameraServiceShellCommand();
                e2.printStackTrace();
            } catch (Throwable th) {
                this.mCamera = null;
            }
            this.mCamera = null;
        }
    }

    private void releaseCameraForIrLocked(Parameters parameters) {
        synchronized (this.mIrLock) {
            if (this.mIsDarkEnvironmentAndNoProximity) {
                if (this.mIsIrLedAvailable) {
                    parameters.set("ir-flash-enable", Switch.SWITCH_ATTR_VALUE_OFF);
                    this.mCamera.setParameters(parameters);
                    this.mIrLedOffTime = System.currentTimeMillis();
                    this.mSensorHandler.removeCallbacks(this.mNotifyOnRunnable);
                    this.mSensorHandler.removeCallbacks(this.mNotifyOffRunnable);
                    this.mSensorHandler.postDelayed(this.mNotifyOffRunnable, 400);
                    int duration = (int) (this.mIrLedOffTime - this.mIrLedOnTime);
                    if (duration > 0) {
                        int flashCycle = (duration / DURATION_TIME_PER_CYCLE) + 1;
                        if (flashCycle > MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE) {
                            flashCycle = MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE;
                        }
                        Log.i(TAG, "releaseCameraForIrLocked: duration = " + duration + " ms, flashCycle = " + flashCycle);
                        this.mAvailableCyclesOfIrLed -= flashCycle;
                        if (this.mAvailableCyclesOfIrLed < 0) {
                            this.mAvailableCyclesOfIrLed = 0;
                        }
                    } else {
                        Log.e(TAG, "releaseCameraForIrLocked: duration is " + duration + " ms < 0, error!!!");
                    }
                }
                hideTips();
            }
            this.mIsDarkEnvironment = false;
            this.mIsDarkEnvironmentAndNoProximity = false;
            this.mHasGetLux = false;
            this.mHasJudgeIrLedAvailable = false;
            Log.i(TAG, "releaseCameraForIrLocked: mIsDarkEnvironment = " + this.mIsDarkEnvironment);
        }
    }

    private void runKillCameraServiceShellCommand() {
        Log.w(TAG, "runKillCameraServiceShellCommand ");
        VivoDmServiceProxy proxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        int PID = getCameraServicePID(runPSCameraServiceShellCommand());
        if (PID != -1) {
            try {
                String cmd = "";
                if (mDAEMONVERSION.equals(BoostConfig.getCurrentModelDaemonVersion())) {
                    cmd = "BSSC16QLxC98VbJCSNWO0EndjxLGUICixbBCBvspxF+y1SMIlB2CcOB3W2zGIDnIifdbdjuPA+q7Iyud4u+c42E+OH16Z/E5MOSKg4oEEwrgytdwhZF7EAuXZH2zVcXj2liswEdUwxV4mSyVW6igLX+zBUJ26nT5wmg9FTgqthiHXuQO/Ggdhf0uPIaUEOOsWg4kpAC/3GX4QMa5N7godR/jvb1/HvlVxjbvrpI7Jj8ikGxAm0puVayzcudyEx5aaUeo1Oc+YPkYzMVPPDb46Kz85msEYBwOH+aEm652bkdPugVlIhto3iMlDnUPoDcBiGbmxtX9TfCLEcEScSkjNg==?" + PID;
                } else {
                    cmd = "kill " + PID;
                }
                Log.w(TAG, "runKillCameraServiceShellCommand cmd : " + cmd);
                proxy.runShellWithResult(cmd);
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, "runKillCameraServiceShellCommand  exception ");
            } finally {
                Log.w(TAG, "runKillCameraServiceShellCommand fianlly PID = " + PID);
            }
        }
    }

    private String runPSCameraServiceShellCommand() {
        Log.w(TAG, "runPSCameraServiceShellCommand ");
        VivoDmServiceProxy proxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
        String ret = "";
        try {
            String cmd = "";
            if (mDAEMONVERSION.equals(BoostConfig.getCurrentModelDaemonVersion())) {
                cmd = "C3hRlYPOEvgPzZrz1SmVTJHSWB944sL0Nu/oHYw2mGVavmWkUi9Lh7CyVetSzfPUd04GFq7uN650JgRo6PXrFGNcNlBn42m3eZEjbg96KAH1lQjx2WTi6qwXhmQceJOS/9OrW01kJaCvCrHcANSXFbkUqVSQvj5GYThf0uE3aOuGP7ZqQJnohKl+7mGYX9R6l6NU88Qylk4ujqkUdIo5Xz37csLZfUI/B5Fjf0GSLOzpd6kmGQrve4WvfqpeLpKsVyTLTWhcBLJuwu3I5ULPVa8iaqm4ZlnHTE/d+39C95Voa6QfDhuW4YmgbLGlM8RJbc2WvwMYGeLNndHt+nugsg==?'S cameraserver'";
            } else {
                cmd = "ps -A | grep 'S cameraserver'";
            }
            Log.w(TAG, "runPSCameraServiceShellCommand cmd : " + cmd);
            ret = proxy.runShellWithResult(cmd);
            Log.v(TAG, "runPSCameraServiceShellCommand  finally ret = " + ret);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "runPSCameraServiceShellCommand  exception");
            Log.v(TAG, "runPSCameraServiceShellCommand  finally ret = " + ret);
            return ret;
        } catch (Throwable th) {
            Log.v(TAG, "runPSCameraServiceShellCommand  finally ret = " + ret);
            return ret;
        }
    }

    private int getCameraServicePID(String msg) {
        Log.w(TAG, "getCameraPID cameraInfo : " + msg);
        if (msg == null) {
            return -1;
        }
        String[] strings = msg.split(" ");
        if (strings.length == 0) {
            return -1;
        }
        boolean flag = false;
        int res = 0;
        for (int j = 0; j < strings.length; j++) {
            char[] array = strings[j].toCharArray();
            if (array != null && array.length != 0 && array[0] <= '9' && array[0] >= '0') {
                res = j;
                flag = true;
                break;
            }
        }
        if (flag) {
            return Integer.parseInt(strings[res]);
        }
        return -1;
    }

    private float getPreviewScale(int type) {
        if (type != 1 && type == 2) {
            return 0.5625f;
        }
        return 0.75f;
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        this.mPreviewCallback = previewCallback;
    }

    public void setPPDataCallback(Object pdDataCallback) {
        this.mPPDataCallback = pdDataCallback;
    }

    public int restartCamera() {
        Log.i(TAG, "restartCamera: ");
        if (FaceDebugConfig.DEBUG_TIME) {
            Log.w(TAG, "restartCamera");
        }
        synchronized (this.mLock) {
            try {
                boolean result = openCameraLocked(1);
                int i;
                if (result || !this.mCameraOccupied) {
                    if (result) {
                        result = updateCameraLocked();
                    }
                    this.mOpenFailed = result ^ 1;
                    if (result) {
                        i = this.OPEN_CAMERA_SUCCESS;
                        return i;
                    }
                    i = this.OPEN_CAMERA_FAILED;
                    return i;
                }
                i = this.OPEN_CAMERA_OCCUPIED;
                return i;
            } catch (Exception e) {
                Log.w(TAG, "restartCamera failed");
                this.mOpenFailed = true;
                e.printStackTrace();
                return this.OPEN_CAMERA_FAILED;
            }
        }
    }

    public void startPreview() {
        if (this.mCamera != null) {
            if (FaceDebugConfig.DEBUG_TIME) {
                Log.d(TAG, "startPreview()");
            }
            this.mCamera.setPreviewCallback(this.mPreviewCallback);
            setPPDataCallback();
            this.mCamera.startPreview();
        }
    }

    public void stopPreview() {
        try {
            if (this.mCamera != null) {
                if (FaceDebugConfig.DEBUG_TIME) {
                    Log.d(TAG, "stopPreview()");
                }
                this.mCamera.stopPreview();
            }
        } catch (Exception e) {
            Log.w(TAG, "stopPreview failed");
            e.printStackTrace();
        }
    }

    void setPPDataCallback() {
        if (this.mCamera != null && this.mPPDataCallback != null) {
            if (this.mPdCallbackClassObj == null) {
                try {
                    this.mPdCallbackClassObj = this.mCamera.getClass();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (this.mPdCallbackClassObj != null && this.mPdCallbackMethod == null) {
                for (Method item : this.mPdCallbackClassObj.getMethods()) {
                    if (item.getName().equals("setPPDataCallback")) {
                        this.mPdCallbackMethod = item;
                        break;
                    }
                }
            }
            try {
                if (this.mPdCallbackMethod != null) {
                    this.mPdCallbackMethod.invoke(this.mCamera, new Object[]{this.mPPDataCallback});
                } else {
                    Log.w(TAG, "pd callback class not found");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void set2PdEnable(boolean enable) {
        this.m2PdEnable = enable;
    }

    public static String printStackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        try {
            t.printStackTrace(new PrintWriter(sw, true));
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.getBuffer().toString();
    }

    private void getCameraCaptureFrame() {
        try {
            this.mCameraCaptureFrameNumber = SystemProperties.getInt(CAMERA_CAPTURE_FRAME, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCameraCaptureFrame(int number) {
        try {
            SystemProperties.set(CAMERA_CAPTURE_FRAME, new Integer(number).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float getRectifiedLux() {
        long start = System.currentTimeMillis();
        float light = getAlsRawDataTimeout();
        long currentCostTime = System.currentTimeMillis() - start;
        if (currentCostTime > this.mMaxCostTime) {
            this.mMaxCostTime = currentCostTime;
        }
        if (currentCostTime < this.mMinCostTime) {
            this.mMinCostTime = currentCostTime;
        }
        Log.i(TAG, "getRectifiedLux: light = " + light + " lux, currentCostTime = " + currentCostTime + " ms, mMaxCostTime = " + this.mMaxCostTime + " ms, mMinCostTime = " + this.mMinCostTime + " ms");
        if (light == -1.0f) {
            return 3.0f;
        }
        float rectifiedLux;
        if (light == 1.0f) {
            rectifiedLux = light;
        } else {
            rectifiedLux = (500.0f * light) / ((float) LIGHT_THRESHOLD);
        }
        Log.i(TAG, "getRectifiedLux: " + rectifiedLux + " lux");
        return rectifiedLux;
    }

    private float getAlsRawDataTimeout() {
        final VivoSensorOperationResult result = new VivoSensorOperationResult();
        result.mTestVal[0] = -1.0f;
        final int[] operationArgs = new int[3];
        operationArgs[0] = 201;
        operationArgs[1] = 111;
        Log.i(TAG, "getAlsRawDataTimeout: start");
        this.mLightHandler.runWithScissors(new Runnable() {
            public void run() {
                if (FaceCamera.this.mVivoSensorOperationUtils != null) {
                    try {
                        FaceCamera.this.mVivoSensorOperationUtils.executeCommand(operationArgs[0], result, operationArgs, operationArgs.length);
                        Log.i(FaceCamera.TAG, "Get als data after executeCommand: " + result.mTestVal[0]);
                    } catch (Exception e) {
                        Slog.e(FaceCamera.TAG, "Fail to get als data");
                    }
                }
            }
        }, 100);
        Log.i(TAG, "getAlsRawDataTimeout: end");
        Log.i(TAG, "Get als data : " + result.mTestVal[0]);
        return result.mTestVal[0];
    }

    public boolean getIsDarkEnvironment() {
        boolean z = true;
        boolean z2 = false;
        if (!FaceCameraManager.IS_RGB_IR_SCHEME && (USE_BINNING_IN_DARK_ENVIRONMENT ^ 1) != 0) {
            return false;
        }
        synchronized (this.mIrLock) {
            if (!this.mHasGetLux) {
                this.mHasGetLux = true;
                this.mRectifiedLux = getRectifiedLux();
                if (this.mRectifiedLux > 2.0f) {
                    z = false;
                }
                this.mIsDarkEnvironment = z;
                if (this.mIsDarkEnvironment) {
                    z2 = this.mProximity ^ 1;
                }
                this.mIsDarkEnvironmentAndNoProximity = z2;
                this.mGetLuxTime = System.currentTimeMillis();
                if (this.mIsDarkEnvironmentAndNoProximity && "MTK".equals(SOLUTION)) {
                    SystemProperties.set(PROP_MTK_FACEIR, "FaceUnlockWithIR");
                    Log.i(TAG, "MTK project get IR pro = " + SystemProperties.get(PROP_MTK_FACEIR, ""));
                }
            }
            Log.i(TAG, "getIsDarkEnvironment: mIsDarkEnvironment = " + this.mIsDarkEnvironment + ", mProximity = " + this.mProximity + ", mIsDarkEnvironmentAndNoProximity = " + this.mIsDarkEnvironmentAndNoProximity);
            z2 = this.mIsDarkEnvironmentAndNoProximity;
        }
        return z2;
    }

    public String getRectifiedLuxStr() {
        return new DecimalFormat("##0.00").format((double) this.mRectifiedLux);
    }

    public boolean getIsScreenOnWhenGetLux() {
        return false;
    }

    private void notifyProximitySensor(boolean on) {
        Log.i(TAG, "notifyProximitySensor: led is " + (on ? Switch.SWITCH_ATTR_VALUE_ON : Switch.SWITCH_ATTR_VALUE_OFF));
        VivoSensorOperationResult operationRes = new VivoSensorOperationResult();
        int[] mOperationArgs = new int[3];
        mOperationArgs[0] = 202;
        if (on) {
            mOperationArgs[1] = 1;
        } else {
            mOperationArgs[1] = 0;
        }
        if (this.mVivoSensorOperationUtils != null) {
            try {
                this.mVivoSensorOperationUtils.executeCommand(mOperationArgs[0], operationRes, mOperationArgs, mOperationArgs.length);
                Log.i(TAG, "notifyProximitySensor: success!");
            } catch (Exception e) {
                Log.e(TAG, "Fail to notify proximity sensor");
            }
        }
    }

    public boolean getIsIrLedAvailable() {
        if (FaceCameraManager.IS_RGB_IR_SCHEME) {
            boolean z;
            synchronized (this.mIrLock) {
                if (!this.mHasJudgeIrLedAvailable) {
                    this.mHasJudgeIrLedAvailable = true;
                    updateIrLedAvailableCyclesLock();
                }
                z = this.mIsIrLedAvailable;
            }
            return z;
        }
        this.mIsIrLedAvailable = false;
        return false;
    }

    private void updateIrLedAvailableCyclesLock() {
        Log.i(TAG, "updateIrLedAvailableCyclesLock: ");
        if (this.mIrLedOffTime == 0) {
            Slog.i(TAG, "updateIrLedAvailableCyclesLock: first time");
            return;
        }
        long coolingTimeInMillis = System.currentTimeMillis() - this.mIrLedOffTime;
        this.mIrLedOffTime = System.currentTimeMillis();
        Slog.i(TAG, "updateIrLedAvailableCyclesLock: coolingTimeInMillis = " + coolingTimeInMillis);
        if (coolingTimeInMillis > 0) {
            long coolingTimeInSecond = coolingTimeInMillis / 1000;
            if (coolingTimeInSecond > ((long) MAX_FLASH_CYCLES_OF_IR_LED)) {
                coolingTimeInSecond = (long) MAX_FLASH_CYCLES_OF_IR_LED;
            }
            this.mAvailableCyclesOfIrLed += ((int) coolingTimeInSecond) * 1;
            if (this.mAvailableCyclesOfIrLed > MAX_FLASH_CYCLES_OF_IR_LED) {
                this.mAvailableCyclesOfIrLed = MAX_FLASH_CYCLES_OF_IR_LED;
            }
            Slog.i(TAG, "updateIrLedAvailableCyclesLock: mAvailableCyclesOfIrLed = " + this.mAvailableCyclesOfIrLed);
            this.mIsIrLedAvailable = this.mAvailableCyclesOfIrLed >= MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE;
        } else {
            Slog.e(TAG, "updateIrLedAvailableCyclesLock: coolingTimeInMillis < 0ms, error!");
        }
    }

    public void enableProximitySensor(boolean enable) {
        if (FaceCameraManager.IS_RGB_IR_SCHEME) {
            Log.i(TAG, "enableProximitySensor: enable = " + enable + ", mProximityEnabled = " + this.mProximityEnabled);
            if (this.mProximityEnabled != enable) {
                if (enable) {
                    this.mProximityEnabled = this.mSensorManager.registerListener(this.mSensorEventListener, this.mProximitySensor, 0, this.mSensorHandler);
                } else {
                    this.mSensorManager.unregisterListener(this.mSensorEventListener);
                    this.mProximityEnabled = false;
                    this.mProximity = false;
                }
            }
        }
    }

    private void showTips(final boolean available) {
        if (SystemProperties.getInt(SHOW_TIPS, 0) == 1) {
            this.mMainThreadHandler.post(new Runnable() {
                public void run() {
                    if (!FaceCamera.this.mAlertDialog.isShowing()) {
                        String msg;
                        if (available) {
                            msg = "IR led available count: " + FaceCamera.this.mAvailableCyclesOfIrLed + " \n\n" + "led is ON";
                        } else {
                            msg = "IR led available count: " + FaceCamera.this.mAvailableCyclesOfIrLed + " \n\n" + "led is cooling...";
                        }
                        FaceCamera.this.mAlertDialog.setMessage(msg);
                        FaceCamera.this.mAlertDialog.show();
                    }
                }
            });
        }
    }

    private void hideTips() {
        if (SystemProperties.getInt(SHOW_TIPS, 0) == 1) {
            this.mMainThreadHandler.post(new Runnable() {
                public void run() {
                    if (FaceCamera.this.mAlertDialog.isShowing()) {
                        FaceCamera.this.mAlertDialog.dismiss();
                    }
                }
            });
        }
    }

    public boolean isIrLedOpened() {
        Log.i(TAG, "isIrLedOpened: " + this.mIrLedOpened);
        return this.mIrLedOpened;
    }

    private void newInstanceClassForMtkNewPlatform() {
        try {
            Class<?> DeclareCls = Class.forName("com.mediatek.powerhalmgr.PowerHalMgrFactory");
            if (DeclareCls != null) {
                Method getInstanceMethod = DeclareCls.getMethod("getInstance", new Class[0]);
                if (getInstanceMethod != null) {
                    Method makePowerHalMgrMethod = DeclareCls.getMethod("makePowerHalMgr", new Class[0]);
                    if (makePowerHalMgrMethod != null) {
                        Object factoryCls = getInstanceMethod.invoke(null, new Object[0]);
                        if (factoryCls != null) {
                            mPowerHalService = makePowerHalMgrMethod.invoke(factoryCls, new Object[0]);
                            if (mPowerHalService != null && mPowerHandle == -1) {
                                try {
                                    Class<?> cls = Class.forName("com.mediatek.powerhalmgr.PowerHalMgr");
                                    if (cls != null) {
                                        Method scnRegMethod = cls.getMethod("scnReg", new Class[0]);
                                        if (scnRegMethod != null) {
                                            mPowerHandle = ((Integer) scnRegMethod.invoke(mPowerHalService, new Object[0])).intValue();
                                            this.scnConfigMethod = cls.getMethod("scnConfig", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                                            this.scnEnableMethod = cls.getMethod("scnEnable", new Class[]{Integer.TYPE, Integer.TYPE});
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void performFunctionForMtkNewPlatform() {
        try {
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(4), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(3000000), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3000000), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(35), Integer.valueOf(100), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(62), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnConfigMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(11), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)});
            this.scnEnableMethod.invoke(mPowerHalService, new Object[]{Integer.valueOf(mPowerHandle), Integer.valueOf(1000)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
