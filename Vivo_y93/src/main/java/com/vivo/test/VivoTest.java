package com.vivo.test;

public class VivoTest {
    public String path = "vendor/vivo/source";

    public static native String hello();

    public static String getLineInfo() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName() + ": Line " + ste.getLineNumber();
    }

    public static void Debug() {
        System.out.println("===>" + getLineInfo());
    }
}
