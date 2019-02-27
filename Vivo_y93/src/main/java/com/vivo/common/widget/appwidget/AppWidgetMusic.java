package com.vivo.common.widget.appwidget;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Calendar.EventsColumns;
import com.vivo.common.provider.Weather.CurrentCity;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class AppWidgetMusic {
    private static final String ACTION_LYRICE = "com.android.music.update.photo";
    private static final String APPWIDGET_UPDATENEEDCLEAR = "APPWIDGET_UPDATENEEDCLEAR";
    private static final String FORCE_CLEAR_MUSIC = "android.intent.action.FORCE_STOP_PACKAGE.com.android.bbkmusic";
    private static final String KILL_SERVICE = "android.intent.action.KILL_BACKGROUND_SERVICE.com.android.bbkmusic";
    private static final String MEDIA_EJECT = "android.intent.action.MEDIA_EJECT";
    private static final String MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";
    private static final String META_CHANGED = "com.android.music.metachanged";
    private static final int MUSICCHANGE = 3;
    private static final int MUSICINFOHANDLER = 2;
    private static final int MUSICQUERY = 4;
    private static final int PHOTO_IMAGE = 1;
    private static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    private static final String QUEUE_CHANGED = "com.android.music.queuechanged";
    public static final String RINGCLIP_CHANGED = "com.android.ringclip.changed";
    private static final String TAG = "AppWidgetMusic";
    private static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    BroadcastReceiver MusicRecevier = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(AppWidgetMusic.TAG, "MusicRecevier-action=" + action);
            Bundle bundle;
            if (AppWidgetMusic.PLAYSTATE_CHANGED.equals(action) || AppWidgetMusic.META_CHANGED.equals(action) || AppWidgetMusic.QUEUE_CHANGED.equals(action)) {
                bundle = intent.getBundleExtra("updatePlaylist");
                if (bundle != null) {
                    Log.i(AppWidgetMusic.TAG, "bundle!=null");
                    AppWidgetMusic.this.mMusicInfo.AlbumImg = bundle.getLongArray("ALBUMLIST");
                    AppWidgetMusic.this.mMusicInfo.SongName = bundle.getStringArray("TRACKLIST");
                    AppWidgetMusic.this.mMusicInfo.mPlayList = bundle.getLongArray("PLAYLIST");
                    AppWidgetMusic.this.mMusicInfo.SingerName = bundle.getStringArray("ARTISTLIST");
                    AppWidgetMusic.this.mMusicInfo.CurPosition = bundle.getInt("POSITION");
                    AppWidgetMusic.this.mMusicInfo.isPlaying = bundle.getBoolean("ISPLAYING");
                }
                if (AppWidgetMusic.PLAYSTATE_CHANGED.equals(action)) {
                    AppWidgetMusic.this.mMusicUtilWidget.playMusic(AppWidgetMusic.this.mMusicInfo.isPlaying);
                }
                if (AppWidgetMusic.META_CHANGED.equals(action) && AppWidgetMusic.this.storageState()) {
                    AppWidgetMusic.this.mMusicUtilWidget.storageState();
                    AppWidgetMusic.this.prePosition = -1;
                }
                AppWidgetMusic.this.musicHandler.removeMessages(2);
                AppWidgetMusic.this.musicHandler.sendEmptyMessageDelayed(2, 200);
            } else if (AppWidgetMusic.FORCE_CLEAR_MUSIC.equals(action)) {
                AppWidgetMusic.this.prePosition = -1;
                AppWidgetMusic.this.mMusicUtilWidget.forceClearMusic();
            } else if (AppWidgetMusic.APPWIDGET_UPDATENEEDCLEAR.equals(action)) {
                AppWidgetMusic.this.needClear = true;
            } else if (AppWidgetMusic.KILL_SERVICE.equals(action)) {
                AppWidgetMusic.this.prePosition = -1;
                AppWidgetMusic.this.mMusicUtilWidget.killMusicService();
            } else if (AppWidgetMusic.MEDIA_UNMOUNTED.equals(action) || AppWidgetMusic.MEDIA_EJECT.equals(action)) {
                ComponentName serviceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
                Intent it = new Intent("FROMZERO");
                it.setComponent(serviceName);
                it.putExtra("fromZero", true);
                context.startService(intent);
            } else if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                AppWidgetMusic.this.prePosition = -1;
                bundle = intent.getBundleExtra("updatePlaylist");
                if (bundle != null) {
                    Log.i(AppWidgetMusic.TAG, "bundle!=null");
                    AppWidgetMusic.this.mMusicInfo.AlbumImg = bundle.getLongArray("ALBUMLIST");
                    AppWidgetMusic.this.mMusicInfo.SongName = bundle.getStringArray("TRACKLIST");
                    AppWidgetMusic.this.mMusicInfo.mPlayList = bundle.getLongArray("PLAYLIST");
                    AppWidgetMusic.this.mMusicInfo.SingerName = bundle.getStringArray("ARTISTLIST");
                    AppWidgetMusic.this.mMusicInfo.CurPosition = bundle.getInt("POSITION");
                    AppWidgetMusic.this.mMusicInfo.isPlaying = bundle.getBoolean("ISPLAYING");
                }
                AppWidgetMusic.this.handlerAction = action;
                AppWidgetMusic.this.musicHandler.removeMessages(2);
                AppWidgetMusic.this.musicHandler.sendEmptyMessage(2);
            } else if (AppWidgetMusic.ACTION_LYRICE.equals(action)) {
                AppWidgetMusic.this.setMusicPhoto(AppWidgetMusic.this.mMusicInfo.mPlayList[AppWidgetMusic.this.mMusicInfo.CurPosition], AppWidgetMusic.this.mMusicInfo.AlbumImg[AppWidgetMusic.this.mMusicInfo.CurPosition]);
            } else if (AppWidgetMusic.RINGCLIP_CHANGED.equals(action)) {
                Log.i(AppWidgetMusic.TAG, "RINGCLIP_CHANGED");
                AppWidgetMusic.this.musicHandler.removeMessages(3);
                AppWidgetMusic.this.musicHandler.sendEmptyMessageDelayed(3, 100);
            }
        }
    };
    private Context context;
    private int curRandomImageId = 0;
    private String handlerAction;
    private Integer[] images = null;
    private boolean isNext = true;
    private MusicInfo mMusicInfo;
    private MusicUtilWidget mMusicUtilWidget;
    private StorageManager mStorageManager = null;
    private StorageManagerWrapper mStorageManagerWrapper = null;
    public Handler musicHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                AppWidgetMusic.this.mMusicUtilWidget.onUpdate(AppWidgetMusic.this.mMusicInfo);
            } else if (msg.what == 2) {
                AppWidgetMusic.this.handMusicInfo();
            } else if (msg.what == 3) {
                AppWidgetMusic.this.changeMusicInfo();
            } else if (msg.what == 4) {
                AppWidgetMusic.this.mMusicUtilWidget.updateQueryData(AppWidgetMusic.this.mMusicInfo);
            }
        }
    };
    private boolean needClear = true;
    private int prePosition = -1;

    public class MusicInfo {
        public long[] AlbumImg = null;
        public int CurPosition = -1;
        public String[] SingerName = null;
        public String[] SongName = null;
        public long[] allSongId = null;
        public String[] allSongName = null;
        public Bitmap bitmap = null;
        public boolean isPlaying = false;
        public long[] mPlayList = null;
    }

    public interface MusicUtilWidget {
        void emptyMusic(Bitmap bitmap);

        void forceClearMusic();

        void killMusicService();

        void onUpdate(MusicInfo musicInfo);

        void playMusic(boolean z);

        void storageState();

        void updateQueryData(MusicInfo musicInfo);
    }

    class StorageManagerWrapper {
        private Object mTarget;
        private Method sMethodgetVolumePaths;
        private Method sMethodgetVolumeState;

        public StorageManagerWrapper(Object o) {
            try {
                this.mTarget = o;
                this.sMethodgetVolumePaths = o.getClass().getDeclaredMethod("getVolumePaths", new Class[0]);
                this.sMethodgetVolumeState = o.getClass().getDeclaredMethod("getVolumeState", new Class[]{String.class});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean getInternalStorageState() {
            try {
                String[] paths = (String[]) this.sMethodgetVolumePaths.invoke(this.mTarget, new Object[0]);
                int length = paths.length;
                int i = 0;
                while (i < length) {
                    String path = paths[i];
                    if (path.contains("/sdcard0") || path.contains("/sdcard")) {
                        return !((String) this.sMethodgetVolumeState.invoke(this.mTarget, new Object[]{path})).equals("mounted");
                    } else {
                        i++;
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public AppWidgetMusic(MusicUtilWidget mMusicUtilWidget, Context context, Integer[] images) {
        this.mMusicUtilWidget = mMusicUtilWidget;
        this.context = context;
        this.images = images;
        this.mMusicInfo = new MusicInfo();
    }

    public void registBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FORCE_CLEAR_MUSIC);
        filter.addAction(PLAYSTATE_CHANGED);
        filter.addAction(ACTION_LYRICE);
        filter.addAction(MEDIA_EJECT);
        filter.addAction(MEDIA_UNMOUNTED);
        filter.addAction(APPWIDGET_UPDATENEEDCLEAR);
        filter.addAction(META_CHANGED);
        filter.addAction(KILL_SERVICE);
        filter.addAction(QUEUE_CHANGED);
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(RINGCLIP_CHANGED);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.MEDIA_MOUNTED");
        filter2.addAction(MEDIA_UNMOUNTED);
        filter2.addAction("android.intent.action.MEDIA_REMOVED");
        filter2.addDataScheme("file");
        this.context.registerReceiver(this.MusicRecevier, filter);
        this.context.registerReceiver(this.MusicRecevier, filter2);
    }

    public void unregistBroadCast() {
        this.context.unregisterReceiver(this.MusicRecevier);
    }

    public void startMusicService() {
        ComponentName serviceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
        Intent intent = new Intent("com.android.music.musicservicecommand.nooperation");
        intent.setComponent(serviceName);
        this.context.startService(intent);
    }

    public void preSong() {
        MusicInfo musicInfo = this.mMusicInfo;
        musicInfo.CurPosition--;
        if (this.mMusicInfo.mPlayList == null || this.mMusicInfo.mPlayList.length <= 0) {
            this.mMusicInfo.CurPosition = -1;
        } else if (this.mMusicInfo.CurPosition == -1) {
            this.mMusicInfo.CurPosition = this.mMusicInfo.mPlayList.length - 1;
        }
        this.isNext = false;
        getMusicIntent(this.mMusicInfo.CurPosition);
    }

    public void nextSong() {
        MusicInfo musicInfo = this.mMusicInfo;
        musicInfo.CurPosition++;
        if (this.mMusicInfo.mPlayList == null || this.mMusicInfo.mPlayList.length <= 0) {
            this.mMusicInfo.CurPosition = -1;
        } else if (this.mMusicInfo.CurPosition == this.mMusicInfo.mPlayList.length) {
            this.mMusicInfo.CurPosition = 0;
        }
        this.isNext = true;
        getMusicIntent(this.mMusicInfo.CurPosition);
    }

    public boolean storageState() {
        if (this.mStorageManager == null) {
            this.mStorageManager = (StorageManager) this.context.getSystemService("storage");
            this.mStorageManagerWrapper = new StorageManagerWrapper(this.mStorageManager);
        }
        return this.mStorageManagerWrapper.getInternalStorageState();
    }

    public void playPause() {
        Intent intent = new Intent();
        intent.setClassName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
        intent.setAction(TOGGLEPAUSE_ACTION);
        this.context.startService(intent);
    }

    public String getSongName() {
        if (this.mMusicInfo.SongName != null) {
            return this.mMusicInfo.SongName[this.mMusicInfo.CurPosition];
        }
        return Events.DEFAULT_SORT_ORDER;
    }

    public String getSingerName() {
        if (this.mMusicInfo.SingerName != null) {
            return this.mMusicInfo.SingerName[this.mMusicInfo.CurPosition];
        }
        return Events.DEFAULT_SORT_ORDER;
    }

    public void playSongByPos(int index) {
        if (this.mMusicInfo.allSongId != null) {
            ComponentName sServiceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
            Intent intent = new Intent("com.android.music.musicservicecommand.orderplayposition");
            intent.setComponent(sServiceName);
            intent.putExtra(CurrentCity.POSITION, index);
            intent.putExtra("PLAYLIST", this.mMusicInfo.allSongId);
            this.context.startService(intent);
        }
    }

    public void entryMusic() {
        ComponentName serviceName;
        Intent it;
        if (this.mMusicInfo.isPlaying) {
            serviceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
            it = new Intent("APPWIDGET_UPDATEFLAG");
            it.setComponent(serviceName);
            this.context.startService(it);
            this.context.sendBroadcast(new Intent("com.android.music.finishself"));
            Intent mediaplay = new Intent();
            mediaplay.setComponent(new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackActivity"));
            mediaplay.addFlags(268435456);
            mediaplay.addFlags(2097152);
            mediaplay.putExtra("fromwidget", true);
            this.context.startActivity(mediaplay);
            return;
        }
        if (this.needClear) {
            serviceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
            it = new Intent("APPWIDGET_UPDATEFLAG");
            it.setComponent(serviceName);
            this.context.startService(it);
            this.context.sendBroadcast(new Intent("com.android.music.finishself"));
            this.needClear = false;
        }
        Intent trackbrowser = new Intent();
        trackbrowser.setComponent(new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.WidgetToTrackActivity"));
        trackbrowser.addFlags(268435456);
        trackbrowser.addFlags(2097152);
        this.context.startActivity(trackbrowser);
    }

    public Bitmap shadeLayer(Bitmap musicImg, Bitmap bg, int width, int height) {
        Bitmap btm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(btm);
        Paint xferPaint = new Paint(1);
        xferPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bg, 0.0f, 0.0f, null);
        canvas.drawBitmap(musicImg, 0.0f, 0.0f, xferPaint);
        return btm;
    }

    public Bitmap scaleImage(Bitmap bitmap, int width, int height) {
        float left;
        float top;
        float right;
        float bottom;
        float oldW = (float) bitmap.getWidth();
        float oldH = (float) bitmap.getHeight();
        float newW = (float) width;
        float newH = (float) height;
        float newRate = newW / newH;
        if (oldW / oldH >= newRate) {
            float tempWidth = newRate * oldH;
            left = (oldW - tempWidth) / 2.0f;
            top = 0.0f;
            right = left + tempWidth;
            bottom = oldH;
        } else {
            float tempHeight = oldW / newRate;
            left = 0.0f;
            top = (oldH - tempHeight) / 2.0f;
            right = oldW;
            bottom = top + tempHeight;
        }
        Bitmap output = Bitmap.createBitmap((int) newW, (int) newH, Config.ARGB_8888);
        new Canvas(output).drawBitmap(bitmap, new Rect((int) left, (int) top, (int) right, (int) bottom), new Rect(0, 0, (int) newW, (int) newH), null);
        return output;
    }

    public void getRandom() {
        Options opts = new Options();
        opts.inPreferredConfig = Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().openRawResource(this.images[this.curRandomImageId % this.images.length].intValue()), null, opts);
        if (this.isNext) {
            this.curRandomImageId++;
        } else {
            this.curRandomImageId--;
        }
        if (this.curRandomImageId > 6) {
            this.curRandomImageId = 0;
        } else if (this.curRandomImageId < 0) {
            this.curRandomImageId = 6;
        }
        this.mMusicUtilWidget.emptyMusic(bitmap);
    }

    private void setMusicPhoto(long song_id, long album_id) {
        final long j = song_id;
        final long j2 = album_id;
        new Thread() {
            public void run() {
                AppWidgetMusic.this.mMusicInfo.bitmap = AppWidgetMusic.this.getBitmap(j, j2);
                if (AppWidgetMusic.this.mMusicInfo.bitmap == null) {
                    Options opts = new Options();
                    opts.inPreferredConfig = Config.RGB_565;
                    AppWidgetMusic.this.mMusicInfo.bitmap = BitmapFactory.decodeStream(AppWidgetMusic.this.context.getResources().openRawResource(AppWidgetMusic.this.images[AppWidgetMusic.this.mMusicInfo.CurPosition % AppWidgetMusic.this.images.length].intValue()), null, opts);
                }
                AppWidgetMusic.this.musicHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    private Bitmap getBitmap(long song_id, long album_id) {
        ContentResolver res = this.context.getContentResolver();
        Bitmap btm = null;
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream inputStream = null;
            try {
                inputStream = res.openInputStream(uri);
                btm = BitmapFactory.decodeStream(inputStream, null, null);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (Exception e2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
            }
        }
        if (btm != null) {
            return btm;
        }
        try {
            ParcelFileDescriptor pfd = this.context.getContentResolver().openFileDescriptor(Uri.parse("content://media/external/audio/media/" + song_id + "/albumart"), "r");
            if (pfd != null) {
                return BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, null);
            }
            return btm;
        } catch (Exception e5) {
            return btm;
        }
    }

    private void getMusicIntent(int position) {
        ComponentName serviceName = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.MediaPlaybackService");
        Intent intent = new Intent("com.android.music.musicservicecommand.playposition");
        intent.putExtra("FromWidgetPos", position);
        intent.putExtra("app_flag", true);
        intent.setComponent(serviceName);
        this.context.startService(intent);
    }

    public void queryMusicDatabase() {
        new Thread() {
            /* JADX WARNING: Missing block: B:23:?, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Cursor cursor = AppWidgetMusic.this.getAllSongsCursor();
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0) {
                            int number = 0;
                            AppWidgetMusic.this.mMusicInfo.allSongId = new long[cursor.getCount()];
                            AppWidgetMusic.this.mMusicInfo.allSongName = new String[cursor.getCount()];
                            while (cursor.moveToNext()) {
                                AppWidgetMusic.this.mMusicInfo.allSongId[number] = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                                AppWidgetMusic.this.mMusicInfo.allSongName[number] = cursor.getString(cursor.getColumnIndexOrThrow(EventsColumns.TITLE));
                                number++;
                            }
                        }
                    } catch (Exception e) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                AppWidgetMusic.this.musicHandler.sendEmptyMessage(4);
                if (cursor != null) {
                    cursor.close();
                }
            }
        }.start();
    }

    public Cursor getAllSongsCursor() {
        String filtString = getFiltString();
        StringBuilder where = new StringBuilder();
        where.append("title != ''");
        where.append(" AND is_music=1");
        where.append(filtString);
        return this.context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, where.toString(), null, "title_key");
    }

    private String getFiltString() {
        String filtString = Events.DEFAULT_SORT_ORDER;
        try {
            SharedPreferences prefs = this.context.createPackageContext("com.android.bbkmusic", 2).getSharedPreferences("Music", 1);
            if (prefs.getBoolean("FILT_DURATION", false)) {
                filtString = filtString + " AND duration>=60000";
            }
            String filtSection = " AND bucket_id NOT IN ";
            String filtPath = prefs.getString("FILT_FOLDER_PATH", Events.DEFAULT_SORT_ORDER);
            if (filtPath.length() <= 0) {
                return filtString;
            }
            return filtString + filtSection + ("(" + filtPath.substring(0, filtPath.length() - 1) + ")");
        } catch (Exception e) {
            return filtString;
        }
    }

    private void changeMusicInfo() {
        Log.i(TAG, "changeMusicInfo");
        queryMusicDatabase();
    }

    private void handMusicInfo() {
        Log.i(TAG, "handMusicInfo");
        if (this.mMusicInfo.AlbumImg == null || this.mMusicInfo.SongName == null || this.mMusicInfo.mPlayList == null || this.mMusicInfo.mPlayList.length <= 0 || this.mMusicInfo.CurPosition < 0 || this.mMusicInfo.AlbumImg.length <= 0) {
            getRandom();
            return;
        }
        Log.i(TAG, "prePosition=" + this.prePosition);
        Log.i(TAG, "mMusicInfo.CurPosition=" + this.mMusicInfo.CurPosition);
        if (this.prePosition != this.mMusicInfo.CurPosition) {
            if (this.curRandomImageId != 0) {
                this.curRandomImageId = 0;
            }
            setMusicPhoto(this.mMusicInfo.mPlayList[this.mMusicInfo.CurPosition], this.mMusicInfo.AlbumImg[this.mMusicInfo.CurPosition]);
            this.prePosition = this.mMusicInfo.CurPosition;
        }
    }
}
