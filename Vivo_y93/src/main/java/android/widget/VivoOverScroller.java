package android.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.FloatMath;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoOverScroller {
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
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final int NB_SAMPLES = 100;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static float PHYSICAL_COEF = 0.0f;
        private static final int SCROLL = 4;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private long mLastTime;
        private double mLastVelocity;
        private int mOver;
        private int mSplineDistance;
        private int mSplineDuration;
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private float mTension;
        private int mVelocity;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < 100; i++) {
                float x;
                float coef;
                float y;
                float alpha = ((float) i) / 100.0f;
                float x_max = 1.0f;
                while (true) {
                    x = x_min + ((x_max - x_min) / 2.0f);
                    coef = (3.0f * x) * (1.0f - x);
                    float tx = ((((1.0f - x) * P1) + (P2 * x)) * coef) + ((x * x) * x);
                    if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                        break;
                    } else if (tx > alpha) {
                        x_max = x;
                    } else {
                        x_min = x;
                    }
                }
                SPLINE_POSITION[i] = ((((1.0f - x) * START_TENSION) + x) * coef) + ((x * x) * x);
                float y_max = 1.0f;
                while (true) {
                    y = y_min + ((y_max - y_min) / 2.0f);
                    coef = (3.0f * y) * (1.0f - y);
                    float dy = ((((1.0f - y) * START_TENSION) + y) * coef) + ((y * y) * y);
                    if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                        break;
                    } else if (dy > alpha) {
                        y_max = y;
                    } else {
                        y_min = y;
                    }
                }
                SPLINE_TIME[i] = ((((1.0f - y) * P1) + (P2 * y)) * coef) + ((y * y) * y);
            }
            float[] fArr = SPLINE_POSITION;
            SPLINE_TIME[100] = 1.0f;
            fArr[100] = 1.0f;
        }

        static void initFromContext(Context context) {
            PHYSICAL_COEF = (386.0878f * (context.getResources().getDisplayMetrics().density * 160.0f)) * 0.84f;
        }

        void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        SplineOverScroller() {
        }

        void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float x_sup = ((float) (index + 1)) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                this.mDuration = (int) (((float) this.mDuration) * (t_inf + (((x - x_inf) / (x_sup - x_inf)) * (SPLINE_TIME[index + 1] - t_inf))));
            }
        }

        void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
            this.mState = 4;
        }

        void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
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
            this.mFinished = true;
            this.mFinal = start;
            this.mStart = start;
            this.mVelocity = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
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
            this.mDuration = 400;
        }

        void fling(int start, int velocity, int min, int max, int over) {
            this.mOver = over;
            this.mFinished = false;
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mSplineDuration = 0;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
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
            double totalDistance = 0.0d;
            if (velocity != 0) {
                int splineFlingDuration = getSplineFlingDuration(velocity);
                this.mSplineDuration = splineFlingDuration;
                this.mDuration = splineFlingDuration;
                this.mDeceleration = (float) (getSplineDeceleration(velocity) * 1000.0d);
                totalDistance = getSplineFlingDistance(velocity);
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            if (this.mFinal < min) {
                adjustDuration(this.mStart, this.mFinal, min);
                this.mFinal = min;
            }
            if (this.mFinal > max) {
                adjustDuration(this.mStart, this.mFinal, max);
                this.mFinal = max;
            }
            this.mLastVelocity = (double) velocity;
            this.mLastTime = this.mStartTime;
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((((float) Math.abs(velocity)) * INFLEXION) / (this.mFlingFriction * PHYSICAL_COEF)));
        }

        private double getSplineFlingDistance(int velocity) {
            return ((double) (this.mFlingFriction * PHYSICAL_COEF)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - 1.0d)) * getSplineDeceleration(velocity));
        }

        private int getSplineFlingDuration(int velocity) {
            return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)) * 1000.0d);
        }

        void bounce(int edge, int range, int velocity, int duration) {
            float maxVelocity = (((float) range) * 0.09606f) / (((float) duration) * 1.0000001E-5f);
            float tension = velocity > 0 ? 10.0f : -10.0f;
            if (((float) Math.abs(velocity)) < maxVelocity) {
                tension = (((float) velocity) * 10.0f) / maxVelocity;
            }
            this.mStart = edge;
            this.mFinal = edge;
            this.mVelocity = velocity;
            this.mOver = range;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mTension = tension;
            this.mState = 3;
        }

        void notifyEdgeReached(int start, int end, int over) {
            if (this.mState == 0) {
                if (start == end) {
                    bounce(end, over, this.mVelocity, 400);
                    return;
                }
                adjustDuration(this.mStart, this.mFinal, this.mCurrentPosition - (start - end));
                this.mOver = over;
                this.mFinal = end;
                onEdgeReached();
            } else if (this.mState == 4) {
                this.mCurrentPosition = 0;
                this.mFinal = 0;
                this.mFinished = true;
            }
        }

        private void onEdgeReached() {
            long time = this.mStartTime + ((long) this.mDuration);
            int index = (int) (100.0f * (((float) this.mDuration) / ((float) this.mSplineDuration)));
            float velocityCoef = 0.0f;
            if (index < 100) {
                float t_inf = ((float) index) / 100.0f;
                float t_sup = ((float) (index + 1)) / 100.0f;
                velocityCoef = (SPLINE_POSITION[index + 1] - SPLINE_POSITION[index]) / (t_sup - t_inf);
            }
            this.mCurrVelocity = ((((float) this.mSplineDistance) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
            this.mDeceleration = (float) (((((double) this.mCurrVelocity) - this.mLastVelocity) / ((double) (time - this.mLastTime))) * 1000.0d);
            bounce(this.mFinal, this.mOver, (int) this.mCurrVelocity, 400);
            this.mStartTime = time;
            update();
        }

        boolean continueWhenFinished() {
            switch (this.mState) {
                case 0:
                    if (this.mDuration < this.mSplineDuration) {
                        onEdgeReached();
                        break;
                    }
                    return false;
                case 1:
                    return false;
                case 3:
                    return false;
            }
            update();
            return true;
        }

        boolean update() {
            long time = AnimationUtils.currentAnimationTimeMillis();
            long currentTime = time - this.mStartTime;
            if (currentTime > ((long) this.mDuration)) {
                return false;
            }
            double distance = 0.0d;
            float t;
            switch (this.mState) {
                case 0:
                    t = ((float) currentTime) / ((float) this.mSplineDuration);
                    int index = (int) (100.0f * t);
                    float distanceCoef = 1.0f;
                    float velocityCoef = 0.0f;
                    if (index < 100) {
                        float t_inf = ((float) index) / 100.0f;
                        float t_sup = ((float) (index + 1)) / 100.0f;
                        float d_inf = SPLINE_POSITION[index];
                        velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                    }
                    distance = (double) (((float) this.mSplineDistance) * distanceCoef);
                    this.mCurrVelocity = ((((float) this.mSplineDistance) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
                    this.mDeceleration = (float) (((((double) this.mCurrVelocity) - this.mLastVelocity) / ((double) (time - this.mLastTime))) * 1000.0d);
                    break;
                case 1:
                    t = ((float) currentTime) / ((float) this.mDuration);
                    distance = (double) ((1.0f - ((1.0f - t) * (1.0f - t))) * ((float) this.mOver));
                    break;
                case 3:
                    t = (((float) currentTime) / ((float) this.mDuration)) - 1.0f;
                    distance = (double) (((((((float) this.mOver) * t) * t) * t) * t) * ((this.mTension * t) + this.mTension));
                    break;
            }
            this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
            return true;
        }
    }

    public VivoOverScroller(Context context) {
        this(context, null);
    }

    public VivoOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public VivoOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
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

    public VivoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    public VivoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
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
                if (!(this.mScrollerX.mFinished || this.mScrollerX.update() || this.mScrollerX.continueWhenFinished())) {
                    this.mScrollerX.finish();
                }
                if (!(this.mScrollerY.mFinished || this.mScrollerY.update() || this.mScrollerY.continueWhenFinished())) {
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

    public void fling(long startTime, int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
        this.mScrollerX.mStartTime = startTime;
        this.mScrollerY.mStartTime = startTime;
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
