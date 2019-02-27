package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.TabActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoTabActivity extends TabActivity implements IVivoTitle {
    private VivoTitleImpl mVivoTitleImpl = new VivoTitleImpl(this);

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initViews();
    }

    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        initViews();
    }

    private void initViews() {
        this.mVivoTitleImpl.init(null);
        CharSequence title = super.getTitle();
        if (title != null) {
            this.mVivoTitleImpl.setTitleCenterText(title);
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
