package com.vivo.common.utils;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.Time;
import android.util.Log;
import android.util.MonthDisplayHelper;
import java.util.Arrays;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class Lunar {
    private static final boolean DEBUG = false;
    public static int DefaultTime = 0;
    private static final int END_YEAR = 2050;
    private static final boolean LOCKSCREEN_DEBUG = true;
    public static int LUNAR_DATE_STRING_LENGTH_LONG = 1;
    public static int LUNAR_DATE_STRING_LENGTH_SHORT = 0;
    public static boolean LastDayIsHoliday = false;
    private static boolean LockScreenChangeTimeAfterNow = false;
    private static final int START_YEAR = 1901;
    private static final String TAG = "Lunar";
    private static int Today = 0;
    private static HashMap<String, Integer> hashHoliday = null;
    private static long lastScreenChangeDay = 0;
    private static Context mContext = null;
    private static Lunar mDefaultInstance = null;
    private static int[] mLunarMonthDays = new int[]{19168, 42352, 21096, 53856, 55632, 27304, 22176, 39632, 19176, 19168, 42200, 42192, 53840, 54600, 46416, 22176, 38608, 38320, 18872, 18864, 42160, 45656, 27216, 27968, 44456, 11104, 38256, 18808, 18800, 25776, 54432, 59984, 27976, 23248, 11104, 37744, 37600, 51560, 51536, 54432, 55888, 46416, 22176, 43736, 9680, 37584, 51544, 43344, 46248, 27808, 46416, 21928, 19872, 42416, 21176, 21168, 43344, 59728, 27296, 44368, 43856, 19296, 42352, 42352, 21088, 59696, 55632, 23208, 22176, 38608, 19176, 19152, 42192, 53864, 53840, 54568, 46400, 46752, 38608, 38320, 18864, 42168, 42160, 45656, 27216, 27968, 44448, 43872, 38256, 18808, 18800, 25776, 27216, 59984, 27432, 23232, 43872, 37736, 37600, 51552, 54440, 54432, 55888, 23208, 22176, 43736, 9680, 37584, 51544, 43344, 46240, 46416, 44368, 21928, 19360, 42416, 21176, 21168, 43312, 29864, 27296, 44368, 19880, 19296, 38256, 42208, 53856, 59696, 54576, 23200, 27472, 38608, 19176, 19152, 42192, 53848, 53840, 54560, 55968, 46496, 22224, 19160, 18864, 42168, 42160, 43600, 46376, 27936, 44448, 21936};
    private static short[] mLunarMonths = new short[]{(short) 0, (short) 80, (short) 4, (short) 0, (short) 32, (short) 96, (short) 5, (short) 0, (short) 32, (short) 112, (short) 5, (short) 0, (short) 64, (short) 2, (short) 6, (short) 0, (short) 80, (short) 3, (short) 7, (short) 0, (short) 96, (short) 4, (short) 0, (short) 32, (short) 112, (short) 5, (short) 0, (short) 48, (short) 128, (short) 6, (short) 0, (short) 64, (short) 3, (short) 7, (short) 0, (short) 80, (short) 4, (short) 8, (short) 0, (short) 96, (short) 4, (short) 10, (short) 0, (short) 96, (short) 5, (short) 0, (short) 48, (short) 128, (short) 5, (short) 0, (short) 64, (short) 2, (short) 7, (short) 0, (short) 80, (short) 4, (short) 9, (short) 0, (short) 96, (short) 4, (short) 0, (short) 32, (short) 96, (short) 5, (short) 0, (short) 48, (short) 112, (short) 6, (short) 0, (short) 80, (short) 2, (short) 7, (short) 0, (short) 80, (short) 3};
    private static Resources mRes = null;
    private static short[] mSolarTerms = new short[]{(short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 135, (short) 150, (short) 135, (short) 135, (short) 121, (short) 105, (short) 105, (short) 105, (short) 120, (short) 120, (short) 134, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 135, (short) 150, (short) 135, (short) 135, (short) 121, (short) 105, (short) 105, (short) 105, (short) 120, (short) 120, (short) 134, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 135, (short) 150, (short) 135, (short) 135, (short) 121, (short) 105, (short) 105, (short) 105, (short) 120, (short) 120, (short) 134, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 149, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 151, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 105, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 166, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 165, (short) 151, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 105, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 105, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 135, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 150, (short) 150, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 119, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 166, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 121, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 119, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 121, (short) 121, (short) 121, (short) 105, (short) 120, (short) 120, (short) 150, (short) 165, (short) 166, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 104, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 165, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 150, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 121, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 151, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 181, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 120, (short) 135, (short) 150, (short) 180, (short) 150, (short) 166, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 119, (short) 150, (short) 164, (short) 165, (short) 181, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 120, (short) 121, (short) 120, (short) 105, (short) 120, (short) 119, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 166, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 121, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 180, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 105, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 181, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 120, (short) 135, (short) 150, (short) 180, (short) 165, (short) 181, (short) 165, (short) 166, (short) 135, (short) 136, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 150, (short) 165, (short) 150, (short) 151, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 135, (short) 136, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 166, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 180, (short) 165, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 121, (short) 119, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 150, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 150, (short) 150, (short) 136, (short) 120, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 150, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 120, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 150, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 165, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 150, (short) 165, (short) 195, (short) 165, (short) 181, (short) 165, (short) 166, (short) 135, (short) 136, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 181, (short) 166, (short) 166, (short) 136, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 136, (short) 135, (short) 150, (short) 165, (short) 195, (short) 165, (short) 180, (short) 165, (short) 166, (short) 135, (short) 136, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 179, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 136, (short) 120, (short) 135, (short) 135, (short) 165, (short) 180, (short) 150, (short) 165, (short) 166, (short) 150, (short) 136, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135, (short) 149, (short) 180, (short) 165, (short) 180, (short) 165, (short) 165, (short) 151, (short) 135, (short) 135, (short) 136, (short) 134, (short) 150, (short) 164, (short) 195, (short) 165, (short) 165, (short) 165, (short) 166, (short) 151, (short) 135, (short) 135, (short) 120, (short) 135, (short) 134, (short) 165, (short) 195, (short) 165, (short) 181, (short) 166, (short) 166, (short) 135, (short) 136, (short) 120, (short) 120, (short) 135, (short) 135};
    private static int oldLockSreenId = -1;
    private String[] animalYears = null;
    private String[] celestialStem = null;
    private String[] lunarDayPrefix = null;
    private String[] lunarDaySuffix = null;
    private String[] lunarMonths = null;
    private Integer[] mSimpleGregorianHolidayArray = new Integer[]{Integer.valueOf(50921479), Integer.valueOf(50921481), Integer.valueOf(50921483), Integer.valueOf(50921485), Integer.valueOf(50921487), Integer.valueOf(50921489), Integer.valueOf(50921491), Integer.valueOf(50921493), Integer.valueOf(50921495), Integer.valueOf(50921497), Integer.valueOf(50921499), Integer.valueOf(50921501)};
    private Integer[] mSimpleGregorianHolidayDateArray = new Integer[]{Integer.valueOf(50921480), Integer.valueOf(50921482), Integer.valueOf(50921484), Integer.valueOf(50921486), Integer.valueOf(50921488), Integer.valueOf(50921490), Integer.valueOf(50921492), Integer.valueOf(50921494), Integer.valueOf(50921496), Integer.valueOf(50921498), Integer.valueOf(50921500), Integer.valueOf(50921502)};
    private String[] solarTermStrings = null;
    private String[] terrestrialBranch = null;

    private class DayOfMonthCursor extends MonthDisplayHelper {
        private int mColumn;
        private int mRow;

        public DayOfMonthCursor(int year, int month, int dayOfMonth, int weekStartDay) {
            super(year, month, weekStartDay);
            this.mRow = getRowOf(dayOfMonth);
            this.mColumn = getColumnOf(dayOfMonth);
        }

        public int getSelectedRow() {
            return this.mRow;
        }

        public int getSelectedColumn() {
            return this.mColumn;
        }

        public void setSelectedRowColumn(int row, int col) {
            this.mRow = row;
            this.mColumn = col;
        }

        public int getSelectedDayOfMonth() {
            return getDayAt(this.mRow, this.mColumn);
        }

        public int getSelectedMonthOffset() {
            if (isWithinCurrentMonth(this.mRow, this.mColumn)) {
                return 0;
            }
            if (this.mRow == 0) {
                return -1;
            }
            return 1;
        }

        public void setSelectedDayOfMonth(int dayOfMonth) {
            this.mRow = getRowOf(dayOfMonth);
            this.mColumn = getColumnOf(dayOfMonth);
        }

        public boolean isSelected(int row, int column) {
            return (this.mRow == row && this.mColumn == column) ? Lunar.LOCKSCREEN_DEBUG : false;
        }

        public boolean up() {
            if (isWithinCurrentMonth(this.mRow - 1, this.mColumn)) {
                this.mRow--;
                return false;
            }
            previousMonth();
            this.mRow = 5;
            while (!isWithinCurrentMonth(this.mRow, this.mColumn)) {
                this.mRow--;
            }
            return Lunar.LOCKSCREEN_DEBUG;
        }

        public boolean down() {
            if (isWithinCurrentMonth(this.mRow + 1, this.mColumn)) {
                this.mRow++;
                return false;
            }
            nextMonth();
            this.mRow = 0;
            while (!isWithinCurrentMonth(this.mRow, this.mColumn)) {
                this.mRow++;
            }
            return Lunar.LOCKSCREEN_DEBUG;
        }

        public boolean left() {
            if (this.mColumn == 0) {
                this.mRow--;
                this.mColumn = 6;
            } else {
                this.mColumn--;
            }
            if (isWithinCurrentMonth(this.mRow, this.mColumn)) {
                return false;
            }
            previousMonth();
            int lastDay = getNumberOfDaysInMonth();
            this.mRow = getRowOf(lastDay);
            this.mColumn = getColumnOf(lastDay);
            return Lunar.LOCKSCREEN_DEBUG;
        }

        public boolean right() {
            if (this.mColumn == 6) {
                this.mRow++;
                this.mColumn = 0;
            } else {
                this.mColumn++;
            }
            if (isWithinCurrentMonth(this.mRow, this.mColumn)) {
                return false;
            }
            nextMonth();
            this.mRow = 0;
            this.mColumn = 0;
            while (!isWithinCurrentMonth(this.mRow, this.mColumn)) {
                this.mColumn++;
            }
            return Lunar.LOCKSCREEN_DEBUG;
        }
    }

    private boolean isLeapYear(int year) {
        if (year % 400 != 0) {
            return (year % 4 != 0 || year % 100 == 0) ? false : LOCKSCREEN_DEBUG;
        } else {
            return LOCKSCREEN_DEBUG;
        }
    }

    private boolean isLeapMonth(int iLunarYear, int iLunarMonth, int[] iGregorianDate) {
        int[] curLunarDate = new int[3];
        short tmp = LunarMonthDays(iLunarYear, iLunarMonth);
        int days = (((tmp >> 8) & 255) + 0) + (tmp & 255);
        long iSpanDays = CalcDateDiff(new int[]{1901, 1, 1}, iGregorianDate);
        CalcLunarDate(iSpanDays, curLunarDate);
        int LunarMonth1 = curLunarDate[1];
        CalcLunarDate(((long) days) + iSpanDays, curLunarDate);
        return curLunarDate[1] - LunarMonth1 != 1 ? LOCKSCREEN_DEBUG : false;
    }

    private int getLeapMonth(int iLunarYear) {
        short flag = mLunarMonths[(iLunarYear - 1901) / 2];
        return (iLunarYear + -1901) % 2 != 0 ? flag & 15 : flag >> 4;
    }

    private short LunarMonthDays(int iLunarYear, int iLunarMonth) {
        if (iLunarYear < 1901) {
            return (short) 30;
        }
        int high = 0;
        int low = 29;
        int iBit = 16 - iLunarMonth;
        int leapMonth = getLeapMonth(iLunarYear);
        if (leapMonth != 0 && iLunarMonth > leapMonth) {
            iBit--;
        }
        if ((mLunarMonthDays[iLunarYear - 1901] & (1 << iBit)) != 0) {
            low = 30;
        }
        if (iLunarMonth == leapMonth) {
            if ((mLunarMonthDays[iLunarYear - 1901] & (1 << (iBit - 1))) != 0) {
                high = 30;
            } else {
                high = 29;
            }
        }
        return (short) (((short) low) | (((short) high) << 8));
    }

    private int LunarYearDays(int iLunarYear) {
        int days = 0;
        for (int i = 1; i <= 12; i++) {
            short tmp = LunarMonthDays(iLunarYear, i);
            days = (days + ((tmp >> 8) & 255)) + (tmp & 255);
        }
        return days;
    }

    private long CalcDateDiff(int[] startDate, int[] endDate) {
        int startYear = startDate[0];
        int startMonth = startDate[1];
        int startDay = startDate[2];
        int endYear = endDate[0];
        int endMonth = endDate[1];
        int endDay = endDate[2];
        int[] monthday = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        long iDiffDays = ((((long) ((endYear - startYear) * 365)) + ((long) (((endYear - 1) / 4) - ((startYear - 1) / 4)))) - ((long) (((endYear - 1) / 100) - ((startYear - 1) / 100)))) + ((long) (((endYear - 1) / 400) - ((startYear - 1) / 400)));
        int i = monthday[endMonth - 1];
        int i2 = (!isLeapYear(endYear) || endMonth <= 2) ? 0 : 1;
        iDiffDays = (iDiffDays + ((long) (i2 + i))) + ((long) endDay);
        i = monthday[startMonth - 1];
        i2 = (!isLeapYear(startYear) || startMonth <= 2) ? 0 : 1;
        return (iDiffDays - ((long) (i2 + i))) - ((long) startDay);
    }

    private void CalcLunarDate(long iSpanDays, int[] lunarDate) {
        if (iSpanDays < 49) {
            lunarDate[0] = 1900;
            if (iSpanDays < 19) {
                lunarDate[1] = 11;
                lunarDate[2] = (int) (11 + iSpanDays);
            } else {
                lunarDate[1] = 12;
                lunarDate[2] = ((int) iSpanDays) - 18;
            }
            return;
        }
        long tmp;
        iSpanDays -= 49;
        lunarDate[0] = 1901;
        lunarDate[1] = 1;
        lunarDate[2] = 1;
        int LunarYearDays = LunarYearDays(lunarDate[0]);
        while (true) {
            tmp = (long) LunarYearDays;
            if (iSpanDays < tmp) {
                break;
            }
            iSpanDays -= tmp;
            LunarYearDays = lunarDate[0] + 1;
            lunarDate[0] = LunarYearDays;
            LunarYearDays = LunarYearDays(LunarYearDays);
        }
        LunarYearDays = LunarMonthDays(lunarDate[0], lunarDate[1]);
        while (true) {
            tmp = (long) (LunarYearDays & 255);
            if (iSpanDays < tmp) {
                break;
            }
            iSpanDays -= tmp;
            if (lunarDate[1] == getLeapMonth(lunarDate[0])) {
                tmp = (long) ((LunarMonthDays(lunarDate[0], lunarDate[1]) >> 8) & 255);
                if (iSpanDays < tmp) {
                    break;
                }
                iSpanDays -= tmp;
            }
            LunarYearDays = lunarDate[0];
            int i = lunarDate[1] + 1;
            lunarDate[1] = i;
            LunarYearDays = LunarMonthDays(LunarYearDays, i);
        }
        lunarDate[2] = (int) (((long) lunarDate[2]) + iSpanDays);
    }

    private int getSolarTermIndex(int[] gregorian_date) {
        int day;
        int i = 1;
        short flag = mSolarTerms[(((gregorian_date[0] - 1901) * 12) + gregorian_date[1]) - 1];
        if (gregorian_date[2] < 15) {
            day = 15 - ((flag >> 4) & 15);
        } else {
            day = (flag & 15) + 15;
        }
        if (gregorian_date[2] != day) {
            return 0;
        }
        int i2 = ((gregorian_date[1] - 1) * 2) + 1;
        if (gregorian_date[2] <= 15) {
            i = 0;
        }
        return i + i2;
    }

    public Lunar() {
        mRes = Resources.getSystem();
        init(mRes);
        this.celestialStem = mRes.getStringArray(50921472);
        this.terrestrialBranch = mRes.getStringArray(50921473);
        this.animalYears = mRes.getStringArray(50921474);
        this.lunarDayPrefix = mRes.getStringArray(50921476);
        this.lunarDaySuffix = mRes.getStringArray(50921477);
        this.lunarMonths = mRes.getStringArray(50921475);
        this.solarTermStrings = mRes.getStringArray(50921478);
    }

    public static Lunar getDefault() {
        if (mDefaultInstance == null) {
            mDefaultInstance = new Lunar();
            Log.v(TAG, "===============getDefault");
        }
        return mDefaultInstance;
    }

    public int[] getLunarDate(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = new int[3];
        CalcLunarDate(CalcDateDiff(new int[]{1901, 1, 1}, new int[]{gregorian_year, gregorian_month, gregorian_day}), lunarDate);
        return lunarDate;
    }

    public int[] getGregorianDate(int lunar_year, int lunar_month, int lunar_day) {
        return null;
    }

    public String getLunarYear(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        StringBuilder sb = new StringBuilder();
        sb.append(this.celestialStem[lunarDate[0] % 10]);
        sb.append(this.terrestrialBranch[lunarDate[0] % 12]);
        return sb.toString();
    }

    public String getLunarAnimalYear(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        StringBuilder sb = new StringBuilder();
        sb.append(this.animalYears[lunarDate[0] % 12]);
        return sb.toString();
    }

    public String getLunarMonth(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        int[] gregorianDate = new int[]{gregorian_year, gregorian_month, gregorian_day};
        StringBuilder sb = new StringBuilder();
        boolean isLeap = isLeapMonth(lunarDate[0], lunarDate[1], gregorianDate);
        if (getLeapMonth(lunarDate[0]) == lunarDate[1] && isLeap) {
            sb.append("闰");
            if (lunarDate[1] == 1) {
                sb.append("一");
            } else {
                sb.append(this.lunarMonths[lunarDate[1] - 1]);
            }
        } else {
            sb.append(this.lunarMonths[lunarDate[1] - 1]);
        }
        return sb.toString();
    }

    public String getLunarDay(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        StringBuilder sb = new StringBuilder();
        if (lunarDate[2] != 20 && lunarDate[2] != 30) {
            sb.append(this.lunarDayPrefix[(lunarDate[2] - 1) / 10]);
            sb.append(this.lunarDaySuffix[(lunarDate[2] - 1) % 10]);
        } else if (lunarDate[2] == 20) {
            sb.append(this.lunarDaySuffix[1]);
            sb.append(this.lunarDaySuffix[9]);
        } else {
            sb.append(this.lunarDayPrefix[lunarDate[2] / 10]);
            sb.append(this.lunarDaySuffix[9]);
        }
        return sb.toString();
    }

    public String getLunarDateString(int gregorian_year, int gregorian_month, int gregorian_day, int format) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        StringBuilder sb = new StringBuilder();
        sb.append(this.celestialStem[lunarDate[0] % 10]);
        sb.append(this.terrestrialBranch[lunarDate[0] % 12]);
        if (format == LUNAR_DATE_STRING_LENGTH_LONG) {
            sb.append('(');
            sb.append(this.animalYears[lunarDate[0] % 12]);
            sb.append(')');
        }
        sb.append("年");
        int[] gregorianDate = new int[]{gregorian_year, gregorian_month, gregorian_day};
        if (getLeapMonth(lunarDate[0]) != lunarDate[1]) {
            sb.append(this.lunarMonths[lunarDate[1] - 1]);
        } else if (isLeapMonth(lunarDate[0], lunarDate[1], gregorianDate)) {
            sb.append("闰");
            if (lunarDate[1] == 1) {
                sb.append("一");
            } else {
                sb.append(this.lunarMonths[lunarDate[1] - 1]);
            }
        } else {
            sb.append(this.lunarMonths[lunarDate[1] - 1]);
        }
        sb.append("月");
        if (lunarDate[2] != 20 && lunarDate[2] != 30) {
            sb.append(this.lunarDayPrefix[(lunarDate[2] - 1) / 10]);
            sb.append(this.lunarDaySuffix[(lunarDate[2] - 1) % 10]);
        } else if (lunarDate[2] == 20) {
            sb.append(this.lunarDaySuffix[1]);
            sb.append(this.lunarDaySuffix[9]);
        } else {
            sb.append(this.lunarDayPrefix[lunarDate[2] / 10]);
            sb.append(this.lunarDaySuffix[9]);
        }
        return sb.toString();
    }

    public String getSolarTermString(int gregorian_year, int gregorian_month, int gregorian_day) {
        StringBuilder sb = new StringBuilder();
        int solarTermIndex = getSolarTermIndex(new int[]{gregorian_year, gregorian_month, gregorian_day});
        if (solarTermIndex == 0) {
            return null;
        }
        sb.append(this.solarTermStrings[solarTermIndex - 1]);
        return sb.toString();
    }

    private String calLunarHoliday(int lunar_month, int lunar_day) {
        Resources res = Resources.getSystem();
        int[] dateArray = res.getIntArray(50921507);
        String[] lunarHolidayArray = res.getStringArray(50921506);
        int index = Arrays.binarySearch(dateArray, (lunar_month * 100) + lunar_day);
        if (index >= 0) {
            return lunarHolidayArray[index];
        }
        return null;
    }

    public String getLunarHolidayByGregorian(int gregorian_year, int gregorian_month, int gregorian_day) {
        int[] lunarDate = getLunarDate(gregorian_year, gregorian_month, gregorian_day);
        int[] gregorianDate = new int[]{gregorian_year, gregorian_month, gregorian_day};
        if (getLeapMonth(lunarDate[0]) == lunarDate[1] && isLeapMonth(lunarDate[0], lunarDate[1], gregorianDate)) {
            return null;
        }
        if (lunarDate[1] == 12 && lunarDate[2] == LunarMonthDays(lunarDate[0], lunarDate[1])) {
            return mRes.getString(51249272);
        }
        return calLunarHoliday(lunarDate[1], lunarDate[2]);
    }

    public String getLunarHolidayByLunar(int lunarYear, int lunayMonth, int lunayDay, int[] gregorianDate) {
        if (getLeapMonth(lunarYear) == lunayMonth && isLeapMonth(lunarYear, lunayMonth, gregorianDate)) {
            return null;
        }
        if (lunayMonth == 12 && lunayDay == LunarMonthDays(lunarYear, lunayMonth)) {
            return mRes.getString(51249272);
        }
        return calLunarHoliday(lunayMonth, lunayDay);
    }

    public String getGregorianHoliday(int gregorian_year, int gregorian_month, int gregorian_day) {
        if (gregorian_month == 5 || gregorian_month == 6 || gregorian_month == 11) {
            Time time = new Time();
            time.year = gregorian_year;
            time.month = gregorian_month - 1;
            time.monthDay = gregorian_day;
            time.normalize(LOCKSCREEN_DEBUG);
            int order = (time.monthDay - 1) / 7;
            if (time.month == 4 && order == 1 && time.weekDay == 0) {
                return mRes.getString(51249273);
            }
            if (time.month == 5 && order == 2 && time.weekDay == 0) {
                return mRes.getString(51249274);
            }
            if (time.month == 10 && order == 3 && time.weekDay == 4) {
                return mRes.getString(51249305);
            }
        }
        int[] dateArray = mRes.getIntArray(this.mSimpleGregorianHolidayDateArray[gregorian_month - 1].intValue());
        String[] holidayArray = mRes.getStringArray(this.mSimpleGregorianHolidayArray[gregorian_month - 1].intValue());
        int index = Arrays.binarySearch(dateArray, gregorian_day);
        if (index >= 0) {
            return holidayArray[index];
        }
        return null;
    }

    private String getLunarDayString(int lunarYear, int lunarMonth, int lunarDay) {
        StringBuilder sb = new StringBuilder();
        if (lunarDay == 20 || lunarDay == 30) {
            sb.append(this.lunarDayPrefix[lunarDay / 10]);
            sb.append(this.lunarDaySuffix[9]);
        } else {
            sb.append(this.lunarDayPrefix[(lunarDay - 1) / 10]);
            sb.append(this.lunarDaySuffix[(lunarDay - 1) % 10]);
        }
        return sb.toString();
    }

    private void getMonthLunarDays(DayOfMonthCursor cursor, String[][] lunarDayStrings, boolean[][] holidayDays) {
        if (lunarDayStrings != null && holidayDays != null) {
            int rows = lunarDayStrings.length;
            int cols = lunarDayStrings[0].length;
            int first_date_year = cursor.getYear();
            int first_date_month = cursor.getMonth() + 1;
            int first_date_day = cursor.getDayAt(0, 0);
            if (!cursor.isWithinCurrentMonth(0, 0)) {
                first_date_month--;
                if (first_date_month == 0) {
                    first_date_month = 12;
                    first_date_year--;
                }
            }
            int[] LunarDate = getLunarDate(first_date_year, first_date_month, first_date_day);
            int[] gregorianDate = new int[]{first_date_year, first_date_month, first_date_day};
            int lunarMonthDays = LunarMonthDays(LunarDate[0], LunarDate[1]);
            boolean isLeap = isLeapMonth(LunarDate[0], LunarDate[1], gregorianDate);
            if (getLeapMonth(LunarDate[0]) == LunarDate[1] && isLeap) {
                lunarMonthDays = (lunarMonthDays >> 8) & 255;
            } else {
                lunarMonthDays &= 255;
            }
            for (int i = 0; i < rows; i++) {
                int j = 0;
                while (j < cols) {
                    if (!(i == 0 && j == 0)) {
                        if (LunarDate[2] + 1 > lunarMonthDays) {
                            int lastrow = i;
                            int lastcol = j - 1;
                            if (lastcol < 0) {
                                lastcol = cols - 1;
                                lastrow = i - 1;
                            }
                            gregorianDate[0] = cursor.getYear();
                            gregorianDate[1] = cursor.getMonth() + 1;
                            gregorianDate[2] = cursor.getDayAt(lastrow, lastcol);
                            if (!cursor.isWithinCurrentMonth(lastrow, lastcol)) {
                                if (i < 2) {
                                    gregorianDate[1] = cursor.getMonth() + 1;
                                    gregorianDate[1] = gregorianDate[1] - 1;
                                    if (gregorianDate[1] == 0) {
                                        gregorianDate[0] = gregorianDate[0] - 1;
                                        gregorianDate[1] = 12;
                                    }
                                } else {
                                    gregorianDate[1] = cursor.getMonth() + 1;
                                    gregorianDate[1] = gregorianDate[1] + 1;
                                    if (gregorianDate[1] == 13) {
                                        gregorianDate[0] = gregorianDate[0] + 1;
                                        gregorianDate[1] = 1;
                                    }
                                }
                            }
                            LunarDate[2] = 1;
                            if (getLeapMonth(LunarDate[0]) == LunarDate[1]) {
                                if (isLeapMonth(LunarDate[0], LunarDate[1], gregorianDate)) {
                                    LunarDate[1] = LunarDate[1] + 1;
                                    if (LunarDate[1] > 12) {
                                        LunarDate[1] = 1;
                                        LunarDate[0] = LunarDate[0] + 1;
                                    }
                                    lunarMonthDays = LunarMonthDays(LunarDate[0], LunarDate[1]) & 255;
                                } else {
                                    lunarMonthDays = (LunarMonthDays(LunarDate[0], LunarDate[1]) >> 8) & 255;
                                }
                            } else {
                                LunarDate[1] = LunarDate[1] + 1;
                                if (LunarDate[1] > 12) {
                                    LunarDate[1] = 1;
                                    LunarDate[0] = LunarDate[0] + 1;
                                }
                                lunarMonthDays = LunarMonthDays(LunarDate[0], LunarDate[1]) & 255;
                            }
                        } else {
                            LunarDate[2] = LunarDate[2] + 1;
                        }
                    }
                    gregorianDate[0] = cursor.getYear();
                    gregorianDate[1] = cursor.getMonth() + 1;
                    gregorianDate[2] = cursor.getDayAt(i, j);
                    if (!cursor.isWithinCurrentMonth(i, j)) {
                        if (i < 2) {
                            gregorianDate[1] = gregorianDate[1] - 1;
                            if (gregorianDate[1] == 0) {
                                gregorianDate[0] = gregorianDate[0] - 1;
                                gregorianDate[1] = 12;
                            }
                        } else {
                            gregorianDate[1] = gregorianDate[1] + 1;
                            if (gregorianDate[1] == 13) {
                                gregorianDate[0] = gregorianDate[0] + 1;
                                gregorianDate[1] = 1;
                            }
                        }
                    }
                    String result = getGregorianHoliday(gregorianDate[0], gregorianDate[1], gregorianDate[2]);
                    if (result != null) {
                        lunarDayStrings[i][j] = result;
                        holidayDays[i][j] = LOCKSCREEN_DEBUG;
                    } else {
                        result = getLunarHolidayByLunar(LunarDate[0], LunarDate[1], LunarDate[2], gregorianDate);
                        if (result != null) {
                            lunarDayStrings[i][j] = result;
                            holidayDays[i][j] = LOCKSCREEN_DEBUG;
                        } else {
                            result = getSolarTermString(gregorianDate[0], gregorianDate[1], gregorianDate[2]);
                            if (result != null) {
                                lunarDayStrings[i][j] = result;
                                holidayDays[i][j] = LOCKSCREEN_DEBUG;
                            } else {
                                holidayDays[i][j] = false;
                                lunarDayStrings[i][j] = getLunarDayString(LunarDate[0], LunarDate[1], LunarDate[2]);
                            }
                        }
                    }
                    j++;
                }
            }
        }
    }

    public int getHolidayId(long lockscreenchange) {
        Time today = new Time();
        today.setToNow();
        int day = Time.getJulianDay(today.normalize(LOCKSCREEN_DEBUG), today.gmtoff);
        Time screenChangeTime = new Time();
        screenChangeTime.set(lockscreenchange);
        int changeDay = Time.getJulianDay(screenChangeTime.normalize(LOCKSCREEN_DEBUG), screenChangeTime.gmtoff);
        Log.v(TAG, "getHolidayId before setId lockscreenchange=" + lockscreenchange + ",Today=" + Today + ",day=" + day + ",changeDay=" + changeDay + ",LockScreenChangeTimeAfterNow=" + LockScreenChangeTimeAfterNow + ",lastScreenChangeDay=" + lastScreenChangeDay + ",oldLockSreenId=" + oldLockSreenId);
        if (Today == 0 || Today != day) {
            Today = day;
            setId(today);
            Log.v(TAG, "getHolidayId after setId lockscreenchange=" + lockscreenchange + ",Today=" + Today + ",day=" + day + ",changeDay=" + changeDay + ",LockScreenChangeTimeAfterNow=" + LockScreenChangeTimeAfterNow + ",lastScreenChangeDay=" + lastScreenChangeDay + ",oldLockSreenId=" + oldLockSreenId);
        }
        if (changeDay > Today) {
            LockScreenChangeTimeAfterNow = LOCKSCREEN_DEBUG;
        } else if (changeDay < Today) {
            LockScreenChangeTimeAfterNow = false;
        }
        if (lockscreenchange != lastScreenChangeDay) {
            LockScreenChangeTimeAfterNow = false;
            lastScreenChangeDay = lockscreenchange;
        }
        if (changeDay != Today || (LockScreenChangeTimeAfterNow ^ 1) == 0) {
            return oldLockSreenId;
        }
        return -1;
    }

    private void setId(Time time) {
        String gregholiday = getGregorianHoliday(time.year, time.month + 1, time.monthDay);
        String lunarholiday = getLunarHolidayByGregorian(time.year, time.month + 1, time.monthDay);
        String solar_term = getSolarTermString(time.year, time.month + 1, time.monthDay);
        Object holiday = null;
        mRes = Resources.getSystem();
        if (!((gregholiday == null || (mRes.getString(51249304).equals(gregholiday) ^ 1) == 0) && lunarholiday == null)) {
            if (lunarholiday != null) {
                holiday = lunarholiday;
            } else {
                String holiday2 = gregholiday;
            }
        }
        if (mRes.getString(51249280).equals(solar_term) || mRes.getString(51249302).equals(solar_term) || mRes.getString(51249303).equals(solar_term)) {
            holiday2 = solar_term;
        }
        if (holiday2 == null) {
            oldLockSreenId = -1;
        } else if (hashHoliday.containsKey(holiday2)) {
            oldLockSreenId = ((Integer) hashHoliday.get(holiday2)).intValue();
        } else {
            oldLockSreenId = -1;
        }
    }

    private static void init(Resources Rs) {
        if (hashHoliday == null) {
            Resources rs = Rs;
            hashHoliday = new HashMap();
            hashHoliday.put(Rs.getString(51249275), Integer.valueOf(50463173));
            hashHoliday.put(Rs.getString(51249276), Integer.valueOf(50463178));
            hashHoliday.put(Rs.getString(51249277), Integer.valueOf(50463179));
            hashHoliday.put(Rs.getString(51249278), Integer.valueOf(50463152));
            hashHoliday.put(Rs.getString(51249279), Integer.valueOf(50463164));
            hashHoliday.put(Rs.getString(51249280), Integer.valueOf(50463177));
            hashHoliday.put(Rs.getString(51249281), Integer.valueOf(50463161));
            hashHoliday.put(Rs.getString(51249282), Integer.valueOf(50463167));
            hashHoliday.put(Rs.getString(51249283), Integer.valueOf(50463180));
            hashHoliday.put(Rs.getString(51249284), Integer.valueOf(50463153));
            hashHoliday.put(Rs.getString(51249285), Integer.valueOf(50463162));
            hashHoliday.put(Rs.getString(51249286), Integer.valueOf(50463175));
            hashHoliday.put(Rs.getString(51249287), Integer.valueOf(50463172));
            hashHoliday.put(Rs.getString(51249289), Integer.valueOf(50463165));
            hashHoliday.put(Rs.getString(51249290), Integer.valueOf(50463155));
            hashHoliday.put(Rs.getString(51249291), Integer.valueOf(50463154));
            hashHoliday.put(Rs.getString(51249292), Integer.valueOf(50463171));
            hashHoliday.put(Rs.getString(51249293), Integer.valueOf(50463163));
            hashHoliday.put(Rs.getString(51249294), Integer.valueOf(50463174));
            hashHoliday.put(Rs.getString(51249295), Integer.valueOf(50463168));
            hashHoliday.put(Rs.getString(51249296), Integer.valueOf(50463160));
            hashHoliday.put(Rs.getString(51249297), Integer.valueOf(50463159));
            hashHoliday.put(Rs.getString(51249298), Integer.valueOf(50463170));
            hashHoliday.put(Rs.getString(51249299), Integer.valueOf(50463158));
            hashHoliday.put(Rs.getString(51249300), Integer.valueOf(50463166));
            hashHoliday.put(Rs.getString(51249301), Integer.valueOf(50463156));
            hashHoliday.put(Rs.getString(51249302), Integer.valueOf(50463169));
            hashHoliday.put(Rs.getString(51249303), Integer.valueOf(50463157));
            hashHoliday.put(Rs.getString(51249305), Integer.valueOf(50463176));
        }
    }
}
