package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.vivo.common.VivoCollectData;
import java.util.ArrayList;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoPplmnCollect {
    private static final Uri CONTENT_URI = Uri.parse("content://telephonyCollectData/pplmn");
    private static final String TAG = "VivoPplmnCollect";
    private ArrayList<PplmnInfo> mAllPplmnInfo;
    private Context mContext;
    private int mDataRat;
    private int mInstanceId;
    private Phone mPhone;
    private String mPlmn;
    private VivoCollectData mVivoCollectData;

    private class PplmnInfo {
        public int mDataRat;
        public String mOperator;
        public String mPlmn;

        public PplmnInfo(String plmn, String operator, int dataRat) {
            this.mPlmn = plmn;
            this.mOperator = operator;
            this.mDataRat = dataRat;
        }
    }

    public VivoPplmnCollect(Context context, Phone phone, int instanceId) {
        this.mVivoCollectData = null;
        this.mAllPplmnInfo = null;
        this.mAllPplmnInfo = new ArrayList();
        this.mContext = context;
        this.mPhone = phone;
        this.mInstanceId = instanceId;
        this.mPlmn = "";
        this.mDataRat = 0;
        if (this.mVivoCollectData == null) {
            this.mVivoCollectData = new VivoCollectData(this.mContext);
        }
    }

    public void setPplmnInfo(String operator, int dataRat) {
        if (!TextUtils.isEmpty(operator) && dataRat != 0 && !operator.equals("00000")) {
            if (this.mDataRat != dataRat || TextUtils.isEmpty(this.mPlmn)) {
                this.mDataRat = dataRat;
                String plmn = this.mPhone.getOperatorNumeric();
                if (TextUtils.isEmpty(plmn)) {
                    log("plmn is empty.");
                    return;
                } else if (!plmn.equals(this.mPlmn)) {
                    log("setPplmnInfo plmn = " + plmn + " mPlmn = " + this.mPlmn);
                    this.mPlmn = plmn;
                }
            }
            if (!isPplmnInfoExist(this.mPlmn, operator, this.mDataRat)) {
                saveAndCollectPplmnInfo(this.mPlmn, operator, dataRat);
            }
        }
    }

    public void dispose() {
        if (this.mAllPplmnInfo != null) {
            this.mAllPplmnInfo.clear();
        }
    }

    private boolean isPplmnInfoExist(String plmn, String operator, int dataRat) {
        boolean bExist = false;
        for (PplmnInfo pp : this.mAllPplmnInfo) {
            if (plmn.equals(pp.mPlmn) && operator.equals(pp.mOperator) && pp.mDataRat == dataRat) {
                bExist = true;
                break;
            }
        }
        if (!bExist) {
            this.mAllPplmnInfo.add(new PplmnInfo(plmn, operator, dataRat));
        }
        return bExist;
    }

    private void saveAndCollectPplmnInfo(final String plmn, final String operator, final int dataRat) {
        new Thread(new Runnable() {
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = VivoPplmnCollect.this.mPhone.getContext().getContentResolver().query(VivoPplmnCollect.CONTENT_URI, null, "plmn = '" + plmn + "' and " + "operator = '" + operator + "' and " + "dataRat = '" + dataRat + "'", null, null);
                    boolean bFound = false;
                    if (cursor != null && cursor.getCount() > 0) {
                        bFound = true;
                    }
                    VivoPplmnCollect.this.log("saveAndCollectPplmnInfo plmn = " + plmn + " operator = " + operator + " dataRat = " + dataRat + " bFound = " + bFound);
                    if (!bFound) {
                        ContentValues value = new ContentValues();
                        value.put("plmn", plmn);
                        value.put("operator", operator);
                        value.put("dataRat", Integer.valueOf(dataRat));
                        Uri uri = VivoPplmnCollect.this.mPhone.getContext().getContentResolver().insert(VivoPplmnCollect.CONTENT_URI, value);
                        VivoPplmnCollect.this.collectData(plmn, operator, String.valueOf(dataRat));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    VivoPplmnCollect.this.log("saveAndCollectPplmnInfo execption e = " + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }).start();
    }

    private void collectData(String plmn, String operator, String rat) {
        HashMap<String, String> params = new HashMap();
        params.put("plmn", plmn);
        params.put("op", operator);
        params.put("rat", rat);
        params.put("band", "");
        params.put("fre", "");
        params.put("ext1", "");
        params.put("ext2", "");
        params.put("ext3", "");
        this.mVivoCollectData.writeData("809", "8091", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
    }

    private void log(String str) {
        Rlog.e(TAG + this.mInstanceId, str);
    }
}
