package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.service.notification.ZenModeConfig;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.vivo.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class VivoTheme {
    public static final String EXTRA_ASSET_PATH = "/system/framework/vivo-res.apk";
    public static final String TAG = "VivoTheme";
    private static final String TAG_THEMEICONS = "ThemeIcons";
    private static String defaultIcontonePath = "icons/";
    private static String defaultThemePath = "/oem/etc/theme/";
    private static int icon_id = 0;
    private static String[] launcherIcon = new String[]{"app_folder_icon", "calendar_bg", "folder_icon", "folder_icon_cover", "icon_bg", "icon_border", "icon_cover", "icon_mask", "launcher_bottom_bar_bg"};
    private static HashMap<String, HashMap<String, String>> mImageMatcher = new HashMap();
    private static HashMap<String, ThemeIconItem> mItemsMap = new HashMap();
    private static HashMap<String, String> mPackageMatcher = new HashMap();
    private static ArrayList<ThemeIconItem> mThemeIconItem = new ArrayList();
    private static String packageName = null;
    private static final String themeBasePath = "/data/bbkcore/theme/";

    public static class ThemeIconItem {
        private String mIconEntry;
        private String mIconPath;
        private String mPackageName;

        public void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        public void setIconEntry(String iconEntry) {
            this.mIconEntry = iconEntry;
        }

        public void setIconPath(String iconPath) {
            this.mIconPath = iconPath;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public String getIconEntry() {
            return this.mIconEntry;
        }

        public String getIconPath() {
            return this.mIconPath;
        }
    }

    public static Drawable getLauncherIcon(Resources res, TypedValue value, int id) {
        String packageName = res.getResourcePackageName(id);
        String resourceName = res.getResourceEntryName(id);
        File file = null;
        if (packageName.equals("com.bbk.launcher2") || packageName.equals(ZenModeConfig.SYSTEM_AUTHORITY) || packageName.equals("vivo") || packageName.equals("com.bbk.scene.tech") || packageName.equals("com.bbk.scene.launcher.theme")) {
            boolean isLauncherIcon = false;
            for (CharSequence contains : launcherIcon) {
                if (resourceName.contains(contains)) {
                    isLauncherIcon = true;
                    break;
                }
            }
            String path = getThemePath();
            if (isLauncherIcon) {
                if (resourceName.contains("vigour")) {
                    resourceName = resourceName.replace("vigour_", "");
                }
                file = new File(path + "launcher/" + resourceName + ".png");
            } else if (resourceName.equals("sym_def_app_icon")) {
                file = new File(path + defaultIcontonePath + "ic_launcher_default_icon.png");
            }
            if (file != null && file.exists()) {
                try {
                    InputStream ins = new FileInputStream(file);
                    Drawable drawable = Drawable.createFromResourceStream(res, value, ins, null, null);
                    ins.close();
                    return drawable;
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static void loadThemeIcons(Resources res) {
        int index = 0;
        if (mThemeIconItem.size() <= 0) {
            try {
                XmlResourceParser parser = res.getXml(R.xml.theme_infomation);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                XmlUtils.beginDocument(parser, TAG_THEMEICONS);
                int depth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                        break;
                    }
                    TypedArray a = res.newTheme().obtainStyledAttributes(attrs, R.styleable.ThemeIcon, 0, 0);
                    String packageName = a.getString(0);
                    String iconEntry = a.getString(1);
                    String iconPath = a.getString(2);
                    ThemeIconItem item = new ThemeIconItem();
                    item.setPackageName(packageName);
                    item.setIconEntry(iconEntry);
                    item.setIconPath(iconPath);
                    mThemeIconItem.add(item);
                    if (!mItemsMap.containsKey(packageName)) {
                        mItemsMap.put(packageName, item);
                    }
                    index++;
                    a.recycle();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static Drawable getAppIconDrawable(Resources res, TypedValue value, String packageName, String activityName) {
        try {
            String fileName;
            File file;
            File file2;
            loadPackageMatchInfo(res);
            String newPkg = getNewPackage(packageName);
            String path = getThemePath();
            String icontonePath = getIcontonePath(path);
            if (activityName == null) {
                fileName = packageName + ".png";
                file = new File(path + icontonePath + fileName);
                try {
                    if (!file.exists()) {
                        file = new File(path + defaultIcontonePath + fileName);
                    }
                    if (!(newPkg == null || (file.exists() ^ 1) == 0)) {
                        fileName = newPkg + ".png";
                        file = new File(path + icontonePath + fileName);
                    }
                } catch (IOException e) {
                    file2 = file;
                }
            } else {
                fileName = activityName + ".png";
                file = new File(path + icontonePath + fileName);
                if (!file.exists()) {
                    file = new File(path + defaultIcontonePath + fileName);
                }
                if (!file.exists()) {
                    if (newPkg != null) {
                        packageName = newPkg;
                    }
                    fileName = packageName + ".png";
                    file = new File(path + icontonePath + fileName);
                }
            }
            InputStream inputStream = null;
            if (file.exists()) {
                file2 = file;
            } else {
                file2 = new File(path + defaultIcontonePath + fileName);
            }
            if (file2 != null) {
                if (file2.exists()) {
                    inputStream = new FileInputStream(file2);
                }
            }
            if (inputStream != null) {
                Drawable mDrawable;
                if (res != null) {
                    mDrawable = Drawable.createFromResourceStream(res, value, inputStream, null, null);
                } else {
                    mDrawable = Drawable.createFromStream(inputStream, null);
                }
                inputStream.close();
                if (mDrawable != null) {
                    return mDrawable;
                }
            }
        } catch (IOException e2) {
        }
        return null;
    }

    public static void setIconId(int id) {
        icon_id = id;
    }

    public static int getIconId() {
        return icon_id;
    }

    public static void setIconPackageName(String packagename) {
        packageName = packagename;
    }

    public static String getIconPackageName() {
        return packageName;
    }

    public static int getThemeResoucesDensity() {
        return 480;
    }

    public static InputStream getThemeInputStream(Resources res, String packageName, TypedValue value) {
        if (mItemsMap == null) {
            return null;
        }
        String path = getThemePath();
        try {
            if (mItemsMap.containsKey(packageName)) {
                int index = 0;
                while (index < mThemeIconItem.size()) {
                    if (packageName.equals(((ThemeIconItem) mThemeIconItem.get(index)).getPackageName())) {
                        String[] entryName = value.string.toString().split("/");
                        if (entryName.length > 0 && entryName[entryName.length - 1].equals(((ThemeIconItem) mThemeIconItem.get(index)).getIconEntry())) {
                            String iconPath = ((ThemeIconItem) mThemeIconItem.get(index)).getIconPath();
                            if (iconPath.contains("vigour")) {
                                iconPath = iconPath.replace("vigour_", "");
                            }
                            File iconFile = new File(path + iconPath);
                            if (iconFile.exists()) {
                                return new FileInputStream(iconFile);
                            }
                        }
                    }
                    index++;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static boolean isSystemIcon(Resources res, int id) {
        if (res == null || id == 0) {
            return false;
        }
        return isSystemIcon(res.getResourcePackageName(id));
    }

    public static boolean isSystemIcon(String packageName) {
        if (packageName == null) {
            return false;
        }
        String path = getThemePath();
        String icontonePath = getIcontonePath(path);
        String pathName = path + icontonePath + packageName + ".png";
        if (packageName.equals("com.bbk.calendar")) {
            return false;
        }
        if (new File(pathName).exists() || new File(path + defaultIcontonePath + packageName + ".png").exists()) {
            return true;
        }
        loadPackageMatchInfo(Resources.getSystem());
        String newPkg = getNewPackage(packageName);
        return newPkg != null && (new File(path + icontonePath + newPkg + ".png").exists() || new File(path + defaultIcontonePath + newPkg + ".png").exists());
    }

    /* JADX WARNING: Missing block: B:59:0x011a, code:
            if (r11 == null) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:61:0x0122, code:
            if ((r5.isEmpty() ^ 1) == 0) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:62:0x0124, code:
            mImageMatcher.put(r11, r5);
     */
    /* JADX WARNING: Missing block: B:63:0x0129, code:
            android.util.Log.d(TAG, "load old package name end");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadPackageMatchInfo(Resources res) {
        if (res != null) {
            Log.d(TAG, "load old package name start");
            try {
                XmlResourceParser xmlParser = res.getXml(R.xml.wholetheme_config);
                try {
                    int event = xmlParser.getEventType();
                    Object pkg = null;
                    HashMap<String, String> map = new HashMap();
                    synchronized (mPackageMatcher) {
                        synchronized (mImageMatcher) {
                            if (mPackageMatcher.isEmpty() && (mImageMatcher.isEmpty() ^ 1) == 0) {
                                while (event != 1) {
                                    switch (event) {
                                        case 2:
                                            if ("package".equals(xmlParser.getName())) {
                                                String newPackage = xmlParser.getAttributeValue(0);
                                                String oldPackage = xmlParser.nextText();
                                                Log.i(TAG, "new : " + newPackage + "   old : " + oldPackage);
                                                mPackageMatcher.put(newPackage, oldPackage);
                                            } else if ("images".equals(xmlParser.getName())) {
                                                if (!(pkg == null || (map.isEmpty() ^ 1) == 0)) {
                                                    mImageMatcher.put(pkg, map);
                                                }
                                                pkg = xmlParser.getAttributeValue(0);
                                                map = (HashMap) mImageMatcher.get(pkg);
                                                if (map == null) {
                                                    map = new HashMap();
                                                }
                                            } else if ("image".equals(xmlParser.getName())) {
                                                String nImage = xmlParser.getAttributeValue(0);
                                                String oImage = xmlParser.nextText();
                                                Log.i(TAG, "nImage : " + nImage + "   oimage: " + oImage);
                                                map.put(nImage, oImage);
                                            }
                                        default:
                                            event = xmlParser.next();
                                    }
                                }
                            }
                        }
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (NotFoundException e3) {
                e3.printStackTrace();
            }
        }
    }

    public static String getNewPackage(String newPkg) {
        synchronized (mPackageMatcher) {
            if (mPackageMatcher.isEmpty()) {
                return null;
            }
            String str = (String) mPackageMatcher.get(newPkg);
            return str;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getNewImage(String pkg, String newImage) {
        synchronized (mImageMatcher) {
            if (!mImageMatcher.isEmpty()) {
                HashMap<String, String> map = (HashMap) mImageMatcher.get(pkg);
                if (map != null) {
                    String str = (String) map.get(newImage);
                    return str;
                }
            }
        }
    }

    public static synchronized String getThemePath() {
        String path;
        synchronized (VivoTheme.class) {
            String color = SystemProperties.get("persist.sys.theme.color", "null");
            String colorPath = null;
            if (!"null".equals(color)) {
                colorPath = defaultThemePath + "custom/" + color + "/";
            }
            File theme = new File("/data/bbkcore/theme/launcher/icon_mask.png");
            path = themeBasePath;
            if (!theme.exists()) {
                if (colorPath == null || !new File(colorPath + "launcher/icon_mask.png").exists()) {
                    path = defaultThemePath;
                } else {
                    path = colorPath;
                }
            }
        }
        return path;
    }

    public static synchronized String getIcontonePath(String themePath) {
        String path;
        synchronized (VivoTheme.class) {
            String colortone = SystemProperties.get("persist.sys.theme.colortone", "null");
            String colortonePath = null;
            if (!"null".equals(colortone)) {
                colortonePath = defaultIcontonePath + colortone + "/";
            }
            path = colortonePath;
            if (!new File(themePath + colortonePath).exists()) {
                path = defaultIcontonePath;
            }
        }
        return path;
    }
}
