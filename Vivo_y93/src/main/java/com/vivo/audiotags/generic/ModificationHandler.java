package com.vivo.audiotags.generic;

import com.vivo.audiotags.AudioFile;
import com.vivo.audiotags.exceptions.ModifyVetoException;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

public class ModificationHandler implements AudioFileModificationListener {
    private Vector listeners = new Vector();

    public void addAudioFileModificationListener(AudioFileModificationListener l) {
        if (!this.listeners.contains(l)) {
            this.listeners.add(l);
        }
    }

    public void fileModified(AudioFile original, File temporary) throws ModifyVetoException {
        Enumeration enumer = this.listeners.elements();
        while (enumer.hasMoreElements()) {
            AudioFileModificationListener current = (AudioFileModificationListener) enumer.nextElement();
            try {
                current.fileModified(original, temporary);
            } catch (ModifyVetoException e) {
                vetoThrown(current, original, e);
                throw e;
            }
        }
    }

    public void fileOperationFinished(File result) {
        Enumeration enumer = this.listeners.elements();
        while (enumer.hasMoreElements()) {
            ((AudioFileModificationListener) enumer.nextElement()).fileOperationFinished(result);
        }
    }

    public void fileWillBeModified(AudioFile file, boolean delete) throws ModifyVetoException {
        Enumeration enumer = this.listeners.elements();
        while (enumer.hasMoreElements()) {
            AudioFileModificationListener current = (AudioFileModificationListener) enumer.nextElement();
            try {
                current.fileWillBeModified(file, delete);
            } catch (ModifyVetoException e) {
                vetoThrown(current, file, e);
                throw e;
            }
        }
    }

    public void removeAudioFileModificationListener(AudioFileModificationListener l) {
        if (this.listeners.contains(l)) {
            this.listeners.remove(l);
        }
    }

    public void vetoThrown(AudioFileModificationListener cause, AudioFile original, ModifyVetoException veto) {
        Enumeration enumer = this.listeners.elements();
        while (enumer.hasMoreElements()) {
            ((AudioFileModificationListener) enumer.nextElement()).vetoThrown(cause, original, veto);
        }
    }
}
