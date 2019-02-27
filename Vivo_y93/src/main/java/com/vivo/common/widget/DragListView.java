package com.vivo.common.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.HashMap;
import java.util.Map;

public class DragListView extends DragLayout {
    private static final boolean DEBUG = true;
    private static final String TAG = "DragListView";
    private int SCROLL_UP_DOWN_OFFSET = 10;
    private ListAdapter mAdapter = null;
    private Context mContext = null;
    private int mDragViewHeight = 0;
    private Point mDragViewPos = new Point();
    private int mDragViewPosDepth = 0;
    private Map<Integer, Integer> mDragViewPosMap = new HashMap();
    private int mDragViewPosition = -1;
    private boolean mDragViewRecyled = false;
    private int mDragViewWidth = 0;
    private Map<Integer, Integer> mListItemOffset = new HashMap();
    private ListView mListView = null;
    private AutoScroller mScroller = null;

    private class AutoScroller {
        private int AUTO_SCROLL_INTERVAL = 10;
        private int AUTO_SCROLL_STEP = 6;
        private final int HANDLER_AUTO_SCROLL = 1;
        private final int SCROLL_DIR_DOWN = 2;
        private final int SCROLL_DIR_UP = 1;
        private Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    int dir = msg.arg1;
                    AutoScroller autoScroller;
                    switch (dir) {
                        case 1:
                            autoScroller = AutoScroller.this;
                            autoScroller.mScrollOffset = autoScroller.mScrollOffset + (-AutoScroller.this.AUTO_SCROLL_STEP);
                            autoScroller = AutoScroller.this;
                            autoScroller.mScrollPos = autoScroller.mScrollPos + ((float) (-AutoScroller.this.AUTO_SCROLL_STEP));
                            DragListView.this.mListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 2, 0.0f, AutoScroller.this.mScrollPos, 0));
                            break;
                        case 2:
                            autoScroller = AutoScroller.this;
                            autoScroller.mScrollOffset = autoScroller.mScrollOffset + AutoScroller.this.AUTO_SCROLL_STEP;
                            autoScroller = AutoScroller.this;
                            autoScroller.mScrollPos = autoScroller.mScrollPos + ((float) AutoScroller.this.AUTO_SCROLL_STEP);
                            DragListView.this.mListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 2, 0.0f, AutoScroller.this.mScrollPos, 0));
                            break;
                    }
                    if ((dir == 1 && AutoScroller.this.canScrollUp()) || (dir == 2 && AutoScroller.this.canScrollDown())) {
                        AutoScroller.this.mHandler.sendMessageDelayed(AutoScroller.this.mHandler.obtainMessage(1, dir, 0), (long) AutoScroller.this.AUTO_SCROLL_INTERVAL);
                    }
                }
            }
        };
        private int mScrollOffset = 0;
        private float mScrollPos = 0.0f;
        private boolean mScrolling = false;

        AutoScroller() {
            this.AUTO_SCROLL_STEP = (int) (((float) this.AUTO_SCROLL_STEP) * DragListView.this.mContext.getResources().getDisplayMetrics().density);
        }

        void startScroll(int dir) {
            if (!this.mScrolling) {
                if (dir == 1 && canScrollUp()) {
                    this.mScrollPos = (float) DragListView.this.mListView.getHeight();
                } else if (dir == 2 && canScrollDown()) {
                    this.mScrollPos = 0.0f;
                } else {
                    return;
                }
                this.mScrolling = DragListView.DEBUG;
                DragListView.this.mListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 0, 0.0f, this.mScrollPos, 0));
                this.mHandler.obtainMessage(1, dir, 0).sendToTarget();
            }
        }

        void stopScroll() {
            if (this.mScrolling) {
                this.mHandler.removeMessages(1);
                DragListView.this.mListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 1, 0.0f, this.mScrollPos, 0));
                this.mScrolling = false;
            }
        }

        boolean isScrolling() {
            return this.mScrolling;
        }

        int getScrollOffset() {
            return this.mScrollOffset;
        }

        void reset() {
            stopScroll();
            this.mScrollOffset = 0;
            this.mScrollPos = 0.0f;
        }

        private boolean canScrollUp() {
            if (DragListView.this.mListView.getLastVisiblePosition() == DragListView.this.mListView.getAdapter().getCount() - 1) {
                return DragListView.this.mListView.getChildAt(DragListView.this.mListView.getChildCount() + -1).getBottom() > DragListView.this.mListView.getHeight() ? DragListView.DEBUG : false;
            } else {
                return DragListView.DEBUG;
            }
        }

        private boolean canScrollDown() {
            if (DragListView.this.mListView.getFirstVisiblePosition() == 0) {
                return DragListView.this.mListView.getChildAt(0).getTop() < 0 ? DragListView.DEBUG : false;
            } else {
                return DragListView.DEBUG;
            }
        }
    }

    private class WrapperAdapter extends BaseAdapter {
        private ListAdapter adapter = null;

        WrapperAdapter(ListAdapter adapter) {
            this.adapter = adapter;
            adapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    super.onChanged();
                    WrapperAdapter.this.notifyDataSetChanged();
                }

                public void onInvalidated() {
                    super.onInvalidated();
                    WrapperAdapter.this.notifyDataSetInvalidated();
                }
            });
        }

        public int getCount() {
            return this.adapter.getCount();
        }

        public Object getItem(int position) {
            return this.adapter.getItem(position);
        }

        public long getItemId(int position) {
            return this.adapter.getItemId(position);
        }

        public int getItemViewType(int position) {
            return this.adapter.getItemViewType(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = this.adapter.getView(position, convertView, parent);
            if (DragListView.this.mDragViewPosition == position) {
                View v = DragListView.this.getDragViewByPosMap(rootView);
                if (v != null) {
                    DragListView.this.mDragView = v;
                    v.setVisibility(4);
                }
            }
            return rootView;
        }
    }

    public DragListView(Context context) {
        super(context);
        initParams();
        initLayout();
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams();
    }

    private void initParams() {
        this.mContext = getContext();
        this.mScroller = new AutoScroller();
        this.SCROLL_UP_DOWN_OFFSET = (int) (((float) this.SCROLL_UP_DOWN_OFFSET) * this.mContext.getResources().getDisplayMetrics().density);
    }

    protected void onFinishInflate() {
        View v = findViewById(16908298);
        if (v == null) {
            throw new IllegalArgumentException("can't find view named : list");
        } else if (v instanceof ListView) {
            this.mListView = (ListView) v;
        } else {
            throw new IllegalArgumentException("view with id[list] must be child view of ListView");
        }
    }

    private void initLayout() {
        this.mListView = new ListView(this.mContext);
        addView(this.mListView, new LayoutParams(-1, -1));
    }

    public void setAdapter(ListAdapter adapter) {
        this.mAdapter = adapter;
        this.mListView.setAdapter(new WrapperAdapter(adapter));
    }

    public ListView getListView() {
        return this.mListView;
    }

    protected boolean shouldAnimateViews() {
        return this.mScroller.isScrolling() ^ 1;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                clearDragInformation();
                this.mScroller.reset();
                break;
            case 2:
                scrollUpDownIfNeed(event);
                break;
        }
        return ret;
    }

    private void clearDragInformation() {
        this.mDragViewRecyled = false;
        this.mDragViewPosition = -1;
        this.mDragViewPosDepth = 0;
        this.mDragViewHeight = 0;
        this.mDragViewWidth = 0;
        Point point = this.mDragViewPos;
        this.mDragViewPos.y = 0;
        point.x = 0;
        this.mDragViewPosMap.clear();
        this.mListItemOffset.clear();
    }

    private void scrollUpDownIfNeed(MotionEvent event) {
        float y = event.getY();
        float top = (float) this.mListView.getTop();
        float bottom = (float) this.mListView.getBottom();
        if (this.mCurrentState == State.TOUCH_MOVE) {
            if (Math.abs(y - top) > ((float) this.SCROLL_UP_DOWN_OFFSET) && Math.abs(y - bottom) > ((float) this.SCROLL_UP_DOWN_OFFSET)) {
                this.mScroller.stopScroll();
            } else if (obtainDragViewInformation()) {
                int dir;
                if (this.mDragViewPosition < this.mListView.getFirstVisiblePosition() || this.mDragViewPosition > this.mListView.getLastVisiblePosition()) {
                    this.mDragView = getDragViewByPosMap(this.mAdapter.getView(this.mDragViewPosition, null, null));
                    this.mDragView.setVisibility(4);
                    ViewGroup parent = (ViewGroup) this.mDragView.getParent();
                    if (parent != null) {
                        parent.removeView(this.mDragView);
                    }
                }
                if (Math.abs(y - top) <= ((float) this.SCROLL_UP_DOWN_OFFSET)) {
                    this.mScroller.getClass();
                    dir = 2;
                } else {
                    this.mScroller.getClass();
                    dir = 1;
                }
                this.mScroller.getClass();
                int pos;
                View v;
                Point endP;
                Point startP;
                if (dir != 1 || this.mDragViewPosition > this.mListView.getFirstVisiblePosition()) {
                    this.mScroller.getClass();
                    if (dir == 2 && this.mDragViewPosition >= this.mListView.getLastVisiblePosition()) {
                        pos = this.mListView.getLastVisiblePosition();
                        v = this.mListView.getChildAt(this.mListView.getChildCount() - 1);
                        if (this.mListItemOffset.get(Integer.valueOf(pos)) == null) {
                            endP = new Point(this.mListView.getLeft(), this.mListView.getTop() + v.getTop());
                            if (pos == this.mDragViewPosition) {
                                startP = obtainViewPosOnCurrentView(this.mDragView);
                            } else {
                                startP = new Point(v.getRight() + this.mListView.getLeft(), v.getBottom() + this.mListView.getTop());
                            }
                            this.mListItemOffset.put(Integer.valueOf(pos), Integer.valueOf(this.mCallback.acquireDragableViews(startP, endP).size()));
                        }
                    }
                } else {
                    pos = this.mListView.getFirstVisiblePosition();
                    v = this.mListView.getChildAt(0);
                    if (this.mListItemOffset.get(Integer.valueOf(pos)) == null) {
                        endP = new Point(v.getRight() + this.mListView.getLeft(), v.getBottom() + this.mListView.getTop());
                        if (this.mDragViewPosition == pos) {
                            startP = obtainViewPosOnCurrentView(this.mDragView);
                        } else {
                            startP = new Point(this.mListView.getLeft() + v.getLeft(), this.mListView.getTop() + v.getTop());
                        }
                        this.mListItemOffset.put(Integer.valueOf(pos), Integer.valueOf(this.mCallback.acquireDragableViews(startP, endP).size()));
                    }
                }
                this.mScroller.startScroll(dir);
            }
        }
    }

    private boolean obtainDragViewInformation() {
        if (this.mDragViewPosition >= 0) {
            return DEBUG;
        }
        this.mDragViewHeight = this.mDragView.getHeight();
        this.mDragViewWidth = this.mDragView.getWidth();
        this.mDragViewPosDepth = 0;
        View preView = this.mDragView;
        while (preView != null) {
            this.mDragViewPosition = this.mListView.indexOfChild(preView);
            if (this.mDragViewPosition >= 0) {
                this.mDragViewPosition += this.mListView.getFirstVisiblePosition();
                break;
            }
            View parent = (ViewGroup) preView.getParent();
            this.mDragViewPosDepth++;
            this.mDragViewPosMap.put(Integer.valueOf(this.mDragViewPosDepth), Integer.valueOf(parent.indexOfChild(preView)));
            preView = parent;
        }
        if (this.mDragViewPosition < 0) {
            Log.e(TAG, "invalidate dragView position");
            return false;
        }
        this.mDragViewPos = obtainViewPosOnCurrentView(this.mDragView);
        Log.d(TAG, "DragViewHeight : " + this.mDragViewHeight + "  DragViewWidth : " + this.mDragViewWidth + "\nDragViewPos   : " + this.mDragViewPos + "\nDragViewPosition : " + this.mDragViewPosition + "  DragViewPosDepth : " + this.mDragViewPosDepth);
        return DEBUG;
    }

    protected Point obtainDragViewCurrentPos() {
        Point p;
        int lastVisiblePos = this.mListView.getLastVisiblePosition();
        int firstVisiblePos = this.mListView.getFirstVisiblePosition();
        if (this.mDragViewPosition < 0 || (this.mDragViewPosition >= firstVisiblePos && this.mDragViewPosition <= lastVisiblePos)) {
            p = obtainViewPosOnCurrentView(this.mDragView);
        } else {
            p = new Point(this.mDragViewPos.x, this.mDragViewPos.y + this.mScroller.getScrollOffset());
        }
        Log.d(TAG, "obtainDragViewCurrentPos : " + p + "\nfirstVisiblePos  : " + firstVisiblePos + "   lastVisiblePos : " + lastVisiblePos + "\nDragViewPosition : " + this.mDragViewPosition);
        return p;
    }

    protected Point obtainDragViewAnimatePos() {
        Point p;
        int lastVisiblePos = this.mListView.getLastVisiblePosition();
        int firstVisiblePos = this.mListView.getFirstVisiblePosition();
        if (this.mDragViewPosition < 0 || (this.mDragViewPosition >= firstVisiblePos && this.mDragViewPosition <= lastVisiblePos)) {
            p = obtainViewPosOnCurrentView(this.mDragView);
        } else if (this.mDragViewPosition <= firstVisiblePos) {
            p = new Point(this.mListView.getRight() - this.mDragViewWidth, Math.min(this.mListView.getTop() - this.mDragViewHeight, this.mListView.getChildAt(0).getTop() + this.mListView.getTop()));
        } else {
            p = new Point(this.mListView.getLeft(), Math.max(this.mListView.getBottom() + this.mDragViewHeight, this.mListView.getChildAt(this.mListView.getChildCount() - 1).getBottom() + this.mListView.getTop()));
        }
        Log.d(TAG, "obtainDragViewAnimatePos : " + p + "\nfirstVisiblePos  : " + firstVisiblePos + "   lastVisiblePosition : " + lastVisiblePos + "\nDragViewPosition : " + this.mDragViewPosition);
        return p;
    }

    int obtainDragViewOffset() {
        int lastPos = this.mListView.getLastVisiblePosition();
        int firstPos = this.mListView.getFirstVisiblePosition();
        int viewOffset = super.obtainDragViewOffset();
        if (this.mDragViewPosition < 0) {
            return viewOffset;
        }
        int offset = 0;
        int i;
        if (this.mDragViewPosition < firstPos) {
            for (i = firstPos - 1; i >= this.mDragViewPosition; i--) {
                Log.d("jpf", "offset : " + i + "  " + offset);
                if (this.mListItemOffset.containsKey(Integer.valueOf(i))) {
                    offset += ((Integer) this.mListItemOffset.get(Integer.valueOf(i))).intValue();
                }
            }
        } else if (this.mDragViewPosition > lastPos) {
            for (i = lastPos + 1; i <= this.mDragViewPosition; i++) {
                if (this.mListItemOffset.containsKey(Integer.valueOf(i))) {
                    offset += -((Integer) this.mListItemOffset.get(Integer.valueOf(i))).intValue();
                }
            }
        }
        Log.d("jpf", "offset : " + offset);
        Log.d("jpf", "viewOffset : " + viewOffset);
        return viewOffset + offset;
    }

    void finishAnimateViews(IAnimation anim) {
        super.finishAnimateViews(anim);
        if (this.mCurrentState == State.TOUCH_MOVE) {
            clearDragInformation();
        }
    }

    private View getDragViewByPosMap(View rootView) {
        View targetView = rootView;
        for (int i = 0; i < this.mDragViewPosDepth; i++) {
            if (!(targetView instanceof ViewGroup)) {
                return null;
            }
            targetView = ((ViewGroup) targetView).getChildAt(((Integer) this.mDragViewPosMap.get(Integer.valueOf(this.mDragViewPosDepth - i))).intValue());
        }
        return targetView;
    }
}
