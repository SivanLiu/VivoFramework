package com.vivo.audiotags.ogg.util;

public class OggPageHeader {
    private double absoluteGranulePosition;
    private byte[] checksum;
    private byte headerTypeFlag;
    private boolean isValid = false;
    private int pageLength = 0;
    private int pageSequenceNumber;
    private byte[] segmentTable;
    private int streamSerialNumber;

    public OggPageHeader(byte[] b) {
        int streamStructureRevision = b[4];
        this.headerTypeFlag = b[5];
        if (streamStructureRevision == 0) {
            int i;
            this.absoluteGranulePosition = 0.0d;
            for (i = 0; i < 8; i++) {
                this.absoluteGranulePosition += ((double) u(b[i + 6])) * Math.pow(2.0d, (double) (i * 8));
            }
            this.streamSerialNumber = ((u(b[14]) + (u(b[15]) << 8)) + (u(b[16]) << 16)) + (u(b[17]) << 24);
            this.pageSequenceNumber = ((u(b[18]) + (u(b[19]) << 8)) + (u(b[20]) << 16)) + (u(b[21]) << 24);
            this.checksum = new byte[]{b[22], b[23], b[24], b[25]};
            this.segmentTable = new byte[(b.length - 27)];
            for (i = 0; i < this.segmentTable.length; i++) {
                this.segmentTable[i] = b[i + 27];
                this.pageLength += u(b[i + 27]);
            }
            this.isValid = true;
        }
    }

    private int u(int i) {
        return i & 255;
    }

    public double getAbsoluteGranulePosition() {
        return this.absoluteGranulePosition;
    }

    public byte[] getCheckSum() {
        return this.checksum;
    }

    public byte getHeaderType() {
        return this.headerTypeFlag;
    }

    public int getPageLength() {
        return this.pageLength;
    }

    public int getPageSequence() {
        return this.pageSequenceNumber;
    }

    public int getSerialNumber() {
        return this.streamSerialNumber;
    }

    public byte[] getSegmentTable() {
        return this.segmentTable;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String toString() {
        return ("Ogg Page Header:\n" + "Is valid?: " + this.isValid + " | page length: " + this.pageLength + "\n") + "Header type: " + this.headerTypeFlag;
    }
}
