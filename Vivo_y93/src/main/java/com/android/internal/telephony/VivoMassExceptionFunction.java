package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Environment;
import android.telephony.Rlog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoMassExceptionFunction implements VivoMassExceptionAPI {
    public static final int LOG_COLLECTION_INTERVAL = 60000;
    public static final int LOG_COLLECTION_MAX = 5;
    public static final int LOG_COLLECTION_MAX_SIZE_OF_ONE = 5242880;
    public static final String LogRootDir = (Environment.getExternalStorageDirectory().getPath() + File.separator + ".vivoNetworkLog");

    public boolean isWriteToDataBaseAPI(int type, long count, int interval, CollectionBean item, Queue<CollectionBean> queue) {
        return false;
    }

    public String buildContentAPI(Queue<CollectionBean> queue) {
        return "";
    }

    public ArrayList<String> buildArrayListContentAPI(Queue<CollectionBean> queue) {
        return null;
    }

    public boolean writeToDatabaseAPI(Queue<CollectionBean> queue, String name) {
        return false;
    }

    public boolean writeFileAPI(String content, String name) {
        return false;
    }

    public void reportToServerAPI(CollectionBean outofserv) {
    }

    public String getCurrentModuleIdAPI() {
        return "";
    }

    public String getCurrrentSignalAPI() {
        return "";
    }

    public void setSignalStrengthsChangedAPI(int rsrp, int rsrq, int asu) {
    }

    public String getCurrrentRsrpAPI() {
        return "";
    }

    public String getCurrrentRsrqAPI() {
        return "";
    }

    public String getExceptionTypeAPI() {
        return "";
    }

    public String getExceptionSubTypeAPI() {
        return "";
    }

    public String getCurrentModuleSubEventIdAPI(int event) {
        return "";
    }

    public String getVersionAPI() {
        return "";
    }

    public boolean isDebugAPI() {
        return false;
    }

    public void setDebugAPI(boolean debug) {
    }

    public String getLocationAPI() {
        return "";
    }

    public void initAPI(Context context) {
    }

    public void disposeAPI(Context context) {
    }

    public String getCurrentModuleEventIdAPI() {
        return "";
    }

    public void uploadLogAPI(String id, String type, String content) {
    }

    private void deleteOldOrBigFile(File[] files) {
        if (files != null) {
            int count = files.length;
            boolean isDeleted = false;
            long[] createTime = new long[count];
            for (int i = 0; i < count; i++) {
                if (files[i].length() > 5242880) {
                    isDeleted = true;
                    logd("deleted file for too big :" + files[i].getName());
                    files[i].delete();
                } else {
                    try {
                        createTime[i] = Long.parseLong(files[i].getName().substring(0, files[i].getName().indexOf(".")));
                    } catch (Exception e) {
                        e.printStackTrace();
                        createTime[i] = System.currentTimeMillis();
                    }
                }
            }
            if (!isDeleted && count >= 5) {
                Arrays.sort(createTime);
                File file = new File(files[0].getParent() + File.separator + createTime[0] + ".log");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public void uploadLogAPI(int moduleId, String param1, String param2, String content, String eventId, JSONObject dt, String fullhash, String logpath) {
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0049 A:{SYNTHETIC, Splitter: B:29:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A:{SYNTHETIC, Splitter: B:24:0x0040} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0034 A:{SYNTHETIC, Splitter: B:17:0x0034} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readToString(String fileName) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        String encoding = "utf-8";
        File file = new File(fileName);
        byte[] filecontent = new byte[Long.valueOf(file.length()).intValue()];
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(file);
            try {
                in2.read(filecontent);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Exception e3) {
                    }
                }
                in = in2;
            } catch (FileNotFoundException e4) {
                e = e4;
                in = in2;
                e.printStackTrace();
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e5) {
                    }
                }
                return new String(filecontent, encoding);
            } catch (IOException e6) {
                e2 = e6;
                in = in2;
                try {
                    e2.printStackTrace();
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e7) {
                        }
                    }
                    return new String(filecontent, encoding);
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e8) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (in != null) {
            }
            return new String(filecontent, encoding);
        } catch (IOException e10) {
            e2 = e10;
            e2.printStackTrace();
            if (in != null) {
            }
            return new String(filecontent, encoding);
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e11) {
            Rlog.e("CollectionUtils", "The OS does not support " + encoding);
            e11.printStackTrace();
            return null;
        }
    }

    protected void logd(String s) {
        if (CollectonUtils.DBG) {
            Rlog.d("CollectionUtils", s);
        }
    }
}
