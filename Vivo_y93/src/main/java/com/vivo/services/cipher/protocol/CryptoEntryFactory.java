package com.vivo.services.cipher.protocol;

public class CryptoEntryFactory {
    public static CryptoEntry getEntry(byte[] data, boolean needVerify) {
        byte[] pv = new byte[2];
        System.arraycopy(data, 10, pv, 0, 2);
        int protocolVersion = NumericUtils.bytesToInt(pv);
        switch (protocolVersion) {
            case 1:
                return new CryptoEntryV1(data, needVerify);
            default:
                throw new UnsupportedProtocolVersion("Unsupported protocol version for CryptoEntry:" + protocolVersion);
        }
    }

    public static CryptoEntry getEntry(byte[] data) {
        return getEntry(data, true);
    }

    public static CryptoEntry getEntry(int protocolVersion, boolean needVerify) {
        switch (protocolVersion) {
            case 1:
                return new CryptoEntryV1(needVerify);
            default:
                throw new UnsupportedProtocolVersion("Unsupported protocol version for CryptoEntry:" + protocolVersion);
        }
    }

    public static CryptoEntry getEntry(int protocolVersion) {
        return getEntry(protocolVersion, true);
    }
}
