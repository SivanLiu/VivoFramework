package android.app.ai.beeservice.capture.types.profile;

import android.app.Activity;
import android.app.ai.beeservice.capture.util.CaptureBase;
import android.app.ai.beeservice.capture.util.CaptureConstants;
import android.app.ai.mobileagent.util.Views;
import android.app.mobileagent.util.LogUtil;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityInfoCapture extends CaptureBase {
    private static final String TAG = "ActivityInfoCapture";
    public boolean isSuccess = false;

    public ActivityInfoCapture(Activity activity) {
        super(activity);
    }

    private boolean isMatchs(String s, String s2) {
        return s != null ? s2.matches(s) : false;
    }

    private <T extends View> boolean isEmptyViews(List<T> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    private JSONObject captureByLabel(View view) {
        JSONException e;
        LogUtil.d(TAG, "Label");
        JSONObject personJsonObject = null;
        List<TextView> matchTextViews = Views.find(view, TextView.class);
        if (!isEmptyViews(matchTextViews)) {
            int length = matchTextViews.size();
            try {
                String titleValue = "";
                String title = "";
                String value = "";
                int i = 0;
                while (i < length) {
                    titleValue = getIndexTextViewValue(matchTextViews, length, i, false, 1);
                    title = string2Unicode(titleValue);
                    Iterator entry$iterator = this.mCurrentPageProperties.entrySet().iterator();
                    while (true) {
                        JSONObject personJsonObject2;
                        try {
                            personJsonObject2 = personJsonObject;
                            if (!entry$iterator.hasNext()) {
                                personJsonObject = personJsonObject2;
                                break;
                            }
                            Entry<String, String> entry = (Entry) entry$iterator.next();
                            String key = (String) entry.getKey();
                            String mapvalue = (String) entry.getValue();
                            if (personJsonObject2 == null) {
                                personJsonObject = new JSONObject();
                            } else {
                                personJsonObject = personJsonObject2;
                            }
                            if (!(TextUtils.isEmpty(titleValue) || (TextUtils.isEmpty(title) ^ 1) == 0 || (TextUtils.isEmpty(mapvalue) ^ 1) == 0 || !title.equals(mapvalue) || (personJsonObject.has(key) ^ 1) == 0)) {
                                value = getIndexTextViewValue(matchTextViews, length, i, true, 1);
                                if (!TextUtils.isEmpty(value)) {
                                    personJsonObject.put((String) entry.getKey(), value);
                                    i++;
                                    break;
                                }
                            }
                        } catch (JSONException e2) {
                            e = e2;
                            personJsonObject = personJsonObject2;
                            LogUtil.e(TAG, "JSONException e: " + e);
                            return personJsonObject;
                        }
                    }
                    i++;
                }
            } catch (JSONException e3) {
                e = e3;
                LogUtil.e(TAG, "JSONException e: " + e);
                return personJsonObject;
            }
        }
        return personJsonObject;
    }

    private JSONObject captureByLabelSub(View view) {
        JSONException e;
        LogUtil.d(TAG, "LabelSub");
        JSONObject personJsonObject = null;
        List<TextView> matchTextViews = Views.find(view, TextView.class);
        if (!isEmptyViews(matchTextViews)) {
            int length = matchTextViews.size();
            try {
                String titleValue = "";
                String title = "";
                String value = "";
                int i = 0;
                while (i < length) {
                    titleValue = getIndexTextViewValue(matchTextViews, length, i, false, 1);
                    title = string2Unicode(titleValue);
                    Iterator entry$iterator = this.mCurrentPageProperties.entrySet().iterator();
                    while (true) {
                        JSONObject personJsonObject2;
                        try {
                            personJsonObject2 = personJsonObject;
                            if (!entry$iterator.hasNext()) {
                                personJsonObject = personJsonObject2;
                                break;
                            }
                            Entry<String, String> entry = (Entry) entry$iterator.next();
                            String key = (String) entry.getKey();
                            String mapvalue = (String) entry.getValue();
                            if (personJsonObject2 == null) {
                                personJsonObject = new JSONObject();
                            } else {
                                personJsonObject = personJsonObject2;
                            }
                            if (!(TextUtils.isEmpty(titleValue) || (TextUtils.isEmpty(title) ^ 1) == 0 || (TextUtils.isEmpty(mapvalue) ^ 1) == 0 || !title.equals(mapvalue) || (personJsonObject.has(key) ^ 1) == 0)) {
                                value = getIndexTextViewValueSub(matchTextViews, length, i, true, 1);
                                if (!TextUtils.isEmpty(value)) {
                                    personJsonObject.put((String) entry.getKey(), value);
                                    i++;
                                    break;
                                }
                            }
                        } catch (JSONException e2) {
                            e = e2;
                            personJsonObject = personJsonObject2;
                            LogUtil.e(TAG, "JSONException e: " + e);
                            return personJsonObject;
                        }
                    }
                    i++;
                }
            } catch (JSONException e3) {
                e = e3;
                LogUtil.e(TAG, "JSONException e: " + e);
                return personJsonObject;
            }
        }
        return personJsonObject;
    }

    private String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        if (!TextUtils.isEmpty(string)) {
            for (int i = 0; i < string.length(); i++) {
                unicode.append("\\u" + Integer.toHexString(string.charAt(i)));
            }
        }
        return unicode.toString();
    }

    private JSONObject captureById(View view) {
        JSONException e2;
        if (view == null) {
            return null;
        }
        LogUtil.d(TAG, "Id");
        JSONObject personJsonObject = null;
        try {
            Iterator view2$iterator = Views.find(view, TextView.class).iterator();
            while (true) {
                JSONObject personJsonObject2;
                try {
                    personJsonObject2 = personJsonObject;
                    if (!view2$iterator.hasNext()) {
                        personJsonObject = personJsonObject2;
                        break;
                    }
                    TextView view2 = (TextView) view2$iterator.next();
                    Object object = view2.getText();
                    String viewId = getViewId(view2);
                    for (Entry<String, String> entry : this.mCurrentPageProperties.entrySet()) {
                        if (!TextUtils.isEmpty((CharSequence) object) && (TextUtils.isEmpty(viewId) ^ 1) != 0 && viewId.equals(entry.getValue())) {
                            if (personJsonObject2 == null) {
                                personJsonObject = new JSONObject();
                            } else {
                                personJsonObject = personJsonObject2;
                            }
                            personJsonObject.put((String) entry.getKey(), object);
                        }
                    }
                    personJsonObject = personJsonObject2;
                } catch (JSONException e) {
                    e2 = e;
                    personJsonObject = personJsonObject2;
                    LogUtil.e(TAG, "JSONException e2: " + e2);
                    return personJsonObject;
                }
            }
        } catch (JSONException e3) {
            e2 = e3;
        }
        return personJsonObject;
    }

    private JSONObject captureByRegular(View view) {
        JSONException e2;
        if (view == null) {
            return null;
        }
        LogUtil.d(TAG, "Regular");
        JSONObject personJsonObject = null;
        try {
            Iterator view2$iterator = Views.find(view, TextView.class).iterator();
            while (true) {
                JSONObject personJsonObject2;
                try {
                    personJsonObject2 = personJsonObject;
                    if (!view2$iterator.hasNext()) {
                        personJsonObject = personJsonObject2;
                        break;
                    }
                    TextView view2 = (TextView) view2$iterator.next();
                    this.isSuccess = true;
                    String text = view2.getText().toString();
                    for (Entry<String, String> entry : this.mCurrentPageProperties.entrySet()) {
                        if (!TextUtils.isEmpty(text) && isMatchs((String) entry.getValue(), text)) {
                            if (personJsonObject2 == null) {
                                personJsonObject = new JSONObject();
                            } else {
                                personJsonObject = personJsonObject2;
                            }
                            personJsonObject.put((String) entry.getKey(), text);
                        }
                    }
                    personJsonObject = personJsonObject2;
                } catch (JSONException e) {
                    e2 = e;
                    personJsonObject = personJsonObject2;
                    LogUtil.e(TAG, "JSONException e2: " + e2);
                    return personJsonObject;
                }
            }
        } catch (JSONException e3) {
            e2 = e3;
        }
        return personJsonObject;
    }

    private String getIndexTextViewValue(List<TextView> list, int size, int n2, boolean b, int n3) {
        int n4 = n2;
        if (b) {
            n4 = n2 + n3;
        }
        if (n4 < size) {
            TextView textView = (TextView) list.get(n4);
            if (textView == null) {
                return "";
            }
            CharSequence text = textView.getText();
            if (!TextUtils.isEmpty(text)) {
                return text.toString();
            }
        }
        return "";
    }

    private String getIndexTextViewValueSub(List<TextView> list, int size, int n2, boolean b, int n3) {
        int n4 = n2;
        if (b) {
            n4 = n2 - n3;
        }
        if (n4 < size) {
            TextView textView = (TextView) list.get(n4);
            if (textView == null) {
                return "";
            }
            CharSequence text = textView.getText();
            if (!TextUtils.isEmpty(text)) {
                return text.toString();
            }
        }
        return "";
    }

    public JSONObject captureView() {
        JSONObject jsonObject = null;
        try {
            ArrayList<String> currentPageCaptureType = this.mCurrentPageCaptureType;
            String infotype = this.mCurrentPageInfoType;
            View decorView = this.mActivity.getWindow().getDecorView();
            if (!(decorView == null || currentPageCaptureType == null || currentPageCaptureType.size() <= 0 || infotype == null)) {
                for (int i = 0; i < currentPageCaptureType.size(); i++) {
                    String pagetype = (String) currentPageCaptureType.get(i);
                    Log.d(TAG, "type:" + pagetype);
                    int type = Integer.parseInt(pagetype);
                    if (type > 0) {
                        this.mCurrentPageProperties = this.mUserProfilingHelper.getPropertiesMapByPagetype(this.mCurrentPagePkg, this.mCurrentPageName, pagetype, this.mActivity);
                        if (this.mCurrentPageProperties != null && this.mCurrentPageProperties.size() > 0) {
                            switch (type) {
                                case 1:
                                    jsonObject = captureByLabel(decorView);
                                    break;
                                case 2:
                                    jsonObject = captureById(decorView);
                                    break;
                                case 3:
                                    jsonObject = captureByRegular(decorView);
                                    break;
                                case 4:
                                    jsonObject = captureByLabelSub(decorView);
                                    break;
                                default:
                                    return null;
                            }
                            if (jsonObject != null && jsonObject.length() > 0) {
                                this.isSuccess = true;
                                this.mUserProfilingHelper.reportDataToBee(CaptureConstants.SUFFIX_USER_PROFILE_URI, jsonObject.toString(), this.mActivity, this.mCurrentPagePkg, infotype);
                            } else if (jsonObject == null && "com.tencent.mm".equals(this.mCurrentPagePkg) && "WebViewUI".equals(this.mCurrentPageName)) {
                                this.mUserProfilingHelper.setWeChatWebViewValue(null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e);
        }
        return jsonObject;
    }
}
