package android.preference;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.BbkMoveBoolButton;
import android.widget.BbkMoveBoolButton.OnCheckedChangeListener;
import android.widget.BbkMoveBoolButton.Status;
import com.android.internal.R;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoCheckBoxPreference extends CheckBoxPreference {
    private View mCheckBoxView;
    private boolean mIsLoading;
    private boolean mIsWaitingForLoadingEnd;
    private boolean mIsWaitingForLoadingStart;
    private boolean mSendClickAccessibilityEventForVivo;
    private boolean mTextAreaClickable;

    public VivoCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTextAreaClickable = false;
        this.mIsWaitingForLoadingStart = false;
        this.mIsWaitingForLoadingEnd = false;
        this.mIsLoading = false;
        initLayout(context, attrs);
    }

    public VivoCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTextAreaClickable = false;
        this.mIsWaitingForLoadingStart = false;
        this.mIsWaitingForLoadingEnd = false;
        this.mIsLoading = false;
        initLayout(context, attrs);
    }

    public VivoCheckBoxPreference(Context context) {
        this(context, null);
    }

    private void initLayout(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        int layout = a.getResourceId(3, com.vivo.internal.R.layout.vigour_preference);
        int widgetLayout = a.getResourceId(9, com.vivo.internal.R.layout.vigour_preference_widget_checkbox);
        a.recycle();
        setLayoutResource(layout);
        setWidgetLayoutResource(widgetLayout);
    }

    public void startLoading() {
        if (this.mCheckBoxView == null) {
            this.mIsWaitingForLoadingStart = true;
            this.mIsLoading = true;
            return;
        }
        this.mIsWaitingForLoadingStart = false;
        if (this.mCheckBoxView instanceof BbkMoveBoolButton) {
            ((BbkMoveBoolButton) this.mCheckBoxView).startLoading();
            this.mIsLoading = true;
        }
    }

    public void endLoading() {
        if (this.mCheckBoxView == null) {
            this.mIsWaitingForLoadingEnd = true;
            this.mIsLoading = false;
            return;
        }
        this.mIsWaitingForLoadingEnd = false;
        if (this.mCheckBoxView instanceof BbkMoveBoolButton) {
            ((BbkMoveBoolButton) this.mCheckBoxView).endLoading();
            this.mIsLoading = false;
        }
    }

    public boolean isLoading() {
        boolean z = true;
        if (this.mCheckBoxView == null) {
            if (!this.mIsWaitingForLoadingStart) {
                z = this.mIsLoading;
            }
            return z;
        } else if (!(this.mCheckBoxView instanceof BbkMoveBoolButton)) {
            return false;
        } else {
            if (!this.mIsLoading) {
                z = ((BbkMoveBoolButton) this.mCheckBoxView).isLoading();
            }
            return z;
        }
    }

    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        if (this.mCheckBoxView != null && (this.mCheckBoxView instanceof BbkMoveBoolButton)) {
            ((BbkMoveBoolButton) this.mCheckBoxView).removeAnimation();
            this.mIsLoading = false;
        }
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        Status status = null;
        this.mCheckBoxView = view.findViewById(R.id.checkbox);
        if (this.mCheckBoxView != null && (this.mCheckBoxView instanceof BbkMoveBoolButton)) {
            ((BbkMoveBoolButton) this.mCheckBoxView).setLoadingStatu(this.mIsLoading);
            status = ((BbkMoveBoolButton) this.mCheckBoxView).getStatus();
        }
        if (this.mCheckBoxView instanceof BbkMoveBoolButton) {
            if (status != null) {
                ((BbkMoveBoolButton) this.mCheckBoxView).startLoading(status);
            }
            ((BbkMoveBoolButton) this.mCheckBoxView).setOnBBKCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(BbkMoveBoolButton buttonView, boolean isChecked) {
                    if (isChecked != VivoCheckBoxPreference.this.isChecked()) {
                        boolean newValue = VivoCheckBoxPreference.this.isChecked() ^ 1;
                        VivoCheckBoxPreference.this.mSendClickAccessibilityEventForVivo = true;
                        if (VivoCheckBoxPreference.this.callChangeListener(Boolean.valueOf(newValue))) {
                            VivoCheckBoxPreference.this.setChecked(newValue);
                        } else if (buttonView != null) {
                            buttonView.setChecked(newValue ^ 1);
                        }
                        if (!(VivoCheckBoxPreference.this.getPreferenceManager().getOnPreferenceTreeClickListener() == null || VivoCheckBoxPreference.this.mTextAreaClickable)) {
                            VivoCheckBoxPreference.this.getPreferenceManager().getOnPreferenceTreeClickListener().onPreferenceTreeClick(VivoCheckBoxPreference.this.getPreferenceManager().getPreferenceScreen(), VivoCheckBoxPreference.this);
                        }
                        VivoCheckBoxPreference.this.onClick();
                        VivoCheckBoxPreference.this.preClick();
                    }
                }
            });
            if (this.mTextAreaClickable) {
                this.mCheckBoxView.setFocusable(false);
                this.mCheckBoxView.setClickable(false);
            }
        }
        if (this.mIsWaitingForLoadingStart) {
            this.mIsWaitingForLoadingStart = false;
            startLoading();
        }
        if (this.mIsWaitingForLoadingEnd) {
            this.mIsWaitingForLoadingEnd = false;
            endLoading();
        }
    }

    void sendAccessibilityEvent(View view) {
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(getContext());
        if (this.mSendClickAccessibilityEventForVivo && accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(1);
            view.onInitializeAccessibilityEvent(event);
            view.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }
        this.mSendClickAccessibilityEventForVivo = false;
    }

    private void preClick() {
        if (super.getOnPreferenceClickListener() != null) {
            super.getOnPreferenceClickListener().onPreferenceClick(this);
        }
    }

    protected void onClick() {
        if (!(this.mCheckBoxView instanceof BbkMoveBoolButton)) {
            this.mSendClickAccessibilityEventForVivo = true;
            super.onClick();
        }
    }

    public void setTextAreaClickable(boolean clickable) {
        this.mTextAreaClickable = clickable;
    }
}
