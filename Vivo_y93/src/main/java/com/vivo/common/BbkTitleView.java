package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.FtBuild;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.vivo.app.VivoBaseActivity;
import com.vivo.common.widget.TitleView;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BbkTitleView extends TitleView {
    private static final String ELLIPSIZE_NORMAL = new String(new char[]{8230});
    private static final int ICON_LEFT_BUTTON = 2;
    private static final int ICON_RIGHT_BUTTON = 5;
    public static final int LEFT_ICON_FIRST = 12;
    public static final int LEFT_ICON_SEC = 14;
    public static final int LEFT_LABEL_BUTTON = 10;
    private static final int NORMAL_BG_ID = 50462963;
    public static final int RIGHT_ICON_FIRST = 7;
    public static final int RIGHT_ICON_SEC = 9;
    public static final int TITLE_BTN_BACK = 2;
    public static final int TITLE_BTN_CREATE = 3;
    public static final int TITLE_BTN_NEW = 4;
    public static final int TITLE_BTN_NORMAL = 1;
    private final boolean DEBUG;
    private int MAIN_TITLE_OFFSET;
    private int SUB_TITLE_OFFSET;
    private final String TAG;
    private int labelButtonTextOffset;
    private boolean mIsDefaultBackImage;
    private Button mLeftButton;
    private TextView mMainTitleView;
    private Button mRightButton;
    private TextView mRightIconFirst;
    private TextView mRightIconSec;
    private View mRootView;
    private TextView mSubTitleView;
    private View mTitleClickListView;

    private final class TitleTextProxy extends ViewProxy {
        private int maxLettersInWord;

        /* synthetic */ TitleTextProxy(BbkTitleView this$0, TitleTextProxy -this1) {
            this();
        }

        private TitleTextProxy() {
            super();
            this.maxLettersInWord = 2;
        }

        public int getContentWidth() {
            return Math.max(getViewContentWidth(BbkTitleView.this.mMainTitleView), getViewContentWidth(BbkTitleView.this.mSubTitleView));
        }

        public int getContentMinWidth() {
            return Math.max(getViewContentMinWidth(BbkTitleView.this.mMainTitleView), getViewContentMinWidth(BbkTitleView.this.mSubTitleView));
        }

        public void setOffset(int sOff, int eOff) {
            BbkTitleView.this.mMainTitleView.setPaddingRelative(sOff, BbkTitleView.this.mMainTitleView.getPaddingTop(), eOff, BbkTitleView.this.mMainTitleView.getPaddingBottom());
            BbkTitleView.this.mSubTitleView.setPaddingRelative(sOff, BbkTitleView.this.mSubTitleView.getPaddingTop(), eOff, BbkTitleView.this.mSubTitleView.getPaddingBottom());
        }

        private int getViewContentWidth(TextView view) {
            if (view == null) {
                return 0;
            }
            int titleLen = 0;
            if (view.getVisibility() == 0 && view.getText() != null) {
                titleLen = (int) view.getPaint().measureText(view.getText().toString());
            }
            return titleLen;
        }

        private int getViewContentMinWidth(TextView view) {
            int needWidth = 0;
            if (view == null || view.getText() == null) {
                return 0;
            }
            TextPaint paint = view.getPaint();
            String str = view.getText().toString();
            for (int i = 1; i <= str.length(); i++) {
                needWidth = (int) paint.measureText(str.substring(0, i) + BbkTitleView.ELLIPSIZE_NORMAL);
                if (TextUtils.ellipsize(str, paint, (float) needWidth, TruncateAt.END).length() > 0) {
                    break;
                }
            }
            return needWidth;
        }
    }

    public BbkTitleView(Context context) {
        this(context, null);
    }

    public BbkTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "BbkTitltView";
        this.DEBUG = false;
        this.labelButtonTextOffset = 0;
        this.MAIN_TITLE_OFFSET = -3;
        this.SUB_TITLE_OFFSET = 3;
        float density = context.getResources().getDisplayMetrics().density;
        this.MAIN_TITLE_OFFSET = (int) Math.floor((double) (((float) this.MAIN_TITLE_OFFSET) * density));
        this.SUB_TITLE_OFFSET = (int) Math.floor((double) (((float) this.SUB_TITLE_OFFSET) * density));
        this.labelButtonTextOffset = this.mContext.getResources().getDimensionPixelOffset(51118232);
        initDefaultLayout();
        setOnTitleClickListener();
    }

    private void initDefaultLayout() {
        addIconViewWidthId(2);
        addIconViewWidthId(5);
        this.mLeftButton = (Button) getIconViewById(2);
        this.mRightButton = (Button) getIconViewById(5);
        this.mLeftButton.setVisibility(8);
        this.mRightButton.setVisibility(8);
    }

    protected ViewProxy initCenterView() {
        this.mCenterView.setOrientation(1);
        this.mRootView = LayoutInflater.from(this.mContext).inflate(50528365, null);
        this.mMainTitleView = (TextView) this.mRootView.findViewById(51183778);
        this.mSubTitleView = (TextView) this.mRootView.findViewById(51183777);
        this.mCenterView.addView(this.mRootView, new LayoutParams(-1, -1));
        if (this.mMainTitleView != null) {
            return new TitleTextProxy(this, null);
        }
        throw new RuntimeException("no find TextView identified : main");
    }

    protected Button initIconView(int position) {
        Button view = new Button(this.mContext, null, 50397185);
        if (!isROM3_0()) {
            view.setGravity(17);
        } else if (position == 0) {
            view.setGravity(8388627);
        } else {
            view.setGravity(8388629);
        }
        return view;
    }

    protected void updateIconViewGapByUser(IconViewInformation info, boolean visible, int position) {
        if (visible && info.self == getIconViewById(10)) {
            MarginLayoutParams lp = (MarginLayoutParams) info.self.getLayoutParams();
            lp.setMarginStart(this.imagePaddingOuter);
            info.self.setLayoutParams(lp);
        }
    }

    public void initRightIconButton() {
        if (getIconViewById(7) == null) {
            addIconViewWidthId(7);
        }
        if (getIconViewById(9) == null) {
            addIconViewWidthId(9);
        }
        this.mRightIconFirst = (TextView) getIconViewById(7);
        this.mRightIconSec = (TextView) getIconViewById(9);
        this.mRightIconFirst.setBackground(null);
        this.mRightIconSec.setBackground(null);
    }

    public void initLeftLabelButton(CharSequence text, int drawableId) {
        if (getIconViewById(10) == null) {
            addIconViewWidthId(10);
            TextView view = (TextView) getIconViewById(10);
            view.setBackgroundResource(getBtnBgResId(drawableId));
            view.setText(text);
            view.setPaddingRelative(Math.max(0, this.labelButtonTextOffset - this.imagePaddingOuter), 0, 0, 0);
        }
    }

    public void initLeftIconButton() {
        if (getIconViewById(12) == null) {
            addIconViewWidthId(12);
        }
        if (getIconViewById(14) == null) {
            addIconViewWidthId(14);
        }
    }

    private boolean isROM3_0() {
        return FtBuild.getRomVersion() >= 3.0f;
    }

    public Button getLeftButton() {
        return this.mLeftButton;
    }

    public Button getRightButton() {
        return this.mRightButton;
    }

    public TextView getCenterView() {
        return this.mMainTitleView;
    }

    public void showLeftButton() {
        setIconViewVisible(2, true);
    }

    public void showRightButton() {
        setIconViewVisible(5, true);
    }

    public void hideLeftButton() {
        setIconViewVisible(2, false);
    }

    public void hideRightButton() {
        setIconViewVisible(5, false);
    }

    public void setIconViewVisible(int id, boolean visible) {
        View view = getIconViewById(id);
        if (view != null) {
            view.setVisibility(visible ? 0 : 8);
        } else {
            Log.e("BbkTitltView", "setIconViewVisible failed id[" + id + "]");
        }
    }

    public void initLeftButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        setButton(2, leftText, drawableId, listener);
    }

    public void initRightButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        setButton(5, leftText, drawableId, listener);
    }

    public void setLeftButtonEnable(boolean enable) {
        this.mLeftButton.setEnabled(enable);
    }

    public void setRightButtonEnable(boolean enable) {
        this.mRightButton.setEnabled(enable);
    }

    public void setLeftButtonText(CharSequence text) {
        setIconViewText(2, text);
    }

    public void setRightButtonText(CharSequence text) {
        setIconViewText(5, text);
    }

    public void setCenterText(CharSequence text) {
        CharSequence str = this.mMainTitleView.getText();
        if (str == null || (str.equals(text) ^ 1) != 0) {
            this.mMainTitleView.setText(text);
            adjustLayoutByUser();
        }
    }

    public void setCenterSubText(CharSequence text) {
        CharSequence str = this.mSubTitleView == null ? null : this.mSubTitleView.getText();
        if (this.mSubTitleView == null || (str != null && (str.equals(text) ^ 1) == 0)) {
            Log.e("BbkTitltView", "setCenterSubText [" + str + "] failed");
            return;
        }
        this.mSubTitleView.setText(text);
        adjustLayoutByUser();
    }

    public void setIconViewText(int id, CharSequence text) {
        if (getIconViewById(id) instanceof TextView) {
            TextView view = (TextView) getIconViewById(id);
            view.setText(text);
            switch (id) {
                case 7:
                case 9:
                case 10:
                    break;
                default:
                    view.setBackgroundResource(NORMAL_BG_ID);
                    break;
            }
        }
    }

    public void setIconViewEnabled(int id, boolean enable) {
        View view = getIconViewById(id);
        if (view != null) {
            view.setEnabled(enable);
        } else {
            Log.e("BbkTitltView", "setIconViewEnable [" + id + "] failed");
        }
    }

    public void setIconViewDrawableRes(int id, int resId) {
        super.setIconViewDrawableRes(id, getBtnBgResId(resId));
    }

    public void setLeftButtonIcon(int drawableId) {
        setIconViewDrawableRes(2, drawableId);
    }

    public void setRightButtonIcon(int drawableId) {
        setIconViewDrawableRes(5, drawableId);
    }

    public void setLeftButtonClickListener(OnClickListener listener) {
        this.mLeftButton.setOnClickListener(listener);
    }

    public void setRightButtonClickListener(OnClickListener listener) {
        this.mRightButton.setOnClickListener(listener);
    }

    public void setIconViewOnClickListner(int id, OnClickListener listener) {
        View view = getIconViewById(id);
        if (view != null) {
            view.setOnClickListener(listener);
        } else {
            Log.w("BbkTitltView", "setIconViewOnClickListener [" + id + "] failed");
        }
    }

    public void setCenterSubViewVisible(boolean visible) {
        if (this.mSubTitleView != null) {
            this.mSubTitleView.setVisibility(visible ? 0 : 8);
        } else {
            Log.e("BbkTitltView", "setCenterSubViewVisible failed");
        }
    }

    public void setOnTitleClickListener(OnClickListener listener) {
        if (listener != null) {
            this.mMainTitleView.setOnClickListener(listener);
        }
    }

    public void setOnTitleClickListener(View view) {
        this.mTitleClickListView = view;
        if (view != null) {
            this.mMainTitleView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if ((BbkTitleView.this.mTitleClickListView instanceof ListView) && BbkTitleView.this.mTitleClickListView.getScrollY() == 0) {
                        ListView listView = (ListView) BbkTitleView.this.mTitleClickListView;
                        listView.smoothScrollBy(0, 0);
                        listView.setSelection(0);
                    } else if (BbkTitleView.this.mTitleClickListView instanceof GridView) {
                        GridView gridView = (GridView) BbkTitleView.this.mTitleClickListView;
                    }
                }
            });
        }
    }

    public void setOnTitleClickListener() {
        this.mMainTitleView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BbkTitleView.this.mContext instanceof VivoBaseActivity) {
                    VivoBaseActivity activity = (VivoBaseActivity) BbkTitleView.this.mContext;
                    if (BbkTitleView.this.mTitleClickListView == null) {
                        return;
                    }
                    if ((BbkTitleView.this.mTitleClickListView instanceof ListView) && BbkTitleView.this.mTitleClickListView.getScrollY() == 0) {
                        ListView listView = (ListView) BbkTitleView.this.mTitleClickListView;
                        listView.smoothScrollBy(0, 0);
                        listView.setSelection(0);
                        return;
                    }
                    boolean z = BbkTitleView.this.mTitleClickListView instanceof GridView;
                }
            }
        });
    }

    private void setButton(int id, CharSequence text, int drawableId, OnClickListener listener) {
        Button view = (Button) getIconViewById(id);
        if (view != null) {
            if (text == null && drawableId < 1 && listener == null) {
                setIconViewVisible(id, false);
                return;
            }
            if (text == null || text.toString().trim().length() == 0) {
                setIconViewDrawableRes(id, drawableId);
            } else {
                setIconViewText(id, text);
            }
            view.setOnClickListener(listener);
            setIconViewVisible(id, true);
        }
    }

    private int getBtnBgResId(int drawableId) {
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.TitleBtnIcon, 50397191, 0);
        int resultBtnBg = 0;
        if (drawableId < 1) {
            return NORMAL_BG_ID;
        }
        if (1 == drawableId) {
            resultBtnBg = a.getResourceId(3, NORMAL_BG_ID);
        } else if (2 == drawableId) {
            resultBtnBg = a.getResourceId(2, NORMAL_BG_ID);
        } else if (3 == drawableId) {
            resultBtnBg = a.getResourceId(0, NORMAL_BG_ID);
        } else if (4 == drawableId) {
            resultBtnBg = a.getResourceId(1, NORMAL_BG_ID);
        }
        if (drawableId >= 16777216) {
            resultBtnBg = drawableId;
        }
        a.recycle();
        return resultBtnBg;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mSubTitleView == null || this.mSubTitleView.getVisibility() != 0) {
            this.mMainTitleView.setGravity(17);
            this.mMainTitleView.setPadding(this.mMainTitleView.getPaddingLeft(), this.mMainTitleView.getPaddingTop(), this.mMainTitleView.getPaddingRight(), 0);
            return;
        }
        this.mMainTitleView.setGravity(81);
        this.mMainTitleView.setPadding(this.mMainTitleView.getPaddingLeft(), this.mMainTitleView.getPaddingTop(), this.mMainTitleView.getPaddingRight(), (this.mRootView.getMeasuredHeight() / 2) + this.MAIN_TITLE_OFFSET);
        this.mSubTitleView.setPadding(this.mSubTitleView.getPaddingLeft(), (this.mRootView.getMeasuredHeight() / 2) + this.SUB_TITLE_OFFSET, this.mSubTitleView.getPaddingRight(), this.mSubTitleView.getPaddingBottom());
    }
}
