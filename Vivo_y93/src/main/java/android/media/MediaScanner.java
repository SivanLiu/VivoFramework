package android.media;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.app.backup.FullBackup;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaCodec.MetricsConstants;
import android.media.MediaFile.MediaFileType;
import android.media.midi.MidiDeviceInfo;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import dalvik.system.CloseGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final String BLACK_WHITE_LIST_FILE = "MediaStorage_BlackWhitelist.xml";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final String EXTERNAL_SD_PATH = "/storage/sdcard1";
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "_data", "format", "date_modified"};
    private static final String[] ID3_GENRES = new String[]{"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = new String[]{DownloadManager.COLUMN_ID};
    private static final String INTERNAL_SD_PATH = "/storage/emulated/0";
    public static final String LAST_INTERNAL_SCAN_FINGERPRINT = "lastScanFingerprint";
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final String ORIGINAL_UPDATE_REPOSITORY = "/data/bbkcore/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = new String[]{"playlist_id"};
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final String SYSTEM_SOUNDS_DIR = "/system/media/audio";
    private static final String TAG = "MediaScanner";
    private static long mBlackWhiteListLastModifyTime = 0;
    private static HashMap<String, String> mMediaPaths = new HashMap();
    private static HashMap<String, String> mNoMediaPaths = new HashMap();
    private static String[] mWhiteListIgnoreNomediaArray;
    private static String[] mWhiteListIgnoreNomediaPaths;
    private static String sLastInternalScanFingerprint;
    private final Uri mAudioUri;
    private final Options mBitmapOptions = new Options();
    private String[] mBlackArray;
    private boolean mBlackListLoaded = false;
    private HashMap<String, String> mBlacklistCache = null;
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultMessageFilename;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient = null;
    private final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    private final Uri mImagesUri;
    private MediaInserter mMediaInserter;
    private final ContentProviderClient mMediaProvider;
    private boolean mMessageSet;
    private boolean mMessageSetUri;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private boolean mNotificationSet;
    private boolean mNotificationSetUri;
    private int mOriginalCount;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mOriginalVideoCount;
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists = new ArrayList();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList();
    private ArrayList<String> mPlaylistFilePathList = new ArrayList();
    private final Uri mPlaylistsUri;
    private final boolean mProcessGenres;
    private final boolean mProcessPlaylists;
    private final Uri mVideoUri;
    private final String mVolumeName;
    private String[] mWhiteArray;
    private boolean mWhiteListLoaded = false;
    private HashMap<String, String> mWhitelistCache = null;

    public class BlackWhiteList implements Serializable {
        private ArrayList<PathList> mPathLists = new ArrayList();
        private int version = 1;

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public ArrayList<PathList> getPathList() {
            return this.mPathLists;
        }

        public void addPathList(PathList pathList) {
            this.mPathLists.add(pathList);
        }
    }

    public class BlackWhiteParseHandler extends DefaultHandler {
        private StringBuilder content;
        private BlackWhiteList mBlackWhiteList;
        private PathList mPathList;

        public BlackWhiteList getBlackWhiteLists() {
            return this.mBlackWhiteList;
        }

        public void startElement(String uri, String localName, String sName, Attributes attributes) throws SAXException {
            this.content = new StringBuilder();
            if (localName.equalsIgnoreCase("paths")) {
                this.mBlackWhiteList = new BlackWhiteList();
            } else if (localName.equalsIgnoreCase("array")) {
                this.mPathList = new PathList();
                this.mPathList.setPathListName(attributes.getValue(MidiDeviceInfo.PROPERTY_NAME));
            }
        }

        public void endElement(String uri, String localName, String qname) throws SAXException {
            if (localName.equalsIgnoreCase("version")) {
                this.mBlackWhiteList.setVersion(Integer.valueOf(this.content.toString()).intValue());
            } else if (localName.equalsIgnoreCase("array")) {
                this.mBlackWhiteList.addPathList(this.mPathList);
                this.mPathList = null;
            } else if (localName.equalsIgnoreCase("value")) {
                this.mPathList.addPaths(this.content.toString());
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.content.append(ch, start, length);
        }
    }

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged = false;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_CLASS)
    private class ImagesScanner {
        private long audioPos;
        private int camera_refocus;
        private int fullview;
        private int group_id;
        private String live_photo;

        /* synthetic */ ImagesScanner(MediaScanner this$0, ImagesScanner -this1) {
            this();
        }

        private ImagesScanner() {
            this.audioPos = 0;
            this.group_id = 0;
            this.fullview = 0;
            this.camera_refocus = 0;
            this.live_photo = null;
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs = new ArrayList(100);
        StringBuilder whereClause = new StringBuilder();

        public MediaBulkDeleter(ContentProviderClient provider, Uri baseUri) {
            this.mProvider = provider;
            this.mBaseUri = baseUri;
        }

        public void delete(long id) throws RemoteException {
            if (this.whereClause.length() != 0) {
                this.whereClause.append(",");
            }
            this.whereClause.append("?");
            this.whereArgs.add("" + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                int numrows = this.mProvider.delete(this.mBaseUri, "_id IN (" + this.whereClause.toString() + ")", (String[]) this.whereArgs.toArray(new String[size]));
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private class MyMediaScannerClient implements MediaScannerClient {
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
        private long mDate;
        private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        private int mDuration;
        private long mFileSize;
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsDrm;
        private long mLastModified;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        private String mLocation;
        private String mMimeType;
        private boolean mNoMedia;
        private String mPath;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        private int mRotation;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        public MyMediaScannerClient() {
            this.mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            this.mMimeType = mimeType;
            this.mFileType = 0;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (!isDirectory) {
                if (!noMedia && MediaScanner.isNoMediaPath(path)) {
                    noMedia = true;
                }
                this.mNoMedia = noMedia;
                if (mimeType != null) {
                    this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                }
                if (this.mFileType == 0) {
                    MediaFileType mediaFileType = MediaFile.getFileType(path);
                    if (mediaFileType != null) {
                        this.mFileType = mediaFileType.fileType;
                        if (this.mMimeType == null) {
                            this.mMimeType = mediaFileType.mimeType;
                        }
                    }
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmFileType(this.mFileType)) {
                    this.mFileType = getFileTypeFromDrm(path);
                }
            }
            FileEntry entry = MediaScanner.this.makeEntryFor(path);
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                } else {
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0);
                }
                entry.mLastModifiedChanged = true;
            }
            if (MediaScanner.this.mProcessPlaylists && MediaFile.isPlayListFileType(this.mFileType)) {
                MediaScanner.this.mPlayLists.add(entry);
                MediaScanner.this.mPlaylistFilePathList.add(path);
                return null;
            }
            this.mArtist = null;
            this.mAlbumArtist = null;
            this.mAlbum = null;
            this.mTitle = null;
            this.mComposer = null;
            this.mGenre = null;
            this.mTrack = 0;
            this.mYear = 0;
            this.mDuration = 0;
            this.mPath = path;
            this.mDate = 0;
            this.mLastModified = lastModified;
            this.mWriter = null;
            this.mCompilation = 0;
            this.mWidth = 0;
            this.mHeight = 0;
            this.mRotation = 0;
            this.mLocation = null;
            return entry;
        }

        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        /* JADX WARNING: Missing block: B:16:0x0056, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get6(r21.this$0)) == false) goto L_0x0058;
     */
        /* JADX WARNING: Missing block: B:20:0x0072, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get9(r21.this$0)) == false) goto L_0x0074;
     */
        /* JADX WARNING: Missing block: B:24:0x008e, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get4(r21.this$0)) != false) goto L_0x0090;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            Uri result = null;
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                if (entry.mPath != null) {
                    if (!MediaScanner.this.mDefaultNotificationSet) {
                        if (!doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                        }
                        Log.w(MediaScanner.TAG, "forcing rescan of " + entry.mPath + "since ringtone setting didn't finish");
                        scanAlways = true;
                    }
                    if (!MediaScanner.this.mDefaultRingtoneSet) {
                    }
                    if (!MediaScanner.this.mDefaultAlarmSet) {
                    }
                    if (MediaScanner.isSystemSoundWithMetadata(entry.mPath) && (Build.FINGERPRINT.equals(MediaScanner.sLastInternalScanFingerprint) ^ 1) != 0) {
                        Log.i(MediaScanner.TAG, "forcing rescan of " + entry.mPath + " since build fingerprint changed");
                        scanAlways = true;
                    }
                }
                if (entry != null && (entry.mLastModifiedChanged || scanAlways)) {
                    if (noMedia) {
                        result = endFile(entry, false, false, false, false, false);
                    } else {
                        String lowpath = path.toLowerCase(Locale.ROOT);
                        boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                        boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                        boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                        boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0;
                        boolean music = lowpath.indexOf(MediaScanner.MUSIC_DIR) <= 0 ? (ringtones || (notifications ^ 1) == 0 || (alarms ^ 1) == 0) ? false : podcasts ^ 1 : true;
                        if (this.mFileType == 4 || this.mFileType == 5 || this.mFileType == 11 || this.mFileType == 13 || this.mFileType == 216 || this.mFileType == 210 || this.mFileType == 212 || this.mFileType == 215) {
                            music = false;
                        }
                        boolean isaudio = MediaFile.isAudioFileType(this.mFileType);
                        boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                        boolean isimage = MediaFile.isImageFileType(this.mFileType);
                        if (isaudio || isvideo || isimage) {
                            path = Environment.maybeTranslateEmulatedPathToInternal(new File(path)).getAbsolutePath();
                        }
                        if (isaudio || isvideo) {
                            MediaScanner.this.processFile(path, mimeType, this);
                        }
                        if (isimage) {
                            processImageFile(path);
                        }
                        result = endFile(entry, ringtones, notifications, alarms, music, podcasts);
                    }
                }
                return result;
            } catch (RemoteException e) {
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
            }
        }

        private long parseDate(String date) {
            try {
                return this.mDateFormatter.parse(date).getTime();
            } catch (ParseException e) {
                return 0;
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - 48;
            while (start2 < length) {
                start = start2 + 1;
                ch = s.charAt(start2);
                if (ch < '0' || ch > '9') {
                    return result;
                }
                result = (result * 10) + (ch - 48);
                start2 = start;
            }
            return result;
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public void handleStringTag(String name, String value) {
            boolean z = true;
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase("composer") || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase("genre") || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase("year") || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
            } else if (name.equalsIgnoreCase(AudioFeatures.KEY_DURATION)) {
                this.mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase("compilation")) {
                this.mCompilation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, 0, 0) != 1) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase("date")) {
                this.mDate = parseDate(value);
            } else if (name.equalsIgnoreCase(MediaFormat.KEY_WIDTH)) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase(MediaFormat.KEY_HEIGHT)) {
                this.mHeight = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("rotation")) {
                this.mRotation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("location")) {
                this.mLocation = value.trim();
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return true;
            }
            Log.d(MediaScanner.TAG, "'" + input + "' -> '" + output + "', expected '" + expected + "'");
            return false;
        }

        private void testGenreNameConverter() {
            convertGenreCode("2", "Country");
            convertGenreCode("(2)", "Country");
            convertGenreCode("(2", "(2");
            convertGenreCode("2 Foo", "Country");
            convertGenreCode("(2) Foo", "Country");
            convertGenreCode("(2 Foo", "(2 Foo");
            convertGenreCode("2Foo", "2Foo");
            convertGenreCode("(2)Foo", "Country");
            convertGenreCode("200 Foo", "Foo");
            convertGenreCode("(200) Foo", "Foo");
            convertGenreCode("200Foo", "200Foo");
            convertGenreCode("(200)Foo", "Foo");
            convertGenreCode("200)Foo", "200)Foo");
            convertGenreCode("200) Foo", "200) Foo");
        }

        public String getGenreName(String genreTagValue) {
            if (genreTagValue == null) {
                return null;
            }
            int length = genreTagValue.length();
            if (length > 0) {
                boolean parenthesized = false;
                StringBuffer number = new StringBuffer();
                int i = 0;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = true;
                    }
                    i++;
                }
                char charAfterNumber = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAfterNumber == ')') || (!parenthesized && Character.isWhitespace(charAfterNumber))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= (short) 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == (short) 255) {
                                return null;
                            }
                            if (genreIndex >= (short) 255 || i + 1 >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAfterNumber == ')') {
                                i++;
                            }
                            String ret = genreTagValue.substring(i).trim();
                            if (ret.length() != 0) {
                                return ret;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return genreTagValue;
        }

        private void processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
            } catch (Throwable th) {
            }
        }

        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith(MetricsConstants.MODE_VIDEO)) {
                this.mMimeType = mimeType;
                this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
            }
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put("is_drm", Boolean.valueOf(this.mIsDrm));
            String resolution = null;
            if (this.mWidth > 0 && this.mHeight > 0) {
                map.put(MediaFormat.KEY_WIDTH, Integer.valueOf(this.mWidth));
                map.put(MediaFormat.KEY_HEIGHT, Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                String str;
                String str2;
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    str = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist;
                    map.put(str, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum;
                    map.put(str, str2);
                    map.put(AudioFeatures.KEY_DURATION, Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put("resolution", resolution);
                    }
                    if (this.mDate > 0) {
                        map.put("datetaken", Long.valueOf(this.mDate));
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && MediaFile.isAudioFileType(this.mFileType)) {
                    String str3 = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist;
                    map.put(str3, str2);
                    str3 = "album_artist";
                    if (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) {
                        str2 = null;
                    } else {
                        str2 = this.mAlbumArtist;
                    }
                    map.put(str3, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum;
                    map.put(str, str2);
                    map.put("composer", this.mComposer);
                    map.put("genre", this.mGenre);
                    if (this.mYear != 0) {
                        map.put("year", Integer.valueOf(this.mYear));
                    }
                    map.put("track", Integer.valueOf(this.mTrack));
                    map.put(AudioFeatures.KEY_DURATION, Integer.valueOf(this.mDuration));
                    map.put("compilation", Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        /* JADX WARNING: Missing block: B:33:0x0140, code:
            if (doesPathHaveFilename(r37.mPath, android.media.MediaScanner.-get6(r36.this$0)) != false) goto L_0x0142;
     */
        /* JADX WARNING: Missing block: B:134:0x0522, code:
            if (doesPathHaveFilename(r37.mPath, android.media.MediaScanner.-get6(r36.this$0)) != false) goto L_0x0524;
     */
        /* JADX WARNING: Missing block: B:142:0x056b, code:
            if (doesPathHaveFilename(r37.mPath, android.media.MediaScanner.-get9(r36.this$0)) != false) goto L_0x056d;
     */
        /* JADX WARNING: Missing block: B:150:0x05ab, code:
            if (doesPathHaveFilename(r37.mPath, android.media.MediaScanner.-get4(r36.this$0)) != false) goto L_0x05ad;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean music, boolean podcasts) throws RemoteException {
            if (this.mArtist == null || this.mArtist.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                values.put("title", MediaFile.getFileTitle(values.getAsString("_data")));
            }
            long rowId = entry.mRowId;
            if (MediaFile.isAudioFileType(this.mFileType) && (rowId == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put("is_ringtone", Boolean.valueOf(ringtones));
                values.put("is_notification", Boolean.valueOf(notifications));
                values.put("is_alarm", Boolean.valueOf(alarms));
                values.put("is_music", Boolean.valueOf(music));
                values.put("is_podcast", Boolean.valueOf(podcasts));
            } else if (MediaFile.isVideoFileType(this.mFileType) && (this.mNoMedia ^ 1) != 0) {
                values.put("orientation", Integer.valueOf(this.mRotation));
                if (this.mLocation != null) {
                    String mLongitude = null;
                    String mLatitude = null;
                    int find = this.mLocation.lastIndexOf("+");
                    if (-1 == find || find == 0) {
                        find = this.mLocation.lastIndexOf("-");
                    }
                    if (find != -1) {
                        mLongitude = this.mLocation.substring(find);
                        mLatitude = this.mLocation.substring(0, find);
                    }
                    if (!(mLongitude == null || mLatitude == null)) {
                        try {
                            values.put("longitude", Double.valueOf(Double.parseDouble(mLongitude)));
                            values.put("latitude", Double.valueOf(Double.parseDouble(mLatitude)));
                        } catch (NumberFormatException e) {
                            Log.e(MediaScanner.TAG, "NumberFormatException in MediaScanner.endFile()", e);
                        }
                    }
                }
                if (this.mFileType == 21 || this.mFileType == 22) {
                    values.put("live_photo", getLivePhoto(entry.mPath));
                }
            } else if ((this.mFileType == 31 || MediaFile.isRawImageFileType(this.mFileType)) && (this.mNoMedia ^ 1) != 0) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(entry.mPath);
                } catch (IOException e2) {
                }
                if (exifInterface != null) {
                    float[] latlng = new float[2];
                    if (exifInterface.getLatLong(latlng)) {
                        values.put("latitude", Float.valueOf(latlng[0]));
                        values.put("longitude", Float.valueOf(latlng[1]));
                    }
                    long time = exifInterface.getGpsDateTime();
                    Log.d(MediaScanner.TAG, "getGpsDateTime time:" + time);
                    if (time != -1) {
                        values.put("datetaken", Long.valueOf(time));
                    } else {
                        time = exifInterface.getDateTime();
                        Log.d(MediaScanner.TAG, "getDateTime time:" + time);
                        if (time != -1 && Math.abs((this.mLastModified * 1000) - time) >= AlarmManager.INTERVAL_DAY) {
                            values.put("datetaken", Long.valueOf(time));
                        }
                    }
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        int degree;
                        switch (orientation) {
                            case 3:
                                degree = 180;
                                break;
                            case 6:
                                degree = 90;
                                break;
                            case 8:
                                degree = 270;
                                break;
                            default:
                                degree = 0;
                                break;
                        }
                        values.put("orientation", Integer.valueOf(degree));
                    }
                    values.put("facing", exifInterface.getAttribute(ExifInterface.TAG_MAKER_NOTE));
                    if (this.mFileType == 31) {
                        ImagesScanner imagesScanner = new ImagesScanner(MediaScanner.this, null);
                        ImagesScanner mImageScanner = checkAudioSeek(entry.mPath);
                        values.put("audioseek", Long.valueOf(mImageScanner.audioPos));
                        values.put("group_id", Integer.valueOf(mImageScanner.group_id));
                        values.put("fullview", Integer.valueOf(mImageScanner.fullview));
                        values.put("camera_refocus", Integer.valueOf(mImageScanner.camera_refocus));
                        values.put("live_photo", mImageScanner.live_photo);
                    }
                }
            }
            Uri tableUri = MediaScanner.this.mFilesUri;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (!this.mNoMedia) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mVideoUri;
                } else if (MediaFile.isImageFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mImagesUri;
                } else if (MediaFile.isAudioFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mAudioUri;
                }
            }
            Uri result = null;
            boolean needToSetSettings = false;
            if (notifications && (MediaScanner.this.mDefaultNotificationSet ^ 1) != 0) {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename)) {
                    if (!(doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename) || TextUtils.isEmpty(MediaScanner.this.mDefaultMessageFilename))) {
                    }
                }
                needToSetSettings = true;
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename)) {
                    if (!doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                        if (!TextUtils.isEmpty(MediaScanner.this.mDefaultMessageFilename)) {
                        }
                        MediaScanner.this.mMessageSetUri = true;
                    }
                }
                MediaScanner.this.mNotificationSetUri = true;
            } else if (ringtones && (MediaScanner.this.mDefaultRingtoneSet ^ 1) != 0) {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) {
                }
                needToSetSettings = true;
            } else if (alarms && (MediaScanner.this.mDefaultAlarmSet ^ 1) != 0) {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename)) {
                }
                needToSetSettings = true;
            }
            if (rowId == 0) {
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    values.put("media_scanner_new_object_id", Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                }
                if (tableUri == MediaScanner.this.mFilesUri) {
                    int format = entry.mFormat;
                    if (format == 0) {
                        format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                    }
                    values.put("format", Integer.valueOf(format));
                }
                if (inserter == null || needToSetSettings) {
                    if (inserter != null) {
                        inserter.flushAll();
                    }
                    result = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                } else if (entry.mFormat == 12289) {
                    inserter.insertwithPriority(tableUri, values);
                } else {
                    inserter.insert(tableUri, values);
                }
                if (result != null) {
                    rowId = ContentUris.parseId(result);
                    entry.mRowId = rowId;
                }
            } else {
                result = ContentUris.withAppendedId(tableUri, rowId);
                values.remove("_data");
                int mediaType = 0;
                if (!MediaScanner.isNoMediaPath(entry.mPath)) {
                    int fileType = MediaFile.getFileTypeForMimeType(this.mMimeType);
                    if (MediaFile.isAudioFileType(fileType)) {
                        mediaType = 2;
                    } else if (MediaFile.isVideoFileType(fileType)) {
                        mediaType = 3;
                    } else if (MediaFile.isImageFileType(fileType)) {
                        mediaType = 1;
                    } else if (MediaFile.isPlayListFileType(fileType)) {
                        mediaType = 4;
                    }
                    values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(mediaType));
                }
                MediaScanner.this.mMediaProvider.update(result, values, null, null);
            }
            if (needToSetSettings) {
                if (notifications) {
                    if (MediaScanner.this.mNotificationSetUri) {
                        setRingtoneIfNotSet("notification_sound", tableUri, rowId);
                        setRingtoneIfNotSet("calendar_sound", tableUri, rowId);
                        MediaScanner.this.mNotificationSet = true;
                    } else if (MediaScanner.this.mMessageSetUri) {
                        setRingtoneIfNotSet("message_sound", tableUri, rowId);
                        setRingtoneIfNotSet("message_sound_sim2", tableUri, rowId);
                        MediaScanner.this.mMessageSet = true;
                    }
                    if (MediaScanner.this.mNotificationSet && MediaScanner.this.mMessageSet) {
                        MediaScanner.this.mDefaultNotificationSet = true;
                    }
                } else if (ringtones) {
                    setRingtoneIfNotSet("ringtone", tableUri, rowId);
                    setRingtoneIfNotSet("ringtone_sim2", tableUri, rowId);
                    MediaScanner.this.mDefaultRingtoneSet = true;
                } else if (alarms) {
                    setRingtoneIfNotSet("alarm_alert", tableUri, rowId);
                    MediaScanner.this.mDefaultAlarmSet = true;
                }
            }
            return result;
        }

        /* JADX WARNING: Removed duplicated region for block: B:49:0x00ac A:{SYNTHETIC, Splitter: B:49:0x00ac} */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00a0 A:{SYNTHETIC, Splitter: B:42:0x00a0} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String getLivePhoto(String path) {
            Exception e;
            Throwable th;
            RandomAccessFile mVideoFile = null;
            byte[] tmpBuff = new byte[47];
            try {
                RandomAccessFile mVideoFile2 = new RandomAccessFile(new File(path), FullBackup.ROOT_TREE_TOKEN);
                try {
                    mVideoFile2.seek(mVideoFile2.length() - 47);
                    mVideoFile2.read(tmpBuff);
                    mVideoFile2.seek(0);
                    if ((tmpBuff[0] & 255) == 0 && (tmpBuff[1] & 255) == 0 && (tmpBuff[2] & 255) == 0 && (tmpBuff[3] & 255) == 47 && (tmpBuff[32] & 255) == 255 && (tmpBuff[33] & 255) == 255 && (tmpBuff[34] & 255) == 255 && (tmpBuff[35] & 255) == 255) {
                        byte[] hashBytes = new byte[28];
                        for (int index = 0; index < 28; index++) {
                            hashBytes[index] = tmpBuff[index + 4];
                        }
                        String livephoto = new String(hashBytes);
                        if (mVideoFile2 != null) {
                            try {
                                mVideoFile2.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        return livephoto;
                    }
                    if (mVideoFile2 != null) {
                        try {
                            mVideoFile2.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    return null;
                } catch (Exception e3) {
                    e22 = e3;
                    mVideoFile = mVideoFile2;
                    try {
                        e22.printStackTrace();
                        if (mVideoFile != null) {
                            try {
                                mVideoFile.close();
                            } catch (Exception e222) {
                                e222.printStackTrace();
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (mVideoFile != null) {
                            try {
                                mVideoFile.close();
                            } catch (Exception e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    mVideoFile = mVideoFile2;
                    if (mVideoFile != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2222 = e4;
                e2222.printStackTrace();
                if (mVideoFile != null) {
                }
                return null;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2 A:{Splitter: B:3:0x0043, ExcHandler: all (th java.lang.Throwable)} */
        /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2 A:{Splitter: B:3:0x0043, ExcHandler: all (th java.lang.Throwable)} */
        /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2 A:{Splitter: B:3:0x0043, ExcHandler: all (th java.lang.Throwable)} */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing block: B:16:0x008c, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:18:?, code:
            r10.printStackTrace();
     */
        /* JADX WARNING: Missing block: B:65:0x01dd, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:67:?, code:
            r10.printStackTrace();
     */
        /* JADX WARNING: Missing block: B:69:0x01e3, code:
            r17 = r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @VivoHook(hookType = VivoHookType.NEW_METHOD)
        private ImagesScanner checkAudioSeek(String pictureFilePath) {
            Exception e;
            byte[] buff = new byte[4096];
            byte[] tmpBuff = new byte[47];
            int bufferSize = buff.length;
            RandomAccessFile randomAccessFile = null;
            boolean checkDone = false;
            boolean rollBack = false;
            long seekPos = 0;
            ImagesScanner imagesScanner = new ImagesScanner(MediaScanner.this, null);
            try {
                RandomAccessFile randomAccessFile2 = new RandomAccessFile(new File(pictureFilePath), "rw");
                try {
                    randomAccessFile2.seek((randomAccessFile2.length() - 1) - 46);
                    randomAccessFile2.read(tmpBuff);
                    if ((tmpBuff[45] & 255) == 255 && (tmpBuff[46] & 255) == 217) {
                        if (randomAccessFile2 != null) {
                            randomAccessFile2.close();
                        }
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        return imagesScanner;
                    } else if ((tmpBuff[45] & 255) != 255 && (tmpBuff[46] & 255) != 217 && (tmpBuff[46] & 255) == 177 && (tmpBuff[45] & 255) == 162 && (tmpBuff[44] & 255) == 147 && (tmpBuff[43] & 255) == 132 && (tmpBuff[42] & 255) == 117 && (tmpBuff[41] & 255) == 102 && (tmpBuff[40] & 255) == 87 && (tmpBuff[39] & 255) == 72 && (tmpBuff[38] & 255) == 57 && (tmpBuff[37] & 255) == 42 && (tmpBuff[36] & 255) == 27) {
                        imagesScanner.group_id = ((((tmpBuff[28] << 24) & -1) + ((tmpBuff[29] << 16) & 16777215)) + ((tmpBuff[30] << 8) & 65535)) + (tmpBuff[31] & 255);
                        imagesScanner.fullview = tmpBuff[27] & 255;
                        if (randomAccessFile2 != null) {
                            randomAccessFile2.close();
                        }
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (Exception e22) {
                                e22.printStackTrace();
                            }
                        }
                        return imagesScanner;
                    } else if ((tmpBuff[46] & 255) == 178 && (tmpBuff[45] & 255) == 162 && (tmpBuff[44] & 255) == 147 && (tmpBuff[43] & 255) == 132 && (tmpBuff[42] & 255) == 117 && (tmpBuff[41] & 255) == 102 && (tmpBuff[40] & 255) == 87 && (tmpBuff[39] & 255) == 72 && (tmpBuff[38] & 255) == 57 && (tmpBuff[37] & 255) == 42 && (tmpBuff[36] & 255) == 27) {
                        imagesScanner.camera_refocus = ((((tmpBuff[28] << 24) & -1) + ((tmpBuff[29] << 16) & 16777215)) + ((tmpBuff[30] << 8) & 65535)) + (tmpBuff[31] & 255);
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (Exception e222) {
                                e222.printStackTrace();
                            }
                        }
                        return imagesScanner;
                    } else if ((tmpBuff[0] & 255) == 0 && (tmpBuff[1] & 255) == 0 && (tmpBuff[2] & 255) == 0 && (tmpBuff[3] & 255) == 47 && (tmpBuff[32] & 255) == 255 && (tmpBuff[33] & 255) == 255 && (tmpBuff[34] & 255) == 255 && (tmpBuff[35] & 255) == 255) {
                        byte[] hashBytes = new byte[28];
                        for (int index = 0; index < 28; index++) {
                            hashBytes[index] = tmpBuff[index + 4];
                        }
                        imagesScanner.live_photo = new String(hashBytes);
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (Exception e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        return imagesScanner;
                    } else {
                        randomAccessFile2.seek(0);
                        while (-1 != randomAccessFile2.read(buff)) {
                            int i = 0;
                            while (i < buff.length - 1) {
                                if ((buff[i] & 255) == 82) {
                                    if (i < (bufferSize - 1) - 3) {
                                        if ((buff[i + 1] & 255) == 73 && (buff[i + 2] & 255) == 70 && (buff[i + 3] & 255) == 70) {
                                            checkDone = true;
                                            imagesScanner.audioPos = ((long) i) + seekPos;
                                            break;
                                        }
                                    }
                                    rollBack = true;
                                    break;
                                }
                                i++;
                            }
                            if (checkDone) {
                                break;
                            }
                            seekPos = (((long) i) + seekPos) + 1;
                            if (rollBack) {
                                rollBack = false;
                                randomAccessFile2.seek(seekPos);
                            }
                        }
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (Exception e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        return imagesScanner;
                    }
                } catch (Exception e3) {
                    e22222 = e3;
                    randomAccessFile = randomAccessFile2;
                } catch (Throwable th) {
                }
            } catch (Exception e4) {
                e22222 = e4;
                try {
                    e22222.printStackTrace();
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (Exception e222222) {
                            e222222.printStackTrace();
                        }
                    }
                    return imagesScanner;
                } catch (Throwable th2) {
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (Exception e2222222) {
                            e2222222.printStackTrace();
                        }
                    }
                    return imagesScanner;
                }
            }
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (path.regionMatches(pathFilenameStart, filename, 0, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
                return true;
            }
            return false;
        }

        private void setRingtoneIfNotSet(String settingName, Uri uri, long rowId) {
            if (!MediaScanner.this.wasRingtoneAlreadySet(settingName)) {
                ContentResolver cr = MediaScanner.this.mContext.getContentResolver();
                if (TextUtils.isEmpty(System.getString(cr, settingName))) {
                    Uri settingUri = System.getUriFor(settingName);
                    RingtoneManager.setActualDefaultRingtoneUri(MediaScanner.this.mContext, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
                }
                System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), 1);
            }
        }

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return 0;
            }
            int resultFileType = 0;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, null)) {
                this.mIsDrm = true;
                String drmMimetype = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
                if (drmMimetype != null) {
                    this.mMimeType = drmMimetype;
                    resultFileType = MediaFile.getFileTypeForMimeType(drmMimetype);
                }
            }
            return resultFileType;
        }
    }

    public class PathList {
        private ArrayList<String> mPath = new ArrayList();
        private String mPathListName;

        public void setPathListName(String listName) {
            this.mPathListName = listName;
        }

        public String getPathListName() {
            return this.mPathListName;
        }

        public void addPaths(String path) {
            this.mPath.add(path);
        }

        public ArrayList<String> getPaths() {
            return this.mPath;
        }
    }

    private static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        /* synthetic */ PlaylistEntry(PlaylistEntry -this0) {
            this();
        }

        private PlaylistEntry() {
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild(BatteryManager.EXTRA_SEQUENCE).getChild("media").setElementListener(this);
            this.handler = root.getContentHandler();
        }

        public void start(Attributes attributes) {
            String path = attributes.getValue("", "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        public void end() {
        }

        ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private native void setLocale(String str);

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    private void loadBlackWhiteList(Context context) {
        getBlackWhiteList();
        if (mWhiteListIgnoreNomediaArray != null) {
            int length = mWhiteListIgnoreNomediaArray.length;
            mWhiteListIgnoreNomediaPaths = new String[(length * 2)];
            for (int i = 0; i < length; i++) {
                mWhiteListIgnoreNomediaPaths[i] = INTERNAL_SD_PATH + mWhiteListIgnoreNomediaArray[i];
                mWhiteListIgnoreNomediaPaths[i + length] = EXTERNAL_SD_PATH + mWhiteListIgnoreNomediaArray[i];
            }
        }
    }

    public MediaScanner(Context c, String volumeName) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        this.mBitmapOptions.inSampleSize = 1;
        this.mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");
        if (sLastInternalScanFingerprint == null) {
            sLastInternalScanFingerprint = this.mContext.getSharedPreferences(SCANNED_BUILD_PREFS_NAME, 0).getString(LAST_INTERNAL_SCAN_FINGERPRINT, new String());
        }
        this.mAudioUri = Media.getContentUri(volumeName);
        this.mVideoUri = Video.Media.getContentUri(volumeName);
        this.mImagesUri = Images.Media.getContentUri(volumeName);
        this.mFilesUri = Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        if (volumeName.equals("internal")) {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        } else {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = Playlists.getContentUri(volumeName);
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    setLocale(language + "_" + country);
                } else {
                    setLocale(language);
                }
            }
        }
        this.mCloseGuard.open("close");
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
        this.mDefaultMessageFilename = SystemProperties.get("ro.config.message_sound");
    }

    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null ? prop.equals("true") : false;
    }

    private static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith("/system/media/audio/alarms/") || path.startsWith("/system/media/audio/ringtones/") || path.startsWith("/system/media/audio/notifications/")) {
            return true;
        }
        return false;
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    public void setBlacklistStr() {
        if (this.mBlackArray != null && this.mBlackArray.length > 0 && this.mBlackListLoaded) {
            this.mBlacklistCache = parseBlackList(this.mContext, this.mBlackArray);
        }
    }

    public void setWhitelistStr() {
        if (this.mWhiteArray != null && this.mWhiteArray.length > 0 && this.mWhiteListLoaded) {
            this.mWhitelistCache = parseBlackList(this.mContext, this.mWhiteArray);
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String where;
        String[] selectionArgs;
        boolean wasRingtoneAlreadySet;
        Cursor c = null;
        this.mPlayLists.clear();
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[]{"", filePath};
        } else {
            where = "_id>?";
            selectionArgs = new String[]{""};
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet("ringtone");
        if (wasRingtoneAlreadySet("notification_sound")) {
            wasRingtoneAlreadySet = wasRingtoneAlreadySet("message_sound");
        } else {
            wasRingtoneAlreadySet = false;
        }
        this.mDefaultNotificationSet = wasRingtoneAlreadySet;
        this.mDefaultAlarmSet = wasRingtoneAlreadySet("alarm_alert");
        Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter("deletedata", "false");
        MediaBulkDeleter mediaBulkDeleter = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            long lastId = Long.MIN_VALUE;
            try {
                Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, "1000").build();
                while (true) {
                    selectionArgs[0] = "" + lastId;
                    if (c != null) {
                        c.close();
                    }
                    c = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, DownloadManager.COLUMN_ID, null);
                    if (c != null) {
                        if (c.getCount() == 0) {
                            break;
                        }
                        while (c.moveToNext()) {
                            long rowId = c.getLong(0);
                            String path = c.getString(1);
                            int format = c.getInt(2);
                            long lastModified = c.getLong(3);
                            lastId = rowId;
                            if (path != null) {
                                if (path.startsWith("/")) {
                                    boolean exists = false;
                                    try {
                                        exists = Os.access(path, OsConstants.F_OK);
                                    } catch (ErrnoException e) {
                                    }
                                    if (exists) {
                                        continue;
                                    } else if ((MtpConstants.isAbstractObject(format) ^ 1) != 0) {
                                        MediaFileType mediaFileType = MediaFile.getFileType(path);
                                        if (!MediaFile.isPlayListFileType(mediaFileType == null ? 0 : mediaFileType.fileType)) {
                                            mediaBulkDeleter.delete(rowId);
                                            if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                                mediaBulkDeleter.flush();
                                                this.mMediaProvider.call("unhide", new File(path).getParent(), null);
                                            }
                                        }
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                mediaBulkDeleter.flush();
            }
        }
        if (c != null) {
            c.close();
        }
        mediaBulkDeleter.flush();
        this.mOriginalCount = 0;
        c = this.mMediaProvider.query(this.mImagesUri, ID_PROJECTION, null, null, null, null);
        if (c != null) {
            this.mOriginalCount = c.getCount();
            c.close();
        }
    }

    private static HashMap<String, String> parseBlackList(Context context, String[] string) {
        HashMap<String, String> mBlacklist = new HashMap();
        if (string == null) {
            return null;
        }
        String internalPath = Environment.getExternalStorageDirectory().toString();
        for (String replace : string) {
            String replace2 = internalPath + replace.replace("%", ".+");
            Log.d(TAG, "regular path =" + replace2);
            mBlacklist.put(replace2, replace2);
        }
        return mBlacklist;
    }

    private void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        this.mPlayLists.clear();
    }

    private void releaseResources() {
        if (this.mDrmManagerClient != null) {
            this.mDrmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    private boolean isBlackWhitelistFileUpdated() {
        File destFile = new File(this.mContext.getFilesDir() + "/" + BLACK_WHITE_LIST_FILE);
        if (destFile.exists() && destFile.lastModified() > mBlackWhiteListLastModifyTime) {
            Log.d(TAG, this.mContext.getFilesDir() + "/" + BLACK_WHITE_LIST_FILE + " is updated");
            mBlackWhiteListLastModifyTime = destFile.lastModified();
            return true;
        } else if (mBlackWhiteListLastModifyTime != 0) {
            return false;
        } else {
            Log.d(TAG, "init with Assets' blackwhitelist file one time");
            mBlackWhiteListLastModifyTime++;
            return true;
        }
    }

    private void initBlackWhiteList() {
        if (isBlackWhitelistFileUpdated()) {
            Log.d(TAG, "to initBlackWhiteList");
            loadBlackWhiteList(this.mContext);
            setBlacklistStr();
            setWhitelistStr();
        }
    }

    private boolean isDirInBlacklist(String string) {
        if (this.mBlacklistCache != null) {
            if (this.mBlacklistCache.containsKey(string)) {
                return true;
            }
            for (String str : this.mBlacklistCache.keySet()) {
                if (Pattern.compile(str).matcher(string).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDirInWhitelist(String string) {
        if (this.mWhitelistCache != null) {
            if (this.mWhitelistCache.containsKey(string)) {
                return true;
            }
            for (String str : this.mWhitelistCache.keySet()) {
                if (Pattern.compile(str).matcher(string).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void scanDirectoriesForMedia(String[] directories, String volumeName) {
        try {
            long start = System.currentTimeMillis();
            initBlackWhiteList();
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, (int) RunningAppProcessInfo.IMPORTANCE_EMPTY);
            for (int i = 0; i < directories.length; i++) {
                if (directories[i] != null) {
                    vivoProcessDirectoryForMedia(directories[i], this.mClient);
                }
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            long end = System.currentTimeMillis();
            Log.d(TAG, " prescan time: " + (prescan - start) + "ms\n");
            Log.d(TAG, "    scan time: " + (scan - prescan) + "ms\n");
            Log.d(TAG, "postscan time: " + (end - scan) + "ms\n");
            Log.d(TAG, "   total time: " + (end - start) + "ms\n");
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        } finally {
            releaseResources();
        }
    }

    public void scanForWhiteFolder(String[] directories, String volumeName) {
        try {
            long start = System.currentTimeMillis();
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, (int) RunningAppProcessInfo.IMPORTANCE_EMPTY);
            for (int i = 0; i < directories.length; i++) {
                if (directories[i] != null) {
                    processDirectory(directories[i], this.mClient);
                }
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            long end = System.currentTimeMillis();
            Log.d(TAG, " prescan time: " + (prescan - start) + "ms\n");
            Log.d(TAG, "    scan time: " + (scan - prescan) + "ms\n");
            Log.d(TAG, "postscan time: " + (end - scan) + "ms\n");
            Log.d(TAG, "   total time: " + (end - start) + "ms\n");
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        } finally {
            releaseResources();
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void scanDirectories(String[] directories) {
        try {
            long start = System.currentTimeMillis();
            initBlackWhiteList();
            prescan(null, true);
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, (int) RunningAppProcessInfo.IMPORTANCE_EMPTY);
            for (int i = 0; i < directories.length; i++) {
                if (directories[i] != null) {
                    vivoProcessDirectory(directories[i], this.mClient);
                }
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        } finally {
            releaseResources();
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public Uri scanSingleFile(String path, String mimeType) {
        try {
            initBlackWhiteList();
            prescan(path, true);
            File file = new File(path);
            if (file.exists() && (file.canRead() ^ 1) == 0) {
                String str = path;
                String str2 = mimeType;
                Uri doScanFile = this.mClient.doScanFile(str, str2, file.lastModified() / 1000, file.length(), file.isDirectory(), true, isNoMediaPath(path));
                releaseResources();
                return doScanFile;
            }
            releaseResources();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            releaseResources();
            return null;
        } catch (Throwable th) {
            releaseResources();
            throw th;
        }
    }

    /* JADX WARNING: Missing block: B:27:0x009f, code:
            if (r10.regionMatches(true, r7 + 1, "AlbumArtSmall", 0, 13) == false) goto L_0x00a1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0 || lastSlash >= path.length() || !path.regionMatches(lastSlash + 1, ".", 0, 1)) {
            if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
                if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                    return true;
                }
                if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                    if (!path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)) {
                        if (!path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                            int length = (path.length() - lastSlash) - 1;
                            if (length == 17) {
                            }
                            if (length == 10) {
                                if (path.regionMatches(true, lastSlash + 1, "Folder", 0, 6)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        Log.d(TAG, "beginFile, ignore hidden file: " + path);
        return true;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                mMediaPaths.clear();
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x0081, code:
            return isNoMediaFile(r10);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return true;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(0, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return true;
            } else if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        String filePath = path.substring(0, slashIndex);
                        if (!ifInWhitelistNomediaPath(filePath) && new File(filePath + ".nomedia").exists()) {
                            mNoMediaPaths.put(parent, "");
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, "");
            }
        }
    }

    private static boolean ifInWhitelistNomediaPath(String path) {
        long t1 = System.currentTimeMillis();
        if (mWhiteListIgnoreNomediaPaths != null) {
            for (String str : mWhiteListIgnoreNomediaPaths) {
                if ((str + "/").toLowerCase(Locale.US).equals(path.toLowerCase(Locale.US))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void scanMtpFile(String path, int objectHandle, int format) {
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioFileType(fileType) || (MediaFile.isVideoFileType(fileType) ^ 1) == 0 || (MediaFile.isImageFileType(fileType) ^ 1) == 0 || (MediaFile.isPlayListFileType(fileType) ^ 1) == 0 || (MediaFile.isDrmFileType(fileType) ^ 1) == 0) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListFileType(fileType)) {
                    prescan(null, true);
                    FileEntry entry = makeEntryFor(path);
                    if (entry != null) {
                        fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, 0, null, null);
                        processPlayList(entry, fileList);
                    }
                } else {
                    prescan(path, false);
                    this.mClient.doScanFile(path, mediaFileType.mimeType, lastModifiedSeconds, file.length(), format == 12289, true, isNoMediaPath(path));
                }
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (Throwable th) {
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
                throw th;
            }
            return;
        }
        ContentValues values = new ContentValues();
        values.put("_size", Long.valueOf(file.length()));
        values.put("date_modified", Long.valueOf(lastModifiedSeconds));
        try {
            this.mMediaProvider.update(Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in scanMtpFile", e2);
        }
    }

    FileEntry makeEntryFor(String path) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{path};
            cursor = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", selectionArgs, null, null);
            if (cursor.moveToFirst()) {
                String str = path;
                FileEntry fileEntry = new FileEntry(cursor.getLong(0), str, cursor.getLong(3), cursor.getInt(2));
                if (cursor != null) {
                    cursor.close();
                }
                return fileEntry;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int matchPaths(String path1, String path2) {
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start2 = slash2 > backSlash2 ? slash2 : backSlash2;
            start1 = start1 < 0 ? 0 : start1 + 1;
            start2 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start1;
            if (end2 - start2 != length || !path1.regionMatches(true, start1, path2, start2, length)) {
                break;
            }
            result++;
            end1 = start1 - 1;
            end2 = start2 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Integer.MAX_VALUE) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Integer.MAX_VALUE;
                } else {
                    int matchLength = matchPaths(data, entry.path);
                    if (matchLength > entry.bestmatchlevel) {
                        entry.bestmatchid = rowId;
                        entry.bestmatchlevel = matchLength;
                    }
                }
            }
        }
        return done;
    }

    private void cachePlaylistEntry(String line, String playListDirectory) {
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            boolean fullPath = ch1 != '/' ? Character.isLetter(ch1) && line.charAt(1) == ':' && line.charAt(2) == '\\' : true;
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext()) {
            if (matchEntries(fileList.getLong(0), fileList.getString(1))) {
                break;
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel > 0) {
                try {
                    values.clear();
                    values.put("play_order", Integer.valueOf(index));
                    values.put("audio_id", Long.valueOf(entry.bestmatchid));
                    this.mMediaProvider.insert(playlistUri, values);
                    index++;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MediaScanner.processCachedPlaylist()", e);
                    return;
                }
            }
        }
        this.mPlaylistEntries.clear();
    }

    private void vivoProcessDirectoryForMedia(String path, MediaScannerClient mediaScannerClient) {
        boolean noMediaPath = isNoMediaPath(path);
        String[] list = new File(path).list();
        if (list != null && list.length > 0) {
            for (String append : list) {
                String string = new StringBuffer().append(path).append("/").append(append).toString();
                File file = new File(string);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        vivoProcessDirectoryForMedia(string, mediaScannerClient);
                    } else {
                        boolean audioFileType = false;
                        boolean videoFileType = false;
                        MediaFileType fileType = MediaFile.getFileType(string);
                        if (fileType != null) {
                            audioFileType = MediaFile.isAudioFileType(fileType.fileType);
                            videoFileType = MediaFile.isVideoFileType(fileType.fileType);
                        }
                        if (audioFileType || videoFileType) {
                            this.mClient.scanFile(string, file.lastModified() / 1000, file.length(), false, noMediaPath);
                        }
                    }
                }
            }
        }
    }

    private void vivoProcessDirectory(String path, MediaScannerClient mediaScannerClient) {
        boolean noMediaPath = isNoMediaPath(path);
        String[] list = new File(path).list();
        if (list != null && list.length > 0) {
            for (String append : list) {
                String string = new StringBuffer().append(path).append("/").append(append).toString();
                File file = new File(string);
                if (file.exists()) {
                    if (!file.isDirectory()) {
                        this.mClient.scanFile(string, file.lastModified() / 1000, file.length(), false, noMediaPath);
                    } else if (isDirInWhitelist(string)) {
                        Log.d(TAG, " dir in whitelist " + string);
                        this.mClient.scanFile(string, file.lastModified() / 1000, 0, true, noMediaPath);
                        vivoProcessDirectory(string, mediaScannerClient);
                    } else if (isDirInBlacklist(string)) {
                        Log.d(TAG, " dir in blacklist " + string);
                        this.mClient.scanFile(string, file.lastModified() / 1000, 0, true, noMediaPath);
                    } else {
                        this.mClient.scanFile(string, file.lastModified() / 1000, 0, true, noMediaPath);
                        vivoProcessDirectory(string, mediaScannerClient);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060 A:{SYNTHETIC, Splitter: B:25:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0072 A:{SYNTHETIC, Splitter: B:31:0x0072} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    String line = reader2.readLine();
                    this.mPlaylistEntries.clear();
                    while (line != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            cachePlaylistEntry(line, playListDirectory);
                        }
                        line = reader2.readLine();
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0068 A:{SYNTHETIC, Splitter: B:25:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007a A:{SYNTHETIC, Splitter: B:31:0x007a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    this.mPlaylistEntries.clear();
                    for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                        if (line.startsWith("File")) {
                            int equals = line.indexOf(61);
                            if (equals > 0) {
                                cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                            }
                        }
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x006b A:{SYNTHETIC, Splitter: B:31:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059 A:{SYNTHETIC, Splitter: B:25:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0044 A:{SYNTHETIC, Splitter: B:17:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        SAXException e;
        IOException e2;
        Throwable th;
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                FileInputStream fis2 = new FileInputStream(f);
                try {
                    this.mPlaylistEntries.clear();
                    Xml.parse(fis2, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                    processCachedPlaylist(fileList, values, uri);
                    fis = fis2;
                } catch (SAXException e3) {
                    e = e3;
                    fis = fis2;
                    e.printStackTrace();
                    if (fis == null) {
                        try {
                            fis.close();
                            return;
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22);
                            return;
                        }
                    }
                    return;
                } catch (IOException e4) {
                    e22 = e4;
                    fis = fis2;
                    try {
                        e22.printStackTrace();
                        if (fis == null) {
                            try {
                                fis.close();
                                return;
                            } catch (IOException e222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e222);
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e2222);
                        }
                    }
                    throw th;
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22222);
                }
            }
        } catch (SAXException e5) {
            e = e5;
            e.printStackTrace();
            if (fis == null) {
            }
        } catch (IOException e6) {
            e22222 = e6;
            e22222.printStackTrace();
            if (fis == null) {
            }
        }
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0) {
            throw new IllegalArgumentException("bad path " + path);
        }
        Uri membersUri;
        long rowId = entry.mRowId;
        String name = values.getAsString(MidiDeviceInfo.PROPERTY_NAME);
        if (name == null) {
            name = values.getAsString("title");
            if (name == null) {
                int lastDot = path.lastIndexOf(46);
                if (lastDot < 0) {
                    name = path.substring(lastSlash + 1);
                } else {
                    name = path.substring(lastSlash + 1, lastDot);
                }
            }
        }
        values.put(MidiDeviceInfo.PROPERTY_NAME, name);
        values.put("date_modified", Long.valueOf(entry.mLastModified));
        Uri uri;
        if (rowId == 0) {
            values.put("_data", path);
            uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
            rowId = ContentUris.parseId(uri);
            membersUri = Uri.withAppendedPath(uri, "members");
        } else {
            uri = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
            this.mMediaProvider.update(uri, values, null, null);
            membersUri = Uri.withAppendedPath(uri, "members");
            this.mMediaProvider.delete(membersUri, null, null);
        }
        String playListDirectory = path.substring(0, lastSlash + 1);
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        if (fileType == 41) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 42) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 43) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = (FileEntry) iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, cursor);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    public void preScanAll(String volume) {
        try {
            initBlackWhiteList();
            prescan(null, true);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
        }
    }

    public void postScanAll(ArrayList<String> playlistFilePathList) {
        try {
            if (this.mProcessPlaylists) {
                for (String path : playlistFilePathList) {
                    FileEntry entry = makeEntryFor(path);
                    long lastModified = new File(path).lastModified();
                    long delta = entry != null ? lastModified - entry.mLastModified : 0;
                    boolean wasModified = delta > 1 || delta < -1;
                    if (entry == null || wasModified) {
                        if (wasModified) {
                            entry.mLastModified = lastModified;
                        } else {
                            entry = new FileEntry(0, path, lastModified, 0);
                        }
                        entry.mLastModifiedChanged = true;
                    }
                    this.mPlayLists.add(entry);
                }
                processPlayLists();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
        }
        this.mPlayLists.clear();
        Log.v(TAG, "postScanAll");
    }

    public ArrayList<String> scanFolders(Handler insertHanlder, String[] folders, String volume, boolean isSingelFile) {
        try {
            initBlackWhiteList();
            this.mMediaInserter = new MediaInserter(insertHanlder, 100);
            int i = 0;
            int length = folders.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String path = folders[i2];
                if (isSingelFile) {
                    File file = new File(path);
                    this.mClient.doScanFile(path, null, file.lastModified() / 1000, file.length(), file.isDirectory(), false, isNoMediaPath(path));
                } else {
                    vivoProcessDirectory(path, this.mClient);
                }
                i = i2 + 1;
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        }
        return this.mPlaylistFilePathList;
    }

    public ArrayList<String> scanFolders(String[] folders, String volume, boolean isSingelFileOrEmptyFolder) {
        try {
            initBlackWhiteList();
            for (int j = 0; j < folders.length; j++) {
                if (folders[j] != null) {
                    prescan(folders[j], true);
                }
            }
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, (int) RunningAppProcessInfo.IMPORTANCE_EMPTY);
            int i = 0;
            int length = folders.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String folder = folders[i2];
                File file = new File(folder);
                if (file.exists()) {
                    this.mClient.doScanFile(folder, null, file.lastModified() / 1000, file.length(), file.isDirectory(), false, isNoMediaPath(folder));
                }
                if (!isSingelFileOrEmptyFolder) {
                    vivoProcessDirectory(folder, this.mClient);
                }
                i = i2 + 1;
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        }
        return this.mPlaylistFilePathList;
    }

    private void getBlackWhiteList() {
        BlackWhiteList blackWhiteList = parseXmlToGetBlackWhiteList(this.mContext);
        if (blackWhiteList != null) {
            getFinalLists(blackWhiteList);
        }
    }

    private void getFinalLists(BlackWhiteList hifiWhiteList) {
        ArrayList<String> blackList = new ArrayList();
        ArrayList<String> whiteList = new ArrayList();
        ArrayList<String> whiteListignorenomedia = new ArrayList();
        ArrayList<PathList> pathLists = hifiWhiteList.getPathList();
        if (pathLists != null) {
            for (int i = 0; i < pathLists.size(); i++) {
                PathList pathList = (PathList) pathLists.get(i);
                if (pathList != null) {
                    String listName = pathList.getPathListName();
                    if (listName != null) {
                        if (listName.equals("blacklist_scanner")) {
                            blackList = pathList.getPaths();
                        } else if (listName.equals("whitelist_scanner")) {
                            whiteList = pathList.getPaths();
                        } else if (listName.equals("whitelist_ignorenomedia")) {
                            whiteListignorenomedia = pathList.getPaths();
                        }
                    }
                }
            }
        }
        if (blackList.size() > 0) {
            this.mBlackArray = (String[]) blackList.toArray(new String[0]);
            this.mBlackListLoaded = true;
        }
        if (whiteList.size() > 0) {
            this.mWhiteArray = (String[]) whiteList.toArray(new String[0]);
            this.mWhiteListLoaded = true;
        }
        if (whiteListignorenomedia.size() > 0) {
            mWhiteListIgnoreNomediaArray = (String[]) whiteListignorenomedia.toArray(new String[0]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:72:0x0106 A:{Splitter: B:1:0x0003, PHI: r0 r5 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0106 A:{Splitter: B:1:0x0003, PHI: r0 r5 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x010c A:{SYNTHETIC, Splitter: B:76:0x010c} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x012b A:{SYNTHETIC, Splitter: B:90:0x012b} */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x013a A:{Splitter: B:5:0x003a, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0134 A:{Splitter: B:5:0x003a, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:30:?, code:
            android.util.Log.e(TAG, "Couldn't find MediaStorage_BlackWhitelist.xml.");
     */
    /* JADX WARNING: Missing block: B:31:0x0098, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            android.util.Log.e(TAG, "Couldn't find MediaStorage_BlackWhitelist.xml.");
     */
    /* JADX WARNING: Missing block: B:39:0x00b8, code:
            if (r6 != null) goto L_0x00ba;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:42:0x00bd, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:43:0x00be, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:44:0x00bf, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:72:0x0106, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:74:?, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:75:0x010a, code:
            if (r5 != null) goto L_0x010c;
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:78:0x0111, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:79:0x0112, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:80:0x0117, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:88:0x0128, code:
            r7 = th;
     */
    /* JADX WARNING: Missing block: B:89:0x0129, code:
            if (r5 != null) goto L_0x012b;
     */
    /* JADX WARNING: Missing block: B:91:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:92:0x012e, code:
            throw r7;
     */
    /* JADX WARNING: Missing block: B:93:0x012f, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:94:0x0130, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:95:0x0134, code:
            r7 = th;
     */
    /* JADX WARNING: Missing block: B:96:0x0135, code:
            r5 = r6;
     */
    /* JADX WARNING: Missing block: B:99:0x013a, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:100:0x013b, code:
            r5 = r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BlackWhiteList parseXmlToGetBlackWhiteList(Context context) {
        BlackWhiteList blackWhiteList = null;
        InputStream inputStream = null;
        try {
            InputStream inputStream2;
            File fileCurrent = new File(context.getFilesDir() + "/" + BLACK_WHITE_LIST_FILE);
            File fileUpate = new File("/data/bbkcore/MediaStorage_BlackWhitelist.xml");
            if (fileCurrent.exists()) {
                inputStream2 = new FileInputStream(fileCurrent);
            } else {
                Log.d(TAG, "current dir doesn't have  MediaStorage_BlackWhitelist.xml");
                inputStream2 = context.getAssets().open(BLACK_WHITE_LIST_FILE);
            }
            try {
                blackWhiteList = parseInputStreamToBlackWhiteList(inputStream2);
                if (blackWhiteList == null) {
                    Log.d(TAG, "parse fileCurrent fail, parse fileUpate instead");
                    if (fileUpate.exists()) {
                        inputStream = new FileInputStream(fileUpate);
                    } else {
                        Log.d(TAG, "bbkcore dir doesn't have MediaStorage_BlackWhitelist.xml");
                        inputStream = context.getAssets().open(BLACK_WHITE_LIST_FILE);
                    }
                    blackWhiteList = parseInputStreamToBlackWhiteList(inputStream);
                    if (blackWhiteList == null) {
                        Log.d(TAG, "no xml file available, parse assert xml instead");
                        inputStream = context.getAssets().open(BLACK_WHITE_LIST_FILE);
                        blackWhiteList = parseInputStreamToBlackWhiteList(inputStream);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return blackWhiteList;
                    }
                    Log.d(TAG, "parsed fileUpate!");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    return blackWhiteList;
                }
                Log.d(TAG, "parsed fileCurrent!");
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                return blackWhiteList;
            } catch (FileNotFoundException e3) {
                FileNotFoundException e4 = e3;
                inputStream = inputStream2;
                e4.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                return blackWhiteList;
            } catch (IOException e5) {
            } catch (Throwable th) {
            }
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "Couldn't find MediaStorage_BlackWhitelist.xml.");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            return blackWhiteList;
        } catch (IOException e7) {
        }
    }

    private BlackWhiteList parseInputStreamToBlackWhiteList(InputStream inputStream) {
        BlackWhiteList blackWhiteList = null;
        try {
            InputSource inputSource = new InputSource(new InputStreamReader(inputStream, "UTF-8"));
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            BlackWhiteParseHandler blackWhiteParseHandler = new BlackWhiteParseHandler();
            xmlReader.setContentHandler(blackWhiteParseHandler);
            xmlReader.parse(inputSource);
            return blackWhiteParseHandler.getBlackWhiteLists();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return blackWhiteList;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return blackWhiteList;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return blackWhiteList;
        } catch (IOException e4) {
            e4.printStackTrace();
            return blackWhiteList;
        }
    }

    private boolean isCurrentDirectoryHasWhiteListXml() {
        return new File(this.mContext.getFilesDir() + "/" + BLACK_WHITE_LIST_FILE).exists();
    }
}
