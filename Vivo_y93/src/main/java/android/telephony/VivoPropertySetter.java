package android.telephony;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.List;

public class VivoPropertySetter {
    private static final int SET_PROP_TIMEOUT = 1000;
    private static final String TAG = "VivoPropertySetter";
    private static Handler sPropertyHandler;
    private static List<String> sPropertyKeys = new ArrayList();

    private static class PropertyHandler extends Handler {
        public PropertyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.obj instanceof PropertyRequest) {
                PropertyRequest request = msg.obj;
                if (hasMessages(msg.what)) {
                    Rlog.d(VivoPropertySetter.TAG, "ignore. there is new property request of the same key");
                } else {
                    try {
                        SystemProperties.set(request.key, request.value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Rlog.d(VivoPropertySetter.TAG, "set property complete. key = " + request.key);
                }
                synchronized (request) {
                    Rlog.d(VivoPropertySetter.TAG, "notify request = " + request);
                    request.notifyAll();
                }
            }
        }
    }

    private static class PropertyRequest {
        public String key;
        public String value;

        public PropertyRequest(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static void setPropertyAsync(String key, String value) {
        Handler handler = getPropertyHandler();
        PropertyRequest request = new PropertyRequest(key, value);
        Message.obtain(handler, getMsgId(key), request).sendToTarget();
        synchronized (request) {
            try {
                Rlog.d(TAG, "wait set property begin request = " + request);
                request.wait(1000);
                Rlog.d(TAG, "wait set property end");
            } catch (InterruptedException e) {
            }
        }
    }

    private static int getMsgId(String key) {
        int msgId;
        synchronized (sPropertyKeys) {
            msgId = sPropertyKeys.indexOf(key);
            if (msgId < 0) {
                msgId = sPropertyKeys.size();
                sPropertyKeys.add(key);
            }
        }
        return msgId;
    }

    private static Handler getPropertyHandler() {
        Handler handler;
        synchronized (sPropertyKeys) {
            if (sPropertyHandler == null) {
                HandlerThread thread = new HandlerThread("set-property");
                thread.start();
                sPropertyHandler = new PropertyHandler(thread.getLooper());
            }
            handler = sPropertyHandler;
        }
        return handler;
    }
}
