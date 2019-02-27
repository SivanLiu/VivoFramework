package android.app;

import android.app.VrManager.CallbackEntry.AnonymousClass1;
import android.app.VrManager.CallbackEntry.AnonymousClass2;

final /* synthetic */ class -$Lambda$BjtyKj7ksh5kcpFCATScxTJ5PrQ implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f37-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f38-$f1;

    private final /* synthetic */ void $m$0() {
        ((AnonymousClass1) this.f38-$f1).m9lambda$-android_app_VrManager$CallbackEntry$1_902(this.f37-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((AnonymousClass2) this.f38-$f1).m10lambda$-android_app_VrManager$CallbackEntry$2_1220(this.f37-$f0);
    }

    public /* synthetic */ -$Lambda$BjtyKj7ksh5kcpFCATScxTJ5PrQ(byte b, boolean z, Object obj) {
        this.$id = b;
        this.f37-$f0 = z;
        this.f38-$f1 = obj;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            default:
                throw new AssertionError();
        }
    }
}
