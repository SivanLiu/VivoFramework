package com.vivo.services.rms.appmng;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.server.am.RMAmsHelper;
import com.android.server.am.RMProcHelper;
import com.vivo.services.rms.EventDispatcher;
import com.vivo.services.rms.EventNotifier;
import com.vivo.services.rms.ProcessInfo;
import com.vivo.services.rms.RMServer;
import com.vivo.services.rms.appmng.namelist.OomPreviousList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.rms.sdk.IntArrayFactory;
import java.io.PrintWriter;

public class AppManager {
    private final IntValueChangeItem mPendingADJChange;
    private final IntValueChangeItem mPendingSchedChange;
    private final StateChangeItem mPendingStateChange;
    private final SparseArray<ProcessInfo> mProcs;
    private final RecentTask mRecentTask;

    private static class Instance {
        private static final AppManager INSTANCE = new AppManager();

        private Instance() {
        }
    }

    private static class IntValueChangeItem {
        private final int mEventType;
        private final SparseArray<Integer> mValues = new SparseArray(12);

        public IntValueChangeItem(int type) {
            this.mEventType = type;
        }

        public void put(int pid, int value) {
            this.mValues.put(pid, Integer.valueOf(value));
        }

        public void apply() {
            int length = this.mValues.size();
            if (length != 0) {
                int[] pids = IntArrayFactory.create(length);
                int[] values = IntArrayFactory.create(length);
                for (int i = 0; i < length; i++) {
                    pids[i] = this.mValues.keyAt(i);
                    values[i] = ((Integer) this.mValues.valueAt(i)).intValue();
                }
                this.mValues.clear();
                switch (this.mEventType) {
                    case 0:
                        EventDispatcher.getInstance().setAdj(pids, values);
                        break;
                    case 2:
                        EventDispatcher.getInstance().setSchedGroup(pids, values);
                        break;
                }
            }
        }
    }

    private static class StateChangeItem {
        private final SparseArray<Integer> mMasks;
        private final SparseArray<Integer> mStates;

        /* synthetic */ StateChangeItem(StateChangeItem -this0) {
            this();
        }

        private StateChangeItem() {
            this.mStates = new SparseArray(5);
            this.mMasks = new SparseArray(5);
        }

        public void putState(int pid, int state, int mask) {
            int states = ((Integer) this.mStates.get(pid, Integer.valueOf(0))).intValue();
            int masks = ((Integer) this.mMasks.get(pid, Integer.valueOf(0))).intValue();
            if ((masks & mask) != 0) {
                states &= ~mask;
                masks &= ~mask;
            } else {
                masks |= mask;
                states = ((~mask) & states) | (state & mask);
            }
            this.mStates.put(pid, Integer.valueOf(states));
            this.mMasks.put(pid, Integer.valueOf(masks));
        }

        public void apply() {
            int length = this.mStates.size();
            if (length != 0) {
                int[] pids = IntArrayFactory.create(length);
                int[] states = IntArrayFactory.create(length);
                int[] masks = IntArrayFactory.create(length);
                for (int i = 0; i < length; i++) {
                    pids[i] = this.mStates.keyAt(i);
                    states[i] = ((Integer) this.mStates.valueAt(i)).intValue();
                    masks[i] = ((Integer) this.mMasks.valueAt(i)).intValue();
                }
                this.mStates.clear();
                this.mMasks.clear();
                EventDispatcher.getInstance().setStates(pids, states, masks);
            }
        }
    }

    /* synthetic */ AppManager(AppManager -this0) {
        this();
    }

    public static AppManager getInstance() {
        return Instance.INSTANCE;
    }

    private AppManager() {
        this.mProcs = new SparseArray(ProcessStates.HASSERVICE);
        this.mPendingADJChange = new IntValueChangeItem(0);
        this.mPendingSchedChange = new IntValueChangeItem(2);
        this.mPendingStateChange = new StateChangeItem();
        this.mRecentTask = new RecentTask();
    }

    public void doInitLocked(Bundle dest) {
        Bundle procs = new Bundle();
        for (int i = 0; i < this.mProcs.size(); i++) {
            procs.putBundle(String.valueOf(this.mProcs.keyAt(i)), ((ProcessInfo) this.mProcs.valueAt(i)).toBundleLocked());
        }
        if (!procs.keySet().isEmpty()) {
            dest.putBundle("_pids", procs);
        }
    }

    public ProcessInfo getProcessInfo(int pid) {
        ProcessInfo processInfo;
        synchronized (this) {
            processInfo = (ProcessInfo) this.mProcs.get(pid);
        }
        return processInfo;
    }

    public ProcessInfo add(ProcessInfo pi) {
        synchronized (this) {
            EventDispatcher.getInstance().add(pi.mUid, pi.mPkgName, pi.mPkgFlags, pi.mPid, pi.mProcName, pi.mCreateReason);
            this.mProcs.put(pi.mPid, pi);
            pi.makeActive();
        }
        return pi;
    }

