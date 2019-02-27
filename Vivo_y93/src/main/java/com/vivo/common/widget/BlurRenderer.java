package com.vivo.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import com.vivo.common.widget.GLTextureView.Renderer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BlurRenderer implements Renderer {
    private static final String TAG = "BlurRenderer";
    private Object mAnalyzeSyncObject = new Object();
    private Thread mAnalyzeThread = null;
    protected int mBlurRadius = 0;
    private ScriptIntrinsicBlur mBlurScript = null;
    protected Context mContext = null;
    protected IGpuRendererListener mGpuRendererListener = null;
    private long mHandle = 0;
    private Allocation mInputAllocation = null;
    private Allocation mOutputAllocation = null;
    private BlurRenderEngine mRenderEngine = null;
    private RenderScript mRenderScript = null;
    protected Bitmap mRenderSrcBitmap = null;
    protected Bitmap mRsBlurBitmap = null;
    protected Object mSyncObject = new Object();
    private final WeakReference<BlurRenderer> mThisWeakRef = new WeakReference(this);

    public interface IGpuRendererListener {
        void notifyAnalyzeDataFinished(int i);

        void notifyFirstFrameRenderFinished();

        void notifyFrameRenderFinished(int i);

        void notifyObtainAdjustBright();

        void notifyRenderAgain();

        void notifySurfaceChanged();
    }

    private static class AnalyzeThread extends Thread {
        private WeakReference<BlurRenderer> mGpuBlurRendererWeakRef;

        AnalyzeThread(WeakReference<BlurRenderer> gpuBaseRenderWeakRef) {
            this.mGpuBlurRendererWeakRef = gpuBaseRenderWeakRef;
        }

        /* JADX WARNING: Missing block: B:15:0x002e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            BlurRenderer render = (BlurRenderer) this.mGpuBlurRendererWeakRef.get();
            if (render != null) {
                synchronized (render.obtainAnalyzeObject()) {
                    long handle = render.obtainBlurHandle();
                    BlurRenderEngine engine = render.obtainRenderEngine();
                    if (engine == null) {
                        return;
                    }
                    int radius = engine.nativeAnalyzeImageData(handle);
                    IGpuRendererListener listener = render.getGpuRendererListener();
                    if (listener != null) {
                        listener.notifyAnalyzeDataFinished(radius);
                        listener.notifyObtainAdjustBright();
                    }
                }
            }
        }
    }

    public BlurRenderer(Context context, BlurRenderEngine engine) {
        this.mContext = context;
        this.mRenderEngine = engine;
    }

    public void setRenderEngine(BlurRenderEngine engine) {
        this.mRenderEngine = engine;
    }

    public void setBlurHandle(long handle) {
        this.mHandle = handle;
    }

    public Object getSyncObject() {
        return this.mSyncObject;
    }

    public void setGpuRendererListener(IGpuRendererListener listener) {
        this.mGpuRendererListener = listener;
    }

    private IGpuRendererListener getGpuRendererListener() {
        return this.mGpuRendererListener;
    }

    public void setRenderScript(RenderScript rs, ScriptIntrinsicBlur sBlur) {
        this.mRenderScript = rs;
        this.mBlurScript = sBlur;
    }

    public static void doStackBlur(Bitmap bitmap, int width, int height, int radius, float bright) {
        boolean isDebug = Log.isLoggable(TAG, 2);
        if (isDebug) {
            saveBitmapForDebug(bitmap, "doStackBlurBefore");
        }
        BlurRenderEngine.nativeDoStackBlur(bitmap, width, height, radius, bright);
        if (isDebug) {
            saveBitmapForDebug(bitmap, "doStackBlurAfter");
        }
    }

    protected void doBlurByRenderScript(Bitmap blurBitmap, int radius) {
        if (blurBitmap != null && !blurBitmap.isRecycled() && this.mBlurScript != null && this.mOutputAllocation != null) {
            Log.e(TAG, "test gpu blur GpuTextureRenderer doBlurByRenderScript <--");
            try {
                this.mBlurScript.setRadius((float) radius);
                this.mBlurScript.forEach(this.mOutputAllocation);
                this.mOutputAllocation.copyTo(blurBitmap);
            } catch (Exception e) {
                Log.e(TAG, "test gpu blur GpuTextureRenderer doBlurByRenderScript e: " + e);
                e.printStackTrace();
            }
        }
    }

    private long obtainBlurHandle() {
        return this.mHandle;
    }

    private BlurRenderEngine obtainRenderEngine() {
        return this.mRenderEngine;
    }

    private Object obtainAnalyzeObject() {
        return this.mAnalyzeSyncObject;
    }

    public void setBlurRadius(int radius) {
        if (this.mRenderEngine != null) {
            this.mBlurRadius = radius;
            this.mRenderEngine.nativeSetBlurRadius(this.mHandle, radius);
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, float widthScaleRatio, float heightScaleRatio) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(widthScaleRatio, heightScaleRatio);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void setRenderSource(Bitmap bitmap, float wScaleRatio, float hScaleRatio) {
        Log.d(TAG, "test gpu blur GpuBaseRenderer setRenderSource bitmap: " + bitmap + ", mBlurScript: " + this.mBlurScript + ", mRenderScript: " + this.mRenderScript + ", mHandle: " + this.mHandle);
        if (this.mRenderEngine != null && bitmap != null && !bitmap.isRecycled() && this.mBlurScript != null) {
            if (this.mRenderSrcBitmap != null) {
                this.mRenderSrcBitmap.recycle();
                this.mRenderSrcBitmap = null;
            }
            this.mRenderSrcBitmap = scaleBitmap(bitmap, wScaleRatio, hScaleRatio);
            if (this.mRenderSrcBitmap == null) {
                Log.e(TAG, "test gpu blur GpuBaseRenderer setRenderSource mRenderSrcBitmap is null");
                return;
            }
            int width = this.mRenderSrcBitmap.getWidth();
            int height = this.mRenderSrcBitmap.getHeight();
            if (this.mRsBlurBitmap == null) {
                this.mRsBlurBitmap = Bitmap.createBitmap(width, height, this.mRenderSrcBitmap.getConfig());
            } else {
                int blurWidth = this.mRsBlurBitmap.getWidth();
                int blurHeight = this.mRsBlurBitmap.getHeight();
                if (!(blurWidth == width && blurHeight == height)) {
                    this.mRsBlurBitmap.recycle();
                    this.mRsBlurBitmap = null;
                    this.mRsBlurBitmap = Bitmap.createBitmap(width, height, this.mRenderSrcBitmap.getConfig());
                }
            }
            synchronized (this.mSyncObject) {
                if (this.mInputAllocation != null) {
                    this.mInputAllocation.destroy();
                    this.mInputAllocation = null;
                }
                this.mInputAllocation = Allocation.createFromBitmap(this.mRenderScript, this.mRenderSrcBitmap);
                this.mBlurScript.setInput(this.mInputAllocation);
                if (this.mOutputAllocation != null) {
                    this.mOutputAllocation.destroy();
                    this.mOutputAllocation = null;
                }
                this.mOutputAllocation = Allocation.createTyped(this.mRenderScript, this.mInputAllocation.getType());
                this.mRenderEngine.nativeSetRenderSource(this.mHandle, this.mRenderSrcBitmap, this.mRsBlurBitmap, width, height);
            }
            if (this.mAnalyzeThread != null) {
                try {
                    this.mAnalyzeThread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mAnalyzeThread = null;
            }
            this.mAnalyzeThread = new AnalyzeThread(this.mThisWeakRef);
            this.mAnalyzeThread.start();
        }
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "test gpu blur GpuTextureRenderer onSurfaceCreated mHandle: " + this.mHandle);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "test gpu blur GpuTextureRenderer onSurfaceChanged mHandle: " + this.mHandle + ", width: " + width + ", height: " + height + ", mRenderEngine: " + this.mRenderEngine + ", mGpuRendererListener: " + this.mGpuRendererListener);
        if (this.mRenderEngine != null) {
            this.mRenderEngine.nativeSurfaceChanged(this.mHandle, width, height);
            if (this.mGpuRendererListener != null) {
                this.mGpuRendererListener.notifySurfaceChanged();
            }
        }
    }

    public void onDrawFrame(GL10 gl) {
        if (this.mRenderEngine != null) {
            boolean isFirstRenderFrameBeforeRender;
            int renderedRadius;
            boolean needRenderAgain;
            synchronized (this.mSyncObject) {
                int currRadius = this.mBlurRadius;
                boolean isNeedRsBlur = this.mRenderEngine.nativeIsNeedRsBlur(this.mHandle, currRadius);
                if (isNeedRsBlur) {
                    doBlurByRenderScript(this.mRsBlurBitmap, currRadius);
                }
                boolean isDebug = Log.isLoggable(TAG, 2);
                this.mRenderEngine.nativeSetDebugState(this.mHandle, isDebug);
                if (isDebug) {
                    saveBitmapForDebug(this.mRsBlurBitmap, "RS_Blur");
                }
                isFirstRenderFrameBeforeRender = this.mRenderEngine.nativeIsFirstRenderFrame(this.mHandle);
                renderedRadius = this.mRenderEngine.nativeRender(this.mHandle);
                needRenderAgain = this.mRenderEngine.nativeNeedRenderAgain(this.mHandle);
                boolean isFirstRenderFrameAfterRender = this.mRenderEngine.nativeIsFirstRenderFrame(this.mHandle);
                Log.d(TAG, "test gpu blur GpuTextureRenderer onDrawFrame mBlurRadius: " + this.mBlurRadius + ", currRadius: " + currRadius + ", isNeedRsBlur: " + isNeedRsBlur + ", renderedRadius: " + renderedRadius + ", needRenderAgain: " + needRenderAgain + ", isFirstRenderFrameBeforeRender: " + isFirstRenderFrameBeforeRender + ", isFirstRenderFrameAfterRender: " + isFirstRenderFrameAfterRender + ", mHandle: " + this.mHandle);
            }
            if (this.mGpuRendererListener != null) {
                if (!isFirstRenderFrameBeforeRender && isFirstRenderFrameAfterRender) {
                    this.mGpuRendererListener.notifyFirstFrameRenderFinished();
                }
                if (needRenderAgain) {
                    this.mGpuRendererListener.notifyRenderAgain();
                }
                this.mGpuRendererListener.notifyFrameRenderFinished(renderedRadius);
            }
        }
    }

    private static void saveBitmapForDebug(Bitmap bitmap, String fileName) {
        Log.d(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug fileName: " + fileName + ", bitmap is not null: " + (bitmap != null));
        if (bitmap != null && !bitmap.isRecycled()) {
            File dir = new File("/storage/emulated/0/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(dir, fileName + ".jpg");
            Log.d(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug full name: " + f.toString());
            try {
                if (f.exists()) {
                    f.delete();
                } else {
                    f.createNewFile();
                }
            } catch (Exception e) {
                Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug error create file e: " + e.getMessage());
                e.printStackTrace();
            }
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(f);
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug error FileNotFoundException e: " + e2.getMessage());
                e2.printStackTrace();
            }
            try {
                bitmap.compress(CompressFormat.JPEG, 90, outputStream);
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug flush e: " + e3.getMessage());
                        e3.printStackTrace();
                    }
                }
            } catch (NullPointerException e4) {
                Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug compress e: " + e4.getMessage());
                e4.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug flush e: " + e32.getMessage());
                        e32.printStackTrace();
                    }
                }
            } catch (IllegalArgumentException e5) {
                Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug compress e: " + e5.getMessage());
                e5.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug flush e: " + e322.getMessage());
                        e322.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e3222) {
                        Log.e(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug flush e: " + e3222.getMessage());
                        e3222.printStackTrace();
                    }
                }
            }
            Log.d(TAG, "test gpu blur GpuTextureRenderer saveBitmapForDebug -->");
        }
    }

    public void onPause() {
        if (this.mAnalyzeThread != null) {
            try {
                this.mAnalyzeThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mAnalyzeThread = null;
        }
        this.mBlurRadius = 0;
        if (this.mRenderEngine != null) {
            this.mRenderEngine.nativePause(this.mHandle);
        }
    }

    public void release() {
        if (this.mInputAllocation != null) {
            this.mInputAllocation.destroy();
            this.mInputAllocation = null;
        }
        if (this.mOutputAllocation != null) {
            this.mOutputAllocation.destroy();
            this.mOutputAllocation = null;
        }
        this.mBlurScript = null;
        this.mRenderScript = null;
        if (this.mRenderSrcBitmap != null) {
            this.mRenderSrcBitmap.recycle();
            this.mRenderSrcBitmap = null;
        }
        if (this.mRsBlurBitmap != null) {
            this.mRsBlurBitmap.recycle();
            this.mRsBlurBitmap = null;
        }
        synchronized (this.mAnalyzeSyncObject) {
            this.mRenderEngine = null;
            this.mHandle = 0;
        }
    }
}
