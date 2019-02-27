package com.vivo.services.rms.appmng;

import java.io.PrintWriter;
import java.util.ArrayList;

public class RecentTask {
    private static final int MAX_SIZE = 13;
    private ArrayList<RecentItem> mRecent = new ArrayList(this.mSize);
    private int mSize = 13;

    class RecentItem {
        String procName;
        int uid;

        RecentItem(String pkg, int uid) {
            fill(pkg, uid);
        }

        void fill(String pkg, int uid) {
            this.procName = pkg;
            this.uid = uid;
        }

        public String toString() {
            return String.format("%s(%d)", new Object[]{this.procName, Integer.valueOf(this.uid)});
        }
    }

    public void put(String procName, int uid) {
        synchronized (this) {
            if (this.mSize == 0) {
                return;
            }
            int index = indexOf(procName, uid);
            Object obj = null;
            if (index != -1) {
                obj = (RecentItem) this.mRecent.remove(index);
            }
            if (this.mRecent.size() >= this.mSize) {
                obj = (RecentItem) this.mRecent.remove(0);
            }
            if (obj != null) {
                obj.fill(procName, uid);
            } else {
                obj = new RecentItem(procName, uid);
            }
            this.mRecent.add(obj);
        }
    }

    public void remove(String procName, int uid) {
        synchronized (this) {
            int index = indexOf(procName, uid);
            if (index != -1) {
                this.mRecent.remove(index);
            }
        }
    }

    public boolean contains(String procName, int uid) {
        boolean z;
        synchronized (this) {
            z = indexOf(procName, uid) != -1;
        }
        return z;
    }

    private int indexOf(String procName, int uid) {
        for (int i = 0; i < this.mRecent.size(); i++) {
            RecentItem item = (RecentItem) this.mRecent.get(i);
            if (uid == item.uid && procName.equals(item.procName)) {
                return i;
            }
        }
        return -1;
    }

    public int taskId(String procName, int uid) {
        synchronized (this) {
            int index = indexOf(procName, uid);
            if (index != -1) {
                int size = (this.mRecent.size() - 1) - index;
                return size;
            }
            return -1;
        }
    }

    public void setSize(int size) {
        synchronized (this) {
            this.mSize = Math.min(Math.max(size, 1), 13);
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this) {
            if (this.mRecent.size() > 0) {
                pw.print("*recent:");
            }
            for (int i = this.mRecent.size() - 1; i >= 0; i--) {
                pw.print(" ");
                pw.print(this.mRecent.get(i));
            }
            if (this.mRecent.size() > 0) {
                pw.println();
            }
        }
    }
}
