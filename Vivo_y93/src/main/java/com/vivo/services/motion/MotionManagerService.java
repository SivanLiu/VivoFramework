package com.vivo.services.motion;

import android.content.Context;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import vivo.app.motion.IMotionManager.Stub;

public class MotionManagerService extends Stub {
    private static final String TAG = "MotionManagerService";
    private static final Object mObjectLock = new Object();
    private final Stack<ClientStackEntry> mClientStack = new Stack();

    private static class ClientStackEntry {
        private String mCallingPackageName;
        private IBinder mCb;
        private String mClientId;
        private DeathHandler mDh;
        private String mType;

        public ClientStackEntry(String clientId, String callingPackageName, String type, IBinder cb, DeathHandler dh) {
            this.mClientId = clientId;
            this.mCallingPackageName = callingPackageName;
            this.mType = type;
            this.mCb = cb;
            this.mDh = dh;
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public DeathHandler getDeathHandler() {
            return this.mDh;
        }

        public void unlinkToDeath() {
            try {
                if (this.mCb != null && this.mDh != null) {
                    this.mCb.unlinkToDeath(this.mDh, 0);
                    this.mDh = null;
                }
            } catch (NoSuchElementException e) {
                Log.d(MotionManagerService.TAG, "Encountered " + e + " in ClientStackEntry.unlinkToDeath()");
            }
        }

        protected void finalize() throws Throwable {
            unlinkToDeath();
            super.finalize();
        }
    }

    private class DeathHandler implements DeathRecipient {
        private String mClientId;

        DeathHandler(String clientId) {
            this.mClientId = clientId;
        }

        public void binderDied() {
            synchronized (MotionManagerService.mObjectLock) {
                Log.d(MotionManagerService.TAG, "[binderDied] mClientId:" + this.mClientId);
                MotionManagerService.this.removeClientStackEntry(this.mClientId, false);
            }
        }
    }

    public MotionManagerService(Context context) {
    }

    public List getClients() {
        if (this.mClientStack == null || (this.mClientStack.empty() ^ 1) == 0) {
            return null;
        }
        List clients = new ArrayList();
        clients.add(((ClientStackEntry) this.mClientStack.peek()).mClientId);
        return clients;
    }

    public int register(String clientId, String callingPackageName, String type, IBinder cb) {
        if (!type.equals("1")) {
            return -1;
        }
        Log.d(TAG, "[register] type:" + type + ",clientId:" + clientId + ",pkg:" + callingPackageName);
        if (cb.pingBinder()) {
            synchronized (mObjectLock) {
                DeathHandler dh = new DeathHandler(clientId);
                try {
                    cb.linkToDeath(dh, 0);
                    if (this.mClientStack.empty() || !((ClientStackEntry) this.mClientStack.peek()).mClientId.equals(clientId)) {
                        removeClientStackEntry(clientId, false);
                        this.mClientStack.push(new ClientStackEntry(clientId, callingPackageName, type, cb, dh));
                        return 0;
                    }
                    Log.d(TAG, "the current top of the client stack:" + clientId);
                    cb.unlinkToDeath(dh, 0);
                    return -1;
                } catch (RemoteException e) {
                    Log.d(TAG, "Could not link to " + cb + " binder death.");
                    return -1;
                }
            }
        }
        Log.d(TAG, "!cb.pingBinder()");
        return -1;
    }

    public int unregister(String clientId) {
        try {
            synchronized (mObjectLock) {
                removeClientStackEntry(clientId, true);
            }
        } catch (Exception e) {
            Log.d(TAG, "FATAL EXCEPTION unregister caused " + e);
            e.printStackTrace();
        }
        return 0;
    }

    private void removeClientStackEntry(String clientToRemove, boolean signal) {
        ClientStackEntry Cse;
        if (this.mClientStack.empty() || !((ClientStackEntry) this.mClientStack.peek()).mClientId.equals(clientToRemove)) {
            Iterator<ClientStackEntry> stackIterator = this.mClientStack.iterator();
            while (stackIterator.hasNext()) {
                Cse = (ClientStackEntry) stackIterator.next();
                if (Cse.mClientId.equals(clientToRemove)) {
                    Log.d(TAG, "Removing entry for " + Cse.mClientId);
                    stackIterator.remove();
                    Cse.unlinkToDeath();
                }
            }
            return;
        }
        Cse = (ClientStackEntry) this.mClientStack.pop();
        Cse.unlinkToDeath();
        Log.d(TAG, "Removed entry for " + Cse.mClientId);
    }
}
