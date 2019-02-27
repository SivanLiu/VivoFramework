package com.vivo.services.epm;

import android.content.Context;
import android.util.SparseArray;
import com.vivo.services.epm.ExceptionType.BaseInfoException;
import com.vivo.services.epm.ExceptionType.PerformanceException;
import com.vivo.services.epm.policy.cpuinfo.CpuInfoPolicyHandler;
import com.vivo.services.epm.policy.crash.CrashExceptionPolicyHandler;
import com.vivo.services.epm.policy.jank.JankExceptionPolicyHandler;
import java.io.PrintWriter;

public class ExceptionPolicyHandlerRegister {
    private static SparseArray<BaseExceptionPolicyHandler> mRegistedExceptionPolicyHandlers = new SparseArray();

    public static void registePolicyHandler(int type, BaseExceptionPolicyHandler handler) {
        if (mRegistedExceptionPolicyHandlers != null) {
            handler.addInterestedException(type);
            mRegistedExceptionPolicyHandlers.put(type, handler);
        }
    }

    public static void unregistePolicyHandler(int type) {
        if (mRegistedExceptionPolicyHandlers != null) {
            BaseExceptionPolicyHandler handler = getExceptionPolicyHandler(type);
            if (handler != null) {
                handler.removeInterestedException(type);
                mRegistedExceptionPolicyHandlers.remove(type);
            }
        }
    }

    public static BaseExceptionPolicyHandler getExceptionPolicyHandler(int type) {
        if (mRegistedExceptionPolicyHandlers != null) {
            return (BaseExceptionPolicyHandler) mRegistedExceptionPolicyHandlers.get(type);
        }
        return null;
    }

    public static void registAllPolicyHandlerDefault(Context context) {
        registePolicyHandler(2, new CrashExceptionPolicyHandler(context));
        registePolicyHandler(PerformanceException.EXCEPTION_TYPE_JANK, new JankExceptionPolicyHandler(context));
        registePolicyHandler(BaseInfoException.EXCEPTION_TYPE_CPU_INFO, new CpuInfoPolicyHandler(context));
    }

    public static void dump(PrintWriter pw) {
        int handlerNumbers = mRegistedExceptionPolicyHandlers.size();
        for (int i = 0; i < handlerNumbers; i++) {
            int exceptionType = mRegistedExceptionPolicyHandlers.keyAt(i);
            ((BaseExceptionPolicyHandler) mRegistedExceptionPolicyHandlers.valueAt(i)).dump(pw);
        }
    }
}
