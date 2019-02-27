package com.vivo.services.rms;

import android.os.Bundle;
import com.vivo.services.rms.sdk.IProcessEvent;
import com.vivo.services.rms.sdk.ISystemEvent;
import com.vivo.services.rms.sdk.args.Args;
import com.vivo.services.rms.sdk.args.ArgsFactory;
import com.vivo.services.rms.sdk.args.BundleArgs;
import com.vivo.services.rms.sdk.args.Int2Args;
import com.vivo.services.rms.sdk.args.Int3Args;
import com.vivo.services.rms.sdk.args.Int3String3Args;
import com.vivo.services.rms.sdk.args.IntArray2Args;
import com.vivo.services.rms.sdk.args.IntArray3Args;
import com.vivo.services.rms.sdk.args.IntArrayStringArrayArgs;
import com.vivo.services.rms.sdk.args.IntStringArgs;
import com.vivo.services.rms.sdk.args.LongStringListArgs;
import com.vivo.services.rms.sdk.args.StringStringListArgs;
import java.util.ArrayList;

public class EventDispatcher implements IProcessEvent, ISystemEvent {
    public static final String KEY_NAME = "key_name";
    public static final String KEY_VALUE = "key_value";
    public static final String NAME_MONKEY_STATE = "monkey_state";
    public static EventDispatcher sPPSEventDispatcher;

    public static class Instance {
        private static final EventDispatcher INSTANCE = new EventDispatcher();
    }

    /* synthetic */ EventDispatcher(EventDispatcher -this0) {
        this();
    }

    private EventDispatcher() {
        ArgsFactory.addClass(Int2Args.class, 4);
        ArgsFactory.addClass(Int3Args.class, 4);
        ArgsFactory.addClass(IntArray2Args.class, 4);
        ArgsFactory.addClass(IntArray3Args.class, 4);
        ArgsFactory.addClass(Int3String3Args.class, 4);
        ArgsFactory.addClass(IntStringArgs.class, 4);
        ArgsFactory.addClass(BundleArgs.class, 2);
        ArgsFactory.addClass(LongStringListArgs.class, 2);
        ArgsFactory.addClass(StringStringListArgs.class, 2);
    }

    public static EventDispatcher getInstance() {
        return Instance.INSTANCE;
    }

    public void add(int uid, String pkgName, int pkgFlags, int pid, String process, String reason) {
        RMServer.getInstance().postProcessEvent(5, builderArgs(uid, pid, pkgFlags, pkgName, process, reason));
    }

    public void remove(int pid, String reason) {
        RMServer.getInstance().postProcessEvent(6, builderArgs(pid, reason));
    }

    public void setOom(int pid, int oom) {
        RMServer.getInstance().postProcessEvent(4, builderArgs(pid, oom));
    }

    public void setAdj(int[] pids, int[] adjs) {
        RMServer.getInstance().postProcessEvent(0, builderArgs(pids, adjs));
    }

    public void setSchedGroup(int[] pids, int[] groups) {
        RMServer.getInstance().postProcessEvent(2, builderArgs(pids, groups));
    }

    public void setStates(int[] pids, int[] states, int[] masks) {
        RMServer.getInstance().postProcessEvent(1, builderArgs(pids, states, masks));
    }

    public void startActivity(String pkgName, String processName, int pid, int uid, int started) {
        RMServer.getInstance().postProcessEvent(7, builderArgs(pid, uid, started, pkgName, processName, null));
    }

    public void addDepPkg(int pid, String pkg) {
        RMServer.getInstance().postProcessEvent(8, builderArgs(pid, pkg));
    }

    public void addPkg(int pid, String pkg) {
        RMServer.getInstance().postProcessEvent(9, builderArgs(pid, pkg));
    }

    public void setConfig(Bundle bundle) {
        RMServer.getInstance().postProcessEvent(10, builderArgs(bundle));
    }

    public void setAppList(String typeName, ArrayList<String> list) {
        RMServer.getInstance().postSystemEvent(0, builderArgs(typeName, (ArrayList) list));
    }

    public void setBundle(Bundle bundle) {
        RMServer.getInstance().postSystemEvent(1, builderArgs(bundle));
    }

    public void setMonkeyState(int state) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, NAME_MONKEY_STATE);
        bundle.putInt(KEY_VALUE, state);
        setBundle(bundle);
    }

    public Args builderArgs(int int0, int int1, int int2, String str0, String str1, String str2) {
        Int3String3Args args = (Int3String3Args) ArgsFactory.create(Int3String3Args.class.getSimpleName());
        args.mInt0 = int0;
        args.mInt1 = int1;
        args.mInt2 = int2;
        args.mString0 = str0;
        args.mString1 = str1;
        args.mString2 = str2;
        return args;
    }

    public Args builderArgs(int int0, String str0) {
        IntStringArgs args = (IntStringArgs) ArgsFactory.create(IntStringArgs.class.getSimpleName());
        args.mInt0 = int0;
        args.mString0 = str0;
        return args;
    }

    public Args builderArgs(int int0, int int1) {
        Int2Args args = (Int2Args) ArgsFactory.create(Int2Args.class.getSimpleName());
        args.mInt0 = int0;
        args.mInt1 = int1;
        return args;
    }

    public Args builderArgs(int int0, int int1, int int2) {
        Int3Args args = (Int3Args) ArgsFactory.create(Int3Args.class.getSimpleName());
        args.mInt0 = int0;
        args.mInt1 = int1;
        args.mInt2 = int2;
        return args;
    }

    public Args builderArgs(int[] int0, int[] int1) {
        IntArray2Args args = (IntArray2Args) ArgsFactory.create(IntArray2Args.class.getSimpleName());
        args.mIntArray0 = int0;
        args.mIntArray1 = int1;
        return args;
    }

    public Args builderArgs(int[] int0, int[] int1, int[] int2) {
        IntArray3Args args = (IntArray3Args) ArgsFactory.create(IntArray3Args.class.getSimpleName());
        args.mIntArray0 = int0;
        args.mIntArray1 = int1;
        args.mIntArray2 = int2;
        return args;
    }

    public Args builderArgs(long long0, ArrayList<String> list) {
        LongStringListArgs args = (LongStringListArgs) ArgsFactory.create(LongStringListArgs.class.getSimpleName());
        args.mLong0 = long0;
        args.mStringList0 = list;
        return args;
    }

    public Args builderArgs(String str0, ArrayList<String> list) {
        StringStringListArgs args = (StringStringListArgs) ArgsFactory.create(StringStringListArgs.class.getSimpleName());
        args.mString0 = str0;
        args.mStringList0 = list;
        return args;
    }

    public Args builderArgs(Bundle bundle) {
        BundleArgs args = (BundleArgs) ArgsFactory.create(BundleArgs.class.getSimpleName());
        args.mBundle = bundle;
        return args;
    }

    public Args builderArgs(int[] int0, String[] str0) {
        IntArrayStringArrayArgs args = (IntArrayStringArrayArgs) ArgsFactory.create(IntArrayStringArrayArgs.class.getSimpleName());
        args.mIntArray0 = int0;
        args.mStringArray0 = str0;
        return args;
    }
}
