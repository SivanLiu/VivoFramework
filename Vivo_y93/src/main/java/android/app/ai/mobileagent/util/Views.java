package android.app.ai.mobileagent.util;

import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public final class Views {
    private static final String TAG = "Views";

    private static class FinderByType<T extends View> implements Processor {
        private final Class<T> type;
        private final List<T> views;

        /* synthetic */ FinderByType(Class type, FinderByType -this1) {
            this(type);
        }

        private FinderByType(Class<T> type) {
            this.type = type;
            this.views = new ArrayList();
        }

        public List<T> getViews() {
            return this.views;
        }

        public void process(View view) {
            if (this.type.isInstance(view)) {
                this.views.add(view);
            }
        }
    }

    public static <T extends View> List<T> find(View view, Class<T> clazz) {
        if (view == null || !(view instanceof ViewGroup)) {
            return null;
        }
        FinderByType<T> finderByType = new FinderByType(clazz, null);
        LayoutTraverser.build(finderByType).traverse((ViewGroup) view);
        return finderByType.getViews();
    }
}
