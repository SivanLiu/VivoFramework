package com.vivo.services.cipher.protocol;

import android.text.format.DateFormat;
import java.util.Arrays;
import java.util.zip.CRC32;

public abstract class AbstractCryptoEntry implements CryptoEntry {
    private byte[] body;
    private int encryptType;
    private byte[] entryBytes;
    private byte[] headerBytes;
    private String keyToken;
    private int keyVersion;
    private boolean needVerifyHeader;

    protected abstract void doParse();

    public AbstractCryptoEntry() {
        this.needVerifyHeader = true;
    }

    public AbstractCryptoEntry(byte[] entry) {
        this(entry, true);
    }

    public AbstractCryptoEntry(boolean needVerifyHeader) {
        this.needVerifyHeader = true;
        this.needVerifyHeader = needVerifyHeader;
    }

    public AbstractCryptoEntry(byte[] entry, boolean needVerifyHeader) {
        this.needVerifyHeader = true;
        if (entry == null || entry.length == 0) {
            throw new IllegalArgumentException("Entry body must not be empty");
        }
        this.entryBytes = entry;
        this.needVerifyHeader = needVerifyHeader;
        parse();
    }

    public byte[] getHeaderBytes() {
        return this.headerBytes;
    }

    public byte[] getEntryBytes() {
        return this.entryBytes;
    }

    public boolean verifyHeader() {
        if (!this.needVerifyHeader) {
            return true;
        }
        if (this.headerBytes == null) {
            throw new CryptoEntryParseException("The haeder bytes must not be empty");
        }
        byte[] checkData = new byte[8];
        byte[] headerToCheck = new byte[(this.headerBytes.length - 10)];
        System.arraycopy(this.headerBytes, 2, checkData, 0, 8);
        System.arraycopy(this.headerBytes, 10, headerToCheck, 0, this.headerBytes.length - 10);
        long checkSum = NumericUtils.bytesToLong(checkData);
        CRC32 crc32 = new CRC32();
        crc32.update(headerToCheck);
        long value = crc32.getValue();
        if (checkSum == value) {
            return true;
        }
        throw new IllegalArgumentException("头数据校验不成功，头部校验和为：" + checkSum + ",计算校验和为：" + value);
    }

    protected void parse() {
        int headerLen = parseHeaderLength();
        if (this.entryBytes.length > headerLen) {
            this.headerBytes = new byte[headerLen];
            System.arraycopy(this.entryBytes, 0, this.headerBytes, 0, headerLen);
            this.body = new byte[(this.entryBytes.length - headerLen)];
            System.arraycopy(this.entryBytes, headerLen, this.body, 0, this.entryBytes.length - headerLen);
        } else {
            this.headerBytes = this.entryBytes;
        }
        verifyHeader();
        doParse();
    }

    public CryptoHeader getHeader() {
        return new SimpleCryptoHeader(this);
    }

    protected int parseHeaderLength() {
        byte[] headerLength = new byte[2];
        System.arraycopy(this.entryBytes, 0, headerLength, 0, 2);
        int hl = NumericUtils.bytesToInt(headerLength);
        if (hl <= 0) {
            throw new CryptoEntryParseException("Illegal header length:" + hl);
        } else if (this.entryBytes.length >= hl) {
            return hl;
        } else {
            throw new CryptoEntryParseException("Header length great than entry length,entry length:" + this.entryBytes.length + ",header length:" + hl);
        }
    }

    protected int parseProtocolVersion() {
        byte[] pv = new byte[2];
        System.arraycopy(this.entryBytes, 10, pv, 0, 2);
        return NumericUtils.bytesToInt(pv);
    }

    public int getEncryptType() {
        return this.encryptType;
    }

    public int getKeyVersion() {
        return this.keyVersion;
    }

    public String getKeyToken() {
        return this.keyToken;
    }

    public void setEncryptType(int type) {
        this.encryptType = type;
    }

    public void setKeyVersion(int version) {
        this.keyVersion = version;
    }

    public void setKeyToken(String token) {
        this.keyToken = token;
    }

    public byte[] getSignature() {
        return this.body;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setBody(byte[] data) {
        this.body = data;
    }

    public void setSignature(byte[] signature) {
        this.body = signature;
    }

    protected void setHeaderBytes(byte[] headerBytes) {
        this.headerBytes = headerBytes;
    }

    protected void setEntryBytes(byte[] entryBytes) {
        this.entryBytes = entryBytes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AbstractCryptoEntry{");
        sb.append("needVerifyHeader=").append(this.needVerifyHeader);
        sb.append(", encryptType=").append(this.encryptType);
        sb.append(", keyVersion=").append(this.keyVersion);
        sb.append(", keyToken='").append(this.keyToken).append(DateFormat.QUOTE);
        sb.append(", body=").append(Arrays.toString(this.body));
        sb.append('}');
        return sb.toString();
    }
}
