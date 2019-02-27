package com.android.internal.telephony.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SubscriptionManager;
import java.util.Arrays;

public class NotificationChannelController {
    public static final String CHANNEL_ID_ALERT = "others";
    public static final String CHANNEL_ID_CALL_FORWARD = "others";
    private static final String CHANNEL_ID_MOBILE_DATA_ALERT_DEPRECATED = "mobileDataAlert";
    public static final String CHANNEL_ID_MOBILE_DATA_STATUS = "others";
    public static final String CHANNEL_ID_OTHER = "others";
    public static final String CHANNEL_ID_SMS = "others";
    public static final String CHANNEL_ID_VOICE_MAIL = "others";
    public static final String CHANNEL_ID_WFC = "others";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                NotificationChannelController.createAll(context);
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && -1 != SubscriptionManager.getDefaultSubscriptionId()) {
                NotificationChannelController.migrateVoicemailNotificationSettings(context);
            }
        }
    };

    private static void createAll(Context context) {
        NotificationManager notiManager = (NotificationManager) context.getSystemService("notification");
        NotificationChannel channel = new NotificationChannel("others", context.getText(51249616), 4);
        channel.setSound(null, channel.getAudioAttributes());
        notiManager.createNotificationChannels(Arrays.asList(new NotificationChannel[]{channel}));
    }

    public NotificationChannelController(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        createAll(context);
    }

    public static NotificationChannel getChannel(String channelId, Context context) {
        return ((NotificationManager) context.getSystemService(NotificationManager.class)).getNotificationChannel(channelId);
    }

    private static void migrateVoicemailNotificationSettings(Context context) {
    }
}
