package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BbkSearchTitleView extends FrameLayout {
    private Context mContext;
    private int mDefaultPadding;
    private OnClickListener mDeleteOnClickListener;
    private Button mLeftButton;
    private RegulaterWatcher mRegulateWatcher;
    private Button mRightButton;
    private LinearLayout mSearchBaseLayout;
    private Button mSearchDeleteButton;
    private LinearLayout mSearchEditLayout;
    private OnClickListener mSearchEditTextOnClickListener;
    private EditText mSearchEditTextView;
    private ImageView mSearchImageView;
    private OnClickListener mSearchTextChanageListener;
    private TextWatcher mSearchTextWatch;
    private View rootView;

    class RegulaterWatcher implements TextWatcher {
        private final int FONT_ADJUST_SIZE = 2;
        private int mMaxWidth = 0;
        private float mOriginTextSize = 0.0f;
        private TextView mView = null;
        final /* synthetic */ BbkSearchTitleView this$0;

        RegulaterWatcher(BbkSearchTitleView this$0, TextView view, int maxWidth) {
            float f = 0.0f;
            this.this$0 = this$0;
            this.mView = view;
            this.mMaxWidth = maxWidth;
            if (view != null) {
                f = view.getTextSize();
            }
            this.mOriginTextSize = f;
        }

        void adjust() {
            int totalWidth = getWrapWidth(this.mView);
            if (this.mView != null && totalWidth > this.mMaxWidth) {
                this.mView.setTextSize(0, this.mOriginTextSize);
                tryAdjustTextSize(this.mView, totalWidth, this.mMaxWidth);
            }
        }

        public void afterTextChanged(Editable s) {
            adjust();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        private boolean tryAdjustTextSize(TextView textView, int totalWidth, int availableLength) {
            textView.setTextSize(0, this.mOriginTextSize - TypedValue.applyDimension(1, 2.0f, this.this$0.mContext.getResources().getDisplayMetrics()));
            if (getWrapWidth(textView) <= availableLength) {
                return true;
            }
            textView.setTextSize(0, this.mOriginTextSize);
            return false;
        }

        private int getWrapWidth(TextView view) {
            if (view == null) {
                return 0;
            }
            int maxWidth = view.getMaxWidth();
            view.setMaxWidth(Integer.MAX_VALUE);
            view.measure(0, 0);
            view.setMaxWidth(maxWidth);
            return view.getMeasuredWidth();
        }
    }

    public BbkSearchTitleView(Context context) {
        this(context, null);
    }

    public BbkSearchTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDefaultPadding = 0;
        this.mRegulateWatcher = null;
        this.mDeleteOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (BbkSearchTitleView.this.mSearchEditTextView != null) {
                    BbkSearchTitleView.this.mSearchEditTextView.setText(Events.DEFAULT_SORT_ORDER);
                }
            }
        };
        this.mSearchTextWatch = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (BbkSearchTitleView.this.mSearchEditTextView.getText().length() == 0) {
                    if (BbkSearchTitleView.this.mSearchDeleteButton != null) {
                        BbkSearchTitleView.this.mSearchDeleteButton.setVisibility(8);
                    }
                } else if (BbkSearchTitleView.this.mSearchDeleteButton != null) {
                    BbkSearchTitleView.this.mSearchDeleteButton.setVisibility(0);
                }
                if (BbkSearchTitleView.this.mSearchTextChanageListener != null) {
                    BbkSearchTitleView.this.mSearchTextChanageListener.onClick(BbkSearchTitleView.this.mSearchEditTextView);
                }
            }
        };
        this.mContext = context;
        initViewLayout();
    }

    private void initViewLayout() {
        TypedArray typedArray = this.mContext.obtainStyledAttributes(null, R.styleable.SearchTitleView, 50397227, 51315008);
        setBackground(typedArray.getDrawable(0));
        setMinimumHeight((int) typedArray.getDimension(1, 0.0f));
        this.mDefaultPadding = (int) typedArray.getDimension(5, 0.0f);
        this.rootView = LayoutInflater.from(this.mContext).inflate(50528357, null);
        this.mLeftButton = (Button) this.rootView.findViewById(51183768);
        this.mSearchEditLayout = (LinearLayout) this.rootView.findViewById(51183769);
        this.mSearchEditLayout.setBackground(typedArray.getDrawable(2));
        this.mSearchImageView = (ImageView) this.rootView.findViewById(51183770);
        this.mSearchImageView.setBackground(typedArray.getDrawable(3));
        this.mSearchDeleteButton = (Button) this.rootView.findViewById(51183772);
        this.mSearchDeleteButton.setOnClickListener(this.mDeleteOnClickListener);
        this.mSearchDeleteButton.setBackground(typedArray.getDrawable(4));
        this.mSearchEditTextView = (EditText) this.rootView.findViewById(51183771);
        this.mSearchEditTextView.addTextChangedListener(this.mSearchTextWatch);
        this.mRightButton = (Button) this.rootView.findViewById(51183773);
        this.mRegulateWatcher = new RegulaterWatcher(this, this.mRightButton, this.mRightButton.getMaxWidth());
        this.mRightButton.addTextChangedListener(this.mRegulateWatcher);
        addView(this.rootView, new LayoutParams(-2, -2));
        typedArray.recycle();
    }

    public void setEnabled(boolean enable) {
        this.mSearchEditLayout.setEnabled(enable);
        this.mSearchEditTextView.setEnabled(enable);
        this.mSearchDeleteButton.setEnabled(enable);
        this.mLeftButton.setEnabled(enable);
        this.mRightButton.setEnabled(enable);
        super.setEnabled(enable);
    }

    public Button getSearchDeleteButton() {
        return this.mSearchDeleteButton;
    }

    public EditText getSearchEditTextView() {
        return this.mSearchEditTextView;
    }

    public Button getSearchRightButton() {
        return this.mRightButton;
    }

    public Button getSearchLeftButton() {
        return this.mLeftButton;
    }

    public void setSearchHeadViewBackground(int res) {
        if (this.mSearchBaseLayout != null) {
            this.mSearchBaseLayout.setBackgroundResource(res);
        }
    }

    public void setSearchImageViewBackground(int res) {
        if (this.mSearchImageView != null) {
            this.mSearchImageView.setBackgroundResource(res);
        }
    }

    public void setSearchEditLayoutBackground(int res) {
        if (this.mSearchEditLayout != null) {
            this.mSearchEditLayout.setBackgroundResource(res);
        }
    }

    public void setSearchDeleteButtonBackground(int res) {
        if (this.mSearchDeleteButton != null) {
            this.mSearchDeleteButton.setBackgroundResource(res);
        }
    }

    public void setSearchLeftButtonBackground(int res) {
        if (this.mLeftButton != null) {
            this.mLeftButton.setBackgroundResource(res);
        }
    }

    public void setSearchRightButtonBackground(int res) {
        if (this.mRightButton != null) {
            this.mRightButton.setBackgroundResource(res);
        }
    }

    public void setSearchTextChanageListener(OnClickListener listener) {
        this.mSearchTextChanageListener = listener;
    }

    public void setLeftButtonEnable(boolean enable) {
        if (this.mLeftButton != null) {
            this.mLeftButton.setEnabled(enable);
            this.mLeftButton.setVisibility(0);
        }
    }

    public void setRightButtonEnable(boolean enable) {
        if (this.mRightButton != null) {
            this.mRightButton.setEnabled(enable);
            this.mRightButton.setVisibility(0);
        }
    }

    public void setLeftButtonClickListener(OnClickListener listener) {
        if (listener != null) {
            this.mLeftButton.setOnClickListener(listener);
        }
    }

    public void setRightButtonClickListener(OnClickListener listener) {
        if (listener != null) {
            this.mRightButton.setOnClickListener(listener);
        }
    }

    public void showTitleLeftButton(CharSequence leftText) {
        if (this.mLeftButton != null) {
            LinearLayout.LayoutParams peditLayout = (LinearLayout.LayoutParams) this.mSearchEditLayout.getLayoutParams();
            if (leftText != null) {
                this.mLeftButton.setVisibility(0);
                this.mLeftButton.setText(leftText);
                peditLayout.setMarginStart(0);
                return;
            }
            this.mLeftButton.setVisibility(8);
            peditLayout.setMarginStart(this.mDefaultPadding);
        }
    }

    public void showTitleRightButton(CharSequence rightText) {
        if (this.mRightButton != null) {
            LinearLayout.LayoutParams peditLayout = (LinearLayout.LayoutParams) this.mSearchEditLayout.getLayoutParams();
            if (rightText != null) {
                this.mRightButton.setVisibility(0);
                this.mRightButton.setText(rightText);
                peditLayout.setMarginEnd(0);
                return;
            }
            this.mRightButton.setVisibility(8);
            peditLayout.setMarginEnd(this.mDefaultPadding);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.rootView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
        this.mRegulateWatcher.adjust();
        this.rootView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
    }
}
