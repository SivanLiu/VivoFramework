package android.preference;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

final class SeekBarVolumizer$Receiver extends BroadcastReceiver {
    private boolean mListening;
    final /* synthetic */ SeekBarVolumizer this$0;

    /* synthetic */ SeekBarVolumizer$Receiver(SeekBarVolumizer this$0, SeekBarVolumizer$Receiver -this1) {
        this(this$0);
    }

    private SeekBarVolumizer$Receiver(SeekBarVolumizer this$0) {
        this.this$0 = this$0;
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (listening) {
                IntentFilter filter = new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION);
                filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
                filter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
                SeekBarVolumizer.-get3(this.this$0).registerReceiver(this, filter);
            } else {
                SeekBarVolumizer.-get3(this.this$0).unregisterReceiver(this);
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AudioManager.VOLUME_CHANGED_ACTION.equals(action)) {
            updateVolumeSlider(intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1), intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, -1));
        } else if (AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION.equals(action)) {
            if (SeekBarVolumizer.-get7(this.this$0)) {
                SeekBarVolumizer.-set3(this.this$0, SeekBarVolumizer.-get1(this.this$0).getRingerModeInternal());
            }
            if (SeekBarVolumizer.-get0(this.this$0)) {
                SeekBarVolumizer.-wrap2(this.this$0);
            }
        } else if (AudioManager.STREAM_DEVICES_CHANGED_ACTION.equals(action)) {
            int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
            updateVolumeSlider(streamType, SeekBarVolumizer.-get1(this.this$0).getStreamVolume(streamType));
        } else if (NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED.equals(action)) {
            SeekBarVolumizer.-set4(this.this$0, SeekBarVolumizer.-get6(this.this$0).getZenMode());
            SeekBarVolumizer.-wrap2(this.this$0);
        }
    }

    private void updateVolumeSlider(int streamType, int streamValue) {
        boolean streamMatch = SeekBarVolumizer.-get7(this.this$0) ? SeekBarVolumizer.-wrap0(streamType) : streamType == SeekBarVolumizer.-get9(this.this$0);
        if (SeekBarVolumizer.-get8(this.this$0) != null && streamMatch && streamValue != -1) {
            boolean muted = !SeekBarVolumizer.-get1(this.this$0).isStreamMute(SeekBarVolumizer.-get9(this.this$0)) ? streamValue == 0 : true;
            SeekBarVolumizer.-get10(this.this$0).postUpdateSlider(streamValue, SeekBarVolumizer.-get4(this.this$0), muted);
        }
    }
}
