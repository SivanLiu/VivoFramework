package com.vivo.audiotags.generic;

import com.vivo.audiotags.AudioFile;
import com.vivo.audiotags.exceptions.ModifyVetoException;
import java.io.File;

public class AudioFileModificationAdapter implements AudioFileModificationListener {
    public void fileModified(AudioFile original, File temporary) throws ModifyVetoException {
    }

    public void fileOperationFinished(File result) {
    }

    public void fileWillBeModified(AudioFile file, boolean delete) throws ModifyVetoException {
    }

    public void vetoThrown(AudioFileModificationListener cause, AudioFile original, ModifyVetoException veto) {
    }
}
