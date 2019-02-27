package com.vivo.common.utils;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.media.MediaFile;
import android.media.MediaFile.MediaFileType;
import android.webkit.MimeTypeMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoFileMimeType {
    public static String getMimeType(String path) {
        if (path == null || path.length() <= 0) {
            return null;
        }
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0 || lastDot >= path.length() - 1) {
            return null;
        }
        MediaFileType mft = MediaFile.getFileType(path);
        String ret = null;
        if (mft != null) {
            ret = mft.mimeType;
        }
        return ret;
    }

    public static String getGeneriMimeTypeFromExtension(String extension) {
        int intType = MediaFile.getFileTypeForMimeType(getMimeType("a." + extension));
        if (MediaFile.isImageFileType(intType)) {
            return "image/*";
        }
        if (MediaFile.isAudioFileType(intType)) {
            return "audio/*";
        }
        if (MediaFile.isVideoFileType(intType)) {
            return "video/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (type == null || type.length() <= 0) {
            return type;
        }
        String preType = type.substring(0, type.indexOf("/"));
        if (preType == null || preType.length() <= 0) {
            return type;
        }
        if (preType.equals("video") || preType.equals("audio") || preType.equals("image")) {
            return null;
        }
        return type;
    }
}
