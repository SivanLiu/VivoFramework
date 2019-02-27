package com.vivo.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FtBuild;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import com.android.internal.app.ResolverHelper;
import com.android.internal.app.ResolverHelper.SharedPackageInfo;
import com.vivo.common.BbkTitleView;
import com.vivo.common.VivoCollectData;
import com.vivo.common.autobrightness.RunningInfo;
import com.vivo.common.widget.DragLayout.Callback;
import com.vivo.common.widget.DragListView;
import com.vivo.content.ImageUtil;
import com.vivo.content.VivoTheme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharedListSortActivity extends Activity {
    private static final boolean DEBUG = true;
    private static final int HANDLER_RESTORE_DATA = 2;
    private static final int HANDLER_SAVE_DATA = 1;
    private static final String JSON_LABEL_NAME = "name";
    private static final String JSON_LABEL_PKG = "pkg";
    private static final String JSON_LABEL_ROOT = "shareInfo";
    private static final String SHARE_INFO_FILE_NAME = "share_sort_info.json";
    private static final String TAG = "SharedListSortActivity";
    private boolean isDrag = false;
    private DragAdapter mAdapter = null;
    private boolean mCancelIconTasks = false;
    private Context mContext = null;
    private List<ResolveInfo> mCurrentResolveInfos = null;
    private int mDragTargetIndex = -1;
    private DragListView mDragView = null;
    private ListView mListView = null;
    private List<LoadIconTask> mLoadIconTasks = new ArrayList();
    private PackageManager mPackageManager = null;
    private List<Bitmap> mRecycleBitmaps = new ArrayList();
    private SaveRestoreHandler mSaveRestoreHandler = null;
    private List<Integer> mSortedSharedItemIndex = new ArrayList();
    private List<SharedPackageInfo> mSortedSharedItemInfo = new ArrayList();
    private BbkTitleView mTitleView = null;

    private class DragAdapter extends BaseAdapter {
        private static final int COLUMN = 4;
        private static final int NUMBERS = 8;
        private static final int ROW = 2;
        static final int VIEW_TYPE_COUNT = 3;
        static final int VIEW_TYPE_GAP = 1;
        static final int VIEW_TYPE_GRID = 0;
        static final int VIEW_TYPE_TIP = 2;
        private int mGapViewMinHeight = 0;
        private int mGridItemColumnGap = 0;
        private int mGridItemPaddingBottom = 0;
        private int mGridItemPaddingHorizontal = 0;
        private int mGridItemPaddingTop = 0;
        private int mGridItemRowGap = 0;
        private LayoutInflater mLayoutInflater = null;
        private OnLongClickListener mLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View childView) {
                int i;
                SharedListSortActivity.this.isDrag = SharedListSortActivity.DEBUG;
                int childIndex = 0;
                int childCount = SharedListSortActivity.this.mListView.getChildCount();
                for (i = 0; i < childCount; i++) {
                    if (DragAdapter.this.getItemViewType(i + SharedListSortActivity.this.mListView.getFirstVisiblePosition()) == 0) {
                        int index = ((GridView) SharedListSortActivity.this.mListView.getChildAt(i)).indexOfItem(childView);
                        if (index >= 0) {
                            childIndex += index;
                            break;
                        }
                        childIndex += 8;
                    }
                }
                for (i = SharedListSortActivity.this.mListView.getFirstVisiblePosition() - 1; i >= 0; i--) {
                    int i2;
                    if (DragAdapter.this.getItemViewType(i) == 0) {
                        i2 = 8;
                    } else {
                        i2 = 0;
                    }
                    childIndex += i2;
                }
                SharedListSortActivity.this.mDragTargetIndex = childIndex;
                SharedListSortActivity.this.mDragView.startDrag(childView);
                SharedListSortActivity.this.mDragView.invalidate();
                return false;
            }
        };

        class GridView extends LinearLayout {
            private int mItems = 0;
            private int maxColumn = 0;
            private int maxRow = 0;

            GridView(Context context, int column, int row) {
                super(context);
                this.maxColumn = column;
                this.maxRow = row;
                setOrientation(1);
                setBackgroundResource(50463459);
                setPadding(DragAdapter.this.mGridItemPaddingHorizontal, DragAdapter.this.mGridItemPaddingTop, DragAdapter.this.mGridItemPaddingHorizontal, DragAdapter.this.mGridItemPaddingBottom);
            }

            private int columnIndex(int index) {
                int column = index % this.maxColumn;
                return column + column;
            }

            View getOrCreateItemView(int index) {
                int row = index / this.maxColumn;
                int column = columnIndex(index);
                int curRow = getChildCount() - 1;
                View childView;
                if (row <= curRow) {
                    childView = ((LinearLayout) getChildAt(row)).getChildAt(column);
                    childView.setVisibility(0);
                    return childView;
                }
                LinearLayout rowView = new LinearLayout(this.mContext);
                rowView.setOrientation(0);
                LayoutParams lp = new LayoutParams(-1, -2);
                if (curRow >= 0) {
                    lp.topMargin = DragAdapter.this.mGridItemRowGap;
                }
                addView(rowView, lp);
                for (int i = 0; i < this.maxColumn; i++) {
                    if (i != 0) {
                        rowView.addView(new Space(this.mContext), new LayoutParams(DragAdapter.this.mGridItemColumnGap, -2));
                    }
                    View v = DragAdapter.this.mLayoutInflater.inflate(50528350, null);
                    v.setVisibility(4);
                    v.setBackground(null);
                    v.setOnLongClickListener(DragAdapter.this.mLongClickListener);
                    v.setTag(new ViewHolder(v));
                    lp = new LayoutParams(0, -2);
                    lp.weight = 1.0f;
                    rowView.addView(v, lp);
                }
                childView = rowView.getChildAt(column);
                childView.setVisibility(0);
                return childView;
            }

            void hideItemView(int index) {
                int row = index / this.maxColumn;
                int column = columnIndex(index);
                int rowCount = getChildCount();
                int i = row;
                while (i < rowCount) {
                    ViewGroup rowView = (ViewGroup) getChildAt(i);
                    int start = row == i ? column : 0;
                    int columnCount = rowView.getChildCount();
                    for (int j = start; j < columnCount; j++) {
                        rowView.getChildAt(j).setVisibility(4);
                    }
                    i++;
                }
                this.mItems = index;
            }

            int getItemCount() {
                return this.mItems;
            }

            View getItem(int index) {
                int row = index / this.maxColumn;
                return ((ViewGroup) getChildAt(row)).getChildAt(columnIndex(index));
            }

            Point getItemCoordinate(int index) {
                View v = getItem(index);
                ViewGroup rowView = (ViewGroup) v.getParent();
                return new Point(rowView.getLeft() + v.getLeft(), rowView.getTop() + v.getTop());
            }

            int indexOfItem(View v) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    int childIndex = ((ViewGroup) getChildAt(i)).indexOfChild(v);
                    if (childIndex >= 0) {
                        return (i * 4) + (childIndex / 2);
                    }
                }
                return -1;
            }
        }

        class ViewHolder {
            ImageView icon;
            TextView text1;

            ViewHolder(View v) {
                this.text1 = (TextView) v.findViewById(51183760);
                this.icon = (ImageView) v.findViewById(51183693);
            }
        }

        DragAdapter() {
            this.mLayoutInflater = LayoutInflater.from(SharedListSortActivity.this.mContext);
            Resources res = SharedListSortActivity.this.mContext.getResources();
            this.mGridItemPaddingTop = res.getDimensionPixelOffset(51118325);
            this.mGridItemPaddingBottom = res.getDimensionPixelOffset(51118322);
            this.mGridItemPaddingHorizontal = res.getDimensionPixelOffset(51118324);
            this.mGridItemColumnGap = res.getDimensionPixelOffset(51118323);
            this.mGridItemRowGap = res.getDimensionPixelOffset(51118332);
            this.mGapViewMinHeight = SharedListSortActivity.this.mContext.getResources().getDimensionPixelOffset(51118332);
        }

        public int getCount() {
            int gridNum = (int) Math.ceil((double) (((float) SharedListSortActivity.this.mSortedSharedItemIndex.size()) / 8.0f));
            return ((gridNum - 1) + gridNum) + 1;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public int getItemViewType(int position) {
            int i = 0;
            if (position <= 0) {
                return 2;
            }
            if ((position - 1) % 2 != 0) {
                i = 1;
            }
            return i;
        }

        public int getViewTypeCount() {
            return 3;
        }

        public View getView(int position, View convertView, ViewGroup viewParent) {
            switch (getItemViewType(position)) {
                case 0:
                    return getGridView(position, convertView, viewParent);
                case 1:
                    return getGapView(convertView, viewParent);
                case 2:
                    return getTipView(convertView, viewParent);
                default:
                    return new View(SharedListSortActivity.this.mContext);
            }
        }

        private View getTipView(View convertView, ViewGroup parent) {
            if (matchViewType(convertView, 2)) {
                return convertView;
            }
            convertView = this.mLayoutInflater.inflate(50528362, null);
            convertView.setTag(Integer.valueOf(2));
            return convertView;
        }

        private View getGapView(View convertView, ViewGroup viewParent) {
            if (matchViewType(convertView, 1)) {
                return convertView;
            }
            convertView = new View(SharedListSortActivity.this.mContext);
            convertView.setMinimumHeight(this.mGapViewMinHeight);
            convertView.setBackgroundColor(0);
            convertView.setTag(Integer.valueOf(1));
            return convertView;
        }

        private View getGridView(int position, View convertView, ViewGroup viewParent) {
            GridView layoutView;
            if (matchViewType(convertView, 0)) {
                layoutView = (GridView) convertView;
            } else {
                layoutView = new GridView(SharedListSortActivity.this.mContext, 4, 2);
                layoutView.setTag(Integer.valueOf(0));
            }
            int index = position / 2;
            int childIndex = 0;
            int start = index * 8;
            int end = Math.min((index + 1) * 8, SharedListSortActivity.this.mSortedSharedItemIndex.size());
            int i = start;
            while (i < end) {
                ViewHolder holder = (ViewHolder) layoutView.getOrCreateItemView(childIndex).getTag();
                SharedPackageInfo info = (SharedPackageInfo) SharedListSortActivity.this.mSortedSharedItemInfo.get(((Integer) SharedListSortActivity.this.mSortedSharedItemIndex.get(i)).intValue());
                holder.icon.setImageDrawable(info.icon);
                holder.text1.setText(info.label);
                i++;
                childIndex++;
            }
            layoutView.hideItemView(childIndex);
            return layoutView;
        }

        private boolean matchViewType(View view, int type) {
            if (view == null || !(view.getTag() instanceof Integer)) {
                return false;
            }
            return ((Integer) view.getTag()).intValue() == type ? SharedListSortActivity.DEBUG : false;
        }
    }

    private class DragCallback implements Callback {
        /* synthetic */ DragCallback(SharedListSortActivity this$0, DragCallback -this1) {
            this();
        }

        private DragCallback() {
        }

        public boolean isDragable(View v) {
            return v.getTag() instanceof ViewHolder;
        }

        public List<View> acquireDragableViews(Point sp, Point ep) {
            List<View> lists = new ArrayList();
            Point p1 = sp;
            Point p2 = ep;
            if (sp.y > ep.y || (sp.y == ep.y && sp.x > ep.x)) {
                p1 = ep;
                p2 = sp;
            }
            float listOffsetX = (float) (SharedListSortActivity.this.mListView.getLeft() - SharedListSortActivity.this.mListView.getScrollX());
            float listOffsetY = (float) (SharedListSortActivity.this.mListView.getTop() - SharedListSortActivity.this.mListView.getScrollY());
            for (int i = 0; i < SharedListSortActivity.this.mListView.getChildCount(); i++) {
                View v = SharedListSortActivity.this.mListView.getChildAt(i);
                if (v instanceof GridView) {
                    GridView layoutView = (GridView) v;
                    float offsetX = listOffsetX + ((float) layoutView.getLeft());
                    float offsetY = listOffsetY + ((float) layoutView.getTop());
                    for (int j = 0; j < layoutView.getItemCount(); j++) {
                        Point p = layoutView.getItemCoordinate(j);
                        p.x = (int) (((float) p.x) + offsetX);
                        p.y = (int) (((float) p.y) + offsetY);
                        if (isValideDragView(p1, p2, p)) {
                            lists.add(layoutView.getItem(j));
                        }
                    }
                }
            }
            return lists;
        }

        private boolean isValideDragView(Point p1, Point p2, Point p) {
            boolean z = SharedListSortActivity.DEBUG;
            if (p1.y == p2.y) {
                if (p.y != p1.y || p.x < p1.x || p.x > p2.x) {
                    z = false;
                }
                return z;
            } else if (p.y > p1.y && p.y < p2.y) {
                return SharedListSortActivity.DEBUG;
            } else {
                if (SharedListSortActivity.this.mDragView.getLayoutDirection() == 1) {
                    if ((p.y != p1.y || p.x > p1.x) && (p.y != p2.y || p.x < p2.x)) {
                        z = false;
                    }
                    return z;
                }
                if ((p.y != p1.y || p.x < p1.x) && (p.y != p2.y || p.x > p2.x)) {
                    z = false;
                }
                return z;
            }
        }

        public void finishDragAction() {
            SharedListSortActivity.this.handleFinishDragAction();
        }

        public void changeViewPosition(int offset) {
            System.putString(SharedListSortActivity.this.mContext.getContentResolver(), "resolveractivity_isSort", "1");
            SharedListSortActivity.this.handleChangeViewPosition(offset);
        }
    }

    private class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        private int mDoubleInstanceUserId = -10000;
        private SharedPackageInfo spInfo = null;

        LoadIconTask(SharedPackageInfo info, int userId) {
            this.mDoubleInstanceUserId = userId;
            this.spInfo = info;
        }

        protected Drawable doInBackground(Void... params) {
            Drawable drawable;
            ResolveInfo ri = this.spInfo.ri;
            ImageUtil imageUtil = ImageUtil.getInstance(SharedListSortActivity.this.mContext);
            Drawable dr = null;
            boolean canCache = false;
            try {
                if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                    dr = getIcon(SharedListSortActivity.this.mPackageManager.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                }
                int iconRes = ri.getIconResource();
                if (dr == null && iconRes != 0) {
                    dr = getIcon(SharedListSortActivity.this.mPackageManager.getResourcesForApplication(ri.resolvePackageName), iconRes);
                }
            } catch (NameNotFoundException e) {
                Log.e(SharedListSortActivity.TAG, "doInBackground cant' find resolvePackage[" + (ri.resolvePackageName == null ? "null" : ri.resolvePackageName) + "] for package : " + ri.activityInfo.packageName);
            }
            if (dr == null) {
                try {
                    String pkg = ri.getComponentInfo().packageName;
                    String name = ri.getComponentInfo().name;
                    Resources res = SharedListSortActivity.this.mPackageManager.getResourcesForApplication(pkg);
                    TypedValue value = new TypedValue();
                    value.density = Resources.getSystem().getDisplayMetrics().densityDpi;
                    dr = VivoTheme.getAppIconDrawable(res, value, pkg, name);
                } catch (NameNotFoundException e2) {
                }
                if (dr == null) {
                    dr = ri.loadIcon(SharedListSortActivity.this.mPackageManager);
                }
            }
            boolean redraw = SharedListSortActivity.DEBUG;
            try {
                redraw = ((Boolean) VivoTheme.class.getDeclaredMethod("isSystemIcon", new Class[]{String.class}).invoke(null, new Object[]{ri.activityInfo.packageName})).booleanValue() ^ 1;
            } catch (Exception e3) {
                Log.e(SharedListSortActivity.TAG, "draw exception", e3);
            }
            if (redraw) {
                drawable = new BitmapDrawable(SharedListSortActivity.this.mContext.getResources(), imageUtil.createRedrawIconBitmap(dr));
                canCache = SharedListSortActivity.DEBUG;
                Log.d(SharedListSortActivity.TAG, "redraw  " + ri.activityInfo.packageName);
            } else {
                drawable = dr;
            }
            if (ri.targetUserId == this.mDoubleInstanceUserId) {
                drawable = SharedListSortActivity.this.mPackageManager.getUserBadgedIcon(drawable, new UserHandle(ri.targetUserId));
            }
            if (canCache) {
                SharedListSortActivity.this.mRecycleBitmaps.add(((BitmapDrawable) drawable).getBitmap());
            }
            if (drawable == null) {
                return dr;
            }
            return drawable;
        }

        private Drawable getIcon(Resources res, int resId) {
            try {
                return res.getDrawable(resId);
            } catch (NotFoundException e) {
                Log.d(SharedListSortActivity.TAG, "getIcon error", e);
                return null;
            }
        }

        protected void onPostExecute(Drawable result) {
            this.spInfo.icon = result;
            SharedListSortActivity.this.mLoadIconTasks.remove(this);
            if (SharedListSortActivity.this.mCancelIconTasks) {
                SharedListSortActivity.this.recycleIconBitmap();
            } else {
                SharedListSortActivity.this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class SaveRestoreHandler extends Handler {
        SaveRestoreHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ResolverHelper.writeShareSortConfFile(SharedListSortActivity.this.mSortedSharedItemInfo);
                    return;
                case 2:
                    SharedListSortActivity.this.mSortedSharedItemInfo = ResolverHelper.readShareSortConfigFile();
                    SharedListSortActivity.this.finishRestoreData();
                    return;
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle bundle) {
        setTheme(51315061);
        super.onCreate(bundle);
        setContentView(50528361);
        initParams();
        initLayout();
        initData();
    }

    private void initParams() {
        this.mContext = this;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mSaveRestoreHandler = new SaveRestoreHandler(thread.getLooper());
        this.mPackageManager = this.mContext.getPackageManager();
    }

    private void initLayout() {
        getWindow().getDecorView().setBackgroundResource(50462727);
        this.mDragView = (DragListView) findViewById(51183775);
        this.mDragView.setCallback(new DragCallback(this, null));
        this.mListView = this.mDragView.getListView();
        this.mAdapter = new DragAdapter();
        this.mDragView.setAdapter(this.mAdapter);
        this.mTitleView = (BbkTitleView) findViewById(51183774);
        if (FtBuild.getRomVersion() >= 4.5f) {
            this.mTitleView.setCenterText(this.mContext.getString(51249568));
        } else {
            this.mTitleView.setCenterText(this.mContext.getString(51249567));
        }
        this.mTitleView.showRightButton();
        this.mTitleView.setRightButtonText(this.mContext.getString(51249200));
        this.mTitleView.setRightButtonClickListener(new OnClickListener() {
            public void onClick(View v) {
                SharedListSortActivity.this.finish();
            }
        });
    }

    private void initData() {
        this.mCurrentResolveInfos = getIntent().getParcelableArrayListExtra("shareResolveInfo");
        if (this.mCurrentResolveInfos == null) {
            this.mCurrentResolveInfos = new ArrayList();
            Log.e(TAG, "read ResolveInfos empty. do nothing");
        }
        if (this.mCurrentResolveInfos.size() > 0) {
            this.mSaveRestoreHandler.obtainMessage(2).sendToTarget();
        }
    }

    protected void onRestart() {
        super.onRestart();
        this.mSortedSharedItemInfo.clear();
        this.mSortedSharedItemIndex.clear();
        this.mAdapter.notifyDataSetChanged();
        this.mSaveRestoreHandler.obtainMessage(2).sendToTarget();
    }

    protected void onStop() {
        super.onStop();
        recycleIconBitmap();
        this.mCancelIconTasks = DEBUG;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.isDrag) {
            new Thread(new Runnable() {
                public void run() {
                    VivoCollectData mCollectData = VivoCollectData.getInstance(SharedListSortActivity.this.mContext);
                    if (mCollectData.getControlInfo("232")) {
                        HashMap<String, String> params = new HashMap();
                        params.put("order_drag", "1");
                        long time = System.currentTimeMillis();
                        mCollectData.writeData("232", "2322", time, time, 0, 1, params);
                    }
                }
            }).start();
        }
        this.mSaveRestoreHandler.removeMessages(1);
        this.mSaveRestoreHandler.removeMessages(2);
    }

    private void recycleIconBitmap() {
        for (Bitmap bitmap : this.mRecycleBitmaps) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        this.mRecycleBitmaps.clear();
    }

    private void finishRestoreData() {
        buildShareList();
        preLoadIcon();
    }

    private void preLoadIcon() {
        this.mCancelIconTasks = false;
        int doubleInstanceUserId = getDoubleInstanceUserId();
        for (Integer index : this.mSortedSharedItemIndex) {
            SharedPackageInfo info = (SharedPackageInfo) this.mSortedSharedItemInfo.get(index.intValue());
            ResolveInfo ri = info.ri;
            if (ri != null) {
                if (info.label == null) {
                    info.label = ri.loadLabel(this.mPackageManager);
                }
                LoadIconTask at = new LoadIconTask(info, doubleInstanceUserId);
                this.mLoadIconTasks.add(at);
                at.execute(new Void[0]);
            }
        }
    }

    private int getDoubleInstanceUserId() {
        UserManager userManager = (UserManager) getSystemService(RunningInfo.REASON_USER);
        int doubleInstanceUserId = -10000;
        try {
            return ((Integer) userManager.getClass().getMethod("getDoubleAppUserId", new Class[0]).invoke(userManager, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "get DoubleInstance UserId Error.", e);
            return doubleInstanceUserId;
        }
    }

    private void buildShareList() {
        ResolveInfo ri;
        int resolveCount = this.mCurrentResolveInfos.size();
        int resolveIndex = 0;
        int itemCount = this.mSortedSharedItemInfo.size();
        int itemIndex = 0;
        while (resolveIndex < resolveCount) {
            ri = (ResolveInfo) this.mCurrentResolveInfos.get(resolveIndex);
            boolean find = false;
            while (itemIndex < itemCount) {
                SharedPackageInfo spi = (SharedPackageInfo) this.mSortedSharedItemInfo.get(itemIndex);
                if (isSharedPackageInfoConsistent(spi, ri)) {
                    spi.ri = ri;
                    find = DEBUG;
                    this.mSortedSharedItemIndex.add(Integer.valueOf(itemIndex));
                    break;
                }
                itemIndex++;
            }
            if (!find) {
                break;
            }
            resolveIndex++;
        }
        while (resolveIndex < resolveCount) {
            ri = (ResolveInfo) this.mCurrentResolveInfos.get(resolveIndex);
            ActivityInfo ai = ri.activityInfo;
            SharedPackageInfo info = new SharedPackageInfo();
            info.packageName = ai.packageName;
            info.name = ai.name;
            info.ri = ri;
            setSharedPackageInfoType(info);
            this.mSortedSharedItemInfo.add(info);
            this.mSortedSharedItemIndex.add(Integer.valueOf(this.mSortedSharedItemInfo.size() - 1));
            resolveIndex++;
        }
    }

    private boolean isSharedPackageInfoConsistent(SharedPackageInfo info, ResolveInfo ri) {
        try {
            return ((Boolean) info.getClass().getMethod("isConsistent", new Class[]{Context.class, ResolveInfo.class}).invoke(info, new Object[]{this, ri})).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "isConsistent Error : " + ri, e);
            ActivityInfo ai = ri.activityInfo;
            return info.packageName.equals(ai.packageName) ? info.name.equals(ai.name) : false;
        }
    }

    private void setSharedPackageInfoType(SharedPackageInfo info) {
        try {
            info.getClass().getMethod("setType", new Class[]{Context.class}).invoke(info, new Object[]{this});
        } catch (Exception e) {
            Log.e(TAG, "setType Error : " + info.packageName + "/" + info.name, e);
        }
    }

    private void handleChangeViewPosition(int offset) {
        if (offset == 0) {
            Log.d(TAG, "view position don't change. ignore");
            return;
        }
        int indexTo = this.mDragTargetIndex + offset;
        if (indexTo >= 0 && indexTo < this.mSortedSharedItemIndex.size()) {
            int step = offset > 0 ? 1 : -1;
            int index = this.mDragTargetIndex;
            SharedPackageInfo info = (SharedPackageInfo) this.mSortedSharedItemInfo.get(((Integer) this.mSortedSharedItemIndex.get(this.mDragTargetIndex)).intValue());
            while (index != indexTo) {
                this.mSortedSharedItemInfo.set(((Integer) this.mSortedSharedItemIndex.get(index)).intValue(), (SharedPackageInfo) this.mSortedSharedItemInfo.get(((Integer) this.mSortedSharedItemIndex.get(index + step)).intValue()));
                index += step;
            }
            this.mSortedSharedItemInfo.set(((Integer) this.mSortedSharedItemIndex.get(index)).intValue(), info);
            this.mDragTargetIndex = indexTo;
        }
        Log.d(TAG, "handleChangeViewPosition : " + offset + "   " + this.mDragTargetIndex + "   " + indexTo);
    }

    private void handleFinishDragAction() {
        this.mSaveRestoreHandler.obtainMessage(1).sendToTarget();
        this.mAdapter.notifyDataSetChanged();
    }
}
