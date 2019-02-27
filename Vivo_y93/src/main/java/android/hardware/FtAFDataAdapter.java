package android.hardware;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import java.lang.reflect.Method;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtAFDataAdapter {

    public interface FtAFDataCallback {
        void onAFData(byte[] bArr, Camera camera);
    }

    public void setAFDataCallback(Camera camera, FtAFDataCallback cb) {
        try {
            Method method = Class.forName("android.hardware.Camera").getDeclaredMethod("setAFDataCallback", new Class[]{FtAFDataCallback.class});
            method.setAccessible(true);
            method.invoke(camera, new Object[]{cb});
        } catch (Exception e) {
        }
    }
}
