package android.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class AbsCpuInfo {
    protected final int DEFAULT_INVALID_VALUE_INT = 0;
    protected final long DEFAULT_INVALID_VALUE_LONG = 0;
    protected int mCpuNumber = 0;
    protected long mCpuType = 0;
    protected long mMaxCpuRate = 0;

    public abstract int getCPUCoreNumber();

    public abstract String getCpuModel(Context context);

    public abstract long getMaxCpuRate();

    protected abstract void init();

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0032 A:{SYNTHETIC, Splitter: B:19:0x0032} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003e A:{SYNTHETIC, Splitter: B:25:0x003e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected String readFileFirstLine(String path) {
        IOException e;
        Throwable th;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        String line = null;
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(file));
            try {
                line = br2.readLine();
                br2.close();
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                br = br2;
            } catch (IOException e3) {
                e2 = e3;
                br = br2;
                try {
                    e2.printStackTrace();
                    if (br != null) {
                    }
                    return line;
                } catch (Throwable th2) {
                    th = th2;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                br = br2;
                if (br != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            e22.printStackTrace();
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return line;
        }
        return line;
    }
}
