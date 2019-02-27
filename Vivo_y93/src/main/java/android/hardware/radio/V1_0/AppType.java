package android.hardware.radio.V1_0;

import com.android.internal.telephony.FtTelephonyAdapterImpl.SimType;
import java.util.ArrayList;

public final class AppType {
    public static final int CSIM = 4;
    public static final int ISIM = 5;
    public static final int RUIM = 3;
    public static final int SIM = 1;
    public static final int UNKNOWN = 0;
    public static final int USIM = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return SimType.SIM_TYPE_SIM_TAG;
        }
        if (o == 2) {
            return SimType.SIM_TYPE_USIM_TAG;
        }
        if (o == 3) {
            return SimType.SIM_TYPE_UIM_TAG;
        }
        if (o == 4) {
            return SimType.SIM_TYPE_CSIM_TAG;
        }
        if (o == 5) {
            return "ISIM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add(SimType.SIM_TYPE_SIM_TAG);
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add(SimType.SIM_TYPE_USIM_TAG);
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add(SimType.SIM_TYPE_UIM_TAG);
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add(SimType.SIM_TYPE_CSIM_TAG);
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ISIM");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
