package com.vivo.audiotags.asf.util;

import com.vivo.audiotags.asf.data.GUID;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.GregorianCalendar;

public class Utils {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void fillChars(char[] chars, RandomAccessFile raf) throws IOException {
        if (chars == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        }
        for (int i = 0; i < chars.length; i++) {
            chars[i] = raf.readChar();
        }
    }

    public static byte[] getBytes(long value, int byteCount) {
        byte[] result = new byte[byteCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) ((int) (255 & value));
            value >>>= 8;
        }
        return result;
    }

    public static GregorianCalendar getDateOf(BigInteger fileTime) {
        GregorianCalendar result = new GregorianCalendar(1601, 0, 1);
        fileTime = fileTime.divide(new BigInteger("10000"));
        BigInteger maxInt = new BigInteger(String.valueOf(Integer.MAX_VALUE));
        while (fileTime.compareTo(maxInt) > 0) {
            result.add(14, Integer.MAX_VALUE);
            fileTime = fileTime.subtract(maxInt);
        }
        result.add(14, fileTime.intValue());
        return result;
    }

    public static int read7Bit(RandomAccessFile raf) throws IOException {
        return raf.read() & 127;
    }

    public static BigInteger readBig64(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[8];
        byte[] oa = new byte[8];
        raf.readFully(bytes);
        for (int i = 0; i < bytes.length; i++) {
            oa[7 - i] = bytes[i];
        }
        return new BigInteger(oa);
    }

    public static String readCharacterSizedString(RandomAccessFile raf) throws IOException {
        StringBuffer result = new StringBuffer();
        int strLen = readUINT16(raf);
        int character = raf.read() | (raf.read() << 8);
        System.out.println("sunrain readCharacterSizedString character 1--> " + character + " , strLen --> " + strLen + " , result.length() --> " + result.length());
        if (strLen == 0) {
            return null;
        }
        do {
            if (character != 0) {
                result.append((char) character);
                character = raf.read() | (raf.read() << 8);
            }
            System.out.println("sunrain readCharacterSizedString character 2--> " + character + " , strLen --> " + strLen + " , result.length() --> " + result.length());
            if (character == 0) {
                break;
            }
        } while (result.length() + 1 < strLen);
        if (strLen == result.length() + 1) {
            return result.toString();
        }
        throw new IllegalStateException("Invalid Data for current interpretation");
    }

    public static GUID readGUID(RandomAccessFile raf) throws IOException {
        if (raf == null) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        int[] binaryGuid = new int[16];
        for (int i = 0; i < binaryGuid.length; i++) {
            binaryGuid[i] = raf.read();
        }
        return new GUID(binaryGuid);
    }

    public static int readUINT16(RandomAccessFile raf) throws IOException {
        return raf.read() | (raf.read() << 8);
    }

    public static long readUINT32(RandomAccessFile raf) throws IOException {
        long result = 0;
        for (int i = 0; i <= 24; i += 8) {
            result |= (long) (raf.read() << i);
        }
        return result;
    }

    public static long readUINT64(RandomAccessFile raf) throws IOException {
        long result = 0;
        for (int i = 0; i <= 56; i += 8) {
            result |= (long) (raf.read() << i);
        }
        return result;
    }

    public static String readUTF16LEStr(RandomAccessFile raf) throws IOException {
        byte[] buf = new byte[readUINT16(raf)];
        if (raf.read(buf) == buf.length) {
            if (buf.length >= 2 && buf[buf.length - 1] == (byte) 0 && buf[buf.length - 2] == (byte) 0) {
                byte[] copy = new byte[(buf.length - 2)];
                System.arraycopy(buf, 0, copy, 0, buf.length - 2);
                buf = copy;
            }
            return new String(buf, "UTF-16LE");
        }
        throw new IllegalStateException("Invalid Data for current interpretation");
    }

    public static void checkStringLengthNullSafe(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (value.getBytes("UTF-16LE").length > 65533) {
                    throw new IllegalArgumentException("\"UTF-16LE\" representation exceeds 65535 bytes. (Including zero term character)");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
