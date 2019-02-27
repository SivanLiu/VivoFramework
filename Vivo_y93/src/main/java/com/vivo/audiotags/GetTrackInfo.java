package com.vivo.audiotags;

import android.util.Log;
import com.vivo.audiotags.ape.MonkeyFileReader;
import com.vivo.audiotags.asf.AsfFileReader;
import com.vivo.audiotags.flac.FlacFileReader;
import com.vivo.audiotags.generic.Utils;
import com.vivo.audiotags.mp3.Mp3FileReader;
import com.vivo.audiotags.mpc.MpcFileReader;
import com.vivo.audiotags.ogg.OggFileReader;
import java.io.File;

public class GetTrackInfo {
    private static int APE = 5;
    private static int ASF = 7;
    private static int FLAC = 2;
    private static int MP3 = 1;
    private static int MPC = 4;
    private static int OGG = 3;
    private static int WAV = 6;
    private static int audioLength = 0;
    private static int fileType = 0;
    private final String LOGTAG = "GetTrackInfo";
    AudioFile audiofile = null;
    private Tag tag = null;

    public void getAudioFileTags(String filePath, String mimeType) {
        File file = new File(filePath);
        fileType = checkAudioFileType(file, mimeType);
        Log.d("hu", "sunrain file path  -->" + filePath + " ,mimeType-->" + mimeType);
        this.tag = null;
        audioLength = 0;
        try {
            if (fileType == MP3) {
                this.audiofile = new Mp3FileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            } else if (fileType == FLAC) {
                this.audiofile = new FlacFileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            } else if (fileType == OGG) {
                this.audiofile = new OggFileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            } else if (fileType == MPC) {
                this.audiofile = new MpcFileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            } else if (fileType == APE) {
                this.audiofile = new MonkeyFileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            } else if (fileType == ASF) {
                this.audiofile = new AsfFileReader().read(file);
                this.tag = this.audiofile.getTag();
                audioLength = this.audiofile.getLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkAudioFileType(File file, String mimeType) {
        Utils utils = new Utils();
        String ext = Utils.getExtension(file);
        if ("audio/mpeg".equalsIgnoreCase(mimeType) && ("mp2".equalsIgnoreCase(ext) ^ 1) != 0) {
            return MP3;
        }
        if ("audio/flac".equalsIgnoreCase(mimeType)) {
            return FLAC;
        }
        if ("audio/ogg".equalsIgnoreCase(mimeType) || "audio/vorbis".equalsIgnoreCase(mimeType)) {
            return OGG;
        }
        if ("audio/ape".equalsIgnoreCase(mimeType)) {
            return APE;
        }
        if ("audio/x-ms-wma".equalsIgnoreCase(mimeType)) {
            return ASF;
        }
        return 0;
    }

    public String getAudioArtist() {
        if (this.tag != null) {
            return this.tag.getFirstArtist();
        }
        return null;
    }

    public String getAudioAlbum() {
        if (this.tag != null) {
            return this.tag.getFirstAlbum();
        }
        return null;
    }

    public String getAudioTitle() {
        if (this.tag != null) {
            return this.tag.getFirstTitle();
        }
        return null;
    }

    public String getAudioComment() {
        if (this.tag != null) {
            return this.tag.getFirstComment();
        }
        return null;
    }

    public String getAudioGenre() {
        if (this.tag != null) {
            return this.tag.getFirstGenre();
        }
        return null;
    }

    public String getAudioTrack() {
        if (this.tag != null) {
            return this.tag.getFirstTrack();
        }
        return null;
    }

    public String getAudioYear() {
        if (this.tag != null) {
            return this.tag.getFirstYear();
        }
        return null;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public void showAudioTag(Tag tag) {
        if (tag != null) {
            Log.d("hu", "sunrain getFirstAlbum -->" + tag.getFirstAlbum());
            Log.d("hu", "sunrain getFirstArtist -->" + tag.getFirstArtist());
            Log.d("hu", "sunrain getFirstComment -->" + tag.getFirstComment());
            Log.d("hu", "sunrain getFirstGenre -->" + tag.getFirstGenre());
            Log.d("hu", "sunrain getFirstTitle -->" + tag.getFirstTitle());
            Log.d("hu", "sunrain getFirstTrack -->" + tag.getFirstTrack());
            Log.d("hu", "sunrain getFirstYear -->" + tag.getFirstYear());
            Log.d("hu", "xxxx  sunrain get duration  -->" + getAudioLength());
        }
    }
}
