package com.vivo.common.animation;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListView;
import com.vivo.common.provider.Calendar.Events;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SearchControl implements SearchViewListener {
    public static final int IN_CONTENTVIEW = 1;
    public static final int OUT_OF_CONTENTVIEW = 0;
    public static final int SEARCH_STATE_NORMAOL = 4096;
    public static final int SEARCH_STATE_SEARCH = 4097;
    public static final int SEARCH_STATE_SWITCHING_NORMAOL = 4098;
    public static final int SEARCH_STATE_SWITCHING_SEARCH = 4099;
    private static final int SYSTEM_BGK_COLOR = -723724;
    private static final String TAG = "SearchControl";
    private int SHADOW_ALPHA = 35;
    private ArrayList<View> mAlphaChnageViewList = new ArrayList();
    private AnimationListener mAnimationListener;
    private Context mContext;
    private View mMovingContainer;
    private int mSearchBarType = 0;
    private ListView mSearchList;
    private int mSearchState = 4096;
    private SearchView mSearchView;
    private View mShaowTitleView;
    private int mTitleHeight;
    private View mTitleView;

    public interface AnimationListener {
        void onAnimationEnd(boolean z);

        void onAnimationStart(boolean z);
    }

    public SearchControl(Context context) {
        this.mContext = context;
    }

    private void Log(String str) {
        Log.d(TAG, str);
    }

    public void setSearchList(ListView searchList) {
        this.mSearchList = searchList;
        if (searchList instanceof LKListView) {
            ((LKListView) searchList).setSearchControl(this);
            ((LKListView) searchList).setClickWillBack(true);
        }
    }

    public void setSearchBarType(int type) {
        if (type == 0) {
            this.mSearchBarType = 0;
        } else {
            this.mSearchBarType = 1;
        }
    }

    public void setTitleView(View view) {
        this.mTitleView = view;
    }

    public void addAlphaChageView(View view) {
        this.mAlphaChnageViewList.add(view);
    }

    void setSearchView(SearchView searchView) {
        this.mSearchView = searchView;
        if (this.mSearchView != null) {
            this.mSearchView.setAnimatorProgressListener(this);
        }
    }

    public void onSearchTextChanged(String text) {
        Log("onSearchTextChanged");
        if (text.equals(Events.DEFAULT_SORT_ORDER)) {
            this.mSearchList.setBackgroundColor(this.SHADOW_ALPHA << 24);
            this.mSearchList.setAdapter(null);
            return;
        }
        if (this.mSearchView.getSearchResoultBackground() instanceof ColorDrawable) {
            this.mSearchList.setBackgroundColor(((ColorDrawable) this.mSearchView.getSearchResoultBackground()).getColor());
        } else {
            this.mSearchList.setBackground(this.mSearchView.getSearchResoultBackground());
        }
    }

    public void onSwitchToSearchStateStart() {
        Log("onSwitchToSearchStateStart");
        this.mSearchList.setBackgroundColor(0);
        this.mSearchList.setVisibility(0);
        this.mSearchList.setAlpha(1.0f);
        this.mSearchList.setAdapter(null);
        this.mTitleHeight = this.mTitleView.getHeight();
        if (this.mSearchBarType == 1 && this.mSearchView.mIScrollLock != null) {
            this.mSearchView.mIScrollLock.lockScroll();
        }
        ((MarginLayoutParams) this.mSearchList.getLayoutParams()).topMargin = this.mSearchView.getHeight();
        ((MarginLayoutParams) this.mTitleView.getLayoutParams()).topMargin = -this.mTitleHeight;
        this.mShaowTitleView.setVisibility(0);
        requestLayout();
        if (this.mAnimationListener != null) {
            this.mAnimationListener.onAnimationStart(false);
        }
        this.mSearchState = 4099;
    }

    private void requestLayout() {
        this.mTitleView.requestLayout();
        this.mSearchView.requestLayout();
    }

    public void onSwitchToSearchStateEnd() {
        Log("onSwitchToSearchStateEnd");
        hidenAlphaView();
        if (this.mAnimationListener != null) {
            this.mAnimationListener.onAnimationEnd(false);
        }
        this.mSearchState = 4097;
    }

    public void onSwitchingToSearch(float p) {
        this.mMovingContainer.setY(((float) this.mTitleHeight) * (1.0f - p));
        this.mSearchList.setBackgroundColor(((int) (((float) this.SHADOW_ALPHA) * p)) << 24);
        changeAlphaView(1.0f - p);
        this.mShaowTitleView.setY(((float) (-this.mTitleHeight)) * p);
    }

    public void onSwitchToNormalStateStart() {
        Log("onSwitchToNormalStateStart");
        showAlphaView();
        if (this.mAnimationListener != null) {
            this.mAnimationListener.onAnimationStart(true);
        }
        this.mSearchState = 4098;
    }

    public void onSwitchToNormalStateEnd() {
        Log("onSwitchToNormalStateEnd");
        if (this.mSearchBarType == 1 && this.mSearchView.mIScrollLock != null) {
            this.mSearchView.mIScrollLock.unLockScroll();
        }
        ((MarginLayoutParams) this.mTitleView.getLayoutParams()).topMargin = 0;
        requestLayout();
        this.mMovingContainer.setY(0.0f);
        this.mShaowTitleView.setVisibility(4);
        this.mSearchList.setVisibility(4);
        if (this.mAnimationListener != null) {
            this.mAnimationListener.onAnimationEnd(true);
        }
        this.mSearchState = 4096;
    }

    public void onSwitchingToNormal(float p) {
        this.mMovingContainer.setY(((float) this.mTitleHeight) * (1.0f - p));
        this.mShaowTitleView.setY(((float) (-this.mTitleHeight)) * p);
        this.mSearchList.setAlpha(p);
        changeAlphaView(1.0f - p);
    }

    public void setMovingContainer(View view) {
        this.mMovingContainer = view;
    }

    private void changeAlphaView(float alpha) {
        int size = this.mAlphaChnageViewList.size();
        for (int i = 0; i < size; i++) {
            ((View) this.mAlphaChnageViewList.get(i)).setAlpha(alpha);
        }
    }

    private void hidenAlphaView() {
        int size = this.mAlphaChnageViewList.size();
        for (int i = 0; i < size; i++) {
            ((View) this.mAlphaChnageViewList.get(i)).setVisibility(4);
        }
    }

    private void showAlphaView() {
        int size = this.mAlphaChnageViewList.size();
        for (int i = 0; i < size; i++) {
            ((View) this.mAlphaChnageViewList.get(i)).setVisibility(0);
        }
    }

    public void setShadowAlpha(int alpha) {
        this.SHADOW_ALPHA = alpha;
    }

    public void setFakeTitleView(View view) {
        this.mShaowTitleView = view;
    }

    void switchToNormalDirectlyEnd() {
        onSwitchToNormalStateStart();
        onSwitchingToNormal(1.0f);
        onSwitchToNormalStateEnd();
    }

    void switchToSearchModleDirectlyEnd() {
        onSwitchToSearchStateStart();
        onSwitchingToSearch(1.0f);
        onSwitchToSearchStateEnd();
    }

    public void switchToNormal() {
        if (this.mSearchView != null) {
            this.mSearchView.hidenSearch();
        }
    }

    public int getSearchState() {
        return this.mSearchState;
    }

    public void setSwitchWithAnimate(boolean value) {
        if (this.mSearchView != null) {
            this.mSearchView.setSwitchWithAnimate(value);
        }
    }

    public void setAnimationListener(AnimationListener l) {
        this.mAnimationListener = l;
    }
}
