package android.security.keymaster;

import java.io.PrintStream;
import java.io.PrintWriter;

public class SecurityKeyException extends Exception {
    public static final int KS_ERROR_AES_DECRYPT_FAIL = -10;
    public static final int KS_ERROR_AES_ENCRYPT_FAIL = -9;
    public static final int KS_ERROR_APP_COUNT_FULL = -31;
    public static final int KS_ERROR_CACULATE_HASH_FAIL = -28;
    public static final int KS_ERROR_CHECK_PLAIN_KEY = -32;
    public static final int KS_ERROR_CMD_ID_NOT_EXIST = -7;
    public static final int KS_ERROR_CREATE_LINK_FAIL = -24;
    public static final int KS_ERROR_DATA_SIZE_OVER_2K = -30;
    public static final int KS_ERROR_DEVICE_CONTEXT_NULL = -20;
    public static final int KS_ERROR_FIND_KEY_FAIL = -25;
    public static final int KS_ERROR_FREE_MEMORY_FAIL = -6;
    public static final int KS_ERROR_FW_BINDER_ERROR = -202;
    public static final int KS_ERROR_FW_INPUT_PARAMETER_LEN = -201;
    public static final int KS_ERROR_HASH_NOT_MATCH = -17;
    public static final int KS_ERROR_INPUT_PARAMETER_LEN = -2;
    public static final int KS_ERROR_INPUT_PARAMETER_NULL = -1;
    public static final int KS_ERROR_KEY_VERSION_NOT_MATCH = -29;
    public static final int KS_ERROR_KM_NOT_EXIST = -100;
    public static final int KS_ERROR_MELLOC_MEMORY_FAIL = -5;
    public static final int KS_ERROR_OPEN_SFS_FILE_FAIL = -26;
    public static final int KS_ERROR_OPERATE_FUNC_NULL = -101;
    public static final int KS_ERROR_OUTPUT_PARAMETER_LEN = -4;
    public static final int KS_ERROR_OUTPUT_PARAMETER_NULL = -3;
    public static final int KS_ERROR_RAED_ROOT_KEY = -19;
    public static final int KS_ERROR_RAED_UNIQUE_ID = -18;
    public static final int KS_ERROR_READ_RPMB_FAIL = -27;
    public static final int KS_ERROR_READ_SFS_FAIL = -16;
    public static final int KS_ERROR_REGISTER_SHARE_BUFFER = -23;
    public static final int KS_ERROR_RSA_DECRYPT_FAIL = -12;
    public static final int KS_ERROR_RSA_ENCRYPT_FAIL = -11;
    public static final int KS_ERROR_RSA_SIGN_FAIL = -13;
    public static final int KS_ERROR_RSA_VERIFY_FAIL = -14;
    public static final int KS_ERROR_SET_BANDWIDTH_FAIL = -8;
    public static final int KS_ERROR_UNKNOWN_ENCRYPT_TYPE = -21;
    public static final int KS_ERROR_UNKNOWN_ERROR = -1000;
    public static final int KS_ERROR_UNKNOWN_OPERATE_TYPE = -22;
    public static final int KS_ERROR_WRITE_SFS_FAIL = -15;
    public static final int SK_ERROR_AES_DECRYPT_INPUT_LEN = 121;
    public static final int SK_ERROR_AES_ENCRYPT_INPUT_LEN = 120;
    public static final int SK_ERROR_CRYPTO_BODY = 151;
    public static final int SK_ERROR_CRYPTO_HEADER = 150;
    public static final int SK_ERROR_INPUT_NULL = 102;
    public static final int SK_ERROR_KEY_NOT_MATCH = 153;
    public static final int SK_ERROR_KEY_UPDATE_CONN_FAIL = 157;
    public static final int SK_ERROR_KEY_UPDATE_CONN_IO_EXP = 170;
    public static final int SK_ERROR_KEY_UPDATE_CONN_ISTREAM_NULL = 167;
    public static final int SK_ERROR_KEY_UPDATE_CONN_MALURL_EXP = 169;
    public static final int SK_ERROR_KEY_UPDATE_CONN_OSTREAM_NULL = 166;
    public static final int SK_ERROR_KEY_UPDATE_CONN_PROTOCOL_EXP = 168;
    public static final int SK_ERROR_KEY_UPDATE_CONN_RESP_CODE = 164;
    public static final int SK_ERROR_KEY_UPDATE_DEVICE_FAIL = 160;
    public static final int SK_ERROR_KEY_UPDATE_FAIL = 155;
    public static final int SK_ERROR_KEY_UPDATE_NET_ACCESS_DENY = 158;
    public static final int SK_ERROR_KEY_UPDATE_RESP_FORMAT_FAULT = 156;
    public static final int SK_ERROR_KEY_UPDATE_SERVER_DATA_EMPTY = 162;
    public static final int SK_ERROR_KEY_UPDATE_SERVER_NO_DATA = 163;
    public static final int SK_ERROR_KEY_UPDATE_SERVER_RETURN_NULL = 165;
    public static final int SK_ERROR_KEY_UPDATE_SERVER_TIMEOUT = 161;
    public static final int SK_ERROR_KEY_UPDATE_TASK_EXP = 171;
    public static final int SK_ERROR_KEY_UPDATE_TASK_NO_DATA_EXP = 172;
    public static final int SK_ERROR_KEY_UPDATE_TYPE_UNKNOWN = 159;
    public static final int SK_ERROR_MODE_NOT_MATCH = 152;
    public static final int SK_ERROR_NOT_AVAILABLE = 101;
    public static final int SK_ERROR_PKG_TYPE_NOT_SUPPORTED = 154;
    public static final int SK_ERROR_RSA_DECRYPT_INPUT_LEN = 131;
    public static final int SK_ERROR_RSA_ENCRYPT_INPUT_LEN = 130;
    public static final int SK_ERROR_SERVER_APP_SIGN_VERIFY = 401;
    public static final int SK_ERROR_SERVER_INTERNAL_ERROR = 500;
    public static final int SK_ERROR_SERVER_REQUEST_FORMAT = 400;
    public static final int SK_ERROR_SERVER_UNIQUEID_NOT_FOUND = 404;
    public static final int SK_ERROR_SIGN_INPUT_LEN = 142;
    public static final int SK_ERROR_SIGN_SIGNED_INPUT_LEN = 140;
    public static final int SK_ERROR_SIGN_VERIFY_INPUT_LEN = 141;
    public static final int SK_ERROR_STORAGE_INPUT_LEN = 110;
    public static final int SK_ERROR_UNKNOWN = 1000;
    private int mErrorCode;

    public SecurityKeyException(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public SecurityKeyException(String errorMessage, int errorCode) {
        super(errorMessage);
        this.mErrorCode = errorCode;
    }

    public SecurityKeyException(Throwable throwable, int errorCode) {
        super(throwable);
        this.mErrorCode = errorCode;
    }

    public SecurityKeyException(String errorMessage, Throwable throwable, int errorCode) {
        super(errorMessage, throwable);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public void printStackTrace(PrintStream paramPrintStream) {
        paramPrintStream.println("ErrorCode = " + getErrorCode());
        super.printStackTrace(paramPrintStream);
    }

    public void printStackTrace(PrintWriter paramPrintWriter) {
        paramPrintWriter.println("ErrorCode = " + getErrorCode());
        super.printStackTrace(paramPrintWriter);
    }
}
