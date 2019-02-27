package com.qti.location.sdk.collection;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CachedLocation {
    private static final int CACHED_BUFFER_MAX = 15;
    private static double DEFAULT_LOCATION = 0.0d;
    private static float DEFAULT_SPEED = 0.0f;
    public static final String KEY_INVALID_TIME = "invTime";
    private static final String KEY_LOCATION = "loc";
    public static final String KEY_SPEED = "speed";
    private static final String KEY_TIMESTAMP = "ts";
    private boolean full = false;
    private int index = 0;
    public long invalidTime = 0;
    private double[] latitude = new double[CACHED_BUFFER_MAX];
    private double[] longitude = new double[CACHED_BUFFER_MAX];
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private float[] speed = new float[CACHED_BUFFER_MAX];
    private long[] timestamp = new long[CACHED_BUFFER_MAX];

    public void reset() {
        this.index = 0;
        this.full = false;
        this.invalidTime = 0;
        for (int i = 0; i < CACHED_BUFFER_MAX; i++) {
            this.latitude[i] = DEFAULT_LOCATION;
            this.longitude[i] = DEFAULT_LOCATION;
            this.speed[i] = DEFAULT_SPEED;
            this.timestamp[i] = 0;
        }
    }

    public void add(double lat, double lon, long time, boolean valid, float spe) {
        if (valid) {
            this.latitude[this.index] = lat;
            this.longitude[this.index] = lon;
            this.timestamp[this.index] = time;
            this.speed[this.index] = spe;
            this.index = (this.index + 1) % CACHED_BUFFER_MAX;
            if (this.index == 0 && (this.full ^ 1) != 0) {
                this.full = true;
                return;
            }
            return;
        }
        this.invalidTime++;
    }

    public String getString() {
        int i;
        String str = "";
        if (this.full) {
            i = this.index;
            while (i < CACHED_BUFFER_MAX) {
                str = str + (i == this.index ? "" : ",") + this.latitude[i] + " " + this.longitude[i];
                i++;
            }
        }
        for (i = 0; i < this.index; i++) {
            str = str + (str.isEmpty() ? "" : ",") + this.latitude[i] + " " + this.longitude[i];
        }
        return str;
    }

    public JSONArray getJsonArray() {
        JSONArray arr = new JSONArray();
        try {
            int i;
            JSONObject o;
            if (this.full) {
                for (i = this.index; i < CACHED_BUFFER_MAX; i++) {
                    o = new JSONObject();
                    o.put(KEY_TIMESTAMP, this.mSdf.format(new Date(this.timestamp[i])));
                    o.put(KEY_LOCATION, String.valueOf(this.longitude[i]) + "," + String.valueOf(this.latitude[i]));
                    o.put(KEY_SPEED, String.valueOf(this.speed[i]));
                    arr.put(o);
                }
            }
            for (i = 0; i < this.index; i++) {
                o = new JSONObject();
                o.put(KEY_TIMESTAMP, this.mSdf.format(new Date(this.timestamp[i])));
                o.put(KEY_LOCATION, String.valueOf(this.longitude[i]) + "," + String.valueOf(this.latitude[i]));
                o.put(KEY_SPEED, String.valueOf(this.speed[i]));
                arr.put(o);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arr;
    }
}
