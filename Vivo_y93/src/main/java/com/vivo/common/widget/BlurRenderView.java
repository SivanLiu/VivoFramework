package com.vivo.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import com.vivo.common.widget.BlurRenderer.IGpuRendererListener;
import java.lang.ref.WeakReference;

public class BlurRenderView extends GLTextureView {
    private static final String TAG = "BlurRenderView";
    private float mAdjustBright = 0.0f;
    private float mBrightMin = 0.0f;
    private Context mContext = null;
    private GpuBlurHandler mGpuBlurHandler = null;
    private GpuRendererListener mGpuRendererListener = null;
    private long mHandle = 0;
    private boolean mIsRenderThreadLaunchFinished = false;
    private boolean mIsResumeState = true;
    private boolean mIsSetRenderSource = false;
    private OnRenderListener mOnRenderListener = null;
    private BlurRenderEngine mRenderEngine = null;
    private BlurRenderer mRenderer = null;
    private int mTargetScaledHeight = 0;
    private int mTargetScaledWidth = 0;
    private final WeakReference<BlurRenderView> mThisWeakRef = new WeakReference(this);
    private ViewAlphaChangedRunnable mViewAlphaChangedRunnable = null;
    private ViewRenderAgainRunnable mViewRenderAgainRunnable = null;

    private static class GpuBlurHandler extends Handler {
        /* synthetic */ GpuBlurHandler(GpuBlurHandler -this0) {
            this();
        }

        private GpuBlurHandler() {
        }

        public void handleMessage(Message msg) {
        }
    }

    private static class GpuRendererListener implements IGpuRendererListener {
        private WeakReference<BlurRenderView> mBlurRenderViewWeakRef = null;

        GpuRendererListener(WeakReference<BlurRenderView> vivoTextureRenderViewWeakRef) {
            this.mBlurRenderViewWeakRef = vivoTextureRenderViewWeakRef;
        }

        public void notifyAnalyzeDataFinished(int radius) {
            Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifyAnalyzeDataFinished <--");
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null && radius > 0) {
                boolean isRenderThreadLaunchFinished = view.isRenderThreadLaunchFinished();
                Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView GpuRendererListener isRenderThreadLaunchFinished: " + isRenderThreadLaunchFinished);
                if (isRenderThreadLaunchFinished) {
                    view.requestRender();
                }
            }
        }

