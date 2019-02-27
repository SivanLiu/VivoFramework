package com.vivo.services.epm;

import android.content.Context;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public abstract class BaseExceptionPolicyHandler {
    private static final int EVENT_DATA_MAX_RECORD_NUMBER = 10;
    protected Context mContext;
    protected LimitQueue<EventData> mEventDataRecords = new LimitQueue(10);
    protected Set<Integer> mInterestedExceptionTypes = new HashSet();

    public abstract void handleExceptionEvent(EventData eventData);

    public BaseExceptionPolicyHandler(Context context) {
        this.mContext = context;
    }

    public final synchronized void addInterestedException(int type) {
        this.mInterestedExceptionTypes.add(Integer.valueOf(type));
    }

    public final synchronized void removeInterestedException(int type) {
        this.mInterestedExceptionTypes.remove(Integer.valueOf(type));
    }

    public final synchronized boolean isInterestedInExcetionType(int type) {
        return this.mInterestedExceptionTypes.contains(Integer.valueOf(type));
    }

    public final void recordExceptionEvent(EventData data) {
        this.mEventDataRecords.offer(data);
    }

    public final LinkedList<EventData> getExceptionEventRecord() {
        return this.mEventDataRecords.getQueue();
    }

    public final void dump(PrintWriter pw) {
        pw.println("***********************************************************************");
        pw.println(getClass().getName() + " interested in exception " + this.mInterestedExceptionTypes);
        String extraInfo = onExtraDump();
        if (extraInfo != null) {
            pw.println("extra info:{");
            pw.println(extraInfo);
            pw.println("}");
        }
        pw.println("***********************************************************************");
    }

    public String onExtraDump() {
        return null;
    }
}
