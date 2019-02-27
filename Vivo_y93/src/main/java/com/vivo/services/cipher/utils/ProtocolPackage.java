package com.vivo.services.cipher.utils;

import android.security.keymaster.SecurityKeyException;
import android.text.TextUtils;
import com.vivo.services.cipher.protocol.CryptoEntry;
import com.vivo.services.cipher.protocol.CryptoEntryFactory;
import com.vivo.services.cipher.protocol.CryptoHeader;

public class ProtocolPackage {
    private byte[] mData;
    private int mKeyVersion;
    private String mToken;
    private int mType;

    public ProtocolPackage(String token, int keyVersion, int type, byte[] data) {
        this.mKeyVersion = keyVersion;
        this.mType = type;
        this.mData = data;
        this.mToken = token;
    }

    public int getKeyVersion() {
        return this.mKeyVersion;
    }

    public void setKeyVersion(int keyVersion) {
        this.mKeyVersion = keyVersion;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public byte[] getData() {
        return this.mData;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    public String getToken() {
        return this.mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("package keyVersion " + this.mKeyVersion + ",");
        sb.append("package token " + this.mToken + ",");
        sb.append("package type " + this.mType + ",");
        sb.append("package data len= " + this.mData.length + ",");
        return sb.toString();
    }

    public byte[] getbytes() {
        CryptoEntry entry = CryptoEntryFactory.getEntry(1, false);
        entry.setKeyVersion(this.mKeyVersion);
        entry.setEncryptType(this.mType);
        entry.setBody(this.mData);
        entry.setKeyToken(this.mToken);
        entry.render();
        return entry.getEntryBytes();
    }

    public byte[] getHeaderbytes() {
        CryptoEntry entry = CryptoEntryFactory.getEntry(1, false);
        entry.setKeyVersion(this.mKeyVersion);
        entry.setEncryptType(this.mType);
        entry.setKeyToken(this.mToken);
        entry.render();
        return entry.getHeaderBytes();
    }

    public static ProtocolPackage buildProtocolPackage(SecurityKeyConfigure configure, byte[] prococalData) throws SecurityKeyException {
        CryptoEntry entry = CryptoEntryFactory.getEntry(prococalData);
        CryptoHeader header = entry.getHeader();
        if (header == null) {
            VLog.e(Contants.TAG, configure, "buildProtocolPackage head is null!");
            throw new SecurityKeyException(Contants.ERROR_CRYPTO_HEADER, 150);
        }
        String packageName = header.getKeyToken();
        if (TextUtils.isEmpty(packageName)) {
            VLog.e(Contants.TAG, configure, "buildProtocolPackage packageName is empty!");
            throw new SecurityKeyException(Contants.ERROR_CRYPTO_HEADER, 150);
        }
        byte[] bodyData = entry.getBody();
        if (bodyData != null) {
            return new ProtocolPackage(packageName, header.getKeyVersion(), header.getEncryptType(), bodyData);
        }
        VLog.e(Contants.TAG, configure, "buildProtocolPackage body is null!");
        throw new SecurityKeyException(Contants.ERROR_CRYPTO_BODY, 151);
    }

    public int getCipherMode() {
        if (this.mToken.contains(Contants.SO_ENCRYPT_PRE_PACKAGE)) {
            return 3;
        }
        return 2;
    }
}
