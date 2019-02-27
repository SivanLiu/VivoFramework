package com.vivo.services.rms.sdk.args;

import android.util.Log;
import com.vivo.services.rms.sdk.ObjectCache;
import java.util.ArrayList;
import java.util.HashMap;

public class ArgsFactory {
    public static final String TAG = "ObjectFactory";
    public static final HashMap<String, ObjectCache<Args>> sCaches = new HashMap();

    public static void addClass(Class<? extends Args> clazz, int maxSize) {
        if (clazz != null) {
            String className = clazz.getSimpleName();
            if (((ObjectCache) sCaches.get(className)) != null) {
                Log.e(TAG, String.format("addClass %s fail.", new Object[]{className}));
                return;
            }
            sCaches.put(className, new ObjectCache(clazz, maxSize));
        }
    }

    public static void recycle(Args obj) {
        if (obj != null) {
            obj.recycle();
            ObjectCache<Args> cache = (ObjectCache) sCaches.get(obj.getClass().getSimpleName());
            if (cache == null) {
                Log.e(TAG, String.format("recycle %s fail.", new Object[]{className}));
                return;
            }
            cache.put((Object) obj);
        }
    }

    public static void recycle(ArrayList<? extends Args> objs) {
        if (!objs.isEmpty()) {
            for (Args obj : objs) {
                recycle(obj);
            }
        }
    }

    public static Args create(String className) {
        ObjectCache<Args> cache = (ObjectCache) sCaches.get(className);
        if (cache != null) {
            return (Args) cache.pop();
        }
        Log.e(TAG, String.format("create %s fail.", new Object[]{className}));
        return null;
    }
}
