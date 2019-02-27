package com.vivo.common.widget;

import android.graphics.Bitmap;

class BlurRenderEngine {
    static final String TAG = "BlurRenderEngine";

    static native void nativeDoStackBlur(Bitmap bitmap, int i, int i2, int i3, float f);

    native int nativeAnalyzeImageData(long j);

    native long nativeCreateEngine();

    native void nativeDestroyEngine(long j);

    native float nativeGetAdjustBright(long j);

    native boolean nativeIsFirstRenderFrame(long j);

    native boolean nativeIsNeedRsBlur(long j, int i);

    native boolean nativeNeedRenderAgain(long j);

    native void nativePause(long j);

    native int nativeRender(long j);

    native void nativeSetBlurRadius(long j, int i);

    native void nativeSetBright(long j, float f, float f2);

    native void nativeSetDebugState(long j, boolean z);

    native void nativeSetRenderSource(long j, Bitmap bitmap, Bitmap bitmap2, int i, int i2);

    native void nativeSurfaceChanged(long j, int i, int i2);

    BlurRenderEngine() {
    }
}
