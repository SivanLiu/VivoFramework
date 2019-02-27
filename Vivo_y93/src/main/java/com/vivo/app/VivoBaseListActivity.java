package com.vivo.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoBaseListActivity extends VivoBaseActivity {
    private ListAdapter mAdapter;
    private boolean mFinishedStart = false;
    private Handler mHandler = new Handler();
    private ListView mList;
    private OnItemClickListener mOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            VivoBaseListActivity.this.onListItemClick((ListView) parent, v, position, id);
        }
    };
    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            VivoBaseListActivity.this.mList.focusableViewAvailable(VivoBaseListActivity.this.mList);
        }
    };

    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    protected void onDestroy() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        super.onDestroy();
    }

    private void initListView() {
        View emptyView = findViewById(16908292);
        this.mList = (ListView) findViewById(16908298);
        if (this.mList == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
        }
        if (emptyView != null) {
            this.mList.setEmptyView(emptyView);
        }
        this.mList.setOnItemClickListener(this.mOnClickListener);
        if (this.mFinishedStart) {
            setListAdapter(this.mAdapter);
        }
        this.mHandler.post(this.mRequestFocus);
        this.mFinishedStart = true;
    }

    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        initListView();
    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initListView();
    }

    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            this.mAdapter = adapter;
            this.mList.setAdapter(adapter);
        }
    }

    public void setSelection(int position) {
        this.mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        return this.mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        return this.mList.getSelectedItemId();
    }

    public ListView getListView() {
        ensureList();
        return this.mList;
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            setContentView(17367160);
        }
    }
}
