package com.android.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.policy.DecorView;

public class BackgroundFallback {
    private Drawable mBackgroundFallback;
    ColorDrawable mBackgroundFallbackVivo = null;
    boolean mVivoBackgroundFallbackSupport = false;

    public void initVivoBgFallback(Context context) {
        int vivoBackgroundFallbackColor = 0;
        try {
            if (context instanceof Activity) {
                String fullActivityName = ((Activity) context).getIntent().getComponent().flattenToShortString();
                if (fullActivityName.contains("com.tencent.qqlive") || fullActivityName.contains("com.youku.phone") || (fullActivityName.contains("com.qiyi.video") && (SystemProperties.getBoolean("persist.vivo.multiwindow_fallbackbg_enable_all", false) ^ 1) != 0)) {
                    this.mVivoBackgroundFallbackSupport = true;
                    vivoBackgroundFallbackColor = 0;
                } else {
                    this.mVivoBackgroundFallbackSupport = SystemProperties.getBoolean("persist.vivo.multiwindow_fallbackbg_enable_force", false);
                    vivoBackgroundFallbackColor = SystemProperties.getInt("persist.vivo.multiwindow_fallbackbg_color_force", -1);
                }
            }
        } catch (Exception e) {
            Log.e("BackgroundFallback", "initVivoBgFallback", e);
            this.mVivoBackgroundFallbackSupport = false;
        }
        if (this.mBackgroundFallbackVivo == null && this.mVivoBackgroundFallbackSupport && -1 != vivoBackgroundFallbackColor) {
            this.mBackgroundFallbackVivo = new ColorDrawable(Color.argb(255, Color.red(vivoBackgroundFallbackColor), Color.green(vivoBackgroundFallbackColor), Color.blue(vivoBackgroundFallbackColor)));
        }
    }

    public void setDrawable(Drawable d) {
        this.mBackgroundFallback = d;
    }

    public boolean hasFallback() {
        if (this.mBackgroundFallback == null) {
            return this.mVivoBackgroundFallbackSupport && this.mBackgroundFallbackVivo != null;
        } else {
            return true;
        }
    }

    public void draw(ViewGroup boundsView, ViewGroup root, Canvas c, View content) {
        if (hasFallback()) {
            int width = boundsView.getWidth();
            int height = boundsView.getHeight();
            int left = width;
            int top = height;
            int right = 0;
            int bottom = 0;
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = root.getChildAt(i);
                Drawable childBg = child.getBackground();
                if (child == content) {
                    if (childBg == null && (child instanceof ViewGroup) && ((ViewGroup) child).getChildCount() == 0) {
                    }
                    left = Math.min(left, child.getLeft());
                    top = Math.min(top, child.getTop());
                    right = Math.max(right, child.getRight());
                    bottom = Math.max(bottom, child.getBottom());
                } else if (child.getVisibility() == 0) {
                    if (childBg != null) {
                        if (childBg.getOpacity() != -1) {
                        }
                        left = Math.min(left, child.getLeft());
                        top = Math.min(top, child.getTop());
                        right = Math.max(right, child.getRight());
                        bottom = Math.max(bottom, child.getBottom());
                    }
                }
            }
            if (left < right && top < bottom) {
                Drawable backgroundFallback = this.mBackgroundFallback;
                if ((boundsView instanceof DecorView) && this.mVivoBackgroundFallbackSupport && this.mBackgroundFallbackVivo != null) {
                    backgroundFallback = this.mBackgroundFallbackVivo;
                }
                if (backgroundFallback != null) {
                    if (top > 0) {
                        backgroundFallback.setBounds(0, 0, width, top);
                        backgroundFallback.draw(c);
                    }
                    if (left > 0) {
                        backgroundFallback.setBounds(0, top, left, height);
                        backgroundFallback.draw(c);
                    }
                    if (right < width) {
                        backgroundFallback.setBounds(right, top, width, height);
                        backgroundFallback.draw(c);
                    }
                    if (bottom < height) {
                        backgroundFallback.setBounds(left, bottom, right, height);
                        backgroundFallback.draw(c);
                    }
                }
            }
        }
    }
}
