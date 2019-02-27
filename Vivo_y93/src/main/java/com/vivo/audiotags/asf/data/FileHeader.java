package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;
import java.util.Date;

public class FileHeader extends Chunk {
    private final BigInteger duration;
    private final Date fileCreationTime;
    private BigInteger fileSize;
    private final long flags;
    private final long maxPackageSize;
    private final long minPackageSize;
    private final BigInteger packageCount;
    private final BigInteger timeEndPos;
    private final BigInteger timeStartPos;
    private final long uncompressedFrameSize;

    public FileHeader(long fileHeaderStart, BigInteger chunckLen, BigInteger size, BigInteger fileTime, BigInteger pkgCount, BigInteger dur, BigInteger timestampStart, BigInteger timestampEnd, long headerFlags, long minPkgSize, long maxPkgSize, long uncmpVideoFrameSize) {
        super(GUID.GUID_FILE, fileHeaderStart, chunckLen);
        this.fileSize = size;
        this.packageCount = pkgCount;
        this.duration = dur;
        this.timeStartPos = timestampStart;
        this.timeEndPos = timestampEnd;
        this.flags = headerFlags;
        this.minPackageSize = minPkgSize;
        this.maxPackageSize = maxPkgSize;
        this.uncompressedFrameSize = uncmpVideoFrameSize;
        this.fileCreationTime = Utils.getDateOf(fileTime).getTime();
    }

    public BigInteger getDuration() {
        return this.duration;
    }

    public int getDurationInSeconds() {
        return this.duration.divide(new BigInteger("10000000")).intValue();
    }

    public Date getFileCreationTime() {
        return this.fileCreationTime;
    }

    public BigInteger getFileSize() {
        return this.fileSize;
    }

    public long getFlags() {
        return this.flags;
    }

    public long getMaxPackageSize() {
        return this.maxPackageSize;
    }

    public long getMinPackageSize() {
        return this.minPackageSize;
    }

    public BigInteger getPackageCount() {
        return this.packageCount;
    }

    public float getPreciseDuration() {
        return (float) (getDuration().doubleValue() / 1.0E7d);
    }

    public BigInteger getTimeEndPos() {
        return this.timeEndPos;
    }

    public BigInteger getTimeStartPos() {
        return this.timeStartPos;
    }

    public long getUncompressedFrameSize() {
        return this.uncompressedFrameSize;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, "\nFileHeader\n");
        result.append("   Filesize      = " + getFileSize().toString() + " Bytes \n");
        result.append("   Media duration= " + getDuration().divide(new BigInteger("10000")).toString() + " ms \n");
        result.append("   Created at    = " + getFileCreationTime() + "\n");
        return result.toString();
    }
}
