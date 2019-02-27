package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import java.util.ArrayList;
import java.util.Queue;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public interface VivoMassExceptionAPI {
    public static final String ACTION_LOG_UPLOAD = "vivo.intent.action.vivonetworklogupload";
    public static final String ACTION_LOG_UPLOAD_IMS = "vivo.intent.action.vivocloudforims";
    public static final String ID_EVENTID_CALL_DROP_v3 = "1";
    public static final String ID_EVENTID_IMS_v3 = "7";
    public static final String ID_EVENTID_MMS_v3 = "6";
    public static final String ID_EVENTID_MessData_CALL_DROP_v4 = "1203_1";
    public static final String ID_EVENTID_MessData_IMS_v4 = "1203_6";
    public static final String ID_EVENTID_MessData_MMS_v4 = "1203_5";
    public static final String ID_EVENTID_MessData_OUT_OF_SERVICE_v4 = "1203_2";
    public static final String ID_EVENTID_MessData_PDP_v4 = "1203_3";
    public static final String ID_EVENTID_MessData_SMS_v4 = "1203_4";
    public static final String ID_EVENTID_MessData_v4 = "1203";
    public static final String ID_EVENTID_OUT_OF_SERVICE_v3 = "2";
    public static final String ID_EVENTID_PDP_v3 = "4";
    public static final String ID_EVENTID_RAT_CHANGE_v3 = "3";
    public static final String ID_EVENTID_SMS_v3 = "5";
    public static final String ID_EVENTID_VivoCloudData_CALL_DROP_v4 = "00010|012";
    public static final String ID_EVENTID_VivoCloudData_IMS_v4 = "00016|012";
    public static final String ID_EVENTID_VivoCloudData_MMS_v4 = "00015|012";
    public static final String ID_EVENTID_VivoCloudData_OUT_OF_SERVICE_v4 = "00011|012";
    public static final String ID_EVENTID_VivoCloudData_PDP_v4 = "00013|012";
    public static final String ID_EVENTID_VivoCloudData_RAT_CHANGE_v4 = "00011|012";
    public static final String ID_EVENTID_VivoCloudData_SMS_v4 = "00014|012";
    public static final String ID_MODULE_TELECOM_v4 = "600";
    public static final int SUB_MODULE_EVENT_CALL_DROP = 1;
    public static final int SUB_MODULE_EVENT_IMS = 7;
    public static final int SUB_MODULE_EVENT_MMS = 6;
    public static final int SUB_MODULE_EVENT_OUT_OF_SERVICE = 2;
    public static final int SUB_MODULE_EVENT_PDP = 4;
    public static final int SUB_MODULE_EVENT_RAT_CHANGE = 3;
    public static final int SUB_MODULE_EVENT_SMS = 5;
    public static final String TYPE_EXCEPTION_COMMUNICATE = "2";
    public static final String TYPE_EXCEPTION_NETWORK = "1";
    public static final String TYPE_SUB_EXCEPTION_CALL_DROP_v4 = "1";
    public static final String TYPE_SUB_EXCEPTION_IMS_v4 = "1";
    public static final String TYPE_SUB_EXCEPTION_MMS_v4 = "6";
    public static final String TYPE_SUB_EXCEPTION_OUT_OF_SERVICE_v4 = "2";
    public static final String TYPE_SUB_EXCEPTION_PDP_v4 = "4";
    public static final String TYPE_SUB_EXCEPTION_RAT_CHANGE_v4 = "3";
    public static final String TYPE_SUB_EXCEPTION_SMS_v4 = "5";

    ArrayList<String> buildArrayListContentAPI(Queue<CollectionBean> queue);

    String buildContentAPI(Queue<CollectionBean> queue);

    void disposeAPI(Context context);

    String getCurrentModuleEventIdAPI();

    String getCurrentModuleIdAPI();

    String getCurrentModuleSubEventIdAPI(int i);

    String getCurrrentRsrpAPI();

    String getCurrrentRsrqAPI();

    String getCurrrentSignalAPI();

    String getExceptionSubTypeAPI();

    String getExceptionTypeAPI();

    String getLocationAPI();

    String getVersionAPI();

    void initAPI(Context context);

    boolean isDebugAPI();

    boolean isWriteToDataBaseAPI(int i, long j, int i2, CollectionBean collectionBean, Queue<CollectionBean> queue);

    void reportToServerAPI(CollectionBean collectionBean);

    void setDebugAPI(boolean z);

    void setSignalStrengthsChangedAPI(int i, int i2, int i3);

    void uploadLogAPI(int i, String str, String str2, String str3, String str4, JSONObject jSONObject, String str5, String str6);

    void uploadLogAPI(String str, String str2, String str3);

    boolean writeFileAPI(String str, String str2);

    boolean writeToDatabaseAPI(Queue<CollectionBean> queue, String str);
}
