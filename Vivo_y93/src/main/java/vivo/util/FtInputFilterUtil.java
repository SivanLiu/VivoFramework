package vivo.util;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.InputEvent;
import android.view.InputFilter;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtInputFilterUtil {
    private final String TAG = "FtInputFilterUtil";
    private Context mContext = null;
    private InputFilterListener mFilterListener = null;
    private InputFilterProxy mInputFilterProxy = null;
    private Looper mLooper = null;
    private IWindowManager wms = null;

    public static abstract class InputFilterListener {
        InputFilterProxy filterProxy = null;

        public abstract void onInputEvent(InputEvent inputEvent, int i);

        private void setFilterProxy(InputFilterProxy proxy) {
            this.filterProxy = proxy;
        }

        public void dispatchInputEvent(InputEvent event, int policyFlags) {
            this.filterProxy.dispatchInputEvent(event, policyFlags);
        }
    }

    private class InputFilterProxy extends InputFilter {
        public InputFilterProxy(Looper looper) {
            super(looper);
        }

        public void onInputEvent(InputEvent event, int policyFlags) {
            if (FtInputFilterUtil.this.mFilterListener != null) {
                FtInputFilterUtil.this.mFilterListener.onInputEvent(event, policyFlags);
            }
        }

        public void dispatchInputEvent(InputEvent event, int policyFlags) {
            super.onInputEvent(event, policyFlags);
        }
    }

    public FtInputFilterUtil(Context context, Looper looper) {
        this.mContext = context;
        this.mLooper = looper;
        this.wms = Stub.asInterface(ServiceManager.getService("window"));
        this.mInputFilterProxy = new InputFilterProxy(looper);
    }

    public boolean setInputFilter(InputFilterListener filterListener) {
        this.mFilterListener = filterListener;
        try {
            if (this.mFilterListener == null) {
                this.wms.setInputFilter(null);
            } else {
                this.mFilterListener.setFilterProxy(this.mInputFilterProxy);
                this.wms.setInputFilter(this.mInputFilterProxy);
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
