package com.vivo.services.epm.config;

import android.content.ContentValues;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPullParserHelper {
    private static final String TAG = "EPM";

    private static ContentValues getTagAttributesAndValues(XmlPullParser parser) {
        ContentValues cv = new ContentValues();
        try {
            if (parser.getEventType() == 2) {
                int count = parser.getAttributeCount();
                for (int i = 0; i < count; i++) {
                    cv.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return cv;
    }

    private static boolean isSpecialTag(XmlPullParser parser, String[] tags) {
        ContentValues cv = getTagAttributesAndValues(parser);
        for (String tag : tags) {
            if (!cv.containsKey(tag)) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x003c A:{SYNTHETIC, Splitter: B:30:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0054 A:{SYNTHETIC, Splitter: B:44:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0041 A:{SYNTHETIC, Splitter: B:33:0x0041} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<Switch> getSwitchListFromFile(String path) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        FileInputStream fis = null;
        Throwable th2;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                List<Switch> parseSwitchListFromInputStream = parseSwitchListFromInputStream(fis2, path);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return parseSwitchListFromInputStream;
                }
                try {
                    throw th2;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    fis = fis2;
                } catch (IOException e4) {
                    e2 = e4;
                    e2.printStackTrace();
                    return null;
                }
            } catch (Throwable th4) {
                th2 = th4;
                fis = fis2;
                th = null;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Throwable th5) {
                        if (th == null) {
                            th = th5;
                        } else if (th != th5) {
                            th.addSuppressed(th5);
                        }
                    }
                }
                if (th == null) {
                    try {
                        throw th;
                    } catch (FileNotFoundException e5) {
                        e = e5;
                    } catch (IOException e6) {
                        e2 = e6;
                        e2.printStackTrace();
                        return null;
                    }
                }
                throw th2;
            }
        } catch (Throwable th6) {
            th2 = th6;
            th = null;
            if (fis != null) {
            }
            if (th == null) {
            }
        }
        e.printStackTrace();
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x003c A:{SYNTHETIC, Splitter: B:30:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0054 A:{SYNTHETIC, Splitter: B:44:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0041 A:{SYNTHETIC, Splitter: B:33:0x0041} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<StringList> getStringListFromFile(String path) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        FileInputStream fis = null;
        Throwable th2;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                List<StringList> parseStringListFromInputString = parseStringListFromInputString(fis2, path);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return parseStringListFromInputString;
                }
                try {
                    throw th2;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    fis = fis2;
                } catch (IOException e4) {
                    e2 = e4;
                    e2.printStackTrace();
                    return null;
                }
            } catch (Throwable th4) {
                th2 = th4;
                fis = fis2;
                th = null;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Throwable th5) {
                        if (th == null) {
                            th = th5;
                        } else if (th != th5) {
                            th.addSuppressed(th5);
                        }
                    }
                }
                if (th == null) {
                    try {
                        throw th;
                    } catch (FileNotFoundException e5) {
                        e = e5;
                    } catch (IOException e6) {
                        e2 = e6;
                        e2.printStackTrace();
                        return null;
                    }
                }
                throw th2;
            }
        } catch (Throwable th6) {
            th2 = th6;
            th = null;
            if (fis != null) {
            }
            if (th == null) {
            }
        }
        e.printStackTrace();
        return null;
    }

    public static List<StringList> parseStringListFromInputString(InputStream is, String path) {
        Exception e;
        List<StringList> list = new ArrayList();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            try {
                parser.setInput(new InputStreamReader(is));
                StringList sl = null;
                boolean firstFoundRootTag = false;
                while (true) {
                    StringList sl2 = sl;
                    if (parser.getEventType() == 1) {
                        break;
                    }
                    if (parser.getEventType() == 2) {
                        if (!firstFoundRootTag) {
                            firstFoundRootTag = true;
                            if (!StringList.ROOT_TAG.equalsIgnoreCase(parser.getName())) {
                                Log.d(TAG, "list " + path + "'s root tag is invalid");
                                break;
                            }
                        }
                        try {
                            if (StringList.LIST_TAG.equalsIgnoreCase(parser.getName())) {
                                if (isSpecialTag(parser, new String[]{"name"})) {
                                    sl = new StringList(false);
                                    try {
                                        sl.setConfigFilePath(path);
                                        sl.setName(getTagAttributesAndValues(parser).getAsString("name"));
                                    } catch (Exception e2) {
                                        e = e2;
                                        e.printStackTrace();
                                        return list;
                                    }
                                }
                                sl = sl2;
                            } else {
                                sl = sl2;
                            }
                            if (BaseList.STANDARD_LIST_ITEM_TAG.equalsIgnoreCase(parser.getName())) {
                                if (isSpecialTag(parser, new String[]{"value"})) {
                                    sl.addItem(getTagAttributesAndValues(parser).getAsString("value"));
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            sl = sl2;
                            e.printStackTrace();
                            return list;
                        }
                    }
                    sl = sl2;
                    if (parser.getEventType() == 3 && StringList.LIST_TAG.equalsIgnoreCase(parser.getName())) {
                        if (ListConvertHelper.isStringListRepeated(list, sl)) {
                            Log.e(TAG, "list " + sl.getName() + " is repeated in " + path + ", your list is ignored!!!!!!");
                        }
                        list.add(sl);
                    }
                    parser.next();
                }
                return list;
            } catch (Exception e4) {
                e4.printStackTrace();
                return null;
            }
        } catch (Exception e42) {
            e42.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x003c A:{SYNTHETIC, Splitter: B:30:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0054 A:{SYNTHETIC, Splitter: B:44:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0041 A:{SYNTHETIC, Splitter: B:33:0x0041} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<ContentValuesList> getContentValuesListFromFile(String path) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        FileInputStream fis = null;
        Throwable th2;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                List<ContentValuesList> parseContentValuesListFromInputString = parseContentValuesListFromInputString(fis2, path);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return parseContentValuesListFromInputString;
                }
                try {
                    throw th2;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    fis = fis2;
                } catch (IOException e4) {
                    e2 = e4;
                    e2.printStackTrace();
                    return null;
                }
            } catch (Throwable th4) {
                th2 = th4;
                fis = fis2;
                th = null;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Throwable th5) {
                        if (th == null) {
                            th = th5;
                        } else if (th != th5) {
                            th.addSuppressed(th5);
                        }
                    }
                }
                if (th == null) {
                    try {
                        throw th;
                    } catch (FileNotFoundException e5) {
                        e = e5;
                    } catch (IOException e6) {
                        e2 = e6;
                        e2.printStackTrace();
                        return null;
                    }
                }
                throw th2;
            }
        } catch (Throwable th6) {
            th2 = th6;
            th = null;
            if (fis != null) {
            }
            if (th == null) {
            }
        }
        e.printStackTrace();
        return null;
    }

    public static List<ContentValuesList> parseContentValuesListFromInputString(InputStream is, String path) {
        Exception e;
        List<ContentValuesList> list = new ArrayList();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            try {
                parser.setInput(new InputStreamReader(is));
                ContentValuesList cl = null;
                boolean firstFoundRootTag = false;
                while (true) {
                    ContentValuesList cl2 = cl;
                    if (parser.getEventType() == 1) {
                        break;
                    }
                    if (parser.getEventType() == 2) {
                        if (!firstFoundRootTag) {
                            firstFoundRootTag = true;
                            if (!ContentValuesList.ROOT_TAG.equalsIgnoreCase(parser.getName())) {
                                Log.d(TAG, "customlist " + path + "'s root tag is invalid");
                                break;
                            }
                        }
                        try {
                            if (ContentValuesList.LIST_TAG.equalsIgnoreCase(parser.getName())) {
                                if (isSpecialTag(parser, new String[]{"name"})) {
                                    cl = new ContentValuesList(false);
                                    try {
                                        cl.setConfigFilePath(path);
                                        cl.setName(getTagAttributesAndValues(parser).getAsString("name"));
                                    } catch (Exception e2) {
                                        e = e2;
                                        e.printStackTrace();
                                        return list;
                                    }
                                }
                                cl = cl2;
                            } else {
                                cl = cl2;
                            }
                            if (BaseList.STANDARD_LIST_ITEM_TAG.equalsIgnoreCase(parser.getName())) {
                                cl.addItem(getTagAttributesAndValues(parser));
                            }
                        } catch (Exception e3) {
                            e = e3;
                            cl = cl2;
                            e.printStackTrace();
                            return list;
                        }
                    }
                    cl = cl2;
                    if (parser.getEventType() == 3 && ContentValuesList.LIST_TAG.equalsIgnoreCase(parser.getName())) {
                        if (ListConvertHelper.isContentValuesListRepeated(list, cl)) {
                            Log.e(TAG, "list " + cl.getName() + " is repeated in " + path + ", your list is ignored!!!!!!");
                        }
                        list.add(cl);
                    }
                    parser.next();
                }
                return list;
            } catch (Exception e4) {
                e4.printStackTrace();
                return list;
            }
        } catch (Exception e42) {
            e42.printStackTrace();
            return list;
        }
    }

    public static List<Switch> parseSwitchListFromInputStream(InputStream is, String path) {
        List<Switch> list = new ArrayList();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            try {
                parser.setInput(new InputStreamReader(is));
                boolean firstFoundRootTag = false;
                while (parser.getEventType() != 1) {
                    if (parser.getEventType() == 2) {
                        if (!firstFoundRootTag) {
                            firstFoundRootTag = true;
                            if (!Switch.ROOT_TAG.equalsIgnoreCase(parser.getName())) {
                                Log.d(TAG, "switch config file " + path + "'s root tag is invalid");
                                break;
                            }
                        }
                        try {
                            if (Switch.SWITCH_ITEM.equalsIgnoreCase(parser.getName())) {
                                if (isSpecialTag(parser, new String[]{"name", "value"})) {
                                    ContentValues cv = getTagAttributesAndValues(parser);
                                    Switch sw = new Switch(cv.getAsString("name"), Switch.SWITCH_ATTR_VALUE_ON.equalsIgnoreCase(cv.getAsString("value")), path, false);
                                    if (ListConvertHelper.isSwitchRepeated(list, sw)) {
                                        Log.e(TAG, "switch " + sw.getName() + " is repeated in " + path + ", your switch is ignored!!!!!!");
                                    }
                                    list.add(sw);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return list;
                        }
                    }
                    parser.next();
                }
                return list;
            } catch (Exception e2) {
                e2.printStackTrace();
                return list;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            return list;
        }
    }

    public static String safeNextText(XmlPullParser parser) {
        String result = null;
        try {
            result = parser.nextText();
            if (parser.getEventType() != 3) {
                parser.nextTag();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
