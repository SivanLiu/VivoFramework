package com.vivo.vivotransition;

import android.animation.TimeInterpolator;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.vivo.vivotransition.algorithm.BaseTransition;
import com.vivo.vivotransition.algorithm.BaseTransition.TransformationInfo;
import com.vivo.vivotransition.algorithm.BlindTransition;
import com.vivo.vivotransition.algorithm.BoxInTransition;
import com.vivo.vivotransition.algorithm.BoxTransition;
import com.vivo.vivotransition.algorithm.CylinderTransition;
import com.vivo.vivotransition.algorithm.DepthTransition;
import com.vivo.vivotransition.algorithm.FadeTransition;
import com.vivo.vivotransition.algorithm.FlipOverTransition;
import com.vivo.vivotransition.algorithm.GoRotateTransition;
import com.vivo.vivotransition.algorithm.PageTransition;
import com.vivo.vivotransition.algorithm.PushTransition;
import com.vivo.vivotransition.algorithm.StackTransition;
import com.vivo.vivotransition.algorithm.WindMillTransition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoTransition {
    private static final float ALPHA_OPAQUE = 0.999f;
    private static final float ALPHA_TRANSPARENT = 0.001f;
    private static final int CAMERA_Z_DEF = -8;
    private static final boolean DBG = false;
    private static final String TAG = "VivoTransition";
    public static final String TRANS_TYPE_BOX = "Box";
    public static final String TRANS_TYPE_CYLINDER = "Cylinder";
    public static final String TRANS_TYPE_DEPTH = "Depth";
    public static final String TRANS_TYPE_FLIPOVER = "Flipover";
    public static final String TRANS_TYPE_NORMAL = "Normal";
    public static final String TRANS_TYPE_PAGE = "Page";
    public static final String TRANS_TYPE_PUSH = "Push";
    public static final String TRANS_TYPE_ROTATION = "Rotation";
    public static final String TRANS_TYPE_WINDMILL = "Windmill";
    private BaseTransition mActiveTransition;
    private int mActiveTransitionType;
    private ArrayList<View> mAlphaViews;
    private ArrayList<Float> mAlphas;
    private ArrayList<AnimateInfo> mAnimInfoEnds;
    private ArrayList<AnimateInfo> mAnimInfos;
    private Bitmap mBackground;
    private Rect mBgDstRect;
    private boolean mBgNoZoom;
    private Paint mBgPaint;
    private Rect mBgSrcRect;
    private boolean mBgStatic;
    private Method mDrawMethod;
    private Paint mErasePaint;
    private int mFirstOffset;
    private boolean mForceDraw;
    private TimeInterpolator mInterpolator;
    private boolean mIsTransparent;
    private ArrayList<Integer> mLayerType;
    private float mOffset;
    private int mPageSpacing;
    private Paint mPaint;
    private View mTargetView;
    private TransformationInfo mTransInfo;
    private HashMap<Integer, BaseTransition> mTransitionsMap;

    static class AnimateInfo {
        long drawingTime;
        long duration = 500;
        boolean isScrolling;
        boolean reverse = false;
        long startTime = -1;
        int type = 1;
        ArrayList<ViewInfo> viewInfos = new ArrayList();

        public AnimateInfo(int i) {
            this.type = i;
        }

        public void clear() {
            Iterator<ViewInfo> iterator = this.viewInfos.iterator();
            while (iterator.hasNext()) {
                ((ViewInfo) iterator.next()).clean();
            }
            this.viewInfos.clear();
            this.viewInfos = null;
        }
    }

    static class ShadowView extends View {
        Bitmap mBmp;
        Paint mPaint = new Paint();

        static ShadowView createShadow(View view) {
            ShadowView shadowview = new ShadowView(view);
            if (shadowview.copyView(view)) {
                return shadowview;
            }
            return null;
        }

        public void clearBitmap() {
            if (this.mBmp != null) {
                this.mBmp.recycle();
            }
        }

        public boolean copyView(View view) {
            boolean flag1 = view.willNotCacheDrawing();
            int i = view.getDrawingCacheBackgroundColor();
            view.setWillNotCacheDrawing(false);
            view.setDrawingCacheBackgroundColor(0);
            if (i != 0) {
                view.destroyDrawingCache();
            }
            view.buildDrawingCache();
            Bitmap bitmap = view.getDrawingCache();
            if (bitmap == null) {
                Log.e(VivoTransition.TAG, "copyView failed, view : " + view);
                return false;
            }
            this.mBmp = Bitmap.createBitmap(bitmap);
            view.destroyDrawingCache();
            view.setWillNotCacheDrawing(flag1);
            view.setDrawingCacheBackgroundColor(i);
            return true;
        }

        public void draw(Canvas canvas) {
            if (this.mBmp != null) {
                canvas.drawBitmap(this.mBmp, 0.0f, 0.0f, this.mPaint);
            } else {
                Log.e(VivoTransition.TAG, "bitmap is null, should not come here!!! ");
            }
        }

        public void setAlpha(float f) {
            this.mPaint.setAlpha((int) (255.0f * f));
        }

        private ShadowView(View view) {
            super(view.getContext());
        }
    }

    static class ViewInfo {
        float alpha = -1.0f;
        float fraction;
        int index;
        boolean isOverScrollFirst;
        boolean isOverScrollLast;
        final int layerType;
        ShadowView shadowView;
        View view;

        void clean() {
            if (this.shadowView != null) {
                this.shadowView.clearBitmap();
                this.shadowView = null;
            }
            this.view = null;
        }

        public ViewInfo(View v) {
            this.view = v;
            if (v != null) {
                this.layerType = this.view.getLayerType();
            } else {
                this.layerType = -1;
            }
        }
    }

    public VivoTransition(View view) {
        this(view, 0);
    }

    public VivoTransition(View view, int transitionType) {
        this(view, transitionType, 0);
    }

    public VivoTransition(View view, int transitionType, int pageSpace) {
        this.mAnimInfos = new ArrayList();
        this.mAnimInfoEnds = new ArrayList();
        this.mAlphaViews = new ArrayList();
        this.mAlphas = new ArrayList();
        this.mLayerType = new ArrayList();
        this.mForceDraw = false;
        this.mBgStatic = false;
        this.mBgNoZoom = true;
        this.mOffset = 0.0f;
        this.mIsTransparent = false;
        this.mFirstOffset = 0;
        this.mTransitionsMap = new HashMap();
        this.mBgPaint = new Paint();
        this.mErasePaint = new Paint();
        this.mBgDstRect = new Rect();
        this.mBgSrcRect = new Rect();
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        if (view != null) {
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            this.mTargetView = view;
            this.mDrawMethod = getDrawChildMethod();
            initTransitions();
            setTransitionType(transitionType);
            setPageSpacing(pageSpace);
        }
    }

    private boolean animView(Canvas canvas, AnimateInfo animateinfo) {
        int breakTimes = this.mActiveTransition.getBreakTimes();
        int childWidth = this.mTargetView.getWidth();
        int childHeight = this.mTargetView.getHeight();
        int viewsSize = animateinfo.viewInfos.size();
        this.mActiveTransition.setState(animateinfo.isScrolling);
        for (int i = 0; i < viewsSize; i++) {
            for (int times = 0; times < breakTimes; times++) {
                ViewInfo viewinfo = (ViewInfo) animateinfo.viewInfos.get(this.mActiveTransition.getDrawingOrder(viewsSize, i, times, ((ViewInfo) animateinfo.viewInfos.get(i)).fraction));
                View view = viewinfo.view;
                int viewLeft = view.getLeft();
                int viewTop = view.getTop();
                if (this.mTargetView == view) {
                    viewLeft = 0;
                    viewTop = 0;
                }
                int part = this.mActiveTransition.getBreakOrder(times, viewinfo.fraction);
                this.mTransInfo = this.mActiveTransition.getTransformation(part, viewinfo.isOverScrollFirst, viewinfo.isOverScrollLast, viewinfo.fraction, this.mTargetView, view, this.mPageSpacing);
                if (this.mTransInfo != null && (!this.mTransInfo.mAlphaDirty || this.mTransInfo.mAlpha >= ALPHA_TRANSPARENT)) {
                    int result = canvas.save();
                    if (!this.mTransInfo.mBoundsDirty) {
                        AlgorithmUtil.getTransformRect(view, this.mTransInfo.mBounds);
                    }
                    this.mTransInfo.mBounds.offset(viewLeft + 0, viewTop + 0);
                    if (this.mBackground != null && this.mTransInfo.mBackgroundDirty) {
                        this.mErasePaint.setColor(16777215);
                        this.mBgDstRect.set(this.mTransInfo.mBounds);
                        if (this.mActiveTransition.isHorizental()) {
                            this.mBgDstRect.top = 0;
                            this.mBgDstRect.bottom = childHeight;
                            if (part == 0) {
                                this.mBgDstRect.left = viewLeft - this.mFirstOffset;
                            } else if (part == breakTimes - 1) {
                                this.mBgDstRect.right = (viewLeft + childWidth) - this.mFirstOffset;
                            }
                        } else {
                            this.mBgDstRect.left = 0;
                            this.mBgDstRect.right = childWidth;
                            if (part == 0) {
                                this.mBgDstRect.top = viewTop - this.mFirstOffset;
                            } else if (part == breakTimes - 1) {
                                this.mBgDstRect.bottom = (viewTop + childHeight) - this.mFirstOffset;
                            }
                        }
                        if (viewinfo.isOverScrollFirst || viewinfo.isOverScrollLast) {
                            canvas.save();
                            canvas.clipRect(this.mBgDstRect);
                            canvas.drawPaint(this.mErasePaint);
                            canvas.restore();
                        }
                    }
                    if (this.mTransInfo.mMatrixDirty) {
                        canvas.translate((float) (viewLeft + 0), (float) (viewTop + 0));
                        canvas.concat(this.mTransInfo.mMatrix);
                        canvas.translate((float) (0 - viewLeft), (float) (0 - viewTop));
                    }
                    if (this.mBackground != null && this.mTransInfo.mBackgroundDirty) {
                        float dxy = 0.0f;
                        int childCounts = ((ViewGroup) this.mTargetView).getChildCount();
                        if (this.mActiveTransition.isHorizental()) {
                            if (this.mBgStatic) {
                                dxy = ((float) (this.mBackground.getWidth() - childWidth)) * this.mOffset;
                            } else if (childCounts > 1) {
                                dxy = (((float) (this.mBackground.getWidth() - childWidth)) / ((float) (childCounts - 1))) * ((float) viewinfo.index);
                            }
                            this.mBgSrcRect.set((int) (((((float) this.mBgDstRect.left) + dxy) - ((float) viewLeft)) + 0.5f), 0, (int) (((((float) this.mBgDstRect.right) + dxy) - ((float) viewLeft)) + 0.5f), this.mBackground.getHeight());
                            if (this.mBgNoZoom) {
                                this.mBgSrcRect.offset(0, this.mBgSrcRect.height() - this.mBgDstRect.height());
                            }
                        } else {
                            if (this.mBgStatic) {
                                dxy = ((float) (this.mBackground.getHeight() - childHeight)) * this.mOffset;
                            } else if (childCounts > 1) {
                                dxy = (((float) (this.mBackground.getHeight() - childHeight)) / ((float) (childCounts - 1))) * ((float) viewinfo.index);
                            }
                            this.mBgSrcRect.set(0, (int) (((((float) this.mBgDstRect.top) + dxy) - ((float) viewLeft)) + 0.5f), this.mBackground.getWidth(), (int) (((((float) this.mBgDstRect.bottom) + dxy) - ((float) viewLeft)) + 0.5f));
                            if (this.mBgNoZoom) {
                                this.mBgSrcRect.offset(this.mBgSrcRect.width() - this.mBgDstRect.width(), 0);
                            }
                        }
                        canvas.drawBitmap(this.mBackground, this.mBgSrcRect, this.mBgDstRect, this.mBgPaint);
                    }
                    if (this.mTransInfo.mBoundsDirty) {
                        canvas.clipRect(this.mTransInfo.mBounds);
                    }
                    if (this.mTransInfo.mAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE && (this.mIsTransparent ^ 1) != 0) {
                        if (breakTimes != 1) {
                            canvas.saveLayerAlpha((float) this.mTransInfo.mBounds.left, (float) this.mTransInfo.mBounds.top, (float) this.mTransInfo.mBounds.right, (float) this.mTransInfo.mBounds.bottom, (int) (this.mTransInfo.mAlpha * 255.0f), 4);
                        } else if (viewinfo.shadowView != null) {
                            viewinfo.shadowView.setAlpha(this.mTransInfo.mAlpha);
                        } else {
                            if (viewinfo.alpha == -1.0f) {
                                viewinfo.alpha = view.getAlpha();
                            }
                            if (viewinfo.layerType != 2) {
                                view.setLayerType(2, null);
                            }
                            view.setAlpha(this.mTransInfo.mAlpha);
                        }
                    }
                    if (animateinfo.type != 1) {
                        try {
                            this.mDrawMethod.invoke(this.mTargetView, new Object[]{canvas, view, Long.valueOf(animateinfo.drawingTime)});
                        } catch (IllegalArgumentException illegalargumentexception) {
                            Log.e(TAG, illegalargumentexception.toString());
                        } catch (IllegalAccessException illegalaccessexception) {
                            Log.e(TAG, illegalaccessexception.toString());
                        } catch (InvocationTargetException invocationtargetexception) {
                            Log.e(TAG, invocationtargetexception.toString());
                        }
                    } else if (viewinfo.shadowView != null) {
                        viewinfo.shadowView.draw(canvas);
                    } else {
                        this.mForceDraw = true;
                        view.draw(canvas);
                    }
                    if (this.mTransInfo.mAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE) {
                        if (this.mIsTransparent) {
                            this.mPaint.setAlpha((int) (this.mTransInfo.mAlpha * 255.0f));
                            this.mTransInfo.mBounds.inset(-1, 0);
                            canvas.drawRect(this.mTransInfo.mBounds, this.mPaint);
                        } else if (breakTimes != 1) {
                            canvas.restore();
                        }
                    }
                    canvas.restoreToCount(result);
                }
            }
        }
        return true;
    }

    private Method getDrawChildMethod() {
        Method method = null;
        try {
            method = ViewGroup.class.getDeclaredMethod("drawChild", new Class[]{Canvas.class, View.class, Long.TYPE});
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException nosuchmethodexception) {
            Log.e(TAG, nosuchmethodexception.toString());
            return method;
        }
    }

    private void initTransitions() {
        if (this.mTransitionsMap.size() == 0) {
            this.mTransitionsMap.put(Integer.valueOf(3), new DepthTransition());
            this.mTransitionsMap.put(Integer.valueOf(5), new WindMillTransition());
            this.mTransitionsMap.put(Integer.valueOf(1), new PushTransition());
            this.mTransitionsMap.put(Integer.valueOf(6), new BoxTransition());
            this.mTransitionsMap.put(Integer.valueOf(8), new FlipOverTransition());
            this.mTransitionsMap.put(Integer.valueOf(2), new GoRotateTransition());
            this.mTransitionsMap.put(Integer.valueOf(9), new PageTransition());
            this.mTransitionsMap.put(Integer.valueOf(4), new CylinderTransition());
            this.mTransitionsMap.put(Integer.valueOf(10), new BlindTransition());
            this.mTransitionsMap.put(Integer.valueOf(11), new FadeTransition());
            this.mTransitionsMap.put(Integer.valueOf(12), new StackTransition());
            this.mTransitionsMap.put(Integer.valueOf(7), new BoxInTransition());
        }
    }

    private void onAnimationEnd(AnimateInfo animateinfo) {
        this.mAnimInfos.remove(animateinfo);
        Iterator<ViewInfo> iterator = animateinfo.viewInfos.iterator();
        while (iterator.hasNext()) {
            ViewInfo viewinfo = (ViewInfo) iterator.next();
            if (viewinfo.alpha != -1.0f) {
                viewinfo.view.setAlpha(viewinfo.alpha);
            }
        }
        animateinfo.clear();
    }

    public boolean animateDispatchDraw(Canvas canvas, int transitonX, boolean isScrolling, int leftPage, int rightPage) {
        if (this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = this.mTargetView;
        int childCount = vg.getChildCount();
        AnimateInfo animateInfo = new AnimateInfo(0);
        animateInfo.drawingTime = vg.getDrawingTime();
        animateInfo.isScrolling = isScrolling;
        View child = vg.getChildAt(0);
        if (this.mActiveTransition.isHorizental()) {
            this.mFirstOffset = child.getLeft();
        } else {
            this.mFirstOffset = child.getTop();
        }
        child = vg.getChildAt(leftPage);
        float scrollProgress = TransitionUtil.getScrollProgress(vg, transitonX, child, leftPage, this.mPageSpacing);
        if (scrollProgress >= 1.0f) {
            return false;
        }
        scrollProgress = ((float) ((int) (100000.0f * scrollProgress))) / 100000.0f;
        if (scrollProgress != 0.0f || (isScrolling ^ 1) == 0) {
            ViewInfo leftViewInfo = new ViewInfo(child);
            leftViewInfo.fraction = scrollProgress;
            leftViewInfo.index = leftPage;
            animateInfo.viewInfos.add(leftViewInfo);
            if (scrollProgress > 0.0f && rightPage <= childCount - 1 && rightPage != leftPage) {
                ViewInfo rightViewInfo = new ViewInfo(vg.getChildAt(rightPage));
                rightViewInfo.fraction = scrollProgress - 1.0f;
                rightViewInfo.index = rightPage;
                animateInfo.viewInfos.add(rightViewInfo);
            }
            animView(canvas, animateInfo);
            Iterator<ViewInfo> iterator = animateInfo.viewInfos.iterator();
            while (iterator.hasNext()) {
                ViewInfo viewinfo1 = (ViewInfo) iterator.next();
                if (!(viewinfo1.alpha == -1.0f || (this.mAlphaViews.contains(viewinfo1.view) ^ 1) == 0)) {
                    this.mAlphaViews.add(viewinfo1.view);
                    this.mAlphas.add(Float.valueOf(viewinfo1.alpha));
                    this.mLayerType.add(Integer.valueOf(viewinfo1.layerType));
                }
            }
            animateInfo.clear();
            return true;
        }
        int sz = this.mAlphaViews.size();
        for (int j = 0; j < sz; j++) {
            ((View) this.mAlphaViews.get(j)).setAlpha(((Float) this.mAlphas.get(j)).floatValue());
            ((View) this.mAlphaViews.get(j)).setLayerType(((Integer) this.mLayerType.get(j)).intValue(), null);
        }
        this.mAlphaViews.clear();
        return false;
    }

    public boolean animateDispatchDraw(Canvas canvas, float scrollProgress, boolean isScrolling, int leftPage, int rightPage) {
        if (this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = this.mTargetView;
        int childCount = vg.getChildCount();
        AnimateInfo animateInfo = new AnimateInfo(0);
        animateInfo.drawingTime = vg.getDrawingTime();
        animateInfo.isScrolling = isScrolling;
        if (scrollProgress >= 1.0f) {
            return false;
        }
        View child = vg.getChildAt(0);
        if (this.mActiveTransition.isHorizental()) {
            this.mFirstOffset = child.getLeft();
        } else {
            this.mFirstOffset = child.getTop();
        }
        child = vg.getChildAt(leftPage);
        scrollProgress = ((float) ((int) (100000.0f * scrollProgress))) / 100000.0f;
        if (scrollProgress != 0.0f || (isScrolling ^ 1) == 0) {
            ViewInfo leftViewInfo = new ViewInfo(child);
            leftViewInfo.fraction = scrollProgress;
            leftViewInfo.index = leftPage;
            animateInfo.viewInfos.add(leftViewInfo);
            if (scrollProgress > 0.0f && rightPage <= childCount - 1 && rightPage != leftPage) {
                ViewInfo rightViewInfo = new ViewInfo(vg.getChildAt(rightPage));
                rightViewInfo.fraction = scrollProgress - 1.0f;
                rightViewInfo.index = rightPage;
                animateInfo.viewInfos.add(rightViewInfo);
            }
            animView(canvas, animateInfo);
            Iterator<ViewInfo> iterator = animateInfo.viewInfos.iterator();
            while (iterator.hasNext()) {
                ViewInfo viewinfo1 = (ViewInfo) iterator.next();
                if (!(viewinfo1.alpha == -1.0f || (this.mAlphaViews.contains(viewinfo1.view) ^ 1) == 0)) {
                    this.mAlphaViews.add(viewinfo1.view);
                    this.mAlphas.add(Float.valueOf(viewinfo1.alpha));
                    this.mLayerType.add(Integer.valueOf(viewinfo1.layerType));
                }
            }
            animateInfo.clear();
            return true;
        }
        int sz = this.mAlphaViews.size();
        for (int j = 0; j < sz; j++) {
            ((View) this.mAlphaViews.get(j)).setAlpha(((Float) this.mAlphas.get(j)).floatValue());
            ((View) this.mAlphaViews.get(j)).setLayerType(((Integer) this.mLayerType.get(j)).intValue(), null);
        }
        this.mAlphaViews.clear();
        return false;
    }

    public boolean animateDraw(Canvas paramCanvas) {
        if (this.mAnimInfos.size() <= 0) {
            return false;
        }
        if (this.mTargetView == null || this.mActiveTransition == null || this.mForceDraw) {
            if (this.mForceDraw) {
                this.mForceDraw = false;
            }
            return false;
        }
        long currentTime = this.mTargetView.getDrawingTime();
        Iterator<AnimateInfo> iterator = this.mAnimInfos.iterator();
        while (iterator.hasNext()) {
            float slideTime;
            float normalizedTime;
            AnimateInfo animateinfo = (AnimateInfo) iterator.next();
            if (animateinfo.startTime == -1) {
                animateinfo.startTime = currentTime;
            }
            if (animateinfo.duration != 0) {
                slideTime = ((float) (currentTime - animateinfo.startTime)) / ((float) animateinfo.duration);
            } else if (currentTime < animateinfo.startTime) {
                slideTime = 0.0f;
            } else {
                slideTime = 1.0f;
            }
            slideTime = Math.max(Math.min(slideTime, 1.0f), 0.01f);
            if (animateinfo.reverse) {
                normalizedTime = 1.0f - slideTime;
            } else {
                normalizedTime = slideTime;
            }
            normalizedTime = ((float) ((int) (this.mInterpolator.getInterpolation(normalizedTime) * 100000.0f))) / 100000.0f;
            int size = animateinfo.viewInfos.size();
            for (int i = 0; i < size; i++) {
                ViewInfo viewinfo = (ViewInfo) animateinfo.viewInfos.get(i);
                viewinfo.fraction = normalizedTime;
                if (i % 2 != 0) {
                    viewinfo.fraction -= 1.0f;
                }
            }
            animView(paramCanvas, animateinfo);
            if (slideTime >= 1.0f) {
                this.mAnimInfoEnds.add(animateinfo);
            }
        }
        Iterator<AnimateInfo> localIterator2 = this.mAnimInfoEnds.iterator();
        while (localIterator2.hasNext()) {
            onAnimationEnd((AnimateInfo) localIterator2.next());
        }
        this.mAnimInfoEnds.clear();
        this.mTargetView.invalidate();
        return true;
    }

    public HashMap<Integer, BaseTransition> getAvailableTransitions() {
        return this.mTransitionsMap;
    }

    public float getLayerOffset(float offset, int i) {
        if (i != 1) {
            if (this.mBackground != null && this.mActiveTransition.mUseBg) {
                float step = 1.0f / ((float) (i - 1));
                float overx = offset % step;
                offset -= overx;
                if (((double) (overx / step)) > 0.5d) {
                    offset += step;
                }
            }
            return offset;
        } else if (Float.isNaN(offset)) {
            return 0.0f;
        } else {
            return offset;
        }
    }

    public int getTransitionType() {
        return this.mActiveTransitionType;
    }

    public boolean is3DAnimation() {
        return this.mActiveTransition.getAnimationType().equals("3D");
    }

    public void setAlphaMode(boolean alphaMode) {
        this.mActiveTransition.setAlphaMode(alphaMode);
    }

    public void setBackground(Bitmap bitmap) {
        if (this.mActiveTransition.mUseBg) {
            this.mBackground = bitmap;
            BaseTransition basetransition = this.mActiveTransition;
            if (this.mBackground == null) {
                basetransition.setAlphaMode(true);
            } else {
                basetransition.setAlphaMode(false);
            }
        }
    }

    public void setBackgroundNoZoom(boolean noZoom) {
        this.mBgNoZoom = noZoom;
    }

    public void setBackgroundOffset(boolean bgStatic, float offset) {
        this.mBgStatic = bgStatic;
        this.mOffset = offset;
    }

    public void setCameraDistance(float distance) {
        if (this.mActiveTransition != null) {
            this.mActiveTransition.setCameraDistance(distance);
        }
    }

    public void setLayerTransparent(boolean isTransparent) {
        this.mIsTransparent = isTransparent;
    }

    public void setPageSpacing(int pageSpacing) {
        this.mPageSpacing = pageSpacing;
    }

    public void setTransitionType(int transitionType) {
        BaseTransition basetransition = (BaseTransition) this.mTransitionsMap.get(Integer.valueOf(transitionType));
        if (basetransition != null) {
            this.mActiveTransition = basetransition;
            this.mActiveTransition.reset();
            this.mActiveTransitionType = transitionType;
            return;
        }
        Log.e(TAG, "setTransitionType failed, no such type : " + transitionType);
    }

    public boolean startAnimation(View view) {
        if (!(view == null || this.mActiveTransition == null)) {
            ShadowView shadowview = ShadowView.createShadow(view);
            if (shadowview != null) {
                Iterator<AnimateInfo> iterator = this.mAnimInfos.iterator();
                while (!iterator.hasNext()) {
                    AnimateInfo animateinfo1 = (AnimateInfo) iterator.next();
                    if (((ViewInfo) animateinfo1.viewInfos.get(0)).view != view) {
                        onAnimationEnd(animateinfo1);
                    }
                }
                AnimateInfo animateinfo = new AnimateInfo(1);
                animateinfo.reverse = true;
                this.mActiveTransition.setLayoutType(1);
                this.mActiveTransition.setOrientation(1);
                animateinfo.viewInfos.add(new ViewInfo(view));
                ViewInfo viewinfo1 = new ViewInfo(view);
                viewinfo1.shadowView = shadowview;
                animateinfo.viewInfos.add(viewinfo1);
                this.mAnimInfos.add(animateinfo);
                view.invalidate();
                return true;
            }
        }
        return false;
    }
}
