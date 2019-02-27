package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.FloatMath;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.vivo.internal.R;
import vivo.util.Spring;
import vivo.util.SpringConfig;

public class SpringOverScroller {
    private static final int DEFAULT_DURATION = 250;
    private static final int FLING_MODE = 1;
    private static final int SCROLL_MODE = 0;
    private final boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMode;
    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    static class SplineOverScroller {
        private static final int BOUNCE = 3;
        private static final int BOUNCE_DURANTION = 400;
        private static final int CUBIC = 1;
        private static final int SCROLL = 4;
        private static float SPEED_SCALE = 1.0f;
        private static final int SPLINE = 0;
        private static SpringConfig mBounceConfig = new SpringConfig(100.0d, 22.0d);
        private static SpringConfig mCubicConfig = new SpringConfig(100.0d, 17.0d);
        private static SpringConfig mFlingConfig = new SpringConfig(0.0d, 2.5d);
        private float mCurrVelocity;
        private int mCurrentPosition;
        private int mDuration;
        private int mFinal;
        private boolean mFinished;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private int mOver;
        private Spring mSpring = new Spring();
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private int mVelocity;

        static void initFromContext(Context context) {
            Resources res = context.getResources();
            float friction = ((float) res.getInteger(R.integer.vigour_config_springFriction)) / 100.0f;
            SPEED_SCALE = ((float) res.getInteger(R.integer.vigour_config_springSpeedScale)) / 100.0f;
            mFlingConfig = new SpringConfig(0.0d, (double) friction);
        }

        void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        SplineOverScroller() {
            this.mSpring.setRestDisplacementThreshold(1.0d);
            this.mFinished = true;
        }

        void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mVelocity = 0;
            this.mState = 4;
        }

