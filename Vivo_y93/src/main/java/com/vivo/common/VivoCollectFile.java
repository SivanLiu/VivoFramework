package com.vivo.common;

import android.os.FileUtils;
import android.os.SystemProperties;
import android.telecom.Logging.Session;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.telephony.SmsConstants;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xmlpull.v1.XmlPullParser;

public class VivoCollectFile {
    private static final String CONFIG_FILE = "/data/data/com.bbk.iqoo.logsystem/shared_prefs/event_id_info.xml";
    private static final String CONFIG_FILE_EXT = "/data/bbkcore/event_id_info.xml";
    private static final boolean DBG = false;
    private static final DateFormat DF_DAY = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat DF_NORMAL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DF_SECOND = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    private static final String DIR = "/data/vlog";
    private static final String FILE_SUFFIX = ".txt";
    private static final int MAX_FILE_NUM = 1;
    private static final String NEW_LINE = "\r\n";
    private static final int RESERVED_SPACE = 104857600;
    private static final String ROOT_DIR = "/data";
    private static final String TAG = ToolUtils.makeTag("CollectFile");
    private static int sMaxDirSize = 1048576;
    private static int sMaxFileSize = (sMaxDirSize / 1);
    private static int sTotalDirSize = 26214400;

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int writeData(String eventId, String fileName, String eventInfo, boolean needRecordHead, String appVersion) {
        if (ToolUtils.isEmpty(eventId) || ToolUtils.isEmpty(fileName) || !fileName.startsWith(eventId + Session.SESSION_SEPARATION_CHAR_CHILD)) {
            return -1;
        }
        return doWrite(eventId, fileName, eventInfo, true, needRecordHead, appVersion);
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x014d A:{ExcHandler: all (th java.lang.Throwable), Splitter: B:27:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x014d A:{ExcHandler: all (th java.lang.Throwable), Splitter: B:27:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0132  */
    /* JADX WARNING: Missing block: B:71:0x014d, code:
            r13 = th;
     */
    /* JADX WARNING: Missing block: B:72:0x014e, code:
            r8 = r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int writeCmd(String eventId, String fileName, String[][] cmds, boolean needRecordHead, String appVersion) {
        InputStreamReader input;
        Process p;
        Throwable th;
        if (ToolUtils.isEmpty(eventId) || ToolUtils.isEmpty(fileName)) {
            return -1;
        }
        if (!fileName.startsWith(eventId + Session.SESSION_SEPARATION_CHAR_CHILD)) {
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int length = cmds.length;
        InputStreamReader input2 = null;
        while (i < length) {
            String[] cmd = cmds[i];
            if (cmd[0].trim().length() <= 0) {
                input = input2;
            } else {
                if (cmd[1] == null) {
                    cmd[1] = " ";
                }
                p = null;
                try {
                    if ("logcat".equals(cmd[0].trim())) {
                        cmd[1] = "-v time -b events -b system -b main -t 500";
                    } else if ("top".equals(cmd[0].trim())) {
                        cmd[1] = "-m 10 -t -n 3";
                    }
                    p = Runtime.getRuntime().exec(cmd[0] + " " + cmd[1]);
                    try {
                        p.getOutputStream().close();
                    } catch (IOException e) {
                    } catch (Throwable th2) {
                    }
                    try {
                        p.getErrorStream().close();
                    } catch (IOException e2) {
                    } catch (Throwable th22) {
                    }
                    input = new InputStreamReader(p.getInputStream());
                    try {
                        sb.append(NEW_LINE);
                        sb.append("cmd: ").append(cmd[0]).append(" ").append(cmd[1]).append(NEW_LINE);
                        char[] buf = new char[8192];
                        while (true) {
                            int num = input.read(buf);
                            if (num > 0) {
                                sb.append(buf, 0, num);
                            } else {
                                try {
                                    break;
                                } catch (InterruptedException e3) {
                                    e3.printStackTrace();
                                }
                            }
                        }
                        p.waitFor();
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e4) {
                            }
                        }
                        if (p != null) {
                            p.destroy();
                        }
                    } catch (IOException e5) {
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } catch (IOException e6) {
                    input = input2;
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e7) {
                        }
                    }
                    if (p != null) {
                        p.destroy();
                    }
                    i++;
                    input2 = input;
                } catch (Throwable th222) {
                }
            }
            i++;
            input2 = input;
        }
        return writeData(eventId, fileName, sb.toString(), needRecordHead, appVersion);
        if (p != null) {
            p.destroy();
        }
        throw th;
        if (input != null) {
            try {
                input.close();
            } catch (IOException e8) {
            }
        }
        if (p != null) {
        }
        throw th;
    }

    @Deprecated
    public static int write(String eventId, String eventInfo) {
        return write(eventId, null, eventInfo, false);
    }

    @Deprecated
    public static int write(String eventId, String fileName, String eventInfo, boolean isAppend) {
        if (eventId == null || "".equals(eventId) || eventInfo == null || "".equals(eventInfo)) {
            return -1;
        }
        return doWrite(eventId, fileName, eventInfo, isAppend, false, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x016c A:{SYNTHETIC, Splitter: B:52:0x016c} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x017a A:{SYNTHETIC, Splitter: B:62:0x017a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized int doWrite(String eventId, String fileName, String eventInfo, boolean isAppend, boolean needRecordHead, String appVersion) {
        Throwable th;
        synchronized (VivoCollectFile.class) {
            if (checkDir(eventId) < 0) {
                return -1;
            }
            boolean needSuffix = true;
            String outFile = new File(DIR, eventId).getAbsolutePath() + File.separator;
            if (fileName != null && fileName.length() > 0) {
                if (isAppend) {
                    String s = chooseFile(outFile + fileName);
                    if (s == null) {
                        outFile = outFile + fileName + Session.SESSION_SEPARATION_CHAR_CHILD;
                    } else if (new File(outFile + s).length() + ((long) eventInfo.length()) > ((long) sMaxFileSize)) {
                        outFile = outFile + fileName + Session.SESSION_SEPARATION_CHAR_CHILD;
                    } else {
                        outFile = outFile + s;
                        needSuffix = false;
                    }
                } else {
                    outFile = outFile + fileName + Session.SESSION_SEPARATION_CHAR_CHILD;
                }
            }
            if (needSuffix) {
                outFile = (outFile + DF_SECOND.format(new Date())) + FILE_SUFFIX;
            }
            int len = eventInfo.length();
            String data = eventInfo;
            if (len > sMaxFileSize) {
                len = sMaxFileSize;
                data = eventInfo.substring(0, sMaxFileSize);
            }
            FileWriter writer = null;
            try {
                FileWriter writer2 = new FileWriter(outFile, true);
                try {
                    StringBuilder sb = new StringBuilder();
                    if (needRecordHead) {
                        sb.append(createRecordHead(appVersion));
                    }
                    writer2.write(sb.toString());
                    writer2.write(data);
                    writer2.write(NEW_LINE);
                    writer2.flush();
                    if (writer2 != null) {
                        try {
                            writer2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    FileUtils.setPermissions(outFile, 511, -1, -1);
                    return len;
                } catch (IOException e2) {
                    writer = writer2;
                    if (writer != null) {
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    writer = writer2;
                    if (writer != null) {
                    }
                    throw th;
                }
            } catch (IOException e3) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                return -1;
            } catch (Throwable th3) {
                th = th3;
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private static int checkDir(String eventId) {
        int n = -1;
        try {
            n = Integer.parseInt(parseXml("file_size"));
        } catch (NumberFormatException e) {
        }
        if (n > 0) {
            sMaxDirSize = n << 20;
            sMaxFileSize = sMaxDirSize / 1;
        }
        n = -1;
        try {
            n = Integer.parseInt(parseXml("dir_size"));
        } catch (NumberFormatException e2) {
        }
        if (n > 0) {
            sTotalDirSize = n << 20;
        }
        if (new File(ROOT_DIR).getFreeSpace() <= 104857600) {
            return -1;
        }
        File file = new File(DIR);
        if (!file.exists() && !file.mkdir()) {
            return -1;
        }
        if (file.isFile()) {
            file.delete();
            if (!file.mkdir()) {
                return -1;
            }
        }
        deleteOldDir(new File(DIR));
        file = new File(DIR, eventId);
        if (file.exists()) {
            deleteOldFile(file, (long) sMaxDirSize);
        } else if (!file.mkdir()) {
            return -1;
        } else {
            FileUtils.setPermissions(file, 511, -1, -1);
        }
        return 0;
    }

    private static void deleteOldDir(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            long size = 0;
            long modifyTime = SubscriptionPlan.BYTES_UNLIMITED;
            File oldFile = null;
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.lastModified() < modifyTime) {
                        oldFile = f;
                        modifyTime = f.lastModified();
                    }
                    File[] tmpFiles = f.listFiles();
                    if (tmpFiles != null) {
                        for (File t : tmpFiles) {
                            if (t.isFile()) {
                                size += t.length();
                            } else {
                                f.delete();
                            }
                        }
                    } else {
                        return;
                    }
                }
                f.delete();
            }
            if (size >= ((long) sTotalDirSize) && oldFile != null) {
                files = oldFile.listFiles();
                if (files != null) {
                    for (File f2 : files) {
                        f2.delete();
                    }
                }
                oldFile.delete();
            }
        }
    }

    private static void deleteOldFile(File file, long MaxSize) {
        int i = 0;
        File[] files = file.listFiles();
        if (files != null) {
            int length;
            File f;
            long size = 0;
            for (File f2 : files) {
                if (f2.isFile()) {
                    size += f2.length();
                } else {
                    f2.delete();
                }
            }
            if (size >= MaxSize) {
                File oldFile = null;
                long modifyTime = SubscriptionPlan.BYTES_UNLIMITED;
                length = files.length;
                while (i < length) {
                    f2 = files[i];
                    if (f2.lastModified() < modifyTime) {
                        oldFile = f2;
                        modifyTime = f2.lastModified();
                    }
                    i++;
                }
                oldFile.delete();
            }
        }
    }

    private static String chooseFile(String outFile) {
        int index = outFile.lastIndexOf(File.separatorChar);
        File[] files = new File(outFile.substring(0, index)).listFiles();
        File targetFile = null;
        long time = 0;
        String name = outFile.substring(index + 1);
        if (files != null) {
            for (File f : files) {
                String temp = f.getName();
                if (temp.startsWith(name)) {
                    temp = temp.substring(temp.lastIndexOf(File.separatorChar) + 1, temp.lastIndexOf(Session.SESSION_SEPARATION_CHAR_CHILD));
                    if (name.compareTo(temp.substring(0, temp.lastIndexOf(Session.SESSION_SEPARATION_CHAR_CHILD))) == 0 && f.lastModified() > time) {
                        time = f.lastModified();
                        targetFile = f;
                    }
                }
            }
        }
        if (targetFile != null) {
            if (targetFile.getName().compareTo(outFile.substring(index + 1) + Session.SESSION_SEPARATION_CHAR_CHILD + DF_DAY.format(new Date())) < 0) {
                targetFile = null;
            }
        }
        return targetFile != null ? targetFile.getName() : null;
    }

    public static boolean needCollection(String id) {
        if ("1".equals(parseXml(id))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0081 A:{SYNTHETIC, Splitter: B:44:0x0081} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String parseXml(String key) {
        Throwable th;
        String ret = "";
        if (key == null || "".equals(key)) {
            return ret;
        }
        boolean isFound = false;
        XmlPullParser parser = Xml.newPullParser();
        FileReader reader = null;
        try {
            FileReader reader2 = new FileReader(new File(CONFIG_FILE_EXT).exists() ? CONFIG_FILE_EXT : CONFIG_FILE);
            try {
                parser.setInput(reader2);
                for (int eventType = parser.getEventType(); eventType != 1 && (isFound ^ 1) != 0; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equalsIgnoreCase("string") && key.equals(parser.getAttributeValue(null, "name"))) {
                                ret = parser.nextText();
                                isFound = true;
                                break;
                            }
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reader = reader2;
            } catch (Exception e2) {
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return ret;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e4) {
        } catch (Throwable th3) {
            th = th3;
            if (reader != null) {
            }
            throw th;
        }
        return ret;
    }

    private static String createRecordHead(String appVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE).append(DF_NORMAL.format(new Date())).append(NEW_LINE).append(SystemProperties.get("ro.vivo.product.version", SmsConstants.FORMAT_UNKNOWN)).append(NEW_LINE);
        if (!TextUtils.isEmpty(appVersion)) {
            sb.append("versionName = ").append(appVersion).append(NEW_LINE);
        }
        return sb.toString();
    }
}
