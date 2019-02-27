package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.telephony.FtTelephony;
import android.telephony.FtTelephonyAdapter;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Base64;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;
import com.vivo.common.VivoCloudData;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONException;
import org.json.JSONObject;

public class DataCollectionUtils extends CollectonUtils {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static long BOOT_COMPLETED_TIME = 0;
    public static final int CAUSE_MODEM_RESTART = -100;
    public static final int CAUSE_RIL_RECONNECTION = -101;
    private static long COLLECTION_DELAY = 300000;
    public static int COLLECTION_INTERVAL = 60000;
    public static final int COLLECTION_TIME_EXPIRE = 86400000;
    public static final int COLLECTION_TYPE_OUT_OF_SERVICE = 2;
    public static final int COLLECTION_TYPE_RAT_CHANGE = 3;
    private static long CollectionStartTime = 0;
    private static String DATA_DENIED_REASON = "DATA_DENIED_REASON";
    public static final int DATA_REGISTRATION_STATE = 21;
    private static String DATA_REG_STATE = "DATA_REG_STATE";
    private static final String[] DEFAULT_MODEM_INFO = new String[]{"ch:65535", "pci:65535", "bd:65535", "s:65535", "q:65535", "snr:65535"};
    private static final String DEFAULT_MODEM_INFO_FULL = "c,ch:65535,pci:65535,bd:65535,s:65535,q:65535,snr:65535&h,ch:65535,pci:65535,bd:65535,s:65535,q:65535,snr:65535&l,ch:65535,pci:65535,bd:65535,s:65535,q:65535,snr:65535";
    private static final String DEFAULT_MODEM_INFO_STR = "ch:65535,pci:65535,bd:65535,s:65535,q:65535,snr:65535";
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_1 = 1003;
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_2 = 1004;
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_OFTEN = 1006;
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_OFTEN1 = 1000;
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_OFTEN2 = 1001;
    public static final int EVENT_CLEAR_OUT_OF_SERVICE_TIME_EXPIRE = 1005;
    public static final int EVENT_CLEAR_RAT_CHANGE = 1002;
    public static final int EVENT_OUT_OF_SERVICE = 0;
    public static final int EVENT_OUT_OF_SERVICE_OFTEN = 1;
    public static final int EVENT_RAT_CHANGE = 2;
    private static final int INFO_TYPE_CDMA = 0;
    private static final int INFO_TYPE_GSM = 0;
    private static final int INFO_TYPE_HSPA = 1;
    private static final int INFO_TYPE_LTE = 2;
    private static final int INFO_TYPE_UMTS = 1;
    private static final int INFO_TYPE_UNKNOWN = -1;
    private static final long INVALID = 65535;
    private static final String LOG_TAG = "DataCollectionUtils";
    private static int MAX_REPORT_OF_DAY = 60;
    private static final int MAX_REPORT_OF_MINUTE = 1;
    private static final int MISC_INFO_MODEM_NETWORK_INFO_REQUEST = 21;
    private static final int MISC_INFO_MODEM_NETWORK_INFO_REQUEST_NEW = 40;
    public static int OUT_OF_SERIVCE_COLLECTION_DELAY = 840000;
    private static final int OUT_OF_SERVICE_COUNT = 3;
    private static final long OUT_OF_SERVICE_DURATION_MAX = 2592000000L;
    private static final int OUT_OF_SERVICE_OFTEN_COUNT = 3;
    private static final int OUT_OF_SERVICE_OFTEN_DDS_COUNT = 5;
    public static final int OUT_OF_SERVICE_OFTEN_TIME_EXPIRE = 600000;
    public static final int OUT_OF_SERVICE_TIME_EXPIRE = 86400000;
    public static final int OUT_OF_SERV_REPORT_DELAY = 20000;
    private static final int RAT_CHANGE_COUNT = 5;
    public static final int RAT_CHANGE_TIME_EXPIRE = 300000;
    private static final int REPORT_COUNT = 3;
    private static final int SIGNALSTRENGTH__RECORD_COUNT = 5;
    private static final String SPRE_COLLECTION_START = "collectionstart";
    private static final String SPRE_OUT_OF_SERV_COUNT = "outofservcount";
    private static final String SPRE_OUT_OF_SERV_LAST = "outofservlast";
    private static final String SPRE_OUT_OF_SERV_OFTEN_COUNT = "outofservoftencount";
    private static final String SPRE_OUT_OF_SERV_OFTEN_LAST = "outofservoftenlast";
    private static final String SPRE_RAT_CHANGE_COUNT = "ratchangecount";
    private static final String SPRE_RAT_CHANGE_LAST = "ratchangelast";
    private static String VOICE_DENIED_REASON = "VOICE_DENIED_REASON";
    public static final int VOICE_REGISTRATION_STATE = 20;
    private static String VOICE_REG_STATE = "VOICE_REG_STATE";
    public static int isPreferredNetworkType = 0;
    private static Context mContext;
    private static DataCollectionUtils mDataCollectionUtils;
    public static int mDataPhoneId = -1;
    private static int mOutOfServCount = 0;
    private static long mOutOfServLastTime = -1;
    private static int mOutOfServOftenCount = 0;
    private static int mOutOfServOftenCountDds = 0;
    private static long mOutOfServOftenLastTime = -1;
    private static Queue<CollectionBean> mQueuOutOfServ1 = new LinkedBlockingQueue<CollectionBean>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<CollectionBean> mQueuOutOfServ2 = new LinkedBlockingQueue<CollectionBean>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<CollectionBean> mQueuOutOfServOften1 = new LinkedBlockingQueue<CollectionBean>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<CollectionBean> mQueuOutOfServOften2 = new LinkedBlockingQueue<CollectionBean>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<CollectionBean> mQueuRatChange = new LinkedBlockingQueue<CollectionBean>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<SignalStrength> mQueuSignalStrength1 = new LinkedBlockingQueue<SignalStrength>() {
        private static final long serialVersionUID = 1;
    };
    private static Queue<SignalStrength> mQueuSignalStrength2 = new LinkedBlockingQueue<SignalStrength>() {
        private static final long serialVersionUID = 1;
    };
    private static int mRatChangeCount = 0;
    private static long mRatChangeLastTime = -1;
    private static ArrayList<HashMap<String, Integer>> mRegStateArrLst = new ArrayList<HashMap<String, Integer>>() {
        {
            add(new HashMap<String, Integer>() {
                private static final long serialVersionUID = 1;

                {
                    put("VOICE_REG_STATE", Integer.valueOf(4));
                    put("VOICE_DENIED_REASON", Integer.valueOf(0));
                    put("DATA_REG_STATE", Integer.valueOf(4));
                    put("DATA_DENIED_REASON", Integer.valueOf(0));
                }
            });
            add(new HashMap<String, Integer>() {
                private static final long serialVersionUID = 1;

                {
                    put("VOICE_REG_STATE", Integer.valueOf(4));
                    put("VOICE_DENIED_REASON", Integer.valueOf(0));
                    put("DATA_REG_STATE", Integer.valueOf(4));
                    put("DATA_DENIED_REASON", Integer.valueOf(0));
                }
            });
        }
    };
    private static int reboot_flag = 0;
    private final String ACTION_UPLOAD_ICCID = "vivo.intent.action.networkiccidupload";
    private boolean AFTER_BOOT_COMPLETED_5_MIN = false;
    protected final String DEBUG_FILENAME = "datacollection";
    private final String LOG_EVENT_ID = VivoMassExceptionAPI.ID_EVENTID_MessData_v4;
    private final String LOG_EVENT_TYPE = VivoMassExceptionAPI.ID_EVENTID_MessData_OUT_OF_SERVICE_v4;
    private String OUT_OF_SERVICE_ONTEN_SIM1_TXT = "outservoften1.txt";
    private String OUT_OF_SERVICE_ONTEN_SIM2_TXT = "outservoften2.txt";
    private String OUT_OF_SERVICE_SIM1_TXT = "outserv1.txt";
    private String OUT_OF_SERVICE_SIM2_TXT = "outserv2.txt";
    private String RAT_CHANGE_TXT = "ratchange.txt";
    IntentFilter filter = new IntentFilter(ACTION_BOOT_COMPLETED);
    private boolean hasReportRilReCon = false;
    private String iccid = "-1";
    private String locationProvider;
    private int mDefaultDataPhoneId = -1;
    private FtTelephony mFtTel;
    private boolean mRadioUnavailableflag = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DataCollectionUtils.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                DataCollectionUtils.log("BroadcastReceiver onReceive ACTION_BOOT_COMPLETED");
                DataCollectionUtils.this.setRilReconnectionFlag(false);
                DataCollectionUtils.BOOT_COMPLETED_TIME = System.currentTimeMillis();
            }
        }
    };
    private boolean mRilReconnectionFlag = false;
    private VivoCloudData mVivoCloudData = null;
    private String plmn = "-1";

    public boolean getRilReconnectionFlag() {
        if (DBG) {
            log("getRilReconnectionFlag: " + this.mRilReconnectionFlag);
        }
        return this.mRilReconnectionFlag;
    }

    public void setRilReconnectionFlag(boolean rilReconnection) {
        if (DBG) {
            log("setRilReconnectionFlag: " + rilReconnection + " hasReportRilReCon: " + this.hasReportRilReCon);
        }
        if (this.hasReportRilReCon) {
            this.mRilReconnectionFlag = rilReconnection;
        } else {
            this.hasReportRilReCon = true;
        }
    }

    public boolean getRadioUnavailableflag() {
        if (DBG) {
            log("getRadioUnavailableflag: " + this.mRadioUnavailableflag);
        }
        return this.mRadioUnavailableflag;
    }

    public void setRadioUnavailableflag(boolean radiostate) {
        if (!isAirModeOn()) {
            if (DBG) {
                log("setRadioUnavailableflag: " + radiostate);
            }
            this.mRadioUnavailableflag = radiostate;
        }
    }

    public String getPlmn() {
        return this.plmn;
    }

    public void setPlmn(String plmn) {
        if (plmn != null && !"".equals(plmn) && !"0".equals(plmn)) {
            this.plmn = plmn;
        }
    }

    public boolean getDefaultDataSubChangeFlag() {
        int dataPhoneId = this.mFtTel.getDefaultDataPhoneId();
        if (DBG) {
            log("getDefaultDataSubChangeFlag mDefaultDataPhoneId:" + this.mDefaultDataPhoneId + " dataPhoneId: " + dataPhoneId);
        }
        if (DBG) {
            return false;
        }
        if (this.mDefaultDataPhoneId == -1 || this.mDefaultDataPhoneId == dataPhoneId) {
            this.mDefaultDataPhoneId = dataPhoneId;
            return false;
        }
        this.mDefaultDataPhoneId = dataPhoneId;
        return true;
    }

    public DataCollectionUtils(Context context) {
        super(context);
        mContext = context;
        mContext.registerReceiver(this.mReceiver, new IntentFilter(ACTION_BOOT_COMPLETED));
        this.mFtTel = FtTelephonyAdapter.getFtTelephony(mContext);
        this.mVivoCloudData = VivoCloudData.getInstance(mContext);
        log("DBG: " + DBG);
        if (DBG) {
            COLLECTION_INTERVAL = OUT_OF_SERV_REPORT_DELAY;
            MAX_REPORT_OF_DAY = 65535;
            OUT_OF_SERIVCE_COLLECTION_DELAY = 60000;
        }
        init();
    }

    private void init() {
        BOOT_COMPLETED_TIME = System.currentTimeMillis();
        CollectionStartTime = Long.valueOf(getSharedPreferences(SPRE_COLLECTION_START)).longValue();
        if (INVALID == CollectionStartTime) {
            CollectionStartTime = BOOT_COMPLETED_TIME;
            mOutOfServLastTime = CollectionStartTime;
            mOutOfServOftenLastTime = CollectionStartTime;
            mRatChangeLastTime = CollectionStartTime;
            mOutOfServCount = 0;
            mOutOfServOftenCount = 0;
            mRatChangeCount = 0;
            setSharedPreferences(SPRE_COLLECTION_START, String.valueOf(CollectionStartTime));
            setSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_COUNT, String.valueOf(mOutOfServOftenCount));
            setSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_LAST, String.valueOf(mOutOfServOftenLastTime));
            setSharedPreferences(SPRE_OUT_OF_SERV_COUNT, String.valueOf(mOutOfServCount));
            setSharedPreferences(SPRE_OUT_OF_SERV_LAST, String.valueOf(mOutOfServLastTime));
            setSharedPreferences(SPRE_RAT_CHANGE_COUNT, String.valueOf(mRatChangeCount));
            setSharedPreferences(SPRE_RAT_CHANGE_LAST, String.valueOf(mRatChangeLastTime));
            return;
        }
        mOutOfServCount = Integer.valueOf(getSharedPreferences(SPRE_OUT_OF_SERV_COUNT)).intValue();
        mOutOfServLastTime = Long.valueOf(getSharedPreferences(SPRE_OUT_OF_SERV_LAST)).longValue();
        mOutOfServOftenCount = Integer.valueOf(getSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_COUNT)).intValue();
        mOutOfServOftenLastTime = Long.valueOf(getSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_LAST)).longValue();
        mRatChangeCount = Integer.valueOf(getSharedPreferences(SPRE_RAT_CHANGE_COUNT)).intValue();
        mRatChangeLastTime = Long.valueOf(getSharedPreferences(SPRE_RAT_CHANGE_LAST)).longValue();
    }

    public boolean addToQueuSignalStrength(SignalStrength signal, int phoneid) {
        if (DBG) {
            log("addToQueuSignalStrength phoneid " + phoneid + " SignalStrength: " + signal.toString());
        }
        switch (phoneid) {
            case 0:
                if (mQueuSignalStrength1.size() > 5) {
                    mQueuSignalStrength1.poll();
                }
                mQueuSignalStrength1.offer(signal);
                break;
            case 1:
                if (mQueuSignalStrength2.size() > 5) {
                    mQueuSignalStrength2.poll();
                }
                mQueuSignalStrength2.offer(signal);
                break;
        }
        return false;
    }

    public boolean addTomQueuOutOfServOften(CollectionBean outofserv) {
        if (DBG) {
            log("addTomQueuOutOfServOften sim " + outofserv.getSim_id());
        }
        outofserv.setReserved1(String.valueOf(1));
        int count = 3;
        if (this.mFtTel.getDefaultDataPhoneId() == outofserv.getSim_id()) {
            count = 5;
        }
        if (outofserv.getSim_id() == 0) {
            if (isWriteToDataBase(1, (long) count, 600000, outofserv, mQueuOutOfServOften1)) {
                return writeToDatabase(mQueuOutOfServOften1, this.OUT_OF_SERVICE_ONTEN_SIM1_TXT);
            }
        } else if (1 == outofserv.getSim_id()) {
            if (isWriteToDataBase(1, (long) count, 600000, outofserv, mQueuOutOfServOften2)) {
                return writeToDatabase(mQueuOutOfServOften2, this.OUT_OF_SERVICE_ONTEN_SIM2_TXT);
            }
        }
        return false;
    }

    public boolean addTomQueuRatChange(CollectionBean ratchange) {
        if (DBG) {
            log("addTomQueuRatChange sim " + ratchange.getSim_id());
        }
        if (isWriteToDataBase(2, 5, 300000, ratchange, mQueuRatChange)) {
            return writeToDatabase(mQueuRatChange, this.RAT_CHANGE_TXT);
        }
        return false;
    }

    public boolean addTomQueuOutOfServ(CollectionBean outofserv) {
        if (DBG) {
            log("addTomQueuOutOfServ sim " + outofserv.getSim_id());
        }
        outofserv.setReserved1(String.valueOf(0));
        if (outofserv.getSim_id() == 0) {
            if (isWriteToDataBase(0, 3, 86400000, outofserv, mQueuOutOfServ1)) {
                return writeToDatabase(mQueuOutOfServ1, this.OUT_OF_SERVICE_SIM1_TXT);
            }
        } else if (1 == outofserv.getSim_id()) {
            if (isWriteToDataBase(0, 3, 86400000, outofserv, mQueuOutOfServ2)) {
                return writeToDatabase(mQueuOutOfServ2, this.OUT_OF_SERVICE_SIM2_TXT);
            }
        }
        return false;
    }

    public void reportOutOfServ(CollectionBean outofserv, String type, int dataOrvoice, int screnStatus) {
        if (outofserv == null) {
            log("reportOutOfServ CollectionBean is null");
            return;
        }
        if (DBG) {
            log("reportOutOfServ type: " + type);
        }
        setRejectCause(outofserv, outofserv.getSim_id(), dataOrvoice);
        long time_now = System.currentTimeMillis();
        if (DBG) {
            outofserv.setReserved1(type);
            reportToServer(outofserv, screnStatus, dataOrvoice);
            log("uploadLog LOG_EVENT_ID: 1203 LOG_EVENT_TYPE: 1203_2");
            uploadLog(VivoMassExceptionAPI.ID_EVENTID_MessData_v4, VivoMassExceptionAPI.ID_EVENTID_MessData_OUT_OF_SERVICE_v4, null);
        } else if (time_now - outofserv.getCurrent() > 20000 && time_now - BOOT_COMPLETED_TIME > COLLECTION_DELAY) {
            outofserv.setReserved1(type);
            if (isInvalidPlmn(outofserv.getPlmn())) {
                outofserv.setPlmn(getPlmn());
            }
            reportToServer(outofserv, screnStatus, dataOrvoice);
            log("uploadLog LOG_EVENT_ID: 1203 LOG_EVENT_TYPE: 1203_2");
            uploadLog(VivoMassExceptionAPI.ID_EVENTID_MessData_v4, VivoMassExceptionAPI.ID_EVENTID_MessData_OUT_OF_SERVICE_v4, null);
        }
    }

    private void reportToServer(CollectionBean outofserv, int screnStatus, int dataOrvoice) {
        if (outofserv == null) {
            log("reportToServer CollectionBean is null");
            return;
        }
        int majorSim;
        int DefaultDataSubID = this.mFtTel.getDefaultDataPhoneId();
        log("mFtTel.getDefaultDataPhoneId() is " + DefaultDataSubID);
        if (DefaultDataSubID == outofserv.getSim_id()) {
            majorSim = 11;
        } else {
            majorSim = 10;
        }
        log("majorSim is " + majorSim);
        ArrayList<String> data = new ArrayList();
        try {
            JSONObject content = new JSONObject();
            content.put("eventId", "00011|012");
            JSONObject dt = new JSONObject();
            dt.put("extype", 1);
            dt.put("subtype", 2);
            dt.put("moduleid", 600);
            dt.put("loc", this.mVivoCloudData.getLocation());
            dt.put("sim_id", majorSim);
            dt.put("status", outofserv.getOutServiceType());
            dt.put("lac", outofserv.getLac());
            dt.put("cid", outofserv.getCid());
            dt.put("cs", outofserv.getCs());
            dt.put("ps", outofserv.getPs());
            dt.put("plmn", Integer.parseInt(outofserv.getPlmn()));
            dt.put("cs2", outofserv.getCs2());
            dt.put("ps2", outofserv.getPs2());
            dt.put("sinr", outofserv.getSnr());
            if (dataOrvoice == 20) {
                String csRejectCause = SystemProperties.get("ril.cs_reject.cause", "-1");
                if ("-1".equals(csRejectCause)) {
                    dt.put("cause", outofserv.getRegCode());
                } else {
                    dt.put("cause", csRejectCause);
                    try {
                        SystemProperties.set("ril.cs_reject.cause", "-1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log("reportToServer csRejectCause: " + csRejectCause + ",outofserv.getRegCode()=" + outofserv.getRegCode());
            } else {
                String psRejectCause = SystemProperties.get("ril.ps_reject.cause", "-1");
                if ("-1".equals(psRejectCause)) {
                    dt.put("cause", outofserv.getRegCode());
                } else {
                    dt.put("cause", psRejectCause);
                    try {
                        SystemProperties.set("ril.ps_reject.cause", "-1");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                log("reportToServer psRejectCause: " + psRejectCause + ",outofserv.getRegCode()=" + outofserv.getRegCode());
            }
            dt.put("timelong", Integer.parseInt(outofserv.getReserved1()) + "," + screnStatus);
            dt.put("band", outofserv.getBand());
            dt.put("s_qua", outofserv.getS_qua());
            dt.put("m_signal", outofserv.getM_signal());
            dt.put("arfcn", outofserv.getArfcn());
            dt.put("times", 1);
            dt.put("otime", outofserv.getCurrent());
            content.put("dt", dt);
            data.add(content.toString());
            if (DBG) {
                log("reportToServer data: " + dt.toString() + ",screnStatus=" + screnStatus);
                writeFile("datacollectioninfo.txt", dt.toString(), false);
            }
            this.mVivoCloudData.sendData(600, data);
        } catch (JSONException e3) {
            e3.printStackTrace();
        }
        String sim_id = String.valueOf(outofserv.getSim_id());
        String status = outofserv.getOutServiceType();
        String lac = String.valueOf(outofserv.getLac());
        String cid = outofserv.getCid();
        String cs = outofserv.getCs();
        String ps = outofserv.getPs();
        String plmn = outofserv.getPlmn();
        String cs2 = outofserv.getCs2();
        String ps2 = outofserv.getPs2();
        String sinr = String.valueOf(outofserv.getSnr());
        String cause = String.valueOf(outofserv.getRegCode());
        String timelong = outofserv.getReserved1();
        String band = outofserv.getBand();
        String s_qua = String.valueOf(outofserv.getS_qua());
        String m_signal = String.valueOf(outofserv.getM_signal());
        String outOfServiceLogString = "OUTSERVICE SIM_ID:" + sim_id + " STATUS:" + status + " LAC:" + lac + " CID:" + cid + " CS:" + cs + " PS:" + ps + " PLMN:" + plmn + " CS2:" + cs2 + " PS2:" + ps2 + " SINR:" + sinr + " CAUSE:" + cause + " TIMELONG:" + timelong + " BAND:" + band + " S_QUA:" + s_qua + " M_SIGNAL:" + m_signal + " ARFCN:" + String.valueOf(outofserv.getArfcn());
        Intent intent = new Intent();
        intent.setAction("vivo.intent.action.neterrlog");
        intent.setPackage("com.vivo.networkimprove");
        intent.putExtra("neterrlogInfo", outOfServiceLogString);
        intent.putExtra("neterrlogId", "2");
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast(int moduleid, ArrayList<String> data) {
        Intent it = new Intent("com.vivo.intent.action.CLOUD_DIAGNOSIS");
        it.putExtra("attr", 1);
        it.putExtra("module", moduleid);
        it.putStringArrayListExtra("data", data);
        it.setPackage("com.bbk.iqoo.logsystem");
        mContext.sendBroadcast(it);
    }

    public void reportIccId(int phoneId) {
        Intent it = new Intent("vivo.intent.action.networkiccidupload");
        it.setPackage("com.vivo.networkimprove");
        if (DBG) {
            log("reportIccId is phoneId: " + phoneId);
        }
        it.putExtra("phoneId", phoneId);
        mContext.sendBroadcast(it);
    }

    protected boolean isWriteToDataBase(int type, long count, int interval, CollectionBean item, Queue<CollectionBean> queue) {
        boolean re = false;
        long curent = item.getCurrent();
        if (DBG) {
            log("BOOT_COMPLETED_TIME: " + BOOT_COMPLETED_TIME + "  curent:  " + curent + " AFTER_BOOT_COMPLETED_5_MIN: " + this.AFTER_BOOT_COMPLETED_5_MIN);
        }
        if (DBG) {
            this.AFTER_BOOT_COMPLETED_5_MIN = true;
        } else if (!this.AFTER_BOOT_COMPLETED_5_MIN) {
            if (curent - BOOT_COMPLETED_TIME <= COLLECTION_DELAY) {
                return false;
            }
            this.AFTER_BOOT_COMPLETED_5_MIN = true;
        }
        if (DBG) {
            log("type:  " + type + " interval: " + interval + " count:  " + count + "queue.size: " + queue.size());
        }
        if (this.AFTER_BOOT_COMPLETED_5_MIN) {
            if (((long) queue.size()) < count - 1) {
                queue.offer(item);
                re = false;
            } else {
                CollectionBean data = (CollectionBean) queue.peek();
                if (DBG) {
                    log("curent-data.getCurrent: " + (curent - data.getCurrent()));
                }
                if (curent - data.getCurrent() > ((long) interval) || (isAllowedToReport(type, item) ^ 1) != 0) {
                    queue.poll();
                    if (DBG) {
                        log("after poll queue.size: " + queue.size());
                    }
                    queue.offer(item);
                    if (DBG) {
                        log("after offer queue.size: " + queue.size());
                    }
                    re = false;
                } else {
                    queue.offer(item);
                    if (DBG) {
                        log("after offer queue.size: " + queue.size());
                    }
                    re = true;
                }
            }
        }
        return re;
    }

    protected boolean writeToDatabase(Queue<CollectionBean> queue, String name) {
        debugInfo(name);
        String reportdata = "";
        int listSize = buildArrayListContent(queue).size();
        return false;
    }

    public String[][] getModemInfo(int phoneid) {
        String info = sendMiscInfo(phoneid, 40, VivoNetLowlatency.MISC_INFO_DEFAULT_SEND_BUFFER);
        if (DBG) {
            log("getModemInfo info: " + info);
        }
        if (info == null || "default null".equals(info) || info.length() < 90) {
            info = DEFAULT_MODEM_INFO_FULL;
        }
        if (DBG) {
            log("getModemInfo info: " + info);
        }
        String[][] infos = parseModemInfo(getModemInfoFormType(info));
        if (DBG) {
            log("writeFile ");
            writeFile("modemInfo" + phoneid + ".txt", info, true);
        }
        return infos;
    }

    private String[][] parseModemInfo(String[] infos) {
        String[][] modeminfo = new String[3][];
        int i = 0;
        while (i < 3) {
            try {
                if (infos[i] != null) {
                    if (DBG) {
                        log("parseModemInfo info: " + infos[i]);
                    }
                    String[] info = infos[i].split(",");
                    if (info == null || info.length < 6) {
                        modeminfo[i] = DEFAULT_MODEM_INFO;
                    } else {
                        modeminfo[i] = info;
                    }
                } else {
                    modeminfo[i] = DEFAULT_MODEM_INFO;
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modeminfo;
    }

    public void fillModemInfo(String[][] infos, CollectionBean bean, int netType) {
        if (infos == null || bean == null) {
            log("fillModemInfo illegal args");
            return;
        }
        try {
            int type = TelephonyManager.getNetworkClass(netType) - 1;
            if (type < 0 || type > 2) {
                type = 0;
            }
            String[] info = infos[type];
            if (DBG) {
                log("fillModemInfo rat:" + netType + " type: " + type);
            }
            bean.setArfcn(Integer.parseInt(info[0].substring(info[0].indexOf(":") + 1).trim()));
            bean.setPci(Integer.parseInt(info[1].substring(info[1].indexOf(":") + 1).trim()));
            bean.setBand(info[2].substring(info[2].indexOf(":") + 1));
            bean.setM_signal(Integer.parseInt(info[3].substring(info[3].indexOf(":") + 1).trim()));
            bean.setS_qua(Integer.parseInt(info[4].substring(info[4].indexOf(":") + 1).trim()));
            bean.setSnr(Integer.parseInt(info[5].substring(info[5].indexOf(":") + 1).trim()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] getModemInfoFormType(String info) {
        String[] infos = new String[3];
        try {
            String[] tmpinfos = info.split("&");
            if (tmpinfos != null) {
                int i = 0;
                for (String s : tmpinfos) {
                    if (DBG) {
                        log("getModemInfoFormType info: " + s);
                    }
                    infos[i] = s.substring(s.indexOf(",") + 1);
                    i++;
                }
            } else {
                infos[0] = DEFAULT_MODEM_INFO_STR;
                infos[1] = DEFAULT_MODEM_INFO_STR;
                infos[2] = DEFAULT_MODEM_INFO_STR;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infos;
    }

    private void writeFile(final String filename, final String content, final boolean needTime) {
        log("writeFile filename: " + filename + " content: " + content);
        new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:35:0x0138 A:{SYNTHETIC, Splitter: B:35:0x0138} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                IOException e;
                Throwable th;
                String dir = Environment.getExternalStorageDirectory().toString() + File.separator + "datacollection";
                File f1 = new File(dir);
                if (!f1.isDirectory()) {
                    f1.mkdirs();
                }
                File f2 = new File(dir + File.separator + filename);
                DataCollectionUtils.log("file path: " + dir + File.separator + filename);
                RandomAccessFile rf = null;
                try {
                    if (!f2.isFile()) {
                        f2.createNewFile();
                    }
                    RandomAccessFile rf2 = new RandomAccessFile(dir + File.separator + filename, "rw");
                    try {
                        long length = rf2.length();
                        DataCollectionUtils.log("length: " + length);
                        String fileContent = "";
                        if (needTime) {
                            fileContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
                        }
                        if (length != 0) {
                            fileContent = "\n" + fileContent + content;
                            rf2.seek(length);
                            rf2.write(fileContent.getBytes());
                        } else {
                            rf2.write((fileContent + content).getBytes());
                        }
                        if (rf2 != null) {
                            try {
                                rf2.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        rf = rf2;
                    } catch (IOException e3) {
                        e2 = e3;
                        rf = rf2;
                    } catch (Throwable th2) {
                        th = th2;
                        rf = rf2;
                        if (rf != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e2 = e4;
                    try {
                        e2.printStackTrace();
                        if (rf != null) {
                            try {
                                rf.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (rf != null) {
                            try {
                                rf.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
            }
        }.start();
    }

    public String sendMiscInfo(int phoneId, int commandId, String buffer) {
        try {
            VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_sendMiscInfo");
            ITelephony iTelephony = Stub.asInterface(ServiceManager.getService("phone"));
            param.put("phoneId", Integer.valueOf(phoneId));
            param.put("commandId", Integer.valueOf(commandId));
            param.put("buffer", buffer);
            VivoTelephonyApiParams ret = iTelephony.vivoTelephonyApi(param);
            if (ret != null) {
                return (String) ret.getAsObject("response");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearDeniedReason(int type, int phoneid) {
        switch (type) {
            case 20:
                if (phoneid >= 0 && 1 >= phoneid) {
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(VOICE_DENIED_REASON, Integer.valueOf(0));
                    break;
                }
                return;
                break;
            case 21:
                if (phoneid >= 0 && 1 >= phoneid) {
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(DATA_DENIED_REASON, Integer.valueOf(0));
                    break;
                }
                return;
        }
    }

    public void setRejectCause(CollectionBean outofserv, int phoneId, int type) {
        log("DataCollectionUtils radio: " + getRadioUnavailableflag() + " rilstate: " + getRilReconnectionFlag());
        if (getRadioUnavailableflag()) {
            if (getRilReconnectionFlag()) {
                outofserv.setRegCode(CAUSE_RIL_RECONNECTION);
            } else {
                outofserv.setRegCode(-100);
            }
        } else if (21 == type) {
            outofserv.setRegCode(getDataRegistrationDeniedReason(phoneId));
        } else if (20 == type) {
            outofserv.setRegCode(getVoiceRegistrationDeniedReason(phoneId));
        }
        clearDeniedReason(type, phoneId);
    }

    private boolean isInvalidPlmn(String plmn) {
        return (plmn == null || "".equals(plmn)) ? true : "-1".equals(plmn);
    }

    private boolean isInvalidIccId(String iccid) {
        return iccid == null || iccid.length() < 10;
    }

    public String doRsaEncrypt(String source) {
        String result = "-1";
        try {
            byte[] plain = source.getBytes(Charset.forName("UTF-8"));
            KeyStore keystore = KeyStore.getInstance();
            if (keystore == null) {
                return result;
            }
            Method vivoRSAEncrypt = KeyStore.class.getMethod("vivoRSAEncrypt", new Class[]{byte[].class});
            if (vivoRSAEncrypt == null) {
                return result;
            }
            byte[] encrypted = (byte[]) vivoRSAEncrypt.invoke(keystore, new Object[]{plain});
            if (encrypted != null) {
                return new String(Base64.encode(encrypted, 0)).replace("\n", "");
            }
            return result;
        } catch (Exception e) {
            if (DBG) {
                log("Encrypt failed " + e);
            }
        }
    }

    protected String getLocation() {
        String temp = "-1";
        try {
            LocationManager mlocationManager = (LocationManager) mContext.getSystemService("location");
            Location location = mlocationManager.getLastKnownLocation("gps");
            if (location == null) {
                location = mlocationManager.getLastKnownLocation("network");
            }
            if (location == null) {
                temp = "-1";
                log("getLocation location is null");
            } else {
                temp = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
                log("getLocation str   temp=" + temp);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String temp_after = doRsaEncrypt(temp);
        logd("ss getLocation temp=" + temp + ",doRsaEncrypt=" + temp_after);
        return temp_after;
    }

    public static void clearQueu(int type) {
        if (DBG) {
            log("clear queue type:  " + type);
        }
        if (!DBG || (isDevMode ^ 1) == 0) {
            switch (type) {
                case 1002:
                    mQueuRatChange.clear();
                    break;
                case 1003:
                    mQueuOutOfServOften1.clear();
                    break;
                case 1004:
                    mQueuOutOfServOften2.clear();
                    break;
                case 1006:
                    mQueuOutOfServOften1.clear();
                    mQueuOutOfServOften2.clear();
                    break;
                case 86400000:
                    mQueuOutOfServ1.clear();
                    mQueuOutOfServ2.clear();
                    break;
            }
        }
    }

    protected ArrayList<String> buildArrayListContent(Queue<CollectionBean> queu) {
        if (DBG) {
            log("queu.size: " + queu.size());
        }
        ArrayList<String> resultList = new ArrayList();
        String newline = "\n";
        for (CollectionBean bean : queu) {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("EXCEPTION TIME:" + bean.getTimeNow() + newline + "EXCEPTION SYSVER:" + this.SysVersion + newline + "EXCEPTION MODULE:com.vivo.network" + newline + "EXCEPTION VERSIONNAME:" + this.proVersion + newline + "EXCEPTION VERSIONCODE:" + this.Version + newline + "EXCEPTION TYPE:" + String.valueOf(bean.getCollectionType()) + newline + "EXCEPTION AH:" + getLocation() + "EXCEPTION ADDR:" + "-1" + newline + "EXCEPTION SIM_ID:" + String.valueOf(bean.getSim_id()) + newline + "EXCEPTION FAIL_CAUSE:" + bean.getOutServiceType() + newline + "EXCEPTION IN_OUT:" + "-1" + newline + "EXCEPTION LAC1:" + String.valueOf(bean.getLac()) + newline + "EXCEPTION LAC2:" + String.valueOf(bean.getLac2()) + newline + "EXCEPTION CID1:" + String.valueOf(bean.getCid()) + newline + "EXCEPTION CID2:" + String.valueOf(bean.getCid2()) + newline + "EXCEPTION PLMN:" + bean.getPlmn() + newline + "EXCEPTION CS1:" + bean.getCs() + newline + "EXCEPTION CS2:" + bean.getCs2() + newline + "EXCEPTION PS1:" + bean.getPs() + newline + "EXCEPTION PS2:" + bean.getPs2() + newline + "EXCEPTION ISIM:" + "-1" + newline + "EXCEPTION MBN INFO:" + "-1" + newline + "EXCEPTION VOLTE Config:" + "-1" + newline + "EXCEPTION VOLTE:" + "-1" + newline + "EXCEPTION DEST_NUM:" + bean.getSignalStrength() + newline + "EXCEPTION KEEPWORD1:" + bean.getReserved1() + newline + "EXCEPTION KEEPWORD2:" + bean.getReserved2() + newline + "EXCEPTION INFO:" + bean.getRegCode() + " " + getRejectCauseFromCode(bean.getRegCode()) + newline + newline);
            resultList.add(strBuf.toString());
            if (DBG) {
                log("tmpRe: " + strBuf.toString());
            }
        }
        queu.clear();
        return resultList;
    }

    public static String getSignalStrength(int phoneid) {
        Iterable queu = null;
        if (phoneid == 0) {
            queu = mQueuSignalStrength1;
        } else if (1 == phoneid) {
            queu = mQueuSignalStrength2;
        }
        if (queu == null) {
            return String.valueOf(Integer.MAX_VALUE);
        }
        String strength = "";
        for (SignalStrength s : queu) {
            strength = strength + s.getDbm() + ",";
        }
        return strength;
    }

    protected static void log(String s) {
        Rlog.d(LOG_TAG, "[DataCollectionUtils] " + s);
    }

    public static boolean isValidImeiMeid(String imei) {
        if (DBG) {
            log("imei&meid: " + imei);
            return true;
        }
        boolean z;
        if (imei == null || imei.length() <= 9 || ("865407010000009".equals(imei) ^ 1) == 0 || ("865407010000017".equals(imei) ^ 1) == 0 || ("A100003A5028D8".equals(imei) ^ 1) == 0) {
            z = false;
        } else {
            z = "123456789012345".equals(imei) ^ 1;
        }
        return z;
    }

    public static boolean isValidLoc(int lac, int cid) {
        boolean z = true;
        if (DBG) {
            log("lac: " + lac + " cid: " + cid);
        }
        if (DBG) {
            return true;
        }
        if (65534 == lac || 65535 == lac || lac == 0 || -1 == lac || cid == 0) {
            z = false;
        }
        return z;
    }

    public static boolean isValidLoc(int lac, int cid, int lac2, int cid2) {
        if (DBG) {
            log("lac: " + lac + " cid: " + cid + " lac2: " + lac2 + " cid2: " + cid2);
        }
        if (65534 == lac || 65535 == lac || lac == 0 || -1 == lac || cid == 0 || 65534 == lac2 || 65535 == lac2 || lac2 == 0 || -1 == lac2 || cid2 == 0) {
            return false;
        }
        return true;
    }

    public static boolean isInService(ServiceState oldSer, ServiceState newSer) {
        return oldSer.getVoiceRegState() == 0 && newSer.getVoiceRegState() == 0;
    }

    public static boolean isRatChangeCdma(ServiceState oldSer, ServiceState newSer) {
        if (TelephonyManager.getNetworkClass(oldSer.getDataNetworkType()) != TelephonyManager.getNetworkClass(newSer.getDataNetworkType()) && newSer.getDataRegState() == 0 && oldSer.getDataRegState() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isAirModeOn() {
        boolean z = true;
        if (DBG && (isDevMode ^ 1) != 0) {
            return false;
        }
        if (Global.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = false;
        }
        return z;
    }

    public boolean isValidSim(int subId) {
        boolean z = true;
        if (DBG) {
            return true;
        }
        if (this.mFtTel.getSimState(this.mFtTel.getSlotBySubId(subId)) != 5) {
            z = false;
        }
        return z;
    }

    public boolean isNotMms() {
        boolean z = false;
        if (!((TelephonyManager) mContext.getSystemService("phone")).isMultiSimEnabled()) {
            return isNotMms(0);
        }
        if (isNotMms(0)) {
            z = isNotMms(1);
        }
        return z;
    }

    private boolean isNotMms(int phoneId) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        boolean re = phone == null || phone.getDataConnectionState("mms") == DataState.DISCONNECTED;
        if (DBG) {
            log("is isNotMms phoneId:  " + phoneId + "  " + re);
        }
        return re;
    }

    public boolean isIdle() {
        boolean re = this.mFtTel.getCallState(0) == State.IDLE.ordinal() && this.mFtTel.getCallState(1) == State.IDLE.ordinal();
        if (DBG) {
            log("isIdle: " + re);
        }
        return re;
    }

    private String getSharedPreferences(String name) {
        return mContext.getSharedPreferences("vivo_data_collection", 32768).getString(name, String.valueOf(INVALID));
    }

    private void setSharedPreferences(String name, String value) {
        Editor editor = mContext.getSharedPreferences("vivo_data_collection", 32768).edit();
        editor.putString(name, value);
        editor.commit();
    }

    private boolean isAllowedToReport(int type, CollectionBean item) {
        boolean z = false;
        if (DBG) {
            if (SystemProperties.getInt(devProp, 0) == 1) {
                z = true;
            }
            isDevMode = z;
            log("isDevMode: " + isDevMode);
        }
        boolean re = false;
        long current = item.getCurrent();
        if (DBG) {
            log("isAllowedToReport type:  " + type + " CollectionStartTime:  " + CollectionStartTime + " current: " + current + " MAX_REPORT_OF_DAY: " + MAX_REPORT_OF_DAY + " COLLECTION_INTERVAL:  " + COLLECTION_INTERVAL);
        }
        if (current - CollectionStartTime > 86400000) {
            reset();
        }
        if (DBG && (isDevMode ^ 1) != 0) {
            return true;
        }
        switch (type) {
            case 0:
                if (DBG) {
                    log(" mOutOfServCount: " + mOutOfServCount + " mOutOfServLastTime: " + mOutOfServLastTime + " current-mOutOfServLastTime  " + (current - mOutOfServLastTime));
                }
                if ((INVALID == Long.valueOf(mOutOfServLastTime).longValue() || ((long) COLLECTION_INTERVAL) < current - mOutOfServLastTime) && mOutOfServCount < MAX_REPORT_OF_DAY) {
                    re = true;
                    mOutOfServLastTime = current;
                    mOutOfServCount += 3;
                    setSharedPreferences(SPRE_OUT_OF_SERV_COUNT, String.valueOf(mOutOfServCount));
                    setSharedPreferences(SPRE_OUT_OF_SERV_LAST, String.valueOf(mOutOfServLastTime));
                    break;
                }
            case 1:
                if (DBG) {
                    log("mOutOfServOftenCount: " + mOutOfServOftenCount + " mOutOfServOftenLastTime: " + mOutOfServOftenLastTime + " current-mOutOfServOftenLastTime  " + (current - mOutOfServOftenLastTime));
                }
                if ((INVALID == Long.valueOf(mOutOfServOftenLastTime).longValue() || ((long) COLLECTION_INTERVAL) < current - mOutOfServOftenLastTime) && mOutOfServOftenCount < MAX_REPORT_OF_DAY) {
                    re = true;
                    mOutOfServOftenLastTime = current;
                    mOutOfServOftenCount++;
                    setSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_COUNT, String.valueOf(mOutOfServOftenCount));
                    setSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_LAST, String.valueOf(mOutOfServOftenLastTime));
                    break;
                }
            case 2:
                if (DBG) {
                    log("mRatChangeCount: " + mRatChangeCount + " mRatChangeLastTime: " + mRatChangeLastTime + " current-mRatChangeLastTime  " + (current - mRatChangeLastTime));
                }
                if ((INVALID == Long.valueOf(mRatChangeLastTime).longValue() || ((long) COLLECTION_INTERVAL) < current - mRatChangeLastTime) && mRatChangeCount < MAX_REPORT_OF_DAY) {
                    re = true;
                    mRatChangeLastTime = current;
                    mRatChangeCount += 5;
                    setSharedPreferences(SPRE_RAT_CHANGE_COUNT, String.valueOf(mRatChangeCount));
                    setSharedPreferences(SPRE_RAT_CHANGE_LAST, String.valueOf(mRatChangeLastTime));
                    break;
                }
        }
        if (DBG) {
            log("is allow to  report : " + re);
        }
        return re;
    }

    private void reset() {
        if (DBG) {
            log("24 hours reset");
        }
        CollectionStartTime = System.currentTimeMillis();
        mOutOfServCount = 0;
        mOutOfServOftenCount = 0;
        mRatChangeCount = 0;
        setDefaultSpre();
    }

    private void setDefaultSpre() {
        setSharedPreferences(SPRE_COLLECTION_START, String.valueOf(CollectionStartTime));
        setSharedPreferences(SPRE_OUT_OF_SERV_COUNT, "0");
        setSharedPreferences(SPRE_OUT_OF_SERV_OFTEN_COUNT, "0");
        setSharedPreferences(SPRE_RAT_CHANGE_COUNT, "0");
    }

    public static int getVoiceRegistrationDeniedReason(int phoneid) {
        if (phoneid == 0 || phoneid == 1) {
            return ((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(VOICE_DENIED_REASON)).intValue();
        }
        return 0;
    }

    public static void setVoiceRegistrationDeniedReason(int phoneid, int VoiceRegistrationDeniedReason) {
        if (phoneid == 0 || phoneid == 1) {
            ((HashMap) mRegStateArrLst.get(phoneid)).put(VOICE_DENIED_REASON, Integer.valueOf(VoiceRegistrationDeniedReason));
        }
    }

    public static int getDataRegistrationDeniedReason(int phoneid) {
        if (phoneid == 0 || phoneid == 1) {
            return ((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(DATA_DENIED_REASON)).intValue();
        }
        return 0;
    }

    public static void setDataRegistrationDeniedReason(int phoneid, int DataRegistrationDeniedReason) {
        if (phoneid == 0 || phoneid == 1) {
            ((HashMap) mRegStateArrLst.get(phoneid)).put(DATA_DENIED_REASON, Integer.valueOf(DataRegistrationDeniedReason));
        }
    }

    public static void storeDeniedReason(int type, int phoneid, int regState, int denied) {
        int oldRegState;
        switch (type) {
            case 20:
                if (phoneid >= 0 && 1 >= phoneid) {
                    oldRegState = ((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(VOICE_REG_STATE)).intValue();
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(VOICE_REG_STATE, Integer.valueOf(regState));
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(VOICE_DENIED_REASON, Integer.valueOf(denied));
                    if (DBG) {
                        log("voice err  every cause  regState=" + regState + ",oldRegState : " + oldRegState + ",mVoiceRegState: " + ((HashMap) mRegStateArrLst.get(phoneid)).get(VOICE_REG_STATE) + ",VoiceRegistrationDeniedCode: " + ((HashMap) mRegStateArrLst.get(phoneid)).get(VOICE_DENIED_REASON) + ",VoiceRegistrationDeniedReason: " + getRejectCauseFromCode(((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(VOICE_DENIED_REASON)).intValue()));
                        break;
                    }
                }
                return;
                break;
            case 21:
                if (phoneid >= 0 && 1 >= phoneid) {
                    oldRegState = ((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(DATA_REG_STATE)).intValue();
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(DATA_REG_STATE, Integer.valueOf(regState));
                    ((HashMap) mRegStateArrLst.get(phoneid)).put(DATA_DENIED_REASON, Integer.valueOf(denied));
                    if (DBG) {
                        log("data err  every cause regState=" + regState + ",oldRegState : " + oldRegState + "mDataRegState: " + ((HashMap) mRegStateArrLst.get(phoneid)).get(DATA_REG_STATE) + "DataRegistrationDeniedCode: " + ((HashMap) mRegStateArrLst.get(phoneid)).get(DATA_DENIED_REASON) + "DataRegistrationDeniedReason: " + getRejectCauseFromCode(((Integer) ((HashMap) mRegStateArrLst.get(phoneid)).get(DATA_DENIED_REASON)).intValue()));
                        break;
                    }
                }
                return;
                break;
        }
    }

    public static String getRejectCauseFromCode(int rejcode) {
        String rej;
        switch (rejcode) {
            case 2:
                rej = "REJECT_CAUSE_IMSI_unknown_in_HSS";
                break;
            case 3:
                rej = "REJECT_CAUSE_Illegal_UE";
                break;
            case 4:
                rej = "REJECT_CAUSE_IMSI_unknown_in_VLR";
                break;
            case 5:
                rej = "REJECT_CAUSE_IMEI_not_accepted";
                break;
            case 6:
                rej = "REJECT_CAUSE_Illegal_ME";
                break;
            case 7:
                rej = "REJECT_CAUSE_EPS_services_not_allowed";
                break;
            case 8:
                rej = "REJECT_CAUSE_EPS_services_and_non_EPS_services_not_allowed";
                break;
            case 9:
                rej = "REJECT_CAUSE_UE_identity_cannot_be_derived_by_the_network";
                break;
            case 10:
                rej = "REJECT_CAUSE_Implicitly_detached";
                break;
            case 11:
                rej = "REJECT_CAUSE_PLMN_not_allowed";
                break;
            case 12:
                rej = "REJECT_CAUSE_Tracking_area_not_allowed";
                break;
            case 13:
                rej = "REJECT_CAUSE_Roaming_not_allowed_in_this_tracking_area";
                break;
            case 14:
                rej = "REJECT_CAUSE_EPS_services_not_allowed_in_this_PLMN";
                break;
            case 15:
                rej = "REJECT_CAUSE_No_suitable_cells_in_tracking_area";
                break;
            case 16:
                rej = "REJECT_CAUSE_MSC_temporarily_not_reachable";
                break;
            case 17:
                rej = "REJECT_CAUSE_Network_failure";
                break;
            case 18:
                rej = "REJECT_CAUSE_CS_domain_not_available";
                break;
            case 19:
                rej = "REJECT_CAUSE_ESM_failure";
                break;
            case 20:
                rej = "REJECT_CAUSE_MAC_failure";
                break;
            case 21:
                rej = "REJECT_CAUSE_Synch_failure";
                break;
            case 22:
                rej = "REJECT_CAUSE_Congestion";
                break;
            case 23:
                rej = "REJECT_CAUSE_UE_security_capabilities_mismatch";
                break;
            case 24:
                rej = "REJECT_CAUSE_Security_mode_rejected_unspecified";
                break;
            case 25:
                rej = "REJECT_CAUSE_Not_Authorized_for_this_CSG";
                break;
            case 26:
                rej = "REJECT_CAUSE_Non-EPS_authentication_unacceptable";
                break;
            case 32:
                rej = "REJECT_CAUSE_Service_option_not_supported";
                break;
            case 33:
                rej = "REJECT_CAUSE_Requested_service_option_not_subscribed";
                break;
            case 34:
                rej = "REJECT_CAUSE_Service_option_temporarily_out_of_order";
                break;
            case 35:
                rej = "REJECT_CAUSE_Requested_service_option_not_authorized";
                break;
            case 38:
                rej = "REJECT_CAUSE_Call_cannot_be_identified";
                break;
            case 39:
                rej = "REJECT_CAUSE_CS_service_temporarily_not_available";
                break;
            case 40:
                rej = "REJECT_CAUSE_No_EPS_bearer_context_activated";
                break;
            case 95:
                rej = "REJECT_CAUSE_Semantically_incorrect_message";
                break;
            case 96:
                rej = "REJECT_CAUSE_Invalid_mandatory_information";
                break;
            case 97:
                rej = "REJECT_CAUSE_Message_type_non-existent_or_not_implemented";
                break;
            case 98:
                rej = "REJECT_CAUSE_Message_type_not_compatible_with_protocol_state";
                break;
            case 99:
                rej = "REJECT_CAUSE_Information_element_non-existent_or_not_implemented";
                break;
            case 100:
                rej = "REJECT_CAUSE_Conditional_IE_error";
                break;
            case 101:
                rej = "REJECT_CAUSE_Message_not_compatible_with_protocol_state";
                break;
            case 111:
                rej = "REJECT_CAUSE_Protocol_error_unspecified";
                break;
            case 255:
                rej = "REJECT_CAUSE_Authentication_rejected";
                break;
            default:
                rej = "Unspecified_failure";
                break;
        }
        if (DBG) {
            log("voice rejcode: " + rejcode + " rejcause: " + rej);
        }
        return rej;
    }

    public static String getDataRejectCauseFromCode(int rejcode) {
        String rej;
        switch (rejcode) {
            case 2:
                rej = "REJECT_CAUSE_IMSI_Unknown";
                break;
            case 7:
                rej = "REJECT_CAUSE_GPRS_services_not_allowed";
                break;
            case 8:
                rej = "REJECT_CAUSE_GPRS services_and_non-GPRS_services_not_allowed";
                break;
            case 9:
                rej = "REJECT_CAUSE_MS identity cannot be derived_by_the_network";
                break;
            case 10:
                rej = "REJECT_CAUSE_Implicitly_detached";
                break;
            case 14:
                rej = "REJECT_CAUSE_GPRS_services_not_allowed_in_this_PLMN";
                break;
            case 16:
                rej = "REJECT_CAUSE_MSC_temporarily_not_reachable";
                break;
            case 40:
                rej = "REJECT_CAUSE_No_PDP_context_activated";
                break;
            default:
                rej = "Unspecified failure";
                break;
        }
        if (DBG) {
            log("data rejcode: " + rejcode + " rejcause: " + rej);
        }
        return rej;
    }

    public static void displayRegState(String[] state) {
        if (DBG) {
            log("displayRegState");
        }
        if (state != null && DBG) {
            for (int i = 0; i < state.length; i++) {
                log("regState[" + i + "]" + state[i]);
            }
        }
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 10:
            case 12:
            case 13:
            case 14:
                return 1;
            case 1:
                return 0;
            case 5:
                return 0;
            default:
                loge("regCodeToServiceState: unexpected service state " + code);
                return 1;
        }
    }

    private void debugInfo(String name) {
        if (this.OUT_OF_SERVICE_ONTEN_SIM1_TXT.equals(name)) {
            log("OUT_OF_SERVICE_ONTEN_SIM1");
        } else if (this.OUT_OF_SERVICE_ONTEN_SIM2_TXT.equals(name)) {
            log("OUT_OF_SERVICE_ONTEN_SIM2");
        } else if (this.OUT_OF_SERVICE_SIM1_TXT.equals(name)) {
            log("OUT_OF_SERVICE_SIM1");
        } else if (this.OUT_OF_SERVICE_SIM2_TXT.equals(name)) {
            log("OUT_OF_SERVICE_SIM2");
        } else if (this.RAT_CHANGE_TXT.equals(name)) {
            log("RAT_CHANGE");
        } else {
            log("Unknown Report");
        }
    }

    public static String getDuration(long start, long end) {
        String res = "-1";
        long duration = end - start;
        if (duration < 1000) {
            duration = 1000;
        } else if (duration > OUT_OF_SERVICE_DURATION_MAX) {
            duration = OUT_OF_SERVICE_DURATION_MAX;
        }
        try {
            return String.valueOf(duration / 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }
}
