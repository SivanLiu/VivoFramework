package com.vivo.common.autobrightness;

import android.util.Slog;
import java.util.HashMap;

public final class DataParameter {
    private static final String TAG = "DataParameter";
    public long duration;
    public long endTime;
    public String eventId;
    public String label;
    public HashMap<String, String> params = null;
    public long startTime;
    public int useNum;

    public DataParameter(String eventId, String label, long startTime, long endTime, long duration, int useNum, HashMap<String, String> params) {
        this.eventId = eventId;
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.useNum = useNum;
        this.params = params;
    }

    public DataParameter(DataParameter other) {
        copyFrom(other);
    }

    public void copyFrom(DataParameter other) {
        if (other == null) {
            Slog.d(TAG, "copyFrom other is null.");
        }
        this.eventId = other.eventId;
        this.label = other.label;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.duration = other.duration;
        this.useNum = other.useNum;
        this.params = other.params;
    }

    public String toString() {
        return "eventId=" + this.eventId + " label=" + this.label + " startTime=" + this.startTime + " endTime=" + this.endTime + " duration=" + this.duration + " useNum=" + this.useNum + " params=" + (this.params != null ? this.params.toString() : "null");
    }
}
