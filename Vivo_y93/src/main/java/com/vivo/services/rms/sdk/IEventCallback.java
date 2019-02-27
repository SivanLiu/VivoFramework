package com.vivo.services.rms.sdk;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import com.vivo.services.rms.sdk.args.Args;
import java.io.FileDescriptor;

public interface IEventCallback extends IInterface {
    public static final String DESCRIPTOR = "com.android.server.rms.IEventCallback";
    public static final int DO_INIT = 5;
    public static final int DUMP = 3;
    public static final int MY_PID = 4;
    public static final int ON_PROCESS_EVENT = 1;
    public static final int ON_SYSTEM_EVENT = 2;

    void doInit(Bundle bundle) throws RemoteException;

    void dumpData(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException;

    int myPid() throws RemoteException;

    void onProcessEvent(int i, Args args) throws RemoteException;

    void onSystemEvent(int i, Args args) throws RemoteException;
}
