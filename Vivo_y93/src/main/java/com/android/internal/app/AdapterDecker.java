package com.android.internal.app;

import android.content.Intent;
import android.view.View;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.internal.app.ResolverAdapter.LifeCycle;
import java.util.List;

interface AdapterDecker {

    public interface Listener {
        boolean onItemClick(View view, int i);

        boolean onItemLongClick(View view, int i);
    }

    public interface Callback {
        void addListener(Listener listener);

        void notifyDataSetChanged();

        void removeListener(Listener listener);
    }

    public interface WrapperAdapter {
        ResolveListAdapter getBaseAdapter();

        TargetInfo getItem(int i);

        int getItemCount();

        void insertItem(int i, TargetInfo targetInfo);

        TargetInfo removeItem(int i);
    }

    void onBindView(int i, View view);

    void onCreate(ResolverActivity resolverActivity, Intent intent, Callback callback);

    void onLifeCycleChanged(LifeCycle lifeCycle);

    void onPreBuildList(List<ResolvedComponentInfo> list);

    void onRebuildList(WrapperAdapter wrapperAdapter);
}
