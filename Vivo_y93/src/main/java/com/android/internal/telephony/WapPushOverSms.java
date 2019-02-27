package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.BroadcastOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.IWapPushManager.Stub;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import java.util.HashMap;

public class WapPushOverSms implements ServiceConnection {
    private static final boolean DBG = false;
    private static final String LOCATION_SELECTION = "m_type=? AND ct_l =?";
    private static final String TAG = "WAP PUSH";
    private static final String THREAD_ID_SELECTION = "m_id=? AND m_type=?";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Rlog.d(WapPushOverSms.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                new BindServiceThread(WapPushOverSms.this, WapPushOverSms.this.mContext, null).start();
            }
        }
    };
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Bundle mBundle;
    private final Context mContext;
    private IDeviceIdleController mDeviceIdleController;
    private volatile IWapPushManager mWapPushManager;
    private String mWapPushManagerPackage;

    private class BindServiceThread extends Thread {
        private final Context context;

        /* synthetic */ BindServiceThread(WapPushOverSms this$0, Context context, BindServiceThread -this2) {
            this(context);
        }

        private BindServiceThread(Context context) {
            this.context = context;
        }

        public void run() {
            WapPushOverSms.this.bindWapPushManagerService(this.context);
        }
    }

    private final class DecodedResult {
        String contentType;
        HashMap<String, String> contentTypeParameters;
        byte[] header;
        byte[] intentData;
        String mimeType;
        GenericPdu parsedPdu;
        int pduType;
        int phoneId;
        int statusCode;
        int subId;
        int transactionId;
        String wapAppId;

        /* synthetic */ DecodedResult(WapPushOverSms this$0, DecodedResult -this1) {
            this();
        }

        private DecodedResult() {
        }
    }

    private void bindWapPushManagerService(Context context) {
        Intent intent = new Intent(IWapPushManager.class.getName());
        ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || (context.bindService(intent, this, 1) ^ 1) != 0) {
            Rlog.e(TAG, "bindService() for wappush manager failed");
            return;
        }
        synchronized (this) {
            this.mWapPushManagerPackage = comp.getPackageName();
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        this.mWapPushManager = Stub.asInterface(service);
    }

    public void onServiceDisconnected(ComponentName name) {
        this.mWapPushManager = null;
    }

    public WapPushOverSms(Context context) {
        this.mContext = context;
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        if (((UserManager) this.mContext.getSystemService("user")).isUserUnlocked()) {
            bindWapPushManagerService(this.mContext);
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    public void dispose() {
        if (this.mWapPushManager != null) {
            this.mContext.unbindService(this);
        } else {
            Rlog.e(TAG, "dispose: not bound to a wappush manager");
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int dispatchWapPdu(byte[] pdu, Bundle extra, BroadcastReceiver receiver, InboundSmsHandler handler) {
        this.mBundle = extra;
        return dispatchWapPdu(pdu, receiver, handler);
    }

    private DecodedResult decodeWapPdu(byte[] pdu, InboundSmsHandler handler) {
        ArrayIndexOutOfBoundsException aie;
        DecodedResult decodedResult = new DecodedResult(this, null);
        try {
            int transactionId = pdu[0] & 255;
            int index = 1 + 1;
            int index2;
            try {
                int pduType = pdu[1] & 255;
                int phoneId = handler.getPhone().getPhoneId();
                if (pduType == 6 || pduType == 7) {
                    index2 = index;
                } else {
                    index2 = this.mContext.getResources().getInteger(17694866);
                    if (index2 != -1) {
                        index = index2 + 1;
                        transactionId = pdu[index2] & 255;
                        index2 = index + 1;
                        pduType = pdu[index] & 255;
                        if (!(pduType == 6 || pduType == 7)) {
                            decodedResult.statusCode = 1;
                            return decodedResult;
                        }
                    }
                    decodedResult.statusCode = 1;
                    return decodedResult;
                }
                WspTypeDecoder pduDecoder = TelephonyComponentFactory.getInstance().makeWspTypeDecoder(pdu);
                if (pduDecoder.decodeUintvarInteger(index2)) {
                    int headerLength = (int) pduDecoder.getValue32();
                    index2 += pduDecoder.getDecodedDataLength();
                    int headerStartIndex = index2;
                    if (pduDecoder.decodeContentType(index2)) {
                        byte[] intentData;
                        int subId;
                        String mimeType = pduDecoder.getValueString();
                        long binaryContentType = pduDecoder.getValue32();
                        index2 += pduDecoder.getDecodedDataLength();
                        byte[] header = new byte[headerLength];
                        System.arraycopy(pdu, headerStartIndex, header, 0, header.length);
                        if (mimeType == null || !mimeType.equals(WspTypeDecoder.CONTENT_TYPE_B_PUSH_CO)) {
                            int dataIndex = headerStartIndex + headerLength;
                            intentData = new byte[(pdu.length - dataIndex)];
                            System.arraycopy(pdu, dataIndex, intentData, 0, intentData.length);
                        } else {
                            intentData = pdu;
                        }
                        int[] subIds = SubscriptionManager.getSubId(phoneId);
                        if (subIds == null || subIds.length <= 0) {
                            subId = SmsManager.getDefaultSmsSubscriptionId();
                        } else {
                            subId = subIds[0];
                        }
                        GenericPdu parsedPdu = null;
                        try {
                            parsedPdu = new PduParser(intentData, shouldParseContentDisposition(subId)).parse();
                        } catch (Exception e) {
                            Rlog.e(TAG, "Unable to parse PDU: " + e.toString());
                        }
                        if (parsedPdu != null) {
                            if (parsedPdu.getMessageType() == 130) {
                                NotificationInd nInd = (NotificationInd) parsedPdu;
                                if (nInd.getFrom() != null && BlockChecker.isBlocked(this.mContext, nInd.getFrom().getString())) {
                                    decodedResult.statusCode = 1;
                                    return decodedResult;
                                }
                            }
                        }
                        if (pduDecoder.seekXWapApplicationId(index2, (index2 + headerLength) - 1)) {
                            pduDecoder.decodeXWapApplicationId((int) pduDecoder.getValue32());
                            String wapAppId = pduDecoder.getValueString();
                            if (wapAppId == null) {
                                wapAppId = Integer.toString((int) pduDecoder.getValue32());
                            }
                            decodedResult.wapAppId = wapAppId;
                            decodedResult.contentType = mimeType == null ? Long.toString(binaryContentType) : mimeType;
                        }
                        decodedResult.subId = subId;
                        decodedResult.phoneId = phoneId;
                        decodedResult.parsedPdu = parsedPdu;
                        decodedResult.mimeType = mimeType;
                        decodedResult.transactionId = transactionId;
                        decodedResult.pduType = pduType;
                        decodedResult.header = header;
                        decodedResult.intentData = intentData;
                        decodedResult.contentTypeParameters = pduDecoder.getContentParameters();
                        decodedResult.statusCode = -1;
                        return decodedResult;
                    }
                    decodedResult.statusCode = 2;
                    return decodedResult;
                }
                decodedResult.statusCode = 2;
                return decodedResult;
            } catch (ArrayIndexOutOfBoundsException e2) {
                aie = e2;
                index2 = index;
                Rlog.e(TAG, "ignoring dispatchWapPdu() array index exception: " + aie);
                decodedResult.statusCode = 2;
                return decodedResult;
            }
        } catch (ArrayIndexOutOfBoundsException e3) {
            aie = e3;
            Rlog.e(TAG, "ignoring dispatchWapPdu() array index exception: " + aie);
            decodedResult.statusCode = 2;
            return decodedResult;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler) {
        try {
            DecodedResult result = decodeWapPdu(pdu, handler);
            if (result.statusCode != -1) {
                return result.statusCode;
            }
            Intent intent;
            if (SmsManager.getDefault().getAutoPersisting()) {
                writeInboxMessage(result.subId, result.parsedPdu);
            }
            if (result.wapAppId != null) {
                boolean processFurther = true;
                try {
                    IWapPushManager wapPushMan = this.mWapPushManager;
                    if (wapPushMan != null) {
                        synchronized (this) {
                            this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(this.mWapPushManagerPackage, 0, "mms-mgr");
                        }
                        intent = new Intent();
                        intent.putExtra("transactionId", result.transactionId);
                        intent.putExtra("pduType", result.pduType);
                        intent.putExtra("header", result.header);
                        intent.putExtra("data", result.intentData);
                        intent.putExtra("contentTypeParameters", result.contentTypeParameters);
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
                        if (this.mBundle != null) {
                            Rlog.d(TAG, "put addr info into intent 1");
                            intent.putExtra("address", this.mBundle.getString("address"));
                            intent.putExtra("service_center", this.mBundle.getString("service_center"));
                        }
                        int procRet = wapPushMan.processMessage(result.wapAppId, result.contentType, intent);
                        if ((procRet & 1) > 0 && (32768 & procRet) == 0) {
                            processFurther = false;
                        }
                    }
                    if (!processFurther) {
                        return 1;
                    }
                } catch (RemoteException e) {
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (result.mimeType == null) {
                return 2;
            }
            intent = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
            intent.setType(result.mimeType);
            intent.putExtra("transactionId", result.transactionId);
            intent.putExtra("pduType", result.pduType);
            intent.putExtra("header", result.header);
            intent.putExtra("data", result.intentData);
            intent.putExtra("contentTypeParameters", result.contentTypeParameters);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
            if (this.mBundle != null) {
                Rlog.d(TAG, "put addr info into intent 2");
                intent.putExtra("address", this.mBundle.getString("address"));
                intent.putExtra("service_center", this.mBundle.getString("service_center"));
            }
            ComponentName componentName = SmsApplication.getDefaultMmsApplication(this.mContext, true);
            Bundle options = null;
            if (componentName != null) {
                intent.setComponent(componentName);
                try {
                    long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(componentName.getPackageName(), 0, "mms-app");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e3) {
                }
            }
            handler.dispatchIntent(intent, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
            return -1;
        } catch (Exception e22) {
            Rlog.e(TAG, "dispatchWapPdu() exception: " + e22);
            return 0;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public boolean isWapPushForMms(byte[] pdu, InboundSmsHandler handler) {
        boolean z = false;
        try {
            DecodedResult result = decodeWapPdu(pdu, handler);
            if (result.statusCode == -1) {
                z = "application/vnd.wap.mms-message".equals(result.mimeType);
            }
            return z;
        } catch (Exception e) {
            Rlog.e(TAG, "isWapPushForMms() exception: " + e);
            return false;
        }
    }

    private static boolean shouldParseContentDisposition(int subId) {
        return SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues().getBoolean("supportMmsContentDisposition", true);
    }

    private void writeInboxMessage(int subId, GenericPdu pdu) {
        if (pdu == null) {
            Rlog.e(TAG, "Invalid PUSH PDU");
        }
        PduPersister persister = PduPersister.getPduPersister(this.mContext);
        int type = pdu.getMessageType();
        switch (type) {
            case 130:
                NotificationInd nInd = (NotificationInd) pdu;
                Bundle configs = SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues();
                if (configs != null && configs.getBoolean("enabledTransID", false)) {
                    byte[] contentLocation = nInd.getContentLocation();
                    if ((byte) 61 == contentLocation[contentLocation.length - 1]) {
                        byte[] transactionId = nInd.getTransactionId();
                        byte[] contentLocationWithId = new byte[(contentLocation.length + transactionId.length)];
                        System.arraycopy(contentLocation, 0, contentLocationWithId, 0, contentLocation.length);
                        System.arraycopy(transactionId, 0, contentLocationWithId, contentLocation.length, transactionId.length);
                        nInd.setContentLocation(contentLocationWithId);
                    }
                }
                if (isDuplicateNotification(this.mContext, nInd)) {
                    Rlog.d(TAG, "Skip storing duplicate MMS WAP push notification ind: " + new String(nInd.getContentLocation()));
                    return;
                }
                if (persister.persist(pdu, Inbox.CONTENT_URI, true, true, null) == null) {
                    Rlog.e(TAG, "Failed to save MMS WAP push notification ind");
                    return;
                }
                return;
            case 134:
            case 136:
                long threadId = getDeliveryOrReadReportThreadId(this.mContext, pdu);
                if (threadId == -1) {
                    Rlog.e(TAG, "Failed to find delivery or read report's thread id");
                    return;
                }
                Uri uri = persister.persist(pdu, Inbox.CONTENT_URI, true, true, null);
                if (uri == null) {
                    Rlog.e(TAG, "Failed to persist delivery or read report");
                    return;
                }
                ContentValues values = new ContentValues(1);
                values.put("thread_id", Long.valueOf(threadId));
                if (SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, values, null, null) != 1) {
                    Rlog.e(TAG, "Failed to update delivery or read report thread id");
                    return;
                }
                return;
            default:
                try {
                    Log.e(TAG, "Received unrecognized WAP Push PDU.");
                    return;
                } catch (Throwable e) {
                    Log.e(TAG, "Failed to save MMS WAP push data: type=" + type, e);
                    return;
                } catch (Throwable e2) {
                    Log.e(TAG, "Unexpected RuntimeException in persisting MMS WAP push data", e2);
                    return;
                }
        }
    }

    private static long getDeliveryOrReadReportThreadId(Context context, GenericPdu pdu) {
        String messageId;
        if (pdu instanceof DeliveryInd) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else if (pdu instanceof ReadOrigInd) {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        } else {
            Rlog.e(TAG, "WAP Push data is neither delivery or read report type: " + pdu.getClass().getCanonicalName());
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"thread_id"}, THREAD_ID_SELECTION, new String[]{DatabaseUtils.sqlEscapeString(messageId), Integer.toString(128)}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            long j = cursor.getLong(0);
            if (cursor != null) {
                cursor.close();
            }
            return j;
        } catch (SQLiteException e) {
            Rlog.e(TAG, "Failed to query delivery or read report thread id", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        if (nInd.getContentLocation() != null) {
            String[] selectionArgs = new String[]{new String(nInd.getContentLocation())};
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, new String[]{HbpcdLookup.ID}, LOCATION_SELECTION, new String[]{Integer.toString(130), new String(rawLocation)}, null);
                if (cursor != null && cursor.getCount() > 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Rlog.e(TAG, "failed to query existing notification ind", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return false;
    }

    public static String getPermissionForType(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return "android.permission.RECEIVE_MMS";
        }
        return "android.permission.RECEIVE_WAP_PUSH";
    }

    public static int getAppOpsPermissionForIntent(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return 18;
        }
        return 19;
    }
}
