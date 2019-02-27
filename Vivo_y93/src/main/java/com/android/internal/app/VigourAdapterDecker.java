package com.android.internal.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.VivoCustomUtils;
import android.view.View;
import com.android.internal.app.AdapterDecker.Callback;
import com.android.internal.app.AdapterDecker.WrapperAdapter;
import com.android.internal.app.ResolverActivity.DisplayResolveInfo;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.internal.app.ResolverAdapter.LifeCycle;
import com.vivo.internal.R;
import com.vivo.provider.VivoSettings;
import java.util.Iterator;
import java.util.List;

class VigourAdapterDecker implements AdapterDecker {
    private static final String ACTION_DESK_SCENE = "com.bbk.launcher.action.SCENE";
    private static final String CHILDREN_MODE_PACKAGENAME = "com.vivo.childrenmode";
    private static final boolean DEBUG = true;
    private static final String MAIN_LAUNCHER_PKGNAME = "com.bbk.launcher2";
    private static final String PKGNAME_DESK_SCENE = "com.bbk.scene.launcher.theme";
    private static final String SCENE_LAUNCHER_THEME_PKGNAME = "com.bbk.scene.launcher.theme";
    private static final String SIMPLE_LAUNCHER_PACKAGENAME = "com.vivo.simplelauncher";
    private static final String TAG = "VigourAdapterDecker";
    private static boolean mFilterEnable = SystemProperties.getBoolean("debug.resolver.vigour.filter", true);
    private static ArrayMap<FilterKey, FilterItem[]> mFilterItems = new ArrayMap();
    private WrapperAdapter mAdapter = null;
    private List<ResolveInfo> mDeskSceneList = null;
    private int mDeskSceneSize = 0;
    private SparseArray<String> mDisplayExtraInfo = new SparseArray();
    private boolean mIsMainAction = false;
    private ResolveInfo mMainLauncherInfo = null;
    private PackageManager mPm = null;
    private ResolverActivity mResolverContext = null;
    private Intent mStartIntent = null;

    private static class FilterItem {
        private String className;
        private String packageName;

        FilterItem(String pkg, String name) {
            this.packageName = pkg;
            this.className = name;
        }

        FilterItem(String name) {
            this(null, name);
        }

        private boolean match(String pkg, String name) {
            if (this.packageName != null) {
                return this.packageName.equals(pkg);
            }
            return this.className.equals(name);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            boolean equals;
            FilterItem item = (FilterItem) o;
            if (this.packageName != null) {
                equals = this.packageName.equals(item.packageName);
            } else {
                equals = this.className.equals(item.className);
            }
            return equals;
        }

        public int hashCode() {
            if (this.packageName != null) {
                return this.packageName.hashCode();
            }
            return this.packageName.hashCode() | this.className.hashCode();
        }

        public String toString() {
            return "FilterItem={packageName=" + this.packageName + " className=" + this.className + "}";
        }
    }

    private static class FilterKey {
        private String action;
        private String mimeType;

        FilterKey(String action, String mimeType) {
            this.action = action;
            this.mimeType = mimeType;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FilterKey key = (FilterKey) o;
            return this.action.equals(key.action) && (this.mimeType == null || (this.mimeType.equals(key.mimeType) ^ 1) == 0);
        }

        public int hashCode() {
            if (this.mimeType == null) {
                return this.action.hashCode();
            }
            return this.action.hashCode() | this.mimeType.hashCode();
        }

        public String toString() {
            return "FilterKey {action=" + this.action + " mimeType=" + this.mimeType + "}";
        }
    }

    VigourAdapterDecker() {
    }

    static {
        mFilterItems.put(new FilterKey("android.intent.action.SEND", "audio/mp4a-latm"), new FilterItem[0]);
        mFilterItems.put(new FilterKey("android.intent.action.SEND_MULTIPLE", "audio/mp4a-latm"), new FilterItem[0]);
        mFilterItems.put(new FilterKey("android.intent.action.SENDTO", "audio/mp4a-latm"), (FilterItem[]) mFilterItems.get(new FilterKey("android.intent.action.SEND", "audio/mp4a-latm")));
    }

