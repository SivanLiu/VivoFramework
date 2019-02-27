package com.vivo.common.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.widget.AbsListView;
import com.vivo.common.provider.Weather;
import vivo.util.Spring;
import vivo.util.SpringConfig;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SideSlipListViewListener implements OnTouchListener {
    private static final int ANIMATION_BACK_TIME = 300;
    private static final int ANIMATION_FORWARD_TIME = 300;
    private static final int ANIMATION_RESTORE_TIME = 250;
    public static final int CURRENT_STATE_EXPAND = 1;
    public static final int CURRENT_STATE_MOVING = 4;
    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_TOEXPAND = 3;
    public static final int CURRENT_STATE_TONORMAL = 2;
    private static final int OPEN_V = 700;
    private static final String TAG = "SideSlipListViewListener";
    private static SpringConfig mFlingConfig = new SpringConfig(17.0d, 7.0d);
    private final float MAX_VELOCITY_X;
    private boolean isCanClick = true;
    private boolean isValid = true;
    private OnIconClickCallback mCallback = null;
    private PathInterpolator mCancleInterpolator;
    private int mDirection = 0;
    private SlipCheckableListItem mDownView = null;
    private float mDownX = 0.0f;
    private float mDownY = 0.0f;
    private double mEffectTagent = Math.tan(0.08726646259971647d);
    private boolean mIsOpen = false;
    private AbsListView mListView = null;
    private boolean mPaused = false;
    private int mSlop;
    private Spring mSpring;
    private int mStatus = 0;
    private boolean mSwiping = false;
    private double mValidTagent = Math.tan(0.5235987755982988d);
    private VelocityTracker mVelocityTracker;
    private int mViewWidth = 1;
    private boolean running = false;

    public interface OnIconClickCallback {
        void onClick(int i, int i2);
    }

    private void clickOpration(int id) {
        if (this.mCallback != null && this.mDownView != null) {
            int position = this.mListView.getPositionForView(this.mDownView);
            Log.d(TAG, "click icon index: " + id + "  pos:" + position);
            this.mCallback.onClick(id, position);
        }
    }

    public void setAngleThreshold(int angle, int effectTagent) {
        this.mValidTagent = Math.tan((((double) angle) * 3.141592653589793d) / 180.0d);
        this.mEffectTagent = Math.tan((((double) effectTagent) * 3.141592653589793d) / 180.0d);
    }

    public SideSlipListViewListener(AbsListView listView, OnIconClickCallback callback) {
        this.mSlop = ViewConfiguration.get(listView.getContext()).getScaledTouchSlop();
        this.mListView = listView;
        this.mCallback = callback;
        this.mCancleInterpolator = (PathInterpolator) AnimationUtils.loadInterpolator(listView.getContext(), 50593837);
        this.mSpring = new Spring();
        this.MAX_VELOCITY_X = 1500.0f * listView.getResources().getDisplayMetrics().density;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0170  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0285  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0240  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouch(View view, MotionEvent motionEvent) {
        MotionEvent cancelEvent;
        if (this.mViewWidth < 2) {
            this.mViewWidth = this.mListView.getWidth();
        }
        if (this.mIsOpen) {
            if (this.running) {
                return true;
            }
            if (this.mDownView != null) {
                int[] listViewCoords = new int[2];
                try {
                    Rect itemRect;
                    float x;
                    float y;
                    if (!this.mListView.getIsHoldingMode() || this.mListView.getChildCount() <= 0 || this.mListView.getChildAt(0) == null) {
                        this.mListView.getLocationOnScreen(listViewCoords);
                        itemRect = new Rect();
                        this.mDownView.getHitRect(itemRect);
                        x = (float) ((int) (motionEvent.getRawX() - ((float) listViewCoords[0])));
                        y = (float) ((int) (motionEvent.getRawY() - ((float) listViewCoords[1])));
                        if (motionEvent.getActionMasked() == 0) {
                            if (itemRect.contains((int) x, (int) y) && this.mDownView.isClickIcon((int) (x - this.mDownView.getX()), (int) (y - this.mDownView.getY()))) {
                                this.mDownView.mIconState = iconState.PRESSED;
                            }
                        }
                        if (motionEvent.getActionMasked() == 2) {
                            boolean checkClickIcon;
                            if (this.mDownView.mIconState == iconState.PRESSED) {
                                if (itemRect.contains((int) x, (int) y)) {
                                    checkClickIcon = this.mDownView.checkClickIcon((int) (x - this.mDownView.getX()), (int) (y - this.mDownView.getY()));
                                    if (!checkClickIcon) {
                                        this.mDownView.mIconState = iconState.CANCLED;
                                    }
                                }
                            }
                            checkClickIcon = false;
                            if (checkClickIcon) {
                            }
                        }
                        if (motionEvent.getActionMasked() == 1) {
                            if (this.mDownView.mIconState == iconState.PRESSED) {
                                if (itemRect.contains((int) x, (int) y) && this.mDownView.checkClickIcon((int) (x - this.mDownView.getX()), (int) (y - this.mDownView.getY()))) {
                                    this.mDownView.mIconState = iconState.CLICK;
                                    this.mDownView.setIconPressState(false);
                                    clickOpration(this.mDownView.getClickIconId());
                                }
                            }
                            this.mDownView.mIconState = iconState.RESET;
                            this.mDownView.setIconPressState(false);
                        }
                        if (this.mDownView == null && this.mDownView.mIconState == iconState.RESET) {
                            resetSmoothly(true);
                            cancelEvent = MotionEvent.obtain(motionEvent);
                            cancelEvent.setAction((motionEvent.getActionIndex() << 8) | 3);
                            this.mListView.onTouchEvent(cancelEvent);
                            return true;
                        }
                        this.mDownView.setIconPressState(this.mDownView.mIconState != iconState.PRESSED);
                        return true;
                    }
                    this.mListView.getChildAt(0).getLocationOnScreen(listViewCoords);
                    itemRect = new Rect();
                    this.mDownView.getHitRect(itemRect);
                    x = (float) ((int) (motionEvent.getRawX() - ((float) listViewCoords[0])));
                    y = (float) ((int) (motionEvent.getRawY() - ((float) listViewCoords[1])));
                    if (motionEvent.getActionMasked() == 0) {
                    }
                    if (motionEvent.getActionMasked() == 2) {
                    }
                    if (motionEvent.getActionMasked() == 1) {
                    }
                    if (this.mDownView == null) {
                    }
                    if (this.mDownView.mIconState != iconState.PRESSED) {
                    }
                    this.mDownView.setIconPressState(this.mDownView.mIconState != iconState.PRESSED);
                    return true;
                } catch (Exception e) {
                    this.mListView.getLocationOnScreen(listViewCoords);
                }
            }
        } else if (this.running) {
            return true;
        }
        float deltaX;
        float refDeltaX;
        float iconsWidth;
        float pos;
        switch (motionEvent.getActionMasked()) {
            case 0:
                if (this.mPaused) {
                    return false;
                }
                this.mSwiping = false;
                View pointView = this.mListView.getChildAt(this.mListView.pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY()) - this.mListView.getFirstVisiblePosition());
                if (pointView instanceof SlipCheckableListItem) {
                    this.mDownView = (SlipCheckableListItem) pointView;
                    this.mDownView.initOprationArea(this.mDirection);
                } else {
                    this.mDownView = null;
                }
                this.mDownX = motionEvent.getRawX();
                this.mDownY = motionEvent.getRawY();
                if (this.mDownView == null) {
                    return false;
                }
                view.onTouchEvent(motionEvent);
                this.mVelocityTracker = VelocityTracker.obtain();
                this.mVelocityTracker.addMovement(motionEvent);
                this.isValid = true;
                this.isCanClick = true;
                return true;
            case 1:
                if (this.isValid) {
                    if (this.mDownView != null) {
                        if (!this.mPaused) {
                            if (this.mVelocityTracker != null) {
                                deltaX = motionEvent.getRawX() - this.mDownX;
                                if (this.mDirection == 1) {
                                    deltaX = Math.max(0.0f, deltaX);
                                } else {
                                    deltaX = Math.min(0.0f, deltaX);
                                }
                                refDeltaX = this.mDirection == 1 ? -deltaX : deltaX;
                                this.mVelocityTracker.addMovement(motionEvent);
                                this.mVelocityTracker.computeCurrentVelocity(Weather.WEATHERVERSION_ROM_2_0);
                                float velocityX = this.mVelocityTracker.getXVelocity();
                                boolean veloExpand = this.mDirection != 1 ? (-velocityX) <= 700.0f : velocityX <= 700.0f;
                                boolean expand = false;
                                if (((-refDeltaX) - (velocityX / 100.0f) > this.mDownView.getOprationAreaWidth() / 2.0f && Math.abs(this.mDownView.getSlipView().getTranslationX()) > 0.05f) || veloExpand) {
                                    expand = true;
                                }
                                Log.i(TAG, "velocityX = " + velocityX + "  del:" + deltaX + "  expand:" + expand);
                                if (this.mDirection == 1) {
                                    if (velocityX < 0.0f) {
                                        velocityX = 0.0f;
                                    }
                                } else if (velocityX > 0.0f) {
                                    velocityX = 0.0f;
                                }
                                if (expand && this.mSwiping) {
                                    this.mIsOpen = true;
                                    iconsWidth = this.mDownView.getOprationAreaWidth();
                                    pos = this.mDownView.getSlipView().getTranslationX();
                                    float endPos = this.mDirection == 1 ? iconsWidth : -iconsWidth;
                                    this.running = true;
                                    this.mSpring.setCurrentValue((double) pos);
                                    this.mSpring.setEndValue((double) endPos);
                                    this.mSpring.setSpringConfig(mFlingConfig);
                                    this.mSpring.setVelocity(((double) Math.max(velocityX, -this.MAX_VELOCITY_X)) * 0.4d);
                                    long startTime = SystemClock.uptimeMillis();
                                    ValueAnimator animator = ValueAnimator.ofFloat(new float[]{pos, endPos});
                                    final long j = startTime;
                                    animator.addUpdateListener(new AnimatorUpdateListener() {
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            if (SideSlipListViewListener.this.running) {
                                                SideSlipListViewListener.this.mStatus = 3;
                                                SideSlipListViewListener.this.mSpring.advance((double) (SystemClock.uptimeMillis() - j));
                                                float springPos = (float) SideSlipListViewListener.this.mSpring.getCurrentValue();
                                                SideSlipListViewListener.this.mDownView.getSlipView().setTranslationX(springPos);
                                                SideSlipListViewListener.this.mDownView.setCurrentPosition(springPos);
                                                SideSlipListViewListener.this.mDownView.invalidate();
                                            }
                                        }
                                    });
                                    animator.addListener(new AnimatorListenerAdapter() {
                                        public void onAnimationEnd(Animator animation) {
                                            SideSlipListViewListener.this.running = false;
                                            SideSlipListViewListener.this.mStatus = 1;
                                        }
                                    });
                                    animator.setDuration(300);
                                    animator.start();
                                } else {
                                    resetSmoothly(true);
                                }
                                this.mDownX = 0.0f;
                                this.mSwiping = false;
                                this.mVelocityTracker.recycle();
                                this.mVelocityTracker = null;
                                break;
                            }
                        }
                        return false;
                    }
                    return false;
                }
                return false;
                break;
            case 2:
                if (!this.isValid || (this.isCanClick ^ 1) != 0) {
                    return false;
                }
                if (this.mDownView == null) {
                    return false;
                }
                if (!(this.mVelocityTracker == null || this.mPaused)) {
                    float fitDeltaX;
                    this.mVelocityTracker.addMovement(motionEvent);
                    deltaX = motionEvent.getRawX() - this.mDownX;
                    float deltaY = motionEvent.getRawY() - this.mDownY;
                    if (this.mDirection == 1) {
                        fitDeltaX = Math.max(0.0f, deltaX);
                    } else {
                        fitDeltaX = Math.min(0.0f, deltaX);
                    }
                    refDeltaX = this.mDirection == 1 ? -fitDeltaX : fitDeltaX;
                    if (this.mSwiping || (-refDeltaX) <= ((float) this.mSlop)) {
                        if (!this.mSwiping && this.isCanClick && Math.abs(deltaX) > ((float) this.mSlop)) {
                            this.isCanClick = false;
                            if (((double) Math.abs(deltaX)) * this.mEffectTagent > ((double) Math.abs(deltaY))) {
                                cancelEvent = MotionEvent.obtain(motionEvent);
                                cancelEvent.setAction((motionEvent.getActionIndex() << 8) | 3);
                                this.mListView.onTouchEvent(cancelEvent);
                            }
                            return false;
                        }
                    } else if (((double) Math.abs(refDeltaX)) * this.mValidTagent < ((double) Math.abs(deltaY))) {
                        this.isValid = false;
                        return false;
                    } else {
                        this.mSwiping = true;
                        this.mStatus = 4;
                        this.mListView.requestDisallowInterceptTouchEvent(true);
                        cancelEvent = MotionEvent.obtain(motionEvent);
                        cancelEvent.setAction((motionEvent.getActionIndex() << 8) | 3);
                        this.mListView.onTouchEvent(cancelEvent);
                    }
                    if (this.mSwiping) {
                        if (refDeltaX == 0.0f) {
                            deltaX = 0.0f;
                        }
                        iconsWidth = this.mDownView.getOprationAreaWidth();
                        if (Math.abs(deltaX) <= iconsWidth) {
                            this.mDownView.getSlipView().setTranslationX(deltaX);
                            this.mDownView.setCurrentPosition(deltaX);
                        } else {
                            pos = (float) (((double) (-iconsWidth)) - Math.pow((double) (((-refDeltaX) - iconsWidth) / 1.0f), 0.95d));
                            if (this.mDirection == 1) {
                                pos = -pos;
                            }
                            this.mDownView.getSlipView().setTranslationX(pos);
                            this.mDownView.setCurrentPosition(pos);
                        }
                        this.mDownView.invalidate();
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public void resetSmoothly(final boolean right) {
        if (this.mDownView != null) {
            final float pos = this.mDownView.getSlipView().getTranslationX();
            int target = right ? 0 : this.mDirection == 1 ? this.mDownView.getWidth() : -this.mDownView.getWidth();
            final ValueAnimator animator = ValueAnimator.ofFloat(new float[]{pos, (float) target});
            this.running = true;
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (SideSlipListViewListener.this.running) {
                        SideSlipListViewListener.this.mStatus = 2;
                        float cutPos = ((Float) animator.getAnimatedValue()).floatValue();
                        SideSlipListViewListener.this.mDownView.getSlipView().setTranslationX(cutPos);
                        if (right) {
                            SideSlipListViewListener.this.mDownView.setCurrentPosition(cutPos);
                        } else {
                            SideSlipListViewListener.this.mDownView.setCurrentPosition(pos);
                        }
                        SideSlipListViewListener.this.mDownView.invalidate();
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    Log.i(SideSlipListViewListener.TAG, "Item is cancel to invisible.");
                    SideSlipListViewListener.this.setDeleteInvisible();
                }
            });
            animator.setDuration(300).setInterpolator(this.mCancleInterpolator);
            animator.start();
        }
    }

    private void setDeleteInvisible() {
        this.mIsOpen = false;
        this.running = false;
        this.mStatus = 0;
        if (this.mDownView != null) {
            this.mDownView.mIconState = iconState.RESET;
        }
    }

    public void setSlipEnabled(boolean enabled) {
        this.mPaused = enabled ^ 1;
    }

    public void setLayoutDirection(int rtl) {
        if (this.mDirection != rtl) {
            this.mDirection = rtl;
        }
    }

    public void resetDirectly() {
        if (this.mDownView != null) {
            this.mDownView.setIconPressState(false);
            this.mDownView.getSlipView().setTranslationX(0.0f);
            this.mDownView.setCurrentPosition(0.0f);
            Log.i(TAG, "reset to invisible.");
            this.mDownView.invalidate();
            setDeleteInvisible();
        }
    }

    public int getStatus() {
        return this.mStatus;
    }
}
