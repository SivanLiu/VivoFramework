package com.vivo.services.common;

import android.content.Context;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Slog;
import com.vivo.services.epm.config.BaseList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import vivo.app.common.IVivoCommon.Stub;

public class VivoCommonService extends Stub {
    private static final String COMMAND_GET_WECHAT_COMPAT_DISABLED = "COMMAND_GET_WECHAT_COMPAT_DISABLED";
    private static final boolean DBG = false;
    private static final String TAG = "VivoCommonService";
    private static final String WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH = "/data/bbkcore/CompatDisable.xml";
    private static VivoCommonService sInstance;
    private boolean isWeChatCompatDisabled = false;
    private Context mContext;
    private FileObserver mFileObserver;
    private Handler mHandler;
    private Object mWeChatCompatLock = new Object();
    private Runnable wechatConfigFileObserverRunnable = new Runnable() {
        public void run() {
            VivoCommonService.this.parseWeChatCompatConfig(VivoCommonService.WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH);
            VivoCommonService.this.observeWeChatCompatDisableConfigFile();
        }
    };

    final class CommonServiceHandler extends Handler {
        public CommonServiceHandler(Looper looper) {
            super(looper);
        }
    }

    private static native String doCommonJobByNative(String str);

    private VivoCommonService(Context context) {
        Slog.i(TAG, "Vivo Common Service");
        this.mContext = context;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new CommonServiceHandler(thread.getLooper());
        initWechatCompatDisable();
    }

    private void initWechatCompatDisable() {
        parseWeChatCompatConfig(WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH);
        observeWeChatCompatDisableConfigFile();
    }

    private boolean getWeChatCompatConfigDisabled() {
        synchronized (this.mWeChatCompatLock) {
        }
        return true;
    }

    private boolean parseWeChatCompatConfig(String filePath) {
        boolean z = false;
        try {
            String result = FileUtils.readTextFile(new File(filePath), 0, null);
            if (result != null) {
                synchronized (this.mWeChatCompatLock) {
                    this.isWeChatCompatDisabled = parseWeChatDisableStateFromXml(new ByteArrayInputStream(result.getBytes()));
                }
                Slog.d(TAG, "CompatConfigDisabled=" + this.isWeChatCompatDisabled);
                return true;
            }
            Slog.d(TAG, "CompatConfigDisabled=" + this.isWeChatCompatDisabled);
            return z;
        } catch (Exception e) {
            try {
                this.isWeChatCompatDisabled = false;
                Slog.e(TAG, "parseCompatConfig error! " + e.fillInStackTrace());
            } finally {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("CompatConfigDisabled=");
                z = this.isWeChatCompatDisabled;
                Slog.d(str, append.append(z).toString());
            }
        }
    }

    private boolean parseWeChatDisableStateFromXml(InputStream is) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            try {
                parser.setInput(new InputStreamReader(is));
                while (parser.getEventType() != 1) {
                    try {
                        if (parser.getEventType() == 2 && BaseList.STANDARD_LIST_ITEM_TAG.equalsIgnoreCase(parser.getName()) && "name".equalsIgnoreCase(parser.getAttributeName(0)) && "disable_wechat_compat".equalsIgnoreCase(parser.getAttributeValue(0)) && "value".equalsIgnoreCase(parser.getAttributeName(1))) {
                            return "1".equalsIgnoreCase(parser.getAttributeValue(1));
                        }
                        parser.next();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return false;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }

    private void observeWeChatCompatDisableConfigFile() {
        if (this.mFileObserver != null) {
            this.mFileObserver.stopWatching();
        }
        String fileToObserve = WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH;
        File file = new File(WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            Slog.e(TAG, "observeWeChatCompatDisableConfigFile create file error");
        }
        this.mFileObserver = new FileObserver(WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH, 1544) {
            public void onEvent(int event, String path) {
                if (8 == event) {
                    VivoCommonService.this.parseWeChatCompatConfig(VivoCommonService.WECHAT_COMPAT_DISABLE_CONFIG_FILE_PATH);
                }
                if (event == 1024 || event == ProcessStates.PAUSING) {
                    VivoCommonService.this.mHandler.removeCallbacks(VivoCommonService.this.wechatConfigFileObserverRunnable);
                    VivoCommonService.this.mHandler.postDelayed(VivoCommonService.this.wechatConfigFileObserverRunnable, 2000);
                }
            }
        };
        this.mFileObserver.startWatching();
    }

    public static synchronized VivoCommonService getInstance(Context context) {
        VivoCommonService vivoCommonService;
        synchronized (VivoCommonService.class) {
            if (sInstance == null) {
                sInstance = new VivoCommonService(context);
            }
            vivoCommonService = sInstance;
        }
        return vivoCommonService;
    }

    public void ping(String msg) {
        Slog.d(TAG, "ping msg = " + msg);
    }

    public String doCommonJob(String msg) {
        if (COMMAND_GET_WECHAT_COMPAT_DISABLED.equals(msg)) {
            return "" + getWeChatCompatConfigDisabled();
        }
        return null;
    }
}
