package android.graphics;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.res.AssetManager;
import android.content.res.FontResourcesParser.FamilyResourceEntry;
import android.content.res.FontResourcesParser.FontFamilyFilesResourceEntry;
import android.content.res.FontResourcesParser.FontFileResourceEntry;
import android.content.res.FontResourcesParser.ProviderResourceEntry;
import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.FontRequest;
import android.provider.FontsContract;
import android.provider.FontsContract.FontInfo;
import android.system.ErrnoException;
import android.text.FontConfig;
import android.text.FontConfig.Alias;
import android.text.FontConfig.Family;
import android.text.FontConfig.Font;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.io.Libcore;
import org.xmlpull.v1.XmlPullParserException;

public class Typeface {
    public static final int BOLD = 1;
    public static final int BOLD_ITALIC = 3;
    public static final Typeface DEFAULT = create((String) null, 0);
    public static final Typeface DEFAULT_BOLD = create((String) null, 1);
    private static final int[] EMPTY_AXES = new int[0];
    static final String FONTS_CONFIG = "fonts.xml";
    public static final int ITALIC = 2;
    public static final Typeface MONOSPACE = create("monospace", 0);
    public static final int NORMAL = 0;
    public static final int RESOLVE_BY_FONT_TABLE = -1;
    public static final Typeface SANS_SERIF = create("sans-serif", 0);
    public static final Typeface SERIF = create("serif", 0);
    private static final int STYLE_ITALIC = 1;
    private static final int STYLE_NORMAL = 0;
    private static String TAG = "Typeface";
    private static final String VIVO_FONT_BLACK = "vivo-font-black";
    private static final String VIVO_FONT_LIGHT = "vivo-font-light";
    private static final String VIVO_FONT_MEDIUM = "vivo-font-medium";
    private static final String VIVO_FONT_THIN = "vivo-font-thin";
    private static final String defaultFont = "/system/fonts/DroidSansFallbackBBK.ttf";
    private static String defaultFontPath = (sOverseas ? "/system/fonts/Roboto-Regular.ttf" : "/system/fonts/DroidSansFallbackBBK.ttf");
    private static final String defaultOverseaFont = "/system/fonts/Roboto-Regular.ttf";
    private static final String sAndroidTTF = "/system/fonts/Roboto-Regular.ttf";
    static Typeface sDefaultTypeface = null;
    static Typeface[] sDefaults = new Typeface[]{DEFAULT, DEFAULT_BOLD, create((String) null, 2), create((String) null, 3)};
    @GuardedBy("sLock")
    private static final LruCache<String, Typeface> sDynamicTypefaceCache = new LruCache(16);
    static FontFamily[] sFallbackFonts = null;
    private static final Object sLock = new Object();
    private static final boolean sOverseas = "yes".equals(SystemProperties.get("ro.vivo.product.overseas", "no"));
    static Map<String, Typeface> sSystemFontMap = null;
    private static final LongSparseArray<SparseArray<Typeface>> sTypefaceCache = new LongSparseArray(3);
    private static final String[] sVivoFont = new String[]{"/system/fonts/DroidSansFallbackBBK.ttf", "/system/fonts/DroidSansMediumBBK.ttf", "/system/fonts/DroidSansBoldBBK.ttf"};
    private static final String sVivoFontLink = "/data/fonts/VivoFont.ttf";
    private static final String sVivoFontTTF = "/system/fonts/DroidSansFallbackBBK.ttf";
    private static FontFamily sVivoMyanmar = null;
    private static final String sVivoMyanmarTTF = "/system/fonts/Vivo-Myanmar.ttf";
    private static FontFamily sVivoNomral;
    private static final int[] sWeight = new int[]{400, RunningAppProcessInfo.IMPORTANCE_EMPTY, 700};
    private int mStyle;
    private int[] mSupportedAxes;
    private int mWeight;
    public long native_instance;

    public static final class Builder {
        public static final int BOLD_WEIGHT = 700;
        public static final int NORMAL_WEIGHT = 400;
        private static final Object sLock = new Object();
        @GuardedBy("sLock")
        private static final LongSparseArray<SparseArray<Typeface>> sTypefaceCache = new LongSparseArray(3);
        private AssetManager mAssetManager;
        private FontVariationAxis[] mAxes;
        private String mFallbackFamilyName;
        private FileDescriptor mFd;
        private Map<Uri, ByteBuffer> mFontBuffers;
        private FontInfo[] mFonts;
        private int mItalic = -1;
        private String mPath;
        private int mTtcIndex;
        private int mWeight = -1;

        public Builder(File path) {
            this.mPath = path.getAbsolutePath();
        }

        public Builder(FileDescriptor fd) {
            this.mFd = fd;
        }

        public Builder(String path) {
            this.mPath = path;
        }

