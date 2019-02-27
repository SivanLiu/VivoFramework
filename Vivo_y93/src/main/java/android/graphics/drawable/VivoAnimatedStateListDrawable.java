package android.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;

public class VivoAnimatedStateListDrawable extends AnimatedStateListDrawable {
    private static final String TAG = "VivoAnimatedStateListDrawable";
    private float globaltheme;
    private boolean mMutated;
    private VivoAnimatedStateListState mState;

    static class VivoAnimatedStateListState extends AnimatedStateListState {
        VivoAnimatedStateListState(AnimatedStateListState orig, AnimatedStateListDrawable owner, Resources res) {
            super(orig, owner, res);
        }

        public Drawable newDrawable() {
            return new VivoAnimatedStateListDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new VivoAnimatedStateListDrawable(this, res, null);
        }
    }

    /* synthetic */ VivoAnimatedStateListDrawable(AnimatedStateListState state, Resources res, VivoAnimatedStateListDrawable -this2) {
        this(state, res);
    }

    VivoAnimatedStateListDrawable() {
        this(null, null);
        this.globaltheme = Resources.getSystem().getDimension(51118202);
    }

    protected boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);
        if (this.globaltheme == 0.0f) {
            jumpToCurrentState();
        }
        return changed;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    protected void setConstantState(DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof VivoAnimatedStateListState) {
            this.mState = (VivoAnimatedStateListState) state;
        }
    }

    private VivoAnimatedStateListDrawable(AnimatedStateListState state, Resources res) {
        this.globaltheme = Resources.getSystem().getDimension(51118202);
        setConstantState(new VivoAnimatedStateListState(state, this, res));
        onStateChange(getState());
        jumpToCurrentState();
    }
}
