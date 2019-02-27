package com.vivo.statistics.sdk;

import android.os.IInterface;
import android.os.RemoteException;

public interface IGather extends IInterface {
    public static final int BEGINGATHER = 3;
    public static final String DESCRIPTOR = "com.vivo.rms.statistics.IGather";
    public static final int ENDGATHER = 4;
    public static final int GATHER = 1;
    public static final int GATHERS = 2;
    public static final String SERVER_NAME = "rms_statistics";

    void beginGather(String str, ArgPack argPack) throws RemoteException;

    void endGather(String str, ArgPack argPack) throws RemoteException;

    void gather(String str, ArgPack argPack) throws RemoteException;

    void gathers(String[] strArr, ArgPack[] argPackArr) throws RemoteException;
}
