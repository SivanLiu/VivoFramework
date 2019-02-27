package com.android.internal.app;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.app.ResolverActivity.TargetInfo;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class DoubleAppResolverActivity extends ResolverActivity {
    private static final String TAG = "DOUBLEAPP";
    private int doubleAppUserId = -10000;
    private UserManager mUserManager;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "DoubleAppResolverActivity onCreate");
        this.mUserManager = (UserManager) getSystemService("user");
        this.doubleAppUserId = this.mUserManager.getDoubleAppUserId();
        Parcelable queryIntent = getIntent().getParcelableExtra("android.intent.extra.INTENT");
        if (queryIntent instanceof Intent) {
            int i;
            Intent intent = new Intent((Intent) queryIntent);
            ArrayList<ResolveInfo> reslInfos = new ArrayList();
            List<ResolveInfo> reslInfos1 = getPackageManager().queryIntentActivities(intent, 65536);
            List<ResolveInfo> reslInfos2 = getPackageManager().queryIntentActivitiesAsUser(intent, 65536, this.doubleAppUserId);
            if (reslInfos1 != null) {
                for (i = 0; i < reslInfos1.size(); i++) {
                    reslInfos.add((ResolveInfo) reslInfos1.get(i));
                }
            }
            if (reslInfos2 != null) {
                for (i = 0; i < reslInfos2.size(); i++) {
                    ResolveInfo reslInfo = new ResolveInfo((ResolveInfo) reslInfos1.get(i));
                    reslInfo.targetUserId = this.doubleAppUserId;
                    reslInfos.add(reslInfo);
                }
            }
            super.onCreate(savedInstanceState, intent, null, null, reslInfos, false);
            return;
        }
        Log.d(TAG, "queryIntent isn't an instanceof Intent");
        finish();
        super.onCreate(null);
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        Log.d(TAG, "DoubleAppResolverActivity onDestroy");
        super.onDestroy();
    }

    protected boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        int targetUserId = target.getResolveInfo().targetUserId;
        StrictMode.disableDeathOnFileUriExposure();
        try {
            target.startAsUser(this, null, new UserHandle(targetUserId));
            return true;
        } finally {
            StrictMode.enableDeathOnFileUriExposure();
        }
    }
}
