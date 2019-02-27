package com.qualcomm.qti.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import vendor.qti.hardware.radio.qtiradio.V1_0.IQtiRadioResponse.Stub;
import vendor.qti.hardware.radio.qtiradio.V1_0.QtiRadioResponseInfo;

public class QtiRadioResponse extends Stub {
    static final String QTI_RILJ_LOG_TAG = "QtiRadioResponse";
    QtiRIL mRil;

    public QtiRadioResponse(QtiRIL ril) {
        this.mRil = ril;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, null);
            msg.sendToTarget();
        }
    }

    static RadioResponseInfo toRadioResponseInfo(QtiRadioResponseInfo qtiResponseInfo) {
        RadioResponseInfo responseInfo = new RadioResponseInfo();
        responseInfo.type = qtiResponseInfo.type;
        responseInfo.serial = qtiResponseInfo.serial;
        responseInfo.error = qtiResponseInfo.error;
        return responseInfo;
    }

    private void responseString(RadioResponseInfo responseInfo, String str) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        if (result != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(result, str);
            }
            this.mRil.qtiProcessResponseDone(request, responseInfo, str);
        }
    }

    public void getAtrResponse(QtiRadioResponseInfo qtiResponseInfo, String atr) {
        Rlog.d(QTI_RILJ_LOG_TAG, "getAtrResponse");
        responseString(toRadioResponseInfo(qtiResponseInfo), atr);
    }
}
