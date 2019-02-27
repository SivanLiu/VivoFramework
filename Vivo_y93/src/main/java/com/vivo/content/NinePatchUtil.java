package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class NinePatchUtil {
    private static final int NO_COLOR = 1;
    private static final String TAG = "NinePatchUtil";

    static class NinePatchChunk {
        public static final int NO_COLOR = 1;
        public static final int TRANSPARENT_COLOR = 0;
        public int[] mColor;
        public int[] mDivX;
        public int[] mDivY;
        public final Rect mPaddings = new Rect();

        NinePatchChunk() {
        }

        private static void readIntArray(int[] data, ByteBuffer buffer) {
            int n = data.length;
            for (int i = 0; i < n; i++) {
                data[i] = buffer.getInt();
            }
        }

        private static void checkDivCount(int length) {
            if (length == 0 || (length & 1) != 0) {
                throw new RuntimeException("invalid nine-patch: " + length);
            }
        }

        public static NinePatchChunk deserialize(byte[] data) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
            if (byteBuffer.get() == (byte) 0) {
                return null;
            }
            NinePatchChunk chunk = new NinePatchChunk();
            chunk.mDivX = new int[byteBuffer.get()];
            chunk.mDivY = new int[byteBuffer.get()];
            chunk.mColor = new int[byteBuffer.getInt()];
            byteBuffer.get();
            byteBuffer.getInt();
            chunk.mPaddings.left = byteBuffer.getInt();
            chunk.mPaddings.right = byteBuffer.getInt();
            chunk.mPaddings.top = byteBuffer.getInt();
            chunk.mPaddings.bottom = byteBuffer.getInt();
            byteBuffer.getInt();
            readIntArray(chunk.mDivX, byteBuffer);
            readIntArray(chunk.mDivY, byteBuffer);
            readIntArray(chunk.mColor, byteBuffer);
            return chunk;
        }
    }

    private NinePatchUtil() {
    }

    public static Drawable decodeDrawableFromAsset(Context context, String assetPath) throws Exception {
        Bitmap bm = decodeFromAsset(context, assetPath);
        if (bm.getNinePatchChunk() == null) {
            return new BitmapDrawable(bm);
        }
        Rect padding = new Rect();
        readPaddingFromChunk(bm.getNinePatchChunk(), padding);
        return new NinePatchDrawable(context.getResources(), bm, bm.getNinePatchChunk(), padding, null);
    }

    public static Bitmap decodeFromStream(InputStream in) throws Exception {
        Bitmap srcBm = BitmapFactory.decodeStream(in);
        byte[] chunk = readChunk(srcBm);
        if (!NinePatch.isNinePatchChunk(chunk)) {
            return srcBm;
        }
        Bitmap tgtBm = Bitmap.createBitmap(srcBm, 1, 1, srcBm.getWidth() - 2, srcBm.getHeight() - 2);
        srcBm.recycle();
        Field f = tgtBm.getClass().getDeclaredField("mNinePatchChunk");
        f.setAccessible(true);
        f.set(tgtBm, chunk);
        return tgtBm;
    }

    public static Drawable decodeDrawableFromStream(InputStream in, Resources res, int id, String file) throws Exception {
        if (!file.endsWith(".9.png") || isNotNinePatchDrawable(res, id, file)) {
            return null;
        }
        Bitmap srcBm = BitmapFactory.decodeStream(in);
        if (srcBm == null) {
            return null;
        }
        byte[] chunk = readChunk(srcBm);
        if (!NinePatch.isNinePatchChunk(chunk) || !file.endsWith(".9.png")) {
            return null;
        }
        Bitmap tgtBm = Bitmap.createBitmap(srcBm, 1, 1, srcBm.getWidth() - 2, srcBm.getHeight() - 2);
        srcBm.recycle();
        Field f = tgtBm.getClass().getDeclaredField("mNinePatchChunk");
        f.setAccessible(true);
        f.set(tgtBm, chunk);
        readPaddingFromChunk(chunk, new Rect());
        return new NinePatchDrawable(res, tgtBm, chunk, NinePatchChunk.deserialize(chunk).mPaddings, null);
    }

    public static Bitmap decodeBitmap(Bitmap srcBm) throws Exception {
        byte[] chunk = readChunk(srcBm);
        if (!NinePatch.isNinePatchChunk(chunk)) {
            return srcBm;
        }
        Bitmap tgtBm = Bitmap.createBitmap(srcBm, 1, 1, srcBm.getWidth() - 2, srcBm.getHeight() - 2);
        srcBm.recycle();
        Field f = tgtBm.getClass().getDeclaredField("mNinePatchChunk");
        f.setAccessible(true);
        f.set(tgtBm, chunk);
        return tgtBm;
    }

    public static Bitmap decodeFromFile(String path) throws Exception {
        InputStream in = new FileInputStream(path);
        Bitmap bm = decodeFromStream(in);
        in.close();
        return bm;
    }

    public static Bitmap decodeFromAsset(Context context, String ninePatchPngPath) throws Exception {
        InputStream is = context.getAssets().open(ninePatchPngPath);
        Bitmap bm = decodeFromStream(is);
        is.close();
        return bm;
    }

    public static void readPaddingFromChunk(byte[] chunk, Rect paddingRect) {
        paddingRect.left = getInt(chunk, 12);
        paddingRect.right = getInt(chunk, 16);
        paddingRect.top = getInt(chunk, 20);
        paddingRect.bottom = getInt(chunk, 24);
    }

    public static byte[] readChunk(Bitmap yuantuBmp) throws IOException {
        int i;
        int BM_W = yuantuBmp.getWidth();
        int BM_H = yuantuBmp.getHeight();
        int xPointCount = 0;
        int yPointCount = 0;
        OutputStream ooo = new ByteArrayOutputStream();
        for (i = 0; i < 32; i++) {
            ooo.write(0);
        }
        int[] pixelsTop = new int[(BM_W - 2)];
        yuantuBmp.getPixels(pixelsTop, 0, BM_W, 1, 0, BM_W - 2, 1);
        boolean topFirstPixelIsBlack = pixelsTop[0] == -16777216;
        boolean topLastPixelIsBlack = pixelsTop[pixelsTop.length + -1] == -16777216;
        int tmpLastColor = 0;
        int len = pixelsTop.length;
        for (i = 0; i < len; i++) {
            if (tmpLastColor != pixelsTop[i]) {
                xPointCount++;
                writeInt(ooo, i);
                tmpLastColor = pixelsTop[i];
            }
        }
        if (topLastPixelIsBlack) {
            xPointCount++;
            writeInt(ooo, pixelsTop.length);
        }
        int xBlockCount = xPointCount + 1;
        if (topFirstPixelIsBlack) {
            xBlockCount--;
        }
        if (topLastPixelIsBlack) {
            xBlockCount--;
        }
        int[] pixelsLeft = new int[(BM_H - 2)];
        yuantuBmp.getPixels(pixelsLeft, 0, 1, 0, 1, 1, BM_H - 2);
        boolean firstPixelIsBlack = pixelsLeft[0] == -16777216;
        boolean lastPixelIsBlack = pixelsLeft[pixelsLeft.length + -1] == -16777216;
        tmpLastColor = 0;
        len = pixelsLeft.length;
        for (i = 0; i < len; i++) {
            if (tmpLastColor != pixelsLeft[i]) {
                yPointCount++;
                writeInt(ooo, i);
                tmpLastColor = pixelsLeft[i];
            }
        }
        if (lastPixelIsBlack) {
            yPointCount++;
            writeInt(ooo, pixelsLeft.length);
        }
        int yBlockCount = yPointCount + 1;
        if (firstPixelIsBlack) {
            yBlockCount--;
        }
        if (lastPixelIsBlack) {
            yBlockCount--;
        }
        for (i = 0; i < xBlockCount * yBlockCount; i++) {
            writeInt(ooo, 1);
        }
        ByteBuffer buf = ByteBuffer.wrap(ooo.toByteArray()).order(ByteOrder.nativeOrder());
        buf.put((byte) 1);
        buf.put((byte) xPointCount);
        buf.put((byte) yPointCount);
        buf.putInt(xBlockCount * yBlockCount);
        byte[] data = buf.array();
        dealPaddingInfo(yuantuBmp, data);
        return data;
    }

    private static void dealPaddingInfo(Bitmap bm, byte[] data) {
        int i;
        int[] bottomPixels = new int[(bm.getWidth() - 2)];
        bm.getPixels(bottomPixels, 0, bottomPixels.length, 1, bm.getHeight() - 1, bottomPixels.length, 1);
        for (i = 0; i < bottomPixels.length; i++) {
            if (-16777216 == bottomPixels[i]) {
                writeInt(data, 12, i);
                break;
            }
        }
        i = bottomPixels.length - 1;
        int j = 0;
        while (i >= 0) {
            if (-16777216 == bottomPixels[i]) {
                writeInt(data, 16, j);
                break;
            } else {
                i--;
                j++;
            }
        }
        int[] rightPixels = new int[(bm.getHeight() - 2)];
        bm.getPixels(rightPixels, 0, 1, bm.getWidth() - 1, 1, 1, rightPixels.length);
        for (i = 0; i < rightPixels.length; i++) {
            if (-16777216 == rightPixels[i]) {
                writeInt(data, 20, i);
                break;
            }
        }
        i = rightPixels.length - 1;
        j = 0;
        while (i >= 0) {
            if (-16777216 == rightPixels[i]) {
                writeInt(data, 24, j);
                return;
            } else {
                i--;
                j++;
            }
        }
    }

    private static void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >> 0) & 255);
        out.write((v >> 8) & 255);
        out.write((v >> 16) & 255);
        out.write((v >> 24) & 255);
    }

    private static void writeInt(byte[] b, int offset, int v) {
        b[offset + 0] = (byte) (v >> 0);
        b[offset + 1] = (byte) (v >> 8);
        b[offset + 2] = (byte) (v >> 16);
        b[offset + 3] = (byte) (v >> 24);
    }

    private static int getInt(byte[] bs, int from) {
        return (((bs[from + 1] << 8) | bs[from + 0]) | (bs[from + 2] << 16)) | (bs[from + 3] << 24);
    }

    public static void printChunkInfo(Bitmap bm) {
        byte[] chunk = bm.getNinePatchChunk();
        if (chunk == null) {
            System.out.println("can't find chunk info from this bitmap(" + bm + ")");
            return;
        }
        int i;
        int xLen = chunk[1];
        int yLen = chunk[2];
        int cLen = chunk[3];
        StringBuilder sb = new StringBuilder();
        int peddingLeft = getInt(chunk, 12);
        int paddingRight = getInt(chunk, 16);
        int paddingTop = getInt(chunk, 20);
        int paddingBottom = getInt(chunk, 24);
        sb.append("peddingLeft=").append(peddingLeft);
        sb.append("\r\n");
        sb.append("paddingRight=").append(paddingRight);
        sb.append("\r\n");
        sb.append("paddingTop=").append(paddingTop);
        sb.append("\r\n");
        sb.append("paddingBottom=").append(paddingBottom);
        sb.append("\r\n");
        sb.append("x info=");
        for (i = 0; i < xLen; i++) {
            sb.append(",").append(getInt(chunk, (i * 4) + 32));
        }
        sb.append("\r\n");
        sb.append("y info=");
        for (i = 0; i < yLen; i++) {
            sb.append(",").append(getInt(chunk, ((xLen * 4) + 32) + (i * 4)));
        }
        sb.append("\r\n");
        sb.append("color info=");
        for (i = 0; i < cLen; i++) {
            sb.append(",").append(getInt(chunk, (((xLen * 4) + (yLen * 4)) + 32) + (i * 4)));
        }
        System.err.println("" + sb);
    }

    private static boolean isNotNinePatchDrawable(Resources res, int id, String file) {
        if (res.getResourcePackageName(id).equals("com.android.settings") && file.endsWith("header_category_background.9.png")) {
            return true;
        }
        return false;
    }
}
