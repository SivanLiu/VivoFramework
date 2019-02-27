package com.vivo.services.motion;

import android.content.Context;
import android.os.Handler;

public interface IMotionRecognitionService {
    boolean startMotionRecognitionService(Context context, Handler handler);

    boolean stopMotionRecognitionService();
}
