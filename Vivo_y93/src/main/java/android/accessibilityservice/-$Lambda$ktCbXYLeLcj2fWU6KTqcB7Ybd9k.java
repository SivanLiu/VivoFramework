package android.accessibilityservice;

import android.accessibilityservice.FingerprintGestureController.FingerprintGestureCallback;

final /* synthetic */ class -$Lambda$ktCbXYLeLcj2fWU6KTqcB7Ybd9k implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ int f5-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f6-$f1;

    /* renamed from: android.accessibilityservice.-$Lambda$ktCbXYLeLcj2fWU6KTqcB7Ybd9k$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f7-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f8-$f1;

        private final /* synthetic */ void $m$0() {
            ((FingerprintGestureCallback) this.f8-$f1).onGestureDetectionAvailabilityChanged(this.f7-$f0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, Object obj) {
            this.f7-$f0 = z;
            this.f8-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((FingerprintGestureCallback) this.f6-$f1).onGestureDetected(this.f5-$f0);
    }

    public /* synthetic */ -$Lambda$ktCbXYLeLcj2fWU6KTqcB7Ybd9k(int i, Object obj) {
        this.f5-$f0 = i;
        this.f6-$f1 = obj;
    }

    public final void run() {
        $m$0();
    }
}
