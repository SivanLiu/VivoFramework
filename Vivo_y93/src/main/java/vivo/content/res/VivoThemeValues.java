package vivo.content.res;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.XmlUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VivoThemeValues {
    private static final boolean DBG = true;
    private static final String TAG = "VivoThemeValues";
    private static final String TYPE_BOOLEAN = "bool";
    private static final String TYPE_COLOR = "color";
    private static final String TYPE_DIMEN = "dimen";
    private static final String TYPE_DRAWABLE = "drawable";
    private static final String TYPE_INTEGER = "integer";
    private static final String TYPE_INTEGER_ARRAY = "integer-array";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_STRING_ARRAY = "string-array";
    public HashMap<Integer, int[]> mIntegerArrays;
    public HashMap<Integer, Integer> mIntegers;
    public HashMap<Integer, String[]> mStringArrays;
    public HashMap<Integer, String> mStrings;
    public ArrayList<ThemeItemInfo> mSystemItems;

    private static class ThemeItemInfo {
        String name;
        String type;
        Object value;

        ThemeItemInfo(String nameStr, String typeStr, Object values) {
            this.name = nameStr;
            this.type = typeStr;
            this.value = values;
        }
    }

    private static void logd(String msg) {
        Log.d(TAG, msg);
    }

    public VivoThemeValues() {
        this.mIntegerArrays = null;
        this.mIntegers = null;
        this.mStringArrays = null;
        this.mStrings = null;
        this.mSystemItems = null;
        this.mIntegerArrays = new HashMap();
        this.mIntegers = new HashMap();
        this.mStringArrays = new HashMap();
        this.mStrings = new HashMap();
        this.mSystemItems = new ArrayList();
    }

    public boolean isEmpty() {
        return (this.mIntegers.isEmpty() && this.mStrings.isEmpty() && this.mIntegerArrays.isEmpty()) ? this.mStringArrays.isEmpty() : false;
    }

    public void putAll(VivoThemeValues themeValues) {
        if (themeValues != null && !themeValues.isEmpty()) {
            this.mIntegers.putAll(themeValues.mIntegers);
            this.mStrings.putAll(themeValues.mStrings);
            this.mIntegerArrays.putAll(themeValues.mIntegerArrays);
            this.mStringArrays.putAll(themeValues.mStringArrays);
        }
    }

    public void clearCache() {
        this.mIntegerArrays.clear();
        this.mIntegers.clear();
        this.mStringArrays.clear();
        this.mStrings.clear();
        this.mSystemItems.clear();
    }

    private static int getIdentifier(Resources res, String resType, String name, String pkg) {
        if (TYPE_INTEGER_ARRAY.equals(resType) || TYPE_STRING_ARRAY.equals(resType)) {
            return res.getIdentifier(name, "array", pkg);
        }
        return res.getIdentifier(name, resType, pkg);
    }

    private static Object parseResourceArrayValue(String resourceType, Element element) {
        NodeList itemList = element.getElementsByTagName("item");
        if (itemList == null) {
            return null;
        }
        int length = itemList.getLength();
        if (length == 0) {
            return null;
        }
        int j;
        if (TYPE_STRING_ARRAY.equals(resourceType)) {
            ArrayList<String> list = new ArrayList(length);
            for (j = 0; j < length; j++) {
                list.add(itemList.item(j).getTextContent());
            }
            return list;
        } else if (!TYPE_INTEGER_ARRAY.equals(resourceType)) {
            return null;
        } else {
            int[] list2 = new int[length];
            for (j = 0; j < length; j++) {
                list2[j] = Integer.valueOf(itemList.item(j).getTextContent()).intValue();
            }
            return list2;
        }
    }

    private static Object parseResourceNonArrayValue(String resType, String value) {
        int i = 0;
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        String str = value.trim();
        if (TYPE_BOOLEAN.equals(resType)) {
            if ("true".equals(str)) {
                i = 1;
            }
            return Integer.valueOf(i);
        } else if (TYPE_COLOR.equals(resType) || TYPE_INTEGER.equals(resType) || TYPE_DRAWABLE.equals(resType)) {
            return Integer.valueOf(XmlUtils.convertValueToUnsignedInt(str, 0));
        } else {
            if (TYPE_DIMEN.equals(resType)) {
                return VivoThemeHelper.parseDimension(str);
            }
            return str;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:87:0x014c A:{SYNTHETIC, Splitter: B:87:0x014c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static VivoThemeValues parseThemeValues(Resources res, InputStream inputStream, String pkg, boolean isSystemResource) {
        Exception e;
        Throwable th;
        if (VivoThemeZipFile.PKG_FRAMEWORK.equals(pkg)) {
            pkg = VivoThemeZipFile.PKG_ANDROID;
        }
        VivoThemeValues themeValues = new VivoThemeValues();
        if (inputStream == null) {
            return themeValues;
        }
        BufferedInputStream bufferedInputStream = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(inputStream, 8192);
            try {
                NodeList childList = builder.parse(bufferedInputStream2).getDocumentElement().getChildNodes();
                if (childList == null) {
                    if (bufferedInputStream2 != null) {
                        try {
                            bufferedInputStream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    return themeValues;
                }
                int length = childList.getLength();
                if (length == 0) {
                    if (bufferedInputStream2 != null) {
                        try {
                            bufferedInputStream2.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    try {
                        inputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                    return themeValues;
                }
                for (int i = length - 1; i >= 0; i--) {
                    Node node = childList.item(i);
                    if (node.getNodeType() == (short) 1) {
                        String name = ((Element) node).getAttribute("name");
                        if (!TextUtils.isEmpty(name)) {
                            String resType = node.getNodeName();
                            int id = getIdentifier(res, resType, name, pkg);
                            if (id > 0) {
                                HashMap map;
                                Object value;
                                if (TYPE_INTEGER_ARRAY.equals(resType)) {
                                    map = themeValues.mIntegerArrays;
                                    value = parseResourceArrayValue(resType, (Element) node);
                                } else if (TYPE_STRING_ARRAY.equals(resType)) {
                                    map = themeValues.mStringArrays;
                                    value = parseResourceArrayValue(resType, (Element) node);
                                } else if (TYPE_STRING.equals(resType)) {
                                    map = themeValues.mStrings;
                                    value = parseResourceNonArrayValue(resType, ((Element) node).getTextContent());
                                } else {
                                    map = themeValues.mIntegers;
                                    value = parseResourceNonArrayValue(resType, ((Element) node).getTextContent());
                                }
                                if (value != null) {
                                    map.put(Integer.valueOf(id), value);
                                    if (isSystemResource) {
                                        if (themeValues.mSystemItems == null) {
                                            themeValues.mSystemItems = new ArrayList();
                                        }
                                        themeValues.mSystemItems.add(new ThemeItemInfo(name, resType, value));
                                    }
                                }
                            }
                        }
                    }
                }
                if (bufferedInputStream2 != null) {
                    try {
                        bufferedInputStream2.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                try {
                    inputStream.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
                bufferedInputStream = bufferedInputStream2;
                return themeValues;
            } catch (Exception e3) {
                e = e3;
                bufferedInputStream = bufferedInputStream2;
            } catch (Throwable th2) {
                th = th2;
                bufferedInputStream = bufferedInputStream2;
                if (bufferedInputStream != null) {
                }
                try {
                    inputStream.close();
                } catch (IOException e2222222) {
                    e2222222.printStackTrace();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            try {
                e.printStackTrace();
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e22222222) {
                        e22222222.printStackTrace();
                    }
                }
                try {
                    inputStream.close();
                } catch (IOException e222222222) {
                    e222222222.printStackTrace();
                }
                return themeValues;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e2222222222) {
                        e2222222222.printStackTrace();
                    }
                }
                inputStream.close();
                throw th;
            }
        }
    }

    public static void mergeValues(VivoThemeValues themeValues, ArrayList<ThemeItemInfo> tobeMerged, Resources res, String pkg) {
        if (tobeMerged != null && !tobeMerged.isEmpty()) {
            for (int i = 0; i < tobeMerged.size(); i++) {
                ThemeItemInfo item = (ThemeItemInfo) tobeMerged.get(i);
                int id = getIdentifier(res, item.type, item.name, pkg);
                if (id > 0) {
                    HashMap map;
                    if (TYPE_INTEGER_ARRAY.equals(item.type)) {
                        map = themeValues.mIntegerArrays;
                    } else if (TYPE_STRING_ARRAY.equals(item.type)) {
                        map = themeValues.mStringArrays;
                    } else if (TYPE_STRING.equals(item.type)) {
                        map = themeValues.mStrings;
                    } else {
                        map = themeValues.mIntegers;
                    }
                    if (!map.containsKey(Integer.valueOf(id))) {
                        map.put(Integer.valueOf(id), item.value);
                    }
                }
            }
        }
    }

    public static void mergeValues(VivoThemeValues themeValues, VivoThemeValues tobeMerged) {
        if (tobeMerged != null) {
            mergeMapValues(themeValues.mIntegers, tobeMerged.mIntegers);
            mergeMapValues(themeValues.mStrings, tobeMerged.mStrings);
            mergeMapValues(themeValues.mIntegerArrays, tobeMerged.mIntegerArrays);
            mergeMapValues(themeValues.mStringArrays, tobeMerged.mStringArrays);
        }
    }

    private static void mergeMapValues(HashMap map, HashMap tobeMerged) {
        if (!tobeMerged.isEmpty()) {
            for (Entry entry : tobeMerged.entrySet()) {
                Integer id = (Integer) entry.getKey();
                if (!map.containsKey(id)) {
                    map.put(id, entry.getValue());
                }
            }
        }
    }
}
