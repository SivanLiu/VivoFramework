package com.android.internal.app;

import android.content.Intent;
import android.os.Bundle;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import java.util.List;

interface ResolverAdapter {

    public enum LifeCycle {
        ON_DESTROY,
        ON_RESTART,
        ON_STOP,
        ON_PAUSE,
        ON_RESUME
    }

    void onCreate(Bundle bundle, Intent intent, Intent[] intentArr, boolean z);

    void onLifeCycleChanged(LifeCycle lifeCycle);

    void onPreBuildList(List<ResolvedComponentInfo> list);

    void onRebuildList(ResolveListAdapter resolveListAdapter, CharSequence charSequence);

    void onRestoreInstanceState(Bundle bundle);
}
