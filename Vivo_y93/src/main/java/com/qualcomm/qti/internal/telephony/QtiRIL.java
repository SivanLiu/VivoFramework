package com.qualcomm.qti.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification.Stub;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.internal.telephony.RIL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;
import vendor.qti.hardware.radio.qtiradio.V1_0.IQtiRadio;

public final class QtiRIL extends RIL {
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int MSG_ID_AND_OEM_STRING_SIZE = 12;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int MSG_ID_SIZE = 4;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int MSG_LENGTH_SIZE = 4;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final int OEM_STRING_SIZE = 8;
    static final String[] QTI_HIDL_SERVICE_NAME = new String[]{"slot1", "slot2", "slot3"};
    static final String TAG = "QTIRILJ";
    final QtiRadioProxyDeathRecipient mDeathRecipient;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Handler mHandler;
    int mQtiPhoneId;
    private IQtiRadio mQtiRadio;
    QtiRadioIndication mQtiRadioIndication;
    final AtomicLong mQtiRadioProxyCookie;
    QtiRadioResponse mQtiRadioResponse;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private QtiRilInterface mQtiRilInterface;
    private final QtiRadioServiceNotification mServiceNotification;

    final class QtiRadioProxyDeathRecipient implements DeathRecipient {
        QtiRadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Rlog.d(QtiRIL.TAG, "serviceDied");
            QtiRIL.this.resetServiceAndRequestList();
        }
    }

    final class QtiRadioServiceNotification extends Stub {
        QtiRadioServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Rlog.d(QtiRIL.TAG, "QtiRadio interface service started " + fqName + " " + name + " preexisting =" + preexisting);
            if (!QtiRIL.this.isQtiRadioServiceConnected()) {
                QtiRIL.this.initQtiRadio();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void invokeOemRilRequestRaw(final byte[] data, final Message responseMsg) {
        this.mHandler.post(new Runnable() {
            /* JADX WARNING: Failed to extract finally block: empty outs */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Object obj = null;
                AsyncResult ar = new AsyncResult(null, null, null);
                try {
                    ByteBuffer buf = ByteBuffer.wrap(data);
                    buf.order(ByteOrder.nativeOrder());
                    int msgId = buf.getInt(8);
                    Rlog.d(QtiRIL.TAG, "invokeOemRilRequestRaw msgId=" + msgId);
                    byte[] request = new byte[(data.length - 12)];
                    System.arraycopy(data, 16, request, 0, (data.length - 12) - 4);
                    ar = QtiRIL.this.mQtiRilInterface.sendQcRilHookMsg(msgId, request, QtiRIL.this.mQtiPhoneId);
                    Rlog.d(QtiRIL.TAG, "invokeOemRilRequestRaw sendQcRilHookMsg finished");
                    if (responseMsg != null) {
                        if (responseMsg != null) {
                            obj = responseMsg.obj;
                        }
                        ar.userObj = obj;
                        responseMsg.obj = ar;
                        responseMsg.sendToTarget();
                    }
                } catch (Exception e) {
                    Rlog.d(QtiRIL.TAG, "invokeOemRilRequestRaw Exception" + e.toString());
                    e.printStackTrace();
                    if (responseMsg != null) {
                        if (responseMsg != null) {
                            obj = responseMsg.obj;
                        }
                        ar.userObj = obj;
                        responseMsg.obj = ar;
                        responseMsg.sendToTarget();
                    }
                } catch (Throwable th) {
                    if (responseMsg != null) {
                        if (responseMsg != null) {
                            obj = responseMsg.obj;
                        }
                        ar.userObj = obj;
                        responseMsg.obj = ar;
                        responseMsg.sendToTarget();
                    }
                    throw th;
                }
            }
        });
    }

    private void resetServiceAndRequestList() {
        resetProxyAndRequestList();
        this.mQtiRadio = null;
        this.mQtiRadioResponse = null;
        this.mQtiRadioIndication = null;
        this.mQtiRadioProxyCookie.incrementAndGet();
    }

    private boolean isQtiRadioServiceConnected() {
        return this.mQtiRadio != null;
    }

    private void registerForQtiRadioServiceNotification() {
        try {
            if (!IServiceManager.getService().registerForNotifications(IQtiRadio.kInterfaceName, QTI_HIDL_SERVICE_NAME[this.mQtiPhoneId], this.mServiceNotification)) {
                Rlog.e(TAG, "Failed to register for service start notifications");
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Failed to register for service start notifications. Exception " + ex);
        }
    }

    private synchronized void initQtiRadio() {
        try {
            this.mQtiRadio = IQtiRadio.getService(QTI_HIDL_SERVICE_NAME[this.mQtiPhoneId]);
            if (this.mQtiRadio == null) {
                Rlog.e(TAG, "initQtiRadio: mQtiRadio is null. Return");
                return;
            }
            Rlog.d(TAG, "initQtiRadio: mQtiRadio" + this.mQtiRadio);
            this.mQtiRadio.linkToDeath(this.mDeathRecipient, this.mQtiRadioProxyCookie.incrementAndGet());
            this.mQtiRadioResponse = new QtiRadioResponse(this);
            this.mQtiRadioIndication = new QtiRadioIndication(this);
            this.mQtiRadio.setCallback(this.mQtiRadioResponse, this.mQtiRadioIndication);
        } catch (Exception ex) {
            Rlog.e(TAG, "initQtiRadio: Exception: " + ex);
            resetServiceAndRequestList();
        }
        return;
    }

    public QtiRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public QtiRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mQtiPhoneId = 0;
        this.mQtiRadioProxyCookie = new AtomicLong(0);
        this.mServiceNotification = new QtiRadioServiceNotification();
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiPhoneId = instanceId.intValue();
        Rlog.d(TAG, "QtiRIL");
        this.mDeathRecipient = new QtiRadioProxyDeathRecipient();
        registerForQtiRadioServiceNotification();
        this.mHandler = new Handler(QtiTelephonyComponentFactory.getRilOemLooper());
    }

    private String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0017 A:{ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0011} */
    /* JADX WARNING: Missing block: B:3:0x0017, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0018, code:
            android.telephony.Rlog.e(TAG, "getAtr: Exception: " + r0);
            resetServiceAndRequestList();
     */
    /* JADX WARNING: Missing block: B:6:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getAtr(Message result) {
        Rlog.d(TAG, "getAtr");
        try {
            this.mQtiRadio.getAtr(obtainRequestSerial(200, result, this.mRILDefaultWorkSource));
        } catch (Exception e) {
        }
    }

    Message qtiGetMessageFromRequest(Object request) {
        return getMessageFromRequest(request);
    }

    Object qtiProcessResponse(RadioResponseInfo responseInfo) {
        return processResponse(responseInfo);
    }

    void qtiProcessResponseDone(Object ret, RadioResponseInfo responseInfo, String str) {
        processResponseDone(ret, responseInfo, str);
    }
}
