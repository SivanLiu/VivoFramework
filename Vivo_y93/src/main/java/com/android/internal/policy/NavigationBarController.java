package com.android.internal.policy;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPostDrawListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ScrollView;
import com.android.internal.R;
import com.android.internal.os.RegionalizationEnvironment;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NavigationBarController {
    static boolean DEBUG = SystemProperties.getBoolean("persist.navcolor.debug", false);
    static boolean DEBUG_DRAW = SystemProperties.getBoolean("persist.navcolor.draw", false);
    static boolean DEBUG_TRACE = SystemProperties.getBoolean("persist.navcolor.trace", false);
    static boolean IMMERSIVE_PERFORMANCE_OPTIMIZE = SystemProperties.getBoolean("persist.navcolor.nativeoptimize", true);
    static boolean JUDGE_VIEW_DIRTY = SystemProperties.getBoolean("persist.navcolor.judgeviewdirty", true);
    private static final int MIN_BAR_HEIGHT_NORMAL = 28;
    private static final int MIN_BAR_HEIGHT_WHEN_SCROLL = 4;
    private static final int MIN_COLOR_ALPHA = 255;
    private static final int MSG_REMOVE_PRE_DRAW_LISTENER = 1;
    private static final int MSG_UPDATE_NAVIGATION_BAR = 2;
    private static final long REMOVE_PRE_DRAW_LISTENER_TIME_INTERNAL = 2147483647L;
    static int SAMPLE_COLOR_COUNT = SystemProperties.getInt("persist.navcolor.colorcount", 24);
    private static final int SLIDE_TYPE_LEFT = 1;
    private static final int SLIDE_TYPE_NONE = 0;
    private static final int SLIDE_TYPE_RIGHT = 2;
    private static final int UPDATE_NAVIGATION_BAR_TIME_INTERNAL = 0;
    private static final int VERTICAL_GAP_STEP = 10;
    private static final int VERTICAL_MAX_GAP = 25;
    private static final int VERTICAL_MID_GAP = 15;
    private static final int VERTICAL_MIN_GAP = 5;
    private ArrayList<MaskLayerInfo> mAllInfoList = new ArrayList();
    private ArrayList<MaskLayerInfo> mAllLayerInfoList = new ArrayList();
    private ArrayList<View> mBottomBarViewList = new ArrayList();
    private int mBottomInset;
    private final NavigationBarColorCallback mCallback;
    private int mColor = 0;
    private int mCurrSlideType = 0;
    private int mCurrVerticalGap = 15;
    private int mDecorHeight;
    private final View mDecorView;
    private int mDecorWidth;
    private float mDensity;
    private int mDockBottomEdge;
    private int mDockTopEdge;
    private View mDrawerLayout = null;
    private ArrayList<Point> mFilledRegionList = new ArrayList();
    private Point mInitUnFilledRegion = new Point();
    private boolean mIsBottomFullBar = false;
    private boolean mIsDebugSpecialView = false;
    private boolean mIsExistSpecialView = false;
    private boolean mIsNotAdjustVisibleBoundWhenAnim = false;
    private ArrayList<MaskLayerInfo> mLastAllLayerInfoList = new ArrayList();
    private ArrayList<MaskLayerInfo> mLastMaskLayerInfoList = new ArrayList();
    private String mLogTag = NavigationBarController.class.getSimpleName();
    private ArrayList<MaskLayerInfo> mMaskLayerInfoList = new ArrayList();
    private float[] mMatrixVals = new float[9];
    private int mMaxBarHeight = 80;
    private int mMaxSlideDrawerWidth = 320;
    private boolean mMeetNavigationBar = false;
    private int mMinBarHeight = 28;
    private int mMinBarItemWidth = 40;
    private int mMinFullBottomHeight = 80;
    private int mMinSlideDrawerWidth = 180;
    private int mNavBgViewColor;
    private View mNavigationBarTarget = null;
    private View mRecoredScrolledView = null;
    private final Rect mRect = new Rect();
    private ArrayList<MaskLayerInfo> mResultInfoList = new ArrayList();
    private int mTargetColor = 0;
    private Rect mTempAnimBounds = new Rect();
    private Rect mTempAnimRect = new Rect();
    private Transformation mTempAnimTransform = new Transformation();
    private final Rect mTempBounds = new Rect();
    private final Paint mTempPaint = new Paint();
    private int mTopInset;
    private ArrayList<Point> mUnFilledRegionList = new ArrayList();
    private int mUpDockBottomEdge;
    private UpdateNavigationBarHandler mUpdateNavigationBarHandler = null;
    private long mUpdateNavigationBarTimestamp = 0;
    private final Rect mViewBounds = new Rect();
    private ViewTreePostDrawListener mViewTreePostDrawListener = null;

    interface NavigationBarColorCallback {
        void updateColor(int i, int i2, boolean z);
    }

    private static class UpdateNavigationBarHandler extends Handler {
        private WeakReference<NavigationBarController> mControllerReference = null;

        public UpdateNavigationBarHandler(NavigationBarController controller) {
            this.mControllerReference = new WeakReference(controller);
        }

        public void handleMessage(Message msg) {
            if (this.mControllerReference != null) {
                NavigationBarController controller = (NavigationBarController) this.mControllerReference.get();
                if (controller != null) {
                    if (NavigationBarController.DEBUG) {
                        Log.i(controller.mLogTag, "test measure color Controller NavigationBar UpdateNavigationBarHandler controller: " + controller + ", msg: " + msg.what);
                    }
                    switch (msg.what) {
                        case 1:
                            View decorView = controller.getDecorView();
                            if (decorView != null) {
                                ViewTreeObserver observer = decorView.getViewTreeObserver();
                                if (observer != null) {
                                    observer.removeOnPostDrawListener(controller.getPostDrawListener());
                                    break;
                                }
                            }
                            break;
                        case 2:
                            controller.updateNavigationBarTarget();
                            long currTime = SystemClock.uptimeMillis();
                            controller.setTimestamp(currTime);
                            if (NavigationBarController.DEBUG) {
                                Log.i(controller.mLogTag, "test measure color Controller NavigationBar UpdateNavigationBarHandler msg: " + msg.what + ", currTime: " + currTime);
                                break;
                            }
                            break;
                    }
                }
            }
        }
    }

    private static class ViewTreePostDrawListener implements OnPostDrawListener {
        private WeakReference<NavigationBarController> mControllerReference = null;

        public ViewTreePostDrawListener(NavigationBarController controller) {
            this.mControllerReference = new WeakReference(controller);
        }

        public void onPostDraw(Rect dirty) {
            if (this.mControllerReference != null) {
                NavigationBarController controller = (NavigationBarController) this.mControllerReference.get();
                if (controller != null) {
                    if (NavigationBarController.DEBUG) {
                        Log.i(controller.mLogTag, "test measure color Controller NavigationBar ViewTreePreDrawListener controller: " + controller + ", dirty = " + dirty);
                    }
                    int decorHeight = controller.getDecorHeight();
                    if (decorHeight > 0 && decorHeight != controller.getDecorView().getHeight()) {
                        controller.initDockEdges();
                    }
                    if ((dirty != null && dirty.bottom >= controller.getDockBottomEdge() && dirty.top < dirty.bottom) || controller.getNavigationBarTarget() == null) {
                        long currTime = SystemClock.uptimeMillis();
                        long timestamp = controller.getTimestamp();
                        long delayed = 0 - (currTime - timestamp);
                        if (NavigationBarController.DEBUG) {
                            Log.i(controller.mLogTag, "test measure color Controller NavigationBar ViewTreePreDrawListener currTime: " + controller + ", currTime: " + currTime + ", timestamp: " + timestamp + ", delayed:" + delayed);
                        }
                        UpdateNavigationBarHandler handler = controller.getUpdateHandler();
                        if (delayed < 0) {
                            if (handler != null) {
                                handler.removeMessages(2);
                            }
                            controller.updateNavigationBarTarget();
                            controller.setTimestamp(currTime);
                        } else if (handler != null) {
                            handler.removeMessages(2);
                            handler.sendEmptyMessageDelayed(2, delayed);
                        }
                    }
                }
            }
        }
    }

    public NavigationBarController(View decor, NavigationBarColorCallback callback) {
        this.mDecorView = decor;
        this.mCallback = callback;
        this.mDensity = decor.getResources().getDisplayMetrics().density;
        this.mMinBarHeight = (int) (((float) this.mMinBarHeight) * this.mDensity);
        this.mMaxBarHeight = (int) (((float) this.mMaxBarHeight) * this.mDensity);
        this.mMinBarItemWidth = (int) (((float) this.mMinBarItemWidth) * this.mDensity);
        this.mMinFullBottomHeight = (int) (((float) this.mMinFullBottomHeight) * this.mDensity);
        this.mMinSlideDrawerWidth = (int) (((float) this.mMinSlideDrawerWidth) * this.mDensity);
        this.mMaxSlideDrawerWidth = (int) (((float) this.mMaxSlideDrawerWidth) * this.mDensity);
        this.mUpdateNavigationBarHandler = new UpdateNavigationBarHandler(this);
        this.mViewTreePostDrawListener = new ViewTreePostDrawListener(this);
    }

    private void updateNavigationBarColor(int color, int bgColor, boolean animate, boolean force) {
        if (this.mColor != color || force) {
            this.mColor = color;
            this.mCallback.updateColor(color, bgColor, animate);
        }
    }

    void insetChanged(int left, int top, int right, int bottom) {
        if (DEBUG) {
            Log.i(this.mLogTag, "inset change, [" + left + "," + top + "," + right + "," + bottom + "]");
        }
        this.mTopInset = top;
        this.mBottomInset = bottom;
    }

    void addPostDrawListener() {
        if (this.mDecorView != null) {
            if (DEBUG) {
                Log.i(this.mLogTag, "test measure color Controller NavigationBar addPreDrawListener mUpdateNavigationBarTimestamp: " + this.mUpdateNavigationBarTimestamp);
            }
            ViewTreeObserver observer = this.mDecorView.getViewTreeObserver();
            if (observer != null) {
                observer.removeOnPostDrawListener(this.mViewTreePostDrawListener);
                observer.addOnPostDrawListener(this.mViewTreePostDrawListener);
            }
            if (this.mUpdateNavigationBarHandler != null) {
                this.mUpdateNavigationBarHandler.removeCallbacksAndMessages(null);
                this.mUpdateNavigationBarHandler.sendEmptyMessageDelayed(1, REMOVE_PRE_DRAW_LISTENER_TIME_INTERNAL);
            }
        }
    }

    void updateNavigationBarTarget() {
        String str = null;
        long start = 0;
        if (DEBUG_TRACE) {
            Trace.traceBegin(8, "updateTarget");
            start = SystemClock.uptimeMillis();
        }
        boolean animate = false;
        boolean force = false;
        View target = findNavigationBarTarget();
        if (DEBUG) {
            String str2 = this.mLogTag;
            StringBuilder append = new StringBuilder().append("Find NavigationBar target , view = ");
            if (target != null) {
                str = getViewDebugInfo(target) + "[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]";
            }
            Log.i(str2, append.append(str).toString());
        }
        if (!(target == null || target == this.mNavigationBarTarget)) {
            if (DEBUG) {
                Log.i(this.mLogTag, "NavigationBar target changed to " + getViewDebugInfo(target) + "[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]" + " from " + getViewDebugInfo(this.mNavigationBarTarget) + ", mTargetColor = " + Integer.toHexString(this.mTargetColor));
            }
            if (this.mNavigationBarTarget != null) {
                animate = true;
            } else {
                force = true;
            }
            this.mNavigationBarTarget = target;
        }
        this.mNavBgViewColor = getNavBgViewColor();
        updateNavigationBarColor(this.mTargetColor, this.mNavBgViewColor, animate, force);
        if (DEBUG_DRAW) {
            this.mDecorView.invalidate();
        }
        if (DEBUG_TRACE) {
            Log.i(this.mLogTag, "Update NavigationBar target cost: " + (SystemClock.uptimeMillis() - start) + "ms");
            Trace.traceEnd(8);
        }
    }

    private int getNavBgViewColor() {
        int size = this.mAllLayerInfoList.size();
        if (IMMERSIVE_PERFORMANCE_OPTIMIZE) {
            int lastSize = this.mLastAllLayerInfoList.size();
            if (DEBUG) {
                Log.i(this.mLogTag, "NavigationBar target getNavBgViewColor size: " + size + ", lastSize: " + lastSize);
            }
            int i;
            if (lastSize <= 0) {
                for (i = size - 1; i >= 0; i--) {
                    this.mLastAllLayerInfoList.add((MaskLayerInfo) this.mAllLayerInfoList.get(i));
                }
            } else if (lastSize != size) {
                this.mLastAllLayerInfoList.clear();
                for (i = size - 1; i >= 0; i--) {
                    this.mLastAllLayerInfoList.add((MaskLayerInfo) this.mAllLayerInfoList.get(i));
                }
            } else {
                boolean isChangeMasks = isMasksChanged(this.mAllLayerInfoList, this.mLastAllLayerInfoList, size);
                if (DEBUG) {
                    Log.i(this.mLogTag, "NavigationBar target getNavBgViewColor isChangeMasks: " + isChangeMasks);
                }
                if (!isChangeMasks) {
                    return this.mNavBgViewColor;
                }
                this.mLastAllLayerInfoList.clear();
                for (i = size - 1; i >= 0; i--) {
                    this.mLastAllLayerInfoList.add((MaskLayerInfo) this.mAllLayerInfoList.get(i));
                }
            }
        }
        if (size > 0) {
            DrawableUtils.blendColors(this.mAllLayerInfoList, this.mAllInfoList);
            if (this.mAllInfoList.size() > 0) {
                MaskLayerInfo info = measureSuitableViewFromResultList(this.mAllInfoList);
                if (info != null) {
                    return info.maskColor;
                }
            }
        }
        return this.mTargetColor;
    }

    private View findNavigationBarTarget() {
        initDockEdges();
        initParameters();
        if (DEBUG) {
            Log.i(this.mLogTag, "mDockTopEdge = " + this.mDockTopEdge + ", mDockBottomEdge = " + this.mDockBottomEdge + ", mUpDockBottomEdge: " + this.mUpDockBottomEdge + ", mDecorHeight: " + this.mDecorHeight);
        }
        judgeAdjustVisibleBoundWhenAnim(this.mDecorView);
        View target = findTraversal(this.mDecorView);
        if (target != this.mDecorView || this.mTargetColor != 0 || (this.mIsNotAdjustVisibleBoundWhenAnim ^ 1) == 0 || this.mCurrVerticalGap >= 25) {
            return target;
        }
        this.mCurrVerticalGap += 10;
        initParameters();
        View resultView = findTraversal(this.mDecorView);
        this.mCurrVerticalGap = 15;
        return resultView;
    }

    private void initDockEdges() {
        this.mDecorWidth = this.mDecorView.getWidth();
        this.mDecorHeight = this.mDecorView.getHeight();
        this.mDockTopEdge = this.mTopInset + 5;
        this.mDockBottomEdge = (this.mDecorHeight - this.mBottomInset) - 5;
    }

    private void initParameters() {
        this.mUpDockBottomEdge = (this.mDecorHeight - this.mBottomInset) - this.mCurrVerticalGap;
        if (this.mMaskLayerInfoList != null) {
            this.mMaskLayerInfoList.clear();
            this.mAllLayerInfoList.clear();
        }
        if (this.mUnFilledRegionList != null) {
            this.mUnFilledRegionList.clear();
            this.mInitUnFilledRegion.set(0, this.mDecorWidth);
            this.mUnFilledRegionList.add(this.mInitUnFilledRegion);
        }
        if (this.mFilledRegionList != null) {
            this.mFilledRegionList.clear();
        }
        if (this.mResultInfoList != null) {
            this.mResultInfoList.clear();
            this.mAllInfoList.clear();
        }
        if (this.mBottomBarViewList != null) {
            this.mBottomBarViewList.clear();
        }
        this.mRecoredScrolledView = null;
        this.mIsExistSpecialView = false;
        this.mMeetNavigationBar = false;
        this.mIsNotAdjustVisibleBoundWhenAnim = false;
    }

    private void judgeAdjustVisibleBoundWhenAnim(View view) {
        if (view instanceof ViewGroup) {
            int i;
            View child;
            ViewGroup group = (ViewGroup) view;
            int childCount = group.getChildCount();
            int validChildCount = 0;
            int validChildIndex = 0;
            for (i = 0; i < childCount; i++) {
                child = group.getChildAt(i);
                if (child != null && (child instanceof ViewGroup) && child.getVisibility() == 0 && getViewAlpha(child, false) != 0.0f) {
                    validChildCount++;
                    validChildIndex = i;
                }
            }
            if (DEBUG) {
                Log.i(this.mLogTag, "judgeAdjustVisibleBoundWhenAnim view: " + getViewDebugInfo(view) + ", childCount: " + childCount + ", validChildCount: " + validChildCount + ", validChildIndex: " + validChildIndex);
            }
            if (validChildCount != 0 && validChildCount <= 1) {
                child = group.getChildAt(validChildIndex);
                if (child != null) {
                    Animation anim = child.getAnimation();
                    Animation targetAnim = anim;
                    if (anim != null && (anim instanceof AnimationSet)) {
                        List<Animation> list = ((AnimationSet) anim).getAnimations();
                        i = 0;
                        while (i < list.size()) {
                            Animation tempAnim = (Animation) list.get(i);
                            if (tempAnim instanceof ScaleAnimation) {
                                targetAnim = tempAnim;
                                break;
                            } else if (!(tempAnim instanceof TranslateAnimation)) {
                                i++;
                            } else {
                                return;
                            }
                        }
                    }
                    if (DEBUG) {
                        Log.i(this.mLogTag, "judgeAdjustVisibleBoundWhenAnim view: " + getViewDebugInfo(child) + ", anim: " + anim + ", targetAnim: " + targetAnim);
                    }
                    if (targetAnim != null && (targetAnim instanceof ScaleAnimation)) {
                        initAnimation(child, targetAnim);
                        if (targetAnim.isInitialized() || targetAnim.isInitedPreDraw()) {
                            Rect visibleBounds = new Rect();
                            child.getGlobalVisibleRect(visibleBounds);
                            boolean isVisibleCloseToNavBar = this.mDockBottomEdge - visibleBounds.top >= this.mMinBarHeight && visibleBounds.bottom >= this.mDockBottomEdge;
                            if (DEBUG) {
                                Log.i(this.mLogTag, "judgeAdjustVisibleBoundWhenAnim visibleBounds: " + visibleBounds + ", isVisibleCloseToNavBar: " + isVisibleCloseToNavBar);
                            }
                            if (isVisibleCloseToNavBar) {
                                this.mTempAnimTransform.clear();
                                targetAnim.getTransformationSpecify(this.mTempAnimTransform, false);
                                this.mTempAnimTransform.getMatrix().getValues(this.mMatrixVals);
                                float fromValueY = this.mMatrixVals[4];
                                this.mTempAnimTransform.clear();
                                targetAnim.getTransformationSpecify(this.mTempAnimTransform, true);
                                Matrix matrix = this.mTempAnimTransform.getMatrix();
                                matrix.getValues(this.mMatrixVals);
                                float toValueY = this.mMatrixVals[4];
                                boolean isEnlarge = fromValueY < toValueY;
                                boolean isShrink = fromValueY > toValueY;
                                float top = (((float) visibleBounds.top) + this.mMatrixVals[5]) - (((float) child.getHeight()) * (1.0f - this.mMatrixVals[4]));
                                int targetVisibleTop = (int) top;
                                int targetVisibleBottom = (int) (top + (((float) visibleBounds.height()) * this.mMatrixVals[4]));
                                boolean isDstVisibleCloseToNavBar = this.mDockBottomEdge - targetVisibleTop >= this.mMinBarHeight && targetVisibleBottom >= this.mDockBottomEdge;
                                if (DEBUG) {
                                    Log.i(this.mLogTag, "judgeAdjustVisibleBoundWhenAnim targetVisibleTop: " + targetVisibleTop + ", targetVisibleBottom: " + targetVisibleBottom + ", matrix: " + matrix.toString() + ", isDstVisibleCloseToNavBar: " + isDstVisibleCloseToNavBar + ", isEnlarge: " + isEnlarge + ", isShrink: " + isShrink);
                                }
                                if ((isEnlarge && isDstVisibleCloseToNavBar) || (isShrink && (isDstVisibleCloseToNavBar ^ 1) != 0)) {
                                    this.mIsNotAdjustVisibleBoundWhenAnim = true;
                                    return;
                                }
                            }
                            return;
                        }
                    }
                    judgeAdjustVisibleBoundWhenAnim(child);
                }
            }
        }
    }

    private boolean checkBelowNavigationBar(View view) {
        if (this.mMeetNavigationBar) {
            return true;
        }
        if (this.mBottomInset == 0) {
            this.mMeetNavigationBar = true;
            return true;
        } else if (view == this.mDecorView) {
            return true;
        } else {
            if (view.getId() != R.id.navigationBarBackground) {
                return false;
            }
            this.mMeetNavigationBar = true;
            return false;
        }
    }

    private View findTraversal(View view) {
        int i;
        if (view == null || view.getVisibility() != 0 || getViewAlpha(view, false) <= 0.0f) {
            if (DEBUG) {
                Log.i(this.mLogTag, "Looking for navigationBar target, skip not visible view = " + getViewDebugInfo(view));
            }
            if (!isTraversalWhenViewNonVisible(view)) {
                return null;
            }
        }
        String viewName = getClassName(view);
        boolean isWebView = judgeWebView(view, viewName);
        if (this.mDrawerLayout == null) {
            handleDrawerLayoutIfNeeded(view, viewName);
        }
        if (view instanceof ViewGroup) {
            boolean isUpScroll;
            boolean isDownScroll;
            try {
                isUpScroll = view.canScrollVertically(-1);
                isDownScroll = view.canScrollVertically(1);
            } catch (NullPointerException e) {
                isUpScroll = true;
                isDownScroll = true;
            }
            boolean isTraversalChild = true;
            if (isUpScroll || isDownScroll) {
                if (isWebView) {
                    isTraversalChild = false;
                } else if (this.mRecoredScrolledView == null) {
                    isTraversalChild = isTraversalChildWhenScrollIfNeeded(view);
                    if (isTraversalChild) {
                        this.mRecoredScrolledView = view;
                    }
                }
            } else if (isWebView || judgeGridView(view, viewName)) {
                isTraversalChild = false;
            }
            if (this.mRecoredScrolledView != null) {
                this.mMinBarHeight = (int) (this.mDensity * 4.0f);
            } else {
                this.mMinBarHeight = (int) (this.mDensity * 28.0f);
            }
            if (isTraversalChild && ((view instanceof AbsListView) ^ 1) != 0 && getSimpleClassName(view).contains("ListView")) {
                isTraversalChild = false;
            }
            if (isTraversalChild) {
                if (viewName.contains("KeyBoard.EmotionLinearLayout]")) {
                    isTraversalChild = false;
                }
            }
            if (DEBUG) {
                Log.i(this.mLogTag, "Looking for navigationBar target maybe traversal child view: " + getViewDebugInfo(view) + ", isTraversalChild: " + isTraversalChild + ", isUpScroll: " + isUpScroll + ", isDownScroll: " + isDownScroll);
            }
            if (isTraversalChild) {
                ViewGroup group = (ViewGroup) view;
                for (i = group.getChildCount() - 1; i >= 0; i--) {
                    View target = findTraversal(group.getChildAt(i));
                    if (target != null) {
                        if (this.mRecoredScrolledView != null) {
                            if (DrawableUtils.isFullColor(this.mTargetColor)) {
                                this.mTargetColor = DrawableUtils.adjustFullColor(this.mTargetColor);
                            }
                            this.mRecoredScrolledView.setRecordedColor(new Integer(this.mTargetColor));
                        }
                        return target;
                    }
                }
            }
            if (view == this.mRecoredScrolledView) {
                endTraversalAllChildrenWhenScroll();
            }
        }
        boolean match = canBeNavigationBarTarget(view, this.mTempBounds);
        if (DEBUG) {
            Log.i(this.mLogTag, "Looking for navigationBar target view = " + getViewDebugInfo(view) + " [" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]" + ", match: " + match + ", isUnFilledRegion: " + isUnFilledRegion(this.mTempBounds) + ", mIsNotAdjustVisibleBoundWhenAnim: " + this.mIsNotAdjustVisibleBoundWhenAnim + ", mCurrSlideType: " + this.mCurrSlideType);
        }
        if (view.getId() == R.id.navigationBarBackground) {
            if (DEBUG) {
                Log.d(this.mLogTag, "find nav bar, remove needless masklayer");
            }
            for (i = this.mMaskLayerInfoList.size() - 1; i >= 0; i--) {
                if (((MaskLayerInfo) this.mMaskLayerInfoList.get(i)).bound.bottom > this.mDockBottomEdge + this.mMinBarHeight) {
                    this.mMaskLayerInfoList.remove(i);
                }
            }
        }
        if (match) {
            if (isUnFilledRegion(this.mTempBounds)) {
                int color;
                int size;
                this.mTempBounds.intersect(0, 0, this.mDecorWidth, this.mDecorHeight);
                boolean isNonFillUp = false;
                float viewAlpha;
                if (JUDGE_VIEW_DIRTY) {
                    int isNonProblemViewForNavFlash = !(view instanceof ImageView) ? (view instanceof ScrollView) ^ 1 : 0;
                    if (isNonProblemViewForNavFlash == 0 || (view.isDirty() ^ 1) == 0 || view.getRecordedColor() == null) {
                        if (isNonProblemViewForNavFlash != 0) {
                            view.setRecordedColor(null);
                        }
                        if (judgeMapSdk(view, viewName)) {
                            color = DrawableUtils.NAVIGATION_BAR_DEFAULT_COLOR;
                        } else {
                            if ((view instanceof ImageView) && (this.mIsBottomFullBar ^ 1) != 0) {
                                isNonFillUp = isNonFillUpImageView(view);
                            }
                            color = DrawableUtils.getColorFromView(view, this.mTempBounds, this.mDecorWidth, isWebView, this.mIsBottomFullBar, isNonFillUp, isImageViewNeedRecordColor(view, viewName));
                        }
                        if (color != 0) {
                            viewAlpha = getViewAlpha(view, true);
                            if (viewAlpha < 1.0f) {
                                color = DrawableUtils.blendAlpha(color, viewAlpha);
                            }
                        }
                        if (isNonProblemViewForNavFlash != 0) {
                            view.setRecordedColor(new Integer(color));
                        }
                    } else {
                        color = view.getRecordedColor().intValue();
                        if (DEBUG) {
                            Log.i(this.mLogTag, "NavigationBar target candidate, non dirty view: " + getViewDebugInfo(view) + "[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]" + ", color: " + Integer.toHexString(color) + ", alpha: " + Color.alpha(color));
                        }
                    }
                } else {
                    if (judgeMapSdk(view, viewName)) {
                        color = DrawableUtils.NAVIGATION_BAR_DEFAULT_COLOR;
                    } else {
                        if ((view instanceof ImageView) && (this.mIsBottomFullBar ^ 1) != 0) {
                            isNonFillUp = isNonFillUpImageView(view);
                        }
                        color = DrawableUtils.getColorFromView(view, this.mTempBounds, this.mDecorWidth, isWebView, this.mIsBottomFullBar, isNonFillUp, isImageViewNeedRecordColor(view, viewName));
                    }
                    if (color != 0) {
                        viewAlpha = getViewAlpha(view, true);
                        if (viewAlpha < 1.0f) {
                            color = DrawableUtils.blendAlpha(color, viewAlpha);
                        }
                    }
                }
                if ((((view instanceof SurfaceView) && ((view instanceof GLSurfaceView) ^ 1) != 0) || (view instanceof TextureView)) && this.mTempBounds.right - this.mTempBounds.left == this.mDecorWidth) {
                    this.mIsExistSpecialView = true;
                }
                if (DEBUG) {
                    Log.i(this.mLogTag, "NavigationBar target candidate, view = " + getViewDebugInfo(view) + "[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]" + ", color = " + Integer.toHexString(color) + ", alpha = " + Color.alpha(color) + ", mIsExistSpecialView: " + this.mIsExistSpecialView + ", isNonFillUp: " + isNonFillUp);
                }
                boolean isColorValid = isValidColor(color);
                boolean force = false;
                if (!isColorValid && view == this.mDecorView) {
                    isColorValid = true;
                    force = true;
                }
                if (isColorValid) {
                    updateUnFilledRegionList(this.mTempBounds, this.mFilledRegionList);
                    size = this.mFilledRegionList.size();
                    if (size > 0) {
                        for (i = 0; i < size; i++) {
                            Point filled = (Point) this.mFilledRegionList.get(i);
                            this.mTempBounds.left = filled.x;
                            this.mTempBounds.right = filled.y;
                            addMaskInfoToListIfNeeded(view, color, isColorValid, this.mTempBounds, force);
                        }
                        this.mFilledRegionList.clear();
                    }
                } else {
                    addMaskInfoToListIfNeeded(view, color, isColorValid, this.mTempBounds, force);
                }
                if (isFillUpHorizontalScreen() || view == this.mDecorView) {
                    size = this.mMaskLayerInfoList.size();
                    if (IMMERSIVE_PERFORMANCE_OPTIMIZE) {
                        int lastSize = this.mLastMaskLayerInfoList.size();
                        if (DEBUG) {
                            Log.i(this.mLogTag, "NavigationBar target docking bottom size: " + size + ", lastSize: " + lastSize);
                        }
                        if (lastSize <= 0) {
                            for (i = size - 1; i >= 0; i--) {
                                this.mLastMaskLayerInfoList.add((MaskLayerInfo) this.mMaskLayerInfoList.get(i));
                            }
                        } else if (lastSize != size) {
                            this.mLastMaskLayerInfoList.clear();
                            for (i = size - 1; i >= 0; i--) {
                                this.mLastMaskLayerInfoList.add((MaskLayerInfo) this.mMaskLayerInfoList.get(i));
                            }
                        } else {
                            boolean isChangeMasks = isMasksChanged(this.mMaskLayerInfoList, this.mLastMaskLayerInfoList, size);
                            if (DEBUG) {
                                Log.i(this.mLogTag, "NavigationBar target docking bottom isChangeMasks: " + isChangeMasks + ", mCurrVerticalGap: " + this.mCurrVerticalGap + ", mNavigationBarTarget: " + getViewDebugInfo(this.mNavigationBarTarget));
                            }
                            if (isChangeMasks) {
                                this.mLastMaskLayerInfoList.clear();
                                for (i = size - 1; i >= 0; i--) {
                                    this.mLastMaskLayerInfoList.add((MaskLayerInfo) this.mMaskLayerInfoList.get(i));
                                }
                            } else if (this.mNavigationBarTarget != null) {
                                return this.mNavigationBarTarget;
                            }
                        }
                    }
                    View resultView = view;
                    this.mTargetColor = color;
                    if (size > 0) {
                        DrawableUtils.blendColors(this.mMaskLayerInfoList, this.mResultInfoList);
                        if (this.mResultInfoList.size() > 0) {
                            MaskLayerInfo dstInfo = measureSuitableViewFromResultList(this.mResultInfoList);
                            if (dstInfo != null) {
                                resultView = dstInfo.view;
                                this.mTargetColor = dstInfo.maskColor;
                                this.mTempBounds.set(dstInfo.bound);
                            }
                        }
                    }
                    if (this.mIsExistSpecialView) {
                        if (DrawableUtils.isFullColor(this.mTargetColor)) {
                            this.mTargetColor = DrawableUtils.adjustFullColor(this.mTargetColor);
                        }
                        this.mIsDebugSpecialView = true;
                    } else {
                        this.mIsDebugSpecialView = false;
                    }
                    if (DEBUG) {
                        Log.i(this.mLogTag, "NavigationBar target docking bottom, view = " + getViewDebugInfo(view) + ", resultView: " + getViewDebugInfo(resultView) + "[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]" + ", mTargetColor = " + Integer.toHexString(this.mTargetColor));
                    }
                    return resultView;
                }
            }
        }
        return null;
    }

    private boolean isMasksChanged(ArrayList<MaskLayerInfo> currMaskList, ArrayList<MaskLayerInfo> lastMaskList, int size) {
        for (int i = 0; i < size; i++) {
            MaskLayerInfo currInfo = (MaskLayerInfo) currMaskList.get(i);
            MaskLayerInfo lastInfo = (MaskLayerInfo) lastMaskList.get(i);
            if (currInfo == null || lastInfo == null || currInfo.view != lastInfo.view || currInfo.maskColor != lastInfo.maskColor || (currInfo.bound.equals(lastInfo.bound) ^ 1) != 0) {
                return true;
            }
        }
        return false;
    }

    private void handleDrawerLayoutIfNeeded(View view, String viewName) {
        if (judgeDrawerLayout(view, viewName)) {
            ViewGroup drawerLayout = (ViewGroup) view;
            int childCount = drawerLayout.getChildCount();
            if (childCount == 2) {
                for (int i = childCount - 1; i >= 0; i--) {
                    View cView = drawerLayout.getChildAt(i);
                    if (cView != null) {
                        getViewBounds(cView, this.mTempBounds);
                        if (this.mTempBounds.left >= this.mDecorWidth) {
                            this.mCurrSlideType = 1;
                            this.mDrawerLayout = view;
                            return;
                        } else if (this.mTempBounds.right <= 0) {
                            this.mCurrSlideType = 2;
                            this.mDrawerLayout = view;
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isBelongToDrawerLayout(View view) {
        View currView = view;
        while (true) {
            View parent = currView.getParent();
            if (parent != null && ((parent instanceof View) ^ 1) == 0) {
                if (parent == this.mDrawerLayout) {
                    return true;
                }
                currView = parent;
            }
        }
        return false;
    }

    private boolean isNonFillUpImageView(View view) {
        boolean isNonFillUp = false;
        ImageView imageView = (ImageView) view;
        int layoutWidth = imageView.getWidth();
        int layoutHeight = imageView.getHeight();
        if (layoutWidth <= 0 || layoutHeight <= 0) {
            return false;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return false;
        }
        int dWidth = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();
        if (dWidth <= 0 || dHeight <= 0) {
            return false;
        }
        ScaleType type = imageView.getScaleType();
        Matrix matrix = imageView.getImageMatrix();
        if (DEBUG) {
            Log.i(this.mLogTag, "isFillUpImageView view: " + getViewDebugInfo(view) + ", layoutWidth: " + layoutWidth + ", layoutHeight: " + layoutHeight + ", dWidth: " + dWidth + ", dHeight: " + dHeight + ", type: " + type);
        }
        if (type == ScaleType.MATRIX) {
            float yScale = 1.0f;
            if (matrix != null) {
                matrix.getValues(this.mMatrixVals);
                yScale = this.mMatrixVals[4];
            }
            float newDrawHeight = ((float) dHeight) * yScale;
            if (DEBUG) {
                Log.i(this.mLogTag, "isFillUpImageView MATRIX yScale: " + yScale + ", newDrawHeight: " + newDrawHeight);
            }
            if (newDrawHeight < ((float) layoutHeight)) {
                isNonFillUp = true;
            }
        } else if (type == ScaleType.FIT_XY) {
            isNonFillUp = false;
        } else if (type == ScaleType.FIT_START) {
            if (((int) ((((float) layoutWidth) / ((float) dWidth)) * ((float) dHeight))) < layoutHeight) {
                isNonFillUp = true;
            }
        } else if (type == ScaleType.FIT_CENTER) {
            if (((int) ((((float) layoutWidth) / ((float) dWidth)) * ((float) dHeight))) < layoutHeight) {
                isNonFillUp = true;
            }
        } else if (type == ScaleType.FIT_END) {
            isNonFillUp = false;
        } else if (type == ScaleType.CENTER) {
            if (dHeight < layoutHeight) {
                isNonFillUp = true;
            }
        } else if (type == ScaleType.CENTER_CROP) {
            isNonFillUp = false;
        } else if (type == ScaleType.CENTER_INSIDE) {
            if (dWidth <= layoutWidth || dHeight <= layoutHeight) {
                isNonFillUp = (dWidth <= layoutWidth || dHeight >= layoutHeight) ? (dWidth >= layoutWidth || dHeight <= layoutHeight) ? dWidth < layoutWidth && dHeight < layoutHeight : false : true;
            } else if (((int) ((((float) layoutWidth) / ((float) dWidth)) * ((float) dHeight))) < layoutHeight) {
                isNonFillUp = true;
            }
        }
        return isNonFillUp;
    }

    private boolean isTraversalWhenViewNonVisible(View view) {
        boolean isTraversal = false;
        if (!(view == null || view.getVisibility() == 0)) {
            Animation anim = view.getAnimation();
            if (anim != null) {
                isTraversal = true;
            }
            if (DEBUG) {
                Log.i(this.mLogTag, "isTraversalWhenViewNonVisible view: " + getViewDebugInfo(view) + ", visibility: " + view.getVisibility() + ", anim: " + anim);
            }
        }
        return isTraversal;
    }

    private boolean isUnFilledRegion(Rect bound) {
        int size = this.mUnFilledRegionList.size();
        for (int i = 0; i < size; i++) {
            Point unFilled = (Point) this.mUnFilledRegionList.get(i);
            if (isLeftIntersectUnFilledRegion(bound, unFilled) || isRightIntersectUnFilledRegion(bound, unFilled) || isInsideUnFilledRegion(bound, unFilled) || isCoverUnFilledRegion(bound, unFilled)) {
                return true;
            }
        }
        return false;
    }

    private void updateUnFilledRegionList(Rect bound, ArrayList<Point> filledRegionList) {
        int size = this.mUnFilledRegionList.size();
        if (size > 0) {
            int i;
            Point[] unFilleds = new Point[size];
            for (i = 0; i < size; i++) {
                unFilleds[i] = (Point) this.mUnFilledRegionList.get(i);
            }
            this.mUnFilledRegionList.clear();
            for (i = 0; i < size; i++) {
                Point unFilled = unFilleds[i];
                if (isLeftIntersectUnFilledRegion(bound, unFilled)) {
                    this.mUnFilledRegionList.add(new Point(unFilled.x, bound.left));
                    filledRegionList.add(new Point(bound.left, unFilled.y));
                } else if (isRightIntersectUnFilledRegion(bound, unFilled)) {
                    this.mUnFilledRegionList.add(new Point(bound.right, unFilled.y));
                    filledRegionList.add(new Point(unFilled.x, bound.right));
                } else if (isInsideUnFilledRegion(bound, unFilled)) {
                    this.mUnFilledRegionList.add(new Point(unFilled.x, bound.left));
                    this.mUnFilledRegionList.add(new Point(bound.right, unFilled.y));
                    filledRegionList.add(new Point(bound.left, bound.right));
                } else if (isCoverUnFilledRegion(bound, unFilled)) {
                    filledRegionList.add(new Point(unFilled.x, unFilled.y));
                } else if (isNoIntersectUnFilledRegion(bound, unFilled)) {
                    this.mUnFilledRegionList.add(unFilled);
                }
            }
        }
    }

    private boolean isLeftIntersectUnFilledRegion(Rect bound, Point unFilled) {
        if (bound.left <= unFilled.x || bound.right < unFilled.y || bound.left >= unFilled.y) {
            return false;
        }
        return true;
    }

    private boolean isRightIntersectUnFilledRegion(Rect bound, Point unFilled) {
        if (bound.left > unFilled.x || bound.right >= unFilled.y || bound.right <= unFilled.x) {
            return false;
        }
        return true;
    }

    private boolean isInsideUnFilledRegion(Rect bound, Point unFilled) {
        if (bound.left <= unFilled.x || bound.right >= unFilled.y) {
            return false;
        }
        return true;
    }

    private boolean isCoverUnFilledRegion(Rect bound, Point unFilled) {
        if (bound.left > unFilled.x || bound.right < unFilled.y) {
            return false;
        }
        return true;
    }

    private boolean isNoIntersectUnFilledRegion(Rect bound, Point unFilled) {
        if (bound.left >= unFilled.y || bound.right <= unFilled.x) {
            return true;
        }
        return false;
    }

    private boolean isFillUpHorizontalScreen() {
        if (this.mUnFilledRegionList.size() <= 0) {
            return true;
        }
        return false;
    }

    private boolean isBelongToBottomBar(View view) {
        if (view.getHeight() <= this.mMaxBarHeight) {
            return true;
        }
        return false;
    }

    private MaskLayerInfo measureSuitableViewFromResultList(ArrayList<MaskLayerInfo> infoList) {
        int size = infoList.size();
        if (size <= 0) {
            return null;
        }
        if (size == 1) {
            return (MaskLayerInfo) infoList.get(0);
        }
        MaskLayerInfo dstInfo;
        int i;
        if (size == 2) {
            MaskLayerInfo fInfo = (MaskLayerInfo) infoList.get(0);
            MaskLayerInfo sInfo = (MaskLayerInfo) infoList.get(1);
            if (fInfo == null || sInfo == null) {
                return null;
            }
            MaskLayerInfo lInfo;
            MaskLayerInfo rInfo;
            if (fInfo.bound.left < sInfo.bound.left) {
                lInfo = fInfo;
                rInfo = sInfo;
            } else {
                lInfo = sInfo;
                rInfo = fInfo;
            }
            dstInfo = null;
            if (!(this.mDrawerLayout == null || this.mCurrSlideType == 0)) {
                View lView = lInfo.view;
                View rView = rInfo.view;
                if ((this.mCurrSlideType == 2 && isBelongToDrawerLayout(lView)) || (this.mCurrSlideType == 1 && isBelongToDrawerLayout(rView))) {
                    ViewGroup group = this.mDrawerLayout;
                    boolean isAtSide = false;
                    for (i = group.getChildCount() - 1; i >= 0; i--) {
                        View currView = group.getChildAt(i);
                        if (currView != null) {
                            getViewBounds(currView, this.mTempBounds);
                            if (this.mTempBounds.left >= this.mDecorWidth || this.mTempBounds.right <= 0) {
                                isAtSide = true;
                            }
                        }
                    }
                    if (!isAtSide) {
                        if (this.mCurrSlideType == 1) {
                            dstInfo = rInfo;
                        } else if (this.mCurrSlideType == 2) {
                            dstInfo = lInfo;
                        }
                    }
                    if (dstInfo != null) {
                        return dstInfo;
                    }
                }
            }
            int lWidth = lInfo.bound.width();
            int rWidth = rInfo.bound.width();
            if (lWidth != rWidth) {
                if (isBelongToSameGroupWhenBottomBar(lInfo.view, rInfo.view)) {
                    dstInfo = measureViewWhenTwoItems(lInfo, rInfo);
                } else if (lWidth > rWidth) {
                    dstInfo = lInfo;
                } else if (lWidth < rWidth) {
                    dstInfo = rInfo;
                }
            } else {
                dstInfo = measureViewWhenTwoItems(lInfo, rInfo);
            }
            return dstInfo;
        }
        removeFullColorItemsInBottomBarFromListIfNeeded(infoList);
        removeCenterFullColorItemsInBottomBarFromListIfNeeded(infoList);
        removeSpecialColorItemsFromListIfNeeded(infoList);
        size = infoList.size();
        boolean[] isMerged = new boolean[size];
        int[] mergeWidths = new int[size];
        int count = 0;
        for (i = 0; i < size; i++) {
            if (!isMerged[i]) {
                MaskLayerInfo currInfo = (MaskLayerInfo) infoList.get(i);
                if (DEBUG) {
                    Log.i(this.mLogTag, "measureSuitableViewFromResultList merge regions index: " + i + ", view: " + getViewDebugInfo(currInfo.view) + ", bound: " + currInfo.bound + ", color: " + Integer.toHexString(currInfo.maskColor));
                }
                int mergeWidth = currInfo.bound.width();
                for (int j = i + 1; j < size; j++) {
                    MaskLayerInfo nextInfo = (MaskLayerInfo) infoList.get(j);
                    if (nextInfo.maskColor == currInfo.maskColor) {
                        isMerged[j] = true;
                        mergeWidth += nextInfo.bound.width();
                    }
                }
                mergeWidths[i] = mergeWidth;
                count++;
            }
        }
        int[] validWidths = new int[count];
        int index = 0;
        for (i = 0; i < size; i++) {
            if (mergeWidths[i] > 0) {
                validWidths[index] = mergeWidths[i];
                index++;
            }
        }
        Arrays.sort(validWidths);
        int maxWidth = validWidths[count - 1];
        index = 0;
        for (i = 0; i < size; i++) {
            if (maxWidth == mergeWidths[i]) {
                index = i;
            }
        }
        if (index >= size) {
            index = size - 1;
        }
        dstInfo = (MaskLayerInfo) infoList.get(index);
        if (DEBUG) {
            Log.i(this.mLogTag, "measureSuitableViewFromResultList dstInfo view: " + getViewDebugInfo(dstInfo.view) + ", bound: " + dstInfo.bound + ", color: " + Integer.toHexString(dstInfo.maskColor) + ", index: " + index + ", size: " + size + ", count: " + count + ", maxWidth: " + maxWidth);
        }
        return dstInfo;
    }

    private boolean isBelongToSameGroupWhenBottomBar(View lView, View rView) {
        boolean isBelongToSameGroup = false;
        boolean isBottomBarItemL = isBelongToBottomBar(lView);
        boolean isBottomBarItemR = isBelongToBottomBar(rView);
        if (!isBottomBarItemL || (isBottomBarItemR ^ 1) != 0) {
            return false;
        }
        getViewBounds(lView, this.mViewBounds);
        int lLeft = this.mViewBounds.left;
        int lRight = this.mViewBounds.right;
        getViewBounds(rView, this.mViewBounds);
        int rLeft = this.mViewBounds.left;
        int rRight = this.mViewBounds.right;
        boolean isParentChild = false;
        if (lLeft <= rLeft && lRight >= rRight) {
            isParentChild = isParentAndChild(lView, rView, 6);
        } else if (rLeft <= lLeft && rRight >= lRight) {
            isParentChild = isParentAndChild(rView, lView, 6);
        }
        if (isParentChild) {
            isBelongToSameGroup = true;
        }
        if (!isBelongToSameGroup) {
            View pView;
            View currView = lView;
            int lookTimes = 0;
            do {
                View parent = currView.getParent();
                if (parent == null || ((parent instanceof View) ^ 1) != 0) {
                    break;
                }
                pView = parent;
                if (!isBelongToBottomBar(pView)) {
                    break;
                }
                this.mBottomBarViewList.add(pView);
                currView = pView;
                lookTimes++;
            } while (lookTimes <= 6);
            int size = this.mBottomBarViewList.size();
            if (size > 0) {
                currView = rView;
                lookTimes = 0;
                do {
                    Object parent2 = currView.getParent();
                    if (parent2 == null || ((parent2 instanceof View) ^ 1) != 0) {
                        break;
                    }
                    pView = (View) parent2;
                    if (!isBelongToBottomBar(pView)) {
                        break;
                    }
                    for (int i = 0; i < size; i++) {
                        if (pView == ((View) this.mBottomBarViewList.get(i))) {
                            isBelongToSameGroup = true;
                            break;
                        }
                    }
                    if (isBelongToSameGroup) {
                        break;
                    }
                    currView = pView;
                    lookTimes++;
                } while (lookTimes <= 6);
            }
        }
        if (DEBUG) {
            Log.i(this.mLogTag, "isBelongToBottomBarSameGroup isParentChild: " + isParentChild + ", lLeft: " + ", lLeft: " + lLeft + ", lRight: " + lRight + ", rLeft: " + rLeft + ", rRight: " + rRight + ", isBelongToSameGroup: " + isBelongToSameGroup);
        }
        return isBelongToSameGroup;
    }

    private boolean isParentAndChild(View parentView, View childView, int maxLookTimes) {
        int lookTimes = 0;
        View currView = childView;
        do {
            View parent = currView.getParent();
            if (parent == null || ((parent instanceof View) ^ 1) != 0) {
                return false;
            }
            View pView = parent;
            if (!isBelongToBottomBar(pView)) {
                return false;
            }
            if (pView == parentView) {
                return true;
            }
            currView = pView;
            lookTimes++;
        } while (lookTimes <= maxLookTimes);
        return false;
    }

    private MaskLayerInfo measureViewWhenTwoItems(MaskLayerInfo lInfo, MaskLayerInfo rInfo) {
        boolean isFullColorL = DrawableUtils.isFullColor(lInfo.maskColor);
        boolean isFullColorR = DrawableUtils.isFullColor(rInfo.maskColor);
        if (isFullColorL && isFullColorR) {
            if (DrawableUtils.getSaturation(lInfo.maskColor) >= DrawableUtils.getSaturation(rInfo.maskColor)) {
                return rInfo;
            }
            return lInfo;
        } else if (isFullColorL && (isFullColorR ^ 1) != 0) {
            return rInfo;
        } else {
            if (!isFullColorL && isFullColorR) {
                return lInfo;
            }
            if (isFullColorL || (isFullColorR ^ 1) == 0) {
                return null;
            }
            if (DrawableUtils.getBright(lInfo.maskColor) >= DrawableUtils.getBright(rInfo.maskColor)) {
                return lInfo;
            }
            return rInfo;
        }
    }

    private void removeCenterFullColorItemsInBottomBarFromListIfNeeded(ArrayList<MaskLayerInfo> infoList) {
        int i;
        MaskLayerInfo info;
        int size = infoList.size();
        Point point = new Point(0, 0);
        MaskLayerInfo[] fullColorNeedRemovedItems = new MaskLayerInfo[size];
        boolean init = true;
        for (i = 0; i < size; i++) {
            info = (MaskLayerInfo) infoList.get(i);
            if (isBelongToBottomBar(info.view) && DrawableUtils.isFullColor(info.maskColor)) {
                if (init) {
                    point.x = info.bound.left;
                    point.y = info.bound.right;
                    init = false;
                }
                if (point.x > info.bound.left) {
                    point.x = info.bound.left;
                }
                if (point.y < info.bound.right) {
                    point.y = info.bound.right;
                }
                fullColorNeedRemovedItems[i] = info;
            }
        }
        if (point.x > 10 && point.y < this.mDecorWidth - 10) {
            for (i = 0; i < size; i++) {
                info = fullColorNeedRemovedItems[i];
                if (info != null && infoList.contains(info)) {
                    infoList.remove(info);
                }
            }
        }
    }

    private void removeFullColorItemsInBottomBarFromListIfNeeded(ArrayList<MaskLayerInfo> infoList) {
        int i;
        MaskLayerInfo info;
        int size = infoList.size();
        int bottomBarItemsCount = 0;
        int fullColorItemsCount = 0;
        MaskLayerInfo[] fullColorItems = new MaskLayerInfo[size];
        for (i = 0; i < size; i++) {
            info = (MaskLayerInfo) infoList.get(i);
            if (isBelongToBottomBar(info.view)) {
                bottomBarItemsCount++;
                if (DrawableUtils.isFullColor(info.maskColor)) {
                    fullColorItems[i] = info;
                    fullColorItemsCount++;
                }
            }
        }
        if (bottomBarItemsCount > 0 && fullColorItemsCount > 0 && fullColorItemsCount < bottomBarItemsCount) {
            for (i = 0; i < size; i++) {
                info = fullColorItems[i];
                if (info != null && infoList.contains(info)) {
                    infoList.remove(info);
                }
            }
        }
        if (DEBUG) {
            Log.i(this.mLogTag, "removeFullColorItemsInBottomBarFromListIfNeeded bottomBarItemsCount: " + bottomBarItemsCount + ", fullColorItemsCount: " + fullColorItemsCount);
        }
    }

    private void removeSpecialColorItemsFromListIfNeeded(ArrayList<MaskLayerInfo> infoList) {
        int size = infoList.size();
        int fullColorCount = 0;
        boolean isExistFullColorOnEdge = false;
        int brightColorCount = 0;
        boolean isExistBrightColorOnEdge = false;
        int darkColorCount = 0;
        boolean isExistDarkColorOnEdge = false;
        for (int i = 0; i < size; i++) {
            MaskLayerInfo info = (MaskLayerInfo) infoList.get(i);
            if (info != null) {
                boolean isFullColor = DrawableUtils.isFullColor(info.maskColor);
                boolean isDarkColor = DrawableUtils.isDarkColor(info.maskColor);
                if (isFullColor) {
                    if (info.bound.left <= 5 || info.bound.right >= this.mDecorWidth - 5) {
                        isExistFullColorOnEdge = true;
                    }
                    fullColorCount++;
                } else if (isDarkColor) {
                    if (info.bound.left <= 5 || info.bound.right >= this.mDecorWidth - 5) {
                        isExistDarkColorOnEdge = true;
                    }
                    darkColorCount++;
                } else {
                    if (info.bound.left <= 5 || info.bound.right >= this.mDecorWidth - 5) {
                        isExistBrightColorOnEdge = true;
                    }
                    brightColorCount++;
                }
            }
        }
        if (!isExistFullColorOnEdge && fullColorCount > 0 && fullColorCount < size) {
            removeItemsFromResultList(infoList, true, false, false);
            size = infoList.size();
        }
        if (!isExistBrightColorOnEdge && brightColorCount > 0 && brightColorCount < size) {
            removeItemsFromResultList(infoList, false, true, false);
            size = infoList.size();
        }
        if (!isExistDarkColorOnEdge && darkColorCount > 0 && darkColorCount < size) {
            removeItemsFromResultList(infoList, false, false, true);
        }
        if (DEBUG) {
            Log.i(this.mLogTag, "removeSpecialColorItemsFromListIfNeeded fullColorCount: " + fullColorCount + ", isExistFullColorOnEdge: " + isExistFullColorOnEdge + ", brightColorCount: " + brightColorCount + ", isExistBrightColorOnEdge: " + isExistBrightColorOnEdge + ", darkColorCount: " + darkColorCount + ", isExistDarkColorOnEdge: " + isExistDarkColorOnEdge);
        }
    }

    private void removeItemsFromResultList(ArrayList<MaskLayerInfo> infoList, boolean isRemoveFullColor, boolean isRemoveBrightColor, boolean isRemoveDarkColor) {
        int i;
        MaskLayerInfo info;
        int size = infoList.size();
        MaskLayerInfo[] specialColorMasks = new MaskLayerInfo[size];
        for (i = 0; i < size; i++) {
            info = (MaskLayerInfo) infoList.get(i);
            if (info != null) {
                if (isRemoveFullColor) {
                    if (DrawableUtils.isFullColor(info.maskColor) && info.bound.left > 0 && info.bound.right < this.mDecorWidth) {
                        specialColorMasks[i] = info;
                    }
                } else if (isRemoveDarkColor) {
                    if (DrawableUtils.isDarkColor(info.maskColor) && info.bound.left > 0 && info.bound.right < this.mDecorWidth) {
                        specialColorMasks[i] = info;
                    }
                } else if (isRemoveBrightColor && !DrawableUtils.isFullColor(info.maskColor) && (DrawableUtils.isDarkColor(info.maskColor) ^ 1) != 0 && info.bound.left > 0 && info.bound.right < this.mDecorWidth) {
                    specialColorMasks[i] = info;
                }
            }
        }
        for (i = 0; i < size; i++) {
            info = specialColorMasks[i];
            if (info != null && infoList.contains(info)) {
                infoList.remove(info);
            }
        }
    }

    private void endTraversalAllChildrenWhenScroll() {
        this.mRecoredScrolledView.setRecordedColor(new Integer(0));
        this.mRecoredScrolledView = null;
    }

    private boolean isTraversalChildWhenScrollIfNeeded(View view) {
        boolean isTraversal = false;
        if (!(view instanceof ScrollView)) {
            return false;
        }
        if (view.getRecordedColor() == null) {
            if (!canBeNavigationBarTarget(view, this.mTempBounds)) {
                return false;
            }
            if (view.getBackground() == null) {
                Rect decorBounds = new Rect();
                getViewBounds(this.mDecorView, decorBounds);
                int decorColor = DrawableUtils.getColorFromView(this.mDecorView, decorBounds, this.mDecorWidth, false, false, false, false);
                if (decorColor == 0) {
                    isTraversal = true;
                } else if (DrawableUtils.isFullColor(decorColor)) {
                    isTraversal = true;
                } else if (DrawableUtils.isDarkColor(decorColor)) {
                    isTraversal = true;
                }
            }
        }
        return isTraversal;
    }

    private boolean judgeMapSdk(View view, String viewName) {
        if (viewName == null || viewName.indexOf("com.tencent.mapsdk") == -1) {
            return false;
        }
        return true;
    }

    private boolean judgeGridView(View view, String viewName) {
        if (view instanceof GridView) {
            return true;
        }
        if (viewName == null || viewName.indexOf("GridView]") == -1) {
            return false;
        }
        return true;
    }

    private boolean judgeWebView(View view, String viewName) {
        if (view instanceof WebView) {
            return true;
        }
        if (viewName == null) {
            return false;
        }
        int wResult = viewName.indexOf("WebView");
        int cResult = viewName.indexOf("com.uc.webkit");
        if (wResult == -1 && cResult == -1) {
            return false;
        }
        return true;
    }

    private boolean judgeAppCompatImageView(View view, String viewName) {
        if (viewName == null || viewName.indexOf("AppCompatImageView") == -1) {
            return false;
        }
        return true;
    }

    private boolean judgeDrawerLayout(View view, String viewName) {
        if (viewName == null || viewName.indexOf("android.support.v4.widget.DrawerLayout") == -1) {
            return false;
        }
        return true;
    }

    private void getViewBounds(View view, Rect outBounds) {
        view.getGlobalVisibleRect(outBounds, null, true);
        if (!this.mIsNotAdjustVisibleBoundWhenAnim) {
            outBounds.set(getTranslateX(view, outBounds));
        }
    }

    private Rect getTranslateX(View view, Rect bounds) {
        Animation anim = view.getAnimation();
        view.getGlobalVisibleRect(this.mTempAnimBounds, null, true);
        if (!this.mTempAnimBounds.intersect(bounds)) {
            this.mTempAnimBounds.setEmpty();
        }
        this.mTempAnimRect.set(this.mTempAnimBounds);
        if (anim != null) {
            initAnimation(view, anim);
            if (anim.isInitialized() || anim.isInitedPreDraw()) {
                this.mTempAnimTransform.clear();
                Animation translate = getTranslateAnimFromSet(anim);
                if (translate != null) {
                    anim = translate;
                }
                if ((anim instanceof TranslateAnimation) && anim.hasStarted() && (anim.hasEnded() ^ 1) != 0) {
                    this.mTempAnimTransform.clear();
                    anim.getTransformationSpecify(this.mTempAnimTransform, false);
                    this.mTempAnimTransform.getMatrix().getValues(this.mMatrixVals);
                    float fromValueY = this.mMatrixVals[5];
                    this.mTempAnimTransform.clear();
                    anim.getTransformationSpecify(this.mTempAnimTransform, true);
                    this.mTempAnimTransform.getMatrix().getValues(this.mMatrixVals);
                    float toValueY = this.mMatrixVals[5];
                    if (toValueY != fromValueY) {
                        boolean z;
                        Transformation transformation = this.mTempAnimTransform;
                        if (toValueY < fromValueY) {
                            z = true;
                        } else {
                            z = false;
                        }
                        anim.getTransformationSpecify(transformation, z);
                    } else {
                        anim.getTransformationNodraw(AnimationUtils.currentAnimationTimeMillis(), this.mTempAnimTransform);
                    }
                } else {
                    anim.getTransformationNodraw(AnimationUtils.currentAnimationTimeMillis(), this.mTempAnimTransform);
                }
                Matrix matrix = this.mTempAnimTransform.getMatrix();
                matrix.getValues(this.mMatrixVals);
                if (DEBUG) {
                    Log.i(this.mLogTag, "view=" + view + "matrix=" + matrix.toString());
                }
                float left = ((float) this.mTempAnimBounds.left) + this.mMatrixVals[2];
                float top = (((float) this.mTempAnimBounds.top) + this.mMatrixVals[5]) - (((float) view.getHeight()) * (1.0f - this.mMatrixVals[4]));
                this.mTempAnimRect.set((int) left, (int) top, (int) (left + (((float) this.mTempAnimBounds.width()) * this.mMatrixVals[0])), (int) (top + (((float) this.mTempAnimBounds.height()) * this.mMatrixVals[4])));
            }
        }
        if (!(view.getParent() instanceof View)) {
            return this.mTempAnimRect;
        }
        return getTranslateX((View) view.getParent(), this.mTempAnimRect);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0046 A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Animation getTranslateAnimFromSet(Animation anim) {
        if (anim != null && (anim instanceof AnimationSet)) {
            List<Animation> list = ((AnimationSet) anim).getAnimations();
            int size = list.size();
            Animation tempAnim;
            if (size == 2) {
                boolean isHasAlphaAnim = false;
                Animation translate = null;
                for (int i = 0; i < size; i++) {
                    tempAnim = (Animation) list.get(i);
                    if (tempAnim != null && (tempAnim instanceof TranslateAnimation)) {
                        translate = tempAnim;
                    }
                    if (tempAnim != null && (tempAnim instanceof AlphaAnimation)) {
                        isHasAlphaAnim = true;
                    }
                }
                if (translate == null || !isHasAlphaAnim) {
                    return null;
                }
                return translate;
            } else if (size == 1) {
                tempAnim = (Animation) list.get(0);
                if (tempAnim != null && (tempAnim instanceof TranslateAnimation)) {
                    return tempAnim;
                }
            }
        }
        return null;
    }

    private void initAnimation(View v, Animation anim) {
        if (!anim.isInitialized() && (anim.isInitedPreDraw() ^ 1) != 0) {
            ViewParent parent = v.getParent();
            if (parent instanceof ViewGroup) {
                anim.initialize(v.getWidth(), v.getHeight(), ((ViewGroup) parent).getWidth(), ((ViewGroup) parent).getHeight());
                anim.setInitedPreDraw();
            }
        }
    }

    private boolean canBeNavigationBarTarget(View v, Rect outRect) {
        getViewBounds(v, this.mTempBounds);
        outRect.set(this.mTempBounds);
        int visibleLeft = this.mTempBounds.left;
        int visibleTop = this.mTempBounds.top;
        int visibleRight = this.mTempBounds.right;
        int visibleBottom = this.mTempBounds.bottom;
        int visibleWidth = visibleRight - visibleLeft;
        int visibleHeight = visibleBottom - visibleTop;
        int layoutWidth = v.getWidth();
        int layoutHeight = v.getHeight();
        if (visibleWidth <= 0 || visibleHeight <= 0 || layoutWidth <= 0 || layoutHeight <= 0) {
            return false;
        }
        if (v == this.mDecorView) {
            return true;
        }
        this.mIsBottomFullBar = false;
        if ((this.mDockBottomEdge - visibleTop < this.mMinBarHeight || visibleBottom < this.mDockBottomEdge) && (this.mUpDockBottomEdge - visibleTop < this.mMinBarHeight || visibleBottom < this.mUpDockBottomEdge)) {
            return false;
        }
        if (layoutHeight <= this.mMaxBarHeight) {
            boolean isBottomBarItem = layoutWidth < (this.mDecorWidth >> 1);
            if (((v instanceof ImageView) && isBottomBarItem) || (v instanceof WebView)) {
                return false;
            }
            this.mIsBottomFullBar = true;
        }
        return true;
    }

    private float getViewAlpha(View view, boolean includeParent) {
        float alpha = 1.0f;
        while (true) {
            alpha *= view.getAlpha() * view.getTransitionAlpha();
            Animation anim = view.getAnimation();
            if (anim != null) {
                initAnimation(view, anim);
                if (anim.isInitialized() || anim.isInitedPreDraw()) {
                    this.mTempAnimTransform.clear();
                    anim.getTransformationNodraw(AnimationUtils.currentAnimationTimeMillis(), this.mTempAnimTransform);
                    alpha *= this.mTempAnimTransform.getAlpha();
                }
            }
            View parent = view.getParent();
            if (!includeParent || parent == null || ((parent instanceof View) ^ 1) != 0) {
                return alpha;
            }
            view = parent;
        }
        return alpha;
    }

    private void addMaskInfoToListIfNeeded(View view, int color, boolean isValidColor, Rect bound, boolean force) {
        int colorA = Color.alpha(color);
        if ((((float) colorA) != 0.0f || force) && bound.left < this.mDecorWidth && bound.right > 0) {
            int left = bound.left;
            int top = bound.top;
            int right = bound.right;
            int bottom = bound.bottom;
            if (right > 0) {
                if (left < 0) {
                    left = 0;
                }
                if (left < this.mDecorWidth) {
                    if (right > this.mDecorWidth) {
                        right = this.mDecorWidth;
                    }
                    float blendRatio = ((float) colorA) / 255.0f;
                    MaskLayerInfo maskInfo = new MaskLayerInfo();
                    maskInfo.view = view;
                    maskInfo.blendRatio = blendRatio;
                    maskInfo.maskColor = color;
                    maskInfo.isValidColor = isValidColor;
                    maskInfo.bound = new Rect();
                    maskInfo.bound.set(left, top, right, bottom);
                    this.mMaskLayerInfoList.add(maskInfo);
                    this.mAllLayerInfoList.add(maskInfo);
                }
            }
        }
    }

    private View getDecorView() {
        return this.mDecorView;
    }

    private UpdateNavigationBarHandler getUpdateHandler() {
        return this.mUpdateNavigationBarHandler;
    }

    private long getTimestamp() {
        return this.mUpdateNavigationBarTimestamp;
    }

    private void setTimestamp(long time) {
        this.mUpdateNavigationBarTimestamp = time;
    }

    private ViewTreePostDrawListener getPostDrawListener() {
        return this.mViewTreePostDrawListener;
    }

    private int getDockBottomEdge() {
        return this.mDockBottomEdge;
    }

    private View getNavigationBarTarget() {
        return this.mNavigationBarTarget;
    }

    private int getDecorHeight() {
        return this.mDecorHeight;
    }

    private boolean isValidColor(int color) {
        return Color.alpha(color) >= 255;
    }

    void removeCallbacks() {
        if (DEBUG) {
            Log.i(this.mLogTag, "test measure color Controller NavigationBar removeCallbacks <--");
        }
        if (this.mDecorView != null) {
            ViewTreeObserver observer = this.mDecorView.getViewTreeObserver();
            if (observer != null) {
                observer.removeOnPostDrawListener(this.mViewTreePostDrawListener);
            }
        }
        if (this.mUpdateNavigationBarHandler != null) {
            this.mUpdateNavigationBarHandler.removeCallbacksAndMessages(null);
        }
    }

    void updateLogTag(LayoutParams params) {
        this.mLogTag = "NavigationBarController[" + getTitleSuffix(params) + "]";
    }

    private static String getTitleSuffix(LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }

    void drawDebug(Canvas canvas) {
        if (DEBUG_DRAW) {
            if (DEBUG) {
                Log.i(this.mLogTag, "drawDebug, mNavigationBarTarget: " + getViewDebugInfo(this.mNavigationBarTarget));
            }
            if (this.mNavigationBarTarget != null) {
                this.mTempPaint.setColor(-16776961);
                this.mTempPaint.setStyle(Style.STROKE);
                this.mTempPaint.setStrokeWidth(10.0f);
                this.mTempPaint.setTextSize(50.0f);
                canvas.drawRect(this.mTempBounds, this.mTempPaint);
                if (DEBUG) {
                    Log.i(this.mLogTag, "drawDebug, drawRect: " + this.mRect);
                }
                this.mTempPaint.setStrokeWidth(5.0f);
                try {
                    canvas.drawText(this.mNavigationBarTarget.getClass().getSimpleName() + "@" + Integer.toHexString(this.mNavigationBarTarget.hashCode()), 100.0f, 200.0f, this.mTempPaint);
                    if (DEBUG) {
                        Log.i(this.mLogTag, "drawDebug, drawText: " + this.mNavigationBarTarget.getClass().getSimpleName() + "@" + Integer.toHexString(this.mNavigationBarTarget.hashCode()));
                    }
                    canvas.drawText(getIdEntryName(this.mNavigationBarTarget), 100.0f, (float) 300, this.mTempPaint);
                    if (DEBUG) {
                        Log.i(this.mLogTag, "drawDebug, drawText: " + getIdEntryName(this.mNavigationBarTarget));
                    }
                    canvas.drawText("[" + this.mTempBounds.left + "," + this.mTempBounds.top + "," + this.mTempBounds.right + "," + this.mTempBounds.bottom + "]", 100.0f, (float) 400, this.mTempPaint);
                    if (this.mIsDebugSpecialView) {
                        canvas.drawText("SurfaceView", (float) (this.mDecorWidth >> 1), (float) 400, this.mTempPaint);
                    }
                    if (DEBUG) {
                        Log.i(this.mLogTag, "drawDebug, drawText: " + getIdEntryName(this.mNavigationBarTarget));
                    }
                } catch (Exception e) {
                }
                if (this.mNavigationBarTarget != null) {
                    int width = this.mNavigationBarTarget.getWidth() / 3;
                    int height = this.mNavigationBarTarget.getHeight() / 3;
                    this.mRect.left = 100;
                    this.mRect.top = 500;
                    this.mRect.right = this.mRect.left + width;
                    this.mRect.bottom = this.mRect.top + height;
                    canvas.save();
                    canvas.translate((float) this.mRect.left, (float) this.mRect.top);
                    canvas.scale(0.33333334f, 0.33333334f);
                    if (judgeWebView(this.mNavigationBarTarget, getClassName(this.mNavigationBarTarget))) {
                        this.mNavigationBarTarget.drawNoChildren(canvas, true);
                    } else {
                        this.mNavigationBarTarget.drawNoChildren(canvas, false);
                    }
                    canvas.restore();
                    canvas.drawRect(this.mRect, this.mTempPaint);
                    if (DEBUG) {
                        Log.i(this.mLogTag, "drawDebug, drawDrawable: " + this.mRect + ", mNavigationBarTarget: " + this.mNavigationBarTarget);
                    }
                    this.mTempPaint.setStyle(Style.FILL);
                    this.mTempPaint.setColor(this.mTargetColor);
                    canvas.drawRect((float) (this.mRect.right + 5), (float) this.mRect.top, (float) (this.mRect.right + 150), (float) (this.mRect.top + 100), this.mTempPaint);
                    return;
                }
                return;
            }
            this.mTempPaint.setColor(-16776961);
            this.mTempPaint.setStyle(Style.STROKE);
            this.mTempPaint.setStrokeWidth(5.0f);
            this.mTempPaint.setTextSize(50.0f);
            canvas.drawText("mNavigationBarTarget: null", 100.0f, 200.0f, this.mTempPaint);
        }
    }

    private String getViewDebugInfo(View view) {
        if (view == null) {
            return "null";
        }
        return getClassName(view) + "@" + Integer.toHexString(view.hashCode()) + " " + getIdEntryName(view);
    }

    private boolean isImageViewNeedRecordColor(View view, String viewName) {
        boolean isNeedRecordColor = false;
        if (!(view instanceof ImageView)) {
            return false;
        }
        if (viewName != null) {
            isNeedRecordColor = (!viewName.contains("AppCompatImageView") ? viewName.contains("com.facebook.drawee.view.SimpleDraweeView") : 1) ^ 1;
        }
        return isNeedRecordColor;
    }

    private String getClassName(View view) {
        Class clz = view.getClass();
        String className = clz.getName();
        String finalName = "";
        while (className != null && (className.startsWith("android.") ^ 1) != 0) {
            finalName = finalName + "[" + className + "]";
            clz = clz.getSuperclass();
            className = clz.getName();
        }
        return finalName + "[" + className + "]";
    }

    private String getSimpleClassName(View view) {
        return "[" + view.getClass().getSimpleName() + "]";
    }

    private String getIdEntryName(View view) {
        StringBuilder out = new StringBuilder();
        int id = view.getId();
        if (id != -1) {
            Resources r = view.getResources();
            if (id > 0 && Resources.resourceHasPackage(id) && r != null) {
                String pkgname;
                switch (-16777216 & id) {
                    case 16777216:
                        pkgname = ZenModeConfig.SYSTEM_AUTHORITY;
                        break;
                    case 2130706432:
                        pkgname = RegionalizationEnvironment.ISREGIONAL_APP;
                        break;
                    default:
                        try {
                            pkgname = r.getResourcePackageName(id);
                            break;
                        } catch (NotFoundException e) {
                            break;
                        }
                }
                String entryname = r.getResourceEntryName(id);
                out.append(pkgname);
                out.append(SettingsStringUtil.DELIMITER);
                out.append(entryname);
            }
        }
        return out.toString();
    }
}
