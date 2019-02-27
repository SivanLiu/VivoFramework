package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public interface IVivoTitle {
    TextView getTitleCenterView();

    Button getTitleLeftButton();

    Button getTitleRightButton();

    void hideTitleLeftButton();

    void hideTitleRightButton();

    void initTitleLeftButton(CharSequence charSequence, int i, OnClickListener onClickListener);

    void initTitleRightButton(CharSequence charSequence, int i, OnClickListener onClickListener);

    void setOnTitleClickListener();

    void setOnTitleClickListener(OnClickListener onClickListener);

    void setOnTitleClickListener(View view);

    void setTitleCenterSubText(CharSequence charSequence);

    void setTitleCenterSubViewVisible(boolean z);

    void setTitleCenterText(CharSequence charSequence);

    void setTitleLeftButtonClickListener(OnClickListener onClickListener);

    void setTitleLeftButtonEnable(boolean z);

    void setTitleLeftButtonIcon(int i);

    void setTitleLeftButtonText(CharSequence charSequence);

    void setTitleRightButtonClickListener(OnClickListener onClickListener);

    void setTitleRightButtonEnable(boolean z);

    void setTitleRightButtonIcon(int i);

    void setTitleRightButtonText(CharSequence charSequence);

    void showTitleLeftButton();

    void showTitleRightButton();
}
