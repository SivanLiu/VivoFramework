package vivo.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Looper;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtInputMonitorUtil {
    private final String TAG = "FtInputMonitorUtil";
    private Context mContext = null;
    private InputChannel mInputChannel = null;
    private HookInputEventReceiver mInputEventReceiver = null;
    private boolean mIsRegisted = false;
    private MonitorInputListener mMonitorInputListener = null;
    private IWindowManager mWms = null;

    private class HookInputEventReceiver extends InputEventReceiver {
        public HookInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event, int displayId) {
            boolean handled = false;
            try {
                if (FtInputMonitorUtil.this.mMonitorInputListener != null) {
                    handled = FtInputMonitorUtil.this.mMonitorInputListener.onInputEvent(event);
                }
                finishInputEvent(event, handled);
            } catch (Throwable th) {
                finishInputEvent(event, false);
            }
        }
    }

    public interface MonitorInputListener {
        boolean onInputEvent(InputEvent inputEvent);
    }

    public FtInputMonitorUtil(Context context) {
        this.mContext = context;
        this.mWms = Stub.asInterface(ServiceManager.getService("window"));
    }

    public boolean registerInputMoniter(MonitorInputListener monitorInputListener) {
        if (this.mIsRegisted) {
            throw new IllegalStateException("One FtInputMonitorUtil object only can register one MonitorInputListener.");
        }
        try {
            this.mMonitorInputListener = monitorInputListener;
            this.mInputChannel = this.mWms.monitorInput("input_hook");
            this.mInputEventReceiver = new HookInputEventReceiver(this.mInputChannel, this.mContext.getMainLooper());
            this.mIsRegisted = true;
            return true;
        } catch (Exception e) {
            this.mIsRegisted = false;
            return false;
        }
    }

    public void unRegisterInputMoniter() {
        if (this.mIsRegisted) {
            if (this.mInputEventReceiver != null) {
                this.mInputEventReceiver.dispose();
            }
            if (this.mInputChannel != null) {
                this.mInputChannel.dispose();
            }
        }
    }
}
