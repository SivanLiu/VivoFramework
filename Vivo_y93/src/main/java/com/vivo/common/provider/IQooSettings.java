package com.vivo.common.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import java.util.TimeZone;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class IQooSettings {

    public static final class BlackList {
        public static final String AUTHORITY = "com.iqoo.secure.provider.blacklistprovider";
        private static final Uri CONTENT_BLACK_LIST_URI = Uri.parse("content://com.iqoo.secure.provider.blacklistprovider/black_list");
        private static final Uri CONTENT_QUERY_INCALL_BLACK_LIST_URI = Uri.parse("content://com.iqoo.secure.provider.blacklistprovider/query_intercept_incall");
        private static final Uri CONTENT_QUERY_SMS_BLACK_LIST_URI = Uri.parse("content://com.iqoo.secure.provider.blacklistprovider/query_intercept_sms");
        private static final Uri CONTENT_WHITE_LIST_URI = Uri.parse("content://com.iqoo.secure.provider.blacklistprovider/white_list");
        public static final String EXTRA_NAME = "name";
        public static final String EXTRA_NUMBER = "number";
        public static final String EXTRA_TYPE = "type";
        public static final String INTENT_ACTION_ADD_BLACKLIST = "bbk.intent.action.ADD_BLACKLIST";
        public static final String INTENT_ACTION_DEL_BLACKLIST = "bbk.intent.action.DEL_BLACKLIST";
        public static final String NUMBER = "number";
        private static final String TAG = "IQooSettings";
        private static final String URI_PREFIX = "content://com.iqoo.secure.provider.blacklistprovider";
        public static final String _ID = "_id";

        public static boolean isSmsIntercept(ContentResolver cr, String number) {
            boolean ans = isIntercept(cr, CONTENT_QUERY_SMS_BLACK_LIST_URI, number, null);
            Log.d(TAG, "isSmsIntercept is : " + ans);
            return ans;
        }

        public static boolean isIncallIntercept(ContentResolver cr, String number) {
            boolean ans = isIntercept(cr, CONTENT_QUERY_INCALL_BLACK_LIST_URI, number, null);
            Log.d(TAG, "isIncallIntercept is : " + ans);
            return ans;
        }

        public static boolean isInBlacklist(ContentResolver cr, String number) {
            boolean ans = isInBlacklist(cr, CONTENT_BLACK_LIST_URI, number);
            Log.d(TAG, "isInBlacklist is : " + ans);
            return ans;
        }

        public static boolean isInWhitelist(ContentResolver cr, String number) {
            boolean ans = isInBlacklist(cr, CONTENT_WHITE_LIST_URI, number);
            Log.d(TAG, "isInWhitelist is : " + ans);
            return ans;
        }

        private static boolean isIntercept(ContentResolver cr, Uri uri, String number, String body) {
            boolean ans;
            boolean interceptEnable = System.getInt(cr, Utils.SETTINGS_HARASS_INTERCEPT, 1) == 1;
            boolean timingInterceptEnable = System.getInt(cr, Utils.SETTINGS_TIMING_INTERCEPT, 0) == 1;
            int timing_intercept_mode = System.getInt(cr, Utils.SETTINGS_TIMING_INTERCEPT_MODE, 2);
            int intercept_mode = System.getInt(cr, Utils.SETTINGS_INTERCEPT_MODE, 1);
            if (!interceptEnable) {
                ans = false;
            } else if (timingInterceptEnable && isCurrentTimeInTimingIntercept(cr)) {
                ans = isInterceptWithMode(cr, uri, 1, number, body);
            } else {
                ans = isInterceptWithMode(cr, uri, 0, number, body);
            }
            Log.d(TAG, "isIntercept() = " + ans);
            return ans;
        }

        public static boolean isCurrentTimeInTimingIntercept(ContentResolver cr) {
            long currentTime = ((System.currentTimeMillis() + ((long) TimeZone.getDefault().getRawOffset())) / 60000) % 1440;
            String mStartTimeString = System.getString(cr, Utils.TIMING_INTERCEPT_START_TIME);
            if (TextUtils.isEmpty(mStartTimeString)) {
                mStartTimeString = Utils.DEFAULT_TIMING_INTERCEPT_START_TIME;
                System.putString(cr, Utils.TIMING_INTERCEPT_START_TIME, mStartTimeString);
            }
            int hourOfDay = Integer.parseInt(mStartTimeString.substring(0, 2));
            long startTime = (long) ((hourOfDay * 60) + Integer.parseInt(mStartTimeString.substring(3)));
            String mEndTimeString = System.getString(cr, Utils.TIMING_INTERCEPT_END_TIME);
            if (TextUtils.isEmpty(mEndTimeString)) {
                mEndTimeString = Utils.DEFAULT_TIMING_INTERCEPT_END_TIME;
                System.putString(cr, Utils.TIMING_INTERCEPT_END_TIME, mEndTimeString);
            }
            hourOfDay = Integer.parseInt(mEndTimeString.substring(0, 2));
            long endTime = (long) ((hourOfDay * 60) + Integer.parseInt(mEndTimeString.substring(3)));
            if (startTime < 0) {
                startTime = 0;
            }
            if (endTime < 0) {
                endTime = 0;
            }
            if (startTime > 1439) {
                startTime = 1439;
            }
            if (endTime > 1439) {
                endTime = 1439;
            }
            if (startTime > endTime) {
                if (startTime <= currentTime && currentTime <= 1439) {
                    return true;
                }
                if (0 > currentTime || currentTime > endTime) {
                    return false;
                }
                return true;
            } else if (startTime > endTime || startTime > currentTime || currentTime > endTime) {
                return false;
            } else {
                return true;
            }
        }

        private static boolean isInterceptWithMode(ContentResolver cr, Uri uri, int InterceptMode, String number, String body) {
            Log.d(TAG, "isInterceptWithMode() InterceptMode = " + InterceptMode);
            boolean ans = false;
            switch (InterceptMode) {
                case 0:
                    if (!Utils.isRejectNoNumberEnable(cr) || !TextUtils.isEmpty(number)) {
                        if (!isSecretContact(cr, number)) {
                            if (!isInWhitelist(cr, number)) {
                                if (!Utils.isRejectBlacklistEnable(cr) || !isMatchBlacklist(cr, uri, number)) {
                                    if (isInContacts(cr, number)) {
                                        ans = false;
                                        break;
                                    }
                                }
                                ans = true;
                                break;
                            }
                            ans = false;
                            break;
                        }
                        ans = false;
                        break;
                    }
                    Log.d(TAG, "RejectNoNumber is true, should reject. ");
                    return true;
                    break;
                case 1:
                    ans = true;
                    if (isSecretContact(cr, number) || isInWhitelist(cr, number) || isInContacts(cr, number)) {
                        ans = false;
                        break;
                    }
            }
            return ans;
        }

        private static synchronized boolean isIntercept_PD1304T(ContentResolver cr, Uri uri, String number) {
            synchronized (BlackList.class) {
                if (TextUtils.isEmpty(number)) {
                    return false;
                }
                boolean ans = false;
                Cursor cursor = null;
                try {
                    cursor = cr.query(uri, null, number, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        ans = true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ans;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        private static boolean isInterceptInSmartMode(ContentResolver cr, Uri uri, String number, String body) {
            if (isSecretContact(cr, number)) {
                return false;
            }
            if (isMatchBlacklist(cr, uri, number)) {
                return true;
            }
            if (isInWhitelist(cr, number) || isInContacts(cr, number)) {
                return false;
            }
            Log.d(TAG, "isInterceptInSmartMode() return " + false + ", will use TMSSDK in SmsReceiver.java");
            return false;
        }

        private static synchronized boolean isInContacts(ContentResolver cr, String phoneNumber) {
            synchronized (BlackList.class) {
                if (TextUtils.isEmpty(phoneNumber)) {
                    return false;
                }
                Log.d(TAG, "isInContacts() begin");
                boolean ans = false;
                Cursor cursor = null;
                try {
                    cursor = cr.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)), new String[]{"display_name"}, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        ans = true;
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
                Log.d(TAG, "isInContacts() end return : " + ans);
                return ans;
            }
        }

        private static boolean isInPrivacyContacts(ContentResolver cr, String Number) {
            return false;
        }

        private static boolean isInTencentBlacklist(ContentResolver cr, String number) {
            return false;
        }

        private static boolean isInterceptMatchKeywords(ContentResolver cr, String body) {
            return false;
        }

        private static synchronized boolean isInBlacklist(ContentResolver cr, Uri uri, String number) {
            synchronized (BlackList.class) {
                if (TextUtils.isEmpty(number)) {
                    return false;
                }
                boolean ans = false;
                Log.d(TAG, "is in blacklist or whitelist number is : " + Utils.removePrefix12520(Utils.formatNumber(number)));
                Cursor cursor = null;
                String selection = "number = ?";
                if (uri.equals(CONTENT_BLACK_LIST_URI)) {
                    selection = "reject_type = 1 and number= ?";
                }
                try {
                    ContentResolver contentResolver = cr;
                    Uri uri2 = uri;
                    cursor = contentResolver.query(uri2, null, selection, new String[]{num}, "_id desc");
                    if (cursor != null && cursor.getCount() > 0) {
                        ans = true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ans;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        private static synchronized boolean isMatchBlacklist(ContentResolver cr, Uri uri, String number) {
            synchronized (BlackList.class) {
                if (TextUtils.isEmpty(number)) {
                    return false;
                }
                boolean ans = false;
                Cursor cursor = null;
                try {
                    cursor = cr.query(uri, null, number, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        ans = true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ans;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        public static synchronized boolean isSecretContact(ContentResolver cr, String number) {
            synchronized (BlackList.class) {
                if (TextUtils.isEmpty(number)) {
                    return false;
                }
                boolean isSecret = false;
                Cursor cursor = null;
                String num = Utils.removePrefix12520(Utils.formatNumber(number));
                Log.d(TAG, "is Secret Contect num is : " + num);
                try {
                    cursor = cr.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num)).buildUpon().appendQueryParameter("encrypt", "> 0").build(), null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        isSecret = true;
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
                Log.d(TAG, "isSecretContact() is : " + isSecret);
                return isSecret;
            }
        }
    }
}
