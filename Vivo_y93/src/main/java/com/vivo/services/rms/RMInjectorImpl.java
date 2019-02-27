package com.vivo.services.rms;

import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.util.ArraySet;
import com.android.server.am.RMAmsHelper;
import com.android.server.am.RMInjector;
import com.android.server.am.RMProcHelper;
import com.android.server.am.RMProcInfo;
import com.vivo.services.rms.appmng.AppManager;
import com.vivo.services.rms.appmng.DeathReason;
import com.vivo.services.rms.appmng.namelist.OomNode;
import com.vivo.services.rms.appmng.namelist.OomPreviousList;
import com.vivo.services.rms.appmng.namelist.OomProtectList;
import com.vivo.services.rms.appmng.namelist.OomStaticList;
import com.vivo.services.rms.appmng.namelist.WidgetList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.util.ArrayList;

public class RMInjectorImpl extends RMInjector {
    public static int MY_PID = Process.myPid();
    private Context mContext;
    private boolean mMonkeyState = false;
    private OomNode mOomNode = new OomNode();

    public void initialize(Object ams, Context context) {
        RMAmsHelper.initialize(ams);
        RMProcHelper.initialize();
        RMServer.publish(context);
        this.mContext = context;
    }

    public void systemReady() {
        RMServer.getInstance().systemReady();
    }

    public static RMInjectorImpl self() {
        return (RMInjectorImpl) getInstance();
    }

    public void startActivity(String pkgName, String processName, int pid, int uid, int started) {
        EventDispatcher.getInstance().startActivity(pkgName, processName, pid, uid, started);
    }

    public RMProcInfo newProcInfo(Object parent, int uid, String pkgName, int flags, String procName) {
        return new ProcessInfo(parent, uid, pkgName, flags, procName);
    }

    public void addProcess(RMProcInfo pi) {
        if (pi.mPid > 0) {
            AppManager.getInstance().add((ProcessInfo) pi);
        }
    }

    public void removeProcess(RMProcInfo pi) {
        if (pi.mPid > 0) {
            DeathReason.fillReason((ProcessInfo) pi);
            AppManager.getInstance().remove((ProcessInfo) pi);
        }
    }

    public void addDepPkg(RMProcInfo pi, String pkg) {
        AppManager.getInstance().addDepPkg((ProcessInfo) pi, pkg);
    }

    public void addPkg(RMProcInfo pi, String pkg) {
        AppManager.getInstance().addPkg((ProcessInfo) pi, pkg);
    }

    public void modifyOomAdj(Object app) {
        ProcessInfo pi = (ProcessInfo) RMProcHelper.getInfo(app);
        if (pi != null) {
            int rawAdj = RMProcHelper.getInt(app, 13);
            if (rawAdj > 0) {
                int curProcState = RMProcHelper.getInt(app, 2);
                int curSchedGroup = RMProcHelper.getInt(app, 1);
                String curAdjType = RMProcHelper.getString(app, 10);
                ArrayList<?> acts = RMProcHelper.getArrayList(app, 8);
                long now = SystemClock.uptimeMillis();
                String setAdjType = null;
                this.mOomNode.adj = rawAdj;
                this.mOomNode.procState = curProcState;
                this.mOomNode.schedGroup = curSchedGroup;
                OomNode node = OomStaticList.getNode(pi, this.mOomNode.adj);
                if (node != null) {
                    applyOomNode(node);
                    setAdjType = "static";
                }
                node = OomProtectList.getNode(pi, this.mOomNode.adj);
                if (node != null) {
                    applyOomNode(node);
                    setAdjType = "protect";
                }
                if (acts.size() > 0) {
                    node = OomPreviousList.getNode(pi, this.mOomNode.adj);
                    if (node != null) {
                        applyOomNode(node);
                        setAdjType = "previous";
                    }
                }
                long lastProviderTime = RMProcHelper.getLong(app, 12);
                if (lastProviderTime > 0 && 20000 + lastProviderTime > now && this.mOomNode.adj > ProcessList.PREVIOUS_APP_ADJ) {
                    this.mOomNode.adj = ProcessList.PREVIOUS_APP_ADJ;
                    setAdjType = "provider";
                    if (this.mOomNode.procState > 14) {
                        this.mOomNode.procState = 14;
                    }
                }
                if (rawAdj > this.mOomNode.adj) {
                    rawAdj = this.mOomNode.adj;
                    RMProcHelper.setInt(app, 13, rawAdj);
                    RMProcHelper.setInt(app, 0, rawAdj);
                    RMProcHelper.setBoolean(app, 7, false);
                    if (curProcState > this.mOomNode.procState) {
                        RMProcHelper.setInt(app, 2, this.mOomNode.procState);
                    }
                    if (curSchedGroup != this.mOomNode.schedGroup) {
                        RMProcHelper.setInt(app, 1, this.mOomNode.schedGroup);
                    }
                    if (setAdjType != null) {
                        curAdjType = setAdjType;
                        RMProcHelper.setString(app, 10, setAdjType);
                    }
                }
            }
        }
    }

