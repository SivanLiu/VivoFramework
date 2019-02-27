package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccConstants;
import com.vivo.services.security.client.VivoPermissionManager;
import java.util.List;

public class IccProvider extends ContentProvider {
    @VivoHook(hookType = VivoHookType.CHANGE_CODE_AND_ACCESS)
    public static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[]{STR_INDEX, "name", STR_NUMBER, STR_EMAILS, STR_ANRS, HbpcdLookup.ID};
    protected static final int ADN = 1;
    protected static final int ADN_ALL = 7;
    protected static final int ADN_SUB = 2;
    private static final boolean DBG = true;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_ADN_LIST_NOT_EXIST = -11;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_ANR_FULL = -14;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_ANR_TOO_LONG = -6;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_EMAIL_FULL = -12;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_EMAIL_TOOLONG = -13;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_GENERIC_FAILURE = -10;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_NOT_READY = -4;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_NO_ERROR = 1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_NUMBER_TOO_LONG = -1;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_PASSWORD_ERROR = -5;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_STORAGE_FULL = -3;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_TEXT_TOO_LONG = -2;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static final int ERROR_ICC_PROVIDER_UNKNOWN = 0;
    protected static final int FDN = 3;
    protected static final int FDN_SUB = 4;
    protected static final int SDN = 5;
    protected static final int SDN_SUB = 6;
    public static final String STR_ANRS = "anrs";
    public static final String STR_EMAILS = "emails";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected static final String STR_ID = "id";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected static final String STR_INDEX = "index";
    public static final String STR_NEW_ANRS = "newAnrs";
    public static final String STR_NEW_EMAILS = "newEmails";
    public static final String STR_NEW_NUMBER = "newNumber";
    public static final String STR_NEW_TAG = "newTag";
    public static final String STR_NUMBER = "number";
    public static final String STR_PIN2 = "pin2";
    public static final String STR_TAG = "tag";
    private static final String TAG = "IccProvider";
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mPrivilegeFlag = false;
    private SubscriptionManager mSubscriptionManager;

    static {
        URL_MATCHER.addURI("icc", "adn", 1);
        URL_MATCHER.addURI("icc", "adn/subId/#", 2);
        URL_MATCHER.addURI("icc", "fdn", 3);
        URL_MATCHER.addURI("icc", "fdn/subId/#", 4);
        URL_MATCHER.addURI("icc", "sdn", 5);
        URL_MATCHER.addURI("icc", "sdn/subId/#", 6);
    }

