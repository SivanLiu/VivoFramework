package com.vivo.services.vivomain;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.vivo.services.vivodevice.VivoDeviceNative;
import com.vivo.services.vivodevice.VivoDeviceProxy;
import com.vivo.services.vivomain.IVivoMainService.Stub;
import java.util.concurrent.TimeUnit;

public class VivoMainService extends Stub {
    private static final String TAG = "VivoMainService";
    private VivoActionProxy actionProxy;
    private VivoDeviceProxy deviceProxy;

    public VivoMainService(Context ctx) {
        this.deviceProxy = VivoDeviceProxy.getInstance();
        this.actionProxy = VivoActionProxy.getInstance();
        VivoDeviceNative.getDeviceNative();
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(15);
                        System.out.println("ywb : sync file system.");
                        VivoDeviceNative.getDeviceNative().fileSystemSync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        Log.d(TAG, "VivoMainService created");
    }

    public String command(String comm) throws RemoteException {
        Log.d(TAG, "VivoMainService comm=" + comm);
        String[] strs = comm.split(":");
        if (strs[0].equals("device")) {
            return handDeviceCommand(strs[1]);
        }
        if (strs[0].equals("file")) {
            return handFileCommand(strs[1]);
        }
        if (strs[0].equals("action")) {
            return handActionCommand(strs[1]);
        }
        return "error";
    }

    private String handDeviceCommand(String comm) {
        String[] strs = comm.split(",");
        String name = strs[0];
        String property = strs[1];
        String operation = strs[2];
        if (operation.equals("get")) {
            return this.deviceProxy.getDeviceByName(name).getPropertyByName(property).getValue();
        }
        if (operation.equals("set")) {
            return this.deviceProxy.getDeviceByName(name).getPropertyByName(property).setValue(strs[3]);
        }
        return "error";
    }

    private String handFileCommand(String comm) {
        if (!comm.equals("copyApanicFile")) {
            boolean equals = comm.equals("copyEfsErrFile");
        }
        return "error";
    }

    private String handActionCommand(String comm) {
        String[] strs = comm.split(",");
        String action = strs[0];
        String target = strs[1];
        String operation = strs[2];
        if (action.equals("play") && target.equals("boot_animation")) {
            if (operation.equals("start")) {
                return this.actionProxy.startBootAnimation();
            }
            if (operation.equals("stop")) {
                return this.actionProxy.stopBootAnimation();
            }
        }
        return "error";
    }
}
