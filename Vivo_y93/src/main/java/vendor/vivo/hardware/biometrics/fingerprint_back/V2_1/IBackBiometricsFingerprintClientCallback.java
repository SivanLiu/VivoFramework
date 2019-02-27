package vendor.vivo.hardware.biometrics.fingerprint_back.V2_1;

import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IBackBiometricsFingerprintClientCallback extends IBiometricsFingerprintClientCallback {
    public static final String kInterfaceName = "vendor.vivo.hardware.biometrics.fingerprint_back@2.1::IBackBiometricsFingerprintClientCallback";

    public static final class Proxy implements IBackBiometricsFingerprintClientCallback {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.vivo.hardware.biometrics.fingerprint_back@2.1::IBackBiometricsFingerprintClientCallback]@Proxy";
            }
        }

        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(fingerId);
            _hidl_request.writeInt32(groupId);
            _hidl_request.writeInt32(remaining);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(acquiredInfo);
            _hidl_request.writeInt32(vendorCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(fingerId);
            _hidl_request.writeInt32(groupId);
            _hidl_request.writeInt8Vector(token);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onError(long deviceId, int error, int vendorCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(error);
            _hidl_request.writeInt32(vendorCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(fingerId);
            _hidl_request.writeInt32(groupId);
            _hidl_request.writeInt32(remaining);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onEnumerate(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(fingerId);
            _hidl_request.writeInt32(groupId);
            _hidl_request.writeInt32(remaining);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onCommandResult(long deviceId, int command, int result, int extras) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBackBiometricsFingerprintClientCallback.kInterfaceName);
            _hidl_request.writeInt64(deviceId);
            _hidl_request.writeInt32(command);
            _hidl_request.writeInt32(result);
            _hidl_request.writeInt32(extras);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    Object _hidl_vec_element = new byte[32];
                    long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                    for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                        _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                        _hidl_array_offset_1++;
                    }
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IBackBiometricsFingerprintClientCallback {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IBackBiometricsFingerprintClientCallback.kInterfaceName, "android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback", "android.hidl.base@1.0::IBase"}));
        }

        public final String interfaceDescriptor() {
            return IBackBiometricsFingerprintClientCallback.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 116, (byte) -24, (byte) 49, (byte) 95, (byte) 81, (byte) -36, (byte) -70, (byte) 107, (byte) 68, (byte) -2, (byte) -125, (byte) 30, (byte) 71, (byte) 75, (byte) -74, (byte) 36, (byte) -93, (byte) -49, (byte) -75, (byte) 78, (byte) 71, (byte) -48, (byte) -80, (byte) 54, (byte) 28, (byte) 107, (byte) 84, (byte) -87, (byte) 86, (byte) 45, (byte) -116, (byte) -96}, new byte[]{(byte) -86, (byte) -69, (byte) 92, (byte) 60, (byte) 88, (byte) 85, (byte) -110, (byte) -41, (byte) 30, (byte) -27, (byte) 123, (byte) 119, (byte) 41, (byte) -116, (byte) 20, (byte) -103, (byte) 61, (byte) 119, (byte) -111, (byte) 77, (byte) -34, (byte) -86, (byte) 100, (byte) -78, (byte) -59, (byte) 16, (byte) -102, (byte) 96, (byte) 43, (byte) 2, (byte) -22, (byte) 71}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = -1;
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            SystemProperties.reportSyspropChanged();
        }

        public final boolean unlinkToDeath(DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IBackBiometricsFingerprintClientCallback.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            switch (_hidl_code) {
                case FingerprintMsgType.ACQUIRED /*1*/:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    onEnrollResult(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 2:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    onAcquired(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case FingerprintMsgType.TEMPLATE_ENROLLING /*3*/:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    long deviceId = _hidl_request.readInt64();
                    int fingerId = _hidl_request.readInt32();
                    onAuthenticated(deviceId, fingerId, _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case FingerprintMsgType.TEMPLATE_REMOVED /*4*/:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    onError(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case FingerprintMsgType.AUTHENTICATED /*5*/:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    onRemoved(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case FingerprintMsgType.TEMPLATE_ENUMERATING /*6*/:
                    _hidl_request.enforceInterface("android.hardware.biometrics.fingerprint@2.1::IBiometricsFingerprintClientCallback");
                    onEnumerate(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case FingerprintMsgType.ENGINEER_COMMAND_RESULT /*7*/:
                    _hidl_request.enforceInterface(IBackBiometricsFingerprintClientCallback.kInterfaceName);
                    onCommandResult(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 256067662:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    _hidl_blob.putInt32(8, _hidl_vec_size);
                    _hidl_blob.putBool(12, false);
                    HwBlob hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    _hidl_blob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(_hidl_blob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    IHwBinder asBinder();

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void onCommandResult(long j, int i, int i2, int i3) throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IBackBiometricsFingerprintClientCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IBackBiometricsFingerprintClientCallback)) {
            return (IBackBiometricsFingerprintClientCallback) iface;
        }
        IBackBiometricsFingerprintClientCallback proxy = new Proxy(binder);
        try {
            for (String descriptor : proxy.interfaceChain()) {
                if (descriptor.equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IBackBiometricsFingerprintClientCallback castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IBackBiometricsFingerprintClientCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IBackBiometricsFingerprintClientCallback getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
