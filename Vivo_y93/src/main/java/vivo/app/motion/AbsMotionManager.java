package vivo.app.motion;

import android.os.IBinder;
import java.util.List;

public abstract class AbsMotionManager {
    public abstract List getClients();

    public abstract int register(String str, String str2, String str3, IBinder iBinder);

    public abstract int unregister(String str);
}
