package android.net.lowpan;

import android.net.lowpan.LowpanInterface.Callback;

final /* synthetic */ class -$Lambda$kGwbyTn61Si3sH7muskKIr7PCeU implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f100-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f101-$f1;

    private final /* synthetic */ void $m$0() {
        ((Callback) this.f101-$f1).onConnectedChanged(this.f100-$f0);
    }

    private final /* synthetic */ void $m$1() {
        ((Callback) this.f101-$f1).onEnabledChanged(this.f100-$f0);
    }

    private final /* synthetic */ void $m$2() {
        ((Callback) this.f101-$f1).onUpChanged(this.f100-$f0);
    }

    public /* synthetic */ -$Lambda$kGwbyTn61Si3sH7muskKIr7PCeU(byte b, boolean z, Object obj) {
        this.$id = b;
        this.f100-$f0 = z;
        this.f101-$f1 = obj;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            case (byte) 2:
                $m$2();
                return;
            default:
                throw new AssertionError();
        }
    }
}
