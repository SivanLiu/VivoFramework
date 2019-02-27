package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.ContentDescriptor;
import com.vivo.audiotags.asf.data.ExtendedContentDescription;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ExtContentDescReader {
    public static ExtendedContentDescription read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_EXTENDED_CONTENT_DESCRIPTION.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new ExtContentDescReader().parseData(raf);
        }
    }

    protected ExtContentDescReader() {
    }

    private ExtendedContentDescription parseData(RandomAccessFile raf) throws IOException {
        ExtendedContentDescription result = null;
        long chunkStart = raf.getFilePointer();
        if (GUID.GUID_EXTENDED_CONTENT_DESCRIPTION.equals(Utils.readGUID(raf))) {
            long descriptorCount = (long) Utils.readUINT16(raf);
            result = new ExtendedContentDescription(chunkStart, Utils.readBig64(raf));
            for (long i = 0; i < descriptorCount; i++) {
                String tagElement = Utils.readUTF16LEStr(raf);
                int type = Utils.readUINT16(raf);
                ContentDescriptor prop = new ContentDescriptor(tagElement, type);
                switch (type) {
                    case 0:
                        prop.setStringValue(Utils.readUTF16LEStr(raf));
                        break;
                    case 1:
                        prop.setBinaryValue(readBinaryData(raf));
                        break;
                    case 2:
                        prop.setBooleanValue(readBoolean(raf));
                        break;
                    case 3:
                        raf.skipBytes(2);
                        prop.setDWordValue(Utils.readUINT32(raf));
                        break;
                    case 4:
                        raf.skipBytes(2);
                        prop.setQWordValue(Utils.readUINT64(raf));
                        break;
                    case 5:
                        raf.skipBytes(2);
                        prop.setWordValue(Utils.readUINT16(raf));
                        break;
                    default:
                        prop.setStringValue("Invalid datatype: " + new String(readBinaryData(raf)));
                        break;
                }
                result.addDescriptor(prop);
            }
        }
        return result;
    }

    private byte[] readBinaryData(RandomAccessFile raf) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int size = Utils.readUINT16(raf);
        for (int i = 0; i < size; i++) {
            bos.write(raf.read());
        }
        return bos.toByteArray();
    }

    private boolean readBoolean(RandomAccessFile raf) throws IOException {
        int size = Utils.readUINT16(raf);
        if (size == 4) {
            return Utils.readUINT32(raf) == 1;
        } else {
            throw new IllegalStateException("Boolean value do require 4 Bytes. (Size value is: " + size + ")");
        }
    }
}
