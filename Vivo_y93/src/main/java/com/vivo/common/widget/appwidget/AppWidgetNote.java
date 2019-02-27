package com.vivo.common.widget.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import com.vivo.common.provider.Weather.CurrentCity;
import java.util.ArrayList;

public class AppWidgetNote {
    private static final String ACTIVITY = "com.android.notes.EditWidget";
    private static final String ADD_ACTION = "WIDGET_ADD_NOTE";
    private static final int CLEAR_APPDATA_UPDATE = 4097;
    private static final String CLEAR_APPNOTIFYDATA_ACTION = "android.intent.action.CLEAR_PACKAGE_DATA.com.android.notes";
    private static final Uri CONTENT_URI = Uri.parse("content://com.provider.notes/note");
    private static final int FIRST_INITAL_UPDATE = 4101;
    private static final String PACKAGE = "com.android.notes";
    private static final int SELF_DEL_NOTE_UPDATE = 4096;
    private static final int SYSTEM_TIME_CHANGE_UPDATE = 4098;
    private static final int SYSTEM_TIME_ZONE_CHANGE_UPDATE = 4099;
    private static final int SYSTEM_WIDGET_UPDATE = 4100;
    private static final String TAG = "AppWidgetNote";
    private static final int TASK_RUNNING = 0;
    private static final int TASK_STOPED = 1;
    private static final String VIEW_ACTION = "WIDGET_VIEW_NOTE";
    private static final String WIDGET_UPDATE_ACTION = "android.widget.action.WIDGET_UPDATE";
    private static final int _DATE = 2;
    private static final int _ID = 0;
    private static final int _TEXT = 3;
    private Context mContext;
    private NoteReceiver mNoteReceiver = new NoteReceiver();
    private NoteUpdateListener mNoteUpdateListener = null;
    private NotesInfo mNotesInfo = null;
    private NoteQueryTask mQueryTask = null;
    private int mQueryTaskStatues = 1;

    public class Note {
        public String mDate;
        public long mDateLong;
        public String mText;

        public Note(Note note) {
            this.mText = note.mText;
            this.mDateLong = note.mDateLong;
            this.mDate = DateUtils.formatDateTime(AppWidgetNote.this.mContext, this.mDateLong, 16);
        }

