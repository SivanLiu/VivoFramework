package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;
import java.util.ArrayList;

public class StreamBitratePropertiesChunk extends Chunk {
    private final ArrayList bitRates = new ArrayList();
    private final ArrayList streamNumbers = new ArrayList();

    public StreamBitratePropertiesChunk(long pos, BigInteger chunkSize) {
        super(GUID.GUID_STREAM_BITRATE_PROPERTIES, pos, chunkSize);
    }

    public void addBitrateRecord(int streamNum, long averageBitrate) {
        this.streamNumbers.add(new Integer(streamNum));
        this.bitRates.add(new Long(averageBitrate));
    }

    public long getAvgBitrate(int streamNumber) {
        int index = this.streamNumbers.indexOf(new Integer(streamNumber));
        if (index != -1) {
            return ((Long) this.bitRates.get(index)).longValue();
        }
        return -1;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, Utils.LINE_SEPARATOR + "Stream Bitrate Properties:" + Utils.LINE_SEPARATOR);
        for (int i = 0; i < this.bitRates.size(); i++) {
            result.append("   Stream no. \"" + this.streamNumbers.get(i) + "\" has an average bitrate of \"" + this.bitRates.get(i) + "\"" + Utils.LINE_SEPARATOR);
        }
        result.append(Utils.LINE_SEPARATOR);
        return result.toString();
    }
}
