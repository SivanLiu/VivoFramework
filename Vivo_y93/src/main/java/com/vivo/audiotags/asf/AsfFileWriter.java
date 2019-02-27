package com.vivo.audiotags.asf;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.asf.data.AsfHeader;
import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.ContentDescription;
import com.vivo.audiotags.asf.data.ContentDescriptor;
import com.vivo.audiotags.asf.data.ExtendedContentDescription;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.io.AsfHeaderReader;
import com.vivo.audiotags.asf.util.ChunkPositionComparator;
import com.vivo.audiotags.asf.util.TagConverter;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.generic.GenericTag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class AsfFileWriter extends AudioFileWriter {
    private void copy(RandomAccessFile source, RandomAccessFile destination, long number) throws IOException {
        byte[] buffer = new byte[8192];
        long bytesCopied = 0;
        while (true) {
            int i;
            if (8192 + bytesCopied > number) {
                i = (int) (number - bytesCopied);
            } else {
                i = 8192;
            }
            int read = source.read(buffer, 0, i);
            if (read > 0) {
                bytesCopied += (long) read;
                destination.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    private void createModifiedCopy(Tag tag, AsfHeader header, RandomAccessFile raf, RandomAccessFile rafTemp, boolean ignoreDescriptions) throws IOException {
        raf.seek(0);
        copy(raf, rafTemp, 30);
        Chunk[] chunks = getOrderedChunks(header);
        long fileSizeDifference = 0;
        long chunkCount = header.getChunkCount();
        long newFileHeaderPos = -1;
        if (header.getExtendedContentDescription() == null && isExtendedContentDescriptionMandatory(tag) && (ignoreDescriptions ^ 1) != 0) {
            chunkCount++;
            fileSizeDifference = 0 + createNewExtendedContentDescription(tag, null, rafTemp).getChunkLength().longValue();
        }
        if (header.getContentDescription() == null && isContentdescriptionMandatory(tag) && (ignoreDescriptions ^ 1) != 0) {
            chunkCount++;
            fileSizeDifference += createNewContentDescription(tag, null, rafTemp).getChunkLength().longValue();
        }
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] == header.getContentDescription()) {
                if (ignoreDescriptions) {
                    chunkCount--;
                    fileSizeDifference -= header.getContentDescription().getChunkLength().longValue();
                } else {
                    fileSizeDifference += createNewContentDescription(tag, header.getContentDescription(), rafTemp).getChunkLength().subtract(header.getContentDescription().getChunkLength()).longValue();
                }
            } else if (chunks[i] != header.getExtendedContentDescription()) {
                if (GUID.GUID_FILE.equals(chunks[i].getGuid())) {
                    newFileHeaderPos = rafTemp.getFilePointer();
                }
                raf.seek(chunks[i].getPosition());
                copy(raf, rafTemp, chunks[i].getChunkLength().longValue());
            } else if (deleteExtendedContentDescription(header.getExtendedContentDescription(), tag) || ignoreDescriptions) {
                chunkCount--;
                fileSizeDifference -= header.getExtendedContentDescription().getChunkLength().longValue();
            } else {
                fileSizeDifference += createNewExtendedContentDescription(tag, header.getExtendedContentDescription(), rafTemp).getChunkLength().subtract(header.getExtendedContentDescription().getChunkLength()).longValue();
            }
        }
        raf.seek(header.getChunckEnd());
        copy(raf, rafTemp, raf.length() - raf.getFilePointer());
        rafTemp.seek(24);
        write16UINT(chunkCount, rafTemp);
        rafTemp.seek(40 + newFileHeaderPos);
        write32UINT(header.getFileHeader().getFileSize().longValue() + fileSizeDifference, rafTemp);
        rafTemp.seek(16);
        write32UINT(header.getChunkLength().longValue() + fileSizeDifference, rafTemp);
    }

    private Chunk createNewContentDescription(Tag tag, ContentDescription contentDescription, RandomAccessFile rafTemp) throws IOException {
        long chunkStart = rafTemp.getFilePointer();
        ContentDescription description = TagConverter.createContentDescription(tag);
        if (contentDescription != null) {
            description.setRating(contentDescription.getRating());
        }
        byte[] asfContent = description.getBytes();
        rafTemp.write(asfContent);
        return new Chunk(GUID.GUID_CONTENTDESCRIPTION, chunkStart, BigInteger.valueOf((long) asfContent.length));
    }

    private Chunk createNewExtendedContentDescription(Tag tag, ExtendedContentDescription tagChunk, RandomAccessFile rafTemp) throws IOException {
        long chunkStart = rafTemp.getFilePointer();
        if (tagChunk == null) {
            tagChunk = new ExtendedContentDescription();
        }
        TagConverter.assignCommonTagValues(tag, tagChunk);
        TagConverter.assignOptionalTagValues(tag, tagChunk);
        byte[] asfBytes = tagChunk.getBytes();
        rafTemp.write(asfBytes);
        return new Chunk(GUID.GUID_EXTENDED_CONTENT_DESCRIPTION, chunkStart, BigInteger.valueOf((long) asfBytes.length));
    }

    private boolean deleteExtendedContentDescription(ExtendedContentDescription tagHeader, Tag tag) {
        HashSet ignoreDescriptors = new HashSet(Arrays.asList(new String[]{ContentDescriptor.ID_GENRE, ContentDescriptor.ID_GENREID, ContentDescriptor.ID_TRACKNUMBER, ContentDescriptor.ID_ALBUM, ContentDescriptor.ID_YEAR}));
        Iterator it = tagHeader.getDescriptors().iterator();
        boolean found = false;
        while (it.hasNext() && (found ^ 1) != 0) {
            found = ignoreDescriptors.contains(((ContentDescriptor) it.next()).getName()) ^ 1;
        }
        if (found) {
            return false;
        }
        return isExtendedContentDescriptionMandatory(tag) ^ 1;
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        try {
            AsfHeader header = AsfHeaderReader.readHeader(raf);
            if (header == null) {
                throw new NullPointerException("Header is null, so file couldn't be read properly. (Interpretation of data, not file access rights.)");
            }
            createModifiedCopy(new GenericTag(), header, raf, tempRaf, true);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception cre) {
            throw new CannotWriteException("Cannot modify tag because exception occured:\n   " + cre.getMessage());
        }
    }

    private Chunk[] getOrderedChunks(AsfHeader header) {
        int i;
        ArrayList result = new ArrayList();
        for (i = 0; i < header.getUnspecifiedChunkCount(); i++) {
            result.add(header.getUnspecifiedChunk(i));
        }
        for (i = 0; i < header.getStreamChunkCount(); i++) {
            result.add(header.getStreamChunk(i));
        }
        if (header.getContentDescription() != null) {
            result.add(header.getContentDescription());
        }
        result.add(header.getFileHeader());
        if (header.getExtendedContentDescription() != null) {
            result.add(header.getExtendedContentDescription());
        }
        if (header.getEncodingChunk() != null) {
            result.add(header.getEncodingChunk());
        }
        if (header.getStreamBitratePropertiesChunk() != null) {
            result.add(header.getStreamBitratePropertiesChunk());
        }
        Chunk[] tmp = (Chunk[]) result.toArray(new Chunk[result.size()]);
        Arrays.sort(tmp, new ChunkPositionComparator());
        return tmp;
    }

    private boolean isContentdescriptionMandatory(Tag tag) {
        if (tag.getFirstArtist().trim().length() > 0 || tag.getFirstComment().trim().length() > 0 || tag.getFirstTitle().trim().length() > 0) {
            return true;
        }
        return false;
    }

    private boolean isExtendedContentDescriptionMandatory(Tag tag) {
        if (tag.getFirstTrack().trim().length() > 0 || tag.getFirstYear().trim().length() > 0 || tag.getFirstGenre().trim().length() > 0 || tag.getFirstAlbum().trim().length() > 0) {
            return true;
        }
        return false;
    }

    private int write16UINT(long value, RandomAccessFile raf) throws IOException {
        raf.write(((int) value) & 255);
        raf.write(((int) (value >> 8)) & 255);
        return 2;
    }

    private int write32UINT(long value, RandomAccessFile raf) throws IOException {
        raf.write(((int) value) & 255);
        value >>= 8;
        raf.write(((int) value) & 255);
        value >>= 8;
        raf.write(((int) value) & 255);
        raf.write(((int) (value >> 8)) & 255);
        return 4;
    }

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        try {
            AsfHeader header = AsfHeaderReader.readHeader(raf);
            if (header == null) {
                throw new NullPointerException("Header is null, so file couldn't be read properly. (Interpretation of data, not file access rights.)");
            }
            createModifiedCopy(tag, header, raf, rafTemp, false);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception cre) {
            throw new CannotWriteException("Cannot modify tag because exception occured:\n   " + cre.getMessage());
        }
    }
}
