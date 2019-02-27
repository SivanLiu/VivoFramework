package com.qualcomm.qti.internal.telephony;

import vendor.qti.hardware.radio.qtiradio.V1_0.IQtiRadioIndication.Stub;

public class QtiRadioIndication extends Stub {
    static final String QTI_RILJ_LOG_TAG = "QtiRadioIndication";
    QtiRIL mRil;

    public QtiRadioIndication(QtiRIL ril) {
        this.mRil = ril;
    }

    public void qtiRadioIndication(int type) {
    }
}
