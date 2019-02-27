package android.view;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface.OutOfResourcesException;

public class SurfaceControlWithSubSurface extends SurfaceControl {
    private final int mFlags;
    private final int mFormat;
    private final String mName;
    private final SurfaceSession mSession;
    private SurfaceControl mSubSurface;

    public SurfaceControlWithSubSurface(SurfaceSession s, String name, int w, int h, int format, int flags) throws OutOfResourcesException {
        super(s, name, w, h, format, flags);
        this.mSession = s;
        this.mName = name;
        this.mFormat = format;
        this.mFlags = flags;
    }

    protected SurfaceControl createSubSurface(int w, int h) {
        if (this.mSubSurface != null) {
            return this.mSubSurface;
        }
        this.mSubSurface = onCreateSubSurface(this.mSession, this.mName, w, h, this.mFormat, this.mFlags);
        return this.mSubSurface;
    }

    public SurfaceControl getSubSurface() {
        return this.mSubSurface;
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        onPositionChanged(x, y);
    }

    public void setSize(int w, int h) {
        super.setSize(w, h);
        onSizeChanged(w, h);
    }

    public void hide() {
        super.hide();
        onVisiblilityChanged(false);
    }

    public void show() {
        super.show();
        onVisiblilityChanged(true);
    }

    public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        super.setMatrix(dsdx, dtdx, dsdy, dtdy);
        onMatrixChanged(dsdx, dtdx, dsdy, dtdy);
    }

    public void setWindowCrop(Rect crop) {
        super.setWindowCrop(crop);
        onCropChanged(crop);
    }

    public void setFinalCrop(Rect crop) {
        super.setFinalCrop(crop);
        onFinalCropChanged(crop);
    }

    public void setLayer(int zorder) {
        super.setLayer(zorder);
        onLayerChanged(zorder);
    }

    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (this.mSubSurface != null) {
            this.mSubSurface.setAlpha(alpha);
        }
    }

    public void setLayerStack(int layerStack) {
        super.setLayerStack(layerStack);
        if (this.mSubSurface != null) {
            this.mSubSurface.setLayerStack(layerStack);
        }
    }

    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        if (this.mSubSurface != null) {
            this.mSubSurface.setOpaque(isOpaque);
        }
    }

    public void setSecure(boolean isSecure) {
        super.setSecure(isSecure);
        if (this.mSubSurface != null) {
            this.mSubSurface.setSecure(isSecure);
        }
    }

    public void destroy() {
        super.destroy();
        if (this.mSubSurface != null) {
            this.mSubSurface.destroy();
        }
    }

    public void release() {
        super.release();
        if (this.mSubSurface != null) {
            this.mSubSurface.release();
        }
    }

    public void deferTransactionUntil(IBinder handle, long frame) {
        super.deferTransactionUntil(handle, frame);
        if (this.mSubSurface != null) {
            this.mSubSurface.deferTransactionUntil(handle, frame);
        }
    }

    protected SurfaceControl onCreateSubSurface(SurfaceSession s, String name, int w, int h, int format, int flags) {
        return new SurfaceControl(s, name + ":Sub", w, h, format, flags);
    }

    protected void onSizeChanged(int w, int h) {
    }

    protected void onPositionChanged(float x, float y) {
    }

    protected void onMatrixChanged(float dsdx, float dtdx, float dsdy, float dtdy) {
    }

    protected void onVisiblilityChanged(boolean visible) {
    }

    protected void onLayerChanged(int layer) {
    }

    protected void onCropChanged(Rect crop) {
    }

    protected void onFinalCropChanged(Rect crop) {
    }

    protected void setMainWindowCrop(Rect crop) {
        super.setWindowCrop(crop);
    }
}
