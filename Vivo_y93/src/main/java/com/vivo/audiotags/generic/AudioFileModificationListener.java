package com.vivo.audiotags.generic;

import com.vivo.audiotags.AudioFile;
import com.vivo.audiotags.exceptions.ModifyVetoException;
import java.io.File;

public interface AudioFileModificationListener {
    void fileModified(AudioFile audioFile, File file) throws ModifyVetoException;

    void fileOperationFinished(File file);

    void fileWillBeModified(AudioFile audioFile, boolean z) throws ModifyVetoException;

    void vetoThrown(AudioFileModificationListener audioFileModificationListener, AudioFile audioFile, ModifyVetoException modifyVetoException);
}
