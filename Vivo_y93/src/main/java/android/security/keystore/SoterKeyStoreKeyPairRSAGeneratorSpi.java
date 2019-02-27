package android.security.keystore;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.security.Credentials;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.Digest;
import android.util.Log;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.ArrayUtils;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERInteger;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.X509CertificateObject;
import com.android.org.bouncycastle.x509.X509V3CertificateGenerator;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.HashSet;
import java.util.Set;
import libcore.util.EmptyArray;

public class SoterKeyStoreKeyPairRSAGeneratorSpi extends KeyPairGeneratorSpi {
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 8192;
    private static final int RSA_MIN_KEY_SIZE = 512;
    public static final long UINT32_MAX_VALUE = 4294967295L;
    private static final long UINT32_RANGE = 4294967296L;
    public static final BigInteger UINT64_MAX_VALUE = UINT64_RANGE.subtract(BigInteger.ONE);
    private static final BigInteger UINT64_RANGE = BigInteger.ONE.shiftLeft(64);
    private static volatile SecureRandom sRng;
    private boolean isAutoAddCounterWhenGetPublicKey = false;
    private boolean isAutoSignedWithAttkWhenGetPublicKey = false;
    private boolean isAutoSignedWithCommonkWhenGetPublicKey = false;
    private boolean isForSoter = false;
    private boolean isNeedNextAttk = false;
    private boolean isSecmsgFidCounterSignedWhenSign = false;
    private String mAutoSignedKeyNameWhenGetPublicKey = "";
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private String mJcaKeyAlgorithm;
    private int mKeySizeBits;
    private KeyStore mKeyStore;
    private int mKeymasterAlgorithm = -1;
    private int[] mKeymasterBlockModes;
    private int[] mKeymasterDigests;
    private int[] mKeymasterEncryptionPaddings;
    private int[] mKeymasterPurposes;
    private int[] mKeymasterSignaturePaddings;
    private final int mOriginalKeymasterAlgorithm = 1;
    private BigInteger mRSAPublicExponent;
    private SecureRandom mRng;
    private KeyGenParameterSpec mSpec;

    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(KeyGenParameterSpec.class.getName() + " required to initialize this KeyPairGenerator");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void, dom blocks: [B:2:0x0006, B:22:0x00db]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1249)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0157 A:{ExcHandler: java.lang.IllegalArgumentException (r2_0 'e' java.lang.RuntimeException), Splitter: B:22:0x00db} */
    public void initialize(java.security.spec.AlgorithmParameterSpec r17, java.security.SecureRandom r18) throws java.security.InvalidAlgorithmParameterException {
        /*
        r16 = this;
        r16.resetAll();
        r15 = 0;
        if (r17 != 0) goto L_0x003e;
    L_0x0006:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0037 }
        r4.<init>();	 Catch:{ all -> 0x0037 }
        r5 = "Must supply params of type ";	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0037 }
        r5 = r5.getName();	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = " or ";	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0037 }
        r5 = r5.getName();	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r4 = r4.toString();	 Catch:{ all -> 0x0037 }
        r3.<init>(r4);	 Catch:{ all -> 0x0037 }
        throw r3;	 Catch:{ all -> 0x0037 }
    L_0x0037:
        r3 = move-exception;
        if (r15 != 0) goto L_0x003d;
    L_0x003a:
        r16.resetAll();
    L_0x003d:
        throw r3;
    L_0x003e:
        r10 = 0;
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r12 = r0.mOriginalKeymasterAlgorithm;	 Catch:{ all -> 0x0037 }
        r0 = r17;	 Catch:{ all -> 0x0037 }
        r3 = r0 instanceof android.security.keystore.KeyGenParameterSpec;	 Catch:{ all -> 0x0037 }
        if (r3 == 0) goto L_0x0097;	 Catch:{ all -> 0x0037 }
    L_0x0049:
        r0 = r17;	 Catch:{ all -> 0x0037 }
        r0 = (android.security.keystore.KeyGenParameterSpec) r0;	 Catch:{ all -> 0x0037 }
        r14 = r0;	 Catch:{ all -> 0x0037 }
        r3 = r14.getKeystoreAlias();	 Catch:{ all -> 0x0037 }
        r3 = android.security.keystore.SoterUtil.getPureKeyAliasFromKeyName(r3);	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mEntryAlias = r3;	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mSpec = r14;	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mKeymasterAlgorithm = r12;	 Catch:{ all -> 0x0037 }
        r3 = 0;	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mEncryptionAtRestRequired = r3;	 Catch:{ all -> 0x0037 }
        r3 = r14.getKeySize();	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mKeySizeBits = r3;	 Catch:{ all -> 0x0037 }
        r16.initAlgorithmSpecificParameters();	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r3 = r0.mKeySizeBits;	 Catch:{ all -> 0x0037 }
        r4 = -1;	 Catch:{ all -> 0x0037 }
        if (r3 != r4) goto L_0x0081;	 Catch:{ all -> 0x0037 }
    L_0x0079:
        r3 = getDefaultKeySize(r12);	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mKeySizeBits = r3;	 Catch:{ all -> 0x0037 }
    L_0x0081:
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r3 = r0.mKeySizeBits;	 Catch:{ all -> 0x0037 }
        checkValidKeySize(r12, r3);	 Catch:{ all -> 0x0037 }
        r3 = r14.getKeystoreAlias();	 Catch:{ all -> 0x0037 }
        if (r3 != 0) goto L_0x00db;	 Catch:{ all -> 0x0037 }
    L_0x008e:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r4 = "KeyStore entry alias not provided";	 Catch:{ all -> 0x0037 }
        r3.<init>(r4);	 Catch:{ all -> 0x0037 }
        throw r3;	 Catch:{ all -> 0x0037 }
    L_0x0097:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0037 }
        r4.<init>();	 Catch:{ all -> 0x0037 }
        r5 = "Unsupported params class: ";	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = r17.getClass();	 Catch:{ all -> 0x0037 }
        r5 = r5.getName();	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = ". Supported: ";	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ all -> 0x0037 }
        r5 = r5.getName();	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = ", ";	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r5 = android.security.KeyPairGeneratorSpec.class;	 Catch:{ all -> 0x0037 }
        r5 = r5.getName();	 Catch:{ all -> 0x0037 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0037 }
        r4 = r4.toString();	 Catch:{ all -> 0x0037 }
        r3.<init>(r4);	 Catch:{ all -> 0x0037 }
        throw r3;	 Catch:{ all -> 0x0037 }
    L_0x00db:
        r11 = android.security.keystore.KeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(r12);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r14.getPurposes();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = android.security.keystore.KeyProperties.Purpose.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterPurposes = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r14.getBlockModes();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = android.security.keystore.KeyProperties.BlockMode.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterBlockModes = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r14.getEncryptionPaddings();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = android.security.keystore.KeyProperties.EncryptionPadding.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterEncryptionPaddings = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r14.getPurposes();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r3 & 1;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        if (r3 == 0) goto L_0x0161;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x010b:
        r3 = r14.isRandomizedEncryptionRequired();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        if (r3 == 0) goto L_0x0161;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0111:
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r0.mKeymasterEncryptionPaddings;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = 0;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = r4.length;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0117:
        if (r3 >= r5) goto L_0x0161;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0119:
        r13 = r4[r3];	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r6 = isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(r13);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        if (r6 != 0) goto L_0x015e;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0121:
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4.<init>();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = "Randomized encryption (IND-CPA) required but may be violated by padding scheme: ";	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = android.security.keystore.KeyProperties.EncryptionPadding.fromKeymaster(r13);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = ". See ";	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = android.security.keystore.KeyGenParameterSpec.class;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = r5.getName();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = " documentation.";	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.append(r5);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.toString();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3.<init>(r4);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        throw r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0157:
        r2 = move-exception;
        r3 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x0037 }
        r3.<init>(r2);	 Catch:{ all -> 0x0037 }
        throw r3;	 Catch:{ all -> 0x0037 }
    L_0x015e:
        r3 = r3 + 1;
        goto L_0x0117;
    L_0x0161:
        r3 = r14.getSignaturePaddings();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = android.security.keystore.KeyProperties.SignaturePadding.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterSignaturePaddings = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = r14.isDigestsSpecified();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        if (r3 == 0) goto L_0x01b4;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x0173:
        r3 = r14.getDigests();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3 = android.security.keystore.KeyProperties.Digest.allToKeymaster(r3);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterDigests = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
    L_0x017f:
        r3 = new android.security.keymaster.KeymasterArguments;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r3.<init>();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r4 = r4.isUserAuthenticationRequired();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = r0.mSpec;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r5 = r5.getUserAuthenticationValidityDurationSeconds();	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r6 = 0;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r7 = 0;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r8 = 0;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        android.security.keystore.KeymasterUtils.addUserAuthArgs(r3, r4, r5, r6, r7, r8);	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mJcaKeyAlgorithm = r11;	 Catch:{ all -> 0x0037 }
        r0 = r18;	 Catch:{ all -> 0x0037 }
        r1 = r16;	 Catch:{ all -> 0x0037 }
        r1.mRng = r0;	 Catch:{ all -> 0x0037 }
        r3 = android.security.KeyStore.getInstance();	 Catch:{ all -> 0x0037 }
        r0 = r16;	 Catch:{ all -> 0x0037 }
        r0.mKeyStore = r3;	 Catch:{ all -> 0x0037 }
        r15 = 1;
        if (r15 != 0) goto L_0x01b3;
    L_0x01b0:
        r16.resetAll();
    L_0x01b3:
        return;
    L_0x01b4:
        r3 = libcore.util.EmptyArray.INT;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0 = r16;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        r0.mKeymasterDigests = r3;	 Catch:{ IllegalArgumentException -> 0x0157, IllegalArgumentException -> 0x0157 }
        goto L_0x017f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterKeyStoreKeyPairRSAGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void");
    }

    private void resetAll() {
        this.mEntryAlias = null;
        this.mJcaKeyAlgorithm = null;
        this.mKeymasterAlgorithm = -1;
        this.mKeymasterPurposes = null;
        this.mKeymasterBlockModes = null;
        this.mKeymasterEncryptionPaddings = null;
        this.mKeymasterSignaturePaddings = null;
        this.mKeymasterDigests = null;
        this.mKeySizeBits = 0;
        this.mSpec = null;
        this.mRSAPublicExponent = null;
        this.mEncryptionAtRestRequired = false;
        this.mRng = null;
        this.mKeyStore = null;
        this.isForSoter = false;
        this.isAutoSignedWithAttkWhenGetPublicKey = false;
        this.isAutoSignedWithCommonkWhenGetPublicKey = false;
        this.mAutoSignedKeyNameWhenGetPublicKey = "";
        this.isSecmsgFidCounterSignedWhenSign = false;
        this.isAutoAddCounterWhenGetPublicKey = false;
        this.isNeedNextAttk = false;
    }

    private void initAlgorithmSpecificParameters() throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algSpecificSpec = this.mSpec.getAlgorithmParameterSpec();
        BigInteger publicExponent = RSAKeyGenParameterSpec.F4;
        if (algSpecificSpec instanceof RSAKeyGenParameterSpec) {
            RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algSpecificSpec;
            if (this.mKeySizeBits == -1) {
                this.mKeySizeBits = rsaSpec.getKeysize();
            } else if (this.mKeySizeBits != rsaSpec.getKeysize()) {
                throw new InvalidAlgorithmParameterException("RSA key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + rsaSpec.getKeysize());
            }
            publicExponent = rsaSpec.getPublicExponent();
            if (publicExponent.compareTo(BigInteger.ZERO) < 1) {
                throw new InvalidAlgorithmParameterException("RSA public exponent must be positive: " + publicExponent);
            } else if (publicExponent.compareTo(UINT64_MAX_VALUE) > 0) {
                throw new InvalidAlgorithmParameterException("Unsupported RSA public exponent: " + publicExponent + ". Maximum supported value: " + UINT64_MAX_VALUE);
            }
        }
        this.mRSAPublicExponent = publicExponent;
        SoterRSAKeyGenParameterSpec soterSpec = SoterUtil.convertKeyNameToParameterSpec(this.mSpec.getKeystoreAlias());
        if (soterSpec != null) {
            this.isForSoter = soterSpec.isForSoter();
            this.isAutoSignedWithAttkWhenGetPublicKey = soterSpec.isAutoSignedWithAttkWhenGetPublicKey();
            this.isAutoSignedWithCommonkWhenGetPublicKey = soterSpec.isAutoSignedWithCommonkWhenGetPublicKey();
            this.mAutoSignedKeyNameWhenGetPublicKey = soterSpec.getAutoSignedKeyNameWhenGetPublicKey();
            this.isSecmsgFidCounterSignedWhenSign = soterSpec.isSecmsgFidCounterSignedWhenSign();
            this.isAutoAddCounterWhenGetPublicKey = soterSpec.isAutoAddCounterWhenGetPublicKey();
            this.isNeedNextAttk = soterSpec.isNeedUseNextAttk();
        }
    }

    public KeyPair generateKeyPair() {
        if (this.mKeyStore == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        int flags = this.mEncryptionAtRestRequired ? 1 : 0;
        if ((flags & 1) == 0 || this.mKeyStore.state() == State.UNLOCKED) {
            KeymasterArguments args = new KeymasterArguments();
            args.addUnsignedInt(KeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
            args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
            args.addEnums(KeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
            args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
            args.addEnums(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterEncryptionPaddings);
            args.addEnums(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterSignaturePaddings);
            args.addEnums(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
            if (this.isForSoter) {
                args.addBoolean(KeymasterDefs.KM_TAG_SOTER_IS_FROM_SOTER);
            }
            KeymasterUtils.addUserAuthArgs(args, this.mSpec.isUserAuthenticationRequired(), this.mSpec.getUserAuthenticationValidityDurationSeconds(), false, false, 0);
            if (this.mSpec.getKeyValidityStart() != null) {
                args.addDate(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, this.mSpec.getKeyValidityStart());
            }
            if (this.mSpec.getKeyValidityForOriginationEnd() != null) {
                args.addDate(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, this.mSpec.getKeyValidityForOriginationEnd());
            }
            if (this.mSpec.getKeyValidityForConsumptionEnd() != null) {
                args.addDate(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, this.mSpec.getKeyValidityForConsumptionEnd());
            }
            addAlgorithmSpecificParameters(args);
            byte[] additionalEntropy = getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            String privateKeyAlias = Credentials.USER_PRIVATE_KEY + this.mEntryAlias;
            try {
                Credentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias);
                int errorCode = this.mKeyStore.generateKey(privateKeyAlias, args, additionalEntropy, flags, new KeyCharacteristics());
                if (errorCode != 1) {
                    throw new ProviderException("Failed to generate key pair", KeyStore.getKeyStoreException(errorCode));
                }
                KeyPair result = SoterKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(this.mKeyStore, privateKeyAlias);
                if (this.mJcaKeyAlgorithm.equalsIgnoreCase(result.getPrivate().getAlgorithm())) {
                    if (this.mKeyStore.put(Credentials.USER_CERTIFICATE + this.mEntryAlias, generateSelfSignedCertificate(result.getPrivate(), result.getPublic()).getEncoded(), -1, flags)) {
                        if (!true) {
                            Credentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias);
                        }
                        return result;
                    }
                    throw new ProviderException("Failed to store self-signed certificate");
                }
                throw new ProviderException("Generated key pair algorithm does not match requested algorithm: " + result.getPrivate().getAlgorithm() + " vs " + this.mJcaKeyAlgorithm);
            } catch (CertificateEncodingException e) {
                throw new ProviderException("Failed to obtain encoded form of self-signed certificate", e);
            } catch (Exception e2) {
                throw new ProviderException("Failed to generate self-signed certificate", e2);
            } catch (UnrecoverableKeyException e3) {
                throw new ProviderException("Failed to load generated key pair from keystore", e3);
            } catch (Throwable th) {
                if (!false) {
                    Credentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias);
                }
            }
        } else {
            throw new IllegalStateException("Encryption at rest using secure lock screen credential requested for key pair, but the user has not yet entered the credential");
        }
    }

    static byte[] getRandomBytesToMixIntoKeystoreRng(SecureRandom rng, int sizeBytes) {
        if (sizeBytes <= 0) {
            return EmptyArray.BYTE;
        }
        if (rng == null) {
            rng = getRng();
        }
        byte[] result = new byte[sizeBytes];
        rng.nextBytes(result);
        return result;
    }

    private static SecureRandom getRng() {
        if (sRng == null) {
            sRng = new SecureRandom();
        }
        return sRng;
    }

    private void addAlgorithmSpecificParameters(KeymasterArguments keymasterArgs) {
        if (this.mRSAPublicExponent != null) {
            keymasterArgs.addUnsignedLong(KeymasterDefs.KM_TAG_RSA_PUBLIC_EXPONENT, this.mRSAPublicExponent);
        }
        if (this.isForSoter) {
            if (!keymasterArgs.getBoolean(KeymasterDefs.KM_TAG_SOTER_IS_FROM_SOTER)) {
                keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_IS_FROM_SOTER);
            }
            keymasterArgs.addUnsignedInt(KeymasterDefs.KM_TAG_SOTER_UID, (long) Process.myUid());
        }
        if (this.isAutoSignedWithAttkWhenGetPublicKey) {
            keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_IS_AUTO_SIGNED_WITH_ATTK_WHEN_GET_PUBLIC_KEY);
        }
        if (this.isAutoSignedWithCommonkWhenGetPublicKey) {
            keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_IS_AUTO_SIGNED_WITH_COMMON_KEY_WHEN_GET_PUBLIC_KEY);
            if (!SoterUtil.isNullOrNil(this.mAutoSignedKeyNameWhenGetPublicKey)) {
                keymasterArgs.addBytes(KeymasterDefs.KM_TAG_SOTER_AUTO_SIGNED_COMMON_KEY_WHEN_GET_PUBLIC_KEY, (Credentials.USER_PRIVATE_KEY + this.mAutoSignedKeyNameWhenGetPublicKey).getBytes());
            }
        }
        if (this.isAutoAddCounterWhenGetPublicKey) {
            keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_AUTO_ADD_COUNTER_WHEN_GET_PUBLIC_KEY);
        }
        if (this.isSecmsgFidCounterSignedWhenSign) {
            keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_IS_SECMSG_FID_COUNTER_SIGNED_WHEN_SIGN);
        }
        if (this.isNeedNextAttk) {
            keymasterArgs.addBoolean(KeymasterDefs.KM_TAG_SOTER_USE_NEXT_ATTK);
        }
    }

    private X509Certificate generateSelfSignedCertificate(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        Log.d("Soter", "generateSelfSignedCertificate");
        String signatureAlgorithm = getCertificateSignatureAlgorithm(this.mKeymasterAlgorithm, this.mKeySizeBits, this.mSpec);
        if (signatureAlgorithm == null) {
            Log.d("Soter", "generateSelfSignedCertificateWithFakeSignature1");
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
        try {
            Log.d("Soter", "generateSelfSignedCertificateWithValidSignature");
            return generateSelfSignedCertificateWithValidSignature(privateKey, publicKey, signatureAlgorithm);
        } catch (Exception e) {
            Log.d("Soter", "generateSelfSignedCertificateWithFakeSignature2");
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
    }

    private byte[] getRealKeyBlobByKeyName(String keyName) {
        Log.d("Soter", "start retrieve key blob by key name: " + keyName);
        return this.mKeyStore.get(Credentials.USER_PRIVATE_KEY + keyName);
    }

    private X509Certificate generateSelfSignedCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setPublicKey(publicKey);
        certGen.setSerialNumber(this.mSpec.getCertificateSerialNumber());
        certGen.setSubjectDN(this.mSpec.getCertificateSubject());
        certGen.setIssuerDN(this.mSpec.getCertificateSubject());
        certGen.setNotBefore(this.mSpec.getCertificateNotBefore());
        certGen.setNotAfter(this.mSpec.getCertificateNotAfter());
        certGen.setSignatureAlgorithm(signatureAlgorithm);
        return certGen.generate(privateKey);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0084 A:{SYNTHETIC, Splitter: B:23:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0089  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws Exception {
        AlgorithmIdentifier sigAlgId;
        byte[] signature;
        Throwable th;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        switch (this.mKeymasterAlgorithm) {
            case 1:
                sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
                signature = new byte[1];
                break;
            case 3:
                sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
                ASN1EncodableVector v = new ASN1EncodableVector();
                v.add(new DERInteger(0));
                v.add(new DERInteger(0));
                signature = new DERSequence().getEncoded();
                break;
            default:
                throw new ProviderException("Unsupported key algorithm: " + this.mKeymasterAlgorithm);
        }
        Throwable th2 = null;
        ASN1InputStream publicKeyInfoIn = null;
        try {
            ASN1InputStream publicKeyInfoIn2 = new ASN1InputStream(publicKey.getEncoded());
            try {
                tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn2.readObject()));
                if (publicKeyInfoIn2 != null) {
                    try {
                        publicKeyInfoIn2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                tbsGenerator.setSerialNumber(new ASN1Integer(this.mSpec.getCertificateSerialNumber()));
                X509Principal subject = new X509Principal(this.mSpec.getCertificateSubject().getEncoded());
                tbsGenerator.setSubject(subject);
                tbsGenerator.setIssuer(subject);
                tbsGenerator.setStartDate(new Time(this.mSpec.getCertificateNotBefore()));
                tbsGenerator.setEndDate(new Time(this.mSpec.getCertificateNotAfter()));
                tbsGenerator.setSignature(sigAlgId);
                TBSCertificate tbsCertificate = tbsGenerator.generateTBSCertificate();
                ASN1EncodableVector result = new ASN1EncodableVector();
                result.add(tbsCertificate);
                result.add(sigAlgId);
                result.add(new DERBitString(signature));
                return new X509CertificateObject(Certificate.getInstance(new DERSequence(result)));
            } catch (Throwable th4) {
                th = th4;
                publicKeyInfoIn = publicKeyInfoIn2;
                if (publicKeyInfoIn != null) {
                    try {
                        publicKeyInfoIn.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (publicKeyInfoIn != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        return 2048;
    }

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        if (keySize < 512 || keySize > 8192) {
            throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 8192");
        }
    }

    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, KeyGenParameterSpec spec) {
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified() || !ArrayUtils.contains(SignaturePadding.allToKeymaster(spec.getSignaturePaddings()), 5)) {
            return null;
        }
        int maxDigestOutputSizeBits = keySizeBits - 240;
        int bestKeymasterDigest = -1;
        int bestDigestOutputSizeBits = -1;
        for (Integer intValue : getAvailableKeymasterSignatureDigests(spec.getDigests(), getSupportedEcdsaSignatureDigests())) {
            int keymasterDigest = intValue.intValue();
            int outputSizeBits = getDigestOutputSizeBits(keymasterDigest);
            if (outputSizeBits <= maxDigestOutputSizeBits) {
                if (bestKeymasterDigest == -1) {
                    bestKeymasterDigest = keymasterDigest;
                    bestDigestOutputSizeBits = outputSizeBits;
                } else if (outputSizeBits > bestDigestOutputSizeBits) {
                    bestKeymasterDigest = keymasterDigest;
                    bestDigestOutputSizeBits = outputSizeBits;
                }
            }
        }
        if (bestKeymasterDigest == -1) {
            return null;
        }
        return Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithRSA";
    }

    private static String[] getSupportedEcdsaSignatureDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        int i = 0;
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet();
        for (int keymasterDigest : Digest.allToKeymaster(authorizedKeyDigests)) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet();
        int[] allToKeymaster = Digest.allToKeymaster(supportedSignatureDigests);
        int length = allToKeymaster.length;
        while (i < length) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(allToKeymaster[i]));
            i++;
        }
        Set<Integer> result = new HashSet(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }

    public static int getDigestOutputSizeBits(int keymasterDigest) {
        switch (keymasterDigest) {
            case 0:
                return -1;
            case 1:
                return 128;
            case 2:
                return 160;
            case 3:
                return 224;
            case 4:
                return 256;
            case 5:
                return MetricsEvent.ACTION_SHOW_SETTINGS_SUGGESTION;
            case 6:
                return 512;
            default:
                throw new IllegalArgumentException("Unknown digest: " + keymasterDigest);
        }
    }

    public static BigInteger toUint64(long value) {
        if (value >= 0) {
            return BigInteger.valueOf(value);
        }
        return BigInteger.valueOf(value).add(UINT64_RANGE);
    }

    public static boolean isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(int keymasterPadding) {
        switch (keymasterPadding) {
            case 1:
                return false;
            case 2:
            case 4:
                return true;
            default:
                throw new IllegalArgumentException("Unsupported asymmetric encryption padding scheme: " + keymasterPadding);
        }
    }

    public static Context getApplicationContext() {
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    public static byte[] intToByteArray(int value) {
        ByteBuffer converter = ByteBuffer.allocate(4);
        converter.order(ByteOrder.nativeOrder());
        converter.putInt(value);
        return converter.array();
    }
}
