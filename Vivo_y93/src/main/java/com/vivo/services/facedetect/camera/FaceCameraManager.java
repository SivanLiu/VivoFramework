package com.vivo.services.facedetect.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import com.vivo.services.facedetect.BoostConfig;
import com.vivo.services.facedetect.FaceDebugConfig;
import com.vivo.services.facedetect.FaceDetectService;
import com.vivo.services.facedetect.FaceDetectShareMemory;
import com.vivo.services.facedetect.camera.FaceCamera2.PreviewCallback2;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FaceCameraManager {
    private static final String[] FACEWINDOW_PROJECT = new String[]{"PD1809", "PD1814F_EX", "PD1813", "PD1813F_EX", "PD1813C", "PD1814", "PD1816", "PD1813B", "PD1818B", "PD1818C", "PD1813D"};
    private static final String[] IR_PROJECT = new String[]{"PD1809", "PD1814F_EX", "PD1813", "PD1814", "PD1816", "PD1813B", "PD1813D"};
    public static boolean IS_RGB_IR_SCHEME = false;
    public static boolean IS_SUPPORT_FACEWINDOW = false;
    private static final String MODEL = SystemProperties.get("ro.vivo.product.model", "");
    private static final String[] PLATFORM = new String[]{"sdm710", "sdm670", "sdm845"};
    public static final int PREVIEW_HEIGHT = 480;
    public static final int PREVIEW_WIDTH = 640;
    private static final String PROP_RGB_IR = "persist.sys.facedetect.rgb_ir";
    private static final String TAG = "FaceCameraManager";
    private static int aBoostTimeOut = 1000;
    private static Object mPowerHalService = null;
    private static Object mPowerHallock = new Object();
    private static int mPowerHandle = -1;
    private boolean isUseCamera2 = false;
    private Object mCameraPerf = null;
    private CameraPreviewCallback mCameraPreviewCallback;
    private long mCameraTimeStart;
    private FaceCamera mFaceCamera;
    private FaceCamera2 mFaceCamera2;
    private boolean mFingerFaceCombine = false;
    private int mFrameCount = 0;
    private boolean mIsUse2PD = false;
    private Method mMethod = null;
    private Object mPPDataCallback = null;
    private Object mPreviewDataLock = new Object();
    private int mScenario = -1;
    private Method scnConfigMethod = null;
    private Method scnEnableMethod = null;

    public interface CameraPreviewCallback {
        void onPreviewFrame(byte[] bArr);
    }

    public interface CameraOpenCallback {
        void onCameraCallback(int i);
    }

    private class CameraCallBack2 implements PreviewCallback2 {
        /* synthetic */ CameraCallBack2(FaceCameraManager this$0, CameraCallBack2 -this1) {
            this();
        }

        private CameraCallBack2() {
        }

        public void onPreviewFrame(byte[] data) {
            Slog.i(FaceCameraManager.TAG, "use camera 2 data");
            FaceCameraManager.this.calCameraTime();
            if (FaceCameraManager.this.mCameraPreviewCallback != null) {
                FaceCameraManager.this.mCameraPreviewCallback.onPreviewFrame(data);
            }
        }
    }

    private class CameraCallBack implements PreviewCallback {
        /* synthetic */ CameraCallBack(FaceCameraManager this$0, CameraCallBack -this1) {
            this();
        }

        private CameraCallBack() {
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            Slog.i(FaceCameraManager.TAG, "use camera old data");
            FaceCameraManager.this.calCameraTime();
            if (FaceCameraManager.this.mCameraPreviewCallback != null) {
                FaceCameraManager.this.mCameraPreviewCallback.onPreviewFrame(data);
            }
        }
    }

    private class MyInterfaceCallback implements InvocationHandler {
        /* synthetic */ MyInterfaceCallback(FaceCameraManager this$0, MyInterfaceCallback -this1) {
            this();
        }

        private MyInterfaceCallback() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!(args == null || args[0] == null)) {
                FaceCameraManager.this.calCameraTime();
                FileDescriptor fd = args[0];
                synchronized (FaceCameraManager.this.mPreviewDataLock) {
                    FaceDetectShareMemory faceDetectShareMemory = null;
                    try {
                        faceDetectShareMemory = new FaceDetectShareMemory(fd, FaceDetectService.MIXED_DATA_LENGTH, true);
                    } catch (IOException e) {
                        Slog.e(FaceCameraManager.TAG, "error", e);
                    }
                    if (faceDetectShareMemory != null) {
                        if (faceDetectShareMemory.getSize() == FaceDetectService.MIXED_DATA_LENGTH) {
                            byte[] data = faceDetectShareMemory.readData(FaceDetectService.MIXED_DATA_LENGTH);
                            if (FaceCameraManager.this.mCameraPreviewCallback != null) {
                                FaceCameraManager.this.mCameraPreviewCallback.onPreviewFrame(data);
                            }
                        }
                    }
                    if (faceDetectShareMemory != null) {
                        faceDetectShareMemory.releaseShareMemory();
                    }
                }
            }
            return null;
        }
    }

    public FaceCameraManager(Context context, Handler handler) {
        getPlatform();
        getIrConfigure();
        setFaceWindowService();
        initPdValues();
        if (this.isUseCamera2) {
            this.mFaceCamera2 = new FaceCamera2(context, handler);
        } else {
            this.mFaceCamera = new FaceCamera(context, handler);
        }
    }

    public void setFingerFaceCombine(boolean enable) {
        if (this.isUseCamera2) {
            this.mFaceCamera2.setFingerFaceCombine(enable);
        }
        this.mFingerFaceCombine = enable;
    }

    public void set2PdEnable(boolean enable) {
        this.mIsUse2PD = enable;
        if (this.isUseCamera2) {
            this.mFaceCamera2.set2PdEnable(enable);
        } else {
            this.mFaceCamera.set2PdEnable(enable);
        }
    }

    public void enableProximitySensor(boolean enable) {
        if (this.isUseCamera2) {
            this.mFaceCamera2.enableProximitySensor(enable);
        } else {
            this.mFaceCamera.enableProximitySensor(enable);
        }
    }

    public void keyGuardExit() {
        if (this.isUseCamera2) {
            this.mFaceCamera2.keyGuardExit();
        }
    }

    public boolean isIrLedOpened() {
        if (this.isUseCamera2) {
            return this.mFaceCamera2.isIrLedOpened();
        }
        return this.mFaceCamera.isIrLedOpened();
    }

    public int restartCamera() {
        increaseFrequency();
        startCameraTime();
        if (this.isUseCamera2) {
            return this.mFaceCamera2.restartCamera();
        }
        return this.mFaceCamera.restartCamera();
    }

    public boolean getUseCamera2() {
        return this.isUseCamera2;
    }

    public void releaseCamera() {
        if (this.isUseCamera2) {
            this.mFaceCamera2.releaseCamera();
        } else {
            this.mFaceCamera.releaseCamera();
        }
    }

    public void onScreenState(boolean isScreenOn) {
        if (this.isUseCamera2) {
            this.mFaceCamera2.onScreenState(isScreenOn);
        }
    }

    public boolean getIsDarkEnvironment() {
        if (this.isUseCamera2) {
            return this.mFaceCamera2.getIsDarkEnvironment();
        }
        return this.mFaceCamera.getIsDarkEnvironment();
    }

    public String getRectifiedLuxStr() {
        if (this.isUseCamera2) {
            return this.mFaceCamera2.getRectifiedLuxStr();
        }
        return this.mFaceCamera.getRectifiedLuxStr();
    }

    public boolean getIsScreenOnWhenGetLux() {
        if (this.isUseCamera2) {
            return this.mFaceCamera2.getIsScreenOnWhenGetLux();
        }
        return this.mFaceCamera.getIsScreenOnWhenGetLux();
    }

    public boolean updateCamera() {
        if (this.isUseCamera2) {
            return true;
        }
        return this.mFaceCamera.updateCamera();
    }

    public boolean openCamera(int mCameraFacing) {
        if (this.isUseCamera2) {
            return true;
        }
        return this.mFaceCamera.openCamera(mCameraFacing);
    }

    public boolean getIsIrLedAvailable() {
        if (this.isUseCamera2) {
            return this.mFaceCamera2.getIsIrLedAvailable();
        }
        return this.mFaceCamera.getIsIrLedAvailable();
    }

    public void stopPreview() {
        if (!this.isUseCamera2) {
            this.mFaceCamera.stopPreview();
        }
    }

    public void updateCameraParametersControlPreviewData(int flag) {
        if (!this.isUseCamera2) {
            this.mFaceCamera.updateCameraParametersControlPreviewData(flag);
        }
    }

    public void setPPDataCallback(Object pdDataCallback) {
        if (!this.isUseCamera2) {
            this.mFaceCamera.setPPDataCallback(pdDataCallback);
        }
    }

    public void setCameraPreviewCallback(CameraPreviewCallback cameraPreviewCallback) {
        this.mCameraPreviewCallback = cameraPreviewCallback;
        if (this.isUseCamera2) {
            this.mFaceCamera2.setPreviewCallback2(new CameraCallBack2(this, null));
        } else if (this.mIsUse2PD) {
            this.mFaceCamera.setPPDataCallback(this.mPPDataCallback);
        } else {
            this.mFaceCamera.setPreviewCallback(new CameraCallBack(this, null));
        }
    }

    public void setCameraOpenCallback(CameraOpenCallback cameraOpenCallback) {
        if (this.isUseCamera2) {
            this.mFaceCamera2.setCameraOpenCallback(cameraOpenCallback);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A:{SYNTHETIC, Splitter: B:34:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0089 A:{SYNTHETIC, Splitter: B:26:0x0089} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getIrConfigure() {
        IOException e;
        Throwable th;
        boolean z = true;
        if (isStrArrayContrast(IR_PROJECT, MODEL)) {
            IS_RGB_IR_SCHEME = true;
            SystemProperties.set(PROP_RGB_IR, "2");
            return;
        }
        int rgbIr = SystemProperties.getInt(PROP_RGB_IR, -1);
        if (rgbIr == -1) {
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader("persist/camera/ir_id_sub"));
                try {
                    String ir = reader2.readLine();
                    Slog.i(TAG, "ir = " + ir);
                    if ("2".equals(ir)) {
                        IS_RGB_IR_SCHEME = true;
                        SystemProperties.set(PROP_RGB_IR, ir);
                    } else if ("0".equals(ir)) {
                        IS_RGB_IR_SCHEME = false;
                        SystemProperties.set(PROP_RGB_IR, ir);
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (IOException e3) {
                    e2 = e3;
                    reader = reader2;
                    try {
                        Slog.e(TAG, "static initializer: ", e2);
                        e2.printStackTrace();
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222 = e4;
                Slog.e(TAG, "static initializer: ", e222);
                e222.printStackTrace();
                if (reader != null) {
                }
            }
        }
        if (rgbIr != 2) {
            z = false;
        }
        IS_RGB_IR_SCHEME = z;
    }

    private void setFaceWindowService() {
        if (isStrArrayContrast(FACEWINDOW_PROJECT, MODEL)) {
            IS_SUPPORT_FACEWINDOW = true;
        }
    }

    private void getPlatform() {
        String mPlatform = SystemProperties.get("ro.board.platform", "");
        Slog.i(TAG, "platform is :" + mPlatform);
        if (isStrArrayContrast(PLATFORM, mPlatform)) {
            this.isUseCamera2 = true;
        } else {
            this.isUseCamera2 = false;
        }
    }

    private boolean isStrArrayContrast(String[] params, String str) {
        for (String param : params) {
            if (param.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void increaseFrequency() {
        if (BoostConfig.isMtkProductModel()) {
            if (this.mCameraPerf == null) {
                newInstanceClassForMtk();
            }
            if (this.mCameraPerf != null && this.mMethod != null) {
                performFunctionForMtk();
            }
        } else if (BoostConfig.isMtkNewBoostFramework()) {
            if (mPowerHalService == null) {
                newInstanceClassForMtkNewPlatform();
            }
            if (FaceDebugConfig.DEBUG_TIME) {
                Slog.i(TAG, "openCamera conf = " + this.scnConfigMethod + ", scm = " + this.scnEnableMethod + ", pref = " + mPowerHalService + ", ph = " + mPowerHandle);
            }
            if (mPowerHalService != null && mPowerHandle != -1 && this.scnConfigMethod != null && this.scnEnableMethod != null) {
                performFunctionForMtkNewPlatform();
            }
        } else {
            if (this.mCameraPerf == null) {
                newInstanceClass("android.util.BoostFramework");
            }
            if (this.mCameraPerf != null && this.mMethod != null) {
                performFunction();
            }
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
        Slog.d(TAG, "performFunctionForMtk enter");
        try {
            if (this.mScenario == -1) {
                this.mScenario = getScenarioForMtk();
            }
            Slog.d(TAG, "mScenario = " + this.mScenario + ",aBoostTimeOut = " + aBoostTimeOut);
            if (this.mCameraPerf != null && this.mMethod != null && this.mScenario != -1) {
                this.mMethod.invoke(this.mCameraPerf, new Object[]{Integer.valueOf(this.mScenario), Integer.valueOf(aBoostTimeOut)});
                Slog.d(TAG, "performFunctionForMtk boostEnableTimeoutMs success");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (this.mMethod != null) {
            int[] configs = BoostConfig.getCurrentModelBoostConfig();
            if (configs != null) {
                if (configs != null && this.mFingerFaceCombine) {
                    configs = BoostConfig.filterOutSchedBoost(configs);
                }
                try {
                    this.mMethod.invoke(this.mCameraPerf, new Object[]{Integer.valueOf(aBoostTimeOut), configs});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initPdValues() {
        MyInterfaceCallback mHandler = new MyInterfaceCallback(this, null);
        try {
            Class<?> mPdCallbackClass = Class.forName("android.hardware.Camera$PPDataCallback");
            if (mPdCallbackClass != null) {
                this.mPPDataCallback = Proxy.newProxyInstance(mPdCallbackClass.getClassLoader(), new Class[]{mPdCallbackClass}, mHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParam(boolean isDarkEnvironment) {
        this.mFaceCamera2.setParam(isDarkEnvironment);
    }

    public void calCameraTime() {
        if (FaceDebugConfig.DEBUG_TIME) {
            this.mFrameCount++;
            if (this.mFrameCount == 1) {
                Trace.traceEnd(8);
                Slog.d(TAG, "FaceCameraSpeed Time = " + (System.currentTimeMillis() - this.mCameraTimeStart));
            }
        }
    }

    private void startCameraTime() {
        if (FaceDebugConfig.DEBUG_TIME) {
            this.mFrameCount = 0;
            this.mCameraTimeStart = System.currentTimeMillis();
            Trace.traceBegin(8, "FaceCamera");
        }
    }
}
