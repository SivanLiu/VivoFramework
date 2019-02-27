package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.telephony.Rlog;
import com.android.internal.telephony.HbpcdLookup.ArbitraryMccSidMatch;
import com.android.internal.telephony.HbpcdLookup.MccIdd;
import com.android.internal.telephony.HbpcdLookup.MccLookup;
import com.android.internal.telephony.HbpcdLookup.MccSidConflicts;
import com.android.internal.telephony.HbpcdLookup.MccSidRange;

public final class HbpcdUtils {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HbpcdUtils";
    private ContentResolver resolver = null;

    public HbpcdUtils(Context context) {
        this.resolver = context.getContentResolver();
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int getMcc(int sid, int tz, int DSTflag, boolean isNitzTimeZone) {
        Cursor cursor = null;
        try {
            cursor = this.resolver.query(ArbitraryMccSidMatch.CONTENT_URI, new String[]{"MCC"}, "SID=" + sid, null, null);
            int tmpMcc;
            if (cursor == null || cursor.getCount() != 1) {
                if (cursor != null) {
                    cursor.close();
                }
                Cursor cursor2 = null;
                try {
                    cursor2 = this.resolver.query(MccSidConflicts.CONTENT_URI, new String[]{"MCC"}, "SID_Conflict=" + sid + " and (((" + MccLookup.GMT_OFFSET_LOW + "<=" + tz + ") and (" + tz + "<=" + MccLookup.GMT_OFFSET_HIGH + ") and (" + "0=" + DSTflag + ")) or ((" + MccLookup.GMT_DST_LOW + "<=" + tz + ") and (" + tz + "<=" + MccLookup.GMT_DST_HIGH + ") and (" + "1=" + DSTflag + ")))", null, null);
                    if (cursor2 != null) {
                        int c3Counter = cursor2.getCount();
                        if (c3Counter > 0) {
                            if (c3Counter > 1) {
                                Rlog.w(LOG_TAG, "something wrong, get more results for 1 conflict SID: " + cursor2);
                            }
                            cursor2.moveToFirst();
                            tmpMcc = cursor2.getInt(0);
                            if (!isNitzTimeZone) {
                                tmpMcc = 0;
                            }
                            cursor2.close();
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                            return tmpMcc;
                        }
                        cursor2.close();
                    }
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                } catch (Throwable th) {
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                }
                Cursor cursor3 = null;
                try {
                    cursor3 = this.resolver.query(MccSidRange.CONTENT_URI, new String[]{"MCC"}, "SID_Range_Low<=" + sid + " and " + MccSidRange.RANGE_HIGH + ">=" + sid, null, null);
                    if (cursor3 == null || cursor3.getCount() <= 0) {
                        if (cursor3 != null) {
                            cursor3.close();
                        }
                        return 0;
                    }
                    cursor3.moveToFirst();
                    tmpMcc = cursor3.getInt(0);
                    cursor3.close();
                    if (cursor3 != null) {
                        cursor3.close();
                    }
                    return tmpMcc;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    if (cursor3 != null) {
                        cursor3.close();
                    }
                } catch (Throwable th2) {
                    if (cursor3 != null) {
                        cursor3.close();
                    }
                }
            } else {
                cursor.moveToFirst();
                tmpMcc = cursor.getInt(0);
                if (cursor != null) {
                    cursor.close();
                }
                return tmpMcc;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th3) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public String getIddByMcc(int mcc) {
        String idd = "";
        Cursor cursor = null;
        try {
            cursor = this.resolver.query(MccIdd.CONTENT_URI, new String[]{MccIdd.IDD}, "MCC=" + mcc, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                idd = cursor.getString(0);
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
        return idd;
    }
}
