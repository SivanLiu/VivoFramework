package vivo.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public final class BlurUtil {
    private static final boolean DEBUG = true;
    private static final float DEFAULT_SCALE = 3.0f;
    private static final int DEF_BLUR_BG_COLOR = 419430400;
    private static final float DEF_BLUR_RADIUS = 25.0f;
    private static final float MAX_BLUR_RADIUS = 25.0f;
    private static final float MIN_BLUR_RADIUS = 0.0f;
    private static final int MIN_SCALE_SIZE = 2;
    private static final String TAG = "BlurUtils";

    public static Bitmap blurBitmap(Context context, Bitmap inBitmap) {
        return blurBitmap(context, inBitmap, 25.0f, DEF_BLUR_BG_COLOR);
    }

    public static Bitmap blurBitmap(Context context, Bitmap inBitmap, float radius, int bgColor) {
        if (inBitmap == null) {
            return null;
        }
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allocation = null;
        Allocation allocation2 = null;
        float scale = context.getResources().getDisplayMetrics().density * DEFAULT_SCALE;
        Log.d(TAG, "default scale : 3.0  current scale : " + scale);
        try {
            inBitmap = scaleBitmap(inBitmap, 1.0f / scale);
            allocation = Allocation.createFromBitmap(rs, inBitmap);
            allocation2 = Allocation.createTyped(rs, allocation.getType());
            blurScript.setRadius(Math.max(Math.min(radius, 25.0f), MIN_BLUR_RADIUS));
            blurScript.setInput(allocation);
            blurScript.forEach(allocation2);
            allocation2.copyTo(inBitmap);
            if (allocation2 != null) {
                allocation2.destroy();
            }
            if (allocation != null) {
                allocation.destroy();
            }
        } catch (Exception e) {
            Log.e(TAG, "blurBitmp failed", e);
            inBitmap = null;
            if (allocation2 != null) {
                allocation2.destroy();
            }
            if (allocation != null) {
                allocation.destroy();
            }
        } catch (Throwable th) {
            if (allocation2 != null) {
                allocation2.destroy();
            }
            if (allocation != null) {
                allocation.destroy();
            }
        }
        blurScript.setInput(null);
        blurScript.destroy();
        rs.destroy();
        if (!(inBitmap == null || ((bgColor >> 24) & 255) == 0)) {
            new Canvas(inBitmap).drawColor(bgColor);
        }
        return inBitmap;
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        if (scale <= MIN_BLUR_RADIUS) {
            Log.e(TAG, "illegal scale factor : " + scale);
            return bitmap;
        }
        int scaleWidth = (int) (((float) bitmap.getWidth()) * scale);
        float adjScale = scale;
        if (((int) (((float) bitmap.getHeight()) * scale)) < 2 && scaleWidth < 2) {
            adjScale = (float) (2 / (bitmap.getWidth() + bitmap.getHeight()));
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, DEBUG);
    }
}
