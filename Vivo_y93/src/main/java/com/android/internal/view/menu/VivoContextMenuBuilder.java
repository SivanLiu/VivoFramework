package com.android.internal.view.menu;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.FtBuild;
import android.os.IBinder;
import android.view.View;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoContextMenuBuilder extends ContextMenuBuilder {
    public VivoContextMenuBuilder(Context context) {
        super(context);
    }

    public MenuDialogHelper showDialog(View originalView, IBinder token) {
        if (originalView != null) {
            originalView.createContextMenu(this);
        }
        if (getVisibleItems().size() <= 0) {
            return null;
        }
        if (FtBuild.getRomVersion() >= 3.0f) {
            EVivoContextMenuDialogHelper helper = new EVivoContextMenuDialogHelper(this);
            helper.show();
            return helper;
        }
        VivoContextMenuDialogHelper helper2 = new VivoContextMenuDialogHelper(this);
        helper2.show();
        return helper2;
    }
}
