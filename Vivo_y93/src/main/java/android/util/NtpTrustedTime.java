package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.os.Debug;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.SettingsStringUtil;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.android.internal.R;
import com.vivo.content.Weather;
import java.util.ArrayList;

public class NtpTrustedTime implements TrustedTime {
    private static final boolean LOGD = true;
    private static final String TAG = "NtpTrustedTime";
    private static String mBackupServer = "";
    private static int mNtpRetries = 0;
    private static int mNtpRetriesMax = 0;
    private static Context sContext;
    private static final ArrayList sNTPServerList = new ArrayList();
    private static NtpTrustedTime sSingleton;
    private String ACTION_NTP_TIMES = "vivo.android.intent.action.NTP_TIMES";
    private final int INTERNAL_ALLOW_NTP_TIME = 60000;
    private String KEY_NTPTIME = "ntpTime";
    private String KEY_NTPTIME_REFERENCE = "ntpTimeReference";
    private final int TIMEOUT_NTP_SERVER_CONNECTTING = Weather.WEATHERVERSION_ROM_3_5;
    private boolean mBackupmode = false;
    private ConnectivityManager mCM;
    private long mCachedNtpCertainty;
    private long mCachedNtpElapsedRealtime;
    private long mCachedNtpTime;
    private boolean mHasCache;
    private final String mServer;
    private final long mTimeout;

    static {
        sNTPServerList.add("2.asia.pool.ntp.org");
        sNTPServerList.add("time.gpsonextra.net");
        sNTPServerList.add("asia.pool.ntp.org");
        sNTPServerList.add("pool.ntp.org");
        sNTPServerList.add("1.cn.pool.ntp.org");
    }

    private NtpTrustedTime(String server, long timeout) {
        Log.d(TAG, "creating NtpTrustedTime using " + server);
        this.mServer = server;
        this.mTimeout = timeout;
    }

