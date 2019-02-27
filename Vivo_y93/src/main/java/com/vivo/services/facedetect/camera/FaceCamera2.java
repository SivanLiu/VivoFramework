package com.vivo.services.facedetect.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.fingerprint.FingerprintManager;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import android.view.Surface;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.sensoroperate.VivoSensorOperationResult;
import com.sensoroperate.VivoSensorOperationUtils;
import com.vivo.services.facedetect.FaceDebugConfig;
import com.vivo.services.facedetect.camera.FaceCameraManager.CameraOpenCallback;
import com.vivo.services.rms.ProcessList;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vendor.vivo.hardware.camera.provider.V1_0.IVivoCameraProvider;

public class FaceCamera2 {
    private static final int BRIGHT_ENVIRONMENT = 0;
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static final int DARK_ENVIRONMENT = 1;
    private static final int DARK_ENVIRONMENT_LUX = 5;
    private static int DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = 0;
    private static int DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 0;
    private static int DURATION_TIME_PER_CYCLE = 0;
    private static final int FORMAT_2PD = 32;
    private static final int LIGHT_THRESHOLD = SystemProperties.getInt("persist.sys.light_threshold", ProcessList.SERVICE_ADJ);
    private static int MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 0;
    private static int MAX_FLASH_CYCLES_OF_IR_LED = 0;
    private static final String MODEL = SystemProperties.get("ro.vivo.product.model", "");
    private static final int MSG_CAMERA_CONFIG = 3;
    private static final int MSG_CREATE_CAMERA_SESSION = 2;
    private static final int MSG_OPEN_CAMERA = 4;
    private static final int MSG_RELEASE_CAMERA = 1;
    private static final int PREVIEW_HEIGHT = 480;
    private static final int PREVIEW_HEIGHT_2PD = 756;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_WIDTH_2PD = 4032;
    private static final String SHOW_TIPS = "persist.facedetect.showtips";
    private static final String TAG = "FaceCamera2";
    private static final String TAG_SPEED = "FaceCamera2Speed";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private int OPEN_CAMERA_FAILED = 2;
    private int OPEN_CAMERA_OCCUPIED = 1;
    private int OPEN_CAMERA_SUCCESS = 0;
    private Map<Long, byte[]> m2PDImages;
    private boolean m2PdEnable = false;
    private AlertDialog mAlertDialog;
    private int mAvailableCyclesOfIrLed = MAX_FLASH_CYCLES_OF_IR_LED;
    private float mCallbackLux = 0.0f;
    private Camera2Handler mCamera2Handler;
    private CameraCaptureSession mCameraCaptureSession;
    private Object mCameraDataLock = new Object();
    private CameraDevice mCameraDevice;
    private String mCameraFacing;
    private CameraManager mCameraManager;
    private CameraOpenCallback mCameraOpenCallback;
    private IVivoCameraProvider mCameraProviderDaemon;
    private final Object mCameraStartLock = new Object();
    private long mCameraStartTime = 0;
    private Context mContext;
    private boolean mFingerFaceCombine = false;
    private FingerprintManager mFingerprintManager;
    private int mFrameCount = 0;
    private final Runnable mGetLuxRunnable = new Runnable() {
        public void run() {
            FaceCamera2.this.getIsDarkEnvironment();
        }
    };
    private long mGetLuxTime = 0;
    private boolean mHasGetLux = false;
    private boolean mHasJudgeIrLedAvailable = false;
    private ImageReader mImageReader;
    private ImageReader mImageReader2PD;
    private long mIrLedOffTime = 0;
    private long mIrLedOnTime = 0;
    private boolean mIrLedOpened = false;
    private final Object mIrLock = new Object();
    private boolean mIsCameraStart = false;
    private boolean mIsDarkEnvironment = false;
    private boolean mIsDarkEnvironmentAndNoProximity = false;
    private boolean mIsIrLedAvailable = true;
    private boolean mIsScreenOn = false;
    private boolean mIsScreenOnWhenGetLux = false;
    private boolean mLightEnabled = false;
    private Handler mLightHandler;
    private Sensor mLightSensor;
    private Handler mMainThreadHandler;
    private long mMaxCostTime = Long.MIN_VALUE;
    private long mMinCostTime = Long.MAX_VALUE;
    private final OnImageAvailableListener mOn2PDImageAvailableListener = new OnImageAvailableListener() {
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            FaceCamera2.this.m2PDImages.put(Long.valueOf(image.getTimestamp()), FaceCamera2.get2PdDataFromImage(image));
            FaceCamera2.this.mergerDataToFace(image.getTimestamp(), true);
            image.close();
        }
    };
    private final OnImageAvailableListener mOnImageAvailableListener = new OnImageAvailableListener() {
        public void onImageAvailable(ImageReader reader) {
            FaceCamera2 faceCamera2 = FaceCamera2.this;
            faceCamera2.mFrameCount = faceCamera2.mFrameCount + 1;
            if (FaceCamera2.this.mFrameCount == 1) {
                long cost = System.currentTimeMillis() - FaceCamera2.this.mSetRepeatingRequestFinishedTime;
                long totalCost = System.currentTimeMillis() - FaceCamera2.this.mCameraStartTime;
                if (FaceDebugConfig.DEBUG) {
                    Trace.traceEnd(8);
                    Slog.i(FaceCamera2.TAG_SPEED, "setRepeatingRequest() to 1st Frame, cost = " + cost);
                    Slog.i(FaceCamera2.TAG_SPEED, "openCamera() to 1st Frame, cost = " + totalCost);
                }
            }
            Image image = reader.acquireLatestImage();
            byte[] data = FaceCamera2.getDataFromImage(image, 2);
            if (FaceCamera2.this.m2PdEnable) {
                FaceCamera2.this.mYuvImages.put(Long.valueOf(image.getTimestamp()), data);
                FaceCamera2.this.mergerDataToFace(image.getTimestamp(), false);
            } else if (FaceCamera2.this.mPreviewCallback2 != null) {
                FaceCamera2.this.mPreviewCallback2.onPreviewFrame(data);
            }
            image.close();
        }
    };
    private PreviewCallback2 mPreviewCallback2;
    private Builder mPreviewRequestBuilder;
    private boolean mProximity = false;
    private boolean mProximityEnabled = false;
    private Sensor mProximitySensor;
    private float mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
    private DeathRecipient mRecipient = new DeathRecipient() {
        public void serviceDied(long cookie) {
            Slog.e(FaceCamera2.TAG, "HIDL CameraProviderDaemon Died");
            FaceCamera2.this.mCameraProviderDaemon = null;
        }
    };
    private float mRectifiedLux = 0.0f;
    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int sensorType = event.sensor.getType();
            if (sensorType == 8) {
                float distance = event.values[0];
                boolean proximity = distance >= 0.0f && distance < FaceCamera2.this.mProximityThreshold;
                FaceCamera2.this.mProximity = proximity;
                Slog.i(FaceCamera2.TAG, "onSensorChanged: proximity = " + proximity);
            } else if (sensorType == 5) {
                float light = event.values[0];
                if (FaceCamera2.this.mIsScreenOn) {
                    synchronized (FaceCamera2.this.mIrLock) {
                        FaceCamera2.this.mCallbackLux = light;
                    }
                }
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(FaceCamera2.TAG, "onSensorChanged: mIsScreenOn = " + FaceCamera2.this.mIsScreenOn + ", light = " + light);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Handler mSensorHandler;
    private SensorManager mSensorManager;
    private long mSetRepeatingRequestFinishedTime = 0;
    private final StateCallback mStateCallback = new StateCallback() {
        public void onOpened(CameraDevice cameraDevice) {
            Slog.i(FaceCamera2.TAG, "camera2 open success");
            FaceCamera2.this.mCameraDevice = cameraDevice;
            FaceCamera2.this.setParam(FaceCamera2.this.getIsDarkEnvironment());
            if (FaceCamera2.this.mCameraOpenCallback != null) {
                FaceCamera2.this.mCameraOpenCallback.onCameraCallback(FaceCamera2.this.OPEN_CAMERA_SUCCESS);
            }
            FaceCamera2.this.sendMessageCamera2Handler(2);
            FaceCamera2.this.setFingerprintCommand(true);
        }

        public void onDisconnected(CameraDevice cameraDevice) {
            Slog.i(FaceCamera2.TAG, "camera2 Disconnected");
            cameraDevice.close();
            FaceCamera2.this.mCameraDevice = null;
            FaceCamera2.this.mIsCameraStart = false;
        }

        public void onError(CameraDevice cameraDevice, int error) {
            Slog.e(FaceCamera2.TAG, "open camera2 error is:" + error);
            if (FaceCamera2.this.mCameraOpenCallback != null) {
                FaceCamera2.this.mCameraOpenCallback.onCameraCallback(error);
            }
            cameraDevice.close();
            FaceCamera2.this.mCameraDevice = null;
            FaceCamera2.this.mIsCameraStart = false;
        }
    };
    private VivoSensorOperationUtils mVivoSensorOperationUtils;
    private Map<Long, byte[]> mYuvImages;

    private class Camera2Handler extends Handler {
        /* synthetic */ Camera2Handler(FaceCamera2 this$0, Looper looper, Camera2Handler -this2) {
            this(looper);
        }

        private Camera2Handler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Slog.i(FaceCamera2.TAG, "MSG_RELEASE_CAMERA");
                    FaceCamera2.this.handlerReleaseCamera();
                    return;
                case 2:
                    Slog.i(FaceCamera2.TAG, "MSG_CREATE_CAMERA_SESSION");
                    FaceCamera2.this.handlerCreateCameraSession();
                    return;
                case 3:
                    Slog.i(FaceCamera2.TAG, "MSG_CAMERA_CONFIG");
                    FaceCamera2.this.handlerSessionConfig();
                    return;
                case 4:
                    Slog.i(FaceCamera2.TAG, "MSG_OPEN_CAMERA");
                    FaceCamera2.this.handlerOpenCamera();
                    return;
                default:
                    return;
            }
        }
    }

    public interface PreviewCallback2 {
        void onPreviewFrame(byte[] bArr);
    }

    static {
        MAX_FLASH_CYCLES_OF_IR_LED = 40;
        MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 10;
        DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 90;
        DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = 330;
        DURATION_TIME_PER_CYCLE = DURATION_TIME_OF_IR_LED_ON_PER_CYCLE + DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE;
        if ("PD1809".equals(MODEL) || "PD1809F_EX".equals(MODEL) || "PD1813B".equals(MODEL) || "PD1813D".equals(MODEL)) {
            MAX_FLASH_CYCLES_OF_IR_LED = 28;
            MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE = 7;
            DURATION_TIME_OF_IR_LED_ON_PER_CYCLE = 110;
            DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE = ProcessList.VERY_LASTEST_PREVIOUS_APP_ADJ;
            DURATION_TIME_PER_CYCLE = DURATION_TIME_OF_IR_LED_ON_PER_CYCLE + DURATION_TIME_OF_IR_LED_OFF_PER_CYCLE;
        }
    }

    public FaceCamera2(Context context, Handler handler) {
        this.mContext = context;
        this.mSensorHandler = handler;
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        HandlerThread camera2Thread = new HandlerThread(TAG);
        camera2Thread.start();
        this.mCamera2Handler = new Camera2Handler(this, camera2Thread.getLooper(), null);
        HandlerThread handlerThread = new HandlerThread("FaceLight");
        handlerThread.start();
        this.mLightHandler = new Handler(handlerThread.getLooper());
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
        this.mVivoSensorOperationUtils = VivoSensorOperationUtils.getInstance();
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mSensorManager != null) {
            this.mProximitySensor = this.mSensorManager.getDefaultSensor(8, true);
            this.mLightSensor = this.mSensorManager.getDefaultSensor(5, true);
            if (this.mProximitySensor == null) {
                Slog.e(TAG, "mProximitySensor is null!");
                this.mProximityThreshold = Math.min(1.0f, TYPICAL_PROXIMITY_THRESHOLD);
            } else {
                this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
            }
        }
        this.mYuvImages = new HashMap();
        this.m2PDImages = new HashMap();
        initAlertDialog();
        Slog.i(TAG, "mAvailableCyclesOfIrLed = " + this.mAvailableCyclesOfIrLed);
    }

    private void initAlertDialog() {
        this.mAlertDialog = new AlertDialog.Builder(this.mContext).create();
        this.mAlertDialog.setTitle("tips");
        this.mAlertDialog.setButton(-2, "sure", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mAlertDialog.setCanceledOnTouchOutside(false);
        this.mAlertDialog.getWindow().addFlags(IQcRilHook.QCRILHOOK_BASE);
        this.mAlertDialog.getWindow().setType(2009);
    }

    public void set2PdEnable(boolean enable) {
        this.m2PdEnable = enable;
    }

    public void setCameraOpenCallback(CameraOpenCallback cameraOpenCallback) {
        this.mCameraOpenCallback = cameraOpenCallback;
    }

    public void setFingerFaceCombine(boolean enable) {
        this.mFingerFaceCombine = enable;
    }

    public boolean getIsDarkEnvironment() {
        boolean z = true;
        boolean z2 = false;
        if (!FaceCameraManager.IS_RGB_IR_SCHEME) {
            return false;
        }
        synchronized (this.mIrLock) {
            if (!this.mHasGetLux) {
                this.mHasGetLux = true;
                if (this.mIsScreenOn) {
                    this.mRectifiedLux = this.mCallbackLux;
                    if (this.mRectifiedLux > 20.0f) {
                        z = false;
                    }
                    this.mIsDarkEnvironment = z;
                } else {
                    this.mRectifiedLux = getRectifiedLux();
                    if (this.mRectifiedLux > TYPICAL_PROXIMITY_THRESHOLD) {
                        z = false;
                    }
                    this.mIsDarkEnvironment = z;
                }
                this.mIsScreenOnWhenGetLux = this.mIsScreenOn;
                if (this.mIsDarkEnvironment) {
                    z2 = this.mProximity ^ 1;
                }
                this.mIsDarkEnvironmentAndNoProximity = z2;
                this.mGetLuxTime = System.currentTimeMillis();
            }
            Slog.i(TAG, "getIsDarkEnvironment: mIsDarkEnvironment = " + this.mIsDarkEnvironment + ", mProximity = " + this.mProximity + ", mIsDarkEnvironmentAndNoProximity = " + this.mIsDarkEnvironmentAndNoProximity + ", mIsScreenOn = " + this.mIsScreenOn);
            z2 = this.mIsDarkEnvironmentAndNoProximity;
        }
        return z2;
    }

    public String getRectifiedLuxStr() {
        return new DecimalFormat("##0.00").format((double) this.mRectifiedLux);
    }

    public boolean getIsScreenOnWhenGetLux() {
        return this.mIsScreenOnWhenGetLux;
    }

    public boolean isIrLedOpened() {
        Slog.i(TAG, "isIrLedOpened: " + this.mIrLedOpened);
        return this.mIrLedOpened;
    }

    public void enableProximitySensor(boolean enable) {
        if (FaceCameraManager.IS_RGB_IR_SCHEME) {
            Slog.i(TAG, "enableProximitySensor: enable = " + enable + ", mProximityEnabled = " + this.mProximityEnabled);
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

    public void keyGuardExit() {
        this.mIsCameraStart = false;
    }

    public void handlerLightSensor(boolean enable) {
        if (this.mProximityEnabled) {
            if (this.mLightEnabled != enable) {
                if (enable) {
                    this.mLightEnabled = this.mSensorManager.registerListener(this.mSensorEventListener, this.mLightSensor, 0, this.mSensorHandler);
                } else {
                    this.mSensorManager.unregisterListener(this.mSensorEventListener, this.mLightSensor);
                    this.mLightEnabled = false;
                    this.mIsDarkEnvironment = false;
                    this.mIsDarkEnvironmentAndNoProximity = false;
                }
            }
            return;
        }
        Slog.i(TAG, "Light sensor fail mProximityEnabled is:" + this.mProximityEnabled);
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

    private void releaseCameraForIrLocked() {
        synchronized (this.mIrLock) {
            if (this.mIsDarkEnvironmentAndNoProximity) {
                if (this.mIsIrLedAvailable) {
                    this.mIrLedOffTime = System.currentTimeMillis();
                    int duration = (int) (this.mIrLedOffTime - this.mIrLedOnTime);
                    if (duration > 0) {
                        int flashCycle = (duration / DURATION_TIME_PER_CYCLE) + 1;
                        if (flashCycle > MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE) {
                            flashCycle = MAX_FLASH_CYCLES_OF_A_VERIFY_CYCLE;
                        }
                        Slog.i(TAG, "releaseCameraForIrLocked: duration = " + duration + " ms, flashCycle = " + flashCycle);
                        this.mAvailableCyclesOfIrLed -= flashCycle;
                        if (this.mAvailableCyclesOfIrLed < 0) {
                            this.mAvailableCyclesOfIrLed = 0;
                        }
                    } else {
                        Slog.e(TAG, "releaseCameraForIrLocked: duration is " + duration + " ms < 0, error!!!");
                    }
                }
                hideTips();
            }
            this.mIsDarkEnvironment = false;
            this.mIsDarkEnvironmentAndNoProximity = false;
            this.mHasGetLux = false;
            this.mHasJudgeIrLedAvailable = false;
            Slog.i(TAG, "releaseCameraForIrLocked: mIsDarkEnvironment = " + this.mIsDarkEnvironment);
        }
    }

    private void updateIrLedAvailableCyclesLock() {
        Slog.i(TAG, "updateIrLedAvailableCyclesLock: ");
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

    public void openCamera2() {
        sendMessageCamera2Handler(4);
    }

    public void releaseCamera() {
        sendMessageCamera2Handler(1);
    }

    public int restartCamera() {
        Slog.i(TAG, "restartCamera2 mIsCameraStart is:" + this.mIsCameraStart);
        synchronized (this.mCameraStartLock) {
            if (!this.mIsCameraStart) {
                this.mIsCameraStart = true;
                openCamera2();
            }
        }
        return 0;
    }

    public void setPreviewCallback2(PreviewCallback2 previewCallback2) {
        this.mPreviewCallback2 = previewCallback2;
    }

    public void onScreenState(boolean isScreenOn) {
        Slog.i(TAG, "onScreenState is:" + isScreenOn);
        handlerLightSensor(isScreenOn);
        this.mIsScreenOn = isScreenOn;
    }

    private void getMCameraFacing() {
        try {
            for (String id : this.mCameraManager.getCameraIdList()) {
                if (((Integer) this.mCameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                    this.mCameraFacing = id;
                    return;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, e.getMessage());
        }
    }

    private void handlerReleaseCamera() {
        Slog.i(TAG, "handlerReleaseCamera begin");
        this.mIsCameraStart = false;
        if (FaceCameraManager.IS_RGB_IR_SCHEME) {
            releaseCameraForIrLocked();
        }
        if (this.mCameraDevice != null) {
            if (this.mCameraCaptureSession != null) {
                this.mCameraCaptureSession.close();
                this.mCameraCaptureSession = null;
            }
            this.mCameraDevice.close();
            this.mCameraDevice = null;
        }
        if (this.mImageReader != null) {
            this.mImageReader.close();
            this.mImageReader = null;
        }
        if (this.mImageReader2PD != null) {
            this.mImageReader2PD.close();
            this.mImageReader2PD = null;
        }
        this.mYuvImages.clear();
        this.m2PDImages.clear();
        setFingerprintCommand(false);
        Slog.i(TAG, "handlerReleaseCamera finish");
    }

    private void handlerCreateCameraSession() {
        createCameraPreviewSession();
    }

    private void handlerSessionConfig() {
        Slog.i(TAG, "handlerSessionConfig");
        if (this.mCameraDevice == null || this.mCameraCaptureSession == null) {
            Slog.i(TAG, "handlerSessionConfig fail camera device close");
            this.mIsCameraStart = false;
            return;
        }
        Slog.i(TAG, "CamerapressviewSession onConfigured");
        if (FaceCameraManager.IS_RGB_IR_SCHEME) {
            openIrLed();
        }
        CaptureRequest mPreviewRequest = this.mPreviewRequestBuilder.build();
        try {
            if (this.mCameraCaptureSession != null) {
                this.mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, null, this.mCamera2Handler);
                this.mSetRepeatingRequestFinishedTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Slog.e(TAG, "onConfigured fail:", e);
            this.mIsCameraStart = false;
        }
    }

    private void handlerOpenCamera() {
        try {
            if (this.mCameraFacing == null) {
                getMCameraFacing();
            }
            if (this.mCameraFacing != null) {
                Slog.i(TAG, "openCamera2 mCameraFacing is: " + this.mCameraFacing + ",m2PdEnable is:" + this.m2PdEnable + ",mIsScreenOn is:" + this.mIsScreenOn);
                this.mFrameCount = 0;
                this.mCameraStartTime = System.currentTimeMillis();
                this.mImageReader = ImageReader.newInstance(640, 480, 35, 1);
                this.mImageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, this.mCamera2Handler);
                if (this.m2PdEnable) {
                    this.mImageReader2PD = ImageReader.newInstance(PREVIEW_WIDTH_2PD, PREVIEW_HEIGHT_2PD, 32, 1);
                    this.mImageReader2PD.setOnImageAvailableListener(this.mOn2PDImageAvailableListener, this.mCamera2Handler);
                }
                if (FaceCameraManager.IS_RGB_IR_SCHEME) {
                    this.mSensorHandler.removeCallbacks(this.mGetLuxRunnable);
                    this.mSensorHandler.post(this.mGetLuxRunnable);
                }
                if (FaceDebugConfig.DEBUG) {
                    Trace.traceBegin(8, "FaceOpenCamera2");
                }
                long start = System.currentTimeMillis();
                this.mCameraManager.openCamera(this.mCameraFacing, this.mStateCallback, this.mCamera2Handler);
                long cost = System.currentTimeMillis() - start;
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG_SPEED, "openCamera() cost = " + cost);
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "open Camera fail ", e);
        }
    }

    private void openIrLed() {
        synchronized (this.mIrLock) {
            this.mIrLedOpened = false;
            Slog.i(TAG, "mIsDarkEnvironmentAndNoProximity = " + this.mIsDarkEnvironmentAndNoProximity);
            if (this.mIsDarkEnvironmentAndNoProximity) {
                if (getIsIrLedAvailable()) {
                    this.mIrLedOpened = true;
                    if (this.mPreviewRequestBuilder != null) {
                        this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(3));
                    }
                    this.mIrLedOnTime = System.currentTimeMillis();
                    Slog.i(TAG, "IR Led is available, open...");
                    showTips(true);
                } else {
                    Slog.i(TAG, "IR Led is not available, don't open...");
                    showTips(false);
                }
            }
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
        Slog.i(TAG, "getRectifiedLux: unRectifiedLux = " + light + " lux, currentCostTime = " + currentCostTime + " ms, mMaxCostTime = " + this.mMaxCostTime + " ms, mMinCostTime = " + this.mMinCostTime + " ms");
        if (light == -1.0f) {
            return 6.0f;
        }
        float rectifiedLux;
        if (light == 1.0f) {
            rectifiedLux = light;
        } else {
            rectifiedLux = (500.0f * light) / ((float) LIGHT_THRESHOLD);
        }
        Slog.i(TAG, "getRectifiedLux: rectifiedLux = " + rectifiedLux + " lux");
        return rectifiedLux;
    }

    private float getAlsRawDataTimeout() {
        final VivoSensorOperationResult result = new VivoSensorOperationResult();
        result.mTestVal[0] = -1.0f;
        final int[] operationArgs = new int[3];
        operationArgs[0] = 201;
        operationArgs[1] = 111;
        Slog.i(TAG, "getAlsRawDataTimeout: start");
        this.mLightHandler.runWithScissors(new Runnable() {
            public void run() {
                if (FaceCamera2.this.mVivoSensorOperationUtils != null) {
                    try {
                        FaceCamera2.this.mVivoSensorOperationUtils.executeCommand(operationArgs[0], result, operationArgs, operationArgs.length);
                        Slog.i(FaceCamera2.TAG, "Get als data after executeCommand: " + result.mTestVal[0]);
                    } catch (Exception e) {
                        Slog.e(FaceCamera2.TAG, "Fail to get als data");
                    }
                }
            }
        }, 100);
        Slog.i(TAG, "getAlsRawDataTimeout: end");
        Slog.i(TAG, "Get als data : " + result.mTestVal[0]);
        return result.mTestVal[0];
    }

    private void createCameraPreviewSession() {
        Slog.i(TAG, "createCameraPreviewSession");
        try {
            if (this.mCameraDevice == null) {
                Slog.i(TAG, "createCameraPreviewSession fail is camera device close");
                this.mIsCameraStart = false;
            } else if (this.mImageReader == null) {
                Slog.i(TAG, "createCameraPreviewSession fail is mImageReader device close");
                this.mCameraDevice.close();
                this.mCameraDevice = null;
                this.mIsCameraStart = false;
            } else {
                List<Surface> outputs;
                this.mPreviewRequestBuilder = this.mCameraDevice.createCaptureRequest(1);
                this.mPreviewRequestBuilder.addTarget(this.mImageReader.getSurface());
                if (this.m2PdEnable) {
                    this.mPreviewRequestBuilder.addTarget(this.mImageReader2PD.getSurface());
                    outputs = Arrays.asList(new Surface[]{this.mImageReader.getSurface(), this.mImageReader2PD.getSurface()});
                } else {
                    outputs = Arrays.asList(new Surface[]{this.mImageReader.getSurface()});
                }
                long start = System.currentTimeMillis();
                this.mCameraDevice.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
                    public void onConfigured(CameraCaptureSession session) {
                        FaceCamera2.this.mCameraCaptureSession = session;
                        FaceCamera2.this.sendMessageCamera2Handler(3);
                    }

                    public void onConfigureFailed(CameraCaptureSession session) {
                        Slog.e(FaceCamera2.TAG, "camera onConfigureFailed");
                        FaceCamera2.this.mIsCameraStart = false;
                    }
                }, this.mCamera2Handler);
                long cost = System.currentTimeMillis() - start;
                if (FaceDebugConfig.DEBUG) {
                    Slog.i(TAG_SPEED, "createCaptureSession, cost = " + cost);
                }
            }
        } catch (Exception e) {
            Slog.i(TAG, "createCameraPreviewSession fail:", e);
            this.mIsCameraStart = false;
        }
    }

    private static byte[] get2PdDataFromImage(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int stride = image.getPlanes()[0].getRowStride();
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        if (byteBuffer.hasArray()) {
            return byteBuffer.array();
        }
        byte[] data = new byte[byteBuffer.capacity()];
        byteBuffer.get(data);
        return data;
    }

    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat == 1 || colorFormat == 2) {
            Rect crop = image.getCropRect();
            int format = image.getFormat();
            int width = crop.width();
            int height = crop.height();
            Plane[] planes = image.getPlanes();
            byte[] data = new byte[(((width * height) * ImageFormat.getBitsPerPixel(format)) / 8)];
            byte[] rowData = new byte[planes[0].getRowStride()];
            int channelOffset = 0;
            int outputStride = 1;
            int i = 0;
            while (i < planes.length) {
                switch (i) {
                    case 0:
                        channelOffset = 0;
                        outputStride = 1;
                        break;
                    case 1:
                        if (colorFormat != 1) {
                            if (colorFormat == 2) {
                                channelOffset = (width * height) + 1;
                                outputStride = 2;
                                break;
                            }
                        }
                        channelOffset = width * height;
                        outputStride = 1;
                        break;
                        break;
                    case 2:
                        if (colorFormat != 1) {
                            if (colorFormat == 2) {
                                channelOffset = width * height;
                                outputStride = 2;
                                break;
                            }
                        }
                        channelOffset = (int) (((double) (width * height)) * 1.25d);
                        outputStride = 1;
                        break;
                        break;
                }
                ByteBuffer buffer = planes[i].getBuffer();
                int rowStride = planes[i].getRowStride();
                int pixelStride = planes[i].getPixelStride();
                int shift = i == 0 ? 0 : 1;
                int w = width >> shift;
                int h = height >> shift;
                buffer.position(((crop.top >> shift) * rowStride) + ((crop.left >> shift) * pixelStride));
                for (int row = 0; row < h; row++) {
                    int length;
                    if (pixelStride == 1 && outputStride == 1) {
                        length = w;
                        buffer.get(data, channelOffset, w);
                        channelOffset += w;
                    } else {
                        length = ((w - 1) * pixelStride) + 1;
                        buffer.get(rowData, 0, length);
                        for (int col = 0; col < w; col++) {
                            data[channelOffset] = rowData[col * pixelStride];
                            channelOffset += outputStride;
                        }
                    }
                    if (row < h - 1) {
                        buffer.position((buffer.position() + rowStride) - length);
                    }
                }
                i++;
            }
            return data;
        }
        throw new IllegalArgumentException("only support COLOR_FormatI420 and COLOR_FormatNV21");
    }

    private void sendMessageCamera2Handler(int what) {
        Message.obtain(this.mCamera2Handler, what).sendToTarget();
    }

    private void mergerDataToFace(long key, boolean is2PD) {
        synchronized (this.mCameraDataLock) {
            boolean isNeedToMerger = false;
            if (is2PD) {
                if (this.mYuvImages.containsKey(Long.valueOf(key))) {
                    isNeedToMerger = true;
                }
            } else if (this.m2PDImages.containsKey(Long.valueOf(key))) {
                isNeedToMerger = true;
            }
            if (isNeedToMerger) {
                byte[] data = byteMergerAll((byte[]) this.m2PDImages.get(Long.valueOf(key)), (byte[]) this.mYuvImages.get(Long.valueOf(key)));
                if (this.mPreviewCallback2 != null) {
                    this.mPreviewCallback2.onPreviewFrame(data);
                }
            }
        }
    }

    private static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (byte[] length : values) {
            length_byte += length.length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    private synchronized IVivoCameraProvider getCameraProviderDaemon() {
        if (this.mCameraProviderDaemon == null) {
            try {
                this.mCameraProviderDaemon = IVivoCameraProvider.getService();
            } catch (Exception e) {
                Slog.e(TAG, "Cameraprovider fail:", e);
            }
            if (this.mCameraProviderDaemon == null) {
                Slog.e(TAG, "CameraProviderDaemon HIDL not available");
                return null;
            }
            this.mCameraProviderDaemon.asBinder().linkToDeath(this.mRecipient, 0);
        }
        return this.mCameraProviderDaemon;
    }

    public void setParam(boolean isDarkEnvironment) {
        getCameraProviderDaemon();
        if (this.mCameraProviderDaemon == null) {
            return;
        }
        if (isDarkEnvironment) {
            try {
                Slog.i(TAG, "dark environment");
                this.mCameraProviderDaemon.setparam(0, 1);
                return;
            } catch (Exception e) {
                Slog.e(TAG, "setParam fail:", e);
                return;
            }
        }
        Slog.i(TAG, "bright environment");
        this.mCameraProviderDaemon.setparam(0, 0);
    }

    private void showTips(final boolean available) {
        if (SystemProperties.getInt(SHOW_TIPS, 0) == 1) {
            this.mMainThreadHandler.post(new Runnable() {
                public void run() {
                    if (!FaceCamera2.this.mAlertDialog.isShowing()) {
                        String msg;
                        if (available) {
                            msg = "IR led available count: " + FaceCamera2.this.mAvailableCyclesOfIrLed + " \n\n" + "led is ON";
                        } else {
                            msg = "IR led available count: " + FaceCamera2.this.mAvailableCyclesOfIrLed + " \n\n" + "led is cooling...";
                        }
                        FaceCamera2.this.mAlertDialog.setMessage(msg);
                        FaceCamera2.this.mAlertDialog.show();
                    }
                }
            });
        }
    }

    private void hideTips() {
        if (SystemProperties.getInt(SHOW_TIPS, 0) == 1) {
            this.mMainThreadHandler.post(new Runnable() {
                public void run() {
                    if (FaceCamera2.this.mAlertDialog.isShowing()) {
                        FaceCamera2.this.mAlertDialog.dismiss();
                    }
                }
            });
        }
    }

    private void setFingerprintCommand(boolean isOpenCameraSuccess) {
        if (this.mFingerFaceCombine) {
            if (this.mFingerprintManager == null) {
                this.mFingerprintManager = (FingerprintManager) this.mContext.getSystemService("fingerprint");
            }
            if (isOpenCameraSuccess) {
                this.mFingerprintManager.sendCommand(32579, 1);
            } else {
                this.mFingerprintManager.sendCommand(32579, 0);
            }
        }
    }
}
