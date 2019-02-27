package android.app.ai.mobileagent.util;

import android.view.View;
import android.view.ViewGroup;

class LayoutTraverser {
    private final Processor processor;

    interface Processor {
        void process(View view);
    }

    private LayoutTraverser(Processor processor) {
        this.processor = processor;
    }

    static LayoutTraverser build(Processor processor) {
        return new LayoutTraverser(processor);
    }

    void traverse(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            this.processor.process(child);
            if (child instanceof ViewGroup) {
                traverse((ViewGroup) child);
            }
        }
    }
}