    public static synchronized NtpTrustedTime getInstance(Context context) {
        NtpTrustedTime ntpTrustedTime;
        synchronized (NtpTrustedTime.class) {
            if (sSingleton == null) {
                Resources res = context.getResources();
                ContentResolver resolver = context.getContentResolver();
                String defaultServer = res.getString(R.string.config_ntpServer);
                long defaultTimeout = (long) res.getInteger(R.integer.config_ntpTimeout);
                String secureServer = Global.getString(resolver, Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Global.getLong(resolver, Global.NTP_TIMEOUT, defaultTimeout));
                sContext = context;
                if (sSingleton != null) {
                    String backupServer = SystemProperties.get("persist.backup.ntpServer");
                    ntpTrustedTime = sSingleton;
                    mNtpRetriesMax = res.getInteger(R.integer.config_ntpRetry);
                    ntpTrustedTime = sSingleton;
                    if (mNtpRetriesMax <= 0 || backupServer == null || backupServer.length() == 0) {
                        ntpTrustedTime = sSingleton;
                        mNtpRetriesMax = 0;
                        ntpTrustedTime = sSingleton;
                        mBackupServer = "";
                    } else {
                        ntpTrustedTime = sSingleton;
                        mBackupServer = backupServer.trim().replace("\"", "");
                    }
                }
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    public boolean forceRefresh() {
        if (TextUtils.isEmpty(this.mServer)) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService("connectivity");
            }
        }
        NetworkInfo ni = this.mCM == null ? null : this.mCM.getActiveNetworkInfo();
        if (ni == null || (ni.isConnected() ^ 1) != 0) {
            Log.d(TAG, "forceRefresh: no connectivity");
            return false;
        }
        Log.d(TAG, "forceRefresh() from cache miss");
        SntpClient client = new SntpClient();
        LongArray mTmpCachedNtpTimeArray = new LongArray();
        LongArray mTmpCachedNtpElapsedRealtimeArray = new LongArray();
        LongArray mTmpCachedNtpCertaintyArray = new LongArray();
        Log.d(TAG, "forceRefresh() start to get time from all ntp servers. " + Debug.getCallers(2));
        int serverCount = sNTPServerList.size();
        for (int index = 0; index < serverCount; index++) {
            if (client.requestTime(sNTPServerList.get(index).toString(), Weather.WEATHERVERSION_ROM_3_5)) {
                long ntpTime = client.getNtpTime();
                long ntpElapsedRealtime = client.getNtpTimeReference();
                long ntpCertainty = client.getRoundTripTime() / 2;
                Log.d(TAG, "forceRefresh() getting time from server " + index + SettingsStringUtil.DELIMITER + sNTPServerList.get(index).toString() + " successfully. NtpTime is " + ntpTime + ", NtpElapsedRealtime is " + ntpElapsedRealtime + ", NtpCertainty is " + ntpCertainty);
                if (ntpTime < 0) {
                    Log.d(TAG, "forceRefresh() continue to get time from next server.");
                } else {
                    mTmpCachedNtpTimeArray.add(ntpTime);
                    mTmpCachedNtpElapsedRealtimeArray.add(ntpElapsedRealtime);
                    mTmpCachedNtpCertaintyArray.add(ntpCertainty);
                    int curArraySize = mTmpCachedNtpTimeArray.size();
                    long last = mTmpCachedNtpTimeArray.get(curArraySize - 1);
                    for (int k = 0; k < curArraySize - 1; k++) {
                        if (Math.abs(last - mTmpCachedNtpTimeArray.get(k)) < DateUtils.MINUTE_IN_MILLIS) {
                            this.mHasCache = true;
                            this.mCachedNtpTime = mTmpCachedNtpTimeArray.get(k);
                            this.mCachedNtpElapsedRealtime = mTmpCachedNtpElapsedRealtimeArray.get(k);
                            this.mCachedNtpCertainty = mTmpCachedNtpCertaintyArray.get(k);
                            broadcastNtpTime();
                            return true;
                        }
                    }
                    continue;
                }
            } else {
                Log.d(TAG, "forceRefresh() getting time from server " + index + " failed.");
            }
        }
        Log.e(TAG, "forceRefresh failed to get a suitable ntp time from all ntp servers.");
        return false;
    }

    public boolean hasCache() {
        return this.mHasCache;
    }

    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long currentTimeMillis() {
        if (this.mHasCache) {
            Log.d(TAG, "currentTimeMillis() cache hit");
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    public long getCachedNtpTime() {
        Log.d(TAG, "getCachedNtpTime() cache hit");
        return this.mCachedNtpTime;
    }

    public long getCachedNtpTimeReference() {
        return this.mCachedNtpElapsedRealtime;
    }

    public void setBackupmode(boolean mode) {
        if (isBackupSupported()) {
            this.mBackupmode = mode;
        }
        Log.d(TAG, "setBackupmode() set the backup mode to be:" + this.mBackupmode);
    }

    private boolean getBackupmode() {
        return this.mBackupmode;
    }

    private boolean isBackupSupported() {
        if (mNtpRetriesMax <= 0 || mBackupServer == null || mBackupServer.length() == 0) {
            return false;
        }
        return true;
    }

    private void countInBackupmode() {
        if (isBackupSupported()) {
            mNtpRetries++;
            if (mNtpRetries == mNtpRetriesMax) {
                mNtpRetries = 0;
                setBackupmode(true);
            }
        }
        Log.d(TAG, "countInBackupmode() func");
    }

    public void broadcastNtpTime() {
        Intent intent = new Intent(this.ACTION_NTP_TIMES);
        intent.putExtra(this.KEY_NTPTIME, this.mCachedNtpTime);
        intent.putExtra(this.KEY_NTPTIME_REFERENCE, this.mCachedNtpElapsedRealtime);
        sContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "send sticky broadcast to nitz, mCachedNtpTime=" + this.mCachedNtpTime + ", mCachedNtpElapsedRealtime=" + this.mCachedNtpElapsedRealtime);
    }
}
