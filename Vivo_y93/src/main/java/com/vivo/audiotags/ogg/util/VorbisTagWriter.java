package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.Utils;
import com.vivo.audiotags.ogg.OggTag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class VorbisTagWriter {
    private VorbisTagReader reader = new VorbisTagReader();
    private VorbisTagCreator tc = new VorbisTagCreator();

    public void delete(RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotWriteException {
        try {
            OggTag tag = (OggTag) this.reader.read(raf);
            OggTag emptyTag = new OggTag();
            emptyTag.setVendor(tag.getVendor());
            write(emptyTag, raf, tempRaf);
        } catch (CannotReadException e) {
            write(new OggTag(), raf, tempRaf);
        }
    }

    public void write(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        int i;
        raf.seek(26);
        byte[] b = new byte[4];
        int pageSegments = raf.readByte() & 255;
        System.err.println("firstPage pageSegments --> : " + pageSegments + " , tmp_pos1 --> " + raf.getFilePointer());
        raf.seek(0);
        long tmp_pos2 = raf.getFilePointer();
        b = new byte[(pageSegments + 27)];
        raf.read(b);
        OggPageHeader firstPage = new OggPageHeader(b);
        System.err.println(firstPage.isValid());
        System.err.println("firstPage firstPage.getPageLength() --> : " + firstPage.getPageLength() + " ,tmp_pos2 -->  " + tmp_pos2);
        raf.seek(0);
        rafTemp.getChannel().transferFrom(raf.getChannel(), 0, (long) ((firstPage.getPageLength() + 27) + pageSegments));
        System.err.println("first page raf.getFilePointer --> : " + raf.getFilePointer());
        rafTemp.skipBytes((int) (((long) firstPage.getPageLength()) + raf.getFilePointer()));
        long pos = raf.getFilePointer();
        raf.seek(raf.getFilePointer() + 26);
        pageSegments = raf.readByte() & 255;
        System.err.println("2nd Page pageSegments --> : " + pageSegments + " , pos -->" + pos);
        raf.seek(pos);
        b = new byte[(pageSegments + 27)];
        raf.read(b);
        OggPageHeader oggPageHeader = new OggPageHeader(b);
        System.err.println(oggPageHeader.isValid());
        long secondPageEndPos = raf.getFilePointer();
        System.err.println("2nd Page secondPageEndPos --> : " + secondPageEndPos);
        raf.seek(raf.getFilePointer() + 7);
        System.err.println("2nd Page comment  raf.getFilePointer()  --> : " + raf.getFilePointer());
        b = new byte[4];
        raf.read(b);
        int vendorStringLength = Utils.getNumber(b, 0, 3);
        int oldCommentLength = (vendorStringLength + 4) + 7;
        System.err.println("2nd Page oldCommentLength --> : " + oldCommentLength + " , vendorStringLength --> " + vendorStringLength);
        raf.seek(raf.getFilePointer() + ((long) vendorStringLength));
        b = new byte[4];
        raf.read(b);
        int userComments = Utils.getNumber(b, 0, 3);
        oldCommentLength += 4;
        System.err.println("2nd Page userComments --> : " + userComments + " , oldCommentLength --> " + oldCommentLength);
        for (i = 0; i < userComments; i++) {
            b = new byte[4];
            raf.read(b);
            int commentLength = Utils.getNumber(b, 0, 3);
            oldCommentLength += commentLength + 4;
            raf.seek(raf.getFilePointer() + ((long) commentLength));
        }
        oldCommentLength++;
        if (raf.readByte() != (byte) 1) {
            throw new CannotWriteException("Unable to retreive old tag informations");
        }
        System.err.println("sunrain add oldCommentLength pos 1  --> : " + raf.getFilePointer() + " ,oldCommentLength -->  " + oldCommentLength);
        int nullByteCommentLength = 0;
        while (raf.readByte() == (byte) 0) {
            nullByteCommentLength++;
        }
        oldCommentLength += nullByteCommentLength;
        System.err.println("sunrain add oldCommentLength pos 2  --> : " + raf.getFilePointer() + " ,nullByteCommentLength--> " + nullByteCommentLength + " , oldCommentLength --> " + oldCommentLength);
        ByteBuffer newComment = this.tc.convert(tag);
        System.err.println("2nd Page comment  tag.toString()  --> : " + tag.toString());
        System.err.println("2nd Page newComment  ByteBuffer  --> : " + newComment.toString());
        int newCommentLength = newComment.capacity();
        System.err.println("newCommentLength: " + newCommentLength);
        int newSecondPageLength = (oggPageHeader.getPageLength() - oldCommentLength) + newCommentLength;
        System.err.println("second Page size: " + oggPageHeader.getPageLength());
        System.err.println("Old comment: " + oldCommentLength);
        System.err.println("newCommentLength: " + newCommentLength);
        byte[] segmentTable = createSegmentTable(oldCommentLength, newCommentLength, oggPageHeader);
        int newSecondPageHeaderLength = segmentTable.length + 27;
        System.err.println("segmentTable.length: " + segmentTable.length);
        System.err.println("newSecondPageHeaderLength: " + newSecondPageHeaderLength);
        ByteBuffer secondPageBuffer = ByteBuffer.allocate(newSecondPageLength + newSecondPageHeaderLength);
        System.err.println("New page length: " + ((oggPageHeader.getPageLength() - oldCommentLength) + newCommentLength));
        secondPageBuffer.put(new String("OggS").getBytes());
        secondPageBuffer.put((byte) 0);
        secondPageBuffer.put((byte) 0);
        secondPageBuffer.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0});
        System.err.println(oggPageHeader.getSerialNumber());
        secondPageBuffer.put(new byte[]{(byte) ((-16777216 & serialNb) >> 24), (byte) ((16711680 & serialNb) >> 16), (byte) ((65280 & serialNb) >> 8), (byte) (serialNb & 255)});
        System.err.println(oggPageHeader.getPageSequence());
        secondPageBuffer.put(new byte[]{(byte) ((-16777216 & seqNb) >> 24), (byte) ((16711680 & seqNb) >> 16), (byte) ((65280 & seqNb) >> 8), (byte) (seqNb & 255)});
        secondPageBuffer.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
        if (segmentTable.length > 255) {
            throw new CannotWriteException("In this special case we need to create a new page, since we still hadn't the time for that we won't write because it wouldn't create an ogg file.");
        }
        secondPageBuffer.put((byte) segmentTable.length);
        System.err.println(" segmentTable.length: " + segmentTable.length);
        for (i = 0; i < segmentTable.length; i++) {
            System.err.println(" segmentTable[i] --> " + segmentTable[i] + " , i --> " + i);
            secondPageBuffer.put(segmentTable[i]);
        }
        secondPageBuffer.put(newComment);
        raf.seek(secondPageEndPos);
        raf.skipBytes(oldCommentLength);
        raf.getChannel().read(secondPageBuffer);
        byte[] crc = OggCRCFactory.computeCRC(secondPageBuffer.array());
        for (i = 0; i < crc.length; i++) {
            System.err.println("Offset: " + (i + 22) + "|" + crc[i]);
            secondPageBuffer.put(i + 22, crc[i]);
            System.err.println(secondPageBuffer.get(i + 22));
        }
        secondPageBuffer.rewind();
        rafTemp.getChannel().write(secondPageBuffer);
        rafTemp.getChannel().transferFrom(raf.getChannel(), rafTemp.getFilePointer(), raf.length() - raf.getFilePointer());
    }

    private byte[] createSegmentTable(int oldCommentLength, int newCommentLength, OggPageHeader secondPage) {
        int totalLenght = secondPage.getPageLength();
        System.err.println("totalLenght --> " + totalLenght + " ,oldCommentLength " + oldCommentLength + " ,newCommentLength " + newCommentLength);
        byte[] restShouldBe = createSegments(totalLenght - oldCommentLength, false);
        byte[] newStart = createSegments(newCommentLength, true);
        System.err.println("newStart.length --> " + newStart.length + " ,restShouldBe.length -->" + restShouldBe.length);
        byte[] result = new byte[(newStart.length + restShouldBe.length)];
        System.arraycopy(newStart, 0, result, 0, newStart.length);
        System.arraycopy(restShouldBe, 0, result, newStart.length, restShouldBe.length);
        return result;
    }

    private byte[] createSegments(int length, boolean quitStream) {
        int i = 0;
        int i2 = length / 255;
        if (length % 255 != 0 || (quitStream ^ 1) == 0) {
            i = 1;
        }
        byte[] result = new byte[(i + i2)];
        int def = length % 255;
        System.err.println("abc --> " + (length / 255) + " ,def -->" + def + " ,result.length --> " + result.length);
        int i3 = 0;
        while (i3 < result.length - 1) {
            result[i3] = (byte) -1;
            i3++;
        }
        int num = length - (i3 * 255);
        System.err.println("i --> " + i3 + " ,length -->" + length + " , num --> " + num);
        result[result.length - 1] = (byte) num;
        for (int j = 0; j < result.length; j++) {
            System.err.println("j --> " + j + " ,result[] -->" + result[j]);
        }
        return result;
    }
}
