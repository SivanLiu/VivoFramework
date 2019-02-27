package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.view.Menu;
import com.vivo.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class ImageUtil {
    private static final int BOTTOMDIRECTION = 1004;
    private static final int LEFTDIRECTION = 1001;
    private static final int RIGHTDIRECTION = 1002;
    private static final int TOPDIRECTION = 1003;
    private static boolean isOpenTheme = false;
    private static int[] mBLPoint = new int[2];
    private static float mDensity = 1.0f;
    private static ImageUtil mInstance;
    private static int[] mLTPoint = new int[2];
    private static int mOldThemeId;
    private static int[] mRTPoint = new int[2];
    private static int[] mTLPoint = new int[2];
    private static int sAlphaThreshold = 100;
    private static float sIconZoomFactor = 1.01f;
    private final int ALPHA_REF = 200;
    private final boolean DEBUG = false;
    private final float MIN_OS_VERSION;
    private final float SCALE_RATIO;
    private final String TAG = "ImageUtil";
    private boolean isStandardShape = false;
    private Context mContext;
    private final Canvas sCanvas = new Canvas();
    int sColorIndex;
    int[] sColors;
    private Drawable sIconBg = null;
    private int sIconBgHeight = -1;
    private int sIconBgRadius = -1;
    private Rect sIconBgRect = new Rect();
    private int sIconBgWidth = -1;
    private int sIconHeight = -1;
    private int sIconLeftOffset = 0;
    private int sIconTextureHeight = -1;
    private int sIconTextureWidth = -1;
    private int sIconTopOffset = 0;
    private int sIconWidth = -1;
    private int sIconWithBgHeight = -1;
    private int sIconWithBgWidth = -1;
    private boolean sInit = false;
    private Bitmap sMaskBitmap = null;
    private int sMaskRadius = -1;
    private Rect sMaskRect = new Rect();
    private boolean sNeedRedraw = false;
    private final Rect sOldBounds = new Rect();

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static ImageUtil getInstance(Context context) {
        if (mInstance == null || mDensity != context.getResources().getDisplayMetrics().density) {
            synchronized (ImageUtil.class) {
                if (mInstance == null || mDensity != context.getResources().getDisplayMetrics().density) {
                    mInstance = new ImageUtil(context);
                }
            }
        }
        isOpenTheme = isOpenThemeStyle();
        return mInstance;
    }

    private ImageUtil(Context context) {
        this.sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        this.sColors = new int[]{Menu.CATEGORY_MASK, -16711936, -16776961};
        this.sColorIndex = 0;
        this.MIN_OS_VERSION = 2.0f;
        this.SCALE_RATIO = 0.7352941f;
        if (this.mContext == null) {
            this.mContext = context;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public Bitmap createIconBitmap(Drawable icon, Resources res, int id) {
        return createIconBitmap(icon, null, this.mContext, true, false, res, id);
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public Bitmap createRedrawIconBitmap(Drawable srcIconDrawable) {
        if (isExport() && isOpenTheme) {
            return createOpenIconBitmap(srcIconDrawable, this.mContext);
        }
        return createIconBitmap(srcIconDrawable, null, 0);
    }

    public Bitmap createOpenIconBitmap(Drawable icon, Context context) {
        Bitmap finalBitmap;
        synchronized (this.sCanvas) {
            if (this.sIconWidth == -1) {
                initStatics(context);
            }
            float mIconBgSize = context.getResources().getDimension(R.dimen.scene_app_bg_size);
            int sIconScaleWidth = (int) (0.7352941f * mIconBgSize);
            int sIconScaleHeight = (int) (0.7352941f * mIconBgSize);
            int width = sIconScaleWidth;
            int height = sIconScaleHeight;
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(sIconScaleWidth);
                painter.setIntrinsicHeight(sIconScaleHeight);
            } else if (icon instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                if (bitmapDrawable.getBitmap().getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int iconWidth = icon.getIntrinsicWidth();
            int iconeHeight = icon.getIntrinsicHeight();
            Bitmap bmpIcon = drawableToBitmap(icon);
            int left = getFactIconSize(bmpIcon, 1001);
            int right = getFactIconSize(bmpIcon, 1002);
            int top = getFactIconSize(bmpIcon, 1003);
            int bottom = getFactIconSize(bmpIcon, 1004);
            int sourceWidth = Math.abs(left - right);
            int sourceHeight = Math.abs(top - bottom);
            if (this.sIconWidth > 0 && this.sIconHeight > 0 && ((double) (iconWidth * iconeHeight)) > ((double) (this.sIconWidth * this.sIconHeight)) * 1.5d) {
                iconWidth = this.sIconWidth;
                iconeHeight = this.sIconHeight;
            }
            if (sourceWidth > 0 && sourceHeight > 0) {
                float ratio = ((float) iconWidth) / ((float) iconeHeight);
                float scaleX = ((float) sIconScaleWidth) / ((float) sourceWidth);
                float scaleY = ((float) sIconScaleHeight) / ((float) sourceHeight);
                if (sourceWidth > sourceHeight) {
                    width = (int) (((float) iconWidth) * scaleX);
                    height = (int) (((float) width) / ratio);
                } else {
                    height = (int) (((float) iconeHeight) * scaleY);
                    width = (int) (((float) height) * ratio);
                }
            }
            if (this.sIconTextureWidth <= 0 || this.sIconTextureHeight <= 0) {
                this.sIconTextureWidth = (int) mIconBgSize;
                this.sIconTextureHeight = (int) mIconBgSize;
            }
            int textureWidth = this.sIconTextureWidth;
            int textureHeight = this.sIconTextureHeight;
            Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Config.ARGB_8888);
            Canvas canvas = this.sCanvas;
            canvas.setBitmap(bitmap);
            left = (textureWidth - width) / 2;
            top = (textureHeight - height) / 2;
            this.sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.draw(canvas);
            icon.setBounds(this.sOldBounds);
            canvas.setBitmap(null);
            finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, this.sIconTextureWidth, this.sIconTextureHeight);
        }
        return finalBitmap;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public Bitmap createIconBitmap(Drawable srcIconDrawable, Bitmap icon, Context context, boolean isScale, boolean isRedraw, Resources res, int id) {
        Configuration config = context.getResources().getConfiguration();
        if (config == null) {
            clearInitFlags(-1);
        } else {
            int mNewThemeId = config.hashCode();
            if (mNewThemeId != mOldThemeId) {
                clearInitFlags(mNewThemeId);
            }
        }
        if (res != null) {
            try {
                if (res.getResourcePackageName(id) != null) {
                    String pkg = res.getResourcePackageName(id);
                    DynamicIcon dyIcon = null;
                    if (pkg.equals("com.bbk.calendar")) {
                        dyIcon = DynamicIcon.creatDynamicIcon(DynamicIcon.CALENDAR_COMP, this.mContext);
                    } else {
                        boolean equals = pkg.equals("com.vivo.weather");
                    }
                    if (dyIcon != null) {
                        Bitmap temBitmap = dyIcon.getIcon(this.mContext);
                        if (temBitmap != null) {
                            return temBitmap;
                        }
                        return drawableToBitmap(srcIconDrawable);
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return createIconBitmap(srcIconDrawable, context);
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public Bitmap createGreyBitmap(Bitmap b) {
        Bitmap greyBitmap;
        synchronized (this.sCanvas) {
            greyBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
            Canvas c = this.sCanvas;
            c.setBitmap(greyBitmap);
            Paint paint = new Paint(1);
            ColorMatrix greyColorMatrix = new ColorMatrix();
            greyColorMatrix.reset();
            greyColorMatrix.setSaturation(0.0f);
            paint.setColorFilter(new ColorMatrixColorFilter(greyColorMatrix));
            paint.setAlpha(255);
            c.drawBitmap(b, 0.0f, 0.0f, paint);
            c.setBitmap(null);
        }
        return greyBitmap;
    }

    /* JADX WARNING: Missing block: B:50:0x022e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initStatics(Context context) {
        Resources resources = context.getResources();
        mDensity = resources.getDisplayMetrics().density;
        readThemeAppIconSize();
        if (this.sMaskBitmap == null || this.sIconBg == null) {
            String icon_mask = VivoTheme.getThemePath() + "launcher/icon_mask.png";
            String icon_bg_one = VivoTheme.getThemePath() + "launcher/icon_bg_one.png";
            this.sMaskBitmap = BitmapFactory.decodeFile(icon_mask);
            Bitmap temap = BitmapFactory.decodeFile(icon_bg_one);
            if (temap == null || this.sMaskBitmap == null) {
                Log.w("ImageUtil", "icon_bg_one or icon_mask is null. exist?");
                return;
            }
            int dimension;
            int iconSize = (int) resources.getDimension(R.dimen.scene_app_icon_size);
            int maskWidth = this.sMaskBitmap.getWidth();
            float maskDensity = mDensity;
            Log.d("ImageUtil", "maskDensity = " + maskDensity + " maskWidth = " + maskWidth + " iconSize =" + iconSize);
            if (iconSize != maskWidth) {
                switch (maskWidth) {
                    case 136:
                        maskDensity = 2.0f;
                        break;
                    case 198:
                    case 204:
                        maskDensity = 3.0f;
                        break;
                    case 250:
                        maskDensity = 4.0f;
                        break;
                }
            }
            float scale = (((float) iconSize) * 1.0f) / ((float) maskWidth);
            if (this.sIconWithBgWidth == -1 || this.sIconWithBgHeight == -1) {
                dimension = (int) resources.getDimension(R.dimen.app_icon_size_has_bg);
                this.sIconWithBgHeight = dimension;
                this.sIconWithBgWidth = dimension;
            } else {
                this.sIconWithBgWidth = (int) (((float) this.sIconWithBgWidth) * (maskDensity * scale));
                this.sIconWithBgHeight = (int) (((float) this.sIconWithBgHeight) * (maskDensity * scale));
                this.sIconTopOffset = (int) (((float) this.sIconTopOffset) * (maskDensity * scale));
                this.sIconLeftOffset = (int) (((float) this.sIconLeftOffset) * (maskDensity * scale));
            }
            this.sMaskBitmap = Bitmap.createScaledBitmap(this.sMaskBitmap, iconSize, iconSize, true);
            this.sIconBg = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(temap, iconSize, iconSize, true));
            Bitmap iconBg = drawableToBitmap(this.sIconBg);
            dimension = iconBg.getWidth();
            this.sIconWidth = dimension;
            this.sIconBgWidth = dimension;
            dimension = iconBg.getHeight();
            this.sIconHeight = dimension;
            this.sIconBgHeight = dimension;
            dimension = this.sIconWidth;
            this.sIconTextureHeight = dimension;
            this.sIconTextureWidth = dimension;
            Log.d("ImageUtil", "iconSize : " + iconSize + "  sIconWidth : " + this.sIconWidth + "  sIconHeight:" + this.sIconHeight);
            this.sMaskRadius = getRadius(this.sMaskBitmap, this.sMaskRect);
            this.sIconBgRadius = getRadius(iconBg, this.sIconBgRect);
            if (this.isStandardShape || (this.sMaskBitmap != null && this.sMaskRadius >= 0 && this.sMaskRadius < this.sMaskRect.height() / 3 && this.sMaskRadius < this.sMaskRect.width() / 3 && this.sIconBgRadius >= 0 && this.sIconBgRadius < this.sIconBgRect.width() / 3 && this.sIconBgRadius < this.sIconBgRect.height() / 3 && this.sMaskRect.left == this.sIconBgRect.left && this.sMaskRect.right == this.sIconBgRect.right && this.sMaskRect.top == this.sIconBgRect.top && this.sMaskRect.bottom == this.sIconBgRect.bottom)) {
                this.sNeedRedraw = true;
            } else {
                this.sNeedRedraw = false;
            }
            this.sInit = true;
            Log.d("ImageUtil", "the sNeedRedraw " + this.sNeedRedraw + "   maskWidth " + this.sMaskBitmap.getWidth());
            Log.d("ImageUtil", "the mask rect = " + this.sMaskRect + " sMaskRadius = " + this.sMaskRadius);
            Log.d("ImageUtil", "the bg rect = " + this.sIconBgRect + " sIconBgRadius = " + this.sIconBgRadius);
        }
    }

    private void readThemeAppIconSize() {
        File iconFile = new File(VivoTheme.getThemePath() + "launcher/iconsize.xml");
        this.sIconWithBgHeight = -1;
        this.sIconWithBgWidth = -1;
        if (iconFile.exists()) {
            try {
                InputStream inS = new FileInputStream(iconFile);
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                XmlFileReadHandler s = new XmlFileReadHandler();
                parser.parse(inS, s);
                HashMap<String, String> themeInfo = s.getThemeInfo();
                if (themeInfo.size() == 3) {
                    this.sIconWithBgWidth = Integer.parseInt((String) themeInfo.get("width"));
                    this.sIconWithBgHeight = Integer.parseInt((String) themeInfo.get("height"));
                    this.sIconLeftOffset = 0;
                    this.sIconTopOffset = 0;
                    if (this.sIconWithBgWidth != this.sIconWithBgHeight) {
                        this.sIconWithBgWidth = -1;
                        this.sIconWithBgHeight = -1;
                    }
                } else {
                    this.sIconWithBgWidth = Integer.parseInt((String) themeInfo.get("width"));
                    this.sIconWithBgHeight = Integer.parseInt((String) themeInfo.get("height"));
                    this.sIconLeftOffset = Integer.parseInt((String) themeInfo.get("leftoffset"));
                    this.sIconTopOffset = Integer.parseInt((String) themeInfo.get("topoffset"));
                    this.isStandardShape = "1".equals(themeInfo.get(XmlFileReadHandler.SHAPE));
                    if (this.sIconWithBgWidth != this.sIconWithBgHeight) {
                        this.sIconWithBgWidth = -1;
                        this.sIconWithBgHeight = -1;
                    }
                }
            } catch (Exception e) {
                Log.d("ImageUtil", "read theme exception: " + e);
                this.sIconWithBgWidth = -1;
                this.sIconWithBgHeight = -1;
                this.sIconLeftOffset = -1;
                this.sIconTopOffset = -1;
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (w <= 0 || h <= 0) {
            Log.w("ImageUtil", "drawableToBitmap error : get drawable width and height error!");
            w = this.sIconWidth;
            h = this.sIconHeight;
        }
        if (this.sIconWidth > 0 && this.sIconHeight > 0 && ((double) (w * h)) > ((double) (this.sIconWidth * this.sIconHeight)) * 1.5d) {
            w = this.sIconWidth;
            h = this.sIconHeight;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    static void computeOutlineRect(Bitmap bitmap, Rect rect) {
        int i;
        int j;
        int[] pixels = new int[(bitmap.getWidth() * bitmap.getHeight())];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0;
        int right = width;
        int top = 0;
        int bottom = height;
        boolean keepGoing = true;
        for (i = 0; i < height && keepGoing; i++) {
            for (j = 0; j < width && keepGoing; j++) {
                if (Color.alpha(pixels[(width * i) + j]) > sAlphaThreshold) {
                    top = i;
                    mTLPoint[0] = j;
                    mTLPoint[1] = i;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = 0; i < width && keepGoing; i++) {
            for (j = 0; j < height && keepGoing; j++) {
                if (Color.alpha(pixels[(width * j) + i]) > sAlphaThreshold) {
                    left = i;
                    mLTPoint[0] = i;
                    mLTPoint[1] = j;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = width - 1; i >= 0 && keepGoing; i--) {
            for (j = 0; j < height && keepGoing; j++) {
                if (Color.alpha(pixels[(width * j) + i]) > sAlphaThreshold) {
                    right = i;
                    mRTPoint[0] = i;
                    mRTPoint[1] = j;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = height - 1; i >= 0 && keepGoing; i--) {
            for (j = 0; j <= width - 1 && keepGoing; j++) {
                if (Color.alpha(pixels[(width * i) + j]) > sAlphaThreshold) {
                    bottom = i;
                    mBLPoint[0] = j;
                    mBLPoint[1] = i;
                    keepGoing = false;
                }
            }
        }
        rect.set(left, top, right, bottom);
    }

    private static int comfirmRadius(Bitmap bitmap) {
        int i;
        int j;
        int[] pixels = new int[(bitmap.getWidth() * bitmap.getHeight())];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] mTRPoint = new int[2];
        int[] mLBPoint = new int[2];
        int[] mRBPoint = new int[2];
        int[] mBRPoint = new int[2];
        boolean keepGoing = true;
        for (i = 0; i < height && keepGoing; i++) {
            for (j = width - 1; j >= 0 && keepGoing; j--) {
                if (Color.alpha(pixels[(width * i) + j]) > sAlphaThreshold) {
                    mTRPoint[0] = j;
                    mTRPoint[1] = i;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = 0; i < width && keepGoing; i++) {
            for (j = height - 1; j >= 0 && keepGoing; j--) {
                if (Color.alpha(pixels[(width * j) + i]) > sAlphaThreshold) {
                    mLBPoint[0] = i;
                    mLBPoint[1] = j;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = width - 1; i >= 0 && keepGoing; i--) {
            for (j = height - 1; j >= 0 && keepGoing; j--) {
                if (Color.alpha(pixels[(width * j) + i]) > sAlphaThreshold) {
                    mRBPoint[0] = i;
                    mRBPoint[1] = j;
                    keepGoing = false;
                }
            }
        }
        keepGoing = true;
        for (i = height - 1; i >= 0 && keepGoing; i--) {
            for (j = width - 1; j >= 0 && keepGoing; j--) {
                if (Color.alpha(pixels[(width * i) + j]) > sAlphaThreshold) {
                    mBRPoint[0] = j;
                    mBRPoint[1] = i;
                    keepGoing = false;
                }
            }
        }
        return Math.max(Math.max(Math.abs(mTLPoint[0] - mLTPoint[0]), Math.abs(mTLPoint[1] - mLTPoint[1])), Math.max(Math.max(Math.abs(mTRPoint[0] - mRTPoint[0]), Math.abs(mTRPoint[1] - mRTPoint[1])), Math.max(Math.max(Math.abs(mLBPoint[0] - mBLPoint[0]), Math.abs(mLBPoint[1] - mBLPoint[1])), Math.max(Math.abs(mRBPoint[0] - mBRPoint[0]), Math.abs(mRBPoint[1] - mBRPoint[1])))));
    }

    private static int computeRadius(int[] pixels, Bitmap bitmap, Rect rect) {
        int radius;
        int width = rect.width();
        int height = rect.height();
        if (width == 0 && height == 0) {
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        int right = width;
        int bottom = height;
        double[] diagonals = new double[2];
        boolean isSymmetrical = false;
        computeOutlineRect(bitmap, rect);
        computeDiagonals(diagonals, rect, pixels, bitmap);
        double diagonal = (diagonals[0] + diagonals[1]) / 2.0d;
        if (Math.abs(diagonals[0] - diagonals[1]) < 10.0d) {
            isSymmetrical = true;
        }
        int radiusX = -1;
        int radiusY = -1;
        if (isSymmetrical) {
            radiusX = Math.abs(mTLPoint[0] - mLTPoint[0]);
            radiusY = Math.abs(mTLPoint[1] - mLTPoint[1]);
            radius = Math.max(radiusX, radiusY);
        } else {
            radius = comfirmRadius(bitmap);
        }
        if ((rect.width() > rect.height() ? (((float) rect.width()) * 1.0f) / ((float) rect.height()) : (((float) rect.height()) * 1.0f) / ((float) rect.width())) >= 1.1f || ((double) radius) >= ((double) width) / 2.7d || ((double) radius) >= ((double) height) / 2.7d || Math.abs(radiusX - radiusY) >= 11) {
            return -1;
        }
        return radius;
    }

    private static void computeDiagonals(double[] diagonals, Rect outline, int[] pixels, Bitmap bitmap) {
        int left = outline.left;
        int right = outline.right;
        int top = outline.top;
        int bottom = outline.bottom;
        int[] LTPoint = new int[2];
        int[] RTPoint = new int[2];
        int[] BRPoint = new int[2];
        int[] BLPoint = new int[2];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        pixels = new int[(width * height)];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int i = left;
        int j = top;
        while (i < right && j < bottom) {
            if (((pixels[(j * width) + i] >> 24) & 255) > sAlphaThreshold) {
                LTPoint[0] = i;
                LTPoint[1] = j;
                break;
            }
            i++;
            j++;
        }
        i = right - 1;
        j = bottom - 1;
        while (i >= left && j >= top) {
            if (((pixels[(j * width) + i] >> 24) & 255) > sAlphaThreshold) {
                BRPoint[0] = i;
                BRPoint[1] = j;
                break;
            }
            i--;
            j--;
        }
        i = right - 1;
        j = top;
        while (i >= left && j < bottom) {
            if (((pixels[(j * width) + i] >> 24) & 255) > sAlphaThreshold) {
                RTPoint[0] = i;
                RTPoint[1] = j;
                break;
            }
            i--;
            j++;
        }
        i = left;
        j = bottom - 1;
        while (i < right && j >= top) {
            if (((pixels[(j * width) + i] >> 24) & 255) > sAlphaThreshold) {
                BLPoint[0] = i;
                BLPoint[1] = j;
                break;
            }
            i++;
            j--;
        }
        double diagonalP = Math.sqrt((double) (((BRPoint[0] - LTPoint[0]) * (BRPoint[0] - LTPoint[0])) + ((BRPoint[1] - LTPoint[1]) * (BRPoint[1] - LTPoint[1]))));
        double diagonalN = Math.sqrt((double) (((BLPoint[0] - RTPoint[0]) * (BLPoint[0] - RTPoint[0])) + ((BLPoint[1] - RTPoint[1]) * (BLPoint[1] - RTPoint[1]))));
        diagonals[0] = diagonalP;
        diagonals[1] = diagonalN;
    }

    /* JADX WARNING: Missing block: B:13:0x001b, code:
            if (r12.sIconBg != null) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:18:0x0040, code:
            return r11;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized Bitmap createIconBitmap(Drawable srcIconDrawable, Context context) {
        this.mContext = context;
        if (this.sInit && this.sMaskBitmap != null) {
        }
        initStatics(context);
        if (!this.sInit) {
            return drawableToBitmap(srcIconDrawable);
        }
        Bitmap iconBitmap = drawableToBitmap(srcIconDrawable);
        Rect iconRect = new Rect();
        computeOutlineRect(iconBitmap, iconRect);
        int iconRadius = getRadius(iconBitmap, iconRect);
        Bitmap mOut;
        if (this.sNeedRedraw) {
            mOut = createNormalBitmap(context, iconBitmap, srcIconDrawable, iconRect, drawableToBitmap(this.sIconBg), iconRadius);
        } else {
            mOut = createNonDefalutBitmap(context, srcIconDrawable, iconRect, this.sMaskBitmap, this.sIconBg, null, (float) iconRadius);
        }
    }

    private static int getRadius(Bitmap iconBitmap, Rect iconRect) {
        int[] iconPixels = new int[(iconBitmap.getWidth() * iconBitmap.getHeight())];
        iconBitmap.getPixels(iconPixels, 0, iconBitmap.getWidth(), 0, 0, iconBitmap.getWidth(), iconBitmap.getHeight());
        return computeRadius(iconPixels, iconBitmap, iconRect);
    }

    private Bitmap createNormalBitmap(Context context, Bitmap iconBitmap, Drawable srcIconDrawable, Rect iconRect, Bitmap iconBgBitmap, int iconRadius) {
        int offsetX;
        int offsetY;
        Bitmap finalIcon;
        Canvas canvas = new Canvas();
        int canvasStatus1 = canvas.save();
        Paint mPaint = new Paint(1);
        int[] iconCenter = new int[2];
        int[] iconBgCenter = new int[2];
        int[] maskCenter = new int[2];
        if (iconRadius == -1) {
            getRadius(iconBitmap, iconRect);
        }
        Bitmap icon;
        if (iconRadius == -1) {
            float scale = ((((float) this.sMaskRect.width()) * 1.0f) / ((float) this.sMaskBitmap.getWidth())) * 0.85f;
            icon = Bitmap.createBitmap((int) (((float) iconBgBitmap.getWidth()) * scale), (int) (((float) iconBgBitmap.getHeight()) * scale), Config.ARGB_8888);
            canvas.setBitmap(icon);
            this.sOldBounds.set(srcIconDrawable.getBounds());
            srcIconDrawable.setBounds(0, 0, (int) (((float) this.sMaskBitmap.getWidth()) * scale), (int) (((float) this.sMaskBitmap.getHeight()) * scale));
            srcIconDrawable.draw(canvas);
            srcIconDrawable.setBounds(this.sOldBounds);
            Log.d("ImageUtil", "scale = " + scale);
            Log.d("ImageUtil", "sOldBounds = " + this.sOldBounds);
            computeOutlineRect(icon, iconRect);
            iconCenter[0] = iconRect.left + (iconRect.width() / 2);
            iconCenter[1] = iconRect.top + (iconRect.height() / 2);
            maskCenter[0] = this.sMaskRect.left + (this.sMaskRect.width() / 2);
            maskCenter[1] = this.sMaskRect.top + (this.sMaskRect.height() / 2);
            offsetX = iconCenter[0] - maskCenter[0];
            offsetY = iconCenter[1] - maskCenter[1];
            finalIcon = Bitmap.createBitmap(this.sMaskBitmap.getWidth(), this.sMaskBitmap.getHeight(), Config.ARGB_8888);
            canvas.restoreToCount(canvasStatus1);
            canvas.setBitmap(finalIcon);
            canvas.drawBitmap(icon, 0.0f, 0.0f, mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawBitmap(this.sMaskBitmap, (float) offsetX, (float) offsetY, mPaint);
        } else {
            float scaleX = (((float) this.sIconBgRect.width()) * sIconZoomFactor) / ((float) iconRect.width());
            float scaleY = (((float) this.sIconBgRect.height()) * sIconZoomFactor) / ((float) iconRect.height());
            if (((float) iconRadius) * scaleX > ((float) this.sIconBgRadius) || ((float) iconRadius) * scaleY > ((float) this.sIconBgRadius)) {
                iconBitmap = restoreBitmap(iconBitmap);
                Drawable bitmapDrawable = new BitmapDrawable(iconBitmap);
            }
            icon = Bitmap.createBitmap((int) (((float) iconBitmap.getWidth()) * scaleX), (int) (((float) iconBitmap.getHeight()) * scaleY), Config.ARGB_8888);
            canvas.setBitmap(icon);
            this.sOldBounds.set(srcIconDrawable.getBounds());
            Log.d("ImageUtil", "scaleX = " + scaleX + " scaleY = " + scaleY);
            Log.d("ImageUtil", "sOldBounds = " + this.sOldBounds);
            srcIconDrawable.setBounds(0, 0, (int) (((float) iconBitmap.getWidth()) * scaleX), (int) (((float) iconBitmap.getHeight()) * scaleY));
            srcIconDrawable.draw(canvas);
            srcIconDrawable.setBounds(this.sOldBounds);
            computeOutlineRect(icon, iconRect);
            iconCenter[0] = iconRect.left + (iconRect.width() / 2);
            iconCenter[1] = iconRect.top + (iconRect.height() / 2);
            maskCenter[0] = this.sMaskRect.left + (this.sMaskRect.width() / 2);
            maskCenter[1] = this.sMaskRect.top + (this.sMaskRect.height() / 2);
            offsetX = maskCenter[0] - iconCenter[0];
            offsetY = maskCenter[1] - iconCenter[1];
            finalIcon = Bitmap.createBitmap(this.sMaskBitmap.getWidth(), this.sMaskBitmap.getHeight(), Config.ARGB_8888);
            canvas.restoreToCount(canvasStatus1);
            canvas.setBitmap(finalIcon);
            canvas.drawBitmap(this.sMaskBitmap, 0.0f, 0.0f, mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(icon, (float) offsetX, (float) offsetY, mPaint);
        }
        computeOutlineRect(finalIcon, iconRect);
        iconCenter[0] = iconRect.left + (iconRect.width() / 2);
        iconCenter[1] = iconRect.top + (iconRect.height() / 2);
        iconBgCenter[0] = this.sIconBgRect.left + (this.sIconBgRect.width() / 2);
        iconBgCenter[1] = this.sIconBgRect.top + (this.sIconBgRect.height() / 2);
        offsetX = iconBgCenter[0] - iconCenter[0];
        offsetY = iconBgCenter[1] - iconCenter[1];
        Bitmap mOut = Bitmap.createBitmap(iconBgBitmap.getWidth(), iconBgBitmap.getHeight(), Config.ARGB_8888);
        canvas.restoreToCount(canvasStatus1);
        canvas.setBitmap(mOut);
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        if (iconRadius != -1) {
            canvas.drawBitmap(finalIcon, (float) offsetX, (float) offsetY, mPaint);
        } else {
            canvas.drawBitmap(iconBgBitmap, 0.0f, 0.0f, mPaint);
            canvas.drawBitmap(finalIcon, (float) offsetX, (float) offsetY, mPaint);
        }
        return mOut;
    }

    private Bitmap restoreBitmap(Bitmap bitmap) {
        int i;
        int j;
        int color;
        int k;
        int[] pixels = new int[(bitmap.getWidth() * bitmap.getHeight())];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (i = 0; i < height; i++) {
            j = 0;
            while (j < width) {
                if (Color.alpha(pixels[(width * i) + j]) > 200) {
                    color = pixels[(width * i) + j];
                    for (k = 0; k < j; k++) {
                        pixels[(width * i) + k] = color;
                    }
                } else {
                    j++;
                }
            }
        }
        for (i = 0; i < height; i++) {
            j = width - 1;
            while (j >= 0) {
                if (Color.alpha(pixels[(width * i) + j]) > 200) {
                    color = pixels[(width * i) + j];
                    for (k = width - 1; k > j; k--) {
                        pixels[(width * i) + k] = color;
                    }
                } else {
                    j--;
                }
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private Bitmap createNonDefalutBitmap(Context context, Drawable srcIcon, Rect iconRect, Bitmap maskBitmap, Drawable iconBg, float scale, float iconRadius) {
        int left;
        int top;
        float factScale;
        Bitmap maskedBitmap = Bitmap.createBitmap(this.sIconBgWidth, this.sIconBgHeight, Config.ARGB_8888);
        Canvas maskCanvas = new Canvas();
        maskCanvas.setBitmap(maskedBitmap);
        float maskWidthScale = (((float) this.sIconBgWidth) * 1.0f) / ((float) maskBitmap.getWidth());
        float maskHeightScale = (((float) this.sIconBgWidth) * 1.0f) / ((float) maskBitmap.getHeight());
        Rect maskRect = new Rect();
        maskRect.set((int) (((float) this.sMaskRect.left) * maskWidthScale), (int) (((float) this.sMaskRect.top) * maskHeightScale), (int) (((float) this.sMaskRect.right) * maskWidthScale), (int) (((float) this.sMaskRect.bottom) * maskHeightScale));
        if (((float) this.sMaskRect.width()) * maskWidthScale > ((float) this.sIconWithBgWidth) && ((float) this.sMaskRect.height()) * maskHeightScale > ((float) this.sIconWithBgHeight)) {
            left = ((this.sIconBgWidth - this.sIconWithBgWidth) / 2) + this.sIconLeftOffset;
            top = ((this.sIconBgHeight - this.sIconWithBgHeight) / 2) + this.sIconTopOffset;
            maskRect.set(left, top, this.sIconWithBgWidth + left, this.sIconWithBgHeight + top);
        }
        if (iconRadius != -1.0f) {
            if (scale <= 0.0f) {
                scale = 1.1f;
            }
        } else if (scale <= 0.0f) {
            scale = 0.85f;
        }
        if (((double) scale) == 1.05d) {
            factScale = Math.max((((float) maskRect.height()) * scale) / ((float) iconRect.height()), (((float) maskRect.width()) * scale) / ((float) iconRect.width()));
        } else {
            factScale = Math.min((((float) maskRect.height()) * scale) / ((float) iconRect.height()), (((float) maskRect.width()) * scale) / ((float) iconRect.width()));
        }
        float marginLeft = ((float) ((maskRect.width() / 2) + maskRect.left)) - (((float) ((iconRect.width() / 2) + iconRect.left)) * factScale);
        float marginTop = ((float) ((maskRect.height() / 2) + maskRect.top)) - (((float) ((iconRect.height() / 2) + iconRect.top)) * factScale);
        Bitmap iconBitmap = drawableToBitmap(srcIcon);
        int iconWidth = iconBitmap.getWidth();
        int iconHeight = iconBitmap.getHeight();
        Log.d("ImageUtil", "factScale = " + factScale);
        Log.d("ImageUtil", "iconWidth = " + iconWidth + " iconHeight = " + iconHeight);
        float targetWidth = factScale * ((float) iconWidth);
        float targetHeight = factScale * ((float) iconHeight);
        this.sOldBounds.set(srcIcon.getBounds());
        srcIcon.setBounds((int) marginLeft, (int) marginTop, (int) Math.ceil((double) (marginLeft + targetWidth)), (int) Math.ceil((double) (marginTop + targetHeight)));
        srcIcon.draw(maskCanvas);
        srcIcon.setBounds(this.sOldBounds);
        Matrix matrix = new Matrix();
        Paint paint = new Paint(1);
        matrix.postScale(maskWidthScale, maskHeightScale);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        maskCanvas.drawBitmap(maskBitmap, matrix, paint);
        Bitmap bitmap = Bitmap.createBitmap(this.sIconTextureWidth, this.sIconTextureHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        left = (this.sIconTextureWidth - this.sIconBgWidth) / 2;
        top = (this.sIconTextureHeight - this.sIconBgHeight) / 2;
        this.sOldBounds.set(iconBg.getBounds());
        iconBg.setBounds(left, top, this.sIconBgWidth + left, this.sIconBgHeight + top);
        iconBg.draw(canvas);
        iconBg.setBounds(this.sOldBounds);
        canvas.drawBitmap(maskedBitmap, (float) left, (float) top, null);
        Drawable iconBorder = context.getResources().getDrawable(R.drawable.vigour_icon_cover);
        iconBorder.setBounds(left, top, this.sIconBgWidth + left, this.sIconBgHeight + top);
        iconBorder.draw(canvas);
        canvas.setBitmap(null);
        maskCanvas.setBitmap(null);
        maskedBitmap.recycle();
        return bitmap;
    }

    private static boolean isActivePix(int color) {
        if (color < 0) {
            return true;
        }
        return false;
    }

    private static int[] getFitableRector(int xStart, int yStart, int LRdirection, int TBdirection, boolean rowFirst, int[] pix, int width, int height) {
        int xEnd = 0;
        int yEnd = 0;
        boolean sXIncrementally = false;
        boolean sYIncrementally = false;
        int[] rector = new int[2];
        if (LRdirection == 1001 && TBdirection == 1003) {
            xEnd = width;
            yEnd = height;
            sXIncrementally = true;
            sYIncrementally = true;
        } else if (LRdirection == 1001 && TBdirection == 1004) {
            xEnd = width;
            yEnd = 0;
            sXIncrementally = true;
            sYIncrementally = false;
        } else if (LRdirection == 1002 && TBdirection == 1003) {
            xEnd = 0;
            yEnd = height;
            sXIncrementally = false;
            sYIncrementally = true;
        } else if (LRdirection == 1002 && TBdirection == 1004) {
            xEnd = 0;
            yEnd = 0;
            sXIncrementally = false;
            sYIncrementally = false;
        }
        boolean sFetch = false;
        int x;
        int y;
        if (rowFirst) {
            if (sXIncrementally) {
                for (x = xStart; x < xEnd; x++) {
                    if (sYIncrementally) {
                        for (y = yStart; y < yEnd; y++) {
                            if (isActivePix(pix[(y * width) + x])) {
                                rector[0] = x;
                                rector[1] = y;
                                sFetch = true;
                                break;
                            }
                        }
                    } else {
                        for (y = yStart; y >= yEnd; y--) {
                            if (isActivePix(pix[(y * width) + x])) {
                                rector[0] = x;
                                rector[1] = y;
                                sFetch = true;
                                break;
                            }
                        }
                    }
                    if (sFetch) {
                        break;
                    }
                }
            } else {
                for (x = xStart; x >= xEnd; x--) {
                    if (sYIncrementally) {
                        for (y = yStart; y < yEnd; y++) {
                            if (isActivePix(pix[(y * width) + x])) {
                                rector[0] = x;
                                rector[1] = y;
                                sFetch = true;
                                break;
                            }
                        }
                    } else {
                        for (y = yStart; y >= yEnd; y--) {
                            if (isActivePix(pix[(y * width) + x])) {
                                rector[0] = x;
                                rector[1] = y;
                                sFetch = true;
                                break;
                            }
                        }
                    }
                    if (sFetch) {
                        break;
                    }
                }
            }
        } else if (sYIncrementally) {
            for (y = yStart; y < yEnd; y++) {
                if (sXIncrementally) {
                    for (x = xStart; x < xEnd; x++) {
                        if (isActivePix(pix[(y * width) + x])) {
                            rector[0] = x;
                            rector[1] = y;
                            sFetch = true;
                            break;
                        }
                    }
                } else {
                    for (x = xStart; x >= xEnd; x--) {
                        if (isActivePix(pix[(y * width) + x])) {
                            rector[0] = x;
                            rector[1] = y;
                            sFetch = true;
                            break;
                        }
                    }
                }
                if (sFetch) {
                    break;
                }
            }
        } else {
            for (y = yStart; y >= yEnd; y--) {
                if (sXIncrementally) {
                    for (x = xStart; x < xEnd; x++) {
                        if (isActivePix(pix[(y * width) + x])) {
                            rector[0] = x;
                            rector[1] = y;
                            sFetch = true;
                            break;
                        }
                    }
                } else {
                    for (x = xStart; x >= xEnd; x--) {
                        if (isActivePix(pix[(y * width) + x])) {
                            rector[0] = x;
                            rector[1] = y;
                            sFetch = true;
                            break;
                        }
                    }
                }
                if (sFetch) {
                    break;
                }
            }
        }
        return rector;
    }

    private void clearInitFlags(int themeId) {
        this.sInit = false;
        this.sMaskBitmap = null;
        this.sIconBg = null;
        mOldThemeId = themeId;
        this.sIconWidth = -1;
        this.sIconHeight = -1;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public Bitmap getCloneBitmap(Drawable drawable, Context context) {
        return getCloneBitmap(drawable, context, true);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public Bitmap getCloneBitmap(Drawable drawable, Context context, boolean themeRelated) {
        Bitmap cloneFlagBitmap;
        Bitmap sourceBitmap;
        float finalScaleWidth;
        float finalScaleHeight;
        String theme_id = System.getString(context.getContentResolver(), "theme_id");
        if (theme_id == null || "2".equals(theme_id)) {
            cloneFlagBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vigour_corner_clone_theme_default);
        } else {
            cloneFlagBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vigour_corner_clone_theme_global);
        }
        int width = cloneFlagBitmap.getWidth();
        int height = cloneFlagBitmap.getHeight();
        if (themeRelated) {
            sourceBitmap = createRedrawIconBitmap(drawable);
        } else {
            sourceBitmap = drawableToBitmap(drawable);
        }
        int x = sourceBitmap.getWidth();
        int y = sourceBitmap.getHeight();
        float srcOffset = (float) getVisibleOffset(sourceBitmap);
        float srcOffsetX = ((float) x) - srcOffset;
        float srcOffsetY = ((float) y) - srcOffset;
        float scaleWidth = ((float) x) / ((float) width);
        float scaleHeight = ((float) y) / ((float) height);
        int visibleCloneWidth = getVisibleOffset(cloneFlagBitmap);
        float cloneOffsetWidth = ((float) (width - visibleCloneWidth)) * scaleWidth;
        float cloneOffsetHeight = ((float) (height - visibleCloneWidth)) * scaleHeight;
        if (themeRelated) {
            finalScaleWidth = scaleWidth + ((cloneOffsetWidth - srcOffsetX) / ((float) x));
            finalScaleHeight = scaleHeight + ((cloneOffsetHeight - srcOffsetY) / ((float) y));
            if (x > width) {
                finalScaleWidth = (float) (((double) finalScaleWidth) + 0.01d);
                finalScaleHeight = (float) (((double) finalScaleHeight) + 0.01d);
            }
        } else {
            finalScaleWidth = ((((float) width) * scaleWidth) * (srcOffset / (((float) x) - cloneOffsetWidth))) / ((float) width);
            finalScaleHeight = ((((float) height) * scaleHeight) * (srcOffset / (((float) y) - cloneOffsetHeight))) / ((float) height);
        }
        Matrix matrix = new Matrix();
        matrix.postScale(finalScaleWidth, finalScaleHeight);
        cloneFlagBitmap = Bitmap.createBitmap(cloneFlagBitmap, 0, 0, width, height, matrix, true);
        Bitmap newbit = Bitmap.createBitmap(x, y, Config.ARGB_8888);
        Canvas canvas = new Canvas(newbit);
        Paint paint = new Paint();
        canvas.drawBitmap(sourceBitmap, 0.0f, 0.0f, paint);
        canvas.drawBitmap(cloneFlagBitmap, 1.5f, 0.0f, paint);
        canvas.save(31);
        canvas.restore();
        cloneFlagBitmap.recycle();
        return newbit;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int getVisibleOffset(Bitmap toMeasure) {
        int dWidht = toMeasure.getWidth();
        int dHeight = toMeasure.getHeight();
        int[] pix = new int[(dWidht * dHeight)];
        toMeasure.getPixels(pix, 0, dWidht, 0, 0, dWidht, dHeight);
        int[] offsetX = getFitableRector(dWidht - 1, dHeight - 1, 1002, 1004, false, pix, dWidht, dHeight);
        return Math.max(offsetX[0], offsetX[1]);
    }

    private boolean isExport() {
        return SystemProperties.get("ro.vivo.product.overseas", "no").equals("yes");
    }

    public static boolean isOpenThemeStyle() {
        File xmlFile = new File(VivoTheme.getThemePath() + "description.xml");
        if (!xmlFile.exists()) {
            return false;
        }
        boolean openTheme;
        try {
            InputStream mInputStream = new FileInputStream(xmlFile);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            XmlFileReadHandler mXmlFileReadHandler = new XmlFileReadHandler();
            parser.parse(mInputStream, mXmlFileReadHandler);
            openTheme = Boolean.parseBoolean((String) mXmlFileReadHandler.getThemeInfo().get("open"));
        } catch (Exception e) {
            openTheme = false;
        }
        return openTheme;
    }

    private static int getFactIconSize(Bitmap bitmap, int direction) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pix = new int[(width * height)];
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);
        int size = 0;
        int x;
        int y;
        if (direction == 1001) {
            for (x = 0; x < width; x++) {
                for (y = 0; y < height; y++) {
                    if (isActivePix(pix[(y * width) + x])) {
                        size = x;
                        break;
                    }
                }
            }
        } else if (direction == 1002) {
            for (x = width - 1; x >= 0; x--) {
                for (y = 0; y < height; y++) {
                    if (isActivePix(pix[(y * width) + x])) {
                        size = x;
                        break;
                    }
                }
            }
        } else if (direction == 1003) {
            for (y = 0; y < height; y++) {
                x = 0;
                while (y < width) {
                    try {
                        if (isActivePix(pix[(y * width) + x])) {
                            size = y;
                            break;
                        }
                        x++;
                    } catch (Exception e) {
                    }
                }
            }
        } else if (direction == 1004) {
            for (y = height - 1; y >= 0; y--) {
                x = 0;
                while (x < width) {
                    try {
                        if (isActivePix(pix[(y * width) + x])) {
                            size = y;
                            break;
                        }
                        x++;
                    } catch (Exception e2) {
                    }
                }
            }
        }
        return size;
    }
}
