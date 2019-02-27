package android.content.res;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class ExtraConfiguration implements Comparable {
    public static final int THEME_CONFIG_CHANGED = Integer.MIN_VALUE;
    public int themeId;

    public static boolean updateThemeConfiguration(ExtraConfiguration extraCon) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null) {
            long token = Binder.clearCallingIdentity();
            try {
                Configuration config = am.getConfiguration();
                config.extraConfig = extraCon;
                am.updateConfiguration(config);
            } catch (RemoteException e) {
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return true;
    }

    public int compareTo(Object obj) {
        return compareTo((ExtraConfiguration) obj);
    }

    public int compareTo(ExtraConfiguration extraconfiguration) {
        if (this.themeId != extraconfiguration.themeId) {
            return 1;
        }
        return 0;
    }

    public int diff(ExtraConfiguration extraconfiguration) {
        if (extraconfiguration.themeId == 0 || this.themeId == extraconfiguration.themeId) {
            return 0;
        }
        return Integer.MIN_VALUE;
    }

    public int hashCode() {
        return this.themeId;
    }

    public void readFromParcel(Parcel parcel) {
        this.themeId = parcel.readInt();
    }

    public void setTo(ExtraConfiguration extraconfiguration) {
        this.themeId = extraconfiguration.themeId;
    }

    public void setToDefaults() {
        this.themeId = 0;
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(" themeId=");
        stringbuilder.append(this.themeId);
        return stringbuilder.toString();
    }

    public int updateFrom(ExtraConfiguration extraconfiguration) {
        if (extraconfiguration.themeId == 0 || this.themeId == extraconfiguration.themeId) {
            return 0;
        }
        this.themeId = extraconfiguration.themeId;
        return Integer.MIN_VALUE;
    }

    public void updateTheme(long l) {
        this.themeId++;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.themeId);
    }

    public static boolean needNewResources(int configChanges) {
        if ((Integer.MIN_VALUE & configChanges) != 0) {
            return true;
        }
        return false;
    }
}
