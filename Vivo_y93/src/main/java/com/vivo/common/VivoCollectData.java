package com.vivo.common;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class VivoCollectData {
    private static final String KEY_ANALYSIS_DATE = "analysisdate";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_EVENT_ID = "event_id";
    private static final String KEY_EVENT_STATUS = "event_status";
    private static final String KEY_EVENT_VALUE = "event_value";
    private static final String KEY_LABEL = "event_label";
    private static final String KEY_LUNCH_COUNT = "lunchcount";
    private static final String KEY_PARAM_NAME = "param_name";
    private static final String KEY_PARAM_VALUE = "param_value";
    private static final String KEY_START_TIME = "start_time";
    private static final String TAG = ToolUtils.makeTag("CollectData");
    private static final ExecutorService THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, 128, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(10), new DiscardOldestPolicy());
    private static final Uri URI_APP = Uri.parse("content://com.bbk.iqoo.logsystemes/aaa");
    private static final Uri URI_CTRL_SWITCH = Uri.parse("content://com.bbk.iqoo.logsystemes/eic");
    private static final Uri URI_SETTING = Uri.parse("content://com.bbk.iqoo.logsystemes/sss");
    private static VivoCollectData sInstance;
    private boolean isDebug;
    private VectorList<ContentValues> mAppDatas = new VectorList(100);
    private Context mContext;
    private Looper mLooper;
    private String mPackageName;
    private BroadcastReceiver mReceiver;
    private ContentResolver mResolver;
    private VectorList<ContentValues> mSettingDatas = new VectorList(100);
    private final int mVersion = 6;

    public interface IResponed {
        void onChange();

        void onChange(boolean z);
    }

    public static class VectorList<E> extends ArrayList<E> {
        public VectorList(int capacity) {
            super(capacity);
        }

        public synchronized boolean add(E e) {
            return super.add(e);
        }

        public synchronized ArrayList<E> cloneAndClear() {
            ArrayList<E> list;
            list = new ArrayList(this);
            super.clear();
            return list;
        }

        public synchronized int size() {
            return super.size();
        }
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Deprecated
    public VivoCollectData(Context context) {
        this.mContext = context.getApplicationContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mPackageName = this.mContext.getPackageName();
    }

    public static VivoCollectData getInstance(Context context) {
        if (sInstance == null) {
            synchronized (VivoCollectData.class) {
                if (sInstance == null) {
                    sInstance = new VivoCollectData(context);
                }
            }
        }
        return sInstance;
    }

    public int getVersion() {
        return 6;
    }

    @Deprecated
    public int getCollectDataVersion() {
        return 6;
    }

    public boolean getControlInfo(String eventId) {
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(URI_CTRL_SWITCH, new String[]{KEY_EVENT_STATUS}, "event_id=?", new String[]{eventId}, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
            boolean equals = "0".equals(cursor.getString(0)) ^ 1;
            if (cursor != null) {
                cursor.close();
            }
            return equals;
        } catch (Exception e) {
            if (this.isDebug) {
                Log.d(TAG, "Query data failed, or failed to read the value of the switch!", e);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void writeData(String eventId, String label, long startTime, long endTime, long duration, int lunchCount, HashMap<String, String> params) {
        String event = eventId;
        String lab = label;
        long start = startTime;
        long end = endTime;
        long dur = duration;
        int count = lunchCount;
        HashMap<String, String> p = params;
        final String str = eventId;
        final String str2 = label;
        final long j = startTime;
        final long j2 = endTime;
        final long j3 = duration;
        final int i = lunchCount;
        final HashMap<String, String> hashMap = params;
        THREAD_POOL_EXECUTOR.submit(new Runnable() {
            public void run() {
                try {
                    VivoCollectData.this.mResolver.insert(VivoCollectData.URI_APP, VivoCollectData.this.encapsulateData(str, str2, j, j2, j3, i, hashMap));
                } catch (Exception e) {
                    if (VivoCollectData.this.isDebug) {
                        Log.d(VivoCollectData.TAG, "save one data to app table failed.", e);
                    }
                }
            }
        });
    }

    public void writeData(String eventId, String label, long startTime, long endTime, long duration, int lunchCount, HashMap<String, String> params, boolean immediately) {
        if (immediately) {
            writeData(eventId, label, startTime, endTime, duration, lunchCount, params);
            return;
        }
        this.mAppDatas.add(encapsulateData(eventId, label, startTime, endTime, duration, lunchCount, params));
        if (this.mAppDatas.size() >= 100) {
            flush(URI_APP, this.mAppDatas.cloneAndClear());
        }
    }

    public void writeData(final String eventId, final String key, final String value) {
        String event = eventId;
        String k = key;
        String v = value;
        THREAD_POOL_EXECUTOR.submit(new Runnable() {
            public void run() {
                try {
                    VivoCollectData.this.mResolver.insert(VivoCollectData.URI_SETTING, VivoCollectData.this.encapsulateData(eventId, key, value));
                } catch (Exception e) {
                    if (VivoCollectData.this.isDebug) {
                        Log.d(VivoCollectData.TAG, "save one data to setting table failed.", e);
                    }
                }
            }
        });
    }

    public void writeData(String eventId, String key, String value, boolean immediately) {
        if (immediately) {
            writeData(eventId, key, value);
            return;
        }
        this.mSettingDatas.add(encapsulateData(eventId, key, value));
        if (this.mSettingDatas.size() >= 100) {
            flush(URI_SETTING, this.mSettingDatas.cloneAndClear());
        }
    }

    public void flush() {
        if (this.mAppDatas.size() > 0) {
            flush(URI_APP, this.mAppDatas.cloneAndClear());
        }
        if (this.mSettingDatas.size() > 0) {
            flush(URI_SETTING, this.mSettingDatas.cloneAndClear());
        }
    }

    private void flush(final Uri url, final ArrayList<ContentValues> data) {
        THREAD_POOL_EXECUTOR.submit(new Runnable() {
            public void run() {
                if (!ToolUtils.isEmpty(data)) {
                    try {
                        VivoCollectData.this.mResolver.bulkInsert(url, (ContentValues[]) data.toArray(new ContentValues[data.size()]));
                    } catch (Exception e) {
                        if (VivoCollectData.this.isDebug) {
                            Log.d(VivoCollectData.TAG, "Save some data to app table failed.", e);
                        }
                    }
                }
            }
        });
    }

    private ContentValues encapsulateData(String eventId, String label, long startTime, long endTime, long duration, int lunchCount, HashMap<String, String> params) {
        ContentValues data = new ContentValues();
        data.put("event_id", eventId);
        data.put(KEY_LABEL, label);
        data.put(KEY_START_TIME, Long.valueOf(startTime));
        data.put(KEY_END_TIME, Long.valueOf(endTime));
        data.put("duration", Long.valueOf(duration));
        data.put(KEY_LUNCH_COUNT, Integer.valueOf(lunchCount));
        data.put(KEY_ANALYSIS_DATE, Long.valueOf(System.currentTimeMillis()));
        if (!ToolUtils.isEmpty((Map) params)) {
            data.put(KEY_EVENT_VALUE, createJsonStr(params));
        }
        return data;
    }

    private ContentValues encapsulateData(String eventId, String key, String value) {
        ContentValues data = new ContentValues();
        data.put("event_id", eventId);
        data.put(KEY_PARAM_NAME, key);
        data.put(KEY_PARAM_VALUE, value);
        data.put(KEY_ANALYSIS_DATE, Long.valueOf(System.currentTimeMillis()));
        return data;
    }

    private String createJsonStr(HashMap<String, String> params) {
        try {
            JSONObject jo = new JSONObject(params);
            if (!ToolUtils.isEmpty(this.mPackageName)) {
                jo.put("log_from", this.mPackageName);
            }
            return jo.toString();
        } catch (Exception e) {
            Log.w(TAG, "Assemble json data failed.", e);
            return "";
        }
    }

    public void registerReceiver(final IResponed rp) {
        if (rp == null) {
            throw new NullPointerException("The rp is null!");
        }
        HandlerThread ht = new HandlerThread("DBObserver[Thread]");
        ht.start();
        this.mLooper = ht.getLooper();
        Handler handler = new Handler(this.mLooper);
        IntentFilter ift = new IntentFilter("com.vivo.intent.action.logsystem.USER_BEHAVIOR_SWITCH_CHANGE");
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (VivoCollectData.this.isDebug) {
                    Log.d(VivoCollectData.TAG, "onReceive: intent = " + intent.getAction());
                }
                rp.onChange();
            }
        };
        this.mContext.registerReceiver(this.mReceiver, ift, "com.bbk.iqoo.logsystem.permission.SIGN_OR_SYSTEM", handler);
    }

    public void registerReceiver(final IResponed rp, final String eventId) {
        if (rp == null) {
            throw new NullPointerException("The rp is null!");
        } else if (ToolUtils.isEmpty(eventId)) {
            throw new NullPointerException("The name is null!");
        } else {
            HandlerThread ht = new HandlerThread("DBObserver[Thread]");
            ht.start();
            this.mLooper = ht.getLooper();
            Handler handler = new Handler(this.mLooper);
            IntentFilter ift = new IntentFilter("com.vivo.intent.action.logsystem.USER_BEHAVIOR_SWITCH_CHANGE");
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (VivoCollectData.this.isDebug) {
                        Log.d(VivoCollectData.TAG, "onReceive: intent = " + intent.getAction());
                    }
                    boolean isOpen = VivoCollectData.this.getControlInfo(eventId);
                    if (VivoCollectData.this.isDebug) {
                        Log.d(VivoCollectData.TAG, "onReceive: isOpen = " + isOpen);
                    }
                    rp.onChange(isOpen);
                }
            };
            this.mContext.registerReceiver(this.mReceiver, ift, "com.bbk.iqoo.logsystem.permission.SIGN_OR_SYSTEM", handler);
        }
    }

    public void unregisterReceiver() {
        if (this.mReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
            } catch (Throwable e) {
                Log.w(TAG, e);
            }
            this.mReceiver = null;
        }
        if (this.mLooper != null) {
            this.mLooper.quit();
        }
    }
}
