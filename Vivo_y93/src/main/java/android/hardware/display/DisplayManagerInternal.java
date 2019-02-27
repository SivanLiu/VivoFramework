package android.hardware.display;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.IntArray;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;

public abstract class DisplayManagerInternal {

    public interface DisplayPowerCallbacks {
        void acquireSuspendBlocker();

        void onDisplayStateChange(int i);

        void onProximityEnabled(boolean z);

        void onProximityNegative();

        void onProximityPositive();

        void onProximityStatus(boolean z);

        void onStateChanged();

        void releaseSuspendBlocker();
    }

    public static final class DisplayPowerRequest {
        public static final int POLICY_BRIGHT = 3;
        public static final int POLICY_DIM = 2;
        public static final int POLICY_DOZE = 1;
        public static final int POLICY_OFF = 0;
        public static final int POLICY_VR = 4;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public boolean antimisoperationTriggered;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public boolean bIsProximitySensorTag;
        public boolean blockScreenOn;
        public boolean boostScreenBrightness;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public String brightnessModeOffBy;
        public boolean brightnessSetByUser;
        public int callState;
        public int dozeScreenBrightness;
        public int dozeScreenState;
        public boolean lowPowerMode;
        public int nightPearl;
        public int policy;
        public float screenAutoBrightnessAdjustment;
        public int screenBrightness;
        public float screenLowPowerBrightnessFactor;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public int settingBrightness;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public String settingBrightnessChangeBy;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        public int settingScreenBrightnessMode;
        public boolean useAutoBrightness;
        public boolean useProximitySensor;

        public DisplayPowerRequest() {
            this.settingBrightnessChangeBy = "unknown";
            this.settingScreenBrightnessMode = -1;
            this.brightnessModeOffBy = "unknown";
            this.nightPearl = 0;
            this.callState = 0;
            this.policy = 3;
            this.useProximitySensor = false;
            this.screenBrightness = 255;
            this.screenAutoBrightnessAdjustment = 0.0f;
            this.screenLowPowerBrightnessFactor = 0.5f;
            this.useAutoBrightness = false;
            this.blockScreenOn = false;
            this.dozeScreenBrightness = -1;
            this.dozeScreenState = 0;
        }

        public DisplayPowerRequest(DisplayPowerRequest other) {
            this.settingBrightnessChangeBy = "unknown";
            this.settingScreenBrightnessMode = -1;
            this.brightnessModeOffBy = "unknown";
            this.nightPearl = 0;
            this.callState = 0;
            copyFrom(other);
        }

        public boolean isBrightOrDim() {
            return this.policy == 3 || this.policy == 2;
        }

