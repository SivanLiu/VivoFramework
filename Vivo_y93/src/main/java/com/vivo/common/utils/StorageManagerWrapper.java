package com.vivo.common.utils;

import android.annotation.SuppressLint;
import android.os.StatFs;
import java.lang.reflect.Method;

public class StorageManagerWrapper {
    private static final String TAG = "StorageManagerWrapper";
    private static StorageManagerWrapper sInstance;
    private boolean isSupportTF = false;
    private String mExternalStoragePath = "/storage/sdcard0/external_sd";
    private String mInternalStoragePath = "/storage/sdcard0";
    private String[] mPathList;
    private Object mTarget;
    private Method sMethodgetVolumePaths;
    private Method sMethodgetVolumeState;

    public enum StorageType {
        InternalStorage,
        ExternalStorage,
        UsbStorage
    }

    public static StorageManagerWrapper getInstance(Object o) {
        if (sInstance == null) {
            sInstance = new StorageManagerWrapper(o);
        }
        return sInstance;
    }

    private StorageManagerWrapper(Object o) {
        int i = 0;
        Class<? extends Object> c = o.getClass();
        try {
            this.mTarget = o;
            this.sMethodgetVolumePaths = c.getDeclaredMethod("getVolumePaths", new Class[0]);
            this.sMethodgetVolumeState = c.getDeclaredMethod("getVolumeState", new Class[]{String.class});
            this.mPathList = getVolumePaths();
            String[] strArr = this.mPathList;
            int length = strArr.length;
            while (i < length) {
                if (getStorageType(strArr[i]) == StorageType.ExternalStorage) {
                    this.isSupportTF = true;
                }
                i++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSupportTF() {
        return this.isSupportTF;
    }

    public String[] getVolumePaths() {
        try {
            return (String[]) this.sMethodgetVolumePaths.invoke(this.mTarget, new Object[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public String getVolumeState(String path) {
        try {
            return (String) this.sMethodgetVolumeState.invoke(this.mTarget, new Object[]{path});
        } catch (Exception e) {
            return "removed";
        }
    }

    @SuppressLint({"SdCardPath"})
    public StorageType getStorageType(String path) {
        if (path.contains("/external_sd") || path.contains("/sdcard1")) {
            return StorageType.ExternalStorage;
        }
        if (path.contains("/sdcard0") || path.contains("/sdcard")) {
            return StorageType.InternalStorage;
        }
        if (path.contains("/otg")) {
            return StorageType.UsbStorage;
        }
        return StorageType.InternalStorage;
    }

    public String getInternalStoragePath() {
        initStoragePath();
        return this.mInternalStoragePath;
    }

    public String getInternalStorageState() {
        initStoragePath();
        return getVolumeState(this.mInternalStoragePath);
    }

    public String getExternalSdPath() {
        initStoragePath();
        return this.mExternalStoragePath;
    }

    public String getExternalSdState() {
        initStoragePath();
        return getVolumeState(this.mExternalStoragePath);
    }

    private void initStoragePath() {
        try {
            for (String path : getVolumePaths()) {
                if (getStorageType(path) == StorageType.ExternalStorage) {
                    this.mExternalStoragePath = path;
                }
                if (getStorageType(path) == StorageType.InternalStorage) {
                    this.mInternalStoragePath = path;
                }
            }
        } catch (Exception e) {
        }
    }

    public long getAvailSize(StatFs fs) {
        return (long) ((((float) (((long) fs.getBlockSize()) * ((long) fs.getAvailableBlocks()))) / 1024.0f) / 1024.0f);
    }
}
