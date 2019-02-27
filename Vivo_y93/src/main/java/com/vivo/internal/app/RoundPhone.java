package com.vivo.internal.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.FtDeviceInfo;
import android.util.FtFeature;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IRotationWatcher.Stub;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class RoundPhone {
    private static final String TAG = "RoundPhone";
    private static final int UPDATE_UPEARVIEW_AND_DOWNROUNDVIEW = 1000;
    private Context mContext;
    private DownRoundView mDownRoundView;
    private final IRotationWatcher mRotationWatcher = new Stub() {
        public void onRotationChanged(int rotation) {
            Log.d(RoundPhone.TAG, "onRotationChanged() rotation = " + rotation);
            Message msg = RoundPhone.this.roundPhoneHandler.obtainMessage(1000);
            msg.arg1 = rotation;
            RoundPhone.this.roundPhoneHandler.sendMessage(msg);
        }
    };
    private boolean mShow = false;
    private UpEarView mUpEarView;
    private IWindowManager mWindowManager;
    private Handler roundPhoneHandler = new Handler() {
        public void handleMessage(Message msg) {
            RoundPhone.this.updateUpEarAndDownRoundOrientation(msg.arg1);
        }
    };

    private class DownRoundView extends View {
        private int h;
        private Bitmap mCornerLeft;
        private Bitmap mCornerRight;
        private int mOrientation;
        private Paint mPaint = new Paint();
        private int mRadius;
        private int mRotate;
        private WindowManager mWindowmanager;
        private LayoutParams mlp;
        private int w;

        DownRoundView(Context mContext, Bitmap leftBmp, Bitmap rightBmp, LayoutParams lp, WindowManager wm) {
            super(mContext);
            this.mCornerLeft = leftBmp;
            this.mCornerRight = rightBmp;
            this.mlp = lp;
            this.mWindowmanager = wm;
            this.mRadius = Math.max(leftBmp.getWidth(), leftBmp.getHeight());
        }

        void downRoundViewConfigurationChanged(int rotation) {
            switch (rotation) {
                case 1:
                    this.mlp.width = this.mRadius;
                    this.mlp.height = -1;
                    this.mlp.gravity = 5;
                    break;
                case 3:
                    this.mlp.width = this.mRadius;
                    this.mlp.height = -1;
                    this.mlp.gravity = 3;
                    break;
                default:
                    this.mlp.height = this.mRadius;
                    this.mlp.width = -1;
                    this.mlp.gravity = 80;
                    break;
            }
            this.mRotate = rotation;
            this.mWindowmanager.updateViewLayout(this, this.mlp);
            invalidate();
        }

        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            switch (this.mRotate) {
                case 1:
                    canvas.save();
                    canvas.rotate(-90.0f);
                    canvas.drawBitmap(this.mCornerRight, (float) (-this.mRadius), 0.0f, this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) h);
                    canvas.rotate(-90.0f);
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, 0.0f, this.mPaint);
                    canvas.restore();
                    return;
                case 3:
                    canvas.save();
                    canvas.rotate(90.0f);
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, (float) (-this.mRadius), this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) (h - this.mRadius));
                    canvas.rotate(90.0f);
                    canvas.drawBitmap(this.mCornerRight, 0.0f, (float) (-this.mRadius), this.mPaint);
                    canvas.restore();
                    return;
                default:
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, 0.0f, this.mPaint);
                    canvas.save();
                    canvas.translate((float) (w - this.mRadius), 0.0f);
                    canvas.drawBitmap(this.mCornerRight, 0.0f, 0.0f, this.mPaint);
                    canvas.restore();
                    return;
            }
        }
    }

    private class RoundView extends View {
        private boolean isFirst = false;
        private Context mContext;
        private Bitmap mCornerBitmap;
        private int mOrientation;
        private Paint mPaint = new Paint();
        private int mRadius;
        private int mRotate;
        private WindowManager mWindowmanager;
        private LayoutParams mlp;
        private int offsetX;
        private int offsetY;

        public RoundView(Context c, boolean up, Bitmap corner, int orientation, LayoutParams lp, WindowManager wm, int radius) {
            super(c);
            this.mCornerBitmap = corner;
            this.isFirst = up;
            this.mOrientation = orientation;
            this.mlp = lp;
            this.mContext = c;
            this.mWindowmanager = wm;
            this.mRadius = radius;
        }

        protected void onConfigurationChanged(Configuration newConfig) {
            int newOrientation = newConfig.orientation;
            if (newOrientation != this.mOrientation) {
                if (newOrientation == 2) {
                    this.mlp.width = -1;
                    this.mlp.height = this.mRadius;
                } else {
                    this.mlp.width = this.mRadius;
                    this.mlp.height = -1;
                }
                if (this.isFirst) {
                    this.mlp.gravity = newOrientation == 2 ? 48 : 3;
                    this.mWindowmanager.updateViewLayout(this, this.mlp);
                } else {
                    this.mlp.gravity = newOrientation == 2 ? 80 : 5;
                    this.mWindowmanager.updateViewLayout(this, this.mlp);
                }
                this.mOrientation = newOrientation;
            }
        }

        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (this.isFirst) {
                if (this.mOrientation == 2) {
                    this.offsetX = w;
                    this.offsetY = 0;
                    this.mRotate = 90;
                } else {
                    this.offsetX = 0;
                    this.offsetY = h;
                    this.mRotate = 270;
                }
                canvas.drawBitmap(this.mCornerBitmap, 0.0f, 0.0f, this.mPaint);
                canvas.save();
                canvas.translate((float) this.offsetX, (float) this.offsetY);
                canvas.rotate((float) this.mRotate);
                canvas.drawBitmap(this.mCornerBitmap, 0.0f, 0.0f, this.mPaint);
                canvas.restore();
                return;
            }
            if (this.mOrientation == 2) {
                this.offsetX = 0;
                this.offsetY = h;
                this.mRotate = 270;
            } else {
                this.offsetX = w;
                this.offsetY = 0;
                this.mRotate = 90;
            }
            canvas.save();
            canvas.translate((float) w, (float) h);
            canvas.rotate(180.0f);
            canvas.drawBitmap(this.mCornerBitmap, 0.0f, 0.0f, this.mPaint);
            canvas.restore();
            canvas.save();
            canvas.translate((float) this.offsetX, (float) this.offsetY);
            canvas.rotate((float) this.mRotate);
            canvas.drawBitmap(this.mCornerBitmap, 0.0f, 0.0f, this.mPaint);
            canvas.restore();
        }
    }

    private class UpEarView extends View {
        private Context mContext;
        private Bitmap mCornerLeft;
        private Bitmap mCornerRight;
        private Bitmap mEarBitmap;
        private int mEarBitmapWidth;
        private Point mEarPosition;
        private int mEarbitmapHeight;
        private Paint mPaint = new Paint();
        private int mRadius;
        private int mRotate;
        private WindowManager mWindowmanager;
        private LayoutParams mlp;

        public UpEarView(Context c, Bitmap leftBmp, Bitmap rightBmp, Bitmap earBmp, Point earposition, LayoutParams lp, WindowManager wm) {
            super(c);
            this.mCornerLeft = leftBmp;
            this.mCornerRight = rightBmp;
            this.mEarBitmap = earBmp;
            this.mlp = lp;
            this.mContext = c;
            this.mWindowmanager = wm;
            this.mRadius = Math.max(leftBmp.getWidth(), leftBmp.getHeight());
            this.mEarBitmapWidth = earBmp.getWidth();
            this.mEarbitmapHeight = earBmp.getHeight();
            this.mRotate = 0;
            this.mEarPosition = earposition;
        }

        void upEarViewConfigurationChanged(int rotation) {
            switch (rotation) {
                case 1:
                    this.mlp.width = this.mRadius;
                    this.mlp.height = -1;
                    this.mlp.gravity = 3;
                    break;
                case 3:
                    this.mlp.width = this.mRadius;
                    this.mlp.height = -1;
                    this.mlp.gravity = 5;
                    break;
                default:
                    this.mlp.height = this.mRadius;
                    this.mlp.width = -1;
                    this.mlp.gravity = 48;
                    break;
            }
            this.mRotate = rotation;
            this.mWindowmanager.updateViewLayout(this, this.mlp);
            invalidate();
        }

        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            switch (this.mRotate) {
                case 1:
                    canvas.save();
                    canvas.rotate(-90.0f);
                    canvas.drawBitmap(this.mCornerRight, (float) (-this.mRadius), 0.0f, this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) h);
                    canvas.rotate(-90.0f);
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, 0.0f, this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) this.mEarPosition.x);
                    canvas.rotate(-90.0f);
                    canvas.drawBitmap(this.mEarBitmap, (float) (-this.mEarPosition.x), 0.0f, this.mPaint);
                    canvas.restore();
                    return;
                case 3:
                    canvas.save();
                    canvas.rotate(90.0f);
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, (float) (-this.mRadius), this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) h);
                    canvas.rotate(90.0f);
                    canvas.drawBitmap(this.mCornerRight, (float) (-this.mRadius), (float) (-this.mRadius), this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(0.0f, (float) this.mEarPosition.x);
                    canvas.rotate(90.0f);
                    canvas.drawBitmap(this.mEarBitmap, 0.0f, (float) (-this.mRadius), this.mPaint);
                    canvas.restore();
                    return;
                default:
                    canvas.drawBitmap(this.mCornerLeft, 0.0f, 0.0f, this.mPaint);
                    canvas.save();
                    canvas.translate((float) w, 0.0f);
                    canvas.drawBitmap(this.mCornerRight, (float) (-this.mRadius), 0.0f, this.mPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate((float) this.mEarPosition.x, 0.0f);
                    canvas.drawBitmap(this.mEarBitmap, 0.0f, 0.0f, this.mPaint);
                    canvas.restore();
                    return;
            }
        }
    }

    public RoundPhone(IWindowManager windowManager, Context context) {
        this.mWindowManager = windowManager;
        this.mContext = context;
    }

    public boolean showRound() {
        if (this.mShow) {
            return false;
        }
        this.mShow = true;
        LayoutParams lp = new LayoutParams(-1, -1);
        lp.type = 2021;
        lp.flags = 1304;
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
            lp.privateFlags |= 2;
        }
        lp.format = -3;
        lp.setTitle("__@@RoundLayer__@@__");
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        lp.inputFeatures |= 2;
        lp.x = 0;
        lp.y = 0;
        if (FtFeature.isFeatureSupport(32)) {
            showUpEarViewAndDownRoundView(wm, lp);
        } else {
            showRoundPhoneView(wm, lp);
        }
        return true;
    }

    private void showRoundPhoneView(WindowManager wm, LayoutParams lp) {
        int radius = (int) this.mContext.getResources().getDimension(51118333);
        Bitmap corner = getRoundCorner(radius, radius, radius);
        lp.width = radius;
        int orientation = this.mContext.getResources().getConfiguration().orientation;
        lp.gravity = 3;
        wm.addView(new RoundView(this.mContext, true, corner, orientation, lp, wm, radius), lp);
        lp.gravity = 5;
        wm.addView(new RoundView(this.mContext, false, corner, orientation, lp, wm, radius), lp);
    }

    private void showUpEarViewAndDownRoundView(WindowManager wm, LayoutParams lp) {
        Resources mRes = this.mContext.getResources();
        Bitmap cornerUpLeftBtm = BitmapFactory.decodeResource(mRes, 50463579);
        Bitmap cornerUpRightBtm = BitmapFactory.decodeResource(mRes, 50463580);
        Bitmap earUpBitmap = BitmapFactory.decodeResource(mRes, 50463578);
        int upCornorRaidus = cornerUpLeftBtm.getHeight();
        Point mEarPosition = FtDeviceInfo.getPortraitEarPosition(this.mContext);
        lp.height = upCornorRaidus;
        lp.gravity = 48;
        this.mUpEarView = new UpEarView(this.mContext, cornerUpLeftBtm, cornerUpRightBtm, earUpBitmap, mEarPosition, lp, wm);
        wm.addView(this.mUpEarView, lp);
        Bitmap cornerDownLeftBtm = BitmapFactory.decodeResource(mRes, 50463576);
        Bitmap cornerDownRightBtm = BitmapFactory.decodeResource(mRes, 50463577);
        lp.height = cornerDownLeftBtm.getHeight();
        lp.gravity = 80;
        this.mDownRoundView = new DownRoundView(this.mContext, cornerDownLeftBtm, cornerDownRightBtm, lp, wm);
        wm.addView(this.mDownRoundView, lp);
        try {
            this.mWindowManager.watchRotation(this.mRotationWatcher, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception when removing rotation watcher");
        }
    }

    private void updateUpEarAndDownRoundOrientation(int rotate) {
        if (this.mUpEarView != null) {
            this.mUpEarView.upEarViewConfigurationChanged(rotate);
        }
        if (this.mDownRoundView != null) {
            this.mDownRoundView.downRoundViewConfigurationChanged(rotate);
        }
    }

    private Bitmap getRoundCorner(int radius, int x, int y) {
        Canvas canvas = new Canvas();
        Bitmap map = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
        canvas.setBitmap(map);
        canvas.drawARGB(255, 0, 0, 0);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        canvas.drawCircle((float) x, (float) y, (float) radius, paint);
        return map;
    }
}
