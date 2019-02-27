package com.vivo.mediaplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.Log;

public class MediaPlayerFactory {
    private static final String[] PROJECTION = new String[]{"_data"};
    private static final String TAG = "VMediaPlayer/Factory";

    public static class PlayerType {
        public static final int MARS_PLAYER = 2;
        public static final int NAIVE_PLAYER = 3;
        public static final int VLC_PLAYER = 1;
    }

    private MediaPlayerFactory() {
    }

    public static IMediaPlayer create(Context context, Uri uri) {
        if ("file".equals(uri.getScheme())) {
            return create(uri.getPath());
        }
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, PROJECTION, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    IMediaPlayer create = create(cursor.getString(0));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return create;
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return new NaiveMediaPlayer();
    }

    public static IMediaPlayer create(String path) {
        Log.d(TAG, "created player for video " + path);
        int mode = "".equals(MediaPlayerUtils.PLAYER_MODE) ? 0 : Integer.parseInt(MediaPlayerUtils.PLAYER_MODE);
        if (VERSION.SDK_INT >= 21 || mode != 1) {
            return new NaiveMediaPlayer();
        }
        Log.i(TAG, "Force creating VMediaPlayer");
        return new VlcMediaPlayer();
    }

    public static IMediaPlayer createByType(int playerType) {
        int mode = "".equals(MediaPlayerUtils.PLAYER_MODE) ? 0 : Integer.parseInt(MediaPlayerUtils.PLAYER_MODE);
        if (VERSION.SDK_INT < 21 && mode == 1) {
            Log.i(TAG, "Force creating VMediaPlayer");
            return new VlcMediaPlayer();
        } else if (mode == 3) {
            Log.i(TAG, "Force creating NaiveMediaPlayer");
            return new NaiveMediaPlayer();
        } else if (mode == 2) {
            Log.i(TAG, "Force creating MarsMediaPlayer");
            return new MarsMediaPlayer((NaiveMediaPlayer) new NaiveMediaPlayer());
        } else {
            switch (playerType) {
                case 1:
                    if (VERSION.SDK_INT < 21) {
                        return new VlcMediaPlayer();
                    }
                    throw new IllegalArgumentException("Does not supoprt VLC in Android 5.0 or later");
                case 3:
                    return new NaiveMediaPlayer();
                default:
                    Log.w(TAG, "wrong player type " + playerType + ", return NaiveMediaPlayer");
                    return new NaiveMediaPlayer();
            }
        }
    }
}
