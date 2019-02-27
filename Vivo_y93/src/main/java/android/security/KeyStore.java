package android.security;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.IKeystoreService.Stub;
import android.security.keymaster.ExportResult;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterBlob;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keymaster.VivoSecurityKeyResult;
import android.security.keystore.KeyExpiredException;
import android.security.keystore.KeyNotYetValidException;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.service.notification.ZenModeConfig;
import android.util.Base64;
import android.util.Log;
import com.vivo.common.VivoCollectData;
import com.vivo.services.cipher.SecurityKeyCipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class KeyStore {
    public static final int FLAG_CRITICAL_TO_DEVICE_ENCRYPTION = 8;
    public static final int FLAG_ENCRYPTED = 1;
    public static final int FLAG_NONE = 0;
    public static final int KEY_NOT_FOUND = 7;
    public static final int LOCKED = 2;
    public static final int NO_ERROR = 1;
    public static final int OP_AUTH_NEEDED = 15;
    public static final int PERMISSION_DENIED = 6;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String PERMISSION_READ_DATA = "com.bbk.iqoo.logsystem.permission.READ_DATA";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String PERMISSION_WRITE_DATA = "com.bbk.iqoo.logsystem.permission.WRITE_DATA";
    public static final int PROTOCOL_ERROR = 5;
    public static final int SYSTEM_ERROR = 4;
    private static final String TAG = "KeyStore";
    public static final int UID_SELF = -1;
    public static final int UNDEFINED_ACTION = 9;
    public static final int UNINITIALIZED = 3;
    public static final int VALUE_CORRUPTED = 8;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_AES128_CBC_ENCRYPT = 100;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_AES_ENCRYPT_MAX_LEN = 4000;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_GET_UNIQUE_ID = 102;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String VIVO_KEYSTORE_AES_LABLE_ID = "2131";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String VIVO_KEYSTORE_ENCRYPT_EVENT_ID = "213";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String VIVO_KEYSTORE_RSA_LABLE_ID = "2132";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_KM_ENCRYPT_ERROR_INPUT_LEN_ZERO = 2;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_KM_ENCRYPT_ERROR_INPUT_NULL = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_KM_ENCRYPT_ERROR_INPUT_OVER_LIMIT = 3;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_KM_ENCRYPT_ERROR_UNKNOW = 100;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_KM_ENCRYPT_SUCCESS = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_RSA2048_ENCRYPT = 101;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_RSA2048_ENCRYPT_MAX_LEN = 256;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_ENCRYPT_TYPE_AES = 1000;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_ENCRYPT_TYPE_RSA = 1001;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_MAX_LEN = 204800;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_RSA_CIPHER_MAX_LEN = 256;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_RSA_PLAIN_MAX_LEN = 245;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURITY_STORAGE_MAX_LEN = 2048;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURYTY_ERROR_BINDER_ERROR = -202;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int VIVO_SECURYTY_ERROR_INPUT_PARAMETER_LEN = -201;
    public static final int WRONG_PASSWORD = 10;
    private final IKeystoreService mBinder;
    private final Context mContext;
    private int mError = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Context mSecurityKeyContext = null;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private String mSecurityKeyToken = null;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private IBinder mToken;

    public enum EncryptType {
        NONE,
        AES,
        RSA,
        END
    }

    public enum KeyType {
        NONE(0),
        EK(1),
        SK(2),
        VK(4),
        END(5);
        
        private int id;

        private KeyType(int id) {
            this.id = id;
        }

        int getId() {
            return this.id;
        }
    }

    public enum OperateType {
        NONE,
        ENCRYPT,
        DECRYPT,
        SIGN,
        VERIFY,
        UPDATE,
        UPDATE_WITH_NAME,
        GET_UNIQUE_ID,
        WRITE_SFS,
        READ_SFS,
        EXPORT_KEY,
        END
    }

    public enum State {
        UNLOCKED,
        LOCKED,
        UNINITIALIZED
    }

    private KeyStore(IKeystoreService binder) {
        this.mBinder = binder;
        this.mContext = getApplicationContext();
    }

    public static Context getApplicationContext() {
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    public static KeyStore getInstance() {
        return new KeyStore(Stub.asInterface(ServiceManager.getService("android.security.keystore")));
    }

    private synchronized IBinder getToken() {
        if (this.mToken == null) {
            this.mToken = new Binder();
        }
        return this.mToken;
    }

    public State state(int userId) {
        try {
            switch (this.mBinder.getState(userId)) {
                case 1:
                    return State.UNLOCKED;
                case 2:
                    return State.LOCKED;
                case 3:
                    return State.UNINITIALIZED;
                default:
                    throw new AssertionError(this.mError);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            throw new AssertionError(e);
        }
    }

    public State state() {
        return state(UserHandle.myUserId());
    }

    public boolean isUnlocked() {
        return state() == State.UNLOCKED;
    }

    public byte[] get(String key, int uid) {
        try {
            return this.mBinder.get(key, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public byte[] get(String key) {
        return get(key, -1);
    }

    public boolean put(String key, byte[] value, int uid, int flags) {
        return insert(key, value, uid, flags) == 1;
    }

    public int insert(String key, byte[] value, int uid, int flags) {
        try {
            return this.mBinder.insert(key, value, uid, flags);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public boolean delete(String key, int uid) {
        boolean z = true;
        try {
            int ret = this.mBinder.del(key, uid);
            if (!(ret == 1 || ret == 7)) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean delete(String key) {
        return delete(key, -1);
    }

    public boolean contains(String key, int uid) {
        boolean z = true;
        try {
            if (this.mBinder.exist(key, uid) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean contains(String key) {
        return contains(key, -1);
    }

    public String[] list(String prefix, int uid) {
        try {
            return this.mBinder.list(prefix, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public String[] list(String prefix) {
        return list(prefix, -1);
    }

    public boolean reset() {
        boolean z = true;
        try {
            if (this.mBinder.reset() != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock(int userId) {
        boolean z = true;
        try {
            if (this.mBinder.lock(userId) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock() {
        return lock(UserHandle.myUserId());
    }

    public boolean unlock(int userId, String password) {
        boolean z = true;
        try {
            this.mError = this.mBinder.unlock(userId, password);
            if (this.mError != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean unlock(String password) {
        return unlock(UserHandle.getUserId(Process.myUid()), password);
    }

    public boolean isEmpty(int userId) {
        boolean z = false;
        try {
            if (this.mBinder.isEmpty(userId) != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isEmpty() {
        return isEmpty(UserHandle.myUserId());
    }

    public boolean generate(String key, int uid, int keyType, int keySize, int flags, byte[][] args) {
        try {
            return this.mBinder.generate(key, uid, keyType, keySize, flags, new KeystoreArguments(args)) == 1;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean importKey(String keyName, byte[] key, int uid, int flags) {
        boolean z = true;
        try {
            if (this.mBinder.import_key(keyName, key, uid, flags) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public byte[] sign(String key, byte[] data) {
        try {
            return this.mBinder.sign(key, data);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean verify(String key, byte[] data, byte[] signature) {
        boolean z = true;
        try {
            if (this.mBinder.verify(key, data, signature) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public String grant(String key, int uid) {
        try {
            String grantAlias = this.mBinder.grant(key, uid);
            if (grantAlias == "") {
                return null;
            }
            return grantAlias;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean ungrant(String key, int uid) {
        boolean z = true;
        try {
            if (this.mBinder.ungrant(key, uid) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public long getmtime(String key, int uid) {
        try {
            long millis = this.mBinder.getmtime(key, uid);
            if (millis == -1) {
                return -1;
            }
            return 1000 * millis;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return -1;
        }
    }

    public long getmtime(String key) {
        return getmtime(key, -1);
    }

    public boolean duplicate(String srcKey, int srcUid, String destKey, int destUid) {
        boolean z = true;
        try {
            if (this.mBinder.duplicate(srcKey, srcUid, destKey, destUid) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isHardwareBacked() {
        return isHardwareBacked(KeyProperties.KEY_ALGORITHM_RSA);
    }

    public boolean isHardwareBacked(String keyType) {
        boolean z = true;
        try {
            if (this.mBinder.is_hardware_backed(keyType.toUpperCase(Locale.US)) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean clearUid(int uid) {
        boolean z = true;
        try {
            if (this.mBinder.clear_uid((long) uid) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int getLastError() {
        return this.mError;
    }

    public boolean addRngEntropy(byte[] data) {
        boolean z = true;
        try {
            if (this.mBinder.addRngEntropy(data) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.generateKey(alias, args, entropy, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int flags, KeyCharacteristics outCharacteristics) {
        return generateKey(alias, args, entropy, -1, flags, outCharacteristics);
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, int uid, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.getKeyCharacteristics(alias, clientId, appId, uid, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, KeyCharacteristics outCharacteristics) {
        return getKeyCharacteristics(alias, clientId, appId, -1, outCharacteristics);
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.importKey(alias, args, format, keyData, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int flags, KeyCharacteristics outCharacteristics) {
        return importKey(alias, args, format, keyData, -1, flags, outCharacteristics);
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId, int uid) {
        try {
            return this.mBinder.exportKey(alias, format, clientId, appId, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId) {
        return exportKey(alias, format, clientId, appId, -1);
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy, int uid) {
        try {
            return this.mBinder.begin(getToken(), alias, purpose, pruneable, args, entropy, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy) {
        return begin(alias, purpose, pruneable, args, entropy, -1);
    }

    public OperationResult update(IBinder token, KeymasterArguments arguments, byte[] input) {
        try {
            return this.mBinder.update(token, arguments, input);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature, byte[] entropy) {
        try {
            return this.mBinder.finish(token, arguments, signature, entropy);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature) {
        return finish(token, arguments, signature, null);
    }

    public int abort(IBinder token) {
        try {
            return this.mBinder.abort(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public boolean isOperationAuthorized(IBinder token) {
        try {
            return this.mBinder.isOperationAuthorized(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int addAuthToken(byte[] authToken) {
        try {
            return this.mBinder.addAuthToken(authToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public boolean onUserPasswordChanged(int userId, String newPassword) {
        boolean z = true;
        if (newPassword == null) {
            newPassword = "";
        }
        try {
            if (this.mBinder.onUserPasswordChanged(userId, newPassword) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public void onUserAdded(int userId, int parentId) {
        try {
            this.mBinder.onUserAdded(userId, parentId);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public void onUserAdded(int userId) {
        onUserAdded(userId, -1);
    }

    public void onUserRemoved(int userId) {
        try {
            this.mBinder.onUserRemoved(userId);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public boolean onUserPasswordChanged(String newPassword) {
        return onUserPasswordChanged(UserHandle.getUserId(Process.myUid()), newPassword);
    }

    public int attestKey(String alias, KeymasterArguments params, KeymasterCertificateChain outChain) {
        try {
            return this.mBinder.attestKey(alias, params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int attestDeviceIds(KeymasterArguments params, KeymasterCertificateChain outChain) {
        try {
            return this.mBinder.attestDeviceIds(params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public void onDeviceOffBody() {
        try {
            this.mBinder.onDeviceOffBody();
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public static KeyStoreException getKeyStoreException(int errorCode) {
        if (errorCode > 0) {
            switch (errorCode) {
                case 1:
                    return new KeyStoreException(errorCode, "OK");
                case 2:
                    return new KeyStoreException(errorCode, "User authentication required");
                case 3:
                    return new KeyStoreException(errorCode, "Keystore not initialized");
                case 4:
                    return new KeyStoreException(errorCode, "System error");
                case 6:
                    return new KeyStoreException(errorCode, "Permission denied");
                case 7:
                    return new KeyStoreException(errorCode, "Key not found");
                case 8:
                    return new KeyStoreException(errorCode, "Key blob corrupted");
                case 15:
                    return new KeyStoreException(errorCode, "Operation requires authorization");
                default:
                    return new KeyStoreException(errorCode, String.valueOf(errorCode));
            }
        }
        switch (errorCode) {
            case -16:
                return new KeyStoreException(errorCode, "Invalid user authentication validity duration");
            default:
                return new KeyStoreException(errorCode, KeymasterDefs.getErrorMessage(errorCode));
        }
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, KeyStoreException e) {
        switch (e.getErrorCode()) {
            case -26:
            case 15:
                KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
                int getKeyCharacteristicsErrorCode = getKeyCharacteristics(keystoreKeyAlias, null, null, uid, keyCharacteristics);
                if (getKeyCharacteristicsErrorCode != 1) {
                    return new InvalidKeyException("Failed to obtained key characteristics", getKeyStoreException(getKeyCharacteristicsErrorCode));
                }
                List<BigInteger> keySids = keyCharacteristics.getUnsignedLongs(KeymasterDefs.KM_TAG_USER_SECURE_ID);
                if (keySids.isEmpty()) {
                    return new KeyPermanentlyInvalidatedException();
                }
                long rootSid;
                if (keyCharacteristics.getBoolean(KeymasterDefs.KM_TAG_SOTER_IS_FROM_SOTER)) {
                    rootSid = GateKeeper.getSecureUserIdForSoter();
                } else {
                    rootSid = GateKeeper.getSecureUserId();
                }
                if (rootSid != 0 && keySids.contains(KeymasterArguments.toUint64(rootSid))) {
                    return new UserNotAuthenticatedException();
                }
                long fingerprintOnlySid = getFingerprintOnlySid();
                if (fingerprintOnlySid == 0 || !keySids.contains(KeymasterArguments.toUint64(fingerprintOnlySid))) {
                    return new KeyPermanentlyInvalidatedException();
                }
                return new UserNotAuthenticatedException();
            case -25:
                return new KeyExpiredException();
            case -24:
                return new KeyNotYetValidException();
            case 2:
                return new UserNotAuthenticatedException();
            case 3:
                return new KeyPermanentlyInvalidatedException();
            default:
                return new InvalidKeyException("Keystore operation failed", e);
        }
    }

    private long getFingerprintOnlySid() {
        FingerprintManager fingerprintManager = (FingerprintManager) this.mContext.getSystemService(FingerprintManager.class);
        if (fingerprintManager == null) {
            return 0;
        }
        return fingerprintManager.getAuthenticatorId();
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, int errorCode) {
        return getInvalidKeyException(keystoreKeyAlias, uid, getKeyStoreException(errorCode));
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private byte[] vivoKeyStoreEncrypt(int encryptType, byte[] plaintData) {
        try {
            return this.mBinder.vivo_keystore_encrypt(encryptType, plaintData);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    private boolean vivoKeyStorecheckPermission() {
        if (this.mContext != null && this.mContext.checkCallingOrSelfPermission("com.bbk.iqoo.logsystem.permission.READ_DATA") == 0 && this.mContext.checkCallingOrSelfPermission("com.bbk.iqoo.logsystem.permission.WRITE_DATA") == 0) {
            Log.d(TAG, "check collect data permisson: PERMISSION_GRANTED");
            return true;
        }
        Log.d(TAG, "check collect data permisson: PERMISSION_DENIED");
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public byte[] vivoAESEncrypt(byte[] plaintData) throws Exception {
        int errorCode;
        byte[] cipherdata = null;
        if (plaintData == null) {
            errorCode = 1;
        } else if (plaintData.length == 0) {
            errorCode = 2;
        } else if (plaintData.length > 4000) {
            errorCode = 3;
        } else {
            cipherdata = vivoKeyStoreEncrypt(100, plaintData);
            if (cipherdata == null) {
                errorCode = 100;
            } else if (cipherdata.length == 4) {
                errorCode = (((cipherdata[0] & 255) | ((cipherdata[1] & 255) << 8)) | ((cipherdata[2] & 255) << 16)) | ((cipherdata[3] & 255) << 24);
            } else {
                errorCode = 0;
            }
        }
        Log.d(TAG, "errorCode: " + errorCode);
        final String errorString = Integer.toString(errorCode);
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (KeyStore.this.vivoKeyStorecheckPermission()) {
                        VivoCollectData mVivoCollectData = new VivoCollectData(KeyStore.this.mContext);
                        HashMap<String, String> mCollectInfoMap = new HashMap();
                        Log.d(KeyStore.TAG, "getControlInfo before");
                        if (mVivoCollectData != null && mCollectInfoMap != null && mVivoCollectData.getControlInfo(KeyStore.VIVO_KEYSTORE_ENCRYPT_EVENT_ID)) {
                            mCollectInfoMap.put("aes_e", errorString);
                            Log.d(KeyStore.TAG, "start writeData");
                            mVivoCollectData.writeData(KeyStore.VIVO_KEYSTORE_ENCRYPT_EVENT_ID, KeyStore.VIVO_KEYSTORE_AES_LABLE_ID, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, mCollectInfoMap);
                        }
                    }
                } catch (Exception e) {
                    Log.w(KeyStore.TAG, "Exception: " + e);
                }
            }
        }).start();
        if (errorCode != 0 || cipherdata == null) {
            return null;
        }
        return cipherdata;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public byte[] vivoRSAEncrypt(byte[] plaintData) throws Exception {
        int errorCode;
        byte[] cipherdata = null;
        if (plaintData == null) {
            errorCode = 1;
        } else if (plaintData.length == 0) {
            errorCode = 2;
        } else if (plaintData.length > 245) {
            errorCode = 3;
        } else {
            cipherdata = vivoKeyStoreEncrypt(101, plaintData);
            if (cipherdata == null) {
                errorCode = 100;
            } else if (cipherdata.length == 4) {
                errorCode = (((cipherdata[0] & 255) | ((cipherdata[1] & 255) << 8)) | ((cipherdata[2] & 255) << 16)) | ((cipherdata[3] & 255) << 24);
                Log.d(TAG, "error: " + errorCode);
            } else {
                errorCode = 0;
            }
        }
        Log.d(TAG, "errorCode: " + errorCode);
        final String errorString = Integer.toString(errorCode);
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (KeyStore.this.vivoKeyStorecheckPermission()) {
                        HashMap<String, String> mCollectInfoMap = new HashMap();
                        VivoCollectData mVivoCollectData = new VivoCollectData(KeyStore.this.mContext);
                        Log.d(KeyStore.TAG, "getControlInfo before");
                        if (mVivoCollectData != null && mCollectInfoMap != null && mVivoCollectData.getControlInfo(KeyStore.VIVO_KEYSTORE_ENCRYPT_EVENT_ID)) {
                            Log.d(KeyStore.TAG, "start writeData");
                            mCollectInfoMap.put("rsa_e", errorString);
                            mVivoCollectData.writeData(KeyStore.VIVO_KEYSTORE_ENCRYPT_EVENT_ID, KeyStore.VIVO_KEYSTORE_RSA_LABLE_ID, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, mCollectInfoMap);
                        }
                    }
                } catch (Exception e) {
                    Log.w(KeyStore.TAG, "Exception: " + e);
                }
            }
        }).start();
        if (errorCode != 0 || cipherdata == null) {
            return null;
        }
        return cipherdata;
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0078 A:{SYNTHETIC, Splitter: B:48:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004e A:{SYNTHETIC, Splitter: B:25:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x006a A:{SYNTHETIC, Splitter: B:41:0x006a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String vivoKeystoreGetUniqueID() {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        byte[] uid = vivoKeyStoreEncrypt(102, null);
        if (uid != null && uid.length > 10) {
            return new String(uid);
        }
        BufferedReader reader2;
        String emmcId;
        try {
            reader2 = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/cid"));
            try {
                emmcId = reader2.readLine();
                if (emmcId != null) {
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    return emmcId;
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                try {
                    reader = new BufferedReader(new FileReader("/sys/ufs/ufsid"));
                    try {
                        emmcId = reader.readLine();
                        if (emmcId == null) {
                            try {
                                reader.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                            return emmcId;
                        }
                        try {
                            reader.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                        return null;
                    } catch (IOException e3) {
                        e2222 = e3;
                    }
                } catch (IOException e4) {
                    e2222 = e4;
                    reader = reader2;
                    try {
                        e2222.printStackTrace();
                        try {
                            reader.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            reader.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    reader.close();
                    throw th;
                }
            } catch (IOException e5) {
                e222222 = e5;
                reader = reader2;
            } catch (Throwable th4) {
                th = th4;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e6) {
            e222222 = e6;
            try {
                e222222.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2222222) {
                        e2222222.printStackTrace();
                    }
                }
                reader2 = reader;
                reader = new BufferedReader(new FileReader("/sys/ufs/ufsid"));
                emmcId = reader.readLine();
                if (emmcId == null) {
                }
            } catch (Throwable th5) {
                th = th5;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e22222222) {
                        e22222222.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    public boolean isSystemCredentialEmpty(int userId) {
        boolean z = false;
        try {
            if (this.mBinder.isSystemCredentialEmpty(userId) != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isSystemCredentialEmpty() {
        boolean result = isSystemCredentialEmpty(UserHandle.myUserId());
        Log.d(TAG, "isSystemCredentialEmpty result:" + result);
        return result;
    }

    private String getAppPackageName() {
        return this.mSecurityKeyToken;
    }

    private String getAppSignaturePublicKey() {
        if (this.mSecurityKeyContext == null) {
            Log.e(TAG, "SecurityKeyContext is null");
            return null;
        }
        String pkgname = this.mSecurityKeyContext.getPackageName();
        PackageManager manager = this.mSecurityKeyContext.getPackageManager();
        if (!(pkgname == null || manager == null)) {
            try {
                PackageInfo packageInfo = manager.getPackageInfo(pkgname, 64);
                if (packageInfo != null && packageInfo.signatures.length > 0) {
                    PublicKey publicKey = packageInfo.signatures[0].getPublicKey();
                    if (publicKey != null) {
                        return new String(Base64.encode(publicKey.getEncoded(), 0));
                    }
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "NameNotFoundException", e);
            } catch (CertificateException e2) {
                Log.e(TAG, "CertificateException", e2);
            }
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyEKEncrypt(int mode, byte[] plainData) {
        return vivoSecurityKeyEncrypt(mode, EncryptType.AES.ordinal(), KeyType.EK.getId(), null, plainData);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyVKEncrypt(int mode, byte[] plainData) {
        return vivoSecurityKeyEncrypt(mode, EncryptType.RSA.ordinal(), KeyType.VK.getId(), null, plainData);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private VivoSecurityKeyResult vivoSecurityKeyEncrypt(int mode, int encryptType, int keyType, String keyName, byte[] plainData) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (plainData == null || plainData.length == 0 || ((encryptType == EncryptType.AES.ordinal() && plainData.length > 204800) || (encryptType == EncryptType.RSA.ordinal() && plainData.length > 245))) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.ENCRYPT.ordinal(), encryptType, keyType, packageName, 0, keyName, publickey, plainData);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyEKDecrypt(int mode, int keyVersion, byte[] cipherData) {
        return vivoSecurityKeyDecrypt(mode, EncryptType.AES.ordinal(), KeyType.EK.getId(), keyVersion, null, cipherData);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeySKDecrypt(int mode, int keyVersion, byte[] cipherData) {
        return vivoSecurityKeyDecrypt(mode, EncryptType.RSA.ordinal(), KeyType.SK.getId(), keyVersion, null, cipherData);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private VivoSecurityKeyResult vivoSecurityKeyDecrypt(int mode, int encryptType, int keyType, int keyVersion, String keyName, byte[] cipherData) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (cipherData == null || cipherData.length == 0 || ((encryptType == EncryptType.AES.ordinal() && cipherData.length > SecurityKeyCipher.AES_DECRYPT_DATA_SIZE_MAX) || (encryptType == EncryptType.RSA.ordinal() && cipherData.length > 256))) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.DECRYPT.ordinal(), encryptType, keyType, packageName, keyVersion, keyName, publickey, cipherData);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeySKSign(int mode, byte[] signatureData) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (signatureData == null || signatureData.length == 0 || signatureData.length > 204800) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.SIGN.ordinal(), 0, KeyType.SK.ordinal(), packageName, 0, null, publickey, signatureData);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyVKVerify(int mode, int keyVersion, byte[] signedData) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (signedData == null || signedData.length == 0 || signedData.length > 205056) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.VERIFY.ordinal(), 0, KeyType.VK.ordinal(), packageName, keyVersion, null, publickey, signedData);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyUpdate(int mode, int keyType, byte[] keyData) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (keyData == null) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.UPDATE.ordinal(), 0, keyType, packageName, 0, null, publickey, keyData);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyGetDeviceInfo(int mode) {
        VivoSecurityKeyResult result;
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.GET_UNIQUE_ID.ordinal(), 0, 0, packageName, 0, null, publickey, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        } catch (Exception e2) {
            Log.e(TAG, "Exception occur:" + e2.getMessage());
            e2.printStackTrace();
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode + " uniqueId:" + result.uniqueId);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyWriteData(int mode, byte[] data) {
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        if (data == null || data.length == 0 || data.length > 2048) {
            Log.e(TAG, "input data error");
            return new VivoSecurityKeyResult(-201);
        }
        VivoSecurityKeyResult result;
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.WRITE_SFS.ordinal(), 0, 0, packageName, 0, null, publickey, data);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyReadData(int mode) {
        VivoSecurityKeyResult result;
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.READ_SFS.ordinal(), 0, 0, packageName, 0, null, publickey, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public VivoSecurityKeyResult vivoSecurityKeyExportKey(int mode, int keyType) {
        VivoSecurityKeyResult result;
        String publickey = getAppSignaturePublicKey();
        String packageName = getAppPackageName();
        try {
            result = this.mBinder.vivoSecurityKeyOperate(mode, OperateType.EXPORT_KEY.ordinal(), 0, keyType, packageName, 0, null, publickey, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot connect to keystore", e);
            result = new VivoSecurityKeyResult(-202);
        }
        if (result != null) {
            Log.d(TAG, "result.resultCode = " + result.resultCode);
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean vivoSecurityKeyInit(Context context, String serviceName) {
        if (context == null) {
            return false;
        }
        this.mSecurityKeyContext = context;
        this.mSecurityKeyToken = context.getPackageName();
        if (this.mSecurityKeyToken.equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
            if (serviceName == null) {
                return false;
            }
            this.mSecurityKeyToken += "." + serviceName;
        }
        return true;
    }

    private boolean isSystemApplication() {
        if (this.mContext == null) {
            return false;
        }
        String pkgname = this.mContext.getPackageName();
        PackageManager manager = this.mContext.getPackageManager();
        if (!(pkgname == null || manager == null)) {
            try {
                PackageInfo packageInfo = manager.getPackageInfo(pkgname, 64);
                if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 1) == 0)) {
                    Log.d(TAG, "is system app");
                    return true;
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "NameNotFoundException", e);
            }
        }
        return false;
    }

    private String vivoGetAppSignaturePublicKey() {
        if (this.mContext == null) {
            return null;
        }
        String pkgname = this.mContext.getPackageName();
        PackageManager manager = this.mContext.getPackageManager();
        if (!(pkgname == null || manager == null)) {
            try {
                PackageInfo packageInfo = manager.getPackageInfo(pkgname, 64);
                if (packageInfo != null && packageInfo.signatures.length > 0) {
                    PublicKey publicKey = packageInfo.signatures[0].getPublicKey();
                    if (publicKey != null) {
                        return new String(Base64.encode(publicKey.getEncoded(), 0));
                    }
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "NameNotFoundException", e);
            } catch (CertificateException e2) {
                Log.e(TAG, "CertificateException", e2);
            }
        }
        return null;
    }

    private String vivoGetAppPackageName() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getPackageName();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean vivoKeyStoreWriteString(String string) {
        int uid = Process.myUid();
        if (string == null || string.length() == 0 || string.length() > 100) {
            Log.d(TAG, "string error");
            return false;
        }
        String signature = vivoGetAppSignaturePublicKey();
        if (signature == null) {
            Log.d(TAG, "signature is null");
            return false;
        }
        String packageName = vivoGetAppPackageName();
        if (packageName == null) {
            Log.d(TAG, "signature is null");
            return false;
        }
        try {
            if (this.mBinder.vivoKeyStoreWriteString(uid, packageName.getBytes(), signature.getBytes(), string.getBytes()) == 1) {
                return true;
            }
            Log.d(TAG, "write fail");
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public String vivoKeyStoreGetString() {
        int uid = Process.myUid();
        String signature = vivoGetAppSignaturePublicKey();
        if (signature == null) {
            Log.d(TAG, "signature is null");
            return null;
        }
        String packageName = vivoGetAppPackageName();
        if (packageName == null) {
            Log.d(TAG, "signature is null");
            return null;
        }
        try {
            byte[] result = this.mBinder.vivoKeyStoreReadString(uid, packageName.getBytes(), signature.getBytes());
            if (result != null) {
                return new String(result);
            }
            Log.d(TAG, "result is null");
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean vivoKeyStoreRemoveString() {
        int uid = Process.myUid();
        String signature = vivoGetAppSignaturePublicKey();
        if (signature == null) {
            Log.d(TAG, "signature is null");
            return false;
        }
        String packageName = vivoGetAppPackageName();
        if (packageName == null) {
            Log.d(TAG, "signature is null");
            return false;
        }
        try {
            if (this.mBinder.vivoKeyStoreRemoveString(uid, packageName.getBytes(), signature.getBytes()) == 1) {
                return true;
            }
            Log.d(TAG, "remove fail");
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }
}
