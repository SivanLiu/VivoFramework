package com.vivo.content;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.content.Weather.CityWeatherEntry;
import com.vivo.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DynamicWeatherIcon extends DynamicIcon {
    private static boolean DEBUG = true;
    public static final int DEFAULT_DEGREE_TEXT_COLOR = -1;
    public static final int DEFAULT_DEGREE_TEXT_COLOR_SUN = -26600;
    private static final String STR_CENTIGRADE = "℃";
    private static final String STR_FAHRENHEIT = "℉";
    private static final String STR_MINUS = "-";
    private static final String mThemePath = "/data/bbkcore/theme/launcher/iconsize.xml";
    private final String TAG = "DynamicWeatherIcon";
    private final String TAG_BACKGROUND = "background";
    private final String TAG_DEGREELEFOFFSET = "degreeleftoffset";
    private final String TAG_DEGREETEXTCOLOR = "degreetextcolor";
    private final String TAG_DEGREETEXTCOLORCLOUDY = "degreetextcolorcloudy";
    private final String TAG_DEGREETEXTCOLORFOG = "degreetextcolorfog";
    private final String TAG_DEGREETEXTCOLORHAZE = "degreetextcolorhaze";
    private final String TAG_DEGREETEXTCOLOROVERCAST = "degreetextcolorovercast";
    private final String TAG_DEGREETEXTCOLORRAIN = "degreetextcolorrain";
    private final String TAG_DEGREETEXTCOLORSNOW = "degreetextcolorsnow";
    private final String TAG_DEGREETEXTCOLORSUN = "degreetextcolorsun";
    private final String TAG_DEGREETEXTCOLORTHUNDER = "degreetextcolorthunder";
    private final String TAG_DEGREETEXTSIZE = "degreetextsize";
    private final String TAG_DEGREETOPOFFSET = "degreetopoffset";
    private final String TAG_ENABLED = "weatherEnabled";
    private final String TAG_LONGDEGREETEXTSIZE = "longdegreetextsize";
    private final String TAG_TEMPCTOPOFFSET = "tempctopoffset";
    private final String TAG_TEMPLEFTOFFSET = "temperatureleftoffset";
    private final String TAG_TEMPNUMANDCOFFSET = "tempnumandcoffset";
    private final String TAG_TEMPNUMFONTWEIGHT = "tempnumfontweight";
    private final String TAG_TEMPNUMTEXTSIZE = "tempnumtextsize";
    private final String TAG_TEMPOCFONTWEIGHT = "tempocfontweight";
    private final String TAG_TEMPOCTEXTSIZE = "tempoctextsize";
    private final String TAG_TEMPOCTOPDELTA = "tempoctopdelta";
    private final String TAG_TEMPTOPOFFSET = "temperaturetopoffset";
    private final String TAG_THEMERESVERSION = "version";
    private final String TAG_WEATHERLEFTOFFSET = "weatherleftoffset";
    private final String TAG_WEATHERTOPOFFSET = "weathertopoffset";
    private final String WEATHER_AUTHORITY = Weather.AUTHORITY;
    private String mBackground;
    private Bitmap mBackgroundBitmap;
    private int mCurrentDegreeTextColor = DEFAULT_DEGREE_TEXT_COLOR_SUN;
    private int mCurrentIcon = -1;
    private String mCurrentTemp = "-1";
    private int mDegreeLeftOffset = 0;
    private int mDegreeTextColor = -1;
    private int[] mDegreeTextColors = new int[]{DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN, DEFAULT_DEGREE_TEXT_COLOR_SUN};
    private int mDegreeTextSize = 110;
    private int mDegreeTopOffset = 0;
    Handler mHandler;
    private boolean mIsDynamicWeatherEnabled = true;
    private int mLongDegreeTextSize = 110;
    private int mTempLeftOffset = 0;
    private int mTempNumFontWeight = 50;
    private int mTempNumTextSize = 48;
    private int mTempTopOffset = 0;
    private int mTempctopoffset = 0;
    private int mTempnumandcoffset = 0;
    private int mTempoCFontWeight = 40;
    private int mTempoCTextSize = 44;
    private int mTempoctopdelta = 0;
    private int mThemeResVersion = 0;
    private Weather mWeather;
    private int mWeatherLeftOffset = 0;
    private int mWeatherTopOffset = 0;
    private boolean newOrOldIcon = false;

    private class SAXEventHandler extends DefaultHandler {
        private String tempString;

        /* synthetic */ SAXEventHandler(DynamicWeatherIcon this$0, SAXEventHandler -this1) {
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
            if ("degreetextsize".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextSize = Integer.parseInt(valueString);
            } else if ("degreetextcolor".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColor = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColor == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColor = -1;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColor = DynamicWeatherIcon.this.mDegreeTextColor | -16777216;
                }
            } else if ("degreetextcolorsun".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[0] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[0] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[0] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[0] = DynamicWeatherIcon.this.mDegreeTextColors[0] | -16777216;
                }
            } else if ("degreetextcolorcloudy".equals(this.tempString)) {
                DynamicWeatherIcon.this.newOrOldIcon = true;
                DynamicWeatherIcon.this.mDegreeTextColors[1] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[1] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[1] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[1] = DynamicWeatherIcon.this.mDegreeTextColors[1] | -16777216;
                }
            } else if ("degreetextcolorovercast".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[2] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[2] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[2] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[2] = DynamicWeatherIcon.this.mDegreeTextColors[2] | -16777216;
                }
            } else if ("degreetextcolorfog".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[3] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[3] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[3] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[3] = DynamicWeatherIcon.this.mDegreeTextColors[3] | -16777216;
                }
            } else if ("degreetextcolorhaze".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[4] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[4] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[4] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[4] = DynamicWeatherIcon.this.mDegreeTextColors[4] | -16777216;
                }
            } else if ("degreetextcolorrain".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[5] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[5] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[5] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[5] = DynamicWeatherIcon.this.mDegreeTextColors[5] | -16777216;
                }
            } else if ("degreetextcolorthunder".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[6] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[6] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[6] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[6] = DynamicWeatherIcon.this.mDegreeTextColors[6] | -16777216;
                }
            } else if ("degreetextcolorsnow".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTextColors[7] = Integer.parseInt(valueString);
                if (DynamicWeatherIcon.this.mDegreeTextColors[7] == 0) {
                    DynamicWeatherIcon.this.mDegreeTextColors[7] = DynamicWeatherIcon.DEFAULT_DEGREE_TEXT_COLOR_SUN;
                } else {
                    DynamicWeatherIcon.this.mDegreeTextColors[7] = DynamicWeatherIcon.this.mDegreeTextColors[7] | -16777216;
                }
            } else if ("weatherleftoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mWeatherLeftOffset = Integer.parseInt(valueString);
            } else if ("weathertopoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mWeatherTopOffset = Integer.parseInt(valueString);
            } else if ("weatherEnabled".equals(this.tempString)) {
                DynamicWeatherIcon.this.mIsDynamicWeatherEnabled = Boolean.parseBoolean(valueString);
            } else if ("degreeleftoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeLeftOffset = Integer.parseInt(valueString);
            } else if ("degreetopoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mDegreeTopOffset = Integer.parseInt(valueString);
            } else if ("temperatureleftoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempLeftOffset = Integer.parseInt(valueString);
            } else if ("temperaturetopoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempTopOffset = Integer.parseInt(valueString);
            } else if ("background".equals(this.tempString)) {
                if (!valueString.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(DynamicWeatherIcon.this.DYNAMIC_ICON_DIR);
                    builder.append(DynamicWeatherIcon.this.mComponentName.getPackageName());
                    builder.append("/");
                    builder.append(valueString);
                    DynamicWeatherIcon.this.mBackground = builder.toString();
                }
            } else if ("longdegreetextsize".equals(this.tempString)) {
                DynamicWeatherIcon.this.mLongDegreeTextSize = Integer.parseInt(valueString);
            } else if ("version".equals(this.tempString)) {
                DynamicWeatherIcon.this.mThemeResVersion = Integer.parseInt(valueString);
            } else if ("tempnumtextsize".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempNumTextSize = Integer.parseInt(valueString);
            } else if ("tempoctextsize".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempoCTextSize = Integer.parseInt(valueString);
            } else if ("tempnumfontweight".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempNumFontWeight = Integer.parseInt(valueString);
            } else if ("tempnumandcoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempnumandcoffset = Integer.parseInt(valueString);
            } else if ("tempctopoffset".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempctopoffset = Integer.parseInt(valueString);
            } else if ("tempocfontweight".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempoCFontWeight = Integer.parseInt(valueString);
            } else if ("tempoctopdelta".equals(this.tempString)) {
                DynamicWeatherIcon.this.mTempoctopdelta = Integer.parseInt(valueString);
            } else if (DynamicWeatherIcon.DEBUG) {
                Log.w("DynamicWeatherIcon", "unknow tag " + this.tempString + " is found when read theme");
            }
        }
    }

    public DynamicWeatherIcon(ComponentName comp, Context context) {
        this.mComponentName = comp;
        this.mWeather = new Weather(context);
        this.mHandler = new Handler();
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0027 A:{Catch:{ all -> 0x0039 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void parserConfig(File config) {
        Throwable th;
        InputStream instream = null;
        try {
            InputStream instream2 = new FileInputStream(config);
            try {
                SAXParserFactory.newInstance().newSAXParser().parse(instream2, new SAXEventHandler(this, null));
                try {
                    instream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                instream = instream2;
            } catch (Exception e2) {
                instream = instream2;
                try {
                    if (DEBUG) {
                    }
                    try {
                        instream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        instream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                instream = instream2;
                instream.close();
                throw th;
            }
        } catch (Exception e4) {
            if (DEBUG) {
                Log.d("DynamicWeatherIcon", "parser dynamic icon manifest failed!");
            }
            instream.close();
        }
    }

    private Bitmap updateWeatherOld(Context context) {
        String str = null;
        Uri CONTENT_URI = Uri.parse("content://com.vivo.weather.provider/cityorder");
        ContentResolver cr = context.getContentResolver();
        String[] CITYORDER_NEW_PROJECTION = new String[]{"_id", "city", "temp", "background", "orderid"};
        Cursor cursor = null;
        if (!this.mWeather.isLBS()) {
            str = "added=1";
        }
        try {
            cursor = cr.query(CONTENT_URI, CITYORDER_NEW_PROJECTION, str, null, null);
            if (!(cursor == null || cursor.getCount() == 0)) {
                int tempIndex = cursor.getColumnIndexOrThrow("temp");
                int backgroundIndex = cursor.getColumnIndexOrThrow("background");
                int orderIdIndex = cursor.getColumnIndexOrThrow("orderid");
                cursor.moveToFirst();
                int smallestOrderId = cursor.getInt(orderIdIndex);
                do {
                    if (cursor.getInt(orderIdIndex) <= smallestOrderId) {
                        smallestOrderId = cursor.getInt(orderIdIndex);
                        this.mCurrentTemp = cursor.getString(tempIndex);
                        this.mCurrentIcon = cursor.getInt(backgroundIndex);
                    }
                } while (cursor.moveToNext());
                Log.d("DynamicWeatherIcon", "mCurrentTemp: " + this.mCurrentTemp + ", mCurrentIcon: " + this.mCurrentIcon);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DynamicWeatherIcon", "can not query data from weather database");
            this.mCurrentTemp = null;
            this.mCurrentIcon = -1;
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return updateIcon(context);
    }

    private Bitmap updateWeather(Context context) {
        try {
            CityWeatherEntry entry = this.mWeather.getWidgetWeatherInfo(this.mWeather.isLBS(), false);
            this.mCurrentTemp = entry.getCurrentTemp();
            this.mCurrentIcon = entry.getBackground();
            initmCurrentDegreeTextColor();
            return updateIcon(context);
        } catch (NoSuchMethodError e) {
            return updateWeatherOld(context);
        }
    }

    private boolean isOversea() {
        return SystemProperties.get("ro.vivo.product.overseas", "").equals("yes");
    }

    private void initmCurrentDegreeTextColor() {
        if (this.newOrOldIcon) {
            this.mCurrentDegreeTextColor = this.mDegreeTextColors[this.mCurrentIcon];
        } else {
            this.mCurrentDegreeTextColor = this.mDegreeTextColor;
        }
    }

    private Bitmap updateIcon(Context context) {
        if (this.mCurrentIcon == -1 || this.mCurrentTemp == null || "".equals(this.mCurrentTemp)) {
            Log.e("DynamicWeatherIcon", "can not get weather data, so just display defalut icon");
            return null;
        }
        Log.d("DynamicWeatherIcon", "updateIcon, mCurrentTemp: " + this.mCurrentTemp + ", mCurrentIcon: " + this.mCurrentIcon);
        int fahrenheitFlag = isOversea() ? isFahrenheit(context) : 0;
        if (fahrenheitFlag != 0) {
            this.mCurrentTemp = CelsiusToFahrenheit(this.mCurrentTemp);
        }
        String dynamicIconPath = this.DYNAMIC_ICON_DIR;
        new Options().inJustDecodeBounds = true;
        int appSize = context.getResources().getDimensionPixelSize(R.dimen.scene_app_icon_size);
        int width = appSize;
        int height = appSize;
        StringBuilder builder = new StringBuilder();
        Resources res = context.getResources();
        float density = res.getDisplayMetrics().density;
        String[] weatherArray = res.getStringArray(R.array.dynamic_weather_icon_array);
        StringBuilder weatherBgPath = new StringBuilder();
        String resDir = null;
        weatherBgPath.append(dynamicIconPath).append(this.mComponentName.getPackageName()).append("/");
        if (density == 1.5f) {
            resDir = "res/drawable-sw360dp-hdpi/";
        } else if (density == 2.0f) {
            resDir = "res/drawable-sw360dp-xhdpi/";
        } else if (density == 3.0f) {
            resDir = "res/drawable-sw360dp-xxhdpi/";
        } else if (density == 4.0f) {
            resDir = "res/drawable-sw360dp-xxxhdpi/";
        }
        Bitmap originWeatherIcon = BitmapFactory.decodeFile(weatherBgPath.toString() + resDir + weatherArray[this.mCurrentIcon] + ".png");
        if (originWeatherIcon == null) {
            originWeatherIcon = BitmapFactory.decodeFile(weatherBgPath.toString() + weatherArray[this.mCurrentIcon] + ".png");
        }
        if (originWeatherIcon == null) {
            Log.w("DynamicWeatherIcon", "originWeatherIcon is null, return");
            return null;
        }
        Bitmap weatherIcon = Bitmap.createScaledBitmap(originWeatherIcon, appSize, appSize, false);
        String degreePath = dynamicIconPath + this.mComponentName.getPackageName() + "/" + resDir + "dynamic_weather_degree.png";
        String minusPath = dynamicIconPath + this.mComponentName.getPackageName() + "/" + resDir + "dynamic_weather_minus.png";
        Bitmap degreeBmp = BitmapFactory.decodeFile(degreePath);
        if (degreeBmp == null) {
            degreeBmp = BitmapFactory.decodeFile(dynamicIconPath + this.mComponentName.getPackageName() + "/" + "dynamic_weather_degree.png");
        }
        Bitmap minusBmp = BitmapFactory.decodeFile(minusPath);
        if (minusBmp == null) {
            minusBmp = BitmapFactory.decodeFile(dynamicIconPath + this.mComponentName.getPackageName() + "/" + "dynamic_weather_minus.png");
        }
        if (weatherIcon == null || degreeBmp == null || minusBmp == null) {
            return null;
        }
        Typeface type;
        float degreeOffsetLeft;
        float degreeOffsetTop;
        int weatherIconWidth = weatherIcon.getWidth();
        int weatherIconHeight = weatherIcon.getHeight();
        Canvas c = new Canvas();
        Bitmap finalBitmap = Bitmap.createBitmap(weatherIconWidth, weatherIconHeight, Config.ARGB_8888);
        Paint paint = new Paint(1);
        c.setBitmap(finalBitmap);
        c.drawBitmap(weatherIcon, 0.0f, 0.0f, paint);
        boolean isTempMinus = false;
        if (this.mCurrentTemp.startsWith("-")) {
            this.mCurrentTemp = this.mCurrentTemp.substring(1, this.mCurrentTemp.getBytes().length);
            isTempMinus = true;
        }
        try {
            type = Typeface.createFromFile("/system/fonts/HYQiHei-35.ttf");
        } catch (Exception e) {
            Log.e("DynamicWeatherIcon", "can not get 35s font from system, so use launcher internal font");
            type = Typeface.createFromAsset(context.getAssets(), "fonts/HYQiHei-35.ttf");
        }
        paint.setTypeface(type);
        Log.d("DynamicWeatherIcon", "mThemeResVersion:" + this.mThemeResVersion);
        if (this.mThemeResVersion > 1) {
            paint.setColor(this.mCurrentDegreeTextColor);
            paint.getTextBounds(this.mCurrentTemp, 0, this.mCurrentTemp.getBytes().length, new Rect());
            if (isTempMinus) {
                this.mCurrentTemp = "-" + this.mCurrentTemp;
            }
            Bitmap bitmap = getTempBitmap(context, this.mCurrentTemp, Boolean.valueOf(fahrenheitFlag ^ 1));
            degreeOffsetLeft = ((float) ((weatherIconWidth - bitmap.getWidth()) / 2)) + ((float) this.mTempLeftOffset);
            degreeOffsetTop = ((float) ((weatherIconHeight - bitmap.getHeight()) / 2)) + ((float) this.mTempTopOffset);
            Log.d("DynamicWeatherIcon", "density:" + density + ",mDegreeTextSize" + this.mDegreeTextSize + ",isTempMinus:" + isTempMinus + ",mCurrentTemp:" + this.mCurrentTemp + ",mTempLeftOffset" + this.mTempLeftOffset + "degreeOffsetLeft:" + degreeOffsetLeft + ",mTempTopOffset:" + this.mTempTopOffset + ",degreeOffsetTop:" + degreeOffsetTop);
            c.drawBitmap(bitmap, degreeOffsetLeft, degreeOffsetTop, paint);
        }
        Rect textRect;
        if (this.mThemeResVersion == 1) {
            paint.setTextAlign(Align.CENTER);
            paint.setColor(this.mCurrentDegreeTextColor);
            textRect = new Rect();
            paint.getTextBounds(this.mCurrentTemp, 0, this.mCurrentTemp.getBytes().length, textRect);
            if (isTempMinus) {
                this.mCurrentTemp = "-" + this.mCurrentTemp + STR_CENTIGRADE;
            } else {
                this.mCurrentTemp += STR_CENTIGRADE;
            }
            paint.setTextSize((float) this.mDegreeTextSize);
            degreeOffsetLeft = ((float) (weatherIconWidth / 2)) + ((float) this.mTempLeftOffset);
            degreeOffsetTop = ((float) ((textRect.height() + weatherIconHeight) / 2)) + ((float) this.mTempTopOffset);
            Log.d("DynamicWeatherIcon", "density:" + density + ",mDegreeTextSize" + this.mDegreeTextSize + ",isTempMinus:" + isTempMinus + ",mCurrentTemp:" + this.mCurrentTemp + ",mTempLeftOffset" + this.mTempLeftOffset + "degreeOffsetLeft:" + degreeOffsetLeft + ",mTempTopOffset:" + this.mTempTopOffset + ",degreeOffsetTop:" + degreeOffsetTop);
            c.drawText(this.mCurrentTemp, degreeOffsetLeft, degreeOffsetTop, paint);
        } else if (this.mThemeResVersion == 0) {
            paint.setTextSize((float) this.mDegreeTextSize);
            paint.setTextAlign(Align.CENTER);
            paint.setColor(this.mCurrentDegreeTextColor);
            textRect = new Rect();
            paint.getTextBounds(this.mCurrentTemp, 0, this.mCurrentTemp.getBytes().length, textRect);
            degreeOffsetLeft = (float) (weatherIconWidth / 2);
            degreeOffsetTop = (float) ((textRect.height() + weatherIconHeight) / 2);
            float minusLeft = 0.0f;
            if (isTempMinus) {
                minusLeft = (float) (((double) minusBmp.getWidth()) + (((double) density) * 1.5d));
            }
            int degreeCircleLeft = (int) ((((double) ((textRect.width() + weatherIconWidth) / 2)) + (((double) density) * 1.5d)) + ((double) this.mDegreeLeftOffset));
            int degreeCircleTop = ((weatherIconHeight - textRect.height()) / 2) + this.mDegreeTopOffset;
            degreeOffsetLeft += (float) this.mTempLeftOffset;
            degreeOffsetTop += (float) this.mTempTopOffset;
            c.drawText(this.mCurrentTemp, degreeOffsetLeft, degreeOffsetTop, paint);
            c.drawBitmap(degreeBmp, (float) degreeCircleLeft, (float) degreeCircleTop, paint);
            if (isTempMinus) {
                c.drawBitmap(minusBmp, (float) ((int) ((degreeOffsetLeft - minusLeft) - ((float) (textRect.width() / 2)))), degreeOffsetTop - ((float) (textRect.height() / 2)), paint);
            }
        }
        return finalBitmap;
    }

    public Bitmap getTempBitmap(Context context, String temp, Boolean unittype) {
        float symbolWidth;
        Canvas sCanvas = new Canvas();
        Paint tempPaint = new Paint();
        tempPaint.setColor(this.mCurrentDegreeTextColor);
        tempPaint.setAntiAlias(true);
        tempPaint.setTypeface(getTypeface(context, this.mTempNumFontWeight));
        tempPaint.setTextSize((float) this.mTempNumTextSize);
        Rect tempRect = new Rect();
        tempPaint.getTextBounds(temp, 0, temp.length(), tempRect);
        float tempWidth = tempPaint.measureText(temp);
        Typeface oCType = getTypeface(context, this.mTempoCFontWeight);
        Paint symbolPaint = new Paint();
        Rect symbolRect = new Rect();
        symbolPaint.setColor(this.mCurrentDegreeTextColor);
        symbolPaint.setAntiAlias(true);
        symbolPaint.setTypeface(oCType);
        symbolPaint.setTextSize((float) this.mTempoCTextSize);
        if (unittype.booleanValue()) {
            symbolPaint.getTextBounds(STR_CENTIGRADE, 0, STR_CENTIGRADE.length(), symbolRect);
            symbolWidth = symbolPaint.measureText(STR_CENTIGRADE);
        } else {
            symbolPaint.getTextBounds(STR_FAHRENHEIT, 0, STR_FAHRENHEIT.length(), symbolRect);
            symbolWidth = symbolPaint.measureText(STR_FAHRENHEIT);
        }
        Bitmap bitmap = Bitmap.createBitmap((int) ((tempWidth + symbolWidth) + ((float) this.mTempnumandcoffset)), Math.max(tempRect.height(), symbolRect.height()) + this.mTempoctopdelta, Config.ARGB_8888);
        sCanvas.setBitmap(bitmap);
        sCanvas.drawText(temp, 0.0f, (float) (Math.abs(tempRect.top) + this.mTempoctopdelta), tempPaint);
        if (unittype.booleanValue()) {
            sCanvas.drawText(STR_CENTIGRADE, ((float) this.mTempnumandcoffset) + tempPaint.measureText(temp), (float) (Math.abs(symbolRect.top) + this.mTempctopoffset), symbolPaint);
        } else {
            sCanvas.drawText(STR_FAHRENHEIT, ((float) this.mTempnumandcoffset) + tempPaint.measureText(temp), (float) (Math.abs(symbolRect.top) + this.mTempctopoffset), symbolPaint);
        }
        symbolPaint.reset();
        tempPaint.reset();
        sCanvas.save();
        sCanvas.restore();
        return bitmap;
    }

    private boolean isFahrenheit(Context context) {
        boolean isFahrenheit = false;
        Uri localweatherUri = Uri.parse("content://com.vivo.weather.provider/localweather");
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(localweatherUri, new String[]{"temperatureunit"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String temperatureunit = cursor.getString(cursor.getColumnIndex("temperatureunit"));
                if (DEBUG) {
                    Log.d("DynamicWeatherIcon", "temperatureunit = " + temperatureunit);
                }
                isFahrenheit = "1".equals(temperatureunit);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e("DynamicWeatherIcon", "query error: ", e);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (DEBUG) {
            Log.d("DynamicWeatherIcon", "isFahrenheit = " + isFahrenheit);
        }
        return isFahrenheit;
    }

    public String CelsiusToFahrenheit(String value) {
        String Ftemp = "";
        if (TextUtils.isEmpty(value) || (value.contains("N") ^ 1) == 0) {
            Ftemp = "N";
        } else {
            try {
                double FtempDouble = (((double) Integer.parseInt(value)) * 1.8d) + 32.0d;
                if (FtempDouble < 0.0d) {
                    Ftemp = String.valueOf(FtempDouble - 0.5d);
                } else {
                    Ftemp = String.valueOf(FtempDouble + 0.5d);
                }
                if (Ftemp.contains(".")) {
                    Ftemp = Ftemp.substring(0, Ftemp.indexOf("."));
                }
            } catch (NumberFormatException e) {
                Log.d("DynamicWeatherIcon", "CelsiusToFahrenheit error = " + e.getMessage());
                Ftemp = "N";
            }
        }
        if (DEBUG) {
            Log.d("DynamicWeatherIcon", "Ftemp = " + Ftemp);
        }
        return Ftemp;
    }

    protected Bitmap creatDynamicIcon(Context context) {
        if (this.mIsDynamicWeatherEnabled) {
            return updateWeather(context);
        }
        return null;
    }
}
