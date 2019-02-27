package com.android.internal.app;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.util.DoubleAppSwitcher;
import android.util.Log;
import android.view.View;
import com.android.internal.app.AdapterDecker.WrapperAdapter;
import com.android.internal.app.ResolverActivity.DisplayResolveInfo;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverActivity.TargetInfo;
import java.util.ArrayList;
import java.util.List;

public class DoubleInstanceAdapterDecker extends AbsAdapterDecker {
    private static final int CFG_DOUBLE_INSTANCE_PACKAGE_NAME = 1;
    private static final boolean DEBUG = true;
    private static final String TAG = "DoubleInstanceAdapterDecker";
    private WrapperAdapter mAdapter = null;
    private int mCallingUid = -10000;
    private List<String> mDoubleAppPackages = null;
    private List<String> mDoubleInstancePackages = null;
    private int mDoubleInstanceUserId = -10000;
    private boolean mDoubleInstanceValid = false;
    private ResolverActivity mResolver = null;
    private Intent mStartIntent = null;

    public DoubleInstanceAdapterDecker(AdapterDecker adapter) {
        super(adapter);
    }

    public DoubleInstanceAdapterDecker(AdapterDecker adapter, int uid) {
        super(adapter);
        this.mCallingUid = uid;
        try {
            this.mDoubleInstancePackages = ActivityManagerNative.getDefault().getDoubleInstanceConfig(1);
        } catch (RemoteException e) {
            this.mDoubleInstancePackages = null;
            Log.e(TAG, "obtain DoubleInstance Pakcages failed", e);
        }
    }

    protected void onAfterCreate(ResolverActivity activity, Intent intent) {
        this.mResolver = activity;
        this.mStartIntent = intent;
    }

    public void onPrebuildList(List<ResolvedComponentInfo> list) {
        this.mDoubleAppPackages = new ArrayList();
        if (this.mDoubleInstancePackages == null || this.mDoubleInstancePackages.size() <= 0) {
            Log.i(TAG, "Ignore DoubleInstance for Intent : " + this.mStartIntent);
            return;
        }
        PackageManager pm = this.mResolver.getPackageManager();
        UserManager userManager = (UserManager) this.mResolverContext.getSystemService("user");
        this.mDoubleInstanceValid = true;
        this.mDoubleInstanceUserId = userManager.getDoubleAppUserId();
        IPackageManager ipm = Stub.asInterface(ServiceManager.getService("package"));
        for (int i = this.mDoubleInstancePackages.size() - 1; i >= 0; i--) {
            String pkg = (String) this.mDoubleInstancePackages.get(i);
            try {
                if (ipm.isPackageAvailable(pkg, this.mDoubleInstanceUserId)) {
                    this.mDoubleAppPackages.add(pkg);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "isPackageAvailable for DoubleInstance failed", e);
            }
        }
        Log.d(TAG, "supported DoubleInstance Pakcages : " + this.mDoubleAppPackages);
    }

    protected void onBuildList(WrapperAdapter adapter) {
        this.mAdapter = adapter;
        List actionWithoutChooser = null;
        try {
            actionWithoutChooser = ActivityManagerNative.getDefault().getDoubleInstanceConfig(13);
        } catch (Exception e) {
            Log.e(TAG, "onBuildList getDoubleInstanceConfig 13 failed :" + e);
        }
        if (this.mDoubleAppPackages == null || this.mDoubleAppPackages.size() <= 0 || (actionWithoutChooser != null && actionWithoutChooser.contains(this.mStartIntent.getAction()))) {
            Log.d(TAG, "onBuildList--No need to generate DoubleInstanceDisplayInfo, just return");
            return;
        }
        PackageManager pm = this.mResolver.getPackageManager();
        for (int i = this.mAdapter.getItemCount() - 1; i >= 0; i--) {
            TargetInfo target = this.mAdapter.getItem(i);
            if (target != null) {
                ResolveInfo info = target.getResolveInfo();
                String pkg = info.activityInfo.packageName;
                for (int j = this.mDoubleAppPackages.size() - 1; j >= 0; j--) {
                    if (pkg.equals((String) this.mDoubleAppPackages.get(j))) {
                        this.mAdapter.insertItem(i + 1, generateDoubleInstanceDisplayInfo(pm, info));
                        break;
                    }
                }
            }
        }
    }

    private DisplayResolveInfo generateDoubleInstanceDisplayInfo(PackageManager pm, ResolveInfo info) {
        ResolveInfo ri = new ResolveInfo(info);
        ri.targetUserId = this.mDoubleInstanceUserId;
        Log.d(TAG, "generate DoubleInstance DisplayResovleInfo for : " + info);
        ResolverActivity resolverActivity = this.mResolverContext;
        resolverActivity.getClass();
        return new DisplayResolveInfo(this.mStartIntent, ri, ri.loadLabel(pm), null, this.mStartIntent);
    }

    protected boolean OnItemClick(View view, int position) {
        if (!this.mDoubleInstanceValid || this.mAdapter == null) {
            return false;
        }
        TargetInfo target = this.mAdapter.getItem(position);
        if (!(target == null || target.getResolveInfo() == null)) {
            ResolveInfo resolve = target.getResolveInfo();
            Intent intent = target.getResolvedIntent();
            if (resolve.targetUserId == this.mDoubleInstanceUserId) {
                if (!ResolverAdapterImpl.startActivityByKeyguard(this.mResolverContext, intent, this.mDoubleInstanceUserId)) {
                    StrictMode.disableDeathOnFileUriExposure();
                    if (DoubleAppSwitcher.sEnabled) {
                        boolean flag = false;
                        if (intent.getExtras() != null && intent.getExtras().containsKey("android.intent.extra.STREAM")) {
                            Uri uri = (Uri) intent.getExtras().getParcelable("android.intent.extra.STREAM");
                            if (uri != null && ContactsContract.AUTHORITY.equals(uri.getAuthority())) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            intent.fixUris(UserHandle.getUserId(this.mCallingUid));
                        }
                        this.mResolver.startActivityAsUser(intent, null, new UserHandle(this.mDoubleInstanceUserId));
                    } else {
                        target.startAsUser(this.mResolverContext, null, new UserHandle(this.mDoubleInstanceUserId));
                    }
                    StrictMode.enableDeathOnFileUriExposure();
                }
                this.mResolverContext.onActivityStarted(target);
                this.mResolverContext.dismiss();
                Log.d(TAG, "Start DoubleInstance : " + resolve + " As User " + resolve.targetUserId);
                return true;
            }
        }
        return false;
    }
}
