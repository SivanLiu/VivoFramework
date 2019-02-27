package android.preference;

import android.database.ContentObserver;
import android.os.Handler;

final class SeekBarVolumizer$Observer extends ContentObserver {
    final /* synthetic */ SeekBarVolumizer this$0;

    public SeekBarVolumizer$Observer(SeekBarVolumizer this$0, Handler handler) {
        this.this$0 = this$0;
        super(handler);
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        SeekBarVolumizer.-wrap2(this.this$0);
    }
}
