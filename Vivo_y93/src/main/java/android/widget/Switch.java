package android.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.FtBuild;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.AllCapsTransformationMethod;
import android.text.method.TransformationMethod2;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.widget.TextView.BufferType;
import com.android.internal.R;
import vivo.util.VivoThemeUtil;

public class Switch extends CompoundButton {
    private static final int[] CHECKED_STATE_SET = new int[]{R.attr.state_checked};
    private static final int CLICK_ANIMATE_MSG = 0;
    private static final int CLICK_ANIMATION_TIME = 330;
    private static final float CLICK_OFFSET_SCALE = 0.27f;
    private static final int DRAG_ANIMATE_MSG = 1;
    private static final int END_ANIMATION_MSG = 3;
    private static final int MONOSPACE = 3;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int THUMB_ANIMATION_DURATION = 250;
    private static final FloatProperty<Switch> THUMB_POS = new FloatProperty<Switch>("thumbPos") {
        public Float get(Switch object) {
            return Float.valueOf(object.mThumbPosition);
        }

        public void setValue(Switch object, float value) {
            object.setThumbPosition(value);
        }
    };
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private static final int TOUCH_MODE_IDLE = 0;
    private int Vigour_mTouchSlop;
    private boolean bInAnimate;
    private boolean isFirstDraw;
    private Rect mBgRect;
    private boolean mChecked;
    private int mCircleRadius;
    private int mEnd;
    private int mHandPos;
    private boolean mHasThumbTint;
    private boolean mHasThumbTintMode;
    private boolean mHasTrackTint;
    private boolean mHasTrackTintMode;
    private ValueAnimator mInterpolator;
    private int mLeftHandPos;
    private int mMaxHandWidth;
    private int mMinFlingVelocity;
    Drawable mOffBgDrawable;
    private Layout mOffLayout;
    private int mOffset;
    private int mOffset2;
    Drawable mOnBgDrawable;
    private Layout mOnLayout;
    private Path mPath;
    private PathInterpolator mPathInterpolator;
    private ObjectAnimator mPositionAnimator;
    private int mRightHandPos;
    private float mRomVersion;
    private int mScrollRange;
    private boolean mShowText;
    private boolean mSplitTrack;
    private int mStart;
    private long mStartTime;
    private int mSwitchBottom;
    private int mSwitchHeight;
    private int mSwitchLeft;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private int mSwitchRight;
    private int mSwitchTop;
    private TransformationMethod2 mSwitchTransformationMethod;
    private int mSwitchWidth;
    private final Rect mTempRect;
    private ColorStateList mTextColors;
    private CharSequence mTextOff;
    private CharSequence mTextOn;
    private TextPaint mTextPaint;
    private Drawable mThumbDrawable;
    private Drawable mThumbHandDrawableDisabled;
    private float mThumbPosition;
    private int mThumbTextPadding;
    private ColorStateList mThumbTintList;
    private Mode mThumbTintMode;
    private int mThumbWidth;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private Drawable mTrackDrawable;
    private Drawable mTrackHandDrawableDisabled;
    private Drawable mTrackRightHandDrawable;
    private ColorStateList mTrackTintList;
    private Mode mTrackTintMode;
    private VelocityTracker mVelocityTracker;
    private Handler mhandler;

    private class SwitchHandler extends Handler {
        /* synthetic */ SwitchHandler(Switch this$0, SwitchHandler -this1) {
            this();
        }

