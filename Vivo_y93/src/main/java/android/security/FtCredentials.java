package android.security;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtCredentials {
    public static final String EXTRA_WAPI_SERVER_CERTIFICATE_DATA = "wapi_server_certificate_data";
    public static final String EXTRA_WAPI_SERVER_CERTIFICATE_NAME = "wapi_server_certificate_name";
    public static final String EXTRA_WAPI_USER_CERTIFICATE_DATA = "wapi_user_certificate_data";
    public static final String EXTRA_WAPI_USER_CERTIFICATE_NAME = "wapi_user_certificate_name";
    public static final String WAPI_SERVER_CERTIFICATE = "WAPISERVERCERT_";
    public static final String WAPI_USER_CERTIFICATE = "WAPIUSERCERT_";
}
