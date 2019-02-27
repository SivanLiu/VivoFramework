package com.android.internal.telephony.uicc;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import java.util.ArrayList;
import java.util.Iterator;

public class AdnRecordCache extends Handler implements IccConstants {
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static boolean DBG = true;
    static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 1;
    static final int EVENT_UPDATE_ADN_DONE = 2;
    static final String LOG_TAG = "AdnRecordCache";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static int MAX_PHB_NAME_LENGTH = 60;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    public static int MAX_PHB_NUMBER_LENGTH = 40;
    SparseArray<ArrayList<AdnRecord>> mAdnLikeFiles = new SparseArray();
    SparseArray<ArrayList<Message>> mAdnLikeWaiters = new SparseArray();
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private int mAdncountofIcc = 0;
    private IccFileHandler mFh;
    SparseArray<Message> mUserWriteResponse = new SparseArray();
    private UsimPhoneBookManager mUsimPhoneBookManager;

    AdnRecordCache(IccFileHandler fh) {
        this.mFh = fh;
        this.mUsimPhoneBookManager = new UsimPhoneBookManager(this.mFh, this);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void reset() {
        this.mAdnLikeFiles.clear();
        this.mUsimPhoneBookManager.reset();
        clearWaiters();
        clearUserWriters();
        logd("AdnRecordCache reset over!");
    }

    private void clearWaiters() {
        int size = this.mAdnLikeWaiters.size();
        for (int i = 0; i < size; i++) {
            notifyWaiters((ArrayList) this.mAdnLikeWaiters.valueAt(i), new AsyncResult(null, null, new RuntimeException("AdnCache reset")));
        }
        this.mAdnLikeWaiters.clear();
    }

    private void clearUserWriters() {
        int size = this.mUserWriteResponse.size();
        for (int i = 0; i < size; i++) {
            sendErrorResponse((Message) this.mUserWriteResponse.valueAt(i), "AdnCace reset");
        }
        this.mUserWriteResponse.clear();
    }

    public ArrayList<AdnRecord> getRecordsIfLoaded(int efid) {
        return (ArrayList) this.mAdnLikeFiles.get(efid);
    }

    public int extensionEfForEf(int efid) {
        switch (efid) {
            case IccConstants.EF_PBR /*20272*/:
                return 0;
            case 28474:
                return IccConstants.EF_EXT1;
            case IccConstants.EF_FDN /*28475*/:
                return IccConstants.EF_EXT2;
            case IccConstants.EF_MSISDN /*28480*/:
                return IccConstants.EF_EXT1;
            case IccConstants.EF_SDN /*28489*/:
                return IccConstants.EF_EXT3;
            case IccConstants.EF_MBDN /*28615*/:
                return IccConstants.EF_EXT6;
            default:
                return -1;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void sendErrorResponse(Message response, String errString) {
        sendErrorResponse(response, errString, 2);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void sendErrorResponse(Message response, String errString, int ril_errno) {
        CommandException e = CommandException.fromRilErrno(ril_errno);
        if (response != null) {
            logd(errString);
            AsyncResult.forMessage(response).exception = e;
            response.sendToTarget();
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    @Deprecated
    public void updateAdnByIndex(int efid, AdnRecord adn, int recordIndex, String pin2, Message response) {
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else if (adn.mAlphaTag.length() > MAX_PHB_NAME_LENGTH) {
            sendErrorResponse(response, "the input length of alphaTag is too long: " + adn.mAlphaTag, 1002);
        } else {
            int num_length = adn.mNumber.length();
            if (adn.mNumber.indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > MAX_PHB_NUMBER_LENGTH) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + adn.mNumber, 1001);
            } else if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
            } else {
                this.mUserWriteResponse.put(efid, response);
                new AdnRecordLoader(this.mFh).updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
            }
        }
    }

    @Deprecated
    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        ArrayList<AdnRecord> oldAdnList;
        if (efid == IccConstants.EF_PBR) {
            oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            sendErrorResponse(response, "Adn list not exist for EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        int index = -1;
        int count = 1;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        while (it.hasNext()) {
            if (oldAdn.isEqual((AdnRecord) it.next())) {
                index = count;
                break;
            }
            count++;
        }
        if (index == -1) {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
            return;
        }
        if (efid == IccConstants.EF_PBR) {
            AdnRecord foundAdn = (AdnRecord) oldAdnList.get(index - 1);
            efid = foundAdn.mEfid;
            extensionEF = foundAdn.mExtRecord;
            index = foundAdn.mRecordNumber;
            newAdn.mEfid = efid;
            newAdn.mExtRecord = extensionEF;
            newAdn.mRecordNumber = index;
        }
        if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        this.mUserWriteResponse.put(efid, response);
        new AdnRecordLoader(this.mFh).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public synchronized void updateAdnBySearchWithError(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        logd("updateAdnBySearch efid:" + efid);
        int index = -1;
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else if (newAdn.mAlphaTag.length() > MAX_PHB_NAME_LENGTH) {
            sendErrorResponse(response, "the input length of alphaTag is too long: " + newAdn.mAlphaTag, 1002);
        } else {
            int num_length = newAdn.mNumber.length();
            if (newAdn.mNumber.indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > MAX_PHB_NUMBER_LENGTH) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.mNumber, 1001);
                return;
            }
            ArrayList oldAdnList;
            if (efid == 20272) {
                try {
                    oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
                } catch (NullPointerException e) {
                    oldAdnList = null;
                }
            } else {
                oldAdnList = getRecordsIfLoaded(efid);
            }
            if (oldAdnList == null) {
                sendErrorResponse(response, "Adn list not exist for EF:0x" + Integer.toHexString(efid).toUpperCase(), 1011);
                return;
            }
            int count = 1;
            Iterator<AdnRecord> it = oldAdnList.iterator();
            while (it.hasNext()) {
                if (oldAdn.isEqual((AdnRecord) it.next())) {
                    index = count;
                    break;
                }
                count++;
            }
            if (index == -1) {
                sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
                return;
            }
            logd("updateAdnBySearch, index :" + index);
            if (efid == 20272) {
                AdnRecord foundAdn = (AdnRecord) oldAdnList.get(index - 1);
                efid = foundAdn.mEfid;
                extensionEF = foundAdn.mExtRecord;
                index = foundAdn.mRecordNumber;
                newAdn.mEfid = efid;
                newAdn.mExtRecord = extensionEF;
                newAdn.mRecordNumber = index;
            }
            if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
                return;
            }
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void requestLoadAllAdnLike(int efid, int extensionEf, Message response) {
        ArrayList<AdnRecord> result;
        boolean z;
        logd("requestLoadAllAdnLike " + efid);
        if (efid == IccConstants.EF_PBR) {
            ArrayList<AdnRecord> combinedResult = new ArrayList();
            result = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
            if (result != null) {
                combinedResult.addAll(result);
            }
        } else {
            result = getRecordsIfLoaded(efid);
        }
        StringBuilder append = new StringBuilder().append("requestLoadAllAdnLike result = null ?");
        if (result == null) {
            z = true;
        } else {
            z = false;
        }
        logd(append.append(z).toString());
        if (result != null) {
            if (response != null) {
                AsyncResult.forMessage(response).result = result;
                response.sendToTarget();
            }
            return;
        }
        ArrayList<Message> waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
        if (waiters != null) {
            waiters.add(response);
            return;
        }
        waiters = new ArrayList();
        waiters.add(response);
        this.mAdnLikeWaiters.put(efid, waiters);
        if (extensionEf < 0) {
            if (response != null) {
                AsyncResult.forMessage(response).exception = new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
                response.sendToTarget();
            }
            return;
        }
        new AdnRecordLoader(this.mFh).loadAllFromEF(efid, extensionEf, obtainMessage(1, efid, 0));
    }

    private void notifyWaiters(ArrayList<Message> waiters, AsyncResult ar) {
        if (waiters != null) {
            int s = waiters.size();
            for (int i = 0; i < s; i++) {
                Message waiter = (Message) waiters.get(i);
                AsyncResult.forMessage(waiter, ar.result, ar.exception);
                waiter.sendToTarget();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void handleMessage(Message msg) {
        AsyncResult ar;
        int efid;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                efid = msg.arg1;
                ArrayList<Message> waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
                this.mAdnLikeWaiters.delete(efid);
                if (ar.exception == null) {
                    this.mAdnLikeFiles.put(efid, (ArrayList) ar.result);
                } else {
                    logd("EVENT_LOAD_ALL_ADN_LIKE_DONE exception", ar.exception);
                }
                notifyWaiters(waiters, ar);
                if (this.mAdnLikeFiles.get(28474) != null) {
                    setAdnCount(((ArrayList) this.mAdnLikeFiles.get(28474)).size());
                    return;
                }
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                efid = msg.arg1;
                int index = msg.arg2;
                AdnRecord adn = ar.userObj;
                if (ar.exception == null) {
                    if (adn != null) {
                        logd("EVENT_UPDATE_ADN_DONE , efid = " + efid + ", index = " + index + " , adn.efid = " + adn.mEfid + " adn.index  = " + adn.mRecordNumber);
                        adn.mRecordNumber = index;
                        if (adn.mEfid <= 0) {
                            adn.mEfid = efid;
                        }
                    }
                    if (this.mAdnLikeFiles.get(efid) != null) {
                        ((ArrayList) this.mAdnLikeFiles.get(efid)).set(index - 1, adn);
                    }
                    this.mUsimPhoneBookManager.invalidateCache();
                }
                Message response = (Message) this.mUserWriteResponse.get(efid);
                this.mUserWriteResponse.delete(efid);
                if (response != null) {
                    AsyncResult.forMessage(response, adn, ar.exception);
                    response.sendToTarget();
                    return;
                }
                return;
            default:
                return;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void updateUsimAdnByIndex(int efid, AdnRecord newAdn, int recordIndex, String pin2, Message response) {
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:" + efid);
        } else if (newAdn.mAlphaTag.length() > MAX_PHB_NAME_LENGTH) {
            sendErrorResponse(response, "the input length of alphaTag is too long: " + newAdn.mAlphaTag, 1002);
        } else {
            int num_length = newAdn.mNumber.length();
            if (newAdn.mNumber.indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > MAX_PHB_NUMBER_LENGTH) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.mNumber, 1001);
                return;
            }
            ArrayList oldAdnList;
            if (efid == 20272) {
                try {
                    oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
                } catch (NullPointerException e) {
                    oldAdnList = null;
                }
            } else {
                oldAdnList = getRecordsIfLoaded(efid);
            }
            if (oldAdnList == null) {
                sendErrorResponse(response, "Adn list not exist for EF:" + efid);
                return;
            }
            int index = recordIndex;
            if (efid == 20272) {
                AdnRecord foundAdn = (AdnRecord) oldAdnList.get(recordIndex - 1);
                newAdn.mEfid = foundAdn.mEfid;
                newAdn.mExtRecord = foundAdn.mExtRecord;
                newAdn.mRecordNumber = foundAdn.mRecordNumber;
            }
            if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                sendErrorResponse(response, "Have pending update for EF:" + efid);
                return;
            }
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(newAdn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, newAdn));
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getAdnCount() {
        return this.mAdncountofIcc;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void setAdnCount(int count) {
        this.mAdncountofIcc = count;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public int getUsimAdnCount() {
        return this.mUsimPhoneBookManager.getUsimAdnCount();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void logd(String msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[AdnRecordCache] " + msg);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void logd(String msg, Throwable tr) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[AdnRecordCache] " + msg, tr);
        }
    }
}
