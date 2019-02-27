package android.net.lowpan;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.lowpan.LowpanInterface.Callback;

final /* synthetic */ class -$Lambda$lq0tFj928XFoCdCDLCq_E-OIg9U implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f102-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f103-$f1;

    private final /* synthetic */ void $m$0() {
        ((InternalCallback) this.f102-$f0).m51xc1833cb6((byte[]) this.f103-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((Callback) this.f102-$f0).onLinkAddressAdded((LinkAddress) this.f103-$f1);
    }

    private final /* synthetic */ void $m$2() {
        ((Callback) this.f102-$f0).onLinkAddressRemoved((LinkAddress) this.f103-$f1);
    }

    private final /* synthetic */ void $m$3() {
        ((Callback) this.f102-$f0).onLinkNetworkAdded((IpPrefix) this.f103-$f1);
    }

    private final /* synthetic */ void $m$4() {
        ((Callback) this.f102-$f0).onLinkNetworkRemoved((IpPrefix) this.f103-$f1);
    }

    private final /* synthetic */ void $m$5() {
        ((Callback) this.f102-$f0).onLowpanIdentityChanged((LowpanIdentity) this.f103-$f1);
    }

    private final /* synthetic */ void $m$6() {
        ((Callback) this.f102-$f0).onRoleChanged((String) this.f103-$f1);
    }

    private final /* synthetic */ void $m$7() {
        ((Callback) this.f102-$f0).onStateChanged((String) this.f103-$f1);
    }

    private final /* synthetic */ void $m$8() {
        ((LowpanScanner.Callback) this.f102-$f0).onNetScanBeacon((LowpanBeaconInfo) this.f103-$f1);
    }

    public /* synthetic */ -$Lambda$lq0tFj928XFoCdCDLCq_E-OIg9U(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f102-$f0 = obj;
        this.f103-$f1 = obj2;
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
            case (byte) 3:
                $m$3();
                return;
            case (byte) 4:
                $m$4();
                return;
            case (byte) 5:
                $m$5();
                return;
            case (byte) 6:
                $m$6();
                return;
            case (byte) 7:
                $m$7();
                return;
            case (byte) 8:
                $m$8();
                return;
            default:
                throw new AssertionError();
        }
    }
}
