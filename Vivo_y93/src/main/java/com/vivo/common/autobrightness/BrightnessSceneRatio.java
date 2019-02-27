package com.vivo.common.autobrightness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.vivo.common.autobrightness.AppClassify.AppType;
import com.vivo.common.autobrightness.AppClassify.Pare;
import com.vivo.common.provider.Calendar.Events;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BrightnessSceneRatio {
    private static final String CONFIG_ENGINE_VERSION = "1.0";
    private static final String CONFIG_IDENTIFY = "app_bright_raio_configuration";
    private static final String CONFIG_MODULE = "AutoBrightness";
    private static final String CONFIG_TYPE = "1";
    private static final String CONFIG_URI = "content://com.vivo.daemonservice.unifiedconfigprovider/configs";
    public static final int[] DEFAULT_RATIO = new int[]{-1};
    private static final String INTENT_CONFIG = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_AutoBrightness";
    private static final String INTENT_PREFIX = "com.vivo.daemonService.unifiedconfig.update_finish_broadcast_";
    private static final String KEY_COMP = "compare";
    private static final String KEY_DATE = "date";
    private static final String KEY_EDITION = "edition";
    private static final String KEY_ENABLE = "enable";
    private static final String KEY_PKGS = "pkgs";
    private static final String KEY_PKG_CAT = "pkgCat";
    private static final String KEY_PKG_CONF = "pkgConf";
    private static final String KEY_PROJ = "aProj";
    private static final String KEY_PROMOTE = "promote";
    private static final String KEY_THRES = "thres";
    private static final String KEY_VGFLAG = "vgf";
    private static final int MSG_READ_CONFIGRATION = 1;
    private static final int MSG_SETUP_RECEIVER = 2;
    private static final String TAG = "BrightnessSceneRatio";
    private static final String VAL_EQUALS = "equals";
    private static final String VAL_STARTSWITH = "startsWith";
    private static float[] defaultGameRatio = new float[]{-1.0f};
    private static int[] defaultGameThres = new int[]{-1};
    private static float[] defaultMobaRatio = new float[]{6.0f, 11.0f, 20.0f, 25.0f, 30.0f, 30.0f, 1.0f};
    private static int[] defaultMobaThres = new int[]{2, 3, 10, 15, 20, 25, 30, 60};
    private static float[] defaultPubgRatio = new float[]{8.0f, 11.0f, 20.0f, 25.0f, 30.0f, 30.0f, 1.0f};
    private static int[] defaultPubgThres = new int[]{2, 3, 10, 15, 20, 25, 30, 60};
    private static float[] defaultVideoRatio = new float[]{-1.0f};
    private static int[] defaultVideoThres = new int[]{-1};
    private static String mConfigDate = "unkown";
    private static RatioConfig[] mConfigs = new RatioConfig[]{new RatioConfig(AppType.TYPE_VIDEO, defaultVideoThres, defaultVideoRatio), new RatioConfig(AppType.TYPE_GAME, defaultGameThres, defaultGameRatio), new RatioConfig(AppType.TYPE_MOBA_GAME, defaultMobaThres, defaultMobaRatio), new RatioConfig(AppType.TYPE_PUBG_GAME, defaultPubgThres, defaultPubgRatio)};
    private Context mContext = null;
    private MyHandler mHandler = null;
    private IntentReceiver mIntentReceiver = null;
    private int mParseRetry = 0;

    private class IntentReceiver extends BroadcastReceiver {
        /* synthetic */ IntentReceiver(BrightnessSceneRatio this$0, IntentReceiver -this1) {
            this();
        }

        private IntentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    Slog.d(BrightnessSceneRatio.TAG, "got intent ACTION_BOOT_COMPLETED");
                    BrightnessSceneRatio.this.mHandler.sendEmptyMessageDelayed(1, 5000);
                } else if (action.equals(BrightnessSceneRatio.INTENT_CONFIG)) {
                    Slog.d(BrightnessSceneRatio.TAG, "got intent INTENT_CONFIG");
                    Slog.d(BrightnessSceneRatio.TAG, "INTENT_CONFIG bundle:" + intent.getExtras().toString());
                    BrightnessSceneRatio.this.mHandler.removeMessages(1);
                    BrightnessSceneRatio.this.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    BrightnessSceneRatio.this.getConfig(BrightnessSceneRatio.CONFIG_URI, BrightnessSceneRatio.CONFIG_MODULE, BrightnessSceneRatio.CONFIG_TYPE, BrightnessSceneRatio.CONFIG_ENGINE_VERSION, BrightnessSceneRatio.CONFIG_IDENTIFY);
                    return;
                case 2:
                    BrightnessSceneRatio.this.setupReceiver();
                    return;
                default:
                    return;
            }
        }
    }

    private static class RatioConfig {
        float[] ratioArray;
        int[] thresArray;
        String type;

        public RatioConfig(String type, int[] thresArray, float[] ratioArray) {
            this.type = type;
            this.thresArray = thresArray;
            this.ratioArray = ratioArray;
        }

        /* JADX WARNING: Removed duplicated region for block: B:9:0x0045  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getTargetBrightness(int brightnessOnFullScale) {
            int brightness = AblConfig.getMapping2048GrayScaleTo256GrayScaleRestore(brightnessOnFullScale);
            int target = brightness;
            try {
                if (!(this.thresArray == null || this.ratioArray == null)) {
                    if (this.thresArray.length >= 1 && this.ratioArray.length >= 1 && !((this.thresArray.length == 1 && this.thresArray[0] == -1) || (this.ratioArray.length == 1 && this.ratioArray[0] == -1.0f))) {
                        int idx = BrightnessSceneRatio.binarySearchRange(this.thresArray, brightness);
                        if (idx == this.ratioArray.length) {
                            target = brightness;
                        } else if (idx < 0) {
                            target = (int) (((double) brightness) * 1.2d);
                        } else if (idx == this.ratioArray.length - 1) {
                            target = (int) (((float) brightness) * this.ratioArray[idx]);
                        } else {
                            target = (int) this.ratioArray[idx];
                            if (brightness >= target) {
                                target = (int) (((double) brightness) * 1.2d);
                            }
                        }
                        BrightnessSceneRatio.log("getAppBrightPromotion brightness = " + brightness + ", brightnessOnFullScale = " + brightnessOnFullScale + ", target = " + target);
                        if (target > 255) {
                            target = 255;
                        }
                        return AblConfig.getMapping2048GrayScaleFrom256GrayScale(target);
                    }
                }
                target = brightness;
                BrightnessSceneRatio.log("getAppBrightPromotion brightness = " + brightness + ", brightnessOnFullScale = " + brightnessOnFullScale + ", target = " + target);
                if (target > 255) {
                }
                return AblConfig.getMapping2048GrayScaleFrom256GrayScale(target);
            } catch (Exception e) {
                e.printStackTrace();
                Slog.e(BrightnessSceneRatio.TAG, "get brightness exception!");
                if (target > 255) {
                    target = 255;
                }
                return AblConfig.getMapping2048GrayScaleFrom256GrayScale(target);
            } catch (Throwable th) {
                if (target > 255) {
                    target = 255;
                }
                return AblConfig.getMapping2048GrayScaleFrom256GrayScale(target);
            }
        }
    }

    public BrightnessSceneRatio(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new MyHandler(looper);
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(2);
        }
        Slog.d(TAG, "constructor called.");
    }

    private void setupReceiver() {
        this.mIntentReceiver = new IntentReceiver(this, null);
        IntentFilter f1 = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        f1.addAction(INTENT_CONFIG);
        this.mContext.registerReceiver(this.mIntentReceiver, f1);
    }

    private void parseConfiguration(String confContent) {
        Slog.d(TAG, "pareseConfiguration\n" + confContent);
        ArrayList<RatioConfig> ratioArrayList = new ArrayList();
        ArrayList<Pare> pareArrayList = new ArrayList();
        ArrayList<String> vgFlagArrayList = new ArrayList();
        String PROJECT = SystemProperties.get("ro.vivo.product.model", "unkown");
        try {
            JSONArray jSONArray = new JSONArray(confContent);
            int i = 0;
            while (i < jSONArray.length()) {
                JSONObject conf = jSONArray.getJSONObject(i);
                String edition = conf.getString(KEY_EDITION);
                String date = conf.getString("date");
                if (edition.equals(CONFIG_ENGINE_VERSION)) {
                    int o;
                    JSONArray aProjs = conf.getJSONArray(KEY_PROJ);
                    String comp = conf.getString(KEY_COMP);
                    boolean enable = conf.getBoolean(KEY_ENABLE);
                    boolean match = false;
                    for (int j = 0; j < aProjs.length(); j++) {
                        String prj = aProjs.getString(j);
                        if (!VAL_STARTSWITH.equals(comp)) {
                            match = PROJECT.equals(prj);
                            if (match) {
                                break;
                            }
                        } else {
                            match = PROJECT.startsWith(prj);
                            if (match) {
                                break;
                            }
                        }
                    }
                    if (match) {
                        JSONArray catsArray = conf.getJSONArray(KEY_PKG_CAT);
                        JSONObject pkgConfObj = conf.getJSONObject(KEY_PKG_CONF);
                        if (catsArray.length() < 1) {
                            throw new JSONException("catsArray length invalid:" + catsArray.length());
                        }
                        String[] cats = new String[catsArray.length()];
                        for (int l = 0; l < catsArray.length(); l++) {
                            cats[l] = catsArray.getString(l);
                        }
                        for (int m = 0; m < cats.length; m++) {
                            JSONObject pareObj = pkgConfObj.getJSONObject(cats[m]);
                            boolean vgFlag = pareObj.getBoolean(KEY_VGFLAG);
                            JSONArray thresArray = pareObj.getJSONArray(KEY_THRES);
                            JSONArray promoteArray = pareObj.getJSONArray(KEY_PROMOTE);
                            JSONArray pkgsArray = pareObj.getJSONArray(KEY_PKGS);
                            if (pkgsArray.length() < 1) {
                                throw new JSONException("pkgsArray length invalid.");
                            }
                            String[] pkgs = new String[pkgsArray.length()];
                            for (int n = 0; n < pkgsArray.length(); n++) {
                                pkgs[n] = pkgsArray.getString(n);
                            }
                            if (thresArray.length() < 1) {
                                throw new JSONException("thresArray length invalid.");
                            }
                            int[] thres = new int[thresArray.length()];
                            for (o = 0; o < thresArray.length(); o++) {
                                thres[o] = thresArray.getInt(o);
                            }
                            if (promoteArray.length() < 1) {
                                throw new JSONException("promoteArray length invalid.");
                            }
                            float[] promote = new float[promoteArray.length()];
                            for (int p = 0; p < promoteArray.length(); p++) {
                                promote[p] = (float) promoteArray.getDouble(p);
                            }
                            pareArrayList.add(new Pare(cats[m], pkgs));
                            ratioArrayList.add(new RatioConfig(cats[m], thres, promote));
                            if (vgFlag) {
                                vgFlagArrayList.add(cats[m]);
                            }
                        }
                    }
                    if (!match) {
                        i++;
                    } else if (pareArrayList.size() < 0 || ratioArrayList.size() < 0) {
                        throw new JSONException("invalid pareList.size=" + pareArrayList.size() + " ratioList.size=" + ratioArrayList.size());
                    } else {
                        mConfigDate = date;
                        Pare[] pareList = new Pare[pareArrayList.size()];
                        for (o = 0; o < pareArrayList.size(); o++) {
                            pareList[o] = (Pare) pareArrayList.get(o);
                        }
                        RatioConfig[] ratioList = new RatioConfig[ratioArrayList.size()];
                        for (o = 0; o < ratioArrayList.size(); o++) {
                            ratioList[o] = (RatioConfig) ratioArrayList.get(o);
                        }
                        mConfigs = ratioList;
                        AppClassify.updatePareList(pareList);
                        if (vgFlagArrayList.size() > 0) {
                            String[] vgFlagList = new String[vgFlagArrayList.size()];
                            for (o = 0; o < vgFlagArrayList.size(); o++) {
                                vgFlagList[o] = (String) vgFlagArrayList.get(o);
                            }
                            AppClassify.updateVgFlagList(vgFlagList);
                        }
                        AblConfig.setUseBrightnessSceneRatio(enable);
                        this.mParseRetry = 0;
                    }
                }
                throw new JSONException("ENGINE_VERSION not match: edition:" + edition + " ENGINE:" + CONFIG_ENGINE_VERSION);
            }
            this.mParseRetry = 0;
        } catch (JSONException e) {
            Slog.e(TAG, "parseConfiguration got exception", e);
            this.mParseRetry++;
            if (this.mParseRetry < 5) {
                this.mHandler.sendEmptyMessageDelayed(1, 3000);
            }
        }
    }

    private void getConfig(String uri, String moduleName, String type, String version, String identifier) {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse(uri), null, null, new String[]{moduleName, type, version, identifier}, null);
            if (cursor != null) {
                String fileId = Events.DEFAULT_SORT_ORDER;
                String tartgetIdentifier = Events.DEFAULT_SORT_ORDER;
                String fileVersion = Events.DEFAULT_SORT_ORDER;
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    while (!cursor.isAfterLast()) {
                        fileId = cursor.getString(cursor.getColumnIndex("id"));
                        tartgetIdentifier = cursor.getString(cursor.getColumnIndex("identifier"));
                        fileVersion = cursor.getString(cursor.getColumnIndex("fileversion"));
                        String confContent = new String(cursor.getBlob(cursor.getColumnIndex("filecontent")), "UTF-8");
                        Slog.d(TAG, "getConfig fileId:" + fileId);
                        Slog.d(TAG, "getConfig tartgetIdentifier:" + tartgetIdentifier);
                        Slog.d(TAG, "getConfig fileVersion:" + fileVersion);
                        Slog.d(TAG, "getConfig filecontent.json:\n  " + confContent);
                        parseConfiguration(confContent);
                        cursor.moveToNext();
                    }
                } else if (AblConfig.isDebug()) {
                    Slog.e(TAG, "getConfig nodata ");
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private static void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    private static int binarySearchRange(int[] A, int target) {
        int low = 0;
        int high = A.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (A[mid] > target) {
                high = mid - 1;
            } else if (A[mid] >= target) {
                return mid;
            } else {
                low = mid + 1;
            }
        }
        if (high < 0) {
            return -1;
        }
        return high;
    }

    public static int getAppBrightPromotion(int current, int lux, String pkg) {
        String type = AppClassify.getAppType(pkg);
        if (type.equals(AppType.TYPE_UNKOWN)) {
            return current;
        }
        for (RatioConfig c : mConfigs) {
            if (c.type.equals(type)) {
                return c.getTargetBrightness(current);
            }
        }
        return current;
    }

    public void dump(PrintWriter pw) {
        pw.println("  RatioConfig:");
        pw.println("    enable:" + AblConfig.isUseBrightnessSceneRatio());
        pw.println("    date:" + mConfigDate);
        for (RatioConfig conf : mConfigs) {
            if (conf == null) {
                pw.println("    conf is null");
            } else {
                pw.println("    type:" + conf.type);
                pw.println("    thar:" + Arrays.toString(conf.thresArray));
                pw.println("    taar:" + Arrays.toString(conf.ratioArray));
            }
        }
        AppClassify.dump(pw);
    }
}
