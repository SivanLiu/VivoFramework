package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class AbcThumbsSelect extends LinearLayout {
    private final int ABC_HEIGHT;
    private final int ABC_HEIGHT_FHD;
    private final int ABC_HEIGHT_H;
    private final int ABC_HEIGHT_HD;
    private final int ABC_HEIGHT_QFHD;
    private final int ABC_HEIGHT_QHD;
    private final String[] ABC_STRING;
    private final int ABC_WIDTH;
    private final int ABC_WIDTH_FHD;
    private final int ABC_WIDTH_HD;
    private final int ABC_WIDTH_QFHD;
    private final int ABC_WIDTH_QHD;
    private final String TAG;
    private boolean addFlag;
    private boolean hasSearchSelect;
    private boolean hasStarredSelect;
    int[] location;
    private Drawable mAbcThumbBg;
    private int mDensityDpi;
    private float mDensityScale;
    private TextView mFloatText;
    private PopupWindow mPopupWin;
    private Drawable mPopupWinBg;
    private int mPopupWinHeight;
    private boolean mPopupWinMoveFlag;
    private int mPopupWinStartY;
    private int mPopupWinWidth;
    private int mPopupWinX;
    private int mPopupWinY;
    private int mSelectTextColor;
    private TextView mSelectTextView;
    private int mStartIndex;
    private int mTextColor;
    private float mTextSize;
    private int mThumbsTextHeight;
    private int mThumbsTextWidth;
    private OnClickListener mTouchListener;
    private boolean showPopupWin;

    private class MyTouchListener implements OnTouchListener {
        /* synthetic */ MyTouchListener(AbcThumbsSelect this$0, MyTouchListener -this1) {
            this();
        }

        private MyTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent e) {
            int i = 0;
            switch (e.getAction()) {
                case 0:
                    AbcThumbsSelect.this.setBackgroundDrawable(AbcThumbsSelect.this.mAbcThumbBg);
                    AbcThumbsSelect.this.updateAbcBackground(true);
                    break;
                case 1:
                    AbcThumbsSelect.this.setSelectTextViewTextColor(AbcThumbsSelect.this.mTextColor);
                    if (AbcThumbsSelect.this.showPopupWin && AbcThumbsSelect.this.mPopupWin.isShowing()) {
                        AbcThumbsSelect.this.mPopupWin.dismiss();
                    }
                    AbcThumbsSelect.this.setBackgroundDrawable(null);
                    AbcThumbsSelect.this.updateAbcBackground(false);
                    break;
                case 2:
                    break;
                case 3:
                    AbcThumbsSelect.this.setSelectTextViewTextColor(AbcThumbsSelect.this.mTextColor);
                    if (AbcThumbsSelect.this.showPopupWin && AbcThumbsSelect.this.mPopupWin.isShowing()) {
                        AbcThumbsSelect.this.mPopupWin.dismiss();
                    }
                    AbcThumbsSelect.this.setBackgroundDrawable(null);
                    AbcThumbsSelect.this.updateAbcBackground(false);
                    break;
            }
            AbcThumbsSelect.this.mPopupWinStartY = ((int) e.getRawY()) - ((int) e.getY());
            String str = AbcThumbsSelect.this.findAbcTabChild(e.getY());
            if (AbcThumbsSelect.this.showPopupWin) {
                if (!AbcThumbsSelect.this.mPopupWin.isShowing()) {
                    PopupWindow -get3 = AbcThumbsSelect.this.mPopupWin;
                    View view = AbcThumbsSelect.this;
                    if (!AbcThumbsSelect.this.isLayoutRtl()) {
                        i = 48;
                    }
                    -get3.showAtLocation(view, i, AbcThumbsSelect.this.mPopupWinX, AbcThumbsSelect.this.mPopupWinY);
                } else if (str.equals(AbcThumbsSelect.this.mFloatText.getText().toString())) {
                    return true;
                } else {
                    AbcThumbsSelect.this.mPopupWin.update(AbcThumbsSelect.this.mPopupWinX, AbcThumbsSelect.this.mPopupWinY, (int) (((float) AbcThumbsSelect.this.mPopupWinWidth) * AbcThumbsSelect.this.mDensityScale), (int) (((float) AbcThumbsSelect.this.mPopupWinHeight) * AbcThumbsSelect.this.mDensityScale));
                }
            }
            AbcThumbsSelect.this.mFloatText.setText(str);
            if (AbcThumbsSelect.this.mFloatText.getText().toString().equals("~")) {
                AbcThumbsSelect.this.mFloatText.setBackgroundResource(50462789);
                AbcThumbsSelect.this.mFloatText.setText(Events.DEFAULT_SORT_ORDER);
            } else {
                AbcThumbsSelect.this.mFloatText.setBackgroundDrawable(null);
            }
            if (!(AbcThumbsSelect.this.mSelectTextView == null || AbcThumbsSelect.this.mTouchListener == null)) {
                AbcThumbsSelect.this.mTouchListener.onClick(AbcThumbsSelect.this.mSelectTextView);
            }
            return true;
        }
    }

    public AbcThumbsSelect(Context context) {
        this(context, null);
    }

    public AbcThumbsSelect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbcThumbsSelect(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.ABC_STRING = new String[]{"~", "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        this.ABC_WIDTH = 27;
        this.ABC_HEIGHT = 12;
        this.ABC_HEIGHT_H = 21;
        this.ABC_WIDTH_QHD = 34;
        this.ABC_HEIGHT_QHD = 26;
        this.ABC_WIDTH_HD = 56;
        this.ABC_HEIGHT_HD = 35;
        this.ABC_WIDTH_FHD = 56;
        this.ABC_HEIGHT_FHD = 50;
        this.ABC_WIDTH_QFHD = 56;
        this.ABC_HEIGHT_QFHD = 66;
        this.TAG = "AbcThumbsSelect";
        this.mPopupWinWidth = 100;
        this.mPopupWinHeight = 100;
        this.mPopupWinX = 120;
        this.mPopupWinY = 100;
        this.mStartIndex = 1;
        this.mPopupWinStartY = -1;
        this.location = new int[2];
        this.mTextSize = 18.0f;
        this.mTextColor = -9868951;
        this.mSelectTextColor = -9868951;
        this.mPopupWinMoveFlag = false;
        this.addFlag = false;
        this.showPopupWin = true;
        this.hasSearchSelect = false;
        this.hasStarredSelect = false;
        setOrientation(1);
        setMinimumWidth(context.getResources().getDimensionPixelSize(51118094));
        setOnTouchListener(new MyTouchListener(this, null));
        setGravity(17);
        TypedArray abcThumbType = this.mContext.obtainStyledAttributes(null, R.styleable.AbcThumbSelect, 50397216, 51314982);
        this.mAbcThumbBg = abcThumbType.getDrawable(0);
        this.mPopupWinBg = abcThumbType.getDrawable(1);
        this.mFloatText = new TextView(getContext());
        this.mFloatText.setTextSize(50.0f);
        this.mFloatText.setHeight((int) (this.mDensityScale * 50.0f));
        this.mFloatText.setWidth((int) (this.mDensityScale * 50.0f));
        this.mFloatText.setGravity(17);
        this.mFloatText.setTextColor(-15198183);
        this.mFloatText.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
        this.mDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
        if (this.mDensityDpi == 160) {
            this.mPopupWinX = 80;
        } else if (this.mDensityDpi == 240) {
            this.mPopupWinWidth = 83;
            this.mPopupWinHeight = 83;
            this.mDensityScale = 1.0f;
            this.mPopupWinX = 100;
            this.mFloatText.setTextSize(32.0f);
        } else if (this.mDensityDpi == 320) {
            this.mPopupWinWidth = 122;
            this.mPopupWinHeight = 122;
            this.mDensityScale = 1.0f;
            this.mTextColor = -9868951;
            this.mSelectTextColor = -9868951;
            this.mPopupWinX = AblConfig.BRIGHTNESS_MAP_HIGH;
            this.mFloatText.setTextSize(35.0f);
        } else if (this.mDensityDpi == 480) {
            this.mTextColor = -9868951;
            this.mSelectTextColor = -9868951;
            this.mPopupWinX = CalendarsColumns.RESPOND_ACCESS;
            this.mPopupWinWidth = 185;
            this.mPopupWinHeight = 185;
            this.mDensityScale = 1.0f;
            this.mFloatText.setTextSize(35.0f);
            this.mFloatText.setTextColor(-1);
        } else {
            this.mPopupWinX = 120;
        }
        if (this.mDensityDpi == 160) {
            this.mThumbsTextHeight = 12;
            this.mThumbsTextWidth = 27;
        } else if (this.mDensityDpi == 240) {
            this.mThumbsTextHeight = 21;
            this.mThumbsTextWidth = 27;
        } else if (this.mDensityDpi == 270) {
            this.mThumbsTextHeight = 26;
            this.mThumbsTextWidth = 34;
        } else if (this.mDensityDpi == 320) {
            this.mThumbsTextHeight = 35;
            this.mThumbsTextWidth = 56;
            this.mTextColor = -9868951;
        } else if (this.mDensityDpi == 480) {
            this.mThumbsTextHeight = 50;
            this.mThumbsTextWidth = 56;
            this.mTextColor = -9868951;
        } else if (this.mDensityDpi == 640) {
            this.mThumbsTextHeight = 66;
            this.mThumbsTextWidth = 56;
            this.mTextColor = -9868951;
            this.mPopupWinHeight = 70;
            this.mPopupWinWidth = 70;
            this.mPopupWinX = 400;
        } else {
            this.mThumbsTextHeight = 35;
            this.mThumbsTextWidth = 56;
        }
        createPopupWindow();
        initLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewHeight = (MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom()) - getPaddingTop();
        if (viewHeight > 0) {
            updateLayout(viewHeight);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getLocationOnScreen(this.location);
        this.mPopupWinY = this.location[1];
        if (this.mDensityDpi == 480) {
            this.mPopupWinY = 400;
        }
        super.onLayout(changed, l, t, r, b);
    }

    protected Parcelable onSaveInstanceState() {
        setSelectTextViewTextColor(this.mTextColor);
        if (this.mPopupWin.isShowing()) {
            this.mPopupWin.dismiss();
        }
        return super.onSaveInstanceState();
    }

    private void createPopupWindow() {
        this.mFloatText.setText(Events.DEFAULT_SORT_ORDER);
        this.mPopupWin = new PopupWindow(this.mFloatText, (int) (((float) this.mPopupWinWidth) * this.mDensityScale), (int) (((float) this.mPopupWinHeight) * this.mDensityScale));
        this.mPopupWin.setBackgroundDrawable(this.mPopupWinBg);
    }

    private int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        FontMetrics fm = paint.getFontMetrics();
        return ((int) Math.ceil((double) (fm.descent - fm.top))) + 2;
    }

    private void updateLayout(int height) {
        if (this.hasSearchSelect && getChildCount() < this.ABC_STRING.length) {
            LayoutParams p = new LayoutParams(-2, -2);
            TextView text = new TextView(getContext());
            if (this.hasStarredSelect) {
                text.setBackgroundResource(50462793);
            } else {
                text.setBackgroundResource(50462792);
            }
            text.setTextSize(1, 11.0f);
            text.setTextColor(0);
            text.setText(this.ABC_STRING[0]);
            text.setGravity(17);
            text.setPadding(0, 0, 0, 0);
            addView(text, 0, p);
        } else if (!this.hasSearchSelect && getChildCount() == this.ABC_STRING.length) {
            removeViewAt(0);
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            updateViewLayout((TextView) getChildAt(i), new LayoutParams(this.mThumbsTextWidth, this.mThumbsTextHeight));
        }
    }

    private void updateAbcBackground(boolean touch) {
        for (int i = 0; i < getChildCount(); i++) {
            TextView view = (TextView) getChildAt(i);
        }
    }

    private void initLayout() {
        LayoutParams p = new LayoutParams(-2, -2);
        for (int i = this.mStartIndex; i < this.ABC_STRING.length; i++) {
            TextView text = new TextView(getContext());
            text.setTextSize(1, 11.0f);
            text.setTextColor(this.mTextColor);
            text.setText(this.ABC_STRING[i]);
            text.setGravity(17);
            text.setPadding(0, 0, 0, 0);
            addView(text, p);
        }
    }

    private void setSelectTextViewTextColor(int color) {
        if (this.mSelectTextView == null) {
            return;
        }
        if (this.hasSearchSelect && this.mSelectTextView.getText().equals(this.ABC_STRING[0])) {
            this.mSelectTextView.setTextColor(0);
        } else {
            this.mSelectTextView.setTextColor(color);
        }
    }

    private String setSelectTextView(TextView view) {
        this.mSelectTextView = view;
        setSelectTextViewTextColor(this.mSelectTextColor);
        if (this.mPopupWinMoveFlag) {
            this.mPopupWinY = this.mPopupWinStartY + this.mSelectTextView.getTop();
            if (this.mPopupWinY > getHeight() + this.mPopupWinStartY) {
                this.mPopupWinY = (this.mPopupWinStartY + getHeight()) - ((int) (((float) this.mPopupWinHeight) * this.mDensityScale));
            }
        }
        return view.getText().toString();
    }

    private String findAbcTabChild(float yy) {
        int y = (int) yy;
        int count = getChildCount();
        if (this.mSelectTextView != null && y >= this.mSelectTextView.getTop() && y <= this.mSelectTextView.getBottom()) {
            return this.mSelectTextView.getText().toString();
        }
        setSelectTextViewTextColor(this.mTextColor);
        if (count > 0) {
            TextView view = (TextView) getChildAt(0);
            if (y < view.getTop()) {
                return setSelectTextView(view);
            }
            view = (TextView) getChildAt(count - 1);
            if (y > view.getBottom()) {
                return setSelectTextView(view);
            }
            for (int i = 0; i < getChildCount(); i++) {
                view = (TextView) getChildAt(i);
                int top = view.getTop();
                int bottom = view.getBottom();
                if (y >= top && y <= bottom) {
                    return setSelectTextView(view);
                }
            }
        }
        return null;
    }

    public void setThumbsTouchListener(OnClickListener listener) {
        this.mTouchListener = listener;
    }

    public void setPopWinBackground(Drawable d) {
        if (d != null) {
            this.mPopupWinBg = d;
            this.mPopupWin.setBackgroundDrawable(this.mPopupWinBg);
        }
    }

    public void setPopWinLayout(int w, int h) {
        this.mPopupWin.setHeight(h);
        this.mPopupWin.setWidth(w);
    }

    public void setPopWinLocation(int x, int y) {
        this.mPopupWinX = x;
        this.mPopupWinY = y;
    }

    public void setPopWinMoveWithSelectText(boolean move) {
        this.mPopupWinMoveFlag = move;
    }

    public void setFloatTextSize(float size) {
        this.mFloatText.setTextSize(size);
    }

    public void setFloatTextColor(int color) {
        this.mFloatText.setTextColor(color);
    }

    public void setSelectThumbTextColor(int color) {
        this.mSelectTextColor = color;
    }

    public void setThumbsTextColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextColor(color);
        }
    }

    public void setThumbsTextSize(float size) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextSize(1, size);
        }
    }

    public void setThumbsTextHeight(int height) {
        this.mThumbsTextHeight = height;
        updateLayout(0);
    }

    public void setHasSelectSearch(boolean has) {
        this.hasSearchSelect = has;
        if (has) {
            this.mStartIndex = 0;
        }
    }

    public void setHasSelectSearch(boolean has, boolean star) {
        this.hasSearchSelect = has;
        this.hasStarredSelect = star;
        if (has) {
            this.mStartIndex = 0;
        }
    }

    public void setPopWinShow(boolean show) {
        this.showPopupWin = show;
    }

    public boolean getHasSearchSelect() {
        return this.hasSearchSelect;
    }

    public boolean getHasStarredSelect() {
        return this.hasStarredSelect;
    }

    private Drawable createBackground() {
        GradientDrawable mDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{-254750512, -251987206, -251987206});
        mDrawable.setShape(0);
        mDrawable.setGradientCenter(0.1f, 0.0f);
        mDrawable.setGradientRadius(1.0f);
        mDrawable.setCornerRadius(0.0f);
        mDrawable.setAlpha(200);
        return mDrawable;
    }
}
