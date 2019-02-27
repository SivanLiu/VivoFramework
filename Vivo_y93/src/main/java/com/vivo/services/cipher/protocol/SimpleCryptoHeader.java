package com.vivo.services.cipher.protocol;

public class SimpleCryptoHeader implements CryptoHeader {
    private CryptoEntry entry;

    public SimpleCryptoHeader(CryptoEntry entry) {
        if (entry == null) {
            throw new NullPointerException("CryptoEntry must not be null");
        }
        this.entry = entry;
    }

    public byte[] getHeaderBytes() {
        return this.entry.getHeaderBytes();
    }

    public int getSupportedProtocolVersion() {
        return this.entry.getSupportedProtocolVersion();
    }

    public String getKeyToken() {
        return this.entry.getKeyToken();
    }

    public int getKeyVersion() {
        return this.entry.getKeyVersion();
    }

    public int getEncryptType() {
        return this.entry.getEncryptType();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SimpleCryptoHeader{");
        sb.append("SupportedProtocolVersion =").append(getSupportedProtocolVersion());
        sb.append(", KeyToken=").append(getKeyToken());
        sb.append(", KeyVersion=").append(getKeyVersion());
        sb.append(", EncryptType=").append(getEncryptType());
        sb.append('}');
        return sb.toString();
    }
}
