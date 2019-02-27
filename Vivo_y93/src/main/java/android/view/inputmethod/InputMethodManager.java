package android.view.inputmethod;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputConnectionWrapper;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodClient.Stub;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.internal.widget.EditableInputConnection;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class InputMethodManager {
    public static final int CONTROL_SHOW_INPUT = 65536;
    public static final int CONTROL_START_INITIAL = 256;
    public static final int CONTROL_WINDOW_FIRST = 4;
    public static final int CONTROL_WINDOW_IS_TEXT_EDITOR = 2;
    public static final int CONTROL_WINDOW_VIEW_HAS_FOCUS = 1;
    static boolean DEBUG = false;
    public static final int DISPATCH_HANDLED = 1;
    public static final int DISPATCH_IN_PROGRESS = -1;
    public static final int DISPATCH_NOT_HANDLED = 0;
    public static final int HIDE_IMPLICIT_ONLY = 1;
    public static final int HIDE_NOT_ALWAYS = 2;
    static final long INPUT_METHOD_NOT_RESPONDING_TIMEOUT = 2500;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final boolean IS_ENG = Build.TYPE.equals("eng");
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final boolean IS_LOG_CTRL_OPEN = "yes".equals(SystemProperties.get("persist.sys.log.ctrl", "no"));
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static final String KEY_VIVO_LOG_CTRL = "persist.sys.log.ctrl";
    static final int MSG_BIND = 2;
    static final int MSG_DUMP = 1;
    static final int MSG_FLUSH_INPUT_EVENT = 7;
    static final int MSG_REPORT_FULLSCREEN_MODE = 10;
    static final int MSG_SEND_INPUT_EVENT = 5;
    static final int MSG_SET_ACTIVE = 4;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 9;
    static final int MSG_TIMEOUT_INPUT_EVENT = 6;
    static final int MSG_UNBIND = 3;
    private static final int NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER = -1;
    static final String PENDING_EVENT_COUNTER = "aq:imm";
    private static final int REQUEST_UPDATE_CURSOR_ANCHOR_INFO_NONE = 0;
    public static final int RESULT_HIDDEN = 3;
    public static final int RESULT_SHOWN = 2;
    public static final int RESULT_UNCHANGED_HIDDEN = 1;
    public static final int RESULT_UNCHANGED_SHOWN = 0;
    public static final int SHOW_FORCED = 2;
    public static final int SHOW_IMPLICIT = 1;
    public static final int SHOW_IM_PICKER_MODE_AUTO = 0;
    public static final int SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES = 2;
    public static final int SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES = 1;
    static final String TAG = "InputMethodManager";
    static InputMethodManager sInstance;
    int DOCKED_BOTTOM;
    int DOCKED_INVALID;
    int DOCKED_LEFT;
    int DOCKED_RIGHT;
    int DOCKED_TOP;
    boolean mActive;
    int mBindSequence;
    final Stub mClient;
    CompletionInfo[] mCompletions;
    InputChannel mCurChannel;
    String mCurId;
    IInputMethodSession mCurMethod;
    View mCurRootView;
    ImeInputEventSender mCurSender;
    EditorInfo mCurrentTextBoxAttribute;
    private CursorAnchorInfo mCursorAnchorInfo;
    int mCursorCandEnd;
    int mCursorCandStart;
    Rect mCursorRect;
    int mCursorSelEnd;
    int mCursorSelStart;
    final InputConnection mDummyInputConnection;
    boolean mFullscreenMode;
    final H mH;
    boolean mHasBeenInactive;
    final IInputContext mIInputContext;
    private int mLastSentUserActionNotificationSequenceNumber;
    final Looper mMainLooper;
    View mNextServedView;
    private int mNextUserActionNotificationSequenceNumber;
    final Pool<PendingEvent> mPendingEventPool;
    final SparseArray<PendingEvent> mPendingEvents;
    private int mRequestUpdateCursorAnchorInfoMonitorMode;
    boolean mServedConnecting;
    ControlledInputConnectionWrapper mServedInputConnectionWrapper;
    View mServedView;
    final IInputMethodManager mService;
    Rect mTmpCursorRect;

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    private static class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        private final InputMethodManager mParentInputMethodManager;

        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn, InputMethodManager inputMethodManager) {
            super(mainLooper, conn);
            this.mParentInputMethodManager = inputMethodManager;
        }

        public boolean isActive() {
            return this.mParentInputMethodManager.mActive ? isFinished() ^ 1 : false;
        }

        void deactivate() {
            if (!isFinished()) {
                closeConnection();
            }
        }

        protected void onUserAction() {
            this.mParentInputMethodManager.notifyUserAction();
        }

        public String toString() {
            return "ControlledInputConnectionWrapper{connection=" + getInputConnection() + " finished=" + isFinished() + " mParentInputMethodManager.mActive=" + this.mParentInputMethodManager.mActive + "}";
        }
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper, null, true);
        }

        /* JADX WARNING: Missing block: B:34:0x00dd, code:
            return;
     */
        /* JADX WARNING: Missing block: B:61:0x0187, code:
            if (r15 == false) goto L_0x0195;
     */
        /* JADX WARNING: Missing block: B:62:0x0189, code:
            r17.this$0.startInputInner(6, null, 0, 0, 0);
     */
        /* JADX WARNING: Missing block: B:63:0x0195, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            int reason;
            boolean fullscreen;
            switch (msg.what) {
                case 1:
                    SomeArgs args = msg.obj;
                    try {
                        InputMethodManager.this.doDump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                    } catch (RuntimeException e) {
                        ((PrintWriter) args.arg2).println("Exception: " + e);
                    }
                    synchronized (args.arg4) {
                        ((CountDownLatch) args.arg4).countDown();
                    }
                    args.recycle();
                    return;
                case 2:
                    InputBindResult res = msg.obj;
                    if (InputMethodManager.DEBUG) {
                        Log.i(InputMethodManager.TAG, "handleMessage: MSG_BIND " + res.sequence + "," + res.id);
                    }
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence < 0 || InputMethodManager.this.mBindSequence != res.sequence) {
                            Log.w(InputMethodManager.TAG, "Ignoring onBind: cur seq=" + InputMethodManager.this.mBindSequence + ", given seq=" + res.sequence);
                            if (!(res.channel == null || res.channel == InputMethodManager.this.mCurChannel)) {
                                res.channel.dispose();
                                break;
                            }
                        }
                        InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
                        InputMethodManager.this.setInputChannelLocked(res.channel);
                        InputMethodManager.this.mCurMethod = res.method;
                        InputMethodManager.this.mCurId = res.id;
                        InputMethodManager.this.mBindSequence = res.sequence;
                        InputMethodManager.this.startInputInner(5, null, 0, 0, 0);
                        return;
                    }
                    break;
                case 3:
                    int sequence = msg.arg1;
                    reason = msg.arg2;
                    if (InputMethodManager.DEBUG) {
                        Log.i(InputMethodManager.TAG, "handleMessage: MSG_UNBIND " + sequence + " reason=" + InputMethodClient.getUnbindReason(reason));
                    }
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence == sequence) {
                            InputMethodManager.this.clearBindingLocked();
                            if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.isFocused()) {
                                InputMethodManager.this.mServedConnecting = true;
                            }
                            boolean startInput = InputMethodManager.this.mActive;
                            break;
                        }
                        return;
                    }
                case 4:
                    boolean active = msg.arg1 != 0;
                    fullscreen = msg.arg2 != 0;
                    if (InputMethodManager.DEBUG) {
                        Log.i(InputMethodManager.TAG, "handleMessage: MSG_SET_ACTIVE " + active + ", was " + InputMethodManager.this.mActive);
                    }
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mActive = active;
                        InputMethodManager.this.mFullscreenMode = fullscreen;
                        if (!active) {
                            InputMethodManager.this.mHasBeenInactive = true;
                            try {
                                InputMethodManager.this.mIInputContext.finishComposingText();
                            } catch (RemoteException e2) {
                            }
                        }
                        if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.hasWindowFocus() && InputMethodManager.this.checkFocusNoStartInput(InputMethodManager.this.mHasBeenInactive)) {
                            if (active) {
                                reason = 7;
                            } else {
                                reason = 8;
                            }
                            InputMethodManager.this.startInputInner(reason, null, 0, 0, 0);
                        }
                    }
                    return;
                case 5:
                    InputMethodManager.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                    return;
                case 6:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, false, true);
                    return;
                case 7:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, false, false);
                    return;
                case 9:
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mNextUserActionNotificationSequenceNumber = msg.arg1;
                    }
                    return;
                case 10:
                    fullscreen = msg.arg1 != 0;
                    InputConnection ic = null;
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mFullscreenMode = fullscreen;
                        if (InputMethodManager.this.mServedInputConnectionWrapper != null) {
                            ic = InputMethodManager.this.mServedInputConnectionWrapper.getInputConnection();
                        }
                    }
                    if (ic != null) {
                        ic.reportFullscreenMode(fullscreen);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class ImeInputEventSender extends InputEventSender {
        public ImeInputEventSender(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEventFinished(int seq, boolean handled) {
            InputMethodManager.this.finishedInputEvent(seq, handled, false);
        }
    }

    private final class PendingEvent implements Runnable {
        public FinishedInputEventCallback mCallback;
        public InputEvent mEvent;
        public boolean mHandled;
        public Handler mHandler;
        public String mInputMethodId;
        public Object mToken;

        /* synthetic */ PendingEvent(InputMethodManager this$0, PendingEvent -this1) {
            this();
        }

        private PendingEvent() {
        }

        public void recycle() {
            this.mEvent = null;
            this.mToken = null;
            this.mInputMethodId = null;
            this.mCallback = null;
            this.mHandler = null;
            this.mHandled = false;
        }

        public void run() {
            this.mCallback.onFinishedInputEvent(this.mToken, this.mHandled);
            synchronized (InputMethodManager.this.mH) {
                InputMethodManager.this.recyclePendingEventLocked(this);
            }
        }
    }

    static {
        boolean z;
        if (IS_LOG_CTRL_OPEN) {
            z = true;
        } else {
            z = IS_ENG;
        }
        DEBUG = z;
    }

    InputMethodManager(Looper looper) throws ServiceNotFoundException {
        this(IInputMethodManager.Stub.asInterface(ServiceManager.getServiceOrThrow("input_method")), looper);
    }

    InputMethodManager(IInputMethodManager service, Looper looper) {
        this.mActive = false;
        this.mHasBeenInactive = true;
        this.mTmpCursorRect = new Rect();
        this.mCursorRect = new Rect();
        this.mNextUserActionNotificationSequenceNumber = -1;
        this.mLastSentUserActionNotificationSequenceNumber = -1;
        this.mCursorAnchorInfo = null;
        this.mBindSequence = -1;
        this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
        this.mPendingEventPool = new SimplePool(20);
        this.mPendingEvents = new SparseArray(20);
        this.DOCKED_INVALID = -1;
        this.DOCKED_LEFT = 1;
        this.DOCKED_TOP = 2;
        this.DOCKED_RIGHT = 3;
        this.DOCKED_BOTTOM = 4;
        this.mClient = new Stub() {
            protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
                CountDownLatch latch = new CountDownLatch(1);
                SomeArgs sargs = SomeArgs.obtain();
                sargs.arg1 = fd;
                sargs.arg2 = fout;
                sargs.arg3 = args;
                sargs.arg4 = latch;
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(1, sargs));
                try {
                    if (!latch.await(5, TimeUnit.SECONDS)) {
                        fout.println("Timeout waiting for dump");
                    }
                } catch (InterruptedException e) {
                    fout.println("Interrupted waiting for dump");
                }
            }

            public void setUsingInputMethod(boolean state) {
            }

            public void onBindMethod(InputBindResult res) {
                InputMethodManager.this.mH.obtainMessage(2, res).sendToTarget();
            }

            public void onUnbindMethod(int sequence, int unbindReason) {
                InputMethodManager.this.mH.obtainMessage(3, sequence, unbindReason).sendToTarget();
            }

            public void setActive(boolean active, boolean fullscreen) {
                int i;
                int i2 = 1;
                H h = InputMethodManager.this.mH;
                if (active) {
                    i = 1;
                } else {
                    i = 0;
                }
                if (!fullscreen) {
                    i2 = 0;
                }
                h.obtainMessage(4, i, i2).sendToTarget();
            }

            public void setUserActionNotificationSequenceNumber(int sequenceNumber) {
                InputMethodManager.this.mH.obtainMessage(9, sequenceNumber, 0).sendToTarget();
            }

            public void reportFullscreenMode(boolean fullscreen) {
                int i;
                H h = InputMethodManager.this.mH;
                if (fullscreen) {
                    i = 1;
                } else {
                    i = 0;
                }
                h.obtainMessage(10, i, 0).sendToTarget();
            }
        };
        this.mDummyInputConnection = new BaseInputConnection(this, false);
        this.mService = service;
        this.mMainLooper = looper;
        this.mH = new H(looper);
        this.mIInputContext = new ControlledInputConnectionWrapper(looper, this.mDummyInputConnection, this);
    }

    public static InputMethodManager getInstance() {
        InputMethodManager inputMethodManager;
        synchronized (InputMethodManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new InputMethodManager(Looper.getMainLooper());
                } catch (ServiceNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            inputMethodManager = sInstance;
        }
        return inputMethodManager;
    }

    public static InputMethodManager peekInstance() {
        return sInstance;
    }

    public IInputMethodClient getClient() {
        return this.mClient;
    }

    public IInputContext getInputContext() {
        return this.mIInputContext;
    }

    public List<InputMethodInfo> getInputMethodList() {
        try {
            return this.mService.getInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return this.mService.getEnabledInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
        String str = null;
        try {
            IInputMethodManager iInputMethodManager = this.mService;
            if (imi != null) {
                str = imi.getId();
            }
            return iInputMethodManager.getEnabledInputMethodSubtypeList(str, allowsImplicitlySelectedSubtypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
        try {
            this.mService.updateStatusIcon(imeToken, packageName, iconId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void hideStatusIcon(IBinder imeToken) {
        try {
            this.mService.updateStatusIcon(imeToken, null, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setImeWindowStatus(IBinder imeToken, IBinder startInputToken, int vis, int backDisposition) {
        try {
            this.mService.setImeWindowStatus(imeToken, startInputToken, vis, backDisposition);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        try {
            this.mService.registerSuggestionSpansForNotification(spans);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        try {
            this.mService.notifySuggestionPicked(span, originalString, index);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isFullscreenMode() {
        boolean z;
        synchronized (this.mH) {
            z = this.mFullscreenMode;
        }
        return z;
    }

    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        try {
            this.mService.reportFullscreenMode(token, fullscreen);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isActive(View view) {
        boolean z = false;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                z = true;
            }
        }
        return z;
    }

    public boolean isActive() {
        boolean z = false;
        checkFocus();
        synchronized (this.mH) {
            if (!(this.mServedView == null || this.mCurrentTextBoxAttribute == null)) {
                z = true;
            }
        }
        return z;
    }

    public boolean isAcceptingText() {
        checkFocus();
        if (this.mServedInputConnectionWrapper == null || this.mServedInputConnectionWrapper.getInputConnection() == null) {
            return false;
        }
        return true;
    }

    void clearBindingLocked() {
        if (DEBUG) {
            Log.v(TAG, "Clearing binding!");
        }
        clearConnectionLocked();
        setInputChannelLocked(null);
        this.mBindSequence = -1;
        this.mCurId = null;
        this.mCurMethod = null;
    }

    void setInputChannelLocked(InputChannel channel) {
        if (this.mCurChannel != channel) {
            if (this.mCurSender != null) {
                flushPendingEventsLocked();
                this.mCurSender.dispose();
                this.mCurSender = null;
            }
            if (this.mCurChannel != null) {
                this.mCurChannel.dispose();
            }
            this.mCurChannel = channel;
        }
    }

    void clearConnectionLocked() {
        this.mCurrentTextBoxAttribute = null;
        if (this.mServedInputConnectionWrapper != null) {
            this.mServedInputConnectionWrapper.deactivate();
            this.mServedInputConnectionWrapper = null;
        }
    }

    void finishInputLocked() {
        this.mNextServedView = null;
        if (this.mServedView != null) {
            if (DEBUG) {
                Log.v(TAG, "FINISH INPUT: mServedView=" + dumpViewInfo(this.mServedView));
            }
            if (this.mCurrentTextBoxAttribute != null) {
                try {
                    this.mService.finishInput(this.mClient);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            this.mServedView = null;
            this.mCompletions = null;
            this.mServedConnecting = false;
            clearConnectionLocked();
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0019, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void displayCompletions(View view, CompletionInfo[] completions) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) {
                this.mCompletions = completions;
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.displayCompletions(this.mCompletions);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0019, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateExtractedText(View view, int token, ExtractedText text) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.updateExtractedText(token, text);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public boolean showSoftInput(View view, int flags) {
        return showSoftInput(view, flags, null);
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) {
                try {
                    boolean showSoftInput = this.mService.showSoftInput(this.mClient, flags, resultReceiver);
                    return showSoftInput;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return false;
        }
    }

    @Deprecated
    public void showSoftInputUnchecked(int flags, ResultReceiver resultReceiver) {
        try {
            Log.w(TAG, "showSoftInputUnchecked() is a hidden method, which will be removed soon. If you are using android.support.v7.widget.SearchView, please update to version 26.0 or newer version.");
            this.mService.showSoftInput(this.mClient, flags, resultReceiver);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return hideSoftInputFromWindow(windowToken, flags, null);
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags, ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == null || this.mServedView.getWindowToken() != windowToken) {
                return false;
            }
            try {
                boolean hideSoftInput = this.mService.hideSoftInput(this.mClient, flags, resultReceiver);
                return hideSoftInput;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0010, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void toggleSoftInputFromWindow(IBinder windowToken, int showFlags, int hideFlags) {
        synchronized (this.mH) {
            if (this.mServedView == null || this.mServedView.getWindowToken() != windowToken) {
            } else if (this.mCurMethod != null) {
                try {
                    this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void toggleSoftInput(int showFlags, int hideFlags) {
        if (this.mCurMethod != null) {
            try {
                this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void restartInput(View view) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) {
                this.mServedConnecting = true;
                startInputInner(3, null, 0, 0, 0);
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x004f, code:
            r20 = r21.getHandler();
     */
    /* JADX WARNING: Missing block: B:15:0x0053, code:
            if (r20 != null) goto L_0x006a;
     */
    /* JADX WARNING: Missing block: B:17:0x0057, code:
            if (DEBUG == false) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:18:0x0059, code:
            android.util.Log.v(TAG, "ABORT input: no handler for view! Close current input.");
     */
    /* JADX WARNING: Missing block: B:19:0x0062, code:
            closeCurrentInput();
     */
    /* JADX WARNING: Missing block: B:20:0x0066, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:25:0x0072, code:
            if (r20.getLooper() == android.os.Looper.myLooper()) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:27:0x0076, code:
            if (DEBUG == false) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:28:0x0078, code:
            android.util.Log.v(TAG, "Starting input: reschedule to view thread");
     */
    /* JADX WARNING: Missing block: B:29:0x0081, code:
            r1 = r24;
            r20.post(new android.view.inputmethod.InputMethodManager.AnonymousClass2(r23));
     */
    /* JADX WARNING: Missing block: B:30:0x0090, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:31:0x0091, code:
            r9 = new android.view.inputmethod.EditorInfo();
            r9.packageName = r21.getContext().getOpPackageName();
            r9.fieldId = r21.getId();
            r15 = r21.onCreateInputConnection(r9);
            r12 = getDockSide();
            r19 = new float[4];
            r19 = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
     */
    /* JADX WARNING: Missing block: B:32:0x00bc, code:
            if (r21.getRootView() == null) goto L_0x0134;
     */
    /* JADX WARNING: Missing block: B:34:0x00c2, code:
            if (r12 == r23.DOCKED_INVALID) goto L_0x0134;
     */
    /* JADX WARNING: Missing block: B:36:0x00ca, code:
            if ((isExport() ^ 1) == 0) goto L_0x0134;
     */
    /* JADX WARNING: Missing block: B:37:0x00cc, code:
            r17 = new android.graphics.Rect();
            r21.getRootView().getWindowVisibleDisplayFrame(r17);
            android.util.Log.i(TAG, "editor info set window rect : " + r17);
     */
    /* JADX WARNING: Missing block: B:38:0x00f8, code:
            if (r9.extras != null) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:39:0x00fa, code:
            r9.extras = new android.os.Bundle();
     */
    /* JADX WARNING: Missing block: B:40:0x0101, code:
            r9.extras.putFloatArray("vivo_window_rect", new float[]{(float) r17.left, (float) r17.top, (float) r17.right, (float) r17.bottom});
            r9.extras.putInt("vivo_dock_size", r12);
     */
    /* JADX WARNING: Missing block: B:42:0x0136, code:
            if (DEBUG == false) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:43:0x0138, code:
            android.util.Log.v(TAG, "Starting input: tba=" + r9 + " ic=" + r15);
     */
    /* JADX WARNING: Missing block: B:44:0x015d, code:
            r22 = r23.mH;
     */
    /* JADX WARNING: Missing block: B:45:0x0163, code:
            monitor-enter(r22);
     */
    /* JADX WARNING: Missing block: B:48:0x016a, code:
            if (r23.mServedView != r21) goto L_0x0174;
     */
    /* JADX WARNING: Missing block: B:50:0x0172, code:
            if ((r23.mServedConnecting ^ 1) == 0) goto L_0x01bb;
     */
    /* JADX WARNING: Missing block: B:52:0x0176, code:
            if (DEBUG == false) goto L_0x01b8;
     */
    /* JADX WARNING: Missing block: B:53:0x0178, code:
            android.util.Log.v(TAG, "Starting input: finished by someone else. view=" + dumpViewInfo(r21) + " mServedView=" + dumpViewInfo(r23.mServedView) + " mServedConnecting=" + r23.mServedConnecting);
     */
    /* JADX WARNING: Missing block: B:55:0x01b9, code:
            monitor-exit(r22);
     */
    /* JADX WARNING: Missing block: B:56:0x01ba, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:59:0x01bf, code:
            if (r23.mCurrentTextBoxAttribute != null) goto L_0x01c7;
     */
    /* JADX WARNING: Missing block: B:60:0x01c1, code:
            r26 = r26 | 256;
     */
    /* JADX WARNING: Missing block: B:61:0x01c7, code:
            r23.mCurrentTextBoxAttribute = r9;
            r23.mServedConnecting = false;
     */
    /* JADX WARNING: Missing block: B:62:0x01d4, code:
            if (r23.mServedInputConnectionWrapper == null) goto L_0x01e2;
     */
    /* JADX WARNING: Missing block: B:63:0x01d6, code:
            r23.mServedInputConnectionWrapper.deactivate();
            r23.mServedInputConnectionWrapper = null;
     */
    /* JADX WARNING: Missing block: B:64:0x01e2, code:
            if (r15 == null) goto L_0x0300;
     */
    /* JADX WARNING: Missing block: B:65:0x01e4, code:
            r23.mCursorSelStart = r9.initialSelStart;
            r23.mCursorSelEnd = r9.initialSelEnd;
            r23.mCursorCandStart = -1;
            r23.mCursorCandEnd = -1;
            r23.mCursorRect.setEmpty();
            r23.mCursorAnchorInfo = null;
            r11 = android.view.inputmethod.InputConnectionInspector.getMissingMethodFlags(r15);
     */
    /* JADX WARNING: Missing block: B:66:0x020c, code:
            if ((r11 & 32) == 0) goto L_0x02e9;
     */
    /* JADX WARNING: Missing block: B:67:0x020e, code:
            r16 = null;
     */
    /* JADX WARNING: Missing block: B:69:0x0212, code:
            if (r16 == null) goto L_0x02fa;
     */
    /* JADX WARNING: Missing block: B:70:0x0214, code:
            r2 = r16.getLooper();
     */
    /* JADX WARNING: Missing block: B:71:0x0218, code:
            r10 = new android.view.inputmethod.InputMethodManager.ControlledInputConnectionWrapper(r2, r15, r23);
     */
    /* JADX WARNING: Missing block: B:72:0x021d, code:
            r23.mServedInputConnectionWrapper = r10;
     */
    /* JADX WARNING: Missing block: B:75:0x0223, code:
            if (DEBUG == false) goto L_0x0268;
     */
    /* JADX WARNING: Missing block: B:76:0x0225, code:
            android.util.Log.v(TAG, "START INPUT: view=" + dumpViewInfo(r21) + " ic=" + r15 + " tba=" + r9 + " controlFlags=#" + java.lang.Integer.toHexString(r26));
     */
    /* JADX WARNING: Missing block: B:77:0x0268, code:
            r18 = r23.mService.startInputOrWindowGainedFocus(r24, r23.mClient, r25, r26, r27, r28, r9, r10, r11);
     */
    /* JADX WARNING: Missing block: B:78:0x0280, code:
            if (DEBUG == false) goto L_0x029e;
     */
    /* JADX WARNING: Missing block: B:79:0x0282, code:
            android.util.Log.v(TAG, "Starting input: Bind result=" + r18);
     */
    /* JADX WARNING: Missing block: B:80:0x029e, code:
            if (r18 == null) goto L_0x0331;
     */
    /* JADX WARNING: Missing block: B:82:0x02a4, code:
            if (r18.id == null) goto L_0x0304;
     */
    /* JADX WARNING: Missing block: B:83:0x02a6, code:
            setInputChannelLocked(r18.channel);
            r23.mBindSequence = r18.sequence;
            r23.mCurMethod = r18.method;
            r23.mCurId = r18.id;
            r23.mNextUserActionNotificationSequenceNumber = r18.userActionNotificationSequenceNumber;
     */
    /* JADX WARNING: Missing block: B:85:0x02d3, code:
            if (r23.mCurMethod == null) goto L_0x02e6;
     */
    /* JADX WARNING: Missing block: B:87:0x02d9, code:
            if (r23.mCompletions == null) goto L_0x02e6;
     */
    /* JADX WARNING: Missing block: B:89:?, code:
            r23.mCurMethod.displayCompletions(r23.mCompletions);
     */
    /* JADX WARNING: Missing block: B:95:0x02eb, code:
            if ((r15 instanceof android.view.inputmethod.InputConnectionWrapper) == false) goto L_0x02f0;
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            r15.getHandler();
     */
    /* JADX WARNING: Missing block: B:99:?, code:
            r16 = r15.getHandler();
     */
    /* JADX WARNING: Missing block: B:103:0x02f9, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:105:?, code:
            r2 = r20.getLooper();
     */
    /* JADX WARNING: Missing block: B:106:0x0300, code:
            r10 = null;
            r11 = 0;
     */
    /* JADX WARNING: Missing block: B:109:0x0308, code:
            if (r18.channel == null) goto L_0x031b;
     */
    /* JADX WARNING: Missing block: B:111:0x0312, code:
            if (r18.channel == r23.mCurChannel) goto L_0x031b;
     */
    /* JADX WARNING: Missing block: B:112:0x0314, code:
            r18.channel.dispose();
     */
    /* JADX WARNING: Missing block: B:114:0x031f, code:
            if (r23.mCurMethod != null) goto L_0x02cf;
     */
    /* JADX WARNING: Missing block: B:116:0x0323, code:
            if (DEBUG == false) goto L_0x032e;
     */
    /* JADX WARNING: Missing block: B:117:0x0325, code:
            android.util.Log.v(TAG, "ABORT input: no input method!");
     */
    /* JADX WARNING: Missing block: B:119:0x032f, code:
            monitor-exit(r22);
     */
    /* JADX WARNING: Missing block: B:120:0x0330, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:123:0x0333, code:
            if (DEBUG == false) goto L_0x037c;
     */
    /* JADX WARNING: Missing block: B:124:0x0335, code:
            android.util.Log.w(TAG, "startInputOrWindowGainedFocus failed. Window focus may have already been lost. startInputReason=" + r24 + " ,mActive=" + r23.mActive + " ,win=" + r25 + " view=" + dumpViewInfo(r21));
     */
    /* JADX WARNING: Missing block: B:125:0x037c, code:
            r23.mHasBeenInactive = true;
     */
    /* JADX WARNING: Missing block: B:126:0x0383, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:128:?, code:
            android.util.Log.w(TAG, "IME died: " + r23.mCurId, r13);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean startInputInner(int startInputReason, IBinder windowGainingFocus, int controlFlags, int softInputMode, int windowFlags) {
        synchronized (this.mH) {
            View view = this.mServedView;
            if (DEBUG) {
                Log.v(TAG, "Starting input: view=" + dumpViewInfo(view) + " reason=" + InputMethodClient.getStartInputReason(startInputReason));
            }
            if (view == null) {
                if (DEBUG) {
                    Log.v(TAG, "ABORT input: no served view!");
                }
                return false;
            }
        }
        return true;
    }

    public void windowDismissed(IBinder appWindowToken) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == appWindowToken) {
                finishInputLocked();
            }
        }
    }

    public void focusIn(View view) {
        synchronized (this.mH) {
            focusInLocked(view);
        }
    }

    void focusInLocked(View view) {
        if (DEBUG) {
            Log.v(TAG, "focusIn: " + dumpViewInfo(view));
        }
        if (view != null && view.isTemporarilyDetached()) {
            if (DEBUG) {
                Log.v(TAG, "Temporarily detached view, ignoring");
            }
        } else if (this.mCurRootView != view.getRootView()) {
            if (DEBUG) {
                Log.v(TAG, "Not IME target window, ignoring");
            }
        } else {
            this.mNextServedView = view;
            scheduleCheckFocusLocked(view);
        }
    }

    public void focusOut(View view) {
        synchronized (this.mH) {
            if (DEBUG) {
                Log.v(TAG, "focusOut: view=" + dumpViewInfo(view) + " mServedView=" + dumpViewInfo(this.mServedView));
            }
            View view2 = this.mServedView;
        }
    }

    public void onViewDetachedFromWindow(View view) {
        synchronized (this.mH) {
            if (this.mServedView == view) {
                this.mNextServedView = null;
                scheduleCheckFocusLocked(view);
            }
        }
    }

    static void scheduleCheckFocusLocked(View view) {
        ViewRootImpl viewRootImpl = view.getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.dispatchCheckFocus();
        }
    }

    public void checkFocus() {
        if (checkFocusNoStartInput(false)) {
            startInputInner(4, null, 0, 0, 0);
        }
    }

    /* JADX WARNING: Missing block: B:30:0x008d, code:
            if (r0 == null) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:31:0x008f, code:
            r0.finishComposingText();
     */
    /* JADX WARNING: Missing block: B:32:0x0092, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkFocusNoStartInput(boolean forceNewFocus) {
        if (this.mServedView == this.mNextServedView && (forceNewFocus ^ 1) != 0) {
            return false;
        }
        synchronized (this.mH) {
            if (this.mServedView != this.mNextServedView || (forceNewFocus ^ 1) == 0) {
                if (DEBUG) {
                    Log.v(TAG, "checkFocus: view=" + this.mServedView + " next=" + this.mNextServedView + " forceNewFocus=" + forceNewFocus + " package=" + (this.mServedView != null ? this.mServedView.getContext().getPackageName() : "<none>"));
                }
                if (this.mNextServedView == null) {
                    finishInputLocked();
                    closeCurrentInput();
                    return false;
                }
                ControlledInputConnectionWrapper ic = this.mServedInputConnectionWrapper;
                this.mServedView = this.mNextServedView;
                this.mCurrentTextBoxAttribute = null;
                this.mCompletions = null;
                this.mServedConnecting = true;
            } else {
                return false;
            }
        }
    }

    void closeCurrentInput() {
        try {
            this.mService.hideSoftInput(this.mClient, 2, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void onPostWindowFocus(View rootView, View focusedView, int softInputMode, boolean first, int windowFlags) {
        boolean forceNewFocus = false;
        synchronized (this.mH) {
            View view;
            if (DEBUG) {
                Log.v(TAG, "onWindowFocus: " + focusedView + " softInputMode=" + InputMethodClient.softInputModeToString(softInputMode) + " first=" + first + " flags=#" + Integer.toHexString(windowFlags));
            }
            if (this.mHasBeenInactive) {
                if (DEBUG) {
                    Log.v(TAG, "Has been inactive!  Starting fresh");
                }
                this.mHasBeenInactive = false;
                forceNewFocus = true;
            }
            if (focusedView != null) {
                view = focusedView;
            } else {
                view = rootView;
            }
            focusInLocked(view);
        }
        int controlFlags = 0;
        if (focusedView != null) {
            controlFlags = 1;
            if (focusedView.onCheckIsTextEditor()) {
                controlFlags = 1 | 2;
            }
        }
        if (first) {
            controlFlags |= 4;
        }
        if (checkFocusNoStartInput(forceNewFocus)) {
            if (startInputInner(1, rootView.getWindowToken(), controlFlags, softInputMode, windowFlags)) {
                return;
            }
        }
        synchronized (this.mH) {
            try {
                if (DEBUG) {
                    Log.v(TAG, "Reporting focus gain, without startInput");
                }
                this.mService.startInputOrWindowGainedFocus(2, this.mClient, rootView.getWindowToken(), controlFlags, softInputMode, windowFlags, null, null, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void onPreWindowFocus(View rootView, boolean hasWindowFocus) {
        synchronized (this.mH) {
            if (rootView == null) {
                this.mCurRootView = null;
            }
            if (hasWindowFocus) {
                this.mCurRootView = rootView;
            } else if (rootView == this.mCurRootView) {
                this.mCurRootView = null;
            } else if (DEBUG) {
                Log.v(TAG, "Ignoring onPreWindowFocus(). mCurRootView=" + this.mCurRootView + " rootView=" + rootView);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return;
     */
    /* JADX WARNING: Missing block: B:34:0x0074, code:
            if (r9.mCursorCandEnd == r14) goto L_0x006c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    if (this.mCursorSelStart == selStart && this.mCursorSelEnd == selEnd) {
                        if (this.mCursorCandStart == candidatesStart) {
                        }
                    }
                    if (DEBUG) {
                        Log.d(TAG, "updateSelection");
                    }
                    try {
                        if (DEBUG) {
                            Log.v(TAG, "SELECTION CHANGE: " + this.mCurMethod);
                        }
                        int oldSelStart = this.mCursorSelStart;
                        int oldSelEnd = this.mCursorSelEnd;
                        this.mCursorSelStart = selStart;
                        this.mCursorSelEnd = selEnd;
                        this.mCursorCandStart = candidatesStart;
                        this.mCursorCandEnd = candidatesEnd;
                        this.mCurMethod.updateSelection(oldSelStart, oldSelEnd, selStart, selEnd, candidatesStart, candidatesEnd);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void viewClicked(View view) {
        boolean focusChanged = this.mServedView != this.mNextServedView;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    try {
                        if (DEBUG) {
                            Log.v(TAG, "onViewClicked: " + focusChanged);
                        }
                        this.mCurMethod.viewClicked(focusChanged);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                }
            }
        }
    }

    @Deprecated
    public boolean isWatchingCursor(View view) {
        return false;
    }

    public boolean isCursorAnchorInfoEnabled() {
        boolean isMonitoring;
        synchronized (this.mH) {
            boolean isImmediate = (this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) != 0;
            isMonitoring = (this.mRequestUpdateCursorAnchorInfoMonitorMode & 2) != 0;
            if (isImmediate) {
                isMonitoring = true;
            }
        }
        return isMonitoring;
    }

    public void setUpdateCursorAnchorInfoMode(int flags) {
        synchronized (this.mH) {
            this.mRequestUpdateCursorAnchorInfoMonitorMode = flags;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public void updateCursor(View view, int left, int top, int right, int bottom) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    this.mTmpCursorRect.set(left, top, right, bottom);
                    if (!this.mCursorRect.equals(this.mTmpCursorRect)) {
                        if (DEBUG) {
                            Log.d(TAG, "updateCursor");
                        }
                        try {
                            if (DEBUG) {
                                Log.v(TAG, "CURSOR CHANGE: " + this.mCurMethod);
                            }
                            this.mCurMethod.updateCursor(this.mTmpCursorRect);
                            this.mCursorRect.set(this.mTmpCursorRect);
                        } catch (RemoteException e) {
                            Log.w(TAG, "IME died: " + this.mCurId, e);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:29:0x0057, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateCursorAnchorInfo(View view, CursorAnchorInfo cursorAnchorInfo) {
        if (view != null && cursorAnchorInfo != null) {
            checkFocus();
            synchronized (this.mH) {
                if ((this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) && this.mCurrentTextBoxAttribute != null) {
                    if (this.mCurMethod != null) {
                        if (((this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) != 0) || !Objects.equals(this.mCursorAnchorInfo, cursorAnchorInfo)) {
                            if (DEBUG) {
                                Log.v(TAG, "updateCursorAnchorInfo: " + cursorAnchorInfo);
                            }
                            try {
                                this.mCurMethod.updateCursorAnchorInfo(cursorAnchorInfo);
                                this.mCursorAnchorInfo = cursorAnchorInfo;
                                this.mRequestUpdateCursorAnchorInfoMonitorMode &= -2;
                            } catch (RemoteException e) {
                                Log.w(TAG, "IME died: " + this.mCurId, e);
                            }
                        } else if (DEBUG) {
                            Log.w(TAG, "Ignoring redundant updateCursorAnchorInfo: info=" + cursorAnchorInfo);
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && (this.mServedView.checkInputConnectionProxy(view) ^ 1) == 0)) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    try {
                        if (DEBUG) {
                            Log.v(TAG, "APP PRIVATE COMMAND " + action + ": " + data);
                        }
                        this.mCurMethod.appPrivateCommand(action, data);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                }
            }
        }
    }

    public void setInputMethod(IBinder token, String id) {
        try {
            this.mService.setInputMethod(token, id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        try {
            this.mService.setInputMethodAndSubtype(token, id, subtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.hideMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.showMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
        synchronized (this.mH) {
            if (this.mCurMethod != null) {
                if (event instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) event;
                    if (keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 63 && keyEvent.getRepeatCount() == 0) {
                        showInputMethodPickerLocked();
                        return 1;
                    }
                }
                if (DEBUG) {
                    Log.v(TAG, "DISPATCH INPUT EVENT: " + this.mCurMethod);
                }
                PendingEvent p = obtainPendingEventLocked(event, token, this.mCurId, callback, handler);
                if (this.mMainLooper.isCurrentThread()) {
                    int sendInputEventOnMainLooperLocked = sendInputEventOnMainLooperLocked(p);
                    return sendInputEventOnMainLooperLocked;
                }
                Message msg = this.mH.obtainMessage(5, p);
                msg.setAsynchronous(true);
                this.mH.sendMessage(msg);
                return -1;
            }
            return 0;
        }
    }

    public void dispatchKeyEventFromInputMethod(View targetView, KeyEvent event) {
        synchronized (this.mH) {
            ViewRootImpl viewRootImpl = targetView != null ? targetView.getViewRootImpl() : null;
            if (viewRootImpl == null && this.mServedView != null) {
                viewRootImpl = this.mServedView.getViewRootImpl();
            }
            if (viewRootImpl != null) {
                viewRootImpl.dispatchKeyFromIme(event);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0011, code:
            invokeFinishedInputEventCallback(r5, r0);
     */
    /* JADX WARNING: Missing block: B:13:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
        synchronized (this.mH) {
            int result = sendInputEventOnMainLooperLocked(p);
            if (result == -1) {
                return;
            }
            boolean handled = result == 1;
        }
    }

    int sendInputEventOnMainLooperLocked(PendingEvent p) {
        if (this.mCurChannel != null) {
            if (this.mCurSender == null) {
                this.mCurSender = new ImeInputEventSender(this.mCurChannel, this.mH.getLooper());
            }
            InputEvent event = p.mEvent;
            int seq = event.getSequenceNumber();
            if (this.mCurSender.sendInputEvent(seq, event)) {
                this.mPendingEvents.put(seq, p);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                Message msg = this.mH.obtainMessage(6, seq, 0, p);
                msg.setAsynchronous(true);
                this.mH.sendMessageDelayed(msg, INPUT_METHOD_NOT_RESPONDING_TIMEOUT);
                return -1;
            }
            Log.w(TAG, "Unable to send input event to IME: " + this.mCurId + " dropping: " + event);
        }
        return 0;
    }

    /* JADX WARNING: Missing block: B:12:0x0047, code:
            invokeFinishedInputEventCallback(r1, r10);
     */
    /* JADX WARNING: Missing block: B:13:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void finishedInputEvent(int seq, boolean handled, boolean timeout) {
        synchronized (this.mH) {
            int index = this.mPendingEvents.indexOfKey(seq);
            if (index < 0) {
                return;
            }
            PendingEvent p = (PendingEvent) this.mPendingEvents.valueAt(index);
            this.mPendingEvents.removeAt(index);
            Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
            if (timeout) {
                Log.w(TAG, "Timeout waiting for IME to handle input event after 2500 ms: " + p.mInputMethodId);
            } else {
                this.mH.removeMessages(6, p);
            }
        }
    }

    void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
        p.mHandled = handled;
        if (p.mHandler.getLooper().isCurrentThread()) {
            p.run();
            return;
        }
        Message msg = Message.obtain(p.mHandler, p);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void flushPendingEventsLocked() {
        this.mH.removeMessages(7);
        int count = this.mPendingEvents.size();
        for (int i = 0; i < count; i++) {
            Message msg = this.mH.obtainMessage(7, this.mPendingEvents.keyAt(i), 0);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }

    private PendingEvent obtainPendingEventLocked(InputEvent event, Object token, String inputMethodId, FinishedInputEventCallback callback, Handler handler) {
        PendingEvent p = (PendingEvent) this.mPendingEventPool.acquire();
        if (p == null) {
            p = new PendingEvent(this, null);
        }
        p.mEvent = event;
        p.mToken = token;
        p.mInputMethodId = inputMethodId;
        p.mCallback = callback;
        p.mHandler = handler;
        return p;
    }

    private void recyclePendingEventLocked(PendingEvent p) {
        p.recycle();
        this.mPendingEventPool.release(p);
    }

    public void showInputMethodPicker() {
        synchronized (this.mH) {
            showInputMethodPickerLocked();
        }
    }

    public void showInputMethodPicker(boolean showAuxiliarySubtypes) {
        synchronized (this.mH) {
            int mode;
            if (showAuxiliarySubtypes) {
                mode = 1;
            } else {
                mode = 2;
            }
            try {
                this.mService.showInputMethodPickerFromClient(this.mClient, mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void showInputMethodPickerLocked() {
        try {
            this.mService.showInputMethodPickerFromClient(this.mClient, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showInputMethodAndSubtypeEnabler(String imiId) {
        synchronized (this.mH) {
            try {
                this.mService.showInputMethodAndSubtypeEnablerFromClient(this.mClient, imiId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        try {
            return this.mService.getCurrentInputMethodSubtype();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        boolean currentInputMethodSubtype;
        synchronized (this.mH) {
            try {
                currentInputMethodSubtype = this.mService.setCurrentInputMethodSubtype(subtype);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return currentInputMethodSubtype;
    }

    /* JADX WARNING: Missing block: B:9:0x0037, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyUserAction() {
        synchronized (this.mH) {
            if (this.mLastSentUserActionNotificationSequenceNumber != this.mNextUserActionNotificationSequenceNumber) {
                try {
                    if (DEBUG) {
                        Log.w(TAG, "notifyUserAction:  mLastSentUserActionNotificationSequenceNumber: " + this.mLastSentUserActionNotificationSequenceNumber + " mNextUserActionNotificationSequenceNumber: " + this.mNextUserActionNotificationSequenceNumber);
                    }
                    this.mService.notifyUserAction(this.mNextUserActionNotificationSequenceNumber);
                    this.mLastSentUserActionNotificationSequenceNumber = this.mNextUserActionNotificationSequenceNumber;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else if (DEBUG) {
                Log.w(TAG, "Ignoring notifyUserAction as it has already been sent. mLastSentUserActionNotificationSequenceNumber: " + this.mLastSentUserActionNotificationSequenceNumber + " mNextUserActionNotificationSequenceNumber: " + this.mNextUserActionNotificationSequenceNumber);
            }
        }
    }

    public Map<InputMethodInfo, List<InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() {
        HashMap<InputMethodInfo, List<InputMethodSubtype>> ret;
        synchronized (this.mH) {
            ret = new HashMap();
            try {
                List<Object> info = this.mService.getShortcutInputMethodsAndSubtypes();
                ArrayList subtypes = null;
                if (info != null && (info.isEmpty() ^ 1) != 0) {
                    int N = info.size();
                    for (int i = 0; i < N; i++) {
                        Object o = info.get(i);
                        if (o instanceof InputMethodInfo) {
                            if (ret.containsKey(o)) {
                                Log.e(TAG, "IMI list already contains the same InputMethod.");
                                break;
                            }
                            subtypes = new ArrayList();
                            ret.put((InputMethodInfo) o, subtypes);
                        } else if (subtypes != null && (o instanceof InputMethodSubtype)) {
                            subtypes.add((InputMethodSubtype) o);
                        }
                    }
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ret;
    }

    public int getInputMethodWindowVisibleHeight() {
        int inputMethodWindowVisibleHeight;
        synchronized (this.mH) {
            try {
                inputMethodWindowVisibleHeight = this.mService.getInputMethodWindowVisibleHeight();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return inputMethodWindowVisibleHeight;
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        synchronized (this.mH) {
            try {
                this.mService.clearLastInputMethodWindowForTransition(token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean switchToLastInputMethod(IBinder imeToken) {
        boolean switchToLastInputMethod;
        synchronized (this.mH) {
            try {
                switchToLastInputMethod = this.mService.switchToLastInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return switchToLastInputMethod;
    }

    public boolean switchToNextInputMethod(IBinder imeToken, boolean onlyCurrentIme) {
        boolean switchToNextInputMethod;
        synchronized (this.mH) {
            try {
                switchToNextInputMethod = this.mService.switchToNextInputMethod(imeToken, onlyCurrentIme);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return switchToNextInputMethod;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder imeToken) {
        boolean shouldOfferSwitchingToNextInputMethod;
        synchronized (this.mH) {
            try {
                shouldOfferSwitchingToNextInputMethod = this.mService.shouldOfferSwitchingToNextInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return shouldOfferSwitchingToNextInputMethod;
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        synchronized (this.mH) {
            try {
                this.mService.setAdditionalInputMethodSubtypes(imiId, subtypes);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        InputMethodSubtype lastInputMethodSubtype;
        synchronized (this.mH) {
            try {
                lastInputMethodSubtype = this.mService.getLastInputMethodSubtype();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return lastInputMethodSubtype;
    }

    public void exposeContent(IBinder token, InputContentInfo inputContentInfo, EditorInfo editorInfo) {
        Uri contentUri = inputContentInfo.getContentUri();
        try {
            IInputContentUriToken uriToken = this.mService.createInputContentUriToken(token, contentUri, editorInfo.packageName);
            if (uriToken != null) {
                inputContentInfo.setUriToken(uriToken);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "createInputContentAccessToken failed. contentUri=" + contentUri.toString() + " packageName=" + editorInfo.packageName, e);
        }
    }

    void doDump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        if (args != null && args.length > 0) {
            if ("enable".equals(args[0])) {
                DEBUG = true;
                BaseInputConnection.DEBUG = true;
                EditableInputConnection.DEBUG = true;
                p.println("InputMethodManager DEBUG is turn on!");
                return;
            } else if ("disable".equals(args[0])) {
                DEBUG = false;
                BaseInputConnection.DEBUG = false;
                EditableInputConnection.DEBUG = false;
                p.println("InputMethodManager DEBUG is turn off!");
                return;
            }
        }
        p.println("Input method client state for " + this + SettingsStringUtil.DELIMITER);
        p.println("  mService=" + this.mService);
        p.println("  mMainLooper=" + this.mMainLooper);
        p.println("  mIInputContext=" + this.mIInputContext);
        p.println("  mActive=" + this.mActive + " mHasBeenInactive=" + this.mHasBeenInactive + " mBindSequence=" + this.mBindSequence + " mCurId=" + this.mCurId);
        p.println("  mFullscreenMode=" + this.mFullscreenMode);
        p.println("  mCurMethod=" + this.mCurMethod);
        p.println("  mCurRootView=" + this.mCurRootView);
        p.println("  mServedView=" + this.mServedView);
        p.println("  mNextServedView=" + this.mNextServedView);
        p.println("  mServedConnecting=" + this.mServedConnecting);
        if (this.mCurrentTextBoxAttribute != null) {
            p.println("  mCurrentTextBoxAttribute:");
            this.mCurrentTextBoxAttribute.dump(p, "    ");
        } else {
            p.println("  mCurrentTextBoxAttribute: null");
        }
        p.println("  mServedInputConnectionWrapper=" + this.mServedInputConnectionWrapper);
        p.println("  mCompletions=" + Arrays.toString(this.mCompletions));
        p.println("  mCursorRect=" + this.mCursorRect);
        p.println("  mCursorSelStart=" + this.mCursorSelStart + " mCursorSelEnd=" + this.mCursorSelEnd + " mCursorCandStart=" + this.mCursorCandStart + " mCursorCandEnd=" + this.mCursorCandEnd);
        p.println("  mNextUserActionNotificationSequenceNumber=" + this.mNextUserActionNotificationSequenceNumber + " mLastSentUserActionNotificationSequenceNumber=" + this.mLastSentUserActionNotificationSequenceNumber);
    }

    private static String dumpViewInfo(View view) {
        if (view == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(view);
        sb.append(",focus=").append(view.hasFocus());
        sb.append(",windowFocus=").append(view.hasWindowFocus());
        sb.append(",window=").append(view.getWindowToken());
        sb.append(",temporaryDetach=").append(view.isTemporarilyDetached());
        return sb.toString();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean isMultiWindow() {
        return getDockSide() != this.DOCKED_INVALID ? isExport() ^ 1 : false;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int getDockSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get dock side: " + e);
            return this.DOCKED_INVALID;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean isExport() {
        return "yes".equals(SystemProperties.get("ro.vivo.product.overseas", "no"));
    }
}
