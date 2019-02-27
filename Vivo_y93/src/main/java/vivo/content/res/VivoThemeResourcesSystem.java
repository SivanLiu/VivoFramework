package vivo.content.res;

import android.content.res.Resources;
import android.content.res.ResourcesImpl.VivoThemeFileInfo;
import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import vivo.app.security.AbsVivoPermissionManager;

public class VivoThemeResourcesSystem extends VivoThemeResources {
    public static final int FILTER_FALSE = 0;
    public static final int FILTER_NONE = -1;
    public static final int FILTER_TRUE = 1;
    private static final String TAG = "VivoThemeResourcesSystem";
    private static VivoThemeResources sFramework;
    private String mFilterPath;
    private HashMap<String, Boolean> mPackages;

    public VivoThemeResourcesSystem(Resources res, String pkg) {
        super(res, pkg);
    }

    public static VivoThemeResourcesSystem getThemeSystem(Resources res) {
        sFramework = new VivoThemeResources(res, VivoThemeZipFile.PKG_FRAMEWORK);
        return new VivoThemeResourcesSystem(res, VivoThemeZipFile.PKG_VIVO);
    }

    public long checkUpdate() {
        return Math.max(super.checkUpdate(), sFramework.checkUpdate());
    }

    public void closeVivoZipFileForPreloaded() {
        super.closeVivoZipFileForPreloaded();
        if (sFramework != null) {
            sFramework.closeVivoZipFileForPreloaded();
        }
    }

    protected void loadThemeValues() {
        loadFilters();
        super.loadThemeValues();
    }

    public void mergeThemeValues(VivoThemeValues themeValues) {
        super.mergeThemeValues(themeValues);
        VivoThemeValues.mergeValues(themeValues, sFramework.mThemeValues);
    }

    public boolean getThemeFile(VivoThemeFileInfo fileInfo) {
        if (super.getThemeFile(fileInfo)) {
            return true;
        }
        return sFramework.getThemeFile(fileInfo);
    }

    public void clearCache() {
        sFramework.clearCache();
        super.clearCache();
        if (this.mPackages != null) {
            this.mPackages.clear();
        }
        this.mFilterPath = null;
    }

    public int getFilterInfo(String pkg) {
        if (this.mPackages == null || !this.mPackages.containsKey(pkg)) {
            return -1;
        }
        return ((Boolean) this.mPackages.get(pkg)).booleanValue() ? 1 : 0;
    }

    public VivoThemeValues getFrameworkValues() {
        return sFramework.mThemeValues;
    }

    public String getFilterPath() {
        return this.mFilterPath;
    }

    public void addIndependentPkg(String pkg, Boolean value) {
        if (this.mPackages == null) {
            this.mPackages = new HashMap();
            this.mPackages.put(pkg, value);
        } else if (!this.mPackages.containsKey(pkg) || this.mPackages.get(pkg) != value) {
            this.mPackages.put(pkg, value);
        }
    }

    private void loadFilters() {
        if (this.mPackages != null) {
            this.mPackages.clear();
        }
        InputStream is = this.mZipFile.getZipInputStream(VivoThemeZipFile.THEME_FILTER_FILE);
        if (is != null) {
            try {
                NodeList filterList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getElementsByTagName("filter");
                for (int i = 0; i < filterList.getLength(); i++) {
                    Element filterElement = (Element) filterList.item(i);
                    this.mFilterPath = filterElement.getAttribute("path");
                    if (!TextUtils.isEmpty(this.mFilterPath) && this.mFilterPath.indexOf("/") == -1) {
                        NodeList pkgList = filterElement.getElementsByTagName(AbsVivoPermissionManager.ACTION_KEY_PACKAGE);
                        for (int j = 0; j < pkgList.getLength(); j++) {
                            Element pkgElement = (Element) pkgList.item(j);
                            if (this.mPackages == null) {
                                this.mPackages = new HashMap();
                            }
                            this.mPackages.put(pkgElement.getAttribute("name"), Boolean.valueOf(pkgElement.getFirstChild().getNodeValue()));
                        }
                    }
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SAXException e2) {
                e2.printStackTrace();
                try {
                    is.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            } catch (IOException e32) {
                e32.printStackTrace();
                try {
                    is.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            } catch (ParserConfigurationException e4) {
                e4.printStackTrace();
                try {
                    is.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e32222) {
                    e32222.printStackTrace();
                }
                throw th;
            }
        }
    }
}
