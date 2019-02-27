package com.vivo.services.epm;

public class ExceptionType {

    public static class BaseInfoException {
        public static final int EXCEPTION_TYPE_CPU_INFO = 3001;
        public static final int EXCEPTION_TYPE_IO_INFO = 3003;
        public static final int EXCEPTION_TYPE_MEM_INFO = 3002;
    }

    public static class PerformanceException {
        public static final int EXCEPTION_TYPE_JANK = 2001;
    }

    public static class SystemException {
        public static final int EXCEPTION_TYPE_CRASH = 2;
    }
}
