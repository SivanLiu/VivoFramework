package com.vivo.services.rms.sdk;

import android.util.SparseArray;

public class Consts {

    public static class ProcessEvent {
        public static final int ADD = 5;
        public static final int ADD_DEPPKG = 8;
        public static final int ADD_PKG = 9;
        public static final int REMOVE = 6;
        public static final int SET_ADJ = 0;
        public static final int SET_ADJTYPE = 3;
        public static final int SET_CONFIG = 10;
        public static final int SET_OOM = 4;
        public static final int SET_SCHEDGROUP = 2;
        public static final int SET_STATES = 1;
        public static final int START_ACTIVITY = 7;
    }

    public static class ProcessStates {
        public static final String FGACTIVITIES_NAME = "fgActivity";
        public static final int FGACTIVITY = 1;
        public static final int FGFORCE = 2;
        public static final String FGFORCE_NAME = "fgForce";
        public static final int FGSERVICE = 4;
        public static final String FGSERVICES_NAME = "fgService";
        public static final int HASACTIVITY = 64;
        public static final String HASACTIVITY_NAME = "hasActivity";
        public static final int HASNOTIFICATION = 256;
        public static final String HASNOTIFICATION_NAME = "hasNoti";
        public static final int HASSERVICE = 128;
        public static final String HASSERVICE_NAME = "hasService";
        public static final int HASSHOWNUI = 32;
        public static final String HASSHOWNUI_NAME = "hasShown";
        public static final int PAUSING = 512;
        public static final String PAUSING_NAME = "pausing";
        public static final SparseArray<String> STATES_NAMES = new SparseArray();
        public static final int VISIBLE = 8;
        public static final String VISIBLE_NAME = "visible";
        public static final int WORKING = 16;
        public static final String WORKING_NAME = "working";

        static {
            STATES_NAMES.put(1, FGACTIVITIES_NAME);
            STATES_NAMES.put(2, FGFORCE_NAME);
            STATES_NAMES.put(4, FGSERVICES_NAME);
            STATES_NAMES.put(8, VISIBLE_NAME);
            STATES_NAMES.put(16, WORKING_NAME);
            STATES_NAMES.put(32, HASSHOWNUI_NAME);
            STATES_NAMES.put(HASNOTIFICATION, HASNOTIFICATION_NAME);
            STATES_NAMES.put(64, HASACTIVITY_NAME);
            STATES_NAMES.put(HASSERVICE, HASSERVICE_NAME);
            STATES_NAMES.put(PAUSING, PAUSING_NAME);
        }

        public static String getName(int state) {
            if (state == 0) {
                return "";
            }
            StringBuilder builder = new StringBuilder(24);
            for (int i = 0; i < STATES_NAMES.size(); i++) {
                if ((STATES_NAMES.keyAt(i) & state) != 0) {
                    builder.append((String) STATES_NAMES.valueAt(i));
                    builder.append(" ");
                }
            }
            return builder.substring(0, builder.length() - 1);
        }
    }

    public static class SystemEvent {
        public static final int SET_APP_LIST = 0;
        public static final int SET_BUNDLE = 1;
    }
}