        public void notifyObtainAdjustBright() {
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                long handle = view.obtainBlurHandle();
                BlurRenderEngine engine = view.obtainRenderEngine();
                if (engine != null) {
                    float bright = engine.nativeGetAdjustBright(handle);
                    Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifyObtainAdjustBright bright: " + bright + ", handle: " + handle);
                    view.setAdjustBright(bright);
                }
            }
        }

        public void notifySurfaceChanged() {
            Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifySurfaceChanged <--");
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifySurfaceChanged mHandle: " + view.obtainBlurHandle());
                view.setRenderThreadLaunchState(true);
                OnRenderListener listener = view.getRenderListener();
                if (listener != null) {
                    listener.onRenderReady();
                }
            }
        }

        public void notifyFrameRenderFinished(int radius) {
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                view.triggerAlphaChange(radius);
            }
        }

        public void notifyFirstFrameRenderFinished() {
            Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifyFirstFrameRenderFinished <--");
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                OnRenderListener listener = view.getRenderListener();
                Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifyFirstFrameRenderFinished mHandle: " + view.obtainBlurHandle() + ", listener: " + listener);
                if (listener != null) {
                    listener.onFirstFrameFinished();
                }
            }
        }

        public void notifyRenderAgain() {
            Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView notifyRenderAgain <--");
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                view.triggerRenderAgain();
            }
        }
    }

    public interface OnRenderListener {
        void onBlurRadiusChanged(int i);

        void onFirstFrameFinished();

        void onRenderReady();
    }

    private static class ViewAlphaChangedRunnable implements Runnable {
        private WeakReference<BlurRenderView> mBlurRenderViewWeakRef;
        private int mRenderRadius = 0;

        ViewAlphaChangedRunnable(WeakReference<BlurRenderView> blurRenderViewWeakRef) {
            this.mBlurRenderViewWeakRef = blurRenderViewWeakRef;
        }

        void setRenderBlurRadius(int radius) {
            this.mRenderRadius = radius;
        }

        int getRenderBlurRadius() {
            return this.mRenderRadius;
        }

        public void run() {
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView ViewAlphaChangedRunnable mRenderRadius: " + this.mRenderRadius + ", alpha: " + view.getAlpha() + ", visibility: " + view.getVisibility());
                OnRenderListener listener = view.getRenderListener();
                if (listener != null) {
                    listener.onBlurRadiusChanged(this.mRenderRadius);
                }
            }
        }
    }

    private static class ViewRenderAgainRunnable implements Runnable {
        private WeakReference<BlurRenderView> mBlurRenderViewWeakRef;

        ViewRenderAgainRunnable(WeakReference<BlurRenderView> blurRenderViewWeakRef) {
            this.mBlurRenderViewWeakRef = blurRenderViewWeakRef;
        }

        public void run() {
            BlurRenderView view = (BlurRenderView) this.mBlurRenderViewWeakRef.get();
            if (view != null) {
                Log.d(BlurRenderView.TAG, "test gpu blur GpuTextureRenderView ViewRenderAgainRunnable visibility: " + view.getVisibility());
                view.requestRender();
            }
        }
    }

    public BlurRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "test gpu blur GpuTextureRenderView constructor <--");
        setEGLContextClientVersion(2);
        this.mContext = context.getApplicationContext();
        this.mRenderEngine = new BlurRenderEngine();
        this.mRenderer = new BlurRenderer(this.mContext, this.mRenderEngine);
        setRenderer(this.mRenderer);
        setRenderMode(0);
        onPause();
        this.mGpuBlurHandler = new GpuBlurHandler();
        this.mViewAlphaChangedRunnable = new ViewAlphaChangedRunnable(this.mThisWeakRef);
        this.mViewRenderAgainRunnable = new ViewRenderAgainRunnable(this.mThisWeakRef);
        this.mGpuRendererListener = new GpuRendererListener(this.mThisWeakRef);
        this.mRenderer.setGpuRendererListener(this.mGpuRendererListener);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureAvailable(surface, width, height);
        Log.d(TAG, "test gpu blur GpuTextureRenderView onSurfaceTextureAvailable mHandle: " + this.mHandle);
        setAlpha(0.0f);
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "test gpu blur GpuTextureRenderView onSurfaceTextureDestroyed mHandle: " + this.mHandle);
        setAlpha(0.0f);
        return super.onSurfaceTextureDestroyed(surface);
    }

    public static void doStackBlur(Bitmap bitmap, int width, int height, int radius, float bright) {
        BlurRenderer.doStackBlur(bitmap, width, height, radius, bright);
    }

    public void setRenderListener(OnRenderListener listener) {
        this.mOnRenderListener = listener;
    }

    public void setRenderScript(RenderScript rs, ScriptIntrinsicBlur sBlur) {
        if (this.mRenderer != null) {
            this.mRenderer.setRenderScript(rs, sBlur);
        }
    }

    public void create() {
        Log.d(TAG, "test gpu blur GpuTextureRenderView create mRenderer: " + this.mRenderer);
        if (this.mRenderer != null) {
            if (this.mRenderEngine == null) {
                this.mRenderEngine = new BlurRenderEngine();
                this.mRenderer.setRenderEngine(this.mRenderEngine);
            }
            this.mHandle = this.mRenderEngine.nativeCreateEngine();
            this.mRenderer.setBlurHandle(this.mHandle);
        }
    }

    private long obtainBlurHandle() {
        return this.mHandle;
    }

    private BlurRenderEngine obtainRenderEngine() {
        return this.mRenderEngine;
    }

    public void setBright(float bright, float progress) {
        if (this.mRenderEngine != null) {
            this.mBrightMin = bright;
            this.mRenderEngine.nativeSetBright(this.mHandle, bright, progress);
        }
    }

    public void onResume() {
        Log.d(TAG, "test gpu blur GpuTextureRenderView onResume mIsResumeState: " + this.mIsResumeState + ", mHandle: " + this.mHandle);
        if (!this.mIsResumeState) {
            super.onResume();
            this.mIsResumeState = true;
            if (this.mViewAlphaChangedRunnable != null) {
                this.mViewAlphaChangedRunnable.setRenderBlurRadius(0);
            }
        }
    }

    private void triggerAlphaChange(int radius) {
        if (this.mViewAlphaChangedRunnable != null && this.mGpuBlurHandler != null) {
            int lastRadius = this.mViewAlphaChangedRunnable.getRenderBlurRadius();
            Log.d(TAG, "test gpu blur GpuTextureRenderView triggerAlphaChange radius: " + radius + ", lastRadius: " + lastRadius + ", mHandle: " + this.mHandle);
            if (lastRadius != radius) {
                this.mViewAlphaChangedRunnable.setRenderBlurRadius(radius);
                this.mGpuBlurHandler.removeCallbacks(this.mViewAlphaChangedRunnable);
                this.mGpuBlurHandler.post(this.mViewAlphaChangedRunnable);
            }
        }
    }

    private void triggerRenderAgain() {
        if (this.mViewRenderAgainRunnable != null && this.mGpuBlurHandler != null) {
            Log.d(TAG, "test gpu blur GpuTextureRenderView triggerRenderAgain mHandle: " + this.mHandle);
            this.mGpuBlurHandler.removeCallbacks(this.mViewRenderAgainRunnable);
            this.mGpuBlurHandler.post(this.mViewRenderAgainRunnable);
        }
    }

    private void setAdjustBright(float bright) {
        this.mAdjustBright = bright;
    }

    private void setRenderThreadLaunchState(boolean isFnished) {
        this.mIsRenderThreadLaunchFinished = isFnished;
    }

    private boolean isRenderThreadLaunchFinished() {
        return this.mIsRenderThreadLaunchFinished;
    }

    private OnRenderListener getRenderListener() {
        return this.mOnRenderListener;
    }

    public void setRenderSource(Bitmap bitmap, int renderWindowWidth, int renderWindowHeight, float scaleRatio) {
        if (bitmap != null && !bitmap.isRecycled()) {
            this.mTargetScaledWidth = (int) ((((float) renderWindowWidth) * scaleRatio) + 0.5f);
            this.mTargetScaledHeight = (int) ((((float) renderWindowHeight) * scaleRatio) + 0.5f);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float wScaleRatio = ((float) this.mTargetScaledWidth) / ((float) width);
            float hScaleRatio = ((float) this.mTargetScaledHeight) / ((float) height);
            Log.d(TAG, "test gpu blur GpuTextureRenderView setRenderSource width: " + width + ", height: " + height + ", wScaleRatio: " + wScaleRatio + ", hScaleRatio: " + hScaleRatio);
            if (this.mRenderer != null) {
                this.mRenderer.setRenderSource(bitmap, wScaleRatio, hScaleRatio);
            }
            this.mIsSetRenderSource = true;
        }
    }

    public void setBlurRadius(int radius) {
        Log.d(TAG, "test gpu blur GpuTextureRenderView setBlurRadius radius: " + radius + ", mIsRenderThreadLaunchFinished: " + this.mIsRenderThreadLaunchFinished + ", mHandle: " + this.mHandle + ", mRenderer: " + this.mRenderer + ", mIsSetRenderSource: " + this.mIsSetRenderSource);
        if (this.mIsRenderThreadLaunchFinished && (this.mIsSetRenderSource ^ 1) == 0 && this.mRenderer != null) {
            this.mRenderer.setBlurRadius(radius);
            requestRender();
        }
    }

    public void onPause() {
        Log.d(TAG, "test gpu blur GpuTextureRenderView onPause mIsResumeState: " + this.mIsResumeState + ", mHandle: " + this.mHandle + " change super.onPause()");
        if (this.mIsResumeState) {
            this.mIsResumeState = false;
            this.mIsRenderThreadLaunchFinished = false;
            this.mIsSetRenderSource = false;
            if (this.mGpuBlurHandler != null) {
                this.mGpuBlurHandler.removeCallbacks(this.mViewAlphaChangedRunnable);
            }
            if (this.mViewAlphaChangedRunnable != null) {
                this.mViewAlphaChangedRunnable.setRenderBlurRadius(0);
            }
            if (this.mGpuBlurHandler != null) {
                this.mGpuBlurHandler.removeCallbacks(this.mViewRenderAgainRunnable);
            }
            super.onPause();
            if (this.mRenderer != null) {
                this.mRenderer.onPause();
            }
            this.mAdjustBright = this.mBrightMin;
        }
    }

    public void release() {
        Log.d(TAG, "test gpu blur GpuTextureRenderView release mHandle: " + this.mHandle);
        this.mIsResumeState = false;
        this.mIsRenderThreadLaunchFinished = false;
        if (this.mRenderer != null) {
            this.mRenderer.release();
        }
        if (this.mRenderEngine != null) {
            this.mRenderEngine.nativeDestroyEngine(this.mHandle);
            this.mRenderEngine = null;
        }
        this.mHandle = 0;
    }
}
