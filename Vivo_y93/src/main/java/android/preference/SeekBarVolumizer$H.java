package android.preference;

import android.os.Handler;
import android.os.Message;

final class SeekBarVolumizer$H extends Handler {
    private static final int UPDATE_SLIDER = 1;
    final /* synthetic */ SeekBarVolumizer this$0;

    /* synthetic */ SeekBarVolumizer$H(SeekBarVolumizer this$0, SeekBarVolumizer$H -this1) {
        this(this$0);
    }

    private SeekBarVolumizer$H(SeekBarVolumizer this$0) {
        this.this$0 = this$0;
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1 && SeekBarVolumizer.-get8(this.this$0) != null) {
            SeekBarVolumizer.-set1(this.this$0, msg.arg1);
            SeekBarVolumizer.-set0(this.this$0, msg.arg2);
            boolean muted = ((Boolean) msg.obj).booleanValue();
            if (muted != SeekBarVolumizer.-get5(this.this$0)) {
                SeekBarVolumizer.-set2(this.this$0, muted);
                if (SeekBarVolumizer.-get2(this.this$0) != null) {
                    SeekBarVolumizer.-get2(this.this$0).onMuted(SeekBarVolumizer.-get5(this.this$0), SeekBarVolumizer.-wrap1(this.this$0));
                }
            }
            this.this$0.updateSeekBar();
        }
    }

    public void postUpdateSlider(int volume, int lastAudibleVolume, boolean mute) {
        obtainMessage(1, volume, lastAudibleVolume, new Boolean(mute)).sendToTarget();
    }
}