    public void onCreate(ResolverActivity activity, Intent in, Callback callback) {
        int i = 0;
        this.mResolverContext = activity;
        this.mStartIntent = in;
        if ("android.intent.action.MAIN".equals(this.mStartIntent.getAction()) && this.mStartIntent.hasCategory("android.intent.category.HOME")) {
            this.mIsMainAction = true;
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_DESK_SCENE);
        this.mDeskSceneList = this.mResolverContext.getPackageManager().queryIntentActivities(intent, 0);
        if (this.mDeskSceneList != null) {
            i = this.mDeskSceneList.size();
        }
        this.mDeskSceneSize = i;
        this.mPm = this.mResolverContext.getPackageManager();
    }

    public void onPreBuildList(List<ResolvedComponentInfo> components) {
        ResolvedComponentInfo lunchComponent = null;
        if (this.mIsMainAction) {
            int count;
            int i;
            ComponentName comp;
            if (!hasOtherLauncher(components)) {
                count = components.size();
                for (i = 0; i < count; i++) {
                    comp = ((ResolvedComponentInfo) components.get(i)).name;
                    if (comp != null && MAIN_LAUNCHER_PKGNAME.equals(comp.getPackageName())) {
                        Log.i(TAG, "start " + comp + " because don't have other launcher");
                        lunchComponent = (ResolvedComponentInfo) components.get(i);
                        break;
                    }
                }
            } else if (VivoCustomUtils.isVivoCustomized()) {
                String ctPreferredLauncher = Secure.getString(this.mResolverContext.getContentResolver(), VivoSettings.Secure.CT_PREFERRED_LAUNCHER_PKG);
                if (1 == Secure.getInt(this.mResolverContext.getContentResolver(), VivoSettings.Secure.CT_OEM_LAUNCHER_ONLY, 0)) {
                }
                String backup = Secure.getString(this.mResolverContext.getContentResolver(), "ct_prefferrd_launcher_backup");
                Log.i(TAG, "ctPreferredLauncher:" + ctPreferredLauncher + " backup:" + backup);
                if (ctPreferredLauncher != null) {
                    count = components.size();
                    for (i = 0; i < count; i++) {
                        comp = ((ResolvedComponentInfo) components.get(i)).name;
                        if (comp != null && ctPreferredLauncher.equals(comp.getPackageName()) && backup == null) {
                            Log.i(TAG, "start " + comp + " ctPreferredLauncher");
                            lunchComponent = (ResolvedComponentInfo) components.get(i);
                            break;
                        }
                    }
                }
            }
        }
        FilterItem[] filterItemArr = null;
        if (this.mStartIntent.getAction() != null) {
            filterItemArr = (FilterItem[]) mFilterItems.get(new FilterKey(this.mStartIntent.getAction(), this.mStartIntent.getType()));
        } else {
            Log.d(TAG, "skip filter items becauseof empty action intent=" + this.mStartIntent);
        }
        if (mFilterEnable && lunchComponent == null && filterItemArr != null) {
            for (FilterItem item : filterItemArr) {
                Iterator<ResolvedComponentInfo> ite = components.iterator();
                while (ite.hasNext()) {
                    ResolvedComponentInfo info = (ResolvedComponentInfo) ite.next();
                    if (item.match(info.name.getPackageName(), info.name.getClassName())) {
                        Log.i(TAG, "remove item component=" + info.name.getClassName());
                        ite.remove();
                    }
                }
            }
            if (components.size() == 1) {
                lunchComponent = (ResolvedComponentInfo) components.get(0);
            }
        }
        if (lunchComponent != null) {
            if (components.size() < 2) {
                components.add((ResolvedComponentInfo) components.get(0));
            }
            ResolveInfo ri = lunchComponent.getResolveInfoAt(0);
            ResolverActivity resolverActivity = this.mResolverContext;
            resolverActivity.getClass();
            this.mResolverContext.onTargetSelected(new DisplayResolveInfo(lunchComponent.getIntentAt(0), ri, ri.loadLabel(this.mPm), ri.loadLabel(this.mPm), lunchComponent.getIntentAt(0)), true);
            this.mResolverContext.finish();
        }
    }

