package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.vivo.common.animation.CheckableRelativeLayout;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SlipCheckableListItem extends CheckableRelativeLayout {
    private final int PADDING_LEFT;
    private final int PADDING_RIGHT;
    private float mDensity;
    private float mIconHeight;
    private Rect mIconRect;
    iconState mIconState;
    private float mIconWidth;
    private ArrayList<Drawable> mIcons;
    private View mItemView;
    private int mLayoutDirection;
    private float mPositionX;
    private Drawable mPressedIcon;
    private int mPressedId;
    private int mRightPadding;
    private int mTopPadding;

    enum iconState {
        PRESSED,
        CANCLED,
        CLICK,
        RESET
    }

    public SlipCheckableListItem(Context context) {
        this(context, null);
    }

    public SlipCheckableListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlipCheckableListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        this.mIcons = new ArrayList();
        this.mIconWidth = 0.0f;
        this.mIconHeight = 0.0f;
        this.PADDING_LEFT = 10;
        this.PADDING_RIGHT = 5;
        this.mIconRect = new Rect();
        this.mPressedIcon = null;
        this.mTopPadding = 0;
        this.mRightPadding = 0;
        this.mLayoutDirection = 0;
        this.mIconState = iconState.RESET;
        this.mDensity = getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIconHeight > 0.0f && this.mIconWidth > 0.0f) {
            int top = (int) (((((float) getHeight()) - this.mIconHeight) / 2.0f) + ((float) this.mTopPadding));
            int bottom = (int) (((float) top) + this.mIconHeight);
            int size = this.mIcons.size();
            int i;
            Drawable dr;
            int right;
            int left;
            if (Math.abs(this.mPositionX) > this.mIconWidth) {
                canvas.save();
                for (i = 0; i < size; i++) {
                    float refDist;
                    dr = (Drawable) this.mIcons.get(i);
                    if (this.mLayoutDirection == 1) {
                        refDist = this.mPositionX - this.mIconWidth;
                    } else {
                        refDist = (-this.mPositionX) - this.mIconWidth;
                    }
                    float dist = (float) Math.pow((double) refDist, 0.95d);
                    float scale = 0.5f;
                    switch (size) {
                        case 2:
                            scale = (float) ((0.6d / ((double) (size - 1))) * ((double) i));
                            break;
                        case 3:
                            scale = (float) ((0.8d / ((double) (size - 1))) * ((double) i));
                            break;
                        case 4:
                            scale = (float) ((0.9d / ((double) (size - 1))) * ((double) i));
                            break;
                    }
                    int temp = (int) ((1.0f - scale) * dist);
                    if (this.mLayoutDirection == 1) {
                        right = (int) (((this.mIconWidth - (((this.mIconWidth - ((float) this.mRightPadding)) / ((float) size)) * ((float) i))) - 10.0f) + ((float) temp));
                        left = right - dr.getIntrinsicWidth();
                    } else {
                        left = (int) ((((((float) getWidth()) - this.mIconWidth) + (((this.mIconWidth - ((float) this.mRightPadding)) / ((float) size)) * ((float) i))) + 10.0f) - ((float) temp));
                        right = left + dr.getIntrinsicWidth();
                    }
                    this.mIconRect.set(left, top, right, bottom);
                    dr.setBounds(this.mIconRect);
                    dr.draw(canvas);
                }
                canvas.restore();
                return;
            }
            canvas.save();
            for (i = 0; i < size; i++) {
                dr = (Drawable) this.mIcons.get(i);
                if (this.mLayoutDirection == 1) {
                    right = (int) ((this.mPositionX - (((this.mIconWidth - ((float) this.mRightPadding)) / ((float) size)) * ((float) i))) - 10.0f);
                    left = right - dr.getIntrinsicWidth();
                } else {
                    left = (int) (((((float) getWidth()) + this.mPositionX) + (((this.mIconWidth - ((float) this.mRightPadding)) / ((float) size)) * ((float) i))) + 10.0f);
                    right = left + dr.getIntrinsicWidth();
                }
                this.mIconRect.set(left, top, right, bottom);
                dr.setBounds(this.mIconRect);
                dr.draw(canvas);
            }
            canvas.restore();
        }
    }

    public void setPadding(int top, int right) {
        this.mTopPadding = top;
        this.mRightPadding = right;
    }

    public void bindSlipView(View view) {
        this.mItemView = view;
    }

    public View getSlipView() {
        return this.mItemView;
    }

    public void addSideIcon(Drawable icon) {
        this.mIcons.add(icon);
    }

    public void clearSideIcons() {
        this.mIcons.clear();
    }

    int getOprationCount() {
        return this.mIcons.size();
    }

    float getOprationAreaWidth() {
        return this.mIconWidth;
    }

    float initOprationArea(int rtl) {
        int width = 0;
        int size = this.mIcons.size();
        for (int i = 0; i < size; i++) {
            Drawable icon = (Drawable) this.mIcons.get(i);
            width += icon.getIntrinsicWidth();
            this.mIconHeight = Math.max(this.mIconHeight, (float) icon.getIntrinsicHeight());
        }
        width = (int) (((float) width) + (((this.mDensity * 15.0f) * ((float) size)) + ((float) this.mRightPadding)));
        this.mIconWidth = (float) width;
        this.mLayoutDirection = rtl;
        return (float) width;
    }

    void setCurrentPosition(float x) {
        this.mPositionX = x;
    }

    boolean isClickIcon(int x, int y) {
        int right;
        int top;
        int left;
        int bottom;
        Rect iconRect = new Rect();
        if (this.mLayoutDirection == 1) {
            right = (int) this.mIconWidth;
            top = (int) (((((float) getHeight()) - this.mIconHeight) / 2.0f) + ((float) this.mTopPadding));
            left = 0;
            bottom = (int) (((float) top) + this.mIconHeight);
        } else {
            left = (int) (((float) getWidth()) - this.mIconWidth);
            top = (int) (((((float) getHeight()) - this.mIconHeight) / 2.0f) + ((float) this.mTopPadding));
            right = (int) (((float) left) + this.mIconWidth);
            bottom = (int) (((float) top) + this.mIconHeight);
        }
        iconRect.set(left, top, right, bottom);
        if (!iconRect.contains(x, y)) {
            return false;
        }
        int index;
        if (this.mLayoutDirection == 1) {
            index = (this.mIcons.size() - ((int) Math.floor((double) ((((float) (x - this.mRightPadding)) / (this.mIconWidth - ((float) this.mRightPadding))) * ((float) this.mIcons.size()))))) - 1;
        } else {
            index = (int) Math.floor((double) ((((float) (x - left)) / (this.mIconWidth - ((float) this.mRightPadding))) * ((float) this.mIcons.size())));
        }
        if (index >= this.mIcons.size()) {
            return false;
        }
        this.mPressedIcon = (Drawable) this.mIcons.get(index);
        this.mPressedId = index;
        return true;
    }

    boolean checkClickIcon(int x, int y) {
        int right;
        int top;
        int left;
        int bottom;
        Rect iconRect = new Rect();
        if (this.mLayoutDirection == 1) {
            right = (int) this.mIconWidth;
            top = (int) (((((float) getHeight()) - this.mIconHeight) / 2.0f) + ((float) this.mTopPadding));
            left = 0;
            bottom = (int) (((float) top) + this.mIconHeight);
        } else {
            left = (int) (((float) getWidth()) - this.mIconWidth);
            top = (int) (((((float) getHeight()) - this.mIconHeight) / 2.0f) + ((float) this.mTopPadding));
            right = (int) (((float) left) + this.mIconWidth);
            bottom = (int) (((float) top) + this.mIconHeight);
        }
        iconRect.set(left, top, right, bottom);
        if (iconRect.contains(x, y)) {
            int index;
            if (this.mLayoutDirection == 1) {
                index = (this.mIcons.size() - ((int) Math.floor((double) ((((float) (x - this.mRightPadding)) / (this.mIconWidth - ((float) this.mRightPadding))) * ((float) this.mIcons.size()))))) - 1;
            } else {
                index = (int) Math.floor((double) ((((float) (x - left)) / (this.mIconWidth - ((float) this.mRightPadding))) * ((float) this.mIcons.size())));
            }
            if (index < this.mIcons.size() && ((Drawable) this.mIcons.get(index)) == this.mPressedIcon) {
                return true;
            }
        }
        return false;
    }

    void setIconPressState(boolean press) {
        if (this.mPressedIcon != null) {
            if (press) {
                this.mPressedIcon.setState(new int[]{16842919});
            } else {
                this.mPressedIcon.setState(new int[]{-16842919});
            }
        }
        invalidate();
    }

    int getClickIconId() {
        return this.mPressedId;
    }
}
