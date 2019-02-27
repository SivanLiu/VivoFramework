package com.android.internal.app;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.UserManager;
import android.util.AtomicFile;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ResolverHelper {
    private static final boolean DEBUG = false;
    public static final String INTENT_EXTRA_RESOLVEINFO = "shareResolveInfo";
    private static final String JSON_LABEL_NAME = "name";
    private static final String JSON_LABEL_PKG = "pkg";
    private static final String JSON_LABEL_ROOT = "shareInfo";
    private static final String JSON_LABEL_TYPE = "type";
    public static final int PACAKGE_INFO_TYPE_DOUBLE_INSTANCE = 1;
    public static final int PAKCAGE_INFO_TYPE_NORMAL = 0;
    private static final String SHARE_INFO_FILE_NAME = "share_sort_info.json";
    private static final String TAG = "ResolverHelper";
    private static int mDoubleInstanceUserId = -10000;

    public static class SharedPackageInfo {
        public Drawable icon;
        public CharSequence label;
        public String name = "<Unknown>";
        public String packageName = "<Unknown>";
        public ResolveInfo ri;
        public int type = 0;

        public void setType(Context context) {
            this.type = resolveType(context, this.ri);
        }

        public boolean isConsistent(Context context, ResolveInfo ri) {
            boolean z = false;
            if (ri == null) {
                return false;
            }
            int resolvedType = resolveType(context, ri);
            if (this.packageName.equals(ri.activityInfo.packageName) && this.name.equals(ri.activityInfo.name) && this.type == resolvedType) {
                z = true;
            }
            return z;
        }

        private int resolveType(Context context, ResolveInfo ri) {
            int type = 0;
            if (ri == null) {
                return 0;
            }
            if (ResolverHelper.mDoubleInstanceUserId == -10000) {
                ResolverHelper.mDoubleInstanceUserId = ((UserManager) context.getSystemService("user")).getDoubleAppUserId();
            }
            if (ri.targetUserId == ResolverHelper.mDoubleInstanceUserId) {
                type = 1;
            }
            return type;
        }
    }

    private static String getConfigFilePath() {
        String path = "/data/android/";
        return Environment.getDataDirectory() + "/data/android/" + File.separator + SHARE_INFO_FILE_NAME;
    }

    public static void writeShareSortConfFile(List<SharedPackageInfo> infos) {
        Exception e;
        String filePath = getConfigFilePath();
        if (infos.size() <= 0) {
            Log.i(TAG, "no valide data. ignore save aciont...");
        } else if (checkWriteCondition(filePath)) {
            JSONObject rootObj = new JSONObject();
            try {
                JSONArray arrayObj = new JSONArray();
                rootObj.put(JSON_LABEL_ROOT, arrayObj);
                for (SharedPackageInfo info : infos) {
                    JSONObject obj = new JSONObject();
                    obj.put(JSON_LABEL_PKG, info.packageName);
                    obj.put("name", info.name);
                    obj.put("type", info.type);
                    arrayObj.put(obj);
                }
                AtomicFile file = new AtomicFile(new File(filePath));
                try {
                    FileOutputStream outStream = file.startWrite();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    try {
                        String str = rootObj.toString();
                        writer.write(str, 0, str.length());
                        BufferedWriter bufferedWriter;
                        try {
                            writer.close();
                            file.finishWrite(outStream);
                            bufferedWriter = writer;
                        } catch (Exception e2) {
                            e = e2;
                            bufferedWriter = writer;
                            Log.e(TAG, "open file failed : " + filePath, e);
                        }
                    } catch (IOException e3) {
                        file.failWrite(outStream);
                        throw e3;
                    } catch (Throwable th) {
                        writer.close();
                    }
                } catch (Exception e4) {
                    e = e4;
                    Log.e(TAG, "open file failed : " + filePath, e);
                }
            } catch (JSONException e5) {
                Log.e(TAG, "construct json data failed", e5);
            }
        }
    }

    private static boolean checkWriteCondition(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "create new file failed : " + path + ". please check your system");
                return false;
            }
        }
        if (!file.isFile()) {
            Log.e(TAG, path + "is not file, please ensure your path");
            return false;
        } else if (file.canWrite()) {
            return true;
        } else {
            Log.e(TAG, path + " can't be write, please ensure your permission, ignore...");
            return false;
        }
    }

    public static ArrayList<SharedPackageInfo> readShareSortConfigFile() {
        FileNotFoundException e;
        String filePath = getConfigFilePath();
        ArrayList<SharedPackageInfo> infos = new ArrayList();
        if (!checkReadCondition(filePath)) {
            return infos;
        }
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        builder.append(line);
                    } else {
                        try {
                            break;
                        } catch (IOException e2) {
                        }
                    }
                } catch (IOException e3) {
                    Log.e(TAG, "read file error : " + filePath, e3);
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                    return infos;
                } catch (Throwable th) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                    }
                    try {
                        throw th;
                    } catch (FileNotFoundException e6) {
                        e = e6;
                        BufferedReader bufferedReader = reader;
                    }
                }
            }
            reader.close();
            try {
                JSONArray rootObj = ((JSONObject) new JSONTokener(builder.toString()).nextValue()).getJSONArray(JSON_LABEL_ROOT);
                int i = 0;
                while (rootObj != null && i < rootObj.length()) {
                    JSONObject obj = rootObj.getJSONObject(i);
                    SharedPackageInfo info = new SharedPackageInfo();
                    info.packageName = obj.getString(JSON_LABEL_PKG);
                    info.name = obj.getString("name");
                    info.type = obj.getInt("type");
                    info.type = 1;
                    if (obj.has("type")) {
                        info.type = obj.getInt("type");
                    }
                    info.type = 1;
                    if (obj.has("type")) {
                        info.type = obj.getInt("type");
                    }
                    infos.add(info);
                    i++;
                }
            } catch (JSONException e7) {
                Log.e(TAG, "parse json failed", e7);
            }
            return infos;
        } catch (FileNotFoundException e8) {
            e = e8;
            Log.e(TAG, "handleRestoreData open error : " + filePath, e);
            return infos;
        }
    }

    private static boolean checkReadCondition(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, path + " not exists, ignore...");
            return false;
        } else if (!file.isFile()) {
            Log.e(TAG, path + "is not file, please ensure your path");
            return false;
        } else if (file.canRead()) {
            return true;
        } else {
            Log.e(TAG, path + " can't be read, please ensure your permission, ignore...");
            return false;
        }
    }
}
