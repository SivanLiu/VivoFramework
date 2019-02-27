package com.vivo.audiotags.mp3.util;

public class XingMPEGFrame implements VbrInfoFrame {
    private int fileSize = 0;
    private int frameCount = 0;
    private boolean isValidXingMPEGFrame = true;
    private int quality;
    private boolean vbr = false;
    private boolean[] vbrFlags = new boolean[4];

    public XingMPEGFrame(byte[] bytesPart1, byte[] bytesPart2) {
        String xing = new String(bytesPart1, 0, 4);
        if (xing.equals("Xing") || xing.equals("Info")) {
            this.vbr = xing.equals("Xing");
            int[] b = u(bytesPart1);
            int[] q = u(bytesPart2);
            updateVBRFlags(b[7]);
            if (this.vbrFlags[0]) {
                this.frameCount = (((b[8] * 16777215) + (b[9] * 65535)) + (b[10] * 255)) + b[11];
            }
            if (this.vbrFlags[1]) {
                this.fileSize = (((b[12] * 16777215) + (b[13] * 65535)) + (b[14] * 255)) + b[15];
            }
            if (this.vbrFlags[3]) {
                this.quality = (((q[0] * 16777215) + (q[1] * 65535)) + (q[2] * 255)) + q[3];
                return;
            }
            return;
        }
        this.isValidXingMPEGFrame = false;
    }

    private int[] u(byte[] b) {
        int[] i = new int[b.length];
        for (int j = 0; j < i.length; j++) {
            i[j] = b[j] & 255;
        }
        return i;
    }

    public int getFrameCount() {
        if (this.vbrFlags[0]) {
            return this.frameCount;
        }
        return -1;
    }

    public boolean isValid() {
        return this.isValidXingMPEGFrame;
    }

    public boolean isVbr() {
        return this.vbr;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public String toString() {
        if (!this.isValidXingMPEGFrame) {
            return "\n!!!No Valid Xing MPEG Frame!!!\n";
        }
        return (("\n----XingMPEGFrame--------------------\n" + "Frame count:" + this.vbrFlags[0] + "\tFile Size:" + this.vbrFlags[1] + "\tQuality:" + this.vbrFlags[3] + "\n") + "Frame count:" + this.frameCount + "\tFile Size:" + this.fileSize + "\tQuality:" + this.quality + "\n") + "--------------------------------\n";
    }

    private void updateVBRFlags(int b) {
        boolean z;
        boolean z2 = true;
        boolean[] zArr = this.vbrFlags;
        if ((b & 1) == 1) {
            z = true;
        } else {
            z = false;
        }
        zArr[0] = z;
        zArr = this.vbrFlags;
        if ((b & 2) == 2) {
            z = true;
        } else {
            z = false;
        }
        zArr[1] = z;
        zArr = this.vbrFlags;
        if ((b & 4) == 4) {
            z = true;
        } else {
            z = false;
        }
        zArr[2] = z;
        boolean[] zArr2 = this.vbrFlags;
        if ((b & 8) != 8) {
            z2 = false;
        }
        zArr2[3] = z2;
    }
}
