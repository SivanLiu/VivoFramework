package com.vivo.gamewatch.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import com.vivo.gamewatch.common.GameWatchManager;
import com.vivo.gamewatch.common.IGameWatch;
import java.io.File;

public class VGTServer implements DeathRecipient {
    public static final int GRAPHIC_FLAG_OPT = 2;
    public static final int GRAPHIC_FLAG_STATISTIC = 1;
    private static final String PROP_VGT_LIST = "sys.vivo.gw_vgt_list";
    private static final String READ_PREF = "read_pref";
    private static final int RESULT_FAIL = 0;
    private static final int RESULT_SUCESS = 1;
    private static final String SET_GRAPHIC_FLAGS = "set_graphic_flags";
    static final String TAG = "VGT";
    private static final int TRANSACTION_SET_BUNDBLE = 1;
    private static final String VGT_SERVER_REGISTER = "vgt_server_register";
    private static final String WRITE_PREF = "write_pref";
    private Context mAppContext;
    private int mFlags = 0;
    private IGameWatch mGameWatch;

    private class BBinder extends Binder {
        /* synthetic */ BBinder(VGTServer this$0, BBinder -this1) {
            this();
        }

        private BBinder() {
        }

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    String name = data.readString();
                    Bundle bundle = new Bundle();
                    bundle.readFromParcel(data);
                    Bundle result = VGTServer.this.setBundle(name, bundle);
                    if (result == null) {
                        result = new Bundle();
                    }
                    result.writeToParcel(reply, 0);
                    reply.writeNoException();
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    private static class Instance {
        private static final VGTServer INSTANCE = new VGTServer();

        private Instance() {
        }
    }

    public static VGTServer getInstance() {
        return Instance.INSTANCE;
    }

    public void initialize(Context appContext, String processName, ApplicationInfo appInfo) {
        if (!appInfo.isSystemApp() && (appInfo.packageName.equals(processName) ^ 1) == 0 && UserHandle.getAppId(appInfo.uid) >= 10000) {
            this.mAppContext = appContext;
            long time = SystemClock.uptimeMillis();
            boolean z = false;
            if (isRegister(appInfo.packageName) && this.mGameWatch == null) {
                IGameWatch service = GameWatchManager.getInstance().getService();
                this.mGameWatch = service;
                if (service != null) {
                    try {
                        z = isPrivPrefExist();
                        GameWatchManager.getInstance().execute(VGT_SERVER_REGISTER, appInfo.packageName, new BBinder(this, null), Boolean.valueOf(z));
                        this.mGameWatch.asBinder().linkToDeath(this, 0);
                    } catch (Throwable e) {
                        Log.e(TAG, "Error initialize VGTServer : " + e.getMessage());
                    }
                    Log.d(TAG, String.format("%s VGTServer initialize %s %d", new Object[]{processName, String.valueOf(z), Long.valueOf(SystemClock.uptimeMillis() - time)}));
                }
            }
        }
    }

    private boolean isRegister(String pkgName) {
        String value = SystemProperties.get(PROP_VGT_LIST, null);
        if (value != null) {
            SimpleStringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(value);
            for (String str : splitter) {
                if (pkgName.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPrivPrefExist() {
        return new File(this.mAppContext.getDataDir(), "shared_prefs").exists();
    }

    public Context getAppContext() {
        return this.mAppContext;
    }

    public Bundle setBundle(String name, Bundle bundle) {
        if (name.equals(SET_GRAPHIC_FLAGS)) {
            int flags = bundle.getInt("flags");
            int mask = bundle.getInt("mask");
            this.mFlags = (this.mFlags & (~mask)) | (flags & mask);
            VGTJni.setGraphicFlags(flags, mask);
            return returnIntResult(1);
        } else if (name.equals(WRITE_PREF)) {
            String fileName = bundle.getString("name");
            if (bundle.getInt("write_mode", 0) == 0) {
                VGTPrefHelper.put(fileName, bundle.getBundle("content"));
            } else {
                VGTPrefHelper.remove(fileName, bundle.getStringArrayList("content"));
            }
            return returnIntResult(1);
        } else if (name.equals(READ_PREF)) {
            return returnBundleResult(VGTPrefHelper.get(bundle.getString("name"), bundle.getStringArrayList("content")));
        } else {
            return returnIntResult(0);
        }
    }

    private Bundle returnIntResult(int code) {
        Bundle result = new Bundle();
        result.putInt(DocumentsContract.EXTRA_RESULT, code);
        return result;
    }

    private Bundle returnBundleResult(Bundle code) {
        Bundle result = new Bundle();
        result.putBundle(DocumentsContract.EXTRA_RESULT, code);
        return result;
    }

    public void binderDied() {
        if (this.mGameWatch != null) {
            this.mGameWatch.asBinder().unlinkToDeath(this, 0);
            this.mGameWatch = null;
        }
        if (this.mFlags != 0) {
            this.mFlags = 0;
            VGTJni.clearGraphicFlags();
        }
        Log.d(TAG, "GameWatch is died");
    }
}
