package com.vivo.services.rms;

import android.os.Bundle;
import android.os.SystemClock;
import com.android.server.am.RMProcInfo;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.util.HashSet;

public class ProcessInfo extends RMProcInfo {
    public static int ACTIVE_MASKS = 24;
    public static int ACTIVE_STATES = 287;

    public ProcessInfo(Object parent, int uid, String pkgName, int pkgFlags, String process) {
        this.mParent = parent;
        this.mUid = uid;
        this.mPkgName = pkgName;
        this.mPkgFlags = pkgFlags;
        this.mProcName = process;
    }

    public void makeActive() {
        this.mLastActiveElapsedTime = SystemClock.elapsedRealtime();
        this.mLastActiveTime = SystemClock.uptimeMillis();
        this.mLastInvisibleTime = this.mLastActiveTime;
        this.mPkgList.clear();
        this.mDepPkgList.clear();
        this.mPkgList.add(this.mPkgName);
        this.mKillReason = null;
        this.mStates = 0;
    }

    public void setState(int state, int mask) {
        this.mStates = (this.mStates & (~mask)) | (state & mask);
        if ((ACTIVE_STATES & state) != 0 || (ACTIVE_MASKS & mask) != 0) {
            this.mLastActiveElapsedTime = SystemClock.elapsedRealtime();
            this.mLastActiveTime = SystemClock.uptimeMillis();
        }
    }

    public boolean isVisible() {
        return (this.mStates & 8) != 0;
    }

    public long getInvisibleTime() {
        return isVisible() ? 0 : SystemClock.uptimeMillis() - this.mLastInvisibleTime;
    }

    public long getInactiveTime() {
        return isVisible() ? 0 : SystemClock.elapsedRealtime() - this.mLastActiveElapsedTime;
    }

    public boolean hasWindow(int winId) {
        if (this.mWindows != null) {
            return this.mWindows.contains(Integer.valueOf(winId));
        }
        this.mWindows = new HashSet(2);
        return false;
    }

    public void addDepPkg(String pkg) {
        this.mDepPkgList.add(pkg);
    }

    public void addPkg(String pkg) {
        this.mPkgList.add(pkg);
    }

    public Bundle toBundleLocked() {
        Bundle data = new Bundle();
        data.putInt("uid", this.mUid);
        data.putString("pkg", this.mPkgName);
        data.putInt("pkgFlags", this.mPkgFlags);
        data.putString("name", this.mProcName);
        data.putInt("pid", this.mPid);
        data.putString("createReason", this.mCreateReason);
        data.putLong("lastActiveElapsedTime", this.mLastActiveElapsedTime);
        data.putLong("lastInvisibleTime", this.mLastInvisibleTime);
        data.putLong("lastActiveTime", this.mLastActiveTime);
        data.putInt("adj", this.mAdj);
        data.putInt("schedGroup", this.mSchedGroup);
        data.putInt("oom", this.mOom);
        data.putInt("states", this.mStates);
        data.putStringArrayList("depPkgList", this.mDepPkgList);
        data.putStringArrayList("pkgList", this.mPkgList);
        return data;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(ProcessStates.HASSERVICE);
        stringBuilder(builder);
        return builder.toString();
    }

    public void stringBuilder(StringBuilder builder) {
        builder.append("*");
        builder.append(this.mProcName);
        builder.append(" uid:").append(this.mUid);
        builder.append(" pid:").append(this.mPid);
        builder.append(" adj:").append(this.mAdj);
        builder.append(" sched:").append(this.mSchedGroup);
        builder.append(" invisible:").append(getInvisibleTime() / 1000);
        builder.append("s");
        builder.append(" inactive:").append(getInactiveTime() / 1000);
        builder.append("s");
        if (this.mOom != 0) {
            builder.append(" oom:").append(this.mOom);
        }
        builder.append("\n\t      pkgList:").append(this.mPkgList.toString());
        if (this.mStates != 0) {
            builder.append(String.format("\n\t      states:[%s]", new Object[]{ProcessStates.getName(this.mStates)}));
        }
        if (this.mCreateReason != null) {
            builder.append("\n\t      create reason:").append(this.mCreateReason);
        }
        if (!this.mDepPkgList.isEmpty()) {
            builder.append("\n\t      depPkgList:").append(this.mDepPkgList.toString());
        }
    }
}
