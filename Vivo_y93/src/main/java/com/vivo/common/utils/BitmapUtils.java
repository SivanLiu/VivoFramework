package com.vivo.common.utils;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapUtils {
    private static final int GRAY_THRESHOLD = 180;
    private static final String TAG = "BitmapUtils";

    public static boolean isBitmapWhiteStyle(Bitmap bitmap) {
        boolean result;
        int grayvalue = getGrayValue(bitmap);
        if (grayvalue < GRAY_THRESHOLD) {
            result = false;
        } else {
            result = true;
        }
        Log.i("VivoWallpaperManager", " grayVale:" + grayvalue + " result:" + result);
        return result;
    }

    public static int getGrayValue(Bitmap bitmap) {
        long gray = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (bitmap != null) {
            for (int i = 0; i < width; i += 2) {
                for (int j = 0; j < height; j += 2) {
                    int color = bitmap.getPixel(i, j);
                    gray += (long) (((red(color) * 229) + (green(color) * 587)) + (blue(color) * 114));
                }
            }
        }
        Log.i("VivoWallpaperManager", "grayVale  width:" + width + " height:" + height + " gray:" + gray);
        return (int) (gray / ((long) ((width * height) * 250)));
    }

    public static boolean isBitmapWhiteStyle(Bitmap bitmap, int startX, int startY, int width, int height, float density) {
        boolean result;
        int grayvalue = getGrayValue(bitmap, startX, startY, width, height, density);
        if (grayvalue < 230) {
            result = false;
        } else {
            result = true;
        }
        Log.i("VivoWallpaperManager", " new-grayVale:" + grayvalue + " result:" + result + " density:" + density);
        return result;
    }

    public static int getGrayValue(Bitmap bitmap, int startX, int startY, int width, int height, float density) {
        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        }
        if (width <= 0 || height <= 0) {
            return 0;
        }
        int gray = 0;
        int count = 0;
        int deltaX = (int) (8.0f * density);
        int deltaY = (int) (8.0f * density);
        if (width <= deltaX) {
            deltaX = 1;
        }
        if (height <= deltaY) {
            deltaY = 1;
        }
        Log.i("VivoWallpaperManager", "bWidth:" + bitmap.getWidth() + " bHeight:" + bitmap.getHeight() + " startX:" + startX + " startY:" + startY + " windth:" + width + " height:" + height + " deltaX:" + deltaX + " deltaY:" + deltaY);
        int i = startX;
        while (i < startX + width && i < bitmap.getWidth()) {
            int j = startY;
            while (j < startY + height && j < bitmap.getHeight()) {
                int color = bitmap.getPixel(i, j);
                gray += (int) (((((float) red(color)) * 0.299f) + (((float) green(color)) * 0.587f)) + (((float) blue(color)) * 0.114f));
                count++;
                j += deltaY;
            }
            i += deltaX;
        }
        Log.i("VivoWallpaperManager", "gray:" + gray + " count:" + count);
        if (count == 0) {
            count = 1;
        }
        return gray / count;
    }

    private static int alpha(int color) {
        return color >>> 24;
    }

    private static int red(int color) {
        return (color >> 16) & 255;
    }

    private static int green(int color) {
        return (color >> 8) & 255;
    }

    private static int blue(int color) {
        return color & 255;
    }
}
