package android.hardware.camera2;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.CameraStatus;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceListener.Stub;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.CameraDeviceUserShim;
import android.hardware.camera2.legacy.LegacyMetadataMapper;
import android.net.NetworkScoreManager;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.util.ArrayMap;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public final class CameraManager {
    private static final int API_VERSION_1 = 1;
    private static final int API_VERSION_2 = 2;
    private static final int CAMERA_TYPE_ALL = 1;
    private static final int CAMERA_TYPE_BACKWARD_COMPATIBLE = 0;
    private static final String TAG = "CameraManager";
    private static final int USE_CALLING_UID = -1;
    private final boolean DEBUG = false;
    private final Context mContext;
    private ArrayList<String> mDeviceIdList;
    private EventHandler mEventHandler;
    private String mIsNormalCamera = "true";
    private final Object mLock = new Object();

    public static abstract class AvailabilityCallback {
        public void onCameraAvailable(String cameraId) {
        }

        public void onCameraUnavailable(String cameraId) {
        }
    }

    private static final class CameraManagerGlobal extends Stub implements DeathRecipient {
        private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
        private static final String TAG = "CameraManagerGlobal";
        private static final CameraManagerGlobal gCameraManager = new CameraManagerGlobal();
        public static final boolean sCameraServiceDisabled = SystemProperties.getBoolean("config.disable_cameraservice", false);
        private final int CAMERA_SERVICE_RECONNECT_DELAY_MS = 1000;
        private final boolean DEBUG = false;
        private final ArrayMap<AvailabilityCallback, Handler> mCallbackMap = new ArrayMap();
        private ICameraService mCameraService;
        private final ArrayMap<String, Integer> mDeviceStatus = new ArrayMap();
        private final Object mLock = new Object();
        private final ArrayMap<TorchCallback, Handler> mTorchCallbackMap = new ArrayMap();
        private Binder mTorchClientBinder = new Binder();
        private final ArrayMap<String, Integer> mTorchStatus = new ArrayMap();

        private CameraManagerGlobal() {
        }

        public static CameraManagerGlobal get() {
            return gCameraManager;
        }

        public IBinder asBinder() {
            return this;
        }

        public ICameraService getCameraService() {
            ICameraService iCameraService;
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (this.mCameraService == null && (sCameraServiceDisabled ^ 1) != 0) {
                    Log.e(TAG, "Camera service is unavailable");
                }
                iCameraService = this.mCameraService;
            }
            return iCameraService;
        }

        private void connectCameraServiceLocked() {
            if (this.mCameraService == null && !sCameraServiceDisabled) {
                Log.i(TAG, "Connecting to camera service");
                IBinder cameraServiceBinder = ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME);
                if (cameraServiceBinder != null) {
                    try {
                        cameraServiceBinder.linkToDeath(this, 0);
                        ICameraService cameraService = ICameraService.Stub.asInterface(cameraServiceBinder);
                        try {
                            CameraMetadataNative.setupGlobalVendorTagDescriptor();
                        } catch (ServiceSpecificException e) {
                            handleRecoverableSetupErrors(e);
                        }
                        try {
                            for (CameraStatus c : cameraService.addListener(this)) {
                                onStatusChangedLocked(c.status, c.cameraId);
                            }
                            this.mCameraService = cameraService;
                        } catch (ServiceSpecificException e2) {
                            throw new IllegalStateException("Failed to register a camera service listener", e2);
                        } catch (RemoteException e3) {
                        }
                    } catch (RemoteException e4) {
                    }
                }
            }
        }

        public String[] getCameraIdList() {
            String[] cameraIds;
            synchronized (this.mLock) {
                int status;
                connectCameraServiceLocked();
                boolean exposeAuxCamera = false;
                String packageName = ActivityThread.currentOpPackageName();
                String packageList = SystemProperties.get("vendor.camera.aux.packagelist");
                if (packageList.length() > 0) {
                    StringSplitter<String> splitter = new SimpleStringSplitter(',');
                    splitter.setString(packageList);
                    for (String str : splitter) {
                        if (packageName.equals(str)) {
                            exposeAuxCamera = true;
                            break;
                        }
                    }
                }
                int idCount = 0;
                int i = 0;
                while (i < this.mDeviceStatus.size() && (exposeAuxCamera || i != 2)) {
                    status = ((Integer) this.mDeviceStatus.valueAt(i)).intValue();
                    if (!(status == 0 || status == 2)) {
                        idCount++;
                    }
                    i++;
                }
                cameraIds = new String[idCount];
                idCount = 0;
                i = 0;
                while (i < this.mDeviceStatus.size() && (exposeAuxCamera || i != 2)) {
                    status = ((Integer) this.mDeviceStatus.valueAt(i)).intValue();
                    if (!(status == 0 || status == 2)) {
                        cameraIds[idCount] = (String) this.mDeviceStatus.keyAt(i);
                        idCount++;
                    }
                    i++;
                }
            }
            return cameraIds;
        }

        public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
            synchronized (this.mLock) {
                if (cameraId == null) {
                    throw new IllegalArgumentException("cameraId was null");
                }
                boolean exposeAuxCamera = false;
                String packageName = ActivityThread.currentOpPackageName();
                String packageList = SystemProperties.get("vendor.camera.aux.packagelist");
                if (packageList.length() > 0) {
                    StringSplitter<String> splitter = new SimpleStringSplitter(',');
                    splitter.setString(packageList);
                    for (String str : splitter) {
                        if (packageName.equals(str)) {
                            exposeAuxCamera = true;
                            break;
                        }
                    }
                }
                if (exposeAuxCamera || Integer.parseInt(cameraId) < 2) {
                    ICameraService cameraService = getCameraService();
                    if (cameraService == null) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable");
                    }
                    try {
                        cameraService.setTorchMode(cameraId, enabled, this.mTorchClientBinder);
                    } catch (ServiceSpecificException e) {
                        CameraManager.throwAsPublicException(e);
                    } catch (RemoteException e2) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable");
                    }
                } else {
                    throw new IllegalArgumentException("invalid cameraId");
                }
            }
        }

        private void handleRecoverableSetupErrors(ServiceSpecificException e) {
            switch (e.errorCode) {
                case 4:
                    Log.w(TAG, e.getMessage());
                    return;
                default:
                    throw new IllegalStateException(e);
            }
        }

        private boolean isAvailable(int status) {
            switch (status) {
                case 1:
                    return true;
                default:
                    return false;
            }
        }

        private boolean validStatus(int status) {
            switch (status) {
                case -2:
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }

        private boolean validTorchStatus(int status) {
            switch (status) {
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }

        private void postSingleUpdate(final AvailabilityCallback callback, Handler handler, final String id, int status) {
            if (isAvailable(status)) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onCameraAvailable(id);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onCameraUnavailable(id);
                    }
                });
            }
        }

        private void postSingleTorchUpdate(final TorchCallback callback, Handler handler, final String id, final int status) {
            switch (status) {
                case 1:
                case 2:
                    handler.post(new Runnable() {
                        public void run() {
                            callback.onTorchModeChanged(id, status == 2);
                        }
                    });
                    return;
                default:
                    handler.post(new Runnable() {
                        public void run() {
                            callback.onTorchModeUnavailable(id);
                        }
                    });
                    return;
            }
        }

        private void updateCallbackLocked(AvailabilityCallback callback, Handler handler) {
            for (int i = 0; i < this.mDeviceStatus.size(); i++) {
                postSingleUpdate(callback, handler, (String) this.mDeviceStatus.keyAt(i), ((Integer) this.mDeviceStatus.valueAt(i)).intValue());
            }
        }

        private void onStatusChangedLocked(int status, String id) {
            boolean exposeMonoCamera = false;
            String packageName = ActivityThread.currentOpPackageName();
            String packageList = SystemProperties.get("vendor.camera.aux.packagelist");
            if (packageList.length() > 0) {
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(packageList);
                for (String str : splitter) {
                    if (packageName.equals(str)) {
                        exposeMonoCamera = true;
                        break;
                    }
                }
            }
            if (!exposeMonoCamera && Integer.parseInt(id) >= 2) {
                Log.w(TAG, "[soar.cts] ignore the status update of camera: " + id);
            } else if (validStatus(status)) {
                Integer oldStatus = (Integer) this.mDeviceStatus.put(id, Integer.valueOf(status));
                if (oldStatus == null || oldStatus.intValue() != status) {
                    if (oldStatus != null) {
                        if (isAvailable(status) == isAvailable(oldStatus.intValue())) {
                            return;
                        }
                    }
                    int callbackCount = this.mCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleUpdate((AvailabilityCallback) this.mCallbackMap.keyAt(i), (Handler) this.mCallbackMap.valueAt(i), id, status);
                    }
                }
            } else {
                Log.e(TAG, String.format("Ignoring invalid device %s status 0x%x", new Object[]{id, Integer.valueOf(status)}));
            }
        }

        private void updateTorchCallbackLocked(TorchCallback callback, Handler handler) {
            for (int i = 0; i < this.mTorchStatus.size(); i++) {
                postSingleTorchUpdate(callback, handler, (String) this.mTorchStatus.keyAt(i), ((Integer) this.mTorchStatus.valueAt(i)).intValue());
            }
        }

        private void onTorchStatusChangedLocked(int status, String id) {
            boolean exposeMonoCamera = false;
            String packageName = ActivityThread.currentOpPackageName();
            String packageList = SystemProperties.get("vendor.camera.aux.packagelist");
            if (packageList.length() > 0) {
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(packageList);
                for (String str : splitter) {
                    if (packageName.equals(str)) {
                        exposeMonoCamera = true;
                        break;
                    }
                }
            }
            if (!exposeMonoCamera && Integer.parseInt(id) >= 2) {
                Log.w(TAG, "ignore the torch status update of camera: " + id);
            } else if (validTorchStatus(status)) {
                Integer oldStatus = (Integer) this.mTorchStatus.put(id, Integer.valueOf(status));
                if (oldStatus == null || oldStatus.intValue() != status) {
                    int callbackCount = this.mTorchCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleTorchUpdate((TorchCallback) this.mTorchCallbackMap.keyAt(i), (Handler) this.mTorchCallbackMap.valueAt(i), id, status);
                    }
                }
            } else {
                Log.e(TAG, String.format("Ignoring invalid device %s torch status 0x%x", new Object[]{id, Integer.valueOf(status)}));
            }
        }

        public void registerAvailabilityCallback(AvailabilityCallback callback, Handler handler) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (((Handler) this.mCallbackMap.put(callback, handler)) == null) {
                    updateCallbackLocked(callback, handler);
                }
                if (this.mCameraService == null) {
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
            synchronized (this.mLock) {
                this.mCallbackMap.remove(callback);
            }
        }

        public void registerTorchCallback(TorchCallback callback, Handler handler) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (((Handler) this.mTorchCallbackMap.put(callback, handler)) == null) {
                    updateTorchCallbackLocked(callback, handler);
                }
                if (this.mCameraService == null) {
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        public void unregisterTorchCallback(TorchCallback callback) {
            synchronized (this.mLock) {
                this.mTorchCallbackMap.remove(callback);
            }
        }

        public void onStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onStatusChangedLocked(status, cameraId);
            }
        }

        public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onTorchStatusChangedLocked(status, cameraId);
            }
        }

        private void scheduleCameraServiceReconnectionLocked() {
            Handler handler;
            if (this.mCallbackMap.size() > 0) {
                handler = (Handler) this.mCallbackMap.valueAt(0);
            } else if (this.mTorchCallbackMap.size() > 0) {
                handler = (Handler) this.mTorchCallbackMap.valueAt(0);
            } else {
                return;
            }
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (CameraManagerGlobal.this.getCameraService() == null) {
                        synchronized (CameraManagerGlobal.this.mLock) {
                            CameraManagerGlobal.this.scheduleCameraServiceReconnectionLocked();
                        }
                    }
                }
            }, 1000);
        }

        public void binderDied() {
            synchronized (this.mLock) {
                if (this.mCameraService == null) {
                    return;
                }
                int i;
                this.mCameraService = null;
                for (i = 0; i < this.mDeviceStatus.size(); i++) {
                    onStatusChangedLocked(0, (String) this.mDeviceStatus.keyAt(i));
                }
                for (i = 0; i < this.mTorchStatus.size(); i++) {
                    onTorchStatusChangedLocked(0, (String) this.mTorchStatus.keyAt(i));
                }
                scheduleCameraServiceReconnectionLocked();
            }
        }
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
        }
    }

    public static abstract class TorchCallback {
        public void onTorchModeUnavailable(String cameraId) {
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
        }
    }

    public CameraManager(Context context) {
        synchronized (this.mLock) {
            this.mContext = context;
        }
    }

    public String[] getCameraIdList() throws CameraAccessException {
        return CameraManagerGlobal.get().getCameraIdList();
    }

    public void registerAvailabilityCallback(AvailabilityCallback callback, Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException("No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        CameraManagerGlobal.get().registerAvailabilityCallback(callback, handler);
    }

    public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
        CameraManagerGlobal.get().unregisterAvailabilityCallback(callback);
    }

    public void registerTorchCallback(TorchCallback callback, Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException("No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        CameraManagerGlobal.get().registerTorchCallback(callback, handler);
    }

    public void unregisterTorchCallback(TorchCallback callback) {
        CameraManagerGlobal.get().unregisterTorchCallback(callback);
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = null;
        if (CameraManagerGlobal.sCameraServiceDisabled) {
            throw new IllegalArgumentException("No cameras available on device");
        }
        synchronized (this.mLock) {
            ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
            if (cameraService == null) {
                throw new CameraAccessException(2, "Camera service is currently unavailable");
            }
            try {
                if (supportsCamera2ApiLocked(cameraId)) {
                    characteristics = new CameraCharacteristics(cameraService.getCameraCharacteristics(cameraId));
                } else {
                    int id = Integer.parseInt(cameraId);
                    characteristics = LegacyMetadataMapper.createCharacteristics(cameraService.getLegacyParameters(id), cameraService.getCameraInfo(id));
                }
            } catch (ServiceSpecificException e) {
                throwAsPublicException(e);
            } catch (RemoteException e2) {
                throw new CameraAccessException(2, "Camera service is currently unavailable", e2);
            }
        }
        return characteristics;
    }

    private CameraDevice openCameraDeviceUserAsync(String cameraId, StateCallback callback, Handler handler, int uid) throws CameraAccessException {
        CameraDevice deviceImpl;
        CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
        synchronized (this.mLock) {
            ICameraDeviceUser cameraUser = null;
            deviceImpl = new CameraDeviceImpl(cameraId, callback, handler, characteristics, this.mContext.getApplicationInfo().targetSdkVersion);
            ICameraDeviceCallbacks callbacks = deviceImpl.getCallbacks();
            try {
                CameraDevice device;
                if (supportsCamera2ApiLocked(cameraId)) {
                    ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                    if (cameraService == null) {
                        throw new ServiceSpecificException(4, "Camera service is currently unavailable");
                    }
                    cameraUser = cameraService.connectDevice(callbacks, cameraId, this.mContext.getOpPackageName(), uid);
                    sendOpenCameraBroadCast();
                    sendStartPreviewBroadCast();
                    deviceImpl.setRemoteDevice(cameraUser);
                    deviceImpl.setIsNormalCamera(this.mIsNormalCamera);
                    device = deviceImpl;
                } else {
                    int id = Integer.parseInt(cameraId);
                    Log.i(TAG, "Using legacy camera HAL.");
                    cameraUser = CameraDeviceUserShim.connectBinderShim(callbacks, id);
                    sendOpenCameraBroadCast();
                    sendStartPreviewBroadCast();
                    deviceImpl.setRemoteDevice(cameraUser);
                    deviceImpl.setIsNormalCamera(this.mIsNormalCamera);
                    device = deviceImpl;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected cameraId to be numeric, but it was: " + cameraId);
            } catch (ServiceSpecificException e2) {
                if (e2.errorCode == 9) {
                    throw new AssertionError("Should've gone down the shim path");
                } else if (e2.errorCode == 7 || e2.errorCode == 8 || e2.errorCode == 6 || e2.errorCode == 4 || e2.errorCode == 10) {
                    deviceImpl.setRemoteFailure(e2);
                    if (e2.errorCode == 6 || e2.errorCode == 4 || e2.errorCode == 7) {
                        throwAsPublicException(e2);
                    }
                } else {
                    throwAsPublicException(e2);
                }
            } catch (RemoteException e3) {
                ServiceSpecificException serviceSpecificException = new ServiceSpecificException(4, "Camera service is currently unavailable");
                deviceImpl.setRemoteFailure(serviceSpecificException);
                throwAsPublicException(serviceSpecificException);
            }
        }
        return deviceImpl;
    }

    private int JudgeSettingAndShowMessage() {
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        if (GetSettingOfCamera(context) != 0) {
            return 0;
        }
        ShowToast(context);
        return 1;
    }

    private int GetSettingOfCamera(Context context) {
        if (!"0".equals(SystemProperties.get("ro.build.gn.support", "0"))) {
            return Secure.getInt(context.getContentResolver(), "ct_peripheral_camera", 1);
        }
        Log.e(TAG, "SCT1613 gn_support: the property is disable");
        return 1;
    }

    private void ShowToast(final Context context) {
        final String msg = context.getString(51249705);
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(looper);
            } else {
                this.mEventHandler = null;
            }
        }
        if (this.mEventHandler != null) {
            this.mEventHandler.post(new Runnable() {
                public void run() {
                    Toast contentstr = Toast.makeText(context, msg, 0);
                    Log.d(CameraManager.TAG, "showToast " + msg);
                    LayoutParams windowParams = contentstr.getWindowParams();
                    windowParams.flags |= 524288;
                    contentstr.show();
                }
            });
        }
    }

    private static boolean checkCTPropertyAndPermissionAndCert() {
        if ("0".equals(SystemProperties.get("ro.build.gn.support", "0"))) {
            return false;
        }
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        PackageManager mPackageManager = context.getPackageManager();
        boolean permissionPid = context.checkCallingOrSelfPermission("com.chinatelecom.permission.security.EMM") == 0;
        if (!permissionPid) {
            Log.e(TAG, "checkCTPropertyAndPermissionAndCert_permissoin = " + permissionPid);
        }
        return permissionPid;
    }

    private void sendOpenCameraBroadCast() {
        String ACTION_OPEN_CAMERA = "com.android.camera.ACTION_OPEN_CAMERA";
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        Intent intent = new Intent(ACTION_OPEN_CAMERA);
        String packageName = ActivityThread.currentPackageName();
        if (isNormalCamera(packageName)) {
            this.mIsNormalCamera = "true";
        } else {
            this.mIsNormalCamera = "false";
        }
        Log.v(TAG, "sendOpenCameraBroadCast isNormalCamera = " + this.mIsNormalCamera);
        intent.putExtra("isNormalCamera", this.mIsNormalCamera);
        intent.putExtra(NetworkScoreManager.EXTRA_PACKAGE_NAME, packageName);
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    private void sendStartPreviewBroadCast() {
        String ACTION_START_PREVIEW = "com.android.camera.ACTION_START_PREVIEW";
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        Intent intent = new Intent(ACTION_START_PREVIEW);
        Log.v(TAG, "sendStartPreviewBroadCast");
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    private boolean isNormalCamera(String packageName) {
        Log.v(TAG, "isNormalCamera packageName = " + packageName);
        if (isTopActivity()) {
            Log.v(TAG, "isNormalCamera isTopActivity true");
            return true;
        }
        Log.v(TAG, "current package isTopActivity false ");
        return false;
    }

    public final boolean isBackgroundProcess() {
        Log.v(TAG, "isBackgroundProcess");
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        if (context != null) {
            for (RunningAppProcessInfo appProcess : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    Log.v(TAG, "isBackgroundProcess importance " + appProcess.importance);
                    if (appProcess.importance == 400 || appProcess.importance == 300) {
                        Log.v(TAG, "appProcess.processName " + appProcess.processName + " true");
                        return true;
                    }
                    Log.v(TAG, "appProcess.processName " + appProcess.processName + " false");
                    return false;
                }
            }
        }
        return false;
    }

    public final boolean isTopActivity() {
        Log.v(TAG, "isTopActivity");
        ActivityThread caller = ActivityThread.currentActivityThread();
        Context context = caller != null ? caller.getApplication() : null;
        if (context != null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
                if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
                    String cmpNameTemp = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getPackageName().toString();
                    Log.i(TAG, "isTopActivity: pkgname: " + cmpNameTemp);
                    if (cmpNameTemp.equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void openCamera(String cameraId, StateCallback callback, Handler handler) throws CameraAccessException {
        openCameraForUid(cameraId, callback, handler, -1);
    }

    public void openCameraForUid(String cameraId, StateCallback callback, Handler handler, int clientUid) throws CameraAccessException {
        if (cameraId == null) {
            throw new IllegalArgumentException("cameraId was null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback was null");
        } else {
            if (handler == null) {
                if (Looper.myLooper() != null) {
                    handler = new Handler();
                } else {
                    throw new IllegalArgumentException("Handler argument is null, but no looper exists in the calling thread");
                }
            }
            if (CameraManagerGlobal.sCameraServiceDisabled) {
                throw new IllegalArgumentException("No cameras available on device");
            } else if (JudgeSettingAndShowMessage() != 0) {
                throw new IllegalArgumentException("Custom define not support camera function");
            } else {
                openCameraDeviceUserAsync(cameraId, callback, handler, clientUid);
            }
        }
    }

    public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
        if (CameraManagerGlobal.sCameraServiceDisabled) {
            throw new IllegalArgumentException("No cameras available on device");
        }
        CameraManagerGlobal.get().setTorchMode(cameraId, enabled);
    }

    public static void throwAsPublicException(Throwable t) throws CameraAccessException {
        if (t instanceof ServiceSpecificException) {
            int reason;
            ServiceSpecificException e = (ServiceSpecificException) t;
            switch (e.errorCode) {
                case 1:
                    throw new SecurityException(e.getMessage(), e);
                case 2:
                case 3:
                    throw new IllegalArgumentException(e.getMessage(), e);
                case 4:
                    reason = 2;
                    break;
                case 6:
                    reason = 1;
                    break;
                case 7:
                    reason = 4;
                    break;
                case 8:
                    reason = 5;
                    break;
                case 9:
                    reason = 1000;
                    break;
                default:
                    reason = 3;
                    break;
            }
            throw new CameraAccessException(reason, e.getMessage(), e);
        } else if (t instanceof DeadObjectException) {
            throw new CameraAccessException(2, "Camera service has died unexpectedly", t);
        } else if (t instanceof RemoteException) {
            throw new UnsupportedOperationException("An unknown RemoteException was thrown which should never happen.", t);
        } else if (t instanceof RuntimeException) {
            throw ((RuntimeException) t);
        }
    }

    private boolean supportsCamera2ApiLocked(String cameraId) {
        return supportsCameraApiLocked(cameraId, 2);
    }

    private boolean supportsCameraApiLocked(String cameraId, int apiVersion) {
        try {
            ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
            if (cameraService == null) {
                return false;
            }
            return cameraService.supportsCameraApi(cameraId, apiVersion);
        } catch (RemoteException e) {
            return false;
        }
    }
}
