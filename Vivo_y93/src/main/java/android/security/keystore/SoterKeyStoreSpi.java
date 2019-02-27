package android.security.keystore;

import android.security.Credentials;
import android.security.KeyStore;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class SoterKeyStoreSpi extends AndroidKeyStoreSpi {
    private KeyStore mKeyStore;

    public SoterKeyStoreSpi() {
        this.mKeyStore = null;
        this.mKeyStore = KeyStore.getInstance();
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (isPrivateKeyEntry(alias)) {
            String privateKeyAlias = Credentials.USER_PRIVATE_KEY + alias;
            if (password == null || !"from_soter_ui".equals(String.valueOf(password))) {
                return SoterKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(this.mKeyStore, privateKeyAlias);
            }
            return SoterKeyStoreProvider.loadJsonPublicKeyFromKeystore(this.mKeyStore, privateKeyAlias);
        } else if (!isSecretKeyEntry(alias)) {
            return null;
        } else {
            return AndroidKeyStoreProvider.loadAndroidKeyStoreSecretKeyFromKeystore(this.mKeyStore, Credentials.USER_SECRET_KEY + alias, -1);
        }
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            return this.mKeyStore.contains(Credentials.USER_PRIVATE_KEY + alias);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isSecretKeyEntry(String alias) {
        if (alias != null) {
            return this.mKeyStore.contains(Credentials.USER_SECRET_KEY + alias);
        }
        throw new NullPointerException("alias == null");
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (engineContainsAlias(alias) && (this.mKeyStore.delete(Credentials.USER_PRIVATE_KEY + alias) | this.mKeyStore.delete(Credentials.USER_CERTIFICATE + alias)) == 0) {
            throw new KeyStoreException("Failed to delete entry: " + alias);
        }
    }

    public boolean engineContainsAlias(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (this.mKeyStore.contains(Credentials.USER_PRIVATE_KEY + alias)) {
            return true;
        } else {
            return this.mKeyStore.contains(Credentials.USER_CERTIFICATE + alias);
        }
    }
}
