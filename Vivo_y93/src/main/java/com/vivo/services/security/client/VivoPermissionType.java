package com.vivo.services.security.client;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS_PART)
public enum VivoPermissionType {
    SEND_SMS(0, VivoPermissionCategory.COMMUNICATION, VivoPermissionGroup.SMS),
    SEND_MMS(1, VivoPermissionCategory.COMMUNICATION, VivoPermissionGroup.CUSTOM),
    CALL_PHONE(2, VivoPermissionCategory.COMMUNICATION, VivoPermissionGroup.PHONE),
    MONITOR_CALL(3, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    READ_SMS(4, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.SMS),
    WRITE_SMS(5, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.CUSTOM),
    READ_MMS(6, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    WRITE_MMS(7, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    READ_CONTACTS(8, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.CONTACTS),
    WRITE_CONTACTS(9, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.CONTACTS),
    READ_CALL_LOG(10, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.PHONE),
    WRITE_CALL_LOG(11, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.PHONE),
    ACCESS_LOCATION(12, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.LOCATION),
    READ_PHONE_STATE(13, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.PHONE),
    CAMERA_IMAGE(14, VivoPermissionCategory.DEVICE, VivoPermissionGroup.CAMERA),
    CAMERA_VIDEO(15, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    RECORD_AUDIO(16, VivoPermissionCategory.DEVICE, VivoPermissionGroup.MICROPHONE),
    CHANGE_NETWORK_STATE(17, VivoPermissionCategory.DEVICE, VivoPermissionGroup.CUSTOM),
    CHANGE_WIFI_STATE(18, VivoPermissionCategory.DEVICE, VivoPermissionGroup.CUSTOM),
    BLUETOOTH(19, VivoPermissionCategory.DEVICE, VivoPermissionGroup.CUSTOM),
    NFC(20, VivoPermissionCategory.DEVICE, VivoPermissionGroup.CUSTOM),
    SEND_EMAIL(21, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    RW_FILE(22, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    INTERNET(23, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    THIRD_PHONE(24, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    SCREENSHOT(25, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    READ_CALENDAR(26, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.CALENDAR),
    WRITE_CALENDAR(27, VivoPermissionCategory.PRIVACY, VivoPermissionGroup.CALENDAR),
    READ_INTERNET_RECORDS(28, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    WHITE_INTERNET_RECORDS(29, VivoPermissionCategory.OTHERS, VivoPermissionGroup.CUSTOM),
    LAST(30, VivoPermissionCategory.LAST_CATEGORY, VivoPermissionGroup.CUSTOM);
    
    private static final HashMap<String, VivoPermissionType> sPermissionMap = null;
    private VivoPermissionCategory mCategory;
    private VivoPermissionGroup mGroup;
    private int mTypeId;

    @VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
    public static final class Constants {
        public static final String CAMERA_VIDEO = "android.permission.CAMERA_VIDEO";
        public static final String CUSTOM_READ_MMS = "android.permission.READ_MMS";
        public static final String CUSTOM_SEND_EMAIL = "android.permission.SEND_EMAIL";
        public static final String CUSTOM_SEND_MMS = "android.permission.SEND_MMS";
        public static final String CUSTOM_WRITE_MMS = "android.permission.WRITE_MMS";
        public static final int VALUE_ACCESS_LOCATION = 12;
        public static final int VALUE_BLUETOOTH = 19;
        public static final int VALUE_CALL_PHONE = 2;
        public static final int VALUE_CAMERA_IMAGE = 14;
        public static final int VALUE_CAMERA_VIDEO = 15;
        public static final int VALUE_CHANGE_NETWORK_STATE = 17;
        public static final int VALUE_CHANGE_WIFI_STATE = 18;
        public static final int VALUE_INTERNET = 23;
        public static final int VALUE_LAST = 30;
        public static final int VALUE_MONITOR_CALL = 3;
        public static final int VALUE_NFC = 20;
        public static final int VALUE_READ_CALENDAR = 26;
        public static final int VALUE_READ_CALL_LOG = 10;
        public static final int VALUE_READ_CONTACTS = 8;
        public static final int VALUE_READ_INTERNET_RECORDS = 28;
        public static final int VALUE_READ_MMS = 6;
        public static final int VALUE_READ_PHONE_STATE = 13;
        public static final int VALUE_READ_SMS = 4;
        public static final int VALUE_RECORD_AUDIO = 16;
        public static final int VALUE_RW_FILE = 22;
        public static final int VALUE_SCREENSHOT = 25;
        public static final int VALUE_SEND_EMAIL = 21;
        public static final int VALUE_SEND_MMS = 1;
        public static final int VALUE_SEND_SMS = 0;
        public static final int VALUE_THIRD_PHONE = 24;
        public static final int VALUE_WHITE_INTERNET_RECORDS = 29;
        public static final int VALUE_WRITE_CALENDAR = 27;
        public static final int VALUE_WRITE_CALL_LOG = 11;
        public static final int VALUE_WRITE_CONTACTS = 9;
        public static final int VALUE_WRITE_MMS = 7;
        public static final int VALUE_WRITE_SMS = 5;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
    public enum VivoPermissionCategory {
        private static final /* synthetic */ VivoPermissionCategory[] $VALUES = null;
        public static final VivoPermissionCategory COMMUNICATION = null;
        public static final VivoPermissionCategory DEVICE = null;
        public static final VivoPermissionCategory LAST_CATEGORY = null;
        public static final VivoPermissionCategory OTHERS = null;
        public static final VivoPermissionCategory PRIVACY = null;
        private int mValue;

        public static VivoPermissionCategory valueOf(String name) {
            return (VivoPermissionCategory) Enum.valueOf(VivoPermissionCategory.class, name);
        }

        public static VivoPermissionCategory[] values() {
            return $VALUES;
        }

        static {
            COMMUNICATION = new VivoPermissionCategory("COMMUNICATION", 0, 0);
            PRIVACY = new VivoPermissionCategory("PRIVACY", 1, 1);
            DEVICE = new VivoPermissionCategory("DEVICE", 2, 2);
            OTHERS = new VivoPermissionCategory("OTHERS", 3, 3);
            LAST_CATEGORY = new VivoPermissionCategory("LAST_CATEGORY", 4, 4);
            $VALUES = new VivoPermissionCategory[]{COMMUNICATION, PRIVACY, DEVICE, OTHERS, LAST_CATEGORY};
        }

        private VivoPermissionCategory(String str, int i, int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
    public enum VivoPermissionGroup {
        private static final /* synthetic */ VivoPermissionGroup[] $VALUES = null;
        public static final VivoPermissionGroup CALENDAR = null;
        public static final VivoPermissionGroup CAMERA = null;
        public static final VivoPermissionGroup CONTACTS = null;
        public static final VivoPermissionGroup CUSTOM = null;
        public static final VivoPermissionGroup LOCATION = null;
        public static final VivoPermissionGroup MICROPHONE = null;
        public static final VivoPermissionGroup PHONE = null;
        public static final VivoPermissionGroup SENSORS = null;
        public static final VivoPermissionGroup SMS = null;
        public static final VivoPermissionGroup STORAGE = null;
        private String mName;

        public static VivoPermissionGroup valueOf(String name) {
            return (VivoPermissionGroup) Enum.valueOf(VivoPermissionGroup.class, name);
        }

        public static VivoPermissionGroup[] values() {
            return $VALUES;
        }

        static {
            PHONE = new VivoPermissionGroup("PHONE", 0, "android.permission-group.PHONE");
            CONTACTS = new VivoPermissionGroup("CONTACTS", 1, "android.permission-group.CONTACTS");
            CALENDAR = new VivoPermissionGroup("CALENDAR", 2, "android.permission-group.CALENDAR");
            SMS = new VivoPermissionGroup("SMS", 3, "android.permission-group.SMS");
            STORAGE = new VivoPermissionGroup("STORAGE", 4, "android.permission-group.STORAGE");
            LOCATION = new VivoPermissionGroup("LOCATION", 5, "android.permission-group.LOCATION");
            MICROPHONE = new VivoPermissionGroup("MICROPHONE", 6, "android.permission-group.MICROPHONE");
            CAMERA = new VivoPermissionGroup("CAMERA", 7, "android.permission-group.CAMERA");
            SENSORS = new VivoPermissionGroup("SENSORS", 8, "android.permission-group.SENSORS");
            CUSTOM = new VivoPermissionGroup("CUSTOM", 9, "vivo.permission-group.CUSTOM");
            $VALUES = new VivoPermissionGroup[]{PHONE, CONTACTS, CALENDAR, SMS, STORAGE, LOCATION, MICROPHONE, CAMERA, SENSORS, CUSTOM};
        }

        private VivoPermissionGroup(String str, int i, String value) {
            this.mName = value;
        }

        public String getValue() {
            return this.mName;
        }
    }

    static {
        sPermissionMap = new HashMap();
        sPermissionMap.put("android.permission.SEND_SMS", SEND_SMS);
        sPermissionMap.put(Constants.CUSTOM_SEND_MMS, SEND_MMS);
        sPermissionMap.put("android.permission.CALL_PHONE", CALL_PHONE);
        sPermissionMap.put("android.permission.PROCESS_OUTGOING_CALLS", MONITOR_CALL);
        sPermissionMap.put("android.permission.READ_SMS", READ_SMS);
        sPermissionMap.put("android.permission.RECEIVE_SMS", READ_SMS);
        sPermissionMap.put("android.permission.WRITE_SMS", WRITE_SMS);
        sPermissionMap.put("android.permission.RECEIVE_MMS", READ_SMS);
        sPermissionMap.put("android.permission.RECEIVE_WAP_PUSH", READ_SMS);
        sPermissionMap.put(Constants.CUSTOM_READ_MMS, READ_SMS);
        sPermissionMap.put(Constants.CUSTOM_WRITE_MMS, WRITE_SMS);
        sPermissionMap.put("android.permission.READ_CONTACTS", READ_CONTACTS);
        sPermissionMap.put("android.permission.WRITE_CONTACTS", WRITE_CONTACTS);
        sPermissionMap.put("android.permission.READ_CALL_LOG", READ_CALL_LOG);
        sPermissionMap.put("android.permission.WRITE_CALL_LOG", WRITE_CALL_LOG);
        sPermissionMap.put("android.permission.ACCESS_FINE_LOCATION", ACCESS_LOCATION);
        sPermissionMap.put("android.permission.ACCESS_COARSE_LOCATION", ACCESS_LOCATION);
        sPermissionMap.put("android.permission.ACCESS_WIFI_STATE", ACCESS_LOCATION);
        sPermissionMap.put("android.permission.READ_PHONE_STATE", READ_PHONE_STATE);
        sPermissionMap.put("android.permission.CAMERA", CAMERA_IMAGE);
        sPermissionMap.put(Constants.CAMERA_VIDEO, CAMERA_IMAGE);
        sPermissionMap.put("android.permission.RECORD_AUDIO", RECORD_AUDIO);
        sPermissionMap.put(Constants.CUSTOM_SEND_EMAIL, SEND_EMAIL);
        sPermissionMap.put("android.permission.READ_EXTERNAL_STORAGE", RW_FILE);
        sPermissionMap.put("android.permission.WRITE_EXTERNAL_STORAGE", RW_FILE);
        sPermissionMap.put("android.permission.CHANGE_NETWORK_STATE", CHANGE_NETWORK_STATE);
        sPermissionMap.put("android.permission.CHANGE_WIFI_STATE", CHANGE_WIFI_STATE);
        sPermissionMap.put("android.permission.INTERNET", INTERNET);
        sPermissionMap.put("android.permission.NFC", NFC);
        sPermissionMap.put("android.permission.BLUETOOTH", BLUETOOTH);
        sPermissionMap.put("android.permission.READ_CALENDAR", READ_CALENDAR);
        sPermissionMap.put("android.permission.WRITE_CALENDAR", WRITE_CALENDAR);
    }

    private VivoPermissionType(int typeId, VivoPermissionCategory category) {
        this.mTypeId = typeId;
        this.mCategory = category;
    }

    private VivoPermissionType(int typeId, VivoPermissionCategory category, VivoPermissionGroup group) {
        this.mTypeId = typeId;
        this.mCategory = category;
        this.mGroup = group;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static VivoPermissionType getVPType(int id) {
        VivoPermissionType type = LAST;
        switch (id) {
            case 0:
                return SEND_SMS;
            case 1:
                return SEND_MMS;
            case 2:
                return CALL_PHONE;
            case 3:
                return MONITOR_CALL;
            case 4:
                return READ_SMS;
            case 5:
                return WRITE_SMS;
            case 6:
                return READ_MMS;
            case 7:
                return WRITE_MMS;
            case 8:
                return READ_CONTACTS;
            case 9:
                return WRITE_CONTACTS;
            case 10:
                return READ_CALL_LOG;
            case 11:
                return WRITE_CALL_LOG;
            case 12:
                return ACCESS_LOCATION;
            case 13:
                return READ_PHONE_STATE;
            case 14:
                return CAMERA_IMAGE;
            case 15:
                return CAMERA_VIDEO;
            case 16:
                return RECORD_AUDIO;
            case 17:
                return CHANGE_NETWORK_STATE;
            case 18:
                return CHANGE_WIFI_STATE;
            case 19:
                return BLUETOOTH;
            case 20:
                return NFC;
            case 21:
                return SEND_EMAIL;
            case 22:
                return RW_FILE;
            case 23:
                return INTERNET;
            case 24:
                return THIRD_PHONE;
            case 25:
                return SCREENSHOT;
            case 26:
                return READ_CALENDAR;
            case 27:
                return WRITE_CALENDAR;
            case 28:
                return READ_INTERNET_RECORDS;
            case 29:
                return WHITE_INTERNET_RECORDS;
            default:
                return LAST;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static VivoPermissionType getVPType(String key) {
        VivoPermissionType type = (VivoPermissionType) sPermissionMap.get(key);
        if (type == null) {
            VivoPermissionManager.printfDebug("Permission=" + key + ", return LAST.");
            return LAST;
        } else if (type != MONITOR_CALL || VivoPermissionManager.getOSVersion() < 3.0f) {
            return type;
        } else {
            return LAST;
        }
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionCategory getVPCategory() {
        if (VivoPermissionManager.getOSVersion() < 3.0f) {
            if (this.mTypeId == 26) {
                this.mCategory = VivoPermissionCategory.OTHERS;
            }
            if (this.mTypeId == 3) {
                this.mCategory = VivoPermissionCategory.PRIVACY;
            }
        }
        return this.mCategory;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public VivoPermissionGroup getVPGroup() {
        return this.mGroup;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public int getVPTypeId() {
        return this.mTypeId;
    }

    @VivoHook(hookType = VivoHookType.PUBLIC_API_METHOD)
    public static boolean isValidTypeId(int type) {
        if (type < 0 || type >= 30) {
            return false;
        }
        return true;
    }
}
