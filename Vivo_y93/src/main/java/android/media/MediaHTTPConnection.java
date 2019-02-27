package android.media;

import android.content.IntentFilter;
import android.media.IMediaHTTPConnection.Stub;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.SystemProperties;
import android.os.health.HealthKeys;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MediaHTTPConnection extends Stub {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final int READDATA_TIMEOUT_MS = 30000;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect = true;
    private boolean mAllowCrossProtocolRedirect = true;
    private HttpURLConnection mConnection = null;
    private long mCurrentOffset = -1;
    private Map<String, String> mHeaders = null;
    private InputStream mInputStream = null;
    private long mNativeContext;
    private long mTotalSize = -1;
    private URL mURL = null;

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    public MediaHTTPConnection() {
        if (CookieHandler.getDefault() == null) {
            Log.w(TAG, "MediaHTTPConnection: Unexpected. No CookieHandler found.");
        }
        native_setup();
    }

    public IBinder connect(String uri, String headers) {
        try {
            disconnect();
            this.mAllowCrossDomainRedirect = true;
            this.mURL = new URL(uri);
            this.mHeaders = convertHeaderStringToMap(headers);
            return native_getIMemory();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean parseBoolean(String val) {
        boolean z = true;
        try {
            if (Long.parseLong(val) == 0) {
                z = false;
            }
            return z;
        } catch (NumberFormatException e) {
            if (!"true".equalsIgnoreCase(val)) {
                z = "yes".equalsIgnoreCase(val);
            }
            return z;
        }
    }

    private boolean filterOutInternalHeaders(String key, String val) {
        if (!"android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            return false;
        }
        this.mAllowCrossDomainRedirect = parseBoolean(val);
        this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        return true;
    }

    private Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map = new HashMap();
        for (String pair : headers.split("\r\n")) {
            int colonPos = pair.indexOf(":");
            if (colonPos >= 0) {
                String key = pair.substring(0, colonPos);
                String val = pair.substring(colonPos + 1);
                if (!filterOutInternalHeaders(key, val)) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    public void disconnect() {
        teardownConnection();
        this.mHeaders = null;
        this.mURL = null;
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            if (this.mInputStream != null) {
                try {
                    this.mInputStream.close();
                } catch (IOException e) {
                }
                this.mInputStream = null;
            }
            this.mConnection.disconnect();
            this.mConnection = null;
            this.mCurrentOffset = -1;
        }
    }

    private static final boolean isLocalHost(URL url) {
        if (url == null) {
            return false;
        }
        String host = url.getHost();
        if (host == null) {
            return false;
        }
        try {
            return host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) || NetworkUtils.numericToInetAddress(host).isLoopbackAddress();
        } catch (IllegalArgumentException e) {
        }
    }

    /* JADX WARNING: Missing block: B:39:0x01d0, code:
            if (r29.mAllowCrossDomainRedirect == false) goto L_0x01e2;
     */
    /* JADX WARNING: Missing block: B:40:0x01d2, code:
            r29.mURL = r29.mConnection.getURL();
     */
    /* JADX WARNING: Missing block: B:42:0x01e8, code:
            if (r16 != 206) goto L_0x0334;
     */
    /* JADX WARNING: Missing block: B:43:0x01ea, code:
            r5 = r29.mConnection.getHeaderField("Content-Range");
            r29.mTotalSize = -1;
     */
    /* JADX WARNING: Missing block: B:44:0x01ff, code:
            if (r5 == null) goto L_0x021d;
     */
    /* JADX WARNING: Missing block: B:45:0x0201, code:
            r10 = r5.lastIndexOf(47);
     */
    /* JADX WARNING: Missing block: B:46:0x0209, code:
            if (r10 < 0) goto L_0x021d;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r29.mTotalSize = java.lang.Long.parseLong(r5.substring(r10 + 1));
     */
    /* JADX WARNING: Missing block: B:95:0x033a, code:
            if (r16 == 200) goto L_0x0342;
     */
    /* JADX WARNING: Missing block: B:97:0x0341, code:
            throw new java.io.IOException();
     */
    /* JADX WARNING: Missing block: B:98:0x0342, code:
            r29.mTotalSize = (long) r29.mConnection.getContentLength();
            android.util.Log.d(TAG, "mTotalSize_1=" + r29.mTotalSize);
     */
    /* JADX WARNING: Missing block: B:100:0x0385, code:
            if (r29.mTotalSize > 0) goto L_0x039e;
     */
    /* JADX WARNING: Missing block: B:102:?, code:
            r29.mTotalSize = java.lang.Long.parseLong(r29.mConnection.getHeaderField("Content-Length"));
     */
    /* JADX WARNING: Missing block: B:105:0x03c4, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:106:0x03c5, code:
            android.util.Log.e(TAG, "Couldn't find content-length: " + r7);
            r29.mTotalSize = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void seekTo(long offset) throws IOException {
        teardownConnection();
        int redirectCount = 0;
        try {
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            while (true) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                boolean set_connect_timeout = SystemProperties.get("vivo.set.con.timeout", "unknown").contains("yes");
                boolean set_read_timeout = SystemProperties.get("vivo.set.read.timeout", "unknown").contains("yes");
                Log.d(TAG, "set_connect_timeout is " + set_connect_timeout + ",set_read_timeout is " + set_read_timeout);
                if (set_connect_timeout) {
                    int connect_timeout = Integer.parseInt(SystemProperties.get("vivo.connect.timeout", "4500"));
                    Log.d(TAG, "setting connect timeout to " + connect_timeout);
                    this.mConnection.setConnectTimeout(connect_timeout);
                } else {
                    this.mConnection.setConnectTimeout(HealthKeys.BASE_PROCESS);
                }
                if (set_read_timeout) {
                    int read_timeout = Integer.parseInt(SystemProperties.get("vivo.read.timeout", "4500"));
                    Log.d(TAG, "setting read timeout to " + read_timeout);
                    this.mConnection.setReadTimeout(read_timeout);
                } else if (!noProxy) {
                    this.mConnection.setReadTimeout(HealthKeys.BASE_PROCESS);
                }
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                if (offset > 0) {
                    this.mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                int response = this.mConnection.getResponseCode();
                if (response != 300 && response != 301 && response != 302 && response != 303 && response != 307) {
                    break;
                }
                redirectCount++;
                if (redirectCount > 20) {
                    throw new NoRouteToHostException("Too many redirects: " + redirectCount);
                }
                String method = this.mConnection.getRequestMethod();
                if (response != 307 || (method.equals("GET") ^ 1) == 0 || (method.equals("HEAD") ^ 1) == 0) {
                    String location = this.mConnection.getHeaderField("Location");
                    if (location == null) {
                        throw new NoRouteToHostException("Invalid redirect");
                    }
                    URL url2 = new URL(this.mURL, location);
                    if (url2.getProtocol().equals(IntentFilter.SCHEME_HTTPS) || (url2.getProtocol().equals(IntentFilter.SCHEME_HTTP) ^ 1) == 0) {
                        boolean sameProtocol = this.mURL.getProtocol().equals(url2.getProtocol());
                        if (this.mAllowCrossProtocolRedirect || (sameProtocol ^ 1) == 0) {
                            boolean sameHost = this.mURL.getHost().equals(url2.getHost());
                            if (!this.mAllowCrossDomainRedirect && (sameHost ^ 1) != 0) {
                                throw new NoRouteToHostException("Cross-domain redirects are disallowed");
                            } else if (response != 307) {
                                this.mURL = url2;
                            }
                        } else {
                            throw new NoRouteToHostException("Cross-protocol redirects are disallowed");
                        }
                    }
                    throw new NoRouteToHostException("Unsupported protocol redirect");
                }
                throw new NoRouteToHostException("Invalid redirect");
            }
            if (offset > 0 || response == 206) {
                this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
                this.mCurrentOffset = offset;
            }
            throw new ProtocolException();
            Log.d(TAG, "mTotalSize_2=" + this.mTotalSize);
            if (offset > 0) {
            }
            this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
            this.mCurrentOffset = offset;
        } catch (IOException e) {
            this.mTotalSize = -1;
            teardownConnection();
            this.mCurrentOffset = -1;
            throw e;
        }
    }

    public int readAt(long offset, int size) {
        return native_readAt(offset, size);
    }

    private int readAt(long offset, byte[] data, int size) {
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset) {
                seekTo(offset);
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
            String msg = e.getMessage();
            e.printStackTrace();
            Log.w(TAG, "ProtocolException readAt " + offset + " / " + size + " => " + e);
            if (msg == null || msg.indexOf("unexpected end of stream") == -1) {
                return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
            }
            return -1;
        } catch (NoRouteToHostException e2) {
            e2.printStackTrace();
            Log.w(TAG, "NoRouteToHostException readAt " + offset + " / " + size + " => " + e2);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (UnknownServiceException e3) {
            e3.printStackTrace();
            Log.w(TAG, "UnknownServiceException readAt " + offset + " / " + size + " => " + e3);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (IOException e4) {
            e4.printStackTrace();
            Log.w(TAG, "io exception,readAt " + offset + " / " + size + " => -1");
            return -1;
        } catch (Exception e5) {
            e5.printStackTrace();
            Log.w(TAG, "unknown exception," + e5 + ",readAt " + offset + " / " + size + " => -1");
            return -1;
        }
    }

    public long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    public String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return "application/octet-stream";
            }
        }
        return this.mConnection.getContentType();
    }

    public String getUri() {
        return this.mURL.toString();
    }

    protected void finalize() {
        native_finalize();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
