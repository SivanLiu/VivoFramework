package com.vivo.common.utils;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaFile;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.webkit.MimeTypeMap;
import com.vivo.common.autobrightness.StateInfo;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.MusicStore.BucketColumns;
import com.vivo.common.utils.HanziToPinyin.Token;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channel;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FileUtils {
    private static long[] CRCTable = new long[256];
    public static final char[] FILENAME_ILLCHAR = new char[]{'\\', '/', ':', '*', '?', '\"', '<', '>', '|', 10};
    public static final int FILE_CREATE_ERROR_DEST_EXIST = 1;
    public static final int FILE_CREATE_ERROR_DEST_STRING_NULL = 2;
    public static final int FILE_CREATE_ERROR_UNKNOWN = 3;
    public static final int FILE_CREATE_MSG_SUCEEDED = 0;
    public static final int FILE_RENAME_ERROR_DEST_EXIST = 3;
    public static final int FILE_RENAME_ERROR_DEST_STRING_NULL = 4;
    public static final int FILE_RENAME_ERROR_NAME_NOT_CHANGE = 2;
    public static final int FILE_RENAME_ERROR_SRC_NULL = 1;
    public static final int FILE_RENAME_ERROR_UNKNOWN = 5;
    public static final int FILE_RENAME_MSG_SUCEEDED = 0;
    private static final long INITIALCRC = -1;
    private static final String LOGTAG = "FileUtils";
    public static final long LOW_STORAGE_THRESHOLD = 1048576;
    private static final long POLY64REV = -7661587058870466123L;
    private static final String THUMBNAIL_FOLDER = (Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase() + "/.thumbnail/");
    public static final long VERY_LOW_STORAGE_THRESHOLD = 65536;
    private static boolean init = false;

    public static String getExtensionWithDot(String name) {
        if (name == null || name.length() <= 0) {
            return null;
        }
        name = name.toLowerCase();
        int dot = name.lastIndexOf(".");
        String ret = null;
        if (dot >= 0) {
            ret = name.substring(dot);
            String tmp = name.substring(0, dot);
            if (tmp != null && tmp.length() > 0) {
                dot = tmp.lastIndexOf(".");
                if (dot >= 0) {
                    tmp = tmp.substring(dot);
                    if (tmp != null && tmp.length() > 0 && tmp.equalsIgnoreCase(".tar")) {
                        ret = name.substring(dot);
                    }
                }
            }
        }
        return ret;
    }

    public static String getExtensionWithoutDot(String name) {
        if (name == null || name.length() <= 0) {
            return null;
        }
        name = name.toLowerCase();
        int dot = name.lastIndexOf(".");
        String ret = null;
        if (dot >= 0) {
            ret = name.substring(dot);
            String tmp = name.substring(0, dot);
            if (tmp != null && tmp.length() > 0) {
                dot = tmp.lastIndexOf(".");
                if (dot >= 0) {
                    tmp = tmp.substring(dot);
                    if (tmp != null && tmp.length() > 0 && tmp.equalsIgnoreCase(".tar")) {
                        ret = name.substring(dot);
                    }
                }
            }
        }
        if (ret != null && ret.length() > 1) {
            ret = ret.substring(1);
        }
        return ret;
    }

    public static String getFileNameWithoutExtension(String name) {
        if (name == null || name.length() <= 0) {
            return null;
        }
        int dot = name.lastIndexOf(".");
        String ret = name;
        if (dot >= 0) {
            String tmp = name.substring(0, dot);
            if (tmp != null && tmp.length() > 0) {
                ret = tmp;
                dot = tmp.lastIndexOf(".");
                if (dot >= 0) {
                    tmp = tmp.substring(dot);
                    if (tmp != null && tmp.length() > 0 && tmp.equalsIgnoreCase(".tar")) {
                        ret = name.substring(0, dot);
                    }
                }
            }
        }
        return ret;
    }

    public static String getNewFileNameWithoutExtension(String currDir, String name, String extensionWithDot) {
        if (currDir == null || currDir.length() <= 0 || name == null || name.length() <= 0) {
            return null;
        }
        if (extensionWithDot == null) {
            extensionWithDot = Events.DEFAULT_SORT_ORDER;
        }
        File f = new File(currDir + "/" + name + extensionWithDot);
        if (!f.exists()) {
            return name;
        }
        int i = 0;
        String tmpName = null;
        while (f.exists()) {
            i++;
            tmpName = name + i;
            f = new File(currDir + "/" + tmpName + extensionWithDot);
        }
        return tmpName;
    }

    public static String getCopyFileNameWithExtension(String currDir, String name, String extensionWithDot) {
        if (currDir == null || currDir.length() <= 0 || name == null || name.length() <= 0) {
            return null;
        }
        if (extensionWithDot == null) {
            extensionWithDot = Events.DEFAULT_SORT_ORDER;
        }
        String tmpName = name + extensionWithDot;
        File f = new File(currDir + "/" + tmpName);
        if (!f.exists()) {
            return tmpName;
        }
        int i = 0;
        while (f.exists()) {
            i++;
            tmpName = name + "(" + i + ")" + extensionWithDot;
            f = new File(currDir + "/" + tmpName);
        }
        return tmpName;
    }

    public static final long Crc64Long(String in) {
        if (in == null || in.length() == 0) {
            return 0;
        }
        long crc = INITIALCRC;
        if (!init) {
            for (int i = 0; i < 256; i++) {
                long part = (long) i;
                for (int j = 0; j < 8; j++) {
                    if ((((int) part) & 1) != 0) {
                        part = (part >> 1) ^ POLY64REV;
                    } else {
                        part >>= 1;
                    }
                }
                CRCTable[i] = part;
            }
            init = true;
        }
        int length = in.length();
        for (int k = 0; k < length; k++) {
            crc = CRCTable[(((int) crc) ^ in.charAt(k)) & 255] ^ (crc >> 8);
        }
        return crc;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap getThumbnail(Context context, long origId, int kind, boolean isImage, String origPath) {
        Throwable e;
        Throwable th;
        if (context == null || origId < 0 || origPath == null || origPath.length() <= 0) {
            return null;
        }
        String columnData;
        String columnOrigId;
        String columnKind;
        String columnWidth;
        String columnHeight;
        Uri uri;
        File file = new File(THUMBNAIL_FOLDER);
        if (file == null || (file.exists() ^ 1) != 0) {
            try {
                file.mkdirs();
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
        }
        File file2 = null;
        String path = null;
        Bitmap bitmap = null;
        Cursor cursor = null;
        long id = 0;
        String columnId = "_id";
        if (isImage) {
            columnData = BucketColumns.BUCKET_PATH;
            columnOrigId = "image_id";
            columnKind = "kind";
            columnWidth = "width";
            columnHeight = "height";
            uri = Thumbnails.EXTERNAL_CONTENT_URI;
        } else {
            columnData = BucketColumns.BUCKET_PATH;
            columnOrigId = "video_id";
            columnKind = "kind";
            columnWidth = "width";
            columnHeight = "height";
            uri = Video.Thumbnails.EXTERNAL_CONTENT_URI;
        }
        ContentResolver cr = context.getContentResolver();
        try {
            cursor = cr.query(uri, new String[]{columnId, columnData}, columnOrigId + "=" + origId + " and " + columnKind + "=" + kind, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(0);
                path = cursor.getString(1);
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th2) {
                }
            }
        } catch (Throwable th3) {
        }
        if (path != null && path.length() > 0) {
            file = new File(path);
        }
        boolean recreateThumbnailFile = false;
        if (file2 == null || !file2.exists()) {
            recreateThumbnailFile = true;
        } else {
            InputStream in = null;
            try {
                InputStream fileInputStream = new FileInputStream(file2);
                try {
                    bitmap = BitmapFactory.decodeStream(fileInputStream);
                    closeQuietly(fileInputStream);
                    in = fileInputStream;
                } catch (Throwable th4) {
                    th = th4;
                    in = fileInputStream;
                    closeQuietly(in);
                    throw th;
                }
            } catch (Throwable th5) {
                e2 = th5;
                e2.printStackTrace();
                bitmap = null;
                closeQuietly(in);
                if (bitmap == null) {
                }
                if (recreateThumbnailFile) {
                }
                return bitmap;
            }
            if (bitmap == null) {
                recreateThumbnailFile = true;
            }
        }
        if (recreateThumbnailFile) {
            if (file2 != null) {
                try {
                    file2.delete();
                } catch (Throwable th6) {
                }
            }
            OutputStream out = null;
            if (isImage) {
                try {
                    uri = Thumbnails.EXTERNAL_CONTENT_URI;
                } catch (Throwable th7) {
                    e2 = th7;
                }
            } else {
                uri = Video.Thumbnails.EXTERNAL_CONTENT_URI;
            }
            if (bitmap != null) {
                OutputStream fileOutputStream;
                boolean updateAction = false;
                if (path != null && path.length() > 0) {
                    updateAction = true;
                }
                path = THUMBNAIL_FOLDER + Long.toString(Crc64Long(origPath));
                file = new File(path);
                try {
                    fileOutputStream = new FileOutputStream(file);
                } catch (Throwable th8) {
                    th = th8;
                    file2 = file;
                    closeQuietly(out);
                    throw th;
                }
                try {
                    int size;
                    bitmap.compress(CompressFormat.PNG, 80, fileOutputStream);
                    ContentValues values = new ContentValues();
                    if (kind == 3) {
                        size = 96;
                    } else {
                        size = 320;
                    }
                    if (updateAction) {
                        values.put(columnData, path);
                        values.put(columnWidth, Integer.valueOf(size));
                        values.put(columnHeight, Integer.valueOf(size));
                    } else {
                        values.put(columnOrigId, Long.valueOf(origId));
                        values.put(columnData, path);
                        values.put(columnKind, Integer.valueOf(kind));
                        values.put(columnWidth, Integer.valueOf(size));
                        values.put(columnHeight, Integer.valueOf(size));
                    }
                    if (updateAction) {
                        cr.update(uri, values, columnId + "=" + id, null);
                        out = fileOutputStream;
                        file2 = file;
                    } else {
                        cr.insert(uri, values);
                        out = fileOutputStream;
                        file2 = file;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    out = fileOutputStream;
                    file2 = file;
                    closeQuietly(out);
                    throw th;
                }
            }
            closeQuietly(out);
        }
        return bitmap;
    }

    public static void deleteVideoThumbnail(Context context, File f) {
        long origId = getMediaFileIdInDatabase(context, f);
        if (origId >= 0) {
            Cursor cursor = null;
            Uri uri = Video.Thumbnails.EXTERNAL_CONTENT_URI;
            ContentResolver cr = context.getContentResolver();
            try {
                cursor = cr.query(uri, new String[]{"_id", BucketColumns.BUCKET_PATH}, "video_id=" + origId, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(0);
                        String p = cursor.getString(1);
                        if (p != null && p.length() > 0) {
                            try {
                                new File(p).delete();
                            } catch (Throwable th) {
                            }
                        }
                        cr.delete(uri, "_id=" + id, null);
                    }
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th2) {
                    }
                }
            } catch (Throwable th3) {
            }
        }
    }

    public static int[] getImageDimension(File file) {
        if (file == null || (file.exists() ^ 1) != 0 || file.isDirectory()) {
            return null;
        }
        int[] ret = new int[2];
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.outWidth = 0;
        options.outHeight = 0;
        options.inSampleSize = 1;
        try {
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            ret[0] = options.outWidth;
            ret[1] = options.outHeight;
            return ret;
        } catch (Throwable th) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0046 A:{SYNTHETIC, Splitter: B:24:0x0046} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004f A:{SYNTHETIC, Splitter: B:29:0x004f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int[] getVideoDimension(File file) {
        Throwable th;
        if (file == null || (file.exists() ^ 1) != 0 || file.isDirectory()) {
            return null;
        }
        int[] array = new int[2];
        int[] ret = array;
        MediaMetadataRetriever retriever = null;
        try {
            MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
            try {
                retriever2.setDataSource(file.getAbsolutePath());
                Bitmap bitmap = retriever2.getFrameAtTime();
                if (bitmap != null) {
                    array[0] = bitmap.getWidth();
                    array[1] = bitmap.getHeight();
                } else {
                    ret = null;
                }
                if (retriever2 != null) {
                    try {
                        retriever2.release();
                    } catch (Throwable th2) {
                    }
                }
                retriever = retriever2;
            } catch (Throwable th3) {
                th = th3;
                retriever = retriever2;
                if (retriever != null) {
                    try {
                        retriever.release();
                    } catch (Throwable th4) {
                    }
                }
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            if (retriever != null) {
            }
            throw th;
        }
        return ret;
    }

    public static long getMediaFileIdInDatabase(Context context, File file) {
        if (context == null || file == null || (file.exists() ^ 1) != 0 || file.isDirectory()) {
            return INITIALCRC;
        }
        Uri uri;
        if (isImageFile(file)) {
            uri = Media.EXTERNAL_CONTENT_URI;
        } else if (isAudioFile(file)) {
            uri = Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (!isVideoFile(file)) {
            return INITIALCRC;
        } else {
            uri = Video.Media.EXTERNAL_CONTENT_URI;
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        String[] cols = new String[]{"_id"};
        long ret = INITIALCRC;
        try {
            cursor = resolver.query(uri, cols, "_data=?", new String[]{file.getAbsolutePath()}, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    ret = cursor.getLong(0);
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return ret;
    }

    public static Uri getFileUri(Context context, File file) {
        if (context == null || file == null) {
            return null;
        }
        Uri uri = Uri.fromFile(file);
        if (file.exists() && file.isFile()) {
            long id;
            if (isImageFile(file)) {
                id = getMediaFileIdInDatabase(context, file);
                if (id != INITIALCRC) {
                    uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
                } else {
                    scanMediaFile(context, file);
                }
            } else if (isAudioFile(file)) {
                id = getMediaFileIdInDatabase(context, file);
                if (id != INITIALCRC) {
                    uri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id);
                } else {
                    scanMediaFile(context, file);
                }
            } else if (isVideoFile(file)) {
                id = getMediaFileIdInDatabase(context, file);
                if (id != INITIALCRC) {
                    uri = ContentUris.withAppendedId(Video.Media.EXTERNAL_CONTENT_URI, id);
                } else {
                    scanMediaFile(context, file);
                }
            }
        }
        return uri;
    }

    public static Uri getFileUriWithFileScheme(File file) {
        if (file == null) {
            return null;
        }
        return Uri.fromFile(file);
    }

    public static void scanAllMediaFile(Context context) {
        scanMediaFile(context, new File(Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase()));
    }

    public static void scanMediaFile(Context context, File file) {
        if (context != null && file != null) {
            Uri uri = getFileUriWithFileScheme(file);
            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            intent.setData(uri);
            try {
                context.sendBroadcast(intent);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static File createFile(String path, int[] retCode) {
        if (retCode == null || retCode.length <= 0) {
            return null;
        }
        if (path == null || path.length() <= 0) {
            retCode[0] = 2;
            return null;
        }
        File f = new File(path);
        if (f.exists()) {
            retCode[0] = 1;
            return null;
        }
        try {
            f.createNewFile();
            retCode[0] = 0;
            return f;
        } catch (Throwable th) {
            retCode[0] = 3;
            return null;
        }
    }

    public static File createDir(String path, int[] retCode) {
        if (retCode == null || retCode.length <= 0) {
            return null;
        }
        if (path == null || path.length() <= 0) {
            retCode[0] = 2;
            return null;
        }
        File f = new File(path);
        if (f.exists()) {
            retCode[0] = 1;
            return null;
        }
        try {
            f.mkdir();
            retCode[0] = 0;
            return f;
        } catch (Throwable th) {
            retCode[0] = 3;
            return null;
        }
    }

    public static int renameFile(File srcFile, String dest, Context context) {
        if (srcFile == null || (srcFile.exists() ^ 1) != 0) {
            return 1;
        }
        if (dest == null || dest.length() <= 0) {
            return 4;
        }
        boolean onlyCaseChanged = false;
        String srcPath = srcFile.getAbsolutePath();
        if (srcPath.equals(dest)) {
            return 2;
        }
        if (srcPath.equalsIgnoreCase(dest)) {
            onlyCaseChanged = true;
        }
        File destFile = new File(dest);
        if (!onlyCaseChanged && destFile.exists()) {
            return 3;
        }
        boolean result;
        if (onlyCaseChanged) {
            File tmpFile = new File(srcPath + Long.toString(Crc64Long(srcPath)));
            srcFile.renameTo(tmpFile);
            result = tmpFile.renameTo(destFile);
        } else {
            try {
                result = srcFile.renameTo(destFile);
            } catch (Throwable th) {
                result = false;
            }
        }
        Uri uri = null;
        if (isImageFile(destFile)) {
            uri = Media.EXTERNAL_CONTENT_URI;
        } else if (isAudioFile(destFile)) {
            uri = Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideoFile(destFile)) {
            uri = Video.Media.EXTERNAL_CONTENT_URI;
        }
        if (uri != null) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put("_display_name", destFile.getName());
            values.put(BucketColumns.BUCKET_PATH, destFile.getAbsolutePath());
            resolver.update(uri, values, "_data=?", new String[]{srcPath});
            scanMediaFile(context, destFile);
        } else if (destFile.isDirectory()) {
            scanAllMediaFile(context);
        }
        if (result) {
            return 0;
        }
        return 5;
    }

    public static boolean isMediaScannerScanning(ContentResolver cr) {
        if (cr == null) {
            return false;
        }
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = cr.query(MediaStore.getMediaScannerUri(), new String[]{"volume"}, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    result = "external".equals(cursor.getString(0));
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public static boolean isMediaFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (isImageFile(extension) || isAudioFile(extension) || isVideoFile(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isVideoOrImage(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (isVideoFile(extension) || isImageFile(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isVideoFile(File f) {
        if (f == null) {
            return false;
        }
        return isVideoFile(getExtensionWithoutDot(f.getName()));
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVideoFile(String extension) {
        if (extension == null || extension.length() <= 0 || !MediaFile.isVideoFileType(MediaFile.getFileTypeForMimeType(VivoFileMimeType.getMimeType("a." + extension)))) {
            return false;
        }
        return true;
    }

    public static boolean isAudioFile(File f) {
        if (f == null) {
            return false;
        }
        return isAudioFile(getExtensionWithoutDot(f.getName()));
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isAudioFile(String extension) {
        if (extension == null || extension.length() <= 0 || !MediaFile.isAudioFileType(MediaFile.getFileTypeForMimeType(VivoFileMimeType.getMimeType("a." + extension)))) {
            return false;
        }
        return true;
    }

    public static boolean isImageFile(File f) {
        if (f == null) {
            return false;
        }
        return isImageFile(getExtensionWithoutDot(f.getName()));
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isImageFile(String extension) {
        if (extension == null || extension.length() <= 0 || !MediaFile.isImageFileType(MediaFile.getFileTypeForMimeType(VivoFileMimeType.getMimeType("a." + extension)))) {
            return false;
        }
        return true;
    }

    public static boolean isTxtFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isTxtFile(extension);
    }

    public static boolean isTxtFile(String extension) {
        if ("txt".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isApkFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isApkFile(extension);
    }

    public static boolean isApkFile(String extension) {
        if ("apk".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isPdfFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isPdfFile(extension);
    }

    public static boolean isPdfFile(String extension) {
        if ("pdf".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isDocFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isDocFile(extension);
    }

    public static boolean isDocFile(String extension) {
        if ("doc".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isXlsFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isXlsFile(extension);
    }

    public static boolean isXlsFile(String extension) {
        if ("xls".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isPptFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isPptFile(extension);
    }

    public static boolean isPptFile(String extension) {
        if ("ppt".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isExeFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isExeFile(extension);
    }

    public static boolean isExeFile(String extension) {
        if ("exe".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isVcfFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isVcfFile(extension);
    }

    public static boolean isVcfFile(String extension) {
        if ("vcf".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isCsvFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isCsvFile(extension);
    }

    public static boolean isCsvFile(String extension) {
        if ("csv".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean isHtmlFile(File f) {
        if (f == null) {
            return false;
        }
        String extension = getExtensionWithoutDot(f.getName());
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isHtmlFile(extension);
    }

    public static boolean isHtmlFile(String extension) {
        if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    public static boolean canUncompress(String srcFileName) {
        if (srcFileName == null || srcFileName.length() <= 0) {
            return false;
        }
        String extension = getExtensionWithoutDot(srcFileName);
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        return isCompressedFile(extension);
    }

    public static boolean isCompressedFile(String extension) {
        if (extension == null || extension.length() <= 0) {
            return false;
        }
        if (extension.equalsIgnoreCase("ar") || extension.equalsIgnoreCase("cpio") || extension.equalsIgnoreCase("jar") || extension.equalsIgnoreCase("tar") || extension.equalsIgnoreCase("zip") || extension.equalsIgnoreCase("tar.gz") || extension.equalsIgnoreCase("tgz") || extension.equalsIgnoreCase("gz") || extension.equalsIgnoreCase("tar.bz2") || extension.equalsIgnoreCase("tbz2") || extension.equalsIgnoreCase("bz2") || extension.equalsIgnoreCase("rar")) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean canCompress(File file, String extensionWithDot) {
        if (file == null || (file.exists() ^ 1) != 0 || extensionWithDot == null || extensionWithDot.length() <= 0) {
            return false;
        }
        if (extensionWithDot.equalsIgnoreCase(".ar") && file.isDirectory()) {
            return false;
        }
        return true;
    }

    public static boolean hasIllChar(String filename) {
        if (filename == null || filename.length() <= 0) {
            return true;
        }
        for (char indexOf : FILENAME_ILLCHAR) {
            if (filename.indexOf(indexOf) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static String removeIllChar(String filename) {
        if (filename == null || filename.length() <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String illChars = new String(FILENAME_ILLCHAR);
        for (int i = 0; i < filename.length(); i++) {
            char c = filename.charAt(i);
            if (illChars.indexOf(c) < 0) {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    public static boolean isStringAllSpaces(String s) {
        if (s == null || s.length() <= 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    public static String[] splitString(String s) {
        if (s == null || s.length() <= 0 || s.indexOf(47) <= 0) {
            return null;
        }
        String[] ret;
        try {
            ret = s.split("/");
        } catch (Throwable th) {
            ret = null;
        }
        return ret;
    }

    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(92) <= 0) {
            return path;
        }
        return path.replace('\\', '/');
    }

    public static boolean getStorageSize(long[] item) {
        if (item == null || item.length < 2) {
            return false;
        }
        boolean ret = true;
        String status = Environment.getExternalStorageState();
        if (status.equals("mounted_ro")) {
            status = "mounted";
        }
        if (status.equals("mounted")) {
            try {
                StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                long blockSize = (long) stat.getBlockSize();
                long availableBlocks = (long) stat.getAvailableBlocks();
                item[0] = ((long) stat.getBlockCount()) * blockSize;
                item[1] = availableBlocks * blockSize;
            } catch (Throwable th) {
                status = "removed";
                ret = false;
            }
        } else {
            ret = false;
        }
        return ret;
    }

    public static boolean getAvailableStorageSize(long[] item) {
        if (item == null || item.length < 1) {
            return false;
        }
        boolean ret = true;
        String status = Environment.getExternalStorageState();
        if (status.equals("mounted_ro")) {
            status = "mounted";
        }
        if (status.equals("mounted")) {
            try {
                StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                item[0] = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            } catch (Throwable th) {
                status = "removed";
                ret = false;
            }
        } else {
            ret = false;
        }
        return ret;
    }

    public static boolean lowStorage(boolean restricted) {
        long[] item = new long[1];
        return getAvailableStorageSize(item) && item[0] < (restricted ? VERY_LOW_STORAGE_THRESHOLD : LOW_STORAGE_THRESHOLD);
    }

    public static void closeQuietly(Reader input) {
        if (input != null) {
            try {
                input.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void closeQuietly(Channel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void closeQuietly(Writer output) {
        if (output != null) {
            try {
                output.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void closeQuietly(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void closeQuietly(OutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (Throwable th) {
            }
        }
    }

    public static String readFile(File file) {
        Throwable e;
        String ret;
        Throwable th;
        if (file == null || (file.exists() ^ 1) != 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        InputStream fis = null;
        Reader isr = null;
        Reader br = null;
        try {
            Reader isr2;
            Reader br2;
            InputStream fis2 = new FileInputStream(file);
            try {
                isr2 = new InputStreamReader(fis2, "UTF8");
                try {
                    br2 = new BufferedReader(isr2);
                } catch (Throwable th2) {
                    th = th2;
                    isr = isr2;
                    fis = fis2;
                    closeQuietly(fis);
                    closeQuietly(isr);
                    closeQuietly(br);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                closeQuietly(fis);
                closeQuietly(isr);
                closeQuietly(br);
                throw th;
            }
            try {
                ret = br2.readLine();
                while (ret != null && ret.length() > 0) {
                    sb.append(ret);
                    sb.append(10);
                    ret = br2.readLine();
                }
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == 10) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                ret = sb.toString();
                closeQuietly(fis2);
                closeQuietly(isr2);
                closeQuietly(br2);
                fis = fis2;
            } catch (Throwable th4) {
                th = th4;
                br = br2;
                isr = isr2;
                fis = fis2;
                closeQuietly(fis);
                closeQuietly(isr);
                closeQuietly(br);
                throw th;
            }
        } catch (Throwable th5) {
            e = th5;
            ret = null;
            e.printStackTrace();
            closeQuietly(fis);
            closeQuietly(isr);
            closeQuietly(br);
            return ret;
        }
        return ret;
    }

    public static boolean isFileCanOpen(String extWithoutDot, Context context) {
        if (extWithoutDot == null || extWithoutDot.length() <= 0 || context == null) {
            return false;
        }
        String filename = "a." + extWithoutDot;
        if (canUncompress(filename)) {
            return true;
        }
        String type = null;
        if (!(MediaFile.isImageFileType(0) || (MediaFile.isAudioFileType(0) ^ 1) == 0 || (MediaFile.isVideoFileType(0) ^ 1) == 0)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extWithoutDot);
            if (type != null && type.length() > 0) {
                String preType = type.substring(0, type.indexOf("/"));
                if (preType != null && preType.length() > 0 && (preType.equals("video") || preType.equals("audio") || preType.equals("image"))) {
                    return false;
                }
            }
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename)), type);
        return context.getPackageManager().resolveActivity(intent, 65536) != null;
    }

    public static boolean isUTF8(byte[] rawtext) {
        int goodbytes = 0;
        int asciibytes = 0;
        int rawtextlen = rawtext.length;
        if (rawtextlen > StateInfo.STATE_BIT_HALL) {
            rawtextlen = StateInfo.STATE_BIT_HALL;
        }
        int i = 0;
        while (i < rawtextlen) {
            if ((rawtext[i] & 127) == rawtext[i]) {
                asciibytes++;
            } else if ((byte) -64 <= rawtext[i] && rawtext[i] <= (byte) -33 && i + 1 < rawtextlen && Byte.MIN_VALUE <= rawtext[i + 1] && rawtext[i + 1] <= (byte) -65) {
                goodbytes += 2;
                i++;
            } else if ((byte) -32 <= rawtext[i] && rawtext[i] <= (byte) -17 && i + 2 < rawtextlen && Byte.MIN_VALUE <= rawtext[i + 1] && rawtext[i + 1] <= (byte) -65 && Byte.MIN_VALUE <= rawtext[i + 2] && rawtext[i + 2] <= (byte) -65) {
                goodbytes += 3;
                i += 2;
            }
            i++;
        }
        if (asciibytes == rawtextlen) {
            return false;
        }
        int score = (goodbytes * 100) / (rawtextlen - asciibytes);
        if (score > 98) {
            return true;
        }
        return score > 95 && goodbytes > 30;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0067  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0134  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Intent getFileIntentToOpen(File file, Context context, String mimeType) {
        if (file == null || (file.exists() ^ 1) != 0 || file.isDirectory() || context == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri data = Uri.fromFile(file);
        Cursor cursor = null;
        String[] cols;
        if (isImageFile(file)) {
            cols = new String[]{"_id", "bucket_id"};
            try {
                cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, cols, "_data=?", new String[]{file.getAbsolutePath()}, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        data = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            scanMediaFile(context, file);
            if (cursor != null) {
            }
        } else if (isAudioFile(file)) {
            String extension;
            cols = new String[]{"_id"};
            try {
                cursor = context.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, cols, "_data=?", new String[]{file.getAbsolutePath()}, null);
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        data = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                        if (cursor != null) {
                            cursor.close();
                        }
                        extension = getExtensionWithoutDot(file.getName());
                        if (extension == null && extension.equalsIgnoreCase("3gpp")) {
                            intent.setClassName("com.android.bbksoundrecorder", "com.android.bbksoundrecorder.ReclistActivity");
                        } else {
                            intent.setPackage("com.android.bbkmusic");
                        }
                        intent.addFlags(67108864);
                    }
                } catch (Throwable th2) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            scanMediaFile(context, file);
            if (cursor != null) {
            }
            extension = getExtensionWithoutDot(file.getName());
            if (extension == null) {
            }
            intent.setPackage("com.android.bbkmusic");
            intent.addFlags(67108864);
        } else if (isVideoFile(file)) {
            cols = new String[]{"_id"};
            try {
                cursor = context.getContentResolver().query(Video.Media.EXTERNAL_CONTENT_URI, cols, "_data=?", new String[]{file.getAbsolutePath()}, null);
            } catch (Throwable e22) {
                e22.printStackTrace();
            }
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        data = ContentUris.withAppendedId(Video.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } catch (Throwable th3) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            scanMediaFile(context, file);
            if (cursor != null) {
            }
        }
        intent.setDataAndType(data, mimeType);
        return intent;
    }

    private static int compareCharByGetPinyin(String s1, String s2) {
        ArrayList<Token> token1 = HanziToPinyin.getInstance().get(s1);
        ArrayList<Token> token2 = HanziToPinyin.getInstance().get(s2);
        if (token1.size() <= 0 && token2.size() > 0) {
            return -1;
        }
        if (token1.size() > 0 && token2.size() <= 0) {
            return 1;
        }
        if (token1.size() <= 0 && token2.size() <= 0) {
            return -1;
        }
        if (((Token) token1.get(0)).type == 2 && ((Token) token2.get(0)).type != 2) {
            return 1;
        }
        if (((Token) token1.get(0)).type != 2 && ((Token) token2.get(0)).type == 2) {
            return -1;
        }
        String target1;
        String target2;
        if (((Token) token1.get(0)).type == 2 || ((Token) token2.get(0)).type == 2) {
            target1 = ((Token) token1.get(0)).target.substring(0, 1);
            target2 = ((Token) token2.get(0)).target.substring(0, 1);
        } else {
            target1 = ((Token) token1.get(0)).target;
            target2 = ((Token) token2.get(0)).target;
        }
        return target1.compareTo(target2);
    }

    public static int compareStringByGetPinyin(String s1, String s2, boolean ignoreCase) {
        boolean s1Empty = false;
        if (s1 == null || s1.length() <= 0) {
            s1Empty = true;
        }
        boolean s2Empty = false;
        if (s2 == null || s2.length() <= 0) {
            s2Empty = true;
        }
        if (s1Empty && s2Empty) {
            return 0;
        }
        if (!s1Empty && s2Empty) {
            return -1;
        }
        if (s1Empty && (s2Empty ^ 1) != 0) {
            return 1;
        }
        int leng1 = s1.length();
        int leng2 = s2.length();
        int leng = leng1 < leng2 ? leng1 : leng2;
        char c1 = 0;
        char c2 = 0;
        int i = 0;
        while (i < leng) {
            if (ignoreCase) {
                c1 = Character.toLowerCase(s1.charAt(i));
                c2 = Character.toLowerCase(s2.charAt(i));
            } else {
                c1 = s1.charAt(i);
                c2 = s2.charAt(i);
            }
            if (c1 != c2) {
                break;
            }
            i++;
        }
        if (i >= leng) {
            return leng1 - leng2;
        }
        return compareCharByGetPinyin(Character.toString(c1), Character.toString(c2));
    }
}
