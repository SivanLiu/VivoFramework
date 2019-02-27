package vendor.qti.hardware.data.latency.V1_0;

import java.util.ArrayList;

public final class StatusCode {
    public static final long INVALID_ARGUMENTS = 1;
    public static final long OK = 0;
    public static final long UNKNOWN_ERROR = 2;

    public static final String toString(long o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "INVALID_ARGUMENTS";
        }
        if (o == 2) {
            return "UNKNOWN_ERROR";
        }
        return "0x" + Long.toHexString(o);
    }

    public static final String dumpBitfield(long o) {
        ArrayList<String> list = new ArrayList();
        long flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("INVALID_ARGUMENTS");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("UNKNOWN_ERROR");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Long.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
