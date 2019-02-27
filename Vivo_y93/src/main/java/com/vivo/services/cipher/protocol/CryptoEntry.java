package com.vivo.services.cipher.protocol;

import com.vivo.services.cipher.utils.Contants;
import java.nio.charset.Charset;

public interface CryptoEntry {
    public static final Charset STRING_CHARSET = Charset.forName(Contants.ENCODE_MODE);

    byte[] getBody();

    int getEncryptType();

    byte[] getEntryBytes();

    CryptoHeader getHeader();

    byte[] getHeaderBytes();

    String getKeyToken();

    int getKeyVersion();

    byte[] getSignature();

    int getSupportedProtocolVersion();

    byte[] render();

    void setBody(byte[] bArr);

    void setEncryptType(int i);

    void setKeyToken(String str);

    void setKeyVersion(int i);

    void setSignature(byte[] bArr);

    boolean verifyHeader();
}
