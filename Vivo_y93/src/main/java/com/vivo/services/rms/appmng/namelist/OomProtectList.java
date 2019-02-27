package com.vivo.services.rms.appmng.namelist;

import android.os.SystemProperties;
import com.vivo.services.rms.ProcessInfo;
import com.vivo.services.rms.ProcessList;
import java.util.ArrayList;
import java.util.HashMap;

public class OomProtectList {
    private static final HashMap<String, MyNode> MAP = new HashMap();
    private static final boolean OVERSEAS = SystemProperties.get("ro.vivo.product.overseas", "no").equals("yes");
    private static int sMinAdj = ProcessList.UNKNOWN_ADJ;
    private static boolean sUpdated = false;

    private static class MyNode extends OomNode {
        public int duration;

        public MyNode(int adj, int dt) {
            super(adj, 11, 0);
            this.adj = adj;
            this.duration = dt;
        }
    }

    static {
        restore();
    }

    public static void restore() {
        synchronized (MAP) {
            if (sUpdated || MAP.isEmpty()) {
                sUpdated = false;
                sMinAdj = ProcessList.UNKNOWN_ADJ;
                MAP.clear();
                if (OVERSEAS) {
                    put("com.facebook.katana", 451, 3600000);
                    put("com.facebook.orca", 451, 3600000);
                    put("com.whatsapp", 451, 3600000);
                } else {
                    put("com.tencent.mm:push", ProcessList.PROTECT_SERVICE_ADJ, 43200000);
                    put("com.tencent.mobileqq:MSF", ProcessList.PROTECT_SERVICE_ADJ, 43200000);
                    put("com.tencent.mm", 451, 3600000);
                    put("com.tencent.mm:tools", 451, 1800000);
                    put("com.tencent.mobileqq", 451, 3600000);
                    put("com.tencent.tmgp.sgame", 452, 1800000);
                    put("com.tencent.tmgp.sgame:xg_service_v2", 452, 1800000);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0027, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static OomNode getNode(ProcessInfo pi, int curAdj) {
        if (curAdj <= sMinAdj) {
            return null;
        }
        synchronized (MAP) {
            MyNode node = (MyNode) MAP.get(pi.mProcName);
            if (node == null || node.adj >= curAdj || pi.getInvisibleTime() >= ((long) node.duration)) {
            } else {
                return node;
            }
        }
    }

    public static void apply(ArrayList<String> activities, ArrayList<String> services, ArrayList<String> games) {
        synchronized (MAP) {
            sUpdated = true;
            MAP.clear();
            sMinAdj = ProcessList.UNKNOWN_ADJ;
            if (services != null) {
                for (String proc : services) {
                    put(proc, ProcessList.PROTECT_SERVICE_ADJ, 43200000);
                }
            }
            if (activities != null) {
                for (String proc2 : activities) {
                    put(proc2, 451, 3600000);
                }
            }
            if (games != null) {
                for (String proc22 : games) {
                    put(proc22, 452, 1800000);
                }
            }
        }
    }

    private static void put(String procName, int adj, int duration) {
        if (adj < sMinAdj) {
            sMinAdj = adj;
        }
        MAP.put(procName, new MyNode(adj, duration));
    }
}
