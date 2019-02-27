package com.android.internal.app;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.FtBuild;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.util.DoubleAppSwitcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.internal.app.AdapterDecker.WrapperAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.internal.app.ResolverHelper.SharedPackageInfo;
import com.vivo.common.VivoCollectData;
import com.vivo.content.ImageUtil;
import com.vivo.internal.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShareGridSortAdapterDecker extends AbsAdapterDecker {
    private static final String ACTION_SHARE_LIST_SORT = "com.android.action.CHOOSER_GRID_SORT";
    private static final String ALLOW_SHOW_TIPS = "1";
    private static final int CFG_DOUBLE_INSTANCE_PACKAGE_NAME = 1;
    private static final boolean DEBUG = true;
    private static final String SHOWTIPS = "resolveractivity_slide_showtips";
    private static final String TAG = "ShareGridSortAdapterDecker";
    private WrapperAdapter mAdapter = null;
    private int mCallingUid = -10000;
    private Handler mConfigFileHandler = null;
    private boolean mNeedRecycle = false;
    private boolean mNeedSortItems = false;
    private ResolverActivity mResolver = null;
    private Drawable mSettingDrawable = null;
    private List<SharedPackageInfo> mSharedItemInfo = new ArrayList();
    private boolean mShouldAddSortIcon = false;
    private boolean mShouldSortItems = false;
    private Intent mStartIntent = null;
    private int mTargetPosition = -1;
    private ImageView shareSortIcon;

    public ShareGridSortAdapterDecker(AdapterDecker adapter) {
        super(adapter);
    }

    public ShareGridSortAdapterDecker(AdapterDecker adapter, int uid) {
        super(adapter);
        this.mCallingUid = uid;
    }

    protected void onAfterCreate(ResolverActivity activity, Intent intent) {
        this.mResolver = activity;
        this.mStartIntent = intent;
        this.shareSortIcon = (ImageView) this.mResolver.findViewById(R.id.title_order);
        if (FtBuild.getRomVersion() < 4.5f) {
            this.shareSortIcon.setVisibility(4);
        }
    }

    protected void onPrebuildList(List<ResolvedComponentInfo> components) {
        boolean shouldSortItems = shouldSortItems(components);
        this.mShouldAddSortIcon = shouldSortItems;
        this.mShouldSortItems = shouldSortItems;
        if (this.mShouldSortItems) {
            this.mSharedItemInfo = ResolverHelper.readShareSortConfigFile();
            if (this.mSharedItemInfo.size() <= 0) {
                this.mShouldSortItems = false;
                loadDefaultShareSortData();
            }
        }
    }

    protected void onBuildList(WrapperAdapter adapter) {
        this.mAdapter = adapter;
        if (this.mShouldSortItems) {
            sortShareItem();
        }
        if (!this.mShouldAddSortIcon) {
            this.shareSortIcon.setVisibility(4);
        } else if (FtBuild.getRomVersion() < 4.5f) {
            this.mAdapter.insertItem(this.mAdapter.getItemCount(), null);
            this.mTargetPosition = this.mAdapter.getItemCount() - 1;
        } else {
            this.shareSortIcon.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    System.putString(ShareGridSortAdapterDecker.this.mResolverContext.getContentResolver(), ShareGridSortAdapterDecker.SHOWTIPS, ShareGridSortAdapterDecker.ALLOW_SHOW_TIPS);
                    final VivoCollectData mCollectData = VivoCollectData.getInstance(ShareGridSortAdapterDecker.this.mResolverContext);
                    new Thread(new Runnable() {
                        public void run() {
                            if (mCollectData.getControlInfo("232")) {
                                HashMap<String, String> params = new HashMap();
                                params.put("order_num", ShareGridSortAdapterDecker.ALLOW_SHOW_TIPS);
                                long time = System.currentTimeMillis();
                                mCollectData.writeData("232", "2321", time, time, 0, 1, params);
                            }
                        }
                    }).start();
                    ShareGridSortAdapterDecker.this.OnOrderItemClick();
                }
            });
        }
    }

    private boolean shouldSortItems(List<ResolvedComponentInfo> components) {
        String action = this.mStartIntent == null ? null : this.mStartIntent.getAction();
        if ("android.intent.action.SEND".equals(action) || ("android.intent.action.SENDTO".equals(action) ^ 1) == 0 || ("android.intent.action.SEND_MULTIPLE".equals(action) ^ 1) == 0) {
            int count = components.size();
            if (count == 2) {
                ResolveInfo r1 = ((ResolvedComponentInfo) components.get(0)).getResolveInfoAt(0);
                ResolveInfo r2 = ((ResolvedComponentInfo) components.get(1)).getResolveInfoAt(0);
                if (!(r1 == null || r2 == null)) {
                    ActivityInfo ai1 = r1.activityInfo;
                    ActivityInfo ai2 = r2.activityInfo;
                    int equals = (ai1.packageName == null || !ai1.packageName.equals(ai2.packageName)) ? 0 : ai1.name != null ? ai1.name.equals(ai2.name) : 0;
                    return equals ^ 1;
                }
            } else if (count < 2) {
                return false;
            }
            return true;
        }
        Log.d(TAG, "ingore sorting item for non-share list : " + action);
        return false;
    }

    private void loadDefaultShareSortData() {
        Log.d(TAG, "loadDefaultShareSortData");
        HandlerThread hThread = new HandlerThread(TAG);
        hThread.start();
        this.mConfigFileHandler = new Handler(hThread.getLooper());
        this.mConfigFileHandler.post(new Runnable() {
            public void run() {
                ResolverHelper.writeShareSortConfFile(ShareGridSortAdapterDecker.this.readDefaultShareSortData());
            }
        });
    }

    protected void onDestroy() {
        if (this.mConfigFileHandler != null) {
            this.mConfigFileHandler.getLooper().quit();
        }
        if (this.mNeedRecycle) {
            ((BitmapDrawable) this.mSettingDrawable).getBitmap().recycle();
            this.mNeedRecycle = false;
        }
    }

    private Intent[] obtainShareIntent() {
        new Intent("android.intent.action.SEND").setType("*/*");
        return new Intent[]{intent};
    }

    private List<SharedPackageInfo> readDefaultShareSortData() {
        int i;
        int j;
        Intent[] intents = obtainShareIntent();
        List<ResolveInfo> resolveInfos = new ArrayList();
        PackageManager pm = this.mResolverContext.getPackageManager();
        for (Intent intent : intents) {
            resolveInfos.addAll(pm.queryIntentActivities(intent, 65536));
        }
        List<ResolvedComponentInfo> componentInfos = new ArrayList();
        for (ResolveInfo info : resolveInfos) {
            ActivityInfo ai = info.activityInfo;
            componentInfos.add(new ResolvedComponentInfo(new ComponentName(ai.packageName, ai.name), null, info));
        }
        List<SharedPackageInfo> result = new ArrayList();
        List<String> fixedItems = ResolverAdapterImpl.mFixedSequenceItems;
        for (i = fixedItems.size() - 1; i >= 0; i--) {
            String pkg = (String) fixedItems.get(i);
            int index = -1;
            for (j = result.size() - 1; j >= 0; j--) {
                if (pkg.equals(((SharedPackageInfo) result.get(j)).ri.activityInfo.packageName)) {
                    index = j;
                    break;
                }
            }
            if (index >= 0) {
                result.add(0, (SharedPackageInfo) result.get(index));
                result.remove(index + 1);
            }
        }
        try {
            List<String> doublePackages = ActivityManagerNative.getDefault().getDoubleInstanceConfig(1);
            int doubleUserId = ((UserManager) this.mResolverContext.getSystemService("user")).getDoubleAppUserId();
            for (i = result.size() - 1; i >= 0; i--) {
                SharedPackageInfo baseInfo = (SharedPackageInfo) result.get(i);
                for (j = doublePackages.size() - 1; j >= 0; j--) {
                    if (baseInfo.packageName.equals(doublePackages.get(j))) {
                        SharedPackageInfo newInfo = new SharedPackageInfo();
                        newInfo.packageName = baseInfo.packageName;
                        newInfo.name = baseInfo.name;
                        ResolveInfo resolveInfo = new ResolveInfo(baseInfo.ri);
                        resolveInfo.targetUserId = doubleUserId;
                        newInfo.ri = resolveInfo;
                        newInfo.setType(this.mResolverContext);
                        result.add(i + 1, newInfo);
                        break;
                    }
                }
            }
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "dDefaultShareSortData obtain doubleInstance Packages failed");
            return new ArrayList();
        }
    }

    private void sortShareItem() {
        for (int j = this.mSharedItemInfo.size() - 1; j >= 0; j--) {
            SharedPackageInfo info = (SharedPackageInfo) this.mSharedItemInfo.get(j);
            int index = -1;
            for (int i = this.mAdapter.getItemCount() - 1; i >= 0; i--) {
                TargetInfo target = this.mAdapter.getItem(i);
                if (target != null) {
                    if (info.isConsistent(this.mResolverContext, target.getResolveInfo())) {
                        index = i;
                        break;
                    }
                }
            }
            if (index >= 0) {
                this.mAdapter.insertItem(0, this.mAdapter.removeItem(index));
            }
        }
    }

    protected void onCreateItemView(int position, View convertView) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (this.mSettingDrawable == null) {
            Drawable dr = this.mResolver.getDrawable(R.drawable.vigour_btn_share_setting_light);
            if (FtBuild.getRomVersion() >= 4.0f) {
                this.mSettingDrawable = new BitmapDrawable(this.mResolverContext.getResources(), ImageUtil.getInstance(this.mResolverContext.getBaseContext()).createRedrawIconBitmap(dr));
                this.mNeedRecycle = true;
            } else {
                this.mSettingDrawable = dr;
            }
        }
        if (position == this.mTargetPosition) {
            holder.extraInfo.setText(null);
            holder.label.setText(null);
            holder.icon.setImageDrawable(this.mSettingDrawable);
        }
    }

    protected boolean OnItemClick(View view, int position) {
        if (position != this.mTargetPosition || this.mAdapter == null) {
            return false;
        }
        int count = this.mAdapter.getItemCount();
        ArrayList<ResolveInfo> infos = new ArrayList();
        for (int i = 0; i < count; i++) {
            TargetInfo info = this.mAdapter.getItem(i);
            if (!(info == null || info.getResolveInfo() == null)) {
                infos.add(info.getResolveInfo());
            }
        }
        Intent intent = new Intent(ACTION_SHARE_LIST_SORT);
        intent.putParcelableArrayListExtra(ResolverHelper.INTENT_EXTRA_RESOLVEINFO, infos);
        Object obj = null;
        List list = null;
        UserManager userManager = null;
        try {
            obj = this.mResolverContext.getPackageManager().getNameForUid(this.mCallingUid);
            list = ActivityManagerNative.getDefault().getDoubleInstanceConfig(1);
            userManager = (UserManager) this.mResolverContext.getSystemService("user");
        } catch (Exception e) {
            Log.e(TAG, "obtain DoubleInstance Pakcages failed", e);
        }
        if (DoubleAppSwitcher.sEnabled && obj != null && list != null && userManager != null && list.contains(obj) && userManager.getDoubleAppUserId() == UserHandle.getUserId(this.mCallingUid)) {
            intent.fixUris(UserHandle.getUserId(this.mCallingUid));
        }
        if (ResolverAdapterImpl.startActivityByKeyguard(this.mResolverContext, intent)) {
            this.mResolver.dismiss();
        } else {
            this.mResolver.startActivity(intent);
        }
        return true;
    }

    protected boolean OnOrderItemClick() {
        int count = this.mAdapter.getItemCount();
        ArrayList<ResolveInfo> infos = new ArrayList();
        for (int i = 0; i < count; i++) {
            TargetInfo info = this.mAdapter.getItem(i);
            if (!(info == null || info.getResolveInfo() == null)) {
                infos.add(info.getResolveInfo());
            }
        }
        Intent intent = new Intent(ACTION_SHARE_LIST_SORT);
        intent.putParcelableArrayListExtra(ResolverHelper.INTENT_EXTRA_RESOLVEINFO, infos);
        Object obj = null;
        List list = null;
        UserManager userManager = null;
        try {
            obj = this.mResolverContext.getPackageManager().getNameForUid(this.mCallingUid);
            list = ActivityManagerNative.getDefault().getDoubleInstanceConfig(1);
            userManager = (UserManager) this.mResolverContext.getSystemService("user");
        } catch (Exception e) {
            Log.e(TAG, "obtain DoubleInstance Pakcages failed", e);
        }
        if (DoubleAppSwitcher.sEnabled && obj != null && list != null && userManager != null && list.contains(obj) && userManager.getDoubleAppUserId() == UserHandle.getUserId(this.mCallingUid)) {
            intent.fixUris(UserHandle.getUserId(this.mCallingUid));
        }
        if (ResolverAdapterImpl.startActivityByKeyguard(this.mResolverContext, intent)) {
            this.mResolver.dismiss();
        } else {
            this.mResolver.startActivity(intent);
        }
        return true;
    }
}
