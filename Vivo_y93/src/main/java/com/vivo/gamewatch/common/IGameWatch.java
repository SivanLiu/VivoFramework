package com.vivo.gamewatch.common;

import android.os.IInterface;
import android.os.RemoteException;

public interface IGameWatch extends IInterface {
    public static final int BEGINGATHER = 3;
    public static final String DESCRIPTOR = "com.vivo.gamewatch.common.IGameWatch";
    public static final int ENDGATHER = 4;
    public static final int EXECUTE = 5;
    public static final int GATHER = 1;
    public static final int GATHERS = 2;
    public static final String SERVER_NAME = "gamewatch_server";

    void beginGather(String str, ArgPack argPack) throws RemoteException;

    void endGather(String str, ArgPack argPack) throws RemoteException;

    ArgPack execute(String str, ArgPack argPack) throws RemoteException;

    void gather(String str, ArgPack argPack) throws RemoteException;

    void gathers(String[] strArr, ArgPack[] argPackArr) throws RemoteException;
}