        public boolean isVr() {
            return this.policy == 4;
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public void copyFrom(DisplayPowerRequest other) {
            this.policy = other.policy;
            this.useProximitySensor = other.useProximitySensor;
            this.screenBrightness = other.screenBrightness;
            this.settingScreenBrightnessMode = other.settingScreenBrightnessMode;
            this.brightnessModeOffBy = other.brightnessModeOffBy;
            this.settingBrightness = other.settingBrightness;
            this.settingBrightnessChangeBy = other.settingBrightnessChangeBy;
            this.screenAutoBrightnessAdjustment = other.screenAutoBrightnessAdjustment;
            this.screenLowPowerBrightnessFactor = other.screenLowPowerBrightnessFactor;
            this.brightnessSetByUser = other.brightnessSetByUser;
            this.useAutoBrightness = other.useAutoBrightness;
            this.blockScreenOn = other.blockScreenOn;
            this.lowPowerMode = other.lowPowerMode;
            this.boostScreenBrightness = other.boostScreenBrightness;
            this.dozeScreenBrightness = other.dozeScreenBrightness;
            this.dozeScreenState = other.dozeScreenState;
            this.antimisoperationTriggered = other.antimisoperationTriggered;
            this.callState = other.callState;
        }

        public boolean equals(Object o) {
            if (o instanceof DisplayPowerRequest) {
                return equals((DisplayPowerRequest) o);
            }
            return false;
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public boolean equals(DisplayPowerRequest other) {
            return other != null && this.policy == other.policy && this.useProximitySensor == other.useProximitySensor && this.screenBrightness == other.screenBrightness && this.settingScreenBrightnessMode == other.settingScreenBrightnessMode && this.brightnessModeOffBy.equals(other.brightnessModeOffBy) && this.settingBrightness == other.settingBrightness && this.settingBrightnessChangeBy.equals(other.settingBrightnessChangeBy) && this.screenAutoBrightnessAdjustment == other.screenAutoBrightnessAdjustment && this.screenLowPowerBrightnessFactor == other.screenLowPowerBrightnessFactor && this.brightnessSetByUser == other.brightnessSetByUser && this.useAutoBrightness == other.useAutoBrightness && this.blockScreenOn == other.blockScreenOn && this.lowPowerMode == other.lowPowerMode && this.boostScreenBrightness == other.boostScreenBrightness && this.dozeScreenBrightness == other.dozeScreenBrightness && this.dozeScreenState == other.dozeScreenState && this.antimisoperationTriggered == other.antimisoperationTriggered && this.callState == other.callState;
        }

        public int hashCode() {
            return 0;
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public String toString() {
            return "policy=" + policyToString(this.policy) + ", useProximitySensor=" + this.useProximitySensor + ", screenBrightness=" + this.screenBrightness + ", screenAutoBrightnessAdjustment=" + this.screenAutoBrightnessAdjustment + ", screenLowPowerBrightnessFactor=" + this.screenLowPowerBrightnessFactor + ", brightnessSetByUser=" + this.brightnessSetByUser + ", useAutoBrightness=" + this.useAutoBrightness + ", blockScreenOn=" + this.blockScreenOn + ", lowPowerMode=" + this.lowPowerMode + ", boostScreenBrightness=" + this.boostScreenBrightness + ", dozeScreenBrightness=" + this.dozeScreenBrightness + ", dozeScreenState=" + Display.stateToString(this.dozeScreenState) + ", antimisoperationTriggered=" + this.antimisoperationTriggered + ", callState=" + this.callState;
        }

        public static String policyToString(int policy) {
            switch (policy) {
                case 0:
                    return "OFF";
                case 1:
                    return "DOZE";
                case 2:
                    return "DIM";
                case 3:
                    return "BRIGHT";
                case 4:
                    return "VR";
                default:
                    return Integer.toString(policy);
            }
        }
    }

    public interface DisplayTransactionListener {
        void onDisplayTransaction();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public abstract int getAnimatedBrightness();

    public abstract DisplayInfo getDisplayInfo(int i);

    public abstract void getNonOverrideDisplayInfo(int i, DisplayInfo displayInfo);

    public abstract void initPowerManagement(DisplayPowerCallbacks displayPowerCallbacks, Handler handler, SensorManager sensorManager);

    public abstract boolean isProximitySensorAvailable();

    public abstract boolean isUidPresentOnDisplay(int i, int i2);

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public abstract int notifyCameraParamLuma(String str);

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public abstract int notifyStateChanged(int i);

    public abstract void performTraversalInTransactionFromWindowManager();

    public abstract void registerDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract boolean requestPowerState(DisplayPowerRequest displayPowerRequest, boolean z);

    public abstract void setDisplayAccessUIDs(SparseArray<IntArray> sparseArray);

    public abstract void setDisplayInfoOverrideFromWindowManager(int i, DisplayInfo displayInfo);

    public abstract void setDisplayOffsets(int i, int i2, int i3);

    public abstract void setDisplayProperties(int i, boolean z, float f, int i2, boolean z2);

    public abstract void setFingerprintOverlayEnable(boolean z);

    public abstract void setFingerprintShooter(Bitmap bitmap, int i, int i2, int i3, int i4);

    public abstract void setFingerprintShooterVisible(int i);

    public abstract void unregisterDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public abstract void updateFaceUnlockStateChanged(int i);
}