    public void applyOomAdjLocked(Object app) {
        boolean z = true;
        ProcessInfo pi = (ProcessInfo) RMProcHelper.getInfo(app);
        if (pi != null) {
            AppManager appmng = AppManager.getInstance();
            synchronized (appmng) {
                boolean z2;
                int curAdj = RMProcHelper.getInt(app, 0);
                int curSchedGroup = RMProcHelper.getInt(app, 1);
                String curAdjType = RMProcHelper.getString(app, 10);
                boolean foregroundActivities = RMProcHelper.getBoolean(app, 5);
                boolean foregroundServices = RMProcHelper.getBoolean(app, 6);
                boolean hasShownUi = RMProcHelper.getBoolean(app, 4);
                int adjSource = RMProcHelper.getInt(app, 3);
                ArrayList<?> acts = RMProcHelper.getArrayList(app, 8);
                ArraySet<?> svcs = RMProcHelper.getArraySet(app, 9);
                appmng.setFgActivityLocked(pi, !foregroundActivities ? "pers-top-activity".equals(curAdjType) : true);
                appmng.setFgServiceLocked(pi, foregroundServices);
                appmng.setHasShownUILocked(pi, hasShownUi);
                if (acts.size() > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                appmng.setHasActivityLocked(pi, z2);
                if (svcs.size() <= 0) {
                    z = false;
                }
                appmng.setHasServiceLocked(pi, z);
                appmng.setPausing(pi, ProcessStates.PAUSING_NAME.equals(curAdjType));
                appmng.setOomLocked(pi, adjSource);
                appmng.setAdjLocked(pi, curAdj);
                appmng.setSchedGroupLocked(pi, curSchedGroup);
            }
        }
    }

    private void applyOomNode(OomNode node) {
        if (this.mOomNode.adj > node.adj) {
            this.mOomNode.adj = node.adj;
            if (this.mOomNode.procState > node.procState) {
                this.mOomNode.procState = node.procState;
            }
            this.mOomNode.schedGroup = node.schedGroup;
        }
    }

    public void updateOomAdjLocked() {
        AppManager.getInstance().applyPenddings();
        boolean isMonkey = RMAmsHelper.isUserAMonkey();
        if (this.mMonkeyState != isMonkey) {
            EventDispatcher.getInstance().setMonkeyState(isMonkey ? 1 : 0);
        }
    }

    public boolean isMonkey() {
        return this.mMonkeyState;
    }

    public String getPkgNameByPid(int pid) {
        ProcessInfo info = AppManager.getInstance().getProcessInfo(pid);
        if (info != null) {
            return info.mPkgName;
        }
        return String.valueOf(pid);
    }

    public String wrapReason(String reason, int callingPid) {
        if (callingPid <= 0 || callingPid == MY_PID) {
            return reason;
        }
        return reason + " by " + getPkgNameByPid(callingPid);
    }

    public void addWidget(String pkg) {
        WidgetList.addWidget(pkg);
    }

    public void removeWidget(String pkg) {
        WidgetList.removeWidget(pkg);
    }

    public void showWindow(int pid, int winId) {
        AppManager.getInstance().addWindow(pid, winId);
    }

    public void hideWindow(int pid, int winId) {
        AppManager.getInstance().removeWindow(pid, winId);
    }

    public void destoryWindow(int pid, int winId) {
        AppManager.getInstance().removeWindow(pid, winId);
    }

    public void onProcAnr(String pkgName, String procName, String activity, String reason) {
        if (this.mContext != null) {
            ProcErrors.getInstance().onProcAnr(pkgName, procName, activity, reason);
        }
    }

    public void onProcCrash(String pkgName, String procName, CrashInfo crashInfo) {
        if (this.mContext != null) {
            ProcErrors.getInstance().onProcCrash(pkgName, procName, crashInfo);
        }
    }

    public Context getContext() {
        return this.mContext;
    }
}
