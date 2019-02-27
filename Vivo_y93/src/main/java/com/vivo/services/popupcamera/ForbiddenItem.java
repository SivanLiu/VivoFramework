package com.vivo.services.popupcamera;

public class ForbiddenItem {
    public static final int MODE_BACKGROUND_USE_EXEMPTION = 5;
    public static final int MODE_FORBIDDEN_ALL = 1;
    public static final int MODE_FORBIDDEN_BACKGROUND_USE = 2;
    public static final int MODE_FORBIDDEN_NONE = 0;
    public static final int MODE_TAKEBACK_IMMEDIATELY = 4;
    public static final int MODE_WARN_USER = 3;
    public int mode;
    public String packageName;

    public ForbiddenItem(String name, int mode) {
        this.packageName = name;
        this.mode = mode;
    }

    public ForbiddenItem(String name) {
        this(name, 0);
    }

    private String mode2String(int mode) {
        switch (mode) {
            case 0:
                return "MODE_FORBIDDEN_NONE";
            case 1:
                return "MODE_FORBIDDEN_ALL";
            case 2:
                return "MODE_FORBIDDEN_BACKGROUND_USE";
            case 3:
                return "MODE_WARN_USER";
            case 4:
                return "MODE_TAKEBACK_IMMEDIATELY";
            case 5:
                return "MODE_BACKGROUND_USE_EXEMPTION";
            default:
                return "invalid";
        }
    }

    public String toString() {
        return "{ package " + this.packageName + " mode " + mode2String(this.mode) + " }";
    }
}
