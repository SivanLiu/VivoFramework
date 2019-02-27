package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.util.Preconditions;
import com.vivo.internal.R;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public final class FloatingToolbar {
    public static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final OnMenuItemClickListener NO_OP_MENUITEM_CLICK_LISTENER = -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.$INST$0;
    private static int TBOFP_RADIUS = R.dimen.vigour_floating_toolbar_overflowpanel_radius;
    private static int TOOLBAR_CONTAINER_MARGIN = R.dimen.vigour_text_edit_floating_toolbar_margin;
    private static int TOOLBAR_LEFT_BG = R.drawable.vigour_text_toolbar_left_light;
    private static int TOOLBAR_MIDDLE_BG = R.drawable.vigour_text_toolbar_center_light;
    private static int TOOLBAR_OVERFLOW_BTN_ANIM_TOOVERFLOW = R.drawable.vigour_ft_avd_tooverflow_animation;
    private static int TOOLBAR_OVERFLOW_BTN_LAYOUT = R.layout.vigour_floating_popup_overflow_button;
    private static int TOOLBAR_OVERFLOW_BTN_TOARROW = R.drawable.vigour_ft_avd_toarrow;
    private static int TOOLBAR_OVERFLOW_BTN_TOARROW_ANIM = R.drawable.vigour_ft_avd_toarrow_animation;
    private static int TOOLBAR_OVERFLOW_BTN_TOOVERFLOW = R.drawable.vigour_ft_avd_tooverflow;
    private static int TOOLBAR_OVERFLOW_ITEM_HEIGHT = R.dimen.popup_toolbar_item_height;
    private static int TOOLBAR_OVERFLOW_ITEM_LAYOUT = R.layout.vigour_floating_popup_overflow_list_item;
    private static int TOOLBAR_RIGHT_BG = R.drawable.vigour_text_toolbar_right_light;
    private static int TOOLBAR_WHOLE_BG = R.drawable.vigour_text_toolbar_single_light;
    private final Rect mContentRect = new Rect();
    private final Context mContext;
    private Menu mMenu;
    private OnMenuItemClickListener mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
    private final OnLayoutChangeListener mOrientationChangeHandler = new OnLayoutChangeListener() {
        private final Rect mNewRect = new Rect();
        private final Rect mOldRect = new Rect();

        public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
            this.mNewRect.set(newLeft, newRight, newTop, newBottom);
            this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
            if (FloatingToolbar.this.mPopup.isShowing() && (this.mNewRect.equals(this.mOldRect) ^ 1) != 0) {
                FloatingToolbar.this.mWidthChanged = true;
                FloatingToolbar.this.updateLayout();
            }
        }
    };
    private final FloatingToolbarPopup mPopup;
    private final Rect mPreviousContentRect = new Rect();
    private List<MenuItem> mShowingMenuItems = new ArrayList();
    private int mSuggestedWidth;
    private boolean mWidthChanged = true;
    private final Window mWindow;

    private static class ContentContainerViewGroup extends RelativeLayout {
        private Paint mPaint = new Paint(1);

        public ContentContainerViewGroup(Context context) {
            super(context);
        }

        public ContentContainerViewGroup(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ContentContainerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public ContentContainerViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            drawCircle(canvas);
        }

        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            drawCircle(canvas);
        }

        private void drawCircle(Canvas canvas) {
            int mRoundCornerRadius = (int) this.mContext.getResources().getDimension(FloatingToolbar.TBOFP_RADIUS);
            canvas.save();
            Path path = new Path();
            path.addRoundRect(new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight()), (float) mRoundCornerRadius, (float) mRoundCornerRadius, Direction.CW);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawPath(path, this.mPaint);
            canvas.restore();
        }
    }

    private static final class FloatingToolbarPopup {
        private static final int MAX_OVERFLOW_SIZE = 4;
        private static final int MIN_OVERFLOW_SIZE = 2;
        private final Drawable mArrow;
        private final AnimationSet mCloseOverflowAnimation;
        private final ViewGroup mContentContainer;
        private final Context mContext;
        private final Point mCoordsOnWindow = new Point();
        private final AnimatorSet mDismissAnimation;
        private boolean mDismissed = true;
        private final Interpolator mFastOutLinearInInterpolator;
        private final Interpolator mFastOutSlowInInterpolator;
        private boolean mHidden;
        private final AnimatorSet mHideAnimation;
        private final int mIconTextSpacing;
        private final OnComputeInternalInsetsListener mInsetsComputer = new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass1(this);
        private boolean mIsOverflowOpen;
        private final int mLineHeight;
        private final Interpolator mLinearOutSlowInInterpolator;
        private final Interpolator mLogAccelerateInterpolator;
        private final FtMainPanelLayout mMainPanel;
        private Size mMainPanelSize;
        private final int mMarginHorizontal;
        private final int mMarginVertical;
        private final OnClickListener mMenuItemButtonOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if ((v.getTag() instanceof MenuItem) && FloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                    FloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
                }
            }
        };
        private OnMenuItemClickListener mOnMenuItemClickListener;
        private final AnimationSet mOpenOverflowAnimation;
        private boolean mOpenOverflowUpwards;
        private final Drawable mOverflow;
        private final AnimationListener mOverflowAnimationListener;
        private final ImageButton mOverflowButton;
        private final Size mOverflowButtonSize;
        private final OverflowPanel mOverflowPanel;
        private Size mOverflowPanelSize;
        private final OverflowPanelViewHelper mOverflowPanelViewHelper;
        private final View mParent;
        private final PopupWindow mPopupWindow;
        private final Runnable mPreparePopupContentRTLHelper = new Runnable() {
            public void run() {
                FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                FloatingToolbarPopup.this.mContentContainer.setAlpha(1.0f);
            }
        };
        private final AnimatorSet mShowAnimation;
        private final int[] mTmpCoords = new int[2];
        private final AnimatedVectorDrawable mToArrow;
        private final AnimatedVectorDrawable mToOverflow;
        private final Region mTouchableRegion = new Region();
        private int mTransitionDurationScale;
        private final Rect mViewPortOnScreen = new Rect();

        private static final class LogAccelerateInterpolator implements Interpolator {
            private static final int BASE = 100;
            private static final float LOGS_SCALE = (1.0f / computeLog(1.0f, 100));

            /* synthetic */ LogAccelerateInterpolator(LogAccelerateInterpolator -this0) {
                this();
            }

            private LogAccelerateInterpolator() {
            }

            private static float computeLog(float t, int base) {
                return (float) (1.0d - Math.pow((double) base, (double) (-t)));
            }

            public float getInterpolation(float t) {
                return 1.0f - (computeLog(1.0f - t, 100) * LOGS_SCALE);
            }
        }

        private static final class OverflowPanel extends ListView {
            private final FloatingToolbarPopup mPopup;

            OverflowPanel(FloatingToolbarPopup popup) {
                super(((FloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
                this.mPopup = popup;
                setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
                setScrollIndicators(3);
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), 1073741824));
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (this.mPopup.isOverflowAnimating()) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            protected boolean awakenScrollBars() {
                return super.awakenScrollBars();
            }
        }

        private static final class OverflowPanelViewHelper {
            private final View mCalculator = createMenuButton(null);
            private final Context mContext;
            private final int mIconTextSpacing;
            private final int mSidePadding;

            public OverflowPanelViewHelper(Context context) {
                this.mContext = (Context) Preconditions.checkNotNull(context);
                this.mIconTextSpacing = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_menu_button_side_padding);
                this.mSidePadding = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_overflow_side_padding);
            }

            public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
                Preconditions.checkNotNull(menuItem);
                if (convertView != null) {
                    FloatingToolbar.updateMenuItemButton(convertView, menuItem, this.mIconTextSpacing);
                } else {
                    convertView = createMenuButton(menuItem);
                }
                convertView.setMinimumWidth(minimumWidth);
                return convertView;
            }

            public int calculateWidth(MenuItem menuItem) {
                FloatingToolbar.updateMenuItemButton(this.mCalculator, menuItem, this.mIconTextSpacing);
                this.mCalculator.measure(0, 0);
                return this.mCalculator.getMeasuredWidth();
            }

            private View createMenuButton(MenuItem menuItem) {
                View button = FloatingToolbar.createMenuItemButton(this.mContext, menuItem, this.mIconTextSpacing);
                button.setBackgroundResource(FloatingToolbar.TOOLBAR_MIDDLE_BG);
                button.setPadding(this.mSidePadding, 0, this.mSidePadding, 0);
                return button;
            }
        }

        /* renamed from: lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_18794 */
        /* synthetic */ void m55xf8980449(InternalInsetsInfo info) {
            info.contentInsets.setEmpty();
            info.visibleInsets.setEmpty();
            info.touchableRegion.set(this.mTouchableRegion);
            info.setTouchableInsets(3);
        }

        public FloatingToolbarPopup(Context context, View parent) {
            this.mParent = (View) Preconditions.checkNotNull(parent);
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mContentContainer = FloatingToolbar.createContentContainer(context);
            this.mContentContainer.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopupWindow = FloatingToolbar.createPopupWindow(this.mContentContainer);
            this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_horizontal_margin);
            this.mMarginVertical = parent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_vertical_margin);
            this.mLineHeight = context.getResources().getDimensionPixelSize(FloatingToolbar.TOOLBAR_OVERFLOW_ITEM_HEIGHT);
            this.mIconTextSpacing = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_menu_button_side_padding);
            this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, com.android.internal.R.interpolator.fast_out_slow_in);
            this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, com.android.internal.R.interpolator.linear_out_slow_in);
            this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, com.android.internal.R.interpolator.fast_out_linear_in);
            this.mArrow = this.mContext.getResources().getDrawable(FloatingToolbar.TOOLBAR_OVERFLOW_BTN_TOOVERFLOW, this.mContext.getTheme());
            this.mArrow.setAutoMirrored(true);
            this.mOverflow = this.mContext.getResources().getDrawable(FloatingToolbar.TOOLBAR_OVERFLOW_BTN_TOARROW, null);
            this.mOverflow.setAutoMirrored(true);
            this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(FloatingToolbar.TOOLBAR_OVERFLOW_BTN_TOARROW_ANIM, this.mContext.getTheme());
            this.mToArrow.setAutoMirrored(true);
            this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(FloatingToolbar.TOOLBAR_OVERFLOW_BTN_ANIM_TOOVERFLOW, this.mContext.getTheme());
            this.mToOverflow.setAutoMirrored(true);
            this.mOverflowButton = createOverflowButton();
            this.mOverflowButtonSize = new Size(measure(this.mOverflowButton).getWidth(), (int) this.mContext.getResources().getDimension(FloatingToolbar.TOOLBAR_OVERFLOW_ITEM_HEIGHT));
            this.mMainPanel = (FtMainPanelLayout) createMainPanel();
            this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext);
            this.mOverflowPanel = createOverflowPanel();
            this.mOverflowAnimationListener = createOverflowAnimationListener();
            this.mOpenOverflowAnimation = new AnimationSet(true);
            this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mCloseOverflowAnimation = new AnimationSet(true);
            this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mShowAnimation = FloatingToolbar.createEnterAnimation(this.mContentContainer);
            this.mDismissAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 150, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                    FloatingToolbarPopup.this.mContentContainer.removeAllViews();
                }
            });
            this.mHideAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                }
            });
        }

        public void layoutMenuItems(List<MenuItem> menuItems, OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
            this.mOnMenuItemClickListener = menuItemClickListener;
            cancelOverflowAnimations();
            clearPanels();
            menuItems = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
            if (!menuItems.isEmpty()) {
                layoutOverflowPanelItems(menuItems);
            }
            updatePopupSize();
        }

        public void show(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (!isShowing()) {
                this.mHidden = false;
                this.mDismissed = false;
                cancelDismissAndHideAnimations();
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.showAtLocation(this.mParent, 0, this.mCoordsOnWindow.x, this.mCoordsOnWindow.y);
                setTouchableSurfaceInsetsComputer();
            }
        }

        public void dismiss() {
            if (!this.mDismissed) {
                this.mHidden = false;
                this.mDismissed = true;
                this.mHideAnimation.cancel();
                this.mPopupWindow.dismiss();
                setZeroTouchableSurface();
            }
        }

        public void hide() {
            if (isShowing()) {
                this.mHidden = true;
                this.mPopupWindow.dismiss();
                setZeroTouchableSurface();
            }
        }

        public boolean isShowing() {
            return !this.mDismissed ? this.mHidden ^ 1 : false;
        }

        public boolean isHidden() {
            return this.mHidden;
        }

        public void updateCoordinates(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (isShowing() && (this.mPopupWindow.isShowing() ^ 1) == 0) {
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.update(this.mCoordsOnWindow.x, this.mCoordsOnWindow.y, this.mPopupWindow.getWidth(), this.mPopupWindow.getHeight());
            }
        }

        private void refreshCoordinatesAndOverflowDirection(Rect contentRectOnScreen) {
            int y;
            refreshViewPort();
            int x = Math.min(contentRectOnScreen.centerX() - (this.mPopupWindow.getWidth() / 2), this.mViewPortOnScreen.right - this.mPopupWindow.getWidth());
            int availableHeightAboveContent = contentRectOnScreen.top - this.mViewPortOnScreen.top;
            int availableHeightBelowContent = this.mViewPortOnScreen.bottom - contentRectOnScreen.bottom;
            int margin = this.mMarginVertical * 2;
            int toolbarHeightWithVerticalMargin = this.mLineHeight + margin;
            boolean isAbove = true;
            if (hasOverflow()) {
                int minimumOverflowHeightWithMargin = calculateOverflowHeight(2) + margin;
                int availableHeightThroughContentDown = (this.mViewPortOnScreen.bottom - contentRectOnScreen.top) + toolbarHeightWithVerticalMargin;
                int availableHeightThroughContentUp = (contentRectOnScreen.bottom - this.mViewPortOnScreen.top) + toolbarHeightWithVerticalMargin;
                if (availableHeightAboveContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightAboveContent - margin);
                    y = contentRectOnScreen.top - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin && availableHeightThroughContentDown >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightThroughContentDown - margin);
                    y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightBelowContent - margin);
                    y = contentRectOnScreen.bottom;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent < toolbarHeightWithVerticalMargin || this.mViewPortOnScreen.height() < minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(this.mViewPortOnScreen.height() - margin);
                    y = this.mViewPortOnScreen.top;
                    this.mOpenOverflowUpwards = false;
                } else {
                    updateOverflowHeight(availableHeightThroughContentUp - margin);
                    y = (contentRectOnScreen.bottom + toolbarHeightWithVerticalMargin) - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                }
            } else {
                toolbarHeightWithVerticalMargin = this.mMainPanel.getMeasuredHeight() + margin;
                if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
                    y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                    isAbove = true;
                } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
                    y = contentRectOnScreen.bottom;
                    isAbove = false;
                } else if (availableHeightBelowContent >= this.mLineHeight) {
                    y = contentRectOnScreen.bottom - this.mMarginVertical;
                    isAbove = false;
                } else {
                    y = Math.max(this.mViewPortOnScreen.top, contentRectOnScreen.top - toolbarHeightWithVerticalMargin);
                    isAbove = true;
                }
            }
            this.mParent.getRootView().getLocationOnScreen(this.mTmpCoords);
            int rootViewLeftOnScreen = this.mTmpCoords[0];
            int rootViewTopOnScreen = this.mTmpCoords[1];
            this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
            int windowLeftOnScreen = rootViewLeftOnScreen - this.mTmpCoords[0];
            int windowTopOnScreen = rootViewTopOnScreen - this.mTmpCoords[1];
            this.mMainPanel.updateFloatingArrow(isAbove ^ 1, (contentRectOnScreen.right + contentRectOnScreen.left) / 2);
            this.mCoordsOnWindow.set(Math.max(0, x - windowLeftOnScreen), Math.max(0, y - windowTopOnScreen));
        }

        private void runShowAnimation() {
            this.mShowAnimation.start();
        }

        private void runDismissAnimation() {
            this.mDismissAnimation.start();
        }

        private void runHideAnimation() {
            this.mHideAnimation.start();
        }

        private void cancelDismissAndHideAnimations() {
            this.mDismissAnimation.cancel();
            this.mHideAnimation.cancel();
        }

        private void cancelOverflowAnimations() {
            this.mContentContainer.clearAnimation();
            this.mMainPanel.animate().cancel();
            this.mOverflowPanel.animate().cancel();
            this.mToArrow.stop();
            this.mToOverflow.stop();
        }

        private void openOverflow() {
            float overflowButtonTargetX;
            final int targetWidth = this.mOverflowPanelSize.getWidth();
            final int targetHeight = this.mOverflowPanelSize.getHeight();
            final int startWidth = this.mContentContainer.getWidth();
            final int startHeight = this.mContentContainer.getHeight();
            final float startY = this.mContentContainer.getY();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        FloatingToolbarPopup.this.mContentContainer.setX(left);
                        FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                        FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                        return;
                    }
                    FloatingToolbarPopup.this.mContentContainer.setX(right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                    FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth));
                    FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - targetWidth));
                }
            };
            Animation heightAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, startHeight + ((int) (((float) (targetHeight - startHeight)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                        FloatingToolbarPopup.this.mContentContainer.setY(startY - ((float) (FloatingToolbarPopup.this.mContentContainer.getHeight() - startHeight)));
                        FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                    }
                }
            };
            final float overflowButtonStartX = this.mOverflowButton.getX();
            setWidth(this.mOverflowButton, this.mOverflowPanelSize.getWidth());
            if (isInRTLMode()) {
                overflowButtonTargetX = overflowButtonStartX;
            } else {
                overflowButtonTargetX = (overflowButtonStartX - ((float) targetWidth)) + ((float) this.mOverflowButton.getWidth());
            }
            Animation overflowButtonAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    int i;
                    float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        i = 0;
                    } else {
                        i = FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth;
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
                }
            };
            widthAnimation.setInterpolator(this.mLogAccelerateInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(250));
            heightAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            heightAnimation.setDuration((long) getAdjustedDuration(250));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.addAnimation(widthAnimation);
            this.mOpenOverflowAnimation.addAnimation(heightAnimation);
            this.mOpenOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mOpenOverflowAnimation);
            this.mIsOverflowOpen = true;
            this.mMainPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(250).start();
            this.mOverflowPanel.setAlpha(1.0f);
        }

        private void closeOverflow() {
            float overflowButtonTargetX;
            final int targetWidth = this.mMainPanelSize.getWidth();
            final int startWidth = this.mContentContainer.getWidth();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        FloatingToolbarPopup.this.mContentContainer.setX(left);
                        FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                        FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                        return;
                    }
                    FloatingToolbarPopup.this.mContentContainer.setX(right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                    FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - targetWidth));
                    FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth));
                }
            };
            int targetHeight = this.mMainPanelSize.getHeight();
            final int i = targetHeight;
            final int height = this.mContentContainer.getHeight();
            final float y = this.mContentContainer.getY() + ((float) this.mContentContainer.getHeight());
            Animation anonymousClass9 = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, height + ((int) (((float) (i - height)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                        FloatingToolbarPopup.this.mContentContainer.setY(y - ((float) FloatingToolbarPopup.this.mContentContainer.getHeight()));
                        FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                    }
                }
            };
            setWidth(this.mOverflowButton, this.mOverflowButtonSize.getWidth());
            final int deltaOverflowButtonWidth = this.mOverflowPanelSize.getWidth() - this.mOverflowButtonSize.getWidth();
            final float overflowButtonStartX = this.mOverflowButton.getX();
            if (isInRTLMode()) {
                overflowButtonTargetX = (overflowButtonStartX - ((float) startWidth)) + ((float) this.mOverflowButton.getWidth());
            } else {
                overflowButtonTargetX = (((float) startWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
            }
            final int i2 = startWidth;
            Animation overflowButtonAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    int i;
                    float actualOverflowButtonX;
                    float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        i = 0;
                    } else {
                        i = FloatingToolbarPopup.this.mContentContainer.getWidth() - i2;
                    }
                    float deltaContainerWidth = (float) i;
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        actualOverflowButtonX = overflowButtonX + deltaContainerWidth;
                    } else {
                        actualOverflowButtonX = (overflowButtonX + deltaContainerWidth) + (((float) deltaOverflowButtonWidth) * interpolatedTime);
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(actualOverflowButtonX);
                }
            };
            widthAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(250));
            anonymousClass9.setInterpolator(this.mLogAccelerateInterpolator);
            anonymousClass9.setDuration((long) getAdjustedDuration(250));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
            this.mCloseOverflowAnimation.getAnimations().clear();
            this.mCloseOverflowAnimation.addAnimation(widthAnimation);
            this.mCloseOverflowAnimation.addAnimation(anonymousClass9);
            this.mCloseOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mCloseOverflowAnimation);
            this.mIsOverflowOpen = false;
            this.mMainPanel.animate().alpha(1.0f).withLayer().setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).start();
            this.mOverflowPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(150).start();
        }

        private void setPanelsStatesAtRestingPosition() {
            this.mOverflowButton.setEnabled(true);
            this.mOverflowPanel.awakenScrollBars();
            Size containerSize;
            if (this.mIsOverflowOpen) {
                containerSize = this.mOverflowPanelSize;
                setSize(this.mContentContainer, containerSize);
                this.mMainPanel.setAlpha(0.0f);
                this.mMainPanel.setVisibility(4);
                this.mOverflowPanel.setAlpha(1.0f);
                this.mOverflowPanel.setVisibility(0);
                this.mOverflowButton.setImageDrawable(this.mArrow);
                this.mOverflowButton.setContentDescription(this.mContext.getString(com.android.internal.R.string.floating_toolbar_close_overflow_description));
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(-this.mContentContainer.getX());
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) this.mMarginVertical);
                    this.mMainPanel.setY((float) (containerSize.getHeight() - this.mContentContainer.getHeight()));
                    this.mOverflowButton.setY((float) (containerSize.getHeight() - this.mOverflowButtonSize.getHeight()));
                    this.mOverflowPanel.setY(0.0f);
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            containerSize = this.mMainPanelSize;
            setSize(this.mContentContainer, containerSize);
            this.mMainPanel.setAlpha(1.0f);
            this.mMainPanel.setVisibility(0);
            this.mOverflowPanel.setAlpha(0.0f);
            this.mOverflowPanel.setVisibility(4);
            this.mOverflowButton.setImageDrawable(this.mOverflow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(com.android.internal.R.string.floating_toolbar_open_overflow_description));
            if (hasOverflow()) {
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX((float) (containerSize.getWidth() - this.mOverflowPanelSize.getWidth()));
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) ((this.mMarginVertical + this.mOverflowPanelSize.getHeight()) - containerSize.getHeight()));
                    this.mMainPanel.setY(0.0f);
                    this.mOverflowButton.setY(0.0f);
                    this.mOverflowPanel.setY((float) (containerSize.getHeight() - this.mOverflowPanelSize.getHeight()));
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            this.mContentContainer.setX((float) this.mMarginHorizontal);
            this.mContentContainer.setY((float) this.mMarginVertical);
            this.mMainPanel.setX(0.0f);
            this.mMainPanel.setY(0.0f);
        }

        private void updateOverflowHeight(int suggestedHeight) {
            if (hasOverflow()) {
                int newHeight = calculateOverflowHeight((suggestedHeight - this.mOverflowButtonSize.getHeight()) / this.mLineHeight);
                if (this.mOverflowPanelSize.getHeight() != newHeight) {
                    this.mOverflowPanelSize = new Size(this.mOverflowPanelSize.getWidth(), newHeight);
                }
                setSize(this.mOverflowPanel, this.mOverflowPanelSize);
                if (this.mIsOverflowOpen) {
                    setSize(this.mContentContainer, this.mOverflowPanelSize);
                    if (this.mOpenOverflowUpwards) {
                        int deltaHeight = this.mOverflowPanelSize.getHeight() - newHeight;
                        this.mContentContainer.setY(this.mContentContainer.getY() + ((float) deltaHeight));
                        this.mOverflowButton.setY(this.mOverflowButton.getY() - ((float) deltaHeight));
                    }
                } else {
                    setSize(this.mContentContainer, this.mMainPanelSize);
                }
                updatePopupSize();
            }
        }

        private void updatePopupSize() {
            int width = 0;
            int height = 0;
            if (this.mMainPanelSize != null) {
                width = Math.max(0, this.mMainPanelSize.getWidth());
                height = Math.max(0, this.mMainPanelSize.getHeight());
            }
            if (this.mOverflowPanelSize != null) {
                width = Math.max(width, this.mOverflowPanelSize.getWidth());
                height = Math.max(height, this.mOverflowPanelSize.getHeight());
            }
            this.mPopupWindow.setWidth((this.mMarginHorizontal * 2) + width);
            this.mPopupWindow.setHeight((this.mMarginVertical * 2) + height);
            maybeComputeTransitionDurationScale();
        }

        private void refreshViewPort() {
            ViewRootImpl viewRootImpl = this.mParent.getViewRootImpl();
            if (viewRootImpl == null || (viewRootImpl.getWindowFlags() & 512) != 512) {
                this.mParent.getWindowVisibleDisplayFrame(this.mViewPortOnScreen);
            } else {
                this.mParent.getLocalVisibleRect(this.mViewPortOnScreen);
            }
        }

        private int getAdjustedToolbarWidth(int suggestedWidth) {
            int width = suggestedWidth;
            refreshViewPort();
            int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_horizontal_margin) * 2);
            if (suggestedWidth <= 0) {
                width = this.mParent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_preferred_width);
            }
            return Math.min(width, maximumWidth);
        }

        private void setZeroTouchableSurface() {
            this.mTouchableRegion.setEmpty();
        }

        private void setContentAreaAsTouchableSurface() {
            int width;
            int height;
            Preconditions.checkNotNull(this.mMainPanelSize);
            if (this.mIsOverflowOpen) {
                Preconditions.checkNotNull(this.mOverflowPanelSize);
                width = this.mOverflowPanelSize.getWidth();
                height = this.mOverflowPanelSize.getHeight();
            } else {
                width = this.mMainPanelSize.getWidth();
                height = this.mMainPanelSize.getHeight();
            }
            this.mTouchableRegion.set((int) this.mContentContainer.getX(), (int) this.mContentContainer.getY(), ((int) this.mContentContainer.getX()) + width, ((int) this.mContentContainer.getY()) + height);
        }

        private void setTouchableSurfaceInsetsComputer() {
            ViewTreeObserver viewTreeObserver = this.mPopupWindow.getContentView().getRootView().getViewTreeObserver();
            viewTreeObserver.removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            viewTreeObserver.addOnComputeInternalInsetsListener(this.mInsetsComputer);
        }

        private boolean isInRTLMode() {
            if (this.mContext.getApplicationInfo().hasRtlSupport()) {
                return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
            } else {
                return false;
            }
        }

        private boolean hasOverflow() {
            return this.mOverflowPanelSize != null;
        }

        public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
            Preconditions.checkNotNull(menuItems);
            int availableWidth = toolbarWidth;
            LinkedList<MenuItem> remainingMenuItems = new LinkedList();
            LinkedList<MenuItem> overflowMenuItems = new LinkedList();
            for (MenuItem menuItem : menuItems) {
                if (menuItem.requiresOverflow()) {
                    overflowMenuItems.add(menuItem);
                } else {
                    remainingMenuItems.add(menuItem);
                }
            }
            remainingMenuItems.addAll(overflowMenuItems);
            this.mMainPanel.removeAllViews();
            this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
            this.mMainPanel.layoutMenuItems(remainingMenuItems, toolbarWidth, this.mOverflowButtonSize.getWidth());
            if (!remainingMenuItems.isEmpty()) {
                this.mMainPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
            }
            this.mMainPanelSize = measure(this.mMainPanel);
            return remainingMenuItems;
        }

        private void layoutOverflowPanelItems(List<MenuItem> menuItems) {
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            int size = menuItems.size();
            for (int i = 0; i < size; i++) {
                overflowPanelAdapter.add((MenuItem) menuItems.get(i));
            }
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            if (this.mOpenOverflowUpwards) {
                this.mOverflowPanel.setY(0.0f);
            } else {
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
            }
            this.mOverflowPanelSize = new Size(Math.max(getOverflowWidth(), this.mOverflowButtonSize.getWidth()), calculateOverflowHeight(4));
            setSize(this.mOverflowPanel, this.mOverflowPanelSize);
        }

        private void preparePopupContent() {
            this.mContentContainer.removeAllViews();
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowPanel);
            }
            this.mContentContainer.addView(this.mMainPanel);
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowButton);
            }
            setPanelsStatesAtRestingPosition();
            setContentAreaAsTouchableSurface();
            if (isInRTLMode()) {
                this.mContentContainer.setAlpha(0.0f);
                this.mContentContainer.post(this.mPreparePopupContentRTLHelper);
            }
        }

        private void clearPanels() {
            this.mOverflowPanelSize = null;
            this.mMainPanelSize = null;
            this.mIsOverflowOpen = false;
            this.mMainPanel.removeAllViews();
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            this.mContentContainer.removeAllViews();
        }

        private void positionContentYCoordinatesIfOpeningOverflowUpwards() {
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY((float) (this.mContentContainer.getHeight() - this.mMainPanelSize.getHeight()));
                this.mOverflowButton.setY((float) (this.mContentContainer.getHeight() - this.mOverflowButton.getHeight()));
                this.mOverflowPanel.setY((float) (this.mContentContainer.getHeight() - this.mOverflowPanelSize.getHeight()));
            }
        }

        private int getOverflowWidth() {
            int overflowWidth = 0;
            int count = this.mOverflowPanel.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                overflowWidth = Math.max(this.mOverflowPanelViewHelper.calculateWidth((MenuItem) this.mOverflowPanel.getAdapter().getItem(i)), overflowWidth);
            }
            return overflowWidth;
        }

        private int calculateOverflowHeight(int maxItemSize) {
            int actualSize = Math.min(4, Math.min(Math.max(2, maxItemSize), this.mOverflowPanel.getCount()));
            int extension = 0;
            if (actualSize < this.mOverflowPanel.getCount()) {
                extension = (int) (((float) this.mLineHeight) * 0.5f);
            }
            return ((this.mLineHeight * actualSize) + this.mOverflowButtonSize.getHeight()) + extension;
        }

        private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
            menuItemButton.setTag(menuItem);
            menuItemButton.setOnClickListener(this.mMenuItemButtonOnClickListener);
        }

        private int getAdjustedDuration(int originalDuration) {
            if (this.mTransitionDurationScale < 150) {
                return Math.max(originalDuration - 50, 0);
            }
            if (this.mTransitionDurationScale > 300) {
                return originalDuration + 50;
            }
            return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
        }

        private void maybeComputeTransitionDurationScale() {
            if (this.mMainPanelSize != null && this.mOverflowPanelSize != null) {
                int w = this.mMainPanelSize.getWidth() - this.mOverflowPanelSize.getWidth();
                int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
                this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
            }
        }

        private ViewGroup createMainPanel() {
            FtMainPanelLayout mainPanel = new FtMainPanelLayout(this.mContext) {
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (FloatingToolbarPopup.this.isOverflowAnimating()) {
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(FloatingToolbarPopup.this.mMainPanelSize.getWidth(), 1073741824);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    return FloatingToolbarPopup.this.isOverflowAnimating();
                }
            };
            mainPanel.setMenuItemOnClickListener(this.mMenuItemButtonOnClickListener);
            return mainPanel;
        }

        private ImageButton createOverflowButton() {
            ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate(FloatingToolbar.TOOLBAR_OVERFLOW_BTN_LAYOUT, null);
            overflowButton.setBackgroundResource(FloatingToolbar.TOOLBAR_MIDDLE_BG);
            LayoutParams params = new LayoutParams(-2, -2);
            params.height = (int) this.mContext.getResources().getDimension(FloatingToolbar.TOOLBAR_OVERFLOW_ITEM_HEIGHT);
            overflowButton.setLayoutParams(params);
            if (isInRTLMode()) {
                overflowButton.setScaleType(ScaleType.FIT_END);
            } else {
                overflowButton.setScaleType(ScaleType.FIT_START);
            }
            overflowButton.setImageDrawable(this.mOverflow);
            overflowButton.setOnClickListener(new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass2(this, overflowButton));
            return overflowButton;
        }

        /* renamed from: lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_77649 */
        /* synthetic */ void m56xf8ec1899(ImageButton overflowButton, View v) {
            if (this.mIsOverflowOpen) {
                overflowButton.setImageDrawable(this.mToOverflow);
                this.mToOverflow.start();
                closeOverflow();
                return;
            }
            overflowButton.setImageDrawable(this.mToArrow);
            this.mToArrow.start();
            openOverflow();
        }

        private OverflowPanel createOverflowPanel() {
            OverflowPanel overflowPanel = new OverflowPanel(this);
            overflowPanel.setLayoutParams(new LayoutParams(-1, -1));
            overflowPanel.setDivider(null);
            overflowPanel.setDividerHeight(0);
            overflowPanel.setEdgeEffect(true);
            overflowPanel.setSpringEffect(false);
            overflowPanel.setHoldingModeEnabled(false);
            overflowPanel.setVerticalScrollBarEnabled(false);
            overflowPanel.setAdapter(new ArrayAdapter<MenuItem>(this.mContext, 0) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    return FloatingToolbarPopup.this.mOverflowPanelViewHelper.getView((MenuItem) getItem(position), FloatingToolbarPopup.this.mOverflowPanelSize.getWidth(), convertView);
                }
            });
            overflowPanel.setOnItemClickListener(new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass3(this, overflowPanel));
            return overflowPanel;
        }

        /* renamed from: lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_79349 */
        /* synthetic */ void m57xf8ecf614(OverflowPanel overflowPanel, AdapterView adapterView, View view, int position, long id) {
            MenuItem menuItem = (MenuItem) overflowPanel.getAdapter().getItem(position);
            if (this.mOnMenuItemClickListener != null) {
                this.mOnMenuItemClickListener.onMenuItemClick(menuItem);
            }
        }

        private boolean isOverflowAnimating() {
            int overflowOpening;
            boolean overflowClosing;
            if (this.mOpenOverflowAnimation.hasStarted()) {
                overflowOpening = this.mOpenOverflowAnimation.hasEnded() ^ 1;
            } else {
                overflowOpening = 0;
            }
            if (this.mCloseOverflowAnimation.hasStarted()) {
                overflowClosing = this.mCloseOverflowAnimation.hasEnded() ^ 1;
            } else {
                overflowClosing = false;
            }
            return overflowOpening == 0 ? overflowClosing : true;
        }

        private AnimationListener createOverflowAnimationListener() {
            return new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    FloatingToolbarPopup.this.mOverflowButton.setEnabled(false);
                    FloatingToolbarPopup.this.mMainPanel.setVisibility(0);
                    FloatingToolbarPopup.this.mOverflowPanel.setVisibility(0);
                }

                public void onAnimationEnd(Animation animation) {
                    FloatingToolbarPopup.this.mContentContainer.post(new -$Lambda$hZenqyGYSNt5KiruOSsv736MIDs((byte) 0, this));
                }

                /* renamed from: lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup$13_81075 */
                /* synthetic */ void m58x32490cdf() {
                    FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                    FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                }

                public void onAnimationRepeat(Animation animation) {
                }
            };
        }

        private static Size measure(View view) {
            boolean z;
            if (view.getParent() == null) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkState(z);
            view.measure(0, 0);
            return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        private static void setSize(View view, int width, int height) {
            view.setMinimumWidth(width);
            view.setMinimumHeight(height);
            LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(0, 0);
            }
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        }

        private static void setSize(View view, Size size) {
            setSize(view, size.getWidth(), size.getHeight());
        }

        private static void setWidth(View view, int width) {
            setSize(view, width, view.getLayoutParams().height);
        }

        private static void setHeight(View view, int height) {
            setSize(view, view.getLayoutParams().width, height);
        }
    }

    public FloatingToolbar(Window window) {
        this.mContext = applyDefaultTheme(window.getContext());
        this.mWindow = (Window) Preconditions.checkNotNull(window);
        this.mPopup = new FloatingToolbarPopup(this.mContext, window.getDecorView());
        initFloatingToolbarRes(this.mContext);
    }

    private void initFloatingToolbarRes(Context context) {
        TypedArray mTypeArray = new ContextThemeWrapper(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.SYSTEM_DEFAULT)).obtainStyledAttributes(null, R.styleable.TextViewToolbar, R.attr.textViewToolbarStyle, R.style.Vigour_TextViewToolbar);
        TOOLBAR_OVERFLOW_BTN_TOARROW = mTypeArray.getResourceId(9, TOOLBAR_OVERFLOW_BTN_TOARROW);
        TOOLBAR_OVERFLOW_BTN_TOARROW_ANIM = mTypeArray.getResourceId(10, TOOLBAR_OVERFLOW_BTN_TOARROW_ANIM);
        TOOLBAR_OVERFLOW_BTN_TOOVERFLOW = mTypeArray.getResourceId(11, TOOLBAR_OVERFLOW_BTN_TOOVERFLOW);
        TOOLBAR_OVERFLOW_BTN_ANIM_TOOVERFLOW = mTypeArray.getResourceId(12, TOOLBAR_OVERFLOW_BTN_ANIM_TOOVERFLOW);
        TOOLBAR_WHOLE_BG = mTypeArray.getResourceId(3, TOOLBAR_WHOLE_BG);
        TOOLBAR_RIGHT_BG = mTypeArray.getResourceId(1, TOOLBAR_RIGHT_BG);
        TOOLBAR_LEFT_BG = mTypeArray.getResourceId(0, TOOLBAR_LEFT_BG);
        TOOLBAR_MIDDLE_BG = mTypeArray.getResourceId(2, TOOLBAR_MIDDLE_BG);
        TOOLBAR_OVERFLOW_ITEM_LAYOUT = mTypeArray.getResourceId(7, TOOLBAR_OVERFLOW_ITEM_LAYOUT);
        TOOLBAR_OVERFLOW_BTN_LAYOUT = mTypeArray.getResourceId(8, TOOLBAR_OVERFLOW_BTN_LAYOUT);
        mTypeArray.recycle();
    }

    public FloatingToolbar setMenu(Menu menu) {
        this.mMenu = (Menu) Preconditions.checkNotNull(menu);
        return this;
    }

    public FloatingToolbar setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        if (menuItemClickListener != null) {
            this.mMenuItemClickListener = menuItemClickListener;
        } else {
            this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        }
        return this;
    }

    public FloatingToolbar setContentRect(Rect rect) {
        this.mContentRect.set((Rect) Preconditions.checkNotNull(rect));
        return this;
    }

    public FloatingToolbar setSuggestedWidth(int suggestedWidth) {
        this.mWidthChanged = ((double) Math.abs(suggestedWidth - this.mSuggestedWidth)) > ((double) this.mSuggestedWidth) * 0.2d;
        this.mSuggestedWidth = suggestedWidth;
        return this;
    }

    public FloatingToolbar show() {
        registerOrientationHandler();
        doShow();
        return this;
    }

    public FloatingToolbar updateLayout() {
        if (this.mPopup.isShowing()) {
            doShow();
        }
        return this;
    }

    public void dismiss() {
        unregisterOrientationHandler();
        this.mPopup.dismiss();
    }

    public void hide() {
        this.mPopup.hide();
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isHidden() {
        return this.mPopup.isHidden();
    }

    private void doShow() {
        List<MenuItem> menuItems = getVisibleAndEnabledMenuItems(this.mMenu);
        tidy(menuItems);
        if (!isCurrentlyShowing(menuItems) || this.mWidthChanged) {
            this.mPopup.dismiss();
            this.mPopup.layoutMenuItems(menuItems, this.mMenuItemClickListener, this.mSuggestedWidth);
            this.mShowingMenuItems = menuItems;
        }
        if (!this.mPopup.isShowing()) {
            this.mPopup.show(this.mContentRect);
        } else if (!this.mPreviousContentRect.equals(this.mContentRect)) {
            this.mPopup.updateCoordinates(this.mContentRect);
        }
        this.mWidthChanged = false;
        this.mPreviousContentRect.set(this.mContentRect);
    }

    private boolean isCurrentlyShowing(List<MenuItem> menuItems) {
        if (this.mShowingMenuItems == null || menuItems.size() != this.mShowingMenuItems.size()) {
            return false;
        }
        int size = menuItems.size();
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = (MenuItem) menuItems.get(i);
            MenuItem showingItem = (MenuItem) this.mShowingMenuItems.get(i);
            if (menuItem.getItemId() != showingItem.getItemId() || (TextUtils.equals(menuItem.getTitle(), showingItem.getTitle()) ^ 1) != 0 || (Objects.equals(menuItem.getIcon(), showingItem.getIcon()) ^ 1) != 0 || menuItem.getGroupId() != showingItem.getGroupId()) {
                return false;
            }
        }
        return true;
    }

    private List<MenuItem> getVisibleAndEnabledMenuItems(Menu menu) {
        List<MenuItem> menuItems = new ArrayList();
        int i = 0;
        while (menu != null && i < menu.size()) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isVisible() && menuItem.isEnabled()) {
                Menu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    menuItems.addAll(getVisibleAndEnabledMenuItems(subMenu));
                } else {
                    menuItems.add(menuItem);
                }
            }
            i++;
        }
        return menuItems;
    }

    private void tidy(List<MenuItem> menuItems) {
        int assistItemIndex = -1;
        Drawable assistItemDrawable = null;
        int size = menuItems.size();
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = (MenuItem) menuItems.get(i);
            if (menuItem.getItemId() == com.android.internal.R.id.textAssist) {
                assistItemIndex = i;
                assistItemDrawable = menuItem.getIcon();
            }
            if (!TextUtils.isEmpty(menuItem.getTitle())) {
                menuItem.setIcon(null);
            }
        }
        if (assistItemIndex > -1) {
            MenuItem assistMenuItem = (MenuItem) menuItems.remove(assistItemIndex);
            assistMenuItem.setIcon(assistItemDrawable);
            menuItems.add(0, assistMenuItem);
        }
    }

    private void registerOrientationHandler() {
        unregisterOrientationHandler();
        this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private void unregisterOrientationHandler() {
        this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem, int iconTextSpacing) {
        View menuItemButton = LayoutInflater.from(context).inflate((int) com.android.internal.R.layout.floating_popup_menu_button, null);
        if (menuItem != null) {
            updateMenuItemButton(menuItemButton, menuItem, iconTextSpacing);
        }
        return menuItemButton;
    }

    private static void updateMenuItemButton(View menuItemButton, MenuItem menuItem, int iconTextSpacing) {
        TextView buttonText = (TextView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_text);
        if (TextUtils.isEmpty(menuItem.getTitle())) {
            buttonText.setVisibility(8);
        } else {
            buttonText.setVisibility(0);
            buttonText.setText(menuItem.getTitle());
        }
        ImageView buttonIcon = (ImageView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_image);
        if (menuItem.getIcon() == null) {
            buttonIcon.setVisibility(8);
            if (buttonText != null) {
                buttonText.setPaddingRelative(0, 0, 0, 0);
            }
        } else {
            buttonIcon.setVisibility(0);
            buttonIcon.setImageDrawable(menuItem.getIcon());
            if (buttonText != null) {
                buttonText.setPaddingRelative(iconTextSpacing, 0, 0, 0);
            }
        }
        CharSequence contentDescription = menuItem.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            menuItemButton.setContentDescription(menuItem.getTitle());
        } else {
            menuItemButton.setContentDescription(contentDescription);
        }
    }

    private static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = new ContentContainerViewGroup(context);
        MarginLayoutParams contentContainerParam = new MarginLayoutParams(-2, -2);
        int containerMargin = (int) context.getResources().getDimension(TOOLBAR_CONTAINER_MARGIN);
        contentContainerParam.setMargins(containerMargin, containerMargin, containerMargin, containerMargin);
        contentContainer.setLayoutParams(contentContainerParam);
        contentContainer.setTag(FLOATING_TOOLBAR_TAG);
        return contentContainer;
    }

    private static PopupWindow createPopupWindow(ViewGroup content) {
        View popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(1005);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        content.setLayoutParams(new LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    private static View createDivider(Context context) {
        View divider = new View(context);
        int _1dp = (int) TypedValue.applyDimension(1, 1.0f, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_1dp, -1);
        params.setMarginsRelative(0, _1dp * 10, 0, _1dp * 10);
        divider.setLayoutParams(params);
        TypedArray a = context.obtainStyledAttributes(new TypedValue().data, new int[]{com.android.internal.R.attr.floatingToolbarDividerColor});
        divider.setBackgroundColor(a.getColor(0, 0));
        a.recycle();
        divider.setImportantForAccessibility(2);
        divider.setEnabled(false);
        divider.setFocusable(false);
        divider.setContentDescription(null);
        return divider;
    }

    private static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f}).setDuration(150);
        animation.playTogether(animatorArr);
        return animation;
    }

    private static AnimatorSet createExitAnimation(View view, int startDelay, AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{1.0f, 0.0f}).setDuration(100);
        animation.playTogether(animatorArr);
        animation.setStartDelay((long) startDelay);
        animation.addListener(listener);
        return animation;
    }

    private static Context applyDefaultTheme(Context originalContext) {
        TypedArray a = originalContext.obtainStyledAttributes(new int[]{com.android.internal.R.attr.isLightTheme});
        int themeId = a.getBoolean(0, true) ? com.android.internal.R.style.Theme_Material_Light : com.android.internal.R.style.Theme_Material;
        a.recycle();
        return new ContextThemeWrapper(originalContext, themeId);
    }
}