        private SwitchHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    float normalizedTime = ((float) (SystemClock.elapsedRealtime() - Switch.this.mStartTime)) / 330.0f;
                    Switch.this.mOffset = Switch.this.mStart + ((int) (((float) (Switch.this.mEnd - Switch.this.mStart)) * Switch.this.mInterpolator.getInterpolator().getInterpolation(Math.max(Math.min(normalizedTime, 1.0f), 0.0f))));
                    if (normalizedTime > Switch.CLICK_OFFSET_SCALE) {
                        Switch.this.mOffset2 = Switch.this.mStart + ((int) (((float) (Switch.this.mEnd - Switch.this.mStart)) * Switch.this.mInterpolator.getInterpolator().getInterpolation(Math.max(Math.min(normalizedTime - Switch.CLICK_OFFSET_SCALE, 1.0f), 0.0f))));
                    }
                    boolean bMore = normalizedTime - Switch.CLICK_OFFSET_SCALE < 1.0f;
                    Switch.this.invalidate();
                    if (bMore && Switch.this.bInAnimate) {
                        Switch.this.mhandler.sendEmptyMessage(0);
                        return;
                    } else {
                        Switch.this.mhandler.sendEmptyMessageDelayed(3, 20);
                        return;
                    }
                case 1:
                    if (Switch.this.mStart != Switch.this.mEnd) {
                        if (Math.abs(Switch.this.mStart - Switch.this.mEnd) <= 2) {
                            Switch.this.mStart = Switch.this.mEnd;
                        } else {
                            Switch.this.mStart = Switch.this.mStart + ((Switch.this.mEnd - Switch.this.mStart) / 2);
                        }
                        Switch.this.mOffset = Switch.this.mStart;
                        Switch.this.invalidate();
                        Switch.this.mhandler.sendEmptyMessageDelayed(1, 20);
                        return;
                    }
                    Switch.this.endOfAnimation();
                    Switch.this.invalidate();
                    return;
                case 3:
                    Switch.this.endOfAnimation();
                    return;
                default:
                    return;
            }
        }
    }

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchStyle);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTrackTintList = null;
        this.mTrackTintMode = null;
        this.mHasTrackTint = false;
        this.mHasTrackTintMode = false;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mTempRect = new Rect();
        this.mChecked = true;
        this.bInAnimate = false;
        this.mPath = new Path();
        this.mBgRect = new Rect();
        this.isFirstDraw = true;
        this.mTextPaint = new TextPaint(1);
        Resources res = getResources();
        this.mTextPaint.density = res.getDisplayMetrics().density;
        this.mTextPaint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyleAttr, defStyleRes);
        this.mThumbDrawable = a.getDrawable(2);
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setCallback(this);
        }
        this.mTrackDrawable = a.getDrawable(4);
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.setCallback(this);
        }
        this.mTextOn = a.getText(0);
        this.mTextOff = a.getText(1);
        this.mShowText = a.getBoolean(11, true);
        this.mThumbTextPadding = a.getDimensionPixelSize(7, 0);
        this.mSwitchMinWidth = a.getDimensionPixelSize(5, 0);
        this.mSwitchPadding = a.getDimensionPixelSize(6, 0);
        this.mSplitTrack = a.getBoolean(8, false);
        ColorStateList thumbTintList = a.getColorStateList(9);
        if (thumbTintList != null) {
            this.mThumbTintList = thumbTintList;
            this.mHasThumbTint = true;
        }
        Mode thumbTintMode = Drawable.parseTintMode(a.getInt(10, -1), null);
        if (this.mThumbTintMode != thumbTintMode) {
            this.mThumbTintMode = thumbTintMode;
            this.mHasThumbTintMode = true;
        }
        if (this.mHasThumbTint || this.mHasThumbTintMode) {
            applyThumbTint();
        }
        ColorStateList trackTintList = a.getColorStateList(12);
        if (trackTintList != null) {
            this.mTrackTintList = trackTintList;
            this.mHasTrackTint = true;
        }
        Mode trackTintMode = Drawable.parseTintMode(a.getInt(13, -1), null);
        if (this.mTrackTintMode != trackTintMode) {
            this.mTrackTintMode = trackTintMode;
            this.mHasTrackTintMode = true;
        }
        if (this.mHasTrackTint || this.mHasTrackTintMode) {
            applyTrackTint();
        }
        int appearance = a.getResourceId(3, 0);
        if (appearance != 0) {
            setSwitchTextAppearance(context, appearance);
        }
        a.recycle();
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            this.mOnBgDrawable = getContext().getResources().getDrawable(com.vivo.internal.R.drawable.vigour_bool_btn_bg_on_light, getContext().getTheme());
            this.mOffBgDrawable = getContext().getResources().getDrawable(com.vivo.internal.R.drawable.vigour_bool_btn_bg_off_light, getContext().getTheme());
            TypedArray ta = getContext().obtainStyledAttributes(attrs, com.vivo.internal.R.styleable.MoveBoolButton, com.vivo.internal.R.attr.moveBoolButtonStyle, defStyleRes);
            this.mThumbHandDrawableDisabled = ta.getDrawable(3);
            this.mTrackHandDrawableDisabled = ta.getDrawable(3);
            this.mTrackRightHandDrawable = ta.getDrawable(5);
            this.mPaddingTop = ta.getDimensionPixelSize(9, 10);
            this.mPaddingBottom = ta.getDimensionPixelSize(10, 10);
            this.mMaxHandWidth = ta.getDimensionPixelSize(11, 0);
            this.mPathInterpolator = (PathInterpolator) AnimationUtils.loadInterpolator(getContext(), ta.getResourceId(12, 0));
            ta.recycle();
            init(getContext());
        }
        ViewConfiguration config = ViewConfiguration.get(context);
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            this.mTouchSlop = (int) (((float) config.getScaledTouchSlop()) * 1.5f);
        } else {
            this.mTouchSlop = config.getScaledTouchSlop();
        }
        this.mMinFlingVelocity = config.getScaledMinimumFlingVelocity();
        refreshDrawableState();
        setChecked(isChecked());
    }

    public void setSwitchTextAppearance(Context context, int resid) {
        TypedArray appearance = context.obtainStyledAttributes(resid, R.styleable.TextAppearance);
        ColorStateList colors = appearance.getColorStateList(3);
        if (colors != null) {
            this.mTextColors = colors;
        } else {
            this.mTextColors = getTextColors();
        }
        int ts = appearance.getDimensionPixelSize(0, 0);
        if (!(ts == 0 || ((float) ts) == this.mTextPaint.getTextSize())) {
            this.mTextPaint.setTextSize((float) ts);
            requestLayout();
        }
        setSwitchTypefaceByIndex(appearance.getInt(1, -1), appearance.getInt(2, -1));
        if (appearance.getBoolean(11, false)) {
            this.mSwitchTransformationMethod = new AllCapsTransformationMethod(getContext());
            this.mSwitchTransformationMethod.setLengthChangesAllowed(true);
        } else {
            this.mSwitchTransformationMethod = null;
        }
        appearance.recycle();
    }

    private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        switch (typefaceIndex) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        setSwitchTypeface(tf, styleIndex);
    }

    public void setSwitchTypeface(Typeface tf, int style) {
        boolean z = false;
        if (style > 0) {
            float f;
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            setSwitchTypeface(tf);
            int need = style & (~(tf != null ? tf.getStyle() : 0));
            TextPaint textPaint = this.mTextPaint;
            if ((need & 1) != 0) {
                z = true;
            }
            textPaint.setFakeBoldText(z);
            textPaint = this.mTextPaint;
            if ((need & 2) != 0) {
                f = -0.25f;
            } else {
                f = 0.0f;
            }
            textPaint.setTextSkewX(f);
            return;
        }
        this.mTextPaint.setFakeBoldText(false);
        this.mTextPaint.setTextSkewX(0.0f);
        setSwitchTypeface(tf);
    }

    public void setSwitchTypeface(Typeface tf) {
        if (this.mTextPaint.getTypeface() != tf) {
            this.mTextPaint.setTypeface(tf);
            requestLayout();
            invalidate();
        }
    }

    public void setSwitchPadding(int pixels) {
        this.mSwitchPadding = pixels;
        requestLayout();
    }

    public int getSwitchPadding() {
        return this.mSwitchPadding;
    }

    public void setSwitchMinWidth(int pixels) {
        this.mSwitchMinWidth = pixels;
        requestLayout();
    }

    public int getSwitchMinWidth() {
        return this.mSwitchMinWidth;
    }

    public void setThumbTextPadding(int pixels) {
        this.mThumbTextPadding = pixels;
        requestLayout();
    }

    public int getThumbTextPadding() {
        return this.mThumbTextPadding;
    }

    public void setTrackDrawable(Drawable track) {
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.setCallback(null);
        }
        this.mTrackDrawable = track;
        if (track != null) {
            track.setCallback(this);
        }
        requestLayout();
    }

    public void setTrackResource(int resId) {
        setTrackDrawable(getContext().getDrawable(resId));
    }

    public Drawable getTrackDrawable() {
        return this.mTrackDrawable;
    }

    public void setTrackTintList(ColorStateList tint) {
        this.mTrackTintList = tint;
        this.mHasTrackTint = true;
        applyTrackTint();
    }

    public ColorStateList getTrackTintList() {
        return this.mTrackTintList;
    }

    public void setTrackTintMode(Mode tintMode) {
        this.mTrackTintMode = tintMode;
        this.mHasTrackTintMode = true;
        applyTrackTint();
    }

    public Mode getTrackTintMode() {
        return this.mTrackTintMode;
    }

    private void applyTrackTint() {
        if (this.mTrackDrawable == null) {
            return;
        }
        if (this.mHasTrackTint || this.mHasTrackTintMode) {
            this.mTrackDrawable = this.mTrackDrawable.mutate();
            if (this.mHasTrackTint) {
                this.mTrackDrawable.setTintList(this.mTrackTintList);
            }
            if (this.mHasTrackTintMode) {
                this.mTrackDrawable.setTintMode(this.mTrackTintMode);
            }
            if (this.mTrackDrawable.isStateful()) {
                this.mTrackDrawable.setState(getDrawableState());
            }
        }
    }

    public void setThumbDrawable(Drawable thumb) {
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setCallback(null);
        }
        this.mThumbDrawable = thumb;
        if (thumb != null) {
            thumb.setCallback(this);
        }
        requestLayout();
    }

    public void setThumbResource(int resId) {
        setThumbDrawable(getContext().getDrawable(resId));
    }

    public Drawable getThumbDrawable() {
        return this.mThumbDrawable;
    }

    public void setThumbTintList(ColorStateList tint) {
        this.mThumbTintList = tint;
        this.mHasThumbTint = true;
        applyThumbTint();
    }

    public ColorStateList getThumbTintList() {
        return this.mThumbTintList;
    }

    public void setThumbTintMode(Mode tintMode) {
        this.mThumbTintMode = tintMode;
        this.mHasThumbTintMode = true;
        applyThumbTint();
    }

    public Mode getThumbTintMode() {
        return this.mThumbTintMode;
    }

    private void applyThumbTint() {
        if (this.mThumbDrawable == null) {
            return;
        }
        if (this.mHasThumbTint || this.mHasThumbTintMode) {
            this.mThumbDrawable = this.mThumbDrawable.mutate();
            if (this.mHasThumbTint) {
                this.mThumbDrawable.setTintList(this.mThumbTintList);
            }
            if (this.mHasThumbTintMode) {
                this.mThumbDrawable.setTintMode(this.mThumbTintMode);
            }
            if (this.mThumbDrawable.isStateful()) {
                this.mThumbDrawable.setState(getDrawableState());
            }
        }
    }

    public void setSplitTrack(boolean splitTrack) {
        this.mSplitTrack = splitTrack;
        invalidate();
    }

    public boolean getSplitTrack() {
        return this.mSplitTrack;
    }

    public CharSequence getTextOn() {
        return this.mTextOn;
    }

    public void setTextOn(CharSequence textOn) {
        this.mTextOn = textOn;
        requestLayout();
    }

    public CharSequence getTextOff() {
        return this.mTextOff;
    }

    public void setTextOff(CharSequence textOff) {
        this.mTextOff = textOff;
        requestLayout();
    }

    public void setShowText(boolean showText) {
        if (this.mShowText != showText) {
            this.mShowText = showText;
            requestLayout();
        }
    }

    public boolean getShowText() {
        return this.mShowText;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            int width = this.mOnBgDrawable.getIntrinsicWidth();
            int height = this.mOnBgDrawable.getIntrinsicHeight();
            setMeasuredDimension(width, height);
            this.mBgRect.set(0, 0, width, height);
            return;
        }
        int thumbWidth;
        int thumbHeight;
        int maxTextWidth;
        int trackHeight;
        if (this.mShowText) {
            if (this.mOnLayout == null) {
                this.mOnLayout = makeLayout(this.mTextOn);
            }
            if (this.mOffLayout == null) {
                this.mOffLayout = makeLayout(this.mTextOff);
            }
        }
        Rect padding = this.mTempRect;
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.getPadding(padding);
            thumbWidth = (this.mThumbDrawable.getIntrinsicWidth() - padding.left) - padding.right;
            thumbHeight = this.mThumbDrawable.getIntrinsicHeight();
        } else {
            thumbWidth = 0;
            thumbHeight = 0;
        }
        if (this.mShowText) {
            maxTextWidth = Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth()) + (this.mThumbTextPadding * 2);
        } else {
            maxTextWidth = 0;
        }
        this.mThumbWidth = Math.max(maxTextWidth, thumbWidth);
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.getPadding(padding);
            trackHeight = this.mTrackDrawable.getIntrinsicHeight();
        } else {
            padding.setEmpty();
            trackHeight = 0;
        }
        int paddingLeft = padding.left;
        int paddingRight = padding.right;
        if (this.mThumbDrawable != null) {
            Insets inset = this.mThumbDrawable.getOpticalInsets();
            paddingLeft = Math.max(paddingLeft, inset.left);
            paddingRight = Math.max(paddingRight, inset.right);
        }
        int switchWidth = Math.max(this.mSwitchMinWidth, ((this.mThumbWidth * 2) + paddingLeft) + paddingRight);
        int switchHeight = Math.max(trackHeight, thumbHeight);
        this.mSwitchWidth = switchWidth;
        this.mSwitchHeight = switchHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredHeight() < switchHeight) {
            setMeasuredDimension(getMeasuredWidthAndState(), switchHeight);
        }
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        CharSequence text = isChecked() ? this.mTextOn : this.mTextOff;
        if (text != null) {
            event.getText().add(text);
        }
    }

    private Layout makeLayout(CharSequence text) {
        CharSequence transformed;
        if (this.mSwitchTransformationMethod != null) {
            transformed = this.mSwitchTransformationMethod.getTransformation(text, this);
        } else {
            transformed = text;
        }
        return new StaticLayout(transformed, this.mTextPaint, (int) Math.ceil((double) Layout.getDesiredWidth(transformed, 0, transformed.length(), this.mTextPaint, getTextDirectionHeuristic())), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
    }

    private boolean hitThumb(float x, float y) {
        boolean z = true;
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            return true;
        }
        if (this.mThumbDrawable == null) {
            return false;
        }
        int thumbOffset = getThumbOffset();
        this.mThumbDrawable.getPadding(this.mTempRect);
        int thumbTop = this.mSwitchTop - this.mTouchSlop;
        int thumbLeft = (this.mSwitchLeft + thumbOffset) - this.mTouchSlop;
        int thumbRight = (((this.mThumbWidth + thumbLeft) + this.mTempRect.left) + this.mTempRect.right) + this.mTouchSlop;
        int thumbBottom = this.mSwitchBottom + this.mTouchSlop;
        if (x <= ((float) thumbLeft) || x >= ((float) thumbRight) || y <= ((float) thumbTop) || y >= ((float) thumbBottom)) {
            z = false;
        }
        return z;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent ev) {
        if (VivoThemeUtil.isVigourTheme(getContext()) && (!isEnabled() || this.bInAnimate)) {
            return false;
        }
        this.mVelocityTracker.addMovement(ev);
        float x;
        float y;
        switch (ev.getActionMasked()) {
            case 0:
                x = ev.getX();
                y = ev.getY();
                if (isEnabled() && hitThumb(x, y)) {
                    this.mTouchMode = 1;
                    this.mTouchX = x;
                    this.mTouchY = y;
                    break;
                }
            case 1:
                if (VivoThemeUtil.isVigourTheme(getContext())) {
                    if (this.mTouchMode != 2) {
                        this.mChecked ^= 1;
                        clickAnimateToCheckedState(this.mChecked);
                        this.mTouchMode = 0;
                        break;
                    }
                    stopDrag(ev);
                    return true;
                }
            case 2:
                switch (this.mTouchMode) {
                    case 1:
                        x = ev.getX();
                        y = ev.getY();
                        if (Math.abs(x - this.mTouchX) > ((float) this.mTouchSlop) || Math.abs(y - this.mTouchY) > ((float) this.mTouchSlop)) {
                            this.mTouchMode = 2;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            this.mTouchX = x;
                            this.mTouchY = y;
                            return true;
                        }
                    case 2:
                        x = ev.getX();
                        if (VivoThemeUtil.isVigourTheme(getContext())) {
                            this.mOffset = Math.max(0, Math.min(this.mOffset + ((int) (this.mTouchX - x)), this.mScrollRange));
                            this.mTouchX = x;
                            invalidate();
                            return true;
                        }
                        float dPos;
                        int thumbScrollRange = getThumbScrollRange();
                        float thumbScrollOffset = x - this.mTouchX;
                        if (thumbScrollRange != 0) {
                            dPos = thumbScrollOffset / ((float) thumbScrollRange);
                        } else {
                            dPos = (float) (thumbScrollOffset > 0.0f ? 1 : -1);
                        }
                        if (isLayoutRtl()) {
                            dPos = -dPos;
                        }
                        float newPos = MathUtils.constrain(this.mThumbPosition + dPos, 0.0f, 1.0f);
                        if (newPos != this.mThumbPosition) {
                            this.mTouchX = x;
                            setThumbPosition(newPos);
                        }
                        return true;
                }
                break;
            case 3:
                if (this.mTouchMode != 2) {
                    this.mTouchMode = 0;
                    this.mVelocityTracker.clear();
                    break;
                }
                stopDrag(ev);
                super.onTouchEvent(ev);
                return true;
        }
        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(3);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    private void stopDrag(MotionEvent ev) {
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            vigourStopDrag(ev);
            return;
        }
        boolean newState;
        this.mTouchMode = 0;
        boolean commitChange = ev.getAction() == 1 ? isEnabled() : false;
        boolean oldState = isChecked();
        if (commitChange) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xvel = this.mVelocityTracker.getXVelocity();
            newState = Math.abs(xvel) > ((float) this.mMinFlingVelocity) ? isLayoutRtl() ? xvel >= 0.0f : xvel <= 0.0f : getTargetCheckedState();
        } else {
            newState = oldState;
        }
        if (newState != oldState) {
            playSoundEffect(0);
        }
        setChecked(newState);
        cancelSuperTouch(ev);
    }

    private void animateThumbToCheckedState(boolean newCheckedState) {
        int i;
        if (newCheckedState) {
            i = 1;
        } else {
            i = 0;
        }
        float targetPosition = (float) i;
        this.mPositionAnimator = ObjectAnimator.ofFloat(this, THUMB_POS, new float[]{targetPosition});
        this.mPositionAnimator.setDuration(250);
        this.mPositionAnimator.setAutoCancel(true);
        this.mPositionAnimator.start();
    }

    private void cancelPositionAnimator() {
        if (this.mPositionAnimator != null) {
            this.mPositionAnimator.cancel();
        }
    }

    private boolean getTargetCheckedState() {
        return this.mThumbPosition > 0.5f;
    }

    private void setThumbPosition(float position) {
        this.mThumbPosition = position;
        invalidate();
    }

    public void toggle() {
        if (!VivoThemeUtil.isVigourTheme(getContext())) {
            setChecked(isChecked() ^ 1);
        }
    }

    public void setChecked(boolean checked) {
        int i = 0;
        super.setChecked(checked);
        if (!VivoThemeUtil.isVigourTheme(getContext())) {
            checked = isChecked();
            if (isAttachedToWindow() && isLaidOut()) {
                animateThumbToCheckedState(checked);
            } else {
                cancelPositionAnimator();
                if (checked) {
                    i = 1;
                }
                setThumbPosition((float) i);
            }
        } else if (!this.bInAnimate) {
            if (this.mChecked != checked) {
                this.mChecked = checked;
            }
            if (this.mChecked) {
                this.mOffset2 = 0;
                this.mOffset = 0;
            } else {
                i = this.mScrollRange;
                this.mOffset2 = i;
                this.mOffset = i;
            }
            invalidate();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int switchLeft;
        int switchRight;
        int switchTop;
        int switchBottom;
        super.onLayout(changed, left, top, right, bottom);
        int opticalInsetLeft = 0;
        int opticalInsetRight = 0;
        if (this.mThumbDrawable != null) {
            Rect trackPadding = this.mTempRect;
            if (this.mTrackDrawable != null) {
                this.mTrackDrawable.getPadding(trackPadding);
            } else {
                trackPadding.setEmpty();
            }
            Insets insets = this.mThumbDrawable.getOpticalInsets();
            opticalInsetLeft = Math.max(0, insets.left - trackPadding.left);
            opticalInsetRight = Math.max(0, insets.right - trackPadding.right);
        }
        if (isLayoutRtl()) {
            switchLeft = getPaddingLeft() + opticalInsetLeft;
            switchRight = ((this.mSwitchWidth + switchLeft) - opticalInsetLeft) - opticalInsetRight;
        } else {
            switchRight = (getWidth() - getPaddingRight()) - opticalInsetRight;
            switchLeft = ((switchRight - this.mSwitchWidth) + opticalInsetLeft) + opticalInsetRight;
        }
        switch (getGravity() & 112) {
            case 16:
                switchTop = (((getPaddingTop() + getHeight()) - getPaddingBottom()) / 2) - (this.mSwitchHeight / 2);
                switchBottom = switchTop + this.mSwitchHeight;
                break;
            case 80:
                switchBottom = getHeight() - getPaddingBottom();
                switchTop = switchBottom - this.mSwitchHeight;
                break;
            default:
                switchTop = getPaddingTop();
                switchBottom = switchTop + this.mSwitchHeight;
                break;
        }
        this.mSwitchLeft = switchLeft;
        this.mSwitchTop = switchTop;
        this.mSwitchBottom = switchBottom;
        this.mSwitchRight = switchRight;
    }

    public void draw(Canvas c) {
        Insets thumbInsets;
        Rect padding = this.mTempRect;
        int switchLeft = this.mSwitchLeft;
        int switchTop = this.mSwitchTop;
        int switchRight = this.mSwitchRight;
        int switchBottom = this.mSwitchBottom;
        int thumbInitialLeft = switchLeft + getThumbOffset();
        if (this.mThumbDrawable != null) {
            thumbInsets = this.mThumbDrawable.getOpticalInsets();
        } else {
            thumbInsets = Insets.NONE;
        }
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.getPadding(padding);
            thumbInitialLeft += padding.left;
            int trackLeft = switchLeft;
            int trackTop = switchTop;
            int trackRight = switchRight;
            int trackBottom = switchBottom;
            if (thumbInsets != Insets.NONE) {
                if (thumbInsets.left > padding.left) {
                    trackLeft = switchLeft + (thumbInsets.left - padding.left);
                }
                if (thumbInsets.top > padding.top) {
                    trackTop = switchTop + (thumbInsets.top - padding.top);
                }
                if (thumbInsets.right > padding.right) {
                    trackRight = switchRight - (thumbInsets.right - padding.right);
                }
                if (thumbInsets.bottom > padding.bottom) {
                    trackBottom = switchBottom - (thumbInsets.bottom - padding.bottom);
                }
            }
            this.mTrackDrawable.setBounds(trackLeft, trackTop, trackRight, trackBottom);
        }
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.getPadding(padding);
            int thumbLeft = thumbInitialLeft - padding.left;
            int thumbRight = (this.mThumbWidth + thumbInitialLeft) + padding.right;
            this.mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);
            Drawable background = getBackground();
            if (background != null) {
                background.setHotspotBounds(thumbLeft, switchTop, thumbRight, switchBottom);
            }
        }
        super.draw(c);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            vigourOnDraw(canvas);
            return;
        }
        int saveCount;
        Rect padding = this.mTempRect;
        Drawable trackDrawable = this.mTrackDrawable;
        if (trackDrawable != null) {
            trackDrawable.getPadding(padding);
        } else {
            padding.setEmpty();
        }
        int switchInnerTop = this.mSwitchTop + padding.top;
        int switchInnerBottom = this.mSwitchBottom - padding.bottom;
        Drawable thumbDrawable = this.mThumbDrawable;
        if (trackDrawable != null) {
            if (!this.mSplitTrack || thumbDrawable == null) {
                trackDrawable.draw(canvas);
            } else {
                Insets insets = thumbDrawable.getOpticalInsets();
                thumbDrawable.copyBounds(padding);
                padding.left += insets.left;
                padding.right -= insets.right;
                saveCount = canvas.save();
                canvas.clipRect(padding, Op.DIFFERENCE);
                trackDrawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
        saveCount = canvas.save();
        if (thumbDrawable != null) {
            thumbDrawable.draw(canvas);
        }
        Layout switchText = getTargetCheckedState() ? this.mOnLayout : this.mOffLayout;
        if (switchText != null) {
            int cX;
            int[] drawableState = getDrawableState();
            if (this.mTextColors != null) {
                this.mTextPaint.setColor(this.mTextColors.getColorForState(drawableState, 0));
            }
            this.mTextPaint.drawableState = drawableState;
            if (thumbDrawable != null) {
                Rect bounds = thumbDrawable.getBounds();
                cX = bounds.left + bounds.right;
            } else {
                cX = getWidth();
            }
            canvas.translate((float) ((cX / 2) - (switchText.getWidth() / 2)), (float) (((switchInnerTop + switchInnerBottom) / 2) - (switchText.getHeight() / 2)));
            switchText.draw(canvas);
        }
        canvas.restoreToCount(saveCount);
    }

    public int getCompoundPaddingLeft() {
        if (!isLayoutRtl()) {
            return super.getCompoundPaddingLeft();
        }
        int padding = super.getCompoundPaddingLeft() + this.mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += this.mSwitchPadding;
        }
        return padding;
    }

    public int getCompoundPaddingRight() {
        if (isLayoutRtl()) {
            return super.getCompoundPaddingRight();
        }
        int padding = super.getCompoundPaddingRight() + this.mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += this.mSwitchPadding;
        }
        return padding;
    }

    private int getThumbOffset() {
        float thumbPosition;
        if (isLayoutRtl()) {
            thumbPosition = 1.0f - this.mThumbPosition;
        } else {
            thumbPosition = this.mThumbPosition;
        }
        return (int) ((((float) getThumbScrollRange()) * thumbPosition) + 0.5f);
    }

    private int getThumbScrollRange() {
        if (this.mTrackDrawable == null) {
            return 0;
        }
        Insets insets;
        Rect padding = this.mTempRect;
        this.mTrackDrawable.getPadding(padding);
        if (this.mThumbDrawable != null) {
            insets = this.mThumbDrawable.getOpticalInsets();
        } else {
            insets = Insets.NONE;
        }
        return ((((this.mSwitchWidth - this.mThumbWidth) - padding.left) - padding.right) - insets.left) - insets.right;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] state = getDrawableState();
        int changed = 0;
        Drawable thumbDrawable = this.mThumbDrawable;
        if (thumbDrawable != null && thumbDrawable.isStateful()) {
            changed = thumbDrawable.setState(state);
        }
        Drawable trackDrawable = this.mTrackDrawable;
        if (trackDrawable != null && trackDrawable.isStateful()) {
            changed |= trackDrawable.setState(state);
        }
        if (changed != 0) {
            invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setHotspot(x, y);
        }
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.setHotspot(x, y);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mThumbDrawable || who == this.mTrackDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.jumpToCurrentState();
        }
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.jumpToCurrentState();
        }
        if (this.mPositionAnimator != null && this.mPositionAnimator.isStarted()) {
            this.mPositionAnimator.end();
            this.mPositionAnimator = null;
        }
    }

    public CharSequence getAccessibilityClassName() {
        return Switch.class.getName();
    }

    public void onProvideStructure(ViewStructure structure) {
        super.onProvideStructure(structure);
        onProvideAutoFillStructureForAssistOrAutofill(structure);
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        super.onProvideAutofillStructure(structure, flags);
        onProvideAutoFillStructureForAssistOrAutofill(structure);
    }

    private void onProvideAutoFillStructureForAssistOrAutofill(ViewStructure structure) {
        CharSequence switchText = isChecked() ? this.mTextOn : this.mTextOff;
        if (!TextUtils.isEmpty(switchText)) {
            CharSequence oldText = structure.getText();
            if (TextUtils.isEmpty(oldText)) {
                structure.setText(switchText);
                return;
            }
            StringBuilder newText = new StringBuilder();
            newText.append(oldText).append(' ').append(switchText);
            structure.setText(newText);
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        CharSequence switchText = isChecked() ? this.mTextOn : this.mTextOff;
        if (!TextUtils.isEmpty(switchText)) {
            CharSequence oldText = info.getText();
            if (TextUtils.isEmpty(oldText)) {
                info.setText(switchText);
                return;
            }
            StringBuilder newText = new StringBuilder();
            newText.append(oldText).append(' ').append(switchText);
            info.setText(newText);
        }
    }

    private void init(Context context) {
        this.mhandler = new SwitchHandler(this, null);
        float density = getContext().getResources().getDisplayMetrics().density;
        this.mRomVersion = FtBuild.getRomVersion();
        if (this.mRomVersion >= 3.0f) {
            this.mMaxHandWidth = (int) Math.min((float) this.mMaxHandWidth, 10.0f * density);
        } else {
            this.mMaxHandWidth = 0;
        }
        this.mCircleRadius = this.mThumbDrawable.getIntrinsicWidth() / 2;
        this.mLeftHandPos = this.mCircleRadius + ((int) (1.0f * density));
        this.mHandPos = (this.mOnBgDrawable.getIntrinsicWidth() - this.mThumbDrawable.getIntrinsicWidth()) - ((int) (2.0f * density));
        this.mScrollRange = (this.mOnBgDrawable.getIntrinsicWidth() - this.mThumbDrawable.getIntrinsicWidth()) - ((int) (4.0f * density));
    }

    public void setText(CharSequence text, BufferType type) {
        if (VivoThemeUtil.isVigourTheme(getContext())) {
            text = "";
        }
        super.setText(text, type);
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
        this.bInAnimate = true;
        ensureInterpolator();
        this.mStart = this.mOffset;
        this.mEnd = targetPos;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mhandler.sendEmptyMessage(0);
    }

    private void ensureInterpolator() {
        if (this.mInterpolator == null) {
            configAnimator();
        }
    }

    private void configAnimator() {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.setInterpolator(this.mPathInterpolator);
        this.mInterpolator = anim.setDuration(330);
    }

    private void endOfAnimation() {
        this.bInAnimate = false;
        setChecked(this.mChecked);
        this.mOffset2 = this.mOffset;
        this.mTouchMode = 0;
    }

    private void vigourStopDrag(MotionEvent ev) {
        if (this.mOffset >= this.mScrollRange / 2) {
            animateToCheckedState(false);
        } else {
            animateToCheckedState(true);
        }
    }

    private void vigourOnDraw(Canvas canvas) {
        Rect rect;
        if (this.isFirstDraw && getLayoutDirection() == 1) {
            setRotation(180.0f);
            this.isFirstDraw = false;
        }
        int alpha = 255 - ((this.mOffset * 255) / this.mScrollRange);
        if (alpha != 255) {
            this.mOffBgDrawable.setBounds(this.mBgRect);
            this.mOffBgDrawable.draw(canvas);
        }
        this.mOnBgDrawable.setAlpha(alpha);
        this.mOnBgDrawable.setBounds(this.mBgRect);
        this.mOnBgDrawable.draw(canvas);
        Drawable handDrawable = this.mThumbDrawable;
        if (!isEnabled()) {
            handDrawable = this.mThumbHandDrawableDisabled;
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
        canvas.save();
        if (alpha != 0 && alpha != 255) {
            this.mPath.reset();
            this.mPath.addCircle((float) ((this.mPaddingLeft + this.mCircleRadius) + 5), (float) (getHeight() / 2), (float) this.mCircleRadius, Direction.CCW);
            this.mPath.addRect((float) (this.mCircleRadius + 5), 0.0f, (float) (getWidth() - this.mCircleRadius), (float) getHeight(), Direction.CCW);
            this.mPath.addCircle((float) (((getWidth() - this.mCircleRadius) - 5) - this.mPaddingRight), (float) (getHeight() / 2), (float) this.mCircleRadius, Direction.CCW);
            canvas.clipPath(this.mPath, Op.REPLACE);
        }
    }
}
