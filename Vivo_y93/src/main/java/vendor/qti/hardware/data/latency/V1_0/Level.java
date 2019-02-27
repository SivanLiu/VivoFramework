package vendor.qti.hardware.data.latency.V1_0;

import java.util.ArrayList;

public final class Level {
    public static final long L1 = 1;
    public static final long L2 = 2;
    public static final long L3 = 3;
    public static final long L4 = 4;

    public static final String toString(long o) {
        if (o == 1) {
            return "L1";
        }
        if (o == 2) {
            return "L2";
        }
        if (o == 3) {
            return "L3";
        }
        if (o == 4) {
            return "L4";
        }
        return "0x" + Long.toHexString(o);
    }

    public static final String dumpBitfield(long o) {
        ArrayList<String> list = new ArrayList();
        long flipped = 0;
        if ((o & 1) == 1) {
            list.add("L1");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("L2");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("L3");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("L4");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Long.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
