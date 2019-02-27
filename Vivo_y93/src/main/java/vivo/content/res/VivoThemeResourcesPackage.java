package vivo.content.res;

import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.content.res.ResourcesImpl.VivoThemeFileInfo;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class VivoThemeResourcesPackage extends VivoThemeResources {
    private static final String TAG = "VivoThemeResourcesPackage";
    private static final ConcurrentHashMap<String, WeakReference<VivoThemeResourcesPackage>> sResourcePackages = new ConcurrentHashMap();

    public VivoThemeResourcesPackage(Resources res, String pkg) {
        super(res, pkg);
    }

    public static VivoThemeResourcesPackage getThemeResources(Resources res, String pkg) {
        WeakReference<VivoThemeResourcesPackage> ref = (WeakReference) sResourcePackages.get(pkg);
        VivoThemeResourcesPackage resourcesPackage = ref == null ? null : (VivoThemeResourcesPackage) ref.get();
        if (resourcesPackage != null) {
            return resourcesPackage;
        }
        resourcesPackage = new VivoThemeResourcesPackage(res, pkg);
        sResourcePackages.put(pkg, new WeakReference(resourcesPackage));
        return resourcesPackage;
    }

    public void clearCache() {
        sResourcePackages.clear();
    }

    protected void loadThemeValues() {
        super.loadThemeValues();
        if (VivoThemeResources.getSystem().getFilterInfo(this.mPkgName) == 0) {
            this.mThemeValues.putAll(VivoThemeValues.parseThemeValues(this.mResources, this.mZipFile.getZipInputStream(VivoThemeResources.getSystem().getFilterPath() + "/" + VivoThemeZipFile.THEME_VALUES_FILE), VivoThemeZipFile.PKG_VIVO, false));
        }
    }

    public void mergeThemeValues(VivoThemeValues themeValues) {
        int filterType = VivoThemeResources.getSystem().getFilterInfo(this.mPkgName);
        super.mergeThemeValues(themeValues);
        if (filterType == 1) {
            VivoThemeValues.mergeValues(themeValues, VivoThemeResources.getSystem().mThemeValues.mSystemItems, this.mResources, this.mPkgName);
            VivoThemeValues.mergeValues(themeValues, VivoThemeResources.getSystem().getFrameworkValues().mSystemItems, this.mResources, this.mPkgName);
        }
        VivoThemeValues.mergeValues(themeValues, VivoThemeResources.getSystem().mThemeValues);
        VivoThemeValues.mergeValues(themeValues, VivoThemeResources.getSystem().getFrameworkValues());
    }

    public boolean getThemeFile(VivoThemeFileInfo fileInfo) {
        int filterType = VivoThemeResources.getSystem().getFilterInfo(this.mPkgName);
        if (filterType == 1) {
            if (super.getThemeFile(fileInfo)) {
                return true;
            }
            return VivoThemeResources.getSystem().getThemeFile(fileInfo);
        } else if (!ResourcesImpl.isSystemCookie(fileInfo.cookie)) {
            return super.getThemeFile(fileInfo);
        } else {
            if (filterType == 0 && this.mZipFile.getThemeFile(this.mResources, fileInfo, VivoThemeResources.getSystem().getFilterPath())) {
                return true;
            }
            return VivoThemeResources.getSystem().getThemeFile(fileInfo);
        }
    }
}
