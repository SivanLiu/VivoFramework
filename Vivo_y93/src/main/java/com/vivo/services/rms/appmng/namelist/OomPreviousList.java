package com.vivo.services.rms.appmng.namelist;

import android.os.Process;
import android.os.SystemClock;
import com.vivo.services.rms.ProcessInfo;
import com.vivo.services.rms.ProcessList;
import com.vivo.services.rms.appmng.AppManager;
import java.util.ArrayList;

public class OomPreviousList {
    private static final ArrayList<String> EXCLUDED_LIST = new ArrayList();
    private static final ArrayList<OomNode> LIST = new ArrayList();
    private static long TIME_OUT = 86400000;
    private static int sMinAdj = ProcessList.UNKNOWN_ADJ;
    private static final long sTotalMem = Process.getTotalMemory();
    private static boolean sUpdated = false;

    static {
        restore();
    }

    public static void restore() {
        synchronized (LIST) {
            if (sUpdated || LIST.isEmpty()) {
                sUpdated = false;
                LIST.clear();
                sMinAdj = ProcessList.UNKNOWN_ADJ;
                put(ProcessList.VERY_LASTEST_PREVIOUS_APP_ADJ, 14);
                put(ProcessList.LASTEST_PREVIOUS_APP_ADJ, 14);
                put(ProcessList.LASTEST_PREVIOUS_APP_MAX, 14);
                put(ProcessList.PREVIOUS_APP_ADJ, 14);
                put(701, 14);
                if (sTotalMem > 5368709120L) {
                    put(702, 14);
                    put(703, 14);
                    put(704, 14);
                }
                if (sTotalMem > 6442450944L) {
                    put(705, 14);
                    put(706, 14);
                }
                if (sTotalMem > 7516192768L) {
                    put(707, 14);
                    put(708, 14);
                }
            }
        }
    }

    public static OomNode getNode(ProcessInfo pi, int curAdj) {
        long now = SystemClock.uptimeMillis();
        if (curAdj <= sMinAdj || now - pi.mLastInvisibleTime > TIME_OUT) {
            return null;
        }
        int taskId = AppManager.getInstance().getRecentTask().taskId(pi.mProcName, pi.mUid);
        if (taskId == -1 || taskId > LIST.size() - 1) {
            return null;
        }
        OomNode oomNode;
        synchronized (LIST) {
            oomNode = (OomNode) LIST.get(taskId);
        }
        return oomNode;
    }

    private static void put(int adj, int state) {
        if (adj < sMinAdj) {
            sMinAdj = adj;
        }
        LIST.add(new OomNode(adj, state, 0));
    }

    public static void apply(ArrayList<Integer> adjs, ArrayList<Integer> states) {
        if (adjs != null && states != null && adjs.size() == states.size()) {
            synchronized (LIST) {
                sUpdated = true;
                LIST.clear();
                sMinAdj = ProcessList.UNKNOWN_ADJ;
                for (int i = 0; i < adjs.size(); i++) {
                    put(((Integer) adjs.get(i)).intValue(), ((Integer) states.get(i)).intValue());
                }
                AppManager.getInstance().getRecentTask().setSize(LIST.size());
            }
        }
    }

    public static void updateExcludedList(ArrayList<String> lists) {
        synchronized (EXCLUDED_LIST) {
            EXCLUDED_LIST.clear();
            EXCLUDED_LIST.addAll(lists);
        }
    }

    public static boolean excluded(String process) {
        if (process == null) {
            return false;
        }
        boolean contains;
        synchronized (EXCLUDED_LIST) {
            contains = EXCLUDED_LIST.contains(process);
        }
        return contains;
    }

    public static void restoreExcludedList() {
        synchronized (EXCLUDED_LIST) {
            EXCLUDED_LIST.clear();
        }
    }
}
