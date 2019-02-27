package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.FtBuild;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.vivo.internal.R;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class LinearView extends LinearLayout {
    private static final int FONT_REDUCE_SIZE = 2;
    private final boolean DEBUG;
    public final int ITEM_INDEX_END;
    private final int MAX_ITEM_COUNT;
    private final String TAG;
    private boolean mAutoPadding;
    protected Context mContext;
    private int mDefaultItemSpace;
    private int mDefaultPaddingEnd;
    private int mDefaultPaddingStart;
    private Drawable mDividerDrawable;
    private int mDividerHeight;
    private int mDividerWidth;
    private List<LinearViewItemInfo> mItemInfo;
    private int mItemLayout;
    private int[] mItemSpace;
    private Drawable mItemTextBackground;
    private ColorStateList mItemTextColor;
    private List<View> mLineInfo;
    private int[] mPaddingEnd;
    private int[] mPaddingStart;

    private class LinearViewItemInfo {
        boolean isReduce;
        boolean isText;
        float textSize;
        Button view;

        LinearViewItemInfo(Button v, boolean t, boolean r, float s) {
            this.view = v;
            this.isText = t;
            this.isReduce = r;
            this.textSize = s;
        }
    }

    public LinearView(Context context) {
        this(context, null);
    }

    public LinearView(Context context, AttributeSet attr) {
        super(context, attr, 50397226);
        this.TAG = "LinearView";
        this.DEBUG = false;
        this.MAX_ITEM_COUNT = 4;
        this.ITEM_INDEX_END = -1;
        this.mContext = null;
        this.mItemInfo = new ArrayList();
        this.mLineInfo = new ArrayList();
        this.mItemLayout = 0;
        this.mAutoPadding = true;
        this.mItemSpace = null;
        this.mPaddingStart = null;
        this.mPaddingEnd = null;
        this.mDefaultItemSpace = 0;
        this.mDefaultPaddingStart = 0;
        this.mDefaultPaddingEnd = 0;
        this.mItemTextBackground = null;
        this.mItemTextColor = null;
        this.mDividerDrawable = null;
        this.mDividerWidth = 2;
        this.mDividerHeight = 2;
        this.mContext = context;
        TypedArray typeArray = this.mContext.obtainStyledAttributes(attr, R.styleable.LinearView, 50397226, 0);
        setOrientation(0);
        setBaselineAligned(false);
        this.mDividerDrawable = typeArray.getDrawable(6);
        this.mDividerWidth = (int) typeArray.getDimension(7, (float) this.mDividerWidth);
        this.mDividerHeight = (int) typeArray.getDimension(8, (float) this.mDividerHeight);
        this.mItemLayout = typeArray.getResourceId(2, 0);
        this.mItemSpace = this.mContext.getResources().getIntArray(typeArray.getResourceId(3, 0));
        this.mPaddingStart = this.mContext.getResources().getIntArray(typeArray.getResourceId(0, 0));
        this.mPaddingEnd = this.mContext.getResources().getIntArray(typeArray.getResourceId(1, 0));
        if (this.mItemSpace == null || this.mItemSpace.length == 0 || this.mPaddingStart == null || this.mPaddingStart.length == 0 || this.mPaddingEnd == null || this.mPaddingEnd.length == 0) {
            throw new RuntimeException("ItemSpace or Padding Array Illegal");
        }
        this.mDefaultItemSpace = this.mItemSpace[0];
        this.mDefaultPaddingStart = this.mPaddingStart[0];
        this.mDefaultPaddingEnd = this.mPaddingEnd[0];
        typeArray.recycle();
    }

    public Button getCurrentItem(int order) {
        return ((LinearViewItemInfo) this.mItemInfo.get(translateOrderIfNeed(order))).view;
    }

    public int getMaxItemCount() {
        return 4;
    }

    public void setAutoPadding(boolean enable) {
        this.mAutoPadding = enable;
        if (this.mAutoPadding) {
            adjustItemSpace();
        }
    }

    public int getCurrentItemCount() {
        return this.mItemInfo.size();
    }

    public void addDrawable(Drawable drawable) {
        addDrawable(drawable, -1);
    }

    public void addText(String text) {
        addText(text, -1);
    }

    public void setItemSpace(int[] space) {
        if (space == null || space.length < 1) {
            throw new NullPointerException("setItemSpace receive null pointer or empty array");
        }
        this.mItemSpace = space;
        this.mDefaultItemSpace = this.mItemSpace[0];
        adjustItemSpace();
    }

    public void addDrawable(Drawable drawable, int order) {
        Button view = (Button) LayoutInflater.from(this.mContext).inflate(this.mItemLayout, null);
        view.setText(null);
        view.setBackground(drawable);
        LayoutParams lParams = new LayoutParams(-2, -2);
        lParams.gravity = 17;
        addItem(view, lParams, order, new LinearViewItemInfo(view, false, false, 0.0f));
    }

    public void addText(String text, int order) {
        Button view = (Button) LayoutInflater.from(this.mContext).inflate(this.mItemLayout, null);
        TypedArray typeArray = this.mContext.obtainStyledAttributes(null, R.styleable.LinearView, 50397226, 0);
        view.setText(text);
        if (!isROM3_0()) {
            view.setBackground(typeArray.getDrawable(4));
            view.setTextColor(typeArray.getColorStateList(5));
        }
        typeArray.recycle();
        LayoutParams lParams = new LayoutParams(0, -2);
        lParams.gravity = 17;
        lParams.weight = 1.0f;
        addItem(view, lParams, order, new LinearViewItemInfo(view, true, false, view.getTextSize()));
    }

    public int addItem(Button view, LayoutParams lParams, int order, LinearViewItemInfo info) {
        if (this.mItemInfo.size() >= 4) {
            throw new RuntimeException("Out of max items[4] : " + (this.mItemInfo.size() + 1));
        } else if (order > this.mItemInfo.size() || (order < 0 && order != -1)) {
            throw new RuntimeException("Out Of MakupView Order : " + order);
        } else {
            int position;
            if (order == -1) {
                position = this.mItemInfo.size();
            } else {
                position = order;
            }
            View line = createLineView();
            LayoutParams lp = new LayoutParams(this.mDividerWidth, this.mDividerHeight);
            lp.gravity = 16;
            if (position == 0) {
                addView(view, indexOfItem(position), lParams);
                if (this.mItemInfo.size() >= 1) {
                    addView(line, indexOfLine(position), lp);
                    this.mLineInfo.add(position, line);
                }
            } else {
                addView(line, indexOfLine(position), lp);
                addView(view, indexOfItem(position), lParams);
                this.mLineInfo.add(position - 1, line);
            }
            this.mItemInfo.add(position, info);
            adjustItemSpace();
            return position;
        }
    }

    public void removeItem(int order) {
        int position = translateOrderIfNeed(order);
        if (this.mLineInfo.size() <= 0) {
            removeViewAt(position);
        } else if (position == 0) {
            this.mLineInfo.remove(position);
            removeViewAt(position + 1);
            removeViewAt(position);
        } else {
            this.mLineInfo.remove(position - 1);
            removeViewAt(indexOfItem(position));
            removeViewAt(indexOfLine(position));
        }
        this.mItemInfo.remove(position);
        adjustItemSpace();
    }

    private boolean isROM3_0() {
        return FtBuild.getRomVersion() >= 3.0f;
    }

    private View createLineView() {
        View view = new View(this.mContext);
        view.setBackground(this.mDividerDrawable);
        return view;
    }

    private int indexOfLine(int order) {
        if (order == 0) {
            return 1;
        }
        return Math.max(0, order - 1) + order;
    }

    private int indexOfItem(int order) {
        return order * 2;
    }

    private void adjustItemSpace() {
        if (this.mItemInfo.size() >= 1) {
            int index = this.mItemInfo.size() - 1;
            int space = this.mItemSpace.length + -1 >= index ? this.mItemSpace[index] : this.mDefaultItemSpace;
            if (this.mAutoPadding) {
                setPaddingRelative(this.mPaddingStart.length + -1 >= index ? this.mPaddingStart[index] : this.mDefaultPaddingStart, getPaddingTop(), this.mPaddingEnd.length + -1 >= index ? this.mPaddingEnd[index] : this.mDefaultPaddingEnd, getPaddingBottom());
            }
            int margin = space > this.mDividerWidth ? (space - this.mDividerWidth) / 2 : 0;
            for (View line : this.mLineInfo) {
                LayoutParams lp = (LayoutParams) line.getLayoutParams();
                lp.rightMargin = margin;
                lp.leftMargin = margin;
            }
        }
    }

    public void setOnClickListener(OnClickListener listener, int order) {
        ((LinearViewItemInfo) this.mItemInfo.get(translateOrderIfNeed(order))).view.setOnClickListener(listener);
    }

    public void setDrawable(Drawable drawable, int order) {
        LinearViewItemInfo itemInfo = (LinearViewItemInfo) this.mItemInfo.get(translateOrderIfNeed(order));
        itemInfo.view.setText(null);
        itemInfo.view.setBackground(drawable);
        itemInfo.isText = false;
    }

    public void setText(String text, int order) {
        LinearViewItemInfo itemInfo = (LinearViewItemInfo) this.mItemInfo.get(translateOrderIfNeed(order));
        if (!itemInfo.isText) {
            itemInfo.view.setBackground(this.mItemTextBackground);
            itemInfo.view.setTextColor(this.mItemTextColor);
        }
        itemInfo.isText = true;
        itemInfo.view.setText(text);
    }

    private int translateOrderIfNeed(int order) {
        int position;
        if (order == -1) {
            position = this.mItemInfo.size() - 1;
        } else {
            position = order;
        }
        if (position >= 0 && position < this.mItemInfo.size()) {
            return position;
        }
        throw new RuntimeException("Out Of MakupView Order : " + order);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        adjustItemSpace();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxHeight = 0;
        for (LinearViewItemInfo itemInfo : this.mItemInfo) {
            if (itemInfo.view.getVisibility() != 8 && maxHeight < itemInfo.view.getMeasuredHeight()) {
                maxHeight = itemInfo.view.getMeasuredHeight();
            }
        }
        for (LinearViewItemInfo itemInfo2 : this.mItemInfo) {
            itemInfo2.view.measure(MeasureSpec.makeMeasureSpec(itemInfo2.view.getMeasuredWidth(), widthMode), MeasureSpec.makeMeasureSpec(maxHeight, 1073741824));
        }
    }

    private int getTextLength(Button view) {
        return (int) ((view.getPaint().measureText(view.getText().toString()) + ((float) view.getPaddingLeft())) + ((float) view.getPaddingRight()));
    }

    private boolean tryReduceFont(Button textView, int availableLength, float originTextSize) {
        boolean retval = false;
        Paint paint = textView.getPaint();
        String textStr = textView.getText().toString();
        if (textStr == null) {
            return false;
        }
        textView.setTextSize(0, originTextSize - TypedValue.applyDimension(1, 2.0f, this.mContext.getResources().getDisplayMetrics()));
        if (((int) ((paint.measureText(textStr) + ((float) textView.getPaddingLeft())) + ((float) textView.getPaddingRight()))) + (textStr.length() > 0 ? (int) paint.measureText(textStr.substring(textStr.length() - 1)) : 0) >= availableLength) {
            textView.setTextSize(0, originTextSize);
        } else {
            retval = true;
        }
        return retval;
    }
}
