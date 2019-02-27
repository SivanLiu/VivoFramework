package com.vivo.services;

import android.content.Context;
import android.os.Handler;
import com.vivo.framework.facedetect.IFaceDetectManager;
import com.vivo.framework.vivo4dgamevibrator.IVivo4DGameVibratorService;
import com.vivo.service.VivoServiceFactory;
import com.vivo.services.backup.VivoBackupManagerService;
import com.vivo.services.common.VivoCommonService;
import com.vivo.services.engineerutile.BBKEngineerUtileService;
import com.vivo.services.epm.ExceptionPolicyManagerService;
import com.vivo.services.facedetect.FaceDetectService;
import com.vivo.services.motion.MotionManagerService;
import com.vivo.services.popupcamera.PopupCameraManagerService;
import com.vivo.services.proxcali.VivoProxCaliService;
import com.vivo.services.sarpower.VivoSarPowerStateService;
import com.vivo.services.security.server.VivoPermissionService;
import com.vivo.services.touchscreen.TouchScreenService;
import com.vivo.services.userprofiling.VivoUserProfilingService;
import com.vivo.services.vivo4dgamevibrator.Vivo4DGameVibratorService;
import vivo.app.backup.IVivoBackupManager;
import vivo.app.common.IVivoCommon.Stub;
import vivo.app.engineerutile.IBBKEngineerUtileService;
import vivo.app.epm.IExceptionPolicyManager;
import vivo.app.motion.IMotionManager;
import vivo.app.popupcamera.IPopupCameraManager;
import vivo.app.proxcali.IVivoProxCali;
import vivo.app.sarpower.IVivoSarPowerState;
import vivo.app.security.IVivoPermissionService;
import vivo.app.touchscreen.ITouchScreen;
import vivo.app.userprofiling.IVivoUserProfiling;

public class ServiceFactoryImpl extends VivoServiceFactory {
    public Stub getVivoCommonService(Context context) {
        return VivoCommonService.getInstance(context);
    }

    public IVivoProxCali.Stub getVivoProxCaliService(Context context) {
        return new VivoProxCaliService(context);
    }

    public IVivoSarPowerState.Stub getVivoSarPowerStateService(Context context) {
        return new VivoSarPowerStateService(context);
    }

    public ITouchScreen.Stub getTouchScreenService(Context context) {
        return new TouchScreenService(context);
    }

    public IMotionManager.Stub getMotionManagerService(Context context) {
        return new MotionManagerService(context);
    }

    public IVivoPermissionService.Stub getVivoPermissionService(Context context, Handler uiHandler) {
        return new VivoPermissionService(context, uiHandler);
    }

    public IVivoUserProfiling.Stub getVivoUserProfilingService(Context context) {
        return new VivoUserProfilingService(context);
    }

    public IBBKEngineerUtileService.Stub getBBKEngineerUtileService(Context context) {
        return new BBKEngineerUtileService(context);
    }

    public IFaceDetectManager.Stub getFaceDetectService(Context context) {
        return FaceDetectService.getInstance(context);
    }

    public IVivoBackupManager.Stub getVivoBackupService(Context context) {
        return new VivoBackupManagerService(context);
    }

    public IExceptionPolicyManager.Stub getExceptionPolicyManagerService(Context context) {
        return ExceptionPolicyManagerService.getInstance(context);
    }

    public IPopupCameraManager.Stub getPopupCameraManagerService(Context context) {
        return PopupCameraManagerService.getInstance(context);
    }

    public IVivo4DGameVibratorService.Stub getVivo4DGameVibratorService(Context context) {
        return new Vivo4DGameVibratorService(context);
    }
}
