package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.ContentDescription;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ContentDescriptionReader {
    public static ContentDescription read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_CONTENTDESCRIPTION.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new ContentDescriptionReader().parseData(raf);
        }
    }

    public static String readFixedSizeUTF16Str(RandomAccessFile raf, int strLen) throws IOException {
        byte[] strBytes = new byte[strLen];
        if (raf.read(strBytes) == strBytes.length) {
            if (strBytes.length >= 2 && strBytes[strBytes.length - 1] == (byte) 0 && strBytes[strBytes.length - 2] == (byte) 0) {
                byte[] copy = new byte[(strBytes.length - 2)];
                System.arraycopy(strBytes, 0, copy, 0, strBytes.length - 2);
                strBytes = copy;
            }
            return new String(strBytes, "UTF-16LE");
        }
        throw new IllegalStateException("Couldn't read the necessary amount of bytes.");
    }

    protected ContentDescriptionReader() {
    }

    private int[] getStringSizes(RandomAccessFile raf) throws IOException {
        int[] result = new int[5];
        for (int i = 0; i < result.length; i++) {
            result[i] = Utils.readUINT16(raf);
        }
        return result;
    }

    private ContentDescription parseData(RandomAccessFile raf) throws IOException {
        ContentDescription result = null;
        long chunkStart = raf.getFilePointer();
        if (GUID.GUID_CONTENTDESCRIPTION.equals(Utils.readGUID(raf))) {
            result = new ContentDescription(chunkStart, Utils.readBig64(raf));
            int[] stringSizes = getStringSizes(raf);
            String[] strings = new String[stringSizes.length];
            for (int i = 0; i < strings.length; i++) {
                if (stringSizes[i] > 0) {
                    strings[i] = readFixedSizeUTF16Str(raf, stringSizes[i]);
                }
            }
            if (stringSizes[0] > 0) {
                result.setTitle(strings[0]);
            }
            if (stringSizes[1] > 0) {
                result.setAuthor(strings[1]);
            }
            if (stringSizes[2] > 0) {
                result.setCopyRight(strings[2]);
            }
            if (stringSizes[3] > 0) {
                result.setComment(strings[3]);
            }
            if (stringSizes[4] > 0) {
                result.setRating(strings[4]);
            }
        }
        return result;
    }
}
