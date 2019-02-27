package com.vivo.audiotags.flac.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.ogg.OggTag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Vector;

public class FlacTagWriter {
    private Vector metadataBlockApplication = new Vector(1);
    private Vector metadataBlockCueSheet = new Vector(1);
    private Vector metadataBlockPadding = new Vector(1);
    private Vector metadataBlockSeekTable = new Vector(1);
    private FlacTagReader reader = new FlacTagReader();
    private FlacTagCreator tc = new FlacTagCreator();

    public void delete(RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotWriteException {
        try {
            OggTag tag = this.reader.read(raf);
            OggTag emptyTag = new OggTag();
            emptyTag.setVendor(tag.getVendor());
            raf.seek(0);
            tempRaf.seek(0);
            write(emptyTag, raf, tempRaf);
        } catch (CannotReadException e) {
            write(new OggTag(), raf, tempRaf);
        }
    }

    public void write(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.metadataBlockPadding.removeAllElements();
        this.metadataBlockApplication.removeAllElements();
        this.metadataBlockSeekTable.removeAllElements();
        this.metadataBlockCueSheet.removeAllElements();
        byte[] b = new byte[4];
        raf.readFully(b);
        if (new String(b).equals("fLaC")) {
            raf.write(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 34});
            raf.seek(4);
            boolean isLastBlock = false;
            while (!isLastBlock) {
                b = new byte[4];
                raf.readFully(b);
                MetadataBlockHeader mbh = new MetadataBlockHeader(b);
                System.err.println("BlockType: " + mbh.getBlockTypeString());
                switch (mbh.getBlockType()) {
                    case 1:
                        handlePadding(mbh, raf);
                        break;
                    case 2:
                        handleApplication(mbh, raf);
                        break;
                    case 3:
                        handleSeekTable(mbh, raf);
                        break;
                    case 4:
                        handlePadding(mbh, raf);
                        break;
                    case 5:
                        handleCueSheet(mbh, raf);
                        break;
                    default:
                        skipBlock(mbh, raf);
                        break;
                }
                isLastBlock = mbh.isLastBlock();
            }
            int availableRoom = computeAvailableRoom();
            int newTagSize = this.tc.getTagLength(tag);
            int neededRoom = newTagSize + computeNeededRoom();
            System.err.println("Av.:" + availableRoom + "|Needed:" + neededRoom + "|newTagSize:" + newTagSize);
            raf.seek(0);
            int i;
            if (availableRoom >= neededRoom) {
                System.out.println("sunrain OVERWRITE EXISTING TAG");
                raf.seek(42);
                for (i = 0; i < this.metadataBlockApplication.size(); i++) {
                    raf.write(((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getHeader().getBytes());
                    raf.write(((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getData().getBytes());
                }
                for (i = 0; i < this.metadataBlockSeekTable.size(); i++) {
                    raf.write(((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getHeader().getBytes());
                    raf.write(((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getData().getBytes());
                }
                for (i = 0; i < this.metadataBlockCueSheet.size(); i++) {
                    raf.write(((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getHeader().getBytes());
                    raf.write(((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getData().getBytes());
                }
                raf.getChannel().write(this.tc.convert(tag, availableRoom - neededRoom));
                return;
            }
            System.out.println("sunrain create new tag with padding");
            FileChannel fc = raf.getChannel();
            b = new byte[42];
            raf.readFully(b);
            raf.seek((long) (availableRoom + 42));
            FileChannel tempFC = rafTemp.getChannel();
            rafTemp.write(b);
            for (i = 0; i < this.metadataBlockApplication.size(); i++) {
                rafTemp.write(((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getHeader().getBytes());
                rafTemp.write(((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getData().getBytes());
            }
            for (i = 0; i < this.metadataBlockSeekTable.size(); i++) {
                rafTemp.write(((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getHeader().getBytes());
                rafTemp.write(((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getData().getBytes());
            }
            for (i = 0; i < this.metadataBlockCueSheet.size(); i++) {
                rafTemp.write(((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getHeader().getBytes());
                rafTemp.write(((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getData().getBytes());
            }
            rafTemp.write(this.tc.convert(tag, 4000).array());
            tempFC.transferFrom(fc, tempFC.position(), fc.size());
            return;
        }
        throw new CannotWriteException("This is not a FLAC file");
    }

    private int computeAvailableRoom() {
        int i;
        int length = 0;
        for (i = 0; i < this.metadataBlockApplication.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getLength();
        }
        for (i = 0; i < this.metadataBlockSeekTable.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getLength();
        }
        for (i = 0; i < this.metadataBlockCueSheet.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getLength();
        }
        for (i = 0; i < this.metadataBlockPadding.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockPadding.elementAt(i)).getLength();
        }
        System.out.println("sunrain FlacTagWriter computeAvailableRoom length-->" + length);
        return length;
    }

    private int computeNeededRoom() {
        int i;
        int length = 0;
        for (i = 0; i < this.metadataBlockApplication.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockApplication.elementAt(i)).getLength();
        }
        for (i = 0; i < this.metadataBlockSeekTable.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockSeekTable.elementAt(i)).getLength();
        }
        for (i = 0; i < this.metadataBlockCueSheet.size(); i++) {
            length += ((MetadataBlock) this.metadataBlockCueSheet.elementAt(i)).getLength();
        }
        System.out.println("sunrain FlacTagWriter computeNeededRoom  length-->" + length);
        return length;
    }

    private void skipBlock(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException {
        raf.seek(raf.getFilePointer() + ((long) mbh.getDataLength()));
    }

    private void handlePadding(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException {
        raf.seek(raf.getFilePointer() + ((long) mbh.getDataLength()));
        this.metadataBlockPadding.add(new MetadataBlock(mbh, new MetadataBlockDataPadding(mbh.getDataLength())));
    }

    private void handleApplication(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException {
        byte[] b = new byte[mbh.getDataLength()];
        raf.readFully(b);
        this.metadataBlockApplication.add(new MetadataBlock(mbh, new MetadataBlockDataApplication(b)));
    }

    private void handleSeekTable(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException {
        byte[] b = new byte[mbh.getDataLength()];
        raf.readFully(b);
        this.metadataBlockSeekTable.add(new MetadataBlock(mbh, new MetadataBlockDataSeekTable(b)));
    }

    private void handleCueSheet(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException {
        byte[] b = new byte[mbh.getDataLength()];
        raf.readFully(b);
        this.metadataBlockCueSheet.add(new MetadataBlock(mbh, new MetadataBlockDataCueSheet(b)));
    }
}
