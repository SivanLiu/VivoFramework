package com.android.internal.telephony.imsphone;

import android.telephony.PhoneNumberUtils;
import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker.IRetryTimeout;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker.PhoneNumberUtilsProxy;

final /* synthetic */ class -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w implements PhoneNumberUtilsProxy {
    public static final /* synthetic */ -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w $INST$0 = new -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w();

    /* renamed from: com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w$2 */
    final /* synthetic */ class AnonymousClass2 implements INotifyStatusChanged {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f47-$f0;

        private final /* synthetic */ void $m$0() {
            ((ImsPhoneCallTracker) this.f47-$f0).m27x31588f77();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f47-$f0 = obj;
        }

        public final void notifyStatusChanged() {
            $m$0();
        }
    }

    /* renamed from: com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w$3 */
    final /* synthetic */ class AnonymousClass3 implements IRetryTimeout {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f48-$f0;

        private final /* synthetic */ int $m$0() {
            return ((ImsPhoneCallTracker) this.f48-$f0).m28x3159073c();
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.f48-$f0 = obj;
        }

        public final int get() {
            return $m$0();
        }
    }

    private final /* synthetic */ boolean $m$0(String arg0) {
        return PhoneNumberUtils.isEmergencyNumber(arg0);
    }

    private /* synthetic */ -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w() {
    }

    public final boolean isEmergencyNumber(String str) {
        return $m$0(str);
    }
}
