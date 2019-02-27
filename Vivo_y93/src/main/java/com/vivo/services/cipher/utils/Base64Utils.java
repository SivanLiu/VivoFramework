package com.vivo.services.cipher.utils;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;

public class Base64Utils {
    private static byte[] base64DecodeChars = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 62, (byte) -1, (byte) -1, (byte) -1, (byte) 63, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 26, GsmAlphabet.GSM_EXTENDED_ESCAPE, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
    private static char[] base64EncodeChars = new char[]{DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F', 'G', 'H', 'I', 'J', 'K', DateFormat.STANDALONE_MONTH, DateFormat.MONTH, PhoneNumberUtils.WILD, 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', DateFormat.AM_PM, 'b', 'c', DateFormat.DATE, 'e', 'f', 'g', DateFormat.HOUR, 'i', 'j', DateFormat.HOUR_OF_DAY, 'l', DateFormat.MINUTE, 'n', 'o', PhoneNumberUtils.BBK_PAUSE, 'q', 'r', DateFormat.SECONDS, 't', 'u', 'v', PhoneNumberUtils.BBK_WAIT, StateProperty.TARGET_X, 'y', DateFormat.TIME_ZONE, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    public static String encode(byte[] data) {
        int i;
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i2 = 0;
        while (i2 < len) {
            i = i2 + 1;
            int b1 = data[i2] & 255;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 3) << 4]);
                sb.append("==");
                break;
            }
            i2 = i + 1;
            int b2 = data[i] & 255;
            if (i2 == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 3) << 4) | ((b2 & 240) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 15) << 2]);
                sb.append(Contants.QSTRING_EQUAL);
                i = i2;
                break;
            }
            i = i2 + 1;
            int b3 = data[i2] & 255;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 3) << 4) | ((b2 & 240) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 15) << 2) | ((b3 & 192) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 63]);
            i2 = i;
        }
        i = i2;
        return sb.toString();
    }

    public static byte[] decode(String str) {
        try {
            return decodePrivate(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x009d A:{LOOP_END, LOOP:0: B:1:0x0012->B:38:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x009b A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x006d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003d A:{LOOP_START, PHI: r6 , LOOP:2: B:13:0x003d->B:12:0x003b} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0024 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] decodePrivate(String str) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        byte[] data = str.getBytes("US-ASCII");
        int len = data.length;
        int i = 0;
        while (i < len) {
            int b1;
            while (true) {
                int i2 = i + 1;
                b1 = base64DecodeChars[data[i]];
                if (i2 < len && b1 == -1) {
                    i = i2;
                } else if (b1 != -1) {
                    i = i2;
                    break;
                } else {
                    int b2;
                    do {
                        i = i2;
                        i2 = i + 1;
                        b2 = base64DecodeChars[data[i]];
                        if (i2 >= len) {
                            break;
                        }
                    } while (b2 == -1);
                    if (b2 == -1) {
                        i = i2;
                        break;
                    }
                    int b3;
                    sb.append((char) ((b1 << 2) | ((b2 & 48) >>> 4)));
                    while (true) {
                        i = i2;
                        i2 = i + 1;
                        b3 = data[i];
                        if (b3 == 61) {
                            return sb.toString().getBytes("iso8859-1");
                        }
                        b3 = base64DecodeChars[b3];
                        if (i2 >= len || b3 != -1) {
                            if (b3 != -1) {
                                i = i2;
                                break;
                            }
                            int b4;
                            sb.append((char) (((b2 & 15) << 4) | ((b3 & 60) >>> 2)));
                            while (true) {
                                i = i2;
                                i2 = i + 1;
                                b4 = data[i];
                                if (b4 == 61) {
                                    return sb.toString().getBytes("iso8859-1");
                                }
                                b4 = base64DecodeChars[b4];
                                if (i2 >= len || b4 != -1) {
                                    if (b4 != -1) {
                                        i = i2;
                                        break;
                                    }
                                    sb.append((char) (((b3 & 3) << 6) | b4));
                                    i = i2;
                                }
                            }
                            if (b4 != -1) {
                            }
                        }
                    }
                    if (b3 != -1) {
                    }
                }
            }
            if (b1 != -1) {
            }
        }
        return sb.toString().getBytes("iso8859-1");
    }
}
