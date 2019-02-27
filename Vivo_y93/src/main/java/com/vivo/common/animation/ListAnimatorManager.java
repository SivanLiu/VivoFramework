package com.vivo.common.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.FtBuild;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.ListView;
import com.vivo.common.provider.Calendar.CalendarsColumns;
import com.vivo.internal.R;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class ListAnimatorManager {
    private static final int AM_INTERAVAL = 350;
    public static final int STATE_IN_CHECK = 4098;
    public static final int STATE_IN_HIDEN_CHECK_AM = 4099;
    public static final int STATE_IN_NORMAL = 4096;
    public static final int STATE_IN_SHOW_CHECK_AM = 4097;
    private static final String TAG = "ListAnimatorManager";
    private final int AM_INTERAVAL_LEGACY;
    private ValueAnimator mAnimator;
    private Context mContext;
    private ArrayList<IListEditControl> mControlSet;
    private float mCurrentProgess;
    private int mDrawableID;
    private AnimatorListener mHidenListener;
    private IListControlHook mIListControlHook;
    private int mLeft;
    private ListView mListView;
    private boolean mLtoR;
    private int mRight;
    private AnimatorListener mShowListener;
    private int mState;
    private int mStyle;
    private int mTop;
    private AnimatorUpdateListener mUpdateListener;

    public interface IListControlHook {
        void onAmProgress(float f, boolean z);

        void onAnimationEnd(boolean z);

        void onAnimationStart(boolean z);

        void onInitalListEditControl(ListEditControl listEditControl, View view);
    }

    public ListAnimatorManager(Context context, int sytle) {
        TypedArray a;
        this.mControlSet = new ArrayList();
        this.mState = 4096;
        this.mCurrentProgess = 0.0f;
        this.mLeft = 0;
        this.mRight = 0;
        this.mTop = -1;
        this.mLtoR = true;
        this.mStyle = -1;
        this.AM_INTERAVAL_LEGACY = CalendarsColumns.RESPOND_ACCESS;
        this.mShowListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ListAnimatorManager.this.mListView.setChoiceMode(2);
                ListAnimatorManager.this.mIListControlHook.onAnimationStart(true);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ListAnimatorManager.this.mState = 4098;
                ListAnimatorManager.this.mIListControlHook.onAnimationEnd(true);
            }

            public void onAnimationCancel(Animator animation) {
            }
        };
        this.mHidenListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ListAnimatorManager.this.mIListControlHook.onAnimationStart(false);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ListAnimatorManager.this.mState = 4096;
                ListAnimatorManager.this.reset();
                ListAnimatorManager.this.mIListControlHook.onAnimationEnd(false);
            }

            public void onAnimationCancel(Animator animation) {
            }
        };
        this.mUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                ListAnimatorManager.this.mCurrentProgess = value;
                ListAnimatorManager.this.updateUIProgress(value);
                if (ListAnimatorManager.this.mIListControlHook != null) {
                    ListAnimatorManager.this.mIListControlHook.onAmProgress(value, ListAnimatorManager.this.mState != 4099);
                }
            }
        };
        this.mStyle = sytle;
        this.mContext = context;
        if (this.mStyle == 0) {
            a = this.mContext.obtainStyledAttributes(R.styleable.VivoTheme);
            this.mStyle = a.getResourceId(23, 51314979);
            a.recycle();
        }
        a = this.mContext.obtainStyledAttributes(this.mStyle, R.styleable.EditorMode);
        this.mLeft = a.getDimensionPixelSize(1, 0);
        this.mRight = a.getDimensionPixelSize(2, 0);
        this.mTop = a.getDimensionPixelSize(3, -1);
        this.mLtoR = a.getBoolean(4, true);
        this.mDrawableID = a.getResourceId(0, 50462835);
        a.recycle();
    }

    public ListAnimatorManager(Context context) {
        this(context, 0);
    }

    private void Log(String str) {
        Log.d(TAG, str);
    }

    private void reset() {
        this.mListView.clearChoices();
        this.mListView.setChoiceMode(0);
        int size = this.mControlSet.size();
        for (int i = 0; i < size; i++) {
            ((IListEditControl) this.mControlSet.get(i)).getEditControl().setChecked(false);
        }
    }

    private void addListEditControl(IListEditControl control) {
        int size = this.mControlSet.size();
        int i = 0;
        while (i < size) {
            if (!((IListEditControl) this.mControlSet.get(i)).equals(control)) {
                i++;
            } else {
                return;
            }
        }
        this.mControlSet.add(control);
    }

    private void updateListValidItem(View skipView) {
        int size = this.mControlSet.size();
        int i = 0;
        while (i < size) {
            View view = (View) this.mControlSet.get(i);
            if (view.equals(skipView)) {
                i++;
            } else if (this.mListView.indexOfChild(view) == -1) {
                size--;
                this.mControlSet.remove(i);
            } else {
                i++;
            }
        }
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public void switchToEditModel() {
        if (this.mState == 4096) {
            this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            if (FtBuild.getRomVersion() >= 3.0f) {
                this.mAnimator.setInterpolator(new PathInterpolator(0.2f, 0.2f, 0.2f, 1.0f));
                this.mAnimator.setDuration(350);
            } else {
                this.mAnimator.setDuration(300);
            }
            this.mAnimator.addListener(this.mShowListener);
            this.mAnimator.addUpdateListener(this.mUpdateListener);
            this.mAnimator.start();
            this.mState = 4097;
        }
    }

    public void switchToEditModelDirectly() {
        if (this.mState == 4096) {
            this.mCurrentProgess = 1.0f;
            updateUIProgress(1.0f);
            if (this.mIListControlHook != null) {
                this.mIListControlHook.onAmProgress(1.0f, true);
            }
            this.mListView.setChoiceMode(2);
            this.mState = 4098;
        }
    }

    public void swtichToNormal() {
        if (this.mState == 4098) {
            this.mAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            if (FtBuild.getRomVersion() >= 3.0f) {
                this.mAnimator.setInterpolator(new PathInterpolator(0.2f, 0.2f, 0.2f, 1.0f));
                this.mAnimator.setDuration(350);
            } else {
                this.mAnimator.setDuration(300);
            }
            this.mAnimator.addListener(this.mHidenListener);
            this.mAnimator.addUpdateListener(this.mUpdateListener);
            this.mAnimator.start();
            this.mState = 4099;
        }
    }

    public void switchToNormalDirectly() {
        if (this.mState == 4098) {
            this.mCurrentProgess = 0.0f;
            updateUIProgress(0.0f);
            if (this.mIListControlHook != null) {
                this.mIListControlHook.onAmProgress(0.0f, false);
            }
            reset();
            this.mState = 4096;
        }
    }

    public void updateControlList(View item) {
        if (item instanceof IListEditControl) {
            IListEditControl control = (IListEditControl) item;
            addListEditControl(control);
            control.getEditControl().clearAnimateChildView();
            if (!control.getEditControl().isInit()) {
                control.getEditControl().init(this.mContext.getResources().getDrawable(this.mDrawableID).mutate(), this.mLeft, this.mTop, this.mRight, this.mLtoR);
            }
            if (this.mIListControlHook != null) {
                this.mIListControlHook.onInitalListEditControl(control.getEditControl(), item);
            }
            control.getEditControl().setLayoutRtl(this.mListView.isLayoutRtl());
            control.getEditControl().onAnimateUpdate(this.mCurrentProgess);
            updateListValidItem(item);
            return;
        }
        Log("Exception: updateControlList--  the item is not IListEditControl type");
    }

    public void endCurrentAnimate() {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.end();
        }
    }

    private void updateUIProgress(float p) {
        int size = this.mControlSet.size();
        for (int i = 0; i < size; i++) {
            ((IListEditControl) this.mControlSet.get(i)).getEditControl().onAnimateUpdate(p);
        }
    }

    public int getListState() {
        return this.mState;
    }

    public void setListControlHook(IListControlHook hook) {
        this.mIListControlHook = hook;
    }

    public void setCheckLeftPadding(int padding) {
        this.mLeft = padding;
    }

    public void setCheckRightPadding(int padding) {
        this.mRight = padding;
    }

    public void setCheckTopPadding(int padding) {
        this.mTop = padding;
    }

    public void setCheckAlignLeft(boolean value) {
        this.mLtoR = value;
    }

    public void setCheckDrawable(int resId) {
        this.mDrawableID = resId;
    }
}
