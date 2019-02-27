package android.telecom;

import android.telecom.Call.Callback;
import android.telecom.Call.RttCall;

final /* synthetic */ class -$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f38-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f39-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f40-$f2;
    /* renamed from: -$f3 */
    private final /* synthetic */ Object f41-$f3;

    private final /* synthetic */ void $m$0() {
        ((Callback) this.f39-$f1).onRttStatusChanged((Call) this.f40-$f2, this.f38-$f0, (RttCall) this.f41-$f3);
    }

    public /* synthetic */ -$Lambda$C1mff0scl0rlO_JIsUmJ5H-4cmo(boolean z, Object obj, Object obj2, Object obj3) {
        this.f38-$f0 = z;
        this.f39-$f1 = obj;
        this.f40-$f2 = obj2;
        this.f41-$f3 = obj3;
    }

    public final void run() {
        $m$0();
    }
}
