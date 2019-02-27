package com.vivo.common;

import android.content.Context;
import android.os.FtBuild;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (FtBuild.getRomVersion() >= 4.5f) {
            setSingleLine();
            setEllipsize(TruncateAt.MARQUEE);
            setFocusable(true);
        }
    }

    public boolean isFocused() {
        if (FtBuild.getRomVersion() >= 4.5f) {
            return true;
        }
        return super.isFocused();
    }
}
