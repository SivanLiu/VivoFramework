package com.vivo.app;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.vivo.common.BbkTitleView;

final class VivoTitleImpl implements IVivoTitle {
    private Activity mActivity;
    private BbkTitleView mBbkTitleView;
    private OnClickListener mDefaultLeftButtonClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (VivoTitleImpl.this.mActivity != null) {
                VivoTitleImpl.this.mActivity.finish();
            }
        }
    };

    public VivoTitleImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void init(View rootView) {
        if (rootView != null) {
            this.mBbkTitleView = (BbkTitleView) rootView.findViewById(51183767);
        } else {
            this.mBbkTitleView = (BbkTitleView) this.mActivity.findViewById(51183767);
        }
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setLeftButtonClickListener(this.mDefaultLeftButtonClickListener);
        }
    }

    public void setTitle(CharSequence title) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setCenterText(title);
        }
    }

    public Button getTitleLeftButton() {
        return this.mBbkTitleView == null ? null : this.mBbkTitleView.getLeftButton();
    }

    public Button getTitleRightButton() {
        return this.mBbkTitleView == null ? null : this.mBbkTitleView.getRightButton();
    }

    public void initTitleLeftButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.initLeftButton(leftText, drawableId, listener);
        }
    }

    public void initTitleRightButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.initRightButton(leftText, drawableId, listener);
        }
    }

    public void setTitleLeftButtonEnable(boolean enable) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setLeftButtonEnable(enable);
        }
    }

    public void setTitleRightButtonEnable(boolean enable) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setRightButtonEnable(enable);
        }
    }

    public void setTitleLeftButtonText(CharSequence text) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setLeftButtonText(text);
        }
    }

    public void setTitleRightButtonText(CharSequence text) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setRightButtonText(text);
        }
    }

    public void setTitleLeftButtonIcon(int drawableId) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setLeftButtonIcon(drawableId);
        }
    }

    public void setTitleRightButtonIcon(int drawableId) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setRightButtonIcon(drawableId);
        }
    }

    public void setTitleLeftButtonClickListener(OnClickListener listener) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setLeftButtonClickListener(listener);
        }
    }

    public void setTitleRightButtonClickListener(OnClickListener listener) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setRightButtonClickListener(listener);
        }
    }

    public void showTitleLeftButton() {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.showLeftButton();
        }
    }

    public void showTitleRightButton() {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.showRightButton();
        }
    }

    public void hideTitleLeftButton() {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.hideLeftButton();
        }
    }

    public void hideTitleRightButton() {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.hideRightButton();
        }
    }

    public TextView getTitleCenterView() {
        return this.mBbkTitleView == null ? null : this.mBbkTitleView.getCenterView();
    }

    public void setTitleCenterText(CharSequence text) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setCenterText(text);
        }
    }

    public void setTitleCenterSubText(CharSequence text) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setCenterSubText(text);
        }
    }

    public void setTitleCenterSubViewVisible(boolean visible) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setCenterSubViewVisible(visible);
        }
    }

    public void setOnTitleClickListener(OnClickListener listener) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setOnTitleClickListener(listener);
        }
    }

    public void setOnTitleClickListener(View view) {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setOnTitleClickListener(view);
        }
    }

    public void setOnTitleClickListener() {
        if (this.mBbkTitleView != null) {
            this.mBbkTitleView.setOnTitleClickListener();
        }
    }
}
