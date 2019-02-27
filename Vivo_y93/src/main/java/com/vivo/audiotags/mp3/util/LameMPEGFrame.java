package com.vivo.audiotags.mp3.util;

public class LameMPEGFrame {
    private int bitrate;
    private boolean containsLameFrame = false;
    private boolean containsLameMPEGFrame;
    private int fileSize;
    private boolean isValidLameMPEGFrame = false;
    private String lameVersion;

    public LameMPEGFrame(byte[] lameHeader) {
        if (new String(lameHeader, 0, 4).equals("LAME")) {
            boolean z;
            this.isValidLameMPEGFrame = true;
            int[] b = u(lameHeader);
            if ((b[9] & 255) == 255) {
                z = true;
            } else {
                z = false;
            }
            this.containsLameFrame = z;
            this.lameVersion = new String(new byte[]{lameHeader[4], lameHeader[5], lameHeader[6], lameHeader[7], lameHeader[8]});
            this.containsLameMPEGFrame = containsLameMPEGFrame();
            if (this.containsLameMPEGFrame) {
                this.bitrate = b[20];
                this.fileSize = (((b[28] * 16777215) + (b[29] * 65535)) + (b[30] * 255)) + b[31];
                return;
            }
            return;
        }
        this.isValidLameMPEGFrame = false;
    }

    private int[] u(byte[] b) {
        int[] i = new int[b.length];
        for (int j = 0; j < i.length; j++) {
            i[j] = b[j] & 255;
        }
        return i;
    }

    public boolean isValid() {
        return this.isValidLameMPEGFrame;
    }

    public String toString() {
        if (!this.isValidLameMPEGFrame) {
            return "\n!!!No Valid Lame MPEG Frame!!!\n";
        }
        String output = "\n----LameMPEGFrame--------------------\n" + "Lame" + this.lameVersion;
        if (this.containsLameMPEGFrame) {
            output = output + "\tMin.Bitrate:" + this.bitrate + "\tLength:" + this.fileSize;
        }
        return output + "\n--------------------------------\n";
    }

    private boolean containsLameMPEGFrame() {
        return this.containsLameFrame;
    }
}
