package android.os;

import android.system.ErrnoException;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MemoryFile {
    private static final int PROT_READ = 1;
    private static final int PROT_WRITE = 2;
    private static String TAG = "MemoryFile";
    private long mAddress;
    private boolean mAllowPurging = false;
    private FileDescriptor mFD = null;
    private int mLength;
    private ByteBuffer mMapping;
    private SharedMemory mSharedMemory;
    private boolean mUsedFD = false;

    private class MemoryInputStream extends InputStream {
        private int mMark;
        private int mOffset;
        private byte[] mSingleByte;

        /* synthetic */ MemoryInputStream(MemoryFile this$0, MemoryInputStream -this1) {
            this();
        }

        private MemoryInputStream() {
            this.mMark = 0;
            this.mOffset = 0;
        }

        public int available() throws IOException {
            if (MemoryFile.this.mUsedFD) {
                if (this.mOffset >= MemoryFile.this.mLength) {
                    return 0;
                }
                return MemoryFile.this.mLength - this.mOffset;
            } else if (this.mOffset >= MemoryFile.this.mSharedMemory.getSize()) {
                return 0;
            } else {
                return MemoryFile.this.mSharedMemory.getSize() - this.mOffset;
            }
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readlimit) {
            this.mMark = this.mOffset;
        }

        public void reset() throws IOException {
            this.mOffset = this.mMark;
        }

        public int read() throws IOException {
            if (this.mSingleByte == null) {
                this.mSingleByte = new byte[1];
            }
            if (read(this.mSingleByte, 0, 1) != 1) {
                return -1;
            }
            return this.mSingleByte[0];
        }

        public int read(byte[] buffer, int offset, int count) throws IOException {
            if (offset < 0 || count < 0 || offset + count > buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, available());
            if (count < 1) {
                return -1;
            }
            int result = MemoryFile.this.readBytes(buffer, this.mOffset, offset, count);
            if (result > 0) {
                this.mOffset += result;
            }
            return result;
        }

        public long skip(long n) throws IOException {
            if (MemoryFile.this.mUsedFD) {
                if (((long) this.mOffset) + n > ((long) MemoryFile.this.mLength)) {
                    n = (long) (MemoryFile.this.mLength - this.mOffset);
                }
                this.mOffset = (int) (((long) this.mOffset) + n);
                return n;
            }
            if (((long) this.mOffset) + n > ((long) MemoryFile.this.mSharedMemory.getSize())) {
                n = (long) (MemoryFile.this.mSharedMemory.getSize() - this.mOffset);
            }
            this.mOffset = (int) (((long) this.mOffset) + n);
            return n;
        }
    }

    private class MemoryOutputStream extends OutputStream {
        private int mOffset;
        private byte[] mSingleByte;

        /* synthetic */ MemoryOutputStream(MemoryFile this$0, MemoryOutputStream -this1) {
            this();
        }

        private MemoryOutputStream() {
            this.mOffset = 0;
        }

        public void write(byte[] buffer, int offset, int count) throws IOException {
            MemoryFile.this.writeBytes(buffer, offset, this.mOffset, count);
            this.mOffset += count;
        }

        public void write(int oneByte) throws IOException {
            if (this.mSingleByte == null) {
                this.mSingleByte = new byte[1];
            }
            this.mSingleByte[0] = (byte) oneByte;
            write(this.mSingleByte, 0, 1);
        }
    }

    private static native void native_close(FileDescriptor fileDescriptor);

    private static native int native_get_size(FileDescriptor fileDescriptor) throws IOException;

    private static native long native_mmap(FileDescriptor fileDescriptor, int i, int i2) throws IOException;

    private static native void native_munmap(long j, int i) throws IOException;

    private static native FileDescriptor native_open(String str, int i) throws IOException;

    private static native boolean native_pin(FileDescriptor fileDescriptor, boolean z) throws IOException;

    private static native int native_read(FileDescriptor fileDescriptor, long j, byte[] bArr, int i, int i2, int i3, boolean z) throws IOException;

    private static native void native_write(FileDescriptor fileDescriptor, long j, byte[] bArr, int i, int i2, int i3, boolean z) throws IOException;

    public MemoryFile(String name, int length) throws IOException {
        try {
            this.mSharedMemory = SharedMemory.create(name, length);
            this.mMapping = this.mSharedMemory.mapReadWrite();
        } catch (ErrnoException ex) {
            ex.rethrowAsIOException();
        }
    }

    public MemoryFile(FileDescriptor fd, int length) throws IOException {
        try {
            this.mSharedMemory = SharedMemory.create(fd, length);
            this.mMapping = this.mSharedMemory.mapReadWrite();
        } catch (ErrnoException ex) {
            ex.rethrowAsIOException();
        }
    }

    public MemoryFile(FileDescriptor fd, int length, boolean vivo) throws IOException {
        this.mUsedFD = vivo;
        if (this.mUsedFD) {
            this.mLength = length;
            if (fd != null) {
                this.mFD = fd;
                this.mAddress = native_mmap(this.mFD, length, 3);
            } else {
                this.mAddress = 0;
            }
            return;
        }
        try {
            this.mSharedMemory = SharedMemory.create(fd, length);
            this.mMapping = this.mSharedMemory.mapReadWrite();
        } catch (ErrnoException ex) {
            ex.rethrowAsIOException();
        }
    }

    private boolean isDeactivated() {
        return this.mAddress == 0;
    }

    private boolean isClosed() {
        if (this.mFD == null) {
            return true;
        }
        return this.mFD.valid() ^ 1;
    }

    protected void finalize() {
        if (this.mUsedFD && !isClosed()) {
            Log.e(TAG, "MemoryFile.finalize() called while ashmem still open");
            close();
        }
    }

    public void close() {
        deactivate();
        if (this.mUsedFD) {
            if (!isClosed()) {
                native_close(this.mFD);
            }
            return;
        }
        this.mSharedMemory.close();
    }

    void deactivate() {
        if (this.mUsedFD) {
            if (!isDeactivated()) {
                try {
                    native_munmap(this.mAddress, this.mLength);
                    this.mAddress = 0;
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString());
                }
            }
            return;
        }
        if (this.mMapping != null) {
            SharedMemory.unmap(this.mMapping);
            this.mMapping = null;
        }
    }

    private void checkActive() throws IOException {
        if (this.mMapping == null) {
            throw new IOException("MemoryFile has been deactivated");
        }
    }

    private void beginAccess() throws IOException {
        checkActive();
        if (this.mAllowPurging && native_pin(this.mSharedMemory.getFileDescriptor(), true)) {
            throw new IOException("MemoryFile has been purged");
        }
    }

    private void endAccess() throws IOException {
        if (this.mAllowPurging) {
            native_pin(this.mSharedMemory.getFileDescriptor(), false);
        }
    }

    public int length() {
        if (this.mUsedFD) {
            return this.mLength;
        }
        return this.mSharedMemory.getSize();
    }

    @Deprecated
    public boolean isPurgingAllowed() {
        return this.mAllowPurging;
    }

    @Deprecated
    public synchronized boolean allowPurging(boolean allowPurging) throws IOException {
        boolean oldValue;
        oldValue = this.mAllowPurging;
        if (oldValue != allowPurging) {
            if (this.mUsedFD) {
                native_pin(this.mFD, allowPurging ^ 1);
            } else {
                native_pin(this.mSharedMemory.getFileDescriptor(), allowPurging ^ 1);
            }
            this.mAllowPurging = allowPurging;
        }
        return oldValue;
    }

    public InputStream getInputStream() {
        return new MemoryInputStream(this, null);
    }

    public OutputStream getOutputStream() {
        return new MemoryOutputStream(this, null);
    }

    public int readBytes(byte[] buffer, int srcOffset, int destOffset, int count) throws IOException {
        if (!this.mUsedFD) {
            beginAccess();
            try {
                this.mMapping.position(srcOffset);
                this.mMapping.get(buffer, destOffset, count);
                return count;
            } finally {
                endAccess();
            }
        } else if (isDeactivated()) {
            throw new IOException("Can't read from deactivated memory file.");
        } else if (destOffset < 0 || destOffset > buffer.length || count < 0 || count > buffer.length - destOffset || srcOffset < 0 || srcOffset > this.mLength || count > this.mLength - srcOffset) {
            throw new IndexOutOfBoundsException();
        } else {
            return native_read(this.mFD, this.mAddress, buffer, srcOffset, destOffset, count, this.mAllowPurging);
        }
    }

    public void writeBytes(byte[] buffer, int srcOffset, int destOffset, int count) throws IOException {
        if (!this.mUsedFD) {
            beginAccess();
            try {
                this.mMapping.position(destOffset);
                this.mMapping.put(buffer, srcOffset, count);
            } finally {
                endAccess();
            }
        } else if (isDeactivated()) {
            throw new IOException("Can't write to deactivated memory file.");
        } else if (srcOffset < 0 || srcOffset > buffer.length || count < 0 || count > buffer.length - srcOffset || destOffset < 0 || destOffset > this.mLength || count > this.mLength - destOffset) {
            throw new IndexOutOfBoundsException();
        } else {
            native_write(this.mFD, this.mAddress, buffer, srcOffset, destOffset, count, this.mAllowPurging);
        }
    }

    public FileDescriptor getFileDescriptor() throws IOException {
        if (this.mUsedFD) {
            return this.mFD;
        }
        return this.mSharedMemory.getFileDescriptor();
    }

    public static int getSize(FileDescriptor fd) throws IOException {
        return native_get_size(fd);
    }
}
