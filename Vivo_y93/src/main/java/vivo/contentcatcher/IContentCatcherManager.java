package vivo.contentcatcher;

import android.app.Activity;
import android.os.Bundle;

public interface IContentCatcherManager {
    void copyNode(Bundle bundle);

    void processImageAndWebview(Bundle bundle);

    void updateToken(int i, Activity activity);
}
