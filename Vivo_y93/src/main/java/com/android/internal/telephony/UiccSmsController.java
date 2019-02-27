package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityThread;
import android.app.Application;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.internal.telephony.ISms.Stub;
import java.util.List;

public class UiccSmsController extends Stub {
    static final String LOG_TAG = "RIL_UiccSmsController";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected String mGn = SystemProperties.get("ro.build.gn.support", "0");

    protected UiccSmsController() {
        if (ServiceManager.getService("isms") == null) {
            ServiceManager.addService("isms", this);
        }
    }

    private Phone getPhone(int subId) {
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            return PhoneFactory.getDefaultPhone();
        }
        return phone;
    }

    public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPackage, int index, int status, byte[] pdu) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.updateMessageOnIccEf(callingPackage, index, status, pdu);
        }
        Rlog.e(LOG_TAG, "updateMessageOnIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public boolean copyMessageToIccEfForSubscriber(int subId, String callingPackage, int status, byte[] pdu, byte[] smsc) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.copyMessageToIccEf(callingPackage, status, pdu, smsc);
        }
        Rlog.e(LOG_TAG, "copyMessageToIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPackage) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getAllMessagesFromIccEf(callingPackage);
        }
        Rlog.e(LOG_TAG, "getAllMessagesFromIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    public void sendDataForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (!isMmsEmmMode(subId, sentIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            } else {
                Rlog.e(LOG_TAG, "sendDataForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
                sendErrorInPendingIntent(sentIntent, 1);
            }
        }
    }

    public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (!isMmsEmmMode(subId, sentIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendDataWithSelfPermissions(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            } else {
                Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
            }
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, true);
    }

    public void sendTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        if (!isMmsEmmMode(subId, sentIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendText(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
            } else {
                Rlog.e(LOG_TAG, "sendTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
                sendErrorInPendingIntent(sentIntent, 1);
            }
        }
    }

    public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        if (!isMmsEmmMode(subId, sentIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendTextWithSelfPermissions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
            } else {
                Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
            }
        }
    }

    public void sendTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, String parts, PendingIntent sentIntents, PendingIntent deliveryIntents, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessage, priority, isExpectMore, validityPeriod);
        } else {
            Rlog.e(LOG_TAG, "sendTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
        }
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
        sendMultipartTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, true);
    }

    public void sendMultipartTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        PendingIntent pendingIntent = null;
        if (sentIntents != null && sentIntents.size() > 0) {
            pendingIntent = (PendingIntent) sentIntents.get(0);
        }
        if (!isMmsEmmMode(subId, pendingIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendMultipartText(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
            } else {
                Rlog.e(LOG_TAG, "sendMultipartTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
                sendErrorInPendingIntents(sentIntents, 1);
            }
        }
    }

    public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessage, priority, isExpectMore, validityPeriod);
        } else {
            Rlog.e(LOG_TAG, "sendMultipartTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
        }
    }

    public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
        return enableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.enableCellBroadcastRange(startMessageId, endMessageId, ranType);
        }
        Rlog.e(LOG_TAG, "enableCellBroadcastRangeForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
        return disableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.disableCellBroadcastRange(startMessageId, endMessageId, ranType);
        }
        Rlog.e(LOG_TAG, "disableCellBroadcastRangeForSubscriber iccSmsIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int getPremiumSmsPermission(String packageName) {
        return getPremiumSmsPermissionForSubscriber(getPreferredSmsSubscription(), packageName);
    }

    public int getPremiumSmsPermissionForSubscriber(int subId, String packageName) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getPremiumSmsPermission(packageName);
        }
        Rlog.e(LOG_TAG, "getPremiumSmsPermissionForSubscriber iccSmsIntMgr is null");
        return 0;
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        setPremiumSmsPermissionForSubscriber(getPreferredSmsSubscription(), packageName, permission);
    }

    public void setPremiumSmsPermissionForSubscriber(int subId, String packageName, int permission) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.setPremiumSmsPermission(packageName, permission);
        } else {
            Rlog.e(LOG_TAG, "setPremiumSmsPermissionForSubscriber iccSmsIntMgr is null");
        }
    }

    public boolean isImsSmsSupportedForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isImsSmsSupported();
        }
        Rlog.e(LOG_TAG, "isImsSmsSupportedForSubscriber iccSmsIntMgr is null");
        return false;
    }

    public boolean isSmsSimPickActivityNeeded(int subId) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            if (subInfoList != null) {
                int subInfoLength = subInfoList.size();
                for (int i = 0; i < subInfoLength; i++) {
                    SubscriptionInfo sir = (SubscriptionInfo) subInfoList.get(i);
                    if (sir != null && sir.getSubscriptionId() == subId) {
                        return false;
                    }
                }
                return subInfoLength > 0 && telephonyManager.getSimCount() > 1;
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getImsSmsFormatForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getImsSmsFormat();
        }
        Rlog.e(LOG_TAG, "getImsSmsFormatForSubscriber iccSmsIntMgr is null");
        return null;
    }

    public void injectSmsPduForSubscriber(int subId, byte[] pdu, String format, PendingIntent receivedIntent) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.injectSmsPdu(pdu, format, receivedIntent);
            return;
        }
        Rlog.e(LOG_TAG, "injectSmsPduForSubscriber iccSmsIntMgr is null");
        sendErrorInPendingIntent(receivedIntent, 2);
    }

    private IccSmsInterfaceManager getIccSmsInterfaceManager(int subId) {
        return getPhone(subId).getIccSmsInterfaceManager();
    }

    public int getPreferredSmsSubscription() {
        return SubscriptionController.getInstance().getDefaultSmsSubId();
    }

    public boolean isSMSPromptEnabled() {
        return PhoneFactory.isSMSPromptEnabled();
    }

    public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        Cursor cursor = null;
        String destAddr = "";
        try {
            cursor = ActivityThread.currentApplication().getApplicationContext().getContentResolver().query(messageUri, new String[]{"address"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                destAddr = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (!isMmsEmmMode(subId, sentIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendStoredText(callingPkg, messageUri, scAddress, sentIntent, deliveryIntent);
            } else {
                Rlog.e(LOG_TAG, "sendStoredText iccSmsIntMgr is null for subscription: " + subId);
                sendErrorInPendingIntent(sentIntent, 1);
            }
        }
    }

    public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
        PendingIntent pendingIntent = null;
        Cursor cursor = null;
        String destAddr = "";
        if (sentIntents != null && sentIntents.size() > 0) {
            pendingIntent = (PendingIntent) sentIntents.get(0);
        }
        try {
            cursor = ActivityThread.currentApplication().getApplicationContext().getContentResolver().query(messageUri, new String[]{"address"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                destAddr = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (!isMmsEmmMode(subId, pendingIntent, destAddr)) {
            IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
            if (iccSmsIntMgr != null) {
                iccSmsIntMgr.sendStoredMultipartText(callingPkg, messageUri, scAddress, sentIntents, deliveryIntents);
            } else {
                Rlog.e(LOG_TAG, "sendStoredMultipartText iccSmsIntMgr is null for subscription: " + subId);
                sendErrorInPendingIntents(sentIntents, 1);
            }
        }
    }

    public String createAppSpecificSmsToken(int subId, String callingPkg, PendingIntent intent) {
        return getPhone(subId).getAppSmsManager().createAppSpecificSmsToken(callingPkg, intent);
    }

    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (CanceledException e) {
            }
        }
    }

    private void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        for (PendingIntent intent : intents) {
            sendErrorInPendingIntent(intent, errorCode);
        }
    }

    public int getSmsCapacityOnIccForSubscriber(int subId) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getSmsCapacityOnIcc();
        }
        Rlog.e(LOG_TAG, "iccSmsIntMgr is null for  subId: " + subId);
        return -1;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean isMmsEmmMode(int subId, PendingIntent sentIntent) {
        Application app = ActivityThread.currentApplication();
        if (app != null) {
            Context context = app.getApplicationContext();
            if ("1".equals(this.mGn) && TelephonyPhoneUtils.isMmsSendEmmMode(context, subId)) {
                if (sentIntent != null) {
                    try {
                        sentIntent.send(1);
                    } catch (Exception e) {
                        return true;
                    } catch (Throwable th) {
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean isMmsEmmMode(int subId, PendingIntent sentIntent, String number) {
        Rlog.d(LOG_TAG, "isMmsEmmMode : mGn = " + this.mGn);
        Application app;
        if ("1".equals(this.mGn)) {
            app = ActivityThread.currentApplication();
            if (app != null && TelephonyPhoneUtils.isMmsSendEmmMode(app.getApplicationContext(), subId)) {
                if (sentIntent != null) {
                    try {
                        sentIntent.send(1);
                    } catch (Exception e) {
                        return true;
                    } catch (Throwable th) {
                        return true;
                    }
                }
                return true;
            }
        } else if (!"0".equals(this.mGn)) {
            app = ActivityThread.currentApplication();
            if (app != null) {
                final Context context = app.getApplicationContext();
                int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
                int smsBlockSim = Secure.getInt(context.getContentResolver(), "ct_network_sms_blocksim", 0);
                int smsBlockSend = Secure.getInt(context.getContentResolver(), "ct_network_sms_blocksend", 1);
                Rlog.d(LOG_TAG, "isMmsEmmMode : smsBlockSim = " + smsBlockSim + ",smsBlockSend = " + smsBlockSend);
                if (((smsBlockSim == 0 || smsBlockSim - 1 == phoneId) && smsBlockSend == 0) || TelephonyPhoneUtils.isMDMForbidden(context, number)) {
                    if (sentIntent != null) {
                        try {
                            sentIntent.send(1);
                        } catch (Exception e2) {
                            return true;
                        } catch (Throwable th2) {
                            return true;
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, context.getResources().getString(51249709), 0).show();
                        }
                    });
                    return true;
                }
            }
        }
        return false;
    }
}
