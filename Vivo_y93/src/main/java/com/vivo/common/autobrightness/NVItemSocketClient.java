package com.vivo.common.autobrightness;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.SystemProperties;
import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.common.provider.Weather;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class NVItemSocketClient {
    private static final String SERVER_NAME = "vivoEmSvr-service";
    private static final String TAG = "NVItemSocketClient";
    private LocalSocket client;
    private LocalSocketAddress localAddr;

    public NVItemSocketClient() {
        int retry = 3;
        while (true) {
            int retry2 = retry;
            retry = retry2 - 1;
            if (retry2 > 0) {
                String emsvr = SystemProperties.get("sys.emsvr.opt", "0");
                String emsvrBak = SystemProperties.get("sys.emsvr.opt.bak", "0");
                if (!(emsvr.equals("1") && (emsvrBak.equals("1") ^ 1) == 0)) {
                    SystemProperties.set("sys.emsvr.opt", "1");
                    for (int i = 0; i < Weather.WEATHERVERSION_ROM_2_0 && !SystemProperties.get("sys.emsvr.opt.bak", "0").equals("1"); i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    this.client = new LocalSocket();
                    this.localAddr = new LocalSocketAddress(SERVER_NAME, Namespace.RESERVED);
                    this.client.connect(this.localAddr);
                } catch (Exception e2) {
                    try {
                        Log.e(TAG, "create socket Exception, retry=" + retry);
                        if (this.client != null) {
                            this.client.close();
                            this.client = null;
                        }
                        Log.e(TAG, "stop vivo_em_svr");
                        SystemProperties.set("sys.emsvr.opt", "0");
                    } catch (Exception e3) {
                        Log.e(TAG, "NullPointerException ex2");
                    }
                }
                if (this.client != null) {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0042, code:
            if (r6.endsWith("\n") == false) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:15:0x0044, code:
            r6 = r6.substring(0, r6.length() - 1);
     */
    /* JADX WARNING: Missing block: B:16:0x004f, code:
            if (r5 == null) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:19:0x0054, code:
            if (r3 == null) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:20:0x0056, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:22:0x005b, code:
            if (r9.client == null) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:23:0x005d, code:
            r9.client.close();
            r9.client = null;
     */
    /* JADX WARNING: Missing block: B:24:0x0065, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:64:0x00e1, code:
            r7 = th;
     */
    /* JADX WARNING: Missing block: B:65:0x00e2, code:
            r2 = r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String sendMessage(String message) throws IOException {
        IOException e;
        Exception e2;
        Throwable th;
        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;
        try {
            String response = Events.DEFAULT_SORT_ORDER;
            if (this.client == null) {
                Log.d(TAG, "the client is null");
                return "error";
            }
            try {
                outputStream = this.client.getOutputStream();
                outputStream.write(message.getBytes());
                BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                while (true) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        response = response + line + "\n";
                    } catch (IOException e3) {
                        e = e3;
                        bufferedReader = in;
                    } catch (Exception e4) {
                        e2 = e4;
                        bufferedReader = in;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = in;
                    }
                }
            } catch (IOException e5) {
                e = e5;
                e.printStackTrace();
                response = "error";
                SystemProperties.set("sys.emsvr.opt", "1");
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
                return response;
            } catch (Exception e6) {
                e2 = e6;
                e2.printStackTrace();
                response = "error";
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
                return response;
            }
        } catch (Throwable th3) {
            th = th3;
            throw th;
        }
    }

    public String sendMessage(String message, boolean needState) throws IOException {
        IOException e;
        Exception e2;
        Throwable th;
        OutputStream outputStream = null;
        BufferedReader in = null;
        String response = Events.DEFAULT_SORT_ORDER;
        if (this.client == null) {
            Log.d(TAG, "the client is null");
            return response;
        }
        try {
            outputStream = this.client.getOutputStream();
            outputStream.write(message.getBytes());
            BufferedReader in2 = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            while (true) {
                try {
                    String line = in2.readLine();
                    if (line == null) {
                        if (!needState && response.endsWith("ok\n")) {
                            response = response.substring(0, response.length() - 3);
                        }
                        Log.d(TAG, "response:" + response);
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (in2 != null) {
                            in2.close();
                        }
                        if (this.client != null) {
                            this.client.close();
                            this.client = null;
                        }
                        in = in2;
                    } else {
                        response = response + line + "\n";
                    }
                } catch (IOException e3) {
                    e = e3;
                    in = in2;
                } catch (Exception e4) {
                    e2 = e4;
                    in = in2;
                } catch (Throwable th2) {
                    th = th2;
                    in = in2;
                }
            }
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            if (in != null) {
                in.close();
            }
            if (this.client != null) {
                this.client.close();
                this.client = null;
            }
            return response;
        } catch (Exception e6) {
            e2 = e6;
            try {
                e2.printStackTrace();
                response = "error";
                if (outputStream != null) {
                    outputStream.close();
                }
                if (in != null) {
                    in.close();
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
                return response;
            } catch (Throwable th3) {
                th = th3;
                if (outputStream != null) {
                    outputStream.close();
                }
                if (in != null) {
                    in.close();
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
                throw th;
            }
        }
        return response;
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x0111 A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0116 A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x011d A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f9 A:{SYNTHETIC, Splitter: B:56:0x00f9} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00fe A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0105 A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00cf A:{SYNTHETIC, Splitter: B:42:0x00cf} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d4 A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00db A:{Catch:{ all -> 0x010e }} */
    /* JADX WARNING: Missing block: B:21:0x0073, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String sendMessageReadByte(String message) throws IOException {
        IOException e;
        Exception e2;
        Throwable th;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String response = Events.DEFAULT_SORT_ORDER;
        if (this.client == null) {
            Log.d(TAG, "the client is null");
            return "error";
        }
        try {
            outputStream = this.client.getOutputStream();
            outputStream.write(message.getBytes());
            Log.d(TAG, "read...");
            inputStream = this.client.getInputStream();
            byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer, 0, 1024);
            Log.d(TAG, "length:" + length);
            if (length < 0) {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }
            } else {
                byte[] result = new byte[length];
                System.arraycopy(buffer, 0, result, 0, length);
                String response2 = new String(result);
                try {
                    Log.d(TAG, "receive:" + response2);
                    if (response2.endsWith("\n")) {
                        response = response2.substring(0, response2.length() - 1);
                    } else {
                        response = response2;
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (this.client != null) {
                        this.client.close();
                        this.client = null;
                    }
                } catch (IOException e3) {
                    e = e3;
                    response = response2;
                    e.printStackTrace();
                    response = "error";
                    SystemProperties.set("sys.emsvr.opt", "1");
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (this.client != null) {
                        this.client.close();
                        this.client = null;
                    }
                    return response;
                } catch (Exception e4) {
                    e2 = e4;
                    response = response2;
                    try {
                        e2.printStackTrace();
                        response = "error";
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (this.client != null) {
                            this.client.close();
                            this.client = null;
                        }
                        return response;
                    } catch (Throwable th2) {
                        th = th2;
                        if (outputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        if (this.client != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    response = response2;
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (this.client != null) {
                        this.client.close();
                        this.client = null;
                    }
                    throw th;
                }
            }
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            response = "error";
            SystemProperties.set("sys.emsvr.opt", "1");
            if (outputStream != null) {
            }
            if (inputStream != null) {
            }
            if (this.client != null) {
            }
            return response;
        } catch (Exception e6) {
            e2 = e6;
            e2.printStackTrace();
            response = "error";
            if (outputStream != null) {
            }
            if (inputStream != null) {
            }
            if (this.client != null) {
            }
            return response;
        }
    }
}
