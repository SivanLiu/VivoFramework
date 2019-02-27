package com.android.internal.view.menu;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import com.android.internal.view.menu.MenuPresenter.Callback;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public class VivoIconMenuDialogHelper implements OnKeyListener, OnDismissListener, Callback {
    private final boolean DEBUG = false;
    private final String TAG = "VivoIconMenuDialogHelper";
    private Dialog mDialog = null;
    private MenuBuilder mMenu = null;
    private MenuView mMenuView = null;
    private VivoIconMenuPresenter mPresenter = null;
    private Callback mPresenterCallback = null;

    public VivoIconMenuDialogHelper(MenuBuilder menu) {
        this.mMenu = menu;
    }

    public void show(IBinder windowToken) {
        MenuBuilder menu = this.mMenu;
        this.mPresenter = new VivoIconMenuPresenter(this.mMenu.getContext());
        this.mPresenter.setCallback(this);
        this.mMenu.addMenuPresenter(this.mPresenter);
        this.mDialog = new Dialog(menu.getContext(), VivoThemeUtil.getSystemThemeStyle(ThemeType.CONTEXT_MENU_DIALOG));
        this.mMenuView = this.mPresenter.getMenuView(null);
        this.mDialog.setContentView((View) this.mMenuView, new LayoutParams(-1, -2));
        this.mDialog.setOnKeyListener(this);
        this.mDialog.setCancelable(true);
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setOnDismissListener(this);
        this.mDialog.getWindow().setWindowAnimations(this.mMenuView.getWindowAnimations());
        WindowManager.LayoutParams lp = this.mDialog.getWindow().getAttributes();
        lp.gravity = 80;
        lp.width = -1;
        lp.height = -2;
        lp.type = 1003;
        lp.flags |= 131072;
        if (windowToken != null) {
            lp.token = windowToken;
        }
        this.mDialog.getWindow().setAttributes(lp);
        this.mDialog.show();
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == 82 || keyCode == 4) {
            Window win;
            View decor;
            DispatcherState ds;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                win = this.mDialog.getWindow();
                if (win != null) {
                    decor = win.getDecorView();
                    if (decor != null) {
                        ds = decor.getKeyDispatcherState();
                        if (ds != null) {
                            ds.startTracking(event, this);
                            return true;
                        }
                    }
                }
            } else if (event.getAction() == 1 && (event.isCanceled() ^ 1) != 0) {
                win = this.mDialog.getWindow();
                if (win != null) {
                    decor = win.getDecorView();
                    if (decor != null) {
                        ds = decor.getKeyDispatcherState();
                        if (ds != null && ds.isTracking(event)) {
                            this.mMenu.close(true);
                            dialog.dismiss();
                            return true;
                        }
                    }
                }
            }
        }
        return this.mMenu.performShortcut(keyCode, event, 0);
    }

    public void setPresenterCallback(Callback cb) {
        this.mPresenterCallback = cb;
    }

    public void dismiss() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        this.mPresenter.onCloseMenu(this.mMenu, true);
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (allMenusAreClosing || menu == this.mMenu) {
            dismiss();
        }
        if (this.mPresenterCallback != null) {
            this.mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    public boolean onOpenSubMenu(MenuBuilder subMenu) {
        if (this.mPresenterCallback != null) {
            return this.mPresenterCallback.onOpenSubMenu(subMenu);
        }
        return false;
    }
}