    public void remove(ProcessInfo pi) {
        synchronized (this) {
            EventDispatcher.getInstance().remove(pi.mPid, pi.mKillReason);
            this.mProcs.remove(pi.mPid);
            if (EventNotifier.PROC_NAME.equals(pi.mProcName)) {
                RMServer.getInstance().getEventNotifier().setDeathReason(pi.mKillReason);
            }
        }
    }

    public void setOomLocked(ProcessInfo pi, int oom) {
        if (pi.mOom != oom) {
            EventDispatcher.getInstance().setOom(pi.mPid, oom);
            pi.mOom = oom;
        }
    }

    public void setAdjLocked(ProcessInfo pi, int adj) {
        if (pi.mAdj != adj) {
            pi.mAdj = adj;
            this.mPendingADJChange.put(pi.mPid, adj);
        }
    }

    public void setSchedGroupLocked(ProcessInfo pi, int schedGroup) {
        if (pi.mSchedGroup != schedGroup) {
            pi.mSchedGroup = schedGroup;
            this.mPendingSchedChange.put(pi.mPid, schedGroup);
        }
    }

    public void addDepPkg(ProcessInfo pi, String pkg) {
        synchronized (this) {
            if (!pi.mDepPkgList.contains(pkg)) {
                EventDispatcher.getInstance().addDepPkg(pi.mPid, pkg);
                pi.addDepPkg(pkg);
            }
        }
    }

    public void addPkg(ProcessInfo pi, String pkg) {
        synchronized (this) {
            if (!pi.mPkgList.contains(pkg)) {
                EventDispatcher.getInstance().addPkg(pi.mPid, pkg);
                pi.addPkg(pkg);
            }
        }
    }

    public RecentTask getRecentTask() {
        return this.mRecentTask;
    }

    public void setFgActivityLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, 1);
    }

    public void setFgServiceLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, 4);
    }

    public void setFgForceLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, 2);
    }

    public void setHasShownUILocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, 32);
    }

    public void setHasActivityLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, 64);
    }

    public void setHasServiceLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, ProcessStates.HASSERVICE);
    }

    public void setHasNotificationLocked(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, ProcessStates.HASNOTIFICATION);
    }

    public void setPausing(ProcessInfo pi, boolean has) {
        setStateLocked(pi, has, ProcessStates.PAUSING);
    }

    private void setStateLocked(ProcessInfo pi, boolean has, int mask) {
        int state = has ? mask : 0;
        if ((pi.mStates & mask) != state) {
            pi.setState(state, mask);
            this.mPendingStateChange.putState(pi.mPid, state, mask);
            if ((mask & 1) == 0) {
                return;
            }
            if ((state & 1) != 0) {
                this.mRecentTask.remove(pi.mProcName, pi.mUid);
            } else if (pi.mAdj >= 0 && pi.mParent != null && pi.mPid != RMAmsHelper.getHomePid() && (OomPreviousList.excluded(pi.mProcName) ^ 1) != 0 && RMProcHelper.getArrayList(pi.mParent, 8).size() > 0) {
                this.mRecentTask.put(pi.mProcName, pi.mUid);
            }
        }
    }

    public void addWindow(int pid, int winId) {
        synchronized (this) {
            ProcessInfo pi = (ProcessInfo) this.mProcs.get(pid);
            if (!(pi == null || pi.hasWindow(winId))) {
                pi.mWindows.add(Integer.valueOf(winId));
                if ((pi.mStates & 8) == 0) {
                    setStateLocked(pi, true, 8);
                    this.mPendingStateChange.apply();
                }
            }
        }
    }

    public void removeWindow(int pid, int winId) {
        synchronized (this) {
            ProcessInfo pi = (ProcessInfo) this.mProcs.get(pid);
            if (pi != null && pi.hasWindow(winId)) {
                pi.mWindows.remove(Integer.valueOf(winId));
                if (pi.mWindows.isEmpty() && (pi.mStates & 8) != 0) {
                    setStateLocked(pi, false, 8);
                    pi.mLastInvisibleTime = SystemClock.uptimeMillis();
                    this.mPendingStateChange.apply();
                }
            }
        }
    }

    public void applyPenddings() {
        synchronized (this) {
            this.mPendingADJChange.apply();
            this.mPendingStateChange.apply();
            this.mPendingSchedChange.apply();
        }
    }

    public static boolean strEquals(String a, String b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        return a.equals(b);
    }

    public void dump(PrintWriter pw) {
        synchronized (this) {
            StringBuilder builder = new StringBuilder(ProcessStates.HASSERVICE);
            for (int i = 0; i < this.mProcs.size(); i++) {
                builder.setLength(0);
                ((ProcessInfo) this.mProcs.valueAt(i)).stringBuilder(builder);
                pw.println(builder.toString());
            }
            this.mRecentTask.dump(pw);
            pw.println("*Total proc=" + this.mProcs.size());
        }
    }
}
