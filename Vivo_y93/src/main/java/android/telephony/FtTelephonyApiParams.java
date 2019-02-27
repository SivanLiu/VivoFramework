package android.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.util.Log;
import com.vivo.services.cipher.utils.Contants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtTelephonyApiParams {
    private static final String API_TAG = "API_TAG";
    public static final String API_TAG_RETURN = "API_TAG_RETURN";
    public static final String RETURN_VALUE_ERROR = "ERROR";
    public static final String RETURN_VALUE_OK = "OK";
    public static final String TAG = "FtTelephonyApiParams";
    private HashMap<String, Object> mValues = new HashMap();

    public FtTelephonyApiParams(String apiTag) {
        if (apiTag == null || apiTag.length() <= 0) {
            throw new IllegalArgumentException("FtTelephonyApiParams()11 error: apiTag=null");
        }
        this.mValues.put(API_TAG, apiTag);
    }

    public FtTelephonyApiParams(FtTelephonyApiParams from) {
        if (from == null || from.getApiTag() == null || from.getApiTag().length() <= 0) {
            throw new IllegalArgumentException("FtTelephonyApiParams()22 error: apiTag=null");
        }
        this.mValues = new HashMap(from.mValues);
    }

    public HashMap<String, Object> getParamValues() {
        return this.mValues;
    }

    public String getApiTag() {
        return getAsString(API_TAG);
    }

    public void setApiTag(String apiTag) {
        if (apiTag != null && apiTag.length() > 0) {
            this.mValues.put(API_TAG, apiTag);
        }
    }

    public boolean equals(Object object) {
        if (object instanceof FtTelephonyApiParams) {
            return this.mValues.equals(((FtTelephonyApiParams) object).mValues);
        }
        return false;
    }

    public int hashCode() {
        return this.mValues.hashCode();
    }

    public void put(String key, String value) {
        this.mValues.put(key, value);
    }

    public void putAll(FtTelephonyApiParams from) {
        if (from == null || from.getApiTag() == null || from.getApiTag().length() <= 0) {
            throw new IllegalArgumentException("putAll() error: apiTag=null");
        }
        this.mValues.putAll(from.mValues);
    }

    public void put(String key, Byte value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Short value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Integer value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Long value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Float value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Double value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Boolean value) {
        this.mValues.put(key, value);
    }

    public void put(String key, byte[] value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Object value) {
        this.mValues.put(key, value);
    }

    public void putNull(String key) {
        this.mValues.put(key, null);
    }

    public int size() {
        return this.mValues.size();
    }

    public void remove(String key) {
        this.mValues.remove(key);
    }

    public void clear() {
        this.mValues.clear();
    }

    public boolean containsKey(String key) {
        return this.mValues.containsKey(key);
    }

    public Object get(String key) {
        return this.mValues.get(key);
    }

    public String getAsString(String key) {
        Object value = this.mValues.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public Long getAsLong(String key) {
        Long valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Long.valueOf(((Number) value).longValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Long.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Long value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Long: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Integer getAsInteger(String key) {
        Integer valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Integer.valueOf(((Number) value).intValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Integer.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Integer value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Integer: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Short getAsShort(String key) {
        Short valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Short.valueOf(((Number) value).shortValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Short.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Short value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Short: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Byte getAsByte(String key) {
        Byte valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Byte.valueOf(((Number) value).byteValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Byte.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Byte value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Byte: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Double getAsDouble(String key) {
        Double valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Double.valueOf(((Number) value).doubleValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Double.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Double value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Double: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Float getAsFloat(String key) {
        Float valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Float.valueOf(((Number) value).floatValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Float.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Float value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Float: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Boolean getAsBoolean(String key) {
        boolean z = false;
        Object value = this.mValues.get(key);
        try {
            return (Boolean) value;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                return Boolean.valueOf(value.toString());
            }
            if (value instanceof Number) {
                if (((Number) value).intValue() != 0) {
                    z = true;
                }
                return Boolean.valueOf(z);
            }
            Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: " + value, e);
            return null;
        }
    }

    public byte[] getAsByteArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }

    public Object getAsObject(String key) {
        return this.mValues.get(key);
    }

    public Set<Entry<String, Object>> valueSet() {
        return this.mValues.entrySet();
    }

    public Set<String> keySet() {
        return this.mValues.keySet();
    }

    public int describeContents() {
        return 0;
    }

    @Deprecated
    public void putStringArrayList(String key, ArrayList<String> value) {
        this.mValues.put(key, value);
    }

    @Deprecated
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList) this.mValues.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : this.mValues.keySet()) {
            String value = getAsString(name);
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(name).append(Contants.QSTRING_EQUAL).append(value);
        }
        return sb.toString();
    }
}
