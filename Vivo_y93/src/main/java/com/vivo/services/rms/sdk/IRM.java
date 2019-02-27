package com.vivo.services.rms.sdk;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import java.util.ArrayList;

public interface IRM extends IInterface {
    public static final String DESCRIPTOR = "com.vivo.rms.IRM";
    public static final int GET_BUNDBLE = 7;
    public static final int GET_PSS = 4;
    public static final int KILL_PROCESS = 2;
    public static final int RESTORE_SYS_FS = 8;
    public static final int SET_APP_LIST = 1;
    public static final int SET_BUNDBLE = 6;
    public static final int STOP_PACKAGE = 3;
    public static final int WRITE_SYS_FS = 5;

    Bundle getBundle(String str) throws RemoteException;

    int getPss(int i) throws RemoteException;

    void killProcess(int[] iArr, int[] iArr2, String str, boolean z) throws RemoteException;

    boolean restoreSysFs(ArrayList<String> arrayList) throws RemoteException;

    void setAppList(String str, ArrayList<String> arrayList) throws RemoteException;

    boolean setBundle(String str, Bundle bundle) throws RemoteException;

    void stopPackage(String str, int i, String str2) throws RemoteException;

    boolean writeSysFs(ArrayList<String> arrayList, ArrayList<String> arrayList2) throws RemoteException;
}
