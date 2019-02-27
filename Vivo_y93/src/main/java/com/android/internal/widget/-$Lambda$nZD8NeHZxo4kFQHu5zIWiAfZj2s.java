package com.android.internal.widget;

import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

final /* synthetic */ class -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s implements OnMenuItemClickListener {
    public static final /* synthetic */ -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s $INST$0 = new -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s();

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$1 */
    final /* synthetic */ class AnonymousClass1 implements OnComputeInternalInsetsListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f144-$f0;

        private final /* synthetic */ void $m$0(InternalInsetsInfo arg0) {
            ((FloatingToolbarPopup) this.f144-$f0).m55xf8980449(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f144-$f0 = obj;
        }

        public final void onComputeInternalInsets(InternalInsetsInfo internalInsetsInfo) {
            $m$0(internalInsetsInfo);
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$2 */
    final /* synthetic */ class AnonymousClass2 implements OnClickListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f145-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f146-$f1;

        private final /* synthetic */ void $m$0(View arg0) {
            ((FloatingToolbarPopup) this.f145-$f0).m56xf8ec1899((ImageButton) this.f146-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f145-$f0 = obj;
            this.f146-$f1 = obj2;
        }

        public final void onClick(View view) {
            $m$0(view);
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$3 */
    final /* synthetic */ class AnonymousClass3 implements OnItemClickListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f147-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f148-$f1;

        private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
            ((FloatingToolbarPopup) this.f147-$f0).m57xf8ecf614((OverflowPanel) this.f148-$f1, arg0, arg1, arg2, arg3);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.f147-$f0 = obj;
            this.f148-$f1 = obj2;
        }

        public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
            $m$0(adapterView, view, i, j);
        }
    }

    private final /* synthetic */ boolean $m$0(MenuItem arg0) {
        return false;
    }

    private /* synthetic */ -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s() {
    }

    public final boolean onMenuItemClick(MenuItem menuItem) {
        return $m$0(menuItem);
    }
}
