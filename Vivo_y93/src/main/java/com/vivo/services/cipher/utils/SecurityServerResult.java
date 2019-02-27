package com.vivo.services.cipher.utils;

import android.security.keymaster.SecurityKeyException;

public class SecurityServerResult {
    private byte[] mData;
    private SecurityKeyException mException;
    private boolean mResult;

    public SecurityServerResult(boolean result, byte[] data, SecurityKeyException e) {
        this.mResult = result;
        this.mData = data;
        this.mException = e;
    }

    public void setResult(boolean result) {
        this.mResult = result;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    public void setException(SecurityKeyException e) {
        this.mException = e;
    }

    public boolean getResult() {
        return this.mResult;
    }

    public byte[] getData() {
        return this.mData;
    }

    public SecurityKeyException getException() {
        return this.mException;
    }
}
