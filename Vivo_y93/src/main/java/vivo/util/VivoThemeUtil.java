package vivo.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.TypedValue;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class VivoThemeUtil {
    /* renamed from: -vivo-util-VivoThemeUtil$ThemeTypeSwitchesValues */
    private static final /* synthetic */ int[] f1-vivo-util-VivoThemeUtil$ThemeTypeSwitchesValues = null;
    private static final String KEY_THEME = "ro.vivo.rom.style";
    private static final String THEME_STYLE = SystemProperties.get(KEY_THEME, "vigour");
    private static int[] sTempCache = new int[1];

    public enum ThemeType {
        SYSTEM_DEFAULT,
        DIALOG_ALERT,
        CONTEXT_MENU_DIALOG,
        INPUT_METHOD,
        BOOT_NOTIFY_DIALOG,
        FULL_SCREEN,
        DIALOG_SLIDE
    }

    /* renamed from: -getvivo-util-VivoThemeUtil$ThemeTypeSwitchesValues */
    private static /* synthetic */ int[] m0-getvivo-util-VivoThemeUtil$ThemeTypeSwitchesValues() {
        if (f1-vivo-util-VivoThemeUtil$ThemeTypeSwitchesValues != null) {
            return f1-vivo-util-VivoThemeUtil$ThemeTypeSwitchesValues;
        }
        int[] iArr = new int[ThemeType.values().length];
        try {
            iArr[ThemeType.BOOT_NOTIFY_DIALOG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ThemeType.CONTEXT_MENU_DIALOG.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ThemeType.DIALOG_ALERT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ThemeType.DIALOG_SLIDE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ThemeType.FULL_SCREEN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ThemeType.INPUT_METHOD.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ThemeType.SYSTEM_DEFAULT.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        f1-vivo-util-VivoThemeUtil$ThemeTypeSwitchesValues = iArr;
        return iArr;
    }

    public static String getSystemThemeString() {
        return THEME_STYLE;
    }

    public static boolean isBrightStyle() {
        return "vigour".equals(THEME_STYLE);
    }

    public static boolean isDarkStyle() {
        return "black".equals(THEME_STYLE);
    }

    public static boolean isVigourTheme(Context context) {
        TypedArray typeArray = context.obtainStyledAttributes(R.styleable.VigourFeature);
        boolean hasStyleFlag = typeArray.getBoolean(0, false);
        typeArray.recycle();
        return hasStyleFlag;
    }

    public static boolean isVigourThemeRaw(int themeRes) {
        if (themeRes == 51315048 || themeRes == vivo.R.style.Theme_Vigour_Light || themeRes == vivo.R.style.Theme_Vigour_Dark) {
            return true;
        }
        return false;
    }

    public static int getSystemThemeStyle(ThemeType type) {
        if (isBrightStyle()) {
            switch (m0-getvivo-util-VivoThemeUtil$ThemeTypeSwitchesValues()[type.ordinal()]) {
                case 1:
                    return 51315059;
                case 2:
                    return 51315054;
                case 3:
                    return vivo.R.style.Theme_Vigour_Light_Dialog_Alert;
                case 4:
                    return vivo.R.style.Theme_Vigour_Light_Dialog_Alert_Slide;
                case 5:
                    return 51315057;
                case 6:
                    return 51315052;
                case 7:
                    return vivo.R.style.Theme_Vigour_Light;
                default:
                    return 0;
            }
        } else if (!isDarkStyle()) {
            return 0;
        } else {
            switch (m0-getvivo-util-VivoThemeUtil$ThemeTypeSwitchesValues()[type.ordinal()]) {
                case 1:
                    return 51315060;
                case 2:
                    return 51315055;
                case 3:
                    return vivo.R.style.Theme_Vigour_Dark_Dialog_Alert;
                case 5:
                    return 51315058;
                case 6:
                    return 51315053;
                case 7:
                    return vivo.R.style.Theme_Vigour_Dark;
                default:
                    return 0;
            }
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static int getAttributeResId(Context context, int attrId) {
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static Drawable getDrawable(Context context, int attrId) {
        sTempCache[0] = attrId;
        TypedArray typeArray = context.obtainStyledAttributes(sTempCache);
        Drawable drawable = typeArray.getDrawable(0);
        typeArray.recycle();
        return drawable;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static int getColor(Context context, int attrId) {
        sTempCache[0] = attrId;
        TypedArray typeArray = context.obtainStyledAttributes(sTempCache);
        int color = typeArray.getColor(0, 0);
        typeArray.recycle();
        return color;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static ColorStateList getColorStateList(Context context, int attrId) {
        sTempCache[0] = attrId;
        TypedArray typeArray = context.obtainStyledAttributes(sTempCache);
        ColorStateList color = typeArray.getColorStateList(0);
        typeArray.recycle();
        return color;
    }
}
