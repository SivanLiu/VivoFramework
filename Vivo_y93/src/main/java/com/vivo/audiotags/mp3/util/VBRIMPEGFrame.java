package com.vivo.audiotags.mp3.util;

public class VBRIMPEGFrame implements VbrInfoFrame {
    private int fileSize = 0;
    private int frameCount = 0;
    private boolean isValidVBRIMPEGFrame = true;

    public VBRIMPEGFrame(byte[] bytes) {
        if (new String(bytes, 0, 4).equals("VBRI")) {
            this.fileSize = ((((bytes[10] << 24) & -16777216) | ((bytes[10] << 16) & 16711680)) | ((bytes[10] << 8) & 65280)) | (bytes[10] & 255);
            this.frameCount = ((((bytes[14] << 24) & -16777216) | ((bytes[14] << 16) & 16711680)) | ((bytes[14] << 8) & 65280)) | (bytes[14] & 255);
            return;
        }
        this.isValidVBRIMPEGFrame = false;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public boolean isValid() {
        return this.isValidVBRIMPEGFrame;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public String toString() {
        if (!this.isValidVBRIMPEGFrame) {
            return "\n!!!No Valid VBRI MPEG Frame!!!\n";
        }
        return ("\n----VBRIMPEGFrame--------------------\n" + "Frame count:" + this.frameCount + "\tFile Size:" + this.fileSize + "\n") + "--------------------------------\n";
    }

    public boolean isVbr() {
        return true;
    }
}
