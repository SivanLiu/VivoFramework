package vivo.app.backup.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExeCommandUtils {
    private static final String TAG = "ExeCommandUtils";
    private boolean bRunning;
    private boolean bSynchronous;
    private BufferedReader errorResult;
    ReadWriteLock lock;
    private DataOutputStream os;
    private Process process;
    private StringBuffer result;
    private BufferedReader successResult;

    public ExeCommandUtils(boolean synchronous) {
        this.bRunning = false;
        this.lock = new ReentrantReadWriteLock();
        this.result = new StringBuffer();
        this.bSynchronous = synchronous;
    }

    public ExeCommandUtils() {
        this.bRunning = false;
        this.lock = new ReentrantReadWriteLock();
        this.result = new StringBuffer();
        this.bSynchronous = true;
    }

    public boolean isRunning() {
        return this.bRunning;
    }

    public String getResult() {
        Lock readLock = this.lock.readLock();
        readLock.lock();
        try {
            String str = new String(this.result);
            return str;
        } finally {
            readLock.unlock();
        }
    }

    public ExeCommandUtils run(String command, final int maxTime) {
        Log.d(TAG, "run command:" + command + ",maxtime:" + maxTime);
        if (command == null || command.length() == 0) {
            return this;
        }
        try {
            this.process = Runtime.getRuntime().exec("sh");
            this.bRunning = true;
            this.successResult = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            this.errorResult = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
            this.os = new DataOutputStream(this.process.getOutputStream());
            try {
                this.os.write(command.getBytes());
                this.os.writeBytes("\n");
                this.os.flush();
                this.os.writeBytes("exit\n");
                this.os.flush();
                this.os.close();
                if (maxTime > 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep((long) maxTime);
                            } catch (Exception e) {
                            }
                            try {
                                int exitValue = ExeCommandUtils.this.process.exitValue();
                            } catch (IllegalThreadStateException e2) {
                                Log.e(ExeCommandUtils.TAG, "take maxTime,forced to destroy process");
                                ExeCommandUtils.this.process.destroy();
                            }
                        }
                    }).start();
                }
                final Thread t1 = new Thread(new Runnable() {
                    public void run() {
                        Lock writeLock = ExeCommandUtils.this.lock.writeLock();
                        while (true) {
                            try {
                                String line = ExeCommandUtils.this.successResult.readLine();
                                if (line != null) {
                                    line = line + "\n";
                                    writeLock.lock();
                                    ExeCommandUtils.this.result.append(line);
                                    writeLock.unlock();
                                } else {
                                    try {
                                        ExeCommandUtils.this.successResult.close();
                                        return;
                                    } catch (Exception e) {
                                        Log.e(ExeCommandUtils.TAG, "close InputStream exception:" + e.toString());
                                        return;
                                    }
                                }
                            } catch (Exception e2) {
                                Log.e(ExeCommandUtils.TAG, "read InputStream exception:" + e2.toString());
                                try {
                                    ExeCommandUtils.this.successResult.close();
                                    return;
                                } catch (Exception e22) {
                                    Log.e(ExeCommandUtils.TAG, "close InputStream exception:" + e22.toString());
                                    return;
                                }
                            } catch (Throwable th) {
                                try {
                                    ExeCommandUtils.this.successResult.close();
                                } catch (Exception e222) {
                                    Log.e(ExeCommandUtils.TAG, "close InputStream exception:" + e222.toString());
                                }
                                throw th;
                            }
                        }
                    }
                });
                t1.start();
                final Thread t2 = new Thread(new Runnable() {
                    public void run() {
                        Lock writeLock = ExeCommandUtils.this.lock.writeLock();
                        while (true) {
                            try {
                                String line = ExeCommandUtils.this.errorResult.readLine();
                                if (line != null) {
                                    line = line + "\n";
                                    writeLock.lock();
                                    ExeCommandUtils.this.result.append(line);
                                    writeLock.unlock();
                                } else {
                                    try {
                                        ExeCommandUtils.this.errorResult.close();
                                        return;
                                    } catch (Exception e) {
                                        Log.e(ExeCommandUtils.TAG, "read ErrorStream exception:" + e.toString());
                                        return;
                                    }
                                }
                            } catch (Exception e2) {
                                Log.e(ExeCommandUtils.TAG, "read ErrorStream exception:" + e2.toString());
                                try {
                                    ExeCommandUtils.this.errorResult.close();
                                    return;
                                } catch (Exception e22) {
                                    Log.e(ExeCommandUtils.TAG, "read ErrorStream exception:" + e22.toString());
                                    return;
                                }
                            } catch (Throwable th) {
                                try {
                                    ExeCommandUtils.this.errorResult.close();
                                } catch (Exception e222) {
                                    Log.e(ExeCommandUtils.TAG, "read ErrorStream exception:" + e222.toString());
                                }
                                throw th;
                            }
                        }
                    }
                });
                t2.start();
                Thread t3 = new Thread(new Runnable() {
                    public void run() {
                        try {
                            t1.join();
                            t2.join();
                            ExeCommandUtils.this.process.waitFor();
                        } catch (Exception e) {
                        } finally {
                            ExeCommandUtils.this.bRunning = false;
                            Log.d(ExeCommandUtils.TAG, "run command process end");
                        }
                    }
                });
                t3.start();
                if (this.bSynchronous) {
                    t3.join();
                }
            } catch (Exception e) {
                Log.e(TAG, "run command process exception:" + e.toString());
            }
            return this;
        } catch (Exception e2) {
            return this;
        }
    }
}
