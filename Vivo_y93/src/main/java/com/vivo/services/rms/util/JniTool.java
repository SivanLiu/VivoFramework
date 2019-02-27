package com.vivo.services.rms.util;

public class JniTool {
    public static native String readFile(String str);

    public static native int writeFile(String str, String str2);
}
