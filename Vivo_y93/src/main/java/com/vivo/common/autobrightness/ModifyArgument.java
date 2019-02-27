package com.vivo.common.autobrightness;

import com.vivo.common.provider.Calendar.Events;
import java.lang.reflect.Array;

public class ModifyArgument {
    public static final int CURVE_VERSION = 2;
    public static final int NEED_LINE = 2;
    public static final int NEED_ROW = 15;
    public boolean bUserSettingBrightness = false;
    public int mCameraOpenCount = 0;
    public int[] mPhoneStatus = new int[]{-1, -1, -1};
    public int mPhoneStatusCount = 0;
    public int[][] mRecordNeed = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{15, 2}));

    public String toString() {
        int i;
        String ret = Events.DEFAULT_SORT_ORDER + "rneed=[\n";
        for (i = 0; i < 15; i++) {
            ret = ret + "  [";
            for (int j = 0; j < 2; j++) {
                ret = ret + this.mRecordNeed[i][j] + ",";
            }
            ret = ret + "],\n";
        }
        ret = ret + "pstatus=[\n";
        for (int i2 : this.mPhoneStatus) {
            ret = ret + i2 + ",";
        }
        return ((((ret + "],\n") + "\npscnt=" + this.mPhoneStatusCount) + "\ncamopcnt=" + this.mCameraOpenCount) + "\nuser_set_bri=" + this.bUserSettingBrightness) + "\nversion=2";
    }
}
