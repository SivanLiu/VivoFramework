package com.chinatelecom.security.emm;

import android.content.ComponentName;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import java.util.List;

public interface APNControl {
    public static final String TAG = "APNControl";

    public static class APNConf {
        public String mApn;
        public int mAuthType;
        public int mBearer;
        public int mCurrent;
        public String mDialNumber;
        public String mHomePage;
        public String mMCC;
        public String mMMSC;
        public String mMMSPort;
        public String mMMSProxy;
        public String mMNC;
        public String mMvNoData;
        public String mMvNoType;
        public String mName;
        public String mNumberic;
        public String mPassword;
        public String mPort;
        public String mProtocol;
        public String mProxy;
        public String mRoamingProtocol;
        public String mServer;
        public String mTypes;
        public String mUser;
    }

    void activeCTAPN(ComponentName componentName, String str) throws SecurityException, IllegalParamaterException;

    void addCTAPN(ComponentName componentName, APNConf aPNConf) throws SecurityException, IllegalParamaterException;

    void deleteCTAPN(ComponentName componentName, String str) throws SecurityException, IllegalParamaterException;

    void editCTAPN(ComponentName componentName, String str, APNConf aPNConf) throws SecurityException, IllegalParamaterException;

    Integer getCTAPNActiveMode(ComponentName componentName) throws SecurityException, IllegalParamaterException;

    List<APNConf> getCTAPNList(ComponentName componentName) throws SecurityException;

    void setCTAPNActiveMode(ComponentName componentName, Integer num) throws SecurityException, IllegalParamaterException;
}
