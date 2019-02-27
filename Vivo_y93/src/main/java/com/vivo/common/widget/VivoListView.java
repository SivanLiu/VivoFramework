package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.vivo.app.IVivoTitle;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoListView extends ListView {
    private boolean hasSearchHead;
    private OnClickListener mDeleteOnClickListener;
    private float mDensityScale;
    private LinearLayout mSearchBaseLayout;
    private Button mSearchDeleteButton;
    private LinearLayout mSearchEditLayout;
    private OnClickListener mSearchEditTextOnClickListener;
    private EditText mSearchEditTextView;
    private ImageView mSearchImageView;
    private OnClickListener mSearchTextChanageListener;
    private TextWatcher mSearchTextWatch;

    public VivoListView(Context context) {
        this(context, null);
    }

    public VivoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public VivoListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.hasSearchHead = false;
        this.mDeleteOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (VivoListView.this.mSearchEditTextView != null) {
                    VivoListView.this.mSearchEditTextView.setText(Events.DEFAULT_SORT_ORDER);
                }
            }
        };
        this.mSearchTextWatch = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (VivoListView.this.mSearchEditTextView.getText().length() == 0) {
                    if (VivoListView.this.mSearchDeleteButton != null) {
                        VivoListView.this.mSearchDeleteButton.setVisibility(8);
                    }
                } else if (VivoListView.this.mSearchDeleteButton != null) {
                    VivoListView.this.mSearchDeleteButton.setVisibility(0);
                }
                if (VivoListView.this.mSearchTextChanageListener != null) {
                    VivoListView.this.mSearchTextChanageListener.onClick(VivoListView.this.mSearchEditTextView);
                }
            }
        };
    }

    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (getAdapter() != null) {
            Context context = getContext();
            if (context instanceof IVivoTitle) {
                ((IVivoTitle) context).setOnTitleClickListener((View) this);
            }
        }
    }

    public void setHasSearchHeadView(boolean has) {
        this.hasSearchHead = has;
        if (this.hasSearchHead) {
            this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
            initViewLayout(8, 8);
        }
    }

    public void setHasSearchHeadView(boolean has, int editMarginLeft, int editMarginRight) {
        this.hasSearchHead = has;
        if (this.hasSearchHead) {
            this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
            initViewLayout((int) (((float) editMarginLeft) * this.mDensityScale), (int) (((float) editMarginRight) * this.mDensityScale));
        }
    }

    public Button getSearchDeleteButton() {
        return this.mSearchDeleteButton;
    }

    public EditText getSearchEditTextView() {
        return this.mSearchEditTextView;
    }

    public ImageView getSearchImageView() {
        return this.mSearchImageView;
    }

    public LinearLayout getSearchBaseLayout() {
        return this.mSearchBaseLayout;
    }

    public LinearLayout getSearchEditLayout() {
        return this.mSearchEditLayout;
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

    public void setSearchTextChanageListener(OnClickListener listener) {
        this.mSearchTextChanageListener = listener;
    }

    public void setSearchEditTextOnClickListener(OnClickListener listener) {
        this.mSearchEditTextOnClickListener = listener;
        if (this.mSearchEditTextOnClickListener != null && this.mSearchEditTextView != null) {
            this.mSearchEditTextView.setFocusable(false);
            this.mSearchEditTextView.setFocusableInTouchMode(false);
            this.mSearchEditTextView.setOnClickListener(listener);
        }
    }

    public void setSearchEditLayoutLeftAndRightMargin(int editMarginLeft, int editMarginRight) {
        if (this.mSearchEditLayout != null) {
            LayoutParams peditLayout = (LayoutParams) this.mSearchEditLayout.getLayoutParams();
            peditLayout.setMarginStart((int) (((float) editMarginLeft) * this.mDensityScale));
            peditLayout.setMarginEnd((int) (((float) editMarginRight) * this.mDensityScale));
        }
    }

    private void initViewLayout(int editMarginLeft, int editMarginRight) {
        TypedArray typedArray = this.mContext.obtainStyledAttributes(null, R.styleable.SearchView, 50397214, 51314973);
        this.mSearchBaseLayout = new LinearLayout(this.mContext);
        this.mSearchBaseLayout.setMinimumHeight((int) this.mContext.getResources().getDimension(51118080));
        this.mSearchBaseLayout.setBackground(typedArray.getDrawable(0));
        this.mSearchEditLayout = new LinearLayout(this.mContext);
        this.mSearchEditLayout.setGravity(16);
        this.mSearchEditLayout.setBackground(typedArray.getDrawable(3));
        LayoutParams peditLayout = new LayoutParams(-1, -2);
        peditLayout.setMarginStart(editMarginLeft);
        peditLayout.setMarginEnd(editMarginRight);
        peditLayout.gravity = 17;
        if (this.mContext.getResources().getDisplayMetrics().densityDpi == 320) {
            peditLayout.height = 64;
        }
        this.mSearchBaseLayout.addView(this.mSearchEditLayout, peditLayout);
        this.mSearchBaseLayout.setFocusable(true);
        LayoutParams pSearchImage = new LayoutParams(-2, -2);
        pSearchImage.gravity = 16;
        int densityDpi = this.mContext.getResources().getDisplayMetrics().densityDpi;
        if (densityDpi == 320) {
            pSearchImage.setMarginStart(0);
        } else if (densityDpi == 480) {
            pSearchImage.setMarginStart(0);
        }
        this.mSearchImageView = new ImageView(this.mContext);
        this.mSearchImageView.setBackground(typedArray.getDrawable(1));
        this.mSearchEditLayout.addView(this.mSearchImageView, pSearchImage);
        LayoutParams pedit = new LayoutParams(-1, -1);
        pedit.weight = 1.0f;
        this.mSearchEditTextView = new EditText(this.mContext);
        this.mSearchEditTextView.setBackgroundDrawable(null);
        this.mSearchEditTextView.setPadding(0, 0, 0, 0);
        this.mSearchEditTextView.addTextChangedListener(this.mSearchTextWatch);
        this.mSearchEditTextView.setSingleLine();
        if (this.mContext.getResources().getDisplayMetrics().densityDpi == 320) {
            this.mSearchEditTextView.setTextSize(18.0f);
        }
        this.mSearchEditTextView.setImeOptions(3);
        this.mSearchEditLayout.addView(this.mSearchEditTextView, pedit);
        LayoutParams pButton = new LayoutParams(-2, -2);
        this.mSearchDeleteButton = new Button(this.mContext);
        this.mSearchDeleteButton.setBackground(typedArray.getDrawable(2));
        this.mSearchDeleteButton.setOnClickListener(this.mDeleteOnClickListener);
        this.mSearchDeleteButton.setVisibility(8);
        this.mSearchEditLayout.addView(this.mSearchDeleteButton, pButton);
        addHeaderView(this.mSearchBaseLayout);
    }

    public void removeSeacherHeaderView() {
        if (this.mSearchBaseLayout != null && this.hasSearchHead) {
            removeHeaderView(this.mSearchBaseLayout);
            this.hasSearchHead = false;
        }
    }

    public void addSeacherHeaderView() {
        if (!this.hasSearchHead) {
            if (this.mSearchBaseLayout != null) {
                this.hasSearchHead = true;
                addHeaderView(this.mSearchBaseLayout);
            } else {
                setHasSearchHeadView(true);
            }
        }
    }
}
