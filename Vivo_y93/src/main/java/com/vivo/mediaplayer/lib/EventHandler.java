package com.vivo.mediaplayer.lib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;

public class EventHandler {
    public static final int MediaListItemAdded = 512;
    public static final int MediaListItemDeleted = 514;
    public static final int MediaPlayerEncounteredError = 266;
    public static final int MediaPlayerEndReached = 265;
    public static final int MediaPlayerPaused = 261;
    public static final int MediaPlayerPlaying = 260;
    public static final int MediaPlayerPositionChanged = 268;
    public static final int MediaPlayerStopped = 262;
    public static final int MediaPlayerVout = 274;
    private static EventHandler mInstance;
    private ArrayList<Handler> mEventHandler = new ArrayList();

    private EventHandler() {
    }

    public static EventHandler getInstance() {
        if (mInstance == null) {
            mInstance = new EventHandler();
        }
        return mInstance;
    }

    public void addHandler(Handler handler) {
        if (!this.mEventHandler.contains(handler)) {
            this.mEventHandler.add(handler);
        }
    }

    public void removeHandler(Handler handler) {
        this.mEventHandler.remove(handler);
    }

    public void callback(int event, Bundle b) {
        b.putInt("event", event);
        for (int i = 0; i < this.mEventHandler.size(); i++) {
            Message msg = Message.obtain();
            msg.setData(b);
            ((Handler) this.mEventHandler.get(i)).sendMessage(msg);
        }
    }
}
