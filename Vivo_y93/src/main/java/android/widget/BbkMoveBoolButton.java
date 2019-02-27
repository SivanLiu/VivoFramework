package android.widget;

import android.animation.ValueAnimator;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.FtBuild;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BbkMoveBoolButton extends View implements Checkable {
    private static final int CLICK_ANIMATE_MSG = 0;
    private static final int CLICK_ANIMATION_TIME = 330;
    private static final float CLICK_OFFSET_SCALE = 0.27f;
    private static final int DRAG_ANIMATE_MSG = 1;
    private static final int END_ANIMATION_MSG = 4;
    private static final int LOADING_PROGRESS = 3;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private static final int TOUCH_MODE_IDLE = 0;
    private String TAG;
    private boolean bInAnimate;
    private int bgDrawableHeight;
    private Rect mBgRect;
    private boolean mChecked;
    private int mCircleRadius;
    private int mEnd;
    private int mHandPos;
    private ValueAnimator mInterpolator;
    private boolean mIsLoading;
    private boolean mIsStartLoading;
    private boolean mIsStopLoading;
    private int mLeftHandPos;
    private float mLoadingAngle;
    private int mLoadingColor;
    private int mMaxHandWidth;
    private Drawable mOffBgDrawable;
    private int mOffset;
    private int mOffset2;
    private OnCheckedChangeListener mOnBBKCheckedChangeListener;
    private Drawable mOnBgDrawable;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private Paint mPaintForLoading;
    private Path mPath;
    private PathInterpolator mPathInterpolator;
    private int mRightHandPos;
    private float mRomVersion;
    private int mScrollRange;
    private int mStart;
    private int mStartLoadingAlpha;
    long mStartTime;
    private float mStepAngle;
    private int mStopLoadingAlpha;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private Drawable mTrackHandDrawable;
    private Drawable mTrackHandDrawableDisabled;
    private Drawable mTrackLeftHandDrawable;
    private Drawable mTrackLeftHandDrawableDisabled;
    private Drawable mTrackRightHandDrawable;
    private Drawable mTrackRightHandDrawableDisabled;
    private Handler mhandler;
    private int trackHandDrawableHeight;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(BbkMoveBoolButton bbkMoveBoolButton, boolean z);
    }

    public static class Status {
        public static final int END_LOADING = 2;
        public static final int LOADING = 1;
        public static final int NORMAL = 3;
        public static final int START_LOADING = 0;
        float angle;
        float progress;
        int status;
    }

    public BbkMoveBoolButton(Context context) {
        this(context, null);
    }

    public BbkMoveBoolButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.moveBoolButtonStyle);
    }

    public BbkMoveBoolButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BbkMoveBoolButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "VivoMoveBoolButton";
        this.mIsLoading = false;
        this.mLoadingAngle = 0.0f;
        this.mStartLoadingAlpha = 0;
        this.mStopLoadingAlpha = 0;
        this.mIsStartLoading = false;
        this.mIsStopLoading = false;
        this.mStepAngle = 4.27f;
        this.mChecked = true;
        this.mPath = new Path();
        this.mBgRect = new Rect();
        this.bInAnimate = false;
        this.mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        float normalizedTime = ((float) (SystemClock.elapsedRealtime() - BbkMoveBoolButton.this.mStartTime)) / 330.0f;
                        BbkMoveBoolButton.this.mOffset = BbkMoveBoolButton.this.mStart + ((int) (((float) (BbkMoveBoolButton.this.mEnd - BbkMoveBoolButton.this.mStart)) * BbkMoveBoolButton.this.mInterpolator.getInterpolator().getInterpolation(Math.max(Math.min(normalizedTime, 1.0f), 0.0f))));
                        if (normalizedTime > BbkMoveBoolButton.CLICK_OFFSET_SCALE) {
                            BbkMoveBoolButton.this.mOffset2 = BbkMoveBoolButton.this.mStart + ((int) (((float) (BbkMoveBoolButton.this.mEnd - BbkMoveBoolButton.this.mStart)) * BbkMoveBoolButton.this.mInterpolator.getInterpolator().getInterpolation(Math.max(Math.min(normalizedTime - BbkMoveBoolButton.CLICK_OFFSET_SCALE, 1.0f), 0.0f))));
                        }
                        boolean bMore = normalizedTime - BbkMoveBoolButton.CLICK_OFFSET_SCALE < 1.0f;
                        BbkMoveBoolButton.this.invalidate();
                        if (bMore && BbkMoveBoolButton.this.bInAnimate) {
                            BbkMoveBoolButton.this.mhandler.sendEmptyMessage(0);
                            return;
                        } else {
                            BbkMoveBoolButton.this.mhandler.sendEmptyMessageDelayed(4, 20);
                            return;
                        }
                    case 1:
                        if (BbkMoveBoolButton.this.mStart != BbkMoveBoolButton.this.mEnd) {
                            if (Math.abs(BbkMoveBoolButton.this.mStart - BbkMoveBoolButton.this.mEnd) <= 2) {
                                BbkMoveBoolButton.this.mStart = BbkMoveBoolButton.this.mEnd;
                            } else {
                                BbkMoveBoolButton.this.mStart = BbkMoveBoolButton.this.mStart + ((BbkMoveBoolButton.this.mEnd - BbkMoveBoolButton.this.mStart) / 2);
                            }
                            BbkMoveBoolButton.this.mOffset = BbkMoveBoolButton.this.mStart;
                            BbkMoveBoolButton.this.invalidate();
                            BbkMoveBoolButton.this.mhandler.sendEmptyMessageDelayed(1, 20);
                            return;
                        }
                        BbkMoveBoolButton.this.endOfAnimation();
                        BbkMoveBoolButton.this.invalidate();
                        return;
                    case 3:
                        if (!BbkMoveBoolButton.this.mIsLoading || BbkMoveBoolButton.this.bInAnimate) {
                            BbkMoveBoolButton.this.mhandler.removeMessages(3);
                            return;
                        }
                        BbkMoveBoolButton bbkMoveBoolButton = BbkMoveBoolButton.this;
                        bbkMoveBoolButton.mLoadingAngle = bbkMoveBoolButton.mLoadingAngle + BbkMoveBoolButton.this.mStepAngle;
                        if (BbkMoveBoolButton.this.mLoadingAngle >= Float.MAX_VALUE - BbkMoveBoolButton.this.mStepAngle) {
                            BbkMoveBoolButton.this.mLoadingAngle = 0.0f;
                        }
                        int alpha;
                        if (BbkMoveBoolButton.this.mIsStopLoading) {
                            alpha = Math.max(BbkMoveBoolButton.this.mPaintForLoading.getAlpha() - 15, 0);
                            BbkMoveBoolButton.this.mPaintForLoading.setAlpha(alpha);
                            if (alpha == 0) {
                                BbkMoveBoolButton.this.mIsLoading = false;
                                BbkMoveBoolButton.this.mIsStartLoading = false;
                                BbkMoveBoolButton.this.mIsStopLoading = false;
                            }
                        } else if (BbkMoveBoolButton.this.mIsStartLoading) {
                            alpha = Math.min(BbkMoveBoolButton.this.mPaintForLoading.getAlpha() + 20, 255);
                            BbkMoveBoolButton.this.mPaintForLoading.setAlpha(alpha);
                            if (alpha == 255) {
                                BbkMoveBoolButton.this.mIsStartLoading = false;
                                BbkMoveBoolButton.this.mIsStopLoading = false;
                            }
                        }
                        BbkMoveBoolButton.this.postInvalidate();
                        BbkMoveBoolButton.this.mhandler.sendEmptyMessageDelayed(3, 16);
                        return;
                    case 4:
                        BbkMoveBoolButton.this.endOfAnimation();
                        return;
                    default:
                        return;
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoveBoolButton, defStyleAttr, defStyleRes);
        this.mOnBgDrawable = a.getDrawable(0);
        this.mOffBgDrawable = a.getDrawable(1);
        this.mTrackHandDrawable = a.getDrawable(2);
        this.mTrackHandDrawableDisabled = a.getDrawable(3);
        this.mTrackLeftHandDrawable = a.getDrawable(4);
        this.mTrackRightHandDrawable = a.getDrawable(5);
        this.mTrackLeftHandDrawableDisabled = a.getDrawable(6);
        this.mTrackRightHandDrawableDisabled = a.getDrawable(7);
        this.mLoadingColor = a.getColor(8, -16777216);
        this.mPaddingTop = a.getDimensionPixelSize(9, 10);
        this.mPaddingBottom = a.getDimensionPixelSize(10, 10);
        this.mMaxHandWidth = a.getDimensionPixelSize(11, 0);
        this.mPathInterpolator = (PathInterpolator) AnimationUtils.loadInterpolator(getContext(), a.getResourceId(12, 0));
        this.bgDrawableHeight = this.mOnBgDrawable.getIntrinsicHeight();
        this.trackHandDrawableHeight = this.mTrackHandDrawable.getIntrinsicHeight();
        a.recycle();
        init(context);
    }

    private void init(Context context) {
        this.mTouchSlop = (int) (((float) ViewConfiguration.get(context).getScaledTouchSlop()) * 1.5f);
        float density = getContext().getResources().getDisplayMetrics().density;
        this.mRomVersion = FtBuild.getRomVersion();
        if (this.mRomVersion >= 3.0f) {
            this.mMaxHandWidth = (int) Math.min((float) this.mMaxHandWidth, 10.0f * density);
        } else {
            this.mMaxHandWidth = 0;
        }
        this.mCircleRadius = this.mTrackHandDrawable.getIntrinsicWidth() / 2;
        this.mLeftHandPos = (this.mPaddingLeft + this.mCircleRadius) + ((int) (1.0f * density));
        this.mHandPos = ((this.mPaddingLeft + this.mOnBgDrawable.getIntrinsicWidth()) - this.mTrackHandDrawable.getIntrinsicWidth()) - ((this.bgDrawableHeight - this.trackHandDrawableHeight) / 2);
        this.mRightHandPos = ((this.mHandPos + this.mOnBgDrawable.getIntrinsicWidth()) - (this.mTrackHandDrawable.getIntrinsicWidth() / 2)) - (this.mTrackRightHandDrawable.getIntrinsicHeight() / 2);
        this.mScrollRange = (this.mOnBgDrawable.getIntrinsicWidth() - this.mTrackHandDrawable.getIntrinsicWidth()) - (this.bgDrawableHeight - this.trackHandDrawableHeight);
        this.mPaintForLoading = new Paint();
        this.mPaintForLoading.setColor(context.getResources().getColor(R.color.vigour_progressloading_check_on_enable_focused_light));
        this.mPaintForLoading.setStyle(Style.FILL);
        this.mPaintForLoading.setAlpha(0);
        this.mPaintForLoading.setAntiAlias(true);
        this.mPaintForLoading.setStrokeWidth(2.0f);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = this.mOnBgDrawable.getIntrinsicWidth();
        int height = this.mOnBgDrawable.getIntrinsicHeight();
        setMeasuredDimension((this.mPaddingLeft + width) + this.mPaddingRight, (this.mPaddingTop + height) + this.mPaddingBottom);
        this.mBgRect.set(this.mPaddingLeft, this.mPaddingTop, this.mPaddingLeft + width, this.mPaddingTop + height);
    }

    protected void onDraw(Canvas canvas) {
        Rect rect;
        canvas.save();
        if (getLayoutDirection() == 1) {
            canvas.translate((float) getWidth(), 0.0f);
            canvas.scale(-1.0f, 1.0f);
        }
        super.onDraw(canvas);
        int alpha = 255 - ((this.mOffset * 255) / this.mScrollRange);
        if (alpha != 255) {
            this.mOffBgDrawable.setBounds(this.mBgRect);
            this.mOffBgDrawable.draw(canvas);
        }
        this.mOnBgDrawable.setAlpha(alpha);
        this.mOnBgDrawable.setBounds(this.mBgRect);
        this.mOnBgDrawable.draw(canvas);
        Drawable handDrawable = this.mTrackHandDrawable;
        if (!isEnabled()) {
            handDrawable = this.mTrackHandDrawableDisabled;
        }
        int w = handDrawable.getIntrinsicWidth();
        int h = handDrawable.getIntrinsicHeight();
        int handLeft = this.mHandPos - this.mOffset;
        int handLeft2 = this.mHandPos - this.mOffset2;
        if (this.mMaxHandWidth == 0) {
            rect = new Rect(this.mHandPos - this.mOffset, (getHeight() - h) / 2, (this.mHandPos - this.mOffset) + w, ((getHeight() - h) / 2) + h);
        } else if (this.mTouchMode != 2) {
            rect = new Rect(Math.min(handLeft, handLeft2), (getHeight() - h) / 2, Math.max(handLeft, handLeft2) + w, ((getHeight() - h) / 2) + h);
        } else if (handLeft <= this.mMaxHandWidth) {
            rect = new Rect(this.mHandPos - this.mScrollRange, (getHeight() - h) / 2, ((handLeft * 2) + w) - (this.mHandPos - this.mScrollRange), ((getHeight() - h) / 2) + h);
        } else if (this.mMaxHandWidth + handLeft >= this.mHandPos) {
            rect = new Rect(handLeft - this.mOffset, (getHeight() - h) / 2, this.mHandPos + w, ((getHeight() - h) / 2) + h);
        } else {
            rect = new Rect(handLeft - this.mMaxHandWidth, (getHeight() - h) / 2, (handLeft + w) + this.mMaxHandWidth, ((getHeight() - h) / 2) + h);
        }
        handDrawable.setBounds(rect);
        handDrawable.draw(canvas);
        drawProgressLoading(canvas, rect, this.mLoadingAngle);
        canvas.save();
        if (!(alpha == 0 || alpha == 255)) {
            this.mPath.reset();
            this.mPath.addCircle((float) ((this.mPaddingLeft + this.mCircleRadius) + 5), (float) (getHeight() / 2), (float) this.mCircleRadius, Direction.CCW);
            this.mPath.addRect((float) (this.mCircleRadius + 5), 0.0f, (float) (getWidth() - this.mCircleRadius), (float) getHeight(), Direction.CCW);
            this.mPath.addCircle((float) (((getWidth() - this.mCircleRadius) - 5) - this.mPaddingRight), (float) (getHeight() / 2), (float) this.mCircleRadius, Direction.CCW);
            canvas.clipPath(this.mPath, Op.REPLACE);
        }
        if (this.mRomVersion < 3.0f) {
            Drawable leftHandDrawable = this.mTrackLeftHandDrawable;
            if (!isEnabled()) {
                leftHandDrawable = this.mTrackLeftHandDrawableDisabled;
            }
            w = leftHandDrawable.getIntrinsicWidth();
            h = leftHandDrawable.getIntrinsicHeight();
            leftHandDrawable.setAlpha(Math.min(255, Math.max(0, 255 - ((this.mOffset * 255) / (this.mCircleRadius + 5)))));
            leftHandDrawable.setBounds(this.mLeftHandPos - this.mOffset, (getHeight() - h) / 2, (this.mLeftHandPos - this.mOffset) + w, ((getHeight() - h) / 2) + h);
            leftHandDrawable.draw(canvas);
            Drawable rightHandDrawable = this.mTrackRightHandDrawable;
            if (!isEnabled()) {
                rightHandDrawable = this.mTrackRightHandDrawableDisabled;
            }
            w = rightHandDrawable.getIntrinsicWidth();
            h = rightHandDrawable.getIntrinsicHeight();
            rightHandDrawable.setAlpha(Math.min(255, Math.max(0, 255 - (((this.mScrollRange - this.mOffset) * 255) / (this.mCircleRadius + 5)))));
            rightHandDrawable.setBounds(this.mRightHandPos - this.mOffset, (getHeight() - h) / 2, (this.mRightHandPos - this.mOffset) + w, ((getHeight() - h) / 2) + h);
            rightHandDrawable.draw(canvas);
            canvas.restore();
        }
        canvas.restore();
    }

    private void drawProgressLoading(Canvas canvas, Rect rect, float angle) {
        if (this.mIsLoading) {
            int i;
            float[] center = new float[]{(float) ((rect.left + rect.right) / 2), (float) ((rect.top + rect.bottom) / 2)};
            canvas.save();
            canvas.rotate(angle, center[0], center[1]);
            int radius = rect.width() / 2;
            float[][] moons = new float[6][];
            for (i = 0; i < 6; i++) {
                moons[i] = getConnection((float) (radius / 2), ((float) i) * 1.0471976f, center);
            }
            for (i = 0; i < 6; i++) {
                canvas.drawCircle(moons[i][0], moons[i][1], 3.0f, this.mPaintForLoading);
            }
            canvas.restore();
            return;
        }
        if (this.mPaintForLoading.getAlpha() != 0) {
            this.mPaintForLoading.setAlpha(0);
        }
    }

    private float[] getConnection(float radius, float angle, float[] center) {
        float[] vector = getVector(radius, angle);
        vector[0] = vector[0] + center[0];
        vector[1] = vector[1] + center[1];
        return vector;
    }

    private float[] getVector(float radius, float angle) {
        return new float[]{(float) (((double) radius) * Math.cos((double) angle)), (float) (((double) radius) * Math.sin((double) angle))};
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || this.bInAnimate || this.mIsLoading) {
            return false;
        }
        float x;
        switch (ev.getActionMasked()) {
            case 0:
                x = ev.getX();
                if (isEnabled()) {
                    this.mTouchMode = 1;
                    this.mTouchX = x;
                    return true;
                }
                break;
            case 1:
                if (this.mTouchMode != 2) {
                    this.mChecked ^= 1;
                    clickAnimateToCheckedState(this.mChecked);
                    this.mTouchMode = 0;
                    break;
                }
                stopDrag();
                return true;
            case 2:
                switch (this.mTouchMode) {
                    case 1:
                        x = ev.getX();
                        if (Math.abs(x - this.mTouchX) > ((float) this.mTouchSlop)) {
                            this.mTouchMode = 2;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            this.mTouchX = x;
                            return true;
                        }
                        break;
                    case 2:
                        x = ev.getX();
                        this.mOffset = Math.max(0, Math.min(this.mOffset + ((int) (this.mTouchX - x)), this.mScrollRange));
                        this.mTouchX = x;
                        invalidate();
                        return true;
                }
                break;
            case 3:
                if (this.mTouchMode != 2) {
                    this.mTouchMode = 0;
                    break;
                }
                stopDrag();
                return true;
        }
        return true;
    }

    public boolean performClick() {
        if (this.mTouchMode == 2) {
            stopDrag();
        } else {
            this.mChecked ^= 1;
            clickAnimateToCheckedState(this.mChecked);
        }
        this.mTouchMode = 0;
        return super.performClick();
    }

    private void stopDrag() {
        if (this.mOffset >= this.mScrollRange / 2) {
            animateToCheckedState(false);
        } else {
            animateToCheckedState(true);
        }
    }

    private void animateToCheckedState(boolean newCheckedState) {
        this.mChecked = newCheckedState;
        int targetPos = newCheckedState ? 0 : this.mScrollRange;
        playSoundEffect(0);
        this.bInAnimate = true;
        this.mStart = this.mOffset;
        this.mEnd = targetPos;
        this.mhandler.sendEmptyMessage(1);
    }

    private void clickAnimateToCheckedState(boolean newCheckedState) {
        int targetPos = newCheckedState ? 0 : this.mScrollRange;
        playSoundEffect(0);
        this.bInAnimate = true;
        ensureInterpolator();
        this.mStart = this.mOffset;
        this.mEnd = targetPos;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mhandler.sendEmptyMessage(0);
    }

    public boolean startLoading() {
        if (this.bInAnimate) {
            return false;
        }
        if (this.mIsLoading) {
            return true;
        }
        this.mLoadingAngle = 0.0f;
        this.mIsLoading = true;
        this.mIsStartLoading = true;
        this.mIsStopLoading = false;
        this.mStartLoadingAlpha = this.mStopLoadingAlpha;
        this.mhandler.sendEmptyMessage(3);
        return true;
    }

    public boolean isLoading() {
        return this.mIsLoading;
    }

    public boolean endLoading() {
        if (!this.mIsLoading) {
            return false;
        }
        this.mIsStopLoading = true;
        this.mIsStartLoading = false;
        this.mStartLoadingAlpha = this.mStartLoadingAlpha;
        this.mhandler.sendEmptyMessageDelayed(3, 16);
        return true;
    }

    protected void onDetachedFromWindow() {
        shutdownLoading();
        super.onDetachedFromWindow();
    }

    public void shutdownLoading() {
        this.mhandler.removeMessages(3);
    }

    private void configAnimator() {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.setInterpolator(this.mPathInterpolator);
        this.mInterpolator = anim.setDuration(330);
    }

    protected void ensureInterpolator() {
        if (this.mInterpolator == null) {
            configAnimator();
        }
    }

    private void endOfAnimation() {
        this.bInAnimate = false;
        if (this.mOnBBKCheckedChangeListener != null) {
            this.mOnBBKCheckedChangeListener.onCheckedChanged(this, this.mChecked);
        }
        this.mOffset2 = this.mOffset;
        this.mTouchMode = 0;
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        setChecked(this.mChecked ^ 1);
    }

    public void setChecked(boolean checked) {
        if (!this.bInAnimate) {
            if (this.mChecked != checked) {
                this.mChecked = checked;
            }
            if (this.mChecked) {
                this.mOffset2 = 0;
                this.mOffset = 0;
            } else {
                int i = this.mScrollRange;
                this.mOffset2 = i;
                this.mOffset = i;
            }
            invalidate();
        }
    }

    public void setLoadingStatu(boolean loading) {
        this.mIsLoading = loading;
        this.mIsStartLoading = loading;
    }

    public void removeAnimation() {
        this.bInAnimate = false;
        if (this.mhandler != null) {
            this.mhandler.removeMessages(0);
        }
    }

    public void setOnBBKCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnBBKCheckedChangeListener = listener;
    }

    public Status getStatus() {
        Status status = new Status();
        status.angle = this.mLoadingAngle;
        if (this.mIsStartLoading) {
            status.status = 0;
            status.progress = (((float) this.mStartLoadingAlpha) * 1.0f) / 256.0f;
        } else if (this.mIsStopLoading) {
            status.status = 2;
            status.progress = 1.0f - ((((float) this.mStopLoadingAlpha) * 1.0f) / 256.0f);
        } else if (this.mIsLoading) {
            status.status = 1;
        } else {
            status.status = 3;
        }
        shutdownLoading();
        return status;
    }

    public void startLoading(Status status) {
        if (status != null) {
            if (status.status != 3) {
                this.mIsLoading = true;
            }
            switch (status.status) {
                case 0:
                    this.mIsStartLoading = true;
                    this.mIsStopLoading = false;
                    this.mStartLoadingAlpha = (int) (status.progress * 256.0f);
                    break;
                case 2:
                    this.mIsStartLoading = false;
                    this.mIsStopLoading = true;
                    this.mStopLoadingAlpha = (int) ((1.0f - status.progress) * 256.0f);
                    break;
                default:
                    this.mIsStartLoading = false;
                    this.mIsStopLoading = false;
                    break;
            }
            postInvalidate();
            this.mhandler.removeMessages(3);
            this.mhandler.sendEmptyMessageDelayed(3, 16);
        }
    }
}
