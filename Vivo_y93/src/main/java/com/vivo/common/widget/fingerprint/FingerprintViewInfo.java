package com.vivo.common.widget.fingerprint;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class FingerprintViewInfo {
    private static final String FINGERPRINTICON = "FingerprintIcon";
    private static final String HEIGHT = "height";
    private static final String ICON_CONFIG = "Config";
    private static final String MODEL = "model";
    private static final String PRODUCT = "product";
    private static final String TAG = "FingerprintViewInfo";
    private static final String WIDTH = "width";
    private static final String X = "x";
    private static final String XML_FILE = "/system/etc/fingerprint_configuration.xml";
    private static final String Y = "y";
    private List<Config> configs = new ArrayList();
    private Config currentConfig = new Config(this.currentProduct, this.currentModel, 445, 1918, 190, 190);
    private String currentModel = SystemProperties.get("persist.sys.fptype", "unknown");
    private String currentProduct = SystemProperties.get("ro.vivo.product.model", "unknown");

    private class Config {
        int height;
        String model;
        String product;
        int width;
        int x;
        int y;

        public Config(String product, String model, int x, int y, int width, int height) {
            this.product = product;
            this.model = model;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public FingerprintViewInfo() {
        readXmlFile();
    }

    private void readXmlFile() {
        try {
            parseXMl(new FileInputStream(new File(XML_FILE)));
        } catch (FileNotFoundException e) {
            Slog.i(TAG, "xml error:", e);
        }
    }

    public int getX() {
        return this.currentConfig.x;
    }

    public int getY() {
        return this.currentConfig.y;
    }

    public int getWidth() {
        return this.currentConfig.width;
    }

    public int getHeight() {
        return this.currentConfig.height;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002a A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0004} */
    /* JADX WARNING: Missing block: B:11:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x002b, code:
            android.util.Slog.i(TAG, "read error:", r0);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseXMl(InputStream in) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(in, "utf-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String nodeName = parser.getName();
                switch (eventType) {
                    case 2:
                        if (!ICON_CONFIG.equals(nodeName)) {
                            break;
                        }
                        addNewConfig(parser);
                        break;
                    case 3:
                        if (!FINGERPRINTICON.equals(nodeName)) {
                            break;
                        }
                        setCurrentConfig();
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
        }
    }

    private void addNewConfig(XmlPullParser parser) {
        String product = parser.getAttributeValue(null, PRODUCT);
        String model = parser.getAttributeValue(null, MODEL);
        int x = getInt(parser.getAttributeValue(null, X));
        int y = getInt(parser.getAttributeValue(null, Y));
        int width = getInt(parser.getAttributeValue(null, WIDTH));
        int height = getInt(parser.getAttributeValue(null, HEIGHT));
        if (this.configs != null) {
            this.configs.add(new Config(product, model, x, y, width, height));
        }
    }

    private int getInt(String value) {
        try {
            if (!TextUtils.isEmpty(value)) {
                return Integer.valueOf(value).intValue();
            }
        } catch (NumberFormatException e) {
            Slog.i(TAG, "number error:", e);
        }
        return 0;
    }

    private void setCurrentConfig() {
        for (Config config : this.configs) {
            if (this.currentModel.equals(config.model) && this.currentProduct.equals(config.product)) {
                this.currentConfig.x = config.x;
                this.currentConfig.y = config.y;
                this.currentConfig.width = config.width;
                this.currentConfig.height = config.height;
                return;
            }
        }
    }
}
