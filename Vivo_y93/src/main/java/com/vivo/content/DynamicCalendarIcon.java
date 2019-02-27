package com.vivo.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.FtBuild;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import com.vivo.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DynamicCalendarIcon extends DynamicIcon {
    public static final int DEFAULT_DATE_TEXT_COLOR = -12698050;
    public static final int DEFAULT_WEEK_TEXT_COLOR = -1;
    public static final int PURE_BLACK_COLOR = -16777216;
    public static final String TAG = "Launcher.DynamicCalendarIcon";
    private final String TAG_BACKGROUND = "background";
    private final String TAG_CALENDAR = "calendar";
    private final String TAG_CALENDARDATE = "calendardate";
    private final String TAG_CALENDARTWEEK = "calendartweek";
    private final String TAG_DATEIMAGE = "dateimage";
    private final String TAG_DATELEFTOFFSET = "dateleftoffset";
    private final String TAG_DATETEXTCOLOR = "datetextcolor";
    private final String TAG_DATETEXTSIZE = "datetextsize";
    private final String TAG_DATETOPOFFSET = "datetopoffset";
    private final String TAG_DEFAULT_CALENDAR = "vivodefault";
    private final String TAG_FONTSWEIGHT = "fontsweight";
    private final String TAG_WEEKLEFTOFFSET = "weekleftoffset";
    private final String TAG_WEEKTEXTCOLOR = "weektextcolor";
    private final String TAG_WEEKTEXTSIZE = "weektextsize";
    private final String TAG_WEEKTOPOFFSET = "weektopoffset";
    private String mBackground;
    private boolean mCalendar = true;
    private boolean mCalendardate = true;
    private boolean mCalendartweek = true;
    private boolean mDateimage;
    private int mDateleftoffset = 0;
    private int mDatetextcolor = DEFAULT_DATE_TEXT_COLOR;
    private int mDatetextsize = 45;
    private int mDatetopoffset = 0;
    private boolean mDefaultCalendar = false;
    private int mFontsweight = 0;
    private int mWeekleftoffset = 0;
    private int mWeektextcolor = -1;
    private int mWeektextsize = 18;
    private int mWeektopoffset = 0;

    private class OldEditionSAXEventHandler extends DefaultHandler {
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
        private String tempString;

        /* synthetic */ OldEditionSAXEventHandler(DynamicCalendarIcon this$0, OldEditionSAXEventHandler -this1) {
            this();
        }

        private OldEditionSAXEventHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            this.tempString = localName;
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            this.tempString = null;
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String valueString = new String(ch, start, length);
            if (CALENDAR_ICON_REPLACE.equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendar = Boolean.parseBoolean(valueString);
            } else if (CALENDAR_WEEK_SHOW.equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendartweek = Boolean.parseBoolean(valueString);
            } else if (CALENDAR_WEEK_TEXTSIZE.equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektextsize = Integer.parseInt(valueString);
            } else if (CALENDAR_WEEK_TEXTCOLOR.equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektextcolor = Integer.parseInt(valueString);
            } else if (CALENDAR_WEEK_LEFT_OFFSET.equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeekleftoffset = Integer.parseInt(valueString);
            } else if (CALENDAR_WEEK_TOP_OFFSET.equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektopoffset = Integer.parseInt(valueString);
            } else if (CALENDAR_MONTH_SHOW.equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendardate = Boolean.parseBoolean(valueString);
            } else if (CALENDAR_MONTH_TEXTSIZE.equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetextsize = Integer.parseInt(valueString);
            } else if (CALENDAR_MONTH_TEXTCOLOR.equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetextcolor = Integer.parseInt(valueString);
                if (DynamicCalendarIcon.this.mDatetextcolor == 0) {
                    DynamicCalendarIcon.this.mDatetextcolor = DynamicCalendarIcon.DEFAULT_DATE_TEXT_COLOR;
                } else {
                    DynamicCalendarIcon.this.mDatetextcolor = DynamicCalendarIcon.this.mDatetextcolor | -16777216;
                }
            } else if (CALENDAR_MONTH_LEFT_OFFSET.equals(this.tempString)) {
                DynamicCalendarIcon.this.mDateleftoffset = Integer.parseInt(valueString);
            } else if (CALENDAR_MONTH_TOP_OFFSET.equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetopoffset = Integer.parseInt(valueString);
            } else {
                Log.w(DynamicCalendarIcon.TAG, "unknow tag " + this.tempString + " is found when read theme");
            }
        }
    }

    private class SAXEventHandler extends DefaultHandler {
        private String tempString;

        /* synthetic */ SAXEventHandler(DynamicCalendarIcon this$0, SAXEventHandler -this1) {
            this();
        }

        private SAXEventHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            this.tempString = localName;
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            this.tempString = null;
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String valueString = new String(ch, start, length);
            if ("calendar".equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendar = Boolean.parseBoolean(valueString);
            } else if ("dateimage".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDateimage = Boolean.parseBoolean(valueString);
            } else if ("calendartweek".equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendartweek = Boolean.parseBoolean(valueString);
            } else if ("weektextsize".equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektextsize = Integer.parseInt(valueString);
            } else if ("weektextcolor".equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektextcolor = Integer.parseInt(valueString);
                if (DynamicCalendarIcon.this.mWeektextcolor == 0) {
                    DynamicCalendarIcon.this.mWeektextcolor = -1;
                } else {
                    DynamicCalendarIcon.this.mWeektextcolor = DynamicCalendarIcon.this.mWeektextcolor | -16777216;
                }
            } else if ("weekleftoffset".equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeekleftoffset = Integer.parseInt(valueString);
            } else if ("weektopoffset".equals(this.tempString)) {
                DynamicCalendarIcon.this.mWeektopoffset = Integer.parseInt(valueString);
            } else if ("calendardate".equals(this.tempString)) {
                DynamicCalendarIcon.this.mCalendardate = Boolean.parseBoolean(valueString);
            } else if ("datetextsize".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetextsize = Integer.parseInt(valueString);
            } else if ("datetextcolor".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetextcolor = Integer.parseInt(valueString);
                if (DynamicCalendarIcon.this.mDatetextcolor == 0) {
                    DynamicCalendarIcon.this.mDatetextcolor = DynamicCalendarIcon.DEFAULT_DATE_TEXT_COLOR;
                } else {
                    DynamicCalendarIcon.this.mDatetextcolor = DynamicCalendarIcon.this.mDatetextcolor | -16777216;
                }
            } else if ("dateleftoffset".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDateleftoffset = Integer.parseInt(valueString);
            } else if ("datetopoffset".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDatetopoffset = Integer.parseInt(valueString);
            } else if ("background".equals(this.tempString)) {
                if (!valueString.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(DynamicCalendarIcon.this.DYNAMIC_ICON_DIR);
                    builder.append(DynamicCalendarIcon.this.mComponentName.getPackageName());
                    builder.append("/");
                    builder.append(valueString);
                    DynamicCalendarIcon.this.mBackground = builder.toString();
                }
            } else if ("vivodefault".equals(this.tempString)) {
                DynamicCalendarIcon.this.mDefaultCalendar = Boolean.parseBoolean(valueString);
            } else if ("fontsweight".equals(this.tempString)) {
                DynamicCalendarIcon.this.mFontsweight = Integer.parseInt(valueString);
            } else {
                Log.w(DynamicCalendarIcon.TAG, "unknow tag " + this.tempString + " is found when read theme");
            }
        }
    }

    DynamicCalendarIcon(ComponentName componetName) {
        this.mComponentName = componetName;
    }

    public Bitmap getIcon(Context context) {
        Bitmap icon = super.getIcon(context);
        if (icon != null) {
            return icon;
        }
        File config = new File(VivoTheme.getThemePath() + "launcher/iconsize.xml");
        if (!config.exists()) {
            return icon;
        }
        parserConfig(config);
        return creatDynamicIcon(context);
    }

    protected void parserConfig(File config) {
        Exception e;
        try {
            InputStream instream = new FileInputStream(config);
            try {
                DefaultHandler handler;
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                String themePath = VivoTheme.getThemePath();
                if (config.getAbsolutePath().equals(themePath + "launcher/iconsize.xml")) {
                    handler = new OldEditionSAXEventHandler(this, null);
                    this.mBackground = themePath + "launcher/calendar_bg.png";
                } else {
                    handler = new SAXEventHandler(this, null);
                }
                parser.parse(instream, handler);
                InputStream inputStream = instream;
            } catch (Exception e2) {
                e = e2;
                Log.d(TAG, "parser dynamic icon manifest failed!", e);
            }
        } catch (Exception e3) {
            e = e3;
            Log.d(TAG, "parser dynamic icon manifest failed!", e);
        }
    }

    protected Bitmap creatDynamicIcon(Context context) {
        if (this.mCalendar || this.mDefaultCalendar) {
            return null;
        }
        Time time = new Time();
        time.set(System.currentTimeMillis());
        String date = String.valueOf(time.monthDay);
        int width = (int) context.getResources().getDimension(R.dimen.scene_app_icon_size);
        int height = (int) context.getResources().getDimension(R.dimen.scene_app_icon_size);
        if (this.mDateimage) {
            Bitmap dateIcon = BitmapFactory.decodeFile(this.DYNAMIC_ICON_DIR + this.mComponentName.getPackageName() + "/" + date + ".png");
            if (dateIcon != null) {
                Bitmap scaledDateIcon = Bitmap.createScaledBitmap(dateIcon, width, height, false);
                if (scaledDateIcon != null) {
                    return scaledDateIcon;
                }
            }
        }
        Resources res = context.getResources();
        int textureSize = (int) res.getDimension(R.dimen.scene_app_icon_size);
        Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize, Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        float denisty = res.getDisplayMetrics().density;
        StringBuilder calendarBgPath = new StringBuilder();
        calendarBgPath.append(this.DYNAMIC_ICON_DIR).append(this.mComponentName.getPackageName()).append("/");
        String path = getDensityResPath(res);
        if (path != null) {
            calendarBgPath.append(path);
        }
        calendarBgPath.append("calendar_bg.png");
        Bitmap b = BitmapFactory.decodeFile(calendarBgPath.toString());
        if (b == null) {
            calendarBgPath = new StringBuilder();
            calendarBgPath.append(this.DYNAMIC_ICON_DIR).append(this.mComponentName.getPackageName()).append("/").append("calendar_bg.png");
            b = BitmapFactory.decodeFile(calendarBgPath.toString());
        }
        if (b == null) {
            return null;
        }
        BitmapDrawable calIcon = new BitmapDrawable(res, Bitmap.createScaledBitmap(b, width, height, false));
        int left = (textureSize - width) / 2;
        int top = (textureSize - height) / 2;
        Rect sOldBounds = new Rect();
        sOldBounds.set(calIcon.getBounds());
        calIcon.setBounds(left, top, left + width, top + height);
        calIcon.draw(canvas);
        calIcon.setBounds(sOldBounds);
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setAntiAlias(true);
        time.set(System.currentTimeMillis());
        int weekHeight = (int) res.getDimension(R.dimen.calendar_icon_week_height);
        int dateHeight = (int) res.getDimension(R.dimen.calendar_icon_date_height);
        String weekDay = DateUtils.getDayOfWeekString(time.weekDay + 1, 10);
        String weekDayShort = DateUtils.getDayOfWeekString(time.weekDay + 1, 30);
        textPaint.setTextSize((float) this.mWeektextsize);
        textPaint.setColor(this.mWeektextcolor);
        float textX = (((float) textureSize) * 0.5f) + ((float) this.mWeekleftoffset);
        float textY = (float) (this.mWeektopoffset + weekHeight);
        if (this.mCalendartweek) {
            Calendar calendar = Calendar.getInstance();
            String countryCode = res.getConfiguration().locale.getCountry();
            if (countryCode.equals("US")) {
                weekDay = weekDay.substring(0, 3).toUpperCase();
            } else if (countryCode.equals("RU")) {
                if (((time.weekDay == 0 ? 1 : 0) | (time.weekDay == 1 ? 1 : 0)) != 0) {
                    weekDay = weekDayShort;
                }
            } else if (countryCode.equals("PT") && time.weekDay == 1) {
                weekDay = weekDayShort;
            }
            canvas.drawText(weekDay, textX, textY, textPaint);
        }
        if (FtBuild.getRomVersion() < 3.0f) {
            textPaint.setTextSize((float) this.mDatetextsize);
            textPaint.setColor(this.mDatetextcolor);
            textX = (((float) textureSize) * 0.5f) + ((float) this.mDateleftoffset);
            textY = (float) (this.mDatetopoffset + dateHeight);
            if (this.mCalendardate) {
                canvas.drawText(date, textX, textY, textPaint);
            }
        } else {
            textPaint.setTextSize((float) this.mDatetextsize);
            textPaint.setColor(this.mDatetextcolor);
            Rect dateTextRect = new Rect();
            Rect bgRect = new Rect();
            textPaint.getTextBounds(date, 0, date.getBytes().length, dateTextRect);
            Bitmap dateBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas c = new Canvas();
            c.setBitmap(dateBitmap);
            textPaint.reset();
            try {
                textPaint.setTypeface(getTypeface(context, this.mFontsweight));
            } catch (Exception e) {
                e.printStackTrace();
            }
            textPaint.setTextSize((float) this.mDatetextsize);
            textPaint.setColor(this.mDatetextcolor);
            textPaint.setAntiAlias(true);
            c.drawText(date, 0.0f, (float) (dateTextRect.height() + 30), textPaint);
            ImageUtil.computeOutlineRect(dateBitmap, dateTextRect);
            ImageUtil.computeOutlineRect(bitmap, bgRect);
            textY = (float) (this.mDatetopoffset + dateHeight);
            float offsetX = (float) ((bgRect.left + (bgRect.width() / 2)) - (dateTextRect.left + (dateTextRect.width() / 2)));
            float offsetY = (float) ((bgRect.top + (bgRect.height() / 2)) - (dateTextRect.top + (dateTextRect.height() / 2)));
            if (this.mCalendartweek) {
                offsetY = ((float) (bgRect.top - dateTextRect.top)) + textY;
            } else {
                offsetY += ((float) this.mDatetopoffset) + (((float) height) * 0.4f);
            }
            offsetY -= (float) dateTextRect.height();
            textPaint.reset();
            textPaint.setTextAlign(Align.CENTER);
            textPaint.setFlags(1);
            if (this.mCalendardate) {
                canvas.drawBitmap(dateBitmap, offsetX, offsetY, textPaint);
            }
            dateBitmap.recycle();
        }
        canvas.setBitmap(null);
        return bitmap;
    }
}
