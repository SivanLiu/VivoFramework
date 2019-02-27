package com.vivo.services.epm;

import com.vivo.services.epm.util.Utils;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public final class RuledMap {
    private static final ArrayList<String> BUILT_IN_KEY = new ArrayList<String>() {
        {
            add(RuledMap.BUILT_IN_KEY_EV);
            add(RuledMap.BUILT_IN_KEY_TIMES);
            add(RuledMap.BUILT_IN_KEY_OSYSVERSION);
            add(RuledMap.BUILT_IN_KEY_MODULE_ID);
        }
    };
    private static final String BUILT_IN_KEY_EV = "ev";
    private static final String BUILT_IN_KEY_MODULE_ID = "moduleid";
    private static final String BUILT_IN_KEY_OSYSVERSION = "osysversion";
    private static final String BUILT_IN_KEY_TIMES = "times";
    private static final String DEFAULT_MUST_KEY = "extype";
    private static final String EPM_EVENT_ID = "00034|012";
    private static final int EPM_MODULE_ID = 1700;
    private static final String EPM_VERSION = "1.0";
    private static final int MAX_MAP_SIZE = 10240;
    private static final int VALUE_MAX_LENGTH = 1024;
    private Set<String> mValidKeys = new HashSet();
    private HashMap<String, String> mValues = new HashMap();

    public static final class InvalidKeyException extends Exception {
        public InvalidKeyException(String s) {
            super(s);
        }
    }

    public static final class InvalidValueException extends Exception {
        public InvalidValueException(String s) {
            super(s);
        }
    }

    public RuledMap(String... keys) throws InvalidKeyException {
        for (String key : keys) {
            this.mValidKeys.add(key);
        }
        for (String key2 : BUILT_IN_KEY) {
            this.mValidKeys.add(key2);
        }
        if (this.mValidKeys.contains(DEFAULT_MUST_KEY)) {
            addBuiltInKeyValue();
            return;
        }
        throw new InvalidKeyException("the RuledMap must have exception type key, kyes=" + keys);
    }

    public final RuledMap addKeyValue(String key, String value) throws InvalidKeyException, InvalidValueException {
        if (!this.mValidKeys.contains(key)) {
            throw new InvalidKeyException("the RuledMap have invalid key, key=" + key);
        } else if (value.length() > VALUE_MAX_LENGTH) {
            throw new InvalidValueException("RuledMap has too big value, key=" + key);
        } else {
            this.mValues.put(key, value);
            return this;
        }
    }

    private void addBuiltInKeyValue() {
        try {
            addKeyValue(BUILT_IN_KEY_EV, String.valueOf("1.0"));
            addKeyValue(BUILT_IN_KEY_TIMES, "0");
            addKeyValue(BUILT_IN_KEY_OSYSVERSION, Utils.getSystemVersion());
            addKeyValue(BUILT_IN_KEY_MODULE_ID, String.valueOf(EPM_MODULE_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        for (Entry entry : this.mValues.entrySet()) {
            try {
                json.put((String) entry.getKey(), (String) entry.getValue());
            } catch (JSONException e) {
            }
        }
        return json;
    }

    public final int size() {
        return toJSONObject().toString().getBytes(Charset.forName("UTF-8")).length;
    }

    public final boolean isValid() {
        return size() < MAX_MAP_SIZE;
    }
}
