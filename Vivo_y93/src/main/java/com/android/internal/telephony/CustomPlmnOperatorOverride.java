package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Environment;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class CustomPlmnOperatorOverride {
    private static final String CUSTOM_PLMN_OVERRIDE_PATH = "etc/custom_plmn_operator.xml";
    private static final String LOG_TAG = "CustomPlmnOperatorOverride";
    public static CustomPlmnOperatorOverride mInstance;
    private Context mContext;
    private HashMap<String, OperatorName> mCustomPlmnOperatorMap = new HashMap();
    private Object mLock = new Object();

    public static class OperatorName {
        public String longName = "";
        public String shortName = "";

        public String toString() {
            return "longName:" + this.longName + " shortName:" + this.shortName;
        }
    }

    CustomPlmnOperatorOverride(Context context) {
        this.mContext = context;
        updateOperator();
    }

    public static synchronized void init(Context context) {
        synchronized (CustomPlmnOperatorOverride.class) {
            if (mInstance == null) {
                mInstance = new CustomPlmnOperatorOverride(context);
            }
        }
    }

    public static synchronized CustomPlmnOperatorOverride getInstance() throws Exception {
        CustomPlmnOperatorOverride customPlmnOperatorOverride;
        synchronized (CustomPlmnOperatorOverride.class) {
            if (mInstance == null) {
                throw new Exception("CustomPlmnOperatorOverride is not init");
            }
            customPlmnOperatorOverride = mInstance;
        }
        return customPlmnOperatorOverride;
    }

    public void updateOperator() {
        new Thread(new Runnable() {
            public void run() {
                CustomPlmnOperatorOverride.this.loadOperatorOverrides();
            }
        }).start();
    }

    public boolean containsCarrier(String carrier) {
        String formatPlmn = carrier;
        if (carrier.length() == 5) {
            StringBuilder builder = new StringBuilder(carrier);
            builder.insert(3, '0');
            formatPlmn = builder.toString();
        }
        synchronized (this.mLock) {
            if (this.mCustomPlmnOperatorMap != null) {
                boolean containsKey = this.mCustomPlmnOperatorMap.containsKey(formatPlmn);
                return containsKey;
            }
            return false;
        }
    }

    public OperatorName getOperator(String carrier) {
        if (TextUtils.isEmpty(carrier)) {
            Rlog.e(LOG_TAG, "plmn is empty");
            return null;
        }
        String formatPlmn = carrier;
        if (carrier.length() == 5) {
            StringBuilder builder = new StringBuilder(carrier);
            builder.insert(3, '0');
            formatPlmn = builder.toString();
            Rlog.d(LOG_TAG, "carrier:" + carrier + " formatPlmn:" + formatPlmn);
        }
        OperatorName operatorName = null;
        synchronized (this.mLock) {
            if (this.mCustomPlmnOperatorMap != null) {
                operatorName = (OperatorName) this.mCustomPlmnOperatorMap.get(formatPlmn);
            }
        }
        return operatorName;
    }

    private void loadOperatorOverrides() {
        Rlog.w(LOG_TAG, "loadOperatorOverrides");
        File file = new File(Environment.getRootDirectory(), CUSTOM_PLMN_OVERRIDE_PATH);
        try {
            FileReader localOperatorReader = new FileReader(file);
            try {
                Reader fileReader;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(localOperatorReader);
                XmlUtils.beginDocument(parser, "operators");
                int localVersion = Integer.parseInt(parser.getAttributeValue(null, "version"));
                if (localOperatorReader != null) {
                    try {
                        localOperatorReader.close();
                    } catch (IOException e) {
                    }
                }
                File onLineFile = this.mContext.getFileStreamPath("custom_plmn_operator.xml");
                int onLineVersion = -1;
                if (onLineFile != null && onLineFile.exists()) {
                    try {
                        fileReader = new FileReader(onLineFile);
                        try {
                            XmlPullParser onLineParser = Xml.newPullParser();
                            onLineParser.setInput(fileReader);
                            XmlUtils.beginDocument(onLineParser, "operators");
                            onLineVersion = Integer.parseInt(onLineParser.getAttributeValue(null, "version"));
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e2) {
                                }
                            }
                        } catch (XmlPullParserException e3) {
                            Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e3);
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e4) {
                                }
                            }
                            return;
                        } catch (IOException e5) {
                            Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e5);
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e6) {
                                }
                            }
                            return;
                        } catch (NumberFormatException e7) {
                            Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e7);
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e8) {
                                }
                            }
                            return;
                        } catch (Throwable th) {
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e9) {
                                }
                            }
                        }
                    } catch (FileNotFoundException e10) {
                        Rlog.w(LOG_TAG, "Can not open " + onLineFile.getAbsolutePath());
                        return;
                    }
                }
                Rlog.d(LOG_TAG, "onLineVersion:" + onLineVersion + " localVersion:" + localVersion);
                HashMap<String, OperatorName> customPlmnOperatorMap = new HashMap();
                File file2;
                if (onLineVersion > localVersion) {
                    file2 = this.mContext.getFileStreamPath("custom_plmn_operator.xml");
                    try {
                        fileReader = new FileReader(file2);
                    } catch (FileNotFoundException e11) {
                        Rlog.w(LOG_TAG, "Can not open " + file2.getAbsolutePath());
                        return;
                    }
                }
                file2 = new File(Environment.getRootDirectory(), CUSTOM_PLMN_OVERRIDE_PATH);
                try {
                    fileReader = new FileReader(file2);
                } catch (FileNotFoundException e12) {
                    Rlog.w(LOG_TAG, "Can not open " + file2.getAbsolutePath());
                    return;
                }
                try {
                    parser = Xml.newPullParser();
                    parser.setInput(operatorReader);
                    XmlUtils.beginDocument(parser, "operators");
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (!"operator".equals(parser.getName())) {
                            break;
                        }
                        String carrier = parser.getAttributeValue(null, "carrier");
                        String shortName = parser.getAttributeValue(null, "shortName");
                        String mcc = parser.getAttributeValue(null, "mcc");
                        String mnc = parser.getAttributeValue(null, "mnc");
                        if (TextUtils.isEmpty(carrier) || TextUtils.isEmpty(mnc) || TextUtils.isEmpty(mcc)) {
                            Rlog.w(LOG_TAG, "loadOperatorOverrides is null carrier = " + carrier + " mcc = " + mcc + " mnc = " + mnc);
                        } else {
                            OperatorName operatorName = new OperatorName();
                            if (carrier == null) {
                                carrier = "";
                            }
                            operatorName.longName = carrier;
                            if (shortName == null) {
                                shortName = "";
                            }
                            operatorName.shortName = shortName;
                            for (String value : mnc.split(",")) {
                                if (!TextUtils.isEmpty(value)) {
                                    String formatMnc = value;
                                    if (value.length() == 2) {
                                        StringBuilder builder = new StringBuilder(value);
                                        builder.insert(0, '0');
                                        formatMnc = builder.toString();
                                    }
                                    customPlmnOperatorMap.put(mcc + formatMnc, operatorName);
                                }
                            }
                        }
                    }
                    if (operatorReader != null) {
                        try {
                            operatorReader.close();
                        } catch (IOException e13) {
                        }
                    }
                } catch (XmlPullParserException e32) {
                    Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e32);
                    if (operatorReader != null) {
                        try {
                            operatorReader.close();
                        } catch (IOException e14) {
                        }
                    }
                } catch (IOException e52) {
                    Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e52);
                    if (operatorReader != null) {
                        try {
                            operatorReader.close();
                        } catch (IOException e15) {
                        }
                    }
                } catch (Throwable th2) {
                    if (operatorReader != null) {
                        try {
                            operatorReader.close();
                        } catch (IOException e16) {
                        }
                    }
                }
                if (customPlmnOperatorMap != null && customPlmnOperatorMap.size() > 0) {
                    synchronized (this.mLock) {
                        if (this.mCustomPlmnOperatorMap != null) {
                            this.mCustomPlmnOperatorMap.clear();
                        }
                        this.mCustomPlmnOperatorMap = customPlmnOperatorMap;
                    }
                }
            } catch (XmlPullParserException e322) {
                Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e322);
                if (localOperatorReader != null) {
                    try {
                        localOperatorReader.close();
                    } catch (IOException e17) {
                    }
                }
            } catch (IOException e522) {
                Rlog.w(LOG_TAG, "Exception in operator-conf parser " + e522);
                if (localOperatorReader != null) {
                    try {
                        localOperatorReader.close();
                    } catch (IOException e18) {
                    }
                }
            } catch (Throwable th3) {
                if (localOperatorReader != null) {
                    try {
                        localOperatorReader.close();
                    } catch (IOException e19) {
                    }
                }
            }
        } catch (FileNotFoundException e20) {
            Rlog.w(LOG_TAG, "Can not open " + file.getAbsolutePath());
        }
    }
}
