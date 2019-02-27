package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import com.vivo.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LayoutDrawable extends DrawableWrapper {
    private static final int BASEBOARD_BOUNDS_COLOR = -16777216;
    private static final int BOUNDS_WIDTH = 1;
    private static final boolean DBG = false;
    private static final String DRAW_RECT_BOUNDS_PROP = "debug.draw.bounds";
    private static final String LAYOUT_EFFECT_PROP = "debug.layout.effect";
    private static final String TAG = "LayoutDrawable";
    private static final int WRAPPER_BOUNDS_COLOR = -16711936;
    private boolean DEBUG;
    private int mBoundsWidth;
    private ContentWrapper mContent;
    private boolean mDrawBounds;
    private boolean mGlobalTheme;
    private int mHeight;
    private boolean mLayoutEffect;
    private Paint mPaint;
    private boolean mResolveBounds;
    private LayoutDrawableState mState;
    private int mWidth;

    private interface ContentWrapper {
        void draw(Canvas canvas);

        int getIntrinsicHeight();

        int getIntrinsicWidth();
    }

    private class LayoutContentWrapper implements ContentWrapper {
        /* synthetic */ LayoutContentWrapper(LayoutDrawable this$0, LayoutContentWrapper -this1) {
            this();
        }

        private LayoutContentWrapper() {
        }

        public void draw(Canvas canvas) {
            if (!LayoutDrawable.this.mGlobalTheme || (LayoutDrawable.this.mState.mThemeVisible ^ 1) == 0) {
                Drawable dr = LayoutDrawable.this.getDrawable();
                if (LayoutDrawable.this.getDrawable() != null) {
                    if (LayoutDrawable.this.mResolveBounds) {
                        LayoutDrawable.this.mResolveBounds = false;
                        updateWrapperDrawableBounds();
                    }
                    Rect bounds = LayoutDrawable.this.getBounds();
                    Rect padding = LayoutDrawable.this.mState.mPadding;
                    canvas.save();
                    if (needAutoMirrored()) {
                        canvas.translate((float) (bounds.right - bounds.left), 0.0f);
                        canvas.scale(-1.0f, 1.0f);
                    }
                    dr.draw(canvas);
                    if (LayoutDrawable.this.mDrawBounds) {
                        drawBaseboardRect(canvas);
                        drawWrapperRect(canvas);
                    }
                    canvas.restore();
                }
            }
        }

        private void drawBaseboardRect(Canvas canvas) {
            Drawable dr = LayoutDrawable.this.getDrawable();
            int baseboardWidth = LayoutDrawable.this.mState.mRequestWidth > 0 ? LayoutDrawable.this.mState.mRequestWidth : dr.getIntrinsicWidth();
            int baseboardHeight = LayoutDrawable.this.mState.mRequestHeight > 0 ? LayoutDrawable.this.mState.mRequestHeight : dr.getIntrinsicHeight();
            Rect dstRect = new Rect();
            Gravity.apply(LayoutDrawable.this.mState.mGravity, baseboardWidth, baseboardHeight, LayoutDrawable.this.getBounds(), dstRect, LayoutDrawable.this.getLayoutDirection());
            LayoutDrawable.this.mPaint.setColor(-16777216);
            drawRect(canvas, dstRect, LayoutDrawable.this.mBoundsWidth, LayoutDrawable.this.mPaint);
        }

        private void drawWrapperRect(Canvas canvas) {
            Rect dstRect = LayoutDrawable.this.getDrawable().getBounds();
            LayoutDrawable.this.mPaint.setColor(-16711936);
            drawRect(canvas, dstRect, LayoutDrawable.this.mBoundsWidth, LayoutDrawable.this.mPaint);
        }

        private void drawRect(Canvas canvas, Rect rect, int width, Paint paint) {
            canvas.drawRect((float) rect.left, (float) rect.top, (float) rect.right, (float) (rect.top + width), paint);
            canvas.drawRect((float) rect.left, (float) rect.top, (float) (rect.left + width), (float) rect.bottom, paint);
            canvas.drawRect((float) rect.left, (float) (rect.bottom - width), (float) rect.right, (float) rect.bottom, paint);
            canvas.drawRect((float) (rect.right - width), (float) rect.top, (float) rect.right, (float) rect.bottom, paint);
        }

        private void updateWrapperDrawableBounds() {
            Rect padding = LayoutDrawable.this.mState.mPadding;
            Drawable dr = LayoutDrawable.this.getDrawable();
            int width = dr.getIntrinsicWidth();
            int height = dr.getIntrinsicHeight();
            float autoVerticalOffset = 0.0f;
            float autoHorizontalOffset = 0.0f;
            if (LayoutDrawable.this.mState.mRequestWidth > width) {
                autoHorizontalOffset = (float) ((LayoutDrawable.this.mState.mRequestWidth - width) / 2);
            }
            if (LayoutDrawable.this.mState.mRequestHeight > height) {
                autoVerticalOffset = (float) ((LayoutDrawable.this.mState.mRequestHeight - height) / 2);
            }
            if (LayoutDrawable.this.DEBUG) {
                Log.d(LayoutDrawable.TAG, "autoHorizontalOffset=" + autoHorizontalOffset + " autoVerticalOffset=" + autoVerticalOffset + " intrinsicWidth=" + width + " intrinsicHeight=" + height);
            }
            Rect rect = new Rect(LayoutDrawable.this.getBounds());
            Rect dstRect = new Rect();
            rect.top = (int) (((float) rect.top) + autoVerticalOffset);
            rect.bottom = (int) (((float) rect.bottom) - autoVerticalOffset);
            rect.left = (int) (((float) rect.left) + autoHorizontalOffset);
            rect.right = (int) (((float) rect.right) - autoHorizontalOffset);
            rect.top += padding.top;
            rect.bottom -= padding.bottom;
            if (LayoutDrawable.this.getLayoutDirection() != 1 || (needAutoMirrored() ^ 1) == 0) {
                rect.left += padding.left;
                rect.right -= padding.right;
            } else {
                rect.left += padding.right;
                rect.right -= padding.left;
            }
            if (LayoutDrawable.this.DEBUG) {
                Log.d(LayoutDrawable.TAG, "layout bounds = " + rect);
            }
            Gravity.apply(LayoutDrawable.this.mState.mGravity, dr.getIntrinsicWidth(), dr.getIntrinsicHeight(), rect, dstRect, LayoutDrawable.this.getLayoutDirection());
            dr.setBounds(dstRect);
        }

        private boolean needAutoMirrored() {
            return LayoutDrawable.this.mState.mAutoMirrored && LayoutDrawable.this.getLayoutDirection() == 1;
        }

        public int getIntrinsicWidth() {
            Drawable dr = LayoutDrawable.this.getDrawable();
            if (dr != null) {
                return Math.max(dr.getIntrinsicWidth(), LayoutDrawable.this.mState.mMinWidth);
            }
            return LayoutDrawable.this.mState.mMinWidth;
        }

        public int getIntrinsicHeight() {
            Drawable dr = LayoutDrawable.this.getDrawable();
            if (dr != null) {
                return Math.max(dr.getIntrinsicWidth(), LayoutDrawable.this.mState.mMinHeight);
            }
            return LayoutDrawable.this.mState.mMinHeight;
        }
    }

    static final class LayoutDrawableState extends DrawableWrapperState {
        private boolean mAutoMirrored = false;
        private int mGravity = 17;
        private int mMinHeight;
        private int mMinWidth;
        private Rect mPadding = new Rect();
        private int mRequestHeight;
        private int mRequestWidth;
        private int[] mThemeAttrs;
        private boolean mThemeEffect = false;
        private boolean mThemeVisible = true;

        LayoutDrawableState(LayoutDrawableState state, Resources res) {
            super(state, res);
            if (state != null) {
                this.mMinWidth = state.mMinWidth;
                this.mMinHeight = state.mMinHeight;
                this.mGravity = state.mGravity;
                this.mAutoMirrored = state.mAutoMirrored;
            }
        }

        public Drawable newDrawable(Resources res) {
            return new LayoutDrawable(this, res, null);
        }
    }

    public LayoutDrawable() {
        this(new LayoutDrawableState(null, null), null);
    }

    public LayoutDrawable(Drawable dr, int minWidth, int minHeight) {
        this(dr, minWidth, minHeight, minWidth, minHeight);
    }

    public LayoutDrawable(Drawable dr, int minWidth, int minHeight, int requestWidth, int requestHeight) {
        this(new LayoutDrawableState(null, null), null);
        this.mState.mMinWidth = minWidth;
        this.mState.mMinHeight = minHeight;
        this.mState.mRequestWidth = requestWidth;
        this.mState.mRequestHeight = requestHeight;
        setDrawable(dr);
    }

    private LayoutDrawable(LayoutDrawableState state, Resources res) {
        boolean z = false;
        super(state, res);
        this.DEBUG = Build.TYPE.equals("eng");
        this.mDrawBounds = false;
        this.mLayoutEffect = true;
        this.mPaint = null;
        this.mBoundsWidth = 0;
        this.mState = null;
        this.mContent = null;
        this.mResolveBounds = false;
        this.mGlobalTheme = false;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mState = state;
        this.mLayoutEffect = SystemProperties.getBoolean(LAYOUT_EFFECT_PROP, true);
        this.mDrawBounds = SystemProperties.getBoolean(DRAW_RECT_BOUNDS_PROP, false);
        this.DEBUG |= this.mDrawBounds;
        if (Resources.getSystem().getDimension(51118202) == 0.0f) {
            z = true;
        }
        this.mGlobalTheme = z;
        setThemeVisible(state.mThemeVisible);
        setThemeEffect(state.mThemeEffect);
        updateLayoutEffect();
        this.mBoundsWidth = (int) (Resources.getSystem().getDisplayMetrics().density * 1.0f);
        this.mPaint = new Paint(1);
        this.mPaint.setStyle(Style.FILL);
    }

    public void setDrawable(Drawable dr) {
        super.setDrawable(dr);
        this.mResolveBounds = true;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.LayoutDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        inflateChildElements(r, parser, attrs, theme);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        LayoutDrawableState state = this.mState;
        if (!(state == null || state.mThemeAttrs == null)) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.LayoutDrawable);
            try {
                updateStateFromTypedArray(a);
                verifyRequiredAttributes(a);
            } catch (XmlPullParserException e) {
                Drawable.rethrowAsRuntimeException(e);
            } finally {
                a.recycle();
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        if (this.mState != null) {
            this.mState.mThemeAttrs = a.extractThemeAttrs();
            this.mState.mMinWidth = a.getDimensionPixelOffset(3, this.mState.mMinWidth);
            this.mState.mMinHeight = a.getDimensionPixelOffset(4, this.mState.mMinHeight);
            this.mState.mGravity = a.getInt(0, this.mState.mGravity);
            this.mState.mAutoMirrored = a.getBoolean(8, false);
            this.mState.mRequestWidth = a.getDimensionPixelOffset(11, this.mState.mMinWidth);
            this.mState.mRequestHeight = a.getDimensionPixelOffset(12, this.mState.mMinHeight);
            int paddingStart = a.getDimensionPixelOffset(6, 0);
            int paddingEnd = a.getDimensionPixelOffset(7, 0);
            this.mState.mPadding.set(paddingStart, a.getDimensionPixelOffset(1, 0), paddingEnd, a.getDimensionPixelOffset(2, 0));
            boolean themeVisible = a.getBoolean(9, true);
            boolean themeEffect = a.getBoolean(10, true);
            if (this.DEBUG) {
                Log.d(TAG, "minWidth=" + this.mState.mMinWidth + " minHeight=" + this.mState.mMinHeight + " Gravity=" + this.mState.mGravity + " autoMirrored=" + this.mState.mAutoMirrored + " requestWidth=" + this.mState.mRequestWidth + " requestHeight=" + this.mState.mRequestHeight + " padding=" + this.mState.mPadding + " themeVisible=" + themeVisible + " themeEffect=" + themeEffect);
            }
            setThemeVisible(themeVisible);
            setThemeEffect(themeEffect);
            updateLayoutEffect();
        }
    }

    public void setThemeVisible(boolean visible) {
        if (this.mState.mThemeVisible != visible) {
            invalidateSelf();
        }
        this.mState.mThemeVisible = visible;
    }

    public boolean isThemeVisible() {
        return this.mState.mThemeVisible;
    }

    public void setThemeEffect(boolean themeEffect) {
        boolean effect = themeEffect ? this.mLayoutEffect : false;
        if (!this.mGlobalTheme || (effect ^ 1) == 0) {
            this.mContent = new LayoutContentWrapper(this, null);
            this.mResolveBounds = true;
        } else {
            this.mContent = null;
            if (getDrawable() != null) {
                getDrawable().setBounds(getBounds());
            }
        }
        if (this.mState.mThemeEffect != effect) {
            invalidateSelf();
        }
        this.mState.mThemeEffect = effect;
    }

    public boolean isThemeEffect() {
        return this.mState.mThemeEffect;
    }

    private void updateLayoutEffect() {
        if (!this.mLayoutEffect) {
            this.mContent = null;
            if (getDrawable() != null) {
                getDrawable().setBounds(getBounds());
            }
            invalidateSelf();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x001f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attr, Theme theme) throws XmlPullParserException, IOException {
        Drawable dr = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || type == 3 || parser.getDepth() <= outerDepth) {
                if (dr == null) {
                    setDrawable(dr);
                    return;
                }
                return;
            } else if (type == 2) {
                dr = Drawable.createFromXmlInner(r, parser, attr, theme);
            }
        }
        if (dr == null) {
        }
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() == null) {
            throw new XmlPullParserException("LayoutDrawable requires a 'drawable' attribute  or child tag defining a drawable");
        }
    }

    public void draw(Canvas canvas) {
        if (this.mContent != null) {
            this.mContent.draw(canvas);
        } else {
            super.draw(canvas);
        }
    }

    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.mWidth = rect.right - rect.left;
        this.mHeight = rect.bottom - rect.top;
        this.mResolveBounds = true;
    }

    protected boolean onLevelChange(int level) {
        this.mResolveBounds = true;
        return super.onLevelChange(level);
    }

    public void setGravity(int gravity) {
        if (this.mState.mGravity != gravity) {
            this.mState.mGravity = gravity;
            invalidateSelf();
        }
    }

    public int getIntrinsicWidth() {
        if (this.mContent != null) {
            return this.mContent.getIntrinsicWidth();
        }
        return super.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        if (this.mContent != null) {
            return this.mContent.getIntrinsicHeight();
        }
        return super.getIntrinsicHeight();
    }

    public void setMinimumHeight(int minHeight) {
        if (this.mState.mMinHeight != minHeight) {
            this.mState.mMinHeight = minHeight;
            invalidateSelf();
        }
    }

    public void setMinimumWidth(int minWidth) {
        if (this.mState.mMinWidth != minWidth) {
            this.mState.mMinWidth = minWidth;
            invalidateSelf();
        }
    }

    public void setRequestWidth(int width) {
        if (this.mState.mRequestWidth != width) {
            this.mState.mRequestWidth = width;
            invalidateSelf();
        }
    }

    public void setRequestHeight(int height) {
        if (this.mState.mRequestHeight != height) {
            this.mState.mRequestHeight = height;
            invalidateSelf();
        }
    }

    public int getMinimumWidth() {
        if (this.mContent != null) {
            return Math.max(super.getMinimumWidth(), this.mState.mMinWidth);
        }
        return super.getMinimumWidth();
    }

    public int getMinimumHeight() {
        if (this.mContent != null) {
            return Math.max(super.getMinimumHeight(), this.mState.mMinHeight);
        }
        return super.getMinimumHeight();
    }

    LayoutDrawableState mutateConstantState() {
        return new LayoutDrawableState(this.mState, null);
    }

    public Drawable getCurrent() {
        Drawable dr = getDrawable();
        if (dr != null) {
            return dr.getCurrent();
        }
        return super.getCurrent();
    }

    public boolean isAutoMirrored() {
        if (this.mContent != null) {
            return this.mState.mAutoMirrored;
        }
        return super.isAutoMirrored();
    }

    public void jumpToCurrentState() {
        Drawable dr = getDrawable();
        if (dr != null) {
            dr.jumpToCurrentState();
            invalidateSelf();
        }
    }

    public void setAutoMirrored(boolean mirrored) {
        super.setAutoMirrored(mirrored);
        this.mState.mAutoMirrored = true;
        invalidateSelf();
    }

    public void setPadding(int start, int top, int end, int bottom) {
        Rect padding = this.mState.mPadding;
        if (padding.left != start || padding.top != top || padding.right != end || padding.bottom != bottom) {
            padding.left = start;
            padding.top = top;
            padding.right = end;
            padding.bottom = bottom;
            invalidateSelf();
        }
    }
}
