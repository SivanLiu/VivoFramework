package android.os;

import android.util.ArrayMap;

public final class VivoFrozenPackageManager {
    private static final String TAG = "VivoFrozenPackageManager";
    private static VivoFrozenPackageManager mInstance = null;
    ArrayMap<Integer, String> frozenPackage = new ArrayMap();

    private VivoFrozenPackageManager() {
    }

    public static VivoFrozenPackageManager getInstance() {
        if (mInstance == null) {
            mInstance = new VivoFrozenPackageManager();
        }
        return mInstance;
    }

    /* JADX WARNING: Missing block: B:20:0x002e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isFrozenPackage(String packageName, int uid, int userId) {
        if (uid < 10000 || this.frozenPackage.size() <= 0) {
            return false;
        }
        if (uid >= 0 && packageName != null) {
            try {
                synchronized (this.frozenPackage) {
                    if (this.frozenPackage.containsKey(Integer.valueOf(uid)) && this.frozenPackage.containsValue(packageName)) {
                        return true;
                    }
                }
            } catch (Exception e) {
            }
        } else if (uid >= 0 && packageName == null) {
            synchronized (this.frozenPackage) {
                if (this.frozenPackage.containsKey(Integer.valueOf(uid))) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public boolean isFrozenPackage(String packageName) {
        return isFrozenPackage(packageName, -1, UserHandle.myUserId());
    }

    public boolean isFrozenPackage(int uid) {
        return isFrozenPackage(null, uid, UserHandle.myUserId());
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addFrozenPackage(String packageName, int uid, int userId) {
        if (packageName == null || uid <= 0 || Binder.getCallingUid() != 1000 || this.frozenPackage == null) {
            return false;
        }
        try {
            synchronized (this.frozenPackage) {
                this.frozenPackage.put(Integer.valueOf(uid), packageName);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addFrozenPackage(String packageName, int uid) {
        return addFrozenPackage(packageName, uid, UserHandle.myUserId());
    }

    public boolean removeFrozenPackage(String packageName, int uid) {
        return removeFrozenPackage(packageName, uid, UserHandle.myUserId());
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean removeFrozenPackage(String packageName, int uid, int userId) {
        if (packageName == null || uid <= 0 || Binder.getCallingUid() != 1000 || this.frozenPackage == null) {
            return false;
        }
        try {
            synchronized (this.frozenPackage) {
                this.frozenPackage.remove(Integer.valueOf(uid));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWhiteListProvider(String packageName) {
        if (packageName == null) {
            return false;
        }
        if (packageName.equals("com.tencent.mm") || packageName.equals("com.tencent.mobileqq") || packageName.equals("com.sina.weibo") || packageName.equals("com.eg.android.AlipayGphone") || packageName.equals("com.google.android.gms")) {
            return true;
        }
        return false;
    }
}