        void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
            this.mSpring.setAtRest();
        }

        void setFinalPosition(int position) {
            this.mFinal = position;
            this.mFinished = false;
        }

        void extendDuration(int extend) {
            this.mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) + extend;
            this.mFinished = false;
        }

        boolean springback(int start, int min, int max) {
            this.mFinal = start;
            this.mStart = start;
            this.mVelocity = 0;
            this.mDuration = 0;
            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }
            return this.mFinished ^ 1;
        }

        private void startSpringback(int start, int end, int velocity) {
            this.mFinished = false;
            this.mState = 1;
            this.mStart = start;
            this.mFinal = end;
            this.mOver = end - start;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mSpring.setSpringConfig(mCubicConfig);
            this.mSpring.setCurrentValue((double) start);
            this.mSpring.setEndValue((double) end);
        }

        void fling(int start, int velocity, int min, int max, int over) {
            velocity = (int) (((float) velocity) * SPEED_SCALE);
            this.mOver = over;
            this.mFinished = false;
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mDuration = 0;
            this.mStart = start;
            this.mCurrentPosition = start;
            if (start > max || start < min) {
                if (start <= max) {
                    max = min;
                }
                startSpringback(start, max, velocity);
                return;
            }
            this.mState = 0;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mSpring.setCurrentValue((double) start);
            this.mSpring.setVelocity((double) velocity);
            this.mSpring.setSpringConfig(mFlingConfig);
            this.mSpring.setRestSpeedThreshold(50.0d);
            Spring spring = this.mSpring;
            if (start < max) {
                min = max;
            }
            spring.setEndValue((double) min);
        }

        void notifyEdgeReached(int start, int end, int over) {
            if (this.mState == 0) {
                float v = (float) this.mSpring.getVelocity();
                this.mSpring.setSpringConfig(mBounceConfig);
                this.mState = 3;
                this.mStart = start;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mSpring.setCurrentValue((double) start);
                this.mSpring.setVelocity((double) v);
                this.mSpring.setEndValue((double) end);
                this.mFinal = end;
            } else if (this.mState == 4) {
                this.mCurrentPosition = 0;
                this.mFinal = 0;
                this.mFinished = true;
            }
        }

        boolean update() {
            long current = SystemClock.uptimeMillis();
            this.mSpring.advance(((double) (current - this.mStartTime)) / 1000.0d);
            this.mStartTime = current;
            switch (this.mState) {
                case 0:
                    int round = (int) Math.round(this.mSpring.getCurrentValue());
                    this.mCurrentPosition = round;
                    this.mFinal = round;
                    return this.mSpring.isAtRest() ^ 1;
                case 1:
                    this.mCurrentPosition = (int) Math.round(this.mSpring.getCurrentValue());
                    if ((this.mStart >= 0 || this.mCurrentPosition < this.mFinal) && ((this.mStart <= 0 || this.mCurrentPosition > this.mFinal) && !this.mSpring.isAtRest())) {
                        return true;
                    }
                    this.mCurrentPosition = 0;
                    if (!this.mSpring.isAtRest()) {
                        this.mSpring.setAtRest();
                    }
                    return false;
                case 3:
                    this.mCurrentPosition = (int) Math.round(this.mSpring.getCurrentValue());
                    if ((this.mStart <= 0 || this.mCurrentPosition > this.mFinal) && ((this.mStart >= 0 || this.mCurrentPosition < this.mFinal) && !this.mSpring.isAtRest())) {
                        return true;
                    }
                    this.mCurrentPosition = 0;
                    if (!this.mSpring.isAtRest()) {
                        this.mSpring.setAtRest();
                    }
                    return false;
                default:
                    return true;
            }
        }
    }

    public SpringOverScroller(Context context) {
        this(context, null);
    }

    public SpringOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public SpringOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFlywheel = flywheel;
        this.mScrollerX = new SplineOverScroller();
        this.mScrollerY = new SplineOverScroller();
        SplineOverScroller.initFromContext(context);
    }

    public SpringOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    public SpringOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public final void setFriction(float friction) {
        this.mScrollerX.setFriction(friction);
        this.mScrollerY.setFriction(friction);
    }

    public final boolean isFinished() {
        return this.mScrollerX.mFinished ? this.mScrollerY.mFinished : false;
    }

    public final void forceFinished(boolean finished) {
        this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
    }

    public final int getCurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    public final int getCurrY() {
        return this.mScrollerY.mCurrentPosition;
    }

    public float getCurrVelocity() {
        return FloatMath.sqrt((this.mScrollerX.mCurrVelocity * this.mScrollerX.mCurrVelocity) + (this.mScrollerY.mCurrVelocity * this.mScrollerY.mCurrVelocity));
    }

    public final int getStartX() {
        return this.mScrollerX.mStart;
    }

    public final int getStartY() {
        return this.mScrollerY.mStart;
    }

    public final int getFinalX() {
        return this.mScrollerX.mFinal;
    }

    public final int getFinalY() {
        return this.mScrollerY.mFinal;
    }

    @Deprecated
    public final int getDuration() {
        return Math.max(this.mScrollerX.mDuration, this.mScrollerY.mDuration);
    }

    @Deprecated
    public void extendDuration(int extend) {
        this.mScrollerX.extendDuration(extend);
        this.mScrollerY.extendDuration(extend);
    }

    @Deprecated
    public void setFinalX(int newX) {
        this.mScrollerX.setFinalPosition(newX);
    }

    @Deprecated
    public void setFinalY(int newY) {
        this.mScrollerY.setFinalPosition(newY);
    }

    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }
        switch (this.mMode) {
            case 0:
                long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
                int duration = this.mScrollerX.mDuration;
                if (elapsedTime >= ((long) duration)) {
                    abortAnimation();
                    break;
                }
                float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                if (!this.mScrollerX.mFinished) {
                    this.mScrollerX.updateScroll(q);
                }
                if (!this.mScrollerY.mFinished) {
                    this.mScrollerY.updateScroll(q);
                    break;
                }
                break;
            case 1:
                if (!(this.mScrollerX.mFinished || this.mScrollerX.update())) {
                    this.mScrollerX.finish();
                }
                if (!(this.mScrollerY.mFinished || this.mScrollerY.update())) {
                    this.mScrollerY.finish();
                    break;
                }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        return !this.mScrollerX.springback(startX, minX, maxX) ? this.mScrollerY.springback(startY, minY, maxY) : true;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (this.mFlywheel && (isFinished() ^ 1) != 0) {
            float oldVelocityX = this.mScrollerX.mCurrVelocity;
            float oldVelocityY = this.mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        this.mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        if (this.mScrollerX.mFinished || this.mScrollerX.mState == 0) {
            return (this.mScrollerY.mFinished || this.mScrollerY.mState == 0) ? false : true;
        } else {
            return true;
        }
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        int dx = this.mScrollerX.mFinal - this.mScrollerX.mStart;
        int dy = this.mScrollerY.mFinal - this.mScrollerY.mStart;
        if (!isFinished() && Math.signum(xvel) == Math.signum((float) dx) && Math.signum(yvel) == Math.signum((float) dy)) {
            return true;
        }
        return false;
    }
}
