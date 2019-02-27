package vendor.qti.hardware.data.latency.V1_0;

import java.util.ArrayList;

public final class Radio {
    public static final long WLAN = 1;
    public static final long WWAN = 0;

    public static final String toString(long o) {
        if (o == 0) {
            return "WWAN";
        }
        if (o == 1) {
            return "WLAN";
        }
        return "0x" + Long.toHexString(o);
    }

    public static final String dumpBitfield(long o) {
        ArrayList<String> list = new ArrayList();
        long flipped = 0;
        list.add("WWAN");
        if ((o & 1) == 1) {
            list.add("WLAN");
            flipped = 1;
        }
        if (o != flipped) {
            list.add("0x" + Long.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
