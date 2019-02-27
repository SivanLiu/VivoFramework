package com.vivo.common.widget;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.Time;
import java.util.HashMap;
import java.util.Locale;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class Lunar {
    public static final int MAX_LUNAR_YEAR = 2050;
    public static final int MIN_LUNAR_YEAR = 1901;
    private static final int START_LUNAR_YEAR = 1901;
    private static final char[] SolarLunarOffsetTable = new char[]{'2', '\'', 29, '/', '#', 25, ',', '!', 22, ')', 30, '1', '%', 26, '-', '#', 23, '*', ' ', '3', '\'', 28, '/', '$', 24, ',', '!', 23, ')', 30, '0', '%', 26, '-', '#', 24, '*', 31, '2', '\'', 27, '.', '$', 25, ',', '!', 22, ')', 29, '0', '%', 27, '-', '\"', 24, '+', 31, '1', '\'', 28, '.', '$', 25, ',', '!', 21, '(', 30, '0', '%', 27, '.', '\"', 23, '*', 31, '1', '&', 28, '/', '$', 25, ',', '!', '3', '(', 29, '0', '%', 27, '.', '#', 23, ')', 31, '2', '&', 28, '/', '$', 24, '+', ' ', 22, '(', 29, '1', '&', 26, '-', '\"', 23, ')', 31, '2', '\'', 28, '/', '$', 25, '+', ' ', 22, ')', 29, '0', '%', 26, ',', '\"', 23, '*', 31, '2', '\'', 28, '.', '!', 23, '*', 31, 21, '(', 29, '/', '$', 25, ',', ' ', 22};
    private static int[] mLunarMonthDays = new int[]{19168, 42352, 21096, 53856, 55632, 27304, 22176, 39632, 19176, 19168, 42200, 42192, 53840, 54600, 46416, 22176, 38608, 38320, 18872, 18864, 42160, 45656, 27216, 27968, 44456, 11104, 38256, 18808, 18800, 25776, 54432, 59984, 27976, 23248, 11104, 37744, 37600, 51560, 51536, 54432, 55888, 46416, 22176, 43736, 9680, 37584, 51544, 43344, 46248, 27808, 46416, 21928, 19872, 42416, 21176, 21168, 43344, 59728, 27296, 44368, 43856, 19296, 42352, 42352, 21088, 59696, 55632, 23208, 22176, 38608, 19176, 19152, 42192, 53864, 53840, 54568, 46400, 46752, 38608, 38320, 18864, 42168, 42160, 45656, 27216, 27968, 44448, 43872, 38256, 18808, 18800, 25776, 27216, 59984, 27432, 23232, 43872, 37736, 37600, 51552, 54440, 54432, 55888, 23208, 22176, 43736, 9680, 37584, 51544, 43344, 46240, 46416, 44368, 21928, 19360, 42416, 21176, 21168, 43312, 29864, 27296, 44368, 19880, 19296, 42352, 42208, 53856, 59696, 54576, 23200, 27472, 38608, 19176, 19152, 42192, 53848, 53840, 54560, 55968, 46496, 22224, 19160, 18864, 42168, 42160, 43600, 46376, 27936, 44448, 21936};
    private static short[] mLunarMonths = new short[]{(short) 0, (short) 80, (short) 4, (short) 0, (short) 32, (short) 96, (short) 5, (short) 0, (short) 32, (short) 112, (short) 5, (short) 0, (short) 64, (short) 2, (short) 6, (short) 0, (short) 80, (short) 3, (short) 7, (short) 0, (short) 96, (short) 4, (short) 0, (short) 32, (short) 112, (short) 5, (short) 0, (short) 48, (short) 128, (short) 6, (short) 0, (short) 64, (short) 3, (short) 7, (short) 0, (short) 80, (short) 4, (short) 8, (short) 0, (short) 96, (short) 4, (short) 10, (short) 0, (short) 96, (short) 5, (short) 0, (short) 48, (short) 128, (short) 5, (short) 0, (short) 64, (short) 2, (short) 7, (short) 0, (short) 80, (short) 4, (short) 9, (short) 0, (short) 96, (short) 4, (short) 0, (short) 32, (short) 96, (short) 5, (short) 0, (short) 48, (short) 112, (short) 6, (short) 0, (short) 80, (short) 2, (short) 7, (short) 0, (short) 80, (short) 3};
    private static int mMaxGeliDateDay = 31;
    private static int mMaxGeliDateMonth = 12;
    private static int mMaxGeliDateYear = 2037;
    private String Year_String = null;
    public String[] iLunarDay = null;
    public String[] iLunarMonth = null;
    private String leapmonth_String = null;
    private String[] mLunarWord = null;
    private Resources mRes = null;
    private HashMap<String, String> mlunarwordMap = null;
    private String month_String = null;

    public class LunarDate {
        public int LeapMonth = Integer.MAX_VALUE;
        public int dayOfMonth = Integer.MAX_VALUE;
        public String mDate = null;
        public int monthOfYear = Integer.MAX_VALUE;
        public int year = Integer.MAX_VALUE;
    }

    public Lunar(Context context) {
        this.mRes = context.getResources();
        initResourcesForChina();
        this.mlunarwordMap = new HashMap();
        for (int i = 0; i < 10; i++) {
            this.mlunarwordMap.put(String.valueOf(i), this.mLunarWord[i]);
        }
    }

    private void initResourcesForChina() {
        Configuration config = this.mRes.getConfiguration();
        Locale originLocale = config.locale;
        config.locale = Locale.CHINA;
        this.mRes.updateConfiguration(config, this.mRes.getDisplayMetrics());
        this.iLunarMonth = this.mRes.getStringArray(50921510);
        this.iLunarDay = this.mRes.getStringArray(50921511);
        this.Year_String = this.mRes.getString(51249265);
        this.month_String = this.mRes.getString(51249266);
        this.leapmonth_String = this.mRes.getString(51249415);
        this.mLunarWord = this.mRes.getStringArray(50921509);
        config.locale = originLocale;
        this.mRes.updateConfiguration(config, this.mRes.getDisplayMetrics());
    }

    public LunarDate CalendarSolarToLundar(int sYear, int sMonth, int sMonthDay) {
        if (sYear < 1901 || sYear > 2050) {
            return null;
        }
        LunarDate mLunarDate = new LunarDate();
        String monthname = null;
        char OffsetDays = GetSolarNewYearOffsetDays(sYear, sMonth + 1, sMonthDay);
        int lYear;
        int LeapMonth;
        int lMonth;
        int OffsetDays2;
        int monthDay;
        if (OffsetDays < SolarLunarOffsetTable[sYear - 1901]) {
            lYear = sYear - 1;
            if (lYear - 1901 < 0) {
                return null;
            }
            LeapMonth = GetLunarLeapMonth(lYear);
            if (LeapMonth == 0) {
                lMonth = 12;
            } else {
                lMonth = 13;
            }
            OffsetDays2 = (SolarLunarOffsetTable[sYear - 1901] - 1) - OffsetDays;
            while (OffsetDays2 >= 0) {
                monthDay = GetLunarMonthDays(lYear, lMonth);
                if (OffsetDays2 < monthDay) {
                    mLunarDate.dayOfMonth = monthDay - OffsetDays2;
                    break;
                }
                OffsetDays2 -= monthDay;
                lMonth--;
            }
            mLunarDate.year = lYear;
            mLunarDate.LeapMonth = LeapMonth;
            if (LeapMonth != 0 && LeapMonth + 1 == lMonth) {
                mLunarDate.monthOfYear = 0;
                monthname = this.leapmonth_String + this.iLunarMonth[mLunarDate.LeapMonth - 1];
            } else if (LeapMonth == 0 || LeapMonth + 1 >= lMonth) {
                mLunarDate.monthOfYear = lMonth;
                monthname = this.iLunarMonth[mLunarDate.monthOfYear - 1];
            } else {
                mLunarDate.monthOfYear = lMonth;
                monthname = this.iLunarMonth[mLunarDate.monthOfYear - 2];
            }
            mLunarDate.mDate = getChineseLunarYear(mLunarDate.year) + this.Year_String + monthname + this.month_String + this.iLunarDay[mLunarDate.dayOfMonth - 1];
        } else {
            lYear = sYear;
            LeapMonth = GetLunarLeapMonth(sYear);
            OffsetDays2 = OffsetDays - (SolarLunarOffsetTable[sYear - 1901] - 1);
            lMonth = 1;
            while (OffsetDays2 > 0) {
                monthDay = GetLunarMonthDays(sYear, lMonth);
                if (OffsetDays2 <= monthDay) {
                    mLunarDate.dayOfMonth = OffsetDays2;
                    break;
                }
                OffsetDays2 -= monthDay;
                lMonth++;
            }
            mLunarDate.year = sYear;
            mLunarDate.LeapMonth = LeapMonth;
            if (LeapMonth == 0) {
                mLunarDate.monthOfYear = lMonth;
                monthname = this.iLunarMonth[mLunarDate.monthOfYear - 1];
            } else if (lMonth == LeapMonth + 1) {
                mLunarDate.monthOfYear = 0;
                monthname = this.leapmonth_String + this.iLunarMonth[mLunarDate.LeapMonth - 1];
            } else if (lMonth < LeapMonth + 1) {
                mLunarDate.monthOfYear = lMonth;
                monthname = this.iLunarMonth[mLunarDate.monthOfYear - 1];
            } else if (lMonth > LeapMonth + 1) {
                mLunarDate.monthOfYear = lMonth - 1;
                monthname = this.iLunarMonth[mLunarDate.monthOfYear - 1];
            }
            mLunarDate.mDate = getChineseLunarYear(mLunarDate.year) + this.Year_String + monthname + this.month_String + this.iLunarDay[mLunarDate.dayOfMonth - 1];
        }
        return mLunarDate;
    }

    public static Time CalendarLundarToSolar(int lYear, int lMonth, int lMonthDay) {
        if (lYear < 1901 || lYear > 2050) {
            return null;
        }
        int i;
        int mMonthDays;
        int OffsetDays = 0;
        int sYear = lYear;
        int sMonth = 0;
        int sMonthDay = 0;
        for (i = 1; i < lMonth; i++) {
            OffsetDays += GetLunarMonthDays(lYear, i);
        }
        OffsetDays = ((OffsetDays + lMonthDay) + SolarLunarOffsetTable[lYear - 1901]) - 1;
        for (i = 1; i <= 12; i++) {
            mMonthDays = GetSolarYearMonthDays(lYear, i);
            if (OffsetDays <= mMonthDays) {
                sMonth = i;
                sMonthDay = OffsetDays;
                break;
            }
            OffsetDays -= mMonthDays;
        }
        if (sMonth == 0) {
            sYear = lYear + 1;
            for (i = 1; i <= 12; i++) {
                mMonthDays = GetSolarYearMonthDays(sYear, i);
                if (OffsetDays <= mMonthDays) {
                    sMonth = i;
                    sMonthDay = OffsetDays;
                    break;
                }
                OffsetDays -= mMonthDays;
            }
        }
        if (sYear > mMaxGeliDateYear) {
            sYear = mMaxGeliDateYear;
            sMonth = mMaxGeliDateMonth;
            sMonthDay = mMaxGeliDateDay;
        }
        Time time = new Time();
        time.year = sYear;
        time.month = sMonth - 1;
        time.monthDay = sMonthDay;
        time.normalize(true);
        return time;
    }

    public static int GetSolarNewYearOffsetDays(int year, int month, int monthDay) {
        int OffsetDays = 0;
        for (int i = 1; i < month; i++) {
            OffsetDays += GetSolarYearMonthDays(year, i);
        }
        return OffsetDays + monthDay;
    }

    public static int GetSolarYearMonthDays(int year, int month) {
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            return 31;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        }
        if (month != 2) {
            return 0;
        }
        if (IsSolarLeapYear(year)) {
            return 29;
        }
        return 28;
    }

    public static boolean IsSolarLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static int GetLunarLeapMonth(int lYear) {
        short LeapMonth = mLunarMonths[(lYear - 1901) / 2];
        if (lYear % 2 == 0) {
            return LeapMonth & 15;
        }
        return (LeapMonth & 240) >> 4;
    }

    public static int GetLunarMonthDays(int lYear, int lMonth) {
        if ((mLunarMonthDays[lYear - 1901] & (GestureConstants.IO_BUFFER_SIZE >> (lMonth - 1))) == 0) {
            return 29;
        }
        return 30;
    }

    public LunarDate getLunarDate(int lYear, int lMonth, int lDay, int LeapMonth, String mDate) {
        LunarDate mLunarDate = new LunarDate();
        mLunarDate.year = lYear;
        mLunarDate.monthOfYear = lMonth;
        mLunarDate.dayOfMonth = lDay;
        mLunarDate.LeapMonth = LeapMonth;
        mLunarDate.mDate = mDate;
        return mLunarDate;
    }

    protected String getChineseLunarYear(int start) {
        String LunarYear;
        if (start / 100 == 19) {
            LunarYear = this.mLunarWord[1] + this.mLunarWord[9];
        } else {
            LunarYear = this.mLunarWord[2] + this.mLunarWord[0];
        }
        return (LunarYear + ((String) this.mlunarwordMap.get(String.valueOf((start % 100) / 10)))) + ((String) this.mlunarwordMap.get(String.valueOf(start % 10)));
    }

    protected String[] getChineseLunarYear(int start, int end) {
        int Size = (end - start) + 1;
        String[] LunarYear = new String[Size];
        for (int i = 0; i < Size; i++) {
            if (start / 100 == 19) {
                LunarYear[i] = this.mLunarWord[1] + this.mLunarWord[9];
            } else {
                LunarYear[i] = this.mLunarWord[2] + this.mLunarWord[0];
            }
            LunarYear[i] = LunarYear[i] + ((String) this.mlunarwordMap.get(String.valueOf((start % 100) / 10)));
            LunarYear[i] = LunarYear[i] + ((String) this.mlunarwordMap.get(String.valueOf(start % 10)));
            start++;
        }
        return LunarYear;
    }
}
