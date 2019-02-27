package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.vivo.common.provider.Weather.WeatherAlert;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SavePowerActivity extends Activity {
    /* JADX WARNING: Missing block: B:61:0x0192, code:
            if ((r8 ^ 1) == 0) goto L_0x0194;
     */
    /* JADX WARNING: Missing block: B:72:0x01c1, code:
            if (r7.equals("mailto") == false) goto L_0x01c3;
     */
    /* JADX WARNING: Missing block: B:79:0x01de, code:
            if (r7.equals("geo") == false) goto L_0x01e0;
     */
    /* JADX WARNING: Missing block: B:84:0x01f2, code:
            if (r4.equals("vnd.android.cursor.item/postal-address_v2") == false) goto L_0x00a4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        int i = 0;
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            boolean allowed = true;
            String packName = getPackageName();
            String action = intent.getAction();
            String schema = intent.getScheme();
            ComponentName cn = intent.getComponent();
            String mimeType = intent.resolveType(this);
            Log.d("SPSApplication", "my package name: " + packName + " \nrequestCode: " + requestCode + " \nintent: " + intent + " \nschema: " + schema + " \ncn: " + cn + " \nmimeType: " + mimeType);
            if (packName.equals("com.android.mms")) {
                if (cn != null) {
                    String className = cn.getClassName();
                    if (className.equals("com.android.mms.ui.SlideshowEditActivity") || className.equals("com.android.gallery3d.app.Gallery") || className.equals("com.android.camera.CameraActivity") || className.equals("com.android.bbksoundrecorder.SoundRecorder")) {
                        allowed = false;
                    }
                } else if (action.equals("android.intent.action.CHOOSER") || action.equals("com.bbk.cloud.ACTION_ACCOUNT_SETTINGS") || action.equals("android.media.action.IMAGE_CAPTURE") || action.equals("android.media.action.VIDEO_CAPTURE") || action.equals("android.intent.action.PICK") || action.equals("com.android.settings.SOUNDPICKER") || action.equals("android.intent.action.GET_CONTENT") || (action.equals("android.intent.action.VIEW") && schema != null && (schema.equals("http") || ((schema.equals(WeatherAlert.CONTENT) && (mimeType == null || (mimeType.equals("vnd.android.cursor.item/contact") ^ 1) != 0)) || schema.equals("mailto"))))) {
                    allowed = false;
                }
            } else if (packName.equals("com.android.contacts")) {
                if (cn == null) {
                    if (!(action.equals("com.android.settings.SOUNDPICKER") || action.equals("android.intent.action.PICK") || action.equals("android.intent.action.CHOOSER"))) {
                        if (action.equals("android.intent.action.GET_CONTENT")) {
                            if (mimeType != null) {
                                i = mimeType.equals("vnd.android.cursor.item/phone_v2");
                            }
                        }
                        if (!action.equals("android.settings.SYNC_SETTINGS")) {
                            if (!action.equals("com.bbk.cloud.ACTION_ACCOUNT_SETTINGS")) {
                                if (!action.equals("android.media.action.IMAGE_CAPTURE")) {
                                    if (action.equals("android.intent.action.SENDTO")) {
                                        if (schema != null) {
                                        }
                                    }
                                    if (action.equals("android.intent.action.VIEW")) {
                                        if (schema != null) {
                                            if (!schema.equals("http")) {
                                            }
                                        }
                                    }
                                    if (action.equals("android.intent.action.VIEW")) {
                                        if (mimeType != null) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    allowed = false;
                } else if (cn.getClassName().equals("com.android.bbksoundrecorder.ReclistActivity")) {
                    allowed = false;
                }
            } else if (packName.equals("com.android.BBKClock")) {
                if (cn == null && action.equals("com.android.settings.SOUNDPICKER")) {
                    allowed = false;
                }
            } else if (packName.equals("com.android.settings") && cn != null && cn.getClassName().equals("com.vivo.settings.SoundPicker")) {
                allowed = false;
            }
            if (!allowed) {
                Log.d("SPSApplication", "in sps mode, start external app in package " + packName + " is not allowed.");
                Toast.makeText(this, 51249357, 1).show();
                return;
            } else if (cn == null) {
                if (action.equals("android.intent.action.SENDTO") && schema != null && schema.equals("smsto")) {
                    intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
                } else if (action.equals("android.intent.action.VIEW") && schema != null && schema.equals("tel")) {
                    intent.setClassName("com.android.dialer", "com.android.dialer.BBKTwelveKeyDialer");
                }
            }
        }
        super.startActivityForResult(intent, requestCode, options);
    }

    public void startActivity(Intent intent) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            String packName = getPackageName();
            String action = intent.getAction();
            String schema = intent.getScheme();
            if (intent.getComponent() == null) {
                if (action.equals("android.intent.action.SENDTO") && schema != null && schema.equals("smsto")) {
                    intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
                } else if (action.equals("android.intent.action.DIAL") && schema != null && schema.equals("tel")) {
                    intent.setClassName("com.android.dialer", "com.android.dialer.BBKTwelveKeyDialer");
                }
            }
        }
        super.startActivity(intent);
    }

    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        View mDecor = getWindow().getDecorView();
        if (outBitmap == null || (outBitmap.isRecycled() ^ 1) == 0 || canvas == null || mDecor == null) {
            return false;
        }
        canvas.setBitmap(outBitmap);
        mDecor.draw(canvas);
        canvas.setBitmap(null);
        return true;
    }
}
