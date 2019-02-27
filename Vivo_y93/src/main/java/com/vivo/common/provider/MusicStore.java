package com.vivo.common.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.net.Uri;
import android.provider.BaseColumns;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public final class MusicStore {
    public static final String AUTHORITY = "media";
    private static final String CONTENT_AUTHORITY_SLASH = "content://media/";

    public interface BucketColumns {
        public static final String BUCKET_KEY = "bucket_key";
        public static final String BUCKET_NAME = "bucket_display_name";
        public static final String BUCKET_PATH = "_data";
        public static final String BUKECT_ID = "_id";
        public static final String NUM_SONGS = "numsongs";
    }

    public static final class Bucket implements BaseColumns, BucketColumns {
        public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
        public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

        public static Uri getContentUri(String volumeName) {
            return Uri.parse(MusicStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/bucket");
        }

        public static final Uri getContentUri(String volumeName, long bucketId) {
            return Uri.parse(MusicStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/bucket/" + bucketId);
        }
    }

    public interface PlayCountColumns {
        public static final String AUDIO_ID = "audio_id";
        public static final String NUMBER_PLAY = "count(audio_id)";
        public static final String PLAY_DATE = "date_played";
    }

    public static final class PlayCounts implements BaseColumns, PlayCountColumns {
        public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
        public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

        public static Uri getContentUri(String volumeName) {
            return Uri.parse(MusicStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playcount");
        }

        public static final Uri getContentUri(String volumeName, long audioId) {
            return Uri.parse(MusicStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playcount/" + audioId);
        }
    }
}
