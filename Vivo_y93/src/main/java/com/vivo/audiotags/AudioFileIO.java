package com.vivo.audiotags;

import com.vivo.audiotags.ape.MonkeyFileReader;
import com.vivo.audiotags.ape.MonkeyFileWriter;
import com.vivo.audiotags.asf.AsfFileReader;
import com.vivo.audiotags.asf.AsfFileWriter;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.flac.FlacFileReader;
import com.vivo.audiotags.flac.FlacFileWriter;
import com.vivo.audiotags.generic.AudioFileModificationListener;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.generic.ModificationHandler;
import com.vivo.audiotags.generic.Utils;
import com.vivo.audiotags.mp3.Mp3FileReader;
import com.vivo.audiotags.mp3.Mp3FileWriter;
import com.vivo.audiotags.mpc.MpcFileReader;
import com.vivo.audiotags.mpc.MpcFileWriter;
import com.vivo.audiotags.ogg.OggFileReader;
import com.vivo.audiotags.ogg.OggFileWriter;
import com.vivo.audiotags.wav.WavFileReader;
import com.vivo.audiotags.wav.WavFileWriter;
import java.io.File;
import java.util.Hashtable;

public class AudioFileIO {
    private static AudioFileIO defaultInstance;
    private final ModificationHandler modificationHandler = new ModificationHandler();
    private Hashtable readers = new Hashtable();
    private Hashtable writers = new Hashtable();

    public static void delete(AudioFile f) throws CannotWriteException {
        getDefaultAudioFileIO().deleteTag(f);
    }

    public static AudioFileIO getDefaultAudioFileIO() {
        if (defaultInstance == null) {
            defaultInstance = new AudioFileIO();
        }
        return defaultInstance;
    }

    public static AudioFile read(File f) throws CannotReadException {
        return getDefaultAudioFileIO().readFile(f);
    }

    public static void write(AudioFile f) throws CannotWriteException {
        getDefaultAudioFileIO().writeFile(f);
    }

    public AudioFileIO() {
        prepareReadersAndWriters();
    }

    public void addAudioFileModificationListener(AudioFileModificationListener listener) {
        this.modificationHandler.addAudioFileModificationListener(listener);
    }

    public void deleteTag(AudioFile f) throws CannotWriteException {
        String ext = Utils.getExtension(f);
        Object afw = this.writers.get(ext);
        if (afw == null) {
            throw new CannotWriteException("No Deleter associated to this extension: " + ext);
        }
        ((AudioFileWriter) afw).delete(f);
    }

    private void prepareReadersAndWriters() {
        this.readers.put("mp3", new Mp3FileReader());
        this.readers.put("ogg", new OggFileReader());
        this.readers.put("flac", new FlacFileReader());
        this.readers.put("wav", new WavFileReader());
        this.readers.put("mpc", new MpcFileReader());
        this.readers.put("mp+", this.readers.get("mpc"));
        this.readers.put("ape", new MonkeyFileReader());
        this.readers.put("wma", new AsfFileReader());
        this.writers.put("mp3", new Mp3FileWriter());
        this.writers.put("ogg", new OggFileWriter());
        this.writers.put("flac", new FlacFileWriter());
        this.writers.put("wav", new WavFileWriter());
        this.writers.put("mpc", new MpcFileWriter());
        this.writers.put("mp+", this.writers.get("mpc"));
        this.writers.put("ape", new MonkeyFileWriter());
        this.writers.put("wma", new AsfFileWriter());
        for (AudioFileWriter curr : this.writers.values()) {
            curr.setAudioFileModificationListener(this.modificationHandler);
        }
    }

    public AudioFile readFile(File f) throws CannotReadException {
        String ext = Utils.getExtension(f);
        Object afr = this.readers.get(ext);
        if (afr != null) {
            return ((AudioFileReader) afr).read(f);
        }
        throw new CannotReadException("No Reader associated to this extension: " + ext);
    }

    public void removeAudioFileModificationListener(AudioFileModificationListener listener) {
        this.modificationHandler.removeAudioFileModificationListener(listener);
    }

    public void writeFile(AudioFile f) throws CannotWriteException {
        String ext = Utils.getExtension(f);
        Object afw = this.writers.get(ext);
        if (afw == null) {
            throw new CannotWriteException("No Writer associated to this extension: " + ext);
        }
        ((AudioFileWriter) afw).write(f);
    }
}
