package vivo.app.userprofiling;

import java.util.ArrayList;
import java.util.Map;

public abstract class AbsVivoUserProfilingManager {
    public abstract String getInfoType(String str, String str2);

    public abstract ArrayList<String> getPageTypeList(String str, String str2);

    public abstract Map<String, String> getPropertiesMapByPagetype(String str, String str2, String str3);

    public abstract String getWeChatWebViewValue();

    public abstract boolean isCaptureOn();

    public abstract boolean isPropertiesContainPage(String str, String str2);

    public abstract boolean isPropertiesContainPkg(String str);

    public abstract void ping(String str);

    public abstract int reportDataToBee(String str, String str2, String str3, String str4);

    public abstract void setWeChatWebViewValue(String str);

    public abstract void updateConfig();

    public abstract void updateWeChatViewId(String str);
}
