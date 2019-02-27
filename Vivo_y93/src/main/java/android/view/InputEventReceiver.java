package android.view;

import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseIntArray;
import com.vivo.services.security.client.VivoPermissionManager;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public abstract class InputEventReceiver {
    public static boolean DEBUG_VIVO_CTRL = SystemProperties.get(VivoPermissionManager.KEY_VIVO_LOG_CTRL, "no").equals("yes");
    public static boolean DEBUG_VIVO_INPUT = SystemProperties.get("persist.sys.input.log", "no").equals("yes");
    public static boolean DEBUG_VIVO_MOTION = SystemProperties.get("persist.sys.input.motionlog", "no").equals("yes");
    private static final String TAG = "InputEventReceiver";
    static int nDebug = 0;
    static long sLastTime = -1;
    Choreographer mChoreographer;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private InputChannel mInputChannel;
    private MessageQueue mMessageQueue;
    private long mReceiverPtr;
    private final SparseIntArray mSeqMap = new SparseIntArray();

    public interface Factory {
        InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper);
    }

    private static native boolean nativeConsumeBatchedInputEvents(long j, long j2);

    private static native void nativeDispose(long j);

    private static native void nativeFinishInputEvent(long j, int i, boolean z);

    private static native long nativeInit(WeakReference<InputEventReceiver> weakReference, InputChannel inputChannel, MessageQueue messageQueue);

    public InputEventReceiver(InputChannel inputChannel, Looper looper) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null");
        } else if (looper == null) {
            throw new IllegalArgumentException("looper must not be null");
        } else {
            this.mInputChannel = inputChannel;
            this.mMessageQueue = looper.getQueue();
            this.mReceiverPtr = nativeInit(new WeakReference(this), inputChannel, this.mMessageQueue);
            this.mCloseGuard.open("dispose");
        }
    }

    protected void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public void dispose() {
        dispose(false);
    }

    private void dispose(boolean finalized) {
        if (this.mCloseGuard != null) {
            if (finalized) {
                this.mCloseGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        if (this.mReceiverPtr != 0) {
            nativeDispose(this.mReceiverPtr);
            this.mReceiverPtr = 0;
        }
        this.mInputChannel = null;
        this.mMessageQueue = null;
    }

    public void onInputEvent(InputEvent event, int displayId) {
        finishInputEvent(event, false);
    }

    public void onBatchedInputEventPending() {
        consumeBatchedInputEvents(-1);
    }

    public final void finishInputEvent(InputEvent event, boolean handled) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        if (this.mReceiverPtr == 0) {
            Log.w(TAG, "Attempted to finish an input event but the input event receiver has already been disposed.");
        } else {
            int index = this.mSeqMap.indexOfKey(event.getSequenceNumber());
            if (index < 0) {
                Log.w(TAG, "Attempted to finish an input event that is not in progress.");
            } else {
                int seq = this.mSeqMap.valueAt(index);
                this.mSeqMap.removeAt(index);
                nativeFinishInputEvent(this.mReceiverPtr, seq, handled);
            }
        }
        event.recycleIfNeededAfterDispatch();
    }

    public final boolean consumeBatchedInputEvents(long frameTimeNanos) {
        if (this.mReceiverPtr != 0) {
            return nativeConsumeBatchedInputEvents(this.mReceiverPtr, frameTimeNanos);
        }
        Log.w(TAG, "Attempted to consume batched input events but the input event receiver has already been disposed.");
        return false;
    }

    private void dispatchInputEvent(int seq, InputEvent event, int displayId) {
        debugVivoInputEvent("dispatchInputEvent", event);
        this.mSeqMap.put(event.getSequenceNumber(), seq);
        onInputEvent(event, displayId);
    }

    private void dispatchBatchedInputEventPending() {
        onBatchedInputEventPending();
    }

    private void dispatchMotionEventInfo(int motionEventType, int touchMoveNum) {
        try {
            if (this.mChoreographer == null) {
                this.mChoreographer = Choreographer.getInstance();
            }
            if (this.mChoreographer != null) {
                this.mChoreographer.setMotionEventInfo(motionEventType, touchMoveNum);
            }
        } catch (Exception e) {
            Log.e(TAG, "cannot invoke setMotionEventInfo.");
        }
    }

    static boolean checkTimeInterval() {
        if (sLastTime == -1) {
            sLastTime = SystemClock.elapsedRealtime();
        }
        if (SystemClock.elapsedRealtime() - sLastTime <= DateUtils.MINUTE_IN_MILLIS) {
            return false;
        }
        sLastTime = SystemClock.elapsedRealtime();
        return true;
    }

    private void debugVivoInputEvent(InputEvent event) {
        debugVivoInputEvent("debugVivoInputEvent", event);
    }

    private void debugVivoInputEvent(String tag, InputEvent event) {
        if (DEBUG_VIVO_CTRL || DEBUG_VIVO_INPUT) {
            if (event != null) {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    if (DEBUG_VIVO_MOTION) {
                        Log.d(TAG, tag + " ,vLog[" + this.mInputChannel + "] ,event=" + event);
                    } else if (motionEvent.getAction() != 2 && motionEvent.getAction() != 7 && motionEvent.getAction() != 8) {
                        Log.d(TAG, tag + " ,vLog[" + this.mInputChannel + "] ,event=" + event);
                    } else if (checkTimeInterval()) {
                        Log.d(TAG, tag + " ,vLog[" + this.mInputChannel + "] ,event=" + event);
                    }
                } else if (event instanceof KeyEvent) {
                    Log.d(TAG, tag + " ,vLog[" + this.mInputChannel + "] ,event=" + ((KeyEvent) event));
                }
            }
        } else if (event != null && (event instanceof KeyEvent)) {
            KeyEvent kevent = (KeyEvent) event;
            if ((kevent.getKeyCode() == 4 || kevent.getKeyCode() == 24) && kevent.getAction() == 1) {
                nDebug++;
                if (nDebug == 10) {
                    nDebug = 0;
                    if (!(this.mInputChannel == null || this.mInputChannel.getName() == null || (this.mInputChannel.getName().contains("WindowManager") ^ 1) == 0 || (this.mInputChannel.getName().contains("input_hook") ^ 1) == 0)) {
                        DEBUG_VIVO_CTRL = SystemProperties.get(VivoPermissionManager.KEY_VIVO_LOG_CTRL, "no").equals("yes");
                    }
                }
            }
        }
    }
}
