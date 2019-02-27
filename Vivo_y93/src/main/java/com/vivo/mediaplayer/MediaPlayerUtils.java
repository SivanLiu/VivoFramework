package com.vivo.mediaplayer;

import android.media.MediaFile;
import android.media.MediaFile.MediaFileType;
import android.os.SystemProperties;

public class MediaPlayerUtils {
    public static boolean DEBUG;
    public static String DR_MODE = SystemProperties.get("media.vlcplayer.drmode");
    public static String MODEL = SystemProperties.get("ro.product.model.bbk", "UNKNOWN");
    public static String PLATFORM = SystemProperties.get("ro.vivo.product.platform", "UNKNOWN");
    public static String PLAYER_MODE = SystemProperties.get("media.vlcplayer.mode");
    public static String SOLUTION = SystemProperties.get("ro.vivo.product.solution", "UNKNOWN");
    private static final int[] VLC_FILE_TYPES = new int[0];

    static {
        boolean z;
        if (SystemProperties.get("ro.build.type").equals("eng")) {
            z = true;
        } else {
            z = SystemProperties.get("media.vlcplayer.debug").equals("yes");
        }
        DEBUG = z;
    }

    private MediaPlayerUtils() {
    }

    public static boolean useVlcPlayer(String path) {
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = 0;
        if (mediaFileType != null) {
            fileType = mediaFileType.fileType;
        }
        return useVlcPlayer(fileType);
    }

    public static boolean useVlcPlayer(int fileType) {
        for (int type : VLC_FILE_TYPES) {
            if (type == fileType) {
                return true;
            }
        }
        return false;
    }

    static String getFileExtension(String file) {
        int index = file.lastIndexOf(".");
        if (index == -1 || file.length() <= index + 1) {
            return "";
        }
        return file.substring(index);
    }
}
