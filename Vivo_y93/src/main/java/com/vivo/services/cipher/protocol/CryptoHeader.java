package com.vivo.services.cipher.protocol;

public interface CryptoHeader {
    int getEncryptType();

    byte[] getHeaderBytes();

    String getKeyToken();

    int getKeyVersion();

    int getSupportedProtocolVersion();
}
