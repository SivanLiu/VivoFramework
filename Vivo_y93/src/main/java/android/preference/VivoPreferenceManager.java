package android.preference;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.vivo.internal.R;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
class VivoPreferenceManager {
    private static final int INDEX_BOTH = 4;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CATEGORY = 0;
    private static final int INDEX_NO = 3;
    private static final int INDEX_TOP = 1;
    private static final int KEY = 83886079;
    private static final String MARK = "process_mark";
    private Drawable mAllRound;
    private Drawable mBottomRound;
    private Context mContext;
    private int mPreferenceGap;
    private PreferenceGroup mPreferenceGroup;
    private PreferenceGroupAdapter mPreferenceGroupAdapter;
    private Drawable mTopRound;
    private Drawable mUnRound;

    class VivoPreferenceGroupAdapter extends PreferenceGroupAdapter {
        private static final String TAG = "VivoPreferenceGroupAdapter";
        private int[] position_index;

        public VivoPreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
            initialLittleGroup();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Preference preference = getItem(position);
            if (position >= 0) {
                try {
                    if (position >= getCount() || (preference instanceof PreferenceCategory)) {
                        return view;
                    }
                    int specialBackground = 0;
                    if (getItem(position) instanceof VivoPreferenceBackground) {
                        specialBackground = ((VivoPreferenceBackground) getItem(position)).getBackgroundRes();
                    }
                    int int_position = this.position_index[position];
                    View divider = null;
                    if (int_position != 0) {
                        divider = view.findViewById(R.id.divider);
                        if (divider != null) {
                            divider.setVisibility(8);
                        }
                    }
                    if (specialBackground > 0) {
                        view.setBackgroundResource(specialBackground);
                    } else if (int_position == 1) {
                        view.setBackground(VivoPreferenceManager.this.mTopRound.getConstantState().newDrawable().mutate());
                        if (divider != null) {
                            divider.setVisibility(0);
                        }
                    } else if (int_position == 2) {
                        view.setBackground(VivoPreferenceManager.this.mBottomRound.getConstantState().newDrawable().mutate());
                    } else if (int_position == 3) {
                        view.setBackground(VivoPreferenceManager.this.mUnRound.getConstantState().newDrawable().mutate());
                        if (divider != null) {
                            divider.setVisibility(0);
                        }
                    } else if (int_position == 4) {
                        view.setBackground(VivoPreferenceManager.this.mAllRound.getConstantState().newDrawable().mutate());
                    }
                    return view;
                } catch (Exception e) {
                    Log.e(TAG, "exception...." + e);
                }
            }
            return view;
        }

        public void notifyDataSetChanged() {
            initialLittleGroup();
            super.notifyDataSetChanged();
        }

        private void initialLittleGroup() {
            int total_pref = getCount();
            if (this.position_index == null || this.position_index.length != total_pref) {
                this.position_index = new int[total_pref];
            }
            int i = 0;
            while (i < total_pref) {
                try {
                    if (getItem(i) instanceof PreferenceCategory) {
                        this.position_index[i] = 0;
                    } else if (i == total_pref - 1) {
                        if ((getItem(i - 1) instanceof PreferenceCategory) || total_pref == 1) {
                            this.position_index[i] = 4;
                        } else {
                            this.position_index[i] = 2;
                        }
                    } else if (i == 0) {
                        if (getItem(i + 1) instanceof PreferenceCategory) {
                            this.position_index[i] = 4;
                        } else {
                            this.position_index[i] = 1;
                        }
                    } else if (getItem(i - 1) instanceof PreferenceCategory) {
                        if (getItem(i + 1) instanceof PreferenceCategory) {
                            this.position_index[i] = 4;
                        } else {
                            this.position_index[i] = 1;
                        }
                    } else if (getItem(i + 1) instanceof PreferenceCategory) {
                        this.position_index[i] = 2;
                    } else {
                        this.position_index[i] = 3;
                    }
                    i++;
                } catch (Exception e) {
                    Log.e(TAG, "...exception...." + e);
                    return;
                }
            }
        }
    }

    public VivoPreferenceManager(PreferenceGroup preferenceGroup) {
        this.mContext = preferenceGroup.getContext();
        this.mPreferenceGroup = preferenceGroup;
        TypedArray as = this.mContext.obtainStyledAttributes(null, R.styleable.PreferenceGroup, R.attr.preferenceGroupStyle, R.style.Vigour_PreferenceGroup);
        this.mTopRound = as.getDrawable(0);
        this.mBottomRound = as.getDrawable(1);
        this.mAllRound = as.getDrawable(2);
        this.mUnRound = as.getDrawable(3);
        this.mPreferenceGap = (int) as.getDimension(4, 0.0f);
        as.recycle();
    }

    public void bindList(ListView list) {
        list.setDivider(null);
        list.setSelector((int) com.android.internal.R.color.transparent);
        if (this.mPreferenceGap != 0 && (isMarked(list) ^ 1) != 0) {
            View v = new View(list.getContext());
            v.setVisibility(4);
            v.setLayoutParams(new LayoutParams(-1, this.mPreferenceGap));
            list.addHeaderView(v);
            v = new View(list.getContext());
            v.setVisibility(4);
            v.setLayoutParams(new LayoutParams(-1, this.mPreferenceGap));
            list.addFooterView(v);
            addMark(list);
        }
    }

    private void addMark(ListView list) {
        list.setTag(KEY, MARK);
    }

    private boolean isMarked(ListView list) {
        Object tag = list.getTag(KEY);
        if (tag != null && (tag instanceof String) && tag.equals(MARK)) {
            return true;
        }
        return false;
    }

    public ListAdapter getPreferenceGroupAdapter() {
        if (this.mPreferenceGroupAdapter == null) {
            this.mPreferenceGroupAdapter = new VivoPreferenceGroupAdapter(this.mPreferenceGroup);
        }
        return this.mPreferenceGroupAdapter;
    }
}
