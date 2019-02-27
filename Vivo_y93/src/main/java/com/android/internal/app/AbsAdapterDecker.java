package com.android.internal.app;

import android.content.Intent;
import android.view.View;
import com.android.internal.app.AdapterDecker.Callback;
import com.android.internal.app.AdapterDecker.Listener;
import com.android.internal.app.AdapterDecker.WrapperAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverAdapter.LifeCycle;
import java.util.List;

abstract class AbsAdapterDecker implements AdapterDecker {
    /* renamed from: -com-android-internal-app-ResolverAdapter$LifeCycleSwitchesValues */
    private static final /* synthetic */ int[] f124x3d2c93ee = null;
    private Callback mCallback = null;
    private AdapterDecker mDeckerAdapter = null;
    protected ResolverActivity mResolverContext = null;

    /* renamed from: -getcom-android-internal-app-ResolverAdapter$LifeCycleSwitchesValues */
    private static /* synthetic */ int[] m47x9c35b7ca() {
        if (f124x3d2c93ee != null) {
            return f124x3d2c93ee;
        }
        int[] iArr = new int[LifeCycle.values().length];
        try {
            iArr[LifeCycle.ON_DESTROY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LifeCycle.ON_PAUSE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[LifeCycle.ON_RESTART.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[LifeCycle.ON_RESUME.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[LifeCycle.ON_STOP.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        f124x3d2c93ee = iArr;
        return iArr;
    }

    public AbsAdapterDecker(AdapterDecker adapter) {
        this.mDeckerAdapter = adapter;
    }

    public final void onCreate(ResolverActivity activity, Intent intent, Callback callback) {
        this.mResolverContext = activity;
        this.mCallback = callback;
        callback.addListener(new Listener() {
            public boolean onItemClick(View view, int position) {
                return AbsAdapterDecker.this.OnItemClick(view, position);
            }

            public boolean onItemLongClick(View view, int position) {
                return AbsAdapterDecker.this.OnItemLongClick(view, position);
            }
        });
        onAfterCreate(activity, intent);
        this.mDeckerAdapter.onCreate(activity, intent, callback);
    }

    protected void onAfterCreate(ResolverActivity activity, Intent intent) {
    }

    public final void onPreBuildList(List<ResolvedComponentInfo> components) {
        onPrebuildList(components);
        this.mDeckerAdapter.onPreBuildList(components);
    }

    protected void onPrebuildList(List<ResolvedComponentInfo> list) {
    }

    public final void onRebuildList(WrapperAdapter adapter) {
        onBuildList(adapter);
        this.mDeckerAdapter.onRebuildList(adapter);
    }

    protected void onBuildList(WrapperAdapter adapter) {
    }

    public final void onBindView(int position, View convertView) {
        onCreateItemView(position, convertView);
        this.mDeckerAdapter.onBindView(position, convertView);
    }

    protected void onCreateItemView(int position, View view) {
    }

    public final void onLifeCycleChanged(LifeCycle life) {
        switch (m47x9c35b7ca()[life.ordinal()]) {
            case 1:
                onDestroy();
                break;
        }
        this.mDeckerAdapter.onLifeCycleChanged(life);
    }

    protected void notifyDataSetChanged() {
        this.mCallback.notifyDataSetChanged();
    }

    protected void onDestroy() {
    }

    protected boolean OnItemClick(View view, int position) {
        return false;
    }

    protected boolean OnItemLongClick(View view, int position) {
        return false;
    }
}
