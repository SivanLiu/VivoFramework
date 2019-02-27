package com.vivo.services.rms.appmng.namelist;

import com.vivo.services.rms.EventNotifier;
import com.vivo.services.rms.ProcessInfo;
import com.vivo.services.rms.ProcessList;
import java.util.ArrayList;
import java.util.HashMap;

public class OomStaticList {
    private static final String ADD = "add";
    private static final HashMap<String, OomNode> MAP = new HashMap();
    private static final String REMOVE = "remove";
    private static final String UPDATE = "update";
    private static int sMinAdj = ProcessList.UNKNOWN_ADJ;
    private static boolean sUpdated = false;

    static {
        restore();
    }

    public static void restore() {
        synchronized (MAP) {
            if (sUpdated || MAP.isEmpty()) {
                sUpdated = false;
                sMinAdj = ProcessList.UNKNOWN_ADJ;
                MAP.clear();
                put(EventNotifier.PROC_NAME, 0, 11, 1);
                put("com.vivo.permissionmanager", 100, 2, 1);
                put("com.iqoo.secure:remote", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.vivo.safecenter", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.bbk.updater:remote", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.vivo.sim.contacts", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.android.vendors.bridge.softsim", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.redteamobile.virtual.softsim", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.android.providers.calendar", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.vivo.weather.provider", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("android.process.media", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("android.process.acore", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.vivo.doubletimezoneclock", ProcessList.BACKUP_APP_ADJ, 11, 0);
                put("com.vivo.bsptest", 0, 11, 1);
            }
        }
    }

    public static OomNode getNode(ProcessInfo pi, int curAdj) {
        if (MAP.isEmpty() || curAdj <= sMinAdj) {
            return null;
        }
        OomNode oomNode;
        synchronized (MAP) {
            oomNode = (OomNode) MAP.get(pi.mProcName);
        }
        return oomNode;
    }

    private static void put(String procName, int adj, int state, int sched) {
        if (adj < sMinAdj) {
            sMinAdj = adj;
        }
        MAP.put(procName, new OomNode(adj, state, sched));
    }

    public static void apply(String policy, ArrayList<String> procs, ArrayList<Integer> adjs, ArrayList<Integer> states, ArrayList<Integer> scheds) {
        synchronized (MAP) {
            sUpdated = true;
            if (policy.equals(ADD)) {
                add(procs, adjs, states, scheds);
            } else if (policy.equals(REMOVE)) {
                remove(procs);
            } else if (policy.equals(UPDATE)) {
                MAP.clear();
                sMinAdj = ProcessList.UNKNOWN_ADJ;
                add(procs, adjs, states, scheds);
            }
        }
    }

    private static void add(ArrayList<String> procs, ArrayList<Integer> adjs, ArrayList<Integer> states, ArrayList<Integer> scheds) {
        if (procs != null && adjs != null && states != null && scheds != null) {
            for (int i = 0; i < procs.size(); i++) {
                put((String) procs.get(i), ((Integer) adjs.get(i)).intValue(), ((Integer) states.get(i)).intValue(), ((Integer) scheds.get(i)).intValue());
            }
        }
    }

    private static void remove(ArrayList<String> procs) {
        if (procs != null) {
            for (int i = 0; i < procs.size(); i++) {
                MAP.remove(procs.get(i));
            }
        }
    }
}
