package vivo.contentcatcher;

import android.app.Activity;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import java.lang.reflect.Constructor;

public class AIEngineContentCatcherManagerFactory {
    private static final String CATCHER_MANAGER_CLASS_NAME = "com.vivo.aiengine.services.contentcatcher.ContentCatcherManager";
    private static final String CATCHER_MANAGER_PACKAGE_NAME = "com.vivo.aiengine";
    private static final String TAG = "AIEngineContentCatcherFactory";
    private static Class<IContentCatcherManager> mContentCatcherClz = null;
    private static volatile PackageInfo mPackageInfo = null;
    public static int versionCode = 1;

    static class DefaultContentCatcherManager implements IContentCatcherManager {
        DefaultContentCatcherManager() {
        }

        public void copyNode(Bundle bundle) {
        }

        public void processImageAndWebview(Bundle bundle) {
        }

        public void updateToken(int token, Activity activity) {
        }
    }

    private static PackageInfo getPackageInfo() {
        if (mPackageInfo != null) {
            return mPackageInfo;
        }
        Application app = AppGlobals.getInitialApplication();
        if (app == null) {
            return null;
        }
        PackageManager pm = app.getPackageManager();
        if (pm == null) {
            return null;
        }
        try {
            mPackageInfo = pm.getPackageInfo(CATCHER_MANAGER_PACKAGE_NAME, 128);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return mPackageInfo;
    }

    public static IContentCatcherManager createContentCatcherManager(int token, Activity activity) {
        IContentCatcherManager contentCatcherManager = null;
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            Class<IContentCatcherManager> cls = getContentCatcherManagerClass();
            if (cls != null) {
                Constructor constructor = cls.getConstructor(new Class[]{Integer.TYPE, Activity.class});
                constructor.setAccessible(true);
                contentCatcherManager = (IContentCatcherManager) constructor.newInstance(new Object[]{Integer.valueOf(token), activity});
            }
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(oldPolicy);
            throw th;
        }
        if (contentCatcherManager != null) {
            return contentCatcherManager;
        }
        Log.d(TAG, "get default contentcatcher manager");
        return new DefaultContentCatcherManager();
    }

    private static synchronized Class<IContentCatcherManager> getContentCatcherManagerClass() throws ClassNotFoundException {
        synchronized (AIEngineContentCatcherManagerFactory.class) {
            synchronized (AIEngineContentCatcherManagerFactory.class) {
                Class<IContentCatcherManager> cls;
                if (mContentCatcherClz != null) {
                    cls = mContentCatcherClz;
                    return cls;
                }
                PackageInfo packageInfo = getPackageInfo();
                if (packageInfo == null) {
                    Log.d(TAG, "package info is null");
                    return null;
                }
                try {
                    Application app = AppGlobals.getInitialApplication();
                    Context context = app.createPackageContext(packageInfo.packageName, 3);
                    app.getAssets().addAssetPath(context.getApplicationInfo().sourceDir);
                    mContentCatcherClz = Class.forName(CATCHER_MANAGER_CLASS_NAME, true, context.getClassLoader());
                    cls = mContentCatcherClz;
                    return cls;
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return null;
                }
            }
        }
    }
}
