package android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemProperties;
import android.service.notification.ZenModeConfig;
import android.telecom.Logging.Session;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity;

public class ResolverActivityIntroduceViewGroup extends RelativeLayout {
    private Display display;
    private final String hasNavigationGesture = "1";
    private View mAnchorView;
    private ResolverActivity mResolverContext;
    private int navigationHeight;
    private int resolveActivityYPositon;
    private int screenHeight;
    private int screenWidth;
    private int statusBarHeight;
    private String tips;
    private float tipsOffset;
    private int tipsShadowColor;
    private float tipsShadowPaddingStart;
    private float tipsShadowPaddingTop;
    private float tipsShadowRaduis;
    private int tipsSize;
    private WindowManager windowManager;

    public ResolverActivityIntroduceViewGroup(Context context, View mAnchorView, String tips) {
        super(context);
        this.mResolverContext = (ResolverActivity) context;
        this.mAnchorView = mAnchorView;
        this.tips = tips;
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        this.windowManager = (WindowManager) context.getSystemService("window");
        this.display = this.windowManager.getDefaultDisplay();
        this.display.getMetrics(mDisplayMetrics);
        int mDensity = (int) mDisplayMetrics.density;
        this.screenWidth = mDisplayMetrics.widthPixels;
        this.screenHeight = mDisplayMetrics.heightPixels;
        if (!"1".equals(SystemProperties.get("qemu.hw.mainkeys"))) {
            this.navigationHeight = this.mResolverContext.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height_landscape);
        }
        this.tipsSize = getResources().getDimensionPixelSize(com.vivo.internal.R.dimen.tipsSize);
        this.tipsOffset = (float) getResources().getDimensionPixelOffset(com.vivo.internal.R.dimen.tipsOffest);
        this.tipsShadowPaddingStart = getResources().getDimension(com.vivo.internal.R.dimen.tipsShadowPaddingStart);
        this.tipsShadowRaduis = getResources().getDimension(com.vivo.internal.R.dimen.tipsShadowRaduis);
        this.tipsShadowPaddingTop = getResources().getDimension(com.vivo.internal.R.dimen.tipsShadowPaddingTop);
        this.tipsShadowColor = getResources().getColor(com.vivo.internal.R.color.shadow);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", ZenModeConfig.SYSTEM_AUTHORITY);
        if (resourceId > 0) {
            this.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        if (mAnchorView == null || tips == null) {
            setWillNotDraw(true);
        } else {
            setWillNotDraw(false);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(this.screenWidth, this.screenHeight);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint(1);
        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(bitmap);
        drawDimLayer(bitmapCanvas, paint);
        drawText(bitmapCanvas, paint, this.tips, drawCircle(bitmapCanvas, paint));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
    }

    public void drawDimLayer(Canvas canvas, Paint paint) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width > 0 && height > 0) {
            RectF outerRectangle = new RectF(0.0f, 0.0f, (float) width, (float) height);
            paint.setColor(-16777216);
            paint.setAlpha(153);
            canvas.drawRect(outerRectangle, paint);
        }
    }

    public float[] drawCircle(Canvas canvas, Paint paint) {
        float centerX;
        float centerY;
        RectF anchorRecr = calculeRectOnScreen(this.mAnchorView);
        float left = anchorRecr.left;
        float top = anchorRecr.top;
        float iconHalfWidth = (float) (((this.mAnchorView.getWidth() - this.mAnchorView.getPaddingStart()) - this.mAnchorView.getPaddingEnd()) / 2);
        float iconHalfHeight = (float) (((this.mAnchorView.getHeight() - this.mAnchorView.getPaddingTop()) - this.mAnchorView.getPaddingBottom()) / 2);
        int rotation = this.display.getRotation();
        int iconPadding = this.mAnchorView.getPaddingStart();
        int statusHeight = this.mResolverContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        if (isRtl()) {
            iconPadding = 0;
        }
        if (isPortraitp()) {
            centerX = (left + iconHalfWidth) + ((float) iconPadding);
            centerY = ((top + iconHalfHeight) + ((float) this.mAnchorView.getPaddingTop())) - ((float) statusHeight);
        } else {
            if (rotation == 1) {
                centerX = ((left + iconHalfWidth) + ((float) iconPadding)) - ((float) statusHeight);
            } else {
                centerX = ((left + iconHalfWidth) + ((float) iconPadding)) - ((float) this.navigationHeight);
            }
            centerY = (top + iconHalfHeight) + ((float) this.mAnchorView.getPaddingTop());
        }
        float raduis = (float) Math.sqrt((double) ((iconHalfWidth * iconHalfWidth) * 2.0f));
        paint.setAntiAlias(true);
        paint.setColor(0);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
        canvas.drawCircle(centerX, centerY, raduis, paint);
        paint.setXfermode(null);
        centerY -= raduis;
        return new float[]{centerX + raduis, centerY};
    }

    public void drawText(Canvas canvas, Paint textPaint, String tips, float[] anchorViewCenterPositin) {
        float tipsShadowLeftPosition;
        float tipsShadowRightPosition;
        boolean isTipsOverLength;
        float tipsShadowTopPosition;
        String showingTips;
        textPaint.setTextSize((float) this.tipsSize);
        float tipsLength = textPaint.measureText(tips);
        int rotation = this.display.getRotation();
        if (isRtl()) {
            tipsShadowLeftPosition = (float) getResources().getDimensionPixelSize(com.vivo.internal.R.dimen.tipsShadowRightPosition);
            if (rotation == 1) {
                tipsShadowLeftPosition = 0.0f;
            }
            tipsShadowRightPosition = (tipsShadowLeftPosition + tipsLength) + (this.tipsShadowPaddingStart * 2.0f);
            isTipsOverLength = tipsShadowLeftPosition + tipsShadowRightPosition > ((float) this.screenWidth);
            if (isTipsOverLength) {
                tipsShadowRightPosition = (float) this.screenWidth;
            }
        } else {
            tipsShadowRightPosition = (float) (this.screenWidth - getResources().getDimensionPixelSize(com.vivo.internal.R.dimen.tipsShadowRightPosition));
            if (rotation == 3) {
                tipsShadowRightPosition = (float) this.screenWidth;
            }
            tipsShadowLeftPosition = (tipsShadowRightPosition - tipsLength) - (this.tipsShadowPaddingStart * 2.0f);
            isTipsOverLength = tipsShadowLeftPosition - this.tipsShadowPaddingStart < 0.0f;
            if (isTipsOverLength) {
                tipsShadowLeftPosition = 0.0f;
            }
        }
        if (isPortraitp()) {
            tipsShadowTopPosition = ((anchorViewCenterPositin[1] - this.tipsOffset) - textPaint.getTextSize()) - this.tipsShadowPaddingTop;
        } else {
            tipsShadowTopPosition = ((anchorViewCenterPositin[1] + this.tipsOffset) + textPaint.getTextSize()) + this.tipsShadowPaddingTop;
        }
        float tipsShadowBottomPosition = (textPaint.getTextSize() + tipsShadowTopPosition) + this.tipsShadowPaddingTop;
        textPaint.setColor(this.tipsShadowColor);
        canvas.drawRoundRect(new RectF(tipsShadowLeftPosition, tipsShadowTopPosition, tipsShadowRightPosition, tipsShadowBottomPosition), this.tipsShadowRaduis, this.tipsShadowRaduis, textPaint);
        textPaint.setTextAlign(Align.LEFT);
        FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseLineY = ((((textPaint.getTextSize() / 2.0f) + tipsShadowTopPosition) + (this.tipsShadowPaddingTop / 2.0f)) - (fontMetrics.top / 2.0f)) - (fontMetrics.bottom / 2.0f);
        float baseLineX = tipsShadowLeftPosition + this.tipsShadowPaddingStart;
        textPaint.setColor(-1);
        if (isTipsOverLength) {
            showingTips = getShowingTips(tips, tipsShadowLeftPosition, tipsShadowRightPosition, textPaint);
        } else {
            showingTips = tips;
        }
        canvas.drawText(showingTips, baseLineX, baseLineY, textPaint);
    }

    private RectF calculeRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF((float) location[0], (float) location[1], (float) (location[0] + view.getMeasuredWidth()), (float) (location[1] + view.getMeasuredHeight()));
    }

    public boolean onTouchEvent(MotionEvent event) {
        setVisibility(8);
        this.mResolverContext.setRequestedOrientation(4);
        return super.onTouchEvent(event);
    }

    private boolean isRtl() {
        return getLayoutDirection() == 1;
    }

    private boolean isPortraitp() {
        return this.mResolverContext.getResources().getConfiguration().orientation == 1;
    }

    private String getShowingTips(String tips, float tipsShadowLeftPosition, float tipsShadowRightPosition, Paint paint) {
        Paint paint2 = paint;
        String str = tips;
        return tips.substring(0, paint2.breakText(str, 0, tips.length(), true, (tipsShadowRightPosition - tipsShadowLeftPosition) - (this.tipsShadowPaddingStart * 2.0f), null) - 2) + Session.TRUNCATE_STRING;
    }
}
