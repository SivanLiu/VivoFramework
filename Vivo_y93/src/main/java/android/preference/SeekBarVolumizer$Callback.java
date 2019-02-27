package android.preference;

import android.widget.SeekBar;

public interface SeekBarVolumizer$Callback {
    void onMuted(boolean z, boolean z2);

    void onProgressChanged(SeekBar seekBar, int i, boolean z);

    void onSampleStarting(SeekBarVolumizer seekBarVolumizer);
}
