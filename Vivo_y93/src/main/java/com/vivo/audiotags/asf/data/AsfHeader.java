package com.vivo.audiotags.asf.data;

import java.math.BigInteger;
import java.util.Arrays;

public class AsfHeader extends Chunk {
    private final long chunkCount;
    private ContentDescription contentDescription;
    private EncodingChunk encodingChunk;
    private ExtendedContentDescription extendedContentDescription;
    private FileHeader fileHeader;
    private StreamBitratePropertiesChunk streamBitratePropertiesChunk;
    private StreamChunk[] streamChunks = new StreamChunk[0];
    private Chunk[] unspecifiedChunks = new Chunk[0];

    public AsfHeader(long pos, BigInteger chunkLen, long chunkCnt) {
        super(GUID.GUID_HEADER, pos, chunkLen);
        this.chunkCount = chunkCnt;
    }

    public void addStreamChunk(StreamChunk toAdd) {
        if (toAdd == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        } else if (!Arrays.asList(this.streamChunks).contains(toAdd)) {
            StreamChunk[] tmp = new StreamChunk[(this.streamChunks.length + 1)];
            System.arraycopy(this.streamChunks, 0, tmp, 0, this.streamChunks.length);
            tmp[tmp.length - 1] = toAdd;
            this.streamChunks = tmp;
        }
    }

    public void addUnspecifiedChunk(Chunk toAppend) {
        if (toAppend == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        } else if (!Arrays.asList(this.unspecifiedChunks).contains(toAppend)) {
            Chunk[] tmp = new Chunk[(this.unspecifiedChunks.length + 1)];
            System.arraycopy(this.unspecifiedChunks, 0, tmp, 0, this.unspecifiedChunks.length);
            tmp[tmp.length - 1] = toAppend;
            this.unspecifiedChunks = tmp;
        }
    }

    public AudioStreamChunk getAudioStreamChunk() {
        AudioStreamChunk result = null;
        for (int i = 0; i < getStreamChunkCount() && result == null; i++) {
            StreamChunk tmp = getStreamChunk(i);
            if (tmp instanceof AudioStreamChunk) {
                result = (AudioStreamChunk) tmp;
            }
        }
        return result;
    }

    public long getChunkCount() {
        return this.chunkCount;
    }

    public ContentDescription getContentDescription() {
        return this.contentDescription;
    }

    public EncodingChunk getEncodingChunk() {
        return this.encodingChunk;
    }

    public ExtendedContentDescription getExtendedContentDescription() {
        return this.extendedContentDescription;
    }

    public FileHeader getFileHeader() {
        return this.fileHeader;
    }

    public StreamBitratePropertiesChunk getStreamBitratePropertiesChunk() {
        return this.streamBitratePropertiesChunk;
    }

    public StreamChunk getStreamChunk(int index) {
        return this.streamChunks[index];
    }

    public int getStreamChunkCount() {
        return this.streamChunks.length;
    }

    public Chunk getUnspecifiedChunk(int index) {
        return this.unspecifiedChunks[index];
    }

    public int getUnspecifiedChunkCount() {
        return this.unspecifiedChunks.length;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, "\nASF Chunk\n");
        result.append("   Contains: \"" + getChunkCount() + "\" chunks\n");
        result.append(getFileHeader());
        result.append(getExtendedContentDescription());
        result.append(getEncodingChunk());
        result.append(getContentDescription());
        result.append(getStreamBitratePropertiesChunk());
        for (int i = 0; i < getStreamChunkCount(); i++) {
            result.append(getStreamChunk(i));
        }
        return result.toString();
    }

    public void setContentDescription(ContentDescription contentDesc) {
        this.contentDescription = contentDesc;
    }

    public void setEncodingChunk(EncodingChunk encChunk) {
        if (encChunk == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        }
        this.encodingChunk = encChunk;
    }

    public void setExtendedContentDescription(ExtendedContentDescription th) {
        this.extendedContentDescription = th;
    }

    public void setFileHeader(FileHeader fh) {
        if (fh == null) {
            throw new IllegalArgumentException("Argument must not be null.");
        }
        this.fileHeader = fh;
    }

    public void setStreamBitratePropertiesChunk(StreamBitratePropertiesChunk streamBitratePropertiesChunk1) {
        this.streamBitratePropertiesChunk = streamBitratePropertiesChunk1;
    }
}
