package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoBaseActivity extends SavePowerActivity implements IVivoTitle {
    private FrameLayout mContentLayout;
    private LayoutInflater mLayoutInflater;
    private VivoTitleImpl mVivoTitleImpl = new VivoTitleImpl(this);

    public boolean useVivoCommonTitle() {
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (useVivoCommonTitle()) {
            requestWindowFeature(1);
            this.mLayoutInflater = LayoutInflater.from(this);
            ViewGroup rootView = (ViewGroup) this.mLayoutInflater.inflate(50528355, null);
            super.setContentView(rootView, new LayoutParams(-1, -1));
            initViews(rootView);
        }
        super.onCreate(savedInstanceState);
    }

    private void initViews(View view) {
        this.mContentLayout = (FrameLayout) view.findViewById(16908290);
        this.mVivoTitleImpl.init(view);
        CharSequence title = super.getTitle();
        if (title != null) {
            this.mVivoTitleImpl.setTitleCenterText(title);
        }
    }

    public void setContentView(View view, LayoutParams params) {
        if (!useVivoCommonTitle() || this.mContentLayout == null) {
            super.setContentView(view, params);
        } else {
            this.mContentLayout.addView(view, params);
        }
    }

    public void setContentView(int layoutResID) {
        if (!useVivoCommonTitle() || this.mContentLayout == null) {
            super.setContentView(layoutResID);
        } else {
            this.mLayoutInflater.inflate(layoutResID, this.mContentLayout);
        }
    }

    public void setTitle(CharSequence title) {
        this.mVivoTitleImpl.setTitle(title);
        super.setTitle(title);
    }

    public Button getTitleLeftButton() {
        return this.mVivoTitleImpl.getTitleLeftButton();
    }

    public Button getTitleRightButton() {
        return this.mVivoTitleImpl.getTitleRightButton();
    }

    public void initTitleLeftButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        this.mVivoTitleImpl.initTitleLeftButton(leftText, drawableId, listener);
    }

    public void initTitleRightButton(CharSequence leftText, int drawableId, OnClickListener listener) {
        this.mVivoTitleImpl.initTitleRightButton(leftText, drawableId, listener);
    }

    public void setTitleLeftButtonEnable(boolean enable) {
        this.mVivoTitleImpl.setTitleLeftButtonEnable(enable);
    }

    public void setTitleRightButtonEnable(boolean enable) {
        this.mVivoTitleImpl.setTitleRightButtonEnable(enable);
    }

    public void setTitleLeftButtonText(CharSequence text) {
        this.mVivoTitleImpl.setTitleLeftButtonText(text);
    }

    public void setTitleRightButtonText(CharSequence text) {
        this.mVivoTitleImpl.setTitleRightButtonText(text);
    }

    public void setTitleLeftButtonIcon(int drawableId) {
        this.mVivoTitleImpl.setTitleLeftButtonIcon(drawableId);
    }

    public void setTitleRightButtonIcon(int drawableId) {
        this.mVivoTitleImpl.setTitleRightButtonIcon(drawableId);
    }

    public void setTitleLeftButtonClickListener(OnClickListener listener) {
        this.mVivoTitleImpl.setTitleLeftButtonClickListener(listener);
    }

    public void setTitleRightButtonClickListener(OnClickListener listener) {
        this.mVivoTitleImpl.setTitleRightButtonClickListener(listener);
    }

    public void showTitleLeftButton() {
        this.mVivoTitleImpl.showTitleLeftButton();
    }

    public void showTitleRightButton() {
        this.mVivoTitleImpl.showTitleRightButton();
    }

    public void hideTitleLeftButton() {
        this.mVivoTitleImpl.hideTitleLeftButton();
    }

    public void hideTitleRightButton() {
        this.mVivoTitleImpl.hideTitleRightButton();
    }

    public TextView getTitleCenterView() {
        return this.mVivoTitleImpl.getTitleCenterView();
    }

    public void setTitleCenterText(CharSequence text) {
        this.mVivoTitleImpl.setTitleCenterText(text);
    }

    public void setTitleCenterSubText(CharSequence text) {
        this.mVivoTitleImpl.setTitleCenterSubText(text);
    }

    public void setTitleCenterSubViewVisible(boolean visible) {
        this.mVivoTitleImpl.setTitleCenterSubViewVisible(visible);
    }

    public void setOnTitleClickListener(OnClickListener listener) {
        this.mVivoTitleImpl.setOnTitleClickListener(listener);
    }

    public void setOnTitleClickListener(View view) {
        this.mVivoTitleImpl.setOnTitleClickListener(view);
    }

    public void setOnTitleClickListener() {
        this.mVivoTitleImpl.setOnTitleClickListener();
    }
}
