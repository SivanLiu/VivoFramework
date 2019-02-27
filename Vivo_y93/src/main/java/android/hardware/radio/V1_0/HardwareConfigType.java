package android.hardware.radio.V1_0;

import com.android.internal.telephony.FtTelephonyAdapterImpl.SimType;
import java.util.ArrayList;

public final class HardwareConfigType {
    public static final int MODEM = 0;
    public static final int SIM = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "MODEM";
        }
        if (o == 1) {
            return SimType.SIM_TYPE_SIM_TAG;
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("MODEM");
        if ((o & 1) == 1) {
            list.add(SimType.SIM_TYPE_SIM_TAG);
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
