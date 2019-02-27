package com.vivo.services.security.client;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.vivo.services.security.client.VivoPermissionType.VivoPermissionCategory;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public class VivoPermissionInfo implements Parcelable {
    public static final Creator<VivoPermissionInfo> CREATOR = new Creator<VivoPermissionInfo>() {
        public VivoPermissionInfo createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            VivoPermissionInfo config = new VivoPermissionInfo(in.readString());
            config.mIsWhiteListApp = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.mIsBlackListApp = z;
            in.readIntArray(config.mPermissionResults);
            in.readIntArray(config.mPermissionBackup);
            if (in.readInt() == 0) {
                z2 = false;
            }
            config.mIsConfigured = z2;
            return config;
        }

        public VivoPermissionInfo[] newArray(int size) {
            return new VivoPermissionInfo[size];
        }
    };
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED = 2;
    public static final int DENIED_DIALOG_FLAG = 3840;
    public static final int DENIED_DIALOG_MAXVALUE = 1024;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_DIALOG_MODE_COUNTDOWN_CHOOSE = 768;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_DIALOG_MODE_NO_COUNTDOWN_CHOOSE = 512;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_DIALOG_MODE_NO_COUNTDOWN_SETTING = 256;
    public static final int DENIED_IMEI_TIPS_ONE_FLAG = 61440;
    public static final int DENIED_IMEI_TIPS_ONE_FLAG_BASE = 4096;
    public static final int DENIED_IMEI_TIPS_TWO_FLAG = 983040;
    public static final int DENIED_IMEI_TIPS_TWO_FLAG_BASE = 65536;
    public static final int DENIED_MODE_FLAG = 240;
    public static final int DENIED_MODE_MAXVALUE = 96;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_MODE_NOT_SHOW = 16;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_MODE_SHOWDIALOG_EVERY_TIME = 64;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_MODE_SHOWDIALOG_ONE_TIME = 48;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_MODE_SHOWDIALOG_ONE_TIMES_USED = 80;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int DENIED_MODE_SHOWDIALOG_ZERO_TIME = 32;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int GRANTED = 1;
    public static final int INITIAL = 4;
    public static final int MAX_VALUE = 5;
    public static final int PERMISSION_FLAG = 15;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int UNKNOWN = 0;
    @VivoHook(hookType = VivoHookType.PUBLIC_API_FIELD)
    public static final int WARNING = 3;
    private boolean mIsBlackListApp = false;
    private boolean mIsConfigured = false;
    private boolean mIsWhiteListApp = false;
    private String mPackageName = null;
    private int[] mPermissionBackup = null;
    private int[] mPermissionResults = null;

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionInfo(String packageName) {
        this.mPackageName = packageName;
        this.mPermissionResults = new int[30];
        this.mPermissionBackup = new int[30];
        for (int index = 0; index < 30; index++) {
            this.mPermissionResults[index] = 0;
            this.mPermissionBackup[index] = 0;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("" + this.mPackageName + " {");
        for (int index = 0; index < 30; index++) {
            if (VivoPermissionType.getVPType(index).getVPCategory() != VivoPermissionCategory.OTHERS) {
                buf.append(VivoPermissionType.getVPType(index) + " = " + this.mPermissionResults[index]);
                buf.append(" ,");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public void copyFrom(VivoPermissionInfo newVpi) {
        if (this.mPackageName.equals(newVpi.getPackageName())) {
            this.mIsWhiteListApp = newVpi.isWhiteListApp();
            this.mIsBlackListApp = newVpi.isBlackListApp();
            this.mIsConfigured = newVpi.isConfigured();
            for (int index = 0; index < 30; index++) {
                this.mPermissionResults[index] = newVpi.getAllPermission(index);
                this.mPermissionBackup[index] = newVpi.getAllPermissionBackup(index);
            }
        }
    }

    public void updateFrom(VivoPermissionInfo oldVpi) {
        if (this.mPackageName.equals(oldVpi.getPackageName())) {
            boolean isCopyBlackWhiteList = true;
            for (int index = 0; index < 30; index++) {
                if (oldVpi.getPermissionResult(index) == 0) {
                    if (getPermissionResult(index) != 0) {
                        VivoPermissionManager.printfInfo("updateFrom add permission:" + VivoPermissionType.getVPType(index));
                        isCopyBlackWhiteList = false;
                    }
                } else if (getPermissionResult(index) == 0) {
                    VivoPermissionManager.printfInfo("updateFrom remove permission:" + VivoPermissionType.getVPType(index));
                } else {
                    setAllPermission(index, oldVpi.getAllPermission(index));
                    setAllPermissionBackup(index, oldVpi.getAllPermissionBackup(index));
                }
            }
            this.mIsBlackListApp = oldVpi.isBlackListApp();
            this.mIsConfigured = oldVpi.isConfigured();
            this.mIsWhiteListApp = oldVpi.isWhiteListApp();
            if (!isCopyBlackWhiteList) {
                this.mIsWhiteListApp = false;
            }
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static boolean isValidPermissionResult(int premResult) {
        int valueDenideMode = premResult & 240;
        int valueDialogDialogMode = premResult & DENIED_DIALOG_FLAG;
        if ((premResult & 15) < 5 && valueDenideMode < 96 && valueDialogDialogMode < 1024) {
            return true;
        }
        VivoPermissionManager.printfInfo("VPI isValidPermissionResult false premResult=" + premResult);
        return false;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public boolean isWhiteListApp() {
        return this.mIsWhiteListApp;
    }

    public void setWhiteListApp(boolean isWhiteListApp) {
        float osVer = VivoPermissionManager.getOSVersion();
        if (this.mIsWhiteListApp != isWhiteListApp) {
            int index;
            if (isWhiteListApp) {
                resetAllDeniedModeToOneTime();
                index = 0;
                while (index < 30) {
                    if (osVer < 3.0f) {
                        this.mPermissionBackup[index] = this.mPermissionResults[index];
                    }
                    if (!(getPermissionResult(index) == 0 || getDeniedMode(index) == 16)) {
                        setPermissionResult(index, 1);
                    }
                    index++;
                }
            } else if (osVer < 3.0f) {
                for (index = 0; index < 30; index++) {
                    this.mPermissionResults[index] = this.mPermissionBackup[index];
                }
            }
            this.mIsWhiteListApp = isWhiteListApp;
        }
    }

    private void resetAllDeniedModeToOneTime() {
        for (int index = 0; index < 30; index++) {
            if (getDeniedMode(index) == 80) {
                setDeniedMode(index, 48);
            }
        }
    }

    public void grantAllPermissions() {
        resetAllDeniedModeToOneTime();
        for (int index = 0; index < 30; index++) {
            if (getPermissionResult(index) != 0) {
                setPermissionResult(index, 1);
            }
        }
    }

    public boolean isBlackListApp() {
        return this.mIsBlackListApp;
    }

    public void setBlackListApp(boolean isBlackListApp) {
        this.mIsBlackListApp = isBlackListApp;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getPermissionResult(int vpTypeId) {
        return this.mPermissionResults[vpTypeId] & 15;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setPermissionResult(int vpTypeId, int permResult) {
        if (permResult >= 5) {
            VivoPermissionManager.printfError("VPI setPermissionResult error permResult:" + permResult);
            return;
        }
        if (this.mPermissionResults[vpTypeId] != permResult && getDeniedMode(vpTypeId) == 80) {
            setDeniedMode(vpTypeId, 48);
        }
        if (permResult == 1) {
            setTipsDialogOneMode(vpTypeId, 0);
            setTipsDialogTwoMode(vpTypeId, 0);
        }
        this.mPermissionResults[vpTypeId] = (this.mPermissionResults[vpTypeId] & -16) | permResult;
        if (VivoPermissionManager.getOSVersion() < 3.0f && this.mIsWhiteListApp && (permResult == 2 || permResult == 3)) {
            this.mIsWhiteListApp = false;
        }
    }

    public void setAllPermission(int vpTypeId, int perm) {
        this.mPermissionResults[vpTypeId] = perm;
    }

    public int getAllPermission(int vpTypeId) {
        return this.mPermissionResults[vpTypeId];
    }

    public void setAllPermissionBackup(int vpTypeId, int perm) {
        this.mPermissionBackup[vpTypeId] = perm;
    }

    public int getAllPermissionBackup(int vpTypeId) {
        return this.mPermissionBackup[vpTypeId];
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getDeniedMode(int vpTypeId) {
        return this.mPermissionResults[vpTypeId] & 240;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setDeniedMode(int vpTypeId, int deniedMode) {
        int deniedModeTemp = deniedMode & 240;
        if (deniedModeTemp >= 96 || deniedModeTemp <= 0) {
            VivoPermissionManager.printfError("VPI setDeniedMode error deniedMode:" + deniedMode);
            return;
        }
        this.mPermissionResults[vpTypeId] = (this.mPermissionResults[vpTypeId] & -241) | deniedModeTemp;
        if (VivoPermissionManager.getOSVersion() < 3.0f && this.mIsWhiteListApp) {
            if (deniedModeTemp == 80) {
                VivoPermissionManager.printfError("have some error problem check check check ");
            }
            this.mPermissionBackup[vpTypeId] = (this.mPermissionBackup[vpTypeId] & -241) | deniedModeTemp;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getDeniedDialogMode(int vpTypeId) {
        return this.mPermissionResults[vpTypeId] & DENIED_DIALOG_FLAG;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void setDeniedDialogMode(int vpTypeId, int deniedDialogMode) {
        int deniedDialogModeTemp = deniedDialogMode & DENIED_DIALOG_FLAG;
        if (deniedDialogModeTemp >= 1024 || deniedDialogModeTemp <= 0) {
            VivoPermissionManager.printfInfo("VPI setDeniedDialogMode error deniedDialogMode:" + deniedDialogMode);
            return;
        }
        this.mPermissionResults[vpTypeId] = (this.mPermissionResults[vpTypeId] & -3841) | deniedDialogModeTemp;
        if (VivoPermissionManager.getOSVersion() < 3.0f && this.mIsWhiteListApp) {
            this.mPermissionBackup[vpTypeId] = (this.mPermissionBackup[vpTypeId] & -3841) | deniedDialogModeTemp;
        }
    }

    public boolean isConfigured() {
        return this.mIsConfigured;
    }

    public void setConfigured(boolean isConfigured) {
        this.mIsConfigured = isConfigured;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int describeContents() {
        return 0;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mIsWhiteListApp ? 1 : 0);
        if (this.mIsBlackListApp) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeIntArray(this.mPermissionResults);
        dest.writeIntArray(this.mPermissionBackup);
        if (!this.mIsConfigured) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public int getTipsDialogOneMode(int vpTypeId) {
        return (this.mPermissionResults[vpTypeId] & DENIED_IMEI_TIPS_ONE_FLAG) / 4096;
    }

    public void setTipsDialogOneMode(int vpTypeId, int mode) {
        int[] iArr = this.mPermissionResults;
        iArr[vpTypeId] = iArr[vpTypeId] & -61441;
        iArr = this.mPermissionResults;
        iArr[vpTypeId] = iArr[vpTypeId] | (mode * 4096);
    }

    public int getTipsDialogTwoMode(int vpTypeId) {
        return (this.mPermissionResults[vpTypeId] & 983040) / 65536;
    }

    public void setTipsDialogTwoMode(int vpTypeId, int mode) {
        int[] iArr = this.mPermissionResults;
        iArr[vpTypeId] = iArr[vpTypeId] & -983041;
        iArr = this.mPermissionResults;
        iArr[vpTypeId] = iArr[vpTypeId] | (65536 * mode);
    }
}
