package com.vivo.common.autobrightness;

import android.hardware.SensorEvent;
import org.json.JSONObject;

public class CameraLumaCallback {

    public interface PreLightCallback {
        void notifyBrightnessToUDFinger(SensorEvent sensorEvent);

        void onSensorChanged(SensorEvent sensorEvent);
    }

    public interface AppRatioUpdateLuxThreshold {
        void onBrightnessRatioChanged(int i);
    }

    public interface ModeRestoreCallback {
        void saveModifyRecord(JSONObject jSONObject);

        void setSecondUserBrightness(int i);
    }

    public interface AppBrightnessCallback {
        void onAppBrightModeChanged(boolean z);
    }

    public interface AutoBrightnessCallback {
        int getCurrentAutoBrightness();

        void onNeedCancelBrightness(int i);

        void onNewScreenValue(AutobrightInfo autobrightInfo);
    }

    public interface BrightnessRatioCallback {
        void onRatioChanged(float f);
    }

    public interface PowerAssistantCallback {
        void onPowerSaveTypeChanged(int i);
    }

    public interface StatisticsCallback {
        boolean onGetAutobrightInfo(AutobrightInfo autobrightInfo);

        int onUpdateSceneState(int i);
    }

    public interface UnderDisplayLightCallback {
        void onAppChanged(String str, boolean z);
    }

    public interface UserChangeBrightnessCallback {
        void onUserChangeBrightness(AutobrightInfo autobrightInfo);
    }
}