    public void onRebuildList(WrapperAdapter adapter) {
        this.mAdapter = adapter;
        if (this.mIsMainAction) {
            setMainLauncherByPriority();
        }
        if (this.mIsMainAction && this.mMainLauncherInfo != null) {
            for (int i = this.mAdapter.getItemCount() - 1; i >= 0; i--) {
                TargetInfo target = this.mAdapter.getItem(i);
                if (target != null) {
                    ResolveInfo ro = target.getResolveInfo();
                    if (canAddExtraInfo(ro) && ro.activityInfo.packageName.equals(this.mMainLauncherInfo.activityInfo.packageName)) {
                        this.mDisplayExtraInfo.put(i, this.mResolverContext.getString(R.string.scenedesktop));
                    }
                }
            }
        }
    }

    private boolean canAddExtraInfo(ResolveInfo info) {
        if (info == null || info.activityInfo == null || this.mMainLauncherInfo == null || this.mMainLauncherInfo.activityInfo == null) {
            return false;
        }
        if (info.activityInfo.packageName.equals(this.mMainLauncherInfo.activityInfo.packageName)) {
            return true;
        }
        for (int i = 0; i < this.mDeskSceneSize; i++) {
            ResolveInfo ri = (ResolveInfo) this.mDeskSceneList.get(i);
            if (ri.activityInfo.packageName.equals(info.activityInfo.packageName) && ri.activityInfo.name.equals(info.activityInfo.name)) {
                return false;
            }
        }
        return true;
    }

    private void setMainLauncherByPriority() {
        ResolveInfo ri = null;
        if (this.mAdapter.getItemCount() <= 0 || this.mDeskSceneList == null || this.mDeskSceneSize < 1) {
            this.mMainLauncherInfo = null;
            return;
        }
        if (this.mDeskSceneSize > 1) {
            for (ResolveInfo item : this.mDeskSceneList) {
                if (item == null || item.activityInfo.packageName == null || !(item.activityInfo.packageName.equals("com.bbk.scene.launcher.theme") || item.activityInfo.packageName.equals(CHILDREN_MODE_PACKAGENAME))) {
                    ri = item;
                    break;
                }
            }
        } else {
            ri = (ResolveInfo) this.mDeskSceneList.get(0);
        }
        if (ri != null && ri.activityInfo.packageName.equals(CHILDREN_MODE_PACKAGENAME)) {
            if (this.mDeskSceneList != null) {
                for (ResolveInfo item2 : this.mDeskSceneList) {
                    if (!(item2 == null || item2.activityInfo.packageName == null || (item2.activityInfo.packageName.equals(CHILDREN_MODE_PACKAGENAME) ^ 1) == 0)) {
                        ri = item2;
                    }
                }
            } else {
                ri = null;
            }
        }
        this.mMainLauncherInfo = ri;
    }

    private boolean hasOtherLauncher(List<ResolvedComponentInfo> components) {
        int count = components.size();
        boolean otherLauncher = false;
        for (int i = 0; i < count; i++) {
            ResolveInfo ri = ((ResolvedComponentInfo) components.get(i)).getResolveInfoAt(0);
            for (int j = 0; j < this.mDeskSceneSize; j++) {
                if (ri.activityInfo.packageName.equals(((ResolveInfo) this.mDeskSceneList.get(j)).activityInfo.packageName)) {
                    break;
                }
            }
            if (!MAIN_LAUNCHER_PKGNAME.equals(ri.activityInfo.packageName) && (SIMPLE_LAUNCHER_PACKAGENAME.equals(ri.activityInfo.packageName) ^ 1) != 0) {
                otherLauncher = true;
                break;
            }
        }
        Log.d(TAG, "hasOtherlauncher : " + otherLauncher);
        return otherLauncher;
    }

    public void onBindView(int position, View convertView) {
        if (this.mAdapter == null) {
            Log.d(TAG, "ignore onBindView for delay onRebuildList");
            return;
        }
        Object target = this.mAdapter.getItem(position);
        if (target != null) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            String pkg = target.getResolveInfo().activityInfo.packageName;
            if (this.mAdapter.getBaseAdapter().getOtherProfile() == target) {
                holder.extraInfo.setText(null);
            } else if (this.mDisplayExtraInfo.get(position) != null) {
                holder.extraInfo.setText((CharSequence) this.mDisplayExtraInfo.get(position));
            }
        }
    }

    public void onLifeCycleChanged(LifeCycle life) {
    }
}
