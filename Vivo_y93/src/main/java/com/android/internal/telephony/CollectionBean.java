package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import java.text.SimpleDateFormat;
import java.util.Date;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class CollectionBean {
    protected final String LOG_TAG;
    private String addr;
    private int arfcn;
    private String band;
    private String cid;
    private String cid2;
    private int collectionType;
    private String cs;
    private String cs2;
    private long current;
    private String isEmergencyOnly;
    private int lac;
    private int lac2;
    private int m_signal;
    private String outServiceType;
    private int pci;
    private int phoneType;
    private String plmn;
    private String ps;
    private String ps2;
    private int regCode;
    private String reserved1;
    private String reserved2;
    private int s_qua;
    protected String signalStrength;
    protected int sim_id;
    private int snr;
    private String timenow;

    public CollectionBean() {
        this.LOG_TAG = "CollectionBean";
        this.sim_id = -1;
        this.lac = -1;
        this.cid = "-1";
        this.lac2 = -1;
        this.cid2 = "-1";
        this.plmn = "-1";
        this.addr = "-1";
        this.ps = "-1";
        this.cs = "-1";
        this.ps2 = "-1";
        this.cs2 = "-1";
        this.reserved1 = "-1";
        this.reserved2 = "-1";
        this.timenow = "-1";
        this.current = 0;
        this.phoneType = -1;
        this.collectionType = -1;
        this.regCode = -1;
        this.outServiceType = "unknown";
        this.signalStrength = "";
        this.isEmergencyOnly = "";
        this.snr = -1;
        this.pci = -1;
        this.arfcn = -1;
        this.m_signal = -1;
        this.s_qua = -1;
        this.band = "-1";
    }

    public CollectionBean(int sim) {
        this.LOG_TAG = "CollectionBean";
        this.sim_id = -1;
        this.lac = -1;
        this.cid = "-1";
        this.lac2 = -1;
        this.cid2 = "-1";
        this.plmn = "-1";
        this.addr = "-1";
        this.ps = "-1";
        this.cs = "-1";
        this.ps2 = "-1";
        this.cs2 = "-1";
        this.reserved1 = "-1";
        this.reserved2 = "-1";
        this.timenow = "-1";
        this.current = 0;
        this.phoneType = -1;
        this.collectionType = -1;
        this.regCode = -1;
        this.outServiceType = "unknown";
        this.signalStrength = "";
        this.isEmergencyOnly = "";
        this.snr = -1;
        this.pci = -1;
        this.arfcn = -1;
        this.m_signal = -1;
        this.s_qua = -1;
        this.band = "-1";
        this.sim_id = sim;
    }

    public CollectionBean(int simid, int phonetype, int collectionType, ServiceState oldSer, ServiceState newSer, CellLocation oldCell, CellLocation newCell) {
        this(simid, phonetype, collectionType, oldSer, newSer, oldCell, newCell, "unknown");
    }

    public CollectionBean(int simid, int type, int collectionType, String plmn, int lac, int cid, String cs, String ps, int lac2, int cid2, String cs2, String ps2) {
        this.LOG_TAG = "CollectionBean";
        this.sim_id = -1;
        this.lac = -1;
        this.cid = "-1";
        this.lac2 = -1;
        this.cid2 = "-1";
        this.plmn = "-1";
        this.addr = "-1";
        this.ps = "-1";
        this.cs = "-1";
        this.ps2 = "-1";
        this.cs2 = "-1";
        this.reserved1 = "-1";
        this.reserved2 = "-1";
        this.timenow = "-1";
        this.current = 0;
        this.phoneType = -1;
        this.collectionType = -1;
        this.regCode = -1;
        this.outServiceType = "unknown";
        this.signalStrength = "";
        this.isEmergencyOnly = "";
        this.snr = -1;
        this.pci = -1;
        this.arfcn = -1;
        this.m_signal = -1;
        this.s_qua = -1;
        this.band = "-1";
        this.sim_id = simid;
        this.phoneType = type;
        this.plmn = plmn;
        this.collectionType = collectionType;
        this.current = System.currentTimeMillis();
        this.timenow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.current));
        this.lac = lac;
        this.cid = cid + "";
        this.cs = cs;
        this.ps = ps;
        this.lac2 = lac2;
        this.cid2 = cid2 + "";
        this.cs2 = cs2;
        this.ps2 = ps2;
        if (CollectonUtils.DBG) {
            log("sim: " + this.sim_id + "  plmn: " + this.plmn + "  lac" + this.lac + "  cid: " + this.cid + "  lac2" + this.lac2 + "  cid2: " + this.cid2);
        }
    }

    public CollectionBean(int simid, int phonetype, int collectionType, ServiceState oldSer, ServiceState newSer, CellLocation oldCell, CellLocation newCell, String outServiceType) {
        this.LOG_TAG = "CollectionBean";
        this.sim_id = -1;
        this.lac = -1;
        this.cid = "-1";
        this.lac2 = -1;
        this.cid2 = "-1";
        this.plmn = "-1";
        this.addr = "-1";
        this.ps = "-1";
        this.cs = "-1";
        this.ps2 = "-1";
        this.cs2 = "-1";
        this.reserved1 = "-1";
        this.reserved2 = "-1";
        this.timenow = "-1";
        this.current = 0;
        this.phoneType = -1;
        this.collectionType = -1;
        this.regCode = -1;
        this.outServiceType = "unknown";
        this.signalStrength = "";
        this.isEmergencyOnly = "";
        this.snr = -1;
        this.pci = -1;
        this.arfcn = -1;
        this.m_signal = -1;
        this.s_qua = -1;
        this.band = "-1";
        this.sim_id = simid;
        this.phoneType = phonetype;
        this.collectionType = collectionType;
        this.current = System.currentTimeMillis();
        this.timenow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.current));
        this.outServiceType = outServiceType;
        this.signalStrength = DataCollectionUtils.getSignalStrength(simid);
        this.isEmergencyOnly = 2 == newSer.getVoiceRegState() ? "EMERGENCY_ONLY" : "";
        if (oldCell instanceof GsmCellLocation) {
            GsmCellLocation gsmOldCell = (GsmCellLocation) oldCell;
            this.lac = gsmOldCell.getLac();
            this.cid = gsmOldCell.getCid() + "";
        } else if (oldCell instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaOldCell = (CdmaCellLocation) oldCell;
            this.lac = cdmaOldCell.getSystemId();
            this.cid = cdmaOldCell.getNetworkId() + "," + cdmaOldCell.getBaseStationId();
        }
        if (newCell instanceof GsmCellLocation) {
            GsmCellLocation gsmNewCell = (GsmCellLocation) newCell;
            this.lac2 = gsmNewCell.getLac();
            this.cid2 = gsmNewCell.getCid() + "";
        } else if (newCell instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaNewCell = (CdmaCellLocation) newCell;
            this.lac2 = cdmaNewCell.getSystemId();
            this.cid2 = cdmaNewCell.getNetworkId() + "," + cdmaNewCell.getBaseStationId();
        }
        this.ps = ServiceState.rilRadioTechnologyToString(oldSer.getRilDataRadioTechnology());
        this.ps2 = ServiceState.rilRadioTechnologyToString(newSer.getRilDataRadioTechnology());
        this.cs = ServiceState.rilRadioTechnologyToString(oldSer.getRilVoiceRadioTechnology());
        this.cs2 = ServiceState.rilRadioTechnologyToString(newSer.getRilVoiceRadioTechnology());
        this.plmn = oldSer.getOperatorNumeric();
        if (CollectonUtils.DBG) {
            log("sim: " + this.sim_id + "  plmn: " + this.plmn + "  ps: " + this.ps + "  cs: " + this.cs + "  lac" + this.lac + "  cid: " + this.cid + "  ps2: " + this.ps2 + "  cs2: " + this.cs2 + "  lac2" + this.lac2 + "  cid2: " + this.cid2 + " outServiceType: " + outServiceType + " signalStrength: " + this.signalStrength + " isEmergencyOnly: " + this.isEmergencyOnly);
        }
    }

    public int getPhoneType() {
        return this.phoneType;
    }

    public void setPhoneTyle(int phoneType) {
        this.phoneType = phoneType;
    }

    public int getSim_id() {
        return this.sim_id;
    }

    public void setSim_id(int sim_id) {
        this.sim_id = sim_id;
    }

    public int getLac() {
        return this.lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public int getLac2() {
        return this.lac2;
    }

    public void setLac2(int lac2) {
        this.lac2 = lac2;
    }

    public String getCid2() {
        return this.cid2;
    }

    public void setCid2(String cid2) {
        this.cid2 = cid2;
    }

    public String getPlmn() {
        return this.plmn;
    }

    public void setPlmn(String plmn) {
        this.plmn = plmn;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPs() {
        return this.ps;
    }

    public void setPs(String ps) {
        this.ps = ps;
    }

    public String getCs() {
        return this.cs;
    }

    public void setCs(String cs) {
        this.cs = cs;
    }

    public String getPs2() {
        return this.ps2;
    }

    public void setPs2(String ps2) {
        this.ps2 = ps2;
    }

    public String getCs2() {
        return this.cs2;
    }

    public void setCs2(String cs2) {
        this.cs2 = cs2;
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public long getCurrent() {
        return this.current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public String getTimeNow() {
        return this.timenow;
    }

    public void setTimeNow(String timenow) {
        this.timenow = timenow;
    }

    public int getCollectionType() {
        return this.collectionType;
    }

    public void setCollectionType(int collectionType) {
        this.collectionType = collectionType;
    }

    public int getRegCode() {
        return this.regCode;
    }

    public void setRegCode(int regCode) {
        this.regCode = regCode;
    }

    public String getOutServiceType() {
        return this.outServiceType;
    }

    public void setOutServiceType(String outServiceType) {
        this.outServiceType = outServiceType;
    }

    public String getSignalStrength() {
        return this.signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getIsEmergencyOnly() {
        return this.isEmergencyOnly;
    }

    public void setIsEmergencyOnly(String isEmergencyOnly) {
        this.isEmergencyOnly = isEmergencyOnly;
    }

    public int getSnr() {
        return this.snr;
    }

    public void setSnr(int snr) {
        this.snr = snr;
    }

    public int getArfcn() {
        return this.arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public int getPci() {
        return this.pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public int getM_signal() {
        return this.m_signal;
    }

    public void setM_signal(int m_signal) {
        this.m_signal = m_signal;
    }

    public int getS_qua() {
        return this.s_qua;
    }

    public void setS_qua(int s_qua) {
        this.s_qua = s_qua;
    }

    public String getBand() {
        return this.band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String toString() {
        return "CollectionBean[sim_id=" + this.sim_id + ", lac=" + this.lac + ", cid=" + this.cid + ", lac2=" + this.lac2 + ", cid2=" + this.cid2 + ", plmn=" + this.plmn + ", addr=" + this.addr + ", ps=" + this.ps + ", cs=" + this.cs + ", ps2=" + this.ps2 + ", cs2=" + this.cs2 + ", reserved1=" + this.reserved1 + ", reserved2=" + this.reserved2 + ", current=" + this.current + ", phoneType=" + this.phoneType + "]";
    }

    protected void log(String s) {
        Rlog.d("CollectionBean", "[CollectionBean] " + s);
    }
}
