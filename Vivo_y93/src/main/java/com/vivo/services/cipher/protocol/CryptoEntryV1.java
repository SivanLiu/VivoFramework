package com.vivo.services.cipher.protocol;

import java.util.zip.CRC32;

public class CryptoEntryV1 extends AbstractCryptoEntry {
    public static final int SUPPORTED_PROTOCOL_VERSION = 1;

    public CryptoEntryV1(boolean needVerifyHeader) {
        super(needVerifyHeader);
    }

    public CryptoEntryV1(byte[] header) {
        super(header);
    }

    public CryptoEntryV1(byte[] header, boolean needVerifyData) {
        super(header, needVerifyData);
    }

    public int getSupportedProtocolVersion() {
        return 1;
    }

    public byte[] render() {
        if (getEntryBytes() != null) {
            return getEntryBytes();
        }
        if (StringUtils.hasText(getKeyToken())) {
            byte[] result;
            byte[] protocolVersion = NumericUtils.shortToBytes((short) getSupportedProtocolVersion());
            byte[] keytTokenBytes = getKeyToken().getBytes(STRING_CHARSET);
            byte[] keyTokenLen = new byte[]{(byte) keytTokenBytes.length};
            byte[] keyVersion = NumericUtils.shortToBytes((short) getKeyVersion());
            byte[] encryptionType = new byte[]{(byte) getEncryptType()};
            int headLen = ((((protocolVersion.length + 10) + keyTokenLen.length) + keytTokenBytes.length) + keyVersion.length) + encryptionType.length;
            byte[] header = new byte[headLen];
            System.arraycopy(NumericUtils.shortToBytes((short) headLen), 0, header, 0, 2);
            System.arraycopy(protocolVersion, 0, header, 10, 2);
            System.arraycopy(keyTokenLen, 0, header, 12, 1);
            System.arraycopy(keytTokenBytes, 0, header, 13, keytTokenBytes.length);
            System.arraycopy(keyVersion, 0, header, keytTokenBytes.length + 13, 2);
            System.arraycopy(encryptionType, 0, header, keytTokenBytes.length + 15, 1);
            byte[] dataToCheck = new byte[(header.length - 10)];
            System.arraycopy(header, 10, dataToCheck, 0, header.length - 10);
            CRC32 crc32 = new CRC32();
            crc32.update(dataToCheck);
            System.arraycopy(NumericUtils.longToBytes(crc32.getValue()), 0, header, 2, 8);
            setHeaderBytes(header);
            byte[] bodyBytes = getBody();
            if (bodyBytes != null) {
                byte[] entry = new byte[(header.length + bodyBytes.length)];
                System.arraycopy(header, 0, entry, 0, header.length);
                System.arraycopy(bodyBytes, 0, entry, header.length, bodyBytes.length);
                result = entry;
            } else {
                result = header;
            }
            setEntryBytes(result);
            return result;
        }
        throw new IllegalArgumentException("keyToken must have value");
    }

    protected void doParse() {
        int protocolVersion = parseProtocolVersion();
        if (protocolVersion != getSupportedProtocolVersion()) {
            throw new CryptoEntryParseException("Unsupported version of:" + protocolVersion + " for this Header + " + getClass());
        }
        byte[] header = getHeaderBytes();
        Integer ktLen = Integer.valueOf(NumericUtils.bytesToInt(new byte[]{header[12]}));
        byte[] kt = new byte[ktLen.intValue()];
        System.arraycopy(header, 13, kt, 0, ktLen.intValue());
        setKeyToken(new String(kt, STRING_CHARSET));
        byte[] kv = new byte[2];
        System.arraycopy(header, ktLen.intValue() + 13, kv, 0, 2);
        setKeyVersion(NumericUtils.bytesToInt(kv));
        byte[] et = new byte[1];
        System.arraycopy(header, ktLen.intValue() + 15, et, 0, 1);
        setEncryptType(NumericUtils.bytesToInt(et));
    }
}
