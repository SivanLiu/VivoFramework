package com.vivo.api.ctgn;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.VivoTelephonyApiParams;
import com.chinatelecom.security.emm.APNControl;
import com.chinatelecom.security.emm.APNControl.APNConf;
import com.chinatelecom.security.emm.exception.IllegalParamaterException;
import com.vivo.services.cust.VivoCustomManager;
import java.util.ArrayList;
import java.util.List;

public class APNManager implements APNControl {
    private VivoCustomManager custManager;

    public APNManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }

    public List<APNConf> getCTAPNList(ComponentName admin) throws SecurityException {
        Log.d(APNControl.TAG, "getCTAPNList");
        List<APNConf> apnConfList = new ArrayList();
        ITelephony telephony = getITelephony();
        VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_readCtApn");
        param.put("componentName", admin);
        if (telephony != null) {
            VivoTelephonyApiParams ret = telephony.vivoTelephonyApi(param);
            if (ret != null) {
                int num = Integer.valueOf(ret.getAsInteger("apnListNum").intValue()).intValue();
                Log.d(APNControl.TAG, "getCTAPNList num = " + num);
                int i = 0;
                while (i < num) {
                    String name = ret.getAsString("name" + i);
                    String apn = ret.getAsString("apn" + i);
                    String user = ret.getAsString("user" + i);
                    String password = ret.getAsString("password" + i);
                    String authType = ret.getAsString("authType" + i);
                    String numeric = ret.getAsString("numeric" + i);
                    String bearer = ret.getAsString("bearer" + i);
                    APNConf apnConf = new APNConf();
                    apnConf.mName = name;
                    apnConf.mApn = apn;
                    apnConf.mUser = user;
                    apnConf.mPassword = password;
                    try {
                        apnConf.mAuthType = Integer.valueOf(authType).intValue();
                    } catch (Exception e) {
                        Log.d(APNControl.TAG, "authType is not int!");
                    }
                    apnConf.mNumberic = numeric;
                    try {
                        apnConf.mBearer = Integer.valueOf(bearer).intValue();
                    } catch (Exception e2) {
                        Log.d(APNControl.TAG, "bearer is not int!");
                    }
                    try {
                        Log.d(APNControl.TAG, "getCTAPNList apnConf = " + printApn(apnConf));
                        apnConfList.add(apnConf);
                        i++;
                    } catch (RemoteException e3) {
                        Log.e(APNControl.TAG, "Failed to talk to package manager", e3);
                    }
                }
            }
        }
        return apnConfList;
    }

    public void addCTAPN(ComponentName admin, APNConf newapn) throws SecurityException, IllegalParamaterException {
        Log.d(APNControl.TAG, "AddCTAPN newapn = " + printApn(newapn));
        ITelephony telephony = getITelephony();
        VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_addCtApn");
        param.put("componentName", admin);
        param.put("name", newapn.mName);
        param.put("apn", newapn.mApn);
        param.put("user", newapn.mUser);
        param.put("password", newapn.mPassword);
        param.put("authType", Integer.valueOf(newapn.mAuthType));
        param.put("dialNumber", newapn.mDialNumber);
        param.put("bearer", Integer.valueOf(newapn.mBearer));
        if (telephony != null) {
            try {
                telephony.vivoTelephonyApi(param);
                return;
            } catch (IllegalArgumentException e) {
                throw new IllegalParamaterException("addCTAPN:IllegalParamaterException occur!");
            } catch (RemoteException e2) {
                Log.e(APNControl.TAG, "Failed to talk to package manager", e2);
                return;
            }
        }
        Log.d(APNControl.TAG, "telephony null!");
    }

    public void editCTAPN(ComponentName admin, String name, APNConf newapn) throws SecurityException, IllegalParamaterException {
        Log.d(APNControl.TAG, "EditCTAPN name = " + name + " newapn = " + printApn(newapn));
        ITelephony telephony = getITelephony();
        VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_editCtApn");
        param.put("componentName", admin);
        param.put("editName", name);
        param.put("name", newapn.mName);
        param.put("apn", newapn.mApn);
        param.put("user", newapn.mUser);
        param.put("password", newapn.mPassword);
        param.put("authType", Integer.valueOf(newapn.mAuthType));
        param.put("dialNumber", newapn.mDialNumber);
        param.put("bearer", Integer.valueOf(newapn.mBearer));
        if (telephony != null) {
            try {
                telephony.vivoTelephonyApi(param);
                return;
            } catch (IllegalArgumentException e) {
                throw new IllegalParamaterException("editCTAPN:IllegalParamaterException occur!");
            } catch (RemoteException e2) {
                Log.e(APNControl.TAG, "Failed to talk to package manager", e2);
                return;
            }
        }
        Log.d(APNControl.TAG, "telephony null!");
    }

    public void deleteCTAPN(ComponentName admin, String name) throws SecurityException, IllegalParamaterException {
        Log.d(APNControl.TAG, "DeleteCTAPN name = " + name);
        ITelephony telephony = getITelephony();
        VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_delCtApn");
        param.put("componentName", admin);
        param.put("name", name);
        if (telephony != null) {
            try {
                telephony.vivoTelephonyApi(param);
                return;
            } catch (IllegalArgumentException e) {
                throw new IllegalParamaterException("deleteCTAPN:IllegalParamaterException occur!");
            } catch (RemoteException e2) {
                Log.e(APNControl.TAG, "Failed to talk to package manager", e2);
                return;
            }
        }
        Log.d(APNControl.TAG, "telephony null!");
    }

    public void activeCTAPN(ComponentName admin, String name) throws SecurityException, IllegalParamaterException {
        Log.d(APNControl.TAG, "ActiveCTAPN name = " + name);
        ITelephony telephony = getITelephony();
        VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_actCtApn");
        param.put("componentName", admin);
        param.put("name", name);
        if (telephony != null) {
            try {
                telephony.vivoTelephonyApi(param);
                return;
            } catch (IllegalArgumentException e) {
                throw new IllegalParamaterException("deleteCTAPN:IllegalParamaterException occur!");
            } catch (RemoteException e2) {
                Log.e(APNControl.TAG, "Failed to talk to package manager", e2);
                return;
            }
        }
        Log.d(APNControl.TAG, "telephony null!");
    }

    public void setCTAPNActiveMode(ComponentName admin, Integer p1) throws SecurityException, IllegalParamaterException {
        try {
            this.custManager.setAPNState(p1.intValue() == 2 ? 0 : p1.intValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalParamaterException("setCTAPNActiveMode:IllegalParamaterException occur!");
        }
    }

    public Integer getCTAPNActiveMode(ComponentName admin) throws SecurityException, IllegalParamaterException {
        int value = this.custManager.getAPNState();
        if (value == 0) {
            return Integer.valueOf(2);
        }
        if (value == 1) {
            return Integer.valueOf(1);
        }
        return Integer.valueOf(-1);
    }

    private ITelephony getITelephony() {
        return Stub.asInterface(ServiceManager.getService("phone"));
    }

    private String printApn(APNConf apn) {
        if (apn != null) {
            return " name = " + apn.mName + " apn = " + apn.mApn + " user = " + apn.mUser + " password = " + apn.mPassword + " authtype = " + apn.mAuthType + " dialnumber = " + apn.mDialNumber + " Bearer = " + apn.mBearer;
        }
        return null;
    }
}
