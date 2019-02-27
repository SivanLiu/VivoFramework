package com.vivo.smartmultiwindow;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.vivo.smartmultiwindow.IVivoSmartMultiWindow.Stub;

public class IVivoSmartMultiWindowHelper {
    private static final String TAG = "IVivoSmartMultiWindowHelper";
    private static IVivoSmartMultiWindowHelper mIVivoSmartMultiWindowHelper;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            Log.e(IVivoSmartMultiWindowHelper.TAG, "VivoSmartMultiWindowException-onServiceDisconnected " + (name == null ? "null" : name.flattenToString()) + " , try to bind service again.");
            if (IVivoSmartMultiWindowHelper.this.mService != null) {
                IVivoSmartMultiWindowHelper.this.mService = null;
            }
            IVivoSmartMultiWindowHelper.this.mIsBindServer = false;
            if (IVivoSmartMultiWindowHelper.this.mContext != null) {
                try {
                    IVivoSmartMultiWindowHelper.this.bind();
                    return;
                } catch (Exception e) {
                    Log.e(IVivoSmartMultiWindowHelper.TAG, "VivoSmartMultiWindowException-onServiceDisconnected-e = " + e);
                    return;
                }
            }
            Log.e(IVivoSmartMultiWindowHelper.TAG, "VivoSmartMultiWindowException-onServiceDisconnected-mContext is NULL.");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(IVivoSmartMultiWindowHelper.TAG, " onServiceConnected is true ");
            IVivoSmartMultiWindowHelper.this.mService = Stub.asInterface(service);
            IVivoSmartMultiWindowHelper.this.mIsBindServer = true;
        }
    };
    private Context mContext;
    private boolean mIsBindServer;
    private IVivoSmartMultiWindow mService = null;

    public static IVivoSmartMultiWindowHelper getInstance(Context context) {
        if (mIVivoSmartMultiWindowHelper == null) {
            mIVivoSmartMultiWindowHelper = new IVivoSmartMultiWindowHelper(context);
        }
        return mIVivoSmartMultiWindowHelper;
    }

    public IVivoSmartMultiWindowHelper(Context context) {
        this.mContext = context;
    }

    public void bind() {
        Intent service = new Intent();
        service.setAction("com.vivo.smartmultiwindow.manager_smartmultiwindow_state");
        service.setPackage("com.vivo.smartmultiwindow");
        Log.d(TAG, "bind ");
        this.mContext.bindService(service, this.mConnection, 1);
    }

    public void unbind() {
        Log.d(TAG, "unbind mIsBindServer = " + this.mIsBindServer);
        if (this.mIsBindServer) {
            this.mIsBindServer = false;
            this.mContext.unbindService(this.mConnection);
        }
    }

    public boolean isBinded() {
        return this.mIsBindServer;
    }

    public void toggleSplitScreen() {
        try {
            Log.d(TAG, " setSmartMultiWindowEnable mService " + this.mService);
            if (this.mService != null) {
                this.mService.toggleSplitScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
