package com.vivo.common.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLDebugHelper;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class GLTextureView extends TextureView implements SurfaceTextureListener {
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private static final boolean LOG_ATTACH_DETACH = false;
    private static final boolean LOG_EGL = false;
    private static final boolean LOG_PAUSE_RESUME = false;
    private static final boolean LOG_RENDERER = false;
    private static final boolean LOG_RENDERER_DRAW_FRAME = false;
    private static final boolean LOG_SURFACE = false;
    private static final boolean LOG_THREADS = false;
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    private static final String TAG = "GLTextureView";
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    private int mDebugFlags;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private int mEGLContextClientVersion;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLThread mGLThread;
    private GLWrapper mGLWrapper;
    private boolean mPreserveEGLContextOnPause;
    private Renderer mRenderer;
    private SurfaceTexture mSurfaceTexture = null;
    private final WeakReference<GLTextureView> mThisWeakRef = new WeakReference(this);

    public interface Renderer {
        void onDrawFrame(GL10 gl10);

        void onSurfaceChanged(GL10 gl10, int i, int i2);

        void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig);
    }

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config)) {
                int numConfigs = num_config[0];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
                EGLConfig[] configs = new EGLConfig[numConfigs];
                if (egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                    EGLConfig config = chooseConfig(egl, display, configs);
                    if (config != null) {
                        return config;
                    }
                    throw new IllegalArgumentException("No config chosen");
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        private int[] filterConfigSpec(int[] configSpec) {
            if (GLTextureView.this.mEGLContextClientVersion != 2 && GLTextureView.this.mEGLContextClientVersion != 3) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + 2)];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = 12352;
            if (GLTextureView.this.mEGLContextClientVersion == 2) {
                newConfigSpec[len] = 4;
            } else {
                newConfigSpec[len] = 64;
            }
            newConfigSpec[len + 1] = 12344;
            return newConfigSpec;
        }
    }

    private class ComponentSizeChooser extends BaseConfigChooser {
        protected int mAlphaSize;
        protected int mBlueSize;
        protected int mDepthSize;
        protected int mGreenSize;
        protected int mRedSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{12324, redSize, 12323, greenSize, 12322, blueSize, 12321, alphaSize, 12325, depthSize, 12326, stencilSize, 12344});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, 12325, 0);
                int s = findConfigAttrib(egl, display, config, 12326, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, 12324, 0);
                    int g = findConfigAttrib(egl, display, config, 12323, 0);
                    int b = findConfigAttrib(egl, display, config, 12322, 0);
                    int a = findConfigAttrib(egl, display, config, 12321, 0);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[0];
            }
            return defaultValue;
        }
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void destroyContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        /* synthetic */ DefaultContextFactory(GLTextureView this$0, DefaultContextFactory -this1) {
            this();
        }

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = new int[]{this.EGL_CONTEXT_CLIENT_VERSION, GLTextureView.this.mEGLContextClientVersion, 12344};
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (GLTextureView.this.mEGLContextClientVersion == 0) {
                attrib_list = null;
            }
            return egl.eglCreateContext(display, config, eGLContext, attrib_list);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void destroySurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        /* synthetic */ DefaultWindowSurfaceFactory(DefaultWindowSurfaceFactory -this0) {
            this();
        }

        private DefaultWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e(GLTextureView.TAG, "eglCreateWindowSurface", e);
                return result;
            }
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private static class EglHelper {
        EGL10 mEgl;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        private WeakReference<GLTextureView> mGLTextureViewWeakRef;

        public EglHelper(WeakReference<GLTextureView> glSurfaceViewWeakRef) {
            this.mGLTextureViewWeakRef = glSurfaceViewWeakRef;
        }

        public void start() {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
                GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
                if (view == null) {
                    this.mEglConfig = null;
                    this.mEglContext = null;
                } else {
                    this.mEglConfig = view.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
                    this.mEglContext = view.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                }
                if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    this.mEglContext = null;
                    throwEglException("createContext");
                }
                this.mEglSurface = null;
                return;
            }
            throw new RuntimeException("eglInitialize failed");
        }

        public boolean createSurface() {
            if (this.mEgl == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            } else {
                destroySurfaceImp();
                GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
                if (view != null) {
                    SurfaceTexture surface = view.getSurface();
                    if (surface != null) {
                        this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, surface);
                    } else {
                        this.mEglSurface = null;
                    }
                } else {
                    this.mEglSurface = null;
                }
                if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglGetError() == 12299) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return false;
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                    return false;
                }
            }
        }

        GL createGL() {
            GL gl = this.mEglContext.getGL();
            GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
            if (view == null) {
                return gl;
            }
            if (view.mGLWrapper != null) {
                gl = view.mGLWrapper.wrap(gl);
            }
            if ((view.mDebugFlags & 3) == 0) {
                return gl;
            }
            int configFlags = 0;
            Writer writer = null;
            if ((view.mDebugFlags & 1) != 0) {
                configFlags = 1;
            }
            if ((view.mDebugFlags & 2) != 0) {
                writer = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, writer);
        }

        public int swap() {
            if (this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return 12288;
            }
            return this.mEgl.eglGetError();
        }

        public void destroySurface() {
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
                }
                this.mEglSurface = null;
            }
        }

        public void finish() {
            if (this.mEglContext != null) {
                GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
                }
                this.mEglContext = null;
            }
            if (this.mEglDisplay != null) {
                this.mEgl.eglTerminate(this.mEglDisplay);
                this.mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, this.mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            throw new RuntimeException(formatEglError(function, error));
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + error;
        }
    }

    static class GLThread extends Thread {
        private EglHelper mEglHelper;
        private ArrayList<Runnable> mEventQueue = new ArrayList();
        private boolean mExited;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLTextureView> mGLTextureViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight = 0;
        private boolean mPaused;
        private boolean mRenderComplete;
        private int mRenderMode = 1;
        private boolean mRequestPaused;
        private boolean mRequestRender = true;
        private boolean mShouldExit;
        private boolean mShouldReleaseEglContext;
        private boolean mSizeChanged = true;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private int mWidth = 0;

        GLThread(WeakReference<GLTextureView> glSurfaceViewWeakRef) {
            this.mGLTextureViewWeakRef = glSurfaceViewWeakRef;
        }

        public void run() {
            setName("GLThread " + getId());
            try {
                guardedRun();
            } catch (InterruptedException e) {
            } finally {
                GLTextureView.sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglSurfaceLocked() {
            if (this.mHaveEglSurface) {
                this.mHaveEglSurface = false;
                this.mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (this.mHaveEglContext) {
                this.mEglHelper.finish();
                this.mHaveEglContext = false;
                GLTextureView.sGLThreadManager.releaseEglContextLocked(this);
            }
        }

        /* JADX WARNING: Missing block: B:24:0x006f, code:
            if (r7 == null) goto L_0x022c;
     */
        /* JADX WARNING: Missing block: B:25:0x0071, code:
            r7.run();
     */
        /* JADX WARNING: Missing block: B:26:0x0074, code:
            r7 = null;
     */
        /* JADX WARNING: Missing block: B:112:0x022c, code:
            if (r4 == false) goto L_0x0250;
     */
        /* JADX WARNING: Missing block: B:115:0x0238, code:
            if (r22.mEglHelper.createSurface() == false) goto L_0x02fd;
     */
        /* JADX WARNING: Missing block: B:116:0x023a, code:
            r20 = com.vivo.common.widget.GLTextureView.-get8();
     */
        /* JADX WARNING: Missing block: B:117:0x023e, code:
            monitor-enter(r20);
     */
        /* JADX WARNING: Missing block: B:120:?, code:
            r22.mFinishedCreatingEglSurface = true;
            com.vivo.common.widget.GLTextureView.-get8().notifyAll();
     */
        /* JADX WARNING: Missing block: B:122:?, code:
            monitor-exit(r20);
     */
        /* JADX WARNING: Missing block: B:123:0x024f, code:
            r4 = false;
     */
        /* JADX WARNING: Missing block: B:124:0x0250, code:
            if (r5 == false) goto L_0x026b;
     */
        /* JADX WARNING: Missing block: B:125:0x0252, code:
            r8 = (javax.microedition.khronos.opengles.GL10) r22.mEglHelper.createGL();
            com.vivo.common.widget.GLTextureView.-get8().checkGLDriver(r8);
            r5 = false;
     */
        /* JADX WARNING: Missing block: B:126:0x026b, code:
            if (r3 == false) goto L_0x0293;
     */
        /* JADX WARNING: Missing block: B:127:0x026d, code:
            r16 = (com.vivo.common.widget.GLTextureView) r22.mGLTextureViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:128:0x0279, code:
            if (r16 == null) goto L_0x0292;
     */
        /* JADX WARNING: Missing block: B:129:0x027b, code:
            com.vivo.common.widget.GLTextureView.-get7(r16).onSurfaceCreated(r8, r22.mEglHelper.mEglConfig);
     */
        /* JADX WARNING: Missing block: B:130:0x0292, code:
            r3 = false;
     */
        /* JADX WARNING: Missing block: B:131:0x0293, code:
            if (r13 == false) goto L_0x02af;
     */
        /* JADX WARNING: Missing block: B:132:0x0295, code:
            r16 = (com.vivo.common.widget.GLTextureView) r22.mGLTextureViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:133:0x02a1, code:
            if (r16 == null) goto L_0x02ae;
     */
        /* JADX WARNING: Missing block: B:134:0x02a3, code:
            com.vivo.common.widget.GLTextureView.-get7(r16).onSurfaceChanged(r8, r17, r9);
     */
        /* JADX WARNING: Missing block: B:135:0x02ae, code:
            r13 = false;
     */
        /* JADX WARNING: Missing block: B:136:0x02af, code:
            r16 = (com.vivo.common.widget.GLTextureView) r22.mGLTextureViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:137:0x02bb, code:
            if (r16 == null) goto L_0x02c6;
     */
        /* JADX WARNING: Missing block: B:138:0x02bd, code:
            com.vivo.common.widget.GLTextureView.-get7(r16).onDrawFrame(r8);
     */
        /* JADX WARNING: Missing block: B:139:0x02c6, code:
            r14 = r22.mEglHelper.swap();
     */
        /* JADX WARNING: Missing block: B:140:0x02d0, code:
            switch(r14) {
                case 12288: goto L_0x02f5;
                case 12302: goto L_0x031f;
                default: goto L_0x02d3;
            };
     */
        /* JADX WARNING: Missing block: B:141:0x02d3, code:
            com.vivo.common.widget.GLTextureView.EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", r14);
            r20 = com.vivo.common.widget.GLTextureView.-get8();
     */
        /* JADX WARNING: Missing block: B:142:0x02e4, code:
            monitor-enter(r20);
     */
        /* JADX WARNING: Missing block: B:145:?, code:
            r22.mSurfaceIsBad = true;
            com.vivo.common.widget.GLTextureView.-get8().notifyAll();
     */
        /* JADX WARNING: Missing block: B:147:?, code:
            monitor-exit(r20);
     */
        /* JADX WARNING: Missing block: B:148:0x02f5, code:
            if (r18 == false) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:149:0x02f7, code:
            r6 = true;
     */
        /* JADX WARNING: Missing block: B:153:0x02fd, code:
            r20 = com.vivo.common.widget.GLTextureView.-get8();
     */
        /* JADX WARNING: Missing block: B:154:0x0301, code:
            monitor-enter(r20);
     */
        /* JADX WARNING: Missing block: B:157:?, code:
            r22.mFinishedCreatingEglSurface = true;
            r22.mSurfaceIsBad = true;
            com.vivo.common.widget.GLTextureView.-get8().notifyAll();
     */
        /* JADX WARNING: Missing block: B:159:?, code:
            monitor-exit(r20);
     */
        /* JADX WARNING: Missing block: B:164:0x031f, code:
            r10 = true;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void guardedRun() throws InterruptedException {
            this.mEglHelper = new EglHelper(this.mGLTextureViewWeakRef);
            this.mHaveEglContext = false;
            this.mHaveEglSurface = false;
            GL10 gl = null;
            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean createGlInterface = false;
            boolean lostEglContext = false;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;
            while (true) {
                synchronized (GLTextureView.sGLThreadManager) {
                    while (!this.mShouldExit) {
                        if (this.mEventQueue.isEmpty()) {
                            boolean pausing = false;
                            if (this.mPaused != this.mRequestPaused) {
                                pausing = this.mRequestPaused;
                                this.mPaused = this.mRequestPaused;
                                GLTextureView.sGLThreadManager.notifyAll();
                            }
                            if (this.mShouldReleaseEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                this.mShouldReleaseEglContext = false;
                                askedToReleaseEglContext = true;
                            }
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = false;
                            }
                            if (pausing && this.mHaveEglSurface) {
                                stopEglSurfaceLocked();
                            }
                            if (pausing && this.mHaveEglContext) {
                                boolean preserveEglContextOnPause;
                                GLTextureView view = (GLTextureView) this.mGLTextureViewWeakRef.get();
                                if (view == null) {
                                    preserveEglContextOnPause = false;
                                } else {
                                    preserveEglContextOnPause = view.mPreserveEGLContextOnPause;
                                }
                                if (!preserveEglContextOnPause || GLTextureView.sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked();
                                }
                            }
                            if (pausing && GLTextureView.sGLThreadManager.shouldTerminateEGLWhenPausing()) {
                                this.mEglHelper.finish();
                            }
                            if (!(this.mHasSurface || (this.mWaitingForSurface ^ 1) == 0)) {
                                if (this.mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                this.mWaitingForSurface = true;
                                this.mSurfaceIsBad = false;
                                GLTextureView.sGLThreadManager.notifyAll();
                            }
                            if (this.mHasSurface && this.mWaitingForSurface) {
                                this.mWaitingForSurface = false;
                                GLTextureView.sGLThreadManager.notifyAll();
                            }
                            if (doRenderNotification) {
                                wantRenderNotification = false;
                                doRenderNotification = false;
                                this.mRenderComplete = true;
                                GLTextureView.sGLThreadManager.notifyAll();
                            }
                            if (readyToDraw()) {
                                if (!this.mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false;
                                    } else if (GLTextureView.sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                        this.mEglHelper.start();
                                        this.mHaveEglContext = true;
                                        createEglContext = true;
                                        GLTextureView.sGLThreadManager.notifyAll();
                                    }
                                }
                                if (this.mHaveEglContext && (this.mHaveEglSurface ^ 1) != 0) {
                                    this.mHaveEglSurface = true;
                                    createEglSurface = true;
                                    createGlInterface = true;
                                    sizeChanged = true;
                                }
                                if (this.mHaveEglSurface) {
                                    if (this.mSizeChanged) {
                                        sizeChanged = true;
                                        w = this.mWidth;
                                        h = this.mHeight;
                                        wantRenderNotification = true;
                                        createEglSurface = true;
                                        this.mSizeChanged = false;
                                    }
                                    this.mRequestRender = false;
                                    GLTextureView.sGLThreadManager.notifyAll();
                                }
                            }
                            GLTextureView.sGLThreadManager.wait();
                        } else {
                            event = (Runnable) this.mEventQueue.remove(0);
                        }
                        try {
                        } catch (RuntimeException t) {
                            GLTextureView.sGLThreadManager.releaseEglContextLocked(this);
                            throw t;
                        } catch (Throwable th) {
                            synchronized (GLTextureView.sGLThreadManager) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                            }
                        }
                    }
                    synchronized (GLTextureView.sGLThreadManager) {
                        stopEglSurfaceLocked();
                        stopEglContextLocked();
                    }
                    return;
                }
            }
        }

        public boolean ableToDraw() {
            return (this.mHaveEglContext && this.mHaveEglSurface) ? readyToDraw() : false;
        }

        private boolean readyToDraw() {
            if (this.mPaused || !this.mHasSurface || (this.mSurfaceIsBad ^ 1) == 0 || this.mWidth <= 0 || this.mHeight <= 0) {
                return false;
            }
            return this.mRequestRender || this.mRenderMode == 1;
        }

        public void setRenderMode(int renderMode) {
            if (renderMode < 0 || renderMode > 1) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (GLTextureView.sGLThreadManager) {
                this.mRenderMode = renderMode;
                GLTextureView.sGLThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            int i;
            synchronized (GLTextureView.sGLThreadManager) {
                i = this.mRenderMode;
            }
            return i;
        }

        public void requestRender() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mRequestRender = true;
                GLTextureView.sGLThreadManager.notifyAll();
            }
        }

        public void surfaceCreated() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = false;
                GLTextureView.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && (this.mFinishedCreatingEglSurface ^ 1) != 0 && (this.mExited ^ 1) != 0) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mHasSurface = false;
                GLTextureView.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && (this.mExited ^ 1) != 0) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mRequestPaused = true;
                GLTextureView.sGLThreadManager.notifyAll();
                while (!this.mExited && (this.mPaused ^ 1) != 0) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mRequestPaused = false;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                GLTextureView.sGLThreadManager.notifyAll();
                while (!this.mExited && this.mPaused && (this.mRenderComplete ^ 1) != 0) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                GLTextureView.sGLThreadManager.notifyAll();
                while (!this.mExited && (this.mPaused ^ 1) != 0 && (this.mRenderComplete ^ 1) != 0 && ableToDraw()) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            synchronized (GLTextureView.sGLThreadManager) {
                this.mShouldExit = true;
                GLTextureView.sGLThreadManager.notifyAll();
                while (!this.mExited) {
                    try {
                        GLTextureView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            this.mShouldReleaseEglContext = true;
            GLTextureView.sGLThreadManager.notifyAll();
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized (GLTextureView.sGLThreadManager) {
                this.mEventQueue.add(r);
                GLTextureView.sGLThreadManager.notifyAll();
            }
        }
    }

    private static class GLThreadManager {
        private static String TAG = "GLThreadManager";
        private static final int kGLES_20 = 131072;
        private static final String kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 ";
        private GLThread mEglOwner;
        private boolean mGLESDriverCheckComplete;
        private int mGLESVersion;
        private boolean mGLESVersionCheckComplete;
        private boolean mLimitedGLESContexts;
        private boolean mMultipleGLESContextsAllowed;

        /* synthetic */ GLThreadManager(GLThreadManager -this0) {
            this();
        }

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            thread.mExited = true;
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread || this.mEglOwner == null) {
                this.mEglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            if (this.mMultipleGLESContextsAllowed) {
                return true;
            }
            if (this.mEglOwner != null) {
                this.mEglOwner.requestReleaseEglContextLocked();
            }
            return false;
        }

        public void releaseEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            return this.mLimitedGLESContexts;
        }

        public synchronized boolean shouldTerminateEGLWhenPausing() {
            checkGLESVersion();
            return this.mMultipleGLESContextsAllowed ^ 1;
        }

        public synchronized void checkGLDriver(GL10 gl) {
            if (!this.mGLESDriverCheckComplete) {
                checkGLESVersion();
                String renderer = gl.glGetString(7937);
                if (this.mGLESVersion < kGLES_20) {
                    this.mMultipleGLESContextsAllowed = renderer.startsWith(kMSM7K_RENDERER_PREFIX) ^ 1;
                    notifyAll();
                }
                this.mLimitedGLESContexts = this.mMultipleGLESContextsAllowed ^ 1;
                this.mGLESDriverCheckComplete = true;
            }
        }

        private void checkGLESVersion() {
            if (!this.mGLESVersionCheckComplete) {
                this.mGLESVersion = SystemProperties.getInt("ro.opengles.version", 0);
                if (this.mGLESVersion >= kGLES_20) {
                    this.mMultipleGLESContextsAllowed = true;
                }
                this.mGLESVersionCheckComplete = true;
            }
        }
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder = new StringBuilder();

        LogWriter() {
        }

        public void close() {
            flushBuilder();
        }

        public void flush() {
            flushBuilder();
        }

        public void write(char[] buf, int offset, int count) {
            for (int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if (c == 10) {
                    flushBuilder();
                } else {
                    this.mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (this.mBuilder.length() > 0) {
                Log.v(GLTextureView.TAG, this.mBuilder.toString());
                this.mBuilder.delete(0, this.mBuilder.length());
            }
        }
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            int i;
            if (withDepthBuffer) {
                i = 16;
            } else {
                i = 0;
            }
            super(8, 8, 8, 0, i, 0);
        }
    }

    public GLTextureView(Context context) {
        super(context);
        init();
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mGLThread != null) {
                this.mGLThread.requestExitAndWait();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void setGLWrapper(GLWrapper glWrapper) {
        this.mGLWrapper = glWrapper;
    }

    public void setDebugFlags(int debugFlags) {
        this.mDebugFlags = debugFlags;
    }

    public int getDebugFlags() {
        return this.mDebugFlags;
    }

    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        this.mPreserveEGLContextOnPause = preserveOnPause;
    }

    public boolean getPreserveEGLContextOnPause() {
        return this.mPreserveEGLContextOnPause;
    }

    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        if (this.mEGLConfigChooser == null) {
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory(this, null);
        }
        if (this.mEGLWindowSurfaceFactory == null) {
            this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        this.mRenderer = renderer;
        this.mGLThread = new GLThread(this.mThisWeakRef);
        this.mGLThread.start();
    }

    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        this.mEGLContextFactory = factory;
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        this.mEGLConfigChooser = configChooser;
    }

    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        this.mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    public void requestRender() {
        this.mGLThread.requestRender();
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        this.mGLThread.surfaceCreated();
        this.mGLThread.onWindowResize(width, height);
    }

    public SurfaceTexture getSurface() {
        return this.mSurfaceTexture;
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        this.mSurfaceTexture = null;
        surface.releaseTexImage();
        surface.release();
        this.mGLThread.surfaceDestroyed();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.mGLThread.onWindowResize(width, height);
    }

    public void onPause() {
        this.mGLThread.onPause();
    }

    public void onResume() {
        this.mGLThread.onResume();
    }

    public void queueEvent(Runnable r) {
        this.mGLThread.queueEvent(r);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = 1;
            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != 1) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = false;
    }

    protected void onDetachedFromWindow() {
        if (this.mGLThread != null) {
            this.mGLThread.requestExitAndWait();
        }
        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }
}
