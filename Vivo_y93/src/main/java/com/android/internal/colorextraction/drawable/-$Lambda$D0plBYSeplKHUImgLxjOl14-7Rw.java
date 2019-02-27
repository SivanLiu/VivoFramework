package com.android.internal.colorextraction.drawable;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

final /* synthetic */ class -$Lambda$D0plBYSeplKHUImgLxjOl14-7Rw implements AnimatorUpdateListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f127-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ int f128-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ int f129-$f2;
    /* renamed from: -$f3 */
    private final /* synthetic */ int f130-$f3;
    /* renamed from: -$f4 */
    private final /* synthetic */ Object f131-$f4;

    private final /* synthetic */ void $m$0(ValueAnimator arg0) {
        ((GradientDrawable) this.f131-$f4).m51xa8bb0260(this.f127-$f0, this.f128-$f1, this.f129-$f2, this.f130-$f3, arg0);
    }

    public /* synthetic */ -$Lambda$D0plBYSeplKHUImgLxjOl14-7Rw(int i, int i2, int i3, int i4, Object obj) {
        this.f127-$f0 = i;
        this.f128-$f1 = i2;
        this.f129-$f2 = i3;
        this.f130-$f3 = i4;
        this.f131-$f4 = obj;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        $m$0(valueAnimator);
    }
}