    public boolean onCreate() {
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        return true;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean checkReadContactsPermission() {
        return VivoPermissionManager.checkCallingVivoPermission("android.permission.READ_CONTACTS");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean checkWriteContactsPermission() {
        return VivoPermissionManager.checkCallingVivoPermission("android.permission.WRITE_CONTACTS");
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        log("query, callingPackage = " + getCallingPackage());
        if (!checkReadContactsPermission()) {
            return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
        }
        switch (URL_MATCHER.match(url)) {
            case 1:
                return loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
            case 2:
                return loadFromEf(28474, getRequestSubId(url));
            case 3:
                return loadFromEf(IccConstants.EF_FDN, SubscriptionManager.getDefaultSubscriptionId());
            case 4:
                return loadFromEf(IccConstants.EF_FDN, getRequestSubId(url));
            case 5:
                return loadFromEf(IccConstants.EF_SDN, SubscriptionManager.getDefaultSubscriptionId());
            case 6:
                return loadFromEf(IccConstants.EF_SDN, getRequestSubId(url));
            case 7:
                return loadAllSimContacts(28474);
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private Cursor loadAllSimContacts(int efType) {
        Cursor[] result;
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList == null || subInfoList.size() == 0) {
            result = new Cursor[0];
        } else {
            int subIdCount = subInfoList.size();
            result = new Cursor[subIdCount];
            for (int i = 0; i < subIdCount; i++) {
                int subId = ((SubscriptionInfo) subInfoList.get(i)).getSubscriptionId();
                result[i] = loadFromEf(efType, subId);
                Rlog.i(TAG, "ADN Records loaded for Subscription ::" + subId);
            }
        }
        return new MergeCursor(result);
    }

    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public Uri insert(Uri url, ContentValues initialValues) {
        String pin2 = null;
        log("insert");
        if (!checkWriteContactsPermission()) {
            return null;
        }
        int efType;
        int subId;
        int match = URL_MATCHER.match(url);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = initialValues.getAsString(STR_TAG);
        String number = initialValues.getAsString(STR_NUMBER);
        String emails = initialValues.getAsString(STR_EMAILS);
        String anrs = covertPauseAndWaitForSim(initialValues.getAsString(STR_ANRS));
        ContentValues values = new ContentValues();
        values.put(STR_TAG, "");
        values.put(STR_NUMBER, "");
        values.put(STR_EMAILS, "");
        values.put(STR_ANRS, "");
        values.put(STR_NEW_TAG, tag);
        values.put(STR_NEW_NUMBER, number);
        values.put(STR_NEW_EMAILS, emails);
        values.put(STR_NEW_ANRS, anrs);
        int result = updateIccRecordInEfWithError(efType, values, pin2, subId);
        StringBuilder buf = new StringBuilder("content://icc/");
        if (result > 0) {
            switch (match) {
                case 1:
                    buf.append("adn/");
                    break;
                case 2:
                    buf.append("adn/subId/");
                    break;
                case 3:
                    buf.append("fdn/");
                    break;
                case 4:
                    buf.append("fdn/subId/");
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot insert into URL: " + url);
            }
        } else if (efType != 28474) {
            return null;
        } else {
            buf.append("error/");
        }
        buf.append(result);
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(url, null);
        log("insert resultUri = " + resultUri.toString());
        return resultUri;
    }

    private String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            log("len of input String is 0");
            return inVal;
        }
        String retVal = inVal;
        if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            retVal = inVal.substring(1, len - 1);
        }
        return retVal;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int delete(Uri url, String where, String[] whereArgs) {
        if (!checkWriteContactsPermission()) {
            return 0;
        }
        int efType;
        int subId;
        switch (URL_MATCHER.match(url)) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        log("delete");
        CharSequence tag = null;
        CharSequence number = null;
        String emails = null;
        String anrs = null;
        String pin2 = null;
        int index = -1;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                log("parsing '" + param + "'");
                String[] pair = param.split("=", 2);
                if (pair.length != 2) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[1].trim();
                    if (STR_TAG.equals(key)) {
                        tag = normalizeValue(val);
                    } else if (STR_NUMBER.equals(key)) {
                        number = normalizeValue(val);
                    } else if (STR_EMAILS.equals(key)) {
                        emails = normalizeValue(val);
                    } else if (STR_ANRS.equals(key)) {
                        anrs = covertPauseAndWaitForSim(normalizeValue(val));
                    } else if (STR_PIN2.equals(key)) {
                        pin2 = normalizeValue(val);
                    } else if (STR_INDEX.equals(key)) {
                        index = Integer.parseInt(val);
                    }
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(STR_TAG, tag);
                values.put(STR_NUMBER, number);
                values.put(STR_EMAILS, emails);
                values.put(STR_ANRS, anrs);
                values.put(STR_NEW_TAG, "");
                values.put(STR_NEW_NUMBER, "");
                values.put(STR_NEW_EMAILS, "");
                values.put(STR_NEW_ANRS, "");
                if (efType == 3 && TextUtils.isEmpty(pin2)) {
                    return 0;
                }
                int result;
                log("delete mvalues= " + values);
                if (index > 0) {
                    log("delete index is " + index);
                    result = updateIccRecordInEfByIndexWithError(efType, "", "", null, null, index, pin2, subId);
                } else if (TextUtils.isEmpty(tag) && TextUtils.isEmpty(number)) {
                    log("delete tag&number both empty!");
                    return 0;
                } else {
                    result = updateIccRecordInEfWithError(efType, values, pin2, subId);
                }
                getContext().getContentResolver().notifyChange(url, null);
                if (efType == 28474) {
                    return result;
                }
                if (result <= 0) {
                    return 0;
                }
                return 1;
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        String pin2 = null;
        log("update");
        if (!checkWriteContactsPermission()) {
            return 0;
        }
        int efType;
        int subId;
        int result;
        switch (URL_MATCHER.match(url)) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = values.getAsString(STR_PIN2);
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = values.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = values.getAsString(STR_TAG);
        String number = values.getAsString(STR_NUMBER);
        String emails = values.getAsString(STR_EMAILS);
        String anrs = values.getAsString(STR_ANRS);
        String newTag = values.getAsString(STR_NEW_TAG);
        String newNumber = values.getAsString(STR_NEW_NUMBER);
        String newEmails = values.getAsString(STR_NEW_EMAILS);
        String newAnrs = values.getAsString(STR_NEW_ANRS);
        Integer intId = values.getAsInteger(STR_INDEX);
        int index = 0;
        if (intId != null) {
            index = intId.intValue();
        }
        anrs = covertPauseAndWaitForSim(anrs);
        newAnrs = covertPauseAndWaitForSim(newAnrs);
        ContentValues values2 = new ContentValues();
        values2.put(STR_TAG, tag);
        values2.put(STR_NUMBER, number);
        values2.put(STR_EMAILS, emails);
        values2.put(STR_ANRS, anrs);
        values2.put(STR_NEW_TAG, newTag);
        values2.put(STR_NEW_NUMBER, newNumber);
        values2.put(STR_NEW_EMAILS, newEmails);
        values2.put(STR_NEW_ANRS, newAnrs);
        if (index > 0) {
            result = updateIccRecordInEfByIndexWithError(efType, newTag, newNumber, newAnrs, newEmails, index, pin2, subId);
        } else {
            result = updateIccRecordInEfWithError(efType, values2, pin2, subId);
        }
        getContext().getContentResolver().notifyChange(url, null);
        if (efType == 28474) {
            return result;
        }
        if (result <= 0) {
            return 0;
        }
        return 1;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private MatrixCursor loadFromEf(int efType, int subId) {
        log("loadFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            log("adnRecords.size=" + N);
            for (int i = 0; i < N; i++) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return null;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        log("addIccRecordToEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, "", "", name, number, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("addIccRecordToEf: " + success);
        return success;
    }

    @Deprecated
    private boolean updateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        log("updateIccRecordInEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(subId, efType, values, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEf: " + success);
        return success;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        log("deleteIccRecordFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, "", "", pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("deleteIccRecordFromEf: " + success);
        return success;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        if (record == null) {
            log("adnRecord get null, index = " + id);
            return;
        }
        if (!record.isEmpty()) {
            Object[] contact = new Object[ADDRESS_BOOK_COLUMN_NAMES.length];
            String alphaTag = record.getAlphaTag();
            String number = resumePauseAndWaitForSim(record.getNumber());
            String[] anrs = record.getAdditionalNumbers();
            contact[1] = alphaTag;
            contact[2] = number;
            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                for (String email : emails) {
                    emailString.append(email);
                    emailString.append(",");
                }
                contact[3] = emailString.toString();
            }
            if (anrs != null) {
                StringBuilder anrString = new StringBuilder();
                for (String anr : anrs) {
                    if (!TextUtils.isEmpty(anr)) {
                        anrString.append(resumePauseAndWaitForSim(anr));
                        anrString.append(":");
                    }
                }
                String anrStr = anrString.toString();
                contact[4] = anrStr.substring(0, anrStr.length() - 1);
            }
            contact[5] = Integer.valueOf(id);
            int index = record.getRecId();
            log("Adding id = " + id + " , index = " + index);
            contact[0] = Integer.valueOf(index);
            cursor.addRow(contact);
        }
    }

    private void log(String msg) {
        Rlog.d(TAG, "[IccProvider] " + msg);
    }

    private int getRequestSubId(Uri url) {
        log("getRequestSubId url: " + url);
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int updateIccRecordInEfWithError(int efType, ContentValues values, String pin2, int subId) {
        log("updateIccRecordInEfWithError: efType=" + efType + ", subId:" + subId);
        int result = 0;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId(efType, values, pin2, subId);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEfWithError: " + result);
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int updateIccRecordInEfByIndexWithError(int efType, String newTag, String newPhoneNumber, String newAnrs, String newEmails, int index, String pin2, int subId) {
        log("updateIccRecordInEfByIndexWithError: efType = " + efType + " , subId= " + subId);
        int result = 0;
        String[] strArr = null;
        String[] strArr2 = null;
        if (!TextUtils.isEmpty(newEmails)) {
            strArr = new String[]{newEmails};
        }
        if (!TextUtils.isEmpty(newAnrs)) {
            strArr2 = new String[]{newAnrs};
        }
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfByIndexWithErrorUsingSubId(efType, newTag, newPhoneNumber, strArr2, strArr, index, pin2, subId);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEfByIndexWithError: " + result);
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected static String covertPauseAndWaitForSim(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        return number.replace(",", "p").replace(";", "w");
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected static String resumePauseAndWaitForSim(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        return number.replace("p", ",").replace("w", ";").replace('T', ',').replace('e', ';');
    }
}
