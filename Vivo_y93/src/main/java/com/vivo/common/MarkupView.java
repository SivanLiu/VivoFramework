package com.vivo.common;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import java.util.Locale;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class MarkupView extends LinearView {
    private boolean isCheckLayout;
    private boolean isDeleteLayout;
    private boolean isMarkedLayout;
    private String strCancel;
    private String strDelete;
    private String strOk;
    private String strReverse;
    private String strSelectAll;

    public MarkupView(Context context) {
        this(context, null);
    }

    public MarkupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.strOk = "Ok";
        this.strDelete = "Delete";
        this.strSelectAll = "SelectAll";
        this.strReverse = "Reverse";
        this.strCancel = "Cancel";
        this.isCheckLayout = false;
        this.isMarkedLayout = false;
        this.isDeleteLayout = false;
        Locale locale = getResources().getConfiguration().locale;
        if (locale != null && locale.getLanguage().equals("zh") && locale.getCountry().equals("CN")) {
            this.strOk = new String(new byte[]{(byte) -25, (byte) -95, (byte) -82, (byte) -27, (byte) -82, (byte) -102});
            this.strDelete = new String(new byte[]{(byte) -27, (byte) -120, (byte) -96, (byte) -23, (byte) -103, (byte) -92});
            this.strSelectAll = new String(new byte[]{(byte) -27, (byte) -123, (byte) -88, (byte) -23, Byte.MIN_VALUE, (byte) -119});
            this.strReverse = new String(new byte[]{(byte) -27, (byte) -113, (byte) -115, (byte) -23, Byte.MIN_VALUE, (byte) -119});
            this.strCancel = new String(new byte[]{(byte) -27, (byte) -113, (byte) -106, (byte) -26, (byte) -74, (byte) -120});
        }
    }

    public void initDeleteLayout() {
        if (!this.isDeleteLayout) {
            removeAllItems();
            addText(this.strDelete);
            this.isDeleteLayout = true;
        }
    }

    public void initCheckLayout() {
        if (!this.isCheckLayout) {
            removeAllItems();
            addText(this.strOk);
            addText(this.strCancel);
            this.isCheckLayout = true;
        }
    }

    public void initMarkedThreeLayout() {
        if (!this.isMarkedLayout) {
            removeAllItems();
            addText(this.strDelete);
            addText(this.strSelectAll);
            addText(this.strCancel);
            this.isMarkedLayout = true;
        }
    }

    public void initMarkedLayout() {
        if (!this.isMarkedLayout) {
            removeAllItems();
            addText(this.strDelete);
            addText(this.strSelectAll);
            addText(this.strReverse);
            addText(this.strCancel);
            this.isMarkedLayout = true;
        }
    }

    public void initMarkedFourLayout() {
        if (!this.isMarkedLayout) {
            removeAllItems();
            addText(this.strDelete);
            addText(this.strSelectAll);
            addText(this.strSelectAll);
            addText(this.strCancel);
            this.isMarkedLayout = true;
        }
    }

    public Button getLeftButton() {
        if (getCurrentItemCount() >= 1) {
            return getCurrentItem(0);
        }
        return null;
    }

    public Button getRightButton() {
        if (getCurrentItemCount() >= 2) {
            return getCurrentItem(-1);
        }
        return null;
    }

    public Button getCenterOneButton() {
        if (getCurrentItemCount() >= 3) {
            return getCurrentItem(1);
        }
        return null;
    }

    public Button getCenterTwoButton() {
        if (getCurrentItemCount() >= 4) {
            return getCurrentItem(2);
        }
        return null;
    }

    public void setLeftButtonText(String leftText) {
        if (leftText != null && getLeftButton() != null) {
            getLeftButton().setText(leftText);
        }
    }

    public void setRightButtonText(String rightText) {
        if (rightText != null && getRightButton() != null) {
            getRightButton().setText(rightText);
        }
    }

    public void recycleLayoutValues() {
        this.isMarkedLayout = false;
        this.isDeleteLayout = false;
        this.isCheckLayout = false;
    }

    private void removeAllItems() {
        while (getCurrentItemCount() > 0) {
            removeItem(0);
        }
    }
}
