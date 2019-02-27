package vivo.content.res;

import android.content.res.Resources;
import android.content.res.ResourcesImpl.VivoThemeFileInfo;
import android.util.Log;
import com.vivo.content.VivoTheme;

public class VivoThemeResources {
    private static VivoThemeResourcesSystem sSystem;
    private final String TAG = "VivoThemeResources";
    private String mOriginPkg;
    protected String mPkgName;
    protected Resources mResources;
    protected VivoThemeValues mThemeValues;
    protected long mUpdatedTime = -1;
    protected VivoThemeZipFile mZipFile;

    public void clearCache() {
        this.mThemeValues.clearCache();
        this.mZipFile.clearCache();
        this.mUpdatedTime = -1;
    }

    public void closeVivoZipFileForPreloaded() {
        if (this.mZipFile != null) {
            this.mZipFile.clearCache();
        }
    }

    public VivoThemeResources(Resources res, String pkg) {
        this.mResources = res;
        this.mOriginPkg = pkg;
        if (VivoThemeZipFile.PKG_VIVO.equals(pkg)) {
            VivoTheme.loadPackageMatchInfo(res);
        }
        if (VivoTheme.getNewPackage(this.mOriginPkg) != null) {
            pkg = VivoTheme.getNewPackage(this.mOriginPkg);
            Log.i("VivoThemeResources", "use new package (" + pkg + ") replace package (" + this.mOriginPkg + ")");
        }
        this.mPkgName = pkg;
        this.mThemeValues = new VivoThemeValues();
        this.mZipFile = VivoThemeZipFile.getThemeZipFile(pkg);
        if (!VivoThemeZipFile.PKG_FRAMEWORK.equals(pkg)) {
            checkUpdate();
        }
    }

    public static VivoThemeResourcesSystem getSystem(Resources res) {
        if (sSystem == null) {
            sSystem = VivoThemeResourcesSystem.getThemeSystem(res);
        }
        return sSystem;
    }

    public static void resetSystemTheme() {
        sSystem = null;
    }

    public static VivoThemeResourcesSystem getSystem() {
        if (sSystem == null) {
            sSystem = getSystem(Resources.getSystem());
        }
        return sSystem;
    }

    public void mergeThemeValues(VivoThemeValues themeValues) {
        if (this.mZipFile.isFileExist()) {
            themeValues.putAll(this.mThemeValues);
        }
    }

    public boolean getThemeFile(VivoThemeFileInfo fileInfo) {
        return this.mZipFile.getThemeFile(this.mResources, fileInfo);
    }

    public long checkUpdate() {
        int id = this.mResources.getIdentifier("is_apk_independent", "bool", this.mPkgName);
        if (id > 0) {
            getSystem().addIndependentPkg(this.mPkgName, Boolean.valueOf(this.mResources.getBoolean(id)));
        }
        if (this.mZipFile.isFileExist()) {
            long zipUpdateTime = this.mZipFile.checkUpdate();
            if (zipUpdateTime != this.mUpdatedTime) {
                this.mUpdatedTime = zipUpdateTime;
                loadThemeValues();
            }
        } else {
            clearCache();
        }
        return this.mUpdatedTime;
    }

    protected void loadThemeValues() {
        this.mThemeValues = VivoThemeValues.parseThemeValues(this.mResources, this.mZipFile.getZipInputStream(VivoThemeZipFile.THEME_VALUES_FILE), this.mOriginPkg, isSystemResource());
    }

    private boolean isSystemResource() {
        return !VivoThemeZipFile.PKG_FRAMEWORK.equals(this.mPkgName) ? VivoThemeZipFile.PKG_VIVO.equals(this.mPkgName) : true;
    }
}
