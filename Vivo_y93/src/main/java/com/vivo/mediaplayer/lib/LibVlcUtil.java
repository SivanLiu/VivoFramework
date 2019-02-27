package com.vivo.mediaplayer.lib;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LibVlcUtil {
    private static String[] CPU_archs = new String[]{"*Pre-v4", "*v4", "*v4T", "v5T", "v5TE", "v5TEJ", "v6", "v6KZ", "v6T2", "v6K", "v7", "*v6-M", "*v6S-M", "*v7E-M", "*v8"};
    private static final int ELF_HEADER_SIZE = 52;
    private static final int EM_386 = 3;
    private static final int EM_ARM = 40;
    private static final int EM_MIPS = 8;
    private static final int SECTION_HEADER_SIZE = 40;
    private static final int SHT_ARM_ATTRIBUTES = 1879048195;
    public static final String TAG = "VMediaPlayer/Util";
    private static String errorMsg = null;
    private static boolean isCompatible = false;

    private static class ElfData {
        String att_arch;
        boolean att_fpu;
        int e_machine;
        int e_shnum;
        int e_shoff;
        ByteOrder order;
        int sh_offset;
        int sh_size;

        /* synthetic */ ElfData(ElfData -this0) {
            this();
        }

        private ElfData() {
        }
    }

    public static boolean isFroyoOrLater() {
        return VERSION.SDK_INT >= EM_MIPS;
    }

    public static boolean isGingerbreadOrLater() {
        return VERSION.SDK_INT >= 9;
    }

    public static boolean isHoneycombOrLater() {
        return VERSION.SDK_INT >= 11;
    }

    public static boolean isICSOrLater() {
        return VERSION.SDK_INT >= 14;
    }

    public static boolean isJellyBeanOrLater() {
        return VERSION.SDK_INT >= 16;
    }

    public static String getErrorMsg() {
        return errorMsg;
    }

    public static boolean hasCompatibleCPU(Context context) {
        if (errorMsg != null || isCompatible) {
            return isCompatible;
        }
        ElfData elf = readLib(context.getApplicationInfo().dataDir + "/lib/libvlcjni.so");
        if (elf == null) {
            Log.e(TAG, "WARNING: Unable to read libvlcjni.so; cannot check device ABI!");
            Log.e(TAG, "WARNING: Cannot guarantee correct ABI for this build (may crash)!");
            return true;
        }
        String CPU_ABI = Build.CPU_ABI;
        String CPU_ABI2 = "none";
        if (VERSION.SDK_INT >= EM_MIPS) {
            try {
                CPU_ABI2 = (String) Build.class.getDeclaredField("CPU_ABI2").get(null);
            } catch (Exception e) {
            }
        }
        String str = TAG;
        StringBuilder append = new StringBuilder().append("machine = ");
        String str2 = elf.e_machine == 40 ? "arm" : elf.e_machine == 3 ? "x86" : "mips";
        Log.i(str, append.append(str2).toString());
        Log.i(TAG, "arch = " + elf.att_arch);
        Log.i(TAG, "fpu = " + elf.att_fpu);
        boolean hasNeon = false;
        boolean hasFpu = false;
        boolean hasArmV6 = false;
        boolean hasArmV7 = false;
        boolean hasMips = false;
        boolean hasX86 = false;
        if (CPU_ABI.equals("x86")) {
            hasX86 = true;
        } else if (CPU_ABI.equals("armeabi-v7a") || CPU_ABI2.equals("armeabi-v7a")) {
            hasArmV7 = true;
            hasArmV6 = true;
        } else if (CPU_ABI.equals("armeabi") || CPU_ABI2.equals("armeabi")) {
            hasArmV6 = true;
        }
        try {
            FileReader fileReader = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fileReader);
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (!hasArmV7 && line.contains("ARMv7")) {
                    hasArmV7 = true;
                    hasArmV6 = true;
                }
                if (!(hasArmV7 || (hasArmV6 ^ 1) == 0 || !line.contains("ARMv6"))) {
                    hasArmV6 = true;
                }
                if (line.contains("clflush size")) {
                    hasX86 = true;
                }
                if (line.contains("microsecond timers")) {
                    hasMips = true;
                }
                if (!hasNeon && line.contains("neon")) {
                    hasNeon = true;
                }
                if (!hasFpu && line.contains("vfp")) {
                    hasFpu = true;
                }
            }
            fileReader.close();
            if (elf.e_machine == 3 && (hasX86 ^ 1) != 0) {
                errorMsg = "x86 build on non-x86 device";
                isCompatible = false;
                return false;
            } else if (elf.e_machine == 40 && hasX86) {
                errorMsg = "ARM build on x86 device";
                isCompatible = false;
                return false;
            } else if (elf.e_machine == EM_MIPS && (hasMips ^ 1) != 0) {
                errorMsg = "MIPS build on non-MIPS device";
                isCompatible = false;
                return false;
            } else if (elf.e_machine == 40 && hasMips) {
                errorMsg = "ARM build on MIPS device";
                isCompatible = false;
                return false;
            } else if (elf.e_machine == 40 && elf.att_arch.startsWith("v7") && (hasArmV7 ^ 1) != 0) {
                errorMsg = "ARMv7 build on non-ARMv7 device";
                isCompatible = false;
                return false;
            } else {
                if (elf.e_machine == 40) {
                    if (elf.att_arch.startsWith("v6") && (hasArmV6 ^ 1) != 0) {
                        errorMsg = "ARMv6 build on non-ARMv6 device";
                        isCompatible = false;
                        return false;
                    } else if (elf.att_fpu && (hasFpu ^ 1) != 0) {
                        errorMsg = "FPU-enabled build on non-FPU device";
                        isCompatible = false;
                        return false;
                    }
                }
                errorMsg = null;
                isCompatible = true;
                return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            errorMsg = "IOException whilst reading cpuinfo flags";
            isCompatible = false;
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0087 A:{SYNTHETIC, Splitter: B:58:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x009c A:{SYNTHETIC, Splitter: B:71:0x009c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ElfData readLib(String path) {
        RandomAccessFile randomAccessFile;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(path);
        if (!file.exists() || (file.canRead() ^ 1) != 0) {
            return null;
        }
        randomAccessFile = null;
        try {
            RandomAccessFile in = new RandomAccessFile(file, "r");
            try {
                ElfData elf = new ElfData();
                if (readHeader(in, elf)) {
                    switch (elf.e_machine) {
                        case 3:
                        case EM_MIPS /*8*/:
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e3) {
                                }
                            }
                            return elf;
                        case 40:
                            in.close();
                            randomAccessFile = new RandomAccessFile(file, "r");
                            if (readSection(randomAccessFile, elf)) {
                                randomAccessFile.close();
                                in = new RandomAccessFile(file, "r");
                                if (readArmAttributes(in, elf)) {
                                    if (in != null) {
                                        try {
                                            in.close();
                                        } catch (IOException e4) {
                                        }
                                    }
                                    return elf;
                                }
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e5) {
                                    }
                                }
                                return null;
                            }
                            if (randomAccessFile != null) {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e6) {
                                }
                            }
                            return null;
                        default:
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e7) {
                                }
                            }
                            return null;
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e8) {
                    }
                }
                return null;
            } catch (FileNotFoundException e9) {
                e = e9;
                randomAccessFile = in;
            } catch (IOException e10) {
                e2 = e10;
                randomAccessFile = in;
                try {
                    e2.printStackTrace();
                    if (randomAccessFile != null) {
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e11) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = in;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e12) {
            e = e12;
        } catch (IOException e13) {
            e2 = e13;
            e2.printStackTrace();
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e14) {
                }
            }
            return null;
        }
        e.printStackTrace();
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e15) {
            }
        }
        return null;
    }

    private static boolean readHeader(RandomAccessFile in, ElfData elf) throws IOException {
        byte[] bytes = new byte[ELF_HEADER_SIZE];
        in.readFully(bytes);
        if (bytes[0] != Byte.MAX_VALUE || bytes[1] != (byte) 69 || bytes[2] != (byte) 76 || bytes[3] != (byte) 70 || bytes[4] != (byte) 1) {
            return false;
        }
        ByteOrder byteOrder;
        if (bytes[5] == (byte) 1) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            byteOrder = ByteOrder.BIG_ENDIAN;
        }
        elf.order = byteOrder;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(elf.order);
        elf.e_machine = buffer.getShort(18);
        elf.e_shoff = buffer.getInt(32);
        elf.e_shnum = buffer.getShort(48);
        return true;
    }

    private static boolean readSection(RandomAccessFile in, ElfData elf) throws IOException {
        byte[] bytes = new byte[40];
        in.seek((long) elf.e_shoff);
        int i = 0;
        while (i < elf.e_shnum) {
            in.readFully(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(elf.order);
            if (buffer.getInt(4) != SHT_ARM_ATTRIBUTES) {
                i++;
            } else {
                elf.sh_offset = buffer.getInt(16);
                elf.sh_size = buffer.getInt(20);
                return true;
            }
        }
        return false;
    }

    private static boolean readArmAttributes(RandomAccessFile in, ElfData elf) throws IOException {
        byte[] bytes = new byte[elf.sh_size];
        in.seek((long) elf.sh_offset);
        in.readFully(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(elf.order);
        if (buffer.get() != (byte) 65) {
            return false;
        }
        while (buffer.remaining() > 0) {
            int start_section = buffer.position();
            int length = buffer.getInt();
            if (getString(buffer).equals("aeabi")) {
                while (buffer.position() < start_section + length) {
                    int start = buffer.position();
                    int tag = buffer.get();
                    int size = buffer.getInt();
                    if (tag != 1) {
                        buffer.position(start + size);
                    } else {
                        while (buffer.position() < start + size) {
                            tag = getUleb128(buffer);
                            if (tag == 6) {
                                elf.att_arch = CPU_archs[getUleb128(buffer)];
                            } else if (tag == 27) {
                                getUleb128(buffer);
                                elf.att_fpu = true;
                            } else {
                                tag %= 128;
                                if (tag == 4 || tag == 5 || tag == 32 || (tag > 32 && (tag & 1) != 0)) {
                                    getString(buffer);
                                } else {
                                    getUleb128(buffer);
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return true;
    }

    private static String getString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder(buffer.limit());
        while (buffer.remaining() > 0) {
            char c = (char) buffer.get();
            if (c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static int getUleb128(ByteBuffer buffer) {
        int ret = 0;
        int c;
        do {
            ret <<= 7;
            c = buffer.get();
            ret |= c & 127;
        } while ((c & 128) > 0);
        return ret;
    }
}
