package com.vivo.common.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class NumberLocalQuery {
    private static final String[] PREFIX = new String[]{"17951", "12593", "17911", "10193", "12520070", "12520026", "12520"};
    private static final String[] PREFIX_CHINA = new String[]{"0086", "86", "+86"};
    private static final boolean ROUTE = "yes".equals(SystemProperties.get("persist.sys.log.ctrl", "no"));
    private static final String TAG = "NumberLocalQuery";
    private static byte[][] fixed_names = null;
    private static int[] fixed_nums = null;
    private static byte[][] mobile_names = null;
    private static HashMap<Integer, Integer> prefix_nums;
    private static String resource = null;
    private static NumberLocalQuery sInstance;
    private static byte[][] special_names = null;
    private static int[] special_nums = null;
    private static int total_length;
    static String version = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("refresh.attr.data".equals(intent.getAction())) {
                if (NumberLocalQuery.ROUTE) {
                    Log.d(NumberLocalQuery.TAG, NumberLocalQuery.this.pkg + " || refresh ...");
                }
                NumberLocalQuery.this.prepareAttributionData();
            }
        }
    };
    private Context mContext = null;
    private InputStream mFileIn;
    private String pkg = null;

    public NumberLocalQuery(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            this.mContext = applicationContext;
        } else {
            this.mContext = context;
        }
        this.pkg = this.mContext.getApplicationInfo().packageName;
        Log.d(TAG, "init pkg is " + this.pkg);
        prepareAttributionData();
        registerReceiver(this.mContext);
    }

    private void initDbData() {
        total_length = 0;
        try {
            int i;
            byte[] temp = new byte[10];
            this.mFileIn.read(temp, 0, 10);
            version = new String(temp).trim();
            total_length += 10;
            if (ROUTE) {
                Log.d(TAG, this.pkg + " || use resource : " + resource + ", version = " + version);
            }
            this.mFileIn.read(temp, 0, 4);
            int total_length_prefix = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int prefix_count = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += total_length_prefix;
            if (ROUTE) {
                Log.d(TAG, "length_prefix = " + total_length_prefix + ", count_prefix = " + prefix_count);
            }
            prefix_nums = new HashMap();
            for (i = 0; i < prefix_count; i++) {
                this.mFileIn.read(temp, 0, 4);
                prefix_nums.put(Integer.valueOf((((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255)), Integer.valueOf(i));
            }
            this.mFileIn.read(temp, 0, 4);
            int special_total_length_num = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int special_count_num = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += special_total_length_num;
            if (ROUTE) {
                Log.d(TAG, "special_length_num = " + special_total_length_num + ", special_count_num = " + special_count_num);
            }
            special_nums = new int[special_count_num];
            for (i = 0; i < special_count_num; i++) {
                this.mFileIn.read(temp, 0, 4);
                special_nums[i] = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            }
            this.mFileIn.read(temp, 0, 4);
            int special_total_length_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int special_count_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += special_total_length_name;
            if (ROUTE) {
                Log.d(TAG, "special_length_name = " + special_total_length_name + ", special_count = " + special_count_name);
            }
            special_names = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{special_count_name, 60});
            for (i = 0; i < special_count_name; i++) {
                this.mFileIn.read(special_names[i], 0, 60);
            }
            this.mFileIn.read(temp, 0, 4);
            int fixed_total_length_num = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int fixed_count_num = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += fixed_total_length_num;
            if (ROUTE) {
                Log.d(TAG, "fixed_length_num = " + fixed_total_length_num + ", fixed_count_num = " + fixed_count_num);
            }
            fixed_nums = new int[fixed_count_num];
            for (i = 0; i < fixed_count_num; i++) {
                this.mFileIn.read(temp, 0, 4);
                fixed_nums[i] = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            }
            this.mFileIn.read(temp, 0, 4);
            int fixed_total_length_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int fixed_count_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += fixed_total_length_name;
            if (ROUTE) {
                Log.d(TAG, "fixed_length_name = " + fixed_total_length_name + ", fixed_count_name = " + fixed_count_name);
            }
            fixed_names = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{fixed_count_name, 60});
            for (i = 0; i < fixed_count_name; i++) {
                this.mFileIn.read(fixed_names[i], 0, 60);
            }
            this.mFileIn.read(temp, 0, 4);
            int mobile_total_length_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            this.mFileIn.read(temp, 0, 4);
            int mobile_count_name = (((temp[2] & 255) << 16) | ((temp[1] & 255) << 8)) | (temp[0] & 255);
            total_length += mobile_total_length_name;
            if (ROUTE) {
                Log.d(TAG, "mobile_length_name = " + mobile_total_length_name + ", mobile_count_name = " + mobile_count_name);
            }
            mobile_names = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{mobile_count_name, 60});
            for (i = 0; i < mobile_count_name; i++) {
                this.mFileIn.read(mobile_names[i], 0, 60);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public static NumberLocalQuery getInstance(Context context) {
        synchronized (NumberLocalQuery.class) {
            NumberLocalQuery numberLocalQuery;
            if (sInstance != null) {
                numberLocalQuery = sInstance;
                return numberLocalQuery;
            }
            sInstance = new NumberLocalQuery(context);
            numberLocalQuery = sInstance;
            return numberLocalQuery;
        }
    }

    private void initData() {
        this.mFileIn = chooseResource();
        if (this.mFileIn == null) {
            resource = "raw";
            this.mFileIn = this.mContext.getResources().openRawResource(50790403);
        }
    }

    private void deInitData() {
        if (this.mFileIn != null) {
            try {
                this.mFileIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable th) {
                this.mFileIn = null;
            }
            this.mFileIn = null;
        }
    }

    public void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("refresh.attr.data");
        context.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void finishQuery() {
        if (ROUTE) {
            Log.d(TAG, "finish Query");
        }
        deInitData();
    }

    public String queryCityNameByNumber(String number) {
        String result = null;
        if (isInternationalVersion()) {
            return result;
        }
        try {
            result = getQueryNumber(number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ROUTE) {
            Log.d(TAG, "query result is " + result);
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01a8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getQueryNumber(String number) {
        if (ROUTE) {
            Log.d(TAG, "begin checking number --> " + number);
        }
        number = stripSeparators(number);
        if (number == null || number.length() < 3) {
            if (ROUTE) {
                Log.d(TAG, "return null, number = " + number);
            }
            return null;
        }
        int i;
        boolean chinaPrefix = false;
        for (i = 0; i < PREFIX.length; i++) {
            if (number.startsWith(PREFIX[i])) {
                number = number.substring(PREFIX[i].length());
                break;
            }
        }
        if ("true".equals(SystemProperties.get("gsm.operator.isroaming"))) {
            if (ROUTE) {
                Log.d(TAG, "sim1 roaming, return null...");
            }
            return null;
        } else if ("true".equals(SystemProperties.get("gsm.operator.isroaming.2"))) {
            if (ROUTE) {
                Log.d(TAG, "sim2 roaming, return null...");
            }
            return null;
        } else if (number.startsWith("00") && (number.startsWith("0086") ^ 1) != 0) {
            return null;
        } else {
            String localtionStr;
            boolean is86Prefix = false;
            for (i = 0; i < PREFIX_CHINA.length; i++) {
                if (number.startsWith(PREFIX_CHINA[i])) {
                    chinaPrefix = true;
                    if (number.startsWith("86")) {
                        is86Prefix = true;
                        if (number.length() <= 8) {
                            return null;
                        }
                    }
                    number = number.substring(PREFIX_CHINA[i].length());
                    if (number.length() > 2 && number.length() < 6 && TextUtils.isDigitsOnly(number)) {
                        if (ROUTE) {
                            Log.d(TAG, "to special...");
                        }
                        localtionStr = number.substring(0, number.length());
                        if (special_nums != null || special_names == null) {
                            if (special_nums != null) {
                                if (ROUTE) {
                                    Log.d(TAG, "special_nums == null");
                                }
                            } else if (ROUTE) {
                                Log.d(TAG, "special_names == null");
                            }
                            return null;
                        }
                        for (int j = 0; j < special_nums.length; j++) {
                            if (localtionStr.equals(String.valueOf(special_nums[j]))) {
                                return new String(special_names[j]).trim();
                            }
                        }
                    }
                    int fixNumber;
                    if (number.startsWith("0") && TextUtils.isDigitsOnly(number.substring(1, 3))) {
                        if (ROUTE) {
                            Log.d(TAG, "to fixed...");
                        }
                        int length = number.length();
                        if (length > 20) {
                            return null;
                        }
                        if (length > 10 && (TextUtils.isDigitsOnly(number.substring(0, 11)) ^ 1) != 0) {
                            return null;
                        }
                        if (number.startsWith("01") || number.startsWith("02")) {
                            number = number.substring(1, 3);
                        } else if (!TextUtils.isDigitsOnly(number.substring(1, 4))) {
                            return null;
                        } else {
                            number = number.substring(1, 4);
                        }
                        fixNumber = Integer.valueOf(number).intValue();
                        if (fixed_nums == null || fixed_names == null) {
                            if (fixed_nums == null) {
                                if (ROUTE) {
                                    Log.d(TAG, "fixed_nums == null");
                                }
                            } else if (ROUTE) {
                                Log.d(TAG, "fixed_names == null");
                            }
                            return null;
                        }
                        for (i = 0; i < fixed_nums.length; i++) {
                            if (fixed_nums[i] == fixNumber) {
                                return new String(fixed_names[i]).trim();
                            }
                        }
                        return null;
                    } else if (number.startsWith("1") && number.length() > 1 && (number.substring(1, 2).equals("0") ^ 1) != 0) {
                        if (ROUTE) {
                            Log.d(TAG, "to mobile...");
                        }
                        if (number.length() < 7 || number.length() > 11 || (TextUtils.isDigitsOnly(number) ^ 1) != 0) {
                            if (number.length() < 7 || number.length() > 11) {
                                if (ROUTE) {
                                    Log.d(TAG, "return null, number.length = " + number.length());
                                }
                            } else if (!TextUtils.isDigitsOnly(number) && ROUTE) {
                                Log.d(TAG, "!TextUtils.isDigitsOnly(number)");
                            }
                            return null;
                        }
                        String replace = String.valueOf(prefix_nums.get(Integer.valueOf(number.substring(0, 3))));
                        if (ROUTE) {
                            Log.d(TAG, "replace = " + replace);
                        }
                        if (replace.equals("null")) {
                            return null;
                        }
                        number = replace + number.substring(3, 7);
                        long index = (long) Integer.valueOf(number).intValue();
                        byte[] cityIndexByte = new byte[4];
                        try {
                            if ("raw".equals(resource)) {
                                if (this.mFileIn == null) {
                                    this.mFileIn = this.mContext.getResources().openRawResource(50790403);
                                }
                                this.mFileIn.reset();
                            } else {
                                if (this.mFileIn != null) {
                                    this.mFileIn.close();
                                }
                                this.mFileIn = new FileInputStream(new File("data/bbkcore/attribution/numberdb"));
                            }
                            this.mFileIn.skip(((long) total_length) + (4 * index));
                            this.mFileIn.read(cityIndexByte, 0, 4);
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (ROUTE) {
                                Log.d(TAG, "read mobile index from db failed!!!");
                            }
                        }
                        int cityIndex = ((((cityIndexByte[2] & 255) << 16) | ((cityIndexByte[1] & 255) << 8)) | (cityIndexByte[0] & 255)) - 1;
                        if (ROUTE) {
                            Log.d(TAG, "number = " + number + ", index = " + index + ", cityIndex = " + cityIndex);
                        }
                        if (cityIndex < 0 || cityIndex > mobile_names.length) {
                            if (ROUTE) {
                                Log.d(TAG, "return null, cityIndex = " + cityIndex);
                            }
                            return null;
                        }
                        if (ROUTE) {
                            Log.d(TAG, "end checking, return " + new String(mobile_names[cityIndex]).trim());
                        }
                        return new String(mobile_names[cityIndex]).trim();
                    } else if (!chinaPrefix) {
                        return null;
                    } else {
                        if (ROUTE) {
                            Log.d(TAG, "to chinaPrefix and fixed again...");
                        }
                        if (number.length() < 10 || number.length() > 20) {
                            return null;
                        }
                        if (is86Prefix && (TextUtils.isDigitsOnly(number.substring(0, 7)) ^ 1) != 0) {
                            return null;
                        }
                        if (!TextUtils.isDigitsOnly(number.substring(0, 10))) {
                            return null;
                        }
                        if (TextUtils.isDigitsOnly(number.substring(0, 2))) {
                            if (number.startsWith("1") || number.startsWith("2")) {
                                number = number.substring(0, 2);
                            } else if (!TextUtils.isDigitsOnly(number.substring(0, 3))) {
                                return null;
                            } else {
                                number = number.substring(0, 3);
                            }
                            if (fixed_nums == null) {
                                Log.w(TAG, "fixed_nums == null");
                                return null;
                            } else if (fixed_names == null) {
                                Log.w(TAG, "fixed_names == null");
                                return null;
                            } else {
                                fixNumber = Integer.valueOf(number).intValue();
                                for (i = 0; i < fixed_nums.length; i++) {
                                    if (fixed_nums[i] == fixNumber) {
                                        return new String(fixed_names[i]).trim();
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }
            }
            if (ROUTE) {
            }
            localtionStr = number.substring(0, number.length());
            if (special_nums != null) {
            }
            if (special_nums != null) {
            }
            return null;
        }
    }

    private String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    private boolean isNonSeparator(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N' || c == 'w' || c == 'p' || c == ',' || c == ';') {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00dc A:{SYNTHETIC, Splitter: B:45:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d0 A:{SYNTHETIC, Splitter: B:38:0x00d0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputStream chooseResource() {
        Exception e;
        Throwable th;
        File dataFile = new File("data/bbkcore/attribution/numberdb");
        InputStream inputStream = null;
        try {
            if (!new File("data/bbkcore/attribution").exists() || (dataFile.exists() ^ 1) != 0) {
                return null;
            }
            FileInputStream dataStream = new FileInputStream(dataFile);
            FileInputStream dataStream2;
            try {
                byte[] dataTemp = new byte[10];
                dataStream.read(dataTemp, 0, 10);
                String dataVersion = new String(dataTemp).trim();
                if (dataVersion == null || (isVersionAvailable(dataVersion) ^ 1) != 0) {
                    dataStream.close();
                    return null;
                }
                inputStream = this.mContext.getResources().openRawResource(50790403);
                byte[] temp = new byte[10];
                inputStream.read(temp, 0, 10);
                String rawVersion = new String(temp).trim();
                if (ROUTE) {
                    Log.d(TAG, "rawVersion = " + rawVersion + ", dataVersion = " + dataVersion + ", dataStream = " + dataStream);
                }
                inputStream.close();
                if (rawVersion.compareTo(dataVersion) <= 0) {
                    resource = "data";
                    dataStream.close();
                    dataStream2 = new FileInputStream(dataFile);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    return dataStream2;
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                return null;
            } catch (Exception e3) {
                e22 = e3;
                dataStream2 = dataStream;
                try {
                    e22.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e2222 = e4;
            e2222.printStackTrace();
            if (inputStream != null) {
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:83:0x01b9 A:{SYNTHETIC, Splitter: B:83:0x01b9} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01be A:{Catch:{ Exception -> 0x01c2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01b9 A:{SYNTHETIC, Splitter: B:83:0x01b9} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01be A:{Catch:{ Exception -> 0x01c2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01ca A:{SYNTHETIC, Splitter: B:91:0x01ca} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01cf A:{Catch:{ Exception -> 0x01d3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01ca A:{SYNTHETIC, Splitter: B:91:0x01ca} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01cf A:{Catch:{ Exception -> 0x01d3 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void replaceDataResourceWithDownload() {
        Exception e;
        Throwable th;
        deInitData();
        long start = System.currentTimeMillis();
        File dataFile = new File("data/bbkcore/attribution/numberdb");
        File dataDir = new File("data/bbkcore/attribution");
        File SDFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "attribution" + File.separator + "numberdb2.bin");
        FileInputStream dataStream = null;
        FileInputStream SDStream = null;
        byte[] temp = new byte[10];
        try {
            if (SDFile.exists()) {
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                    String[] cmds = new String[3];
                    cmds[0] = "sh";
                    cmds[1] = "-c";
                    Runtime rt = Runtime.getRuntime();
                    cmds[2] = "chmod 777 " + dataDir.getAbsolutePath();
                    rt.exec(cmds);
                }
                if (dataFile.exists()) {
                    String SDVersion;
                    FileInputStream dataStream2;
                    FileInputStream SDStream2 = new FileInputStream(SDFile);
                    try {
                        SDStream2.read(temp, 0, 10);
                        SDVersion = new String(temp).trim();
                        dataStream2 = new FileInputStream(dataFile);
                    } catch (Exception e2) {
                        e = e2;
                        SDStream = SDStream2;
                        try {
                            e.printStackTrace();
                            if (dataStream != null) {
                            }
                            if (SDStream != null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (dataStream != null) {
                            }
                            if (SDStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        SDStream = SDStream2;
                        if (dataStream != null) {
                            try {
                                dataStream.close();
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                throw th;
                            }
                        }
                        if (SDStream != null) {
                            SDStream.close();
                        }
                        throw th;
                    }
                    try {
                        dataStream2.read(temp, 0, 10);
                        String dataVersion = new String(temp).trim();
                        if (!isVersionAvailable(SDVersion) || (isVersionAvailable(dataVersion) ^ 1) == 0 || copyFileThroughChannel(SDFile, dataFile)) {
                            dataStream = new FileInputStream(dataFile);
                            dataStream.read(temp, 0, 10);
                            dataVersion = new String(temp).trim();
                            if (dataVersion.compareTo(SDVersion) < 0) {
                                if (copyFileThroughChannel(SDFile, dataFile)) {
                                    dataStream2 = new FileInputStream(dataFile);
                                    dataStream2.read(temp, 0, 10);
                                    dataVersion = new String(temp).trim();
                                    dataStream = dataStream2;
                                } else {
                                    if (dataStream != null) {
                                        try {
                                            dataStream.close();
                                        } catch (Exception e32) {
                                            e32.printStackTrace();
                                        }
                                    }
                                    if (SDStream2 != null) {
                                        SDStream2.close();
                                    }
                                    return;
                                }
                            }
                            if (dataVersion.compareTo(SDVersion) != 0 || dataFile.length() >= SDFile.length()) {
                                SDStream = SDStream2;
                            } else if (copyFileThroughChannel(SDFile, dataFile)) {
                                SDStream = SDStream2;
                            } else {
                                if (dataStream != null) {
                                    try {
                                        dataStream.close();
                                    } catch (Exception e322) {
                                        e322.printStackTrace();
                                    }
                                }
                                if (SDStream2 != null) {
                                    SDStream2.close();
                                }
                                return;
                            }
                        }
                        if (dataStream2 != null) {
                            try {
                                dataStream2.close();
                            } catch (Exception e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        if (SDStream2 != null) {
                            SDStream2.close();
                        }
                        return;
                    } catch (Exception e4) {
                        e3222 = e4;
                        SDStream = SDStream2;
                        dataStream = dataStream2;
                        e3222.printStackTrace();
                        if (dataStream != null) {
                        }
                        if (SDStream != null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        SDStream = SDStream2;
                        dataStream = dataStream2;
                        if (dataStream != null) {
                        }
                        if (SDStream != null) {
                        }
                        throw th;
                    }
                } else if (!copyFileThroughChannel(SDFile, dataFile)) {
                    return;
                }
                if (dataStream != null) {
                    try {
                        dataStream.close();
                    } catch (Exception e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (SDStream != null) {
                    SDStream.close();
                }
            }
        } catch (Exception e5) {
            e32222 = e5;
            e32222.printStackTrace();
            if (dataStream != null) {
                try {
                    dataStream.close();
                } catch (Exception e322222) {
                    e322222.printStackTrace();
                }
            }
            if (SDStream != null) {
                SDStream.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0147 A:{SYNTHETIC, Splitter: B:44:0x0147} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x014c A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0151 A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0156 A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0147 A:{SYNTHETIC, Splitter: B:44:0x0147} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x014c A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0151 A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0156 A:{Catch:{ Exception -> 0x0167 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x016f A:{SYNTHETIC, Splitter: B:61:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0174 A:{Catch:{ Exception -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0179 A:{Catch:{ Exception -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x017e A:{Catch:{ Exception -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x016f A:{SYNTHETIC, Splitter: B:61:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0174 A:{Catch:{ Exception -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0179 A:{Catch:{ Exception -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x017e A:{Catch:{ Exception -> 0x0182 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean copyFileThroughChannel(File source, File target) {
        Exception e;
        Throwable th;
        if (checkFileIntegrity(source)) {
            Log.d(TAG, "copy...");
            int length = 2097152;
            long totalLength = 0;
            FileInputStream in = null;
            long sourceLength = source.length();
            FileOutputStream out = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            try {
                FileOutputStream out2;
                if (!target.exists()) {
                    target.createNewFile();
                    String[] cmds = new String[3];
                    cmds[0] = "sh";
                    cmds[1] = "-c";
                    Runtime rt = Runtime.getRuntime();
                    cmds[2] = "chmod 777 " + target.getAbsolutePath();
                    rt.exec(cmds);
                }
                FileInputStream in2 = new FileInputStream(source);
                try {
                    out2 = new FileOutputStream(target);
                } catch (Exception e2) {
                    e = e2;
                    in = in2;
                    try {
                        Log.d(TAG, "copy source " + source.getPath() + " from " + target.getPath() + " failed...");
                        e.printStackTrace();
                        if (inChannel != null) {
                            try {
                                inChannel.close();
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                target.delete();
                                return false;
                            }
                        }
                        if (outChannel != null) {
                            outChannel.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        target.delete();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inChannel != null) {
                        }
                        if (outChannel != null) {
                        }
                        if (in != null) {
                        }
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (inChannel != null) {
                        try {
                            inChannel.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                            throw th;
                        }
                    }
                    if (outChannel != null) {
                        outChannel.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    throw th;
                }
                try {
                    inChannel = in2.getChannel();
                    outChannel = out2.getChannel();
                    while (inChannel.position() != inChannel.size()) {
                        if (inChannel.size() - inChannel.position() < ((long) length)) {
                            length = (int) (inChannel.size() - inChannel.position());
                        }
                        if (ROUTE) {
                            Log.d(TAG, "size:" + inChannel.size() + ", position:" + inChannel.position() + ", length:" + length);
                        }
                        totalLength += inChannel.transferTo(inChannel.position(), (long) length, outChannel);
                        inChannel.position(inChannel.position() + ((long) length));
                    }
                    boolean z = totalLength == sourceLength;
                    if (inChannel != null) {
                        try {
                            inChannel.close();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (outChannel != null) {
                        outChannel.close();
                    }
                    if (in2 != null) {
                        in2.close();
                    }
                    if (out2 != null) {
                        out2.close();
                    }
                    return z;
                } catch (Exception e4) {
                    e322 = e4;
                    out = out2;
                    in = in2;
                    Log.d(TAG, "copy source " + source.getPath() + " from " + target.getPath() + " failed...");
                    e322.printStackTrace();
                    if (inChannel != null) {
                    }
                    if (outChannel != null) {
                    }
                    if (in != null) {
                    }
                    if (out != null) {
                    }
                    target.delete();
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    out = out2;
                    in = in2;
                    if (inChannel != null) {
                    }
                    if (outChannel != null) {
                    }
                    if (in != null) {
                    }
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e322 = e5;
                Log.d(TAG, "copy source " + source.getPath() + " from " + target.getPath() + " failed...");
                e322.printStackTrace();
                if (inChannel != null) {
                }
                if (outChannel != null) {
                }
                if (in != null) {
                }
                if (out != null) {
                }
                target.delete();
                return false;
            }
        }
        Log.w(TAG, "check file checkFileIntegrity failed");
        source.delete();
        return false;
    }

    private void prepareAttributionData() {
        deInitData();
        initData();
        initDbData();
    }

    public boolean isVersionAvailable(String version) {
        int i = 0;
        boolean result = true;
        if (version == null) {
            result = false;
        }
        String[] ss = version.replace(".", "@").split("@");
        int length = ss.length;
        while (i < length) {
            try {
                int i2 = Integer.valueOf(ss[i]).intValue();
                if (i2 < 0 || 9 < i2) {
                    result = false;
                }
                i++;
            } catch (Exception e) {
                result = false;
            }
        }
        if (ROUTE) {
            Log.d(TAG, "is version available ? " + result);
        }
        return result;
    }

    public boolean isInternationalVersion() {
        return "yes".equalsIgnoreCase(SystemProperties.get("ro.vivo.product.overseas", "no"));
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x007c A:{SYNTHETIC, Splitter: B:41:0x007c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkFileIntegrity(File file) {
        IOException e;
        Throwable th;
        RandomAccessFile randomFile = null;
        try {
            String END_FLAG = "$$$$";
            RandomAccessFile randomFile2 = new RandomAccessFile(file, "r");
            try {
                randomFile2.seek(randomFile2.length() - 5);
                byte[] bytes = new byte[5];
                randomFile2.readFully(bytes);
                if (bytes == null || bytes.length == 0) {
                    if (randomFile2 != null) {
                        try {
                            randomFile2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    return false;
                }
                String value = new String(bytes);
                Log.d(TAG, "file end value is " + value);
                if (value.startsWith(END_FLAG)) {
                    if (randomFile2 != null) {
                        try {
                            randomFile2.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return true;
                }
                if (randomFile2 != null) {
                    try {
                        randomFile2.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                randomFile = randomFile2;
                return false;
            } catch (IOException e3) {
                e222 = e3;
                randomFile = randomFile2;
            } catch (Throwable th2) {
                th = th2;
                randomFile = randomFile2;
                if (randomFile != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e222 = e4;
            try {
                e222.printStackTrace();
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }
}
