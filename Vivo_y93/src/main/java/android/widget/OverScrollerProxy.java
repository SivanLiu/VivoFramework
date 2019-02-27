package android.widget;

import android.content.Context;
import android.view.animation.Interpolator;

public class OverScrollerProxy {
    public static final int SPRING_EFFECT = 2;
    public static final int SPRING_EFFECT_EX = 3;
    public static final int SYSTEM_DEFAULT = 1;
    private int mEffect = 1;
    private Object mScroller;

    public OverScrollerProxy(int effect, Context context) {
        this.mEffect = effect;
        switch (this.mEffect) {
            case 1:
                this.mScroller = new OverScroller(context);
                return;
            case 2:
                this.mScroller = new VivoOverScroller(context);
                return;
            case 3:
                this.mScroller = new SpringOverScroller(context);
                return;
            default:
                return;
        }
    }

    void setInterpolator(Interpolator interpolator) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).setInterpolator(interpolator);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).setInterpolator(interpolator);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).setInterpolator(interpolator);
                return;
            default:
                return;
        }
    }

    public final void setFriction(float friction) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).setFriction(friction);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).setFriction(friction);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).setFriction(friction);
                return;
            default:
                return;
        }
    }

    public final boolean isFinished() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).isFinished();
            case 2:
                return ((VivoOverScroller) this.mScroller).isFinished();
            case 3:
                return ((SpringOverScroller) this.mScroller).isFinished();
            default:
                return false;
        }
    }

    public final void forceFinished(boolean finished) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).forceFinished(finished);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).forceFinished(finished);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).forceFinished(finished);
                return;
            default:
                return;
        }
    }

    public final int getCurrX() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getCurrX();
            case 2:
                return ((VivoOverScroller) this.mScroller).getCurrX();
            case 3:
                return ((SpringOverScroller) this.mScroller).getCurrX();
            default:
                return 0;
        }
    }

    public final int getCurrY() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getCurrY();
            case 2:
                return ((VivoOverScroller) this.mScroller).getCurrY();
            case 3:
                return ((SpringOverScroller) this.mScroller).getCurrY();
            default:
                return 0;
        }
    }

    public float getCurrVelocity() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getCurrVelocity();
            case 2:
                return ((VivoOverScroller) this.mScroller).getCurrVelocity();
            case 3:
                return ((SpringOverScroller) this.mScroller).getCurrVelocity();
            default:
                return 0.0f;
        }
    }

    public final int getStartX() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getStartX();
            case 2:
                return ((VivoOverScroller) this.mScroller).getStartX();
            case 3:
                return ((SpringOverScroller) this.mScroller).getStartX();
            default:
                return 0;
        }
    }

    public final int getStartY() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getStartY();
            case 2:
                return ((VivoOverScroller) this.mScroller).getStartY();
            case 3:
                return ((SpringOverScroller) this.mScroller).getStartY();
            default:
                return 0;
        }
    }

    public final int getFinalX() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getFinalX();
            case 2:
                return ((VivoOverScroller) this.mScroller).getFinalX();
            case 3:
                return ((SpringOverScroller) this.mScroller).getFinalX();
            default:
                return 0;
        }
    }

    public final int getFinalY() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getFinalY();
            case 2:
                return ((VivoOverScroller) this.mScroller).getFinalY();
            case 3:
                return ((SpringOverScroller) this.mScroller).getFinalY();
            default:
                return 0;
        }
    }

    public final int getDuration() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).getDuration();
            case 2:
                return ((VivoOverScroller) this.mScroller).getDuration();
            case 3:
                return ((SpringOverScroller) this.mScroller).getDuration();
            default:
                return 0;
        }
    }

    public void extendDuration(int extend) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).extendDuration(extend);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).extendDuration(extend);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).extendDuration(extend);
                return;
            default:
                return;
        }
    }

    public void setFinalX(int newX) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).setFinalX(newX);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).setFinalX(newX);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).setFinalX(newX);
                return;
            default:
                return;
        }
    }

    public void setFinalY(int newY) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).setFinalY(newY);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).setFinalY(newY);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).setFinalY(newY);
                return;
            default:
                return;
        }
    }

    public boolean computeScrollOffset() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).computeScrollOffset();
            case 2:
                return ((VivoOverScroller) this.mScroller).computeScrollOffset();
            case 3:
                return ((SpringOverScroller) this.mScroller).computeScrollOffset();
            default:
                return false;
        }
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).startScroll(startX, startY, dx, dy);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).startScroll(startX, startY, dx, dy);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).startScroll(startX, startY, dx, dy);
                return;
            default:
                return;
        }
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).startScroll(startX, startY, dx, dy, duration);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).startScroll(startX, startY, dx, dy, duration);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).startScroll(startX, startY, dx, dy, duration);
                return;
            default:
                return;
        }
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).springBack(startX, startY, minX, maxX, minY, maxY);
            case 2:
                return ((VivoOverScroller) this.mScroller).springBack(startX, startY, minX, maxX, minY, maxY);
            case 3:
                return ((SpringOverScroller) this.mScroller).springBack(startX, startY, minX, maxX, minY, maxY);
            default:
                return false;
        }
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                return;
            default:
                return;
        }
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
                return;
            default:
                return;
        }
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).notifyHorizontalEdgeReached(startX, finalX, overX);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).notifyHorizontalEdgeReached(startX, finalX, overX);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).notifyHorizontalEdgeReached(startX, finalX, overX);
                return;
            default:
                return;
        }
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).notifyVerticalEdgeReached(startY, finalY, overY);
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).notifyVerticalEdgeReached(startY, finalY, overY);
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).notifyVerticalEdgeReached(startY, finalY, overY);
                return;
            default:
                return;
        }
    }

    public boolean isOverScrolled() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).isOverScrolled();
            case 2:
                return ((VivoOverScroller) this.mScroller).isOverScrolled();
            case 3:
                return ((SpringOverScroller) this.mScroller).isOverScrolled();
            default:
                return false;
        }
    }

    public void abortAnimation() {
        switch (this.mEffect) {
            case 1:
                ((OverScroller) this.mScroller).abortAnimation();
                return;
            case 2:
                ((VivoOverScroller) this.mScroller).abortAnimation();
                return;
            case 3:
                ((SpringOverScroller) this.mScroller).abortAnimation();
                return;
            default:
                return;
        }
    }

    public int timePassed() {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).timePassed();
            case 2:
                return ((VivoOverScroller) this.mScroller).timePassed();
            case 3:
                return ((SpringOverScroller) this.mScroller).timePassed();
            default:
                return 0;
        }
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        switch (this.mEffect) {
            case 1:
                return ((OverScroller) this.mScroller).isScrollingInDirection(xvel, yvel);
            case 2:
                return ((VivoOverScroller) this.mScroller).isScrollingInDirection(xvel, yvel);
            case 3:
                return ((SpringOverScroller) this.mScroller).isScrollingInDirection(xvel, yvel);
            default:
                return false;
        }
    }
}
