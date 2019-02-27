package android.net.lowpan;

import android.net.lowpan.LowpanScanner.AnonymousClass2;
import android.net.lowpan.LowpanScanner.Callback;
import java.util.function.ToIntFunction;

final /* synthetic */ class -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII implements ToIntFunction {
    public static final /* synthetic */ -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII $INST$0 = new -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII();

    /* renamed from: android.net.lowpan.-$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f94-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f95-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f96-$f2;

        private final /* synthetic */ void $m$0() {
            AnonymousClass2.m49lambda$-android_net_lowpan_LowpanScanner$2_8042((Callback) this.f96-$f2, this.f94-$f0, this.f95-$f1);
        }

        public /* synthetic */ AnonymousClass1(int i, int i2, Object obj) {
            this.f94-$f0 = i;
            this.f95-$f1 = i2;
            this.f96-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ int $m$0(Object arg0) {
        return ((Integer) arg0).intValue();
    }

    private /* synthetic */ -$Lambda$ahIH8UUgV8jOvhfOz4liCd3-gII() {
    }

    public final int applyAsInt(Object obj) {
        return $m$0(obj);
    }
}
