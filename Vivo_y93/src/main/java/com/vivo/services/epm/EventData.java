package com.vivo.services.epm;

import android.content.ContentValues;

public class EventData {
    private ContentValues content;
    private boolean isStringMessage;
    private String message;
    private long timestamp;
    private int type;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ContentValues getContent() {
        return this.content;
    }

    public void setContent(ContentValues content) {
        this.content = content;
    }

    public boolean isStringMessage() {
        return this.isStringMessage;
    }

    public String toString() {
        return "EventData{type=" + this.type + ", timestamp=" + this.timestamp + ", message='" + this.message + '\'' + ", content=" + this.content + ", isStringMessage=" + this.isStringMessage + '}';
    }

    public EventData(int type, long timestamp, String message) {
        this.type = type;
        this.timestamp = timestamp;
        this.message = message;
        this.content = null;
        this.isStringMessage = true;
    }

    public EventData(int type, long timestamp, ContentValues content) {
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.message = null;
        this.isStringMessage = false;
    }
}