        public Note(String text, long date) {
            this.mText = text;
            this.mDateLong = date;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Note)) {
                return false;
            }
            Note other = (Note) o;
            if (this.mText.equals(other.mText) && this.mDateLong == other.mDateLong) {
                z = true;
            }
            return z;
        }
    }

    public class NoteDelTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... params) {
            int index = params[0].intValue();
            Cursor cur = AppWidgetNote.this.mContext.getContentResolver().query(AppWidgetNote.CONTENT_URI, null, null, null, null);
            if (cur != null) {
                cur.moveToPosition(index);
                AppWidgetNote.this.mContext.getContentResolver().delete(Uri.parse(AppWidgetNote.CONTENT_URI + "/" + cur.getString(0)), null, null);
                cur.close();
                return Integer.valueOf(index);
            }
            AppWidgetNote.this.Log("del note failed! the cursor return null");
            return null;
        }

        protected void onPostExecute(Integer result) {
            if (result != null) {
                AppWidgetNote.this.mNotesInfo.delNote(result.intValue());
                AppWidgetNote.this.trigerNoteUpdate(4096);
            }
            super.onPostExecute(result);
        }
    }

    public class NoteQueryTask extends AsyncTask<Integer, Integer, NotesInfo> {
        private int mAction;

        protected NotesInfo doInBackground(Integer... params) {
            this.mAction = params[0].intValue();
            NotesInfo notesInfo = new NotesInfo();
            Cursor cur = AppWidgetNote.this.mContext.getContentResolver().query(AppWidgetNote.CONTENT_URI, null, null, null, null);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        notesInfo.addNote(cur.getString(3), cur.getLong(2));
                    } while (cur.moveToNext());
                }
                cur.close();
                return notesInfo;
            }
            AppWidgetNote.this.Log("query noteinfo failed! the cursor return null");
            return null;
        }

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onCancelled(NotesInfo result) {
            AppWidgetNote.this.mQueryTaskStatues = 1;
            AppWidgetNote.this.Log("the query task is cancled!");
        }

        protected void onPreExecute() {
            AppWidgetNote.this.mQueryTaskStatues = 0;
            super.onPreExecute();
        }

        protected void onPostExecute(NotesInfo result) {
            if (needUpdate(result)) {
                AppWidgetNote.this.mNotesInfo = null;
                AppWidgetNote.this.mNotesInfo = result;
                AppWidgetNote.this.trigerNoteUpdate(this.mAction);
            } else {
                AppWidgetNote.this.Log("the data is the same,no need update!");
            }
            super.onPostExecute(result);
            AppWidgetNote.this.mQueryTaskStatues = 1;
        }

        private boolean needUpdate(NotesInfo newNotesInfo) {
            if (AppWidgetNote.this.mNotesInfo == null) {
                return true;
            }
            return AppWidgetNote.this.mNotesInfo.equals(newNotesInfo) ^ 1;
        }
    }

    public class NoteReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AppWidgetNote.this.Log("action:" + action + "   mQueryTaskStatues:" + AppWidgetNote.this.mQueryTaskStatues);
            if (action.equals(AppWidgetNote.CLEAR_APPNOTIFYDATA_ACTION)) {
                if (AppWidgetNote.this.mNotesInfo != null) {
                    AppWidgetNote.this.mNotesInfo.clear();
                    AppWidgetNote.this.trigerNoteUpdate(4097);
                }
            } else if (action.equals(AppWidgetNote.WIDGET_UPDATE_ACTION)) {
                AppWidgetNote.this.updateNotesData(AppWidgetNote.SYSTEM_WIDGET_UPDATE);
            } else if (action.equals("android.intent.action.TIME_SET")) {
                if (AppWidgetNote.this.mNotesInfo != null && AppWidgetNote.this.mQueryTaskStatues == 1) {
                    AppWidgetNote.this.trigerNoteUpdate(4098);
                }
            } else if (action.equals("android.intent.action.TIMEZONE_CHANGED") && AppWidgetNote.this.mNotesInfo != null && AppWidgetNote.this.mQueryTaskStatues == 1) {
                AppWidgetNote.this.trigerNoteUpdate(4099);
            }
        }
    }

    public interface NoteUpdateListener {
        void onNoteUpdate(int i, NotesInfo notesInfo);
    }

    public class NotesInfo {
        private ArrayList<Note> mNotesList = new ArrayList();

        private void addNote(String text, long date) {
            this.mNotesList.add(new Note(text, date));
        }

        private void delNote(int index) {
            this.mNotesList.remove(index);
        }

        private void clear() {
            this.mNotesList.clear();
        }

        public int getNotesCount() {
            return this.mNotesList.size();
        }

        public Note getNote(int index) {
            if (index >= this.mNotesList.size()) {
                return null;
            }
            return new Note((Note) this.mNotesList.get(index));
        }

        public boolean equals(Object o) {
            if (!(o instanceof NotesInfo)) {
                return false;
            }
            NotesInfo other = (NotesInfo) o;
            if (other.getNotesCount() == getNotesCount()) {
                int size = getNotesCount();
                int i = 0;
                while (i < size && getNote(i).equals(other.getNote(i))) {
                    i++;
                }
                if (i >= size) {
                    return true;
                }
            }
            return false;
        }
    }

    private void Log(String str) {
        Log.d(TAG, str);
    }

    public AppWidgetNote(Context context) {
        this.mContext = context;
        updateNotesData(FIRST_INITAL_UPDATE);
    }

    private void updateNotesData(int action) {
        if (this.mQueryTaskStatues == 0) {
            if (this.mQueryTask != null) {
                this.mQueryTask.cancel(true);
            }
            Log("cacle the query task!");
        }
        this.mQueryTask = new NoteQueryTask();
        this.mQueryTask.execute(new Integer[]{Integer.valueOf(action)});
    }

    public void moniterAppChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CLEAR_APPNOTIFYDATA_ACTION);
        filter.addAction(WIDGET_UPDATE_ACTION);
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this.mNoteReceiver, filter);
    }

    public void unMoniterNoteAppChange() {
        this.mContext.unregisterReceiver(this.mNoteReceiver);
    }

    public void addNote() {
        Intent addIntent = new Intent();
        addIntent.setAction(ADD_ACTION);
        addIntent.setClassName(PACKAGE, ACTIVITY);
        addIntent.setFlags(335544320);
        this.mContext.startActivity(addIntent);
    }

    public void delNote(int index) {
        if (this.mNotesInfo == null) {
            Log("the notes data is not ready!");
        } else if (index >= this.mNotesInfo.getNotesCount() || index < 0) {
            Log("the open note index is invalid,value : " + index + "  the notes size : " + this.mNotesInfo.getNotesCount());
        } else {
            new NoteDelTask().execute(new Integer[]{Integer.valueOf(index)});
        }
    }

    public void viewNote(int index) {
        if (this.mNotesInfo == null) {
            Log("the notes data is not ready!");
        } else if (index >= this.mNotesInfo.getNotesCount() || index < 0) {
            Log("the open note index is invalid,value : " + index + "  the notes size : " + this.mNotesInfo.getNotesCount());
        } else {
            Intent viewIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CurrentCity.POSITION, index);
            viewIntent.setAction(VIEW_ACTION);
            viewIntent.putExtras(bundle);
            viewIntent.setClassName(PACKAGE, ACTIVITY);
            viewIntent.setFlags(335544320);
            this.mContext.startActivity(viewIntent);
        }
    }

    public void setOnNoteUpdateListener(NoteUpdateListener listener) {
        this.mNoteUpdateListener = listener;
    }

    private void trigerNoteUpdate(int action) {
        Log("-----trigerNoteUpdate----");
        if (this.mNoteUpdateListener != null) {
            this.mNoteUpdateListener.onNoteUpdate(action, this.mNotesInfo);
        }
    }
}
