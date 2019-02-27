package com.vivo.alphaindex;

import java.util.Locale;

public final class AlphabeticIndex {
    private long peer;

    public static final class ImmutableIndex {
        private long peer;

        /* synthetic */ ImmutableIndex(long peer, ImmutableIndex -this1) {
            this(peer);
        }

        private static native int getBucketCount(long j);

        private static native int getBucketIndex(long j, String str);

        private static native String getBucketLabel(long j, int i);

        private ImmutableIndex(long peer) {
            this.peer = peer;
        }

        protected synchronized void finalize() throws Throwable {
            try {
                AlphabeticIndex.destroy(this.peer);
                this.peer = 0;
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        public int getBucketCount() {
            return getBucketCount(this.peer);
        }

        public int getBucketIndex(String s) {
            return getBucketIndex(this.peer, s);
        }

        public String getBucketLabel(int index) {
            return getBucketLabel(this.peer, index);
        }
    }

    private static native void addLabelRange(long j, int i, int i2);

    private static native void addLabels(long j, String str);

    private static native long buildImmutableIndex(long j);

    private static native long create(String str);

    private static native void destroy(long j);

    private static native int getBucketCount(long j);

    private static native int getBucketIndex(long j, String str);

    private static native String getBucketLabel(long j, int i);

    private static native int getMaxLabelCount(long j);

    private static native void setMaxLabelCount(long j, int i);

    public AlphabeticIndex(Locale locale) {
        this.peer = create(locale.toString());
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

    public synchronized int getMaxLabelCount() {
        return getMaxLabelCount(this.peer);
    }

    public synchronized AlphabeticIndex setMaxLabelCount(int count) {
        setMaxLabelCount(this.peer, count);
        return this;
    }

    public synchronized AlphabeticIndex addLabels(Locale locale) {
        addLabels(this.peer, locale.toString());
        return this;
    }

    public synchronized AlphabeticIndex addLabelRange(int codePointStart, int codePointEnd) {
        addLabelRange(this.peer, codePointStart, codePointEnd);
        return this;
    }

    public synchronized int getBucketCount() {
        return getBucketCount(this.peer);
    }

    public synchronized int getBucketIndex(String s) {
        return getBucketIndex(this.peer, s);
    }

    public synchronized String getBucketLabel(int index) {
        return getBucketLabel(this.peer, index);
    }

    public synchronized ImmutableIndex getImmutableIndex() {
        return new ImmutableIndex(buildImmutableIndex(this.peer), null);
    }
}
