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
import android.view.MotionEvent;
import android.view.View;
import java.lang.reflect.Constructor;

public class ContentCatcherManagerFactory {
    private static final String CATCHER_MANAGER_CLASS_NAME = "com.vivo.contentcatcher.ContentCatcherManager";
    private static final String CATCHER_MANAGER_PACKAGE_NAME = "com.vivo.contentcatcher";
    private static final String CATCHER_UTIL_CLASS_NAME = "com.vivo.contentcatcher.ApplicationWatcher";
    private static final String TAG = "ContentCatcherFactory";
    private static volatile IApplicationWatcher mApplicationWatcher;
    private static Class<IApplicationWatcher> mApplicationWatcherClz = null;
    private static Class<IContentCatcherManager> mContentCatcherManagerClz = null;
    private static volatile PackageInfo mPackageInfo = null;
    public static int versionCode = 2;

    static class DefaultApplicationWatcher implements IApplicationWatcher {
        DefaultApplicationWatcher() {
        }

        public void onTouchEvent(View view, MotionEvent event) {
        }

        public void onLoad(Activity activity) {
        }
    }

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
        return new DefaultContentCatcherManager();
    }

    private static synchronized Class<IContentCatcherManager> getContentCatcherManagerClass() throws ClassNotFoundException {
        synchronized (ContentCatcherManagerFactory.class) {
            synchronized (ContentCatcherManagerFactory.class) {
                Class<IContentCatcherManager> cls;
                if (mContentCatcherManagerClz != null) {
                    cls = mContentCatcherManagerClz;
                    return cls;
                }
                PackageInfo packageInfo = getPackageInfo();
                if (packageInfo == null) {
                    return null;
                }
                Application app = AppGlobals.getInitialApplication();
                if (app == null) {
                    return null;
                }
                try {
                    Context context = app.createPackageContext(packageInfo.packageName, 3);
                    app.getAssets().addAssetPath(context.getApplicationInfo().sourceDir);
                    mContentCatcherManagerClz = Class.forName(CATCHER_MANAGER_CLASS_NAME, true, context.getClassLoader());
                    cls = mContentCatcherManagerClz;
                    return cls;
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return null;
                }
            }
        }
    }

    private static synchronized Class<IApplicationWatcher> getApplicationWatcherClass() throws ClassNotFoundException {
        synchronized (ContentCatcherManagerFactory.class) {
            synchronized (ContentCatcherManagerFactory.class) {
                Class<IApplicationWatcher> cls;
                if (mApplicationWatcherClz != null) {
                    cls = mApplicationWatcherClz;
                    return cls;
                }
                PackageInfo packageInfo = getPackageInfo();
                if (packageInfo == null) {
                    return null;
                }
                Application app = AppGlobals.getInitialApplication();
                if (app == null) {
                    return null;
                }
                try {
                    Context context = app.createPackageContext(packageInfo.packageName, 3);
                    app.getAssets().addAssetPath(context.getApplicationInfo().sourceDir);
                    mApplicationWatcherClz = Class.forName(CATCHER_UTIL_CLASS_NAME, true, context.getClassLoader());
                    cls = mApplicationWatcherClz;
                    return cls;
                } catch (Exception e) {
                    Log.e(TAG, "getApplicationWatcherClass", e);
                    return null;
                }
            }
        }
    }

    private static IApplicationWatcher createApplicationWatcher() {
        IApplicationWatcher applicationWatcher = null;
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            Class<IApplicationWatcher> cls = getApplicationWatcherClass();
            if (cls != null) {
                Constructor constructor = cls.getConstructor(new Class[0]);
                constructor.setAccessible(true);
                applicationWatcher = (IApplicationWatcher) constructor.newInstance(new Object[0]);
            }
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Exception e) {
            Log.e(TAG, "createApplicationWatcher", e);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(oldPolicy);
            throw th;
        }
        if (applicationWatcher != null) {
            return applicationWatcher;
        }
        Log.d(TAG, "use default application watcher");
        return new DefaultApplicationWatcher();
    }

    public static IApplicationWatcher getApplicationWatcher() {
        return mApplicationWatcher;
    }

    public static void setApplicationWatcher(IApplicationWatcher applicationWatcher) {
        mApplicationWatcher = applicationWatcher;
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
            Log.d(TAG, "package manager is null");
            return null;
        }
        try {
            mPackageInfo = pm.getPackageInfo(CATCHER_MANAGER_PACKAGE_NAME, 128);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return mPackageInfo;
    }
}
