package com.qti.location.sdk.collection;

import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EZ {
    private static final String CLIPHER = "AES/CBC/PKCS5Padding";
    private static final String ENCODING = "UTF-8";
    private static final byte[] KEY_VI = new byte[]{(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8};
    private static final String PASSWORD_KEY = "N0n_Sense_C0de16";

    public String getClipher() {
        return CLIPHER;
    }

    public String encrypt(String con) {
        if (con == null || con.length() < 1) {
            return null;
        }
        return Base64.encodeToString(encryptInner(gzip(con)), 10);
    }

    public String decrypt(String con) {
        if (con == null || con.length() < 1) {
            return null;
        }
        byte[] decrypt = decryptInner(Base64.decode(con, 10));
        if (decrypt == null || decrypt.length <= 0) {
            return null;
        }
        return new String(ungzip(decrypt));
    }

    private byte[] encryptInner(byte[] byteContent) {
        if (byteContent == null || byteContent.length < 1) {
            return null;
        }
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(KEY_VI);
            SecretKeySpec key = new SecretKeySpec(PASSWORD_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(CLIPHER);
            cipher.init(1, key, zeroIv);
            return cipher.doFinal(byteContent);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (BadPaddingException e3) {
            e3.printStackTrace();
        } catch (NoSuchPaddingException e4) {
            e4.printStackTrace();
        } catch (IllegalBlockSizeException e5) {
            e5.printStackTrace();
        } catch (InvalidAlgorithmParameterException e6) {
            e6.printStackTrace();
        }
        return null;
    }

    private byte[] decryptInner(byte[] byteContent) {
        if (byteContent == null || byteContent.length < 1) {
            return null;
        }
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(KEY_VI);
            SecretKeySpec key = new SecretKeySpec(PASSWORD_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(CLIPHER);
            cipher.init(2, key, zeroIv);
            return cipher.doFinal(byteContent);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (BadPaddingException e3) {
            e3.printStackTrace();
        } catch (NoSuchPaddingException e4) {
            e4.printStackTrace();
        } catch (IllegalBlockSizeException e5) {
            e5.printStackTrace();
        } catch (InvalidAlgorithmParameterException e6) {
            e6.printStackTrace();
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0035 A:{SYNTHETIC, Splitter: B:22:0x0035} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0041 A:{SYNTHETIC, Splitter: B:28:0x0041} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] gzip(String primStr) {
        IOException e;
        Throwable th;
        if (primStr == null || primStr.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            GZIPOutputStream gzip2 = new GZIPOutputStream(out);
            try {
                gzip2.write(primStr.getBytes(ENCODING));
                if (gzip2 != null) {
                    try {
                        gzip2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                gzip = gzip2;
            } catch (IOException e3) {
                e2 = e3;
                gzip = gzip2;
                try {
                    e2.printStackTrace();
                    if (gzip != null) {
                    }
                    return out.toByteArray();
                } catch (Throwable th2) {
                    th = th2;
                    if (gzip != null) {
                        try {
                            gzip.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                gzip = gzip2;
                if (gzip != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            e22.printStackTrace();
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return out.toByteArray();
        }
        return out.toByteArray();
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x0065 A:{SYNTHETIC, Splitter: B:51:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x006a A:{SYNTHETIC, Splitter: B:54:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x006f A:{SYNTHETIC, Splitter: B:57:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0065 A:{SYNTHETIC, Splitter: B:51:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x006a A:{SYNTHETIC, Splitter: B:54:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x006f A:{SYNTHETIC, Splitter: B:57:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0032 A:{SYNTHETIC, Splitter: B:22:0x0032} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0037 A:{SYNTHETIC, Splitter: B:25:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x003c A:{SYNTHETIC, Splitter: B:28:0x003c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String ungzip(byte[] compressed) {
        IOException e;
        Throwable th;
        if (compressed == null || compressed.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        String decompressed = null;
        try {
            ByteArrayInputStream in2 = new ByteArrayInputStream(compressed);
            try {
                GZIPInputStream ginzip2 = new GZIPInputStream(in2);
                try {
                    byte[] buffer = new byte[2048];
                    while (true) {
                        int offset = ginzip2.read(buffer);
                        if (offset == -1) {
                            break;
                        }
                        out.write(buffer, 0, offset);
                    }
                    decompressed = out.toString();
                    if (ginzip2 != null) {
                        try {
                            ginzip2.close();
                        } catch (IOException e2) {
                        }
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e3) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                        }
                    }
                    ginzip = ginzip2;
                } catch (IOException e5) {
                    e = e5;
                    ginzip = ginzip2;
                    in = in2;
                } catch (Throwable th2) {
                    th = th2;
                    ginzip = ginzip2;
                    in = in2;
                    if (ginzip != null) {
                    }
                    if (in != null) {
                    }
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e = e6;
                in = in2;
                try {
                    e.printStackTrace();
                    if (ginzip != null) {
                        try {
                            ginzip.close();
                        } catch (IOException e7) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e8) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e9) {
                        }
                    }
                    return decompressed;
                } catch (Throwable th3) {
                    th = th3;
                    if (ginzip != null) {
                        try {
                            ginzip.close();
                        } catch (IOException e10) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e11) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e12) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                if (ginzip != null) {
                }
                if (in != null) {
                }
                if (out != null) {
                }
                throw th;
            }
        } catch (IOException e13) {
            e = e13;
            e.printStackTrace();
            if (ginzip != null) {
            }
            if (in != null) {
            }
            if (out != null) {
            }
            return decompressed;
        }
        return decompressed;
    }
}
