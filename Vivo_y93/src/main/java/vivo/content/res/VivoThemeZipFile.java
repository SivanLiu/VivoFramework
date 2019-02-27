package vivo.content.res;

import android.content.res.Resources;
import android.content.res.ResourcesImpl.VivoThemeFileInfo;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.content.VivoTheme;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class VivoThemeZipFile {
    public static final String PKG_ANDROID = "android";
    public static final String PKG_FRAMEWORK = "framework-res";
    public static final String PKG_VIVO = "vivo";
    private static final String TAG = "VivoThemeZipFile";
    public static final String THEME_FILTER_FILE = "filters.xml";
    public static final String THEME_VALUES_FILE = "theme_values.xml";
    private static final HashMap<String, WeakReference<VivoThemeZipFile>> sThemeZipFiles = new HashMap();
    private long mLastModifyTime = -1;
    private String mPath;
    private VivoZipFile mZipFile;

    private class VivoZipFile extends ZipFile {
        HashMap<String, ZipEntry> mEntryCache = new HashMap(size());

        public VivoZipFile(File file) throws ZipException, IOException {
            super(file);
            Enumeration<? extends ZipEntry> entries = entries();
            while (entries.hasMoreElements()) {
                try {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (!entry.isDirectory()) {
                        this.mEntryCache.put(entry.getName(), entry);
                    }
                } catch (Exception e) {
                }
            }
        }

        public ZipEntry getEntry(String entryName) {
            return (ZipEntry) this.mEntryCache.get(entryName);
        }
    }

    public void clearCache() {
        closeZipFile();
        sThemeZipFiles.clear();
        this.mLastModifyTime = -1;
    }

    private VivoZipFile getThemeZipFile() {
        if (this.mZipFile == null) {
            checkUpdate();
        }
        return this.mZipFile;
    }

    public static VivoThemeZipFile getThemeZipFile(String pkg) {
        String zipPath = VivoTheme.getThemePath() + getPathName(pkg);
        WeakReference<VivoThemeZipFile> ref = (WeakReference) sThemeZipFiles.get(zipPath);
        VivoThemeZipFile zipFile = ref == null ? null : (VivoThemeZipFile) ref.get();
        if (zipFile == null) {
            zipFile = new VivoThemeZipFile(zipPath);
            if (zipFile.isFileExist()) {
                sThemeZipFiles.put(zipPath, new WeakReference(zipFile));
            }
        }
        return zipFile;
    }

    private VivoThemeZipFile(String path) {
        this.mPath = path;
    }

    public InputStream getZipInputStream(String file) {
        if (!isValid() || file == null) {
            return null;
        }
        ZipEntry entry = getThemeZipFile().getEntry(file);
        if (entry != null) {
            try {
                return getThemeZipFile().getInputStream(entry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean getThemeFile(Resources res, VivoThemeFileInfo fileInfo) {
        return getThemeFile(res, fileInfo, null);
    }

    /* JADX WARNING: Missing block: B:24:0x011b, code:
            if (getZipInputStream(r17, r9.replace(".png", ".9.png"), r18, false) != false) goto L_0x011d;
     */
    /* JADX WARNING: Missing block: B:30:0x016b, code:
            if (getZipInputStream(r17, r11.replace(".png", ".9.png"), r18, false) != false) goto L_0x016d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getThemeFile(Resources res, VivoThemeFileInfo fileInfo, String filterPath) {
        if (!isFileExist() || (isValid() ^ 1) != 0) {
            return false;
        }
        boolean exist;
        String str = getNameFromPath(fileInfo.resourcePath);
        StringBuilder sbu = new StringBuilder();
        StringBuilder ldRtlSbu = new StringBuilder();
        boolean isDpiRes = false;
        String dir = getDensityResPath(res);
        BidiFormatter bidi = BidiFormatter.getInstance(Locale.getDefault());
        String ldRtlStr = "";
        if (bidi.isRtlContext()) {
            ldRtlSbu.append("res-ldrtl/").append(dir);
            ldRtlStr = ldRtlSbu.append("/").append(str).toString();
            isDpiRes = getZipInputStream(fileInfo, ldRtlStr, filterPath, false);
            if (!isDpiRes) {
                isDpiRes = getZipInputStream(fileInfo, ldRtlStr.replace(".png", ".9.png"), filterPath, false);
            }
        }
        if (!isDpiRes) {
            sbu.append("res/").append(dir);
            isDpiRes = getZipInputStream(fileInfo, sbu.append("/").append(str).toString(), filterPath, false);
            if (!isDpiRes) {
                isDpiRes = getZipInputStream(fileInfo, ldRtlStr.replace(".png", ".9.png"), filterPath, false);
            }
            if (isDpiRes) {
                return true;
            }
        }
        if (!isDpiRes) {
            sbu = new StringBuilder();
            ldRtlSbu = new StringBuilder();
            dir = "drawable-sw360dp-xxhdpi";
            str = getNameFromPath(fileInfo.resourcePath);
            ldRtlStr = ldRtlSbu.append("res-ldrtl/").append(dir).append("/").append(str).toString();
            str = sbu.append("res/").append(dir).append("/").append(str).toString();
            if (bidi.isRtlContext()) {
                if (!getZipInputStream(fileInfo, ldRtlStr, filterPath, false)) {
                }
                fileInfo.resCompiled = true;
                Log.d(TAG, "get density resources. path:" + str + "  res compile?:" + fileInfo.resCompiled);
                return true;
            }
            if (!getZipInputStream(fileInfo, str, filterPath, false)) {
            }
            fileInfo.resCompiled = true;
            Log.d(TAG, "get density resources. path:" + str + "  res compile?:" + fileInfo.resCompiled);
            return true;
        }
        sbu = new StringBuilder();
        str = getNameFromPath(fileInfo.resourcePath);
        String language = res.getConfiguration().locale.getLanguage();
        String country = res.getConfiguration().locale.getCountry();
        if (!TextUtils.isEmpty(filterPath)) {
            sbu.append(filterPath).append("/");
        }
        sbu.append(language).append("-").append(country).append("/").append(str);
        str = sbu.toString();
        if (getZipInputStream(fileInfo, str, filterPath)) {
            exist = true;
        } else {
            exist = getZipInputStream(fileInfo, str.replace(".png", ".9.png"), filterPath);
        }
        if (!exist && str.endsWith(".9.png")) {
            exist = getZipInputStream(fileInfo, str.replace(".9.png", ".png"), filterPath);
        }
        return exist;
    }

    public long checkUpdate() {
        File file = new File(this.mPath);
        long modifyTime = file.lastModified();
        if (modifyTime != this.mLastModifyTime) {
            this.mLastModifyTime = modifyTime;
            closeZipFile();
            try {
                this.mZipFile = new VivoZipFile(file);
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return this.mLastModifyTime;
    }

    public boolean isFileExist() {
        return this.mPath != null ? new File(this.mPath).exists() : false;
    }

    public boolean isValid() {
        return getThemeZipFile() != null;
    }

    protected void finalize() throws Throwable {
        closeZipFile();
        super.finalize();
    }

    private void closeZipFile() {
        if (this.mZipFile != null) {
            try {
                this.mZipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mZipFile = null;
        }
    }

    private boolean getZipInputStream(VivoThemeFileInfo fileInfo, String path, String filter) {
        return getZipInputStream(fileInfo, path, filter, true);
    }

    private boolean getZipInputStream(VivoThemeFileInfo fileInfo, String path, String filter, boolean def) {
        try {
            ZipEntry zipEntry = getThemeZipFile().getEntry(path);
            if (zipEntry == null && def) {
                path = getNameFromPath(path);
                if (!TextUtils.isEmpty(filter)) {
                    path = filter + "/" + path;
                }
                zipEntry = getThemeZipFile().getEntry(path);
            }
            if (zipEntry != null) {
                fileInfo.resourcePath = this.mPath + "/" + path;
                if (fileInfo.requestStream) {
                    fileInfo.inputStream = getThemeZipFile().getInputStream(zipEntry);
                    if (fileInfo.inputStream == null) {
                        return false;
                    }
                }
                updateFileInfoDensity(fileInfo, path);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getNameFromPath(String path) {
        int i = path.lastIndexOf(47);
        if (i < 0) {
            return path;
        }
        path = path.substring(i + 1);
        if (path.endsWith("xml")) {
            return path.replace(".xml", ".png");
        }
        return path;
    }

    private static String getPathName(String pkg) {
        if (pkg == null || !pkg.equals(PKG_ANDROID)) {
            return pkg;
        }
        return PKG_FRAMEWORK;
    }

    private String getDensityResPath(Resources res) {
        switch (res.getDisplayMetrics().densityDpi) {
            case 240:
                return "drawable-sw360dp-hdpi";
            case 320:
                return "drawable-sw360dp-xhdpi";
            case 640:
                return "drawable-sw360dp-xxxhdpi";
            default:
                return null;
        }
    }

    private void updateFileInfoDensity(VivoThemeFileInfo fileInfo, String path) {
        int i = path.lastIndexOf(47);
        if (i > 0) {
            path = path.substring(0, i);
            i = path.lastIndexOf(47);
            if (i > 0) {
                path = path.substring(i + 1);
            }
        }
        if (path.equals("drawable-sw360dp-hdpi")) {
            fileInfo.density = 240;
        } else if (path.equals("drawable-sw360dp-xhdpi")) {
            fileInfo.density = 320;
        } else if (path.equals("drawable-sw360dp-xxhdpi")) {
            fileInfo.density = 480;
        } else if (path.equals("drawable-sw360dp-xxxhdpi")) {
            fileInfo.density = 640;
        }
    }
}
