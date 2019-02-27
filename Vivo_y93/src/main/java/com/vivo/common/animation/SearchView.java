package com.vivo.common.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SearchView extends LinearLayout {
    private static final int MAX_TEXT_LEN = 255;
    private static final String TAG = "SearchView";
    private int INTERVAL;
    private boolean isSearchMode;
    private ValueAnimator mAnimator;
    private AnimatorListener mAnimatorHidenListener;
    private AnimatorListener mAnimatorShowListener;
    private AnimatorUpdateListener mAnimatorUpdateListener;
    private Button mButton;
    private OnClickListener mButtonClickLinstener;
    private boolean mButtonVisible;
    private int mButtonWidth;
    private ImageView mClearButton;
    private boolean mCodeClear;
    private Context mContext;
    private boolean mDirection;
    private EditText mEditText;
    private boolean mEnableInnerClick;
    private int mHeightMeasureSpec;
    IScrollLock mIScrollLock;
    private boolean mIgonreCheck;
    private Drawable mNormalDrawable;
    private OnClickListener mOnClickListener;
    private int mPadding;
    private float mProgress;
    private ExtendSearchContent mSearchContent;
    private int mSearchContentMarginLeft;
    private int mSearchContentMarginRight;
    private int mSearchContentOriginWidth;
    private SearchControl mSearchControl;
    private ImageView mSearchImage;
    private SearchLinstener mSearchLinstener;
    private Drawable mSearchResoultBackground;
    private SearchViewListener mSearchViewListener;
    private Drawable mShadowDrawable;
    private Rect mShadowRect;
    private boolean mSwitchWithAm;
    private TextWatcher mTextWatcher;
    private int mWidthDiff;
    private int mWidthMeasureSpec;

    public interface IScrollLock {
        void lockScroll();

        void unLockScroll();
    }

    interface SearchViewListener {
        void onSearchTextChanged(String str);

        void onSwitchToNormalStateEnd();

        void onSwitchToNormalStateStart();

        void onSwitchToSearchStateEnd();

        void onSwitchToSearchStateStart();

        void onSwitchingToNormal(float f);

        void onSwitchingToSearch(float f);
    }

    class ExtendSearchContent extends LinearLayout {
        ExtendSearchContent(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!isEnabled()) {
                getDrawingRect(SearchView.this.mShadowRect);
                SearchView.this.mShadowDrawable.setBounds(SearchView.this.mShadowRect);
                SearchView.this.mShadowDrawable.draw(canvas);
            }
        }
    }

    public interface SearchLinstener {
        void onSearchTextChanged(String str);

        boolean processSearchClick();
    }

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mButtonVisible = false;
        this.mSearchContentMarginLeft = 15;
        this.mSearchContentMarginRight = 15;
        this.mWidthDiff = 0;
        this.mButtonWidth = 100;
        this.mSearchContentOriginWidth = 0;
        this.mSearchViewListener = null;
        this.mEnableInnerClick = true;
        this.isSearchMode = false;
        this.INTERVAL = CalendarsColumns.RESPOND_ACCESS;
        this.mCodeClear = false;
        this.mSwitchWithAm = true;
        this.mShadowRect = new Rect();
        this.mWidthMeasureSpec = 0;
        this.mHeightMeasureSpec = 0;
        this.mSearchResoultBackground = null;
        this.mPadding = 0;
        this.mAnimatorUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                SearchView.this.updateLayoutParam(value);
                if (SearchView.this.mSearchViewListener == null) {
                    return;
                }
                if (SearchView.this.mButtonVisible) {
                    SearchView.this.mSearchViewListener.onSwitchingToSearch(value);
                } else {
                    SearchView.this.mSearchViewListener.onSwitchingToNormal(value);
                }
            }
        };
        this.mDirection = false;
        this.mAnimatorShowListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (SearchView.this.mSearchViewListener != null) {
                    SearchView.this.mSearchViewListener.onSwitchToSearchStateStart();
                }
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (SearchView.this.mSearchViewListener != null) {
                    SearchView.this.mSearchViewListener.onSwitchToSearchStateEnd();
                }
                SearchView.this.showInput();
            }

            public void onAnimationCancel(Animator animation) {
            }
        };
        this.mAnimatorHidenListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (SearchView.this.mSearchViewListener != null) {
                    SearchView.this.mSearchViewListener.onSwitchToNormalStateStart();
                }
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (SearchView.this.mSearchViewListener != null) {
                    SearchView.this.mSearchViewListener.onSwitchToNormalStateEnd();
                }
            }

            public void onAnimationCancel(Animator animation) {
            }
        };
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                SearchView.this.Log("onClick   mEnableInnerClick:" + SearchView.this.mEnableInnerClick);
                if (SearchView.this.mButton.equals(v)) {
                    if (SearchView.this.mEnableInnerClick && SearchView.this.mButtonVisible && (SearchView.this.isAnimRun() ^ 1) != 0) {
                        SearchView.this.hidenSearch();
                    }
                    if (SearchView.this.mButtonClickLinstener != null) {
                        SearchView.this.mButtonClickLinstener.onClick(SearchView.this.mButton);
                    }
                } else if (SearchView.this.mEditText.equals(v)) {
                    if (!(SearchView.this.mSearchLinstener == null || !SearchView.this.mSearchLinstener.processSearchClick() || SearchView.this.mButtonVisible || (SearchView.this.isAnimRun() ^ 1) == 0)) {
                        SearchView.this.showSearch();
                    }
                } else if (SearchView.this.mClearButton.equals(v)) {
                    SearchView.this.showInput();
                    SearchView.this.mEditText.setText(Events.DEFAULT_SORT_ORDER);
                }
            }
        };
        this.mTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable arg0) {
                if (SearchView.this.mSearchViewListener != null && (SearchView.this.mCodeClear ^ 1) != 0) {
                    if (arg0.toString().equals(Events.DEFAULT_SORT_ORDER) && SearchView.this.mClearButton.getVisibility() != 8) {
                        SearchView.this.mClearButton.setVisibility(8);
                    } else if (SearchView.this.mClearButton.getVisibility() == 8) {
                        SearchView.this.mClearButton.setVisibility(0);
                    }
                    SearchView.this.mSearchViewListener.onSearchTextChanged(arg0.toString());
                    if (SearchView.this.mSearchLinstener != null) {
                        SearchView.this.mSearchLinstener.onSearchTextChanged(arg0.toString());
                    }
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        };
        this.mIgonreCheck = false;
        init(context);
    }

    private void Log(String str) {
        Log.d(TAG, str);
    }

    private void updateLayoutParam(float value) {
        this.mProgress = value;
        LayoutParams lp = (LayoutParams) this.mSearchContent.getLayoutParams();
        lp.setMarginEnd((int) (((float) this.mSearchContentMarginRight) * (1.0f - value)));
        this.mSearchContent.setLayoutParams(lp);
        this.mSearchContent.getLayoutParams().width = this.mSearchContentOriginWidth - ((int) (((float) this.mWidthDiff) * value));
        requestSelfLayout();
    }

    private void showInput() {
        this.mEditText.setFocusable(true);
        this.mEditText.setFocusableInTouchMode(true);
        this.mEditText.requestFocus();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.showSoftInput(this.mEditText, 0);
        }
    }

    private void hidenInput() {
        this.mEditText.setFocusable(false);
        this.mEditText.setFocusableInTouchMode(false);
        this.mEditText.requestFocus();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mEditText.getWindowToken(), 0);
        }
    }

    private void clearEditText() {
        this.mCodeClear = true;
        this.mEditText.setText(Events.DEFAULT_SORT_ORDER);
        this.mCodeClear = false;
    }

    private void init(Context context) {
        this.mContext = context;
        setOrientation(0);
        TypedArray searchtype = this.mContext.obtainStyledAttributes(null, R.styleable.SearchView, 50397214, 51314973);
        setBackground(searchtype.getDrawable(0));
        this.mSearchContent = new ExtendSearchContent(context);
        LayoutParams layout = new LayoutParams(-1, -2);
        this.mPadding = searchtype.getDimensionPixelSize(6, 0);
        this.mSearchContentMarginRight = this.mPadding;
        this.mSearchContentMarginLeft = this.mPadding;
        layout.setMarginStart(this.mPadding);
        layout.setMarginEnd(this.mPadding);
        layout.gravity = 16;
        addView(this.mSearchContent, layout);
        this.mSearchImage = new ImageView(context);
        this.mSearchImage.setImageDrawable(searchtype.getDrawable(1));
        int searchImagePadding = getResources().getDimensionPixelSize(51118355);
        this.mSearchImage.setPadding(searchImagePadding, 0, searchImagePadding, 0);
        layout = new LayoutParams(-2, -2);
        layout.gravity = 16;
        this.mSearchContent.addView(this.mSearchImage, layout);
        this.mEditText = new EditText(context, null, 50397231);
        this.mEditText.setFilters(new InputFilter[]{new LengthFilter(MAX_TEXT_LEN)});
        this.mEditText.setBackground(null);
        this.mEditText.addTextChangedListener(this.mTextWatcher);
        this.mEditText.setOnClickListener(this.mOnClickListener);
        layout = new LayoutParams(0, -1);
        layout.weight = 1.0f;
        layout.gravity = 16;
        this.mSearchContent.addView(this.mEditText, layout);
        this.mClearButton = new ImageView(this.mContext);
        this.mClearButton.setImageDrawable(searchtype.getDrawable(2));
        this.mClearButton.setPadding(searchImagePadding, 0, searchImagePadding, 0);
        this.mClearButton.setOnClickListener(this.mOnClickListener);
        this.mClearButton.setVisibility(8);
        layout = new LayoutParams(-2, -2);
        layout.gravity = 16;
        this.mSearchContent.addView(this.mClearButton, layout);
        this.mButton = (Button) LayoutInflater.from(this.mContext).inflate(50528358, null);
        this.mButton.setText(17039360);
        this.mButtonWidth = getButtonWidth(this.mButton);
        this.mButton.setBackground(null);
        this.mButton.setOnClickListener(this.mOnClickListener);
        layout = new LayoutParams(this.mButtonWidth, -1);
        layout.gravity = 16;
        addView(this.mButton, layout);
        this.mShadowDrawable = searchtype.getDrawable(4);
        this.mNormalDrawable = searchtype.getDrawable(3);
        this.mSearchContent.setBackground(this.mNormalDrawable);
        this.mSearchResoultBackground = searchtype.getDrawable(7);
        searchtype.recycle();
    }

    private int getButtonWidth(View button) {
        measureButton(button);
        return this.mButton.getMeasuredWidth();
    }

    private void measureButton(View button) {
        ViewGroup.LayoutParams lp = this.mButton.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-2, -2);
        }
        button.setLayoutParams(lp);
        button.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
    }

    private void resetButtonWidth(View button) {
        this.mButtonWidth = getButtonWidth(this.mButton);
        this.mButton.getLayoutParams().width = this.mButtonWidth;
        this.mButton.requestLayout();
    }

    private void switchToSearchModle() {
        this.mWidthDiff = this.mButtonWidth - this.mSearchContentMarginRight;
        this.mSearchContentOriginWidth = this.mSearchContent.getWidth();
        this.mDirection = true;
        ValueAnimator am = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        am.addUpdateListener(this.mAnimatorUpdateListener);
        am.addListener(this.mAnimatorShowListener);
        am.setDuration((long) this.INTERVAL);
        this.mAnimator = am;
        am.start();
    }

    private void switchToNormal() {
        if (this.mClearButton.getVisibility() == 0) {
            this.mClearButton.setVisibility(8);
        }
        ValueAnimator am = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        am.addUpdateListener(this.mAnimatorUpdateListener);
        am.addListener(this.mAnimatorHidenListener);
        am.setDuration((long) this.INTERVAL);
        am.setStartDelay(300);
        this.mAnimator = am;
        hidenInput();
        am.start();
    }

    private boolean isAnimRun() {
        if (this.mAnimator == null) {
            return false;
        }
        return this.mAnimator.isStarted();
    }

    public String getSearchText() {
        return this.mEditText.getText().toString();
    }

    public void setSearchHint(String hint) {
        this.mEditText.setHint(hint);
    }

    public void setSearchHintTextColor(int color) {
        this.mEditText.setHintTextColor(color);
    }

    void setAnimatorProgressListener(SearchViewListener l) {
        this.mSearchViewListener = l;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!this.mIgonreCheck) {
            if (!this.isSearchMode || ((double) this.mProgress) < 1.0d) {
                if (!(this.isSearchMode || this.mProgress > 0.0f || this.mSearchContent.getLayoutParams().width == getMeasuredWidth() - (this.mPadding * 2))) {
                    Log.d(TAG, "  isSearchMode ---------");
                    this.mSearchContent.getLayoutParams().width = getMeasuredWidth() - (this.mPadding * 2);
                    this.mSearchContentOriginWidth = this.mSearchContent.getLayoutParams().width;
                    requestSelfLayout();
                }
            } else if (this.mSearchContent.getLayoutParams().width != (getMeasuredWidth() - this.mButtonWidth) - this.mPadding) {
                Log.d(TAG, "  isSearchMode ++++++++++");
                this.mSearchContent.getLayoutParams().width = (getMeasuredWidth() - this.mButtonWidth) - this.mPadding;
                this.mSearchContentOriginWidth = getMeasuredWidth() - (this.mPadding * 2);
                requestSelfLayout();
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidthMeasureSpec = widthMeasureSpec;
        this.mHeightMeasureSpec = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void requestSelfLayout() {
        if (this.mWidthMeasureSpec != 0) {
            onMeasure(this.mWidthMeasureSpec, this.mHeightMeasureSpec);
            this.mIgonreCheck = true;
            onLayout(false, getLeft(), getTop(), getRight(), getBottom());
            this.mIgonreCheck = false;
            invalidate();
        }
    }

    public void setSearchMarginRight(int value) {
        this.mSearchContentMarginRight = value;
        LayoutParams lp = (LayoutParams) this.mSearchContent.getLayoutParams();
        lp.setMarginEnd(value);
        this.mSearchContent.setLayoutParams(lp);
        requestSelfLayout();
    }

    public void setSearchMarginLeft(int value) {
        this.mSearchContentMarginLeft = value;
        LayoutParams lp = (LayoutParams) this.mSearchContent.getLayoutParams();
        lp.setMarginStart(value);
        this.mSearchContent.setLayoutParams(lp);
        requestSelfLayout();
    }

    public void setOnButtonClickLinster(OnClickListener l) {
        this.mButtonClickLinstener = l;
    }

    public void setEnableInnerButtonClickProcess(boolean value) {
        this.mEnableInnerClick = value;
    }

    public void setAnimatorDuration(int duration) {
        this.INTERVAL = duration;
    }

    public SearchControl getSearchControl() {
        if (this.mSearchControl == null) {
            this.mSearchControl = new SearchControl(this.mContext);
            this.mSearchControl.setSearchView(this);
        }
        return this.mSearchControl;
    }

    private void hidenSearchDirectly() {
        hidenInput();
        updateLayoutParam(0.0f);
        if (this.mClearButton.getVisibility() == 0) {
            this.mClearButton.setVisibility(8);
        }
        this.mSearchControl.switchToNormalDirectlyEnd();
    }

    void hidenSearch() {
        this.isSearchMode = false;
        if (this.mButtonVisible && (isAnimRun() ^ 1) != 0 && this.mSearchControl != null) {
            this.mButtonVisible = false;
            clearEditText();
            if (this.mSwitchWithAm) {
                switchToNormal();
            } else {
                hidenSearchDirectly();
            }
        }
    }

    void showSearch() {
        this.isSearchMode = true;
        if (!this.mButtonVisible && (isAnimRun() ^ 1) != 0 && this.mSearchControl != null) {
            this.mButtonVisible = true;
            if (this.mSwitchWithAm) {
                switchToSearchModle();
            } else {
                showSearchDirectly();
            }
        }
    }

    private void showSearchDirectly() {
        this.mWidthDiff = this.mButtonWidth - this.mSearchContentMarginRight;
        this.mSearchContentOriginWidth = this.mSearchContent.getWidth();
        updateLayoutParam(1.0f);
        showInput();
        this.mSearchControl.switchToSearchModleDirectlyEnd();
    }

    public void setScrollLockImp(IScrollLock imp) {
        this.mIScrollLock = imp;
    }

    public void setSearchLinstener(SearchLinstener l) {
        this.mSearchLinstener = l;
    }

    void setSwitchWithAnimate(boolean value) {
        this.mSwitchWithAm = value;
    }

    public void setTextColor(int color) {
        this.mEditText.setTextColor(color);
    }

    public void setTextSize(int size) {
        this.mEditText.setTextSize(1, (float) size);
    }

    public void setButtonText(String text) {
        this.mButton.setText(text);
        resetButtonWidth(this.mButton);
    }

    public void setButtonTextSize(float size) {
        this.mButton.setTextSize(1, size);
        resetButtonWidth(this.mButton);
    }

    public void setButtonBackground(int resid) {
        this.mButton.setBackgroundResource(resid);
        resetButtonWidth(this.mButton);
    }

    public void setButtonBackground(Bitmap image) {
        this.mButton.setBackground(new BitmapDrawable(image));
        resetButtonWidth(this.mButton);
    }

    public void setButtonBackground(Drawable drawable) {
        this.mButton.setBackgroundDrawable(drawable);
        resetButtonWidth(this.mButton);
    }

    public void setButtonTextColor(int color) {
        this.mButton.setTextColor(color);
    }

    public void setButtonTextColor(ColorStateList color) {
        this.mButton.setTextColor(color);
    }

    public void setSearchContentBackground(Bitmap image) {
        this.mNormalDrawable = new BitmapDrawable(image);
        this.mSearchContent.setBackground(this.mNormalDrawable);
    }

    public void setSearchContentBackground(Drawable drawable) {
        this.mSearchContent.setBackgroundDrawable(drawable);
        this.mNormalDrawable = drawable;
    }

    public void setSearchContentBackground(int resid) {
        this.mSearchContent.setBackgroundResource(resid);
        this.mNormalDrawable = this.mSearchContent.getBackground();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mEditText.setEnabled(enabled);
        this.mSearchContent.setEnabled(enabled);
    }

    public void setFindMarkImage(int resid) {
        this.mSearchImage.setImageResource(resid);
    }

    public void setFindMarkImage(Bitmap image) {
        this.mSearchImage.setImageBitmap(image);
    }

    public void setFindMarkImage(Drawable drawable) {
        this.mSearchImage.setImageDrawable(drawable);
    }

    public void setClearMarkImage(int resid) {
        this.mClearButton.setImageResource(resid);
    }

    public void setClearMarkImage(Drawable drawable) {
        this.mClearButton.setImageDrawable(drawable);
    }

    public void setDisableShadowProgess(float p) {
        this.mShadowDrawable.setAlpha((int) (((double) (255.0f * p)) + 0.5d));
        this.mSearchContent.invalidate();
    }

    public void setDisableShadow(Drawable drawable) {
        this.mShadowDrawable = drawable;
    }

    public void setDisableShadow(Bitmap image) {
        this.mShadowDrawable = new BitmapDrawable(image);
    }

    public void setSoftInputType(int type) {
        this.mEditText.setInputType(type);
    }

    protected Drawable getSearchResoultBackground() {
        return this.mSearchResoultBackground;
    }

    public void setSearchResoultBackground(Drawable d) {
        this.mSearchResoultBackground = d;
    }
}
