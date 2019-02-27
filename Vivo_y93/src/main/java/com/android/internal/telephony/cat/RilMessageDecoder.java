package com.android.internal.telephony.cat;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

class RilMessageDecoder extends StateMachine {
    private static final int CMD_PARAMS_READY = 2;
    private static final int CMD_START = 1;
    private static RilMessageDecoder[] mInstance = null;
    private static int mSimCount = 0;
    private Handler mCaller = null;
    private CommandParamsFactory mCmdParamsFactory = null;
    private RilMessage mCurrentRilMessage = null;
    private StateCmdParamsReady mStateCmdParamsReady = new StateCmdParamsReady(this, null);
    private StateStart mStateStart = new StateStart(this, null);

    private class StateCmdParamsReady extends State {
        /* synthetic */ StateCmdParamsReady(RilMessageDecoder this$0, StateCmdParamsReady -this1) {
            this();
        }

        private StateCmdParamsReady() {
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public boolean processMessage(Message msg) {
            if (msg.what == 2) {
                try {
                    RilMessageDecoder.this.mCurrentRilMessage.mResCode = ResultCode.fromInt(msg.arg1);
                    RilMessageDecoder.this.mCurrentRilMessage.mData = msg.obj;
                    RilMessageDecoder.this.sendCmdForExecution(RilMessageDecoder.this.mCurrentRilMessage);
                    RilMessageDecoder.this.transitionTo(RilMessageDecoder.this.mStateStart);
                } catch (Exception e) {
                    CatLog.d((Object) this, "there is exception. may because it is disposed");
                    e.printStackTrace();
                }
            } else {
                CatLog.d((Object) this, "StateCmdParamsReady expecting CMD_PARAMS_READY=2 got " + msg.what);
                RilMessageDecoder.this.deferMessage(msg);
            }
            return true;
        }
    }

    private class StateStart extends State {
        /* synthetic */ StateStart(RilMessageDecoder this$0, StateStart -this1) {
            this();
        }

        private StateStart() {
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public boolean processMessage(Message msg) {
            if (msg.what == 1) {
                try {
                    if (RilMessageDecoder.this.decodeMessageParams((RilMessage) msg.obj)) {
                        RilMessageDecoder.this.transitionTo(RilMessageDecoder.this.mStateCmdParamsReady);
                    }
                } catch (Exception e) {
                    CatLog.d((Object) this, "there is exception. may because it is disposed");
                    e.printStackTrace();
                }
            } else {
                CatLog.d((Object) this, "StateStart unexpected expecting START=1 got " + msg.what);
            }
            return true;
        }
    }

    public static synchronized RilMessageDecoder getInstance(Handler caller, IccFileHandler fh, int slotId) {
        synchronized (RilMessageDecoder.class) {
            if (mInstance == null) {
                mSimCount = TelephonyManager.getDefault().getSimCount();
                mInstance = new RilMessageDecoder[mSimCount];
                for (int i = 0; i < mSimCount; i++) {
                    mInstance[i] = null;
                }
            }
            if (slotId == -1 || slotId >= mSimCount) {
                CatLog.d("RilMessageDecoder", "invaild slot id: " + slotId);
                return null;
            }
            if (mInstance[slotId] == null) {
                mInstance[slotId] = new RilMessageDecoder(caller, fh);
            }
            RilMessageDecoder rilMessageDecoder = mInstance[slotId];
            return rilMessageDecoder;
        }
    }

    public void sendStartDecodingMessageParams(RilMessage rilMsg) {
        Message msg = obtainMessage(1);
        msg.obj = rilMsg;
        sendMessage(msg);
    }

    public void sendMsgParamsDecoded(ResultCode resCode, CommandParams cmdParams) {
        Message msg = obtainMessage(2);
        msg.arg1 = resCode.value();
        msg.obj = cmdParams;
        sendMessage(msg);
    }

    private void sendCmdForExecution(RilMessage rilMsg) {
        this.mCaller.obtainMessage(10, new RilMessage(rilMsg)).sendToTarget();
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private RilMessageDecoder(Handler caller, IccFileHandler fh) {
        super("RilMessageDecoder");
        addState(this.mStateStart);
        addState(this.mStateCmdParamsReady);
        setInitialState(this.mStateStart);
        this.mCaller = caller;
        if (fh != null) {
            this.mCmdParamsFactory = new CommandParamsFactory(this, fh);
        } else {
            this.mCmdParamsFactory = null;
        }
    }

    private RilMessageDecoder() {
        super("RilMessageDecoder");
    }

    private boolean decodeMessageParams(RilMessage rilMsg) {
        this.mCurrentRilMessage = rilMsg;
        switch (rilMsg.mId) {
            case 1:
            case 4:
                this.mCurrentRilMessage.mResCode = ResultCode.OK;
                sendCmdForExecution(this.mCurrentRilMessage);
                return false;
            case 2:
            case 3:
            case 5:
                try {
                    try {
                        this.mCmdParamsFactory.make(BerTlv.decode(IccUtils.hexStringToBytes((String) rilMsg.mData)));
                        return true;
                    } catch (ResultException e) {
                        CatLog.d((Object) this, "decodeMessageParams: caught ResultException e=" + e);
                        this.mCurrentRilMessage.mResCode = e.result();
                        sendCmdForExecution(this.mCurrentRilMessage);
                        return false;
                    }
                } catch (Exception e2) {
                    CatLog.d((Object) this, "decodeMessageParams dropping zombie messages");
                    return false;
                }
            default:
                return false;
        }
    }

    public void dispose() {
        quitNow();
        this.mStateStart = null;
        this.mStateCmdParamsReady = null;
        this.mCmdParamsFactory.dispose();
        this.mCmdParamsFactory = null;
        this.mCurrentRilMessage = null;
        this.mCaller = null;
        mInstance = null;
    }
}
