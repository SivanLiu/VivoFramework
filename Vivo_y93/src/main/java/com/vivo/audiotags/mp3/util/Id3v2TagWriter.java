package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Id3v2TagWriter {
    private Id3v2TagCreator tc = new Id3v2TagCreator();

    public RandomAccessFile delete(RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException {
        FileChannel fc = raf.getChannel();
        fc.position(0);
        if (!tagExists(fc)) {
            return raf;
        }
        fc.position(6);
        ByteBuffer b = ByteBuffer.allocate(4);
        fc.read(b);
        b.rewind();
        int tagSize = ((((b.get() & 255) << 21) + ((b.get() & 255) << 14)) + ((b.get() & 255) << 7)) + (b.get() & 255);
        FileChannel tempFC = tempRaf.getChannel();
        tempFC.position(0);
        fc.position((long) (tagSize + 10));
        b = ByteBuffer.allocate(4);
        int skip = 0;
        while (fc.read(b) != -1) {
            if ((b.get(0) & 255) == 255 && (b.get(1) & 224) == 224 && (b.get(1) & 6) != 0 && (b.get(2) & 240) != 240 && (b.get(2) & 8) != 8) {
                fc.position(fc.position() - 4);
                break;
            }
            fc.position(fc.position() - 3);
            b.rewind();
            skip++;
        }
        tempFC.transferFrom(fc, 0, ((fc.size() - ((long) tagSize)) - 10) - ((long) skip));
        return tempRaf;
    }

    private boolean tagExists(FileChannel fc) throws IOException {
        ByteBuffer b = ByteBuffer.allocate(3);
        fc.position(0);
        fc.read(b);
        return new String(b.array()).equals("ID3");
    }

    private boolean canOverwrite(RandomAccessFile raf) throws IOException {
        boolean z;
        raf.seek(3);
        String versionHigh = raf.read() + "";
        if (versionHigh.equals("4") || versionHigh.equals("3")) {
            z = true;
        } else {
            z = versionHigh.equals("2");
        }
        if (z) {
            return true;
        }
        return false;
    }

    public void write(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        FileChannel fc = raf.getChannel();
        int oldTagSize = 0;
        if (tagExists(fc)) {
            if (canOverwrite(raf)) {
                fc.position(6);
                ByteBuffer buf = ByteBuffer.allocate(4);
                fc.read(buf);
                oldTagSize = (((((buf.get(0) & 255) << 21) + ((buf.get(1) & 255) << 14)) + ((buf.get(2) & 255) << 7)) + (buf.get(3) & 255)) + 10;
                int newTagSize = this.tc.getTagLength(tag);
                if (oldTagSize >= newTagSize) {
                    fc.position(0);
                    fc.write(this.tc.convert(tag, oldTagSize - newTagSize));
                    return;
                }
            }
            throw new CannotWriteException("Overwritting of this kind of ID3v2 tag not supported yet");
        }
        fc.position((long) oldTagSize);
        if (fc.size() > 15728640) {
            FileChannel tempFC = tempRaf.getChannel();
            tempFC.position(0);
            tempFC.write(this.tc.convert(tag, 4000));
            tempFC.transferFrom(fc, tempFC.position(), fc.size() - ((long) oldTagSize));
            fc.close();
        } else {
            ByteBuffer[] content = new ByteBuffer[2];
            try {
                content[1] = ByteBuffer.allocate((int) fc.size());
            } catch (Exception e) {
                System.err.println("sunrain catch  bytebuff allocate exception -->  " + e);
            }
            fc.read(content[1]);
            content[1].rewind();
            content[0] = this.tc.convert(tag, 4000);
            fc.position(0);
            fc.write(content);
        }
    }
}
