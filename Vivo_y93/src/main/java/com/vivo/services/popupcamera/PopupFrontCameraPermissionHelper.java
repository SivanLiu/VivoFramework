package com.vivo.services.popupcamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PopupFrontCameraPermissionHelper {
    private static final String POPUP_CAMERA_CONTROL_PROJECTION_ALWAYS_DENY = "alwaysdeny";
    private static final String POPUP_CAMERA_CONTROL_PROJECTION_CURRENT_STATE = "currentstate";
    private static final String POPUP_CAMERA_CONTROL_PROJECTION_PKGNAME = "pkgname";
    private static final String POPUP_CAMERA_CONTROL_URI = "content://com.vivo.permissionmanager.provider.permission/pop_camera_control";
    private static final String TAG = "PopupCameraManagerService";

    public static class PopupFrontCameraPermissionState {
        public int alwaysDeny = -1;
        public int currentState = -1;
        public boolean isValid = false;
        public String packageName;

        public PopupFrontCameraPermissionState(String name, int state, int always, boolean valid) {
            this.packageName = name;
            this.currentState = state;
            this.alwaysDeny = always;
            this.isValid = valid;
        }

        public boolean isPopupFrontCameraPermissionGranted() {
            return this.currentState == 0;
        }

        public boolean isAlwaysDeny() {
            return this.alwaysDeny == 1;
        }

        public boolean isPermissionStateValid() {
            return this.isValid;
        }

        public String toString() {
            return "{" + this.packageName + " isPopupFrontCameraPermissionGranted=" + isPopupFrontCameraPermissionGranted() + " isAlwaysDeny=" + isAlwaysDeny() + "isValid=" + this.isValid + "}";
        }
    }

    public static PopupFrontCameraPermissionState getFrontCameraPermissionStateFromSettings(Context context, String packageName) {
        Cursor cursor = null;
        int currentState = 0;
        int alwaysDeny = 0;
        boolean foundSettings = false;
        try {
            cursor = context.getContentResolver().query(Uri.parse(POPUP_CAMERA_CONTROL_URI), new String[]{POPUP_CAMERA_CONTROL_PROJECTION_PKGNAME, POPUP_CAMERA_CONTROL_PROJECTION_CURRENT_STATE, POPUP_CAMERA_CONTROL_PROJECTION_ALWAYS_DENY}, "pkgname=?", new String[]{packageName}, null);
            if (cursor != null && cursor.getCount() >= 1) {
                cursor.moveToNext();
                currentState = cursor.getInt(cursor.getColumnIndex(POPUP_CAMERA_CONTROL_PROJECTION_CURRENT_STATE));
                alwaysDeny = cursor.getInt(cursor.getColumnIndex(POPUP_CAMERA_CONTROL_PROJECTION_ALWAYS_DENY));
                foundSettings = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "getFrontCameraPermissionStateFromSettings get error");
            foundSettings = false;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new PopupFrontCameraPermissionState(packageName, currentState, alwaysDeny, foundSettings);
    }

    public static void setFrontCameraPermissionStateToSettings(Context context, PopupFrontCameraPermissionState ps) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse(POPUP_CAMERA_CONTROL_URI);
        ContentValues cv = new ContentValues();
        cv.put(POPUP_CAMERA_CONTROL_PROJECTION_CURRENT_STATE, Integer.valueOf(ps.currentState));
        cv.put(POPUP_CAMERA_CONTROL_PROJECTION_ALWAYS_DENY, Integer.valueOf(ps.alwaysDeny));
        cv.put(POPUP_CAMERA_CONTROL_PROJECTION_PKGNAME, ps.packageName);
        if (resolver != null) {
            try {
                resolver.update(uri, cv, "pkgname=?", new String[]{ps.packageName});
            } catch (Exception e) {
                Log.d(TAG, "setFrontCameraPermissionStateToSettings" + ps + " failed");
            }
        }
    }
}
