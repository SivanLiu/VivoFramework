package com.vivo.common.utils;

import android.util.Slog;
import com.vivo.common.provider.Calendar.Events;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MiscUtils {
    private static final int MAX_LOG_LENGTH = 3800;
    private static final String TAG = "MiscUtils";

    public static void showSlogCompletion(String tag, String log) {
        if (log == null || log.length() <= MAX_LOG_LENGTH) {
            Slog.e(tag, log);
            return;
        }
        Slog.e(tag, log.substring(0, MAX_LOG_LENGTH));
        if (log.length() - 3800 > MAX_LOG_LENGTH) {
            showSlogCompletion(tag, log.substring(MAX_LOG_LENGTH, log.length()));
        } else {
            Slog.e(tag, log.substring(MAX_LOG_LENGTH, log.length()));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x005a A:{Catch:{ InterruptedException -> 0x009f }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005a A:{Catch:{ InterruptedException -> 0x009f }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005a A:{Catch:{ InterruptedException -> 0x009f }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x009b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String execCommand(String command) throws IOException {
        IOException e;
        Throwable th;
        Runtime runtime = Runtime.getRuntime();
        InputStream inputStream = null;
        InputStreamReader inputstreamreader = null;
        BufferedReader bufferedreader = null;
        StringBuilder sb = new StringBuilder();
        try {
            Process proc = runtime.exec(command);
            if (proc == null) {
                return Events.DEFAULT_SORT_ORDER;
            }
            try {
                inputStream = proc.getInputStream();
                InputStreamReader inputstreamreader2 = new InputStreamReader(inputStream);
                try {
                    BufferedReader bufferedreader2 = new BufferedReader(inputstreamreader2);
                    try {
                        String str = Events.DEFAULT_SORT_ORDER;
                        while (true) {
                            str = bufferedreader2.readLine();
                            if (str == null) {
                                break;
                            }
                            sb.append(str);
                            sb.append(10);
                        }
                        if (bufferedreader2 != null) {
                            bufferedreader2.close();
                        }
                        if (inputstreamreader2 != null) {
                            inputstreamreader2.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        bufferedreader = bufferedreader2;
                    } catch (IOException e2) {
                        e = e2;
                        bufferedreader = bufferedreader2;
                        inputstreamreader = inputstreamreader2;
                        try {
                            e.printStackTrace();
                            if (bufferedreader != null) {
                            }
                            if (inputstreamreader != null) {
                            }
                            if (inputStream != null) {
                            }
                            if (proc.waitFor() != 0) {
                            }
                            return sb.toString();
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedreader != null) {
                            }
                            if (inputstreamreader != null) {
                            }
                            if (inputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedreader = bufferedreader2;
                        inputstreamreader = inputstreamreader2;
                        if (bufferedreader != null) {
                        }
                        if (inputstreamreader != null) {
                        }
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e3) {
                    e = e3;
                    inputstreamreader = inputstreamreader2;
                    e.printStackTrace();
                    if (bufferedreader != null) {
                    }
                    if (inputstreamreader != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (proc.waitFor() != 0) {
                    }
                    return sb.toString();
                } catch (Throwable th4) {
                    th = th4;
                    inputstreamreader = inputstreamreader2;
                    if (bufferedreader != null) {
                        bufferedreader.close();
                    }
                    if (inputstreamreader != null) {
                        inputstreamreader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                e.printStackTrace();
                if (bufferedreader != null) {
                    bufferedreader.close();
                }
                if (inputstreamreader != null) {
                    inputstreamreader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (proc.waitFor() != 0) {
                }
                return sb.toString();
            }
            try {
                if (proc.waitFor() != 0) {
                    System.err.println("exit value = " + proc.exitValue());
                }
            } catch (InterruptedException e5) {
                System.err.println(e5);
            }
            return sb.toString();
        } catch (IOException e6) {
            e6.printStackTrace();
            return Events.DEFAULT_SORT_ORDER;
        }
    }

    public static void dumpMeminfo(String tag) {
        try {
            showSlogCompletion(tag, execCommand("dumpsys -t 6 meminfo"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
