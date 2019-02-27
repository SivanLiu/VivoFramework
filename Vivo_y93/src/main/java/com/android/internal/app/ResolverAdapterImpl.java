package com.android.internal.app;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FtBuild;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.DoubleAppSwitcher;
import android.util.FtDeviceInfo;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnScrollChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResolverActivityIntroduceViewGroup;
import android.widget.SlideLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.app.AdapterDecker.Callback;
import com.android.internal.app.AdapterDecker.Listener;
import com.android.internal.app.AdapterDecker.WrapperAdapter;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.internal.app.ResolverAdapter.LifeCycle;
import com.vivo.common.VivoCollectData;
import com.vivo.content.ImageUtil;
import com.vivo.content.VivoTheme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

class ResolverAdapterImpl implements ResolverAdapter {
    /* renamed from: -com-android-internal-app-ResolverAdapter$LifeCycleSwitchesValues */
    private static final /* synthetic */ int[] f125x3d2c93ee = null;
    private static final boolean DEBUG = true;
    private static final String KEYGUARD_ACTION = "com.android.keyguard.KeyguardService";
    private static final String KEYGUARD_OPEN_INTENT = "open_intent";
    private static final String KEYGUARD_OPEN_USER = "open_user";
    private static final String KEYGUARD_OPERATION = "opt";
    private static final String PROP_NAVBAR = "qemu.hw.mainkeys";
    private static final String RESOLVER_ACTION = "android.intent.action.RESOLVER";
    private static final String RESOLVER_ACTION_CATEGORY = "category";
    private static final String RESOLVER_ACTION_TYPE = "action";
    private static final String RESOLVER_EXTRA_DEFAULT = "default";
    private static final String RESOLVER_EXTRA_INTENT = "intent";
    private static final String RESOLVER_EXTRA_RI = "resolveInfo";
    private static final String TAG = "ResolverAdapterImpl";
    static List<String> mFixedSequenceItems = new ArrayList();
    static Intent mKeyguardIntent = null;
    private static final float mRomVersion = FtBuild.getRomVersion();
    private ResolveListAdapter mAdapter = null;
    private AdapterDecker mAdapterDecker = null;
    private CheckBox mAlwaysButton = null;
    private boolean mAlwaysUseOption = false;
    private ViewGroup mButtonBar = null;
    private ImageView mCancelButton = null;
    VivoCollectData mCollectData;
    private IContentView mContentView = null;
    private ListAdapterDecker mDeckerAdapter = null;
    private boolean mHasBuildList = false;
    private Map<Integer, Drawable> mListIconDrawable = new HashMap();
    private List<Listener> mListeners = new ArrayList();
    private TextView mOnceButton = null;
    private PackageManager mPm = null;
    private ResolverActivity mResolverContext = null;
    private ViewGroup mRootView = null;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                try {
                    Log.d(ResolverAdapterImpl.TAG, "dismiss ResolverAcitivty because of screen_off");
                    ResolverAdapterImpl.this.mResolverContext.dismiss();
                } catch (Exception e) {
                    Log.e(ResolverAdapterImpl.TAG, "ScreenOffReceiver dismiss fatal", e);
                }
            }
        }
    };
    private List<Drawable> mThemeBitmapCache = new ArrayList();
    private CharSequence mTitle = null;
    private UserManager mUm = null;

    private class DeckerCallback implements Callback {
        /* synthetic */ DeckerCallback(ResolverAdapterImpl this$0, DeckerCallback -this1) {
            this();
        }

        private DeckerCallback() {
        }

        public void notifyDataSetChanged() {
            ResolverAdapterImpl.this.mContentView.updateView();
        }

        public void addListener(Listener listener) {
            if (listener != null) {
                ResolverAdapterImpl.this.mListeners.add(listener);
            }
        }

        public void removeListener(Listener listener) {
            ResolverAdapterImpl.this.mListeners.remove(listener);
        }
    }

    private interface IContentView {
        int getItemLayoutId();

        int getLayoutId();

        void initLayout();

        void onPostBindView(int i, TargetInfo targetInfo, View view);

        void showEmptyMessage();

        void updateView();
    }

    private class ListAdapterDecker extends BaseAdapter implements WrapperAdapter {
        private float ITEM_PRESSED_ALPHA = 0.3f;
        private ResolveListAdapter mAdapter = null;
        private List<TargetInfo> mTargets = new ArrayList();

        public ListAdapterDecker(int count) {
            for (int i = 0; i < count; i++) {
                this.mTargets.add(null);
            }
        }

        public void setBaseAdapter(ResolveListAdapter adapter) {
            this.mAdapter = adapter;
            int count = this.mAdapter.getUnfilteredCount();
            this.mTargets.clear();
            for (int i = 0; i < count; i++) {
                this.mTargets.add(this.mAdapter.targetInfoForPosition(i, false));
            }
        }

        public ResolveListAdapter getBaseAdapter() {
            return this.mAdapter;
        }

        public int getItemCount() {
            return this.mTargets.size();
        }

        public void insertItem(int position, TargetInfo info) {
            if (position < 0 || position > this.mTargets.size()) {
                Log.e(ResolverAdapterImpl.TAG, "WrapperAdapter.insertItem out of Index position=" + position + " TargetInfo=" + info + " count=" + this.mTargets.size(), new Throwable());
            } else {
                this.mTargets.add(position, info);
            }
        }

        public TargetInfo removeItem(int position) {
            if (position >= 0 && position < this.mTargets.size()) {
                return (TargetInfo) this.mTargets.remove(position);
            }
            Log.e(ResolverAdapterImpl.TAG, "WrapperAdapter.remoteItem out of Index position=" + position + " count=" + this.mTargets.size());
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public int getCount() {
            return getItemCount();
        }

        public TargetInfo getItem(int position) {
            return (TargetInfo) this.mTargets.get(position);
        }

        public ViewGroup obtainConvertView(int position) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(ResolverAdapterImpl.this.mResolverContext).inflate(ResolverAdapterImpl.this.mContentView.getItemLayoutId(), null);
            view.setTag(new ViewHolder(view));
            return view;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = obtainConvertView(position);
            }
            Object target = getItem(position);
            if (target instanceof TargetInfo) {
                onBindView(position, (TargetInfo) target, convertView);
            }
            ResolverAdapterImpl.this.mAdapterDecker.onBindView(position, convertView);
            onPostBindView(position, (TargetInfo) target, convertView);
            return convertView;
        }

        private void onBindView(int position, TargetInfo info, View convertView) {
            ViewGroup view = (ViewGroup) convertView;
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            CharSequence label = info.getDisplayLabel();
            if (label != null) {
                label = label.toString().replaceAll("\n", "");
            }
            if (!TextUtils.equals(viewHolder.label.getText(), label)) {
                viewHolder.label.setText(label);
            }
            if (TextUtils.isEmpty(info.getExtendedInfo())) {
                viewHolder.extraInfo.setVisibility(8);
            } else {
                viewHolder.extraInfo.setVisibility(0);
                viewHolder.extraInfo.setText(info.getExtendedInfo());
            }
            viewHolder.icon.setImageDrawable((Drawable) ResolverAdapterImpl.this.mListIconDrawable.get(Integer.valueOf(position)));
        }

        private void onPostBindView(int position, TargetInfo target, View convertView) {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.icon.setImageDrawable(wrapperIcon(viewHolder.icon.getDrawable()));
            ResolverAdapterImpl.this.mContentView.onPostBindView(position, target, convertView);
        }

        /* JADX WARNING: Missing block: B:3:0x0006, code:
            return r5;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Drawable wrapperIcon(Drawable d) {
            if (!(d instanceof BitmapDrawable) || d == null || ResolverAdapterImpl.mRomVersion < 3.6f) {
                return d;
            }
            StateListDrawable stateDrawable = new StateListDrawable();
            d.mutate();
            d.setAlpha((int) (this.ITEM_PRESSED_ALPHA * 255.0f));
            stateDrawable.addState(new int[]{R.attr.state_enabled, R.attr.state_pressed}, d);
            stateDrawable.addState(new int[0], new BitmapDrawable(((BitmapDrawable) d).getBitmap()));
            return stateDrawable;
        }
    }

    private class ListContentView implements IContentView {
        private static final int ITEM_LAYOUT_ID = 50528352;
        private static final int LAYOUT_ID = 50528351;
        private ListView mListView;

        private class OnItemClickListener implements android.widget.AdapterView.OnItemClickListener, OnItemLongClickListener {
            /* synthetic */ OnItemClickListener(ListContentView this$1, OnItemClickListener -this1) {
                this();
            }

            private OnItemClickListener() {
            }

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listView = parent instanceof ListView ? (ListView) parent : null;
                if (listView == null || position < 0) {
                    Log.e(ResolverAdapterImpl.TAG, "OnItemClickListener  [ListView : " + listView + " position : " + position + "]");
                } else {
                    ResolverAdapterImpl.this.clickItem(view, position);
                }
            }

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object listView = parent instanceof ListView ? (ListView) parent : null;
                if (listView != null && position >= 0) {
                    return ResolverAdapterImpl.this.longClickItem(view, position);
                }
                Log.e(ResolverAdapterImpl.TAG, "OnItemClickListener  [ListView : " + listView + " position : " + position + "]");
                return false;
            }
        }

        /* synthetic */ ListContentView(ResolverAdapterImpl this$0, ListContentView -this1) {
            this();
        }

        private ListContentView() {
            this.mListView = null;
        }

        public void initLayout() {
            View view = ResolverAdapterImpl.this.mRootView.findViewById(com.vivo.internal.R.id.resolver_grid);
            if (view instanceof ListView) {
                this.mListView = (ListView) view;
                this.mListView.setAdapter(ResolverAdapterImpl.this.mDeckerAdapter);
                OnItemClickListener mItemClickListener = new OnItemClickListener(this, null);
                this.mListView.setOnItemClickListener(mItemClickListener);
                this.mListView.setOnItemLongClickListener(mItemClickListener);
                return;
            }
            throw new RuntimeException("can't find View named [list] in layout : " + ResolverAdapterImpl.this.mContentView.getLayoutId());
        }

        public void updateView() {
            ResolverAdapterImpl.this.mDeckerAdapter.notifyDataSetChanged();
        }

        public int getLayoutId() {
            return 50528351;
        }

        public int getItemLayoutId() {
            return 50528352;
        }

        public void showEmptyMessage() {
            ResolverAdapterImpl.this.mRootView.findViewById(com.vivo.internal.R.id.empty).setVisibility(0);
            this.mListView.setVisibility(8);
        }

        public void onPostBindView(int position, TargetInfo target, View convertView) {
        }
    }

    private abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        private ResolveInfo mResolveInfo = null;

        LoadIconTask(TargetInfo target) {
            this.mResolveInfo = target.getResolveInfo();
        }

        protected Drawable doInBackground(Void... params) {
            Drawable drawable;
            ResolveInfo ri = this.mResolveInfo;
            ImageUtil imageUtil = ImageUtil.getInstance(ResolverAdapterImpl.this.mResolverContext.getBaseContext());
            Drawable dr = null;
            boolean canCache = false;
            try {
                if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                    dr = ResolverAdapterImpl.this.mResolverContext.getIcon(ResolverAdapterImpl.this.mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                }
                int iconRes = ri.getIconResource();
                if (dr == null && iconRes != 0) {
                    dr = ResolverAdapterImpl.this.mResolverContext.getIcon(ResolverAdapterImpl.this.mPm.getResourcesForApplication(ri.resolvePackageName), iconRes);
                }
            } catch (NameNotFoundException e) {
                Log.e(ResolverAdapterImpl.TAG, "doInBackground cant' find resolvePackage[" + (ri.resolvePackageName == null ? "null" : ri.resolvePackageName) + "] for package : " + ri.activityInfo.packageName);
            }
            if (dr == null) {
                try {
                    String pkg = ri.getComponentInfo().packageName;
                    String name = ri.getComponentInfo().name;
                    Resources res = ResolverAdapterImpl.this.mPm.getResourcesForApplication(pkg);
                    TypedValue value = new TypedValue();
                    value.density = Resources.getSystem().getDisplayMetrics().densityDpi;
                    dr = VivoTheme.getAppIconDrawable(res, value, pkg, name);
                } catch (NameNotFoundException e2) {
                }
                if (dr == null) {
                    dr = ri.loadIcon(ResolverAdapterImpl.this.mPm);
                }
            }
            if (VivoTheme.isSystemIcon(ri.activityInfo.packageName)) {
                drawable = dr;
            } else {
                drawable = new BitmapDrawable(ResolverAdapterImpl.this.mResolverContext.getResources(), imageUtil.createRedrawIconBitmap(dr));
                canCache = true;
                Log.d(ResolverAdapterImpl.TAG, "redraw  " + ri.activityInfo.packageName);
            }
            if (DoubleAppSwitcher.sEnabled && ResolverAdapterImpl.this.mUm.isDoubleAppUserExist() && ResolverAdapterImpl.this.mUm.getDoubleAppUserId() == ri.targetUserId) {
                drawable = ResolverAdapterImpl.this.mPm.getUserBadgedIcon(drawable, new UserHandle(ri.targetUserId));
            }
            if (canCache) {
                ResolverAdapterImpl.this.mThemeBitmapCache.add(drawable);
            }
            if (drawable == null) {
                return dr;
            }
            return drawable;
        }
    }

    private class LoadAdapterIconTask extends LoadIconTask {
        private int index = 0;

        LoadAdapterIconTask(TargetInfo target, int pos) {
            super(target);
            this.index = pos;
        }

        protected void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            ResolverAdapterImpl.this.mListIconDrawable.put(Integer.valueOf(this.index), d);
            ResolverAdapterImpl.this.mContentView.updateView();
        }
    }

    private class LoadIconTaskIntoView extends LoadIconTask {
        private ImageView mView = null;

        LoadIconTaskIntoView(TargetInfo target, ImageView view) {
            super(target);
            this.mView = view;
        }

        protected void onPostExecute(Drawable d) {
            this.mView.setImageDrawable(d);
        }
    }

    private class SlideContentView implements IContentView {
        private static final String ALLOW_SHOW_TIPS = "1";
        private static final int ITEM_LAYOUT_ID = 50528350;
        private static final int LAYOUT_ID = 50528349;
        private static final String SHOWTIPS = "resolveractivity_slide_showtips";
        private OnItemClickListener mItemClickListener;
        private SlideLayout mSlideView;

        private class OnItemClickListener implements OnClickListener, OnLongClickListener {
            /* synthetic */ OnItemClickListener(SlideContentView this$1, OnItemClickListener -this1) {
                this();
            }

            private OnItemClickListener() {
            }

            public void onClick(View v) {
                ResolverAdapterImpl.this.clickItem(v, SlideContentView.this.mSlideView.indexOfChild(v));
            }

            public boolean onLongClick(View v) {
                return ResolverAdapterImpl.this.longClickItem(v, SlideContentView.this.mSlideView.indexOfChild(v));
            }
        }

        /* synthetic */ SlideContentView(ResolverAdapterImpl this$0, SlideContentView -this1) {
            this();
        }

        private SlideContentView() {
            this.mSlideView = null;
            this.mItemClickListener = new OnItemClickListener(this, null);
        }

        public void initLayout() {
            this.mSlideView = (SlideLayout) ResolverAdapterImpl.this.mRootView.findViewById(com.vivo.internal.R.id.resolver_slide);
            final ImageView shareSortIcon = (ImageView) ResolverAdapterImpl.this.mRootView.findViewById(com.vivo.internal.R.id.title_order);
            if (FtBuild.getRomVersion() >= 4.5f) {
                this.mSlideView.setOnScrollChangeListener(new OnScrollChangeListener() {
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if (!SlideContentView.ALLOW_SHOW_TIPS.equals(System.getString(ResolverAdapterImpl.this.mResolverContext.getContentResolver(), SlideContentView.SHOWTIPS)) && SlideContentView.this.mSlideView.getCurrentPagePosition() != 0) {
                            System.putString(ResolverAdapterImpl.this.mResolverContext.getContentResolver(), SlideContentView.SHOWTIPS, SlideContentView.ALLOW_SHOW_TIPS);
                            WindowManager windowManager = (WindowManager) ResolverAdapterImpl.this.mResolverContext.getSystemService("window");
                            int rotation = windowManager.getDefaultDisplay().getRotation();
                            if (ResolverAdapterImpl.this.mResolverContext.getResources().getConfiguration().orientation == 1) {
                                ResolverAdapterImpl.this.mResolverContext.setRequestedOrientation(1);
                            } else {
                                int orientation;
                                if (rotation == 1) {
                                    orientation = 0;
                                } else {
                                    orientation = 8;
                                }
                                ResolverAdapterImpl.this.mResolverContext.setRequestedOrientation(orientation);
                            }
                            ResolverActivityIntroduceViewGroup introduceViewGroup = new ResolverActivityIntroduceViewGroup(ResolverAdapterImpl.this.mResolverContext, shareSortIcon, ResolverAdapterImpl.this.mRootView.getResources().getString(com.vivo.internal.R.string.vivo_resolve_activity_slide_tips));
                            introduceViewGroup.setLayoutParams(new LayoutParams(-1, -1));
                            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                            params.width = -1;
                            params.height = -1;
                            params.format = 1;
                            params.flags = 1032;
                            windowManager.addView(introduceViewGroup, params);
                        }
                    }
                });
            }
            int count = ResolverAdapterImpl.this.mDeckerAdapter.getItemCount();
            for (int i = 0; i < count; i++) {
                View view = ResolverAdapterImpl.this.mDeckerAdapter.obtainConvertView(i);
                view.setOnClickListener(this.mItemClickListener);
                view.setOnLongClickListener(this.mItemClickListener);
                this.mSlideView.addView(view);
            }
        }

        public void updateView() {
            int i;
            int count = ResolverAdapterImpl.this.mDeckerAdapter.getItemCount();
            for (i = 0; i < count; i++) {
                View view = this.mSlideView.getChildAt(i);
                if (view == null) {
                    view = ResolverAdapterImpl.this.mDeckerAdapter.getView(i, null, null);
                    view.setOnClickListener(this.mItemClickListener);
                    view.setOnLongClickListener(this.mItemClickListener);
                    this.mSlideView.addView(view);
                } else {
                    ResolverAdapterImpl.this.mDeckerAdapter.getView(i, view, null);
                    this.mSlideView.requestLayout();
                }
            }
            for (i = this.mSlideView.getChildCount() - 1; i >= count; i--) {
                this.mSlideView.removeViewAt(i);
            }
        }

        public int getLayoutId() {
            return 50528349;
        }

        public int getItemLayoutId() {
            return 50528350;
        }

        public void showEmptyMessage() {
            ResolverAdapterImpl.this.mRootView.findViewById(com.vivo.internal.R.id.empty).setVisibility(0);
            this.mSlideView.setVisibility(8);
        }

        public void onPostBindView(int position, TargetInfo info, View convertView) {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder != null) {
                viewHolder.extraInfo.setVisibility(8);
                if (info != null && (TextUtils.isEmpty(info.getExtendedInfo()) ^ 1) != 0) {
                    Log.w(ResolverAdapterImpl.TAG, "hide extraInfo [" + info.getExtendedInfo() + "] in position [" + position + "] for : " + info);
                }
            }
        }
    }

    class ViewHolder {
        TextView appStatus;
        TextView extraInfo;
        ImageView icon;
        TextView label;

        ViewHolder(View v) {
            this.label = (TextView) v.findViewById(com.vivo.internal.R.id.text1);
            this.extraInfo = (TextView) v.findViewById(com.vivo.internal.R.id.text2);
            this.icon = (ImageView) v.findViewById(com.vivo.internal.R.id.icon);
            this.appStatus = (TextView) v.findViewById(com.vivo.internal.R.id.text_app_download_status);
        }
    }

    /* renamed from: -getcom-android-internal-app-ResolverAdapter$LifeCycleSwitchesValues */
    private static /* synthetic */ int[] m50x9c35b7ca() {
        if (f125x3d2c93ee != null) {
            return f125x3d2c93ee;
        }
        int[] iArr = new int[LifeCycle.values().length];
        try {
            iArr[LifeCycle.ON_DESTROY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LifeCycle.ON_PAUSE.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[LifeCycle.ON_RESTART.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[LifeCycle.ON_RESUME.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[LifeCycle.ON_STOP.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        f125x3d2c93ee = iArr;
        return iArr;
    }

    static {
        mFixedSequenceItems.add("com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias");
        mFixedSequenceItems.add("com.whatsapp.ContactPicker");
        mFixedSequenceItems.add("com.tencent.mm.ui.tools.ShareImgUI");
        mFixedSequenceItems.add("com.tencent.mm.ui.tools.ShareToTimeLineUI");
        mFixedSequenceItems.add("com.tencent.mobileqq.activity.JumpActivity");
        mFixedSequenceItems.add("com.qzonex.module.operation.ui.QZonePublishMoodActivity");
        mFixedSequenceItems.add("com.vivo.easyshare.activity.ShareFileActivity");
        mFixedSequenceItems.add("com.android.mms.ui.ComposeMessageActivity");
        mFixedSequenceItems.add("com.vivo.email.ui.compose.EmailComposeActivity");
        mFixedSequenceItems.add("com.sina.weibo.composerinde.ComposerDispatchActivity");
    }

    ResolverAdapterImpl(ResolverActivity resolver, AdapterDecker decker) {
        this.mResolverContext = resolver;
        this.mAdapterDecker = decker;
        this.mPm = this.mResolverContext.getPackageManager();
        this.mResolverContext.requestWindowFeature(1);
        this.mResolverContext.getWindow().addFlags(524288);
        this.mResolverContext.setFinishOnTouchOutside(true);
        if (mRomVersion >= 3.6f) {
            this.mResolverContext.setTheme(VivoThemeUtil.getSystemThemeStyle(ThemeType.CONTEXT_MENU_DIALOG));
            Window window = this.mResolverContext.getWindow();
            window.setGravity(80);
            boolean navBarHide = SystemProperties.getInt(PROP_NAVBAR, 0) == 1;
            if (mRomVersion < 4.0f || !navBarHide) {
                window.getAttributes().y = this.mResolverContext.getResources().getDimensionPixelSize(com.vivo.internal.R.dimen.vigour_dialog_margin_bottom_navgationbar);
            } else {
                window.getAttributes().y = this.mResolverContext.getResources().getDimensionPixelOffset(com.vivo.internal.R.dimen.vigour_dialog_margin_bottom_no_navgationbar) - FtDeviceInfo.getGestureBarHeight(this.mResolverContext);
            }
        } else {
            this.mResolverContext.setTheme(VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT));
        }
        if (DoubleAppSwitcher.sEnabled) {
            this.mUm = (UserManager) this.mResolverContext.getSystemService("user");
        }
        this.mCollectData = VivoCollectData.getInstance(this.mResolverContext);
    }

    public void onCreate(Bundle bundle, Intent intent, Intent[] initialIntent, boolean supportsAlwaysUseOption) {
        this.mAlwaysUseOption = supportsAlwaysUseOption;
        this.mContentView = generateContentView();
        this.mResolverContext.registerReceiver(this.mScreenOffReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
        preInitLayout();
        this.mAdapterDecker.onCreate(this.mResolverContext, intent, new DeckerCallback(this, null));
    }

    private IContentView generateContentView() {
        if (mRomVersion >= 3.6f) {
            return new SlideContentView(this, null);
        }
        return new ListContentView(this, null);
    }

    private void preInitLayout() {
        View view = this.mResolverContext.findViewById(R.id.contentPanel);
        if (view != null) {
            this.mRootView = (ViewGroup) view.getParent();
        } else {
            this.mRootView = (ViewGroup) this.mResolverContext.getWindow().getDecorView();
        }
        for (int i = this.mRootView.getChildCount() - 1; i >= 0; i--) {
            this.mRootView.getChildAt(i).setVisibility(8);
        }
        LayoutInflater.from(this.mResolverContext).inflate(this.mContentView.getLayoutId(), this.mRootView, true);
        this.mRootView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ResolverAdapterImpl.this.mResolverContext.dismiss();
            }
        });
        initialLayoutView();
    }

    private void initialLayoutView() {
        this.mCancelButton = (ImageView) this.mRootView.findViewById(com.vivo.internal.R.id.cancel);
        if (this.mCancelButton != null) {
            this.mCancelButton.setVisibility(0);
            this.mCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ResolverAdapterImpl.this.mResolverContext.overridePendingTransition(0, 0);
                    ResolverAdapterImpl.this.mResolverContext.dismiss();
                }
            });
        }
        if (this.mAlwaysUseOption) {
            this.mButtonBar = (ViewGroup) this.mRootView.findViewById(com.vivo.internal.R.id.button_bar);
            this.mButtonBar.setVisibility(0);
            this.mAlwaysButton = (CheckBox) this.mButtonBar.findViewById(com.vivo.internal.R.id.button_always);
            this.mAlwaysButton.setText((int) R.string.alwaysUse);
            this.mAlwaysButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ResolverAdapterImpl.this.mOnceButton.setVisibility(isChecked ? 0 : 8);
                }
            });
            this.mOnceButton = (TextView) this.mButtonBar.findViewById(com.vivo.internal.R.id.button_once);
            if (FtBuild.getRomVersion() >= 4.5f) {
                this.mOnceButton.setText((int) com.vivo.internal.R.string.new_clearDefaultHintApp);
            } else {
                this.mOnceButton.setText((int) com.vivo.internal.R.string.clearDefaultHintApp);
            }
        }
    }

    public void onPreBuildList(List<ResolvedComponentInfo> infos) {
        this.mHasBuildList = false;
        if (infos == null) {
            infos = new ArrayList();
        }
        Log.d(TAG, "onPreBuildList Component Items Count : " + infos.size(), new Throwable());
        this.mAdapterDecker.onPreBuildList(infos);
        this.mDeckerAdapter = new ListAdapterDecker(infos.size());
        this.mContentView.initLayout();
    }

    public void onRebuildList(ResolveListAdapter adapter, CharSequence title) {
        this.mTitle = title;
        this.mAdapter = adapter;
        this.mDeckerAdapter.setBaseAdapter(adapter);
        Log.d(TAG, "onRebuildList items : " + this.mDeckerAdapter.getItemCount(), new Throwable());
        sortItemSequence();
        this.mAdapterDecker.onRebuildList(this.mDeckerAdapter);
        this.mHasBuildList = true;
        postInitLayout();
        preLoadIconDrawable();
    }

    private void postInitLayout() {
        TextView titleView = (TextView) this.mRootView.findViewById(com.vivo.internal.R.id.alertTitle);
        if (TextUtils.isEmpty(this.mTitle)) {
            titleView.setVisibility(8);
        } else {
            titleView.setText(this.mTitle);
            if (this.mCancelButton != null && this.mCancelButton.getVisibility() == 0) {
                int padding = (this.mCancelButton.getPaddingLeft() + this.mCancelButton.getPaddingRight()) + this.mCancelButton.getBackground().getIntrinsicWidth();
                titleView.setPadding(padding, titleView.getPaddingTop(), padding, titleView.getPaddingBottom());
            }
        }
        if (this.mDeckerAdapter.getItemCount() <= 0) {
            Log.d(TAG, "have nothing available items, hide ListView and show empty message");
            this.mContentView.showEmptyMessage();
            if (this.mButtonBar != null) {
                this.mButtonBar.setVisibility(8);
            }
        }
    }

    private void preLoadIconDrawable() {
        int count = this.mDeckerAdapter.getItemCount();
        Log.d(TAG, "preLoadIconDrawable for ResolverListAdapter count : " + count);
        this.mListIconDrawable.clear();
        for (int i = 0; i < count; i++) {
            Object info = this.mDeckerAdapter.getItem(i);
            if (info instanceof TargetInfo) {
                new LoadAdapterIconTask((TargetInfo) info, i).execute(new Void[0]);
            }
        }
    }

    private void sortItemSequence() {
        sortByFixedSequence();
        if (this.mAdapter.getOtherProfile() != null) {
            this.mDeckerAdapter.insertItem(this.mDeckerAdapter.getItemCount(), this.mAdapter.getOtherProfile());
        }
    }

    private void sortByFixedSequence() {
        int count = this.mDeckerAdapter.getItemCount();
        ArrayList<Integer> fixedSequenceAppIndex = new ArrayList();
        for (int i = mFixedSequenceItems.size() - 1; i >= 0; i--) {
            String componentName = (String) mFixedSequenceItems.get(i);
            for (int j = 0; j < count; j++) {
                TargetInfo target = this.mDeckerAdapter.getItem(j);
                if (target != null && componentName.equals(target.getResolveInfo().activityInfo.name)) {
                    fixedSequenceAppIndex.add(Integer.valueOf(j));
                }
            }
            for (int k = 0; k < fixedSequenceAppIndex.size(); k++) {
                TargetInfo info = this.mDeckerAdapter.removeItem(((Integer) fixedSequenceAppIndex.get(k)).intValue());
                ActivityInfo activityInfo = info.getResolveInfo().activityInfo;
                this.mDeckerAdapter.insertItem(0, info);
            }
            fixedSequenceAppIndex.clear();
        }
    }

    public void onLifeCycleChanged(LifeCycle life) {
        switch (m50x9c35b7ca()[life.ordinal()]) {
            case 1:
                onDestroy();
                break;
            case 2:
                onResume();
                break;
            default:
                Log.e(TAG, "invalidate LifeCycle : " + life);
                break;
        }
        this.mAdapterDecker.onLifeCycleChanged(life);
    }

    protected void onDestroy() {
        if (this.mThemeBitmapCache != null) {
            Log.d(TAG, "clear theme bitmap cache");
            for (int i = 0; i < this.mThemeBitmapCache.size(); i++) {
                if (this.mThemeBitmapCache.get(i) instanceof BitmapDrawable) {
                    Bitmap map = ((BitmapDrawable) this.mThemeBitmapCache.get(i)).getBitmap();
                    if (!map.isRecycled()) {
                        map.recycle();
                    }
                }
            }
        }
        try {
            if (this.mScreenOffReceiver != null) {
                this.mResolverContext.unregisterReceiver(this.mScreenOffReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "unRegister ScreenOff fatal", e);
        }
    }

    private void onResume() {
        this.mResolverContext.getWindow().setLayout(-1, -2);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
    }

    private void clickItem(View view, final int position) {
        new Thread(new Runnable() {
            public void run() {
                if (ResolverAdapterImpl.this.mCollectData.getControlInfo("232")) {
                    HashMap<String, String> params_shared_success = new HashMap();
                    long time = System.currentTimeMillis();
                    params_shared_success.put("shared_susccess_num", "1");
                    ResolverAdapterImpl.this.mCollectData.writeData("232", "2323", time, time, 0, 1, params_shared_success);
                    Log.e("sqy", "run: " + System.getString(ResolverAdapterImpl.this.mResolverContext.getContentResolver(), "resolveractivity_isSort"));
                    if ("1".equals(System.getString(ResolverAdapterImpl.this.mResolverContext.getContentResolver(), "resolveractivity_isSort"))) {
                        HashMap<String, String> sorted_position = new HashMap();
                        sorted_position.put("sorted_position", String.valueOf(position));
                        ResolverAdapterImpl.this.mCollectData.writeData("232", "2325", time, time, 0, 1, sorted_position);
                        return;
                    }
                    HashMap<String, String> params_unsorted = new HashMap();
                    params_unsorted.put("unsorted_position", String.valueOf(position));
                    ResolverAdapterImpl.this.mCollectData.writeData("232", "2324", time, time, 0, 1, params_unsorted);
                }
            }
        }).start();
        if (this.mHasBuildList) {
            for (Listener listener : this.mListeners) {
                if (listener.onItemClick(view, position)) {
                    return;
                }
            }
            if (!this.mResolverContext.isFinishing()) {
                boolean alwaysChecked = this.mAlwaysButton != null ? this.mAlwaysButton.isChecked() : false;
                TargetInfo target = this.mDeckerAdapter.getItem(position);
                if (target != null) {
                    Intent intent = target.getResolvedIntent();
                    if (startActivityByKeyguard(this.mResolverContext, intent) || this.mResolverContext.onTargetSelected(target, alwaysChecked)) {
                        sendItemSelectedBroadcast(target, intent, alwaysChecked);
                        this.mResolverContext.finish();
                        return;
                    }
                }
            }
            return;
        }
        Log.e(TAG, "ignore clickItem for empty-adapter position=" + position + " view=" + view);
    }

    private boolean longClickItem(View view, int position) {
        if (this.mHasBuildList) {
            for (Listener listener : this.mListeners) {
                if (listener.onItemLongClick(view, position)) {
                    return true;
                }
            }
            TargetInfo target = this.mDeckerAdapter.getItem(position);
            if (target == null) {
                return false;
            }
            ResolveInfo ri = target.getResolveInfo();
            if (startActivityByKeyguard(this.mResolverContext, new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288))) {
                this.mResolverContext.dismiss();
            } else {
                this.mResolverContext.showTargetDetails(ri);
            }
            return true;
        }
        Log.e(TAG, "ignore longClickItem for empty-adapter position=" + position + " view=" + view);
        return true;
    }

    static boolean startActivityByKeyguard(Context context, Intent intent) {
        return startActivityByKeyguard(context, intent, -2);
    }

    static boolean startActivityByKeyguard(Context context, Intent intent, int user) {
        if (((KeyguardManager) context.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            if (mKeyguardIntent == null) {
                List<ResolveInfo> infos = context.getPackageManager().queryIntentServices(new Intent(KEYGUARD_ACTION), 1048576);
                if (infos == null || infos.size() != 1) {
                    Log.e(TAG, "queryIntentServices " + intent + " : " + infos);
                    return false;
                }
                ResolveInfo ri = (ResolveInfo) infos.get(0);
                ComponentName component = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
                mKeyguardIntent = new Intent();
                mKeyguardIntent.setComponent(component);
            }
            mKeyguardIntent.putExtra(KEYGUARD_OPEN_INTENT, intent);
            mKeyguardIntent.putExtra(KEYGUARD_OPERATION, 1);
            mKeyguardIntent.putExtra(KEYGUARD_OPEN_USER, user);
            context.startService(mKeyguardIntent);
            return true;
        }
        Log.d(TAG, "ignore keyguard, start Activity normally");
        return false;
    }

    private void sendItemSelectedBroadcast(TargetInfo target, Intent in, boolean alwaysChecked) {
        ResolveInfo ri = target.getResolveInfo();
        if (ri == null) {
            Log.w(TAG, "Ignore ItemSelectedBroadcast becauseof Null TargetInfo");
            return;
        }
        Intent intent = new Intent(RESOLVER_ACTION);
        intent.putExtra("category", 0);
        intent.putExtra(RESOLVER_ACTION_TYPE, 0);
        intent.putExtra("intent", in);
        intent.putExtra(RESOLVER_EXTRA_RI, ri);
        intent.putExtra("default", alwaysChecked);
        this.mResolverContext.sendBroadcast(intent);
    }
}
