package android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class NotificationSubHeaderView extends LinearLayout {
    private final boolean Debug;
    private String TAG;
    private final int mChildMinWidth;
    private View mSubHeaderText;
    private View mTextDivider;
    private View mTime;
    private View mTimeDivider;

    public NotificationSubHeaderView(Context context) {
        this(context, null);
    }

    public NotificationSubHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationSubHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationSubHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "NotificationSubHeaderView";
        this.Debug = false;
        this.mChildMinWidth = getResources().getDimensionPixelSize(R.dimen.notification_header_shrink_min_width);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSubHeaderText = findViewById(com.vivo.internal.R.id.subHeaderText);
        this.mTextDivider = findViewById(com.vivo.internal.R.id.sub_header_text_divider);
        this.mTimeDivider = findViewById(com.vivo.internal.R.id.sub_time_divider);
        this.mTime = findViewById(com.vivo.internal.R.id.subHeaderTime);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = MeasureSpec.makeMeasureSpec(givenWidth, Integer.MIN_VALUE);
        int wrapContentHeightSpec = MeasureSpec.makeMeasureSpec(givenHeight, Integer.MIN_VALUE);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        int maxHeight = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                child.measure(ViewGroup.getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), ViewGroup.getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                totalWidth += (lp.leftMargin + lp.rightMargin) + child.getMeasuredWidth();
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
        }
        if (totalWidth > givenWidth) {
            int overFlow = totalWidth - givenWidth;
            int subHeaderWidth = this.mSubHeaderText.getMeasuredWidth();
            if (overFlow > 0 && this.mSubHeaderText.getVisibility() != 8 && subHeaderWidth > this.mChildMinWidth) {
                int newSize = subHeaderWidth - Math.min(subHeaderWidth - this.mChildMinWidth, overFlow);
                this.mSubHeaderText.measure(MeasureSpec.makeMeasureSpec(newSize, Integer.MIN_VALUE), wrapContentHeightSpec);
                overFlow -= subHeaderWidth - newSize;
            }
        }
        setMeasuredDimension(givenWidth, Math.min(givenHeight, maxHeight));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingStart();
        int childCount = getChildCount();
        int ownHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                int childHeight = child.getMeasuredHeight();
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                left += params.getMarginStart();
                int right = left + child.getMeasuredWidth();
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left;
                int layoutRight = right;
                if (getLayoutDirection() == 1) {
                    int ltrLeft = left;
                    layoutLeft = getWidth() - right;
                    layoutRight = getWidth() - left;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
                left = right + params.getMarginEnd();
            }
        }
    }
}
