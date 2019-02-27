package android.media;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaInserter {
    public static final String INSERT_TABLE_URI_KEY = "insert_table_uri_key";
    public static final int MSG_INSERT_ALL = 2;
    public static final int MSG_INSERT_FOLDER = 1;
    public static final int MSG_INSERT_TO_DATABASE = 0;
    public static final int MSG_SCAN_DIRECTORY = 10;
    public static final int MSG_SCAN_FINISH_WITH_THREADPOOL = 13;
    public static final int MSG_SCAN_SINGLE_FILE = 11;
    public static final int MSG_SHUTDOWN_THREADPOOL = 12;
    public static final int MSG_STOP_INSERT = 3;
    private final int mBufferSizePerUri;
    private Handler mInsertHanlder;
    private final HashMap<Uri, List<ContentValues>> mPriorityRowMap = new HashMap();
    private final ContentProviderClient mProvider;
    private final HashMap<Uri, List<ContentValues>> mRowMap = new HashMap();

    public MediaInserter(ContentProviderClient provider, int bufferSizePerUri) {
        this.mProvider = provider;
        this.mBufferSizePerUri = bufferSizePerUri;
    }

    public void insert(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, false);
    }

    public void insertwithPriority(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, true);
    }

    private void insert(Uri tableUri, ContentValues values, boolean priority) throws RemoteException {
        HashMap<Uri, List<ContentValues>> rowmap = priority ? this.mPriorityRowMap : this.mRowMap;
        List<ContentValues> list = (List) rowmap.get(tableUri);
        if (list == null) {
            list = new ArrayList();
            rowmap.put(tableUri, list);
        }
        list.add(new ContentValues(values));
        if (list.size() >= this.mBufferSizePerUri) {
            flushAllPriority();
            flush(tableUri, list);
        }
    }

    public void flushAll() throws RemoteException {
        flushAllPriority();
        for (Uri tableUri : this.mRowMap.keySet()) {
            flush(tableUri, (List) this.mRowMap.get(tableUri));
        }
        this.mRowMap.clear();
    }

    private void flushAllPriority() throws RemoteException {
        for (Uri tableUri : this.mPriorityRowMap.keySet()) {
            flushPriority(tableUri, (List) this.mPriorityRowMap.get(tableUri));
        }
        this.mPriorityRowMap.clear();
    }

    private void flush(Uri tableUri, List<ContentValues> list) throws RemoteException {
        if (!list.isEmpty()) {
            if (this.mProvider != null) {
                this.mProvider.bulkInsert(tableUri, (ContentValues[]) list.toArray(new ContentValues[list.size()]));
                list.clear();
                return;
            }
            ContentValues matchUriValue = new ContentValues(1);
            matchUriValue.put(INSERT_TABLE_URI_KEY, tableUri.toString());
            ArrayList<ContentValues> sendList = new ArrayList(list.size() + 1);
            sendList.add(matchUriValue);
            sendList.addAll(list);
            list.clear();
            this.mInsertHanlder.sendMessage(this.mInsertHanlder.obtainMessage(0, -1, -1, sendList));
        }
    }

    public MediaInserter(Handler inserterHandler, int bufferSizePerUri) {
        this.mInsertHanlder = inserterHandler;
        this.mBufferSizePerUri = bufferSizePerUri;
        this.mProvider = null;
    }

    private void flushPriority(Uri tableUri, List<ContentValues> list) throws RemoteException {
        if (!list.isEmpty()) {
            if (this.mProvider != null) {
                this.mProvider.bulkInsert(tableUri, (ContentValues[]) list.toArray(new ContentValues[list.size()]));
                list.clear();
                return;
            }
            ContentValues matchUriValue = new ContentValues(1);
            matchUriValue.put(INSERT_TABLE_URI_KEY, tableUri.toString());
            ArrayList<ContentValues> sendList = new ArrayList(list.size() + 1);
            sendList.add(matchUriValue);
            sendList.addAll(list);
            list.clear();
            this.mInsertHanlder.sendMessage(this.mInsertHanlder.obtainMessage(0, 1, -1, sendList));
        }
    }
}
