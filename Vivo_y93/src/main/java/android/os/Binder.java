package android.os;

import android.os.IBinder.DeathRecipient;
import android.util.ExceptionUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FunctionalUtils.ThrowingRunnable;
import com.android.internal.util.FunctionalUtils.ThrowingSupplier;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import libcore.io.IoUtils;

public class Binder implements IBinder {
    public static final boolean CHECK_PARCEL_SIZE = true;
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    public static boolean LOG_RUNTIME_EXCEPTION = false;
    static final String TAG = "Binder";
    private static BinderCallback sBinderCallback = null;
    private static volatile String sDumpDisabled = null;
    private static volatile boolean sTracingEnabled = false;
    private static volatile TransactionTracker sTransactionTracker = null;
    static volatile boolean sWarnOnBlocking = false;
    private String mDescriptor;
    private long mObject;
    private IInterface mOwner;

    public interface BinderCallback {
        void onBinderCallBegin(Thread thread, int i);

        void onBinderCallEnd(Thread thread);
    }

    public static final native void blockUntilThreadAvailable();

    public static final native long clearCallingIdentity();

    private final native void destroyBinder();

    public static final native void flushPendingCommands();

    public static final native int getCallingPid();

    public static final native int getCallingUid();

    public static final native int getThreadStrictModePolicy();

    private final native void init();

    public static final native void joinThreadPool();

    public static final native void restoreCallingIdentity(long j);

    public static final native void setThreadStrictModePolicy(int i);

    public static void enableTracing() {
        sTracingEnabled = true;
    }

    public static void disableTracing() {
        sTracingEnabled = false;
    }

    public static boolean isTracingEnabled() {
        return sTracingEnabled;
    }

    public static synchronized TransactionTracker getTransactionTracker() {
        TransactionTracker transactionTracker;
        synchronized (Binder.class) {
            if (sTransactionTracker == null) {
                sTransactionTracker = new TransactionTracker();
            }
            transactionTracker = sTransactionTracker;
        }
        return transactionTracker;
    }

    public static void setWarnOnBlocking(boolean warnOnBlocking) {
        sWarnOnBlocking = warnOnBlocking;
    }

    public static IBinder allowBlocking(IBinder binder) {
        try {
            if (binder instanceof BinderProxy) {
                ((BinderProxy) binder).mWarnOnBlocking = false;
            } else if (binder != null && binder.queryLocalInterface(binder.getInterfaceDescriptor()) == null) {
                Log.w(TAG, "Unable to allow blocking on interface " + binder);
            }
        } catch (RemoteException e) {
        }
        return binder;
    }

    public static void copyAllowBlocking(IBinder fromBinder, IBinder toBinder) {
        if ((fromBinder instanceof BinderProxy) && (toBinder instanceof BinderProxy)) {
            ((BinderProxy) toBinder).mWarnOnBlocking = ((BinderProxy) fromBinder).mWarnOnBlocking;
        }
    }

    public static final UserHandle getCallingUserHandle() {
        return UserHandle.of(UserHandle.getUserId(getCallingUid()));
    }

    public static final void withCleanCallingIdentity(ThrowingRunnable action) {
        long callingIdentity = clearCallingIdentity();
        try {
            action.run();
        } catch (Throwable throwable) {
            Throwable throwableToPropagate = throwable;
            if (throwable != null) {
                RuntimeException propagate = ExceptionUtils.propagate(throwable);
            }
        } finally {
            restoreCallingIdentity(callingIdentity);
        }
    }

