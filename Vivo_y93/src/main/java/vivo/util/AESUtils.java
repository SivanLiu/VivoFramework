package vivo.util;

import android.util.Slog;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static final String HEX = "0123456789ABCDEF";
    public static final String KEY_ALGORITHM = "AES";
    private static final String TAG = "AESUtils";
    private static final String key = "yyKyeaGqD+iHxz3Vq+Of/Q==";

    public static String encrypt(String seed, String cleartext) throws Exception {
        return toHex(encrypt(getRawKey(seed.getBytes()), cleartext.getBytes()));
    }

    public static String decrypt(String seed, String encrypted) throws Exception {
        return new String(decrypt(getRawKey(seed.getBytes()), toByte(encrypted)));
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(128, sr);
        return kgen.generateKey().getEncoded();
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(1, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(clear);
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(2, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(encrypted);
    }

    private static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    private static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (byte appendHex : buf) {
            appendHex(result, appendHex);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 15)).append(HEX.charAt(b & 15));
    }

    public static String aesEncrypt(String content) {
        if (content != null) {
            try {
                return encrypt(key, content);
            } catch (Exception e) {
                print(e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String aesDecrypt(String content) {
        if (content != null) {
            try {
                return decrypt(key, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void print(String s) {
        Slog.d(TAG, " " + s);
    }
}
