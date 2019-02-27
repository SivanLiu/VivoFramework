package com.vivo.framework;

import com.vivo.framework.backup.VivoBackupManager;
import com.vivo.framework.common.VivoCommonManager;
import com.vivo.framework.motion.MotionManager;
import com.vivo.framework.nightpearl.NightPearlManager;
import com.vivo.framework.popupcamera.PopupCameraManager;
import com.vivo.framework.proxcali.VivoProxCaliManager;
import com.vivo.framework.sarpower.VivoSarPowerStateManager;
import com.vivo.framework.security.VivoPermissionManager;
import com.vivo.framework.touchscreen.TouchScreenManager;
import com.vivo.framework.userprofiling.VivoUserProfilingManager;
import vivo.app.VivoFrameworkFactory;
import vivo.app.backup.AbsVivoBackupManager;
import vivo.app.common.AbsVivoCommonManager;
import vivo.app.motion.AbsMotionManager;
import vivo.app.nightpearl.AbsNightPearlManager;
import vivo.app.popupcamera.AbsPopupCameraManager;
import vivo.app.proxcali.AbsVivoProxCaliManager;
import vivo.app.sarpower.AbsVivoSarPowerStateManager;
import vivo.app.security.AbsVivoPermissionManager;
import vivo.app.touchscreen.AbsTouchScreenManager;
import vivo.app.userprofiling.AbsVivoUserProfilingManager;

public class FrameworkFactoryImpl extends VivoFrameworkFactory {
    public AbsVivoCommonManager getVivoCommonManager() {
        return VivoCommonManager.getInstance();
    }

    public AbsNightPearlManager getNightPearlManager() {
        return NightPearlManager.getInstance();
    }

    public AbsVivoProxCaliManager getVivoProxCaliManager() {
        return VivoProxCaliManager.getInstance();
    }

    public AbsVivoSarPowerStateManager getVivoSarPowerStateManager() {
        return VivoSarPowerStateManager.getInstance();
    }

    public AbsTouchScreenManager getTouchScreenManager() {
        return TouchScreenManager.getInstance();
    }

    public AbsMotionManager getMotionManager() {
        return MotionManager.getInstance();
    }

    public AbsVivoPermissionManager getVivoPermissionManager() {
        return VivoPermissionManager.getInstance();
    }

    public AbsVivoUserProfilingManager getVivoUserProfilingManager() {
        return VivoUserProfilingManager.getInstance();
    }

    public AbsVivoBackupManager getVivoBackupManager() {
        return VivoBackupManager.getInstance();
    }

    public AbsPopupCameraManager getPopupCameraManager() {
        return PopupCameraManager.getInstance();
    }
}
