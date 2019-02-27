package com.vivo.services.cipher;

import android.content.Context;
import android.security.KeyStore;
import android.security.keymaster.SecurityKeyException;
import android.security.keymaster.VivoSecurityKeyResult;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import com.vivo.services.cipher.utils.Base64Utils;
import com.vivo.services.cipher.utils.Contants;
import com.vivo.services.cipher.utils.ProtocolPackage;
import com.vivo.services.cipher.utils.SecurityKeyConfigure;
import com.vivo.services.cipher.utils.SecurityKeyDataCollection;
import com.vivo.services.cipher.utils.SecurityServerHandler;
import com.vivo.services.cipher.utils.SecurityServerResult;
import com.vivo.services.cipher.utils.VLog;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SecurityKeyCipher {
    public static final int AES_DECRYPT_DATA_SIZE_MAX = 204816;
    public static final int AES_ENCRYPT_DATA_SIZE_MAX = 204800;
    public static final int KEY_TYPE_EK = 1;
    public static final int KEY_TYPE_SK = 2;
    public static final int KEY_TYPE_VK = 4;
    public static final int PKGTYPE_AES_ENCRYPT = 5;
    public static final int PKGTYPE_RSA_ENCRYPT = 6;
    public static final int PKGTYPE_SIGN_SIGNED = 10;
    private static final int RETRY_TIMES = 3;
    public static final int RSA_DECRYPT_DATA_SIZE_MAX = 256;
    public static final int RSA_ENCRYPT_DATA_SIZE_MAX = 245;
    public static final int SECURITY_DATA_SIZE_MAX = 2048;
    public static final int SIGN_DATA_SIZE = 256;
    public static final int SIGN_SIGNED_DATA_SIZE_MAX = 204800;
    public static final int SIGN_VERIFY_DATA_SIZE_MAX = 204800;
    private static final long SK_CIPHER_EXEC_INTERVAL = 20;
    public static final int SK_MODE_AUTO = 1;
    public static final int SK_MODE_SOFT = 3;
    public static final int SK_MODE_TEE = 2;
    private static final long SK_TRY_UPDATE_KEY_INTERVAL = 28800000;
    private static final int SK_UPDATE_KEY_FAIL_CAUSE_NETWORK = 1;
    private static final int SK_UPDATE_KEY_FAIL_CAUSE_TEE = 2;
    private static volatile Map<String, SecurityKeyCipher> ciphers = new HashMap();
    private SecurityKeyConfigure mConfigure = new SecurityKeyConfigure();
    private boolean mDevInitSuc = false;

    private SecurityKeyCipher(Context context, String serviceName) {
        this.mConfigure.setContext(context);
        this.mConfigure.setKeystore(KeyStore.getInstance());
        this.mConfigure.setCipherMode(1);
        if (!isServiceContext(context)) {
            this.mConfigure.setPackageName(context.getPackageName());
            this.mConfigure.getKeystore().vivoSecurityKeyInit(context, null);
        } else if (serviceName != null) {
            this.mConfigure.setPackageName(context.getPackageName() + "." + serviceName);
            this.mConfigure.getKeystore().vivoSecurityKeyInit(context, serviceName);
            this.mConfigure.setRealCipherMode(2);
        }
        VLog.d(Contants.TAG, this.mConfigure, "Create new securityKeyCipher");
    }

    public static SecurityKeyCipher getInstance(Context context) {
        return getInstance(context, null);
    }

    public static synchronized SecurityKeyCipher getInstance(Context context, String moduleName) {
        SecurityKeyCipher cipher;
        synchronized (SecurityKeyCipher.class) {
            if (context == null) {
                VLog.e(Contants.TAG, "getInstance context inputed is null");
                return null;
            } else if (isServiceContext(context) && moduleName == null) {
                VLog.e(Contants.TAG, "getInstance moduleName inputed is null");
                return null;
            } else {
                String packageName;
                if (isServiceContext(context)) {
                    packageName = moduleName;
                } else {
                    packageName = context.getPackageName();
                }
                if (packageName == null || TextUtils.isEmpty(packageName)) {
                    VLog.e(Contants.TAG, "getInstance package name is null or empty");
                    return null;
                } else if (ciphers.containsKey(packageName)) {
                    SecurityKeyCipher securityKeyCipher = (SecurityKeyCipher) ciphers.get(packageName);
                    return securityKeyCipher;
                } else {
                    cipher = new SecurityKeyCipher(context, moduleName);
                    if (cipher != null) {
                        ciphers.put(packageName, cipher);
                        try {
                            cipher.init();
                        } catch (Exception e) {
                            VLog.e(Contants.TAG, "SecurityKey cipher init fail");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return cipher;
    }

    private static boolean isServiceContext(Context context) {
        if (context.getPackageName().equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
            return true;
        }
        return false;
    }

    private boolean init() throws SecurityKeyException {
        boolean result = syncDeviceInfo();
        if (result) {
            this.mDevInitSuc = true;
            return result;
        }
        VLog.e(Contants.TAG, this.mConfigure, "synchronize device information fail");
        return false;
    }

    private int getUpdateKeyType() {
        int updateKeyType = 0;
        if (this.mConfigure.getKeyVersion(1) == 0) {
            updateKeyType = 1;
        }
        if (this.mConfigure.getKeyVersion(2) == 0) {
            updateKeyType |= 2;
        }
        if (this.mConfigure.getKeyVersion(4) == 0) {
            return updateKeyType | 4;
        }
        return updateKeyType;
    }

    /* JADX WARNING: Missing block: B:45:0x00dc, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isKeyReady(int keyType) throws SecurityKeyException {
        if (!this.mDevInitSuc && (syncDeviceInfo() ^ 1) != 0) {
            return false;
        }
        if ((this.mConfigure.getRealCipherMode() == 2 || this.mConfigure.getCipherMode() == 3) && this.mConfigure.getKeyVersion(keyType) != 0) {
            return true;
        }
        long curTime = System.currentTimeMillis();
        int updateKeyType = getUpdateKeyType();
        if (this.mConfigure.getRealCipherMode() == 2) {
            VLog.w(Contants.TAG, this.mConfigure, "Try to get key " + keyType + "again");
            this.mConfigure.setLastTryUpdateKeyTime(curTime);
            return updateKey(updateKeyType);
        }
        boolean updateKeySuc = false;
        if (this.mConfigure.getUpdateKeyFailCause() == 1 && this.mConfigure.getCipherMode() == 1 && this.mConfigure.getRealCipherMode() == 3 && curTime - this.mConfigure.getLastTryUpdateKeyTime() > SK_TRY_UPDATE_KEY_INTERVAL) {
            VLog.d(Contants.TAG, this.mConfigure, "Auto try to get key " + keyType + " again");
            try {
                this.mConfigure.setRealCipherMode(2);
                if (init()) {
                    updateKeySuc = updateKey(getUpdateKeyType());
                }
            } catch (SecurityKeyException e) {
                if (!isNetworkUnstable(e.getErrorCode())) {
                    this.mConfigure.setUpdateKeyFailCause(2);
                }
            }
            if (!updateKeySuc) {
                this.mConfigure.setLastTryUpdateKeyTime(curTime);
                this.mConfigure.setRealCipherMode(3);
                init();
            }
        }
    }

    public boolean needToRetry(int dataCollectionType, int tryTimes, VivoSecurityKeyResult result) {
        if (result != null) {
            SecurityKeyDataCollection.collectData(this.mConfigure, tryTimes, dataCollectionType, result.resultCode);
            if (result.resultCode == 0) {
                return false;
            }
            VLog.e(Contants.TAG, this.mConfigure, "Actiontype " + SecurityKeyDataCollection.getActionKey(this.mConfigure, dataCollectionType) + "error: " + result.resultCode);
            return tryTimes < 3;
        } else {
            SecurityKeyDataCollection.collectData(this.mConfigure, tryTimes, dataCollectionType, 1000);
            VLog.e(Contants.TAG, this.mConfigure, "Actiontype " + SecurityKeyDataCollection.getActionKey(this.mConfigure, dataCollectionType) + "return null");
            return tryTimes < 3;
        }
    }

    public boolean setCipherMode(int mode) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "switch mode of cipher to " + mode);
        this.mConfigure.setCipherMode(mode);
        if (mode == 1) {
            this.mConfigure.setRealCipherMode(2);
        } else {
            this.mConfigure.setRealCipherMode(mode);
        }
        return init();
    }

    private boolean setInterCipherMode(int mode) {
        VLog.i(Contants.TAG, this.mConfigure, "internal switch mode of cipher to " + mode);
        if (this.mConfigure.getCipherMode() != 1) {
            VLog.e(Contants.TAG, this.mConfigure, "Current mode is not auto");
            return false;
        }
        this.mConfigure.setRealCipherMode(mode);
        try {
            return init();
        } catch (SecurityKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getCurCipherMode() {
        try {
            if (!(this.mConfigure.getRealCipherMode() != 2 || isKeyReady(1) || (isKeyReady(2) ^ 1) == 0 || (isKeyReady(4) ^ 1) == 0)) {
                setInterCipherMode(3);
            }
        } catch (SecurityKeyException e) {
            e.printStackTrace();
            VLog.e(Contants.TAG, this.mConfigure, "isKeyReady Fail");
            setInterCipherMode(3);
        }
        return this.mConfigure.getRealCipherMode();
    }

    public int getKeyVersion(int keyType) {
        try {
            if (this.mConfigure.getRealCipherMode() == 2 && !isKeyReady(keyType)) {
                setInterCipherMode(3);
            }
        } catch (SecurityKeyException e) {
            e.printStackTrace();
            VLog.e(Contants.TAG, this.mConfigure, "isKeyReady Fail");
            setInterCipherMode(3);
        }
        return this.mConfigure.getKeyVersion(keyType);
    }

    public boolean isFatalErrorOfTee(int errorCode) {
        if (this.mConfigure.getRealCipherMode() != 2) {
            return false;
        }
        if (errorCode < 0 || isNetworkUnstable(errorCode)) {
            return true;
        }
        return false;
    }

    private boolean syncDeviceInfo() throws SecurityKeyException {
        VivoSecurityKeyResult result;
        int tryTimes = 0;
        do {
            result = this.mConfigure.getKeystore().vivoSecurityKeyGetDeviceInfo(this.mConfigure.getRealCipherMode());
            if (this.mConfigure.getCipherMode() == 1 && result != null && isFatalErrorOfTee(result.resultCode)) {
                this.mConfigure.setRealCipherMode(3);
            }
            tryTimes++;
        } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_DEVICE_INIT, tryTimes, result));
        if (result == null) {
            VLog.e(Contants.TAG, this.mConfigure, "Get device information return null");
            throw new SecurityKeyException(Contants.ERROR_DEVICE_INIT_FAIL, 1000);
        } else if (result.resultCode != 0) {
            VLog.e(Contants.TAG, this.mConfigure, "Get device information error: " + result.resultCode);
            throw new SecurityKeyException(Contants.ERROR_DEVICE_INIT_FAIL, result.resultCode);
        } else {
            if (result.uniqueId.equals(Contants.NO_UNIQUEID)) {
                this.mConfigure.setRealCipherMode(3);
            } else {
                this.mConfigure.setRealCipherMode(2);
            }
            this.mConfigure.setUniqueId(result.uniqueId);
            if (result.uniqueId.equals(Contants.NO_UNIQUEID)) {
                String emmcId = this.mConfigure.getKeystore().vivoKeystoreGetUniqueID();
                if (!(emmcId == null || (TextUtils.isEmpty(emmcId) ^ 1) == 0)) {
                    this.mConfigure.setUniqueId(emmcId);
                }
            }
            this.mConfigure.setKeyVersion(result.keyVersion);
            this.mConfigure.setAppSignHash(Base64Utils.encode(result.publicKeyHash));
            if (this.mConfigure.getRealCipherMode() == 2) {
                this.mConfigure.setToken(this.mConfigure.getPackageName());
            } else {
                this.mConfigure.setToken(Contants.SO_ENCRYPT_PRE_PACKAGE + this.mConfigure.getPackageName());
            }
            return true;
        }
    }

    public String getUniqueId() {
        if (this.mDevInitSuc) {
            return this.mConfigure.getUniqueId();
        }
        VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
        return "Unknown";
    }

    public boolean updateKey() throws SecurityKeyException {
        VLog.d(Contants.TAG, this.mConfigure, "Update all key");
        return updateKey(7);
    }

    private byte[] getSecurityKeysFromServer(int keyType) throws SecurityKeyException {
        FutureTask<SecurityServerResult> task = new FutureTask(new SecurityServerHandler(this.mConfigure, keyType));
        new Thread(task).start();
        try {
            SecurityServerResult serverResult = (SecurityServerResult) task.get(Contants.UPDATE_KEY_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            if (serverResult == null) {
                VLog.e(Contants.TAG, this.mConfigure, "update key network return null");
                throw new SecurityKeyException("update key fail", 1000);
            } else if (serverResult.getResult()) {
                if (serverResult.getData() != null) {
                    return serverResult.getData();
                }
                VLog.e(Contants.TAG, this.mConfigure, "update key network keys is null");
                throw new SecurityKeyException("update key fail", 1000);
            } else if (serverResult.getException() != null) {
                VLog.e(Contants.TAG, this.mConfigure, "update key network occur exception");
                throw serverResult.getException();
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "update key network occur unkown error");
                throw new SecurityKeyException("update key fail", 1000);
            }
        } catch (TimeoutException e) {
            VLog.e(Contants.TAG, this.mConfigure, "update key network timeout:" + e.getMessage());
            e.printStackTrace();
            throw new SecurityKeyException(Contants.ERROR_KEY_UPDATE_TIMEOUT, 161);
        } catch (Exception e2) {
            VLog.e(Contants.TAG, this.mConfigure, "update key network error:" + e2.getMessage());
            e2.printStackTrace();
            throw new SecurityKeyException("update key fail", 1000);
        }
    }

    private boolean isNetworkUnstable(int errorCode) {
        if (errorCode == 162 || errorCode == 157 || errorCode == 161 || errorCode == 163 || errorCode == 156 || errorCode == 166 || errorCode == 167 || errorCode == 164 || errorCode == 168 || errorCode == 169 || errorCode == 170 || errorCode == 171 || errorCode == 172 || errorCode == 165) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0104 A:{Catch:{ SecurityKeyException -> 0x00bd, Exception -> 0x0085 }} */
    /* JADX WARNING: Missing block: B:15:?, code:
            com.vivo.services.cipher.utils.VLog.i(com.vivo.services.cipher.utils.Contants.TAG, r14.mConfigure, "Get key from server consume time: " + (java.lang.System.currentTimeMillis() - r6));
     */
    /* JADX WARNING: Missing block: B:16:0x006a, code:
            if (r2 != null) goto L_0x0110;
     */
    /* JADX WARNING: Missing block: B:17:0x006c, code:
            com.vivo.services.cipher.utils.VLog.e(com.vivo.services.cipher.utils.Contants.TAG, r14.mConfigure, "update key network keyData is null");
     */
    /* JADX WARNING: Missing block: B:18:0x0081, code:
            throw new android.security.keymaster.SecurityKeyException("update key fail", 172);
     */
    /* JADX WARNING: Missing block: B:38:0x0110, code:
            r5 = 0;
            r6 = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:39:0x0116, code:
            r3 = r14.mConfigure.getKeystore().vivoSecurityKeyUpdate(2, r15, r2);
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:40:0x0129, code:
            if (needToRetry(com.vivo.services.cipher.utils.SecurityKeyDataCollection.ACTIONTYPE_KEY_UPDATE, r5, r3) != false) goto L_0x0116;
     */
    /* JADX WARNING: Missing block: B:41:0x012b, code:
            if (r3 == null) goto L_0x0188;
     */
    /* JADX WARNING: Missing block: B:43:0x012f, code:
            if (r3.resultCode != 0) goto L_0x0159;
     */
    /* JADX WARNING: Missing block: B:44:0x0131, code:
            syncDeviceInfo();
            com.vivo.services.cipher.utils.VLog.i(com.vivo.services.cipher.utils.Contants.TAG, r14.mConfigure, "Save Key consume time: " + (java.lang.System.currentTimeMillis() - r6));
     */
    /* JADX WARNING: Missing block: B:47:0x0158, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            com.vivo.services.cipher.utils.VLog.e(com.vivo.services.cipher.utils.Contants.TAG, r14.mConfigure, "updateKey error: " + r3.resultCode);
            r14.mConfigure.setUpdateKeyFailCause(2);
     */
    /* JADX WARNING: Missing block: B:50:0x0187, code:
            throw new android.security.keymaster.SecurityKeyException("update key fail", r3.resultCode);
     */
    /* JADX WARNING: Missing block: B:51:0x0188, code:
            com.vivo.services.cipher.utils.VLog.e(com.vivo.services.cipher.utils.Contants.TAG, r14.mConfigure, "updateKey return null");
     */
    /* JADX WARNING: Missing block: B:52:0x019d, code:
            throw new android.security.keymaster.SecurityKeyException("update key fail", 1000);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean updateKey(int keyType) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "Update key " + keyType);
        if (this.mConfigure.getRealCipherMode() != 2) {
            VLog.e(Contants.TAG, this.mConfigure, "Update key fail: device is not supported tee");
            return false;
        }
        byte[] keyData = null;
        int tryTimes = 0;
        long startTime = System.currentTimeMillis();
        while (true) {
            tryTimes++;
            try {
                keyData = getSecurityKeysFromServer(keyType);
                SecurityKeyDataCollection.collectData(this.mConfigure, tryTimes, SecurityKeyDataCollection.ACTIONTYPE_KEY_UPDATE, 0);
            } catch (SecurityKeyException e) {
                SecurityKeyDataCollection.collectData(this.mConfigure, tryTimes, SecurityKeyDataCollection.ACTIONTYPE_KEY_UPDATE, e.getErrorCode(), e.getMessage());
                VLog.e(Contants.TAG, this.mConfigure, "update key network occur exception:" + e.getErrorCode());
                if (!isNetworkUnstable(e.getErrorCode()) || tryTimes == 3) {
                    if (isNetworkUnstable(e.getErrorCode())) {
                    }
                    throw e;
                } else if (tryTimes <= 3) {
                }
            } catch (Exception e2) {
                SecurityKeyDataCollection.collectData(this.mConfigure, tryTimes, SecurityKeyDataCollection.ACTIONTYPE_KEY_UPDATE, 1000);
                VLog.e(Contants.TAG, this.mConfigure, "update key network occur exception:" + e2.getMessage());
                e2.printStackTrace();
                throw new SecurityKeyException("update key fail", 171);
            }
            break;
        }
        if (isNetworkUnstable(e.getErrorCode())) {
            this.mConfigure.setUpdateKeyFailCause(1);
        }
        throw e;
    }

    public byte[] aesEncrypt(byte[] inputData) throws SecurityKeyException {
        try {
            return aesEncryptImpl(inputData);
        } catch (SecurityKeyException e) {
            if (isFatalErrorOfTee(e.getErrorCode()) && setInterCipherMode(3)) {
                VLog.w(Contants.TAG, this.mConfigure, "Aes Encrypt Auto Switch to Soft Mode");
                return aesEncryptImpl(inputData);
            }
            throw e;
        }
    }

    public byte[] aesEncryptImpl(byte[] inputData) throws SecurityKeyException {
        VLog.d(Contants.TAG, this.mConfigure, "start aesEncrypt");
        long startTime = System.currentTimeMillis();
        if (isKeyReady(1)) {
            if (startTime - this.mConfigure.getLastExecTime() < SK_CIPHER_EXEC_INTERVAL) {
                try {
                    Thread.sleep(SK_CIPHER_EXEC_INTERVAL - (startTime - this.mConfigure.getLastExecTime()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (inputData == null) {
                VLog.e(Contants.TAG, this.mConfigure, "aesEncrypt input data is null");
                throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
            } else if (inputData.length > 204800) {
                VLog.e(Contants.TAG, this.mConfigure, "aesEncrypt input data length " + inputData.length + " max length:" + 204800);
                throw new SecurityKeyException("input length > 200k", 120);
            } else {
                VivoSecurityKeyResult result;
                int tryTimes = 0;
                do {
                    result = this.mConfigure.getKeystore().vivoSecurityKeyEKEncrypt(this.mConfigure.getRealCipherMode(), inputData);
                    tryTimes++;
                } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_AES_ENCRYPT, tryTimes, result));
                if (result == null) {
                    VLog.e(Contants.TAG, this.mConfigure, "aesEncrypt result is null");
                    throw new SecurityKeyException(Contants.ERROR_EK_ENCRYPT, 1000);
                } else if (result.resultCode != 0) {
                    VLog.e(Contants.TAG, this.mConfigure, "aesEncrypt error: " + result.resultCode);
                    throw new SecurityKeyException(Contants.ERROR_EK_ENCRYPT, result.resultCode);
                } else if (result.operateData != null) {
                    byte[] encryptData = new ProtocolPackage(this.mConfigure.getToken(), result.keyVersion, 5, result.operateData).getbytes();
                    VLog.i(Contants.TAG, this.mConfigure, "aesEncrypt consume time: " + (System.currentTimeMillis() - startTime));
                    this.mConfigure.setLastExecTime(System.currentTimeMillis());
                    return encryptData;
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "aesEncrypt operateData is null ");
                    throw new SecurityKeyException(Contants.ERROR_EK_ENCRYPT, 1000);
                }
            }
        }
        VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
        throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
    }

    public byte[] aesDecrypt(byte[] inputData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start aesDecrypt");
        long startTime = System.currentTimeMillis();
        if (inputData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (isKeyReady(1)) {
            if (startTime - this.mConfigure.getLastExecTime() < SK_CIPHER_EXEC_INTERVAL) {
                try {
                    Thread.sleep(SK_CIPHER_EXEC_INTERVAL - (startTime - this.mConfigure.getLastExecTime()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ProtocolPackage pkg = ProtocolPackage.buildProtocolPackage(this.mConfigure, inputData);
            if (pkg.getData().length > AES_DECRYPT_DATA_SIZE_MAX) {
                VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt input data length " + pkg.getData().length + " max length:" + AES_DECRYPT_DATA_SIZE_MAX);
                throw new SecurityKeyException(Contants.ERROR_EK_DECRYPT_INPUT_LEN, 121);
            }
            if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(1)) {
                VLog.w(Contants.TAG, this.mConfigure, "aesDecrypt key version is not match current:" + this.mConfigure.getKeyVersion(1) + " target:" + pkg.getKeyVersion());
                if (!updateKey(1)) {
                    VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt update key fail");
                    throw new SecurityKeyException("update key fail", 155);
                } else if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(1)) {
                    VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt key version still not match after sync with server");
                    throw new SecurityKeyException(Contants.ERROR_KEY_NOT_MATCH, 153);
                }
            }
            if (this.mConfigure.getRealCipherMode() == 2 || pkg.getCipherMode() != 2) {
                int tryTimes = 0;
                if (5 == pkg.getType()) {
                    VivoSecurityKeyResult result;
                    do {
                        result = this.mConfigure.getKeystore().vivoSecurityKeyEKDecrypt(pkg.getCipherMode(), pkg.getKeyVersion(), pkg.getData());
                        tryTimes++;
                    } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_AES_DECRYPT, tryTimes, result));
                    if (result == null) {
                        VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt result is null");
                        throw new SecurityKeyException(Contants.ERROR_EK_DECRYPT, 1000);
                    } else if (result.resultCode != 0) {
                        VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt error: " + result.resultCode);
                        throw new SecurityKeyException(Contants.ERROR_EK_DECRYPT, result.resultCode);
                    } else if (result.operateData != null) {
                        byte[] decryptData = result.operateData;
                        VLog.i(Contants.TAG, this.mConfigure, "aesDecrypt consume time: " + (System.currentTimeMillis() - startTime));
                        this.mConfigure.setLastExecTime(System.currentTimeMillis());
                        return decryptData;
                    } else {
                        VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt operateData is null");
                        throw new SecurityKeyException(Contants.ERROR_EK_DECRYPT, 1000);
                    }
                }
                VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt decrypt type " + pkg.getType() + " is not supported");
                throw new SecurityKeyException(Contants.ERROR_TYPE_NOT_SUPPORTED, 154);
            }
            VLog.e(Contants.TAG, this.mConfigure, "aesDecrypt mode is not match need SOFT,device supported TEE,not switch to SOFT");
            throw new SecurityKeyException(Contants.ERROR_MODE_NOT_MATCH, 152);
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public byte[] rsaEncrypt(byte[] inputData) throws SecurityKeyException {
        try {
            return rsaEncryptImpl(inputData);
        } catch (SecurityKeyException e) {
            if (isFatalErrorOfTee(e.getErrorCode()) && setInterCipherMode(3)) {
                VLog.w(Contants.TAG, this.mConfigure, "Aes Decrypt Auto Switch to Soft Mode");
                return rsaEncryptImpl(inputData);
            }
            throw e;
        }
    }

    public byte[] rsaEncryptImpl(byte[] inputData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start rsaEncrypt");
        long startTime = System.currentTimeMillis();
        if (inputData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (inputData.length > 245) {
            VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt input data length:" + inputData.length + "max length:" + 245);
            throw new SecurityKeyException(Contants.ERROR_VK_ENCRYPT_INPUT_LEN, 130);
        } else if (isKeyReady(4)) {
            VivoSecurityKeyResult result;
            int tryTimes = 0;
            do {
                result = this.mConfigure.getKeystore().vivoSecurityKeyVKEncrypt(this.mConfigure.getRealCipherMode(), inputData);
                tryTimes++;
            } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_RSA_ENCRYPT, tryTimes, result));
            if (result == null) {
                VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt result is null");
                throw new SecurityKeyException(Contants.ERROR_VK_ENCRYPT, 1000);
            } else if (result.resultCode != 0) {
                VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt error:" + result.resultCode);
                throw new SecurityKeyException(Contants.ERROR_VK_ENCRYPT, result.resultCode);
            } else if (result.operateData != null) {
                byte[] encryptData = new ProtocolPackage(this.mConfigure.getToken(), result.keyVersion, 7, result.operateData).getbytes();
                VLog.i(Contants.TAG, this.mConfigure, "rsaEncrypt consume time: " + (System.currentTimeMillis() - startTime));
                return encryptData;
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt operateData is null");
                throw new SecurityKeyException(Contants.ERROR_VK_ENCRYPT, 1000);
            }
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public byte[] rsaDecrypt(byte[] inputData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start rsaDecrypt");
        long startTime = System.currentTimeMillis();
        if (inputData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "rsaEncrypt input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (isKeyReady(2)) {
            ProtocolPackage pkg = ProtocolPackage.buildProtocolPackage(this.mConfigure, inputData);
            if (pkg.getData().length > 256) {
                VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt input data length:" + pkg.getData().length + " max length:" + 256);
                throw new SecurityKeyException(Contants.ERROR_SK_DECRYPT_INPUT_LEN, 131);
            }
            if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(2)) {
                VLog.w(Contants.TAG, this.mConfigure, "rsaDecrypt key version is not match current:" + this.mConfigure.getKeyVersion(2) + " target:" + pkg.getKeyVersion());
                if (!updateKey(2)) {
                    VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt update key fail");
                    throw new SecurityKeyException("update key fail", 155);
                } else if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(2)) {
                    VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt key version still not match after sync key with server");
                    throw new SecurityKeyException(Contants.ERROR_KEY_NOT_MATCH, 153);
                }
            }
            if (this.mConfigure.getRealCipherMode() != 2 && pkg.getCipherMode() == 2) {
                VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt mode is not match need SOFT,device supported TEE,not switch to SOFT");
                throw new SecurityKeyException(Contants.ERROR_MODE_NOT_MATCH, 152);
            } else if (6 == pkg.getType() || 7 == pkg.getType()) {
                VivoSecurityKeyResult result;
                int tryTimes = 0;
                do {
                    result = this.mConfigure.getKeystore().vivoSecurityKeySKDecrypt(pkg.getCipherMode(), pkg.getKeyVersion(), pkg.getData());
                    tryTimes++;
                } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_RSA_DECRYPT, tryTimes, result));
                if (result == null) {
                    VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt result is null");
                    throw new SecurityKeyException(Contants.ERROR_SK_DECRYPT, 1000);
                } else if (result.resultCode != 0) {
                    VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt error: " + result.resultCode);
                    throw new SecurityKeyException(Contants.ERROR_SK_DECRYPT, result.resultCode);
                } else if (result.operateData != null) {
                    byte[] decryptData = result.operateData;
                    VLog.i(Contants.TAG, this.mConfigure, "rsaDecrypt consume time: " + (System.currentTimeMillis() - startTime));
                    return decryptData;
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt operateData is null");
                    throw new SecurityKeyException(Contants.ERROR_SK_DECRYPT, 1000);
                }
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "rsaDecrypt decrypt type " + pkg.getType() + " is not supported");
                throw new SecurityKeyException(Contants.ERROR_TYPE_NOT_SUPPORTED, 154);
            }
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public byte[] sign(byte[] inputData) throws SecurityKeyException {
        try {
            return signImpl(inputData);
        } catch (SecurityKeyException e) {
            if (isFatalErrorOfTee(e.getErrorCode()) && setInterCipherMode(3)) {
                VLog.w(Contants.TAG, this.mConfigure, "Sign Auto Switch to Soft Mode");
                return signImpl(inputData);
            }
            throw e;
        }
    }

    public byte[] signImpl(byte[] inputData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start sign");
        long startTime = System.currentTimeMillis();
        if (inputData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "sign input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (inputData.length > 204800) {
            VLog.e(Contants.TAG, this.mConfigure, "sign input data length " + inputData.length + " max length:" + 204800);
            throw new SecurityKeyException("input length > 200k", 140);
        } else if (isKeyReady(2)) {
            VivoSecurityKeyResult result;
            int tryTimes = 0;
            do {
                result = this.mConfigure.getKeystore().vivoSecurityKeySKSign(this.mConfigure.getRealCipherMode(), inputData);
                tryTimes++;
            } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_SIGN_SIGNED, tryTimes, result));
            if (result == null) {
                VLog.e(Contants.TAG, this.mConfigure, "sign result is null");
                throw new SecurityKeyException(Contants.ERROR_SIGN, 1000);
            } else if (result.resultCode != 0) {
                VLog.e(Contants.TAG, this.mConfigure, "sign error: " + result.resultCode);
                throw new SecurityKeyException(Contants.ERROR_SIGN, result.resultCode);
            } else if (result.operateData != null) {
                byte[] signature = new ProtocolPackage(this.mConfigure.getToken(), result.keyVersion, 9, result.operateData).getbytes();
                VLog.i(Contants.TAG, this.mConfigure, "sign consume time: " + (System.currentTimeMillis() - startTime));
                return signature;
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "sign operateData is null");
                throw new SecurityKeyException(Contants.ERROR_SIGN, 1000);
            }
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public boolean signatureVerify(byte[] inputData, byte[] signData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start signatureVerify");
        long startTime = System.currentTimeMillis();
        if (inputData == null || signData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "signatureVerify input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (inputData.length > 204800) {
            VLog.e(Contants.TAG, this.mConfigure, "signatureVerify input data length " + inputData.length + " max length:" + 204800);
            throw new SecurityKeyException("input length > 200k", 141);
        } else if (isKeyReady(4)) {
            ProtocolPackage pkg = ProtocolPackage.buildProtocolPackage(this.mConfigure, signData);
            if (pkg.getData().length != 256) {
                VLog.e(Contants.TAG, this.mConfigure, "signatureVerify length: " + pkg.getData().length + " must equals " + 256);
                throw new SecurityKeyException(Contants.ERROR_SIGN_INPUT_LEN, 142);
            }
            if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(4)) {
                VLog.w(Contants.TAG, this.mConfigure, "signatureVerify key version is not match current:" + this.mConfigure.getKeyVersion(4) + " target:" + pkg.getKeyVersion());
                if (!updateKey(4)) {
                    VLog.e(Contants.TAG, this.mConfigure, "signatureVerify update key fail");
                    throw new SecurityKeyException("update key fail", 155);
                } else if (pkg.getKeyVersion() != this.mConfigure.getKeyVersion(4)) {
                    VLog.e(Contants.TAG, this.mConfigure, "signatureVerify key version still not match after sync key with server");
                    throw new SecurityKeyException(Contants.ERROR_KEY_NOT_MATCH, 153);
                }
            }
            if (this.mConfigure.getRealCipherMode() != 2 && pkg.getCipherMode() == 2) {
                VLog.e(Contants.TAG, this.mConfigure, "signatureVerify mode is not match need SOFT,device supported TEE,not switch to SOFT");
                throw new SecurityKeyException(Contants.ERROR_MODE_NOT_MATCH, 152);
            } else if (10 == pkg.getType() || 9 == pkg.getType()) {
                VivoSecurityKeyResult result;
                byte[] verifyData = new byte[(inputData.length + pkg.getData().length)];
                System.arraycopy(inputData, 0, verifyData, 0, inputData.length);
                System.arraycopy(pkg.getData(), 0, verifyData, inputData.length, pkg.getData().length);
                int tryTimes = 0;
                do {
                    result = this.mConfigure.getKeystore().vivoSecurityKeyVKVerify(pkg.getCipherMode(), pkg.getKeyVersion(), verifyData);
                    tryTimes++;
                } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_SIGN_VERIFY, tryTimes, result));
                if (result == null) {
                    VLog.e(Contants.TAG, this.mConfigure, "signatureVerify result is null");
                    throw new SecurityKeyException(Contants.ERROR_UNKNOWN, 1000);
                } else if (result.resultCode == 0) {
                    VLog.i(Contants.TAG, this.mConfigure, "signatureVerify consume time: " + (System.currentTimeMillis() - startTime));
                    return true;
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "signatureVerify error: " + result.resultCode);
                    throw new SecurityKeyException(Contants.ERROR_SIGN_VERIFY, result.resultCode);
                }
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "signatureVerify decrypt type " + pkg.getType() + "is not supported");
                throw new SecurityKeyException(Contants.ERROR_TYPE_NOT_SUPPORTED, 154);
            }
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public boolean saveData(byte[] inputData) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start saveData");
        long startTime = System.currentTimeMillis();
        if (inputData == null) {
            VLog.e(Contants.TAG, this.mConfigure, "saveData input data is null");
            throw new SecurityKeyException(Contants.ERROR_INVALID_PARAMS, 102);
        } else if (inputData.length > 2048) {
            VLog.e(Contants.TAG, this.mConfigure, "saveData input data length " + inputData.length + " max length:" + 2048);
            throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_INPUT_LEN, 110);
        } else if (isKeyReady(1)) {
            VivoSecurityKeyResult result;
            int tryTimes = 0;
            do {
                result = this.mConfigure.getKeystore().vivoSecurityKeyWriteData(this.mConfigure.getRealCipherMode(), inputData);
                tryTimes++;
            } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_DATA_SAVE, tryTimes, result));
            if (result == null) {
                VLog.e(Contants.TAG, this.mConfigure, "saveData result is null");
                throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_SAVE, 1000);
            } else if (result.resultCode == 0) {
                VLog.d(Contants.TAG, this.mConfigure, "saveData consume time: " + (System.currentTimeMillis() - startTime));
                return true;
            } else {
                VLog.e(Contants.TAG, this.mConfigure, "saveData error: " + result.resultCode);
                throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_SAVE, result.resultCode);
            }
        } else {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        }
    }

    public byte[] readData() throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "start readData");
        long startTime = System.currentTimeMillis();
        if (isKeyReady(1)) {
            VivoSecurityKeyResult result;
            byte[] readData = null;
            int tryTimes = 0;
            do {
                result = this.mConfigure.getKeystore().vivoSecurityKeyReadData(this.mConfigure.getRealCipherMode());
                tryTimes++;
            } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_DATA_READ, tryTimes, result));
            if (result != null) {
                if (result.resultCode != 0) {
                    VLog.e(Contants.TAG, this.mConfigure, "readData error: " + result.resultCode);
                    if (!(result.resultCode == -16 && result.resultCode == -26)) {
                        throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, result.resultCode);
                    }
                } else if (result.operateData != null) {
                    readData = result.operateData;
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "readData operateData is null");
                    throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, 1000);
                }
                VLog.d(Contants.TAG, this.mConfigure, "readData consume time: " + (System.currentTimeMillis() - startTime));
                return readData;
            }
            VLog.e(Contants.TAG, this.mConfigure, "readData result is null");
            throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, 1000);
        }
        VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
        throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
    }

    public byte[] exportKey(int keyType) throws SecurityKeyException {
        VLog.i(Contants.TAG, this.mConfigure, "export aes key");
        long startTime = System.currentTimeMillis();
        if (!isKeyReady(1)) {
            VLog.e(Contants.TAG, this.mConfigure, "security key cipher is not available");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        } else if (this.mConfigure.getRealCipherMode() != 2) {
            VLog.e(Contants.TAG, this.mConfigure, "Not support key exported");
            throw new SecurityKeyException(Contants.ERROR_NOT_AVAILABLE, 101);
        } else {
            VivoSecurityKeyResult result;
            int tryTimes = 0;
            do {
                result = this.mConfigure.getKeystore().vivoSecurityKeyExportKey(this.mConfigure.getRealCipherMode(), keyType);
                tryTimes++;
            } while (needToRetry(SecurityKeyDataCollection.ACTIONTYPE_DATA_READ, tryTimes, result));
            byte[] keyData = null;
            if (result != null) {
                if (result.resultCode != 0) {
                    VLog.e(Contants.TAG, this.mConfigure, "exportKey error: " + result.resultCode);
                    if (!(result.resultCode == -16 && result.resultCode == -26)) {
                        throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, result.resultCode);
                    }
                } else if (result.operateData != null) {
                    keyData = result.operateData;
                } else {
                    VLog.e(Contants.TAG, this.mConfigure, "exportKey operateData is null");
                    throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, 1000);
                }
                VLog.d(Contants.TAG, this.mConfigure, "exportKey consume time: " + (System.currentTimeMillis() - startTime));
                return keyData;
            }
            VLog.e(Contants.TAG, this.mConfigure, "exportKey result is null");
            throw new SecurityKeyException(Contants.ERROR_SECURITY_STORAGE_READ, 1000);
        }
    }

    public byte[] getProtocolHeader(int packageType) throws SecurityKeyException {
        int keyType = 0;
        switch (packageType) {
            case 5:
                keyType = 1;
                break;
            case 6:
                keyType = 2;
                break;
            case 10:
                keyType = 2;
                break;
        }
        return new ProtocolPackage(this.mConfigure.getToken(), getKeyVersion(keyType), packageType, null).getHeaderbytes();
    }

    public String vivoBase64Encode(byte[] data) {
        return Base64Utils.encode(data);
    }

    public byte[] vivoBase64Decode(String data) {
        return Base64Utils.decode(data);
    }
}
