package android.net.lowpan;

import android.net.lowpan.LowpanManager.AnonymousClass2;
import android.net.lowpan.LowpanManager.Callback;

final /* synthetic */ class -$Lambda$fU5N8X3bFktKBQFPK6v4czv7e_Y implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f97-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f98-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f99-$f2;

    private final /* synthetic */ void $m$0() {
        ((AnonymousClass2) this.f97-$f0).m30lambda$-android_net_lowpan_LowpanManager$2_8833((ILowpanInterface) this.f98-$f1, (Callback) this.f99-$f2);
    }

    private final /* synthetic */ void $m$1() {
        ((AnonymousClass2) this.f97-$f0).m31lambda$-android_net_lowpan_LowpanManager$2_9391((ILowpanInterface) this.f98-$f1, (Callback) this.f99-$f2);
    }

    public /* synthetic */ -$Lambda$fU5N8X3bFktKBQFPK6v4czv7e_Y(byte b, Object obj, Object obj2, Object obj3) {
        this.$id = b;
        this.f97-$f0 = obj;
        this.f98-$f1 = obj2;
        this.f99-$f2 = obj3;
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
