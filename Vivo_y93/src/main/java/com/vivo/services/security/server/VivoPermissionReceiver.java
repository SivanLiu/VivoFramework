package com.vivo.services.security.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VivoPermissionReceiver extends BroadcastReceiver {
    private VivoPermissionService mVPS = null;

    public VivoPermissionReceiver(VivoPermissionService vps) {
        this.mVPS = vps;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString();
        if (packageName != null) {
            packageName = packageName.replace("package:", "");
            VivoPermissionService.printfInfo("onReceive action=" + action);
            boolean grantPermissions = intent.getBooleanExtra("grantPermissions", false);
            Intent vIntent = new Intent();
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                this.mVPS.updateForPackageAdded(packageName, grantPermissions);
                vIntent.setAction("com.vivo.services.security.client.PACKAGE_PERMISSION_ADDED");
                if (intent.hasExtra("install_resource")) {
                    String install_resource = intent.getStringExtra("install_resource");
                    boolean IsInstallSilence = intent.getBooleanExtra("IsInstallSilence", false);
                    vIntent.putExtra("install_resource", install_resource);
                    vIntent.putExtra("IsInstallSilence", IsInstallSilence);
                }
                sendBroadcast(context, vIntent, packageName);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                this.mVPS.updateForPackageRemoved(packageName);
                vIntent.setAction("com.vivo.services.security.client.PACKAGE_PERMISSION_REMOVED");
                sendBroadcast(context, vIntent, packageName);
            } else if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
                this.mVPS.updateForPackageReplaced(packageName);
                vIntent.setAction("com.vivo.services.security.client.PACKAGE_PERMISSION_REPLACED");
                sendBroadcast(context, vIntent, packageName);
            }
        }
    }

    public void sendBroadcast(Context context, Intent intent, String packageName) {
        intent.putExtra("package", packageName);
        intent.setPackage("com.vivo.permissionmanager");
        context.sendBroadcast(intent);
        VivoPermissionService.printfInfo("sendBroadcast-->intent=" + intent + "; packageName=" + packageName);
    }
}
