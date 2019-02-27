package com.vivo.common.widget.fingerprint;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.fingerprint.FingerprintManager;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.View;
import com.vivo.framework.facedetect.FaceDetectManager;
import java.lang.reflect.Method;

public class FingerprintView extends View {
    private static final int CMD_BASE = 32512;
    private static final int CMD_VIEW_FOCUS = 32575;
    private static final int CMD_VIEW_LOSE_FOCUS = 32576;
    private static final String SYSTEM_UI_PACKAGENAME = "com.android.systemui";
    private static final String TAG = "FingerprintView";
    private Bitmap mFingerBitmap;
    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;
    private String mPackageName;

    public FingerprintView(Context context) {
        super(context);
        initFingerprintView(context);
    }

    public FingerprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFingerprintView(context);
    }

    public FingerprintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFingerprintView(context);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Slog.i(TAG, "hasWindowFocus is:" + hasWindowFocus + ",view visibility is: " + getVisibility() + ",name:" + this.mPackageName);
        if (!SYSTEM_UI_PACKAGENAME.equals(this.mPackageName) || isKeyguardShowing()) {
            sendCommandMessage(hasWindowFocus);
        } else {
            Slog.i(TAG, "not in keyguard view");
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Slog.i(TAG, "visibility is:" + visibility);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mFingerBitmap != null) {
            canvas.drawBitmap(this.mFingerBitmap, 0.0f, 0.0f, null);
        }
    }

    private void initFingerprintView(Context context) {
        try {
            this.mPackageName = context.getPackageName();
            this.mKeyguardManager = (KeyguardManager) context.getSystemService(FaceDetectManager.CMD_FACE_DETECT_KEYGUARD);
            if (this.mFingerBitmap == null) {
                this.mFingerBitmap = BitmapFactory.decodeResource(context.getResources(), 50462732);
                invalidate();
            }
        } catch (Exception e) {
            Slog.e(TAG, "get default image fail", e);
        }
    }

    private boolean isKeyguardShowing() {
        if (this.mKeyguardManager == null) {
            return false;
        }
        return this.mKeyguardManager.isKeyguardLocked();
    }

    private void sendCommandMessage(boolean isWindowFocusChange) {
        if (this.mFingerprintManager == null) {
            this.mFingerprintManager = (FingerprintManager) getContext().getSystemService(FingerprintManager.class);
        }
        try {
            Method sendCommand = FingerprintManager.class.getDeclaredMethod("sendCommand", new Class[]{Integer.TYPE, Integer.TYPE});
            if (isWindowFocusChange) {
                sendCommand.invoke(this.mFingerprintManager, new Object[]{Integer.valueOf(CMD_VIEW_FOCUS), Integer.valueOf(0)});
                return;
            }
            sendCommand.invoke(this.mFingerprintManager, new Object[]{Integer.valueOf(CMD_VIEW_LOSE_FOCUS), Integer.valueOf(0)});
        } catch (Exception e) {
            Slog.i(TAG, "sendCommand fail", e);
        }
    }

    public static Rect getFingerprintIconPosition(Context context) {
        Rect mRect = new Rect(445, 1918, 635, 2108);
        try {
            mRect.left = (int) context.getResources().getDimension(51118373);
            mRect.top = (int) context.getResources().getDimension(51118374);
            mRect.right = (int) (((float) mRect.left) + context.getResources().getDimension(51118375));
            mRect.bottom = (int) (((float) mRect.top) + context.getResources().getDimension(51118376));
        } catch (Exception e) {
            Slog.e(TAG, "get demin fail", e);
        }
        return mRect;
    }
}
