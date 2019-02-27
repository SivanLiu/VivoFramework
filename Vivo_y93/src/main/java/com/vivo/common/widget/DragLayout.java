package com.vivo.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragLayout extends LinearLayout {
    /* renamed from: -com-vivo-common-widget-DragLayout$StateSwitchesValues */
    private static final /* synthetic */ int[] f0-com-vivo-common-widget-DragLayout$StateSwitchesValues = null;
    private static final float ALPHA = 0.6f;
    private static final int ANIMATE_LAYOUT_UPDATE_TIME = 50;
    private static final int ANIMATE_SCROLL_DISTANCE = 100;
    private static final boolean DEBUG = true;
    private static final int MSG_DRAG_ACTION_FINISHED = 2;
    private static final int MSG_DRAG_POSITION_CHANGED = 1;
    private static final float SCALE = 1.2f;
    private static final String TAG = "DragLayout";
    private static final float VELOCITY_TRACKER_MAX = 60.0f;
    private static final int VELOCITY_TRACKER_UNITS = 1000;
    private boolean mAnimateViewFirstStage;
    private List<AnimateViewInfo> mAnimateViews;
    private boolean mAnimateViewsReverse;
    private List<IAnimation> mAnimations;
    Callback mCallback;
    private List<IAnimation> mCompleteAnims;
    private View mCoverageView;
    State mCurrentState;
    private AnimateViewInfo mDragAnimateViewInfo;
    private Bitmap mDragBitmap;
    private float mDragPosX;
    private float mDragPosY;
    View mDragView;
    private float mDragViewAlpha;
    private float mDragViewScale;
    private boolean mExcuteDragAction;
    private Handler mHandler;
    private long mLastDragViewAnimateFinishTime;
    private float mLastTouchX;
    private float mLastTouchY;
    private long mLastViewAnimatFinishTime;
    private IAnimation mPenddingDragAnimation;
    private MotionEvent mPenddingEvent;
    private VelocityTracker mVelocityTracker;

    public interface Callback {
        List<View> acquireDragableViews(Point point, Point point2);

        void changeViewPosition(int i);

        void finishDragAction();

        boolean isDragable(View view);
    }

    class AnimateViewInfo {
        Bitmap bitmap;
        float curX;
        float curY;
        float endX;
        float endY;
        float startX;
        float startY;
        View view;

        AnimateViewInfo() {
        }
    }

    private class DefaultCallback implements Callback {
        /* synthetic */ DefaultCallback(DragLayout this$0, DefaultCallback -this1) {
            this();
        }

        private DefaultCallback() {
        }

        public boolean isDragable(View v) {
            return false;
        }

        public List<View> acquireDragableViews(Point sp, Point ep) {
            return null;
        }

        public void finishDragAction() {
        }

        public void changeViewPosition(int offset) {
        }
    }

    private class DragHandler extends Handler {
        /* synthetic */ DragHandler(DragLayout this$0, DragHandler -this1) {
            this();
        }

        private DragHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DragLayout.this.mCallback.changeViewPosition(msg.arg1);
                    return;
                case 2:
                    DragLayout.this.mCallback.finishDragAction();
                    return;
                default:
                    return;
            }
        }
    }

    interface IAnimation {
        boolean computeValue();

        void draw(Canvas canvas);

        void start();
    }

    private class EnterDragActionAnim implements IAnimation {
        private int alphaAnimId = 0;
        private MutiScroller animScroller = null;
        private boolean enterMode = DragLayout.DEBUG;
        private int scaleAnimId = 0;

        EnterDragActionAnim() {
            this.animScroller = new MutiScroller();
            Interpolator polator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
            this.alphaAnimId = this.animScroller.addScroller(110, polator);
            this.scaleAnimId = this.animScroller.addScroller(110, polator);
        }

        public void start() {
            this.animScroller.startScroll();
        }

        public boolean computeValue() {
            if (this.animScroller.isFinished()) {
                return DragLayout.DEBUG;
            }
            this.animScroller.computeScroller();
            DragLayout.this.mDragViewAlpha = 1.0f - (this.animScroller.getValue(this.alphaAnimId) * 0.39999998f);
            DragLayout.this.mDragViewScale = (this.animScroller.getValue(this.scaleAnimId) * 0.20000005f) + 1.0f;
            return false;
        }

        public void draw(Canvas canvas) {
        }
    }

    private class ExitDragActionAnim implements IAnimation {
        private int alphaId;
        private MutiScroller animScroller = null;
        private float endX;
        private float endY;
        private int scaleId;
        private float startX;
        private float startY;
        private int translateId;

        ExitDragActionAnim(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
            this.animScroller = new MutiScroller();
            Interpolator polator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
            this.translateId = this.animScroller.addScroller(0, 200, polator);
            this.scaleId = this.animScroller.addScroller(0, 200, polator);
            this.alphaId = this.animScroller.addScroller(0, 200, polator);
        }

        public void start() {
            this.animScroller.startScroll();
        }

        public boolean computeValue() {
            if (this.animScroller.isFinished()) {
                return DragLayout.DEBUG;
            }
            this.animScroller.computeScroller();
            float percent = this.animScroller.getValue(this.translateId);
            DragLayout.this.mDragPosX = this.startX + ((this.endX - this.startX) * percent);
            DragLayout.this.mDragPosY = this.startY + ((this.endY - this.startY) * percent);
            DragLayout.this.mDragViewAlpha = (this.animScroller.getValue(this.alphaId) * 0.39999998f) + 0.6f;
            DragLayout.this.mDragViewScale = DragLayout.SCALE - (this.animScroller.getValue(this.scaleId) * 0.20000005f);
            return false;
        }

        public void draw(Canvas canvas) {
        }
    }

    class MutiScroller {
        private int mCurrentFrame = 0;
        private long mDuration = 0;
        private List<Long> mDurations = new ArrayList();
        private List<Integer> mFrames = new ArrayList();
        private List<Interpolator> mInterpolators = new ArrayList();
        private int mRemainFrame = 0;
        private long mStartTime = 0;
        private List<Long> mStartTimes = new ArrayList();
        private List<Float> mValues = new ArrayList();

        MutiScroller() {
        }

        int addScroller(long time, Interpolator interpolator) {
            return addScroller(0, time, interpolator);
        }

        int addScroller(int frame, long time, Interpolator interpolator) {
            if (time <= 0 || interpolator == null) {
                Log.e(DragLayout.TAG, "addScroller receive invalidate parameters");
                return -1;
            }
            this.mInterpolators.add(interpolator);
            this.mDurations.add(Long.valueOf(time));
            this.mFrames.add(Integer.valueOf(frame));
            this.mRemainFrame++;
            return this.mInterpolators.size() - 1;
        }

        void startScroll() {
            this.mStartTime = SystemClock.uptimeMillis();
            int count = this.mInterpolators.size();
            for (int i = 0; i < count; i++) {
                this.mStartTimes.add(Long.valueOf(-1));
                this.mValues.add(Float.valueOf(0.0f));
            }
        }

        void computeScroller() {
            int i;
            long time = SystemClock.uptimeMillis();
            for (i = this.mFrames.size() - 1; i >= 0; i--) {
                this.mStartTimes.set(i, Long.valueOf(this.mCurrentFrame == ((Integer) this.mFrames.get(i)).intValue() ? time : ((Long) this.mStartTimes.get(i)).longValue()));
            }
            i = this.mStartTimes.size() - 1;
            while (i >= 0) {
                if (((Long) this.mStartTimes.get(i)).longValue() > 0 && ((Float) this.mValues.get(i)).floatValue() < 1.0f) {
                    float value = ((Interpolator) this.mInterpolators.get(i)).getInterpolation(Math.max(0.0f, Math.min(((float) (time - ((Long) this.mStartTimes.get(i)).longValue())) / (((float) ((Long) this.mDurations.get(i)).longValue()) * 1.0f), 1.0f)));
                    this.mValues.set(i, Float.valueOf(value));
                    if (value >= 1.0f) {
                        this.mRemainFrame = Math.max(0, this.mRemainFrame - 1);
                    }
                }
                i--;
            }
            this.mCurrentFrame++;
        }

        float getValue(int index) {
            if (index < 0 || index >= this.mValues.size()) {
                return 0.0f;
            }
            return ((Float) this.mValues.get(index)).floatValue();
        }

        boolean isFinished() {
            return this.mRemainFrame <= 0 ? DragLayout.DEBUG : false;
        }
    }

    enum State {
        NORMAL,
        ENTER_DRAG,
        TOUCH_MOVE,
        ANIMATE_VIEW_FIRST,
        ANIMATE_VIEW_SEC,
        ANIMATE_VIEW_EXIT_DRAG,
        ENTER_EXIT_DRAG,
        EXIT_DRAG
    }

    private class ViewAnimation implements IAnimation {
        private MutiScroller animateScroller;
        private Map<Integer, Integer> mapId;

        /* synthetic */ ViewAnimation(DragLayout this$0, ViewAnimation -this1) {
            this();
        }

        private ViewAnimation() {
            this.animateScroller = new MutiScroller();
            this.mapId = new HashMap();
        }

        public void start() {
            Interpolator polator = new PathInterpolator(0.3f, 0.1f, 0.3f, 1.0f);
            for (int i = DragLayout.this.mAnimateViews.size() - 1; i >= 0; i--) {
                this.mapId.put(Integer.valueOf(i), Integer.valueOf(this.animateScroller.addScroller(i, 200, polator)));
            }
            this.animateScroller.startScroll();
        }

        public boolean computeValue() {
            if (this.animateScroller.isFinished()) {
                return DragLayout.DEBUG;
            }
            this.animateScroller.computeScroller();
            for (int i = DragLayout.this.mAnimateViews.size() - 1; i >= 0; i--) {
                AnimateViewInfo info = (AnimateViewInfo) DragLayout.this.mAnimateViews.get(i);
                float percent = this.animateScroller.getValue(((Integer) this.mapId.get(Integer.valueOf(i))).intValue());
                info.curX = info.startX + ((info.endX - info.startX) * percent);
                info.curY = info.startY + ((info.endY - info.startY) * percent);
            }
            return false;
        }

        public void draw(Canvas canvas) {
            Paint paint = new Paint();
            for (AnimateViewInfo info : DragLayout.this.mAnimateViews) {
                canvas.drawBitmap(info.bitmap, info.curX, info.curY, paint);
            }
        }
    }

    /* renamed from: -getcom-vivo-common-widget-DragLayout$StateSwitchesValues */
    private static /* synthetic */ int[] m0-getcom-vivo-common-widget-DragLayout$StateSwitchesValues() {
        if (f0-com-vivo-common-widget-DragLayout$StateSwitchesValues != null) {
            return f0-com-vivo-common-widget-DragLayout$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ANIMATE_VIEW_EXIT_DRAG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ANIMATE_VIEW_FIRST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.ANIMATE_VIEW_SEC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.ENTER_DRAG.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.ENTER_EXIT_DRAG.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.EXIT_DRAG.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.NORMAL.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.TOUCH_MOVE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        f0-com-vivo-common-widget-DragLayout$StateSwitchesValues = iArr;
        return iArr;
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mExcuteDragAction = false;
        this.mCoverageView = null;
        this.mDragView = null;
        this.mDragBitmap = null;
        this.mDragViewAlpha = 1.0f;
        this.mDragViewScale = 1.0f;
        this.mPenddingEvent = null;
        this.mAnimateViews = new ArrayList();
        this.mLastViewAnimatFinishTime = 0;
        this.mAnimateViewsReverse = false;
        this.mAnimateViewFirstStage = DEBUG;
        this.mDragAnimateViewInfo = new AnimateViewInfo();
        this.mPenddingDragAnimation = null;
        this.mLastDragViewAnimateFinishTime = 0;
        this.mAnimations = new ArrayList();
        this.mCompleteAnims = new ArrayList();
        this.mCallback = new DefaultCallback(this, null);
        this.mHandler = new DragHandler(this, null);
        this.mVelocityTracker = null;
        this.mCurrentState = State.NORMAL;
        this.mCoverageView = this;
        this.mCoverageView.setWillNotDraw(false);
    }

    public void setCallback(Callback callback) {
        if (callback == null) {
            callback = this.mCallback;
        }
        this.mCallback = callback;
    }

    public boolean isDragActionMode() {
        return this.mExcuteDragAction;
    }

    public boolean startDrag(View v) {
        if (this.mExcuteDragAction) {
            Log.e(TAG, "existing drag action, ignore current drag action");
            return false;
        }
        this.mDragBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);
        v.draw(new Canvas(this.mDragBitmap));
        int[] targetViewPos = new int[2];
        int[] coverageViewPos = new int[2];
        v.getLocationOnScreen(targetViewPos);
        this.mCoverageView.getLocationOnScreen(coverageViewPos);
        this.mDragPosX = (float) (targetViewPos[0] - coverageViewPos[0]);
        this.mDragPosY = (float) (targetViewPos[1] - coverageViewPos[1]);
        v.setVisibility(4);
        this.mDragView = v;
        IAnimation animation = new EnterDragActionAnim();
        this.mAnimations.add(animation);
        animation.start();
        this.mCurrentState = State.ENTER_DRAG;
        this.mExcuteDragAction = DEBUG;
        this.mCoverageView.invalidate();
        return DEBUG;
    }

    public void computeScroll() {
        for (IAnimation anim : this.mAnimations) {
            if (anim.computeValue()) {
                this.mCompleteAnims.add(anim);
            }
        }
        for (IAnimation anim2 : this.mCompleteAnims) {
            finishAnimation(anim2);
        }
        this.mCompleteAnims.clear();
        if (this.mAnimations.size() > 0) {
            invalidate();
        }
    }

    private void finishAnimation(IAnimation anim) {
        switch (m0-getcom-vivo-common-widget-DragLayout$StateSwitchesValues()[this.mCurrentState.ordinal()]) {
            case 1:
                finishAnimateViewExitDrag(anim);
                break;
            case 2:
            case 3:
                finishAnimateViews(anim);
                break;
            case 4:
                finishEnterDragAction(anim);
                break;
            case 5:
                finishEnterExitDragActionAnim(anim);
                break;
            case 6:
                finishExitDragActionAnim(anim);
                break;
            default:
                Log.e(TAG, "invalidate state : " + this.mCurrentState);
                break;
        }
        onFinishAnimation(anim);
    }

    protected void onFinishAnimation(IAnimation anim) {
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawAnimateView(canvas);
        drawDragView(canvas);
    }

    private void drawAnimateView(Canvas canvas) {
        for (IAnimation anim : this.mAnimations) {
            anim.draw(canvas);
        }
    }

    private void drawDragView(Canvas canvas) {
        if (this.mDragBitmap != null) {
            Paint paint = new Paint();
            paint.setAlpha((int) (this.mDragViewAlpha * 255.0f));
            Matrix matrix = new Matrix();
            matrix.preScale(this.mDragViewScale, this.mDragViewScale);
            float x = this.mDragPosX + (((1.0f - this.mDragViewScale) * ((float) this.mDragBitmap.getWidth())) / 2.0f);
            float y = this.mDragPosY + (((1.0f - this.mDragViewScale) * ((float) this.mDragBitmap.getHeight())) / 2.0f);
            canvas.save();
            canvas.translate(x, y);
            canvas.drawBitmap(this.mDragBitmap, matrix, paint);
            canvas.restore();
        }
    }

    /* JADX WARNING: Missing block: B:21:0x00ad, code:
            if (r6.getY(r0) == r5.mLastTouchY) goto L_0x00af;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        switch (event.getActionMasked()) {
            case 0:
                this.mLastTouchX = event.getX();
                this.mLastTouchY = event.getY();
                this.mVelocityTracker.clear();
                this.mVelocityTracker.addMovement(event);
                break;
            case 1:
            case 3:
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                if (this.mExcuteDragAction) {
                    startExitDragActionAnim(event);
                    break;
                }
                break;
            case 2:
                if (!this.mExcuteDragAction) {
                    this.mLastTouchX = event.getX();
                    this.mLastTouchY = event.getY();
                    break;
                }
                this.mDragPosX += event.getX() - this.mLastTouchX;
                this.mDragPosY += event.getY() - this.mLastTouchY;
                this.mLastTouchX = event.getX();
                this.mLastTouchY = event.getY();
                this.mCoverageView.invalidate();
                this.mVelocityTracker.addMovement(event);
                this.mVelocityTracker.computeCurrentVelocity(1000);
                if (Math.abs(this.mVelocityTracker.getXVelocity()) <= VELOCITY_TRACKER_MAX && Math.abs(this.mVelocityTracker.getYVelocity()) <= VELOCITY_TRACKER_MAX) {
                    startAnimateViews(event);
                }
                return DEBUG;
            case 6:
                int index = event.getActionIndex();
                if (event.getX(index) == this.mLastTouchX) {
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void finishEnterExitDragActionAnim(IAnimation anim) {
        finishEnterDragAction(anim);
        startExitDragActionAnim(this.mPenddingEvent);
    }

    private void finishEnterDragAction(IAnimation anim) {
        Log.d(TAG, "finishEnterDragAction : " + anim);
        this.mAnimations.remove(anim);
        this.mCurrentState = State.TOUCH_MOVE;
    }

    Point obtainDragViewCurrentPos() {
        return obtainViewPosOnCurrentView(this.mDragView);
    }

    private void startExitDragActionAnim(MotionEvent event) {
        if (this.mLastDragViewAnimateFinishTime < event.getEventTime()) {
            if (this.mCurrentState == State.ENTER_DRAG) {
                this.mCurrentState = State.ENTER_EXIT_DRAG;
                this.mPenddingEvent = event;
                return;
            }
            Point coordinate;
            if (this.mCurrentState == State.TOUCH_MOVE) {
                coordinate = obtainDragViewCurrentPos();
            } else if (this.mCurrentState == State.ANIMATE_VIEW_FIRST || this.mCurrentState == State.ANIMATE_VIEW_SEC) {
                coordinate = obtainViewPosOnCurrentView(((AnimateViewInfo) this.mAnimateViews.get(this.mAnimateViews.size() - 1)).view);
            } else {
                Log.e(TAG, "ignore ExitDragActionAnimn invalidate state : " + this.mCurrentState);
                return;
            }
            IAnimation animation = new ExitDragActionAnim(this.mDragPosX, this.mDragPosY, (float) coordinate.x, (float) coordinate.y);
            this.mAnimations.add(animation);
            animation.start();
            if (this.mCurrentState == State.ANIMATE_VIEW_FIRST || this.mCurrentState == State.ANIMATE_VIEW_SEC) {
                this.mAnimateViewFirstStage = this.mCurrentState == State.ANIMATE_VIEW_FIRST ? DEBUG : false;
                this.mCurrentState = State.ANIMATE_VIEW_EXIT_DRAG;
            } else {
                this.mCurrentState = State.EXIT_DRAG;
            }
            this.mCoverageView.invalidate();
        }
    }

    Point obtainViewPosOnCurrentView(View v) {
        int[] screenPos = new int[2];
        v.getLocationOnScreen(screenPos);
        int[] curScreenPos = new int[2];
        getLocationOnScreen(curScreenPos);
        return new Point(screenPos[0] - curScreenPos[0], screenPos[1] - curScreenPos[1]);
    }

    private void finishExitDragActionAnim(IAnimation anim) {
        Log.d(TAG, "finishExitDragActionAnim : " + anim);
        if (this.mCurrentState != State.EXIT_DRAG) {
            Log.e(TAG, "ignore finishExitDragActionAnim becauseof exists ViewAnimation, will be excute later");
            return;
        }
        this.mAnimations.remove(anim);
        this.mLastDragViewAnimateFinishTime = SystemClock.uptimeMillis() + 50;
        this.mCurrentState = State.NORMAL;
        this.mDragView.setVisibility(0);
        this.mDragBitmap.recycle();
        this.mHandler.obtainMessage(2).sendToTarget();
        this.mDragView = null;
        this.mDragBitmap = null;
        this.mAnimations.clear();
        this.mExcuteDragAction = false;
        this.mCoverageView.invalidate();
    }

    private boolean pointerInView(float x, float y, View v) {
        if (x < ((float) v.getLeft()) || x > ((float) v.getRight()) || y < ((float) v.getTop()) || y > ((float) v.getBottom())) {
            return false;
        }
        return DEBUG;
    }

    private View getRushedView(ViewGroup parentView, float x, float y) {
        View targetView = null;
        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            View child = parentView.getChildAt(i);
            if (child.getVisibility() == 0 && pointerInView(x, y, child)) {
                targetView = child;
                break;
            }
        }
        if (targetView != null && this.mCallback.isDragable(targetView)) {
            return targetView;
        }
        if (targetView instanceof ViewGroup) {
            return getRushedView((ViewGroup) targetView, (x - ((float) targetView.getLeft())) + ((float) targetView.getScrollX()), (y - ((float) targetView.getTop())) + ((float) targetView.getScrollY()));
        }
        return null;
    }

    boolean shouldAnimateViews() {
        return DEBUG;
    }

    Point obtainDragViewAnimatePos() {
        return obtainViewPosOnCurrentView(this.mDragView);
    }

    private void startAnimateViews(MotionEvent event) {
        if (this.mLastViewAnimatFinishTime < event.getEventTime() && this.mCurrentState == State.TOUCH_MOVE && shouldAnimateViews()) {
            View view = getRushedView(this, event.getX(), event.getY());
            if (view != null && view != this.mDragView) {
                List<View> animateViews = this.mCallback.acquireDragableViews(obtainViewPosOnCurrentView(view), obtainDragViewAnimatePos());
                if (animateViews != null && animateViews.size() > 0 && animateViews.indexOf(view) >= 0) {
                    this.mAnimateViewsReverse = view == animateViews.get(0) ? false : DEBUG;
                    animateViews.remove(this.mDragView);
                    if (animateViews.size() > 0) {
                        int[] selfScreenPos = new int[2];
                        getLocationOnScreen(selfScreenPos);
                        View preAnimateView = this.mDragView;
                        int index = this.mAnimateViewsReverse ? 0 : animateViews.size() - 1;
                        do {
                            View animateView = (View) animateViews.get(index);
                            AnimateViewInfo info = new AnimateViewInfo();
                            int[] animateScreenPos = new int[2];
                            animateView.getLocationOnScreen(animateScreenPos);
                            info.startX = (float) (animateScreenPos[0] - selfScreenPos[0]);
                            info.startY = (float) (animateScreenPos[1] - selfScreenPos[1]);
                            if (preAnimateView == this.mDragView) {
                                Point p = obtainDragViewAnimatePos();
                                info.endX = (float) p.x;
                                info.endY = (float) p.y;
                            } else {
                                int[] preScreenPos = new int[2];
                                preAnimateView.getLocationOnScreen(preScreenPos);
                                info.endX = (float) (preScreenPos[0] - selfScreenPos[0]);
                                info.endY = (float) (preScreenPos[1] - selfScreenPos[1]);
                            }
                            Bitmap bitmap = Bitmap.createBitmap(animateView.getWidth(), animateView.getHeight(), Config.ARGB_8888);
                            animateView.draw(new Canvas(bitmap));
                            info.view = animateView;
                            info.bitmap = bitmap;
                            animateView.setVisibility(4);
                            this.mAnimateViews.add(info);
                            preAnimateView = animateView;
                            index = this.mAnimateViewsReverse ? index + 1 : index - 1;
                            if (index < 0) {
                                break;
                            }
                        } while (index < animateViews.size());
                        IAnimation animation = new ViewAnimation(this, null);
                        this.mAnimations.add(animation);
                        animation.start();
                        this.mCurrentState = State.ANIMATE_VIEW_FIRST;
                        this.mCoverageView.invalidate();
                    }
                }
            }
        }
    }

    void finishAnimateViews(IAnimation anim) {
        switch (m0-getcom-vivo-common-widget-DragLayout$StateSwitchesValues()[this.mCurrentState.ordinal()]) {
            case 2:
                finishAnimateViewsFir(anim);
                return;
            case 3:
                finishAnimateViewsSec(anim);
                return;
            default:
                return;
        }
    }

    private void finishAnimateViewsFir(IAnimation anim) {
        Log.d(TAG, "finishAnimateViews : " + anim);
        if (this.mAnimateViews.size() > 0) {
            AnimateViewInfo info;
            View preAnimateView = this.mDragView;
            ViewGroup preParent = (ViewGroup) this.mDragView.getParent();
            if (preParent != null) {
                int preIndex = preParent.indexOfChild(preAnimateView);
                info = new AnimateViewInfo();
                info.view = new View(getContext());
                preParent.addView(info.view, preIndex);
                this.mAnimateViews.add(0, info);
            }
            for (int i = this.mAnimateViews.size() - 1; i >= 0; i--) {
                info = (AnimateViewInfo) this.mAnimateViews.get(i);
                ViewGroup parent = (ViewGroup) info.view.getParent();
                if (preParent != null) {
                    preParent.removeView(preAnimateView);
                }
                if (this.mAnimateViewsReverse) {
                    parent.addView(preAnimateView, Math.min(parent.indexOfChild(info.view) + 1, parent.getChildCount()));
                } else {
                    parent.addView(preAnimateView, parent.indexOfChild(info.view));
                }
                preAnimateView = info.view;
                preParent = parent;
            }
            if (this.mDragView.getParent() != null) {
                View v = ((AnimateViewInfo) this.mAnimateViews.get(0)).view;
                ((ViewGroup) v.getParent()).removeView(v);
                this.mAnimateViews.remove(0);
            }
            this.mCurrentState = State.ANIMATE_VIEW_SEC;
            this.mCoverageView.invalidate();
        }
    }

    int obtainDragViewOffset() {
        return this.mAnimateViewsReverse ? this.mAnimateViews.size() : -this.mAnimateViews.size();
    }

    private void finishAnimateViewsSec(IAnimation anim) {
        this.mAnimations.remove(anim);
        Log.d(TAG, "finishAnimateViewsSec : " + anim);
        for (AnimateViewInfo info : this.mAnimateViews) {
            info.bitmap.recycle();
            info.view.setVisibility(0);
        }
        this.mLastViewAnimatFinishTime = SystemClock.uptimeMillis() + 50;
        this.mHandler.obtainMessage(1, obtainDragViewOffset(), 0).sendToTarget();
        this.mAnimateViews.clear();
        this.mCurrentState = State.TOUCH_MOVE;
    }

    private void finishAnimateViewExitDrag(IAnimation anim) {
        Log.d(TAG, "finishAnimateViewExitDrag : " + anim);
        if (!(!(anim instanceof ExitDragActionAnim) ? anim instanceof ViewAnimation : DEBUG)) {
            Log.d(TAG, "finishAnimateViewExitDrag invalidate IAnimation : " + anim);
        } else if (anim instanceof ExitDragActionAnim) {
            this.mPenddingDragAnimation = anim;
            this.mAnimations.remove(anim);
        } else {
            if (this.mAnimateViewFirstStage) {
                this.mCurrentState = State.ANIMATE_VIEW_FIRST;
                finishAnimateViews(anim);
                this.mCurrentState = State.ANIMATE_VIEW_EXIT_DRAG;
                this.mAnimateViewFirstStage = false;
            } else {
                this.mCurrentState = State.ANIMATE_VIEW_SEC;
                finishAnimateViews(anim);
                this.mCurrentState = State.EXIT_DRAG;
                if (this.mPenddingDragAnimation != null) {
                    this.mAnimations.add(this.mPenddingDragAnimation);
                    finishExitDragActionAnim(this.mPenddingDragAnimation);
                    this.mPenddingDragAnimation = null;
                    this.mAnimateViewFirstStage = DEBUG;
                }
            }
        }
    }
}
