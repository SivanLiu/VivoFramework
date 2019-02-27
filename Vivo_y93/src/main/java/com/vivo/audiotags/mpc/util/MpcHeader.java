package com.vivo.audiotags.mpc.util;

import com.vivo.audiotags.generic.Utils;

public class MpcHeader {
    byte[] b;

    public MpcHeader(byte[] b) {
        this.b = b;
    }

    public int getSamplesNumber() {
        if (this.b[0] == (byte) 7) {
            return Utils.getNumber(this.b, 1, 4);
        }
        return -1;
    }

    public int getSamplingRate() {
        if (this.b[0] != (byte) 7) {
            return -1;
        }
        switch (this.b[6] & 2) {
            case 0:
                return 44100;
            case 1:
                return 48000;
            case 2:
                return 37800;
            case 3:
                return 32000;
            default:
                return -1;
        }
    }

    public int getChannelNumber() {
        return this.b[0] == (byte) 7 ? 2 : 2;
    }

    public String getEncodingType() {
        StringBuffer out = new StringBuffer().append("MPEGplus (MPC)");
        if (this.b[0] == (byte) 7) {
            out.append(" rev.7, Profile:");
            switch ((this.b[7] & 240) >> 4) {
                case 0:
                    out.append("No profile");
                    break;
                case 1:
                    out.append("Unstable/Experimental");
                    break;
                case 2:
                    out.append("Unused");
                    break;
                case 3:
                    out.append("Unused");
                    break;
                case 4:
                    out.append("Unused");
                    break;
                case 5:
                    out.append("Below Telephone (q= 0.0)");
                    break;
                case 6:
                    out.append("Below Telephone (q= 1.0)");
                    break;
                case 7:
                    out.append("Telephone (q= 2.0)");
                    break;
                case 8:
                    out.append("Thumb (q= 3.0)");
                    break;
                case 9:
                    out.append("Radio (q= 4.0)");
                    break;
                case 10:
                    out.append("Standard (q= 5.0)");
                    break;
                case 11:
                    out.append("Xtreme (q= 6.0)");
                    break;
                case 12:
                    out.append("Insane (q= 7.0)");
                    break;
                case 13:
                    out.append("BrainDead (q= 8.0)");
                    break;
                case 14:
                    out.append("Above BrainDead (q= 9.0)");
                    break;
                case 15:
                    out.append("Above BrainDead (q=10.0)");
                    break;
                default:
                    out.append("No profile");
                    break;
            }
        }
        return out.toString();
    }

    public String getEncoderInfo() {
        int encoder = this.b[24];
        StringBuffer out = new StringBuffer().append("Mpc encoder v").append(((double) encoder) / 100.0d).append(" ");
        if (encoder % 10 == 0) {
            out.append("Release");
        } else if (encoder % 2 == 0) {
            out.append("Beta");
        } else if (encoder % 2 == 1) {
            out.append("Alpha");
        }
        return out.toString();
    }
}
