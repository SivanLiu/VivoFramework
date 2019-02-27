package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import com.vivo.media.FeatureManager;
import java.util.Arrays;

public class GUID {
    public static final GUID GUID_AUDIOSTREAM = new GUID(new int[]{64, 158, 105, 248, 77, 91, 207, 17, 168, 253, 0, 128, 95, 92, 68, 43}, " Audio stream");
    public static final GUID GUID_AUDIO_ERROR_CONCEALEMENT_ABSENT = new GUID(new int[]{64, 164, 241, 73, 206, 78, 208, 17, 163, 172, 0, 160, FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK, 3, 72, 246}, "Audio error concealment absent.");
    public static final GUID GUID_AUDIO_ERROR_CONCEALEMENT_INTERLEAVED = new GUID(new int[]{64, 164, 241, 73, 206, 78, 208, 17, 163, 172, 0, 160, FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK, 3, 72, 246}, "Interleaved audio error concealment.");
    public static final GUID GUID_CONTENTDESCRIPTION = new GUID(new int[]{51, 38, 178, 117, 142, 102, 207, 17, 166, 217, 0, 170, 0, 98, 206, 108}, "Content Description");
    public static final GUID GUID_ENCODING = new GUID(new int[]{64, 82, 209, 134, 29, 49, 208, 17, 163, 164, 0, 160, FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK, 3, 72, 246}, "Encoding description");
    public static final GUID GUID_EXTENDED_CONTENT_DESCRIPTION = new GUID(new int[]{64, 164, 208, 210, 7, 227, 210, 17, 151, 240, 0, 160, FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK, 94, 168, 80}, "Extended Content Description");
    public static final GUID GUID_FILE = new GUID(new int[]{161, 220, 171, 140, 71, 169, 207, 17, 142, 228, 0, 192, 12, 32, 83, 101}, "File header");
    public static final GUID GUID_HEADER = new GUID(new int[]{48, 38, 178, 117, 142, 102, 207, 17, 166, 217, 0, 170, 0, 98, 206, 108}, "Asf header");
    public static final GUID GUID_HEADER_EXTENSION = new GUID(new int[]{181, 3, 191, 95, 46, 169, 207, 17, 142, 227, 0, 192, 12, 32, 83, 101}, "Header Extension");
    public static final int GUID_LENGTH = 16;
    public static final GUID GUID_STREAM = new GUID(new int[]{145, 7, 220, 183, 183, 169, 207, 17, 142, 230, 0, 192, 12, 32, 83, 101}, "Stream");
    public static final GUID GUID_STREAM_BITRATE_PROPERTIES = new GUID(new int[]{206, 117, 248, 123, 141, 70, 209, 17, 141, 130, 0, 96, 151, FeatureManager.FEATURE_NOTIFY_PLAY_CALLBACK, 162, 178}, "Stream bitrate properties");
    public static final GUID GUID_VIDEOSTREAM = new GUID(new int[]{192, 239, 25, 188, 77, 91, 207, 17, 168, 253, 0, 128, 95, 92, 68, 43}, "Video stream");
    public static final GUID[] KNOWN_GUIDS = new GUID[]{GUID_AUDIO_ERROR_CONCEALEMENT_ABSENT, GUID_AUDIO_ERROR_CONCEALEMENT_INTERLEAVED, GUID_CONTENTDESCRIPTION, GUID_AUDIOSTREAM, GUID_ENCODING, GUID_FILE, GUID_HEADER, GUID_STREAM, GUID_EXTENDED_CONTENT_DESCRIPTION, GUID_VIDEOSTREAM, GUID_HEADER_EXTENSION, GUID_STREAM_BITRATE_PROPERTIES};
    private String description;
    private int[] guid;

    public static boolean assertGUID(int[] value) {
        if (value == null || value.length != 16) {
            return false;
        }
        return true;
    }

    public static String getGuidDescription(GUID guid) {
        String result = null;
        if (guid == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        }
        for (int i = 0; i < KNOWN_GUIDS.length; i++) {
            if (KNOWN_GUIDS[i].equals(guid)) {
                result = KNOWN_GUIDS[i].getDescription();
            }
        }
        return result;
    }

    public GUID(int[] value) {
        this.description = "";
        this.guid = null;
        setGUID(value);
    }

    public GUID(int[] value, String desc) {
        this(value);
        if (desc == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        }
        this.description = desc;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GUID)) {
            return super.equals(obj);
        }
        return Arrays.equals(getGUID(), ((GUID) obj).getGUID());
    }

    public byte[] getBytes() {
        byte[] result = new byte[this.guid.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (this.guid[i] & 255);
        }
        return result;
    }

    public String getDescription() {
        return this.description;
    }

    public int[] getGUID() {
        int[] copy = new int[this.guid.length];
        System.arraycopy(this.guid, 0, copy, 0, this.guid.length);
        return copy;
    }

    public boolean isValid() {
        return assertGUID(getGUID());
    }

    private void setGUID(int[] value) {
        if (assertGUID(value)) {
            this.guid = new int[16];
            System.arraycopy(value, 0, this.guid, 0, 16);
            return;
        }
        throw new IllegalArgumentException("The given guid doesn't match the GUID specification.");
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        if (getDescription().trim().length() > 0) {
            result.append("Description: " + getDescription() + Utils.LINE_SEPARATOR + "   ");
        }
        for (int i = 0; i < this.guid.length; i++) {
            String tmp = Integer.toHexString(this.guid[i]);
            if (tmp.length() < 2) {
                tmp = "0" + tmp;
            }
            if (i > 0) {
                result.append(", ");
            }
            result.append("0x" + tmp);
        }
        return result.toString();
    }
}
