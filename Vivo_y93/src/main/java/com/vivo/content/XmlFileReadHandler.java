package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class XmlFileReadHandler extends DefaultHandler {
    private static final String BBK_THEME = "BBKTHEME";
    private static final String BBK_THEME_ID = "id";
    private static final String BBK_THEME_LOWERCASE = "bbktheme";
    private static final String BBK_THEME_OPEN = "open";
    private static final String BBK_THEME_TITLE = "title";
    static final String BUBLE_SIZE = "bubblesize";
    private static final String CALENDAR_ICON_REPLACE = "calendar";
    private static final String CALENDAR_MONTH_LEFT_OFFSET = "monthleftoffset";
    private static final String CALENDAR_MONTH_SHADOW_COLOR = "monthtextshadowcolor";
    private static final String CALENDAR_MONTH_SHOW = "calendarmonth";
    private static final String CALENDAR_MONTH_TEXTCOLOR = "monthtextcolor";
    private static final String CALENDAR_MONTH_TEXTSIZE = "monthtextsize";
    private static final String CALENDAR_MONTH_TOP_OFFSET = "monthtopoffset";
    private static final String CALENDAR_MONTH_TOP_SHADOW_Y = "monthtextshadowy";
    private static final String CALENDAR_WEEK_LEFT_OFFSET = "weekleftoffset";
    private static final String CALENDAR_WEEK_SHOW = "calendartweek";
    private static final String CALENDAR_WEEK_TEXTCOLOR = "weektextcolor";
    private static final String CALENDAR_WEEK_TEXTSIZE = "weektextsize";
    private static final String CALENDAR_WEEK_TOP_OFFSET = "weektopoffset";
    static final String COLOR_WHEEL = "colorwheel";
    static final String COLOR_WHEEL_CAMERA_COLOR = "cameracolor";
    static final String COLOR_WHEEL_PART_NUMBER = "colorpart";
    public static final String FAVORITE_APP_REFLECTION = "favoritereflection";
    public static final String FAVORITE_APP_REFLECTION_MARGINTOP = "favoritereflectionmargintop";
    public static final String FAVORITE_APP_TITLE_MARGINTOP = "favoritetitlemargintop";
    private static final String FOLDER_ICON_SIZE = "foldericonsize";
    static final String FOLDER_PREVIEW_COLUMN = "folderpreviewcolumn";
    static final String FOLDER_PREVIEW_HEIGHT = "foldericonheight";
    static final String FOLDER_PREVIEW_PADDING = "folderpreviewpadding";
    static final String FOLDER_PREVIEW_ROW = "folderpreviewrow";
    static final String FOLDER_PREVIEW_WIDTH = "foldericonwidth";
    private static final String ICON_ROTATE = "iconrotate";
    private static final String ICON_SIZE = "ICONSIZE";
    private static final String ICON_SIZE_HEIGHT = "height";
    private static final String ICON_SIZE_LEFT_OFFSET = "leftoffset";
    private static final String ICON_SIZE_TOP_OFFSET = "topoffset";
    private static final String ICON_SIZE_WIDTH = "width";
    static final String MAIN_COLOR = "maincolor";
    static final String ROUND_CORNER_X = "filletX";
    static final String ROUND_CORNER_Y = "filletY";
    public static final String SHAPE = "shape";
    static final String VERSION = "version";
    private final String TAG = "XmlFileReadHandler";
    private HashMap<String, String> map;
    private String tempString;

    public HashMap<String, String> getThemeInfo() {
        return this.map;
    }

    public void startDocument() throws SAXException {
        this.map = new HashMap();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.map != null) {
            String valueString = new String(ch, start, length);
            if ("width".equals(this.tempString)) {
                this.map.put("width", valueString);
            } else if ("height".equals(this.tempString)) {
                this.map.put("height", valueString);
            } else if (CALENDAR_ICON_REPLACE.equals(this.tempString)) {
                this.map.put(CALENDAR_ICON_REPLACE, valueString);
            } else if (ICON_SIZE_LEFT_OFFSET.equals(this.tempString)) {
                this.map.put(ICON_SIZE_LEFT_OFFSET, valueString);
            } else if (ICON_SIZE_TOP_OFFSET.equals(this.tempString)) {
                this.map.put(ICON_SIZE_TOP_OFFSET, valueString);
            } else if (FOLDER_ICON_SIZE.equals(this.tempString)) {
                this.map.put(FOLDER_ICON_SIZE, valueString);
            } else if (CALENDAR_WEEK_SHOW.equals(this.tempString)) {
                this.map.put(CALENDAR_WEEK_SHOW, valueString);
            } else if (CALENDAR_WEEK_TEXTSIZE.equals(this.tempString)) {
                this.map.put(CALENDAR_WEEK_TEXTSIZE, valueString);
            } else if (CALENDAR_WEEK_TEXTCOLOR.equals(this.tempString)) {
                this.map.put(CALENDAR_WEEK_TEXTCOLOR, valueString);
            } else if (CALENDAR_WEEK_LEFT_OFFSET.equals(this.tempString)) {
                this.map.put(CALENDAR_WEEK_LEFT_OFFSET, valueString);
            } else if (CALENDAR_WEEK_TOP_OFFSET.equals(this.tempString)) {
                this.map.put(CALENDAR_WEEK_TOP_OFFSET, valueString);
            } else if (CALENDAR_MONTH_SHOW.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_SHOW, valueString);
            } else if (CALENDAR_MONTH_TEXTSIZE.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_TEXTSIZE, valueString);
            } else if (CALENDAR_MONTH_TEXTCOLOR.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_TEXTCOLOR, valueString);
            } else if (CALENDAR_MONTH_LEFT_OFFSET.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_LEFT_OFFSET, valueString);
            } else if (CALENDAR_MONTH_TOP_OFFSET.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_TOP_OFFSET, valueString);
            } else if (CALENDAR_MONTH_SHADOW_COLOR.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_SHADOW_COLOR, valueString);
            } else if (CALENDAR_MONTH_TOP_SHADOW_Y.equals(this.tempString)) {
                this.map.put(CALENDAR_MONTH_TOP_SHADOW_Y, valueString);
            } else if (FAVORITE_APP_REFLECTION.equals(this.tempString)) {
                this.map.put(FAVORITE_APP_REFLECTION, valueString);
            } else if (FAVORITE_APP_TITLE_MARGINTOP.equals(this.tempString)) {
                this.map.put(FAVORITE_APP_TITLE_MARGINTOP, valueString);
            } else if (FAVORITE_APP_REFLECTION_MARGINTOP.equals(this.tempString)) {
                this.map.put(FAVORITE_APP_REFLECTION_MARGINTOP, valueString);
            } else if ("version".equals(this.tempString)) {
                this.map.put("version", valueString);
            } else if (FOLDER_PREVIEW_WIDTH.equals(this.tempString)) {
                this.map.put(FOLDER_PREVIEW_WIDTH, valueString);
            } else if (FOLDER_PREVIEW_HEIGHT.equals(this.tempString)) {
                this.map.put(FOLDER_PREVIEW_HEIGHT, valueString);
            } else if (FOLDER_PREVIEW_COLUMN.equals(this.tempString)) {
                this.map.put(FOLDER_PREVIEW_COLUMN, valueString);
            } else if (FOLDER_PREVIEW_ROW.equals(this.tempString)) {
                this.map.put(FOLDER_PREVIEW_ROW, valueString);
            } else if (FOLDER_PREVIEW_PADDING.equals(this.tempString)) {
                this.map.put(FOLDER_PREVIEW_PADDING, valueString);
            } else if (COLOR_WHEEL.equals(this.tempString)) {
                this.map.put(COLOR_WHEEL, valueString);
            } else if (ROUND_CORNER_X.equals(this.tempString)) {
                this.map.put(ROUND_CORNER_X, valueString);
            } else if (ROUND_CORNER_Y.equals(this.tempString)) {
                this.map.put(ROUND_CORNER_Y, valueString);
            } else if (BUBLE_SIZE.equals(this.tempString)) {
                this.map.put(BUBLE_SIZE, valueString);
            } else if (MAIN_COLOR.equals(this.tempString)) {
                this.map.put(MAIN_COLOR, valueString);
            } else if (COLOR_WHEEL_PART_NUMBER.equals(this.tempString)) {
                this.map.put(COLOR_WHEEL_PART_NUMBER, valueString);
            } else if (COLOR_WHEEL_CAMERA_COLOR.equals(this.tempString)) {
                this.map.put(COLOR_WHEEL_CAMERA_COLOR, valueString);
            } else if (ICON_ROTATE.equals(this.tempString)) {
                this.map.put(ICON_ROTATE, valueString);
            } else if ("title".equals(this.tempString)) {
                this.map.put("title", valueString);
            } else if (BBK_THEME_OPEN.equals(this.tempString)) {
                this.map.put(BBK_THEME_OPEN, valueString);
            } else if (BBK_THEME_ID.equals(this.tempString)) {
                this.map.put(BBK_THEME_ID, valueString);
            } else if (SHAPE.equals(this.tempString)) {
                this.map.put(SHAPE, valueString);
            }
        }
    }

    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (ICON_SIZE.equals(localName) || BBK_THEME_LOWERCASE.equals(localName) || BBK_THEME.equals(localName)) {
            this.map = new HashMap();
        }
        this.tempString = localName;
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        this.tempString = null;
    }
}
