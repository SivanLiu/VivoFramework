package com.vivo.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;

public class BlurFilter {
    private static final float BLUR_OVERLAY_SCALE = 0.05f;
    private static final int BLUR_RADIUS = 11;
    public static final int DIM_STYLE = 1;
    public static final int LIGHT_STYLE = 2;
    public static final int NO_STYLE = 0;
    private static final String TAG = "BlurFilter";
    private static float[] mArrayDim = new float[]{1.003373f, -0.575883f, -0.07749f, 0.0f, 41.275f, -0.291627f, 0.719117f, -0.07749f, 0.0f, 41.275f, -0.291627f, -0.575883f, 1.21751f, 0.0f, 41.275f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static float[] mArrayLight = new float[]{1.20742f, -0.18282f, -0.0246f, 0.0f, 45.0f, -0.09258f, 1.11718f, -0.0246f, 0.0f, 45.0f, -0.09258f, -0.18282f, 1.2754f, 0.0f, 45.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static float[] mArrayNone = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    public static void save(Bitmap bm) {
        if (bm == null) {
            Log.d(TAG, "no bitmap");
            return;
        }
        try {
            bm.compress(CompressFormat.PNG, 100, new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "blur.png"));
        } catch (Exception e) {
            Log.e(TAG, "error:", e);
        }
    }

    public static Bitmap blur(Bitmap src, int lightness, int radius, float scaled, boolean canBeReused) {
        long startMs = System.currentTimeMillis();
        Bitmap overlay = Bitmap.createBitmap((int) (((float) src.getWidth()) * scaled), (int) (((float) src.getHeight()) * scaled), Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(scaled, scaled);
        ColorMatrix lightnessMatrix = new ColorMatrix();
        if (lightness == 1) {
            lightnessMatrix.set(mArrayDim);
        } else if (lightness == 2) {
            lightnessMatrix.set(mArrayLight);
        } else if (lightness == 0) {
            lightnessMatrix.set(mArrayNone);
        }
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(lightnessMatrix));
        paint.setFlags(2);
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        overlay = doStackBlur(overlay, radius, canBeReused);
        Log.d(TAG, "blur time : " + (System.currentTimeMillis() - startMs) + "ms");
        return overlay;
    }

    private static Bitmap doBlurByRenderScript(Bitmap src, int radius, Context context) {
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, src, MipmapControl.MIPMAP_NONE, 1);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius((float) radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(src);
        script.setInput(null);
        script.destroy();
        input.destroy();
        output.destroy();
        rs.destroy();
        return src;
    }

    private static Bitmap doStackBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }
        if (radius < 1) {
            return null;
        }
        int i;
        int y;
        int bsum;
        int gsum;
        int rsum;
        int boutsum;
        int goutsum;
        int routsum;
        int binsum;
        int ginsum;
        int rinsum;
        int p;
        int[] sir;
        int rbs;
        int stackpointer;
        int x;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[(w * h)];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = (radius + radius) + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int[] vmin = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[(divsum * 256)];
        for (i = 0; i < divsum * 256; i++) {
            dv[i] = i / divsum;
        }
        int yi = 0;
        int yw = 0;
        int[][] stack = (int[][]) Array.newInstance(Integer.TYPE, new int[]{div, 3});
        int r1 = radius + 1;
        for (y = 0; y < h; y++) {
            bsum = 0;
            gsum = 0;
            rsum = 0;
            boutsum = 0;
            goutsum = 0;
            routsum = 0;
            binsum = 0;
            ginsum = 0;
            rinsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[Math.min(wm, Math.max(i, 0)) + yi];
                sir = stack[i + radius];
                sir[0] = (16711680 & p) >> 16;
                sir[1] = (65280 & p) >> 8;
                sir[2] = p & 255;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min((x + radius) + 1, wm);
                }
                p = pix[vmin[x] + yw];
                sir[0] = (16711680 & p) >> 16;
                sir[1] = (65280 & p) >> 8;
                sir[2] = p & 255;
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            bsum = 0;
            gsum = 0;
            rsum = 0;
            boutsum = 0;
            goutsum = 0;
            routsum = 0;
            binsum = 0;
            ginsum = 0;
            rinsum = 0;
            int yp = (-radius) * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (((pix[yi] & -16777216) | (dv[rsum] << 16)) | (dv[gsum] << 8)) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private static Bitmap doBoxBlur(Bitmap bmp, int mHRadius, int mVRadius, int mIterations) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        long temp = start;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[(width * height)];
        int[] outPixels = new int[(width * height)];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < mIterations; i++) {
            boxblur(inPixels, outPixels, width, height, (float) mHRadius);
            start = System.currentTimeMillis();
            boxblur(outPixels, inPixels, height, width, (float) mVRadius);
            start = System.currentTimeMillis();
        }
        blurFractional(inPixels, outPixels, width, height, (float) mHRadius);
        start = System.currentTimeMillis();
        blurFractional(outPixels, inPixels, height, width, (float) mVRadius);
        start = System.currentTimeMillis();
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static void boxblur(int[] in, int[] out, int width, int height, float radius) {
        int i;
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = (r * 2) + 1;
        int[] divide = new int[(tableSize * 256)];
        for (i = 0; i < tableSize * 256; i++) {
            divide[i] = i / tableSize;
        }
        int inIndex = 0;
        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0;
            int tr = 0;
            int tg = 0;
            int tb = 0;
            for (i = -r; i <= r; i++) {
                int rgb = in[clamp(i, 0, width - 1) + inIndex];
                ta += (rgb >> 24) & 255;
                tr += (rgb >> 16) & 255;
                tg += (rgb >> 8) & 255;
                tb += rgb & 255;
            }
            for (int x = 0; x < width; x++) {
                out[outIndex] = (((divide[ta] << 24) | (divide[tr] << 16)) | (divide[tg] << 8)) | divide[tb];
                int i1 = (x + r) + 1;
                if (i1 > widthMinus1) {
                    i1 = widthMinus1;
                }
                int i2 = x - r;
                if (i2 < 0) {
                    i2 = 0;
                }
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];
                ta += ((rgb1 >> 24) & 255) - ((rgb2 >> 24) & 255);
                tr += ((16711680 & rgb1) - (16711680 & rgb2)) >> 16;
                tg += ((65280 & rgb1) - (65280 & rgb2)) >> 8;
                tb += (rgb1 & 255) - (rgb2 & 255);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    private static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
        radius -= (float) ((int) radius);
        float f = 1.0f / ((2.0f * radius) + 1.0f);
        int inIndex = 0;
        for (int y = 0; y < height; y++) {
            int outIndex = y;
            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];
                int a2 = (rgb2 >> 24) & 255;
                int r2 = (rgb2 >> 16) & 255;
                int g2 = (rgb2 >> 8) & 255;
                int b2 = rgb2 & 255;
                int i2 = ((((int) (((float) (a2 + ((int) (((float) (((rgb1 >> 24) & 255) + ((rgb3 >> 24) & 255))) * radius)))) * f)) << 24) | (((int) (((float) (r2 + ((int) (((float) (((rgb1 >> 16) & 255) + ((rgb3 >> 16) & 255))) * radius)))) * f)) << 16)) | (((int) (((float) (g2 + ((int) (((float) (((rgb1 >> 8) & 255) + ((rgb3 >> 8) & 255))) * radius)))) * f)) << 8);
                out[outIndex] = i2 | ((int) (((float) (b2 + ((int) (((float) ((rgb1 & 255) + (rgb3 & 255))) * radius)))) * f));
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    private static int clamp(int x, int a, int b) {
        if (x < a) {
            return a;
        }
        return x > b ? b : x;
    }
}
