package com.vivo.services.daemon;

import android.os.IInterface;
import android.os.RemoteException;

interface IVivoDmService extends IInterface {
    public static final int RUN_SHELL_FILE_TRANSACTION = 2;
    public static final int RUN_SHELL_TRANSACTION = 1;
    public static final int RUN_SHELL_WITH_RESULT_TRANSACTION = 3;
    public static final String descriptor = "IVivoDmService";

    int runShell(String str) throws RemoteException;

    int runShellFile(String str) throws RemoteException;

    String runShellWithResult(String str) throws RemoteException;
}
