package android.app;

import android.app.EnterTransitionCoordinator.AnonymousClass3;
import android.os.Bundle;
import android.util.ArrayMap;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$Pcw-0289sroTvc5U7X-pS90OouM implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f40-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f41-$f1;

    private final /* synthetic */ void $m$0() {
        ((ActivityTransitionCoordinator) this.f40-$f0).lambda$-android_app_ActivityTransitionCoordinator_27512((ArrayList) this.f41-$f1);
    }

    private final /* synthetic */ void $m$1() {
        ((ActivityTransitionState) this.f40-$f0).m5lambda$-android_app_ActivityTransitionState_12157((Activity) this.f41-$f1);
    }

    private final /* synthetic */ void $m$2() {
        ((AnonymousClass3) this.f40-$f0).m19lambda$-android_app_EnterTransitionCoordinator$3_17809((Bundle) this.f41-$f1);
    }

    private final /* synthetic */ void $m$3() {
        ((AnonymousClass3) this.f40-$f0).m18lambda$-android_app_EnterTransitionCoordinator$3_17761((Bundle) this.f41-$f1);
    }

    private final /* synthetic */ void $m$4() {
        ((EnterTransitionCoordinator) this.f40-$f0).m16lambda$-android_app_EnterTransitionCoordinator_6436((ArrayMap) this.f41-$f1);
    }

    private final /* synthetic */ void $m$5() {
        ((ExitTransitionCoordinator) this.f40-$f0).m12lambda$-android_app_ExitTransitionCoordinator_6391((ArrayList) this.f41-$f1);
    }

    public /* synthetic */ -$Lambda$Pcw-0289sroTvc5U7X-pS90OouM(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f40-$f0 = obj;
        this.f41-$f1 = obj2;
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
            default:
                throw new AssertionError();
        }
    }
}
