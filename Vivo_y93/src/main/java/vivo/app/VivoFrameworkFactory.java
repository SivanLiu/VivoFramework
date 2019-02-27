package vivo.app;

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

public abstract class VivoFrameworkFactory {
    private static VivoFrameworkFactory factoryImpl;
    private static final Object mLock = new Object();

    public abstract AbsMotionManager getMotionManager();

    public abstract AbsNightPearlManager getNightPearlManager();

    public abstract AbsPopupCameraManager getPopupCameraManager();

    public abstract AbsTouchScreenManager getTouchScreenManager();

    public abstract AbsVivoBackupManager getVivoBackupManager();

    public abstract AbsVivoCommonManager getVivoCommonManager();

    public abstract AbsVivoPermissionManager getVivoPermissionManager();

    public abstract AbsVivoProxCaliManager getVivoProxCaliManager();

    public abstract AbsVivoSarPowerStateManager getVivoSarPowerStateManager();

    public abstract AbsVivoUserProfilingManager getVivoUserProfilingManager();

    public static VivoFrameworkFactory getFrameworkFactoryImpl() {
        if (factoryImpl != null) {
            return factoryImpl;
        }
        synchronized (mLock) {
            try {
                factoryImpl = (VivoFrameworkFactory) Class.forName("com.vivo.framework.FrameworkFactoryImpl").newInstance();
                if (factoryImpl != null) {
                    VivoFrameworkFactory vivoFrameworkFactory = factoryImpl;
                    return vivoFrameworkFactory;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
