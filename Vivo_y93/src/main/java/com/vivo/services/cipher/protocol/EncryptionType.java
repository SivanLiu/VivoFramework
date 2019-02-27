package com.vivo.services.cipher.protocol;

public class EncryptionType {
    public static final int PLAINTEXT = 0;
    public static final int ROOT_RSA_PRI_KEY_SHA256_WITH_RSA_SIGN = 8;
    public static final int ROOT_RSA_PUB_KEY_NONE_NOPADDING_ENCRYPT = 1;
    public static final int ROOT_RSA_PUB_KEY_NONE_PKCS1PADDING_ENCRYPT = 2;
    public static final int SECOND_AES_KEY_CBC_PKCS7PADDING_ENCRYPT = 4;
    public static final int SECOND_AES_KEY_ECB_NOPADDING_ENCRYPT = 3;
    public static final int THIRD_EK_AES_KEY_CBC_PKCS7PADDING_ENCRYPT = 5;
    public static final int THIRD_SK_RSA_PRI_KEY_SHA256_WITH_RSA_AND_MGF1_SIGN = 9;
    public static final int THIRD_SK_RSA_PRI_KEY_SHA256_WITH_RSA_SIGN = 11;
    public static final int THIRD_SK_RSA_PUB_KEY_NONE_PKCS1PADDING_ENCRYPT = 6;
    public static final int THIRD_VK_RSA_PRI_KEY_SHA256_WITH_RSA_AND_MGF1_SIGN = 10;
    public static final int THIRD_VK_RSA_PRI_KEY_SHA256_WITH_RSA_SIGN = 12;
    public static final int THIRD_VK_RSA_PUB_KEY_NONE_PKCS1PADDING_ENCRYPT = 7;
}
