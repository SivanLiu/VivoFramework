package com.vivo.alphaindex;

public final class Transliterator {
    private long peer;

    private static native long create(String str);

    private static native void destroy(long j);

    public static native String[] getAvailableIDs();

    private static native String transliterate(long j, String str);

    public Transliterator(String id) {
        this.peer = create(id);
    }

    protected synchronized void finalize() throws Throwable {
        try {
            destroy(this.peer);
            this.peer = 0;
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public String transliterate(String s) {
        return transliterate(this.peer, s);
    }
}
