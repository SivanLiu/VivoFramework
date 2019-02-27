package com.vivo.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.provider.BaseColumns;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class VivoDownloads {
    public static final String COLUMN_DESTINATION_SUB_DIR = "destination_subdir";
    public static final int CONTROL_PAUSED = 1;
    public static final int CONTROL_RUN = 0;
    public static final int VISIBILITY_HIDDEN = 2;
    public static final int VISIBILITY_VISIBLE = 0;
    public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;

    public static final class Impl implements BaseColumns {
        public static final String COLUMN_DESTINATION_SUB_DIR = "destination_subdir";
        public static final String COLUMN_GROUP_POSITION = "position";
        public static final String COLUMN_NETWORK_CHANGED_PAUSED = "network_changed";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_DESCRIPTION = "OMA_Download_DDFileInfo_Description";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_NAME = "OMA_Download_DDFileInfo_Name";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_SIZE = "OMA_Download_DDFileInfo_Size";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_TYPE = "OMA_Download_DDFileInfo_Type";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_VENDOR = "OMA_Download_DDFileInfo_Vendor";
        public static final String COLUMN_OMA_DOWNLOAD_FLAG = "OMA_Download";
        public static final String COLUMN_OMA_DOWNLOAD_INSTALL_NOTIFY_URL = "OMA_Download_Install_Notify_Url";
        public static final String COLUMN_OMA_DOWNLOAD_NEXT_URL = "OMA_Download_Next_Url";
        public static final String COLUMN_OMA_DOWNLOAD_OBJECT_URL = "OMA_Download_Object_Url";
        public static final String COLUMN_OMA_DOWNLOAD_STATUS = "OMA_Download_Status";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_USERNAME = "username";
        public static final String CONTINUE_DOWNLOAD_WITH_SAME_FILENAME = "continue_download_with_same_filename";
        public static final String DOWNLOAD_PATH_SELECTED_FROM_FILEMANAGER = "download_path_selected_from_filemanager";
        public static final String DRM_RIGHT_VALID = "drm_right_valid";
        public static final boolean MTK_OMA_DOWNLOAD_SUPPORT = true;
        public static final String OMADL_OCCUR_ERROR_NEED_NOTIFY = "OMADL_ERROR_NEED_NOTIFY";
        public static final int OMADL_STATUS_DOWNLOAD_COMPLETELY = 200;
        public static final int OMADL_STATUS_ERROR_ALERTDIALOG_SHOWED = 599;
        public static final int OMADL_STATUS_ERROR_ATTRIBUTE_MISMATCH = 512;
        public static final int OMADL_STATUS_ERROR_INSTALL_FAILED = 400;
        public static final int OMADL_STATUS_ERROR_INSUFFICIENT_MEMORY = 403;
        public static final int OMADL_STATUS_ERROR_INVALID_DDVERSION = 515;
        public static final int OMADL_STATUS_ERROR_INVALID_DESCRIPTOR = 404;
        public static final int OMADL_STATUS_ERROR_NON_ACCEPTABLE_CONTENT = 492;
        public static final int OMADL_STATUS_ERROR_USER_CANCELLED = 490;
        public static final int OMADL_STATUS_ERROR_USER_DOWNLOAD_MEDIA_OBJECT = 491;
        public static final int OMADL_STATUS_HAS_NEXT_URL = 203;
        public static final int OMADL_STATUS_PARSE_DDFILE_SUCCESS = 201;
        public static final int STATUS_NEED_HTTP_AUTH = 401;
        public static final int STATUS_PAUSED_BY_MANUAL = 197;

        private Impl() {
        }

        public static boolean isStatusError(int status) {
            return android.provider.Downloads.Impl.isStatusError(status) || status == 198;
        }

        public static boolean isStatusCompleted(int status) {
            return android.provider.Downloads.Impl.isStatusCompleted(status) || status == 198;
        }
    }

    private VivoDownloads() {
    }
}
