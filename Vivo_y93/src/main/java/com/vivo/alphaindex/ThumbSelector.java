package com.vivo.alphaindex;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageButton;
import com.vivo.internal.R;
import java.util.ArrayList;
import java.util.List;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class ThumbSelector extends ImageButton {
    private boolean isNeedUpdate;
    private List<String> mAlphabet;
    private int mAlphabetOffset;
    private int mAlphabetPrePos;
    private List<Bitmap> mFooter;
    private List<Bitmap> mHeader;
    private OnSlideListener mListener;
    private Paint mPaint;
    private int mTextHeight;
    private int mTextWidth;

    public interface OnSlideListener {
        void onSlide(View view, int i);

        void onSlideEnd(View view);

        void onSlideStart(View view, int i);
    }

    public ThumbSelector(Context context) {
        this(context, null);
    }

    public ThumbSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 50397225);
    }

    public ThumbSelector(Context context, AttributeSet attrs, int defAttr) {
        super(context, attrs, defAttr);
        this.mPaint = new Paint(1);
        this.mTextHeight = 0;
        this.mTextWidth = 0;
        this.mAlphabet = new ArrayList();
        this.mHeader = new ArrayList();
        this.mFooter = new ArrayList();
        this.isNeedUpdate = true;
        this.mAlphabetPrePos = -1;
        this.mAlphabetOffset = 0;
        this.mListener = null;
        float density = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThumbSelector, defAttr, 0);
        this.mPaint.setTextSize(a.getDimension(0, 14.0f * density));
        this.mPaint.setColor(a.getColor(1, -16777216));
        this.mPaint.setAntiAlias(true);
        a.recycle();
    }

    private int itemCount() {
        return (this.mAlphabet.size() + this.mHeader.size()) + this.mFooter.size();
    }

    protected Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int alphabetStartPos = this.mAlphabetOffset + getPaddingTop();
        if (itemCount() > 0 && this.mTextHeight > 0) {
            int alphabetPos = (int) ((event.getY() - ((float) alphabetStartPos)) / ((float) this.mTextHeight));
            Log.d("Thumb", "paddingTop = " + alphabetStartPos + " eventY = " + event.getY());
            Log.d("Thumb", "alphabetNumber = " + itemCount() + "  alphbetPos = " + alphabetPos);
            if (alphabetPos >= itemCount() || alphabetPos < 0) {
                alphabetPos = -1;
            }
            if (alphabetPos >= 0 || event.getAction() != 0) {
                switch (event.getAction()) {
                    case 0:
                        if (this.mListener != null) {
                            this.mListener.onSlideStart(this, alphabetPos);
                            break;
                        }
                        break;
                    case 1:
                    case 3:
                        this.mAlphabetPrePos = -1;
                        if (this.mListener != null) {
                            this.mListener.onSlideEnd(this);
                            break;
                        }
                        break;
                    case 2:
                        if (!(this.mListener == null || alphabetPos < 0 || alphabetPos == this.mAlphabetPrePos)) {
                            this.mListener.onSlide(this, alphabetPos);
                            this.mAlphabetPrePos = alphabetPos;
                            break;
                        }
                }
            }
            setOnTouchEvent(event, alphabetPos);
        }
        return super.onTouchEvent(event);
    }

    protected void setOnTouchEvent(MotionEvent event, int alphabetPos) {
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = getMeasuredWidth();
        int heightSize = getMeasuredHeight();
        if (widthMode != 1073741824) {
            int width = 0;
            for (String str : this.mAlphabet) {
                if (width < ((int) this.mPaint.measureText(str))) {
                    width = (int) this.mPaint.measureText(str);
                }
            }
            width += getPaddingLeft() + getPaddingRight();
            if (width > widthSize) {
                widthSize = width;
            }
        }
        if (heightMode != 1073741824) {
            FontMetrics metrics = this.mPaint.getFontMetrics();
            int height = ((itemCount() * ((int) Math.abs(metrics.bottom - metrics.top))) + getPaddingTop()) + getPaddingBottom();
            if (height > heightSize) {
                heightSize = height;
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        createIfNeed(right - left, bottom - top);
    }

    /* JADX WARNING: Missing block: B:3:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createIfNeed(int width, int height) {
        int contentWidth = (width - getPaddingLeft()) - getPaddingRight();
        int contentHeight = (height - getPaddingTop()) - getPaddingBottom();
        if (contentWidth > 0 && contentHeight > 0 && this.isNeedUpdate && itemCount() > 0) {
            this.mTextHeight = contentHeight / itemCount();
            this.mTextWidth = contentWidth;
            this.mAlphabetOffset = (contentHeight - (itemCount() * this.mTextHeight)) / 2;
            this.mAlphabetOffset = this.mAlphabetOffset > 0 ? this.mAlphabetOffset : 0;
            Log.d("Thumb", "contentHeight = " + contentHeight + " TextHeight = " + this.mTextHeight);
            setImageBitmap(createAlphabetBitmap(contentWidth, contentHeight));
            this.isNeedUpdate = false;
        }
    }

    private Bitmap createAlphabetBitmap(int width, int height) {
        Rect rect;
        float left;
        float top;
        Canvas alphabetCanvas = new Canvas();
        Bitmap map = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        FontMetrics metrics = this.mPaint.getFontMetrics();
        int index = 0;
        int letterWidth = this.mTextWidth;
        int letterHeight = this.mTextHeight;
        alphabetCanvas.setBitmap(map);
        for (Bitmap head : this.mHeader) {
            rect = new Rect(0, 0, head.getWidth(), head.getHeight());
            if (letterWidth > head.getWidth()) {
                left = (float) ((letterWidth - head.getWidth()) / 2);
            } else {
                left = 0.0f;
            }
            top = (float) ((index * letterHeight) + this.mAlphabetOffset);
            alphabetCanvas.drawBitmap(head, rect, new Rect((int) left, (int) top, letterWidth, (int) (((float) letterHeight) + top)), this.mPaint);
            index++;
        }
        int fontHeight = (int) Math.abs(metrics.top - metrics.bottom);
        for (String str : this.mAlphabet) {
            float f;
            float abs;
            if (((float) letterWidth) > this.mPaint.measureText(str)) {
                left = (((float) letterWidth) - this.mPaint.measureText(str)) / 2.0f;
            } else {
                left = 0.0f;
            }
            top = (float) this.mAlphabetOffset;
            if (letterHeight > fontHeight) {
                f = (float) (((index * letterHeight) + ((letterHeight - fontHeight) / 2)) + fontHeight);
                abs = Math.abs(metrics.descent);
            } else {
                f = (float) ((index + 1) * letterHeight);
                abs = Math.abs(metrics.descent);
            }
            top += f - abs;
            Log.d("Thumb", str + " : [ " + left + " " + top + " ]");
            alphabetCanvas.drawText(str, left, top, this.mPaint);
            index++;
        }
        for (Bitmap foot : this.mFooter) {
            rect = new Rect(0, 0, foot.getWidth(), foot.getHeight());
            if (letterWidth > foot.getWidth()) {
                left = (float) ((letterWidth - foot.getWidth()) / 2);
            } else {
                left = 0.0f;
            }
            top = (float) ((index * letterHeight) + this.mAlphabetOffset);
            alphabetCanvas.drawBitmap(foot, rect, new Rect((int) left, (int) top, letterWidth, (int) (((float) letterHeight) + top)), this.mPaint);
            index++;
        }
        return map;
    }

    public void setSlideListener(OnSlideListener listener) {
        this.mListener = listener;
    }

    public void updateThumbSelector() {
        if (getVisibility() == 0) {
            this.isNeedUpdate = true;
            requestLayout();
        }
    }

    public void setAlphabet(List<String> data) {
        this.mAlphabet.clear();
        if (data != null) {
            for (String str : data) {
                this.mAlphabet.add(str);
            }
        }
        updateThumbSelector();
    }

    public List<String> getAlphabet() {
        return this.mAlphabet;
    }

    public void addHeader(Bitmap bitmap) {
        if (bitmap != null) {
            this.mHeader.add(bitmap);
            updateThumbSelector();
        }
    }

    public Bitmap addHeader(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = drawableToBitmap(drawable);
        addHeader(bitmap);
        return bitmap;
    }

    public void delHeader(Bitmap bitmap) {
        if (bitmap != null) {
            this.mHeader.remove(bitmap);
            updateThumbSelector();
        }
    }

    public List<Bitmap> getHeader() {
        return this.mHeader;
    }

    public void addFooter(Bitmap bitmap) {
        if (bitmap != null) {
            this.mFooter.add(bitmap);
            updateThumbSelector();
        }
    }

    public Bitmap addFooter(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = drawableToBitmap(drawable);
        this.mFooter.add(bitmap);
        return bitmap;
    }

    public void delFooter(Bitmap bitmap) {
        if (bitmap != null) {
            this.mFooter.remove(bitmap);
            updateThumbSelector();
        }
    }

    public List<Bitmap> getFooter() {
        return this.mFooter;
    }

    public void setTextColor(int color) {
        this.mPaint.setColor(color);
        updateThumbSelector();
    }

    public int getTextColor() {
        return this.mPaint.getColor();
    }

    public void setTextSize(float size) {
        this.mPaint.setTextSize(size);
        updateThumbSelector();
    }

    public float getTextSize() {
        return this.mPaint.getTextSize();
    }
}
