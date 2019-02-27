package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import com.android.internal.telephony.-$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg.AnonymousClass1;
import com.android.internal.telephony.ISub.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SubscriptionController extends Stub {
    protected static final boolean DBG = true;
    protected static final boolean DBG_CACHE = false;
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    private static final Comparator<SubscriptionInfo> SUBSCRIPTION_INFO_COMPARATOR = -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg.$INST$0;
    protected static final boolean VDBG = false;
    protected static int mDefaultFallbackSubId = -1;
    protected static int mDefaultPhoneId = Integer.MAX_VALUE;
    protected static SubscriptionController sInstance = null;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIndexToSubId = new ConcurrentHashMap();
    private int[] colorArr;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    private AtomicReference<List<SubscriptionInfo>> mCacheActiveSubInfoList = new AtomicReference();
    protected Context mContext;
    private ScLocalLog mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
    protected final Object mLock = new Object();
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private Map<Integer, Integer> mSlotIdxToSubIdTemporary = new ConcurrentHashMap();
    protected TelephonyManager mTelephonyManager;

    static class ScLocalLog {
        private LinkedList<String> mLog = new LinkedList();
        private int mMaxLines;
        private Time mNow;

        public ScLocalLog(int maxLines) {
            this.mMaxLines = maxLines;
            this.mNow = new Time();
        }

        public synchronized void log(String msg) {
            if (this.mMaxLines > 0) {
                int pid = Process.myPid();
                int tid = Process.myTid();
                this.mNow.setToNow();
                this.mLog.add(this.mNow.format("%m-%d %H:%M:%S") + " pid=" + pid + " tid=" + tid + " " + msg);
                while (this.mLog.size() > this.mMaxLines) {
                    this.mLog.remove();
                }
            }
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = this.mLog.listIterator(0);
            int i = 0;
            while (true) {
                int i2 = i;
                if (itr.hasNext()) {
                    i = i2 + 1;
                    pw.println(Integer.toString(i2) + ": " + ((String) itr.next()));
                    if (i % 10 == 0) {
                        pw.flush();
                    }
                }
            }
        }
    }

    /* renamed from: lambda$-com_android_internal_telephony_SubscriptionController_5370 */
    static /* synthetic */ int m1x2e1a88f2(SubscriptionInfo arg0, SubscriptionInfo arg1) {
        int flag = arg0.getSimSlotIndex() - arg1.getSimSlotIndex();
        if (flag == 0) {
            return arg0.getSubscriptionId() - arg1.getSubscriptionId();
        }
        return flag;
    }

    public static SubscriptionController init(Phone phone) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController init(Context c, CommandsInterface[] ci) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    protected SubscriptionController(Context c) {
        init(c);
    }

    protected void init(Context c) {
        this.mContext = c;
        this.mCM = CallManager.getInstance();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        logdl("[SubscriptionController] init by Context");
    }

    @VivoHook(hookType = VivoHookType.CHANGE_ACCESS)
    public boolean isSubInfoReady() {
        if (sSlotIndexToSubId.size() <= 0 || this.mCacheActiveSubInfoList.get() == null) {
            return false;
        }
        if (sSlotIndexToSubId.size() == ((List) this.mCacheActiveSubInfoList.get()).size()) {
            return true;
        }
        return false;
    }

    private SubscriptionController(Phone phone) {
        this.mContext = phone.getContext();
        this.mCM = CallManager.getInstance();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        logdl("[SubscriptionController] init by Phone");
    }

    private boolean canReadPhoneState(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    protected void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged() {
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE"));
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
    }

    public void notifySubscriptionInfoChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged();
    }

    private SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        UiccAccessRule[] accessRules;
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number = cursor.getString(cursor.getColumnIndexOrThrow(IccProvider.STR_NUMBER));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302735);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow("mcc"));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow("mnc"));
        String countryIso = getSubscriptionCountryIso(id);
        boolean isEmbedded = cursor.getInt(cursor.getColumnIndexOrThrow("is_embedded")) == 1;
        if (isEmbedded) {
            accessRules = UiccAccessRule.decodeRules(cursor.getBlob(cursor.getColumnIndexOrThrow("access_rules")));
        } else {
            accessRules = null;
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!(TextUtils.isEmpty(line1Number) || (line1Number.equals(number) ^ 1) == 0)) {
            number = line1Number;
        }
        return new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules);
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return "";
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0051  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        Exception e;
        Throwable th;
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[]{queryKey.toString()};
        }
        ArrayList<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, null);
        if (cursor != null) {
            while (true) {
                ArrayList<SubscriptionInfo> subList2 = subList;
                try {
                    if (!cursor.moveToNext()) {
                        subList = subList2;
                        break;
                    }
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList2 == null) {
                            subList = new ArrayList();
                        } else {
                            subList = subList2;
                        }
                        try {
                            subList.add(subInfo);
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } else {
                        subList = subList2;
                    }
                } catch (Exception e3) {
                    e = e3;
                    subList = subList2;
                    try {
                        e.printStackTrace();
                        if (cursor != null) {
                            cursor.close();
                        }
                        return subList;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    subList = subList2;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } else {
            logd("Query fail");
            if (cursor != null) {
            }
        }
        return subList;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17236060);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != ((SubscriptionInfo) availableSubInfos.get(j)).getIconTint()) {
                    j++;
                }
                if (j == availableSubInfos.size()) {
                    return this.colorArr[i];
                }
                i++;
            }
            colorIdx = availableSubInfos.size() % this.colorArr.length;
        }
        return this.colorArr[colorIdx];
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfo")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSubscriptionId() == subId) {
                        logd("[getActiveSubscriptionInfo]+ subId=" + subId + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubInfoForSubscriber]- subId=" + subId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForIccId") || iccId == null) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (iccId.equals(si.getIccId())) {
                        logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSimSlotIndex() == slotIndex) {
                        logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=" + si);
                        return si;
                    }
                }
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=null");
            } else {
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ subList=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        logd("[getAllSubInfoList]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo(null, null);
            if (subList != null) {
                logd("[getAllSubInfoList]- " + subList.size() + " infos return");
            } else {
                logd("[getAllSubInfoList]- no info return");
            }
            Binder.restoreCallingIdentity(identity);
            return subList;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                List<SubscriptionInfo> tmpCachedSubList = (List) this.mCacheActiveSubInfoList.get();
                if (tmpCachedSubList != null) {
                    List<SubscriptionInfo> activeSub = new ArrayList();
                    for (SubscriptionInfo cache : tmpCachedSubList) {
                        if (sSlotIndexToSubId.containsKey(Integer.valueOf(cache.getSimSlotIndex()))) {
                            activeSub.add(cache);
                        }
                    }
                    if (activeSub.isEmpty()) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    }
                    Binder.restoreCallingIdentity(identity);
                    return activeSub;
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            logdl("[getActiveSubInfoList] Sub Controller not ready");
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected void refreshCachedActiveSubscriptionInfoList() {
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo("sim_id>=0", null);
            if (subList != null) {
                subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
            }
            this.mCacheActiveSubInfoList.set(subList);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCount(String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (records == null) {
                return 0;
            }
            int size = records.size();
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int getAllSubInfoCount(String callingPackage) {
        logd("[getAllSubInfoCount]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                logd("[getAllSubInfoCount]- " + count + " SUB(s) in DB");
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getAllSubInfoCount]- no SUB in DB");
            Binder.restoreCallingIdentity(identity);
            return 0;
        } catch (Exception e) {
            try {
                e.printStackTrace();
                return 0;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList(String callingPackage) {
        if (canReadPhoneState(callingPackage, "getAvailableSubscriptionInfoList")) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (((EuiccManager) this.mContext.getSystemService("euicc_service")).isEnabled()) {
                    List<SubscriptionInfo> subList = getSubInfo("sim_id>=0 OR is_embedded=1", null);
                    if (subList != null) {
                        subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                    } else {
                        logdl("[getAvailableSubInfoList]- no info return");
                    }
                    Binder.restoreCallingIdentity(identity);
                    return subList;
                }
                logdl("[getAvailableSubInfoList] Embedded subscriptions are disabled");
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("Need READ_PHONE_STATE to call  getAvailableSubscriptionInfoList");
        }
    }

    public List<SubscriptionInfo> getAccessibleSubscriptionInfoList(String callingPackage) {
        if (((EuiccManager) this.mContext.getSystemService("euicc_service")).isEnabled()) {
            this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
            long identity = Binder.clearCallingIdentity();
            try {
                List<SubscriptionInfo> subList = getSubInfo("is_embedded=1", null);
                if (subList != null) {
                    return (List) subList.stream().filter(new AnonymousClass1(this, callingPackage)).sorted(SUBSCRIPTION_INFO_COMPARATOR).collect(Collectors.toList());
                }
                logdl("[getAccessibleSubInfoList] No info returned");
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            logdl("[getAccessibleSubInfoList] Embedded subscriptions are disabled");
            return null;
        }
    }

    /* renamed from: lambda$-com_android_internal_telephony_SubscriptionController_32836 */
    /* synthetic */ boolean m2x9519f641(String callingPackage, SubscriptionInfo subscriptionInfo) {
        return subscriptionInfo.canManageSubscription(this.mContext, callingPackage);
    }

    public List<SubscriptionInfo> getSubscriptionInfoListForEmbeddedSubscriptionUpdate(String[] embeddedIccids, boolean isEuiccRemovable) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(").append("is_embedded").append("=1");
        if (isEuiccRemovable) {
            whereClause.append(" AND ").append("is_removable").append("=1");
        }
        whereClause.append(") OR ").append("icc_id").append(" IN (");
        for (int i = 0; i < embeddedIccids.length; i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("\"").append(embeddedIccids[i]).append("\"");
        }
        whereClause.append(")");
        List<SubscriptionInfo> list = getSubInfo(whereClause.toString(), null);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void requestEmbeddedSubscriptionInfoListRefresh() {
        this.mContext.enforceCallingOrSelfPermission("com.android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", "requestEmbeddedSubscriptionInfoListRefresh");
        long token = Binder.clearCallingIdentity();
        try {
            PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(callback);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int, dom blocks: [B:2:0x0038, B:10:0x0076, B:19:0x00bb]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1249)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01c4 A:{SYNTHETIC, EDGE_INSN: B:90:0x01c4->B:36:0x01c4 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00a1 A:{SYNTHETIC, Splitter: B:15:0x00a1} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00bb A:{SYNTHETIC, Splitter: B:19:0x00bb} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x01c6 A:{SYNTHETIC, Splitter: B:37:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0293  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01d7 A:{Catch:{ all -> 0x027b, all -> 0x0255, Exception -> 0x025c }} */
    @android.annotation.VivoHook(hookType = android.annotation.VivoHook.VivoHookType.CHANGE_CODE)
    public int addSubInfoRecord(java.lang.String r24, int r25) {
        /*
        r23 = this;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "[addSubInfoRecord]+ iccId:";
        r3 = r3.append(r4);
        r4 = android.telephony.SubscriptionInfo.givePrintableIccid(r24);
        r3 = r3.append(r4);
        r4 = " slotIndex:";
        r3 = r3.append(r4);
        r0 = r25;
        r3 = r3.append(r0);
        r3 = r3.toString();
        r0 = r23;
        r0.logdl(r3);
        r3 = "addSubInfoRecord";
        r0 = r23;
        r0.enforceModifyPhoneState(r3);
        r12 = android.os.Binder.clearCallingIdentity();
        if (r24 != 0) goto L_0x0045;
    L_0x0038:
        r3 = "[addSubInfoRecord]- null iccId";	 Catch:{ Exception -> 0x025c }
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r0.logdl(r3);	 Catch:{ Exception -> 0x025c }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x0045:
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r3 = r0.mContext;	 Catch:{ Exception -> 0x025c }
        r2 = r3.getContentResolver();	 Catch:{ Exception -> 0x025c }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ Exception -> 0x025c }
        r4 = 3;	 Catch:{ Exception -> 0x025c }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x025c }
        r5 = "_id";	 Catch:{ Exception -> 0x025c }
        r6 = 0;	 Catch:{ Exception -> 0x025c }
        r4[r6] = r5;	 Catch:{ Exception -> 0x025c }
        r5 = "sim_id";	 Catch:{ Exception -> 0x025c }
        r6 = 1;	 Catch:{ Exception -> 0x025c }
        r4[r6] = r5;	 Catch:{ Exception -> 0x025c }
        r5 = "name_source";	 Catch:{ Exception -> 0x025c }
        r6 = 2;	 Catch:{ Exception -> 0x025c }
        r4[r6] = r5;	 Catch:{ Exception -> 0x025c }
        r5 = "icc_id=?";	 Catch:{ Exception -> 0x025c }
        r6 = 1;	 Catch:{ Exception -> 0x025c }
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x025c }
        r7 = 0;	 Catch:{ Exception -> 0x025c }
        r6[r7] = r24;	 Catch:{ Exception -> 0x025c }
        r7 = 0;	 Catch:{ Exception -> 0x025c }
        r9 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x025c }
        r17 = 0;
        if (r9 == 0) goto L_0x007e;
    L_0x0076:
        r3 = r9.moveToFirst();	 Catch:{ all -> 0x0255 }
        r3 = r3 ^ 1;	 Catch:{ all -> 0x0255 }
        if (r3 == 0) goto L_0x01f7;	 Catch:{ all -> 0x0255 }
    L_0x007e:
        r17 = 1;	 Catch:{ all -> 0x0255 }
        r21 = r23.insertEmptySubInfoRecord(r24, r25);	 Catch:{ all -> 0x0255 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0255 }
        r3.<init>();	 Catch:{ all -> 0x0255 }
        r4 = "[addSubInfoRecord] New record created: ";	 Catch:{ all -> 0x0255 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0255 }
        r0 = r21;	 Catch:{ all -> 0x0255 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0255 }
        r3 = r3.toString();	 Catch:{ all -> 0x0255 }
        r0 = r23;	 Catch:{ all -> 0x0255 }
        r0.logdl(r3);	 Catch:{ all -> 0x0255 }
    L_0x009f:
        if (r9 == 0) goto L_0x00a4;
    L_0x00a1:
        r9.close();	 Catch:{ Exception -> 0x025c }
    L_0x00a4:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ Exception -> 0x025c }
        r5 = "sim_id=?";	 Catch:{ Exception -> 0x025c }
        r4 = 1;	 Catch:{ Exception -> 0x025c }
        r6 = new java.lang.String[r4];	 Catch:{ Exception -> 0x025c }
        r4 = java.lang.String.valueOf(r25);	 Catch:{ Exception -> 0x025c }
        r7 = 0;	 Catch:{ Exception -> 0x025c }
        r6[r7] = r4;	 Catch:{ Exception -> 0x025c }
        r4 = 0;	 Catch:{ Exception -> 0x025c }
        r7 = 0;	 Catch:{ Exception -> 0x025c }
        r9 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x025c }
        if (r9 == 0) goto L_0x01c4;
    L_0x00bb:
        r3 = r9.moveToFirst();	 Catch:{ all -> 0x027b }
        if (r3 == 0) goto L_0x01c4;	 Catch:{ all -> 0x027b }
    L_0x00c1:
        r3 = "_id";	 Catch:{ all -> 0x027b }
        r3 = r9.getColumnIndexOrThrow(r3);	 Catch:{ all -> 0x027b }
        r19 = r9.getInt(r3);	 Catch:{ all -> 0x027b }
        r3 = sSlotIndexToSubId;	 Catch:{ all -> 0x027b }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x027b }
        r8 = r3.get(r4);	 Catch:{ all -> 0x027b }
        r8 = (java.lang.Integer) r8;	 Catch:{ all -> 0x027b }
        if (r8 == 0) goto L_0x00e2;	 Catch:{ all -> 0x027b }
    L_0x00da:
        r3 = r8.intValue();	 Catch:{ all -> 0x027b }
        r0 = r19;	 Catch:{ all -> 0x027b }
        if (r3 == r0) goto L_0x0265;	 Catch:{ all -> 0x027b }
    L_0x00e2:
        r3 = sSlotIndexToSubId;	 Catch:{ all -> 0x027b }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x027b }
        r5 = java.lang.Integer.valueOf(r19);	 Catch:{ all -> 0x027b }
        r3.put(r4, r5);	 Catch:{ all -> 0x027b }
        r20 = r23.getActiveSubInfoCountMax();	 Catch:{ all -> 0x027b }
        r10 = r23.getDefaultSubId();	 Catch:{ all -> 0x027b }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027b }
        r3.<init>();	 Catch:{ all -> 0x027b }
        r4 = "[addSubInfoRecord] sSlotIndexToSubId.size=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r4 = sSlotIndexToSubId;	 Catch:{ all -> 0x027b }
        r4 = r4.size();	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r4 = " slotIndex=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r25;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r4 = " subId=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r19;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r4 = " defaultSubId=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r3 = r3.append(r10);	 Catch:{ all -> 0x027b }
        r4 = " simCount=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r20;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r3 = r3.toString();	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r0.logdl(r3);	 Catch:{ all -> 0x027b }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r10);	 Catch:{ all -> 0x027b }
        if (r3 == 0) goto L_0x0153;	 Catch:{ all -> 0x027b }
    L_0x014e:
        r3 = 1;	 Catch:{ all -> 0x027b }
        r0 = r20;	 Catch:{ all -> 0x027b }
        if (r0 != r3) goto L_0x0287;	 Catch:{ all -> 0x027b }
    L_0x0153:
        r0 = r23;	 Catch:{ all -> 0x027b }
        r1 = r19;	 Catch:{ all -> 0x027b }
        r0.setDefaultFallbackSubId(r1);	 Catch:{ all -> 0x027b }
    L_0x015a:
        r3 = 1;	 Catch:{ all -> 0x027b }
        r0 = r20;	 Catch:{ all -> 0x027b }
        if (r0 != r3) goto L_0x018f;	 Catch:{ all -> 0x027b }
    L_0x015f:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027b }
        r3.<init>();	 Catch:{ all -> 0x027b }
        r4 = "[addSubInfoRecord] one sim set defaults to subId=";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r19;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r3 = r3.toString();	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r0.logdl(r3);	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r1 = r19;	 Catch:{ all -> 0x027b }
        r0.setDefaultDataSubId(r1);	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r1 = r19;	 Catch:{ all -> 0x027b }
        r0.setDefaultSmsSubId(r1);	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r1 = r19;	 Catch:{ all -> 0x027b }
        r0.setDefaultVoiceSubId(r1);	 Catch:{ all -> 0x027b }
    L_0x018f:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027b }
        r3.<init>();	 Catch:{ all -> 0x027b }
        r4 = "[addSubInfoRecord] hashmap(";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r25;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r4 = ",";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r0 = r19;	 Catch:{ all -> 0x027b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x027b }
        r4 = ")";	 Catch:{ all -> 0x027b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x027b }
        r3 = r3.toString();	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r0.logdl(r3);	 Catch:{ all -> 0x027b }
        r3 = r9.moveToNext();	 Catch:{ all -> 0x027b }
        if (r3 != 0) goto L_0x00c1;
    L_0x01c4:
        if (r9 == 0) goto L_0x01c9;
    L_0x01c6:
        r9.close();	 Catch:{ Exception -> 0x025c }
    L_0x01c9:
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r1 = r25;	 Catch:{ Exception -> 0x025c }
        r19 = r0.getSubIdUsingPhoneId(r1);	 Catch:{ Exception -> 0x025c }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r19);	 Catch:{ Exception -> 0x025c }
        if (r3 != 0) goto L_0x0293;	 Catch:{ Exception -> 0x025c }
    L_0x01d7:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x025c }
        r3.<init>();	 Catch:{ Exception -> 0x025c }
        r4 = "[addSubInfoRecord]- getSubId failed invalid subId = ";	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r0 = r19;	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r0);	 Catch:{ Exception -> 0x025c }
        r3 = r3.toString();	 Catch:{ Exception -> 0x025c }
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r0.logdl(r3);	 Catch:{ Exception -> 0x025c }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x01f7:
        r3 = 0;
        r19 = r9.getInt(r3);	 Catch:{ all -> 0x0255 }
        r3 = 1;	 Catch:{ all -> 0x0255 }
        r16 = r9.getInt(r3);	 Catch:{ all -> 0x0255 }
        r3 = 2;	 Catch:{ all -> 0x0255 }
        r14 = r9.getInt(r3);	 Catch:{ all -> 0x0255 }
        r22 = new android.content.ContentValues;	 Catch:{ all -> 0x0255 }
        r22.<init>();	 Catch:{ all -> 0x0255 }
        r0 = r25;	 Catch:{ all -> 0x0255 }
        r1 = r16;	 Catch:{ all -> 0x0255 }
        if (r0 == r1) goto L_0x021d;	 Catch:{ all -> 0x0255 }
    L_0x0211:
        r3 = "sim_id";	 Catch:{ all -> 0x0255 }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x0255 }
        r0 = r22;	 Catch:{ all -> 0x0255 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0255 }
    L_0x021d:
        r3 = 2;	 Catch:{ all -> 0x0255 }
        if (r14 == r3) goto L_0x0222;	 Catch:{ all -> 0x0255 }
    L_0x0220:
        r17 = 1;	 Catch:{ all -> 0x0255 }
    L_0x0222:
        r3 = r22.size();	 Catch:{ all -> 0x0255 }
        if (r3 <= 0) goto L_0x024b;	 Catch:{ all -> 0x0255 }
    L_0x0228:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0255 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0255 }
        r4.<init>();	 Catch:{ all -> 0x0255 }
        r5 = "_id=";	 Catch:{ all -> 0x0255 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0255 }
        r0 = r19;	 Catch:{ all -> 0x0255 }
        r6 = (long) r0;	 Catch:{ all -> 0x0255 }
        r5 = java.lang.Long.toString(r6);	 Catch:{ all -> 0x0255 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0255 }
        r4 = r4.toString();	 Catch:{ all -> 0x0255 }
        r5 = 0;	 Catch:{ all -> 0x0255 }
        r0 = r22;	 Catch:{ all -> 0x0255 }
        r2.update(r3, r0, r4, r5);	 Catch:{ all -> 0x0255 }
    L_0x024b:
        r3 = "[addSubInfoRecord] Record already exists";	 Catch:{ all -> 0x0255 }
        r0 = r23;	 Catch:{ all -> 0x0255 }
        r0.logdl(r3);	 Catch:{ all -> 0x0255 }
        goto L_0x009f;
    L_0x0255:
        r3 = move-exception;
        if (r9 == 0) goto L_0x025b;
    L_0x0258:
        r9.close();	 Catch:{ Exception -> 0x025c }
    L_0x025b:
        throw r3;	 Catch:{ Exception -> 0x025c }
    L_0x025c:
        r11 = move-exception;
        r11.printStackTrace();	 Catch:{ all -> 0x0282 }
        android.os.Binder.restoreCallingIdentity(r12);
    L_0x0263:
        r3 = 0;
        return r3;
    L_0x0265:
        r3 = r8.intValue();	 Catch:{ all -> 0x027b }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r3);	 Catch:{ all -> 0x027b }
        r3 = r3 ^ 1;	 Catch:{ all -> 0x027b }
        if (r3 != 0) goto L_0x00e2;	 Catch:{ all -> 0x027b }
    L_0x0271:
        r3 = "[addSubInfoRecord] currentSubId != null && currentSubId is valid, IGNORE";	 Catch:{ all -> 0x027b }
        r0 = r23;	 Catch:{ all -> 0x027b }
        r0.logdl(r3);	 Catch:{ all -> 0x027b }
        goto L_0x018f;
    L_0x027b:
        r3 = move-exception;
        if (r9 == 0) goto L_0x0281;
    L_0x027e:
        r9.close();	 Catch:{ Exception -> 0x025c }
    L_0x0281:
        throw r3;	 Catch:{ Exception -> 0x025c }
    L_0x0282:
        r3 = move-exception;
        android.os.Binder.restoreCallingIdentity(r12);
        throw r3;
    L_0x0287:
        r0 = r23;	 Catch:{ all -> 0x027b }
        r3 = r0.isActiveSubId(r10);	 Catch:{ all -> 0x027b }
        r3 = r3 ^ 1;
        if (r3 == 0) goto L_0x015a;
    L_0x0291:
        goto L_0x0153;
    L_0x0293:
        if (r17 == 0) goto L_0x030f;
    L_0x0295:
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r3 = r0.mTelephonyManager;	 Catch:{ Exception -> 0x025c }
        r0 = r19;	 Catch:{ Exception -> 0x025c }
        r18 = r3.getSimOperatorName(r0);	 Catch:{ Exception -> 0x025c }
        r15 = r23.checkOperatorByIccidForSimName(r24);	 Catch:{ Exception -> 0x025c }
        r3 = "UNKNOWN";	 Catch:{ Exception -> 0x025c }
        r3 = r3.equals(r15);	 Catch:{ Exception -> 0x025c }
        if (r3 == 0) goto L_0x02c6;	 Catch:{ Exception -> 0x025c }
    L_0x02ac:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x025c }
        r3.<init>();	 Catch:{ Exception -> 0x025c }
        r4 = "SIM";	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r4 = r25 + 1;	 Catch:{ Exception -> 0x025c }
        r4 = java.lang.Integer.toString(r4);	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r15 = r3.toString();	 Catch:{ Exception -> 0x025c }
    L_0x02c6:
        r22 = new android.content.ContentValues;	 Catch:{ Exception -> 0x025c }
        r22.<init>();	 Catch:{ Exception -> 0x025c }
        r3 = "display_name";	 Catch:{ Exception -> 0x025c }
        r0 = r22;	 Catch:{ Exception -> 0x025c }
        r0.put(r3, r15);	 Catch:{ Exception -> 0x025c }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ Exception -> 0x025c }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x025c }
        r4.<init>();	 Catch:{ Exception -> 0x025c }
        r5 = "_id=";	 Catch:{ Exception -> 0x025c }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x025c }
        r0 = r19;	 Catch:{ Exception -> 0x025c }
        r6 = (long) r0;	 Catch:{ Exception -> 0x025c }
        r5 = java.lang.Long.toString(r6);	 Catch:{ Exception -> 0x025c }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x025c }
        r4 = r4.toString();	 Catch:{ Exception -> 0x025c }
        r5 = 0;	 Catch:{ Exception -> 0x025c }
        r0 = r22;	 Catch:{ Exception -> 0x025c }
        r2.update(r3, r0, r4, r5);	 Catch:{ Exception -> 0x025c }
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x025c }
        r3.<init>();	 Catch:{ Exception -> 0x025c }
        r4 = "[addSubInfoRecord] sim name = ";	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r15);	 Catch:{ Exception -> 0x025c }
        r3 = r3.toString();	 Catch:{ Exception -> 0x025c }
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r0.logdl(r3);	 Catch:{ Exception -> 0x025c }
    L_0x030f:
        r23.refreshCachedActiveSubscriptionInfoList();	 Catch:{ Exception -> 0x025c }
        r3 = sPhones;	 Catch:{ Exception -> 0x025c }
        r3 = r3[r25];	 Catch:{ Exception -> 0x025c }
        r3.updateDataConnectionTracker();	 Catch:{ Exception -> 0x025c }
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x025c }
        r3.<init>();	 Catch:{ Exception -> 0x025c }
        r4 = "[addSubInfoRecord]- info size=";	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r4 = sSlotIndexToSubId;	 Catch:{ Exception -> 0x025c }
        r4 = r4.size();	 Catch:{ Exception -> 0x025c }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x025c }
        r3 = r3.toString();	 Catch:{ Exception -> 0x025c }
        r0 = r23;	 Catch:{ Exception -> 0x025c }
        r0.logdl(r3);	 Catch:{ Exception -> 0x025c }
        android.os.Binder.restoreCallingIdentity(r12);
        goto L_0x0263;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int");
    }

    public Uri insertEmptySubInfoRecord(String iccId, int slotIndex) {
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        value.put("icc_id", iccId);
        value.put("color", Integer.valueOf(getUnusedColor(this.mContext.getOpPackageName())));
        value.put("sim_id", Integer.valueOf(slotIndex));
        value.put("carrier_name", "");
        return resolver.insert(SubscriptionManager.CONTENT_URI, value);
    }

    public boolean setPlmnSpn(int slotIndex, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int subId = getSubIdUsingPhoneId(slotIndex);
            if (this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) == null || (SubscriptionManager.isValidSubscriptionId(subId) ^ 1) != 0) {
                logd("[setPlmnSpn] No valid subscription to store info");
                notifySubscriptionInfoChanged();
                return false;
            }
            String carrierText = "";
            if (showPlmn) {
                carrierText = plmn;
                if (showSpn && !Objects.equals(spn, plmn)) {
                    carrierText = plmn + this.mContext.getString(17040138).toString() + spn;
                }
            } else if (showSpn) {
                carrierText = spn;
            }
            setCarrierText(carrierText, subId);
            return true;
        }
    }

    private int setCarrierText(String text, int subId) {
        logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        enforceModifyPhoneState("setCarrierText");
        long identity = Binder.clearCallingIdentity();
        try {
            ContentValues value = new ContentValues(1);
            value.put("carrier_name", text);
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setIconTint(int tint, int subId) {
        logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        enforceModifyPhoneState("setIconTint");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put("color", Integer.valueOf(tint));
            logd("[setIconTint]- tint:" + tint + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayNameUsingSrc(displayName, subId, -1);
    }

    public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) {
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
            String nameToSet;
            validateSubId(subId);
            if (displayName == null) {
                nameToSet = this.mContext.getString(17039374);
            } else {
                nameToSet = displayName;
            }
            ContentValues value = new ContentValues(1);
            value.put("display_name", nameToSet);
            if (nameSource >= 0) {
                logd("Set nameSource=" + nameSource);
                value.put("name_source", Long.valueOf(nameSource));
            }
            logd("[setDisplayName]- mDisplayName:" + nameToSet + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayNumber(String number, int subId) {
        logd("[setDisplayNumber]+ subId:" + subId);
        enforceModifyPhoneState("setDisplayNumber");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            int phoneId = getPhoneId(subId);
            if (number != null && phoneId >= 0) {
                if (phoneId < this.mTelephonyManager.getPhoneCount()) {
                    ContentValues value = new ContentValues(1);
                    value.put(IccProvider.STR_NUMBER, number);
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
                    refreshCachedActiveSubscriptionInfoList();
                    logd("[setDisplayNumber]- update result :" + result);
                    notifySubscriptionInfoChanged();
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
            }
            logd("[setDispalyNumber]- fail");
            return -1;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDataRoaming(int roaming, int subId) {
        logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        enforceModifyPhoneState("setDataRoaming");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            if (roaming < 0) {
                logd("[setDataRoaming]- fail");
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put("data_roaming", Integer.valueOf(roaming));
            logd("[setDataRoaming]- roaming:" + roaming + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setMccMnc(String mccMnc, int subId) {
        int mcc = 0;
        int mnc = 0;
        try {
            mcc = Integer.parseInt(mccMnc.substring(0, 3));
            mnc = Integer.parseInt(mccMnc.substring(3));
        } catch (NumberFormatException e) {
            loge("[setMccMnc] - couldn't parse mcc/mnc: " + mccMnc);
        }
        logd("[setMccMnc]+ mcc/mnc:" + mcc + "/" + mnc + " subId:" + subId);
        ContentValues value = new ContentValues(2);
        value.put("mcc", Integer.valueOf(mcc));
        value.put("mnc", Integer.valueOf(mnc));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
        return result;
    }

    public int getSlotIndex(int subId) {
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotIndex]- subId invalid");
            return -1;
        } else if (sSlotIndexToSubId.size() == 0) {
            logd("[getSlotIndex]- size == 0, return SIM_NOT_INSERTED instead");
            return -1;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    return sim;
                }
            }
            logd("[getSlotIndex]- return fail");
            return -1;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public int[] getSubId(int slotIndex) {
        if (slotIndex == Integer.MAX_VALUE) {
            slotIndex = getSlotIndex(getDefaultSubId());
        }
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            int slot;
            int sub;
            int i;
            if (sSlotIndexToSubId.size() > 0) {
                ArrayList<Integer> subIds = new ArrayList();
                for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                    slot = ((Integer) entry.getKey()).intValue();
                    sub = ((Integer) entry.getValue()).intValue();
                    if (slotIndex == slot) {
                        subIds.add(Integer.valueOf(sub));
                    }
                }
                int numSubIds = subIds.size();
                if (numSubIds > 0) {
                    int[] subIdArr = new int[numSubIds];
                    for (i = 0; i < numSubIds; i++) {
                        subIdArr[i] = ((Integer) subIds.get(i)).intValue();
                    }
                    return subIdArr;
                }
            }
            if (this.mSlotIdxToSubIdTemporary.size() > 0) {
                ArrayList<Integer> subIdList = new ArrayList();
                for (Entry<Integer, Integer> entry2 : this.mSlotIdxToSubIdTemporary.entrySet()) {
                    slot = ((Integer) entry2.getKey()).intValue();
                    sub = ((Integer) entry2.getValue()).intValue();
                    if (slotIndex == slot) {
                        logd("[getSubIdFromTmp] - slotIndex=" + slotIndex + " sub = " + sub);
                        subIdList.add(Integer.valueOf(sub));
                    }
                    int num = subIdList.size();
                    if (num > 0) {
                        int[] subIdArrs = new int[num];
                        for (i = 0; i < num; i++) {
                            subIdArrs[i] = ((Integer) subIdList.get(i)).intValue();
                        }
                        return subIdArrs;
                    }
                }
            }
            return getDummySubIds(slotIndex);
        }
        logd("[getSubId]- invalid slotIndex=" + slotIndex);
        return null;
    }

    public int getPhoneId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return -1;
        }
        int sim;
        if (sSlotIndexToSubId.size() > 0) {
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    return sim;
                }
            }
        }
        if (this.mSlotIdxToSubIdTemporary.size() > 0) {
            for (Entry<Integer, Integer> entry2 : this.mSlotIdxToSubIdTemporary.entrySet()) {
                sim = ((Integer) entry2.getKey()).intValue();
                if (subId == ((Integer) entry2.getValue()).intValue()) {
                    return sim;
                }
            }
        }
        int phoneId = mDefaultPhoneId;
        logdl("[getPhoneId], returning default phoneId=" + phoneId);
        return phoneId;
    }

    protected int[] getDummySubIds(int slotIndex) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = -2 - slotIndex;
        }
        return dummyValues;
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIndexToSubId.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            sSlotIndexToSubId.clear();
            logdl("[clearSubInfo]- clear size=" + size);
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected void logvl(String msg) {
        logv(msg);
        this.mLocalLog.log(msg);
    }

    protected void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    protected void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    protected static void slogd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    protected void logel(String msg) {
        loge(msg);
        this.mLocalLog.log(msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getDefaultSubId() {
        int subId;
        if (this.mContext.getResources().getBoolean(17957059)) {
            subId = getDefaultVoiceSubId();
        } else {
            subId = getDefaultDataSubId();
        }
        if (isActiveSubId(subId)) {
            return subId;
        }
        return mDefaultFallbackSubId;
    }

    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultSmsSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
        broadcastDefaultSmsSubIdChanged(subId);
    }

    private void broadcastDefaultSmsSubIdChanged(int subId) {
        logdl("[broadcastDefaultSmsSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultSmsSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
    }

    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultVoiceSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
        broadcastDefaultVoiceSubIdChanged(subId);
    }

    private void broadcastDefaultVoiceSubIdChanged(int subId) {
        logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultVoiceSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
    }

    public int getDefaultDataSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", -1);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void setDefaultDataSubId(int subId) {
        enforceModifyPhoneState("setDefaultDataSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
        ProxyController proxyController = ProxyController.getInstance();
        int len = sPhones.length;
        logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[len];
            boolean atLeastOneMatch = false;
            int[] currentAccessFamily = new int[]{0, 0};
            boolean[] isSimCtcc = new boolean[]{false, false};
            boolean isBindingNeeded = true;
            isSimCtcc[0] = TelephonyPhoneUtils.isSimCtcc(0, this.mContext);
            isSimCtcc[1] = TelephonyPhoneUtils.isSimCtcc(1, this.mContext);
            currentAccessFamily[0] = proxyController.getRadioAccessFamily(0);
            currentAccessFamily[1] = proxyController.getRadioAccessFamily(1);
            logd("isSimCtcc[0]=" + isSimCtcc[0] + " isSimCtcc[1]=" + isSimCtcc[1]);
            int phoneId = 0;
            while (phoneId < len) {
                int raf;
                int id = sPhones[phoneId].getSubId();
                if (id == subId) {
                    if (isSimCtcc[phoneId]) {
                        raf = proxyController.getMaxRafSupported();
                    } else {
                        raf = proxyController.getMinRafSupported();
                    }
                    atLeastOneMatch = true;
                } else if (!isSimCtcc[phoneId] || (isSimCtcc[1 - phoneId] ^ 1) == 0) {
                    raf = proxyController.getMinRafSupported();
                } else {
                    raf = proxyController.getMaxRafSupported();
                }
                logdl("[setDefaultDataSubId] phoneId=" + phoneId + " subId=" + id + " RAF=" + raf);
                rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                phoneId++;
            }
            logdl("currentAccessFamily[0]=" + currentAccessFamily[0] + " currentAccessFamily[1]=" + currentAccessFamily[1]);
            if (currentAccessFamily[0] >= rafs[0].getRadioAccessFamily() && currentAccessFamily[1] >= rafs[1].getRadioAccessFamily()) {
                isBindingNeeded = false;
            }
            if (!isBindingNeeded) {
                logdl("no need to binding beacuse present rat family can handle issues");
            } else if (atLeastOneMatch) {
                proxyController.setRadioCapability(rafs);
            } else {
                logdl("[setDefaultDataSubId] no valid subId's found - not updating.");
            }
        }
        updateAllDataConnectionTrackers();
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
        broadcastDefaultDataSubIdChanged(subId);
    }

    protected void updateAllDataConnectionTrackers() {
        int len = sPhones.length;
        logdl("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        for (int phoneId = 0; phoneId < len; phoneId++) {
            logdl("[updateAllDataConnectionTrackers] phoneId=" + phoneId);
            sPhones[phoneId].updateDataConnectionTracker();
        }
    }

    protected void broadcastDefaultDataSubIdChanged(int subId) {
        logdl("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected void setDefaultFallbackSubId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultFallbackSubId] subId=" + subId);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            int phoneId = getPhoneId(subId);
            if (phoneId < 0 || (phoneId >= this.mTelephonyManager.getPhoneCount() && this.mTelephonyManager.getSimCount() != 1)) {
                logdl("[setDefaultFallbackSubId] not set invalid phoneId=" + phoneId + " subId=" + subId);
                return;
            }
            logdl("[setDefaultFallbackSubId] set mDefaultFallbackSubId=" + subId);
            mDefaultFallbackSubId = subId;
            MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId), false);
            Intent intent = new Intent("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
            intent.addFlags(553648128);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subId);
            logdl("[setDefaultFallbackSubId] broadcast default subId changed phoneId=" + phoneId + " subId=" + subId);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void clearDefaultsForInactiveSubIds() {
    }

    protected boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSubscriptionId();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return true;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public List<SubscriptionInfo> getSubInfoUsingSlotIndexWithCheck(int slotIndex, boolean needCheck, String callingPackage) {
        Throwable th;
        logd("[getSubInfoUsingSlotIndexWithCheck]+ slotIndex:" + slotIndex);
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        if (slotIndex == Integer.MAX_VALUE) {
            try {
                slotIndex = getSlotIndex(getDefaultSubId());
            } catch (Exception e) {
                e.printStackTrace();
                Binder.restoreCallingIdentity(identity);
                return null;
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
                throw th2;
            }
        }
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            if (needCheck) {
                if ((isSubInfoReady() ^ 1) != 0) {
                    logd("[getSubInfoUsingSlotIndexWithCheck]- not ready");
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            }
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
            List<SubscriptionInfo> subList = null;
            if (cursor != null) {
                while (true) {
                    ArrayList<SubscriptionInfo> subList2;
                    ArrayList<SubscriptionInfo> subList3 = subList2;
                    try {
                        if (!cursor.moveToNext()) {
                            subList = subList3;
                            break;
                        }
                        SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                        if (subInfo != null) {
                            if (subList3 == null) {
                                subList2 = new ArrayList();
                            } else {
                                subList2 = subList3;
                            }
                            try {
                                subList2.add(subInfo);
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        } else {
                            subList2 = subList3;
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                        subList2 = subList3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th2;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getSubInfoUsingSlotIndex]- null info return");
            Binder.restoreCallingIdentity(identity);
            return subList;
        }
        logd("[getSubInfoUsingSlotIndexWithCheck]- invalid slotIndex");
        Binder.restoreCallingIdentity(identity);
        return null;
    }

    private void validateSubId(int subId) {
        logd("validateSubId subId: " + subId);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            throw new RuntimeException("Invalid sub id passed as parameter");
        } else if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("Default sub id passed as parameter");
        }
    }

    public void updatePhonesAvailability(Phone[] phones) {
        sPhones = phones;
    }

    public int[] getActiveSubIdList() {
        Set<Entry<Integer, Integer>> simInfoSet = new HashSet(sSlotIndexToSubId.entrySet());
        int[] subIdArr = new int[simInfoSet.size()];
        int i = 0;
        for (Entry<Integer, Integer> entry : simInfoSet) {
            subIdArr[i] = ((Integer) entry.getValue()).intValue();
            i++;
        }
        return subIdArr;
    }

    public boolean isActiveSubId(int subId) {
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            return sSlotIndexToSubId.containsValue(Integer.valueOf(subId));
        }
        return false;
    }

    public int getSimStateForSlotIndex(int slotIndex) {
        State simState;
        String err;
        if (slotIndex < 0) {
            simState = State.UNKNOWN;
            err = "invalid slotIndex";
        } else {
            Phone phone = PhoneFactory.getPhone(slotIndex);
            if (phone == null) {
                simState = State.UNKNOWN;
                err = "phone == null";
            } else {
                IccCard icc = phone.getIccCard();
                if (icc == null) {
                    simState = State.UNKNOWN;
                    err = "icc == null";
                } else {
                    simState = icc.getState();
                    err = "";
                }
            }
        }
        return simState.ordinal();
    }

    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        if (propKey.equals("enable_cmas_extreme_threat_alerts") || propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog")) {
            value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
        } else {
            logd("Invalid column name");
        }
        resolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(subId), null);
        refreshCachedActiveSubscriptionInfoList();
        Binder.restoreCallingIdentity(token);
    }

    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
            return null;
        }
        String resultValue = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{propKey}, InboundSmsHandler.SELECT_BY_ID, new String[]{subId + ""}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (!propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        if (!(propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog"))) {
                            logd("Invalid column name");
                        }
                    }
                    resultValue = cursor.getInt(0) + "";
                } else {
                    logd("Valid row not present in db");
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            logd("Query failed");
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("getSubscriptionProperty Query value = " + resultValue);
        return resultValue;
    }

    protected static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        slogd("StackTrace - " + msg);
        boolean first = true;
        for (StackTraceElement ste : re.getStackTrace()) {
            if (first) {
                first = false;
            } else {
                slogd(ste.toString());
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private String checkOperatorByIccidForSimName(String iccId) {
        String defaultSimName = "UNKNOWN";
        if (TelephonyPhoneUtils.sIsOversea) {
            return defaultSimName;
        }
        if (!TextUtils.isEmpty(iccId)) {
            if (TelephonyPhoneUtils.checkOperatorByIccid(iccId) == 2) {
                defaultSimName = this.mContext.getText(51249523).toString();
            } else if (TelephonyPhoneUtils.checkOperatorByIccid(iccId) == 0) {
                defaultSimName = this.mContext.getText(51249521).toString();
            } else if (TelephonyPhoneUtils.checkOperatorByIccid(iccId) == 1) {
                defaultSimName = this.mContext.getText(51249522).toString();
            }
        }
        return defaultSimName;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void saveInfoToTemporary() {
        if (this.mSlotIdxToSubIdTemporary != null) {
            this.mSlotIdxToSubIdTemporary.clear();
            if (sSlotIndexToSubId != null && sSlotIndexToSubId.size() > 0) {
                this.mSlotIdxToSubIdTemporary.putAll(sSlotIndexToSubId);
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void clearInfoFromTemporary() {
        if (this.mSlotIdxToSubIdTemporary != null) {
            this.mSlotIdxToSubIdTemporary.clear();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        try {
            pw.println("SubscriptionController:");
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                pw.println(" sSlotIndexToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                for (SubscriptionInfo entry2 : sirl) {
                    pw.println("  " + entry2.toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sirl = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" AllSubInfoList:");
                for (SubscriptionInfo entry22 : sirl) {
                    pw.println("  " + entry22.toString());
                }
            } else {
                pw.println(" AllSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            this.mLocalLog.dump(fd, pw, args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            pw.flush();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