    public static final <T> T withCleanCallingIdentity(ThrowingSupplier<T> action) {
        T t = null;
        long callingIdentity = clearCallingIdentity();
        try {
            t = action.get();
            return t;
        } catch (Throwable throwable) {
            Throwable throwableToPropagate = throwable;
            if (throwable == null) {
                return t;
            }
            RuntimeException propagate = ExceptionUtils.propagate(throwable);
        } finally {
            restoreCallingIdentity(callingIdentity);
        }
    }

    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface;
    }

    public Binder() {
        init();
    }

    public void attachInterface(IInterface owner, String descriptor) {
        this.mOwner = owner;
        this.mDescriptor = descriptor;
    }

    public String getInterfaceDescriptor() {
        return this.mDescriptor;
    }

    public boolean pingBinder() {
        return true;
    }

    public boolean isBinderAlive() {
        return true;
    }

    public IInterface queryLocalInterface(String descriptor) {
        if (this.mDescriptor.equals(descriptor)) {
            return this.mOwner;
        }
        return null;
    }

    public static void setDumpDisabled(String msg) {
        sDumpDisabled = msg;
    }

    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        String[] args;
        if (code == IBinder.INTERFACE_TRANSACTION) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code == IBinder.DUMP_TRANSACTION) {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            args = data.readStringArray();
            if (fd != null) {
                try {
                    dump(fd.getFileDescriptor(), args);
                } finally {
                    IoUtils.closeQuietly(fd);
                }
            }
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        } else if (code != IBinder.SHELL_COMMAND_TRANSACTION) {
            return false;
        } else {
            ParcelFileDescriptor in = data.readFileDescriptor();
            ParcelFileDescriptor out = data.readFileDescriptor();
            ParcelFileDescriptor err = data.readFileDescriptor();
            args = data.readStringArray();
            ShellCallback shellCallback = (ShellCallback) ShellCallback.CREATOR.createFromParcel(data);
            ResultReceiver resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
            if (out != null) {
                FileDescriptor fileDescriptor;
                if (in != null) {
                    try {
                        fileDescriptor = in.getFileDescriptor();
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(err);
                        if (reply != null) {
                            reply.writeNoException();
                        }
                        StrictMode.clearGatheredViolations();
                    }
                }
                fileDescriptor = null;
                shellCommand(fileDescriptor, out.getFileDescriptor(), err != null ? err.getFileDescriptor() : out.getFileDescriptor(), args, shellCallback, resultReceiver);
            }
            IoUtils.closeQuietly(in);
            IoUtils.closeQuietly(out);
            IoUtils.closeQuietly(err);
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        }
    }

    public void dump(FileDescriptor fd, String[] args) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        try {
            doDump(fd, pw, args);
        } finally {
            pw.flush();
        }
    }

    void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (sDumpDisabled == null) {
            try {
                dump(fd, pw, args);
                return;
            } catch (SecurityException e) {
                pw.println("Security exception: " + e.getMessage());
                throw e;
            } catch (Throwable e2) {
                pw.println();
                pw.println("Exception occurred while dumping:");
                e2.printStackTrace(pw);
                return;
            }
        }
        pw.println(sDumpDisabled);
    }

    public void dumpAsync(FileDescriptor fd, String[] args) {
        final PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        final FileDescriptor fileDescriptor = fd;
        final String[] strArr = args;
        new Thread("Binder.dumpAsync") {
            public void run() {
                try {
                    Binder.this.dump(fileDescriptor, pw, strArr);
                } finally {
                    pw.flush();
                }
            }
        }.start();
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }

    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        onShellCommand(in, out, err, args, callback, resultReceiver);
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        if (err == null) {
            err = out;
        }
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(err));
        pw.println("No shell command implementation.");
        pw.flush();
        resultReceiver.send(0, null);
    }

    public final boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (data != null) {
            data.setDataPosition(0);
        }
        boolean r = onTransact(code, data, reply, flags);
        if (reply != null) {
            reply.setDataPosition(0);
        }
        return r;
    }

    public void linkToDeath(DeathRecipient recipient, int flags) {
    }

    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return true;
    }

    protected void finalize() throws Throwable {
        try {
            destroyBinder();
        } finally {
            super.finalize();
        }
    }

    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
        if (parcel.dataSize() >= 819200) {
            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append(": on ");
            sb.append(obj);
            sb.append(" calling ");
            sb.append(code);
            sb.append(" size ");
            sb.append(parcel.dataSize());
            sb.append(" (data: ");
            parcel.setDataPosition(0);
            sb.append(parcel.readInt());
            sb.append(", ");
            sb.append(parcel.readInt());
            sb.append(", ");
            sb.append(parcel.readInt());
            sb.append(")");
            Slog.wtfStack(TAG, sb.toString());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0064 A:{Splitter: B:2:0x0014, ExcHandler: android.os.RemoteException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:13:0x0064, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0067, code:
            if (LOG_RUNTIME_EXCEPTION != false) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:17:0x0069, code:
            android.util.Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", r4);
     */
    /* JADX WARNING: Missing block: B:19:0x0074, code:
            if ((r18 & 1) != 0) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:21:0x0078, code:
            if ((r4 instanceof android.os.RemoteException) != false) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:22:0x007a, code:
            android.util.Log.w(TAG, "Binder call failed.", r4);
     */
    /* JADX WARNING: Missing block: B:23:0x0083, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:24:0x0084, code:
            if (r8 != false) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:25:0x0086, code:
            android.os.Trace.traceEnd(1);
     */
    /* JADX WARNING: Missing block: B:26:0x008b, code:
            if (r2 != null) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:27:0x008d, code:
            r2.onBinderCallEnd(r7);
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            android.util.Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", r4);
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r5.setDataPosition(0);
            r5.writeException(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        BinderCallback callback = sBinderCallback;
        Thread thread = Thread.currentThread();
        boolean tracingEnabled = isTracingEnabled();
        if (tracingEnabled) {
            try {
                Trace.traceBegin(1, getClass().getName() + ":" + code);
            } catch (Exception e) {
            } catch (Throwable th) {
                if (tracingEnabled) {
                    Trace.traceEnd(1);
                }
                if (callback != null) {
                    callback.onBinderCallEnd(thread);
                }
            }
        }
        if (callback != null) {
            callback.onBinderCallBegin(thread, getCallingPid());
        }
        boolean res = onTransact(code, data, reply, flags);
        if (tracingEnabled) {
            Trace.traceEnd(1);
        }
        if (callback != null) {
            callback.onBinderCallEnd(thread);
        }
        checkParcel(this, code, reply, "Unreasonably large binder reply buffer");
        reply.recycle();
        data.recycle();
        StrictMode.clearGatheredViolations();
        return res;
    }

    public static void setBinderCallback(BinderCallback callback) {
        sBinderCallback = callback;
    }
}
