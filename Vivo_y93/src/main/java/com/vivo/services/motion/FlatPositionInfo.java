package com.vivo.services.motion;

import android.os.SystemProperties;
import android.util.Log;
import com.sensoroperate.SensorTestResult;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FlatPositionInfo {
    private static String MTK_PLATFORM = "MTK";
    private static String PLATFORM_TAG = "ro.vivo.product.solution";
    private static String QCOM_PLATFORM = "QCOM";
    static final String TAG = "FlatPositionInfo";
    static long last_acc_x = 65535;
    static long last_acc_y = 65535;
    static long last_acc_z = 65535;
    private static VivoSensorTest mVivoSensorTest = null;

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a9 A:{SYNTHETIC, Splitter: B:26:0x00a9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readFile(String fileName) {
        FileNotFoundException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                reader2.close();
            } catch (Exception e2) {
                try {
                    Log.d(TAG, "reader.readLine():" + e2.getMessage());
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Log.d(TAG, "the readFile is:" + e1.getMessage());
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e4) {
            e = e4;
            try {
                Log.d(TAG, "the readFile is:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Log.d(TAG, "the readFile is:" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.d(TAG, "the readFile is:" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    FlatPositionInfo() {
        if (SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null) != null && AllConfig.mIsArchADSP) {
            mVivoSensorTest = VivoSensorTest.getInstance();
        }
    }

    public boolean IsDevInFlatState() {
        String prop;
        boolean result = false;
        String data = null;
        float[] mAccSensorVal = new float[3];
        float[] DefBase_digit = new float[3];
        float[] MinBase_digit = new float[3];
        float[] MaxBase_digit = new float[3];
        try {
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            if (AllConfig.mIsArchADSP) {
                SensorTestResult vivo_result = new SensorTestResult();
                int[] arg = new int[]{1};
                if (mVivoSensorTest != null) {
                    if (mVivoSensorTest.vivoSensorTest(49, vivo_result, arg, 1) == 0) {
                        data = "1";
                        vivo_result.getAllTestResult(mAccSensorVal, DefBase_digit, MinBase_digit, MaxBase_digit);
                    } else {
                        vivo_result.getTestResult(mAccSensorVal);
                    }
                }
            } else {
                data = readFile("/sys/bus/platform/drivers/gsensor/data");
            }
        } catch (Exception e) {
            Log.d(TAG, "readFile:" + e.getMessage());
        }
        if (data != null) {
            long acc_z;
            long acc_x = 0;
            long acc_y = 0;
            String[] out = data.split(" ");
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            String PLATFORM_INFO = SystemProperties.get(PLATFORM_TAG);
            if (AllConfig.mIsArchADSP) {
                acc_x = (long) (((int) mAccSensorVal[0]) * 10);
                acc_y = (long) (((int) mAccSensorVal[1]) * 10);
                acc_z = (long) (((int) mAccSensorVal[2]) * 10);
                Log.d(TAG, "++++++++++++acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + acc_z);
            } else if (out != null) {
                try {
                    acc_x = (long) Integer.parseInt(out[0].trim());
                    acc_y = (long) Integer.parseInt(out[1].trim());
                    acc_z = (long) Integer.parseInt(out[2].trim());
                } catch (Exception e2) {
                    Log.d(TAG, "acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + 0);
                    Log.d(TAG, "data error");
                    return false;
                }
            } else {
                Log.d(TAG, "out null");
                return false;
            }
            long comp_sum = ((acc_x * acc_x) + (acc_y * acc_y)) + (acc_z * acc_z);
            if (comp_sum >= 240000000 || comp_sum <= 30000000) {
                last_acc_x = 65535;
                last_acc_y = 65535;
                last_acc_z = 65535;
                result = false;
            } else {
                last_acc_x = acc_x;
                last_acc_y = acc_y;
                last_acc_z = acc_z;
                result = true;
            }
            if (!(!result || last_acc_x == 65535 || last_acc_y == 65535 || last_acc_z == 65535 || (Math.abs(acc_x - last_acc_x) <= 5000 && Math.abs(acc_y - last_acc_y) <= 5000 && Math.abs(acc_z - last_acc_z) <= 5000))) {
                result = false;
            }
            if (!result && Math.abs(acc_x) < 3000 && Math.abs(acc_y) < 3000 && acc_z > -3000) {
                last_acc_x = 65535;
                last_acc_y = 65535;
                last_acc_z = 65535;
                result = true;
            }
            Log.d(TAG, "IsDevInFlatState--" + acc_x + "," + acc_y + "," + acc_z + "," + comp_sum + "," + last_acc_x + "," + last_acc_y + "," + last_acc_z + "," + result);
        } else {
            Log.d(TAG, "IsDevInFlatState Data Error!!");
        }
        return result;
    }

    public static boolean IsProxAcrossInFlatState() {
        String prop;
        String PLATFORM_INFO;
        boolean result = false;
        String data = null;
        float[] mAccSensorVal = new float[3];
        float[] DefBase_digit = new float[3];
        float[] MinBase_digit = new float[3];
        float[] MaxBase_digit = new float[3];
        try {
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            PLATFORM_INFO = SystemProperties.get(PLATFORM_TAG);
            if (AllConfig.mIsArchADSP) {
                SensorTestResult vivo_result = new SensorTestResult();
                int[] arg = new int[]{1};
                if (mVivoSensorTest != null) {
                    if (mVivoSensorTest.vivoSensorTest(49, vivo_result, arg, 1) == 0) {
                        data = "1";
                        vivo_result.getAllTestResult(mAccSensorVal, DefBase_digit, MinBase_digit, MaxBase_digit);
                    } else {
                        vivo_result.getTestResult(mAccSensorVal);
                    }
                }
            } else {
                data = readFile("/sys/bus/platform/drivers/gsensor/data");
            }
        } catch (Exception e) {
            Log.d(TAG, "readFile:" + e.getMessage());
        }
        if (data != null) {
            long acc_z;
            long acc_x = 0;
            long acc_y = 0;
            String[] out = data.split(" ");
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            PLATFORM_INFO = SystemProperties.get(PLATFORM_TAG);
            if (AllConfig.mIsArchADSP) {
                acc_x = (long) (((int) mAccSensorVal[0]) * 10);
                acc_y = (long) (((int) mAccSensorVal[1]) * 10);
                acc_z = (long) (((int) mAccSensorVal[2]) * 10);
                Log.d(TAG, "++++++++++++acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + acc_z);
            } else if (out != null) {
                try {
                    acc_x = (long) Integer.parseInt(out[0].trim());
                    acc_y = (long) Integer.parseInt(out[1].trim());
                    acc_z = (long) Integer.parseInt(out[2].trim());
                } catch (Exception e2) {
                    Log.d(TAG, "acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + 0);
                    Log.d(TAG, "data error");
                    return false;
                }
            } else {
                Log.d(TAG, "out null");
                return false;
            }
            long comp_sum = ((acc_x * acc_x) + (acc_y * acc_y)) + (acc_z * acc_z);
            if (comp_sum >= 240000000 || comp_sum <= 30000000) {
                last_acc_x = 65535;
                last_acc_y = 65535;
                last_acc_z = 65535;
                result = false;
            } else {
                last_acc_x = acc_x;
                last_acc_y = acc_y;
                last_acc_z = acc_z;
                result = true;
            }
            if (!(!result || last_acc_x == 65535 || last_acc_y == 65535 || last_acc_z == 65535)) {
                if (Math.abs(acc_x - last_acc_x) > 5000 || Math.abs(acc_y - last_acc_y) > 5000 || Math.abs(acc_z - last_acc_z) > 5000) {
                    result = false;
                } else if (acc_y < -8000) {
                    result = false;
                } else if (Math.abs(acc_x) > 9500 && Math.abs(acc_y) + Math.abs(acc_z) > 2900) {
                    result = false;
                }
            }
            if (!result && Math.abs(acc_x) < 3000 && Math.abs(acc_y) < 3000 && acc_z > -3000) {
                last_acc_x = 65535;
                last_acc_y = 65535;
                last_acc_z = 65535;
                result = true;
            }
            Log.d(TAG, "IsDevInFlatState--" + acc_x + "," + acc_y + "," + acc_z + "," + comp_sum + "," + last_acc_x + "," + last_acc_y + "," + last_acc_z + "," + result);
        } else {
            Log.d(TAG, "IsProxAcrossInFlatState Data Error!!");
        }
        return result;
    }

    public static boolean IsDevInStaticState() {
        String PLATFORM_INFO;
        String prop;
        boolean result = false;
        String data = null;
        try {
            PLATFORM_INFO = SystemProperties.get(PLATFORM_TAG);
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            if (AllConfig.mIsArchADSP) {
                data = readFile("/sys/class/input/input2/data");
            } else {
                data = readFile("/sys/bus/platform/drivers/gsensor/data");
            }
        } catch (Exception e) {
            Log.d(TAG, "readFile:" + e.getMessage());
        }
        if (data != null) {
            long acc_z;
            long acc_x = 0;
            long acc_y = 0;
            String[] out = data.split(" ");
            PLATFORM_INFO = SystemProperties.get(PLATFORM_TAG);
            prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            if (AllConfig.mIsArchADSP) {
                if (out != null) {
                    try {
                        acc_x = (long) (Integer.parseInt(out[0].trim()) / 100);
                        acc_y = (long) (Integer.parseInt(out[1].trim()) / 100);
                        acc_z = (long) (Integer.parseInt(out[2].trim()) / 100);
                    } catch (Exception e2) {
                        Log.d(TAG, "acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + 0);
                        Log.d(TAG, "data error");
                        return false;
                    }
                }
                Log.d(TAG, "out null");
                return false;
            } else if (out != null) {
                try {
                    acc_x = (long) Integer.parseInt(out[0].trim());
                    acc_y = (long) Integer.parseInt(out[1].trim());
                    acc_z = (long) Integer.parseInt(out[2].trim());
                } catch (Exception e3) {
                    Log.d(TAG, "acc_x" + acc_x + "acc_y" + acc_y + "acc_z" + 0);
                    Log.d(TAG, "data error");
                    return false;
                }
            } else {
                Log.d(TAG, "out null");
                return false;
            }
            long comp_sum = ((acc_x * acc_x) + (acc_y * acc_y)) + (acc_z * acc_z);
            if (comp_sum < 121000000 && comp_sum > 72250000) {
                result = true;
            }
            Log.d(TAG, "IsDevInStaticState--" + acc_x + "," + acc_y + "," + acc_z + "," + comp_sum + "," + result);
        } else {
            Log.d(TAG, "IsDevInStaticState Data Error!!");
        }
        return result;
    }
}