        public Builder(AssetManager assetManager, String path) {
            this.mAssetManager = (AssetManager) Preconditions.checkNotNull(assetManager);
            this.mPath = (String) Preconditions.checkStringNotEmpty(path);
        }

        public Builder(FontInfo[] fonts, Map<Uri, ByteBuffer> buffers) {
            this.mFonts = fonts;
            this.mFontBuffers = buffers;
        }

        public Builder setWeight(int weight) {
            this.mWeight = weight;
            return this;
        }

        public Builder setItalic(boolean italic) {
            this.mItalic = italic ? 1 : 0;
            return this;
        }

        public Builder setTtcIndex(int ttcIndex) {
            if (this.mFonts != null) {
                throw new IllegalArgumentException("TTC index can not be specified for FontResult source.");
            }
            this.mTtcIndex = ttcIndex;
            return this;
        }

        public Builder setFontVariationSettings(String variationSettings) {
            if (this.mFonts != null) {
                throw new IllegalArgumentException("Font variation settings can not be specified for FontResult source.");
            } else if (this.mAxes != null) {
                throw new IllegalStateException("Font variation settings are already set.");
            } else {
                this.mAxes = FontVariationAxis.fromFontVariationSettings(variationSettings);
                return this;
            }
        }

        public Builder setFontVariationSettings(FontVariationAxis[] axes) {
            if (this.mFonts != null) {
                throw new IllegalArgumentException("Font variation settings can not be specified for FontResult source.");
            } else if (this.mAxes != null) {
                throw new IllegalStateException("Font variation settings are already set.");
            } else {
                this.mAxes = axes;
                return this;
            }
        }

        public Builder setFallback(String familyName) {
            this.mFallbackFamilyName = familyName;
            return this;
        }

        private static String createAssetUid(AssetManager mgr, String path, int ttcIndex, FontVariationAxis[] axes, int weight, int italic) {
            SparseArray<String> pkgs = mgr.getAssignedPackageIdentifiers();
            StringBuilder builder = new StringBuilder();
            int size = pkgs.size();
            for (int i = 0; i < size; i++) {
                builder.append((String) pkgs.valueAt(i));
                builder.append("-");
            }
            builder.append(path);
            builder.append("-");
            builder.append(Integer.toString(ttcIndex));
            builder.append("-");
            builder.append(Integer.toString(weight));
            builder.append("-");
            builder.append(Integer.toString(italic));
            builder.append("-");
            if (axes != null) {
                for (FontVariationAxis axis : axes) {
                    builder.append(axis.getTag());
                    builder.append("-");
                    builder.append(Float.toString(axis.getStyleValue()));
                }
            }
            return builder.toString();
        }

