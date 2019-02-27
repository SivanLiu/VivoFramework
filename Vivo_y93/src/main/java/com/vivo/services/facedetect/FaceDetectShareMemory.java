package com.vivo.services.facedetect;

import android.os.MemoryFile;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;

public class FaceDetectShareMemory {
    private static final String TAG = FaceDetectShareMemory.class.getSimpleName();
    private MemoryFile shareMemory;

    public FaceDetectShareMemory(String name, int length) throws IOException {
        this.shareMemory = new MemoryFile(name, length);
    }

    public FaceDetectShareMemory(FileDescriptor fd, int length) throws IOException {
        try {
            this.shareMemory = (MemoryFile) MemoryFile.class.getConstructor(new Class[]{FileDescriptor.class, Integer.TYPE}).newInstance(new Object[]{fd, Integer.valueOf(length)});
        } catch (Exception e) {
            Log.d(TAG, "new share memory error ", e);
        }
    }

    public FaceDetectShareMemory(FileDescriptor fd, int length, boolean oldfd) throws IOException {
        try {
            this.shareMemory = (MemoryFile) MemoryFile.class.getConstructor(new Class[]{FileDescriptor.class, Integer.TYPE, Boolean.TYPE}).newInstance(new Object[]{fd, Integer.valueOf(length), Boolean.valueOf(oldfd)});
        } catch (Exception e) {
            Log.d(TAG, "new share memory error ", e);
        }
    }

    public boolean writeData(byte[] data) {
        try {
            if (!(this.shareMemory == null || data == null || data.length <= 0)) {
                this.shareMemory.writeBytes(data, 0, 0, data.length);
                return true;
            }
        } catch (Exception e) {
            Log.i(TAG, "write share memory exception", e);
        }
        return false;
    }

    public byte[] readData(int length) {
        try {
            if (this.shareMemory != null) {
                byte[] buffer = new byte[length];
                this.shareMemory.readBytes(buffer, 0, 0, length);
                return buffer;
            }
        } catch (Exception e) {
            Log.i(TAG, "read share memory exception", e);
        }
        return null;
    }

    public FileDescriptor getFileDescriptor() {
        try {
            if (this.shareMemory != null) {
                return this.shareMemory.getFileDescriptor();
            }
            return null;
        } catch (Exception e) {
            Log.i(TAG, "read share memory exception", e);
            return null;
        }
    }

    public int getSize() {
        try {
            if (this.shareMemory != null) {
                return this.shareMemory.length();
            }
            return 0;
        } catch (Exception e) {
            Log.i(TAG, "read share memory exception", e);
            return 0;
        }
    }

    public void clearData() {
        if (this.shareMemory != null) {
            int size = getSize();
            try {
                this.shareMemory.writeBytes(new byte[size], 0, 0, size);
            } catch (Exception e) {
                Log.e(TAG, "clear data error", e);
            }
        }
    }

    public void releaseShareMemory() {
        if (this.shareMemory != null) {
            this.shareMemory.close();
        }
    }
}
