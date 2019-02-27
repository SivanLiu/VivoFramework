package com.android.internal.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FtBuild;
import android.os.SystemProperties;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverAdapter.LifeCycle;
import java.util.List;

final class ResolverAdapterFactory {
    private static final boolean DEBUG = true;
    private static final String TAG = "ResolverAdapterFactory";
    private static final float mRomVersion = FtBuild.getRomVersion();

    private static final class DefaultResolverAdapterImpl implements ResolverAdapter {
        /* synthetic */ DefaultResolverAdapterImpl(DefaultResolverAdapterImpl -this0) {
            this();
        }

        private DefaultResolverAdapterImpl() {
        }

        public void onCreate(Bundle bundle, Intent intent, Intent[] initialIntent, boolean supportsAlwaysUseOption) {
        }

        public void onPreBuildList(List<ResolvedComponentInfo> list) {
        }

        public void onRebuildList(ResolveListAdapter adapter, CharSequence title) {
        }

        public void onLifeCycleChanged(LifeCycle life) {
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
        }
    }

    ResolverAdapterFactory() {
    }

    public static ResolverAdapter getResolverAdapter(ResolverActivity resolver) {
        AdapterDecker adapter = new VigourAdapterDecker();
        if (isShareGridSort()) {
            adapter = new ShareGridSortAdapterDecker(adapter);
        }
        if (isDoubleInstance(resolver)) {
            adapter = new DoubleInstanceAdapterDecker(adapter);
        }
        if (isVigourResolver()) {
            return new ResolverAdapterImpl(resolver, adapter);
        }
        return new DefaultResolverAdapterImpl();
    }

    public static ResolverAdapter getResolverAdapter(ResolverActivity resolver, int uid) {
        AdapterDecker adapter = new VigourAdapterDecker();
        if (isShareGridSort()) {
            adapter = new ShareGridSortAdapterDecker(adapter, uid);
        }
        if (isDoubleInstance(resolver)) {
            adapter = new DoubleInstanceAdapterDecker(adapter, uid);
        }
        if (isVigourResolver()) {
            return new ResolverAdapterImpl(resolver, adapter);
        }
        return new DefaultResolverAdapterImpl();
    }

    private static boolean isShareGridSort() {
        return mRomVersion >= 3.6f;
    }

    private static boolean isVigourResolver() {
        return SystemProperties.getBoolean("debug.resolver.vigour", true);
    }

    private static boolean isDoubleInstance(Activity activity) {
        return (activity instanceof DoubleAppResolverActivity) ^ 1;
    }
}