        private Typeface resolveFallbackTypeface() {
            int i = 1;
            if (this.mFallbackFamilyName == null) {
                return null;
            }
            Typeface base = (Typeface) Typeface.sSystemFontMap.get(this.mFallbackFamilyName);
            if (base == null) {
                base = Typeface.sDefaultTypeface;
            }
            if (this.mWeight == -1 && this.mItalic == -1) {
                return base;
            }
            int weight = this.mWeight == -1 ? base.mWeight : this.mWeight;
            boolean italic = this.mItalic != -1 ? this.mItalic != 1 : (base.mStyle & 2) == 0;
            int i2 = weight << 1;
            if (!italic) {
                i = 0;
            }
            int key = i2 | i;
            synchronized (sLock) {
                Typeface typeface;
                SparseArray<Typeface> innerCache = (SparseArray) sTypefaceCache.get(base.native_instance);
                if (innerCache != null) {
                    typeface = (Typeface) innerCache.get(key);
                    if (typeface != null) {
                        return typeface;
                    }
                }
                typeface = new Typeface(Typeface.nativeCreateFromTypefaceWithExactStyle(base.native_instance, weight, italic), null);
                if (innerCache == null) {
                    innerCache = new SparseArray(4);
                    sTypefaceCache.put(base.native_instance, innerCache);
                }
                innerCache.put(key, typeface);
                return typeface;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:50:0x0099 A:{SYNTHETIC, Splitter: B:50:0x0099} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x00ac A:{Catch:{ IOException -> 0x009f }} */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x009e A:{SYNTHETIC, Splitter: B:53:0x009e} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Typeface build() {
            Throwable th;
            Throwable th2;
            FontFamily fontFamily;
            if (this.mFd != null) {
                Throwable th3 = null;
                FileInputStream fis = null;
                try {
                    FileInputStream fileInputStream = new FileInputStream(this.mFd);
                    try {
                        FileChannel channel = fileInputStream.getChannel();
                        ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
                        fontFamily = new FontFamily();
                        Typeface resolveFallbackTypeface;
                        if (!fontFamily.addFontFromBuffer(buffer, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic)) {
                            fontFamily.abortCreation();
                            resolveFallbackTypeface = resolveFallbackTypeface();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable th4) {
                                    th3 = th4;
                                }
                            }
                            if (th3 == null) {
                                return resolveFallbackTypeface;
                            }
                            try {
                                throw th3;
                            } catch (IOException e) {
                                fis = fileInputStream;
                            }
                        } else if (fontFamily.freeze()) {
                            resolveFallbackTypeface = Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, this.mWeight, this.mItalic);
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable th5) {
                                    th3 = th5;
                                }
                            }
                            if (th3 == null) {
                                return resolveFallbackTypeface;
                            }
                            throw th3;
                        } else {
                            resolveFallbackTypeface = resolveFallbackTypeface();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable th6) {
                                    th3 = th6;
                                }
                            }
                            if (th3 == null) {
                                return resolveFallbackTypeface;
                            }
                            throw th3;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        fis = fileInputStream;
                        th2 = null;
                        if (fis != null) {
                        }
                        if (th2 == null) {
                        }
                    }
                } catch (Throwable th8) {
                    th = th8;
                    th2 = null;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Throwable th9) {
                            if (th2 == null) {
                                th2 = th9;
                            } else if (th2 != th9) {
                                th2.addSuppressed(th9);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            return resolveFallbackTypeface();
                        }
                    }
                    throw th;
                }
            } else if (this.mAssetManager != null) {
                String key = createAssetUid(this.mAssetManager, this.mPath, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic);
                synchronized (sLock) {
                    Typeface typeface = (Typeface) Typeface.sDynamicTypefaceCache.get(key);
                    if (typeface != null) {
                        return typeface;
                    }
                    fontFamily = new FontFamily();
                    if (!fontFamily.addFontFromAssetManager(this.mAssetManager, this.mPath, this.mTtcIndex, true, this.mTtcIndex, this.mWeight, this.mItalic, this.mAxes)) {
                        fontFamily.abortCreation();
                        return resolveFallbackTypeface();
                    } else if (fontFamily.freeze()) {
                        typeface = Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, this.mWeight, this.mItalic);
                        Typeface.sDynamicTypefaceCache.put(key, typeface);
                        return typeface;
                    } else {
                        return resolveFallbackTypeface();
                    }
                }
            } else if (this.mPath != null) {
                fontFamily = new FontFamily();
                if (!fontFamily.addFont(this.mPath, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic)) {
                    fontFamily.abortCreation();
                    return resolveFallbackTypeface();
                } else if (!fontFamily.freeze()) {
                    return resolveFallbackTypeface();
                } else {
                    return Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, this.mWeight, this.mItalic);
                }
            } else if (this.mFonts != null) {
                fontFamily = new FontFamily();
                boolean atLeastOneFont = false;
                for (FontInfo font : this.mFonts) {
                    ByteBuffer fontBuffer = (ByteBuffer) this.mFontBuffers.get(font.getUri());
                    if (fontBuffer != null) {
                        if (fontFamily.addFontFromBuffer(fontBuffer, font.getTtcIndex(), font.getAxes(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                            atLeastOneFont = true;
                        } else {
                            fontFamily.abortCreation();
                            return null;
                        }
                    }
                }
                if (atLeastOneFont) {
                    fontFamily.freeze();
                    return Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, this.mWeight, this.mItalic);
                }
                fontFamily.abortCreation();
                return null;
            } else {
                throw new IllegalArgumentException("No source was set.");
            }
        }
    }

    /* synthetic */ Typeface(long ni, Typeface -this1) {
        this(ni);
    }

    private static native long nativeCreateFromArray(long[] jArr, int i, int i2);

    private static native long nativeCreateFromTypeface(long j, int i);

    private static native long nativeCreateFromTypefaceWithExactStyle(long j, int i, boolean z);

    private static native long nativeCreateFromTypefaceWithVariation(long j, List<FontVariationAxis> list);

    private static native long nativeCreateWeightAlias(long j, int i);

    private static native int nativeGetStyle(long j);

    private static native int[] nativeGetSupportedAxes(long j);

    private static native int nativeGetWeight(long j);

    private static native void nativeSetDefault(long j);

    private static native void nativeUnref(long j);

    private static native void nativeUpdateTypeface(long j, long j2, boolean z);

    static {
        init();
    }

    private static String getDefaltPath(String path) {
        try {
            if (new File(path).exists()) {
                String ret = Libcore.os.realpath(path);
                Log.v(TAG, "getDefaltPath: " + ret);
                return ret;
            }
            Log.d(TAG, "link file not exit,return default font!");
            return defaultFontPath;
        } catch (ErrnoException ex) {
            ex.printStackTrace();
            return defaultFontPath;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultFontPath;
        }
    }

    private static boolean isDefaultFont() {
        boolean isDefaultFont = true;
        try {
            if (new File(sVivoFontLink).exists()) {
                String str = Libcore.os.realpath(sVivoFontLink);
                Log.v(TAG, "isDefaultFont path: " + str);
                if (!"/system/fonts/DroidSansFallbackBBK.ttf".equals(str)) {
                    isDefaultFont = false;
                    Log.d(TAG, "switch to online font!");
                }
                return isDefaultFont;
            }
            Log.d(TAG, "link file not exit,return default font!");
            return true;
        } catch (ErrnoException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAndroidFont() {
        boolean isAndroidFont = false;
        try {
            if (new File(sVivoFontLink).exists()) {
                String str = Libcore.os.realpath(sVivoFontLink);
                Log.v(TAG, "CurFontPath: " + str);
                if ("/system/fonts/Roboto-Regular.ttf".equals(str)) {
                    isAndroidFont = true;
                    Log.d(TAG, "switch to android orgin ttf");
                }
                return isAndroidFont;
            }
            Log.d(TAG, "link file not exit,return fault font!");
            return false;
        } catch (ErrnoException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isVivoFamily(String name) {
        return name != null ? "sans-serif".equals(name) : false;
    }

    private static String adjustFamilyNameForVivo(String name) {
        if (name == null) {
            return name;
        }
        if (VIVO_FONT_THIN.equals(name)) {
            return "sans-serif-thin";
        }
        if (VIVO_FONT_LIGHT.equals(name)) {
            return "sans-serif-light";
        }
        if (VIVO_FONT_MEDIUM.equals(name)) {
            return "sans-serif-medium";
        }
        if (VIVO_FONT_BLACK.equals(name)) {
            return "sans-serif-black";
        }
        return name;
    }

    private static boolean replaceFont() {
        String fontPath = defaultFontPath;
        try {
            if (new File(sVivoFontLink).exists()) {
                fontPath = Libcore.os.realpath(sVivoFontLink);
            } else {
                Log.d(TAG, "link file not exit,switch to default font!");
                fontPath = defaultFontPath;
            }
        } catch (ErrnoException ex) {
            ex.printStackTrace();
            fontPath = defaultFontPath;
        } catch (Exception e) {
            e.printStackTrace();
            fontPath = defaultFontPath;
        }
        boolean isAndroid = "/system/fonts/Roboto-Regular.ttf".equals(fontPath);
        boolean isDefault = "/system/fonts/DroidSansFallbackBBK.ttf".equals(fontPath);
        Log.d(TAG, "replaceFont fontPath : " + fontPath + " ,isDefault : " + isDefault + " ,isAndroid : " + isAndroid);
        ByteBuffer fontBuffer;
        if (sOverseas) {
            fontBuffer = getBufferForVivoFont(fontPath);
            if (fontBuffer == null) {
                Log.e(TAG, "Error creating font " + defaultFontPath);
                return false;
            }
            sVivoNomral.replaceFont(fontBuffer, 0);
        } else {
            int index = 0;
            while (index < sVivoFont.length && index < sWeight.length) {
                String str;
                if (isDefault) {
                    str = sVivoFont[index];
                } else {
                    str = fontPath;
                }
                fontBuffer = getBufferForVivoFont(str);
                if (fontBuffer == null) {
                    fontBuffer = getBufferForVivoFont(defaultFontPath);
                    if (fontBuffer == null) {
                        Log.e(TAG, "Error creating font " + defaultFontPath);
                        return false;
                    }
                }
                if (sVivoNomral.replaceFont(fontBuffer, index)) {
                    index++;
                } else {
                    Log.e(TAG, "Error replace font : " + fontPath);
                    return false;
                }
            }
        }
        Object[] list = sSystemFontMap.values().toArray();
        for (int i = 0; i < list.length; i++) {
            if (sOverseas && sVivoMyanmar != null) {
                updateTypeface((Typeface) list[i], sVivoMyanmar, false);
            }
            updateTypeface((Typeface) list[i], sVivoNomral, isAndroid);
        }
        return true;
    }

    public static void reloadVivoFont(boolean zygote) {
        if (sVivoMyanmar != null && sOverseas) {
            ByteBuffer fontBufferMyanmar = getBufferForVivoFont(sVivoMyanmarTTF);
            if (fontBufferMyanmar == null) {
                Log.e(TAG, "Error creating font buffer /system/fonts/Vivo-Myanmar.ttf");
            } else if (!sVivoMyanmar.replaceFont(fontBufferMyanmar, 0)) {
                Log.e(TAG, "Error creating font /system/fonts/Vivo-Myanmar.ttf");
            }
        }
        if (sVivoNomral != null) {
            replaceFont();
        } else {
            Log.d(TAG, "reloadVivoFont sVivoNomral is null!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0063 A:{SYNTHETIC, Splitter: B:23:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0076 A:{Catch:{ IOException -> 0x0069 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0068 A:{SYNTHETIC, Splitter: B:26:0x0068} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ByteBuffer getBufferForVivoFont(String path) {
        IOException e;
        Throwable th;
        FileInputStream file = null;
        Throwable th2;
        try {
            FileInputStream file2 = new FileInputStream(path);
            try {
                FileChannel fileChannel = file2.getChannel();
                long fontSize = fileChannel.size();
                ByteBuffer fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fontSize);
                Log.d(TAG, "getBufferForVivoFont fontSize = " + fontSize);
                if (file2 != null) {
                    try {
                        file2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return fontBuffer;
                }
                try {
                    throw th2;
                } catch (IOException e2) {
                    e = e2;
                    file = file2;
                }
            } catch (Throwable th4) {
                th2 = th4;
                file = file2;
                th = null;
                if (file != null) {
                }
                if (th == null) {
                }
            }
        } catch (Throwable th5) {
            th2 = th5;
            th = null;
            if (file != null) {
                try {
                    file.close();
                } catch (Throwable th6) {
                    if (th == null) {
                        th = th6;
                    } else if (th != th6) {
                        th.addSuppressed(th6);
                    }
                }
            }
            if (th == null) {
                try {
                    throw th;
                } catch (IOException e3) {
                    e = e3;
                    Log.e(TAG, "getBufferForVivoFont error mapping font file " + path);
                    e.printStackTrace();
                    return null;
                }
            }
            throw th2;
        }
    }

    private static void updateTypeface(Typeface typeface, FontFamily family, boolean androidFont) {
        nativeUpdateTypeface(typeface.native_instance, family.mNativePtr, androidFont);
    }

    private static void setDefault(Typeface t) {
        sDefaultTypeface = t;
        nativeSetDefault(t.native_instance);
    }

    public int getStyle() {
        return this.mStyle;
    }

    public final boolean isBold() {
        return (this.mStyle & 1) != 0;
    }

    public final boolean isItalic() {
        return (this.mStyle & 2) != 0;
    }

    public static Typeface createFromResources(AssetManager mgr, String path, int cookie) {
        if (sFallbackFonts != null) {
            synchronized (sDynamicTypefaceCache) {
                String key = Builder.createAssetUid(mgr, path, 0, null, -1, -1);
                Typeface typeface = (Typeface) sDynamicTypefaceCache.get(key);
                if (typeface != null) {
                    return typeface;
                }
                FontFamily fontFamily = new FontFamily();
                if (fontFamily.addFontFromAssetManager(mgr, path, cookie, false, 0, -1, -1, null)) {
                    if (fontFamily.freeze()) {
                        typeface = createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, -1, -1);
                        sDynamicTypefaceCache.put(key, typeface);
                        return typeface;
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public static Typeface createFromResources(FamilyResourceEntry entry, AssetManager mgr, String path) {
        if (sFallbackFonts == null) {
            return null;
        }
        Typeface typeface;
        if (entry instanceof ProviderResourceEntry) {
            ProviderResourceEntry providerEntry = (ProviderResourceEntry) entry;
            List<List<String>> givenCerts = providerEntry.getCerts();
            List<List<byte[]>> certs = new ArrayList();
            if (givenCerts != null) {
                for (int i = 0; i < givenCerts.size(); i++) {
                    List<String> certSet = (List) givenCerts.get(i);
                    List<byte[]> byteArraySet = new ArrayList();
                    for (int j = 0; j < certSet.size(); j++) {
                        byteArraySet.add(Base64.decode((String) certSet.get(j), 0));
                    }
                    certs.add(byteArraySet);
                }
            }
            typeface = FontsContract.getFontSync(new FontRequest(providerEntry.getAuthority(), providerEntry.getPackage(), providerEntry.getQuery(), certs));
            if (typeface == null) {
                typeface = DEFAULT;
            }
            return typeface;
        }
        typeface = findFromCache(mgr, path);
        if (typeface != null) {
            return typeface;
        }
        FontFamilyFilesResourceEntry filesEntry = (FontFamilyFilesResourceEntry) entry;
        FontFamily fontFamily = new FontFamily();
        FontFileResourceEntry[] entries = filesEntry.getEntries();
        int i2 = 0;
        int length = entries.length;
        while (true) {
            int i3 = i2;
            if (i3 < length) {
                FontFileResourceEntry fontFile = entries[i3];
                if (!fontFamily.addFontFromAssetManager(mgr, fontFile.getFileName(), 0, false, 0, fontFile.getWeight(), fontFile.getItalic(), null)) {
                    return null;
                }
                i2 = i3 + 1;
            } else if (!fontFamily.freeze()) {
                return null;
            } else {
                typeface = createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, -1, -1);
                synchronized (sDynamicTypefaceCache) {
                    sDynamicTypefaceCache.put(Builder.createAssetUid(mgr, path, 0, null, -1, -1), typeface);
                }
                return typeface;
            }
        }
    }

    public static Typeface findFromCache(AssetManager mgr, String path) {
        synchronized (sDynamicTypefaceCache) {
            Typeface typeface = (Typeface) sDynamicTypefaceCache.get(Builder.createAssetUid(mgr, path, 0, null, -1, -1));
            if (typeface != null) {
                return typeface;
            }
            return null;
        }
    }

    public static Typeface create(String familyName, int style) {
        String fName = adjustFamilyNameForVivo(familyName);
        if (sSystemFontMap != null) {
            return create((Typeface) sSystemFontMap.get(fName), style);
        }
        return null;
    }

    public static Typeface create(Typeface family, int style) {
        Typeface typeface;
        if (style < 0 || style > 3) {
            style = 0;
        }
        long ni = 0;
        if (family != null) {
            if (family.mStyle == style) {
                return family;
            }
            ni = family.native_instance;
        }
        SparseArray<Typeface> styles = (SparseArray) sTypefaceCache.get(ni);
        if (styles != null) {
            typeface = (Typeface) styles.get(style);
            if (typeface != null) {
                return typeface;
            }
        }
        typeface = new Typeface(nativeCreateFromTypeface(ni, style));
        if (styles == null) {
            styles = new SparseArray(4);
            sTypefaceCache.put(ni, styles);
        }
        styles.put(style, typeface);
        return typeface;
    }

    public static Typeface createFromTypefaceWithVariation(Typeface family, List<FontVariationAxis> axes) {
        return new Typeface(nativeCreateFromTypefaceWithVariation(family == null ? 0 : family.native_instance, axes));
    }

    public static Typeface defaultFromStyle(int style) {
        return sDefaults[style];
    }

    public static Typeface createFromAsset(AssetManager mgr, String path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (sFallbackFonts != null) {
            synchronized (sLock) {
                Typeface typeface = new Builder(mgr, path).build();
                if (typeface != null) {
                    return typeface;
                }
                String key = Builder.createAssetUid(mgr, path, 0, null, -1, -1);
                typeface = (Typeface) sDynamicTypefaceCache.get(key);
                if (typeface != null) {
                    return typeface;
                }
                FontFamily fontFamily = new FontFamily();
                if (fontFamily.addFontFromAssetManager(mgr, path, 0, true, 0, -1, -1, null)) {
                    fontFamily.allowUnsupportedFont();
                    fontFamily.freeze();
                    typeface = createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, -1, -1);
                    sDynamicTypefaceCache.put(key, typeface);
                    return typeface;
                }
                fontFamily.abortCreation();
            }
        }
        throw new RuntimeException("Font asset not found " + path);
    }

    private static String createProviderUid(String authority, String query) {
        StringBuilder builder = new StringBuilder();
        builder.append("provider:");
        builder.append(authority);
        builder.append("-");
        builder.append(query);
        return builder.toString();
    }

    public static Typeface createFromFile(File path) {
        return createFromFile(path.getAbsolutePath());
    }

    public static Typeface createFromFile(String path) {
        if (sFallbackFonts != null) {
            FontFamily fontFamily = new FontFamily();
            if (fontFamily.addFont(path, 0, null, -1, -1)) {
                fontFamily.allowUnsupportedFont();
                fontFamily.freeze();
                return createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, -1, -1);
            }
            fontFamily.abortCreation();
        }
        throw new RuntimeException("Font not found " + path);
    }

    private static Typeface createFromFamilies(FontFamily[] families) {
        long[] ptrArray = new long[families.length];
        for (int i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray, -1, -1));
    }

    private static Typeface createFromFamiliesWithDefault(FontFamily[] families, int weight, int italic) {
        int i;
        long[] ptrArray = new long[(families.length + sFallbackFonts.length)];
        for (i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        for (i = 0; i < sFallbackFonts.length; i++) {
            ptrArray[families.length + i] = sFallbackFonts[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray, weight, italic));
    }

    private Typeface(long ni) {
        this.mStyle = 0;
        this.mWeight = 0;
        if (ni == 0) {
            throw new RuntimeException("native typeface cannot be made");
        }
        this.native_instance = ni;
        this.mStyle = nativeGetStyle(ni);
        this.mWeight = nativeGetWeight(ni);
    }

    /* JADX WARNING: Removed duplicated region for block: B:116:0x02f0 A:{SYNTHETIC, Splitter: B:116:0x02f0} */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0303 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x02f5 A:{SYNTHETIC, Splitter: B:119:0x02f5} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b3 A:{SYNTHETIC, Splitter: B:33:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x00c6 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b8 A:{SYNTHETIC, Splitter: B:36:0x00b8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static FontFamily makeFamilyFromParsed(Family family, Map<String, ByteBuffer> bufferForPath) {
        FileInputStream file;
        Throwable th;
        Throwable th2;
        Throwable th3;
        FontFamily fontFamily = new FontFamily(family.getLanguage(), family.getVariant());
        int i;
        Font font;
        String fullPathName;
        ByteBuffer fontBuffer;
        Throwable th4;
        FileInputStream fileInputStream;
        FileChannel fileChannel;
        if (!isVivoFamily(family.getName()) || (SystemProperties.getBoolean("debug.def.typeface", false) ^ 1) == 0) {
            Font[] fonts = family.getFonts();
            i = 0;
            int length = fonts.length;
            while (true) {
                int i2 = i;
                if (i2 < length) {
                    font = fonts[i2];
                    fullPathName = "/system/fonts/" + font.getFontName();
                    fontBuffer = (ByteBuffer) bufferForPath.get(fullPathName);
                    if (fontBuffer == null) {
                        th4 = null;
                        file = null;
                        try {
                            fileInputStream = new FileInputStream(fullPathName);
                            try {
                                fileChannel = fileInputStream.getChannel();
                                fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                                bufferForPath.put(fullPathName, fontBuffer);
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable th5) {
                                        th4 = th5;
                                    }
                                }
                                if (th4 != null) {
                                    try {
                                        throw th4;
                                    } catch (IOException e) {
                                        file = fileInputStream;
                                    }
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                file = fileInputStream;
                                th3 = null;
                                if (file != null) {
                                }
                                if (th3 == null) {
                                }
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            th3 = null;
                            if (file != null) {
                                try {
                                    file.close();
                                } catch (Throwable th8) {
                                    if (th3 == null) {
                                        th3 = th8;
                                    } else if (th3 != th8) {
                                        th3.addSuppressed(th8);
                                    }
                                }
                            }
                            if (th3 == null) {
                                try {
                                    throw th3;
                                } catch (IOException e2) {
                                    Log.e(TAG, "Error mapping font file " + fullPathName);
                                    i = i2 + 1;
                                }
                            } else {
                                throw th;
                            }
                        }
                    }
                    if (!fontFamily.addFontFromBuffer(fontBuffer, font.getTtcIndex(), font.getAxes(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                        Log.e(TAG, "Error creating font " + fullPathName + "#" + font.getTtcIndex());
                    }
                    if (sVivoMyanmar == null && sVivoMyanmarTTF.equals(fullPathName) && sOverseas) {
                        sVivoMyanmar = fontFamily;
                        Log.e(TAG, "makeFamilyFromParsed init sVivoMyanmar");
                    }
                    Log.e(TAG, "makeFamilyFromParsed font.fontName:" + fullPathName);
                    i = i2 + 1;
                } else if (fontFamily.freeze()) {
                    return fontFamily;
                } else {
                    Log.e(TAG, "Unable to load Family: " + family.getName() + ":" + family.getLanguage());
                    return null;
                }
            }
        }
        if (sOverseas) {
            Font[] fonts2 = family.getFonts();
            i = 0;
            int length2 = fonts2.length;
            while (true) {
                int i3 = i;
                if (i3 < length2) {
                    font = fonts2[i3];
                    fullPathName = "/system/fonts/" + font.getFontName();
                    fontBuffer = (ByteBuffer) bufferForPath.get(fullPathName);
                    if (fontBuffer == null) {
                        th4 = null;
                        file = null;
                        try {
                            fileInputStream = new FileInputStream(fullPathName);
                            try {
                                fileChannel = fileInputStream.getChannel();
                                fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                                bufferForPath.put(fullPathName, fontBuffer);
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable th9) {
                                        th4 = th9;
                                    }
                                }
                                if (th4 != null) {
                                    try {
                                        throw th4;
                                    } catch (IOException e3) {
                                        file = fileInputStream;
                                    }
                                }
                            } catch (Throwable th10) {
                                th = th10;
                                file = fileInputStream;
                                th3 = null;
                                if (file != null) {
                                }
                                if (th3 == null) {
                                }
                            }
                        } catch (Throwable th11) {
                            th = th11;
                            th3 = null;
                            if (file != null) {
                                try {
                                    file.close();
                                } catch (Throwable th82) {
                                    if (th3 == null) {
                                        th3 = th82;
                                    } else if (th3 != th82) {
                                        th3.addSuppressed(th82);
                                    }
                                }
                            }
                            if (th3 == null) {
                                try {
                                    throw th3;
                                } catch (IOException e4) {
                                    Log.e(TAG, "Error mapping font file " + fullPathName);
                                    i = i3 + 1;
                                }
                            } else {
                                throw th;
                            }
                        }
                    }
                    if (!fontFamily.addFontFromBuffer(fontBuffer, font.getTtcIndex(), font.getAxes(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                        Log.e(TAG, "Error creating font " + fullPathName + "#" + font.getTtcIndex());
                    }
                    i = i3 + 1;
                } else if (!fontFamily.freeze()) {
                    Log.e(TAG, "Unable to load Family: " + family.getName() + ":" + family.getLanguage());
                    return null;
                }
            }
        }
        boolean isDefault = isDefaultFont();
        Log.d(TAG, "makeFamilyFromParsed isDefault : " + isDefault);
        Font[] list = family.getFonts();
        font = list.length > 0 ? list[0] : null;
        if (font == null) {
            Log.w(TAG, "makeFamilyFromParsed default font is null.");
            return null;
        }
        FontVariationAxis[] axis = font.getAxes();
        int index = 0;
        while (index < sVivoFont.length && index < sWeight.length) {
            String path = getDefaltPath(isDefault ? sVivoFont[index] : sVivoFontLink);
            fontBuffer = getBufferForVivoFont(path);
            if (fontBuffer == null || (fontFamily.addFontFromBuffer(fontBuffer, 0, axis, sWeight[index], 0) ^ 1) != 0) {
                Log.e(TAG, "Error creating font " + path + "#" + index);
                ByteBuffer defBuffer = getBufferForVivoFont(defaultFontPath);
                if (defBuffer != null) {
                    if ((fontFamily.addFontFromBuffer(defBuffer, 0, axis, sWeight[index], 0) ^ 1) == 0) {
                    }
                }
                Log.e(TAG, "Error creating with deffont " + defaultFontPath + "#" + index);
            }
            index++;
        }
        if (!fontFamily.freeze()) {
            Log.e(TAG, "Unable to load Family: " + family.getName() + ":" + family.getLanguage());
            return null;
        }
        if (sVivoNomral == null) {
            sVivoNomral = fontFamily;
            Log.d(TAG, "makeFamilyFromParsed init sVivoNomral");
        }
        return fontFamily;
    }

    private static void init() {
        File configFilename = new File(getSystemFontConfigLocation(), FONTS_CONFIG);
        try {
            int i;
            Family f;
            FontConfig fontConfig = FontListParser.parse(new FileInputStream(configFilename));
            Map<String, ByteBuffer> bufferForPath = new HashMap();
            List<FontFamily> familyList = new ArrayList();
            for (i = 0; i < fontConfig.getFamilies().length; i++) {
                f = fontConfig.getFamilies()[i];
                if (i == 0 || f.getName() == null) {
                    FontFamily family = makeFamilyFromParsed(f, bufferForPath);
                    if (family != null) {
                        familyList.add(family);
                    }
                }
            }
            sFallbackFonts = (FontFamily[]) familyList.toArray(new FontFamily[familyList.size()]);
            setDefault(createFromFamilies(sFallbackFonts));
            Map<String, Typeface> systemFonts = new HashMap();
            for (i = 0; i < fontConfig.getFamilies().length; i++) {
                f = fontConfig.getFamilies()[i];
                if (f.getName() != null) {
                    Typeface typeface;
                    if (i == 0) {
                        typeface = sDefaultTypeface;
                    } else {
                        if (makeFamilyFromParsed(f, bufferForPath) != null) {
                            typeface = createFromFamiliesWithDefault(new FontFamily[]{makeFamilyFromParsed(f, bufferForPath)}, -1, -1);
                        }
                    }
                    systemFonts.put(f.getName(), typeface);
                }
            }
            for (Alias alias : fontConfig.getAliases()) {
                Typeface base = (Typeface) systemFonts.get(alias.getToName());
                Typeface newFace = base;
                int weight = alias.getWeight();
                if (weight != 400) {
                    Typeface typeface2 = new Typeface(nativeCreateWeightAlias(base.native_instance, weight));
                }
                systemFonts.put(alias.getName(), newFace);
            }
            sSystemFontMap = systemFonts;
            if (isAndroidFont() && sVivoNomral != null) {
                updateTypeface(sDefaultTypeface, sVivoNomral, true);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Didn't create default family (most likely, non-Minikin build)", e);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "Error opening " + configFilename, e2);
        } catch (IOException e3) {
            Log.e(TAG, "Error reading " + configFilename, e3);
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "XML parse exception for " + configFilename, e4);
        }
    }

    private static File getSystemFontConfigLocation() {
        return new File("/system/etc/");
    }

    protected void finalize() throws Throwable {
        try {
            nativeUnref(this.native_instance);
            this.native_instance = 0;
        } finally {
            super.finalize();
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Typeface typeface = (Typeface) o;
        if (!(this.mStyle == typeface.mStyle && this.native_instance == typeface.native_instance)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((int) (this.native_instance ^ (this.native_instance >>> 32))) + 527) * 31) + this.mStyle;
    }

    public boolean isSupportedAxes(int axis) {
        if (this.mSupportedAxes == null) {
            synchronized (this) {
                if (this.mSupportedAxes == null) {
                    this.mSupportedAxes = nativeGetSupportedAxes(this.native_instance);
                    if (this.mSupportedAxes == null) {
                        this.mSupportedAxes = EMPTY_AXES;
                    }
                }
            }
        }
        if (Arrays.binarySearch(this.mSupportedAxes, axis) >= 0) {
            return true;
        }
        return false;
    }
}
