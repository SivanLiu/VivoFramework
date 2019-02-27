package android.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtTelephonyAdapter {
    private static FtTelephony sImpl = null;
    private static FtTelephony sImpl2 = null;

    private FtTelephonyAdapter() {
    }

    public static FtTelephony getFtTelephony(Context context) {
        if (sImpl2 != null) {
            return sImpl2;
        }
        if (context == null) {
            return null;
        }
        try {
            sImpl2 = (FtTelephony) Class.forName("com.android.internal.telephony.FtTelephonyAdapterImpl").getConstructors()[1].newInstance(new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sImpl2;
    }
}
