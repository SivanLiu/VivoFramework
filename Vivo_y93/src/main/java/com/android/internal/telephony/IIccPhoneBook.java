package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public interface IIccPhoneBook extends IInterface {

    public static abstract class Stub extends Binder implements IIccPhoneBook {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IIccPhoneBook";
        static final int TRANSACTION_get3gTransferTo2gFlag = 24;
        static final int TRANSACTION_getAdnCount = 12;
        static final int TRANSACTION_getAdnCountUsingSubId = 13;
        static final int TRANSACTION_getAdnRecordsCapacity = 10;
        static final int TRANSACTION_getAdnRecordsCapacityForSubscriber = 11;
        static final int TRANSACTION_getAdnRecordsFromQmiPbm = 26;
        static final int TRANSACTION_getAdnRecordsInEf = 1;
        static final int TRANSACTION_getAdnRecordsInEfForSubscriber = 2;
        static final int TRANSACTION_getAdnRecordsSize = 8;
        static final int TRANSACTION_getAdnRecordsSizeForSubscriber = 9;
        static final int TRANSACTION_getAnrCount = 14;
        static final int TRANSACTION_getAnrCountUsingSubId = 15;
        static final int TRANSACTION_getEmailCount = 16;
        static final int TRANSACTION_getEmailCountUsingSubId = 17;
        static final int TRANSACTION_getQmiPbmCapabilities = 25;
        static final int TRANSACTION_getSpareAnrCount = 18;
        static final int TRANSACTION_getSpareAnrCountUsingSubId = 19;
        static final int TRANSACTION_getSpareEmailCount = 20;
        static final int TRANSACTION_getSpareEmailCountUsingSubId = 21;
        static final int TRANSACTION_updateAdnRecordsInEfByIndex = 6;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber = 7;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexWithErrorUsingSubId = 23;
        static final int TRANSACTION_updateAdnRecordsInEfBySearch = 3;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber = 4;
        static final int TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchUsingSubId = 5;
        static final int TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId = 22;
        static final int TRANSACTION_updateQmiPbmRecord = 27;

        private static class Proxy implements IIccPhoneBook {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(oldTag);
                    _data.writeString(oldPhoneNumber);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeString(pin2);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(oldTag);
                    _data.writeString(oldPhoneNumber);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeString(pin2);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeInt(index);
                    _data.writeString(pin2);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeInt(index);
                    _data.writeString(pin2);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAdnRecordsSize(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAdnRecordsCapacity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdnCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdnCountUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAnrCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAnrCountUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEmailCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEmailCountUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareAnrCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareAnrCountUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareEmailCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareEmailCountUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId(int efid, ContentValues values, String pin2, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    _data.writeInt(subId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateAdnRecordsInEfByIndexWithErrorUsingSubId(int efid, String newTag, String newPhoneNumber, String[] anrNumbers, String[] emails, int index, String pin2, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeStringArray(anrNumbers);
                    _data.writeStringArray(emails);
                    _data.writeInt(index);
                    _data.writeString(pin2);
                    _data.writeInt(subId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean get3gTransferTo2gFlag(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getQmiPbmCapabilities(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AdnRecord> getAdnRecordsFromQmiPbm(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateQmiPbmRecord(int subId, int index, String name, String number, String[] emails, String[] anrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(index);
                    _data.writeString(name);
                    _data.writeString(number);
                    _data.writeStringArray(emails);
                    _data.writeStringArray(anrs);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIccPhoneBook asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIccPhoneBook)) {
                return new Proxy(obj);
            }
            return (IIccPhoneBook) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<AdnRecord> _result;
            boolean _result2;
            int _arg0;
            int[] _result3;
            int _result4;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEf(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 5:
                    ContentValues _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _result2 = updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(_arg0, _arg1, _arg2, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsSize(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsCapacity();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsCapacityForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnCount();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnCountUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAnrCount();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAnrCountUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEmailCount();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEmailCountUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareAnrCount();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareAnrCountUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareEmailCount();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareEmailCountUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 22:
                    ContentValues _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg12 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    _result4 = updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId(_arg0, _arg12, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = updateAdnRecordsInEfByIndexWithErrorUsingSubId(data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get3gTransferTo2gFlag(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getQmiPbmCapabilities(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsFromQmiPbm(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = updateQmiPbmRecord(data.readInt(), data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean get3gTransferTo2gFlag(int i) throws RemoteException;

    int getAdnCount() throws RemoteException;

    int getAdnCountUsingSubId(int i) throws RemoteException;

    int[] getAdnRecordsCapacity() throws RemoteException;

    int[] getAdnRecordsCapacityForSubscriber(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsFromQmiPbm(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEf(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEfForSubscriber(int i, int i2) throws RemoteException;

    int[] getAdnRecordsSize(int i) throws RemoteException;

    int[] getAdnRecordsSizeForSubscriber(int i, int i2) throws RemoteException;

    int getAnrCount() throws RemoteException;

    int getAnrCountUsingSubId(int i) throws RemoteException;

    int getEmailCount() throws RemoteException;

    int getEmailCountUsingSubId(int i) throws RemoteException;

    int[] getQmiPbmCapabilities(int i) throws RemoteException;

    int getSpareAnrCount() throws RemoteException;

    int getSpareAnrCountUsingSubId(int i) throws RemoteException;

    int getSpareEmailCount() throws RemoteException;

    int getSpareEmailCountUsingSubId(int i) throws RemoteException;

    boolean updateAdnRecordsInEfByIndex(int i, String str, String str2, int i2, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfByIndexForSubscriber(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    int updateAdnRecordsInEfByIndexWithErrorUsingSubId(int i, String str, String str2, String[] strArr, String[] strArr2, int i2, String str3, int i3) throws RemoteException;

    boolean updateAdnRecordsInEfBySearch(int i, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsInEfBySearchForSubscriber(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(int i, int i2, ContentValues contentValues, String str) throws RemoteException;

    int updateAdnRecordsWithContentValuesInEfBySearchWithErrorUsingSubId(int i, ContentValues contentValues, String str, int i2) throws RemoteException;

    int updateQmiPbmRecord(int i, int i2, String str, String str2, String[] strArr, String[] strArr2) throws RemoteException;
}
