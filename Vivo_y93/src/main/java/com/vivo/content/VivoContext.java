package com.vivo.content;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoContext {
    public static final String BBK_TOUCH_SCREEN_SERVICE = "bbk_touch_screen_service";
    public static final String DEVICE_PARA_PROVIDE_SERVICE = "device_para_provide_service";
    public static final String EXCEPTION_POLICY_SERVICE = "exception_policy";
    public static final String HALL_STATE_MANAGER = "hall_state_manager";
    public static final String HALL_STATE_SERVICE = "hall_state_service";
    public static final String MOTION_RECONGNITION_SERVICE = "motion_recongnition";
    public static final String NIGHT_PEARL_MANAGER = "night_pearl_manager";
    public static final String NIGHT_PEARL_SERVICE = "night_pearl_service";
    public static final String POPUP_CAMERA_SERVICE = "popup_camera_service";
    public static final String SENSOR_LOG_SERVICE = "sensor_log";
    public static final String TAG = "VivoContext";
    public static final String VIVO_4D_GAME_VIBRATOR_SERVICE = "vivo_4d_game_vibrator_service";
    public static final String VIVO_BACKUP_SERVICE = "vivo_backup_service";
    public static final String VIVO_LOG_SERVICE = "vivo_log_service";
    public static final String VIVO_PERMISSION_SERVICE = "vivo_permission_service";
    public static final String VIVO_PROX_CALI_SERVICE = "vivo_prox_cali_service";
}
