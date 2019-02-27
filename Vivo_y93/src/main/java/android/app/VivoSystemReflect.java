package android.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.animation.Interpolator;
import com.vivo.content.VivoConstants;
import java.lang.reflect.Constructor;
import vivo.app.VivoFrameworkFactory;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoSystemReflect {
    private static final String MSENSE_MANAGER_CLASS = "com.vivo.msense.MotionRecognitionManager";
    private static String PLATFORM_INFO = null;
    private static String PRODUCT_INFO = null;
    private static final String TAG = "VivoSystemReflect";
    private static final String VIVO_CUSTOM_SERVICE = "vivo_customized";
    private static final String VIVO_CUSTOM_SERVICE2 = "vivo_spec_customized";
    private static final String VIVO_CUSTOM_SERVICE_CLASS = "com.vivo.services.cust.server.VivoCustomService";
    private static final String VIVO_CUSTOM_SERVICE_CLASS2 = "com.vivo.services.cust.spec.VivoCustomSpecService";
    private static boolean VIVO_CUSTOM_SUPPORT = false;
    private Class<?> invokeClass = null;
    private Object invokeInstance = null;

    public Object Call(String cName, String MethodName, String[] types, String[] params) {
        Object retObject = null;
        try {
            Class<?> cls = Class.forName(cName);
            return cls.getMethod(MethodName, getMethodTypesClass(types)).invoke(cls.getConstructor(null).newInstance(null), getMethodParamObject(types, params));
        } catch (Exception e) {
            System.err.println(e);
            return retObject;
        }
    }

    public Class<?>[] getMethodTypesClass(String[] types) {
        Class<?>[] cs = new Class[types.length];
        int i = 0;
        while (i < cs.length) {
            if (types[i] != null || (types[i].trim().equals("") ^ 1) != 0) {
                if (types[i].equals("int") || types[i].equals("Integer")) {
                    cs[i] = Integer.TYPE;
                } else if (types[i].equals("float") || types[i].equals("Float")) {
                    cs[i] = Float.TYPE;
                } else if (types[i].equals("double") || types[i].equals("Double")) {
                    cs[i] = Double.TYPE;
                } else if (types[i].equals("boolean") || types[i].equals("Boolean")) {
                    cs[i] = Boolean.TYPE;
                } else {
                    cs[i] = String.class;
                }
            }
            i++;
        }
        return cs;
    }

    public Object[] getMethodParamObject(String[] types, String[] params) {
        Object[] retObjects = new Object[params.length];
        int i = 0;
        while (i < retObjects.length) {
            if (!params[i].trim().equals("") || params[i] != null) {
                if (types[i].equals("int") || types[i].equals("Integer")) {
                    retObjects[i] = new Integer(params[i]);
                } else if (types[i].equals("float") || types[i].equals("Float")) {
                    retObjects[i] = new Float(params[i]);
                } else if (types[i].equals("double") || types[i].equals("Double")) {
                    retObjects[i] = new Double(params[i]);
                } else if (types[i].equals("boolean") || types[i].equals("Boolean")) {
                    retObjects[i] = new Boolean(params[i]);
                } else {
                    retObjects[i] = params[i];
                }
            }
            i++;
        }
        return retObjects;
    }

    public void ReflectTest() {
        try {
            if (this.invokeClass == null || this.invokeInstance == null) {
                this.invokeClass = Class.forName(MSENSE_MANAGER_CLASS);
                this.invokeInstance = this.invokeClass.newInstance();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        try {
            this.invokeClass.getMethod("getMimeTypeForFile", new Class[]{String.class});
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
        } catch (ClassCastException e6) {
            e6.printStackTrace();
        }
    }

    public static void addVivoCustomService(Context context, Handler uiHandler) {
        boolean z = true;
        if (Integer.parseInt(SystemProperties.get("ro.build.gn.support", "0")) <= 0) {
            z = false;
        }
        VIVO_CUSTOM_SUPPORT = z;
        Slog.d(TAG, "VivoSystemReflect:addVivoCustomService; VIVO_CUSTOM_SUPPORT = " + VIVO_CUSTOM_SUPPORT);
        if (VIVO_CUSTOM_SUPPORT) {
            try {
                Slog.i(TAG, VIVO_CUSTOM_SERVICE_CLASS);
                ServiceManager.addService(VIVO_CUSTOM_SERVICE, (IBinder) Class.forName(VIVO_CUSTOM_SERVICE_CLASS).getMethod("main", new Class[]{Context.class, Handler.class}).invoke(null, new Object[]{context, uiHandler}));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting VivoCustomService", e);
            }
        }
    }

    public static void addBinderService(Context context, Handler uiHandler) {
        Slog.d(TAG, "VivoSystemReflect.addBinderService begin");
        init();
        String str;
        if (VivoConstants.MTK_PLATFORM.equals(PLATFORM_INFO)) {
            try {
                Slog.i(TAG, "VivoMainService Service");
                str = "vivo_main_service";
                ServiceManager.addService(str, (IBinder) getServiceConstructor("com.vivo.services.vivomain.VivoMainService")[1].newInstance(new Object[]{context}));
                String readCofig = SystemProperties.get("persist.sys.usb.config");
                ContentResolver cr = context.getContentResolver();
                if (readCofig.contains(UsbManager.USB_FUNCTION_ADB)) {
                    Secure.putInt(cr, "adb_enabled", 1);
                }
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting VivoMainService", e);
            }
        } else if (VivoConstants.QCOM_PLATFORM.equals(PLATFORM_INFO)) {
            try {
                Slog.i(TAG, "SensorLogService Service");
                str = "sensor_log";
                ServiceManager.addService(str, (IBinder) getServiceConstructor("com.vivo.services.sensorlog.SensorLogService")[1].newInstance(new Object[]{context}));
            } catch (Throwable e2) {
                Slog.e(TAG, "Failure starting SensorLogService", e2);
            }
        }
        if (VIVO_CUSTOM_SUPPORT) {
            try {
                Slog.i(TAG, "VivoCustomService initcom.vivo.services.cust.server.VivoCustomService");
                IBinder iBinder = (IBinder) Class.forName(VIVO_CUSTOM_SERVICE_CLASS).getMethod("init", new Class[0]).invoke(null, new Object[0]);
            } catch (Throwable e22) {
                Slog.e(TAG, "Failure starting VivoCustomService", e22);
            }
            try {
                Slog.i(TAG, VIVO_CUSTOM_SERVICE_CLASS2);
                ServiceManager.addService(VIVO_CUSTOM_SERVICE2, (IBinder) getServiceConstructor(VIVO_CUSTOM_SERVICE_CLASS2)[1].newInstance(new Object[]{context, uiHandler}));
            } catch (Throwable e222) {
                Slog.e(TAG, "Failure starting VivoCustomService2", e222);
            }
        }
        Slog.d(TAG, "VivoSystemReflect.addBinderService end");
    }

    public static void registerMangerService() {
        SystemServiceRegistry.registerSystemService("night_pearl_manager", null, new CachedServiceFetcher<Object>() {
            public Object createService(ContextImpl ctx) {
                if (VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
                    return VivoFrameworkFactory.getFrameworkFactoryImpl().getNightPearlManager();
                }
                Slog.e(VivoSystemReflect.TAG, "VivoFrameworkFactory.getFrameworkFactoryImpl() is null!");
                return null;
            }
        });
        SystemServiceRegistry.registerSystemService("motion_recongnition", null, new CachedServiceFetcher<Object>() {
            public Object createService(ContextImpl ctx) {
                try {
                    return VivoSystemReflect.getServiceConstructor("com.vivo.services.motion.MotionRecognitionManager")[1].newInstance(new Object[]{ctx.mMainThread.getHandler().getLooper()});
                } catch (Throwable e) {
                    Slog.e(VivoSystemReflect.TAG, "Failure register MotionRecognitionManager", e);
                    return null;
                }
            }
        });
        SystemServiceRegistry.registerSystemService("device_para_provide_service", null, new CachedServiceFetcher<Object>() {
            public Object createService(ContextImpl ctx) {
                try {
                    return VivoSystemReflect.getServiceConstructor("com.vivo.services.DeviceParaProvideService")[0].newInstance(new Object[0]);
                } catch (Throwable e) {
                    Slog.e(VivoSystemReflect.TAG, "Failure register DeviceParaProvideService", e);
                    return null;
                }
            }
        });
        Slog.i(TAG, "add vivoBackupManager");
        SystemServiceRegistry.registerSystemService("vivo_backup_service", null, new CachedServiceFetcher<Object>() {
            public Object createService(ContextImpl ctx) {
                if (VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
                    return VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoBackupManager();
                }
                Slog.e(VivoSystemReflect.TAG, "VivoFrameworkFactory.getFrameworkFactoryImpl() is null!");
                return null;
            }
        });
    }

    private static Constructor<?>[] getServiceConstructor(String className) throws ClassNotFoundException {
        return Class.forName(className).getConstructors();
    }

    private static void init() {
        PLATFORM_INFO = SystemProperties.get(VivoConstants.PLATFORM_TAG);
        PRODUCT_INFO = SystemProperties.get(VivoConstants.PRODUCT_TAG);
        Slog.d(TAG, "VivoSystemReflect.init PLATFORM_INFO == " + PLATFORM_INFO + ";PRODUCT_INFO == " + PRODUCT_INFO);
    }

    public static Interpolator getDialogInterpolator(Context c, AttributeSet attrs) {
        Interpolator result = null;
        try {
            return (Interpolator) Class.forName("com.vivo.common.animation.CustomBounceInterpolator").getConstructors()[1].newInstance(new Object[]{c, attrs});
        } catch (Throwable th) {
            Slog.d(TAG, "get the CustomBounceInterpolator instance failed!");
            return result;
        }
    }

    public static void loadLibrary(String libName) {
        Slog.d(TAG, "start to load lib " + libName);
        try {
            System.loadLibrary(libName);
        } catch (Exception e) {
            Slog.d(TAG, e.getMessage());
        }
    }
}
