package com.vivo.services.epm.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.vivo.common.VivoCloudData;
import com.vivo.services.epm.RuledMap;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LogSystemHelper {
    private static final String EPM_EVENT_ID = "00034|012";
    private static final int EPM_MODULE_ID = 1700;
    private static final int MAX_BUFFER_SIZE = 5242880;
    private static final int MAX_DATA_SIZE = 1048576;
    private static final int MIN_FLUSH_INTERVAL = 660000;
    private static final int MSG_FLUSH = 0;
    private static final String TAG = "EPM";
    private static LogSystemHelper sInstance = null;
    private boolean isFlushing = false;
    private VivoCloudData mCloudDataInstance;
    private Context mContext;
    private int mCurrentSize = 0;
    private List<RuledMap> mPendingFlushLogs;
    private Handler mWorkHandler;
    private HandlerThread mWorkThread;

    private final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LogSystemHelper.this.flush();
                    sendEmptyMessageDelayed(0, 660000);
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized LogSystemHelper init(Context context) {
        LogSystemHelper logSystemHelper;
        synchronized (LogSystemHelper.class) {
            if (sInstance == null) {
                sInstance = new LogSystemHelper(context);
            }
            logSystemHelper = sInstance;
        }
        return logSystemHelper;
    }

    public static synchronized LogSystemHelper getInstance() {
        LogSystemHelper logSystemHelper;
        synchronized (LogSystemHelper.class) {
            logSystemHelper = sInstance;
        }
        return logSystemHelper;
    }

    private LogSystemHelper(Context context) {
        this.mContext = context;
        this.mCloudDataInstance = VivoCloudData.getInstance(context);
        this.mWorkThread = new HandlerThread("EPM_LOG_WRITER");
        this.mWorkThread.start();
        this.mWorkHandler = new MainHandler(this.mWorkThread.getLooper());
        this.mPendingFlushLogs = new ArrayList();
    }

    public synchronized boolean addLog(RuledMap log) {
        if (log != null) {
            if (log.isValid()) {
                synchronized (this) {
                    if (this.mCurrentSize + log.size() < MAX_BUFFER_SIZE) {
                        this.mPendingFlushLogs.add(log);
                        this.mCurrentSize += log.size();
                        return true;
                    }
                    Log.d(TAG, "the log buffer is full, discard the log " + log.toJSONObject());
                    return false;
                }
            }
        }
        Log.d(TAG, "log is valid");
        return false;
    }

    private List<RuledMap> pickupDataSendByOnce() {
        int total = 0;
        List<RuledMap> ret = new ArrayList();
        synchronized (this) {
            int size = this.mPendingFlushLogs.size();
            for (int i = 0; i < size; i++) {
                total += ((RuledMap) this.mPendingFlushLogs.get(i)).size();
                if (total > MAX_DATA_SIZE) {
                    break;
                }
                ret.add((RuledMap) this.mPendingFlushLogs.get(i));
            }
            this.mCurrentSize -= total;
            for (RuledMap map : ret) {
                this.mPendingFlushLogs.remove(map);
            }
        }
        return ret;
    }

    private void flush() {
        if (Utils.isWifi(this.mContext)) {
            this.isFlushing = true;
            writeData(pickupDataSendByOnce());
            return;
        }
        Log.d(TAG, "not wifi , delay flush");
    }

    public void dump(PrintWriter pw) {
        pw.println("pending flush logs:");
        synchronized (this) {
            pw.println("mCurrentPendingFlushSize=" + this.mCurrentSize);
            for (RuledMap rm : this.mPendingFlushLogs) {
                pw.println("***********************************************************************");
                pw.println(rm.toJSONObject());
                pw.println("***********************************************************************");
            }
        }
    }

    public void startFlushData() {
        if (this.mWorkHandler != null) {
            this.mWorkHandler.sendEmptyMessage(0);
        }
    }

    private void writeData(List<RuledMap> records) {
        if (records != null && records.size() > 0) {
            ArrayList<String> data = new ArrayList();
            for (RuledMap record : records) {
                data.add(VivoCloudData.initOneData(EPM_EVENT_ID, record.toJSONObject()));
            }
            Log.d(TAG, "writeData data=" + data);
            if (this.mCloudDataInstance != null && data.size() > 0) {
                this.mCloudDataInstance.sendData(EPM_MODULE_ID, data);
            }
        }
    }

    private boolean checkDataSizeValid(List<RuledMap> records) {
        int size = 0;
        for (RuledMap r : records) {
            size += r.size();
        }
        return size < MAX_DATA_SIZE;
    }
}
